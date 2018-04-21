package dna.export;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import dna.SqlConnection;
import dna.dataStructures.AttributeVector;
import dna.dataStructures.Data;
import dna.dataStructures.Document;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementType;

public class ExporterR {

	// objects for R calls
	String dbfile;
	SqlConnection sql;
	Data data;
	ArrayList<Statement> filteredStatements;
	Matrix matrix;
	ArrayList<Matrix> timeWindowMatrices;
	ArrayList<Date> timeLabels;
	AttributeVector[] attributes;
	Object[] eventListColumnsR;
	String[] columnNames, columnTypes;
	ExportHelper exportHelper;

	/**
	 * Constructor for external R calls. Load and prepare data for export.
	 */
	public ExporterR(String dbtype, String dbfile, String login, String password, boolean verbose) {
		this.dbfile = dbfile;
		this.sql = new SqlConnection(dbtype, this.dbfile, login, password);
		this.data = sql.getAllData();
		this.filteredStatements = new ArrayList<Statement>();
		for (int i = 0; i < data.getStatements().size(); i++) {
			filteredStatements.add(data.getStatements().get(i));
		}
		if (verbose == true) {
			String statementString = " statements and ";
			if (this.data.getStatements().size() == 1) {
				statementString = " statement and ";
			}
			String documentString = " documents.";
			if (this.data.getDocuments().size() == 1) {
				documentString = " document.";
			}
			System.out.println("Data loaded: " + data.getStatements().size() + statementString + data.getDocuments().size() + documentString);
		}
		this.exportHelper = new ExportHelper();
	}
	
	/**
	 * A function for printing details about the dataset. Used by rDNA.
	 */
	public void rShow() {
		String statementString = " statements in ";
		if (this.data.getStatements().size() == 1) {
			statementString = " statement in ";
		}
		String documentString = " documents";
		if (this.data.getDocuments().size() == 1) {
			documentString = " document";
		}
		System.out.println("DNA database: " + this.dbfile);
		System.out.println(data.getStatements().size() + statementString + data.getDocuments().size() + documentString);
		System.out.print("Statement types: ");
		for (int i = 0; i < data.getStatementTypes().size(); i++) {
			System.out.print(data.getStatementTypes().get(i).getLabel());
			if (i < data.getStatementTypes().size() - 1) {
				System.out.print(", ");
			}
		}
		System.out.print("\n");
	}
	
	/**
	 * Compute one-mode or two-mode network matrix based on R arguments.
	 * 
	 * @param networkType            The network type as provided by rDNA (can be 'eventlist', 'twomode', or 'onemode')
	 * @param statementType          Statement type as a String
	 * @param variable1              First variable for export, provided as a String
	 * @param variable1Document      boolean indicating if the first variable is at the document level
	 * @param variable2              Second variable for export, provided as a String
	 * @param variable2Document      boolean indicating if the second variable is at the document level
	 * @param qualifier              Qualifier variable as a String
	 * @param qualifierAggregation   Aggregation rule for the qualifier variable (can be 'ignore', 'combine', 'subtract', 'congruence', or 'conflict')
	 * @param normalization          Normalization setting as a String, as provided by rDNA (can be 'no', 'activity', 'prominence', 'average', 'Jaccard', or 'cosine')
	 * @param includeIsolates        boolean indicating whether nodes not currently present should still be inserted into the network matrix
	 * @param duplicates             An input String from rDNA that can be 'include', 'document', 'week', 'month', 'year', or 'acrossrange'
	 * @param startDate              Start date for the export, provided as a String with format "dd.MM.yyyy"
	 * @param stopDate               Stop date for the export, provided as a String with format "dd.MM.yyyy"
	 * @param startTime              Start time for the export, provided as a String with format "HH:mm:ss"
	 * @param stopTime               Stop time for the export, provided as a String with format "HH:mm:ss"
	 * @param timewindow             A string indicating the time window setting. Valid options are 'no', 'events', 'seconds', 'minutes', 'hours', 'days', 'weeks', 'months', and 'years'.
	 * @param windowsize             An int indicating the duration of the time window in the units specified in the timeWindow argument.
	 * @param excludeVariables       A String array with n elements, indicating the variable of the n'th value
	 * @param excludeValues          A String array with n elements, indicating the value pertaining to the n'th variable String
	 * @param excludeAuthors         A String array of values to exclude in the 'author' variable at the document level
	 * @param excludeSources         A String array of values to exclude in the 'source' variable at the document level
	 * @param excludeSections        A String array of values to exclude in the 'section' variable at the document level
	 * @param excludeTypes           A String array of values to exclude in the 'type' variable at the document level
	 * @param invertValues           boolean indicating whether the statement-level exclude values should be included (= true) rather than excluded
	 * @param invertAuthors          boolean indicating whether the document-level author values should be included (= true) rather than excluded
	 * @param invertSources          boolean indicating whether the document-level source values should be included (= true) rather than excluded
	 * @param invertSections         boolean indicating whether the document-level section values should be included (= true) rather than excluded
	 * @param invertTypes            boolean indicating whether the document-level type values should be included (= true) rather than excluded
	 * @param verbose                Report progress to the console?
	 * @return                       A Matrix object containing the resulting one-mode or two-mode network
	 */
	public void rNetwork(String networkType, String statementType, String variable1, boolean variable1Document, String variable2, 
			boolean variable2Document, String qualifier, String qualifierAggregation, String normalization, boolean includeIsolates, 
			String duplicates, String startDate, String stopDate, String startTime, String stopTime, String timewindow, int windowsize, 
			String[] excludeVariables, String[] excludeValues, String[] excludeAuthors, String[] excludeSources, String[] excludeSections, 
			String[] excludeTypes, boolean invertValues, boolean invertAuthors, boolean invertSources, boolean invertSections, 
			boolean invertTypes, boolean verbose) {
		
		// step 1: preprocess arguments
		int max = 5;
		if (networkType.equals("eventlist")) {
			max = 4;
		}
		if (verbose == true) {
			System.out.print("(1/" + max + "): Processing network options... ");
		}
		networkType = formatNetworkType(networkType);
		StatementType st = processStatementType(networkType, statementType, variable1, variable2, qualifier, qualifierAggregation);
		boolean ignoreQualifier = qualifier.equals("ignore");
		int statementTypeId = st.getId();
		normalization = formatNormalization(networkType, normalization);
		duplicates = formatDuplicates(duplicates);
		
		Date start = formatDate(startDate, startTime);
		Date stop = formatDate(stopDate, stopTime);
		
		if (timewindow == null || timewindow.startsWith("no")) {
			timewindow = "no time window";
		}
		if (timewindow.equals("seconds")) {
			timewindow = "using seconds";
		}
		if (timewindow.equals("minutes")) {
			timewindow = "using minutes";
		}
		if (timewindow.equals("hours")) {
			timewindow = "using hours";
		}
		if (timewindow.equals("days")) {
			timewindow = "using days";
		}
		if (timewindow.equals("weeks")) {
			timewindow = "using weeks";
		}
		if (timewindow.equals("months")) {
			timewindow = "using months";
		}
		if (timewindow.equals("years")) {
			timewindow = "using years";
		}
		if (timewindow.equals("events")) {
			timewindow = "using events";
		}
		
		HashMap<String, ArrayList<String>> map = processExcludeVariables(excludeVariables, excludeValues, invertValues, data.getStatements(), 
				data.getStatements(), data.getDocuments(), statementTypeId, includeIsolates);
		ArrayList<String> authorExclude = processExcludeDocument("author", excludeAuthors, invertAuthors, data.getStatements(), data.getStatements(), 
				data.getDocuments(), statementTypeId, includeIsolates);
		ArrayList<String> sourceExclude = processExcludeDocument("source", excludeSources, invertSources, data.getStatements(), data.getStatements(),  
				data.getDocuments(), statementTypeId, includeIsolates);
		ArrayList<String> sectionExclude = processExcludeDocument("section", excludeSections, invertSections, data.getStatements(), data.getStatements(), 
				data.getDocuments(), statementTypeId, includeIsolates);
		ArrayList<String> typeExclude = processExcludeDocument("type", excludeTypes, invertTypes, data.getStatements(), data.getStatements(), 
				data.getDocuments(), statementTypeId, includeIsolates);

		if (verbose == true) {
			System.out.print("Done.\n");
		}
		
		// step 2: filter
		boolean filterEmptyFields = true;
		if (networkType.equals("Event list")) {
			filterEmptyFields = false;
		}
		if (verbose == true) {
			System.out.print("(2/" + max + "): Filtering statements...\n");
		}
		this.filteredStatements = exportHelper.filter(data.getStatements(), data.getDocuments(), start, stop, st, variable1, variable2, 
				variable1Document, variable2Document, qualifier, ignoreQualifier, duplicates, authorExclude, sourceExclude, sectionExclude, 
				typeExclude, map, filterEmptyFields, verbose);
		if (verbose == true) {
			System.out.print(this.filteredStatements.size() + " out of " + data.getStatements().size() + " statements retained.\n");
		}
		
		if (!timewindow.equals("no time window") && startDate.equals("01.01.1900") && startTime.equals("00:00:00")) {
			start = filteredStatements.get(0).getDate();
		}
		if (!timewindow.equals("no time window") && stopDate.equals("31.12.2099") && stopTime.equals("23:59:59")) {
			if (timewindow.equals("using events")) {
				stop = filteredStatements.get(filteredStatements.size() - 1).getDate();
			} else {
				GregorianCalendar stopTemp = new GregorianCalendar();
				stopTemp.setTime(start);
				GregorianCalendar lastDateTemp = new GregorianCalendar();
				lastDateTemp.setTime(filteredStatements.get(filteredStatements.size() - 1).getDate());
				while (stopTemp.before(lastDateTemp)) {
					if (timewindow.equals("using seconds")) {
						stopTemp.add(Calendar.SECOND, 1);
					}
					if (timewindow.equals("using minutes")) {
						stopTemp.add(Calendar.MINUTE, 1);
					}
					if (timewindow.equals("using hours")) {
						stopTemp.add(Calendar.HOUR, 1);
					}
					if (timewindow.equals("using days")) {
						stopTemp.add(Calendar.DAY_OF_MONTH, 1);
					}
					if (timewindow.equals("using weeks")) {
						stopTemp.add(Calendar.WEEK_OF_YEAR, 1);
					}
					if (timewindow.equals("using months")) {
						stopTemp.add(Calendar.MONTH, 1);
					}
					if (timewindow.equals("using years")) {
						stopTemp.add(Calendar.YEAR, 1);
					}
				}
				stop = stopTemp.getTime();
			}
			
		}
		
		// step 3: compile node labels
		String[] names1 = null;
		String[] names2 = null;
		if (!networkType.equals("Event list")) {
			if (verbose == true) {
				System.out.print("(3/" + max + "): Compiling node labels... ");
			}
			names1 = exportHelper.extractLabels(this.filteredStatements, data.getStatements(), data.getDocuments(), variable1, variable1Document, 
					statementTypeId, includeIsolates);
			names2 = exportHelper.extractLabels(this.filteredStatements, data.getStatements(), data.getDocuments(), variable2, variable2Document, 
					statementTypeId, includeIsolates);
			if (verbose == true) {
				System.out.print(names1.length + " entries for the first and " + names2.length + " entries for the second variable.\n");
			}
		}
		
		// step 4: create matrix
		if (verbose == true) {
			int step = 4;
			if (networkType.equals("Event list")) {
				step = 3;
			}
			System.out.print("(" + step + "/" + max + "): Computing network matrix... ");
		}
		Matrix m = null;
		timeWindowMatrices = null;
		if (networkType.equals("Two-mode network")) {
			if (timewindow.equals("no time window")) {
				m = exportHelper.computeTwoModeMatrix(filteredStatements, data.getDocuments(), st, variable1, variable2, variable1Document, 
						variable2Document, names1, names2, qualifier, qualifierAggregation, normalization, verbose);
			} else {
				this.timeWindowMatrices = exportHelper.computeTimeWindowMatrices(filteredStatements, data.getDocuments(), st, variable1, variable2, 
						variable1Document, variable2Document, names1, names2, qualifier, qualifierAggregation, normalization, true, start, stop, 
						timewindow, windowsize, includeIsolates);
			}
			this.matrix = m;
		} else if (networkType.equals("One-mode network")) {
			if (timewindow.equals("no time window")) {
				m = exportHelper.computeOneModeMatrix(filteredStatements, data.getDocuments(), st, variable1, variable2, variable1Document, 
						variable2Document, names1, names2, qualifier, qualifierAggregation, normalization);
			} else {
				this.timeWindowMatrices = exportHelper.computeTimeWindowMatrices(filteredStatements, data.getDocuments(), st, variable1, variable2, 
						variable1Document, variable2Document, names1, names2, qualifier, qualifierAggregation, normalization, false, start, stop, 
						timewindow, windowsize, includeIsolates);
			}
			this.matrix = m;
		} else if (networkType.equals("Event list")) {
			this.matrix = null;
			this.eventListColumnsR = eventListR(filteredStatements, data.getDocuments(), st);
		}
		if (verbose == true) {
			System.out.print("Done.\n");
			int step = 5;
			if (networkType.equals("Event list")) {
				step = 4;
			}
			System.out.println("(" + step + "/" + max + "): Retrieving results.");
		}
	}

	/**
	 * Format the R argument 'networkType' (can be 'eventlist', 'twomode', or 'onemode') to 'Event list', 'Two-mode network', or 'One-mode network'.
	 * 
	 * @param networkType   R argument
	 * @return              formatted string
	 */
	private String formatNetworkType(String networkType) {
		if (networkType.equals("eventlist")) {
			networkType = "Event list";
		} else if (networkType.equals("twomode")) {
			networkType = "Two-mode network";
		} else if (networkType.equals("onemode")) {
			networkType = "One-mode network";
		} else {
			System.err.println("Network type was not recognized. Use 'twomode', 'onemode', or 'eventlist'.");
		}
		return networkType;
	}

	/**
	 * Check if variables and statement type (provided as a String) are valid and return statement type.
	 * 
	 * @param networkType            Java-DNA-formatted network type String 
	 * @param statementType          Statement type given as a String
	 * @param variable1              First variable as a String
	 * @param variable2              Second variable as a String
	 * @param qualifier              Qualifier variable as a String
	 * @param qualifierAggregation   Qualifier aggregation rule as a String ('ignore', 'combine', 'subtract', 'congruence', or 'conflict')
	 * @return                       StatementType to be used
	 */
	private StatementType processStatementType(String networkType, String statementType, String variable1, String variable2, String qualifier, String qualifierAggregation) {
		StatementType st = data.getStatementType(statementType);
		if (st == null) {
			System.err.println("Statement type '" + statementType + " does not exist!");
		}
		
		if (!st.getVariables().containsKey(variable1)) {
			System.err.println("Variable 1 ('" + variable1 + "') does not exist in this statement type.");
		}
		if (!st.getVariables().get(variable1).equals("short text")) {
			System.err.println("Variable 1 ('" + variable1 + "') is not a short text variable.");
		}
		
		if (!st.getVariables().containsKey(variable2)) {
			System.err.println("Variable 2 ('" + variable2 + "') does not exist in this statement type.");
		}
		if (!st.getVariables().get(variable2).equals("short text")) {
			System.err.println("Variable 2 ('" + variable2 + "') is not a short text variable.");
		}
		
		if (!st.getVariables().containsKey(qualifier)) {
			System.err.println("The qualifier variable ('" + qualifier + "') does not exist in this statement type.");
		}
		if (!st.getVariables().get(qualifier).equals("boolean") && !st.getVariables().get(qualifier).equals("integer")) {
			System.err.println("The qualifier variable ('" + qualifier + "') is not a boolean or integer variable.");
		}
		
		if (!qualifierAggregation.equals("ignore") && !qualifierAggregation.equals("subtract") && !qualifierAggregation.equals("combine")
				&& !qualifierAggregation.equals("congruence") && !qualifierAggregation.equals("conflict")) {
			System.err.println("'qualifierAggregation' must be 'ignore', 'combine', 'subtract', 'congruence', or 'conflict'.");
		}
		if (qualifierAggregation.equals("combine") && !networkType.equals("Two-mode network")) {
			System.err.println("qualifierAggregation = 'combine' is only possible with two-mode networks.");
		}
		if (qualifierAggregation.equals("congruence") && !networkType.equals("One-mode network")) {
			System.err.println("qualifierAggregation = 'congruence' is only possible with one-mode networks.");
		}
		if (qualifierAggregation.equals("conflict") && !networkType.equals("One-mode network")) {
			System.err.println("qualifierAggregation = 'conflict' is only possible with one-mode networks.");
		}
		
		return st;
	}

	/**
	 * Format the normalization R argument.
	 * 
	 * @param networkType     Java-DNA-formatted network type String
	 * @param normalization   R argument String with the normalization type (can be 'no', 'activity', 'prominence', 'average', 'Jaccard', or 'cosine')
	 * @return                Formatted normalization String for DNA export (can be 'no', 'activity', 'prominence', 'average activity', 'Jaccard', or 'cosine')
	 */
	private String formatNormalization(String networkType, String normalization) {
		if (normalization.equals("jaccard")) {
			normalization = "Jaccard";
		}
		if (normalization.equals("Cosine")) {
			normalization = "cosine";
		}
		if (!normalization.equals("no") && !normalization.equals("activity") && !normalization.equals("prominence") 
				&& !normalization.equals("average") && !normalization.equals("Jaccard") && !normalization.equals("cosine")) {
			System.err.println("'normalization' must be 'no', 'activity', 'prominence', 'average', 'Jaccard', or 'cosine'.");
		}
		if (normalization.equals("activity") && !networkType.equals("Two-mode network")) {
			System.err.println("'normalization = 'activity' is only possible with two-mode networks.");
		}
		if (normalization.equals("prominence") && !networkType.equals("Two-mode network")) {
			System.err.println("'normalization = 'prominence' is only possible with two-mode networks.");
		}
		if (normalization.equals("average") && !networkType.equals("One-mode network")) {
			System.err.println("'normalization = 'average' is only possible with one-mode networks.");
		}
		if (normalization.equals("Jaccard") && !networkType.equals("One-mode network")) {
			System.err.println("'normalization = 'Jaccard' is only possible with one-mode networks.");
		}
		if (normalization.equals("cosine") && !networkType.equals("One-mode network")) {
			System.err.println("'normalization = 'cosine' is only possible with one-mode networks.");
		}
		if (normalization.equals("average")) {
			normalization = "average activity";
		}
		return normalization;
	}
	
	/**
	 * Format the duplicates R argument.
	 * 
	 * @param duplicates   An input String that can be 'include', 'document', 'week', 'month', 'year', or 'acrossrange'.
	 * @return             An output String that can be 'include all duplicates', 'ignore per document', 'ignore per calendar week', 'ignore per calendar month', 'ignore per calendar year', or 'ignore across date range'
	 */
	private String formatDuplicates(String duplicates) {
		if (!duplicates.equals("include") && !duplicates.equals("document") && !duplicates.equals("week") && !duplicates.equals("month") 
				&& !duplicates.equals("year") && !duplicates.equals("acrossrange")) {
			System.err.println("'duplicates' must be 'include', 'document', 'week', 'month', 'year', or 'acrossrange'.");
		}
		if (duplicates.equals("include")) {
			duplicates = "include all duplicates";
		} else if (duplicates.equals("document")) {
			duplicates = "ignore per document";
		} else if (duplicates.equals("week")) {
			duplicates = "ignore per calendar week";
		} else if (duplicates.equals("month")) {
			duplicates = "ignore per calendar month";
		} else if (duplicates.equals("year")) {
			duplicates = "ignore per calendar year";
		} else if (duplicates.equals("acrossrange")) {
			duplicates = "ignore across date range";
		}
		
		return duplicates;
	}
	
	/**
	 * Convert a date String of format "dd.MM.yyyy" and a time String of format "HH:mm:ss" to a Date object.
	 * 
	 * @param dateString    date String of format "dd.MM.yyyy"
	 * @param timeString    time String of format "HH:mm:ss"
	 * @return              Date object containing both the date and the time
	 */
	private Date formatDate(String dateString, String timeString) {
		String s = dateString + " " + timeString;
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		Date d = null;
		try {
			d = df.parse(s);
		} catch (ParseException e) {
			System.err.println("Date or time is invalid!");
		}
		if (!s.equals(df.format(d))) {
			d = null;
			System.err.println("Date or time is invalid!");
		}
		return d;
	}
	
	/**
	 * Convert a String array of variables and another one of values to be excluded from export into an array list, after considering the invert argument.
	 * 
	 * @param excludeVariables     A String array with n elements, indicating the variable of the n'th value
	 * @param excludeValues        A String array with n elements, indicating the value pertaining to the n'th variable String
	 * @param invertValues         boolean indicating whether the values should be included (= true) rather than excluded
	 * @param statements           ArrayList<String> of filtered statements
	 * @param originalStatements   Original ArrayList<String> of statements before applying the filter
	 * @param documents            ArrayList<Document> containing all documents in which the statements are embedded
	 * @param statementTypeId      int ID of the statement type to which the variables belong
	 * @param isolates             Should isolates be included in the network export?
	 * @return
	 */
	private HashMap<String, ArrayList<String>> processExcludeVariables(String[] excludeVariables, String[] excludeValues, 
			boolean invertValues, ArrayList<Statement> statements, ArrayList<Statement> originalStatements, 
			ArrayList<Document> documents, int statementTypeId, boolean isolates) {
		
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
		if (invertValues == true) {
			Iterator<String> mapIterator = map.keySet().iterator();
			while (mapIterator.hasNext()) {
				String key = mapIterator.next();
				ArrayList<String> values = map.get(key);
				String[] labels = exportHelper.extractLabels(statements, originalStatements, documents, key, false, statementTypeId, isolates);
				ArrayList<String> newValues = new ArrayList<String>();
				for (int i = 0; i < labels.length; i++) {
					if (!values.contains(labels[i])) {
						newValues.add(labels[i]);
					}
				}
				map.put(key, newValues);
			}
		}
		return map;
	}
	
	/**
	 * Process exclude arguments for a document-level variable for R export.
	 * 
	 * @param documentVariable     String denoting the document-level variable for which the excludeValues should be excluded
	 * @param excludeValues        Values to be excluded in the docuementVariable, provided as an array of Strings
	 * @param invert               boolean indicating whether the values should be included (= true) rather than excluded
	 * @param statements           ArrayList<String> of filtered statements
	 * @param originalStatements   Original ArrayList<String> of statements before applying the filter
	 * @param documents            ArrayList<Document> containing all documents in which the statements are embedded
	 * @param statementTypeId      int ID of the statement type to which the variables belong
	 * @param isolates             Should isolates be included in the network export?
	 * @return                     ArrayList<String> of values to be excluded
	 */
	private ArrayList<String> processExcludeDocument(String documentVariable, String[] excludeValues, boolean invert, ArrayList<Statement> statements, 
			ArrayList<Statement> originalStatements, ArrayList<Document> documents, int statementTypeId, boolean isolates) {
		
		ArrayList<String> excludeValuesList = new ArrayList<String>();
		if (excludeValues.length > 0) {
			for (int i = 0; i < excludeValues.length; i++) {
				excludeValuesList.add(excludeValues[i]);
			}
		}
		ArrayList<String> exclude = new ArrayList<String>();
		if (invert == false) {
			exclude.addAll(excludeValuesList);
		} else {
			String[] labels = exportHelper.extractLabels(statements, originalStatements, data.getDocuments(), documentVariable, true, statementTypeId, isolates);
			for (int i = 0; i < labels.length; i++) {
				if (!excludeValuesList.contains(labels[i])) {
					exclude.add(labels[i]);
				}
			}
		}
		
		return exclude;
	}
	
	/**
	 * This function accepts a list of statements that should be included in the relational event export, 
	 * and it returns the variables of all statements, along with the statement ID and a date/time stamp. 
	 * There is one statement per row, and the number of columns is the number of variables present in 
	 * the statement type plus 8 columns that represent statement ID and document-level variables.
	 * 
	 * @param statements	 An array list of {@link Statement}s (of the same statement type) that should be exported.
	 * @param documents      An array list of {@link Document}s in which the statements are embedded.
	 * @param statementType  The statement type corresponding to the statements.
	 */
	private Object[] eventListR(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType) {
		String key, value;
		Document doc;
		int statementTypeId = statementType.getId();
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getStatementTypeId() != statementTypeId) {
				throw new IllegalArgumentException("More than one statement type was selected. Cannot export to a spreadsheet!");
			}
		}

		// HashMap for fast lookup of document indices by ID
		HashMap<Integer, Integer> docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}
		
		// Get variable names and types of current statement type
		HashMap<String, String> variables = statementType.getVariables();
		Iterator<String> keyIterator;
		ArrayList<String> variableNames = new ArrayList<String>();
		ArrayList<String> variableTypes = new ArrayList<String>();
		keyIterator = variables.keySet().iterator();
		while (keyIterator.hasNext()){
			key = keyIterator.next();
			value = variables.get(key);
			variableNames.add(key);
			variableTypes.add(value);
		}
		columnNames = new String[variableNames.size()];
		columnTypes = new String[variableTypes.size()];
		for (int i = 0; i < variableNames.size(); i++) {
			columnNames[i] = variableNames.get(i);
			columnTypes[i] = variableTypes.get(i);
		}
		
		// create array of columns and populate document-level and statement-level columns; leave out variables for now
		Object[] columns = new Object[variableNames.size() + 8];
		int[] ids = new int[statements.size()];
		long[] time = new long[statements.size()];
		int[] docId = new int[statements.size()];
		String[] docTitle = new String[statements.size()];
		String[] author = new String[statements.size()];
		String[] source = new String[statements.size()];
		String[] section = new String[statements.size()];
		String[] type = new String[statements.size()];
		for (int i = 0; i < statements.size(); i++) {
			ids[i] = statements.get(i).getId();
			time[i] = statements.get(i).getDate().getTime() / 1000;  // convert milliseconds to seconds (since 1/1/1970)
			docId[i] = statements.get(i).getDocumentId();
			doc = documents.get(docMap.get(docId[i]));
			docTitle[i] = doc.getTitle();
			author[i] = doc.getAuthor();
			source[i] = doc.getSource();
			section[i] = doc.getSection();
			type[i] = doc.getType();
		}
		columns[0] = ids;
		columns[1] = time;
		columns[2] = docId;
		columns[3] = docTitle;
		columns[4] = author;
		columns[5] = source;
		columns[6] = section;
		columns[7] = type;
		
		// Now add the variables to the columns array
		for (int i = 0; i < variableNames.size(); i++) {
			if (columnTypes[i].equals("short text") || columnTypes[i].equals("long text")) {
				columns[i + 8] = new String[statements.size()];
			} else {
				columns[i + 8] = new int[statements.size()];
			}
		}
		for (int i = 0; i < statements.size(); i++) {
			for (int j = 0; j < variableNames.size(); j++) {
				if (columnTypes[j].equals("short text") || columnTypes[j].equals("long text")) {
					String[] temp = ((String[]) columns[j + 8]);
					temp[i] = (String) statements.get(i).getValues().get(columnNames[j]);
					columns[j + 8] = temp;
				} else {
					int[] temp = ((int[]) columns[j + 8]);
					temp[i] = (int) statements.get(i).getValues().get(columnNames[j]);
					columns[j + 8] = temp;
				}
			}
		}
		
		return columns;
	}
	
	/**
	 * Save an array of AttributeVector objects to the Exporter class.
	 * 
	 * @param variable              The variable for which the attributes should be retrieved.
	 * @param statementTypeString   The statement type (given as a string) to which the variable belongs.
	 * @param values                String array of value names for which the attributes should be saved.
	 */
	public void rAttributes(String variable, String statementTypeString, String[] values) {
		
		// get statement type ID and all attribute vectors
		int statementTypeId = this.data.getStatementType(statementTypeString).getId();
		AttributeVector[] av = this.data.getAttributes(variable, statementTypeId);
		
		// extract full set of labels in alphabetical order if no names vector is provided
		if (values == null || values.length == 0) {
			values = exportHelper.extractLabels(this.filteredStatements, data.getStatements(), data.getDocuments(), variable, false, statementTypeId, true);
		}
		
		// extract only those attribute vectors that match the names vector
		AttributeVector[] at = new AttributeVector[values.length];
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < av.length; j++) {
				if (values[i].equals(av[j].getValue())) {
					at[i] = av[j];
				}
			}
		}
		
		this.attributes = at;
	}

	/**
	 * Get attribute IDs
	 * 
	 * @return integer array of attribute IDs
	 */
	public int[] getAttributeIds() {
		int[] ids = new int[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			ids[i] = this.attributes[i].getId();
		}
		return ids;
	}
	
	/**
	 * Get attribute values
	 * 
	 * @return String array of attribute values
	 */
	public String[] getAttributeValues() {
		String[] values = new String[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			values[i] = this.attributes[i].getValue();
		}
		return values;
	}
	
	/**
	 * Get attribute colors
	 * 
	 * @return String array of attribute colors
	 */
	public String[] getAttributeColors() {
		String[] colors = new String[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			colors[i] = String.format("#%02X%02X%02X", this.attributes[i].getColor().getRed(), 
					this.attributes[i].getColor().getGreen(), this.attributes[i].getColor().getBlue());
		}
		return colors;
	}
	
	/**
	 * Get attribute types
	 * 
	 * @return String array of attribute types
	 */
	public String[] getAttributeTypes() {
		String[] types = new String[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			types[i] = this.attributes[i].getType();
		}
		return types;
	}
	
	/**
	 * Get attribute aliases
	 * 
	 * @return String array of attribute aliases
	 */
	public String[] getAttributeAlias() {
		String[] alias = new String[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			alias[i] = this.attributes[i].getAlias();
		}
		return alias;
	}
	
	/**
	 * Get attribute notes
	 * 
	 * @return   String array of attribute notes
	 */
	public String[] getAttributeNotes() {
		String[] notes = new String[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			notes[i] = this.attributes[i].getNotes();
		}
		return notes;
	}
	
	/**
	 * Get boolean array indicating if each value is in the dataset
	 * 
	 * @return boolean array indicating if each attribute value is currently in the dataset
	 */
	public boolean[] getAttributeInDataset(String statementTypeString) {
		boolean[] inDataset = new boolean[attributes.length];
		int statementTypeId = this.data.getStatementType(statementTypeString).getId();
		boolean current = false;
		for (int i = 0; i < this.attributes.length; i++) {
			String variable = this.attributes[i].getVariable();
			String value = this.attributes[i].getValue();
			current = false;
			for (int j = 0; j < this.data.getStatements().size(); j++) {
				if (this.data.getStatements().get(j).getStatementTypeId() == statementTypeId 
						&& this.data.getStatements().get(j).getValues().get(variable).equals(value) ) {
					current = true;
					break;
				}
			}
			inDataset[i] = current;
		}
		return inDataset;
	}

	/**
	 * Get boolean array indicating if each value is in the exported network
	 * 
	 * @return boolean array indicating if each attribute value is currently in the network
	 */
	public boolean[] getAttributeInNetwork(String statementTypeString) {
		boolean[] inNetwork = new boolean[attributes.length];
		int statementTypeId = this.data.getStatementType(statementTypeString).getId();
		boolean current = false;
		for (int i = 0; i < this.attributes.length; i++) {
			String variable = this.attributes[i].getVariable();
			String value = this.attributes[i].getValue();
			current = false;
			for (int j = 0; j < this.filteredStatements.size(); j++) {
				if (this.filteredStatements.get(j).getStatementTypeId() == statementTypeId 
						&& this.filteredStatements.get(j).getValues().get(variable).equals(value) ) {
					current = true;
					break;
				}
			}
			inNetwork[i] = current;
		}
		return inNetwork;
	}
	
	/**
	 * Get attribute value frequencies
	 * 
	 * @return   integer array of frequencies (i.e., how often was a node active in the list of filtered statements?)
	 */
	public int[] getAttributeFrequencies(String variable) {
		String[] values = exportHelper.retrieveValues(this.filteredStatements, data.getDocuments(), variable, false);
		int[] frequencies = exportHelper.countFrequencies(values, this.getAttributeValues());
		return frequencies;
	}

	/**
	 * Return variable names in this.eventListColumnsR
	 * 
	 * @return   array of Strings with variable names
	 */
	public String[] getEventListColumnsRNames() {
		return columnNames;
	}

	/**
	 * Return variable types in this.eventListColumnsR
	 * 
	 * @return   array of Strings with variable types
	 */
	public String[] getEventListColumnsRTypes() {
		return columnTypes;
	}
	
	/**
	 * Return Object[] from this.eventListColumnsR
	 * 
	 * @return   array of array of different data types, which represent the columns
	 */
	public Object[] getEventListColumnsR() {
		return eventListColumnsR;
	}
	
	/**
	 * Return double[][] from this.matrix.
	 * 
	 * @return   network matrix
	 */
	public double[][] getMatrix() {
		return matrix.getMatrix();
	}
	
	/**
	 * Return row names from this.matrix.
	 * 
	 * @return   String array of node names for the row variable.
	 */
	public String[] getRowNames() {
		return matrix.getRownames();
	}
	
	/**
	 * Return column names from this.matrix.
	 * 
	 * @return   String array of node names for the column variable.
	 */
	public String[] getColumnNames() {
		return matrix.getColnames();
	}
	
	/**
	 * Return a single matrix in this.timeWindowMatrices.
	 * 
	 * @return   double[][] matrix
	 */
	public double[][] getTimeWindowNetwork(int t) {
		return this.timeWindowMatrices.get(t).getMatrix();
	}

	/**
	 * Return the row names of a single matrix in this.timeWindowMatrices.
	 * 
	 * @return   String[] row names
	 */
	public String[] getTimeWindowRowNames(int t) {
		return timeWindowMatrices.get(t).getRownames();
	}

	/**
	 * Return the column names of a single matrix in this.timeWindowMatrices.
	 * 
	 * @return   String[] column names
	 */
	public String[] getTimeWindowColumnNames(int t) {
		return timeWindowMatrices.get(t).getColnames();
	}
	
	/**
	 * Return time labels corresponding to a time window sequence.
	 * 
	 * @return   array of Unix times as seconds since 1/1/1970
	 */
	public long[] getTimeWindowTimes() {
		long[] times = new long[timeLabels.size()];
		for (int i = 0; i < timeLabels.size(); i++) {
			times[i] = (long) (timeLabels.get(i).getTime() / 1000);
		}
		return times;
	}

	/**
	 * Retrieve an object array of all document data for R.
	 * 
	 * @return Object array with further arrays for title, text etc.
	 */
	public Object[] getDocuments() {
		int n = this.data.getDocuments().size();
		
		int[] id = new int[n];
		String[] title = new String[n];
		String[] text = new String[n];
		int[] coder = new int[n];
		String[] author = new String[n];
		String[] source = new String[n];
		String[] section = new String[n];
		String[] notes = new String[n];
		String[] type = new String[n];
		long[] date = new long[n];
		
		for (int i = 0; i < this.data.getDocuments().size(); i++) {
			id[i] = this.data.getDocuments().get(i).getId();
			title[i] = this.data.getDocuments().get(i).getTitle();
			text[i] = this.data.getDocuments().get(i).getText();
			coder[i] = this.data.getDocuments().get(i).getCoder();
			author[i] = this.data.getDocuments().get(i).getAuthor();
			source[i] = this.data.getDocuments().get(i).getSource();
			section[i] = this.data.getDocuments().get(i).getSection();
			notes[i] = this.data.getDocuments().get(i).getNotes();
			type[i] = this.data.getDocuments().get(i).getType();
			date[i] = this.data.getDocuments().get(i).getDate().getTime();
		}
		
		Object[] documents = new Object[10];
		documents[0] = id;
		documents[1] = title;
		documents[2] = text;
		documents[3] = coder;
		documents[4] = author;
		documents[5] = source;
		documents[6] = section;
		documents[7] = notes;
		documents[8] = type;
		documents[9] = date;
		
		return documents;
	}
	
	/**
	 * Update the list of documents based on an array of arrays for the document data.
	 * 
	 * @param documents            Array of objects containing document IDs, title, text, coder, author, source, section, notes, type, and date.
	 * @param removeStatements     Delete statements contained in documents that are removed?
	 * @param simulate             If true, changes are not actually carried out.
	 * @param verbose              Should statistics on updating process be reported?
	 */
	public void setDocuments(Object[] documents, boolean removeStatements, boolean simulate, boolean verbose) {
		int[] id = (int[]) documents[0];
		String[] title = (String[]) documents[1];
		String[] text = (String[]) documents[2];
		int[] coder = (int[]) documents[3];
		String[] author = (String[]) documents[4];
		String[] source = (String[]) documents[5];
		String[] section = (String[]) documents[6];
		String[] notes = (String[]) documents[7];
		String[] type = (String[]) documents[8];
		long[] date = (long[]) documents[9];
		
		int updateCountTitle = 0;
		int updateCountText = 0;
		int updateCountCoder = 0;
		int updateCountAuthor = 0;
		int updateCountSource = 0;
		int updateCountSection = 0;
		int updateCountNotes = 0;
		int updateCountType = 0;
		int updateCountDate = 0;
		int updateCountNewDocuments = 0;
		int updateCountDeleted = 0;
		int updateCountStatementsDeleted = 0;
		
		if (verbose == true) {
			if (simulate == true) {
				System.out.println("Simulation mode: no actual changes are made to the database!");
			} else {
				System.out.println("Changes will be written both in memory and to the SQL database!");
			}
		}
		
		// delete documents that are not in the array
		if (this.data.getDocuments().size() > 0) {
			for (int j = this.data.getDocuments().size() - 1; j > -1; j--) {
				boolean delete = true;
				for (int i = 0; i < id.length; i++) {
					if (this.data.getDocuments().get(j).getId() == id[i]) {
						delete = false;
					}
				}
				if (delete == true) {
					boolean containsStatements = false;
					if (!this.data.getStatements().isEmpty()) {
						for (int k = this.data.getStatements().size() - 1; k > -1; k--) {
							if (this.data.getStatements().get(k).getDocumentId() == this.data.getDocuments().get(j).getId()) {
								if (removeStatements == true) {
									int statementId = this.data.getStatements().get(k).getId();
									if (simulate == false) {
										this.data.getStatements().remove(k);
										this.sql.removeStatement(statementId);
									}
									updateCountStatementsDeleted++;
								} else {
									containsStatements = true;
								}
							}
						}
						if (!containsStatements) {
							if (simulate == false) {
								this.data.getDocuments().remove(j);
								this.sql.removeDocument(this.data.getDocuments().get(j).getId());
							}
							updateCountDeleted++;
						} else {
							System.err.println("Document " + this.data.getDocuments().get(j).getId() + " contains statements and was not removed.");
						}
					}
				}
			}
		}
		
		// add or update documents
		for (int i = 0; i < id.length; i++) {
			boolean update = true;
			
			// check if coder field is valid
			if (this.data.getCoderById(coder[i]) == null) {
				update = false;
				System.err.println("Document ID " + id[i] + ": coder ID is invalid. Skipping this document.");
			}
			
			// check if document ID exists
			int foundIndex = -1;
			for (int j = 0; j < this.data.getDocuments().size(); j++) {
				if (id[i] == this.data.getDocuments().get(j).getId()) {
					foundIndex = j;
				}
			}
			
			// check if document length is shorter than last statement in the document
			int minLength = 0;
			if (foundIndex > -1) {
				for (int k = 0; k < this.data.getStatements().size(); k++) {
					if (this.data.getStatements().get(k).getStop() > minLength && this.data.getStatements().get(k).getDocumentId() == id[i]) {
						minLength = this.data.getStatements().get(k).getStop();
					}
				}
			}
			if (text[i].length() < minLength) {
				update = false;
				System.err.println("Document ID " + id[i] + ": text is not long enough to accommodate existing statements. Skipping this document.");
			}
			
			if (foundIndex > -1 && update == true) {
				if (!this.data.getDocuments().get(foundIndex).getTitle().equals(title[i])) {
					if (simulate == false) {
						this.data.getDocuments().get(foundIndex).setTitle(title[i]);
					}
					updateCountTitle++;
				}
				if (!this.data.getDocuments().get(foundIndex).getText().equals(text[i])) {
					if (simulate == false) {
						this.data.getDocuments().get(foundIndex).setText(text[i]);
					}
					updateCountText++;
				}
				if (this.data.getDocuments().get(foundIndex).getCoder() != coder[i]) {
					if (simulate == false) {
						this.data.getDocuments().get(foundIndex).setCoder(coder[i]);
					}
					updateCountCoder++;
				}
				if (!this.data.getDocuments().get(foundIndex).getAuthor().equals(author[i])) {
					if (simulate == false) {
						this.data.getDocuments().get(foundIndex).setTitle(author[i]);
					}
					updateCountAuthor++;
				}
				if (!this.data.getDocuments().get(foundIndex).getSource().equals(source[i])) {
					if (simulate == false) {
						this.data.getDocuments().get(foundIndex).setSource(source[i]);
					}
					updateCountSource++;
				}
				if (!this.data.getDocuments().get(foundIndex).getSection().equals(section[i])) {
					if (simulate == false) {
						this.data.getDocuments().get(foundIndex).setSection(section[i]);
					}
					updateCountSection++;
				}
				if (!this.data.getDocuments().get(foundIndex).getNotes().equals(notes[i])) {
					if (simulate == false) {
						this.data.getDocuments().get(foundIndex).setNotes(notes[i]);
					}
					updateCountNotes++;
				}
				if (!this.data.getDocuments().get(foundIndex).getType().equals(type[i])) {
					if (simulate == false) {
						this.data.getDocuments().get(foundIndex).setType(type[i]);
					}
					updateCountType++;
				}
				if (this.data.getDocuments().get(foundIndex).getDate().getTime() != date[i]) {
					if (simulate == false) {
						this.data.getDocuments().get(foundIndex).setDate(new Date(date[i]));
					}
					updateCountDate++;
				}
				if (simulate == false) {
					this.sql.upsertDocument(this.data.getDocuments().get(foundIndex));
				}
			} else if (update == true) {
				Document d = new Document(id[i], title[i], text[i], coder[i], author[i], source[i], section[i], notes[i], type[i], new Date(date[i]));
				if (simulate == false) {
					this.data.addDocument(d);
					this.sql.upsertDocument(d);
				}
				updateCountNewDocuments++;
			}
		}
		
		// report statistics
		if (verbose == true) {
			System.out.println("New documents added: " + updateCountNewDocuments);
			System.out.println("Deleted documents:   " + updateCountDeleted);
			System.out.println("Deleted statements:  " + updateCountStatementsDeleted);
			System.out.println("Titles updated:      " + updateCountTitle);
			System.out.println("Texts updated:       " + updateCountText);
			System.out.println("Coders updated:      " + updateCountCoder);
			System.out.println("Authors updated:     " + updateCountAuthor);
			System.out.println("Sources updated:     " + updateCountSource);
			System.out.println("Sections updated:    " + updateCountSection);
			System.out.println("Notes updated:       " + updateCountNotes);
			System.out.println("Types updated:       " + updateCountType);
			System.out.println("Dates updated:       " + updateCountDate);
		}
	}
	
	/**
	 * Add a document both to memory and the SQL database.
	 * 
	 * @param title     Title of the document.
	 * @param text      Text of the document.
	 * @param coder     Coder ID of the document.
	 * @param author    Author of the document.
	 * @param source    Source of the document.
	 * @param section   Section of the document.
	 * @param notes     Notes for the document.
	 * @param type      Type of the document.
	 * @param date      Date of the document in milliseconds since 1970-01-01.
	 * @return          New document ID.
	 */
	public int addDocument(String title, String text, int coder, String author, String source, String section, String notes, String type, long date) {
		if (this.data.getCoderById(coder) == null) {
			System.err.println("Document could not be added because coder ID is unknown.");
			return -1;
		}
		int id = this.data.generateNewId("documents");
		Date dateObject = new Date(date);
		Document d = new Document(id, title, text, coder, author, source, section, notes, type, dateObject);
		this.data.addDocument(d);
		this.sql.upsertDocument(d);
		return id;
	}
	
	/**
	 * Remove a document based on its ID.
	 * 
	 * @param id        ID of the document.
	 * @param verbose   Should messages be printed?
	 */
	public void removeDocument(int id, boolean verbose) {
		boolean success = false;
		for (int i = this.data.getDocuments().size() - 1; i > -1 ; i--) {
			if (this.data.getDocuments().get(i).getId() == id) {
				this.data.getDocuments().remove(i);
				success = true;
			}
		}
		if (success) {
			this.sql.removeDocument(id);
		}
		if (verbose) {
			if (success) {
				System.out.println("Document " + id + " was successfully removed.");
			} else {
				System.err.println("A document with ID " + id + " was not found.");
			}
		}
	}
}