package dna.features;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dna.DNAToken;

public class Vocabulary {
	
	public static void main(String[] args) {
		//test case
		
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
		
		tokens.add( tok1 );
		tokens.add( tok2 );
		tokens.add( tok3 );
		tokens.add( tok4 );
		tokens.add( tok5 );
		
		Vocabulary voc = new Vocabulary();
		
		for (String t : voc.getTokens()) {
			System.out.println("->"+t);
		}
		
	}
	
	private Map<String, Integer> vocab;
	
	public Vocabulary() {
		vocab = readVocabularyFile();
	}
	
	public Vocabulary(List<DNAToken> tokens) {
		buildVocabularyFile(tokens);
		vocab = readVocabularyFile();
	}
	
	public int getIndex( String txt ) {
		txt = preprocess(txt);
		if ( vocab.containsKey(txt) )
			return vocab.get(txt);
		else 
			return -1;
	}
	
	public Set<String> getTokens() {
		return vocab.keySet();
	}
	
	public int getSize() {
		return vocab.size();
	}
	
	public static String preprocess(String txt) {
		return txt.toLowerCase();
	}
	
	public static Map<String, Integer> readVocabularyFile() {
		File file = new File("vocab.txt");
		Map<String, Integer> vocab = new LinkedHashMap<String, Integer>();

		if ( !file.exists() ) {
			throw new RuntimeException("Couldn't find the vocabulary file!");
		}
		else {
			try {
				FileReader fr = new FileReader( file );
				BufferedReader br = new BufferedReader(fr);
				String tok = "";
				for( int i = 0; ( tok = br.readLine() ) != null; i++ ) {
					if (!vocab.containsKey(tok)) {
						vocab.put(tok, i);
					}
					else {
						throw new RuntimeException("A duplicate in the tokens file!");
					}
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return vocab;
	}

	public static void buildVocabularyFile(List<DNAToken> tokens) {
		Set<String> vocab = new LinkedHashSet<String>();
		File file = new File("vocab.txt");

		if (!file.exists()) {
			try {
				System.out.println("Creating the vocabulary file.");
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);

				for (DNAToken tok : tokens) {
					if( !vocab.contains( preprocess( tok.getText() ) ) ) {
						vocab.add( preprocess( tok.getText() ) );
					}
				}

				for (String tok : vocab) {
					bw.write(tok + "\n");
				}

				bw.close();
				System.out.println("Done.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.err.println("Can't create a vocabulary file because it exists!");
		}
	}
}
