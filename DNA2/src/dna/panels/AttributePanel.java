package dna.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTextField;

import dna.Dna;
import dna.dataStructures.AttributeVector;
import dna.dataStructures.StatementType;
import dna.renderer.AttributeCellRenderer;
import dna.renderer.AttributeTableModel;
import dna.renderer.ColorChooserEditor;
import dna.renderer.StatementTypeComboBoxModel;
import dna.renderer.StatementTypeComboBoxRenderer;

@SuppressWarnings("serial")
public class AttributePanel extends JPanel {
	
	JToggleButton attributeToggleButton, searchToggleButton, recodeToggleButton;
	public AttributeTableModel attributeTableModel;
	public JTable attributeTable;
	public JComboBox<StatementType> typeComboBox;
	public JComboBox<String> entryBox;
	public JButton cleanUpButton, addMissingButton;
	TableRowSorter<AttributeTableModel> sorter;
	AttributeCellRenderer renderer;
	
	public AttributePanel() {
		this.setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(new EmptyBorder(0,5,0,5));
		
		// toggle buttons, top right corner
		searchToggleButton = new JToggleButton(new ImageIcon(getClass().getResource("/icons/find.png")));
		searchToggleButton.setPreferredSize(new Dimension(24, 18));
		searchToggleButton.setSelected(false);
		searchToggleButton.setName("searchToggle");
		searchToggleButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout) Dna.gui.textPanel.bottomCardPanel.getLayout();
				cl.show(Dna.gui.textPanel.bottomCardPanel, "searchPanel");
				searchToggleButton.setSelected(true);
				recodeToggleButton.setSelected(false);
				attributeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.recodePanel.searchToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.recodePanel.recodeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.recodePanel.attributeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.searchToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.recodeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.attributeToggleButton.setSelected(false);
			}
		});
		recodeToggleButton = new JToggleButton(new ImageIcon(getClass().getResource("/icons/table_edit.png")));
		recodeToggleButton.setPreferredSize(new Dimension(24, 18));
		recodeToggleButton.setSelected(true);
		recodeToggleButton.setName("recodeToggle");
		recodeToggleButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout) Dna.gui.textPanel.bottomCardPanel.getLayout();
				cl.show(Dna.gui.textPanel.bottomCardPanel, "recodePanel");
				searchToggleButton.setSelected(false);
				recodeToggleButton.setSelected(true);
				attributeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.recodePanel.searchToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.recodePanel.recodeToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.recodePanel.attributeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.searchToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.recodeToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.attributeToggleButton.setSelected(false);
			}
		});
		attributeToggleButton = new JToggleButton(new ImageIcon(getClass().getResource("/icons/tag_purple.png")));
		attributeToggleButton.setPreferredSize(new Dimension(24, 18));
		attributeToggleButton.setSelected(false);
		attributeToggleButton.setName("attributeToggle");
		attributeToggleButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout) Dna.gui.textPanel.bottomCardPanel.getLayout();
				cl.show(Dna.gui.textPanel.bottomCardPanel, "attributePanel");
				searchToggleButton.setSelected(false);
				recodeToggleButton.setSelected(false);
				attributeToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.recodePanel.searchToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.recodePanel.recodeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.recodePanel.attributeToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.searchToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.recodeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.attributeToggleButton.setSelected(true);
			}
		});
		JPanel switchPanel = new JPanel();
		switchPanel.add(searchToggleButton);
		switchPanel.add(recodeToggleButton);
		switchPanel.add(attributeToggleButton);
		topPanel.add(switchPanel, BorderLayout.EAST);
		
		attributeTableModel = new AttributeTableModel();
		attributeTable = new JTable(attributeTableModel);
		attributeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane sp = new JScrollPane(attributeTable);
		sp.setPreferredSize(new Dimension(300, 150));

		attributeTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 150 );
		attributeTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 25 );
		attributeTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 150 );
		attributeTable.getColumnModel().getColumn( 3 ).setPreferredWidth( 150 );
		attributeTable.getColumnModel().getColumn( 4 ).setPreferredWidth( 150 );
		renderer = new AttributeCellRenderer();
		attributeTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		attributeTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		attributeTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		attributeTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
		attributeTable.getColumnModel().getColumn(4).setCellRenderer(renderer);
		ColorChooserEditor cce = new ColorChooserEditor();
		attributeTable.getColumnModel().getColumn(1).setCellEditor(cce);
		
		// combo boxes and buttons, top left
		StatementTypeComboBoxRenderer renderer = new StatementTypeComboBoxRenderer();
		StatementTypeComboBoxModel model = new StatementTypeComboBoxModel();
		typeComboBox = new JComboBox<StatementType>(model);
		typeComboBox.setRenderer(renderer);
		typeComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				ArrayList<String> variables = new ArrayList<String>();
				if (typeComboBox.getSelectedIndex() == -1) {
					entryBox.setModel(new DefaultComboBoxModel<String>(new String[0]));
				} else {
					Iterator<String> it = ((StatementType) typeComboBox.getSelectedItem()).getVariables().keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						String type = ((StatementType) typeComboBox.getSelectedItem()).getVariables().get(key);
						if (type.equals("short text") || type.equals("long text")) {
							variables.add(key);
						}
					}
					String[] varArray = new String[variables.size()];
					varArray = variables.toArray(varArray);
					entryBox.setModel(new DefaultComboBoxModel<String>(varArray));
					variableFilter((String) entryBox.getSelectedItem(), ((StatementType) typeComboBox.getSelectedItem()).getId());
				}
			}
		});
		typeComboBox.setPreferredSize(new Dimension(200, 30));
		typeComboBox.setEnabled(false);
		entryBox = new JComboBox<String>();
		entryBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				variableFilter((String) entryBox.getSelectedItem(), ((StatementType) typeComboBox.getSelectedItem()).getId());
			}
		});
		entryBox.setPreferredSize(new Dimension(200, 30));
		entryBox.setEnabled(false);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(typeComboBox);
		buttonPanel.add(entryBox);
		topPanel.add(buttonPanel, BorderLayout.WEST);
		
		sorter = new TableRowSorter<AttributeTableModel>(attributeTableModel);
		attributeTable.setRowSorter(sorter);
		emptyFilter();
		
		//panel with field and buttons at the bottom
		JPanel lowerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JXTextField newField = new JXTextField("New value...");
		newField.setColumns(14);
		lowerPanel.add(newField);
		Icon addIcon = new ImageIcon(getClass().getResource("/icons/table_row_insert.png"));
		JButton addButton = new JButton("Add entry", addIcon);
		addButton.setPreferredSize(new Dimension(addButton.getPreferredSize().width, newField.getPreferredSize().height));
		addButton.setEnabled(false);
		lowerPanel.add(addButton);
		Icon deleteIcon = new ImageIcon(getClass().getResource("/icons/table_row_delete.png"));
		JButton deleteButton = new JButton("Delete entry", deleteIcon);
		deleteButton.setPreferredSize(new Dimension(deleteButton.getPreferredSize().width, newField.getPreferredSize().height));
		deleteButton.setEnabled(false);
		lowerPanel.add(deleteButton);
		Icon cleanUpIcon = new ImageIcon(getClass().getResource("/icons/table_delete.png"));
		cleanUpButton = new JButton("Clean up", cleanUpIcon);
		cleanUpButton.setPreferredSize(new Dimension(cleanUpButton.getPreferredSize().width, newField.getPreferredSize().height));
		cleanUpButton.setEnabled(false);
		lowerPanel.add(cleanUpButton);
		Icon addMissingIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
		addMissingButton = new JButton("Add missing", addMissingIcon);
		addMissingButton.setPreferredSize(new Dimension(addMissingButton.getPreferredSize().width, newField.getPreferredSize().height));
		addMissingButton.setEnabled(false);
		lowerPanel.add(addMissingButton);

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
				if (typeComboBox.getSelectedItem() != null) {
					int statementTypeId = ((StatementType) typeComboBox.getSelectedItem()).getId();
					String variable = (String) entryBox.getSelectedItem();
					boolean add = true;
					if (Dna.dna.sql == null) {
						add = false;
					}
					if (newValue.equals("")) {
						add = false;
					}
					for (int i = 0; i < Dna.data.getAttributes().size(); i++) {
						AttributeVector av = Dna.data.getAttributes().get(i);
						if (av.getVariable().equals(variable) && av.getStatementTypeId() == statementTypeId && av.getValue().equals(newValue)) {
							add = false;
							break;
						}
					}
					if (add == false) {
						addButton.setEnabled(false);
					} else {
						addButton.setEnabled(true);
					}
				}
			}
		});

		// check if selected row is in dataset and disable delete button if necessary
		attributeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				int viewRow = attributeTable.getSelectedRow();
				if (viewRow > -1 && attributeTable.getRowCount() > 1) {
					int modelRow = attributeTable.convertRowIndexToModel(viewRow);
					if (!attributeTableModel.get(modelRow).getValue().equals("")) {
						if (attributeTableModel.get(modelRow).isInDataset()) {
							deleteButton.setEnabled(false);
						} else {
							deleteButton.setEnabled(true);
						}
					} else {
						deleteButton.setEnabled(false);
					}
				} else {
					deleteButton.setEnabled(false);
				}
			}
		});
		
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = newField.getText();
				int id = Dna.data.generateNewId("attributes");
				int statementTypeId = ((StatementType) typeComboBox.getSelectedItem()).getId();
				String variable = (String) entryBox.getSelectedItem();
				AttributeVector av = new AttributeVector(id, value, new Color(0, 0, 0), "", "", "", "", statementTypeId, variable);
				attributeTableModel.addRow(av);
				newField.setText("");
				addButton.setEnabled(false);
			}
		});
		
		deleteButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int modelRow = attributeTable.convertRowIndexToModel(attributeTable.getSelectedRow());
				if (!attributeTableModel.get(modelRow).isInDataset() && attributeTable.getRowCount() > 1 
						&& !attributeTableModel.get(modelRow).getValue().equals("")) {
					attributeTableModel.deleteRow(modelRow);
				} else {
					System.err.println("Entry cannot be deleted because it is still present in the dataset.");
				}
			}
		});
		
		cleanUpButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "Are you sure you want to delete all values that have never \n"
						+ "been coded (= all rows colored in red) for this variable?";
				if (typeComboBox.getSelectedItem() != null && attributeTable.getRowCount() > 1) {
					int dialog = JOptionPane.showConfirmDialog(Dna.gui, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						int count = 0;
						int statementTypeId = ((StatementType) typeComboBox.getSelectedItem()).getId();
						String variable = (String) entryBox.getSelectedItem();
						for (int i = Dna.data.getAttributes().size() - 1; i > -1 ; i--) {
							AttributeVector av = Dna.data.getAttributes().get(i);
							if (variable.equals(av.getVariable()) && statementTypeId == av.getStatementTypeId() && av.isInDataset() == false 
									&& !av.getValue().equals("")) {
								attributeTableModel.deleteRow(i);
								count++;
							}
						}
						JOptionPane.showMessageDialog(Dna.gui, count + " rows were deleted.");
					}
				}
			}
		});
		
		addMissingButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "Are you sure you want to go through all statements and check \n"
						+ "for missing attribute rows? This can take a while.";
				int dialog = JOptionPane.showConfirmDialog(Dna.gui, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
				if (dialog == 0) {
					startMissingThread();
				}
			}
		});
		
		this.add(topPanel, BorderLayout.NORTH);
		this.add(sp, BorderLayout.CENTER);
		this.add(lowerPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Start a new AttributeInserter thread to fill in missing attribute vectors
	 */
	public void startMissingThread() {
		Thread addMissingThread = new Thread( new AttributeInserter(), "Inserting missing attributes..." );
		addMissingThread.start();
	}
	
	private void variableFilter(String variable, int statementTypeId) {
		RowFilter<AttributeTableModel, Integer> attributeFilter = new RowFilter<AttributeTableModel, Integer>() {
			public boolean include(Entry<? extends AttributeTableModel, ? extends Integer> entry) {
				AttributeTableModel atm = entry.getModel();
				AttributeVector av = atm.get(entry.getIdentifier());
				int stId = av.getStatementTypeId();
				String var = av.getVariable();
				if (variable.equals(var) && stId == statementTypeId) {
					return true;
				} else {
					return false;
				}
			}
		};
		sorter.setRowFilter(attributeFilter);
	}
	
	private void emptyFilter() {
		RowFilter<AttributeTableModel, Integer> attributeFilter = new RowFilter<AttributeTableModel, Integer>() {
			public boolean include(Entry<? extends AttributeTableModel, ? extends Integer> entry) {
				return false;
			}
		};
		sorter.setRowFilter(attributeFilter);
	}
	
	/**
	 * Add missing attribute vectors to attribute table; run as separate thread.
	 * 
	 * @author Philip Leifeld
	 *
	 */
	class AttributeInserter implements Runnable {
		
		ProgressMonitor progressMonitor;
		
		public AttributeInserter() {
			// nothing to do here in the constructor
		}
		
		public void run() {
			progressMonitor = new ProgressMonitor(Dna.gui, "Adding missing attributes...", "", 0, Dna.data.getStatements().size() - 1);
			progressMonitor.setMillisToDecideToPopup(1);
			addMissingButton.setEnabled(false);
			
			int statementTypeId;
			ArrayList<String> vars;
			String value;
			int id;
			AttributeVector avk, av;
			ArrayList<AttributeVector> al = new ArrayList<AttributeVector>();
			for (int i = 0; i < Dna.data.getStatements().size(); i++) {
				if (progressMonitor.isCanceled()) {
					addMissingButton.setEnabled(true);
					break;
				}
				statementTypeId = Dna.data.getStatements().get(i).getStatementTypeId();
				vars = Dna.data.getStatementTypeById(statementTypeId).getVariablesByType("short text");
				if (vars.size() > 0) {
					for (int j = 0; j < vars.size(); j++) {
						value = (String) Dna.data.getStatements().get(i).getValues().get(vars.get(j));
						boolean exists = false;
						for (int k = 0; k < Dna.data.getAttributes().size(); k++) {
							avk = Dna.data.getAttributes().get(k);
							if (avk.getValue().equals(value) && avk.getVariable().equals(vars.get(j)) && avk.getStatementTypeId() == statementTypeId) {
								exists = true;
								break;
							}
						}
						if (exists == false) {
							id = Dna.data.generateNewId("attributes");
							av = new AttributeVector(id, value, new Color(0, 0, 0), "", "", "", "", statementTypeId, vars.get(j));
							Dna.data.getAttributes().add(av);
							al.add(av);
						}
					}
				}
				progressMonitor.setProgress(i);
			}
			Dna.dna.sql.insertAttributeVectors(al);
			attributeTableModel.sort();
			addMissingButton.setEnabled(true);
		}
	}
}
