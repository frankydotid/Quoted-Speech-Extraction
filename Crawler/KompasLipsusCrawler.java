import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.URL;
import org.jsoup.Jsoup;

public class KompasLipsusCrawler extends Crawler {
	private int numOfPages = 0;
	private int numOfNews = 0;
	private int lipsusId = 0;
	private String topicName;
	private String addressPrefix;
	private Vector<String> addressesOfNews;
	
	public KompasLipsusCrawler() {
		this.addressesOfNews = new Vector<String>();
		this.addressPrefix = "http://lipsus.kompas.com/topikpilihanlist/";
	}
		
	private boolean initializeCrawler(String page, int lipsusId) {
		if(page.contains("A PHP Error was encountered"))
			return false;
		
		this.lipsusId = lipsusId;
		setTopicName(page);
		setNumOfPages(page);
		
		String currentPage = "";
		for(int i=0;i<numOfPages;i++) {
			currentPage = getHtmlPage(addressPrefix+lipsusId+"/"+(i+1));
			setAddressesOfNews(currentPage);
		}
		System.out.println("Number of news: " + addressesOfNews.size());
		
		return true;
	}
	
	public void getNewsFrom(int lipsusId, String dirOutput) throws IOException {
		boolean successInit = initializeCrawler(getHtmlPage(addressPrefix+lipsusId), lipsusId);
		
		if(!successInit)
			return;
		
		if(!(new File(dirOutput+"/"+lipsusId+"-"+topicName)).exists() &&
				!(new File(dirOutput+"/"+lipsusId+"-"+topicName)).mkdir())
			return;
		
		PrintWriter pw;
		
		String page = "";
		String parsedPage = "";
		String title = "";
		
		for(int i=0;i<addressesOfNews.size();i++) {
			page = getHtmlPage(addressesOfNews.elementAt(i));
			parsedPage = parsePage(page);
					
			Pattern p = Pattern.compile("<TITLE>(.*)</TITLE>.*");
			Matcher m = p.matcher(parsedPage);
		
			if(m.find())
				title = m.group(1);
			
			pw = new PrintWriter(new File(dirOutput+"/"+lipsusId+"-"+topicName+"/"+lipsusId+"-"+i+"-"+title.replaceAll("[^a-zA-Z0-9. ]","")+".txt"));
			
			System.out.println(lipsusId+"-"+i+":"+addressesOfNews.elementAt(i));
			
			pw.println("<DOC-ID>" + lipsusId + "-" + i + "</DOC-ID>");
			pw.println(parsedPage);
			pw.flush();
		}
		
	}
	
	private String parsePage(String page) {
		String title = getTitleFrom(page);
		String timestamp = getTimestampFrom(page);
		String content = getContentFrom(page);
		
		String parsedPage = "" +
					"<TITLE>" + title + "</TITLE>\n" +
					"<TIMESTAMP>" + timestamp + "</TIMESTAMP>\n" +
					"<CONTENT>" + content + "</CONTENT>";
		
		return parsedPage;
	}
	
	private String getTitleFrom(String page) {
		String title = "";
		
		Pattern p = Pattern.compile(".*<h1>(.*)</h1>.*");
		Matcher m = p.matcher(page);
		
		if(m.find()) {
			title = m.group(1);
		}
		
		return title;
	}
	
	private String getTimestampFrom(String page) {
		String timestamp = "";
		
		Pattern p = Pattern.compile(".*<h3>.*</h3><h3>(.*)</h3></div><div class=\"artikel\">.*");
		Matcher m = p.matcher(page);
		
		if(m.find()) {
			timestamp = m.group(1);
		}
		
		return timestamp;
	}
	
	private String getContentFrom(String page) {
		String content = "";
		
		Pattern p = Pattern.compile(".*<div class=\"artikel\">(.*)<div class=\"banner\">.*");
		Matcher m = p.matcher(page);
		
		if(m.find()) {
			content = m.group(1);
		}
		content = Jsoup.parse(content).text();
		
		return content;
	}
	
	private void setTopicName(String page) {
		Pattern p = Pattern.compile(".*<strong>Topik Hari Ini : (.*)</strong></div>.*");
		Matcher m = p.matcher(page);
		
		if(m.matches()) {
			topicName = m.group(1).toLowerCase();
			System.out.println("Topic name: " + topicName);
		}
		
		if(topicName.trim().equals("")) {
			topicName = lipsusId + "";
		}
		
		topicName = topicName.replaceAll("[^a-zA-Z0-9. ]","");
	}
	
	private void setNumOfPages(String page) {
		Pattern p = Pattern.compile(".*<img alt=\"\"src=\"http://stat.k.kidsklik.com/data/2k10/images/panah_1.gif\" border=\"0\" /></a>(.*)<img alt=\"\"src=\"http://stat.k.kidsklik.com/data/2k10/images/panah_2.gif\" border=\"0\" /></a>.*");
		Matcher m = p.matcher(page);
		
		String matchedString = "";
		if(m.matches()) {
			numOfPages = m.group(1).split("</a>").length-1;
			System.out.println("Number of pages: " + numOfPages);
		}
	}
	
	private void setAddressesOfNews(String page) {		
		Pattern p = Pattern.compile("<div class=\"font16 fbold c_biru_kompas2011 pb_5\">(.*)<div class=\"font13 lh16 c_abu01_kompas2011 pb_10 imgsubsub\">");
		Matcher m = p.matcher(page);
		
		String matchedString = "";
		if(m.find()) {
			matchedString = m.group(1);
		}
		
		String matchedStrings[] = matchedString.split("</a></div>");
		p = Pattern.compile(".*<a href=\"(.*)\">.*");
		
		for(int i=0;i<matchedStrings.length;i++) {
			m = p.matcher(matchedStrings[i]);
			if(m.find()) {
				addressesOfNews.add(m.group(1));
				// System.out.println(m.group(1));
			}
		}
	}
}