package fiu.kdrg.storyline;

import java.util.*;

import org.jblas.DoubleMatrix;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import fiu.kdrg.storyline.event.*;

public class StorylineGenSteinerTree extends StorylineGenBaseline {

	public StorylineGenSteinerTree() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StorylineGen storyline = new StorylineGenSteinerTree();
		storyline.loadEvents("./sandy_all_clean_nodup_events_latlng.txt", 
				dateFormat.parse("2012-10-24"), dateFormat.parse("2012-11-06"), 
				"sandy|hurricane|storm|disaster");
		storyline.genStoryline();

	}
	
//	@Override
//	public void genStoryline() {
//		List<LatLng> locations = new ArrayList<LatLng>();
//		for(Event event : events) {
//			locations.add(event.getLatlng());
//		}
//		
//		KMeansClusteringJava clustering = new KMeansClusteringJava();
//		clustering.locations = locations;
//		Members[] clusters = clustering.cluster(k);
//		
//		for(Members members : clusters) {
//			System.out.println(members.size());
//			final List<Event> localEvents = new ArrayList<Event>();
//			for(Integer i : members) {
//				localEvents.add(events.get(i));
//			}
//			
//			double[][] simGraph = genSimGraph(localEvents);
//			
//			List<Integer> doms = getDomSet(localEvents, 10, simGraph);
//			Collections.sort(doms, new Comparator<Integer>() {
//				@Override
//				public int compare(Integer o1, Integer o2) {
//					return (int)(localEvents.get(o1).getEventDate() - localEvents.get(o2).getEventDate()); 
//				}
//			});
//			for(Integer di : doms) {
//				Event dom = localEvents.get(di);
//				System.out.println(String.format("%s, %s\n%s", 
//						dateFormat.format(new Date(dom.getEventDate())), 
//						dom.getEventLocation(),
//						dom.getEventContent()));
//			}
//			
//			DirectedGraph<Integer, Edge> connGraph = getConnGraph(localEvents,
//					simGraph);
//			
//			SteinerTree tree =  getSteinerTree(connGraph, doms, doms.get(0), doms.size());
//			System.out.println();
//		}
//		
//	}
//
	private DirectedGraph<Integer, Edge> getConnGraph(
			final List<Event> localEvents, double[][] simGraph) {
		DirectedGraph<Integer, Edge> connGraph = new DefaultDirectedGraph<Integer, Edge>(Edge.class);
		try {
			Edge ee = Edge.class.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i = 0; i < simGraph.length; i++)
			connGraph.addVertex(i);
		
		
		for(int i = 0; i < simGraph.length; i++) {
			long ti = localEvents.get(i).getEventDate();
			for(int j = 0; j < simGraph.length; j++) {
				if (i == j)
					continue;
				if (simGraph[i][j] <= 0.1) {
					continue;
				}
				
				long tj = localEvents.get(j).getEventDate();
				if (ti <= tj && ti > tj - 1000 * 3600 * 50) {
					connGraph.addEdge(i, j);
				}
			}
		}
		return connGraph;
	}
	
	protected static SteinerTree getSteinerTree(DirectedGraph<Integer, Edge> connGraph, List<Integer> doms, 
			int root, int k) {
//		List<Integer>[][] paths = shortestPaths(connGraph);
		return getSteinerTree(connGraph, doms, root, k, 1);
	}
	
	protected static SteinerTree getSteinerTree(DirectedGraph<Integer, Edge> connGraph, List<Integer> doms, 
			int root, int k, int si) {
		
		if (si == 1)
			return getBaseSteinerTree(connGraph, doms, root, k);
		int n = connGraph.vertexSet().size();
		SteinerTree tree = new SteinerTree(doms);
		while(k > 0) {
			SteinerTree besttree = new SteinerTree(doms);
			besttree.add(root);
			for(Edge e : connGraph.outgoingEdgesOf(root)) {
				int v = connGraph.getEdgeTarget(e);
				for(int kp = 1; kp <= k; kp++) {
					SteinerTree ctree = getSteinerTree(connGraph, doms, v, kp, si-1);
					if (ctree == null)
						continue;
					ctree.add(root);
					if (ctree.cost() < besttree.cost())
						besttree = ctree;
				}
			}
			if (besttree.cover() == 0) 
				return null;
			k -= besttree.cover();
			doms = new ArrayList<Integer>(doms);
			doms.removeAll(besttree.getDoms());
			
			tree.add(besttree);
		}
		
		return tree;
	}
	
	protected static SteinerTree getBaseSteinerTree(DirectedGraph<Integer, Edge> connGraph, List<Integer> doms, 
			final int root, int k) {
		new DijkstraShortestPath<Integer, Edge>(connGraph, root, 0);
		final BellmanFordShortestPath<Integer, Edge> shortestPath = new BellmanFordShortestPath<Integer, Edge>(connGraph, root);
		SteinerTree tree = new SteinerTree(doms);
		tree.add(root);
		List<Integer> targets = new ArrayList<Integer>(doms);
		targets.remove(new Integer(root));
		Collections.sort(targets, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				int l1 = 100000;
				int l2 = 100000;
				List<Edge> path = shortestPath.getPathEdgeList(o1);
				if (path != null)
					l1 = path.size();
				path = shortestPath.getPathEdgeList(o2);
				if (path != null)
					l2 = path.size();
				
				return l1 - l2;
			}
		});
		
		for(int i = 0; i < targets.size(); i++) {
			if (tree.cover() == k)
				break;
			int nodei = targets.get(i);
			if (shortestPath.getPathEdgeList(nodei) == null)
				break;
			for(Edge e : shortestPath.getPathEdgeList(nodei)) {
				Integer t = connGraph.getEdgeTarget(e);
				Integer s = connGraph.getEdgeSource(e);
				tree.add(t);
			}
		}
		
		if (tree.cover() != k)
			return null;
		else
			return tree;
	}
	
	protected static List<Integer>[][] shortestPaths(DoubleMatrix connGraph) {
		int n = connGraph.rows;
		@SuppressWarnings("unchecked")
		List<Integer>[][] paths = new ArrayList[n][n];
		int[][] lengthOfPaths = new int[n][n];
		
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				if (connGraph.get(i, j) > 0.5) {
					lengthOfPaths[i][j] = 1;
					paths[i][j] = new ArrayList<Integer>();
				} else
					lengthOfPaths[i][j] = 100000;
			}
		}
		
		for(int k = 0; k < n; k++) {
			for(int i = 0; i < n; i++) {
				for(int j = 0; j < n; j++) {
					if (i == j)
						continue;
					int s = lengthOfPaths[i][k] + lengthOfPaths[k][j];
					if (s < lengthOfPaths[i][j]) {
						lengthOfPaths[i][j] = s;
						paths[i][j] = new ArrayList<Integer>();
						paths[i][j].addAll(paths[i][k]);
						paths[i][j].add(k);
						paths[i][j].addAll(paths[k][j]);
					}
				}
			}
		}
		
		return paths;
	}
	

	int k = 12;
	
	
}
