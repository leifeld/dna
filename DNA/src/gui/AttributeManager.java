package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTextField;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import model.Entity;
import model.StatementType;
import model.Value;

public class AttributeManager extends JDialog {
	private static final long serialVersionUID = 6180793159551336995L;
	private JTable attributeTable;
	private AttributeTableModel model;
	private TableRowSorter<AttributeTableModel> sorter;
	private JScrollPane scrollPane;
	private JPanel tablePanel;
	private JComboBox<StatementType> statementTypeBox;
	private JComboBox<Value> variableBox;
	private ArrayList<AttributeTableRefreshWorker> workers;
	private JButton deleteButton;
	
	public AttributeManager() {
		this.setModal(true);
		this.setTitle("Attribute manager");
		this.setLayout(new BorderLayout());
		
		workers = new ArrayList<AttributeTableRefreshWorker>();
		
		// create empty table panel, which will be overwritten after setting the other components
		this.tablePanel = new JPanel();
		this.attributeTable = new JTable();
		this.tablePanel.add(attributeTable);
		this.add(tablePanel, BorderLayout.CENTER);
		
		// combo boxes
		variableBox = new JComboBox<Value>();
		ArrayList<StatementType> statementTypes = Dna.sql.getStatementTypes();
		statementTypeBox = new JComboBox<StatementType>();
		for (int i = 0; i < statementTypes.size(); i++) {
			ArrayList<Value> values = statementTypes.get(i).getVariables();
			for (int j = 0; j < values.size(); j++) {
				if (values.get(j).getDataType().equals("short text")) {
					statementTypeBox.addItem(statementTypes.get(i));
					break;
				}
			}
		}
		statementTypeBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateVariableBox();
			}
		});
		StatementTypeComboBoxRenderer statementTypeRenderer = new StatementTypeComboBoxRenderer();
		statementTypeBox.setRenderer(statementTypeRenderer);
		variableBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int variableId = ((Value) variableBox.getSelectedItem()).getVariableId();
				createNewTable(variableId);
			}
		});
		VariableComboBoxRenderer variableRenderer = new VariableComboBoxRenderer();
		variableBox.setRenderer(variableRenderer);
		JPanel boxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		boxPanel.add(statementTypeBox);
		boxPanel.add(variableBox);
		JPanel upperPanel = new JPanel(new BorderLayout());
		upperPanel.add(boxPanel, BorderLayout.WEST);
		
		// button panel at the bottom
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JXTextField newField = new JXTextField("New entity...");
		newField.setColumns(20);
		newField.setPreferredSize(new Dimension(newField.getPreferredSize().width, newField.getPreferredSize().height + 4));
		buttonPanel.add(newField);
		
		ImageIcon addIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-circle-plus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		JButton addButton = new JButton("Add entity", addIcon);
		addButton.setToolTipText("Add a new entity to the list. It can then be coded in a statement popup window.");
		addButton.setPreferredSize(new Dimension(addButton.getPreferredSize().width, newField.getPreferredSize().height));
		addButton.setEnabled(false);
		buttonPanel.add(addButton);
		
		newField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkButton();
			}
			public void checkButton() {
				String newValue = newField.getText();
				if (newValue.matches("^\\s*$") || model.containsValue(newValue)) {
					addButton.setEnabled(false);
				} else {
					addButton.setEnabled(true);
				}
			}
		});
		
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HashMap<String, String> map = new HashMap<String, String>();
				for (int i = 0; i < model.getAttributeVariables().size(); i++) {
					map.put(model.getAttributeVariables().get(i), "");
				}
				Entity entity = new Entity(-1, newField.getText(), Color.BLACK, -1, false, map);
				int variableId = ((Value) variableBox.getSelectedItem()).getVariableId();
				Dna.sql.addEntity(entity, variableId);
				refreshTable(variableId);
				newField.setText("");
			}
		});
		
		ImageIcon deleteIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-circle-minus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		deleteButton = new JButton("Delete entry", deleteIcon);
		deleteButton.setToolTipText("Delete the selected unused entities (the selected rows highlighted in red).");
		deleteButton.setPreferredSize(new Dimension(deleteButton.getPreferredSize().width, newField.getPreferredSize().height));
		deleteButton.setEnabled(false);
		buttonPanel.add(deleteButton);
		
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = attributeTable.getSelectedRows();
				for (int i = 0; i < selectedRows.length; i++) {
					selectedRows[i] = model.getRow(attributeTable.convertRowIndexToModel(selectedRows[i])).getId();
				}
				int dialog = JOptionPane.showConfirmDialog(null, "Delete " + selectedRows.length + " unused entities from the attribute manager?", "Confirmation", JOptionPane.YES_NO_OPTION);
				if (dialog == 0) {
					Dna.sql.deleteEntities(selectedRows);
					refreshTable(((Value) variableBox.getSelectedItem()).getVariableId());
				}
			}
		});

		ImageIcon cleanUpIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-circle-0.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		JButton cleanUpButton = new JButton("Clean up", cleanUpIcon);
		cleanUpButton.setToolTipText("Delete all unused entities (the rows highlighted in red) at once.");
		cleanUpButton.setPreferredSize(new Dimension(cleanUpButton.getPreferredSize().width, newField.getPreferredSize().height));
		buttonPanel.add(cleanUpButton);

		cleanUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<Integer> entityIds = new ArrayList<Integer>();
				for (int i = 0; i < model.getRowCount(); i++) {
					if (!model.getRow(i).isInDatabase()) {
						entityIds.add(model.getRow(i).getId());
					}
				}
				if (entityIds.size() > 0) {
					int[] ids = new int[entityIds.size()];
					for (int i = 0; i < entityIds.size(); i++) {
						ids[i] = entityIds.get(i);
					}
					int dialog = JOptionPane.showConfirmDialog(null, "Delete " + ids.length + " unused entities from the attribute manager?", "Confirmation", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						Dna.sql.deleteEntities(ids);
						refreshTable(((Value) variableBox.getSelectedItem()).getVariableId());
					}
				}
			}
		});

		this.add(buttonPanel, BorderLayout.SOUTH);
		
		// close button, upper right corner
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ImageIcon closeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		JButton closeButton = new JButton("Close", closeIcon);
		closeButton.setToolTipText("Close the attribute manager.");
		closeButton.setPreferredSize(new Dimension(closeButton.getPreferredSize().width, newField.getPreferredSize().height));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (workers.size() > 0) {
					for (int i = workers.size() - 1; i >= 0; i--) {
						workers.get(i).cancel(true);
						workers.remove(i);
					}
				}
				dispose();
			}
		});
		closePanel.add(closeButton);
		upperPanel.add(closePanel, BorderLayout.EAST);
		
		this.add(upperPanel, BorderLayout.NORTH);
		
		updateVariableBox(); // populate variable box and create table initially
		
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	private void updateVariableBox() {
		StatementType st = (StatementType) statementTypeBox.getSelectedItem();
		ArrayList<Value> values = st.getVariables();
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i).getDataType().equals("short text")) {
				variableBox.addItem(values.get(i));
			}
		}
	}
	
	private void createNewTable(int variableId) {
		// remove the table panel and update GUI
		this.remove(tablePanel);
		this.revalidate();
		
		// create new panel with table
		tablePanel = new JPanel();
		model = new AttributeTableModel(Dna.sql.getAttributeVariables(variableId));
		attributeTable = new JTable(model);
		
		// editors and renderers
		EntityTableCellRenderer tableRenderer = new EntityTableCellRenderer();
		attributeTable.setDefaultRenderer(String.class, tableRenderer);
		attributeTable.setDefaultRenderer(Color.class, tableRenderer);
		attributeTable.setDefaultRenderer(Integer.class, tableRenderer);
		ColorChooserEditor cce = new ColorChooserEditor();
		attributeTable.getColumnModel().getColumn(2).setCellEditor(cce);
		
		// appearance of the table
		attributeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		sorter = new TableRowSorter<AttributeTableModel>(model);
		attributeTable.setRowSorter(sorter);
		scrollPane = new JScrollPane(attributeTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(attributeTable);
		scrollPane.setPreferredSize(new Dimension(1000, 700));
		for (int i = 0; i < (model.getAttributeVariables().size() + 3); i++) {
			int viewColumn = attributeTable.convertColumnIndexToView(i);
			if (i == 0 || i == 2) {
				attributeTable.getColumnModel().getColumn(viewColumn).setPreferredWidth(50);
			} else {
				attributeTable.getColumnModel().getColumn(viewColumn).setPreferredWidth(900 / (model.getAttributeVariables().size() + 1));
			}
		}
		tablePanel.add(scrollPane);
		
		// populate table using swing worker
		refreshTable(variableId);
		
		// check if selected rows are in dataset and disable delete button if necessary
		attributeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				boolean noneInDatabase = true;
				int[] selectedRows = attributeTable.getSelectedRows();
				for (int i = 0; i < selectedRows.length; i++) {
					if (model.getRow(attributeTable.convertRowIndexToModel(selectedRows[i])).isInDatabase()) {
						noneInDatabase = false;
						break;
					}
				}
				if (noneInDatabase == true) {
					deleteButton.setEnabled(true);
				} else {
					deleteButton.setEnabled(false);
				}
			}
		});
		
		// add the new table panel to GUI and update
		this.add(tablePanel, BorderLayout.CENTER);
		this.revalidate();
		this.setLocationRelativeTo(null);
		this.pack();
	}
	
	private void refreshTable(int variableId) {
		if (workers.size() > 0) {
			for (int i = workers.size() - 1; i >= 0; i--) {
				workers.get(i).cancel(true);
				workers.remove(i);
			}
		}
		AttributeTableRefreshWorker worker = new AttributeTableRefreshWorker(variableId);
		workers.add(worker);
		worker.execute();
	}
	
	private class AttributeTableRefreshWorker extends SwingWorker<List<Entity>, Entity> {
		int variableId;
		
		private AttributeTableRefreshWorker(int variableId) {
			this.variableId = variableId;
			model.clearRows();
		}
		
		@Override
		protected List<Entity> doInBackground() {
			boolean inDatabase;
			try (Connection conn = Dna.sql.getDataSource().getConnection();
					PreparedStatement s1 = conn.prepareStatement("SELECT ID, Value, Red, Green, Blue, ChildOf FROM ENTITIES WHERE VariableId = ?;");
					PreparedStatement s2 = conn.prepareStatement("SELECT COUNT(ID) FROM DATASHORTTEXT WHERE VariableId = ? AND Entity = ?;");
					PreparedStatement s3 = conn.prepareStatement("SELECT AttributeVariable, AttributeValue FROM ATTRIBUTEVALUES AS AVAL INNER JOIN ATTRIBUTEVARIABLES AS AVAR ON AVAL.AttributeVariableId = AVAR.ID WHERE EntityId = ?;")) {
				ResultSet r1, r2;
				Color color;
				int entityId;
				HashMap<String, String> map;
				s1.setInt(1, variableId);
				s2.setInt(1, variableId);
				r1 = s1.executeQuery();
	        	while (r1.next()) {
	            	color = new Color(r1.getInt("Red"), r1.getInt("Green"), r1.getInt("Blue"));
	            	s2.setInt(2, r1.getInt("ID"));
	            	r2 = s2.executeQuery();
	            	if (r2.getInt(1) > 0) {
	            		inDatabase = true;
	            	} else {
	            		inDatabase = false;
	            	}
	            	
	            	entityId = r1.getInt("ID");
	            	map = new HashMap<String, String>();
	            	s3.setInt(1, entityId);
	            	r2 = s3.executeQuery();
	            	while (r2.next()) {
	            		map.put(r2.getString("AttributeVariable"), r2.getString("AttributeValue"));
	            	}
	            	publish(new Entity(entityId, r1.getString("Value"), color, r1.getInt("ChildOf"), inDatabase, map));
	            }
			} catch (SQLException e1) {
	        	LogEvent e = new LogEvent(Logger.WARNING,
	        			"[SQL] Entities for Variable " + variableId + " could not be retrieved.",
	        			"Entities for Variable " + variableId + " could not be retrieved. Check if the database is still there and/or if the connection has been interrupted, then try again.",
	        			e1);
	        	Dna.logger.log(e);
			}
			return null;
		}
        
        @Override
        protected void process(List<Entity> chunks) {
        	model.addRows(chunks); // transfer a batch of rows to the table model
        }

        @Override
        protected void done() {
        	// nothing to do
        }
    }
	
	class AttributeTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 7576473809983501603L;
		private ArrayList<String> attributeVariables;
		private ArrayList<Entity> rows;
		
		public AttributeTableModel(ArrayList<String> attributeVariables) {
			this.attributeVariables = attributeVariables;
			this.rows = new ArrayList<Entity>();
		}
		
		@Override
		public int getColumnCount() {
			return (3 + this.attributeVariables.size());
		}

		@Override
		public int getRowCount() {
			return this.rows.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (this.rows.size() == 0 || row > rows.size() - 1) {
				return null;
			}
			if (col == 0) {
				return rows.get(row).getId();
			} else if (col == 1) {
				return rows.get(row).getValue();
			} else if (col == 2) {
				return rows.get(row).getColor();
			} else if (col > (this.attributeVariables.size() + 2)) {
				return null;
			} else {
				return rows.get(row).getAttributeValues().get(this.attributeVariables.get(col - 3));
			}
		}
		
		public void setValueAt(Object aValue, int row, int col) {
			if (col > (this.attributeVariables.size() + 2) || row > rows.size() - 1) {
				// do nothing because row or col outside of table; not sure if this condition is ever reached
			} else if (col == 0) { // ID
				// do nothing because the ID cannot be changed
			} else if (col == 1) { // value
				if (getValueAt(row, col).equals("")) {
					// empty entities must not be changed because otherwise empty new statements cannot be created anymore
				} else {
					try {
						Dna.sql.setEntityValue(this.rows.get(row).getId(), (String) aValue);
						this.rows.get(row).setValue((String) aValue);
					} catch (SQLException ex) {
			        	LogEvent l = new LogEvent(Logger.ERROR,
			        			"[SQL] Entity value could not be updated in the database.",
			        			"The value for Entity " + this.rows.get(row).getId() + " could not be updated in the database.",
			        			ex);
			        	Dna.logger.log(l);
					}
				}
			} else if (col == 2) { // color
				try {
					Dna.sql.setEntityColor(this.rows.get(row).getId(), (Color) aValue);
					this.rows.get(row).setColor((Color) aValue);
				} catch (SQLException ex) {
		        	LogEvent l = new LogEvent(Logger.ERROR,
		        			"[SQL] Entity color could not be updated in the database.",
		        			"The color for Entity " + this.rows.get(row).getId() + " could not be updated in the database.",
		        			ex);
		        	Dna.logger.log(l);
				}
			} else { // an attribute value
				String attributeVariable = this.attributeVariables.get(col - 3);
				try {
					Dna.sql.setAttributeValue(this.rows.get(row).getId(), attributeVariable, (String) aValue);
					this.rows.get(row).getAttributeValues().put(attributeVariable, (String) aValue);
				} catch (SQLException ex) {
		        	LogEvent l = new LogEvent(Logger.ERROR,
		        			"[SQL] Attribute could not be updated in the database.",
		        			"The value for attribute \"" + attributeVariable + "\" could not be updated in the database for Entity " + this.rows.get(row).getId() + ".",
		        			ex);
		        	Dna.logger.log(l);
				}
			}
		}
		
		public String getColumnName(int col) {
			if (col == 0) {
				return "ID";
			} else if (col == 1) {
				return "Value";
			} else if (col == 2) {
				return "Color";
			} else if (col > (this.attributeVariables.size() + 2)) {
				return null;
			} else {
				return this.attributeVariables.get(col - 3);
			}
		}
		
		public Class<?> getColumnClass(int col) {
			if (this.rows.size() == 0) {
				return null;
			}
			if (col == 0) {
				return Integer.class;
			} else if (col == 1) {
				return String.class;
			} else if (col == 2) {
				return Color.class;
			} else if (col > (this.attributeVariables.size() + 2)) {
				return null;
			} else {
				return String.class;
			}
		}
		
		public boolean isCellEditable(int row, int col) {
			if (col == 0) {
				return false;
			} else {
				return true;
			}
		}
		
		public boolean containsValue(String value) {
			for (int i = 0; i < rows.size(); i++) {
				if (rows.get(i).getValue().equals(value)) {
					return true;
				}
			}
			return false;
		}
		
		public Entity getRow(int row) {
			return this.rows.get(row);
		}
		
		public void addRows(List<Entity> entities) {
			for (int i = 0; i < entities.size(); i++) {
				rows.add(entities.get(i));
			}
			sort();
		}
		
		public ArrayList<String> getAttributeVariables() {
			return this.attributeVariables;
		}
		
		public void setAttributeVariables(ArrayList<String> attributeVariables) {
			this.attributeVariables = attributeVariables;
		}
		
		void clearRows() {
			this.rows.clear();
			try {
				fireTableDataChanged();
			} catch (NullPointerException npe) {
				// ignore
			}
		}

		void sort() {
			Collections.sort(this.rows);
			fireTableDataChanged();
		}
	}
	
	private class EntityTableCellRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = 5436254013893853394L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
        	DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        	Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        	Entity entity = model.getRow(attributeTable.convertRowIndexToModel(row));

        	Color defaultColor = javax.swing.UIManager.getColor("Table.background");
	        Color selectedColor = javax.swing.UIManager.getColor("Table.dropCellBackground");
	        Color notInDatabaseColor = new Color(255, 102, 102);
			Color selectedAndNotInDatabaseColor = new Color((selectedColor.getRed() + notInDatabaseColor.getRed()) / 2, (selectedColor.getGreen() + notInDatabaseColor.getGreen()) / 2, (selectedColor.getBlue() + notInDatabaseColor.getBlue()) / 2);
			
	        if (!entity.isInDatabase() && isSelected) {
	        	c.setBackground(selectedAndNotInDatabaseColor);
	        } else if (isSelected) {
	        	c.setBackground(selectedColor);
	        } else if (!entity.isInDatabase()) {
	        	c.setBackground(notInDatabaseColor);
	        } else {
	        	c.setBackground(defaultColor);
	        }
	        
        	if (attributeTable.convertColumnIndexToModel(column) == 2) {
        		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    			JButton colorButton = (new JButton() {
					private static final long serialVersionUID = 1648028274961429514L;
					public void paintComponent(Graphics g) {
    					super.paintComponent(g);
    					g.setColor(entity.getColor());
    					g.fillRect(0, 0, 30, 8);
    				}
    			});
    			colorButton.setPreferredSize(new Dimension(30, 8));
    			colorButton.setEnabled(false);
    			panel.add(colorButton);
    	        if (!entity.isInDatabase() && isSelected) {
    	        	panel.setBackground(selectedAndNotInDatabaseColor);
    	        } else if (isSelected) {
    	        	panel.setBackground(selectedColor);
    	        } else if (!entity.isInDatabase()) {
    	        	panel.setBackground(notInDatabaseColor);
    	        } else {
    	        	panel.setBackground(defaultColor);
    	        }
    			return panel;
        	} else {
    	        if (!entity.isInDatabase() && isSelected) {
    	        	c.setBackground(selectedAndNotInDatabaseColor);
    	        } else if (isSelected) {
    	        	c.setBackground(selectedColor);
    	        } else if (!entity.isInDatabase()) {
    	        	c.setBackground(notInDatabaseColor);
    	        } else {
    	        	c.setBackground(defaultColor);
    	        }
            	return c;
        	}
		}
	}
	
	public class ColorChooserEditor extends AbstractCellEditor implements TableCellEditor {
		private static final long serialVersionUID = 2145176699224057432L;
		private JButton delegate = new JButton();
		JPanel panel;
		Color savedColor;
		
		public ColorChooserEditor() {
			panel = new JPanel();
			panel.add(delegate);
			delegate.setPreferredSize(new Dimension(30, 8));
			
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
			panel.setBackground(UIManager.getColor("Table.selectionBackground"));
			return panel;
		}
	}
	
	private class StatementTypeComboBoxRenderer implements ListCellRenderer<StatementType> {
		@Override
		public Component getListCellRendererComponent(JList<? extends StatementType> list, StatementType statementType, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = new JLabel(statementType.getLabel());
			label.setPreferredSize(new Dimension(300, label.getPreferredSize().height));
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(label, BorderLayout.NORTH);
			if (isSelected) {
				panel.setBackground(UIManager.getColor("List.selectionBackground"));
			}
			return panel;
		}
	}

	private class VariableComboBoxRenderer implements ListCellRenderer<Value> {
		@Override
		public Component getListCellRendererComponent(JList<? extends Value> list, Value value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = new JLabel(value.getKey());
			label.setPreferredSize(new Dimension(300, label.getPreferredSize().height));
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(label, BorderLayout.NORTH);
			if (isSelected) {
				panel.setBackground(UIManager.getColor("List.selectionBackground"));
			}
			return panel;
		}
	}
}