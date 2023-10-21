package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import model.Entity;

/**
 * A renderer for JComboBox items that represent {@link model.Entity
 * Entity} objects. The value is shown as text. The color is shown as the
 * foreground color. If the attribute is not present in the database, it
 * gets a red background color. The renderer is used to display combo boxes
 * for short text variables in popup windows. The renderer only displays the
 * list items, not the contents of the text editor at the top of the list.
 */
class AttributeComboBoxRenderer implements ListCellRenderer<Object> {
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Entity a = (Entity) value;
		JLabel label = new JLabel(a.getValue());
		label.setForeground(a.getColor().toAWTColor());
		
		// list background
		Color selectedColor = javax.swing.UIManager.getColor("List.selectionBackground");
		Color notInDatabaseColor = new Color(255, 102, 102);
		// selected entry that is not in database: average of the previous two colors
		Color selectedAndNotInDatabaseColor = new Color((selectedColor.getRed() + notInDatabaseColor.getRed()) / 2, (selectedColor.getGreen() + notInDatabaseColor.getGreen()) / 2, (selectedColor.getBlue() + notInDatabaseColor.getBlue()) / 2);
		Color defaultColor = javax.swing.UIManager.getColor("List.background");
		if (isSelected == true && a.isInDatabase() == true) {
			label.setBackground(selectedColor);
		} else if (isSelected == true && a.isInDatabase() == false) {
			label.setBackground(selectedAndNotInDatabaseColor);
		} else if (isSelected == false && a.isInDatabase() == false) {
			label.setBackground(notInDatabaseColor);
		} else if (isSelected == false && a.isInDatabase() == true) {
			label.setBackground(defaultColor);
		}
		label.setOpaque(true);
		
		return label;
	}
}