package dna;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

public class DataAccess {
	String dbfile;
	
	public DataAccess(String dbfile) {
		this.dbfile = dbfile;
	}
	
	Connection getConnection() throws ClassNotFoundException, SQLException {    
		Class.forName("org.sqlite.JDBC");
		Connection conn= DriverManager.getConnection("jdbc:sqlite:" + dbfile);
		return conn;
	}
	
	public void createTables() {
		Connection connection = null;
		Statement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
	        statement.execute(
	        		"CREATE TABLE IF NOT EXISTS SETTINGS(" + 
	    	        "Property TEXT PRIMARY KEY, " + 
	    	        "Value TEXT)"
	    	);
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
		

		try {
			connection = getConnection();
			statement = connection.createStatement();
			statement.execute(
	        		"CREATE TABLE IF NOT EXISTS DOCUMENTS(" + 
	        		"ID INTEGER NOT NULL PRIMARY KEY, " + 
	        		"Title TEXT, " + 
	        		"Text TEXT, " + 
	        		"Date INTEGER, " + 
	        		"Coder TEXT, " + 
	        		"Source TEXT, " + 
	        		"Notes TEXT, " + 
	        		"Type TEXT)"
	        );
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
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
	        statement.execute(
	        		"CREATE TABLE IF NOT EXISTS REGEX(" + 
	                "Label TEXT PRIMARY KEY, " + 
	                "Red INTEGER, " +  
	                "Green INTEGER, " + 
	                "Blue INTEGER)"
	        );
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
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			statement.execute(
	    			"CREATE TABLE IF NOT EXISTS STATEMENTTYPE(" + 
	    			"Label TEXT PRIMARY KEY, " +
	    			"Red INTEGER, " +
	    			"Green INTEGER, " +
	    			"Blue INTEGER)"
	    	);
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

		try {
			connection = getConnection();
			statement = connection.createStatement();
	        statement.execute(
	    			"CREATE TABLE IF NOT EXISTS VARIABLES(" + 
	    			"ID INTEGER NOT NULL PRIMARY KEY, " + 
	    	    	"Variable TEXT, " + 
	    	    	"DataType TEXT, " +
	    	    	"StatementType INTEGER, " +
	    	    	"FOREIGN KEY(StatementType) REFERENCES STATEMENTTYPE(ID))"
	    	);
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
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			statement.execute(
	    			"CREATE TABLE IF NOT EXISTS STATEMENTS(" +
	    			"ID INTEGER NOT NULL PRIMARY KEY, " +
	    			"Type TEXT, " +
	        		"Document INTEGER, " + 
	        		"Start INTEGER, " + 
	        		"Stop INTEGER, " + 
	        		"FOREIGN KEY(Document) REFERENCES DOCUMENTS(ID))"
	    	);
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
	
	public void insertStatementType(String label, int red, int green, int blue, 
			HashMap<String, String> variables) {
		Connection connection = null;
		Statement statement = null;

		try {
			connection = getConnection();
			statement = connection.createStatement();

			statement.execute(
					"INSERT INTO STATEMENTTYPE (Label, Red, Green, Blue) " + 
					"VALUES('" + label + "', " + red + ", " + green + 
					", " + blue + ")"
	        );
			
			String tabString = "CREATE TABLE IF NOT EXISTS " + label + "(" + 
					"ID INTEGER NOT NULL PRIMARY KEY, StatementID INTEGER";
			
			Iterator<String> keyIterator = variables.keySet().iterator();
			while (keyIterator.hasNext()){
				String key = keyIterator.next();
				String value = variables.get(key);
				String type;
				if (value.equals("short text") || value.equals("long text")) {
					type = "TEXT";
				} else if (value.equals("boolean") || value.equals("integer")) {
					type = "INTEGER";
				} else {
					type = "INTEGER";
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
}
