package fiu.kdrg.crawler;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mysql.jdbc.PreparedStatement;

public class BingNewsCrawler {

	public static String QUERY_FORMAT = "http://www.bing.com/news/search?q=%s&ctp=&first=%d&FORM=NWRFSH";
	public static String SR_DIV_SEL = ".sn_r";
	public static String SEL_SPACE = " ";
	public static String TITLE_LINK_SEL = ".newstitle a";
	public static String PUBLISHER_CITE_SEL = ".sn_ST .sn_src";
	public static String PUBLISHDATE_SPAN_SEL = ".sn_ST .sn_tm";
	public static String EDITORS_SPAN_SEL = ".sn_ST .sn_by span";
	
	private String query;
	private int numToCraw;
	private int numPerPage;
	
	public BingNewsCrawler(String query,int numToCraw) {
		// TODO Auto-generated constructor stub
		this.query = query;
		this.numToCraw = numToCraw;
		this.numPerPage = 10;
	}
	
	
	
	public void startCrawling(){
		
		List<BingSearchNews> news = crawlNewsUrl();
		for(int i = 0; i < news.size(); i++){
			try {
				news.get(i).setHtml(downloadWebPage(news.get(i).getUrl()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}
	
	
	
	public void storeData(Connection conn){
		
		PreparedStatement pstm = null;
		
		
		
	}
	
	
	public List<BingSearchNews> crawlNewsUrl(){
		
		List<BingSearchNews> news = new ArrayList<BingSearchNews>();
		
		for(int i = 1; i <= numToCraw; i += numPerPage){
			
			String queryUrl = composeBingQueryPageUrl(query, i);
			Document searchPage = null;
			try {
				searchPage = Jsoup.connect(queryUrl).get();
				Elements htmlNews = searchPage.select(SR_DIV_SEL);
				for(Element aNews : htmlNews){
					BingSearchNews bsn = extractBingSearchNews(aNews);
					if(bsn != null)
						news.add(bsn);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return news;
	}
	
	
	
	public String downloadWebPage(String url) throws IOException{
		
		return Jsoup.connect(url).get().html();
		
	}
	
	
	public String composeBingQueryPageUrl(String query, int start){
		
		return String.format(QUERY_FORMAT, query.replaceAll("\\s+", "+"), start);
		
	}
	
	
	public BingSearchNews extractBingSearchNews(Element aNews){
		
		BingSearchNews bs = new BingSearchNews();
		Elements eles = aNews.select(TITLE_LINK_SEL);
		if(eles.size() == 0) return null;
		
		bs.setUrl(eles.get(0).attr("href"));
		bs.setTitle(eles.get(0).html().replaceAll("<[^>]*>", ""));
		
		eles = aNews.select(PUBLISHER_CITE_SEL);
		if(eles.size() > 0)
			bs.setPublisher(eles.get(0).text().trim());
		
		eles = aNews.select(PUBLISHDATE_SPAN_SEL);
		if(eles.size() > 0)
			bs.setDateTime(eles.get(0).text().trim());
		
		eles = aNews.select(EDITORS_SPAN_SEL);
		String authors = "";
		if(eles.size() > 0){
			authors += eles.get(0).text();
			for(int i = 1; i < eles.size(); i++){
				authors += ";"+eles.get(i).text();
			}
			bs.setAuthors(authors);
		}
			
		return bs;
	}
	
	
	public static void main(String[] args) {
	
		BingNewsCrawler newsCrawer = new BingNewsCrawler("Hurricane Irene", 500);
		String baseUrl = newsCrawer.composeBingQueryPageUrl("Hurricane Irene", 1);
		System.out.println(baseUrl);
		System.out.println(newsCrawer.crawlNewsUrl().size());;
		
	}
	
	
}
