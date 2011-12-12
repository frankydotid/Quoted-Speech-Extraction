import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;

/**
	Representation of a news extracted
*/
public class News {
	
	public News() {
		this.docId = "";
		this.title = "";
		this.timestamp = "";
		this.content = "";
		
		namedEntities = new Vector<String>();
		quotes = new Hashtable<String, String>();
	}
	
	/**
		Set this news object using the news specified by newsFile
	*/
	public void setNews(String newsFile) throws IOException {
		Pattern pDocId = Pattern.compile("<DOC-ID>(.*)</DOC-ID>");
		Pattern pTitle = Pattern.compile("<TITLE>(.*)</TITLE>");
		Pattern pTimestamp = Pattern.compile("<TIMESTAMP>(.*)</TIMESTAMP>");
		Pattern pContent = Pattern.compile("<CONTENT>(.*)</CONTENT>");
		Matcher m = null;
		
		BufferedReader br = new BufferedReader(new FileReader(newsFile));
		
		String line = "";
		String news = "";
		while((line=br.readLine())!=null) {
			news += line;
		}	
		
		// get news metadata and content
		m = pDocId.matcher(news);
		if(m.find()) {
			this.docId = m.group(1);
		}
		
		m = pTitle.matcher(news);
		if(m.find()) {
			this.title = m.group(1);
		}
		
		m = pTimestamp.matcher(news);
		if(m.find()) {
			this.timestamp = m.group(1);
		}
		
		m = pContent.matcher(news);
		if(m.find()) {
			this.content = m.group(1);
		}
		
		// parse the sentences inside the news
		sentences = Utility.parseAsSentences(content);
		
		// extract named entities for this news
		for(Sentence sentence: sentences) {
			for(String s: sentence.getNamedEntity()) {
				namedEntities.add(s);
				//~ System.out.println(s);
			}
		}
		
		// extract quoted text
		for(Sentence sentence: sentences) {
			if(sentence.containsQuotedSpeech()) {
				// detect name located after the quote
				int idxFirstQuote = sentence.indexOf(new Token("\""),0);
				if(idxFirstQuote == -1) 
					idxFirstQuote = sentence.indexOf(new Token("”"),0);
				
				int idxLastQuote = sentence.indexOf(new Token("\""), idxFirstQuote+1);
				if(idxLastQuote == -1) 
					idxLastQuote = sentence.indexOf(new Token("”"),idxFirstQuote+1);
			
				boolean containsIndicator = false;
				int idxIndicator = -1;
				
				// detect name indicator words
				for(int i=0;i<indicator.length;i++) {
					if((idxIndicator=sentence.indexOf(new Token(indicator[i]), idxLastQuote+1))>-1) {
						containsIndicator = true;
						break;
					}
				}
				
				// get quote and name of speaker
				if(containsIndicator) {
					String name = sentence.getFirstCapSeqOfTerms(idxIndicator+1, 
													sentence.numOfTokens()-1);
					
					// get the longest name for this name mentioned in this news
					String fullName = getClosestMatch(name);
					
					if(name.length() > 0) {
						if(fullName.equals("NONE"))
							fullName = name;	
						
						if(quotes.get(fullName)!=null) {
							String prevQuotes = quotes.get(fullName);
							quotes.put(fullName, prevQuotes + "\t" + 
									"<ORINAME>" + name + "</ORINAME>\t" +
									"<REPNAME>" + fullName + "</REPNAME>\t" +  
									"<QUOTE>" + sentence.subsentence(idxFirstQuote, 
										idxLastQuote).toString() + "</QUOTE>");
						} else {
							quotes.put(fullName, "<NEWS>" + newsFile + "</NEWS>\t" + 
									"<ORINAME>" + name + "</ORINAME>\t" +
									"<REPNAME>" + fullName + "</REPNAME>\t" +  
									"<QUOTE>" + sentence.subsentence(idxFirstQuote, 
										idxLastQuote).toString() + "</QUOTE>");
						}
						
						//~ System.out.println("NAME: {" + name + ", " + fullName + "}");
						//~ System.out.println(sentence.subsentence(idxFirstQuote, 
													//~ idxLastQuote).toString() + "\n");
					}
				}
			}
		}
	}
	
	public Vector<Sentence> getSentences() {
		return sentences;
	}
	
	public Vector<String> getNamedEntities() {
		return namedEntities;
	}
	
	public Hashtable<String, String> getQuotes() {
		return quotes;
	}
	
	private String getClosestMatch(String name) {
		String closestMatch = "NONE";
		int max = 0;
		int maxIdx = -1;
		int threshold = 5;
		
		for(int i=0;i<namedEntities.size();i++) {
			String namedEntity = namedEntities.get(i);
			
			if(namedEntity.contains(name) && namedEntity.length() > max) {
				maxIdx = i;
				max = namedEntity.length();
			}
		}
		
		if(maxIdx > -1)
			closestMatch = namedEntities.get(maxIdx);
		
		return closestMatch;
	}
	
	private Vector<String> namedEntities;
	private Vector<Sentence> sentences;
	private Hashtable<String, String> quotes;
	private String []indicator = {"kata", "tandas", "lanjut", "ujar", "papar", "pinta", "tutur", "imbuh", "ucap", "ungkap", "pungkas", "jelas", "tegas"};
	private String docId;
	private String title;
	private String timestamp;
	private String content;	
}