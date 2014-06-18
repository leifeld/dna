package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

class SidebarPanel extends JScrollPane {
	
	private static final long serialVersionUID = 1L;
	
	SidebarStatementContainer ssc;
	JTable statementTable;
	JScrollPane statementTableScrollPane;
	JPanel statementPanel;
	StatementFilter statementFilter;
	TableRowSorter<SidebarStatementContainer> sorter;
	JTextField filterField;
	
	public SidebarPanel() {
		this.setPreferredSize(new Dimension(260, 440));
		//this.setVerticalScrollBarPolicy(JScrollPane.
		//		VERTICAL_SCROLLBAR_ALWAYS);
		JXTaskPaneContainer tpc = new JXTaskPaneContainer();
		this.setColumnHeaderView(tpc);
		
		SearchPanel sp = new SearchPanel();
		JXTaskPane searchTaskPane = new JXTaskPane();
		ImageIcon findIcon = new ImageIcon(getClass().getResource(
				"/icons/find.png"));
		searchTaskPane.setName("Search within article");
		searchTaskPane.setTitle("Search within article");
		searchTaskPane.setIcon(findIcon);
		searchTaskPane.setCollapsed(true);
		((Container)tpc).add(searchTaskPane);
        searchTaskPane.add(sp);
		
        statementPanel();
		JXTaskPane statementTaskPane = new JXTaskPane();
		ImageIcon statementIcon = new ImageIcon(getClass().getResource(
				"/icons/comments.png"));
		statementTaskPane.setName("Statements");
		statementTaskPane.setTitle("Statements");
		statementTaskPane.setIcon(statementIcon);
        ((Container)tpc).add(statementTaskPane);
        statementTaskPane.add(statementPanel);

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
		    Color col = ((SidebarStatementContainer)table.getModel()).get(row)
		    		.getColor();
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

	private void highlightStatementInText(int statementId) {
		int docId = Dna.dna.db.getStatement(statementId).getDocumentId();
		int docRow = Dna.dna.gui.documentPanel.documentContainer.
				getRowIndexById(docId);
		Dna.dna.gui.documentPanel.documentTable.getSelectionModel().
				setSelectionInterval(docRow, docRow);
		Dna.dna.gui.textPanel.selectStatement(statementId, docId);
	}
	
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

			JPanel filterPanel = new JPanel(new BorderLayout());
			JLabel filterLabel = new JLabel("filter");
			filterField = new JTextField(13);
			filterPanel.add(filterLabel, BorderLayout.WEST);
			filterPanel.add(filterField, BorderLayout.EAST);
			
			toggleEnabled(false);
			
			JPanel fieldsPanel = new JPanel(new GridLayout(1,1)); // was: 6,1
			fieldsPanel.add(filterPanel);
			
			this.add(showPanel, BorderLayout.NORTH);
			this.add(fieldsPanel, BorderLayout.CENTER);

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
	        
	        filterField.getDocument().addDocumentListener(dl);
		}

		public void toggleEnabled(boolean enabled) {
			filterField.setEnabled(enabled);
		}

		public void allFilter() {
			try {
				RowFilter<SidebarStatementContainer, Object> rf = null;
	    		rf = RowFilter.regexFilter("");
	    		sorter.setRowFilter(rf);
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
			sorter.setRowFilter(documentFilter);
		}
		
		private void filter() {
			//final String p = personField.getText();
			//final String i = idField.getText();
			RowFilter<SidebarStatementContainer, Integer> idFilter = new 
					RowFilter<SidebarStatementContainer, Integer>() {
				public boolean include(Entry<? extends 
						SidebarStatementContainer, ? extends Integer> entry) {
					SidebarStatementContainer stcont = entry.getModel();
					SidebarStatement st = stcont.get(entry.getIdentifier());
					
					//Pattern pPattern = Pattern.compile(p);
					//Matcher pMatcher = pPattern.matcher(st.getPerson());
					//boolean pBoolean = pMatcher.find();
					
					//Pattern iPattern = Pattern.compile(i);
					//Matcher iMatcher = iPattern.matcher(new Integer(st.getId()).toString());
					//boolean iBoolean = iMatcher.find();
					
					//if (pBoolean == true && iBoolean == true) {
					//	return true;
					//}
					return false;
				}
			};
			sorter.setRowFilter(idFilter);
		}
	}
	
}
