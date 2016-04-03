package dna.export;

/**
 * @author Philip Leifeld
 * 
 * A class for Edge objects. An edge consists of a source node, a target node, and an edge weight. 
 * Some of the export functions take Edgelist objects as input data. This class represents the edges in 
 * such an edge list.
 * 
 */
public class Edge {
	String source;
	String target;
	double weight;
	
	public Edge(String source, String target, double weight) {
		this.source = source;
		this.target = target;
		this.weight = weight;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
}