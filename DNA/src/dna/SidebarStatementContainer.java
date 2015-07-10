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

	// Ele
	public ArrayList<SidebarStatement> getAll () {
		return statements;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SidebarStatement s = statements.get(rowIndex);
		if (columnIndex == 0) {
			return s.getStatementId();
		} else if (columnIndex == 1) {
			int start = s.getStart();
			int stop = s.getStop();
			int documentId = s.getDocumentId();
			//SK -------- start ------
			//String text = Dna.dna.db.getDocument(documentId).getText().substring(start, stop);
			String text = Dna.dna.gui.documentPanel.documentContainer.getDocumentByID(documentId).getText().substring(start, stop);
			//SK -------- end ------
			return text;
		} else {
			return null;
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
}