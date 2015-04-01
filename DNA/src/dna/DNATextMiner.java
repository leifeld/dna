package dna;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DNATextMiner {

	public static void main(String[] args) {
		String file = "/Users/rockyrock/Desktop/file.dna";
		DNATextMiner textMiner = new DNATextMiner( new SimpleDNATokenizer() );
		textMiner.extract_data(file, "Person");
//		test();
	}
	
	private DNATokenizer tokenzier;
	
	public DNATextMiner(DNATokenizer tokenzier) {
		this.tokenzier = tokenzier;
	}
	
	/**
	 * Produces a CSV file from a DNA file with the tokens of the documents, their features and their labels. 
	 * The file will be saved in the same location but with a .csv extension.
	 * @param filePath
	 * @param classLabel can be ('Person', 'Organization', 'Concept')
	 */
	public void extract_data( String filePath, String classLabel ) {
		
		DataAccess dataAccess = new DataAccess("sqlite", filePath );
		List<Document> documentsList = dataAccess.getDocuments();
		List<DNAToken> allTokens = new ArrayList<DNAToken>();
		
		
		for (Document document : documentsList) {
			List<DNAToken> docTokens = new ArrayList<DNAToken>();
			List<SidebarStatement> statements = 
					dataAccess.getStatementsPerDocumentId(document.getId());
			
			HashMap<Integer, Integer> statements_positions = new HashMap<Integer, Integer>();
			
			String docString = document.getText();
			
			//Store statements start and end positions
			for (SidebarStatement st : statements) {
				if (st.getType().equals(classLabel)) {
					
					if ( !statements_positions.containsKey( st.getStart() ) ) {
						statements_positions.put(st.getStart(), st.getStop());
					}
					else {
						//Store the statement with the larger range
						if ( statements_positions.get( st.getStart() ) < st.getStop() ) {
							statements_positions.put( st.getStart(),  st.getStop());
						}
					}
				}
			}
			
			//Remove the short highlighted statements inside a larger statement,
			//i.e. just select the wider statement.
			statements_positions = removeInnerStatements( statements_positions );
			
			StringBuffer normalText = new StringBuffer();
			StringBuffer statementText = new StringBuffer();
			int normalTextStartPosition = 0;
			//buffer statements and normal text and then tokenize them
			for( int index = 0; index < docString.length(); index++ ) {
				if ( statements_positions.containsKey(index) ) {
					
					//tokenize and flush the normal text and clear its buffer.
					List<DNAToken> temp_tokens = getTokenzier().tokenize(normalTextStartPosition,
							normalText.toString());
					temp_tokens = giveLabels(temp_tokens, "N");
					docTokens.addAll( temp_tokens );
					normalText = new StringBuffer();
					
					//tokenize and flush the statement text and then clear its buffer.
					int start_pos = index;
					int end_pos = statements_positions.get(start_pos);
					index = end_pos-1;// update index to continue buffering normal text after statement
					statementText.append( docString.substring( start_pos, end_pos ) );
					
					temp_tokens = getTokenzier().tokenize(start_pos, statementText.toString());
					temp_tokens = giveLabels(temp_tokens, "P");
					docTokens.addAll( temp_tokens );
					statementText = new StringBuffer();
					normalTextStartPosition = end_pos;
				}
				else {
					normalText.append( docString.charAt(index) );
				}
			}
			
			if ( normalText.length() > 0 ) {
				List<DNAToken> temp_tokens = getTokenzier().tokenize(normalTextStartPosition,
						normalText.toString());
				temp_tokens = giveLabels(temp_tokens, "N");
				docTokens.addAll( temp_tokens );
				normalText = new StringBuffer();
			}
			
			for ( int i = 0; i < docTokens.size(); i++ ) {
				DNAToken tok = docTokens.get(i);
				tok.setDocId( document.getId() );
				tok.setId(i);
			}
			
			allTokens.addAll(docTokens);
//			break;
		}
		
		dataAccess.closeFile();
		
		FeatureFactory featFact = new FeatureFactory(allTokens);
		allTokens = featFact.addFeatures();
		toCSVFile(allTokens, featFact.getNumberOfFeatures(), filePath);
	}
	
	/**
	 * It checks if a word is from a specific class (Person, Organization, Concept)
	 * @param wStart the start caret position of the word in the text.
	 * @param wStop the end caret position of the word in the text.
	 * @param ranges an array that contains the start and end position for every highlighted class/statement in a document.
	 * @return true if the word is in one of the ranges of the respective class.
	 */
	public static boolean isFromType( int wStart, int wStop, List<int[]> ranges ) {
		
		boolean isFromType = false;
		
		for (int[] range : ranges) {
			
			int pStart = range[0];
			int pStop = range[1];
			
			if ( (wStart >= pStart) && (wStop <= pStop) ) {
				isFromType = true;
				break;
			}
			
		}
		
		return isFromType;
		
	}
	
	/**
	 * Removes the statements that fall in a larger statement range.
	 * @param statements_positions
	 * @return
	 */
	public static HashMap<Integer, Integer> removeInnerStatements( HashMap<Integer, Integer> 
		statements_positions ) {
		ArrayList<Integer> tobe_removed = new ArrayList<Integer>();
		
		for (Integer start : statements_positions.keySet()) {
			int end = statements_positions.get(start);
			
			for (Integer temp_start : statements_positions.keySet()) {
				int temp_end = statements_positions.get(temp_start);
				
				if ( ( start >= temp_start && end <= temp_end) && ( start != temp_start || end !=temp_end ) ) {
					tobe_removed.add(start);
				}
				
			}
			
		}
		
		for (int key : tobe_removed) {
			statements_positions.remove(key);
		}
		
//		System.out.println(statements_positions);
		
		return statements_positions;
	}

	public DNATokenizer getTokenzier() {
		return tokenzier;
	}

	public void setTokenzier(DNATokenizer tokenzier) {
		this.tokenzier = tokenzier;
	}
	
	public static List<DNAToken> giveLabels( List<DNAToken> tokens, String label ) {
		
		for (DNAToken token : tokens) {
			token.setLabel(label);
		}
		
		return tokens;
	}

	public static void test() {
		String file = "/Users/rockyrock/Desktop/file.dna";
		DataAccess dataAccess = new DataAccess("sqlite", file );
		ArrayList<Document> documentsList = dataAccess.getDocuments();
		
		for (Document document : documentsList) {
			List<SidebarStatement> statements = 
					dataAccess.getStatementsPerDocumentId(document.getId());
			String docString = document.getText();
			
			for (SidebarStatement st : statements) {
				if (st.getType().equals("Person")) {
					String txt = docString.substring(st.getStart(), st.getStop());
					System.out.println(txt);
				}
			}
			
			break;
		}
	}
	
	public static void toCSVFile(List<DNAToken> tokens, int numberOfFeatures, String path) {
		System.out.println("Saving as CSV file ...");
		File oldFile = new File(path);
		File csvFile = new File( oldFile.getAbsoluteFile() +  ".csv" );
		System.out.println(csvFile.getAbsolutePath());
		if (!csvFile.exists()) {
			try {
				csvFile.createNewFile();
				
				FileWriter fw = new FileWriter(csvFile.getAbsoluteFile());
				
				BufferedWriter bw = new BufferedWriter(fw);
				
				//Write header
				bw.write("token,id,docId,start_position,end_position,");
				
				for (int i = 0; i < numberOfFeatures; i++) {
					bw.write("f"+i+",");
				}
				
				bw.write("label\n");
				
				for (DNAToken tok : tokens) {
					bw.write(tok.getText() + "," + tok.getId() +
							"," + tok.getDocId() + "," + tok.getStart_position() +
							"," + tok.getEnd_position() + ",");
					
					for (Double f : tok.getFeatures()) {
						bw.write(f.toString() + ",");
					}
					
					bw.write(tok.getLabel() + "\n");
				}
				
				bw.close();
				System.out.println("Done.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.err.println("CSV file exists!");
		}
		
	}
	
}











