package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dna.SidebarPanel.StatementCellRenderer;

public class StatementTypeEditor extends JFrame {
	
	private static final long serialVersionUID = -7821187025150495806L;
	

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
		StatementTypeContainer stc = new StatementTypeContainer(types);
		JTable typeTable = new JTable( stc );
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
		JTextField addTypeTextField = new JTextField(15);
		@SuppressWarnings("serial")
		JButton addColorButton = (new JButton() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(this.getForeground());
                g.fillRect(2, 2, 14, 14);
            }
        });
		addColorButton.setForeground(Color.ORANGE);
		addColorButton.setPreferredSize(new Dimension(18, 18));
		addColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color actualColor = Color.ORANGE;
				Color newColor = JColorChooser.showDialog(StatementTypeEditor.
						this, "choose color...", actualColor);
				if (newColor != null) {
					((JButton) e.getSource()).setForeground(newColor);
				}
			}
		});
		ImageIcon addTypeIcon = new ImageIcon(getClass().getResource(
				"/icons/add.png"));
		ImageIcon removeTypeIcon = new ImageIcon(getClass().getResource(
				"/icons/trash.png"));
		ImageIcon applyTypeIcon = new ImageIcon(getClass().getResource(
				"/icons/accept.png"));
		JButton applyTypeButton = new JButton(applyTypeIcon);
		applyTypeButton.setPreferredSize(new Dimension(18, 18));
		applyTypeButton.setToolTipText("apply changes to this statement type");
		JButton addTypeButton = new JButton(addTypeIcon);
		addTypeButton.setPreferredSize(new Dimension(18, 18));
		addTypeButton.setToolTipText("add this new statement type");
		JButton removeTypeButton = new JButton(removeTypeIcon);
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
		
		// variable table
		String[] columnNames = {"variable name", "data type"};
		DefaultTableModel varTableModel = new DefaultTableModel(columnNames, 0);
		JTable varTable = new JTable(varTableModel);
		varTable.setCellSelectionEnabled(false);
		varTable.setRowSelectionAllowed(false);
		varTable.setColumnSelectionAllowed(false);
		JScrollPane varScrollPane = new JScrollPane(varTable);
		varScrollPane.setPreferredSize(new Dimension(100, 200));
		varTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 50 );
		varTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 50 );
		JPanel middleRightPanel = new JPanel(new BorderLayout());
		middleRightPanel.add(varScrollPane, BorderLayout.NORTH);
		
		// variable buttons
		JPanel varButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		// TODO: reduce dimensions of the three var buttons and add a text 
		// field for the var name and radio buttons or a combo box for the data 
		// types
		// TODO: connect buttons to tables, tables to database, and 
		// enable/disable buttons; also add tooltips
		
		ImageIcon addIcon = new ImageIcon(getClass().getResource(
				"/icons/layout_add.png"));
		ImageIcon editIcon = new ImageIcon(getClass().getResource(
				"/icons/layout_edit.png"));
		ImageIcon removeIcon = new ImageIcon(getClass().getResource(
				"/icons/layout_delete.png"));
		JButton addVariable = new JButton("add...", addIcon);
		JButton editVariable = new JButton("edit...", editIcon);
		JButton removeVariable = new JButton("remove...", removeIcon);
		varButtonPanel.add(addVariable);
		varButtonPanel.add(editVariable);
		varButtonPanel.add(removeVariable);
		
		// assemble right panel
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(middleRightPanel, BorderLayout.NORTH);
		rightPanel.add(varButtonPanel, BorderLayout.SOUTH);
		this.add(rightPanel);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
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
		    return c;
		}
	}
	
}
