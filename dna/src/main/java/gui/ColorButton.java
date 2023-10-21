package gui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JButton;

/**
 * A custom Button for displaying and choosing a color
 */
class ColorButton extends JButton {
	private static final long serialVersionUID = -8121834065246525986L;
	private model.Color color;
	
	public ColorButton() {
		this.color = new model.Color(0, 0, 0);
		this.setPreferredSize(new Dimension(18, 18));
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(color.toAWTColor());
		g.fillRect(0, 0, 18, 18);
	}
	
	void setColor(model.Color color) {
		this.color = color;
		this.repaint();
	}
	
	model.Color getColor() {
		return this.color;
	}
}