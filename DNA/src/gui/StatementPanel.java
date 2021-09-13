package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import dna.Dna.CoderListener;
import dna.Dna.SqlListener;
import gui.MainWindow.ActionRemoveStatements;
import model.Attribute;
import model.Coder;
import model.Statement;
import model.StatementType;
import model.Value;

/**
 * Statement panel on the right side of the screen.
 */
class StatementPanel extends JPanel implements SqlListener, CoderListener {
	private static final long serialVersionUID = 1044070479152247253L;
	private JTable statementTable;
	private StatementTableModel statementTableModel;
	private ArrayList<Value> variables;
	private String idFieldPattern = "";
	private JRadioButton allButton, docButton, filterButton;
	private JComboBox<StatementType> statementTypeBox;
	private int documentId; // needed for the filter to check if a statement is in the current document; updated by listener

	/**
	 * Create a new statement panel.
	 * 
	 * @param statementTableModel     Statement table model for the statements.
	 * @param actionRemoveStatements  An action that deletes statements.
	 */
	StatementPanel(StatementTableModel statementTableModel, ActionRemoveStatements actionRemoveStatements) {
		this.setLayout(new BorderLayout());
		this.statementTableModel = statementTableModel;
		statementTable = new JTable(statementTableModel);

		statementTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		TableRowSorter<StatementTableModel> sorter = new TableRowSorter<StatementTableModel>(statementTableModel);
		statementTable.setRowSorter(sorter);

		// set column visibility
		TableColumn column[] = new TableColumn[6];
	    for (int i = 0; i < column.length; i++) {
	        column[i] = statementTable.getColumnModel().getColumn(i);
	    }
	    Boolean[] columnsVisible = new Boolean[] {true, false, false, false, false, true};
	    
		statementTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		statementTable.getColumnModel().getColumn(1).setPreferredWidth(20);
		statementTable.getColumnModel().getColumn(2).setPreferredWidth(20);
		statementTable.getColumnModel().getColumn(3).setPreferredWidth(20);
		statementTable.getColumnModel().getColumn(4).setPreferredWidth(70);
		statementTable.getColumnModel().getColumn(5).setPreferredWidth(250);

		// statement table cell renderer
		statementTable.setDefaultRenderer(Integer.class, new StatementTableCellRenderer());
		statementTable.setDefaultRenderer(String.class, new StatementTableCellRenderer());
        statementTable.setDefaultRenderer(Coder.class, new StatementTableCellRenderer());

		JScrollPane statementTableScroller = new JScrollPane(statementTable);
		statementTableScroller.setViewportView(statementTable);
		statementTableScroller.setPreferredSize(new Dimension(300, 300));
		this.add(statementTableScroller, BorderLayout.CENTER);

	    // right-click menu for statement table
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuItemDelete = new JMenuItem(actionRemoveStatements);
		popupMenu.add(menuItemDelete);
		JSeparator sep = new JSeparator();
		popupMenu.add(sep);
		ImageIcon checkedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-checkbox.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon uncheckedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-square.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JMenuItem menuItemStatementId = new JMenuItem("ID", checkedIcon);
		popupMenu.add(menuItemStatementId);
		JMenuItem menuItemDocumentId = new JMenuItem("Document ID", uncheckedIcon);
		popupMenu.add(menuItemDocumentId);
		JMenuItem menuItemStart = new JMenuItem("Start position", uncheckedIcon);
		popupMenu.add(menuItemStart);
		JMenuItem menuItemEnd = new JMenuItem("End position", uncheckedIcon);
		popupMenu.add(menuItemEnd);
		JMenuItem menuItemCoder = new JMenuItem("Coder", uncheckedIcon);
		popupMenu.add(menuItemCoder);
		JMenuItem menuItemText = new JMenuItem("Statement text", checkedIcon);
		popupMenu.add(menuItemText);
		statementTable.setComponentPopupMenu(popupMenu);
		statementTable.getTableHeader().setComponentPopupMenu(popupMenu);
		statementTableScroller.setComponentPopupMenu(popupMenu);

		while (statementTable.getColumnModel().getColumnCount() > 0) {
			statementTable.getColumnModel().removeColumn(statementTable.getColumnModel().getColumn(0));
		}
		for (int i = 0; i < columnsVisible.length; i++) {
			if (columnsVisible[i] == true) {
				statementTable.getColumnModel().addColumn(column[i]);
			}
		}
		
		// ActionListener with actions for right-click statement menu
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == menuItemStatementId) {
					if (columnsVisible[0] == false) {
						columnsVisible[0] = true;
						menuItemStatementId.setIcon(checkedIcon);
					} else {
						columnsVisible[0] = false;
						menuItemStatementId.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemDocumentId) {
					if (columnsVisible[1] == false) {
						columnsVisible[1] = true;
						menuItemDocumentId.setIcon(checkedIcon);
					} else {
						columnsVisible[1] = false;
						menuItemDocumentId.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemStart) {
					if (columnsVisible[2] == false) {
						columnsVisible[2] = true;
						menuItemStart.setIcon(checkedIcon);
					} else {
						columnsVisible[2] = false;
						menuItemStart.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemEnd) {
					if (columnsVisible[3] == false) {
						columnsVisible[3] = true;
						menuItemEnd.setIcon(checkedIcon);
					} else {
						columnsVisible[3] = false;
						menuItemEnd.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemCoder) {
					if (columnsVisible[4] == false) {
						columnsVisible[4] = true;
						menuItemCoder.setIcon(checkedIcon);
					} else {
						columnsVisible[4] = false;
						menuItemCoder.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemText) {
					if (columnsVisible[5] == false) {
						columnsVisible[5] = true;
						menuItemText.setIcon(checkedIcon);
					} else {
						columnsVisible[5] = false;
						menuItemText.setIcon(uncheckedIcon);
					}
				}

				while (statementTable.getColumnModel().getColumnCount() > 0) {
					statementTable.getColumnModel().removeColumn(statementTable.getColumnModel().getColumn(0));
				}

				for (int i = 0; i < columnsVisible.length; i++) {
					if (columnsVisible[i] == true) {
						statementTable.getColumnModel().addColumn(column[i]);
					}
				}
			}
		};
		menuItemStatementId.addActionListener(al);
		menuItemDocumentId.addActionListener(al);
		menuItemStart.addActionListener(al);
		menuItemEnd.addActionListener(al);
		menuItemCoder.addActionListener(al);
		menuItemText.addActionListener(al);

		// row filter
		RowFilter<StatementTableModel, Integer> statementFilter = new RowFilter<StatementTableModel, Integer>() {
			public boolean include(Entry<? extends StatementTableModel, ? extends Integer> entry) {
				Statement s = statementTableModel.getRow(entry.getIdentifier());
				return filter(s, documentId);
			}
		};
		sorter.setRowFilter(statementFilter);
		
		// statement filter panel at the bottom
		StatementFilterPanel sfp = new StatementFilterPanel();
		this.add(sfp, BorderLayout.SOUTH);
	}
	
	JTable getStatementTable() {
		return statementTable;
	}
	
	/**
	 * Convert a statement table row index (view) to a row index in the table
	 * model.
	 * 
	 * @param row  The row in the table.
	 * @return     The row in the model.
	 */
	int convertRowIndexToModel(int row) {
		return this.statementTable.convertRowIndexToModel(row);
	}
	
	/**
	 * Get the selected rows in the statement table.
	 * 
	 * @return  An int array with the selected row indices.
	 */
	int[] getSelectedRows() {
		return this.statementTable.getSelectedRows();
	}
	
	/**
	 * Return the ID of the statement currently selected in the table.
	 * 
	 * @return Statement ID (or {@code -1} if no statement is selected.
	 */
	int getSelectedStatementId() {
		try {
			return (int) statementTable.getValueAt(statementTable.getSelectedRow(), 0);
		} catch (IndexOutOfBoundsException | NullPointerException e) { // e.g., no statements yet in table or nothing currently selected, so ID cannot be saved
			return -1;
		}
	}
	
	/**
	 * Select a statement with a certain ID in the statement table.
	 * 
	 * @param statementId  ID of the statement to be selected.
	 */
	void setSelectedStatementId(int statementId) {
		if (this.statementTable.getSelectedRowCount() == 1 && this.getSelectedStatementId() == statementId) {
			// right statement selected; do not clear selection
		} else {
			int modelRowIndex = statementTableModel.getModelRowById(statementId);
			if (modelRowIndex > -1) { // if no statement was previously selected, don't select a statement now.
				int tableRow = statementTable.convertRowIndexToView(modelRowIndex);
				this.statementTable.setRowSelectionInterval(tableRow, tableRow);
			}
		}
	}

	/**
	 * Table cell renderer for the statement table
	 */
	private class StatementTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -7421516568789969759L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        	Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        	StatementTableModel model = (StatementTableModel) table.getModel();
        	Statement s = model.getRow(table.convertRowIndexToModel(row));
        	if (value.getClass().toString().endsWith("Coder")) {
        		Coder coder = (Coder) value;
				CoderBadgePanel cbp = new CoderBadgePanel(coder, 14, 22);
				if (isSelected) {
            		cbp.setBackground(javax.swing.UIManager.getColor("Table.dropCellBackground"));
				} else {
					cbp.setBackground(s.getStatementTypeColor()); // TODO: if-clause to use Coder color; store coder in Sql class?
				}
				return cbp;
        	} else {
            	if (isSelected) {
            		c.setBackground(javax.swing.UIManager.getColor("Table.dropCellBackground"));
            	} else {
            		c.setBackground(s.getStatementTypeColor()); // TODO: if-clause to use Coder color; store coder in Sql class?
            	}
    	        return c;
        	}
		}
	}
	
	/**
	 * Filter statements. Return {@code true} if the statement should be listed
	 * in the table and {@code false} otherwise. This depends on the document
	 * that is currently being displayed and on the settings of the filter
	 * fields, which always keep the {@code variables} list up-to-date with the
	 * current filter contents, using a document filter.
	 * 
	 * @param s           The statement that should be assessed on whether it
	 *   should be displayed.
	 * @param documentId  The ID of the document that is currently being
	 *   displayed.
	 * @return            Whether the statement should be shown or not.
	 */
	private boolean filter(Statement s, int documentId) {
		if (allButton.isSelected()) {
			return true; // show all statements
		} else if (docButton.isSelected()) {
			if (s.getDocumentId() == documentId) {
				return true; // show statement if it's in the right document
			} else {
				return false;
			}
		} else if (variables == null || variables.size() == 0) {
			if (statementTypeBox.getSelectedItem() == null) {
				return true; // no statement type -> something went wrong; show the statement
			} else if (s.getStatementTypeId() == ((StatementType) statementTypeBox.getSelectedItem()).getId()) {
				return true; // statement type matches, variables cannot be found; show the statement
			} else {
				return false; // statement type does not match and there are no variables; don't show the statement
			}
		} else {
			// check statement type from statement type box for a non-match
			if (s.getStatementTypeId() != ((StatementType) statementTypeBox.getSelectedItem()).getId()) {
				return false;
			} else {
				// check ID field for a non-match
				Pattern pattern = Pattern.compile(idFieldPattern);
				Matcher m = pattern.matcher(String.valueOf((int) s.getId()));
				if (!m.find()) {
					return false;
				}
				// check variables for a non-match 
				for (int i = 0; i < variables.size(); i++) {
					pattern = Pattern.compile((String) variables.get(i).getValue());
					if (s.getValues().get(i).getDataType().equals("short text")) {
						m = pattern.matcher(((Attribute) s.getValues().get(i).getValue()).getValue());
					} else if (s.getValues().get(i).getDataType().equals("boolean") || s.getValues().get(i).getDataType().equals("integer")) {
						m = pattern.matcher(String.valueOf((int) s.getValues().get(i).getValue()));
					} else {
						m = pattern.matcher((String) s.getValues().get(i).getValue());
					}
					if (!m.find()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * The panel at the bottom of the filter panel, which takes care of
	 * filtering statements. The panel can dynamically rebuild the required
	 * filter fields depending on what is selected.
	 */
	private class StatementFilterPanel extends JPanel {
		private static final long serialVersionUID = -5543257765293355702L;
		JPanel statementTypePanel, variablePanel;
		
		/**
		 * Create a new statement filter panel.
		 */
		StatementFilterPanel() {
			this.setLayout(new BorderLayout());
			allButton = new JRadioButton("All");
			docButton = new JRadioButton("Current");
			filterButton = new JRadioButton("Filter");
			ButtonGroup group = new ButtonGroup();
			group.add(allButton);
			group.add(docButton);
			group.add(filterButton);
			allButton.setSelected(true);
			allButton.setEnabled(false);
			docButton.setEnabled(false);
			filterButton.setEnabled(false);
			JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			radioButtonPanel.add(allButton);
			radioButtonPanel.add(docButton);
			radioButtonPanel.add(filterButton);
			
			// react to changes in the selected statement type and rebuild the panel with the filter fields
			ItemListener aListener = new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					removeVariablePanel();
					StatementType st = (StatementType) statementTypeBox.getSelectedItem();
					variables = st.getVariables();
					JPanel varPanel = new JPanel(new GridBagLayout());
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.gridx = 0;
					gbc.gridy = 0;
					gbc.insets = new Insets(5, 10, 2, 8);
					gbc.anchor = GridBagConstraints.WEST;
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.weightx = 1.0;
					
					// first, create a filter field for statement ID
					JLabel idLabel = new JLabel("statement ID");
					varPanel.add(idLabel, gbc);
					gbc.gridx++;
					JTextField idFilterField = new JTextField(5);
					varPanel.add(idFilterField, gbc);
					idFilterField.getDocument().addDocumentListener(new DocumentListener() {
						@Override
						public void changedUpdate(DocumentEvent arg0) {
							updatePatterns();
						}
						@Override
						public void insertUpdate(DocumentEvent arg0) {
							updatePatterns();
						}
						@Override
						public void removeUpdate(DocumentEvent arg0) {
							updatePatterns();
						}
						private void updatePatterns() {
							idFieldPattern = idFilterField.getText();
							statementTableModel.fireTableDataChanged();
						}
					});
					gbc.gridx--;
					gbc.gridy++;
					
					// second, go through variables and create more filter fields
					for (int i = 0; i < variables.size(); i++) {
						final int VARINDEX = i;
						variables.get(i).setValue("");
						JLabel keyLabel = new JLabel(variables.get(i).getKey(), JLabel.LEFT);
						varPanel.add(keyLabel, gbc);
						gbc.gridx++;
						JTextField filterField = new JTextField(5);
						varPanel.add(filterField, gbc);
						gbc.gridx--;
						gbc.gridy++;
						filterField.getDocument().addDocumentListener(new DocumentListener() {
							@Override
							public void changedUpdate(DocumentEvent arg0) {
								updatePatterns();
							}
							@Override
							public void insertUpdate(DocumentEvent arg0) {
								updatePatterns();
							}
							@Override
							public void removeUpdate(DocumentEvent arg0) {
								updatePatterns();
							}
							private void updatePatterns() {
								variables.get(VARINDEX).setValue((String) filterField.getText());
								statementTableModel.fireTableDataChanged();
							}
						});
					}
					variablePanel = varPanel;
					variablePanel.setVisible(true);
					addVariablePanel();
					statementTableModel.fireTableDataChanged();
				}
			};
			
			// action listener for the three filter buttons to reload the filter elements below
			ActionListener al = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == allButton) {
						allButton.setEnabled(false);
						docButton.setEnabled(true);
						filterButton.setEnabled(true);
						removeVariablePanel();
						statementTypeBox.removeItemListener(aListener);
						statementTypeBox.removeAllItems();
						statementTypeBox.setVisible(false);
					} else if (e.getSource() == docButton) {
						allButton.setEnabled(true);
						docButton.setEnabled(false);
						filterButton.setEnabled(true);
						removeVariablePanel();
						statementTypeBox.removeItemListener(aListener);
						statementTypeBox.removeAllItems();
						statementTypeBox.setVisible(false);
					} else if (e.getSource() == filterButton) {
						allButton.setEnabled(true);
						docButton.setEnabled(true);
						filterButton.setEnabled(false);
						removeVariablePanel();
						statementTypeBox.removeItemListener(aListener);
						statementTypeBox.removeAllItems();
						statementTypeBox.addItemListener(aListener);
						ArrayList<StatementType> statementTypeArrayList = Dna.sql.getStatementTypes();
						for (int i = 0; i < statementTypeArrayList.size(); i++) {
							statementTypeBox.addItem(statementTypeArrayList.get(i));
						}
						statementTypeBox.setVisible(true);
					}
					statementTableModel.fireTableDataChanged(); // update the filter even if no new statement type has been selected yet
				}
			};
			allButton.addActionListener(al);
			docButton.addActionListener(al);
			filterButton.addActionListener(al);
			this.add(radioButtonPanel, BorderLayout.NORTH);
			
			// statement type combo box panel is empty for now
			statementTypePanel = new JPanel(new BorderLayout());
			statementTypeBox = new JComboBox<StatementType>();
			statementTypeBox.setRenderer(new StatementTypeComboBoxRenderer());
			statementTypeBox.setVisible(false);
			statementTypeBox.setBorder(new EmptyBorder(0, 10, 0, 10));
			statementTypePanel.add(statementTypeBox, BorderLayout.CENTER);
			this.add(statementTypePanel, BorderLayout.CENTER);
			
			// placeholder for the filter field panel at the bottom
			variablePanel = new JPanel();
			variablePanel.setVisible(false);
			this.add(variablePanel, BorderLayout.SOUTH);
		}

		/**
		 * Remove the panel with regex filter fields from the statement filter
		 * panel and update the user interface.
		 */
		private void removeVariablePanel() {
			this.remove(variablePanel);
			this.revalidate();
		}
		
		/**
		 * Add a panel with regex filter fields to the statement filter panel.
		 */
		private void addVariablePanel() {
			this.add(variablePanel, BorderLayout.SOUTH);
			this.revalidate();
		}
		
		/**
		 * A combo box renderer for statement types.
		 */
		private class StatementTypeComboBoxRenderer implements ListCellRenderer<Object> {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				if (value == null) {
					return new JLabel();
				} else {
					StatementType s = (StatementType) value;
					JButton colorRectangle = (new JButton() {
						private static final long serialVersionUID = 435490918616472975L;
						public void paintComponent(Graphics g) {
							super.paintComponent(g);
							g.setColor(s.getColor());
							g.fillRect(2, 2, 14, 14);
						}
					});
					colorRectangle.setPreferredSize(new Dimension(14, 14));
					colorRectangle.setEnabled(false);
					
					JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
					panel.add(colorRectangle);
					JLabel statementTypeLabel = new JLabel(s.getLabel());
					panel.add(statementTypeLabel);
					
					if (isSelected) {
						UIDefaults defaults = javax.swing.UIManager.getDefaults();
						Color bg = defaults.getColor("List.selectionBackground");
						panel.setBackground(bg);
					}
					return panel;
				}
			}
		}
	}
	
	@Override
	public void adjustToChangedCoder() {
		// TODO Auto-generated method stub
	}

	@Override
	public void adjustToDatabaseState() {
		if (Dna.sql == null) {
			allButton.setSelected(true);
			allButton.setEnabled(false);
			docButton.setEnabled(false);
			filterButton.setEnabled(false);
		} else {
			allButton.setEnabled(true);
			docButton.setEnabled(true);
			filterButton.setEnabled(true);
		}
	}
	
	void setDocumentId(int documentId) {
		this.documentId = documentId;
	}
}