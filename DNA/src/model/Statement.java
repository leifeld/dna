package model;


import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Represents a statement. There are two constructors: one for a basic statement
 * version that contains only the data necessary for inserting an empty
 * statement into the database and one with all data necessary for opening a
 * statement popup window.
 */
public class Statement implements Comparable<Statement> {
	private int id;
	private int start, stop;
	private int statementTypeId;
	private String statementTypeLabel;
	private Color statementTypeColor;
	private int coderId;
	private String coderName;
	private Color coderColor;
	private ArrayList<Value> values;
	private int documentId;
	private String text;
	private LocalDateTime dateTime;
	
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

	/**
	 * Get the coder name.
	 * 
	 * @return The coder name.
	 */
	public String getCoderName() {
		return coderName;
	}

	/**
	 * Get the coder color.
	 * 
	 * @return The coder color.
	 */
	public Color getCoderColor() {
		return coderColor;
	}

	/**
	 * Get the document ID.
	 * 
	 * @return The ID of the document the statement is nested in.
	 */
	public int getDocumentId() {
		return documentId;
	}

	/**
	 * Get the document text portion highlighted by the statement.
	 * 
	 * @return A text portion from the document as a String.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Get the statement type label.
	 * 
	 * @return The name or label of the statement type associated with the
	 *   statement.
	 */
	public String getStatementTypeLabel() {
		return statementTypeLabel;
	}
	
	/**
	 * Get the color of the statement type associated with the statement.
	 * 
	 * @return The statement type color of the statement.
	 */
	public Color getStatementTypeColor() {
		return statementTypeColor;
	}
	
	/**
	 * Get the statement ID.
	 * 
	 * @return The ID of the statement.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Get the ID of the coder who owns the statement.
	 * 
	 * @return The coder ID of the statement.
	 */
	public int getCoderId() {
		return coderId;
	}
	
	/**
	 * Set the coder ID of the statement.
	 * 
	 * @param coderId The new coder ID for the statement.
	 */
	void setCoderId(int coderId) {
		this.coderId = coderId;
	}
	
	/**
	 * Get the start coordinate of the statement in the document.
	 * 
	 * @return The character start position in the text.
	 */
	public int getStart() {
		return start;
	}
	
	/**
	 * Set the start coordinate of the statement in the document.
	 * 
	 * @param start The new character start position in the text.
	 */
	void setStart(int start) {
		this.start = start;
	}

	/**
	 * Get the end coordinate of the statement in the document.
	 * 
	 * @return The character end position in the text.
	 */
	public int getStop() {
		return stop;
	}

	/**
	 * Set the end coordinate of the statement in the document.
	 * 
	 * @param stop The new character end position in the text.
	 */
	void setStop(int stop) {
		this.stop = stop;
	}
	
	/**
	 * Get the statement type ID of the statement.
	 * 
	 * @return The statement type ID.
	 */
	public int getStatementTypeId() {
		return statementTypeId;
	}
	
	/**
	 * Get the date/time stored in the statement.
	 * 
	 * @return The date/time as a {@link LocalDateTime} object.
	 */
	LocalDateTime getDateTime() {
		return dateTime;
	}

	/**
	 * Get the values of the variables stored in the statement.
	 * 
	 * @return An array list of {@link model.Value Value} objects
	 *   corresponding to the variables specified in the corresponding statement
	 *   type.
	 */
	public ArrayList<Value> getValues() {
		return values;
	}
	
	/**
	 * Set the values of the variables stored in the statement.
	 * 
	 * @param values An array list of {@link model.Value Value} objects
	 *   corresponding to the variables specified in the corresponding statement
	 *   type.
	 */
	void setValues(ArrayList<Value> values) {
		this.values = values;
	}

	/**
	 * Implementation of the {@link java.lang.Comparable Comparable} interface
	 * to sort statements in the statement table and possibly elsewhere.
	 */
	@Override
	public int compareTo(Statement s) {
		if (this.dateTime != null && this.getDateTime().isBefore(s.getDateTime())) {
			return -1;
		} else if (this.getDocumentId() == s.getDocumentId() && this.getStart() < s.getStart()) {
			return -1;
		} else {
			return 1;
		}
	}
}