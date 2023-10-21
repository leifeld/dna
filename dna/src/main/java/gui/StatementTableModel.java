package gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import model.Coder;
import model.TableStatement;

/**
 * A table model for the statements shown in the statement panel.
 */
class StatementTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 3231562380143470667L;
	private ArrayList<TableStatement> rows;
	
	/**
	 * Create a new statement table model.
	 */
	StatementTableModel() {
		rows = new ArrayList<TableStatement>();
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
	 * @return              A {@link model.TableStatement TableStatement} object.
	 */
	public TableStatement getRow(int modelRowIndex) {
		return rows.get(modelRowIndex);
	}

	/**
	 * Get all statements from the table.
	 * 
	 * @return An array list of all statements.
	 */
	public ArrayList<TableStatement> getRows() {
		return rows;
	}
	
	/**
	 * Remove an array of statement indices from the model and notify the table.
	 * 
	 * @param modelRowIndices The model rows of the statements to be removed.
	 */
	public void removeStatements(int[] modelRowIndices) {
		for (int i = modelRowIndices.length - 1; i >= 0; i--) {
			rows.remove(modelRowIndices[i]);
			fireTableRowsDeleted(modelRowIndices[i], modelRowIndices[i]);
		}
	}

	/**
	 * Remove statements from a set of documents from the model and notify the table.
	 * 
	 * @param documentIds  An array list of document IDs.
	 */
	public void removeStatementsByDocuments(ArrayList<Integer> documentIds) {
		for (int i = rows.size() - 1; i >= 0; i--) {
			if (documentIds.contains(rows.get(i).getDocumentId())) {
				rows.remove(i);
				fireTableRowsDeleted(i, i);
			}
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
	 * Insert a table statement into the array list of statements at the right index and fire table update.
	 * 
	 * @param s  The table statement to insert.
	 * @return   New row index of the added statement.
	 */
	int addRow(TableStatement s) {
		int newRowIndex = -1;
		if (rows.size() > 0) {
			for (int i = 0; i < rows.size(); i++) {
				if (s.compareTo(rows.get(i)) == -1) {
					newRowIndex = i;
					break;
				}
			}
		} else {
			newRowIndex = 0;
		}
		if (newRowIndex == -1) { // there were other statements, but the new statement is more recent than all of them
			newRowIndex = rows.size();
		}
		rows.add(newRowIndex, s);
		fireTableRowsInserted(newRowIndex, newRowIndex);
		return newRowIndex;
	}
	
	/**
	 * Add a list of {@link model.TableStatement TableStatement} objects to the table model and notify the listeners.
	 * 
	 * @param chunks A list of {@link model.TableStatement TableStatement}
	 *   objects.
	 */
	void addRows(List<TableStatement> chunks) {
    	int n = this.rows.size();
        for (TableStatement row : chunks) {
            rows.add(row);
        }
        fireTableRowsInserted(n, n + chunks.size() - 1); // subtract one because we don't need the cursor to be at the next position; it should refer to the last position
	}
	
	/**
	 * Take a list of statements and replace any existing statements with the
	 * same IDs by these statements.
	 * 
	 * @param chunks
	 */
	void updateStatements(List<TableStatement> chunks) {
		for (int i = 0; i < chunks.size(); i++) {
			for (int j = 0; j < rows.size(); j++) {
				if (rows.get(j).getId() == chunks.get(i).getId()) {
					rows.set(j, chunks.get(i));
					this.fireTableRowsUpdated(j, j);
					break;
				}
			}
		}
	}

	/**
	 * Update the coder in a statement/row.
	 *
	 * @param row The model row.
	 * @param coderId The new coder ID.
	 * @param coderName The new coder name.
	 * @param coderColor The new coder color.
	 */
	void updateCoderInRow(int row, int coderId, String coderName, model.Color coderColor) {
		this.rows.get(row).setCoderId(coderId);
		this.rows.get(row).setCoderName(coderName);
		this.rows.get(row).setCoderColor(coderColor);
		this.fireTableCellUpdated(row, 4);
	}
	
	void sort() {
		Collections.sort(rows);
		fireTableDataChanged();
	}
}