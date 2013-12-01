package fiu.kdrg.storyline.event;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fiu.kdrg.db.DBConnection;
import fiu.kdrg.geocode.Geocoder;
import fiu.kdrg.nlp.NLPProcessor;
import fiu.kdrg.util.EventUtil;

public class EventRecognizer2DB extends EventRecognizer {
	
	private Connection conn;
	private String disaster;
	private int disasterID;
	private static Integer COUNTER = 0;
	
	public EventRecognizer2DB(Properties props, JobList jobList,
							Connection conn, String disaster) {
		// TODO Auto-generated constructor stub
		super();
		this.conn = conn;
		this.disaster = disaster;
		this.disasterID = getDisasterID(conn, disaster);
		processor = new NLPProcessor(props);
		this.jobList = jobList;
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		
		Connection conn = DBConnection.getDisasterConnection();
		String disaster = "Hurricane Sandy";
		int disasterID = 1;
//		recognizeEvents(conn, disaster);
		fetchEventLatLng(conn,disasterID);
		
	}
	
	
	public static String QUERY_NEWS_SQL = "select url, post_date, text from disaster_news " +
						"where disaster_id = ? and text IS NOT NULL";
	public static void recognizeEvents(Connection conn, String disaster) 
						throws InterruptedException{
		
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.put("ner.model.3class", "");
		props.put("ner.model.MISCclass", "");
		
		JobList jobList = new JobList();
		ExecutorService executor = Executors.newFixedThreadPool(JobList.MAX_AVAILABLE);
		for(int i = 0; i < JobList.MAX_AVAILABLE; i++){
			executor.execute(new EventRecognizer2DB(props, jobList,conn, disaster));
		}
		
		executor.shutdown();
		
		int disasterID = getDisasterID(conn, disaster);
		if(disasterID == -1) return;
		try {
			PreparedStatement pstm = conn.prepareStatement(QUERY_NEWS_SQL);
			pstm.setInt(1, disasterID);
			ResultSet rs = pstm.executeQuery();
			while(rs.next()){
				String url = rs.getString(1);
				String date = rs.getString(2);
				String text = rs.getString(3);
				TextJob job = new TextJob(url, text, date);
				jobList.putItem(job);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i = 0; i < JobList.MAX_AVAILABLE; i++) {
			jobList.putItem(null);//help to break the run loop
		}
		
		executor.awaitTermination(1000, TimeUnit.DAYS);
		System.out.println("Totally count: " + COUNTER);
		
	}
	
	
	public static String QUERY_DISASTER_SQL = "select id from disasters where name = ?";
	public static int getDisasterID(Connection conn, String disaster) {
		
		PreparedStatement pstm = null;
		try {
			pstm = conn.prepareStatement(QUERY_DISASTER_SQL);
			pstm.setString(1, disaster);
			ResultSet rs = pstm.executeQuery();
			if(rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	
	
	public static String INSERT_EVENT_SQL = 
			"insert into events (disaster_id,url,content,event_date,location) values (?,?,?,?,?)";
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			TextJob job = null;
			synchronized(jobList) {
				try{
					job = jobList.getJob();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				
				if(job == null)
					break;
				
				List<RawEvent> rawEvents = processor.processString2RawEvents(job.text);
//				EventUtil.displayRawEvents(rawEvents);
				List<Event> events = NLPProcessor.getFinedEvent(rawEvents, job.date);
				synchronized (COUNTER) {
					COUNTER += events.size();
				}
				synchronized (conn) {
					try {
						
						conn.setAutoCommit(false);
						PreparedStatement pstm = conn.prepareStatement(INSERT_EVENT_SQL);
						for(Event event: events){
							pstm.setInt(1, disasterID);
							pstm.setString(2, job.url);
							pstm.setString(3, event.getEventContent());
							pstm.setDate(4, new Date(event.getEventDate()));
							pstm.setString(5, event.getEventLocation());
							pstm.addBatch();
						}
						
						pstm.executeBatch();
						conn.commit();
						conn.setAutoCommit(true);
						
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						System.err.println("failed to process url: " + job.url);
					}
				}
			}
		}
	}
	
	
	public static String INSERT_EVENTS = "insert into events " +
			"(disaster_id,url,content,event_date,location,latitude,longtitude) values (?,?,?,?,?,?,?)";
	public static void insertBatchEvent2DB(List<Event> events, int disasterID) {
		
		Connection conn = null;
		int count = 0;
		
		try {
			
			conn = DBConnection.getDisasterConnection();
			conn.setAutoCommit(false);
			PreparedStatement pstm = conn.prepareStatement(INSERT_EVENTS);
			for(Event event: events){
				pstm.setInt(1, disasterID);
				pstm.setString(2, event.getEventURL());
				pstm.setString(3, event.getEventContent());
				pstm.setDate(4, new Date(event.getEventDate()));
				pstm.setString(5, event.getEventLocation());
				pstm.setFloat(6, event.getLatlng().getLatitude());
				pstm.setFloat(7, event.getLatlng().getLongtitude());
				pstm.addBatch();
				count ++;
				
				if(count % 200 == 0 ) {
					pstm.executeBatch();
					conn.commit();
				}
				
			}
			
			pstm.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	
	
	public static String EVENTS_QUERY = "select event_id, location from events where disaster_id = ? and " +
										"(latitude IS NULL or longtitude IS NULL)";
	public static String UPDATE_EVENTS_LATLNG = "update events set latitude = ?, longtitude = ? " +
												"where event_id = ?";
	public static void fetchEventLatLng(Connection conn, int disaster) {
		PreparedStatement pstm = null;
		ResultSet rs = null;
		Map<Integer, String> queryInfo = new HashMap<Integer, String>();
		Map<Integer, LatLng> updateInfo = new HashMap<Integer, LatLng>();
		
		try {
			pstm = conn.prepareStatement(EVENTS_QUERY);
			pstm.setInt(1, disaster);
			rs = pstm.executeQuery();
			while(rs.next()) {
				queryInfo.put(rs.getInt(1), rs.getString(2));
			}

			Geocoder geoCoder = new Geocoder();
			for(int key : queryInfo.keySet()) {
				LatLng tmpLat = geoCoder.getLatLng(queryInfo.get(key));
				if(tmpLat != null)
					updateInfo.put(key, tmpLat);
			}
			
			conn.setAutoCommit(false);
			pstm = conn.prepareStatement(UPDATE_EVENTS_LATLNG);
			LatLng latLng = null;
			int count = 0;
			for(int key: updateInfo.keySet()) {
				latLng = updateInfo.get(key);
				pstm.setFloat(1,latLng.getLatitude());
				pstm.setFloat(2, latLng.getLongtitude());
				pstm.setInt(3, key);
				pstm.addBatch();
				count ++;
				
				if ((count+1) % 200 == 0) {
					pstm.executeBatch();
					conn.commit();
				}
			}
			
			pstm.executeBatch();
			conn.commit();
			conn.setAutoCommit(false);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
}
