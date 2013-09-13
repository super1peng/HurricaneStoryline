package fiu.kdrg.storyline2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.EventUtil;
import fiu.kdrg.util.Util;

public class MediumStoryTeller extends StoryTeller {
	
	List<Event> events;
	Event center = null;
	float circleRange;
	DocFilter filter = null;
	List<Event> neighbor = null;
	int k = 0;
	

	public MediumStoryTeller(List<Event> events, Event center, float circleRange) {
		// TODO Auto-generated constructor stub
		super();
		this.events = events;
		this.center = center;
		this.circleRange = circleRange;
		this.neighbor = getNeighbors();
		this.filter = new DocFilter(this.neighbor);
		k = this.neighbor.size() / 4 > 12 ? 12 : this.neighbor.size()/4;
	}
	
	
	private List<Event> getNeighbors(){
		List<Event> result = new ArrayList<Event>();
		
		for(Event event : this.events){
			
			if(event.hasDistanceLe(center,circleRange)){
				result.add(event);
			}
			
		}
		return result;
	}
	
	
	private void  chooseStorylineEvents(int k){
		
		ArrayList<Event> tmpEvents = this.filter.filter(k);
		this.setDomEvents(EventUtil.sortEventByDate(tmpEvents));
		
	}
	
	
	public List<Event> getDomset(){
		
		chooseStorylineEvents(k);
		return domEvents;
		
	}
	
	
	public void setCircleRange(float circleRange) {
		this.circleRange = circleRange;
	}
	
	public void setK(int k){
		this.k = k;
	}


	public static void main(String[] args) {
		
		ArrayList<Event> allEvent = null;
		ArrayList<Event> finalEvent = null; 
		
		try {
			allEvent = (ArrayList<Event>) SerializeFactory.deSerialize(Util.rootDir + "allEvents.out");
			finalEvent = (ArrayList<Event>) SerializeFactory.deSerialize(Util.rootDir + "finalResult.out");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		for(Event center: StoryUtil.findStoryEvents(finalEvent)){
			
			MediumStoryTeller msteller = new MediumStoryTeller(allEvent, center,4);
			msteller.filter.setMaxDist(0.4);
			msteller.filter.setMiniSim(0.4);
			List<Event> tmpEvents = msteller.getDomset();
			System.out.println(tmpEvents.size());
			
			try {
				SerializeFactory.serialize(Util.rootDir + "storyline" + center.getId() + ".out", tmpEvents);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	
}
