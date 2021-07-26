package dna;

import java.awt.Color;
import java.util.ArrayList;

public class StatementType {
	int id;
	String label;
	Color color;
	ArrayList<Value> variables;

	public StatementType(int id, String label, Color color, ArrayList<Value> variables) {
		this.id = id;
		this.label = label;
		this.color = color;
		this.variables = variables;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public ArrayList<Value> getVariables() {
		return variables;
	}

	public void setVariables(ArrayList<Value> variables) {
		this.variables = variables;
	}
}