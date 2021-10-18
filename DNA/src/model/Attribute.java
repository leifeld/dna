package model;

import java.awt.Color;

public class Attribute {
	private int id, childOf;
	private String value;
	private Color color;
	private boolean inDatabase;

	public Attribute(int id, String value, Color color, int childOf, boolean inDatabase) {
		this.id = id;
		this.childOf = childOf;
		this.value = value;
		this.color = color;
		this.inDatabase = inDatabase;
	}
	
	public Attribute(String value) {
		this.value = value;
		this.id = -1;
		this.color = Color.BLACK;
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