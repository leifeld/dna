package dna.export;

import java.util.Date;

/**
 * @author Philip Leifeld
 *
 * A class for Matrix objects. As two-dimensional arrays do not store the row and column labels, 
 * this class stores both the two-dimensional array and its labels. Matrix objects are created 
 * by the different network algorithms. Some of the file export functions take Matrix objects as 
 * input data. A date slot holds an optional date label (useful when the time window method is 
 * used.
 *
 */
public class Matrix {
	double[][] matrix;
	String[] rownames, colnames;
	boolean integer;
	Date date;
	
	public Matrix(double[][] matrix, String[] rownames, String[] colnames, boolean integer) {
		this.matrix = matrix;
		this.rownames = rownames;
		this.colnames = colnames;
		this.integer = integer;
	}

	/**
	 * @return the matrix
	 */
	public double[][] getMatrix() {
		return matrix;
	}

	/**
	 * @param matrix the matrix to set
	 */
	public void setMatrix(double[][] matrix) {
		this.matrix = matrix;
	}

	/**
	 * @return the rownames
	 */
	public String[] getRownames() {
		return rownames;
	}

	/**
	 * @param rownames the rownames to set
	 */
	public void setRownames(String[] rownames) {
		this.rownames = rownames;
	}

	/**
	 * @return the colnames
	 */
	public String[] getColnames() {
		return colnames;
	}

	/**
	 * @param colnames the colnames to set
	 */
	public void setColnames(String[] colnames) {
		this.colnames = colnames;
	}

	/**
	 * @return a boolean value indicating whether the values can be cast to integer
	 */
	public boolean getInteger() {
		return integer;
	}

	/**
	 * @param integer   the integer boolean indicator to be set
	 */
	public void setInteger(boolean integer) {
		this.integer = integer;
	}

	/**
	 * @return a boolean value indicating whether the values can be cast to integer
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param integer   the integer boolean indicator to be set
	 */
	public void setDate(Date date) {
		this.date = date;
	}
}