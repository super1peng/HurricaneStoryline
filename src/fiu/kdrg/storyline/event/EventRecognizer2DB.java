package fiu.kdrg.storyline.event;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fiu.kdrg.db.DBConnection;
import fiu.kdrg.nlp.NLPProcessor;

public class EventRecognizer2DB extends EventRecognizer {
	
	private Connection conn;
	private String disaster;
	private int disasterID;
	
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
		String disaster = "Hurricane Irene";
		recognizeEvents(conn, disaster);
		
	}
	
	
	public static String QUERY_NEWS_SQL = "select url , text from disaster_news " +
						"where disaster_id = ? and text IS NOT NULL";
	private static void recognizeEvents(Connection conn, String disaster) 
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
				String text = rs.getString(2);
				TextJob job = new TextJob(url, text);
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
				List<Event> events = NLPProcessor.getFinedEvent(rawEvents);
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
	
}
