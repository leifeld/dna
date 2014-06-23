package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class StatementTypeEditor extends JFrame {
	
	private static final long serialVersionUID = -7821187025150495806L;
	StatementTypeContainer stc;
	JTable typeTable, varTable;
	JButton addColorButton, addTypeButton, applyTypeButton, removeTypeButton, 
			addVariable, trashVariable;
	JTextField addTypeTextField, varTextField;
	JRadioButton stext, ltext, integ, bool;

	public StatementTypeEditor() {
		this.setTitle("Edit statement types...");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon tableAddIcon = new ImageIcon(getClass().getResource(
				"/icons/table_add.png"));
		this.setIconImage(tableAddIcon.getImage());
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		//LEFT PANEL FOR STATEMENT TYPES
		JPanel leftPanel = new JPanel(new BorderLayout());
		ArrayList<StatementType> types = Dna.dna.db.getStatementTypes();
		
		// type panel
		stc = new StatementTypeContainer(types);
		typeTable = new JTable( stc );
		typeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane typeTableScrollPane = new JScrollPane(typeTable);
		typeTableScrollPane.setPreferredSize(new Dimension(200, 230));
		typeTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 155 );
		typeTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 45 );
		typeTable.getTableHeader().setReorderingAllowed( false );
		typeTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		TypeCellRenderer typeCellRenderer = new TypeCellRenderer();
		typeTable.getColumnModel().getColumn(0).setCellRenderer(
				typeCellRenderer);
		typeTable.getColumnModel().getColumn(1).setCellRenderer(
				typeCellRenderer);
		leftPanel.add(typeTableScrollPane, BorderLayout.NORTH);
		
		// add/remove buttons
		JPanel addTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		addTypeTextField = new JTextField(15);

		addTypeTextField.getDocument().addDocumentListener(new 
				DocumentListener() {
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
				String type = addTypeTextField.getText();
				boolean validAdd = false;
				boolean validApply = false;
				if (type.equals("") || type.matches(".*\\s+.*")) {
					validAdd = false;
					validApply = false;
				} else {
					validAdd = true;
				}
				for (int i = 0; i < stc.getRowCount(); i++) {
					if (stc.get(i).getLabel().equals(type)) {
						validAdd = false;
						validApply = true;
					}
				}
				if (validAdd == true) {
					addTypeButton.setEnabled(true);
				} else {
					addTypeButton.setEnabled(false);
				}
				if (validApply == true) {
					applyTypeButton.setEnabled(true);
					removeTypeButton.setEnabled(true);
				} else {
					applyTypeButton.setEnabled(false);
					removeTypeButton.setEnabled(false);
				}
			}
		});
		
		@SuppressWarnings("serial")
		JButton addColorButtonTemp = (new JButton() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(this.getForeground());
                g.fillRect(2, 2, 14, 14);
            }
        });
		addColorButton = addColorButtonTemp;
		addColorButton.setForeground(Color.LIGHT_GRAY);
		addColorButton.setPreferredSize(new Dimension(18, 18));
		addColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color actualColor = ((JButton)e.getSource()).getForeground();
				Color newColor = JColorChooser.showDialog(StatementTypeEditor.
						this, "choose color...", actualColor);
				if (newColor != null) {
					((JButton) e.getSource()).setForeground(newColor);
				}
			}
		});
		ImageIcon addIcon = new ImageIcon(getClass().getResource(
				"/icons/add.png"));
		ImageIcon removeIcon = new ImageIcon(getClass().getResource(
				"/icons/trash.png"));
		ImageIcon applyIcon = new ImageIcon(getClass().getResource(
				"/icons/accept.png"));
		applyTypeButton = new JButton(applyIcon);
		applyTypeButton.setPreferredSize(new Dimension(18, 18));
		applyTypeButton.setToolTipText("apply changes to this statement type");
        applyTypeButton.setEnabled(false);
        applyTypeButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				int dialog = JOptionPane.showConfirmDialog(
						StatementTypeEditor.this, 
						"Modify the statement type and all statements?", 
						"Confirmation required", JOptionPane.OK_CANCEL_OPTION);
				if (dialog == 0) {
					// change in the database
					Color newColor = addColorButton.getForeground();
					int red = newColor.getRed();
					int green = newColor.getGreen();
					int blue = newColor.getBlue();
					String newLabel = addTypeTextField.getText();
					int oldTypeIndex = typeTable.getSelectedRow();
					String oldLabel = stc.get(oldTypeIndex).getLabel();
					Dna.dna.db.changeStatementType(oldLabel, newLabel, red, 
							green, blue);
					
					// change in the table of the current window
					stc.get(oldTypeIndex).setColor(newColor);
					stc.get(oldTypeIndex).setLabel(newLabel);
					typeTable.updateUI();
					typeTable.getSelectionModel().setSelectionInterval(
							oldTypeIndex, oldTypeIndex);
					
					//change the remaining GUI
					Dna.dna.gui.textPanel.paintStatements();
					for (int i = 0; i < Dna.dna.gui.sidebarPanel.ssc.size(); 
							i++) {
						Dna.dna.gui.sidebarPanel.ssc.get(i).setColor(newColor);
						Dna.dna.gui.sidebarPanel.ssc.get(i).setType(newLabel);
					}
					Dna.dna.gui.sidebarPanel.statementTable.updateUI();
					Dna.dna.gui.sidebarPanel.updateStatementTypes();
				}
        	}
        });
        
		addTypeButton = new JButton(addIcon);
		addTypeButton.setPreferredSize(new Dimension(18, 18));
		addTypeButton.setToolTipText("add this new statement type");
        addTypeButton.setEnabled(false);
        addTypeButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				String newLabel = addTypeTextField.getText();
        		int dialog = JOptionPane.showConfirmDialog(
						StatementTypeEditor.this, 
						"Add new statement type \"" + newLabel + "\"?", 
						"Confirmation required", JOptionPane.OK_CANCEL_OPTION);
				if (dialog == 0) {
					// change in the database
					Color newColor = addColorButton.getForeground();
					int red = newColor.getRed();
					int green = newColor.getGreen();
					int blue = newColor.getBlue();
					HashMap<String, String> emptyVar = new HashMap<String, 
							String>();
					Dna.dna.db.insertStatementType(newLabel, red, green, blue, 
							emptyVar);
					
					// change in the table of the current window
					StatementType statementType = new StatementType(newLabel, 
							newColor, emptyVar);
					stc.addStatementType(statementType);
					typeTable.updateUI();
					int newRow = stc.getIndexByLabel(newLabel);
					typeTable.getSelectionModel().setSelectionInterval(
							newRow, newRow);
					Dna.dna.gui.sidebarPanel.updateStatementTypes();
				}
        	}
        });
        
		removeTypeButton = new JButton(removeIcon);
		removeTypeButton.setPreferredSize(new Dimension(18, 18));
		removeTypeButton.setToolTipText("remove this statement type");
        removeTypeButton.setEnabled(false);
        removeTypeButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
				int oldTypeIndex = typeTable.getSelectedRow();
				String oldLabel = stc.get(oldTypeIndex).getLabel();
        		int dialog = JOptionPane.showConfirmDialog(
						StatementTypeEditor.this, 
						"Really remove statement type \"" + oldLabel + 
						"\" and all corresponding statements?", 
						"Confirmation required", JOptionPane.OK_CANCEL_OPTION);
				if (dialog == 0) {
					// change in the database
					Dna.dna.db.removeStatementType(oldLabel);
					
					// change in the tables of the current window
					stc.remove(oldTypeIndex);
					typeTable.updateUI();
		    		((DefaultTableModel)varTable.getModel()).setRowCount(0);
		    		stext.setSelected(true);

					//change the remaining GUI
					Dna.dna.gui.textPanel.paintStatements();
					for (int i = Dna.dna.gui.sidebarPanel.ssc.size() - 1; 
							i >= 0; i--) {
						String currentType = Dna.dna.gui.sidebarPanel.ssc.
								get(i).getType();
						if (currentType.equals(oldLabel)) {
							Dna.dna.gui.sidebarPanel.ssc.remove(i);
						}
					}
					Dna.dna.gui.sidebarPanel.statementTable.updateUI();
					addTypeTextField.setText("");
					Dna.dna.gui.sidebarPanel.updateStatementTypes();
				}
        	}
        });
        
		addTypePanel.add(addColorButton);
		addTypePanel.add(addTypeTextField);
		addTypePanel.add(applyTypeButton);
		addTypePanel.add(addTypeButton);
		addTypePanel.add(removeTypeButton);
		leftPanel.add(addTypePanel, BorderLayout.SOUTH);
		this.add(leftPanel);
		
		
		// RIGHT PANEL FOR VARIABLES
		JPanel rightPanel = new JPanel(new BorderLayout());
		
		// variable table
		String[] columnNames = {"variable name", "data type"};
		DefaultTableModel varTableModel = new DefaultTableModel(columnNames, 0);
		varTable = new JTable(varTableModel);
		varTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane varScrollPane = new JScrollPane(varTable);
		varScrollPane.setPreferredSize(new Dimension(100, 184));
		varTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 50 );
		varTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 50 );
		Class<?> colClass = varTable.getColumnClass(0);
	    varTable.setDefaultEditor(colClass, null);
		colClass = varTable.getColumnClass(1);
	    varTable.setDefaultEditor(colClass, null);
		JPanel middleRightPanel = new JPanel(new BorderLayout());
		middleRightPanel.add(varScrollPane, BorderLayout.NORTH);

		JPanel radioButtonPanel = new JPanel(new GridLayout(2, 2));
		ButtonGroup buttonGroup = new ButtonGroup();
		stext = new JRadioButton("short text");
		ltext = new JRadioButton("long text");
		integ = new JRadioButton("integer");
		bool = new JRadioButton("boolean");
		stext.setEnabled(false);
		ltext.setEnabled(false);
		integ.setEnabled(false);
		bool.setEnabled(false);
		buttonGroup.add(stext);
		buttonGroup.add(ltext);
		buttonGroup.add(integ);
		buttonGroup.add(bool);
		radioButtonPanel.add(stext);
		radioButtonPanel.add(ltext);
		radioButtonPanel.add(integ);
		radioButtonPanel.add(bool);
		
		// variable buttons
		JPanel varButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		varTextField = new JTextField(12);
		varTextField.getDocument().addDocumentListener(new 
				DocumentListener() {
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
				String var = varTextField.getText();
				boolean validAdd = false;
				boolean validApply = false;
				if (var.equals("") || var.matches(".*\\s+.*")) {
					validAdd = false;
					validApply = false;
				} else {
					validAdd = true;
				}
				int selRow = typeTable.getSelectedRow();
				if (selRow > -1) {
					HashMap<String, String> map = Dna.dna.db.getVariables(
							stc.get(selRow).getLabel());
					Object[] types = map.keySet().toArray();
					for (int i = 0; i < types.length; i++) {
						if (types[i].equals(var)) {
							validAdd = false;
							validApply = true;
						}
					}
				} else {
					validAdd = false;
					validApply = false;
				}
				if (!stext.isSelected() && !ltext.isSelected() && 
						!integ.isSelected() && !bool.isSelected()) {
					validAdd = false;
					validApply = false;
				}
				if (validAdd == true) {
					addVariable.setEnabled(true);
				} else {
					addVariable.setEnabled(false);
				}
				if (validApply == true) {
					trashVariable.setEnabled(true);
				} else {
					trashVariable.setEnabled(false);
				}
			}
		});
		
		varTextField.setEnabled(false);
		addVariable = new JButton(addIcon);
		addVariable.setPreferredSize(new Dimension(18, 18));
		addVariable.setToolTipText("add this new variable to the list");
		addVariable.setEnabled(false);
        addVariable.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		// collect required data
        		int typeRow = typeTable.getSelectedRow();
        		String statementType = stc.get(typeRow).getLabel();
        		String dataType = null;
        		if (stext.isSelected()) {
        			dataType = "short text";
        		} else if (ltext.isSelected()) {
        			dataType = "long text";
        		} else if (integ.isSelected()) {
        			dataType = "integer";
        		} else if (bool.isSelected()) {
        			dataType = "boolean";
        		}
        		String newVar = varTextField.getText();

        		// update table in the current window
	    		int varRows = varTable.getModel().getRowCount();
	    		String[] newRow = {newVar, dataType};
	    		((DefaultTableModel)varTable.getModel()).insertRow(
	    				varRows, newRow);
        		
        		// update database
        		Dna.dna.db.addVariable(newVar, dataType, statementType);
        		
        		// update statement type container
        		HashMap<String, String> varMap = stc.get(typeRow).
        				getVariables();
        		varMap.put(newVar, dataType);
        		stc.get(typeRow).setVariables(varMap);
        		Dna.dna.gui.sidebarPanel.updateStatementTypes();
        	}
        });
        
		trashVariable = new JButton(removeIcon);
		trashVariable.setPreferredSize(new Dimension(18, 18));
		trashVariable.setToolTipText("remove this variable from the list");
		trashVariable.setEnabled(false);
        trashVariable.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		// collect data
        		String varName = varTextField.getText();
        		int typeRow = typeTable.getSelectedRow();
        		String statementType = stc.get(typeRow).getLabel();
        		
        		// ask for confirmation
        		int dialog = JOptionPane.showConfirmDialog(
						StatementTypeEditor.this, 
						"Really remove variable \"" + varName + 
						"\" along with all data?", 
						"Confirmation required", JOptionPane.OK_CANCEL_OPTION);
        		
        		if (dialog == 0) {
            		// update statement type container
            		HashMap<String, String> varMap = stc.get(typeRow).
            				getVariables();
            		varMap.remove(varName);
            		stc.get(typeRow).setVariables(varMap);
            		
            		// update database
            		Dna.dna.db.removeVariable(varName, statementType);
            		
            		// update table in the current window
            		for (int i = varTable.getModel().getRowCount() - 1; i >= 0; 
            				i--) {
            			if (varTable.getModel().getValueAt(i, 0).equals(
            					varName)) {
            				((DefaultTableModel)varTable.getModel()).removeRow(
            						i);
            			}
            		}
            		Dna.dna.gui.sidebarPanel.updateStatementTypes();
        		}
        	}
        });
        
		varButtonPanel.add(varTextField);
		varButtonPanel.add(addVariable);
		varButtonPanel.add(trashVariable);
		
		// assemble right panel
		rightPanel.add(middleRightPanel, BorderLayout.NORTH);
		rightPanel.add(radioButtonPanel, BorderLayout.CENTER);
		rightPanel.add(varButtonPanel, BorderLayout.SOUTH);
		this.add(rightPanel);
		
		typeTable.getSelectionModel().addListSelectionListener(new 
				TypeTableSelectionHandler());

		varTable.getSelectionModel().addListSelectionListener(new 
				VarTableSelectionHandler());
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	class TypeTableSelectionHandler implements ListSelectionListener {
	    public void valueChanged(ListSelectionEvent e) {
	    	ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	    	if (!lsm.getValueIsAdjusting()) {
    			int typeRow = lsm.getMinSelectionIndex();
	    		((DefaultTableModel)varTable.getModel()).setRowCount(0);
	    		if (typeRow > -1) {
	    			StatementType st = stc.get(typeRow);
			        HashMap<String, String> variables = st.getVariables();
			        Iterator<String> keyIterator = variables.keySet().
			        		iterator();
			        while (keyIterator.hasNext()){
			    		String key = keyIterator.next();
			    		String value = variables.get(key);
			    		int varRows = varTable.getModel().getRowCount();
			    		String[] newRow = {key, value};
			    		((DefaultTableModel)varTable.getModel()).insertRow(
			    				varRows, newRow);
			    	}
			        Color col = st.getColor();
			        addColorButton.setForeground(col);
					addColorButton.setEnabled(true);
			        
			        addTypeTextField.setText(st.getLabel());
			        addTypeTextField.setEnabled(true);
			        
			        applyTypeButton.setEnabled(true);
			        removeTypeButton.setEnabled(true);
			        
			        varTextField.setEnabled(true);
    				varTextField.setText(varTextField.getText());
    	    		stext.setEnabled(true);
    	    		ltext.setEnabled(true);
    	    		integ.setEnabled(true);
    	    		bool.setEnabled(true);
    	    		stext.setSelected(true);
	    		}
	    	}
	    }
	}
	
	class VarTableSelectionHandler implements ListSelectionListener {
	    public void valueChanged(ListSelectionEvent e) {
    		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
    		if (!lsm.getValueIsAdjusting()) {
    			int varRow = lsm.getMinSelectionIndex();
    			if (varRow > -1) {
    				String varName = (String) varTable.getValueAt(varRow, 0);
    	    		varTextField.setText(varName);
    	    		String dataType = (String) varTable.getValueAt(varRow, 1);
    	        	if (dataType.equals("short text")) {
    	        		stext.setSelected(true);
    	        	} else if (dataType.equals("long text")) {
    	        		ltext.setSelected(true);
    	        	} else if (dataType.equals("integer")) {
    	        		integ.setSelected(true);
    	        	} else if (dataType.equals("boolean")) {
    	        		bool.setSelected(true);
    	        	}
    	        	
    	    		stext.setEnabled(true);
    	    		ltext.setEnabled(true);
    	    		integ.setEnabled(true);
    	    		bool.setEnabled(true);
    		        
    		        varTextField.setEnabled(true);
    				trashVariable.setEnabled(true);
    			} else {
    				stext.setSelected(true);
    				varTextField.setText("");
    			}
	    	}
	    }
	}
	
	public class TypeCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row, 
				int column) {
			Component c = super.getTableCellRendererComponent(table, value, 
					isSelected, hasFocus, row, column);
			Color col = ((StatementTypeContainer)table.getModel()).get(row).
					getColor();
			c.setBackground(col);
		    if (isSelected && column != 1) {
		    	UIDefaults defaults = javax.swing.UIManager.getDefaults();
		    	Color bg = defaults.getColor("List.selectionBackground");
		    	c.setBackground(bg);
		    }
		    return c;
		}
	}
}
