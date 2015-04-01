package dna;

import java.util.ArrayList;
import java.util.List;

public class DNAToken {
	
	//Add features array, and label
	private String text;
	private int start_position;
	private int end_position;
	private String label;
	private List<Double> features;
	private int id;
	private int docId;
	
	public DNAToken() {
		features = new ArrayList<Double>();
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getStart_position() {
		return start_position;
	}
	public void setStart_position(int start_position) {
		this.start_position = start_position;
	}
	public int getEnd_position() {
		return end_position;
	}
	public void setEnd_position(int end_position) {
		this.end_position = end_position;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<Double> getFeatures() {
		return features;
	}

	public void setFeatures(List<Double> features) {
		this.features = features;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}
	
	

}
