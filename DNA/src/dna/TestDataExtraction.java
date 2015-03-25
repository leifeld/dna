package dna;

import java.util.ArrayList;
import java.util.List;

public class TestDataExtraction {

	public static void main(String[] args) {
		String file = "/Users/rockyrock/Desktop/file.dna";
		DataAccess dataAccess = new DataAccess("sqlite", file );
		ArrayList<Document> documentsList = dataAccess.getDocuments();
		
		for (Document document : documentsList) {
			List<SidebarStatement> statements = 
					dataAccess.getStatementsPerDocumentId(document.getId());
			
			List<int[]> classPersonRanges = new ArrayList<int[]>();
			List<int[]> classOrgRanges = new ArrayList<int[]>();
			List<int[]> classConceptRanges = new ArrayList<int[]>();
			
			String docString = document.getText();
			
			for (SidebarStatement st : statements) {
				if (st.getType().equals("Person")) {
					int[] range = new int[2];
					range[0] = st.getStart();
					range[1] = st.getStop();
					classPersonRanges.add(range);
				}
				else if (st.getType().equals("Organization")) {
					int[] range = new int[2];
					range[0] = st.getStart();
					range[1] = st.getStop();
					classOrgRanges.add(range);
				}
				else if (st.getType().equals("Concept")) {
					int[] range = new int[2];
					range[0] = st.getStart();
					range[1] = st.getStop();
					classConceptRanges.add(range);
				}
			}
			
			//TODO now tokenize the words of the document, store the start and stop caret position of the words,
			//and then check to which class does each word belong.
			//
			
		}
		
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

}











