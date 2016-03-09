package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;

import dna.dataStructures.Coder;

/**
 * Renderer for coders in a JComboBox
 */
class CoderComboBoxRenderer extends JLabel implements ListCellRenderer {
	//private static final long serialVersionUID = 1L;
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Coder coder = (Coder) value;
		
		JButton colorRectangle = new JButton();
		colorRectangle.setPreferredSize(new Dimension(16, 16));
		colorRectangle.setBackground(coder.getColor());
		
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		namePanel.add(colorRectangle);
		namePanel.add(Box.createRigidArea(new Dimension(5,5)));
		JLabel coderLabel = new JLabel(coder.getName());
		namePanel.add(coderLabel);
		
		if (isSelected) {
			UIDefaults defaults = javax.swing.UIManager.getDefaults();
			Color bg = defaults.getColor("List.selectionBackground");
			namePanel.setBackground(bg);
			namePanel.setBackground(bg);
		}
		
		return namePanel;
	}
}