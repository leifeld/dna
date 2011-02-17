package dna;

import java.util.Date;

public class DnaArticle{
	String title, text;
	Date date;
	
	public DnaArticle( String title, Date date ) {
		this.title = title;
		this.date = date;
	}
	
	public DnaArticle( String title, Date date, String text ) {
		this.title = title;
		this.date = date;
		this.text = text;
	}
	
	public String getTitle(){ return title; }
	public Date getDate(){ return date; }
	public String getText(){ return text; }
	
	public void setTitle(String title){ this.title = title; }
	public void setDate(Date date){ this.date = date; }
	public void setText(String text){ this.text = text; }
}