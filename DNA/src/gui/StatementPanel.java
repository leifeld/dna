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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
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
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import dna.Dna.CoderListener;
import dna.Dna.SqlListener;
import gui.MainWindow.ActionRemoveStatements;
import gui.TextPanel.TextPanelListener;
import logger.LogEvent;
import logger.Logger;
import model.Attribute;
import model.Coder;
import model.Statement;
import model.StatementType;
import model.Value;

/**
 * Statement panel on the right side of the screen.
 */
class StatementPanel extends JPanel implements SqlListener, CoderListener, TextPanelListener {
	private static final long serialVersionUID = 1044070479152247253L;
	private List<StatementListener> statementListeners = new ArrayList<StatementListener>();
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

		statementTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				
				int rowCount = statementTable.getSelectedRowCount();
				if (rowCount > 0) {
					actionRemoveStatements.setEnabled(true);
				} else {
					actionRemoveStatements.setEnabled(false);
				}
				if (rowCount == 1) {
					int selectedRow = statementTable.getSelectedRow();
					int selectedModelIndex = statementTable.convertRowIndexToModel(selectedRow);
					int id = (int) statementTableModel.getValueAt(selectedModelIndex, 0);
					fireSingleSelection(id);
				}
			}
		});
		
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
	private int getSelectedStatementId() {
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
	private void setSelectedStatementId(int statementId) {
		int modelRowIndex = statementTableModel.getModelRowById(statementId);
		if (modelRowIndex > -1) { // if no statement was previously selected, don't select a statement now.
			int tableRow = statementTable.convertRowIndexToView(modelRowIndex);
			this.statementTable.setRowSelectionInterval(tableRow, tableRow);
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
	
	/**
	 * Swing worker class for loading statements from the database and adding
	 * them to the statement table in a background thread. The class contains
	 * SQL code right here instead of moving the SQL code to the {@link sql.Sql
	 * Sql} class because it contains nested {@code publish} calls that need
	 * to be in the SQL code but also need to remain in the Swing worker class. 
	 */
	private class StatementTableRefreshWorker extends SwingWorker<List<Statement>, Statement> {
		/**
		 * Time stamp to measure the duration it takes to update the table. The
		 * duration is logged when the table has been updated.
		 */
		private long time;
		/**
		 * ID of the selected statement in the statement table, to restore it
		 * later and scroll back to the same position in the table after update.
		 */
		private int selectedId;

		/**
		 * A Swing worker that reloads all statements from the database and
		 * stores them in the table model for displaying them in the statement
		 * table.
		 */
		private StatementTableRefreshWorker() {
    		time = System.nanoTime(); // take the time to compute later how long the updating took
    		fireStatementRefreshStart(); // start displaying the update message in the status bar
    		selectedId = getSelectedStatementId();
    		statementTableModel.clear(); // remove all documents from the table model before re-populating the table
			LogEvent le = new LogEvent(Logger.MESSAGE,
					"[GUI] Initializing thread to populate statement table: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").",
					"A new swing worker thread has been started to populate the statement table with statements from the database in the background: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").");
			Dna.logger.log(le);
		}
		
		@Override
		protected List<Statement> doInBackground() {
			String query = "SELECT STATEMENTS.ID AS StatementId, "
					+ "StatementTypeId, "
					+ "STATEMENTTYPES.Label AS StatementTypeLabel, "
					+ "STATEMENTTYPES.Red AS StatementTypeRed, "
					+ "STATEMENTTYPES.Green AS StatementTypeGreen, "
					+ "STATEMENTTYPES.Blue AS StatementTypeBlue, "
					+ "Start, "
					+ "Stop, "
					+ "STATEMENTS.Coder AS CoderId, "
					+ "CODERS.Name AS CoderName, "
					+ "CODERS.Red AS CoderRed, "
					+ "CODERS.Green AS CoderGreen, "
					+ "CODERS.Blue AS CoderBlue, "
					+ "DocumentId, "
					+ "DOCUMENTS.Date AS Date, "
					+ "SUBSTRING(DOCUMENTS.Text, Start + 1, Stop - Start) AS Text "
					+ "FROM STATEMENTS "
					+ "INNER JOIN CODERS ON STATEMENTS.Coder = CODERS.ID "
					+ "INNER JOIN STATEMENTTYPES ON STATEMENTS.StatementTypeId = STATEMENTTYPES.ID "
					+ "INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId ORDER BY DOCUMENTS.DATE ASC;";
			ArrayList<Value> values;
			int statementId, statementTypeId, variableId;
			String variable, dataType;
			Color aColor, sColor, cColor;
			try (Connection conn = Dna.sql.getDataSource().getConnection();
					PreparedStatement s1 = conn.prepareStatement(query);
					PreparedStatement s2 = conn.prepareStatement("SELECT ID, Variable, DataType FROM VARIABLES WHERE StatementTypeId = ?;");
					PreparedStatement s3 = conn.prepareStatement("SELECT A.ID AS AttributeId, StatementId, A.VariableId, DST.ID AS DataId, A.Value, Red, Green, Blue, Type, Alias, Notes, ChildOf FROM DATASHORTTEXT AS DST LEFT JOIN ATTRIBUTES AS A ON A.ID = DST.Value AND A.VariableId = DST.VariableId WHERE DST.StatementId = ? AND DST.VariableId = ?;");
					PreparedStatement s4 = conn.prepareStatement("SELECT Value FROM DATALONGTEXT WHERE VariableId = ? AND StatementId = ?;");
					PreparedStatement s5 = conn.prepareStatement("SELECT Value FROM DATAINTEGER WHERE VariableId = ? AND StatementId = ?;");
					PreparedStatement s6 = conn.prepareStatement("SELECT Value FROM DATABOOLEAN WHERE VariableId = ? AND StatementId = ?;")) {
				ResultSet r1, r2, r3;
				r1 = s1.executeQuery();
				while (r1.next()) {
				    statementId = r1.getInt("StatementId");
				    statementTypeId = r1.getInt("StatementTypeId");
				    sColor = new Color(r1.getInt("StatementTypeRed"), r1.getInt("StatementTypeGreen"), r1.getInt("StatementTypeBlue"));
				    cColor = new Color(r1.getInt("CoderRed"), r1.getInt("CoderGreen"), r1.getInt("CoderBlue"));
				    s2.setInt(1, statementTypeId);
				    r2 = s2.executeQuery();
				    values = new ArrayList<Value>();
				    while (r2.next()) {
				    	variableId = r2.getInt("ID");
				    	variable = r2.getString("Variable");
				    	dataType = r2.getString("DataType");
				    	if (dataType.equals("short text")) {
					    	s3.setInt(1, statementId);
					    	s3.setInt(2, variableId);
					    	r3 = s3.executeQuery();
					    	while (r3.next()) {
				            	aColor = new Color(r3.getInt("Red"), r3.getInt("Green"), r3.getInt("Blue"));
				            	Attribute attribute = new Attribute(r3.getInt("AttributeId"), r3.getString("Value"), aColor, r3.getString("Type"), r3.getString("Alias"), r3.getString("Notes"), r3.getInt("ChildOf"), true);
					    		values.add(new Value(variableId, variable, dataType, attribute));
					    	}
				    	} else if (dataType.equals("long text")) {
					    	s4.setInt(1, variableId);
					    	s4.setInt(2, statementId);
					    	r3 = s4.executeQuery();
					    	while (r3.next()) {
					    		values.add(new Value(variableId, variable, dataType, r3.getString("Value")));
					    	}
				    	} else if (dataType.equals("integer")) {
					    	s5.setInt(1, variableId);
					    	s5.setInt(2, statementId);
					    	r3 = s5.executeQuery();
					    	while (r3.next()) {
					    		values.add(new Value(variableId, variable, dataType, r3.getInt("Value")));
					    	}
				    	} else if (dataType.equals("boolean")) {
					    	s6.setInt(1, variableId);
					    	s6.setInt(2, statementId);
					    	r3 = s6.executeQuery();
					    	while (r3.next()) {
					    		values.add(new Value(variableId, variable, dataType, r3.getInt("Value")));
					    	}
				    	}
				    }
				    Statement statement = new Statement(statementId,
				    		r1.getInt("Start"),
				    		r1.getInt("Stop"),
				    		statementTypeId,
				    		r1.getString("StatementTypeLabel"),
				    		sColor,
				    		r1.getInt("CoderId"),
				    		r1.getString("CoderName"),
				    		cColor,
				    		values,
				    		r1.getInt("DocumentId"),
				    		r1.getString("Text"),
				    		LocalDateTime.ofEpochSecond(r1.getLong("Date"), 0, ZoneOffset.UTC));
				    publish(statement);
				}
			} catch (SQLException e) {
				LogEvent l = new LogEvent(Logger.WARNING,
						"[SQL] Failed to retrieve statements.",
						"Attempted to retrieve all statements from the database, but something went wrong. You should double-check if the statements are all shown!",
						e);
				Dna.logger.log(l);
			}
			return null;
		}
        
        @Override
        protected void process(List<Statement> chunks) {
        	statementTableModel.addRows(chunks); // transfer a batch of rows to the statement table model
			setSelectedStatementId(selectedId); // select the statement from before; skipped if the statement not found in this batch
        }

        @Override
        protected void done() {
    		fireStatementRefreshEnd(); // stop displaying the update message in the status bar
			statementTableModel.fireTableDataChanged(); // update the statement filter
			setSelectedStatementId(selectedId);
    		long elapsed = System.nanoTime(); // measure time again for calculating difference
    		LogEvent le = new LogEvent(Logger.MESSAGE,
    				"[GUI]  ├─ (Re)loaded all " + statementTableModel.getRowCount() + " statements in " + (elapsed - time) / 1000000 + " milliseconds.",
    				"The statement table swing worker loaded the " + statementTableModel.getRowCount() + " statements from the DNA database in the "
    				+ "background and stored them in the statement table. This took " + (elapsed - time) / 1000000 + " seconds.");
    		Dna.logger.log(le);
			le = new LogEvent(Logger.MESSAGE,
					"[GUI]  └─ Closing thread to populate statement table: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").",
					"The statement table has been populated with statements from the database. Closing thread: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").");
			Dna.logger.log(le);
        }
    }

	/**
	 * Refresh the statement table using a Swing worker in the background.
	 */
	void refresh() {
		if (Dna.sql == null) {
			statementTableModel.clear();
		} else {
			StatementTableRefreshWorker worker = new StatementTableRefreshWorker();
			worker.execute();
		}
	}
	
	/**
	 * Listener interface for statement panel listeners. It makes all classes that
	 * implement this interface also implement the methods included here. The
	 * statement panel class can then notify the listeners by executing their
	 * methods.
	 */
	public interface StatementListener {
		void statementRefreshStart();
		void statementRefreshEnd();
		void statementSelected(int statementId);
	}
	
	/**
	 * Add a listener to the class.
	 * 
	 * @param listener An object that implements the {@link
	 *   gui.StatementListener StatementListener} interface.
	 */
	void addStatementListener(StatementListener listener) {
        statementListeners.add(listener);
    }

	/**
	 * Notify the listeners that a statement refresh swing worker has started.
	 */
	private void fireStatementRefreshStart() {
		for (int i = 0; i < statementListeners.size(); i++) {
			statementListeners.get(i).statementRefreshStart();
		}
	}

	/**
	 * Notify the listeners that a statement refresh swing worker has completed.
	 */
	private void fireStatementRefreshEnd() {
		for (int i = 0; i < statementListeners.size(); i++) {
			statementListeners.get(i).statementRefreshEnd();
		}
	}

	private void fireSingleSelection(int statementId) {
		for (int i = 0; i < statementListeners.size(); i++) {
			statementListeners.get(i).statementSelected(statementId);
		}
	}
	
	@Override
	public void adjustToChangedCoder() {
		// TODO Auto-generated method stub
	}

	@Override
	public void adjustToDatabaseState() {
		if (Dna.sql == null) {
			statementTableModel.clear();
			allButton.setSelected(true);
			allButton.setEnabled(false);
			docButton.setEnabled(false);
			filterButton.setEnabled(false);
		} else {
	        StatementTableRefreshWorker statementWorker = new StatementTableRefreshWorker();
	        statementWorker.execute();
			allButton.setEnabled(true);
			docButton.setEnabled(true);
			filterButton.setEnabled(true);
		}
	}

	@Override
	public void adjustToSelectedDocument(int documentId) {
		this.documentId = documentId;
		statementTableModel.fireTableDataChanged(); // update statement filter when a new document is selected
	}
}