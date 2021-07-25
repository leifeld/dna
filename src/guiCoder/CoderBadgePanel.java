package guiCoder;

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
class CoderBadgePanel extends JPanel {
	private static final long serialVersionUID = 2559090800466724235L;

	/**
	 * Constructor for CoderBadgePanel
	 * 
	 * @param coder  A Coder object, which contains ID, name, and color.
	 */
	public CoderBadgePanel(Coder coder) {
		createLayout(coder);
	}

	/**
	 * Constructor for CoderBadgePanel which looks up the active coder
	 */
	public CoderBadgePanel() {
		Coder coder = Dna.sql.getCoder(Dna.sql.getConnectionProfile().getCoderId());
		createLayout(coder);
	}
	
	private void createLayout(Coder coder) {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton colorButton = (new JButton() {
			private static final long serialVersionUID = -7254611710375602710L;
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(coder.getColor());
				g.fillRect(0, 0, 18, 18);
			}
		});
		colorButton.setPreferredSize(new Dimension(18, 18));
		colorButton.setEnabled(false);
		String name = coder.getName();
		JLabel coderName = new JLabel(name);
		coderName.setLabelFor(colorButton);
		this.add(colorButton);
		this.add(coderName);
	}
}