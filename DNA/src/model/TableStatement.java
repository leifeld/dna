package model;

import java.awt.Color;
import java.time.LocalDateTime;

public class TableStatement {
	int id, coderId, documentId, start, stop;
	LocalDateTime dateTime;
	String text;
	Color statementTypeColor, coderColor;
	
	public TableStatement(int id, int coderId, int documentId, int start, int stop, LocalDateTime dateTime, String text, Color statementTypeColor, Color coderColor) {
		this.id = id;
		this.coderId = coderId;
		this.documentId = documentId;
		this.start = start;
		this.stop = stop;
		this.dateTime = dateTime;
		this.text = text;
		this.statementTypeColor = statementTypeColor;
		this.coderColor = coderColor;
	}

	public int getId() {
		return id;
	}

	public int getCoderId() {
		return coderId;
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

	public Color getCoderColor() {
		return coderColor;
	}
}
