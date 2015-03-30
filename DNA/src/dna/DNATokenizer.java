package dna;

import java.util.List;

public interface DNATokenizer {

	public List<DNAToken> tokenize(int caretPosition, String text);
	public List<String> tokenize(String text);
	
}
