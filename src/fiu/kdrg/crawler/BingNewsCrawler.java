package fiu.kdrg.crawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class BingNewsCrawler {

	public static String QUERY_FORMAT = "http://www.bing.com/news/search?q=%s&ctp=&first=%d&FORM=NWRFSH";
	public static String SR_DIV_CLASS = ".sn_r";
	public static String TITLE_DIV_CLASS = ".newstitle";
	public static String SEL_SPACE = " ";
	
	private String query;
	private int numToCraw;
	private int numPerPage;
	
	public BingNewsCrawler(String query,int numToCraw) {
		// TODO Auto-generated constructor stub
		this.query = query;
		this.numToCraw = numToCraw;
		this.numPerPage = 10;
	}
	
	
	public Set<String> crawlNewsUrl(){
		
		Set<String> newsUrls = new HashSet<String>();
		
		for(int i = 1; i <= numToCraw; i += numPerPage){
			
			String queryUrl = composeBingQueryPageUrl(query, i);
			Document searchPage = null;
			try {
				searchPage = Jsoup.connect(queryUrl).get();
				Elements news = searchPage.select(SR_DIV_CLASS);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return newsUrls;
	}
	
	
	
	public String composeBingQueryPageUrl(String query, int start){
		
		return String.format(QUERY_FORMAT, query.replaceAll("\\s+", "+"), start);
		
	}
	
	
	
	public static void main(String[] args) {
	
		BingNewsCrawler newsCrawer = new BingNewsCrawler("Hurricane Irene", 500);
		String baseUrl = newsCrawer.composeBingQueryPageUrl("Hurricane Irene", 1);
		System.out.println(baseUrl);
		
	}
	
	
}
