package export;

/**
 * Data for representing a barplot, including the variable name, the distinct values for the variable in alphabetical
 * order, the frequency of occurrence after filtering grouped by qualifier level, the attributes for the variable, the
 * distinct qualifier values present in the filtered data, and the names of the attribute variables.
 */
public class BarplotResult {
    /**
     * The name of the variable for which the barplot is constructed.
     */
    private String variable;
    /**
     * The distinct and alphabetically ordered entity labels, for example the concepts, as strings.
     */
    private String[] values;
    /**
     * An m x n 2D int array with the frequencies of the m entity values in the filtered statements grouped by the n
     * qualifier levels.
     */
    private int[][] counts;
    /**
     * An m x n 2D String array with the n attributes for the m entities.
     */
    private String[][] attributes;
    /**
     * The distinct qualifier values that correspond to the n columns in {@link #counts}.
     */
    private int[] intValues;
    /**
     * The distinct attribute variable names that correspond to the n columns in {@link #attributes}.
     */
    private String[] attributeVariables;

    /**
     * Constructor to create a data structure holding data for generating a barplot for a variable.
     *
     * @param variable The {@link #variable}.
     * @param values The {@link #values}.
     * @param counts The {@link #counts}.
     * @param attributes The {@link #attributes}.
     * @param intValues The {@link #intValues}.
     * @param attributeVariables The {@link #attributeVariables}.
     */
    public BarplotResult(String variable, String[] values, int[][] counts, String[][] attributes, int[] intValues, String[] attributeVariables) {
        this.variable = variable;
        this.values = values;
        this.counts = counts;
        this.attributes = attributes;
        this.intValues = intValues;
        this.attributeVariables = attributeVariables;
    }

    /**
     * @return The {@link #variable}.
     */
    public String getVariable() {
        return variable;
    }

    /**
     * @return The {@link #values}.
     */
    public String[] getValues() {
        return values;
    }

    /**
     * @return The {@link #counts}.
     */
    public int[][] getCounts() {
        return counts;
    }

    /**
     * @return The {@link #attributes}.
     */
    public String[][] getAttributes() {
        return attributes;
    }

    /**
     * @return The {@link #intValues}.
     */
    public int[] getIntValues() {
        return intValues;
    }

    /**
     * @return The {@link #attributeVariables}.
     */
    public String[] getAttributeVariables() {
        return attributeVariables;
    }
}