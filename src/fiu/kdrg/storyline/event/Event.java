package fiu.kdrg.storyline.event;

import java.io.Serializable;
import java.util.*;

public class Event implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8421733172009352520L;
	
	String eventURL;
	String eventContent;
	String eventLocation;
	Long eventDate;
	LatLng latlng;
	int id;
	double weight;
	int clusterId;
	
	public Event() {
		// TODO Auto-generated constructor stub
	}
	
	
	public Event(String eventURL, String eventContent, String eventLocation, Long eventDate, LatLng latlng) {
		// TODO Auto-generated constructor stub
		this.eventURL = eventURL;
		this.eventContent = eventContent;
		this.eventLocation = eventLocation;
		this.eventDate = eventDate;
		this.latlng = latlng;
		weight = 0;
		clusterId = -1;
	}
	
	public Event(String eventURL, String eventContent, String eventLocation){
		this(eventURL, eventContent, eventLocation, null, null);
	}
	
	public Event(String eventURL, String eventContent, String eventLocation, Long eventDate)
	{
		this(eventURL, eventContent, eventLocation, eventDate, null);
	}

	public String getEventURL() {
		return eventURL;
	}

	public void setEventURL(String eventURL) {
		this.eventURL = eventURL;
	}

	public String getEventContent() {
		return eventContent;
	}

	public void setEventContent(String eventContent) {
		this.eventContent = eventContent;
	}

	public String getEventLocation() {
		return eventLocation;
	}

	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}

	public Long getEventDate() {
		return eventDate;
	}

	public void setEventDate(Long eventDate) {
		this.eventDate = eventDate;
	}

	public LatLng getLatlng() {
		return latlng;
	}

	public void setLatlng(LatLng latlng) {
		this.latlng = latlng;
	}
	

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}
	

	public double getWeight() {
		return weight;
	}


	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public int getClusterId() {
		return clusterId;
	}


	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}


	public List<String> getNGramsOfContent() {
		List<String> ngrams = new ArrayList<String>();
		String cont = getEventContent();
		String[] words = cont.split(" ");
		for(int n = 1; n < 3; n++) {
			for(int i = 0; i < words.length - n + 1; i++) {
				String ngram = words[i];
				for(int j = 1; j < n; j++) {
					ngram += " " + words[i + j];
				}
				ngrams.add(ngram);
			}
		}
		return ngrams;
	}
	
	
	/**
	 * test whether distance of two events within certain range.
	 * @param event
	 * @param n
	 * @return
	 */
	public boolean hasDistanceLe(Event event, double n){
		
		double dist = this.computeDist(event);
		if(dist <= n)
			return true;
		else
			return false;
	}
	
	
	
	
	public double computeDist(Event event){
		
		double latDif = this.getLatlng().getLatitude() - event.getLatlng().getLatitude();
		double lngDif = this.getLatlng().getLongtitude() - event.getLatlng().getLongtitude();
		
		return Math.sqrt(latDif * latDif + lngDif * lngDif);
		
	}
}