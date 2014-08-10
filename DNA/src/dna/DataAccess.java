package dna;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

//import com.microsoft.sqlserver.jdbc.SQLServerException;


public class DataAccess {
	boolean sqlite = true;
	String dbfile;
	String login = "";
	String password = "";

	/**
	 * Create a new DataAccess object, which manages database access.
	 * 
	 * @param sqlite  boolean indicating whether an SQLite file shall be used.
	 */
	public DataAccess(boolean sqlite) {
		//no file given; openFile() must be called first
		this.dbfile = null;
		this.sqlite = sqlite;
	}
	
	/**
	 * Create a new DataAccess object, which manages database access.
	 * 
	 * @param sqlite  true indicates an SQLite database, false a mySQL database.
	 * @param dbfile  File name or URL of the database.
	 */
	public DataAccess(boolean sqlite, String dbfile) {
		this.dbfile = dbfile;
		this.sqlite = sqlite;
	}
	
	/**
	 * Create a new DataAccess object, which manages database access.
	 * 
	 * @param sqlite    true indicates an SQLite database, false mySQL.
	 * @param dbfile    File name or URL of the database.
	 * @param login     User name of the database.
	 * @param password  Password for the database.
	 */
	public DataAccess(boolean sqlite, String url, String userName, 
			String password) {
		this.dbfile = url;
		this.sqlite = sqlite;
		this.login = userName;
		this.password = password;
	}
	
	/**
	 * Establish the connection to a .dna SQLite database file.
	 * 
	 * @return The connection to the database.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	Connection getSQLiteConnection() throws ClassNotFoundException, 
			SQLException {    
		Class.forName("org.sqlite.JDBC");
		Connection conn= DriverManager.getConnection("jdbc:sqlite:" + dbfile);
		return conn;
	}

	/**
	 * Establish the connection to a mySQL database on a server.
	 * 
	 * @return  The connection to the database.
	 * @throws  ClassNotFoundException
	 * @throws  SQLException
	 */
	Connection getMySQLConnection() throws ClassNotFoundException, 
			SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		//mysql://philipleifeld.de/phillei_db3
		//phillei_3_w
		//rzGbs6YuR3589SKP
		Connection conn = DriverManager.getConnection("jdbc:" + dbfile, login, 
				password);
		return conn;
	}
	
	/**
	 * Establish the connection to a mySQL database on a server.
	 * 
	 * @return  The connection to the database.
	 * @throws  ClassNotFoundException
	 * @throws  SQLException
	 */
	/*Connection getMySQLConnection() throws ClassNotFoundException, 
			SQLException {
		//Class.forName("com.mysql.jdbc.Driver");
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		//sqlserver://eaw-sql2.eawag.wroot.emp-eaw.ch
		//overlapuser
		//sedPZ53TG
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:" + dbfile + ";databaseName=Overlap;user=" + login + ";password=" + password + ";");
		} catch(SQLServerException e) {
			System.err.println("Error: connection to the database could not be established. Check your connection.");
		}
		return conn;
	}*/
	
	/**
	 * Forget the name of the current .dna file for data access.
	 */
	public void closeFile() {
		this.dbfile = null;
		this.sqlite = true;
		this.login = null;
		this.password = null;
	}
	
	/**
	 * Remember the name of a new .dna file for data access.
	 * 
	 * @param dnaFile  The file name of the new .dna file to be opened.
	 */
	public void openFile(String dnaFile) {
		this.dbfile = dnaFile;
		this.sqlite = true;
		this.login = null;
		this.password = null;
	}
	
	/**
	 * Remember the credentials of a mySQL database for data access.
	 * 
	 * @param sqlite    true indicates an SQLite database, false mySQL.
	 * @param dbfile    File name or URL of the database.
	 * @param login     User name of the database.
	 * @param password  Password for the database.
	 */
	public void openMySQL(boolean sqlite, String url, String userName, 
			String password) {
		this.dbfile = url;
		this.sqlite = sqlite;
		this.login = userName;
		this.password = password;
		
		ArrayList<?> al = executeQueryForList("SHOW TABLES");
		if (!al.contains("DOCUMENTS") || !al.contains("STATEMENTS")) {
			int dialog = JOptionPane.showConfirmDialog(
					Dna.dna.gui, 
					"The database does not contain the DNA data structure. " +
					"Create it?", "Confirmation required", 
					JOptionPane.OK_CANCEL_OPTION);
    		
    		if (dialog == 0) {
    			createTables();
    		}
		}
	}
	
	/**
	 * Return the file name of the database.
	 * 
	 * @return  File name of the database as a string.
	 */
	public String getFileName() {
		return(dbfile);
	}

	/**
	 * Execute a statement on the database.
	 * 
	 * @param myStatement  A string representation of the SQL statement.
	 */
	public void executeStatement(String myStatement) {
		Connection connection = null;
		Statement statement = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			statement.execute(myStatement);
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
	}
	
	/**
	 * Execute a statement on the database and get the ID of the row.
	 * 
	 * @param myStatement  A string representation of the SQL statement.
	 * @return             The ID of the row that was changed.
	 */
	public int executeStatementForId(String myStatement) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		int id = -1;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			statement.execute(myStatement);
			if (sqlite == true) {
				resultSet = statement.executeQuery(
						"SELECT last_insert_rowid()"
						);
			} else {
				resultSet = statement.executeQuery(
						"SELECT LAST_INSERT_ID()"
						);
			}
			if (resultSet.next()) {
				do {
					id = resultSet.getInt(1);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		if (id == -1) {
			System.err.println("ID could not be retrieved.");
		}
		return(id);
	}
	
	/**
	 * Execute a query on the database and return an array list.
	 * 
	 * @param myQuery  A string representation of an SQL query.
	 * @return         An array list with objects.
	 */
	public ArrayList<Object> executeQueryForList(String myQuery) {
		ArrayList<Object> al = new ArrayList<Object>();
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery(myQuery);
			if (resultSet.next()) {
				do {
					al.add(resultSet.getObject(1));
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return(al);
	}
	
	/**
	 * Execute a query on the database and return a string.
	 * 
	 * @param myQuery  A string representation of an SQL query.
	 * @return         A string with the result of the query.
	 */
	public String executeQueryForString(String myQuery) {
		String s = null;
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery(myQuery);
			if (resultSet.next()) {
				do {
					s = resultSet.getString(1);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return(s);
	}

	/**
	 * Execute a query on the database and return a string.
	 * 
	 * @param myQuery  A string representation of an SQL query.
	 * @return         A string with the result of the query.
	 */
	public int executeQueryForInt(String myQuery) {
		int i = -1;
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery(myQuery);
			if (resultSet.next()) {
				do {
					i = resultSet.getInt(1);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return(i);
	}
	
	/**
	 * Create the table structure in a new database file.
	 */
	public void createTables() {
		
		if (sqlite == true) {  // sqlite tables
			executeStatement(
					"CREATE TABLE IF NOT EXISTS SETTINGS(" + 
	    	        "Property TEXT PRIMARY KEY, " + 
	    	        "Value TEXT)"
	    	        );
			
			executeStatement(
					"CREATE TABLE IF NOT EXISTS DOCUMENTS(" + 
	        		"ID INTEGER NOT NULL PRIMARY KEY, " + 
	        		"Title TEXT, " + 
	        		"Text TEXT, " + 
	        		"Date INTEGER, " + 
	        		"Coder TEXT, " + 
	        		"Source TEXT, " + 
	        		"Section TEXT, " + 
	        		"Notes TEXT, " + 
	        		"Type TEXT)"
	        		);
			
			executeStatement(
					"CREATE TABLE IF NOT EXISTS REGEX(" + 
	                "Label TEXT PRIMARY KEY, " + 
	                "Red INTEGER, " +  
	                "Green INTEGER, " + 
	                "Blue INTEGER)"
	                );
			
			executeStatement(
					"CREATE TABLE IF NOT EXISTS STATEMENTTYPE(" + 
	    			"Label TEXT PRIMARY KEY, " +
	    			"Red INTEGER, " +
	    			"Green INTEGER, " +
	    			"Blue INTEGER)"
	    			);
			
			executeStatement(
					"CREATE TABLE IF NOT EXISTS VARIABLES(" + 
	    			"ID INTEGER NOT NULL PRIMARY KEY, " + 
	    	    	"Variable TEXT, " + 
	    	    	"DataType TEXT, " +
	    	    	"StatementType TEXT, " +
	    	    	"FOREIGN KEY(StatementType) REFERENCES STATEMENTTYPE(Label))"
	    	    	);
			
			executeStatement(
					"CREATE TABLE IF NOT EXISTS STATEMENTS(" +
	    			"ID INTEGER NOT NULL PRIMARY KEY, " +
	    			"Type TEXT, " +
	        		"Document INTEGER, " + 
	        		"Start INTEGER, " + 
	        		"Stop INTEGER, " + 
	        		"FOREIGN KEY(Type) REFERENCES STATEMENTTYPE(Label), " + 
	        		"FOREIGN KEY(Document) REFERENCES DOCUMENTS(ID))"
	        		);
		} else {  // mysql tables
			executeStatement(
					"CREATE TABLE IF NOT EXISTS SETTINGS(" + 
	    	        "Property VARCHAR(200), " + 
	    	        "Value VARCHAR(200)," +
	    	        "PRIMARY KEY (Property))"
	    	        );

			executeStatement(
					"CREATE TABLE IF NOT EXISTS DOCUMENTS(" + 
	        		"ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, " + 
	        		"Title VARCHAR(200), " + 
	        		"Text MEDIUMTEXT, " + 
	        		"Date BIGINT, " + 
	        		"Coder VARCHAR(200), " + 
	        		"Source VARCHAR(200), " + 
	        		"Section VARCHAR(200), " + 
	        		"Notes TEXT, " + 
	        		"Type VARCHAR(200), " +
	        		"PRIMARY KEY(ID))"
	        		);
			
			executeStatement(
					"CREATE TABLE IF NOT EXISTS REGEX(" + 
	                "Label VARCHAR(200), " + 
	                "Red SMALLINT UNSIGNED, " +  
	                "Green SMALLINT UNSIGNED, " + 
	                "Blue SMALLINT UNSIGNED, " +
	                "PRIMARY KEY(Label))"
	                );
			
			executeStatement(
					"CREATE TABLE IF NOT EXISTS STATEMENTTYPE(" + 
	    			"Label VARCHAR(200), " +
	    			"Red SMALLINT UNSIGNED, " +
	    			"Green SMALLINT UNSIGNED, " +
	    			"Blue SMALLINT UNSIGNED, " +
	    			"PRIMARY KEY(Label))"
	    			);
			
			executeStatement(
					"CREATE TABLE IF NOT EXISTS VARIABLES(" + 
	    			"ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, " + 
	    	    	"Variable VARCHAR(200), " + 
	    	    	"DataType VARCHAR(200), " +
	    	    	"StatementType VARCHAR(200), " +
	    	    	"FOREIGN KEY(StatementType) REFERENCES " +
	    	    			"STATEMENTTYPE(Label), " +
	    	    	"PRIMARY KEY(ID))"
	    	    	);
			
			executeStatement(
					"CREATE TABLE IF NOT EXISTS STATEMENTS(" +
	    			"ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, " +
	    			"Type VARCHAR(200), " +
	        		"Document MEDIUMINT UNSIGNED NOT NULL, " + 
	        		"Start BIGINT UNSIGNED, " + 
	        		"Stop BIGINT UNSIGNED, " + 
	        		"FOREIGN KEY(Type) REFERENCES STATEMENTTYPE(Label), " + 
	        		"FOREIGN KEY(Document) REFERENCES DOCUMENTS(ID), " +
	        		"PRIMARY KEY(ID))"
	        		);
		}
		
		// add default statement types
		
		// DNAStatement
		LinkedHashMap<String, String> dnaStatementMap = new 
				LinkedHashMap<String, String>();
		dnaStatementMap.put("person", "short text");
		dnaStatementMap.put("organization", "short text");
		dnaStatementMap.put("category", "short text");
		dnaStatementMap.put("agreement", "boolean");
		insertStatementType("DNAStatement", 255, 255, 100, dnaStatementMap);
		
		// PoliticalClaim
		LinkedHashMap<String, String> pcaMap = new LinkedHashMap<String, 
				String>();
		pcaMap.put("actor", "short text");
		pcaMap.put("tone", "integer");
		pcaMap.put("adressee", "short text");
		pcaMap.put("objectActor", "short text");
		pcaMap.put("form", "short text");
		pcaMap.put("issue", "short text");
		pcaMap.put("frame", "short text");
		pcaMap.put("region", "short text");
		insertStatementType("PoliticalClaim", 180, 255, 255, pcaMap);

		// Annotation
		LinkedHashMap<String, String> annotationMap = new LinkedHashMap<String, 
				String>();
		annotationMap.put("annotation", "long text");
		insertStatementType("Annotation", 255, 180, 180, annotationMap);
	}
	
	/**
	 * Retrieve a variable entry of type String from a custom statement table.
	 * 
	 * @param statementId  The ID of the statement for which to get the value.
	 * @param key          The name of the variable for which to get the entry.
	 * @return             The cell entry corresponding to the variable.
	 */
	public String getVariableStringEntry(int statementId, String key) {
		String type = getStatementType(statementId);
		String result = executeQueryForString(
				"SELECT " + key + " FROM " + type + " WHERE StatementID = " + 
				statementId
				);
		return result;
	}
	
	/**
	 * Retrieve a variable entry of type int from a custom statement table.
	 * 
	 * @param statementId  The ID of the statement for which to get the value.
	 * @param key          The name of the variable for which to get the entry.
	 * @return             The cell entry corresponding to the variable.
	 */
	public int getVariableIntEntry(int statementId, String key) {
		String type = getStatementType(statementId);
		int result = executeQueryForInt(
				"SELECT " + key + " FROM " + type + " WHERE StatementID = " + 
				statementId
				);
		return result;
	}
	
	/**
	 * Retrieve data type for a given statement type and variable name.
	 * 
	 * @param variable       String name of the variable.
	 * @param statementType  String name of the statement type.
	 * @return               Data type of the variable.
	 */
	public String getDataType(String variable, String statementType) {
		String type = executeQueryForString(
				"SELECT DataType FROM VARIABLES WHERE Variable = '" + 
				variable + "' AND StatementType = '" + statementType + "'"
				);
		return type;
	}
	
	/**
	 * Match a variable against a pattern and return matching statement IDs.
	 * 
	 * @param statementType  Label of the statement type/name of the table.
	 * @param variable       Name of the variable in the table.
	 * @param pattern        Regex against which to compare the cell entry.
	 * @return               An array list of matching statement IDs.
	 */
	public ArrayList<Integer> getStatementMatch(String statementType, 
			String variable, String pattern) {
		String dt = getDataType(variable, statementType);
		ArrayList<Integer> idlist = new ArrayList<Integer>();
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT StatementID, " + 
					variable + " FROM " + statementType);
			if (resultSet.next()) {
				do {
					int id = resultSet.getInt("StatementID");
					String s = "";
					if (dt.equals("short text") || dt.equals("long text")) {
						s = resultSet.getString(variable);
					} else {
						int i = resultSet.getInt(variable);
						s = new Integer(i).toString();
					}
					Pattern p = Pattern.compile(pattern);
					Matcher m = p.matcher(s);
					boolean b = m.find();
					if (b == true) {
						idlist.add(id);
					}
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		return idlist;
	}
	
	/**
	 * Retrieve all variable entries of type int from a statement table.
	 * 
	 * @param key            The name of the variable to be retrieved.
	 * @param statementType  The type of statement (= the name of the table).
	 * @return               Vector of int entries.
	 */
	public int[] getVariableIntEntries(String key, String statementType) {
		ArrayList<?> al = executeQueryForList("SELECT DISTINCT " + key + 
				" FROM " + statementType);
		int[] entries = new int[al.size()];
		for (int i = 0; i < al.size(); i++) {
			entries[i] = (Integer) al.get(i);
		}
		return entries;
	}
	
	/**
	 * Retrieve all variable entries of type String from a statement table.
	 * 
	 * @param key            The name of the variable to be retrieved.
	 * @param statementType  The type of statement (= the name of the table).
	 * @return               Vector of strings with the entries.
	 */
	public String[] getVariableStringEntries(String key, String statementType) {
		ArrayList<?> al = executeQueryForList("SELECT DISTINCT " + key + 
				" FROM " + statementType);
		String[] entries = new String[al.size()];
		for (int i = 0; i < al.size(); i++) {
			entries[i] = (String) al.get(i);
		}
		return entries;
	}
	
	/**
	 * Insert a statement type into the STATEMENTTYPE table.
	 * 
	 * @param label      A description of the statement type.
	 * @param red        The red RGB component of the statement type color.
	 * @param green      The green RGB component of the statement type color.
	 * @param blue       The blue RGB component of the statement type color.
	 * @param variables  A LinkedHashMap of variables and their data types.
	 */
	public void insertStatementType(String label, int red, int green, int blue, 
			LinkedHashMap<String, String> variables) {
		Connection connection = null;
		Statement statement = null;
		
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();

			statement.execute(
					"INSERT INTO STATEMENTTYPE (Label, Red, Green, Blue) " + 
					"VALUES('" + label + "', " + red + ", " + green + 
					", " + blue + ")"
	        );
			
			String tabString;
			if (sqlite == true) {
				tabString = "CREATE TABLE IF NOT EXISTS " + label + "(" + 
						"StatementID INTEGER PRIMARY KEY";
			} else {
				tabString = "CREATE TABLE IF NOT EXISTS " + label + "(" + 
						"StatementID MEDIUMINT UNSIGNED NOT NULL, " +
						"PRIMARY KEY(StatementID)";
			}
			
			Iterator<String> keyIterator = variables.keySet().iterator();
			while (keyIterator.hasNext()){
				String key = keyIterator.next();
				String value = variables.get(key);
				String type;
				if (sqlite == true) {
					if (value.equals("short text") || value.equals(
							"long text")) {
						type = "TEXT";
					} else if (value.equals("boolean") || value.equals(
							"integer")) {
						type = "INTEGER";
					} else {
						type = "INTEGER";
					}
				} else {
					if (value.equals("short text")) {
						type = "VARCHAR(200)";
					} else if (value.equals("long text")) {
						type = "TEXT";
					} else if (value.equals("boolean") || value.equals(
							"integer")) {
						type = "SMALLINT";
					} else {
						type = "SMALLINT";
					}
				}
				statement.execute(
						"INSERT INTO VARIABLES (Variable, DataType, " + 
						"StatementType) VALUES('" + key + "','" + value + 
						"', '" + label + "')"
		        );
				
				tabString = tabString + ", " + key + " " + type;
			}
			
			tabString = tabString + 
					", FOREIGN KEY(StatementID) REFERENCES STATEMENTS(ID))";
			statement.execute(tabString);
			
	    	statement.close();
	    	connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("There was a problem when a new statement " +
					"definition was added. Did this statement type already " +
					"exist?");
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
	}

	/**
	 * Delete a statement type (if there are no statements of this type left).
	 * 
	 * @param label  The statement type to be removed.
	 */
	public void removeStatementType(String label) {
		ArrayList<?> al = executeQueryForList(
				"SELECT ID FROM STATEMENTS WHERE Type = '" + label + "'");
		
		executeStatement("DELETE FROM STATEMENTS WHERE Type = '" + label + "'");
		
		executeStatement("DELETE FROM VARIABLES WHERE StatementType = '" + 
				label + "'");
		
		executeStatement("DELETE FROM STATEMENTTYPE WHERE Label = '" + label + 
				"'");
		
		executeStatement("DROP TABLE " + label);
		
		if (al.size() > 0) {
			System.out.println(al.size() + " statements were removed.");
		}
	}
	
	/**
	 * Add a variable to the VARIABLES table and the specific statement table.
	 * 
	 * @param varName        Name of the variable.
	 * @param dataType       Data type of the variable.
	 * @param statementType  The statement type to which this variable belongs.
	 */
	public void addVariable(String varName, String dataType, String 
			statementType) {
		
		executeStatement("INSERT INTO VARIABLES (Variable, DataType, " +
				"StatementType) VALUES('" + varName + "', '" + dataType + 
				"', '" + statementType + "')");
		
		executeStatement("ALTER TABLE " + statementType + " ADD " + varName + 
				" " + dataType);
	}
	
	/**
	 * Remove a variables from the VARIABLES table and the statement table.
	 * 
	 * @param varName        Name of the variable.
	 * @param statementType  Statement type label.
	 */
	public void removeVariable(String varName, String statementType) {
		// remove from VARIABLES table
		executeStatement("DELETE FROM VARIABLES WHERE Variable = '" + varName + 
				"' AND StatementType = '" + statementType + "'");
		
		// get names of remaining variables and concatenate them
		ArrayList<?> al = executeQueryForList(
				"SELECT Variable FROM VARIABLES WHERE StatementType = '" + 
				statementType + "'");
		@SuppressWarnings("unchecked")
		ArrayList<String> varNames = (ArrayList<String>) al;
		String varString = "StatementID";
		for (int i = 0; i < varNames.size(); i++) {
			if (!varName.equals(varNames.get(i))) {
				varString = varString + "," + varNames.get(i);
			}
		}
		
		// replace statement-specific table by a version without the variable
		executeStatement("CREATE TABLE t1_backup(" + varString + ")");
		executeStatement("INSERT INTO t1_backup SELECT " + varString + 
				" FROM " + statementType);
		executeStatement("DROP TABLE " + statementType);
		executeStatement("CREATE TABLE " + statementType + "(" + varString + 
				")");
		executeStatement("INSERT INTO " + statementType + " SELECT " + 
				varString +	" FROM t1_backup");
		executeStatement("DROP TABLE t1_backup");
	}
	
	/**
	 * Add a statement to the STATEMENTS table.
	 * 
	 * @param statementType  The type of statement (see STATEMENTTYPE table).
	 * @param doc            The ID of the corresponding document.
	 * @param start          The start caret position of the statement.
	 * @param stop           The stop caret position within the document.
	 * @return               ID of the newly created statement.
	 */
	public int addStatement(String type, int doc, int start, int stop) {
		int id = executeStatementForId(
				"INSERT INTO STATEMENTS (Type, Document, Start, Stop) " +
				"VALUES('" + type + "', " + doc + ", " + start + ", " + 
				stop + ")"
				);
		executeStatement(
				"INSERT INTO " + type + " (StatementID) VALUES (" + id + ")"
				);
		return id;
	}
	
	/**
	 * Retrieve a string array of unique document coder names in the database.
	 * 
	 * @return  String array of distinct coders.
	 */
	public String[] getDocumentCoders() {
		ArrayList<?> al = executeQueryForList(
				"SELECT DISTINCT Coder FROM DOCUMENTS"
				);
		@SuppressWarnings("unchecked")
		ArrayList<String> a = (ArrayList<String>) al;
		if (!a.contains("")) {
			a.add(0, "");
		}
		String[] coders = new String[a.size()];
		for (int i = 0; i < al.size(); i++) {
			coders[i] = (String) al.get(i);
		}
		return coders;
	}

	/**
	 * Retrieve a string array of unique document types in the database.
	 * 
	 * @return  String array of distinct types.
	 */
	public String[] getDocumentTypes() {
		ArrayList<?> al = executeQueryForList(
				"SELECT DISTINCT Type FROM DOCUMENTS"
				);
		@SuppressWarnings("unchecked")
		ArrayList<String> a = (ArrayList<String>) al;
		if (!a.contains("")) {
			a.add(0, "");
		}
		String[] types = new String[a.size()];
		for (int i = 0; i < al.size(); i++) {
			types[i] = (String) al.get(i);
		}
		return types;
	}

	/**
	 * Retrieve a string array of unique document sources in the database.
	 * 
	 * @return  String array of distinct sources.
	 */
	public String[] getDocumentSources() {
		ArrayList<?> al = executeQueryForList(
				"SELECT DISTINCT Source FROM DOCUMENTS"
				);
		@SuppressWarnings("unchecked")
		ArrayList<String> a = (ArrayList<String>) al;
		if (!a.contains("")) {
			a.add(0, "");
		}
		String[] sources = new String[a.size()];
		for (int i = 0; i < al.size(); i++) {
			sources[i] = (String) al.get(i);
		}
		return sources;
	}

	/**
	 * Retrieve a string array of unique document sections in the database.
	 * 
	 * @return  String array of distinct sections.
	 */
	public String[] getDocumentSections() {
		ArrayList<?> al = executeQueryForList(
				"SELECT DISTINCT Section FROM DOCUMENTS"
				);
		@SuppressWarnings("unchecked")
		ArrayList<String> a = (ArrayList<String>) al;
		if (!a.contains("")) {
			a.add(0, "");
		}
		String[] sections = new String[a.size()];
		for (int i = 0; i < al.size(); i++) {
			sections[i] = (String) al.get(i);
		}
		return sections;
	}
	
	/**
	 * Retrieve the color of a statement type from the STATEMENTTYPE table.
	 * 
	 * @param statementType  A string representing the statement type label.
	 * @return               The color of the statement type.
	 */
	public Color getStatementTypeColor(String statementType) {
		int red = executeQueryForInt(
				"SELECT Red FROM STATEMENTTYPE WHERE Label = '" + 
				statementType + "'"
				);
		int green = executeQueryForInt(
				"SELECT Green FROM STATEMENTTYPE WHERE Label = '" + 
				statementType + "'"
				);
		int blue = executeQueryForInt(
				"SELECT Blue FROM STATEMENTTYPE WHERE Label = '" + 
				statementType + "'"
				);
		Color color = new Color(red, green, blue);
		return color;
	}
	
	/**
	 * Retrieve a list of statements from a document.
	 * 
	 * @param documentId  The ID of the document from the DOCUMENTS table.
	 * @return            An array list of SidebarStatements.
	 */
	public ArrayList<SidebarStatement> getStatementsPerDocumentId(
			int documentId) {
		ArrayList<SidebarStatement> statements = 
				new ArrayList<SidebarStatement>();
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT ID, Type, Start, " +
					"Stop FROM STATEMENTS WHERE Document = " + documentId);
			if (resultSet.next()) {
				do {
					int id = resultSet.getInt("ID");
					String type = resultSet.getString("Type");
					int start = resultSet.getInt("Start");
					int stop = resultSet.getInt("Stop");
					Date d = getDocument(documentId).getDate();
					Color color = getStatementTypeColor(type);
					SidebarStatement s = new SidebarStatement(id, documentId, 
							start, stop, d, color, type);
					statements.add(s);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return statements;
	}
	
	/**
	 * Get an array list of statements of a specific statement type.
	 * 
	 * @param type  The statement type from the STATEMENTTYPE table.
	 * @return      An array list of SidebarStatements.
	 */
	public ArrayList<SidebarStatement> getStatementsByType(String type) {
		ArrayList<SidebarStatement> statements = 
				new ArrayList<SidebarStatement>();
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT ID, Document, " + 
					"Start, Stop FROM STATEMENTS WHERE Type = '" + type + "'");
			if (resultSet.next()) {
				do {
					int id = resultSet.getInt("ID");
					int documentId = resultSet.getInt("Document");
					int start = resultSet.getInt("Start");
					int stop = resultSet.getInt("Stop");
					Date d = getDocument(documentId).getDate();
					Color color = getStatementTypeColor(type);
					SidebarStatement s = new SidebarStatement(id, documentId, 
							start, stop, d, color, type);
					statements.add(s);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return statements;
	}
	
	/**
	 * Retrieve first statement from a document at a specific caret position.
	 * 
	 * @param documentId  ID of the document from the DOCUMENTS table.
	 * @param pos         Caret position.
	 * @return            An array list of sidebar statements.
	 */
	public SidebarStatement getStatementAtLocation(
			int documentId, int pos) {
		SidebarStatement s = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT ID, Type, Start, " +
					"Stop FROM STATEMENTS WHERE Document = " + documentId + 
					" AND Start < " + pos + " AND Stop > " + pos);
			if (resultSet.next()) {
				do {
					int id = resultSet.getInt("ID");
					String type = resultSet.getString("Type");
					int startCaret = resultSet.getInt("Start");
					int stopCaret = resultSet.getInt("Stop");
					Date d = getDocument(documentId).getDate();
					Color color = getStatementTypeColor(type);
					s = new SidebarStatement(id, documentId, startCaret, 
							stopCaret, d, color, type);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return s;
	}
	
	/**
	 * Retrieve the type of a statement as a string.
	 * 
	 * @param statementId  ID of the statement in the STATEMENTS table.
	 * @return             Type of the statement in the STATEMENTS table.
	 */
	public String getStatementType(int statementId) {
		String type = executeQueryForString(
				"SELECT Type FROM STATEMENTS WHERE ID = " + statementId
				);
		return(type);
	}
	
	/**
	 * Get the variable names and data types associated with a statement type.
	 * 
	 * @param statementType  The statement type from the STATEMENTTYPE table.
	 * @return               A hash map of variable names and data types.
	 */
	public LinkedHashMap<String, String> getVariables(String statementType) {
		LinkedHashMap<String, String> variables = new LinkedHashMap<String, 
				String>();
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery(
					"SELECT * FROM VARIABLES WHERE StatementType = '" + 
					statementType + "'"
					);
			if (resultSet.next()) {
				do {
					String key = resultSet.getString("Variable");
					String value = resultSet.getString("DataType");
					variables.put(key, value);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return variables;
	}
	
	/**
	 * Alter a statement type with a given label.
	 * 
	 * @param label  The label in the STATEMENTTYPE table.
	 * @param red    The red RGB value.
	 * @param green  The green RGB value.
	 * @param blue   The blue RGB value.
	 */
	public void changeStatementType(String oldLabel, String newLabel, int red, 
			int green, int blue) {
		executeStatement(
				"UPDATE STATEMENTTYPE SET Label = '" + newLabel + 
				"', Red = " + red + ", Green = " + green + ", Blue = " + 
				blue + " WHERE Label = '" + oldLabel + "'"
				);
		
		if (!oldLabel.equals(newLabel)) {
			executeStatement(
					"UPDATE STATEMENTS SET Type = '" + newLabel + 
					"' WHERE Type = '" + oldLabel + "'"
					);
			
			executeStatement(
					"UPDATE VARIABLES SET StatementType = '" + newLabel + 
					"' WHERE StatementType = '" + oldLabel + "'"
					);
			
			executeStatement("ALTER TABLE " + oldLabel + " RENAME TO " + 
					newLabel);
		}
	}
	
	/**
	 * Retrieve all statement types including variables from the database.
	 * 
	 * @return  An array list of statement types.
	 */
	public ArrayList<StatementType> getStatementTypes() {
		ArrayList<StatementType> types = new ArrayList<StatementType>();
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM STATEMENTTYPE");
			if (resultSet.next()) {
				do {
					String label = resultSet.getString("Label");
					int red = resultSet.getInt("Red");
					int green = resultSet.getInt("Green");
					int blue = resultSet.getInt("Blue");
					Color color = new Color(red, green, blue);
					LinkedHashMap<String, String> var = getVariables(label);
					StatementType t = new StatementType(label, color, var);
					types.add(t);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return types;
	}
	
	/**
	 * Remove a statement from the STATEMENTS table.
	 * 
	 * @param id  The ID of the statement.
	 */
	public void removeStatement(int id) {
		String type = getStatementType(id);
		executeStatement("DELETE FROM " + type + " WHERE StatementID = " + id);
		executeStatement("DELETE FROM STATEMENTS WHERE ID = " + id);
	}

	/**
	 * Retrieve the data type of a variable from the VARIABLES table.
	 * 
	 * @param variableId  ID of the variable in the VARIABLES table.
	 * @return            Data type of the variable from the VARIABLES table.
	 */
	public String getDataType(int variableId) {
		String dataType = executeQueryForString(
				"SELECT DataType FROM VARIABLES WHERE ID = " + variableId
				);
		return(dataType);
	}

	/**
	 * Retrieve the name of a variable from the VARIABLES table.
	 * 
	 * @param variableId  ID of the variable in the VARIABLES table.
	 * @return            Name of the variable from the VARIABLES table.
	 */
	public String getVariableName(int variableId) {
		String varName = executeQueryForString(
				"SELECT Variable FROM VARIABLES WHERE ID = " + variableId
				);
		return(varName);
	}
	
	/**
	 * Change a variable entry of an existing statement. 
	 * 
	 * @param statementId  ID of the statement in the STATEMENTS table.
	 * @param variableId   ID of the variable in the VARIABLES table.
	 * @param entry        Value of the variable.
	 */
	public void changeStatement(int statementId, String varName, Object entry, 
			String dataType) {
		String statementType = getStatementType(statementId);
		
		String quotMark = "";
		if (dataType.equals("short text") || dataType.equals("long text")) {
			quotMark = "'";
		}
		
		executeStatement(
				"UPDATE " + statementType + " SET " + varName + " = " + 
				quotMark + entry + quotMark + " WHERE StatementID = " + 
				statementId
				);
	}
	
	/**
	 * Retrieve a document from the DOCUMENTS table by its document ID.
	 * 
	 * @param documentId  The document ID from the DOCUMENTS table.
	 * @return            A Document.
	 */
	public Document getDocument(int documentId) {
		Document d = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery(
					"SELECT * FROM DOCUMENTS WHERE ID = " + documentId
					);
			if (resultSet.next()) {
				do {
					String title = resultSet.getString("Title");
					String text = resultSet.getString("Text");
					String coder = resultSet.getString("Coder");
					String source = resultSet.getString("Source");
					String section = resultSet.getString("Section");
					String notes = resultSet.getString("Notes");
					String type = resultSet.getString("Type");
					long intDate = resultSet.getLong("Date");
					Date date = new Date();
					date.setTime(intDate);
					d = new Document(documentId, title, text, date, coder, 
							source,	section, notes, type);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return d;
	}
	
	/**
	 * Retrieve the complete list of documents.
	 * 
	 * @return  ArrayList of all documents in the DOCUMENTS table.
	 */
	public ArrayList<Document> getDocuments() {
		ArrayList<?> al = executeQueryForList("SELECT ID FROM DOCUMENTS");
		@SuppressWarnings("unchecked")
		ArrayList<Integer> ids = (ArrayList<Integer>) al;
		ArrayList<Document> docs = new ArrayList<Document>();
		for (int i = 0; i < ids.size(); i++) {
			Document d = getDocument(ids.get(i));
			docs.add(d);
		}
		return docs;
	}
	
	/**
	 * Add a regular expression to the REGEX table.
	 * 
	 * @param label  The label of the regular expression.
	 * @param red    The red RGB component of the regex color.
	 * @param green  The green RGB component of the regex color.
	 * @param blue   The blue RGB component of the regex color.
	 */
	public void addRegex(String label, int red, int green, int blue) {
		executeStatement(
				"INSERT INTO REGEX (Label, Red, Green, Blue) " + 
				"VALUES('" + label + "', " + red + ", " + green + 
				", " + blue + ")"
				);
	}
	
	/**
	 * Remove a regular expression from the REGEX table.
	 * 
	 * @param label  The label of the regular expression.
	 */
	public void removeRegex(String label) {
		executeStatement("DELETE FROM REGEX WHERE Label = " + label);
	}
	
	/**
	 * Return the complete list of regular expressions from the REGEX table.
	 * 
	 * @return  List of Regex objects.
	 */
	public ArrayList<Regex> getRegex() {
		ArrayList<Regex> regex = new ArrayList<Regex>();
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM REGEX");
			if (resultSet.next()) {
				do {
					String label = resultSet.getString("Label");
					int red = resultSet.getInt("Red");
					int green = resultSet.getInt("Green");
					int blue = resultSet.getInt("Blue");
					Color color = new Color(red, green, blue);
					Regex r = new Regex(label, color);
					regex.add(r);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return regex;
	}
	
	/**
	 * Retrieve a statement by providing its ID from the STATEMENTS table.
	 * 
	 * @param statementId  The ID of the statement.
	 * @return             A SidebarStatement.
	 */
	public SidebarStatement getStatement(int statementId) {
		SidebarStatement s = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			if (sqlite == true) {
				connection = getSQLiteConnection();
			} else {
				connection = getMySQLConnection();
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery(
					"SELECT * FROM STATEMENTS WHERE ID = " + statementId
					);
			if (resultSet.next()) {
				do {
					int documentId = resultSet.getInt("Document");
					String type = resultSet.getString("Type");
					int startCaret = resultSet.getInt("Start");
					int stopCaret = resultSet.getInt("Stop");
					Date d = getDocument(documentId).getDate();
					Color color = getStatementTypeColor(type);
					s = new SidebarStatement(statementId, documentId, 
							startCaret, stopCaret, d, color, type);
				} while (resultSet.next());
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { statement.close(); } catch(Exception e) {}
			try { connection.close(); } catch(Exception e) {}
		}
		
		return s;
	}
	
	/**
	 * Retrieve the IDs of all statements within a document.
	 * 
	 * @param documentId  ID of the document associated with a statement.
	 * @return            ArrayList with the statement IDs.
	 */
	public ArrayList<Integer> getStatementIds(int documentId) {
		ArrayList<?> al = executeQueryForList(
				"SELECT ID FROM STATEMENTS WHERE Document = " + documentId);
		@SuppressWarnings("unchecked")
		ArrayList<Integer> ids = (ArrayList<Integer>) al;
		return(ids);
	}
	
	/**
	 * Remove a document and all statements contained in the document.
	 * 
	 * @param documentId  The ID of the document to be removed.
	 */
	public void removeDocument(int documentId) {
		ArrayList<Integer> statementIds = getStatementIds(documentId);
		for (int i = 0; i < statementIds.size(); i++) {
			removeStatement(statementIds.get(i));
		}
		executeStatement("DELETE FROM DOCUMENTS WHERE ID = " + documentId);
	}
	
	/**
	 * Add a new document to the DOCUMENTS table.
	 * 
	 * @param title   The title of the document.
	 * @param text    The text of the document.
	 * @param date    The date of the document.
	 * @param coder   Who annotated the document?
	 * @param source  What is the document source (e.g., which newspaper)?
	 * @param notes   Additional free-text notes regarding the document.
	 * @param type    Type of document (e.g., article from business section).
	 * @return        ID of the new document.
	 */
	public int addDocument(String title, String text, Date date, String coder, 
			String source, String section, String notes, String type) {
		long intDate = date.getTime();
		int id = executeStatementForId(
				"INSERT INTO DOCUMENTS (Title, Text, Date, Coder, " +
				"Source, Section, Notes, Type) VALUES('" + title + "', '" + 
				text + "', " + intDate + ", '" + coder + "', '" + source + 
				"', '" + section + "', '" + notes + "', '" + type + "')"
				);
		return id;
	}

	/**
	 * Update the metadata of a document.
	 * 
	 * @param documentId  ID (not modified; used for identification of the doc).
	 * @param title       The title of the document.
	 * @param date        The date of the document.
	 * @param coder       The coder of the document.
	 * @param source      The source of the document.
	 * @param notes       The notes of the document.
	 * @param type        The type of the document.
	 */
	public void changeDocument(int documentId, String title, Date date, 
			String coder, String source, String section, String notes, 
			String type) {
		long dateInt = date.getTime();
		executeStatement(
				"UPDATE DOCUMENTS SET Title = '" + title + "', Date = " + 
				dateInt + ", Coder = '" + coder + "', Source = '" + source + 
				"', Section = '" + section + "', Notes = '" + notes + 
				"', Type = '" + type + "' WHERE ID = " + documentId
				);
	}
	
	/**
	 * Create a copy of an existing statement with a given new statement ID
	 * 
	 * @param oldStatementId  Existing statement ID.
	 * @param newStatementId  ID of the new statement.
	 */
	public int duplicateStatement(int oldStatementId, int documentId, 
			int start, int stop) {
		
		String type = this.getStatementType(oldStatementId);
		
		int id = executeStatementForId(
				"INSERT INTO STATEMENTS (Type, Document, Start, Stop) " +
				"VALUES('" + type + "', " + documentId + ", " + start + ", " + 
				stop + ")"
				);
		
		executeStatement(
				"INSERT INTO " + type + " (StatementID) VALUES (" + id + ")");
		
		LinkedHashMap<String, String> variables = this.getVariables(type);
		
		Iterator<String> keyIterator = variables.keySet().iterator();
		while (keyIterator.hasNext()){
			String key = keyIterator.next();
			String value = variables.get(key);
			String quotes;
			if (value.equals("short text") || value.equals("long text")) {
				quotes = "'";
				String content = this.getVariableStringEntry(oldStatementId, 
						key);
				executeStatement(
						"UPDATE " + type + " SET " + key + " = " + quotes + 
						content + quotes + " WHERE StatementId = " + id
		        );
			} else {
				quotes = "";
				int content = this.getVariableIntEntry(oldStatementId, 
						key);
				executeStatement(
						"UPDATE " + type + " SET " + key + " = " + quotes + 
						content + quotes + " WHERE StatementId = " + id
		        );
			}
		}
		
		return id;
	}
}