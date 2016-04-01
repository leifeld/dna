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

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

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
	 * @param key    Property to set
	 * @param value  Value corresponding to the property
	 */
	public void upsertSetting(String key, String value) {
		if (dbtype == "sqlite") {
			executeStatement("INSERT OR REPLACE INTO SETTINGS (Property, Value) VALUES ('" + key + "', '" + value + "')");
		} else if (dbtype == "mysql") {
			executeStatement("INSERT INTO SETTINGS (Property, Value) VALUES('" + key + "', '" + value + "') "
					+ "ON DUPLICATE KEY UPDATE Value = '" + value + "'");
		}
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
		
		if (dbtype == "sqlite") {
			executeStatement("INSERT OR REPLACE INTO CODERS (ID, Name, Red, Green, Blue, Password) "
					+ "VALUES (" + id + ", '" + name + "', " + red + ", " + green + ", " + blue + ", '" + password + "')");
		} else if (dbtype == "mysql") {
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
	
	/**
	 * @param document   Document to add to/update in the DOCUMENTS table
	 */
	public void upsertDocument(Document document) {
		/*
		long count = -1;
		try {
			count = (long) executeQueryForObject("SELECT COUNT(1) FROM DOCUMENTS WHERE ID = " + document.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (count == 1) {
			executeStatement("UPDATE DOCUMENTS SET Title = " + document.getTitle().replaceAll("'", "''") + ", Text = '" 
					+ document.getText().replaceAll("'", "''") + "', Coder = " + document.getCoder() + ", Author = '" 
					+ document.getAuthor().replaceAll("'", "''") + "', Source = '" + document.getSource().replaceAll("'", "''") 
					+ "', Section = '" + document.getSection().replaceAll("'", "''") + "', Notes = '" 
					+ document.getNotes().replaceAll("'", "''") + "', Type = '" + document.getType().replaceAll("'", "''") 
					+ "', Date = " + document.getDate().getTime());
		} else {
			executeStatement("INSERT INTO DOCUMENTS(ID, Title, Text, Coder, Author, Source, Section, Notes, Type, Date) "
					+ "VALUES (" + document.getId() + ", '" + document.getTitle().replaceAll("'", "''")  + "', '" 
					+ document.getText().replaceAll("'", "''") + "', " + document.getCoder() + ", '" 
					+ document.getAuthor().replaceAll("'", "''")  + "', '" + document.getSource().replaceAll("'", "''")  + "', '" 
					+ document.getSection().replaceAll("'", "''") + "', '" + document.getNotes().replaceAll("'", "''") + "', '" 
					+ document.getType().replaceAll("'", "''") + "', " + document.getDate().getTime() + ")");
		}
		*/
		executeStatement("REPLACE INTO DOCUMENTS(ID, Title, Text, Coder, Author, Source, Section, Notes, Type, Date) "
				+ "VALUES (" + document.getId() + ", '" + document.getTitle().replaceAll("'", "''")  + "', '" 
				+ document.getText().replaceAll("'", "''") + "', " + document.getCoder() + ", '" 
				+ document.getAuthor().replaceAll("'", "''")  + "', '" + document.getSource().replaceAll("'", "''")  + "', '" 
				+ document.getSection().replaceAll("'", "''") + "', '" + document.getNotes().replaceAll("'", "''") + "', '" 
				+ document.getType().replaceAll("'", "''") + "', " + document.getDate().getTime() + ")");
	}
	
	/**
	 * @param regex   Regular expression to add to/update in the REGEXES table
	 */
	public void upsertRegex(Regex regex) {
		executeStatement("REPLACE INTO REGEXES(Label, Red, Green, Blue) "
				+ "VALUES ('" + regex.getLabel() + "', " + regex.getColor().getRed() + ", " + regex.getColor().getGreen()
				+ ", " + regex.getColor().getBlue() + ")");
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
	 * @param sl   StatementLink to add to/update in the STATEMENTLINKS table
	 */
	public void upsertStatementLink(StatementLink sl) {
		executeStatement("REPLACE INTO STATEMENTLINKS(ID, SourceId, TargetId) "
				+ "VALUES (" + sl.getId() + ", " + sl.getSourceId() + ", " + sl.getTargetId() + ")");
	}
	
	/*
	class DocumentLoader implements Runnable {
		ProgressMonitor progressMonitor;
		ArrayList<Document> al;
		
		public ArrayList<Document> DocumentLoader() {
			al = new ArrayList<Document>();
			run();
			return al;
		}
		
		public void run() {
			int count = 0;
			try {
				count = (int) executeQueryForObject("SELECT COUNT(*) FROM DOCUMENTS");
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			progressMonitor = new ProgressMonitor(Dna.dna.gui, "Importing documents and statements...", "", 0, count);
			progressMonitor.setMillisToDecideToPopup(1);
			
			try {
				String myQuery = "SELECT * FROM DOCUMENTS";
				PreparedStatement preStatement = (PreparedStatement) connection.prepareStatement(myQuery);
				ResultSet result = preStatement.executeQuery();
				int i = 0;
				if (result.next()) {
					do {
						i++;
						progressMonitor.setProgress(i);
						if (progressMonitor.isCanceled()) {
							break;
						}
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
		}
	}
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
		int dataId = (int) executeQueryForObject("SELECT ID FROM " + table + " WHERE VariableId = " 
				+ "(SELECT ID FROM VARIABLES WHERE StatementTypeId = " + statementTypeId + " AND Variable = '" 
				+ variableName + "') AND StatementId = " + statementId);

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
	
	public void removeDocument(int documentId) {
		executeStatement("DELETE FROM DATABOOLEAN WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = " + documentId + ")");
		executeStatement("DELETE FROM DATAINTEGER WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = " + documentId + ")");
		executeStatement("DELETE FROM DATASHORTTEXT WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = " + documentId + ")");
		executeStatement("DELETE FROM DATALONGTEXT WHERE StatementId IN (SELECT ID FROM STATEMENTS WHERE DocumentId = " + documentId + ")");
		executeStatement("DELETE FROM STATEMENTS WHERE DocumentId = " + documentId);
		executeStatement("DELETE FROM DOCUMENTS WHERE ID = " + documentId);
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

	public void removeRegex(String label) {
		executeStatement("DELETE FROM REGEXES WHERE Label = '" + label + "'");
	}
	
	public void removeStatement(int statementId) {
		executeStatement("DELETE FROM DATABOOLEAN WHERE StatementId = " + statementId);
		executeStatement("DELETE FROM DATAINTEGER WHERE StatementId = " + statementId);
		executeStatement("DELETE FROM DATASHORTTEXT WHERE StatementId = " + statementId);
		executeStatement("DELETE FROM DATALONGTEXT WHERE StatementId = " + statementId);
		executeStatement("DELETE FROM STATEMENTS WHERE ID = " + statementId);
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
		return data;
	}
	
	/**
	 * @return     Array list of all documents in the SQL database.
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
			JOptionPane.showMessageDialog(Dna.dna.gui, 
					"Database access could not be executed properly. Report this problem along with the \n "
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
}