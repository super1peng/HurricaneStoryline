package fiu.kdrg.storyline;

import java.util.*;

import org.jblas.DoubleMatrix;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import fiu.kdrg.storyline.event.*;
import fiu.kdrg.util.Util;

public class SimpleStorylineGenSteinerTree extends StorylineGenBaseline {

	public SimpleStorylineGenSteinerTree() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * main entry for storyline
	 */
	public static void main(String[] args) throws Exception {
		StorylineGenSteinerTree storyline = new StorylineGenSteinerTree();
		storyline.loadEvents("./sandy_all_clean_nodup_events_latlng.txt", 
				dateFormat.parse("2012-10-24"), dateFormat.parse("2012-11-06"), 
				"sandy|hurricane|storm|disaster");
		List<LatLng> locations = new ArrayList<LatLng>();
		for(Event event : storyline.events) {
			locations.add(event.getLatlng());
		}
		
		
		
		KMeansClusteringJava clustering = new KMeansClusteringJava();
		clustering.locations = locations;
		Members[] clusters = clustering.cluster(storyline.k);
//		Members[] clusters = clustering.clusteringWithSeedsAndRanges(getSeedsWithRanges());
//		Members[] clusters = clustering.clusteringUsingDomset(storyline.events);
				
		CompositeStorylineGenSteinerTree compositeTree = new CompositeStorylineGenSteinerTree();
		compositeTree.events = storyline.events;//添加全局的事件
		
		
		String path = Util.rootDir + "events/totalEvents.out";
		SerializeFactory.serialize(path, (ArrayList<Event>)storyline.events);
		ArrayList<Event> allStoryEvents = new ArrayList<Event>();
		
		for(int clusterID = 0; clusterID < clusters.length; clusterID++) {
			Members members = clusters[clusterID];
//			System.out.println("memeber size: " + members.size());
			if (members.size() > 1000) {
				continue;
			}
			final List<Event> localEvents = new ArrayList<Event>();
			//localEvents中的序列i与原来events中的序列i是不一样的
			for(Integer i : members) {
				storyline.events.get(i).setClusterId(clusterID);//set clusterID
				localEvents.add(storyline.events.get(i));
			}
			
			System.err.println(localEvents.size());
			//用于测试一个cluster中距离最远两个事件的测试
			printGeoDiff(localEvents);
			
			SimpleStorylineGenSteinerTree sss = new SimpleStorylineGenSteinerTree();
			sss.events = localEvents;
			allStoryEvents.addAll(localEvents);
			
			//serialize local events and show on map 
			String path1 = Util.rootDir + "events/storyline.out" + clusterID;
			SerializeFactory.serialize(path1, (ArrayList<Event>)sss.events);
			
			
			sss.genSimGraph();
			sss.getDomSet(10);//最多10个点，其中的dom(List<Integer>)中的序列相对于local来说
			sss.setEventWeights(sss.events, sss.simGraph);
			sss.getSteinerTree(4);
			
			compositeTree.addSimpleStoryline(sss);		
			System.out.println(sss.localSteinerTreeInfo.cost());
		}
		
//		storyline.genStoryline();
		String path2 = Util.rootDir + "events/allStorylineEvents.out";
		SerializeFactory.serialize(path2, allStoryEvents);
		compositeTree.integrateLocalTrees();
		compositeTree.genStoryline();

	}
	
	@Override
	public void genStoryline() {

		
	}

	DirectedGraph<Integer, Edge> connGraph = null;
	private void genConnGraph() {
		connGraph = new DefaultDirectedGraph<Integer, Edge>(Edge.class);
//		try {
//			Edge ee = Edge.class.newInstance();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		for(int i = 0; i < simGraph.length; i++)
			connGraph.addVertex(i);
		
		
		for(int i = 0; i < simGraph.length; i++) {
			long ti = events.get(i).getEventDate();
			for(int j = 0; j < simGraph.length; j++) {
				if (i == j)
					continue;
				if (simGraph[i][j] <= 0.1) {
					continue;
				}
				
				//相差50个小时，相似度大于0.1
				long tj = events.get(j).getEventDate();
				if (ti <= tj && ti > tj - 1000 * 3600 * 50) {
					connGraph.addEdge(i, j);
				}
			}
		}
	}
	
	List<Map<Integer, DijkstraShortestPath<Integer, Edge>>> shortestPathsFromAllNodesToDoms;
	SteinerTree localSteinerTreeInfo = null;
	//这个图所对应的序列号还是局部的
	
	protected void getSteinerTree(int approx) {
		Collections.sort(doms, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return (int)(events.get(o1).getEventDate() - events.get(o2).getEventDate()); 
			}
		});
		
//		for(Integer di : sss.dom) {
//			Event dom = localEvents.get(di);
//			System.out.println(String.format("%s, %s\n%s", 
//					dateFormat.format(new Date(dom.getEventDate())), 
//					dom.getEventLocation(),
//					dom.getEventContent()));
//		}
		
		genConnGraph();
		shortestPathsFromAllNodesToDoms = new ArrayList<Map<Integer, DijkstraShortestPath<Integer, Edge>>>();
		for(int i = 0; i < simGraph.length; i++) {
			Map<Integer, DijkstraShortestPath<Integer, Edge>> paths = new HashMap<Integer, DijkstraShortestPath<Integer, Edge>>(); 
			for(int j = 0; j < doms.size(); j++) {
				DijkstraShortestPath<Integer, Edge> path = new DijkstraShortestPath<Integer, Edge>(connGraph, i, doms.get(j));
				path.getPath();
				paths.put(doms.get(j), path);
			}
			shortestPathsFromAllNodesToDoms.add(paths);
		}
		
		int kk = doms.size();
		//doms.get(0),最早的dom元素做为root
		//为了使图形的SteinerTree信息保存下来，在SteinerTree类里面加入图信息（树？）
		SteinerTree tree = getSteinerTree(doms, doms.get(0), kk--, approx);
		while(tree == null) {
			tree = getSteinerTree(doms, doms.get(0), kk--, approx);
		}
		localSteinerTreeInfo = tree;
	}
	
	protected SteinerTree getSteinerTree(List<Integer> doms, 
			int root, int k, int si) {
		if (si == 1)
			return getBaseSteinerTree(doms, root, k);
		int n = connGraph.vertexSet().size();
		SteinerTree tree = new SteinerTree(doms);
		while(k > 0) {
			SteinerTree besttree = new SteinerTree(doms);
			besttree.add(root);
			for(Edge e : connGraph.outgoingEdgesOf(root)) {
				int v = connGraph.getEdgeTarget(e);
				for(int kp = 1; kp <= k; kp++) {
					SteinerTree ctree = getSteinerTree(doms, v, kp, si-1);
					if (ctree == null)
						continue;
					ctree.add(root);//这个地方应该加条root到v的边
					ctree.graphSteinerTreeInfo.addEdge(root, v);
					if (ctree.cost() < besttree.cost())//cost有问题
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
	
	protected SteinerTree getBaseSteinerTree(List<Integer> doms, final int root, int k) {
		final Map<Integer, DijkstraShortestPath<Integer, Edge>> paths = this.shortestPathsFromAllNodesToDoms.get(root);
		
		SteinerTree tree = new SteinerTree(doms);
		tree.add(root);
		List<Integer> targets = new ArrayList<Integer>(doms);
		targets.remove(new Integer(root));
		Collections.sort(targets, new Comparator<Integer>() {

			@Override
			//从root到dom的最短路径排序。
			public int compare(Integer o1, Integer o2) {
				int l1 = 100000;
				int l2 = 100000;
				List<Edge> path = paths.get(o1).getPathEdgeList(); //shortestPath.getPathEdgeList(o1);
				if (path != null)
					l1 = path.size();
				path = paths.get(o2).getPathEdgeList();
				if (path != null)
					l2 = path.size();
				
				return l1 - l2;
			}
		});
		
		//这不一定是最优的最短路径集合。在这个地方可构造出对应的steinerTree（详细的图，而不是只有点和cover的点信息）。
		for(int i = 0; i < targets.size(); i++) {
			if (tree.cover() == k)
				break;
			int nodei = targets.get(i);
			if (paths.get(nodei).getPathEdgeList() == null)
				break;
			for(Edge e : paths.get(nodei).getPathEdgeList()) {//最短路径生成的不一定是颗树？？？
				Integer target = connGraph.getEdgeTarget(e);
				Integer source = connGraph.getEdgeSource(e);
				tree.add(target);
				tree.getGraphSteinerTreeInfo().addVertex(source);
				tree.getGraphSteinerTreeInfo().addVertex(target);
				tree.getGraphSteinerTreeInfo().addEdge(source, target);
				
			}
		}
		
		if (tree.cover() != k)
			return null;
		else
			return tree;
	}
	

	int k = 12;
	
	public void setEventWeights(
			List<Event> localEvents, double[][] simGraph){
		
		int n = simGraph.length;
		DoubleMatrix simGraphMatrix = new DoubleMatrix(simGraph);
		DoubleMatrix addMatrix = DoubleMatrix.ones(1,n);
		DoubleMatrix weights = DoubleMatrix.zeros(1, n);
		weights = addMatrix.mmul(simGraphMatrix);
		
		for(int i=0;i<n;i++){
			localEvents.get(i).setWeight(weights.get(i));
		}
	}
	
	
	public static void printGeoDiff(List<Event> cluster){
		
		//测试一个cluster中地理位置的差异情况
		List<Event> forTest = cluster;
		Collections.sort(forTest, new Comparator<Event>() {
			@Override
			public int compare(Event o1, Event o2) {
				// TODO Auto-generated method stub
				int locSum1 = (int) Math.abs(o1.getLatlng().getLatitude()) 
								+ (int) Math.abs(o1.getLatlng().getLongtitude()); 
				int locSum2 = (int) Math.abs(o2.getLatlng().getLatitude()) 
						+ (int) Math.abs(o2.getLatlng().getLongtitude());
				return locSum1 - locSum2;
			}
		});
		if(!cluster.isEmpty()){
		System.out.println(String.format("Most South: %f,%f", forTest.get(0).getLatlng().getLatitude(),
								         					forTest.get(0).getLatlng().getLongtitude()));
		System.out.println(String.format("Most North: %f,%f", forTest.get(forTest.size() - 1).getLatlng().getLatitude(),
															forTest.get(forTest.size() - 1).getLatlng().getLongtitude()));
		}
		
	}
	
	
	
	public static Map<LatLng, Double> getSeedsWithRanges(){
		Map<LatLng, Double> seeds = new HashMap<LatLng, Double>();
		seeds.put(new LatLng(18.52f, -75.06f), new Double(4));// between Jamaica and Haiti
		seeds.put(new LatLng(23.24f, -79.23f), new Double(4)); //Between Havana and The Bahamas
		seeds.put(new LatLng(26.54f, -80.6f), new Double(4)); // around miami
		seeds.put(new LatLng(29.38f, -81.5f), new Double(4)); // around jacksonville
		seeds.put(new LatLng(32.2f, -80.6f), new Double(4));  // between Georige and South Carolina
		seeds.put(new LatLng(35.2f, -77.48f), new Double(4)); // around North Carolina
		seeds.put(new LatLng(38.58f, -76.94f), new Double(4)); // around DC
		seeds.put(new LatLng(41.31f, -73.65f), new Double(4)); // around NY
		seeds.put(new LatLng(40.38f, -79.23f), new Double(4)); // around Columbus
		return seeds;
	}
	
	
	
}
