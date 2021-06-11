package stack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import dna.Dna;
import sql.Sql.SQLCloseable;

public class RemoveDocumentsEvent extends DnaEvent {
	ArrayList<StackDocument> documents;
	
	
	public RemoveDocumentsEvent(ArrayList<StackDocument> documents) {
		super("Remove Documents Event");
		this.documents = documents;
	}

	@Override
	void doSql() {
		this.documents = undoRemoveDocumentsEvent(false, this.documents);
	}

	@Override
	void undoSql() {
		this.documents = undoRemoveDocumentsEvent(true, this.documents);
	}
	
	public ArrayList<StackDocument> undoRemoveDocumentsEvent(boolean undo, ArrayList<StackDocument> documents) {
		if (Dna.sql.getConnectionProfile().getType().equals("sqlite")) {
			try {
				Dna.sql.sqliteConnection.setAutoCommit(false);
				documents = undoRemoveDocumentsEventHelper(undo, Dna.sql.sqliteConnection, documents);
			} catch (SQLException e) {
				System.err.println("Could not establish connection to database to remove or re-add documents.");
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
				documents = undoRemoveDocumentsEventHelper(undo, conn, documents);
			} catch (SQLException e) {
				System.err.println("Could not establish connection to database to remove or re-add documents.");
				e.printStackTrace();
			}
		} else {
			System.err.println("Database type not recognized.");
		}
		return documents;
	}

	private ArrayList<StackDocument> undoRemoveDocumentsEventHelper(boolean undo, Connection conn, ArrayList<StackDocument> documents) {
		int i, j, k;
		if (undo == false) { // do or re-do action
			try (PreparedStatement s1 = conn.prepareStatement("DELETE FROM DATABOOLEAN WHERE StatementID = ?");
					PreparedStatement s2 = conn.prepareStatement("DELETE FROM DATAINTEGER WHERE StatementID = ?");
					PreparedStatement s3 = conn.prepareStatement("DELETE FROM DATASHORTTEXT WHERE StatementID = ?");
					PreparedStatement s4 = conn.prepareStatement("DELETE FROM DATALONGTEXT WHERE StatementID = ?");
					PreparedStatement s5 = conn.prepareStatement("DELETE FROM STATEMENTS WHERE ID = ?");
					PreparedStatement s6 = conn.prepareStatement("DELETE FROM DOCUMENTS WHERE ID = ?")) {
				for (i = 0; i < documents.size(); i++) {
					for (j = 0; j < documents.get(i).getStatements().size(); j++) {
						s1.setInt(1, documents.get(i).getStatements().get(j).getId());
						s1.executeUpdate();
						s2.setInt(1, documents.get(i).getStatements().get(j).getId());
						s2.executeUpdate();
						s3.setInt(1, documents.get(i).getStatements().get(j).getId());
						s3.executeUpdate();
						s4.setInt(1, documents.get(i).getStatements().get(j).getId());
						s4.executeUpdate();
						s5.setInt(1, documents.get(i).getStatements().get(j).getId());
						s5.executeUpdate();
						documents.get(i).getStatements().get(j).setId(-1);
					}
					s6.setInt(1, documents.get(i).getId());
					s6.executeUpdate();
					documents.get(i).setId(-1);
				}
				conn.commit();
			} catch (SQLException e) {
				System.err.println("Could not remove documents (possibly including contained statements) from the database.");
				e.printStackTrace();
			}
		} else {
			try (PreparedStatement s1 = conn.prepareStatement("INSERT INTO DATABOOLEAN (StatementId, VariableId, StatementTypeId, Value) VALUES (?, (SELECT ID FROM VARIABLES WHERE StatementTypeId = ? AND Variable = ?), ?, ?)");
					PreparedStatement s2 = conn.prepareStatement("INSERT INTO DATAINTEGER (StatementId, VariableId, StatementTypeId, Value) VALUES (?, (SELECT ID FROM VARIABLES WHERE StatementTypeId = ? AND Variable = ?), ?, ?)");
					PreparedStatement s3 = conn.prepareStatement("INSERT INTO DATASHORTTEXT (StatementId, VariableId, StatementTypeId, Value) VALUES (?, (SELECT ID FROM VARIABLES WHERE StatementTypeId = ? AND Variable = ?), ?, ?)");
					PreparedStatement s4 = conn.prepareStatement("INSERT INTO DATALONGTEXT (StatementId, VariableId, StatementTypeId, Value) VALUES (?, (SELECT ID FROM VARIABLES WHERE StatementTypeId = ? AND Variable = ?), ?, ?)");
					PreparedStatement s5 = conn.prepareStatement("INSERT INTO STATEMENTS (StatementTypeId, DocumentId, Start, Stop, Coder) VALUES (?, ?, ?, ?, ?)");
					PreparedStatement s6 = conn.prepareStatement("INSERT INTO DOCUMENTS (Title, Text, Coder, Author, Source, Section, Notes, Type, Date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				for (i = 0; i < documents.size(); i++) {
					s6.setString(1, documents.get(i).getTitle());
					s6.setString(2, documents.get(i).getText());
					s6.setInt(3, documents.get(i).getCoder());
					s6.setString(4, documents.get(i).getAuthor());
					s6.setString(5, documents.get(i).getSource());
					s6.setString(6, documents.get(i).getSection());
					s6.setString(7, documents.get(i).getNotes());
					s6.setString(8, documents.get(i).getDocumentType());
					s6.setLong(9, documents.get(i).getDate().getTime());
					s6.executeUpdate();
					if (s6.getGeneratedKeys().next()) {
						documents.get(i).setId(s6.getGeneratedKeys().getInt(1));
					}
					for (j = 0; j < documents.get(i).getStatements().size(); j++) {
						s5.setInt(1, documents.get(i).getStatements().get(j).getStatementTypeId());
						s5.setInt(2, documents.get(i).getId());
						s5.setInt(3, documents.get(i).getStatements().get(j).getStart());
						s5.setInt(4, documents.get(i).getStatements().get(j).getStop());
						s5.setInt(5, documents.get(i).getStatements().get(j).getCoder());
						s5.executeUpdate();
						if (s5.getGeneratedKeys().next()) {
							documents.get(i).getStatements().get(j).setId(s5.getGeneratedKeys().getInt(1));
						}
						for (k = 0; k < documents.get(i).getStatements().get(j).getValues().size(); k++) {
							if (documents.get(i).getStatements().get(j).getValues().get(k).getDataType().equals("long text")) {
								s4.setInt(1, documents.get(i).getStatements().get(j).getId());
								s4.setInt(2, documents.get(i).getStatements().get(j).getStatementTypeId());
								s4.setString(3, documents.get(i).getStatements().get(j).getValues().get(k).getKey());
								s4.setInt(4, documents.get(i).getStatements().get(j).getStatementTypeId());
								s4.setString(5, (String) documents.get(i).getStatements().get(j).getValues().get(k).getValue());
								s4.executeUpdate();
							} else if (documents.get(i).getStatements().get(j).getValues().get(k).getDataType().equals("short text")) {
								s3.setInt(1, documents.get(i).getStatements().get(j).getId());
								s3.setInt(2, documents.get(i).getStatements().get(j).getStatementTypeId());
								s3.setString(3, documents.get(i).getStatements().get(j).getValues().get(k).getKey());
								s3.setInt(4, documents.get(i).getStatements().get(j).getStatementTypeId());
								s3.setString(5, (String) documents.get(i).getStatements().get(j).getValues().get(k).getValue());
								s3.executeUpdate();
							} else if (documents.get(i).getStatements().get(j).getValues().get(k).getDataType().equals("integer")) {
								s2.setInt(1, documents.get(i).getStatements().get(j).getId());
								s2.setInt(2, documents.get(i).getStatements().get(j).getStatementTypeId());
								s2.setString(3, documents.get(i).getStatements().get(j).getValues().get(k).getKey());
								s2.setInt(4, documents.get(i).getStatements().get(j).getStatementTypeId());
								s2.setInt(5, (int) documents.get(i).getStatements().get(j).getValues().get(k).getValue());
								s2.executeUpdate();
							} else if (documents.get(i).getStatements().get(j).getValues().get(k).getDataType().equals("boolean")) {
								s1.setInt(1, documents.get(i).getStatements().get(j).getId());
								s1.setInt(2, documents.get(i).getStatements().get(j).getStatementTypeId());
								s1.setString(3, documents.get(i).getStatements().get(j).getValues().get(k).getKey());
								s1.setInt(4, documents.get(i).getStatements().get(j).getStatementTypeId());
								s1.setInt(5, (int) documents.get(i).getStatements().get(j).getValues().get(k).getValue());
								s1.executeUpdate();
							}
						}
					}
				}
				conn.commit();
			} catch (SQLException e) {
				System.err.println("Could not re-insert documents (possibly including contained statements) from the database.");
				e.printStackTrace();
			}
		}
		return documents;
	}
}