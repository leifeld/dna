package dna.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.Box;
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
			return new JLabel("");
		} else {
			StatementType statementType = (StatementType) value;
			JPanel panel = new JPanel(new BorderLayout());
			JButton colorRectangle = (new JButton() {
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.setColor(statementType.getColor());
					g.fillRect(2, 2, 14, 14);
				}
			});
			colorRectangle.setPreferredSize(new Dimension(18, 18));
			colorRectangle.setEnabled(false);
			//JButton colorRectangle = new JButton();
			//colorRectangle.setPreferredSize(new Dimension(18, 18));
			//colorRectangle.setBackground(statementType.getColor());
			//colorRectangle.setForeground(statementType.getColor());
			//colorRectangle.setOpaque(true);
			//colorRectangle.setBorderPainted(false);
			
			JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			namePanel.add(colorRectangle);
			namePanel.add(Box.createRigidArea(new Dimension(5,5)));
			JLabel statementTypeLabel = new JLabel(statementType.getLabel());
			namePanel.add(statementTypeLabel);
			
			if (isSelected) {
				UIDefaults defaults = javax.swing.UIManager.getDefaults();
				Color bg = defaults.getColor("List.selectionBackground");
				//Color fg = defaults.getColor("List.selectionForeground");
				panel.setBackground(bg);
				namePanel.setBackground(bg);
			}
			
			panel.add(namePanel, BorderLayout.NORTH);
			return panel;
		}
	}
}
