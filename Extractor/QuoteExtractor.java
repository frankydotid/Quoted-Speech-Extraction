import java.util.*;
import java.io.*;
import java.util.regex.*;

/**
	Class to automatically collects quotes from news together
	with the information of the person who speaks the quote,
	the topic of the quote, and the related news. 
*/
public class QuoteExtractor {
		
	/**
		Extract quotes from given path that contains dir of topics. The mainPath contains news
		that are already grouped by their topics.
	*/
	public static Vector<Quote> extractQuotes(String mainPath) throws IOException {
		Vector<Quote> quotes = new Vector<Quote>(); // prepare container for quotes found
		
		// get the topics
		String topicDirs[] = (new File(mainPath)).list();
		
		// get quotes for each topic
		for(String topicDir : topicDirs) {
			// get topic id
			String pathComponents[] = topicDir.split("[\\|/]");
			String topicId = pathComponents[pathComponents.length-1];
			
			// get the list of news to be extracted
			String newsFiles[] = (new File(mainPath+"/"+topicDir)).list();
			
			// extract quotes from each news
			for(String newsFile: newsFiles) {
				Vector<Quote> quotesFromNews = extractQuotesFromNews(topicId, 
													new News(mainPath+"/"+topicDir+"/"+newsFile));
				
				// add quotes found from this news to collection of all quotes
				addQuotesTo(quotesFromNews, quotes);
			}
			
		}
		
		return quotes;
	}
	
	/**
		Extract quotes from single news
	*/
	private static Vector<Quote> extractQuotesFromNews(String topicId, News news) {
		Vector<Quote> quotesFromNews = new Vector<Quote>();
		Vector<String> namedEntities = new Vector<String>();

		// get news id and content
		String newsId = news.getDocId();
		String content = news.getContent();
		
		// get Sentences from the news
		Vector<Sentence> sentences = TextUtility.parseAsSentences(content);
		
		// extract the possible named entities
		for(Sentence sentence: sentences) {
			Vector<String> namedEntitiesInSentence = extractNamedEntities(sentence);
		
			for(String namedEntity: namedEntitiesInSentence)
				namedEntities.add(namedEntity);
		}
		
		// candidate names to be used in selecting replacement for reference
		Hashtable<Integer, String> candidateNames = new Hashtable<Integer, String>();
		
		// extract quotes with explicit speaker, return names used
		extractQuotedSpeechWithExplicitName(quotesFromNews, sentences, namedEntities, candidateNames, 
												topicId, newsId);
		
		generateCandidateNames(quotesFromNews, sentences, candidateNames);
		
		// extract quotes with reference to the speaker's name
		extractQuotedSpeechWithReference(quotesFromNews, sentences, namedEntities, candidateNames, 
												topicId, newsId);
		
		return quotesFromNews;
	}
	
	/**
		Extract quotes with explicit speaker's name
	*/
	private static void extractQuotedSpeechWithExplicitName(Vector<Quote> quotesFromNews, 
								Vector<Sentence> sentences, Vector<String> namedEntities, 
								Hashtable<Integer, String> candidateNames,
								String topicId, String newsId) {
		int currSentenceNum = 0;
		
		for(Sentence sentence: sentences) {
			if(sentence.containsQuotedSpeech()) {
				int idxFirstQuote = 0;
				int idxLastQuote = 0;
				
				// get quotes position
				if((idxFirstQuote=sentence.indexOf(new Token("\""),0))>-1) {
					idxLastQuote = sentence.indexOf(new Token("\""), idxFirstQuote+1);
				} else {
					idxFirstQuote = sentence.indexOf(new Token("”"),0);
					idxLastQuote = sentence.indexOf(new Token("”"),idxFirstQuote+1);
				}
				
				boolean containsIndicator = false;
				int idxIndicator = -1;
				
				// detect name indicator words after the last quote only
				for(int i=0;i<speakerIndicator.length;i++) {
					if((idxIndicator=sentence.indexOf(new Token(speakerIndicator[i]), 
								idxLastQuote+1))>-1) {
						containsIndicator = true;
						break;
					}
				}
				
				// get quote and name of speaker
				if(containsIndicator) {
					// using simple rule, get the first sequence of words with all words has capitalized first character
					String name = sentence.getFirstCapSeqOfTerms(idxIndicator+1, 
													sentence.numOfTokens()-1);
					
					if(name.length() > 0) {
						// get the longest name for this name mentioned in this news
						String longestName = getLongestNameFor(name, namedEntities);
						
						// no full name for this name, use current name
						if(longestName.equals("NONE"))
							longestName = name;	
						
						// add name for candidates of reference
						candidateNames.put(currSentenceNum, longestName);
						
						// add quote found
						quotesFromNews.add(new Quote(longestName, longestName, 
								sentence.subsentence(idxFirstQuote, idxLastQuote).toString(),
								topicId, newsId));
					}
				}
			}
			currSentenceNum++;
		}
	}
	
	/**
		Generate candidates name for reference
	*/
	private static void generateCandidateNames(Vector<Quote> quotesFromNews, 
													Vector<Sentence> sentences,
													Hashtable<Integer, String> candidateNames) {
		String candidate = "";
		int currSentenceNum = 0;
		
		for(Sentence sentence: sentences) {
			// ignore sentence with quoted speech
			if(sentence.containsQuotedSpeech()) {
				currSentenceNum++;
				continue;
			}
			
			Vector<Token> tokens = sentence.getTokens();
			
			boolean containsIndicatorAfterName = false;
			boolean containsIndicatorBeforeName = false;
			
			int idxIndicator = -1;
			for(Token t: tokens) {
				if(isContainedIn(t.getToken().toLowerCase(), beforeNameIndicator)) {
					idxIndicator = sentence.indexOf(t, 0);

					candidate = sentence.getFirstCapSeqOfTerms(idxIndicator+1, sentence.numOfTokens());
					
					containsIndicatorBeforeName = true;
					
					if(candidate.equals("")) {
						continue;
					} else {
						candidateNames.put(currSentenceNum, candidate);
						break;
					}
				}
			}
			
			if(!containsIndicatorBeforeName) {
				idxIndicator = -1;
				candidate = "";
				
				for(Token t: tokens) {
					if(isContainedIn(t.getToken().toLowerCase(), afterNameIndicator)) {
						idxIndicator = sentence.indexOf(t, 0);
						
						candidate = sentence.getFirstCapSeqOfTermsRev(idxIndicator-1, 0);
						
						containsIndicatorAfterName = true;
						
						if(candidate.equals("")) {
							continue;
						} else {
							candidateNames.put(currSentenceNum, candidate);
							break;
						}
					}
				}
			}
			currSentenceNum++;
		}
	}
	
	/**
		Extract quoted speech with reference (no explicit speaker's name)
	*/
	private static void extractQuotedSpeechWithReference(Vector<Quote> quotesFromNews, 
								Vector<Sentence> sentences, Vector<String> namedEntities,
								Hashtable<Integer, String> candidateNames,
								String topicId, String newsId) {
		int currSentenceNum = 0;
		for(Sentence sentence: sentences) {
			if(!(candidateNames.get(currSentenceNum)!=null) && sentence.containsQuotedSpeech()) {
				int idxFirstQuote = 0;
				int idxLastQuote = 0;
				
				// get quote position
				if((idxFirstQuote=sentence.indexOf(new Token("\""),0))>-1) {
					idxLastQuote = sentence.indexOf(new Token("\""), idxFirstQuote+1);
				} else {
					idxFirstQuote = sentence.indexOf(new Token("”"),0);
					idxLastQuote = sentence.indexOf(new Token("”"),idxFirstQuote+1);
				}
				
				boolean containsIndicator = false;
				int idxIndicator = -1;
				
				// detect indicator words for reference after the last quote only with reference 'nya' only
				for(int i=0;i<speakerIndicator.length;i++) {
					if((idxIndicator=sentence.indexOf(new Token(speakerIndicator[i]+"nya"), 
										idxLastQuote+1))>-1) {
						containsIndicator = true;

						break;
					}
				}
				
				if(containsIndicator) {
					String name = "";
					
					// get name with closest proximity from candidates
					for(int i=currSentenceNum-1;i>-1;i--) {
						if(candidateNames.containsKey(i)) {
							name = candidateNames.get(i);
							break;
						}
					}
										
					if(name.length() > 0) {
						// get the longest name for this name mentioned in this news
						String longestName = getLongestNameFor(name, namedEntities);
						
						// no full name for this name
						if(longestName.equals("NONE"))
							longestName = name;
												
						// add quote found
						quotesFromNews.add(new Quote(longestName, longestName, 
								"REF\t" + sentence.subsentence(idxFirstQuote, idxLastQuote).toString(),
								topicId, newsId));
					}
				}
			}
			currSentenceNum++;
		}
	}
	
	/**
		Get longest named entity that contains this name
	*/
	private static String getLongestNameFor(String name, Vector<String> namedEntities) {
		String longestName = "NONE";
		
		int max = 0;
		int maxIdx = -1;
		int threshold = 5;
		
		for(int i=0;i<namedEntities.size();i++) {
			String namedEntity = namedEntities.get(i);
			
			// get the longest name representation
			if(namedEntity.contains(name) && namedEntity.length() > max) {
				maxIdx = i;
				max = namedEntity.length();
			}
		}
		
		if(maxIdx > -1)
			longestName = namedEntities.get(maxIdx);
		
		return longestName;
	}
	
	/**
		Extract named entities from a Sentence
	*/
	private static Vector<String> extractNamedEntities(Sentence sentence) {
		Vector<String> namedEntities = new Vector<String>();
		
		// quote pattern
		Pattern pQuote = Pattern.compile("([\"”])");
		Matcher m = null;
		
		// useful information
		String prevNonEmpty = "";
		String prevBeforeCap = "";	// used to identify word before named entity, e.g. 'di' or 'ke', means the named entity is a location
		
		// get tokens from sentence
		Vector<Token> tokens = sentence.getTokens();
		
		// get named entities by processing the tokens
		for(int i=0;i<tokens.size();i++) {
			String namedEntity = "";	// container for named entity found
			
			// ignore everything inside quote
			m = pQuote.matcher(tokens.elementAt(i).getToken());
			if(m.find()) {
				int startingI = i;
				
				String quoteType = m.group(1);
				
				i++;
				while(i < tokens.size() 
					&& !tokens.elementAt(i).getToken().equals(quoteType))
					i++;
				
				if(i==tokens.size())
					i = startingI;
				
				continue;
			}
			
			// ignore space
			if(tokens.elementAt(i).getToken().equals(" "))
				continue;
			
			// record the useful information
			if(i>1) {
				prevBeforeCap = tokens.elementAt(i-2).getToken();
				prevNonEmpty = prevBeforeCap;
			}
			
			// if the token starts with uppercase letter then  
			if(tokens.elementAt(i).firstChar().matches("[A-Z]")) {
				int numOfTokens = 0;
				
				// to minimize false identification of common words as named entity
				if(isContainedIn(tokens.elementAt(i).getToken().toLowerCase(), beforeNameIndicator))
					continue;
				
				// process this token and tokens afterward to identify whether we can find a named entity
				for(i=i;i<tokens.size();i++) {
					Token t = tokens.elementAt(i);
					
					// cleanup this 'KOMPAS.com' token to minimize false identification, need more general rule
					if(t.getToken().equals("KOMPAS.com")) {
						numOfTokens = 0;
						break;
					}
					
					// tolerate a named entity whose components separated by space and also ignore the name of the day
					if(t.getToken().equals(" ")
						|| t.getToken().equals("Senin") || t.getToken().equals("Selasa")
						|| t.getToken().equals("Rabu") || t.getToken().equals("Kamis")
						|| t.getToken().equals("Jumat") || t.getToken().equals("Sabtu")
						|| t.getToken().equals("Minggu"))
						continue;
					
					// get the real components of a named entity
					if(t.firstChar().matches("[A-Z]") 
						|| t.getToken().equals("dan")
						|| t.getToken().equals("selaku") 
						|| t.getToken().equals("untuk")
						|| t.getToken().equals("(")
						|| t.getToken().equals(")")) {
						
						// set up some rule to put space or not between each component
						if(!prevNonEmpty.matches("\\(") && !t.getToken().matches("\\)"))
							namedEntity += " ";
							
						namedEntity += t.getToken();
						prevNonEmpty = t.getToken();
							
						numOfTokens++;
					} else {
						i--;
						break;
					}
				}
				
				// Kompas related problem
				if(namedEntity.contains("Photo")) {
					namedEntity = namedEntity.replaceFirst("Photo", "");
					numOfTokens--;
				}
				
				// remove these words if the named entity ends with one of them
				if(prevNonEmpty.equals("dan") || prevNonEmpty.equals("selaku")
						|| prevNonEmpty.equals("untuk")) {
					namedEntity = namedEntity.replaceFirst(prevNonEmpty, "");
					numOfTokens--;
				}
				
				// remove these punctuations
				if(namedEntity.contains("(") && !namedEntity.contains(")")) {
					namedEntity = namedEntity.replaceFirst("\\(", "");
					numOfTokens--;
				}
				
				if(numOfTokens > 1) {					
					// ignore location entity captured by this simple rule
					if(prevBeforeCap.equals("di") || prevBeforeCap.equals("ke")) {
						//~ namedEntities.add("<LOCATION>" + namedEntity.trim() + "</LOCATION>");
					}
					// add the real named entity
					else {					
						namedEntities.add(namedEntity.trim());
					}
				}
			}
		}
		
		return namedEntities;
	}
	
	/**
		To check whether a word is contained in the array of string
	*/
	private static boolean isContainedIn(String word, String []words) {
		for(String w: words)
			if(w.equals(word))
				return true;
		
		return false;
	}
	
	/**
		Add quote found in current news to collection of quotes
	*/
	private static void addQuotesTo(Vector<Quote> quoteFromNews, Vector<Quote> quotes) {
		for(Quote q : quoteFromNews) {
			String similarName = getSimilarKnownName(q.getNameInNews(), quotes);
			
			if(similarName.equals("NONE")) {
				quotes.add(q);
			} else {
				q.setName(similarName);
				quotes.add(q);
			}
		}
	}
	
	/**
		Find similar name for a given name
	*/
	private static String getSimilarKnownName(String name, Vector<Quote> quotes) {
		String similarName = "NONE";
		int nameCount = (name.split(" ")).length;
		
		TreeSet<String> setOfNames = getSetOfNames(quotes);
		
		/** 
			For this case, the Levenshtein distance doesn't work so well for a single token name.
			So, a person with single token name must perfectly match their name mentioned in
			the other place. Usually, the person with single token found in news are not so
			important, so their full name is usually neglicted.
		*/
		if(nameCount < 2) {
			// compare with every name previously stored
			for(String s: setOfNames) {
				if(s.equals(name))
					return s;
			}
		}
		
		else {
			int threshold = 6;
			int minDistance = 1000;
			
			// compare with every name previously stored
			for(String s: setOfNames) {
				if(s.split(" ").length < 2)
					continue;
				
				/** 
					If the name contained (part of) in the stored name, consider  them as the name 
					from the same person. We assume this is a valid thing to do since when we 
					extracted the name from a news, we always take the longest name for a person.
					So, if the name found here contained in other name, with high probability they
					are the name for the same person.
				*/
				if(s.contains(name) || name.contains(s)) {
					similarName =  s;
					break;
				}
				
				// find the stored name with the smallest distance from the name given
				else {
					int distance = TextUtility.levenshteinDistance(s, name);
					
					if(distance < threshold && distance < minDistance)
						similarName = s;
				}
			}
			
			// replace the name stored in quotes with the longest name
			if(!similarName.equals("NONE") 
				&& similarName.length() < name.length()) {
			
				updateNamesIn(quotes, similarName, name);
				similarName = name;
			}
		}

		return similarName;
	}
	
	/**
		Get set of names from quotes
	*/
	private static TreeSet<String> getSetOfNames(Vector<Quote> quotes) {
		TreeSet<String> setOfNames = new TreeSet<String>();
		
		for(Quote q: quotes)
			setOfNames.add(q.getName());
		
		return setOfNames;
	}
	
	/**
		Get set of news from quotes
	*/
	private static TreeSet<String> getSetOfNews(Vector<Quote> quotes) {
		TreeSet<String> setOfNames = new TreeSet<String>();
		
		for(Quote q: quotes)
			setOfNames.add(q.getNewsId());
		
		return setOfNames;
	}
	
	/**
		Get set of topics from quotes
	*/
	private static TreeSet<String> getSetOfTopics(Vector<Quote> quotes) {
		TreeSet<String> setOfNames = new TreeSet<String>();
		
		for(Quote q: quotes)
			setOfNames.add(q.getTopicId());
		
		return setOfNames;
	}
	
	/**
		Update names in quotes
	*/
	private static void updateNamesIn(Vector<Quote> quotes, String similarName, String name) {
		for(Quote q: quotes) {
			if(q.getName().equals(similarName))
				q.setName(name);
		}
	}
	
	/**
		As it said, print all the quotes found sorted by name
	*/
	public static void printQuotes(Vector<Quote> quotes) {
		TreeSet<String> setOfNames = getSetOfNames(quotes);
		
		for(String name: setOfNames) {
			System.out.println("NAME: " + name + "\n");
			
			for(Quote q: quotes) {
				if(q.getName().equals(name)) {
					System.out.println("NEWS: " + q.getNewsId());
					System.out.println("TOPIC: " + q.getTopicId());
					System.out.println("QUOTE: " + q.getQuote());
					System.out.println("");
				}
			}
			System.out.println("\n==============================\n");
		}
	}
	
	/**
		As it said, print all the quotes found sorted by news
	*/
	public static void printQuotesByNews(Vector<Quote> quotes) {
		TreeSet<String> setOfNames = getSetOfNews(quotes);
		
		for(String name: setOfNames) {
			System.out.println("NEWS ID: " + name + "\n");
			
			for(Quote q: quotes) {
				if(q.getNewsId().equals(name)) {
					System.out.println("NAME: " + q.getName());
					System.out.println("TOPIC: " + q.getTopicId());
					System.out.println("QUOTE: " + q.getQuote());
					System.out.println("");
				}
			}
			System.out.println("\n==============================\n");
		}
	}
	
	/**
		As it said, print all the quotes found sorted by topic
	*/
	public static void printQuotesByTopic(Vector<Quote> quotes) {
		TreeSet<String> setOfNames = getSetOfTopics(quotes);
		
		for(String name: setOfNames) {
			System.out.println("TOPIC ID: " + name + "\n");
			
			for(Quote q: quotes) {
				if(q.getTopicId().equals(name)) {
					System.out.println("NAME: " + q.getName());
					System.out.println("NEWS: " + q.getNewsId());
					System.out.println("QUOTE: " + q.getQuote());
					System.out.println("");
				}
			}
			System.out.println("\n==============================\n");
		}
	}
	
	// words that indicate the speaker/person who produce the quote
	private static String []speakerIndicator = {"kata", "tandas", "lanjut", "ujar", "papar", "pinta",
												"tutur", "imbuh", "ucap", "ungkap", "pungkas", "jelas",
												"tegas", "tambah"};
												
	// words that indicate there is a name (animate named entity) after this word	
	private static String []beforeNameIndicator = {"menurut", "dikatakan", "dipaparkan", "kata", 
													"lanjut"};
	
	// words that indicate there is a name (animate named entity) before this word
	private static String []afterNameIndicator = {"mengatakan", "menyatakan", "memaparkan",
												"mengemukakan", "menjawab", "melanjutkan", 
												"menegaskan", "berpendapat"};
}