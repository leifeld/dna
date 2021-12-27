package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import dna.Dna;
import model.Coder;

/**
 * Represents a coder selection panel for selecting the active coder.
 */
class CoderSelectionPanel extends JPanel {
	private static final long serialVersionUID = 7852541276993938860L;
	private CoderBadgePanel coderBadgePanel;
	private JButton changeCoderButton;
	private GridBagConstraints gbc;

	/**
	 * Create a new coder selection panel.
	 */
	public CoderSelectionPanel() {
		setLayout(new GridBagLayout());
		ImageIcon changeCoderIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-users.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		changeCoderButton = new JButton("Change coder", changeCoderIcon);
		changeCoderButton.setEnabled(false);
		JToolBar tb = new JToolBar();
		tb.add(changeCoderButton);
		tb.setFloatable(false);
		tb.setRollover(true);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		this.add(tb, gbc);
		
		coderBadgePanel = new CoderBadgePanel();
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		this.add(coderBadgePanel, gbc);
	}
	
	/**
	 * Get a reference to the change coder button.
	 * 
	 * @return The change coder button.
	 */
	JButton getChangeCoderButton() {
		return this.changeCoderButton;
	}
	
	/**
	 * Change the coder badge and (de)activate the change coder button.
	 */
	void changeCoderBadge() {
		this.gbc.gridx = 1;
		this.gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		this.gbc.weightx = 0.0;
		this.remove(coderBadgePanel);
		Coder coder = Dna.sql.getActiveCoder();
		if (coder != null) {
			this.coderBadgePanel = new CoderBadgePanel(coder, 18, 0, 30);
		} else {
			this.coderBadgePanel = new CoderBadgePanel();
		}
		this.add(this.coderBadgePanel);
		this.revalidate();
		this.repaint();
		changeCoderButton.setEnabled(true);
		if (Dna.sql.getConnectionProfile() == null || Dna.sql.getActiveCoder() == null) {
			changeCoderButton.setEnabled(false);
		} else {
			changeCoderButton.setEnabled(true);
		}
	}
}