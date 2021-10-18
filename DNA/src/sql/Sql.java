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
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.sqlite.SQLiteDataSource;

import com.mysql.cj.jdbc.MysqlDataSource;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import model.Attribute;
import model.Coder;
import model.CoderRelation;
import model.Document;
import model.Statement;
import model.StatementType;
import model.Value;

/**
 * This class contains information on the database connection, the data source
 * for establishing connections, and methods for interacting with the database.
 * 
 * @category setup
 */
public class Sql {
	
	/**
	 * The {@link sql.ConnectionProfile ConnectionProfile} to be used for
	 * connecting to a database.
	 * 
	 * @category setup
	 */
	private ConnectionProfile cp;
	
	/**
	 * The {@link javax.sql.DataSource DataSource} to be used for connections.
	 * 
	 * @category setup
	 */
	private DataSource ds;
	
	/**
	 * The active {@link model.Coder Coder} including permissions.
	 */
	private Coder activeCoder;
	
	/**
	 * Keep a list of classes that depend on which coder is selected and what
	 * characteristics the coder has, for examples text panel.
	 */
	private static List<SqlListener> sqlListeners = new ArrayList<SqlListener>();
	
	
	/* =========================================================================
	 * Setup
	 * ====================================================================== */

	/**
	 * Create an instance of the Sql class and create a data source based on a
	 * {@link sql.ConnectionProfile connectionProfile} object for SQLite,
	 * MySQL, or PostgreSQL.
	 * 
	 * @param cp    A {@link sql.ConnectionProfile connectionProfile} object,
	 *   which contains connection details for a DNA database.
	 * @param test  Boolean indicating whether this is just a connection test.
	 *   In that event, it is assumed that no data structures/tables are present
	 *   yet, and no coder will be selected.
	 *   
	 * @category setup
	 */
	public Sql(ConnectionProfile cp, boolean test) {
		this.setConnectionProfile(cp, test);
	}

	/**
	 * Create an instance of the Sql class without an initial connection.
	 *   
	 * @category setup
	 */
	public Sql() {
		this.setConnectionProfile(null, false);
	}

	/**
	 * Get the connection profile.
	 * 
	 * @return A {@link sql.ConnectionProfile connectionProfile} object.
	 * 
	 * @category setup
	 */
	public ConnectionProfile getConnectionProfile() {
		return this.cp;
	}

	/**
	 * Set the connection profile and save the current coder with permissions.
	 * 
	 * @param cp    A {@link sql.ConnectionProfile connectionProfile} object.
	 * @param test  Boolean indicating whether this is just a connection test.
	 *   In that event, it is assumed that no data structures/tables are present
	 *   yet, and no coder will be selected.
	 * 
	 * @category setup
	 */
	public void setConnectionProfile(ConnectionProfile cp, boolean test) {
		this.cp = cp;
		if (cp == null) { // null connection
			ds = null;
			this.cp = null;
			this.activeCoder = null;
		} else if (cp.getType().equals("sqlite")) { // no user name and password needed for file-based database
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
		fireConnectionChange();
		if (test == false) {
			updateActiveCoder();  // update active coder and notify listeners about the update
		}
	}
	
	/**
	 * Get the active coder.
	 * 
	 * @return A {@link model.Coder Coder} object with all permissions.
	 * 
	 * @category setup
	 */
	public Coder getActiveCoder() {
		return activeCoder;
	}

	/**
	 * Get the data source stored in the {@link sql.Sql Sql} object.
	 * 
	 * @return A {@link javax.sql.DataSource DataSource} object.
	 * 
	 * @category setup
	 */
	public DataSource getDataSource() {
		return ds;
	}

	/**
	 * An interface for listeners for changes in the active connection or coder,
	 * including selection of a different coder and changes in any coder
	 * details, such as permissions, and a change of connection profile. For
	 * example, the GUI should react to permission changes or change of coder by
	 * enabling or disabling statement popups windows, repainting statements in
	 * the text etc. The GUI components thus need to be registered as listeners.
	 * 
	 * @category setup
	 */
	public interface SqlListener {
		void adjustToChangedConnection();
		void adjustToChangedCoder();
	}

	/**
	 * Add a coder listener. This can be an object that implements the
	 * {@link CoderListener} interface.
	 * 
	 * @param listener An object implementing the {@link SqlListener}
	 *   interface.
	 * 
	 * @category setup
	 */
	public void addSqlListener(SqlListener listener) {
        sqlListeners.add(listener);
    }

	/**
	 * Notify listeners that the connection has changed.
	 * 
	 * @category setup
	 */
	public static void fireConnectionChange() {
		for (SqlListener listener : sqlListeners) {
			listener.adjustToChangedConnection();
		}
	}

	/**
	 * Notify listeners that the active coder has changed.
	 * 
	 * @category setup
	 */
	public static void fireCoderChange() {
		for (SqlListener listener : sqlListeners) {
			listener.adjustToChangedCoder();
		}
	}

	/**
	 * Set a new active coder.
	 * 
	 * @param coderId  ID of the new active coder.
	 * 
	 * @category setup
	 */
	public void changeActiveCoder(int coderId) {
		if (coderId > -1) {
			getConnectionProfile().setCoder(coderId);
			this.activeCoder = getCoder(coderId);
		} else {
			setConnectionProfile(null, false); // not a connection test, so false
			this.activeCoder = null;
		}
		fireCoderChange();
	}

	/**
	 * Reload settings and permissions of active coder.
	 * 
	 * @category setup
	 */
	public void updateActiveCoder() {
		if (getConnectionProfile() == null) {
			this.activeCoder = null;
		} else {
			this.activeCoder = getCoder(cp.getCoderId());
		}
		fireCoderChange();
	}

	/**
	 * An interface that rolls back failed execution attempts of SQL connections
	 * in {@literal try}-with-resources headers. Use this as the last code line
	 * in any {@literal try}-with-resources header that uses SQL transactions.
	 * 
	 * @category setup
	 */
	public interface SQLCloseable extends AutoCloseable {
	    /**
	     * Close the object, which rolls back any initiated transactions.
	     * 
	     * @category setup
	     */
	    @Override public void close() throws SQLException;
	}

	/**
	 * A class that contains a JDBC connection, a prepared statement, and a
	 * result set for processing results in another class and closing the
	 * connection there when done. For example, the class can be used to
	 * transport results between the {@link sql.Sql Sql} class and a Swing
	 * worker. The Swing worker can use the result set and then invoke the
	 * {@code close} method to close the connection when done.
	 * 
	 * @category setup
	 */
	public class SqlResults implements AutoCloseable {
		ResultSet rs;
		PreparedStatement ps;
		Connection c;
		
		/**
		 * Create a new instance of an {@link SqlResults}
		 * container.
		 * 
		 * @param rs  A {@link java.sql.ResultSet ResultSet} object.
		 * @param ps  A {@link java.sql.PreparedStatement PreparedStatement}.
		 * @param c   A {@link java.sql.Connection Connection} object.
		 */
		public SqlResults(ResultSet rs, PreparedStatement ps, Connection c) {
			this.rs = rs;
			this.ps = ps;
			this.c = c;
		}

		public void close() throws SQLException {
			rs.close();
			ps.close();
			c.close();
		}
		
		/**
		 * Get the result set.
		 * 
		 * @return An SQL/JDBC result set.
		 */
		public ResultSet getResultSet() {
			return rs;
		}
	}
	
	/**
	 * Create data structures (tables and basic contents) in a new DNA database.
	 * 
	 * @param encryptedAdminPassword  The encrypted/hashed password of the
	 *   {@code Admin} coder for storage in the {@code CODERS} table of the
	 *   database.
	 *   
	 * @return A {@link boolean} indicator of whether the data structures were
	 *   successfully created ({@code true}) or rolled back ({@code false}).
	 * 
	 * @category setup
	 */
	public boolean createTables(String encryptedAdminPassword) {
		boolean success = true;
		ArrayList<String> s = new ArrayList<String>();
		if (cp.getType().equals("sqlite")) {
			s.add("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property TEXT PRIMARY KEY, "
					+ "Value TEXT NOT NULL);");
			s.add("CREATE TABLE IF NOT EXISTS CODERS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Name TEXT NOT NULL, "
					+ "Red INTEGER NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "Refresh INTEGER NOT NULL CHECK (Refresh BETWEEN 0 AND 9999) DEFAULT 0, "
					+ "FontSize INTEGER NOT NULL CHECK (FontSize BETWEEN 1 AND 99) DEFAULT 14, "
					+ "Password TEXT NOT NULL, "
					+ "PopupWidth INTEGER CHECK (PopupWidth BETWEEN 100 AND 9999) DEFAULT 300, "
					+ "ColorByCoder INTEGER NOT NULL CHECK (ColorByCoder BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PopupDecoration INTEGER NOT NULL CHECK (PopupDecoration BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PopupAutoComplete INTEGER NOT NULL CHECK (PopupAutoComplete BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionAddDocuments INTEGER NOT NULL CHECK (PermissionAddDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditDocuments INTEGER NOT NULL CHECK (PermissionEditDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionDeleteDocuments INTEGER NOT NULL CHECK (PermissionDeleteDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionImportDocuments INTEGER NOT NULL CHECK (PermissionImportDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionAddStatements INTEGER NOT NULL CHECK (PermissionAddStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditStatements INTEGER NOT NULL CHECK (PermissionEditStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionDeleteStatements INTEGER NOT NULL CHECK (PermissionDeleteStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditAttributes INTEGER NOT NULL CHECK (PermissionEditAttributes BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditRegex INTEGER NOT NULL CHECK (PermissionEditRegex BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditStatementTypes INTEGER NOT NULL CHECK (PermissionEditStatementTypes BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PermissionEditCoders INTEGER NOT NULL CHECK (PermissionEditCoders BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PermissionViewOthersDocuments INTEGER NOT NULL CHECK (PermissionViewOthersDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditOthersDocuments INTEGER NOT NULL CHECK (PermissionEditOthersDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionViewOthersStatements INTEGER NOT NULL CHECK (PermissionViewOthersStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditOthersStatements INTEGER NOT NULL CHECK (PermissionEditOthersStatements BETWEEN 0 AND 1) DEFAULT 1);");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Title TEXT NOT NULL, "
					+ "Text TEXT NOT NULL, "
					+ "Coder INTEGER, "
					+ "Author TEXT NOT NULL DEFAULT '', "
					+ "Source TEXT NOT NULL DEFAULT '', "
					+ "Section TEXT NOT NULL DEFAULT '', "
					+ "Notes TEXT NOT NULL DEFAULT '', "
					+ "Type TEXT NOT NULL DEFAULT '', "
					+ "Date INTEGER NOT NULL, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Label TEXT NOT NULL, "
					+ "Red INTEGER NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Variable TEXT NOT NULL, "
					+ "DataType TEXT NOT NULL DEFAULT 'short text', "
					+ "StatementTypeId INTEGER, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (Variable, StatementTypeId));");
			s.add("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label TEXT PRIMARY KEY, "
					+ "Red INTEGER NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Coder INTEGER CHECK(Coder > 0 AND Coder != OtherCoder), "
					+ "OtherCoder INTEGER CHECK(OtherCoder > 0 AND OtherCoder != Coder), "
					+ "viewStatements INTEGER NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editStatements INTEGER NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "viewDocuments INTEGER NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editDocuments INTEGER NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(OtherCoder) REFERENCES CODERS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "StatementTypeId INTEGER, "
					+ "DocumentId INTEGER, "
					+ "Start INTEGER NOT NULL CHECK(Start >= 0 AND Start < Stop), "
					+ "Stop INTEGER NOT NULL CHECK(Stop >= 0 AND Stop > Start), "
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
					+ "Value INTEGER NOT NULL DEFAULT 1, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "Value INTEGER NOT NULL DEFAULT 0, "
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
					+ "Value TEXT DEFAULT '', "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTES("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "Value TEXT NOT NULL DEFAULT '', "
					+ "Red INTEGER CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER CHECK (Blue BETWEEN 0 AND 255), "
					+ "ChildOf INTEGER CHECK(ChildOf > 0 AND ChildOf != ID), "
					+ "UNIQUE (VariableId, Value), "
					+ "FOREIGN KEY(ChildOf) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEVARIABLES("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "MetaVariable TEXT NOT NULL, "
					+ "UNIQUE(VariableId, MetaVariable), "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEMETADATA("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "AttributeId INTEGER NOT NULL, "
					+ "MetaVariableId INTEGER NOT NULL, "
					+ "Value TEXT NOT NULL DEFAULT '', "
					+ "UNIQUE (AttributeId, MetaVariableId), "
					+ "FOREIGN KEY(AttributeId) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(MetaVariableId) REFERENCES ATTRIBUTEVARIABLES(ID) ON DELETE CASCADE);");
		} else if (cp.getType().equals("mysql")) {
			s.add("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property VARCHAR(500), "
					+ "Value VARCHAR(500) NOT NULL,"
					+ "PRIMARY KEY (Property));");
			s.add("CREATE TABLE IF NOT EXISTS CODERS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Name VARCHAR(5000) NOT NULL, "
					+ "Red SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "Refresh SMALLINT UNSIGNED NOT NULL CHECK (Refresh BETWEEN 0 AND 9999) DEFAULT 0, "
					+ "FontSize SMALLINT UNSIGNED NOT NULL CHECK (FontSize BETWEEN 1 AND 99) DEFAULT 14, "
					+ "Password VARCHAR(300) NOT NULL, "
					+ "PopupWidth SMALLINT UNSIGNED NOT NULL CHECK (PopupWidth BETWEEN 100 AND 9999) DEFAULT 300, "
					+ "ColorByCoder TINYINT UNSIGNED NOT NULL CHECK (ColorByCoder BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PopupDecoration TINYINT UNSIGNED NOT NULL CHECK (PopupDecoration BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PopupAutoComplete TINYINT UNSIGNED NOT NULL CHECK (PopupAutoComplete BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionAddDocuments TINYINT UNSIGNED NOT NULL CHECK (PermissionAddDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditDocuments TINYINT UNSIGNED NOT NULL CHECK (PermissionEditDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionDeleteDocuments TINYINT UNSIGNED NOT NULL CHECK (PermissionDeleteDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionImportDocuments TINYINT UNSIGNED NOT NULL CHECK (PermissionImportDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionAddStatements TINYINT UNSIGNED NOT NULL CHECK (PermissionAddStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditStatements TINYINT UNSIGNED NOT NULL CHECK (PermissionEditStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionDeleteStatements TINYINT UNSIGNED NOT NULL CHECK (PermissionDeleteStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditAttributes TINYINT UNSIGNED NOT NULL CHECK (PermissionEditAttributes BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditRegex TINYINT UNSIGNED NOT NULL CHECK (PermissionEditRegex BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditStatementTypes TINYINT UNSIGNED NOT NULL CHECK (PermissionEditStatementTypes BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PermissionEditCoders TINYINT UNSIGNED NOT NULL CHECK (PermissionEditCoders BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PermissionViewOthersDocuments TINYINT UNSIGNED NOT NULL CHECK (PermissionViewOthersDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditOthersDocuments TINYINT UNSIGNED NOT NULL CHECK (PermissionEditOthersDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionViewOthersStatements TINYINT UNSIGNED NOT NULL CHECK (PermissionViewOthersStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditOthersStatements TINYINT UNSIGNED NOT NULL CHECK (PermissionEditOthersStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Title VARCHAR(5000) NOT NULL, "
					+ "Text MEDIUMTEXT NOT NULL, "
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "Author VARCHAR(5000) NOT NULL DEFAULT '', "
					+ "Source VARCHAR(5000) NOT NULL DEFAULT '', "
					+ "Section VARCHAR(5000) NOT NULL DEFAULT '', "
					+ "Notes TEXT NOT NULL DEFAULT '', "
					+ "Type VARCHAR(5000) NOT NULL DEFAULT '', "
					+ "Date BIGINT NOT NULL, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Label VARCHAR(5000) NOT NULL, "
					+ "Red SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Variable VARCHAR(500) NOT NULL, "
					+ "DataType VARCHAR(200) NOT NULL DEFAULT 'short text', "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (Variable, StatementTypeId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label VARCHAR(2000), "
					+ "Red SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "PRIMARY KEY(Label));");
			s.add("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Coder SMALLINT UNSIGNED NOT NULL CHECK(Coder > 0 AND Coder != OtherCoder), "
					+ "OtherCoder SMALLINT UNSIGNED NOT NULL CHECK(OtherCoder > 0 AND OtherCoder != Coder), "
					+ "viewStatements SMALLINT UNSIGNED NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editStatements SMALLINT UNSIGNED NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "viewDocuments SMALLINT UNSIGNED NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editDocuments SMALLINT UNSIGNED NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(OtherCoder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "DocumentId MEDIUMINT UNSIGNED NOT NULL, "
					+ "Start BIGINT UNSIGNED NOT NULL CHECK(Start >= 0 AND Start < Stop), "
					+ "Stop BIGINT UNSIGNED NOT NULL CHECK(Stop >= 0 AND Stop > Start), "
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
					+ "Value SMALLINT UNSIGNED NOT NULL DEFAULT 1, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (StatementId, VariableId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "Value MEDIUMINT NOT NULL DEFAULT 0, "
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
					+ "Value TEXT NOT NULL DEFAULT '', "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (StatementId, VariableId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTES("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "Value TEXT NOT NULL DEFAULT '', "
					+ "Red SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "ChildOf MEDIUMINT UNSIGNED CHECK(ChildOf > 0 AND ChildOf != ID), "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(ChildOf) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (VariableId, Value), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEVARIABLES("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "VariableId MEDIUMINT UNSIGNED NOT NULL, "
					+ "MetaVariable TEXT NOT NULL, "
					+ "UNIQUE KEY(VariableId, MetaVariable), "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEMETADATA("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "AttributeId MEDIUMINT UNSIGNED NOT NULL, "
					+ "MetaVariableId MEDIUMINT UNSIGNED NOT NULL, "
					+ "Value TEXT NOT NULL DEFAULT '', "
					+ "UNIQUE KEY (AttributeId, MetaVariableId), "
					+ "FOREIGN KEY(AttributeId) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(MetaVariableId) REFERENCES ATTRIBUTEVARIABLES(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
		} else if (cp.getType().equals("postgresql")) {
			s.add("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property VARCHAR(500) PRIMARY KEY, "
					+ "Value VARCHAR(500) NOT NULL);");
			s.add("CREATE TABLE IF NOT EXISTS CODERS("
					+ "ID SERIAL NOT NULL PRIMARY KEY CHECK (ID > 0), "
					+ "Name VARCHAR(5000) NOT NULL, "
					+ "Red SERIAL NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SERIAL NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SERIAL NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "Refresh SMALLINT NOT NULL CHECK (Refresh BETWEEN 0 AND 9999) DEFAULT 0, "
					+ "FontSize SMALLINT NOT NULL CHECK (FontSize BETWEEN 1 AND 99) DEFAULT 14, "
					+ "Password VARCHAR(300) NOT NULL, "
					+ "PopupWidth SMALLINT CHECK (PopupWidth BETWEEN 100 AND 9999) DEFAULT 300, "
					+ "ColorByCoder SMALLINT NOT NULL CHECK (ColorByCoder BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PopupDecoration SMALLINT NOT NULL CHECK (PopupDecoration BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PopupAutoComplete SMALLINT NOT NULL CHECK (PopupAutoComplete BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionAddDocuments SMALLINT NOT NULL CHECK (PermissionAddDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditDocuments SMALLINT NOT NULL CHECK (PermissionEditDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionDeleteDocuments SMALLINT NOT NULL CHECK (PermissionDeleteDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionImportDocuments SMALLINT NOT NULL CHECK (PermissionImportDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionAddStatements SMALLINT NOT NULL CHECK (PermissionAddStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditStatements SMALLINT NOT NULL CHECK (PermissionEditStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionDeleteStatements SMALLINT NOT NULL CHECK (PermissionDeleteStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditAttributes SMALLINT NOT NULL CHECK (PermissionEditAttributes BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditRegex SMALLINT NOT NULL CHECK (PermissionEditRegex BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditStatementTypes SMALLINT NOT NULL CHECK (PermissionEditStatementTypes BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PermissionEditCoders SMALLINT NOT NULL CHECK (PermissionEditCoders BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PermissionViewOthersDocuments SMALLINT NOT NULL CHECK (PermissionViewOthersDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditOthersDocuments SMALLINT NOT NULL CHECK (PermissionEditOthersDocuments BETWEEN 0 AND 1) DEFAULT 0, "
					+ "PermissionViewOthersStatements SMALLINT NOT NULL CHECK (PermissionViewOthersStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditOthersStatements SMALLINT NOT NULL CHECK (PermissionEditOthersStatements BETWEEN 0 AND 1) DEFAULT 0);");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Title VARCHAR(5000) NOT NULL, "
					+ "Text TEXT NOT NULL, "
					+ "Coder INT NOT NULL REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "Author VARCHAR(5000) NOT NULL DEFAULT '', "
					+ "Source VARCHAR(5000) NOT NULL DEFAULT '', "
					+ "Section VARCHAR(5000) NOT NULL DEFAULT '', "
					+ "Notes TEXT NOT NULL DEFAULT '', "
					+ "Type VARCHAR(5000) NOT NULL DEFAULT '', "
					+ "Date BIGINT NOT NULL);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Label VARCHAR(5000) NOT NULL, "
					+ "Red SERIAL NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SERIAL NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SERIAL NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Variable VARCHAR(500) NOT NULL, "
					+ "DataType VARCHAR(200) NOT NULL DEFAULT 'short text', "
					+ "StatementTypeId INT NOT NULL CHECK(StatementTypeId > 0) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (Variable, StatementTypeId));");
			s.add("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label VARCHAR(2000) PRIMARY KEY, "
					+ "Red SERIAL NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SERIAL NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SERIAL NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Coder INT NOT NULL CHECK(Coder > 0 AND Coder != OtherCoder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "OtherCoder INT NOT NULL CHECK(OtherCoder > 0 AND OtherCoder != Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "viewStatements INT NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editStatements INT NOT NULL DEFAULT 1 CHECK(editStatements BETWEEN 0 AND 1), "
					+ "viewDocuments INT NOT NULL DEFAULT 1 CHECK(viewDocuments BETWEEN 0 AND 1), "
					+ "editDocuments INT NOT NULL DEFAULT 1 CHECK(editDocuments BETWEEN 0 AND 1));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementTypeId INT NOT NULL CHECK(StatementTypeId > 0) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "DocumentId INT NOT NULL CHECK(DocumentId > 0) REFERENCES DOCUMENTS(ID) ON DELETE CASCADE, "
					+ "Start BIGINT NOT NULL CHECK(Start >= 0 AND Start < Stop), "
					+ "Stop BIGINT NOT NULL CHECK(Stop >= 0 AND Stop > Start), "
					+ "Coder INT NOT NULL CHECK(Coder > 0) REFERENCES CODERS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTLINKS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "SourceId INT NOT NULL CHECK(SourceId > 0 AND SourceId != TargetId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "TargetId INT NOT NULL CHECK(TargetId > 0 AND TargetId != SourceId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS DATABOOLEAN("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "Value INT NOT NULL DEFAULT 1 CHECK(Value BETWEEN 0 AND 1), "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "Value INT NOT NULL DEFAULT 0, "
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
					+ "Value TEXT NOT NULL DEFAULT '', "
					+ "UNIQUE (StatementId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "Value TEXT NOT NULL DEFAULT '', "
					+ "Red SERIAL NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SERIAL NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SERIAL NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "ChildOf INT CHECK(ChildOf > 0 AND ChildOf != ID) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (VariableId, Value));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEVARIABLES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "MetaVariable TEXT NOT NULL, "
					+ "UNIQUE (VariableId, MetaVariable));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEMETADATA("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "AttributeId INT NOT NULL CHECK(AttributeId > 0) REFERENCES ATTRIBUTES(ID) ON DELETE CASCADE, "
					+ "MetaVariableId INT NOT NULL CHECK(MetaVariableId > 0) REFERENCES ATTRIBUTEVARIABLES(ID) ON DELETE CASCADE, "
					+ "Value TEXT NOT NULL DEFAULT '', "
					+ "UNIQUE (AttributeId, MetaVariableId));");
		}
		// fill default data into the tables (Admin coder, settings, statement types)
		s.add("INSERT INTO CODERS (ID, Name, Red, Green, Blue, Password, PermissionEditStatementTypes, PermissionEditCoders, PermissionEditOthersDocuments, PermissionEditOthersStatements) VALUES (1, 'Admin', 255, 255, 0, '" + encryptedAdminPassword + "', 1, 1, 1, 1);");
		s.add("INSERT INTO SETTINGS (Property, Value) VALUES ('version', '" + Dna.version + "');");
		s.add("INSERT INTO SETTINGS (Property, Value) VALUES ('date', '" + Dna.date + "');");
		s.add("INSERT INTO STATEMENTTYPES (ID, Label, Red, Green, Blue) VALUES (1, 'DNA Statement', 239, 208, 51);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType, StatementTypeId) VALUES(1, 'person', 'short text', 1);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType, StatementTypeId) VALUES(2, 'organization', 'short text', 1);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType, StatementTypeId) VALUES(3, 'concept', 'short text', 1);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType, StatementTypeId) VALUES(4, 'agreement', 'boolean', 1);");
		s.add("INSERT INTO STATEMENTTYPES (ID, Label, Red, Green, Blue) VALUES (2, 'Annotation', 211, 211, 211);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType, StatementTypeId) VALUES(5, 'note', 'long text', 2);");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, MetaVariable) VALUES (1, 'Type');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, MetaVariable) VALUES (2, 'Type');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, MetaVariable) VALUES (3, 'Type');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, MetaVariable) VALUES (1, 'Alias');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, MetaVariable) VALUES (2, 'Alias');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, MetaVariable) VALUES (3, 'Alias');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, MetaVariable) VALUES (1, 'Notes');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, MetaVariable) VALUES (2, 'Notes');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, MetaVariable) VALUES (3, 'Notes');");
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

	
	/* =========================================================================
	 * Coders
	 * ====================================================================== */

	/**
	 * Retrieve a coder based on its ID.
	 * 
	 * @param coderId  The ID of the coder to be retrieved from the database.
	 * @return         The coder to be retrieved, as a {@link model.Coder
	 *   Coder} object.
	 * 
	 * @category coder
	 */
	public Coder getCoder(int coderId) {
		Coder c = null;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT * FROM CODERS WHERE ID = ?;");
				PreparedStatement s2 = conn.prepareStatement("SELECT * FROM CODERRELATIONS WHERE Coder = ?;")) {
			s1.setInt(1, coderId);
			ResultSet rs1 = s1.executeQuery();
			while (rs1.next()) {
				s2.setInt(1, coderId);
				ResultSet rs2 = s2.executeQuery();
				HashMap<Integer, CoderRelation> map = new HashMap<Integer, CoderRelation>();
				while (rs2.next()) {
					map.put(rs2.getInt("OtherCoder"),
							new CoderRelation(
									rs2.getInt("viewDocuments") == 1,
									rs2.getInt("viewStatements") == 1,
									rs2.getInt("editDocuments") == 1,
									rs2.getInt("editStatements") == 1));
				}
			    c = new Coder(coderId,
			    		rs1.getString("Name"),
			    		rs1.getInt("Red"),
			    		rs1.getInt("Green"),
			    		rs1.getInt("Blue"),
			    		rs1.getInt("Refresh"),
			    		rs1.getInt("FontSize"),
			    		rs1.getInt("PopupWidth"),
			    		rs1.getInt("ColorByCoder") == 1,
			    		rs1.getInt("PopupDecoration") == 1,
			    		rs1.getInt("popupAutoComplete") == 1,
			    		rs1.getInt("PermissionAddDocuments") == 1,
			    		rs1.getInt("PermissionEditDocuments") == 1,
			    		rs1.getInt("PermissionDeleteDocuments") == 1,
			    		rs1.getInt("PermissionImportDocuments") == 1,
			    		rs1.getInt("PermissionAddStatements") == 1,
			    		rs1.getInt("PermissionEditStatements") == 1,
			    		rs1.getInt("PermissionDeleteStatements") == 1,
			    		rs1.getInt("PermissionEditAttributes") == 1,
			    		rs1.getInt("PermissionEditRegex") == 1,
			    		rs1.getInt("PermissionEditStatementTypes") == 1,
			    		rs1.getInt("PermissionEditCoders") == 1,
			    		rs1.getInt("PermissionViewOthersDocuments") == 1,
			    		rs1.getInt("PermissionEditOthersDocuments") == 1,
			    		rs1.getInt("PermissionViewOthersStatements") == 1,
			    		rs1.getInt("PermissionEditOthersStatements") == 1,
			    		map);
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
	 * Retrieve a list of coders in the database.
	 * 
	 * @return An {@link java.util.ArrayList ArrayList} of {@link model.Coder
	 *   Coder} objects.
	 * 
	 * @category coder
	 */
	public ArrayList<Coder> getCoders() {
		ArrayList<Coder> coders = new ArrayList<Coder>();
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT * FROM Coders;");
				PreparedStatement s2 = conn.prepareStatement("SELECT * FROM CODERRELATIONS WHERE Coder = ?;")) {
        	ResultSet rs1 = s1.executeQuery();
        	while (rs1.next()) {
        		s2.setInt(1, rs1.getInt("ID"));
				ResultSet rs2 = s2.executeQuery();
				HashMap<Integer, CoderRelation> map = new HashMap<Integer, CoderRelation>();
				while (rs2.next()) {
					map.put(rs2.getInt("OtherCoder"),
							new CoderRelation(
									rs2.getInt("viewDocuments") == 1,
									rs2.getInt("viewStatements") == 1,
									rs2.getInt("editDocuments") == 1,
									rs2.getInt("editStatements") == 1));
				}
            	coders.add(new Coder(rs1.getInt("ID"),
            			rs1.getString("Name"),
			    		rs1.getInt("Red"),
			    		rs1.getInt("Green"),
			    		rs1.getInt("Blue"),
			    		rs1.getInt("Refresh"),
			    		rs1.getInt("FontSize"),
			    		rs1.getInt("PopupWidth"),
			    		rs1.getInt("ColorByCoder") == 1,
			    		rs1.getInt("PopupDecoration") == 1,
			    		rs1.getInt("popupAutoComplete") == 1,
			    		rs1.getInt("PermissionAddDocuments") == 1,
			    		rs1.getInt("PermissionEditDocuments") == 1,
			    		rs1.getInt("PermissionDeleteDocuments") == 1,
			    		rs1.getInt("PermissionImportDocuments") == 1,
			    		rs1.getInt("PermissionAddStatements") == 1,
			    		rs1.getInt("PermissionEditStatements") == 1,
			    		rs1.getInt("PermissionDeleteStatements") == 1,
			    		rs1.getInt("PermissionEditAttributes") == 1,
			    		rs1.getInt("PermissionEditRegex") == 1,
			    		rs1.getInt("PermissionEditStatementTypes") == 1,
			    		rs1.getInt("PermissionEditCoders") == 1,
			    		rs1.getInt("PermissionViewOthersDocuments") == 1,
			    		rs1.getInt("PermissionEditOthersDocuments") == 1,
			    		rs1.getInt("PermissionViewOthersStatements") == 1,
			    		rs1.getInt("PermissionEditOthersStatements") == 1,
			    		map));
            }
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to retrieve coders from the database.",
        			"Attempted to retrieve all coders from the database. Check your connection.",
        			e);
        	Dna.logger.log(l);
		}
		return coders;
	}

	/**
	 * Set a new font size for a coder.
	 * 
	 * @param coderId   ID of the coder in the database.
	 * @param fontSize  New font size, between 1 and 99.
	 */
	public void setCoderFontSize(int coderId, int fontSize) {
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("UPDATE CODERS SET FontSize = ? WHERE ID = ?;")) {
        	s.setInt(1, fontSize);
        	s.setInt(2,  coderId);
        	s.executeUpdate();
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to update font size for Coder " + coderId + " in the database.",
        			"Attempted to set a new font size for Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
		updateActiveCoder();
	}

	/**
	 * Update a coder's color by coder setting, i.e., write into the database
	 * whether the statements in the text should be painted according to the
	 * color of the respective coder (= 1) or the color of the respective
	 * statement type (= 0).
	 * 
	 * @param coderId       ID of the coder in the database.
	 * @param colorByCoder  Color statements in the text by coder color?
	 */
	public void setColorByCoder(int coderId, boolean colorByCoder) {
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("UPDATE CODERS SET ColorByCoder = ? WHERE ID = ?;")) {
			int c = 0;
			if (colorByCoder == true) {
				c = 1;
			}
        	s.setInt(1, c);
        	s.setInt(2,  coderId);
        	s.executeUpdate();
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to update color by coder setting for Coder " + coderId + " in the database.",
        			"Attempted to update the color setting that is used for painting statements in the text (statement type color or coder color) for Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
		updateActiveCoder();
	}

	/**
	 * Set a new popup window width for a coder.
	 * 
	 * @param coderId     ID of the coder in the database.
	 * @param popupWidth  New popup window width, between 100 and 9999.
	 */
	public void setCoderPopupWidth(int coderId, int popupWidth) {
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("UPDATE CODERS SET PopupWidth = ? WHERE ID = ?;")) {
        	s.setInt(1, popupWidth);
        	s.setInt(2,  coderId);
        	s.executeUpdate();
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to update popup window width for Coder " + coderId + " in the database.",
        			"Attempted to set a new short text field display width for statement popup windows for Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
		updateActiveCoder();
	}

	/**
	 * Update window decoration setting for a coder.
	 * 
	 * @param coderId     ID of the coder in the database.
	 * @param decoration  boolean value indicating whether popup windows should
	 *   have buttons and a dialog frame for the user.
	 */
	public void setCoderPopupDecoration(int coderId, boolean decoration) {
		int d = 0;
		if (decoration == true) {
			d = 1;
		}
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("UPDATE CODERS SET PopupDecoration = ? WHERE ID = ?;")) {
        	s.setInt(1, d);
        	s.setInt(2,  coderId);
        	s.executeUpdate();
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to update popup decoration setting for Coder " + coderId + " in the database.",
        			"Attempted to update window decoration settings for statement popup windows for Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
		updateActiveCoder();
	}

	/**
	 * Update popup autocomplete setting for a coder.
	 * 
	 * @param coderId       ID of the coder in the database.
	 * @param autoComplete  boolean value indicating whether autocomplete should
	 *   be set as active for the coder.
	 */
	public void setCoderPopupAutoComplete(int coderId, boolean autoComplete) {
		int a = 0;
		if (autoComplete == true) {
			a = 1;
		}
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("UPDATE CODERS SET PopupAutoComplete = ? WHERE ID = ?;")) {
        	s.setInt(1, a);
        	s.setInt(2,  coderId);
        	s.executeUpdate();
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to update popup auto-complete setting for Coder " + coderId + " in the database.",
        			"Attempted to update auto-completion settings for text fields in statement popup windows for Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
		updateActiveCoder();
	}
	
	/**
	 * Authenticate a coder. Check if a user-provided clear-text password for
	 * the current coder matches the hash of the password stored for the coder
	 * in the database.
	 * 
	 * @param clearPassword Clear-text password provided by the coder.
	 * @return              boolean value: {@code true} if the password matches,
	 *   {@code false} if not.
	 * 
	 * @category coder
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

	
	/* =========================================================================
	 * Documents
	 * ====================================================================== */

	/**
	 * Add a batch of documents to the database.
	 * 
	 * @param documents An {@link java.util.ArrayList ArrayList} of
	 *   {@link model.Document Document} objects, containing the documents to
	 *   be added to the database.
	 * 
	 * @category document
	 */
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
	
	/**
	 * Count the number of documents. Use an SQL query to get the number of rows
	 * in the {@code DOCUMENTS} table of the database.
	 * 
	 * @return The number of documents.
	 * 
	 * @category document
	 */
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
	 * Check for an array of documents whether any of them contains a statement.
	 * Used to determine in the {@link gui.DocumentEditor DocumentEditor}
	 * dialog whether the text field should be editable.
	 * 
	 * @param documentIds  An array of document IDs.
	 * @return             boolean value indicating the presence of statements.
	 * 
	 * @category document
	 */
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
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to count number of statements in document selection.",
					"Attempted to count how many statements are contained in each of the " + documentIds.length + " selected documents, but something went wrong while accessing the database. Most likely this means that the document table shows misleading counts, but there should be no other negative consequences.",
					e);
			Dna.logger.log(l);
		}
		return contains;
	}
	
	/**
	 * Get documents for a batch of document IDs. The data can be displayed and
	 * edited in a {@link gui.DocumentEditor DocumentEditor} dialog. The
	 * documents do not contain any statements.
	 * 
	 * @param documentIds  An array of document IDs for which the data should be
	 *   queried.
	 * @return             An {@link java.util.ArrayList ArrayList} of
	 *   {@link model.Document Document} objects, containing the documents and
	 *   their meta-data.
	 * 
	 * @category document
	 */
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

	/**
	 * Get the document text for a specified document ID.
	 * 
	 * @param documentId  The ID of a document.
	 * @return            A String representing the document text.
	 * 
	 * @category document
	 */
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
	
	/**
	 * Query the database for unique values of the author, source, section, and
	 * type meta-data fields of all documents in the database. These will be
	 * used to populate JComboBoxes with possible choices for new documents or
	 * altering existing documents.
	 * 
	 * @return An {@link SqlResults} object with a connection, statement, and
	 *   result set.
	 */
	public SqlResults getDocumentFieldResultSet() {
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement s = null;
		String sql = "SELECT DISTINCT * " + 
				"FROM (" + 
				"SELECT 'Author' AS Field, Author as Value FROM DOCUMENTS " + 
				"UNION ALL " + 
				"SELECT 'Source' AS Field, Source as Value FROM DOCUMENTS " + 
				"UNION ALL " + 
				"SELECT 'Section' AS Field, Section as Value FROM DOCUMENTS " + 
				"UNION ALL " + 
				"SELECT 'Type' AS Field, Type as Value FROM DOCUMENTS) " + 
				"WHERE Field IS NOT NULL ORDER BY Field, Value;";
		try {
			conn = getDataSource().getConnection();
			s = conn.prepareStatement(sql);
			rs = s.executeQuery();
		} catch (SQLException e) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"[SQL] Could not retrieve document meta-data fields from database.",
					"The document editor swing worker tried to retrieve all unique values for the selected documents' authors, sources, sections, and types from the database to display them as possible choices in the combo boxes for these variables, but the data could not be retrieved.",
					e);
			Dna.logger.log(le);
		} finally {
			// nothing gets closed here because the results would no longer be valid
		}
		SqlResults sr = new SqlResults(rs, s, conn);
		return sr;
	}
	
	/**
	 * Query the database for shallow representations of all documents (i.e.,
	 * the documents without their text or any contained statements but with
	 * their associated statement frequencies) for display in a document table.
	 * 
	 * @return An {@link SqlResults} object.
	 */
	public SqlResults getTableDocumentResultSet() {
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement tableStatement = null;
		try {
			conn = getDataSource().getConnection();
			tableStatement = conn.prepareStatement("SELECT D.ID, Title, (SELECT COUNT(ID) FROM STATEMENTS WHERE DocumentId = D.ID) AS Frequency, C.ID AS CoderId, Name AS CoderName, Red, Green, Blue, Date, Author, Source, Section, Type, Notes FROM CODERS C INNER JOIN DOCUMENTS D ON D.Coder = C.ID;");
			rs = tableStatement.executeQuery();
		} catch (SQLException e) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"[SQL] Could not retrieve documents from database.",
					"The document table model swing worker tried to retrieve all documents from the database to display them in the document table, but some or all documents could not be retrieved. The document table may be incomplete.",
					e);
			Dna.logger.log(le);
		} finally {
			// nothing gets closed here because the results would no longer be valid
		}
		SqlResults s = new SqlResults(rs, tableStatement, conn);
		return s;
	}
	
	/**
	 * <p>Update a batch of documents at once. The text and meta-data for the
	 * document IDs are provided as arguments. The information from the
	 * arguments is replaced in all the documents supplied by their document IDs
	 * in the database, i.e., the title, author etc. is going to be identical
	 * across all selected documents after the updating is complete.</p>
	 * <p>The arguments can contain wildcards that are replaced by the
	 * respective field from the document in the database. For example, {@code
	 * %author} reads the author field (before replacement) from the database
	 * and replaces the String {@code %author} by the actual contents of the
	 * author field in any field it is used in. Valid wildcards are the
	 * following partial Strings:</p>
	 * <dl>
	 *   <dt>{@code %title}</dt>
	 *   <dd>The title field of the document.</dd>
	 *   <dt>{@code %text}</dt>
	 *   <dd>The text of the document.</dd>
	 *   <dt>{@code %author}</dt>
	 *   <dd>The author field of the document.</dd>
	 *   <dt>{@code %source}</dt>
	 *   <dd>The source field of the document.</dd>
	 *   <dt>{@code %section}</dt>
	 *   <dd>The section field of the document.</dd>
	 *   <dt>{@code %type}</dt>
	 *   <dd>The type field of the document.</dd>
	 *   <dt>{@code %notes}</dt>
	 *   <dd>The notes field of the document.</dd>
	 *   <dt>{@code %day}</dt>
	 *   <dd>The day from the date-time field of the document.</dd>
	 *   <dt>{@code %month}</dt>
	 *   <dd>The month from the date-time field of the document.</dd>
	 *   <dt>{@code %year}</dt>
	 *   <dd>The year from the date-time field of the document.</dd>
	 *   <dt>{@code %hour}</dt>
	 *   <dd>The hour from the date-time field of the document.</dd>
	 *   <dt>{@code %minute}</dt>
	 *   <dd>The minute from the date-time field of the document.</dd>
	 * </dl>
	 * 
	 * @param documentIds An array of document IDs for which the contents should
	 *   be replaced in the database.
	 * @param title       A new title.
	 * @param text        A new text.
	 * @param author      A new author.
	 * @param source      A new source.
	 * @param section     A new section.
	 * @param type        A new type.
	 * @param notes       A new notes String.
	 * @param dateTime    A new date-time stamp (as a {@link
	 *   java.time.LocalDateTime LocalDateTime} object).
	 * 
	 * @category document
	 */
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

	/**
	 * Delete documents from the database, given an array of document IDs.
	 * 
	 * @param documentIds  An array of document IDs to be deleted.
	 * 
	 * @category document
	 */
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

	
	/* =========================================================================
	 * Statements
	 * ====================================================================== */

	/**
	 * Add a statement (with variable values) to the database.
	 * 
	 * @param statement   A {@link model.Statement Statement} object, including
	 *   the values for the different variables.
	 * @param documentId  The ID of the document in which the statement is
	 *   nested.
	 * 
	 * @category statement
	 */
	public void addStatement(Statement statement, int documentId) {
		int statementId = -1, attributeId = -1;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("INSERT INTO STATEMENTS (StatementTypeId, DocumentId, Start, Stop, Coder) VALUES (?, ?, ?, ?, ?);");
				PreparedStatement s2 = conn.prepareStatement("INSERT INTO DATASHORTTEXT (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s3 = conn.prepareStatement("INSERT INTO DATALONGTEXT (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s4 = conn.prepareStatement("INSERT INTO DATAINTEGER (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s5 = conn.prepareStatement("INSERT INTO DATABOOLEAN (StatementId, VariableId, Value) VALUES (?, ?, ?);");
				PreparedStatement s6 = conn.prepareStatement("INSERT INTO ATTRIBUTES (VariableId, Value, Red, Green, Blue) VALUES (?, ?, ?, ?, ?);");
				PreparedStatement s7 = conn.prepareStatement("SELECT ID FROM ATTRIBUTES WHERE VariableId = ? AND Value = ?;");
				PreparedStatement s8 = conn.prepareStatement("SELECT ID, MetaVariable FROM ATTRIBUTEVARIABLES WHERE VariableId = ?;");
				PreparedStatement s9 = conn.prepareStatement("INSERT INTO ATTRIBUTEMETADATA (AttributeId, MetaVariableId, Value) VALUES (?, ?, ?);");
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
			s1.setInt(5, statement.getCoderId());
			s1.executeUpdate();
			if (s1.getGeneratedKeys().next()) {
				statementId = s1.getGeneratedKeys().getInt(1);
			}
			l = new LogEvent(Logger.MESSAGE,
					"[SQL]  ├─ Transaction: Row with ID " + statementId + " added to the STATEMENTS table.",
					"Added a row to the STATEMENTS table during the transaction. The new statement has ID " + statementId + ".");
			Dna.logger.log(l);
			for (int i = 0; i < statement.getValues().size(); i++) {
				if (statement.getValues().get(i).getDataType().equals("short text")) {
					
					// first, try to create an attribute and catch error if it already exists
					int variableId = statement.getValues().get(i).getVariableId();
					s6.setInt(1, variableId);
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
					try {
						s6.executeUpdate();
						l = new LogEvent(Logger.MESSAGE,
								"[SQL]  ├─ Transaction: Added \"" + value + "\" to the ATTRIBUTES table.",
								"Added a row with value \"" + value + "\" to the ATTRIBUTES table during the transaction.");
						Dna.logger.log(l);
					} catch (SQLException e2) {
						if (e2.getMessage().contains("UNIQUE constraint failed")) {
							l = new LogEvent(Logger.MESSAGE,
									"[SQL]  ├─ Transaction: Value \"" + value + "\" was already present in the ATTRIBUTES table.",
									"A row with value \"" + value + "\" did not have to be added to the ATTRIBUTES table during the transaction because it was already present.");
							Dna.logger.log(l);
						} else {
							l = new LogEvent(Logger.WARNING,
									"[SQL]  ├─ Failed to add value \"" + value + "\" to the ATTRIBUTES table.",
									"Failed to add value \"" + value + "\" to the ATTRIBUTES table. The next step will check if the attribute is already there. If so, no problem. If not, there will be another log event with an error message.",
									e2);
							Dna.logger.log(l);
						}
					}
					
					// find the attribute ID for the attribute that was just added (or that may have already existed)
					attributeId = -1;
					s7.setInt(1, variableId);
					s7.setString(2, value);
					r = s7.executeQuery();
					while (r.next()) {
						attributeId = r.getInt("ID");
					}
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Attribute ID identified as " + attributeId + ".",
							"The attribute for value \"" + value + "\", which was added to, or identified in, the ATTRIBUTES table during the transaction, has ID " + attributeId + ".");
					Dna.logger.log(l);
					
					// find attribute meta variable IDs for the attribute and insert new entries to the ATTRIBUTEMETADATA table (catch errors if they already exist)
					s8.setInt(1, variableId); // set variable ID to find all meta variables by ID corresponding to the attribute
					r = s8.executeQuery();
					while (r.next()) {
						try {
							s9.setInt(1, attributeId); // attribute ID
							s9.setInt(2, r.getInt("ID")); // meta variable ID
							s9.setString(3, ""); // put an empty value into the meta variable field initially
							s9.executeUpdate();
							l = new LogEvent(Logger.MESSAGE,
									"[SQL]  ├─ Transaction: Added meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " to the ATTRIBUTEMETADATA table.",
									"Added meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " to the ATTRIBUTEMETADATA table during the transaction.");
							Dna.logger.log(l);
						} catch (Exception e2) {
							if (e2.getMessage().contains("UNIQUE constraint failed")) {
								l = new LogEvent(Logger.MESSAGE,
										"[SQL]  ├─ Transaction: A value for meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " was already present in the ATTRIBUTEMETADATA table.",
										"A row for meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " did not have to be added to the ATTRIBUTEMETADATA table during the transaction because it was already present.");
								Dna.logger.log(l);
							} else {
								l = new LogEvent(Logger.WARNING,
										"[SQL]  ├─ Failed to add a new value for meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " to the ATTRIBUTEMETADATA table.",
										"Failed to add a new value for meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " to the ATTRIBUTEMETADATA table. The next step will check if the entry is already there. If so, no problem. If not, there will be another log event with an error message.",
										e2);
								Dna.logger.log(l);
							}
						}
					}
					
					// finally, write into the DATASHORTTEXT table
					s2.setInt(1, statementId);
					s2.setInt(2, statement.getValues().get(i).getVariableId());
					s2.setInt(3, attributeId);
					s2.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added a value to the DATASHORTTEXT table for Variable " + statement.getValues().get(i).getVariableId() + ".",
							"Added a row with attribute ID " + attributeId + " for Variable " + statement.getValues().get(i).getVariableId() + " to the DATASHORTTEXT table during the transaction.");
					Dna.logger.log(l);
				} else if (statement.getValues().get(i).getDataType().equals("long text")) {
					s3.setInt(1, statementId);
					s3.setInt(2, statement.getValues().get(i).getVariableId());
					s3.setString(3, (String) statement.getValues().get(i).getValue());
					s3.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added a value to the DATALONGTEXT table for Variable " + statement.getValues().get(i).getVariableId() + ".",
							"Added a row for Variable " + statement.getValues().get(i).getVariableId() + " to the DATALONGTEXT table during the transaction.");
					Dna.logger.log(l);
				} else if (statement.getValues().get(i).getDataType().equals("integer")) {
					s4.setInt(1, statementId);
					s4.setInt(2, statement.getValues().get(i).getVariableId());
					s4.setInt(3, (int) statement.getValues().get(i).getValue());
					s4.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added a value to the DATAINTEGER table for Variable " + statement.getValues().get(i).getVariableId() + ".",
							"Added a row with Value " + (int) statement.getValues().get(i).getValue() + " for Variable " + statement.getValues().get(i).getVariableId() + " to the DATAINTEGER table during the transaction.");
					Dna.logger.log(l);
				} else if (statement.getValues().get(i).getDataType().equals("boolean")) {
					s5.setInt(1, statementId);
					s5.setInt(2, statement.getValues().get(i).getVariableId());
					s5.setInt(3, (int) statement.getValues().get(i).getValue());
					s5.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added a value to the DATABOOLEAN table for Variable " + statement.getValues().get(i).getVariableId() + ".",
							"Added a row with Value " + (int) statement.getValues().get(i).getValue() + " for Variable " + statement.getValues().get(i).getVariableId() + " to the DATABOOLEAN table during the transaction.");
					Dna.logger.log(l);
				}
			}
			conn.commit();
			l = new LogEvent(Logger.MESSAGE,
					"[SQL]  └─ Completed SQL transaction to add Statement " + statementId + ".",
					"Completed SQL transaction to add a new statement with ID " + statementId + " to Document " + documentId + ". The contents have been written into the database.");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL]  └─ Failed to add statement to Document " + documentId + ".",
					"Failed to add statement to Document " + documentId + ". Check the connection and database availability.",
					e);
			Dna.logger.log(l);
		}
	}

	/**
	 * Update the variable contents of a statement using new values.
	 * 
	 * @param statementId  The ID of the statement to be updated.
	 * @param values       An ArrayList of {@link model.Value Value} objects. They
	 *   are used to update each variable value in the statement.
	 * 
	 * @category statement
	 */
	public void updateStatement(int statementId, ArrayList<Value> values) {
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("UPDATE DATABOOLEAN SET Value = ? WHERE StatementId = ? AND VariableId = ?;");
				PreparedStatement s2 = conn.prepareStatement("UPDATE DATAINTEGER SET Value = ? WHERE StatementId = ? AND VariableId = ?;");
				PreparedStatement s3 = conn.prepareStatement("UPDATE DATALONGTEXT SET Value = ? WHERE StatementId = ? AND VariableId = ?;");
				PreparedStatement s4 = conn.prepareStatement("UPDATE DATASHORTTEXT SET Value = ? WHERE StatementId = ? AND VariableId = ?;");
				PreparedStatement s5 = conn.prepareStatement("INSERT INTO ATTRIBUTES (VariableId, Value, Red, Green, Blue) VALUES (?, ?, ?, ?, ?);");
				PreparedStatement s6 = conn.prepareStatement("SELECT ID FROM ATTRIBUTES WHERE VariableId = ? AND Value = ?;");
				PreparedStatement s7 = conn.prepareStatement("SELECT ID, MetaVariable FROM ATTRIBUTEVARIABLES WHERE VariableId = ?;");
				PreparedStatement s8 = conn.prepareStatement("INSERT INTO ATTRIBUTEMETADATA (AttributeId, MetaVariableId, Value) VALUES (?, ?, ?);");
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
							"[SQL]  ├─ Variable " + variableId + " in Statement " + statementId + " was updated in the transaction.",
							"Variable " + variableId + " (boolean) in Statement " + statementId + " was updated in the SQL transaction with value: " + (int) values.get(i).getValue() + ".");
					Dna.logger.log(e2);
				} else if (values.get(i).getDataType().equals("integer")) {
					s2.setInt(1, (int) values.get(i).getValue());
					s2.setInt(2, statementId);
					s2.setInt(3, variableId);
					s2.executeUpdate();
					LogEvent e2 = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Variable " + variableId + " in Statement " + statementId + " was updated in the transaction.",
							"Variable " + variableId + " (integer) in Statement " + statementId + " was updated in the SQL transaction with value: " + (int) values.get(i).getValue() + ".");
					Dna.logger.log(e2);
				} else if (values.get(i).getDataType().equals("long text")) {
					s3.setString(1, (String) values.get(i).getValue());
					s3.setInt(2, statementId);
					s3.setInt(3, variableId);
					s3.executeUpdate();
					LogEvent e2 = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Variable " + variableId + " in Statement " + statementId + " was updated in the transaction.",
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
						s5.executeUpdate();
						
						// new attribute has been created; now we have to get its ID
						s6.setInt(1, variableId);
						s6.setString(2, attribute.getValue());
						r = s6.executeQuery();
						while (r.next()) {
							attributeId = r.getInt(1);
						}
						LogEvent e2 = new LogEvent(Logger.MESSAGE,
								"[SQL]  ├─ Attribute with ID " + attributeId + " added to the transaction.",
								"An attribute with ID " + attributeId + " and value \"" + attribute.getValue() + "\" was created for variable ID " + variableId + " and added to the SQL transaction.");
						Dna.logger.log(e2);
						
						// since the attribute did not exist, we also need to add metavariable entries;
						// first get the IDs of the meta-variables, then add the values
						s7.setInt(1, variableId); // set variable ID to find all meta variables by ID corresponding to the variable
						r = s7.executeQuery();
						while (r.next()) {
							try {
								s8.setInt(1, attributeId); // attribute ID
								s8.setInt(2, r.getInt("ID")); // meta variable ID
								s8.setString(3, ""); // put an empty value into the meta variable field initially
								s8.executeUpdate();
								LogEvent l = new LogEvent(Logger.MESSAGE,
										"[SQL]  ├─ Transaction: Added meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " to the ATTRIBUTEMETADATA table.",
										"Added meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " to the ATTRIBUTEMETADATA table during the transaction.");
								Dna.logger.log(l);
							} catch (Exception e3) {
								if (e3.getMessage().contains("UNIQUE constraint failed")) {
									LogEvent l = new LogEvent(Logger.MESSAGE,
											"[SQL]  ├─ Transaction: A value for meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " was already present in the ATTRIBUTEMETADATA table.",
											"A row for meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " did not have to be added to the ATTRIBUTEMETADATA table during the transaction because it was already present.");
									Dna.logger.log(l);
								} else {
									LogEvent l = new LogEvent(Logger.WARNING,
											"[SQL]  ├─ Failed to add a new value for meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " to the ATTRIBUTEMETADATA table.",
											"Failed to add a new value for meta-variable \"" + r.getString("MetaVariable") + "\" for Attribute " + attributeId + " to the ATTRIBUTEMETADATA table. The next step will check if the entry is already there. If so, no problem. If not, there will be another log event with an error message.",
											e3);
									Dna.logger.log(l);
								}
							}
						}
					}

					// write the attribute ID as the value in the DATASHORTTEXT table
					s4.setInt(1, attributeId);
					s4.setInt(2, statementId);
					s4.setInt(3, variableId);
					s4.executeUpdate();
					LogEvent e2 = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Variable " + variableId + " in Statement " + statementId + " was updated in the transaction.",
							"Variable " + variableId + " (short text) in Statement " + statementId + " was updated in the SQL transaction with Attribute " + attributeId + ".");
					Dna.logger.log(e2);
				}
			}
			conn.commit();
			LogEvent e2 = new LogEvent(Logger.MESSAGE,
					"[SQL]  └─ Completed SQL transaction to update Statement " + statementId + ".",
					"Completed SQL transaction to update the variables in the statement with ID " + statementId + ". The contents have been written into the database.");
			Dna.logger.log(e2);
		} catch (SQLException e) {
			LogEvent e2 = new LogEvent(Logger.ERROR,
					"[SQL]  └─ Statement " + statementId + " could not be updated in the database.",
					"When the statement popup window for Statement " + statementId + " was closed, the contents for the different variables could not be saved into the database. The database still contains the old values before the contents were edited. Please double-check to make sure that the statement contains the right values for all variables. Check whether the database may be locked and close all programs other than DNA that are currently accessing the database before trying again.",
					e);
			Dna.logger.log(e2);
		}
	}
	
	/**
	 * Create a copy of a statement in the database.
	 * 
	 * @param statementId  The ID of the statement to be cloned.
	 * @param newCoderId   The ID of the coder who will own the statement copy.
	 * @return             The ID of the new (cloned) statement.
	 * 
	 * @category statement
	 */
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

	/**
	 * Get a statement from the database based on its ID.
	 * 
	 * @param statementId  The statement ID of the statement to be retrieved.
	 * @return             A {@link model.Statement Statement} with all relevant
	 *   values for the different variables.
	 * 
	 * @category statement
	 */
	public Statement getStatement(int statementId) {
		Statement statement = null;
		ArrayList<Value> values;
		int statementTypeId, variableId;
		String variable, dataType;
		Color aColor, sColor, cColor;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT STATEMENTS.ID AS StatementId, StatementTypeId, STATEMENTTYPES.Label AS StatementTypeLabel, STATEMENTTYPES.Red AS StatementTypeRed, STATEMENTTYPES.Green AS StatementTypeGreen, STATEMENTTYPES.Blue AS StatementTypeBlue, Start, Stop, STATEMENTS.Coder AS CoderId, CODERS.Name AS CoderName, CODERS.Red AS CoderRed, CODERS.Green AS CoderGreen, CODERS.Blue AS CoderBlue, DocumentId, DOCUMENTS.Date AS Date, SUBSTRING(DOCUMENTS.Text, Start + 1, Stop - Start) AS Text FROM STATEMENTS INNER JOIN CODERS ON STATEMENTS.Coder = CODERS.ID INNER JOIN STATEMENTTYPES ON STATEMENTS.StatementTypeId = STATEMENTTYPES.ID INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId WHERE StatementId = ?;");
				PreparedStatement s2 = conn.prepareStatement("SELECT ID, Variable, DataType FROM VARIABLES WHERE StatementTypeId = ?;");
				PreparedStatement s3 = conn.prepareStatement("SELECT A.ID AS AttributeId, StatementId, A.VariableId, DST.ID AS DataId, A.Value, Red, Green, Blue, ChildOf FROM DATASHORTTEXT AS DST LEFT JOIN ATTRIBUTES AS A ON A.ID = DST.Value AND A.VariableId = DST.VariableId WHERE DST.StatementId = ? AND DST.VariableId = ?;");
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
			            	Attribute attribute = new Attribute(r3.getInt("AttributeId"), r3.getString("Value"), aColor, r3.getInt("ChildOf"), true);
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
			    statement = new Statement(statementId,
			    		r1.getInt("Start"),
			    		r1.getInt("Stop"),
			    		statementTypeId,
			    		r1.getString("StatementTypeLabel"),
			    		sColor,
			    		r1.getInt("CoderId"),
			    		r1.getString("CoderName"),
			    		cColor,
			    		values,
			    		r1.getInt("DocumentId"),
			    		r1.getString("Text"),
			    		LocalDateTime.ofEpochSecond(r1.getLong("Date"), 0, ZoneOffset.UTC));
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

	/**
	 * Get all statements, or get all statements nested in a specific document.
	 * 
	 * @param documentId  The ID of the document for which statements should be
	 *   retrieved. If the document ID is {@code -1}, the statements from all
	 *   documents are retrieved.
	 * @return            An ArrayList of {@link model.Statement Statement}
	 *   objects, including their variable contents.
	 * 
	 * @category statement
	 */
	public ArrayList<Statement> getStatements(int documentId) {
		String where = "";
		String forDocument = "";
		if (documentId > -1) {
			where = " WHERE DocumentId = ?"; // for restricting the query to a single document
			forDocument = " for Document " + documentId; // for log messages at the end
		}
		String query = "SELECT STATEMENTS.ID AS StatementId, "
				+ "StatementTypeId, "
				+ "STATEMENTTYPES.Label AS StatementTypeLabel, "
				+ "STATEMENTTYPES.Red AS StatementTypeRed, "
				+ "STATEMENTTYPES.Green AS StatementTypeGreen, "
				+ "STATEMENTTYPES.Blue AS StatementTypeBlue, "
				+ "Start, "
				+ "Stop, "
				+ "STATEMENTS.Coder AS CoderId, "
				+ "CODERS.Name AS CoderName, "
				+ "CODERS.Red AS CoderRed, "
				+ "CODERS.Green AS CoderGreen, "
				+ "CODERS.Blue AS CoderBlue, "
				+ "DocumentId, "
				+ "DOCUMENTS.Date AS Date, "
				+ "SUBSTRING(DOCUMENTS.Text, Start + 1, Stop - Start) AS Text "
				+ "FROM STATEMENTS "
				+ "INNER JOIN CODERS ON STATEMENTS.Coder = CODERS.ID "
				+ "INNER JOIN STATEMENTTYPES ON STATEMENTS.StatementTypeId = STATEMENTTYPES.ID "
				+ "INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId" + where + " ORDER BY DOCUMENTS.DATE ASC;";
		ArrayList<Statement> statements = new ArrayList<Statement>();
		ArrayList<Value> values;
		int statementId, statementTypeId, variableId;
		String variable, dataType;
		Color aColor, sColor, cColor;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement(query);
				PreparedStatement s2 = conn.prepareStatement("SELECT ID, Variable, DataType FROM VARIABLES WHERE StatementTypeId = ?;");
				PreparedStatement s3 = conn.prepareStatement("SELECT A.ID AS AttributeId, StatementId, A.VariableId, DST.ID AS DataId, A.Value, Red, Green, Blue, ChildOf FROM DATASHORTTEXT AS DST LEFT JOIN ATTRIBUTES AS A ON A.ID = DST.Value AND A.VariableId = DST.VariableId WHERE DST.StatementId = ? AND DST.VariableId = ?;");
				PreparedStatement s4 = conn.prepareStatement("SELECT Value FROM DATALONGTEXT WHERE VariableId = ? AND StatementId = ?;");
				PreparedStatement s5 = conn.prepareStatement("SELECT Value FROM DATAINTEGER WHERE VariableId = ? AND StatementId = ?;");
				PreparedStatement s6 = conn.prepareStatement("SELECT Value FROM DATABOOLEAN WHERE VariableId = ? AND StatementId = ?;")) {
			ResultSet r1, r2, r3;
			if (documentId > -1) { // restrict to a single document if necessary
				s1.setInt(1, documentId);
			}
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
			            	Attribute attribute = new Attribute(r3.getInt("AttributeId"), r3.getString("Value"), aColor, r3.getInt("ChildOf"), true);
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
			    statements.add(new Statement(statementId,
			    		r1.getInt("Start"),
			    		r1.getInt("Stop"),
			    		statementTypeId,
			    		r1.getString("StatementTypeLabel"),
			    		sColor,
			    		r1.getInt("CoderId"),
			    		r1.getString("CoderName"),
			    		cColor,
			    		values,
			    		r1.getInt("DocumentId"),
			    		r1.getString("Text"),
			    		LocalDateTime.ofEpochSecond(r1.getLong("Date"), 0, ZoneOffset.UTC)));
			}
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] " + statements.size() + " statement(s) have been retrieved" + forDocument + ".",
					statements.size() + " statement(s) have been retrieved for" + forDocument + ".");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to retrieve statements" + forDocument + ".",
					"Attempted to retrieve all statements" + forDocument + " from the database, but something went wrong. You should double-check if the statements are all shown!",
					e);
			Dna.logger.log(l);
		}
		return statements;
	}

	/**
	 * Delete a statement from the database based on its ID.
	 * 
	 * @param statementId  ID of the statement to be deleted.
	 * 
	 * @category statement
	 */
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

	/**
	 * Delete statements from the database, given an array of statement IDs.
	 * 
	 * @param statementIds  An array of statement IDs to be deleted.
	 * 
	 * @category statement
	 */
	public void deleteStatements(int[] statementIds) {
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("DELETE FROM STATEMENTS WHERE ID = ?");
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			for (int i = 0; i < statementIds.length; i++) {
				s.setInt(1, statementIds[i]);
				s.executeUpdate();
			}
			conn.commit();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Deleted " + statementIds.length + " statements.",
					"Successfully deleted " + statementIds.length + " statements from the STATEMENTS table in the database. The transaction has been committed to the database.");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to delete statements from database.",
					"Attempted to remove " + statementIds.length + " statements from the STATEMENTS table in the database, but something went wrong. The transaction has been rolled back, and nothing has been removed.",
					e);
			Dna.logger.log(l);
		}
	}

	
	/* =========================================================================
	 * Attributes
	 * ====================================================================== */

	/**
	 * Get an array of all attributes corresponding to a variable.
	 *  
	 * @param variableId  The ID of the variable for which all attributes will
	 *   be retrieved.
	 * @return            An array of {@link dna.Attribute Attribute} objects.
	 * 
	 * @category attribute
	 */
	public Attribute[] getAttributes(int variableId) {
		ArrayList<Attribute> attributesList = new ArrayList<Attribute>();
		boolean inDatabase;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT ID, Value, Red, Green, Blue, ChildOf FROM ATTRIBUTES WHERE VariableId = ?;");
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
            	attributesList.add(new Attribute(r1.getInt("ID"), r1.getString("Value"), color, r1.getInt("ChildOf"), inDatabase));
            }
        	LogEvent e = new LogEvent(Logger.MESSAGE,
        			"[SQL] Retrieved " + attributesList.size() + " attribute(s) for Variable " + variableId + ".",
        			"Retrieved " + attributesList.size() + " attribute(s) from the database for Variable " + variableId + ".");
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

	
	/* =========================================================================
	 * Statement types
	 * ====================================================================== */

	/**
	 * Get an array list of all statement types in the database.
	 * 
	 * @return An ArrayList of {@link model.StatementType StatementType} objects.
	 * 
	 * @category statementtype
	 */
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

	
	/* =========================================================================
	 * Settings
	 * ====================================================================== */

	/**
	 * Get a setting for a specific coder from the SETTINGS database table.
	 * 
	 * @param coderId   ID of the coder for whom the setting was saved.
	 * @param property  The name of the setting that is to be retrieved.
	 * @return          A String of the setting value.
	 */
	/*
	public String getSetting(int coderId, String property) {
		String value = null;
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("SELECT Value FROM SETTINGS WHERE Property = ? AND Coder = ?;")) {
        	s.setString(1, property);
        	s.setInt(2, coderId);
			ResultSet r = s.executeQuery();
        	while (r.next()) {
            	value = r.getString("Value");
            }
        	LogEvent l = new LogEvent(Logger.MESSAGE,
        			"[SQL] Retrieved " + property + " setting from the database.",
        			"Queried the database for the setting \"" + property + "\" for Coder " + coderId + ". The value is: \"" + value + "\".");
        	Dna.logger.log(l);
		} catch (SQLException e1) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to retrieve setting from the database.",
					"Queried the database for the setting \"" + property + "\" for Coder " + coderId + ", but the query failed. Check database connection.",
					e1);
			Dna.logger.log(l);
		}
		return value;
	}
	*/

	/**
	 * Insert or update a setting for a coder in the SETTINGS table.
	 * 
	 * @param coderId   The ID of the coder to whom the setting applies.
	 * @param property  The name of the setting to be updated or inserted.
	 * @param value     The value of the setting to be updated or inserted.
	 */
	/*
	public void setSetting(int coderId, String property, String value) {
		String dbtype, sql;
		if (getConnectionProfile().getType().equals("mysql")) {
			dbtype = "MySQL";
			sql = "INSERT INTO SETTINGS (Property, Coder, Value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Value = ?;";
		} else {
			if (getConnectionProfile().getType().equals("sqlite")) {
				dbtype = "SQLite";
			} else {
				dbtype = "PostgreSQL";
			}
			sql = "INSERT INTO SETTINGS (Property, Coder, Value) VALUES (?, ?, ?) ON CONFLICT (Property, Coder) DO UPDATE SET Value = ?;";
		}
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement(sql)) {
			s.setString(1, property);
			s.setInt(2, coderId);
			s.setString(3, value);
			s.setString(4, value);
			s.executeUpdate();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Setting \"" + property + "\" has been updated in the database.",
					"Updated property \"" + property + "\" for Coder " + coderId + ". The new value is: \"" + value + "\".");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to update setting in the database.",
					"Attempted to update the setting \"" + property + "\" for Coder " + coderId + " in the SETTINGS table of the " + dbtype + " database, but the update failed.",
					e);
			Dna.logger.log(l);
		}
	}
	*/
}