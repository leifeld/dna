package dna;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Dna {
	static Dna dna;
	DataAccess db;
	Gui gui;
	
	public Dna () {
		//String dbfile = "/home/philip/test.dna";
		//db = new DataAccess(dbfile);
		//db.createTables();
		
		//HashMap<String, String> dnaStatementMap = new HashMap<String, String>();
		//dnaStatementMap.put("person", "short text");
		//dnaStatementMap.put("organization", "short text");
		//dnaStatementMap.put("category", "short text");
		//dnaStatementMap.put("agreement", "boolean");
		//db.insertStatementType("DNAStatement", 0, 255, 255, dnaStatementMap);
		db = new DataAccess();
		gui = new Gui();
	}
	
	public static void main (String[] args) {
		dna = new Dna();
	}
	
	public void addDocument(String title, String text, Date date, String coder, 
			String source, String notes, String type) {
		int id = dna.db.addDocument(title, text, date, coder, source, notes, 
				type);
		Document d = Dna.dna.db.getDocument(id);
		dna.gui.documentPanel.documentContainer.addDocument(d);
	}
	
	public void openFile(String dbfile) {
		Dna.dna.db.openFile(dbfile);
		Dna.dna.gui.statusBar.resetLabel();
		ArrayList<Document> docs = Dna.dna.db.getDocuments();
		for (int i = 0; i < docs.size(); i++) {
			Dna.dna.gui.documentPanel.documentContainer.addDocument(docs.get(i));
		}
	}
}
