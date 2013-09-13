package fiu.kdrg.storyline2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fiu.kdrg.nlp.StanfordLemmatizer;
import fiu.kdrg.storyline.StorylineGen;
import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.Util;

public class StoryUtil {

	
	
	public static StanfordLemmatizer lemmatizer;
	
	static{
		lemmatizer = StanfordLemmatizer.getInstance();
	}
	
	
	public static List<Event> loadAllEvents(){
		
		StorylineGen loader  = new StorylineGen();
		try {
			loader.loadEvents("./sandy_all_clean_nodup_events_latlng.txt", 
					loader.dateFormat.parse("2012-10-24"), 
					loader.dateFormat.parse("2012-11-06"), 
					"sandy|hurricane|storm|disaster");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			SerializeFactory.serialize(Util.rootDir + "allEvents.out", loader.getEvents());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return loader.getEvents();
	}
	
	
	
	/**
	 * give a lemmatized list<String>, converted it to nGram list.
	 * @param lemmatizedStr
	 * @param N
	 * @return
	 */
	public static List<String> getNGramOfContent(String document,
			int N) {
		
		List<String> lemmatizedStr = lemmatizer.lemmatize(document);
		List<String> ngrams = new ArrayList<String>();
		
		for (int n = 1; n < N; n++) {
			for (int i = 0; i < lemmatizedStr.size() - n + 1; i++) {
				String ngram = lemmatizedStr.get(i);
				for (int j = 1; j < n; j++) {
					ngram += " " + lemmatizedStr.get(i + j);
				}
				ngrams.add(ngram);
			}
		}
		return ngrams;
	}
	
	
	
	
	
	
	
	public static List<Map<String,Double>> 
		vectorizeEvents(List<Event> events){
		
		Map<String, Integer> idf = new HashMap<String, Integer>();
		List<Map<String, Double>> X = new ArrayList<Map<String, Double>>();
		for(Event event : events) {
			Set<String> checkDup = new HashSet<String>(); 
			for(String ngram : new HashSet<String>(
						getNGramOfContent(event.getEventContent(), 3))) {
				
				if(checkDup.contains(ngram))
					continue;
				else{
					checkDup.add(ngram);
					Integer df = idf.get(ngram);
					if (df == null)
						df = 0;
					idf.put(ngram, df + 1);//document frequency
				}
			}
		}
		
		System.err.println("idf done");
		
		for(Event event : events) {
			Map<String, Double> x = new HashMap<String, Double>();
			for(String ngram : getNGramOfContent(event.getEventContent(), 3)) {
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
		return X;
	}
	
	
	
	
	
	
	public static double[][] computeSimilarity(List<Event> events){
		
		double[][] simGraph = new double[events.size()][events.size()];
		List<Map<String, Double>> X = vectorizeEvents(events);
		
		long start = new Date().getTime();
		for(int i = 0; i < events.size(); i++) {
			simGraph[i][i] = 1;
			for(int j = i + 1; j < events.size(); j++) {
				simGraph[i][j] = simGraph[j][i] = innerProduct(X.get(i), X.get(j));
			}
		}
		System.err.println("sim graph done, " + (new Date().getTime() - start) / 1000);
		return simGraph;
	}
	
	
	
	
	
	public static double innerProduct(Map<String, Double> a, Map<String, Double> b) {
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
	
	
	public static boolean hasSharpAngle(Event first, Event middle, Event last){
		
		double distFM = first.computeDist(middle);
		double distFL = first.computeDist(last);
		double distML = middle.computeDist(last);
		
		return Math.pow(distFL, 2) <= Math.pow(distFM, 2) + Math.pow(distML, 2);
		
	}
	
	
	
	public static List<Event> findStoryEvents(List<Event> events){
		
		List<Event> rtn = new ArrayList<Event>();
		for(Event event: events){
			
			if(event.isMainEvent()){
				rtn.add(event);
			}
			
		}
		
		return rtn;
		
	}
	
	
}
