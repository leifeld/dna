package dna.export;

import java.util.ArrayList;

/**
 * @author Philip Leifeld
 * 
 * A class for Edgelist objects. An edge list is a list of Edge objects and is an alternative to 
 * Matrix objects for storing network data. If an edge is added that is already part of the 
 * edge list, its weights is increased instead of adding a duplicate edge.
 */
class Edgelist {
	ArrayList<Edge> edgelist;

	public Edgelist(ArrayList<Edge> edgelist) {
		this.edgelist = edgelist;
	}
	
	public Edgelist() {
		this.edgelist = new ArrayList<Edge>();
	}
	
	public void addEdge(Edge edge) {
		int id = -1;
		for (int i = 0; i < edgelist.size(); i++) {
			if (edgelist.get(i).getSource().equals(edge.getSource()) && edgelist.get(i).getTarget().equals(edge.getTarget())) {
				id = i;
			}
		}
		if (id == -1) {
			edgelist.add(edge);
		} else {
			edgelist.get(id).setWeight(edge.getWeight());
		}
	}
	
	/**
	 * @return unique String array of all source node names in the edge list
	 */
	public String[] getSources() {
		ArrayList<String> sources = new ArrayList<String>();
		String currentSource;
		for (int i = 0; i < edgelist.size(); i++) {
			currentSource = edgelist.get(i).getSource();
			if (!sources.contains(currentSource)) {
				sources.add(currentSource);
			}
		}
		String[] s = new String[sources.size()]; // cast row names from array list to array
		s = sources.toArray(s);
		return s;
	}

	/**
	 * @return unique String array of all target node names in the edge list
	 */
	public String[] getTargets() {
		ArrayList<String> targets = new ArrayList<String>();
		String currentTarget;
		for (int i = 0; i < edgelist.size(); i++) {
			currentTarget = edgelist.get(i).getTarget();
			if (!targets.contains(currentTarget)) {
				targets.add(currentTarget);
			}
		}
		String[] t = new String[targets.size()]; // cast row names from array list to array
		t = targets.toArray(t);
		return t;
	}

	/**
	 * @return the edgelist
	 */
	public ArrayList<Edge> getEdgelist() {
		return edgelist;
	}

	/**
	 * @param edgelist the edgelist to set
	 */
	public void setEdgelist(ArrayList<Edge> edgelist) {
		this.edgelist = edgelist;
	}
}