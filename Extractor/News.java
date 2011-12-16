import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;

/**
	Representation of a news extracted
*/
public class News {
	private String docId;
	private String title;
	private String timestamp;
	private String content;
	
	public News() {
		this.docId = "";
		this.title = "";
		this.timestamp = "";
		this.content = "";
	}
	
	public News(String newsFile) throws IOException {
		this.docId = "";
		this.title = "";
		this.timestamp = "";
		this.content = "";
		
		setNews(newsFile);
	}
	
	/**
		Set this news object using the news specified by newsFile
	*/
	public void setNews(String newsFile) throws IOException {
		Pattern pDocId = Pattern.compile("<DOC-ID>(.*)</DOC-ID>");
		Pattern pTitle = Pattern.compile("<TITLE>(.*)</TITLE>");
		Pattern pTimestamp = Pattern.compile("<TIMESTAMP>(.*)</TIMESTAMP>");
		Pattern pContent = Pattern.compile("<CONTENT>(.*)</CONTENT>");
		Matcher m = null;
		
		BufferedReader br = new BufferedReader(new FileReader(newsFile));
		
		String line = "";
		String news = "";
		while((line=br.readLine())!=null) {
			news += line;
		}	
		
		// get news metadata and content
		m = pDocId.matcher(news);
		if(m.find()) {
			this.docId = m.group(1);
		}
		
		m = pTitle.matcher(news);
		if(m.find()) {
			this.title = m.group(1);
		}
		
		m = pTimestamp.matcher(news);
		if(m.find()) {
			this.timestamp = m.group(1);
		}
		
		m = pContent.matcher(news);
		if(m.find()) {
			this.content = m.group(1);
		}
	}
	
	public String getDocId() {
		return docId;
	}
	
	public String getContent() {
		return content;
	}
}