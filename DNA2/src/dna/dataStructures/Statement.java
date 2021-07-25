package dna.dataStructures;

import java.util.Date;
import java.util.LinkedHashMap;

public class Statement implements Comparable<Statement> {
	int id, documentId, start, stop;
	Date date;
	int statementTypeId;
	int coder;
	LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
	
	public Statement(int statementId, int documentId, int start, int stop, Date date, int statementTypeId, int coder) {
		this.id = statementId;
		this.documentId = documentId;
		this.start = start;
		this.stop = stop;
		this.date = date;
		this.statementTypeId = statementTypeId;
		this.coder = coder;
		this.values = new LinkedHashMap<String, Object>();
	}
	
	public Statement(int statementId, int documentId, int start, int stop, Date date, int statementTypeId, int coder, LinkedHashMap<String, Object> values) {
		this.id = statementId;
		this.documentId = documentId;
		this.start = start;
		this.stop = stop;
		this.date = date;
		this.statementTypeId = statementTypeId;
		this.coder = coder;
		this.values = values;
	}
	
	/**
	 * @return the values
	 */
	public LinkedHashMap<String, Object> getValues() {
		return values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(LinkedHashMap<String, Object> values) {
		this.values = values;
	}

	/**
	 * @return the type
	 */
	public int getStatementTypeId() {
		return statementTypeId;
	}

	public void setStatementTypeId(int statementTypeId) {
		this.statementTypeId = statementTypeId;
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
			if (((Integer) this.documentId).compareTo(s.getDocumentId()) < 0) {
				return -1;
			} else if (((Integer) this.documentId).compareTo(s.getDocumentId()) 
					> 0) {
				return 1;
			} else if (((Integer) this.documentId).compareTo(s.getDocumentId()) 
					== 0 && this.start < s.getStart()) {
				return -1;
			} else if (((Integer) this.documentId).compareTo(s.getDocumentId()) 
					== 0 && this.start > s.getStart()) {
				return 1;
			} else if (((Integer) this.documentId).compareTo(s.getDocumentId()) 
					== 0 && this.start == s.getStart() && this.stop < s.getStop()) {
				return -1;
			} else if (((Integer) this.documentId).compareTo(s.getDocumentId()) 
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
