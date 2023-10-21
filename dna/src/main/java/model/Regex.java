package model;

/**
 * A class for regular expression patterns to be highlighted in the document
 * text in a specified color. 
 */
public class Regex implements Comparable<Regex> {
	private String label;
	private Color color;
	
	/**
	 * Create a new Regex object.
	 * 
	 * @param label The regular expression pattern.
	 * @param color The color of the regular expression.
	 */
	public Regex(String label, Color color) {
		this.label = label;
		this.color = color;
	}

	/**
	 * Retrieve the string label of a regular expression.
	 * 
	 * @return  The label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Retrieve the color of a regular expression.
	 * 
	 * @return  The color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * How should entries be sorted in a list?
	 * 
	 * @param regex  Another {@link Regex} object to compare to.
	 * @return       {@code -1} if the current regex is lower in the sort
	 *   order, {@code 0} if it is equal, or {@code 1} if it is higher.
	 */
	public int compareTo(Regex regex) {
		if (this.getLabel().compareTo(regex.getLabel().toLowerCase()) < 0) {
			return -1;
		} else if (this.getLabel().compareTo(regex.getLabel().toLowerCase()) > 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * Check if the regex is equal to another regex. Necessary for sorting
	 * purposes.
	 * 
	 * @param o  Another object, e.g., a regex.
	 * @return   Is the object equal to the current regex?
	 */
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (getClass() != o.getClass()) return false;
		return compareTo((Regex) o) == 0;
	}
}