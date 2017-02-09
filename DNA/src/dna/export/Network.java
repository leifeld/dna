package dna.export;

import java.util.ArrayList;

/**
 * @author Philip Leifeld
 * 
 * A class for Network objects. A Network object is merely a container for {@link Matrix} objects and/or 
 * {@link Edgelist} objects. This container class is necessary because the export functions should be able 
 * to return either matrices or edgelists; but since only one data type can be returned by functions, 
 * this is going to be a Network object that contains either the matrix or the edge list or both.
 * 
 */
class Network {
	Matrix matrix;
	Edgelist edgelist;
	int modes;
	
	// constructor when only the matrix has been computed: also convert to edge list
	public Network(Matrix matrix, int modes) {
		this.matrix = matrix;
		this.modes = modes;
		double[][] m = matrix.getMatrix();
		String[] r = matrix.getRownames();
		String[] c = matrix.getColnames();
		ArrayList<Edge> el = new ArrayList<Edge>();
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++) {
				if (i != j && m[i][j] != 0) {
					el.add(new Edge(r[i], c[j], m[i][j]));
				}
			}
		}
		this.edgelist = new Edgelist(el);
	}
	
	// constructor when only the edge list has been computed: also convert to matrix
	public Network(Edgelist edgelist, int modes) {
		this.edgelist = edgelist;
		this.modes = modes;
		String[] sources = edgelist.getSources();
		String[] targets = edgelist.getTargets();
		double[][] mat = new double[sources.length][targets.length];
		int row = -1;
		int col = -1;
		ArrayList<Edge> el = edgelist.getEdgelist();
		for (int i = 0; i < el.size(); i++) {
			for (int j = 0; j < sources.length; j++) {
				if (el.get(i).getSource().equals(sources[j])) {
					row = j;
				}
			}
			for (int j = 0; j < targets.length; j++) {
				if (el.get(i).getTarget().equals(targets[j])) {
					col = j;
				}
			}
			mat[row][col] = el.get(i).getWeight();
		}
		this.matrix = new Matrix(mat, sources, targets, false);
	}
	
	// constructor when both matrix and edge list are present
	public Network(Matrix matrix, Edgelist edgelist, int modes) {
		this.matrix = matrix;
		this.edgelist = edgelist;
		this.modes = modes;
	}

	/**
	 * @return the matrix
	 */
	public Matrix getMatrix() {
		return matrix;
	}

	/**
	 * @param matrix the matrix to set
	 */
	public void setMatrix(Matrix matrix) {
		this.matrix = matrix;
	}

	/**
	 * @return the edgelist
	 */
	public Edgelist getEdgelist() {
		return edgelist;
	}

	/**
	 * @param edgelist the edgelist to set
	 */
	public void setEdgelist(Edgelist edgelist) {
		this.edgelist = edgelist;
	}

	/**
	 * @return the number of node classes
	 */
	public int getModes() {
		return modes;
	}

	/**
	 * @param modes the number of node classes to set
	 */
	public void setModes(int modes) {
		this.modes = modes;
	}
}