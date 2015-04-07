package dna.features;

import java.util.ArrayList;
import java.util.List;

import dna.DNAFeature;
import dna.DNAToken;

/**
 * Checks if "ALL" the letters in the token are capitalized 
 *
 */

public class AllCapitalizedDNAFeature extends DNAFeature {

	public static void main(String[] args) {
		//Test case
		List<DNAToken> tokens = new ArrayList<DNAToken>();
		DNAToken tok1 = new DNAToken();
		tok1.setText("Rak5an");
		
		DNAToken tok2 = new DNAToken();
		tok2.setText("writing");
		
		DNAToken tok3 = new DNAToken();
		tok3.setText("haKAn");
		
		DNAToken tok4 = new DNAToken();
		tok4.setText("IS");
		
		DNAToken tok5 = new DNAToken();
		tok5.setText("wri$ting");
		
		DNAToken tok6 = new DNAToken();
		tok6.setText("texiti");
		
		tokens.add( tok1 );
		tokens.add( tok2 );
		tokens.add( tok3 );
		tokens.add( tok4 );
		tokens.add( tok5 );
		tokens.add( tok6 );
		
		DNAFeature f = new AllCapitalizedDNAFeature();
		f.buildFeature(tokens);
		for (DNAToken tok : tokens) {
			System.out.println( tok.getText() + ": " + tok.getFeatures() );
		}
	}

	@Override
	public List<DNAToken> buildFeature(List<DNAToken> tokens) {
		for ( DNAToken tok : tokens ) {
			String txt = tok.getText();
			boolean allCapitals = txt.equals( txt.toUpperCase() );
			
			if ( allCapitals ) {
				tok.getFeatures().add(1.0);
			}
			else {
				tok.getFeatures().add(0.0);
			}
		}
		
		return tokens;
	}
	
	
}
