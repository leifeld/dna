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
	 * Retrieve the coder of the document.
	 * 
	 * @return  The coder.
	 */
	public String getCoder() {
		return coder;
	}

	/**
	 * Set the coder of the document.
	 * 
	 * @param coder  The coder to set.
	 */
	public void setCoder(String coder) {
		this.coder = coder;
	}

	/**
	 * Retrieve the source field of the document.
	 * 
	 * @return  The source.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Set the source field of the document.
	 * 
	 * @param source  The source to set.
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Retrieve the notes field of the document.
	 * 
	 * @return  The notes.
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Set the notes field of the document.
	 * 
	 * @param notes  The notes to set.
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * Retrieve the document type.
	 * 
	 * @return  The type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set the document type.
	 * 
	 * @param type  The type to set.
	 */
	public void setType(String type) {
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
