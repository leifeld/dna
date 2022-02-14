package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;

import model.Coder;

class CoderComboBoxRenderer implements ListCellRenderer<Object> {
	int buttonSize, margin, maxNameLength;
	
	/**
	 * Constructor for CoderComboBoxRenderer. Create a new renderer.
	 * 
	 * @param buttonSize     Height/width of the color button.
	 * @param margin         Border margin. Can be 0.
	 * @param maxNameLength  Maximal character length of the name.
	 */
	public CoderComboBoxRenderer(int buttonSize, int margin, int maxNameLength) {
		this.buttonSize = buttonSize;
		this.margin = margin;
		this.maxNameLength = maxNameLength;
	}
	
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value == null) {
			return new JLabel("select coder...");
		} else {
			Coder coder = (Coder) value;
			CoderBadgePanel cbp = new CoderBadgePanel(coder, CoderComboBoxRenderer.this.buttonSize, CoderComboBoxRenderer.this.margin, CoderComboBoxRenderer.this.maxNameLength);
			if (isSelected) {
				UIDefaults defaults = javax.swing.UIManager.getDefaults();
				Color bg = defaults.getColor("List.selectionBackground");
				cbp.setBackground(bg);
			}
			return cbp;
		}
	}
}