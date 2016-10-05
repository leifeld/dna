package dna.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class ColorChooserEditor extends AbstractCellEditor implements TableCellEditor {

	private JButton delegate = new JButton();
	JPanel panel;
	Color savedColor;
	
	public ColorChooserEditor() {
		panel = new JPanel();
		panel.add(delegate);
		delegate.setPreferredSize(new Dimension(30, 6));
		
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				Color color = JColorChooser.showDialog(delegate, "Color Chooser", savedColor);
				ColorChooserEditor.this.changeColor(color);
			}
		};
		delegate.addActionListener(actionListener);
	}
	
	public Object getCellEditorValue() {
		return savedColor;
	}

	private void changeColor(Color color) {
		if (color != null) {
			savedColor = color;
			delegate.setBackground(color);
		}
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		changeColor((Color) value);
		//return delegate;
		//if (!isSelected) {
		//	panel.setBackground(Color.WHITE);
		//} else {
			panel.setBackground(UIManager.getColor("Tree.selectionBackground"));
		//}
		return panel;
	}
}