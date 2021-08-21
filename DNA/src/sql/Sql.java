package sql;

import java.awt.Color;
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

import dna.Attribute;
import dna.Dna;
import dna.Document;
import dna.Statement;
import dna.StatementType;
import dna.Value;
import guiCoder.Coder;
import guiCoder.ConnectionProfile;
import logger.LogEvent;
import logger.Logger;

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
		if (cp.getType().equals("sqlite")) { // no user name and password needed for file-based database
			ds = new SQLiteDataSource();
	        ((SQLiteDataSource) ds).setUrl("jdbc:sqlite:" + cp.getUrl());
	        ((SQLiteDataSource) ds).setEnforceForeignKeys(true); // if this is not set, ON DELETE CASCADE won't work
	        LogEvent l = new LogEvent(Logger.MESSAGE,
	        		"[SQL] An SQLite DNA database has been opened as a data source.",
	        		"An SQLite DNA database has been opened as a data source.");
	        Dna.logger.log(l);
		} else if (cp.getType().equals("mysql")) {
			ds = new MysqlDataSource();
			((MysqlDataSource) ds).setUrl("jdbc:mysql://" + cp.getUrl());
			((MysqlDataSource) ds).setUser(cp.getUser());
			((MysqlDataSource) ds).setPassword(cp.getPassword());
	        LogEvent l = new LogEvent(Logger.MESSAGE,
	        		"[SQL] A MySQL DNA database has been opened as a data source.",
	        		"A MySQL DNA database has been opened as a data source.");
	        Dna.logger.log(l);
		} else if (cp.getType().equals("postgresql")) {
			ds = new BasicDataSource(); // use Apache DBCP for connection pooling with PostgreSQL
			((BasicDataSource) ds).setDriverClassName("org.postgresql.Driver");
			((BasicDataSource) ds).setUrl("jdbc:postgresql://" + cp.getUrl());
			((BasicDataSource) ds).setUsername(cp.getUser());
			((BasicDataSource) ds).setPassword(cp.getPassword());
	        LogEvent l = new LogEvent(Logger.MESSAGE,
	        		"[SQL] A PostgreSQL DNA database has been opened as a data source.",
	        		"A PostgreSQL DNA database has been opened as a data source.");
	        Dna.logger.log(l);
		} else {
	        LogEvent l = new LogEvent(Logger.ERROR,
	        		"[SQL] Failed to regognize database format.",
	        		"Attempted to open a database of type \"" + cp.getType() + "\", but the type does not seem to be supported.");
	        Dna.logger.log(l);
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
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to retrieve text for Document " + documentId + ".",
					"Attempted to retrieve the text contents for the document with ID " + documentId + " from the DOCUMENTS table of the database, but something went wrong, and nothing was retrieved. Check your connection.",
					e);
			Dna.logger.log(l);
		}
		return text;
	}
	
	public ArrayList<Statement> getStatementsByDocument(int documentId) {
		ArrayList<Statement> statements = new ArrayList<Statement>();
		ArrayList<Value> values;
		int statementId, statementTypeId, variableId;
		String variable, dataType;
		Color aColor, sColor, cColor;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT STATEMENTS.ID AS StatementId, StatementTypeId, STATEMENTTYPES.Label AS StatementTypeLabel, STATEMENTTYPES.Red AS StatementTypeRed, STATEMENTTYPES.Green AS StatementTypeGreen, STATEMENTTYPES.Blue AS StatementTypeBlue, Start, Stop, Coder AS CoderId, CODERS.Red AS CoderRed, CODERS.Green AS CoderGreen, CODERS.Blue AS CoderBlue FROM STATEMENTS INNER JOIN CODERS ON STATEMENTS.Coder = CODERS.ID INNER JOIN STATEMENTTYPES ON STATEMENTS.StatementTypeId = STATEMENTTYPES.ID WHERE DocumentId = ?;");
				PreparedStatement s2 = conn.prepareStatement("SELECT ID, Variable, DataType FROM VARIABLES WHERE StatementTypeId = ?;");
				PreparedStatement s3 = conn.prepareStatement("SELECT A.ID AS AttributeId, StatementId, A.VariableId, DST.ID AS DataId, A.Value, Red, Green, Blue, Type, Alias, Notes, ChildOf FROM DATASHORTTEXT AS DST LEFT JOIN ATTRIBUTES AS A ON A.ID = DST.Value AND A.VariableId = DST.VariableId WHERE DST.StatementId = ? AND DST.VariableId = ?;");
				PreparedStatement s4 = conn.prepareStatement("SELECT Value FROM DATALONGTEXT WHERE VariableId = ? AND StatementId = ?;");
				PreparedStatement s5 = conn.prepareStatement("SELECT Value FROM DATAINTEGER WHERE VariableId = ? AND StatementId = ?;");
				PreparedStatement s6 = conn.prepareStatement("SELECT Value FROM DATABOOLEAN WHERE VariableId = ? AND StatementId = ?;")) {
			ResultSet r1, r2, r3;
			s1.setInt(1, documentId);
			r1 = s1.executeQuery();
			while (r1.next()) {
			    statementId = r1.getInt("StatementId");
			    statementTypeId = r1.getInt("StatementTypeId");
			    sColor = new Color(r1.getInt("StatementTypeRed"), r1.getInt("StatementTypeGreen"), r1.getInt("StatementTypeBlue"));
			    cColor = new Color(r1.getInt("CoderRed"), r1.getInt("CoderGreen"), r1.getInt("CoderBlue"));
			    s2.setInt(1, statementTypeId);
			    r2 = s2.executeQuery();
			    values = new ArrayList<Value>();
			    while (r2.next()) {
			    	variableId = r2.getInt("ID");
			    	variable = r2.getString("Variable");
			    	dataType = r2.getString("DataType");
			    	if (dataType.equals("short text")) {
				    	s3.setInt(1, statementId);
				    	s3.setInt(2, variableId);
				    	r3 = s3.executeQuery();
				    	while (r3.next()) {
			            	aColor = new Color(r3.getInt("Red"), r3.getInt("Green"), r3.getInt("Blue"));
			            	Attribute attribute = new Attribute(r3.getInt("AttributeId"), r3.getString("Value"), aColor, r3.getString("Type"), r3.getString("Alias"), r3.getString("Notes"), r3.getInt("ChildOf"), true);
				    		values.add(new Value(variableId, variable, dataType, attribute));
				    	}
			    	} else if (dataType.equals("long text")) {
				    	s4.setInt(1, variableId);
				    	s4.setInt(2, statementId);
				    	r3 = s4.executeQuery();
				    	while (r3.next()) {
				    		values.add(new Value(variableId, variable, dataType, r3.getString("Value")));
				    	}
			    	} else if (dataType.equals("integer")) {
				    	s5.setInt(1, variableId);
				    	s5.setInt(2, statementId);
				    	r3 = s5.executeQuery();
				    	while (r3.next()) {
				    		values.add(new Value(variableId, variable, dataType, r3.getInt("Value")));
				    	}
			    	} else if (dataType.equals("boolean")) {
				    	s6.setInt(1, variableId);
				    	s6.setInt(2, statementId);
				    	r3 = s6.executeQuery();
				    	while (r3.next()) {
				    		values.add(new Value(variableId, variable, dataType, r3.getInt("Value")));
				    	}
			    	}
			    }
			    statements.add(new Statement(statementId, r1.getInt("CoderId"), r1.getInt("Start"), r1.getInt("Stop"), statementTypeId, values, sColor, cColor, r1.getString("StatementTypeLabel")));
			}
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] " + statements.size() + " statements have been retrieved for Document " + documentId + ".",
					statements.size() + " statements have been retrieved for Document " + documentId + ".");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to retrieve statements for Document " + documentId + ".",
					"Attempted to retrieve all statements for Document " + documentId + " from the database, but something went wrong. You should double-check if the statements are all shown!",
					e);
			Dna.logger.log(l);
		}
		return statements;
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
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Deleted " + documentIds.length + " documents (and their statements).",
					"Successfully deleted " + documentIds.length + " documents from the DOCUMENTS table in the database, and also deleted all statements that may have been contained in these documents. The transaction has been committed to the database.");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to delete documents from database.",
					"Attempted to remove " + documentIds.length + " documents from the DOCUMENTS table in the database, including all associated statements, but something went wrong. The transaction has been rolled back, and nothing has been removed.",
					e);
			Dna.logger.log(l);
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
				stmt.setString(8, documents.get(i).getType());
				stmt.setLong(9, documents.get(i).getDateTime().toEpochSecond(ZoneOffset.UTC)); // convert date-time to seconds since 01/01/1970 at 00:00:00 in UTC time zone
				stmt.executeUpdate();
			}
			conn.commit();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Added " + documents.size() + " documents to the DOCUMENTS table in the database.",
					"Successfully added " + documents.size() + " new documents to the DOCUMENTS table in the database. The transaction is complete and has been committed to the database.");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to add documents to the database.",
					"Attempted to add " + documents.size() + " new documents to the DOCUMENTS table in the database, but something went wrong. The transaction has been rolled back; nothing has been committed to the database. Check your connection.",
					e);
			Dna.logger.log(l);
		}
	}
	
	public int countDocuments() {
		int count = 0;
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("SELECT COUNT(*) FROM DOCUMENTS;")) {
			ResultSet r = s.executeQuery();
			while (r.next()) {
				count = r.getInt(1);
			}
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to count number of documents in the database.",
					"Attempted to count how many documents are in the DOCUMENTS table of the database, but failed. Check your connection.",
					e);
			Dna.logger.log(l);
		}
		return count;
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
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Coder with ID " + coderId + " could not be retrieved from the database.",
					"The details of the coder with ID " + coderId + " could not be retrieved from the database. Check your database connection.",
					e);
			Dna.logger.log(l);
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
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to retrieve hashed password for Coder " + this.cp.getCoderId() + " from database.",
					"Attempted to authenticate the coder with ID " + this.cp.getCoderId() + ", but the password hash could not be retrieved from the database. Check your connection and database integrity.",
					e);
			Dna.logger.log(l);
		}
		
		// check if the provided clear-text password corresponds to the hashed password in the database
		if (encryptedHash == null) {
			return false;
		} else {
			StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
			boolean correct = passwordEncryptor.checkPassword(clearPassword, encryptedHash);
			if (correct == true) {
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"[SQL] Coder successfully authenticated.",
						"The password provided by the coder with ID " + this.cp.getCoderId() + " matches the hash stored in the database.");
				Dna.logger.log(l);
			} else {
				LogEvent l = new LogEvent(Logger.WARNING,
						"[SQL] Coder failed to authenticate.",
						"The password provided by the coder with ID " + this.cp.getCoderId() + " does not match the hash stored in the database.");
				Dna.logger.log(l);
			}
			return correct;
		}
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
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to retrieve coders from the database.",
        			"Attempted to retrieve all coders from the database. Check your connection.",
        			e1);
        	Dna.logger.log(l);
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
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to retrieve document meta-data from the database.",
					"Attempted to retrieve document data (other than the document text) for " + documentIds.length + " documents, but this failed. Check if all documents are being displayed in the user interface.",
					e);
			Dna.logger.log(l);
		}
		return documents;
	}
	
	public boolean documentsContainStatements(int[] documentIds) {
		boolean contains = true;
		String s = "[SQL] SELECT COUNT(*) FROM STATEMENTS WHERE DocumentId IN (";
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
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to count number of statements in document selection.",
					"Attempted to count how many statements are contained in each of the " + documentIds.length + " selected documents, but something went wrong while accessing the database. Most likely this means that the document table shows misleading counts, but there should be no other negative consequences.",
					e);
			Dna.logger.log(l);
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
			ResultSet r;
			for (int i = 0; i < documentIds.length; i++) {
				s.setInt(1, documentIds[i]);
				r = s.executeQuery();
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
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] The meta-data of " + documentIds.length + " documents have been updated.",
					"The meta-data of " + documentIds.length + " documents have been updated.");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to update the meta-data of " + documentIds.length + " documents.",
					"Attempted to recode/edit " + documentIds.length + " documents. The transaction has been rolled back, and none of the documents has been updated in the database.",
					e);
			Dna.logger.log(l);
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
					+ "Value INTEGER, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "Value INTEGER, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "Value INTEGER NOT NULL, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(Value) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "Value TEXT, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
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
					+ "ChildOf INTEGER CHECK(ChildOf > 0 AND ChildOf != ID), "
					+ "UNIQUE (VariableId, Value), "
					+ "FOREIGN KEY(ChildOf) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
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
					+ "Value SMALLINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (StatementId, VariableId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "Value MEDIUMINT NOT NULL, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (StatementId, VariableId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "Value INT NOT NULL REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (StatementId, VariableId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "Value TEXT, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
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
					+ "ChildOf MEDIUMINT UNSIGNED CHECK(ChildOf > 0 AND ChildOf != ID), "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(ChildOf) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (VariableId, Value), "
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
					+ "Value INT NOT NULL CHECK(Value BETWEEN 0 AND 1), "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "Value INT NOT NULL, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "Value INT NOT NULL CHECK(Value > 0) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
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
					+ "ChildOf INT CHECK(ChildOf > 0 AND ChildOf != ID) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (VariableId, Value));");
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
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Created data structures successfully in " + cp.getType().toUpperCase() + " database.",
					"New tables and initial contents have been successfully created in " + cp.getType().toUpperCase() + " database.");
			Dna.logger.log(l);
		} catch (SQLException e) {
			success = false;
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to create data structures in " + cp.getType().toUpperCase() + " database.",
					"Attempted to create new tables and initial contents in " + cp.getType().toUpperCase() + " database, but failed during the transaction. No tables have been created, and no data have been written into the database; the transaction was rolled back. Check your database connection and SQL user rights.",
					e);
			Dna.logger.log(l);
		}
		return success;
	}
	
	public interface SQLCloseable extends AutoCloseable {
	    @Override public void close() throws SQLException;
	}

	public ArrayList<StatementType> getStatementTypes() {
		ArrayList<StatementType> statementTypes = new ArrayList<StatementType>();
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT * FROM STATEMENTTYPES;");
				PreparedStatement s2 = conn.prepareStatement("SELECT * FROM VARIABLES WHERE StatementTypeId = ?;")) {
        	ArrayList<Value> variables;
        	int statementTypeId;
			ResultSet r1 = s1.executeQuery();
			ResultSet r2;
			Color color;
        	while (r1.next()) {
            	variables = new ArrayList<Value>();
            	statementTypeId = r1.getInt("ID");
            	color = new Color(r1.getInt("Red"), r1.getInt("Green"), r1.getInt("Blue"));
            	s2.setInt(1, statementTypeId);
            	r2 = s2.executeQuery();
            	while (r2.next()) {
            		variables.add(new Value(r2.getInt("ID"), r2.getString("Variable"), r2.getString("DataType")));
            	}
            	statementTypes.add(new StatementType(r1.getInt("ID"), r1.getString("Label"), color, variables));
            }
        	LogEvent l = new LogEvent(Logger.MESSAGE,
        			"[SQL] Retrieved " + statementTypes.size() + " statement types from the database.",
        			"Retrieved " + statementTypes.size() + " statement types from the database.");
        	Dna.logger.log(l);
		} catch (SQLException e1) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to retrieve statement types from the database.",
					"Failed to retrieve statement types from the database. Check database connection and consistency of the STATEMENTTYPES and VARIABLES tables in the database.",
					e1);
			Dna.logger.log(l);
		}
		return statementTypes;
	}
	
	public int addStatement(Statement statement, int documentId) {
		int statementId = -1, attributeId = -1;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("INSERT INTO STATEMENTS (StatementTypeId, DocumentId, Start, Stop, Coder) VALUES (?, ?, ?, ?, ?);");
				PreparedStatement s2 = conn.prepareStatement("INSERT INTO DATASHORTTEXT (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s3 = conn.prepareStatement("INSERT INTO DATALONGTEXT (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s4 = conn.prepareStatement("INSERT INTO DATAINTEGER (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s5 = conn.prepareStatement("INSERT INTO DATABOOLEAN (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s6 = conn.prepareStatement("INSERT INTO ATTRIBUTES (VariableId, Value, Red, Green, Blue, Type, Alias, Notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
				PreparedStatement s7 = conn.prepareStatement("SELECT ID FROM ATTRIBUTES WHERE VariableId = ? AND Value = ?;");
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Started SQL transaction to add statement to Document " + documentId + ".",
					"Started a new SQL transaction to add a new statement to the document with ID " + documentId + ". The contents will not be written into the database until the transaction is committed.");
			Dna.logger.log(l);
			ResultSet r;
			s1.setInt(1, statement.getStatementTypeId());
			s1.setInt(2, documentId);
			s1.setInt(3, statement.getStart());
			s1.setInt(4, statement.getStop());
			s1.setInt(5, statement.getCoder());
			s1.executeUpdate();
			if (s1.getGeneratedKeys().next()) {
				statementId = s1.getGeneratedKeys().getInt(1);
			}
			l = new LogEvent(Logger.MESSAGE,
					"[SQL]     Transaction: Row with ID " + statementId + " added to the STATEMENTS table.",
					"Added a row to the STATEMENTS table during the transaction. The new statement has ID " + statementId + ".");
			Dna.logger.log(l);
			for (int i = 0; i < statement.getValues().size(); i++) {
				if (statement.getValues().get(i).getDataType().equals("short text")) {
					s6.setInt(1, statement.getValues().get(i).getVariableId());
					String value = "";
					if (statement.getValues().get(i).getValue() == null) {
						value = "";
					} else {
						value = ((Attribute) statement.getValues().get(i).getValue()).getValue(); // not sure if this case ever occurs
					}
					s6.setString(2, value);
					s6.setInt(3, 255);
					s6.setInt(4, 255);
					s6.setInt(5,  255);
					s6.setString(6, "");
					s6.setString(7, "");
					s6.setString(8,  "");
					try {
						s6.executeUpdate();
						l = new LogEvent(Logger.MESSAGE,
								"[SQL]     Transaction: Added \"" + value + "\" to the ATTRIBUTES table.",
								"Added a row with value \"" + value + "\" to the ATTRIBUTES table during the transaction.");
						Dna.logger.log(l);
					} catch (SQLException e2) {
						if (e2.getMessage().contains("UNIQUE constraint failed")) {
							l = new LogEvent(Logger.MESSAGE,
									"[SQL]     Transaction: Value \"" + value + "\" was already present in the ATTRIBUTES table.",
									"A row with value \"" + value + "\" did not have to be added to the ATTRIBUTES table during the transaction because it was already present.");
							Dna.logger.log(l);
						} else {
							l = new LogEvent(Logger.WARNING,
									"[SQL] Failed to add value \"" + value + "\" to the ATTRIBUTES table.",
									"Failed to add value \"" + value + "\" to the ATTRIBUTES table. The next step will check if the attribute is already there. If so, no problem. If not, there will be another log event with an error message.",
									e2);
							Dna.logger.log(l);
						}
					}
					attributeId = -1;
					s7.setInt(1, statement.getValues().get(i).getVariableId());
					s7.setString(2, value);
					r = s7.executeQuery();
					while (r.next()) {
						attributeId = r.getInt("ID");
					}
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]     Transaction: Attribute ID identified as " + attributeId + ".",
							"The attribute for value \"" + value + "\", which was added to, or identified in, the ATTRIBUTES table during the transaction, has ID " + attributeId + ".");
					Dna.logger.log(l);
					s2.setInt(1, statementId);
					s2.setInt(2, statement.getValues().get(i).getVariableId());
					s2.setInt(3, attributeId);
					s2.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]     Transaction: Added a value to the DATASHORTTEXT table for Variable " + statement.getValues().get(i).getVariableId() + ".",
							"Added a row with attribute ID " + attributeId + " for Variable " + statement.getValues().get(i).getVariableId() + " to the DATASHORTTEXT table during the transaction.");
					Dna.logger.log(l);
				} else if (statement.getValues().get(i).getDataType().equals("long text")) {
					s3.setInt(1, statementId);
					s3.setInt(2, statement.getValues().get(i).getVariableId());
					s3.setString(3, (String) statement.getValues().get(i).getValue());
					s3.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]     Transaction: Added a value to the DATALONGTEXT table for Variable " + statement.getValues().get(i).getVariableId() + ".",
							"Added a row for Variable " + statement.getValues().get(i).getVariableId() + " to the DATALONGTEXT table during the transaction.");
					Dna.logger.log(l);
				} else if (statement.getValues().get(i).getDataType().equals("integer")) {
					s4.setInt(1, statementId);
					s4.setInt(2, statement.getValues().get(i).getVariableId());
					s4.setInt(3, (int) statement.getValues().get(i).getValue());
					s4.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]     Transaction: Added a value to the DATAINTEGER table for Variable " + statement.getValues().get(i).getVariableId() + ".",
							"Added a row with Value " + (int) statement.getValues().get(i).getValue() + " for Variable " + statement.getValues().get(i).getVariableId() + " to the DATAINTEGER table during the transaction.");
					Dna.logger.log(l);
				} else if (statement.getValues().get(i).getDataType().equals("boolean")) {
					s5.setInt(1, statementId);
					s5.setInt(2, statement.getValues().get(i).getVariableId());
					s5.setInt(3, (int) statement.getValues().get(i).getValue());
					s5.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]     Transaction: Added a value to the DATABOOLEAN table for Variable " + statement.getValues().get(i).getVariableId() + ".",
							"Added a row with Value " + (int) statement.getValues().get(i).getValue() + " for Variable " + statement.getValues().get(i).getVariableId() + " to the DATABOOLEAN table during the transaction.");
					Dna.logger.log(l);
				}
			}
			conn.commit();
			l = new LogEvent(Logger.MESSAGE,
					"[SQL] Completed SQL transaction to add Statement " + statementId + ".",
					"Completed SQL transaction to add a new statement with ID " + statementId + " to Document " + documentId + ". The contents have been written into the database.");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to add statement to Document " + documentId + ".",
					"Failed to add statement to Document " + documentId + ". Check the connection and database availability.",
					e);
			Dna.logger.log(l);
		}
		return -1;
	}

	public void updateStatement(int statementId, ArrayList<Value> values) {
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("UPDATE DATABOOLEAN SET Value = ? WHERE StatementId = ? AND VariableId = ?;");
				PreparedStatement s2 = conn.prepareStatement("UPDATE DATAINTEGER SET Value = ? WHERE StatementId = ? AND VariableId = ?;");
				PreparedStatement s3 = conn.prepareStatement("UPDATE DATALONGTEXT SET Value = ? WHERE StatementId = ? AND VariableId = ?;");
				PreparedStatement s4 = conn.prepareStatement("UPDATE DATASHORTTEXT SET Value = ? WHERE StatementId = ? AND VariableId = ?;");
				PreparedStatement s5 = conn.prepareStatement("INSERT INTO ATTRIBUTES (VariableId, Value, Red, Green, Blue, Type, Alias, Notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
				PreparedStatement s6 = conn.prepareStatement("SELECT ID FROM ATTRIBUTES WHERE VariableId = ? AND Value = ?;");
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			LogEvent e1 = new LogEvent(Logger.MESSAGE,
					"[SQL] Started SQL transaction to update Statement " + statementId + ".",
					"Started a new SQL transaction to update the variables in the statement with ID " + statementId + ". The contents will not be written into the database until the transaction is committed.");
			Dna.logger.log(e1);
			Attribute attribute;
			int attributeId, variableId;
			ResultSet r;
			for (int i = 0; i < values.size(); i++) {
				variableId = values.get(i).getVariableId();
				if (values.get(i).getDataType().equals("boolean")) {
					s1.setInt(1, (int) values.get(i).getValue());
					s1.setInt(2, statementId);
					s1.setInt(3, variableId);
					s1.executeUpdate();
					LogEvent e2 = new LogEvent(Logger.MESSAGE,
							"[SQL]     Variable " + variableId + " in Statement " + statementId + " was updated in the transaction.",
							"Variable " + variableId + " (boolean) in Statement " + statementId + " was updated in the SQL transaction with value: " + (int) values.get(i).getValue() + ".");
					Dna.logger.log(e2);
				} else if (values.get(i).getDataType().equals("integer")) {
					s2.setInt(1, (int) values.get(i).getValue());
					s2.setInt(2, statementId);
					s2.setInt(3, variableId);
					s2.executeUpdate();
					LogEvent e2 = new LogEvent(Logger.MESSAGE,
							"[SQL]     Variable " + variableId + " in Statement " + statementId + " was updated in the transaction.",
							"Variable " + variableId + " (integer) in Statement " + statementId + " was updated in the SQL transaction with value: " + (int) values.get(i).getValue() + ".");
					Dna.logger.log(e2);
				} else if (values.get(i).getDataType().equals("long text")) {
					s3.setString(1, (String) values.get(i).getValue());
					s3.setInt(2, statementId);
					s3.setInt(3, variableId);
					s3.executeUpdate();
					LogEvent e2 = new LogEvent(Logger.MESSAGE,
							"[SQL]     Variable " + variableId + " in Statement " + statementId + " was updated in the transaction.",
							"Variable " + variableId + " (long text) in Statement " + statementId + " was updated in the SQL transaction.");
					Dna.logger.log(e2);
				} else if (values.get(i).getDataType().equals("short text")) {
					// try to recognise attribute ID from database; should be more reliable (e.g., with empty Strings)
					attribute = (Attribute) values.get(i).getValue();
					attributeId = -1;
					s6.setInt(1, variableId);
					s6.setString(2, attribute.getValue());
					r = s6.executeQuery();
					while (r.next()) {
						attributeId = r.getInt("ID");
					}
					
					if (attributeId == -1) {
						// if the attribute does not exist, insert new attribute with given String value
						s5.setInt(1, variableId);
						s5.setString(2, attribute.getValue());
						s5.setInt(3, attribute.getColor().getRed());
						s5.setInt(4, attribute.getColor().getGreen());
						s5.setInt(5, attribute.getColor().getBlue());
						s5.setString(6, attribute.getType());
						s5.setString(7, attribute.getAlias());
						s5.setString(8, attribute.getNotes());
						s5.executeUpdate();

						// new attribute has been created; now we have to get its ID
						s6.setInt(1, variableId);
						s6.setString(2, attribute.getValue());
						r = s6.executeQuery();
						while (r.next()) {
							attributeId = r.getInt(1);
						}
						LogEvent e2 = new LogEvent(Logger.MESSAGE,
								"[SQL]     Attribute with ID " + attributeId + " added to the transaction.",
								"An attribute with ID " + attributeId + " and value \"" + attribute.getValue() + "\" was created for variable ID " + variableId + " and added to the SQL transaction.");
						Dna.logger.log(e2);
					}

					// write the attribute ID as the value in the DATASHORTTEXT table
					s4.setInt(1, attributeId);
					s4.setInt(2, statementId);
					s4.setInt(3, variableId);
					s4.executeUpdate();
					LogEvent e2 = new LogEvent(Logger.MESSAGE,
							"[SQL]     Variable " + variableId + " in Statement " + statementId + " was updated in the transaction.",
							"Variable " + variableId + " (short text) in Statement " + statementId + " was updated in the SQL transaction with Attribute " + attributeId + ".");
					Dna.logger.log(e2);
				}
			}
			conn.commit();
			LogEvent e2 = new LogEvent(Logger.MESSAGE,
					"[SQL] Completed SQL transaction to update Statement " + statementId + ".",
					"Completed SQL transaction to update the variables in the statement with ID " + statementId + ". The contents have been written into the database.");
			Dna.logger.log(e2);
		} catch (SQLException e) {
			LogEvent e2 = new LogEvent(Logger.ERROR,
					"[SQL] Statement " + statementId + " could not be updated in the database.",
					"When the statement popup window for Statement " + statementId + " was closed, the contents for the different variables could not be saved into the database. The database still contains the old values before the contents were edited. Please double-check to make sure that the statement contains the right values for all variables. Check whether the database may be locked and close all programs other than DNA that are currently accessing the database before trying again.",
					e);
			Dna.logger.log(e2);
		}
	}
	
	public int cloneStatement(int statementId, int newCoderId) {
		int id = statementId;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("INSERT INTO STATEMENTS (StatementTypeId, DocumentId, Start, Stop, Coder) SELECT StatementTypeId, DocumentId, Start, Stop, Coder FROM STATEMENTS WHERE ID = ?;");
				PreparedStatement s2 = conn.prepareStatement("UPDATE STATEMENTS SET Coder = ? WHERE ID = ?;");
				PreparedStatement s3 = conn.prepareStatement("SELECT VariableId, Value FROM DATABOOLEAN WHERE StatementId = ?;");
				PreparedStatement s4 = conn.prepareStatement("INSERT INTO DATABOOLEAN (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s5 = conn.prepareStatement("SELECT VariableId, Value FROM DATAINTEGER WHERE StatementId = ?;");
				PreparedStatement s6 = conn.prepareStatement("INSERT INTO DATAINTEGER (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s7 = conn.prepareStatement("SELECT VariableId, Value FROM DATASHORTTEXT WHERE StatementId = ?;");
				PreparedStatement s8 = conn.prepareStatement("INSERT INTO DATASHORTTEXT (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s9 = conn.prepareStatement("SELECT VariableId, Value FROM DATALONGTEXT WHERE StatementId = ?;");
				PreparedStatement s10 = conn.prepareStatement("INSERT INTO DATALONGTEXT (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				SQLCloseable finish = conn::rollback) {
			ResultSet r;
			conn.setAutoCommit(false);
			
			// copy the statement in the STATEMENTS table
			s1.setInt(1, statementId);
			s1.executeUpdate();
			if (s1.getGeneratedKeys().next()) {
				id = s1.getGeneratedKeys().getInt(1);
			}
			
			// set new coder
			s2.setInt(1, newCoderId);
			s2.setInt(2, id);
			s2.executeUpdate();
			
			// clone relevant entries in the DATABOOLEAN table
			s3.setInt(1, statementId);
			r = s3.executeQuery();
			while (r.next()) {
				s4.setInt(1, id);
				s4.setInt(2, r.getInt("VariableId"));
				s4.setInt(3, r.getInt("Value"));
				s4.executeUpdate();
			}
			
			// clone relevant entries in the DATAINTEGER table
			s5.setInt(1, statementId);
			r = s5.executeQuery();
			while (r.next()) {
				s6.setInt(1, id);
				s6.setInt(2, r.getInt("VariableId"));
				s6.setInt(3, r.getInt("Value"));
				s6.executeUpdate();
			}

			// clone relevant entries in the DATASHORTTEXT table
			s7.setInt(1, statementId);
			r = s7.executeQuery();
			while (r.next()) {
				s8.setInt(1, id);
				s8.setInt(2, r.getInt("VariableId"));
				s8.setInt(3, r.getInt("Value"));
				s8.executeUpdate();
			}

			// clone relevant entries in the DATALONGTEXT table
			s9.setInt(1, statementId);
			r = s9.executeQuery();
			while (r.next()) {
				s10.setInt(1, id);
				s10.setInt(2, r.getInt("VariableId"));
				s10.setString(3, r.getString("Value"));
				s10.executeUpdate();
			}
			
			conn.commit();
			LogEvent e = new LogEvent(Logger.MESSAGE,
					"[SQL] Cloned Statement " + statementId + " --> " + id + ".",
					"Cloned Statement " + statementId + ". The new statement ID of the copy is " + id + " (new Coder ID: " + newCoderId + ") and successfully saved to the database.");
			Dna.logger.log(e);
		} catch (SQLException e1) {
			LogEvent e = new LogEvent(Logger.ERROR,
					"[SQL] Failed to clone Statement " + statementId + ".",
					"Failed to clone Statement " + statementId + " in the database. The original statement is still there, but a copy of the statement was not created. Check whether the database may be locked and close all programs other than DNA that are currently accessing the database before trying again.",
					e1);
			Dna.logger.log(e);
		}
		return id;
	}

	public Statement getStatement(int statementId) {
		Statement statement = null;
		ArrayList<Value> values;
		int statementTypeId, variableId;
		String variable, dataType;
		Color aColor, sColor, cColor;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT STATEMENTS.ID AS StatementId, StatementTypeId, STATEMENTTYPES.Label AS StatementTypeLabel, STATEMENTTYPES.Red AS StatementTypeRed, STATEMENTTYPES.Green AS StatementTypeGreen, STATEMENTTYPES.Blue AS StatementTypeBlue, Start, Stop, Coder AS CoderId, CODERS.Red AS CoderRed, CODERS.Green AS CoderGreen, CODERS.Blue AS CoderBlue FROM STATEMENTS INNER JOIN CODERS ON STATEMENTS.Coder = CODERS.ID INNER JOIN STATEMENTTYPES ON STATEMENTS.StatementTypeId = STATEMENTTYPES.ID WHERE StatementId = ?;");
				PreparedStatement s2 = conn.prepareStatement("SELECT ID, Variable, DataType FROM VARIABLES WHERE StatementTypeId = ?;");
				PreparedStatement s3 = conn.prepareStatement("SELECT A.ID AS AttributeId, StatementId, A.VariableId, DST.ID AS DataId, A.Value, Red, Green, Blue, Type, Alias, Notes, ChildOf FROM DATASHORTTEXT AS DST LEFT JOIN ATTRIBUTES AS A ON A.ID = DST.Value AND A.VariableId = DST.VariableId WHERE DST.StatementId = ? AND DST.VariableId = ?;");
				PreparedStatement s4 = conn.prepareStatement("SELECT Value FROM DATALONGTEXT WHERE VariableId = ? AND StatementId = ?;");
				PreparedStatement s5 = conn.prepareStatement("SELECT Value FROM DATAINTEGER WHERE VariableId = ? AND StatementId = ?;");
				PreparedStatement s6 = conn.prepareStatement("SELECT Value FROM DATABOOLEAN WHERE VariableId = ? AND StatementId = ?;")) {
			ResultSet r1, r2, r3;
			s1.setInt(1, statementId);
			r1 = s1.executeQuery();
			while (r1.next()) {
			    statementTypeId = r1.getInt("StatementTypeId");
			    sColor = new Color(r1.getInt("StatementTypeRed"), r1.getInt("StatementTypeGreen"), r1.getInt("StatementTypeBlue"));
			    cColor = new Color(r1.getInt("CoderRed"), r1.getInt("CoderGreen"), r1.getInt("CoderBlue"));
			    s2.setInt(1, statementTypeId);
			    r2 = s2.executeQuery();
			    values = new ArrayList<Value>();
			    while (r2.next()) {
			    	variableId = r2.getInt("ID");
			    	variable = r2.getString("Variable");
			    	dataType = r2.getString("DataType");
			    	if (dataType.equals("short text")) {
				    	s3.setInt(1, statementId);
				    	s3.setInt(2, variableId);
				    	r3 = s3.executeQuery();
				    	while (r3.next()) {
			            	aColor = new Color(r3.getInt("Red"), r3.getInt("Green"), r3.getInt("Blue"));
			            	Attribute attribute = new Attribute(r3.getInt("AttributeId"), r3.getString("Value"), aColor, r3.getString("Type"), r3.getString("Alias"), r3.getString("Notes"), r3.getInt("ChildOf"), true);
				    		values.add(new Value(variableId, variable, dataType, attribute));
				    	}
			    	} else if (dataType.equals("long text")) {
				    	s4.setInt(1, variableId);
				    	s4.setInt(2, statementId);
				    	r3 = s4.executeQuery();
				    	while (r3.next()) {
				    		values.add(new Value(variableId, variable, dataType, r3.getString("Value")));
				    	}
			    	} else if (dataType.equals("integer")) {
				    	s5.setInt(1, variableId);
				    	s5.setInt(2, statementId);
				    	r3 = s5.executeQuery();
				    	while (r3.next()) {
				    		values.add(new Value(variableId, variable, dataType, r3.getInt("Value")));
				    	}
			    	} else if (dataType.equals("boolean")) {
				    	s6.setInt(1, variableId);
				    	s6.setInt(2, statementId);
				    	r3 = s6.executeQuery();
				    	while (r3.next()) {
				    		values.add(new Value(variableId, variable, dataType, r3.getInt("Value")));
				    	}
			    	}
			    }
			    statement = new Statement(statementId, r1.getInt("CoderId"), r1.getInt("Start"), r1.getInt("Stop"), statementTypeId, values, sColor, cColor, r1.getString("StatementTypeLabel"));
			    LogEvent l = new LogEvent(Logger.MESSAGE,
			    		"[SQL] Statement " + statementId + " was retrieved from the database.",
			    		"Statement " + statementId + " was retrieved from the database.");
			    Dna.logger.log(l);
			}
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to retrieve Statement " + statementId + " from database.",
					"Failed to retrieve Statement " + statementId + " from database. Check if the connection is still there, the database file has not been moved, and make sure a statement with this ID actually exists in the database.",
					e);
			Dna.logger.log(l);
		}
		return statement;
	}
	
	public void deleteStatement(int statementId) {
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("DELETE FROM STATEMENTS WHERE ID = ?;");) {
			s.setInt(1, statementId);
			s.executeUpdate();
        	LogEvent l = new LogEvent(Logger.MESSAGE,
        			"[SQL] Statement with ID " + statementId + " was deleted.",
        			"Statement with ID " + statementId + " was deleted.");
        	Dna.logger.log(l);
		} catch (SQLException e1) {
        	LogEvent l = new LogEvent(Logger.ERROR,
        			"[SQL] Failed to delete Statement " + statementId + ".",
        			"Attempted to delete the Statement with ID " + statementId + ", but the attempt failed. Check if the database is locked, the database file has been moved, or the connection is interrupted, then try again.",
        			e1);
        	Dna.logger.log(l);
		}
	}
	
	public Attribute[] getAttributes(int variableId) {
		ArrayList<Attribute> attributesList = new ArrayList<Attribute>();
		boolean inDatabase;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT ID, Value, Red, Green, Blue, Type, Alias, Notes, ChildOf FROM ATTRIBUTES WHERE VariableId = ?;");
				PreparedStatement s2 = conn.prepareStatement("SELECT COUNT(ID) FROM DATASHORTTEXT WHERE VariableId = ? AND Value = ?;")) {
			ResultSet r1, r2;
			Color color;
			s1.setInt(1, variableId);
			s2.setInt(1, variableId);
			r1 = s1.executeQuery();
        	while (r1.next()) {
            	color = new Color(r1.getInt("Red"), r1.getInt("Green"), r1.getInt("Blue"));
            	s2.setInt(2, r1.getInt("ID"));
            	r2 = s2.executeQuery();
            	if (r2.getInt(1) > 0) {
            		inDatabase = true;
            	} else {
            		inDatabase = false;
            	}
            	attributesList.add(new Attribute(r1.getInt("ID"), r1.getString("Value"), color, r1.getString("Type"), r1.getString("Alias"), r1.getString("Notes"), r1.getInt("ChildOf"), inDatabase));
            }
        	LogEvent e = new LogEvent(Logger.MESSAGE,
        			"[SQL] " + attributesList.size() + " attribute(s) retrieved for Variable " + variableId + ".",
        			attributesList.size() + " attribute(s) retrieved from the database for Variable " + variableId + ".");
        	Dna.logger.log(e);
		} catch (SQLException e1) {
        	LogEvent e = new LogEvent(Logger.WARNING,
        			"[SQL] Attributes for Variable " + variableId + " could not be retrieved.",
        			"Attributes for Variable " + variableId + " could not be retrieved. Check if the database is still there and/or if the connection has been interrupted, then try again.",
        			e1);
        	Dna.logger.log(e);
		}
		Attribute[] attributesArray = new Attribute[attributesList.size()];
		attributesArray = attributesList.toArray(attributesArray);
		return attributesArray;
	}
}