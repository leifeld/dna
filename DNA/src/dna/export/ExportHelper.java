package dna.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import dna.dataStructures.AttributeVector;
import dna.dataStructures.Document;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementType;

public class ExportHelper {

	public ExportHelper() {
		// nothing to do here
	}

	/**
	 * Extract the labels for all nodes for a variable from the statements, conditional on isolates settings
	 * 
	 * @param statements          {@link ArrayList} of filtered {@link Statement}s
	 * @param originalStatements  {@link ArrayList} of unfiltered {@link Statement}s (i.e., the original list of statements)
	 * @param documents           {@link ArrayList} of documents in the database
	 * @param variable            {@link String} indicating the variable for which labels should be extracted
	 * @param statementTypeId     {@link int} specifying the statement type ID to which the variable belongs
	 * @param includeIsolates     {@link boolean} indicating whether all nodes should be included or just those after applying the statement filter
	 * @return                    {@link String} array containing all sorted node names
	 */
	String[] extractLabels(
			ArrayList<Statement> statements, 
			ArrayList<Statement> originalStatements, 
			ArrayList<Document> documents, 
			String variable, 
			boolean variableDocument, 
			int statementTypeId, 
			boolean includeIsolates) {
		
		// decide whether to use the original statements or the filtered statements
		ArrayList<Statement> finalStatements;
		if (includeIsolates == true) {
			finalStatements = originalStatements;
		} else {
			finalStatements = statements;
		}

		// HashMap for fast lookup of document indices by ID
		HashMap<Integer, Integer> docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}
		
		// go through statements and extract names
		ArrayList<String> names = new ArrayList<String>();
		String n = null;
		for (int i = 0; i < finalStatements.size(); i++) {
			if (variableDocument == true) {
				if (variable.equals("author")) {
					n = documents.get(docMap.get(finalStatements.get(i).getDocumentId())).getAuthor();
				} else if (variable.equals("source")) {
					n = documents.get(docMap.get(finalStatements.get(i).getDocumentId())).getSource();
				} else if (variable.equals("section")) {
					n = documents.get(docMap.get(finalStatements.get(i).getDocumentId())).getSection();
				} else if (variable.equals("type")) {
					n = documents.get(docMap.get(finalStatements.get(i).getDocumentId())).getType();
				} else if (variable.equals("id")) {
					n = String.valueOf(documents.get(docMap.get(finalStatements.get(i).getDocumentId())).getId());
				} else if (variable.equals("title")) {
					n = documents.get(docMap.get(finalStatements.get(i).getDocumentId())).getTitle();
				}
				if (!names.contains(n)) {
					names.add(n);
				}
			} else if (finalStatements.get(i).getStatementTypeId() == statementTypeId) {
				n = (String) finalStatements.get(i).getValues().get(variable).toString();
				if (!names.contains(n)) {
					names.add(n);
				}
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
	 * @param statements          {@link ArrayList} of {@link Statement}s to be filtered.
	 * @param startDate           {@link Date} object indicating the start date
	 * @param stopDate            {@link Date} object indicating the end date
	 * @param statementType       {@link StatementType} to which the export is restricted
	 * @param var1                {@link String} indicating the first variable used for network construction, e.g., "organization"
	 * @param var2                {@link String} indicating the second variable used for network construction, e.g., "concept"
	 * @param var1Document        {@link boolean} indicating if the var1 variable is a document-level variable (as opposed to statement-level)
	 * @param var2Document        {@link boolean} indicating if the var2 variable is a document-level variable (as opposed to statement-level)
	 * @param qualifierName       {@link String} indicating the qualifier variable, e.g., "agreement"
	 * @param ignoreQualifier     {@link boolean} indicating whether the qualifier variable should be ignored
	 * @param duplicateSetting    {@link String} indicating how to handle duplicates; valid settings include "include all duplicates", "ignore per document", "ignore per calendar week", "ignore per calendar month", "ignore per calendar year", or "ignore across date range"
	 * @param excludeAuthor       {@link ArrayList} with {@link String}s containing document authors to exclude
	 * @param excludeSource       {@link ArrayList} with {@link String}s containing document sources to exclude
	 * @param excludeSection      {@link ArrayList} with {@link String}s containing document sections to exclude
	 * @param excludeType         {@link ArrayList} with {@link String}s containing document types to exclude
	 * @param excludeValues       {@link HashMap} with {@link String}s as keys (indicating the variable for which entries should be excluded from export) and {@link HashMap}s of {@link String}s (containing variable entries to exclude from network export)
	 * @param filterEmptyFields   {@link boolean} indicating whether empty fields (i.e., "") should be excluded
	 * @return                    {@link ArrayList} of filtered {@link Statement}s
	 */
	ArrayList<Statement> filter(
			ArrayList<Statement> statements, 
			ArrayList<Document> documents, 
			Date startDate, 
			Date stopDate, 
			StatementType statementType, 
			String var1, 
			String var2, 
			boolean var1Document, 
			boolean var2Document, 
			String qualifierName, 
			boolean ignoreQualifier, 
			String duplicateSetting, 
			ArrayList<String> excludeAuthor, 
			ArrayList<String> excludeSource, 
			ArrayList<String> excludeSection, 
			ArrayList<String> excludeType, 
			HashMap<String, ArrayList<String>> excludeValues, 
			boolean filterEmptyFields, 
			boolean verbose) {
		
		// sort statements by date and time
		Collections.sort(statements);
		
		// reporting
		Iterator<String> excludeIterator = excludeValues.keySet().iterator();
		while (excludeIterator.hasNext()) {
			String key = excludeIterator.next();
			ArrayList<String> values = excludeValues.get(key);
			if (verbose == true) {
				for (int i = 0; i < values.size(); i++) {
					System.out.println("       [Excluded] " + key + ": " + values.get(i));
				}
			}
		}
		if (verbose == true) {
			for (int i = 0; i < excludeAuthor.size(); i++) {
				System.out.println("       [Excluded] author: " + excludeAuthor.get(i));
			}
			for (int i = 0; i < excludeSource.size(); i++) {
				System.out.println("       [Excluded] source: " + excludeSource.get(i));
			}
			for (int i = 0; i < excludeSection.size(); i++) {
				System.out.println("       [Excluded] section: " + excludeSection.get(i));
			}
			for (int i = 0; i < excludeType.size(); i++) {
				System.out.println("       [Excluded] type: " + excludeType.get(i));
			}
		}
		
		// HashMap for fast lookup of document indices by ID
		HashMap<Integer, Integer> docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}
		
		// Create arrays with variable values
		String[] values1 = retrieveValues(statements, documents, var1, var1Document);
		String[] values2 = retrieveValues(statements, documents, var2, var2Document);
		
		// process and exclude statements
		Statement s;
		ArrayList<Statement> al = new ArrayList<Statement>();
	    String previousVar1 = null;
	    String previousVar2 = null;
	    Calendar cal, calPrevious;
	    int year, month, week, yearPrevious, monthPrevious, weekPrevious;
		for (int i = 0; i < statements.size(); i++) {
			boolean select = true;
			s = statements.get(i);
			
			// step 1: get all statement IDs corresponding to date range and statement type
			if (s.getDate().before(startDate)) {
				select = false;
			} else if (s.getDate().after(stopDate)) {
				select = false;
			} else if (s.getStatementTypeId() != statementType.getId()) {
				select = false;
			}
			
			// step 2: check against excluded values
			if (excludeAuthor.contains(documents.get(docMap.get(s.getDocumentId())).getAuthor())) {
				select = false;
			} else if (excludeSource.contains(documents.get(docMap.get(s.getDocumentId())).getSource())) {
				select = false;
			} else if (excludeSection.contains(documents.get(docMap.get(s.getDocumentId())).getSection())) {
				select = false;
			} else if (excludeType.contains(documents.get(docMap.get(s.getDocumentId())).getType())) {
				select = false;
			}
			if (select == true) {
				Iterator<String> keyIterator = excludeValues.keySet().iterator();
				while (keyIterator.hasNext()) {
					String key = keyIterator.next();
					String string = "";
					if (statementType.getVariables().get(key) == null) {
						throw new NullPointerException("'" + key + "' is not a statement-level variable and cannot be excluded.");
					} else if (statementType.getVariables().get(key).equals("boolean") || statementType.getVariables().get(key).equals("integer")) {
						string = String.valueOf(s.getValues().get(key));
					} else {
						string = (String) s.getValues().get(key);
					}
					if (excludeValues.get(key).contains(string)) {
						select = false;
					}
				}
			}

			// step 3: check against empty fields
			if (select == true) {
				if (values1[i].equals("") || values2[i].equals("")) {
					if (filterEmptyFields == true) {
						select = false;
					}
				}
			}
			
			// step 4: check for duplicates
			cal = Calendar.getInstance();
		    cal.setTime(s.getDate());
		    year = cal.get(Calendar.YEAR);
		    month = cal.get(Calendar.MONTH);
		    week = cal.get(Calendar.WEEK_OF_YEAR);
			if (!duplicateSetting.equals("include all duplicates")) {
				for (int j = al.size() - 1; j >= 0; j--) {
				    if (var1Document == false) {
				    	previousVar1 = (String) al.get(j).getValues().get(var1);
				    } else if (var1.equals("author")) {
				    	previousVar1 = documents.get(docMap.get(al.get(j).getDocumentId())).getAuthor();
				    } else if (var1.equals("source")) {
				    	previousVar1 = documents.get(docMap.get(al.get(j).getDocumentId())).getSource();
				    } else if (var1.equals("section")) {
				    	previousVar1 = documents.get(docMap.get(al.get(j).getDocumentId())).getSection();
				    } else if (var1.equals("type")) {
				    	previousVar1 = documents.get(docMap.get(al.get(j).getDocumentId())).getType();
				    } else if (var1.equals("id")) {
				    	previousVar1 = String.valueOf(documents.get(docMap.get(al.get(j).getDocumentId())).getId());
				    } else if (var1.equals("title")) {
				    	previousVar1 = documents.get(docMap.get(al.get(j).getDocumentId())).getTitle();
				    }
				    if (var2Document == false) {
				    	previousVar2 = (String) al.get(j).getValues().get(var2);
				    } else if (var2.equals("author")) {
				    	previousVar2 = documents.get(docMap.get(al.get(j).getDocumentId())).getAuthor();
				    } else if (var2.equals("source")) {
				    	previousVar2 = documents.get(docMap.get(al.get(j).getDocumentId())).getSource();
				    } else if (var2.equals("section")) {
				    	previousVar2 = documents.get(docMap.get(al.get(j).getDocumentId())).getSection();
				    } else if (var2.equals("type")) {
				    	previousVar2 = documents.get(docMap.get(al.get(j).getDocumentId())).getType();
				    } else if (var2.equals("id")) {
				    	previousVar2 = String.valueOf(documents.get(docMap.get(al.get(j).getDocumentId())).getId());
				    } else if (var2.equals("title")) {
				    	previousVar2 = documents.get(docMap.get(al.get(j).getDocumentId())).getTitle();
				    }
					calPrevious = Calendar.getInstance();
				    calPrevious.setTime(al.get(j).getDate());
				    yearPrevious = calPrevious.get(Calendar.YEAR);
				    monthPrevious = calPrevious.get(Calendar.MONTH);
				    weekPrevious = calPrevious.get(Calendar.WEEK_OF_YEAR);
					if ( s.getStatementTypeId() == al.get(j).getStatementTypeId()
							&& (al.get(j).getDocumentId() == s.getDocumentId() && duplicateSetting.equals("ignore per document") 
								|| duplicateSetting.equals("ignore across date range")
								|| (duplicateSetting.equals("ignore per calendar year") && year == yearPrevious)
								|| (duplicateSetting.equals("ignore per calendar month") && month == monthPrevious)
								|| (duplicateSetting.equals("ignore per calendar week") && week == weekPrevious) )
							&& values1[i].equals(previousVar1)
							&& values2[i].equals(previousVar2)
							&& (ignoreQualifier == true || s.getValues().get(qualifierName).equals(al.get(j).getValues().get(qualifierName))) ) {
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
	 * Retrieve the values across statements/documents given the name of the variable. 
	 * E.g., provide a list of statements, a list of documents, a variable name, and 
	 * information on whether the variable is defined at the document level (e.g., 
	 * author or section) or at the statement level (e.g., organization), and return 
	 * a one-dimensional array of values (e.g., the organization names or authors for 
	 * all statements provided.
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param documents             An {@link ArrayList} of {@link Document}s which contain the statements.
	 * @param variable              {@link String} denoting the first variable (containing the row values).
	 * @param documentLevel         {@link boolean} indicating whether the first variable is a document-level variable.
	 * @return                      String array of values.
	 */
	String[] retrieveValues(ArrayList<Statement> statements, ArrayList<Document> documents, String variable, boolean documentLevel) {
		
		// HashMap for fast lookup of document indices by ID
		HashMap<Integer, Integer> docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}
		
		Statement s;
		String[] values = new String[statements.size()];
		for (int i = 0; i < statements.size(); i++) {
			s = statements.get(i);
			if (documentLevel == true) {
				if (variable.equals("author")) {
					values[i] = documents.get(docMap.get(s.getDocumentId())).getAuthor();
				} else if (variable.equals("source")) {
					values[i] = documents.get(docMap.get(s.getDocumentId())).getSource();
				} else if (variable.equals("section")) {
					values[i] = documents.get(docMap.get(s.getDocumentId())).getSection();
				} else if (variable.equals("type")) {
					values[i] = documents.get(docMap.get(s.getDocumentId())).getType();
				} else if (variable.equals("id")) {
					values[i] = String.valueOf(documents.get(docMap.get(s.getDocumentId())).getId());
				} else if (variable.equals("title")) {
					values[i] = documents.get(docMap.get(s.getDocumentId())).getTitle();
				}
			} else {
				values[i] = (String) s.getValues().get(variable);
			}
		}
		
		return values;
	}

	/**
	 * Count how often a value is used across the range of filtered statements.
	 * 
	 * @param variableValues  String array of all values of a certain variable in a set of statements.
	 * @param uniqueNames     String array of unique values of the same variable across all statements.
	 * @return                int array of value frequencies for each unique value in same order as uniqueNames.
	 */
	int[] countFrequencies(String[] variableValues, String[] uniqueNames) {
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
	 * Create a series of one-mode or two-mode networks using a moving time window.
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param documents             An {@link ArrayList} of {@link Document}s which contain the statements.
	 * @param statementType         The {@link StatementType} corresponding to the statements.
	 * @param var1                  {@link String} denoting the first variable (containing the row values).
	 * @param var2                  {@link String} denoting the second variable (containing the columns values).
	 * @param var1Document          {@link boolean} indicating whether the first variable is a document-level variable.
	 * @param var2Document          {@link boolean} indicating whether the second variable is a document-level variable.
	 * @param names1                {@link String} array containing the row labels.
	 * @param names2                {@link String} array containing the column labels.
	 * @param qualifier             {@link String} denoting the name of the qualifier variable.
	 * @param qualifierAggregation  {@link String} indicating how different levels of the qualifier variable are aggregated. Valid values are "ignore", "subtract", and "combine".
	 * @param normalization         {@link String} indicating what type of normalization will be used. Valid values are "no", "average activity", "Jaccard", and "cosine".
	 * @param twoMode               Create two-mode networks? If false, one-mode networks are created.
	 * @param start                 Start date of the time range over which the time window moves.
	 * @param stop                  End date of the time range over which the time window moves.
	 * @param unitType              {@link String} indicating the kind of temporal unit used for the moving window. Valid values are "using seconds", "using minutes", "using hours", "using days", "using weeks", "using months", "using years", and "using events".
	 * @param timeUnits             How large is the time window? E.g., 100 days, where "days" are defined in the unit type argument.
	 * @param includeIsolates       Boolean indicating whether all nodes should be present at all times
	 * @return                      {@link Matrix} object containing a one-mode network matrix.
	 */
	ArrayList<Matrix> computeTimeWindowMatrices(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, 
			String var1, String var2, boolean var1Document, boolean var2Document, String[] names1, String[] names2, String qualifier, 
			String qualifierAggregation, String normalization, boolean twoMode, Date start, Date stop, String unitType, int timeUnits, 
			boolean includeIsolates) {
		
		ArrayList<Matrix> timeWindowMatrices = new ArrayList<Matrix>();
		Collections.sort(statements);
		ArrayList<Statement> currentWindowStatements = new ArrayList<Statement>();
		Matrix m;
		if (unitType.equals("using events")) {
			int iteratorStart, iteratorStop;
			for (int t = 0; t < statements.size(); t++) {
				currentWindowStatements.clear();
				iteratorStart = t - (int) Math.round((double) (timeUnits - 1) / 2);
				iteratorStop = t + (int) Math.round((double) (timeUnits - 1) / 2);
				if (iteratorStart >= 0 && iteratorStop < statements.size()) {
					for (int i = iteratorStart; i <= iteratorStop; i++) {
						currentWindowStatements.add(statements.get(i));
					}
					if (currentWindowStatements.size() > 0) {
						if (includeIsolates == false) {
							names1 = extractLabels(currentWindowStatements, statements, documents, var1, var1Document, statementType.getId(), includeIsolates);
							names2 = extractLabels(currentWindowStatements, statements, documents, var2, var2Document, statementType.getId(), includeIsolates);
						}
						int firstDocId = currentWindowStatements.get(0).getDocumentId();
						Date first = null;
						for (int i = 0; i < documents.size(); i++) {
							if (firstDocId == documents.get(i).getId()) {
								first = documents.get(i).getDate();
								break;
							}
						}
						int lastDocId = currentWindowStatements.get(currentWindowStatements.size() - 1).getDocumentId();
						Date last = null;
						for (int i = documents.size() - 1; i > -1; i--) {
							if (lastDocId == documents.get(i).getId()) {
								last = documents.get(i).getDate();
								break;
							}
						}
						if (twoMode == true) {
							boolean verbose = false;
							m = computeTwoModeMatrix(currentWindowStatements, documents, statementType, var1, var2, var1Document, 
									var2Document, names1, names2, qualifier, qualifierAggregation, normalization, first, last, verbose);
						} else {
							m = computeOneModeMatrix(currentWindowStatements, documents, statementType, var1, var2, var1Document, 
									var2Document, names1, names2, qualifier, qualifierAggregation, normalization, first, last);
						}
						m.setDate(statements.get(t).getDate());
						m.setNumStatements(currentWindowStatements.size());
						timeWindowMatrices.add(m);
					}
				}
			}
		} else {
			GregorianCalendar startCalendar = new GregorianCalendar();  // start of statement list
			startCalendar.setTime(start);
			GregorianCalendar stopCalendar = new GregorianCalendar();   // end of statement list
			stopCalendar.setTime(stop);
			GregorianCalendar currentTime = new GregorianCalendar();    // current time while progressing through list of statements
			currentTime.setTime(start);
			GregorianCalendar windowStart = new GregorianCalendar();    // start of the time window
			windowStart.setTime(start);
			GregorianCalendar windowStop = new GregorianCalendar();     // end of the time window
			windowStop.setTime(start);
			GregorianCalendar iTime = new GregorianCalendar();          // time of the statement to be potentially added to the time slice
			int addition = 0;
			while (!currentTime.after(stopCalendar)) {
				Date matrixTime = currentTime.getTime();
				windowStart.setTime(matrixTime);
				windowStop.setTime(matrixTime);
				currentWindowStatements.clear();
				addition = (int) Math.round(((double) timeUnits - 1) / 2);
				if (unitType.equals("using seconds")) {
					windowStart.add(Calendar.SECOND, -addition);
					windowStop.add(Calendar.SECOND, addition);
					currentTime.add(Calendar.SECOND, 1);
				} else if (unitType.equals("using minutes")) {
					windowStart.add(Calendar.MINUTE, -addition);
					windowStop.add(Calendar.MINUTE, addition);
					currentTime.add(Calendar.MINUTE, 1);
				} else if (unitType.equals("using hours")) {
					windowStart.add(Calendar.HOUR_OF_DAY, -addition);
					windowStop.add(Calendar.HOUR_OF_DAY, addition);
					currentTime.add(Calendar.HOUR_OF_DAY, 1);
				} else if (unitType.equals("using days")) {
					windowStart.add(Calendar.DAY_OF_MONTH, -addition);
					windowStop.add(Calendar.DAY_OF_MONTH, addition);
					currentTime.add(Calendar.DAY_OF_MONTH, 1);
				} else if (unitType.equals("using weeks")) {
					windowStart.add(Calendar.WEEK_OF_YEAR, -addition);
					windowStop.add(Calendar.WEEK_OF_YEAR, addition);
					currentTime.add(Calendar.WEEK_OF_YEAR, 1);
				} else if (unitType.equals("using months")) {
					windowStart.add(Calendar.MONTH, -addition);
					windowStop.add(Calendar.MONTH, addition);
					currentTime.add(Calendar.MONTH, 1);
				} else if (unitType.equals("using years")) {
					windowStart.add(Calendar.YEAR, -addition);
					windowStop.add(Calendar.YEAR, addition);
					currentTime.add(Calendar.YEAR, 1);
				}
				if (!windowStart.before(startCalendar) && !windowStop.after(stopCalendar)) {
					for (int i = 0; i < statements.size(); i++) {
						iTime.setTime(statements.get(i).getDate());
						if (!iTime.before(windowStart) && !iTime.after(windowStop)) {
							currentWindowStatements.add(statements.get(i));
						}
					}
					if (currentWindowStatements.size() > 0) {
						if (includeIsolates == false) {
							names1 = extractLabels(currentWindowStatements, statements, documents, var1, var1Document, statementType.getId(), includeIsolates);
							names2 = extractLabels(currentWindowStatements, statements, documents, var2, var2Document, statementType.getId(), includeIsolates);
						}
						if (twoMode == true) {
							boolean verbose = false;
							m = computeTwoModeMatrix(currentWindowStatements, documents, statementType, var1, var2, var1Document, 
									var2Document, names1, names2, qualifier, qualifierAggregation, normalization, 
									windowStart.getTime(), windowStop.getTime(), verbose);
						} else {
							m = computeOneModeMatrix(currentWindowStatements, documents, statementType, var1, var2, var1Document, 
									var2Document, names1, names2, qualifier, qualifierAggregation, normalization, 
									windowStart.getTime(), windowStop.getTime());
						}
						m.setDate(matrixTime);
						m.setNumStatements(currentWindowStatements.size());
						timeWindowMatrices.add(m);
					}
				}
			}
		}
		return timeWindowMatrices;
	}
	
	
	/**
	 * Create a one-mode network {@link Matrix}.
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param documents             An {@link ArrayList} of {@link Document}s which contain the statements.
	 * @param statementType         The {@link StatementType} corresponding to the statements.
	 * @param var1                  {@link String} denoting the first variable (containing the row values).
	 * @param var2                  {@link String} denoting the second variable (containing the columns values).
	 * @param var1Document          {@link boolean} indicating whether the first variable is a document-level variable.
	 * @param var2Document          {@link boolean} indicating whether the second variable is a document-level variable.
	 * @param names1                {@link String} array containing the row labels.
	 * @param names2                {@link String} array containing the column labels.
	 * @param qualifier             {@link String} denoting the name of the qualifier variable.
	 * @param qualifierAggregation  {@link String} indicating how different levels of the qualifier variable are aggregated. Valid values are "ignore", "subtract", and "combine".
	 * @param normalization         {@link String} indicating what type of normalization will be used. Valid values are "no", "average activity", "Jaccard", and "cosine".
	 * @return                      {@link Matrix} object containing a one-mode network matrix.
	 */
	Matrix computeOneModeMatrix(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, 
			String var1, String var2, boolean var1Document, boolean var2Document, String[] names1, String[] names2, String qualifier, 
			String qualifierAggregation, String normalization, Date start, Date stop) {
		
		if (statements.size() == 0) {
			double[][] m = new double[names1.length][names1.length];
			Matrix mt = new Matrix(m, names1, names1, true, start, stop);
			return mt;
		}
		
		boolean booleanQualifier = true;  // is the qualifier boolean, rather than integer or null?
		if (qualifier == null || statementType.getVariables().get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (qualifier == null) {
			qualifierValues = new int[] { 0 };
		} else if (statementType.getVariables().get(qualifier).equals("integer")) {
			qualifierValues = getIntValues(statements, qualifier);
		} else {
			qualifierValues = new int[] {0, 1};
		}
		
		double[][][] array = createArray(statements, documents, statementType, var1, var2, var1Document, var2Document, 
				names1, names2, qualifier, qualifierAggregation);
		
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
						if (qualifierAggregation.equals("ignore")) {
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
						if (qualifierAggregation.equals("congruence") || qualifierAggregation.equals("subtract")) {
							for (int k1 = 0; k1 < qualifierValues.length; k1++) {
								for (int k2 = 0; k2 < qualifierValues.length; k2++) {
									mat1[i1][i2] = mat1[i1][i2] + (array[i1][j][k1] * array[i2][j][k2] * (1.0 - ((Math.abs(qualifierValues[k1] - qualifierValues[k2]) / range))));
								}
							}
						}
						// "conflict": same as congruence, but distance instead of proximity
						if (qualifierAggregation.equals("conflict") || qualifierAggregation.equals("subtract")) {
							for (int k1 = 0; k1 < qualifierValues.length; k1++) {
								for (int k2 = 0; k2 < qualifierValues.length; k2++) {
									mat2[i1][i2] = mat2[i1][i2] + (array[i1][j][k1] * array[i2][j][k2] * ((Math.abs(qualifierValues[k1] - qualifierValues[k2]) / range)));
								}
							}
						}
					}
					
					// normalization
					double norm = 1.0;
					if (normalization.equals("no")) {
						norm = 1.0;
					} else if (normalization.equals("average activity")) {
						i1count = 0.0;
						i2count = 0.0;
						for (int j = 0; j < names2.length; j++) {
							for (int k = 0; k < qualifierValues.length; k++) {
								i1count = i1count + array[i1][j][k];
								i2count = i2count + array[i2][j][k];
							}
						}
						norm = (i1count + i2count) / 2;
					} else if (normalization.equals("Jaccard")) {
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
					} else if (normalization.equals("cosine")) {
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
					if (qualifierAggregation.equals("ignore")) {
						m[i1][i2] = mat1[i1][i2];
					} else if (qualifierAggregation.equals("congruence")) {
						m[i1][i2] = mat1[i1][i2];
					} else if (qualifierAggregation.equals("conflict")) {
						m[i1][i2] = mat2[i1][i2];
					} else if (qualifierAggregation.equals("subtract")) {
						m[i1][i2] = mat1[i1][i2] - mat2[i1][i2];
					}
				}
			}
		}
		
		
		boolean integerBoolean;
		if (normalization.equals("no") && booleanQualifier == true) {
			integerBoolean = true;
		} else {
			integerBoolean = false;
		}
		
		Matrix matrix = new Matrix(m, names1, names1, integerBoolean, start, stop);
		return matrix;
	}
	
	/**
	 * Create a two-mode network {@link Matrix}.
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param documents             An {@link ArrayList} of {@link Document}s which contain the statements.
	 * @param statementType         The {@link StatementType} corresponding to the statements.
	 * @param var1                  {@link String} denoting the first variable (containing the row values).
	 * @param var2                  {@link String} denoting the second variable (containing the columns values).
	 * @param var1Document          {@link boolean} indicating whether the first variable is a document-level variable.
	 * @param var2Document          {@link boolean} indicating whether the second variable is a document-level variable.
	 * @param names1                {@link String} array containing the row labels.
	 * @param names2                {@link String} array containing the column labels.
	 * @param qualifier             {@link String} denoting the name of the qualifier variable.
	 * @param qualifierAggregation  {@link String} indicating how different levels of the qualifier variable are aggregated. Valid values are "ignore", "subtract", and "combine".
	 * @param normalization         {@link String} indicating what type of normalization will be used. Valid values are "no", "activity", and "prominence".
	 * @return                      {@link Matrix} object containing a two-mode network matrix.
	 */
	Matrix computeTwoModeMatrix(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, 
			String var1, String var2, boolean var1Document, boolean var2Document, String[] names1, String[] names2, String qualifier, 
			String qualifierAggregation, String normalization, Date start, Date stop, boolean verbose) {
		if (statements.size() == 0) {
			double[][] m = new double[names1.length][names2.length];
			Matrix mt = new Matrix(m, names1, names2, true, start, stop);
			return mt;
		}
		
		boolean booleanQualifier = true;  // is the qualifier boolean, rather than integer or null?
		if (qualifier == null) {
			booleanQualifier = false;
		} else if (statementType.getVariables().get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (qualifier == null) {
			qualifierValues = new int[] { 0 };
		} else if (statementType.getVariables().get(qualifier).equals("integer")) {
			qualifierValues = getIntValues(statements, qualifier);
		} else {
			qualifierValues = new int[] {0, 1};
		}
		
		double[][][] array = createArray(statements, documents, statementType, var1, var2, var1Document, var2Document, 
				names1, names2, qualifier, qualifierAggregation);
		
		// combine levels of the qualifier variable conditional on qualifier aggregation option
		double[][] mat = new double[names1.length][names2.length];  // initialized with zeros
		HashMap<Integer, ArrayList<Integer>> combinations = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < names1.length; i++) {
			for (int j = 0; j < names2.length; j++) {
				if (qualifierAggregation.equals("combine")) {  // combine
					double[] vec = array[i][j];  // may be weighted, so create a second, binary vector vec2
					int[] vec2 = new int[vec.length];
					ArrayList<Integer> qualVal = new ArrayList<Integer>();  // a list of qualifier values used at mat[i][j]
					for (int k = 0; k < vec.length; k++) {
						if (vec[k] > 0) {
							vec2[k] = 1;
							qualVal.add(qualifierValues[k]);
						}
					}
					mat[i][j] = lexRank(vec2);  // compute lexical rank, i.e., map the combination of values to a single integer
					combinations.put(lexRank(vec2), qualVal);  // the bijection needs to be stored for later reporting
				} else {
					for (int k = 0; k < qualifierValues.length; k++) {
						if (qualifierAggregation.equals("ignore")) {  // ignore
							mat[i][j] = mat[i][j] + array[i][j][k];  // duplicates were already filtered out in the statement filter, so just add
						} else if (qualifierAggregation.equals("subtract")) {  // subtract
							if (booleanQualifier == false && qualifierValues[k] < 0) {  // subtract weighted absolute value
								mat[i][j] = mat[i][j] - (Math.abs(qualifierValues[k]) * array[i][j][k]);
							} else if (booleanQualifier == false && qualifierValues[k] >= 0) {  // add weighted absolute value
								mat[i][j] = mat[i][j] + (Math.abs(qualifierValues[k]) * array[i][j][k]);
							} else if (booleanQualifier == true && qualifierValues[k] == 0) {  // subtract 1 at most
								mat[i][j] = mat[i][j] - array[i][j][k];
							} else if (booleanQualifier == true && qualifierValues[k] > 0) {  // add 1 at most
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
		if (normalization.equals("no")) {
			integerBoolean = true;
		} else if (normalization.equals("activity")) {
			integerBoolean = false;
			double currentDenominator;
			for (int i = 0; i < names1.length; i++) {
				currentDenominator = 0.0;
				if (qualifierAggregation.equals("ignore")) {  // iterate through columns of matrix and sum weighted values
					for (int j = 0; j < names2.length; j++) {
						currentDenominator = currentDenominator + mat[i][j];
					}
				} else if (qualifierAggregation.equals("combine")) {  // iterate through columns of matrix and count how many are larger than one
					System.err.println("Warning: Normalization and qualifier setting 'combine' yield results that cannot be interpreted.");
					for (int j = 0; j < names2.length; j++) {
						if (mat[i][j] > 0.0) {
							currentDenominator = currentDenominator + 1.0;
						}
					}
				} else if (qualifierAggregation.equals("subtract")) {  // iterate through array and sum for different levels
					for (int j = 0; j < names2.length; j++) {
						for (int k = 0; k < qualifierValues.length; k++) {
							currentDenominator = currentDenominator + array[i][j][k];
						}
					}
				}
				for (int j = 0; j < names2.length; j++) {  // divide all values by current denominator
					mat[i][j] = mat[i][j] / currentDenominator;
				}
			}
		} else if (normalization.equals("prominence")) {
			integerBoolean = false;
			double currentDenominator;
			for (int i = 0; i < names2.length; i++) {
				currentDenominator = 0.0;
				if (qualifierAggregation.equals("ignore")) {  // iterate through rows of matrix and sum weighted values
					for (int j = 0; j < names1.length; j++) {
						currentDenominator = currentDenominator + mat[j][i];
					}
				} else if (qualifierAggregation.equals("combine")) {  // iterate through rows of matrix and count how many are larger than one
					System.err.println("Warning: Normalization and qualifier setting 'combine' yield results that cannot be interpreted.");
					for (int j = 0; j < names1.length; j++) {
						if (mat[i][j] > 0.0) {
							currentDenominator = currentDenominator + 1.0;
						}
					}
				} else if (qualifierAggregation.equals("subtract")) {  // iterate through array and sum for different levels
					for (int j = 0; j < names1.length; j++) {
						for (int k = 0; k < qualifierValues.length; k++) {
							currentDenominator = currentDenominator + array[j][i][k];
						}
					}
				}
				for (int j = 0; j < names1.length; j++) {  // divide all values by current denominator
					mat[j][i] = mat[j][i] / currentDenominator;
				}
			}
		}
		
		// create Matrix object and return
		Matrix matrix = new Matrix(mat, names1, names2, integerBoolean, start, stop); // assemble the Matrix object with labels
		return matrix;
	}
	
	/**
	 * Create a three-dimensional array (variable 1 x variable 2 x qualifier).
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param documents             An {@link ArrayList} of {@link Document}s which contain the statements.
	 * @param statementType         The {@link StatementType} corresponding to the statements.
	 * @param var1                  {@link String} denoting the first variable (containing the row values).
	 * @param var2                  {@link String} denoting the second variable (containing the columns values).
	 * @param var1Document          {@link boolean} indicating whether the first variable is a document-level variable.
	 * @param var2Document          {@link boolean} indicating whether the second variable is a document-level variable.
	 * @param names1                {@link String} array containing the row labels.
	 * @param names2                {@link String} array containing the column labels.
	 * @param qualifier             {@link String} denoting the name of the qualifier variable. Can be null.
	 * @param qualifierAggregation  {@link String} indicating how different levels of the qualifier variable are aggregated. Valid values are "ignore", "subtract", and "combine".
	 * @return                      3D double array
	 */
	private double[][][] createArray(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, 
			String var1, String var2, boolean var1Document, boolean var2Document, String[] names1, String[] names2, String qualifier, 
			String qualifierAggregation) {
		
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (qualifier == null) {
			qualifierValues = null;
		} else if (statementType.getVariables().get(qualifier).equals("integer")) {
			qualifierValues = getIntValues(statements, qualifier);
		} else {
			qualifierValues = new int[] {0, 1};
		}

		// Create arrays with variable values
		String[] values1 = retrieveValues(statements, documents, var1, var1Document);
		String[] values2 = retrieveValues(statements, documents, var2, var2Document);
		
		// create and populate array
		double[][][] array;
		if (qualifierValues == null) {
			array = new double[names1.length][names2.length][1];
		} else {
			array = new double[names1.length][names2.length][qualifierValues.length]; // 3D array: rows x cols x qualifier value
		}
		for (int i = 0; i < statements.size(); i++) {
			String n1 = values1[i];  // retrieve first value from statement
			String n2 = values2[i];  // retrieve second value from statement
			int q;
			if (qualifier == null) {
				q = 0;
			} else {
				q = (int) statements.get(i).getValues().get(qualifier);  // retrieve qualifier value from statement
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
			} else {
				qual = -1;  // qualifier level in the array
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
	 * Retrieve unique integer values from an integer qualifier variable
	 * @param statements     An array list of statements.
	 * @param qualifier      Name of the qualifier variable as a string.
	 * @return               Vector of unique int values.
	 */
	private int[] getIntValues(ArrayList<Statement> statements, String qualifier) {
		ArrayList<Integer> al = new ArrayList<Integer>();
		for (int i = 0; i < statements.size(); i++) {
			int q = (int) statements.get(i).getValues().get(qualifier);
			if (!al.contains(q)) {
				al.add(q);
			}
		}
		Collections.sort(al);
		int[] qualifierValues = new int[al.size()];
		for (int i = 0; i < al.size(); i++) {
			qualifierValues[i] = (int) al.get(i);
		}
		return qualifierValues;
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
	 * This bijection is used to map combinations of qualifier values into edge weights in the resulting network matrix.
	 * 
	 * Source: https://cw.fel.cvut.cz/wiki/_media/courses/b4m33pal/pal06.pdf
	 * 
	 * @param binaryVector  A binary int array of arbitrary length, indicating which qualifier values are used in the dataset
	 * @return              An integer
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
	 * This function is supposed to convert a rank back into a binary vector, but it does not seem to work properly.
	 * 
	 * Source: https://cw.fel.cvut.cz/wiki/_media/courses/b4m33pal/pal06.pdf
	 * 
	 * @param rank  The integer rank
	 * @param n     Length of the binary vector
	 * @return      Binary vector of length n
	 */
	/*
	private int[] lexUnrank(int rank, int n) {
		int[] binaryVector = new int[n];
		for (int i = n; i > 0; i--) {
			if (rank % 2 == 1) {
				binaryVector[i - 1] = 1;
				rank = (int) Math.floor(rank / 2);
			}
		}
		return binaryVector;
	}
	*/

	/**
	 * This function accepts a list of statements that should be included in the relational event export, 
	 * and it exports the variables of all statements to a CSV file, along with the statement ID and a 
	 * date/time stamp. There is one statement per row, and the number of columns is the number of variables 
	 * present in the statement type.
	 * 
	 * @param statements	 An array list of {@link Statement}s (of the same statement type) that should be exported.
	 * @param documents      An array list of {@link Document}s in which the statements are embedded.
	 * @param statementType  The statement type corresponding to the statements.
	 * @param fileName		 String with the file name of the CSV file to which the event list will be exported.
	 */
	void eventCSV(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, String fileName) {
		String key, value;
		int statementId;
		Date d;
		SimpleDateFormat dateFormat;
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
		
		HashMap<String, String> variables = statementType.getVariables();
		Iterator<String> keyIterator;
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
			keyIterator = variables.keySet().iterator();
			out.write("\"statement ID\";\"time\";\"document ID\";\"document title\";\"author\";\"source\";\"section\";\"type\";\"text\"");
			while (keyIterator.hasNext()){
				out.write(";\"" + keyIterator.next() + "\"");
			}
			for (int i = 0; i < statements.size(); i++) {
				out.newLine();
				statementId = statements.get(i).getId();
				String stringId = Integer.valueOf(statementId).toString();
				out.write(stringId);
				d = statements.get(i).getDate();
				dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				out.write(";" + dateFormat.format(d));
				Document doc = documents.get(docMap.get(statements.get(i).getDocumentId()));
				out.write(";" + doc.getId());
				out.write(";\"" + doc.getTitle().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getAuthor().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getSource().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getSection().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getType().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getText().substring(statements.get(i).getStart(), 
						statements.get(i).getStop()).replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				keyIterator = variables.keySet().iterator();
				while (keyIterator.hasNext()){
					key = keyIterator.next();
					value = variables.get(key);
					if (value.equals("short text") || value.equals("long text")) {
						out.write(";\"" + ((String) statements.get(i).getValues().get(key)).replaceAll(";", ",").replaceAll("\"", "'") + "\"");
					} else if (value.equals("boolean") || value.equals("integer")) {
						out.write(";" + statements.get(i).getValues().get(key));
					}
				}
			}
			out.close();
			System.out.println("Event list has been exported to \"" + fileName + "\".");
		} catch (IOException e) {
			System.err.println("Error while saving CSV file: " + e);
		}
	}

	/**
	 * Export {@link Matrix} to a CSV matrix file.
	 * 
	 * @param matrix   The input {@link Matrix} object.
	 * @param outfile  The path and file name of the target CSV file.
	 */
	void exportCSV (Matrix m, String outfile) {
		String[] rn = m.getRownames();
		String[] cn = m.getColnames();
		int nr = rn.length;
		int nc = cn.length;
		double[][] mat = m.getMatrix();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("\"\"");
			for (int i = 0; i < nc; i++) {
				out.write(";\"" + cn[i].replaceAll("\"", "'") + "\"");
			}
			for (int i = 0; i < nr; i++) {
				out.newLine();
				out.write("\"" + rn[i].replaceAll("\"", "'") + "\"");
				for (int j = 0; j < nc; j++) {
					if (m.getInteger() == true) {
						out.write(";" + (int) mat[i][j]);
					} else {
						out.write(";" + String.format(new Locale("en"), "%.6f", mat[i][j]));  // six decimal places
					}
				}
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while saving CSV matrix file.");
		}
	}
	
	/**
	 * Export network to a DL fullmatrix file for the software UCINET.
	 * 
	 * @param matrix   The input {@link Matrix} object.
	 * @param outfile  The path and file name of the target .dl file.
	 * @param twoMode  A {@link boolean} indicating if the input matrix is a two-mode network matrix (rather than one-mode). 
	 */
	void exportDL (Matrix m, String outfile, boolean twoMode) {
		String[] rn = m.getRownames();
		String[] cn = m.getColnames();
		int nr = rn.length;
		int nc = cn.length;
		double[][] mat = m.getMatrix();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("dl ");
			if (twoMode == false) {
				out.write("n = " + nr);
			} else if (twoMode == true) {
				out.write("nr = " + nr + ", nc = " + nc);
			}
			out.write(", format = fullmatrix");
			out.newLine();
			if (twoMode == true) {
				out.write("row labels:");
			} else {
				out.write("labels:");
			}
			for (int i = 0; i < nr; i++) {
				out.newLine();
				out.write("\"" + rn[i].replaceAll("\"", "'").replaceAll("'", "") + "\"");
			}
			if (twoMode == true) {
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
					if (m.getInteger() == true) {
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
	
	/**
	 * Export filter for graphML files.
	 * 
	 * @param matrix                 Input {@link Matrix}.
	 * @param twoMode                Indicates whether the network is a two-mode network.
	 * @param statementType          The statement type on which the network is based.
	 * @param outfile                Name of the output file.
	 * @param var1                   Name of the first variable (the rows of the matrix).
	 * @param var2                   Name of the second variable (the columns of the matrix).
	 * @param frequencies1           The number of statements in which the row node is involved (after filtering).
	 * @param frequencies2           The number of statements in which the column node is involved (after filtering).
	 * @param attributes             An ArrayList of {@link AttributeVector}s containing all attribute vectors in the database.
	 * @param qualifierAggregation   A String denoting the qualifier aggregation. Valid values are "ignore", "combine", "subtract", "congruence", and "conflict".
	 * @param qualifierBinary        Indicates whether the qualifier is a binary variable.
	 */
	void exportGraphml(Matrix mt, boolean twoMode, StatementType statementType, String outfile, 
			String var1, String var2, int[] frequencies1, int[] frequencies2, ArrayList<AttributeVector> attributes, 
			String qualifierAggregation, boolean qualifierBinary) {
		
		// extract attributes
		String[] rn = mt.getRownames();
		String[] cn = mt.getColnames();
		String[] names;
		String[] variables;
		int[] frequencies;
		if (twoMode == true) {
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
			variables[i] = var1;
			frequencies[i] = frequencies1[i];
		}
		if (twoMode == true) {
			for (int i = 0; i < cn.length; i++) {
				names[i + rn.length] = cn[i];
				variables[i + rn.length] = var2;
				frequencies[i + rn.length] = frequencies2[i];
			}
		}
		int[] id = new int[names.length];
		String[] color = new String[names.length];
		String[] type = new String[names.length];
		String[] alias = new String[names.length];
		String[] notes = new String[names.length];
		for (int i = 0; i < attributes.size(); i++) {
			if (attributes.get(i).getStatementTypeId() == statementType.getId() && attributes.get(i).getVariable().equals(var1)) {
				for (int j = 0; j < rn.length; j++) {
					if (rn[j].equals(attributes.get(i).getValue())) {
						id[j] = attributes.get(i).getId();
						color[j] = String.format("#%02X%02X%02X", attributes.get(i).getColor().getRed(), 
								attributes.get(i).getColor().getGreen(), attributes.get(i).getColor().getBlue());
						type[j] = attributes.get(i).getType();
						alias[j] = attributes.get(i).getAlias();
						notes[j] = attributes.get(i).getNotes();
					}
				}
			} else if (attributes.get(i).getStatementTypeId() == statementType.getId() && attributes.get(i).getVariable().equals(var2) && twoMode == true) {
				for (int j = 0; j < cn.length; j++) {
					if (cn[j].equals(attributes.get(i).getValue())) {
						id[j + rn.length] = attributes.get(i).getId();
						color[j + rn.length] = String.format("#%02X%02X%02X", attributes.get(i).getColor().getRed(), 
								attributes.get(i).getColor().getGreen(), attributes.get(i).getColor().getBlue());
						type[j + rn.length] = attributes.get(i).getType();
						alias[j + rn.length] = attributes.get(i).getAlias();
						notes[j + rn.length] = attributes.get(i).getNotes();
					}
				}
			}
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
		
		Element keyType = new Element("key", xmlns);
		keyType.setAttribute(new Attribute("id", "type"));
		keyType.setAttribute(new Attribute("for", "node"));
		keyType.setAttribute(new Attribute("attr.name", "type"));
		keyType.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyType);
		
		Element keyAlias = new Element("key", xmlns);
		keyAlias.setAttribute(new Attribute("id", "alias"));
		keyAlias.setAttribute(new Attribute("for", "node"));
		keyAlias.setAttribute(new Attribute("attr.name", "alias"));
		keyAlias.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyAlias);
		
		Element keyNote = new Element("key", xmlns);
		keyNote.setAttribute(new Attribute("id", "note"));
		keyNote.setAttribute(new Attribute("for", "node"));
		keyNote.setAttribute(new Attribute("attr.name", "note"));
		keyNote.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyNote);
		
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
		if (twoMode == false) {
			numEdges = (numEdges / 2) - rn.length;
		}
		int numNodes = rn.length;
		if (twoMode == true) {
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
			
			Element typeElement = new Element("data", xmlns);
			typeElement.setAttribute(new Attribute("key", "type"));
			typeElement.setText(type[i]);
			node.addContent(typeElement);
			
			Element aliasElement = new Element("data", xmlns);
			aliasElement.setAttribute(new Attribute("key", "alias"));
			aliasElement.setText(alias[i]);
			node.addContent(aliasElement);
			
			Element notesElement = new Element("data", xmlns);
			notesElement.setAttribute(new Attribute("key", "notes"));
			notesElement.setText(notes[i]);
			node.addContent(notesElement);
			
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
		double[][] m = mt.getMatrix();
		Comment edges = new Comment(" edges ");
		graphElement.addContent(edges);
		for (int i = 0; i < rn.length; i++) {
			for (int j = 0; j < cn.length; j++) {
				if (m[i][j] != 0.0 && (twoMode == true || (twoMode == false && i < j))) {  // only lower triangle is used for one-mode networks
					Element edge = new Element("edge", xmlns);
					
					int currentId = id[i];
					edge.setAttribute(new Attribute("source", "n" + String.valueOf(currentId)));
					if (twoMode == true) {
						currentId = id[j + rn.length];
					} else {
						currentId = id[j];
					}
					edge.setAttribute(new Attribute("target", "n" + String.valueOf(currentId)));
					
					Element weight = new Element("data", xmlns);
					weight.setAttribute(new Attribute("key", "weight"));
					weight.setText(String.valueOf(m[i][j]));
					edge.addContent(weight);

					Element visEdge = new Element("data", xmlns);
					visEdge.setAttribute("key", "e0");
					Element visPolyLineEdge = new Element("polyLineEdge", visone);
					Element yPolyLineEdge = new Element("PolyLineEdge", yNs);

					Element yLineStyle = new Element("LineStyle", yNs);
					if (qualifierAggregation.equals("combine") && qualifierBinary == true) {
						if (m[i][j] == 1.0) {
							yLineStyle.setAttribute("color", "#00ff00");
						} else if (m[i][j] == 2.0) {
							yLineStyle.setAttribute("color", "#ff0000");
						} else if (m[i][j] == 3.0) {
							yLineStyle.setAttribute("color", "#0000ff");
						}
					} else if (qualifierAggregation.equals("subtract")) {
						if (m[i][j] < 0) {
							yLineStyle.setAttribute("color", "#ff0000");
						} else if (m[i][j] > 0) {
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
		File dnaFile = new File(outfile);
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

	/*
	void exportGEFX(Matrix mt, boolean twoMode, StatementType statementType, String outfile, String var1, String var2, 
			int[] frequencies1, int[] frequencies2, ArrayList<AttributeVector> attributes, String qualifierAggregation, 
			boolean qualifierBinary) {

		// extract attributes
		String[] rn = mt.getRownames();
		String[] cn = mt.getColnames();
		String[] names;
		String[] variables;
		int[] frequencies;
		if (twoMode == true) {
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
			variables[i] = var1;
			frequencies[i] = frequencies1[i];
		}
		if (twoMode == true) {
			for (int i = 0; i < cn.length; i++) {
				names[i + rn.length] = cn[i];
				variables[i + rn.length] = var2;
				frequencies[i + rn.length] = frequencies2[i];
			}
		}
		int[] id = new int[names.length];
		java.awt.Color[] color = new java.awt.Color[names.length];
		String[] type = new String[names.length];
		String[] alias = new String[names.length];
		String[] notes = new String[names.length];
		for (int i = 0; i < attributes.size(); i++) {
			if (attributes.get(i).getStatementTypeId() == statementType.getId() && attributes.get(i).getVariable().equals(var1)) {
				for (int j = 0; j < rn.length; j++) {
					if (rn[j].equals(attributes.get(i).getValue())) {
						id[j] = attributes.get(i).getId();
						color[j] = attributes.get(i).getColor();
						type[j] = attributes.get(i).getType();
						alias[j] = attributes.get(i).getAlias();
						notes[j] = attributes.get(i).getNotes();
					}
				}
			} else if (attributes.get(i).getStatementTypeId() == statementType.getId() && attributes.get(i).getVariable().equals(var2) && twoMode == true) {
				for (int j = 0; j < cn.length; j++) {
					if (cn[j].equals(attributes.get(i).getValue())) {
						id[j + rn.length] = attributes.get(i).getId();
						color[j + rn.length] = attributes.get(i).getColor();
						type[j + rn.length] = attributes.get(i).getType();
						alias[j + rn.length] = attributes.get(i).getAlias();
						notes[j + rn.length] = attributes.get(i).getNotes();
					}
				}
			}
		}
		
		
		
		DocType dt = new DocType("xml");
		org.jdom.Document document = new org.jdom.Document();
		document.setDocType(dt);
		
		// gexf element with schema information
		Element gexfElement = new Element("gexf");
		gexfElement.setAttribute(new Attribute("xmlns", "http://www.gexf.net/1.2draft"));
		gexfElement.setAttribute(new Attribute("xmlns:viz", "http://www.gexf.net/1.2draft/viz"));
		gexfElement.setAttribute(new Attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"));
		gexfElement.setAttribute(new Attribute("xsi:schemaLocation", "http://www.gexf.net/1.2draft/gexf.xsd"));
		gexfElement.setAttribute(new Attribute("version", "1.2"));
		document.addContent(gexfElement);
		
		// meta data
		Element meta = new Element("meta");
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
		Date today = new Date();
		meta.setAttribute(new Attribute("lastmodifieddate", df.format(today)));
		Element creator = new Element("creator");
		creator.setText("Discourse Network Analyzer (DNA)");
		meta.addContent(creator);
		Element description = new Element("description");
		if (twoMode == true) {
			description.setText("Two-mode network");
		} else {
			description.setText("One-mode network");
		}
		meta.addContent(description);
		gexfElement.addContent(meta);
		
		// graph element with node and edge attribute definitions
		Element graphElement = new Element("graph");
		graphElement.setAttribute(new Attribute("mode", "static"));
		graphElement.setAttribute(new Attribute("defaultedgetype", "undirected"));
		
		Element attributesElement = new Element("attributes");
		attributesElement.setAttribute(new Attribute("class", "node"));
		Element at0 = new Element("attribute");
		at0.setAttribute(new Attribute("id", "0"));
		at0.setAttribute(new Attribute("title", "type"));
		at0.setAttribute(new Attribute("type", "string"));
		attributesElement.addContent(at0);
		Element at1 = new Element("attribute");
		at1.setAttribute(new Attribute("id", "1"));
		at1.setAttribute(new Attribute("title", "alias"));
		at1.setAttribute(new Attribute("type", "string"));
		attributesElement.addContent(at1);
		Element at2 = new Element("attribute");
		at2.setAttribute(new Attribute("id", "2"));
		at2.setAttribute(new Attribute("title", "notes"));
		at2.setAttribute(new Attribute("type", "string"));
		attributesElement.addContent(at2);
		
		//<attributes class="node">
	    //  <attribute id="0" title="url" type="string"/>
	    //  <attribute id="1" title="indegree" type="float"/>
	    //  <attribute id="2" title="frog" type="boolean">
	    //    <default>true</default>
	    //  </attribute>
	    //</attributes>
		
		// add nodes
		Element nodes = new Element("nodes");
		for (int i = 0; i < names.length; i++) {
			Element node = new Element("node");
			node.setAttribute(new Attribute("id", "" + id[i]));
			node.setAttribute(new Attribute("label", "" + names[i]));
			
			Element attvaluesElement = new Element("attvalues");
			
			Element attvalueElement0 = new Element("attvalue");
			attvalueElement0.setAttribute(new Attribute("for", "0"));
			attvalueElement0.setAttribute(new Attribute("value", type[i]));
			attvaluesElement.addContent(attvalueElement0);

			Element attvalueElement1 = new Element("attvalue");
			attvalueElement1.setAttribute(new Attribute("for", "1"));
			attvalueElement1.setAttribute(new Attribute("value", alias[i]));
			attvaluesElement.addContent(attvalueElement1);

			Element attvalueElement2 = new Element("attvalue");
			attvalueElement2.setAttribute(new Attribute("for", "2"));
			attvalueElement2.setAttribute(new Attribute("value", notes[i]));
			attvaluesElement.addContent(attvalueElement2);

			Element attvalueElement3 = new Element("attvalue");
			attvalueElement3.setAttribute(new Attribute("for", "3"));
			attvalueElement3.setAttribute(new Attribute("value", variables[i]));
			attvaluesElement.addContent(attvalueElement3);

			Element attvalueElement4 = new Element("attvalue");
			attvalueElement4.setAttribute(new Attribute("for", "4"));
			attvalueElement4.setAttribute(new Attribute("value", String.valueOf(frequencies[i])));
			attvaluesElement.addContent(attvalueElement4);
			
			node.addContent(attvaluesElement);
			
			Element vizColor = new Element("viz:color");
			vizColor.setAttribute(new Attribute("r", "" + color[i].getRed()));
			vizColor.setAttribute(new Attribute("g", "" + color[i].getGreen()));
			vizColor.setAttribute(new Attribute("b", "" + color[i].getBlue()));
			vizColor.setAttribute(new Attribute("a", "" + color[i].getAlpha()));
			node.addContent(vizColor);
			
			Element vizShape = new Element("viz:shape");
			if (i < rn.length) {
				vizShape.setAttribute(new Attribute("value", "disc"));
			} else {
				vizShape.setAttribute(new Attribute("value", "square"));
			}
			node.addContent(vizShape);
			
			nodes.addContent(node);
		}
		graphElement.addContent(nodes);
		
		// add edges
		Element edges = new Element("edges");
		
		graphElement.addContent(edges);
		
		gexfElement.addContent(graphElement);
		
		// write to file
		File dnaFile = new File(outfile);
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
	*/
}