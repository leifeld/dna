package model;

import java.time.LocalDateTime;

/**
 * Represents the rows in a document table model.
 */
public class TableDocument implements Comparable<TableDocument> {
	private int id, frequency;
	private Coder coder;
	private String title, author, source, section, type, notes;
	private LocalDateTime dateTime;

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
	public TableDocument(int id, String title, int frequency, Coder coder, String author, String source, String section,
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

	/**
	 * Get the ID of the document.
	 * 
	 * @return The document ID.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the number of statements in the document. This is taken from the
	 * frequency field, which is set manually, not from the database or by
	 * counting anything.
	 * 
	 * @return The number of statements in the document.
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * Set the number of statements in the document.
	 * 
	 * @param frequency The number of statements in the document.
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	/**
	 * Get the coder ID of the document.
	 * 
	 * @return The coder ID.
	 */
	public Coder getCoder() {
		return coder;
	}

	/**
	 * Get the document title.
	 * 
	 * @return The title of the document.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Get the document author.
	 * 
	 * @return The author of the document.
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Get the document source.
	 * 
	 * @return The source of the document.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Get the document section.
	 * 
	 * @return The section field of the document.
	 */
	public String getSection() {
		return section;
	}

	/**
	 * Get the document type.
	 * 
	 * @return The type of the document.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Get the document notes.
	 * 
	 * @return The notes of the document.
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Get the document date/time stamp.
	 * 
	 * @return The document date/time stamp.
	 */
	public LocalDateTime getDateTime() {
		return dateTime;
	}

	/**
	 * Implementation of the {@link java.lang.Comparable Comparable} interface
	 * to sort documents in the document table and possibly elsewhere.
	 */
	@Override
	public int compareTo(TableDocument d) {
		if (this.dateTime != null && this.getDateTime().isBefore(d.getDateTime())) {
			return -1;
		} else if (this.getId() < d.getId()) {
			return -1;
		} else {
			return 1;
		}
	}
}