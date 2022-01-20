package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.table.DefaultTableCellRenderer;

import model.Coder;

/**
 * A renderer for {@link model.Coder} objects in {@link JTable} tables.
 */
class CoderTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 5929937877823094103L;
	private int border = 1;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value == null) {
			return new JLabel("");
		} else {
			Coder coder = (Coder) value;
			CoderBadgePanel cbp = new CoderBadgePanel(coder, 13, border, 22);
			if (isSelected) {
				UIDefaults defaults = javax.swing.UIManager.getDefaults();
				Color bg = defaults.getColor("Table.selectionBackground");
				cbp.setBackground(bg);
			}
			return cbp;
		}
	}
	
	public void setBorder(int border) {
		this.border = border;
	}
}