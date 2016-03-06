package dna.dataStructures;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


import static java.lang.Math.toIntExact;

public class SqlConnection {
	String dbtype;
	String dbfile;
	String login;
	String password;
	Connection connection = null;
	PreparedStatement preStatement = null;
	ResultSet result = null;
	
	public SqlConnection(String dbtype, String dbfile, String login, String password) {
		this.dbtype = dbtype;
		this.dbfile = dbfile;
		this.login = login;
		this.password = password;
		try {
			if (dbtype == "mysql") {
				Class.forName("com.mysql.jdbc.Driver");
				this.connection = DriverManager.getConnection("jdbc:mysql://" + dbfile, login, password);
			} else if (dbtype == "sqlite") {
				Class.forName("org.sqlite.JDBC");
				this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			}
		} catch (SQLException | ClassNotFoundException e) {
			//e.printStackTrace();
		}
	}
	
	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			//e.printStackTrace();
		} catch (NullPointerException e) {
			// no connection was established in the first place
		}
	}
	
	/**
	 * Tests whether a mySQL connection can be established and returns a status message.
	 * 
	 * @return   Status message
	 * @throws SQLException 
	 */
	public String testMySQLConnection() {
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
		int count = -1;
		int i = 0;
		while (count == -1) {
			try {
				count = toIntExact((long) executeQueryForObject("SELECT COUNT(*) FROM " + tableNames.get(i)));
			} catch (Exception e) {
				// if we end up here, the table does not exist yet in the database
			}
			i++;
			if (i == 8) {
				break;
			}
		}
		if (i == -1) {
			return("OK. Tables will be created.");
		} else {
			return("Warning: Existing tables will be erased!");
		}
	}
	
	public void createDataStructure() {
		if (dbtype.equals("sqlite")) {
			executeStatement("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property TEXT PRIMARY KEY, " 
					+ "Value TEXT)");
			
			executeStatement("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, " 
					+ "Title TEXT, "
					+ "Text TEXT, " 
					+ "Coder INTEGER, "
					+ "Source TEXT, " 
					+ "Section TEXT, " 
					+ "Notes TEXT, "
					+ "Type TEXT, "
					+ "Date INTEGER, "
					+ "FOREIGN KEY(Coder) REFERENCES CODER(ID))");

			executeStatement("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "Label TEXT PRIMARY KEY, " 
					+ "Red INTEGER, "
					+ "Green INTEGER, " 
					+ "Blue INTEGER)");
			
			executeStatement("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID INTEGER NOT NULL PRIMARY KEY, " 
					+ "Variable TEXT, "
					+ "DataType TEXT, "
					+ "StatementType TEXT, "
					+ "FOREIGN KEY(StatementType) REFERENCES STATEMENTTYPE(Label))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label TEXT PRIMARY KEY, " 
					+ "Red INTEGER, "
					+ "Green INTEGER, " 
					+ "Blue INTEGER)");
			
			executeStatement("CREATE TABLE IF NOT EXISTS CODER("
					+ "ID INTEGER NOT NULL PRIMARY KEY, " 
					+ "Name TEXT, "
					+ "Red INTEGER, "
					+ "Green INTEGER, "
					+ "Blue INTEGER, "
					+ "Password TEXT, "
					+ "AddDocuments INTEGER, "
					+ "ViewOtherDocuments INTEGER, "
					+ "EditOtherDocuments INTEGER, "
					+ "AddStatements INTEGER, "
					+ "ViewOtherStatements INTEGER, "
					+ "EditOtherStatements INTEGER, "
					+ "EditCoders INTEGER, "
					+ "EditStatementTypes INTEGER, "
					+ "EditRegex INTEGER)");
			
			executeStatement("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, " 
					+ "Coder INTEGER, "
					+ "OtherCoder INTEGER, "
					+ "Type TEXT, "
					+ "Permission INTEGER, "
					+ "FOREIGN KEY(Coder) REFERENCES CODER(ID), "
					+ "FOREIGN KEY(OtherCoder) REFERENCES CODER(ID))");
			
			executeStatement("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, " 
					+ "Type TEXT, "
					+ "Document INTEGER, " 
					+ "Start INTEGER, " 
					+ "Stop INTEGER, "
					+ "Coder INTEGER, "
					+ "FOREIGN KEY(Type) REFERENCES STATEMENTTYPE(Label), "
					+ "FOREIGN KEY(Coder) REFERENCES CODER(ID), "
					+ "FOREIGN KEY(Document) REFERENCES DOCUMENTS(ID))");
			
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
	        		+ "StatementType TEXT, "
	        		+ "Value INTEGER, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementType) REFERENCES STATEMENTTYPES(Label))");
	        
	        executeStatement("CREATE TABLE IF NOT EXISTS DATAINTEGER("
	        		+ "ID INTEGER PRIMARY KEY NOT NULL, "
	        		+ "StatementId INTEGER NOT NULL, "
	        		+ "VariableId INTEGER NOT NULL, "
	        		+ "StatementType TEXT, "
	        		+ "Value INTEGER, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementType) REFERENCES STATEMENTTYPES(Label))");

	        executeStatement("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
	        		+ "ID INTEGER PRIMARY KEY NOT NULL, "
	        		+ "StatementId INTEGER NOT NULL, "
	        		+ "VariableId INTEGER NOT NULL, "
	        		+ "StatementType TEXT, "
	        		+ "Value TEXT, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementType) REFERENCES STATEMENTTYPES(Label))");

	        executeStatement("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
	        		+ "ID INTEGER PRIMARY KEY NOT NULL, "
	        		+ "StatementId INTEGER NOT NULL, "
	        		+ "VariableId INTEGER NOT NULL, "
	        		+ "StatementType TEXT, "
	        		+ "Value TEXT, "
	        		+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID), "
	        		+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID), "
	        		+ "FOREIGN KEY(StatementType) REFERENCES STATEMENTTYPES(Label))");
	        
	        // TODO: check if this data structure makes sense; fill in contents; add same for mysql; 
	        // TODO: bind DNA data structures to databases; update whenever document is left/closed;
	        // TODO: NewDataBaseDialog: summary panel needs to be finished;
	        // TODO: add coder management etc. to side panel or other parts of GUI.
		}
		
	}
	
	/**
	 * Execute a statement on the database.
	 * 
	 * @param myStatement     A string representation of the SQL statement.
	 */
	public void executeStatement(String myStatement) {
		try {
			//Statement statement = connection.createStatement();
			//statement.execute(myStatement);
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myStatement);
			preStatement.execute();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Execute a query on the database and get an object back.
	 * 
	 * @param myStatement     A string representation of the SQL statement.
	 * @return                The ID of the row that was changed.
	 */
	public Object executeQueryForObject(String myQuery) {
		Object object = null;
		try {
			PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
			ResultSet result = preStatement.executeQuery();
			if (result.next()) {
				do {
					object = result.getObject(1);
				} while (result.next());
			}
			result.close();
			preStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return object;
	}
}
