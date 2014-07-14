package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.ProgressMonitor;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.JXTextField;


class SidebarPanel extends JScrollPane {
	
	private static final long serialVersionUID = 1L;
	
	SidebarStatementContainer ssc;
	ContradictionPanel cp;
	JTable statementTable;
	JScrollPane statementTableScrollPane;
	JPanel statementPanel;
	StatementFilter statementFilter;
	TableRowSorter<SidebarStatementContainer> sorter;
	JXTextField patternField1, patternField2;
	JComboBox typeComboBox1,  variableComboBox1, variableComboBox2;
		// LB.Change: JComboBox<String> typeComboBox1, typeComboBox2, variableComboBox1, 

	
	public SidebarPanel() {
		this.setPreferredSize(new Dimension(260, 440));
		//this.setVerticalScrollBarPolicy(JScrollPane.
		//		VERTICAL_SCROLLBAR_ALWAYS);
		JXTaskPaneContainer tpc = new JXTaskPaneContainer();
		this.setColumnHeaderView(tpc);

        statementPanel();
		JXTaskPane statementTaskPane = new JXTaskPane();
		ImageIcon statementIcon = new ImageIcon(getClass().getResource(
				"/icons/comments.png"));
		statementTaskPane.setName("Statements");
		statementTaskPane.setTitle("Statements");
		statementTaskPane.setIcon(statementIcon);
        ((Container)tpc).add(statementTaskPane);
        statementTaskPane.add(statementPanel);
        
		SearchPanel sp = new SearchPanel();
		JXTaskPane searchTaskPane = new JXTaskPane();
		ImageIcon findIcon = new ImageIcon(getClass().getResource(
				"/icons/find.png"));
		searchTaskPane.setName("Search within document");
		searchTaskPane.setTitle("Search within document");
		searchTaskPane.setIcon(findIcon);
		searchTaskPane.setCollapsed(true);
		((Container)tpc).add(searchTaskPane);
        searchTaskPane.add(sp);
		
        
        /*
         * LB.Add: Panel for Finding self-contradictions
         * Class: ContradictionPanel()
         */
        cp = new ContradictionPanel();
        JXTaskPane selfContradictionTaskPane = new JXTaskPane();
		ImageIcon groupIcon = new ImageIcon(getClass().getResource(
				"/icons/group.png"));
		selfContradictionTaskPane.setName("Find self-contradictions");
		selfContradictionTaskPane.setTitle("Find self-contradictions");
		selfContradictionTaskPane.setIcon(groupIcon);
		selfContradictionTaskPane.setCollapsed(true);
		((Container)tpc).add(selfContradictionTaskPane);
		selfContradictionTaskPane.add(cp);
        
        
        /*
        JXTaskPane docStatisticsTaskPane = new JXTaskPane();
        DocStats docStats = new DocStats();
        ImageIcon docStatisticsIcon = new ImageIcon(getClass().getResource("/icons/chart_bar.png"));
        docStatisticsTaskPane.setName("Document summary statistics");
        docStatisticsTaskPane.setTitle("Document summary statistics");
        docStatisticsTaskPane.setIcon(docStatisticsIcon);
        docStatisticsTaskPane.setCollapsed(true);
        docStatisticsTaskPane.add(docStats);
        ((Container)tpc).add(docStatisticsTaskPane);
        */
	}
	
	public class StatementCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row, 
				int column) {
			Component c = super.getTableCellRendererComponent(table, value, 
					isSelected, hasFocus, row, column);
			int modelRow = table.convertRowIndexToModel(row);
		    Color col = ((SidebarStatementContainer)table.getModel()).
		    		get(modelRow).getColor();
		    c.setBackground(col);
		    return c;
		}
	}
	
	private void statementPanel() {
		ssc = new SidebarStatementContainer();
		statementTable = new JTable( ssc );
		statementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statementTableScrollPane = new JScrollPane(statementTable);
		statementTableScrollPane.setPreferredSize(new Dimension(200, 240));
		statementTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 30 );
		statementTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 170 );
		
		statementTable.getTableHeader().setReorderingAllowed( false );
		statementTable.putClientProperty("terminateEditOnFocusLost", 
				Boolean.TRUE);
		
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
					int statementId = ssc.get(row).getStatementId();
					if (statementId != -1) {
						highlightStatementInText(statementId);
					}
				}
			}
		});
	}


	//LB.Change: changed to public 
	public void highlightStatementInText(int statementId) {
		int docId = Dna.dna.db.getStatement(statementId).getDocumentId();
		int docRow = Dna.dna.gui.documentPanel.documentContainer.
				getRowIndexById(docId);
		Dna.dna.gui.documentPanel.documentTable.getSelectionModel().
				setSelectionInterval(docRow, docRow);
		Dna.dna.gui.textPanel.selectStatement(statementId, docId);
	}
	
	public void updateStatementTypes() {
		typeComboBox1.removeAllItems();
		if (Dna.dna.db.getFileName() != null && !Dna.dna.db.getFileName().
				equals("")) {
			ArrayList<StatementType> types = Dna.dna.db.getStatementTypes();
			for (int i = 0; i < types.size(); i++) {
				typeComboBox1.addItem(types.get(i).getLabel());
			}
			typeComboBox1.setSelectedIndex(0);
		}
		//LB.Add: dasselbe noch einmal für den SelfCont-Filter
		cp.typeComboBox1b.removeAllItems();
		if (Dna.dna.db.getFileName() != null && !Dna.dna.db.getFileName().
				equals("")) {
			ArrayList<StatementType> types = Dna.dna.db.getStatementTypes();
			for (int i = 0; i < types.size(); i++) {
				cp.typeComboBox1b.addItem(types.get(i).getLabel());
			}
			cp.typeComboBox1b.setSelectedIndex(0);
		}

	}
	
	
	public void updateVariables() {
		variableComboBox1.removeAllItems();
		variableComboBox2.removeAllItems();
		String type = (String) typeComboBox1.getSelectedItem();
		if (type != null && !type.equals("")) {
			HashMap<String, String> variables = Dna.dna.db.getVariables(type);
			Iterator<String> keyIterator = variables.keySet().iterator();
			while (keyIterator.hasNext()){
				String key = keyIterator.next();
				variableComboBox1.addItem(key);
				variableComboBox2.addItem(key);
			}
			variableComboBox1.setSelectedIndex(0);
			variableComboBox2.setSelectedIndex(0);
		}
	}
	

/*	
	private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {
		
		String[] options = {"-Select-", "Item 1", "Item 2", "Item 3", "Item 4"};
		String[] selected = {"-Select-", "-Select-", "-Select-", "-Select-"};

		//Loop through all of the comboboxes in comboBoxes
	    for(int i = 0; i < varComboBoxes.length; i++) {
	        //Check to see if the current combobox in the array matches the source of your event
	        if(evt.getSource() == varComboBoxes[i]) {
	            //Get the string value of the combobox that fired the event
	            String currentSelection = (String)varComboBoxes[i].getSelectedItem();
	                    //Add back the previous value to all comboboxes other than the one that fired the event
	                    for(int j = 0; j < varComboBoxes.length; j++) {
	                        if(j != i) {
	                        	varComboBoxes[j].addItem(selected[i]);
	                        }
	                //If current value of the combobox is "-Select-" don't remove it from all other comboboxes
	                if(!currentSelection.equals(options[0])) {
	                    //Remove the current value from all comboboxes other than the one that fired the event
	                    for(int j1 = 0; j1 < varComboBoxes.length; j1++) {
	                        if(j1 != i) {
	                        	varComboBoxes[j1].removeItem(varComboBoxes[i].getSelectedItem());
	                        }
	                    }
	                }
	            }
	            //Set the selected item for the combobox that fired the event to the current value
	            selected[i] = currentSelection;
	        }
	    }
	}
	
*/
	
	public void setRowSorterEnabled(boolean enabled) {
		if (enabled == true) {
			sorter = new TableRowSorter<SidebarStatementContainer>(ssc) {
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
		
		private static final long serialVersionUID = 1L;
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
			showGroup.add(showAll);
			showGroup.add(showCurrent);
			showGroup.add(showFilter);
			showPanel.add(showAll);
			showPanel.add(showCurrent);
			showPanel.add(showFilter);
			
			typeComboBox1 = new JComboBox();
			//LB.Change: typeComboBox1 = new JComboBox<String>();
			typeComboBox1.setPreferredSize(new Dimension(208, 20));
			typeComboBox1.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					updateVariables();
					filter();
				}
				
			});
			
			variableComboBox1 = new JComboBox();
			// LB.Change: variableComboBox1 = new JComboBox<String>();
			variableComboBox1.setPreferredSize(new Dimension(100, 20));
			
			variableComboBox1.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					filter();
				}
				
			});
			patternField1 = new JXTextField("regex");
			patternField1.setPreferredSize(new Dimension(104, 20));
			
			variableComboBox2 = new JComboBox();
			// LB.Change: variableComboBox2 = new JComboBox<String>();
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
			JPanel filterPanel = new JPanel(new BorderLayout());
			filterPanel.add(filterPanel0, BorderLayout.NORTH);
			filterPanel.add(filterPanel1, BorderLayout.CENTER);
			filterPanel.add(filterPanel2, BorderLayout.SOUTH);
			
			this.add(showPanel, BorderLayout.NORTH);
			this.add(filterPanel, BorderLayout.CENTER);

			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == showAll) {
						toggleEnabled(false);
						allFilter();
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
			typeComboBox1.setEnabled(enabled);
			variableComboBox1.setEnabled(enabled);
			patternField1.setEnabled(enabled);
			variableComboBox2.setEnabled(enabled);
			patternField2.setEnabled(enabled);
		}

		public void allFilter() {
			try {
				RowFilter<SidebarStatementContainer, Object> rf = null;
	    		rf = RowFilter.regexFilter("");
				if (showAll.isSelected()) {
					sorter.setRowFilter(rf);
				}
			} catch (java.util.regex.PatternSyntaxException pse) {
				return;
			}
		}

		public void documentFilter() {
			int row = Dna.dna.gui.documentPanel.documentTable.getSelectedRow();
			int docId = -1;
			if (row > -1) {
				docId = Dna.dna.gui.documentPanel.documentContainer.get(row).
						getId();
			}
			final int documentId = docId;
			
			RowFilter<SidebarStatementContainer, Integer> documentFilter = new 
					RowFilter<SidebarStatementContainer, Integer>() {
				public boolean include(Entry<? extends 
						SidebarStatementContainer, ? extends Integer> entry) {
    				SidebarStatementContainer stcont = entry.getModel();
    				SidebarStatement st = stcont.get(entry.getIdentifier());
    				if (st.getDocumentId() == documentId) {
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
			String fn = Dna.dna.db.getFileName();
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
					final ArrayList<Integer> ids1 = Dna.dna.db.
							getStatementMatch(t1, v1, p1);
					final ArrayList<Integer> ids2 = Dna.dna.db.
							getStatementMatch(t1, v2, p2);
					final String p1final = p1;
					final String p2final = p2;
					
					RowFilter<SidebarStatementContainer, Integer> idFilter = 
							new	RowFilter<SidebarStatementContainer, Integer>() 
							{
						public boolean include(Entry<? extends 
								SidebarStatementContainer, ? extends Integer> 
								entry) {
							SidebarStatementContainer stcont = entry.getModel();
							SidebarStatement st = stcont.get(entry.
									getIdentifier());
							
							boolean contentMatch;
							if (ids1.contains(st.getStatementId()) && (ids2.
									contains(st.getStatementId()) || p2final.
									equals(""))) {
								contentMatch = true;
							} else if (ids2.contains(st.getStatementId()) && 
									(ids1.contains(st.getStatementId()) || 
									p1final.equals(""))) {
								contentMatch = true;
							} else {
								contentMatch = false;
							}
							
							if (contentMatch == true) {
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