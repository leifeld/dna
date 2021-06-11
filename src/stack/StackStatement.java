package stack;

import java.util.ArrayList;

public class StackStatement {
	int id, coder, start, stop, statementTypeId;
	ArrayList<StackValue> values;
	
	public StackStatement(int id, int coder, int start, int stop, int statementTypeId, ArrayList<StackValue> values) {
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
	public ArrayList<StackValue> getValues() {
		return values;
	}
	void setValues(ArrayList<StackValue> values) {
		this.values = values;
	}
}