package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import gui.MainWindow.ActionRecodeStatements;
import gui.MainWindow.ActionRemoveStatements;
import logger.LogEvent;
import logger.Logger;
import model.*;

/**
 * Statement panel on the right side of the screen.
 */
class StatementPanel extends JPanel {
	private static final long serialVersionUID = 1048170479152247253L;
	private JTable statementTable;
	private StatementTableModel statementTableModel;
	private ArrayList<Variable> variables;
	private String idFieldPattern = "";
	private JRadioButton allButton, docButton, filterButton;
	private JComboBox<StatementType> statementTypeBox;
	private int documentId; // needed for the filter to check if a statement is in the current document; updated by listener
	private TableRowSorter<StatementTableModel> sorter;
	private JMenuItem menuItemStatementsSelected, menuItemStatementTypesSelected, menuItemToggleSelection;
	private StatementFilterPanel sfp;

	/**
	 * Create a new statement panel.
	 * 
	 * @param statementTableModel     Statement table model for the statements.
	 * @param actionRemoveStatements  An action that deletes statements.
	 */
	StatementPanel(
			StatementTableModel statementTableModel,
			ActionRecodeStatements actionRecodeStatements,
			ActionRemoveStatements actionRemoveStatements
			) {
		this.setLayout(new BorderLayout());
		this.statementTableModel = statementTableModel;
		statementTable = new JTable(statementTableModel);

		statementTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		sorter = new TableRowSorter<StatementTableModel>(statementTableModel);
		statementTable.setRowSorter(sorter);

		// set column visibility
		TableColumn column[] = new TableColumn[6];
	    for (int i = 0; i < column.length; i++) {
	        column[i] = statementTable.getColumnModel().getColumn(i);
	    }
	    Boolean[] columnsVisible = new Boolean[] {true, false, false, false, true, true};
	    
		statementTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		statementTable.getColumnModel().getColumn(1).setPreferredWidth(20);
		statementTable.getColumnModel().getColumn(2).setPreferredWidth(20);
		statementTable.getColumnModel().getColumn(3).setPreferredWidth(20);
		statementTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		statementTable.getColumnModel().getColumn(5).setPreferredWidth(250);

		// statement table cell renderer
		statementTable.setDefaultRenderer(Integer.class, new StatementTableCellRenderer());
		statementTable.setDefaultRenderer(String.class, new StatementTableCellRenderer());
        statementTable.setDefaultRenderer(Coder.class, new StatementTableCellRenderer());

		JScrollPane statementTableScroller = new JScrollPane(statementTable);
		statementTableScroller.setViewportView(statementTable);
		statementTableScroller.setPreferredSize(new Dimension(350, 300));
		this.add(statementTableScroller, BorderLayout.CENTER);

	    // right-click menu for statement table
		JPopupMenu popupMenu = new JPopupMenu();
		ImageIcon statementsIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-messages.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		menuItemStatementsSelected = new JMenuItem("0 statements selected", statementsIcon);
		menuItemStatementsSelected.setEnabled(false);
		popupMenu.add(menuItemStatementsSelected);
		menuItemStatementTypesSelected = new JMenuItem("of 0 statement type(s)");
		menuItemStatementTypesSelected.setEnabled(false);
		popupMenu.add(menuItemStatementTypesSelected);
		ImageIcon selectionIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-tallymarks.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		menuItemToggleSelection = new JMenuItem("Select all / none", selectionIcon);
		menuItemToggleSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (statementTable.getSelectedRowCount() == statementTable.getRowCount()) {
					statementTable.clearSelection();
				} else {
					statementTable.selectAll();
				}
			}
		});
		menuItemToggleSelection.setEnabled(false);
		popupMenu.add(menuItemToggleSelection);
		JSeparator sep1 = new JSeparator();
		popupMenu.add(sep1);
		JMenuItem menuItemRecode = new JMenuItem(actionRecodeStatements);
		popupMenu.add(menuItemRecode);
		JMenuItem menuItemDelete = new JMenuItem(actionRemoveStatements);
		popupMenu.add(menuItemDelete);
		JSeparator sep2 = new JSeparator();
		popupMenu.add(sep2);
		ImageIcon checkedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-checkbox.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		ImageIcon uncheckedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-square.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JMenuItem menuItemStatementId = new JMenuItem("ID", checkedIcon);
		popupMenu.add(menuItemStatementId);
		JMenuItem menuItemDocumentId = new JMenuItem("Document ID", uncheckedIcon);
		popupMenu.add(menuItemDocumentId);
		JMenuItem menuItemStart = new JMenuItem("Start position", uncheckedIcon);
		popupMenu.add(menuItemStart);
		JMenuItem menuItemEnd = new JMenuItem("End position", uncheckedIcon);
		popupMenu.add(menuItemEnd);
		JMenuItem menuItemCoder = new JMenuItem("Coder", checkedIcon);
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
		// TODO: add row filter back in
		/*
		RowFilter<StatementTableModel, Integer> statementFilter = new RowFilter<StatementTableModel, Integer>() {
			public boolean include(Entry<? extends StatementTableModel, ? extends Integer> entry) {
				TableStatement s = statementTableModel.getRow(entry.getIdentifier());
				ArrayList<Variable> values = Dna.sql.getValues(s.getId());
				return filter(s, documentId, values);
			}
		};
		sorter.setRowFilter(statementFilter);
		*/
		
		// statement filter panel at the bottom
		sfp = new StatementFilterPanel();
		sfp.setVisible(false); // TODO: make visible once done
		this.add(sfp, BorderLayout.SOUTH);
	}
	
	/**
	 * Set the document ID stored in the statement panel, which is used for
	 * filtering statements.
	 * 
	 * @param documentId  Document ID of the currently displayed document.
	 */
	void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	/**
	 * Get a reference to the statement table.
	 * 
	 * @return The statement table.
	 */
	JTable getStatementTable() {
		return statementTable;
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
				try {
					this.statementTable.setRowSelectionInterval(tableRow, tableRow);
				} catch (IllegalArgumentException e) {
					LogEvent l = new LogEvent(Logger.MESSAGE,
							"Tried to select a statement that was not selectable.",
							"Attempted selecting a statement from the statement table to show it in a popup window, but the active statement filter prevented the statement from being shown in the table, hence it could not be selected.",
							e);
					Dna.logger.log(l);
				}
				
			}
		}
	}

	/**
	 * Set the number of selected statements for the context menu.
	 * 
	 * @param message Menu item message.
	 */
	void setMenuItemStatementsSelected(String message) {
		this.menuItemStatementsSelected.setText(message);
	}
	
	/**
	 * Set the number of selected statement types for the context menu.
	 * 
	 * @param message Menu item message.
	 */
	void setMenuItemStatementTypesSelected(String message) {
		this.menuItemStatementTypesSelected.setText(message);
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
        	TableStatement s = model.getRow(table.convertRowIndexToModel(row));
        	if (value.getClass().toString().endsWith("Coder")) {
        		Coder coder = (Coder) value;
				CoderBadgePanel cbp = new CoderBadgePanel(coder, 13, 1, 22);
				if (isSelected) {
            		cbp.setBackground(javax.swing.UIManager.getColor("Table.selectionBackground"));
				} else {
					if (Dna.sql.getActiveCoder().isColorByCoder()) {
						cbp.setBackground(s.getCoderColor());
					} else {
						cbp.setBackground(s.getStatementTypeColor());
					}
				}
				return cbp;
        	} else {
            	if (isSelected) {
            		c.setBackground(javax.swing.UIManager.getColor("Table.selectionBackground"));
            	} else {
            		if (Dna.sql.getActiveCoder().isColorByCoder()) {
						c.setBackground(s.getCoderColor());
					} else {
						c.setBackground(s.getStatementTypeColor());
					}
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
	 * @param s The statement that should be assessed on whether it should be displayed.
	 * @param documentId The ID of the document that is currently being displayed.
	 * @param values The variables including values for the statement.
	 * @return Whether the statement should be shown or not.
	 */
	private boolean filter(Statement s, int documentId, ArrayList<Variable> values) {
		if (Dna.sql.getActiveCoder() == null || Dna.sql.getConnectionProfile() == null) {
			return false;
		}
		if (s.getCoderId() != Dna.sql.getActiveCoder().getId()) {
			if (!Dna.sql.getActiveCoder().isPermissionViewOthersStatements()) {
				return false;
			} else if (!Dna.sql.getActiveCoder().isPermissionViewOthersStatements(s.getCoderId())) {
				return false;
			}
		}
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
				// TODO: use roles instead of variables in filter (and also in the GUI for the filter)!
				/*
				for (int i = 0; i < variables.size(); i++) {
					pattern = Pattern.compile((String) variables.get(i).getValue());
					if (values.get(i).getValue().getClass().toString().endsWith("Entity")) {
						m = pattern.matcher(((Entity) values.get(i).getValue()).getValue());
					} else if (values.get(i).getValue().getClass().toString().endsWith("Integer")) {
						m = pattern.matcher(String.valueOf((int) values.get(i).getValue()));
					} else {
						m = pattern.matcher((String) values.get(i).getValue());
					}
					if (!m.find()) {
						return false;
					}
				}
				*/
			}
		}
		return true;
	}

	public class StatementFilter extends JPanel {

		private String label;
		private String source;
		private String value;
		private boolean negate;
		private JButton button;

		public StatementFilter(String label, String source, String value, boolean negate) {
			this.label = label;
			this.source = source;
			this.value = value;
			this.negate = negate;

			setLayout(new BorderLayout());
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(2, 2, 2, 2),
					BorderFactory.createLineBorder(Color.GRAY)));

			String lab;
			if (source.equals("text")) {
				lab = "text";
			} else if (source.equals("id")) {
				lab = "id";
			} else {
				lab = this.label;
			}
			lab = lab + ": " + this.value;
			if (this.negate) lab = lab + " (negated)";
			JLabel displayLabel = new JLabel(lab);
			add(displayLabel, BorderLayout.WEST);

			ImageIcon filterRemoveIcon = new SvgIcon("/icons/tabler_trash.svg", 16).getImageIcon();
			button = new JButton(filterRemoveIcon);
			button.setToolTipText("add filter");
			button.setMargin(new Insets(0, 0, 0, 0));
			button.setContentAreaFilled(false);
			button.setBorder(new EmptyBorder(0, 0, 0, 0));
			add(button, BorderLayout.EAST);
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public boolean isNegate() {
			return negate;
		}

		public void setNegate(boolean negate) {
			this.negate = negate;
		}

		public JButton getButton() {
			return this.button;
		}
	}

	public class StatementFilterPanel extends JPanel {

		private JButton addButton;
		private JComboBox<String> sourceBox;
		private JComboBox<String> labelBox;
		private JTextField valueField;
		private JCheckBox negateCheckBox;
		private JPanel filterPanel;
		private JScrollPane scrollPane;

		public StatementFilterPanel() {
			setLayout(new BorderLayout());

			filterPanel = new JPanel();
			filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
			scrollPane = new JScrollPane(filterPanel);
			add(scrollPane, BorderLayout.CENTER);

			JPanel addFilterPanel = new JPanel(new BorderLayout());

			ImageIcon filterAddIcon = new SvgIcon("/icons/tabler_filter_plus.svg", 16).getImageIcon();
			addButton = new JButton(filterAddIcon);
			addButton.setToolTipText("add filter");
			addButton.setMargin(new Insets(0, 0, 0, 0));
			addButton.setContentAreaFilled(false);
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					StatementFilter newFilter = new StatementFilter(
							(String) labelBox.getSelectedItem(),
							(String) sourceBox.getSelectedItem(),
							valueField.getText(),
							negateCheckBox.isSelected()
					);
					newFilter.getButton().addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							filterPanel.remove(newFilter);
							updateFilterPanelSize();
						}
					});
					filterPanel.add(newFilter);
					updateFilterPanelSize();
				}
			});
			addButton.setEnabled(false);
			addFilterPanel.add(addButton, BorderLayout.WEST);

			negateCheckBox = new JCheckBox("negate");
			negateCheckBox.setSelected(false);
			negateCheckBox.setEnabled(false);
			addFilterPanel.add(negateCheckBox, BorderLayout.EAST);

			JPanel comboBoxPanel = new JPanel(new BorderLayout());

			sourceBox = new JComboBox<>(new String[]{"role", "variable", "document", "id", "text"});
			sourceBox.setPreferredSize(new Dimension(80, 18));
			sourceBox.setEnabled(false);
			sourceBox.addItemListener(e -> {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					resetLabelBox((String) e.getItem());
				}
			});
			comboBoxPanel.add(sourceBox, BorderLayout.WEST);

			labelBox = new JComboBox<>();
			labelBox.setPreferredSize(new Dimension(80, 18));
			labelBox.setEnabled(false);
			comboBoxPanel.add(labelBox, BorderLayout.CENTER);

			valueField = new JTextField(12);
			valueField.setEnabled(false);
			comboBoxPanel.add(valueField, BorderLayout.EAST);

			addFilterPanel.add(comboBoxPanel, BorderLayout.CENTER);

			add(addFilterPanel, BorderLayout.SOUTH);
		}

		private void resetLabelBox(String source) {
			labelBox.setVisible(!(source.equals("text") || source.equals("id")));
			labelBox.removeAllItems();
			if (source.equals("document")) {
				labelBox.addItem("author");
				labelBox.addItem("source");
				labelBox.addItem("section");
				labelBox.addItem("type");
				labelBox.addItem("notes");
			} else if (source.equals("role")) {
				Dna.sql.getRoles().stream().forEach(r -> labelBox.addItem(r.getRoleName()));
			} else if (source.equals("variable")) {
				Dna.sql.getVariables().stream().forEach(v -> labelBox.addItem(v.getVariableName()));
			}
		}

		private void updateFilterPanelSize() {
			int panelHeight = 0;
			if (filterPanel.getComponentCount() > 0) {
				panelHeight = Math.min(312, 4 + 22 * filterPanel.getComponentCount());
			}
			Dimension newSize = new Dimension(0, panelHeight);
			scrollPane.setPreferredSize(newSize);
			repaintStatementPanel();
		}

		public void setEnabled(boolean enabled) {
			addButton.setEnabled(enabled);
			sourceBox.setEnabled(enabled);
			labelBox.setEnabled(enabled);
			valueField.setEnabled(enabled);
			negateCheckBox.setEnabled(enabled);
		}

		private class StatementFilterRenderer extends JPanel implements ListCellRenderer<StatementFilter> {

			@Override
			public Component getListCellRendererComponent(JList<? extends StatementFilter> list, StatementFilter value, int index, boolean isSelected, boolean cellHasFocus) {
				return value;
			}
		}
	}

	/**
	 * Revalidate and repaint the statement panel to update the vertical size of the filter panel. Called from within
	 * the statement filter panel when a filter is added or removed.
	 */
	private void repaintStatementPanel() {
		revalidate();
		repaint();
	}

	/**
	 * Adjust the filter buttons when a database has been opened or closed.
	 */
	public void adjustToChangedConnection() {
		if (Dna.sql == null) {
			menuItemToggleSelection.setEnabled(false);
			//allButton.setSelected(true);
			//allButton.setEnabled(false);
			//docButton.setEnabled(false);
			//filterButton.setEnabled(false);
			sfp.setEnabled(false);
		} else {
			menuItemToggleSelection.setEnabled(true);
			//allButton.setSelected(true);
			//allButton.doClick();
			//allButton.setEnabled(false);
			//docButton.setEnabled(true);
			//filterButton.setEnabled(true);
			sfp.setEnabled(true);
		}
	}
}