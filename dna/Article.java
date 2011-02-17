package dna;

import java.util.Date;

public class Article implements Comparable<Article> {
	String title, text;
	Date date;
	
	public Article( String title, Date date ) {
		this.title = title;
		this.date = date;
	}
	
	public Article( String title, Date date, String text ) {
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
	
	//this tells the array list how to sort Articles, i.e. by a combination of date and title
	public int compareTo(Article a) {
		if (this.getDate().compareTo(a.getDate()) < 0) {
			return -1;
		} else if (this.getDate().compareTo(a.getDate()) > 0) {
			return 1;
		} else if (this.getDate().compareTo(a.getDate()) == 0 && this.getTitle().compareToIgnoreCase(a.getTitle()) < 0) {
			return -1;
		} else if (this.getDate().compareTo(a.getDate()) == 0 && this.getTitle().compareToIgnoreCase(a.getTitle()) > 0) {
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
		return compareTo((Article) o) == 0;
	}
}