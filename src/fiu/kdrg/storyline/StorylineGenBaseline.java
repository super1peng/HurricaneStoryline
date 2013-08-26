package fiu.kdrg.storyline;

import java.util.*;
import java.util.Map.Entry;
import org.jblas.*;

import fiu.kdrg.storyline.event.Event;

public class StorylineGenBaseline extends StorylineGen {
	
	static public void main(String[] args) throws Exception {
		StorylineGen storyline = new StorylineGenBaseline();
		storyline.loadEvents("../sandy_all_clean_nodup_events_latlng.txt", 
				dateFormat.parse("2012-10-24"), dateFormat.parse("2012-11-06"), 
				"sandy|hurricane|storm|disaster");
		storyline.genStoryline();
	}
	
	
	List<Event> storyline;
	public List<Event> getStoryline() {
		if (storyline == null)
			genStoryline();
		return storyline;
	}
	
	@Override
	public void genStoryline() {
//		storyline = new ArrayList<Event>();
//		
//		List<List<Integer>> eventsOfDays = getEventsOfDays();
//		double[][] simGraph = StorylineGenBaseline.genSimGraph(events);
//		double[][] geoConnGraph = StorylineGenBaseline.genGeoConnGraph(events);
//		
//		DoubleMatrix gW = new DoubleMatrix(geoConnGraph);
//		DoubleMatrix sW = new DoubleMatrix(simGraph);
//		
//		Collections.sort(eventsOfDays, new Comparator<List<Integer>>() {
//
//			@Override
//			public int compare(List<Integer> o1, List<Integer> o2) {
//				return o2.size() - o1.size();
//			}
//		});
//		
//		DoubleMatrix uncovered = DoubleMatrix.ones(1,events.size());
//		
//		for(List<Integer> eventsOfDay : eventsOfDays) {
//			int[] ind = new int[eventsOfDay.size()];
//			for(int i = 0; i < ind.length; i++) {
//				ind[i] = eventsOfDay.get(i);
//			}
//			DoubleMatrix gWc = gW.get(ind, ind);
//			DoubleMatrix sWc = sW.get(ind, ind);
//			int i = gWc.mul(sWc).rowSums().argmax();
//			int sel = ind[i];
//			Event selevent = events.get(sel);
//			storyline.add(selevent);
//		}
//		
//		Collections.sort(storyline, new Comparator<Event>() {
//
//			@Override
//			public int compare(Event o1, Event o2) {
//				return (int)(o1.getEventDate() - o2.getEventDate());
//			}
//			
//		});
//		for(Event event : storyline) {
//			System.out.println(this.dateFormat.format(new Date(event.getEventDate())) + 
//					" " + event.getEventLocation() + " " + event.getEventContent());
//		}
	}




	protected List<List<Integer>> getEventsOfDays() {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2012, 9, 24);
		
		List<List<Integer>> eventsOfDays = new ArrayList<List<Integer>>();
		Date endDate = null;
		try {
			endDate = dateFormat.parse("2012-11-06");
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (cal.getTime().getTime() <= endDate.getTime()) {
			List<Integer> eventsOfDay = new ArrayList<Integer>();
			for(int i = 0; i < events.size(); i++) {
				Event event = events.get(i);
				if (event.getEventDate() == cal.getTime().getTime())
					eventsOfDay.add(i);
				
			}
			cal.add(Calendar.DAY_OF_MONTH, 1);
			eventsOfDays.add(eventsOfDay);
		}
		return eventsOfDays;
	}
	
	List<Integer> doms = null;
	Map<Integer, List<Integer>> clusters = null;
	
	protected void getDomSet(int k) {
		doms = new ArrayList<Integer>();
		clusters = new HashMap<Integer, List<Integer>>();
		
		int n = events.size();
		DoubleMatrix connGraph = DoubleMatrix.zeros(n, n);
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				if (simGraph[i][j] > 0.7){
					connGraph.put(i, j, 1);
//					System.out.println(1);
				}
			}
		}
		
		DoubleMatrix uncovered = DoubleMatrix.ones(1,n);
		int i = 0;
		while(i < k) {
			DoubleMatrix covering = DoubleMatrix.ones(1,n);
			covering.copy(uncovered);
			covering = covering.mmul(connGraph);
			int sel = covering.argmax();
			double maxdeg = covering.get(sel);
			
			if (maxdeg < 0.5)
				break;
			
			doms.add(sel);
			
			DoubleMatrix ind = connGraph.getRow(sel).ge(0.5);
			List<Integer> members = new ArrayList<Integer>();
			for(int rowN = 0; rowN < ind.rows; rowN++)
				for(int colN = 0; colN < ind.columns; colN ++){
					if(ind.get(rowN, colN) != 0)
						members.add(colN);
				}
			
			clusters.put(sel, members);
			
			int[] neighbors = ind.findIndices();
			uncovered = uncovered.put(0, ind, 0);
			i++;
		}
	}
	
	static private double[][] genGeoConnGraph(List<Event> events) {
		int n = events.size();
		double[][] connGraph = new double[n][n];
		for(int i = 0; i < events.size(); i++) {
			Event ea = events.get(i);
			connGraph[i][i] = 1;
			for(int j = i + 1; j < events.size(); j++) {
				Event eb = events.get(j);
				float latd = ea.getLatlng().getLatitude() - eb.getLatlng().getLatitude();
				float lngd = ea.getLatlng().getLongtitude() - eb.getLatlng().getLongtitude();
				double dist = Math.sqrt(latd * latd + lngd * lngd);
				if (dist < 3)
					connGraph[i][j] = 1;
			}
 		}
		
		return connGraph;
		
	}
	
	
	double[][] simGraph = null;
	protected void genSimGraph() {
		Map<String, Integer> idf = new HashMap<String, Integer>();
		List<Map<String, Double>> X = new ArrayList<Map<String, Double>>();
		simGraph = new double[events.size()][events.size()];
		for(Event event : events) {
			for(String ngram : new HashSet<String>(event.getNGramsOfContent())) {
				Integer df = idf.get(ngram);
				if (df == null)
					df = 0;
				idf.put(ngram, df + 1);//其实这个是Collection Frequency,不是document frequency
			}
		}
		
		System.err.println("idf done");
		
		for(Event event : events) {
			Map<String, Double> x = new HashMap<String, Double>();
			for(String ngram : event.getNGramsOfContent()) {
				Integer ngramidf = idf.get(ngram);
				if (ngramidf != null && ngram.equals(1))
					continue;
				
				Double tf = x.get(ngram);
				if (tf == null)
					tf = 0.0;
				x.put(ngram, tf + 1);
			}
			
			double norm = 0;
			
			for(Entry<String, Double> en : x.entrySet()) {
				String ngram = en.getKey();
				Integer ngramidf = idf.get(ngram);
				Double tf = en.getValue();
				tf *= Math.log((events.size() + 1.0) / ngramidf);
				en.setValue(tf);
				norm += tf * tf;//tf 即是tf-idf
			}
			norm = Math.sqrt(norm);
			for(Entry<String, Double> en : x.entrySet()) {
				en.setValue(en.getValue() / norm);
			}
			X.add(x);
		}
		
		System.err.println("vectorization done");
		long start = new Date().getTime();
		
		for(int i = 0; i < events.size(); i++) {
			simGraph[i][i] = 1;
			for(int j = i + 1; j < events.size(); j++) {
				simGraph[i][j] = simGraph[j][i] = innerProduct(X.get(i), X.get(j));
			}
		}
		System.err.println("sim graph done, " + (new Date().getTime() - start) / 1000);
	}
	
	static public double innerProduct(Map<String, Double> a, Map<String, Double> b) {
		double inner = 0;
		if (a.size() > b.size()) {
			Map<String, Double> c = a;
			a = b;
			b = c;
		}
		for(Entry<String, Double> en : a.entrySet()) {
			Double v = b.get(en.getKey());
			if (v != null)
				inner += en.getValue() * v;
		}
		return inner;
	}
	
	/**
	 * 自身不相连
	 * @param events
	 * @param maxDist
	 * @return
	 */
	static public double[][] genGeoConnGraph(List<Event> events, double maxDist) {
		int n = events.size();
		double[][] connGraph = new double[n][n];
		for(int i = 0; i < n; i++) {
			Event ea = events.get(i);
			connGraph[i][i] = 0;
			for(int j = i + 1; j < n; j++) {//经纬度范围计算按道理是有错误的，没有考虑到正负的变化，不过在此不产生问题
				Event eb = events.get(j);
				float latd = ea.getLatlng().getLatitude() - eb.getLatlng().getLatitude();
				float lngd = ea.getLatlng().getLongtitude() - eb.getLatlng().getLongtitude();
				double dist = Math.sqrt(latd * latd + lngd * lngd);
				if (dist < maxDist)
					connGraph[i][j] = 1;
					connGraph[j][i] = 1;
			}
 		}
		
		return connGraph;
		
	}
	
	
	/**
	 * 自身不相连
	 * @param events
	 * @param maxTimeGap
	 * @return
	 */
	static public double[][] genTempoConnGraph(List<Event> events, float maxTimeGap){
		int n = events.size();
		double[][] connGraph = new double[n][n];
		for(int i=0;i<events.size();i++){
			Event ea = events.get(i);
			connGraph[i][i] = 0;
			for(int j = i+1; j < events.size(); j++){
				Event eb = events.get(j);
				long tempDiff = ea.getEventDate() - eb.getEventDate();
				if(Math.abs(tempDiff) > maxTimeGap){
					connGraph[j][i] = 0;
					connGraph[i][j] = 0;
				}else if(tempDiff == 0){
					connGraph[j][i] = 1;
					connGraph[i][j] = 1;
				}
				else{
					connGraph[j][i] = 1 * Math.signum(tempDiff);
					connGraph[i][j] = -1 * Math.signum(tempDiff);
				}
			}
		}
		return connGraph;
	}
}
