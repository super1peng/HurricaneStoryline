package fiu.kdrg.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jgrapht.graph.DefaultDirectedGraph;

import fiu.kdrg.storyline.Edge;


public class Test2 {
	


	public static void main(String[] args) {
		
		List<Integer> order = new ArrayList<Integer>();
		order.add(3);
		order.add(5);
		
		Collections.sort(order, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				// TODO Auto-generated method stub
				return -(o1 - o2);
			}
		});
		
		System.out.println(order.get(0));
		
		
		DefaultDirectedGraph<Integer, Edge> graph = new DefaultDirectedGraph<Integer, Edge>(Edge.class);
		graph.addVertex(1);
		graph.addVertex(3);
		System.out.println(graph.edgeSet().size());
		graph.addEdge(1, 3);
		System.out.println(graph.edgeSet().size());
		graph.addEdge(1, 3);
		System.out.println(graph.edgeSet().size());
		
	}
	
}
