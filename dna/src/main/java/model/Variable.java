package model;

/**
 * Represents values saved in a statement, corresponding to the variables saved
 * in the statement type that corresponds to the statement.
 */
public class Variable {
    private int variableId;
    private String variableName, dataType;

    /**
     * Creates a value.
     *
     * @param variableId The ID of the variable of which the value is an instance.
     * @param variableName The variable name.
     * @param dataType The data type of the value. Can be {@code "short text"}, {@code "long text"}, {@code "integer"}, or {@code "boolean"}.
     */
    public Variable(int variableId, String variableName, String dataType) {
        this.variableId = variableId;
        this.variableName = variableName;
        this.dataType = dataType;
    }

    /**
     * Copy constructor to create a deep clone of a variable object.
     *
     * @param variable A variable.
     */
    public Variable(Variable variable) {
        this.variableId = variable.getVariableId();
        this.variableName = variable.getVariableName();
        this.dataType = variable.getDataType();
    }

    /**
     * Get the variable ID.
     *
     * @return The variable ID.
     */
    public int getVariableId() {
        return this.variableId;
    }

    /**
     * Set the variable ID.
     *
     * @param variableId The variable ID.
     */
    public void setVariableId(int variableId) {
        this.variableId = variableId;
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
     * Get a String representation of the variable.
     *
     * @return The variable name as a String representation of the variable.
     */
    public String toString() {
        return this.getVariableName();
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
        if (this.variableId != v.getVariableId()) {
            return false;
        }
        if ((this.variableName == null) != (v.getVariableName() == null)) {
            return false;
        }
        if (!this.variableName.equals(v.getVariableName())) {
            return false;
        }
        return true;
    }
}