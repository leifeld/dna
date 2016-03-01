package dna.dataStructures;

import java.awt.Color;
import java.util.Date;

public class Statement implements Comparable<Statement> {
	int id, document, start, stop;
	Date date;
	Color color;
	String type;
	int coder;
	
	public Statement(int statementId, int documentId, int start, 
			int stop, Date date, Color color, String type, int coder) {
		this.id = statementId;
		this.document = documentId;
		this.start = start;
		this.stop = stop;
		this.date = date;
		this.color = color;
		this.type = type;
		this.coder = coder;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDocument() {
		return document;
	}

	public void setDocument(int document) {
		this.document = document;
	}
	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getStop() {
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}

	public int getCoder() {
		return coder;
	}

	public void setCoder(int coder) {
		this.coder = coder;
	}

	// this tells the array list how to sort statements, i.e., by a combination 
	// of date, document ID and position
	public int compareTo(Statement s) {
		if (this.getDate().compareTo(s.getDate()) < 0) {
			return -1;
		} else if (this.getDate().compareTo(s.getDate()) > 0) {
			return 1;
		} else {
			if (((Integer) this.document).compareTo(s.getDocument()) < 0) {
				return -1;
			} else if (((Integer) this.document).compareTo(s.getDocument()) 
					> 0) {
				return 1;
			} else if (((Integer) this.document).compareTo(s.getDocument()) 
					== 0 && this.start < s.getStart()) {
				return -1;
			} else if (((Integer) this.document).compareTo(s.getDocument()) 
					== 0 && this.start > s.getStart()) {
				return 1;
			} else if (((Integer) this.document).compareTo(s.getDocument()) 
					== 0 && this.start == s.getStart() && this.stop < s.getStop()) {
				return -1;
			} else if (((Integer) this.document).compareTo(s.getDocument()) 
					== 0 && this.start == s.getStart() && this.stop > s.getStop()) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	//necessary for sorting purposes
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (getClass() != o.getClass()) return false;
		return compareTo((Statement) o) == 0;
	}
}
