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
	DocumentTableSwingWorker worker;
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
    		worker = new DocumentTableSwingWorker();
            worker.execute();
    	} else {
            fireTableDataChanged();
    	}
	}
	
	public void updateFrequency(int documentId) {
		int row = getModelRowById(documentId);
		rows.get(row).setFrequency(rows.get(row).getFrequency() + 1);
		fireTableCellUpdated(row, 2);
	}

	public void removeDocuments(int[] rows) {
		for (int i = 0; i < rows.length; i++) {
			rows[i] = getIdByModelRow(rows[i]);
		}
		Dna.sql.deleteDocuments(rows);
		fireTableDataChanged();
	}
	
	/**
	 * Swing worker class for loading documents from the database and adding
	 * them to the document table in a background thread.
	 * 
	 * https://stackoverflow.com/questions/43161033/cant-add-tablerowsorter-to-jtable-produced-by-swingworker
	 */
	private class DocumentTableSwingWorker extends SwingWorker<List<TableDocument>, TableDocument> {
		long time;
		int n;
		
		/**
		 * Create a new swing worker.
		 */
		public DocumentTableSwingWorker() {
    		Dna.guiCoder.statusBar.setDocumentRefreshing(true);
    		time = System.nanoTime();
		}
		
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
        	n = getRowCount();
            for (TableDocument row : chunks) {
                rows.add(row);
            }
            fireTableRowsInserted(n, n + chunks.size() - 1); // subtract one because we don't need the cursor to be at the next position; it should refer to the last position
        }

        @Override
        protected void done() {
            // fireTableDataChanged();
    		Dna.guiCoder.statusBar.setDocumentRefreshing(false);
    		long elapsed = System.nanoTime();
    		System.out.println("(Re)loaded all documents in " + (elapsed - time) / 1000000 + " milliseconds.");
        }
    }

	/**
	 * Represents the rows in a {@link DocumentTableModel}.
	 */
	public class TableDocument {
		int id, frequency;
		Coder coder;
		String title, author, source, section, type, notes;
		LocalDateTime dateTime;

		/**
		 * Create an instance of a table row.
		 * 
		 * @param id The document ID of the {@link Document}.
		 * @param title The title of the {@link Document}.
		 * @param frequency How many statements does the {@link Document} contain?
		 * @param coder ID of the coder who added the {@link Document}.
		 * @param author Author of the {@link Document}.
		 * @param source Source of the {@link Document}.
		 * @param section Section of the {@link Document}.
		 * @param type Type of the {@link Document}.
		 * @param notes Notes for the {@link Document}.
		 * @param dateTime The date and time the {@link Document} happened.
		 */
		TableDocument(int id, String title, int frequency, Coder coder, String author, String source, String section,
				String type, String notes, LocalDateTime dateTime) {
			this.id = id;
			this.frequency = frequency;
			this.coder = coder;
			this.title = title;
			this.author = author;
			this.source = source;
			this.section = section;
			this.type = type;
			this.notes = notes;
			this.dateTime = dateTime;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getFrequency() {
			return frequency;
		}

		public void setFrequency(int frequency) {
			this.frequency = frequency;
		}

		public Coder getCoder() {
			return coder;
		}

		public void setCoder(Coder coder) {
			this.coder = coder;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getSection() {
			return section;
		}

		public void setSection(String section) {
			this.section = section;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getNotes() {
			return notes;
		}

		public void setNotes(String notes) {
			this.notes = notes;
		}

		public LocalDateTime getDateTime() {
			return dateTime;
		}

		public void setDateTime(LocalDateTime dateTime) {
			this.dateTime = dateTime;
		}
	}
}