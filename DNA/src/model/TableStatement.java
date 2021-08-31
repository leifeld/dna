package model;

import java.awt.Color;
import java.time.LocalDateTime;

public class TableStatement {
	int id, documentId, start, stop;
	Coder coder;
	LocalDateTime dateTime;
	String text;
	Color statementTypeColor;
	
	public TableStatement(int id, Coder coder, int documentId, int start, int stop, LocalDateTime dateTime, String text, Color statementTypeColor) {
		this.id = id;
		this.coder = coder;
		this.documentId = documentId;
		this.start = start;
		this.stop = stop;
		this.dateTime = dateTime;
		this.text = text;
		this.statementTypeColor = statementTypeColor;
	}

	public int getId() {
		return id;
	}

	public Coder getCoder() {
		return coder;
	}

	public int getDocumentId() {
		return documentId;
	}

	public int getStart() {
		return start;
	}

	public int getStop() {
		return stop;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public String getText() {
		return text;
	}

	public Color getStatementTypeColor() {
		return statementTypeColor;
	}
}
