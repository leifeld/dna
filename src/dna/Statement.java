package dna;


import java.util.ArrayList;

public class Statement {
	int id, coder, start, stop, statementTypeId;
	ArrayList<Value> values;
	
	public Statement(int id, int coder, int start, int stop, int statementTypeId, ArrayList<Value> values) {
		this.id = id;
		this.coder = coder;
		this.start = start;
		this.stop = stop;
		this.statementTypeId = statementTypeId;
		this.values = values;
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