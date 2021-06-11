package stack;

public class StackValue {
	String key, dataType;
	Object value;
	
	public StackValue(String key, String dataType, Object value) {
		this.key = key;
		this.dataType = dataType;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	
	void setKey(String key) {
		this.key = key;
	}
	
	public String getDataType() {
		return dataType;
	}
	
	void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public Object getValue() {
		return value;
	}
	
	void setValue(Object value) {
		this.value = value;
	}
}