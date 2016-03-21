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
	}
	
	public void addRegex(Regex regex) {
		Dna.dna.gui.rightPanel.rm.regexListModel.addElement(regex);
	}
	
	public void removeRegex(String label) {
		for (int i = Dna.dna.gui.rightPanel.rm.regexListModel.getSize() - 1; i > -1; i--) {
			String currentLabel = ((Regex) Dna.dna.gui.rightPanel.rm.regexListModel.getElementAt(i)).getLabel();
			if (currentLabel.equals(label)) {
				Dna.dna.gui.rightPanel.rm.regexListModel.removeElement(i);
				break;
			}
		}
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
		settings.put("activeCoder", (new Integer(activeCoder)).toString());
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

	public int generateNewStatementTypeId() {
		if (statementTypes.size() == 0) {
			return(1);
		}
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < statementTypes.size(); i++) {
			ids.add(statementTypes.get(i).getId());
		}
		Collections.sort(ids);
		if (ids.size() == 1) {
			if (ids.get(0) == 1) {
				return(2);
			} else {
				return(1);
			}
		}
		for (int i = 1; i < ids.size(); i++) {
			if (ids.get(i) - 1 > ids.get(i - 1)) {
				return(ids.get(i - 1) + 1);
			}
		}
		return(statementTypes.size() + 1);
	}
	
	public int generateNewStatementLinkId() {
		if (statementLinks.size() == 0) {
			return(1);
		}
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < statementLinks.size(); i++) {
			ids.add(statementLinks.get(i).getId());
		}
		Collections.sort(ids);
		if (ids.size() == 1) {
			if (ids.get(0) == 1) {
				return(2);
			} else {
				return(1);
			}
		}
		for (int i = 1; i < ids.size(); i++) {
			if (ids.get(i) - 1 > ids.get(i - 1)) {
				return(ids.get(i - 1) + 1);
			}
		}
		return(statementLinks.size() + 1);
	}

	public int generateNewCoderId() {
		if (coders.size() == 0) {
			return(1);
		}
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < coders.size(); i++) {
			ids.add(coders.get(i).getId());
		}
		Collections.sort(ids);
		if (ids.size() == 1) {
			if (ids.get(0) == 1) {
				return(2);
			} else {
				return(1);
			}
		}
		for (int i = 1; i < ids.size(); i++) {
			if (ids.get(i) - 1 > ids.get(i - 1)) {
				return(ids.get(i - 1) + 1);
			}
		}
		return(coders.size() + 1);
	}

	public int generateNewCoderRelationId() {
		if (coderRelations.size() == 0) {
			return(1);
		}
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < coderRelations.size(); i++) {
			ids.add(coderRelations.get(i).getId());
		}
		Collections.sort(ids);
		if (ids.size() == 1) {
			if (ids.get(0) == 1) {
				return(2);
			} else {
				return(1);
			}
		}
		for (int i = 1; i < ids.size(); i++) {
			if (ids.get(i) - 1 > ids.get(i - 1)) {
				return(ids.get(i - 1) + 1);
			}
		}
		return(coderRelations.size() + 1);
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
				int crId = generateNewCoderRelationId();
				CoderRelation cr = new CoderRelation(crId, currentId, remoteId, true, true, true, true);
				coderRelations.add(cr);
				int crId2 = generateNewCoderRelationId();
				CoderRelation cr2 = new CoderRelation(crId2, remoteId, currentId, true, true, true, true);
				coderRelations.add(cr2);
			}
		}
		Collections.sort(coders);
	}
	
	public void removeCoder(int id) {
		for (int i = documents.size() - 1; i > -1; i--) {
			if (documents.get(i).getCoder() == id) {
				Dna.dna.gui.documentPanel.documentContainer.remove(i);
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

	public void removeStatementType(String label) {
		for (int i = statements.size() - 1; i > -1; i--) {
			if (statementTypes.get(i).getLabel().equals(label)) {
				statementTypes.remove(i);
			}
		}
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
		for (int i = 0; i < statementTypes.size(); i++) {
			if (statementTypes.get(i).getLabel().equals(label)) {
				return(statementTypes.get(i));
			}
		}
		return(null);
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
	
	public int generateNewStatementId() {
		if (statements.size() == 0) {
			return(1);
		}
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < statements.size(); i++) {
			ids.add(statements.get(i).getId());
		}
		Collections.sort(ids);
		
		int sum = 0;
		for (int i = 0; i < ids.size(); i++) {
			if (ids.get(i) != 0) {
		         sum += ids.get(i);
		    }
		}
		int total = (ids.size() + 1) * ids.size() / 2;
		return total - sum;
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

	public int generateNewDocumentId() {
		if (documents.size() == 0) {
			return(1);
		}
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < documents.size(); i++) {
			ids.add(documents.get(i).getId());
		}
		Collections.sort(ids);
		
		int sum = 0;
		for (int i = 0; i < ids.size(); i++) {
			if (ids.get(i) != 0) {
		         sum += ids.get(i);
		    }
		}
		int total = (ids.size() + 1) * ids.size() / 2;
		return total - sum;
	}

	public void removeDocument(int documentId) {
		// remove statements
		Dna.dna.gui.rightPanel.setRowSorterEnabled(false);
		for (int i = Dna.dna.gui.rightPanel.ssc.size() - 1; i > -1; i--) {
			if (Dna.dna.gui.rightPanel.ssc.get(i).getDocumentId() == documentId) {
				Dna.dna.gui.rightPanel.ssc.remove(i);
			}
		}
		Dna.dna.gui.rightPanel.setRowSorterEnabled(true);
		
		// remove document
		int row = Dna.dna.gui.documentPanel.documentContainer.getRowIndexById(documentId);
		Dna.dna.gui.documentPanel.setRowSorterEnabled(false);
		Dna.dna.gui.documentPanel.documentContainer.remove(row);
		Dna.dna.gui.documentPanel.setRowSorterEnabled(true);
		if (Dna.data.getDocuments().size() > 0) {
			Dna.dna.gui.documentPanel.documentTable.setRowSelectionInterval(0, 0);
		}
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
}
