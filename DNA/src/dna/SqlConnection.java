package dna;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import dna.dataStructures.AttributeVector;
import dna.dataStructures.Coder;
import dna.dataStructures.CoderRelation;
import dna.dataStructures.Data;
import dna.dataStructures.Document;
import dna.dataStructures.Regex;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementLink;
import dna.dataStructures.StatementType;

import static java.lang.Math.toIntExact;
import java.awt.Color;

public class SqlConnection {
	String dbtype;
	String dbfile;
	String login;
	String password;
	Connection connection = null;
	PreparedStatement preStatement = null;
	ResultSet result = null;

	
	/* =================================================================================================================
	 * Constructor and general-purpose functions for executing statements and testing and closing connections
	 * =================================================================================================================
	 */

	public SqlConnection(String dbtype, String dbfile, String login, String password) {
		this.dbtype = dbtype;
		this.dbfile = dbfile;
		this.login = login;
		this.password = password;
		try {
			if (dbtype.equals("mysql")) {
				Class.forName("com.mysql.jdbc.Driver");
				this.connection = DriverManager.getConnection("jdbc:mysql://" + dbfile, login, password);
			} else if (dbtype.equals("sqlite")) {
				Class.forName("org.sqlite.JDBC");
				this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			} else {
				System.err.println("Database format not recognized: " + dbtype + ".");
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			// no connection was established in the first place
			e.printStackTrace();
		}
	}

	/**
	 * Execute a statement on the database.
	 * 
	 * @param myStatement     A string representation of the SQL statement.
	 */
	public void executeStatement(String myStatement) {
		try {
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myStatement);
			preStatement.execute();
			preStatement.close();
		} catch (SQLException e) {
			System.err.println("Database access could not be executed properly. Report this problem along with the \n "
					+ "error log if you can see a systematic pattern here. Also, reload your file.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Execute a query on the database and get an object back.
	 * 
	 * @param myStatement     A string representation of the SQL statement.
	 * @return                The ID of the row that was changed.
	 * @throws SQLException 
	 */
	public Object executeQueryForObject(String myQuery) throws SQLException {
		Object object = null;
		PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
		ResultSet result = preStatement.executeQuery();
		if (result.next()) {
			do {
				object = result.getObject(1);
			} while (result.next());
		}
		result.close();
		preStatement.close();
		return object;
	}
	
	/**
	 * Execute a query on the database and get an object back.
	 * 
	 * @param myQuery         A string representation of the SQL query.
	 * @return                An array list with resulting objects.
	 * @throws SQLException 
	 */
	public ArrayList<Object> executeQueryForList(String myQuery){
		ArrayList<Object> al = new ArrayList<Object>();
		try {
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result;
			result = preStatement.executeQuery();
			if (result.next()) {
				do {
					al.add(result.getObject(1));
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return al;
	}

	/**
	 * Tests whether a mySQL connection can be established and returns a status message.
	 * 
	 * @return   Status message
	 * @throws   SQLException 
	 */
	public String testNewMySQLConnection() {
		if (connection == null) {
			return "Error: Connection could not be established!";
		}
		ArrayList<String> tableNames = new ArrayList<String>();
		tableNames.add("STATEMENTS");
		tableNames.add("DOCUMENTS");
		tableNames.add("CODERS");
		tableNames.add("SETTINGS");
		tableNames.add("CODERRELATIONS");
		tableNames.add("CODERPERMISSIONS");
		tableNames.add("REGEXES");
		tableNames.add("STATEMENTTYPES");
		tableNames.add("STATEMENTLINKS");
		tableNames.add("VARIABLES");
		tableNames.add("DATABOOLEAN");
		tableNames.add("DATAINTEGER");
		tableNames.add("DATASHORTTEXT");
		tableNames.add("DATALONGTEXT");
		tableNames.add("ATTRIBUTES");
		
		boolean tablesExist = false;
		for (int i = 0; i < tableNames.size(); i++) {
			ArrayList<Object> al = executeQueryForList("SHOW TABLES LIKE '" + tableNames.get(i) + "'");
			if (al.size() > 0) {
				tablesExist = true;
			}
		}
		if (tablesExist == true) {
			return "Warning: Database contains data that may be overwritten!";
		} else {
			return "OK. Tables will be created.";
		}
	}
	
	/**
	 * Tests whether a mySQL connection can be established and returns a status message.
	 * 
	 * @return   Status message
	 * @throws   SQLException 
	 */
	public String testExistingMySQLConnection() {
		if (connection == null) {
			return("Error: Connection could not be established!");
		}
		ArrayList<String> tableNames = new ArrayList<String>();
		tableNames.add("STATEMENTS");
		tableNames.add("DOCUMENTS");
		tableNames.add("CODERS");
		tableNames.add("SETTINGS");
		tableNames.add("CODERRELATIONS");
		tableNames.add("REGEXES");
		tableNames.add("STATEMENTTYPES");
		tableNames.add("STATEMENTLINKS");
		tableNames.add("VARIABLES");
		tableNames.add("CODERPERMISSIONS");
		tableNames.add("DATABOOLEAN");
		tableNames.add("DATAINTEGER");
		tableNames.add("DATASHORTTEXT");
		tableNames.add("DATALONGTEXT");
		tableNames.add("ATTRIBUTES");
		int count = 0;
		for (int i = 0; i < tableNames.size(); i++) {
			try {
				@SuppressWarnings("unused")
				int test =  toIntExact((long) executeQueryForObject("SELECT COUNT(*) FROM " + tableNames.get(i)));
				count++;
			} catch (Exception e) {
				// if we end up here, the table does not exist yet in the database
			}
		}
		if (count == tableNames.size()) {
			return("OK. The database contains all necessary tables.");
		} else {
			return("Error: Database does not contain all tables necessary.");
		}
	}

	/**
	 * @return     Data object.
	 */
	public Data getAllData() {
		Data data = new Data();
		data.setSettings(getAllSettings());
		data.setDocuments(getAllDocuments());
		data.setCoders(getAllCoders());
		data.setCoderRelations(getAllCoderRelations());
		data.setRegexes(getAllRegexes());
		data.setStatementLinks(getAllStatementLinks());
		data.setStatementTypes(getAllStatementTypes());
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
		try {
			String myQuery = "SELECT * FROM STATEMENTS";
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				do {
					int id = result.getInt("ID");
					int documentId = result.getInt("DocumentId");
					int start = result.getInt("Start");
					int stop = result.getInt("Stop");
					int statementTypeId = result.getInt("StatementTypeId");
					int coder = result.getInt("Coder");
					Date date = data.getDocument(documentId).getDate();
					StatementType st = data.getStatementTypeById(statementTypeId);
					LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
					
					Iterator<String> keyIterator = st.getVariables().keySet().iterator();
			        while (keyIterator.hasNext()){
			    		String key = keyIterator.next();
			    		String value = st.getVariables().get(key);
			    		String tableExtension = "";
			    		if (value.equals("boolean")) {
			    			tableExtension = "BOOLEAN";
			    		} else if (value.equals("integer")) {
			    			tableExtension = "INTEGER";
			    		} else if (value.equals("short text")) {
			    			tableExtension = "SHORTTEXT";
			    		} else if (value.equals("long text")) {
			    			tableExtension = "LONGTEXT";
			    		}
			    		String myQuery2 = "SELECT * FROM DATA" + tableExtension + " WHERE StatementId = " + id 
			    				+ " AND VariableId = (SELECT ID FROM VARIABLES WHERE StatementTypeId = " + statementTypeId 
			    				+ " AND Variable = '" + key + "')";
						PreparedStatement preStatement2 = (PreparedStatement) connection.prepareStatement(myQuery2);
						ResultSet result2 = preStatement2.executeQuery();
						if (result2.next()) {
							do {
								values.put(key, result2.getObject("Value"));
							} while (result2.next());
						}
						result2.close();
						preStatement2.close();
						if (values.size() == 0 || values.get(key) == null) {  // Fix errors here if no statement contents availabe
							System.err.print("Statement " + id + ": variable \"" + key + "\" was not saved... ");
							String query = "SELECT ID FROM VARIABLES WHERE (StatementTypeId = " + statementTypeId 
									+ " AND Variable = '" + key + "')";
							int varId = (int) executeQueryForObject(query);
							String replacementValue = "0";
							if (value.equals("short text") || value.equals("long text")) {
								replacementValue = "''";
							}
							String statement = "INSERT INTO DATA" + tableExtension + " (StatementId, VariableId, StatementTypeId, Value) "
									+ "Values (" + id + ", " + varId + ", " + statementTypeId + ", " + replacementValue + ")";
							executeStatement(statement);
							if (value.equals("short text") || value.equals("long text")) {
								values.put(key, "");
							} else {
								values.put(key, 0);
							}
							System.err.println("The problem has been fixed. Please review this statement.");
						}
			    	}
					Statement statement = new Statement(id, documentId, start, stop, date, statementTypeId, coder, values);
					statements.add(statement);
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		data.setStatements(statements);
		data.setAttributes(getAllAttributes());
		return data;
	}

	
	/* =================================================================================================================
	 * Function for creating new database structures
	 * =================================================================================================================
	 */

	public void createDataStructure() {
		if (dbtype.equals("sqlite")) {
			executeStatement("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property TEXT PRIMARY KEY, " 
					+ "Value TEXT)");

			executeStatement("CREATE TABLE IF NOT EXISTS CODERS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, " 
					+ "Name TEXT, "
					+ "Red INTEGER, "
					+ "Green INTEGER, "
					+ "Blue INTEGER, "
					+ "Password TEXT)");

			executeStatement("CREATE TABLE IF NOT EXISTS DOCUMENTS("
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
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Label TEXT, " 
					+ "Red INTEGER, "
					+ "Green INTEGER, " 
					+ "Blue INTEGER)");
			
			executeStatement("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID INTEGER NOT NULL PRIMARY KEY, " 
					+ "Variable TEXT, "
					+ "DataType TEXT, "
					+ "StatementTypeId INTEGER, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "UNIQUE (Variable, StatementTypeId))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label TEXT PRIMARY KEY, " 
					+ "Red INTEGER, "
					+ "Green INTEGER, " 
					+ "Blue INTEGER)");
			
			executeStatement("CREATE TABLE IF NOT EXISTS CODERPERMISSIONS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, " 
					+ "Coder INTEGER, "
					+ "Type TEXT, "
					+ "Permission INTEGER, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID), "
					+ "UNIQUE (Coder, Type))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, " 
					+ "Coder INTEGER, "
					+ "OtherCoder INTEGER, "
					+ "viewStatements INTEGER, "
					+ "editStatements INTEGER, "
					+ "viewDocuments INTEGER, "
					+ "editDocuments INTEGER, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID), "
					+ "FOREIGN KEY(OtherCoder) REFERENCES CODERS(ID))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, " 
					+ "StatementTypeId INTEGER, "
					+ "DocumentId INTEGER, " 
					+ "Start INTEGER, " 
					+ "Stop INTEGER, "
					+ "Coder INTEGER, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID), "
					+ "FOREIGN KEY(DocumentId) REFERENCES DOCUMENTS(ID))");
			
	        executeStatement("CREATE TABLE IF NOT EXISTS STATEMENTLINKS("
	        		+ "ID INTEGER PRIMARY KEY NOT NULL, " 
					+ "SourceId INTEGER NOT NULL, " 
	        		+ "TargetId INTEGER NOT NULL, "
	                + "FOREIGN KEY(SourceId) REFERENCES STATEMENTS(ID),"
	                + "FOREIGN KEY(TargetId) REFERENCES STATEMENTS(ID))");
	        
	        executeStatement("CREATE TABLE IF NOT EXISTS DATABOOLEAN("
	        		+ "ID INTEGER PRIMARY KEY NOT NULL, "
	        		+ "StatementId INTEGER NOT NULL, "
	        		+ "VariableId INTEGER NOT NULL, "
	        		+ "StatementTypeId INTEGER, "
	        		+ "Value INTEGER, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "UNIQUE (StatementId, VariableId))");
	        
	        executeStatement("CREATE TABLE IF NOT EXISTS DATAINTEGER("
	        		+ "ID INTEGER PRIMARY KEY NOT NULL, "
	        		+ "StatementId INTEGER NOT NULL, "
	        		+ "VariableId INTEGER NOT NULL, "
	        		+ "StatementTypeId INTEGER, "
	        		+ "Value INTEGER, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "UNIQUE (StatementId, VariableId))");

	        executeStatement("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
	        		+ "ID INTEGER PRIMARY KEY NOT NULL, "
	        		+ "StatementId INTEGER NOT NULL, "
	        		+ "VariableId INTEGER NOT NULL, "
	        		+ "StatementTypeId INTEGER, "
	        		+ "Value TEXT, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "UNIQUE (StatementId, VariableId))");
	        
	        executeStatement("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
	        		+ "ID INTEGER PRIMARY KEY NOT NULL, "
	        		+ "StatementId INTEGER NOT NULL, "
	        		+ "VariableId INTEGER NOT NULL, "
	        		+ "StatementTypeId INTEGER, "
	        		+ "Value TEXT, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "UNIQUE (StatementId, VariableId))");
	        
	        executeStatement("CREATE TABLE IF NOT EXISTS ATTRIBUTES("
	        		+ "ID INTEGER PRIMARY KEY NOT NULL, "
	        		+ "VariableId INTEGER NOT NULL, "
	        		+ "Value TEXT NOT NULL, "
					+ "Red INTEGER, "
					+ "Green INTEGER, "
					+ "Blue INTEGER, "
					+ "Type TEXT, " 
					+ "Alias TEXT, "
					+ "Notes TEXT, " 
					+ "ChildOf TEXT, " 
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID))");
	        
		} else if (dbtype.equals("mysql")) {
			
			executeStatement("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property VARCHAR(200), " 
					+ "Value VARCHAR(200),"
					+ "PRIMARY KEY (Property))");

			executeStatement("CREATE TABLE IF NOT EXISTS CODERS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, " 
					+ "Name VARCHAR(200), "
					+ "Red SMALLINT UNSIGNED, "
					+ "Green SMALLINT UNSIGNED, "
					+ "Blue SMALLINT UNSIGNED, "
					+ "Password VARCHAR(50), "
					+ "PRIMARY KEY(ID))");

			executeStatement("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL, "
					+ "Title VARCHAR(200), " 
					+ "Text MEDIUMTEXT, "
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "Author VARCHAR(200), "
					+ "Source VARCHAR(200), " 
					+ "Section VARCHAR(200), "
					+ "Notes TEXT, " 
					+ "Type VARCHAR(200), "
					+ "Date BIGINT, " 
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID), " 
					+ "PRIMARY KEY(ID))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID SMALLINT UNSIGNED NOT NULL, "
					+ "Label VARCHAR(200), " 
					+ "Red SMALLINT UNSIGNED, "
					+ "Green SMALLINT UNSIGNED, " 
					+ "Blue SMALLINT UNSIGNED, "
					+ "PRIMARY KEY(ID))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Variable VARCHAR(200), " 
					+ "DataType VARCHAR(200), "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), " 
					+ "UNIQUE KEY VariableType (Variable, StatementTypeId), "
					+ "PRIMARY KEY(ID))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label VARCHAR(200), " 
					+ "Red SMALLINT UNSIGNED, "
					+ "Green SMALLINT UNSIGNED, " 
					+ "Blue SMALLINT UNSIGNED, "
					+ "PRIMARY KEY(Label))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS CODERPERMISSIONS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, " 
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "Type VARCHAR(50), "
					+ "Permission SMALLINT UNSIGNED, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID), "
					+ "UNIQUE KEY CoderPerm (Coder, Type), "
					+ "PRIMARY KEY(ID))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, " 
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "OtherCoder SMALLINT UNSIGNED NOT NULL, "
					+ "viewStatements SMALLINT UNSIGNED, "
					+ "editStatements SMALLINT UNSIGNED, "
					+ "viewDocuments SMALLINT UNSIGNED, "
					+ "editDocuments SMALLINT UNSIGNED, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID), "
					+ "FOREIGN KEY(OtherCoder) REFERENCES CODERS(ID), "
					+ "PRIMARY KEY(ID))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL, "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "DocumentId MEDIUMINT UNSIGNED NOT NULL, "
					+ "Start BIGINT UNSIGNED, " 
					+ "Stop BIGINT UNSIGNED, "
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID), " 
					+ "FOREIGN KEY(DocumentId) REFERENCES DOCUMENTS(ID), "
					+ "PRIMARY KEY(ID))");

	        executeStatement("CREATE TABLE IF NOT EXISTS STATEMENTLINKS("
	        		+ "ID INTEGER UNSIGNED NOT NULL, " 
					+ "SourceId MEDIUMINT UNSIGNED NOT NULL, " 
	        		+ "TargetId MEDIUMINT UNSIGNED NOT NULL, "
	                + "FOREIGN KEY(SourceId) REFERENCES STATEMENTS(ID),"
	                + "FOREIGN KEY(TargetId) REFERENCES STATEMENTS(ID), "
					+ "PRIMARY KEY(ID))");
	        
			executeStatement("CREATE TABLE IF NOT EXISTS STATEMENTLINKS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, " 
					+ "SourceId MEDIUMINT NOT NULL, " 
					+ "TargetId MEDIUMINT NOT NULL, "
					+ "FOREIGN KEY(SourceId) REFERENCES STATEMENTS(ID),"
					+ "FOREIGN KEY(TargetId) REFERENCES STATEMENTS(ID), "
					+ "PRIMARY KEY(ID))");
			
	        executeStatement("CREATE TABLE IF NOT EXISTS DATABOOLEAN("
	        		+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
	        		+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
	        		+ "VariableId SMALLINT UNSIGNED NOT NULL, "
	        		+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
	        		+ "Value SMALLINT UNSIGNED NOT NULL, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "UNIQUE KEY StatementVariable (StatementId, VariableId), "
					+ "PRIMARY KEY(ID))");
	        
	        executeStatement("CREATE TABLE IF NOT EXISTS DATAINTEGER("
	        		+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
	        		+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
	        		+ "VariableId SMALLINT UNSIGNED NOT NULL, "
	        		+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
	        		+ "Value MEDIUMINT NOT NULL, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "UNIQUE KEY StatementVariable (StatementId, VariableId), "
					+ "PRIMARY KEY(ID))");

	        executeStatement("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
	        		+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
	        		+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
	        		+ "VariableId SMALLINT UNSIGNED NOT NULL, "
	        		+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
	        		+ "Value VARCHAR(200), "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "UNIQUE KEY StatementVariable (StatementId, VariableId), "
					+ "PRIMARY KEY(ID))");

	        executeStatement("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
	        		+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
	        		+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
	        		+ "VariableId SMALLINT UNSIGNED NOT NULL, "
	        		+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
	        		+ "Value TEXT, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID), "
					+ "UNIQUE KEY StatementVariable (StatementId, VariableId), "
					+ "PRIMARY KEY(ID))");

	        executeStatement("CREATE TABLE IF NOT EXISTS ATTRIBUTES("
	        		+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
	        		+ "VariableId SMALLINT UNSIGNED NOT NULL, "
	        		+ "Value TEXT, "
					+ "Red SMALLINT UNSIGNED, "
					+ "Green SMALLINT UNSIGNED, " 
					+ "Blue SMALLINT UNSIGNED, "
					+ "Type TEXT, "
					+ "Alias TEXT, "
					+ "Notes TEXT, " 
					+ "ChildOf TEXT, " 
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
					+ "PRIMARY KEY(ID))");
		}
	}

	
	/* =================================================================================================================
	 * Functions related to documents
	 * =================================================================================================================
	 */

	/**
	 * @return     Array list of all documents in the SQL database.
	 */
	private ArrayList<Document> getAllDocuments() {
		ArrayList<Document> al = new ArrayList<Document>();
		try {
			String myQuery = "SELECT * FROM DOCUMENTS";
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				do {
					int id = result.getInt("ID");
					long d = result.getLong("Date");
					Date date = new Date(d);
					Document document = new Document(
							id, 
							result.getString("Title"), 
							result.getString("Text"), 
							result.getInt("Coder"), 
							result.getString("Author"), 
							result.getString("Source"), 
							result.getString("Section"), 
							result.getString("Notes"), 
							result.getString("Type"), 
							date
					);
					al.add(document);
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.sort(al);
		return al;
	}

	/**
	 * Add multiple new documents to the DOCUMENTS table of the database.
	 * 
	 * @param al An array list of Document objects to be inserted into the DOCUMENTS table
	 */
	public void insertDocuments(ArrayList<Document> al) {
		if (al.size() > 0) {
			String query = "INSERT INTO DOCUMENTS(ID, Title, Text, Coder, Author, Source, Section, Notes, Type, Date) VALUES ";
			for (int i = 0; i < al.size(); i++) {
				query = query + "(" + al.get(i).getId() + ", '" + al.get(i).getTitle().replaceAll("'", "''") + "', '" 
						+ al.get(i).getText().replaceAll("'", "''") + "', " + al.get(i).getCoder() + ", '" 
						+ al.get(i).getAuthor().replaceAll("'", "''")  + "', '" + al.get(i).getSource().replaceAll("'", "''")  + "', '" 
						+ al.get(i).getSection().replaceAll("'", "''") + "', '" + al.get(i).getNotes().replaceAll("'", "''") + "', '" 
						+ al.get(i).getType().replaceAll("'", "''") + "', " + al.get(i).getDate().getTime() + ")";
				if (i < al.size() - 1) {
					query = query + ", ";
				}
			}
			executeStatement(query);
		}
	}

	/**
	 * @param document   Document to add to/update in the DOCUMENTS table
	 */
	public void upsertDocument(Document document) {
		executeStatement("REPLACE INTO DOCUMENTS(ID, Title, Text, Coder, Author, Source, Section, Notes, Type, Date) "
				+ "VALUES (" + document.getId() + ", '" + document.getTitle().replaceAll("'", "''")  + "', '" 
				+ document.getText().replaceAll("'", "''") + "', " + document.getCoder() + ", '" 
				+ document.getAuthor().replaceAll("'", "''")  + "', '" + document.getSource().replaceAll("'", "''")  + "', '" 
				+ document.getSection().replaceAll("'", "''") + "', '" + document.getNotes().replaceAll("'", "''") + "', '" 
				+ document.getType().replaceAll("'", "''") + "', " + document.getDate().getTime() + ")");
	}
	
	public void removeDocument(int documentId) {
		executeStatement("DELETE FROM DATABOOLEAN WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = " + documentId + ")");
		executeStatement("DELETE FROM DATAINTEGER WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = " + documentId + ")");
		executeStatement("DELETE FROM DATASHORTTEXT WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = " + documentId + ")");
		executeStatement("DELETE FROM DATALONGTEXT WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = " + documentId + ")");
		executeStatement("DELETE FROM STATEMENTS WHERE DocumentId = " + documentId);
		executeStatement("DELETE FROM DOCUMENTS WHERE ID = " + documentId);
	}
	
	public void removeDocuments(ArrayList<Integer> documentIds) {
		// create a string of document IDs for batch-selecting entries in SQL
		String ids = "";
		for (int i = 0; i < documentIds.size(); i++) {
			ids = ids + documentIds.get(i);
			if (i < documentIds.size() - 1) {
				ids = ids + ", ";
			}
		}
		
		// remove statements and documents
		executeStatement("DELETE FROM DATABOOLEAN WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId IN (" + ids + "))");
		executeStatement("DELETE FROM DATAINTEGER WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId IN (" + ids + "))");
		executeStatement("DELETE FROM DATASHORTTEXT WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId IN (" + ids + "))");
		executeStatement("DELETE FROM DATALONGTEXT WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId IN (" + ids + "))");
		executeStatement("DELETE FROM STATEMENTS WHERE DocumentId IN (" + ids + ")");
		executeStatement("DELETE FROM DOCUMENTS WHERE ID IN (" + ids + ")");
		
		// free up unused physical space in the database
		if (dbtype.equals("mysql")) {
			executeStatement("OPTIMIZE TABLE DATABOOLEAN");
			executeStatement("OPTIMIZE TABLE DATAINTEGER");
			executeStatement("OPTIMIZE TABLE DATASHORTTEXT");
			executeStatement("OPTIMIZE TABLE DATALONGTEXT");
			executeStatement("OPTIMIZE TABLE STATEMENTS");
			executeStatement("OPTIMIZE TABLE DOCUMENTS");
		} else if (dbtype.equals("sqlite")) {
			executeStatement("VACUUM");
		}
	}
	
	/**
	 * Update the date in multiple documents.
	 * 
	 * @param documentIds  An array list of IDs of the documents to update
	 * @param newDates  An array list of new dates to insert; same length and order as documentIds
	 */
	public void updateDocumentDates(ArrayList<Integer> documentIds, ArrayList<Date> newDates) {
		String myStatement = "UPDATE DOCUMENTS SET Date = ? WHERE ID = ?;";
		try {
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myStatement);
			connection.setAutoCommit(false);
			for (int i = 0; i < documentIds.size(); i++) {
				preStatement.setLong(1, newDates.get(i).getTime());
				preStatement.setInt(2, documentIds.get(i));
				preStatement.addBatch();
			}
			preStatement.executeBatch();
			connection.commit();
			preStatement.clearBatch();
			preStatement.close();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			System.err.println("Database access could not be executed properly. Report this problem along with the \n "
					+ "error log if you can see a systematic pattern here. Also, reload your file.");
			e.printStackTrace();
		}
	}

	
	/* =================================================================================================================
	 * Functions related to statements
	 * =================================================================================================================
	 */

	public void addStatements(ArrayList<Statement> al) {
		if (al.size() > 0) {
			
			class Tuple implements Comparable<Tuple> {
				int statementTypeId;
				String variable;
				public Tuple(int statementTypeId, String variable) {
					this.statementTypeId = statementTypeId;
					this.variable = variable;
				}

				// how should entries be sorted in a list or table?
				public int compareTo(Tuple t) {
					if (((Integer) this.statementTypeId).compareTo(t.statementTypeId) < 0) {
						return -1;
					} else if (((Integer) this.statementTypeId).compareTo(t.statementTypeId) > 0) {
						return 1;
					} else {
						if (this.variable.compareTo(t.variable) < 0) {
							return -1;
						} else if (this.variable.compareTo(t.variable) > 0) {
							return 1;
						} else {
							return 0;
						}
					}
				}
				
				//necessary for sorting purposes
				public boolean equals(Object o) {
					if (o == null) return false;
					if (this == o) return true;
					if (getClass() != o.getClass()) return false;
					return compareTo((Tuple) o) == 0;
				}
			}
			
			TreeMap<Tuple, String> map = new TreeMap<Tuple, String>();
			try {
				String myQuery = "SELECT Variable, DataType, StatementTypeId FROM VARIABLES";
				PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
				ResultSet result = preStatement.executeQuery();
				if (result.next()) {
					do {
						map.put(new Tuple(result.getInt("StatementTypeId"), result.getString("Variable")), result.getString("DataType"));
					} while (result.next());
				}
				result.close();
				preStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			String queryStatements = "INSERT INTO STATEMENTS (ID, StatementTypeId, DocumentId, Start, Stop, Coder) VALUES ";
			String queryShortText = "INSERT INTO DATASHORTTEXT (StatementId, VariableId, StatementTypeId, Value) VALUES ";
			String queryLongText = "INSERT INTO DATALONGTEXT (StatementId, VariableId, StatementTypeId, Value) VALUES ";
			String queryBoolean = "INSERT INTO DATABOOLEAN (StatementId, VariableId, StatementTypeId, Value) VALUES ";
			String queryInteger = "INSERT INTO DATAINTEGER (StatementId, VariableId, StatementTypeId, Value) VALUES ";
			int statementId;
			int statementTypeId;
			int documentId;
			int start;
			int stop;
			int coderId;
			for (int i = 0; i < al.size(); i++) {
				statementId = al.get(i).getId();
				statementTypeId = al.get(i).getStatementTypeId();
				documentId = al.get(i).getDocumentId();
				start = al.get(i).getStart();
				stop = al.get(i).getStop();
				coderId = al.get(i).getCoder();
				queryStatements = queryStatements + "(" + statementId + ", " + statementTypeId + ", " + documentId + ", " + start 
						+ ", " + stop  + ", " + coderId + ")";
				
				Iterator<String> keyIterator = al.get(i).getValues().keySet().iterator();
		        while (keyIterator.hasNext()){
		    		String key = keyIterator.next();
		    		Object object = al.get(i).getValues().get(key);
		    		String type = map.get(new Tuple(statementTypeId, key));
		    		if (type.equals("short text")) {
		    			String myString = ((String) object).replaceAll("'", "''");
		    			queryShortText = queryShortText + "(" + statementId + ", (SELECT ID FROM VARIABLES WHERE Variable = '" + key 
			    				+ "' AND StatementTypeId = " + statementTypeId + "), " + statementTypeId + ", '" + myString + "'), ";
		    		} else if (type.equals("long text")) {
		    			String myString = ((String) object).replaceAll("'", "''");
		    			queryLongText = queryLongText + "(" + statementId + ", (SELECT ID FROM VARIABLES WHERE Variable = '" + key 
			    				+ "' AND StatementTypeId = " + statementTypeId + "), " + statementTypeId + ", '" + myString + "'), ";
		    		} else if (type.equals("boolean")) {
		    			queryBoolean = queryBoolean + "(" + statementId + ", (SELECT ID FROM VARIABLES WHERE Variable = '" + key 
			    				+ "' AND StatementTypeId = " + statementTypeId + "), " + statementTypeId + ", " + (int) object + "), ";
		    		} else if (type.equals("integer")) {
		    			queryInteger = queryInteger + "(" + statementId + ", (SELECT ID FROM VARIABLES WHERE Variable = '" + key 
			    				+ "' AND StatementTypeId = " + statementTypeId + "), " + statementTypeId + ", " + (int) object + "), ";
		    		}
		        }
				if (i < al.size() - 1) {
					queryStatements = queryStatements + ", ";
				}
			}
			if (queryShortText.endsWith(", ")) {
				queryShortText = queryShortText.substring(0, queryShortText.length() - 2);
			}
			if (queryLongText.endsWith(", ")) {
				queryLongText = queryLongText.substring(0, queryLongText.length() - 2);
			}
			if (queryBoolean.endsWith(", ")) {
				queryBoolean = queryBoolean.substring(0, queryBoolean.length() - 2);
			}
			if (queryInteger.endsWith(", ")) {
				queryInteger = queryInteger.substring(0, queryInteger.length() - 2);
			}
			executeStatement(queryStatements);
			if (!queryShortText.endsWith("VALUES ")) {
				executeStatement(queryShortText);
			}
			if (!queryLongText.endsWith("VALUES ")) {
				executeStatement(queryLongText);
			}
			if (!queryBoolean.endsWith("VALUES ")) {
				executeStatement(queryBoolean);
			}
			if (!queryInteger.endsWith("VALUES ")) {
				executeStatement(queryInteger);
			}
		}
	}
	
	public void addStatement(Statement statement, LinkedHashMap<String, String> variables) {
		executeStatement("INSERT INTO STATEMENTS(ID, StatementTypeId, DocumentId, Start, Stop, Coder) "
				+ "VALUES (" + statement.getId() + ", " + statement.getStatementTypeId() + ", " + statement.getDocumentId() 
				+ ", " + statement.getStart() + ", " + statement.getStop() + ", " + statement.getCoder() + ")");
		
		Iterator<String> keyIterator = statement.getValues().keySet().iterator();
        while (keyIterator.hasNext()){
    		String key = keyIterator.next();
    		Object object = statement.getValues().get(key);
    		String type = variables.get(key);
			String tableExtension = null;
			String ap = "";
			if (type.equals("boolean")) {
				tableExtension = "BOOLEAN";
			} else if (type.equals("integer")) {
    			tableExtension = "INTEGER";
    		} else if (type.equals("short text")) {
    			tableExtension = "SHORTTEXT";
    			ap = "'";
    			object = ((String) object).replaceAll("'", "''");
    		} else if (type.equals("long text")) {
    			tableExtension = "LONGTEXT";
    			ap = "'";
    			object = ((String) object).replaceAll("'", "''");
    		}
			
			String myStatement = "INSERT INTO DATA" + tableExtension + " (StatementId, VariableId, StatementTypeId, Value) "
					+ "VALUES (" + statement.getId() + ", " + "(SELECT ID FROM VARIABLES WHERE Variable = '" + key 
					+ "' AND StatementTypeId = " + statement.getStatementTypeId() + "), " + statement.getStatementTypeId() + ", "  
					+ ap + object + ap + ")";
			executeStatement(myStatement);
    	}
	}
	
	/**
	 * @param statement       A Statement object.
	 * @param variables       A LinkedHashMap as contained in a statement type.
	 */
	public void upsertStatement(Statement statement, LinkedHashMap<String, String> variables) {
		executeStatement("REPLACE INTO STATEMENTS(ID, StatementTypeId, DocumentId, Start, Stop, Coder) "
				+ "VALUES (" + statement.getId() + ", " + statement.getStatementTypeId() + ", " + statement.getDocumentId() 
				+ ", " + statement.getStart() + ", " + statement.getStop() + ", " + statement.getCoder() + ")");
		
		Iterator<String> keyIterator = statement.getValues().keySet().iterator();
        while (keyIterator.hasNext()) {
    		String key = keyIterator.next();
    		Object object = statement.getValues().get(key);
    		String type = variables.get(key);
			String tableExtension = null;
			String ap = "";
			if (type.equals("boolean")) {
				tableExtension = "BOOLEAN";
			} else if (type.equals("integer")) {
    			tableExtension = "INTEGER";
    		} else if (type.equals("short text")) {
    			tableExtension = "SHORTTEXT";
    			ap = "'";
    			object = ((String) object).replaceAll("'", "''");
    		} else if (type.equals("long text")) {
    			tableExtension = "LONGTEXT";
    			ap = "'";
    			object = ((String) object).replaceAll("'", "''");
    		}
			
			int varid = -1;
			int dataId = -1;
			try {
				varid = (int) executeQueryForObject("SELECT ID FROM VARIABLES WHERE Variable = '" + key 
						+ "' AND StatementTypeId = " + statement.getStatementTypeId());
				dataId = (int) executeQueryForObject("SELECT ID FROM DATA" + tableExtension + " WHERE StatementId = " + statement.getId() 
						+ " AND VariableId = " + varid);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			String myStatement = "REPLACE INTO DATA" + tableExtension + " (ID, StatementId, VariableId, StatementTypeId, Value) "
					+ "VALUES (" + dataId + ", " + statement.getId() + ", " + varid + ", " + statement.getStatementTypeId() 
					+ ", "  + ap + object + ap + ")";
			executeStatement(myStatement);
    	}
	}

	public void removeStatement(int statementId) {
		executeStatement("DELETE FROM DATABOOLEAN WHERE StatementId = " + statementId);
		executeStatement("DELETE FROM DATAINTEGER WHERE StatementId = " + statementId);
		executeStatement("DELETE FROM DATASHORTTEXT WHERE StatementId = " + statementId);
		executeStatement("DELETE FROM DATALONGTEXT WHERE StatementId = " + statementId);
		executeStatement("DELETE FROM STATEMENTS WHERE ID = " + statementId);
	}
	
	/**
	 * @return     Array list of all statement links in the SQL database.
	 */
	private ArrayList<StatementLink> getAllStatementLinks() {
		ArrayList<StatementLink> al = new ArrayList<StatementLink>();
		try {
			String myQuery = "SELECT * FROM STATEMENTLINKS";
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				do {
					StatementLink statementLink = new StatementLink(
							result.getInt("ID"), result.getInt("SourceId"), result.getInt("TargetId"));
					al.add(statementLink);
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return al;
	}

	/**
	 * @param sl   StatementLink to add to/update in the STATEMENTLINKS table
	 */
	public void upsertStatementLink(StatementLink sl) {
		executeStatement("REPLACE INTO STATEMENTLINKS(ID, SourceId, TargetId) "
				+ "VALUES (" + sl.getId() + ", " + sl.getSourceId() + ", " + sl.getTargetId() + ")");
	}

	
	/* =================================================================================================================
	 * Functions related to statement types
	 * =================================================================================================================
	 */

	/**
	 * @return     Array list of all statement types in the SQL database.
	 */
	private ArrayList<StatementType> getAllStatementTypes() {
		ArrayList<StatementType> al = new ArrayList<StatementType>();
		try {
			String myQuery = "SELECT * FROM STATEMENTTYPES";
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				do {
					int id = result.getInt("id");
					String label = result.getString("Label");
					Color color = new Color(result.getInt("red"), result.getInt("green"), result.getInt("blue"));
					LinkedHashMap<String, String> variables = new LinkedHashMap<String, String>();
					
					String myQuery2 = "SELECT * FROM VARIABLES WHERE StatementTypeId = " + id;
					PreparedStatement preStatement2 = (PreparedStatement) connection.prepareStatement(myQuery2);
					ResultSet result2 = preStatement2.executeQuery();
					if (result2.next()) {
						do {
							variables.put(result2.getString("Variable"), result2.getString("DataType"));
						} while (result2.next());
					}
					result2.close();
					preStatement2.close();
					StatementType statementType = new StatementType(id, label, color, variables);
					al.add(statementType);
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return al;
	}

	public void upsertStatementType(StatementType statementType) {
		executeStatement("REPLACE INTO STATEMENTTYPES(ID, Label, Red, Green, Blue) "
				+ "VALUES (" + statementType.getId() + ", '" + statementType.getLabel() + "', " 
				+ statementType.getColor().getRed() + ", " + statementType.getColor().getGreen() + ", " 
				+ statementType.getColor().getBlue() + ")");
		Iterator<String> keyIterator = statementType.getVariables().keySet().iterator();
        while (keyIterator.hasNext()){
    		String key = keyIterator.next();
    		String value = statementType.getVariables().get(key);
    		String query = "SELECT ID FROM VARIABLES WHERE Variable = '" + key + "' AND StatementTypeId = " + statementType.getId();
    		int variableId = -1;
    		try {
        		PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(query);
        		ResultSet result;
    			result = preStatement.executeQuery();
				if (result.next()) {
					do {
						variableId = result.getInt(1);
					} while (result.next());
				}
	    		result.close();
	    		preStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    		
    		if (variableId == -1) {
    			executeStatement("INSERT INTO VARIABLES(Variable, DataType, StatementTypeId) VALUES ('"
        				+ key + "', '" + value + "', " + statementType.getId() + ")");
    		} else {
    			executeStatement("REPLACE INTO VARIABLES(ID, Variable, DataType, StatementTypeId) VALUES (" + variableId + ", '" 
        				+ key + "', '" + value + "', " + statementType.getId() + ")");
    		}
    		
    	}
	}
	
	/**
	 * Remove a statement type including all variables, attributes, and statements.
	 * 
	 * @param statementTypeId  ID of the statement type to be removed.
	 */
	public void removeStatementType(int statementTypeId) {
		executeStatement("DELETE FROM ATTRIBUTES WHERE VariableId IN (SELECT ID FROM VARIABLES WHERE StatementTypeId = " + statementTypeId + ")");
		executeStatement("DELETE FROM DATASHORTTEXT WHERE StatementTypeId = " + statementTypeId + ")");
		executeStatement("DELETE FROM DATALONGTEXT WHERE StatementTypeId = " + statementTypeId + ")");
		executeStatement("DELETE FROM DATAINTEGER WHERE StatementTypeId = " + statementTypeId + ")");
		executeStatement("DELETE FROM DATABOOLEAN WHERE StatementTypeId = " + statementTypeId + ")");
		executeStatement("DELETE FROM STATEMENTS WHERE StatementTypeId = " + statementTypeId + ")");
		executeStatement("DELETE FROM VARIABLES WHERE StatementTypeId = " + statementTypeId + ")");
		executeStatement("DELETE FROM STATEMENTTYPES WHERE ID = " + statementTypeId + ")");
	}

	/**
	 * Set a new label for an existing statement type in the SQL database
	 * 
	 * @param statementTypeId  ID of the statement type to rename
	 * @param newLabel         New String label for the statement type
	 */
	public void renameStatementType(int statementTypeId, String newLabel) {
		executeStatement("UPDATE STATEMENTTYPES SET Label = '" + newLabel + "' WHERE ID = " + statementTypeId);
	}

	/**
	 * Set a new color for an existing statement type in the SQL database
	 * 
	 * @param statementTypeId  ID of the statement type to rename
	 * @param red              Red color, from 0 to 255
	 * @param green            Green color, from 0 to 255
	 * @param blue             Blue color, from 0 to 255
	 */
	public void colorStatementType(int statementTypeId, int red, int green, int blue) {
		executeStatement("UPDATE STATEMENTTYPES SET Red = " + red + ", Green = " + green + ", Blue = " + blue + " WHERE ID = " + statementTypeId);
	}

	
	/* =================================================================================================================
	 * Functions related to variables
	 * =================================================================================================================
	 */

	/**
	 * Add a variable to the Variables table. Note that attributes and statements are not updated automatically. 
	 * This needs to be done separately.
	 * 
	 * @param variable         Variable name as a string.
	 * @param dataType         Data type as a string ("short text", "long text", "boolean", or "integer").
	 * @param statementTypeId  Statement type ID as an integer.
	 */
	public void addVariable(String variable, String dataType, int statementTypeId) {
		executeStatement("INSERT INTO VARIABLES (Variable, DataType, StatementTypeId) VALUES ('" + variable 
				+ "', '" + dataType + "', " + statementTypeId + ")");
	}

	/**
	 * Update or insert the content of one variable in one statement in the SQL database.
	 * 
	 * @param value            The value to be inserted or updated.
	 * @param statementId      The ID of the statement.
	 * @param variableName     The name of the variable.
	 * @param statementTypeId  The statement type ID.
	 * @param dataType         The data type. Valid values are "short text", "long text", "boolean", or "integer".
	 * @throws Exception
	 */
	public void upsertVariableContent(Object value, int statementId, String variableName, int statementTypeId, String dataType) throws Exception {
		String table = "";
		String ap = "";
		if (dataType.equals("integer")) {
			table = "DATAINTEGER";
		} else if (dataType.equals("boolean")) {
			table = "DATABOOLEAN";
		} else if (dataType.equals("short text")) {
			table = "DATASHORTTEXT";
			ap = "'";
			value = ((String) value).replaceAll("'", "''");
		} else if (dataType.equals("long text")) {
			table = "DATALONGTEXT";
			ap = "'";
			value = ((String) value).replaceAll("'", "''");
		}
		
		// get ID of data entry to update
		int dataId = -1;
		try {
			dataId = (int) executeQueryForObject("SELECT ID FROM " + table + " WHERE VariableId = " 
					+ "(SELECT ID FROM VARIABLES WHERE StatementTypeId = " + statementTypeId + " AND Variable = '" 
					+ variableName + "') AND StatementId = " + statementId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// then replace entry
		String myStatement = "";
		if (dataId == -1) {
			myStatement = "INSERT INTO " + table + "(StatementId, VariableId, StatementTypeId, Value) "
					+ "VALUES (" + statementId + ", (SELECT ID FROM VARIABLES WHERE StatementTypeId = " 
					+ statementTypeId + " AND Variable = '" + variableName + "')" + ", " + statementTypeId 
					+ ", " + ap + value + ap + ")";
		} else {
			myStatement = "REPLACE INTO " + table + "(ID, StatementId, VariableId, StatementTypeId, Value) "
					+ "VALUES (" + dataId + ", " + statementId + ", (SELECT ID FROM VARIABLES WHERE StatementTypeId = " 
					+ statementTypeId + " AND Variable = '" + variableName + "')" + ", " + statementTypeId 
					+ ", " + ap + value + ap + ")";
		}
		executeStatement(myStatement);
	}
	
	/**
	 * Remove a variable from a statement type, including the respective statements and attributes
	 * 
	 * @param statementTypeId  The ID of the statement type in which the variable is defined 
	 * @param variable         The name of the variable as a String
	 * @throws Exception
	 */
	public void removeVariable(int statementTypeId, String variable) throws Exception {
		int varid = -1;
		String dataType = "";
		try {
			varid = (int) executeQueryForObject("SELECT ID FROM VARIABLES WHERE Variable = '" + variable + "' AND StatementTypeId = " + statementTypeId);
			dataType = (String) executeQueryForObject("SELECT DataType FROM VARIABLES WHERE ID = " + varid);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String table;
		if (dataType.equals("short text")) {
			table = "DATASHORTTEXT";
		} else if (dataType.equals("long text")) {
			table = "DATALONGTEXT";
		} else if (dataType.equals("integer")) {
			table = "DATAINTEGER";
		} else if (dataType.equals("boolean")) {
			table = "DATABOOLEAN";
		} else {
			throw new Exception("Data type invalid.");
		}
		executeStatement("DELETE FROM ATTRIBUTES WHERE VariableId = " + varid);
		executeStatement("DELETE FROM " + table + " WHERE VariableId = " + varid);
		executeStatement("DELETE FROM VARIABLES WHERE ID = " + varid);
	}

	/**
	 * Rename a variable in the SQL database.
	 * 
	 * @param statementTypeId  ID of the statement type in which the variable is defined.
	 * @param oldVariable      Name of the variable to be renamed.
	 * @param newVariable      New name for the variable.
	 */
	public void renameVariable(int statementTypeId, String oldVariable, String newVariable) {
		executeStatement("UPDATE VARIABLES Set Variable = '" + newVariable + "' WHERE StatementTypeId = " + statementTypeId 
				+ " AND Variable = '" + oldVariable + "'");
	}
	
	/**
	 * Change the data type of a variable from short text to long text, from long text to short text, from integer to 
	 * boolean, or from boolean to integer. Automatically determine the source and target data type. Move data around 
	 * between SQL tables as necessary.
	 * 
	 * @param statementTypeId  ID of the statement type in which the variable is defined.
	 * @param variable         Name of the variable to be recoded.
	 * @throws Exception
	 */
	public void recastVariable(int statementTypeId, String variable) throws Exception {
		
		// identify current data type to determine how to recast
		String dataType = (String) executeQueryForObject("SELECT DataType FROM VARIABLES WHERE StatementTypeId = " + statementTypeId 
				+ " AND Variable = '" + variable + "'");
		
		String sourceTable, targetTable, newDataType;
		if (dataType.equals("short text")) {
			sourceTable = "DATASHORTTEXT";
			targetTable = "DATALONGTEXT";
			newDataType = "long text";
		} else if (dataType.equals("long text")) {
			sourceTable = "DATALONGTEXT";
			targetTable = "DATASHORTTEXT";
			newDataType = "short text";
		} else if (dataType.equals("boolean")) {
			sourceTable = "DATABOOLEAN";
			targetTable = "DATAINTEGER";
			newDataType = "integer";
		} else if (dataType.equals("integer")) {
			sourceTable = "DATAINTEGER";
			targetTable = "DATABOOLEAN";
			newDataType = "boolean";
		} else {
			throw new Exception("Data type in database for variable '" + variable + "' was not recognized.");
		}
		
		// copy data from sourceTable to targetTable (and look up Variable ID)
		String string = "INSERT INTO " + targetTable + " (StatementID, VariableID, StatementTypeId, Value) "
				+ "SELECT StatementId, VariableId, StatementTypeId, Value FROM " + sourceTable + " WHERE "
				+ "StatementTypeId = " + statementTypeId + " AND VariableID = (SELECT ID FROM VARIABLES WHERE StatementTypeId = " 
				+ statementTypeId + " AND Variable = '" + variable + "');";
		executeStatement(string);

		// delete in sourceTable
		string = " DELETE FROM " + sourceTable + " WHERE StatementTypeId = " + statementTypeId 
				+ " AND VariableId = (SELECT ID FROM VARIABLES WHERE StatementTypeId = " + statementTypeId + " AND Variable = '" + variable + "');";
		executeStatement(string);

		// change variable data type in VARIABLES
		string = " UPDATE VARIABLES SET DataType = '" + newDataType + "' WHERE StatementTypeId = " + statementTypeId 
				+ " AND Variable = '" + variable + "';";
		executeStatement(string);
	}

	
	/* =================================================================================================================
	 * Functions related to attributes
	 * =================================================================================================================
	 */

	/**
	 * Read all meta-variables/attributes from SQL database and return them as an array list of attribute vectors.
	 * 
	 * @return array list of attribute vectors, containing attributes for the statement values
	 */
	private ArrayList<AttributeVector> getAllAttributes() {
		ArrayList<AttributeVector> al = new ArrayList<AttributeVector>();
		try {
			String myQuery = "SELECT ATTRIBUTES.*, VARIABLES.StatementTypeId, VARIABLES.Variable FROM ATTRIBUTES LEFT JOIN VARIABLES ON ATTRIBUTES.VariableId = VARIABLES.ID";
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			int id;
			String value;
			Color color;
			String type;
			String alias;
			String notes;
			String childOf;
			int statementTypeId;
			String variable;
			AttributeVector av;
			if (result.next()) {
				do {
					id = result.getInt("ID");
					value = result.getString("Value");
					color = new Color(result.getInt("Red"), result.getInt("Green"), result.getInt("Blue"));
					type = result.getString("Type");
					alias = result.getString("Alias");
					notes = result.getString("Notes");
					childOf = result.getString("childOf");
					statementTypeId = result.getInt("StatementTypeId");
					variable = result.getString("Variable");
					av = new AttributeVector(id, value, color, type, alias, notes, childOf, statementTypeId, variable);
					al.add(av);
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return al;
	}
	
	/**
	 * Add multiple new attribute vectors to the ATTRIBUTES table of the database.
	 * 
	 * @param al An array list of AttributeVector objects to be inserted into the ATTRIBUTES table
	 */
	public void insertAttributeVectors(ArrayList<AttributeVector> al) {
		if (al.size() > 0) {
			String query = "INSERT INTO ATTRIBUTES(ID, VariableId, Value, Red, Green, Blue, Type, Alias, Notes, ChildOf) VALUES ";
			for (int i = 0; i < al.size(); i++) {
				query = query + "(" + al.get(i).getId() + ", (SELECT ID FROM VARIABLES WHERE StatementTypeId = " + al.get(i).getStatementTypeId() 
						+ " AND Variable = '" + al.get(i).getVariable() + "'), '" + al.get(i).getValue().replaceAll("'", "''") + "', " 
						+ al.get(i).getColor().getRed() + ", " + al.get(i).getColor().getGreen() + ", " + al.get(i).getColor().getBlue() + ", '" 
						+ al.get(i).getType().replaceAll("'", "''") + "', '" + al.get(i).getAlias().replaceAll("'", "''") + "', '" 
						+ al.get(i).getNotes().replaceAll("'", "''") + "', '" + al.get(i).getChildOf().replaceAll("'", "''") + "')";
				if (i < al.size() - 1) {
					query = query + ", ";
				}
			}
			executeStatement(query);
		}
	}
	
	/**
	 * Update an existing attribute vector or insert a new item into the ATTRIBUTES table in the database. 
	 * 
	 * @param av AttributeVector to be updated in the SQL database
	 */
	public void upsertAttributeVector(AttributeVector av) {
		try {
			int statementTypeId = av.getStatementTypeId();
			String variable = av.getVariable();
			int variableId = (int) executeQueryForObject("SELECT ID FROM VARIABLES WHERE StatementTypeID = " + statementTypeId + 
					" AND Variable = '" + variable + "'");

			executeStatement("REPLACE INTO ATTRIBUTES(ID, VariableId, Value, Red, Green, Blue, Type, Alias, Notes, ChildOf) "
					+ "VALUES (" + av.getId() + ", " + variableId + ", '" + av.getValue().replaceAll("'", "''") + "', " 
					+ av.getColor().getRed() + ", " + av.getColor().getGreen() + ", " + av.getColor().getBlue()	+ ", '" 
					+ av.getType().replaceAll("'", "''") + "', '" + av.getAlias().replaceAll("'", "''") + "', '" 
					+ av.getNotes().replaceAll("'", "''") + "', '" + av.getChildOf().replaceAll("'", "''") + "')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update some String attribute in an attribute vector.
	 * 
	 * @param id        ID of the attribute vector
	 * @param attribute Attribute string; which variable should be updated?
	 * @param newValue  New value of the attribute
	 */
	public void updateAttribute(int id, String attribute, String newValue) {
		executeStatement("UPDATE ATTRIBUTES SET " + attribute + " = '" + newValue.replaceAll("'", "''") + "' WHERE ID = " + id);
	}

	/**
	 * Delete an existing attribute vector from the database using its ID.
	 * 
	 * @param attributeVectorId  The ID of the attribute vector
	 */
	public void deleteAttributeVector(int attributeVectorId) {
		executeStatement("DELETE FROM ATTRIBUTES WHERE ID = " + attributeVectorId);
	}
	
	/**
	 * Update color in an attribute vector.
	 * 
	 * @param id     ID of the attribute vector
	 * @param color  New color of the attribute vector
	 */
	public void updateAttributeColor(int id, Color color) {
		executeStatement("UPDATE ATTRIBUTES SET Red = " + color.getRed() + ", Green = " + color.getGreen() + ", Blue = " + color.getBlue() + " WHERE ID = " + id);
	}

	/**
	 * Update color in an attribute vector.
	 * 
	 * @param id     ID of the attribute vector
	 * @param color  New color of the attribute vector
	 */
	public void updateAttributeColor(int id, String color) {
		Color c = new Color(Integer.parseInt(color.substring(1), 16));
		executeStatement("UPDATE ATTRIBUTES SET Red = " + c.getRed() + ", Green = " + c.getGreen() + ", Blue = " + c.getBlue() + " WHERE ID = " + id);
	}

	
	/* =================================================================================================================
	 * Functions related to coders
	 * =================================================================================================================
	 */

	/**
	 * @param key    Property to extract
	 * @return       Value corresponding to the property
	 */
	public ArrayList<Coder> getAllCoders() {
		ArrayList<Coder> al = new ArrayList<Coder>();
		try {
			String myQuery = "SELECT * FROM CODERS";
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				do {
					int id = result.getInt("ID");
					String name = result.getString("Name");
					int red = result.getInt("Red");
					int green = result.getInt("Green");
					int blue = result.getInt("Blue");
					String password = result.getString("Password");
					HashMap<String, Boolean> map = new HashMap<String, Boolean>();
					
					String myQuery2 = "SELECT * FROM CODERPERMISSIONS WHERE Coder = " + id;
					PreparedStatement preStatement2 = (PreparedStatement) connection.prepareStatement(myQuery2);
					ResultSet result2 = preStatement2.executeQuery();
					if (result2.next()) {
						do {
							int perm = result2.getInt("Permission");
							boolean b;
							if (perm == 0) {
								b = false;
							} else {
								b = true;
							}
							String type = result2.getString("Type");
							map.put(type, b);
						} while (result2.next());
					}
					result2.close();
					preStatement2.close();
					
					Coder coder = new Coder(id, name, new Color(red, green, blue), password, map);
					al.add(coder);
					Collections.sort(al);
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return al;
	}

	/**
	 * @param key    Property to extract
	 * @return       Value corresponding to the property
	 */
	public Coder getCoder(int id) {
		Coder coder = null;
		try {
			String myQuery = "SELECT * FROM CODERS WHERE ID = " + id;
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				do {
					String name = result.getString("Name");
					int red = result.getInt("Red");
					int green = result.getInt("Green");
					int blue = result.getInt("Blue");
					String password = result.getString("Password");
					coder = new Coder(id, name, new Color(red, green, blue), password, new HashMap<String, Boolean>());
				} while (result.next());
			}
			result.close();
			preStatement.close();
			
			myQuery = "SELECT * FROM CODERPERMISSIONS WHERE Coder = " + id;
			PreparedStatement preStatement2 = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result2 = preStatement2.executeQuery();
			HashMap<String, Boolean> permissions = new HashMap<String, Boolean>();
			if (result2.next()) {
				do {
					int perm = result.getInt("Permission");
					boolean b;
					if (perm == 0) {
						b = false;
					} else {
						b = true;
					}
					String type = result.getString("Type");
					permissions.put(type, b);
				} while (result.next());
			}
			coder.setPermissions(permissions);
			result2.close();
			preStatement2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return coder;
	}
	
	public void addCoder(Coder coder) {
		// insert the coder
		String coderStatement = "INSERT INTO CODERS(ID, Name, Red, Green, Blue, Password) VALUES(" + coder.getId() + ", '" 
				+ coder.getName() + "', " + coder.getColor().getRed() + ", " + coder.getColor().getGreen() + ", " 
				+ coder.getColor().getBlue() + ", '')";
		executeStatement(coderStatement);
		
		// insert the permissions of the coder
		Iterator<String> keyIterator = coder.getPermissions().keySet().iterator();
        while (keyIterator.hasNext()){
    		String key = keyIterator.next();
    		boolean perm = coder.getPermissions().get(key);
    		int permInt = 0;
    		if (perm == true) {
    			permInt = 1;
    		}
    		String statement = "INSERT INTO CODERPERMISSIONS(Coder, Type, Permission) VALUES("
    				+ coder.getId() + ", '" + key + "', " + permInt + ")";
    		executeStatement(statement);
        }
        
        // insert coder relations
        ArrayList<Object> coderIds = executeQueryForList("SELECT ID FROM CODERS");
        for (int i = 0; i < coderIds.size(); i++) {
        	int id = (int) coderIds.get(i);
        	if (id != coder.getId()) {
        		String statement = "INSERT INTO CODERRELATIONS(Coder, OtherCoder, ViewStatements, EditStatements, "
        				+ "ViewDocuments, EditDocuments) VALUES(" + id + ", " + coder.getId() + ", 1, 1, 1, 1)";
        		executeStatement(statement);
        		statement = "INSERT INTO CODERRELATIONS(Coder, OtherCoder, ViewStatements, EditStatements, "
        				+ "ViewDocuments, EditDocuments) VALUES(" + coder.getId() + ", " + id + ", 1, 1, 1, 1)";
        		executeStatement(statement);
        	}
        }
	}

	/**
	 * @param coder     The coder to add to/update in the Coders table
	 */
	public void upsertCoder(Coder coder) {
		int id = coder.getId();
		String name = coder.getName();
		int red = coder.getColor().getRed();
		int green = coder.getColor().getGreen();
		int blue = coder.getColor().getBlue();
		String password = coder.getPassword();
		HashMap<String, Boolean> permissions = coder.getPermissions();
		
		if (dbtype.equals("sqlite")) {
			executeStatement("INSERT OR REPLACE INTO CODERS (ID, Name, Red, Green, Blue, Password) "
					+ "VALUES (" + id + ", '" + name + "', " + red + ", " + green + ", " + blue + ", '" + password + "')");
		} else if (dbtype.equals("mysql")) {
			executeStatement("INSERT INTO CODERS (ID, Name, Red, Green, Blue, Password) "
					+ "VALUES(" + id + ", '" + name + "', " + red + ", " + green + ", " + blue + ", '" + password + "') "
					+ "ON DUPLICATE KEY UPDATE Name = '" + name + "', red = " + red + ", green = " + green + ", blue = "
					+ blue + ", Password = '" + password + "'");
		}
		
		Iterator<String> keyIterator = permissions.keySet().iterator();
        while (keyIterator.hasNext()){
    		String key = keyIterator.next();
    		Boolean value = permissions.get(key);
    		int intValue;
    		if (value == true) {
    			intValue = 1;
    		} else {
    			intValue = 0;
    		}
    		int permissionId;
			try {
				permissionId = (int) executeQueryForObject("SELECT ID from CODERPERMISSIONS WHERE Coder = " + id + " AND Type = '" + key + "'");
				executeStatement("REPLACE INTO CODERPERMISSIONS(ID, Coder, Type, Permission) "
    					+ "VALUES (" + permissionId + ", " + id + ", '" + key + "', " + intValue + ")");
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
        
        if (permissions.get("viewOthersStatements") == false) {
        	executeStatement("UPDATE CODERRELATIONS SET ViewStatements = 0 WHERE Coder = " + id);
        } else {
        	executeStatement("UPDATE CODERRELATIONS SET ViewStatements = 1 WHERE Coder = " + id);
        }
        if (permissions.get("editOthersStatements") == false) {
        	executeStatement("UPDATE CODERRELATIONS SET EditStatements = 0 WHERE Coder = " + id);
        } else {
        	executeStatement("UPDATE CODERRELATIONS SET EditStatements = 1 WHERE Coder = " + id);
        }
        if (permissions.get("viewOthersDocuments") == false) {
        	executeStatement("UPDATE CODERRELATIONS SET ViewDocuments = 0 WHERE Coder = " + id);
        } else {
        	executeStatement("UPDATE CODERRELATIONS SET ViewDocuments = 1 WHERE Coder = " + id);
        }
        if (permissions.get("editOthersDocuments") == false) {
        	executeStatement("UPDATE CODERRELATIONS SET EditDocuments = 0 WHERE Coder = " + id);
        } else {
        	executeStatement("UPDATE CODERRELATIONS SET EditDocuments = 1 WHERE Coder = " + id);
        }
	}
	
	public void removeCoder(int id) {
		executeStatement("DELETE FROM DATABOOLEAN WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE Coder = " + id + ")");
		executeStatement("DELETE FROM DATAINTEGER WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE Coder = " + id + ")");
		executeStatement("DELETE FROM DATASHORTTEXT WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE Coder = " + id + ")");
		executeStatement("DELETE FROM DATALONGTEXT WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE Coder = " + id + ")");
		executeStatement("DELETE FROM STATEMENTS WHERE Coder = " + id);
		executeStatement("DELETE FROM DOCUMENTS WHERE Coder = " + id);
		executeStatement("DELETE FROM CODERRELATIONS WHERE OtherCoder = " + id + " OR Coder = " + id);
		executeStatement("DELETE FROM CODERPERMISSIONS WHERE Coder = " + id);
		executeStatement("DELETE FROM CODERS WHERE ID = " + id);
	}

	/**
	 * @return     Array list of all coder relations in the SQL database.
	 */
	private ArrayList<CoderRelation> getAllCoderRelations() {
		ArrayList<CoderRelation> al = new ArrayList<CoderRelation>();
		try {
			String myQuery = "SELECT * FROM CODERRELATIONS";
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				do {
					int viewStatementsInt = result.getInt("viewStatements");
					int editStatementsInt = result.getInt("editStatements");
					int viewDocumentsInt = result.getInt("viewDocuments");
					int editDocumentsInt = result.getInt("editDocuments");
					boolean viewStatements, editStatements, viewDocuments, editDocuments;
					if (viewStatementsInt == 1) {
						viewStatements = true;
					} else {
						viewStatements = false;
					}
					if (editStatementsInt == 1) {
						editStatements = true;
					} else {
						editStatements = false;
					}
					if (viewDocumentsInt == 1) {
						viewDocuments = true;
					} else {
						viewDocuments = false;
					}
					if (editDocumentsInt == 1) {
						editDocuments = true;
					} else {
						editDocuments = false;
					}
					CoderRelation coderRelation = new CoderRelation(result.getInt("ID"), result.getInt("Coder"), 
							result.getInt("OtherCoder"), viewStatements, editStatements, viewDocuments, editDocuments);
					al.add(coderRelation);
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return al;
	}

	public void updateCoderRelationViewStatements(int id, boolean viewStatements) {
		String booleanString = "0";
		if (viewStatements == true) {
			booleanString = "1";
		}
		executeStatement("UPDATE CODERRELATIONS SET ViewStatements = " + booleanString + " WHERE ID = " + id);
	}

	public void updateCoderRelationEditStatements(int id, boolean editStatements) {
		String booleanString = "0";
		if (editStatements == true) {
			booleanString = "1";
		}
		executeStatement("UPDATE CODERRELATIONS SET EditStatements = " + booleanString + " WHERE ID = " + id);
	}

	public void updateCoderRelationViewDocuments(int id, boolean viewDocuments) {
		String booleanString = "0";
		if (viewDocuments == true) {
			booleanString = "1";
		}
		executeStatement("UPDATE CODERRELATIONS SET ViewDocuments = " + booleanString + " WHERE ID = " + id);
	}

	public void updateCoderRelationEditDocuments(int id, boolean editDocuments) {
		String booleanString = "0";
		if (editDocuments == true) {
			booleanString = "1";
		}
		executeStatement("UPDATE CODERRELATIONS SET EditDocuments = " + booleanString + " WHERE ID = " + id);
	}

	
	/* =================================================================================================================
	 * Functions related to settings
	 * =================================================================================================================
	 */

	/**
	 * @return     Array list of all settings in the SQL database.
	 */
	private HashMap<String, String> getAllSettings() {
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			String myQuery = "SELECT * FROM SETTINGS";
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				do {
					map.put(result.getString("Property"), result.getString("Value"));
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * @param key    Property to extract
	 * @return       Value corresponding to the property
	 */
	public String getSetting(String key) {
		String value = "";
		try {
			value = (String) executeQueryForObject("SELECT Value FROM SETTINGS WHERE Property = '" + key + "'");
		} catch (SQLException e) {
			e.printStackTrace();
			return "";
		}
		return value;
	}

	/**
	 * @param key    Property to set
	 * @param value  Value corresponding to the property
	 */
	public void upsertSetting(String key, String value) {
		if (dbtype.equals("sqlite")) {
			executeStatement("INSERT OR REPLACE INTO SETTINGS (Property, Value) VALUES ('" + key + "', '" + value + "')");
		} else if (dbtype.equals("mysql")) {
			executeStatement("INSERT INTO SETTINGS (Property, Value) VALUES('" + key + "', '" + value + "') "
					+ "ON DUPLICATE KEY UPDATE Value = '" + value + "'");
		}
	}

	
	/* =================================================================================================================
	 * Functions related to regexes
	 * =================================================================================================================
	 */

	/**
	 * @return     Array list of all regular expressions in the SQL database.
	 */
	private ArrayList<Regex> getAllRegexes() {
		ArrayList<Regex> al = new ArrayList<Regex>();
		try {
			String myQuery = "SELECT * FROM REGEXES";
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				do {
					Regex regex = new Regex(
							result.getString("Label"), 
							new Color(result.getInt("red"), result.getInt("green"), result.getInt("blue")));
					al.add(regex);
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return al;
	}

	/**
	 * @param regex   Regular expression to add to/update in the REGEXES table
	 */
	public void upsertRegex(Regex regex) {
		executeStatement("REPLACE INTO REGEXES(Label, Red, Green, Blue) "
				+ "VALUES ('" + regex.getLabel() + "', " + regex.getColor().getRed() + ", " + regex.getColor().getGreen()
				+ ", " + regex.getColor().getBlue() + ")");
	}

	public void removeRegex(String label) {
		executeStatement("DELETE FROM REGEXES WHERE Label = '" + label + "'");
	}
}