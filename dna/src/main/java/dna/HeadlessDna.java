package dna;

import java.io.File;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import export.*;
import me.tongfei.progressbar.ProgressBar;
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
public class HeadlessDna implements Logger.LogListener {
	private Exporter exporter;

	/**
	 * Constructor for creating an instance of the headless DNA class.
	 */
	public HeadlessDna() {
		// divert stdout to R console
		Rengine r = new Rengine();
		RConsoleOutputStream rsRegular = new RConsoleOutputStream(r, 0);
		System.setOut(new PrintStream(rsRegular));
		RConsoleOutputStream rsWarningError = new RConsoleOutputStream(r, 1);
		System.setErr(new PrintStream(rsWarningError));
	}

	/**
	 * A function for generating a brief report with details about the database.
	 */
	public void printDatabaseDetails() {
		String s = "";
		if (Dna.sql == null || Dna.sql.getConnectionProfile() == null || Dna.sql.getActiveCoder() == null) {
			s = "No active database.";
		} else {
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
			s = "DNA database: " + Dna.sql.getConnectionProfile().getUrl() + "\n" + statements + statementString + documents + documentString + "\nStatement types: ";
			for (int i = 0; i < statementTypes.size(); i++) {
				s = s + statementTypes.get(i).getLabel() + " (" + Dna.sql.countStatements(statementTypes.get(i).getId()) + ")";
				if (i < statementTypes.size() - 1) {
					s = s + ", ";
				}
			}
			s = s + ".\nActive coder '" + Dna.sql.getActiveCoder().getName() + "' (ID: " + Dna.sql.getActiveCoder().getId() + ") owns " + coderItems[0] + " documents and " + coderItems[1] + " statements.";
		}
		System.out.println(s);
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"Database details were aggregated and printed.",
				"The active coder queried the database for the number of documents and statements and similar statistics and printed the output to the console.");
		Dna.logger.log(l);
	}
	
	/**
	 * Open a database connection and authenticate the coder.
	 * 
	 * @param coderId The coder ID. If smaller than {@code 1}, a coder password
	 *   check dialog is shown.
	 * @param coderPassword The coder password. If {@code null}, a coder
	 *   password check dialog is shown.
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
		if (coderId < 1) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"Failed to authenticate coder " + coderId + ".",
					"Coder " + coderId + " could not be authenticated. Check the coder ID and password. You can query the available coders using the 'queryCoders' function.");
			Dna.logger.log(l);
			return false;
		}
		ConnectionProfile cp = new ConnectionProfile(type, databaseUrl, databaseName, databasePort, databaseUser, databasePassword);
		Sql testSql = new Sql(cp, true);
		boolean success = testSql.authenticate(coderId, coderPassword);
		if (success) {
			Dna.sql.setConnectionProfile(cp, false);
			Dna.sql.selectCoder(coderId);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"Coder " + Dna.sql.getActiveCoder().getId() + " (" + Dna.sql.getActiveCoder().getName() + ") successfully authenticated.",
					"Coder " + Dna.sql.getActiveCoder().getId() + " (" + Dna.sql.getActiveCoder().getName() + ") successfully authenticated. You can now use the functions available to this user.");
			Dna.logger.log(l);
			printDatabaseDetails();
		} else {
			LogEvent l = new LogEvent(Logger.ERROR,
					"Failed to authenticate coder " + coderId + ".",
					"Coder " + coderId + " could not be authenticated. Check the coder ID and password. You can query the available coders using the 'queryCoders' function.");
			Dna.logger.log(l);
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
			cp = Dna.readConnectionProfile(fileName, clearCoderPassword);
		} catch (EncryptionOperationNotPossibleException e2) {
			cp = null;
			LogEvent l = new LogEvent(Logger.ERROR,
					"Connection profile could not be decrypted.",
					"Tried to decrypt a connection profile, but the decryption failed. Make sure the password corresponds to the coder who saved the connection profile.",
					e2);
			Dna.logger.log(l);
		}
		if (cp != null) {
			Sql sqlTemp = new Sql(cp, true); // just for authentication purposes, so a test
			if (sqlTemp.getDataSource() == null) {
				LogEvent l = new LogEvent(Logger.ERROR,
						"No data source available in the database connection.",
						"Tried to a connection profile, but the coder could not be authenticated because there is no data source available. This may be due to a failed connection to the database. Look out for other error messages.");
				Dna.logger.log(l);
			} else {
				boolean authenticated = sqlTemp.authenticate(-1, clearCoderPassword);
				if (authenticated) {
					Dna.sql.setConnectionProfile(sqlTemp.getConnectionProfile(), false);
					LogEvent l = new LogEvent(Logger.MESSAGE,
							"Connection profile opened and coder authenticated.",
							"A connection profile was opened, and coder " + Dna.sql.getActiveCoder().getId() + " (" + Dna.sql.getActiveCoder().getName() + ") was successfully authenticated. You can now use the functions available to this user.");
					Dna.logger.log(l);
					printDatabaseDetails();
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
		if (Dna.sql == null || Dna.sql.getConnectionProfile() == null || Dna.sql.getActiveCoder() == null) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"No database open. Could not save connection profile.",
					"Tried to save a connection profile, but no database connection was open or no coder was active.");
			Dna.logger.log(l);
			return false;
		}
		if (fileName == null || !fileName.toLowerCase().endsWith(".dnc")) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"File name must end with '.dnc'.",
					"Tried to save a connection profile with the following file name, but the file name does not end with '.dnc' as required: \"" + fileName + "\".");
			Dna.logger.log(l);
			return false;
		}
		File file = new File(fileName);
		if (file.exists()) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"File already exists and will not be overwritten.",
					"Tried to save a connection profile with the following file name, but the file already exists and will not be overwritten: \"" + fileName + "\".");
			Dna.logger.log(l);
			return false;
		}
		boolean authenticated = Dna.sql.authenticate(-1, clearCoderPassword);
		if (authenticated) {
			// write the connection profile to disk, with an encrypted version of the password
			Dna.writeConnectionProfile(fileName, new ConnectionProfile(Dna.sql.getConnectionProfile()), clearCoderPassword);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"Connection profile saved to file.",
					"A connection profile was successfully saved to the following file: \"" + fileName + "\".");
			Dna.logger.log(l);
			return true;
		} else {
			LogEvent l = new LogEvent(Logger.ERROR,
					"Coder password could not be verified. Try again.",
					"Tried to save a connection profile, but the coder could not be authenticated. Please try again.");
			Dna.logger.log(l);
			return false;
		}
	}
	
	/**
	 * Close the current database.
	 */
	public void closeDatabase() {
		if (Dna.sql == null || Dna.sql.getConnectionProfile() == null || Dna.sql.getActiveCoder() == null) {
			Dna.sql.setConnectionProfile(null, false);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"Tried to close database, but none was open.",
					"Tried to close database, but none was open. Set the current connection profile to null anyway.");
			Dna.logger.log(l);
			System.out.println("Tried to close database, but none was open.");
		} else {
			Dna.sql.setConnectionProfile(null, false);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"Database was closed.",
					"Closed database connection.");
			Dna.logger.log(l);
			System.out.println("Database was closed.");
		}
		
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

	/**
	 * Convert start and stop date and time strings into {@link LocalDateTime} objects.
	 *
	 * @param startDate The start date. If {@code "dd.MM.yyyy"}, {@code ""}, or {@code null}, the earliest date in the
	 *                  database will be queried instead.
	 * @param startTime The start time. If {@code "00:00:00"}, {@code ""}, or {@code null}, {@code "00:00:00"} will be
	 *                  used.
	 * @param stopDate The end date. If {@code "dd.MM.yyyy"}, {@code ""}, or {@code null}, the last date in the
	 * 	               database will be queried instead.
	 * @param stopTime The end time. If {@code "00:00:00"}, {@code ""}, or {@code null}, {@code "00:00:00"} will be
	 * 	               used.
	 * @return A {@link LocalDateTime} array with two elements for the start and end date/time.
	 */
	private LocalDateTime[] formatDateTime(String startDate, String startTime, String stopDate, String stopTime) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
		LocalDateTime ldtStart, ldtStop;
		LocalDateTime[] dateRange = Dna.sql.getDateTimeRange();
		if (startTime == null || startTime.equals("")) {
			startTime = "00:00:00";
		}
		if (startDate == null || startDate.equals("") || startDate.equals("01.01.1900")) {
			ldtStart = dateRange[0];
		} else {
			String startString = startDate + " " + startTime;
			ldtStart = LocalDateTime.parse(startString, dtf);
			if (!startString.equals(dtf.format(ldtStart))) {
				ldtStart = dateRange[0];
				LogEvent le = new LogEvent(Logger.WARNING,
						"Start date or time is invalid.",
						"When initializing computations, the start date or time (" + startString + ") did not conform to the format dd.MM.yyyy HH:mm:ss and could not be interpreted. Assuming earliest date and time in the dataset: " + ldtStart.format(dtf) + ".");
				Dna.logger.log(le);
			}
		}
		if (stopTime == null || stopTime.equals("")) {
			stopTime = "23:59:59";
		}
		if (stopDate == null || stopDate.equals("") || stopDate.equals("31.12.2099")) {
			ldtStop = dateRange[1];
		} else {
			String stopString = stopDate + " " + stopTime;
			ldtStop = LocalDateTime.parse(stopString, dtf);
			if (!stopString.equals(dtf.format(ldtStop))) {
				ldtStop = dateRange[1];
				LogEvent le = new LogEvent(Logger.WARNING,
						"End date or time is invalid.",
						"When initializing computations, the end date or time (" + stopString + ") did not conform to the format dd.MM.yyyy HH:mm:ss and could not be interpreted. Assuming latest date and time in the dataset: " + ldtStop.format(dtf) + ".");
				Dna.logger.log(le);
			}
		}
		return new LocalDateTime[] { ldtStart, ldtStop };
	}

	/**
	 * Compute one-mode or two-mode network matrix based on R arguments.
	 *
	 * @param networkType            The network type as provided by rDNA (can be {@code "eventlist"}, {@code "twomode"}, or {@code "onemode"}).
	 * @param statementType          Statement type as a {@link String}.
	 * @param variable1              First variable for export, provided as a {@link String}.
	 * @param variable1Document      boolean indicating if the first variable is at the document level.
	 * @param variable2              Second variable for export, provided as a {@link String}.
	 * @param variable2Document      boolean indicating if the second variable is at the document level.
	 * @param qualifier              Qualifier variable as a {@link String}.
	 * @param qualifierDocument      boolean indicating if the qualifier variable is at the document level.
	 * @param qualifierAggregation   Aggregation rule for the qualifier variable (can be {@code "ignore"}, {@code "combine"}, {@code "subtract"}, {@code "congruence"}, or {@code "conflict"}).
	 * @param normalization          Normalization setting as a {@link String}, as provided by rDNA (can be {@code "no"}, {@code "activity"}, {@code "prominence"}, {@code "average"}, {@code "jaccard"}, or {@code "cosine"}).
	 * @param includeIsolates        boolean indicating whether nodes not currently present should still be inserted into the network matrix.
	 * @param duplicates             An input {@link String} from rDNA that can be {@code "include"}, {@code "document"}, {@code "week"}, {@code "month"}, {@code "year"}, or {@code "acrossrange"}.
	 * @param startDate              Start date for the export, provided as a {@link String} with format {@code "dd.MM.yyyy"}.
	 * @param stopDate               Stop date for the export, provided as a {@link String} with format {@code "dd.MM.yyyy"}.
	 * @param startTime              Start time for the export, provided as a {@link String} with format {@code "HH:mm:ss"}.
	 * @param stopTime               Stop time for the export, provided as a {@link String} with format {@code "HH:mm:ss"}.
	 * @param timeWindow             A {@link String} indicating the time window setting. Valid options are {@code "no"}, {@code "events"}, {@code "seconds"}, {@code "minutes"}, {@code "hours"}, {@code "days"}, {@code "weeks"}, {@code "months"}, and {@code "years"}.
	 * @param windowSize             Duration of the time window in the units specified in the {@code timeWindow} argument.
	 * @param excludeVariables       A {@link String} array with n elements, indicating the variable of the n'th value.
	 * @param excludeValues          A {@link String} array with n elements, indicating the value pertaining to the n'th variable {@link String}.
	 * @param excludeAuthors         A {@link String} array of values to exclude in the {@code author} variable at the document level.
	 * @param excludeSources         A {@link String} array of values to exclude in the {@code source} variable at the document level.
	 * @param excludeSections        A {@link String} array of values to exclude in the {@code section} variable at the document level.
	 * @param excludeTypes           A {@link String} array of values to exclude in the {@code "type"} variable at the document level.
	 * @param invertValues           boolean indicating whether the statement-level exclude values should be included (= {@code true}) rather than excluded.
	 * @param invertAuthors          boolean indicating whether the document-level author values should be included (= {@code true}) rather than excluded.
	 * @param invertSources          boolean indicating whether the document-level source values should be included (= {@code true}) rather than excluded.
	 * @param invertSections         boolean indicating whether the document-level section values should be included (= {@code true}) rather than excluded.
	 * @param invertTypes            boolean indicating whether the document-level type values should be included (= {@code true}) rather than excluded.
	 * @param outfile                {@link String} with a file name under which the resulting network should be saved.
	 * @param fileFormat             {@link String} with the file format. Valid values are {@code "csv"}, {@code "dl"}, {@code "graphml"}, and {@code null} (for no file export).
	 * @return                       A {@link Matrix} object containing the resulting one-mode or two-mode network.
	 */
	public void rNetwork(String networkType, String statementType, String variable1, boolean variable1Document, String variable2,
						 boolean variable2Document, String qualifier, boolean qualifierDocument, String qualifierAggregation, String normalization, boolean includeIsolates,
						 String duplicates, String startDate, String stopDate, String startTime, String stopTime, String timeWindow, int windowSize,
						 String[] excludeVariables, String[] excludeValues, String[] excludeAuthors, String[] excludeSources, String[] excludeSections,
						 String[] excludeTypes, boolean invertValues, boolean invertAuthors, boolean invertSources, boolean invertSections,
						 boolean invertTypes, String outfile, String fileFormat) {

		// step 1: preprocess arguments
		StatementType st = Dna.sql.getStatementType(statementType); // format statement type

		// format dates and times with input formats "dd.MM.yyyy" and "HH:mm:ss"
		LocalDateTime ldtStart, ldtStop;
		LocalDateTime[] dateRange = formatDateTime(startDate, startTime, stopDate, stopTime);
		ldtStart = dateRange[0];
		ldtStop = dateRange[1];

		// process exclude variables: create HashMap with variable:value pairs
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		if (excludeVariables.length > 0) {
			for (int i = 0; i < excludeVariables.length; i++) {
				ArrayList<String> values = map.get(excludeVariables[i]);
				if (values == null) {
					values = new ArrayList<String>();
				}
				if (!values.contains(excludeValues[i])) {
					values.add(excludeValues[i]);
				}
				Collections.sort(values);
				map.put(excludeVariables[i], values);
			}
		}

		// initialize Exporter class
		this.exporter = new Exporter(
				networkType,
				st,
				variable1,
				variable1Document,
				variable2,
				variable2Document,
				qualifier,
				qualifierDocument,
				qualifierAggregation,
				normalization,
				includeIsolates,
				duplicates,
				ldtStart,
				ldtStop,
				timeWindow,
				windowSize,
				map,
				Stream.of(excludeAuthors).collect(Collectors.toCollection(ArrayList::new)),
				Stream.of(excludeSources).collect(Collectors.toCollection(ArrayList::new)),
				Stream.of(excludeSections).collect(Collectors.toCollection(ArrayList::new)),
				Stream.of(excludeTypes).collect(Collectors.toCollection(ArrayList::new)),
				invertValues,
				invertAuthors,
				invertSources,
				invertSections,
				invertTypes,
				fileFormat,
				outfile);

		// step 2: filter
		this.exporter.loadData();
		this.exporter.filterStatements();

		// step 3: compute results
		if (networkType.equals("eventlist")) {
		} else {
			try {
				this.exporter.computeResults();
			} catch (Exception e) {
				LogEvent le = new LogEvent(Logger.ERROR,
						"Error while exporting network.",
						"An unexpected error occurred while exporting a network. See the stack trace for details. Consider reporting this error.",
						e);
				Dna.logger.log(le);
			}
		}

		// step 4: save to file
		if (fileFormat != null && !fileFormat.equals("") && outfile != null && !outfile.equals("")) {
			this.exporter.exportToFile();
		}
	}

	/**
	 * Generate data to construct a barplot.
	 *
	 * @param statementType     Statement type as a {@link String}.
	 * @param variable          First variable for export, provided as a {@link String}.
	 * @param qualifier         Qualifier variable as a {@link String}.
	 * @param duplicates        An input {@link String} from rDNA that can be {@code "include"}, {@code "document"}, {@code "week"}, {@code "month"}, {@code "year"}, or {@code "acrossrange"}.
	 * @param startDate         Start date for the export, provided as a {@link String} with format {@code "dd.MM.yyyy"}.
	 * @param stopDate          Stop date for the export, provided as a {@link String} with format {@code "dd.MM.yyyy"}.
	 * @param startTime         Start time for the export, provided as a {@link String} with format {@code "HH:mm:ss"}.
	 * @param stopTime          Stop time for the export, provided as a {@link String} with format {@code "HH:mm:ss"}.
	 * @param excludeVariables  A {@link String} array with n elements, indicating the variable of the n'th value.
	 * @param excludeValues     A {@link String} array with n elements, indicating the value pertaining to the n'th variable {@link String}.
	 * @param excludeAuthors    A {@link String} array of values to exclude in the {@code author} variable at the document level.
	 * @param excludeSources    A {@link String} array of values to exclude in the {@code source} variable at the document level.
	 * @param excludeSections   A {@link String} array of values to exclude in the {@code section} variable at the document level.
	 * @param excludeTypes      A {@link String} array of values to exclude in the {@code "type"} variable at the document level.
	 * @param invertValues      boolean indicating whether the statement-level exclude values should be included (= {@code true}) rather than excluded.
	 * @param invertAuthors     boolean indicating whether the document-level author values should be included (= {@code true}) rather than excluded.
	 * @param invertSources     boolean indicating whether the document-level source values should be included (= {@code true}) rather than excluded.
	 * @param invertSections    boolean indicating whether the document-level section values should be included (= {@code true}) rather than excluded.
	 * @param invertTypes       boolean indicating whether the document-level type values should be included (= {@code true}) rather than excluded.
	 * @return                  A {@link BarplotResult} object.
	 */
	public BarplotResult rBarplotData(String statementType, String variable, String qualifier, String duplicates,
									  String startDate, String stopDate, String startTime, String stopTime,
									  String[] excludeVariables, String[] excludeValues, String[] excludeAuthors,
									  String[] excludeSources, String[] excludeSections, String[] excludeTypes,
									  boolean invertValues, boolean invertAuthors, boolean invertSources, boolean invertSections,
									  boolean invertTypes) {

		// step 1: preprocess arguments
		StatementType st = Dna.sql.getStatementType(statementType); // format statement type

		// format dates and times with input formats "dd.MM.yyyy" and "HH:mm:ss"
		LocalDateTime ldtStart, ldtStop;
		LocalDateTime[] dateRange = formatDateTime(startDate, startTime, stopDate, stopTime);
		ldtStart = dateRange[0];
		ldtStop = dateRange[1];

		// process exclude variables: create HashMap with variable:value pairs
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		if (excludeVariables.length > 0) {
			for (int i = 0; i < excludeVariables.length; i++) {
				ArrayList<String> values = map.get(excludeVariables[i]);
				if (values == null) {
					values = new ArrayList<String>();
				}
				if (!values.contains(excludeValues[i])) {
					values.add(excludeValues[i]);
				}
				Collections.sort(values);
				map.put(excludeVariables[i], values);
			}
		}

		// initialize Exporter class for barplot data
		this.exporter = new Exporter(
				st,
				variable,
				qualifier,
				duplicates,
				ldtStart,
				ldtStop,
				map,
				Stream.of(excludeAuthors).collect(Collectors.toCollection(ArrayList::new)),
				Stream.of(excludeSources).collect(Collectors.toCollection(ArrayList::new)),
				Stream.of(excludeSections).collect(Collectors.toCollection(ArrayList::new)),
				Stream.of(excludeTypes).collect(Collectors.toCollection(ArrayList::new)),
				invertValues,
				invertAuthors,
				invertSources,
				invertSections,
				invertTypes);

		// step 2: filter
		this.exporter.loadData();
		this.exporter.filterStatements();

		// step 3: compute results
		BarplotResult barplotResult = this.exporter.generateBarplotData();
		return barplotResult;
	}

	/**
	 * Compute backbone and set of redundant entities on the second mode in a discourse network.
	 *
	 * @param p                      Penalty parameter, for example {@code 7.5}.
	 * @param T                      Number of iterations, for example {@code 50000}.
	 * @param statementType          Statement type as a {@link String}.
	 * @param variable1              First variable for export, provided as a {@link String}.
	 * @param variable1Document      boolean indicating if the first variable is at the document level.
	 * @param variable2              Second variable for export, provided as a {@link String}.
	 * @param variable2Document      boolean indicating if the second variable is at the document level.
	 * @param qualifier              Qualifier variable as a {@link String}.
	 * @param qualifierDocument      boolean indicating if the qualifier variable is at the document level.
	 * @param qualifierAggregation   Aggregation rule for the qualifier variable (can be {@code "ignore"}, {@code "combine"}, {@code "subtract"}, {@code "congruence"}, or {@code "conflict"}). Note that negative values in the {@code "subtract"} case are replaced by {@code 0}.
	 * @param normalization          Normalization setting as a {@link String}, as provided by rDNA (can be {@code "no"}, {@code "activity"}, {@code "prominence"}, {@code "average"}, {@code "jaccard"}, or {@code "cosine"}).
	 * @param duplicates             An input {@link String} from rDNA that can be {@code "include"}, {@code "document"}, {@code "week"}, {@code "month"}, {@code "year"}, or {@code "acrossrange"}.
	 * @param startDate              Start date for the export, provided as a {@link String} with format {@code "dd.MM.yyyy"}.
	 * @param stopDate               Stop date for the export, provided as a {@link String} with format {@code "dd.MM.yyyy"}.
	 * @param startTime              Start time for the export, provided as a {@link String} with format {@code "HH:mm:ss"}.
	 * @param stopTime               Stop time for the export, provided as a {@link String} with format {@code "HH:mm:ss"}.
	 * @param excludeVariables       A {@link String} array with n elements, indicating the variable of the n'th value.
	 * @param excludeValues          A {@link String} array with n elements, indicating the value pertaining to the n'th variable {@link String}.
	 * @param excludeAuthors         A {@link String} array of values to exclude in the {@code author} variable at the document level.
	 * @param excludeSources         A {@link String} array of values to exclude in the {@code source} variable at the document level.
	 * @param excludeSections        A {@link String} array of values to exclude in the {@code section} variable at the document level.
	 * @param excludeTypes           A {@link String} array of values to exclude in the {@code "type"} variable at the document level.
	 * @param invertValues           boolean indicating whether the statement-level exclude values should be included (= {@code true}) rather than excluded.
	 * @param invertAuthors          boolean indicating whether the document-level author values should be included (= {@code true}) rather than excluded.
	 * @param invertSources          boolean indicating whether the document-level source values should be included (= {@code true}) rather than excluded.
	 * @param invertSections         boolean indicating whether the document-level section values should be included (= {@code true}) rather than excluded.
	 * @param invertTypes            boolean indicating whether the document-level type values should be included (= {@code true}) rather than excluded.
	 * @param outfile                {@link String} with a file name under which the resulting network should be saved.
	 * @param fileFormat             {@link String} with the file format. Valid values are {@code "xml"}, {@code "json"}, and {@code null} (for no file export).
	 * @return                       A {@link BackboneResult} object containing the results.
	 */
	public void rBackbone(double p, int T, String statementType, String variable1, boolean variable1Document, String variable2,
						  boolean variable2Document, String qualifier, boolean qualifierDocument, String qualifierAggregation, String normalization,
						  String duplicates, String startDate, String stopDate, String startTime, String stopTime,
						  String[] excludeVariables, String[] excludeValues, String[] excludeAuthors, String[] excludeSources, String[] excludeSections,
						  String[] excludeTypes, boolean invertValues, boolean invertAuthors, boolean invertSources, boolean invertSections,
						  boolean invertTypes, String outfile, String fileFormat) {

		// step 1: preprocess arguments
		StatementType st = Dna.sql.getStatementType(statementType); // format statement type

		// format dates and times with input formats "dd.MM.yyyy" and "HH:mm:ss"
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
		LocalDateTime ldtStart, ldtStop;
		LocalDateTime[] dateRange = Dna.sql.getDateTimeRange();
		if (startTime == null || startTime.equals("")) {
			startTime = "00:00:00";
		}
		if (startDate == null || startDate.equals("") || startDate.equals("01.01.1900")) {
			ldtStart = dateRange[0];
		} else {
			String startString = startDate + " " + startTime;
			ldtStart = LocalDateTime.parse(startString, dtf);
			if (!startString.equals(dtf.format(ldtStart))) {
				ldtStart = dateRange[0];
				LogEvent le = new LogEvent(Logger.WARNING,
						"Start date or time is invalid.",
						"When computing the backbone and redundant set of the network, the start date or time (" + startString + ") did not conform to the format dd.MM.yyyy HH:mm:ss and could not be interpreted. Assuming earliest date and time in the dataset: " + ldtStart.format(dtf) + ".");
				Dna.logger.log(le);
			}
		}
		if (stopTime == null || stopTime.equals("")) {
			stopTime = "23:59:59";
		}
		if (stopDate == null || stopDate.equals("") || stopDate.equals("31.12.2099")) {
			ldtStop = dateRange[1];
		} else {
			String stopString = stopDate + " " + stopTime;
			ldtStop = LocalDateTime.parse(stopString, dtf);
			if (!stopString.equals(dtf.format(ldtStop))) {
				ldtStop = dateRange[1];
				LogEvent le = new LogEvent(Logger.WARNING,
						"End date or time is invalid.",
						"When computing the backbone and redundant set of the network, the end date or time (" + stopString + ") did not conform to the format dd.MM.yyyy HH:mm:ss and could not be interpreted. Assuming latest date and time in the dataset: " + ldtStop.format(dtf) + ".");
				Dna.logger.log(le);
			}
		}

		// process exclude variables: create HashMap with variable:value pairs
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		if (excludeVariables.length > 0) {
			for (int i = 0; i < excludeVariables.length; i++) {
				ArrayList<String> values = map.get(excludeVariables[i]);
				if (values == null) {
					values = new ArrayList<String>();
				}
				if (!values.contains(excludeValues[i])) {
					values.add(excludeValues[i]);
				}
				Collections.sort(values);
				map.put(excludeVariables[i], values);
			}
		}

		// initialize Exporter class
		this.exporter = new Exporter(
				"onemode",
				st,
				variable1,
				variable1Document,
				variable2,
				variable2Document,
				qualifier,
				qualifierDocument,
				qualifierAggregation,
				normalization,
				true,
				duplicates,
				ldtStart,
				ldtStop,
				"no",
				1,
				map,
				Stream.of(excludeAuthors).collect(Collectors.toCollection(ArrayList::new)),
				Stream.of(excludeSources).collect(Collectors.toCollection(ArrayList::new)),
				Stream.of(excludeSections).collect(Collectors.toCollection(ArrayList::new)),
				Stream.of(excludeTypes).collect(Collectors.toCollection(ArrayList::new)),
				invertValues,
				invertAuthors,
				invertSources,
				invertSections,
				invertTypes,
				null,
				null);

		// step 2: filter
		this.exporter.loadData();
		this.exporter.filterStatements();
		if (exporter.getFilteredStatements().size() == 0) {
			LogEvent le = new LogEvent(Logger.ERROR,
					"No statements left after filtering.",
					"Attempted to filter the statements by date and other criteria before finding backbone. But no statements were left after applying the filters. Perhaps the time period was mis-specified?");
			Dna.logger.log(le);
		}

		// step 3: compute results
		this.exporter.backbone(p, T); // initial results
		try (ProgressBar pb = new ProgressBar("Simulated annealing...", T)) {
			while (exporter.getCurrentT() <= T) { // run up to upper bound of iterations T, provided by the user
				pb.stepTo(exporter.getCurrentT());
				exporter.iterateBackbone();
			}
			exporter.saveBackboneResult();

			// step 4: save to file
			if (fileFormat != null && outfile != null) {
				if (fileFormat.equals("json") && !outfile.toLowerCase().endsWith(".json")) {
					outfile = outfile + ".json";
					LogEvent le = new LogEvent(Logger.WARNING,
							"Appended \".json\" to file name.",
							"The outfile for the backbone export did not end with \".json\" although the \"json\" file format was chosen. Appending \".json\" to the file name.");
					Dna.logger.log(le);
				}
				if (fileFormat.equals("xml") && !outfile.toLowerCase().endsWith(".xml")) {
					outfile = outfile + ".xml";
					LogEvent le = new LogEvent(Logger.WARNING,
							"Appended \".xml\" to file name.",
							"The outfile for the backbone export did not end with \".xml\" although the \"xml\" file format was chosen. Appending \".xml\" to the file name.");
					Dna.logger.log(le);
				}
				if (!fileFormat.equals("xml") && !fileFormat.equals("json")) {
					fileFormat = null;
					LogEvent le = new LogEvent(Logger.WARNING,
							"File format for backbone export not recognized.",
							"The file format for saving a backbone and redundant set to disk was not recognized. Valid file formats are \"json\" and \"xml\". The file format you provided was \"" + fileFormat + "\". Not saving the file to disk because the file format is unknown.");
					Dna.logger.log(le);
				} else {
					this.exporter.writeBackboneToFile(outfile);
				}
			}

			pb.stepTo(T);
		}
	}

	/* =================================================================================================================
	 * Functions for managing attributes
	 * =================================================================================================================
	 */

	/**
	 * Retrieve entities with attributes for a specific variable with a given variable ID.
	 *
	 * @param variableId ID of the variable to query for entities and their attributes.
	 * @return Data frame with entities and attributes, as defined in {@link sql.DataExchange#getAttributes(int)}.
	 */
	public DataFrame getAttributes(int variableId) {
		DataFrame df = sql.DataExchange.getAttributes(variableId);

		LogEvent l = new LogEvent(Logger.MESSAGE,
				"Attributes have been queried.",
				"The attributes for Variable " + variableId + " have been successfully retrieved from the database.");
		Dna.logger.log(l);

		return df;
	}

	/**
	 * Retrieve entities with attributes for a specific variable with a given variable name and statement type ID.
	 *
	 * @param statementTypeId The statement type ID in which the variable is defined.
	 * @param variable The name of the variable.
	 * @return Data frame with entities and attributes, as defined in {@link sql.DataExchange#getAttributes(int)}.
	 */
	public DataFrame getAttributes(int statementTypeId, String variable) {
		DataFrame df = sql.DataExchange.getAttributes(statementTypeId, variable);

		LogEvent l = new LogEvent(Logger.MESSAGE,
				"Attributes have been queried.",
				"The attributes for Variable \"" + variable + "\" have been successfully retrieved from the database.");
		Dna.logger.log(l);

		return df;
	}

	/**
	 * Retrieve entities with attributes for a specific variable with a given variable name and statement type name.
	 *
	 * @param statementType The statement type in which the variable is defined.
	 * @param variable The name of the variable.
	 * @return Data frame with entities and attributes, as defined in {@link sql.DataExchange#getAttributes(int)}.
	 */
	public DataFrame getAttributes(String statementType, String variable) {
		DataFrame df = sql.DataExchange.getAttributes(statementType, variable);

		LogEvent l = new LogEvent(Logger.MESSAGE,
				"Attributes have been queried.",
				"The attributes for Variable \"" + variable + "\" have been successfully retrieved from the database.");
		Dna.logger.log(l);

		return df;
	}

	/**
	 * Set entities and attributes for a specific variable.
	 *
	 * @param variableId ID of the variable to modify the entities and their attributes for.
	 * @param df A data frame with entities and attributes, as defined in {@link sql.DataExchange#getAttributes(int)}.
	 * @param simulate If {@code true}, the changes are rolled back. If {@code false}, the changes are committed to the database.
	 */
	public void setAttributes(int variableId, DataFrame df, boolean simulate) {
		sql.DataExchange.getAttributes(variableId);

		LogEvent l = new LogEvent(Logger.MESSAGE,
				"Attributes and entities have been set.",
				"The entities (with attributes) for Variable " + variableId + " have been successfully updated.");
		Dna.logger.log(l);
	}

	/**
	 * Get the {@link Exporter} object that contains the results.
	 *
	 * @return {@link Exporter} object with results.
	 */
	public Exporter getExporter() {
		return this.exporter;
	}

	@Override
	public void processLogEvents() {
		LogEvent l = Dna.logger.getRow(Dna.logger.getRowCount() - 1);
		if (l.getPriority() == 2 || l.getPriority() == 3) {
			l.print();
		}
	}
}