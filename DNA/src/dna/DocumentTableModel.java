package dna;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import dna.dataStructures.Document;

class DocumentTableModel implements TableModel {
	private Vector<TableModelListener> listeners = 	new Vector<TableModelListener>();

	//SK start
	DocumentTableModel(ArrayList<Document> documents) {
		Dna.data.setDocuments(documents);	
		sort();
	}
	
	DocumentTableModel() {
		super();
	}
	//SK end
	
	public boolean containsTitle(String title) {
		boolean contains = false;
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (Dna.data.getDocuments().get(i).getTitle().equals(title)) {
				contains = true;
			}
		}
		return contains;
	}

	public void addDocument( Document document ){
		int id = document.getId();
		Dna.data.getDocuments().add( document );
		sort();
		int index = getRowIndexById(id);

		//notify all listeners
		TableModelEvent e = new TableModelEvent( this, index, index, 
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public void changeDocument(int documentId, String title, Date date, 
			int coder, String source, String section, String notes, 
			String type) {
		int i = getRowIndexById(documentId);
		Dna.data.getDocuments().get(i).setTitle(title);
		Dna.data.getDocuments().get(i).setDate(date);
		Dna.data.getDocuments().get(i).setCoder(coder);
		Dna.data.getDocuments().get(i).setSource(source);
		Dna.data.getDocuments().get(i).setSection(section);
		Dna.data.getDocuments().get(i).setNotes(notes);
		Dna.data.getDocuments().get(i).setType(type);
	}
	
	public Document get(int index) {
		return Dna.data.getDocuments().get(index);
	}

	public Document getDocumentByID(int id)	{
		for(Document doc: Dna.data.getDocuments() ) {
			if (doc.getId() == id) {
				return doc;
			}
		}
		return null;
	}
	
	//return number of columns
	public int getColumnCount() {
		return 2;
	}

	//return number of documents in the table
	public int getRowCount() {		
		return Dna.data.getDocuments().size();
	}

	public int getRowIndexById(int id) throws NullPointerException {
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (Dna.data.getDocuments().get(i).getId() == id) {
				return i;
			}
		}
		throw new NullPointerException();
	}
	
	public int getIdByRowIndex(int row) {
		return Dna.data.getDocuments().get(row).getId();
	}
	
	public void remove(int index) {
		Dna.data.getDocuments().remove(index);
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}

	public void clear() {
		Dna.data.getDocuments().clear();
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
		Document document = Dna.data.getDocuments().get(rowIndex);
		switch( columnIndex ){
			case 0: return document.getTitle();
			case 1: return document.getDate();
			default: return null;
		}
	}

	//which type of object (i.e., class) shall be shown in the columns?
	public Class<?> getColumnClass(int columnIndex) {
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
		Document document = Dna.data.getDocuments().get(rowIndex);

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
		Collections.sort(Dna.data.getDocuments());
	}
}