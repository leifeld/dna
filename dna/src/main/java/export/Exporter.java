package export;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Collectors;

import dna.Dna;
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
	private String var1, var2, qualifierName, duplicateSetting;
	private boolean var1Document, var2Document, ignoreQualifier;
	private LocalDateTime startDateTime, stopDateTime;
	private HashMap<String, ArrayList<String>> excludeValues;
	private ArrayList<String> excludeAuthors, excludeSources, excludeSections, excludeTypes;
	
	private ArrayList<TableDocument> documents;
	private HashMap<Integer, Integer> docMap;
	private ArrayList<ExportStatement> originalStatements;
	private ArrayList<ExportStatement> filteredStatements;

	/**
	 * Create a new Exporter class instance, holding an array list of export
	 * statements (i.e., statements with added document information and a hash
	 * map for easier access to variable values.
	 * 
	 * @param statements Array list of statements.
	 */
	public Exporter(
			StatementType statementType,
			String var1,
			String var2,
			boolean var1Document,
			boolean var2Document,
			LocalDateTime startDateTime,
			LocalDateTime stopDateTime,
			String qualifierName,
			boolean ignoreQualifier,
			String duplicateSetting,
			HashMap<String, ArrayList<String>> excludeValues,
			ArrayList<String> excludeAuthors,
			ArrayList<String> excludeSources,
			ArrayList<String> excludeSections,
			ArrayList<String> excludeTypes) {
		this.statementType = statementType;
		this.var1 = var1;
		this.var2 = var2;
		this.var1Document = var1Document;
		this.var2Document = var2Document;
		this.startDateTime = startDateTime;
		this.stopDateTime = stopDateTime;
		this.qualifierName = qualifierName;
		this.ignoreQualifier = ignoreQualifier;
		this.duplicateSetting = duplicateSetting;
		this.excludeValues = excludeValues;
		this.excludeAuthors = excludeAuthors;
		this.excludeSources = excludeSources;
		this.excludeSections = excludeSections;
		this.excludeTypes = excludeTypes;
		
		this.documents = Dna.sql.getTableDocuments(new int[0]);
		this.docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}
		
		this.originalStatements = Dna.sql.getStatements(new int[0],
				this.startDateTime,
				this.stopDateTime,
				this.excludeAuthors,
				false,
				this.excludeSources,
				false,
				this.excludeSections,
				false,
				this.excludeTypes,
				false)
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
	 * Apply filter to statements.
	 */
	public void executeFilter() {
		// create a deep copy of the original statements
		this.filteredStatements = new ArrayList<ExportStatement>();
		for (int i = 0; i < this.originalStatements.size(); i++) {
			this.filteredStatements.add(new ExportStatement(filteredStatements.get(i)));
		}
		
		// filter the copied statements
		this.filteredStatements = this.filter(this.filteredStatements,
				this.statementType,	this.var1, this.var2, this.var1Document,
				this.var2Document, this.qualifierName, this.ignoreQualifier,
				this.duplicateSetting, this.excludeValues);
	}
	
	/**
	 * Extract the labels for all nodes for a variable from the statements,
	 * conditional on isolates settings.
	 * 
	 * @param filteredStatements  Array list of filtered statements.
	 * @param originalStatements  Array list of unfiltered/original statements.
	 * @param documents           Array list of documents in the database.
	 * @param docMap              Hash map containing document indices by ID.
	 * @param variable            String indicating the variable for which
	 *   labels should be extracted.
	 * @param variableDocument    Is the variable a document-level variable?
	 * @param includeIsolates     Indicates whether all nodes should be included
	 *   or just those after applying the statement filter.
	 * @return                    String array containing all sorted node names.
	 */
	String[] extractLabels(
			ArrayList<ExportStatement> filteredStatements,
			ArrayList<ExportStatement> originalStatements,
			ArrayList<TableDocument> documents,
			HashMap<Integer, Integer> docMap,
			String variable,
			boolean variableDocument,
			boolean includeIsolates) {
		
		// decide whether to use the original statements or the filtered statements
		ArrayList<ExportStatement> finalStatements;
		if (includeIsolates) {
			finalStatements = originalStatements;
		} else {
			finalStatements = filteredStatements;
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
	 * Return a filtered list of {@link Statement}s based on the settings in the GUI.
	 * 
	 * @param statements         Array list of statements to be filtered.
	 * @param statementType      The statement type.
	 * @param var1               The first variable used for network
	 *   construction, e.g., "organization".
	 * @param var2               The second variable used for network
	 *   construction, e.g., "concept".
	 * @param var1Document       Indicates if the var1 variable is a
	 *   document-level variable (as opposed to statement-level).
	 * @param var2Document       Indicates if the var2 variable is a
	 *   document-level variable (as opposed to statement-level).
	 * @param qualifierName      The qualifier variable, e.g., "agreement".
	 * @param ignoreQualifier    Indicates whether the qualifier variable should
	 *   be ignored.
	 * @param duplicateSetting   String indicating how to handle duplicates;
	 *   valid settings include "include all duplicates", "ignore per document",
	 *   "ignore per calendar week", "ignore per calendar month", "ignore per
	 *   calendar year", or "ignore across date range".
	 * @param excludeValues      Hash map with keys indicating the variable for
	 *   which entries should be excluded from export and values being hash maps
	 *   of variable entries to exclude from network export.
	 * @return                   Array list of filtered export statements.
	 */
	ArrayList<ExportStatement> filter(
			ArrayList<ExportStatement> statements,
			StatementType statementType,
			String var1,
			String var2,
			boolean var1Document,
			boolean var2Document,
			String qualifierName,
			boolean ignoreQualifier,
			String duplicateSetting,
			HashMap<String, ArrayList<String>> excludeValues) {
		
		// sort statements by date and time
		Collections.sort(statements);
		
		// Create arrays with variable values
		String[] values1 = retrieveValues(statements, var1, var1Document);
		String[] values2 = retrieveValues(statements, var2, var2Document);
		
		// put variable data types into a map for quick lookup
		HashMap<String, String> dataTypes = new HashMap<String, String>();
		for (int i = 0; i < statementType.getVariables().size(); i++) {
			dataTypes.put(statementType.getVariables().get(i).getKey(), statementType.getVariables().get(i).getDataType());
		}
		
		// process and exclude statements
		ExportStatement s;
		ArrayList<ExportStatement> al = new ArrayList<ExportStatement>();
	    String previousVar1 = null;
	    String previousVar2 = null;
	    LocalDateTime cal, calPrevious;
	    int year, month, week, yearPrevious, monthPrevious, weekPrevious;
		for (int i = 0; i < statements.size(); i++) {
			boolean select = true;
			s = statements.get(i);

			// check against excluded values
			Iterator<String> keyIterator = excludeValues.keySet().iterator();
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				String string = "";
				if (dataTypes.get(key) == null) {
					throw new NullPointerException("'" + key + "' is not a statement-level variable and cannot be excluded.");
				} else if (dataTypes.get(key).equals("boolean") || dataTypes.get(key).equals("integer")) {
					string = String.valueOf(s.get(key));
				} else {
					string = (String) s.get(key);
				}
				if (excludeValues.get(key).contains(string)) {
					select = false;
				}
			}

			// check against empty fields
			if (select) {
				if (values1[i].equals("") || values2[i].equals("")) {
					select = false;
				}
			}
			
			// step 4: check for duplicates
			cal = s.getDateTime();
		    year = cal.getYear();
		    month = cal.getMonthValue();
		    @SuppressWarnings("static-access")
			WeekFields weekFields = WeekFields.of(Locale.UK.getDefault()); // use UK definition of calendar weeks
			week = cal.get(weekFields.weekOfWeekBasedYear());
			if (!duplicateSetting.equals("include all duplicates")) {
				for (int j = al.size() - 1; j >= 0; j--) {
				    if (var1Document == false) {
				    	previousVar1 = (String) al.get(j).get(var1);
				    } else if (var1.equals("author")) {
				    	previousVar1 = al.get(j).getAuthor();
				    } else if (var1.equals("source")) {
				    	previousVar1 = al.get(j).getSource();
				    } else if (var1.equals("section")) {
				    	previousVar1 = al.get(j).getSection();
				    } else if (var1.equals("type")) {
				    	previousVar1 = al.get(j).getType();
				    } else if (var1.equals("id")) {
				    	previousVar1 = al.get(j).getDocumentIdAsString();
				    } else if (var1.equals("title")) {
				    	previousVar1 = al.get(j).getTitle();
				    }
				    if (var2Document == false) {
				    	previousVar2 = (String) al.get(j).get(var2);
				    } else if (var2.equals("author")) {
				    	previousVar2 = al.get(j).getAuthor();
				    } else if (var2.equals("source")) {
				    	previousVar2 = al.get(j).getSource();
				    } else if (var2.equals("section")) {
				    	previousVar2 = al.get(j).getSection();
				    } else if (var2.equals("type")) {
				    	previousVar2 = al.get(j).getType();
				    } else if (var2.equals("id")) {
				    	previousVar2 = al.get(j).getDocumentIdAsString();
				    } else if (var2.equals("title")) {
				    	previousVar2 = al.get(j).getTitle();
				    }
				    calPrevious = al.get(j).getDateTime();
				    yearPrevious = calPrevious.getYear();
				    monthPrevious = calPrevious.getMonthValue();
				    @SuppressWarnings("static-access")
					WeekFields weekFieldsPrevious = WeekFields.of(Locale.UK.getDefault()); // use UK definition of calendar weeks
					weekPrevious = calPrevious.get(weekFieldsPrevious.weekOfWeekBasedYear());
					if ( s.getStatementTypeId() == al.get(j).getStatementTypeId()
							&& (al.get(j).getDocumentId() == s.getDocumentId() && duplicateSetting.equals("ignore per document") 
								|| duplicateSetting.equals("ignore across date range")
								|| (duplicateSetting.equals("ignore per calendar year") && year == yearPrevious)
								|| (duplicateSetting.equals("ignore per calendar month") && month == monthPrevious)
								|| (duplicateSetting.equals("ignore per calendar week") && week == weekPrevious) )
							&& values1[i].equals(previousVar1)
							&& values2[i].equals(previousVar2)
							&& (ignoreQualifier == true || s.get(qualifierName).equals(al.get(j).get(qualifierName))) ) {
						select = false;
						break;
					}
				}
			}
			
			// step 5: add only if the statement passed all checks
			if (select == true) {
				al.add(s);
			}
		}
		return(al);
	}
	
	/**
	 * Retrieve the values across statements/documents given the name of the
	 * variable. E.g., provide a variable name and information on whether the
	 * variable is defined at the document level (e.g., author or section) or at
	 * the statement level (e.g., organization), and return a one-dimensional
	 * array of values (e.g., the organization names or authors for all
	 * statements provided.
	 * 
	 * @param statements     Original or filtered array list of statements.
	 * @param variable       String denoting the first variable (containing the
	 *   row values).
	 * @param documentLevel  boolean indicating whether the first variable is a
	 *   document-level variable.
	 * @return               String array of values.
	 */
	String[] retrieveValues(ArrayList<ExportStatement> statements, String variable, boolean documentLevel) {
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
				values[i] = (String) s.get(variable);
			}
		}
		return values;
	}
	
	/**
	 * An extension of the Statement class, which also holds some document meta-
	 * data and a hash map of the values (in addition to the array list of
	 * values).
	 */
	private class ExportStatement extends Statement {
		private HashMap<String, Object> map;
		String title, author, source, section, type;

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
		
		public ExportStatement(ExportStatement exportStatement) {
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