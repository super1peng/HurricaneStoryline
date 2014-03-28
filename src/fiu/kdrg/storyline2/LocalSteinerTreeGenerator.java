package fiu.kdrg.storyline2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.EventUtil;
import fiu.kdrg.util.Util;
import graphTheory.algorithms.steinerProblems.steinerArborescenceApproximation.ShPAlgorithm;
import graphTheory.graph.DirectedGraph;
import graphTheory.instances.steiner.classic.SteinerDirectedInstance;

public class LocalSteinerTreeGenerator {

	
	private Logger logger = LoggerFactory.getLogger(LocalSteinerTreeGenerator.class);
	
	private List<Event> events;
	private double circleRange;
	private int k;
	private int hourGap;
	private int dID;
	
	
	public LocalSteinerTreeGenerator(int dID) {
		this.dID = dID;
		this.circleRange = 5.0;
		this.k = 12;
		hourGap = 50;
		init();
	}
	
	
	public ShPAlgorithm compute(int id){
		Event event = searchEvent(id);
		if(event != null)
			return compute(searchEvent(id));
		return null;
	}
	
	
	/**
	 * compute Steiner Tree
	 * @param id
	 * @return
	 */
	public ShPAlgorithm compute(Event e){
		
		DirectedGraph dg = new DirectedGraph();
		
		List<Event> localEvents = getNeighbors(e);
		for(Event event : localEvents){
			dg.addVertice(event.getId());
		}
		
		
		for(int i = 0; i < localEvents.size(); i ++){
			long ti = localEvents.get(i).getEventDate();
			for(int j = i + 1; j < localEvents.size(); j ++){
				long tj = localEvents.get(j).getEventDate();
				if(ti == tj){
					dg.addDirectedEdge(localEvents.get(i).getId(), localEvents.get(j).getId());
					dg.addDirectedEdge(localEvents.get(j).getId(), localEvents.get(i).getId());
				}else if(Math.abs(ti - tj) < 1000 * 3600 * hourGap){
					if(ti < tj){
						dg.addDirectedEdge(localEvents.get(i).getId(), localEvents.get(j).getId());
					}else{
						dg.addDirectedEdge(localEvents.get(j).getId(), localEvents.get(i).getId());
					}
				}
			}
		}
		
		SteinerDirectedInstance sdi = new SteinerDirectedInstance(dg);
		List<Event> dominate = EventUtil.sortEventByDate((new DocFilter(localEvents)).filter(this.k));
		sdi.setRoot(dominate.get(0).getId());
		
		for(Event event : dominate){
			sdi.setRequired(event.getId());
		}
		
		ShPAlgorithm alg = new ShPAlgorithm();
		alg.setInstance(sdi);
		alg.compute();
		
		return alg;
	}
	
	
	private void init(){
		
		try {
			events = (List<Event>) SerializeFactory.deSerialize(Util.rootDir + "allEvents"+dID+".out");
			logger.info(String.format("load disaster %d event done!", dID));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private List<Event> getNeighbors(Event center){
		List<Event> result = new ArrayList<Event>();
		
		for(Event event : this.events){
			if(event.hasDistanceLe(center,circleRange)){
				result.add(event);
			}
		}
		
		result.add(center);
		return result;
	}
	
	
	
	private Event searchEvent(int id){
		for(Event event : events){
			if(event.getId() == id){
				return event;
			}
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		
		int disaster_id = 4;
		List<Event> finalR = null;
		
		try {
			finalR = (List<Event>) SerializeFactory.deSerialize(Util.rootDir + "finalResult"+disaster_id+".out");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Event> st = StoryUtil.findStoryEvents(finalR);
		System.out.println(finalR.size());
		System.out.println(st.size());
		
		
		LocalSteinerTreeGenerator stg = new LocalSteinerTreeGenerator(disaster_id);
		ShPAlgorithm alg = stg.compute(st.get(0));
		
		System.out.println("Returned solution : " + alg.getArborescence());
		System.out.println("Cost: " + alg.getCost());
		System.out.println("Running Time: " + alg.getTime() + " ms");
	}
	
	
}
