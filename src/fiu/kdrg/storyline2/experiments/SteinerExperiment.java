package fiu.kdrg.storyline2.experiments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline2.DocFilter;
import fiu.kdrg.storyline2.EventLoader;
import fiu.kdrg.storyline2.StoryUtil;
import fiu.kdrg.util.EventUtil;
import graphTheory.algorithms.steinerProblems.steinerArborescenceApproximation.GFLACAlgorithm;
import graphTheory.algorithms.steinerProblems.steinerArborescenceApproximation.SteinerArborescenceApproximationAlgorithm;
import graphTheory.graph.Arc;
import graphTheory.graph.DirectedGraph;
import graphTheory.graphDrawer.EnergyAnalogyGraphDrawer;
import graphTheory.instances.steiner.classic.SteinerDirectedInstance;

public class SteinerExperiment {

	
	public static void visualizeArcs(Set<Arc> arcs){
		if(arcs == null) return;
		DirectedGraph dg = new DirectedGraph();
		
		//add vertices
		for(Arc arc : arcs){
			dg.addVertice(arc.getInput());
			dg.addVertice(arc.getOutput());
		}
		
		//add arcs
		for(Arc arc : arcs){
			dg.addArc(arc.getInput(), arc.getOutput(), true);
		}
		
		new EnergyAnalogyGraphDrawer(dg);
	}
	
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
		List<Event> filteredEvents = filter.filter(12);
		
		double[][] simGraph = StoryUtil.computeSimilarity(allEvents);
		
		DirectedGraph dg = new DirectedGraph();
		for(Event e : allEvents){
			dg.addVertice(e.getId());
		}
		
		//construct directed graph
		double minSim = 0.0;
		for(int i = 0; i < allEvents.size(); i ++){
            long ti = allEvents.get(i).getEventDate();
            for(int j = i + 1; j < allEvents.size(); j ++){
                long tj = allEvents.get(j).getEventDate();
                if(simGraph[i][j] > minSim){
                	if(ti == tj){
                        dg.addDirectedEdge(allEvents.get(i).getId(), allEvents.get(j).getId());
                        dg.addDirectedEdge(allEvents.get(j).getId(), allEvents.get(i).getId());
                    }else if(Math.abs(ti - tj) < 1000 * 3600 * 30){//30 hour
                        if(ti < tj){
                            dg.addDirectedEdge(allEvents.get(i).getId(), allEvents.get(j).getId());
                        }else{
                            dg.addDirectedEdge(allEvents.get(j).getId(), allEvents.get(i).getId());
                        }
                    }
                }
            }
        }
		
		
		SteinerDirectedInstance sdi = new SteinerDirectedInstance(dg);
		List<Event> dominate = EventUtil.sortEventByDate(filteredEvents);
		sdi.setRoot(dominate.get(0).getId());
		//set required node
		for(Event e : filteredEvents){
			sdi.setRequired(e.getId());
		}
		
		
		
		SteinerArborescenceApproximationAlgorithm alg = new GFLACAlgorithm();
		alg.setInstance(sdi);
		alg.compute();
		
		Set<Integer> ids = new HashSet<Integer>();
		for(Arc arc : alg.getArborescence()){
			ids.add(arc.getInput());
			ids.add(arc.getOutput());
		}
		
		List<Event> result = new ArrayList<Event>();
		for(Event e : allEvents){
			if(ids.contains(e.getId())){
				result.add(e);
			}
		}
		
		result = EventUtil.sortEventByDate(result);
		EventUtil.displayEvents(result);
		//search database for detail information
//		SteinerExperiment.visualizeArcs(alg.getArborescence());
		
	}
	
	
}
