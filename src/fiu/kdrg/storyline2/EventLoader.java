package fiu.kdrg.storyline2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fiu.kdrg.db.DBConnection;
import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.LatLng;

public class EventLoader {

	
	public static String QUERY_EVENTS_BY_DID = "select * from events where disaster_id = ? and " +
							" event_date > ? and event_date < ?"; 
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
				event.setEventURL(rs.getString("url"));
				event.setEventDate(rs.getLong("event_date"));
				event.setLatlng(new LatLng(rs.getFloat("latitude"), 
											rs.getFloat("longtitude")));
				
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
		
		System.out.println(events.size());
		return events;
	}
	
	public static void main(String[] args) {
		
		EventLoader.loadEventByDisaster(1,"2005-01-01","2006-01-01");
		
	}
	
}
