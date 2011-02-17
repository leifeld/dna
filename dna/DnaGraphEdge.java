package dna;

import java.util.GregorianCalendar;

public class DnaGraphEdge {
	
	int id;
    double weight;
    DnaGraphVertex source;
    DnaGraphVertex target;
    GregorianCalendar date;
    String detail;
    String category;
    
    public DnaGraphEdge(int id, double weight, DnaGraphVertex source, DnaGraphVertex target) {
        this.weight = weight;
        this.id = id;
        this.source = source;
        this.target = target;
    }
    
    public DnaGraphEdge(int id, double weight, DnaGraphVertex source, DnaGraphVertex target, GregorianCalendar date, String category, String detail) {
        this.weight = weight;
        this.id = id;
        this.source = source;
        this.target = target;
        this.date = date;
        this.category = category;
        this.detail = detail;
    }
    
    public int getId() {
        return id;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public DnaGraphVertex getSource() {
        return source;
    }
    
    public DnaGraphVertex getTarget() {
        return target;
    }
    
    public void setSource(DnaGraphVertex dbv) {
        this.source = dbv;
    }
    
    public void setTarget(DnaGraphVertex dbv) {
        this.target = dbv;
    }
    
    public void setWeight(double w) {
        this.weight = w;
    }
    
    public void setId(int id) {
        this.id = id;
    }
	
    public void addToWeight(double w) {
        this.weight = this.weight + w;
    }
    
    public GregorianCalendar getDate() {
        return date;
    }
    
    public void setDate(GregorianCalendar date) {
        this.date = date;
    }
    
    public void setDetail(String detail) {
    	this.detail = detail;
    }
    
    public String getDetail() {
    	return detail;
    }
    
    public void setCategory(String category) {
    	this.category = category;
    }
    
    public String getCategory() {
    	return category;
    }
}