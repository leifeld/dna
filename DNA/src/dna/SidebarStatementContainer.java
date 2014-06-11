package dna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class SidebarStatementContainer implements TableModel {
	
	ArrayList<SidebarStatement> statements = new ArrayList<SidebarStatement>();
	Vector<TableModelListener> listeners = new Vector<TableModelListener>();
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add( l );
	}
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove( l );
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return Integer.class;
			case 1: return String.class;
			default: return null;
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int column) {
		switch( column ){
			case 0: return "ID";
			case 1: return "Text";
			default: return null;
		}
	}

	@Override
	public int getRowCount() { //same as size(); but needs to be implemented
		return statements.size();
	}
	
	public int size() { //same as getRowCount()
		return statements.size();
	}
	
	public void remove(int index) {
		statements.remove(index);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SidebarStatement s = statements.get(rowIndex);
		
		switch( columnIndex ){
			case 0: return s.getStatementId();
			case 1: return s.getType();
			default: return null;
		}
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		int statementId = statements.get(rowIndex).getStatementId();
		
		switch( columnIndex ){
			case 0: 
				getByStatementId(statementId).setStatementId((Integer) aValue);
				break;
			case 1: 
				getByStatementId(statementId).setType((String) aValue);
				break;
		}
	}
	
	
	public SidebarStatement get(int index) {
		return statements.get(index);
	}
	
	public SidebarStatement getByStatementId(int id) throws NullPointerException {
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getStatementId() == id) {
				return statements.get(i);
			}
		}
		throw new NullPointerException();
	}
	
	public int getIndexByStatementId(int id) {
		int index = -1;
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getStatementId() == id) {
				index = i;
			}
		}
		return index;
	}
	
	public void clear() {
		statements.clear();
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public boolean containsStatementId(int id) {
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getStatementId() == id) {
				return true;
			}
		}
		return false;
	}
	
	public void sort() {
		Collections.sort(statements);
	}
	
	public void addSidebarStatement(SidebarStatement s, boolean sort) {
		if (containsStatementId(s.getStatementId()) == true) {
			System.err.println("A statement with ID " + s.getStatementId() + 
					" already exists. It will not be added.");
		} else {
			int statementId = s.getStatementId();
			statements.add(s);
			if (sort == true) {
				sort();
			}
			int index = getIndexByStatementId(statementId);
			
			//notify all listeners
			TableModelEvent e = new TableModelEvent( this, index, index, //create event 'new row at index'
					TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
			for( int i = 0, n = listeners.size(); i < n; i++ ){
				((TableModelListener)listeners.get( i )).tableChanged( e );
			}
			
		}
	}
	
	public void removeSidebarStatement(int statementId) {
		for (int i = statements.size() - 1; i > -1 ; i--) {
			if (statements.get(i).getStatementId() == statementId) {
				statements.remove(i);
			}
		}
		
		//TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
		TableModelEvent e = new TableModelEvent(this);
		for( int j = 0, n = listeners.size(); j < n; j++ ){
			((TableModelListener)listeners.get( j )).tableChanged( e );
		}
	}
	
	public int getFirstUnusedId() {
		sort();
		int unused = 1;
		boolean accept = false;
		while (accept == false) {
			boolean used = false;
			for (int i = 0; i < statements.size(); i++) {
				if (unused == statements.get(i).getStatementId()) {
					used = true;
				}
			}
			if (used == true) {
				accept = false;
				unused++;
			} else {
				accept = true;
			}
		}
		return unused;
	}
	
	/*
	public void rebuildTable() {
		this.clear();
		ArrayList<SidebarStatement> sidebarStatements = new SqlQuery(Dna.mainProgram.dbfile).getSidebarStatements();
		for (int i = 0; i < sidebarStatements.size(); i++) {
			addSidebarStatement(sidebarStatements.get(i), false);
		}
		
	}
	*/
	
	/*
	protected SidebarStatementContainer clone() {
		SidebarStatementContainer stc = new SidebarStatementContainer();
		for (int i = 0; i < statements.size(); i++) {
			SidebarStatement st = statements.get(i);
			String articleTitle = st.getArticleTitle();
			String agreement = st.getAgreement();
			int start = st.getStart();
			int stop = st.getStop();
			int id = st.getId();
			String person = st.getPerson();
			String organization = st.getOrganization();
			String category = st.getCategory();
			Date date = st.getDate();
			String text = st.getText();
			Statement stm = new Statement(id, start, stop, date, text, articleTitle, person, organization, category, agreement);
			try {
				stc.addStatement(stm, false);
			} catch (DuplicateStatementIdException e) {
				try {
					throw new DuplicateStatementIdException("A statement with ID " + id + " already exists. It will not be used.");
				} catch (DuplicateStatementIdException dse) {
					System.out.println(dse.getMessage());
				}
			}
		}
		return stc;
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
				list.add(statements.get(i).getAgreement());
			}
		} else if (type.equals("t")) {
			for (int i = 0; i < statements.size(); i++) {
				list.add(statements.get(i).getText());
			}
		}
		return list;
	}
	
	public ArrayList<GregorianCalendar> getDateList() {
		ArrayList<GregorianCalendar> list = new ArrayList<GregorianCalendar>();
		for (int i = 0; i < statements.size(); i++) {
			GregorianCalendar g = new GregorianCalendar();
			g.setTime(statements.get(i).getDate());
			list.add(g);
		}
		return list;
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
			String c = statements.get(i).getCategory();
			if (!c.equals("")) {
				boolean cont = false;
				for (int j = 0; j < categories.size(); j++) {
					if (categories.get(j).equals(c)) {
						cont = true;
					}
				}
				if (cont == false) {
					categories.add(c);
				}
			}
		}
		Collections.sort(categories);
		return categories;
	}
	
	public GregorianCalendar getFirstDate() {
		if (statements.size() > 0) {
			ArrayList<GregorianCalendar> dateList = new ArrayList<GregorianCalendar>();
			for (int i = 0; i < statements.size(); i++) {
				Statement s = statements.get(i);
				GregorianCalendar g = new GregorianCalendar();
				g.setTime(s.getDate());
				dateList.add(g);
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
				Statement s = statements.get(i);
				GregorianCalendar g = new GregorianCalendar();
				g.setTime(s.getDate());
				dateList.add(g);
			}
			Collections.sort(dateList);
			return dateList.get(dateList.size()-1);
		} else {
			return new GregorianCalendar(2099, 1, 1);
		}
	}
	*/
}