package dna.dataStructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Data {
	public ArrayList<Statement> statements;
	public ArrayList<Document> documents;
	public ArrayList<Coder> coders;
	public ArrayList<Regex> regexes;
	public ArrayList<StatementType> statementTypes;
	public ArrayList<CoderRelation> coderRelations;
	public HashMap<String, String> settings;
	public ArrayList<StatementLink> statementLinks;
	int activeCoder = 1;
	
	/*
	public Data(ArrayList<Statement> statements, ArrayList<Document> documents, ArrayList<Coder> coders,
			ArrayList<Regex> regexes, ArrayList<StatementType> statementTypes, ArrayList<CoderRelation> coderRelations,
			HashMap<String, String> settings, ArrayList<StatementLink> statementLinks) {
		super();
		this.statements = statements;
		this.documents = documents;
		this.coders = coders;
		this.regexes = regexes;
		this.statementTypes = statementTypes;
		this.coderRelations = coderRelations;
		this.settings = settings;
		this.statementLinks = statementLinks;
	}
	*/
	
	public Data() {
		this.statements = new ArrayList<Statement>();
		this.documents = new ArrayList<Document>();
		this.coders = new ArrayList<Coder>();
		this.regexes = new ArrayList<Regex>();
		this.statementTypes = new ArrayList<StatementType>();
		this.coderRelations = new ArrayList<CoderRelation>();
		this.settings = new HashMap<String, String>();
		this.statementLinks = new ArrayList<StatementLink>();
		this.activeCoder = 1;
	}
	
	/**
	 * @return the activeCoder
	 */
	public int getActiveCoder() {
		return activeCoder;
	}

	/**
	 * @param activeCoder the activeCoder to set
	 */
	public void setActiveCoder(int activeCoder) {
		this.activeCoder = activeCoder;
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

	public void addCoder(Coder coder) {
		coders.add(coder);
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
	
	public ArrayList<Statement> getStatementsByStatementTypeId(int id) {
		ArrayList<Statement> s = new ArrayList<Statement>();
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getStatementTypeId() == id) {
				s.add(statements.get(i));
			}
		}
		return(s);
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
		return(statements.size() + 1);
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
		return(documents.size() + 1);
	}

	public void removeDocument(int id) {
		for (int i = documents.size() - 1; i > -1; i--) {
			if (documents.get(i).getId() == id) {
				documents.remove(i);
			}
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
