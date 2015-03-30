package dna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DNATextMiner {

	public static void main(String[] args) {
		String file = "/Users/rockyrock/Desktop/file.dna";
		DNATextMiner textMiner = new DNATextMiner( new SimpleDNATokenizer() );
		textMiner.data_extractor(file);
	}
	
	private DNATokenizer tokenzier;
	
	public DNATextMiner(DNATokenizer tokenzier) {
		this.tokenzier = tokenzier;
	}
	
	/**
	 * Produces a CSV file from a DNA file with the tokens of the documents, their features and their labels. 
	 * The file will be saved in the same location but with a .csv extension.
	 * @param filePath
	 */
	public void data_extractor( String filePath ) {
		
		DataAccess dataAccess = new DataAccess("sqlite", filePath );
		ArrayList<Document> documentsList = dataAccess.getDocuments();
		ArrayList<DNAToken> tokens = new ArrayList<DNAToken>();
		
		
		for (Document document : documentsList) {
			List<SidebarStatement> statements = 
					dataAccess.getStatementsPerDocumentId(document.getId());
			
			HashMap<Integer, Integer> statements_positions = new HashMap<Integer, Integer>();
			
			String docString = document.getText();
			
			for (SidebarStatement st : statements) {
				if (st.getType().equals("Person")) {
					statements_positions.put(st.getStart(), st.getStop());
				}
			}
			
			StringBuffer normalText = new StringBuffer();
			StringBuffer statementText = new StringBuffer();
			int normalTextStartPosition = 0;
			
			for( int index = 0; index < docString.length(); index++ ) {
				if ( statements_positions.containsKey(index) ) {
					
					//tokenize and flush the normal text and clear its buffer.
					tokens.addAll( getTokenzier().tokenize(normalTextStartPosition,
							normalText.toString()) );
					normalText = new StringBuffer();
					
					//tokenize and flush the statement text and then clear its buffer.
					int start_pos = index;
					int end_pos = statements_positions.get(start_pos);
					index = end_pos;// update index to continue buffering normal text after statement
					
					for (int j = start_pos; j <= end_pos; j++) {
						statementText.append( docString.charAt(j));
					}
					
					tokens.addAll( getTokenzier().tokenize(start_pos, statementText.toString()) );
					statementText = new StringBuffer();
					normalTextStartPosition = end_pos + 1;
				}
				else {
					normalText.append( docString.charAt(index) );
				}
			}
			
		}
		
		dataAccess.closeFile();
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

	public DNATokenizer getTokenzier() {
		return tokenzier;
	}

	public void setTokenzier(DNATokenizer tokenzier) {
		this.tokenzier = tokenzier;
	}

	
	
}











