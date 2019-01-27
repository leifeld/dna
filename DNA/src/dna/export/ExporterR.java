package dna.export;

import java.io.PrintStream;
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
import java.util.LinkedHashMap;

import org.rosuda.JRI.RConsoleOutputStream;
import org.rosuda.JRI.Rengine;

import dna.SqlConnection;
import dna.dataStructures.AttributeVector;
import dna.dataStructures.Data;
import dna.dataStructures.Document;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementType;

public class ExporterR {

	/* =================================================================================================================
	 * Object definitions; constructor; and helper functions
	 * =================================================================================================================
	 */

	// objects for R calls
	String dbfile;
	SqlConnection sql;
	Data data;
	ArrayList<Statement> filteredStatements;
	Matrix matrix;
	ArrayList<Matrix> timeWindowMatrices;
	AttributeVector[] attributes;
	Object[] eventListColumnsR;
	String[] columnNames, columnTypes;
	ExportHelper exportHelper;

	/**
	 * Constructor for external R calls. Load and prepare data for export.
	 */
	public ExporterR(String dbtype, String dbfile, String login, String password, boolean verbose) {
		
		// divert stdout to R console
		Rengine r = new Rengine();
		RConsoleOutputStream rs = new RConsoleOutputStream(r, 0);
		System.setOut(new PrintStream(rs));
		
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
	 * 
	 * @return   A string with details about the database.
	 */
	public String rShow() {
		String statementString = " statements in ";
		if (this.data.getStatements().size() == 1) {
			statementString = " statement in ";
		}
		String documentString = " documents";
		if (this.data.getDocuments().size() == 1) {
			documentString = " document";
		}
		String s = "DNA database: " + this.dbfile + "\n" + data.getStatements().size() + statementString + data.getDocuments().size() + documentString + "\nStatement types: ";
		for (int i = 0; i < data.getStatementTypes().size(); i++) {
			s = s + data.getStatementTypes().get(i).getLabel();
			if (i < data.getStatementTypes().size() - 1) {
				s = s + ", ";
			}
		}
		return s;
	}
	
	
	/* =================================================================================================================
	 * Functions for generating and retrieving networks
	 * =================================================================================================================
	 */
	
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
		
		// format network type
		// valid R input: 'eventlist', 'twomode', or 'onemode'
		// valid Java output: 'Event list', 'Two-mode network', or 'One-mode network'
		if (networkType.equals("eventlist")) {
			networkType = "Event list";
		} else if (networkType.equals("twomode")) {
			networkType = "Two-mode network";
		} else if (networkType.equals("onemode")) {
			networkType = "One-mode network";
		} else {
			System.err.println("Network type was not recognized. Use 'twomode', 'onemode', or 'eventlist'.");
		}
		
		// process and check validity of statement type etc.
		// valid R input for qualifierAggregation: 'ignore', 'combine', 'subtract', 'congruence', or 'conflict'
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
		
		boolean ignoreQualifier = qualifier.equals("ignore");
		int statementTypeId = st.getId();
		
		// format normalization argument
		// R input can be: 'no', 'activity', 'prominence', 'average', 'Jaccard', or 'cosine'
		// formatted Java output can be: 'no', 'activity', 'prominence', 'average activity', 'Jaccard', or 'cosine'
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
		
		// format duplicates argument
		// valid R input: 'include', 'document', 'week', 'month', 'year', or 'acrossrange'
		// valid Java output: 'include all duplicates', 'ignore per document', 'ignore per calendar week', 'ignore per calendar month', 'ignore per calendar year', or 'ignore across date range'
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

		// format dates and times with input formats "dd.MM.yyyy" and "HH:mm:ss"
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		String startString = startDate + " " + startTime;
		Date start = null;
		try {
			start = df.parse(startString);
		} catch (ParseException e) {
			System.err.println("Start date or time is invalid!");
		}
		if (!startString.equals(df.format(start))) {
			startDate = null;
			System.err.println("Start date or time is invalid!");
		}
		String stopString = stopDate + " " + stopTime;
		Date stop = null;
		try {
			stop = df.parse(stopString);
		} catch (ParseException e) {
			System.err.println("Stop date or time is invalid!");
		}
		if (!stopString.equals(df.format(stop))) {
			stopDate = null;
			System.err.println("Stop date or time is invalid!");
		}
		
		// format time window arguments
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
		if (invertValues == true) {
			Iterator<String> mapIterator = map.keySet().iterator();
			while (mapIterator.hasNext()) {
				String key = mapIterator.next();
				ArrayList<String> values = map.get(key);
				String[] labels = exportHelper.extractLabels(data.getStatements(), data.getStatements(), data.getDocuments(), key, false, statementTypeId, includeIsolates);
				ArrayList<String> newValues = new ArrayList<String>();
				for (int i = 0; i < labels.length; i++) {
					if (!values.contains(labels[i])) {
						newValues.add(labels[i]);
					}
				}
				map.put(key, newValues);
			}
		}
		
		// process document-level exclude variables using repeated calls of private function 'processExcludeDocument'
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
			// convert list of filtered statements into event list in the form of an Object[], including all statement- and document-level variables
			this.matrix = null;
		    String key, value;
			Document doc;
			for (int i = 0; i < filteredStatements.size(); i++) {
				if (filteredStatements.get(i).getStatementTypeId() != statementTypeId) {
					throw new IllegalArgumentException("More than one statement type was selected. Cannot export to a spreadsheet!");
				}
			}

			// HashMap for fast lookup of document indices by ID
			HashMap<Integer, Integer> docMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < data.getDocuments().size(); i++) {
				docMap.put(data.getDocuments().get(i).getId(), i);
			}
			
			// Get variable names and types of current statement type
			HashMap<String, String> variables = st.getVariables();
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
			eventListColumnsR = new Object[variableNames.size() + 8];
			int[] ids = new int[filteredStatements.size()];
			long[] time = new long[filteredStatements.size()];
			int[] docId = new int[filteredStatements.size()];
			String[] docTitle = new String[filteredStatements.size()];
			String[] author = new String[filteredStatements.size()];
			String[] source = new String[filteredStatements.size()];
			String[] section = new String[filteredStatements.size()];
			String[] type = new String[filteredStatements.size()];
			for (int i = 0; i < filteredStatements.size(); i++) {
				ids[i] = filteredStatements.get(i).getId();
				time[i] = filteredStatements.get(i).getDate().getTime() / 1000;  // convert milliseconds to seconds (since 1/1/1970)
				docId[i] = filteredStatements.get(i).getDocumentId();
				doc = data.getDocuments().get(docMap.get(docId[i]));
				docTitle[i] = doc.getTitle();
				author[i] = doc.getAuthor();
				source[i] = doc.getSource();
				section[i] = doc.getSection();
				type[i] = doc.getType();
			}
			eventListColumnsR[0] = ids;
			eventListColumnsR[1] = time;
			eventListColumnsR[2] = docId;
			eventListColumnsR[3] = docTitle;
			eventListColumnsR[4] = author;
			eventListColumnsR[5] = source;
			eventListColumnsR[6] = section;
			eventListColumnsR[7] = type;
			
			// Now add the variables to the columns array
			for (int i = 0; i < variableNames.size(); i++) {
				if (columnTypes[i].equals("short text") || columnTypes[i].equals("long text")) {
					eventListColumnsR[i + 8] = new String[filteredStatements.size()];
				} else {
					eventListColumnsR[i + 8] = new int[filteredStatements.size()];
				}
			}
			for (int i = 0; i < filteredStatements.size(); i++) {
				for (int j = 0; j < variableNames.size(); j++) {
					if (columnTypes[j].equals("short text") || columnTypes[j].equals("long text")) {
						String[] temp = ((String[]) eventListColumnsR[j + 8]);
						temp[i] = (String) filteredStatements.get(i).getValues().get(columnNames[j]);
						eventListColumnsR[j + 8] = temp;
					} else {
						int[] temp = ((int[]) eventListColumnsR[j + 8]);
						temp[i] = (int) filteredStatements.get(i).getValues().get(columnNames[j]);
						eventListColumnsR[j + 8] = temp;
					}
				}
			}
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
	 * @throws Exception 
	 */
	public long[] getTimeWindowTimes() throws Exception {
		long[] times = new long[timeWindowMatrices.size()];
		if (times.length > 0) {
			for (int i = 0; i < timeWindowMatrices.size(); i++) {
				times[i] = (long) (timeWindowMatrices.get(i).getDate().getTime() / 1000);
			}
		} else {
			throw new Exception("Not a single network matrix has been generated. Does the time window size exceed the time range?");
		}
		return times;
	}

	/**
	 * Return numbers of statements corresponding to a time window sequence.
	 * 
	 * @return   array of integers representing how many statements a time window network is composed of
	 * @throws Exception 
	 */
	public int[] getTimeWindowNumStatements() throws Exception {
		int[] numStatements = new int[timeWindowMatrices.size()];
		if (numStatements.length > 0) {
			for (int i = 0; i < timeWindowMatrices.size(); i++) {
				numStatements[i] = timeWindowMatrices.get(i).getNumStatements();
			}
		} else {
			throw new Exception("Not a single network matrix has been generated. Does the time window size exceed the time range?");
		}
		return numStatements;
	}

	
	/* =================================================================================================================
	 * Functions for managing attributes
	 * =================================================================================================================
	 */
	
	/**
	 * Retrieve attributes for a specific variable defined in a statement type (here: definition for String statement type label).
	 * 
	 * @param statementTypeLabel  Name of the statement type in which the variable is defined. Make sure there are no duplicate names!
	 * @param variable            Variable name for which the values and attributes should be retrieved.
	 * @param values              An array of values to which the attributes should be limited. Useful for handing over the nodes of a network, for example.
	 * @return                    Object array containing arrays with the ID, value, color, type, alias, notes, and frequency of each entry.
	 */
	public Object[] getAttributes(String statementTypeLabel, String variable, String[] values) {
		StatementType st = this.data.getStatementType(statementTypeLabel);
		int id = -1;
		if (st != null) {
			id = st.getId();
		}
		return getAttributes(id, variable, values);
	}

	/**
	 * Retrieve attributes for a specific variable defined in a statement type (here: definition for int statement type ID).
	 * 
	 * @param statementTypeId     ID of the statement type in which the variable is defined.
	 * @param variable            Variable name for which the values and attributes should be retrieved.
	 * @param values              An array of values to which the attributes should be limited. Useful for handing over the nodes of a network, for example.
	 * @return                    Object array containing arrays with the ID, value, color, type, alias, notes, and frequency of each entry.
	 */
	public Object[] getAttributes(int statementTypeId, String variable, String[] values) {
		
		if (statementTypeId == -1) {
			Object[] o = new Object[7];
			String[] value = new String[0];
			int[] id = new int[0];
			String[] color = new String[0];
			String[] type = new String[0];
			String[] alias = new String[0];
			String[] notes = new String[0];
			int[] frequency = new int[0];
			o[0] = id;
			o[1] = value;
			o[2] = color;
			o[3] = type;
			o[4] = alias;
			o[5] = notes;
			o[6] = frequency;
			System.err.println("Statement type was not found!");
			return o;
		}
		
		AttributeVector[] av = this.data.getAttributes(variable, statementTypeId);
		
		// create master list of values to be included (check if present in the file)
		ArrayList<String> valuesList = new ArrayList<String>();
		if (values.length < 1) {
			for (int j = 0; j < av.length; j++) {
				if (!av[j].getValue().equals("")) {
					valuesList.add(av[j].getValue());
				}
			}
		} else {
			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < av.length; j++) {
					if (values[i].equals(av[j].getValue()) && !valuesList.contains(values[i]) && !values[i].equals("")) {
						valuesList.add(values[i]);
					}
				}
				if (!valuesList.contains(values[i])) {
					System.err.println("Value '" + values[i] + "' is not contained in the database and will be ignored.");
				}
			}
		}
		Collections.sort(valuesList);
		
		// create arrays of values and other attribute metadata
		String[] value = new String[valuesList.size()];
		int[] id = new int[valuesList.size()];
		String[] color = new String[valuesList.size()];
		String[] type = new String[valuesList.size()];
		String[] alias = new String[valuesList.size()];
		String[] notes = new String[valuesList.size()];
		int[] frequency = new int[valuesList.size()];
		
		// populate arrays
		for (int i = 0; i < valuesList.size(); i++) {
			for (int j = 0; j < av.length; j++) {
				if (av[j].getValue().equals(valuesList.get(i))) {
					value[i] = av[j].getValue();
					id[i] = av[j].getId();
					color[i] = String.format("#%02X%02X%02X", av[j].getColor().getRed(), av[j].getColor().getGreen(), av[j].getColor().getBlue());
					type[i] = av[j].getType();
					alias[i] = av[j].getAlias();
					notes[i] = av[j].getNotes();
				}
			}
			
			// compute frequencies of each value
			frequency[i] = 0;
			for (int k = 0; k < this.data.getStatements().size(); k++) {
				if (this.data.getStatements().get(k).getStatementTypeId() == statementTypeId) {
					if (((String) this.data.getStatements().get(k).getValues().get(variable)).equals(value[i])) {
						frequency[i] = frequency[i] + 1;
					}
				}
			}
		}
		
		// create object array and return results
		Object[] attributes = new Object[7];
		attributes[0] = id;
		attributes[1] = value;
		attributes[2] = color;
		attributes[3] = type;
		attributes[4] = alias;
		attributes[5] = notes;
		attributes[6] = frequency;
		return attributes;
	}
	
	/**
	 * Update the list of attributes based on an array of arrays for the attribute data.
	 * 
	 * @param statementTypeLabel  Statement type label for which the attributes will be updated (as a String).
	 * @param variable            Variable name for which the attributes will be updated.
	 * @param attributes          Array of objects containing attribute ID, value, color, type, alias, and notes.
	 * @param removeStatements    Delete statements containing attribute values that are removed?
	 * @param simulate            If true, changes are not actually carried out.
	 * @param verbose             Should statistics on updating process be reported?
	 * @throws Exception
	 */
	public void setAttributes(String statementTypeLabel, String variable, Object[] attributes, boolean removeStatements, boolean simulate, boolean verbose) throws Exception {
		StatementType st = this.data.getStatementType(statementTypeLabel);
		int id = -1;
		if (st != null) {
			id = st.getId();
		}
		setAttributes(id, variable, attributes, removeStatements, simulate, verbose);
	}
	
	/**
	 * Update the list of attributes based on an array of arrays for the attribute data.
	 * 
	 * @param statementTypeId   Statement type ID for which the attributes will be updated.
	 * @param variable          Variable name for which the attributes will be updated.
	 * @param attributes        Array of objects containing attribute ID, value, color, type, alias, and notes.
	 * @param removeStatements  Delete statements containing attribute values that are removed?
	 * @param simulate          If true, changes are not actually carried out.
	 * @param verbose           Should statistics on updating process be reported?
	 * @throws Exception
	 */
	public void setAttributes(int statementTypeId, String variable, Object[] attributes, boolean removeStatements, boolean simulate, boolean verbose) throws Exception {
		int[] id = (int[]) attributes[0];
		String[] value = (String[]) attributes[1];
		String[] color = (String[]) attributes[2];
		String[] type = (String[]) attributes[3];
		String[] alias = (String[]) attributes[4];
		String[] notes = (String[]) attributes[5];
		
		// check for duplicate attribute values and IDs and throw exception if necessary
		if (id.length > 0) {
			for (int i = 0; i < id.length; i++) {
				for (int j = 0; j < id.length; j++) {
					if (i != j && value[i].equals(value[j])) {
						throw new Exception("Duplicate attribute values are not permitted (ID " + id[i] + " and ID " + id[j] + ", value \"" + value[i] + "\").");
					}
					if (i != j && id[i] == id[j]) {
						throw new Exception("Duplicate attribute IDs are not permitted (ID " + id[i] + " and ID " + id[j] + ").");
					}
				}
			}
		}
		
		int updateCountValue = 0;
		int updateCountColor = 0;
		int updateCountType = 0;
		int updateCountAlias = 0;
		int updateCountNotes = 0;
		int updateCountNewAttributes = 0;
		int updateCountDeleted = 0;
		int updateCountStatementsDeleted = 0;
		int updateCountStatementsChanged = 0;
		
		if (verbose == true) {
			if (simulate == true) {
				System.out.println("Simulation mode: no actual changes are made to the database!");
			} else {
				System.out.println("Changes will be written both in memory and to the SQL database!");
			}
		}
		
		// delete attribute IDs in database that are not in the array provided by the user and update fields if the attribute ID exists and entries have changed
		HashMap<String, String> valueChangeMap = new HashMap<String, String>();
		ArrayList<String> deleteList = new ArrayList<String>();
		ArrayList<Integer> deleteListId = new ArrayList<Integer>();
		if (this.data.getAttributes(variable, statementTypeId).length > 0) {
			for (int j = this.data.getAttributes(variable, statementTypeId).length - 1; j > -1; j--) {
				// mark attribute in database for deletion if necessary or update fields
				boolean delete = true;
				if (this.data.getAttributes(variable, statementTypeId)[j].getValue().equals("")) {
					delete = false;
				} else if (id.length > 0) {
					for (int i = 0; i < id.length; i++) {
						if (this.data.getAttributes(variable, statementTypeId)[j].getId() == id[i]) {
							delete = false;
							if (!this.data.getAttributes(variable, statementTypeId)[j].getValue().equals(value[i])) {
								updateCountValue++;
								valueChangeMap.put(this.data.getAttributes(variable, statementTypeId)[j].getValue(), value[i]); // save values to be recoded in hash map
								if (simulate == false) {
									this.data.getAttributes(variable, statementTypeId)[j].setValue(value[i]);
									this.sql.updateAttribute(id[i], "Value", value[i]);
								}
							}
							String col = String.format("#%02X%02X%02X", 
									this.data.getAttributes(variable, statementTypeId)[j].getColor().getRed(), 
									this.data.getAttributes(variable, statementTypeId)[j].getColor().getGreen(), 
									this.data.getAttributes(variable, statementTypeId)[j].getColor().getBlue());
							if (!col.equals(color[i])) {
								updateCountColor++;
								if (simulate == false) {
									this.data.getAttributes(variable, statementTypeId)[j].setColor(color[i]);
									this.sql.updateAttributeColor(id[i], color[i]);
								}
							}
							if (!this.data.getAttributes(variable, statementTypeId)[j].getAlias().equals(alias[i])) {
								updateCountAlias++;
								if (simulate == false) {
									this.data.getAttributes(variable, statementTypeId)[j].setAlias(alias[i]);
									this.sql.updateAttribute(id[i], "Alias", alias[i]);
								}
							}
							if (!this.data.getAttributes(variable, statementTypeId)[j].getNotes().equals(notes[i])) {
								updateCountNotes++;
								if (simulate == false) {
									this.data.getAttributes(variable, statementTypeId)[j].setNotes(notes[i]);
									this.sql.updateAttribute(id[i], "Notes", notes[i]);								
								}
							}
						}
					}
				}
				
				// delete attribute in database and mark for statement deletion
				if (delete == true) {
					deleteList.add(this.data.getAttributes(variable, statementTypeId)[j].getValue());
					deleteListId.add(this.data.getAttributes(variable, statementTypeId)[j].getId());
				}
			}
			
			// delete attributes based on delete list for IDs
			if (deleteListId.size() > 0) {
				for (int j = 0; j < deleteListId.size(); j++) {
					for (int i = this.data.getAttributes().size() - 1; i > -1; i--) {
						if (this.data.getAttributes().get(i).getId() == deleteListId.get(j)) {
							updateCountDeleted++;
							if (simulate == false) {
								this.sql.deleteAttributeVector(this.data.getAttributes().get(i).getId());
								this.data.getAttributes().remove(i);
							}
						}
					}
				}
			}
			
			// delete statements where the value is marked for deletion
			if (removeStatements == true) {
				for (int k = this.data.getStatements().size() - 1; k > -1; k--) {
					if (deleteList.contains(this.data.getStatements().get(k).getValues().get(variable))) {
						updateCountStatementsDeleted++;
						if (simulate == false) {
							this.sql.removeStatement(this.data.getStatements().get(k).getId());
							this.data.getStatements().remove(k);
						}
					}
				}
			}
		}
		
		// recode values in statements (as saved in hash map above)
		if (this.data.getStatements().size() > 0) {
			for (int k = 0; k < this.data.getStatements().size(); k++) {
				if (valueChangeMap.containsKey(this.data.getStatements().get(k).getValues().get(variable))) {
					updateCountStatementsChanged++;
					if (simulate == false) {
						String oldValue = (String) this.data.getStatements().get(k).getValues().get(variable);
						String newValue = valueChangeMap.get(oldValue);
						int statementId = this.data.getStatements().get(k).getId();
						this.sql.upsertVariableContent(newValue, statementId, variable, statementTypeId, "short text");
						this.data.getStatements().get(k).getValues().put(variable, newValue);
					}
				}
				
			}
		}

		// add new attributes that don't exist in the database yet
		if (id.length > 0) {
			for (int i = 0; i < id.length; i++) {
				boolean exists = false;
				boolean fixId = false;
				if (this.data.getAttributes(variable, statementTypeId).length > 0) {
					for (int j = 0; j < this.data.getAttributes().size(); j++) {
						if (id[i] == this.data.getAttributes().get(j).getId()) {
							if (this.data.getAttributes().get(j).getStatementTypeId() == statementTypeId 
									&& this.data.getAttributes().get(j).getVariable().equals(variable) 
									&& !this.data.getAttributes().get(j).getValue().equals("")) {
								exists = true;
							} else {
								exists = false;
								fixId = true;
							}
						}
					}
				}
				if (!exists) {
					int newId;
					if (fixId == true || id[i] == -1) {
						newId = this.data.generateNewId("attributes");
					} else {
						newId = id[i];
					}
					updateCountNewAttributes++;
					if (simulate == false) {
						AttributeVector av = new AttributeVector(newId, value[i], color[i], type[i], alias[i], notes[i], "", statementTypeId, variable);
						this.data.getAttributes().add(av);
						this.sql.upsertAttributeVector(av);
					}
				}
			}
		}

		// report statistics
		if (verbose == true) {
			System.out.println("New attributes added: " + updateCountNewAttributes);
			System.out.println("Deleted attributes:   " + updateCountDeleted);
			System.out.println("Values updated:       " + updateCountValue);
			System.out.println("Colors updated:       " + updateCountColor);
			System.out.println("Types updated:        " + updateCountType);
			System.out.println("Aliases updated:      " + updateCountAlias);
			System.out.println("Notes updated:        " + updateCountNotes);
			System.out.println("Updated statements:   " + updateCountStatementsChanged);
			System.out.println("Deleted statements:   " + updateCountStatementsDeleted);
		}
	}

	/**
	 * Add an attribute both to memory and the SQL database, with a given statement type label.
	 * 
	 * @param statementTypeLabel  Statement type string in which the variable is defined.
	 * @param variable            Variable name to which the attribute will be added, as defined in the respective statement type.
	 * @param value               Attribute value.
	 * @param color               Attribute color.
	 * @param type                Attribute type.
	 * @param alias               Attribute alias.
	 * @param notes               Attribute notes.
	 * @return                    New document ID.
	 * @throws Exception 
	 */
	public int addAttribute(String statementTypeLabel, String variable, String value, String color, String type, String alias, String notes) throws Exception {
		int statementTypeId = this.data.getStatementType(statementTypeLabel).getId();
		return addAttribute(statementTypeId, variable, value, color, type, alias, notes);
	}
	
	/**
	 * Add an attribute both to memory and the SQL database, with a given statement type ID.
	 * 
	 * @param statementTypeId  Statement type ID in which the variable is defined.
	 * @param variable         Variable name to which the attribute will be added, as defined in the respective statement type.
	 * @param value            Attribute value.
	 * @param color            Attribute color.
	 * @param type             Attribute type.
	 * @param alias            Attribute alias.
	 * @param notes            Attribute notes.
	 * @return                 New document ID.
	 * @throws Exception 
	 */
	public int addAttribute(int statementTypeId, String variable, String value, String color, String type, String alias, String notes) throws Exception {
		if (value.equals("")) {
			throw new Exception("The 'value' field cannot be empty. Aborting.");
		}
		for (int i = 0; i < this.data.getAttributes(variable, statementTypeId).length; i++) {
			if (this.data.getAttributes(variable, statementTypeId)[i].getValue().equals(value)) {
				throw new Exception("An attribute with the same statement type, variable, and value already exists. Aborting.");
			}
		}
		int id = this.data.generateNewId("attributes");
		AttributeVector av = new AttributeVector(id, value, color, type, alias, notes, "", statementTypeId, variable);
		this.data.getAttributes().add(av);
		Collections.sort(this.data.getAttributes());
		this.sql.upsertAttributeVector(av);
		return id;
	}
	
	/**
	 * Remove an attribute based on its value. This is a wrapper for the function with the same name but an additional id argument.
	 * 
	 * @param statementTypeId   Statement type ID associated with the attribute.
	 * @param variable          Variable contained in the statement ID that is associated with the attribute.
	 * @param value             String value of the attribute to be removed.
	 * @param removeStatements  Delete statements associated with attributes that are removed?
	 * @param simulate          If true, changes are not actually carried out.
	 * @param verbose           Should messages be printed?
	 */
	public void removeAttribute(int statementTypeId, String variable, String value, boolean removeStatements, boolean simulate, boolean verbose) {
		int id = -1;
		for (int i = 0; i < this.data.getAttributes(variable, statementTypeId).length; i++) {
			if (this.data.getAttributes(variable, statementTypeId)[i].getValue().equals(value)) {
				id = this.data.getAttributes(variable, statementTypeId)[i].getId();
			}
		}
		removeAttribute(statementTypeId, variable, value, id, removeStatements, simulate, verbose);
	}

	/**
	 * Remove an attribute based on its ID. This is a wrapper for the function with the same name but an additional value argument.
	 * 
	 * @param statementTypeId   Statement type ID associated with the attribute.
	 * @param variable          Variable contained in the statement ID that is associated with the attribute.
	 * @param id                ID of the attribute to be removed.
	 * @param removeStatements  Delete statements associated with attributes that are removed?
	 * @param simulate          If true, changes are not actually carried out.
	 * @param verbose           Should messages be printed?
	 */
	public void removeAttribute(int statementTypeId, String variable, int id, boolean removeStatements, boolean simulate, boolean verbose) {
		String value = "";
		for (int i = 0; i < this.data.getAttributes(variable, statementTypeId).length; i++) {
			if (this.data.getAttributes(variable, statementTypeId)[i].getId() == id) {
				value = this.data.getAttributes(variable, statementTypeId)[i].getValue();
			}
		}
		removeAttribute(statementTypeId, variable, value, id, removeStatements, simulate, verbose);
	}
	
	/**
	 * Remove an attribute based on its value and ID.
	 * 
	 * @param statementTypeId   Statement type ID associated with the attribute.
	 * @param variable          Variable contained in the statement ID that is associated with the attribute.
	 * @param value             String value of the attribute to be removed.
	 * @param id                ID of the attribute to be removed.
	 * @param removeStatements  Delete statements associated with attributes that are removed?
	 * @param simulate          If true, changes are not actually carried out.
	 * @param verbose           Should messages be printed?
	 */
	public void removeAttribute(int statementTypeId, String variable, String value, int id, boolean removeStatements, boolean simulate, boolean verbose) {
		// report simulation mode
		if (verbose == true) {
			if (simulate == true) {
				System.out.println("Simulation mode: no actual changes are made to the database!");
			} else {
				System.out.println("Changes will be written both in memory and to the SQL database!");
			}
		}
		
		// delete statements if necessary
		int updateCountStatementsDeleted = 0;
		boolean affectsStatements = false;
		if (!this.data.getStatements().isEmpty()) {
			for (int k = this.data.getStatements().size() - 1; k > -1; k--) {
				if (this.data.getStatements().get(k).getStatementTypeId() == statementTypeId && this.data.getStatements().get(k).getValues().get(variable).equals(value)) {
					if (removeStatements == true) {
						int statementId = this.data.getStatements().get(k).getId();
						if (simulate == false) {
							this.data.getStatements().remove(k);
							this.sql.removeStatement(statementId);
						}
						updateCountStatementsDeleted++;
					} else {
						affectsStatements = true;
					}
				}
			}
		}
		
		// report result of statement deletion
		if (verbose == true) {
			System.out.println("Statements removed: " + updateCountStatementsDeleted);
		}
		
		// remove attribute from memory and SQL database if there are no statements or the statements were deleted
		if (!affectsStatements) {
			boolean success = false;
			for (int i = this.data.getAttributes().size() - 1; i > -1 ; i--) {
				if (this.data.getAttributes().get(i).getStatementTypeId() == statementTypeId && this.data.getAttributes().get(i).getValue().equals(value) && !this.data.getAttributes().get(i).getValue().equals("")) {
					if (simulate == false) {
						this.data.getAttributes().remove(i);
					}
					success = true;
				}
			}
			if (success) {
				if (simulate == false) {
					this.sql.deleteAttributeVector(id);
				}
			}
			if (verbose) {
				if (success) {
					System.out.println("Removal of attribute " + id + ": successful.");
				} else {
					System.err.println("Removal of attribute " + id + ": ID was not found.");
				}
			}
		} else {
			System.err.println("Removal of attribute " + id + ": attribute was not removed because it is used in existing statements.");
		}
	}

	
	/* =================================================================================================================
	 * Functions for managing documents
	 * =================================================================================================================
	 */
	
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
					}
					if (!containsStatements) {
						if (simulate == false) {
							int docId = this.data.getDocuments().get(j).getId();
							this.data.removeDocument(docId);
							this.sql.removeDocument(docId);
						}
						updateCountDeleted++;
					} else {
						System.err.println("Document " + this.data.getDocuments().get(j).getId() + " contains statements and was not removed.");
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
				int newId = id[i];
				if (newId == -1) {
					newId = this.data.generateNewId("documents");
				}
				Document d = new Document(newId, title[i], text[i], coder[i], author[i], source[i], section[i], notes[i], type[i], new Date(date[i]));
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
	 * @param id                   ID of the document.
	 * @param removeStatements     Delete statements contained in documents that are removed?
	 * @param simulate             If true, changes are not actually carried out.
	 * @param verbose              Should messages be printed?
	 */
	public void removeDocument(int id, boolean removeStatements, boolean simulate, boolean verbose) {
		
		// report simulation mode
		if (verbose == true) {
			if (simulate == true) {
				System.out.println("Simulation mode: no actual changes are made to the database!");
			} else {
				System.out.println("Changes will be written both in memory and to the SQL database!");
			}
		}
		
		// delete statements if necessary
		int updateCountStatementsDeleted = 0;
		boolean containsStatements = false;
		if (!this.data.getStatements().isEmpty()) {
			for (int k = this.data.getStatements().size() - 1; k > -1; k--) {
				if (this.data.getStatements().get(k).getDocumentId() == id) {
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
		}
		
		// report result of statement deletion
		if (verbose == true) {
			System.out.println("Statements removed in Document " + id + ": " + updateCountStatementsDeleted);
		}
		
		// remove document from memory and SQL database if there are no statements or the statements were deleted
		if (!containsStatements) {
			boolean success = false;
			for (int i = this.data.getDocuments().size() - 1; i > -1 ; i--) {
				if (this.data.getDocuments().get(i).getId() == id) {
					if (simulate == false) {
						this.data.getDocuments().remove(i);
					}
					success = true;
				}
			}
			if (success) {
				if (simulate == false) {
					this.sql.removeDocument(id);
				}
			}
			if (verbose) {
				if (success) {
					System.out.println("Removal of Document " + id + ": successful.");
				} else {
					System.err.println("Removal of Document " + id + ": ID was not found.");
				}
			}
		} else {
			System.err.println("Removal of Document " + id + ": document contains statements and was not removed.");
		}
	}

	
	/* =================================================================================================================
	 * Functions for managing statements
	 * =================================================================================================================
	 */

	/**
	 * Retrieve an object array of all statement data for R using a statement type label.
	 * 
	 * @param statementType  The statement type name for which the statements should be retrieved.
	 * @return               An object array with the different slots for the variables.
	 * @throws Exception 
	 */
	public Object[] getStatements(String statementType) throws Exception {
		try {
			int statementTypeId = this.data.getStatementType(statementType).getId();
			Object[] objects = getStatements(statementTypeId);
			return objects;
		} catch (NullPointerException npe) {
			throw new Exception("Statement type '" + statementType + "' could not be found.");
		}
	}
	
	/**
	 * Retrieve an object array of all statement data for R using a statement type ID.
	 * 
	 * @param statementTypeId  The statement type ID for which the statements should be retrieved.
	 * @return                 An object array with the different slots for the variables.
	 */
	public Object[] getStatements(int statementTypeId) {
		ArrayList<Statement> sl = this.data.getStatementsByStatementTypeId(statementTypeId);
		int n = sl.size();
		int[] statementIds = new int[n];
		int[] documentIds = new int[n];
		int[] startCarets = new int[n];
		int[] endCarets = new int[n];
		int[] statementTypeIds = new int[n];
		int[] coders = new int[n];
		Statement s;
		for (int i = 0; i < n; i++) {
			s = sl.get(i);
			statementIds[i] = s.getId();
			documentIds[i] = s.getDocumentId();
			startCarets[i] = s.getStart();
			endCarets[i] = s.getStop();
			statementTypeIds[i] = statementTypeId;
			coders[i] = s.getCoder();
		}

		StatementType st = this.data.getStatementTypeById(statementTypeId);
		Object[] object = new Object[6 + st.getVariables().size()];
		object[0] = statementIds;
		object[1] = documentIds;
		object[2] = startCarets;
		object[3] = endCarets;
		object[4] = statementTypeIds;
		object[5] = coders;
		
		int counter = 5;
		String key, value;
		Iterator<String> iterator = st.getVariables().keySet().iterator();
		while (iterator.hasNext()) {
			counter++;
			key = iterator.next();
			value = st.getVariables().get(key);
			if (value.equals("short text") || value.equals("long text")) {
				String[] var = new String[n];
				for (int i = 0; i < n; i++) {
					var[i] = (String) sl.get(i).getValues().get(key);
				}
				object[counter] = var;
			} else {
				int[] var = new int[n];
				for (int i = 0; i < n; i++) {
					var[i] = (int) sl.get(i).getValues().get(key);
				}
				object[counter] = var;
			}
		}
		
		return object;
	}
	
	/**
	 * Update the list of statements based on an array of arrays for the statement data.
	 * 
	 * @param statements  Array of objects containing statement IDs, document IDs, start carets, end carets, statement type IDs, coders, and further variables defined in the statement type.
	 * @param simulate    If true, changes are not actually carried out.
	 * @param verbose     Should statistics on updating process be reported?
	 * @throws Exception
	 */
	public void setStatements(Object[] statements, boolean simulate, boolean verbose) throws Exception {
		int[] id = (int[]) statements[0];
		int[] documentId = (int[]) statements[1];
		int[] startCaret = (int[]) statements[2];
		int[] endCaret = (int[]) statements[3];
		int[] statementTypeIDs = (int[]) statements[4]; // deviate slightly from naming convention here because 'statementTypeId' will be used later for only the first value
		int[] coder = (int[]) statements[5];

		int updateCountDeleted = 0;
		int updateCountNewStatements = 0;
		int updateCountDocumentId = 0;
		int updateCountStartCaret = 0;
		int updateCountEndCaret = 0;
		int updateCountCoder = 0;
		int numVar = statements.length - 6;
		int[] updateCountVariables = new int[numVar];

		ArrayList<ArrayList<String>> addedAttributes = new ArrayList<>();
		for (int i = 0; i < numVar; i++) {
			addedAttributes.add(new ArrayList<String>());
		}
		
		if (verbose == true) {
			if (simulate == true) {
				System.out.println("Simulation mode: no actual changes are made to the database!");
			} else {
				System.out.println("Changes will be written both in memory and to the SQL database!");
			}
		}
		
		// find out which variables are in the table and what data types they have, based on the first entry
		String[] varNames = new String[numVar];
		String[] varTypes = new String[numVar];
		int statementTypeId = statementTypeIDs[0];
		StatementType st;
		try {
			st = this.data.getStatementTypeById(statementTypeId);
		} catch (NullPointerException npe) {
			throw new Exception("Statement type ID of the first statement was not found in the database. Aborting.");
		}
		LinkedHashMap<String, String> variables = st.getVariables();
		Iterator<String> iterator = variables.keySet().iterator();
		int counter = 0;
		while (iterator.hasNext()) {
			String key = iterator.next();
			varNames[counter] = key;
			varTypes[counter] = variables.get(key);
			counter++;
		}
		if (counter != numVar) {
			throw new Exception("Number of variables in the data frame does not match the number of variables in the statement type definition. Aborting.");
		}
		
		// delete statements that are not in the input array 'statements'
		if (this.data.getStatements().size() > 0) {
			boolean delete;
			for (int j = this.data.getStatements().size() - 1; j > -1; j--) {
				if (this.data.getStatements().get(j).getStatementTypeId() == statementTypeId) {
					delete = true;
					for (int i = 0; i < id.length; i++) {
						if (this.data.getStatements().get(j).getId() == id[i]) {
							delete = false;
						}
					}
					if (delete == true) {
						if (verbose == true) {
							int tempId = this.data.getStatements().get(j).getId();
							System.out.print("  - Deleting statement " + tempId + "... ");
						}
						if (simulate == false) {
							int statementId = this.data.getStatements().get(j).getId();
							this.data.removeStatement(statementId);
							this.sql.removeStatement(statementId);
						}
						if (verbose == true) {
							System.out.println("Done.");
						}
						updateCountDeleted++;
					}
				}
			}
		}
		
		// add or update statements
		for (int i = 0; i < id.length; i++) {
			boolean update = false;

			// check if statement ID exists
			int foundIndex = -1;
			for (int j = 0; j < this.data.getStatements().size(); j++) {
				if (id[i] == this.data.getStatements().get(j).getId()) {
					foundIndex = j;
				}
			}
			
			// check if coder field is valid
			if (this.data.getCoderById(coder[i]) == null) {
				System.err.println("Statement ID " + id[i] + ": coder ID is invalid. Skipping this statement.");
			}

			// check if the document ID is valid
			if (this.data.getDocument(documentId[i]) == null) {
				System.err.println("Statement ID " + id[i] + ": document ID was not found in the database. Skipping this statement.");
			}
						
			// check if start caret < end caret
			if (startCaret[i] >= endCaret[i]) {
				System.err.println("Statement ID " + id[i] + ": end caret is not greater than the start caret, meaning the statement would have zero or negative length. Skipping this statement.");
			}
			
			// check if document length is shorter than the supplied start caret
			if (this.data.getDocument(documentId[i]).getText().length() - 1 < startCaret[i]) {
				System.err.println("Statement ID " + id[i] + ": start caret would be after the last character of the document. Skipping this statement.");
			}

			// check if document length is shorter than the supplied end caret
			if (this.data.getDocument(documentId[i]).getText().length() < endCaret[i]) {
				System.err.println("Statement ID " + id[i] + ": end caret would be more than one character after the last character of the document. Skipping this statement.");
			}
			
			// check if statement type matches the first statement type in the 'statements' data frame
			if (statementTypeId != statementTypeIDs[i]) {
				System.err.println("Statement ID " + id[i] + ": statement type ID is not identical to the first statement type ID in the data frame. Skipping this statement.");
			}
			
			// check if boolean variables are indeed 0 or 1
			for (int j = 0; j < numVar; j++) {
				if (varTypes[j].equals("boolean") && ((int[]) statements[j + 6])[i] != 0 && ((int[]) statements[j + 6])[i] != 1) {
					System.err.println("Statement ID " + id[i] + ": variable '" + varNames[j] + "' is not 0 or 1. Skipping this statement.");
				}
			}
			
			if (foundIndex > -1) { // update (rather than add)
				if (this.data.getStatements().get(foundIndex).getStart() != startCaret[i]) {
					if (simulate == false) {
						this.data.getStatements().get(foundIndex).setStart(startCaret[i]);
					}
					update = true;
					updateCountStartCaret++;
				}
				if (this.data.getStatements().get(foundIndex).getStop() != endCaret[i]) {
					if (simulate == false) {
						this.data.getStatements().get(foundIndex).setStop(endCaret[i]);
					}
					update = true;
					updateCountEndCaret++;
				}
				if (this.data.getStatements().get(foundIndex).getDocumentId() != documentId[i]) {
					if (simulate == false) {
						this.data.getStatements().get(foundIndex).setDocumentId(documentId[i]);
					}
					update = true;
					updateCountDocumentId++;
				}
				if (this.data.getStatements().get(foundIndex).getCoder() != coder[i]) {
					if (simulate == false) {
						this.data.getStatements().get(foundIndex).setCoder(coder[i]);
					}
					update = true;
					updateCountCoder++;
				}
				
				// go through remaining variables and update where necessary
				for (int j = 0; j < numVar; j++) {
					if (varTypes[j].equals("short text") || varTypes[j].equals("long text")) {
						String s = ((String[]) statements[j + 6])[i];
						if (!this.data.getStatements().get(foundIndex).getValues().get(varNames[j]).equals(s)) {
							if (simulate == false) {
								// update variable in the database (in memory)
								this.data.getStatements().get(foundIndex).getValues().put(varNames[j], s);
							}
							// also add a new attribute if the value doesn't exist yet in the database (in memory and SQL)
							if (this.data.getAttributeId(s, varNames[j], statementTypeId) == -1 && !addedAttributes.get(j).contains(s)) {
								if (verbose == true) {
									System.out.print("  - New attribute for variable '" + varNames[j] + "': '" + s + "'... ");
								}
								int attributeId = this.data.generateNewId("attributes");
								AttributeVector av = new AttributeVector(attributeId, s, "#000000", "", "", "", "", statementTypeId, varNames[j]);
								if (simulate == false) {
									this.data.attributes.add(av);
									Collections.sort(this.data.getAttributes());
									this.sql.upsertAttributeVector(av);
								}
								addedAttributes.get(j).add(s); // save added attributes in a list so they are not added multiple times in simulation mode
								if (verbose == true) {
									System.out.println("Done.");
								}
							}
							update = true;
							updateCountVariables[j]++;
						}
					} else {
						if ((int) this.data.getStatements().get(foundIndex).getValues().get(varNames[j]) != ((int[]) statements[j + 6])[i]) {
							if (simulate == false) {
								this.data.getStatements().get(foundIndex).getValues().put(varNames[j], ((int[]) statements[j + 6])[i]);
							}
							update = true;
							updateCountVariables[j]++;
						}
					}
				}

				if (update == true) {
					if (verbose == true) {
						System.out.print("  - Updating statement " + this.data.getStatements().get(foundIndex).getId() + "... ");
					}
					if (simulate == false) {
						this.sql.upsertStatement(this.data.getStatements().get(foundIndex), st.getVariables());
					}
					if (verbose == true) {
						System.out.println("Done.");
					}
				}
			} else { // add (rather than update)
				int newId = this.data.generateNewId("statements");
				Statement statement = new Statement(newId, documentId[i], startCaret[i], endCaret[i], this.data.getDocument(documentId[i]).getDate(), statementTypeId, coder[i]);
				for (int j = 0; j < numVar; j++) {
					if (varTypes[j].equals("short text") || varTypes[j].equals("long text")) {
						String s = ((String[]) statements[j + 6])[i];

						// put value in statement (in memory)
						statement.getValues().put(varNames[j], s);

						// add a new attribute if the value doesn't exist yet in the database (in memory and SQL)
						if (this.data.getAttributeId(s, varNames[j], statementTypeId) == -1 && !addedAttributes.get(j).contains(s)) {
							if (verbose == true) {
								System.out.print("  - New attribute for variable '" + varNames[j] + "': '" + s + "'... ");
							}
							int attributeId = this.data.generateNewId("attributes");
							AttributeVector av = new AttributeVector(attributeId, s, "#000000", "", "", "", "", statementTypeId, varNames[j]);
							if (simulate == false) {
								this.data.attributes.add(av);
								Collections.sort(this.data.getAttributes());
								this.sql.upsertAttributeVector(av);
							}
							addedAttributes.get(j).add(s); // save added attributes in a list so they are not added multiple times in simulation mode
							if (verbose == true) {
								System.out.println("Done.");
							}
						}
					} else { // attributes only exist for short or long text variables
						statement.getValues().put(varNames[j], ((int[]) statements[j + 6])[i]);
					}
				}
				if (verbose == true) {
					System.out.print("  - Adding statement... ");
				}
				if (simulate == false) {
					this.data.addStatement(statement);
					System.out.print("New statement ID: " + statement.getId() + "... ");
					this.sql.addStatement(statement, st.getVariables());
				}
				if (verbose == true) {
					System.out.println("Done.");
				}
				updateCountNewStatements++;
			}
		}

		// report statistics
		if (verbose == true) {
			System.out.println("New statements: " + updateCountNewStatements);
			System.out.println("Deleted statements: " + updateCountDeleted);
			System.out.println("Document IDs updated: " + updateCountDocumentId);
			System.out.println("Start carets updated: " + updateCountStartCaret);
			System.out.println("End carets updated: " + updateCountEndCaret);
			System.out.println("Coders updated: " + updateCountCoder);
			for (int i = 0; i < numVar; i++) {
				System.out.println("Updated variable '" + varNames[i] + "': " + updateCountVariables[i]);
			}
		}
	}
	
	/**
	 * Wrapper for the {@link addStatement} function. This function is identical but specifies the statement type as a 
	 * String label instead of the statement type ID AND specifies only a single variable name (because there is only 
	 * one variable defined in the statement type).
	 * 
	 * @param documentId       The document ID of the document to which the statement should be added.
	 * @param startCaret       The start position of the statement in the document.
	 * @param endCaret         The stop position of the statement in the document.
	 * @param statementType    The label of the statement type of which the statement to be created is an instance.
	 * @param coder            The ID of the coder that adds the current statement.
	 * @param varName          A single variable names to which the values should be added (in case there is only one single variable present).
	 * @param values           The values to be added to the statement variables as an {@link Object} array.
	 * @param verbose          Report feedback to the console?
	 * @return                 A new ID of the statement that was added.
	 * @throws Exception
	 */
	public int addStatement(int documentId, int startCaret, int endCaret, String statementType, int coder, String varName, Object[] values, boolean verbose) {
		int statementTypeId = -1;
		try {
			statementTypeId = this.data.getStatementType(statementType).getId();
		} catch (NullPointerException npe) {
			System.err.println("Statement could not be added because the statement type is unknown.");
			return -1;
		}
		String[] varNames = new String[] { varName };  // convert string into string array
		return addStatement(documentId, startCaret, endCaret, statementTypeId, coder, varNames, values, verbose);
	}
	
	/**
	 * Wrapper for the {@link addStatement} function. This function is identical but specifies the statement type as a String label instead of the statement type ID.
	 * 
	 * @param documentId       The document ID of the document to which the statement should be added.
	 * @param startCaret       The start position of the statement in the document.
	 * @param endCaret         The stop position of the statement in the document.
	 * @param statementType    The label of the statement type of which the statement to be created is an instance.
	 * @param coder            The ID of the coder that adds the current statement.
	 * @param varNames         The variable names to which the values should be added, in the same order as the values.
	 * @param values           The values to be added to the statement variables as an {@link Object} array.
	 * @param verbose          Report feedback to the console?
	 * @return                 A new ID of the statement that was added.
	 * @throws Exception
	 */
	public int addStatement(int documentId, int startCaret, int endCaret, String statementType, int coder, String[] varNames, Object[] values, boolean verbose) {
		int statementTypeId = -1;
		try {
			statementTypeId = this.data.getStatementType(statementType).getId();
		} catch (NullPointerException npe) {
			System.err.println("Statement could not be added because the statement type is unknown.");
			return -1;
		}
		return addStatement(documentId, startCaret, endCaret, statementTypeId, coder, varNames, values, verbose);
	}

	/**
	 * Wrapper for the {@link addStatement} function. This function is identical but specifies only a single variable 
	 * name (because there is only one variable defined in the statement type).
	 * 
	 * @param documentId       The document ID of the document to which the statement should be added.
	 * @param startCaret       The start position of the statement in the document.
	 * @param endCaret         The stop position of the statement in the document.
	 * @param statementTypeId  The ID of the statement type of which the statement to be created is an instance.
	 * @param coder            The ID of the coder that adds the current statement.
	 * @param varName          A single variable names to which the values should be added (in case there is only one single variable present).
	 * @param values           The values to be added to the statement variables as an {@link Object} array.
	 * @param verbose          Report feedback to the console?
	 * @return                 A new ID of the statement that was added.
	 * @throws Exception
	 */
	public int addStatement(int documentId, int startCaret, int endCaret, int statementTypeId, int coder, String varName, Object[] values, boolean verbose) {
		String[] varNames = new String[] { varName };  // convert string into string array
		return addStatement(documentId, startCaret, endCaret, statementTypeId, coder, varNames, values, verbose);
	}
	
	/**
	 * Add a new statement with custom contents to the database.
	 * 
	 * @param documentId       The document ID of the document to which the statement should be added.
	 * @param startCaret       The start position of the statement in the document.
	 * @param endCaret         The stop position of the statement in the document.
	 * @param statementTypeId  The ID of the statement type of which the statement to be created is an instance.
	 * @param coder            The ID of the coder that adds the current statement.
	 * @param varNames         The variable names to which the values should be added, in the same order as the values.
	 * @param values           The values to be added to the statement variables as an {@link Object} array.
	 * @param verbose          Report feedback to the console?
	 * @return                 A new ID of the statement that was added.
	 * @throws Exception
	 */
	public int addStatement(int documentId, int startCaret, int endCaret, int statementTypeId, int coder, String[] varNames, Object[] values, boolean verbose) {
		int statementId = this.data.generateNewId("statements");
		int docLength = 0;
		try {
			docLength = data.getDocument(documentId).getText().length();
		} catch (NullPointerException npe) {
			System.err.println("Error: Document ID could not be found.");
			return -1;
		}
		if (docLength == 0) {
			System.err.println("Error: No statements can be added to this document because its length is zero.");
			return -1;
		}
		Date date = data.getDocument(documentId).getDate();
		if (startCaret < 0) {
			System.err.println("Error: 'startCaret' must be 0 or greater than 0.");
			return -1;
		}
		if (startCaret > docLength - 1) {
			System.err.println("Error: 'startCaret' is greater than the length minus one of the document with ID " + documentId + ".");
			return -1;
		}
		if (endCaret < startCaret + 1) {
			System.err.println("Error: 'endCaret' position must be greater than the 'startCaret' position.");
			return -1;
		}
		if (endCaret > docLength) {
			System.err.println("Error: 'endCaret' is greater than the length of the document with ID " + documentId + ".");
			return -1;
		}
		if (this.data.getCoderById(coder) == null) {
			System.err.println("Error: Statement could not be added because coder ID is unknown.");
			return -1;
		}
		
		StatementType st = this.data.getStatementTypeById(statementTypeId);
		if (st == null) {
			System.err.println("Error: Statement could not be added because the statement type is unknown.");
			return -1;
		}
		
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		for (int i = 0; i < values.length; i++) {
			if (!st.getVariables().containsKey(varNames[i])) {
				System.err.println("Warning: The value for variable '" + varNames[i] + "' is ignored because this variable is not defined in statement type '" + st.getLabel() + "'.");
				continue;
			}
			if (st.getVariables().get(varNames[i]).equals("boolean") || st.getVariables().get(varNames[i]).equals("integer")) {
				int[] v = (int[]) values[i];
				if (st.getVariables().get(varNames[i]).equals("boolean") && (v[0] < 0 || v[0] > 1)) {
					System.err.println("Warning: The value for the boolean variable '" + varNames[i] + "' is replaced by 1 because the value provided is outside the valid range of values.");
					map.put(varNames[i], 1);
				} else {
					map.put(varNames[i], v[0]);
				}
				if (verbose == true) {
					System.out.println(varNames[i] + ": " + v[0]);
				}
			} else {
				String[] v = (String[]) values[i];
				map.put(varNames[i], v[0]);
				
				// also add a new attribute if the value doesn't exist yet in the database
				String attrString = "";
				if (this.data.getAttributeId(v[0], varNames[i], statementTypeId) == -1) {
					int attributeId = this.data.generateNewId("attributes");
					AttributeVector av = new AttributeVector(attributeId, v[0], "#000000", "", "", "", "", statementTypeId, varNames[i]);
					this.data.attributes.add(av);
					Collections.sort(this.data.getAttributes());
					this.sql.upsertAttributeVector(av);
					attrString = " [new attribute added]";
				}
				
				if (verbose == true) {
					System.out.println(varNames[i] + ": " + v[0] + attrString);
				}
			}
		}
		Iterator<String> mapIterator = st.getVariables().keySet().iterator();
		String key, type;
		while (mapIterator.hasNext()) {
			key = mapIterator.next();
			type = st.getVariables().get(key);
			if (!map.containsKey(key)) {
				if (type.equals("boolean")) {
					map.put(key, 1);
				} else if (type.equals("integer")) {
					map.put(key, 0);
				} else if (type.equals("short text") || type.equals("long text")) {
					map.put(key, "");
				}
			}
		}
		Statement s = new Statement(statementId, documentId, startCaret, endCaret, date, statementTypeId, coder, map);
		this.data.addStatement(s);
		this.sql.addStatement(s, st.getVariables());
		if (verbose == true) {
			System.out.println("A new statement with ID " + statementId + " was added to the database in document " + documentId + ".");
		}
		return statementId;
	}
	
	/**
	 * Remove a statement with a given statement ID.
	 * 
	 * @param statementId  ID of the statement to delete from the database and memory.
	 * @param verbose      Print details?
	 * @throws Exception
	 */
	public void removeStatement(int statementId, boolean verbose) throws Exception {
		if (this.data.getStatement(statementId) == null) {
			throw new Exception("Statement with ID " + statementId + " not found.");
		}
		this.data.removeStatement(statementId);
		this.sql.removeStatement(statementId);
		if (verbose == true) {
			System.out.println("Removal of Statement " + statementId + ": successful.");
		}
	}

	
	/* =================================================================================================================
	 * Functions for managing statement types
	 * =================================================================================================================
	 */

	/**
	 * Create and return an object that contains all statement types (but not their variable definitions).
	 * 
	 * @return  An Object array of IDs, labels, and color strings.
	 */
	public Object[] getStatementTypes() {
		int n = this.data.getStatementTypes().size();
		int[] ids = new int[n];
		String[] labels = new String[n];
		String[] colors = new String[n];
		StatementType st;
		for (int i = 0; i < n; i++) {
			st = this.data.getStatementTypes().get(i);
			ids[i] = st.getId();
			labels[i] = st.getLabel();
			colors[i] = String.format("#%02X%02X%02X", st.getColor().getRed(), st.getColor().getGreen(), st.getColor().getBlue()); 
		}
		Object[] object = new Object[3];
		object[0] = ids;
		object[1] = labels;
		object[2] = colors;
		return object;
	}

	/**
	 * Add a new, empty statement type (without variables) to the database.
	 * 
	 * @param statementTypeLabel  Name of the statement type.
	 * @param color               Color of the statement type as an RGB hex string (e.g., "#FF0000").
	 * @throws Exception
	 */
	public void addStatementType(String statementTypeLabel, String color) throws Exception {
		if (this.data.getStatementType(statementTypeLabel) != null) {
			throw new Exception("A statement type called '" + statementTypeLabel + "' already exists and will not be added.");
		}
		int id = this.data.generateNewId("statementTypes");
		LinkedHashMap<String, String> variables = new LinkedHashMap<String, String>();
		StatementType st = new StatementType(id, statementTypeLabel, color, variables);
		this.data.addStatementType(st);
		this.sql.upsertStatementType(st);
	}

	/**
	 * Remove a statement type including all variables, attributes, and statements, based on the statement type label.
	 * 
	 * @param statementTypeLabel  Label of the statement type to be removed.
	 * @param simulate            Simulate the changes without writing them to the database?
	 * @param verbose             Print details to the console?
	 * @throws Exception
	 */
	public void removeStatementType(String statementTypeLabel, boolean simulate, boolean verbose) throws Exception {
		int statementTypeId = this.data.getStatementType(statementTypeLabel).getId();
		removeStatementType(statementTypeId, simulate, verbose);
	}
	
	/**
	 * Remove a statement type including all variables, attributes, and statements, based on the statement type ID.
	 * 
	 * @param statementTypeId  ID of the statement type to be removed.
	 * @param simulate         Simulate the changes without writing them to the database?
	 * @param verbose          Print details to the console?
	 * @throws Exception
	 */
	public void removeStatementType(int statementTypeId, boolean simulate, boolean verbose) throws Exception {
		if (this.data.getStatementTypeById(statementTypeId) == null) {
			throw new Exception("A statement type with ID " + statementTypeId + " was not found in the database.");
		}
		
		// report simulation mode
		if (verbose == true) {
			if (simulate == true) {
				System.out.println("Simulation mode: no actual changes are made to the database!");
			} else {
				System.out.println("Changes will be written both in memory and to the SQL database!");
			}
		}

		// remove attributes associated with the statement type ID
		int removeAttributeCounter = 0;
		for (int i = this.data.getAttributes().size() - 1; i > -1 ; i--) {
			if (this.data.getAttributes().get(i).getStatementTypeId() == statementTypeId) {
				if (simulate == false) {
					this.data.getAttributes().remove(i);
				}
				removeAttributeCounter++;
			}
		}
		
		// remove statements associated with the statement type ID
		int removeStatementCounter = 0;
		for (int i = this.data.getStatements().size() - 1; i > -1; i--) {
			if (this.data.getStatements().get(i).getStatementTypeId() == statementTypeId) {
				if (simulate == false) {
					this.data.getStatements().remove(i);
				}
				removeStatementCounter++;
			}
		}
		
		// remove statement type
		if (simulate == false) {
			for (int i = this.data.getStatementTypes().size() - 1; i > -1; i--) {
				if (this.data.getStatementTypes().get(i).getId() == statementTypeId) {
					this.data.getStatementTypes().remove(i);
				}
			}
			this.sql.removeStatementType(statementTypeId);
		}

		// report statistics
		if (verbose == true) {
			System.out.println("Deleted attributes:      " + removeAttributeCounter);
			System.out.println("Deleted statements:      " + removeStatementCounter);
			System.out.println("Deleted statement types: 1");
		}
	}

	/**
	 * Rename a statement type by providing a new String label, based on the current label as input.
	 * 
	 * @param statementTypeLabel  Label of the statement type to be renamed.
	 * @param newLabel            New label as a string.
	 */
	public void renameStatementType(String statementTypeLabel, String newLabel) {
		int statementTypeId = this.data.getStatementType(statementTypeLabel).getId();
		this.data.getStatementTypeById(statementTypeId).setLabel(newLabel);
		this.sql.renameStatementType(statementTypeId, newLabel);
	}

	/**
	 * Rename a statement type by providing a new String label, based on the statement type ID as input.
	 * 
	 * @param statementTypeId  ID of the statement type to be renamed.
	 * @param newLabel         New label as a string.
	 */
	public void renameStatementType(int statementTypeId, String newLabel) {
		this.data.getStatementTypeById(statementTypeId).setLabel(newLabel);
		this.sql.renameStatementType(statementTypeId, newLabel);
	}

	/**
	 * Change the color of an existing statement type by providing a new hex color string, based on statement type label.
	 * 
	 * @param statementTypeLabel  Label of the statement type to be updated.
	 * @param color               String with a hexadecimal RGB color, for example "#FFFF00".
	 */
	public void colorStatementType(String statementTypeLabel, String color) {
		int statementTypeId = this.data.getStatementType(statementTypeLabel).getId();
		colorStatementType(statementTypeId, color);
	}

	/**
	 * Change the color of an existing statement type by providing a new hex color string, based on statement type ID.
	 * 
	 * @param statementTypeId  ID of the statement type to be updated.
	 * @param color            String with a hexadecimal RGB color, for example "#FFFF00".
	 */
	public void colorStatementType(int statementTypeId, String color) {
		this.data.getStatementTypeById(statementTypeId).setColor(color);
		this.sql.colorStatementType(statementTypeId,
				this.data.getStatementTypeById(statementTypeId).getBlue(),
				this.data.getStatementTypeById(statementTypeId).getBlue(),
				this.data.getStatementTypeById(statementTypeId).getBlue());
	}

	
	/* =================================================================================================================
	 * Functions for managing variables
	 * =================================================================================================================
	 */

	/**
	 * Retrieve variables and data type definitions for a given statement type (via label) and an Object array.
	 * 
	 * @param statementTypeLabel  Label of the statement type for which variables should be retrieved.
	 * @return                    Object array of variables and data type definitions.
	 */
	public Object[] getVariables(String statementTypeLabel) {
		int id = this.data.getStatementType(statementTypeLabel).getId();
		return getVariables(id);
	}
	
	/**
	 * Retrieve variables and data type definitions for a given statement type (via ID) and an Object array.
	 * 
	 * @param statementTypeId  ID of the statement type for which variables should be retrieved.
	 * @return                 Object array of variables and data type definitions.
	 */
	public Object[] getVariables(int statementTypeId) {
		StatementType st = this.data.getStatementTypeById(statementTypeId);
		int n = st.getVariables().size();
		String[] variables = new String[n];
		String[] types = new String[n];
		int counter = 0;
		String key, value;
		Iterator<String> iterator = st.getVariables().keySet().iterator();
		while (iterator.hasNext()) {
			key = iterator.next();
			value = st.getVariables().get(key);
			variables[counter] = key;
			types[counter] = value;
			counter++;
		}
		Object[] object = new Object[2];
		object[0] = variables;
		object[1] = types;
		return object;
	}
	
	/**
	 * Add a new variable to an existing statement type (as provided by a statement type label).
	 * 
	 * @param statementType    Label of the statement type in which the variable will be defined.
	 * @param variable         Variable name.
	 * @param dataType         Data type of the variable. Must be "short text", "long text", "integer", or "boolean".
	 * @param simulate         Simulate the changes without writing them to the database?
	 * @param verbose          Print details to the console?
	 * @throws Exception
	 */
	public void addVariable(String statementType, String variable, String dataType, boolean simulate, boolean verbose) throws Exception {
		int statementTypeId = this.data.getStatementType(statementType).getId();
		addVariable(statementTypeId, variable, dataType, simulate, verbose);
	}
	
	/**
	 * Add a new variable to an existing statement type (as provided by a statement type ID).
	 * 
	 * @param statementTypeId  ID of the statement type in which the variable will be defined.
	 * @param variable         Variable name.
	 * @param dataType         Data type of the variable. Must be "short text", "long text", "integer", or "boolean".
	 * @param simulate         Simulate the changes without writing them to the database?
	 * @param verbose          Print details to the console?
	 * @throws Exception
	 */
	public void addVariable(int statementTypeId, String variable, String dataType, boolean simulate, boolean verbose) throws Exception {
		if (this.data.getStatementTypeById(statementTypeId) == null) {
			throw new Exception("A statement type with ID " + statementTypeId + " was not found in the database.");
		}
		if (this.data.getStatementTypeById(statementTypeId).getVariables().containsKey(variable)) {
			throw new Exception("Variable '" + variable + "' already exists in statement type ID " + statementTypeId + ".");
		}
		if (!dataType.equals("short text") && !dataType.equals("long text") && !dataType.equals("integer") && !dataType.equals("boolean")) {
			throw new Exception("Data type is invalid.");
		}

		// report simulation mode
		if (verbose == true) {
			if (simulate == true) {
				System.out.println("Simulation mode: no actual changes are made to the database!");
			} else {
				System.out.println("Changes will be written both in memory and to the SQL database!");
			}
		}

		if (simulate == false) {
			this.data.getStatementTypeById(statementTypeId).getVariables().put(variable, dataType);
			this.sql.addVariable(variable, dataType, statementTypeId);
		}
		if (dataType.equals("short text") || dataType.equals("long text")) {
			AttributeVector av = new AttributeVector(this.data.generateNewId("attributes"), "", "#000000", "", "", "", "", statementTypeId, variable);
			if (simulate == false) {
				this.data.getAttributes().add(av);
				Collections.sort(this.data.getAttributes());
				this.sql.upsertAttributeVector(av);
			}
		}
		int updateStatementsCount = 0;
		for (int i = 0; i < this.data.getStatements().size(); i++) {
			if (this.data.getStatements().get(i).getStatementTypeId() == statementTypeId 
					&& !this.data.getStatements().get(i).getValues().containsKey(variable)) {
				if (simulate == false) {
					if (dataType.equals("short text") || dataType.equals("long text")) {
						this.data.getStatements().get(i).getValues().put(variable, "");
						this.sql.upsertVariableContent("", this.data.getStatements().get(i).getId(), variable, statementTypeId, dataType);
					} else if (dataType.equals("boolean")) {
						this.data.getStatements().get(i).getValues().put(variable, 1);
						this.sql.upsertVariableContent(1, this.data.getStatements().get(i).getId(), variable, statementTypeId, dataType);
					} else if (dataType.equals("integer")) {
						this.data.getStatements().get(i).getValues().put(variable, 0);
						this.sql.upsertVariableContent(0, this.data.getStatements().get(i).getId(), variable, statementTypeId, dataType);
					}
				}
				updateStatementsCount++;
			}
		}

		// report statistics
		if (verbose == true) {
			System.out.println("Added variables:    1");
			System.out.println("Added attributes:   1");
			System.out.println("Updated statements: " + updateStatementsCount);
		}
	}

	/**
	 * Remove a variable from a statement type, including attributes and statements.
	 * 
	 * @param statementTypeLabel  The String label of the statement type in which the variable is defined.
	 * @param variable            The name of the variable as a String.
	 * @param simulate            If true, changes are not actually carried out.
	 * @param verbose             Should statistics on updating process be reported?
	 * @throws Exception
	 */
	public void removeVariable(String statementTypeLabel, String variable, boolean simulate, boolean verbose) throws Exception {
		int statementTypeId = this.data.getStatementType(statementTypeLabel).getId();
		removeVariable(statementTypeId, variable, simulate, verbose);
	}
	
	/**
	 * Remove a variable from a statement type, including attributes and statements.
	 * 
	 * @param statementTypeId  The ID of the statement type in which the variable is defined.
	 * @param variable         The name of the variable as a String.
	 * @param simulate         If true, changes are not actually carried out.
	 * @param verbose          Should statistics on updating process be reported?
	 * @throws Exception
	 */
	public void removeVariable(int statementTypeId, String variable, boolean simulate, boolean verbose) throws Exception {
		if (!this.data.getStatementTypeById(statementTypeId).getVariables().containsKey(variable)) {
			throw new Exception("Variable '" + variable + "' does not exist in statement type " + statementTypeId + ".");
		}
		int removeFromStatementCounter = 0;
		int removeAttributeCounter = 0;
		for (int i = this.data.getAttributes().size() - 1; i > -1 ; i--) {
			if (this.data.getAttributes().get(i).getStatementTypeId() == statementTypeId && this.data.getAttributes().get(i).getVariable().equals(variable)) {
				if (simulate == false) {
					this.data.getAttributes().remove(i);
				}
				removeAttributeCounter++;
			}
		}
		for (int i = 0; i < this.data.getStatements().size(); i++) {
			if (this.data.getStatements().get(i).getStatementTypeId() == statementTypeId) {
				if (simulate == false) {
					this.data.getStatements().get(i).getValues().remove(variable);
				}
				removeFromStatementCounter++;
			}
		}
		if (simulate == false) {
			this.data.getStatementTypeById(statementTypeId).getVariables().remove(variable);
			this.sql.removeVariable(statementTypeId, variable);
		}

		// report statistics
		if (verbose == true) {
			System.out.println("Removed attributes: " + removeAttributeCounter);
			System.out.println("Updated statements: " + removeFromStatementCounter);
			System.out.println("Removed variables:  1");
		}
	}

	/**
	 * Rename a variable by providing a new String label, based on the statement type label and variable label as input.
	 * 
	 * @param statementTypeLabel  Label of the statement type to be renamed.
	 * @param variable            Old variable name.
	 * @param newLabel            New label as a string.
	 * @param simulate            If true, changes are not actually carried out.
	 * @param verbose             Should statistics on updating process be reported?
	 * @throws Exception 
	 */
	public void renameVariable(String statementTypeLabel, String variable, String newLabel, boolean simulate, boolean verbose) throws Exception {
		int statementTypeId = this.data.getStatementType(statementTypeLabel).getId();
		renameVariable(statementTypeId, variable, newLabel, simulate, verbose);
	}
	
	/**
	 * Rename a variable by providing a new String label, based on the statement type ID and variable label as input.
	 * 
	 * @param statementTypeId  ID of the statement type to be renamed.
	 * @param variable         Old variable name.
	 * @param newLabel         New label as a string.
	 * @param simulate         If true, changes are not actually carried out.
	 * @param verbose          Should statistics on updating process be reported?
	 * @throws Exception 
	 */
	public void renameVariable(int statementTypeId, String variable, String newLabel, boolean simulate, boolean verbose) throws Exception {
		if (this.data.getStatementTypeById(statementTypeId) == null) {
			throw new Exception("A statement type with ID " + statementTypeId + " was not found in the database.");
		}
		if (newLabel.contains(" ")) {
			throw new Exception("The new variable name contains spaces. This is not permitted.");
		}
		
		// report simulation mode
		if (verbose == true) {
			if (simulate == true) {
				System.out.println("Simulation mode: no actual changes are made to the database!");
			} else {
				System.out.println("Changes will be written both in memory and to the SQL database!");
			}
		}

		// update attributes
		int updateAttributeCounter = 0;
		for (int i = 0; i < this.data.getAttributes().size(); i++) {
			if (this.data.getAttributes().get(i).getStatementTypeId() == statementTypeId && this.data.getAttributes().get(i).getVariable().equals(variable)) {
				if (simulate == false) {
					this.data.getAttributes().get(i).setVariable(newLabel);
				}
				updateAttributeCounter++;
			}
		}
		
		// update statements
		int updateStatementCounter = 0;
		for (int i = 0; i < this.data.getStatements().size(); i++) {
			if (this.data.getStatements().get(i).getStatementTypeId() == statementTypeId) {
				if (simulate == false) {
					this.data.getStatements().get(i).getValues().put(newLabel, this.data.getStatements().get(i).getValues().get(variable));
					this.data.getStatements().get(i).getValues().remove(variable);
				}
				updateStatementCounter++;
			}
		}
		
		// update statement type
		if (simulate == false) {
			String dataType = this.data.getStatementTypeById(statementTypeId).getVariables().get(variable);
			this.data.getStatementTypeById(statementTypeId).getVariables().put(newLabel, dataType);
			this.data.getStatementTypeById(statementTypeId).getVariables().remove(variable);
		}
		
		// also change in the SQL database
		if (simulate == false) {
			this.sql.renameVariable(statementTypeId, variable, newLabel);
		}

		// report statistics
		if (verbose == true) {
			System.out.println("Updated attributes: " + updateAttributeCounter);
			System.out.println("Updated statements: " + updateStatementCounter);
			System.out.println("Updated variables:  1");
		}
	}

	/**
	 * Recast a variable from short text to long text, from long text to short text, from integer to boolean, or from 
	 * boolean to integer, including any necessary changes in statements and attributes. Based on statement type label
	 * 
	 * @param statementTypeLabel  Label of the statement type in which the variable is defined.
	 * @param variable            Name of the variable to be recast.
	 * @param simulate            If true, changes are not actually carried out.
	 * @param verbose             Should statistics on updating process be reported?
	 * @throws Exception
	 */
	public void recastVariable(String statementTypeLabel, String variable, boolean simulate, boolean verbose) throws Exception {
		int statementTypeId = this.data.getStatementType(statementTypeLabel).getId();
		recastVariable(statementTypeId, variable, simulate, verbose);
	}
	
	/**
	 * Recast a variable from short text to long text, from long text to short text, from integer to boolean, or from 
	 * boolean to integer, including any necessary changes in statements and attributes. Based on statement type ID.
	 * 
	 * @param statementTypeId  ID of the statement type in which the variable is defined.
	 * @param variable         Name of the variable to be recast.
	 * @param simulate         If true, changes are not actually carried out.
	 * @param verbose          Should statistics on updating process be reported?
	 * @throws Exception
	 */
	public void recastVariable(int statementTypeId, String variable, boolean simulate, boolean verbose) throws Exception {
		
		// check validity of input arguments
		if (this.data.getStatementTypeById(statementTypeId) == null) {
			throw new Exception("A statement type with ID " + statementTypeId + " was not found in the database.");
		}
		if (!this.data.getStatementTypeById(statementTypeId).getVariables().containsKey(variable)) {
			throw new Exception("Variable '" + variable + "' is undefined in statement type " + statementTypeId + ".");
		}
		
		// report simulation mode
		if (verbose == true) {
			if (simulate == true) {
				System.out.println("Simulation mode: no actual changes are made to the database!");
			} else {
				System.out.println("Changes will be written both in memory and to the SQL database!");
			}
		}

		// do the recoding, depending on which input data type is found
		int updateAttributeCounter = 0;
		int updateStatementCounter = 0;
		String oldDataType = this.data.getStatementTypeById(statementTypeId).getVariables().get(variable);
		if (oldDataType.equals("short text")) { // just change the data type; no other changes necessary
			if (simulate == false) {
				this.data.getStatementTypeById(statementTypeId).getVariables().put(variable, "long text");
				this.sql.recastVariable(statementTypeId, variable);
			}
		} else if (oldDataType.equals("long text")) { // cut off values in statements and attributes that are longer than 200 characters and change data type
			ArrayList<String> oldValues = new ArrayList<String>();
			String oldValue;
			if (this.data.getStatements().size() > 0) {
				for (int i = 0; i < this.data.getStatements().size(); i++) {
					if (((String) this.data.getStatements().get(i).getValues().get(variable)).length() > 200) {
						oldValue = (String) this.data.getStatements().get(i).getValues().get(variable);
						if (!oldValues.contains(oldValue)) {
							oldValues.add(oldValue);
						}
						if (simulate == false) {
							this.data.getStatements().get(i).getValues().put(variable, oldValue.substring(0, 200));
							this.sql.upsertVariableContent(oldValue.substring(0, 200), this.data.getStatements().get(i).getId(), variable, statementTypeId, "long text");
						}
						updateStatementCounter++;
					}
				}
			}
			if (oldValues.size() > 0) {
				int index;
				for (int i = 0; i < oldValues.size(); i++) {
					index = this.data.getAttributeIndex(oldValues.get(i), variable, statementTypeId);
					if (simulate == false) {
						this.data.getAttributes().get(index).setValue(oldValues.get(i).substring(0, 200));
						this.sql.updateAttribute(this.data.getAttributes().get(index).getId(), "Value", oldValues.get(i).substring(0, 200));
					}
					updateAttributeCounter++;
				}
			}
			if (simulate == false) {
				this.data.getStatementTypeById(statementTypeId).getVariables().put(variable, "short text");
				this.sql.recastVariable(statementTypeId, variable);
			}
		} else if (oldDataType.equals("integer")) { // recode statements into boolean and change data type; do not modify attributes
			ArrayList<Integer> oldValues = new ArrayList<Integer>();
			int oldValue;
			if (this.data.getStatements().size() > 0) {
				for (int i = 0; i < this.data.getStatements().size(); i++) {
					oldValue = (int) this.data.getStatements().get(i).getValues().get(variable);
					if (!oldValues.contains(oldValue)) {
						oldValues.add(oldValue);
					}
				}
				Collections.sort(oldValues);
			}
			if (oldValues.size() > 2) { // old integer variable has more than two values: fail
				System.err.print("Variable type could not be changed from integer to boolean because there are more than two values:");
				for (int i = 0; i < oldValues.size(); i++) {
					System.err.print(" " + oldValues.get(i));
				}
				System.err.println("\nUse the dna_setStatements() function to recode values.");
			} else if (oldValues.size() == 2) { // old integer variable has exactly two values: recode into 0 and 1 where necessary
				for (int i = 0; i < this.data.getStatements().size(); i++) {
					oldValue = (int) this.data.getStatements().get(i).getValues().get(variable);
					if (oldValue == oldValues.get(0) && oldValue != 0) {
						if (simulate == false) {
							this.data.getStatements().get(i).getValues().put(variable, 0);
							this.sql.upsertVariableContent(0, this.data.getStatements().get(i).getId(), variable, statementTypeId, "integer");
						}
						updateStatementCounter++;
					} else if (oldValue == oldValues.get(1) && oldValue != 1) {
						if (simulate == false) {
							this.data.getStatements().get(i).getValues().put(variable, 1);
							this.sql.upsertVariableContent(1, this.data.getStatements().get(i).getId(), variable, statementTypeId, "integer");
						}
						updateStatementCounter++;
					}
				}
			} else if (oldValues.size() == 1) { // old integer variable has only one value: recode that value into 1
				for (int i = 0; i < this.data.getStatements().size(); i++) {
					oldValue = (int) this.data.getStatements().get(i).getValues().get(variable);
					if (oldValue != 1) {
						if (simulate == false) {
							this.data.getStatements().get(i).getValues().put(variable, 1);
							this.sql.upsertVariableContent(1, this.data.getStatements().get(i).getId(), variable, statementTypeId, "integer");
						}
						updateStatementCounter++;
					}
				}
			} else {
				// no statements to recode
			}
			if (simulate == false) {
				this.data.getStatementTypeById(statementTypeId).getVariables().put(variable, "boolean");
				this.sql.recastVariable(statementTypeId, variable);
			}
		} else if (oldDataType.equals("boolean")) { // recode statements from [0; 1] to [-1; +1]
			if (simulate == false) {
				this.data.getStatementTypeById(statementTypeId).getVariables().put(variable, "integer");
				this.sql.recastVariable(statementTypeId, variable);
			}
			for (int i = 0; i < this.data.getStatements().size(); i++) {
				if ((int) this.data.getStatements().get(i).getValues().get(variable) == 0) {
					if (simulate = false) {
						this.data.getStatements().get(i).getValues().put(variable, -1);
						this.sql.upsertVariableContent(-1, this.data.getStatements().get(i).getId(), variable, statementTypeId, "integer");
					}
					updateStatementCounter++;
				}
			}
		} else {
			throw new Exception("Data type not recognized.");
		}
		
		// report statistics
		if (verbose == true) {
			System.out.println("Updated variables:  1");
			System.out.println("Updated attributes: " + updateAttributeCounter);
			System.out.println("Updated statements: " + updateStatementCounter);
		}
	}
}
