package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.AES256TextEncryptor;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import gui.DocumentTablePanel;
import dna.Dna;
import dna.Dna.SqlListener;
import logger.LogEvent;
import logger.Logger;
import logger.LoggerDialog;
import model.Attribute;
import model.Coder;
import model.Statement;
import model.StatementType;
import model.TableDocument;
import model.Value;
import sql.ConnectionProfile;
import sql.Sql;
import sql.Sql.SqlResults;

/**
 * Main window that instantiates and plugs the different view components
 * together. It contains controls for the different actions and defines
 * listeners that are attached to the view components to interact between the
 * components. 
 */
public class MainWindow extends JFrame implements SqlListener {
	private static final long serialVersionUID = 2740437090361841747L;
	private Container c;
	private DocumentTablePanel documentTablePanel;
	private DocumentTableModel documentTableModel;
	private StatementPanel statementPanel;
	private StatementTableModel statementTableModel;
	private TextPanel textPanel;
	private StatusBar statusBar;
	private ActionOpenDatabase actionOpenDatabase;
	private ActionCreateDatabase actionCreateDatabase;
	private ActionOpenProfile actionOpenProfile; 
	private ActionSaveProfile actionSaveProfile;
	private ActionQuit actionQuit;
	private ActionCloseDatabase actionCloseDatabase;
	private ActionAddDocument actionAddDocument;
	private ActionRemoveDocuments actionRemoveDocuments;
	private ActionEditDocuments actionEditDocuments;
	private ActionRefresh actionRefresh;
	private ActionBatchImportDocuments actionBatchImportDocuments;
	private ActionRemoveStatements actionRemoveStatements;
	private ActionLoggerDialog actionLoggerDialog;
	private ActionAboutWindow actionAboutWindow;

	// TODO: reorder methods and classes in main window, popup, text panel, document table, and statement table panel classes
	// TODO: add javadoc to the aforementioned classes and methods
	// TODO: popup colouring and window decoration have a bug: sometimes multiple popups shown after switching
	// TODO: remove toolbar listener interface and move the document listener here into the main window class for controlling the document table filter
	// TODO: when a statement popup is closed, unselect the statement in the statement table
	// TODO: double-check if interaction between statement table selection, document selection, and popups in the text panel works well
	// TODO: double-check if there is view-specific code in the main window class that can be moved into the view components
	// TODO: probably the statement table should be refreshed every time the document table is refreshed (?)
	// TODO: ensure statements are sorted chronologically and by position in the statement table (implement Comparable interface in Statement class?)
	// TODO: take into account coder permissions and relations everywhere in the controls in the main window class
	// TODO: popups with window decoration should check if the contents have changed when closed, instead of just asking anyway if changes should be saved
	// TODO: make the attribute table and usage more flexible, with additional variables
	
	/**
	 * Create a new main window.
	 */
	public MainWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			System.err.println("Unsupport look and feel. Using default theme.");
	    } catch (ClassNotFoundException e) {
			System.err.println("Class not found. Using default theme.");
	    } catch (InstantiationException e) {
			System.err.println("Instantiation exception. Using default theme.");
	    } catch (IllegalAccessException e) {
			System.err.println("Illegal access exception. Using default theme.");
	    }

		c = getContentPane();
		this.setTitle("Discourse Network Analyzer");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
		this.setIconImage(dna32Icon.getImage());
		
		// close SQL connection before exit
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// documentTableModel = null;
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"Exiting DNA from the GUI main window.",
						"Exiting DNA from the GUI main window.");
				Dna.logger.log(l);
				System.exit(0);
			}
		});
		
		// initialize actions
		ImageIcon openDatabaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionOpenDatabase = new ActionOpenDatabase("Open DNA database", openDatabaseIcon, "Open a dialog window to establish a connection to a remote or file-based database", KeyEvent.VK_O);

		ImageIcon closeDatabaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionCloseDatabase = new ActionCloseDatabase("Close database", closeDatabaseIcon, "Close the connection to the current database and reset graphical user interface", KeyEvent.VK_X);
		actionCloseDatabase.setEnabled(false);
		
		ImageIcon createDatabaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-plus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionCreateDatabase = new ActionCreateDatabase("Create new DNA database", createDatabaseIcon, "Open a dialog window to create a new remote or file-based database", KeyEvent.VK_C);
		
		ImageIcon openProfileIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-link.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionOpenProfile = new ActionOpenProfile("Open connection profile", openProfileIcon, "Open a connection profile, which acts as a bookmark to a database", KeyEvent.VK_P);
		
		ImageIcon saveProfileIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-download.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionSaveProfile = new ActionSaveProfile("Save connection profile", saveProfileIcon, "Save a connection profile, which acts as a bookmark to a database", KeyEvent.VK_S);
		actionSaveProfile.setEnabled(false);
		
		ImageIcon quitIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-logout.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionQuit = new ActionQuit("Exit / quit", quitIcon, "Close the Discourse Network Analyzer", KeyEvent.VK_Q);
		
		ImageIcon addDocumentIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-file-plus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionAddDocument = new ActionAddDocument("Add document", addDocumentIcon, "Open a dialog window to enter details of a new document", KeyEvent.VK_A);
		actionAddDocument.setEnabled(false);
		
		ImageIcon removeDocumentsIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-file-minus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionRemoveDocuments = new ActionRemoveDocuments("Remove document(s)", removeDocumentsIcon, "Remove the document(s) currently selected in the document table", KeyEvent.VK_R);
		actionRemoveDocuments.setEnabled(false);
		
		ImageIcon editDocumentsIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-edit.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionEditDocuments = new ActionEditDocuments("Edit document(s)", editDocumentsIcon, "Edit the document(s) currently selected in the document table", KeyEvent.VK_E);
		actionEditDocuments.setEnabled(false);
		
		ImageIcon documentTableRefreshIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-refresh.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionRefresh = new ActionRefresh("Refresh document table", documentTableRefreshIcon, "Fetch new documents from the database and insert them into the document table and remove deleted rows from the table", KeyEvent.VK_F);
		actionRefresh.setEnabled(false);

		ImageIcon batchImportDocumentsIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-file-import.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionBatchImportDocuments = new ActionBatchImportDocuments("Import from directory", batchImportDocumentsIcon, "Batch-import all text files from a folder as new documents", KeyEvent.VK_I);
		actionBatchImportDocuments.setEnabled(false);

		ImageIcon removeStatementsIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-square-minus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionRemoveStatements = new ActionRemoveStatements("Remove statement(s)", removeStatementsIcon, "Remove the statement(s) currently selected in the statement table", KeyEvent.VK_D);
		actionRemoveStatements.setEnabled(false);
		
		ImageIcon aboutIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/dna32.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionAboutWindow = new ActionAboutWindow("About DNA", aboutIcon, "Display information about DNA", KeyEvent.VK_B);
		
		ImageIcon loggerIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-message-report.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionLoggerDialog = new ActionLoggerDialog("Message log", loggerIcon, "Display a log of messages, warnings, and errors in a dialog window", KeyEvent.VK_L);
		
		// define models
		documentTableModel = new DocumentTableModel();
		statementTableModel = new StatementTableModel();
		
		// define GUI elements
		ToolbarPanel toolbar = new ToolbarPanel(documentTableModel,
				actionAddDocument,
				actionRemoveDocuments,
				actionEditDocuments,
				actionRefresh,
				actionRemoveStatements);
		documentTablePanel = new DocumentTablePanel(documentTableModel,
				actionAddDocument,
				actionRemoveDocuments,
				actionEditDocuments);
		MenuBar menuBar = new MenuBar(actionOpenDatabase,
				actionCloseDatabase,
				actionCreateDatabase,
				actionOpenProfile,
				actionSaveProfile,
				actionQuit,
				actionAddDocument,
				actionRemoveDocuments,
				actionEditDocuments,
				actionBatchImportDocuments,
				actionRefresh,
				actionRemoveStatements,
				actionLoggerDialog,
				actionAboutWindow);
		statusBar = new StatusBar();
		statementPanel = new StatementPanel(statementTableModel, actionRemoveStatements);
		textPanel = new TextPanel(statementPanel);
		
		// add listeners
		Dna.addSqlListener(this);
		Dna.logger.addListener(statusBar);
		Dna.addCoderListener(textPanel);
		Dna.addCoderListener(documentTablePanel);
		Dna.addSqlListener(statementPanel);
		Dna.addCoderListener(statementPanel);
		Dna.addSqlListener(toolbar);
		Dna.addCoderListener(toolbar);
		toolbar.addToolbarListener(documentTablePanel);
		
		// layout
		JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, documentTablePanel, textPanel);
		verticalSplitPane.setOneTouchExpandable(true);
		JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplitPane, statementPanel);
		rightSplitPane.setOneTouchExpandable(true);
		
		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.add(toolbar, BorderLayout.NORTH);
		innerPanel.add(rightSplitPane, BorderLayout.CENTER);
		innerPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(menuBar, BorderLayout.NORTH);
		mainPanel.add(innerPanel, BorderLayout.CENTER);
		mainPanel.add(statusBar, BorderLayout.SOUTH);
		
		// selection listener for the statement table; select statement or enable remove statements action
		JTable statementTable = statementPanel.getStatementTable();
		statementTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				
				int rowCount = statementTable.getSelectedRowCount();
				if (rowCount > 0) {
					actionRemoveStatements.setEnabled(true);
				} else {
					actionRemoveStatements.setEnabled(false);
				}
				if (rowCount == 1) {
					int selectedRow = statementTable.getSelectedRow();
					int selectedModelIndex = statementTable.convertRowIndexToModel(selectedRow);
					Statement s = statementTableModel.getRow(selectedModelIndex);
					
					documentTablePanel.setSelectedDocumentId(s.getDocumentId());
					
					boolean editable = false;
					if (Dna.sql.getActiveCoder().getPermissionEditStatements() == 1 &&
							(Dna.sql.getActiveCoder().getPermissionEditOthersStatements() == 1 ||
							(Dna.sql.getActiveCoder().getPermissionEditOthersStatements() == 0 && Dna.sql.getActiveCoder().getId() == s.getCoderId()))) {
						editable = true;
					}
					textPanel.selectStatement(s, s.getDocumentId(), editable);
				}
			}
		});
		
		// selection listener for the document table; set contents of the text panel,
		// update statement table, and enable or disable actions
		JTable documentTable = documentTablePanel.getDocumentTable();
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
					textPanel.setContents(-1, "");
					statementPanel.setDocumentId(-1);
					statementTableModel.fireTableDataChanged();
				} else if (rowCount > 1) {
					int[] selectedRows = documentTable.getSelectedRows();
					int[] selectedDocumentIds = new int[selectedRows.length];
					for (int i = 0; i < selectedRows.length; i++) {
						selectedDocumentIds[i] = documentTableModel.getIdByModelRow(documentTable.convertRowIndexToModel(selectedRows[i]));
					}
					textPanel.setContents(-1, "");
					statementPanel.setDocumentId(-1);
					statementTableModel.fireTableDataChanged();
				} else if (rowCount == 1) {
					int selectedRow = documentTable.getSelectedRow();
					int selectedModelIndex = documentTable.convertRowIndexToModel(selectedRow);
					int id = (int) documentTableModel.getValueAt(selectedModelIndex, 0);
					textPanel.setContents(id, documentTableModel.getDocumentText(id));
					statementPanel.setDocumentId(id);
					statementTableModel.fireTableDataChanged();
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

		// MouseListener for text window to show popups or context menu in text area; one method for Windows and one for Unix
		JTextPane textWindow = textPanel.getTextWindow();
		textWindow.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				try {
					mouseListenPopup(me);
				} catch (ArrayIndexOutOfBoundsException ex) {
					//no documents available
				}
			}
			public void mousePressed(MouseEvent me) {
				try {
					mouseListenPopup(me);
				} catch (ArrayIndexOutOfBoundsException ex) {
					//no documents available
				}
			}

			public void mouseClicked(MouseEvent me) {
				try {
					mouseListenSelect(me);
				} catch (ArrayIndexOutOfBoundsException ex) {
					//no documents available
				}
			}

			/**
			 * Show a text popup menu upon right mouse click to insert statements.
			 * 
			 * @param comp  The AWT component on which to draw the dialog window.
			 * @param x     The horizontal coordinate where the dialog should be shown.
			 * @param y     The vertical coordinate where the dialog should be shown.
			 */
			private void popupMenu(Component comp, int x, int y) {
				JPopupMenu popmen = new JPopupMenu();
				ArrayList<StatementType> statementTypes = Dna.sql.getStatementTypes();
				for (int i = 0; i < statementTypes.size(); i++) {
					StatementType statementType = statementTypes.get(i);
					JMenuItem menuItem = new JMenuItem("Format as " + statementType.getLabel());
					menuItem.setOpaque(true);
					menuItem.setBackground(statementType.getColor());
					popmen.add(menuItem);
					
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							int selectionStart = textWindow.getSelectionStart();
							int selectionEnd = textWindow.getSelectionEnd();
							Statement statement = new Statement(selectionStart,
									selectionEnd,
									statementType.getId(),
									Dna.sql.getActiveCoder().getId(),
									statementType.getVariables());
							Dna.sql.addStatement(statement, documentTablePanel.getSelectedDocumentId());
							documentTableModel.increaseFrequency(documentTablePanel.getSelectedDocumentId());
							textPanel.paintStatements();
							textWindow.setCaretPosition(selectionEnd);
						}
					});
				}
				popmen.show(comp, x, y);
			}

			/**
			 * Add a new statement and display a popup dialog window with the
			 * new statement.
			 * 
			 * @param me  A mouse event.
			 * @throws ArrayIndexOutOfBoundsException
			 */
			private void mouseListenPopup(MouseEvent me) throws ArrayIndexOutOfBoundsException {
				if (me.isPopupTrigger()) {
					if (!(textWindow.getSelectedText() == null) && Dna.sql.getActiveCoder() != null && Dna.sql.getActiveCoder().getPermissionAddStatements() == 1) {
						popupMenu(me.getComponent(), me.getX(), me.getY());
					}
				}
			}

			/**
			 * Check the current position in the text for statements and display
			 * a statement popup window if there is a statement.
			 * 
			 * @param me  The mouse event that triggers the popup window,
			 *   including the location.
			 * @throws ArrayIndexOutOfBoundsException
			 */
			private void mouseListenSelect(MouseEvent me) throws ArrayIndexOutOfBoundsException {
				if (me.isPopupTrigger()) {
					if (!(textWindow.getSelectedText() == null)) {
						popupMenu(me.getComponent(), me.getX(), me.getY());
					}
				} else {
					int pos = textWindow.getCaretPosition(); //click caret position
					Point p = me.getPoint();
					
					ArrayList<Statement> statements = Dna.sql.getStatements(documentTablePanel.getSelectedDocumentId());
					if (statements != null && statements.size() > 0) {
						for (int i = 0; i < statements.size(); i++) {
							if (statements.get(i).getStart() < pos
									&& statements.get(i).getStop() > pos
									&& Dna.sql.getActiveCoder() != null
									&& (Dna.sql.getActiveCoder().getPermissionViewOthersStatements() == 1 || statements.get(i).getCoderId() == Dna.sql.getActiveCoder().getId())
									// TODO here: check also the CODERRELATIONS table
									) {
								statementPanel.setSelectedStatementId(statements.get(i).getId());
								Point location = textWindow.getLocationOnScreen();
								textWindow.setSelectionStart(statements.get(i).getStart());
								textWindow.setSelectionEnd(statements.get(i).getStop());
								/*
									int row = Dna.gui.rightPanel.statementPanel.ssc.getIndexByStatementId(statementId);
									if (row > -1) {
										Dna.gui.rightPanel.statementPanel.statementTable.setRowSelectionInterval(row, row);
										Dna.gui.rightPanel.statementPanel.statementTable.scrollRectToVisible(new Rectangle(  // scroll to selected row
												Dna.gui.rightPanel.statementPanel.statementTable.getCellRect(i, 0, true)));
									}

									int docModelIndex = Dna.gui.documentPanel.documentContainer.getModelIndexById(Dna.data.getStatements().get(i).getDocumentId());
									int docRow = Dna.gui.documentPanel.documentTable.convertRowIndexToView(docModelIndex);
									//int docRow = Dna.dna.gui.documentPanel.documentContainer.getRowIndexById(Dna.data.getStatements().get(i).getDocumentId());
									Dna.gui.documentPanel.documentTable.scrollRectToVisible(new Rectangle(Dna.gui.documentPanel.documentTable.getCellRect(docRow, 0, true)));
									if (b[1] == true) {  // statement is editable by the active coder
										new Popup(p.getX(), p.getY(), statementId, location, true);
									} else {
										new Popup(p.getX(), p.getY(), statementId, location, false);
									}
								 */
								new Popup(p.getX(), p.getY(), statements.get(i), documentTablePanel.getSelectedDocumentId(), location, Dna.sql.getActiveCoder(), statementPanel);
								break;
								//}
							}
						}
					}
				}
			}
		});

		c.add(mainPanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Retrieve the text panel.
	 * 
	 * @return Text panel.
	 */
	TextPanel getTextPanel() {
		return textPanel;
	}

	/**
	 * Retrieve the document table model.
	 * 
	 * @return Document table model.
	 */
	DocumentTableModel getDocumentTableModel() {
		return documentTableModel;
	}

	/**
	 * Refresh the document table using a Swing worker in the background.
	 */
	void refreshDocumentTable() {
		if (Dna.sql == null) {
			documentTableModel.clear();
		} else {
	        DocumentTableRefreshWorker documentWorker = new DocumentTableRefreshWorker();
	        documentWorker.execute();
		}
	}

	/**
	 * Refresh the statement table using a Swing worker in the background.
	 */
	void refreshStatementTable() {
		if (Dna.sql == null) {
			statementTableModel.clear();
		} else {
	        StatementTableRefreshWorker statementWorker = new StatementTableRefreshWorker();
	        statementWorker.execute();
		}
	}

	/**
	 * Swing worker class for loading documents from the database and adding
	 * them to the document table in a background thread.
	 * 
	 * @see <a href="https://stackoverflow.com/questions/43161033/cant-add-tablerowsorter-to-jtable-produced-by-swingworker" target="_top">https://stackoverflow.com/questions/43161033/</a>
	 * @see <a href="https://stackoverflow.com/questions/68884145/how-do-i-use-a-jdbc-swing-worker-with-connection-pooling-ideally-while-separati" target="_top">https://stackoverflow.com/questions/68884145/</a>
	 */
	private class DocumentTableRefreshWorker extends SwingWorker<List<TableDocument>, TableDocument> {
		/**
		 * Time stamp to measure the duration it takes to update the table. The
		 * duration is logged when the table has been updated.
		 */
		private long time;
		
		/**
		 * ID of the selected document to reinstated the selection after
		 * repopulating the document table.
		 */
		private int selectedId;

		/**
		 * Vertical scroll position of the scroll pane in the text window before
		 * reloading the documents, in order to reinstate the scroll position
		 * after refreshing the document table.
		 */
		private int verticalScrollLocation;

		/**
		 * Create a new document table swing worker.
		 */
		private DocumentTableRefreshWorker() {
			actionAddDocument.setEnabled(false);
			actionRemoveDocuments.setEnabled(false);
			actionEditDocuments.setEnabled(false);
			actionBatchImportDocuments.setEnabled(false);
			actionRefresh.setEnabled(false);
			statusBar.documentRefreshStarted();
			time = System.nanoTime(); // take the time to compute later how long the updating took
			verticalScrollLocation = textPanel.getVerticalScrollLocation();
			selectedId = documentTablePanel.getSelectedDocumentId(); // remember the document ID to select the same document when done
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
	    	documentTablePanel.setSelectedDocumentId(selectedId);
	    	textPanel.setVerticalScrollLocation(this.verticalScrollLocation);
	    }

	    @Override
	    protected void done() {
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
			statusBar.documentRefreshEnded();
			if (!statusBar.isRefreshInProgress()) {
				actionAddDocument.setEnabled(true);
				actionRemoveDocuments.setEnabled(true);
				actionEditDocuments.setEnabled(true);
				actionBatchImportDocuments.setEnabled(true);
				actionRefresh.setEnabled(true);
			}
	    }
	}

	/**
	 * Swing worker class for loading statements from the database and adding
	 * them to the statement table in a background thread. The class contains
	 * SQL code right here instead of moving the SQL code to the {@link sql.Sql
	 * Sql} class because it contains nested {@code publish} calls that need
	 * to be in the SQL code but also need to remain in the Swing worker class. 
	 */
	private class StatementTableRefreshWorker extends SwingWorker<List<Statement>, Statement> {
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
		 * A Swing worker that reloads all statements from the database and
		 * stores them in the table model for displaying them in the statement
		 * table.
		 */
		private StatementTableRefreshWorker() {
			actionRefresh.setEnabled(false);
    		time = System.nanoTime(); // take the time to compute later how long the updating took
    		statusBar.statementRefreshStart();
    		selectedId = statementPanel.getSelectedStatementId();
    		statementTableModel.clear(); // remove all documents from the table model before re-populating the table
			LogEvent le = new LogEvent(Logger.MESSAGE,
					"[GUI] Initializing thread to populate statement table: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").",
					"A new swing worker thread has been started to populate the statement table with statements from the database in the background: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").");
			Dna.logger.log(le);
		}
		
		@Override
		protected List<Statement> doInBackground() {
			String query = "SELECT STATEMENTS.ID AS StatementId, "
					+ "StatementTypeId, "
					+ "STATEMENTTYPES.Label AS StatementTypeLabel, "
					+ "STATEMENTTYPES.Red AS StatementTypeRed, "
					+ "STATEMENTTYPES.Green AS StatementTypeGreen, "
					+ "STATEMENTTYPES.Blue AS StatementTypeBlue, "
					+ "Start, "
					+ "Stop, "
					+ "STATEMENTS.Coder AS CoderId, "
					+ "CODERS.Name AS CoderName, "
					+ "CODERS.Red AS CoderRed, "
					+ "CODERS.Green AS CoderGreen, "
					+ "CODERS.Blue AS CoderBlue, "
					+ "DocumentId, "
					+ "DOCUMENTS.Date AS Date, "
					+ "SUBSTRING(DOCUMENTS.Text, Start + 1, Stop - Start) AS Text "
					+ "FROM STATEMENTS "
					+ "INNER JOIN CODERS ON STATEMENTS.Coder = CODERS.ID "
					+ "INNER JOIN STATEMENTTYPES ON STATEMENTS.StatementTypeId = STATEMENTTYPES.ID "
					+ "INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId ORDER BY DOCUMENTS.DATE ASC;";
			ArrayList<Value> values;
			int statementId, statementTypeId, variableId;
			String variable, dataType;
			Color aColor, sColor, cColor;
			try (Connection conn = Dna.sql.getDataSource().getConnection();
					PreparedStatement s1 = conn.prepareStatement(query);
					PreparedStatement s2 = conn.prepareStatement("SELECT ID, Variable, DataType FROM VARIABLES WHERE StatementTypeId = ?;");
					PreparedStatement s3 = conn.prepareStatement("SELECT A.ID AS AttributeId, StatementId, A.VariableId, DST.ID AS DataId, A.Value, Red, Green, Blue, Type, Alias, Notes, ChildOf FROM DATASHORTTEXT AS DST LEFT JOIN ATTRIBUTES AS A ON A.ID = DST.Value AND A.VariableId = DST.VariableId WHERE DST.StatementId = ? AND DST.VariableId = ?;");
					PreparedStatement s4 = conn.prepareStatement("SELECT Value FROM DATALONGTEXT WHERE VariableId = ? AND StatementId = ?;");
					PreparedStatement s5 = conn.prepareStatement("SELECT Value FROM DATAINTEGER WHERE VariableId = ? AND StatementId = ?;");
					PreparedStatement s6 = conn.prepareStatement("SELECT Value FROM DATABOOLEAN WHERE VariableId = ? AND StatementId = ?;")) {
				ResultSet r1, r2, r3;
				r1 = s1.executeQuery();
				while (r1.next()) {
				    statementId = r1.getInt("StatementId");
				    statementTypeId = r1.getInt("StatementTypeId");
				    sColor = new Color(r1.getInt("StatementTypeRed"), r1.getInt("StatementTypeGreen"), r1.getInt("StatementTypeBlue"));
				    cColor = new Color(r1.getInt("CoderRed"), r1.getInt("CoderGreen"), r1.getInt("CoderBlue"));
				    s2.setInt(1, statementTypeId);
				    r2 = s2.executeQuery();
				    values = new ArrayList<Value>();
				    while (r2.next()) {
				    	variableId = r2.getInt("ID");
				    	variable = r2.getString("Variable");
				    	dataType = r2.getString("DataType");
				    	if (dataType.equals("short text")) {
					    	s3.setInt(1, statementId);
					    	s3.setInt(2, variableId);
					    	r3 = s3.executeQuery();
					    	while (r3.next()) {
				            	aColor = new Color(r3.getInt("Red"), r3.getInt("Green"), r3.getInt("Blue"));
				            	Attribute attribute = new Attribute(r3.getInt("AttributeId"), r3.getString("Value"), aColor, r3.getString("Type"), r3.getString("Alias"), r3.getString("Notes"), r3.getInt("ChildOf"), true);
					    		values.add(new Value(variableId, variable, dataType, attribute));
					    	}
				    	} else if (dataType.equals("long text")) {
					    	s4.setInt(1, variableId);
					    	s4.setInt(2, statementId);
					    	r3 = s4.executeQuery();
					    	while (r3.next()) {
					    		values.add(new Value(variableId, variable, dataType, r3.getString("Value")));
					    	}
				    	} else if (dataType.equals("integer")) {
					    	s5.setInt(1, variableId);
					    	s5.setInt(2, statementId);
					    	r3 = s5.executeQuery();
					    	while (r3.next()) {
					    		values.add(new Value(variableId, variable, dataType, r3.getInt("Value")));
					    	}
				    	} else if (dataType.equals("boolean")) {
					    	s6.setInt(1, variableId);
					    	s6.setInt(2, statementId);
					    	r3 = s6.executeQuery();
					    	while (r3.next()) {
					    		values.add(new Value(variableId, variable, dataType, r3.getInt("Value")));
					    	}
				    	}
				    }
				    Statement statement = new Statement(statementId,
				    		r1.getInt("Start"),
				    		r1.getInt("Stop"),
				    		statementTypeId,
				    		r1.getString("StatementTypeLabel"),
				    		sColor,
				    		r1.getInt("CoderId"),
				    		r1.getString("CoderName"),
				    		cColor,
				    		values,
				    		r1.getInt("DocumentId"),
				    		r1.getString("Text"),
				    		LocalDateTime.ofEpochSecond(r1.getLong("Date"), 0, ZoneOffset.UTC));
				    publish(statement);
				}
			} catch (SQLException e) {
				LogEvent l = new LogEvent(Logger.WARNING,
						"[SQL] Failed to retrieve statements.",
						"Attempted to retrieve all statements from the database, but something went wrong. You should double-check if the statements are all shown!",
						e);
				Dna.logger.log(l);
			}
			return null;
		}
        
        @Override
        protected void process(List<Statement> chunks) {
        	statementTableModel.addRows(chunks); // transfer a batch of rows to the statement table model
			statementPanel.setSelectedStatementId(selectedId); // select the statement from before; skipped if the statement not found in this batch
        }

        @Override
        protected void done() {
        	statusBar.statementRefreshEnd(); // stop displaying the update message in the status bar
			statementTableModel.fireTableDataChanged(); // update the statement filter
			statementPanel.setSelectedStatementId(selectedId);
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
			if (!statusBar.isRefreshInProgress()) {
				actionRefresh.setEnabled(true);
			}
        }
    }

	/**
	 * React to changes in the state (= presence or absence) of the DNA database
	 * in the {@link dna.Dna Dna} class.
	 */
	public void adjustToDatabaseState() {
    	if (Dna.sql != null) {
			actionAddDocument.setEnabled(true);
			actionBatchImportDocuments.setEnabled(true);
			actionRefresh.setEnabled(true);
			if (actionCloseDatabase != null) {
				actionCloseDatabase.setEnabled(true);
			}
			if (actionSaveProfile != null) {
				actionSaveProfile.setEnabled(true);
			}
    	} else {
			actionAddDocument.setEnabled(false);
			actionBatchImportDocuments.setEnabled(false);
			actionRefresh.setEnabled(false);
			if (actionCloseDatabase != null) {
				actionCloseDatabase.setEnabled(false);
			}
			if (actionSaveProfile != null) {
				actionSaveProfile.setEnabled(false);
			}
    	}
    	refreshDocumentTable();
    	refreshStatementTable();
	}

	/**
	 * An action to close the open SQL database.
	 */
	class ActionCloseDatabase extends AbstractAction {
		private static final long serialVersionUID = -4463742124397662610L;
		public ActionCloseDatabase(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		public void actionPerformed(ActionEvent e) {
			Dna.setSql(null);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: closed database.",
					"Closed database connection from the GUI.");
			Dna.logger.log(l);
		}
	}
	
	/**
	 * An action to display a new database dialog to create a new DNA database.
	 */
	class ActionCreateDatabase extends AbstractAction {
		private static final long serialVersionUID = -9019267411134217476L;

		public ActionCreateDatabase(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			NewDatabaseDialog n = new NewDatabaseDialog(false);
			ConnectionProfile cp = n.getConnectionProfile();
			if (cp != null) {
				Dna.setSql(new Sql(cp));
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"[GUI] Action executed: created new database.",
						"Created a new database from the GUI.");
				Dna.logger.log(l);
			}
		}
	}
	
	/**
	 * An action to display a file chooser and open a connection profile.
	 */
	class ActionOpenProfile extends AbstractAction {
		private static final long serialVersionUID = -1985734783855268915L;
		
		public ActionOpenProfile(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			String filename = null;
			boolean validFileInput = false;
			while (!validFileInput) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".dnc") || f.isDirectory();
					}
					public String getDescription() {
						return "DNA connection profile (*.dnc)";
					}
				});
				int returnVal = fc.showOpenDialog(null);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (file.exists()) {
						filename = new String(file.getPath());
						if (!filename.endsWith(".dnc")) {
							filename = filename + ".dnc";
						}
						validFileInput = true; // file choice accepted
					} else {
						JOptionPane.showMessageDialog(null, "The file name you entered does not exist. Please choose a new file.");
					}
				} else { // cancel button; mark as valid file input, but reset file name
					filename = null;
					validFileInput = true;
				}
			}
			
			if (filename != null) { // if file has been chosen successfully, go on with authentication
				boolean validPasswordInput = false;
				while (!validPasswordInput) {
					// ask user for password (for the user in the connection profile) to decrypt the profile
					CoderPasswordCheckDialog d = new CoderPasswordCheckDialog();
					String key = d.getPassword();
					
					// decrypt connection profile, create SQL connection, and set as default SQL connection
					if (key == null) {
						validPasswordInput = true; // user must have pressed cancel; quit the while-loop
					} else {
						if (!key.equals("")) {
							ConnectionProfile cp = null;
							try {
								cp = readConnectionProfile(filename, key);
							} catch (EncryptionOperationNotPossibleException e2) {
								cp = null;
							}
							if (cp != null) {
								Sql sqlTemp = new Sql(cp);
								boolean authenticated = sqlTemp.authenticate(key);
								if (authenticated == true) {
									validPasswordInput = true; // authenticated; quit the while-loop
									Dna.setSql(sqlTemp);
								} else {
									cp = null;
								}
							}
							if (cp == null) {
								JOptionPane.showMessageDialog(null,
					        			"Database credentials could not be decrypted.\n"
					        					+ "Did you enter the right password?",
									    "Check failed",
									    JOptionPane.ERROR_MESSAGE);
							}
						} else {
							JOptionPane.showMessageDialog(null,
				        			"Password check failed. Zero-length passwords are not permitted.",
								    "Check failed",
								    JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: opened connection profile.",
					"Opened a connection profile from the GUI.");
			Dna.logger.log(l);
		}

		/**
		 * Read in a saved connection profile from a JSON file, decrypt the
		 * credentials, and return the connection profile.
		 * 
		 * @param file  The file name including path of the JSON connection profile
		 * @param key   The key/password of the coder to decrypt the credentials
		 * @return      Decrypted connection profile
		 */
		private ConnectionProfile readConnectionProfile(String file, String key) throws EncryptionOperationNotPossibleException {

			// read connection profile JSON file in, in String format but with encrypted credentials
			ConnectionProfile cp = null;
			Gson gson = new Gson();
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				cp = gson.fromJson(br, ConnectionProfile.class);
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				LogEvent l = new LogEvent(Logger.ERROR,
						"[GUI] Failed to read connection profile.",
						"Tried to read a connection profile from a JSON file and failed.",
						e);
				Dna.logger.log(l);
			}
			
			// decrypt the URL, user name, and SQL connection password inside the profile
			AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
			textEncryptor.setPassword(key);
			cp.setUrl(textEncryptor.decrypt(cp.getUrl()));
			cp.setUser(textEncryptor.decrypt(cp.getUser()));
			cp.setPassword(textEncryptor.decrypt(cp.getPassword()));
			
			return cp;
		}
	}

	/**
	 * An action to display a file chooser and save a connection profile.
	 */
	class ActionSaveProfile extends AbstractAction {
		private static final long serialVersionUID = 6515595073332160633L;
		
		public ActionSaveProfile(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			String filename = null;
			File file = null;
			boolean validFileInput = false;
			while (!validFileInput) {
				JFileChooser fc;
				if (file == null) {
					fc = new JFileChooser();
				} else {
					fc = new JFileChooser(file);
				}
				fc.setDialogTitle("Save connection profile...");
				fc.setApproveButtonText("Save");
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".dnc") || f.isDirectory();
					}
					public String getDescription() {
						return "DNA connection profile (*.dnc)";
					}
				});
				int returnVal = fc.showOpenDialog(null);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					file = fc.getSelectedFile();
					filename = new String(file.getPath());
					if (!filename.endsWith(".dnc")) {
						filename = filename + ".dnc";
					}
					file = new File(filename);
					if (!file.exists()) {
						validFileInput = true; // file approved
					} else {
						file = null;
						JOptionPane.showMessageDialog(null, "The file name you entered already exists. Please choose a new file.");
					}
				} else {
					validFileInput = true; // user must have clicked cancel in file chooser
				}
			}
			// after getting a valid file, authenticate coder and write to file
			if (file != null) {
				boolean validPasswordInput = false;
				while (!validPasswordInput) {
					CoderPasswordCheckDialog d = new CoderPasswordCheckDialog(Dna.sql, false);
					String key = d.getPassword();
					if (key == null) { // user must have pressed cancel
						validPasswordInput = true;
					} else {
						boolean authenticated = Dna.sql.authenticate(key);
						if (authenticated == true) {
							// write the connection profile to disk, with an encrypted version of the password
							writeConnectionProfile(filename, Dna.sql.getConnectionProfile(), key);
							validPasswordInput = true; // quit the while-loop after successful export
							JOptionPane.showMessageDialog(null,
									"The profile was saved as:\n" + new File(filename).getAbsolutePath(),
									"Success",
								    JOptionPane.PLAIN_MESSAGE);
						} else {
				        	JOptionPane.showMessageDialog(null,
				        			"Coder password could not be verified. Try again.",
								    "Check failed",
								    JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: saved connection profile.",
					"Saved a connection profile from the GUI.");
			Dna.logger.log(l);
		}

		/**
		 * Take a decrypted connection profile, encrypt the credentials, and write
		 * it to a JSON file on disk.
		 * 
		 * @param file  The file name including full path as a String
		 * @param cp    The connection profile to be encrypted and saved
		 * @param key   The key/password of the coder to encrypt the credentials
		 */
		private void writeConnectionProfile(String file, ConnectionProfile cp, String key) {
			// encrypt URL, user, and password using Jasypt
			AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
			textEncryptor.setPassword(key);
			cp.setUrl(textEncryptor.encrypt(cp.getUrl()));
			cp.setUser(textEncryptor.encrypt(cp.getUser()));
			cp.setPassword(textEncryptor.encrypt(cp.getPassword()));
			
			// serialize Connection object to JSON file and save to disk
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				Gson gson = new Gson();
				gson.toJson(cp, writer);
			} catch (IOException e) {
				LogEvent l = new LogEvent(Logger.ERROR,
						"[GUI] Failed to write connection profile.",
						"Tried to write a connection profile to a JSON file and failed.",
						e);
				Dna.logger.log(l);
			}
		}
	}
	
	/**
	 * An action to quit DNA.
	 */
	class ActionQuit extends AbstractAction {
		private static final long serialVersionUID = 3334696382161923841L;

		public ActionQuit(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: quit DNA.",
					"Quit DNA from the GUI.");
			Dna.logger.log(l);
			System.exit(0);
		}
	}
	
	/**
	 * An action to open a document editor dialog and add a new document.
	 */
	class ActionAddDocument extends AbstractAction {
		private static final long serialVersionUID = -3332492885668412485L;

		public ActionAddDocument(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			new DocumentEditor();
	    	refreshDocumentTable();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: added a new document.",
					"Added a new document from the GUI.");
			Dna.logger.log(l);
		}
	}
	
	/**
	 * An action to remove the selected documents.
	 */
	class ActionRemoveDocuments extends AbstractAction {
		private static final long serialVersionUID = -5326085918049798694L;

		public ActionRemoveDocuments(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			int[] selectedRows = documentTablePanel.getSelectedRows();
			String message = "Are you sure you want to delete " + selectedRows.length + " document(s) including all statements?";
			int dialog = JOptionPane.showConfirmDialog(null, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
			if (dialog == 0) {
				for (int i = 0; i < selectedRows.length; i++) {
					selectedRows[i] = documentTablePanel.convertRowIndexToModel(selectedRows[i]);
				}
				documentTableModel.removeDocuments(selectedRows);
			}
	    	refreshDocumentTable();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: removed document(s).",
					"Deleted one or more documents in the database from the GUI.");
			Dna.logger.log(l);
		}
	}
	
	/**
	 * An action to edit the selected document(s).
	 */
	class ActionEditDocuments extends AbstractAction {
		private static final long serialVersionUID = 4411902324874635324L;

		public ActionEditDocuments(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			int[] selectedRows = documentTablePanel.getSelectedRows();
			for (int i = 0; i < selectedRows.length; i++) {
				selectedRows[i] = documentTableModel.getIdByModelRow(documentTablePanel.convertRowIndexToModel(selectedRows[i]));
			}
			new DocumentEditor(selectedRows);
	    	refreshDocumentTable();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: edited meta-data for document(s).",
					"Edited the meta-data for one or more documents in the database.");
			Dna.logger.log(l);
		}
	}
	
	/**
	 * An action to start a dialog for batch-importing documents from a folder.
	 */
	class ActionBatchImportDocuments extends AbstractAction {
		private static final long serialVersionUID = -1460878736275897716L;
		
		public ActionBatchImportDocuments(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			new DocumentBatchImporter();
	    	refreshDocumentTable();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: used document batch importer.",
					"Batch-imported documents to the database.");
			Dna.logger.log(l);
		}
	}
	
	/**
	 * An action to reload the documents and statements from the database.
	 */
	class ActionRefresh extends AbstractAction {
		private static final long serialVersionUID = -5684628034931158710L;

		public ActionRefresh(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
	    	refreshDocumentTable();
			refreshStatementTable();
		}
	}

	/**
	 * An action to remove the selected statements from the database.
	 */
	class ActionRemoveStatements extends AbstractAction {
		private static final long serialVersionUID = -4938838325040431112L;

		public ActionRemoveStatements(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			int[] selectedRows = statementPanel.getSelectedRows();
			String message = "Are you sure you want to delete " + selectedRows.length + " statement(s)?";
			int dialog = JOptionPane.showConfirmDialog(null, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
			if (dialog == 0) {
				for (int i = 0; i < selectedRows.length; i++) {
					selectedRows[i] = statementPanel.convertRowIndexToModel(selectedRows[i]);
				}
				statementTableModel.removeStatements(selectedRows);
			}
			refreshStatementTable();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: removed statement(s).",
					"Deleted one or more statements in the database from the GUI.");
			Dna.logger.log(l);
		}
	}
	
	/**
	 * An action to open a new logger dialog window.
	 */
	class ActionLoggerDialog extends AbstractAction {
		private static final long serialVersionUID = -629086240908166990L;

		public ActionLoggerDialog(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			new LoggerDialog();
		}
	}
	
	/**
	 * An action to display a new About DNA window.
	 */
	class ActionAboutWindow extends AbstractAction {
		private static final long serialVersionUID = -9078666078201409563L;
		
		public ActionAboutWindow(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			new AboutWindow(Dna.version, Dna.date);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: opened About DNA window.",
					"Opened an About DNA window from the GUI.");
			Dna.logger.log(l);
		}
	}
}