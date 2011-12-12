import java.io.*;
import java.util.*;
import java.util.regex.*;

public class CrawlKompas {
	
	public static void main(String []args) {
		KompasLipsusCrawler crawler = new KompasLipsusCrawler();
		int startId = Integer.parseInt(args[0]);
		int endId = Integer.parseInt(args[1]);
		
		// crawl Kompas lipsus page at given range
		for(int i=startId;i<=endId;i++) {
			try {
				crawler = new KompasLipsusCrawler();
				crawler.getNewsFrom(i, "../Data/News/Kompas");
				Thread.currentThread().sleep(1000);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}