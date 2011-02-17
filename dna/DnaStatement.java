package dna;
import java.util.*;

public class DnaStatement {
	
	String content;
	String person;
	String organization;
	String category;
	Boolean agreement;
	GregorianCalendar date;

	public DnaStatement() {
	} //create empty statement
	
	public DnaStatement(String content, String person, String organization, String category, Boolean agreement, GregorianCalendar date) {
		this.content = content;
		this.person = person;
		this.organization = organization;
		this.category = category;
		this.agreement = agreement;
		this.date = date;
	}

	public Boolean getAgreement() {
		return agreement;
	}

	public void setAgreement(Boolean agreement) {
		this.agreement = agreement;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}
	
	public GregorianCalendar getDate() {
		return date;
	}

	public void setDate(GregorianCalendar date) {
		this.date = date;
	}
	
}
