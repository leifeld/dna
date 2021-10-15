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
	 * Get the statement type color.
	 * 
	 * @return The color of the statement type.
	 */
	public Color getColor() {
		return color;
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
}