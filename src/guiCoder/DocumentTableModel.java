package guiCoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import dna.Dna;

@SuppressWarnings("serial")
public class DocumentTableModel extends AbstractTableModel {
	private List<TableDocument> rows;
	JDBCWorker worker;
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MM yyyy");
	DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
	
	public DocumentTableModel() {
		rows = new ArrayList<>();
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
		case 3: return rows.get(rowIndex).getDateTime().format(dateFormatter);
		case 4: return rows.get(rowIndex).getDateTime().format(timeFormatter);
		case 5: return rows.get(rowIndex).getCoder();
		case 6: return rows.get(rowIndex).getAuthor();
		case 7: return rows.get(rowIndex).getSource();
		case 8: return rows.get(rowIndex).getSection();
		case 9: return rows.get(rowIndex).getType();
		case 10: return rows.get(rowIndex).getNotes();
		default: return null;
		}
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
	
	// which type of object (i.e., class) shall be shown in the columns?
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return Integer.class; // ID
			case 1: return String.class;  // Title
			case 2: return Integer.class; // #
			case 3: return String.class;  // Date
			case 4: return String.class;  // Time
			case 5: return Coder.class;   // Coder
			case 6: return String.class;  // Author
			case 7: return String.class;  // Source
			case 8: return String.class;  // Section
			case 9: return String.class;  // Type
			case 10: return String.class; // Notes
			default: return null;
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public int getIdByModelRow(int row) {
		return rows.get(row).getId();
	}
	
	public int getModelRowById(int documentId) {
		for (int i = 0; i < rows.size(); i++) {
			if (rows.get(i).getId() == documentId) {
				return i;
			}
		}
		return -1;
	}
	
	public String getDocumentText(int documentId) {
		return Dna.sql.getDocumentText(documentId);
	}
	
	public void reloadTableFromSQL() {
    	rows.clear();
    	if (Dna.sql != null) {
    		worker = new JDBCWorker();
            worker.execute();
    	} else {
            fireTableDataChanged();
    	}
	}

	public void removeDocuments(int[] rows) {
		for (int i = 0; i < rows.length; i++) {
			rows[i] = getIdByModelRow(rows[i]);
		}
		Dna.sql.deleteDocuments(rows);
		fireTableDataChanged();
	}
	
	// https://stackoverflow.com/questions/43161033/cant-add-tablerowsorter-to-jtable-produced-by-swingworker
	private class JDBCWorker extends SwingWorker<List<TableDocument>, TableDocument> {
		
        @Override
        protected List<TableDocument> doInBackground() {
        	try (Connection conn = Dna.sql.getDataSource().getConnection();
					PreparedStatement tableStatement = conn.prepareStatement("SELECT D.ID, Title, (SELECT COUNT(ID) FROM STATEMENTS WHERE DocumentId = D.ID) AS Frequency, C.ID AS CoderId, Name AS CoderName, Red, Green, Blue, Date, Author, Source, Section, Type, Notes FROM CODERS C INNER JOIN DOCUMENTS D ON D.Coder = C.ID;")) {
            	ResultSet rs = tableStatement.executeQuery();
                while (rs.next()) {
                	TableDocument r = new TableDocument(
                			rs.getInt("ID"),
                			rs.getString("Title"),
                			rs.getInt("Frequency"),
                			new Coder(rs.getInt("CoderId"),
                					rs.getString("CoderName"),
                					rs.getInt("Red"),
                					rs.getInt("Green"),
                					rs.getInt("Blue")),
                			rs.getString("Author"),
                			rs.getString("Source"),
                			rs.getString("Section"),
                			rs.getString("Type"),
                			rs.getString("Notes"),
                			LocalDateTime.ofEpochSecond(rs.getLong("Date"), 0, ZoneOffset.UTC));
                    publish(r);
                }
			} catch (SQLException e) {
				System.err.println("Could not retrieve documents from database.");
				e.printStackTrace();
			}
            return null;
        }
        
        @Override
        protected void process(List<TableDocument> chunks) {
            for (TableDocument row : chunks) {
                rows.add(row);
            }
            fireTableDataChanged();
        }

        @Override
        protected void done() {
            fireTableDataChanged();
        }
    }
}