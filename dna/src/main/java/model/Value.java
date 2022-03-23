package model;


/**
 * Represents values saved in a statement, corresponding to the variables saved
 * in the statement type that corresponds to the statement. 
 */
public class Value {
	private int variableId;
	private String key, dataType;
	private Object value;
	
	/**
	 * Creates a value.
	 * 
	 * @param variableId  The ID of the variable of which the value is an
	 *   instance.
	 * @param key         The variable name.
	 * @param dataType    The data type of the value. Can be {@code
	 *   "short text"}, {@code "long text"}, {@code "integer"}, or {@code
	 *   "boolean"}.
	 * @param value       The actual value saved for the variable. The value is
	 *   saved as an Object and must be cast into String or Integer, depending
	 *   on the data type.
	 */
	public Value(int variableId, String key, String dataType, Object value) {
		this.variableId = variableId;
		this.key = key;
		this.dataType = dataType;
		this.value = value;
	}

	/**
	 * Creates a value, but initially an empty one.
	 * 
	 * @param variableId  The ID of the variable of which the value is an
	 *   instance.
	 * @param key         The variable name.
	 * @param dataType    The data type of the value. Can be {@code
	 *   "short text"}, {@code "long text"}, {@code "integer"}, or {@code
	 *   "boolean"}.
	 */
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
	
	/**
	 * Copy constructor to create a deep clone of a value object.
	 * 
	 * @param value A value.
	 */
	public Value(Value value) {
		this.variableId = value.getVariableId();
		this.key = value.getKey();
		this.dataType = value.getDataType();
		this.value = value.getValue();
	}

	/**
	 * Get the variable ID.
	 * 
	 * @return The variable ID.
	 */
	public int getVariableId() {
		return variableId;
	}

	/**
	 * Get the variable name.
	 * 
	 * @return The variable name.
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Set the variable key (= name).
	 * 
	 * @param key The variable key or name.
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * Get the data type.
	 * 
	 * @return The data type of the variable. Can be one of {@code
	 *   "short text"}, {@code "long text"}, {@code "integer"}, or {@code
	 *   "boolean"}.
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * Set the variable data type.
	 * 
	 * @param dataType The variable data type as a String.
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	/**
	 * Get the value as an object (needs to be cast into String or Integer).
	 * 
	 * @return The value (as an Object that needs to be cast).
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Set the actual value of the value.
	 * 
	 * @param value The value to set.
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * Is this value equal to another value?
	 * 
	 * @param v A value for comparison.
	 * @return  A boolean indicator of whether the other value is identical.
	 */
	public boolean equals(Value v) {
		if (this.variableId == v.getVariableId() &&
				this.getKey().equals(v.getKey()) &&
				((this.getValue() == null && v.getValue() == null) || this.getValue().equals(v.getValue())) &&
				this.getDataType().equals(v.getDataType())) {
			return true;
		} else {
			return false;
		}
	}
}