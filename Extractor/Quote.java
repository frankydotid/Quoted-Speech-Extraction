import java.util.*;
import java.io.*;

public class Quote {
	
	public Quote() {
		quotes = new Hashtable<String, String>();
	}
	
	public void buildQuoteIndexByName(String mainPath) throws IOException {
		String topicDirs[] = (new File(mainPath)).list();
		
		
		for(String topicDir : topicDirs) {
			System.out.println(topicDir);
			
			Topic topic = new Topic();
			topic.setTopic(mainPath + "/" + topicDir);
			
			Hashtable<String, String> topicQuotes = topic.getQuotes();
			
			// get quotes for each name found in topic
			for(String name: topicQuotes.keySet()) {
				String storedName = getStoredName(name); // name known
				
				if(storedName.equals("NONE")) {
					quotes.put(name, "<QNAME>" + name + "</QNAME>\t" + topicQuotes.get(name));
				} else {
					quotes.put(storedName, quotes.get(storedName) + "\t" + topicQuotes.get(name));
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
	
	private Hashtable<String, String> quotes;
}