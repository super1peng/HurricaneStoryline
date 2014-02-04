package fiu.kdrg.storyline2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fiu.kdrg.db.DBConnection;
import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.LatLng;

public class EventLoader {

	private static Logger logger = LoggerFactory.getLogger(EventLoader.class);
	
	public static String QUERY_EVENTS_BY_DID = "select * from events where disaster_id = ? and " +
							" event_date >= ? and event_date <= ?"; 
	public static List<Event> loadEventByDisaster(int disasterId,
			String from, String to) {
		
		List<Event> events = new ArrayList<Event>();
		Connection conn = DBConnection.getDisasterConnection();
		PreparedStatement pstm = null;
		
		try {
			pstm = conn.prepareStatement(QUERY_EVENTS_BY_DID);
			pstm.setInt(1, disasterId);
			pstm.setString(2, from);
			pstm.setString(3, to);
			
			ResultSet rs = pstm.executeQuery();
			while(rs.next()) {
				
				Event event = new Event();
				event.setId(rs.getInt("event_id"));
				event.setEventContent(rs.getString("content"));
//				rs.getDate("event_date").getTime();
				event.setEventURL(rs.getString("url"));
				event.setEventDate(rs.getDate("event_date").getTime());
				event.setLatlng(new LatLng(rs.getFloat("latitude"), 
											rs.getFloat("longtitude")));
				event.setEventLocation(rs.getString("location"));
				events.add(event);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		logger.info(String.format("loading %d events",events.size()));
		return events;
	}
	
	
	
	public static String QUERY_DOMS_BY_DID = "select * from eventss where disaster_id = ? and " +
			" event_date >= ? and event_date <= ?"; 
	public static List<Event> loadDOMSByDisaster(int disasterId,
			String from, String to) {
		
		List<Event> events = new ArrayList<Event>();
		Connection conn = DBConnection.getDisasterConnection();
		PreparedStatement pstm = null;
		
		try {
			pstm = conn.prepareStatement(QUERY_DOMS_BY_DID);
			pstm.setInt(1, disasterId);
			pstm.setString(2, from);
			pstm.setString(3, to);
			
			ResultSet rs = pstm.executeQuery();
			while(rs.next()) {
				
				Event event = new Event();
				event.setId(rs.getInt("event_id"));
				event.setEventContent(rs.getString("content"));
//				rs.getDate("event_date").getTime();
				event.setEventURL(rs.getString("url"));
				event.setEventDate(rs.getDate("event_date").getTime());
				event.setLatlng(new LatLng(rs.getFloat("latitude"), 
											rs.getFloat("longtitude")));
				event.setEventLocation(rs.getString("location"));
				events.add(event);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		logger.info(String.format("loading %d events",events.size()));
		return events;
	}
	
	public static void main(String[] args) {
		
		EventLoader.loadEventByDisaster(1,"2005-01-01","2006-01-01");
		
	}
	
}
