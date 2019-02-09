package dna;

import dna.dataStructures.*;

import java.awt.Color;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.swing.ImageIcon;

public class Dna {
	public static Data data = new Data();
	public static Dna dna;
	public static Gui gui;
	public SqlConnection sql;
	public String version, date;
	PrintStream console;
	
	public Dna() {
		date = "2019-02-09";
		version = "2.0 beta 24";
	}
	
	public static void main (String[] args) {
		dna = new Dna();
		System.out.println("DNA version: " + dna.version + " (" + dna.date + ")");
		System.out.println("Java version: " + System.getProperty("java.version"));
		System.out.println("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
		dna.console = System.err;
		gui = new Gui();

		if (args.length == 1) {
			new OpenDatabaseDialog(args[0]);
		} else if (args.length == 3) {
			new OpenDatabaseDialog(args[0], args[1], args[2]);
		} else if (args.length > 0) {
			System.err.println("A maximum of three startup arguments is recognized.");
		}
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
			int modelIndex = Dna.gui.documentPanel.documentTable.convertRowIndexToModel(documentRows[i]);
			modelIndices.add(modelIndex);
			int docId = Dna.gui.documentPanel.documentContainer.getIdByModelIndex(modelIndex);
			docIds.add(docId);
		}
		
		// remove documents in GUI
		Dna.data.removeDocuments(docIds, modelIndices);
		
		// remove documents in SQL database
		sql.removeDocuments(docIds);
	}

	/**
	 * Set the time (hours, minutes, second, milliseconds) of a document date to zero for multiple documents.
	 * 
	 * @param documentRows  The indices of the documents to update in the document array list
	 */
	public void resetTimeOfDocuments(int[] documentRows) {
		// compile lists of model indices and document IDs
		ArrayList<Integer> docIds = new ArrayList<Integer>();
		ArrayList<Integer> modelIndices = new ArrayList<Integer>();
		for (int i = 0; i < documentRows.length; i++) {
			int modelIndex = Dna.gui.documentPanel.documentTable.convertRowIndexToModel(documentRows[i]);
			modelIndices.add(modelIndex);
			int docId = Dna.gui.documentPanel.documentContainer.getIdByModelIndex(modelIndex);
			docIds.add(docId);
		}
		
		// change GUI documents
		ArrayList<Date> newDates = new ArrayList<Date>();
		for (int i = 0; i < modelIndices.size(); i++) {
			Date d = data.getDocuments().get(modelIndices.get(i)).getDate();
			Calendar calendar = Calendar.getInstance();
	        calendar.setTime(d);
	        calendar.set(Calendar.MILLISECOND, 0);
	        calendar.set(Calendar.SECOND, 0);
	        calendar.set(Calendar.MINUTE, 0);
	        calendar.set(Calendar.HOUR, 0);
	        calendar.set(Calendar.AM_PM, 0);
	        d = calendar.getTime();
	        data.getDocuments().get(modelIndices.get(i)).setDate(d);
	        newDates.add(d);
		}
		
		// change SQL documents
		sql.updateDocumentDates(docIds, newDates);
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
	
	public void addRegex(Regex regex) {
		data.addRegex(regex);
		sql.upsertRegex(regex);
	}
	
	public void removeRegex(String label) {
		data.removeRegex(label);
		sql.removeRegex(label);
	}
	
	/**
	 * Add a new attribute vector. An attribute vector is an entry of a variable coupled with a color and some meta-data like type or alias.
	 * 
	 * @param av  The attribute vector
	 */
	public void addAttributeVector(AttributeVector av) {
		data.getAttributes().add(av);
		gui.textPanel.bottomCardPanel.attributePanel.attributeTableModel.sort();
		sql.upsertAttributeVector(av);
	}
	
	/**
	 * Delete an existing attribute vector. An attribute vector is an entry of a variable coupled with a color and some meta-data like type or alias.
	 * 
	 * @param row  Index of the attribute vector in the table/data structure
	 */
	public void deleteAttributeVector(int row) {
		int attributeVectorId = Dna.gui.textPanel.bottomCardPanel.attributePanel.attributeTableModel.get(row).getId();
		Dna.data.getAttributes().remove(row);
		sql.deleteAttributeVector(attributeVectorId);
	}
	
	/**
	 * Update the color of an existing attribute vector.
	 * 
	 * @param row    Index of the attribute vector
	 * @param color  The new color
	 */
	public void updateAttributeColor(int row, Color color) {
		data.getAttributes().get(row).setColor(color);
		sql.updateAttributeColor(data.getAttributes().get(row).getId(), color);
	}

	/**
	 * Update the Value field of an existing attribute vector.
	 * 
	 * @param row    Index of the attribute vector
	 * @param value  String containing the value
	 */
	public void updateAttributeValue(int row, String value) {
		data.getAttributes().get(row).setValue(value);
		sql.updateAttribute(data.getAttributes().get(row).getId(), "Value", value);
	}

	/**
	 * Update the Type field of an existing attribute vector.
	 * 
	 * @param row    Index of the attribute vector
	 * @param type  String containing the type
	 */
	public void updateAttributeType(int row, String type) {
		data.getAttributes().get(row).setType(type);
		sql.updateAttribute(data.getAttributes().get(row).getId(), "Type", type);
	}

	/**
	 * Update the Alias field of an existing attribute vector.
	 * 
	 * @param row    Index of the attribute vector
	 * @param alias  String containing the alias
	 */
	public void updateAttributeAlias(int row, String alias) {
		data.getAttributes().get(row).setAlias(alias);
		sql.updateAttribute(data.getAttributes().get(row).getId(), "Alias", alias);
	}

	/**
	 * Update the Notes field of an existing attribute vector.
	 * 
	 * @param row    Index of the attribute vector
	 * @param notes  String containing the notes
	 */
	public void updateAttributeNotes(int row, String notes) {
		data.getAttributes().get(row).setNotes(notes);
		sql.updateAttribute(data.getAttributes().get(row).getId(), "Notes", notes);
	}
	
	public void closeDatabase() {
		data = new Data();
		sql.closeConnection();
		sql = null;
		Dna.gui.leftPanel.coderPanel.clear();
		Dna.gui.statusBar.resetLabel();
		Dna.gui.textPanel.bottomCardPanel.attributePanel.attributeTableModel.clear();
		Dna.gui.textPanel.bottomCardPanel.attributePanel.typeComboBox.updateUI();
		Dna.gui.textPanel.bottomCardPanel.attributePanel.entryBox.updateUI();
		Dna.gui.textPanel.bottomCardPanel.attributePanel.typeComboBox.setEnabled(false);
		Dna.gui.textPanel.bottomCardPanel.attributePanel.entryBox.setEnabled(false);
		Dna.gui.textPanel.bottomCardPanel.attributePanel.addMissingButton.setEnabled(false);
		Dna.gui.textPanel.bottomCardPanel.attributePanel.typeComboBox.setEnabled(false);
		Dna.gui.textPanel.bottomCardPanel.attributePanel.entryBox.setEnabled(false);
		Dna.gui.textPanel.bottomCardPanel.attributePanel.cleanUpButton.setEnabled(false);
		Dna.gui.textPanel.bottomCardPanel.recodePanel.updateBoxes();
		Dna.gui.textPanel.bottomCardPanel.recodePanel.typeComboBox.updateUI();
		Dna.gui.textPanel.bottomCardPanel.recodePanel.entryBox.updateUI();
		Dna.gui.textPanel.bottomCardPanel.recodePanel.tableModel.setRowCount(0);
		Dna.gui.textPanel.bottomCardPanel.recodePanel.listModel.clear();
		Dna.gui.textPanel.bottomCardPanel.recodePanel.applyButton.setEnabled(false);
		Dna.gui.textPanel.bottomCardPanel.recodePanel.resetButton.setEnabled(false);
		Dna.gui.textPanel.bottomCardPanel.recodePanel.typeComboBox.setEnabled(false);
		Dna.gui.textPanel.bottomCardPanel.recodePanel.entryBox.setEnabled(false);
		Dna.gui.documentPanel.documentContainer.clear();
		Dna.gui.rightPanel.statementPanel.ssc.clear();
		Dna.gui.textPanel.setDocumentText("");
		Dna.gui.menuBar.openDatabase.setEnabled(true);
		Dna.gui.menuBar.newDatabase.setEnabled(true);
		Dna.gui.menuBar.colorCoderButton.setIcon(new ImageIcon(getClass().getResource("/icons/tick.png")));
		Dna.gui.menuBar.colorCoderButton.setEnabled(false);
		Dna.gui.menuBar.colorStatementTypeButton.setEnabled(false);
		//Dna.dna.gui.menuBar.typeEditorButton.setEnabled(false);
		Dna.gui.menuBar.newDocumentButton.setEnabled(false);
		Dna.gui.menuBar.importTextButton.setEnabled(false);
		Dna.gui.menuBar.importOldButton.setEnabled(false);
		Dna.gui.menuBar.importDnaButton.setEnabled(false);
		Dna.gui.menuBar.recodeMetaData.setEnabled(false);
		Dna.gui.menuBar.networkButton.setEnabled(false);
		Dna.gui.menuBar.closeDatabase.setEnabled(false);
		Dna.gui.menuBar.networkButton.setEnabled(false);
		//Dna.dna.gui.rightPanel.statementPanel.updateStatementTypes();  //TODO: reimplement
		Dna.gui.rightPanel.rm.addButton.setEnabled(false);
		Dna.gui.rightPanel.rm.clear();
		//Dna.dna.gui.rightPanel.linkedTableModel.setRowCount(0);
		Dna.gui.rightPanel.rm.regexListModel.updateList();
		Dna.gui.rightPanel.rm.setFieldsEnabled(false);
		Dna.gui.leftPanel.docStats.clear();
		Dna.gui.leftPanel.docStats.refreshButton.setEnabled(false);
		Dna.gui.rightPanel.statementPanel.model.clear();
		Dna.gui.rightPanel.statementPanel.typeComboBox.setEnabled(false);
		Dna.gui.rightPanel.statementPanel.statementFilter.showAll.setSelected(true);
		Dna.gui.rightPanel.statementPanel.statementFilter.showAll.doClick();
	}
}