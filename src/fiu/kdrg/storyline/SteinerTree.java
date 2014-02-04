package fiu.kdrg.storyline;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;

public class SteinerTree {
	Set<Integer> doms;//doms并不一定被cover
	Set<Integer> nodes;
	DefaultDirectedGraph<Integer, Edge> graphSteinerTreeInfo = null;
	
	int cover = 0;
	public SteinerTree(List<Integer> doms) {
		this.doms = new HashSet<Integer>(doms);
		nodes = new HashSet<Integer>();
		graphSteinerTreeInfo = new DefaultDirectedGraph<Integer, Edge>(Edge.class);
	}
	
	public int cover() {
		return cover;
	}

	public void add(int node) {
		if (!nodes.contains(node)) {	
			nodes.add(node);
			graphSteinerTreeInfo.addVertex(node);//图信息树也加上这个node
			if (doms.contains(node))
				cover++;
		}
	}
	
	public void add(SteinerTree besttree) {
		for(Integer i : besttree.nodes){
			add(i);
			for(Edge edge : besttree.getGraphSteinerTreeInfo().outgoingEdgesOf(i)){
				Integer target = besttree.getGraphSteinerTreeInfo().getEdgeTarget(edge);
				add(target);
				graphSteinerTreeInfo.addEdge(i, target);//把对方的图树信息结合起来
			}
		}
		
	}

	public Set<Integer> getDoms() {
		Set<Integer> coveredDoms = new HashSet<Integer>(doms);
		coveredDoms.retainAll(nodes);
		return coveredDoms;
	}

//	public double cost() {
//		if (nodes.size() == 0)
//			return Double.MAX_VALUE;
//		return nodes.size() / (cover + 0.000000001);
//	}
	
	public double cost() {
		if (graphSteinerTreeInfo.edgeSet().size() == 0)
			return Double.MAX_VALUE;
		return graphSteinerTreeInfo.edgeSet().size() / (cover + 0.000000001);
	}

//	public int size() {
//		return nodes.size();
//	}

	public DefaultDirectedGraph<Integer, Edge> getGraphSteinerTreeInfo() {
		return graphSteinerTreeInfo;
	}

	public void setGraphSteinerTreeInfo(
			DefaultDirectedGraph<Integer, Edge> graphSteinerTreeInfo) {
		this.graphSteinerTreeInfo = graphSteinerTreeInfo;
	}
	
}
