package dna;

import java.awt.Color;

public class RegexTerm {
	String pattern;
	Color color;
	
	public RegexTerm(String pattern, Color color) {
		this.pattern = pattern;
		this.color = color;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}