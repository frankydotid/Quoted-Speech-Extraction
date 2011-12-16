import java.io.*;
import java.util.*;

public class Tester {
	
	public static void main(String []args) throws Exception {		
		String mainPath = "../Data/SelectedNews";
		
		Vector<Quote> quotes = QuoteExtractor.extractQuotes(mainPath);
		//~ QuoteExtractor.printQuotes(quotes);
		
		//~ QuoteExtractor.printQuotesByNews(quotes);
		
		QuoteExtractor.printQuotesByTopic(quotes);
	}
}