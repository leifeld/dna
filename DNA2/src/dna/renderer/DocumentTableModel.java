package dna.renderer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import dna.Dna;
import dna.dataStructures.Coder;
import dna.dataStructures.Document;

@SuppressWarnings("serial")
public class DocumentTableModel extends AbstractTableModel {
	private Vector<TableModelListener> listeners = 	new Vector<TableModelListener>();
	
	public DocumentTableModel(ArrayList<Document> documents) {
		Dna.data.setDocuments(documents);	
		sort();
	}
	
	public DocumentTableModel() {
		super();
	}
	
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
		int index = getModelIndexById(id);

		//notify all listeners
		TableModelEvent e = new TableModelEvent( this, index, index, 
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public void addDocuments(ArrayList<Document> al) {
		Dna.data.getDocuments().addAll(al);
		sort();
		this.fireTableDataChanged();
	}
	
	public void changeDocument(int documentId, String title, Date date, int coder, String source, String section, String notes, String type) {
		int i = getModelIndexById(documentId);
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
	
	//return number of documents in the table
	public int getRowCount() {		
		return Dna.data.getDocuments().size();
	}
	
	public int getModelIndexById(int id) throws NullPointerException {
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (Dna.data.getDocuments().get(i).getId() == id) {
				return i;
			}
		}
		throw new NullPointerException();
	}
	
	public int getIdByModelIndex(int modelIndex) {
		return Dna.data.getDocuments().get(modelIndex).getId();
	}

	public void remove(int index, boolean alsoStatements) {
		if (alsoStatements == true) {
			int id = Dna.data.getDocuments().get(index).getId();
			for (int i = Dna.data.getStatements().size() - 1; i > -1; i--) {
				if (Dna.data.getStatements().get(i).getDocumentId() == id) {
					Dna.data.getStatements().remove(i);
				}
			}
			Dna.gui.rightPanel.statementPanel.statementTable.updateUI();
		}
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
	
	//return number of columns
	public int getColumnCount() {
		return 11;
	}
	
	//return the name of a column
	public String getColumnName(int column) {
		switch( column ){
			case 0: return "ID";
			case 1: return "Title";
			case 2: return "#";
			case 3: return "Date";
			case 4: return "Time";
			case 5: return "Coder";
			case 6: return "Author";
			case 7: return "Source";
			case 8: return "Section";
			case 9: return "Type";
			case 10: return "Notes";
			default: return null;
		}
	}
	
	//get the value of a cell (rowIndex, columnIndex)
	public Object getValueAt(int rowIndex, int columnIndex) {
		Document document = Dna.data.getDocuments().get(rowIndex);
		SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
		switch( columnIndex ){
			case 0: return document.getId();
			case 1: return document.getTitle();
			case 2: return Dna.data.countStatementsPerDocument(document.getId());
			case 3: return document.getDate();
			case 4: return time.format(document.getDate());
			case 5: return Dna.data.getCoderById(document.getCoder());
			case 6: return document.getAuthor();
			case 7: return document.getSource();
			case 8: return document.getSection();
			case 9: return document.getType();
			case 10: return document.getNotes();
			default: return null;
		}
	}

	//which type of object (i.e., class) shall be shown in the columns?
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return Integer.class;  // ID
			case 1: return String.class;  // Title
			case 2: return Integer.class;  // #
			case 3: return Date.class;  // Date
			case 4: return String.class;  // Time
			case 5: return Coder.class;  // Coder
			case 6: return String.class;  // Author
			case 7: return String.class;  // Source
			case 8: return String.class;  // Section
			case 9: return String.class;  // Type
			case 10: return String.class;  // Notes
			default: return null;
		}
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Document document = Dna.data.getDocuments().get(rowIndex);
		switch(columnIndex) {
		case 0: 
			document.setId( (Integer)aValue );
			break;
		case 1: 
			document.setTitle( (String)aValue );
			break;
		case 3: 
			document.setDate( (Date)aValue );
			break;
		case 4: 
			document.setDate( (Date)aValue );
			break;
		case 5: 
			document.setCoder( ((Coder)aValue).getId() );
			break;
		case 6: 
			document.setAuthor( (String)aValue );
			break;
		case 7: 
			document.setSource( (String)aValue );
			break;
		case 8: 
			document.setSection( (String)aValue );
			break;
		case 9: 
			document.setType( (String)aValue );
			break;
		case 10: 
			document.setNotes( (String)aValue );
			break;
		}
		
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
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
	
	public void sort() {
		Collections.sort(Dna.data.getDocuments());
		this.fireTableDataChanged();
	}
}