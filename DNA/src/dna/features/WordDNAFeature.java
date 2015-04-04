package dna.features;

import java.util.List;

import dna.DNAFeature;
import dna.DNAToken;
import dna.Utils;

public class WordDNAFeature extends DNAFeature {
	
	private Vocabulary vocab;
	
	public WordDNAFeature() {
		vocab = new Vocabulary();
	}


	@Override
	public List<DNAToken> buildFeature(List<DNAToken> tokens) {
		if (!tokens.isEmpty()) {
			for (DNAToken tok : tokens) {
				int index = vocab.getIndex( tok.getText() );
				double[] oneHot = new double[ vocab.getSize() ];
				if (index != -1) {
					oneHot[index] = 1;
				}
				tok.getFeatures().addAll(Utils.asList(oneHot));
			}
		}
		else {
			throw new RuntimeException("No tokens in the tokens list to add features to!");
		}

		return tokens;
	}

}