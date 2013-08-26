package fiu.kdrg.storyline;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import com.sleepycat.asm.Label;

import fiu.kdrg.geocode.Geocoder;
import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.LatLng;

public class StorylineGen {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StorylineGen storyline = new StorylineGen();
		storyline.loadEvents("./sandy_all_clean_nodup_events_latlng.txt", 
				dateFormat.parse("2012-10-24"), dateFormat.parse("2012-11-06"), "sandy|hurricane|storm|disaster");
//		storyline.exportEvents("../sandy_all_clean_nodup_events_latlng.txt");
//		for(Event event : storyline.events) 
//			System.out.println(event.getEventLocation());
	}
	
	public StorylineGen() {
		
	}
	
	List<Event> events;
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	StorylineStructure storyline;

	public void loadEvents(String file, Date from, Date to, String keywordpat) throws Exception {
		Pattern pat = null;
		if (keywordpat != null)
			pat = Pattern.compile(keywordpat);
		Geocoder geocoder = Geocoder.getGeocoder();
		events = new ArrayList<Event>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = br.readLine()) != null) {
			String[] fds = line.split("\t");
			if (fds.length > 5) {
				LatLng latlng = new LatLng(Float.parseFloat(fds[4]), Float.parseFloat(fds[5]));
				events.add(new Event(fds[0], fds[1], 
						fds[2], Long.parseLong(fds[3]), latlng));
			} else {
				long time = dateFormat.parse(fds[1]).getTime();
				if (time >= from.getTime() && time <= to.getTime() && (pat == null || pat.matcher(fds[3].toLowerCase()).find())) {
					Event event = new Event(fds[0], fds[3], fds[2], time);
					LatLng latlng = geocoder.getLatLng(event.getEventLocation());
					if (latlng != null && latlng.isValid()) {
						event.setLatlng(latlng);
						events.add(event);
					}
				}
			}
		}
		br.close();
		System.err.println("load " + events.size() + " events");
	}
	
	public void genStoryline() {
		
	}
	
	public void exportEvents(String outputfile) throws Exception {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputfile)); 
		for(Event event : events) {
			LatLng latlng = event.getLatlng();
			bw.write(event.getEventURL() + "\t" + event.getEventContent() + "\t" + event.getEventLocation() + "\t" + event.getEventDate() + "\t" + latlng.getLatitude() + "\t" + latlng.getLongtitude() + "\n");
			bw.flush();
//			event.setLatlng(latlng);
//			if (latlng != null && !( latlng.getLatitude().equals(0.0F) && latlng.getLongtitude().equals(0.0F))) {
//				
//			}
			
		}
		bw.close();
	}

	public List<Event> getEvents() {
		return events;
	}

}
