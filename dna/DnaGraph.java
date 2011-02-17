package dna;

import java.util.*;

public class DnaGraph {
	
	//Declarations
    ArrayList<DnaGraphVertex> v = new ArrayList<DnaGraphVertex>();
    ArrayList<DnaGraphEdge> e = new ArrayList<DnaGraphEdge>();
    
    public void addVertex(DnaGraphVertex vertex) {
        int i;
        int condition = 0;
        for (i = 0; i < v.size(); i = i + 1) {
            if (v.get(i).getId() == vertex.getId()) {
                condition = 1;
                System.out.println("A vertex with the ID " + v.get(i).getId() 
                    + " is already attached to this graph.");
            }
        }
        if (condition == 0) {
            v.add(vertex);
        } else {
            System.out.println("The vertex has not been added to the graph.");
        }
    }
	
    public void addEdge(DnaGraphEdge edge) {
        int i;
        int condition = 0;
        for (i = 0; i < e.size(); i = i + 1) {
            if (e.get(i).getId() == edge.getId()) {
                condition = 1;
                System.out.println("An edge with the ID " + e.get(i).getId() 
                    + " is already attached to this graph.");
            }
        }
        
        if (condition == 0) {
            e.add(edge);
        } else {
            System.out.println("The edge has not been added to the graph.");
        }
    }
    
    public void removeVertex(DnaGraphVertex vertex) {
        v.remove(vertex);
    }
    
    public void removeEdge(DnaGraphEdge edge) {
        e.remove(edge);
    }
    
    public void removeAllEdges() {
        e.clear();
    }
    
    public void removeAllVertices() {
        v.clear();
    }
    
    public int countVertices() {
        return v.size();
    }
    
    public int countEdges() {
        return e.size();
    }
    
    public int countVertexType(String type) {
    	int count = 0;
        for (int i = 0; i < v.size(); i = i + 1) {
            if (v.get(i).getType().equals(type)) {
                count++;
            }
        }
        return count;
    }
    
    public DnaGraphVertex getVertex(int id) {
        DnaGraphVertex vertex = null;
        for (int i = 0; i < v.size(); i++) {
            if (v.get(i).getId() == id) {
                vertex = v.get(i);
            }
        }
        return vertex;
    }
    
    public DnaGraphVertex getVertex(String label) {
        DnaGraphVertex vertex = null;
        for (int i = 0; i < v.size(); i++) {
            if (v.get(i).getLabel().equals(label)) {
                vertex = v.get(i);
            }
        }
        return vertex;
    }
    
    public DnaGraphEdge getEdge(int id) {
        DnaGraphEdge edge = null;
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i).getId() == id) {
                edge = e.get(i);
            }
        }
        return edge;
    }
    
    public DnaGraphEdge getEdge(int sourceId, int targetId) {
        DnaGraphEdge edge = null;
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i).getSource().getId() == sourceId
                && e.get(i).getTarget().getId() == targetId) {
                edge = e.get(i);
            }
        }
        return edge;
    }
    
    public boolean containsEdge(int id) {
        int condition = 0;
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i).getId() == id) {
                condition = 1;
            }
        }
        if (condition == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean containsEdge(int sourceId, int targetId) {
        int condition = 0;
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i).getSource().getId() == sourceId
                && e.get(i).getTarget().getId() == targetId) {
                condition = 1;
            }
        }
        if (condition == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean containsVertex(int id) {
        int condition = 0;
        for (int i = 0; i < v.size(); i++) {
            if (v.get(i).getId() == id) {
                condition = 1;
            }
        }
        if (condition == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean vertexIsIsolate(DnaGraphVertex vertex) {
        Boolean iso = true;
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i).getSource().equals(vertex) || e.get(i).getTarget().equals(vertex)) {
                iso = false;
            }
        }
        return iso;
    }
    
    public Double getMedianWeight() {
    	double m = Math.floor(e.size()/2);
    	Double mw = e.get((int)m).getWeight();
    	return mw;
    }
    
    public Double getMeanWeight() {
    	double a = 0.0;
    	for (int i = 0; i < e.size(); i++) {
    		double b = e.get(i).getWeight();
    		double c = b * 10000;
    		double d = Math.round(c);
    		double e = d / 10000;
    		a = a + e;
    	}
    	double f = a / e.size();
    	return f;
    }
    
    public Double getMinimumWeight() {
    	ArrayList<Double> ew = new ArrayList<Double>();
    	for (int i = 0; i < e.size(); i++) {
    		ew.add(e.get(i).getWeight());
    	}
    	Collections.sort(ew);
    	return ew.get(0);
    }
    
    public Double getMaximumWeight() {
    	ArrayList<Double> ew = new ArrayList<Double>();
    	for (int i = 0; i < e.size(); i++) {
    		ew.add(e.get(i).getWeight());
    	}
    	Collections.sort(ew);
    	return ew.get(ew.size()-1);
    }
}