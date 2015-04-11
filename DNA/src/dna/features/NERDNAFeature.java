package dna.features;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dna.DNAFeature;
import dna.DNAToken;
import dna.Utils;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * A NER feature that uses the Stanford NER classifier. 
 *
 */

public class NERDNAFeature extends DNAFeature {

	private static final String classifier_path = "/Users/rockyrock/git/dna/DNA/src/dna/features/models/hgc_175m_600.crf.ser.gz";
	private AbstractSequenceClassifier<CoreLabel> classifier;
	private static final int NUMB_FEATURES = 4;

	public  NERDNAFeature() {
		try {
			classifier = CRFClassifier.getClassifier(classifier_path);
		} catch (ClassCastException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		//Test case
		List<DNAToken> tokens = new ArrayList<DNAToken>();
		DNAToken tok1 = new DNAToken();
		tok1.setText("Syrien");

		DNAToken tok2 = new DNAToken();
		tok2.setText("Name");

		DNAToken tok3 = new DNAToken();
		tok3.setText("ist");

		DNAToken tok4 = new DNAToken();
		tok4.setText("John");

		DNAToken tok5 = new DNAToken();
		tok5.setText("und");

		DNAToken tok6 = new DNAToken();
		tok6.setText("Syrien");

		DNAToken tok7 = new DNAToken();
		tok7.setText("Amerikaner");

		tokens.add( tok1 );
		tokens.add( tok2 );
		tokens.add( tok3 );
		tokens.add( tok4 );
		tokens.add( tok5 );
		tokens.add( tok6 );
		tokens.add( tok7 );

		DNAFeature f = new NERDNAFeature();
		f.buildFeature(tokens);
		for (DNAToken tok : tokens) {
			System.out.println( tok.getText() + ": " + tok.getFeatures() );
		}

	}

	@Override
	public List<DNAToken> buildFeature(List<DNAToken> tokens) {

		StringBuffer sbuffer = new StringBuffer();
		List<String> words = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();

		for (DNAToken tok : tokens) {
			sbuffer.append( tok.getText() + " " );
		}

		List<List<CoreLabel>> out = classifier.classify(sbuffer.toString());
		for (List<CoreLabel> sentence : out) {
			for (CoreLabel word : sentence) {
				words.add(word.word());
				tags.add(word.get(CoreAnnotations.AnswerAnnotation.class));
			}
		}

		for ( int i = 0; i < tokens.size(); i++ ) {
			DNAToken tok = tokens.get(i);
			String word = words.get(i);
			String tag = tags.get(i);
			double[] features = new double[ NUMB_FEATURES ];
			
			if ( word.equals( tok.getText() ) ) {
				
				if ( NUMB_FEATURES != 4 )
					throw new RuntimeException( "The number of tags are different in NER than Stanford's German NER tags!" );
				else {
					if ( tag.equals( "I-PER" ) || tag.equals( "O-PER" ) )
						features[0] = 1;
					else if ( tag.equals( "I-LOC" ) || tag.equals( "O-LOC" ) )
						features[1] = 1;
					else if ( tag.equals( "I-ORG" ) || tag.equals( "O-ORG" ) )
						features[2] = 1;
					else if ( tag.equals( "I-MISC" ) || tag.equals( "O-MISC" ) )
						features[3] = 1;
					
					tok.getFeatures().addAll( Utils.asList(features) );
				}
				
			}
			else {
				throw new RuntimeException( "The DNA token do not correspond to the Stanford token in NER" );
			}
		}
		
		return tokens;
	}

}














