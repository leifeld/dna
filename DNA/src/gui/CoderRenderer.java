package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

import model.Coder;

/**
 * A combo box or list cell renderer that displays coder badges.
 */
class CoderRenderer implements ListCellRenderer<Object> {
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Coder coder = (Coder) value;
		CoderBadgePanel cbp;
		if (coder == null) {
			cbp = new CoderBadgePanel();
		} else {
			cbp = new CoderBadgePanel(coder, 18, 5, 100);
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
		
		return cbp;
	}
}