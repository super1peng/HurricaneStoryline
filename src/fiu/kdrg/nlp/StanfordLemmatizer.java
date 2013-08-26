package fiu.kdrg.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordLemmatizer {
	
	private static StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
	private StanfordCoreNLP pipeline;
	
	private StanfordLemmatizer(){
		
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		
		this.pipeline = new StanfordCoreNLP(props);
		
	}
	
	
	public static StanfordLemmatizer getInstance(){
		
		return lemmatizer;
		
	}
	
	
	public List<String> lemmatize(String documentText){
		
		List<String> lemmas = new ArrayList<String>();
		
		//create an empty Annotation just with the given text
		Annotation document = new Annotation(documentText);
		
		//run all annotators on this text
		this.pipeline.annotate(document);
		
		
		// Iterate over all of the sentence found
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences){
			
			for(CoreLabel token: sentence.get(TokensAnnotation.class)){
				
				lemmas.add(token.get(LemmaAnnotation.class));
				
			}
			
		}
		
		return lemmas;
		
	}
	
	public static void main(String[] args) {
		
		StanfordLemmatizer lemmatizer = StanfordLemmatizer.getInstance();
		List<String> answer = lemmatizer.lemmatize("I did not think so");
		System.out.println(answer.toString());
		answer = lemmatizer.lemmatize("You are thinking so");
		System.out.println(answer.toString());
		
	}
	
	
}













