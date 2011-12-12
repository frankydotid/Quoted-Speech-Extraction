import java.util.regex.Pattern;
import java.util.Vector;


/**
	Representing sentence inside the news
*/
public class Sentence {
	Vector<Token> tokens = new Vector<Token>();
	
	public Sentence() {
		
	}
	
	public void add(Token t) {
		tokens.add(t);
	}
	
	public Token getTokenAt(int i) {
		return tokens.elementAt(i);
	}
	
	public int numOfTokens() {
		return tokens.size();
	}
	
	public int indexOf(Token token, int startPos) {
		for(int i=startPos;i<tokens.size();i++) {
			if(tokens.elementAt(i).getToken().equals(token.getToken()))
				return i;
		}
		return -1;
	}
	
	public boolean containsQuotedSpeech() {
		int idxOfFirstQuote = -1;
		int idxOfSecondQuote = -1;
		
		if((idxOfFirstQuote = indexOf(new Token("\""), 0)) > -1 
			|| (idxOfFirstQuote = indexOf(new Token("”"), 0)) > -1)
			idxOfSecondQuote = indexOf(new Token("\""), idxOfFirstQuote+1);
		
		return ((idxOfSecondQuote > -1 
			|| (idxOfSecondQuote = indexOf(new Token("”"), idxOfFirstQuote+1)) > -1)
			&& idxOfSecondQuote - idxOfFirstQuote > 2);
	}
	
	public Vector<String> getNamedEntity() {
		Vector<String> namedEntities = new Vector<String>();
		
		String prevNonEmpty = "";
		for(int i=0;i<tokens.size();i++) {
			String namedEntity = "";
			
			// skip space when extracting
			if(tokens.elementAt(i).getToken().equals(" "))
				continue;
			
			prevNonEmpty = tokens.elementAt(i).getToken();
			
			// detect whether the token starts with uppercase letter or not
			if(tokens.elementAt(i).firstChar().matches("[A-Z]")) {
				// ignore unrelated words to the named entity
				if(!tokens.elementAt(i).getToken().equals("Menurut"))
					namedEntity = tokens.elementAt(i).getToken();
				
				int numOfTokens = 0;
				for(i=i+1;i<tokens.size();i++) {
					Token t = tokens.elementAt(i);
					
					// cleanup this 'KOMPAS.com' token to minimize false identification, need more general rule
					if(t.getToken().equals("KOMPAS.com")) {
						numOfTokens = 0;
						break;
					}
					
					// tolerate a named entity whose components separated by space, comma, and also ignore the name of the day
					if(t.getToken().equals(" ") || t.getToken().equals(",")
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
						break;
					}
				}
				
				// get the named entity but not named entity with length 1 that occurs after "." (dot), to minimize false identification
				// of first word in a sentence as a named entity
				if(numOfTokens > 0 && !prevNonEmpty.equals(".")) {
					// remove these words if the named entity ends with one of them
					if(prevNonEmpty.equals("dan") || prevNonEmpty.equals("selaku")
						|| prevNonEmpty.equals("untuk"))
						namedEntity = namedEntity.replaceFirst(prevNonEmpty, "");
					
					// remove these punctuations
					if(namedEntity.contains("(") && !namedEntity.contains(")"))
						namedEntity = namedEntity.replaceFirst("\\(", "");
					
					// remove this Kompas news specific word
					namedEntity = namedEntity.replaceFirst("Photo", "");
					
					namedEntities.add(namedEntity.trim());
				}
			}
		}
		
		return namedEntities;
	}
	
	/**
		Return a sequence of words with their first letter of each word capitalized
	*/
	public String getFirstCapSeqOfTerms(int startTokenPos, int endTokenPos) {
		String capitalizedTokens = "";
		
		for(int i=startTokenPos;i<endTokenPos && i<tokens.size();i++) {
			if(tokens.elementAt(i).firstChar().matches("[A-Z]")) {
				capitalizedTokens += tokens.elementAt(i).getToken();
				
				for(i=i+1;i<=endTokenPos && i<tokens.size();i++) {
					Token t = tokens.elementAt(i);
					if(t.getToken().equals(" "))
						continue;
					
					if(tokens.elementAt(i).firstChar().matches("[A-Z]")) {
						capitalizedTokens += " " + tokens.elementAt(i).getToken();
					} else {
						return capitalizedTokens;
					}
				}
			}
		}
		return capitalizedTokens;
	} 
	
	public Sentence subsentence(int startTokenPos, int endTokenPos) {
		Sentence subsentence = new Sentence();
		for(int i=startTokenPos;i<tokens.size() && i<=endTokenPos;i++) {
			subsentence.add(tokens.elementAt(i));
		}
		return subsentence;
	}
	
	public String toString() {
		String sentence = "";
		
		for(Token t: tokens) {
			sentence += t.getToken();
		}
		
		return sentence;
	}
}