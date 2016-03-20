package dna.dataStructures;

import java.awt.Color;

public class Regex implements Comparable<Regex> {
	
	String label;
	Color color;

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
	 * Set the string label of a regular expression.
	 * 
	 * @param label  The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
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
	 * Set the color of a regular expression.
	 * 
	 * @param color  The color to set.
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	// how should entries be sorted in a list?
	public int compareTo(Regex regex) {
		if (this.getLabel().compareTo(regex.getLabel()) < 0) {
			return -1;
		} else if (this.getLabel().compareTo(regex.getLabel()) > 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	//necessary for sorting purposes
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (getClass() != o.getClass()) return false;
		return compareTo((Regex) o) == 0;
	}
}