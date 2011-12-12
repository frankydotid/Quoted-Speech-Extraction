import java.io.*;

public class Tester {
	
	public static void main(String []args) throws Exception {
		//~ News news = new News();
		//~ news.setNews("3.txt");
		
		String mainPath = "../Data/SelectedNews";
		Quote quote = new Quote();
		quote.buildQuoteIndexByName(mainPath);
		quote.printQuotes();
	}
}