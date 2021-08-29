package model;


public class Value {
	int variableId;
	String key, dataType;
	Object value;
	
	public Value(int variableId, String key, String dataType, Object value) {
		this.variableId = variableId;
		this.key = key;
		this.dataType = dataType;
		this.value = value;
	}
	
	public Value(int variableId, String key, String dataType) {
		this.variableId = variableId;
		this.key = key;
		this.dataType = dataType;
		if (dataType.equals("short text")) {
			this.value = null;
		} else if (dataType.equals("long text")) {
			this.value = "";
		} else if (dataType.equals("integer")) {
			this.value = 0;
		} else if (dataType.equals("boolean")) {
			this.value = 1;
		}
	}

	public int getVariableId() {
		return variableId;
	}

	public void setVariableId(int variableId) {
		this.variableId = variableId;
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getDataType() {
		return dataType;
	}
	
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
}