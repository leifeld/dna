package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
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
		//TODO: statementPanel must be populated with statements or updated when the database is loaded or changed
		
		ssc = new SidebarStatementContainer();
		statementTable = new JTable( ssc );
		statementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statementTableScrollPane = new JScrollPane(statementTable);
		statementTableScrollPane.setPreferredSize(new Dimension(200, 240));
		statementTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 30 );
		statementTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 170 );
		
		statementTable.getTableHeader().setReorderingAllowed( false );
		statementTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		setRowSorterEnabled(true);
		
		StatementCellRenderer statementCellRenderer = new StatementCellRenderer();
		statementTable.getColumnModel().getColumn(0).setCellRenderer(statementCellRenderer);
		
		//TODO
		//StatementFilter statementFilter = new StatementFilter();
		
		statementPanel = new JPanel(new BorderLayout());
		statementPanel.add(statementTableScrollPane, BorderLayout.CENTER);
		//TODO
		//statementPanel.add(statementFilter, BorderLayout.SOUTH);
		
		statementTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = -1;
				row = statementTable.rowAtPoint(e.getPoint());
				row = statementTable.convertRowIndexToModel(row);
				
				if (row > -1) {
					int statementId = ssc.get(row).getStatementId();
					if (statementId != -1) {
						//TODO
						//highlightStatementInText(statementId);
						//TODO
						//new Popup(statementId);
					}
				}
			}
		});
		
	}

	public void setRowSorterEnabled(boolean enabled) {
		if (enabled == true) {
			TableRowSorter<SidebarStatementContainer> sorter = new TableRowSorter<SidebarStatementContainer>(ssc) {
				public void toggleSortOrder(int i) {
					//leave blank; the overwritten method makes the table unsortable
				}
			};
	        statementTable.setRowSorter(sorter);
		} else {
			//TODO
			//statementFilter.showAll.setSelected(true);
    		statementTable.setRowSorter(null);
		}
	}
	
}
