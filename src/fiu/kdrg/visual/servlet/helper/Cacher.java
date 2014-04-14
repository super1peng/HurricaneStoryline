package fiu.kdrg.visual.servlet.helper;

import fiu.kdrg.storyline2.LocalSteinerTreeGenerator;
import graphTheory.algorithms.steinerProblems.steinerArborescenceApproximation.ShPAlgorithm;
import graphTheory.algorithms.steinerProblems.steinerArborescenceApproximation.SteinerArborescenceApproximationAlgorithm;
import graphTheory.graph.Arc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cacher {

	private static Logger logger = LoggerFactory.getLogger(Cacher.class);
	
	private static Map<String, Set<Arc>> modelCacher;

	static {
		modelCacher = new HashMap<String, Set<Arc>>();
	}

	
	
	public static Set<Arc> query(int dID, int eID) {

		if (!modelCacher.containsKey(generateKey(dID, eID))) {
			
			LocalSteinerTreeGenerator stg = new LocalSteinerTreeGenerator(dID);
			SteinerArborescenceApproximationAlgorithm alg = stg.compute(eID);
			if(alg != null){
				modelCacher.put(generateKey(dID, eID), alg.getArborescence());
			}
			else{
				logger.info(String.format("Something with disaster %d and event %d.", dID, eID));
			}
		}
		return modelCacher.get(generateKey(dID, eID));
	}

	
	private static String generateKey(int dID, int eID) {
		return dID + "_" + eID;
	}
	
	
	public static int size(){
		return modelCacher.size();
	}

}

