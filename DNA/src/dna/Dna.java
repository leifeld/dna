package dna;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Dna {
	static Dna dna;
	DataAccess db;
	Gui gui;
	
	public Dna () {
		db = new DataAccess();
		gui = new Gui();
	}
	
	public static void main (String[] args) {
		dna = new Dna();
	}
	
	public void addStatement(String type, int doc, int start, int stop) {
		int statementId = dna.db.addStatement(type, doc, start, stop);
		Color color = dna.db.getStatementTypeColor(type);
		Date date = dna.db.getDocumentDate(doc);
		SidebarStatement s = new SidebarStatement(statementId, doc, start, 
				stop, date, color, type);
		dna.gui.sidebarPanel.ssc.addSidebarStatement(s, true);
	}
	
	public void removeStatement(int statementId) {
		dna.db.removeStatement(statementId);
		int row = dna.gui.sidebarPanel.ssc.getIndexByStatementId(statementId);
		dna.gui.sidebarPanel.ssc.remove(row);
	}
	
	public void addDocument(String title, String text, Date date, String coder, 
			String source, String notes, String type) {
		int id = dna.db.addDocument(title, text, date, coder, source, notes, 
				type);
		Document d = Dna.dna.db.getDocument(id);
		dna.gui.documentPanel.documentContainer.addDocument(d);
	}

	public void removeDocument(int documentId) {
		int row = dna.gui.documentPanel.documentContainer.getRowIndexById(documentId);
		dna.gui.documentPanel.documentContainer.remove(row);
		dna.db.removeDocument(documentId);
	}
	
	public void openFile(String dbfile) {
		Dna.dna.db.openFile(dbfile);
		Dna.dna.gui.statusBar.resetLabel();
		ArrayList<Document> docs = Dna.dna.db.getDocuments();
		for (int i = 0; i < docs.size(); i++) {
			Dna.dna.gui.documentPanel.documentContainer.addDocument(docs.
					get(i));
			int documentId = docs.get(i).getId();
			ArrayList<SidebarStatement> statements = Dna.dna.db.
					getStatementsPerDocumentId(documentId);
			for (int j = 0; j < statements.size(); j++) {
				SidebarStatement s = statements.get(j);
				Dna.dna.gui.sidebarPanel.ssc.addSidebarStatement(s, true);
			}
		}
	}
	
	public void closeFile() {
		Dna.dna.db.closeFile();
		Dna.dna.gui.statusBar.resetLabel();
		Dna.dna.gui.documentPanel.documentContainer.clear();
		Dna.dna.gui.sidebarPanel.ssc.clear();
		Dna.dna.gui.textPanel.setDocumentText("");
	}
	
	public void newFile(String filename) {
		Dna.dna.db.openFile(filename);
		Dna.dna.gui.statusBar.resetLabel();
		db.createTables();
		HashMap<String, String> dnaStatementMap = new HashMap<String, String>();
		dnaStatementMap.put("person", "short text");
		dnaStatementMap.put("organization", "short text");
		dnaStatementMap.put("category", "short text");
		dnaStatementMap.put("agreement", "boolean");
		db.insertStatementType("DNAStatement", 255, 255, 0, dnaStatementMap);
	}
}