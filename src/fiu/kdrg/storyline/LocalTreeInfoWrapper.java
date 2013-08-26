package fiu.kdrg.storyline;

import java.util.List;
import org.jgrapht.DirectedGraph;
import fiu.kdrg.storyline.event.Event;

class LocalTreeInfoWrapper{
	SteinerTree tree;
	DirectedGraph<Integer, Edge> connGraph;
	List<Event> events;
	
	public SteinerTree getTree() {
		return tree;
	}
	public void setTree(SteinerTree tree) {
		this.tree = tree;
	}
	public DirectedGraph<Integer, Edge> getConnGraph() {
		return connGraph;
	}
	public void setConnGraph(DirectedGraph<Integer, Edge> connGraph) {
		this.connGraph = connGraph;
	}
	public List<Event> getEvents() {
		return events;
	}
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	
}