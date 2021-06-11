package stack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import dna.Dna;
import sql.Sql.SQLCloseable;

/**
 * DnaEvent for adding a document to the database.
 *
 */
public class AddDocumentsEvent extends DnaEvent {
	ArrayList<StackDocument> documents;

	public AddDocumentsEvent(ArrayList<StackDocument> documents) {
		super("Add Document Event");
		this.documents = documents;
	}

	public void setDocuments(ArrayList<StackDocument> documents) {
		this.documents = documents;
	}

	public ArrayList<StackDocument> getDocuments() {
		return documents;
	}

	@Override
	void doSql() {
		this.documents = undoAddDocumentsEvent(false, this.documents);
	}
	
	@Override
	void undoSql() {
		this.documents = undoAddDocumentsEvent(true, this.documents);
	}

	public ArrayList<StackDocument> undoAddDocumentsEvent(boolean undo, ArrayList<StackDocument> documents) {
		if (Dna.sql.getConnectionProfile().getType().equals("sqlite")) {
			try {
				Dna.sql.sqliteConnection.setAutoCommit(false);
				documents = undoAddDocumentsEventHelper(undo, Dna.sql.sqliteConnection, documents);
			} catch (SQLException e) {
				System.err.println("Could not establish connection to database to add or remove documents.");
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
				documents = undoAddDocumentsEventHelper(undo, conn, documents);
			} catch (SQLException e) {
				System.err.println("Could not establish connection to database to add or remove documents.");
				e.printStackTrace();
			}
		} else {
			System.err.println("Database type not recognized.");
		}
		return documents;
	}

	private ArrayList<StackDocument> undoAddDocumentsEventHelper(boolean undo, Connection conn, ArrayList<StackDocument> documents) {
		if (undo == false) { // do or re-do action
			String s = "INSERT INTO DOCUMENTS (Title, Text, Coder, Author, Source, Section, Notes, Type, Date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
			try (PreparedStatement stmt = conn.prepareStatement(s)) {
				for (int i = 0; i < documents.size(); i++) {
					stmt.setString(1, documents.get(i).getTitle());
					stmt.setString(2, documents.get(i).getText());
					stmt.setInt(3, documents.get(i).getCoder());
					stmt.setString(4, documents.get(i).getAuthor());
					stmt.setString(5, documents.get(i).getSource());
					stmt.setString(6, documents.get(i).getSection());
					stmt.setString(7, documents.get(i).getNotes());
					stmt.setString(8, documents.get(i).getDocumentType());
					stmt.setLong(9, documents.get(i).getDate().getTime());
					stmt.executeUpdate();
					// TODO: nothing gets added with PostgreSQL (and possibly MySQL?)
					if (stmt.getGeneratedKeys().next()) {
						// TODO: the keys are only generated with SQLite, but apparently not PostgreSQL...
						documents.get(i).setId(stmt.getGeneratedKeys().getInt(1));
					}
				}
			} catch (SQLException e) {
				System.err.println("Could not add document to the database.");
				e.printStackTrace();
			}
		} else { // undo action
			try (PreparedStatement db = conn.prepareStatement("DELETE FROM DATABOOLEAN WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = ?)");
					PreparedStatement di = conn.prepareStatement("DELETE FROM DATAINTEGER WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = ?)");
					PreparedStatement ds = conn.prepareStatement("DELETE FROM DATASHORTTEXT WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = ?)");
					PreparedStatement dl = conn.prepareStatement("DELETE FROM DATALONGTEXT WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = ?)");
					PreparedStatement s = conn.prepareStatement("DELETE FROM STATEMENTS WHERE DocumentId = ?");
					PreparedStatement d = conn.prepareStatement("DELETE FROM DOCUMENTS WHERE ID = ?")) {
				for (int i = 0; i < documents.size(); i++) {
					if (documents.get(i).getId() > -1) {
						db.setInt(1, documents.get(i).getId());
						di.setInt(1, documents.get(i).getId());
						ds.setInt(1, documents.get(i).getId());
						dl.setInt(1, documents.get(i).getId());
						s.setInt(1, documents.get(i).getId());
						d.setInt(1, documents.get(i).getId());
						db.executeUpdate();
						di.executeUpdate();
						ds.executeUpdate();
						dl.executeUpdate();
						s.executeUpdate();
						d.executeUpdate();
						documents.get(i).setId(-1);
					}
				}
				conn.commit();
			} catch (SQLException e) {
				System.err.println("Could remove document(s) from the database.");
				e.printStackTrace();
			}
		}
		return documents;
	}
}