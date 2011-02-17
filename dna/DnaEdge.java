package dna;

public class DnaEdge {
	
	String id;
    double weight;
    DnaVertex source;
    DnaVertex target;
    
    public DnaEdge(String id, double weight, DnaVertex source, DnaVertex target) {
        this.weight = weight;
        this.id = id;
        this.source = source;
        this.target = target;
    }
    
    public String getId() {
        return id;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public DnaVertex getSource() {
        return source;
    }
    
    public DnaVertex getTarget() {
        return target;
    }
    
    public void setSource(DnaVertex dbv) {
        this.source = dbv;
    }
    
    public void setTarget(DnaVertex dbv) {
        this.target = dbv;
    }
    
    public void setWeight(double w) {
        this.weight = w;
    }
    
    public void setId(String ident) {
        this.id = ident;
    }
	
    public void addToWeight(double w) {
        this.weight = this.weight + w;
    }
}
