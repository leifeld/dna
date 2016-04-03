package dna.export;

/**
 * @author Philip Leifeld
 *
 * A class for Matrix objects. As two-dimensional arrays do not store the row and column labels, 
 * this class stores both the two-dimensional array and its labels. Matrix objects are created 
 * by the different network algorithms. Some of the file export functions take Matrix objects as 
 * input data.
 *
 */
public class Matrix {
	double[][] matrix;
	String[] rownames, colnames;
	
	public Matrix(double[][] matrix, String[] rownames, String[] colnames) {
		this.matrix = matrix;
		this.rownames = rownames;
		this.colnames = colnames;
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
}
