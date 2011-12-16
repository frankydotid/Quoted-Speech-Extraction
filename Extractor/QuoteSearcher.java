import java.io.*;
import java.util.*;

public class QuoteSearcher {
	private Vector<Quote> quotes;
	
	public QuoteSearcher() {
		quotes = new Vector<Quote>();
	}
	
	/**
		Read quotes from file
	*/
	public void readModel(String modelFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(modelFile));
		
		String line = "";
		while((line=br.readLine())!=null) {
			String columns[] = line.split("###");
			
			//~ System.out.println(line);
			
			Quote q = new Quote();
			q.setName(columns[0]);
			q.setNameInNews(columns[1]);
			q.setQuote(columns[2]);
			q.setNewsId(columns[3]);
			q.setTopicId(columns[4]);
			
			quotes.add(q);
		}
	}
	
	/**
		Search for a certain person quotes, return a nice print
	*/
	public void searchQuotesFor(String name) {
		Vector<String> similarName = getSimilarKnownName(name);
		
		TreeSet<String> setOfTopics = QuoteExtractor.getSetOfTopics(quotes);
		
		String toPrint = "";
		
		for(String n : similarName) {
			for(Quote q: quotes) {
				if(q.getName().equals(n)) {
					toPrint += n + " commented on " + getPrettyPrint(q.getTopicId()) + "\n";
					toPrint += q.getQuote()+"\n";
				}
			}
			toPrint += "\n";			
		}
		
		System.out.println(toPrint);
	}
	
	private String getPrettyPrint(String topicId) {
		String topic = "";
		
		String words[] = (topicId.substring(5, topicId.length())).split("\\s+");
		for(String w: words) {
			if(w.length() > 1)
				topic += w.substring(0,1).toUpperCase() + w.substring(1,w.length()) + " ";
			else
				topic += w.toUpperCase() + " ";
		}
		
		return topic;
	}
	
	/**
		Find similar name for a given name
	*/
	private Vector<String> getSimilarKnownName(String name) {
		Vector<String> similarName = new Vector<String>();
		
		TreeSet<String> setOfNames = QuoteExtractor.getSetOfNames(quotes);
		
		int threshold = 6;
		
		// compare with every name previously stored
		for(String s: setOfNames) {
			if(s.split(" ").length < 2 ) {
				if(s.equals(name))
					similarName.add(s);
			} 
			
			else if(s.contains(name) || name.contains(s)) {
				similarName.add(s);
			} 	
			
			// find the stored name with the smallest distance from the name given
			else {
				int distance = TextUtility.levenshteinDistance(s, name);
				
				if(distance < threshold)
					similarName.add(s);
			}
		}

		return similarName;
	}
}