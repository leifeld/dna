package dna.renderer;

import dna.Dna;
import dna.dataStructures.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class StatementTableModel extends AbstractTableModel {
	
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
		return Dna.data.getStatements().size();
	}
	
	public int size() { //same as getRowCount()
		return Dna.data.getStatements().size();
	}
	
	public void remove(int index) {
		Dna.data.getStatements().remove(index);
		TableModelEvent e = new TableModelEvent(this);
		for( int j = 0, n = listeners.size(); j < n; j++ ){
			((TableModelListener)listeners.get( j )).tableChanged( e );
		}
	}

	// Ele
	public ArrayList<Statement> getAll () {
		return Dna.data.getStatements();
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Statement s = Dna.data.getStatements().get(rowIndex);
		if (columnIndex == 0) {
			return s.getId();
		} else if (columnIndex == 1) {
			int start = s.getStart();
			int stop = s.getStop();
			int documentId = s.getDocumentId();
			//SK -------- start ------
			//String text = Dna.dna.db.getDocument(documentId).getText().substring(start, stop);
			//String text = Dna.dna.gui.documentPanel.documentContainer.getDocumentByID(documentId).getText().substring(start, stop);
			String text = "";
			try {
				text = Dna.data.getDocument(documentId).getText().substring(start, stop);
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				System.err.println("Statement " + s.getId() + ": Location outside the document text.");
				text = "";
			}
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
		int statementId = Dna.data.getStatements().get(rowIndex).getId();
		
		switch( columnIndex ){
			case 0: 
				getByStatementId(statementId).setId((Integer) aValue);
				break;
			case 1: 
				getByStatementId(statementId).setStatementTypeId((Integer) aValue);
				break;
		}
	}
	
	public Statement get(int index) {
		return Dna.data.getStatements().get(index);
	}
	
	public Statement getByStatementId(int id) throws NullPointerException {
		for (int i = 0; i < Dna.data.getStatements().size(); i++) {
			if (Dna.data.getStatements().get(i).getId() == id) {
				return Dna.data.getStatements().get(i);
			}
		}
		throw new NullPointerException();
	}
	
	public int getIndexByStatementId(int id) {
		int index = -1;
		for (int i = 0; i < Dna.gui.rightPanel.statementPanel.statementTable.getRowCount(); i++) {
			if ((int) Dna.gui.rightPanel.statementPanel.statementTable.getValueAt(i, 0) == id) {
				index = i;
			}
		}
		return index;
	}
	
	public void clear() {
		Dna.data.getStatements().clear();
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public boolean containsStatementId(int id) {
		for (int i = 0; i < Dna.data.getStatements().size(); i++) {
			if (Dna.data.getStatements().get(i).getId() == id) {
				return true;
			}
		}
		return false;
	}
	
	public void sort() {
		Collections.sort(Dna.data.getStatements());
	}
	
	public void addStatement(Statement s) {
		Dna.data.getStatements().add(s);
		sort();
		this.fireTableDataChanged();
	}
	
	public void removeStatement(int statementId) {
		for (int i = Dna.data.getStatements().size() - 1; i > -1 ; i--) {
			if (Dna.data.getStatements().get(i).getId() == statementId) {
				Dna.data.getStatements().remove(i);
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
			for (int i = 0; i < Dna.data.getStatements().size(); i++) {
				if (unused == Dna.data.getStatements().get(i).getId()) {
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
