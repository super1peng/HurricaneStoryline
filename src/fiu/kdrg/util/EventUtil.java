package fiu.kdrg.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.NamedEntity;
import fiu.kdrg.storyline.event.RawEvent;

public class EventUtil {

	
	public final static String YEAR_REGEX = "(?<![0-9])[0-9]{4}(?![0-9])";
	public final static String MONTH_REGEX = "(?i)jan(uary)?|feb(ruary)?|mar(ch)?|apr(il)?|may|jun(e)?|jul(y)?|" +
			"aug(ust)?|sep(tember)?|oct(ober)?|nov(ember)?|dec(ember)?";
	public final static String DAY_REGEX = "(?<![0-9])(0?[1-9]|[1-2][0-9]|3[0-1])(?![0-9])";
	public final static String WEEKDAY_REGEX = "(?i)Mon(day)?|Tue(sday)?|Wen(desday)?|Thu(rsday)?|Fri(day)?|Sat(urday)?|Sun(day)?";
	public final static String[] MONTH_MAPPER = {"jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"};
	
	public static void displayEvent(Event event)
	{
		if(null == event)
			return;
		System.out.print(event.getEventURL() + "\t");
		System.out.print(event.getEventContent() + "\t");
		System.out.print(event.getEventLocation() +"\t" + event.getEventDate());
		if(null != event.getLatlng())
			System.out.print("\t" + event.getLatlng().getLatitude() + "\t" + event.getLatlng().getLongtitude());
		System.out.println();
	}
	
	
	
	
	/**
	 * display all elements of events and content of every element
	 * @param events
	 */
	public static void displayEvents(List<Event> events)
	{
		if(null == events || events.isEmpty())
			return;
		
		Event tmp;
		int i = 1;
		for(Iterator<Event> it = events.iterator(); it.hasNext(); )
		{
			tmp = it.next();
			System.out.println(i++);
			displayEvent(tmp);
		}
	}
	
	
	
	/**
	 * display a single rawEvent
	 * @param rawEvent
	 */
	public static void displayRawEvent(RawEvent rawEvent)
	{
		if(null == rawEvent)
			return;
		
		System.out.println(rawEvent.getSentence());
		
		Iterator<NamedEntity> iter = rawEvent.getEntities().iterator();
		while(iter.hasNext())
		{
			NamedEntity tmp = (NamedEntity) iter.next();
			
			System.out.println("TYPE: " + tmp.getType() + "\t" +
								"ENTITYTEXT: " + tmp.getEntityText() + "\t" + 
								"POSITION: " + tmp.getBeginPosition() + "\t");
			
		}
		
	}
	
	
	
	/**
	 * display a list of rawevents
	 * @param rawEvents
	 */
	public static void displayRawEvents(List<RawEvent> rawEvents)
	{
		if(rawEvents.isEmpty())
			return;
		
		for(int i = 0; i < rawEvents.size(); i++)
		{
			System.out.println(i+1);
			displayRawEvent(rawEvents.get(i));
		}
	}
	
	
	public static List<Event> sortEventByDate(List<Event> events){
		
		Collections.sort(events, new Comparator<Event>() {
			
			@Override
			public int compare(Event o1, Event o2) {
				// TODO Auto-generated method stub
				return o1.getEventDate().compareTo(o2.getEventDate());
			}
			
		});
		return events;
	}
	
	
	public static boolean allAttributesNonempty(Event event)
	{
		boolean hasURL = !(null == event.getEventURL() || event.getEventURL().equals(""));
		boolean hasContent = !(null == event.getEventContent() || event.getEventContent().equals(""));
		boolean hasLocation = !(null == event.getEventLocation() || event.getEventLocation().equals(""));
		boolean hasDate = !( null == event.getEventDate());
		boolean haslatlng = !(null == event.getLatlng());
		
		return (hasURL && hasContent && hasLocation && hasDate && haslatlng);
	}
	
	
	
	
	
	
}
