package export;

import java.util.ArrayList;

public class DataFrame {
    /**
     * A two-dimensional array holding all the data. The first dimension indexes the rows. The second dimension indexes
     * the columns. Columns can have different data types while rows cannot.
     */
    Object[][] data;

    /**
     * The column names (= variable names) for each column in the data frame. The vector must have the same length as
     * the second dimension of {@link #data}.
     */
    String[] variableNames;

    /**
     * The data types held in each column. Valid data types are {@code "int"} (for integer numbers) and {@code String}.
     * Other data types can be used if they are understood by all classes. The vector must have the same length as the
     * second dimension of {@link #data}.
     */
    String[] dataTypes;

    /**
     * Create a new data frame.
     *
     * @param data The data.
     * @param variableNames The variable names.
     * @param dataTypes The data types.
     */
    public DataFrame(Object[][] data, String[] variableNames, String[] dataTypes) {
        this.data = data;
        this.variableNames = variableNames;
        this.dataTypes = dataTypes;
    }

    /**
     * Get a specific value from the data frame using row and column indices.
     *
     * @param row The index of the row, starting at {@code 0}.
     * @param column The index of the column, starting at {@code 0}.
     * @return The object stored in the data frame at the specified position.
     */
    public Object getValue(int row, int column) {
        return this.data[row][column];
    }

    /**
     * Get a specific value from the data frame using a row index and the variable name.
     *
     * @param row The index of the row, starting at {@code 0}.
     * @param variableName The name of the variable as specified in {@link #variableNames}.
     * @return The object stored in the data frame at the specified position.
     */
    public Object getValue(int row, String variableName) {
        int columnIndex = -1;
        for (int i = 0; i < this.variableNames.length; i++) {
            if (this.variableNames[i].equals(variableName)) {
                columnIndex = i;
            }
            if (columnIndex > -1) break;
        }
        return this.getValue(row, columnIndex);
    }

    /**
     * Get the data array containing all values.
     *
     * @return The two-dimensional array holding all data.
     */
    public Object[][] getData() {
        return this.data;
    }

    /**
     * Get the data type for a specific column.
     *
     * @param column The index of the column.
     * @return The data type, for example {@code "int"} or {@code "String"}.
     */
    public String getDataType(int column) {
        return this.dataTypes[column];
    }

    /**
     * Get the data types for all columns.
     *
     * @return A vector with data types, for example {@code "int"} or {@code "String"}.
     */
    public String[] getDataTypes() {
        return this.dataTypes;
    }

    /**
     * Get the variable name of a specific column.
     *
     * @param column The index of the column.
     * @return The column name as stored in the {@link #variableNames} slot.
     */
    public String getVariableName(int column) {
        return this.variableNames[column];
    }

    /**
     * Get the variable names for all columns.
     *
     * @return A vector with column names.
     */
    public String[] getVariableNames() {
        return this.variableNames;
    }

    /**
     * Return the number of columns in the data frame.
     *
     * @return The number of columns.
     */
    public int ncol() {
        return this.variableNames.length;
    }

    /**
     * Return the number of rows in the data frame.
     *
     * @return The number of rows.
     */
    public int nrow() {
        return this.data.length;
    }

    /**
     * Delete rows from the data frame by creating a new data array with fewer rows.
     *
     * @param rows An array list with row indices, starting at {@code 0}.
     */
    public void deleteRows(ArrayList<Integer> rows) {
        Object[][] newData = new Object[nrow() - rows.size()][ncol()];
        int rowCounter = 0;
        for (int i = 0; i < nrow(); i++) {
            if (!rows.contains(i)) {
                for (int j = 0; j < ncol(); j++) {
                    newData[rowCounter][j] = this.data[i][j];
                }
                rowCounter++;
            }
        }
        this.data = newData;
    }
}