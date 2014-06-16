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

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dna.SidebarPanel.StatementCellRenderer;

public class StatementTypeEditor extends JFrame {
	
	private static final long serialVersionUID = -7821187025150495806L;
	StatementTypeContainer stc;
	JTable typeTable, varTable;
	JButton addColorButton;
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
		JButton applyTypeButton = new JButton(applyIcon);
		applyTypeButton.setPreferredSize(new Dimension(18, 18));
		applyTypeButton.setToolTipText("apply changes to this statement type");
		JButton addTypeButton = new JButton(addIcon);
		addTypeButton.setPreferredSize(new Dimension(18, 18));
		addTypeButton.setToolTipText("add this new statement type");
		JButton removeTypeButton = new JButton(removeIcon);
		removeTypeButton.setPreferredSize(new Dimension(18, 18));
		removeTypeButton.setToolTipText("remove this statement type");
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
		
		varTextField = new JTextField(10);
		JButton addVariable = new JButton(addIcon);
		addVariable.setPreferredSize(new Dimension(18, 18));
		addVariable.setToolTipText("add this new variable to the list");
		JButton trashVariable = new JButton(removeIcon);
		trashVariable.setPreferredSize(new Dimension(18, 18));
		trashVariable.setToolTipText("remove this variable from the list");
		JButton acceptVariable = new JButton(applyIcon);
		acceptVariable.setPreferredSize(new Dimension(18, 18));
		acceptVariable.setToolTipText("apply changes made to this variable");
		varButtonPanel.add(varTextField);
		varButtonPanel.add(acceptVariable);
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
	        int typeRow = e.getFirstIndex();
    		((DefaultTableModel)varTable.getModel()).setRowCount(0);
	        StatementType st = stc.get(typeRow);
	        HashMap<String, String> variables = st.getVariables();
	        Iterator<String> keyIterator = variables.keySet().iterator();
	        while (keyIterator.hasNext()){
	    		String key = keyIterator.next();
	    		String value = variables.get(key);
	    		int varRows = varTable.getModel().getRowCount();
	    		String[] newRow = {key, value};
	    		((DefaultTableModel)varTable.getModel()).insertRow(varRows, 
	    				newRow);
	    	}
	        Color col = st.getColor();
	        addColorButton.setForeground(col);
	        
	        addTypeTextField.setText(st.getLabel());
	    }
	}
	
	class VarTableSelectionHandler implements ListSelectionListener {
	    public void valueChanged(ListSelectionEvent e) {
    		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
    		if (!lsm.getValueIsAdjusting()) {
    			int varRow = lsm.getMinSelectionIndex();
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
	    	}
	    }
	}
	
	// TODO: enable/disable components; write listener for buttons
	
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
