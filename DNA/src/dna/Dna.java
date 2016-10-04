package dna;

import dna.dataStructures.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.swing.ImageIcon;

public class Dna {
	public static Data data = new Data();
	public static Dna dna;
	public Gui gui;
	public SqlConnection sql;
	public String version, date;
	PrintStream console;
	
	public Dna() {
		date = "2016-10-03";
		version = "2.0 beta 14";
		System.out.println("DNA version: " + version + " (" + date + ")");
		System.out.println("Java version: " + System.getProperty("java.version"));
		System.out.println("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
		console = System.err;
		
		gui = new Gui();
	}
	
	public static void main (String[] args) {
		dna = new Dna();
	}

	public void addDocument(Document document) {
		gui.documentPanel.setRowSorterEnabled(false);
		gui.documentPanel.documentContainer.addDocument(document);
		gui.documentPanel.setRowSorterEnabled(true);
		sql.upsertDocument(document);
	}
	
	public void removeDocument(int documentId) {
		Dna.data.removeDocument(documentId);
		sql.removeDocument(documentId);
	}
	
	public void removeDocuments(int[] documentRows) {
		// compile lists of model indices and document IDs
		ArrayList<Integer> docIds = new ArrayList<Integer>();
		ArrayList<Integer> modelIndices = new ArrayList<Integer>();
		for (int i = 0; i < documentRows.length; i++) {
			int modelIndex = Dna.dna.gui.documentPanel.documentTable.convertRowIndexToModel(documentRows[i]);
			modelIndices.add(modelIndex);
			int docId = Dna.dna.gui.documentPanel.documentContainer.getIdByModelIndex(modelIndex);
			docIds.add(docId);
		}
		
		// remove documents in GUI
		Dna.data.removeDocuments(docIds, modelIndices);
		
		// remove documents in SQL database
		sql.removeDocuments(docIds);
	}
	
	public void removeStatement(int statementId) {
		gui.rightPanel.statementPanel.ssc.removeStatement(statementId);
		sql.removeStatement(statementId);
	}
	
	public void addStatement(Statement statement) {
		gui.rightPanel.statementPanel.setRowSorterEnabled(false);
		gui.rightPanel.statementPanel.ssc.addStatement(statement);
		gui.rightPanel.statementPanel.setRowSorterEnabled(true);
		int statementTypeId = statement.getStatementTypeId();
		LinkedHashMap<String, String> map = data.getStatementTypeById(statementTypeId).getVariables();
		sql.addStatement(statement, map);
	}
	
	public void updateVariable(int statementId, int statementTypeId, Object content, String variable) {
		StatementType st = data.getStatementTypeById(statementTypeId);
		String dataType = st.getVariables().get(variable);
		try {
			sql.upsertVariableContent(content, statementId, variable, statementTypeId, dataType);
			Dna.data.getStatement(statementId).getValues().put(variable, content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addCoder(Coder coder) {
		data.addCoder(coder);
		sql.addCoder(coder);
	}
	
	public void replaceCoder(Coder coder) {
		data.replaceCoder(coder);
		sql.upsertCoder(coder);
		
	}
	
	public void setActiveCoder(int activeCoder) {
		data.setActiveCoder(activeCoder);
		sql.upsertSetting("activeCoder", (new Integer(activeCoder)).toString());
	}
	
	public void addRegex(Regex regex) {
		data.addRegex(regex);
		sql.upsertRegex(regex);
	}
	
	public void removeRegex(String label) {
		data.removeRegex(label);
		sql.removeRegex(label);
	}
	
	public void addAttributeVector(AttributeVector av) {
		data.getAttributes().add(av);
		sql.upsertAttributeVector(av);
	}
	
	public void closeDatabase() {
		data = new Data();
		sql.closeConnection();
		sql = null;
		Dna.dna.gui.leftPanel.coderPanel.clear();
		Dna.dna.gui.statusBar.resetLabel();
		Dna.dna.gui.documentPanel.documentContainer.clear();
		Dna.dna.gui.rightPanel.statementPanel.ssc.clear();
		Dna.dna.gui.textPanel.setDocumentText("");
		Dna.dna.gui.menuBar.openDatabase.setEnabled(true);
		Dna.dna.gui.menuBar.newDatabase.setEnabled(true);
		Dna.dna.gui.menuBar.colorCoderButton.setIcon(new ImageIcon(getClass().getResource("/icons/tick.png")));
		Dna.dna.gui.menuBar.colorCoderButton.setEnabled(false);
		Dna.dna.gui.menuBar.colorStatementTypeButton.setEnabled(false);
		//Dna.dna.gui.menuBar.typeEditorButton.setEnabled(false);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(false);
		Dna.dna.gui.menuBar.importTextButton.setEnabled(false);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(false);
		Dna.dna.gui.menuBar.importDnaButton.setEnabled(false);
		Dna.dna.gui.menuBar.recodeMetaData.setEnabled(false);
		Dna.dna.gui.menuBar.networkButton.setEnabled(false);
		Dna.dna.gui.menuBar.closeDatabase.setEnabled(false);
		Dna.dna.gui.menuBar.networkButton.setEnabled(false);
		//Dna.dna.gui.rightPanel.statementPanel.updateStatementTypes();  //TODO: reimplement
		Dna.dna.gui.rightPanel.rm.addButton.setEnabled(false);
		Dna.dna.gui.rightPanel.rm.clear();
		//Dna.dna.gui.rightPanel.linkedTableModel.setRowCount(0);
		Dna.dna.gui.rightPanel.rm.regexListModel.updateList();
		Dna.dna.gui.rightPanel.rm.setFieldsEnabled(false);
		Dna.dna.gui.leftPanel.docStats.clear();
		Dna.dna.gui.leftPanel.docStats.refreshButton.setEnabled(false);
		Dna.dna.gui.rightPanel.statementPanel.model.clear();
		Dna.dna.gui.rightPanel.statementPanel.typeComboBox.setEnabled(false);
		Dna.dna.gui.rightPanel.statementPanel.statementFilter.showAll.doClick();
	}
}