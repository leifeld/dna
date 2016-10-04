package dna.renderer;

import java.awt.Color;

import javax.swing.table.AbstractTableModel;

import dna.Dna;
import dna.dataStructures.AttributeVector;

@SuppressWarnings("serial")
public class AttributeTableModel extends AbstractTableModel {
	
	public int getColumnCount() {
		return 5;
	}
	
	public int getRowCount() {
		return Dna.data.getAttributes().size();
	}
	
	public String getColumnName(int column) {
		switch( column ){
			case 0: return "Value";
			case 1: return "Color";
			case 2: return "Type";
			case 3: return "Alias";
			case 4: return "Notes";
			default: return null;
		}
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch( columnIndex ){
			case 0: return Dna.data.getAttributes().get(rowIndex).getValue(); 
			case 1: return Dna.data.getAttributes().get(rowIndex).getColor();
			case 2: return Dna.data.getAttributes().get(rowIndex).getType();
			case 3: return Dna.data.getAttributes().get(rowIndex).getAlias();
			case 4: return Dna.data.getAttributes().get(rowIndex).getNotes();
			default: return null;
		}
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return String.class;
			case 1: return Color.class;
			case 2: return String.class;
			case 3: return String.class;
			case 4: return String.class;
			default: return null;
		}	
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			dna.Dna.data.getAttributes().get(rowIndex).setValue((String) aValue);
		} else if (columnIndex == 1) {
			dna.Dna.data.getAttributes().get(rowIndex).setColor((Color) aValue);
		} else if (columnIndex == 2) {
			dna.Dna.data.getAttributes().get(rowIndex).setType((String) aValue);
		} else if (columnIndex == 3) {
			dna.Dna.data.getAttributes().get(rowIndex).setAlias((String) aValue);
		} else if (columnIndex == 4) {
			dna.Dna.data.getAttributes().get(rowIndex).setNotes((String) aValue);
		}
		this.fireTableCellUpdated(rowIndex, columnIndex);
	}
	
	/**
	 * Add a row to the table (and its contents to the data structure).
	 * 
	 * @param av  New attribute vector
	 */
	public void addRow(AttributeVector av) {
		Dna.dna.addAttributeVector(av);
		//this.fireTableRowsInserted(Dna.data.getAttributes().size() - 1, Dna.data.getAttributes().size() - 1);
		fireTableDataChanged();
	}
	
	/**
	 * Get the attribute vector stored in a particular row.
	 * 
	 * @param row  The row that should be retrieved
	 * @return     An AttributeVector object
	 */
	public AttributeVector get(int row) {
		return Dna.data.getAttributes().get(row);
	}
}