/**
	Representing quote found in news
*/
public class Quote {
	private String name;
	private String nameInNews;
	private String quote;
	private String topicId;
	private String newsId;
	
	public Quote() {
		this.name = "";
		this.quote = "";
		this.topicId = "";
		this.newsId = "";
	}
	
	public Quote(String name, String quote, String topicId, String newsId) {
		this.name = name;
		this.quote = quote;
		this.topicId = topicId;
		this.newsId = newsId;
	}
	
	public Quote(String name, String nameInNews, String quote, String topicId, String newsId) {
		this.name = name;
		this.nameInNews = nameInNews;
		this.quote = quote;
		this.topicId = topicId;
		this.newsId = newsId;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setNameInNews(String nameInNews) {
		this.nameInNews = nameInNews;
	}
	
	public String getNameInNews() {
		return nameInNews;
	}
	
	public void setQuote(String quote) {
		this.quote = quote;
	}
	
	public String getQuote() {
		return quote;
	}
	
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	
	public String getTopicId() {
		return topicId;
	}
	
	public void setNewsId(String newsId) {
		this.newsId = newsId;
	}
	
	public String getNewsId() {
		return newsId;
	}
}