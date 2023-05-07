package sql;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import gui.DocumentEditor;
import model.*;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.sqlite.SQLiteDataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;

/**
 * This class contains information on the database connection, the data source
 * for establishing connections, and methods for interacting with the database.
 */
public class Sql {

	/**
	 * The {@link sql.ConnectionProfile ConnectionProfile} to be used for
	 * connecting to a database.
	 */
	private ConnectionProfile cp;

	/**
	 * The {@link javax.sql.DataSource DataSource} to be used for connections.
	 */
	private DataSource ds;

	/**
	 * The active {@link model.Coder Coder} including permissions.
	 */
	private Coder activeCoder;


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
	 * @param existingDatabase Does the database exist and contain the required tables?
	 */
	public Sql(ConnectionProfile cp, boolean test, boolean existingDatabase) {
		this.setConnectionProfile(cp, test, existingDatabase);
	}

	/**
	 * Create an instance of the Sql class without an initial connection.
	 */
	public Sql() {
		this.setConnectionProfile(null, false, false);
	}

	/**
	 * Get the connection profile.
	 *
	 * @return A {@link sql.ConnectionProfile connectionProfile} object.
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
	 * @return Was the connection profile successfully set and the data source created?
	 */
	public boolean setConnectionProfile(ConnectionProfile cp, boolean test, boolean existingDatabase) {
		boolean success = false;
		this.cp = cp;
		if (cp == null) { // null connection
			ds = null;
			this.cp = null;
			this.activeCoder = null;
		} else if (cp.getType().equals("sqlite")) { // no user name and password needed for file-based database
			SQLiteDataSource sqds = new SQLiteDataSource();
			sqds.setUrl("jdbc:sqlite:" + cp.getUrl());
			sqds.setEnforceForeignKeys(true); // if this is not set, ON DELETE CASCADE won't work
			if (!existingDatabase || checkDatabaseVersion(sqds)) {
				ds = sqds;
				success = true;
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"[SQL] An SQLite DNA database has been opened as a data source.",
						"An SQLite DNA database has been opened as a data source.");
				Dna.logger.log(l);
			}
		} else if (cp.getType().equals("mysql") || cp.getType().equals("postgresql")) {
			HikariConfig config = new HikariConfig();
			config.setMaximumPoolSize(30);
			config.setMinimumIdle(5);
			config.setPassword(cp.getPassword());
			config.setUsername(cp.getUser());
			config.setJdbcUrl("jdbc:" + cp.getType() + "://" + cp.getUrl() + ":" + cp.getPort() + "/" + cp.getDatabaseName());
			if (cp.getType().equals("mysql")) {
				config.setDriverClassName("com.mysql.cj.jdbc.Driver");
			} else {
				config.setDriverClassName("org.postgresql.Driver");
			}
			try {
				HikariDataSource dsTest = new HikariDataSource(config);
				if (!existingDatabase || checkDatabaseVersion(dsTest)) {
					ds = dsTest;
					success = true;
					LogEvent l = new LogEvent(Logger.MESSAGE,
							"[SQL] A " + cp.getType() + " DNA database has been opened as a data source.",
							"A " + cp.getType() + " DNA database has been opened as a data source.");
					Dna.logger.log(l);
				}
			} catch (PoolInitializationException e) {
				LogEvent l = new LogEvent(Logger.ERROR,
		        		"[SQL] Database access denied. Failed to initialize connection pool.",
		        		"Database access denied. Failed to initialize connection pool.",
		        		e);
		        Dna.logger.log(l);
			}

		} else {
			LogEvent l = new LogEvent(Logger.ERROR,
	        		"[SQL] Failed to regognize database format.",
	        		"Attempted to open a database of type \"" + cp.getType() + "\", but the type does not seem to be supported.");
	        Dna.logger.log(l);
		}
		if (test == false && cp != null) {
			selectCoder(cp.getCoderId());
		}
		return success;
	}

	/**
	 * Check if a data source has been successfully set when instantiating the class. If not, this may indicate that the
	 * database version check failed.
	 *
	 * @return True if a data source has been set and false otherwise.
	 */
	public boolean hasDataSource() {
		return this.ds != null;
	}

	/**
	 * Check if a data source is compatible with the current DNA version by inspecting the version number saved in the
	 * SETTINGS table.
	 *
	 * @param dataSource The data source to be used and checked for compatibility.
	 * @return True if the database version is compatible with the current DNA version. False if it needs to be updated.
	 */
	private boolean checkDatabaseVersion(DataSource dataSource) {
		boolean compatible = true;
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement s1 = conn.prepareStatement("SELECT * FROM SETTINGS WHERE Property IN ('version', 'date');")) {
			ResultSet rs = s1.executeQuery();
			String property, version = "", date = "";
			while (rs.next()) {
				property = rs.getString("Property");
				if (property.equals("version")) {
					version = rs.getString("Value");
				} else if (property.equals("date")) {
					date = rs.getString("Value");
				}
			}
			if (version.startsWith("1") || version.startsWith("2") || version.startsWith("3.0")) {
				compatible = false;
				String msg = "";
				if (version.startsWith("1")) {
					msg = "Contents from databases that were created with DNA 1 can only be imported into the old DNA 2. See the release page online for old DNA 2 versions.";

				} else if (version.startsWith("2")) {
					msg = "Contents from databases that were created with DNA 2 can be imported into the current DNA version. To do so, create a new database, create coders that correspond to the coders in the old database (if required), and use the \"Import from DNA database\" dialog in the \"Documents\" menu.";
				} else if (version.startsWith("3.0")) {
					msg = "Contents from databases that were created with DNA 3.0 cannot be imported to the current DNA version at this time. Support for this will be added in the near future.";
				}
				LogEvent l = new LogEvent(Logger.ERROR,
						"[SQL] Wrong database version.",
						"You tried to open a database that was created with version " + version + " of DNA (release date: " + date + "). You are currently using DNA " + Dna.version + " (release date: " + Dna.date + "). The database version is incompatible with the DNA version. " + msg);
				Dna.logger.log(l);
			}
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to determine database version.",
					"Attempted to check if the database version is compatible with the DNA version, but failed to do so. If you do not see any other warnings or errors, you can probably ignore this message. If it happens often, consider filing an issue on GitHub.",
					e);
			Dna.logger.log(l);
		}
		return compatible;
	}

	/**
	 * Get the active coder.
	 *
	 * @return A {@link model.Coder Coder} object with all permissions.
	 */
	public Coder getActiveCoder() {
		return activeCoder;
	}

	/**
	 * Get the data source stored in the {@link sql.Sql Sql} object.
	 *
	 * @return A {@link javax.sql.DataSource DataSource} object.
	 */
	public DataSource getDataSource() {
		return ds;
	}

	/**
	 * Retrieve a coder from the database and set it as the active coder.
	 *
	 * @param coderId  The ID of the coder to be selected.
	 */
	public void selectCoder(int coderId) {
		if (coderId < 1) {
			setConnectionProfile(null, false, true); // not a connection test, so false
			this.activeCoder = null;
		} else {
			getConnectionProfile().setCoder(coderId);
			this.activeCoder = getCoder(coderId);
		}
	}

	/**
	 * Select a specified coder as the active coder.
	 *
	 * @param coder  The coder to be selected.
	 */
	public void selectCoder(Coder coder) {
		getConnectionProfile().setCoder(coder.getId());
		this.activeCoder = coder;
	}

	/**
	 * An interface that rolls back failed execution attempts of SQL connections
	 * in {@literal try}-with-resources headers. Use this as the last code line
	 * in any {@literal try}-with-resources header that uses SQL transactions.
	 */
	public interface SQLCloseable extends AutoCloseable {
	    /**
	     * Close the object, which rolls back any initiated transactions.
	     */
	    @Override public void close() throws SQLException;
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
					+ "Name TEXT NOT NULL CHECK (LENGTH(Name) < 191), "
					+ "Red INTEGER NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "Refresh INTEGER NOT NULL CHECK (Refresh BETWEEN 0 AND 9999) DEFAULT 0, "
					+ "FontSize INTEGER NOT NULL CHECK (FontSize BETWEEN 1 AND 99) DEFAULT 14, "
					+ "Password TEXT NOT NULL CHECK (LENGTH(Password) < 191), "
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
					+ "PermissionEditStatementTypes INTEGER NOT NULL CHECK (PermissionEditStatementTypes BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditCoders INTEGER NOT NULL CHECK (PermissionEditCoders BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditCoderRelations INTEGER NOT NULL CHECK (PermissionEditCoderRelations BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionViewOthersDocuments INTEGER NOT NULL CHECK (PermissionViewOthersDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditOthersDocuments INTEGER NOT NULL CHECK (PermissionEditOthersDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionViewOthersStatements INTEGER NOT NULL CHECK (PermissionViewOthersStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditOthersStatements INTEGER NOT NULL CHECK (PermissionEditOthersStatements BETWEEN 0 AND 1) DEFAULT 1);");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Title TEXT NOT NULL CHECK (LENGTH(Title) < 191), "
					+ "Text TEXT NOT NULL, "
					+ "Coder INTEGER, "
					+ "Author TEXT NOT NULL DEFAULT '' CHECK (LENGTH(Author) < 191), "
					+ "Source TEXT NOT NULL DEFAULT '' CHECK (LENGTH(Source) < 191), "
					+ "Section TEXT NOT NULL DEFAULT '' CHECK (LENGTH(Section) < 191), "
					+ "Notes TEXT NOT NULL DEFAULT '', "
					+ "Type TEXT NOT NULL DEFAULT '' CHECK (LENGTH(Type) < 191), "
					+ "Date INTEGER NOT NULL, "
					+ "Complete INTEGER NOT NULL DEFAULT 0 CHECK (Complete BETWEEN 0 AND 1), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Label TEXT NOT NULL CHECK (LENGTH(Label) < 191), "
					+ "Red INTEGER NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Variable TEXT NOT NULL CHECK (LENGTH(Variable) < 191), "
					+ "DataType TEXT NOT NULL CHECK (DataType = 'boolean' OR DataType = 'integer' OR DataType = 'long text' OR DataType = 'short text') DEFAULT 'short text', "
					+ "UNIQUE (Variable));");
			s.add("CREATE TABLE IF NOT EXISTS ROLES("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "RoleName TEXT NOT NULL CHECK (LENGTH(RoleName) < 191), "
					+ "StatementTypeId INTEGER, "
					+ "Position INTEGER NOT NULL DEFAULT 1 CHECK (Position > 0), "
					+ "NumMin INTEGER NOT NULL DEFAULT 1 CHECK (NumMin > -1), "
					+ "NumMax INTEGER NOT NULL DEFAULT 1, "
					+ "NumDefault INTEGER NOT NULL DEFAULT 1, "
					+ "Red INTEGER NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "DefaultVariableId INTEGER NOT NULL, "
					+ "CHECK (NumMax >= NumMin), "
					+ "CHECK (NumDefault >= NumMin), "
					+ "CHECK (NumMax >= NumDefault), "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(DefaultVariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (RoleName, StatementTypeId), "
					+ "UNIQUE (StatementTypeId, Position));");
			s.add("CREATE TABLE IF NOT EXISTS ROLEVARIABLELINKS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "RoleId INTEGER, "
					+ "VariableId INTEGER, "
					+ "FOREIGN KEY(RoleId) REFERENCES ROLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (RoleId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label TEXT PRIMARY KEY CHECK (LENGTH(Label) < 191), "
					+ "Red INTEGER NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Coder INTEGER CHECK(Coder > 0), "
					+ "OtherCoder INTEGER CHECK(OtherCoder > 0), "
					+ "viewStatements INTEGER NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editStatements INTEGER NOT NULL DEFAULT 1 CHECK(editStatements BETWEEN 0 AND 1), "
					+ "viewDocuments INTEGER NOT NULL DEFAULT 1 CHECK(viewDocuments BETWEEN 0 AND 1), "
					+ "editDocuments INTEGER NOT NULL DEFAULT 1 CHECK(editDocuments BETWEEN 0 AND 1), "
					+ "CHECK (Coder != OtherCoder), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(OtherCoder) REFERENCES CODERS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "StatementTypeId INTEGER, "
					+ "DocumentId INTEGER, "
					+ "Start INTEGER NOT NULL CHECK(Start >= 0), "
					+ "Stop INTEGER NOT NULL CHECK(Stop >= 0), "
					+ "Coder INTEGER, "
					+ "CHECK (Start < Stop), "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(DocumentId) REFERENCES DOCUMENTS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS DATABOOLEAN("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "RoleVariableLinkId INTEGER NOT NULL, "
					+ "Value INTEGER NOT NULL DEFAULT 1, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(RoleVariableLinkId) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "RoleVariableLinkId INTEGER NOT NULL, "
					+ "Value INTEGER NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(RoleVariableLinkId) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS ENTITIES("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "Value TEXT NOT NULL DEFAULT '' CHECK (LENGTH(Value) < 191), "
					+ "Red INTEGER DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green INTEGER DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue INTEGER DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "ChildOf INTEGER CHECK(ChildOf > 0), "
					+ "UNIQUE (VariableId, Value), "
					+ "FOREIGN KEY(ChildOf) REFERENCES ENTITIES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "RoleVariableLinkId INTEGER NOT NULL, "
					+ "Entity INTEGER NOT NULL, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(RoleVariableLinkId) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(Entity) REFERENCES ENTITIES(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "StatementId INTEGER NOT NULL, "
					+ "RoleVariableLinkId INTEGER NOT NULL, "
					+ "Value TEXT DEFAULT '', "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(RoleVariableLinkId) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEVARIABLES("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "VariableId INTEGER NOT NULL, "
					+ "AttributeVariable TEXT NOT NULL CHECK (LENGTH(AttributeVariable) < 191), "
					+ "UNIQUE(VariableId, AttributeVariable), "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEVALUES("
					+ "ID INTEGER PRIMARY KEY NOT NULL, "
					+ "EntityId INTEGER NOT NULL, "
					+ "AttributeVariableId INTEGER NOT NULL, "
					+ "AttributeValue TEXT NOT NULL DEFAULT '' CHECK (LENGTH(AttributeValue) < 191), "
					+ "UNIQUE (EntityId, AttributeVariableId), "
					+ "FOREIGN KEY(EntityId) REFERENCES ENTITIES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(AttributeVariableId) REFERENCES ATTRIBUTEVARIABLES(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTTAGS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Tag TEXT NOT NULL DEFAULT '' CHECK (LENGTH(Tag) < 191), "
					+ "DocumentId INTEGER, "
					+ "UNIQUE (Tag, DocumentId), "
					+ "FOREIGN KEY(DocumentId) REFERENCES DOCUMENTS(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS ENTITYTAGS("
					+ "ID INTEGER NOT NULL PRIMARY KEY, "
					+ "Tag TEXT NOT NULL DEFAULT '' CHECK (LENGTH(Tag) < 191), "
					+ "EntityId INTEGER, "
					+ "UNIQUE (Tag, EntityId), "
					+ "FOREIGN KEY(EntityId) REFERENCES ENTITIES(ID) ON DELETE CASCADE);");
		} else if (cp.getType().equals("mysql")) {
			s.add("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property VARCHAR(190), "
					+ "Value VARCHAR(190) NOT NULL,"
					+ "PRIMARY KEY (Property));");
			s.add("CREATE TABLE IF NOT EXISTS CODERS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Name VARCHAR(190) NOT NULL, "
					+ "Red SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "Refresh SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Refresh BETWEEN 0 AND 9999), "
					+ "FontSize SMALLINT UNSIGNED NOT NULL DEFAULT 14 CHECK (FontSize BETWEEN 1 AND 99), "
					+ "Password VARCHAR(190) NOT NULL, "
					+ "PopupWidth SMALLINT UNSIGNED NOT NULL DEFAULT 300 CHECK (PopupWidth BETWEEN 100 AND 9999), "
					+ "ColorByCoder TINYINT UNSIGNED NOT NULL DEFAULT 0 CHECK (ColorByCoder BETWEEN 0 AND 1), "
					+ "PopupDecoration TINYINT UNSIGNED NOT NULL DEFAULT 0 CHECK (PopupDecoration BETWEEN 0 AND 1), "
					+ "PopupAutoComplete TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PopupAutoComplete BETWEEN 0 AND 1), "
					+ "PermissionAddDocuments TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionAddDocuments BETWEEN 0 AND 1), "
					+ "PermissionEditDocuments TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionEditDocuments BETWEEN 0 AND 1), "
					+ "PermissionDeleteDocuments TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionDeleteDocuments BETWEEN 0 AND 1), "
					+ "PermissionImportDocuments TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionImportDocuments BETWEEN 0 AND 1), "
					+ "PermissionAddStatements TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionAddStatements BETWEEN 0 AND 1), "
					+ "PermissionEditStatements TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionEditStatements BETWEEN 0 AND 1), "
					+ "PermissionDeleteStatements TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionDeleteStatements BETWEEN 0 AND 1), "
					+ "PermissionEditAttributes TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionEditAttributes BETWEEN 0 AND 1), "
					+ "PermissionEditRegex TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionEditRegex BETWEEN 0 AND 1), "
					+ "PermissionEditStatementTypes TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionEditStatementTypes BETWEEN 0 AND 1), "
					+ "PermissionEditCoders TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionEditCoders BETWEEN 0 AND 1), "
					+ "PermissionEditCoderRelations TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionEditCoderRelations BETWEEN 0 AND 1), "
					+ "PermissionViewOthersDocuments TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionViewOthersDocuments BETWEEN 0 AND 1), "
					+ "PermissionEditOthersDocuments TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionEditOthersDocuments BETWEEN 0 AND 1), "
					+ "PermissionViewOthersStatements TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionViewOthersStatements BETWEEN 0 AND 1), "
					+ "PermissionEditOthersStatements TINYINT UNSIGNED NOT NULL DEFAULT 1 CHECK (PermissionEditOthersStatements BETWEEN 0 AND 1), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Title VARCHAR(190) NOT NULL, "
					+ "Text MEDIUMTEXT NOT NULL, "
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "Author VARCHAR(190) NOT NULL DEFAULT '', "
					+ "Source VARCHAR(190) NOT NULL DEFAULT '', "
					+ "Section VARCHAR(190) NOT NULL DEFAULT '', "
					+ "Notes TEXT NOT NULL, "
					+ "Type VARCHAR(190) NOT NULL DEFAULT '', "
					+ "Date BIGINT NOT NULL, "
					+ "Complete SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Complete BETWEEN 0 AND 1), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Label VARCHAR(190) NOT NULL, "
					+ "Red SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Variable VARCHAR(190) NOT NULL, "
					+ "DataType VARCHAR(190) NOT NULL DEFAULT 'short text' CHECK (DataType = 'boolean' OR DataType = 'integer' OR DataType = 'long text' OR DataType = 'short text'), "
					+ "UNIQUE KEY (Variable), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS ROLES("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "RoleName VARCHAR(190) NOT NULL, "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "Position SMALLINT UNSIGNED NOT NULL DEFAULT 1 CHECK (Position > 0), "
					+ "NumMin SMALLINT UNSIGNED DEFAULT 1 CHECK (NumMin > -1), "
					+ "NumMax SMALLINT UNSIGNED DEFAULT 1, "
					+ "NumDefault SMALLINT UNSIGNED DEFAULT 1, "
					+ "Red SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "DefaultVariableId SMALLINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(DefaultVariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "CONSTRAINT ck_min_max CHECK (NumMax >= NumMin), "
					+ "CONSTRAINT ck_min_default CHECK (NumDefault >= NumMin), "
					+ "CONSTRAINT ck_max_default CHECK (NumMax >= NumDefault), "
					+ "UNIQUE KEY(RoleName, StatementTypeId), "
					+ "UNIQUE KEY(StatementTypeId, Position), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS ROLEVARIABLELINKS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "RoleId SMALLINT UNSIGNED NOT NULL, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(RoleId) REFERENCES ROLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY(RoleId, VariableId), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label VARCHAR(190), "
					+ "Red SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "PRIMARY KEY(Label));");
			s.add("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "OtherCoder SMALLINT UNSIGNED NOT NULL CHECK(OtherCoder > 0), "
					+ "viewStatements SMALLINT UNSIGNED NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editStatements SMALLINT UNSIGNED NOT NULL DEFAULT 1 CHECK(editStatements BETWEEN 0 AND 1), "
					+ "viewDocuments SMALLINT UNSIGNED NOT NULL DEFAULT 1 CHECK(viewDocuments BETWEEN 0 AND 1), "
					+ "editDocuments SMALLINT UNSIGNED NOT NULL DEFAULT 1 CHECK(editDocuments BETWEEN 0 AND 1), "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(OtherCoder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "CONSTRAINT ck_other_coder CHECK (Coder != OtherCoder), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementTypeId SMALLINT UNSIGNED NOT NULL, "
					+ "DocumentId MEDIUMINT UNSIGNED NOT NULL, "
					+ "Start BIGINT UNSIGNED NOT NULL CHECK(Start >= 0), "
					+ "Stop BIGINT UNSIGNED NOT NULL CHECK(Stop >= 0), "
					+ "Coder SMALLINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(StatementTypeId) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(Coder) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(DocumentId) REFERENCES DOCUMENTS(ID) ON DELETE CASCADE, "
					+ "CONSTRAINT ck_start_stop CHECK (Start < Stop), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATABOOLEAN("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "RoleVariableLinkId SMALLINT UNSIGNED NOT NULL, "
					+ "Value SMALLINT UNSIGNED NOT NULL DEFAULT 1, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(RoleVariableLinkId) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "RoleVariableLinkId SMALLINT UNSIGNED NOT NULL, "
					+ "Value MEDIUMINT NOT NULL DEFAULT 0, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(RoleVariableLinkId) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS ENTITIES("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "Value VARCHAR(190) NOT NULL DEFAULT '', "
					+ "Red SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT UNSIGNED NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "ChildOf MEDIUMINT UNSIGNED CHECK(ChildOf > 0), "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(ChildOf) REFERENCES ENTITIES(ID) ON DELETE CASCADE, "
					+ "UNIQUE KEY (VariableId, Value), "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "RoleVariableLinkId SMALLINT UNSIGNED NOT NULL, "
					+ "Entity MEDIUMINT UNSIGNED NOT NULL, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(RoleVariableLinkId) REFERENCES ROLEVARIABLELINK(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(Entity) REFERENCES ENTITIES(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "StatementId MEDIUMINT UNSIGNED NOT NULL, "
					+ "RoleVariableLinkId SMALLINT UNSIGNED NOT NULL, "
					+ "Value TEXT NOT NULL, "
					+ "FOREIGN KEY(StatementId) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(RoleVariableLinkId) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEVARIABLES("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "VariableId SMALLINT UNSIGNED NOT NULL, "
					+ "AttributeVariable VARCHAR(190) NOT NULL, "
					+ "UNIQUE KEY(VariableId, AttributeVariable), "
					+ "FOREIGN KEY(VariableId) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEVALUES("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "EntityId MEDIUMINT UNSIGNED NOT NULL, "
					+ "AttributeVariableId MEDIUMINT UNSIGNED NOT NULL, "
					+ "AttributeValue VARCHAR(190) NOT NULL DEFAULT '', "
					+ "UNIQUE KEY (EntityId, AttributeVariableId), "
					+ "FOREIGN KEY(EntityId) REFERENCES ENTITIES(ID) ON DELETE CASCADE, "
					+ "FOREIGN KEY(AttributeVariableId) REFERENCES ATTRIBUTEVARIABLES(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTTAGS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Tag VARCHAR(190) NOT NULL DEFAULT '', "
					+ "DocumentId MEDIUMINT UNSIGNED NOT NULL, "
					+ "UNIQUE KEY (Tag, DocumentId), "
					+ "FOREIGN KEY(DocumentId) REFERENCES DOCUMENTS(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
			s.add("CREATE TABLE IF NOT EXISTS ENTITYTAGS("
					+ "ID MEDIUMINT UNSIGNED NOT NULL AUTO_INCREMENT, "
					+ "Tag VARCHAR(190) NOT NULL DEFAULT '', "
					+ "EntityId MEDIUMINT UNSIGNED NOT NULL, "
					+ "UNIQUE KEY (Tag, EntityId), "
					+ "FOREIGN KEY(EntityId) REFERENCES ENTITIES(ID) ON DELETE CASCADE, "
					+ "PRIMARY KEY(ID));");
		} else if (cp.getType().equals("postgresql")) {
			s.add("CREATE TABLE IF NOT EXISTS SETTINGS("
					+ "Property VARCHAR(190) PRIMARY KEY, "
					+ "Value VARCHAR(190) NOT NULL);");
			s.add("CREATE TABLE IF NOT EXISTS CODERS("
					+ "ID SERIAL NOT NULL PRIMARY KEY CHECK (ID > 0), "
					+ "Name VARCHAR(190) NOT NULL, "
					+ "Red SMALLINT NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "Refresh SMALLINT NOT NULL CHECK (Refresh BETWEEN 0 AND 9999) DEFAULT 0, "
					+ "FontSize SMALLINT NOT NULL CHECK (FontSize BETWEEN 1 AND 99) DEFAULT 14, "
					+ "Password VARCHAR(190) NOT NULL, "
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
					+ "PermissionEditStatementTypes SMALLINT NOT NULL CHECK (PermissionEditStatementTypes BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditCoders SMALLINT NOT NULL CHECK (PermissionEditCoders BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditCoderRelations SMALLINT NOT NULL CHECK (PermissionEditCoderRelations BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionViewOthersDocuments SMALLINT NOT NULL CHECK (PermissionViewOthersDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditOthersDocuments SMALLINT NOT NULL CHECK (PermissionEditOthersDocuments BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionViewOthersStatements SMALLINT NOT NULL CHECK (PermissionViewOthersStatements BETWEEN 0 AND 1) DEFAULT 1, "
					+ "PermissionEditOthersStatements SMALLINT NOT NULL CHECK (PermissionEditOthersStatements BETWEEN 0 AND 1) DEFAULT 1);");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Title VARCHAR(190) NOT NULL, "
					+ "Text TEXT NOT NULL, "
					+ "Coder INT NOT NULL REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "Author VARCHAR(190) NOT NULL DEFAULT '', "
					+ "Source VARCHAR(190) NOT NULL DEFAULT '', "
					+ "Section VARCHAR(190) NOT NULL DEFAULT '', "
					+ "Notes TEXT NOT NULL DEFAULT '', "
					+ "Type VARCHAR(190) NOT NULL DEFAULT '', "
					+ "Date BIGINT NOT NULL, "
					+ "Complete SMALLINT NOT NULL DEFAULT 0 CHECK (Complete BETWEEN 0 AND 1));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTTYPES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Label VARCHAR(190) NOT NULL, "
					+ "Red SMALLINT NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS VARIABLES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Variable VARCHAR(190) NOT NULL, "
					+ "DataType VARCHAR(190) NOT NULL CHECK (DataType = 'boolean' OR DataType = 'integer' OR DataType = 'long text' OR DataType = 'short text') DEFAULT 'short text', "
					+ "UNIQUE (Variable));");
			s.add("CREATE TABLE IF NOT EXISTS ROLES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "RoleName VARCHAR(190) NOT NULL, "
					+ "StatementTypeId INT NOT NULL CHECK(StatementTypeId > 0) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "Position SMALLINT NOT NULL DEFAULT 1 CHECK (Position > 0), "
					+ "NumMin SMALLINT NOT NULL DEFAULT 1 CHECK (NumMin > -1), "
					+ "NumMax SMALLINT NOT NULL DEFAULT 1, "
					+ "NumDefault SMALLINT NOT NULL DEFAULT 1, "
					+ "Red SMALLINT NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "DefaultVariableId INT NOT NULL CHECK(DefaultVariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "CONSTRAINT ck_min_max CHECK (NumMax >= NumMin), "
					+ "CONSTRAINT ck_min_default CHECK (NumDefault >= NumMin), "
					+ "CONSTRAINT ck_max_default CHECK (NumMax >= NumDefault), "
					+ "UNIQUE (RoleName, StatementTypeId), "
					+ "UNIQUE (StatementTypeId, Position));");
			s.add("CREATE TABLE IF NOT EXISTS ROLEVARIABLELINKS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "RoleId SMALLINT NOT NULL CHECK(RoleId > 0) REFERENCES ROLES(ID) ON DELETE CASCADE, "
					+ "VariableId SMALLINT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (RoleId, VariableId));");
			s.add("CREATE TABLE IF NOT EXISTS REGEXES("
					+ "Label VARCHAR(190) PRIMARY KEY, "
					+ "Red SMALLINT NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255));");
			s.add("CREATE TABLE IF NOT EXISTS CODERRELATIONS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Coder INT NOT NULL CHECK(Coder > 0) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "OtherCoder INT NOT NULL CHECK(OtherCoder > 0) REFERENCES CODERS(ID) ON DELETE CASCADE, "
					+ "viewStatements INT NOT NULL DEFAULT 1 CHECK(viewStatements BETWEEN 0 AND 1), "
					+ "editStatements INT NOT NULL DEFAULT 1 CHECK(editStatements BETWEEN 0 AND 1), "
					+ "viewDocuments INT NOT NULL DEFAULT 1 CHECK(viewDocuments BETWEEN 0 AND 1), "
					+ "editDocuments INT NOT NULL DEFAULT 1 CHECK(editDocuments BETWEEN 0 AND 1), "
					+ "CONSTRAINT ck_other_coder CHECK (Coder != OtherCoder));");
			s.add("CREATE TABLE IF NOT EXISTS STATEMENTS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementTypeId INT NOT NULL CHECK(StatementTypeId > 0) REFERENCES STATEMENTTYPES(ID) ON DELETE CASCADE, "
					+ "DocumentId INT NOT NULL CHECK(DocumentId > 0) REFERENCES DOCUMENTS(ID) ON DELETE CASCADE, "
					+ "Start BIGINT NOT NULL CHECK(Start >= 0), "
					+ "Stop BIGINT NOT NULL CHECK(Stop >= 0), "
					+ "Coder INT NOT NULL CHECK(Coder > 0) REFERENCES CODERS(ID) ON DELETE CASCADE), "
					+ "CONSTRAINT ck_start_stop CHECK (Start < Stop));");
			s.add("CREATE TABLE IF NOT EXISTS DATABOOLEAN("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "RoleVariableLinkId INT NOT NULL CHECK(RoleVariableLinkId > 0) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE, "
					+ "Value INT NOT NULL DEFAULT 1 CHECK(Value BETWEEN 0 AND 1));");
			s.add("CREATE TABLE IF NOT EXISTS DATAINTEGER("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "RoleVariableLinkId INT NOT NULL CHECK(RoleVariableLinkId > 0) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE, "
					+ "Value INT NOT NULL DEFAULT 0);");
			s.add("CREATE TABLE IF NOT EXISTS ENTITIES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "Value VARCHAR(190) NOT NULL DEFAULT '', "
					+ "Red SMALLINT NOT NULL DEFAULT 0 CHECK (Red BETWEEN 0 AND 255), "
					+ "Green SMALLINT NOT NULL DEFAULT 0 CHECK (Green BETWEEN 0 AND 255), "
					+ "Blue SMALLINT NOT NULL DEFAULT 0 CHECK (Blue BETWEEN 0 AND 255), "
					+ "ChildOf INT CHECK(ChildOf > 0) REFERENCES ENTITIES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (VariableId, Value));");
			s.add("CREATE TABLE IF NOT EXISTS DATASHORTTEXT("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "RoleVariableLinkId INT NOT NULL CHECK(RoleVariableLinkId > 0) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE, "
					+ "Entity INT NOT NULL CHECK(Entity > 0) REFERENCES ENTITIES(ID) ON DELETE CASCADE);");
			s.add("CREATE TABLE IF NOT EXISTS DATALONGTEXT("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "StatementId INT NOT NULL CHECK(StatementId > 0) REFERENCES STATEMENTS(ID) ON DELETE CASCADE, "
					+ "RoleVariableLinkId INT NOT NULL CHECK(RoleVariableLinkId > 0) REFERENCES ROLEVARIABLELINKS(ID) ON DELETE CASCADE, "
					+ "Value TEXT NOT NULL);");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEVARIABLES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "VariableId INT NOT NULL CHECK(VariableId > 0) REFERENCES VARIABLES(ID) ON DELETE CASCADE, "
					+ "AttributeVariable VARCHAR(190) NOT NULL, "
					+ "UNIQUE (VariableId, AttributeVariable));");
			s.add("CREATE TABLE IF NOT EXISTS ATTRIBUTEVALUES("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "EntityId INT NOT NULL CHECK(EntityId > 0) REFERENCES ENTITIES(ID) ON DELETE CASCADE, "
					+ "AttributeVariableId INT NOT NULL CHECK(AttributeVariableId > 0) REFERENCES ATTRIBUTEVARIABLES(ID) ON DELETE CASCADE, "
					+ "AttributeValue VARCHAR(190) NOT NULL DEFAULT '', "
					+ "UNIQUE (EntityId, AttributeVariableId));");
			s.add("CREATE TABLE IF NOT EXISTS DOCUMENTTAGS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Tag VARCHAR(190) NOT NULL DEFAULT '', "
					+ "DocumentId INT NOT NULL REFERENCES DOCUMENTS(ID) ON DELETE CASCADE, "
					+ "UNIQUE (Tag, DocumentId));");
			s.add("CREATE TABLE IF NOT EXISTS ENTITYTAGS("
					+ "ID SERIAL NOT NULL PRIMARY KEY, "
					+ "Tag VARCHAR(190) NOT NULL DEFAULT '', "
					+ "EntityId INT NOT NULL REFERENCES ENTITIES(ID) ON DELETE CASCADE, "
					+ "UNIQUE (Tag, EntityId));");
		}
		// fill default data into the tables (Admin coder, settings, statement types)
		s.add("INSERT INTO CODERS (ID, Name, Red, Green, Blue, Password, PermissionEditStatementTypes, PermissionEditCoders, PermissionEditOthersDocuments, PermissionEditOthersStatements) VALUES (1, 'Admin', 255, 255, 0, '" + encryptedAdminPassword + "', 1, 1, 1, 1);");
		s.add("INSERT INTO SETTINGS (Property, Value) VALUES ('version', '" + Dna.version + "');");
		s.add("INSERT INTO SETTINGS (Property, Value) VALUES ('date', '" + Dna.date + "');");
		// DNA Statement
		s.add("INSERT INTO STATEMENTTYPES (ID, Label, Red, Green, Blue) VALUES (1, 'DNA Statement', 239, 208, 51);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType) VALUES(1, 'person', 'short text');");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType) VALUES(2, 'organization', 'short text');");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType) VALUES(3, 'concept', 'short text');");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType) VALUES(4, 'agreement', 'boolean');");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (1, 'person', 1, 1, 0, 10, 1, 0, 0, 0, 1);");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (2, 'organization', 1, 2, 0, 10, 1, 0, 0, 0, 2);");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (3, 'concept', 1, 3, 1, 10, 1, 0, 0, 0, 3);");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (4, 'agreement', 1, 4, 1, 1, 1, 0, 0, 0, 4);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (1, 1, 1);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (2, 2, 2);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (3, 3, 3);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (4, 4, 4);");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (1, 'Type');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (2, 'Type');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (3, 'Type');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (1, 'Alias');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (2, 'Alias');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (3, 'Alias');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (1, 'Notes');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (2, 'Notes');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (3, 'Notes');");
		s.add("INSERT INTO ENTITIES (VariableId) VALUES(1);");
		s.add("INSERT INTO ENTITIES (VariableId) VALUES(2);");
		s.add("INSERT INTO ENTITIES (VariableId) VALUES(3);");
		s.add("INSERT INTO ENTITIES (VariableId) VALUES(4);");
		// NPF Story Element
		s.add("INSERT INTO STATEMENTTYPES (ID, Label, Red, Green, Blue) VALUES (2, 'NPF Story Element', 100, 200, 190);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType) VALUES(5, 'group', 'short text');");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType) VALUES(6, 'plot', 'short text');");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (5, 'narrator', 2, 1, 0, 10, 0, 0, 0, 0, 1);");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (6, 'victim', 2, 2, 0, 10, 0, 0, 0, 0, 5);");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (7, 'villain', 2, 3, 0, 10, 0, 0, 0, 0, 5);");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (8, 'plot', 2, 4, 0, 5, 0, 0, 0, 0, 6);");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (9, 'hero', 2, 5, 0, 10, 0, 0, 0, 0, 5);");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (10, 'policy solution', 2, 6, 0, 10, 0, 0, 0, 0, 3);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (5, 5, 1);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (6, 5, 2);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (7, 6, 1);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (8, 6, 2);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (9, 6, 5);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (10, 7, 1);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (11, 7, 2);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (12, 7, 5);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (13, 8, 6);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (14, 9, 1);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (15, 9, 2);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (16, 9, 5);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (17, 10, 3);");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (5, 'Type');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (6, 'Type');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (5, 'Alias');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (6, 'Alias');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (5, 'Notes');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (6, 'Notes');");
		s.add("INSERT INTO ENTITIES (VariableId) VALUES(5);");
		s.add("INSERT INTO ENTITIES (VariableId) VALUES(6);");
		// Annotation
		s.add("INSERT INTO STATEMENTTYPES (ID, Label, Red, Green, Blue) VALUES (3, 'Annotation', 211, 211, 211);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType) VALUES(7, 'note', 'long text');");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (11, 'note', 3, 1, 1, 1, 1, 0, 0, 0, 7);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (18, 11, 7);");
		s.add("INSERT INTO ENTITIES (VariableId) VALUES(7);");
		// Theme
		s.add("INSERT INTO STATEMENTTYPES (ID, Label, Red, Green, Blue) VALUES (4, 'Theme', 252, 3, 119);");
		s.add("INSERT INTO VARIABLES (ID, Variable, DataType) VALUES(8, 'theme', 'short text');");
		s.add("INSERT INTO ROLES (ID, RoleName, StatementTypeId, Position, NumMin, NumMax, NumDefault, Red, Green, Blue, DefaultVariableId) VALUES (12, 'theme', 4, 1, 1, 20, 1, 0, 0, 0, 8);");
		s.add("INSERT INTO ROLEVARIABLELINKS (ID, RoleId, VariableId) VALUES (19, 12, 8);");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (8, 'Type');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (8, 'Alias');");
		s.add("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (8, 'Notes');");
		s.add("INSERT INTO ENTITIES (VariableId) VALUES(8);");
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
	 * Retrieve a coder based on its ID. Works with DNA 2 and 3.
	 *
	 * @param coderId  The ID of the coder to be retrieved from the database.
	 * @return         The coder to be retrieved, as a {@link model.Coder
	 *   Coder} object.
	 */
	public Coder getCoder(int coderId) {
		Coder c = null;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT * FROM CODERS WHERE ID = ?;");
				PreparedStatement s2 = conn.prepareStatement("SELECT * FROM CODERRELATIONS WHERE Coder = ?;");
				PreparedStatement s3 = conn.prepareStatement("SELECT Name, Red, Green, Blue FROM CODERS WHERE ID = ?;");
				PreparedStatement s4 = conn.prepareStatement("SELECT Value FROM SETTINGS WHERE Property = 'version';")) {
			ResultSet rs1, rs2, rs3;

			// get DNA version
			int v = 3;
			rs1 = s4.executeQuery();
			while (rs1.next()) {
				if (rs1.getString("Value").startsWith("2")) {
					v = 2;
				}
			}
			rs1.close();

			PreparedStatement s5V2 = null;
			if (v == 2) {
				s5V2 = conn.prepareStatement("SELECT Type, Permission FROM CODERPERMISSIONS WHERE Coder = ?;");
			}

			// get coder
        	int sourceCoderId, targetCoderId;
        	String targetCoderName = null;
        	Color targetCoderColor = null;
			s1.setInt(1, coderId);
			rs1 = s1.executeQuery();
			while (rs1.next()) {
        		sourceCoderId = rs1.getInt("ID");

        		// get coder relations
        		s2.setInt(1, sourceCoderId);
				rs2 = s2.executeQuery();
				HashMap<Integer, CoderRelation> map = new HashMap<Integer, CoderRelation>();
				while (rs2.next()) {

					// get details from other coder and create coder relations map
					targetCoderId = rs2.getInt("OtherCoder");
					s3.setInt(1, targetCoderId);
					rs3 = s3.executeQuery();
					while (rs3.next()) {
						targetCoderName = rs3.getString("Name");
						targetCoderColor = new Color(rs3.getInt("Red"), rs3.getInt("Green"), rs3.getInt("Blue"));
					}
					map.put(rs2.getInt("OtherCoder"),
							new CoderRelation(
									targetCoderId,
									targetCoderName,
									targetCoderColor,
									rs2.getInt("viewDocuments") == 1,
									rs2.getInt("editDocuments") == 1,
									rs2.getInt("viewStatements") == 1,
									rs2.getInt("editStatements") == 1));
				}

				// create coder
				if (v == 2) { // DNA 2.0
					s5V2.setInt(1, sourceCoderId);
					rs2 = s5V2.executeQuery();
					HashMap<String, Boolean> perm = new HashMap<String, Boolean>();
					while (rs2.next()) {
						if (rs2.getString("Type").equals("addDocuments")) {
							perm.put("PermissionAddDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editDocuments")) {
							perm.put("PermissionEditDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("deleteDocuments")) {
							perm.put("PermissionDeleteDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("importDocuments")) {
							perm.put("PermissionImportDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("addStatements")) {
							perm.put("PermissionAddStatements", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editStatements")) {
							perm.put("PermissionEditStatements", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("deleteStatements")) {
							perm.put("PermissionDeleteStatements", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editAttributes")) {
							perm.put("PermissionEditAttributes", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editRegex")) {
							perm.put("PermissionEditRegex", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editStatementTypes")) {
							perm.put("PermissionEditStatementTypes", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editCoders")) {
							perm.put("PermissionEditCoders", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editCoderRelations")) {
							perm.put("PermissionEditCoderRelations", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("viewOthersDocuments")) {
							perm.put("PermissionViewOthersDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editOthersDocuments")) {
							perm.put("PermissionEditOthersDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("viewOthersStatements")) {
							perm.put("PermissionViewOthersStatements", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editOthersStatements")) {
							perm.put("PermissionEditOthersStatements", rs2.getInt("Permission") == 1);
						}
					}
					rs2.close();
					if (!perm.containsKey("permissionAddDocuments")) {
						perm.put("PermissionAddDocuments", true);
					}
					if (!perm.containsKey("permissionEditDocuments")) {
						perm.put("PermissionEditDocuments", true);
					}
					if (!perm.containsKey("permissionDeleteDocuments")) {
						perm.put("PermissionDeleteDocuments", true);
					}
					if (!perm.containsKey("permissionImportDocuments")) {
						perm.put("PermissionImportDocuments", true);
					}
					if (!perm.containsKey("permissionAddStatements")) {
						perm.put("PermissionAddStatements", true);
					}
					if (!perm.containsKey("permissionEditStatements")) {
						perm.put("PermissionEditStatements", true);
					}
					if (!perm.containsKey("permissionDeleteStatements")) {
						perm.put("PermissionDeleteStatements", true);
					}
					if (!perm.containsKey("permissionEditAttributes")) {
						perm.put("PermissionEditAttributes", true);
					}
					if (!perm.containsKey("permissionEditRegex")) {
						perm.put("PermissionEditRegex", true);
					}
					if (!perm.containsKey("permissionEditStatementTypes")) {
						perm.put("PermissionEditStatementTypes", true);
					}
					if (!perm.containsKey("permissionEditCoders")) {
						perm.put("PermissionEditCoders", true);
					}
					if (!perm.containsKey("permissionEditCoderRelations")) {
						perm.put("PermissionEditCoderRelations", true);
					}
					if (!perm.containsKey("permissionViewOthersDocuments")) {
						perm.put("PermissionViewOthersDocuments", true);
					}
					if (!perm.containsKey("permissionEditOthersDocuments")) {
						perm.put("PermissionEditOthersDocuments", true);
					}
					if (!perm.containsKey("permissionViewOthersStatements")) {
						perm.put("PermissionViewOthersStatements", true);
					}
					if (!perm.containsKey("permissionEditOthersStatements")) {
						perm.put("PermissionEditOthersStatements", true);
					}
	            	c = new Coder(coderId,
	            			rs1.getString("Name"),
				    		rs1.getInt("Red"),
				    		rs1.getInt("Green"),
				    		rs1.getInt("Blue"),
				    		0,
				    		14,
				    		300,
				    		false,
				    		false,
				    		true,
				    		perm.get("PermissionAddDocuments"),
				    		perm.get("PermissionEditDocuments"),
				    		perm.get("PermissionDeleteDocuments"),
				    		perm.get("PermissionImportDocuments"),
				    		perm.get("PermissionAddStatements"),
				    		perm.get("PermissionEditStatements"),
				    		perm.get("PermissionDeleteStatements"),
				    		perm.get("PermissionEditAttributes"),
				    		perm.get("PermissionEditRegex"),
				    		perm.get("PermissionEditStatementTypes"),
				    		perm.get("PermissionEditCoders"),
				    		perm.get("PermissionEditCoderRelations"),
				    		perm.get("PermissionViewOthersDocuments"),
				    		perm.get("PermissionEditOthersDocuments"),
				    		perm.get("PermissionViewOthersStatements"),
				    		perm.get("PermissionEditOthersStatements"),
				    		map);
				} else { // >= DNA 3.0
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
				    		rs1.getInt("PermissionEditCoderRelations") == 1,
				    		rs1.getInt("PermissionViewOthersDocuments") == 1,
				    		rs1.getInt("PermissionEditOthersDocuments") == 1,
				    		rs1.getInt("PermissionViewOthersStatements") == 1,
				    		rs1.getInt("PermissionEditOthersStatements") == 1,
				    		map);
				}
			}
			if (v == 2) {
				s5V2.close();
			}
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Coder with ID " + coderId + " could not be retrieved from the database.",
					"The details of the coder with ID " + coderId + " could not be retrieved from the database. Check your database connection.",
					e);
			Dna.logger.log(l);
		}
		return c;
	}

	/**
	 * Retrieve a list of coders in the database. Works with DNA 2 and 3.
	 *
	 * @return An {@link java.util.ArrayList ArrayList} of {@link model.Coder
	 *   Coder} objects.
	 */
	public ArrayList<Coder> getCoders() {
		ArrayList<Coder> coders = new ArrayList<Coder>();
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT * FROM CODERS;");
				PreparedStatement s2 = conn.prepareStatement("SELECT * FROM CODERRELATIONS WHERE Coder = ?;");
				PreparedStatement s3 = conn.prepareStatement("SELECT Name, Red, Green, Blue FROM CODERS WHERE ID = ?;");
				PreparedStatement s4 = conn.prepareStatement("SELECT Value FROM SETTINGS WHERE Property = 'version';")) {
			ResultSet rs1, rs2, rs3;

			// get DNA version
			int v = 3;
			rs1 = s4.executeQuery();
			while (rs1.next()) {
				if (rs1.getString("Value").startsWith("2")) {
					v = 2;
				}
			}
			rs1.close();

			PreparedStatement s5V2 = null;
			if (v == 2) {
				s5V2 = conn.prepareStatement("SELECT Type, Permission FROM CODERPERMISSIONS WHERE Coder = ?;");
			}

			// get coders
        	rs1 = s1.executeQuery();
        	int sourceCoderId, targetCoderId;
        	String targetCoderName = null;
        	Color targetCoderColor = null;
        	while (rs1.next()) {
        		sourceCoderId = rs1.getInt("ID");

        		// get coder relations
        		s2.setInt(1, sourceCoderId);
				rs2 = s2.executeQuery();
				HashMap<Integer, CoderRelation> map = new HashMap<Integer, CoderRelation>();
				while (rs2.next()) {

					// get details from other coder and create coder relations map
					targetCoderId = rs2.getInt("OtherCoder");
					s3.setInt(1, targetCoderId);
					rs3 = s3.executeQuery();
					while (rs3.next()) {
						targetCoderName = rs3.getString("Name");
						targetCoderColor = new Color(rs3.getInt("Red"), rs3.getInt("Green"), rs3.getInt("Blue"));
					}
					map.put(rs2.getInt("OtherCoder"),
							new CoderRelation(
									targetCoderId,
									targetCoderName,
									targetCoderColor,
									rs2.getInt("viewDocuments") == 1,
									rs2.getInt("editDocuments") == 1,
									rs2.getInt("viewStatements") == 1,
									rs2.getInt("editStatements") == 1));
				}
				rs2.close();

				// create coder and add to list
				if (v == 2) { // DNA 2.0
					s5V2.setInt(1, sourceCoderId);
					rs2 = s5V2.executeQuery();
					HashMap<String, Boolean> perm = new HashMap<String, Boolean>();
					while (rs2.next()) {
						if (rs2.getString("Type").equals("addDocuments")) {
							perm.put("PermissionAddDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editDocuments")) {
							perm.put("PermissionEditDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("deleteDocuments")) {
							perm.put("PermissionDeleteDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("importDocuments")) {
							perm.put("PermissionImportDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("addStatements")) {
							perm.put("PermissionAddStatements", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editStatements")) {
							perm.put("PermissionEditStatements", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("deleteStatements")) {
							perm.put("PermissionDeleteStatements", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editAttributes")) {
							perm.put("PermissionEditAttributes", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editRegex")) {
							perm.put("PermissionEditRegex", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editStatementTypes")) {
							perm.put("PermissionEditStatementTypes", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editCoders")) {
							perm.put("PermissionEditCoders", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editCoderRelations")) {
							perm.put("PermissionEditCoderRelations", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("viewOthersDocuments")) {
							perm.put("PermissionViewOthersDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editOthersDocuments")) {
							perm.put("PermissionEditOthersDocuments", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("viewOthersStatements")) {
							perm.put("PermissionViewOthersStatements", rs2.getInt("Permission") == 1);
						}
						if (rs2.getString("Type").equals("editOthersStatements")) {
							perm.put("PermissionEditOthersStatements", rs2.getInt("Permission") == 1);
						}
					}
					rs2.close();
					if (!perm.containsKey("permissionAddDocuments")) {
						perm.put("PermissionAddDocuments", true);
					}
					if (!perm.containsKey("permissionEditDocuments")) {
						perm.put("PermissionEditDocuments", true);
					}
					if (!perm.containsKey("permissionDeleteDocuments")) {
						perm.put("PermissionDeleteDocuments", true);
					}
					if (!perm.containsKey("permissionImportDocuments")) {
						perm.put("PermissionImportDocuments", true);
					}
					if (!perm.containsKey("permissionAddStatements")) {
						perm.put("PermissionAddStatements", true);
					}
					if (!perm.containsKey("permissionEditStatements")) {
						perm.put("PermissionEditStatements", true);
					}
					if (!perm.containsKey("permissionDeleteStatements")) {
						perm.put("PermissionDeleteStatements", true);
					}
					if (!perm.containsKey("permissionEditAttributes")) {
						perm.put("PermissionEditAttributes", true);
					}
					if (!perm.containsKey("permissionEditRegex")) {
						perm.put("PermissionEditRegex", true);
					}
					if (!perm.containsKey("permissionEditStatementTypes")) {
						perm.put("PermissionEditStatementTypes", true);
					}
					if (!perm.containsKey("permissionEditCoders")) {
						perm.put("PermissionEditCoders", true);
					}
					if (!perm.containsKey("permissionEditCoderRelations")) {
						perm.put("PermissionEditCoderRelations", true);
					}
					if (!perm.containsKey("permissionViewOthersDocuments")) {
						perm.put("PermissionViewOthersDocuments", true);
					}
					if (!perm.containsKey("permissionEditOthersDocuments")) {
						perm.put("PermissionEditOthersDocuments", true);
					}
					if (!perm.containsKey("permissionViewOthersStatements")) {
						perm.put("PermissionViewOthersStatements", true);
					}
					if (!perm.containsKey("permissionEditOthersStatements")) {
						perm.put("PermissionEditOthersStatements", true);
					}
	            	coders.add(new Coder(rs1.getInt("ID"),
	            			rs1.getString("Name"),
				    		rs1.getInt("Red"),
				    		rs1.getInt("Green"),
				    		rs1.getInt("Blue"),
				    		0,
				    		14,
				    		300,
				    		false,
				    		false,
				    		true,
				    		perm.get("PermissionAddDocuments"),
				    		perm.get("PermissionEditDocuments"),
				    		perm.get("PermissionDeleteDocuments"),
				    		perm.get("PermissionImportDocuments"),
				    		perm.get("PermissionAddStatements"),
				    		perm.get("PermissionEditStatements"),
				    		perm.get("PermissionDeleteStatements"),
				    		perm.get("PermissionEditAttributes"),
				    		perm.get("PermissionEditRegex"),
				    		perm.get("PermissionEditStatementTypes"),
				    		perm.get("PermissionEditCoders"),
				    		perm.get("PermissionEditCoderRelations"),
				    		perm.get("PermissionViewOthersDocuments"),
				    		perm.get("PermissionEditOthersDocuments"),
				    		perm.get("PermissionViewOthersStatements"),
				    		perm.get("PermissionEditOthersStatements"),
				    		map));
				} else { // DNA 3.0
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
				    		rs1.getInt("PermissionEditCoderRelations") == 1,
				    		rs1.getInt("PermissionViewOthersDocuments") == 1,
				    		rs1.getInt("PermissionEditOthersDocuments") == 1,
				    		rs1.getInt("PermissionViewOthersStatements") == 1,
				    		rs1.getInt("PermissionEditOthersStatements") == 1,
				    		map));
				}
            }
			if (v == 2) {
				s5V2.close();
			}
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.ERROR,
        			"[SQL] Failed to retrieve coders from the database.",
        			"Attempted to retrieve all coders from the database. Check your connection.",
        			e);
        	Dna.logger.log(l);
		}
		return coders;
	}

	/**
	 * Retrieve a list of coders in the database, just with the names, IDs, and
	 * colors, without any further information. This serves to allow R users to
	 * see what the IDs are that correspond to coders.
	 *
	 * @return An {@link java.util.ArrayList ArrayList} of {@link model.Coder
	 *   Coder} objects with only the ID, coder name, and color as useful
	 *   information.
	 */
	public ArrayList<Coder> queryCoders() {
		ArrayList<Coder> coders = new ArrayList<Coder>();
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("SELECT ID, Name, Red, Green, Blue FROM CODERS;")) {
			ResultSet r = s.executeQuery();
        	while (r.next()) {
        		Coder c = new Coder(r.getInt("ID"),
        				r.getString("Name"),
        				new Color(r.getInt("Red"),
        						r.getInt("Green"),
        						r.getInt("Blue")));
        		coders.add(c);
        	}
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.ERROR,
        			"[SQL] Failed to query coders from the database.",
        			"Attempted to query which coders are available in the database. Check your connection.",
        			e);
        	Dna.logger.log(l);
		}
		return coders;
	}

	/**
	 * Create a new coder with default permissions and coder relations.
	 *
	 * @param coderName     Name of the new coder.
	 * @param coderColor    Color of the new coder.
	 * @param passwordHash  Encrypted password hash for the new coder.
	 * @return              ID of the new coder.
	 */
	public int addCoder(String coderName, Color coderColor, String passwordHash) {
		int coderId = -1;
		String sql1 = "INSERT INTO CODERS (Name, Red, Green, Blue, Password) VALUES (?, ?, ?, ?, ?);";
		String sql2 = "INSERT INTO CODERRELATIONS(Coder, OtherCoder) VALUES (?, ?);";
		String sql3 = "SELECT ID FROM CODERS WHERE ID != ?;";
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s1 = conn.prepareStatement(sql1, PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement s2 = conn.prepareStatement(sql2);
				PreparedStatement s3 = conn.prepareStatement(sql3);
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);

			// add new coder
			s1.setString(1, coderName);
			s1.setInt(2, coderColor.getRed());
			s1.setInt(3, coderColor.getGreen());
			s1.setInt(4, coderColor.getBlue());
			s1.setString(5, passwordHash);
			s1.executeUpdate();

			// find ID of the new coder
			ResultSet generatedKeysResultSet = s1.getGeneratedKeys();
			while (generatedKeysResultSet.next()) {
				coderId = generatedKeysResultSet.getInt(1);
			}

			// identify other coders
			s3.setInt(1, coderId);
			ResultSet r = s3.executeQuery();
			while (r.next()) {
				// coder relations from current coder to other coders
				s2.setInt(1, coderId);
				s2.setInt(2, r.getInt(1));
				s2.executeUpdate();

				// coder relations from other coders to current coder
				s2.setInt(1, r.getInt(1));
				s2.setInt(2, coderId);
				s2.executeUpdate();
			}

        	conn.commit();
        	LogEvent l = new LogEvent(Logger.MESSAGE,
        			"[SQL] New Coder " + coderId + " successfully created.",
        			"New Coder " + coderId + " successfully created.");
        	Dna.logger.log(l);
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.ERROR,
        			"[SQL] Failed to create new coder in the database.",
        			"Attempted to create a new coder, but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
		return coderId;
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
    		this.activeCoder.setFontSize(fontSize);
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to update font size for Coder " + coderId + " in the database.",
        			"Attempted to set a new font size for Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
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
    		this.activeCoder.setColorByCoder(colorByCoder);
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to update color by coder setting for Coder " + coderId + " in the database.",
        			"Attempted to update the color setting that is used for painting statements in the text (statement type color or coder color) for Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
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
        	this.activeCoder.setPopupWidth(popupWidth);
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to update popup window width for Coder " + coderId + " in the database.",
        			"Attempted to set a new short text field display width for statement popup windows for Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
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
        	this.activeCoder.setPopupDecoration(decoration);
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to update popup decoration setting for Coder " + coderId + " in the database.",
        			"Attempted to update window decoration settings for statement popup windows for Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
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
        	this.activeCoder.setPopupAutoComplete(autoComplete);
		} catch (SQLException e) {
        	LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to update popup auto-complete setting for Coder " + coderId + " in the database.",
        			"Attempted to update auto-completion settings for text fields in statement popup windows for Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
	}

	/**
	 * Update an existing coder in the database.
	 *
	 * @param coder            The coder with all details, permissions, and
	 *   coder relations.
	 * @param newPasswordHash  The new password hash String. Can be {@code ""}
	 *   or {@code null}, in which case the existing password is retained.
	 * @return                 Indicator of success.
	 */
	public boolean updateCoder(Coder coder, String newPasswordHash) {
		int coderId = coder.getId();
		boolean success = false;
		String sql1 = "UPDATE CODERS SET "
				+ "Name = ?, "
				+ "Red = ?, "
				+ "Green = ?, "
				+ "Blue = ?, "
				+ "Password = ?, "
				+ "PermissionAddDocuments = ?, "
				+ "PermissionEditDocuments = ?, "
				+ "PermissionDeleteDocuments = ?, "
				+ "PermissionImportDocuments = ?, "
				+ "PermissionAddStatements = ?, "
				+ "PermissionEditStatements = ?, "
				+ "PermissionDeleteStatements = ?, "
				+ "PermissionEditAttributes = ?, "
				+ "PermissionEditRegex = ?, "
				+ "PermissionEditStatementTypes = ?, "
				+ "PermissionEditCoders = ?, "
				+ "PermissionEditCoderRelations = ?, "
				+ "PermissionViewOthersDocuments = ?, "
				+ "PermissionEditOthersDocuments = ?, "
				+ "PermissionViewOthersStatements = ?, "
				+ "PermissionEditOthersStatements = ? "
				+ "WHERE ID = ?;";
		String sql2 = "UPDATE CODERRELATIONS SET "
				+ "viewDocuments = ?, "
				+ "editDocuments = ?, "
				+ "viewStatements = ?, "
				+ "editStatements = ? "
				+ "WHERE Coder = ? AND OtherCoder = ?;";
		String sql3 = "SELECT COUNT(ID) FROM CODERRELATIONS WHERE Coder = ? AND OtherCoder = ?;";
		String sql4 = "INSERT INTO CODERRELATIONS "
				+ "(Coder, OtherCoder, viewDocuments, editDocuments, viewStatements, editStatements) "
				+ "VALUES (?, ?, ?, ?, ?, ?);";
		String sql5 = "SELECT Password FROM CODERS WHERE ID = ?;";

		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s1 = conn.prepareStatement(sql1);
				PreparedStatement s2 = conn.prepareStatement(sql2);
				PreparedStatement s3 = conn.prepareStatement(sql3);
				PreparedStatement s4 = conn.prepareStatement(sql4);
				PreparedStatement s5 = conn.prepareStatement(sql5);
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			ResultSet r;

			// determine if the new password is the same as the old one
			String password = null;
			if (newPasswordHash != null && !newPasswordHash.equals("")) {
				password = newPasswordHash;
			} else {
				s5.setInt(1, coderId);
				r = s5.executeQuery();
				while(r.next()) {
					password = r.getString("Password");
				}
			}

			// update coder permissions and details
			s1.setString(1, coder.getName());
			s1.setInt(2, coder.getColor().getRed());
			s1.setInt(3, coder.getColor().getGreen());
			s1.setInt(4, coder.getColor().getBlue());
			s1.setString(5, password);
			s1.setInt(6, coder.isPermissionAddDocuments() ? 1 : 0);
			s1.setInt(7, coder.isPermissionEditDocuments() ? 1 : 0);
			s1.setInt(8, coder.isPermissionDeleteDocuments() ? 1 : 0);
			s1.setInt(9, coder.isPermissionImportDocuments() ? 1 : 0);
			s1.setInt(10, coder.isPermissionAddStatements() ? 1 : 0);
			s1.setInt(11, coder.isPermissionEditStatements() ? 1 : 0);
			s1.setInt(12, coder.isPermissionDeleteStatements() ? 1 : 0);
			s1.setInt(13, coder.isPermissionEditAttributes() ? 1 : 0);
			s1.setInt(14, coder.isPermissionEditRegex() ? 1 : 0);
			s1.setInt(15, coder.isPermissionEditStatementTypes() ? 1 : 0);
			s1.setInt(16, coder.isPermissionEditCoders() ? 1 : 0);
			s1.setInt(17, coder.isPermissionEditCoderRelations() ? 1 : 0);
			s1.setInt(18, coder.isPermissionViewOthersDocuments() ? 1 : 0);
			s1.setInt(19, coder.isPermissionEditOthersDocuments() ? 1 : 0);
			s1.setInt(20, coder.isPermissionViewOthersStatements() ? 1 : 0);
			s1.setInt(21, coder.isPermissionEditOthersStatements() ? 1 : 0);
			s1.setInt(22, coderId);
        	s1.executeUpdate();

        	// go through coder relations and update or insert
        	for (HashMap.Entry<Integer, CoderRelation> entry : coder.getCoderRelations().entrySet()) {
        		s3.setInt(1, coderId);
        		s3.setInt(2, entry.getValue().getTargetCoderId());
        		r = s3.executeQuery();
        		while (r.next()) {
        			if (r.getInt(1) == 0) {
        				// insert new coder relation
        				s4.setInt(1,  coderId);
        				s4.setInt(2, entry.getValue().getTargetCoderId());
        				s4.setInt(3, entry.getValue().isViewDocuments() ? 1 : 0);
        				s4.setInt(4, entry.getValue().isEditDocuments() ? 1 : 0);
        				s4.setInt(5, entry.getValue().isViewStatements() ? 1 : 0);
        				s4.setInt(6, entry.getValue().isEditStatements() ? 1 : 0);
        				s4.executeUpdate();
        			} else {
        				// update existing coder relation
        				s2.setInt(1, entry.getValue().isViewDocuments() ? 1 : 0);
        				s2.setInt(2, entry.getValue().isEditDocuments() ? 1 : 0);
        				s2.setInt(3, entry.getValue().isViewStatements() ? 1 : 0);
        				s2.setInt(4, entry.getValue().isEditStatements() ? 1 : 0);
        				s2.setInt(5,  coderId);
        				s2.setInt(6, entry.getValue().getTargetCoderId());
        				s2.executeUpdate();
        			}
        		}
        	}

        	conn.commit();
        	success = true;
        	LogEvent l = new LogEvent(Logger.MESSAGE,
        			"[SQL] Coder " + coderId + " successfully updated.",
        			"Coder " + coderId + " successfully updated.");
        	Dna.logger.log(l);
		} catch (SQLException e) {
			success = false;
        	LogEvent l = new LogEvent(Logger.ERROR,
        			"[SQL] Failed to update Coder " + coderId + " in the database.",
        			"Attempted to update Coder " + coderId + ", but the database access failed.",
        			e);
        	Dna.logger.log(l);
		}
		return success;
	}

	/**
	 * Delete a coder from the database. Note that this will also delete all
	 * documents, statements etc. created by the coder.
	 *
	 * @param coderId The ID of the coder to be deleted.
	 * @return        An indicator of whether the deletion was successful.
	 */
	public boolean deleteCoder(int coderId) {
		boolean success = false;
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("DELETE FROM CODERS WHERE ID = ?;")) {
			s.setInt(1, coderId);
			s.executeUpdate();
			success = true;
			LogEvent l = new LogEvent(Logger.MESSAGE,
        			"[SQL] Successfully deleted Coder " + coderId + " from the database.",
        			"Successfully deleted Coder " + coderId + " from the database.");
        	Dna.logger.log(l);
		} catch (SQLException e) {
			success = false;
        	LogEvent l = new LogEvent(Logger.ERROR,
        			"[SQL] Failed to delete Coder " + coderId + " from the database.",
        			"Attempted to delete Coder " + coderId + ", but the database operation failed.",
        			e);
        	Dna.logger.log(l);
		}
		return success;
	}

	/**
	 * Count how many documents and statements a coder owns.
	 *
	 * @param coderId  The ID of the coder.
	 * @return         An int[] array with frequency counts of documents and
	 *   statements.
	 */
	public int[] countCoderItems(int coderId) {
		int[] results = new int[2];
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT COUNT(ID) FROM DOCUMENTS WHERE Coder = ?;");
				PreparedStatement s2 = conn.prepareStatement("SELECT COUNT(ID) FROM STATEMENTS WHERE Coder = ?;")) {
			s1.setInt(1, coderId);
			ResultSet r1 = s1.executeQuery();
			while (r1.next()) {
				results[0] = r1.getInt(1);
			}
			s2.setInt(1, coderId);
			ResultSet r2 = s2.executeQuery();
			while (r2.next()) {
				results[1] = r2.getInt(1);
			}
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to count documents and statements for Coder " + coderId + ".",
        			"Attempted to count with how many documents and statements Coder " + coderId + " is associated with, but the database operation failed.",
        			e);
        	Dna.logger.log(l);
		}
		return results;
	}

	/**
	 * Authenticate a coder. Check if a user-provided clear-text password for
	 * the current coder or a provided coder ID matches the hash of the password
	 * stored for the coder in the database.
	 *
	 * @param coderId       ID of the coder to authenticate. Can be {@code -1}
	 *   if the currently active coder is supposed to be authenticated.
	 * @param clearPassword Clear-text password provided by the coder.
	 * @return              boolean value: {@code true} if the password matches,
	 *   {@code false} if not.
	 */
	public boolean authenticate(int coderId, String clearPassword) {
		String encryptedHash = null;
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("SELECT Password FROM CODERS WHERE ID = ?;")) {
			if (coderId > 0) {
				s.setInt(1, coderId);
			} else {
				s.setInt(1, this.cp.getCoderId());
			}
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
			if (correct) {
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
	 * @return          Array of generated document IDs.
	 */
	public int[] addDocuments(ArrayList<Document> documents) {
		int[] documentIds = new int[documents.size()];
		try (Connection conn = ds.getConnection();
				PreparedStatement stmt = conn.prepareStatement("INSERT INTO DOCUMENTS (Title, Text, Coder, Author, Source, Section, Notes, Type, Date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			ResultSet generatedKeysResultSet;
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

				// get generated document ID and save in array
				generatedKeysResultSet = stmt.getGeneratedKeys();
				while (generatedKeysResultSet.next()) {
					documentIds[i] = generatedKeysResultSet.getInt(1);
				}
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
		return documentIds;
	}

	/**
	 * Count the number of documents. Use an SQL query to get the number of rows
	 * in the {@code DOCUMENTS} table of the database.
	 *
	 * @return The number of documents.
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
	 * Used to determine in the {@link DocumentEditor}
	 * dialog whether the text field should be editable.
	 *
	 * @param documentIds  An array of document IDs.
	 * @return             boolean value indicating the presence of statements.
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
	 * Get documents for a batch of document IDs. The documents are of class
	 * {@link model.TableDocument TableDocument} and contain neither the
	 * document text nor any statement statements. They do contain the full
	 * coder and the number of statements at the time of execution of the method
	 * as a field. If the document ID list is empty, all documents are returned.
	 *
	 * @param documentIds    An array of document IDs for which the data should
	 *   be queried. Can be empty (to select all documents).
	 * @return             An {@link java.util.ArrayList ArrayList} of
	 *   {@link model.TableDocument TableDocument} objects, containing the
	 *   document meta-data.
	 */
	public ArrayList<TableDocument> getTableDocuments(int[] documentIds) {
		ArrayList<TableDocument> documents = new ArrayList<TableDocument>();
		String sql = "SELECT DOCUMENTS.ID, Title, Author, Source, Section, Type, Notes, Date, "
				+ "CODERS.ID AS CoderId, Name AS CoderName, Red, Green, Blue, "
				+ "COALESCE(Frequency, 0) AS Frequency "
				+ "FROM DOCUMENTS LEFT JOIN "
				+ "(SELECT DocumentId, COUNT(DocumentId) AS Frequency FROM STATEMENTS GROUP BY DocumentId) AS C ON C.DocumentId = DOCUMENTS.ID "
				+ "LEFT JOIN CODERS ON CODERS.ID = DOCUMENTS.Coder";
		if (documentIds.length > 0) {
			sql = sql + " WHERE DOCUMENTS.ID IN(";
			for (int i = 0; i < documentIds.length; i++) {
				sql = sql + documentIds[i];
				if (i < documentIds.length - 1) {
					sql = sql + ", ";
				}
			}
			sql = sql + ")";
		}
		sql = sql + ";";
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement(sql)) {
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				TableDocument d = new TableDocument(
						rs.getInt("ID"),
						rs.getString("Title"),
						rs.getInt("Frequency"),
						new Coder(rs.getInt("CoderId"),
								rs.getString("CoderName"),
								new Color(rs.getInt("Red"), rs.getInt("Green"), rs.getInt("Blue"))),
						rs.getString("Author"),
						rs.getString("Source"),
						rs.getString("Section"),
						rs.getString("Type"),
						rs.getString("Notes"),
						LocalDateTime.ofEpochSecond(rs.getLong("Date"), 0, ZoneOffset.UTC));
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
	 * Get documents for a batch of document IDs. The data can be displayed and
	 * edited in a {@link gui.DocumentEditor DocumentEditor} dialog. The
	 * documents do not contain any statements.
	 *
	 * @param documentIds    An array of document IDs for which the data should
	 *   be queried.
	 * @return             An {@link java.util.ArrayList ArrayList} of
	 *   {@link model.Document Document} objects, containing the documents and
	 *   their meta-data.
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
	 * Retrieve the minimum and maximum document date/time from the database.
	 *
	 * @return A LocalDateTime array with the minimum and maximum date/time.
	 */
	public LocalDateTime[] getDateTimeRange() {
		LocalDateTime[] range = new LocalDateTime[2];
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT MIN(DATE) FROM DOCUMENTS;");
				PreparedStatement s2 = conn.prepareStatement("SELECT MAX(DATE) FROM DOCUMENTS;")) {
			ResultSet r = s1.executeQuery();
			while (r.next()) {
			    range[0] = LocalDateTime.ofEpochSecond(r.getLong(1), 0, ZoneOffset.UTC);
			}
			r = s2.executeQuery();
			while (r.next()) {
			    range[1] = LocalDateTime.ofEpochSecond(r.getLong(1), 0, ZoneOffset.UTC);
			}
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to retrieve date/time range.",
					"Attempted to retrieve the minimum and maximum date/time stamp across all documents in the database.",
					e);
			Dna.logger.log(l);
		}
		return range;
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
	 * @param coder    A new coder ID ({@code -1} for keeping existing IDs)
	 * @param title    A new title.
	 * @param text     A new text.
	 * @param author   A new author.
	 * @param source   A new source.
	 * @param section  A new section.
	 * @param type     A new type.
	 * @param notes    A new notes String.
	 * @param date     A new date stamp (as a {@link java.time.LocalDate
	 *   LocalDate} object).
	 * @param time     A new time stamp (as a {@link java.time.LocalTime
	 *   LocalTime} object).
	 */
	public void updateDocuments(int[] documentIds, int coder, String title, String text, String author, String source, String section, String type, String notes, LocalDate date, LocalTime time) {
		String sel = "SELECT Title, Text, Author, Source, Section, Type, Notes, Date, Coder FROM DOCUMENTS WHERE ID = ?;";
		String upd = "UPDATE DOCUMENTS SET Title = ?, Text = ?, Author = ?, Source = ?, Section = ?, Type = ?, Notes = ?, Date = ?, Coder = ? WHERE ID = ?;";
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
			int existingCoder = -1;
			for (int i = 0; i < documentIds.length; i++) {
				s.setInt(1, documentIds[i]);
				r = s.executeQuery();
				while (r.next()) {
					titleTemp1 = Matcher.quoteReplacement(r.getString("Title"));
					textTemp1 = Matcher.quoteReplacement(r.getString("Text"));
					authorTemp1 = Matcher.quoteReplacement(r.getString("Author"));
					sectionTemp1 = Matcher.quoteReplacement(r.getString("Section"));
					sourceTemp1 = Matcher.quoteReplacement(r.getString("Source"));
					typeTemp1 = Matcher.quoteReplacement(r.getString("Type"));
					notesTemp1 = Matcher.quoteReplacement(r.getString("Notes"));
					dateTimeTemp = r.getLong("Date");
					ldt = LocalDateTime.ofEpochSecond(dateTimeTemp, 0, ZoneOffset.UTC);
					day = ldt.format(dayFormatter);
					month = ldt.format(monthFormatter);
					year = ldt.format(yearFormatter);
					hour = ldt.format(hourFormatter);
					minute = ldt.format(minuteFormatter);
					existingCoder = r.getInt("Coder");
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

				// date and time
				LocalDate ldtDate = ldt.toLocalDate();
				LocalTime ldtTime = ldt.toLocalTime();
				if (time == null && date == null) {
					// keep ldt as is
				} else if (time == null) {
					ldt = LocalDateTime.of(date, ldtTime);
				} else if (date == null) {
					ldt = LocalDateTime.of(ldtDate, time);
				} else {
					ldt = LocalDateTime.of(date, time);
				}
				u.setLong(8, ldt.toEpochSecond(ZoneOffset.UTC));

				if (coder < 1) {
					u.setInt(9, existingCoder);
				} else {
					u.setInt(9, coder);
				}
				u.setInt(10, documentIds[i]);
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
	 * @return             Were the documents successfully deleted?
	 */
	public boolean deleteDocuments(int[] documentIds) {
		boolean success = false;
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("DELETE FROM DOCUMENTS WHERE ID = ?"); // will cascade to statements
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			for (int i = 0; i < documentIds.length; i++) {
				s.setInt(1, documentIds[i]);
				s.executeUpdate();
			}
			conn.commit();
			success = true;
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
		return success;
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
	 * @return            The generated statement ID of the new statement.
	 */
	/*
	public int addStatement(Statement statement, int documentId) {
		long statementId = -1, entityId = -1, attributeVariableId = -1;
		try (Connection conn = ds.getConnection();
			 PreparedStatement s1 = conn.prepareStatement("INSERT INTO STATEMENTS (StatementTypeId, DocumentId, Start, Stop, Coder) VALUES (?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
			 PreparedStatement s2 = conn.prepareStatement("INSERT INTO DATASHORTTEXT (StatementId, RoleVariableLinkId, Entity) VALUES (?, ?, ?);");
			 PreparedStatement s3 = conn.prepareStatement("INSERT INTO DATALONGTEXT (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
			 PreparedStatement s4 = conn.prepareStatement("INSERT INTO DATAINTEGER (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
			 PreparedStatement s5 = conn.prepareStatement("INSERT INTO DATABOOLEAN (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
			 PreparedStatement s6 = conn.prepareStatement("INSERT INTO ENTITIES (VariableId, Value, Red, Green, Blue) VALUES (?, ?, ?, ?, ?);");
			 PreparedStatement s7 = conn.prepareStatement("SELECT ID FROM ENTITIES WHERE VariableId = ? AND Value = ?;");
			 PreparedStatement s8 = conn.prepareStatement("SELECT ID, AttributeVariable FROM ATTRIBUTEVARIABLES WHERE VariableId = ?;");
			 PreparedStatement s9 = conn.prepareStatement("INSERT INTO ATTRIBUTEVALUES (EntityId, AttributeVariableId, AttributeValue) VALUES (?, ?, ?);");
			 PreparedStatement s10 = conn.prepareStatement("SELECT COUNT(ID) FROM ENTITIES WHERE VariableId = ? AND Value = ?;");
			 PreparedStatement s11 = conn.prepareStatement("SELECT COUNT(ID) FROM ATTRIBUTEVALUES WHERE EntityId = ? AND AttributeVariableId = ?;");
			 PreparedStatement s12 = conn.prepareStatement("SELECT ROLEVARIABLELINKS.ID, ROLEVARIABLELINKS.RoleId, ROLEVARIABLELINKS.VariableId, ROLES.StatementTypeId FROM ROLEVARIABLELINKS INNER JOIN ROLES ON ROLES.ID = ROLEVARIABLELINKS.RoleID WHERE ROLES.StatementTypeId = ?;");
			 SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Started SQL transaction to add statement to Document " + documentId + ".",
					"Started a new SQL transaction to add a new statement to the document with ID " + documentId + ". The contents will not be written into the database until the transaction is committed.");
			Dna.logger.log(l);
			ResultSet r, r2;
			s1.setInt(1, statement.getStatementTypeId());
			s1.setInt(2, documentId);
			s1.setInt(3, statement.getStart());
			s1.setInt(4, statement.getStop());
			s1.setInt(5, statement.getCoderId());
			s1.executeUpdate();
			ResultSet generatedKeysResultSet = s1.getGeneratedKeys();
			while (generatedKeysResultSet.next()) {
				statementId = generatedKeysResultSet.getInt(1);
			}
			l = new LogEvent(Logger.MESSAGE,
					"[SQL]  ├─ Transaction: Row with ID " + statementId + " added to the STATEMENTS table.",
					"Added a row to the STATEMENTS table during the transaction. The new statement has ID " + statementId + ".");
			Dna.logger.log(l);
			for (int i = 0; i < statement.getValues().size(); i++) {
				// find ID in ROLEVARIABLELINKS table
				int variableId = -1;
				int roleId = -1;
				int roleVariableId = -1;
				s12.setInt(1, statement.getStatementTypeId());
				r = s12.executeQuery();
				while (r.next()) {
					roleVariableId = r.getInt("ID");
					roleId = r.getInt("RoleId");
					variableId = r.getInt("VariableId");
				}
				if (roleVariableId < 0 || roleId < 0 || variableId < 0) {
					l = new LogEvent(Logger.ERROR,
							"[SQL]  ├─ Failed to find role-variable ID for statement.",
							"Statement " + statementId + ": could not find role-variable ID (role ID " + roleId + "; variable ID: " + variableId + ").");
					Dna.logger.log(l);
					throw new SQLException();
				}

				// add to DATA... tables (and ENTITIES table if short text)
				if (statement.getValues().get(i).getDataType().equals("short text")) {
					// first, try to create an entity if it does not exist yet
					String value = "";
					if (statement.getValues().get(i).getValue() != null) {
						value = ((Entity) statement.getValues().get(i).getValue()).getValue(); // not sure if this case ever occurs
					}

					s10.setInt(1, variableId);
					s10.setString(2, value);
					r = s10.executeQuery();
					while (r.next()) {
						if (r.getInt(1) > 0) {
							// entity exists already; do nothing
						} else {
							// entity does not exist; add it to ENTITIES table
							s6.setInt(1, variableId);
							s6.setString(2, value);
							s6.setInt(3, 0);
							s6.setInt(4, 0);
							s6.setInt(5, 0);
							try {
								s6.executeUpdate();
								l = new LogEvent(Logger.MESSAGE,
										"[SQL]  ├─ Transaction: Added \"" + value + "\" to the ENTITIES table.",
										"Added a row with value \"" + value + "\" to the ENTITIES table during the transaction.");
								Dna.logger.log(l);
							} catch (SQLException e2) {
								l = new LogEvent(Logger.WARNING,
										"[SQL]  ├─ Failed to add value \"" + value + "\" to the ENTITIES table.",
										"Failed to add value \"" + value + "\" to the ENTITIES table. The next step will check if the attribute is already there. If so, no problem. If not, there will be another log event with an error message.",
										e2);
								Dna.logger.log(l);
							}
						}
					}

					// find the attribute ID for the attribute that was just added (or that may have already existed)
					entityId = -1;
					s7.setInt(1, variableId);
					s7.setString(2, value);
					r = s7.executeQuery();
					while (r.next()) {
						entityId = r.getInt("ID");
					}
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Entity ID identified as " + entityId + ".",
							"The entity \"" + value + "\", which was added to, or identified in, the ENTITIES table during the transaction, has ID " + entityId + ".");
					Dna.logger.log(l);

					// find attribute variable IDs for the entity and insert new values to the ATTRIBUTEVALUES table (catch errors if they already exist)
					s8.setInt(1, variableId); // set variable ID to find all attribute variables by ID corresponding to the entity
					r = s8.executeQuery();
					while (r.next()) {
						try {
							attributeVariableId = r.getInt("ID");
							s11.setLong(1, entityId);
							s11.setLong(2, attributeVariableId);
							r2 = s11.executeQuery();
							while (r2.next()) {
								if (r2.getInt(1) > 0) {
									// attribute value already exists in the ATTRIBUTEVALUES table; don't do anything
								} else {
									s9.setLong(1, entityId); // entity ID
									s9.setLong(2, attributeVariableId); // attribute variable ID
									s9.setString(3, ""); // put an empty value into the attribute value field initially
									s9.executeUpdate();
									l = new LogEvent(Logger.MESSAGE,
											"[SQL]  ├─ Transaction: Added attribute \"" + r.getString("AttributeVariable") + "\" for Entity " + entityId + " to the ATTRIBUTEVALUES table.",
											"Added attribute \"" + r.getString("AttributeVariable") + "\" for Entity " + entityId + " to the ATTRIBUTEVALUES table during the transaction.");
									Dna.logger.log(l);
								}
							}
						} catch (Exception e2) {
							l = new LogEvent(Logger.WARNING,
									"[SQL]  ├─ Failed to add a new value for attribute variable \"" + r.getString("AttributeVariable") + "\" for Entity " + entityId + " to the ATTRIBUTEVALUES table.",
									"Failed to add a new value for attribute variable \"" + r.getString("AttributeVariable") + "\" for Entity " + entityId + " to the ATTRIBUTEVALUES table. The next step will check if the attribute is already there. If so, no problem. If not, there will be another log event with an error message.",
									e2);
							Dna.logger.log(l);
						}
					}

					// finally, write into the DATASHORTTEXT table
					s2.setLong(1, statementId);
					s2.setInt(2, roleVariableId);
					s2.setLong(3, entityId);
					s2.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added an entity to the DATASHORTTEXT table.",
							"Added a row with entity ID " + entityId + " for Variable \"" + statement.getValues().get(i).getKey() + "\" (ID " + variableId + ") and Role \"" + statement.getValues().get(i).getRoleName() + "\" (ID " + roleId + " to the DATASHORTTEXT table during the transaction.");
					Dna.logger.log(l);
				} else if (statement.getValues().get(i).getDataType().equals("long text")) {
					s3.setLong(1, statementId);
					s3.setInt(2, roleVariableId);
					s3.setString(3, (String) statement.getValues().get(i).getValue());
					s3.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added a value to the DATALONGTEXT table.",
							"Added a row for Variable \"" + statement.getValues().get(i).getKey() + "\" (ID " + variableId + ") and Role \"" + statement.getValues().get(i).getRoleName() + "\" (ID " + roleId + ") to the DATALONGTEXT table during the transaction.");
					Dna.logger.log(l);
				} else if (statement.getValues().get(i).getDataType().equals("integer")) {
					s4.setLong(1, statementId);
					s4.setInt(2, roleVariableId);
					s4.setInt(3, (int) statement.getValues().get(i).getValue());
					s4.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added a value to the DATAINTEGER table.",
							"Added a row with Value " + (int) statement.getValues().get(i).getValue() + " for Variable \"" + statement.getValues().get(i).getKey() + "\" (ID " + variableId + ") and Role \"" + statement.getValues().get(i).getRoleName() + "\" (ID " + roleId + ") to the DATAINTEGER table during the transaction.");
					Dna.logger.log(l);
				} else if (statement.getValues().get(i).getDataType().equals("boolean")) {
					s5.setLong(1, statementId);
					s5.setInt(2, roleVariableId);
					s5.setInt(3, (int) statement.getValues().get(i).getValue());
					s5.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added a value to the DATABOOLEAN table.",
							"Added a row with Value " + (int) statement.getValues().get(i).getValue() + " for Variable \"" + statement.getValues().get(i).getKey() + "\" (ID " + variableId + ") and Role \"" + statement.getValues().get(i).getRoleName() + "\" (ID " + roleId + ") to the DATABOOLEAN table during the transaction.");
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
		return (int) statementId;
	}
	*/

	/**
	 * Update the coder ID and variable contents (with entities, but without their attributes) of multiple statements
	 * using new contents.
	 *
	 * @param tableStatements An array list of table statements to be updated in the database. (Attributes are ignored.)
	 * @return Were the statements successfully updated in the database?
	 */
	public boolean updateTableStatements(ArrayList<TableStatement> tableStatements) {
		boolean success = false;
		try (Connection conn = ds.getConnection();
			 PreparedStatement s1 = conn.prepareStatement("SELECT ID, RoleVariableLinkId, Value FROM DATABOOLEAN WHERE StatementId = ?;");
			 PreparedStatement s2 = conn.prepareStatement("SELECT ID, RoleVariableLinkId, Value FROM DATAINTEGER WHERE StatementId = ?;");
			 PreparedStatement s3 = conn.prepareStatement("SELECT ID, RoleVariableLinkId, Value FROM DATALONGTEXT WHERE StatementId = ?;");
			 PreparedStatement s4 = conn.prepareStatement("SELECT ID, RoleVariableLinkId, Entity FROM DATASHORTTEXT WHERE StatementId = ?;");
			 PreparedStatement s5 = conn.prepareStatement("SELECT ENTITIES.ID, ENTITIES.VariableId, ENTITIES.Value, ENTITIES.Red, ENTITIES.Green, ENTITIES.Blue FROM ENTITIES INNER JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.VariableId = ENTITIES.VariableId INNER JOIN ROLES ON ROLES.ID = ROLEVARIABLELINKS.RoleId WHERE ROLES.StatementTypeId = ?;");
			 PreparedStatement s6 = conn.prepareStatement("INSERT INTO DATABOOLEAN (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
			 PreparedStatement s7 = conn.prepareStatement("INSERT INTO DATAINTEGER (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
			 PreparedStatement s8 = conn.prepareStatement("INSERT INTO DATALONGTEXT (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
			 PreparedStatement s9 = conn.prepareStatement("INSERT INTO DATASHORTTEXT (StatementId, RoleVariableLinkId, Entity) VALUES (?, ?, ?);");
			 PreparedStatement s10 = conn.prepareStatement("INSERT INTO ENTITIES (VariableId, Value) VALUES (?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
			 PreparedStatement s11 = conn.prepareStatement("UPDATE DATABOOLEAN SET Value = ? WHERE ID = ?;");
			 PreparedStatement s12 = conn.prepareStatement("UPDATE DATAINTEGER SET Value = ? WHERE ID = ?;");
			 PreparedStatement s13 = conn.prepareStatement("UPDATE DATALONGTEXT SET Value = ? WHERE ID = ?;");
			 PreparedStatement s14 = conn.prepareStatement("UPDATE DATASHORTTEXT SET Entity = ? WHERE ID = ?;");
			 PreparedStatement s15 = conn.prepareStatement("DELETE FROM DATABOOLEAN WHERE ID = ?;");
			 PreparedStatement s16 = conn.prepareStatement("DELETE FROM DATAINTEGER WHERE ID = ?;");
			 PreparedStatement s17 = conn.prepareStatement("DELETE FROM DATALONGTEXT WHERE ID = ?;");
			 PreparedStatement s18 = conn.prepareStatement("DELETE FROM DATASHORTTEXT WHERE ID = ?;");
			 PreparedStatement s19 = conn.prepareStatement("UPDATE STATEMENTS SET Coder = ? WHERE ID = ?;");
			 SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			LogEvent e1 = new LogEvent(Logger.MESSAGE,
					"[SQL] Started SQL transaction to update " + tableStatements.size() + " statements.",
					"Started a new SQL transaction to update the variables in a set of " + tableStatements.size() + " statements. The contents will not be written into the database until the transaction is committed.");
			Dna.logger.log(e1);

			class DataResult {
				int id, roleVariableLinkId, valueInt;
				String valueString, dataType;

				public String toString() {
					if (this.dataType.equals("short text") || this.dataType.equals("long text")) {
						return this.valueString;
					} else {
						return "" + this.valueInt;
					}
				}
			}

			ResultSet r1;
			ArrayList<DataResult> data;
			ArrayList<Entity> entities;
			List<RoleValue> insertionPile;
			List<DataResult> deletionPile;
			for (TableStatement s : tableStatements) {
				data = new ArrayList<>();
				entities = new ArrayList<>();

				// get boolean data entries from database corresponding to current statement
				s1.setInt(1, s.getId());
				r1 = s1.executeQuery();
				while (r1.next()) {
					DataResult dr = new DataResult();
					dr.id = r1.getInt("ID");
					dr.roleVariableLinkId = r1.getInt("RoleVariableLinkId");
					dr.valueInt = r1.getInt("Value");
					dr.dataType = "boolean";
					data.add(dr);
				}

				// get integer data entries from database corresponding to current statement
				s2.setInt(1, s.getId());
				r1 = s2.executeQuery();
				while (r1.next()) {
					DataResult dr = new DataResult();
					dr.id = r1.getInt("ID");
					dr.roleVariableLinkId = r1.getInt("RoleVariableLinkId");
					dr.valueInt = r1.getInt("Value");
					dr.dataType = "integer";
					data.add(dr);
				}

				// get long text data entries from database corresponding to current statement
				s3.setInt(1, s.getId());
				r1 = s3.executeQuery();
				while (r1.next()) {
					DataResult dr = new DataResult();
					dr.id = r1.getInt("ID");
					dr.roleVariableLinkId = r1.getInt("RoleVariableLinkId");
					dr.valueString = r1.getString("Value");
					dr.dataType = "long text";
					data.add(dr);
				}

				// get short text data entries from database corresponding to current statement
				s4.setInt(1, s.getId());
				r1 = s4.executeQuery();
				while (r1.next()) {
					DataResult dr = new DataResult();
					dr.id = r1.getInt("ID");
					dr.roleVariableLinkId = r1.getInt("RoleVariableLinkId");
					dr.valueInt = r1.getInt("Entity");
					dr.dataType = "short text";
					data.add(dr);
				}

				// get entities corresponding to the current statement type belonging to current statement
				s5.setInt(1, s.getStatementTypeId());
				r1 = s5.executeQuery();
				while (r1.next()) {
					entities.add(new Entity(r1.getInt("ID"),
							r1.getInt("VariableId"),
							r1.getString("Value"),
							new Color(r1.getInt("Red"), r1.getInt("Green"), r1.getInt("Blue"))));
				}

				// go through valueRoles in statement and put those that don't exist in DATA* onto a pile for database insertion
				List<DataResult> finalData = data;
				insertionPile = s.getRoleValues()
						.stream()
						.filter(v -> finalData
								.stream()
								.noneMatch(d -> d.dataType.equals(v.getDataType()) &&
										d.roleVariableLinkId == v.getRoleVariableLinkId() &&
										((d.dataType.equals("long text") && d.valueString.equals((String) v.getValue())) ||
												(d.dataType.equals("short text") && d.valueInt == ((Entity) v.getValue()).getId()) ||
												((d.dataType.equals("boolean") || d.dataType.equals("integer")) && d.valueInt == (int) v.getValue()))))
						.collect(Collectors.toList());

				// create a set of data entries to remove from database because they don't exist anymore in the statement
				deletionPile = data
						.stream()
						.filter(d -> s.getRoleValues()
								.stream()
								.noneMatch(v -> v.getDataType().equals(d.dataType) &&
										v.getRoleVariableLinkId() == d.roleVariableLinkId &&
										((v.getDataType().equals("long text") && v.getValue().equals(d.valueString)) ||
												(v.getDataType().equals("short text") && d.valueInt == ((Entity) v.getValue()).getId()) ||
												((v.getDataType().equals("boolean") || v.getDataType().equals("integer")) && (int) v.getValue() == d.valueInt))))
						.collect(Collectors.toList());

				// insert missing entities into the database first, then insert data entries
				for (int j = 0; j < insertionPile.size(); j++) {
					if (insertionPile.get(j).getDataType().equals("short text")) {
						boolean match = false;
						for (int k = 0; k < entities.size(); k++) {
							if (entities.get(k).getValue().equals(((Entity) insertionPile.get(j).getValue()).getValue()) && entities.get(k).getVariableId() == insertionPile.get(j).getVariableId()) {
								match = true;
							}
						}
						if (!match) {
							s10.setInt(1, insertionPile.get(j).getVariableId());
							s10.setString(2, ((Entity) insertionPile.get(j).getValue()).getValue());
							s10.executeUpdate();

							// get generated primary key and save in entity in insertion pile as entity ID
							r1 = s10.getGeneratedKeys();
							while (r1.next()) {
								((Entity) insertionPile.get(j).getValue()).setId(r1.getInt(1));
							}
						}
					}
				}

				// go through deletion pile and insertion pile together and look for commonalities, then rename those in database and remove from piles
				for (int j = deletionPile.size() - 1; j >= 0; j--) {
					for (int k = insertionPile.size() - 1; k >= 0; k--) {
						if (deletionPile.get(j).roleVariableLinkId == insertionPile.get(k).getRoleVariableLinkId()) {
							if (deletionPile.get(j).dataType.equals("long text") &&
									insertionPile.get(k).getDataType().equals("long text") &&
									!deletionPile.get(j).valueString.equals(insertionPile.get(k).getValue())) {
								s13.setString(1, (String) insertionPile.get(k).getValue());
								s13.setInt(2, deletionPile.get(j).id);
								s13.executeUpdate();
								deletionPile.remove(j);
								insertionPile.remove(k);
								break;
							} else if (deletionPile.get(j).dataType.equals("short text") &&
									insertionPile.get(k).getDataType().equals("short text") &&
									deletionPile.get(j).valueInt != ((Entity) insertionPile.get(k).getValue()).getId()) {
								s14.setInt(1, ((Entity) insertionPile.get(k).getValue()).getId());
								s14.setInt(2, deletionPile.get(j).id);
								s14.executeUpdate();
								deletionPile.remove(j);
								insertionPile.remove(k);
								break;
							} else if (deletionPile.get(j).valueInt != (int) insertionPile.get(k).getValue() &&
									deletionPile.get(j).dataType.equals(insertionPile.get(k).getDataType())) {
								if (deletionPile.get(j).dataType.equals("boolean")) {
									s11.setInt(1, (int) insertionPile.get(k).getValue());
									s11.setInt(2, deletionPile.get(j).id);
									s11.executeUpdate();
								} else if (deletionPile.get(j).dataType.equals("integer")) {
									s12.setInt(1, (int) insertionPile.get(k).getValue());
									s12.setInt(2, deletionPile.get(j).id);
									s12.executeUpdate();
								}
								deletionPile.remove(j);
								insertionPile.remove(k);
								break;
							}
						}
					}
				}

				// delete entries
				for (int j = 0; j < deletionPile.size(); j++) {
					if (deletionPile.get(j).dataType.equals("boolean")) {
						s15.setInt(1, deletionPile.get(j).id);
						s15.executeUpdate();
					} else if (deletionPile.get(j).dataType.equals("integer")) {
						s16.setInt(1, deletionPile.get(j).id);
						s16.executeUpdate();
					} else if (deletionPile.get(j).dataType.equals("long text")) {
						s17.setInt(1, deletionPile.get(j).id);
						s17.executeUpdate();
					} else if (deletionPile.get(j).dataType.equals("short text")) {
						s18.setInt(1, deletionPile.get(j).id);
						s18.executeUpdate();
					}
				}

				// insert entries
				for (int j = 0; j < insertionPile.size(); j++) {
					if (insertionPile.get(j).getDataType().equals("boolean")) {
						s6.setInt(1, s.getId());
						s6.setInt(2, insertionPile.get(j).getRoleVariableLinkId());
						s6.setInt(3, (int) insertionPile.get(j).getValue());
						s6.executeUpdate();
					} else if (insertionPile.get(j).getDataType().equals("integer")) {
						s7.setInt(1, s.getId());
						s7.setInt(2, insertionPile.get(j).getRoleVariableLinkId());
						s7.setInt(3, (int) insertionPile.get(j).getValue());
						s7.executeUpdate();
					} else if (insertionPile.get(j).getDataType().equals("long text")) {
						s8.setInt(1, s.getId());
						s8.setInt(2, insertionPile.get(j).getRoleVariableLinkId());
						s8.setString(3, (String) insertionPile.get(j).getValue());
						s8.executeUpdate();
					} else if (insertionPile.get(j).getDataType().equals("short text")) {
						s9.setInt(1, s.getId());
						s9.setInt(2, insertionPile.get(j).getRoleVariableLinkId());
						s9.setInt(3, ((Entity) insertionPile.get(j).getValue()).getId());
						s9.executeUpdate();
					}
				}

				// update coder
				s19.setInt(1, s.getCoderId());
				s19.setInt(2, s.getId());
				s19.executeUpdate();

				LogEvent e2 = new LogEvent(Logger.MESSAGE,
						"[SQL]  ├─ Updated Statement " + s.getId() + " in the transaction.",
						"Statement " + s.getId() + " was updated in the SQL transaction. The transaction is ongoing and has not been committed to the database yet.");
				Dna.logger.log(e2);
			}

			conn.commit();
			success = true;
			LogEvent e2 = new LogEvent(Logger.MESSAGE,
					"[SQL]  └─ Completed SQL transaction to update " + tableStatements.size() + " statement(s).",
					"Completed SQL transaction to update the variables in " + tableStatements.size() + " statement(s). The contents have been written into the database.");
			Dna.logger.log(e2);
		} catch (SQLException e) {
			LogEvent e2 = new LogEvent(Logger.ERROR,
					"[SQL]  └─ Statements could not be updated in the database.",
					"When trying to update statement details in the database, something went wrong. Maybe another coder concurrently removed the statements you were working on, or maybe there was a connection issue. See exception below.",
					e);
			Dna.logger.log(e2);
		}
		return success;
	}

	/**
	 * Create a copy of a statement in the database.
	 *
	 * @param statementId  The ID of the statement to be cloned.
	 * @param newCoderId   The ID of the coder who will own the statement copy.
	 * @return             The ID of the new (cloned) statement.
	 */
	public int cloneStatement(int statementId, int newCoderId) {
		int id = statementId;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("INSERT INTO STATEMENTS (StatementTypeId, DocumentId, Start, Stop, Coder) SELECT StatementTypeId, DocumentId, Start, Stop, Coder FROM STATEMENTS WHERE ID = ?;", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement s2 = conn.prepareStatement("UPDATE STATEMENTS SET Coder = ? WHERE ID = ?;");
				PreparedStatement s3 = conn.prepareStatement("SELECT RoleVariableLinkId, Value FROM DATABOOLEAN WHERE StatementId = ?;");
				PreparedStatement s4 = conn.prepareStatement("INSERT INTO DATABOOLEAN (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
				PreparedStatement s5 = conn.prepareStatement("SELECT RoleVariableLinkId, Value FROM DATAINTEGER WHERE StatementId = ?;");
				PreparedStatement s6 = conn.prepareStatement("INSERT INTO DATAINTEGER (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
				PreparedStatement s7 = conn.prepareStatement("SELECT RoleVariableLinkId, Entity FROM DATASHORTTEXT WHERE StatementId = ?;");
				PreparedStatement s8 = conn.prepareStatement("INSERT INTO DATASHORTTEXT (StatementId, RoleVariableLinkId, Entity) VALUES (?, ?, ?);");
				PreparedStatement s9 = conn.prepareStatement("SELECT RoleVariableLinkId, Value FROM DATALONGTEXT WHERE StatementId = ?;");
				PreparedStatement s10 = conn.prepareStatement("INSERT INTO DATALONGTEXT (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
				SQLCloseable finish = conn::rollback) {
			ResultSet r;
			conn.setAutoCommit(false);

			// copy the statement in the STATEMENTS table
			s1.setInt(1, statementId);
			s1.executeUpdate();
			ResultSet generatedKeysResultSet = s1.getGeneratedKeys();
			while (generatedKeysResultSet.next()) {
				id = generatedKeysResultSet.getInt(1);
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
				s4.setInt(2, r.getInt("RoleVariableLinkId"));
				s4.setInt(3, r.getInt("Value"));
				s4.executeUpdate();
			}

			// clone relevant entries in the DATAINTEGER table
			s5.setInt(1, statementId);
			r = s5.executeQuery();
			while (r.next()) {
				s6.setInt(1, id);
				s6.setInt(2, r.getInt("RoleVariableLinkId"));
				s6.setInt(3, r.getInt("Value"));
				s6.executeUpdate();
			}

			// clone relevant entries in the DATASHORTTEXT table
			s7.setInt(1, statementId);
			r = s7.executeQuery();
			while (r.next()) {
				s8.setInt(1, id);
				s8.setInt(2, r.getInt("RoleVariableLinkId"));
				s8.setInt(3, r.getInt("Entity"));
				s8.executeUpdate();
			}

			// clone relevant entries in the DATALONGTEXT table
			s9.setInt(1, statementId);
			r = s9.executeQuery();
			while (r.next()) {
				s10.setInt(1, id);
				s10.setInt(2, r.getInt("RoleVariableLinkId"));
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
	 * @param statementId The statement ID of the statement to be retrieved.
	 * @return A {@link model.TableStatement Statement} with all relevant values for the different roles and variables.
	 */
	public TableStatement getTableStatement(int statementId) {
		TableStatement tableStatement = null;
		ArrayList<RoleValue> roleValues;
		int statementTypeId, variableId, roleId, roleVariableLinkId, entityId;
		String variableName, roleName, dataType;
		Color aColor, sColor, cColor;
		HashMap<String, String> map;
		String subString = "SUBSTRING(DOCUMENTS.Text, Start + 1, Stop - Start) AS Text ";
		if (getConnectionProfile().getType().equals("postgresql")) {
			subString = "SUBSTRING(DOCUMENTS.Text, CAST(Start + 1 AS INT4), CAST(Stop - Start AS INT4)) AS Text ";
		}
		String s1Query = "SELECT STATEMENTS.ID AS StatementId, "
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
				+ subString
				+ "FROM STATEMENTS "
				+ "INNER JOIN CODERS ON STATEMENTS.Coder = CODERS.ID "
				+ "INNER JOIN STATEMENTTYPES ON STATEMENTS.StatementTypeId = STATEMENTTYPES.ID "
				+ "INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId "
				+ "WHERE STATEMENTS.ID = ?;";
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement(s1Query);
				PreparedStatement s2 = conn.prepareStatement("SELECT ROLEVARIABLELINKS.ID AS RoleVariableLinkId, ROLES.ID AS RoleId, RoleName, VariableId, Variable AS VariableName, DataType FROM ROLES INNER JOIN ROLEVARIABLELINKS INNER JOIN VARIABLES WHERE ROLES.ID = ROLEVARIABLELINKS.RoleId AND VariableId = VARIABLES.ID AND StatementTypeId = ?;");
				PreparedStatement s3 = conn.prepareStatement("SELECT E.ID AS EntityId, StatementId, E.VariableId, DST.RoleVariableLinkId, E.Value, Red, Green, Blue, ChildOf FROM DATASHORTTEXT AS DST LEFT JOIN ENTITIES AS E ON E.ID = DST.Entity WHERE DST.StatementId = ? AND DST.RoleVariableLinkId = ?;");
				PreparedStatement s4 = conn.prepareStatement("SELECT Value FROM DATALONGTEXT WHERE RoleVariableLinkId = ? AND StatementId = ?;");
				PreparedStatement s5 = conn.prepareStatement("SELECT Value FROM DATAINTEGER WHERE RoleVariableLinkId = ? AND StatementId = ?;");
				PreparedStatement s6 = conn.prepareStatement("SELECT Value FROM DATABOOLEAN WHERE RoleVariableLinkId = ? AND StatementId = ?;");
				PreparedStatement s7 = conn.prepareStatement("SELECT AttributeVariable, AttributeValue FROM ATTRIBUTEVALUES AS AVAL INNER JOIN ATTRIBUTEVARIABLES AS AVAR ON AVAL.AttributeVariableId = AVAR.ID WHERE EntityId = ?;")) {
			ResultSet r1, r2, r3, r4;

			// first, get the statement information, including coder and statement type info
			s1.setInt(1, statementId);
			r1 = s1.executeQuery();
			while (r1.next()) {
			    statementTypeId = r1.getInt("StatementTypeId");
			    sColor = new Color(r1.getInt("StatementTypeRed"), r1.getInt("StatementTypeGreen"), r1.getInt("StatementTypeBlue"));
			    cColor = new Color(r1.getInt("CoderRed"), r1.getInt("CoderGreen"), r1.getInt("CoderBlue"));

			    // second, get the role-variable combinations associated with the statement type and their values
				roleValues = new ArrayList<RoleValue>();
				s2.setInt(1, statementTypeId);
				r2 = s2.executeQuery();
				while (r2.next()) {
					roleVariableLinkId = r2.getInt("RoleVariableLinkId");
					roleId = r2.getInt("RoleId");
					roleName = r2.getString("RoleName");
					variableId = r2.getInt("VariableId");
					variableName = r2.getString("VariableName");
					dataType = r2.getString("DataType");

					// third, get the values from DATABOOLEAN, DATAINTEGER, DATALONGTEXT, and DATASHORTTEXT
			    	if (dataType.equals("short text")) {
				    	s3.setInt(1, statementId);
			    		s3.setInt(2, roleVariableLinkId);
				    	r3 = s3.executeQuery();
				    	while (r3.next()) {
				    		entityId = r3.getInt("EntityId");
			            	aColor = new Color(r3.getInt("Red"), r3.getInt("Green"), r3.getInt("Blue"));

			            	// fourth, in the case of short text, also look up information in ENTITIES table
			            	s7.setInt(1, entityId);
			            	r4 = s7.executeQuery();
			            	map = new HashMap<String, String>();
			            	while (r4.next()) {
			            		map.put(r4.getString("AttributeVariable"), r4.getString("AttributeValue"));
			            	}
			            	Entity entity = new Entity(entityId, variableId, r3.getString("Value"), aColor, r3.getInt("ChildOf"), true, map);
							roleValues.add(new RoleValue(variableId, variableName, dataType, entity, roleVariableLinkId, roleId, roleName, statementTypeId));
				    	}
			    	} else if (dataType.equals("long text")) {
				    	s4.setInt(1, roleVariableLinkId);
				    	s4.setInt(2, statementId);
				    	r3 = s4.executeQuery();
				    	while (r3.next()) {
							roleValues.add(new RoleValue(variableId, variableName, dataType, r3.getString("Value"), roleVariableLinkId, roleId, roleName, statementTypeId));
				    	}
			    	} else if (dataType.equals("integer")) {
				    	s5.setInt(1, roleVariableLinkId);
				    	s5.setInt(2, statementId);
				    	r3 = s5.executeQuery();
				    	while (r3.next()) {
							roleValues.add(new RoleValue(variableId, variableName, dataType, r3.getInt("Value"), roleVariableLinkId, roleId, roleName, statementTypeId));
				    	}
			    	} else if (dataType.equals("boolean")) {
				    	s6.setInt(1, roleVariableLinkId);
				    	s6.setInt(2, statementId);
				    	r3 = s6.executeQuery();
				    	while (r3.next()) {
							roleValues.add(new RoleValue(variableId, variableName, dataType, r3.getInt("Value"), roleVariableLinkId, roleId, roleName, statementTypeId));
				    	}
			    	}
			    }

			    // assemble the table statement with all the information from the previous steps
			    tableStatement = new TableStatement(statementId,
			    		r1.getInt("Start"),
			    		r1.getInt("Stop"),
			    		statementTypeId,
						r1.getInt("CoderId"),
						r1.getInt("DocumentId"),
						LocalDateTime.ofEpochSecond(r1.getLong("Date"), 0, ZoneOffset.UTC),
						r1.getString("Text"),
						r1.getString("CoderName"),
						cColor,
						r1.getString("StatementTypeLabel"),
						sColor,
			    		roleValues);
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
		return tableStatement;
	}

	/**
	 * Get statements, potentially filtered by statement IDs, statement type
	 * IDs, document meta-data, date/time range, and duplicates setting.
	 *
	 * @param statementIds Array of statement IDs to retrieve. Can be empty or
	 *   {@code null}, in which case all statements are selected.
	 * @param statementTypeId Array list of statement type IDs to include. Can
	 *   be empty or {@code null}, in which case all statement types are
	 *   selected.
	 * @param startDateTime Date/time before which statements are discarded.
	 * @param stopDateTime Date/time after which statements are discarded.
	 * @param authors Array list of document authors to exclude. Can be empty or
	 *   {@code null}, in which case all statements are selected.
	 * @param authorInclude Include authors instead of excluding them?
	 * @param sources Array list of document sources to exclude. Can be empty or
	 *   {@code null}, in which case all statements are selected.
	 * @param sourceInclude Include sources instead of excluding them?
	 * @param sections Array list of document sections to exclude. Can be empty
	 *   or {@code null}, in which case all statements are selected.
	 * @param sectionInclude Include sections instead of excluding them?
	 * @param types Array list of document types to exclude. Can be empty or
	 *   {@code null}, in which case all statements are selected.
	 * @param typeInclude Include types instead of excluding them?
	 * @return Array list of statements with all details.
	 */
	/*
	public ArrayList<Statement> getStatements(
			int[] statementIds,
			int statementTypeId,
			LocalDateTime startDateTime,
			LocalDateTime stopDateTime,
			ArrayList<String> authors,
			boolean authorInclude,
			ArrayList<String> sources,
			boolean sourceInclude,
			ArrayList<String> sections,
			boolean sectionInclude,
			ArrayList<String> types,
			boolean typeInclude) {
		String whereStatements = "";
		String whereShortText = "";
		String whereLongText = "";
		String whereBoolean = "";
		String whereInteger = "";
		if (statementIds != null && statementIds.length > 0) {
			String ids = "";
			for (int i = 0; i < statementIds.length; i++) {
				ids = ids + statementIds[i];
				if (i < statementIds.length - 1) {
					ids = ids + ", ";
				}
			}
			whereStatements = "WHERE STATEMENTS.ID IN (" + ids + ") ";
			whereShortText = "AND DATASHORTTEXT.StatementId IN (" + ids + ") ";
			whereLongText = "AND DATALONGTEXT.StatementId IN (" + ids + ") ";
			whereBoolean = "AND DATABOOLEAN.StatementId IN (" + ids + ") ";
			whereInteger = "AND DATAINTEGER.StatementId IN (" + ids + ") ";
		}
		if (startDateTime != null) {
			whereStatements = whereStatements + "AND Date >= " + startDateTime.toEpochSecond(ZoneOffset.UTC) + " ";
			whereShortText = whereShortText + "AND Date >= " + startDateTime.toEpochSecond(ZoneOffset.UTC) + " ";
			whereLongText = whereLongText + "AND Date >= " + startDateTime.toEpochSecond(ZoneOffset.UTC) + " ";
			whereBoolean = whereBoolean + "AND Date >= " + startDateTime.toEpochSecond(ZoneOffset.UTC) + " ";
			whereInteger = whereInteger + "AND Date >= " + startDateTime.toEpochSecond(ZoneOffset.UTC) + " ";
		}
		if (stopDateTime != null) {
			whereStatements = whereStatements + "AND Date <= " + stopDateTime.toEpochSecond(ZoneOffset.UTC) + " ";
			whereShortText = whereShortText + "AND Date <= " + stopDateTime.toEpochSecond(ZoneOffset.UTC) + " ";
			whereLongText = whereLongText + "AND Date <= " + stopDateTime.toEpochSecond(ZoneOffset.UTC) + " ";
			whereBoolean = whereBoolean + "AND Date <= " + stopDateTime.toEpochSecond(ZoneOffset.UTC) + " ";
			whereInteger = whereInteger + "AND Date <= " + stopDateTime.toEpochSecond(ZoneOffset.UTC) + " ";
		}
		if (authors != null && authors.size() > 0) {
			String authorNot = "";
			if (!authorInclude) {
				authorNot = "NOT ";
			}
			String authorWhere = "AND DOCUMENTS.Author "
					+ authorNot
					+ "IN ('"
					+ authors.stream().collect(Collectors.joining("', '"))
					+ "') ";
			whereStatements = whereStatements + authorWhere;
			whereShortText = whereShortText + authorWhere;
			whereLongText = whereLongText + authorWhere;
			whereBoolean = whereBoolean + authorWhere;
			whereInteger = whereInteger + authorWhere;
		}
		if (sources != null && sources.size() > 0) {
			String sourceNot = "";
			if (!sourceInclude) {
				sourceNot = "NOT ";
			}
			String sourceWhere = "AND DOCUMENTS.Source "
					+ sourceNot
					+ "IN ('"
					+ sources.stream().collect(Collectors.joining("', '"))
					+ "') ";
			whereStatements = whereStatements + sourceWhere;
			whereShortText = whereShortText + sourceWhere;
			whereLongText = whereLongText + sourceWhere;
			whereBoolean = whereBoolean + sourceWhere;
			whereInteger = whereInteger + sourceWhere;
		}
		if (sections != null && sections.size() > 0) {
			String sectionNot = "";
			if (!sectionInclude) {
				sectionNot = "NOT ";
			}
			String sectionWhere = "AND DOCUMENTS.Section "
					+ sectionNot
					+ "IN ('"
					+ sections.stream().collect(Collectors.joining("', '"))
					+ "') ";
			whereStatements = whereStatements + sectionWhere;
			whereShortText = whereShortText + sectionWhere;
			whereLongText = whereLongText + sectionWhere;
			whereBoolean = whereBoolean + sectionWhere;
			whereInteger = whereInteger + sectionWhere;
		}
		if (types != null && types.size() > 0) {
			String typeNot = "";
			if (!typeInclude) {
				typeNot = "NOT ";
			}
			String typeWhere = "AND DOCUMENTS.Type "
					+ typeNot
					+ "IN ('"
					+ types.stream().collect(Collectors.joining("', '"))
					+ "') ";
			whereStatements = whereStatements + typeWhere;
			whereShortText = whereShortText + typeWhere;
			whereLongText = whereLongText + typeWhere;
			whereBoolean = whereBoolean + typeWhere;
			whereInteger = whereInteger + typeWhere;
		}
		if (whereStatements.startsWith("AND")) { // ensure correct form if no statement ID filtering
			whereStatements = whereStatements.replaceFirst("AND", "WHERE");
		}

		String subString = "SUBSTRING(DOCUMENTS.Text, Start + 1, Stop - Start) AS Text ";
		if (Dna.sql.getConnectionProfile().getType().equals("postgresql")) {
			subString = "SUBSTRING(DOCUMENTS.Text, CAST(Start + 1 AS INT4), CAST(Stop - Start AS INT4)) AS Text ";
		}
		String q1 = "SELECT STATEMENTS.ID AS StatementId, "
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
				+ subString
				+ "FROM STATEMENTS "
				+ "INNER JOIN CODERS ON STATEMENTS.Coder = CODERS.ID "
				+ "INNER JOIN STATEMENTTYPES ON STATEMENTS.StatementTypeId = STATEMENTTYPES.ID "
				+ "INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId "
				+ whereStatements
				+ "AND STATEMENTTYPES.ID = " + statementTypeId + " "
				+ "ORDER BY DOCUMENTS.DATE ASC;";

		String q4a = "SELECT DATASHORTTEXT.StatementId, ROLEVARIABLELINKS.RoleId, ROLES.RoleName, ROLEVARIABLELINKS.VariableId, Variable AS VariableName, ENTITIES.ID AS EntityId, ENTITIES.Value AS Value, ENTITIES.Red AS Red, ENTITIES.Green AS Green, ENTITIES.Blue AS Blue, ENTITIES.ChildOf AS ChildOf FROM DATASHORTTEXT " +
				"INNER JOIN ROLEVARIABLELINKS ON DATASHORTTEXT.RoleVariableLinkId = ROLEVARIABLELINKS.ID " +
				"INNER JOIN ROLES ON ROLEVARIABLELINKS.RoleId = ROLES.ID " +
				"INNER JOIN VARIABLES ON ROLEVARIABLELINKS.VariableId = VARIABLES.ID " +
				"INNER JOIN ENTITIES ON ENTITIES.VariableId = VARIABLES.ID AND ENTITIES.ID = DATASHORTTEXT.Entity " +
				"INNER JOIN STATEMENTS ON STATEMENTS.ID = DATASHORTTEXT.StatementId " +
				"INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId " +
				"WHERE ROLES.StatementTypeId = " + statementTypeId + " " + whereShortText + "ORDER BY 1, 2, 4 ASC;";
		String q4b = "SELECT DATALONGTEXT.StatementId, ROLEVARIABLELINKS.RoleId, ROLES.RoleName, ROLEVARIABLELINKS.VariableId, Variable AS VariableName, DATALONGTEXT.Value FROM DATALONGTEXT " +
				"INNER JOIN ROLEVARIABLELINKS ON DATALONGTEXT.RoleVariableLinkId = ROLEVARIABLELINKS.ID " +
				"INNER JOIN ROLES ON ROLEVARIABLELINKS.RoleId = ROLES.ID " +
				"INNER JOIN VARIABLES ON ROLEVARIABLELINKS.VariableId = VARIABLES.ID " +
				"INNER JOIN STATEMENTS ON STATEMENTS.ID = DATALONGTEXT.StatementId " +
				"INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId " +
				"WHERE ROLES.StatementTypeId = " + statementTypeId + " " + whereLongText + "ORDER BY 1, 2, 4 ASC;";
		String q4c = "SELECT DATABOOLEAN.StatementId, ROLEVARIABLELINKS.RoleId, ROLES.RoleName, ROLEVARIABLELINKS.VariableId, Variable AS VariableName, DATABOOLEAN.Value FROM DATABOOLEAN " +
				"INNER JOIN ROLEVARIABLELINKS ON DATABOOLEAN.RoleVariableLinkId = ROLEVARIABLELINKS.ID " +
				"INNER JOIN ROLES ON ROLEVARIABLELINKS.RoleId = ROLES.ID " +
				"INNER JOIN VARIABLES ON ROLEVARIABLELINKS.VariableId = VARIABLES.ID " +
				"INNER JOIN STATEMENTS ON STATEMENTS.ID = DATABOOLEAN.StatementId " +
				"INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId " +
				"WHERE ROLES.StatementTypeId = " + statementTypeId + " " + whereBoolean + "ORDER BY 1, 2, 4 ASC;";
		String q4d = "SELECT DATAINTEGER.StatementId, ROLEVARIABLELINKS.RoleId, ROLES.RoleName, ROLEVARIABLELINKS.VariableId, Variable AS VariableName, DATAINTEGER.Value FROM DATAINTEGER " +
				"INNER JOIN ROLEVARIABLELINKS ON DATAINTEGER.RoleVariableLinkId = ROLEVARIABLELINKS.ID " +
				"INNER JOIN ROLES ON ROLEVARIABLELINKS.RoleId = ROLES.ID " +
				"INNER JOIN VARIABLES ON ROLEVARIABLELINKS.VariableId = VARIABLES.ID " +
				"INNER JOIN STATEMENTS ON STATEMENTS.ID = DATAINTEGER.StatementId " +
				"INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId " +
				"WHERE ROLES.StatementTypeId = " + statementTypeId + " " + whereInteger + "ORDER BY 1, 2, 4 ASC;";

		String q5 = "SELECT ATTRIBUTEVALUES.EntityId, AttributeVariable, AttributeValue FROM ATTRIBUTEVALUES "
				+ "INNER JOIN ATTRIBUTEVARIABLES ON ATTRIBUTEVARIABLES.ID = AttributeVariableId "
				+ "INNER JOIN VARIABLES ON VARIABLES.ID = ATTRIBUTEVARIABLES.VariableId "
				+ "INNER JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.VariableId = VARIABLES.ID "
				+ "INNER JOIN ROLES ON ROLES.ID = ROLEVARIABLELINKS.RoleId "
				+ "WHERE ROLES.StatementTypeId = " + statementTypeId + ";";

		ArrayList<Statement> listOfStatements = null;
		int statementId, variableId, roleId, entityId;
		String variableName, roleName;
		Color sColor, cColor;
		HashMap<Integer, Statement> statementMap = new HashMap<Integer, Statement>(); // statement ID to Statement
		ResultSet r4, r5;
		try (Connection conn = Dna.sql.getDataSource().getConnection();
				PreparedStatement s1 = conn.prepareStatement(q1);
				PreparedStatement s4a = conn.prepareStatement(q4a);
				PreparedStatement s4b = conn.prepareStatement(q4b);
				PreparedStatement s4c = conn.prepareStatement(q4c);
				PreparedStatement s4d = conn.prepareStatement(q4d);
				PreparedStatement s5 = conn.prepareStatement(q5);) {

			// assemble statements without values for now and save them in a hash map
			ResultSet r1 = s1.executeQuery();
			while (r1.next()) {
				statementId = r1.getInt("StatementId");
			    statementTypeId = r1.getInt("StatementTypeId");
			    sColor = new Color(r1.getInt("StatementTypeRed"), r1.getInt("StatementTypeGreen"), r1.getInt("StatementTypeBlue"));
			    cColor = new Color(r1.getInt("CoderRed"), r1.getInt("CoderGreen"), r1.getInt("CoderBlue"));
			    Statement statement = new Statement(statementId,
			    		r1.getInt("Start"),
			    		r1.getInt("Stop"),
			    		statementTypeId,
			    		r1.getString("StatementTypeLabel"),
			    		sColor,
			    		r1.getInt("CoderId"),
			    		r1.getString("CoderName"),
			    		cColor,
			    		new ArrayList<Value>(),
			    		r1.getInt("DocumentId"),
			    		r1.getString("Text"),
			    		LocalDateTime.ofEpochSecond(r1.getLong("Date"), 0, ZoneOffset.UTC));
			    statementMap.put(statementId, statement);
			}

			// attributes
			r5 = s5.executeQuery();
			HashMap<Integer, HashMap<String, String>> attributeMap = new HashMap<Integer, HashMap<String, String>>();
			String attributeKey;
			String attributeValue;
			while (r5.next()) {
				entityId = r5.getInt("EntityId");
				attributeKey = r5.getString("AttributeVariable");
				attributeValue = r5.getString("AttributeValue");
				if (!attributeMap.containsKey(entityId)) {
					attributeMap.put(entityId, new HashMap<String, String>());
				}
				attributeMap.get(entityId).put(attributeKey, attributeValue);
			}

			// get values and put them into the statements
			r4 = s4a.executeQuery();
			while (r4.next()) {
				variableId = r4.getInt("VariableId");
				variableName = r4.getString("VariableName");
				roleId = r4.getInt("RoleId");
				roleName = r4.getString("RoleName");
				entityId = r4.getInt("EntityId");
				Entity e = new Entity(entityId,
						variableId,
						r4.getString("Value"),
						new Color(r4.getInt("Red"), r4.getInt("Green"), r4.getInt("Blue")),
						r4.getInt("ChildOf"),
						true,
						attributeMap.get(entityId));
				statementMap.get(r4.getInt("StatementId")).getValues().add(new Value(variableId, variableName, "short text", e, roleId, roleName));
			}
			r4 = s4b.executeQuery();
			while (r4.next()) {
				variableId = r4.getInt("VariableId");
				variableName = r4.getString("VariableName");
				roleId = r4.getInt("RoleId");
				roleName = r4.getString("RoleName");
				String value = r4.getString("Value");
				statementMap.get(r4.getInt("StatementId")).getValues().add(new Value(variableId, variableName, "long text", value, roleId, roleName));
			}
			r4 = s4c.executeQuery();
			while (r4.next()) {
				variableId = r4.getInt("VariableId");
				variableName = r4.getString("VariableName");
				roleId = r4.getInt("RoleId");
				roleName = r4.getString("RoleName");
				int value = r4.getInt("Value");
				statementMap.get(r4.getInt("StatementId")).getValues().add(new Value(variableId, variableName, "boolean", value, roleId, roleName));
			}
			r4 = s4d.executeQuery();
			while (r4.next()) {
				variableId = r4.getInt("VariableId");
				variableName = r4.getString("VariableName");
				roleId = r4.getInt("RoleId");
				roleName = r4.getString("RoleName");
				int value = r4.getInt("Value");
				statementMap.get(r4.getInt("StatementId")).getValues().add(new Value(variableId, variableName, "integer", value, roleId, roleName));
			}

			// assemble and sort all statements
			Collection<Statement> s = statementMap.values();
	        listOfStatements = new ArrayList<Statement>(s);
			Collections.sort(listOfStatements);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to retrieve statements.",
					"Attempted to retrieve a set of " + statementIds.length + " statements from the database, but something went wrong.",
					e);
			Dna.logger.log(l);
		}
		return listOfStatements;
	}
	*/

	/**
	 * Get a shallow representation of all statements in a specific document for
	 * the purpose of painting the statements in the text. For this purpose,
	 * variable contents, date, coder name etc. are unnecessary. This speeds up
	 * the retrieval.
	 *
	 * @param documentId  ID of the document for which statements are retrieved.
	 * @return Array list of statements.
	 */
	public ArrayList<TextStatement> getTextStatements(int documentId) {
		String query = "SELECT S.ID, S.Start, S.Stop, S.StatementTypeId, "
				+ "T.Label AS StatementTypeLabel, T.Red AS StatementTypeRed, "
				+ "T.Green AS StatementTypeGreen, T.Blue AS StatementTypeBlue, "
				+ "S.Coder, C.Red AS CoderRed, C.Green AS CoderGreen, C.Blue AS CoderBlue "
				+ "FROM STATEMENTS S LEFT JOIN CODERS C ON C.ID = S.Coder "
				+ "LEFT JOIN STATEMENTTYPES T ON T.ID = S.StatementTypeId "
				+ "WHERE S.DocumentId = ? ORDER BY Start ASC;";
		ArrayList<TextStatement> textStatements = new ArrayList<TextStatement>();
		try (Connection conn = ds.getConnection();
			 PreparedStatement s = conn.prepareStatement(query)) {
			ResultSet r;
			s.setInt(1, documentId);
			r = s.executeQuery();
			while (r.next()) {
				TextStatement textStatement = new TextStatement(r.getInt("ID"),
						r.getInt("Start"),
						r.getInt("Stop"),
						r.getInt("StatementTypeId"),
						r.getInt("Coder"),
						documentId,
						null,
						new Color(r.getInt("CoderRed"), r.getInt("CoderGreen"), r.getInt("CoderBlue")),
						new Color(r.getInt("StatementTypeRed"), r.getInt("StatementTypeGreen"), r.getInt("StatementTypeBlue")));
				textStatements.add(textStatement);
			}
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] " + textStatements.size() + " statement(s) have been retrieved for Document " + documentId + ".",
					textStatements.size() + " statement(s) have been retrieved for for Document " + documentId + ".");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"[SQL] Failed to retrieve statements for Document " + documentId + ".",
					"Attempted to retrieve all statements for Document " + documentId + " from the database, but something went wrong. You should double-check if the statements are all shown!",
					e);
			Dna.logger.log(l);
		}
		return textStatements;
	}

	/**
	 * Delete statements from the database, given an array of statement IDs.
	 *
	 * @param statementIds  An array of statement IDs to be deleted.
	 * @return              Were the statements successfully deleted?
	 */
	public boolean deleteStatements(int[] statementIds) {
		boolean committed = false;
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("DELETE FROM STATEMENTS WHERE ID = ?");
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			for (int i = 0; i < statementIds.length; i++) {
				s.setInt(1, statementIds[i]);
				s.executeUpdate();
			}
			conn.commit();
			committed = true;
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Deleted " + statementIds.length + " statement(s).",
					"Successfully deleted " + statementIds.length + " statement(s) from the STATEMENTS table in the database. The transaction has been committed to the database.");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to delete statement(s) from database.",
					"Attempted to remove " + statementIds.length + " statement(s) from the STATEMENTS table in the database, but something went wrong. The transaction has been rolled back, and nothing has been removed.",
					e);
			Dna.logger.log(l);
		}
		return committed;
	}

	/**
	 * Count how many statements of a certain statement type exist.
	 *
	 * @param statementTypeId  The ID of the statement type. Can be {@code -1}
	 *   to count all statement types.
	 * @return                 An integer count of the statement frequency.
	 */
	public int countStatements(int statementTypeId) {
		int result = -1;
		String where = "";
		if (statementTypeId != -1) {
			where = " WHERE StatementTypeId = " + statementTypeId;
		}
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("SELECT COUNT(ID) FROM STATEMENTS" + where + ";")) {
			ResultSet r = s.executeQuery();
			while (r.next()) {
				result = r.getInt(1);
			}
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
        			"[SQL] Failed to count statements of Statement Type " + statementTypeId + ".",
        			"Attempted to count with how many statements of Statement Type " + statementTypeId + " exist in the database, but the database operation failed.",
        			e);
        	Dna.logger.log(l);
		}
		return result;
	}


	/* =========================================================================
	 * Entities and attributes
	 * ====================================================================== */

	/**
	 * Add an entity to the ENTITIES table of the database and save the
	 * attribute values contained in the entity into the ATTRIBUTEVALUES table
	 * of the database, in a transaction. Return the ID of the new entity.
	 *
	 * @param entity  An entity.
	 * @return        The ID of the newly saved entity.
	 */
	public int addEntity(Entity entity) {
		int entityId = -1;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("INSERT INTO ENTITIES (VariableId, Value, Red, Green, Blue) VALUES (?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement s2 = conn.prepareStatement("INSERT INTO ATTRIBUTEVALUES (EntityId, AttributeVariableId, AttributeValue) VALUES (?, ?, ?);");
				PreparedStatement s3 = conn.prepareStatement("SELECT ID, AttributeVariable FROM ATTRIBUTEVARIABLES WHERE VariableId = ?;");
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);

			// insert entity into ENTITIES table
			s1.setInt(1, entity.getVariableId());
			s1.setString(2, entity.getValue());
			s1.setInt(3, entity.getColor().getRed());
			s1.setInt(4, entity.getColor().getGreen());
			s1.setInt(5, entity.getColor().getBlue());
			s1.executeUpdate();
			ResultSet generatedKeysResultSet = s1.getGeneratedKeys();
			while (generatedKeysResultSet.next()) {
				entityId = generatedKeysResultSet.getInt(1);
			}

			// get the attribute variable IDs from the ATTRIBUTEVARIABLES table
			s3.setInt(1, entity.getVariableId());
			ResultSet r = s3.executeQuery();
			while (r.next()) {
				// insert attribute values into ATTRIBUTEVALUES table
				s2.setInt(1, entityId);
				s2.setInt(2, r.getInt("ID"));
				s2.setString(3, entity.getAttributeValues().get(r.getString("AttributeVariable")));
				s2.executeUpdate();
			}
        	conn.commit();
			LogEvent e = new LogEvent(Logger.MESSAGE,
					"[SQL] Added Entity " + entityId + " (" + entity.getValue() + ") to database.",
					"Added Entity " + entityId + " (" + entity.getValue() + " ) and successfully saved to the database.");
			Dna.logger.log(e);
		} catch (SQLException e1) {
        	LogEvent e = new LogEvent(Logger.WARNING,
        			"[SQL] Entity with value \"" + entity.getValue() + "\" (variable ID " + entity.getVariableId() + ") could not be added.",
        			"New entity could not be added to the ENTITIES and ATTRIBUTEVALUES tables in the database. Check if the database is still there and/or if the connection has been interrupted, then try again.",
        			e1);
        	Dna.logger.log(e);
		}
		return entityId;
	}

	/**
	 * Retrieve the full set of entities for a set of variable IDs. The result
	 * is an array list with nested array lists of entities for each variable
	 * ID.
	 *
	 * @param variableIds The IDs of the variables for which all entities will
	 *   be retrieved, supplied as an array list of integers.
	 * @param withAttributes Include attributes and indicator of whether the
	 *   entity has been used in the database in each {@link model.Entity
	 *   Entity}? Doing so takes much longer for large databases.
	 * @return An array list of array lists of {@link Entity}
	 *   objects.
	 */
	/*
	public ArrayList<ArrayList<Entity>> getEntities(ArrayList<Integer> variableIds, boolean withAttributes) {
		ArrayList<ArrayList<Entity>> entities = new ArrayList<ArrayList<Entity>>();
		String sqlString = "SELECT ID, Value, Red, Green, Blue FROM ENTITIES WHERE VariableId = ?;";
		if (withAttributes) {
			sqlString = "SELECT ID, Value, Red, Green, Blue, ChildOf, (SELECT COUNT(ID) FROM DATASHORTTEXT WHERE Entity = ENTITIES.ID) AS Count FROM ENTITIES WHERE VariableId = ?;";
		}
		try (Connection conn = ds.getConnection();
			 PreparedStatement s1 = conn.prepareStatement(sqlString);
			 PreparedStatement s2 = conn.prepareStatement("SELECT AttributeVariable, AttributeValue FROM ATTRIBUTEVALUES AS AVAL INNER JOIN ATTRIBUTEVARIABLES AS AVAR ON AVAL.AttributeVariableId = AVAR.ID WHERE EntityId = ?;")) {
			ResultSet r1, r2;
			HashMap<String, String> map;
			ArrayList<Entity> entitiesList;
			for (int i = 0; i < variableIds.size(); i++) {
				entitiesList = new ArrayList<Entity>();
				s1.setInt(1, variableIds.get(i));
				r1 = s1.executeQuery();
				while (r1.next()) {
					if (withAttributes) {
						map = new HashMap<String, String>();
						s2.setInt(1, r1.getInt("ID"));
						r2 = s2.executeQuery();
						while (r2.next()) {
							map.put(r2.getString("AttributeVariable"), r2.getString("AttributeValue"));
						}
						entitiesList.add(
								new Entity(r1.getInt("ID"),
										variableIds.get(i),
										r1.getString("Value"),
										new Color(r1.getInt("Red"), r1.getInt("Green"), r1.getInt("Blue")),
										r1.getInt("ChildOf"),
										r1.getInt("Count") > 0,
										map));
					} else {
						entitiesList.add(
								new Entity(r1.getInt("ID"),
										variableIds.get(i),
										r1.getString("Value"),
										new Color(r1.getInt("Red"), r1.getInt("Green"), r1.getInt("Blue"))));
					}
				}
				entities.add(entitiesList);
			}
			LogEvent e = new LogEvent(Logger.MESSAGE,
					"[SQL] Retrieved entities for " + variableIds.size() + " variables.",
					"Retrieved entities for " + variableIds.size() + " variables.");
			Dna.logger.log(e);
		} catch (SQLException e1) {
			LogEvent e = new LogEvent(Logger.WARNING,
					"[SQL] Entities could not be retrieved.",
					"Entities for " + variableIds.size() + " could not be retrieved. Check if the database is still there and/or if the connection has been interrupted, then try again.",
					e1);
			Dna.logger.log(e);
		}
		return entities;
	}
	*/

	/**
	 * Delete all entities corresponding to certain entity IDs. Check if the
	 * entities can be deleted safely and log a warning instead of deleting the
	 * entities if deleting them would also delete existing statements.
	 *
	 * @param entityIds  An int array of entity IDs to be deleted.
	 */
	public void deleteEntities(int[] entityIds) {
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("DELETE FROM ENTITIES WHERE ID = ?;");
				PreparedStatement s2 = conn.prepareStatement("SELECT COUNT(ID) FROM DATASHORTTEXT WHERE Entity = ?;");
				SQLCloseable finish = conn::rollback) {
			ResultSet r;
			int i;
			int numDeletedStatements = 0;
			for (i = 0; i < entityIds.length; i++) {
				s2.setInt(1, entityIds[i]);
				r = s2.executeQuery();
				while (r.next()) {
					numDeletedStatements = numDeletedStatements + r.getInt(1);
				}
			}
			if (numDeletedStatements > 0) {
				LogEvent e = new LogEvent(Logger.WARNING,
						"[SQL] Could not delete row(s) in the attribute manager.",
						"Attempted to delete " + entityIds.length + " rows in the ENTITIES table in the database. But this would also lead to the deletion of " + numDeletedStatements + " statements. The entries were not deleted to prevent accidental damage. Please choose the rows to delete more carefully.");
				Dna.logger.log(e);
			} else {
				conn.setAutoCommit(false);
				for (i = 0; i < entityIds.length; i++) {
					s1.setInt(1, entityIds[i]);
					s1.executeUpdate();
				}
	        	conn.commit();
				LogEvent e = new LogEvent(Logger.MESSAGE,
						"[SQL] Deleted " + entityIds.length + " row(s) from ENTITIES table in the database.",
						"Successfully deleted " + entityIds.length + " unused entities from the database without affecting any statements.");
				Dna.logger.log(e);
			}
		} catch (SQLException e1) {
        	LogEvent e = new LogEvent(Logger.WARNING,
        			"[SQL] Entities could not be deleted.",
        			"Tried to delete " + entityIds.length + " row(s) from the ENTITIES table in the database. These are variable values that are not in use in any statement. Since the transaction failed, the database commit has been rolled back, and the entities are still in the database.",
        			e1);
        	Dna.logger.log(e);
		}
	}

	/**
	 * Update/set the value of an entity.
	 *
	 * @param entityId       ID of the entity.
	 * @param newValue       The new value to be set in the entity.
	 * @throws SQLException
	 */
	public void setEntityValue(int entityId, String newValue) throws SQLException {
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("UPDATE ENTITIES SET Value = ? WHERE ID = ?;")) {
        	s.setString(1, newValue);
        	s.setInt(2,  entityId);
        	s.executeUpdate();
		} catch (SQLException ex) {
			throw ex;
		}
	}

	/**
	 * Update/set the color of an entity.
	 *
	 * @param entityId       ID of the entity.
	 * @param newColor       The new color to be set in the entity.
	 * @throws SQLException
	 */
	public void setEntityColor(int entityId, Color newColor) throws SQLException {
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("UPDATE ENTITIES SET Red = ?, Green = ?, Blue = ? WHERE ID = ?;")) {
        	s.setInt(1, newColor.getRed());
        	s.setInt(2, newColor.getGreen());
        	s.setInt(3, newColor.getBlue());
        	s.setInt(4, entityId);
        	s.executeUpdate();
		} catch (SQLException ex) {
			throw ex;
		}
	}

	/**
	 * Retrieve unique values for a specific variable.
	 *
	 * @param statementTypeId  Statement type ID to which the variable belongs.
	 * @param variable         The name of the variable.
	 * @return                 Array list of unique String values.
	 */
	/*
	public ArrayList<String> getUniqueValues(int statementTypeId, String variable) {
		ArrayList<String> values = new ArrayList<String>();
		try (Connection conn = ds.getConnection();
				//PreparedStatement s1 = conn.prepareStatement("SELECT DISTINCT Value FROM DATAINTEGER INNER JOIN VARIABLES ON VARIABLES.ID = DATAINTEGER.VariableId WHERE VARIABLES.Variable = ? AND VARIABLES.StatementTypeId = ?;");
			 	PreparedStatement s1 = conn.prepareStatement("SELECT DISTINCT Value FROM DATAINTEGER INNER JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.ID = DATAINTEGER.RoleVariableLinkId WHERE ROLEVARIABLELINKS.VariableId = ?;");
				PreparedStatement s2 = conn.prepareStatement("SELECT DISTINCT Value FROM DATABOOLEAN INNER JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.ID = DATABOOLEAN.RoleVariableLinkId WHERE ROLEVARIABLELINKS.VariableId = ?;");
				PreparedStatement s3 = conn.prepareStatement("SELECT DISTINCT Value FROM DATALONGTEXT INNER JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.ID = DATALONGTEXT.RoleVariableLinkId WHERE ROLEVARIABLELINKS.VariableId = ?;");
			 	PreparedStatement s4 = conn.prepareStatement("SELECT DISTINCT Value FROM ENTITIES INNER JOIN DATASHORTTEXT ON DATASHORTTEXT.Entity = ENTITIES.ID INNER JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.ID = DATASHORTTEXT.RoleVariableLinkId WHERE ROLEVARIABLELINKS.VariableId = ?;");
				PreparedStatement s5 = conn.prepareStatement("SELECT DataType FROM VARIABLES WHERE Variable = ?;")) { // TODO: input used to be a variable label, not ID; merge with VARIABLES table each time to get it? Or do we actually need this for roles, not variables?
			ResultSet r1, r2;
			s5.setString(1, variable);
			s5.setInt(2, statementTypeId);
			r1 = s5.executeQuery();
			while (r1.next()) {
				String dataType = r1.getString("DataType");
				if (dataType.equals("integer")) {
					s1.setString(1, variable);
					s1.setInt(2, statementTypeId);
					r2 = s1.executeQuery();
					while (r2.next()) {
						values.add(String.valueOf(r2.getInt("Value")));
					}
				} else if (dataType.equals("boolean")) {
					s2.setString(1, variable);
					s2.setInt(2, statementTypeId);
					r2 = s2.executeQuery();
					while (r2.next()) {
						values.add(String.valueOf(r2.getInt("Value")));
					}
				} else if (dataType.equals("long text")) {
					s3.setString(1, variable);
					s3.setInt(2, statementTypeId);
					r2 = s3.executeQuery();
					while (r2.next()) {
						values.add(r2.getString("Value"));
					}
				} else {
					s4.setString(1, variable);
					s4.setInt(2, statementTypeId);
					r2 = s4.executeQuery();
					while (r2.next()) {
						values.add(r2.getString("Value"));
					}
				}
			}
		} catch (SQLException e1) {
        	LogEvent e = new LogEvent(Logger.WARNING,
        			"[SQL] Values could not be retrieved.",
        			"The unique values for variable \"" + variable + "\" (statement type ID " + statementTypeId + ") could not be retrieved from the database.",
        			e1);
        	Dna.logger.log(e);
		}
		Collections.sort(values);
		return values;
	}
	*/

	/**
	 * Update/set an attribute value for an entity.
	 *
	 * @param entityId          ID of the entity.
	 * @param variableId        The variable ID to which the entity belongs.
	 * @param attributeVariable The name of the attribute variable to update.
	 * @param newValue          The new attribute value.
	 * @throws SQLException
	 */
	public void setAttributeValue(int entityId, int variableId, String attributeVariable, String newValue) throws SQLException {
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("UPDATE ATTRIBUTEVALUES SET AttributeValue = ? WHERE (EntityId = ? AND AttributeVariableId = (SELECT ID FROM ATTRIBUTEVARIABLES WHERE VariableId = ? AND AttributeVariable = ?));")) {
        	s.setString(1, newValue);
        	s.setInt(2,  entityId);
        	s.setInt(3, variableId);
        	s.setString(4, attributeVariable);
        	s.executeUpdate();
		} catch (SQLException ex) {
			throw ex;
		}
	}

	/**
	 * Retrieve a list of attribute variable names for a given variable from the
	 * database.
	 *
	 * @param variableId  ID of the variable.
	 * @return            ArrayList of attribute variable names.
	 */
	public ArrayList<String> getAttributeVariables(int variableId) {
		ArrayList<String> attributeVariables = new ArrayList<String>();
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT AttributeVariable FROM ATTRIBUTEVARIABLES WHERE VariableId = ?;")) {
			s1.setInt(1, variableId);
			ResultSet r1 = s1.executeQuery();
			while (r1.next()) {
				attributeVariables.add(r1.getString("AttributeVariable"));
			}
		} catch (SQLException e1) {
        	LogEvent e = new LogEvent(Logger.WARNING,
        			"[SQL] Attribute variables for Variable " + variableId + " could not be retrieved.",
        			"Attribute variable names for Variable " + variableId + " could not be retrieved. Check if the database is still there and/or if the connection has been interrupted, then try again.",
        			e1);
        	Dna.logger.log(e);
		}
		return attributeVariables;
	}

	/**
	 * Add a new attribute variable to a variable.
	 *
	 * @param variableId         The variable ID.
	 * @param attributeVariable  The attribute variable name.
	 */
	public void addAttributeVariable(int variableId, String attributeVariable) {
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement s2 = conn.prepareStatement("INSERT INTO ATTRIBUTEVALUES (EntityId, AttributeVariableId, AttributeValue) VALUES (?, ?, '');");
				PreparedStatement s3 = conn.prepareStatement("SELECT DISTINCT Entity FROM DATASHORTTEXT INNER JOIN ROLEVARIABLELINKS ON DATASHORTTEXT.RoleVariableLinkId = ROLEVARIABLELINKS.ID WHERE ROLEVARIABLELINKS.VariableId = ?;");
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			ResultSet r;
			s1.setInt(1, variableId);
			s1.setString(2, attributeVariable);
			s1.executeUpdate();
			ResultSet generatedKeysResultSet = s1.getGeneratedKeys();
			int attributeVariableId = -1;
			while (generatedKeysResultSet.next()) {
				attributeVariableId = generatedKeysResultSet.getInt(1);
			}
			s3.setInt(1, variableId);
			r = s3.executeQuery();
			while (r.next()) {
				s2.setInt(1, r.getInt("Entity"));
				s2.setInt(2, attributeVariableId);
				s2.executeUpdate();
			}
        	conn.commit();
		} catch (SQLException e1) {
        	LogEvent e = new LogEvent(Logger.WARNING,
        			"[SQL] Attribute could not be added to Variable " + variableId + ".",
        			"Attribute variable \"" + attributeVariable + "\" could not be added to Variable " + variableId + ". Check if the database is still there and/or if the connection has been interrupted, then try again.",
        			e1);
        	Dna.logger.log(e);
		}
	}

	/**
	 * Delete an attribute variable.
	 *
	 * @param variableId         The variable ID.
	 * @param attributeVariable  The attribute variable name.
	 */
	public void deleteAttributeVariable(int variableId, String attributeVariable) {
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("DELETE FROM ATTRIBUTEVARIABLES WHERE (VariableId = ? AND AttributeVariable = ?);")) {
			s1.setInt(1, variableId);
			s1.setString(2, attributeVariable);
			s1.executeUpdate();
		} catch (SQLException e1) {
        	LogEvent e = new LogEvent(Logger.WARNING,
        			"[SQL] Attribute could not be deleted from Variable " + variableId + ".",
        			"Attribute variable \"" + attributeVariable + "\" could not be deleted from Variable " + variableId + ". Check if the database is still there and/or if the connection has been interrupted, then try again.",
        			e1);
        	Dna.logger.log(e);
		}
	}

	/**
	 * Rename an attribute variable.
	 *
	 * @param variableId                The variable ID.
	 * @param oldAttributeVariableName  The attribute variable name to rename.
	 * @param newAttributeVariableName  The new attribute variable name.
	 */
	public boolean updateAttributeVariableName(int variableId, String oldAttributeVariableName, String newAttributeVariableName) {
		boolean success = false;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("UPDATE ATTRIBUTEVARIABLES SET AttributeVariable = ? WHERE VariableId = ? AND AttributeVariable = ?;")) {
			s1.setString(1, newAttributeVariableName);
			s1.setInt(2, variableId);
			s1.setString(3, oldAttributeVariableName);
			s1.executeUpdate();
			success = true;
		} catch (SQLException e1) {
        	LogEvent e = new LogEvent(Logger.WARNING,
        			"[SQL] Attribute could not be renamed.",
        			"Attribute variable \"" + oldAttributeVariableName + "\" for Variable " + variableId + " could not be renamed. Check if the database is still there and/or if the connection has been interrupted, then try again.",
        			e1);
        	Dna.logger.log(e);
		}
		return success;
	}

	/* =========================================================================
	 * Statement types
	 * ====================================================================== */

	/**
	 * Get a statement type from the database. The variable definitions are saved as an array list of
	 * {@link model.Value Value} objects containing the variable ID, variable name, and data type.
	 *
	 * @return A {@link model.StatementType StatementType} object.
	 */
	/*
	public StatementType getStatementType(int statementTypeId) {
		StatementType st = null;
		String statementTypeLabel = "";
		try (Connection conn = ds.getConnection();
			 PreparedStatement s1 = conn.prepareStatement("SELECT * FROM STATEMENTTYPES WHERE ID = ?;");
			 PreparedStatement s2 = conn.prepareStatement("SELECT * FROM VARIABLES WHERE StatementTypeId = ?;")) {
			ArrayList<Value> variables;
			s1.setInt(1, statementTypeId);
			ResultSet r1 = s1.executeQuery();
			ResultSet r2;
			Color color;
			while (r1.next()) {
				variables = new ArrayList<Value>();
				statementTypeLabel = r1.getString("Label");
				color = new Color(r1.getInt("Red"), r1.getInt("Green"), r1.getInt("Blue"));
				s2.setInt(1, statementTypeId);
				r2 = s2.executeQuery();
				while (r2.next()) {
					variables.add(new Value(r2.getInt("ID"), r2.getString("Variable"), r2.getString("DataType")));
				}
				st = new StatementType(statementTypeId, statementTypeLabel, color, variables);
			}
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Retrieved statement type '" + statementTypeLabel + "' (ID: " + statementTypeId + ") from the database.",
					"Retrieved statement type '" + statementTypeLabel + "' (ID: " + statementTypeId + ") from the database.");
			Dna.logger.log(l);
		} catch (SQLException e1) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to retrieve statement type from the database.",
					"Failed to retrieve statement type '" + statementTypeLabel + "' from the database. Check database connection and consistency of the STATEMENTTYPES and VARIABLES tables in the database.",
					e1);
			Dna.logger.log(l);
		}
		return st;
	}
	*/

	/**
	 * Get a statement type from the database. The variable definitions are saved as an array list of
	 * {@link model.Value Value} objects containing the variable ID, variable name, and data type.
	 *
	 * @return A {@link model.StatementType StatementType} object.
	 */
	/*
	public StatementType getStatementType(String statementTypeLabel) {
		StatementType st = null;
		try (Connection conn = ds.getConnection();
			 PreparedStatement s1 = conn.prepareStatement("SELECT * FROM STATEMENTTYPES WHERE Label = ?;");
			 PreparedStatement s2 = conn.prepareStatement("SELECT * FROM VARIABLES WHERE StatementTypeId = ?;")) {
			ArrayList<Value> variables;
			int statementTypeId = -1;
			s1.setString(1, statementTypeLabel);
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
				st = new StatementType(r1.getInt("ID"), r1.getString("Label"), color, variables);
			}
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Retrieved statement type '" + statementTypeLabel + "' (ID: " + statementTypeId + ") from the database.",
					"Retrieved statement type '" + statementTypeLabel + "' (ID: " + statementTypeId + ") from the database.");
			Dna.logger.log(l);
		} catch (SQLException e1) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to retrieve statement type from the database.",
					"Failed to retrieve statement type '" + statementTypeLabel + "' from the database. Check database connection and consistency of the STATEMENTTYPES and VARIABLES tables in the database.",
					e1);
			Dna.logger.log(l);
		}
		return st;
	}
	*/

	/*
	public StatementType getStatementType(String statementTypeLabel) {
		StatementType st = null;
		try (Connection conn = ds.getConnection();
			 PreparedStatement s1 = conn.prepareStatement("SELECT * FROM STATEMENTTYPES WHERE Label = ?;");
			 PreparedStatement s2 = conn.prepareStatement("SELECT * FROM VARIABLES WHERE StatementTypeId = ?;")) {
			ArrayList<Value> variables;
			int statementTypeId = -1;
			s1.setString(1, statementTypeLabel);
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
				st = new StatementType(r1.getInt("ID"), r1.getString("Label"), color, variables);
			}
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Retrieved statement type '" + statementTypeLabel + "' (ID: " + statementTypeId + ") from the database.",
					"Retrieved statement type '" + statementTypeLabel + "' (ID: " + statementTypeId + ") from the database.");
			Dna.logger.log(l);
		} catch (SQLException e1) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to retrieve statement type from the database.",
					"Failed to retrieve statement type '" + statementTypeLabel + "' from the database. Check database connection and consistency of the STATEMENTTYPES and VARIABLES tables in the database.",
					e1);
			Dna.logger.log(l);
		}
		return st;
	}
	*/

	/**
	 * Get an array list of all statement types in the database.
	 *
	 * @return An ArrayList of {@link model.StatementType StatementType} objects.
	 */
	public ArrayList<StatementType> getStatementTypes() {
		ArrayList<StatementType> statementTypes = new ArrayList<StatementType>();
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("SELECT * FROM STATEMENTTYPES;")) {
        	int statementTypeId;
			ResultSet r1 = s1.executeQuery();
			ResultSet r2;
			Color color;
        	while (r1.next()) {
				statementTypes.add(new StatementType(r1.getInt("ID"), r1.getString("Label"), new Color(r1.getInt("Red"), r1.getInt("Green"), r1.getInt("Blue"))));
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

	/**
	 * Add a statement type (without any variables) to the database.
	 *
	 * @param label  Label, or name, of the statement type.
	 * @param color  Color of the statement type.
	 * @return       The ID of the new statement type.
	 */
	public int addStatementType(String label, Color color) {
		int statementTypeId = -1;
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("INSERT INTO STATEMENTTYPES (Label, Red, Green, Blue) VALUES (?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS)) {
        	s.setString(1, label);
        	s.setInt(2, color.getRed());
        	s.setInt(3, color.getGreen());
        	s.setInt(4, color.getBlue());
        	s.executeUpdate();
        	ResultSet generatedKeysResultSet = s.getGeneratedKeys();
			while (generatedKeysResultSet.next()) {
				statementTypeId = generatedKeysResultSet.getInt(1);
			}
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Statement type added to the database.",
					"Added new statement type (ID " + statementTypeId + ") to the STATEMENTTYPES table in the database.");
			Dna.logger.log(l);
		} catch (SQLException e1) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to add new statement type to the database.",
					"Tried to add a new statement type (ID " + statementTypeId + ") to the STATEMENTTYPES table in the database, but something went wrong. Check the database connection and the message log.",
					e1);
			Dna.logger.log(l);
		}
		return statementTypeId;
	}

	/**
	 * Delete a statement type from the database. Note that this will also
	 * delete all statements and entities/attributes corresponding to this
	 * statement type.
	 *
	 * @param statementTypeId The ID of the statement type to be deleted.
	 * @return                Was the deletion successful?
	 */
	public boolean deleteStatementType(int statementTypeId) {
		boolean success = false;
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("DELETE FROM STATEMENTTYPES WHERE ID = ?;")) {
			s.setInt(1, statementTypeId);
			s.executeUpdate();
			success = true;
			LogEvent l = new LogEvent(Logger.MESSAGE,
        			"[SQL] Successfully deleted Statement Type " + statementTypeId + " from the database.",
        			"Successfully deleted Statement Type " + statementTypeId + " from the database.");
        	Dna.logger.log(l);
		} catch (SQLException e) {
			success = false;
        	LogEvent l = new LogEvent(Logger.ERROR,
        			"[SQL] Failed to delete Statement Type " + statementTypeId + " from the database.",
        			"Attempted to delete Statement Type " + statementTypeId + ", but the database operation failed.",
        			e);
        	Dna.logger.log(l);
		}
		return success;
	}

	/**
	 * Update the label and color of a statement type in the STATEMENTTYPES
	 * table.
	 *
	 * @param statementTypeId  ID of the statement type to update.
	 * @param label            The new label or name of the statement type.
	 * @param color            The new color of the statement type.
	 * @return                 Was the update successful?
	 */
	public boolean updateStatementType(int statementTypeId, String label, Color color) {
		boolean success = false;
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("UPDATE STATEMENTTYPES SET Label = ?, Red = ?, Green = ?, Blue = ? WHERE ID = ?;")) {
			s.setString(1, label);
			s.setInt(2, color.getRed());
			s.setInt(3, color.getGreen());
			s.setInt(4, color.getBlue());
			s.setInt(5, statementTypeId);
			s.executeUpdate();
			success = true;
			LogEvent e = new LogEvent(Logger.MESSAGE,
        			"[SQL] Statement type " + statementTypeId + " was updated.",
        			"Statement type " + statementTypeId + " was successfully updated in the STATEMENTTYPES table in the database.");
        	Dna.logger.log(e);
		} catch (SQLException e1) {
        	LogEvent e = new LogEvent(Logger.ERROR,
        			"[SQL] Statement type " + statementTypeId + " could not be updated.",
        			"Tried to update statement type " + statementTypeId + " in the STATEMENTTYPES table in the database, but the update was unsuccessful.",
        			e1);
        	Dna.logger.log(e);
		}
		return success;
	}

	/**
	 * Add a variable to the VARIABLES table.
	 *
	 * @param statementTypeId ID of the statement type.
	 * @param variableName    Name of the new variable.
	 * @param dataType        The data type of the variable. Must be one of the
	 *   following: {@code "short text"}, {@code "long text"},
	 *   {@code "integer"}, or {@code "boolean"}.
	 * @return                The ID of the new variable.
	 */
	public int addVariable(int statementTypeId, String variableName, String dataType) {
		int variableId = -1;
		try (Connection conn = ds.getConnection();
				PreparedStatement s1 = conn.prepareStatement("INSERT INTO VARIABLES (Variable, DataType, StatementTypeId) VALUES (?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement s2 = conn.prepareStatement("INSERT INTO ENTITIES (VariableId, Value, Red, Green, Blue) VALUES (?, '', 0, 0, 0);", PreparedStatement.RETURN_GENERATED_KEYS);
				PreparedStatement s3 = conn.prepareStatement("SELECT ID FROM STATEMENTS WHERE StatementTypeId = ?;");
				PreparedStatement s4 = conn.prepareStatement("INSERT INTO DATASHORTTEXT (StatementId, VariableId, Entity) VALUES (?, ?, ?);");
				PreparedStatement s5 = conn.prepareStatement("INSERT INTO DATALONGTEXT (StatementId, VariableId, Value) VALUES (?, ?, '');");
				PreparedStatement s6 = conn.prepareStatement("INSERT INTO DATABOOLEAN (StatementId, VariableId, Value) VALUES (?, ?, 1);");
				PreparedStatement s7 = conn.prepareStatement("INSERT INTO DATAINTEGER (StatementId, VariableId, Value) VALUES (?, ?, 0);");
				SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);

			// add variable to VARIABLES table
        	s1.setString(1, variableName);
        	s1.setString(2, dataType);
        	s1.setInt(3, statementTypeId);
        	s1.executeUpdate();
        	ResultSet generatedKeysResultSet = s1.getGeneratedKeys();
			while (generatedKeysResultSet.next()) {
				variableId = generatedKeysResultSet.getInt(1);
			}

			// create an entity in the ENTITIES table if short text
			int entityId = -1;
			if (dataType.equals("short text")) {
				s2.setInt(1, variableId);
				s2.executeUpdate();
				generatedKeysResultSet = s2.getGeneratedKeys();
				while (generatedKeysResultSet.next()) {
					entityId = generatedKeysResultSet.getInt(1);
				}
			}

			// get statement IDs to update
			s3.setInt(1, statementTypeId);
			ResultSet r3 = s3.executeQuery();
			int statementId = -1;
			while (r3.next()) {
				statementId = r3.getInt("ID");
				if (dataType.equals("short text")) { // update short text statements
					s4.setInt(1, statementId);
					s4.setInt(2, variableId);
					s4.setInt(3, entityId);
					s4.executeUpdate();
				} else if (dataType.equals("long text")) { // update long text statements
					s5.setInt(1, statementId);
					s5.setInt(2, variableId);
					s5.executeUpdate();
				} else if (dataType.equals("boolean")) { // update boolean statements
					s6.setInt(1, statementId);
					s6.setInt(2, variableId);
					s6.executeUpdate();
				} else if (dataType.equals("integer")) { // update integer statements
					s7.setInt(1, statementId);
					s7.setInt(2, variableId);
					s7.executeUpdate();
				}
			}

			conn.commit();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Variable added to the database.",
					"Added new variable \"" + variableName + "\" (ID " + variableId + ") to statement type " + statementTypeId + ".");
			Dna.logger.log(l);
		} catch (SQLException e1) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL] Failed to add new variable to the database.",
					"Tried to add a new variable to Statement Type " + statementTypeId + ", but something went wrong. Check the database connection and the message log.",
					e1);
			Dna.logger.log(l);
		}
		return variableId;
	}

	/**
	 * Delete a variable from the database. Note that this will also delete all
	 * corresponding entities and their attributes if applicable.
	 *
	 * @param variableId The ID of the statement type to be deleted.
	 * @return           Was the deletion successful?
	 */
	public boolean deleteVariable(int variableId) {
		boolean success = false;
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("DELETE FROM VARIABLES WHERE ID = ?;")) {
			s.setInt(1, variableId);
			s.executeUpdate();
			success = true;
			LogEvent l = new LogEvent(Logger.MESSAGE,
        			"[SQL] Successfully deleted Variable " + variableId + " from the database.",
        			"Successfully deleted Variable " + variableId + " from the database.");
        	Dna.logger.log(l);
		} catch (SQLException e) {
			success = false;
        	LogEvent l = new LogEvent(Logger.ERROR,
        			"[SQL] Failed to delete Variable " + variableId + " from the database.",
        			"Attempted to delete Variable " + variableId + ", but the database operation failed.",
        			e);
        	Dna.logger.log(l);
		}
		return success;
	}

	/**
	 * Update the name of a variable in the VARIABLES table.
	 *
	 * @param variableId  ID of the variable to update.
	 * @param name        The new name of the variable.
	 * @return            Was the update successful?
	 */
	public boolean updateVariableName(int variableId, String name) {
		boolean success = false;
		try (Connection conn = ds.getConnection();
				PreparedStatement s = conn.prepareStatement("UPDATE VARIABLES SET Variable = ? WHERE ID = ?;")) {
			s.setString(1, name);
			s.setInt(2, variableId);
			s.executeUpdate();
			success = true;
			LogEvent e = new LogEvent(Logger.MESSAGE,
        			"[SQL] Name of variable " + variableId + " was updated.",
        			"The name of variable " + variableId + " was successfully updated in the VARIABLES table in the database.");
        	Dna.logger.log(e);
		} catch (SQLException e1) {
        	LogEvent e = new LogEvent(Logger.ERROR,
        			"[SQL] Name of variable " + variableId + " could not be updated.",
        			"Tried to update the name of variable " + variableId + " in the VARIABLES table in the database, but the update was unsuccessful.",
        			e1);
        	Dna.logger.log(e);
		}
		return success;
	}

	/**
	 * Add a regex to the REGEXES table in the database.
	 *
	 * @param label  The regex pattern.
	 * @param red    The red component of the RGB color (0-255).
	 * @param green  The green component of the RGB color (0-255).
	 * @param blue   The blue component of the RGB color (0-255).
	 * @return       Was the regex successfully added to the database?
	 */
	public boolean addRegex(String label, int red, int green, int blue) {
		boolean added = false;
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("INSERT INTO REGEXES (Label, Red, Green, Blue) VALUES (?, ?, ?, ?);")) {
			s.setString(1, label);
			s.setInt(2, red);
			s.setInt(3, green);
			s.setInt(4, blue);
			s.executeUpdate();
			added = true;
		} catch (SQLException e) {
			added = false;
			LogEvent le = new LogEvent(Logger.ERROR,
					"[SQL] Could not add regex \"" + label + "\" to the database.",
					"Tried to add regex \"" + label + "\" to the database, but there was a problem.",
					e);
			Dna.logger.log(le);
		}
		return added;
	}

	/**
	 * Get all regexes from the REGEXES table in the database.
	 *
	 * @return An array list of all regex terms.
	 */
	public ArrayList<Regex> getRegexes() {
		ArrayList<Regex> regexList = new ArrayList<Regex>();
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("SELECT * FROM REGEXES;")) {
			ResultSet r = s.executeQuery();
			while (r.next()) {
				regexList.add(new Regex(r.getString("Label"), new Color(r.getInt("Red"), r.getInt("Green"), r.getInt("Blue"))));
			}
		} catch (SQLException e) {
			LogEvent le = new LogEvent(Logger.ERROR,
					"[SQL] Could not retrieve regex entries from the database.",
					"Tried to load regular expressions for highlighting in the text from the database, but there was a problem.",
					e);
			Dna.logger.log(le);
		}
		return regexList;
	}

	/**
	 * Remove a regex from the REGEXES table in the database.
	 *
	 * @param label  The regex pattern to be deleted.
	 * @return       Was the regex successfully removed from the database?
	 */
	public boolean deleteRegex(String label) {
		boolean deleted = false;
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("DELETE FROM REGEXES WHERE Label = ?;")) {
			s.setString(1, label);
			s.executeUpdate();
			deleted = true;
		} catch (SQLException e) {
			deleted = false;
			LogEvent le = new LogEvent(Logger.ERROR,
					"[SQL] Could not delete regex \"" + label + "\" from the database.",
					"Tried to delete regex \"" + label + "\" from the database, but there was a problem.",
					e);
			Dna.logger.log(le);
		}
		return deleted;
	}

	/**
	 * Query the database for the version saved in the SETTINGS table.
	 *
	 * @return The DNA version the database was created with.
	 */
	public String getVersion() {
		String version = "";
		try (Connection conn = getDataSource().getConnection();
				PreparedStatement s = conn.prepareStatement("SELECT Value FROM SETTINGS WHERE Property = 'version';")) {
			ResultSet r = s.executeQuery();
			while (r.next()) {
				version = r.getString("Value");
			}
		} catch (SQLException e) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"[SQL] Could not retrieve DNA version from database.",
					"Tried to read the DNA with version the database was created from the database, but the version could not be read from the database.",
					e);
			Dna.logger.log(le);
		}
		return version;
	}






	// NEW FUNCTIONS FOR DNA 3.1

	public ArrayList<Variable> getVariables() {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		try (Connection conn = getDataSource().getConnection();
			 PreparedStatement s = conn.prepareStatement("SELECT * FROM VARIABLES;")) {
			ResultSet r = s.executeQuery();
			while (r.next()) {
				variables.add(new Variable(r.getInt("ID"), r.getString("Variable"), r.getString("DataType")));
			}
		} catch (SQLException e) {
			LogEvent le = new LogEvent(Logger.ERROR,
					"[SQL] Could not retrieve variables from database.",
					"Tried to load variables from the database, but there was a problem retrieving the variables.",
					e);
			Dna.logger.log(le);
		}
		return variables;
	}

	public ArrayList<Role> getRoles() {
		ArrayList<Role> roles = new ArrayList<Role>();
		try (Connection conn = getDataSource().getConnection();
			 PreparedStatement s = conn.prepareStatement("SELECT * FROM ROLES;")) {
			ResultSet r = s.executeQuery();
			while (r.next()) {
				roles.add(new Role(r.getInt("ID"),
						r.getString("RoleName"),
						new Color(r.getInt("Red"), r.getInt("Green"), r.getInt("Blue")),
						r.getInt("StatementTypeId"),
						r.getInt("Position"),
						r.getInt("NumMin"),
						r.getInt("NumMax"),
						r.getInt("NumDefault"),
						r.getInt("DefaultVariableId")));
			}
		} catch (SQLException e) {
			LogEvent le = new LogEvent(Logger.ERROR,
					"[SQL] Could not retrieve roles from database.",
					"Tried to load roles from the database, but there was a problem retrieving the roles.",
					e);
			Dna.logger.log(le);
		}
		return roles;
	}

	/*
	public ArrayList<Variable> getValues(int statementId) {
		ArrayList<Variable> values = new ArrayList<Variable>();
		try (Connection conn = getDataSource().getConnection();
			 PreparedStatement s1 = conn.prepareStatement("SELECT VARIABLES.ID, VARIABLES.Variable, VARIABLES.DataType FROM STATEMENTS LEFT JOIN ROLES ON ROLES.StatementTypeId = STATEMENTS.StatementTypeId LEFT JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.RoleId = ROLES.ID LEFT JOIN VARIABLES ON VARIABLES.ID = ROLEVARIABLELINKS.VariableId WHERE STATEMENTS.ID = ?;");
			 PreparedStatement s2 = conn.prepareStatement("SELECT ENTITIES.ID, Value, ENTITIES.Red, ENTITIES.Green, ENTITIES.Blue, ENTITIES.ChildOf FROM ENTITIES LEFT JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.VariableId = ENTITIES.VariableId LEFT JOIN ROLES ON ROLES.ID = ROLEVARIABLELINKS.RoleId LEFT JOIN STATEMENTS ON STATEMENTS.StatementTypeId = ROLES.StatementTypeId WHERE STATEMENTS.ID = ? AND ENTITIES.VariableId = ?;")) {
			 PreparedStatement s3 = conn.prepareStatement("SELECT DATALONGTEXT.Value LEFT JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.ID = DATALONGTEXT.RoleVariableLinkId WHERE DATALONGTEXT.StatementId = ? AND ROLEVARIABLELINKS.VariableId = ?";);
			 PreparedStatement s4 = conn.prepareStatement("SELECT DATAINTEGER.Value LEFT JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.ID = DATAINTEGER.RoleVariableLinkId WHERE DATAINTEGER.StatementId = ? AND ROLEVARIABLELINKS.VariableId = ?";);
			 PreparedStatement s5 = conn.prepareStatement("SELECT DATABOOLEAN.Value LEFT JOIN ROLEVARIABLELINKS ON ROLEVARIABLELINKS.ID = DATABOOLEAN.RoleVariableLinkId WHERE DATABOOLEAN.StatementId = ? AND ROLEVARIABLELINKS.VariableId = ?";);
			ResultSet r2;
			s1.setInt(1, statementId);
			ResultSet r1 = s1.executeQuery();
			String dataType, variableName;
			int variableId;
			Entity e;
			while (r1.next()) {
				dataType = r1.getString("DataType");
				variableId = r1.getInt("ID");
				variableName = r1.getString("Variable");
				if (dataType.equals("short text")) {
					s2.setInt(1, statementId);
					s2.setInt(2, variableId);
					r2 = s2.executeQuery();
					while (r2.next()) {
						e = new Entity(r2.getInt("ID"), variableId, variableName, new Color(r2.getInt("Red"), r2.getInt("Green"), r2.getInt("Blue")));
						e.setChildOf(r2.getInt("ChildOf"));
						values.add(new Variable(variableId, variableName, dataType, e));
					}
				} else {
					if (dataType.equals("long text")) {
						s3.setInt(1, statementId);
						s3.setInt(2, variableId);
						r2 = s3.executeQuery();
					} else if (dataType.equals("integer")) {
						s4.setInt(1, statementId);
						s4.setInt(2, variableId);
						r2 = s4.executeQuery();
					} else if (dataType.equals("boolean")) {
						s5.setInt(1, statementId);
						s5.setInt(2, variableId);
						r2 = s5.executeQuery();
					}
					while (r2.next()) {
						values.add(new Variable(variableId, variableName, dataType, r2.getString("Value")));
					}
				}
			}
		} catch (SQLException e) {
			LogEvent le = new LogEvent(Logger.ERROR,
					"[SQL] Could not retrieve variables from database.",
					"Tried to load variables from the database, but there was a problem retrieving the variables.",
					e);
			Dna.logger.log(le);
		}
		return values;
	}
	*/

	public int addNewStatement(Statement statement) {
		int statementId = -1;
		try (Connection conn = ds.getConnection();
			 PreparedStatement s1 = conn.prepareStatement("INSERT INTO STATEMENTS (StatementTypeId, DocumentId, Start, Stop, Coder) VALUES (?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
			 PreparedStatement s2 = conn.prepareStatement("INSERT INTO DATASHORTTEXT (StatementId, RoleVariableLinkId, Entity) VALUES (?, ?, ?);");
			 PreparedStatement s3 = conn.prepareStatement("INSERT INTO DATALONGTEXT (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
			 PreparedStatement s4 = conn.prepareStatement("INSERT INTO DATAINTEGER (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
			 PreparedStatement s5 = conn.prepareStatement("INSERT INTO DATABOOLEAN (StatementId, RoleVariableLinkId, Value) VALUES (?, ?, ?);");
			 PreparedStatement s6 = conn.prepareStatement("INSERT INTO ENTITIES (VariableId, Value, Red, Green, Blue) VALUES (?, ?, ?, ?, ?);");
			 PreparedStatement s7 = conn.prepareStatement("SELECT ID FROM ENTITIES WHERE VariableId = ? AND Value = ?;");
			 PreparedStatement s8 = conn.prepareStatement("SELECT ID, AttributeVariable FROM ATTRIBUTEVARIABLES WHERE VariableId = ?;");
			 PreparedStatement s9 = conn.prepareStatement("INSERT INTO ATTRIBUTEVALUES (EntityId, AttributeVariableId, AttributeValue) VALUES (?, ?, ?);");
			 PreparedStatement s10 = conn.prepareStatement("SELECT COUNT(ID) FROM ENTITIES WHERE VariableId = ? AND Value = ?;");
			 PreparedStatement s11 = conn.prepareStatement("SELECT COUNT(ID) FROM ATTRIBUTEVALUES WHERE EntityId = ? AND AttributeVariableId = ?;");
			 PreparedStatement s12 = conn.prepareStatement("SELECT ROLEVARIABLELINKS.ID, ROLEVARIABLELINKS.RoleId, ROLES.RoleName, ROLEVARIABLELINKS.VariableId, VARIABLES.Variable AS VariableName, ROLES.NumDefault, VARIABLES.DataType FROM ROLEVARIABLELINKS INNER JOIN ROLES ON ROLES.ID = ROLEVARIABLELINKS.RoleID INNER JOIN VARIABLES ON VARIABLES.ID = ROLEVARIABLELINKS.VariableId WHERE ROLES.NumDefault > 0 AND ROLES.StatementTypeId = ?;");
			 SQLCloseable finish = conn::rollback) {
			conn.setAutoCommit(false);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[SQL] Started SQL transaction to add statement to Document " + statement.getDocumentId() + ".",
					"Started a new SQL transaction to add a new statement to the document with ID " + statement.getDocumentId() + ". The contents will not be written into the database until the transaction is committed.");
			Dna.logger.log(l);
			ResultSet r1, r2, r3;

			// insert into STATEMENTS table and retrieve primary key (statement ID)
			s1.setInt(1, statement.getStatementTypeId());
			s1.setInt(2, statement.getDocumentId());
			s1.setInt(3, statement.getStart());
			s1.setInt(4, statement.getStop());
			s1.setInt(5, statement.getCoderId());
			s1.executeUpdate();
			ResultSet generatedKeysResultSet = s1.getGeneratedKeys();
			while (generatedKeysResultSet.next()) {
				statementId = generatedKeysResultSet.getInt(1);
			}
			l = new LogEvent(Logger.MESSAGE,
					"[SQL]  ├─ Transaction: Row with ID " + statementId + " added to the STATEMENTS table.",
					"Added a row to the STATEMENTS table during the transaction. The new statement has ID " + statementId + ".");
			Dna.logger.log(l);

			// find out what roles and variables are associated with the statement type of the statement and process each role-variable combination
			int roleVariableLinkId = -1;
			int roleId = -1;
			int variableId = -1;
			int numDefault = -1;
			long entityId, attributeVariableId;
			String dataType = "", variableName = "", roleName = "";
			s12.setInt(1, statement.getStatementTypeId());
			r1 = s12.executeQuery();
			ArrayList<Integer> addedRoles = new ArrayList<Integer>();
			while (r1.next()) {
				roleVariableLinkId = r1.getInt("ID");
				roleId = r1.getInt("RoleId");
				roleName = r1.getString("RoleName");
				variableId = r1.getInt("VariableId");
				variableName = r1.getString("VariableName");
				numDefault = r1.getInt("NumDefault");
				dataType = r1.getString("DataType");
				if (roleVariableLinkId < 0 || roleId < 0 || variableId < 0) {
					l = new LogEvent(Logger.ERROR,
							"[SQL]  ├─ Failed to find role-variable ID for statement.",
							"Statement " + statementId + ": could not find role-variable ID (role ID " + roleId + "; variable ID: " + variableId + ").");
					Dna.logger.log(l);
					throw new SQLException();
				}

				int finalRoleId = roleId;
				if (dataType.equals("short text") && addedRoles.stream().filter(r -> r == finalRoleId).count() < numDefault) { // don't add multiple variables for the same role
					// look up if an entry exists in ENTITIES and create zero-length entity if not
					s10.setInt(1, variableId);
					s10.setString(2, "");
					r2 = s10.executeQuery();
					while (r2.next()) {
						if (r2.getInt(1) > 0) {
							// entity exists already; do nothing
						} else {
							// entity does not exist; add it to ENTITIES table
							s6.setInt(1, variableId);
							s6.setString(2, "");
							s6.setInt(3, 0);
							s6.setInt(4, 0);
							s6.setInt(5, 0);
							try {
								s6.executeUpdate();
								l = new LogEvent(Logger.MESSAGE,
										"[SQL]  ├─ Transaction: Added \"\" to the ENTITIES table.",
										"Added a row with value \"\" to the ENTITIES table during the transaction.");
								Dna.logger.log(l);
							} catch (SQLException e2) {
								l = new LogEvent(Logger.WARNING,
										"[SQL]  ├─ Failed to add value \"\" to the ENTITIES table.",
										"Failed to add value \"\" to the ENTITIES table. The next step will check if the attribute is already there. If so, no problem. If not, there will be another log event with an error message.",
										e2);
								Dna.logger.log(l);
							}
						}
					}

					// find the entity ID for the entity that was just added (or that may have already existed)
					entityId = -1;
					s7.setInt(1, variableId);
					s7.setString(2, "");
					r2 = s7.executeQuery();
					while (r2.next()) {
						entityId = r2.getInt("ID");
					}
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Entity ID identified as " + entityId + ".",
							"The entity \"\", which was added to, or identified in, the ENTITIES table during the transaction, has ID " + entityId + ".");
					Dna.logger.log(l);

					// find attribute variable IDs for the entity and insert new values to the ATTRIBUTEVALUES table (catch errors if they already exist)
					s8.setInt(1, variableId); // set variable ID to find all attribute variables by ID corresponding to the entity
					r2 = s8.executeQuery();
					while (r2.next()) {
						try {
							attributeVariableId = r2.getInt("ID");
							s11.setLong(1, entityId);
							s11.setLong(2, attributeVariableId);
							r3 = s11.executeQuery();
							while (r3.next()) {
								if (r3.getInt(1) > 0) {
									// attribute value already exists in the ATTRIBUTEVALUES table; don't do anything
								} else {
									s9.setLong(1, entityId); // entity ID
									s9.setLong(2, attributeVariableId); // attribute variable ID
									s9.setString(3, ""); // put an empty value into the attribute value field initially
									s9.executeUpdate();
									l = new LogEvent(Logger.MESSAGE,
											"[SQL]  ├─ Transaction: Added attribute \"" + r2.getString("AttributeVariable") + "\" for Entity " + entityId + " to the ATTRIBUTEVALUES table.",
											"Added attribute \"" + r2.getString("AttributeVariable") + "\" for Entity " + entityId + " to the ATTRIBUTEVALUES table during the transaction.");
									Dna.logger.log(l);
								}
							}
						} catch (Exception e2) {
							l = new LogEvent(Logger.WARNING,
									"[SQL]  ├─ Failed to add a new value for attribute variable \"" + r2.getString("AttributeVariable") + "\" for Entity " + entityId + " to the ATTRIBUTEVALUES table.",
									"Failed to add a new value for attribute variable \"" + r2.getString("AttributeVariable") + "\" for Entity " + entityId + " to the ATTRIBUTEVALUES table. The next step will check if the attribute is already there. If so, no problem. If not, there will be another log event with an error message.",
									e2);
							Dna.logger.log(l);
						}
					}

					// finally, write into the DATASHORTTEXT table
					for (int i = 0; i < numDefault; i++) {
						s2.setLong(1, statementId);
						s2.setInt(2, roleVariableLinkId);
						s2.setLong(3, entityId);
						s2.executeUpdate();
						addedRoles.add(roleId);
						l = new LogEvent(Logger.MESSAGE,
								"[SQL]  ├─ Transaction: Added an entity to the DATASHORTTEXT table.",
								"Added a row with entity ID " + entityId + " for Variable \"" + variableName + "\" (ID " + variableId + ") and Role \"" + roleName + "\" (ID " + roleId + ") to the DATASHORTTEXT table during the transaction.");
						Dna.logger.log(l);
					}
				} else if (dataType.equals("long text")) {
					// create empty values in DATALONGTEXT table
					s3.setInt(1, statementId);
					s3.setInt(2, roleVariableLinkId);
					s3.setString(3, "");
					s3.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added a value to the DATALONGTEXT table.",
							"Added a row for Variable \"" + variableName + "\" (ID " + variableId + ") and Role \"" + roleName + "\" (ID " + roleId + ") to the DATALONGTEXT table during the transaction.");
					Dna.logger.log(l);
				} else if (dataType.equals("integer")) {
					// create empty values in DATAINTEGER table
					s4.setInt(1, statementId);
					s4.setInt(2, roleVariableLinkId);
					s4.setInt(3, 0);
					s4.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added a value to the DATAINTEGER table.",
							"Added a row with Value 0 for Variable \"" + variableName + "\" (ID " + variableId + ") and Role \"" + roleName + "\" (ID " + roleId + ") to the DATAINTEGER table during the transaction.");
					Dna.logger.log(l);
				} else if (dataType.equals("boolean")) {
					// create empty values in DATABOOLEAN table
					s5.setInt(1, statementId);
					s5.setInt(2, roleVariableLinkId);
					s5.setInt(3, 1);
					s5.executeUpdate();
					l = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Transaction: Added a value to the DATABOOLEAN table.",
							"Added a row with Value 1 for Variable \"" + variableName + "\" (ID " + variableId + ") and Role \"" + roleName + "\" (ID " + roleId + ") to the DATABOOLEAN table during the transaction.");
					Dna.logger.log(l);
				}
			}
			conn.commit();
			l = new LogEvent(Logger.MESSAGE,
					"[SQL]  └─ Completed SQL transaction to add Statement " + statementId + ".",
					"Completed SQL transaction to add a new statement with ID " + statementId + " to Document " + statement.getDocumentId() + ". The contents have been written into the database.");
			Dna.logger.log(l);
		} catch (SQLException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[SQL]  └─ Failed to add statement to Document " + statement.getDocumentId() + ".",
					"Failed to add statement to Document " + statement.getDocumentId() + ". Check the connection and database availability.",
					e);
			Dna.logger.log(l);
		}
		return (int) statementId;
	}

	public ArrayList<Entity> getEntities(int statementTypeId) {
		ArrayList<Entity> entities = new ArrayList<>();
		String q1 = "SELECT DISTINCT VariableId FROM VARIABLES " +
				"INNER JOIN ROLEVARIABLELINKS ON VARIABLES.ID = ROLEVARIABLELINKS.VariableId " +
				"INNER JOIN ROLES ON ROLES.ID = ROLEVARIABLELINKS.RoleId " +
				"WHERE StatementTypeId = ? ORDER BY 1 ASC;";
		String q2a = "SELECT E.ID, E.VariableId, E.Value, E.Red, E.Green, E.Blue, E.ChildOf, " +
				"CASE WHEN EXISTS (SELECT ID from DATASHORTTEXT D WHERE D.Entity = E.ID) THEN 1 ELSE 0 END AS InDatabase " +
				"FROM ENTITIES E WHERE E.VariableId IN ("; // to be completed with variable listing, such as "1, 4, 6);"
		// Alternative query string; not sure which one is faster:
		// String q2b = "SELECT ID, VariableId, Value, Red, Green, Blue, ChildOf, " +
		// 		"(SELECT COUNT(ID) FROM DATASHORTTEXT WHERE Entity = ENTITIES.ID) AS Count " +
		// 		"FROM ENTITIES WHERE VariableId IN ("; // to be completed with variable listing, such as "1, 4, 6);"
		String q3 = "SELECT EntityId, AttributeVariable, AttributeValue "
				+ "FROM ATTRIBUTEVALUES AS AVAL "
				+ "INNER JOIN ATTRIBUTEVARIABLES AS AVAR ON AVAL.AttributeVariableId = AVAR.ID "
				+ "WHERE VariableId = ?;";
		try (Connection conn = ds.getConnection();
			 PreparedStatement s1 = conn.prepareStatement(q1);
			 PreparedStatement s3 = conn.prepareStatement(q3)) {
			HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>(); // entity ID to index
			ResultSet r;
			int entityId;

			// get variable IDs corresponding to the provided statement type
			s1.setInt(1, statementTypeId);
			r = s1.executeQuery();
			ArrayList<Integer> variableIdList = new ArrayList<>();
			while (r.next()) {
				variableIdList.add(r.getInt("VariableId"));
			}
			String variableIdString = variableIdList
					.stream()
					.map(v -> "" + v)
					.collect(Collectors.joining(", "));

			// get all entities for all variables related to the statement type
			PreparedStatement s2 = conn.prepareStatement(q2a + variableIdString + ");");
			r = s2.executeQuery();
			while (r.next()) {
				entityId = r.getInt("ID");
				indexMap.put(entityId, entities.size());
				entities.add(new Entity(entityId,
						r.getInt("VariableId"),
						r.getString("Value"),
						new Color(r.getInt("Red"),
								r.getInt("Green"),
								r.getInt("Blue")),
						r.getInt("ChildOf"),
						r.getInt("InDatabase") == 1,
						new HashMap<String, String>()));
			}

			// add attributes to the list elements via map
			for (Integer variableId : variableIdList) {
				s3.setInt(1, variableId);
				r = s3.executeQuery();
				while (r.next()) {
					entities.get(indexMap.get(r.getInt("EntityId"))).getAttributeValues().put(r.getString("AttributeVariable"), r.getString("AttributeValue"));
				}
			}
		} catch (SQLException e1) {
			LogEvent e = new LogEvent(Logger.WARNING,
					"[SQL] Entities could not be retrieved.",
					"Entities for statement type " + statementTypeId + " could not be retrieved. Check if the database is still there and/or if the connection has been interrupted, then try again.",
					e1);
			Dna.logger.log(e);
		}
		return entities;
	}

	public ArrayList<RoleVariableLink> getRoleVariableLinks(int statementTypeId) {
		ArrayList<RoleVariableLink> roleVariableLinks = new ArrayList<>();
		try (Connection conn = ds.getConnection();
			 PreparedStatement s1 = conn.prepareStatement("SELECT ROLEVARIABLELINKS.ID, RoleId, VariableId FROM ROLEVARIABLELINKS INNER JOIN ROLES ON ROLEVARIABLELINKS.RoleId = ROLES.ID WHERE ROLES.StatementTypeId = ?;");) {
			ResultSet r;
			s1.setInt(1, statementTypeId);
			r = s1.executeQuery();
			while (r.next()) {
				roleVariableLinks.add(new RoleVariableLink(r.getInt("ID"), r.getInt("RoleId"), r.getInt("VariableId")));
			}
		} catch (SQLException e1) {
			LogEvent e = new LogEvent(Logger.WARNING,
					"[SQL] Role-value links could not be retrieved.",
					"Role-value links could not be retrieved from the database. Check if the database is still there and/or if the connection has been interrupted, then try again.",
					e1);
			Dna.logger.log(e);
		}
		return roleVariableLinks;
	}
}