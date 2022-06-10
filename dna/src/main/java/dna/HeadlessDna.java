package dna;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.rosuda.JRI.RConsoleOutputStream;
import org.rosuda.JRI.Rengine;

import logger.LogEvent;
import logger.Logger;
import model.Coder;
import model.StatementType;
import sql.ConnectionProfile;
import sql.Sql;

/**
 * A class with public functions for accessing DNA without a GUI.
 */
public class HeadlessDna {

	/**
	 * Constructor for creating an instance of the headless DNA class.
	 */
	public HeadlessDna() {
		// divert stdout to R console
		Rengine r = new Rengine();
		RConsoleOutputStream rs = new RConsoleOutputStream(r, 0);
		System.setOut(new PrintStream(rs));
		System.setErr(new PrintStream(rs));
	}

	/**
	 * A function for generating a brief report with details about the database.
	 * 
	 * @return A string with details about the database.
	 */
	public String printDatabaseDetails() {
		if (Dna.sql == null || Dna.sql.getConnectionProfile() == null || Dna.sql.getActiveCoder() == null) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"Database details could not be aggregated.",
					"Failed to aggregate and show database details because there was no active connection or coder. Try opening a database first.");
			Dna.logger.log(l);
			System.err.println("Database details could not be aggregated.");
			return "";
		}
		int documents = Dna.sql.countDocuments();
		int statements = Dna.sql.countStatements(-1);
		int[] coderItems = Dna.sql.countCoderItems(Dna.sql.getActiveCoder().getId());
		ArrayList<StatementType> statementTypes = Dna.sql.getStatementTypes();
		String statementString = " statements in ";
		if (statements == 1) {
			statementString = " statement in ";
		}
		String documentString = " documents.";
		if (documents == 1) {
			documentString = " document";
		}
		String s = "DNA database: " + Dna.sql.getConnectionProfile().getUrl() + "\n" + statements + statementString + documents + documentString + "\nStatement types: ";
		for (int i = 0; i < statementTypes.size(); i++) {
			s = s + statementTypes.get(i).getLabel() + " (" + Dna.sql.countStatements(statementTypes.get(i).getId()) + ")";
			if (i < statementTypes.size() - 1) {
				s = s + ", ";
			}
		}
		s = s + ".\nActive coder '" + Dna.sql.getActiveCoder().getName() + "' (ID: " + Dna.sql.getActiveCoder().getId() + ") owns " + coderItems[0] + " documents and " + coderItems[1] + " statements.";
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"Database details were aggregated for printing.",
				"The active coder successfully queried the database for the number of documents and statements and similar statistics.");
		Dna.logger.log(l);
		return s;
	}
	
	/**
	 * Open a database connection and authenticate the coder.
	 * 
	 * @param coderId The coder ID.
	 * @param coderPassword The coder password.
	 * @param type The database type. Can be {@code "sqlite"}, {@code "mysql"},
	 *   or {@code "postgresql"}.
	 * @param databaseUrl The database URL (for MySQL/MariaDB or PostgreSQL) or
	 *   file name with full path (for SQLite).
	 * @param databaseName The database name. Can be an empty string ({@code
	 *   ""}) for SQLite.
	 * @param databasePort The connection port for the database. Can be {@code
	 *   -1} for SQLite.
	 * @param databaseUser The user name to connect to the database. Can be an
	 *   empty string ({@code ""}) for SQLite.
	 * @param databasePassword The password to connect to the database. Can be
	 *   an empty string ({@code ""}) for SQLite.
	 * @return Indicator of successful coder authentication.
	 */
	public boolean openDatabase(int coderId, String coderPassword, String type, String databaseUrl, String databaseName, int databasePort, String databaseUser, String databasePassword) {
		ConnectionProfile cp = new ConnectionProfile(type, databaseUrl, databaseName, databasePort, databaseUser, databasePassword);
		Sql testSql = new Sql(cp, true);
		boolean success = testSql.authenticate(coderId, coderPassword);
		if (success) {
			Dna.sql.setConnectionProfile(cp, false);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"Coder " + Dna.sql.getActiveCoder().getId() + " (" + Dna.sql.getActiveCoder().getName() + ") successfully authenticated.",
					"Coder " + Dna.sql.getActiveCoder().getId() + " (" + Dna.sql.getActiveCoder().getName() + ") successfully authenticated. You can now use the functions available to this user.");
			Dna.logger.log(l);
		} else {
			LogEvent l = new LogEvent(Logger.ERROR,
					"Failed to authenticate coder " + coderId + ".",
					"Coder " + coderId + " could not be authenticated. Check the coder ID and password. You can query the available coders using the 'queryCoders' function.");
			Dna.logger.log(l);
			System.err.println("Failed to authenticate coder " + coderId + ".");
		}
		return success;
	}
	
	/**
	 * Open a connection profile and authenticate its coder.
	 * 
	 * @param fileName The file name with full path to access the connection
	 *   profile. 
	 * @param clearCoderPassword The clear coder password.
	 * @return Indicator of whether the database was opened and the coder was
	 *   successfully authenticated.
	 */
	public boolean openConnectionProfile(String fileName, String clearCoderPassword) {
		ConnectionProfile cp = null;
		try {
			cp = Dna.dna.readConnectionProfile(fileName, clearCoderPassword);
		} catch (EncryptionOperationNotPossibleException e2) {
			cp = null;
			LogEvent l = new LogEvent(Logger.ERROR,
					"Connection profile could not be decrypted.",
					"Tried to decrypt a connection profile, but the decryption failed. Make sure the password corresponds to the coder who saved the connection profile.",
					e2);
			Dna.logger.log(l);
			System.err.println("Connection profile could not be decrypted.");
		}
		if (cp != null) {
			Sql sqlTemp = new Sql(cp, true); // just for authentication purposes, so a test
			if (sqlTemp.getDataSource() == null) {
				LogEvent l = new LogEvent(Logger.ERROR,
						"No data source available in the database connection.",
						"Tried to a connection profile, but the coder could not be authenticated because there is no data source available. This may be due to a failed connection to the database. Look out for other error messages.");
				Dna.logger.log(l);
				System.err.println("No data source available in the database connection.");
			} else {
				boolean authenticated = sqlTemp.authenticate(-1, clearCoderPassword);
				if (authenticated) {
					Dna.sql.setConnectionProfile(sqlTemp.getConnectionProfile(), false);
					LogEvent l = new LogEvent(Logger.MESSAGE,
							"Connection profile opened and coder authenticated.",
							"A connection profile was opened, and coder " + Dna.sql.getActiveCoder().getId() + " (" + Dna.sql.getActiveCoder().getName() + ") was successfully authenticated. You can now use the functions available to this user.");
					Dna.logger.log(l);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Save connection profile to a file.
	 * 
	 * @param fileName File name with absolute path.
	 * @param clearCoderPassword The clear coder password (for verification).
	 * @return Indicator of success.
	 */
	public boolean saveConnectionProfile(String fileName, String clearCoderPassword) {
		if (fileName == null || !fileName.toLowerCase().endsWith(".dnc")) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"File name must end with '.dnc'.",
					"Tried to save a connection profile with the following file name, but the file name does not end with '.dnc' as required: " + fileName + ".");
			Dna.logger.log(l);
			System.err.println("File name must end with '.dnc'.");
			return false;
		}
		File file = new File(fileName);
		if (file.exists()) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"File already exists and will not be overwritten.",
					"Tried to save a connection profile with the following file name, but the file already exists and will not be overwritten: " + fileName + ".");
			Dna.logger.log(l);
			System.err.println("File already exists and will not be overwritten.");
			return false;
		}
		boolean authenticated = Dna.sql.authenticate(-1, clearCoderPassword);
		if (authenticated) {
			// write the connection profile to disk, with an encrypted version of the password
			Dna.dna.writeConnectionProfile(fileName, new ConnectionProfile(Dna.sql.getConnectionProfile()), clearCoderPassword);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"Connection profile saved to file.",
					"A connection profile was successfully saved to the following file: " + fileName + ".");
			Dna.logger.log(l);
			return true;
		} else {
			LogEvent l = new LogEvent(Logger.ERROR,
					"Coder password could not be verified. Try again.",
					"Tried to save a connection profile, but the coder could not be authenticated. Please try again.");
			Dna.logger.log(l);
			System.err.println("Coder password could not be verified. Try again.");
			return false;
		}
	}
	
	/**
	 * Close the current database.
	 */
	public void closeDatabase() {
		Dna.sql.setConnectionProfile(null, false);
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"Database was closed.",
				"Closed database connection.");
		Dna.logger.log(l);
	}
	
	/**
	 * Return IDs, names, and colors of all coders found in a database.
	 * 
	 * @param type The database type. Can be {@code "sqlite"}, {@code "mysql"},
	 *   or {@code "postgresql"}.
	 * @param databaseUrl The database URL (for MySQL/MariaDB or PostgreSQL) or
	 *   file name with full path (for SQLite).
	 * @param databaseName The database name. Can be an empty string ({@code
	 *   ""}) for SQLite.
	 * @param databasePort The connection port for the database. Can be {@code
	 *   -1} for SQLite.
	 * @param databaseUser The user name to connect to the database. Can be an
	 *   empty string ({@code ""}) for SQLite.
	 * @param databasePassword The password to connect to the database. Can be
	 *   an empty string ({@code ""}) for SQLite.
	 * @return An {@link Object} array, where the first element is an {@link
	 *   int} array of coder IDs, the second element is a {@link String} array
	 *   of coder names, and the third element is an array of coder colors.
	 */
	public Object[] queryCoders(String type, String databaseUrl, String databaseName, int databasePort, String databaseUser, String databasePassword) {
		ConnectionProfile testCp = new ConnectionProfile(type, databaseUrl, databaseName, databasePort, databaseUser, databasePassword);
		Sql testSql = new Sql(testCp, true);
		ArrayList<Coder> coders = testSql.queryCoders();
		Object[] objects = new Object[3];
		objects[0] = coders.stream().mapToInt(c -> c.getId()).toArray();
		objects[1] = coders.stream().map(c -> c.getName()).toArray(String[]::new);
		objects[2] = coders
				.stream()
				.map(c -> c.getColor())
				.map(col -> String.format("#%02X%02X%02X",
						col.getRed(),
						col.getGreen(),
						col.getBlue()))
				.toArray(String[]::new);
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"Coders have been queried.",
				"The coders for the following database were queried successfully: " + databaseUrl);
		Dna.logger.log(l);
		return objects;
	}
}