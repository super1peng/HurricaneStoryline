package fiu.kdrg.storyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.Util;

public class CompositeStorylineGenSteinerTree extends
		SimpleStorylineGenSteinerTree {

	List<SimpleStorylineGenSteinerTree> simpleStorylines = null;
	Map<Integer, BellmanFordShortestPath<Integer, Edge>> allShortestPath;
	List<DefaultDirectedGraph<Integer, Edge>> localGraphTrees = new ArrayList<DefaultDirectedGraph<Integer, Edge>>();
	DefaultDirectedGraph<Integer, Edge> globalTree = new DefaultDirectedGraph<Integer, Edge>(
			Edge.class);

	public CompositeStorylineGenSteinerTree() {
		simpleStorylines = new ArrayList<SimpleStorylineGenSteinerTree>();
	}
	
	
	

	@Override
	public void genStoryline() {
		// TODO Auto-generated method stub
		
		
		Set<Integer> initialX = new HashSet<Integer>();
		RGResult finalResult = new RGResult(null, 0);
		RGResult tmpResult = new RGResult(null, 0);
		
//		double allW = objectFunction(new HashSet<Integer>(vertices), initialX);
//		System.err.println("Test object function: " + allW);
//		recursiveGreedyAlgorithm(1007, 1567, 12, initialX, 5);//for Test
		
		
		allShortestPath = new HashMap<Integer, BellmanFordShortestPath<Integer, Edge>>();
		for(Integer vertex : globalTree.vertexSet()){
			BellmanFordShortestPath<Integer, Edge> shortestPath = 
					new BellmanFordShortestPath<Integer, Edge>(globalTree, vertex);
			allShortestPath.put(vertex, shortestPath);
			
		}
		
		List<Integer> orderedNodes = new ArrayList<Integer>(globalTree.vertexSet());
		Collections.sort(orderedNodes, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				// TODO Auto-generated method stub
				return (int) (events.get(o1).getEventDate() - events.get(o2).getEventDate());
			}
		});
		
		
		//取前五个，与后五个做粗略的比较
		int len = orderedNodes.size();
		for(int i = 0; i < 5; i++){
			Integer vS = orderedNodes.get(i);
			for(int j = len - 1; j >= len-10; j--){ 
				Integer vT = orderedNodes.get(j);
				if(vS != vT){
					System.err.println("source: " + vS + " target: " + vT );
					tmpResult = recursiveGreedyAlgorithm(vS, vT, 16, initialX, 2);
					if(tmpResult.getObjectVal() > finalResult.getObjectVal()){
						finalResult = tmpResult;
					}
				}
			}
		}
		
//		for(Integer vS : globalTree.vertexSet()){
//			for(Integer vT : globalTree.vertexSet()){
//				if(vS != vT){
//					System.err.println("source: " + vS + " target: " + vT );
//					tmpResult = recursiveGreedyAlgorithm(vS, vT, 12, initialX, 2);
//					if(tmpResult.getObjectVal() > finalResult.getObjectVal()){
//						finalResult = tmpResult;
//					}
//				}
//			}
//		}
		
		System.out.println("Max Object Value " + finalResult.getObjectVal());
		System.err.println("DONE!");
		ArrayList<Event> serilizableSSS = new ArrayList<Event>();
		
		for(Integer vertex : finalResult.getPath()){
			Event dom = events.get(vertex);
			serilizableSSS.add(dom);
			System.out.println(String.format("event[%d]:latitude:%f,longtitude:%f\n%s, %s\n%s",vertex,
					dom.getLatlng().getLatitude(),dom.getLatlng().getLongtitude(),
					dateFormat.format(new Date(dom.getEventDate())), 
					dom.getEventLocation(),
					dom.getEventContent()));
		}
		
		System.out.println("Storyline Serilization");
		try {
			SerializeFactory.serialize(Util.rootDir + "/events/storyline.out", serilizableSSS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void integrateLocalTrees() {

		int count = 0;
		for (SimpleStorylineGenSteinerTree localTreeInfo : simpleStorylines) {
			SteinerTree tmpSteinerTree = localTreeInfo.localSteinerTreeInfo;
			System.out.println("localGraphTrees " + count++);
//			System.out.println("#nodes: " + tmpSteinerTree.nodes.size());
			System.out.println("#covered: " + tmpSteinerTree.cover);
			System.out.println("#vertices: " + tmpSteinerTree.graphSteinerTreeInfo.vertexSet().size());
			System.out.println("#edges: " + tmpSteinerTree.graphSteinerTreeInfo.edgeSet().size());
			localGraphTrees.add(recoverGraphTree(localTreeInfo));
		}

		for (int i = 0; i < localGraphTrees.size(); i++) {
			DefaultDirectedGraph<Integer, Edge> tmpSteinerGraphTree = localGraphTrees.get(i);
			for (Integer vertex : tmpSteinerGraphTree.vertexSet()) {
				globalTree.addVertex(vertex);
				for (Edge edge : tmpSteinerGraphTree.outgoingEdgesOf(vertex)) {
					Integer target = tmpSteinerGraphTree.getEdgeTarget(edge);
					globalTree.addVertex(target);
					globalTree.addEdge(vertex, target);
				}
			}
		}

		System.err.println("Group Done!");
		System.out.println("#vertices: " + globalTree.vertexSet().size());
		System.out.println("#edges: " + globalTree.edgeSet().size());

		double[][] geoDist = genGeoConnGraph(events, 3);
		double[][] tempDist = genTempoConnGraph(events, 1000 * 3600 * 24);
		for (int i = 0; i < localGraphTrees.size(); i++) {// 连接不同cluster里的点，只连接baseline上的点。
			for (Integer source : localGraphTrees.get(i).vertexSet()) {
				for (int j = i + 1; j < localGraphTrees.size(); j++) {
					for (Integer target : localGraphTrees.get(j).vertexSet()) {
						if (geoDist[source][target] == 1) {// 地理位置是相邻的
							if (tempDist[source][target] == 1) {
								globalTree.addEdge(source, target);
							} else if (tempDist[source][target] == -1) {
								globalTree.addEdge(target, source);
							}
						}
					}
				}
			}

		}

		System.err.println("cluster connection Done!");
		System.out.println("#vertices: " + globalTree.vertexSet().size());
		System.out.println("#edges: " + globalTree.edgeSet().size());
	}

	// 把事件序列号同步回全局事件号
	private DefaultDirectedGraph<Integer, Edge> recoverGraphTree(
			SimpleStorylineGenSteinerTree tree) {
		DefaultDirectedGraph<Integer, Edge> returnGraphTree = new DefaultDirectedGraph<Integer, Edge>(
				Edge.class);
		List<Event> localEvents = tree.events;
		DefaultDirectedGraph<Integer, Edge> localSteinerGraphTree = tree.localSteinerTreeInfo.graphSteinerTreeInfo;

		for (Integer vertex : localSteinerGraphTree.vertexSet()) {
			Integer source = events.indexOf(localEvents.get(vertex));

			returnGraphTree.addVertex(source);
			for (Edge edge : localSteinerGraphTree.outgoingEdgesOf(vertex)) {
				Integer target = localSteinerGraphTree.getEdgeTarget(edge);// 局部事件的序列号
				target = events.indexOf(localEvents.get(target));// 转化成全局事件的序列号
				returnGraphTree.addVertex(target);
				returnGraphTree.addEdge(source, target);
			}

		}	
		return returnGraphTree;
	}

	// ================================================================================
	private RGResult recursiveGreedyAlgorithm(Integer s, Integer t, int B,
			Set<Integer> X, int k) {

		// BellmanFordShortestPath<Integer, Edge> shortestPath =
		// new BellmanFordShortestPath<Integer, Edge>(steinerGraph, s);
		// System.out.println("@ source: " + s + " target: " + t);
		List<Edge> tmpPath = allShortestPath.get(s).getPathEdgeList(t);// shortestPath.getPathEdgeList(t);
		if (tmpPath == null || tmpPath.size() > B)
			return new RGResult(null, 0);

		if (k == 0) {
			return new RGResult(translatePath(tmpPath), objectFunction(
					new HashSet<Integer>(translatePath(tmpPath)), X));
		}

		List<Integer> P = translatePath(tmpPath);
		double objectValue = objectFunction(new HashSet<Integer>(P), X);

		for (Integer vertex : globalTree.vertexSet()) {
			if (!vertex.equals(s) && !vertex.equals(t)) {
				for (int B1 = 1; B1 <= B; B1++) {

					List<Integer> P1 = recursiveGreedyAlgorithm(s, vertex, B1,
							X, k - 1).getPath();
					if (P1 == null)
						continue;

					Set<Integer> unionX = new HashSet<Integer>(X);
					unionX.addAll(P1);

					List<Integer> P2 = recursiveGreedyAlgorithm(vertex, t,
							B - B1, unionX, k - 1).getPath();
					if (P2 == null)
						continue;

					P1.remove(P1.size() - 1);
					P1.addAll(P2);
					double tmpValue = objectFunction(new HashSet<Integer>(P1),
							X);
					if (tmpValue > objectValue) {
						P = P1;
						objectValue = tmpValue;
					}
				}
			}
		}
		return new RGResult(P, objectValue);
	}

	private double objectFunction(Set<Integer> vertices, Set<Integer> X) {
		vertices.addAll(X);
		return defFunction(vertices, 1.0) - defFunction(X, 1.0);
	}

	// some mistakes here
	private double defFunction(Set<Integer> vertices, double alpha) {
		Set<Integer> numOfCluster = new HashSet<Integer>();
		double weights = 0;

		for (Integer vertex : vertices) {
			numOfCluster.add(events.get(vertex).getClusterId());
			weights += events.get(vertex).getWeight();
		}
		return weights += alpha * numOfCluster.size();
	}

	//把一条path转化成点
	private List<Integer> translatePath(List<Edge> path) {
		List<Integer> vertices = new ArrayList<Integer>();
		for (int i = 0; i < path.size(); i++) {
			if (i == 0) {
				vertices.add(globalTree.getEdgeSource(path.get(i)));
			}
			vertices.add(globalTree.getEdgeTarget(path.get(i)));
		}

		return vertices;
	}
	
	public void addSimpleStoryline(SimpleStorylineGenSteinerTree tree){
		simpleStorylines.add(tree);
	}

}

class RGResult {
	List<Integer> path;
	double objectVal;

	public RGResult(List<Integer> path, double objectVal) {
		// TODO Auto-generated constructor stub
		setObjectVal(objectVal);
		if(path == null)
			path = new ArrayList<Integer>();
		else
			setPath(path);
	}

	public List<Integer> getPath() {
		return path;
	}

	public void setPath(List<Integer> path) {
		this.path = path;
	}

	public double getObjectVal() {
		return objectVal;
	}

	public void setObjectVal(double objectVal) {
		this.objectVal = objectVal;
	}

}
