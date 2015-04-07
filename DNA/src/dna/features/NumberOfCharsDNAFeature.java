package dna.features;

import java.util.ArrayList;
import java.util.List;

import dna.DNAFeature;
import dna.DNAToken;

public class NumberOfCharsDNAFeature extends DNAFeature {
	
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
		
		DNAFeature f = new NumberOfCharsDNAFeature();
		f.buildFeature(tokens);
		for (DNAToken tok : tokens) {
			System.out.println( tok.getText() + ": " + tok.getFeatures() );
		}
	}

	@Override
	public List<DNAToken> buildFeature(List<DNAToken> tokens) {
		
		for (DNAToken tok : tokens) {
			Double charsNumber = new Double( tok.getText().length() );
			tok.getFeatures().add( charsNumber );
		}
		
		return tokens;
		
	}
	
	

}
