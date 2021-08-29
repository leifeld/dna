package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import dna.Dna.CoderListener;
import dna.Dna.SqlListener;
import dna.StatementListener;
import logger.LogEvent;
import logger.Logger;
import model.TableStatement;
import sql.Sql.SqlResults;

public class StatementPanel extends JPanel implements SqlListener, CoderListener {
	private static final long serialVersionUID = 1044070479152247253L;
	private List<StatementListener> statementListeners = new ArrayList<StatementListener>();
	private JTable statementTable;
	private StatementTableModel statementTableModel;

	public StatementPanel(StatementTableModel statementTableModel) {
		this.setLayout(new BorderLayout());
		this.statementTableModel = statementTableModel;
		statementTable = new JTable(statementTableModel);

		statementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableRowSorter<StatementTableModel> sorter = new TableRowSorter<StatementTableModel>(statementTableModel);
		statementTable.setRowSorter(sorter);

		// set column visibility
		TableColumn column[] = new TableColumn[6];
	    for (int i = 0; i < column.length; i++) {
	        column[i] = statementTable.getColumnModel().getColumn(i);
	    }
	    Boolean[] columnsVisible = new Boolean[] {true, false, false, false, false, true};
	    
		statementTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		statementTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		statementTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		statementTable.getColumnModel().getColumn(3).setPreferredWidth(50);
		statementTable.getColumnModel().getColumn(4).setPreferredWidth(30);
		statementTable.getColumnModel().getColumn(5).setPreferredWidth(110);
		
		JScrollPane statementTableScroller = new JScrollPane(statementTable);
		statementTableScroller.setViewportView(statementTable);
		statementTableScroller.setPreferredSize(new Dimension(400, 600));
		this.add(statementTableScroller, BorderLayout.CENTER);
	}
	
	public int getSelectedStatementId() {
		try {
			return (int) statementTable.getValueAt(statementTable.getSelectedRow(), 0);
		} catch (NullPointerException npe) {
			return -1;
		}
	}
	
	public void setSelectedStatementId(int statementId) {
		int modelRowIndex = statementTableModel.getModelRowById(statementId);
		if (modelRowIndex > -1) {
			int tableRow = statementTable.convertRowIndexToView(modelRowIndex);
			this.statementTable.setRowSelectionInterval(tableRow, tableRow);
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
		 * ID of the document that is currently selected. After updating the
		 * table model and table, the document is selected again, and for this
		 * the ID needs to be stored.
		 */
		private int selectedId;
		/**
		 * ID of the selected statement in the statement table, to restore it
		 * later and scroll back to the same position in the table after update.
		 */

		public StatementTableRefreshWorker() {
    		time = System.nanoTime(); // take the time to compute later how long the updating took
    		//statusBar.setStatementRefreshing(true); // display a message in the status bar that statements are being loaded
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
							rs.getInt("Coder"),
							rs.getInt("DocumentId"),
							rs.getInt("Start"),
							rs.getInt("Stop"),
							LocalDateTime.ofEpochSecond(rs.getLong("Date"), 0, ZoneOffset.UTC),
							rs.getString("Text"),
							new Color(rs.getInt("StatementTypeRed"), rs.getInt("StatementTypeGreen"), rs.getInt("StatementTypeBlue")),
							new Color(rs.getInt("CoderRed"), rs.getInt("CoderGreen"), rs.getInt("CoderBlue")));
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
        	statementTableModel.addRows(chunks); // transfer a batch of rows to the table model
			setSelectedStatementId(selectedId); // select the statement from before; skipped if the statement not found in this batch
        }

        @Override
        protected void done() {
            //statusBar.setStatementRefreshing(false); // stop displaying the update message in the status bar
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

	public void addStatementListener(StatementListener listener) {
        statementListeners.add(listener);
    }

	void fireStatementRefreshStart(ArrayList<StatementListener> statementListeners) {
		for (int i = 0; i < statementListeners.size(); i++) {
			statementListeners.get(i).statementRefreshStart();
		}
	}

	void fireStatementRefreshEnd(ArrayList<StatementListener> statementListeners) {
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
	        // StatementTableRefreshWorker statementWorker = new StatementTableRefreshWorker();
	        // statementWorker.execute(); // TODO
		}
	}
}