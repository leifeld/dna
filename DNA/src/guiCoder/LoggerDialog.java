package guiCoder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import dna.LogEvent;
import dna.Logger;

/**
 * A dialog window with a table, showing log messages, warnings, and errors that
 * have accumulated in DNA during usage.
 */
public class LoggerDialog extends JDialog {
	private static final long serialVersionUID = -8365310356679647056L;

	/**
	 * Create a new instance of the logger dialog window.
	 */
	public LoggerDialog() {
		this.setModal(true);
		this.setTitle("Log entries");
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		JTable table = new JTable(Dna.dna.logger);
		TableRowSorter<Logger> sorter = new TableRowSorter<Logger>(Dna.dna.logger);
		table.setRowSorter(sorter);
		table.getColumnModel().getColumn(0).setPreferredWidth(105);
		table.getColumnModel().getColumn(1).setPreferredWidth(400);
		table.getColumnModel().getColumn(2).setPreferredWidth(40);
		table.getColumnModel().getColumn(3).setPreferredWidth(40);
		table.getColumnModel().getColumn(4).setPreferredWidth(15);
		table.setDefaultRenderer(String.class, new LogTableCellRenderer());
		table.setDefaultRenderer(LocalDateTime.class, new LogTableCellRenderer());
		table.setDefaultRenderer(Integer.class, new LogTableCellRenderer());
		JScrollPane scroller = new JScrollPane(table);
		scroller.setViewportView(table);
		scroller.setPreferredSize(new Dimension(800, 600));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mainPanel.add(scroller, BorderLayout.CENTER);
		
		JButton button = new JButton("Test");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// nothing to do
			}
		});
		mainPanel.add(button, BorderLayout.SOUTH);

		this.add(mainPanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * Renderer for the table cells of the log table. Displays the contents of
	 * the table in appropriate ways.
	 */
	private class LogTableCellRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = 5607731678747286839L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
        	DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        	Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        	Logger model = (Logger) table.getModel();
	        LogEvent logEntry = model.getRow(table.convertRowIndexToModel(row));
	        
			Color priorityColor = javax.swing.UIManager.getColor("Table.background");
	        Color selectedColor = javax.swing.UIManager.getColor("Table.dropCellBackground");
	        if (logEntry.getPriority() == 1) {
	        	// priorityColor = new Color(130, 255, 130);
	        } else if (logEntry.getPriority() == 2) {
	        	priorityColor = new Color(255, 255, 130);
	        } else if (logEntry.getPriority() == 3) {
	        	priorityColor = new Color(255, 130, 130);
	        }
	        
        	if (column == 0) {
    			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss.SSS");
        		String s = ((LocalDateTime) value).format(formatter);
        		c = renderer.getTableCellRendererComponent(table, s, isSelected, hasFocus, row, column);
        	} else if (column == 4) {
        		if ((int) value == -1) {
        			value = null;
        		}
        	}
        	if (isSelected == true) {
	        	c.setBackground(selectedColor);
	        } else {
	        	c.setBackground(priorityColor);
	        }
        	return c;
		}
	}
}