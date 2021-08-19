package guiCoder;

import java.time.LocalDateTime;

/**
 * Represents the rows in a {@link DocumentTableModel}.
 */
class TableDocument {
	int id, frequency;
	Coder coder;
	String title, author, source, section, type, notes;
	LocalDateTime dateTime;

	/**
	 * Create an instance of a table row.
	 * 
	 * @param id The document ID of the {@link Document}.
	 * @param title The title of the {@link Document}.
	 * @param frequency How many statements does the {@link Document} contain?
	 * @param coder ID of the coder who added the {@link Document}.
	 * @param author Author of the {@link Document}.
	 * @param source Source of the {@link Document}.
	 * @param section Section of the {@link Document}.
	 * @param type Type of the {@link Document}.
	 * @param notes Notes for the {@link Document}.
	 * @param dateTime The date and time the {@link Document} happened.
	 */
	TableDocument(int id, String title, int frequency, Coder coder, String author, String source, String section,
			String type, String notes, LocalDateTime dateTime) {
		this.id = id;
		this.frequency = frequency;
		this.coder = coder;
		this.title = title;
		this.author = author;
		this.source = source;
		this.section = section;
		this.type = type;
		this.notes = notes;
		this.dateTime = dateTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public Coder getCoder() {
		return coder;
	}

	public void setCoder(Coder coder) {
		this.coder = coder;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
}