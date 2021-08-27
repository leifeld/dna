package guiCoder;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import dna.TableDocument;
import dna.TableStatement;

class StatementTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 3231569380143470667L;
	ArrayList<TableStatement> rows;
	
	StatementTableModel() {
		rows = new ArrayList<TableStatement>();
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rows.size() == 0 || rowIndex > rows.size() - 1) {
			return null;
		}
		switch(columnIndex) {
		case 0: return rows.get(rowIndex).getId();
		case 1: return rows.get(rowIndex).getStart();
		case 2: return rows.get(rowIndex).getStop();
		case 3: return rows.get(rowIndex).getCoderId();
		case 4: return rows.get(rowIndex).getText();
		default: return null;
		}
	}

	//return the name of a column
	public String getColumnName(int column) {
		switch( column ){
			case 0: return "ID";
			case 1: return "Start";
			case 2: return "End";
			case 3: return "Coder";
			case 4: return "Text";
			default: return null;
		}
	}

	// which type of object (i.e., class) shall be shown in the columns?
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return Integer.class; // ID
			case 1: return Integer.class; // Start
			case 2: return Integer.class; // End
			case 3: return Integer.class; // Coder
			case 4: return String.class;  // Text
			default: return null;
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public int getModelRowById(int statementId) {
		for (int i = 0; i < rows.size(); i++) {
			if (rows.get(i).getId() == statementId) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Delete all {@link dna.TableStatement TableStatement} objects from the
	 * table model and notify the listeners.
	 */
	void clear() {
		rows.clear();
		fireTableDataChanged();
	}
	
	/**
	 * Add a list of {@link dna.TableStatement TableStatement} objects to
	 * the table model and notify the listeners.
	 * 
	 * @param chunks A list of {@link dna.TableStatement TableStatement}
	 *   objects.
	 */
	void addRows(List<TableStatement> chunks) {
    	int n = this.rows.size();
        for (TableStatement row : chunks) {
            rows.add(row);
        }
        fireTableRowsInserted(n, n + chunks.size() - 1); // subtract one because we don't need the cursor to be at the next position; it should refer to the last position
	}
}