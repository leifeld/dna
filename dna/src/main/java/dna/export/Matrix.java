package dna.export;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * A class for Matrix objects. As two-dimensional arrays do not store the row
 * and column labels, this class stores both the two-dimensional array and its
 * labels. Matrix objects are created by the different network algorithms. Some
 * of the file export functions take Matrix objects as input data. A date slot
 * holds an optional date label (useful when the time window method is used.
 */
public class Matrix implements Cloneable {
	private double[][] matrix;
	private String[] rowNames, columnNames;
	private boolean integer;
	private LocalDateTime dateTime;
	private LocalDateTime start;
	private LocalDateTime stop;
	private int numStatements;
	
	public Matrix(double[][] matrix, String[] rowNames, String[] columnNames, boolean integer, LocalDateTime start, LocalDateTime stop) {
		this.matrix = matrix;
		this.rowNames = rowNames;
		this.columnNames = columnNames;
		this.integer = integer;
		this.start = start;
		this.stop = stop;
	}

	/**
	 * Constructor for empty object with only the dates inserted (useful for time slice creation).
	 * @param start The start date.
	 * @param dateTime The mid-point.
	 * @param stop The end date.
	 */
	public Matrix(String[] rowNames, String[] columnNames, boolean integer,  LocalDateTime start, LocalDateTime dateTime, LocalDateTime stop) {
		this.rowNames = rowNames;
		this.columnNames = columnNames;
		this.integer = integer;
		this.start = start;
		this.dateTime = dateTime;
		this.stop = stop;
	}

	/**
	 * Copy constructor.
	 */
	public Matrix(Matrix matrix) {
		this.matrix = matrix.getMatrix();
		this.rowNames = matrix.getRowNames();
		this.columnNames = matrix.getColumnNames();
		this.integer = matrix.getInteger();
		this.start = matrix.getStart();
		this.stop = matrix.getStop();
		this.dateTime = matrix.getDateTime();
		this.numStatements = matrix.getNumStatements();
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
	 * @return the row names
	 */
	public String[] getRowNames() {
		return rowNames;
	}

	/**
	 * @param rowNames the row names to set
	 */
	public void setRowNames(String[] rowNames) {
		this.rowNames = rowNames;
	}

	/**
	 * @return the column names
	 */
	public String[] getColumnNames() {
		return columnNames;
	}

	/**
	 * @param columnNames the column names to set
	 */
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
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
	public LocalDateTime getDateTime() {
		return dateTime;
	}

	/**
	 * @return Date and time in milliseconds since 1 January 1970
	 */
	public long getDateTimeLong() {
		return dateTime.toEpochSecond(ZoneOffset.UTC);
	}

	/**
	 * @param dateTime the date and time to set
	 */
	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	/**
	 * @return the start
	 */
	public LocalDateTime getStart() {
		return start;
	}

	/**
	 * @return Start date/time in milliseconds since 1 January 1970
	 */
	public long getStartLong() {
		return start.toEpochSecond(ZoneOffset.UTC);
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
	 * @return Stop date/time in milliseconds since 1 January 1970
	 */
	public long getStopLong() {
		return stop.toEpochSecond(ZoneOffset.UTC);
	}

	/**
	 * @param stop the stop to set
	 */
	public void setStop(LocalDateTime stop) {
		this.stop = stop;
	}
}