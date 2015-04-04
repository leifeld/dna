package dna;

import java.util.ArrayList;
import java.util.List;

/**
 * Extract features for the tokens.
 * 
 */
public class FeatureFactory {

	private List<DNAToken> tokens;
	private int numberOfFeatures;
	private List<DNAFeature> features;
	
	public FeatureFactory(List<DNAToken> tokens) {
		this.tokens = tokens;
		this.features = new ArrayList<DNAFeature>();
		//TODO add the features
		this.numberOfFeatures = features.size();
		
	}
	
	/**
	 * Generates the features for the tokens.
	 * @return the tokens with added features to them.
	 */
	public List<DNAToken> addFeatures() {
		
		for (DNAFeature feature : features) {
			tokens = feature.buildFeature(tokens);
		}
		
		return tokens;
	}
	
	public int getNumberOfFeatures() {
		return numberOfFeatures;
	}
	

}















