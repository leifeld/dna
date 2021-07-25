package dna.dataStructures;

import java.awt.Color;
import java.util.HashMap;

public class Coder implements Comparable<Coder> {
	int id;
	Color color;
	String name, password;
	HashMap<String, Boolean> permissions;
	
	public Coder(
			int id, 
			String name, 
			Color color, 
			String password, 
			HashMap<String, Boolean> permissions
			) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.password = password;
		this.permissions = permissions;
	}
	
	public Coder(int id) {
		this.id = id;
		this.name = "";
		this.color = Color.YELLOW;
		this.password = "";
		this.permissions = new HashMap<String, Boolean>();
		this.permissions.put("addDocuments", true);
		this.permissions.put("editDocuments", true);
		this.permissions.put("deleteDocuments", true);
		this.permissions.put("importDocuments", true);
		this.permissions.put("viewOthersDocuments", true);
		this.permissions.put("editOthersDocuments", true);
		this.permissions.put("addStatements", true);
		this.permissions.put("viewOthersStatements", true);
		this.permissions.put("editOthersStatements", true);
		this.permissions.put("editCoders", true);
		this.permissions.put("editStatementTypes", true);
		this.permissions.put("editRegex", true);
	}
	
	public String toString() {
		return(this.getName());
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
	 * @param color the color to set
	 */
	public void setColor(String color) {
		this.color = new Color(Integer.parseInt(color.substring(1), 16));
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * @return the permissions
	 */
	public HashMap<String, Boolean> getPermissions() {
		return permissions;
	}

	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(HashMap<String, Boolean> permissions) {
		this.permissions = permissions;
	}

	// how should entries be sorted in a list?
	public int compareTo(Coder c) {
		if (this.getName().compareTo(c.getName()) < 0) {
			return -1;
		} else if (this.getName().compareTo(c.getName()) > 0) {
			return 1;
		} else {
			if (((Integer) this.getId()).compareTo(c.getId()) < 0) {
				return -1;
			} else if (((Integer) this.getId()).compareTo(c.getId()) > 0) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	//necessary for sorting purposes
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (getClass() != o.getClass()) return false;
		return compareTo((Coder) o) == 0;
	}
}