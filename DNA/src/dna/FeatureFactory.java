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
	
	public FeatureFactory(List<DNAToken> tokens) {
		this.tokens = tokens;
	}
	
	/**
	 * Generates the features for the tokens.
	 * @return the tokens with added features to them.
	 */
	public List<DNAToken> addFeatures() {
		
		for (DNAToken tok : tokens) {
			List<Double> features = new ArrayList<Double>();
			features.add(5.5);
			features.add(6.0);
			tok.setFeatures(features);
		}
		
		return tokens;
	}
	
	public int getNumberOfFeatures() {
		return numberOfFeatures;
	}

}
