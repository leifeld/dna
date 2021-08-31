package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import dna.Dna.CoderListener;
import dna.Dna.SqlListener;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import model.TableStatement;
import sql.Sql.SqlResults;

/**
 * Statement panel on the right side of the screen.
 */
class StatementPanel extends JPanel implements SqlListener, CoderListener {
	private static final long serialVersionUID = 1044070479152247253L;
	private List<StatementListener> statementListeners = new ArrayList<StatementListener>();
	private JTable statementTable;
	private StatementTableModel statementTableModel;

	/**
	 * Create a new statement panel.
	 * 
	 * @param statementTableModel  Statement table model for the statements.
	 */
	StatementPanel(StatementTableModel statementTableModel) {
		this.setLayout(new BorderLayout());
		this.statementTableModel = statementTableModel;
		statementTable = new JTable(statementTableModel);

		statementTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		TableRowSorter<StatementTableModel> sorter = new TableRowSorter<StatementTableModel>(statementTableModel);
		statementTable.setRowSorter(sorter);

		// set column visibility
		TableColumn column[] = new TableColumn[6];
	    for (int i = 0; i < column.length; i++) {
	        column[i] = statementTable.getColumnModel().getColumn(i);
	    }
	    Boolean[] columnsVisible = new Boolean[] {true, false, false, false, false, true};
	    
		statementTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		statementTable.getColumnModel().getColumn(1).setPreferredWidth(30);
		statementTable.getColumnModel().getColumn(2).setPreferredWidth(30);
		statementTable.getColumnModel().getColumn(3).setPreferredWidth(30);
		statementTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		statementTable.getColumnModel().getColumn(5).setPreferredWidth(300);

		// statement table cell renderer
		statementTable.setDefaultRenderer(Integer.class, new StatementTableCellRenderer());
		statementTable.setDefaultRenderer(String.class, new StatementTableCellRenderer());
        statementTable.setDefaultRenderer(Coder.class, new StatementTableCellRenderer());

		JScrollPane statementTableScroller = new JScrollPane(statementTable);
		statementTableScroller.setViewportView(statementTable);
		statementTableScroller.setPreferredSize(new Dimension(400, 600));
		this.add(statementTableScroller, BorderLayout.CENTER);
		
	    // right-click menu for statement table
		JPopupMenu popupMenu = new JPopupMenu();
		ImageIcon checkedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-checkbox.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon uncheckedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-square.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JMenuItem menuItemStatementId = new JMenuItem("ID", checkedIcon);
		popupMenu.add(menuItemStatementId);
		JMenuItem menuItemDocumentId = new JMenuItem("Document ID", uncheckedIcon);
		popupMenu.add(menuItemDocumentId);
		JMenuItem menuItemStart = new JMenuItem("Start position", uncheckedIcon);
		popupMenu.add(menuItemStart);
		JMenuItem menuItemEnd = new JMenuItem("End position", uncheckedIcon);
		popupMenu.add(menuItemEnd);
		JMenuItem menuItemCoder = new JMenuItem("Coder", uncheckedIcon);
		popupMenu.add(menuItemCoder);
		JMenuItem menuItemText = new JMenuItem("Statement text", checkedIcon);
		popupMenu.add(menuItemText);
		statementTable.setComponentPopupMenu(popupMenu);
		statementTable.getTableHeader().setComponentPopupMenu(popupMenu);
		statementTableScroller.setComponentPopupMenu(popupMenu);

		while (statementTable.getColumnModel().getColumnCount() > 0) {
			statementTable.getColumnModel().removeColumn(statementTable.getColumnModel().getColumn(0));
		}
		for (int i = 0; i < columnsVisible.length; i++) {
			if (columnsVisible[i] == true) {
				statementTable.getColumnModel().addColumn(column[i]);
			}
		}
		
		// ActionListener with actions for right-click statement menu
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == menuItemStatementId) {
					if (columnsVisible[0] == false) {
						columnsVisible[0] = true;
						menuItemStatementId.setIcon(checkedIcon);
					} else {
						columnsVisible[0] = false;
						menuItemStatementId.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemDocumentId) {
					if (columnsVisible[1] == false) {
						columnsVisible[1] = true;
						menuItemDocumentId.setIcon(checkedIcon);
					} else {
						columnsVisible[1] = false;
						menuItemDocumentId.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemStart) {
					if (columnsVisible[2] == false) {
						columnsVisible[2] = true;
						menuItemStart.setIcon(checkedIcon);
					} else {
						columnsVisible[2] = false;
						menuItemStart.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemEnd) {
					if (columnsVisible[3] == false) {
						columnsVisible[3] = true;
						menuItemEnd.setIcon(checkedIcon);
					} else {
						columnsVisible[3] = false;
						menuItemEnd.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemCoder) {
					if (columnsVisible[4] == false) {
						columnsVisible[4] = true;
						menuItemCoder.setIcon(checkedIcon);
					} else {
						columnsVisible[4] = false;
						menuItemCoder.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemText) {
					if (columnsVisible[5] == false) {
						columnsVisible[5] = true;
						menuItemText.setIcon(checkedIcon);
					} else {
						columnsVisible[5] = false;
						menuItemText.setIcon(uncheckedIcon);
					}
				}

				while (statementTable.getColumnModel().getColumnCount() > 0) {
					statementTable.getColumnModel().removeColumn(statementTable.getColumnModel().getColumn(0));
				}

				for (int i = 0; i < columnsVisible.length; i++) {
					if (columnsVisible[i] == true) {
						statementTable.getColumnModel().addColumn(column[i]);
					}
				}
			}
		};
		menuItemStatementId.addActionListener(al);
		menuItemDocumentId.addActionListener(al);
		menuItemStart.addActionListener(al);
		menuItemEnd.addActionListener(al);
		menuItemCoder.addActionListener(al);
		menuItemText.addActionListener(al);
	}
	
	/**
	 * Return the ID of the statement currently selected in the table.
	 * 
	 * @return Statement ID (or {@code -1} if no statement is selected.
	 */
	private int getSelectedStatementId() {
		try {
			return (int) statementTable.getValueAt(statementTable.getSelectedRow(), 0);
		} catch (IndexOutOfBoundsException | NullPointerException e) { // e.g., no statements yet in table or nothing currently selected, so ID cannot be saved
			return -1;
		}
	}
	
	/**
	 * Select a statement with a certain ID in the statement table.
	 * 
	 * @param statementId  ID of the statement to be selected.
	 */
	private void setSelectedStatementId(int statementId) {
		int modelRowIndex = statementTableModel.getModelRowById(statementId);
		if (modelRowIndex > -1) { // if no statement was previously selected, don't select a statement now.
			int tableRow = statementTable.convertRowIndexToView(modelRowIndex);
			this.statementTable.setRowSelectionInterval(tableRow, tableRow);
		}
	}
	
	
	/**
	 * Table cell renderer for the statement table
	 */
	class StatementTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -7421516568789969759L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        	Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        	StatementTableModel model = (StatementTableModel) table.getModel();
        	TableStatement s = model.getRow(table.convertRowIndexToModel(row));
        	if (value.getClass().toString().endsWith("Coder")) {
        		Coder coder = (Coder) value;
				CoderBadgePanel cbp = new CoderBadgePanel(coder, 14, 22);
				if (isSelected) {
            		cbp.setBackground(javax.swing.UIManager.getColor("Table.dropCellBackground"));
				} else {
					cbp.setBackground(s.getStatementTypeColor()); // TODO: if-clause to use Coder color; store coder in Sql class?
				}
				return cbp;
        	} else {
            	if (isSelected) {
            		c.setBackground(javax.swing.UIManager.getColor("Table.dropCellBackground"));
            	} else {
            		c.setBackground(s.getStatementTypeColor()); // TODO: if-clause to use Coder color; store coder in Sql class?
            	}
    	        return c;
        	}
		}
	}
	
	/**
	 * Swing worker class for loading statements from the database and adding
	 * them to the statement table in a background thread.
	 */
	private class StatementTableRefreshWorker extends SwingWorker<List<TableStatement>, TableStatement> {
		/**
		 * Time stamp to measure the duration it takes to update the table. The
		 * duration is logged when the table has been updated.
		 */
		private long time;
		/**
		 * ID of the selected statement in the statement table, to restore it
		 * later and scroll back to the same position in the table after update.
		 */
		private int selectedId;

		/**
		 * A Swing worker that reloads shallow representations of statements
		 * from the database and stores them in the table model for displaying
		 * them in the statement table.
		 */
		private StatementTableRefreshWorker() {
    		time = System.nanoTime(); // take the time to compute later how long the updating took
    		fireStatementRefreshStart(); // start displaying the update message in the status bar
    		selectedId = getSelectedStatementId();
    		statementTableModel.clear(); // remove all documents from the table model before re-populating the table
			LogEvent le = new LogEvent(Logger.MESSAGE,
					"[GUI] Initializing thread to populate statement table: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").",
					"A new swing worker thread has been started to populate the statement table with statements from the database in the background: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").");
			Dna.logger.log(le);
		}
		
		@Override
		protected List<TableStatement> doInBackground() {
			try (SqlResults s = Dna.sql.getTableStatementResultSet(); // result set and connection are automatically closed when done because SqlResults implements AutoCloseable
					ResultSet rs = s.getResultSet();) {
				while (rs.next()) {
					System.out.println(rs.getInt("DocumentId"));
					TableStatement r = new TableStatement(
							rs.getInt("ID"),
							new Coder(rs.getInt("CoderId"),
									rs.getString("CoderName"),
									rs.getInt("CoderRed"),
									rs.getInt("CoderGreen"),
									rs.getInt("CoderBlue"),
									0, 14, 300, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
							rs.getInt("DocumentId"),
							rs.getInt("Start"),
							rs.getInt("Stop"),
							LocalDateTime.ofEpochSecond(rs.getLong("Date"), 0, ZoneOffset.UTC),
							rs.getString("Text"),
							new Color(rs.getInt("StatementTypeRed"), rs.getInt("StatementTypeGreen"), rs.getInt("StatementTypeBlue")));
					publish(r); // send the new statement row out of the background thread
				}
			} catch (SQLException e) {
				LogEvent le = new LogEvent(Logger.WARNING,
						"[SQL]  ├─ Could not retrieve statements from database.",
						"The statement table model swing worker tried to retrieve all statements from the database to display them in the statement table, but some or all statements could not be retrieved because there was a problem while processing the result set. The statement table may be incomplete.",
						e);
				Dna.logger.log(le);
			}
			return null;
		}
        
        @Override
        protected void process(List<TableStatement> chunks) {
        	statementTableModel.addRows(chunks); // transfer a batch of rows to the statement table model
			setSelectedStatementId(selectedId); // select the statement from before; skipped if the statement not found in this batch
        }

        @Override
        protected void done() {
    		fireStatementRefreshEnd(); // stop displaying the update message in the status bar
    		long elapsed = System.nanoTime(); // measure time again for calculating difference
    		LogEvent le = new LogEvent(Logger.MESSAGE,
    				"[GUI]  ├─ (Re)loaded all " + statementTableModel.getRowCount() + " statements in " + (elapsed - time) / 1000000 + " milliseconds.",
    				"The statement table swing worker loaded the " + statementTableModel.getRowCount() + " statements from the DNA database in the "
    				+ "background and stored them in the statement table. This took " + (elapsed - time) / 1000000 + " seconds.");
    		Dna.logger.log(le);
			le = new LogEvent(Logger.MESSAGE,
					"[GUI]  └─ Closing thread to populate statement table: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").",
					"The statement table has been populated with statements from the database. Closing thread: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").");
			Dna.logger.log(le);
        }
    }

	/**
	 * Add a listener to the class.
	 * 
	 * @param listener An object that implements the {@link
	 *   gui.StatementListener StatementListener} interface.
	 */
	void addStatementListener(StatementListener listener) {
        statementListeners.add(listener);
    }

	/**
	 * Notify the listeners that a statement refresh swing worker has started.
	 */
	private void fireStatementRefreshStart() {
		for (int i = 0; i < statementListeners.size(); i++) {
			statementListeners.get(i).statementRefreshStart();
		}
	}

	/**
	 * Notify the listeners that a statement refresh swing worker has completed.
	 */
	private void fireStatementRefreshEnd() {
		for (int i = 0; i < statementListeners.size(); i++) {
			statementListeners.get(i).statementRefreshEnd();
		}
	}

	@Override
	public void adjustToChangedCoder() {
		// TODO Auto-generated method stub
	}

	@Override
	public void adjustToDatabaseState() {
		if (Dna.sql == null) {
			statementTableModel.clear();
		} else {
	        StatementTableRefreshWorker statementWorker = new StatementTableRefreshWorker();
	        statementWorker.execute();
		}
	}
}