package fiu.kdrg.storyline2;

import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.EventRecognizer2DB;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.EventUtil;
import fiu.kdrg.util.IOUtil;
import fiu.kdrg.util.Util;

public class StoryTeller {

	
	List<Event> domEvents;//sorted by events' date
	double[][] simGraph;
	int[][][] locConstraints;
	int [][] distConstraints;
	int[] edgeRange = new int[2];
	double edgeW;
	double radius = 0;
	List<Event> storyline;
	
	StoryTeller() {
		// TODO Auto-generated constructor stub
		edgeRange[0] = 5;
		edgeRange[1] = 20; //default val
		radius = 3;
		edgeW = 0.001;
		storyline = null;
	}
	
	public StoryTeller(List<Event> events) {
		// TODO Auto-generated constructor stub
		this();
		this.domEvents = EventUtil.sortEventByDate((ArrayList<Event>) events);
	}
	
	
	
	protected void computeLineConstraints(){
		
		int n = domEvents.size();
		locConstraints = new int[n][n][n];
		
		for(int i = 0; i < n; i++){
			for(int j = i + 1; j < n; j++){
				for(int k = j + 1; k < n; k++){
					if(!StoryUtil.hasSharpAngle(domEvents.get(i), domEvents.get(j), domEvents.get(k)))
						locConstraints[i][j][k] = 1;
				}
			}
		}
		
	}
	
	
	
	protected void computeDistConstraints(double radius){
		computeDistConstraints(0, radius);
	}
	
	
	protected void computeDistConstraints(double min, double max) {
		
		int n = domEvents.size();
		distConstraints = new int[n][n];
		
		// note that distConstraints[i][i] = 1
		for(int i = 0; i < n; i++){
			for(int j = i; j < n; j++){
				//has distance less or equal than radius, then its value set to 1
				if(domEvents.get(i).hasRange(domEvents.get(j), min, max)){
					distConstraints[i][j] = 1;
					distConstraints[j][i] = 1;
				} else {
					distConstraints[i][j] = 0;
					distConstraints[j][i] = 0;
				}
			}
		}
		
	}
	
	
	
	public void ilp() throws IloException{
		
		computeLineConstraints();
//		computeDistConstraints(radius);
		computeDistConstraints(3, 10);
		simGraph = StoryUtil.computeSimilarity(domEvents);
		
		int node_n = domEvents.size();
		int nextNode_n = node_n * node_n;
		
		IloCplex cplex = new IloCplex();
		
		// variables
		IloIntVar[] nodeActiveVars = cplex.intVarArray(node_n, 0, 1);
		IloIntVar[] nextNodeActiveVars = cplex.intVarArray(nextNode_n, 0, 1);
		IloNumVar minedge = cplex.numVar(Double.MIN_VALUE, Double.MAX_VALUE);
		IloIntVar edgeNum = cplex.intVar(edgeRange[0], edgeRange[1]);
		

		
		//constraint 1 , active most maxEdge nodes
		IloIntExpr t = cplex.intExpr();
		for(int i = 0; i < node_n; i++){
			t = cplex.sum(t, nodeActiveVars[i]);
		}
		cplex.addEq(t, edgeNum);
		
		
		//constraint 2, active most maxEdge-1 edges(next_node)
		IloIntExpr maxEdgeCt = cplex.intExpr(); // most value maxEdge-1
		for(int i = 0; i < node_n; i++){
			for(int j = 0; j < node_n; j++){
				if(i != j){
					maxEdgeCt = cplex.sum(maxEdgeCt, nextNodeActiveVars[i*node_n + j]);
				}
			}
		}
		cplex.addEq(maxEdgeCt, cplex.sum(edgeNum, -1));
		
		
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
		
		
		//the chain can not have two nodes getting too close and too far
		// note that distConstraints[i][i] = 1
		for(int i = 0; i < node_n; i++){			
			IloIntExpr distConstraint = cplex.intExpr();
			for(int j = 0; j < node_n; j++){
//				distConstraint = cplex.sum(distConstraint,
//						cplex.prod(distConstraints[i][j], nodeActiveVars[j]));
				distConstraint = cplex.sum(distConstraint,
						cplex.prod(1 - distConstraints[i][j], nextNodeActiveVars[i*node_n + j]));
			}
			// if you want only and only if one neighbor choose, you can set this to "equal"
			cplex.addLe(distConstraint, 0.5);
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
		
		
//		cplex.addMaximize(minedge);
		cplex.addMaximize(cplex.sum(minedge, cplex.prod(edgeW, edgeNum)));
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
		domEvents.get(start).setMainEvent(true);
		storyline.add(domEvents.get(start));
		
		while(current != end){
			
			for(next = 0; next < n; next++){
				if(path[current][next] == 1){
					domEvents.get(next).setMainEvent(true);
					storyline.add(domEvents.get(next));
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
	
	
	public void genStoryline() throws IloException{
		this.ilp();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		int disaster_id = 2;
		DocFilter filter = null;
		ArrayList<Event> allEvents = null;
		
		switch (disaster_id) {
		case 1:
			allEvents = (ArrayList<Event>) EventLoader.loadEventByDisaster(disaster_id, "2005-08-16","2006-01-01");
			break;
		case 2:
			allEvents = (ArrayList<Event>) EventLoader.loadEventByDisaster(disaster_id, "2012-10-16","2014-01-01");
			break;
		case 3:
			allEvents = (ArrayList<Event>) EventLoader.loadEventByDisaster(disaster_id, "2011-08-09","2012-01-01");
			break;
		case 4:
			allEvents = (ArrayList<Event>) EventLoader.loadEventByDisaster(disaster_id, "2012-10-16","2014-01-01");
		default:
			break;
		}
//		DocFilter filter = new DocFilter(EventLoader.loadEventByDisaster(disaster_id, "2005-01-01","2006-01-01"));
//		DocFilter filter = new DocFilter(EventLoader.loadEventByDisaster(disaster_id, "2012-01-01","2014-01-01"));
//		DocFilter filter = new DocFilter(EventLoader.loadEventByDisaster(disaster_id, "2011-01-01","2012-01-01"));
		
		filter = new DocFilter(allEvents);
		filter.setMiniSim(0.5);
		filter.setMaxDist(4);
		ArrayList<Event> filteredEvents = filter.filter(120);
		
		System.out.println(filteredEvents.size());
		
		try {
			SerializeFactory.serialize(Util.rootDir + "allEvents"+disaster_id+".out", allEvents);
			SerializeFactory.serialize(Util.rootDir + "filterEvents"+disaster_id+".out", filteredEvents);
//			IOUtil.writeEventsToFile(filteredEvents, Util.rootDir + "filterEvents"+disaster_id+".txt");
//			EventRecognizer2DB.insertBatchEvent2DB(filteredEvents, disaster_id,"events2");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		StoryTeller storyTeller = new StoryTeller(filteredEvents);
		storyTeller.setEdgeRange(6, 20);
		storyTeller.setRadius(3);
		storyTeller.setEdgeW(0.002);
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
			storyTeller.genStoryline();
			EventUtil.displayEvents(storyTeller.storyline);
			SerializeFactory.serialize(Util.rootDir + "finalResult"+disaster_id+".out", storyTeller.domEvents);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public void setEdgeRange(int min,int max) {
		this.edgeRange[0] = min;
		this.edgeRange[1] = max;
	}



	public void setRadius(double radius) {
		this.radius = radius;
	}



	public void setEdgeW(double edgeW) {
		this.edgeW = edgeW;
	}



	public List<Event> getDomEvents() {
		return domEvents;
	}



	public void setDomEvents(List<Event> domEvents) {
		this.domEvents = domEvents;
	}
	
}
