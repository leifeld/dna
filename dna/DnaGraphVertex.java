package dna;

public class DnaGraphVertex {
	
	String label, type;
    int id;
    
    public DnaGraphVertex(int id, String label) {
        this.label = label;
        this.id = id;
    }
    
    public DnaGraphVertex(int id, String label, String type) {
        this.label = label;
        this.id = id;
        this.type = type;
    }
    
    public String getLabel() {
        return label;
    }
    
    public int getId() {
        return id;
    }
    
    public void setLabel(String lab) {
        this.label = lab;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setType(String type) {
    	this.type = type;
    }
    
    public String getType() {
    	return type;
    }
}
