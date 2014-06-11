package dna;

import java.util.Date;

class Document implements Comparable<Document> {
	int id;
	String title, text, coder, source, notes, type;
	Date date;
	
	public Document( int id, String title, String text, Date date, 
			String coder, String source, String notes, String type ) {
		this.id = id;
		this.title = title;
		this.text = text;
		this.date = date;
		this.coder = coder;
		this.source = source;
		this.notes = notes;
		this.type = type;
	}

	/**
	 * Retrieve the document text.
	 * 
	 * @return  The text.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Set the document text.
	 * 
	 * @param text  The text to set.
	 */
	public void setText(String text) {
		this.text = text;
	}

	public int getId(){ return id; }
	public String getTitle(){ return title; }
	public Date getDate(){ return date; }

	public void setId(int id){ this.id = id; }
	public void setTitle(String title){ this.title = title; }
	public void setDate(Date date){ this.date = date; }
	
	//this tells the array list how to sort Articles, i.e. by a 
	//combination of date and title
	public int compareTo(Document a) {
		if (this.getDate().compareTo(a.getDate()) < 0) {
			return -1;
		} else if (this.getDate().compareTo(a.getDate()) > 0) {
			return 1;
		} else if (this.getDate().compareTo(a.getDate()) == 0 && 
				this.getTitle().compareToIgnoreCase(a.getTitle()) < 0) {
			return -1;
		} else if (this.getDate().compareTo(a.getDate()) == 0 && 
				this.getTitle().compareToIgnoreCase(a.getTitle()) > 0) {
			return 1;
		} else if (this.getDate().compareTo(a.getDate()) == 0 &&
				this.getTitle().compareToIgnoreCase(a.getTitle()) == 0 &&
				new Integer(this.getId()).compareTo(a.getId()) < 0) {
			return -1;
		} else if (this.getDate().compareTo(a.getDate()) == 0 &&
				this.getTitle().compareToIgnoreCase(a.getTitle()) == 0 &&
				new Integer(this.getId()).compareTo(a.getId()) > 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	//necessary for sorting purposes
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (getClass() != o.getClass()) return false;
		return compareTo((Document) o) == 0;
	}
}
