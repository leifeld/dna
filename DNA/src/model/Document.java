package model;


import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Represents documents as required by the document editor, document batch
 * importer, and the SQL class.
 */
public class Document implements Comparable<Document> {
	private int id, coder;
	private String title, text, author, source, section, type, notes;
	private LocalDateTime dateTime;
	private ArrayList<Statement> statements;
	
	/**
	 * Create a new document representation.
	 * 
	 * @param id          The document ID.
	 * @param coder       The coder ID.
	 * @param title       The title of the document.
	 * @param text        The text of the document.
	 * @param author      The author of the document.
	 * @param source      The source of the document.
	 * @param section     The section of the document.
	 * @param type        The type of the document.
	 * @param notes       Notes for the document.
	 * @param dateTime    The date/time stamp of the document.
	 * @param statements  An array list with the statements contained in the
	 *   document.
	 */
	public Document(int id, int coder, String title, String text, String author, String source, String section,
			String type, String notes, LocalDateTime dateTime, ArrayList<Statement> statements) {
		this.id = id;
		this.coder = coder;
		this.title = title;
		this.text = text;
		this.author = author;
		this.source = source;
		this.section = section;
		this.type = type;
		this.notes = notes;
		this.dateTime = dateTime;
		this.statements = statements;
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
	 * Get the coder ID of the document.
	 * 
	 * @return The coder ID.
	 */
	public int getCoder() {
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
	 * Get the document text.
	 * 
	 * @return The text of the document.
	 */
	public String getText() {
		return text;
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
	 * Get the statements nested in the document.
	 * 
	 * @return An array list of statements saved in the document instance.
	 */
	public ArrayList<Statement> getStatements() {
		return statements;
	}

	/**
	 * Implementation of the {@link java.lang.Comparable Comparable} interface
	 * to sort documents.
	 */
	@Override
	public int compareTo(Document d) {
		if (this.dateTime != null) {
			if (this.getDateTime().isBefore(d.getDateTime())) {
				return -1;
			} else if (this.getDateTime().isAfter(d.getDateTime())) {
				return 1;
			}
		}
		if (this.getId() < d.getId()) {
			return -1;
		} else {
			return 1;
		}
	}
}