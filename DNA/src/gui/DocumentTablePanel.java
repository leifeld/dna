package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import dna.Dna.CoderListener;
import dna.Dna.SqlListener;
import gui.MainWindow.ActionAddDocument;
import gui.MainWindow.ActionEditDocuments;
import gui.MainWindow.ActionRemoveDocuments;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import model.TableDocument;
import sql.Sql.SqlResults;

class DocumentTablePanel extends JPanel implements SqlListener, CoderListener, ToolbarListener {
	private static final long serialVersionUID = 4543056929753553570L;
	private ArrayList<DocumentPanelListener> listeners = new ArrayList<DocumentPanelListener>();
	private JTable documentTable;
	private DocumentTableModel documentTableModel;
	private String documentFilterPattern = "";

	DocumentTablePanel(DocumentTableModel documentTableModel,
			ActionAddDocument actionAddDocument,
			ActionRemoveDocuments actionRemoveDocuments,
			ActionEditDocuments actionEditDocuments) {
		this.setLayout(new BorderLayout());
		this.documentTableModel = documentTableModel;
		documentTable = new JTable(documentTableModel);
		documentTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		TableRowSorter<DocumentTableModel> sorter = new TableRowSorter<DocumentTableModel>(documentTableModel);
		documentTable.setRowSorter(sorter);

		// set column visibility
		TableColumn column[] = new TableColumn[11];
	    for (int i = 0; i < column.length; i++) {
	        column[i] = documentTable.getColumnModel().getColumn(i);
	    }
	    Boolean[] columnsVisible = new Boolean[] {true, true, true, true, true, true, true, true, true, true, true};
	    
		documentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		documentTable.getColumnModel().getColumn(1).setPreferredWidth(600);
		documentTable.getColumnModel().getColumn(2).setPreferredWidth(30);
		documentTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		documentTable.getColumnModel().getColumn(4).setPreferredWidth(80);
		documentTable.getColumnModel().getColumn(5).setPreferredWidth(110);
		
		// document table cell renderers
        documentTable.setDefaultRenderer(Coder.class, new CoderTableCellRenderer());

		JScrollPane documentTableScroller = new JScrollPane(documentTable);
		documentTableScroller.setViewportView(documentTable);
		documentTableScroller.setPreferredSize(new Dimension(1000, 300));
		this.add(documentTableScroller, BorderLayout.CENTER);

		// row filter
		RowFilter<DocumentTableModel, Integer> documentFilter = new RowFilter<DocumentTableModel, Integer>() {
			public boolean include(Entry<? extends DocumentTableModel, ? extends Integer> entry) {
				TableDocument d = documentTableModel.getRow(entry.getIdentifier());
				try {
					Pattern pattern = Pattern.compile(documentFilterPattern);
					Matcher matcherTitle = pattern.matcher(d.getTitle());
					Matcher matcherAuthor = pattern.matcher(d.getAuthor());
					Matcher matcherSource = pattern.matcher(d.getSource());
					Matcher matcherSection = pattern.matcher(d.getSection());
					Matcher matcherType = pattern.matcher(d.getType());
					Matcher matcherNotes = pattern.matcher(d.getTitle());
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy HH:mm");
					Matcher matcherDateTime = pattern.matcher(d.getDateTime().format(formatter));
					if (documentFilterPattern.equals("")) {
						return true;
					} else if (matcherTitle.find()) {
						return true;
					} else if (matcherAuthor.find()) {
						return true;
					} else if (matcherSource.find()) {
						return true;
					} else if (matcherSection.find()) {
						return true;
					} else if (matcherType.find()) {
						return true;
					} else if (matcherNotes.find()) {
						return true;
					} else if (matcherDateTime.find()) {
						return true;
					} else {
						return false;
					}
				} catch(PatternSyntaxException pse) {
					return true;
				}
				
			}
		};
		sorter.setRowFilter(documentFilter);

	    // right-click menu for document table
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuItemAddDocument = new JMenuItem(actionAddDocument);
		popupMenu.add(menuItemAddDocument);
		JMenuItem menuItemDelete = new JMenuItem(actionRemoveDocuments);
		popupMenu.add(menuItemDelete);
		JMenuItem menuItemEdit = new JMenuItem(actionEditDocuments);
		popupMenu.add(menuItemEdit);
		JSeparator sep = new JSeparator();
		popupMenu.add(sep);
		ImageIcon checkedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-checkbox.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon uncheckedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-square.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JMenuItem menuItemId = new JMenuItem("ID", checkedIcon);
		popupMenu.add(menuItemId);
		JMenuItem menuItemTitle = new JMenuItem("Title", checkedIcon);
		popupMenu.add(menuItemTitle);
		JMenuItem menuItemNumber = new JMenuItem("#", checkedIcon);
		popupMenu.add(menuItemNumber);
		JMenuItem menuItemDate = new JMenuItem("Date", checkedIcon);
		popupMenu.add(menuItemDate);
		JMenuItem menuItemTime = new JMenuItem("Time", checkedIcon);
		popupMenu.add(menuItemTime);
		JMenuItem menuItemCoder = new JMenuItem("Coder", checkedIcon);
		popupMenu.add(menuItemCoder);
		JMenuItem menuItemAuthor = new JMenuItem("Author", checkedIcon);
		popupMenu.add(menuItemAuthor);
		JMenuItem menuItemSource = new JMenuItem("Source", checkedIcon);
		popupMenu.add(menuItemSource);
		JMenuItem menuItemSection = new JMenuItem("Section", checkedIcon);
		popupMenu.add(menuItemSection);
		JMenuItem menuItemType = new JMenuItem("Type", checkedIcon);
		popupMenu.add(menuItemType);
		JMenuItem menuItemNotes = new JMenuItem("Notes", checkedIcon);
		popupMenu.add(menuItemNotes);
		documentTable.setComponentPopupMenu(popupMenu);
		documentTable.getTableHeader().setComponentPopupMenu(popupMenu);
		documentTableScroller.setComponentPopupMenu(popupMenu);
		
		// ActionListener with actions for right-click document menu
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == menuItemId) {
					if (columnsVisible[0] == false) {
						columnsVisible[0] = true;
						menuItemId.setIcon(checkedIcon);
					} else {
						columnsVisible[0] = false;
						menuItemId.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemTitle) {
					if (columnsVisible[1] == false) {
						columnsVisible[1] = true;
						menuItemTitle.setIcon(checkedIcon);
					} else {
						columnsVisible[1] = false;
						menuItemTitle.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemNumber) {
					if (columnsVisible[2] == false) {
						columnsVisible[2] = true;
						menuItemNumber.setIcon(checkedIcon);
					} else {
						columnsVisible[2] = false;
						menuItemNumber.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemDate) {
					if (columnsVisible[3] == false) {
						columnsVisible[3] = true;
						menuItemDate.setIcon(checkedIcon);
					} else {
						columnsVisible[3] = false;
						menuItemDate.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemTime) {
					if (columnsVisible[4] == false) {
						columnsVisible[4] = true;
						menuItemTime.setIcon(checkedIcon);
					} else {
						columnsVisible[4] = false;
						menuItemTime.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemCoder) {
					if (columnsVisible[5] == false) {
						columnsVisible[5] = true;
						menuItemCoder.setIcon(checkedIcon);
					} else {
						columnsVisible[5] = false;
						menuItemCoder.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemAuthor) {
					if (columnsVisible[6] == false) {
						columnsVisible[6] = true;
						menuItemAuthor.setIcon(checkedIcon);
					} else {
						columnsVisible[6] = false;
						menuItemAuthor.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemSource) {
					if (columnsVisible[7] == false) {
						columnsVisible[7] = true;
						menuItemSource.setIcon(checkedIcon);
					} else {
						columnsVisible[7] = false;
						menuItemSource.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemSection) {
					if (columnsVisible[8] == false) {
						columnsVisible[8] = true;
						menuItemSection.setIcon(checkedIcon);
					} else {
						columnsVisible[8] = false;
						menuItemSection.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemType) {
					if (columnsVisible[9] == false) {
						columnsVisible[9] = true;
						menuItemType.setIcon(checkedIcon);
					} else {
						columnsVisible[9] = false;
						menuItemType.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemNotes) {
					if (columnsVisible[10] == false) {
						columnsVisible[10] = true;
						menuItemNotes.setIcon(checkedIcon);
					} else {
						columnsVisible[10] = false;
						menuItemNotes.setIcon(uncheckedIcon);
					}
				}

				while (documentTable.getColumnModel().getColumnCount() > 0) {
					documentTable.getColumnModel().removeColumn(documentTable.getColumnModel().getColumn(0));
				}

				for (int i = 0; i < columnsVisible.length; i++) {
					if (columnsVisible[i] == true) {
						documentTable.getColumnModel().addColumn(column[i]);
					}
				}
			}
		};

		menuItemId.addActionListener(al);
		menuItemTitle.addActionListener(al);
		menuItemNumber.addActionListener(al);
		menuItemDate.addActionListener(al);
		menuItemTime.addActionListener(al);
		menuItemCoder.addActionListener(al);
		menuItemAuthor.addActionListener(al);
		menuItemSource.addActionListener(al);
		menuItemSection.addActionListener(al);
		menuItemType.addActionListener(al);
		menuItemNotes.addActionListener(al);
		
		documentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				
				int rowCount = documentTable.getSelectedRowCount();
				if (rowCount > 0) {
					actionRemoveDocuments.setEnabled(true);
					actionEditDocuments.setEnabled(true);
				} else {
					actionRemoveDocuments.setEnabled(false);
					actionEditDocuments.setEnabled(false);
				}
				if (rowCount == 0) {
					fireNoSelection();
				} else if (rowCount > 1) {
					int[] selectedRows = documentTable.getSelectedRows();
					int[] selectedDocumentIds = new int[selectedRows.length];
					for (int i = 0; i < selectedRows.length; i++) {
						selectedDocumentIds[i] = documentTableModel.getIdByModelRow(documentTable.convertRowIndexToModel(selectedRows[i]));
					}
					fireMultipleSelection(selectedDocumentIds);
				} else if (rowCount == 1) {
					int selectedRow = documentTable.getSelectedRow();
					int selectedModelIndex = documentTable.convertRowIndexToModel(selectedRow);
					int id = (int) documentTableModel.getValueAt(selectedModelIndex, 0);
					fireSingleSelection(id, documentTableModel.getDocumentText(id));
				} else {
					LogEvent l = new LogEvent(Logger.WARNING,
							"[GUI] Negative number of rows in the document table!",
							"When a document is selected in the document table in the DNA coding window, the text of the document is displayed in the text panel. When checking which row in the table was selected, it was found that the table contained negative numbers of documents. This is obviously an error. Please report it by submitting a bug report along with the saved log.");
					Dna.logger.log(l);
				}
				// if (Dna.gui.rightPanel.statementPanel.statementFilter.showCurrent.isSelected()) {
				// 	Dna.gui.rightPanel.statementPanel.statementFilter.currentDocumentFilter();
				// }
				
				// if (Dna.dna.sql != null) {
				// 	Dna.gui.textPanel.paintStatements();
				// }
				
				/*
				int ac = Dna.data.getActiveCoder();
				if (Dna.gui.leftPanel.editDocPanel.saveDetailsButton != null) {
					if (Dna.dna.sql == null || Dna.data.getCoderById(ac).getPermissions().get("editDocuments") == false) {
						Dna.gui.leftPanel.editDocPanel.saveDetailsButton.setEnabled(false);
						Dna.gui.leftPanel.editDocPanel.cancelButton.setEnabled(false);
					} else {
						Dna.gui.leftPanel.editDocPanel.saveDetailsButton.setEnabled(true);
						Dna.gui.leftPanel.editDocPanel.cancelButton.setEnabled(true);
					}
				}
				
				if (Dna.dna.sql == null || Dna.data.getCoderById(ac).getPermissions().get("deleteDocuments") == false) {
					Dna.gui.documentPanel.menuItemDelete.setEnabled(false);
				} else {
					Dna.gui.documentPanel.menuItemDelete.setEnabled(true);
				}
				
				if (Dna.dna.sql == null || Dna.data.getCoderById(ac).getPermissions().get("addDocuments") == false) {
					Dna.gui.menuBar.newDocumentButton.setEnabled(false);
				} else {
					Dna.gui.menuBar.newDocumentButton.setEnabled(true);
				}
				*/
			}
		});
	}
	
	void refresh() {
		if (Dna.sql != null) {
			DocumentTableRefreshWorker worker = new DocumentTableRefreshWorker();
			worker.execute();
		} else {
			documentTableModel.clear();
		}
	}

	/**
	 * Swing worker class for loading documents from the database and adding
	 * them to the document table in a background thread.
	 * 
	 * @see <a href="https://stackoverflow.com/questions/43161033/cant-add-tablerowsorter-to-jtable-produced-by-swingworker" target="_top">https://stackoverflow.com/questions/43161033/</a>
	 * @see <a href="https://stackoverflow.com/questions/68884145/how-do-i-use-a-jdbc-swing-worker-with-connection-pooling-ideally-while-separati" target="_top">https://stackoverflow.com/questions/68884145/</a>
	 */
	class DocumentTableRefreshWorker extends SwingWorker<List<TableDocument>, TableDocument> {
		/**
		 * Time stamp to measure the duration it takes to update the table. The
		 * duration is logged when the table has been updated.
		 */
		private long time;

		DocumentTableRefreshWorker() {
			fireDocumentRefreshStart();
			time = System.nanoTime(); // take the time to compute later how long the updating took
			//statusBar.setDocumentRefreshing(true); // display a message in the status bar that documents are being loaded
			//selectedId = getSelectedDocumentId(); // remember the document ID to select the same document when done
			//y = documentPanel.getViewportPosition(); // remember the vertical position in the text area to go back to the same position when done
			documentTableModel.clear(); // remove all documents from the table model before re-populating the table
			LogEvent le = new LogEvent(Logger.MESSAGE,
					"[GUI] Initializing thread to populate document table: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").",
					"A new swing worker thread has been started to populate the document table with documents from the database in the background: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").");
			Dna.logger.log(le);
		}
		
		@Override
		protected List<TableDocument> doInBackground() {
			try (SqlResults s = Dna.sql.getTableDocumentResultSet(); // result set and connection are automatically closed when done because SqlResults implements AutoCloseable
					ResultSet rs = s.getResultSet();) {
				while (rs.next()) {
					TableDocument r = new TableDocument(
							rs.getInt("ID"),
							rs.getString("Title"),
							rs.getInt("Frequency"),
							new Coder(rs.getInt("CoderId"),
									rs.getString("CoderName"),
									rs.getInt("Red"),
									rs.getInt("Green"),
									rs.getInt("Blue"),
									0, 14, 300, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
							rs.getString("Author"),
							rs.getString("Source"),
							rs.getString("Section"),
							rs.getString("Type"),
							rs.getString("Notes"),
							LocalDateTime.ofEpochSecond(rs.getLong("Date"), 0, ZoneOffset.UTC));
					publish(r); // send the new document row out of the background thread
				}
			} catch (SQLException e) {
				LogEvent le = new LogEvent(Logger.WARNING,
						"[SQL]  ├─ Could not retrieve documents from database.",
						"The document table model swing worker tried to retrieve all documents from the database to display them in the document table, but some or all documents could not be retrieved because there was a problem while processing the result set. The document table may be incomplete.",
						e);
				Dna.logger.log(le);
			}
			return null;
		}
	    
	    @Override
	    protected void process(List<TableDocument> chunks) {
	    	documentTableModel.addRows(chunks); // transfer a batch of rows to the table model
	    	fireDocumentRefreshChunk();
			//documentPanel.setUserLocation(selectedId, y); // select the document from before and scroll to the right position; skipped if the document not found in this batch
	    }

	    @Override
	    protected void done() {
	        //statusBar.setDocumentRefreshing(false); // stop displaying the update message in the status bar
			long elapsed = System.nanoTime(); // measure time again for calculating difference
			LogEvent le = new LogEvent(Logger.MESSAGE,
					"[GUI]  ├─ (Re)loaded all " + documentTableModel.getRowCount() + " documents in " + (elapsed - time) / 1000000 + " milliseconds.",
					"The document table swing worker loaded the " + documentTableModel.getRowCount() + " documents from the DNA database in the "
					+ "background and stored them in the document table. This took " + (elapsed - time) / 1000000 + " seconds.");
			Dna.logger.log(le);
			le = new LogEvent(Logger.MESSAGE,
					"[GUI]  └─ Closing thread to populate document table: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").",
					"The document table has been populated with documents from the database. Closing thread: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").");
			Dna.logger.log(le);
			fireDocumentRefreshEnd();
	    }
	}

	/**
	 * Return the indices of the rows that are currently selected in the
	 * document table.
	 * 
	 * @return A one-dimensional integer array of row indices in the table.
	 */
	int[] getSelectedRows() {
		return documentTable.getSelectedRows();
	}

	/**
	 * Retrieve the ID of the document that is currently selected in the table.
	 * 
	 * @return  The document ID. Can be {@code -1} if nothing is selected.
	 */
	/*
	int getSelectedDocumentId() {
		int viewRow = this.documentTable.getSelectedRow();
		if (viewRow > -1) {
			int modelRow = this.convertRowIndexToModel(viewRow);
			int id = this.documentTableModel.getIdByModelRow(modelRow);
			return id;
		} else {
			return -1;
		}
	}
	*/
	
	/**
	 * Convert a row index in the document table to a document table model index.
	 * 
	 * @param rowIndex The row index in the table to convert.
	 * @return The row index in the table model corresponding to the table row index.
	 */
	int convertRowIndexToModel(int rowIndex) {
		return documentTable.convertRowIndexToModel(rowIndex);
	}
	
	/*
	private void selectDocument(int documentId) {
		int index = documentTable.convertRowIndexToView(this.documentTableModel.getModelRowById(documentId));
		this.documentTable.setRowSelectionInterval(index, index);
	}
	*/

	private void fireNoSelection() {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).documentTableNoSelection();
		}
	}
	
	private void fireMultipleSelection(int[] documentId) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).documentTableMultipleSelection(documentId);
		}
	}
	
	private void fireSingleSelection(int documentId, String documentText) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).documentTableSingleSelection(documentId, documentText);
		}
	}

    private void fireDocumentRefreshStart() {
    	for (int i = 0; i < listeners.size(); i++) {
    		listeners.get(i).documentRefreshStarted();
    	}
    }

    private void fireDocumentRefreshChunk() {
    	for (int i = 0; i < listeners.size(); i++) {
    		listeners.get(i).documentRefreshChunkComplete();
    	}
    }

    private void fireDocumentRefreshEnd() {
    	for (int i = 0; i < listeners.size(); i++) {
    		listeners.get(i).documentRefreshEnded();
    	}
    }

	void addDocumentPanelListener(DocumentPanelListener listener) {
		listeners.add(listener);
	}

	@Override
	public void updatedDocumentFilterPattern(String pattern) {
		this.documentFilterPattern = pattern;
	}

	@Override
	public void adjustToChangedCoder() {
		// TODO Auto-generated method stub
	}

	@Override
	public void adjustToDatabaseState() {
		refresh();
	}
}