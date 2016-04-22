package dna.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

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
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import dna.Dna;
import dna.dataStructures.StatementType;
import dna.renderer.StatementTypeComboBoxModel;
import dna.renderer.StatementTypeComboBoxRenderer;

public class RecodePanel extends JPanel {
	
	JToggleButton searchToggleButton, recodeToggleButton;
	public JComboBox typeComboBox, entryBox;
	public JTable table;
	DefaultTableModel tableModel;
	JButton applyButton, resetButton;
	JList<String> uniqueList;
	DefaultListModel<String> listModel;
	
	@SuppressWarnings("serial")
	public RecodePanel() {
		this.setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(new EmptyBorder(0,5,0,5));
		
		// toggle buttons, top right corner
		searchToggleButton = new JToggleButton(new ImageIcon(getClass().getResource("/icons/find.png")));
		searchToggleButton.setPreferredSize(new Dimension(36, 18));
		searchToggleButton.setSelected(false);
		searchToggleButton.setName("searchToggle");
		searchToggleButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout) Dna.dna.gui.textPanel.bottomCardPanel.getLayout();
				cl.show(Dna.dna.gui.textPanel.bottomCardPanel, "searchPanel");
				searchToggleButton.setSelected(true);
				recodeToggleButton.setSelected(false);
				Dna.dna.gui.textPanel.bottomCardPanel.searchWindow.searchToggleButton.setSelected(true);
				Dna.dna.gui.textPanel.bottomCardPanel.searchWindow.recodeToggleButton.setSelected(false);
			}
		});
		recodeToggleButton = new JToggleButton(new ImageIcon(getClass().getResource("/icons/table_edit.png")));
		recodeToggleButton.setPreferredSize(new Dimension(36, 18));
		recodeToggleButton.setSelected(true);
		recodeToggleButton.setName("recodeToggle");
		recodeToggleButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout) Dna.dna.gui.textPanel.bottomCardPanel.getLayout();
				cl.show(Dna.dna.gui.textPanel.bottomCardPanel, "recodePanel");
				searchToggleButton.setSelected(false);
				recodeToggleButton.setSelected(true);
				Dna.dna.gui.textPanel.bottomCardPanel.searchWindow.searchToggleButton.setSelected(false);
				Dna.dna.gui.textPanel.bottomCardPanel.searchWindow.recodeToggleButton.setSelected(true);
			}
		});
		JPanel switchPanel = new JPanel();
		switchPanel.add(searchToggleButton);
		switchPanel.add(recodeToggleButton);
		topPanel.add(switchPanel, BorderLayout.EAST);
		
		// combo boxes and buttons, top left
		StatementTypeComboBoxRenderer renderer = new StatementTypeComboBoxRenderer();
		StatementTypeComboBoxModel model = new StatementTypeComboBoxModel();
		typeComboBox = new JComboBox(model);
		typeComboBox.setRenderer(renderer);
		typeComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				ArrayList<String> variables = new ArrayList<String>();
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
				entryBox.setModel(new DefaultComboBoxModel(varArray));
				updateTable();
			}
		});
		typeComboBox.setPreferredSize(new Dimension(200, 30));
		entryBox = new JComboBox();
		entryBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateTable();
			}
		});
		entryBox.setPreferredSize(new Dimension(200, 30));
		applyButton = new JButton("save", new ImageIcon(getClass().getResource("/icons/accept.png")));
		applyButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Dna.dna.gui, 
						"Are you sure you want to recode all values that have been changed?", "Confirmation", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					int count = recode();
					updateTable();
					JOptionPane.showMessageDialog(Dna.dna.gui, count + " statements have been updated.");
				}
			}
		});
		applyButton.setEnabled(false);
		resetButton = new JButton("reset", new ImageIcon(getClass().getResource("/icons/cancel.png")));
		resetButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Dna.dna.gui, 
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
			listModel.addElement(entries[1]);
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
			if (!original.equals(edited)) {
				for (int j = 0; j < Dna.data.getStatements().size(); j++) {
					int statementId = Dna.data.getStatements().get(j).getId();
					String value = (String) Dna.data.getStatements().get(j).getValues().get(variable);
					System.out.println(value + " - " + original);
					if (value.equals(original)) {
						Dna.dna.updateVariable(statementId, statementTypeId, edited, variable);
						count++;
					}
				}
			}
		}
		return count;
	}
}
