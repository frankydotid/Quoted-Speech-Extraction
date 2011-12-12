import java.io.*;
import java.util.*;

/**
	Representing a topic of news
*/
public class Topic {
	private String topic;
	private String newsFiles[];
	
	public Topic() {
		this.topic = "";
		
		quotes = new Hashtable<String, String>();
	}
	
	public void setTopic(String topicPath) throws IOException {
		String pathComponents[] = topicPath.split("[\\|/]");
		this.topic = pathComponents[pathComponents.length-1];
		
		newsFiles = (new File(topicPath)).list();
		
		// process news inside this topic
		for(String nf: newsFiles) {
			News currentNews = new News();
			currentNews.setNews(topicPath+"/"+nf);
			
			Hashtable<String, String> newsQuotes = currentNews.getQuotes();
			
			// get quotes for each name found in news
			for(String name: newsQuotes.keySet()) {
				String storedName = getStoredName(name); // name known by this topic
				
				if(storedName.equals("NONE")) {
					quotes.put(name, "<TOPIC>" + topicPath + "</TOPIC>\t" + 
								"<TOPNAME>" + name + "</TOPNAME>\t" 
								+  newsQuotes.get(name));
				} else {
					quotes.put(storedName, quotes.get(storedName) + "\t" + newsQuotes.get(name));
				}
			}
			
		}
	}
	
	public void printQuotes() {
		Set<String> names = quotes.keySet();
		SortedSet<String> sortedNames = new TreeSet<String>();
		
		for(String s: names) {
			sortedNames.add(s);
		}
		
		for(String s: sortedNames) {
			String quote[] = quotes.get(s).split("\t");
			
			System.out.println();
			for(String q: quote) {
				System.out.println(q);
			}
			System.out.println();
		}
	}
	
	private String getStoredName(String name) {
		String storedName = "NONE";
		
		for(String s: quotes.keySet()) {
			// get the first match with edit distance cost less that threshold
			if(Utility.levenshteinDistance(s, name) < 5) {
				storedName = s;
				break;
			}
		}
		
		return storedName;
	}
	
	public Hashtable<String, String> getQuotes() {
		return quotes;
	}
	
	private Hashtable<String, String> quotes;
	
}