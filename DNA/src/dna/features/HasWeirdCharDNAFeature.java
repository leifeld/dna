package dna.features;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dna.DNAFeature;
import dna.DNAToken;

/**
 * A feature that checks if a token has weird characters (non alphabetic) or not.
 *
 */

public class HasWeirdCharDNAFeature extends DNAFeature {
	
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
		tok4.setText("is");
		
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
		
		DNAFeature f = new HasWeirdCharDNAFeature();
		f.buildFeature(tokens);
		for (DNAToken tok : tokens) {
			System.out.println( tok.getText() + ": " + tok.getFeatures() );
		}
	}

	@Override
	public List<DNAToken> buildFeature(List<DNAToken> tokens) {
		Pattern pattern = Pattern.compile("\\P{L}+");
		Matcher m = null;
		
		for ( DNAToken tok : tokens ) {
			String txt = tok.getText();
			m = pattern.matcher(txt);
			boolean hasWeirdChar = m.find();
			
			if ( hasWeirdChar ) {
				tok.getFeatures().add(1.0);
			}
			else {
				tok.getFeatures().add(0.0);
			}
		}
		
		return tokens;
	}
	
	

}
