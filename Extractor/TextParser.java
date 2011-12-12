import java.util.Vector;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
	Class that provide method to parse text as token and sentence
*/
public class TextParser {
	
	public static Vector<Sentence> parseAsSentences(String text) {
		Vector<Sentence> sentences = new Vector<Sentence>();
		Vector<Token> tokens = TextParser.parseAsTokens(text);
		
		Token previousToken = null;
		Token currentToken = null;
		
		Sentence sentence = new Sentence();
				
		// construct sentences
		for(int i=0;i<tokens.size();i++) {
			currentToken = tokens.get(i);
			
			// if it is in quote treat each sentences inside quotes as part of the main sentence
			if(currentToken.getToken().matches("[\"'”]")) {
				sentence.add(currentToken);
				previousToken = currentToken;
				
				// get what inside this quote
				for(i=i+1;i<tokens.size();i++) {
					currentToken = tokens.get(i);
					sentence.add(currentToken);
					
					// detect variation of how the quote ends
					if(currentToken.getToken().matches("[\"'”]")) {
						if(previousToken.getToken().equals("[\\.]")) {
							sentences.add(sentence);
							sentence = new Sentence();
							
							//~ System.out.println(sentence.toString());
						}						
						previousToken = currentToken;						
						break;
					}
					previousToken = currentToken;
				}
				continue;
			}
			
			// consider these punctuations as sentence boundary
			if(currentToken.getToken().matches("[\\.]|[—]|[-]")) {
				sentence.add(currentToken);
				sentences.add(sentence);
				previousToken = currentToken;
				
				//~ System.out.println(sentence.toString());		
				
				sentence = new Sentence();
			} 
			// the last sentence
			else if(i==tokens.size()-1) {
				sentence.add(currentToken);
				sentences.add(sentence);
				
				//~ System.out.println(sentence.toString());
			}
			// just ordinary word/token
			else {
				sentence.add(currentToken);
				previousToken = currentToken;
			} 
		}
		
		return sentences;
	}
	
	public static Vector<Token> parseAsTokens(String text) {
		Vector<Token> tokens = new Vector<Token>();
		
		// you cannot see it, but the space after "|" is different, not ordinary space, do not delete it
		String tokensBySpace[] = text.split("\\s+| ");
		
		// get tokens
		for(String tbs: tokensBySpace) {
			Token t = new Token(tbs.trim());
			
			// add directly if this is title
			if(t.isTitle()) {
				tokens.add(new Token(t.getToken()));
				
				//~ System.out.println("TITLE:\t " + t.getToken());
			}
			// start the lengthy tokenization process
			else {
				//~ System.out.print(t.getToken() + " : {");
				int numOfPuncInFront = 0;
				int numOfPuncInBack = 0;
				
				char c[] = t.getToken().toCharArray();
				
				// parse the punctuations in front of this word (if any)
				for(int i=0;i<c.length;i++) {
					if((c[i] + "").matches("[^a-zA-Z0-9]")) {
						numOfPuncInFront++;
						tokens.add(new Token(c[i] + ""));
						
						//~ System.out.print(c[i] + ", ");
					} else {
						break;
					}
				}
				
				// parse the punctuations at the end of this word (if any)
				for(int i=c.length-1;i>=numOfPuncInFront;i--) {
					if((c[i] + "").matches("[^a-zA-Z0-9]")) {
						numOfPuncInBack++;
					} else {
						break;
					}
				}
				
				// get the real word after punctuation removal
				tokens.add(new Token(t.getToken().substring(numOfPuncInFront, 
													t.getToken().length()-numOfPuncInBack)));
				
				// add the punctuations found before (for the end punctuations only)
				for(int i=c.length-1;i>=c.length-numOfPuncInBack;i--) {
					tokens.add(new Token(c[i] + ""));
					//~ System.out.print(c[i] + ", ");
				}
				
				//~ System.out.print(t.getToken().substring(numOfPuncInFront, 
													//~ t.getToken().length()-numOfPuncInBack));
				//~ System.out.println("}");
				
				
			}
			
			// add space as word boundary
			tokens.add(new Token(" "));
		}
				
		return tokens;
	}
}