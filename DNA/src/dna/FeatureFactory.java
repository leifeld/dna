package dna;

import java.util.ArrayList;
import java.util.List;

public class FeatureFactory {

	private List<DNAToken> tokens;
	
	public FeatureFactory(List<DNAToken> tokens) {
		this.tokens = tokens;
	}
	
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
		return 2;
	}

}
