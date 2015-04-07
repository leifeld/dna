package dna.features;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dna.DNAFeature;
import dna.DNAToken;
import dna.Utils;

public class NGramDNAFeature extends DNAFeature {
	
	public static void main(String[] args) {
		//Test case
		List<DNAToken> tokens = new ArrayList<DNAToken>();
		DNAToken tok1 = new DNAToken();
		tok1.setText("Rakan");
		
		DNAToken tok2 = new DNAToken();
		tok2.setText("writing");
		
		DNAToken tok3 = new DNAToken();
		tok3.setText("haKAn");
		
		DNAToken tok4 = new DNAToken();
		tok4.setText("is");
		
		DNAToken tok5 = new DNAToken();
		tok5.setText("writing");
		
		DNAToken tok6 = new DNAToken();
		tok6.setText("texiti");
		
		tokens.add( tok1 );
		tokens.add( tok2 );
		tokens.add( tok3 );
		tokens.add( tok4 );
		tokens.add( tok5 );
		tokens.add( tok6 );
		
		DNAFeature f = new NGramDNAFeature(3);
		f.buildFeature(tokens);
		for (DNAToken tok : tokens) {
			System.out.println( tok.getText() + ": " + tok.getFeatures() );
		}
	}
	
	private Vocabulary vocab;
	private Map<String, Integer> nGrams;
	private final int n;
	
	public NGramDNAFeature(int n) {
		vocab = new Vocabulary();
		nGrams = new LinkedHashMap<String, Integer>();
		this.n = n;
		buildNGrams();
	}

	@Override
	public List<DNAToken> buildFeature(List<DNAToken> tokens) {
		
		for (DNAToken tok : tokens) {
			String tokString = tok.getText();
			tokString = preprocess(tokString);
			List<String> grams = findGrams(tokString);
			
			double[] oneHot = new double[nGrams.size()];
			
			for ( String gram : grams ) {
				if ( nGrams.containsKey(gram) ) {
					int index = nGrams.get(gram);
					oneHot[index] = 1;
				}
			}
			
			tok.getFeatures().addAll(Utils.asList(oneHot));
		}
		
		return tokens;
	}

	private void buildNGrams() {
		int n = getN();
		Set<String> grams = new LinkedHashSet<String>();
		for ( String tok : vocab.getTokens() ) {
			tok = preprocess(tok);
			for ( int i = 0; i + n <= tok.length(); i++  ) {
				String gram = tok.substring(i, i+n);
				if( !grams.contains(gram) ) {
					grams.add(gram);
				}
			}
		}
		
		int index = 0;
		for ( String gram : grams ) {
			nGrams.put(gram, index);
			index++;
		}
	}
	
	public int getN() {
		return n;
	}

	public List<String> findGrams( String token ) {
		token = preprocess(token);
		int n = getN();
		List<String> grams = new ArrayList<String>();
		
		for ( int i = 0; i + n <= token.length(); i++  ) {
			String gram = token.substring(i, i+n);
			grams.add(gram);
		}
		
		return grams;
	}
	
	public static String preprocess(String txt) {
		return txt.toLowerCase();
	}
}























