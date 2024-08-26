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
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
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

/**
 * Represents an attribute manager dialog window to create and delete entities
 * and edit attribute values.
 */
public class AttributeManager extends JDialog {
	private static final long serialVersionUID = 6180793159551336995L;

	/**
	 * A table that shows the entities and their attributes. A new table is
	 * created each time a new variable is selected.
	 */
	private JTable attributeTable;
	
	/**
	 * A custom table model for the attribute/entity table.
	 */
	private AttributeTableModel model;
	
	/**
	 * A table sorter for the table.
	 */
	private TableRowSorter<AttributeTableModel> sorter;
	
	/**
	 * A scroll pane containing the table with the entities and attributes.
	 */
	private JScrollPane scrollPane;
	
	/**
	 * A panel holding the table.
	 */
	private JPanel tablePanel;
	
	/**
	 * A combo box showing the statement types in the database. When selecting
	 * a statement type, the variable combo box is populated with variables.
	 * The boxes serve to select which entities to show in the table.
	 */
	private JComboBox<StatementType> statementTypeBox;
	
	/**
	 * A combo box showing the variables. Used to select the variable for which
	 * the entities will be shown in the table.
	 */
	private JComboBox<Value> variableBox;
	
	/**
	 * A swing worker thread reference to make sure the thread is terminated
	 * before a new worker is executed to populate the table.
	 */
	private AttributeTableRefreshWorker worker;
	
	/**
	 * A button that deletes unused selected entities.
	 */
	private JButton deleteButton;
	
	/**
	 * Create a new attribute manager dialog window.
	 */
	public AttributeManager(JFrame parent) {
		super(parent, "Attribute manager", true);
		this.setModal(true);
		this.setTitle("Attribute manager");
		ImageIcon attributeManagerIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-list.png"));
		this.setIconImage(attributeManagerIcon.getImage());
		this.setLayout(new BorderLayout());
		
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
				if (e.getStateChange() == ItemEvent.SELECTED) {
					updateVariableBox();
				}
			}
		});
		StatementTypeComboBoxRenderer statementTypeRenderer = new StatementTypeComboBoxRenderer();
		statementTypeBox.setRenderer(statementTypeRenderer);
		variableBox.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent e) {
		    	if (e.getStateChange() == ItemEvent.SELECTED && variableBox.getItemCount() > 0) {
					int variableId = ((Value) variableBox.getSelectedItem()).getVariableId();
					createNewTable(variableId);
				}
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
		
		// add entity button
		ImageIcon addIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-circle-plus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JButton addButton = new JButton("Add entity", addIcon);
		addButton.setToolTipText("Add a new entity to the list. It can then be coded in a statement popup window.");
		addButton.setPreferredSize(new Dimension(addButton.getPreferredSize().width, newField.getPreferredSize().height));
		addButton.setEnabled(false);
		buttonPanel.add(addButton);
		
		// document listener to react to changes in the new entity field
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
				if (newValue.matches("^\\s*$") || model.containsValue(newValue) || newValue.length() > 190) {
					addButton.setEnabled(false);
				} else {
					addButton.setEnabled(true);
				}
			}
		});
		
		// action listener for the add entity button
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HashMap<String, String> map = new HashMap<String, String>();
				for (int i = 0; i < model.getAttributeVariables().size(); i++) {
					map.put(model.getAttributeVariables().get(i), "");
				}
				int variableId = ((Value) variableBox.getSelectedItem()).getVariableId();
				Entity entity = new Entity(-1, variableId, newField.getText(), new model.Color(0, 0, 0), -1, false, map);
				Dna.sql.addEntity(entity);
				refreshTable(variableId);
				newField.setText("");
			}
		});
		
		// delete entity button and action listener
		ImageIcon deleteIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-circle-minus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		deleteButton = new JButton("Delete selected unused entities", deleteIcon);
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
				int dialog = JOptionPane.showConfirmDialog(AttributeManager.this, "Delete " + selectedRows.length + " unused entities from the attribute manager?", "Confirmation", JOptionPane.YES_NO_OPTION);
				if (dialog == 0) {
					Dna.sql.deleteEntities(selectedRows);
					refreshTable(((Value) variableBox.getSelectedItem()).getVariableId());
				}
			}
		});

		// clean up button and action listener
		ImageIcon cleanUpIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-circle-0.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JButton cleanUpButton = new JButton("Delete all unused entities", cleanUpIcon);
		cleanUpButton.setToolTipText("Delete all unused entities (the rows highlighted in red) at once.");
		cleanUpButton.setPreferredSize(new Dimension(cleanUpButton.getPreferredSize().width, newField.getPreferredSize().height));
		buttonPanel.add(cleanUpButton);

		cleanUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<Integer> entityIds = new ArrayList<Integer>();
				for (int i = 0; i < model.getRowCount(); i++) {
					if (!model.getRow(i).isInDatabase() && !model.getRow(i).getValue().equals("")) { // don't add the empty entity
						entityIds.add(model.getRow(i).getId());
					}
				}
				if (entityIds.size() > 0) {
					int[] ids = new int[entityIds.size()];
					for (int i = 0; i < entityIds.size(); i++) {
						ids[i] = entityIds.get(i);
					}
					int dialog = JOptionPane.showConfirmDialog(AttributeManager.this, "Delete " + ids.length + " unused entities from the attribute manager?", "Confirmation", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						Dna.sql.deleteEntities(ids);
						refreshTable(((Value) variableBox.getSelectedItem()).getVariableId());
					}
				}
			}
		});

		this.add(buttonPanel, BorderLayout.SOUTH);
		
		// close button, upper right corner, and action listener
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ImageIcon closeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JButton closeButton = new JButton("Close", closeIcon);
		closeButton.setToolTipText("Close the attribute manager.");
		closeButton.setPreferredSize(new Dimension(closeButton.getPreferredSize().width, newField.getPreferredSize().height));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (worker != null) {
					worker.cancel(true);
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
	
	/**
	 * Populate the variable combo box with variables after a statement type has
	 * been selected in the statement type combo box.
	 */
	private void updateVariableBox() {
		StatementType st = (StatementType) statementTypeBox.getSelectedItem();
		ArrayList<Value> values = st.getVariables();
		variableBox.removeAllItems();
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i).getDataType().equals("short text")) {
				variableBox.addItem(values.get(i));
			}
		}
	}
	
	/**
	 * Create a new table with a table model, put it in a panel, and replace the
	 * old with the new table panel. Set all the renderers and editors for the
	 * table and format the table's appearance.
	 * 
	 * @param variableId  The variable ID for which entities should be shown in
	 *   the table.
	 */
	private void createNewTable(int variableId) {
		// remove the table panel and update GUI
		this.remove(tablePanel);
		this.revalidate();
		
		// create new panel with table
		tablePanel = new JPanel(new BorderLayout());
		model = new AttributeTableModel(Dna.sql.getAttributeVariables(variableId));
		attributeTable = new JTable(model);
		
		// editors and renderers
		EntityTableCellRenderer tableRenderer = new EntityTableCellRenderer();
		attributeTable.setDefaultRenderer(String.class, tableRenderer);
		attributeTable.setDefaultRenderer(Color.class, tableRenderer);
		attributeTable.setDefaultRenderer(Integer.class, tableRenderer);
		ColorChooserEditor cce = new ColorChooserEditor();
		attributeTable.getColumnModel().getColumn(2).setCellEditor(cce);
		attributeTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		// appearance of the table
		attributeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		sorter = new TableRowSorter<AttributeTableModel>(model);
		attributeTable.setRowSorter(sorter);
		scrollPane = new JScrollPane(attributeTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(attributeTable);
		scrollPane.setPreferredSize(new Dimension(1000, 600));
		for (int i = 0; i < (model.getAttributeVariables().size() + 3); i++) {
			int viewColumn = attributeTable.convertColumnIndexToView(i);
			if (i == 0 || i == 2) {
				attributeTable.getColumnModel().getColumn(viewColumn).setPreferredWidth(50);
			} else {
				attributeTable.getColumnModel().getColumn(viewColumn).setPreferredWidth(900 / (model.getAttributeVariables().size() + 1));
			}
		}
		tablePanel.add(scrollPane, BorderLayout.CENTER);
		
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
					Entity entity = model.getRow(attributeTable.convertRowIndexToModel(selectedRows[i]));
					if (entity.isInDatabase() || entity.getValue().equals("")) { // never delete the empty entity
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
	
	/**
	 * Refresh the existing table (without creating a new table), for example
	 * after adding or removing an entity. The method calls a Swing worker to
	 * populate the table after terminating all existing Swing worker threads.
	 * 
	 * @param variableId  The ID of the variable for which entities are shown.
	 */
	private void refreshTable(int variableId) {
		if (worker != null) {
			worker.cancel(true);
			while (!worker.isDone()) {
				// pause until the existing worker is done
			}
		}
		worker = new AttributeTableRefreshWorker(variableId);
		worker.execute();
	}
	
	/**
	 * A Swing worker that loads entities from the database and populates the
	 * entity/attribute table with these entities and their attributes. The
	 * class contains SQL code instead of moving the SQL code to the Sql class
	 * because chunks of the results need to be published inside the result set.
	 */
	private class AttributeTableRefreshWorker extends SwingWorker<List<Entity>, Entity> {
		/**
		 * The variable ID for which entities should be added.
		 */
		int variableId;
		
		/**
		 * Constructor. Creates a new Swing worker.
		 * 
		 * @param variableId  The variable ID for the entities.
		 */
		private AttributeTableRefreshWorker(int variableId) {
			this.variableId = variableId;
			model.clearRows();
		}
		
		/**
		 * The code of the background thread to retrieve the entities from the
		 * database and publish them such that they can be added to the table.
		 */
		@Override
		protected List<Entity> doInBackground() {
			// all entities with a given variable ID and an additional variable that checks if the entity was used in a statement, i.e., exists in DATASHORTTEXT
			String q1 = "SELECT E.ID, E.VariableId, E.Value, E.Red, E.Green, E.Blue, E.ChildOf, " + 
					"CASE WHEN EXISTS (SELECT ID from DATASHORTTEXT D WHERE D.Entity = E.ID AND D.VariableId = E.VariableId) " + 
					"THEN 1 " + 
					"ELSE 0 " + 
					"END AS InDatabase " + 
					"FROM " + 
					"ENTITIES E WHERE E.VariableId = ?;";
			
			// attribute variables and their values corresponding to a given entity ID
			String q2 = "SELECT EntityId, AttributeVariable, AttributeValue "
					+ "FROM ATTRIBUTEVALUES AS AVAL "
					+ "INNER JOIN ATTRIBUTEVARIABLES AS AVAR ON AVAL.AttributeVariableId = AVAR.ID "
					+ "WHERE VariableId = ?;";
			
			try (Connection conn = Dna.sql.getDataSource().getConnection();
					PreparedStatement s1 = conn.prepareStatement(q1);
					PreparedStatement s2 = conn.prepareStatement(q2)) {
				ArrayList<Entity> l = new ArrayList<Entity>();
				HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>(); // entity ID to index
				ResultSet r;
				
				// save entities in a list
				model.Color color;
				int entityId;
				s1.setInt(1, variableId);
				r = s1.executeQuery();
				while (!isCancelled() && !isDone() && r.next()) {
					if (isCancelled() || isDone()) {
						return null;
					}
	            	color = new model.Color(r.getInt("Red"), r.getInt("Green"), r.getInt("Blue"));
	            	entityId = r.getInt("ID");
	            	indexMap.put(entityId, l.size());
	            	l.add(new Entity(entityId, variableId, r.getString("Value"), color, r.getInt("ChildOf"), r.getInt("InDatabase") == 1, new HashMap<String, String>()));
	            }
				r.close();
				
				// add attributes to the list elements via map
				s2.setInt(1, variableId);
				r = s2.executeQuery();
				while (!isCancelled() && !isDone() && r.next()) {
					if (isCancelled() || isDone()) {
						return null;
					}
	            	l.get(indexMap.get(r.getInt("EntityId"))).getAttributeValues().put(r.getString("AttributeVariable"), r.getString("AttributeValue"));
	            }
				r.close();
				
				// publish complete entities
            	if (!isCancelled()) {
            		for (int i = 0; i < l.size(); i++) {
            			publish(l.get(i));
            		}
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
        
        /**
         * Transfer the entities that have been retrieved from the database to
         * the table model. 
         */
        @Override
        protected void process(List<Entity> chunks) {
        	if (!isCancelled()) {
        		model.addRows(chunks); // transfer a batch of rows to the table model
        	}
        }

        /**
         * Execute any code after all entities have been transferred to the
         * table model. Currently no code needs to be executed here.
         */
        @Override
        protected void done() {
        	if (attributeTable.getSelectedRowCount() == 0) {
        		deleteButton.setEnabled(false);
        	}
        }
    }
	
	/**
	 * A table model for representing entities including their attributes, along
	 * with an array of attribute variable names. The table model also contains
	 * methods for editing cells.
	 */
	class AttributeTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 7576473809983501603L;
		/**
		 * The names of the attribute variables that are stored in each entity.
		 */
		private ArrayList<String> attributeVariables;
		
		/**
		 * The entities including their attributes.
		 */
		private ArrayList<Entity> rows;
		
		/**
		 * Constructor. Creates an attribute table model and sets the attribute
		 * variable names. The entities are added later by the Swing worker.
		 * 
		 * @param attributeVariables  An array list of attribute variable names.
		 */
		public AttributeTableModel(ArrayList<String> attributeVariables) {
			this.attributeVariables = attributeVariables;
			this.rows = new ArrayList<Entity>();
		}
		
		/**
		 * How many columns are in the table?
		 * 
		 * @return The number of columns.
		 */
		@Override
		public int getColumnCount() {
			return (3 + this.attributeVariables.size());
		}

		/**
		 * How many rows are in the table?
		 * 
		 * @return The number of rows.
		 */
		@Override
		public int getRowCount() {
			return this.rows.size();
		}

		/**
		 * Get the contents of any cell in the table.
		 * 
		 * @param row  The row index, starting with 0.
		 * @param col  The column index, starting with 0.
		 * @return     The contents of the cell as an Object.
		 */
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
		
		/**
		 * Set/edit/update the value of any cell and write it into the database.
		 * 
		 * @param aValue  The new value to be written into the cell/database.
		 * @param row     The row index to update.
		 * @param col     The column index to update.
		 */
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
			        			"The value for Entity " + this.rows.get(row).getId() + " could not be updated in the database. You could check if an entity with the same name already exists; duplicate entities are not permitted. If that is not the case, it could be a database connection issue.",
			        			ex);
			        	Dna.logger.log(l);
					}
				}
			} else if (col == 2) { // color
				try {
					Dna.sql.setEntityColor(this.rows.get(row).getId(), new model.Color(((Color) aValue).getRed(), ((Color) aValue).getGreen(), ((Color) aValue).getBlue()));
					this.rows.get(row).setColor(new model.Color(((Color) aValue).getRed(), ((Color) aValue).getGreen(), ((Color) aValue).getBlue()));
				} catch (SQLException ex) {
		        	LogEvent l = new LogEvent(Logger.ERROR,
		        			"[SQL] Entity color could not be updated in the database.",
		        			"The color for Entity " + this.rows.get(row).getId() + " could not be updated in the database.",
		        			ex);
		        	Dna.logger.log(l);
				}
			} else { // an attribute value
				String attributeVariable = this.attributeVariables.get(col - 3);
				int variableId = this.rows.get(row).getVariableId();
				try {
					Dna.sql.setAttributeValue(this.rows.get(row).getId(), variableId, attributeVariable, (String) aValue);
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
		
		/**
		 * Get the name of a column.
		 * 
		 * @param col  The column index.
		 */
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
		
		/**
		 * Get the class of a column.
		 * 
		 * @param col  The column index.
		 */
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
		
		/**
		 * Is the cell editable? The ID should not be editable, all other cells
		 * can be edited.
		 * 
		 * @param row  The row index.
		 * @param col  The column index.
		 * @return     Is the cell editable?
		 */
		public boolean isCellEditable(int row, int col) {
			if (col == 0) {
				return false;
			} else {
				return true;
			}
		}
		
		/**
		 * Does the table contain an entity that corresponds to a certain String
		 * value? Used to check if an entity can be edited or already exists.
		 * 
		 * @param value  The value to look for.
		 * @return       Boolean indicating if an entity with the value exists.
		 */
		public boolean containsValue(String value) {
			for (int i = 0; i < rows.size(); i++) {
				if (rows.get(i).getValue().equals(value)) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Get an entity specified by a model row index.
		 * 
		 * @param row  The row index.
		 * @return     The entity corresponding to the row index.
		 */
		public Entity getRow(int row) {
			return this.rows.get(row);
		}
		
		/**
		 * Add a batch of rows to the table model and sort the entities.
		 * 
		 * @param entities  A list of entities to add.
		 */
		public void addRows(List<Entity> entities) {
			for (int i = 0; i < entities.size(); i++) {
				rows.add(entities.get(i));
			}
			sort();
		}
		
		/**
		 * Get an array list of attribute names for the entities.
		 * 
		 * @return  An array list of attribute variable names.
		 */
		public ArrayList<String> getAttributeVariables() {
			return this.attributeVariables;
		}
		
		/**
		 * Set the array list of attribute variable names for the entities.
		 * 
		 * @param attributeVariables  The array list of attribute names.
		 */
		public void setAttributeVariables(ArrayList<String> attributeVariables) {
			this.attributeVariables = attributeVariables;
		}
		
		/**
		 * Clear all entities from the table model and notify the listeners if
		 * possible. But keep the attribute variable names. This is used to
		 * repopulate the table after changes to the underlying database have
		 * occurred.
		 */
		void clearRows() {
			this.rows.clear();
			try {
				fireTableDataChanged();
			} catch (NullPointerException npe) {
				// ignore
			}
		}

		/**
		 * Sort the entities in the table model.
		 */
		void sort() {
			Collections.sort(this.rows);
			fireTableDataChanged();
		}
	}
	
	/**
	 * A table cell renderer that can display the contents of the table.
	 */
	private class EntityTableCellRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = 5436254013893853394L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
        	DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        	Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        	Entity entity = model.getRow(attributeTable.convertRowIndexToModel(row));

        	Color defaultColor = javax.swing.UIManager.getColor("Table.background");
	        Color selectedColor = javax.swing.UIManager.getColor("Table.selectionBackground");
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
	        
	        // the entity color
        	if (attributeTable.convertColumnIndexToModel(column) == 2) {
        		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    			JButton colorButton = (new JButton() {
					private static final long serialVersionUID = 1648028274961429514L;
					public void paintComponent(Graphics g) {
    					super.paintComponent(g);
    					g.setColor(entity.getColor().toAWTColor());
    					g.fillRect(0, 0, 30, 8);
    				}
    			});
    			colorButton.setPreferredSize(new Dimension(30, 8));
    			colorButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
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
        	} else { // all other (String) cells
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
	
	/**
	 * A table cell editor for colors, which displays a color picker dialog.
	 */
	public class ColorChooserEditor extends AbstractCellEditor implements TableCellEditor {
		private static final long serialVersionUID = 2145176699224057432L;
		private JButton delegate = new JButton();
		JPanel panel;
		Color savedColor;
		
		/**
		 * Create a new color chooser editor.
		 */
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
		
		/**
		 * Get the saved color.
		 * 
		 * @return  The saved color as an Object.
		 */
		public Object getCellEditorValue() {
			return savedColor;
		}

		/**
		 * Change the color of the class.
		 * 
		 * @param color  The new color to change into.
		 */
		private void changeColor(Color color) {
			if (color != null) {
				savedColor = color;
				delegate.setBackground(color);
			}
		}

		/**
		 * Get the panel with the color.
		 * 
		 * @param table       The table.
		 * @param value       The color as an Object.
		 * @param isSelected  Is the cell selected?
		 * @param row         The row index of the cell.
		 * @param column      The column index of the cell.
		 */
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			changeColor(((model.Color) value).toAWTColor());
			panel.setBackground(UIManager.getColor("Table.selectionBackground"));
			return panel;
		}
	}
	
	/**
	 * A combo box renderer for statement types. Used to display the name of the
	 * statement type in the combo box in the upper left corner of the attribute
	 * manager.
	 */
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

	/**
	 * A combo box renderer for variables in a statement type. Used to display
	 * the variable names in the combo box in the upper part of the attribute
	 * manager, where the user selects what types of entities to display in the
	 * table.
	 */
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