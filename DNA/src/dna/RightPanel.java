package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.JXTextField;

import dna.dataStructures.Regex;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementLink;
import dna.dataStructures.StatementType;
import dna.panels.DocStatsPanel;
import dna.panels.RegexPanel;
import dna.panels.SearchPanel;
import dna.renderer.StatementTableModel;

@SuppressWarnings("serial")
public class RightPanel extends JScrollPane {
	public StatementTableModel ssc;
	ContradictionPanel cp;
	public RegexPanel rm;
	public JTable statementTable;
	JScrollPane statementTableScrollPane;
	JPanel statementPanel;
	public StatementFilter statementFilter;
	TableRowSorter<StatementTableModel> sorter;
	JXTextField patternField1, patternField2;
	JComboBox<String> typeComboBox1, typeComboBox2, variableComboBox1, 
	variableComboBox2;
	
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

		statementPanel();
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

	public class StatementCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			int modelRow = table.convertRowIndexToModel(row);
			c.setBackground(Dna.data.getStatementColor(((StatementTableModel)table.getModel()).get(modelRow).getId()));
			return c;
		}
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
	
	private void statementPanel() {
		ssc = new StatementTableModel();
		statementTable = new JTable( ssc );
		statementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statementTableScrollPane = new JScrollPane(statementTable);
		statementTableScrollPane.setPreferredSize(new Dimension(200, 240));
		statementTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 38 );
		statementTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 162 );

		statementTable.getTableHeader().setReorderingAllowed( false );
		statementTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		setRowSorterEnabled(true);

		StatementCellRenderer statementCellRenderer = new 
				StatementCellRenderer();
		statementTable.getColumnModel().getColumn(0).setCellRenderer(
				statementCellRenderer);

		statementFilter = new StatementFilter();

		statementPanel = new JPanel(new BorderLayout());
		statementPanel.add(statementTableScrollPane, BorderLayout.CENTER);
		statementPanel.add(statementFilter, BorderLayout.SOUTH);

		statementTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = -1;
				row = statementTable.rowAtPoint(e.getPoint());
				row = statementTable.convertRowIndexToModel(row);

				if (row > -1) {
					int statementId = ssc.get(row).getId();
					if (statementId != -1) {
						boolean[] b = Dna.data.getActiveStatementPermissions(statementId);
						int docId = Dna.data.getStatement(statementId).getDocumentId();
						int docRow = Dna.dna.gui.documentPanel.documentContainer.getRowIndexById(docId);
						Dna.dna.gui.documentPanel.documentTable.getSelectionModel().
						setSelectionInterval(docRow, docRow);
						Dna.dna.gui.documentPanel.documentTable.scrollRectToVisible(new Rectangle(
								Dna.dna.gui.documentPanel.documentTable.getCellRect(docRow, 0, true)));
						Dna.dna.gui.textPanel.selectStatement(statementId, docId, b[1]);
					}
				}
			}
		});
	}

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
	}
	
	public void updateVariables() {
		variableComboBox1.removeAllItems();
		variableComboBox2.removeAllItems();
		String type = (String) typeComboBox1.getSelectedItem();
		if (type != null && !type.equals("")) {
			HashMap<String, String> variables = Dna.data.getStatementType(type).getVariables();
			Iterator<String> keyIterator = variables.keySet().iterator();
			while (keyIterator.hasNext()){
				String key = keyIterator.next();
				variableComboBox1.addItem(key);
				variableComboBox2.addItem(key);
			}
			try{
				variableComboBox1.setSelectedIndex(0);
			}
			catch (IllegalArgumentException ex){
			}
			try{
				variableComboBox2.setSelectedIndex(0);
			}
			catch (IllegalArgumentException ex){
			}
		}
	}
	
	public void setRowSorterEnabled(boolean enabled) {
		if (enabled == true) {
			sorter = new TableRowSorter<StatementTableModel>(ssc) {
				public void toggleSortOrder(int i) {
					//leave blank; overwritten method makes the table unsortable
				}
			};
			statementTable.setRowSorter(sorter);
		} else {
			statementFilter.showAll.setSelected(true);
			statementTable.setRowSorter(null);
		}
	}
	
	public class StatementFilter extends JPanel {
		JRadioButton showAll;
		JRadioButton showCurrent;
		JRadioButton showFilter;
		
		public StatementFilter() {
			this.setLayout(new BorderLayout());
			JPanel showPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			ButtonGroup showGroup = new ButtonGroup();
			showAll = new JRadioButton("all");
			showAll.setSelected(true);
			showCurrent = new JRadioButton("current");
			showFilter = new JRadioButton("filter:");
			showFilter.setEnabled(false);
			showGroup.add(showAll);
			showGroup.add(showCurrent);
			showGroup.add(showFilter);
			showPanel.add(showAll);
			showPanel.add(showCurrent);
			showPanel.add(showFilter);

			typeComboBox1 = new JComboBox<String>();
			typeComboBox1.setPreferredSize(new Dimension(208, 20));
			typeComboBox1.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					updateVariables();
					filter();
				}
			});

			variableComboBox1 = new JComboBox<String>();
			variableComboBox1.setPreferredSize(new Dimension(100, 20));

			variableComboBox1.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					filter();
				}
			});
			patternField1 = new JXTextField("regex");
			patternField1.setPreferredSize(new Dimension(104, 20));

			variableComboBox2 = new JComboBox<String>();
			variableComboBox2.setPreferredSize(new Dimension(100, 20));
			variableComboBox2.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					filter();
				}
			});
			patternField2 = new JXTextField("regex");
			patternField2.setPreferredSize(new Dimension(104, 20));
			
			toggleEnabled(false);

			JPanel filterPanel0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel0.add(typeComboBox1);
			JPanel filterPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel1.add(variableComboBox1);
			filterPanel1.add(patternField1);
			JPanel filterPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel2.add(variableComboBox2);
			filterPanel2.add(patternField2);

			JPanel filterPanel = new JPanel();
			filterPanel.setLayout(new BoxLayout(filterPanel,BoxLayout.Y_AXIS));
			filterPanel.add(filterPanel0, Component.CENTER_ALIGNMENT);
			filterPanel.add(filterPanel1, Component.CENTER_ALIGNMENT);
			filterPanel.add(filterPanel2, Component.CENTER_ALIGNMENT);
			
			this.add(showPanel, BorderLayout.NORTH);
			this.add(filterPanel, BorderLayout.CENTER);
			
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == showAll) {				
						allFilter();
						toggleEnabled(false);

					} else if (e.getSource() == showCurrent) {
						toggleEnabled(false);
						allFilter();
						documentFilter();
					} else if (e.getSource() == showFilter) {
						toggleEnabled(true);
						filter();

					}
					//if (!(e.getSource() == showAll)) {
					//	Dna.mainProgram.contradictionReporter.clearTree();
					//}
				}
			};
			
			showAll.addActionListener(al);
			showCurrent.addActionListener(al);
			showFilter.addActionListener(al);

			DocumentListener dl = new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					applyFilter();
				}
				public void insertUpdate(DocumentEvent e) {
					applyFilter();
				}
				public void removeUpdate(DocumentEvent e) {
					applyFilter();
				}
				public void applyFilter() {
					filter();
				}
			};

			patternField1.getDocument().addDocumentListener(dl);
			patternField2.getDocument().addDocumentListener(dl);
		}

		public void toggleEnabled(boolean enabled) {
			patternField1.setText("");
			patternField2.setText("");			
			typeComboBox1.setEnabled(enabled);
			variableComboBox1.setEnabled(enabled);
			patternField1.setEnabled(enabled);
			variableComboBox2.setEnabled(enabled);
			patternField2.setEnabled(enabled);
			if (enabled == true) {
				updateStatementTypes();
			}
			this.updateUI();
		}
		
		// used in the coder relation table model to update the statement table when coder relations are changed 
		public void updateFilter() {
			if (showAll.isSelected()) {
				allFilter();
				toggleEnabled(false);
			}
			if (showCurrent.isSelected()) {
				toggleEnabled(false);
				allFilter();
				documentFilter();
			}
			if (showFilter.isSelected()) {
				toggleEnabled(true);
				filter();
			}
		}
		
		public void allFilter() {
			RowFilter<StatementTableModel, Integer> allFilter = new RowFilter<StatementTableModel, Integer>() {
				public boolean include(Entry<? extends StatementTableModel, ? extends Integer> entry) {
					StatementTableModel stcont = entry.getModel();
					Statement st = stcont.get(entry.getIdentifier());
					boolean[] b = Dna.data.getActiveStatementPermissions(st.getId());
					if (b[0] == true && Dna.data.getActiveDocumentPermissions(st.getDocumentId())[0] == true) {
						return true;
					}
					return false;
				}
			};
			if (showAll.isSelected()) {
				sorter.setRowFilter(allFilter);
			}
		}
		
		public void documentFilter() {
			int row = Dna.dna.gui.documentPanel.documentTable.getSelectedRow();
			int docId = -1;
			if (row > -1) {
				docId = Dna.dna.gui.documentPanel.documentContainer.get(row).getId();
			}
			final int documentId = docId;
			
			RowFilter<StatementTableModel, Integer> documentFilter = new RowFilter<StatementTableModel, Integer>() {
				public boolean include(Entry<? extends StatementTableModel, ? extends Integer> entry) {
					StatementTableModel stcont = entry.getModel();
					Statement st = stcont.get(entry.getIdentifier());
					boolean[] b = Dna.data.getActiveStatementPermissions(st.getId());
					if (st.getDocumentId() == documentId && b[0] == true && Dna.data.getActiveDocumentPermissions(st.getDocumentId())[0] == true) {
						return true;
					}
					return false;
				}
			};
			if (showCurrent.isSelected()) {
				sorter.setRowFilter(documentFilter);
			}
		}
		
		private void filter() {
			String fn = Dna.data.getSettings().get("filename");
			if (fn != null && !fn.equals("")) {
				String p1 = patternField1.getText();
				String p2 = patternField2.getText();

				String t1 = (String) typeComboBox1.getSelectedItem();
				String v1 = (String) variableComboBox1.getSelectedItem();
				if (p1 == null) {
					p1 = "";
				}
				if (t1 == null) {
					t1 = "";
				}
				if (v1 == null) {
					v1 = "";
				}

				String v2 = (String) variableComboBox2.getSelectedItem();
				if (p2 == null) {
					p2 = "";
				}
				if (v2 == null) {
					v2 = "";
				}

				if (!t1.equals("") && ! v1.equals("") && !v2.equals("")) {
					ArrayList<Integer> ids1 = new ArrayList<Integer>();
					Pattern p = Pattern.compile(p1);
					for (int i = 0; i < Dna.data.getStatements().size(); i++) {
						String s = (String) Dna.data.getStatements().get(i).getValues().get(v1);
						Matcher m = p.matcher(s);
						boolean b = m.find();
						if (b == true) {
							ids1.add(Dna.data.getStatements().get(i).getId());
						}
					}
					ArrayList<Integer> ids2 = new ArrayList<Integer>();
					p = Pattern.compile(p2);
					for (int i = 0; i < Dna.data.getStatements().size(); i++) {
						String s = (String) Dna.data.getStatements().get(i).getValues().get(v2);
						Matcher m = p.matcher(s);
						boolean b = m.find();
						if (b == true) {
							ids2.add(Dna.data.getStatements().get(i).getId());
						}
					}
					
					final String p1final = p1;
					final String p2final = p2;

					RowFilter<StatementTableModel, Integer> idFilter = new RowFilter<StatementTableModel, Integer>() {
						public boolean include(Entry<? extends StatementTableModel, ? extends Integer> entry) {
							StatementTableModel stcont = entry.getModel();
							Statement st = stcont.get(entry.getIdentifier());
							boolean[] b = Dna.data.getActiveStatementPermissions(st.getId());
							boolean contentMatch;
							if (ids1.contains(st.getId()) && (ids2.contains(st.getId()) || p2final.equals(""))) {
								contentMatch = true;
							} else if (ids2.contains(st.getId()) && (ids1.contains(st.getId()) || p1final.equals(""))) {
								contentMatch = true;
							} else {
								contentMatch = false;
							}

							if (contentMatch == true && b[0] == true && Dna.data.getActiveDocumentPermissions(st.getDocumentId())[0] == true) {
								return true;
							}
							return false;
						}
					};
					if (showFilter.isSelected()) {
						sorter.setRowFilter(idFilter);
					}
				}
			}
		}
	}
}