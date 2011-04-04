package dna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class StatementContainer implements TableModel {
	
	DuplicateStatementIdException dsie;
	ArrayList<Statement> statements = new ArrayList<Statement>();
	Vector<TableModelListener> listeners = new Vector<TableModelListener>();
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add( l );
	}
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove( l );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public Class getColumnClass(int columnIndex) {
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
		Statement s = statements.get(rowIndex);
		
		switch( columnIndex ){
			case 0: return s.getId();
			case 1: return s.getText();
			default: return null;
		}
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		int id = statements.get(rowIndex).getId();
		
		switch( columnIndex ){
			case 0: 
				getById(id).setId((Integer) aValue);
				break;
			case 1: 
				getById(id).setText((String) aValue);
				break;
		}
	}
	
	
	public Statement get(int index) {
		return statements.get(index);
	}
	
	public Statement getById(int id) throws NullPointerException {
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).id == id) {
				return statements.get(i);
			}
		}
		throw new NullPointerException();
	}
	
	public int getIndexById(int id) {
		int index = -1;
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).id == id) {
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
	
	public boolean containsId(int id) {
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).id == id) {
				return true;
			}
		}
		return false;
	}
	
	public void sort() {
		Collections.sort(statements);
	}
	
	public void addStatement(Statement s, boolean sort) throws DuplicateStatementIdException {
		if (containsId(s.id) == true) {
			throw new DuplicateStatementIdException("A statement with ID " + s.id + " already exists. It will not be added.");
		} else {
			int id = s.getId();
			statements.add(s);
			if (sort == true) {
				sort();
			}
			int index = getIndexById(id);
			
			//notify all listeners
			TableModelEvent e = new TableModelEvent( this, index, index, //create event 'new row at index'
					TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
			for( int i = 0, n = listeners.size(); i < n; i++ ){
				((TableModelListener)listeners.get( i )).tableChanged( e );
			}
			
		}
	}
	
	public void removeStatement(int id) {
		for (int i = statements.size() - 1; i > -1 ; i--) {
			if (statements.get(i).getId() == id) {
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
				if (unused == statements.get(i).id) {
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
	
	protected StatementContainer clone() {
		StatementContainer stc = new StatementContainer();
		for (int i = 0; i < statements.size(); i++) {
			Statement st = statements.get(i);
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
}