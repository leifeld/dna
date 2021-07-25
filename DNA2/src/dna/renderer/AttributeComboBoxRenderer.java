package dna.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import dna.dataStructures.AttributeVector;

public class AttributeComboBoxRenderer implements ListCellRenderer<Object> {
	
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value == null) {
			return new JLabel("");
		} else {
			AttributeVector a = (AttributeVector) value;
			JLabel label = new JLabel(a.getValue());
			label.setForeground(a.getColor());
			if (a.isInDataset() == false) {
				label.setBackground(new Color(255, 102, 102));
				label.setOpaque(true);
			}
			return label;
		}
	}
}
