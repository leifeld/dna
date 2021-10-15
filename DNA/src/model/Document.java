package model;


import java.time.LocalDateTime;
import java.util.ArrayList;

public class Document implements Comparable<Document> {
	int id, coder;
	String title, text, author, source, section, type, notes;
	LocalDateTime dateTime;
	ArrayList<Statement> statements;
	
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
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getCoder() {
		return coder;
	}
	
	void setCoder(int coder) {
		this.coder = coder;
	}
	
	public String getTitle() {
		return title;
	}
	
	void setTitle(String title) {
		this.title = title;
	}
	
	public String getText() {
		return text;
	}
	
	void setText(String text) {
		this.text = text;
	}
	
	public String getAuthor() {
		return author;
	}
	
	void setAuthor(String author) {
		this.author = author;
	}
	
	public String getSource() {
		return source;
	}
	
	void setSource(String source) {
		this.source = source;
	}
	
	public String getSection() {
		return section;
	}
	
	void setSection(String section) {
		this.section = section;
	}
	
	public String getType() {
		return type;
	}
	
	void setType(String type) {
		this.type = type;
	}
	
	public String getNotes() {
		return notes;
	}
	
	void setNotes(String notes) {
		this.notes = notes;
	}
	
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	
	void setDate(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
	
	public ArrayList<Statement> getStatements() {
		return statements;
	}
	
	void setStatements(ArrayList<Statement> statements) {
		this.statements = statements;
	}

	@Override
	public int compareTo(Document d) {
		if (this.dateTime != null && this.getDateTime().isBefore(d.getDateTime())) {
			return -1;
		} else if (this.getId() < d.getId()) {
			return -1;
		} else {
			return 1;
		}
	}
}