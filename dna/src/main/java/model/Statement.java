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
	private int coderId;
	private int documentId;
	private LocalDateTime dateTime;
	
	/**
	 * Create a statement. Full constructor with all fields.
	 * 
	 * @param id                  The ID of the statement.
	 * @param start               Start position in the text.
	 * @param stop                End position in the text.
	 * @param statementTypeId     Statement type ID.
	 * @param coderId             ID of the coder.
	 * @param documentId          The document ID.
	 * @param dateTime            The date and time of the statement.
	 */
	public Statement(int id,
			int start,
			int stop,
			int statementTypeId,
			int coderId,
			int documentId,
			LocalDateTime dateTime) {
		this.id = id;
		this.start = start;
		this.stop = stop;
		this.statementTypeId = statementTypeId;
		this.coderId = coderId;
		this.documentId = documentId;
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
	 */
	public Statement(int start, int stop, int statementTypeId, int coderId) {
		this.start = start;
		this.stop = stop;
		this.statementTypeId = statementTypeId;
		this.coderId = coderId;
	}
	
	/**
	 * Copy constructor. Creates a deep copy/clone of another statement.
	 * 
	 * @param statement  The other statement to clone.
	 */
	public Statement(Statement statement) {
		this.id = statement.getId();
		this.start = statement.getStart();
		this.stop = statement.getStop();
		this.statementTypeId = statement.getStatementTypeId();
		this.coderId = statement.getCoderId();
		this.documentId = statement.getDocumentId();
		this.dateTime = statement.getDateTime();
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
	 * Set the document ID.
	 * 
	 * @param documentId The document ID to set.
	 */
	public void setDocumentId(int documentId) {
		this.documentId = documentId;
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
	 * Set the statement ID.
	 * 
	 * @param id The ID to set.
	 */
	public void setId(int id) {
		this.id = id;
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
	public void setCoderId(int coderId) {
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
	public LocalDateTime getDateTime() {
		return dateTime;
	}

	/**
	 * Set the date/time of the corresponding document.
	 * 
	 * @param dateTime The new date/time to set.
	 */
	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	/**
	 * Implementation of the {@link java.lang.Comparable Comparable} interface
	 * to sort statements in the statement table and possibly elsewhere.
	 */
	@Override
	public int compareTo(Statement s) {
		if (this.dateTime != null) {
			if (this.getDateTime().isBefore(s.getDateTime())) {
				return -1;
			} else if (this.getDateTime().isAfter(s.getDateTime())) {
				return 1;
			}
		}
		if (this.getDocumentId() < s.getDocumentId()) {
			return -1;
		} else if (this.getDocumentId() > s.getDocumentId()) {
			return 1;
		}
		if (this.getStart() < s.getStart()) {
			return -1;
		} else if (this.getStart() > s.getStart()) {
			return 1;
		}
		if (this.getStop() < s.getStop()) {
			return -1;
		} else if (this.getStop() > s.getStop()) {
			return 1;
		}
		if (this.getId() < s.getId()) {
			return -1;
		} else {
			return 1;
		}
	}
}