import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;

public class Crawler {
	
	public Crawler() {}
	
	/**
		Get html raw page from given address
	*/
	public String getHtmlPage(String address) {
		String page = "";
		try {
			URL urlAdd = new URL(address);
			URLConnection url = urlAdd.openConnection();
			url.setConnectTimeout(20000);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
			
			String line = "";
			while((line=br.readLine())!=null) {
				page += line;
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return page;
	}
}