import java.util.regex.*;
import java.util.Vector;

/**
	Representing a sentence inside the news
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
	
	public Vector<Token> getTokens() {
		return tokens;
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
		
		boolean quotedTextFound = false;
		if((idxOfFirstQuote = indexOf(new Token("\""), 0)) > -1 && (idxOfSecondQuote = indexOf(new Token("\""), idxOfFirstQuote+1)) > -1) {
			quotedTextFound = true;
		} else if((idxOfFirstQuote = indexOf(new Token("”"), 0)) > -1 && (idxOfSecondQuote = indexOf(new Token("”"), idxOfFirstQuote+1)) > -1) {
			quotedTextFound = true;
		}
		
		return (quotedTextFound && (idxOfSecondQuote - idxOfFirstQuote) > 2);
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
	
	/**
		Return a sequence of words with their first letter of each word capitalized
	*/
	public String getFirstCapSeqOfTermsRev(int startTokenPos, int endTokenPos) {
		String capitalizedTokens = "";
		
		for(int i=startTokenPos;i>=endTokenPos && i>-1;i--) {
			if(tokens.elementAt(i).firstChar().matches("[A-Z]")) {
				capitalizedTokens += tokens.elementAt(i).getToken();
				
				for(i=i-1;i>=endTokenPos && i>-1;i--) {
					Token t = tokens.elementAt(i);
					if(t.getToken().equals(" "))
						continue;
					
					if(tokens.elementAt(i).firstChar().matches("[A-Z]")) {
						capitalizedTokens = tokens.elementAt(i).getToken() + " " + capitalizedTokens;
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
	
	public void trim() {
		for(int i=0;i<tokens.size();i++) {
			if(!tokens.elementAt(i).getToken().equals(" ") &&
				!tokens.elementAt(i).getToken().equals("")) {
				break;
			} else {
				tokens.removeElementAt(i);
				i--;
			}
		}
		
		for(int i=tokens.size()-1;i>-1;i--) {
			if(!tokens.elementAt(i).getToken().equals(" ") &&
				!tokens.elementAt(i).getToken().equals("")) {
				break;
			} else {
				tokens.removeElementAt(i);
			}
		}
	}
	
	public String toString() {
		String sentence = "";
		
		for(Token t: tokens) {
			sentence += t.getToken();
		}
		
		return sentence;
	}
}