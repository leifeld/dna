package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

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
		coderBox.setRenderer(new CoderComboBoxRenderer());
		coderBox.setPreferredSize(new Dimension(34, 34));
		coderBox.setEnabled(false);
		this.add(coderBox, BorderLayout.CENTER);
	}
	
	/**
	 * A combo box renderer that displays coder badges.
	 */
	private class CoderComboBoxRenderer implements ListCellRenderer<Object> {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Coder coder = (Coder) value;
			CoderBadgePanel cbp;
			if (coder == null) {
				cbp = new CoderBadgePanel();
			} else {
				cbp = new CoderBadgePanel(coder, 18, 100);
			}
			
			// list background
			Color selectedColor = javax.swing.UIManager.getColor("List.dropCellBackground");
			
			// selected entry that is not in database: average of the previous two colors
			Color defaultColor = javax.swing.UIManager.getColor("List.background");
			if (isSelected == true) {
				cbp.setBackground(selectedColor);
			} else {
				cbp.setBackground(defaultColor);
			}
			cbp.setOpaque(true);
			cbp.setBorder(new EmptyBorder(5, 5, 5, 5));
			
			return cbp;
		}
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
			coderBox.setEnabled(false);
		}
	}

	@Override
	public void adjustToChangedCoder() {
		// nothing to do; coder updates must be done manually in the main window to avoid infinite looping
	}
}