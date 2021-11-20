package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import dna.Dna;
import model.StatementType;
import model.Value;

public class StatementTypeEditor extends JDialog {
	private static final long serialVersionUID = 3646305292547200629L;
	private JList<StatementType> statementTypeList;
	private ArrayList<StatementType> statementTypes;
	private JTextField idField, nameField, variableField;
	private JLabel idLabel, nameLabel, colorLabel;
	private ColorButton colorButton;
	private StatementType selectedStatementTypeCopy;
	private JTable variableTable;
	private VariableTableModel variableTableModel;
	private JButton addVariableButton, deleteVariableButton, renameVariableButton;

	public StatementTypeEditor() {
		this.setModal(true);
		this.setTitle("Edit statement types");
		ImageIcon statementTypeIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-message-2.png"));
		this.setIconImage(statementTypeIcon.getImage());
		this.setLayout(new BorderLayout());

		// get statement types from the database
		statementTypes = Dna.sql.getStatementTypes();
		StatementType[] st = new StatementType[statementTypes.size()];
		st = statementTypes.toArray(st);

		// statement type list
		statementTypeList = new JList<StatementType>(st);
		statementTypeList.setCellRenderer(new StatementTypeRenderer());
		statementTypeList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane listScroller = new JScrollPane(statementTypeList);
		listScroller.setPreferredSize(new Dimension(200, 600));
		statementTypeList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					loadStatementTypes();
				}
			}
		});
		this.add(listScroller, BorderLayout.WEST);

		// details panel
		idField = new JTextField(2);
		idField.setEditable(false);
		idLabel = new JLabel("ID", JLabel.TRAILING);
		idLabel.setLabelFor(idField);
		
		nameField = new JTextField(20);
		nameField.setEnabled(false);
		nameLabel = new JLabel("Name", JLabel.TRAILING);
		nameLabel.setLabelFor(nameField);
		nameLabel.setEnabled(false);

		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				if (selectedStatementTypeCopy != null) {
					selectedStatementTypeCopy.setLabel(nameField.getText());
				}
				checkButtons();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (selectedStatementTypeCopy != null) {
					selectedStatementTypeCopy.setLabel(nameField.getText());
				}
				checkButtons();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				if (selectedStatementTypeCopy != null) {
					selectedStatementTypeCopy.setLabel(nameField.getText());
				}
				checkButtons();
			}
		};
		nameField.getDocument().addDocumentListener(documentListener);
		
		colorButton = new ColorButton();
		colorButton.setEnabled(false);
		colorLabel = new JLabel("Color", JLabel.TRAILING);
		colorLabel.setLabelFor(colorButton);
		colorLabel.setEnabled(false);
		colorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(StatementTypeEditor.this, "Choose color...", colorButton.getColor());
				if (newColor != null) {
					colorButton.setColor(newColor);
				}
				selectedStatementTypeCopy.setColor(newColor);
				checkButtons();
			}
		});

		JPanel detailsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.insets = new Insets(5, 5, 0, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		detailsPanel.add(idLabel, gbc);
		gbc.gridx = 1;
		detailsPanel.add(idField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		detailsPanel.add(nameLabel, gbc);
		gbc.gridx = 1;
		detailsPanel.add(nameField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		detailsPanel.add(colorLabel, gbc);
		gbc.gridx = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		detailsPanel.add(colorButton, gbc);

		CompoundBorder borderDetails;
		borderDetails = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Statement type details"));
		detailsPanel.setBorder(borderDetails);
		
		// variable panel
		JPanel variablePanel = new JPanel(new BorderLayout());
        variableTableModel = new VariableTableModel();
        variableTable = new JTable(variableTableModel);
		variableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ArrayList<String> dataTypes = new ArrayList<String>();
        dataTypes.add("short text");
        dataTypes.add("long text");
        dataTypes.add("boolean");
        dataTypes.add("integer");
        variableTable.getColumnModel().getColumn(2).setCellEditor(new ComboBoxCellEditor(dataTypes));
        variableTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (variableTable.getSelectedRowCount() == 1 && variableTable.getSelectedRow() > -1) {
					variableField.setText((String) variableTable.getValueAt(variableTable.getSelectedRow(), 1));
				} else {
					variableField.setText("");
				}
			}
        });
        JScrollPane variableScrollPane = new JScrollPane(variableTable);
        variableScrollPane.setPreferredSize(new Dimension(300, 300));
        variablePanel.add(variableScrollPane, BorderLayout.CENTER);

        JPanel variableButtonPanel = new JPanel();
        
        variableField = new JTextField(15);
        variableField.setEnabled(false);
        variableField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkButtons();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkButtons();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkButtons();
			}
        });
        variableButtonPanel.add(variableField);

		ImageIcon addVariableIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-code-plus.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		addVariableButton = new JButton("Add", addVariableIcon);
		addVariableButton.setToolTipText("Add new variable to the statement type");
		addVariableButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Value v = new Value(0, variableField.getText(), "short text");
				selectedStatementTypeCopy.getVariables().add(v);
				variableTableModel.addRow(v);
				variableField.setText("");
			}
		});
		variableButtonPanel.add(addVariableButton);

		ImageIcon deleteVariableIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-code-minus.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		deleteVariableButton = new JButton("Delete", deleteVariableIcon);
		deleteVariableButton.setToolTipText("Delete selected variable from the statement type");
		deleteVariableButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = variableTable.getSelectedRow();
				row = variableTable.convertRowIndexToModel(row);
				Value v = variableTableModel.getRow(row);
				variableTableModel.removeRow(row);
				for (int i = 0; i < selectedStatementTypeCopy.getVariables().size(); i++) {
					if (v.getVariableId() == selectedStatementTypeCopy.getVariables().get(i).getVariableId()) {
						selectedStatementTypeCopy.getVariables().remove(i);
						break;
					}
				}
				variableField.setText("");
			}
		});
		variableButtonPanel.add(deleteVariableButton);

		ImageIcon renameVariableIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-forms.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		renameVariableButton = new JButton("Rename", renameVariableIcon);
		renameVariableButton.setToolTipText("Rename selected variable");
		renameVariableButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = variableTable.getSelectedRow();
				int rowModel = variableTable.convertRowIndexToModel(row);
				variableTable.setValueAt(variableField.getText(), rowModel, 1);
				Value v = variableTableModel.getRow(rowModel);
				for (int i = 0; i < selectedStatementTypeCopy.getVariables().size(); i++) {
					if (v.getVariableId() == selectedStatementTypeCopy.getVariables().get(i).getVariableId()) {
						selectedStatementTypeCopy.getVariables().get(i).setKey(variableField.getText());
						break;
					}
				}
				variableTable.setRowSelectionInterval(row, row);
				checkButtons();
			}
		});
		variableButtonPanel.add(renameVariableButton);
		variablePanel.add(variableButtonPanel, BorderLayout.SOUTH);
        
		CompoundBorder borderVariables;
		borderVariables = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Variables"));
		variablePanel.setBorder(borderVariables);
		
		// content panel: details, variables, and links
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(detailsPanel, BorderLayout.NORTH);
		contentPanel.add(variablePanel, BorderLayout.CENTER);
		this.add(contentPanel, BorderLayout.CENTER);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * Load all statement types from the database and put them into the GUI. A
	 * specified statement type can be selected afterwards. If this is not
	 * desired, a value of {@code -1} can be provided.
	 * 
	 * @param selectStatementTypeId  ID of the statement type to be selected.
	 *   Can be {@code -1}.
	 */
	private void repopulateStatementTypeListFromDatabase(int selectStatementTypeId) {
		DefaultListModel<StatementType> model = new DefaultListModel<StatementType>();
		statementTypes = Dna.sql.getStatementTypes();
		int statementTypeIndex = -1;
		for (int i = 0; i < statementTypes.size(); i++) {
			model.addElement(statementTypes.get(i));
			if (statementTypes.get(i).getId() == selectStatementTypeId) {
				statementTypeIndex = i;
			}
		}
		statementTypeList.setModel(model);
		if (statementTypeIndex > -1) {
			statementTypeList.setSelectedIndex(statementTypeIndex);
		}
	}
	
	private void loadStatementTypes() {
		if (!statementTypeList.isSelectionEmpty()) { // trigger only if selection has been completed and a statement type is selected
			selectedStatementTypeCopy = new StatementType(statementTypeList.getSelectedValue());
			
			if (selectedStatementTypeCopy.getId() == 1) { // do not make details editable if it is the DNA Statement type (ID = 1)
				idField.setEnabled(false);
				nameField.setEnabled(false);
				colorButton.setEnabled(false);
				variableField.setEnabled(false);
			} else {
				idField.setEnabled(true);
				nameField.setEnabled(true);
				colorButton.setEnabled(true);
				variableField.setEnabled(true);
			}
			
			idLabel.setEnabled(true);
			nameLabel.setEnabled(true);
			colorLabel.setEnabled(true);
			
			idField.setText(selectedStatementTypeCopy.getId() + "");
			nameField.setText(selectedStatementTypeCopy.getLabel());
			colorButton.setColor(selectedStatementTypeCopy.getColor());
			variableField.setText("");
			
			variableTableModel.clear();
			for (int i = 0; i < selectedStatementTypeCopy.getVariables().size(); i++) {
				variableTableModel.addRow(selectedStatementTypeCopy.getVariables().get(i));
			}
			if (variableTable.isEditing()) { // this condition fixes a bug where the combo box editor is still active when selecting a different statement type
				variableTable.getCellEditor().stopCellEditing();
			}
			if (statementTypeList.getSelectedValue().getId() == 1) {
				variableTable.setEnabled(false);
			} else {
				variableTable.setEnabled(true);
			}
		} else if (statementTypeList.isSelectionEmpty()) { // reset button was pressed
			selectedStatementTypeCopy = null;

			idLabel.setEnabled(false);
			idField.setEnabled(false);
			nameLabel.setEnabled(false);
			nameField.setEnabled(false);
			colorLabel.setEnabled(false);
			colorButton.setEnabled(false);
			variableField.setEnabled(false);
			
			idField.setText("");
			nameField.setText("");
			colorButton.setColor(Color.BLACK);
			variableField.setText("");
			
			variableTableModel.clear();
			variableTable.setEnabled(false);
		}
	}
	
	private void checkButtons() {
		if (statementTypeList.isSelectionEmpty() || variableField.getText().matches("^\\s*$")) {
			addVariableButton.setEnabled(false);
		} else {
			addVariableButton.setEnabled(true);
		}
		if (variableTable.getSelectedRowCount() < 1 ||
				statementTypeList.isSelectionEmpty() ||
				variableField.getText().matches("^\\s*$") ||
				!variableField.getText().equals(variableTable.getValueAt(variableTable.getSelectedRow(), 1))) {
			deleteVariableButton.setEnabled(false);
		} else {
			deleteVariableButton.setEnabled(true);
		}
		if (variableTable.getSelectedRowCount() < 1 ||
				statementTypeList.isSelectionEmpty() ||
				variableField.getText().matches("^\\s*$") ||
				variableField.getText().equals(variableTable.getValueAt(variableTable.getSelectedRow(), 1))) {
			renameVariableButton.setEnabled(false);
		} else {
			renameVariableButton.setEnabled(true);
		}
	}

	class StatementTypeRenderer implements ListCellRenderer<Object> {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			StatementType statementType = (StatementType) value;
			JLabel label = new JLabel(statementType.getLabel());
			if (isSelected == true) {
				label.setBackground(javax.swing.UIManager.getColor("List.dropCellBackground"));
			} else {
				label.setBackground(javax.swing.UIManager.getColor("List.background"));
			}
			label.setOpaque(true);
			label.setBorder(new EmptyBorder(5, 5, 5, 5));
			return label;
		}
	}

	/**
	 * Table cell editor for data types.
	 */
	public class ComboBoxCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
		private static final long serialVersionUID = -8838177513367736701L;
		private String dataType;
		private ArrayList<String> dataTypes;

		public ComboBoxCellEditor(ArrayList<String> dataTypes) {
			this.dataTypes = dataTypes;
		}

		@Override
		public Object getCellEditorValue() {
			return this.dataType;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			if (value instanceof String) {
				this.dataType = (String) value;
			}

			JComboBox<String> comboDataTypes = new JComboBox<String>();

			for (String aString : dataTypes) {
				comboDataTypes.addItem(aString);
			}

			comboDataTypes.setSelectedItem(this.dataType);
			comboDataTypes.addActionListener(this);

			if (isSelected) {
				comboDataTypes.setBackground(javax.swing.UIManager.getColor("Table.dropCellBackground"));
			} else {
				comboDataTypes.setBackground(javax.swing.UIManager.getColor("Table.background"));
			}

			return comboDataTypes;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			@SuppressWarnings("unchecked")
			JComboBox<String> comboDataTypes = (JComboBox<String>) event.getSource();
			this.dataType = (String) comboDataTypes.getSelectedItem();
		}
	}
	
	/**
	 * A table model for variables.
	 */
	private class VariableTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 6268706192843972473L;
		private ArrayList<Value> variables;

		/**
		 * Create a new variable table model.
		 */
		VariableTableModel() {
			this.variables = new ArrayList<Value>();
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return this.variables.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (row > -1 && row < variables.size()) {
				if (col == 0) {
					return variables.get(row).getVariableId();
				} else if (col == 1) {
					return variables.get(row).getKey();
				} else if (col == 2) {
					return variables.get(row).getDataType();
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
			if (row > -1 && row < variables.size() && col > 0) {
				if (col == 1) {
					variables.get(row).setKey((String) object); 
				} else if (col == 2) {
					variables.get(row).setDataType((String) object); 
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
			if (row > -1 && row < variables.size() && col > 1 && col < 3) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Return the name of a column.
		 * 
		 * @param column The column index of the table for which the name should
		 *   be returned, starting at 0.
		 */
		public String getColumnName(int column) {
			switch(column) {
				case 0: return "Variable ID";
				case 1: return "Name";
				case 2: return "Data type";
				default: return null;
			}
		}

		/**
		 * Return a row of the table based on the internal array list of values.
		 * 
		 * @param row Index of the {@link model.Value Value} object in the array
		 *   list.
		 * @return The {@link model.Value Value} object.
		 */
		public Value getRow(int row) {
			return variables.get(row);
		}

		/**
		 * Which type of object (i.e., class) shall be shown in the columns?
		 * 
		 * @param col The column index of the table for which the class type
		 *   should be returned, starting at 0.
		 */
		public Class<?> getColumnClass(int col) {
			switch (col) {
				case 0: return int.class;
				case 1: return String.class;
				case 2: return String.class;
				default: return null;
			}
		}
		
		/**
		 * Remove all variables from the model.
		 */
		public void clear() {
			variables.clear();
			fireTableDataChanged();
		}
		
		/**
		 * Add a new variable to the model.
		 * 
		 * @param variable A new {@link model.Value Value} object.
		 */
		public void addRow(Value variable) {
			variables.add(variable);
			fireTableRowsInserted(variables.size() - 1, variables.size() - 1);
		}
		
		/**
		 * Delete a variable at a specified row index.
		 * 
		 * @param modelRow The position in the array list.
		 */
		public void removeRow(int modelRow) {
			variables.remove(modelRow);
			fireTableRowsDeleted(modelRow, modelRow);
		}
	}
}