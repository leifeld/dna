package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import dna.Dna;
import model.Coder;
import sql.Sql.SqlListener;

/**
 * Represents a coder selection panel for selecting the active coder.
 */
class CoderSelectionPanel extends JPanel implements SqlListener {
	private static final long serialVersionUID = 7852541276993938860L;
	JComboBox<Coder> coderBox;

	/**
	 * Create a new coder selection panel.
	 */
	public CoderSelectionPanel() {
		setLayout(new BorderLayout());
		coderBox = new JComboBox<Coder>();
		coderBox.setRenderer(new CoderRenderer());
		coderBox.setPreferredSize(new Dimension(34, 34));
		coderBox.setEnabled(false);
		this.add(coderBox, BorderLayout.CENTER);
	}
	
	/**
	 * Get a reference to the JComboBox containing the coders.
	 * 
	 * @return The coder box.
	 */
	JComboBox<Coder> getCoderBox() {
		return coderBox;
	}

	@Override
	public void adjustToChangedConnection() {
		if (Dna.sql.getConnectionProfile() != null) {
			ArrayList<Coder> coders = Dna.sql.getCoders();
			coderBox.removeAllItems();
			if (coders.size() > 0) {
				for (int i = 0; i < coders.size(); i++) {
					coderBox.addItem(coders.get(i));
				}
			}
			coderBox.setEnabled(true);
		} else {
			coderBox.removeAllItems();
			coderBox.setEnabled(false);
		}
	}

	@Override
	public void adjustToChangedCoder() {
		// nothing to do; coder updates must be done manually in the main window to avoid infinite looping
	}
}