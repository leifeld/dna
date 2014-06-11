package dna;

import java.awt.Color;
import java.util.Date;

public class SidebarStatement implements Comparable<SidebarStatement> {		
	int statementId, documentId, start, stop;
	Date date;
	Color color;
	String type;
	
	public SidebarStatement(int statementId, int documentId, int start, 
			int stop, Date date, Color color, String type) {
		this.statementId = statementId;
		this.documentId = documentId;
		this.start = start;
		this.stop = stop;
		this.date = date;
		this.color = color;
		this.type = type;
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

	public int getStatementId() {
		return statementId;
	}

	public void setStatementId(int statementId) {
		this.statementId = statementId;
	}

	public int getDocumentId() {
		return documentId;
	}

	public void setDocumentId(int documentId) {
		this.documentId = documentId;
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
	
	// this tells the array list how to sort statements, i.e., by a combination 
	// of date, document ID and position
	public int compareTo(SidebarStatement s) {
		if (this.getDate().compareTo(s.getDate()) < 0) {
			return -1;
		} else if (this.getDate().compareTo(s.getDate()) > 0) {
			return 1;
		} else {
			if (((Integer) this.documentId).compareTo(s.getDocumentId()) < 0) {
				return -1;
			} else if (((Integer) this.documentId).compareTo(s.getDocumentId()) 
					> 0) {
				return 1;
			} else if (((Integer) this.documentId).compareTo(s.getDocumentId()) 
					== 0 && this.start < s.start) {
				return -1;
			} else if (((Integer) this.documentId).compareTo(s.getDocumentId()) 
					== 0 && this.start > s.start) {
				return 1;
			} else if (((Integer) this.documentId).compareTo(s.getDocumentId()) 
					== 0 && this.start == s.start && this.stop < s.stop) {
				return -1;
			} else if (((Integer) this.documentId).compareTo(s.getDocumentId()) 
					== 0 && this.start == s.start && this.stop > s.stop) {
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
		return compareTo((SidebarStatement) o) == 0;
	}
}