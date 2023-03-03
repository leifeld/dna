package model;

/**
 * Represents values saved in a statement, corresponding to the variables saved
 * in the statement type that corresponds to the statement.
 */
public class Variable {
    private int id;
    private String variableName, dataType;
    private Object value;

    /**
     * Creates a value.
     *
     * @param id The ID of the variable of which the value is an instance.
     * @param variableName The variable name.
     * @param dataType The data type of the value. Can be {@code "short text"}, {@code "long text"}, {@code "integer"}, or {@code "boolean"}.
     */
    public Variable(int id, String variableName, String dataType, Object value) {
        this.id = id;
        this.variableName = variableName;
        this.dataType = dataType;
        this.value = value;
    }

    /**
     * Creates a value, but initially an empty one, without value.
     *
     * @param id The ID of the variable of which the value is an instance.
     * @param variableName The variable name.
     * @param dataType The data type of the value. Can be {@code "short text"}, {@code "long text"}, {@code "integer"}, or {@code "boolean"}.
     */
    public Variable(int id, String variableName, String dataType) {
        this.id = id;
        this.variableName = variableName;
        this.dataType = dataType;
    }

    /**
     * Copy constructor to create a deep clone of a variable object.
     *
     * @param variable A variable.
     */
    public Variable(Variable variable) {
        this.id = variable.getId();
        this.variableName = variable.getVariableName();
        this.dataType = variable.getDataType();
        this.value = variable.getValue();
    }

    /**
     * Get the variable ID.
     *
     * @return The variable ID.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get the variable name.
     *
     * @return The variable name.
     */
    public String getVariableName() {
        return this.variableName;
    }

    /**
     * Set the variable name.
     *
     * @param variableName The variable name.
     */
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    /**
     * Get the data type.
     *
     * @return The data type of the variable. Can be one of {@code "short text"}, {@code "long text"}, {@code "integer"}, or {@code "boolean"}.
     */
    public String getDataType() {
        return this.dataType;
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
        return this.value;
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
     * Is this variable equal to another object?
     *
     * @param o An object for comparison.
     * @return  A boolean indicator of whether the other variable is identical.
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass())	return false;
        Variable v = (Variable) o;
        if ((this.dataType == null) != (v.getDataType() == null)) {
            return false;
        }
        if (!this.dataType.equals(v.getDataType())) {
            return false;
        }
        if (this.id != v.getId()) {
            return false;
        }
        if ((this.variableName == null) != (v.getVariableName() == null)) {
            return false;
        }
        if (!this.variableName.equals(v.getVariableName())) {
            return false;
        }
        if ((this.value == null) != (v.getValue() == null)) {
            return false;
        }
        if (this.dataType != null && v.getDataType() != null && this.value != null && v.getValue() != null && this.dataType.equals("long text") && !(((String) v.getValue()).equals((String) this.value))) {
            return false;
        }
        if (this.dataType != null && v.getDataType() != null && this.value != null && v.getValue() != null && (this.dataType.equals("integer") || this.dataType.equals("boolean")) && !(((int) v.getValue()) == ((int) this.value))) {
            return false;
        }
        if (this.dataType != null && v.getDataType() != null && this.value != null && v.getValue() != null && this.dataType.equals("short text") && !(((Entity) v.getValue()).equals((Entity) this.value))) {
            return false;
        }
        return true;
    }
}