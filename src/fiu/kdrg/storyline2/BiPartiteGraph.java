package fiu.kdrg.storyline2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.jblas.DoubleMatrix;

public class BiPartiteGraph {

	private HashMap<String, Integer> keywords;
	private ArrayList<ArrayList<String>> documents;
	private DoubleMatrix biPartiteGraph;
	private int keyN;
	private int docN;
	private double[][][] influence;
	
	public void BiPartieG(ArrayList<ArrayList<String>> documents) {
		// TODO Auto-generated method stub
		this.documents = documents;
		this.docN = documents.size();
	}
	
	
	private Map<String, Integer> getDocFrequency(){
		
		Map<String, Integer> idf = new HashMap<String, Integer>();
		Set<String> wordSet = new HashSet<String>();
		
		for(List<String> document: documents){
			Set<String> checkDup = new HashSet<String>();
			for(String word: document){
				if(checkDup.contains(word))
					continue;
				else{
					checkDup.add(word);
					wordSet.add(word);
					Integer df = idf.get(word);
					if (df == null)
						df = 0;
					idf.put(word, df + 1);  //这个才是真正的document frequency
				}
			}
		}
		
		// wordID start from documents.size()
		int wordID = documents.size();
		for(String word: wordSet){
			keywords.put(word, wordID);
			wordID ++;
		}
		return idf;
	}
	
	
	
	
	public void constructBiPartiteGraph(){
		
		// invode getDocFrequency to set some variable first
		Map<String, Integer> df = getDocFrequency();
		keyN = keywords.entrySet().size();
		
		int dimension = docN + keyN;
		biPartiteGraph = DoubleMatrix.zeros(dimension, dimension);
		DoubleMatrix norm = DoubleMatrix.zeros(docN, 1);
		
		for(int row = 0; row < docN; row++){
			List<String> document = documents.get(row);
			Map<String,Integer> tfMap = new HashMap<String, Integer>();
			
			for(String word: document){
				Integer tf = tfMap.get(word);
				if(tf == null)
					tf = 0;
				tfMap.put(word, tf + 1);
			}
			
			for(Entry<String, Integer> en: tfMap.entrySet()){
				String key = en.getKey();
				int col = keywords.get(key);
				double val = en.getValue() * Math.log(docN/df.get(key));
				biPartiteGraph.put(row, col, val); // tf-idf for word key in document row
				norm.put(row, 1, norm.get(row, 1) + val * val);
			}
		}
		
		System.err.println("tf-idf done!.");
		//next step, normalization and set word-document weights.  truncate some word/maybe
		for(int row = 0; row < docN; row++){
			double normVal = Math.sqrt(norm.get(row, 1));
			for(int col = docN; col < dimension; col++){
				biPartiteGraph.put(row, col, biPartiteGraph.get(row, col)/normVal);
			}
			
		}
		
		//set word-document weights
		DoubleMatrix columnSums = biPartiteGraph.columnSums();
		for(Entry<String, Integer> en: keywords.entrySet()){
			
			int col = en.getValue();
			double wordNorm = columnSums.get(1,col);
			if(wordNorm > 0){
				for(int row = 0; row < docN; row++){
					biPartiteGraph.put(col, row, biPartiteGraph.get(row, col)/wordNorm);
				}
			}
			
		}
		
		System.err.println("biPartite Graph Done!.");
		
	}
	
	
	
	public void computeInfluenceByMultiplyIF_IDF(){
		
		influence = new double[docN][docN][keyN];
		for(int row1 = 0; row1 < docN; row1++){
			
			DoubleMatrix doc1 = biPartiteGraph.getRow(row1);
			for(int row2 = row1 + 1; row2 < docN; row2++){
				DoubleMatrix doc2 = biPartiteGraph.getRow(row2); 
				for(int col = 0; col < keyN + docN; col++){
					
					double val = doc1.get(1, col) * doc2.get(1, col);
					influence[row1][row2][col] = val;
					influence[row2][row1][col] = val;
					
				}
			}
		}
		
	}
	
	
	
	
		
	public HashMap<String, Integer> getKeywords() {
		return keywords;
	}


	public void setKeywords(HashMap<String, Integer> keywords) {
		this.keywords = keywords;
	}


	public ArrayList<ArrayList<String>> getDocuments() {
		return documents;
	}


	public void setDocuments(ArrayList<ArrayList<String>> documents) {
		this.documents = documents;
	}


	public DoubleMatrix getBiPartiteGraph() {
		return biPartiteGraph;
	}


	public void setBiPartiteGraph(DoubleMatrix biPartiteGraph) {
		this.biPartiteGraph = biPartiteGraph;
	}


	public int getKeyN() {
		return keyN;
	}


	public void setKeyN(int keyN) {
		this.keyN = keyN;
	}


	public int getDocN() {
		return docN;
	}


	public void setDocN(int docN) {
		this.docN = docN;
	}


	public double[][][] getInfluence() {
		return influence;
	}


	public void setInfluence(double[][][] influence) {
		this.influence = influence;
	}


	public static void main(String[] args) {
		
		Set<String> testSet = new TreeSet<String>();
		testSet.add("abc");
		testSet.add("bcd");
				
	}
	
}
