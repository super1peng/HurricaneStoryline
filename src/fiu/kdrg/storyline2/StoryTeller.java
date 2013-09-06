package fiu.kdrg.storyline2;

import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.EventUtil;
import fiu.kdrg.util.Util;

public class StoryTeller {

	
	List<Event> events;//sorted by events' date
	double[][] simGraph;
	int[][][] locConstraints;
	int maxEdge = 0;
	List<Event> storyline;
	
	public StoryTeller(List<Event> events) {
		// TODO Auto-generated constructor stub
		this.events = EventUtil.sortEventByDate((ArrayList<Event>) events);
		maxEdge = 20;//default val
		storyline = null;
	}
	
	
	
	protected void computeLineConstraints(){
		
		int n = events.size();
		locConstraints = new int[n][n][n];
		
		for(int i = 0; i < n; i++){
			for(int j = i + 1; j < n; j++){
				for(int k = j + 1; k < n; k++){
					if(!StoryUtil.hasSharpAngle(events.get(i), events.get(j), events.get(k)))
						locConstraints[i][j][k] = 1;
				}
			}
		}
		
	}
	
	
	
	public void ilp() throws IloException{
		
		computeLineConstraints();
		simGraph = StoryUtil.computeSimilarity(events);
		
		int node_n = events.size();
		int nextNode_n = node_n * node_n;
		
		IloCplex cplex = new IloCplex();
		
		// variables
		IloIntVar[] nodeActiveVars = cplex.intVarArray(node_n, 0, 1);
		IloIntVar[] nextNodeActiveVars = cplex.intVarArray(nextNode_n, 0, 1);
		IloNumVar minedge = cplex.numVar(Double.MIN_VALUE, Double.MAX_VALUE);

		
		//constraint 1 , active most maxEdge nodes
		IloIntExpr t = cplex.intExpr();
		for(int i = 0; i < node_n; i++){
			t = cplex.sum(t, nodeActiveVars[i]);
		}
		cplex.addEq(t, maxEdge);
		
		
		//constraint 2, active most maxEdge-1 edges(next_node)
		IloIntExpr maxEdgeCt = cplex.intExpr(); // most value maxEdge-1
		for(int i = 0; i < node_n; i++){
			for(int j = 0; j < node_n; j++){
				if(i != j){
					maxEdgeCt = cplex.sum(maxEdgeCt, nextNodeActiveVars[i*node_n + j]);
				}
			}
		}
		cplex.addEq(maxEdgeCt, maxEdge-1);
		
		
		//nodes have one in-edge and one out-edge
		// without specifying start and ends, we change 
		//equation constraints to unequal constraints
		for(int i = 0; i < node_n; i++){
			IloIntExpr nodeOutEdge = cplex.intExpr();
			IloIntExpr nodeInEdge = cplex.intExpr();
			
			for(int j = 0; j < node_n; j++){
				if(i != j){
					nodeOutEdge = cplex.sum(nodeOutEdge, nextNodeActiveVars[i*node_n + j]);
					nodeInEdge = cplex.sum(nodeInEdge, nextNodeActiveVars[j*node_n + i]);
				}
			}
			//sum{next_node_{i,j}} <= node_active_{i}
			cplex.addLe(nodeOutEdge, nodeActiveVars[i]); 
			cplex.addLe(nodeInEdge, nodeActiveVars[i]);
		}
		
		
		// the chain is ordered chronologically
		for(int i = 0; i < node_n; i ++){
			for(int j = 0; j <=  i; j++){
				cplex.addEq(nextNodeActiveVars[i*node_n + j], 0);
			}
		}
		
		
		
		// a transition can not be active if a middle document is
		for(int i = 0; i < node_n; i++){
			for(int j = i+2; j < node_n; j++){
				for(int k = i+1; k < j; k++){
					cplex.addLe(cplex.sum(nextNodeActiveVars[i*node_n + j], 
										  nodeActiveVars[k]), 1);
				}
			}
		}

		
		// next-node_{i,j} and next-node_{j,k} can not simultaneously 
		// active if locConstraints[i][j][k] = 0
		for(int i = 0; i < node_n; i++){
			for(int k = node_n-1; k >= i + 2; k --){
				for(int j = i+1; j < k; j++){
					cplex.addLe(cplex.sum(nextNodeActiveVars[i*node_n + j], 
										  nextNodeActiveVars[j*node_n + k]), 
										  1 + locConstraints[i][j][k]);
				}
			}
		}
		
		
		//constraint on minedge
		for(int i = 0; i < node_n; i++){
			for(int j = 0; j < node_n; j++){
//				IloNumExpr min = cplex.sum(minedge,
//						  cplex.prod(1-simGraph[i][j], nextNodeActiveVars[i*node_n + j]));
				cplex.addLe(cplex.sum(minedge,
						  cplex.prod(1-simGraph[i][j], nextNodeActiveVars[i*node_n + j])), 1);
			}
		}
		
		
		cplex.addMaximize(minedge);
		cplex.setParam(IloCplex.DoubleParam.EpGap, 0.005);
		System.err.println(cplex.solve());
		System.err.println(cplex.getObjValue());
		
//		double[] nodeActives = cplex.getValues(nodeActiveVars);
		double[] edgeActives = cplex.getValues(nextNodeActiveVars);
		analyzeSol(edgeActives);
		
	}
	
	
	
	private void analyzeSol(double[] edgeActives){
		
		int n = (int)Math.sqrt(edgeActives.length);
		int start = 0, end = 0;
		
		//find start and end. 
		int[][] path = new int[n][n];
		int[] edgeIn = new int[n]; 
		int[] edgeOut = new int[n];
		int row = 0,col = 0;
		
		for(int i = 0; i < edgeActives.length; i++){
			if(edgeActives[i] == 1){
				row = (int) (i / n);
				col = i % n;
				if(row != col){
					path[row][col] = 1;
					edgeOut[row] = 1;
					edgeIn[col] = 1;
				}
			}
		}
		
		for(int i = 0; i < n; i++){
			if(edgeOut[i] == 1 && edgeIn[i] != 1){
				start = i;
			}else if(edgeIn[i] == 1 && edgeOut[i] != 1){
				end = i;
			}
		}
		
		int current = start;
		int next = 0;
		storyline = new ArrayList<Event>();
		events.get(start).setMainEvent(true);
		storyline.add(events.get(start));
		
		while(current != end){
			
			for(next = 0; next < n; next++){
				if(path[current][next] == 1){
					events.get(next).setMainEvent(true);
					storyline.add(events.get(next));
					current = next;
					break;
				}
			}
			
			if(next == n){          // find no next, something wrong
				System.err.println("something must be wrong!");
				System.exit(0);
			}
		}
		
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		ArrayList<Event> filterEvents = null;
		try {
			filterEvents = (ArrayList<Event>) SerializeFactory.deSerialize(Util.rootDir + "filterEvents.out");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		StoryTeller storyTeller = new StoryTeller(filterEvents);
		storyTeller.setMaxEdge(20);
//		EventUtil.displayEvents(storyTeller.events);
//		storyTeller.simGraph = StoryUtil.computeSimilarity(storyTeller.events);
//		for(int i = 0; i < 100; i++){
//			for(int j = 0; j < 100; j++){
//				System.out.println(storyTeller.simGraph[i][j]);
//			}
//		}
//		storyTeller.computeLineConstraints();
//		for(int i = 0; i < 100; i++){
//			for(int j = 0; j < 100; j++){
//				for(int k = 0; k < 100; k ++){
//					if(storyTeller.locConstraints[i][j][k] != 0)
//						System.out.println(storyTeller.locConstraints[i][j][k]);
//				}
//			}
//		}
		try {
			storyTeller.ilp();
			EventUtil.displayEvents(storyTeller.storyline);
			SerializeFactory.serialize(Util.rootDir + "finalResult.out", storyTeller.events);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public void setMaxEdge(int maxEdge) {
		this.maxEdge = maxEdge;
	}
	
	
}
