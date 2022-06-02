package model;

import java.time.LocalDateTime;

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
class Matrix implements Cloneable {
	private double[][] matrix;
	private String[] rownames, colnames;
	private boolean integer;
	private LocalDateTime date;
	private LocalDateTime start;
	private LocalDateTime stop;
	private int numStatements;
	
	public Matrix(double[][] matrix, String[] rownames, String[] colnames, boolean integer, LocalDateTime start, LocalDateTime stop) {
		this.matrix = matrix;
		this.rownames = rownames;
		this.colnames = colnames;
		this.integer = integer;
		this.start = start;
		this.stop = stop;
	}

	protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
	
	/**
	 * @return the numStatements
	 */
	public int getNumStatements() {
		return numStatements;
	}

	/**
	 * @param numStatements the numStatements to set
	 */
	public void setNumStatements(int numStatements) {
		this.numStatements = numStatements;
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
	public LocalDateTime getDate() {
		return date;
	}

	/**
	 * @param integer   the integer boolean indicator to be set
	 */
	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	/**
	 * @return the start
	 */
	public LocalDateTime getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	/**
	 * @return the stop
	 */
	public LocalDateTime getStop() {
		return stop;
	}

	/**
	 * @param stop the stop to set
	 */
	public void setStop(LocalDateTime stop) {
		this.stop = stop;
	}
}