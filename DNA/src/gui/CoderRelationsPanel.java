package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import model.Coder;
import model.CoderRelation;

/**
 * A panel with coder relations.
 */
public class CoderRelationsPanel extends JPanel {
	private static final long serialVersionUID = -5085399805870006250L;
	CoderRelationTableModel coderRelationTableModel;
	JTable coderRelationTable;
	
	/**
	 * Create a new coder relation panel.
	 */
	public CoderRelationsPanel() {
		this.setLayout(new BorderLayout());
		coderRelationTableModel = new CoderRelationTableModel();
		coderRelationTable = new JTable(coderRelationTableModel);
		CoderRelationTableCellRenderer coderRelationTableCellRenderer = new CoderRelationTableCellRenderer();
		coderRelationTable.setDefaultRenderer(Coder.class, coderRelationTableCellRenderer);
		coderRelationTable.setDefaultRenderer(boolean.class, coderRelationTableCellRenderer);
		coderRelationTable.getTableHeader().setReorderingAllowed(false);
		JScrollPane coderRelationScrollPane = new JScrollPane(coderRelationTable);
		coderRelationScrollPane.setPreferredSize(new Dimension(600, 300));
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(coderRelationScrollPane, BorderLayout.CENTER);
		
		EmptyBorder tablePanelBorder = new EmptyBorder(5, 5, 5, 5);
		tablePanel.setBorder(tablePanelBorder);
		this.add(tablePanel, BorderLayout.CENTER);
		
		CompoundBorder borderRelations = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Coder relations: Whose documents/statements can the coder view/edit?"));
		this.setBorder(borderRelations);
	}

	/**
	 * Get the coder relation table.
	 * 
	 * @return The coder relation JTable.
	 */
	JTable getTable() {
		return this.coderRelationTable;
	}
	
	/**
	 * Get the coder relation table model.
	 * 
	 * @return The coder relation table model.
	 */
	CoderRelationTableModel getModel() {
		return this.coderRelationTableModel;
	}

	/**
	 * A table cell renderer that can display coder relations.
	 */
	private class CoderRelationTableCellRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = -4743373298435293984L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value == null) {
				return null;
			} else if (table.convertColumnIndexToModel(column) == 0) {
				return new CoderBadgePanel((Coder) value, 16, 0, 13);
        	} else if ((boolean) value == true) {
				ImageIcon eyeIconGreen = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-eye-green.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
        		return new JLabel(eyeIconGreen);
        	} else {
				ImageIcon eyeIconRed = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-eye-off-red.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
        		return new JLabel(eyeIconRed);
        	}
		}
	}
	
	/**
	 * A table model for coder relations.
	 */
	class CoderRelationTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 6531584132561341365L;
		ArrayList<CoderRelation> coderRelations;

		/**
		 * Create a new coder relation table model.
		 */
		CoderRelationTableModel() {
			this.coderRelations = new ArrayList<CoderRelation>();
		}
		
		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return this.coderRelations.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (row > -1 && row < coderRelations.size()) {
				if (col == 0) {
					return new Coder(coderRelations.get(row).getTargetCoderId(),
							coderRelations.get(row).getTargetCoderName(),
							coderRelations.get(row).getTargetCoderColor());
				} else if (col == 1) {
					return coderRelations.get(row).isViewDocuments();
				} else if (col == 2) {
					return coderRelations.get(row).isEditDocuments();
				} else if (col == 3) {
					return coderRelations.get(row).isViewStatements();
				} else if (col == 4) {
					return coderRelations.get(row).isEditStatements();
				}
			}
			return null;
		}
		
		/**
		 * Set the value for an arbitrary table cell.
		 * 
		 * @param object The object to save for the cell.
		 * @param row    The table row.
		 * @param col    The table column.
		 */
		public void setValueAt(Object object, int row, int col) {
			if (row > -1 && row < coderRelations.size()) {
				if (col == 0) {
					coderRelations.get(row).setTargetCoderId(((Coder) object).getId());
					coderRelations.get(row).setTargetCoderName(((Coder) object).getName());
					coderRelations.get(row).setTargetCoderColor(((Coder) object).getColor());
				} else if (col == 1) {
					coderRelations.get(row).setViewDocuments((boolean) object); 
				} else if (col == 2) {
					coderRelations.get(row).setEditDocuments((boolean) object); 
				} else if (col == 3) {
					coderRelations.get(row).setViewStatements((boolean) object); 
				} else if (col == 4) {
					coderRelations.get(row).setEditStatements((boolean) object); 
				}
			}
			fireTableCellUpdated(row, col);
		}
		
		/**
		 * Is the cell editable?
		 * 
		 * @param row The table row.
		 * @param col The table column.
		 */
		public boolean isCellEditable(int row, int col) {
			if (row > -1 && row < coderRelations.size() && col > 0 && col < 5) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Return the name of a column.
		 * 
		 * @param column The column index of the table for which the name should be returned, starting at 0.
		 */
		public String getColumnName(int column) {
			switch(column) {
				case 0: return "Other coder";
				case 1: return "View documents";
				case 2: return "Edit documents";
				case 3: return "View statements";
				case 4: return "Edit statements";
				default: return null;
			}
		}

		/**
		 * Return a row of the table based on the internal array list of coder
		 * relations.
		 * 
		 * @param row Index of the {@link model.CoderRelation CoderRelation}
		 *   object in the array list.
		 * @return The {@link model.CoderRelation CoderRelation} object.
		 */
		public CoderRelation getRow(int row) {
			return coderRelations.get(row);
		}

		/**
		 * Which type of object (i.e., class) shall be shown in the columns?
		 * 
		 * @param col The column index of the table for which the class type
		 *   should be returned, starting at 0.
		 */
		public Class<?> getColumnClass(int col) {
			switch (col) {
				case 0: return Coder.class;
				case 1: return boolean.class;
				case 2: return boolean.class;
				case 3: return boolean.class;
				case 4: return boolean.class;
				default: return null;
			}
		}
		
		/**
		 * Remove all coder relation objects from the model.
		 */
		public void clear() {
			coderRelations.clear();
			fireTableDataChanged();
		}
		
		/**
		 * Add a new coder relation to the model.
		 * 
		 * @param cr A new {@link model.CoderRelation CoderRelation} object.
		 */
		public void addRow(CoderRelation cr) {
			coderRelations.add(cr);
			fireTableRowsInserted(coderRelations.size() - 1, coderRelations.size() - 1);
		}
	}
}