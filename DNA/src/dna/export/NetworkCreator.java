package dna.export;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import dna.Dna;
import dna.dataStructures.Statement;

public class NetworkCreator {

	public NetworkCreator() {
		//TODO
	}

	/**
	 * Return a filtered list of {@link SidebarStatement}s based on the settings saved in the {@link NetworkExporterObject}.
	 * 
	 * @return	ArrayList of filtered {@link SidebarStatement}s
	 */
	/*
	ArrayList<Statement> filter() {
		// step 1: get all statement IDs corresponding to date range and statement type
		ArrayList<Integer> al = new ArrayList<Integer>();
		for (int i = 0; i < Dna.dna.gui.rightPanel.statementPanel.ssc.size(); i++) {
			boolean select = true;
			Statement s = Dna.dna.gui.rightPanel.statementPanel.ssc.get(i);
			if (s.getDate().before(startDate)) {
				select = false;
			} else if (s.getDate().after(stopDate)) {
				select = false;
			} else if (s.getStatementTypeId() != statementType.getId()) {
				select = false;
			}
			if (select == true) {
				al.add(s.getId());
			}
		}
		
		// step 2: identify variables with excluded values
		ArrayList<ExcludeObject> exObj = new ArrayList<ExcludeObject>();
		Iterator<String> keyIterator = excludeValIndices.keySet().iterator();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			int[] value = excludeValIndices.get(key);
			if (value.length > 0) {
				String dt = statementType.getVariables().get(key);
				ExcludeObject eo;
				if (dt.equals("short text") || dt.equals("long text")) {
					ArrayList<String> values = getUniqueValuesAsString(key); // all values of a certain variable (as contained in the right JList in the exclude panel)
					int[] selectedIndices = excludeValIndices.get(key);  // indices: which of them are selected to be excluded?
					ArrayList<String> selectedValues = new ArrayList<String>();  // actual values: which of them are selected to be excluded?
					for (int j = 0; j < selectedIndices.length; j++) {
						selectedValues.add(values.get(selectedIndices[j]));
					}
					eo = new ExcludeObject(key, true, selectedValues);  // a variable along with its values to be excluded
				} else {
					ArrayList<Integer> values = getUniqueIntValues(key);
					int[] selectedIndices = excludeValIndices.get(key);
					ArrayList<Integer> selectedValues = new ArrayList<Integer>();
					for (int j = 0; j < selectedIndices.length; j++) {
						selectedValues.add(values.get(selectedIndices[j]));
					}
					eo = new ExcludeObject(key, false, selectedValues);
				}
				exObj.add(eo);
			}
		}
		
		// step 3: get all statements from database and check against values selected for inclusion
		ArrayList<Integer> keepIds = new ArrayList<Integer>();
		for (int i = 0; i < al.size(); i++) {
			boolean select = true;
			for (int j = 0; j < exObj.size(); j++) {
				String ex = exObj.get(j).getExVar();
				//if ((exObj.get(j).isExStr() == true && exObj.get(j).getExVal().contains(Dna.dna.db.getVariableStringEntry(al.get(i), exObj.get(j).getExVar()))) || 
				//		(exObj.get(j).isExStr() == false && exObj.get(j).getExVal().contains(Dna.dna.db.getVariableIntEntry(al.get(i), exObj.get(j).getExVar())))) {
				if (exObj.get(j).getExVal().contains(Dna.data.getStatement(al.get(i)).getValues().get(ex))) {
					select = false;
				}
			}
			if (select == true) {
				keepIds.add(al.get(i));
			}
		}
		al = keepIds;
		
		// step 4: create reduced array list with SidebarStatements for the computations
		ArrayList<Statement> l = new ArrayList<Statement>();
		for (int i = 0; i < Dna.dna.gui.rightPanel.statementPanel.ssc.size(); i++) {
			Statement s = Dna.dna.gui.rightPanel.statementPanel.ssc.get(i);
			if (al.contains(s.getId())) {
				l.add(s);
			}
		}
		return(l);
	}
	*/
	
	/**
	 * Compute the matrix product of two two-dimensional arrays.
	 * 
	 * @param mat1	Two-dimensional array with the first input matrix.
	 * @param mat2	Two-dimensional array with the second input matrix.
	 * @return		Two-dimensional array with the output matrix.
	 */
	public static double[][] multiply(double[][] mat1, double[][] mat2) {
        int aRows = mat1.length;
        int aColumns = mat1[0].length;
        int bRows = mat2.length;
        int bColumns = mat2[0].length;
        
        if (aColumns != bRows) {
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }
        
        double[][] mat3 = new double[aRows][bColumns];
        
        for (int i = 0; i < aRows; i++) { // aRow
            for (int j = 0; j < bColumns; j++) { // bColumn
                for (int k = 0; k < aColumns; k++) { // aColumn
                    mat3[i][j] += mat1[i][k] * mat2[k][j];
                }
            }
        }
        
        return mat3;
    }
	
	/**
	 * Transpose a two-dimensional array.
	 * 
	 * @param mat	Two-dimensional array that should be transposed.
	 * @return		Transposed two-dimensional array.
	 */
	public static double[][] transpose(double[][] mat) {
	    int m = mat.length;
	    int n = mat[0].length;

	    double[][] t = new double[n][m];

	    for(int i = 0; i < n; i++) {
	    	for(int j = 0; j < m; j++) {
	            t[i][j] = mat[j][i];
	        }
	    }
	    return t;
	}

	/**
	 * Add two two-dimensional arrays.
	 * 
	 * @param mat1	Two-dimensional array with the first input matrix.
	 * @param mat2	Two-dimensional array with the second input matrix.
	 * @return		Two-dimensional array with the output matrix.
	 */
	public static double[][] add(double[][] mat1, double[][] mat2) {
        int aRows = mat1.length;
        int aColumns = mat1[0].length;
        int bRows = mat2.length;
        int bColumns = mat2[0].length;

        if (aRows != bRows) {
            throw new IllegalArgumentException("Matrix dimensions do not match: " + aRows + " vs. " + bRows + " rows.");
        }
        if (aColumns != bColumns) {
            throw new IllegalArgumentException("Matrix dimensions do not match: " + aColumns + " vs. " + bColumns + " columns.");
        }
        
        double[][] mat3 = new double[aRows][aColumns];
        
        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < aColumns; j++) {
                mat3[i][j] = mat1[i][j] + mat2[i][j];
            }
        }
        
        return mat3;
    }
	
	/**
	 * This function accepts a list of statements that should be included in the network export, 
	 * retrieves their actual contents from the database, and creates and returns a two-mode 
	 * network matrix (= an affiliation matrix) or network based on the statement data. 
	 * 
	 * @param statements	An array list of SidebarStatement objects that should be included in the export.
	 * @param variable1		The name of the first variable (for the row labels).
	 * @param variable2		The name of the second variable (for the column labels).
	 * @param qualifier		The name of the agreement variable that determines whether an edge should be established.
	 * @return				A Network object with an affiliation network.
	 */
	public Network affiliation(ArrayList<Statement> statements, String variable1, String variable2, String qualifier) {
		ArrayList<String> names1 = new ArrayList<String>(); // unique row labels
		ArrayList<String> names2 = new ArrayList<String>(); // unique column labels
		ArrayList<String> entries1 = new ArrayList<String>(); // all variable 1 entries
		ArrayList<String> entries2 = new ArrayList<String>(); // all variable 2 entries
		ArrayList<Integer> qualifierValues = new ArrayList<Integer>(); // unique agreement qualifier values
		for (int i = 0; i < statements.size(); i++) { // retrieve the data for variables 1 and 2 from database
			int statementId = statements.get(i).getId();
			String name1 = (String) Dna.data.getStatement(statementId).getValues().get(variable1);
			if (name1 != null && !name1.equals("")) {
				entries1.add(name1);
				if (!names1.contains(name1)) {
					names1.add(name1);
				}
			}
			String name2 = (String) Dna.data.getStatement(statementId).getValues().get(variable2);
			if (name2 != null && !name2.equals("")) {
				entries2.add(name2);
				if (!names2.contains(name2)) {
					names2.add(name2);
				}
			}
			int qual = (int) Dna.data.getStatement(statementId).getValues().get(qualifier);
			if (!qualifierValues.contains(qual)) {
				qualifierValues.add(qual);
			}
		}
		double[][] mat = new double[names1.size()][names2.size()]; // the resulting affiliation matrix; 0 by default
		Edgelist edgelist = new Edgelist();
		for (int i = 0; i < entries1.size(); i++) {
			int row = -1;
			for (int j = 0; j < names1.size(); j++) {
				if (entries1.get(i).equals(names1.get(j))) {
					row = j;
				}
			}
			int col = -1;
			for (int j = 0; j < names2.size(); j++) {
				if (entries2.get(i).equals(names2.get(j))) {
					col = j;
				}
			}
			if (row != -1 && col != -1) {
				mat[row][col] = mat[row][col] + 1; // populate matrix
				edgelist.addEdge(new Edge(entries1.get(i), entries2.get(i), 1)); // populate edgelist
			}
		}
		String[] rownames = new String[names1.size()]; // cast row names from array list to array
		rownames = names1.toArray(rownames);
		String[] colnames = new String[names2.size()]; // cast column names from array list to array
		colnames = names2.toArray(colnames);
		Matrix matrix = new Matrix(mat, rownames, colnames); // assemble the Matrix object with labels
		Network network = new Network(matrix, edgelist, 2);  // wrap matrix and edgelist in a network object
		return(network);
		// TODO: should agreement = 0 result in negative edge weights?
	}
	
	/**
	 * This function accepts a list of statements that should be included in the network export, 
	 * retrieves their actual contents from the database, and creates and returns a one-mode 
	 * network matrix (co-occurrence/congruence or conflict matrix) based on the statement data.
	 * 
	 * @param statements	An array list of SidebarStatement objects that should be included in the export.
	 * @param variable1		The name of the variable for which the new matrix should be created (e.g., actors).
	 * @param variable2		The name of the variable via which the new matrix should be aggregated (e.g., concepts).
	 * @param qualifier		The name of the agreement variable via which an edge should be established.
	 * @param type			A string with with type of one-mode matrix to be created. Can have values "congruence" or "conflict").
	 * @return				A network object with a one-mode network.
	 */
	public Network oneModeNetwork(ArrayList<Statement> statements, String variable1, String variable2, String qualifier, String type) {
		ArrayList<String> names1 = new ArrayList<String>(); // unique row labels
		ArrayList<String> names2 = new ArrayList<String>(); // unique column labels
		ArrayList<Integer> qualifierValues = new ArrayList<Integer>(); // unique agreement qualifier values
		for (int i = 0; i < statements.size(); i++) { // retrieve the row and column names from database
			int statementId = statements.get(i).getId();
			//String name1 = Dna.dna.db.getVariableStringEntry(statementId, variable1);
			String name1 = (String) Dna.data.getStatement(statementId).getValues().get(variable1);
			if (name1 != null && !name1.equals("")) {
				if (!names1.contains(name1)) {
					names1.add(name1);
				}
			}
			//String name2 = Dna.dna.db.getVariableStringEntry(statementId, variable2);
			String name2 = (String) Dna.data.getStatement(statementId).getValues().get(variable2);
			if (name2 != null && !name2.equals("")) {
				if (!names2.contains(name2)) {
					names2.add(name2);
				}
			}
			//int qual = Dna.dna.db.getVariableIntEntry(statementId, qualifier);
			int qual = (int) Dna.data.getStatement(statementId).getValues().get(qualifier);
			if (!qualifierValues.contains(qual)) {
				qualifierValues.add(qual);
			}
		}
		double[][] cooc = new double[names1.size()][names1.size()];
		for (int i = 0; i < qualifierValues.size(); i++) { // compute one-mode projections for each agreement level, then add up
			double[][] mat = affiliation(statements, variable1, variable2, qualifier).getMatrix().getMatrix();
			mat = multiply(mat, transpose(mat));
			cooc = add(cooc, mat);
		}
		for (int i = 0; i < names1.size(); i++) {
			cooc[i][i] = 0;
		}
		String[] labels = new String[names1.size()]; // cast row names from array list to array
		labels = names1.toArray(labels);
		Matrix matrix = new Matrix(cooc, labels, labels);
		Network network = new Network(matrix, 1);
		return network;
		// TODO: take into account the type variable inside this function to create conflict networks
		// TODO: allow for concept congruence networks; i.e., multiply numbers of nodes by qualifier selection size
		// TODO: add normalization options (divide, subtract, Jaccard, cosine similarity)
		// TODO: consider not only matching agreement levels, but using distance measure, e.g., intensities 2 and 5 have a distance of 3.
	}
	
	/**
	 * This function accepts a list of statements that should be included in the relational event export, 
	 * and it exports the variables of all statements to a CSV file, along with the statement ID and a 
	 * date/time stamp. There is one statement per row, and the number of columns is the number of variables 
	 * present in the statement type.
	 * 
	 * @param statements	An array list of SidebarStatement objects (of the same statement type) that should be exported.
	 * @param fileName		String with the file name of the CSV file to which the event list will be exported.
	 */
	public void releventCSV(ArrayList<Statement> statements, String fileName) {
		String key, value;
		int statementId;
		Date d;
		SimpleDateFormat dateFormat;
		int statementTypeId = statements.get(0).getStatementTypeId();
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getId() != statementTypeId) {
				throw new IllegalArgumentException("More than one statement type was selected. Cannot export to a spreadsheet!");
			}
		}
		//HashMap<String, String> variables = Dna.dna.db.getVariables(statementType);
		HashMap<String, String> variables = Dna.data.getStatementTypeById(statementTypeId).getVariables();
		Iterator<String> keyIterator;
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF8"));
			keyIterator = variables.keySet().iterator();
			out.write("\"ID\";\"time\"");
			while (keyIterator.hasNext()){
				out.write(";\"" + keyIterator.next() + "\"");
			}
			for (int i = 0; i < statements.size(); i++) {
				out.newLine();
				statementId = statements.get(i).getId();
				String stringId = new Integer(statementId).toString();
				out.write(stringId);
				d = statements.get(i).getDate();
				dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				out.write(";" + dateFormat.format(d));
				keyIterator = variables.keySet().iterator();
				while (keyIterator.hasNext()){
					key = keyIterator.next();
					value = variables.get(key);
					if (value.equals("short text") || value.equals("long text")) {
						//out.write(";\"" + Dna.dna.db.getVariableStringEntry(statementId, key).replaceAll(";", ",") + "\"");
						out.write(";\"" + ((String) Dna.data.getStatement(statementId).getValues().get(key)).replaceAll(";", ",") + "\"");
					} else if (value.equals("boolean") || value.equals("integer")) {
						//out.write(";" + Dna.dna.db.getVariableIntEntry(statementId, key));
						out.write(";" + Dna.data.getStatement(statementId).getValues().get(key));
					}
				}
			}
			out.close();
			System.out.println("File has been exported to \"" + fileName + "\".");
		} catch (IOException e) {
			System.err.println("Error while saving CSV file: " + e);
		}
	}
}
