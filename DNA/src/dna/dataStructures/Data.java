package dna.dataStructures;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import dna.Dna;

public class Data {
	public ArrayList<Statement> statements;
	public ArrayList<Document> documents;
	public ArrayList<Coder> coders;
	public ArrayList<Regex> regexes;
	public ArrayList<StatementType> statementTypes;
	public ArrayList<CoderRelation> coderRelations;
	public HashMap<String, String> settings;
	public ArrayList<StatementLink> statementLinks;
	public ArrayList<AttributeVector> attributes;
	
	public Data() {
		this.statements = new ArrayList<Statement>();
		this.documents = new ArrayList<Document>();
		this.coders = new ArrayList<Coder>();
		this.regexes = new ArrayList<Regex>();
		this.statementTypes = new ArrayList<StatementType>();
		this.coderRelations = new ArrayList<CoderRelation>();
		this.settings = new HashMap<String, String>();
		this.statementLinks = new ArrayList<StatementLink>();
		settings.put("activeCoder", "1");
		this.attributes = new ArrayList<AttributeVector>();
	}
	
	
	/*
	 * use Dna.dna.statementTypes.contains() instead!
	 */
	/*
	public boolean statementTypeExists(StatementType st) {
		for (int i = 0; i < statementTypes.size(); i++) {
			if (st.getLabel().equals(statementTypes.get(i).getLabel())) {
				System.out.println(" - Label matches");
				if (st.getColor().equals(statementTypes.get(i).getColor())) {
					System.out.println(" - Color matches");
					if (st.getVariables().equals(statementTypes.get(i).getVariables())) {
						System.out.println(" - Variables match");
						return true;
					}
				}
			}
		}
		return false;
	}
	*/
	
	
	/**
	 * Check if an attribute value already exists in the the attributes array list. Return its ID if found and -1 otherwise.
	 * 
	 * @param value            The attribute value as a string
	 * @param variable         The variable name as a string
	 * @param statementTypeId  The statement type ID as an int
	 * @return                 The ID of the attribute vector as an int; -1 if the attribute vector does not exist
	 */
	public int getAttributeId(String value, String variable, int statementTypeId) {
		for (int i = 0; i < getAttributes().size(); i++) {
			if (getAttributes().get(i).getValue().equals(value) && getAttributes().get(i).getVariable().equals(variable) 
					&& getAttributes().get(i).getStatementTypeId() == statementTypeId) {
				return getAttributes().get(i).getId();
			}
		}
		return -1;
	}

	/**
	 * Check if an attribute value already exists in the the attributes array list. Return its row index if found and -1 otherwise.
	 * 
	 * @param value            The attribute value as a string
	 * @param variable         The variable name as a string
	 * @param statementTypeId  The statement type ID as an int
	 * @return                 The row index of the attribute vector as an int; -1 if the attribute vector does not exist
	 */
	public int getAttributeIndex(String value, String variable, int statementTypeId) {
		for (int i = 0; i < getAttributes().size(); i++) {
			if (getAttributes().get(i).getValue().equals(value) && getAttributes().get(i).getVariable().equals(variable) 
					&& getAttributes().get(i).getStatementTypeId() == statementTypeId) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Retrieve an array of attribute vectors that have a certain statement type and variable.
	 * 
	 * @param variable         The variable name that should match
	 * @param statementTypeId  The statementTypeId that should match
	 * @return                 An array of AttributeVector objects
	 */
	public AttributeVector[] getAttributes(String variable, int statementTypeId) {
		ArrayList<AttributeVector> al = new ArrayList<AttributeVector>();
		for (int i = 0; i < getAttributes().size(); i++) {
			if (getAttributes().get(i).getVariable().equals(variable) && getAttributes().get(i).getStatementTypeId() == statementTypeId) {
				al.add(getAttributes().get(i));
			}
		}
		AttributeVector[] array = new AttributeVector[al.size()];
		for (int i = 0; i < al.size(); i++) {
			array[i] = al.get(i);
		}
		return array;
	}
	
	/**
	 * Delete an attribute vector from the data in the GUI.
	 * 
	 * @param attributeVectorId  The ID of the attribute vector to delete
	 */
	/*
	public void deleteAttributeVector(int attributeVectorId) {
		int row = -1;
		for (int i = 0; i < attributes.size(); i++) {
			if (attributes.get(i).getId() == attributeVectorId) {
				row = i;
				break;
			}
		}
		if (row < 0) {
			System.err.println("Attribute vector could not be identified.");
		} else {
			attributes.remove(row);
		}
	}
	*/
	
	/**
	 * @return the attributes
	 */
	public ArrayList<AttributeVector> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(ArrayList<AttributeVector> attributes) {
		this.attributes = attributes;
	}

	public void addRegex(Regex regex) {
		Dna.gui.rightPanel.rm.regexListModel.addElement(regex);
	}
	
	public void removeRegex(String label) {
		for (int i = Dna.gui.rightPanel.rm.regexListModel.getSize() - 1; i > -1; i--) {
			String currentLabel = ((Regex) Dna.gui.rightPanel.rm.regexListModel.getElementAt(i)).getLabel();
			if (currentLabel.equals(label)) {
				Dna.gui.rightPanel.rm.regexListModel.removeElement(i);
				break;
			}
		}
	}
	
	public String[] getStringEntries(int statementTypeId, String variableName) {
		String type = getStatementTypeById(statementTypeId).getVariables().get(variableName);
		ArrayList<Statement> subset = getStatementsByStatementTypeId(statementTypeId);
		ArrayList<String> entries = new ArrayList<String>();
		for (int i = 0; i < subset.size(); i++) {
			String mykey;
			if (type.equals("shorttext") || type.equals("longtext")) {
				mykey = (String) subset.get(i).getValues().get(variableName);
			} else {
				mykey = String.valueOf(subset.get(i).getValues().get(variableName));
			}
			if (!entries.contains(mykey)) {
				entries.add(mykey);
			}
		}
		Collections.sort(entries);
		String[] entriesArray = entries.toArray(new String[0]);
		return entriesArray;
	}
	
	public boolean[] getActiveStatementPermissions(int statementId) {
		int ac = getActiveCoder();
		boolean[] b = new boolean[4];
		if (this.getStatement(statementId).getCoder() == ac) {
			b[0] = true;
			b[1] = true;
			b[2] = true;
			b[3] = true;
		} else {
			for (int i = 0; i < coderRelations.size(); i++) {
				if (coderRelations.get(i).getCoder() == ac && coderRelations.get(i).getOtherCoder() == this.getStatement(statementId).getCoder()) {
					b[0] = coderRelations.get(i).isViewStatements();
					b[1] = coderRelations.get(i).isEditStatements();
					b[2] = coderRelations.get(i).isViewDocuments();
					b[3] = coderRelations.get(i).isEditDocuments();
					return b;
				}
			}
		}
		return b;
	}

	public boolean[] getActiveDocumentPermissions(int documentId) {
		int ac = getActiveCoder();
		boolean[] b = new boolean[2];
		if (this.getDocument(documentId).getCoder() == ac) {
			b[0] = true;
			b[1] = true;
		} else {
			for (int i = 0; i < coderRelations.size(); i++) {
				if (coderRelations.get(i).getCoder() == ac && coderRelations.get(i).getOtherCoder() == this.getDocument(documentId).getCoder()) {
					b[0] = coderRelations.get(i).isViewDocuments();
					b[1] = coderRelations.get(i).isEditDocuments();
					return b;
				}
			}
		}
		return b;
	}
	
	/**
	 * @return the activeCoder
	 */
	public int getActiveCoder() {
		String s = settings.get("activeCoder");
		return (int) Integer.valueOf(s);
	}
	
	/**
	 * @param activeCoder the activeCoder to set
	 */
	public void setActiveCoder(int activeCoder) {
		settings.put("activeCoder", Integer.toString(activeCoder));
	}
	
	/**
	 * @return the statementLinks
	 */
	public ArrayList<StatementLink> getStatementLinks() {
		return statementLinks;
	}

	/**
	 * @param statementLinks the statementLinks to set
	 */
	public void setStatementLinks(ArrayList<StatementLink> statementLinks) {
		this.statementLinks = statementLinks;
	}
	
	/**
	 * @return the regexes
	 */
	public ArrayList<Regex> getRegexes() {
		return regexes;
	}

	/**
	 * @param regexes the regexes to set
	 */
	public void setRegexes(ArrayList<Regex> regexes) {
		this.regexes = regexes;
	}

	public void addStatement(Statement statement) {
		statements.add(statement);
	}
	
	public void addDocument(Document document) {
		documents.add(document);
	}

	public void replaceCoder(Coder coder) {
		boolean found = false;
		for (int i = 0; i < coders.size(); i++) {
			if (coders.get(i).getId() == coder.getId()) {
				coders.set(i, coder);
				found = true;
				break;
			}
		}
		Collections.sort(coders);
		for (int i = 0; i < coderRelations.size(); i++) {
			if (coderRelations.get(i).getCoder() == coder.getId()) {
				if (coder.getPermissions().get("viewOthersStatements") == false) {
					coderRelations.get(i).setViewStatements(false);
				}
				if (coder.getPermissions().get("editOthersStatements") == false) {
					coderRelations.get(i).setEditStatements(false);
				}
				if (coder.getPermissions().get("viewOthersDocuments") == false) {
					coderRelations.get(i).setViewDocuments(false);
				}
				if (coder.getPermissions().get("editOthersDocuments") == false) {
					coderRelations.get(i).setEditDocuments(false);
				}
			}
		}
		if (found == false) {
			throw new NullPointerException("Coder with ID = " + coder.getId() + " not found.");
		}
	}
	
	public void addCoder(Coder coder) {
		coders.add(coder);
		int currentId = coder.getId();
		for (int i = 0; i < coders.size(); i++) {
			int remoteId = coders.get(i).getId();
			if (currentId != remoteId) {
				int crId = generateNewId("coderRelations");
				CoderRelation cr = new CoderRelation(crId, currentId, remoteId, true, true, true, true);
				coderRelations.add(cr);
				int crId2 = generateNewId("coderRelations");
				CoderRelation cr2 = new CoderRelation(crId2, remoteId, currentId, true, true, true, true);
				coderRelations.add(cr2);
			}
		}
		Collections.sort(coders);
	}
	
	public void removeCoder(int id) {
		for (int i = statements.size() - 1; i > -1; i--) {
			if (statements.get(i).getCoder() == id) {
				Dna.gui.rightPanel.statementPanel.ssc.remove(i);
			}
		}
		for (int i = documents.size() - 1; i > -1; i--) {
			if (documents.get(i).getCoder() == id) {
				Dna.gui.documentPanel.documentContainer.remove(i, false);
			}
		}
		int index = -1;
		for (int i = 0; i < coders.size(); i++) {
			if (coders.get(i).getId() == id) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			throw new NullPointerException("Coder not found.");
		}
		for (int i = coderRelations.size() - 1; i > -1; i--) {
			if (coderRelations.get(i).getCoder() == id || coderRelations.get(i).getOtherCoder() == id) {
				coderRelations.remove(i);
			}
		}
		coders.remove(index);
	}

	public void addStatementType(StatementType statementType) {
		statementTypes.add(statementType);
	}
	
	public Statement getStatement(int id) {
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getId() == id) {
				return(statements.get(i));
			}
		}
		return null;
	}
	
	public StatementType getStatementType(String label) {
		ArrayList<StatementType> s = new ArrayList<StatementType>();
		for (int i = 0; i < statementTypes.size(); i++) {
			if (statementTypes.get(i).getLabel().equals(label)) {
				s.add(statementTypes.get(i));
			}
		}
		if (s.size() > 1) {
			System.err.println("Multiple statement types with the same name were found. Using the first one with ID " + s.get(0).getId() + ".");
		}
		if (s.size() < 1) {
			return null;
		}
		return s.get(0);
	}
	
	public Color getStatementColor(int statementId) {
		if (settings.get("statementColor").equals("statementType")) {
			for (int i = 0; i < statementTypes.size(); i++) {
				if (statementTypes.get(i).getId() == this.getStatement(statementId).getStatementTypeId()) {
					return statementTypes.get(i).getColor();
				}
			}
		} else if (settings.get("statementColor").equals("coder")) {
			for (int i = 0; i < coders.size(); i++) {
				if (coders.get(i).getId() == this.getStatement(statementId).getCoder()) {
					return coders.get(i).getColor();
				}
			}
		}
		return null;
	}
	
	public ArrayList<Statement> getStatementsByStatementTypeId(int id) {
		ArrayList<Statement> s = new ArrayList<Statement>();
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getStatementTypeId() == id) {
				s.add(statements.get(i));
			}
		}
		return(s);
	}

	public Coder getCoderById(int id) {
		for (int i = 0; i < coders.size(); i++) {
			if (coders.get(i).getId() == id) {
				return coders.get(i);
			}
		}
		return(null);
	}

	public StatementType getStatementTypeById(int id) {
		for (int i = 0; i < statementTypes.size(); i++) {
			if (statementTypes.get(i).getId() == id) {
				return statementTypes.get(i);
			}
		}
		return(null);
	}
	
	public int generateNewId(String arrayList) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		if (arrayList.equals("statements")) {
			for (int i = 0; i < statements.size(); i++) {
				ids.add(statements.get(i).getId());
			}
		} else if (arrayList.equals("documents")) {
			for (int i = 0; i < documents.size(); i++) {
				ids.add(documents.get(i).getId());
			}
		} else if (arrayList.equals("coders")) {
			for (int i = 0; i < coders.size(); i++) {
				ids.add(coders.get(i).getId());
			}
		} else if (arrayList.equals("statementLinks")) {
			for (int i = 0; i < statementLinks.size(); i++) {
				ids.add(statementLinks.get(i).getId());
			}
		} else if (arrayList.equals("statementTypes")) {
			for (int i = 0; i < statementTypes.size(); i++) {
				ids.add(statementTypes.get(i).getId());
			}
		} else if (arrayList.equals("coderRelations")) {
			for (int i = 0; i < coderRelations.size(); i++) {
				ids.add(coderRelations.get(i).getId());
			}
		} else if (arrayList.equals("attributes")) {
			for (int i = 0; i < attributes.size(); i++) {
				ids.add(attributes.get(i).getId());
			}
		}
		Collections.sort(ids);
		int unused = 1;
		while (ids.contains(unused)) {
			unused++;
		}
		return unused;
	}
	
	public void removeStatement(int id) {
		for (int i = statements.size() - 1; i > -1; i--) {
			if (statements.get(i).getId() == id) {
				statements.remove(i);
			}
		}
	}
	
	public void addCoderRelation(CoderRelation coderRelation) {
		coderRelations.add(coderRelation);
	}

	/**
	 * @return the statements
	 */
	public ArrayList<Statement> getStatements() {
		return statements;
	}

	/**
	 * @param statements the statements to set
	 */
	public void setStatements(ArrayList<Statement> statements) {
		this.statements = statements;
	}

	/**
	 * @return the documents
	 */
	public ArrayList<Document> getDocuments() {
		return documents;
	}

	/**
	 * @param documents the documents to set
	 */
	public void setDocuments(ArrayList<Document> documents) {
		this.documents = documents;
	}

	public Document getDocument(int id) {
		for (int i = 0; i < documents.size(); i++) {
			if (documents.get(i).getId() == id) {
				return(documents.get(i));
			}
		}
		return null;
	}
	
	public void removeDocument(int documentId) {
		// remove statements
		Dna.gui.rightPanel.statementPanel.setRowSorterEnabled(false);
		for (int i = Dna.gui.rightPanel.statementPanel.ssc.size() - 1; i > -1; i--) {
			if (Dna.gui.rightPanel.statementPanel.ssc.get(i).getDocumentId() == documentId) {
				Dna.gui.rightPanel.statementPanel.ssc.remove(i);
			}
		}
		Dna.gui.rightPanel.statementPanel.setRowSorterEnabled(true);
		
		// remove document
		int documentModelIndex = Dna.gui.documentPanel.documentContainer.getModelIndexById(documentId);
		//int row = Dna.dna.gui.documentPanel.documentContainer.getRowIndexById(documentId);
		Dna.gui.documentPanel.setRowSorterEnabled(false);
		Dna.gui.documentPanel.documentContainer.remove(documentModelIndex, false);
		//Dna.dna.gui.documentPanel.documentContainer.remove(row);
		Dna.gui.documentPanel.setRowSorterEnabled(true);
		if (getDocuments().size() > 0) {
			Dna.gui.documentPanel.documentTable.setRowSelectionInterval(0, 0);
		}
		
		Dna.gui.textPanel.bottomCardPanel.attributePanel.attributeTableModel.fireTableDataChanged();
	}
	
	public void removeDocuments(ArrayList<Integer> documentIds, ArrayList<Integer> modelIndices) {
		// remove statements
		Dna.gui.rightPanel.statementPanel.setRowSorterEnabled(false);
		for (int i = Dna.gui.rightPanel.statementPanel.ssc.size() - 1; i > -1; i--) {
			if (documentIds.contains(Dna.gui.rightPanel.statementPanel.ssc.get(i).getDocumentId())) {
				Dna.gui.rightPanel.statementPanel.ssc.remove(i);
			}
		}
		Dna.gui.rightPanel.statementPanel.setRowSorterEnabled(true);
		
		// remove documents
		Dna.gui.documentPanel.setRowSorterEnabled(false);
		Collections.sort(modelIndices, Collections.reverseOrder());
		for (int i : modelIndices) {
			Dna.gui.documentPanel.documentContainer.remove(i, false);
		}
		
		// reset selection
		Dna.gui.documentPanel.setRowSorterEnabled(true);
		Dna.gui.documentPanel.documentTable.updateUI();
		if (getDocuments().size() > 0) {
			Dna.gui.documentPanel.documentTable.setRowSelectionInterval(0, 0);
		}
		
		Dna.gui.textPanel.bottomCardPanel.attributePanel.attributeTableModel.fireTableDataChanged();
	}
	
	/**
	 * @return the coders
	 */
	public ArrayList<Coder> getCoders() {
		return coders;
	}

	/**
	 * @param coders the coders to set
	 */
	public void setCoders(ArrayList<Coder> coders) {
		this.coders = coders;
	}
	
	/**
	 * @return the statementTypes
	 */
	public ArrayList<StatementType> getStatementTypes() {
		return statementTypes;
	}

	/**
	 * @param statementTypes the statementTypes to set
	 */
	public void setStatementTypes(ArrayList<StatementType> statementTypes) {
		this.statementTypes = statementTypes;
	}

	/**
	 * @return the coderRelations
	 */
	public ArrayList<CoderRelation> getCoderRelations() {
		return coderRelations;
	}

	/**
	 * @param coderRelations the coderRelations to set
	 */
	public void setCoderRelations(ArrayList<CoderRelation> coderRelations) {
		this.coderRelations = coderRelations;
	}

	/**
	 * @return the settings
	 */
	public HashMap<String, String> getSettings() {
		return settings;
	}

	/**
	 * @param settings the settings to set
	 */
	public void setSettings(HashMap<String, String> settings) {
		this.settings = settings;
	}
	
	/**
	 * @param documentId the ID of the document for which statements should be counted
	 * @return number of statements with the document ID that is handed over
	 */
	public int countStatementsPerDocument(int documentId) {
		int count = 0;
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getDocumentId() == documentId) {
				count++;
			}
		}
		return count;
	}
}
