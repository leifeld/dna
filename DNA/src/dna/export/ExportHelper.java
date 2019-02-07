package dna.export;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import dna.dataStructures.Document;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementType;

public class ExportHelper {

	public ExportHelper() {
		// TODO Auto-generated constructor stub
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
							&& (s.getValues().get(qualifierName).equals(al.get(j).getValues().get(qualifierName)) || ignoreQualifier == true) ) {
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
		String docAuthor, docSource, docSection, docType;
		String[] values = new String[statements.size()];
		for (int i = 0; i < statements.size(); i++) {
			s = statements.get(i);
			docAuthor = documents.get(docMap.get(s.getDocumentId())).getAuthor();
			docSource = documents.get(docMap.get(s.getDocumentId())).getSource();
			docSection = documents.get(docMap.get(s.getDocumentId())).getSection();
			docType = documents.get(docMap.get(s.getDocumentId())).getType();
			if (documentLevel == true) {
				if (variable.equals("author")) {
					values[i] = docAuthor;
				} else if (variable.equals("source")) {
					values[i] = docSource;
				} else if (variable.equals("section")) {
					values[i] = docSection;
				} else if (variable.equals("type")) {
					values[i] = docType;
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
						if (twoMode == true) {
							boolean verbose = false;
							m = computeTwoModeMatrix(currentWindowStatements, documents, statementType, var1, var2, var1Document, 
									var2Document, names1, names2, qualifier, qualifierAggregation, normalization, verbose);
						} else {
							m = computeOneModeMatrix(currentWindowStatements, documents, statementType, var1, var2, var1Document, 
									var2Document, names1, names2, qualifier, qualifierAggregation, normalization);
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
									var2Document, names1, names2, qualifier, qualifierAggregation, normalization, verbose);
						} else {
							m = computeOneModeMatrix(currentWindowStatements, documents, statementType, var1, var2, var1Document, 
									var2Document, names1, names2, qualifier, qualifierAggregation, normalization);
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
			String qualifierAggregation, String normalization) {
		
		if (statements.size() == 0) {
			double[][] m = new double[names1.length][names1.length];
			Matrix mt = new Matrix(m, names1, names1, true);
			return mt;
		}
		
		boolean booleanQualifier = true;  // is the qualifier boolean, rather than integer?
		if (statementType.getVariables().get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (booleanQualifier == true) {
			qualifierValues = new int[] {0, 1};
		} else {
			qualifierValues = getIntValues(statements, qualifier);
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
		
		Matrix matrix = new Matrix(m, names1, names1, integerBoolean);
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
			String qualifierAggregation, String normalization, boolean verbose) {
		if (statements.size() == 0) {
			double[][] m = new double[names1.length][names2.length];
			Matrix mt = new Matrix(m, names1, names2, true);
			return mt;
		}
		boolean booleanQualifier = true;  // is the qualifier boolean, rather than integer?
		// TODO: it may be possible that there is no qualifier; adjust for this case (also in the one-mode case?)
		if (statementType.getVariables().get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (booleanQualifier == true) {
			qualifierValues = new int[] {0, 1};
		} else {
			qualifierValues = getIntValues(statements, qualifier);
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
						if (verbose == true) {
							System.out.print(values.get(i) + " ");
						}
					}
				}
			}
			System.out.print("\n");
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
		Matrix matrix = new Matrix(mat, names1, names2, integerBoolean); // assemble the Matrix object with labels
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
	 * @param qualifier             {@link String} denoting the name of the qualifier variable.
	 * @param qualifierAggregation  {@link String} indicating how different levels of the qualifier variable are aggregated. Valid values are "ignore", "subtract", and "combine".
	 * @return                      3D double array
	 */
	private double[][][] createArray(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, 
			String var1, String var2, boolean var1Document, boolean var2Document, String[] names1, String[] names2, String qualifier, 
			String qualifierAggregation) {
		
		boolean booleanQualifier = true;  // is the qualifier boolean, rather than integer?
		if (statementType.getVariables().get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (booleanQualifier == true) {
			qualifierValues = new int[] {0, 1};
		} else {
			qualifierValues = getIntValues(statements, qualifier);
		}

		// Create arrays with variable values
		String[] values1 = retrieveValues(statements, documents, var1, var1Document);
		String[] values2 = retrieveValues(statements, documents, var2, var2Document);
		
		// create and populate array
		double[][][] array = new double[names1.length][names2.length][qualifierValues.length]; // 3D array: rows x cols x qualifier value
		for (int i = 0; i < statements.size(); i++) {
			String n1 = values1[i];  // retrieve first value from statement
			String n2 = values2[i];  // retrieve second value from statement
			int q = (int) statements.get(i).getValues().get(qualifier);  // retrieve qualifier value from statement
			
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
			int qual = -1;  // qualifier level in the array
			for (int j = 0; j < qualifierValues.length; j++) {
				if (qualifierValues[j] == q) {
					qual = j;
					break;
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
	
}
