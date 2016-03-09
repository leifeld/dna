package dna;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import dna.dataStructures.Coder;

public class LeftPanel extends JPanel {
	JPanel coderToggleButtonPanel;
	
	public LeftPanel() {
		//this.add(new JLabel("test"));
		JXTaskPaneContainer tpc = new JXTaskPaneContainer();
		//this.setColumnHeaderView(tpc);
		tpc.setBackground(this.getBackground());
		this.add(tpc);
		
		
		// coder visibility panel
		JXTaskPane coderVisibilityTaskPane = new JXTaskPane();
		ImageIcon groupIcon = new ImageIcon(getClass().getResource("/icons/group.png"));
		coderVisibilityTaskPane.setName("Coder");
		coderVisibilityTaskPane.setTitle("Coder");
		coderVisibilityTaskPane.setIcon(groupIcon);
		((Container)tpc).add(coderVisibilityTaskPane);
		//coderToggleButtonPanel = new CoderToggleButtonPanel();
		JTable table = new JTable(new ToggleTableModel());
		coderVisibilityTaskPane.add(table);
		
		// TODO: use a list or table of JToggleButtons rather than a JPanel to list the coders
		
		//coderVisibilityTaskPane.add(coderToggleButtonPanel);

	}

	class ToggleTableModel implements TableModel {
		private Vector<TableModelListener> listeners = 	new Vector<TableModelListener>();
		
		public int getRowCount() {
			return Dna.data.getCoders().size();
		}
		
		public int getColumnCount() {
			return 1;
		}
		
		public String getColumnName(int columnIndex) {
			return "Coder";
		}
		
		public Class<?> getColumnClass(int columnIndex) {
			return Coder.class;
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			return Dna.data.getCoders().get(rowIndex);
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Dna.data.getCoders().set(rowIndex, (Coder) aValue);
		}
		
		public void addTableModelListener(TableModelListener l) {
			listeners.add( l );
		}
		
		public void removeTableModelListener(TableModelListener l) {
			listeners.remove( l );
		}
	}
	
	public class ToggleCellRenderer implements TableCellRenderer {

		private TableCellRenderer toggleCellRenderer;

		public ToggleCellRenderer(TableCellRenderer cellRenderer) {
			super();
			this.toggleCellRenderer = cellRenderer;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component rendererComponent = toggleCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			/*
	        if (hasFocus)
	            rendererComponent.setBackground(Color.RED);
	        else
	            rendererComponent.setBackground(row % 2 == 0 ? null : Color.LIGHT_GRAY );
			 */

			Coder coder = (Coder) value;

			JToggleButton button = new JToggleButton(coder.getName());
			button.setBackground(coder.getColor());
			button.setForeground(coder.getColor());
			button.setSelected(false);
			button.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					if (ev.getStateChange() == ItemEvent.SELECTED){
						// TODO: add to CODERRELATIONS, then paint only statements permitted by CODERRELATIONS
					} else if(ev.getStateChange() == ItemEvent.DESELECTED){
						// TODO
					}
				}
			});
			JPanel panel = new JPanel();
			panel.add(button);
			//rendererComponent.add(panel);
			return panel;
			//return rendererComponent;
		}

	}
	
	
	/*
	class CoderToggleRenderer extends JLabel implements ListCellRenderer {
		//private static final long serialVersionUID = 1L;
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Coder coder = (Coder) value;
			
			JToggleButton b = new JToggleButton(coder.getName());
			b.setBackground(coder.getColor());
			b.setSelected(false);
			b.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ev) {
					if (ev.getStateChange() == ItemEvent.SELECTED){
						// TODO: add to CODERRELATIONS, then paint only statements permitted by CODERRELATIONS
					} else if(ev.getStateChange() == ItemEvent.DESELECTED){
						// TODO
					}
				}
			});
			this.add(b);
			return this;
		}
	}
	*/
	
	/*
	public void updateToggleButtons() {
		this.coderToggleButtonPanel = new CoderToggleButtonPanel();
	}

	public class CoderToggleButtonPanel extends JPanel {
		public CoderToggleButtonPanel() {
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			for (int i = 0; i < Dna.data.getCoders().size(); i++) {
				JToggleButton button = new JToggleButton(Dna.data.getCoders().get(i).getName());
				button.setBackground(Dna.data.getCoders().get(i).getColor());
				button.setEnabled(true);
				button.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ev) {
						if (ev.getStateChange() == ItemEvent.SELECTED){
							// TODO: add to CODERRELATIONS, then paint only statements permitted by CODERRELATIONS
						} else if(ev.getStateChange() == ItemEvent.DESELECTED){
							// TODO
						}
					}
				});
				this.add(button);
			}
		}
	}
	*/
}
