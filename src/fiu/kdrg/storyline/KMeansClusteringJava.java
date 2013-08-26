package fiu.kdrg.storyline;

import java.util.*;

import org.apache.log4j.Logger;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.LatLng;
import fiu.kdrg.util.EventUtil;

class Members extends ArrayList<Integer> {
	
}

/**
 * 对于每个Members中的变量，其是一个ArrayList<Integer>.其中对应的Integer是原event中对应的事件序列号
 *
 */

public class KMeansClusteringJava {
	static private Logger log = Logger.getLogger(KMeansClusteringJava.class);
	
	Random rnd;
	
	public KMeansClusteringJava() {
		rnd = new Random();
		rnd.setSeed(11);
	}

	List<LatLng> locations = null;
	public Members[] cluster(int k) {
		
		List<Integer> rp = new ArrayList<Integer>();
		for(int i = 0; i < locations.size(); i++) 
			rp.add(i);
		Collections.shuffle(rp, rnd);
		
		List<LatLng> centers = new ArrayList<LatLng>();
		for(int i = 0; i < k; i++) {
			centers.add(locations.get(rp.get(i)));
		}
		
		int iterMax = 100;
		int iter = 0;
		int[] indicator = new int[locations.size()];
		Members[] members = null;
		while(iter < iterMax) {
			members = new Members[k];
			for(int i = 0; i < k; i++) {
				members[i] = new Members();
			}
			int[] indicatorsPrev = indicator.clone();
			for(int i = 0; i < indicator.length; i++) {
				int minCenter = 0;
				double minDist = Double.MAX_VALUE;
				for(int j = 0; j < centers.size(); j++) {
					LatLng loc1 = locations.get(i);
					LatLng loc2 = centers.get(j);
					double dist = dist(loc1, loc2); 
					if (dist < minDist) {
						minCenter = j;
						minDist = dist;
					}
				}
				indicator[i] = minCenter;
				members[minCenter].add(i);
			}
			
			if (Arrays.equals(indicator, indicatorsPrev)) {
				break;
			}
			
			centers.clear();
			for(int i = 0; i < k; i++) {
				double latsum = 0;
				double lngsum = 0;
				for(int j = 0; j < members[i].size(); j++) {
					latsum += locations.get(members[i].get(j)).getLatitude();
					lngsum += locations.get(members[i].get(j)).getLongtitude();
				}
				
				latsum /= members[i].size();
				lngsum /= members[i].size();
				
				centers.add(new LatLng((float)latsum, (float)lngsum));
				System.err.print(String.format("%.4f,%.4f ", latsum, lngsum));
			}
			System.err.println();
			
			iter++;
		}
		return members;

	}
	
	
	public Members[] clusteringWithSeedsAndRanges(Map<LatLng, Double> seeds){
		int seedsLen = seeds.size();
		Double range = 0d;
		Members[] members = new Members[seedsLen];
		for(int i = 0; i < seedsLen; i++)
			members[i] = new Members();
		List<LatLng> seedsLoc = new ArrayList<LatLng>(seeds.keySet());
		
		for(int i = 0; i < locations.size(); i++){
			LatLng latlng1 = locations.get(i);
			Double minDist = Double.MAX_VALUE;
			int indicator = -1;
			
			for(int j = 0; j < seedsLoc.size(); j++){
				LatLng latlng2 = seedsLoc.get(j);
				range = seeds.get(latlng2);
				double distance = dist(latlng1, latlng2);
				if(distance <= range){
					if(distance < minDist){
						minDist = distance;
						indicator = j;
					}
				}
			}// Done check for on location
		if(indicator != -1)	
			members[indicator].add(i); // add to members
			
		}
		
		return members;
	}
	
	
	
	public Members[] clusteringUsingDomset(List<Event> events){
		
		SimpleStorylineGenSteinerTree sss = new SimpleStorylineGenSteinerTree();
		sss.events = events;
		sss.genSimGraph();
		sss.getDomSet(10);
		int clusterLen = sss.doms.size();
		Members[] members = new Members[clusterLen];
		for(int i = 0; i < clusterLen; i++)
			members[i] = new Members();
		for(int i = 0; i < clusterLen; i++){
			members[i].add(sss.doms.get(i));						// add dominate node
//			List<Integer> mem = sss.clusters.get(sss.doms.get(i));
//			System.out.println(mem.size());
			members[i].addAll(sss.clusters.get(sss.doms.get(i)));   // add node cover by this dominate node
		}
		
		return members;
	}
	
	
	
	
	private double dist(LatLng loc1, LatLng loc2) {
		float d1 = loc2.getLatitude() - loc1.getLatitude();
		float d2 = loc2.getLongtitude() - loc1.getLongtitude();
		double dist = Math.sqrt(d1 * d1 + d2 * d2);
		return dist;
	}

}
