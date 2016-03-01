package dna.dataStructures;

import java.util.ArrayList;

public class Data {
	public ArrayList<Statement> statements;
	public ArrayList<Document> documents;
	public ArrayList<Coder> coders;
	public ArrayList<Regex> regexes;
	public ArrayList<StatementType> statementTypes;
	public ArrayList<CoderRelation> coderRelations;
	public ArrayList<Setting> settings;
	
	public Data(ArrayList<Statement> statements, ArrayList<Document> documents, ArrayList<Coder> coders,
			ArrayList<Regex> regexes, ArrayList<StatementType> statementTypes, ArrayList<CoderRelation> coderRelations,
			ArrayList<Setting> settings) {
		super();
		this.statements = statements;
		this.documents = documents;
		this.coders = coders;
		this.regexes = regexes;
		this.statementTypes = statementTypes;
		this.coderRelations = coderRelations;
		this.settings = settings;
	}
	
	public Data() {
		this.statements = new ArrayList<Statement>();
		this.documents = new ArrayList<Document>();
		this.coders = new ArrayList<Coder>();
		this.regexes = new ArrayList<Regex>();
		this.statementTypes = new ArrayList<StatementType>();
		this.coderRelations = new ArrayList<CoderRelation>();
		this.settings = new ArrayList<Setting>();
	}

	/*
	public void addStatement(Statement statement) {
		statements.add(statement);
	}

	public void addDocument(Document document) {
		documents.add(document);
	}

	public void addCoder(Coder coder) {
		coders.add(coder);
	}

	public void addRegex(Regex regex) {
		regexes.add(regex);
	}

	public void addStatementType(StatementType statementType) {
		statementTypes.add(statementType);
	}

	public void addCoderRelation(CoderRelation coderRelation) {
		coderRelations.add(coderRelation);
	}

	public void addSetting(Setting setting) {
		settings.add(setting);
	}
	*/
	
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
	public ArrayList<Setting> getSettings() {
		return settings;
	}

	/**
	 * @param settings the settings to set
	 */
	public void setSettings(ArrayList<Setting> settings) {
		this.settings = settings;
	}
}
