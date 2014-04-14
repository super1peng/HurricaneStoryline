package fiu.kdrg.storyline2.experiments;

import java.util.ArrayList;
import java.util.List;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline2.DocFilter;
import fiu.kdrg.storyline2.EventLoader;
import fiu.kdrg.util.EventUtil;

public class DominatingExperiment {

	public static void main(String[] args) {
		
		int disaster_id = 3;
		DocFilter filter = null;
		List<Event> allEvents = null;
		
		switch (disaster_id) {
		case 1:
			allEvents = EventLoader.loadEventByDisaster(disaster_id, "2005-08-16","2006-01-01");
			break;
		case 2:
			allEvents = EventLoader.loadEventByDisaster(disaster_id, "2012-10-16","2014-01-01");
			break;
		case 3:
			allEvents = EventLoader.loadEventByDisaster(disaster_id, "2011-08-09","2012-01-01");
			break;
		case 4:
			allEvents = EventLoader.loadEventByDisaster(disaster_id, "2012-10-16","2014-01-01");
		default:
			break;
		}
		
		filter = new DocFilter(allEvents);
		filter.setMiniSim(0.5);
		filter.setMaxDist(4);
		List<Event> filteredEvents = filter.filter(15);
		
		filteredEvents = EventUtil.sortEventByDate(filteredEvents);
		EventUtil.displayEvents(filteredEvents);
		
	}
	
}
