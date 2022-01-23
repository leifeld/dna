package gui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import model.TableDocument;

/**
 * A document table model that holds {@link model.TableDocument
 * TableDocument} objects, which are a shallow representation of document
 * meta-data without the actual text (to make their retrieval from the database
 * more efficient). The table model knows how to remove documents from the
 * database and edit meta-data in the database and represent document meta-data
 * in a table.
 */
@SuppressWarnings("serial")
class DocumentTableModel extends AbstractTableModel {
	private ArrayList<TableDocument> rows;
	
	/**
	 * Create an instance of the document table model class.
	 */
	DocumentTableModel() {
		rows = new ArrayList<TableDocument>();
	}
	
	@Override
	public int getColumnCount() {
		return 11;
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
		case 1: return rows.get(rowIndex).getTitle();
		case 2: return rows.get(rowIndex).getFrequency();
		case 3: return rows.get(rowIndex).getDateTime();
		case 4: return rows.get(rowIndex).getDateTime();
		case 5: return rows.get(rowIndex).getCoder();
		case 6: return rows.get(rowIndex).getAuthor();
		case 7: return rows.get(rowIndex).getSource();
		case 8: return rows.get(rowIndex).getSection();
		case 9: return rows.get(rowIndex).getType();
		case 10: return rows.get(rowIndex).getNotes();
		default: return null;
		}
	}
	
	/**
	 * Set the value of a table cell.
	 * 
	 * @param object  The new object to set.
	 * @param row     The row in which to set a new value.
	 * @param column  The column in which to set a new value.
	 */
	public void setValueAt(Object object, int row, int column) {
		switch(column) {
		case 0: break;
		case 1: rows.get(row).setTitle((String) object); break;
		case 2: rows.get(row).setFrequency((int) object); break;
		case 3: rows.get(row).setDateTime((LocalDateTime) object); break;
		case 4: rows.get(row).setDateTime((LocalDateTime) object); break;
		case 5: rows.get(row).setCoder((Coder) object); break;
		case 6: rows.get(row).setAuthor((String) object); break;
		case 7: rows.get(row).setSource((String) object); break;
		case 8: rows.get(row).setSection((String) object); break;
		case 9: rows.get(row).setType((String) object); break;
		case 10: rows.get(row).setNotes((String) object); break;
		}
		fireTableCellUpdated(row, column);
	}

	/**
	 * Return the name of a column.
	 * 
	 * @param column  The column index.
	 * @return        The name of the column for the table header.
	 */
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

	/**
	 * Which type of object (i.e., class) shall be shown in the columns?
	 * 
	 * @param columnIndex  The column index (starting at {@code 0} of the column
	 *   for which the class should be returned.
	 * @return             The class of the column.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return Integer.class;       // ID
			case 1: return String.class;        // Title
			case 2: return Integer.class;       // #
			case 3: return LocalDateTime.class; // Date
			case 4: return LocalDateTime.class; // Time
			case 5: return Coder.class;         // Coder
			case 6: return String.class;        // Author
			case 7: return String.class;        // Source
			case 8: return String.class;        // Section
			case 9: return String.class;        // Type
			case 10: return String.class;       // Notes
			default: return null;
		}
	}

	/**
	 * Is a specific cell editable?
	 * 
	 * @param rowIndex     The row index of the cell.
	 * @param columnIndex  The column index of the cell.
	 * @return boolean indicating if the cell can be edited.
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex > rows.size() - 1 || columnIndex == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get the ID of a document stored in a specific model row.
	 * 
	 * @param row  The model row.
	 * @return     The ID of the document.
	 * 
	 * @see {@link #getModelRowById(int documentId)}
	 */
	public int getIdByModelRow(int row) {
		return rows.get(row).getId();
	}
	
	/**
	 * Get the row in the model list in which a document with a specific ID is
	 * stored.
	 * 
	 * @param documentId  The document ID.
	 * @return            The model row.
	 * 
	 * @see {@link #getIdByModelRow(int row)}
	 */
	public int getModelRowById(int documentId) {
		for (int i = 0; i < rows.size(); i++) {
			if (rows.get(i).getId() == documentId) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Return the table document (i.e., a shallow representation of the document
	 * without the actual text) that corresponds to a model index. This is
	 * useful for filtering the table in the GUI.
	 * 
	 * @param modelRowIndex The index of the document in the model.
	 * @return              A {@link model.TableDocument TableDocument}
	 *   object.
	 */
	public TableDocument getRow(int modelRowIndex) {
		return rows.get(modelRowIndex);
	}
	
	/**
	 * Add one to the frequency column for a specific document.
	 * 
	 * @param documentId  ID of the document.
	 */
	public void increaseFrequency(int documentId) {
		int row = getModelRowById(documentId);
		rows.get(row).setFrequency(rows.get(row).getFrequency() + 1);
		fireTableCellUpdated(row, 2);
	}

	/**
	 * Subtract one from the frequency column for a specific document.
	 * 
	 * @param documentId  ID of the document.
	 */
	public void decreaseFrequency(int documentId) {
		int row = getModelRowById(documentId);
		if (row == -1) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"Document row could not be identified in the document table model.",
					"Document row for Document " + documentId + " could not be identified in the document table model.");
			Dna.logger.log(l);
		} else if (rows.get(row).getFrequency() > 0) {
			rows.get(row).setFrequency(rows.get(row).getFrequency() - 1);
			fireTableCellUpdated(row, 2);
		}
	}

	/**
	 * Remove an array of document indices from the model and notify the table.
	 * 
	 * @param rows  The model rows of the documents.
	 */
	public void removeDocuments(int[] modelRowIndices) {
		for (int i = modelRowIndices.length - 1; i >= 0; i--) {
			rows.remove(modelRowIndices[i]);
			fireTableRowsDeleted(modelRowIndices[i], modelRowIndices[i]);
		}
	}

	/**
	 * Delete all {@link TableDocument} objects from the table model and notify
	 * the listeners.
	 */
	void clear() {
		rows.clear();
		fireTableDataChanged();
	}

	/**
	 * Insert a table document into the array list of documents at the right
	 * index and fire table update.
	 * 
	 * @param d  The table document to insert.
	 * @return   Model index where the new document was inserted.
	 */
	int addRow(TableDocument d) {
		int newRowIndex = -1;
		boolean inserted = false;
		for (int i = 0; i < rows.size(); i++) {
			if (d.compareTo(rows.get(i)) == -1) {
				rows.add(i, d);
				fireTableRowsInserted(i, i);
				newRowIndex = i;
				inserted = true;
				break;
			}
		}
		if (!inserted && d.compareTo(rows.get(rows.size() - 1)) == 1) {
			rows.add(d);
			fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
			newRowIndex = rows.size() - 1;
		}
		return newRowIndex;
	}
	
	/**
	 * Add a list of {@link TableDocument} objects to the table model and notify
	 * the listeners.
	 * 
	 * @param chunks A list of {@link TableDocument} objects.
	 */
	void addRows(List<TableDocument> chunks) {
    	int n = this.rows.size();
        for (TableDocument row : chunks) {
            rows.add(row);
        }
        fireTableRowsInserted(n, n + chunks.size() - 1); // subtract one because we don't need the cursor to be at the next position; it should refer to the last position
	}
	
	/**
	 * Sort the table documents by date and then ID.
	 */
	void sort() {
		Collections.sort(rows);
		fireTableDataChanged();
	}
}