package fiu.kdrg.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.LatLng;



public class IOUtil {
	public static void main(String[] args) throws IOException, ParseException {
		
		String path = System.getProperty("user.dir");
		path = path + "/WebContent/location.txt";
		
		System.out.println(path);
		
		List<Event> eventArray = new ArrayList<Event>();
		eventArray = IOUtil.parseFileTOEvents(path);
		eventArray = EventUtil.sortEventByDate(eventArray);
		
		for(Event e : eventArray)
		{
			System.out.println(e.getEventDate());
		}
		
	}
	
	
	
	
	public static boolean writeStringToFile(String content, String destination, String charSet){
		
		BufferedWriter bw;
		
		try {
			bw = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(new File(destination)), charSet));
			
			bw.write(content);
			bw.flush();
			bw.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	public static boolean writeEventsToFile(List<Event> events, String destination)
	{
		BufferedWriter bw;
		StringBuffer sb = new StringBuffer();
		
		try {
			
			bw = new BufferedWriter(new FileWriter(new File(destination)));
			for(int i = 0; i < events.size(); i++)
			{
				Event tmpEvent = events.get(i);
				
				if(EventUtil.allAttributesNonempty(tmpEvent))
				{
					sb.append(tmpEvent.getEventURL() + "\t\t" + tmpEvent.getEventContent().replaceAll("\n", " ") + "\t\t" + tmpEvent.getEventLocation()
							   		+ "\t\t" + tmpEvent.getEventDate() + "\t\t" + tmpEvent.getLatlng().getLatitude() 
							   		+ "\t\t" + tmpEvent.getLatlng().getLongtitude() + "\n");
				}
			}
			
			bw.write(sb.toString());
			bw.flush();
			bw.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
			
		}
		
		return true;
		
	}
	
	
public static ArrayList<Event> parseFileTOEvents(String filepath) throws IOException{
		
		ArrayList<Event> eventArray = new ArrayList<Event>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filepath)));
			
			String str = "";
			String[] parsedSingleEvent;
			while(null != (str = br.readLine()))
			{
				parsedSingleEvent = str.split("\t+");
				LatLng latlng = new LatLng(Float.parseFloat(parsedSingleEvent[4]), Float.parseFloat(parsedSingleEvent[5]));
				
				
				eventArray.add(new Event(parsedSingleEvent[0], parsedSingleEvent[1], 
												parsedSingleEvent[2], Long.parseLong(parsedSingleEvent[3]), latlng));
				
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return eventArray;
		
	}
	
	
}












