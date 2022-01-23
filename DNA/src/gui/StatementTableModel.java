package gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import model.Coder;
import model.Statement;

/**
 * A table model for the statements shown in the statement panel.
 */
class StatementTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 3231569380143470667L;
	private ArrayList<Statement> rows;
	
	/**
	 * Create a new statement table model.
	 */
	StatementTableModel() {
		rows = new ArrayList<Statement>();
	}

	@Override
	public int getColumnCount() {
		return 6;
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
		case 1: return rows.get(rowIndex).getDocumentId();
		case 2: return rows.get(rowIndex).getStart();
		case 3: return rows.get(rowIndex).getStop();
		case 4: return new Coder(rows.get(rowIndex).getCoderId(), rows.get(rowIndex).getCoderName(), rows.get(rowIndex).getCoderColor());
		case 5: return rows.get(rowIndex).getText();
		default: return null;
		}
	}

	/**
	 * Return the name of a column.
	 * 
	 * @param column  Column position, starting with {@code 0} for the first
	 *   column.
	 * @return        Name of the column.
	 */
	public String getColumnName(int column) {
		switch( column ){
			case 0: return "ID";
			case 1: return "Doc";
			case 2: return "Start";
			case 3: return "End";
			case 4: return "Coder";
			case 5: return "Text";
			default: return null;
		}
	}

	/**
	 * Which type of object (i.e., class) shall be shown in the columns?
	 * 
	 * @param columnIndex  Index of the column.
	 * @return             Class of the column.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return Integer.class; // Statement ID
			case 1: return Integer.class; // Document ID
			case 2: return Integer.class; // Start
			case 3: return Integer.class; // End
			case 4: return Coder.class;   // Coder
			case 5: return String.class;  // Text
			default: return null;
		}
	}

	/**
	 * Is the respective cell editable?
	 * 
	 * @param rowIndex     The row.
	 * @param columnIndex  The column.
	 * @return boolean indicating whether the cell is editable.
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * Get the ID of a statement stored in a specific model row.
	 * 
	 * @param row  The model row.
	 * @return     The ID of the statement.
	 * 
	 * @see {@link #getModelRowById(int statementId)}
	 */
	public int getIdByModelRow(int row) {
		return rows.get(row).getId();
	}
	
	/**
	 * Get the model row in which a statement with a specific ID is stored.
	 * 
	 * @param statementId  ID of the statement.
	 * @return             Row in the model.
	 * 
	 * @see {@link #getIdByModelRow(int row)}
	 */
	public int getModelRowById(int statementId) {
		for (int i = 0; i < rows.size(); i++) {
			if (rows.get(i).getId() == statementId) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Return the table statement that corresponds to a model index. This is
	 * useful for filtering the table in the GUI and rendering contents.
	 * 
	 * @param modelRowIndex The index of the statement in the model.
	 * @return              A {@link model.TableStatement TableStatement}
	 *   object.
	 */
	public Statement getRow(int modelRowIndex) {
		return rows.get(modelRowIndex);
	}

	public ArrayList<Statement> getRows() {
		return rows;
	}
	
	/**
	 * Remove an array of statements from the model and notify the table.
	 * 
	 * @param rows  The model rows of the statements.
	 */
	public void removeStatements(int[] modelRowIndices) {
		for (int i = modelRowIndices.length - 1; i >= 0; i--) {
			rows.remove(modelRowIndices[i]);
			fireTableRowsDeleted(modelRowIndices[i], modelRowIndices[i]);
		}
	}
	
	/**
	 * Delete all {@link model.TableStatement TableStatement} objects from the
	 * table model and notify the listeners.
	 */
	void clear() {
		rows.clear();
		fireTableDataChanged();
	}
	
	/**
	 * Insert a statement into the array list of statements at the right index
	 * and fire table update.
	 * 
	 * @param s  The statement to insert.
	 */
	void addRow(Statement s) {
		boolean inserted = false;
		for (int i = 0; i < rows.size(); i++) {
			if (s.compareTo(rows.get(i)) == -1) {
				rows.add(i, s);
				fireTableRowsInserted(i, i);
				inserted = true;
				break;
			}
		}
		if (!inserted && s.compareTo(rows.get(rows.size() - 1)) == 1) {
			rows.add(s);
			fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
		}
	}
	
	/**
	 * Add a list of {@link model.TableStatement TableStatement} objects to
	 * the table model and notify the listeners.
	 * 
	 * @param chunks A list of {@link model.TableStatement TableStatement}
	 *   objects.
	 */
	void addRows(List<Statement> chunks) {
    	int n = this.rows.size();
        for (Statement row : chunks) {
            rows.add(row);
        }
        fireTableRowsInserted(n, n + chunks.size() - 1); // subtract one because we don't need the cursor to be at the next position; it should refer to the last position
	}
	
	void sort() {
		Collections.sort(rows);
		fireTableDataChanged();
	}
}