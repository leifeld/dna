package dna;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import dna.dataStructures.Statement;
import dna.dataStructures.StatementType;

public class StatementTypeTableModel implements TableModel {
	
	Vector<TableModelListener> listeners = new Vector<TableModelListener>();
	
	public void addTableModelListener(TableModelListener l) {
		listeners.add( l );
	}
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove( l );
	}
	
	public StatementTypeTableModel(ArrayList<StatementType> types) {
		Dna.data.setStatementTypes(types);
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return String.class;
			case 1: return Integer.class;
			default: return null;
		}
	}
	
	public int getColumnCount() {
		return 2;
	}
	
	public String getColumnName(int column) {
		switch( column ){
			case 0: return "statement type";
			case 1: return "count";
			default: return null;
		}
	}
	
	public int getRowCount() {
		return Dna.data.getStatementTypes().size();
	}
	
	public int size() {
		return Dna.data.getStatementTypes().size();
	}
	
	public void remove(int index) {
		Dna.data.getStatementTypes().remove(index);
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		StatementType t = Dna.data.getStatementTypes().get(rowIndex);
		if (columnIndex == 0) {
			return t.getLabel();
		} else if (columnIndex == 1) {
			//ArrayList<Statement> s = Dna.dna.db.getStatementsByType(t.getLabel());
			ArrayList<Statement> s = Dna.data.getStatementsByType(t.getLabel());
			int count = s.size();
			return count;
		} else {
			return null;
		}
	}

	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch( columnIndex ){
			case 0: 
				String newLabel = (String) aValue;
				String oldLabel = Dna.data.getStatementTypes().get(rowIndex).getLabel();
				//Color col = Dna.dna.db.getStatementTypeColor(oldLabel);
				Color col = Dna.data.getStatementType(oldLabel).getColor();
				int red = col.getRed();
				int green = col.getGreen();
				int blue = col.getBlue();
				//Dna.dna.db.changeStatementType(oldLabel, newLabel, red, green, blue);
				Dna.data.getStatementType(oldLabel).setLabel(newLabel);
				Dna.data.getStatementType(newLabel).setColor(new Color(red, green, blue));
				Dna.data.getStatementTypes().get(rowIndex).setLabel(newLabel);
				break;
			case 1: 
				break;
		}
	}

	public void addStatementType(StatementType statementType) {
		Dna.data.getStatementTypes().add(statementType);
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public StatementType get(int index) {
		return Dna.data.getStatementTypes().get(index);
	}
	
	public StatementType get(String label) throws NullPointerException {
		for (int i = 0; i < Dna.data.getStatementTypes().size(); i++) {
			if (Dna.data.getStatementTypes().get(i).getLabel().equals(label)) {
				return Dna.data.getStatementTypes().get(i);
			}
		}
		throw new NullPointerException();
	}
	
	public int getIndexByLabel(String label) {
		int index = -1;
		for (int i = 0; i < Dna.data.getStatementTypes().size(); i++) {
			if (Dna.data.getStatementTypes().get(i).getLabel().equals(label)) {
				index = i;
			}
		}
		return index;
	}
	
	public void clear() {
		Dna.data.getStatementTypes().clear();
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
}