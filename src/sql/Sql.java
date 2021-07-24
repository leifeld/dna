package sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.sqlite.SQLiteDataSource;

import com.mysql.cj.jdbc.MysqlDataSource;

import dna.Dna;
import dna.Document;
import dna.Statement;
import guiCoder.Coder;
import guiCoder.ConnectionProfile;

public class Sql {
	private ConnectionProfile cp;
	private DataSource ds;
	
	public ConnectionProfile getConnectionProfile() {
		return cp;
	}

	public void setConnectionProfile(ConnectionProfile cp) {
		this.cp = cp;
	}
	
	public DataSource getDataSource() {
		return ds;
	}

	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}

	public Sql(ConnectionProfile cp) {
		this.cp = cp;
		
		// prepare data source or connection
		if (cp.getType().equals("sqlite")) { // no user name and password needed for file-based database
			ds = new SQLiteDataSource();
	        ((SQLiteDataSource) ds).setUrl("jdbc:sqlite:" + cp.getUrl());
		} else if (cp.getType().equals("mysql")) {
			ds = new MysqlDataSource();
			((MysqlDataSource) ds).setUrl("jdbc:mysql://" + cp.getUrl());
			((MysqlDataSource) ds).setUser(cp.getUser());
			((MysqlDataSource) ds).setPassword(cp.getPassword());
		} else if (cp.getType().equals("postgresql")) {
			ds = new BasicDataSource(); // use Apache DBCP for connection pooling with PostgreSQL
			((BasicDataSource) ds).setDriverClassName("org.postgresql.Driver");
			((BasicDataSource) ds).setUrl("jdbc:postgresql://" + cp.getUrl());
			((BasicDataSource) ds).setUsername(cp.getUser());
			((BasicDataSource) ds).setPassword(cp.getPassword());
		} else {
			System.err.println("Database format not recognized: " + cp.getType() + ".");
		}
	}
	
	public String getDocumentText(int documentId) {
		String text = null;
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("SELECT Text FROM DOCUMENTS WHERE ID = ?;")) {
			s.setInt(1, documentId);
			ResultSet result = s.executeQuery();
			while (result.next()) {
			    text = result.getString("Text");
			}
		} catch (SQLException e) {
			System.err.println("Could not establish connection to database to retrieve the document text.");
			e.printStackTrace();
		}
		return text;
	}
	
	public void deleteDocuments(int[] documentIds) {
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("DELETE FROM DOCUMENTS WHERE ID = ?"); // will cascade to statements
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			for (int i = 0; i < documentIds.length; i++) {
				s.setInt(1, documentIds[i]);
				s.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			System.err.println("Could not establish connection to database to remove document(s).");
			e.printStackTrace();
		}
	}
	
	public void addDocuments(ArrayList<Document> documents) {
		try (Connection conn = ds.getConnection();
				PreparedStatement stmt = conn.prepareStatement("INSERT INTO DOCUMENTS (Title, Text, Coder, Author, Source, Section, Notes, Type, Date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			for (int i = 0; i < documents.size(); i++) {
				stmt.setString(1, documents.get(i).getTitle());
				stmt.setString(2, documents.get(i).getText());
				stmt.setInt(3, documents.get(i).getCoder());
				stmt.setString(4, documents.get(i).getAuthor());
				stmt.setString(5, documents.get(i).getSource());
				stmt.setString(6, documents.get(i).getSection());
				stmt.setString(7, documents.get(i).getNotes());
				stmt.setString(8, documents.get(i).getDocumentType());
				stmt.setLong(9, documents.get(i).getDateTime().toEpochSecond(ZoneOffset.UTC)); // convert date-time to seconds since 01/01/1970 at 00:00:00 in UTC time zone
				stmt.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			System.err.println("Could not establish connection to database to add or remove documents.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieve a coder based on its ID.
	 * 
	 * @return The coder to be retrieved.
	 */
	public guiCoder.Coder getCoder(int coderId) {
		guiCoder.Coder c = null;
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("SELECT Name, Red, Green, Blue FROM CODERS WHERE ID = ?;")) {
			s.setInt(1, coderId);
			ResultSet result = s.executeQuery();
			while (result.next()) {
			    c = new guiCoder.Coder(coderId, result.getString("Name"), result.getInt("Red"), result.getInt("Green"), result.getInt("Blue"));
			}
		} catch (SQLException e) {
			System.err.println("Could not establish connection to database to retrieve the coder.");
			e.printStackTrace();
		}
		return c;
	}
	
	/**
	 * Check if a user-provided clear-text password for the current coder
	 * matches the hash of the password stored for the coder in the database.
	 * 
	 * @param clearPassword Clear-text password provided by the coder.
	 * @return              boolean, true if the password matches, false if not.
	 */
	public boolean authenticate(String clearPassword) {
		String encryptedHash = null;
		try (Connection conn = this.ds.getConnection();
				PreparedStatement s = conn.prepareStatement("SELECT Password FROM CODERS WHERE ID = ?;")) {
			s.setInt(1, this.cp.getCoderId());
			ResultSet result = s.executeQuery();
			while (result.next()) {
			    encryptedHash = result.getString("Password");
			}
		} catch (SQLException e) {
			System.err.println("Could not retrieve password hash from database.");
			e.printStackTrace();
		}
		
		// check if the provided clear-text password corresponds to the hashed password in the database
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		return passwordEncryptor.checkPassword(clearPassword, encryptedHash);
	}

	/**
	 * Retrieve a list of coders in the database.
	 * 
	 * @return An array list of coders.
	 */
	public ArrayList<Coder> getCoders() {
		ArrayList<Coder> coders = new ArrayList<Coder>();
		try (Connection conn = ds.getConnection();
				PreparedStatement tableStatement = conn.prepareStatement("SELECT * FROM Coders;")) {
        	ResultSet rs = tableStatement.executeQuery();
        	while (rs.next()) {
            	coders.add(new Coder(rs.getInt("ID"), rs.getString("Name"), rs.getInt("Red"), rs.getInt("Green"), rs.getInt("Blue")));
            }
		} catch (SQLException e1) {
			System.err.println("Could not retrieve coders from database.");
			e1.printStackTrace();
		}
		return coders;
	}
	
	// get documents corresponding to specific document IDs, but without their statements etc.
	public ArrayList<Document> getDocuments(int[] documentIds) {
		ArrayList<Document> documents = new ArrayList<Document>();
		String sql = "SELECT * FROM DOCUMENTS WHERE ID IN (";
		for (int i = 0; i < documentIds.length; i++) {
			sql = sql + documentIds[i];
			if (i < documentIds.length - 1) {
				sql = sql + ", ";
			}
		}
		sql = sql + ");";
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement(sql)) {
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				Document d = new Document(
						rs.getInt("ID"),
						rs.getInt("Coder"),
						rs.getString("Title"),
						rs.getString("Text"),
						rs.getString("Author"),
						rs.getString("Source"),
						rs.getString("Section"),
						rs.getString("Type"),
						rs.getString("Notes"),
						LocalDateTime.ofEpochSecond(rs.getLong("Date"), 0, ZoneOffset.UTC),
						new ArrayList<Statement>());
				documents.add(d);
			}
		} catch (SQLException e) {
			System.err.println("Could not retrieve documents from database.");
			e.printStackTrace();
		}
		return documents;
	}
	
	public boolean documentsContainStatements(int[] documentIds) {
		boolean contains = true;
		String s = "SELECT COUNT(*) FROM STATEMENTS WHERE DocumentId IN (";
		for (int i = 0; i < documentIds.length; i++) {
			s = s + documentIds[i];
			if (i < documentIds.length - 1) {
				s = s + ", ";
			}
		}
		s = s + ");";
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement ps = conn.prepareStatement(s)) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.getInt(1) == 0) {
					contains = false;
				}
			}
		} catch (SQLException e) {
			System.err.println("Could not count statements in document selection.");
			e.printStackTrace();
		}
		return contains;
	}
	
	public void updateDocuments(int[] documentIds, String title, String text, String author, String source, String section, String type, String notes, LocalDateTime dateTime) {
		String sel = "SELECT Title, Text, Author, Source, Section, Type, Notes, Date FROM DOCUMENTS WHERE ID = ?;";
		String upd = "UPDATE DOCUMENTS SET Title = ?, Text = ?, Author = ?, Source = ?, Section = ?, Type = ?, Notes = ?, Date = ? WHERE ID = ?;";
		String titleTemp1 = "", titleTemp2, textTemp1 = "", textTemp2, authorTemp1 = "", authorTemp2, sourceTemp1 = "", sourceTemp2, sectionTemp1 = "", sectionTemp2, typeTemp1 = "", typeTemp2, notesTemp1 = "", notesTemp2, day = "", month = "", year = "", hour = "", minute = "";
		long dateTimeTemp = 0;
		LocalDateTime ldt = null;
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");
		DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM");
		DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
		DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH");
		DateTimeFormatter minuteFormatter = DateTimeFormatter.ofPattern("mm");
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement u = conn.prepareStatement(upd);
				PreparedStatement s = conn.prepareStatement(sel);
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			for (int i = 0; i < documentIds.length; i++) {
				s.setInt(1, documentIds[i]);
				ResultSet r = s.executeQuery();
				while (r.next()) {
					titleTemp1 = r.getString("Title");
					textTemp1 = r.getString("Text");
					authorTemp1 = r.getString("Author");
					sectionTemp1 = r.getString("Section");
					sourceTemp1 = r.getString("Source");
					typeTemp1 = r.getString("Type");
					notesTemp1 = r.getString("Notes");
					dateTimeTemp = r.getLong("Date");
					ldt = LocalDateTime.ofEpochSecond(dateTimeTemp, 0, ZoneOffset.UTC);
					day = ldt.format(dayFormatter);
					month = ldt.format(monthFormatter);
					year = ldt.format(yearFormatter);
					hour = ldt.format(hourFormatter);
					minute = ldt.format(minuteFormatter);
				}
				titleTemp2 = title.replaceAll("%title", titleTemp1);
				titleTemp2 = titleTemp2.replaceAll("%text", textTemp1);
				titleTemp2 = titleTemp2.replaceAll("%author", authorTemp1);
				titleTemp2 = titleTemp2.replaceAll("%source", sourceTemp1);
				titleTemp2 = titleTemp2.replaceAll("%section", sectionTemp1);
				titleTemp2 = titleTemp2.replaceAll("%type", typeTemp1);
				titleTemp2 = titleTemp2.replaceAll("%notes", notesTemp1);
				titleTemp2 = titleTemp2.replaceAll("%day", day);
				titleTemp2 = titleTemp2.replaceAll("%month", month);
				titleTemp2 = titleTemp2.replaceAll("%year", year);
				titleTemp2 = titleTemp2.replaceAll("%hour", hour);
				titleTemp2 = titleTemp2.replaceAll("%minute", minute);
				u.setString(1, titleTemp2);
				textTemp2 = text.replaceAll("%title", titleTemp1);
				textTemp2 = textTemp2.replaceAll("%text", textTemp1);
				textTemp2 = textTemp2.replaceAll("%author", authorTemp1);
				textTemp2 = textTemp2.replaceAll("%source", sourceTemp1);
				textTemp2 = textTemp2.replaceAll("%section", sectionTemp1);
				textTemp2 = textTemp2.replaceAll("%type", typeTemp1);
				textTemp2 = textTemp2.replaceAll("%notes", notesTemp1);
				textTemp2 = textTemp2.replaceAll("%day", day);
				textTemp2 = textTemp2.replaceAll("%month", month);
				textTemp2 = textTemp2.replaceAll("%year", year);
				textTemp2 = textTemp2.replaceAll("%hour", hour);
				textTemp2 = textTemp2.replaceAll("%minute", minute);
				u.setString(2, textTemp2);
				authorTemp2 = author.replaceAll("%title", titleTemp1);
				authorTemp2 = authorTemp2.replaceAll("%text", textTemp1);
				authorTemp2 = authorTemp2.replaceAll("%author", authorTemp1);
				authorTemp2 = authorTemp2.replaceAll("%source", sourceTemp1);
				authorTemp2 = authorTemp2.replaceAll("%section", sectionTemp1);
				authorTemp2 = authorTemp2.replaceAll("%type", typeTemp1);
				authorTemp2 = authorTemp2.replaceAll("%notes", notesTemp1);
				authorTemp2 = authorTemp2.replaceAll("%day", day);
				authorTemp2 = authorTemp2.replaceAll("%month", month);
				authorTemp2 = authorTemp2.replaceAll("%year", year);
				authorTemp2 = authorTemp2.replaceAll("%hour", hour);
				authorTemp2 = authorTemp2.replaceAll("%minute", minute);
				u.setString(3, authorTemp2);
				sourceTemp2 = source.replaceAll("%title", titleTemp1);
				sourceTemp2 = sourceTemp2.replaceAll("%text", textTemp1);
				sourceTemp2 = sourceTemp2.replaceAll("%author", authorTemp1);
				sourceTemp2 = sourceTemp2.replaceAll("%source", sourceTemp1);
				sourceTemp2 = sourceTemp2.replaceAll("%section", sectionTemp1);
				sourceTemp2 = sourceTemp2.replaceAll("%type", typeTemp1);
				sourceTemp2 = sourceTemp2.replaceAll("%notes", notesTemp1);
				sourceTemp2 = sourceTemp2.replaceAll("%day", day);
				sourceTemp2 = sourceTemp2.replaceAll("%month", month);
				sourceTemp2 = sourceTemp2.replaceAll("%year", year);
				sourceTemp2 = sourceTemp2.replaceAll("%hour", hour);
				sourceTemp2 = sourceTemp2.replaceAll("%minute", minute);
				u.setString(4, sourceTemp2);
				sectionTemp2 = section.replaceAll("%title", titleTemp1);
				sectionTemp2 = sectionTemp2.replaceAll("%text", textTemp1);
				sectionTemp2 = sectionTemp2.replaceAll("%author", authorTemp1);
				sectionTemp2 = sectionTemp2.replaceAll("%source", sourceTemp1);
				sectionTemp2 = sectionTemp2.replaceAll("%section", sectionTemp1);
				sectionTemp2 = sectionTemp2.replaceAll("%type", typeTemp1);
				sectionTemp2 = sectionTemp2.replaceAll("%notes", notesTemp1);
				sectionTemp2 = sectionTemp2.replaceAll("%day", day);
				sectionTemp2 = sectionTemp2.replaceAll("%month", month);
				sectionTemp2 = sectionTemp2.replaceAll("%year", year);
				sectionTemp2 = sectionTemp2.replaceAll("%hour", hour);
				sectionTemp2 = sectionTemp2.replaceAll("%minute", minute);
				u.setString(5, sectionTemp2);
				typeTemp2 = type.replaceAll("%title", titleTemp1);
				typeTemp2 = typeTemp2.replaceAll("%text", textTemp1);
				typeTemp2 = typeTemp2.replaceAll("%author", authorTemp1);
				typeTemp2 = typeTemp2.replaceAll("%source", sourceTemp1);
				typeTemp2 = typeTemp2.replaceAll("%section", sectionTemp1);
				typeTemp2 = typeTemp2.replaceAll("%type", typeTemp1);
				typeTemp2 = typeTemp2.replaceAll("%notes", notesTemp1);
				typeTemp2 = typeTemp2.replaceAll("%day", day);
				typeTemp2 = typeTemp2.replaceAll("%month", month);
				typeTemp2 = typeTemp2.replaceAll("%year", year);
				typeTemp2 = typeTemp2.replaceAll("%hour", hour);
				typeTemp2 = typeTemp2.replaceAll("%minute", minute);
				u.setString(6, typeTemp2);
				notesTemp2 = notes.replaceAll("%title", titleTemp1);
				notesTemp2 = notesTemp2.replaceAll("%text", textTemp1);
				notesTemp2 = notesTemp2.replaceAll("%author", authorTemp1);
				notesTemp2 = notesTemp2.replaceAll("%source", sourceTemp1);
				notesTemp2 = notesTemp2.replaceAll("%section", sectionTemp1);
				notesTemp2 = notesTemp2.replaceAll("%type", typeTemp1);
				notesTemp2 = notesTemp2.replaceAll("%notes", notesTemp1);
				notesTemp2 = notesTemp2.replaceAll("%day", day);
				notesTemp2 = notesTemp2.replaceAll("%month", month);
				notesTemp2 = notesTemp2.replaceAll("%year", year);
				notesTemp2 = notesTemp2.replaceAll("%hour", hour);
				notesTemp2 = notesTemp2.replaceAll("%minute", minute);
				u.setString(7, notesTemp2);
				if (dateTime != null) {
					u.setLong(8, dateTime.toEpochSecond(ZoneOffset.UTC));
				} else {
					u.setLong(8, dateTimeTemp);
				}
				u.setInt(9, documentIds[i]);
				u.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			System.err.println("Could not update documents in the database.");
			e.printStackTrace();
		}
	}
	
	public boolean createTables(String encryptedAdminPassword) {
		boolean success = true;
		ArrayList<String> s = new ArrayList<String>();
		if (cp.getType().equals("sqlite")) {
			s.add("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property TEXT PRIMARY KEY, "
					+ "Value TEXT);");
			s.add("CREATE TABLE IF NOT EXISTS CODERS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Name TEXT, "
					+ "Red INTEGER CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER CHECK (Blue BETWEEN 0 AND 255), "
					+ "Password TEXT);");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Title TEXT, "
					+ "Text TEXT, "
					+ "Coder INTEGER, "
					+ "Author TEXT, "
					+ "Source TEXT, "
					+ "Section TEXT, "
					+ "Notes TEXT, "
					+ "Type TEXT, "
					+ "Date INTEGER, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Label TEXT, "
					+ "Red INTEGER CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Variable TEXT, "
					+ "DataType TEXT, "
					+ "StatementTypeId INTEGER, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (Variable, StatementTypeId));");
			s.add("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label TEXT PRIMARY KEY, "
					+ "Red INTEGER CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS CODERPERMISSIONS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Coder INTEGER, "
					+ "Type TEXT, "
					+ "Permission INTEGER CHECK(Permission BETWEEN 0 AND 1), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "UNIQUE (Coder, Type));");
			s.add("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Coder INTEGER CHECK(Coder > 0 AND Coder != OtherCoder), "
					+ "OtherCoder INTEGER CHECK(OtherCoder > 0 AND OtherCoder != Coder), "
					+ "viewStatements INTEGER CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editStatements INTEGER CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "viewDocuments INTEGER CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editDocuments INTEGER CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(OtherCoder) REFERENCES CODERS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "StatementTypeId INTEGER, "
					+ "DocumentId INTEGER, "
					+ "Start INTEGER CHECK(Start >= 0 AND Start < Stop), "
					+ "Stop INTEGER CHECK(Stop >= 0 AND Stop > Start), "
					+ "Coder INTEGER, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(DocumentId) REFERENCES DOCUMENTS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTLINKS("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "SourceId INTEGER NOT NULL CHECK(SourceId > 0 AND SourceId != TargetId), "
					+ "TargetId INTEGER NOT NULL CHECK(TargetId > 0 AND SourceId != TargetId), "
					+ "FOREIGN KEY(SourceId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE,"
					+ "FOREIGN KEY(TargetId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS DATABOOLEAN("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "StatementTypeId INTEGER, "
					+ "Value INTEGER, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "StatementTypeId INTEGER, "
					+ "Value INTEGER, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "StatementTypeId INTEGER, "
					+ "Value TEXT, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "StatementTypeId INTEGER, "
					+ "Value TEXT, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTES("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "Value TEXT NOT NULL, "
					+ "Red INTEGER CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER CHECK (Blue BETWEEN 0 AND 255), "
					+ "Type TEXT, "
					+ "Alias TEXT, "
					+ "Notes TEXT, "
					+ "ChildOf INTEGER NOT NULL CHECK(ChildOf > 0 AND ChildOf != ID), "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE);");
		} else if (cp.getType().equals("mysql")) {
			s.add("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property VARCHAR(500), "
					+ "Value VARCHAR(500),"
					+ "PRIMARY KEY (Property));");
			s.add("CREATE TABLE IF NOT EXISTS CODERS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Name VARCHAR(5000), "
					+ "Red SMALLINT UNSIGNED CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED CHECK (Blue BETWEEN 0 AND 255), "
					+ "Password VARCHAR(300), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Title VARCHAR(5000), "
					+ "Text MEDIUMTEXT, "
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "Author VARCHAR(5000), "
					+ "Source VARCHAR(5000), "
					+ "Section VARCHAR(5000), "
					+ "Notes TEXT, "
					+ "Type VARCHAR(5000), "
					+ "Date BIGINT, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Label VARCHAR(5000), "
					+ "Red SMALLINT UNSIGNED CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED CHECK (Blue BETWEEN 0 AND 255), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Variable VARCHAR(500), "
					+ "DataType VARCHAR(200), "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (Variable, StatementTypeId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label VARCHAR(2000), "
					+ "Red SMALLINT UNSIGNED CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED CHECK (Blue BETWEEN 0 AND 255), "
					+ "PRIMARY KEY(Label));");
			s.add("CREATE TABLE IF NOT EXISTS CODERPERMISSIONS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "Type VARCHAR(50), "
					+ "Permission SMALLINT UNSIGNED CHECK(Permission BETWEEN 0 AND 1), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY CoderPerm (Coder, Type), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Coder SMALLINT UNSIGNED NOT NULL CHECK(Coder > 0 AND Coder != OtherCoder), "
					+ "OtherCoder SMALLINT UNSIGNED NOT NULL CHECK(OtherCoder > 0 AND OtherCoder != Coder), "
					+ "viewStatements SMALLINT UNSIGNED CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editStatements SMALLINT UNSIGNED CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "viewDocuments SMALLINT UNSIGNED CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editDocuments SMALLINT UNSIGNED CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(OtherCoder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "DocumentId MEDIUMINT UNSIGNED NOT NULL, "
					+ "Start BIGINT UNSIGNED CHECK(Start >= 0 AND Start < Stop), "
					+ "Stop BIGINT UNSIGNED CHECK(Stop >= 0 AND Stop > Start), "
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(DocumentId) REFERENCES DOCUMENTS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTLINKS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "SourceId MEDIUMINT UNSIGNED NOT NULL CHECK(SourceId > 0 AND SourceId != TargetId), "
					+ "TargetId MEDIUMINT UNSIGNED NOT NULL CHECK(TargetId > 0 AND SourceId != TargetId), "
					+ "FOREIGN KEY(SourceId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(TargetId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATABOOLEAN("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "Value SMALLINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (StatementId, VariableId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "Value MEDIUMINT NOT NULL, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (StatementId, VariableId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "Value VARCHAR(5000), "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (StatementId, VariableId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "Value TEXT, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (StatementId, VariableId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTES("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "Value TEXT, "
					+ "Red SMALLINT UNSIGNED CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED CHECK (Blue BETWEEN 0 AND 255), "
					+ "Type TEXT, "
					+ "Alias TEXT, "
					+ "Notes TEXT, "
					+ "ChildOf MEDIUMINT UNSIGNED NOT NULL CHECK(ChildOf > 0 AND ChildOf != ID), "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(ChildOf) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
		} else if (cp.getType().equals("postgresql")) {
			s.add("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property VARCHAR(500) PRIMARY KEY, "
					+ "Value VARCHAR(500));");
			s.add("CREATE TABLE IF NOT EXISTS CODERS("
					+ "ID SERIAL NOT NULL PRIMARY KEY CHECK (ID > 0), "
					+ "Name VARCHAR(5000), "
					+ "Red SERIAL CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SERIAL CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SERIAL CHECK (Blue BETWEEN 0 AND 255), "
					+ "Password VARCHAR(300));");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Title VARCHAR(5000), "
					+ "Text TEXT, "
					+ "Coder INT NOT NULL REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "Author VARCHAR(5000), "
					+ "Source VARCHAR(5000), "
					+ "Section VARCHAR(5000), "
					+ "Notes TEXT, "
					+ "Type VARCHAR(5000), "
					+ "Date BIGINT);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Label VARCHAR(5000), "
					+ "Red SERIAL CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SERIAL CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SERIAL CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Variable VARCHAR(500), "
					+ "DataType VARCHAR(200), "
					+ "StatementTypeId INT NOT NULL CHECK(StatementTypeId > 0) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (Variable, StatementTypeId));");
			s.add("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label VARCHAR(2000) PRIMARY KEY, "
					+ "Red SERIAL CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SERIAL CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SERIAL CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS CODERPERMISSIONS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Coder INT NOT NULL CHECK(Coder > 0) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "Type VARCHAR(50), "
					+ "Permission INT CHECK(Permission BETWEEN 0 AND 1), "
					+ "UNIQUE (Coder, Type));");
			s.add("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Coder INT NOT NULL CHECK(Coder > 0 AND Coder != OtherCoder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "OtherCoder INT NOT NULL CHECK(OtherCoder > 0 AND OtherCoder != Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "viewStatements INT CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editStatements INT CHECK(editStatements BETWEEN 0 AND 1), "
					+ "viewDocuments INT CHECK(viewDocuments BETWEEN 0 AND 1), "
					+ "editDocuments INT CHECK(editDocuments BETWEEN 0 AND 1));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementTypeId INT NOT NULL CHECK(StatementTypeId > 0) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "DocumentId INT NOT NULL CHECK(DocumentId > 0) REFERENCES DOCUMENTS(ID) ON DELETE CASCADE, "
					+ "Start BIGINT CHECK(Start >= 0 AND Start < Stop), "
					+ "Stop BIGINT CHECK(Stop >= 0 AND Stop > Start), "
					+ "Coder INT NOT NULL CHECK(Coder > 0) REFERENCES CODERS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTLINKS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "SourceId INT NOT NULL CHECK(SourceId > 0 AND SourceId != TargetId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "TargetId INT NOT NULL CHECK(TargetId > 0 AND TargetId != SourceId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS DATABOOLEAN("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "StatementTypeId INT NOT NULL CHECK(StatementTypeId > 0) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "Value INT NOT NULL CHECK(Value BETWEEN 0 AND 1), "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "StatementTypeId INT NOT NULL CHECK(StatementTypeId > 0) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "Value INT NOT NULL, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "StatementTypeId INT NOT NULL CHECK(StatementTypeId > 0) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "Value VARCHAR(5000), "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "StatementTypeId INT NOT NULL CHECK(StatementTypeId > 0) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "Value TEXT, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "Value TEXT, "
					+ "Red SERIAL CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SERIAL CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SERIAL CHECK (Blue BETWEEN 0 AND 255), "
					+ "Type TEXT, "
					+ "Alias TEXT, "
					+ "Notes TEXT, "
					+ "ChildOf INT NOT NULL CHECK(ChildOf > 0 AND ChildOf != ID) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE);");
		}
		// fill default data into the tables (Admin coder, settings, statement types)
		s.add("INSERT INTO CODERS (ID, Name, Red, Green, Blue, Password) VALUES (1, 'Admin', 239, 208, 51, '" + encryptedAdminPassword + "');");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'deleteDocuments', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'addDocuments', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'importDocuments', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'editDocuments', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'addStatements', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'editRegex', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'editAttributes', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'viewOthersDocuments', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'editOthersDocuments', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'viewOthersStatements', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'editOthersStatements', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'editStatementTypes', 1);");
		s.add("INSERT INTO CODERPERMISSIONS (Coder, Type, Permission) VALUES(1, 'editCoders', 1);");
		s.add("INSERT INTO SETTINGS (Property, Value) VALUES ('version', '" + Dna.dna.version + "');");
		s.add("INSERT INTO SETTINGS (Property, Value) VALUES ('date', '" + Dna.dna.date + "');");
		s.add("INSERT INTO SETTINGS (Property, Value) VALUES ('popupWidth', '300');");
		s.add("INSERT INTO SETTINGS (Property, Value) VALUES ('statementColor', 'statementType');");
		s.add("INSERT INTO STATEMENTTYPES (ID, Label, Red, Green, Blue) VALUES (1, 'DNA Statement', 239, 208, 51);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType, StatementTypeId) VALUES(1, 'person', 'short text', 1);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType, StatementTypeId) VALUES(2, 'organization', 'short text', 1);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType, StatementTypeId) VALUES(3, 'concept', 'short text', 1);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType, StatementTypeId) VALUES(4, 'agreement', 'boolean', 1);");
		s.add("INSERT INTO STATEMENTTYPES (ID, Label, Red, Green, Blue) VALUES (2, 'Annotation', 211, 211, 211);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType, StatementTypeId) VALUES(5, 'note', 'long text', 2);");
		try (Connection conn = ds.getConnection();
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			for (int i = 0; i < s.size(); i++) {
				PreparedStatement p = conn.prepareStatement(s.get(i));
				p.executeUpdate();
				p.close();
			}
			conn.commit();
		} catch (SQLException e) {
			success = false;
			System.err.println("Could not establish connection to database to create tables.");
			e.printStackTrace();
		}
		return success;
	}
	
	public interface SQLCloseable extends AutoCloseable {
	    @Override public void close() throws SQLException;
	}
}