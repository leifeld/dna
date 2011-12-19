package dna;

import java.util.Date;

public class DnaStatement implements Comparable<DnaStatement> {		
	int id, start, stop;
	String text, person, organization, category, agreement, articleTitle;
	Date date;
	
	public DnaStatement(int id, int start, int stop, Date date, String text, String articleTitle, String person, String organization, String category, String agreement) {
		this.id = id;
		this.start = start;
		this.stop = stop;
		this.date = date;
		this.text = text;
		this.articleTitle = articleTitle;
		this.person = person;
		this.organization = organization;
		this.category = category;
		this.agreement = agreement;
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
	
	public int getStart() {
		return start;
	}

	public String getArticleTitle() {
		return articleTitle;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getStop() {
		return stop;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAgreement() {
		return agreement;
	}

	public void setAgreement(String agreement) {
		this.agreement = agreement;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	//this tells the array list how to sort Statements, i.e. by a combination of date, articleTitle and position
	public int compareTo(DnaStatement s) {
		if (this.getDate().compareTo(s.getDate()) < 0) {
			return -1;
		} else if (this.getDate().compareTo(s.getDate()) > 0) {
			return 1;
		} else if (this.getDate().compareTo(s.getDate()) == 0 && this.articleTitle.compareToIgnoreCase(s.articleTitle) < 0) {
			return -1;
		} else if (this.getDate().compareTo(s.getDate()) == 0 && this.articleTitle.compareToIgnoreCase(s.articleTitle) > 0) {
			return 1;
		} else if (this.getDate().compareTo(s.getDate()) == 0 && this.articleTitle.compareToIgnoreCase(s.articleTitle) == 0 && this.start < s.start) {
			return -1;
		} else if (this.getDate().compareTo(s.getDate()) == 0 && this.articleTitle.compareToIgnoreCase(s.articleTitle) == 0 && this.start > s.start) {
			return 1;
		} else if (this.getDate().compareTo(s.getDate()) == 0 && this.articleTitle.compareToIgnoreCase(s.articleTitle) == 0 && this.start == s.start && this.stop < s.stop) {
			return -1;
		} else if (this.getDate().compareTo(s.getDate()) == 0 && this.articleTitle.compareToIgnoreCase(s.articleTitle) == 0 && this.start == s.start && this.stop > s.stop) {
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
		return compareTo((DnaStatement) o) == 0;
	}
}