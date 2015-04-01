package dna;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class StanfordDNATokenizer implements DNATokenizer {

	@Override
	public List<DNAToken> tokenize(int offset, String text) {
		ArrayList<DNAToken> tokens = new ArrayList<DNAToken>();
		
		StringReader reader = new StringReader(text);
		PTBTokenizer ptbt = new PTBTokenizer(reader,
				new CoreLabelTokenFactory(), "");
		for (CoreLabel label; ptbt.hasNext(); ) {
			label = (CoreLabel) ptbt.next();
			
			DNAToken token = new DNAToken();
			token.setText(label.toString());
			token.setStart_position(offset + label.beginPosition());
			token.setEnd_position(offset + label.endPosition());
			
			tokens.add(token);
			
		}
		
		
		return tokens;
	}

	@Override
	public List<String> tokenize(String text) {
		ArrayList<String> tokens = new ArrayList<String>();
		return tokens;
	}

	public static void main(String[] args) throws IOException {
		test2();
	}

	/**
	 * Tokenizing using regex.
	 */
	public static void test1() {
		Pattern p = Pattern.compile("([$\\d.,]+)|([\\w\\d!$]+)");
		String str = "This is a Germän text, 21$ and for 23.";
		System.out.println("input: " + str);

		Matcher m = p.matcher(str);
		while(m.find()) {
			System.out.println("token: " + m.group());
		}
	}

	/**
	 * How to use the Stanford CoreNLP for tokenizing.
	 * @throws IOException
	 */
	public static void test2() throws IOException {
		String txt = "this is a germän text";
		StringReader reader = new StringReader("this is a germän text");
		PTBTokenizer ptbt = new PTBTokenizer(reader,
				new CoreLabelTokenFactory(), "");
		for (CoreLabel label; ptbt.hasNext(); ) {
			label = (CoreLabel) ptbt.next();
			System.out.println( "L:"+ txt.substring(label.beginPosition(), label.endPosition()) );
			System.out.println(label);
		}
	}

}













