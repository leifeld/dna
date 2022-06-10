package export;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import dna.Dna;
import model.Entity;
import model.Matrix;
import model.Statement;
import model.StatementType;
import model.TableDocument;
import model.Value;

/**
 * Exporter class. This class contains functions for filtering statement array
 * lists, creating network matrices, and writing networks to files.
 */
public class Exporter {
	private StatementType statementType;
	private String networkType, variable1, variable2, qualifier, qualifierAggregation;
	private String normalization, duplicates, timeWindow;
	private boolean variable1Document, variable2Document, isolates;
	private LocalDateTime startDateTime, stopDateTime;
	private int windowSize;
	private HashMap<String, ArrayList<String>> excludeValues;
	private ArrayList<String> excludeAuthors, excludeSources, excludeSections, excludeTypes;
	private boolean invertValues, invertAuthors, invertSources, invertSections, invertTypes;
	private String fileFormat, outfile;
	
	private ArrayList<TableDocument> documents;
	private HashMap<Integer, Integer> docMap;
	private HashMap<String, String> dataTypes;
	private ArrayList<ExportStatement> originalStatements;
	private ArrayList<ExportStatement> filteredStatements;
	private ArrayList<Matrix> matrixResults;

	/**
	 * <p>Create a new Exporter class instance, holding an array list of export
	 * statements (i.e., statements with added document information and a hash
	 * map for easier access to variable values.
	 * 
	 * @param networkType The type of network to be exported. Valid values are:
	 *   <ul>
	 *     <li>{@code "twomode"} (to create a two-mode network)</li>
	 *     <li>{@code "onemode"} (to create a one-mode network)</li>
	 *     <li>{@code "eventlist"} (to create an event list)</li>
	 *   </ul>
	 * @param statementType The statement type.
	 * @param variable1 The name of the first variable, for example {@code
	 *   "organization"}. In addition to the variables defined in the statement
	 *   type, the document variables {@code author}, {@code source}, {@code
	 *   section}, {@code type}, {@code id}, and {@code title} are valid. If
	 *   document-level variables are used, this must be declared using the
	 *   {@code variable1Document} argument.
	 * @param variable1Document Is the first variable defined at the document
	 *   level, for instance the author or document ID?
	 * @param variable2 The name of the second variable, for example {@code
	 *   "concept"}. In addition to the variables defined in the statement type,
	 *   the document variables {@code author}, {@code source}, {@code section},
	 *   {@code type}, {@code id}, and {@code title} are valid. If
	 *   document-level variables are used, this must be declared using the
	 *   {@code variable2Document} argument.
	 * @param variable2Document Is the second variable defined at the document
	 *   level, for instance the author or document ID?
	 * @param qualifierName The qualifier variable, for example {@code
	 *  "agreement"}.
	 * @param qualifierAggregation The way in which the qualifier variable is
	 *   used to aggregate ties in the network.<br/>
	 *   Valid values if the {@code networkType} argument equals {@code
	 *   "onemode"} are:
	 *   <ul>
	 *     <li>{@code "ignore"} (for ignoring the qualifier variable)</li>
	 *     <li>{@code "congruence"} (for recording a network tie only if both
	 *       nodes have the same qualifier value in the binary case or for
	 *       recording the similarity between the two nodes on the qualifier
	 *       variable in the integer case)</li>
	 *     <li>{@code "conflict"} (for recording a network tie only if both
	 *       nodes have a different qualifier value in the binary case or for
	 *       recording the distance between the two nodes on the qualifier
	 *       variable in the integer case)</li>
	 *     <li>{@code "subtract"} (for subtracting the conflict tie value from
	 *       the congruence tie value in each dyad)</li>
	 *     <li>{@code "congruence & conflict"} (only applicable to time window
	 *       networks: add both a congruence and a conflict network to the time
	 *       window list of networks at each time step)</li>
	 *   </ul>
	 *   Valid values if the {@code networkType} argument equals {@code
	 *   "twomode"} are:
	 *   <ul>
	 *     <li>{@code "ignore"} (for ignoring the qualifier variable)</li>
	 *     <li>{@code "combine"} (for creating multiplex combinations, e.g.,
	 *       {@code 1} for positive, {@code 2} for negative, and {@code 3} for
	 *       mixed)</li>
	 *     <li>{@code "subtract"} (for subtracting negative from positive
	 *       ties)</li>
	 *   </ul>
	 *   The argument is ignored if {@code networkType} equals {@code
	 *   "eventlist"}.
	 * @param normalization Normalization of edge weights. Valid settings for
	 *   <em>one-mode</em> networks are:
     *   <ul>
	 *     <li>{@code "no"} (for switching off normalization)</li>
	 *     <li>{@code "average"} (for average activity normalization)</li>
	 *     <li>{@code "jaccard"} (for Jaccard coefficient normalization)</li>
	 *     <li>{@code "cosine"} (for cosine similarity normalization)</li>
	 *   </ul>
	 *   Valid settings for <em>two-mode</em> networks are:
	 *   <ul>
	 *     <li>{@code "no"} (for switching off normalization)</li>
	 *     <li>{@code "activity"} (for activity normalization)</li>
	 *     <li>{@code "prominence"} (for prominence normalization)</li>
	 *   </ul>
	 * @param isolates Should all nodes of the respective variable be included
	 *    in the network matrix ({@code true}), or should only those nodes be
	 *    included that are active in the current time period and are not
	 *    excluded ({@code false})?
	 * @param duplicates Setting for excluding duplicate statements before
	 *   network construction. Valid values are:
	 *   <ul>
	 *     <li>{@code "include"} (for including all statements in network
	 *       construction)</li>
	 *     <li>{@code "document"} (for counting only one identical statement per
	 *       document)</li>
	 *     <li>{@code "week"} (for counting only one identical statement per
	 *       calendar week as defined in the UK locale, i.e., Monday to Sunday)
	 *       </li>
	 *     <li>{@code "month"} (for counting only one identical statement per
	 *       calendar month)</li>
	 *     <li>{@code "year"} (for counting only one identical statement per
	 *       calendar year)</li>
	 *     <li>{@code "acrossrange"} (for counting only one identical statement
	 *       across the whole time range)</li>
	 *   </ul>
	 * @param startDateTime The start date and time for network construction.
	 *   All statements before this specified date/time will be excluded.
	 * @param stopDateTime The stop date and time for network construction.
	 *   All statements after this specified date/time will be excluded.
	 * @param timeWindow If any of the time units is selected, a moving time
	 *    window will be imposed, and only the statements falling within the
	 *    time period defined by the window will be used to create the network.
	 *    The time window will then be moved forward by one time unit at a time,
	 *    and a new network with the new time boundaries will be created. This
	 *    is repeated until the end of the overall time span is reached. All
	 *    time windows will be saved as separate network matrices in a list. The
	 *    duration of each time window is defined by the {@code windowsize}
	 *    argument. For example, this could be used to create a time window of
	 *    six months that moves forward by one month each time, thus creating
	 *    time windows that overlap by five months. If {@code "events"} is used
	 *    instead of a natural time unit, the time window will comprise exactly
	 *    as many statements as defined in the {@code windowsize} argument.
	 *    However, if the start or end statement falls on a date and time where
	 *    multiple events happen, those additional events that occur
	 *    simultaneously are included because there is no other way to decide
	 *    which of the statements should be selected. Therefore the window size
	 *    is sometimes extended when the start or end point of a time window is
	 *    ambiguous in event time. Valid argument values are:
	 *   <ul>
	 *     <li>{@code "no"} (no time window will be used)</li>
	 *     <li>{@code "events"} (time window length = number of statements)</li>
	 *     <li>{@code "seconds"} (number of seconds)</li>
	 *     <li>{@code "minutes"} (number of minutes)</li>
	 *     <li>{@code "hours"} (number of hours)</li>
	 *     <li>{@code "days"} (number of days)</li>
	 *     <li>{@code "weeks"} (number of calendar weeks)</li>
	 *     <li>{@code "months"} (number of calendar months)</li>
	 *     <li>{@code "years"} (number of calendar years)</li>
	 *   </ul>
	 *   @param windowSize The number of time units of which a moving time
	 *     window is comprised. This can be the number of statement events, the
	 *     number of days etc., as defined in the {@code timeWindow} argument.
	 *   @param excludeValues A hash map that contains values which should be
	 *     excluded during network construction. The hash map is indexed by
	 *     variable name (for example, {@code "organization"} as the key, and
	 *     the corresponding value is an array list of values to exclude, for
	 *     example {@code "org A"} or {@code "org B"}. This is irrespective of
	 *     whether these values appear in {@code variable1}, {@code variable2},
	 *     or the {@code qualifier} variable. Note that only variables at the
	 *     statement level can be used here. There are separate arguments for
	 *     excluding statements nested in documents with certain meta-data.
	 *   @param excludeAuthors An array of authors. If a statement is nested in
	 *     a document where one of these authors is set in the {@code author}
	 *     meta-data field, the statement is excluded from network construction.
	 *   @param excludeSources An array of sources. If a statement is nested in
	 *     a document where one of these sources is set in the {@code source}
	 *     meta-data field, the statement is excluded from network construction.
	 *   @param excludeSections An array of sections. If a statement is nested
	 *     in a document where one of these sections is set in the {@code
	 *     section} meta-data field, the statement is excluded from network
	 *     construction.
	 *   @param excludeTypes An array of types. If a statement is nested in a
	 *     document where one of these types is set in the {@code type}
	 *     meta-data field, the statement is excluded from network construction.
	 *   @param invertValues Indicates whether the entries provided by the
	 *     {@code excludeValues} argument should be excluded from network
	 *     construction ({@code false}) or if they should be the only values
	 *     that should be included during network construction ({@code true}).
	 *   @param invertAuthors Indicates whether the values provided by the
	 *     {@code excludeAuthors} argument should be excluded from network
	 *     construction ({@code false}) or if they should be the only values
	 *     that should be included during network construction ({@code true}).
	 *   @param invertSources Indicates whether the values provided by the
	 *     {@code excludeSources} argument should be excluded from network
	 *     construction ({@code false}) or if they should be the only values
	 *     that should be included during network construction ({@code true}).
	 *   @param invertSections Indicates whether the values provided by the
	 *     {@code excludeSections} argument should be excluded from network
	 *     construction ({@code false}) or if they should be the only values
	 *     that should be included during network construction ({@code true}).
	 *   @param invertTypes Indicates whether the values provided by the
	 *     {@code excludeTypes} argument should be excluded from network
	 *     construction ({@code false}) or if they should be the only values
	 *     that should be included during network construction ({@code true}).
	 *   @param fileFormat The file format specification for saving the
	 *     resulting network(s) to a file instead of returning an object. Valid
	 *     values are:
	 *     <ul>
	 *       <li>{@code "csv"} (for network matrices or event lists)</li>
	 *       <li>{@code "dl"} (for UCINET DL full-matrix files)</li>
	 *       <li>{@code "graphml"} (for visone {@code .graphml} files; this
	 *         specification is also compatible with time windows)</li>
	 *     </ul>
	 *   @param outfile The file name for saving the network.
	 */
	public Exporter(
			String networkType,
			StatementType statementType,
			String variable1,
			boolean variable1Document,
			String variable2,
			boolean variable2Document,
			String qualifier,
			String qualifierAggregation,
			String normalization,
			boolean isolates,
			String duplicates,
			LocalDateTime startDateTime,
			LocalDateTime stopDateTime,
			String timeWindow,
			int windowSize,
			HashMap<String, ArrayList<String>> excludeValues,
			ArrayList<String> excludeAuthors,
			ArrayList<String> excludeSources,
			ArrayList<String> excludeSections,
			ArrayList<String> excludeTypes,
			boolean invertValues,
			boolean invertAuthors,
			boolean invertSources,
			boolean invertSections,
			boolean invertTypes,
			String fileFormat,
			String outfile) {
		this.networkType = networkType;
		this.statementType = statementType;
		this.variable1 = variable1;
		this.variable1Document = variable1Document;
		this.variable2 = variable2;
		this.variable2Document = variable2Document;
		this.qualifier = qualifier;
		this.qualifierAggregation = qualifierAggregation;
		this.normalization = normalization;
		this.isolates = isolates;
		this.duplicates = duplicates;
		this.startDateTime = startDateTime;
		this.stopDateTime = stopDateTime;
		this.timeWindow = timeWindow;
		this.windowSize = windowSize;
		this.excludeValues = excludeValues;
		this.invertValues = invertValues;
		this.excludeAuthors = excludeAuthors;
		this.invertAuthors = invertAuthors;
		this.excludeSources = excludeSources;
		this.invertSources = invertSources;
		this.excludeSections = excludeSections;
		this.invertSections = invertSections;
		this.excludeTypes = excludeTypes;
		this.invertTypes = invertTypes;
		this.fileFormat = fileFormat;
		this.outfile = outfile;
	}
	
	/**
	 * Load statements and documents from the database and pre-process them.
	 */
	public void loadData() {
		// put variable data types into a map for quick lookup
		this.dataTypes = new HashMap<String, String>();
		for (int i = 0; i < this.statementType.getVariables().size(); i++) {
			this.dataTypes.put(this.statementType.getVariables().get(i).getKey(), this.statementType.getVariables().get(i).getDataType());
		}

		// get documents and create document hash map for quick lookup
		this.documents = Dna.sql.getTableDocuments(new int[0]);
		Collections.sort(documents);
		this.docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}

		// get statements and convert to {@link ExportStatement} objects with additional information
		this.originalStatements = Dna.sql.getStatements(new int[0],
				new int[] {this.statementType.getId()},
				this.startDateTime,
				this.stopDateTime,
				this.excludeAuthors,
				this.invertAuthors,
				this.excludeSources,
				this.invertSources,
				this.excludeSections,
				this.invertSections,
				this.excludeTypes,
				this.invertTypes)
		.stream()
		.map(s -> {
			int docIndex = docMap.get(s.getDocumentId());
			return new ExportStatement(s,
					documents.get(docIndex).getTitle(),
					documents.get(docIndex).getAuthor(),
					documents.get(docIndex).getSource(),
					documents.get(docIndex).getSection(),
					documents.get(docIndex).getType());
		})
		.collect(Collectors.toCollection(ArrayList::new));
	}
	
	/**
	 * Extract the labels for all nodes for a variable from the statements,
	 * conditional on isolates settings.
	 * 
	 * @param processedStatements These are usually filtered statements, but
	 *   could be more processed than just filtered, for example for
	 *   constructing time window sequences of network matrices.
	 * @param variable String indicating the variable for which labels should be
	 *   extracted, for example {@code "organization"}.
	 * @param variableDocument Is the variable a document-level variable?
	 * @param includeIsolates Indicates whether all nodes should be included or
	 *   just those after applying the statement filter.
	 * @return String array containing all sorted node names.
	 */
	private String[] extractLabels(
			ArrayList<ExportStatement> processedStatements,
			String variable,
			boolean variableDocument,
			boolean includeIsolates) {
		
		// decide whether to use the original statements or the filtered statements
		ArrayList<ExportStatement> finalStatements;
		if (this.isolates) {
			finalStatements = originalStatements;
		} else {
			finalStatements = processedStatements;
		}
		
		// go through statements and extract names
		ArrayList<String> names = new ArrayList<String>();
		String n = null;
		ExportStatement es;
		for (int i = 0; i < finalStatements.size(); i++) {
			es = finalStatements.get(i);
			if (variableDocument) {
				if (variable.equals("author")) {
					n = es.getAuthor();
				} else if (variable.equals("source")) {
					n = es.getSource();
				} else if (variable.equals("section")) {
					n = es.getSection();
				} else if (variable.equals("type")) {
					n = es.getType();
				} else if (variable.equals("id")) {
					n = es.getDocumentIdAsString();
				} else if (variable.equals("title")) {
					n = es.getTitle();
				}
			} else {
				n = (String) es.get(variable).toString();
			}
			if (!names.contains(n)) {
				names.add(n);
			}
		}
		
		// sort and convert to array, then return
		Collections.sort(names);
		if (names.size() > 0 && names.get(0).equals("")) { // remove empty field
			names.remove(0);
		}
		String[] nameArray = new String[names.size()];
		if (names.size() > 0) {
			for (int i = 0; i < names.size(); i++) {
				nameArray[i] = names.get(i);
			}
		}
		return nameArray;
	}

	/**
	 * Filter the statements based on the {@link #originalStatements} slot of
	 * the class and create a filtered statement list, which is saved in the
	 * {@link #filteredStatements} slot of the class. 
	 */
	public void filterStatements() {
		// create a deep copy of the original statements
		this.filteredStatements = new ArrayList<ExportStatement>();
		for (int i = 0; i < this.originalStatements.size(); i++) {
			this.filteredStatements.add(new ExportStatement(originalStatements.get(i)));
		}
		
		// sort statements by date and time
		Collections.sort(this.filteredStatements);
		
		// Create arrays with variable values
		String[] values1 = retrieveValues(this.filteredStatements, this.variable1, this.variable1Document);
		String[] values2 = retrieveValues(this.filteredStatements, this.variable2, this.variable2Document);
		
		// process and exclude statements
		ExportStatement s;
		ArrayList<ExportStatement> al = new ArrayList<ExportStatement>();
	    String previousVar1 = null;
	    String previousVar2 = null;
	    LocalDateTime cal, calPrevious;
	    int year, month, week, yearPrevious, monthPrevious, weekPrevious;
		for (int i = 0; i < this.filteredStatements.size(); i++) {
			boolean select = true;
			s = this.filteredStatements.get(i);

			// check against excluded values
			Iterator<String> keyIterator = this.excludeValues.keySet().iterator();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				String string = "";
				if (dataTypes.get(key) == null) {
					throw new NullPointerException("'" + key + "' is not a statement-level variable and cannot be excluded.");
				} else if (dataTypes.get(key).equals("boolean") || dataTypes.get(key).equals("integer")) {
					string = String.valueOf(s.get(key));
				} else if (dataTypes.get(key).equals("short text")) {
					string = ((Entity) s.get(key)).getValue();
				} else if (dataTypes.get(key).equals("long text")) {
					string = (String) s.get(key);
				}
				if ((this.excludeValues.get(key).contains(string) && !this.invertValues) ||
						(!this.excludeValues.get(key).contains(string) && this.invertValues)) {
					select = false;
				}
			}

			// check against empty fields
			if (select &&
					!this.networkType.equals("eventlist") &&
					(values1[i].equals("") || values2[i].equals(""))) {
				select = false;
			}
			
			// check for duplicates
			cal = s.getDateTime();
		    year = cal.getYear();
		    month = cal.getMonthValue();
		    @SuppressWarnings("static-access")
			WeekFields weekFields = WeekFields.of(Locale.UK.getDefault()); // use UK definition of calendar weeks
			week = cal.get(weekFields.weekOfWeekBasedYear());
			if (!this.duplicates.equals("include all duplicates")) {
				for (int j = al.size() - 1; j >= 0; j--) {
				    if (!this.variable1Document) {
				    	previousVar1 = ((Entity) al.get(j).get(this.variable1)).getValue();
				    } else if (this.variable1.equals("author")) {
				    	previousVar1 = al.get(j).getAuthor();
				    } else if (this.variable1.equals("source")) {
				    	previousVar1 = al.get(j).getSource();
				    } else if (this.variable1.equals("section")) {
				    	previousVar1 = al.get(j).getSection();
				    } else if (this.variable1.equals("type")) {
				    	previousVar1 = al.get(j).getType();
				    } else if (this.variable1.equals("id")) {
				    	previousVar1 = al.get(j).getDocumentIdAsString();
				    } else if (this.variable1.equals("title")) {
				    	previousVar1 = al.get(j).getTitle();
				    }
				    if (!this.variable2Document) {
				    	previousVar2 = ((Entity) al.get(j).get(this.variable2)).getValue();
				    } else if (this.variable2.equals("author")) {
				    	previousVar2 = al.get(j).getAuthor();
				    } else if (this.variable2.equals("source")) {
				    	previousVar2 = al.get(j).getSource();
				    } else if (this.variable2.equals("section")) {
				    	previousVar2 = al.get(j).getSection();
				    } else if (this.variable2.equals("type")) {
				    	previousVar2 = al.get(j).getType();
				    } else if (this.variable2.equals("id")) {
				    	previousVar2 = al.get(j).getDocumentIdAsString();
				    } else if (this.variable2.equals("title")) {
				    	previousVar2 = al.get(j).getTitle();
				    }
				    calPrevious = al.get(j).getDateTime();
				    yearPrevious = calPrevious.getYear();
				    monthPrevious = calPrevious.getMonthValue();
				    @SuppressWarnings("static-access")
					WeekFields weekFieldsPrevious = WeekFields.of(Locale.UK.getDefault()); // use UK definition of calendar weeks
					weekPrevious = calPrevious.get(weekFieldsPrevious.weekOfWeekBasedYear());
					if ( s.getStatementTypeId() == al.get(j).getStatementTypeId()
							&& (al.get(j).getDocumentId() == s.getDocumentId() && duplicates.equals("ignore per document") 
								|| duplicates.equals("ignore across date range")
								|| (duplicates.equals("ignore per calendar year") && year == yearPrevious)
								|| (duplicates.equals("ignore per calendar month") && month == monthPrevious)
								|| (duplicates.equals("ignore per calendar week") && week == weekPrevious) )
							&& values1[i].equals(previousVar1)
							&& values2[i].equals(previousVar2)
							&& (this.qualifierAggregation.equals("ignore") || s.get(this.qualifier).equals(al.get(j).get(this.qualifier))) ) {
						select = false;
						break;
					}
				}
			}
			
			// add only if the statement passed all checks
			if (select == true) {
				al.add(s);
			}
		}
		this.filteredStatements = al;
	}
	
	/**
	 * Retrieve the values across statements/documents given the name of the
	 * variable. E.g., provide a variable name and information on whether the
	 * variable is defined at the document level (e.g., author or section) or at
	 * the statement level (e.g., organization), and return a one-dimensional
	 * array of values (e.g., the organization names or authors for all
	 * statements provided.
	 * 
	 * @param statements Original or filtered array list of statements.
	 * @param variable String denoting the first variable (containing the row
	 *   values).
	 * @param documentLevel Indicates if the first variable is at the document
	 *   level.
	 * @return String array of values.
	 */
	private String[] retrieveValues(ArrayList<ExportStatement> statements, String variable, boolean documentLevel) {
		ExportStatement s;
		String[] values = new String[statements.size()];
		for (int i = 0; i < statements.size(); i++) {
			s = statements.get(i);
			if (documentLevel) {
				if (variable.equals("author")) {
					values[i] = s.getAuthor();
				} else if (variable.equals("source")) {
					values[i] = s.getSource();
				} else if (variable.equals("section")) {
					values[i] = s.getSection();
				} else if (variable.equals("type")) {
					values[i] = s.getType();
				} else if (variable.equals("id")) {
					values[i] = s.getDocumentIdAsString();
				} else if (variable.equals("title")) {
					values[i] = s.getTitle();
				}
			} else {
				values[i] = (String) ((Entity) s.get(variable)).getValue();
			}
		}
		return values;
	}

	/**
	 * Count how often a value is used across the range of filtered statements.
	 * 
	 * @param variableValues String array of all values of a certain variable in
	 *   a set of statements.
	 * @param uniqueNames String array of unique values of the same variable
	 *   across all statements.
	 * @return {@link int} array of value frequencies for each unique value in
	 *   same order as {@code uniqueNames}.
	 */
	private int[] countFrequencies(String[] variableValues, String[] uniqueNames) {
		int[] frequencies = new int[uniqueNames.length];
		for (int i = 0; i < uniqueNames.length; i++) {
			for (int j = 0; j < variableValues.length; j++) {
				if (uniqueNames[i].equals(variableValues[j])) {
					frequencies[i] = frequencies[i] + 1;
				}
			}
		}
		return frequencies;
	}

	/**
	 * Lexical ranking of a binary vector.
	 * 
	 * Examples:
	 * 
	 * [0, 0] -> 0
	 * [0, 1] -> 1
	 * [1, 0] -> 2
	 * [1, 1] -> 3
	 * [0, 0, 1, 0, 1, 0] -> 10
	 * 
	 * This bijection is used to map combinations of qualifier values into edge
	 * weights in the resulting network matrix.
	 * 
	 * Source: https://cw.fel.cvut.cz/wiki/_media/courses/b4m33pal/pal06.pdf
	 * 
	 * @param binaryVector A binary int array of arbitrary length, indicating
	 *   which qualifier values are used in the dataset
	 * @return An integer
	 */
	private int lexRank(int[] binaryVector) {
		int n = binaryVector.length;
		int r = 0;
		for (int i = 0; i < n; i++) {
			if (binaryVector[i] > 0) {
				r = r + (int) Math.pow(2, n - i - 1);
			}
		}
		return r;
	}
	
	/**
	 * Create a three-dimensional array (variable 1 x variable 2 x qualifier).
	 * 
	 * @param names1 {@link String} array containing the row labels.
	 * @param names2 {@link String} array containing the column labels.
	 * @return 3D double array.
	 */
	private double[][][] createArray(ArrayList<ExportStatement> processedStatements,
			String[] names1, String[] names2) {
		
		int[] qualifierValues; // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (qualifier == null) {
			qualifierValues = null;
		} else if (this.dataTypes.get(qualifier).equals("integer")) {
			qualifierValues = this.originalStatements
					.stream()
					.mapToInt(s -> (int) s.get(qualifier))
					.distinct()
					.sorted()
					.toArray();
		} else {
			qualifierValues = new int[] {0, 1};
		}

		// Create arrays with variable values
		String[] values1 = retrieveValues(processedStatements, variable1, variable1Document);
		String[] values2 = retrieveValues(processedStatements, variable2, variable2Document);
		
		// create and populate array
		double[][][] array;
		if (qualifierValues == null) {
			array = new double[names1.length][names2.length][1];
		} else {
			array = new double[names1.length][names2.length][qualifierValues.length]; // 3D array: rows x cols x qualifier value
		}
		for (int i = 0; i < processedStatements.size(); i++) {
			String n1 = values1[i]; // retrieve first value from statement
			String n2 = values2[i]; // retrieve second value from statement
			int q;
			if (qualifier == null) {
				q = 0;
			} else {
				q = (int) processedStatements.get(i).get(qualifier); // retrieve qualifier value from statement
			}
			
			// find out which matrix row corresponds to the first value
			int row = -1;
			for (int j = 0; j < names1.length; j++) {
				if (names1[j].equals(n1)) {
					row = j;
					break;
				}
			}
			
			// find out which matrix column corresponds to the second value
			int col = -1;
			for (int j = 0; j < names2.length; j++) {
				if (names2[j].equals(n2)) {
					col = j;
					break;
				}
			}
			
			// find out which qualifier level corresponds to the qualifier value
			int qual = -1;
			if (qualifierValues == null) {
				qual = 0;
			} else { // qualifier level in the array
				for (int j = 0; j < qualifierValues.length; j++) {
					if (qualifierValues[j] == q) {
						qual = j;
						break;
					}
				}
			}
			
			// add match to matrix (note that duplicates were dealt with at the statement filter stage)
			array[row][col][qual] = array[row][col][qual] + 1.0;
		}
		
		return array;
	}
	
	/**
	 * Compute the results. Choose the right method based on the settings.
	 * 
	 * @throws Exception
	 */
	public void computeResults() throws Exception {
		if (networkType.equals("onemode") && timeWindow.equals("no")) {
			computeOneModeMatrix();
		} else if (networkType.equals("twomode") && timeWindow.equals("no")) {
			computeTwoModeMatrix(true);
		} else if (!networkType.equals("eventlist") && !timeWindow.equals("no")) {
			computeTimeWindowMatrices();
		}
	}
	
	/**
	 * Wrapper method to compute one-mode network matrix with class settings.
	 */
	private void computeOneModeMatrix() {
		ArrayList<Matrix> matrices = new ArrayList<Matrix>(); 
		matrices.add(this.computeOneModeMatrix(this.filteredStatements,
				this.qualifierAggregation, this.startDateTime, this.stopDateTime));
		this.matrixResults = matrices;
	}
	
	/**
	 * Create a one-mode network {@link Matrix}.
	 * 
	 * @param processedStatements Usually the filtered list of export
	 *   statements, but it can be a more processed list of export statements,
	 *   for example for use in constructing a time window sequence of networks.
	 * @param aggregation Qualifier aggregation; usually the qualifier
	 *   aggregation in the constructor/class field ({@link
	 *   #qualifierAggregation}), but it can deviate from it, for example in the
	 *   time window functionality, where multiple versions need to be created.
	 * @param start Start date/time.
	 * @param stop End date/time.
	 * @return {@link model.Matrix Matrix} object containing a one-mode network
	 *   matrix.
	 */
	private Matrix computeOneModeMatrix(ArrayList<ExportStatement> processedStatements, String aggregation, LocalDateTime start, LocalDateTime stop) {
		String[] names1 = this.extractLabels(processedStatements, this.variable1, this.variable1Document, this.isolates);
		String[] names2 = this.extractLabels(processedStatements, this.variable2, this.variable2Document, this.isolates);
		
		if (processedStatements.size() == 0) {
			double[][] m = new double[names1.length][names1.length];
			Matrix mt = new Matrix(m, names1, names1, true, start, stop);
			return mt;
		}
		
		boolean booleanQualifier = true;  // is the qualifier boolean, rather than integer or null?
		if (qualifier == null || dataTypes.get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (qualifier == null) {
			qualifierValues = new int[] { 0 };
		} else if (dataTypes.get(qualifier).equals("integer")) {
			qualifierValues = this.originalStatements
					.stream()
					.mapToInt(s -> (int) s.get(qualifier))
					.distinct()
					.sorted()
					.toArray();
		} else {
			qualifierValues = new int[] {0, 1};
		}
		
		double[][][] array = createArray(processedStatements, names1, names2);
		
		double[][] mat1 = new double[names1.length][names1.length];  // square matrix for "congruence" (or "ignore") results
		double[][] mat2 = new double[names1.length][names1.length];  // square matrix for "conflict" results
		double[][] m = new double[names1.length][names1.length];  // square matrix for final results
		double range = Math.abs(qualifierValues[qualifierValues.length - 1] - qualifierValues[0]);
		double i1count = 0.0;
		double i2count = 0.0;
		for (int i1 = 0; i1 < names1.length; i1++) {
			for (int i2 = 0; i2 < names1.length; i2++) {
				if (i1 != i2) {
					for (int j = 0; j < names2.length; j++) {
						// "ignore": sum up i1 and i2 independently over levels of k, then multiply.
						// In the binary case, this amounts to counting how often each concept is used and then multiplying frequencies.
						if (aggregation.equals("ignore")) {
							i1count = 0.0;
							i2count = 0.0;
							for (int k = 0; k < qualifierValues.length; k++) {
								i1count = i1count + array[i1][j][k];
								i2count = i2count + array[i2][j][k];
							}
							mat1[i1][i2] = mat1[i1][i2] + i1count * i2count;
						}
						// "congruence": sum up proximity of i1 and i2 per level of k, weighted by joint usage.
						// In the binary case, this reduces to the sum of weighted matches per level of k
						if (aggregation.equals("congruence") || aggregation.equals("subtract")) {
							for (int k1 = 0; k1 < qualifierValues.length; k1++) {
								for (int k2 = 0; k2 < qualifierValues.length; k2++) {
									mat1[i1][i2] = mat1[i1][i2] + (array[i1][j][k1] * array[i2][j][k2] * (1.0 - ((Math.abs(qualifierValues[k1] - qualifierValues[k2]) / range))));
								}
							}
						}
						// "conflict": same as congruence, but distance instead of proximity
						if (aggregation.equals("conflict") || aggregation.equals("subtract")) {
							for (int k1 = 0; k1 < qualifierValues.length; k1++) {
								for (int k2 = 0; k2 < qualifierValues.length; k2++) {
									mat2[i1][i2] = mat2[i1][i2] + (array[i1][j][k1] * array[i2][j][k2] * ((Math.abs(qualifierValues[k1] - qualifierValues[k2]) / range)));
								}
							}
						}
					}
					
					// normalization
					double norm = 1.0;
					if (this.normalization.equals("no")) {
						norm = 1.0;
					} else if (this.normalization.equals("average")) {
						i1count = 0.0;
						i2count = 0.0;
						for (int j = 0; j < names2.length; j++) {
							for (int k = 0; k < qualifierValues.length; k++) {
								i1count = i1count + array[i1][j][k];
								i2count = i2count + array[i2][j][k];
							}
						}
						norm = (i1count + i2count) / 2;
					} else if (this.normalization.equals("jaccard")) {
						double m10 = 0.0;
						double m01 = 0.0;
						double m11 = 0.0;
						for (int j = 0; j < names2.length; j++) {
							for (int k = 0; k < qualifierValues.length; k++) {
								if (array[i2][j][k] == 0) {
									m10 = m10 + array[i1][j][k];
								}
								if (array[i1][j][k] == 0) {
									m01 = m01 + array[i2][j][k];
								}
								if (array[i1][j][k] > 0 && array[i2][j][k] > 0) {
									m11 = m11 + (array[i1][j][k] * array[i2][j][k]);
								}
							}
						}
						norm = m01 + m10 + m11;
					} else if (this.normalization.equals("cosine")) {
						i1count = 0.0;
						i2count = 0.0;
						for (int j = 0; j < names2.length; j++) {
							for (int k = 0; k < qualifierValues.length; k++) {
								i1count = i1count + array[i1][j][k];
								i2count = i2count + array[i2][j][k];
							}
						}
						norm = Math.sqrt(i1count * i1count) * Math.sqrt(i2count * i2count);
					}
					mat1[i1][i2] = mat1[i1][i2] / norm;
					mat2[i1][i2] = mat2[i1][i2] / norm;
					
					// "subtract": congruence minus conflict; use the appropriate matrix or matrices
					if (aggregation.equals("ignore")) {
						m[i1][i2] = mat1[i1][i2];
					} else if (aggregation.equals("congruence")) {
						m[i1][i2] = mat1[i1][i2];
					} else if (aggregation.equals("conflict")) {
						m[i1][i2] = mat2[i1][i2];
					} else if (aggregation.equals("subtract")) {
						m[i1][i2] = mat1[i1][i2] - mat2[i1][i2];
					}
				}
			}
		}
		
		boolean integerBoolean;
		if (this.normalization.equals("no") && booleanQualifier == true) {
			integerBoolean = true;
		} else {
			integerBoolean = false;
		}
		
		Matrix matrix = new Matrix(m, names1, names1, integerBoolean, start, stop);
		return matrix;
	}

	/**
	 * Wrapper method to compute two-mode network matrix with class settings.
	 */
	public void computeTwoModeMatrix(boolean verbose) {
		ArrayList<Matrix> matrices = new ArrayList<Matrix>(); 
		matrices.add(this.computeTwoModeMatrix(this.filteredStatements,
				this.startDateTime, this.stopDateTime, verbose));
		this.matrixResults = matrices;
	}
	
	/**
	 * Create a two-mode network {@link Matrix}.
	 * 
	 * @param processedStatements Usually the filtered list of export
	 *   statements, but it can be a more processed list of export statements,
	 *   for example for use in constructing a time window sequence of networks.
	 * @param start Start date/time.
	 * @param stop End date/time.
	 * @param verbose Report details?
	 * @return {@link model.Matrix Matrix} object containing a two-mode network
	 *   matrix.
	 */
	private Matrix computeTwoModeMatrix(ArrayList<ExportStatement> processedStatements,
			LocalDateTime start, LocalDateTime stop, boolean verbose) {
		String[] names1 = this.extractLabels(processedStatements, this.variable1, this.variable1Document, this.isolates);
		String[] names2 = this.extractLabels(processedStatements, this.variable2, this.variable2Document, this.isolates);
		
		if (processedStatements.size() == 0) {
			double[][] m = new double[names1.length][names2.length];
			Matrix mt = new Matrix(m, names1, names2, true, start, stop);
			return mt;
		}
		
		boolean booleanQualifier = true; // is the qualifier boolean, rather than integer or null?
		if (qualifier == null) {
			booleanQualifier = false;
		} else if (dataTypes.get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues; // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (qualifier == null) {
			qualifierValues = new int[] { 0 };
		} else if (dataTypes.get(qualifier).equals("integer")) {
			qualifierValues = this.originalStatements
					.stream()
					.mapToInt(s -> (int) s.get(qualifier))
					.distinct()
					.sorted()
					.toArray();
		} else {
			qualifierValues = new int[] {0, 1};
		}
		
		double[][][] array = createArray(processedStatements, names1, names2);
		
		// combine levels of the qualifier variable conditional on qualifier aggregation option
		double[][] mat = new double[names1.length][names2.length];  // initialized with zeros
		HashMap<Integer, ArrayList<Integer>> combinations = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < names1.length; i++) {
			for (int j = 0; j < names2.length; j++) {
				if (this.qualifierAggregation.equals("combine")) { // combine
					double[] vec = array[i][j]; // may be weighted, so create a second, binary vector vec2
					int[] vec2 = new int[vec.length];
					ArrayList<Integer> qualVal = new ArrayList<Integer>(); // a list of qualifier values used at mat[i][j]
					for (int k = 0; k < vec.length; k++) {
						if (vec[k] > 0) {
							vec2[k] = 1;
							qualVal.add(qualifierValues[k]);
						}
					}
					mat[i][j] = lexRank(vec2); // compute lexical rank, i.e., map the combination of values to a single integer
					combinations.put(lexRank(vec2), qualVal); // the bijection needs to be stored for later reporting
				} else {
					for (int k = 0; k < qualifierValues.length; k++) {
						if (this.qualifierAggregation.equals("ignore")) { // ignore
							mat[i][j] = mat[i][j] + array[i][j][k]; // duplicates were already filtered out in the statement filter, so just add
						} else if (this.qualifierAggregation.equals("subtract")) { // subtract
							if (booleanQualifier == false && qualifierValues[k] < 0) { // subtract weighted absolute value
								mat[i][j] = mat[i][j] - (Math.abs(qualifierValues[k]) * array[i][j][k]);
							} else if (booleanQualifier == false && qualifierValues[k] >= 0) { // add weighted absolute value
								mat[i][j] = mat[i][j] + (Math.abs(qualifierValues[k]) * array[i][j][k]);
							} else if (booleanQualifier == true && qualifierValues[k] == 0) { // subtract 1 at most
								mat[i][j] = mat[i][j] - array[i][j][k];
							} else if (booleanQualifier == true && qualifierValues[k] > 0) { // add 1 at most
								mat[i][j] = mat[i][j] + array[i][j][k];
							}
						}
					}
				}
			}
		}
		
		// report combinations if necessary
		if (combinations.size() > 0) {
			Iterator<Integer> keyIterator = combinations.keySet().iterator();
			while (keyIterator.hasNext()){
				Integer key = (Integer) keyIterator.next();
				ArrayList<Integer> values = combinations.get(key);
				if (verbose == true) {
					System.out.print("\n       An edge weight of " + key + " maps onto integer combination: ");
					for (int i = 0; i < values.size(); i++) {
						System.out.print(values.get(i) + " ");
					}
				}
			}
			if (verbose == true) {
				System.out.print("\n");
			}
		}
		
		// normalization
		boolean integerBoolean = false;
		if (this.normalization.equals("no")) {
			integerBoolean = true;
		} else if (this.normalization.equals("activity")) {
			integerBoolean = false;
			double currentDenominator;
			for (int i = 0; i < names1.length; i++) {
				currentDenominator = 0.0;
				if (qualifierAggregation.equals("ignore")) { // iterate through columns of matrix and sum weighted values
					for (int j = 0; j < names2.length; j++) {
						currentDenominator = currentDenominator + mat[i][j];
					}
				} else if (qualifierAggregation.equals("combine")) { // iterate through columns of matrix and count how many are larger than one
					System.err.println("Warning: Normalization and qualifier setting 'combine' yield results that cannot be interpreted.");
					for (int j = 0; j < names2.length; j++) {
						if (mat[i][j] > 0.0) {
							currentDenominator = currentDenominator + 1.0;
						}
					}
				} else if (qualifierAggregation.equals("subtract")) { // iterate through array and sum for different levels
					for (int j = 0; j < names2.length; j++) {
						for (int k = 0; k < qualifierValues.length; k++) {
							currentDenominator = currentDenominator + array[i][j][k];
						}
					}
				}
				for (int j = 0; j < names2.length; j++) { // divide all values by current denominator
					mat[i][j] = mat[i][j] / currentDenominator;
				}
			}
		} else if (this.normalization.equals("prominence")) {
			integerBoolean = false;
			double currentDenominator;
			for (int i = 0; i < names2.length; i++) {
				currentDenominator = 0.0;
				if (this.qualifierAggregation.equals("ignore")) { // iterate through rows of matrix and sum weighted values
					for (int j = 0; j < names1.length; j++) {
						currentDenominator = currentDenominator + mat[j][i];
					}
				} else if (this.qualifierAggregation.equals("combine")) { // iterate through rows of matrix and count how many are larger than one
					System.err.println("Warning: Normalization and qualifier setting 'combine' yield results that cannot be interpreted.");
					for (int j = 0; j < names1.length; j++) {
						if (mat[i][j] > 0.0) {
							currentDenominator = currentDenominator + 1.0;
						}
					}
				} else if (this.qualifierAggregation.equals("subtract")) { // iterate through array and sum for different levels
					for (int j = 0; j < names1.length; j++) {
						for (int k = 0; k < qualifierValues.length; k++) {
							currentDenominator = currentDenominator + array[j][i][k];
						}
					}
				}
				for (int j = 0; j < names1.length; j++) { // divide all values by current denominator
					mat[j][i] = mat[j][i] / currentDenominator;
				}
			}
		}
		
		// create Matrix object and return
		Matrix matrix = new Matrix(mat, names1, names2, integerBoolean, start, stop); // assemble the Matrix object with labels
		return matrix;
	}

	/**
	 * Create a series of one-mode or two-mode networks using a moving time
	 * window.
	 * 
	 * @throws Exception
	 */
	public void computeTimeWindowMatrices() throws Exception {
		ArrayList<Matrix> timeWindowMatrices = new ArrayList<Matrix>();
		Collections.sort(this.filteredStatements); // probably not necessary, but can't hurt to have it
		ArrayList<ExportStatement> currentWindowStatements = new ArrayList<ExportStatement>(); // holds all statements in the current time window
		ArrayList<ExportStatement> startStatements = new ArrayList<ExportStatement>(); // holds all statements corresponding to the time stamp of the first statement in the window
		ArrayList<ExportStatement> stopStatements = new ArrayList<ExportStatement>(); // holds all statements corresponding to the time stamp of the last statement in the window
		ArrayList<ExportStatement> beforeStatements = new ArrayList<ExportStatement>(); // holds all statements between (and excluding) the time stamp of the first statement in the window and the focal statement
		ArrayList<ExportStatement> afterStatements = new ArrayList<ExportStatement>(); // holds all statements between (and excluding) the the focal statement and the time stamp of the last statement in the window
		Matrix m;
		if (this.timeWindow.equals("events")) {
			if (this.windowSize < 2) {
				throw new Exception("You must choose a timeUnits parameter of at least 2, otherwise it is impossible to create a time window.");
			}
			int iteratorStart, iteratorStop, i, j;
			int samples;
			for (int t = 0; t < this.filteredStatements.size(); t++) {
				int halfDuration = (int) Math.floor(this.windowSize / 2);
				iteratorStart = t - halfDuration;
				iteratorStop = t + halfDuration;
				
				startStatements.clear();
				stopStatements.clear();
				beforeStatements.clear();
				afterStatements.clear();
				if (iteratorStart >= 0 && iteratorStop < this.filteredStatements.size()) {
					for (i = 0; i < this.filteredStatements.size(); i++) {
						if (this.filteredStatements.get(i).getDateTime().equals(this.filteredStatements.get(iteratorStart).getDateTime())) {
							startStatements.add(this.filteredStatements.get(i));
						}
						if (this.filteredStatements.get(i).getDateTime().equals(this.filteredStatements.get(iteratorStop).getDateTime())) {
							stopStatements.add(this.filteredStatements.get(i));
						}
						if (this.filteredStatements.get(i).getDateTime().isAfter(this.filteredStatements.get(iteratorStart).getDateTime()) && i < t) {
							beforeStatements.add(this.filteredStatements.get(i));
						}
						if (this.filteredStatements.get(i).getDateTime().isBefore(this.filteredStatements.get(iteratorStop).getDateTime()) && i > t) {
							afterStatements.add(this.filteredStatements.get(i));
						}
					}
					if (startStatements.size() + beforeStatements.size() > halfDuration || stopStatements.size() + afterStatements.size() > halfDuration) {
						samples = 1; // this number should be larger than the one below, for example 10 (for 10 random combinations of start and stop statements)
					} else {
						samples = 1;
					}
					
					for (j = 0; j < samples; j++) {
						// add statements from start, before, after, and stop set to current window
						currentWindowStatements.clear();
						Collections.shuffle(startStatements);
						for (i = 0; i < halfDuration - beforeStatements.size(); i++) {
							currentWindowStatements.add(startStatements.get(i));
						}
						currentWindowStatements.addAll(beforeStatements);
						currentWindowStatements.add(this.filteredStatements.get(t));
						currentWindowStatements.addAll(afterStatements);
						Collections.shuffle(stopStatements);
						for (i = 0; i < halfDuration - afterStatements.size(); i++) {
							currentWindowStatements.add(stopStatements.get(i));
						}

						// convert time window to network and add to list
						if (currentWindowStatements.size() > 0) {
							int firstDocId = currentWindowStatements.get(0).getDocumentId();
							LocalDateTime first = null;
							for (i = 0; i < this.documents.size(); i++) {
								if (firstDocId == this.documents.get(i).getId()) {
									first = documents.get(i).getDateTime();
									break;
								}
							}
							int lastDocId = currentWindowStatements.get(currentWindowStatements.size() - 1).getDocumentId();
							LocalDateTime last = null;
							for (i = this.documents.size() - 1; i > -1; i--) {
								if (lastDocId == this.documents.get(i).getId()) {
									last = this.documents.get(i).getDateTime();
									break;
								}
							}
							if (this.networkType.equals("twomode")) {
								boolean verbose = false;
								m = computeTwoModeMatrix(currentWindowStatements, first, last, verbose);
								m.setDateTime(this.filteredStatements.get(t).getDateTime());
								m.setNumStatements(currentWindowStatements.size());
								timeWindowMatrices.add(m);
							} else {
								if (qualifierAggregation.equals("congruence & conflict")) { // note: the networks are saved in alternating order and need to be disentangled
									m = computeOneModeMatrix(currentWindowStatements, "congruence", first, last);
									m.setDateTime(this.filteredStatements.get(t).getDateTime());
									m.setNumStatements(currentWindowStatements.size());
									timeWindowMatrices.add(m);
									m = computeOneModeMatrix(currentWindowStatements, "conflict", first, last);
									m.setDateTime(this.filteredStatements.get(t).getDateTime());
									m.setNumStatements(currentWindowStatements.size());
									timeWindowMatrices.add(m);
								} else {
									m = computeOneModeMatrix(currentWindowStatements, this.qualifierAggregation, first, last);
									m.setDateTime(this.filteredStatements.get(t).getDateTime());
									m.setNumStatements(currentWindowStatements.size());
									timeWindowMatrices.add(m);
								}
								
							}
						}
					}
				}
			}
		} else {
			LocalDateTime startCalendar = this.startDateTime; // start of statement list
			LocalDateTime stopCalendar = this.stopDateTime; // end of statement list
			LocalDateTime currentTime = this.startDateTime; // current time while progressing through list of statements
			LocalDateTime windowStart = this.startDateTime; // start of the time window
			LocalDateTime windowStop = this.startDateTime; // end of the time window
			LocalDateTime iTime; // time of the statement to be potentially added to the time slice
			int addition = 0;
			while (!currentTime.isAfter(stopCalendar)) {
				LocalDateTime matrixTime = currentTime;
				windowStart = matrixTime;
				windowStop = matrixTime;
				currentWindowStatements.clear();
				addition = (int) Math.round(((double) windowSize - 1) / 2);
				if (timeWindow.equals("seconds")) {
					windowStart.minusSeconds(addition);
					windowStop.plusSeconds(addition);
					currentTime.plusSeconds(1);
				} else if (timeWindow.equals("minutes")) {
					windowStart.minusMinutes(addition);
					windowStop.plusMinutes(addition);
					currentTime.plusMinutes(1);
				} else if (timeWindow.equals("hours")) {
					windowStart.minusHours(addition);
					windowStop.plusHours(addition);
					currentTime.plusHours(1);
				} else if (timeWindow.equals("days")) {
					windowStart.minusDays(addition);
					windowStop.plusDays(addition);
					currentTime.plusDays(1);
				} else if (timeWindow.equals("weeks")) {
					windowStart.minusWeeks(addition);
					windowStop.plusWeeks(addition);
					currentTime.plusWeeks(1);
				} else if (timeWindow.equals("months")) {
					windowStart.minusMonths(addition);
					windowStop.plusMonths(addition);
					currentTime.plusMonths(1);
				} else if (timeWindow.equals("years")) {
					windowStart.minusYears(addition);
					windowStop.plusYears(addition);
					currentTime.plusYears(1);
				}
				if (!windowStart.isBefore(startCalendar) && !windowStop.isAfter(stopCalendar)) {
					for (int i = 0; i < this.filteredStatements.size(); i++) {
						iTime = this.filteredStatements.get(i).getDateTime();
						if (!iTime.isBefore(windowStart) && !iTime.isAfter(windowStop)) {
							currentWindowStatements.add(this.filteredStatements.get(i));
						}
					}
					if (currentWindowStatements.size() > 0) {
						if (this.networkType.equals("twomode")) {
							boolean verbose = false;
							m = computeTwoModeMatrix(currentWindowStatements, windowStart, windowStop, verbose);
						} else {
							m = computeOneModeMatrix(currentWindowStatements, this.qualifierAggregation, windowStart, windowStop);
						}
						m.setDateTime(matrixTime);
						m.setNumStatements(currentWindowStatements.size());
						timeWindowMatrices.add(m);
					}
				}
			}
		}
		this.matrixResults = timeWindowMatrices;
	}
	
	/**
	 * Get the computed network matrix results.
	 * 
	 * @return An array list of {@link model.Matrix Matrix} objects. If time
	 *   window functionality was used, there are multiple matrices in the list,
	 *   otherwise just one.
	 */
	public ArrayList<Matrix> getMatrixResults() {
		return matrixResults;
	}
	
	/**
	 * Write results to file.
	 */
	public void exportToFile() {
		if (networkType.equals("eventlist") && fileFormat.equals("csv")) {
			eventCSV();
		} else if (fileFormat.equals("csv")) {
			exportCSV();
		} else if (fileFormat.equals("dl")) {
			exportDL();
		} else if (fileFormat.equals("graphml")) {
			exportGraphml();
		}
	}

	/**
	 * Write an event list to a CSV file. The event list contains all filtered
	 * statements including their IDs, date/time stamps, variable values, text,
	 * and document meta-data. There is one statement per row. The event list
	 * can be used for estimating relational event models.
	 */
	private void eventCSV() {
		String key;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outfile), "UTF-8"));
			out.write("\"statement ID\";\"time\";\"document ID\";\"document title\";\"author\";\"source\";\"section\";\"type\";\"text\"");
			for (int i = 0; i < statementType.getVariables().size(); i++) {
				out.write(";\"" + statementType.getVariables().get(i).getKey() + "\"");
			}
			ExportStatement s;
			for (int i = 0; i < this.filteredStatements.size(); i++) {
				s = this.filteredStatements.get(i);
				out.newLine();
				String stringId = Integer.valueOf(s.getId()).toString();
				out.write(stringId);
				out.write(";" + s.getDateTime().format(formatter));
				out.write(";" + s.getDocumentIdAsString());
				out.write(";\"" + s.getTitle().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + s.getAuthor().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + s.getSource().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + s.getSection().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + s.getType().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + s.getText().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				for (int j = 0; j < statementType.getVariables().size(); j++) {
					key = statementType.getVariables().get(j).getKey();
					if (this.dataTypes.get(key).equals("short text")) {
						out.write(";\"" + (((Entity) s.get(key)).getValue()).replaceAll(";", ",").replaceAll("\"", "'") + "\"");
					} else if (this.dataTypes.get(key).equals("long text")) {
						out.write(";\"" + ((String) s.get(key)).replaceAll(";", ",").replaceAll("\"", "'") + "\"");
					} else {
						out.write(";" + s.get(key));
					}
				}
			}
			out.close();
			System.out.println("Event list has been exported to \"" + this.outfile + "\".");
		} catch (IOException e) {
			System.err.println("Error while saving CSV file: " + e);
		}
	}

	/**
	 * Export {@link model.Matrix Matrix} to a CSV matrix file.
	 * 
	 * @param matrix   The input {@link Matrix} object.
	 * @param outfile  The path and file name of the target CSV file.
	 */
	private void exportCSV () {
		String filename2;
		String filename1 = this.outfile.substring(0, this.outfile.length() - 4);
		String filename3 = this.outfile.substring(this.outfile.length() - 4, this.outfile.length());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		for (int k = 0; k < this.matrixResults.size(); k++) {
			// assemble file name for time window networks if necessary
			String filename = this.outfile;
			if (this.matrixResults.size() > 1) {
				filename2 = " " + String.format("%0" + String.valueOf(this.matrixResults.size()).length() + "d", k + 1) + " " + this.matrixResults.get(k).getDateTime().format(formatter);
				filename = filename1 + filename2 + filename3;
			}
			
			// get current data
			String[] rn = this.matrixResults.get(k).getRownames();
			String[] cn = this.matrixResults.get(k).getColnames();
			int nr = rn.length;
			int nc = cn.length;
			double[][] mat = this.matrixResults.get(k).getMatrix();
			
			// export
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
				out.write("\"\"");
				for (int i = 0; i < nc; i++) {
					out.write(";\"" + cn[i].replaceAll("\"", "'") + "\"");
				}
				for (int i = 0; i < nr; i++) {
					out.newLine();
					out.write("\"" + rn[i].replaceAll("\"", "'") + "\"");
					for (int j = 0; j < nc; j++) {
						if (this.matrixResults.get(k).getInteger()) {
							out.write(";" + (int) mat[i][j]);
						} else {
							out.write(";" + String.format(new Locale("en"), "%.6f", mat[i][j])); // six decimal places
						}
					}
				}
				out.close();
			} catch (IOException e) {
				System.err.println("Error while saving CSV matrix file.");
			}
		}
	}

	/**
	 * Export network to a DL fullmatrix file for the software UCINET.
	 */
	private void exportDL () {
		String filename2;
		String filename1 = this.outfile.substring(0, this.outfile.length() - 4);
		String filename3 = this.outfile.substring(this.outfile.length() - 4, this.outfile.length());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		for (int k = 0; k < this.matrixResults.size(); k++) {
			// assemble file name for time window networks if necessary
			String filename = this.outfile;
			if (this.matrixResults.size() > 1) {
				filename2 = " " + String.format("%0" + String.valueOf(this.matrixResults.size()).length() + "d", k + 1) + " " + this.matrixResults.get(k).getDateTime().format(formatter);
				filename = filename1 + filename2 + filename3;
			}
			
			// get current data
			String[] rn = this.matrixResults.get(k).getRownames();
			String[] cn = this.matrixResults.get(k).getColnames();
			int nr = rn.length;
			int nc = cn.length;
			double[][] mat = this.matrixResults.get(k).getMatrix();
			
			// export
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
				out.write("dl ");
				if (this.networkType.equals("onemode")) {
					out.write("n = " + nr);
				} else if (this.networkType.equals("twomode")) {
					out.write("nr = " + nr + ", nc = " + nc);
				}
				out.write(", format = fullmatrix");
				out.newLine();
				if (this.networkType.equals("twomode")) {
					out.write("row labels:");
				} else {
					out.write("labels:");
				}
				for (int i = 0; i < nr; i++) {
					out.newLine();
					out.write("\"" + rn[i].replaceAll("\"", "'").replaceAll("'", "") + "\"");
				}
				if (this.networkType.equals("twomode")) {
					out.newLine();
					out.write("col labels:");
					for (int i = 0; i < nc; i++) {
						out.newLine();
						out.write("\"" + cn[i].replaceAll("\"", "'").replaceAll("'", "") + "\"");
					}
				}
				out.newLine();
				out.write("data:");
				for (int i = 0; i < nr; i++) {
					out.newLine();
					for (int j = 0; j < nc; j++) {
						if (this.matrixResults.get(k).getInteger()) {
							out.write(" " + (int) mat[i][j]);
						} else {
							out.write(" " + String.format(new Locale("en"), "%.6f", mat[i][j]));
						}
					}
				}
				out.close();
			} catch (IOException e) {
				System.err.println("Error while saving DL fullmatrix file.");
			}
		}
	}

	/**
	 * Export filter for graphML files.
	 */
	private void exportGraphml() {
		// set up file name components for time window (in case this will be required later)
		String filename2;
		String filename1 = this.outfile.substring(0, this.outfile.length() - 4);
		String filename3 = this.outfile.substring(this.outfile.length() - 4, this.outfile.length());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		// get variable IDs for variable 1 and variable 2
		int variable1Id = this.statementType.getVariables()
				.stream()
				.filter(v -> v.getKey().equals(this.variable1))
				.mapToInt(v -> v.getVariableId())
				.findFirst()
				.getAsInt();
		int variable2Id = this.statementType.getVariables()
				.stream()
				.filter(v -> v.getKey().equals(this.variable2))
				.mapToInt(v -> v.getVariableId())
				.findFirst()
				.getAsInt();
		
		// get attribute variable names for variable 1 and variable 2
		ArrayList<String> attributeVariables1 = Dna.sql.getAttributeVariables(variable1Id);
		ArrayList<String> attributeVariables2 = Dna.sql.getAttributeVariables(variable2Id);
		
		// get entities with attribute values for variable 1 and variable 2 and save in hash maps
		ArrayList<Integer> variableIds = new ArrayList<Integer>();
		variableIds.add(variable1Id);
		variableIds.add(variable2Id);
		ArrayList<ArrayList<Entity>> entities = Dna.sql.getEntities(variableIds, true);
		HashMap<String, Entity> entityMap1 = new HashMap<String, Entity>();
		HashMap<String, Entity> entityMap2 = new HashMap<String, Entity>();
		entities.get(0).stream().forEach(entity -> entityMap1.put(entity.getValue(), entity));
		entities.get(1).stream().forEach(entity -> entityMap2.put(entity.getValue(), entity));
		
		Matrix m;
		for (int k = 0; k < this.matrixResults.size(); k++) {
			m = this.matrixResults.get(k);
			
			// assemble file name for time window networks if necessary
			String filename = this.outfile;
			if (this.matrixResults.size() > 1) {
				filename2 = " " + String.format("%0" + String.valueOf(this.matrixResults.size()).length() + "d", k + 1) + " " + m.getDateTime().format(formatter);
				filename = filename1 + filename2 + filename3;
			}
			
			// frequencies
			String[] values1 = this.retrieveValues(this.filteredStatements, this.variable1, this.variable1Document);
			String[] values2 = this.retrieveValues(this.filteredStatements, this.variable2, this.variable2Document);
			int[] frequencies1 = this.countFrequencies(values1, m.getRownames());
			int[] frequencies2 = this.countFrequencies(values2, m.getColnames());
			
			// join names, frequencies, and variable names into long arrays for both modes
			String[] rn = this.matrixResults.get(k).getRownames();
			String[] cn = this.matrixResults.get(k).getColnames();
			String[] names;
			String[] variables;
			int[] frequencies;
			if (this.networkType.equals("twomode")) {
				names = new String[rn.length + cn.length];
				variables = new String[names.length];
				frequencies = new int[names.length];
			} else {
				names = new String[rn.length];
				variables = new String[rn.length];
				frequencies = new int[rn.length];
			}
			for (int i = 0; i < rn.length; i++) {
				names[i] = rn[i];
				variables[i] = this.variable1;
				frequencies[i] = frequencies1[i];
			}
			if (this.networkType.equals("twomode")) {
				for (int i = 0; i < cn.length; i++) {
					names[i + rn.length] = cn[i];
					variables[i + rn.length] = this.variable2;
					frequencies[i + rn.length] = frequencies2[i];
				}
			}
			
			// get id and color arrays
			int[] id = Arrays.stream(rn).mapToInt(s -> entityMap1.get(s).getId()).toArray();
			String[] color = Arrays.stream(rn).map(s -> {
				Color col = entityMap1.get(s).getColor();
				return String.format("#%02X%02X%02X", col.getRed(), col.getGreen(), col.getBlue());
			}).toArray(String[]::new);
			if (networkType.equals("twomode")) {
				id = IntStream.concat(IntStream.of(id), Arrays.stream(cn).mapToInt(s -> entityMap2.get(s).getId())).toArray();
				color = Stream.concat(Stream.of(color), Arrays.stream(cn).map(s -> {
					Color col = entityMap2.get(s).getColor();
					return String.format("#%02X%02X%02X", col.getRed(), col.getGreen(), col.getBlue());
				})).toArray(String[]::new);
			}
			
			// set up graph structure
			Namespace xmlns = Namespace.getNamespace("http://graphml.graphdrawing.org/xmlns");
			Element graphml = new Element("graphml", xmlns);
			Namespace visone = Namespace.getNamespace("visone", "http://visone.info/xmlns");
			graphml.addNamespaceDeclaration(visone);
			Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			graphml.addNamespaceDeclaration(xsi);
			Namespace yNs = Namespace.getNamespace("y", "http://www.yworks.com/xml/graphml");
			graphml.addNamespaceDeclaration(yNs);
			Attribute attSchema = new Attribute("schemaLocation", "http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd ", xsi);
			graphml.setAttribute(attSchema);
			org.jdom.Document document = new org.jdom.Document(graphml);
			
			Comment dataSchema = new Comment(" data schema ");
			graphml.addContent(dataSchema);
			
			Element keyVisoneNode = new Element("key", xmlns);
			keyVisoneNode.setAttribute(new Attribute("for", "node"));
			keyVisoneNode.setAttribute(new Attribute("id", "d0"));
			keyVisoneNode.setAttribute(new Attribute("yfiles.type", "nodegraphics"));
			graphml.addContent(keyVisoneNode);

			Element keyVisoneEdge = new Element("key", xmlns);
			keyVisoneEdge.setAttribute(new Attribute("for", "edge"));
			keyVisoneEdge.setAttribute(new Attribute("id", "e0"));
			keyVisoneEdge.setAttribute(new Attribute("yfiles.type", "edgegraphics"));
			graphml.addContent(keyVisoneEdge);

			Element keyVisoneGraph = new Element("key", xmlns);
			keyVisoneGraph.setAttribute(new Attribute("for", "graph"));
			keyVisoneGraph.setAttribute(new Attribute("id", "prop"));
			keyVisoneGraph.setAttribute(new Attribute("visone.type", "properties"));
			graphml.addContent(keyVisoneGraph);
			
			Element keyId = new Element("key", xmlns);
			keyId.setAttribute(new Attribute("id", "id"));
			keyId.setAttribute(new Attribute("for", "node"));
			keyId.setAttribute(new Attribute("attr.name", "id"));
			keyId.setAttribute(new Attribute("attr.type", "string"));
			graphml.addContent(keyId);

			Element keyName = new Element("key", xmlns);
			keyName.setAttribute(new Attribute("id", "name"));
			keyName.setAttribute(new Attribute("for", "node"));
			keyName.setAttribute(new Attribute("attr.name", "name"));
			keyName.setAttribute(new Attribute("attr.type", "string"));
			graphml.addContent(keyName);
			
			ArrayList<String> addedAttributes = new ArrayList<String>();
			attributeVariables1.stream().forEach(v -> {
				Element keyAttribute = new Element("key", xmlns);
				keyAttribute.setAttribute(new Attribute("id", v));
				keyAttribute.setAttribute(new Attribute("for", "node"));
				keyAttribute.setAttribute(new Attribute("attr.name", v));
				keyAttribute.setAttribute(new Attribute("attr.type", "string"));
				graphml.addContent(keyAttribute);
				addedAttributes.add(v);
			});
			if (this.networkType.equals("twomode")) {
				attributeVariables2.stream().forEach(v -> {
					if (!addedAttributes.contains(v)) {
						Element keyAttribute = new Element("key", xmlns);
						keyAttribute.setAttribute(new Attribute("id", v));
						keyAttribute.setAttribute(new Attribute("for", "node"));
						keyAttribute.setAttribute(new Attribute("attr.name", v));
						keyAttribute.setAttribute(new Attribute("attr.type", "string"));
						graphml.addContent(keyAttribute);
						addedAttributes.add(v);
					}
				});
			}
			
			Element keyVariable = new Element("key", xmlns);
			keyVariable.setAttribute(new Attribute("id", "variable"));
			keyVariable.setAttribute(new Attribute("for", "node"));
			keyVariable.setAttribute(new Attribute("attr.name", "variable"));
			keyVariable.setAttribute(new Attribute("attr.type", "string"));
			graphml.addContent(keyVariable);

			Element keyFrequency = new Element("key", xmlns);
			keyFrequency.setAttribute(new Attribute("id", "frequency"));
			keyFrequency.setAttribute(new Attribute("for", "node"));
			keyFrequency.setAttribute(new Attribute("attr.name", "frequency"));
			keyFrequency.setAttribute(new Attribute("attr.type", "int"));
			graphml.addContent(keyFrequency);

			Element keyWeight = new Element("key", xmlns);
			keyWeight.setAttribute(new Attribute("id", "weight"));
			keyWeight.setAttribute(new Attribute("for", "edge"));
			keyWeight.setAttribute(new Attribute("attr.name", "weight"));
			keyWeight.setAttribute(new Attribute("attr.type", "double"));
			graphml.addContent(keyWeight);
			
			Element graphElement = new Element("graph", xmlns);
			graphElement.setAttribute(new Attribute("edgedefault", "undirected"));
			
			graphElement.setAttribute(new Attribute("id", "DNA"));
			int numEdges = rn.length * cn.length;
			if (this.networkType.equals("onemode")) {
				numEdges = (numEdges - rn.length) / 2;
			}
			int numNodes = rn.length;
			if (this.networkType.equals("twomode")) {
				numNodes = numNodes + cn.length;
			}
			graphElement.setAttribute(new Attribute("parse.edges", String.valueOf(numEdges)));
			graphElement.setAttribute(new Attribute("parse.nodes", String.valueOf(numNodes)));
			graphElement.setAttribute(new Attribute("parse.order", "free"));
			Element properties = new Element("data", xmlns);
			properties.setAttribute(new Attribute("key", "prop"));
			Element labelAttribute = new Element("labelAttribute", visone);
			labelAttribute.setAttribute("edgeLabel", "weight");
			labelAttribute.setAttribute("nodeLabel", "name");
			properties.addContent(labelAttribute);
			graphElement.addContent(properties);
			
			// add nodes
			Comment nodes = new Comment(" nodes ");
			graphElement.addContent(nodes);
			
			for (int i = 0; i < names.length; i++) {
				Element node = new Element("node", xmlns);
				node.setAttribute(new Attribute("id", "n" + id[i]));
				
				Element idElement = new Element("data", xmlns);
				idElement.setAttribute(new Attribute("key", "id"));
				idElement.setText(String.valueOf(id[i]));
				node.addContent(idElement);
				
				Element nameElement = new Element("data", xmlns);
				nameElement.setAttribute(new Attribute("key", "name"));
				nameElement.setText(names[i]);
				node.addContent(nameElement);
				
				for (int j = 0; j < attributeVariables1.size(); j++) {
					if (i < rn.length) { // first mode: rows
						Element element = new Element("data", xmlns);
						element.setAttribute(new Attribute("key", attributeVariables1.get(j)));
						element.setText(entityMap1.get(names[i]).getAttributeValues().get(attributeVariables1.get(j)));
						node.addContent(element);
					}
				}
				if (this.networkType.equals("twomode")) {
					for (int j = 0; j < attributeVariables2.size(); j++) {
						if (i >= rn.length) { // second mode: columns
							Element element = new Element("data", xmlns);
							element.setAttribute(new Attribute("key", attributeVariables2.get(j)));
							element.setText(entityMap2.get(names[i]).getAttributeValues().get(attributeVariables2.get(j)));
							node.addContent(element);
						}
					}
				}
				
				Element variableElement = new Element("data", xmlns);
				variableElement.setAttribute(new Attribute("key", "variable"));
				variableElement.setText(variables[i]);
				node.addContent(variableElement);
				
				Element frequency = new Element("data", xmlns);
				frequency.setAttribute(new Attribute("key", "frequency"));
				frequency.setText(String.valueOf(frequencies[i]));
				node.addContent(frequency);
				
				Element vis = new Element("data", xmlns);
				vis.setAttribute(new Attribute("key", "d0"));
				Element visoneShapeNode = new Element("shapeNode", visone);
				Element yShapeNode = new Element("ShapeNode", yNs);
				Element geometry = new Element("Geometry", yNs);
				geometry.setAttribute(new Attribute("height", "20.0"));
				geometry.setAttribute(new Attribute("width", "20.0"));
				geometry.setAttribute(new Attribute("x", String.valueOf(Math.random() * 800)));
				geometry.setAttribute(new Attribute("y", String.valueOf(Math.random() * 600)));
				yShapeNode.addContent(geometry);
				Element fill = new Element("Fill", yNs);
				fill.setAttribute(new Attribute("color", color[i]));

				fill.setAttribute(new Attribute("transparent", "false"));
				yShapeNode.addContent(fill);
				Element borderStyle = new Element("BorderStyle", yNs);
				borderStyle.setAttribute(new Attribute("color", "#000000"));
				borderStyle.setAttribute(new Attribute("type", "line"));
				borderStyle.setAttribute(new Attribute("width", "1.0"));
				yShapeNode.addContent(borderStyle);

				Element nodeLabel = new Element("NodeLabel", yNs);
				nodeLabel.setAttribute(new Attribute("alignment", "center"));
				nodeLabel.setAttribute(new Attribute("autoSizePolicy", "content"));
				nodeLabel.setAttribute(new Attribute("backgroundColor", "#FFFFFF"));
				nodeLabel.setAttribute(new Attribute("fontFamily", "Dialog"));
				nodeLabel.setAttribute(new Attribute("fontSize", "12"));
				nodeLabel.setAttribute(new Attribute("fontStyle", "plain"));
				nodeLabel.setAttribute(new Attribute("hasLineColor", "false"));
				nodeLabel.setAttribute(new Attribute("height", "19.0"));
				nodeLabel.setAttribute(new Attribute("modelName", "eight_pos"));
				nodeLabel.setAttribute(new Attribute("modelPosition", "n"));
				nodeLabel.setAttribute(new Attribute("textColor", "#000000"));
				nodeLabel.setAttribute(new Attribute("visible", "true"));
				nodeLabel.setText(names[i]);
				yShapeNode.addContent(nodeLabel);
				
				Element shape = new Element("Shape", yNs);
				if (i < rn.length) {
					shape.setAttribute(new Attribute("type", "ellipse"));
				} else {
					shape.setAttribute(new Attribute("type", "roundrectangle"));
				}
				yShapeNode.addContent(shape);
				visoneShapeNode.addContent(yShapeNode);
				vis.addContent(visoneShapeNode);
				node.addContent(vis);
				
				graphElement.addContent(node);
			}
			
			// add edges
			Comment edges = new Comment(" edges ");
			graphElement.addContent(edges);
			for (int i = 0; i < rn.length; i++) {
				for (int j = 0; j < cn.length; j++) {
					if (m.getMatrix()[i][j] != 0.0 && (this.networkType.equals("twomode") || (this.networkType.equals("onemode") && i < j))) {  // only lower triangle is used for one-mode networks
						Element edge = new Element("edge", xmlns);
						
						int currentId = id[i];
						edge.setAttribute(new Attribute("source", "n" + String.valueOf(currentId)));
						if (this.networkType.equals("twomode")) {
							currentId = id[j + rn.length];
						} else {
							currentId = id[j];
						}
						edge.setAttribute(new Attribute("target", "n" + String.valueOf(currentId)));
						
						Element weight = new Element("data", xmlns);
						weight.setAttribute(new Attribute("key", "weight"));
						weight.setText(String.valueOf(m.getMatrix()[i][j]));
						edge.addContent(weight);

						Element visEdge = new Element("data", xmlns);
						visEdge.setAttribute("key", "e0");
						Element visPolyLineEdge = new Element("polyLineEdge", visone);
						Element yPolyLineEdge = new Element("PolyLineEdge", yNs);

						Element yLineStyle = new Element("LineStyle", yNs);
						if (qualifierAggregation.equals("combine") && dataTypes.get(qualifier).equals("boolean")) {
							if (m.getMatrix()[i][j] == 1.0) {
								yLineStyle.setAttribute("color", "#00ff00");
							} else if (m.getMatrix()[i][j] == 2.0) {
								yLineStyle.setAttribute("color", "#ff0000");
							} else if (m.getMatrix()[i][j] == 3.0) {
								yLineStyle.setAttribute("color", "#0000ff");
							}
						} else if (qualifierAggregation.equals("subtract")) {
							if (m.getMatrix()[i][j] < 0) {
								yLineStyle.setAttribute("color", "#ff0000");
							} else if (m.getMatrix()[i][j] > 0) {
								yLineStyle.setAttribute("color", "#00ff00");
							}
						} else if (qualifierAggregation.equals("conflict")) {
							yLineStyle.setAttribute("color", "#ff0000");
						} else if (qualifierAggregation.equals("congruence")) {
							yLineStyle.setAttribute("color", "#00ff00");
						} else {
							yLineStyle.setAttribute("color", "#000000");
						}
						yLineStyle.setAttribute(new Attribute("type", "line"));
						yLineStyle.setAttribute(new Attribute("width", "2.0"));
						yPolyLineEdge.addContent(yLineStyle);
						visPolyLineEdge.addContent(yPolyLineEdge);
						visEdge.addContent(visPolyLineEdge);
						edge.addContent(visEdge);
						
						graphElement.addContent(edge);
					}
				}
			}
			
			graphml.addContent(graphElement);
			
			// write to file
			File dnaFile = new File(filename);
			try {
				FileOutputStream outStream = new FileOutputStream(dnaFile);
				XMLOutputter outToFile = new XMLOutputter();
				Format format = Format.getPrettyFormat();
				format.setEncoding("utf-8");
				outToFile.setFormat(format);
				outToFile.output(document, outStream);
				outStream.flush();
				outStream.close();
			} catch (IOException e) {
				System.err.println("Cannot save \"" + dnaFile + "\":" + e.getMessage());
			}
		}
	}

	/**
	 * An extension of the Statement class, which also holds some document meta-
	 * data and a hash map of the values (in addition to the array list of
	 * values).
	 */
	private class ExportStatement extends Statement {
		private HashMap<String, Object> map;
		private String title, author, source, section, type;

		/**
		 * Create an export statement.
		 * 
		 * @param statement The statement to be converted.
		 * @param title The document title.
		 * @param author The author.
		 * @param source The source.
		 * @param section The section.
		 * @param type The type.
		 */
		public ExportStatement(Statement statement, String title, String author,
				String source, String section, String type) {
			super(statement);
			this.title = title;
			this.author = author;
			this.source = source;
			this.section = section;
			this.type = type;
			this.map = new HashMap<String, Object>();
			for (Value value : ExportStatement.this.getValues()) {
				map.put(value.getKey(), value.getValue());
			}
		}
		
		/**
		 * Copy constructor to create a deep copy of an export statement.
		 * 
		 * @param exportStatement An existing export statement.
		 */
		private ExportStatement(ExportStatement exportStatement) {
			super(exportStatement);
			this.title = exportStatement.getTitle();
			this.author = exportStatement.getAuthor();
			this.source = exportStatement.getSource();
			this.section = exportStatement.getSection();
			this.type = exportStatement.getType();
			this.map = new HashMap<String, Object>();
			for (Value value : exportStatement.getValues()) {
				map.put(value.getKey(), value.getValue());
			}
		}
		
		public Object get(String key) {
			return this.map.get(key);
		}
		
		public String getTitle() {
			return this.title;
		}
		
		public String getAuthor() {
			return this.author;
		}
		
		public String getSource() {
			return this.source;
		}
		
		public String getSection() {
			return this.section;
		}
		
		public String getType() {
			return this.type;
		}
		
		public String getDocumentIdAsString() {
			return String.valueOf(this.getDocumentId());
		}
	}
}