package dna.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import dna.Dna;
import dna.dataStructures.Coder;
import dna.dataStructures.CoderRelation;

public class CoderRelationCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		int modelRow = table.convertRowIndexToModel(row);
		CoderRelation cr = ((CoderRelationTableModel)table.getModel()).get(modelRow);
		if (column == 0) {
			int otherCoderId = cr.getOtherCoder();
			Coder otherCoder = Dna.data.getCoderById(otherCoderId);
			Color otherColor = otherCoder.getColor();
			@SuppressWarnings("serial")
			JButton colorButton = (new JButton() {
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					g.setColor(otherColor);
					g.fillRect(2, 2, 12, 12);
				}
			});
			colorButton.setPreferredSize(new Dimension(12, 12));
			colorButton.setEnabled(false);
			panel.add(colorButton);
			String name = otherCoder.getName();
			int nameLength = name.length();
			if (nameLength > 14) {
				nameLength = 14 - 3;
				name = name.substring(0,  nameLength);
				name = name + "...";
			}
			JLabel otherName = new JLabel(name);
			panel.add(otherName);
			otherName.setEnabled(false);
		} else if (column == 1) {
			boolean viewStatements = cr.isViewStatements();
			JCheckBox box = new JCheckBox();
			box.setSelected(viewStatements);
			if (Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions().get("viewOthersStatements") == false) {
				box.setEnabled(false);
			}
			return box;
		} else if (column == 2) {
			boolean editStatements = cr.isEditStatements();
			JCheckBox box = new JCheckBox();
			box.setSelected(editStatements);
			if (Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions().get("editOthersStatements") == false) {
				box.setEnabled(false);
			}
			return box;
		} else if (column == 3) {
			boolean viewDocuments = cr.isViewDocuments();
			JCheckBox box = new JCheckBox();
			box.setSelected(viewDocuments);
			if (Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions().get("viewOthersDocuments") == false) {
				box.setEnabled(false);
			}
			return box;
		} else if (column == 4) {
			boolean editDocuments = cr.isEditDocuments();
			JCheckBox box = new JCheckBox();
			box.setSelected(editDocuments);
			if (Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions().get("editOthersDocuments") == false) {
				box.setEnabled(false);
			}
			return box;
		}
		return panel;
	}
}