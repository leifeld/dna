package dna;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import dna.dataStructures.Coder;

/**
 * Renderer for coders in a JComboBox
 */
class CoderComboBoxRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Coder coder = Dna.data.getCoders().get((int) value);
		this.setText(coder.getName());
		this.setForeground(new Color(coder.getRed(), coder.getGreen(), coder.getBlue()));
		return this;
	}
}