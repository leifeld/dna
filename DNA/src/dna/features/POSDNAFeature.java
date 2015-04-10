package dna.features;

import java.util.ArrayList;
import java.util.List;

import dna.DNAFeature;
import dna.DNAToken;
import dna.Utils;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Part-of-Speech feature that uses the Stanford POS tagger for German.  
 * Tag-list from here: https://web.archive.org/web/20130128232615/http://www.ims.uni-stuttgart.de/projekte/CQPDemos/Bundestag/help-tagset.html
 */

public class POSDNAFeature extends DNAFeature {
	
	private static final String model_path = "/Users/rockyrock/git/dna/DNA/src/dna/features/models/german-hgc.tagger";
	private static final String[] tagsList = 
		{ "ADJA", "ADJD", "ADV", "APPR", "APPRART", "APPO", "APZR", "ART", "CARD",
		"FM", "ITJ", "KOUI", "KOUS", "KON", "KOKOM", "NN", "NE", "PDS", "PDAT", "PIS", "PIAT",
		"PIDAT", "PPER", "PPOSS", "PPOSAT", "PRELS", "PRELAT", "PRF", "PWS", "PWAT", "PWAV",
		"PAV", "PTKZU", "PTKNEG", "PTKVZ", "PTKANT", "PTKA", "TRUNC", "VVFIN", "VVIMP", "VVINF",
		"VVIZU", "VVPP", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN", "VMINF", "VMPP",
		"XY", "$,", "$.", "$("}; 
	
	public static void main(String[] args) {
		//testcase
		MaxentTagger tagger = new MaxentTagger(model_path);
		String result = tagger.tagTokenizedString("Mein Name ist Rakan , und ich    gehe");
		
		System.out.println(result);
		
		for (String s : result.split(" ")) {
			System.out.println(s.split("_")[1]);
		}
		
		//Test case
				List<DNAToken> tokens = new ArrayList<DNAToken>();
				DNAToken tok1 = new DNAToken();
				tok1.setText("Mein");
				
				DNAToken tok2 = new DNAToken();
				tok2.setText("Name");
				
				DNAToken tok3 = new DNAToken();
				tok3.setText("ist");
				
				DNAToken tok4 = new DNAToken();
				tok4.setText(",");
				
				DNAToken tok5 = new DNAToken();
				tok5.setText("und");
				
				DNAToken tok6 = new DNAToken();
				tok6.setText("ich");
				
				DNAToken tok7 = new DNAToken();
				tok7.setText("gehe");
				
				tokens.add( tok1 );
				tokens.add( tok2 );
				tokens.add( tok3 );
				tokens.add( tok4 );
				tokens.add( tok5 );
				tokens.add( tok6 );
				tokens.add( tok7 );
				
				DNAFeature f = new POSDNAFeature();
				f.buildFeature(tokens);
				for (DNAToken tok : tokens) {
					System.out.println( tok.getText() + ": " + tok.getFeatures() );
				}
		
	}

	@Override
	public List<DNAToken> buildFeature(List<DNAToken> tokens) {
		
		StringBuffer sbuffer = new StringBuffer();
		
		for (DNAToken tok : tokens) {
			sbuffer.append( tok.getText() + " " );
		}
		
		MaxentTagger tagger = new MaxentTagger(model_path);
		String result = tagger.tagTokenizedString(sbuffer.toString());
		String[] tempTokensTags = result.split(" ");
		
		for ( int i = 0; i < tokens.size(); i++ ) {
			String tokenTag = tempTokensTags[i].split("_")[1];
			DNAToken tok = tokens.get(i);
			double[] features = new double[tagsList.length];
			
			for ( int j = 0; j < tagsList.length; j++ ) {
				if ( tokenTag.equals( tagsList[j] ) ) {
					features[j] = 1;
					System.out.println(tok.getText());
					System.out.println(tagsList[j]);
				}
					
			}
			
			tok.getFeatures().addAll( Utils.asList(features) );
		}
		
		return tokens;
	}

}



















