package dna.dataStructures;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class StatementType implements Comparable<StatementType> {
	int id;
	String label;
	Color color;
	LinkedHashMap<String, String> variables;

	public StatementType(int id, String label, String color, LinkedHashMap<String, String> variables) {
		this.id = id;
		this.label = label;
		this.color = new Color(Integer.parseInt(color.substring(1), 16));
		this.variables = variables;
	}
	
	public StatementType(int id, String label, Color color, LinkedHashMap<String, String> variables) {
		this.id = id;
		this.label = label;
		this.color = color;
		this.variables = variables;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
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
	 * Retrieve the red color (as part of RGB) of the statement type.
	 * 
	 * @return  The red color.
	 */
	public int getRed() {
		return color.getRed();
	}

	/**
	 * Retrieve the green color (as part of RGB) of the statement type.
	 * 
	 * @return  The green color.
	 */
	public int getGreen() {
		return color.getGreen();
	}

	/**
	 * Retrieve the blue color (as part of RGB) of the statement type.
	 * 
	 * @return  The blue color.
	 */
	public int getBlue() {
		return color.getBlue();
	}

	/**
	 * Set the color of the statement type.
	 * 
	 * @param color  The color to set, as a hex RGB string, such as "#FFFF00".
	 */
	public void setColor(String color) {
		this.color = new Color(Integer.parseInt(color.substring(1), 16));
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
	public LinkedHashMap<String, String> getVariables() {
		return variables;
	}

	/**
	 * Retrieve an array list of variable names of only a specific type.
	 * 
	 * @return  The variables as an array list of strings.
	 */
	public ArrayList<String> getVariablesByType(String type) {
		ArrayList<String> vars = new ArrayList<String>();
		Iterator<String> keyIterator = variables.keySet().iterator();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			String value = variables.get(key);
			if (value.equals(type)) {
				vars.add(key);
			}
		}
		return(vars);
	}
	
	/**
	 * Set the hashmap with the variables and data types.
	 * 
	 * @param variables  The variables to set.
	 */
	public void setVariables(LinkedHashMap<String, String> variables) {
		this.variables = variables;
	}
	
	// this tells the array list how to sort statement types
	public int compareTo(StatementType s) {
		if (this.getLabel().compareTo(s.getLabel()) < 0) {
			return -1;
		} else if (this.getLabel().compareTo(s.getLabel()) > 0) {
			return 1;
		} else {
			if (this.getColor().equals(s.getColor())) {
				if (this.getVariables().equals(s.getVariables())) {
					return 0;
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		}
	}
	
	//necessary for sorting purposes
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (getClass() != o.getClass()) return false;
		return compareTo((StatementType) o) == 0;
	}
}
