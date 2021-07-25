package dna.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;

import dna.dataStructures.StatementType;

public class StatementTypeComboBoxRenderer implements ListCellRenderer<Object> {
	
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value == null) {
			JPanel panel = new JPanel();
			JLabel label = new JLabel("");
			panel.add(label);
			return panel;
		} else {
			StatementType statementType = (StatementType) value;
			@SuppressWarnings("serial")
			JButton colorRectangle = (new JButton() {
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.setColor(statementType.getColor());
					g.fillRect(2, 2, 14, 14);
				}
			});
			colorRectangle.setPreferredSize(new Dimension(14, 14));
			colorRectangle.setEnabled(false);
			
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
			panel.add(colorRectangle);
			JLabel statementTypeLabel = new JLabel(statementType.getLabel());
			panel.add(statementTypeLabel);
			
			if (isSelected) {
				UIDefaults defaults = javax.swing.UIManager.getDefaults();
				Color bg = defaults.getColor("List.selectionBackground");
				panel.setBackground(bg);
			}
			
			return panel;
		}
	}
}
