package dna.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import dna.Dna;

public class AttributeCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		int modelRow = table.convertRowIndexToModel(row);
		Color lightred = new Color(255, 102, 102);
		Color darkred = new Color(175, 112, 112);
		if (column == 1) {
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			if (Dna.data.getAttributes().get(modelRow).isInDataset() == true) {
				if (!isSelected) {
					panel.setBackground(Color.WHITE);
				} else {
					panel.setBackground(UIManager.getColor("Tree.selectionBackground"));
				}
			} else {
				if (!isSelected) {
					panel.setBackground(lightred);
				} else {
					panel.setBackground(darkred);
				}
			}
			Color color = Dna.data.getAttributes().get(modelRow).getColor();
			@SuppressWarnings("serial")
			JButton colorButton = (new JButton() {
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.setColor(color);
					g.fillRect(0, 0, 30, 6);
				}
			});
			colorButton.setPreferredSize(new Dimension(30, 6));
			colorButton.setEnabled(false);
			panel.add(colorButton);
			return panel;
		} else {
			Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (Dna.data.getAttributes().get(modelRow).isInDataset() == true) {
				if (!isSelected) {
					cellComponent.setBackground(Color.WHITE);
				} else {
					cellComponent.setBackground(UIManager.getColor("Tree.selectionBackground"));
				}
			} else {
				if (!isSelected) {
					cellComponent.setBackground(lightred);
				} else {
					cellComponent.setBackground(darkred);
				}
			}
			return cellComponent;
		}
	}
}