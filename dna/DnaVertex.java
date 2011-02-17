package dna;

public class DnaVertex {
	
	String label;
    String id;
    boolean class1;
    int numericalId;
    
    public DnaVertex(String id, String label) {
        this.label = label;
        this.id = id;
    }
    
    public DnaVertex(String id, String label, boolean class1) {
        this.label = label;
        this.id = id;
        this.class1 = class1;
    }
    
    public DnaVertex(String id, String label, int numid) {
        this.label = label;
        this.id = id;
        this.numericalId = numid;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getId() {
        return id;
    }
    
    public void setLabel(String lab) {
        this.label = lab;
    }
    
    public void setId(String ident) {
        this.id = ident;
    }
    
    public void setClass1(boolean vc) {
        this.class1 = vc;
    }
    
    public boolean getClass1() {
        return class1;
    }
    
    public int getNumericalId() {
        return numericalId;
    }
    
    public void setId(int numid) {
        this.numericalId = numid;
    }
}
