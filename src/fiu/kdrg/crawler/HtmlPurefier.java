package fiu.kdrg.crawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.ArticleSentencesExtractor;
import de.l3s.boilerpipe.extractors.DefaultExtractor;

import fiu.kdrg.db.DBConnection;

public class HtmlPurefier {

	public static String QUERY_DISASTER_SQL = "select id from disasters where name = ?";
	public static String QUERY_DISASTER_NEWS_SQL = "select news_id, url,html from disaster_news " +
													"where disaster_id = ? and html IS NOT NULL";
	public static String UPDATE_TEXT_SQL = "update disaster_news set text = ? where news_id = ?";
	public static final int JSOUP_METHOD = 1;
	public static final int BP_DEFAULT = 2;
	public static final int BP_ARTICLE = 3;
	public static final int BP_ARTICLE_NEWS = 4;
	
	private String query;
	
	public HtmlPurefier(String query) {
		// TODO Auto-generated constructor stub
		this.query = query;
	}
	
	
	public void startPurefy(){
		
		List<BingSearchNews> news = fetchRawBingSearchNews();
		Connection conn = null;
		PreparedStatement pstm = null;
		
		try {
			conn = DBConnection.getDisasterConnection();
			pstm = conn.prepareStatement(UPDATE_TEXT_SQL);
			conn.setAutoCommit(false);
			
			for(int i = 0; i < news.size(); i++){
				String text = purefy(news.get(i).getHtml(), BP_ARTICLE);
				pstm.setString(1, text);
				pstm.setInt(2, news.get(i).getId());
				pstm.addBatch();
				
				if((i+1) % 100 == 0){
					pstm.executeBatch();
					conn.commit();
				}
			}
			
			pstm.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BoilerpipeProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	
	
	public String purefy(String html, int method) throws BoilerpipeProcessingException{
		
		String text = "";
		switch (method) {
		case JSOUP_METHOD:
			Document doc = Jsoup.parse(html);
			text = doc.body().text();
			break;
		case BP_DEFAULT:
			DefaultExtractor extractor = DefaultExtractor.getInstance();
			text = extractor.getText(html);
		case BP_ARTICLE:
			ArticleExtractor articleEx = ArticleExtractor.getInstance();
			text = articleEx.getText(html);
		case BP_ARTICLE_NEWS:
			ArticleSentencesExtractor articleS = ArticleSentencesExtractor.getInstance();
			text = articleS.getText(html);
		default:
			break;
		}

		return text;
	}
	
	
	public List<BingSearchNews> fetchRawBingSearchNews(){
		
		Connection conn = null;
		PreparedStatement pstm = null;
		conn = DBConnection.getDisasterConnection();
		ResultSet rs = null;
		int disasterID = -1;
		List<BingSearchNews> news = new ArrayList<BingSearchNews>();
		
		try {
			pstm = conn.prepareStatement(QUERY_DISASTER_SQL);
			pstm.setString(1, query);
			rs = pstm.executeQuery();
			if(rs.next()){
				disasterID = rs.getInt(1);
			}
			
			pstm = conn.prepareStatement(QUERY_DISASTER_NEWS_SQL);
			pstm.setInt(1, disasterID);
			rs = pstm.executeQuery();
			
			while(rs.next()){
				BingSearchNews tmp = new BingSearchNews();
				tmp.setId(rs.getInt(1));
				tmp.setUrl(rs.getString(2));
				tmp.setHtml(rs.getString(3));
				news.add(tmp);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return news;
		
	}
	
	
	public static void main(String[] args) throws BoilerpipeProcessingException {
		
		HtmlPurefier purefier = new HtmlPurefier("Hurricane Irene");
//		List<BingSearchNews> news = purefier.fetchRawBingSearchNews();
//		for(int i = 0 ; i < 2; i++){
//			System.out.println(news.get(i).getUrl());
//			System.out.println();
//			System.out.println(purefier.purefy(news.get(i).getHtml(),HtmlPurefier.BP_ARTICLE_NEWS));
//			System.out.println();
//		}
		purefier.startPurefy();
		
	}
	
	
}
