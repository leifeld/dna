package model;


import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Statement {
	int id;
	int start, stop;
	int statementTypeId;
	String statementTypeLabel;
	Color statementTypeColor;
	int coderId;
	String coderName;
	Color coderColor;
	ArrayList<Value> values;
	int documentId;
	String text;
	LocalDateTime dateTime;
	
	/**
	 * Create a statement. Full constructor with all fields.
	 * 
	 * @param id                  The ID of the statement.
	 * @param start               Start position in the text.
	 * @param stop                End position in the text.
	 * @param statementTypeId     Statement type ID.
	 * @param statementTypeLabel  State type label.
	 * @param statementTypeColor  Statement type color.
	 * @param coderId             ID of the coder.
	 * @param coderName           Name of the coder.
	 * @param coderColor          Color of the coder.
	 * @param values              An array list with variable contents.
	 * @param documentId          The document ID.
	 * @param text                The selected text.
	 * @param dateTime            The date and time of the statement.
	 */
	public Statement(int id,
			int start,
			int stop,
			int statementTypeId,
			String statementTypeLabel,
			Color statementTypeColor,
			int coderId,
			String coderName,
			Color coderColor,
			ArrayList<Value> values,
			int documentId,
			String text,
			LocalDateTime dateTime) {
		this.id = id;
		this.start = start;
		this.stop = stop;
		this.statementTypeId = statementTypeId;
		this.statementTypeLabel = statementTypeLabel;
		this.statementTypeColor = statementTypeColor;
		this.coderId = coderId;
		this.coderName = coderName;
		this.coderColor = coderColor;
		this.values = values;
		this.documentId = documentId;
		this.text = text;
		this.dateTime = dateTime;
	}

	/**
	 * Create a new statement. Light version for inserting an empty statement
	 * into the database.
	 * 
	 * @param start            Start position in the text.
	 * @param stop             End position in the text.
	 * @param statementTypeId  Statement type ID.
	 * @param coderId          The ID of the coder who owns the statement.
	 * @param values           An array list with variable contents.
	 */
	public Statement(int start, int stop, int statementTypeId, int coderId, ArrayList<Value> values) {
		this.start = start;
		this.stop = stop;
		this.statementTypeId = statementTypeId;
		this.coderId = coderId;
		this.values = values;
	}

	public String getCoderName() {
		return coderName;
	}

	public Color getCoderColor() {
		return coderColor;
	}

	public int getDocumentId() {
		return documentId;
	}

	public String getText() {
		return text;
	}

	public String getStatementTypeLabel() {
		return statementTypeLabel;
	}
	
	public void setStatementTypeLabel(String statementTypeLabel) {
		this.statementTypeLabel = statementTypeLabel;
	}
	
	public Color getStatementTypeColor() {
		return statementTypeColor;
	}
	
	public void setStatementTypeColor(Color statementTypeColor) {
		this.statementTypeColor = statementTypeColor;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getCoderId() {
		return coderId;
	}
	
	void setCoderId(int coderId) {
		this.coderId = coderId;
	}
	
	public int getStart() {
		return start;
	}
	
	void setStart(int start) {
		this.start = start;
	}
	
	public int getStop() {
		return stop;
	}
	
	void setStop(int stop) {
		this.stop = stop;
	}
	
	public int getStatementTypeId() {
		return statementTypeId;
	}
	
	void setStatementTypeId(int statementTypeId) {
		this.statementTypeId = statementTypeId;
	}
	
	public ArrayList<Value> getValues() {
		return values;
	}
	
	void setValues(ArrayList<Value> values) {
		this.values = values;
	}
}