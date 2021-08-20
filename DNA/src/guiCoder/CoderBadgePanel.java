package guiCoder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dna.Dna;

/**
 * JPanel displaying the color and name of a coder.
 */
public class CoderBadgePanel extends JPanel {
	private static final long serialVersionUID = 2559090800466724235L;

	/**
	 * Constructor for CoderBadgePanel
	 * 
	 * @param coder  A Coder object, which contains ID, name, and color.
	 */
	public CoderBadgePanel(Coder coder) {
		createLayout(coder, 18, 22);
	}

	/**
	 * Constructor for CoderBadgePanel
	 * 
	 * @param coder  A Coder object, which contains ID, name, and color.
	 */
	public CoderBadgePanel(Coder coder, int size, int maxNameLength) {
		createLayout(coder, size, maxNameLength);
	}

	/**
	 * Constructor for CoderBadgePanel which looks up the active coder
	 */
	public CoderBadgePanel() {
		Coder coder;
		if (Dna.sql == null) {
			coder = new Coder(-1, "(no coder)", Color.BLACK);
		} else {
			coder = Dna.sql.getCoder(Dna.sql.getConnectionProfile().getCoderId());
		}
		
		createLayout(coder, 18, 22);
	}
	
	private void createLayout(Coder coder, int size, int maxNameLength) {
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JButton colorButton = (new JButton() {
			private static final long serialVersionUID = -7254611710375602710L;
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(coder.getColor());
				g.fillRect(0, 0, size, size);
			}
		});
		colorButton.setPreferredSize(new Dimension(size, size));
		colorButton.setEnabled(false);
		String name = coder.getName();

		int nameLength = name.length();
		if (nameLength > maxNameLength) {
			nameLength = maxNameLength - 3;
			name = name.substring(0,  nameLength);
			name = name + "...";
		}

		JLabel coderName = new JLabel(name);
		coderName.setLabelFor(colorButton);
		this.add(colorButton);
		this.add(coderName);
	}
}