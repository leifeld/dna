package dna;

import java.util.*;

public class DnaStatementCollection {
	
	ArrayList<DnaStatement> statements;
	
	public DnaStatementCollection() {
		statements = new ArrayList<DnaStatement>();
	}
	
	public void add(DnaStatement s) {
		statements.add(s);
	}
	
	public DnaStatement get(int i) {
		return statements.get(i);
	}
	
	public int size() {
		return statements.size();
	}
	
	public void clearAll() {
		statements.clear();
	}
	
	public void reformatDl() {
		for (int i = 0; i < statements.size(); i++) {
			DnaStatement s = statements.get(i);
			String p = s.getPerson();
			p = p.replaceAll("'", "");
			p = p.replaceAll("^ ", "");
			p = p.replaceAll(" $", "");
			p = p.replaceAll("\\s+", "_");
			p = p.replaceAll(";", "");
			s.setPerson(p);
			String o = s.getOrganization();
			o = o.replaceAll("'", "");
			o = o.replaceAll("^ ", "");
			o = o.replaceAll(" $", "");
			o = o.replaceAll("\\s+", "_");
			o = o.replaceAll(";", ",");
			s.setOrganization(o);
			String c = s.getCategory();
			c = c.replaceAll("'", "");
			c = c.replaceAll("^ ", "");
			c = c.replaceAll(" $", "");
			c = c.replaceAll("\\s+", "_");
			c = c.replaceAll(";", ",");
			s.setCategory(c);
			String t = s.getContent();
			t = t.replaceAll("'", "");
			t = t.replaceAll("^ ", "");
			t = t.replaceAll(" $", "");
			t = t.replaceAll("\\s+", "_");
			t = t.replaceAll(";", ",");
			s.setContent(t);
			statements.set(i, s);
		}
	}
	
	public void reformat() {
		for (int i = 0; i < statements.size(); i++) {
			DnaStatement s = statements.get(i);
			String p = s.getPerson();
			p = p.replaceAll("'", "");
			p = p.replaceAll("^ ", "");
			p = p.replaceAll(" $", "");
			p = p.replaceAll("\\s+", " ");
			p = p.replaceAll(";", "");
			s.setPerson(p);
			String o = s.getOrganization();
			o = o.replaceAll("'", "");
			o = o.replaceAll("^ ", "");
			o = o.replaceAll(" $", "");
			o = o.replaceAll("\\s+", " ");
			o = o.replaceAll(";", ",");
			s.setOrganization(o);
			String c = s.getCategory();
			c = c.replaceAll("'", "");
			c = c.replaceAll("^ ", "");
			c = c.replaceAll(" $", "");
			c = c.replaceAll("\\s+", " ");
			c = c.replaceAll(";", ",");
			s.setCategory(c);
			String t = s.getContent();
			t = t.replaceAll("'", "");
			t = t.replaceAll("^ ", "");
			t = t.replaceAll(" $", "");
			t = t.replaceAll("\\s+", " ");
			t = t.replaceAll(";", ",");
			s.setContent(t);
			statements.set(i, s);
		}
	}
	
	public ArrayList<String> getStringList(String type) {
		ArrayList<String> list = new ArrayList<String>();
		
		if (type.equals("o")) {
			for (int i = 0; i < statements.size(); i++) {
				list.add(statements.get(i).getOrganization());
			}
		} else if (type.equals("p")) {
			for (int i = 0; i < statements.size(); i++) {
				list.add(statements.get(i).getPerson());
			}
		} else if (type.equals("c")) {
			for (int i = 0; i < statements.size(); i++) {
				list.add(statements.get(i).getCategory());
			}
		} else if (type.equals("a")) {
			for (int i = 0; i < statements.size(); i++) {
				if (statements.get(i).getAgreement() == true) {
					list.add("yes");
				} else if (statements.get(i).getAgreement() == false) {
					list.add("no");
				}
			}
		} else if (type.equals("t")) {
			for (int i = 0; i < statements.size(); i++) {
				list.add(statements.get(i).getContent());
			}
		}
		return list;
	}
	
	public ArrayList<GregorianCalendar> getDateList() {
		
		ArrayList<GregorianCalendar> list = new ArrayList<GregorianCalendar>();
		for (int i = 0; i < statements.size(); i++) {
			list.add(statements.get(i).getDate());
		}
		return list;
		
	}
	
	public void agreementFilter(String agreement) {
		int oldSize = statements.size();
		for (int i = statements.size()-1; i >= 0; i--) {
			if ((statements.get(i).getAgreement() == false && agreement.equals("yes")) || (statements.get(i).getAgreement() == true && agreement.equals("no"))) {
				statements.remove(i);
			}
		}
		System.out.println("Applying agreement filter: Keeping " + statements.size() + " out of " + oldSize + " statements.");
	}
	
	public DnaStatementCollection clone() {
		DnaStatementCollection sc = new DnaStatementCollection();
		for (int i = 0; i < statements.size(); i++) {
			sc.add(statements.get(i));
		}
		return sc;
	}
	
	public void excludeFilter (Object[] persons, Object[] organizations, Object[] categories) {
		int oldSize = statements.size();
		for (int i = statements.size()-1; i >= 0; i--) {
			boolean exclude = false;
			for (int j = 0; j < persons.length; j++) {
				String p = (String)persons[j];
				if (statements.get(i).getPerson().equals(p)) {
					exclude = true;
				}
			}
			for (int j = 0; j < organizations.length; j++) {
				String o = (String)organizations[j];
				if (statements.get(i).getOrganization().equals(o)) {
					exclude = true;
				}
			}
			for (int j = 0; j < categories.length; j++) {
				String c = (String)categories[j];
				if (statements.get(i).getCategory().equals(c)) {
					exclude = true;
				}
			}
			if (exclude == true) {
				statements.remove(i);
			}
		}
		System.out.println("Applying exclude list: Keeping " + statements.size() + " out of " + oldSize + " statements.");
	}
	
	public void timeFilter(GregorianCalendar startDate, GregorianCalendar stopDate) {
		int oldSize = statements.size();
		for (int i = statements.size()-1; i >= 0; i--) {
			if (statements.get(i).getDate().before(startDate) || statements.get(i).getDate().after(stopDate)) {
				statements.remove(i);
			}
		}
		System.out.println("Applying date filter: Keeping " + statements.size() + " out of " + oldSize + " statements.");
	}
	
	public ArrayList<String> getPersonList() {
		ArrayList<String> persons = new ArrayList<String>();
		for (int i = 0; i < statements.size(); i++) {
			if (!persons.contains(statements.get(i).getPerson()) && !statements.get(i).getPerson().equals("")) {
				persons.add(statements.get(i).getPerson());
			}
		}
		Collections.sort(persons);
		return persons;
	}
	
	public ArrayList<String> getOrganizationList() {
		ArrayList<String> organizations = new ArrayList<String>();
		for (int i = 0; i < statements.size(); i++) {
			if (!organizations.contains(statements.get(i).getOrganization()) && !statements.get(i).getOrganization().equals("")) {
				organizations.add(statements.get(i).getOrganization());
			}
		}
		Collections.sort(organizations);
		return organizations;
	}
	
	public ArrayList<String> getCategoryList() {
		ArrayList<String> categories = new ArrayList<String>();
		for (int i = 0; i < statements.size(); i++) {
			if (!categories.contains(statements.get(i).getCategory()) && !statements.get(i).getCategory().equals("")) {
				categories.add(statements.get(i).getCategory());
			}
		}
		Collections.sort(categories);
		return categories;
	}
	
	public GregorianCalendar getFirstDate() {
		if (statements.size() > 0) {
			ArrayList<GregorianCalendar> dateList = new ArrayList<GregorianCalendar>();
			for (int i = 0; i < statements.size(); i++) {
				DnaStatement s = statements.get(i);
				dateList.add(s.getDate());
			}
			Collections.sort(dateList);
			return dateList.get(0);
		} else {
			return new GregorianCalendar(1900, 1, 1);
		}
		
	}
	
	public GregorianCalendar getLastDate() {
		if (statements.size() > 0) {
			ArrayList<GregorianCalendar> dateList = new ArrayList<GregorianCalendar>();
			for (int i = 0; i < statements.size(); i++) {
				DnaStatement s = statements.get(i);
				dateList.add(s.getDate());
			}
			Collections.sort(dateList);
			return dateList.get(dateList.size()-1);
		} else {
			return new GregorianCalendar(2099, 1, 1);
		}
	}
}