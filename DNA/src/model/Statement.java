package model;


import java.awt.Color;
import java.util.ArrayList;

public class Statement {
	int id, coder, start, stop, statementTypeId;
	ArrayList<Value> values;
	Color statementTypeColor, coderColor;
	String statementTypeLabel;
	
	public Statement(int id, int coder, int start, int stop, int statementTypeId, ArrayList<Value> values) {
		this.id = id;
		this.coder = coder;
		this.start = start;
		this.stop = stop;
		this.statementTypeId = statementTypeId;
		this.values = values;
	}
	
	public Statement(int id, int coder, int start, int stop, int statementTypeId, ArrayList<Value> values, Color statementTypeColor, Color coderColor, String statementTypeLabel) {
		this.id = id;
		this.coder = coder;
		this.start = start;
		this.stop = stop;
		this.statementTypeId = statementTypeId;
		this.values = values;
		this.statementTypeColor = statementTypeColor;
		this.coderColor = coderColor;
		this.statementTypeLabel = statementTypeLabel;
	}
	
	public String getStatementTypeLabel() {
		return statementTypeLabel;
	}
	
	public void setStatementTypeLabel(String statementTypeLabel) {
		this.statementTypeLabel = statementTypeLabel;
	}
	
	public Color getStatementTypeColor() {
		return statementTypeColor;
	}
	
	public void setStatementTypeColor(Color statementTypeColor) {
		this.statementTypeColor = statementTypeColor;
	}
	
	public Color getCoderColor() {
		return coderColor;
	}
	
	public void setCoderColor(Color coderColor) {
		this.coderColor = coderColor;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getCoder() {
		return coder;
	}
	
	void setCoder(int coder) {
		this.coder = coder;
	}
	
	public int getStart() {
		return start;
	}
	
	void setStart(int start) {
		this.start = start;
	}
	
	public int getStop() {
		return stop;
	}
	
	void setStop(int stop) {
		this.stop = stop;
	}
	
	public int getStatementTypeId() {
		return statementTypeId;
	}
	
	void setStatementTypeId(int statementTypeId) {
		this.statementTypeId = statementTypeId;
	}
	
	public ArrayList<Value> getValues() {
		return values;
	}
	
	void setValues(ArrayList<Value> values) {
		this.values = values;
	}
}