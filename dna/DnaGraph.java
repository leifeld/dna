package dna;

import java.util.ArrayList;

public class DnaGraph {
	
	//Declarations
    ArrayList<DnaVertex> v = new ArrayList<DnaVertex>();
    ArrayList<DnaEdge> e = new ArrayList<DnaEdge>();
    
    public void addVertex(DnaVertex vertex) {
        int i;
        int condition = 0;
        for (i = 0; i < v.size(); i = i + 1) {
            if (v.get(i).getId().equals(vertex.getId())) {
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
	
    public void addEdge(DnaEdge edge) {
        int i;
        int condition = 0;
        for (i = 0; i < e.size(); i = i + 1) {
            if (e.get(i).getId().equals(edge.getId())) {
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
    
    public void removeVertex(DnaVertex vertex) {
        v.remove(vertex);
    }
    
    public void removeEdge(DnaEdge edge) {
        e.remove(edge);
    }
    
    public void removeAllEdges() {
        e.clear();
    }
    
    public void removeAllVertices() {
        v.clear();
    }
    
    public int numberOfVertices() {
        return v.size();
    }
    
    public int numberOfEdges() {
        return e.size();
    }
    
    public int numberOfClass0() {
        int count = 0;
        for (int i = 0; i < v.size(); i = i + 1) {
            if (v.get(i).getClass1() == false) {
                count = count + 1;
            }
        }
        return count;
    }
    
    public int numberOfClass1() {
        int count = 0;
        for (int i = 0; i < v.size(); i = i + 1) {
            if (v.get(i).getClass1() == true) {
                count = count + 1;
            }
        }
        return count;
    }
    
    public DnaVertex getVertex(String id) {
        DnaVertex vertex = null;
        for (int i = 0; i < v.size(); i++) {
            if (v.get(i).getId().equals(id)) {
                vertex = v.get(i);
            }
        }
        return vertex;
    }
    
    public DnaEdge getEdge(String id) {
        DnaEdge edge = null;
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i).getId().equals(id)) {
                edge = e.get(i);
            }
        }
        return edge;
    }
    
    public DnaEdge getEdge(String sourceId, String targetId) {
        DnaEdge edge = null;
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i).getSource().getId().equals(sourceId)
                && e.get(i).getTarget().getId().equals(targetId)) {
                edge = e.get(i);
            }
        }
        return edge;
    }
    
    public boolean containsEdge(String id) {
        int condition = 0;
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i).getId().equals(id)) {
                condition = 1;
            }
        }
        if (condition == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean containsEdge(String sourceId, String targetId) {
        int condition = 0;
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i).getSource().getId().equals(sourceId)
                && e.get(i).getTarget().getId().equals(targetId)) {
                condition = 1;
            }
        }
        if (condition == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean containsVertex(String id) {
        int condition = 0;
        for (int i = 0; i < v.size(); i++) {
            if (v.get(i).getId().equals(id)) {
                condition = 1;
            }
        }
        if (condition == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean vertexIsIsolate(DnaVertex vertex) {
        Boolean iso = true;
        for (int i = 0; i < e.size(); i++) {
            if (e.get(i).getSource().equals(vertex) || e.get(i).getTarget().equals(vertex)) {
                iso = false;
            }
        }
        return iso;
    }
}
