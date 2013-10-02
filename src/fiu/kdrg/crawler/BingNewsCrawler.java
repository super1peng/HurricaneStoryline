package fiu.kdrg.crawler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fiu.kdrg.db.DBConnection;

public class BingNewsCrawler {

	public static String QUERY_FORMAT = "http://www.bing.com/news/search?q=%s&ctp=&first=%d&FORM=NWRFSH";
	public static String SR_DIV_SEL = ".sn_r";
	public static String SEL_SPACE = " ";
	public static String TITLE_LINK_SEL = ".newstitle a";
	public static String PUBLISHER_CITE_SEL = ".sn_ST .sn_src";
	public static String PUBLISHDATE_SPAN_SEL = ".sn_ST .sn_tm";
	public static String EDITORS_SPAN_SEL = ".sn_ST .sn_by span";
	
	//*************************************************
	public static String QUERY_DISASTER_SQL = "select id from disasters where name = ?";
	public static String INSERT_DISASTER_SQL = "insert into disasters (name) values (?)";
	public static String INSERT_DISASTER_NEWS_SQL = "insert into disaster_news " +
													"(disaster_id,title,authors,publisher,post_date,url,html) values " +
													"(?,?,?,?,?,?,?)";
	
	private String query;
	private int numToCraw;
	private int numPerPage;
	private int startPage;
	
	public BingNewsCrawler(String query,int startPage,int numToCraw) {
		// TODO Auto-generated constructor stub
		this.query = query;
		this.numToCraw = numToCraw;
		this.numPerPage = 10;
		this.startPage = startPage;
	}
	
	
	
	public void startCrawling(){
		
		List<BingSearchNews> news = crawlNewsUrl();
		System.out.println("news size: " + news.size());
		for(int i = 0; i < news.size(); i++){
			try {
				//download page might be out of time
				news.get(i).setHtml(downloadWebPage(news.get(i).getUrl()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(i % 50 == 0) System.out.println("Current page: " + i);
		}
		
		Connection conn = null;
		conn = DBConnection.getDisasterConnection();
		try {
			storeNewsData(conn, query, news);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void storeNewsData(Connection conn, String query, List<BingSearchNews> news) throws SQLException{
		
		PreparedStatement pstm = null;
		ResultSet rs = null;
		
		//find disaster id if already exists in db, otherwise insert one.
		int disaster_id;
		pstm = conn.prepareStatement(QUERY_DISASTER_SQL);
		pstm.setString(1, query);
		rs = pstm.executeQuery();
		if(rs.next()){
			disaster_id = rs.getInt(1);
		}else{
			pstm = conn.prepareStatement(INSERT_DISASTER_SQL);
			pstm.setString(1, query);
			pstm.executeUpdate();
			
			pstm = conn.prepareStatement(QUERY_DISASTER_SQL);
			pstm.setString(1, query);
			rs = pstm.executeQuery();
			if(rs.next()){
				disaster_id = rs.getInt(1);
			}else{
				System.err.println("Store Data Error!");
				return;
			}
		}
		
		
//		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		conn.setAutoCommit(false);
		pstm = conn.prepareStatement(INSERT_DISASTER_NEWS_SQL);
		for(int i = 0; i < news.size(); i++){
			
			BingSearchNews aNews = news.get(i);
			//(disaster_id,title,authors,publisher,post_date,url,html)
			pstm.setInt(1, disaster_id);
			pstm.setString(2, aNews.getTitle());
			pstm.setString(3, aNews.getAuthors());
			pstm.setString(4, aNews.getPublisher());
			pstm.setString(5,aNews.getDateTime());
			pstm.setString(6, aNews.getUrl());
			pstm.setString(7, aNews.getHtml());
			pstm.addBatch();
			
			if((i+1) % 100 == 0){
				pstm.executeBatch();
				conn.commit();
			}
		}
		
		
		pstm.executeBatch();
		conn.commit();
		conn.setAutoCommit(true);
	}
	
	
	public List<BingSearchNews> crawlNewsUrl(){
		
		List<BingSearchNews> news = new ArrayList<BingSearchNews>();
		
		for(int i = startPage; i <= numToCraw + startPage; i += numPerPage){
			
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
	
		BingNewsCrawler newsCrawer = new BingNewsCrawler("Hurricane Sandy", 1, 800);
		newsCrawer.startCrawling();
//		String title = "Hurricane Irene-damaged summer camp to rebuild";
//		System.out.println(title.length());
//		
	}
	
	
}
