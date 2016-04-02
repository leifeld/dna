package dna;

import java.awt.Container;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import dna.panels.RegexPanel;
import dna.panels.SearchPanel;
import dna.panels.StatementPanel;

@SuppressWarnings("serial")
public class RightPanel extends JScrollPane {
	public ContradictionPanel cp;
	public RegexPanel rm;
	public StatementPanel statementPanel;
	
	/*
	// SK added for linked statements
    StatementTableModel connectedSC;
    JButton connectButton, clearButton, deleteButton;
    JPanel connectedStatementPanel, viewLinkedStatementPanel;
    JTable connectedStatementTable, linkedTable;
    JTabbedPane linksTabPane;    
    public DefaultTableModel linkedTableModel;
    JScrollPane  linkedTableScrollPane, viewLinkedTableScroll;
    */
	
	public RightPanel() {
		this.setPreferredSize(new Dimension(260, 440));
		JXTaskPaneContainer tpc = new JXTaskPaneContainer();
		this.setColumnHeaderView(tpc);
		tpc.setBackground(this.getBackground());
		
		statementPanel = new StatementPanel();
		
		JXTaskPane statementTaskPane = new JXTaskPane();
		ImageIcon statementIcon = new ImageIcon(getClass().getResource("/icons/comments.png"));
		statementTaskPane.setName("Statements");
		statementTaskPane.setTitle("Statements");
		statementTaskPane.setIcon(statementIcon);
		((Container)tpc).add(statementTaskPane);
		statementTaskPane.add(statementPanel);

		SearchPanel sp = new SearchPanel();
		JXTaskPane searchTaskPane = new JXTaskPane();
		ImageIcon findIcon = new ImageIcon(getClass().getResource("/icons/find.png"));
		searchTaskPane.setName("Search within document");
		searchTaskPane.setTitle("Search within document");
		searchTaskPane.setIcon(findIcon);
		searchTaskPane.setCollapsed(false);
		((Container)tpc).add(searchTaskPane);
		searchTaskPane.add(sp);

		/*
		 * LB.Add: Panel for Finding self-contradictions
		 * Class: ContradictionPanel()
		 */
		/*
		cp = new ContradictionPanel();
		JXTaskPane selfContradictionTaskPane = new JXTaskPane();
		ImageIcon groupIcon = new ImageIcon(getClass().getResource("/icons/group.png"));
		selfContradictionTaskPane.setName("Find self-contradictions");
		selfContradictionTaskPane.setTitle("Find self-contradictions");
		selfContradictionTaskPane.setIcon(groupIcon);
		selfContradictionTaskPane.setCollapsed(true);
		((Container)tpc).add(selfContradictionTaskPane);
		selfContradictionTaskPane.add(cp);        
		*/
		
		// regex panel
		rm = new RegexPanel();
		JXTaskPane highlighterTaskPane = new JXTaskPane();
		ImageIcon tableEditIcon = new ImageIcon(getClass().getResource("/icons/color_swatch.png"));
		highlighterTaskPane.setName("Regex highlighter");
		highlighterTaskPane.setTitle("Regex highlighter");
		highlighterTaskPane.setIcon(tableEditIcon);
		highlighterTaskPane.setCollapsed(true);
		((Container)tpc).add(highlighterTaskPane);
		highlighterTaskPane.add(rm);
		
		/*
        SK add : Panel to save details of Linked statements in database    
        */
		/*
        JXTaskPane saveRecordTaskPane = new JXTaskPane();
        ImageIcon saveIcon = new ImageIcon(getClass().getResource("/icons/table_relationship.png"));
        saveRecordTaskPane.setName("Linked statements");
        saveRecordTaskPane.setTitle("Linked statements");
        saveRecordTaskPane.setIcon(saveIcon);
        createViewLinkedStatementPanel();
        createConnectedStatementPanel();
        ImageIcon ViewLinksIcon = new ImageIcon(getClass().getResource("/icons/table_link.png"));
        ImageIcon createLinksIcon = new ImageIcon(getClass().getResource("/icons/link_add.png"));         
        linksTabPane = new JTabbedPane();
        linksTabPane.addTab("View", ViewLinksIcon, viewLinkedStatementPanel);
        linksTabPane.addTab("Create", createLinksIcon, connectedStatementPanel);
        saveRecordTaskPane.add(linksTabPane);
        saveRecordTaskPane.setCollapsed(true);
        ((Container) tpc).add(saveRecordTaskPane);
        */
	}
	
	/*
	public class LinksCellRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelRow = table.convertRowIndexToModel(row);
            int modelColumn = table.convertColumnIndexToModel(column);
            int statementID = (int) table.getModel().getValueAt(modelRow, modelColumn);
            
            Color col = Dna.data.getStatementColor(statementID);
            //Color col = null;
            //for (Statement ss : Dna.data.getStatements()) {
            //    if (statementID == ss.getId())  {
            //        //col = ss.getColor();
            //    	col = Dna.data.getStatementColor(ss.getId());
            //    }
            //}
            
            if (!isSelected) {
                c.setBackground(col);
            }
            
            return c;
        }
    }
	*/
		
	  /**
     * @author Shraddha 
     * Panel for creating Linked statements
     */
	/*
    private void createConnectedStatementPanel() {
        connectedStatementTable = new JTable(ssc);
        connectedStatementTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        linkedTableScrollPane = new JScrollPane(connectedStatementTable);
        linkedTableScrollPane.setPreferredSize(new Dimension(200, 90));
        connectedStatementTable.getColumnModel().getColumn(0).setPreferredWidth(30);
//                connectedStatementTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 30 );
        connectedStatementTable.getColumnModel().getColumn(1).setPreferredWidth(170);

        connectedStatementTable.getTableHeader().setReorderingAllowed(false);
        connectedStatementTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        setRowSorterEnabled(true);

        StatementCellRenderer statementCellRenderer = new StatementCellRenderer();
        connectedStatementTable.getColumnModel().getColumn(0).setCellRenderer(
                statementCellRenderer);

        // add two buttons
        Icon tickIcon = new ImageIcon(getClass().getResource("/icons/link_go.png"));
        Icon clearIcon = new ImageIcon(getClass().getResource(
                "/icons/arrow_rotate_clockwise.png"));
        connectButton = new JButton("Connect", tickIcon);
        clearButton = new JButton("Clear", clearIcon);
        connectButton.setEnabled(false);
        clearButton.setEnabled(true);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(connectButton);
        buttonPanel.add(clearButton);

        // add task for clear-button
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connectedStatementTable.clearSelection();
                connectButton.setEnabled(false);
            }
        });

        // add task for save-links-button
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //save connection in DB
                int[] selectedRows = connectedStatementTable.getSelectedRows();
                if (selectedRows.length == 2) {
                    int s1 = (int) connectedStatementTable.getModel().getValueAt(selectedRows[0], 0);
                    int s2 = (int) connectedStatementTable.getModel().getValueAt(selectedRows[1], 0);

//                                 System.out.println("selectecd ids: "+ s1 + " and " + s2);
                    //Dna.dna.db.addLinkedStatement(s1, s2);
                    int linkId = Dna.data.generateNewStatementLinkId();
                    Dna.data.getStatementLinks().add(new StatementLink(linkId, s1, s2));
                    
                    updateViewLinksTable();
                    connectedStatementTable.clearSelection();
                    connectButton.setEnabled(false);
                    
                } 
                else {
                    JOptionPane.showMessageDialog(Dna.dna.gui, "For linking, you must select exact 2 statemnts.");
                }

                connectButton.setEnabled(false);
            }
        });

        connectedStatementPanel = new JPanel(new BorderLayout());
//        JLabel createLbl = new JLabel("Select 2 statements.");
//        connectedStatementPanel.add(createLbl, BorderLayout.NORTH);
        connectedStatementPanel.add(linkedTableScrollPane, BorderLayout.CENTER);
        connectedStatementPanel.add(buttonPanel, BorderLayout.SOUTH);

        connectedStatementTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                int selectedRowcount = connectedStatementTable.getSelectedRowCount();

                if (selectedRowcount == 2) {
                    connectButton.setEnabled(true);
                }
                else
                {
                	connectButton.setEnabled(false);
                }
                int row = -1;
                row = connectedStatementTable.rowAtPoint(e.getPoint());
                row = connectedStatementTable.convertRowIndexToModel(row);

                if (row > -1) {
                    int statementId = ssc.get(row).getId();
                    if (statementId != -1) {
                        //int docId = Dna.dna.db.getStatement(statementId).getDocument();
                    	int docId = Dna.data.getStatement(statementId).getDocumentId();
                        int docRow = Dna.dna.gui.documentPanel.documentContainer.getRowIndexById(docId);
                        Dna.dna.gui.documentPanel.documentTable.getSelectionModel().
                                setSelectionInterval(docRow, docRow);
//						Dna.dna.gui.textPanel.selectStatement(statementId, docId);
                        Dna.dna.gui.textPanel.highlightSelectedStatement(statementId);
                    }
                }
            }
        });

    }
	*/
	
    /**
     * @author Shraddha 
     * Panel for creating Linked statements
     */
	/*
     private void createViewLinkedStatementPanel() {

        viewLinkedStatementPanel = new JPanel(new BorderLayout());
        
        String[] tableColumnsName = {"ID", "Statement 1", "Statement 2"};
        linkedTableModel = new DefaultTableModel(null, tableColumnsName) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;//This causes all cells to be not editable
            }
        };

        linkedTable = new JTable();
        linkedTable.setModel(linkedTableModel);
        linkedTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        linkedTable.setVisible(true);
        linkedTable.updateUI();
        
        linkedTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) 
            {
                int selectedRowcount = linkedTable.getSelectedRowCount();
//                        System.out.println("selectecd rows: "+ selectedRowcount);
                if (selectedRowcount > 0) {
                    deleteButton.setEnabled(true);
                }
                int selectedrow = linkedTable.getSelectedRow();
                int selectedCol = linkedTable.getSelectedColumn();
                if (selectedCol <= 0) {
                    selectedCol = 1;

                }

                int statement1Id = (int) linkedTable.getModel().getValueAt(selectedrow, selectedCol);
                if (statement1Id != -1) {
                    //int docId = Dna.dna.db.getStatement(statement1Id).getDocument();
                	int docId = Dna.data.getStatement(statement1Id).getDocumentId();
                    int docRow = Dna.dna.gui.documentPanel.documentContainer.getRowIndexById(docId);
                    Dna.dna.gui.documentPanel.documentTable.getSelectionModel().
                            setSelectionInterval(docRow, docRow);
//						Dna.dna.gui.textPanel.selectStatement(statementId, docId);
                    Dna.dna.gui.textPanel.highlightSelectedStatement(statement1Id);
                }
            }

        });

        // add two buttons
        Icon tickIcon = new ImageIcon(getClass().getResource("/icons/link_delete.png"));
        Icon clearIcon = new ImageIcon(getClass().getResource(
                "/icons/arrow_rotate_clockwise.png"));
        deleteButton = new JButton("Delete", tickIcon);
        clearButton = new JButton("Clear", clearIcon);
        deleteButton.setEnabled(false);
        clearButton.setEnabled(true);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // add task for clear-button
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                linkedTable.clearSelection();
                deleteButton.setEnabled(false);
            }
        });

        // add task for delete-links-button
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //delete connection in DB
                
                int question = JOptionPane.showConfirmDialog(Dna.dna.gui, 
						"Are you sure you want to remove selected links?", 
						"Remove?", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					int[] selectedRows = linkedTable.getSelectedRows();
                                        for (int id : selectedRows) {
                                            int linkID = (int) linkedTable.getModel().getValueAt(id, 0);
                                            //Dna.dna.db.removeLink(linkID);
                                            Dna.data.getStatementLinks().remove(linkID);
                        //                    System.out.println("selectecd rows: " + id + " and id: " + linkID);
                                            updateViewLinksTable();
                                        }
				}
                                
                
                linkedTable.clearSelection();
                deleteButton.setEnabled(false);
            }
        });

        viewLinkedStatementPanel.add(buttonPanel, BorderLayout.SOUTH);
        viewLinkedTableScroll = new JScrollPane(linkedTable);
        viewLinkedTableScroll.setPreferredSize(new Dimension(200, 90));
        viewLinkedTableScroll.setVisible(true);
        viewLinkedStatementPanel.add(viewLinkedTableScroll, BorderLayout.CENTER);
        viewLinkedStatementPanel.revalidate();
        viewLinkedStatementPanel.repaint();
    }

    public void updateViewLinksTable() {
        linkedTableModel.setRowCount(0);
        ArrayList<int[]> data = new ArrayList<>();
        Integer[][] tableData = null;
        if (Dna.dna != null) {
        	for (int i = 0; i < Dna.data.getStatementLinks().size(); i++) {
        		int[] ids = new int[2];
        		ids[0] = Dna.data.getStatementLinks().get(i).getSourceId();
        		ids[1] = Dna.data.getStatementLinks().get(i).getTargetId();
        		data.add(ids);
        	}
            tableData = new Integer[data.size()][3];
//            System.out.println("rs. size " + data.size());
            for (int i = 0; i < data.size(); i++) {

                for (int j = 0; j < 3; j++) {
                    tableData[i][j] = data.get(i)[j];
                }
                linkedTableModel.addRow(tableData[i]);
                linkedTableModel.fireTableStructureChanged();
            }
        }
        LinksCellRenderer linkedCellRenderer = new LinksCellRenderer();
        linkedTable.getColumnModel().getColumn(1).setCellRenderer(linkedCellRenderer);
        linkedTable.getColumnModel().getColumn(2).setCellRenderer(linkedCellRenderer);

        linkedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        linkedTable.setRowSelectionAllowed(true);
        linkedTable.setColumnSelectionAllowed(false);
        linkedTable.setCellSelectionEnabled(true);
        linkedTable.revalidate();
        linkedTable.repaint();

    }
	*/

	//called on click of filter radio button to update statement types
	/*
	public void updateStatementTypes() {
		typeComboBox1.removeAllItems();
		ArrayList<StatementType> types = new ArrayList<StatementType>();
		if (Dna.data.getSettings().get("filename") != null && !Dna.data.getSettings().get("filename").equals("")) {
			types = Dna.data.getStatementTypes();
			for (int i = 0; i < types.size(); i++) {				
				String type = types.get(i).getLabel().toString();
				if(type!=null )
				{
					typeComboBox1.addItem(type.trim());
				}

			}
			try{
				typeComboBox1.setSelectedIndex(0);
			}
			catch (IllegalArgumentException ex){
				typeComboBox1.setSelectedIndex(-1);
			}
		}
		*/
		/*
		//LB.Add: same for the Self-Contradiction Filter
		cp.filterComboBoxType.removeAllItems();
		//if (Dna.dna.db.getFileName() != null && !Dna.dna.db.getFileName().equals("")) {
		if (Dna.data.getSettings().get("filename") != null && !Dna.data.getSettings().get("filename").equals("")) {
			//types = Dna.dna.db.getStatementTypes();
			for (int i = 0; i < types.size(); i++) {
				cp.filterComboBoxType.addItem(types.get(i).getLabel());
			}
			try{
				cp.filterComboBoxType.setSelectedIndex(0);			
			}
			catch (Exception ex){
				cp.filterComboBoxType.setSelectedIndex(-1);	
				cp.goButton.setEnabled(false);
				cp.clearButton.setEnabled(false);
			}
			try{
				cp.filterComboBoxVar1.setSelectedIndex(0);
			}
			catch (IllegalArgumentException ex){
				cp.goButton.setEnabled(false);
				cp.clearButton.setEnabled(false);
			}
			try{
				cp.filterComboBoxVar2.setSelectedIndex(0);
			}
			catch (IllegalArgumentException ex){
				cp.goButton.setEnabled(false);
				cp.clearButton.setEnabled(false);
			}
			cp.goButton.setEnabled(true);
			cp.clearButton.setEnabled(true);
		}else{
			cp.goButton.setEnabled(false);
			cp.clearButton.setEnabled(false);
		}
		*/
	//}
}