package model;

import java.awt.Color;
import java.util.ArrayList;

/**
 * Represents a statement type.
 */
public class StatementType {
	/**
	 * The statement type ID.
	 */
	private int id;
	
	/**
	 * The label or name of the statement type.
	 */
	private String label;
	
	/**
	 * The color associated with the statement type.
	 */
	private Color color;
	
	/**
	 * An array list of (usually empty) values that represent the data structure
	 * of variables that are contained in the statement type. 
	 */
	private ArrayList<Value> variables;

	/**
	 * Create a statement type.
	 * 
	 * @param id         The statement type ID.
	 * @param label      The label or name of the statement type.
	 * @param color      The color of the statement type.
	 * @param variables  An array list of values representing the variables. Can
	 *   be empty values (using the {@link model.Value#Value(int, String,
	 *   String) simplified constructor} for the {@link model.Value Value}
	 *   class.
	 */
	public StatementType(int id, String label, Color color, ArrayList<Value> variables) {
		this.id = id;
		this.label = label;
		this.color = color;
		this.variables = variables;
	}

	/**
	 * Copy constructor for statement type objects to create a deep clone.
	 * 
	 * @param st A statement type.
	 */
	public StatementType(StatementType st) {
		this.id = st.getId();
		this.label = st.getLabel();
		this.color = st.getColor();
		this.variables = new ArrayList<Value>();
		for (int i = 0; i < st.getVariables().size(); i++) {
			this.variables.add(new Value(st.getVariables().get(i)));
		}
	}
	
	/**
	 * Get the statement type ID.
	 * 
	 * @return The statement type ID.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the statement type label/name.
	 * 
	 * @return The statement type label/name.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get the statement type color.
	 * 
	 * @return The color of the statement type.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * An array list of (possibly empty) values that represents the variables
	 * and their data types associated with the statement type.
	 * 
	 * @return The array list of values stored in the statement type.
	 */
	public ArrayList<Value> getVariables() {
		return variables;
	}
	
	/**
	 * Is this statement type equal to another statement type?
	 * 
	 * @param st  The other statement type for comparison.
	 * @return    Boolean indicator: Is the other statement type identical?
	 */
	public boolean equals(StatementType st) {
		boolean valuesEqual = true;
		if (this.getVariables().size() == st.getVariables().size()) {
			for (int i = 0; i < this.getVariables().size(); i++) {
				if (!this.getVariables().get(i).equals(st.getVariables().get(i))) {
					valuesEqual = false;
					break;
				}
			}
		} else {
			valuesEqual = false;
		}
		if (this.getId() == st.getId() &&
				this.getLabel().equals(st.getLabel()) &&
				this.getClass().equals(st.getColor()) &&
				valuesEqual) {
			return true;
		} else {
			return false;
		}
	}
}