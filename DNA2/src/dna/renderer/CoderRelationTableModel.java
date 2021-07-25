package dna.renderer;

import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import dna.Dna;
import dna.dataStructures.CoderRelation;

@SuppressWarnings("serial")
public class CoderRelationTableModel extends AbstractTableModel implements TableModel {

	Vector<TableModelListener> listeners = new Vector<TableModelListener>();
	
	public CoderRelation get(int modelRow) {
		return Dna.data.getCoderRelations().get(modelRow);
	}
	
	@Override
	public int getRowCount() {
		return Dna.data.getCoderRelations().size();
	}

	@Override
	public int getColumnCount() {
		return 5;
	}
	
	@Override
	public String getColumnName(int column) {
		switch( column ){
		case 0: return "Name";
		case 1: return "view statements";
		case 2: return "edit statements";
		case 3: return "view documents";
		case 4: return "edit documents";
		default: return null;
		}
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
		case 0: return String.class;
		case 1: return Boolean.class;
		case 2: return Boolean.class;
		case 3: return Boolean.class;
		case 4: return Boolean.class;
		default: return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return false;
		} else if (columnIndex == 1 && Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions().get("viewOthersStatements") == false) {
			return false;
		} else if (columnIndex == 2 && Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions().get("editOthersStatements") == false) {
			return false;
		} else if (columnIndex == 3 && Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions().get("viewOthersDocuments") == false) {
			return false;
		} else if (columnIndex == 4 && Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions().get("editOthersDocuments") == false) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		CoderRelation cr = Dna.data.getCoderRelations().get(rowIndex);
		if (columnIndex == 0) {
			return Dna.data.getCoderById(cr.getOtherCoder()).getName();
		} else if (columnIndex == 1) {
			return Dna.data.getCoderRelations().get(rowIndex).isViewStatements();
		} else if (columnIndex == 2) {
			return Dna.data.getCoderRelations().get(rowIndex).isEditStatements();
		} else if (columnIndex == 3) {
			return Dna.data.getCoderRelations().get(rowIndex).isViewDocuments();
		} else if (columnIndex == 4) {
			return Dna.data.getCoderRelations().get(rowIndex).isEditDocuments();
		} else {
			return null;
		}
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		CoderRelation cr = Dna.data.getCoderRelations().get(rowIndex);
		if (columnIndex == 0) {
			Dna.data.getCoderById(cr.getOtherCoder()).setName((String) aValue);
		} else if (columnIndex == 1) {
			Dna.data.getCoderRelations().get(rowIndex).setViewStatements((boolean) aValue);
			Dna.dna.sql.updateCoderRelationViewStatements(cr.getId(), (boolean) aValue);
		} else if (columnIndex == 2) {
			Dna.data.getCoderRelations().get(rowIndex).setEditStatements((boolean) aValue);
			Dna.dna.sql.updateCoderRelationEditStatements(cr.getId(), (boolean) aValue);
		} else if (columnIndex == 3) {
			Dna.data.getCoderRelations().get(rowIndex).setViewDocuments((boolean) aValue);
			Dna.dna.sql.updateCoderRelationViewDocuments(cr.getId(), (boolean) aValue);
		} else if (columnIndex == 4) {
			Dna.data.getCoderRelations().get(rowIndex).setEditDocuments((boolean) aValue);
			Dna.dna.sql.updateCoderRelationEditDocuments(cr.getId(), (boolean) aValue);
		}
		
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
		//fireTableCellUpdated(rowIndex, columnIndex);
		
		// update statement-related GUI parts
		Dna.gui.textPanel.paintStatements();
		Dna.gui.rightPanel.statementPanel.statementFilter.updateFilter();
		
		// update document-related GUI parts
		Dna.gui.documentPanel.documentFilter();
	}
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add( l );
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove( l );
	}
	
	public void clear() {
		Dna.data.getCoderRelations().clear();

		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
}