import java.util.regex.Pattern;

/**
	Representing a token
*/
public class Token {
	
	public Token(String token) {
		this.token = token;
	}
	
	public String firstChar() {
		if (token.length()==0)
			return "";
		
		return token.substring(0,1);
	}
	
	public String lastChar() {
		if (token.length()==0)
			return "";
		
		return token.substring(token.length()-1, token.length());
	}
	
	public boolean isPunctuation() {
		return Pattern.matches("[^a-zA-Z0-9]", token);
	}
	
	public boolean isWhitespace() {
		return Pattern.matches("\\s", token);
	}
	
	public boolean isTitle() {
		return (Pattern.matches("drg[\\.]|Drg[\\.]|dr[\\.]|Dr[\\.]|Tn[\\.]|Ny[\\.]|Yth[\\.]|" + 
				"Bpk[\\.]|Prof[\\.]|Sdr[\\.]|Sdri[\\.]|K.H[\\.]", token) 
		|| Pattern.matches("s[\\.](pd|h|e|s|kp|kom|i[\\.]p|sos|psi|ked|" + 
				"k[\\.]m|k[\\.]g|p|t[\\.]p|pt|pi|hut|k[\\.]h|si|" + 
				"t|sn|ag)[\\.]|m[\\.](a|m|hum|kom|si|kes|p|sn|" + 
				"pd|ag)[\\.]|ph[\\.]d[\\.]", token.toLowerCase()));
	}
	
	public static boolean isTitle(String s) {
		return (new Token(s)).isTitle();
	}
	
	public String getToken() {
		return token;
	}
	
	public int length() {
		return token.length();
	}
	
	private String token;
}