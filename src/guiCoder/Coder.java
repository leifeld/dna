package guiCoder;

import java.awt.Color;

public class Coder {
	int id;
	String name;
	Color color;

	public Coder(int id, String name, Color color) {
		this.id = id;
		this.name = name;
		this.color = color;
	}

	public Coder(int id, String name, int red, int green, int blue) {
		this.id = id;
		this.name = name;
		this.color = new Color(red, green, blue);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}