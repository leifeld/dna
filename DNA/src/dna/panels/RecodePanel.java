package dna.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import dna.Dna;
import dna.dataStructures.AttributeVector;
import dna.dataStructures.StatementType;
import dna.renderer.StatementTypeComboBoxModel;
import dna.renderer.StatementTypeComboBoxRenderer;

@SuppressWarnings("serial")
public class RecodePanel extends JPanel {
	
	JToggleButton attributeToggleButton, searchToggleButton, recodeToggleButton;
	public JComboBox<StatementType> typeComboBox;
	public JComboBox<String> entryBox;
	public JTable table;
	public DefaultTableModel tableModel;
	public JButton applyButton, resetButton;
	JList<String> uniqueList;
	public DefaultListModel<String> listModel;
	
	public RecodePanel() {
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
				Dna.gui.textPanel.bottomCardPanel.searchWindow.searchToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.recodeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.attributeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.attributePanel.searchToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.attributePanel.recodeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.attributePanel.attributeToggleButton.setSelected(false);
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
				Dna.gui.textPanel.bottomCardPanel.searchWindow.searchToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.recodeToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.attributeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.attributePanel.searchToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.attributePanel.recodeToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.attributePanel.attributeToggleButton.setSelected(false);
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
				Dna.gui.textPanel.bottomCardPanel.searchWindow.searchToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.recodeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.searchWindow.attributeToggleButton.setSelected(true);
				Dna.gui.textPanel.bottomCardPanel.attributePanel.searchToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.attributePanel.recodeToggleButton.setSelected(false);
				Dna.gui.textPanel.bottomCardPanel.attributePanel.attributeToggleButton.setSelected(true);
			}
		});
		JPanel switchPanel = new JPanel();
		switchPanel.add(searchToggleButton);
		switchPanel.add(recodeToggleButton);
		switchPanel.add(attributeToggleButton);
		topPanel.add(switchPanel, BorderLayout.EAST);
		
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
					tableModel.setRowCount(0);
					listModel.clear();
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
					updateTable();
				}
			}
		});
		typeComboBox.setPreferredSize(new Dimension(200, 30));
		typeComboBox.setEnabled(false);
		entryBox = new JComboBox<String>();
		entryBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateTable();
			}
		});
		entryBox.setPreferredSize(new Dimension(200, 30));
		entryBox.setEnabled(false);
		applyButton = new JButton("save", new ImageIcon(getClass().getResource("/icons/accept.png")));
		applyButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Dna.gui, 
						"Are you sure you want to recode all values that have been changed?", "Confirmation", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					int count = recode();
					updateTable();
					JOptionPane.showMessageDialog(Dna.gui, count + " statements have been updated.");
				}
			}
		});
		applyButton.setEnabled(false);
		resetButton = new JButton("reset", new ImageIcon(getClass().getResource("/icons/cancel.png")));
		resetButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Dna.gui, 
						"Are you sure you want to revert all changes you have made?", "Confirmation", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					updateTable();
				}
			}
		});
		resetButton.setEnabled(false);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(typeComboBox);
		buttonPanel.add(entryBox);
		buttonPanel.add(applyButton);
		buttonPanel.add(resetButton);
		topPanel.add(buttonPanel, BorderLayout.WEST);
		this.add(topPanel, BorderLayout.NORTH);

		String[] columnNames = {"original value", "edited value"};
		tableModel = new DefaultTableModel( columnNames, 0 ) {
		    @Override
		    public boolean isCellEditable(int row, int column) {
		        if (column == 0) {
		        	return false;
		        } else {
		        	return true;
		        }
		    }
		};
		table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(300, 150));
		
		table.getModel().addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				listModel.clear();
				for (int i = 0; i < tableModel.getRowCount(); i++) {
					if (!listModel.contains((String) tableModel.getValueAt(i, 1))) {
						listModel.addElement((String) tableModel.getValueAt(i, 1));
					}
				}
			}
			
		});
		
		listModel = new DefaultListModel<String>();
		uniqueList = new JList<String>(listModel);
		JScrollPane listScroller = new JScrollPane(uniqueList);
		listScroller.setPreferredSize(new Dimension(50, 150));
		uniqueList.setEnabled(false);
		
		JPanel tablePanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.7;
		gbc.gridx = 0;
		gbc.gridy = 0;
		tablePanel.add(scrollPane, gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.5;
		tablePanel.add(listScroller, gbc);
		this.add(tablePanel, BorderLayout.CENTER);
	}
	
	/**
	 * This function is called to update the combo boxes at the top of the recode panel. For example from the Dna class when a database is closed.
	 */
	public void updateBoxes() {
		typeComboBox.setModel(new DefaultComboBoxModel<StatementType>());
		entryBox.setModel(new DefaultComboBoxModel<String>(new String[0]));
		tableModel.setRowCount(0);
		listModel.clear();
		StatementTypeComboBoxModel model = new StatementTypeComboBoxModel();
		typeComboBox.setModel(model);
	}
	
	public void updateTable() {
		String var = (String) entryBox.getSelectedItem();
		int statementTypeId = ((StatementType) typeComboBox.getModel().getSelectedItem()).getId();
		String[] entries = Dna.data.getStringEntries(statementTypeId, var);
		tableModel.setRowCount(0);
		for (int i = 0; i < entries.length; i++) {
			String[] data = new String[2];
			data[0] = entries[i];
			data[1] = entries[i];
			tableModel.addRow(data);
		}
		applyButton.setEnabled(true);
		resetButton.setEnabled(true);
	}
	
	public int recode() {
		int count = 0;
		int statementTypeId = ((StatementType) typeComboBox.getSelectedItem()).getId();
		String variable = (String) entryBox.getSelectedItem();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			String original = (String) tableModel.getValueAt(i, 0);
			String edited = (String) tableModel.getValueAt(i, 1);
			if (!original.equals(edited)) {  // if the entry on the left and on the right of the recode table don't match...
				for (int j = 0; j < Dna.data.getStatements().size(); j++) {  // ...go through the statements and change all instances
					String value = (String) Dna.data.getStatements().get(j).getValues().get(variable);
					if (value != null && value.equals(original)) {
						int statementId = Dna.data.getStatements().get(j).getId();
						Dna.dna.updateVariable(statementId, statementTypeId, edited, variable);
						count++;
					}
				}
				int avIndexOriginal = Dna.data.getAttributeIndex(original, variable, statementTypeId);
				AttributeVector avOriginal = Dna.data.getAttributes().get(avIndexOriginal);
				int avIndexTarget = Dna.data.getAttributeIndex(edited, variable, statementTypeId);
				if (avIndexTarget == -1) {  // edited version does not exist yet, so just rename it
					Dna.dna.updateAttributeValue(avIndexOriginal, edited);
				} else {  // already exists
					AttributeVector avTarget = Dna.data.getAttributes().get(avIndexTarget);
					ArrayList<Integer> indicesToDelete = new ArrayList<Integer>();
					if ((avOriginal.getType().equals("")  // both are identical or the original one is empty, so delete the original one
							&& avOriginal.getAlias().equals("") 
							&& avOriginal.getNotes().equals("") 
							&& avOriginal.getColor().equals(Color.BLACK)) 
							|| (avOriginal.getType().equals(avTarget.getType()) 
							&& avOriginal.getAlias().equals(avTarget.getAlias()) 
							&& avOriginal.getNotes().equals(avTarget.getNotes()) 
							&& avOriginal.getColor().equals(avTarget.getColor()) )) {
						indicesToDelete.add(avIndexOriginal);
					} else if (avTarget.getType().equals("")  // target is empty while original is not, so delete target and rename original
							&& avTarget.getAlias().equals("") 
							&& avTarget.getNotes().equals("") 
							&& avTarget.getColor().equals(Color.BLACK) 
							&& (!avOriginal.getType().equals("") 
							|| !avOriginal.getAlias().equals("") 
							|| !avOriginal.getNotes().equals("") 
							|| !avOriginal.getColor().equals(Color.BLACK) )) {
						Dna.dna.updateAttributeValue(avIndexOriginal, edited);
						indicesToDelete.add(avIndexTarget);
					} else {  // more complicated: both contain some contents, so keep both
						System.out.println("Case 3: " + avTarget.getValue() + avOriginal.getValue());
						// nothing to do
					}
					if (indicesToDelete.size() > 0) {  // now delete those that were marked earlier
						for (int j = indicesToDelete.size() - 1; j > -1; j--) {
							Dna.dna.deleteAttributeVector(indicesToDelete.get(j));
						}
					}
				}
			}
		}
		return count;
	}
}
