package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
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
	private static final long serialVersionUID = 1048180479152247253L;
	private JTable statementTable;
	private StatementTableModel statementTableModel;
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
		TableColumn[] column = new TableColumn[6];
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
			if (columnsVisible[i]) {
				statementTable.getColumnModel().addColumn(column[i]);
			}
		}
		
		// ActionListener with actions for right-click statement menu
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == menuItemStatementId) {
					if (!columnsVisible[0]) {
						columnsVisible[0] = true;
						menuItemStatementId.setIcon(checkedIcon);
					} else {
						columnsVisible[0] = false;
						menuItemStatementId.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemDocumentId) {
					if (!columnsVisible[1]) {
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
		setRowFilter();
		
		// statement filter panel at the bottom
		sfp = new StatementFilterPanel();
		this.add(sfp, BorderLayout.SOUTH);
	}

	private void setRowFilter() {
		RowFilter<StatementTableModel, Integer> statementFilter = new RowFilter<StatementTableModel, Integer>() {
			public boolean include(Entry<? extends StatementTableModel, ? extends Integer> entry) {
				return filter(statementTableModel.getRow(entry.getIdentifier()));
			}
		};
		sorter.setRowFilter(statementFilter);
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
						cbp.setBackground(s.getCoderColor().toAWTColor());
					} else {
						cbp.setBackground(s.getStatementTypeColor().toAWTColor());
					}
				}
				return cbp;
        	} else {
            	if (isSelected) {
            		c.setBackground(javax.swing.UIManager.getColor("Table.selectionBackground"));
            	} else {
            		if (Dna.sql.getActiveCoder().isColorByCoder()) {
						c.setBackground(s.getCoderColor().toAWTColor());
					} else {
						c.setBackground(s.getStatementTypeColor().toAWTColor());
					}
            	}
    	        return c;
        	}
		}
	}

	/**
	 * Filter statement contents. Returns false if the statement should not be shown, true otherwise.
	 *
	 * @param s The table statement.
	 * @return True if the statement should be shown, false otherwise.
	 */
	private boolean filter(TableStatement s) {
		// do not show statement if no active coder
		if (Dna.sql.getActiveCoder() == null || Dna.sql.getConnectionProfile() == null) {
			return false;
		}

		// do not show statement if coder does not have the permission
		if (s.getCoderId() != Dna.sql.getActiveCoder().getId()) {
			if (!Dna.sql.getActiveCoder().isPermissionViewOthersStatements()) {
				return false;
			} else if (!Dna.sql.getActiveCoder().isPermissionViewOthersStatements(s.getCoderId())) {
				return false;
			}
		}

		// go through the different statement filters and decide if statement should be shown
		for (int i = 0; i < sfp.getFilterPanel().getComponents().length; i++) {
			StatementFilter f = (StatementFilter) sfp.getFilterPanel().getComponents()[i];
			Pattern p = Pattern.compile(f.getValue()); // to enable case-insensitive matching: Pattern.compile(f.getValue(), Pattern.CASE_INSENSITIVE);

			// documents
			if (f.getSource().equals("document")) {
				if (f.getLabel().equals("(current)")) {
					if (StatementPanel.this.documentId == s.getDocumentId() && f.isNegate()) {
						return false;
					}
					if (StatementPanel.this.documentId != s.getDocumentId() && !f.isNegate()) {
						return false;
					}
				} else if (f.getLabel().equals("id")) {
					Matcher m = p.matcher(String.valueOf(s.getDocumentId()));
					if ((m.find() && f.isNegate()) || (!m.find() && !f.isNegate())) {
						return false;
					}
				}
			}

			// roles
			if (f.getSource().equals("role")) {
				boolean matched = s.getRoleValues()
						.stream()
						.filter(rv -> {
							Matcher m = p.matcher(rv.getValue().toString());
							return rv.getRoleName().equals(f.getLabel()) && m.find();
						})
						.count() > 0;
				if (matched && f.isNegate() || !matched && !f.isNegate()) {
					return false;
				}
			}

			// variables
			if (f.getSource().equals("variable")) {
				boolean matched = s.getRoleValues()
						.stream()
						.filter(rv -> {
							Matcher m = p.matcher(rv.getValue().toString());
							return rv.getVariableName().equals(f.getLabel()) && m.find();
						})
						.count() > 0;
				if (matched && f.isNegate() || !matched && !f.isNegate()) {
					return false;
				}
			}

			// coder
			if (f.getSource().equals("coder")) {
				if (f.getLabel().equals("id")) {
					Matcher m = p.matcher(String.valueOf(s.getCoderId()));
					if ((m.find() && f.isNegate()) || (!m.find() && !f.isNegate())) {
						return false;
					}
				}
				if (f.getLabel().equals("name")) {
					Matcher m = p.matcher(s.getCoderName());
					if ((m.find() && f.isNegate()) || (!m.find() && !f.isNegate())) {
						return false;
					}
				}
			}

			// statement
			if (f.getSource().equals("statement")) {
				if (f.getLabel().equals("id")) {
					Matcher m = p.matcher(String.valueOf(s.getId()));
					if ((m.find() && f.isNegate()) || (!m.find() && !f.isNegate())) {
						return false;
					}
				}
				if (f.getLabel().equals("text")) {
					Matcher m = p.matcher(s.getText());
					if ((m.find() && f.isNegate()) || (!m.find() && !f.isNegate())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Represents a statement filter. The row filter subjects each row in the table to each statement filter object
	 * saved in the filter panel. A statement filter is a panel but contains relevant settings for filtering.
	 */
	public class StatementFilter extends JPanel {
		/**
		 * The type of filter. E.g., should we filter by role, variable etc.? Valid values are {@code "role"},
		 * {@code "variable"}, {@code "statement"}, {@code "document"}, and {@code "coder"}.
		 */
		private String source;
		/**
		 * The specific unit by which we want to filter, for example a specific role, variable etc.
		 */
		private String label;
		/**
		 * A regular expression to be matched against the statement contents.
		 */
		private String value;
		/**
		 * If negated, all non-matches will be retained. If not negated, all matches will be retained.
		 */
		private boolean negate;
		/**
		 * A delete button for removing the filter from the filter panel.
		 */
		private JButton button;

		/**
		 * Constructor for creating a new statement filter.
		 *
		 * @param label The label.
		 * @param source The source.
		 * @param value The value.
		 * @param negate The negate setting.
		 */
		public StatementFilter(String label, String source, String value, boolean negate) {
			this.label = label;
			this.source = source;
			this.value = value;
			this.negate = negate;

			setToolTipText("This statement filter can be deleted by clicking on the trash can.");
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(2, 2, 2, 2),
					BorderFactory.createLineBorder(Color.GRAY)));
			if (negate) {
				setBackground(new Color(242, 187, 201));
			} else {
				setBackground(new Color(187, 242, 201));
			}

			String lab;
			if (source.equals("document") && label.equals("(current)")) {
				lab = "[document] (current)";
			} else {
				lab = "[" + this.source + "] " + this.label + ": " + this.value;
			}
			if (this.negate) lab = lab + " (negated)";
			JLabel displayLabel = new JLabel(lab);
			add(displayLabel, BorderLayout.WEST);

			ImageIcon filterRemoveIcon = new SvgIcon("/icons/tabler_trash.svg", 16).getImageIcon();
			button = new JButton(filterRemoveIcon);
			button.setToolTipText("Delete this statement filter.");
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

	/**
	 * The statement filter panel is a GUI panel for adding and removing statement filters below the statement table.
	 */
	public class StatementFilterPanel extends JPanel {
		private JPanel filterPanel;
		private JScrollPane scrollPane;
		private JCheckBox negateCheckBox;
		private JComboBox<String> sourceBox;
		private JComboBox<String> labelBox;
		private JTextField valueField;
		private JButton addButton;

		/**
		 * Create a new statement filter panel.
		 */
		public StatementFilterPanel() {
			setLayout(new BorderLayout());

			// filter panel contains the statement filters; located in the CENTER of the BorderLayout
			filterPanel = new JPanel();
			filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
			scrollPane = new JScrollPane(filterPanel);
			add(scrollPane, BorderLayout.CENTER);

			// contains the two combo boxes and the value text field; GridLayout ensures equal width of the three components
			JPanel boxPanel = new JPanel(new GridLayout(1, 3));

			// source combo box, e.g., "role", "variable" etc.
			sourceBox = new JComboBox<>(new String[]{"role", "variable", "statement", "document", "coder"});
			sourceBox.setPreferredSize(new Dimension(0, 16));
			sourceBox.setEnabled(false);
			sourceBox.addItemListener(e -> {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					resetLabelBox((String) e.getItem());
				}
			});
			sourceBox.setToolTipText("Select what type of source to put a statement filter on.");
			boxPanel.add(sourceBox);

			// label combo box, e.g., "person", "agreement" etc.
			labelBox = new JComboBox<>();
			labelBox.setPreferredSize(new Dimension(0, 16));
			labelBox.addItemListener(e -> {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					resetValueField((String) e.getItem());
				}
			});
			labelBox.setEnabled(false);
			labelBox.setToolTipText("Select the criterion, role, or variable to filter on.");
			boxPanel.add(labelBox);

			// value text field, containing the regular expression
			valueField = new JTextField();
			valueField.setPreferredSize(new Dimension(0, 16));
			valueField.setEnabled(false);
			String valueFieldToolTip = "<html><p width=\"500\">Enter a regular expression (regex) with which statements are " +
					"matched. A regex is a search pattern. Regexes are a powerful way to filter statements, but they are " +
					"not trivial to use. Consult a website like <a href=\"https://regex101.com/\">https://regex101.com/</a> " +
					"to try out your regex pattern before using it here. The <a " +
					"href=\"https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html\">Oracle " +
					"Java Pattern website</a> contains a useful overview of regex elements. Note that any sub-unit of the text " +
					"is matched, not only the whole text. Note also that capitalization in the regex pattern matters. " +
					"For example, the patterns \"something\" and \"Something\" do not return the same results. To disregard " +
					"capitalization, try \"(?i)something\".</p></html>";
			valueField.setToolTipText(valueFieldToolTip);
			boxPanel.add(valueField);

			// contains the negate button (WEST), add button (EAST), and a panel with the combo boxes and text field (CENTER)
			JPanel addFilterPanel = new JPanel(new BorderLayout());
			addFilterPanel.add(boxPanel, BorderLayout.CENTER);

			// the negate checkbox on the left
			negateCheckBox = new JCheckBox("negate");
			negateCheckBox.setSelected(false);
			negateCheckBox.setEnabled(false);
			negateCheckBox.setToolTipText("If negated, statements matching the filter will NOT be shown (i.e., excluded).");
			addFilterPanel.add(negateCheckBox, BorderLayout.WEST);

			// button on the right; to add a new filter to the list
			ImageIcon filterAddIcon = new SvgIcon("/icons/tabler_filter_plus.svg", 16).getImageIcon();
			addButton = new JButton(filterAddIcon);
			addButton.setMargin(new Insets(0, 0, 0, 0));
			addButton.setContentAreaFilled(false);
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!valueField.getText().equals("") || (sourceBox.getSelectedItem().equals("document") && labelBox.getSelectedItem().equals("(current)"))) {

						// create new filter
						StatementFilter newFilter = new StatementFilter(
								(String) labelBox.getSelectedItem(),
								(String) sourceBox.getSelectedItem(),
								valueField.getText(),
								negateCheckBox.isSelected()
						);
						// add trash/delete button on the right of the filter
						newFilter.getButton().addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								filterPanel.remove(newFilter);
								updateFilterPanelSize();
								setRowFilter(); // update the row filter after removing
							}
						});
						filterPanel.add(newFilter);
						updateFilterPanelSize();
						setRowFilter(); // update the row filter after adding
					}
				}
			});
			addButton.setEnabled(false);
			addButton.setToolTipText("Add statement filter with the settings that were entered on the left.");
			addFilterPanel.add(addButton, BorderLayout.EAST);

			add(addFilterPanel, BorderLayout.SOUTH);
		}

		/**
		 * Reset the values available in the label combo box depending on the selection in the source combo box.
		 *
		 * @param source The selected value in the source combo box.
		 */
		private void resetLabelBox(String source) {
			labelBox.removeAllItems();
			if (source.equals("document")) {
				labelBox.addItem("(current)");
				labelBox.addItem("id");
				valueField.setVisible(false);
			} else if (source.equals("role")) {
				if (Dna.sql.getDataSource() != null) {
					Dna.sql.getRoles().stream().forEach(r -> labelBox.addItem(r.getRoleName()));
				}
				valueField.setVisible(true);
			} else if (source.equals("variable")) {
				if (Dna.sql.getDataSource() != null) {
					Dna.sql.getVariables().stream().forEach(v -> labelBox.addItem(v.getVariableName()));
				}
				valueField.setVisible(true);
			} else if (source.equals("statement")) {
				labelBox.addItem("id");
				labelBox.addItem("text");
				valueField.setVisible(true);
			} else if (source.equals("coder")) {
				labelBox.addItem("id");
				labelBox.addItem("name");
			}
		}

		/**
		 * Reset the value text field when a current document filter is selected.
		 *
		 * @param label The label from the label combo box to determine if a current document filter is selected.
		 */
		private void resetValueField(String label) {
			if (label.equals("(current)") && ((String) sourceBox.getSelectedItem()).equals("document")) {
				valueField.setText("");
				valueField.setVisible(false);
			} else if (((String) sourceBox.getSelectedItem()).equals("document")) {
				valueField.setVisible(true);
			}
		}

		/**
		 * Increase or decrease the size of the statement filter panel vertically, to make space for more filters or
		 * display fewer filters when they are added or removed.
		 */
		private void updateFilterPanelSize() {
			int panelHeight = 0;
			if (filterPanel.getComponentCount() > 0) {
				panelHeight = Math.min(312, 4 + 22 * filterPanel.getComponentCount());
			}
			Dimension newSize = new Dimension(0, panelHeight);
			scrollPane.setPreferredSize(newSize);
			repaintStatementPanel();
		}

		/**
		 * Allow adding new filters? If true, enable the different input fields. If false, disable them, and remove all
		 * existing filters.
		 *
		 * @param enabled Allow adding new filters, i.e., enable the controls for adding new filters?
		 */
		public void allowAddingFilter(boolean enabled) {
			addButton.setEnabled(enabled);
			sourceBox.setEnabled(enabled);
			labelBox.setEnabled(enabled);
			valueField.setEnabled(enabled);
			negateCheckBox.setEnabled(enabled);
			if (!enabled) {
				filterPanel.removeAll();
				updateFilterPanelSize();
			}
			setRowFilter();
		}

		/**
		 * Get the filter panel, for access from outside the class, to access the statement filters for filtering.
		 *
		 * @return A reference to the filter panel.
		 */
		public JPanel getFilterPanel() {
			return this.filterPanel;
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
		if (Dna.sql == null || Dna.sql.getActiveCoder() == null) {
			menuItemToggleSelection.setEnabled(false);
			sfp.sourceBox.setSelectedItem("role");
			sfp.resetLabelBox("role");
			sfp.valueField.setText("");
			sfp.valueField.setVisible(true);
			sfp.negateCheckBox.setSelected(false);
			sfp.allowAddingFilter(false);
		} else {
			menuItemToggleSelection.setEnabled(true);
			sfp.sourceBox.setSelectedItem("role");
			sfp.resetLabelBox("role");
			sfp.valueField.setText("");
			sfp.valueField.setVisible(true);
			sfp.negateCheckBox.setSelected(false);
			sfp.allowAddingFilter(true);
		}
	}
}