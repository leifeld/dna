package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import dna.Dna;
import model.Coder;
import model.Entity;
import model.Statement;
import model.StatementType;

public class StatementRecoder extends JDialog {
	private static final long serialVersionUID = -6224715694254322483L;
	private Container c;
	private int currentIndex = 0;
	private StatementType statementType;
	private StatementRecodeTableModel tableModel;
	private JComboBox<Coder> coderComboBox;
	private CoderComboBoxModel comboBoxModel;
	private JTextField idField, indexField;
	private JButton previousButton, nextButton;
	private ArrayList<Integer> variableIds;
	HashMap<Integer, Integer> indexMap;
	private ArrayList<Component> components = new ArrayList<Component>();

	public StatementRecoder(Frame parent, int[] statementIds, StatementType statementType) {
		super(parent, "StatementRecorder", true);
		this.statementType = statementType;
		
		this.setTitle("Recode statements");
		this.setModal(true);
		ImageIcon statementIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-pencil.png"));
		this.setIconImage(statementIcon.getImage());
		c = getContentPane();

		JPanel dialogPanel = new JPanel(new BorderLayout());
		JPanel contentsPanel = new JPanel(new BorderLayout());
		
		/* Dialog layout:
		 * 
		 * NORTH of dialog panel: contents panel
		 * 
		 * - NORTH of contents panel: control panel
		 *   - west: navigation panel with previous/next buttons and ID
		 *   - east: button panel with reset, cancel, and save buttons
		 * 
		 * - SOUTH of contents panel: statement details panel
		 *   - coder combo box with apply all button
		 *   - combo boxes and other contents, with apply all buttons
		 * 
		 * CENTER of dialog panel: preview table panel
		 */
		
		// dialog panel CENTER: preview table panel
		tableModel = new StatementRecodeTableModel(Dna.sql.getStatements(statementIds), statementType);
		JTable table = new JTable(tableModel);
		table.setDefaultRenderer(Coder.class, new CoderTableCellRenderer());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(800, 200));
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(scrollPane, BorderLayout.CENTER);
		CompoundBorder tablePanelBorder = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Preview"));
		tablePanel.setBorder(tablePanelBorder);
		dialogPanel.add(tablePanel, BorderLayout.CENTER);
		
		// control panel WEST: navigation panel
		JPanel controlPanel = new JPanel(new BorderLayout());
		JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		int h = 20; // getting the text field height does not work properly on MacOS, so need to hard-code

		ImageIcon previousIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-caret-left.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		previousButton = new JButton(previousIcon);
		previousButton.setPreferredSize(new Dimension(h, h));
		previousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentIndex > 0) {
					currentIndex--;
					updateContents(currentIndex);
				}
			}
		});
		navigationPanel.add(previousButton);
		
		ImageIcon nextIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-caret-right.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		nextButton = new JButton(nextIcon);
		nextButton.setPreferredSize(new Dimension(h, h));
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentIndex < statementIds.length - 1) {
					currentIndex++;
					updateContents(currentIndex);
				}
			}
		});
		navigationPanel.add(nextButton);
		
		JLabel indexLabel = new JLabel(" Showing");
		navigationPanel.add(indexLabel);
		
		indexField = new JTextField(1 + " / " + statementIds.length);
		int digits = Integer.toString(tableModel.getRowCount()).length();
		int columns = 2 * digits + 3;
		indexField.setColumns(columns);
		indexField.setPreferredSize(new Dimension(indexField.getPreferredSize().width, h));
		indexField.setEditable(false);
		navigationPanel.add(indexField);

		JLabel idLabel = new JLabel(" ID");
		navigationPanel.add(idLabel);
		
		idField = new JTextField(Integer.toString(tableModel.getRow(currentIndex).getId()));
		idField.setColumns(digits);
		idField.setPreferredSize(new Dimension(idField.getPreferredSize().width, h));
		idField.setEditable(false);
		navigationPanel.add(idField);

		controlPanel.add(navigationPanel, BorderLayout.WEST);
		
		// control panel EAST: button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton resetButton = new JButton("Reset");
		buttonPanel.add(resetButton);
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		JButton saveButton = new JButton("Save");
		buttonPanel.add(saveButton);
		controlPanel.add(buttonPanel, BorderLayout.EAST);
		
		CompoundBorder controlBorder = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Navigation and control"));
		controlPanel.setBorder(controlBorder);
		contentsPanel.add(controlPanel, BorderLayout.NORTH);
		
		// statement details panel CENTER: statement fields and their buttons
		JPanel statementDetailsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 0;
		
		JLabel coderLabel = new JLabel("coder", JLabel.TRAILING);
		statementDetailsPanel.add(coderLabel, gbc);
		
		gbc.weightx = 1;
		gbc.gridx++;
		coderComboBox = new JComboBox<Coder>();
		ArrayList<Coder> coders = Dna.sql.getCoders()
				.stream()
				.filter(c -> (Dna.sql.getActiveCoder().isPermissionEditOthersStatements(c.getId()) || Dna.sql.getActiveCoder().getId() == c.getId()))
				.collect(Collectors.toCollection(ArrayList::new)); // get coders for whom active coder has permission
		comboBoxModel = new CoderComboBoxModel(coders);
		coderComboBox.setModel(comboBoxModel);
		coderComboBox.setRenderer(new CoderComboBoxRenderer(9, 0, 22));
		coderComboBox.setSelectedIndex(comboBoxModel.getIndexByCoderId(tableModel.getRow(currentIndex).getCoderId()));
		coderComboBox.setPreferredSize(new Dimension(coderComboBox.getPreferredSize().width, h));
		statementDetailsPanel.add(coderComboBox, gbc);
		
		gbc.weightx = 0;
		gbc.gridx++;
		ImageIcon applyAllIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-copy.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JButton applyAllCoderButton = new JButton(applyAllIcon);
		applyAllCoderButton.setPreferredSize(new Dimension(h, h));
		applyAllCoderButton.setToolTipText("Apply this coder to all " + statementIds.length + " selected statements.");
		applyAllCoderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});
		statementDetailsPanel.add(applyAllCoderButton);
		gbc.gridx = 0;
		gbc.gridy++;
		
		// add fields for the values
		variableIds = new ArrayList<Integer>();
		indexMap = new HashMap<Integer, Integer>();
		int counter = 0;
		for (int i = 0; i < statementType.getVariables().size(); i++) {
			if (statementType.getVariables().get(i).getDataType().equals("short text")) {
				indexMap.put(i, counter);
				counter++;
				variableIds.add(statementType.getVariables().get(i).getVariableId());
			}
		}
		ArrayList<ArrayList<Entity>> entities = Dna.sql.getEntities(variableIds, false); // switch to true to color unused entities in red; but it takes much longer with large databases

		// create boxes with values
		for (int i = 0; i < statementType.getVariables().size(); i++) {
			String key = statementType.getVariables().get(i).getKey();
			String dataType = statementType.getVariables().get(i).getDataType();
			JLabel label = new JLabel(key, JLabel.TRAILING);
			if (dataType.equals("short text")) {
				Entity[] entitiesArray = new Entity[entities.get(indexMap.get(i)).size()];
				entitiesArray = entities.get(indexMap.get(i)).toArray(entitiesArray);
				JComboBox<Entity> box = new JComboBox<Entity>(entitiesArray);
				box.setRenderer(new AttributeComboBoxRenderer());
				box.setEditable(true);
				
				// paint the selected value in the attribute color
				String s = ((JTextField) box.getEditor().getEditorComponent()).getText();
				Color fg = javax.swing.UIManager.getColor("TextField.foreground"); // default unselected foreground color of JTextField
				for (int j = 0; j < box.getModel().getSize(); j++) {
					if (s.equals(box.getModel().getElementAt(j).getValue())) {
						fg = box.getModel().getElementAt(j).getColor();
					}
				}
				((JTextField) box.getEditor().getEditorComponent()).setSelectedTextColor(fg);
				((JTextField) box.getEditor().getEditorComponent()).setForeground(fg);
				
				// add a document listener to the combobox to paint the selected value in the attribute color, despite being highlighted
				((JTextField) box.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {
					@Override
					public void changedUpdate(DocumentEvent arg0) {
						formatEntry();
					}
					@Override
					public void insertUpdate(DocumentEvent arg0) {
						formatEntry();
					}
					@Override
					public void removeUpdate(DocumentEvent arg0) {
						formatEntry();
					}
					private void formatEntry() {
						Color fg = javax.swing.UIManager.getColor("TextField.foreground"); // default unselected foreground color of JTextField
						for (int i = 0; i < box.getModel().getSize(); i++) {
							if (((JTextField) box.getEditor().getEditorComponent()).getText().equals(box.getModel().getElementAt(i).getValue())) {
								fg = box.getModel().getElementAt(i).getColor();
							}
						}
						((JTextField) box.getEditor().getEditorComponent()).setSelectedTextColor(fg);
						((JTextField) box.getEditor().getEditorComponent()).setForeground(fg);
					}
				});
				
				if (Dna.sql.getActiveCoder().isPopupAutoComplete()) {
					AutoCompleteDecorator.decorate(box); // auto-complete short text values; part of SwingX
				}
    			box.setPreferredSize(new Dimension(Dna.sql.getActiveCoder().getPopupWidth(), 20));
    			box.setSelectedItem((Entity) tableModel.getRow(currentIndex).getValues().get(i).getValue());
    			
    			// need to add a focus listener to save the contents; otherwise without auto-completion the last edited box will not be saved
    			box.getEditor().getEditorComponent().addFocusListener(new FocusListener() {
    				
					@Override
					public void focusGained(FocusEvent e) {
						// no action needed when focus is gained
					}
					
					@Override
					public void focusLost(FocusEvent e) {
						box.setSelectedItem(box.getEditor().getItem());
					}
    			});
    			
    			components.add(box);
    			
				gbc.anchor = GridBagConstraints.EAST;
				statementDetailsPanel.add(label, gbc);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.gridx++;
				gbc.weightx = 1;
				statementDetailsPanel.add(box, gbc);

				gbc.weightx = 0;
				gbc.gridx++;
				JButton applyAllButton = new JButton(applyAllIcon);
				applyAllButton.setPreferredSize(new Dimension(h, h));
				applyAllButton.setToolTipText("Apply this entity or pattern to all " + statementIds.length + " selected statements.");
				applyAllButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO
					}
				});
				statementDetailsPanel.add(applyAllButton, gbc);
				gbc.gridx = 0;
				gbc.gridy++;
			} else if (dataType.equals("long text")) {
				String entry = (String) tableModel.getRow(currentIndex).getValues().get(i).getValue();
    			JTextArea box = new JTextArea();
    			box.setEditable(true);
    			box.setWrapStyleWord(true);
    			box.setLineWrap(true);
    			box.setText(entry);
    			JScrollPane boxScroller = new JScrollPane(box);
    			boxScroller.setPreferredSize(new Dimension(Dna.sql.getActiveCoder().getPopupWidth(), 100));
    			boxScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    			components.add(box);
    			
				gbc.anchor = GridBagConstraints.NORTHEAST;
				statementDetailsPanel.add(label, gbc);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.weightx = 1;
				gbc.gridx++;
				statementDetailsPanel.add(boxScroller, gbc);

				gbc.weightx = 0;
				gbc.gridx++;
				JButton applyAllButton = new JButton(applyAllIcon);
				applyAllButton.setPreferredSize(new Dimension(h, h));
				applyAllButton.setToolTipText("Apply this text or pattern to all " + statementIds.length + " selected statements.");
				applyAllButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO
					}
				});
				statementDetailsPanel.add(applyAllButton, gbc);
				gbc.gridx = 0;
				gbc.gridy++;
			} else if (dataType.equals("boolean")) {
				int entry = (Integer) tableModel.getRow(currentIndex).getValues().get(i).getValue();
				boolean val;
				if (entry == 0) {
					val = false;
				} else {
					val = true;
				}
				BooleanButtonPanel buttons = new BooleanButtonPanel();
				if (val == true) {
					buttons.setYes(true);
				} else {
					buttons.setYes(false);
				}

    			components.add(buttons);
    			
				gbc.anchor = GridBagConstraints.EAST;
				statementDetailsPanel.add(label, gbc);
				gbc.insets = new Insets(0, 0, 0, 0);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.weightx = 1;
				gbc.gridx++;
				statementDetailsPanel.add(buttons, gbc);

				gbc.insets = new Insets(3,3,3,3);
				gbc.weightx = 0;
				gbc.gridx++;
				JButton applyAllButton = new JButton(applyAllIcon);
				applyAllButton.setPreferredSize(new Dimension(h, h));
				applyAllButton.setToolTipText("Apply this Boolean value to all " + statementIds.length + " selected statements.");
				applyAllButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO
					}
				});
				statementDetailsPanel.add(applyAllButton, gbc);
				gbc.gridx = 0;
				gbc.gridy++;
			} else if (dataType.equals("integer")) {
				int entry = (Integer) tableModel.getRow(currentIndex).getValues().get(i).getValue();
				JSpinner jsp = new JSpinner();
				jsp.setValue(entry);
    			jsp.setPreferredSize(new Dimension(70, 20));
    			jsp.setEnabled(true);
    			JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
    			jp.add(jsp);

    			components.add(jsp);
    			
				gbc.anchor = GridBagConstraints.EAST;
				statementDetailsPanel.add(label, gbc);
				gbc.insets = new Insets(0, 0, 0, 0);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.weightx = 1;
				gbc.gridx++;
				statementDetailsPanel.add(jp, gbc);

				gbc.insets = new Insets(3, 3, 3, 3);
				gbc.weightx = 0;
				gbc.gridx++;
				JButton applyAllButton = new JButton(applyAllIcon);
				applyAllButton.setPreferredSize(new Dimension(h, h));
				applyAllButton.setToolTipText("Apply this integer value to all " + statementIds.length + " selected statements.");
				applyAllButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO
					}
				});
				statementDetailsPanel.add(applyAllButton, gbc);
				gbc.gridx = 0;
				gbc.gridy++;
			}
		}

		CompoundBorder statementDetailsBorder = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Statement details"));
		statementDetailsPanel.setBorder(statementDetailsBorder);
		contentsPanel.add(statementDetailsPanel, BorderLayout.SOUTH);
		dialogPanel.add(contentsPanel, BorderLayout.NORTH);
		
		/* TODO
		 * - Dna.sql.getStatements() currently returns entities as strings; get them as entities for the combo box!
		 * - While at it, also return integers and booleans in the right way immediately by breaking up the UNION code.
		 * - When loading the dialog, select the first table row.
		 * - Add row selection listener to table and update the right statement upon click in the statement details.
		 * - When pressing previous/next buttons, make selection in the table as well.
		 * - Add a FINAL backup array list of statements to the table model for comparison.
		 * - Implement button listeners in the control panel in the top right corner. In particular, save to database.
		 * - Implement apply-all button listeners. 
		 * - Question with summary before applying changes to all.
		 * - Add select all/none to statement table context menu; add recode button/action to table and/or menu?
		 */
		
		c.add(dialogPanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Update the content components when a new statement is loaded.
	 * 
	 * @param statementIndex The index of the statement that is loaded.
	 */
	@SuppressWarnings("unchecked")
	private void updateContents(int statementIndex) {
		// GUI elements in the upper part of the window
		idField.setText(Integer.toString(tableModel.getRow(currentIndex).getId()));
		indexField.setText((currentIndex + 1) + " / " + tableModel.getRowCount());
		coderComboBox.setSelectedIndex(comboBoxModel.getIndexByCoderId(tableModel.getRow(currentIndex).getCoderId()));
		coderComboBox.repaint();
		previousButton.setEnabled(currentIndex > 0);
		nextButton.setEnabled(currentIndex < tableModel.getRowCount() - 1);
		
		// content combo boxes
		ArrayList<ArrayList<Entity>> entities = Dna.sql.getEntities(variableIds, false); // switch to true to color unused entities in red; but it takes much longer with large databases
		for (int i = 0; i < statementType.getVariables().size(); i++) {
			String dataType = statementType.getVariables().get(i).getDataType();
			if (dataType.equals("short text")) {
				Entity[] entitiesArray = new Entity[entities.get(indexMap.get(i)).size()];
				entitiesArray = entities.get(indexMap.get(i)).toArray(entitiesArray);
				ComboBoxModel<Entity> model = new DefaultComboBoxModel<Entity>(entitiesArray);
				((JComboBox<Entity>) components.get(i)).setModel(model);
				((JComboBox<Entity>) components.get(i)).getModel().setSelectedItem((Entity) tableModel.getRow(currentIndex).getValues().get(i).getValue());
			} else if (dataType.equals("long text")) {
				((JTextArea) components.get(i)).setText((String) tableModel.getRow(currentIndex).getValues().get(i).getValue());
			} else if (dataType.equals("boolean")) {
				((BooleanButtonPanel) components.get(i)).setYes((Integer) tableModel.getRow(currentIndex).getValues().get(i).getValue() != 0);
			} else if (dataType.equals("integer")) {
				((JSpinner) components.get(i)).setValue((Integer) tableModel.getRow(currentIndex).getValues().get(i).getValue());
			}
		}
	}
	
	/**
	 * Save the updated contents of the current statement to the database.
	 */
	/*
	private void saveContents() {
		boolean statementChanged = false;
		for (int i = 0; i < statementType.getVariables().size(); i++) {
			String dataType = statementType.getVariables().get(i).getDataType();
			if (dataType.equals("short text")) {
				@SuppressWarnings("unchecked")
				Entity selectedEntity = (Entity) ((JComboBox<Entity>) components.get(i)).getSelectedItem();
				if (!selectedEntity.equals(currentStatement.getValues().get(i).getValue())) {
					currentStatement.getValues().get(i).setValue(selectedEntity);
					statementChanged = true;
				}
			} else if (dataType.equals("long text")) {
				String text = ((JTextArea) components.get(i)).getText();
				if (!text.equals(currentStatement.getValues().get(i).getValue())) {
					currentStatement.getValues().get(i).setValue(text);
					statementChanged = true;
				}
			} else if (dataType.equals("boolean")) {
				int bool = 0;
				if (((BooleanButtonPanel) components.get(i)).isYes()) {
					bool = 1;
				}
				if (bool != (int) currentStatement.getValues().get(i).getValue()) {
					currentStatement.getValues().get(i).setValue(bool);
					statementChanged = true;
				}
			} else if (dataType.equals("integer")) {
				int integer = (int) ((JSpinner) components.get(i)).getValue();
				if (integer != (int) currentStatement.getValues().get(i).getValue()) {
					currentStatement.getValues().get(i).setValue(integer);
					statementChanged = true;
				}
			}
		}
		if (((Coder) coderComboBox.getSelectedItem()).getId() != currentStatement.getCoderId()) {
			currentStatement.setCoderId(((Coder) coderComboBox.getSelectedItem()).getId());
			statementChanged = true;
		}
		if (statementChanged) {
			Dna.sql.updateStatement(currentStatement.getId(), currentStatement.getValues(), currentStatement.getCoderId());
			LogEvent l = new LogEvent(
					0,
					"Updated statement.",
					"Updated statement " + currentStatement.getId() + " in the database after changes were made in the Statement Recoder dialog window.");
			Dna.logger.log(l);
		}
	}
	*/
	
	/**
	 * Table model for the statement recode table.
	 */
	private class StatementRecodeTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -5971964746106990839L;
		private ArrayList<Statement> statements;
		private StatementType statementType;
		
		public StatementRecodeTableModel(ArrayList<Statement> statements, StatementType statementType) {
			this.statements = statements;
			this.statementType = statementType;
		}

		@Override
		public int getColumnCount() {
			return statementType.getVariables().size() + 2;
		}

		@Override
		public int getRowCount() {
			return this.statements.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return this.statements.get(row).getId();
			} else if (col == statementType.getVariables().size() + 1) {
				Coder coder = new Coder(this.statements.get(row).getCoderId(),
						this.statements.get(row).getCoderName(),
						this.statements.get(row).getCoderColor());
				return coder;
			} else if (col > 0 && col < this.statements.get(row).getValues().size() + 1) {
				if (this.statements.get(row).getValues().get(col - 1).getDataType().equals("short text")) {
					return (String) this.statements.get(row).getValues().get(col - 1).getValue();
				} else if (this.statements.get(row).getValues().get(col - 1).getDataType().equals("long text")) {
					return (String) this.statements.get(row).getValues().get(col - 1).getValue();
				} else if (this.statements.get(row).getValues().get(col - 1).getDataType().equals("boolean")) {
					if ((int) this.statements.get(row).getValues().get(col - 1).getValue() == 1) {
						return "1 - yes";
					} else {
						return "0 - no";
					}
				} else if (this.statements.get(row).getValues().get(col - 1).getDataType().equals("integer")) {
					return (int) this.statements.get(row).getValues().get(col - 1).getValue();
				} else {
					return null;
				}
			} else {
				return null;
			}
		}

		/**
		 * Get a statement specified by a model row index.
		 * 
		 * @param row  The row index.
		 * @return     The statement corresponding to the row index.
		 */
		public Statement getRow(int row) {
			return this.statements.get(row);
		}

		/**
		 * Is the cell editable?
		 * 
		 * @param row  The row index.
		 * @param col  The column index.
		 * @return     Is the cell editable?
		 */
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		/**
		 * Get the name of a column.
		 * 
		 * @param col  The column index.
		 */
		public String getColumnName(int col) {
			if (col == 0) {
				return "ID";
			} else if (col == statementType.getVariables().size() + 1) {
				return "Coder";
			} else {
				return this.statementType.getVariables().get(col - 1).getKey();
			}
		}
		
		/**
		 * Get the class of a column.
		 * 
		 * @param col  The column index.
		 */
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return Integer.class;
			} else if (col == statementType.getVariables().size() + 1) {
				return Coder.class;
			} else if (statementType.getVariables().get(col - 1).getDataType().equals("short text")) {
				return String.class;
			} else if (statementType.getVariables().get(col - 1).getDataType().equals("long text")) {
				return String.class;
			} else if (statementType.getVariables().get(col - 1).getDataType().equals("boolean")) {
				return String.class;
			} else if (statementType.getVariables().get(col - 1).getDataType().equals("integer")) {
				return int.class;
			} else {
				return String.class;
			}
		}
	}
}