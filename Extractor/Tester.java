import java.io.*;
import java.util.*;

public class Tester {
	
	public static void main(String []args) throws Exception {		
		String mainPath = "../Data/News/Kompas";
		
		//~ long startTime = System.currentTimeMillis();
		//~ Vector<Quote> quotes = QuoteExtractor.extractQuotes(mainPath);
		//~ QuoteExtractor.printQuotes(quote	s);
		//~ QuoteExtractor.printQuotesByNews(quotes);
		//~ QuoteExtractor.printQuotesByTopic(quotes);
		//~ QuoteExtractor.writeModel(quotes, "../Output/ALL-QUOTES.txt");
		
		//~ System.out.println("Build in " + (System.currentTimeMillis()-startTime)/1000 + " s");
		
		QuoteSearcher qs = new QuoteSearcher();
		qs.readModel("../Output/ALL-QUOTES.txt");
		
		String name = System.console().readLine("Name: ");
		while(!name.equals("exit")) {
			long startTime = System.currentTimeMillis();
			
			qs.searchQuotesFor(name);
			
			System.out.println("Search in " + (System.currentTimeMillis()-startTime)/1000 + " s");
			
			name = System.console().readLine("Name: ");
		}
	}
}