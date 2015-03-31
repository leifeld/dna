package dna;

import java.util.ArrayList;
import java.util.List;

public class SimpleDNATokenizer implements DNATokenizer {

	@Override
	public List<DNAToken> tokenize(int caretPosition, String text) {
		System.out.println("Pos: " + caretPosition);
		System.out.println(text);
		
		System.out.println("\n\n\n");
		
		ArrayList<DNAToken> tokens = new ArrayList<DNAToken>();
		return tokens;
	}

	@Override
	public List<String> tokenize(String text) {
		ArrayList<String> tokens = new ArrayList<String>();
		return tokens;
	}


}
