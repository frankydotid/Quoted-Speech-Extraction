public class SearchQuote {
	
	public static void main(String []args) throws Exception {
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