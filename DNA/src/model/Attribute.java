package model;

import java.awt.Color;

public class Attribute {
	int id, childOf;
	String value, type, alias, notes;
	Color color;
	boolean inDatabase;

	public Attribute(int id, String value, Color color, String type, String alias, String notes, int childOf, boolean inDatabase) {
		this.id = id;
		this.childOf = childOf;
		this.value = value;
		this.type = type;
		this.alias = alias;
		this.notes = notes;
		this.color = color;
		this.inDatabase = inDatabase;
	}
	
	public Attribute(String value) {
		this.value = value;
		this.id = -1;
		this.color = Color.BLACK;
		this.type = "";
		this.alias = "";
		this.notes = "";
		this.inDatabase = false;
	}

	public boolean isInDatabase() {
		return inDatabase;
	}

	public void setInDatabase(boolean inDatabase) {
		this.inDatabase = inDatabase;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getChildOf() {
		return childOf;
	}

	public void setChildOf(int childOf) {
		this.childOf = childOf;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public String toString() {
		return this.value;
	}
}