package dna;

import java.awt.Color;

public class Regex {
	
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

}
