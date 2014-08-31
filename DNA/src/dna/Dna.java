package dna;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;

public class Dna {
	String version = "2.0 alpha 6";
	String date = "August 31, 2014";
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

	public int addStatement(String type, int doc, int start, int stop) {
		int statementId = dna.db.addStatement(type, doc, start, stop);
		Color color = dna.db.getStatementTypeColor(type);
		Date date = dna.db.getDocument(doc).getDate();
		SidebarStatement s = new SidebarStatement(statementId, doc, start, 
				stop, date, color, type);
		dna.gui.sidebarPanel.ssc.addSidebarStatement(s, true);
		return statementId;
	}

	public void removeStatement(int statementId) {
		dna.db.removeStatement(statementId);
		int row = dna.gui.sidebarPanel.ssc.getIndexByStatementId(statementId);
		dna.gui.sidebarPanel.ssc.remove(row);
		dna.gui.sidebarPanel.statementTable.updateUI();
	}

	public int addDocument(String title, String text, Date date, String coder, 
			String source, String section, String notes, String type) {
		int id = dna.db.addDocument(title, text, date, coder, source, section, 
				notes, type);
		Document d = Dna.dna.db.getDocument(id);
		dna.gui.documentPanel.documentContainer.addDocument(d);
		return id;
	}

	public void removeDocument(int documentId) {
		ArrayList<SidebarStatement> al = dna.db.getStatementsPerDocumentId(documentId);
		for (int i = 0; i < al.size(); i++) {
			removeStatement(al.get(i).getStatementId());
		}
		int row = dna.gui.documentPanel.documentContainer.getRowIndexById(documentId);
		dna.gui.documentPanel.documentContainer.remove(row);
		dna.db.removeDocument(documentId);
	}

	public void openFile(String dbfile) {
		Dna.dna.db.openSQLite(dbfile);
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
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateRegexManagerPanel();
	}

	public void openMySQL(String url, String userName, String password) {
		Dna.dna.db.openMySQL(url, userName, password);
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
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateRegexManagerPanel();
	}

	public void openMSSQL(String url, String dbname, String userName, 
			String password) {
		Dna.dna.db.openMSSQL(url, dbname, userName, password);
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
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateRegexManagerPanel();
	}

	public void closeFile() {
		Dna.dna.db.closeFile();
		Dna.dna.gui.statusBar.resetLabel();
		Dna.dna.gui.documentPanel.documentContainer.clear();
		Dna.dna.gui.sidebarPanel.ssc.clear();
		Dna.dna.gui.textPanel.setDocumentText("");
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(false);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(false);
		Dna.dna.gui.menuBar.closeFile.setEnabled(false);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(false);
		Dna.dna.gui.menuBar.networkButton.setEnabled(false);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(false);
		Dna.dna.gui.sidebarPanel.rm.clear();
	}

	public void newFile(String filename) {
		Dna.dna.db.openSQLite(filename);
		Dna.dna.gui.statusBar.resetLabel();
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
	}
}