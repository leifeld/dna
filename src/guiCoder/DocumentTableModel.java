package guiCoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import dna.Dna;
import sql.Sql.SQLCloseable;
import stack.RemoveDocumentsEvent;
import stack.StackDocument;
import stack.StackStatement;
import stack.StackValue;

@SuppressWarnings("serial")
public class DocumentTableModel extends AbstractTableModel {
	//PreparedStatement tableStatement;
	//ResultSet rs;
	private List<TableDocument> rows;
	JDBCWorker worker;
	//String sqlString;
	
	public DocumentTableModel() {
		rows = new ArrayList<>();
		/*
		if (Dna.sql.getConnectionProfile().getType().equals("sqlite")) {
        	try {
				tableStatement = Dna.sql.sqliteConnection.prepareStatement(sqlString);
			} catch (SQLException e) {
				System.out.println("Could not prepare document table model SQL statement.");
				e.printStackTrace();
			}
		}
		
		this.reloadTableFromSQL();
		*/
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
		if (rowIndex > rows.size() - 1) {
			return null;
		}
		switch(columnIndex) {
		case 0: return rows.get(rowIndex).getId();
		case 1: return rows.get(rowIndex).getTitle();
		case 2: return rows.get(rowIndex).getFrequency();
		case 3: return rows.get(rowIndex).getDate();
		case 4: return new SimpleDateFormat("HH:mm:ss").format(rows.get(rowIndex).getDate());
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
	
	//which type of object (i.e., class) shall be shown in the columns?
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return Integer.class; // ID
			case 1: return String.class;  // Title
			case 2: return Integer.class; // #
			case 3: return Date.class;    // Date
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
	
	public void reloadTableFromSQL() {
    	rows.clear();
		worker = new JDBCWorker();
        worker.execute();
	}

	public void removeDocuments(int[] rows) {
		int[] ids = new int[rows.length];
		for (int i = 0; i < rows.length; i++) {
			ids[i] = (int) getValueAt(rows[i], 0);
		}
		ArrayList<StackDocument> documents = null;
		if (Dna.sql.getConnectionProfile().getType().equals("sqlite")) {
			try {
				Dna.sql.sqliteConnection.setAutoCommit(false);
				documents = removeDocumentsHelper(Dna.sql.sqliteConnection, ids);
			} catch (SQLException e) {
				System.err.println("Could not establish connection to database to retrieve documents with statements.");
				e.printStackTrace();
				if (Dna.sql.sqliteConnection != null) {
					try {
						System.err.println("Transaction is being rolled back.");
						Dna.sql.sqliteConnection.rollback();
					} catch (SQLException excep) {
						System.err.println("Transaction could not be rolled back.");
						excep.printStackTrace();
					}
				}
			} finally {
				try {
					Dna.sql.sqliteConnection.setAutoCommit(true);
				} catch (SQLException excep) {
					System.err.println("Could not set AutoCommit back to true.");
					excep.printStackTrace();
				}
			}
		} else if (Dna.sql.getConnectionProfile().getType().equals("mysql") || Dna.sql.getConnectionProfile().getType().equals("postgresql")) {
			try (Connection conn = Dna.sql.ds.getConnection();
					 SQLCloseable finish = conn::rollback) {
				conn.setAutoCommit(false);
				documents = removeDocumentsHelper(conn, ids);
			} catch (SQLException e) {
				System.err.println("Could not establish connection to database to add or remove documents.");
				e.printStackTrace();
			}
		} else {
			System.err.println("Database type not recognized.");
		}
		Dna.guiCoder.stack.add(new RemoveDocumentsEvent(documents));
	}
	
	private ArrayList<StackDocument> removeDocumentsHelper(Connection conn, int[] ids) {
		ArrayList<StackDocument> documents = new ArrayList<StackDocument>();
		ArrayList<StackStatement> statements = new ArrayList<StackStatement>();
		ArrayList<StackValue> values = new ArrayList<StackValue>();
		ResultSet resultDocument, resultStatement, resultValue;
		int statementId;
		try (PreparedStatement docStatement = conn.prepareStatement("SELECT * FROM DOCUMENTS WHERE ID = ?");
				PreparedStatement stStatement = conn.prepareStatement("SELECT * FROM STATEMENTS WHERE DocumentId = ?");
				PreparedStatement intStatement = conn.prepareStatement("SELECT Value, Variable, DataType FROM DATABOOLEAN INNER JOIN VARIABLES ON VARIABLES.ID = DATABOOLEAN.VariableId WHERE StatementId = ? UNION ALL SELECT Value, Variable, DataType FROM DATAINTEGER INNER JOIN VARIABLES ON VARIABLES.ID = DATAINTEGER.VariableId WHERE StatementId = ?");
				PreparedStatement textStatement = conn.prepareStatement("SELECT Value, Variable, DataType FROM DATASHORTTEXT INNER JOIN VARIABLES ON VARIABLES.ID = DATASHORTTEXT.VariableId WHERE StatementId = ? UNION ALL SELECT Value, Variable, DataType FROM DATALONGTEXT INNER JOIN VARIABLES ON VARIABLES.ID = DATALONGTEXT.VariableId WHERE StatementId = ?")) {
			for (int i = 0; i < ids.length; i++) {
				docStatement.setInt(1, ids[i]);
				resultDocument = docStatement.executeQuery();
				while (resultDocument.next()) {
					statements = new ArrayList<StackStatement>();
					stStatement.setInt(1, ids[i]);
					resultStatement = stStatement.executeQuery();
					while (resultStatement.next()) {
						statementId = resultStatement.getInt("ID");
						values = new ArrayList<StackValue>();
						textStatement.setInt(1, statementId);
						textStatement.setInt(2, statementId);
						resultValue = textStatement.executeQuery();
						while (resultValue.next()) {
							values.add(new StackValue(resultValue.getString("Variable"), resultValue.getString("DataType"), resultValue.getInt("Value")));
						}
						intStatement.setInt(1, statementId);
						intStatement.setInt(2, statementId);
						resultValue = intStatement.executeQuery();
						while (resultValue.next()) {
							values.add(new StackValue(resultValue.getString("Variable"), resultValue.getString("DataType"), resultValue.getInt("Value")));
						}
						statements.add(new StackStatement(statementId, resultStatement.getInt("Coder"), resultStatement.getInt("Start"), resultStatement.getInt("Stop"), resultStatement.getInt("StatementTypeId"), values));
					}
					documents.add(new StackDocument(ids[i], resultDocument.getInt("Coder"), resultDocument.getString("Title"), resultDocument.getString("Text"), resultDocument.getString("Author"), resultDocument.getString("Source"), resultDocument.getString("Section"), resultDocument.getString("Type"), resultDocument.getString("Notes"), new Date(resultDocument.getLong("Date")), statements));
				}
			}
		} catch (SQLException ex) {
			System.err.println("Could not select documents and statements for removal.");
			ex.printStackTrace();
		}
		return documents;
	}
	
	// https://stackoverflow.com/questions/43161033/cant-add-tablerowsorter-to-jtable-produced-by-swingworker
	private class JDBCWorker extends SwingWorker<List<TableDocument>, TableDocument> {
		
        @Override
        protected List<TableDocument> doInBackground() {
        	String sqlString = "SELECT D.ID, Title, (SELECT COUNT(ID) FROM STATEMENTS WHERE DocumentId = D.ID) AS Frequency, C.ID AS CoderId, Name AS CoderName, Red, Green, Blue, Date, Author, Source, Section, Type, Notes FROM CODERS C INNER JOIN DOCUMENTS D ON D.Coder = C.ID;";
        	if (Dna.sql.getConnectionProfile().getType().equals("sqlite")) {
                try (PreparedStatement tableStatement = Dna.sql.sqliteConnection.prepareStatement(sqlString)) {
                	ResultSet rs = tableStatement.executeQuery();
                    while (rs.next()) {
                    	TableDocument r = new TableDocument(rs.getInt("ID"), rs.getString("Title"), rs.getInt("Frequency"), new Coder(rs.getInt("CoderId"), rs.getString("CoderName"), rs.getInt("Red"), rs.getInt("Green"), rs.getInt("Blue")), rs.getString("Author"), rs.getString("Source"), rs.getString("Section"), rs.getString("Type"), rs.getString("Notes"), new Date(rs.getLong("Date")));
                        publish(r);
                    }
					rs.close();
                } catch (SQLException e) {
					System.err.println("Could not retrieve documents from database.");
                    e.printStackTrace();
                }
        	} else if (Dna.sql.getConnectionProfile().getType().equals("mysql") || Dna.sql.getConnectionProfile().getType().equals("postgresql")) {
        		try (Connection conn = Dna.sql.ds.getConnection();
    					PreparedStatement tableStatement = conn.prepareStatement(sqlString)) {
                	ResultSet rs = tableStatement.executeQuery();
                    while (rs.next()) {
                    	TableDocument r = new TableDocument(rs.getInt("ID"), rs.getString("Title"), rs.getInt("Frequency"), new Coder(rs.getInt("CoderId"), rs.getString("CoderName"), rs.getInt("Red"), rs.getInt("Green"), rs.getInt("Blue")), rs.getString("Author"), rs.getString("Source"), rs.getString("Section"), rs.getString("Type"), rs.getString("Notes"), new Date(rs.getLong("Date")));
                        publish(r);
                    }
				} catch (SQLException e) {
					System.err.println("Could not retrieve documents from database.");
					e.printStackTrace();
				}
        	} else {
        		System.err.println("Database type not recognized.");
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