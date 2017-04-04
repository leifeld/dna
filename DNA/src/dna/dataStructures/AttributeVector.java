package dna.dataStructures;

import java.awt.Color;
import dna.Dna;

public class AttributeVector implements Comparable<AttributeVector> {
	int id;
	String value;
	Color color;
	String type;
	String alias;
	String notes;
	String childOf;
	int statementTypeId;
	String variable;

	public AttributeVector(int id, String value, Color color, String type, String alias, String notes, String childOf, int statementTypeId, String variable) {
		this.id = id;
		this.value = value;
		this.color = color;
		this.type = type;
		this.alias = alias;
		this.notes = notes;
		this.childOf = childOf;
		this.statementTypeId = statementTypeId;
		this.variable = variable;
	}
	
	public String toString() {
		return this.value;
	}
	
	/**
	 * Checks if the present value is contained in the dataset or not.
	 * 
	 * @return boolean variable indicating whether the present value is contained in the current dataset
	 */
	public boolean isInDataset() {
		for (int i = 0; i < Dna.data.getStatements().size(); i++) {
			if (Dna.data.getStatements().get(i).getStatementTypeId() == this.getStatementTypeId() 
					&& Dna.data.getStatements().get(i).getValues().get(this.variable).equals(this.value)) {
				return true;
			}
		}
		return false;
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
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the color
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * @return the childOf
	 */
	public String getChildOf() {
		return childOf;
	}

	/**
	 * @param childOf the childOf to set
	 */
	public void setChildOf(String childOf) {
		this.childOf = childOf;
	}
	
	/**
	 * @return the statementTypeId
	 */
	public int getStatementTypeId() {
		return statementTypeId;
	}

	/**
	 * @param statementTypeId the statementTypeId to set
	 */
	public void setStatementTypeId(int statementTypeId) {
		this.statementTypeId = statementTypeId;
	}

	/**
	 * @return the variable
	 */
	public String getVariable() {
		return variable;
	}
	
	// necessary for sorting purposes
	public int hashCode(){
        return this.id; // this.hashCode()
    }
	
	/**
	 * @param variable the variable to set
	 */
	public void setVariable(String variable) {
		this.variable = variable;
	}

	// how should entries be sorted in a list or table?
	public int compareTo(AttributeVector av) {
		if (((Integer) this.getStatementTypeId()).compareTo(av.getStatementTypeId()) < 0) {
			return -1;
		} else if (((Integer) this.getStatementTypeId()).compareTo(av.getStatementTypeId()) > 0) {
			return 1;
		} else {
			if (this.getVariable().compareTo(av.getVariable()) < 0) {
				return -1;
			} else if (this.getVariable().compareTo(av.getVariable()) > 0) {
				return 1;
			} else {
				if (this.getValue().compareTo(av.getValue()) < 0) {
					return -1;
				} else if (this.getValue().compareTo(av.getValue()) > 0) {
					return 1;
				} else {
					return 0;
				}
			}
		}
	}
	
	//necessary for sorting purposes
	public boolean equals(AttributeVector o) {
		if (o == null) return false;
		if (this == o) return true;
		if (getClass() != o.getClass()) return false;
		return compareTo((AttributeVector) o) == 0;
	}
}