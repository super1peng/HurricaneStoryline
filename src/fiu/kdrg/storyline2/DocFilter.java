package fiu.kdrg.storyline2;

import java.util.ArrayList;
import java.util.List;

import org.jblas.DoubleMatrix;

import fiu.kdrg.storyline.StorylineGen;
import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.EventUtil;
import fiu.kdrg.util.Util;

public class DocFilter {
	
	StorylineGen loader;
	List<Event> events;
	double miniSim;
	double maxDist;
	
	public DocFilter() {
		// TODO Auto-generated constructor stub
		this.loader  = new StorylineGen();
		try {
			loader.loadEvents("./sandy_all_clean_nodup_events_latlng.txt", 
					loader.dateFormat.parse("2012-10-24"), 
					loader.dateFormat.parse("2012-11-06"), 
					"sandy|hurricane|storm|disaster");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		events = loader.getEvents();
		setEventID();
		
		miniSim = 0.5;
		maxDist = 5;
	}
	
	
	
	public ArrayList<Event> filter(int k){
		
		DoubleMatrix connGraph = genConnGraph();
		int n = events.size();
		DoubleMatrix uncovered = DoubleMatrix.ones(1,n);
		ArrayList<Event> results = new ArrayList<Event>();
		
		int i = 0;
		while(i < k) {
			DoubleMatrix covering = DoubleMatrix.ones(1,n);
			covering.copy(uncovered);
			covering = covering.mmul(connGraph);
			int sel = covering.argmax();
			double maxdeg = covering.get(sel);
			
			if (maxdeg < 0.5)
				break;
			
			results.add(events.get(sel));
			
			DoubleMatrix ind = connGraph.getRow(sel).ge(0.5);			
			uncovered = uncovered.put(0, ind, 0);
			
			i++;
		}
		
		return results;
	}
	
	
	
	
	
	private DoubleMatrix genConnGraph(){
		
		double[][] simGraph = StoryUtil.computeSimilarity(events);
		int n = events.size();
		DoubleMatrix connGraph = DoubleMatrix.zeros(n, n);
		for(int i = 0; i < n; i++) {
			for(int j = i+1; j < n; j++) {
				if (simGraph[i][j] > miniSim &&
						events.get(i).hasDistanceLe(events.get(j), maxDist)){
					connGraph.put(i, j, 1);
					connGraph.put(j, i, 1);
				}
			}
		}
		
		
		return connGraph;
	}
	

	public void setMiniSim(double miniSim) {
		this.miniSim = miniSim;
	}
	
	

	public void setMaxDist(double maxDist) {
		this.maxDist = maxDist;
	}
	
	/**
	 * set id for events, after this, all events will identifies by its id.
	 */
	private void setEventID()
	{
		for(int id = 0; id < events.size(); id++){
			events.get(id).setId(id);
		}
	}


	public static void main(String[] args) {
		
//		DocFilter filter = new DocFilter();
//		filter.setMiniSim(0.5);
//		filter.setMaxDist(5);
//		ArrayList<Event> filteredEvents = filter.filter(100);
//		
//		try {
//			SerializeFactory.serialize(Util.rootDir + "filterEvents.out", filteredEvents);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		ArrayList<Event> filterEvents = null;
		
		try {
			filterEvents = (ArrayList<Event>) SerializeFactory.deSerialize(Util.rootDir + "filterEvents.out");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(filterEvents.size());
		EventUtil.displayEvents(EventUtil.sortEventByDate(filterEvents));
		
	}
	
}
