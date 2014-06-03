package dna;

import java.awt.Color;
import java.util.HashMap;

public class StatementType {
	
	String label;
	Color color;
	HashMap<String, String> variables;

	public StatementType(String label, Color color, 
			HashMap<String, String> variables) {
		this.label = label;
		this.color = color;
		this.variables = variables;
	}

	/**
	 * Retrieve the label of the statement type.
	 * 
	 * @return  The label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label of the statement type.
	 * 
	 * @param label  The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Retrieve the color of the statement type.
	 * 
	 * @return  The color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Set the color of the statement type.
	 * 
	 * @param color  The color to set.
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Retrieve a hash map with the variables and their data types.
	 * 
	 * @return  The variables.
	 */
	public HashMap<String, String> getVariables() {
		return variables;
	}

	/**
	 * Set the hashmap with the variables and data types.
	 * 
	 * @param variables  The variables to set.
	 */
	public void setVariables(HashMap<String, String> variables) {
		this.variables = variables;
	}
}
