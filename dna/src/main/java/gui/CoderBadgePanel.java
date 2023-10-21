package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dna.Dna;
import model.Coder;

/**
 * JPanel displaying the color and name of a coder.
 */
public class CoderBadgePanel extends JPanel {
	private static final long serialVersionUID = 2559090800466724235L;
	private Coder coder;
	private JLabel coderName;

	/**
	 * Constructor for CoderBadgePanel
	 * 
	 * @param coder  A Coder object, which contains ID, name, and color.
	 */
	public CoderBadgePanel(Coder coder) {
		this.coder = coder;
		createLayout(18, 0, 22);
	}

	/**
	 * Constructor for CoderBadgePanel
	 * 
	 * @param coder          A Coder object, which contains ID, name, and color.
	 * @param buttonSize     Height/width of the color button.
	 * @param border         Border margin. Can be 0.
	 * @param maxNameLength  Maximal character length of the name.
	 */
	public CoderBadgePanel(Coder coder, int buttonSize, int border, int maxNameLength) {
		this.coder = coder;
		createLayout(buttonSize, border, maxNameLength);
	}

	/**
	 * Constructor for CoderBadgePanel which looks up the active coder
	 */
	public CoderBadgePanel() {
		if (Dna.sql.getConnectionProfile() == null || Dna.sql.getActiveCoder() == null) {
			this.coder = new Coder(-1, "(no coder)", new model.Color(0, 0, 0));
		} else {
			this.coder = Dna.sql.getActiveCoder();
		}
		
		createLayout(18, 1, 22);
	}
	
	/**
	 * Creates the layout of the coder badge panel. For internal use in the
	 * class.
	 *
	 * @param buttonSize     Height/width of the color button.
	 * @param border         Border margin. Can be 0.
	 * @param maxNameLength  Maximal character length of the name.
	 */
	private void createLayout(int buttonSize, int border, int maxNameLength) {
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JButton colorButton = (new JButton() {
			private static final long serialVersionUID = -7254611710375602710L;
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(CoderBadgePanel.this.coder.getColor().toAWTColor());
				g.fillRect(0, 0, buttonSize, buttonSize);
			}
		});
		colorButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
		colorButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		colorButton.setEnabled(false);
		String name = CoderBadgePanel.this.coder.getName();

		int nameLength = name.length();
		if (nameLength > maxNameLength) {
			nameLength = maxNameLength - 3;
			name = name.substring(0,  nameLength);
			name = name + "...";
		}

		CoderBadgePanel.this.coderName = new JLabel(name);
		CoderBadgePanel.this.coderName.setLabelFor(colorButton);
		this.add(colorButton);
		this.add(CoderBadgePanel.this.coderName);
		if (border > 0) {
			this.setBorder(new EmptyBorder(border, 0, border, 0));
		}
	}
	
	/**
	 * Set the color of the coder name label.
	 * 
	 * @param color The new color for the name.
	 */
	void setCoderNameColor(Color color) {
		this.coderName.setForeground(color);
	}
	
	/**
	 * Get the coder.
	 * 
	 * @return The coder.
	 */
	public Coder getCoder() {
		return this.coder;
	}
}