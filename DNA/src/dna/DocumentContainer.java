package dna;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

class DocumentContainer implements TableModel {
	private ArrayList<Document> documents = new ArrayList<Document> ();
	private Vector<TableModelListener> listeners = 
			new Vector<TableModelListener>();

	public boolean containsTitle(String title) {
		boolean contains = false;
		for (int i = 0; i < documents.size(); i++) {
			if (documents.get(i).getTitle().equals(title)) {
				contains = true;
			}
		}
		return contains;
	}

	public void addDocument( Document document ){
		int id = document.getId();
		documents.add( document );
		sort();
		int index = getRowIndexById(id);

		//notify all listeners
		TableModelEvent e = new TableModelEvent( this, index, index, 
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public Document get(int index) {
		return documents.get(index);
	}

	//return number of columns
	public int getColumnCount() {
		return 2;
	}

	//return number of documents in the table
	public int getRowCount() {
		return documents.size();
	}

	public int getRowIndexById(int id) throws NullPointerException {
		for (int i = 0; i < documents.size(); i++) {
			if (documents.get(i).getId() == id) {
				return i;
			}
		}
		throw new NullPointerException();
	}

	public void remove(int index) {
		documents.remove(index);
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}

	public void clear() {
		documents.clear();
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}

	//return the name of a column
	public String getColumnName(int column) {
		switch( column ){
			case 0: return "Title";
			case 1: return "Date";
			default: return null;
		}
	}

	//get the value of a cell (rowIndex, columnIndex)
	public Object getValueAt(int rowIndex, int columnIndex) {
		Document document = documents.get(rowIndex);
		switch( columnIndex ){
			case 0: return document.getTitle();
			case 1: return document.getDate();
			default: return null;
		}
	}

	//which type of object (i.e. class) shall be shown in the columns?
	public Class getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return String.class;
			case 1: return Date.class;
			default: return null;
		}	
	}

	public void addTableModelListener(TableModelListener l) {
		listeners.add( l );
	}
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove( l );
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Document document = documents.get(rowIndex);

		switch( columnIndex ){
		case 0: 
			document.setTitle( (String)aValue );
			break;
		case 1: 
			document.setDate((Date)aValue);
			break;
		}

		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}

	public void sort() {
		Collections.sort(documents);
	}
}