package model;

import java.awt.Color;
import java.util.HashMap;

public class Entity implements Comparable<Entity> {
	private int id, childOf;
	private String value;
	private Color color;
	private boolean inDatabase;
	private HashMap<String, String> attributeValues;

	public Entity(int id, String value, Color color, int childOf, boolean inDatabase, HashMap<String, String> metaValues) {
		this.id = id;
		this.childOf = childOf;
		this.value = value;
		this.color = color;
		this.inDatabase = inDatabase;
		this.attributeValues = metaValues;
	}
	
	public Entity(String value) {
		this.value = value;
		this.id = -1;
		this.color = Color.BLACK;
		this.inDatabase = false;
		this.attributeValues = null;
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
	
	/**
	 * @return the attributeValues
	 */
	public HashMap<String, String> getAttributeValues() {
		return attributeValues;
	}

	/**
	 * @param attributeValues the metaValues to set
	 */
	public void setMetaValues(HashMap<String, String> attributeValues) {
		this.attributeValues = attributeValues;
	}

	public String toString() {
		return this.value;
	}

	@Override
	public int compareTo(Entity a) {
		return this.getValue().compareTo(a.getValue());
	}
}