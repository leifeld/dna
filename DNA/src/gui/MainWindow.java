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
import java.awt.geom.Rectangle2D;
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
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.AES256TextEncryptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import gui.DocumentTablePanel;
import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import logger.LoggerDialog;
import model.Coder;
import model.Statement;
import model.StatementType;
import model.TableDocument;
import model.Value;
import sql.ConnectionProfile;
import sql.Sql;

/**
 * Main window that instantiates and plugs the different view components
 * together. It contains controls for the different actions and defines
 * listeners that are attached to the view components to interact between the
 * components. 
 */
public class MainWindow extends JFrame {
	private static final long serialVersionUID = 2740437090361841747L;
	private Container c;
	private DocumentTablePanel documentTablePanel;
	private DocumentTableModel documentTableModel;
	private StatementPanel statementPanel;
	private StatementTableModel statementTableModel;
	private TextPanel textPanel;
	private MenuBar menuBar;
	private ToolbarPanel toolbar;
	private StatusBar statusBar;
	private CoderSelectionPanel coderSelectionPanel;
	private ActionOpenDatabase actionOpenDatabase;
	private ActionCreateDatabase actionCreateDatabase;
	private ActionOpenProfile actionOpenProfile; 
	private ActionSaveProfile actionSaveProfile;
	private ActionCoderManager actionCoderManager;
	private ActionQuit actionQuit;
	private ActionCloseDatabase actionCloseDatabase;
	private ActionAddDocument actionAddDocument;
	private ActionRemoveDocuments actionRemoveDocuments;
	private ActionEditDocuments actionEditDocuments;
	private ActionRefresh actionRefresh;
	private ActionBatchImportDocuments actionBatchImportDocuments;
	private ActionImporter actionImporter;
	private ActionRemoveStatements actionRemoveStatements;
	private ActionStatementTypeEditor actionStatementTypeEditor;
	private ActionAttributeManager actionAttributeManager;
	private ActionCoderRelationsEditor actionCoderRelationsEditor;
	private ActionLoggerDialog actionLoggerDialog;
	private ActionAboutWindow actionAboutWindow;

	/**
	 * A document table swing worker thread.
	 */
	private DocumentTableRefreshWorker documentTableWorker;

	/**
	 * A statement table swing worker thread.
	 */
	private StatementTableRefreshWorker statementTableWorker;
	
	/**
	 * Create a new main window.
	 */
	public MainWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"Set system look and feel for GUI.",
					"Set system look and feel for GUI.");
			Dna.logger.log(l);
		} catch (UnsupportedLookAndFeelException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"Unsupport look and feel. Using default theme.",
					"Unsupport look and feel. Using default theme.",
					e);
			Dna.logger.log(l);
	    } catch (ClassNotFoundException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"Look and feel class not found. Using default theme.",
					"Look and feel class not found. Using default theme.",
					e);
			Dna.logger.log(l);
	    } catch (InstantiationException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"Look and feel instantiation exception. Using default theme.",
					"Look and feel instantiation exception. Using default theme.",
					e);
			Dna.logger.log(l);
	    } catch (IllegalAccessException e) {
			LogEvent l = new LogEvent(Logger.WARNING,
					"Look and feel illegal access exception. Using default theme.",
					"Look and feel illegal access exception. Using default theme.",
					e);
			Dna.logger.log(l);
	    }
		
		/*
		 * TODO:
		 * - Add a fourth logger message category to track actual changes in the document only; streamline logger events
		 * - Fix any issues with resizing dialog windows and the main window on different operating systems
		 * - Add auto-refresh
		 * - Add regex editor
		 */

		c = getContentPane();
		this.setTitle("Discourse Network Analyzer");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
		this.setIconImage(dna32Icon.getImage());
		
		// close SQL connection before exit
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (documentTableWorker != null) {
					documentTableWorker.cancel(true);
				}
				if (statementTableWorker != null) {
					statementTableWorker.cancel(true);
				}
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"Exiting DNA from the GUI main window.",
						"Exiting DNA from the GUI main window.");
				Dna.logger.log(l);
				System.exit(0);
			}
		});
		
		// initialize actions
		ImageIcon openDatabaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionOpenDatabase = new ActionOpenDatabase("Open DNA database", openDatabaseIcon, "Open a dialog window to establish a connection to a remote or file-based database", KeyEvent.VK_O);

		ImageIcon closeDatabaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionCloseDatabase = new ActionCloseDatabase("Close database", closeDatabaseIcon, "Close the connection to the current database and reset graphical user interface", KeyEvent.VK_X);
		actionCloseDatabase.setEnabled(false);
		
		ImageIcon createDatabaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-plus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionCreateDatabase = new ActionCreateDatabase("Create new DNA database", createDatabaseIcon, "Open a dialog window to create a new remote or file-based database", KeyEvent.VK_C);
		
		ImageIcon openProfileIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-link.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionOpenProfile = new ActionOpenProfile("Open connection profile", openProfileIcon, "Open a connection profile, which acts as a bookmark to a database", KeyEvent.VK_P);
		
		ImageIcon saveProfileIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-download.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionSaveProfile = new ActionSaveProfile("Save connection profile", saveProfileIcon, "Save a connection profile, which acts as a bookmark to a database", KeyEvent.VK_S);
		actionSaveProfile.setEnabled(false);

		ImageIcon coderManagerIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-users.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionCoderManager = new ActionCoderManager("Open coder manager", coderManagerIcon, "Open the coder manager to edit coders and their permissions.", KeyEvent.VK_M);
		actionCoderManager.setEnabled(false);
		
		ImageIcon quitIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-logout.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionQuit = new ActionQuit("Exit / quit", quitIcon, "Close the Discourse Network Analyzer", KeyEvent.VK_Q);
		
		ImageIcon addDocumentIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-file-plus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionAddDocument = new ActionAddDocument("Add document", addDocumentIcon, "Open a dialog window to enter details of a new document", KeyEvent.VK_A);
		actionAddDocument.setEnabled(false);
		
		ImageIcon removeDocumentsIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-file-minus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionRemoveDocuments = new ActionRemoveDocuments("Remove document(s)", removeDocumentsIcon, "Remove the document(s) currently selected in the document table", KeyEvent.VK_R);
		actionRemoveDocuments.setEnabled(false);
		
		ImageIcon editDocumentsIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-edit.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionEditDocuments = new ActionEditDocuments("Edit document(s)", editDocumentsIcon, "Edit the document(s) currently selected in the document table", KeyEvent.VK_E);
		actionEditDocuments.setEnabled(false);
		
		ImageIcon documentTableRefreshIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-refresh.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionRefresh = new ActionRefresh("Refresh document table", documentTableRefreshIcon, "Fetch new documents from the database and insert them into the document table and remove deleted rows from the table", KeyEvent.VK_F);
		actionRefresh.setEnabled(false);

		ImageIcon batchImportDocumentsIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-file-import.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionBatchImportDocuments = new ActionBatchImportDocuments("Import from directory", batchImportDocumentsIcon, "Batch-import all text files from a folder as new documents", KeyEvent.VK_I);
		actionBatchImportDocuments.setEnabled(false);

		ImageIcon importerIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database-import.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionImporter = new ActionImporter("Import from DNA database", importerIcon, "Import documents, statements, entities, attributes, and regexes from another DNA database", KeyEvent.VK_D);
		actionImporter.setEnabled(false);

		ImageIcon removeStatementsIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-square-minus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionRemoveStatements = new ActionRemoveStatements("Remove statement(s)", removeStatementsIcon, "Remove the statement(s) currently selected in the statement table", KeyEvent.VK_D);
		actionRemoveStatements.setEnabled(false);

		ImageIcon statementTypeEditorIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-message-2.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionStatementTypeEditor = new ActionStatementTypeEditor("Edit statement types", statementTypeEditorIcon, "Open the statement type editor to edit statement types and their variables.", KeyEvent.VK_T);
		actionStatementTypeEditor.setEnabled(false);

		ImageIcon attributeManagerIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-list.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionAttributeManager = new ActionAttributeManager("Open attribute manager", attributeManagerIcon, "Open the attribute manager to edit entities and their attribute values.", KeyEvent.VK_A);
		actionAttributeManager.setEnabled(false);

		ImageIcon coderRelationsEditorIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-user-check.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionCoderRelationsEditor = new ActionCoderRelationsEditor("Edit coder relations", coderRelationsEditorIcon, "Open the coder relations editor define whose documents and statements you can view and edit.", KeyEvent.VK_R);
		actionCoderRelationsEditor.setEnabled(false);
		
		ImageIcon loggerIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-message-report.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionLoggerDialog = new ActionLoggerDialog("Display message log", loggerIcon, "Display a log of messages, warnings, and errors in a dialog window", KeyEvent.VK_L);
		
		ImageIcon aboutIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/dna32.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		actionAboutWindow = new ActionAboutWindow("About DNA", aboutIcon, "Display information about DNA", KeyEvent.VK_B);
		
		// define models
		documentTableModel = new DocumentTableModel();
		statementTableModel = new StatementTableModel();
		
		// define GUI elements
		toolbar = new ToolbarPanel(documentTableModel,
				actionAddDocument,
				actionRemoveDocuments,
				actionEditDocuments,
				actionRefresh,
				actionRemoveStatements);
		documentTablePanel = new DocumentTablePanel(documentTableModel,
				actionAddDocument,
				actionRemoveDocuments,
				actionEditDocuments);
		menuBar = new MenuBar(actionOpenDatabase,
				actionCloseDatabase,
				actionCreateDatabase,
				actionOpenProfile,
				actionSaveProfile,
				actionCoderManager,
				actionQuit,
				actionAddDocument,
				actionRemoveDocuments,
				actionEditDocuments,
				actionBatchImportDocuments,
				actionImporter,
				actionRefresh,
				actionRemoveStatements,
				actionStatementTypeEditor,
				actionAttributeManager,
				actionCoderRelationsEditor,
				actionLoggerDialog,
				actionAboutWindow);
		statusBar = new StatusBar();
		statementPanel = new StatementPanel(statementTableModel, actionRemoveStatements);
		textPanel = new TextPanel();
		coderSelectionPanel = new CoderSelectionPanel();
		
		// add listeners
		Dna.logger.addListener(statusBar);
		
		// layout
		JPanel documentsAndToolBarPanel = new JPanel(new BorderLayout());
		documentsAndToolBarPanel.add(toolbar, BorderLayout.NORTH);
		documentsAndToolBarPanel.add(documentTablePanel, BorderLayout.CENTER);
		
		JPanel statementsAndCoderPanel = new JPanel(new BorderLayout());
		statementsAndCoderPanel.add(coderSelectionPanel, BorderLayout.NORTH);
		statementsAndCoderPanel.add(getStatementPanel(), BorderLayout.CENTER);
		
		JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, documentsAndToolBarPanel, textPanel);
		verticalSplitPane.setOneTouchExpandable(true);
		JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplitPane, statementsAndCoderPanel);
		rightSplitPane.setOneTouchExpandable(true);
		rightSplitPane.setBorder(new EmptyBorder(0, 5, 0, 0));
		rightSplitPane.setResizeWeight(0.9); // right pane gets 10% of any extra space if resized (e.g., maximized)

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(menuBar, BorderLayout.NORTH);
		mainPanel.add(rightSplitPane, BorderLayout.CENTER);
		mainPanel.add(statusBar, BorderLayout.SOUTH);
		
		// selection listener for the statement table; select statement or enable remove statements action
		JTable statementTable = getStatementPanel().getStatementTable();
		statementTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				boolean editable = false;
				int rowCount = statementTable.getSelectedRowCount();
				if (rowCount == 1) {
					int selectedRow = statementTable.getSelectedRow();
					int selectedModelIndex = statementTable.convertRowIndexToModel(selectedRow);
					Statement statementWithoutValues = statementTableModel.getRow(selectedModelIndex);
					int statementId = statementWithoutValues.getId();
					Statement s = Dna.sql.getStatement(statementId);
					documentTablePanel.setSelectedDocumentId(statementWithoutValues.getDocumentId());
					if (Dna.sql.getActiveCoder().isPermissionDeleteStatements() == true &&
							(Dna.sql.getActiveCoder().isPermissionEditOthersStatements() == true ||
							Dna.sql.getActiveCoder().getId() == s.getCoderId()) &&
							(Dna.sql.getActiveCoder().getId() == s.getCoderId() ||
							Dna.sql.getActiveCoder().isPermissionEditOthersStatements(s.getCoderId()) == true)) {
						actionRemoveStatements.setEnabled(true);
					} else {
						actionRemoveStatements.setEnabled(false);
					}
					if (Dna.sql.getActiveCoder().isPermissionEditStatements() == true &&
							(Dna.sql.getActiveCoder().isPermissionEditOthersStatements() == true ||
							Dna.sql.getActiveCoder().getId() == s.getCoderId()) &&
							(Dna.sql.getActiveCoder().getId() == s.getCoderId() ||
									Dna.sql.getActiveCoder().isPermissionEditOthersStatements(s.getCoderId()) == true)) {
						editable = true;
					}
					if (Dna.sql.getActiveCoder().getId() == s.getCoderId() ||
							(Dna.sql.getActiveCoder().isPermissionViewOthersStatements() == true &&
									Dna.sql.getActiveCoder().isPermissionViewOthersStatements(s.getCoderId()) == true)) {
						selectStatement(s, s.getDocumentId(), editable);
					}
				} else if (rowCount > 1) {
					int[] rows = statementTable.getSelectedRows();
					boolean allOwned = true;
					boolean allOthersEditPermitted = true;
					for (int i = 0; i < rows.length; i++) {
						int modelRow = statementTable.convertRowIndexToModel(rows[i]);
						int coderId = statementTableModel.getRow(modelRow).getCoderId();
						if (coderId != Dna.sql.getActiveCoder().getId()) {
							allOwned = false;
						}
						if (Dna.sql.getActiveCoder().isPermissionEditOthersStatements(coderId) == false) {
							allOthersEditPermitted = false;
						}
					}
					if (Dna.sql.getActiveCoder().isPermissionDeleteStatements() == true &&
							(allOwned == true ||
							(Dna.sql.getActiveCoder().isPermissionEditOthersStatements() == true &&	allOthersEditPermitted == true))) {
						actionRemoveStatements.setEnabled(true);
					} else {
						actionRemoveStatements.setEnabled(false);
					}
					// no particular statement to select because multiple statements highlighted in table
				} else {
					actionRemoveStatements.setEnabled(false);
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
				changedDocumentTableSelection();
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
							refreshStatementTable();
						}
					});
					
					if (Dna.sql.getActiveCoder().isPermissionAddStatements() == false) {
						menuItem.setEnabled(false);
					}
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
					if (!(textWindow.getSelectedText() == null) && Dna.sql.getActiveCoder() != null && Dna.sql.getActiveCoder().isPermissionAddStatements() == true) {
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
				} else if (documentTable.getSelectedRowCount() > 0) {
					int pos = textWindow.getCaretPosition(); //click caret position
					Point p = me.getPoint();

					ArrayList<Statement> statements = Dna.sql.getStatements(documentTablePanel.getSelectedDocumentId());
					if (statements != null && statements.size() > 0) {
						Statement s;
						for (int i = 0; i < statements.size(); i++) {
							s = statements.get(i);
							if (s.getStart() < pos
									&& s.getStop() > pos
									&& Dna.sql.getActiveCoder() != null
									&& (s.getCoderId() == Dna.sql.getActiveCoder().getId() || Dna.sql.getActiveCoder().isPermissionViewOthersStatements())
									&& (s.getCoderId() == Dna.sql.getActiveCoder().getId() || Dna.sql.getActiveCoder().getCoderRelations().get(s.getCoderId()).isViewStatements())) {
								Point location = textWindow.getLocationOnScreen();
								textWindow.setSelectionStart(s.getStart());
								textWindow.setSelectionEnd(s.getStop());
								newPopup(p.getX(), p.getY(), statements.get(i), documentTablePanel.getSelectedDocumentId(), location);
								break;
							}
						}
					}
				}
			}
		});

		// toolbar document filter field listener
		JTextField documentFilterField = toolbar.getDocumentFilterField();
		documentFilterField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				processFilterDocumentChanges();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				processFilterDocumentChanges();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				processFilterDocumentChanges();
			}
			
			private void processFilterDocumentChanges() {
				documentTablePanel.setDocumentFilterPattern(documentFilterField.getText());
				documentTableModel.fireTableDataChanged();
			}
		});
		
		// listeners for menu items
		JSpinner fontSizeSpinner = menuBar.getFontSizeSpinner();
		fontSizeSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (Dna.sql.getConnectionProfile() != null) {
					Dna.sql.setCoderFontSize(Dna.sql.getConnectionProfile().getCoderId(), (int) fontSizeSpinner.getValue());
					textPanel.adjustToChangedCoder();
				}
			}
		});
		JSpinner popupWidthSpinner = menuBar.getPopupWidthSpinner();
		popupWidthSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (Dna.sql.getConnectionProfile() != null) {
					Dna.sql.setCoderPopupWidth(Dna.sql.getConnectionProfile().getCoderId(), (int) popupWidthSpinner.getValue());
				}
			}
		});
		JCheckBoxMenuItem popupAutoCompleteItem = menuBar.getPopupAutoCompleteItem();
		popupAutoCompleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Dna.sql.getConnectionProfile() != null) {
					Dna.sql.setCoderPopupAutoComplete(Dna.sql.getConnectionProfile().getCoderId(), popupAutoCompleteItem.isSelected());
				}
			}
		});
		JCheckBoxMenuItem popupDecorationItem = menuBar.getPopupDecorationItem();
		popupDecorationItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Dna.sql.getConnectionProfile() != null) {
					Dna.sql.setCoderPopupDecoration(Dna.sql.getConnectionProfile().getCoderId(), popupDecorationItem.isSelected());
				}
			}
		});
		JCheckBoxMenuItem colorByCoderItem = menuBar.getColorByCoderItem();
		colorByCoderItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Dna.sql.getConnectionProfile() != null) {
					Dna.sql.setColorByCoder(Dna.sql.getConnectionProfile().getCoderId(), colorByCoderItem.isSelected());
					textPanel.adjustToChangedCoder();
				}
			}
		});
		
		// change coder button listener
		JButton changeCoderButton = coderSelectionPanel.getChangeCoderButton();
		changeCoderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean authenticated = false;
				int coderIdToSelect = -1;
				if (Dna.sql.getActiveCoder() != null) {
					coderIdToSelect = Dna.sql.getActiveCoder().getId();
				}
				while (!authenticated) {
					CoderPasswordCheckDialog dialog = new CoderPasswordCheckDialog(Dna.sql, true, coderIdToSelect, 3);
					Coder coder = dialog.getCoder();
					String clearPassword = dialog.getPassword();
					if (coder != null && clearPassword != null) {
						coderIdToSelect = coder.getId();
						if (Dna.sql.authenticate(coder.getId(), clearPassword)) {
							authenticated = true;
							boolean reloadDocumentTable = coder.differentViewDocumentPermissions(Dna.sql.getActiveCoder());
							boolean reloadStatementTable = coder.differentViewStatementPermissions(Dna.sql.getActiveCoder());
							boolean repaintDocument = coder.differentPaintSettings(Dna.sql.getActiveCoder());
							Dna.sql.selectCoder(coder.getId());
							if (reloadDocumentTable) {
								refreshDocumentTable();
							}
							if (reloadStatementTable) {
								refreshStatementTable();
							}
							if (repaintDocument) {
								textPanel.adjustToChangedCoder();
							}
							coderSelectionPanel.changeCoderBadge();
							menuBar.adjustToChangedCoder();
							MainWindow.this.adjustToCoderSelection();
						} else {
							LogEvent l = new LogEvent(Logger.WARNING,
    								"Authentication failed. Check your password.",
    								"Tried to select coder, but a wrong password was entered for Coder " + coder.getId() + ".");
    						Dna.logger.log(l);
		    				JOptionPane.showMessageDialog(null,
		    						"Authentication failed. Check your password.",
		    					    "Check failed",
		    					    JOptionPane.ERROR_MESSAGE);
						}
					} else {
						authenticated = true; // user must have pressed cancel
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
	private TextPanel getTextPanel() {
		return textPanel;
	}

	/**
	 * Get the statement panel.
	 * 
	 * @return The statement panel.
	 */
	private StatementPanel getStatementPanel() {
		return statementPanel;
	}

	/**
	 * Retrieve the document table model.
	 * 
	 * @return Document table model.
	 */
	private DocumentTableModel getDocumentTableModel() {
		return documentTableModel;
	}

	/**
	 * Set text in the editor pane, select statement, and open popup window
	 * 
	 * @param s           The statement to be displayed in a popup dialog.
	 * @param documentId  The ID of the document.
	 * @param editable    Should the popup dialog be editable?
	 */
	private void selectStatement(Statement s, int documentId, boolean editable) {
		int documentCoderId = this.documentTableModel.getRow(this.documentTableModel.getModelRowById(documentId)).getCoder().getId();
		if (Dna.sql.getActiveCoder().getId() != documentCoderId &&
				(Dna.sql.getActiveCoder().isPermissionViewOthersDocuments() == false ||
				!Dna.sql.getActiveCoder().isPermissionViewOthersDocuments(documentCoderId))) {
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Statement " + s.getId() + ": Cannot open statement popup due to lack of permissions.",
					"Statement " + s.getId() + " cannot be opened in a popup window because the document in which it is contained is owned by a different coder and the current coder does not have permission to view this coder's documents.");
			Dna.logger.log(l);
		} else {
			JTextPane textWindow = getTextPanel().getTextWindow();
			JScrollPane textScrollPane = getTextPanel().getTextScrollPane();
			int start = s.getStart();
			int stop = s.getStop();
			textWindow.grabFocus();
			textWindow.select(start, stop);
			
			// the selection is too slow, so wait for it to finish...
			SwingUtilities.invokeLater(new Runnable() {
				@SuppressWarnings("deprecation") // modelToView becomes modelToView2D in Java 9, but we still want Java 8 compliance
				public void run() {
					Rectangle2D mtv = null;
					try {
						double y = textWindow.modelToView(start).getY();
						int l = textWindow.getText().length();
						double last = textWindow.modelToView(l).getY();
						double frac = y / last;
						double max = textScrollPane.getVerticalScrollBar().getMaximum();
						double h = textScrollPane.getHeight();
						int value = (int) Math.ceil(frac * max - (h / 2));
						textScrollPane.getVerticalScrollBar().setValue(value);
						mtv = textWindow.modelToView(start);
						Point loc = textWindow.getLocationOnScreen();
						newPopup(mtv.getX(), mtv.getY(), s, documentId, loc);
					} catch (BadLocationException e) {
						LogEvent l = new LogEvent(Logger.WARNING,
								"[GUI] Statement " + s.getId() + ": Popup window bad location exception.",
								"Statement " + s.getId() + ": Popup window cannot be opened because the location is outside the document text.");
						Dna.logger.log(l);
					}
				}
			});
		}
	}
	
	/**
	 * Show a statement popup window.
	 * 
	 * @param x           X location on the screen.
	 * @param y           Y location on the screen.
	 * @param s           The statement to show.
	 * @param documentId  The document ID for the statement.
	 * @param location    The location of the popup window.
	 * @param coder       The active coder.
	 */
	private void newPopup(double x, double y, Statement s, int documentId, Point location) {
		Popup popup = new Popup(x, y, s, documentId, location, Dna.sql.getActiveCoder());
		
		// duplicate button action listener
		JButton duplicate = popup.getDuplicateButton();
		duplicate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Dna.sql.getActiveCoder().isPermissionAddStatements() == true) {
					if (popup.isEditable() == true && popup.hasWindowDecoration() == true) {
						String message = "Save any changes in Statement " + s.getId() + " before creating copy?";
						int dialog = JOptionPane.showConfirmDialog(popup, message, "Confirmation", JOptionPane.YES_NO_OPTION);
						if (dialog == 0) {
							popup.saveContents(false);
						}
					} else if (popup.isEditable() && popup.hasWindowDecoration() == false) {
						popup.saveContents(false);
					}
					int newStatementId = Dna.sql.cloneStatement(s.getId(), Dna.sql.getActiveCoder().getId());
					if (statementTableWorker != null) {
						statementTableWorker.cancel(true);
						statusBar.statementRefreshEnd();
					}
			        statementTableWorker = new StatementTableRefreshWorker(LocalTime.now().toString(), newStatementId); // do not use refreshStatementTable method because we need to select the new statement ID
			        statementTableWorker.execute();
					popup.dispose();
				}
			}
		});
		
		// remove button action listener
		JButton remove = popup.getRemoveButton();
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(null, 
						"Are you sure you want to remove this statement?", 
						"Remove?", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					Dna.sql.deleteStatement(s.getId());
					refreshStatementTable();
					getTextPanel().paintStatements();
					getDocumentTableModel().decreaseFrequency(documentId);
					popup.dispose();
				}
			}
		});
		
		// save and close window or focus listener
		if (popup.hasWindowDecoration() == true) {
			popup.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					if (popup.isEditable() == true) {
						if (popup.saveContents(true) == true) { // check first if there are any changes; ask to save only if necessary
							String message = "Save changes in Statement " + s.getId() + "?";
							int dialog = JOptionPane.showConfirmDialog(popup, message, "Confirmation", JOptionPane.YES_NO_OPTION);
							if (dialog == 0) {
								popup.saveContents(false);
							}
						}
					}
					statementPanel.getStatementTable().clearSelection(); // clear statement table selection when popup window closed
					popup.dispose();
				}
			});
			popup.setModal(true); // set modal after adding controls because otherwise controls can't be added anymore while modal
		} else {
			popup.addWindowFocusListener(new WindowAdapter() {
				public void windowLostFocus(WindowEvent e) {
					popup.saveContents(false);
					statementPanel.getStatementTable().clearSelection(); // clear statement table selection when popup window closed
	                popup.dispose();
				}
			});
		}
		popup.setVisible(true); // needs to be called after setting modal; hence here instead of in the Popup class
	}

	/**
	 * Refresh the document table using a Swing worker in the background.
	 */
	private void refreshDocumentTable() {
		if (Dna.sql.getConnectionProfile() == null) {
			documentTableModel.clear();
		} else {
			if (documentTableWorker != null) {
				documentTableWorker.cancel(true);
				statusBar.documentRefreshEnd();
			}
	        documentTableWorker = new DocumentTableRefreshWorker(LocalTime.now().toString());
	        documentTableWorker.execute();
		}
	}

	/**
	 * Refresh the statement table using a Swing worker in the background and
	 * select the previously selected statement again (if applicable and
	 * available).
	 */
	private void refreshStatementTable() {
		if (Dna.sql.getConnectionProfile() == null) {
			statementTableModel.clear();
		} else {
			if (statementTableWorker != null) {
				statementTableWorker.cancel(true);
				statusBar.statementRefreshEnd();
			}
	        statementTableWorker = new StatementTableRefreshWorker(LocalTime.now().toString());
	        statementTableWorker.execute();
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
		 * A name for the swing worker thread to identify it in the message log.
		 */
		private String name;

		/**
		 * Create a new document table swing worker.
		 * 
		 * @param name  The name of the thread.
		 */
		private DocumentTableRefreshWorker(String name) {
			this.name = name;
			actionAddDocument.setEnabled(false);
			actionRemoveDocuments.setEnabled(false);
			actionEditDocuments.setEnabled(false);
			actionBatchImportDocuments.setEnabled(false);
			actionRefresh.setEnabled(false);
			statusBar.documentRefreshStart();
			time = System.nanoTime(); // take the time to compute later how long the updating took
			verticalScrollLocation = textPanel.getVerticalScrollLocation();
			selectedId = documentTablePanel.getSelectedDocumentId(); // remember the document ID to select the same document when done
			documentTableModel.clear(); // remove all documents from the table model before re-populating the table
			LogEvent le = new LogEvent(Logger.MESSAGE,
					"[GUI] Initializing thread to populate document table: " + this.getName() + ".",
					"A new swing worker thread has been started to populate the document table with documents from the database in the background: " + this.getName() + ".");
			Dna.logger.log(le);
		}
		
		@Override
		protected List<TableDocument> doInBackground() {
			try (Connection conn = Dna.sql.getDataSource().getConnection();
					PreparedStatement s = conn.prepareStatement("SELECT D.ID, Title, (SELECT COUNT(ID) FROM STATEMENTS WHERE DocumentId = D.ID) AS Frequency, C.ID AS CoderId, Name AS CoderName, Red, Green, Blue, Date, Author, Source, Section, Type, Notes FROM CODERS C INNER JOIN DOCUMENTS D ON D.Coder = C.ID;");
					ResultSet rs = s.executeQuery();) {
				while (!isCancelled() && rs.next()) {
					if (isCancelled()) {
						return null;
					}
					TableDocument r = new TableDocument(
							rs.getInt("ID"),
							rs.getString("Title"),
							rs.getInt("Frequency"),
							new Coder(rs.getInt("CoderId"),
									rs.getString("CoderName"),
									new Color(rs.getInt("Red"), rs.getInt("Green"), rs.getInt("Blue"))),
							rs.getString("Author"),
							rs.getString("Source"),
							rs.getString("Section"),
							rs.getString("Type"),
							rs.getString("Notes"),
							LocalDateTime.ofEpochSecond(rs.getLong("Date"), 0, ZoneOffset.UTC));
					publish(r); // send the new document row out of the background thread
				}
			} catch (SQLException e) {
				if (e.getMessage().matches(".*Interrupted during connection acquisition.*")) {
					LogEvent l = new LogEvent(Logger.MESSAGE,
							"[GUI]  ├─ Document retrieval canceled in Thread " + this.getName() + ".",
							"Refreshing the document table by reloading all documents from the database and populating the document table with them was canceled, presumably because a new swing worker to retrieve documents was initiated, which then superseded the existing thread.",
							e);
					Dna.logger.log(l);
				} else {
					LogEvent le = new LogEvent(Logger.WARNING,
							"[SQL]  ├─ Could not retrieve documents from database.",
							"The document table model swing worker tried to retrieve all documents from the database to display them in the document table, but some or all documents could not be retrieved because there was a problem while processing the result set. The document table may be incomplete.",
							e);
					Dna.logger.log(le);
				}
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
        	documentTableModel.sort();
			long elapsed = System.nanoTime(); // measure time again for calculating difference
			LogEvent le = new LogEvent(Logger.MESSAGE,
					"[GUI]  ├─ (Re)loaded all " + documentTableModel.getRowCount() + " documents in " + (elapsed - time) / 1000000 + " milliseconds.",
					"The document table swing worker loaded the " + documentTableModel.getRowCount() + " documents from the DNA database in the "
					+ "background and stored them in the document table. This took " + (elapsed - time) / 1000000 + " milliseconds.");
			Dna.logger.log(le);
			le = new LogEvent(Logger.MESSAGE,
					"[GUI]  └─ Closing thread to populate document table: " + this.getName() + ".",
					"The document table has been populated with documents from the database. Closing thread: " + this.getName() + ".");
			Dna.logger.log(le);
			statusBar.documentRefreshEnd();
			if (!statusBar.isRefreshInProgress()) {
				if (Dna.sql.getConnectionProfile() != null) {
					actionRefresh.setEnabled(true);
				} else {
					actionRefresh.setEnabled(false);
				}
			}
			changedDocumentTableSelection();
	    }
	    
	    /**
	     * Get the name of the thread.
	     * 
	     * @return  Thread name.
	     */
	    String getName() {
	    	return this.name;
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
		 * A name for the swing worker thread to identify it in the message log.
		 */
		private String name;

		/**
		 * ID of the selected statement in the statement table, to restore it
		 * later and scroll back to the same position in the table after update.
		 */
		private int selectedId;

		/**
		 * A Swing worker that reloads all statements from the database and
		 * stores them in the table model for displaying them in the statement
		 * table. Selects the previously selected statement when done.
		 */
		private StatementTableRefreshWorker(String name) {
			this.name = name;
			selectedId = getStatementPanel().getSelectedStatementId();
			initialiseRefreshWorker();
		}
		
		/**
		 * A Swing worker that reloads all statements from the database and
		 * stores them in the table model for displaying them in the statement
		 * table.
		 * 
		 * @param statementIdToSelect The ID of the statement that should be
		 *   selected when done refreshing.
		 */
		private StatementTableRefreshWorker(String name, int statementIdToSelect) {
			this.name = name;
			selectedId = statementIdToSelect;
			initialiseRefreshWorker();
		}
		
		/**
		 * Initialise the statement table refresh worker. This common code is
		 * executed by both constructors.
		 */
		private void initialiseRefreshWorker() {
			actionRefresh.setEnabled(false);
    		time = System.nanoTime(); // take the time to compute later how long the updating took
    		statusBar.statementRefreshStart();
    		statementTableModel.clear(); // remove all documents from the table model before re-populating the table
			LogEvent le = new LogEvent(Logger.MESSAGE,
					"[GUI] Initializing thread to populate statement table: " + this.getName() + ".",
					"A new swing worker thread has been started to populate the statement table with statements from the database in the background: " + this.getName() + ".");
			Dna.logger.log(le);
		}
		
		@Override
		protected List<Statement> doInBackground() {
			String subString = "SUBSTRING(DOCUMENTS.Text, Start + 1, Stop - Start) AS Text ";
			if (Dna.sql.getConnectionProfile().getType().equals("postgresql")) {
				subString = "SUBSTRING(DOCUMENTS.Text, CAST(Start + 1 AS INT4), CAST(Stop - Start AS INT4)) AS Text ";
			}
			String q1 = "SELECT STATEMENTS.ID AS StatementId, "
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
					+ subString
					+ "FROM STATEMENTS "
					+ "INNER JOIN CODERS ON STATEMENTS.Coder = CODERS.ID "
					+ "INNER JOIN STATEMENTTYPES ON STATEMENTS.StatementTypeId = STATEMENTTYPES.ID "
					+ "INNER JOIN DOCUMENTS ON DOCUMENTS.ID = STATEMENTS.DocumentId ORDER BY DOCUMENTS.DATE ASC;";
			
			String q2 = "SELECT ID FROM STATEMENTTYPES;";
			
			String q3 = "SELECT ID, Variable, DataType FROM VARIABLES;";
			
			String q4 = "SELECT DATASHORTTEXT.StatementId, VARIABLES.ID AS VariableId, ENTITIES.Value AS Value FROM DATASHORTTEXT "
					+ "INNER JOIN VARIABLES ON VARIABLES.ID = DATASHORTTEXT.VariableId "
					+ "INNER JOIN ENTITIES ON ENTITIES.VariableId = VARIABLES.ID AND ENTITIES.ID = DATASHORTTEXT.Entity WHERE VARIABLES.StatementTypeId = ? "
					+ "UNION "
					+ "SELECT DATALONGTEXT.StatementId, VARIABLES.ID AS VariableId, DATALONGTEXT.Value FROM DATALONGTEXT "
					+ "INNER JOIN VARIABLES ON VARIABLES.ID = DATALONGTEXT.VariableId WHERE VARIABLES.StatementTypeId = ? "
					+ "UNION "
					+ "SELECT DATABOOLEAN.StatementId, VARIABLES.ID AS VariableId, DATABOOLEAN.Value FROM DATABOOLEAN "
					+ "INNER JOIN VARIABLES ON VARIABLES.ID = DATABOOLEAN.VariableId WHERE VARIABLES.StatementTypeId = ? "
					+ "UNION "
					+ "SELECT DATAINTEGER.StatementId, VARIABLES.ID AS VariableId, DATAINTEGER.Value FROM DATAINTEGER "
					+ "INNER JOIN VARIABLES ON VARIABLES.ID = DATAINTEGER.VariableId WHERE VARIABLES.StatementTypeId = ? "
					+ "ORDER BY 1, 2 ASC;";
			
			int statementTypeId, statementId, variableId;
			Color sColor, cColor;
			HashMap<Integer, String> variableNameMap = new HashMap<Integer, String>(); // variable ID to variable name
			HashMap<Integer, String> variableDataTypeMap = new HashMap<Integer, String>(); // variable ID to data type
			HashMap<Integer, Statement> statementMap = new HashMap<Integer, Statement>(); // statement ID to Statement
			ResultSet r3, r4;
			try (Connection conn = Dna.sql.getDataSource().getConnection();
					PreparedStatement s1 = conn.prepareStatement(q1);
					PreparedStatement s2 = conn.prepareStatement(q2);
					PreparedStatement s3 = conn.prepareStatement(q3);
					PreparedStatement s4 = conn.prepareStatement(q4);) {
				
				// assemble statements without values for now and save them in a hash map
				ResultSet r1 = s1.executeQuery();
				while (r1.next()) {
					statementId = r1.getInt("StatementId");
				    statementTypeId = r1.getInt("StatementTypeId");
				    sColor = new Color(r1.getInt("StatementTypeRed"), r1.getInt("StatementTypeGreen"), r1.getInt("StatementTypeBlue"));
				    cColor = new Color(r1.getInt("CoderRed"), r1.getInt("CoderGreen"), r1.getInt("CoderBlue"));
				    Statement statement = new Statement(statementId,
				    		r1.getInt("Start"),
				    		r1.getInt("Stop"),
				    		statementTypeId,
				    		r1.getString("StatementTypeLabel"),
				    		sColor,
				    		r1.getInt("CoderId"),
				    		r1.getString("CoderName"),
				    		cColor,
				    		new ArrayList<Value>(),
				    		r1.getInt("DocumentId"),
				    		r1.getString("Text"),
				    		LocalDateTime.ofEpochSecond(r1.getLong("Date"), 0, ZoneOffset.UTC));
				    statementMap.put(statementId, statement);
				}
				
				// get variables
				r3 = s3.executeQuery();
				while (r3.next()) {
					variableNameMap.put(r3.getInt("ID"), r3.getString("Variable"));
					variableDataTypeMap.put(r3.getInt("ID"), r3.getString("DataType"));
				}
				
				// get statement types
				ResultSet r2 = s2.executeQuery();
				while (r2.next()) {
					statementTypeId = r2.getInt("ID");
					
					// get values and put them into the statements
					s4.setInt(1, statementTypeId);
					s4.setInt(2, statementTypeId);
					s4.setInt(3, statementTypeId);
					s4.setInt(4, statementTypeId);
					r4 = s4.executeQuery();
					while (r4.next()) {
						variableId = r4.getInt("VariableId");
						statementMap.get(r4.getInt("StatementId")).getValues().add(new Value(variableId, variableNameMap.get(variableId), variableDataTypeMap.get(variableId), r4.getString("Value")));
					}
				}
				
				// publish all statements
				Collection<Statement> s = statementMap.values();
		        ArrayList<Statement> listOfStatements = new ArrayList<Statement>(s);
				Collections.sort(listOfStatements);
		        for (int i = 0; i < listOfStatements.size(); i++) {
		        	publish(listOfStatements.get(i));
		        }
			} catch (SQLException e) {
				if (e.getMessage().matches(".*Interrupted during connection acquisition.*")) {
					LogEvent l = new LogEvent(Logger.MESSAGE,
							"[GUI]  ├─ Statement retrieval canceled in Thread " + this.getName() + ".",
							"Refreshing the statement table by reloading all statements from the database and populating the statement table with them was canceled, presumably because a new swing worker to retrieve statements was initiated, which then superseded the existing thread.",
							e);
					Dna.logger.log(l);
				} else {
					LogEvent l = new LogEvent(Logger.WARNING,
							"[SQL] Failed to retrieve statements.",
							"Attempted to retrieve all statements from the database, but something went wrong. You should double-check if the statements are all shown!",
							e);
					Dna.logger.log(l);
				}
			}
			return null;
		}
        
        @Override
        protected void process(List<Statement> chunks) {
        	statementTableModel.addRows(chunks); // transfer a batch of rows to the statement table model
			getStatementPanel().setSelectedStatementId(selectedId); // select the statement from before; skipped if the statement not found in this batch
        }

        @Override
        protected void done() {
        	statementTableModel.sort();
        	statusBar.statementRefreshEnd(); // stop displaying the update message in the status bar
			statementTableModel.fireTableDataChanged(); // update the statement filter
			getStatementPanel().setSelectedStatementId(selectedId);
    		long elapsed = System.nanoTime(); // measure time again for calculating difference
    		LogEvent le = new LogEvent(Logger.MESSAGE,
    				"[GUI]  ├─ (Re)loaded all " + statementTableModel.getRowCount() + " statements in " + (elapsed - time) / 1000000 + " milliseconds.",
    				"The statement table swing worker loaded the " + statementTableModel.getRowCount() + " statements from the DNA database in the "
    				+ "background and stored them in the statement table. This took " + (elapsed - time) / 1000000 + " milliseconds.");
    		Dna.logger.log(le);
			le = new LogEvent(Logger.MESSAGE,
					"[GUI]  └─ Closing thread to populate statement table: " + this.getName() + ".",
					"The statement table has been populated with statements from the database. Closing thread: " + this.getName() + ".");
			Dna.logger.log(le);
			if (!statusBar.isRefreshInProgress()) {
				if (Dna.sql.getConnectionProfile() != null) {
					actionRefresh.setEnabled(true);
				} else {
					actionRefresh.setEnabled(false);
				}
			}
        }
	    
	    /**
	     * Get the name of the thread.
	     * 
	     * @return  Thread name.
	     */
	    String getName() {
	    	return this.name;
	    }
    }

	/**
	 * Adjust controls to document table selection.
	 */
	private void changedDocumentTableSelection() {
		JTable documentTable = documentTablePanel.getDocumentTable();
		int rowCount = documentTable.getSelectedRowCount();
		boolean allOwned = true;
		boolean allOthersEditPermitted = true;
		int[] rows = documentTable.getSelectedRows();
		if (rowCount > 0) {
			for (int i = 0; i < rows.length; i++) {
				int modelRow = documentTable.convertRowIndexToModel(rows[i]);
				int coderId = documentTableModel.getRow(modelRow).getCoder().getId();
				if (coderId != Dna.sql.getActiveCoder().getId()) {
					allOwned = false; // does the active coder own all selected documents?
					if (Dna.sql.getActiveCoder().isPermissionEditOthersDocuments(coderId) == false) {
						allOthersEditPermitted = false;
					}
				}
			}
		}
		// enable or disable action for deleting documents depending on selection and user rights
		if (rowCount > 0 && Dna.sql.getActiveCoder().isPermissionDeleteDocuments() == true &&
				(Dna.sql.getActiveCoder().isPermissionEditOthersDocuments() == true || allOwned == true) &&
				allOthersEditPermitted == true) {
			actionRemoveDocuments.setEnabled(true);
		} else {
			actionRemoveDocuments.setEnabled(false);
		}
		// enable or disable action for editing documents depending on selection and user rights
		if (rowCount > 0 && Dna.sql.getActiveCoder().isPermissionEditDocuments() == true &&
				(Dna.sql.getActiveCoder().isPermissionEditOthersDocuments() == true || allOwned == true) &&
				allOthersEditPermitted == true) {
			actionEditDocuments.setEnabled(true);
		} else {
			actionEditDocuments.setEnabled(false);
		}
		if (rowCount == 0 || rowCount > 1) {
			textPanel.setContents(-1, "");
			getStatementPanel().setDocumentId(-1);
			statementTableModel.fireTableDataChanged();
		} else if (rowCount == 1) {
			int selectedRow = documentTable.getSelectedRow();
			int selectedModelIndex = documentTable.convertRowIndexToModel(selectedRow);
			int id = documentTableModel.getIdByModelRow(selectedModelIndex);
			textPanel.setContents(id, documentTableModel.getDocumentText(id));
			getStatementPanel().setDocumentId(id);
			statementTableModel.fireTableDataChanged();
		} else {
			LogEvent l = new LogEvent(Logger.WARNING,
					"[GUI] Negative number of rows in the document table!",
					"When a document is selected in the document table in the DNA coding window, the text of the document is displayed in the text panel. When checking which row in the table was selected, it was found that the table contained negative numbers of documents. This is obviously an error. Please report it by submitting a bug report along with the saved log.");
			Dna.logger.log(l);
		}
		
		// enable or disable actions for adding and importing documents
		if (Dna.sql.getActiveCoder() == null || Dna.sql.getConnectionProfile() == null) {
			actionAddDocument.setEnabled(false);
			actionBatchImportDocuments.setEnabled(false);
		} else {
			if (Dna.sql.getActiveCoder().isPermissionAddDocuments() == true) {
				actionAddDocument.setEnabled(true);
				actionBatchImportDocuments.setEnabled(true);
			} else {
				actionAddDocument.setEnabled(false);
				actionBatchImportDocuments.setEnabled(false);
			}
			if (Dna.sql.getActiveCoder().isPermissionImportDocuments() == true) {
				actionImporter.setEnabled(true);
			} else {
				actionImporter.setEnabled(false);
			}
		}
	}
	
	/**
	 * React to changes in the active coder in the {@link dna.Sql Sql} class.
	 */
	public void adjustToCoderSelection() {
		actionCloseDatabase.setEnabled(true);
		actionCreateDatabase.setEnabled(true);
		actionOpenProfile.setEnabled(true);
		actionSaveProfile.setEnabled(true);
		if (Dna.sql.getActiveCoder() != null && Dna.sql.getActiveCoder().isPermissionEditCoders() == true) {
			actionCoderManager.setEnabled(true);
		} else {
			actionCoderManager.setEnabled(false);
		}
		changedDocumentTableSelection();
		actionRefresh.setEnabled(true);
		if (Dna.sql.getActiveCoder() != null && Dna.sql.getActiveCoder().isPermissionDeleteStatements() == true) {
			actionRemoveStatements.setEnabled(true);
		} else {
			actionRemoveStatements.setEnabled(false);
		}
		if (Dna.sql.getActiveCoder() != null && Dna.sql.getActiveCoder().isPermissionEditStatementTypes() == true) {
			actionStatementTypeEditor.setEnabled(true);
		} else {
			actionStatementTypeEditor.setEnabled(false);
		}
		if (Dna.sql.getActiveCoder() != null && Dna.sql.getActiveCoder().isPermissionEditAttributes() == true) {
			actionAttributeManager.setEnabled(true);
		} else {
			actionAttributeManager.setEnabled(false);
		}
		if (Dna.sql.getActiveCoder() != null && Dna.sql.getActiveCoder().isPermissionEditCoderRelations() == true && Dna.sql.getActiveCoder().getId() != 1) {
			actionCoderRelationsEditor.setEnabled(true);
		} else {
			actionCoderRelationsEditor.setEnabled(false);
		}
		refreshDocumentTable();
		refreshStatementTable();
	}
	
	/**
	 * An action to display a dialog to open a database.
	 */
	class ActionOpenDatabase extends AbstractAction {
		private static final long serialVersionUID = 5076889002458881750L;
		
		public ActionOpenDatabase(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			NewDatabaseDialog n = new NewDatabaseDialog(true);
			ConnectionProfile cp = n.getConnectionProfile();
			if (cp != null) {
				Dna.sql.setConnectionProfile(cp, false);
				adjustToCoderSelection();
				
				// changes in other classes
				statusBar.updateUrl();
				toolbar.adjustToChangedConnection();
				textPanel.setContents(-1, "");
				statementPanel.adjustToChangedConnection();
				menuBar.adjustToChangedCoder();
				coderSelectionPanel.changeCoderBadge();
			}
			
			if (cp == null) {
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"[GUI] Action executed: could not open database.",
						"Started opening a database connection from the GUI, but the connection was not established.");
				Dna.logger.log(l);
			} else {
				Dna.sql.setConnectionProfile(cp, false); // not a connection test, so false
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"[GUI] Action executed: opened database.",
						"Opened a database connection from the GUI.");
				Dna.logger.log(l);
			}
		}
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
			Dna.sql.selectCoder(-1);
			
			// subsequent changes in main window class
			actionCloseDatabase.setEnabled(false);
			actionCreateDatabase.setEnabled(true);
			actionOpenProfile.setEnabled(true);
			actionSaveProfile.setEnabled(false);
			actionCoderManager.setEnabled(false);
			actionAddDocument.setEnabled(false);
			actionRemoveDocuments.setEnabled(false);
			actionEditDocuments.setEnabled(false);
			actionBatchImportDocuments.setEnabled(false);
			actionImporter.setEnabled(false);
			actionRefresh.setEnabled(false);
			actionRemoveStatements.setEnabled(false);
			actionStatementTypeEditor.setEnabled(false);
			actionAttributeManager.setEnabled(false);
			actionCoderRelationsEditor.setEnabled(false);
			documentTableModel.clear();
			statementTableModel.clear();
			
			// changes in other classes
			statusBar.updateUrl();
			toolbar.adjustToChangedConnection();
			textPanel.setContents(-1, "");
			statementPanel.adjustToChangedConnection();
			menuBar.adjustToChangedCoder();
			coderSelectionPanel.changeCoderBadge();
			
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
			Dna.sql.setConnectionProfile(cp, false); // this is after creating data structures, so no test (= false)
			adjustToCoderSelection();
			
			// changes in other classes
			statusBar.updateUrl();
			toolbar.adjustToChangedConnection();
			textPanel.setContents(-1, "");
			statementPanel.adjustToChangedConnection();
			menuBar.adjustToChangedCoder();
			coderSelectionPanel.changeCoderBadge();
			
			if (cp == null) {
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"[GUI] Action executed: new database was not created.",
						"Started creating a new database from the GUI, but a connection was not established.");
				Dna.logger.log(l);
			} else {
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
								Sql sqlTemp = new Sql(cp, true); // just for authentication purposes, so a test
								boolean authenticated = sqlTemp.authenticate(-1, key);
								if (authenticated == true) {
									validPasswordInput = true; // authenticated; quit the while-loop
									Dna.sql.setConnectionProfile(cp, false); // use the connection profile, so no test
									adjustToCoderSelection();
									
									// changes in other classes
									statusBar.updateUrl();
									toolbar.adjustToChangedConnection();
									textPanel.setContents(-1, "");
									statementPanel.adjustToChangedConnection();
									menuBar.adjustToChangedCoder();
									coderSelectionPanel.changeCoderBadge();
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
					CoderPasswordCheckDialog d = new CoderPasswordCheckDialog(Dna.sql, false, -1, 3);
					String key = d.getPassword();
					if (key == null) { // user must have pressed cancel
						validPasswordInput = true;
					} else {
						boolean authenticated = Dna.sql.authenticate(-1, key);
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
				Gson prettyGson = new GsonBuilder()
			            .setPrettyPrinting()
			            .serializeNulls()
			            .disableHtmlEscaping()
			            .create();
				String g = prettyGson.toJson(cp);
				writer.write(g);
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
	 * An action to display a coder manager dialog window.
	 */
	class ActionCoderManager extends AbstractAction {
		private static final long serialVersionUID = -771898426024889217L;

		public ActionCoderManager(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			if (Dna.sql.getActiveCoder().isPermissionEditCoders() == true) {
				new CoderManager();
				if (Dna.sql.getActiveCoder().getId() != 1) {
					Coder coderCopy = Dna.sql.getCoder(Dna.sql.getConnectionProfile().getCoderId());
					
					// refresh document and statement table and repaint text if necessary; reselect the coder
					boolean updateViewDocuments = false;
					if (coderCopy.differentViewDocumentPermissions(Dna.sql.getActiveCoder())) {
						updateViewDocuments = true;
					}
					boolean updateViewStatements = false;
					if (coderCopy.differentViewStatementPermissions(Dna.sql.getActiveCoder())) {
						updateViewStatements = true;
					}
					boolean updatePaintSettings = false;
					if (coderCopy.differentPaintSettings(Dna.sql.getActiveCoder())) {
						updatePaintSettings = true;
					}
					Dna.sql.selectCoder(coderCopy);
					if (updateViewDocuments) {
						refreshDocumentTable();
					}
					if (updateViewStatements) {
						refreshStatementTable();
					}
					if (updatePaintSettings) {
						textPanel.adjustToChangedCoder();
					}
					
					// enable or disable actions as necessary after update
					if (Dna.sql.getActiveCoder().isPermissionEditCoders() == true) {
						actionCoderManager.setEnabled(true);
					} else {
						actionCoderManager.setEnabled(false);
					}
					changedDocumentTableSelection();
					if (Dna.sql.getActiveCoder().isPermissionDeleteStatements() == true) {
						actionRemoveStatements.setEnabled(true);
					} else {
						actionRemoveStatements.setEnabled(false);
					}
					if (Dna.sql.getActiveCoder().isPermissionEditStatementTypes() == true) {
						actionStatementTypeEditor.setEnabled(true);
					} else {
						actionStatementTypeEditor.setEnabled(false);
					}
					if (Dna.sql.getActiveCoder().isPermissionEditAttributes() == true) {
						actionAttributeManager.setEnabled(true);
					} else {
						actionAttributeManager.setEnabled(false);
					}
					if (Dna.sql.getActiveCoder().isPermissionEditCoderRelations() == true) {
						actionCoderRelationsEditor.setEnabled(true);
					} else {
						actionCoderRelationsEditor.setEnabled(false);
					}
				}
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"[GUI] Action executed: opened coder manager.",
						"Opened a coder manager window from the GUI.");
				Dna.logger.log(l);
			} else {
				LogEvent l = new LogEvent(Logger.WARNING,
						"[GUI] Action could not be executed: insufficient permissions to open coder manager.",
						"Attempted to open a coder manager from the GUI, but the coder did not have sufficient permissions for editing coders. This message should never appear because the menu item for opening a coder manager should be grayed out when the active coder has insufficient permissions. Please report the full error log to the developers through the issue tracker on GitHub.");
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
			refreshStatementTable();
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
			refreshStatementTable();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: used document batch importer.",
					"Batch-imported documents to the database.");
			Dna.logger.log(l);
		}
	}

	/**
	 * An action to start a dialog for importing from another DNA database.
	 */
	class ActionImporter extends AbstractAction {
		private static final long serialVersionUID = 4613926523251135254L;

		public ActionImporter(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			new Importer();
	    	refreshDocumentTable();
			refreshStatementTable();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: used DNA database import dialog.",
					"Imported from another database into the current DNA database.");
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
			int[] selectedRows = getStatementPanel().getStatementTable().getSelectedRows();
			String message = "Are you sure you want to delete " + selectedRows.length + " statements?";
			int dialog = JOptionPane.showConfirmDialog(null, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
			if (dialog == 0) {
				for (int i = 0; i < selectedRows.length; i++) {
					selectedRows[i] = getStatementPanel().getStatementTable().convertRowIndexToModel(selectedRows[i]);
				}
				statementTableModel.removeStatements(selectedRows);
			}
			refreshStatementTable();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: removed statement(s).",
					"Deleted multiple statements in the database from the GUI.");
			Dna.logger.log(l);
		}
	}

	/**
	 * An action to display a statement type editor dialog window.
	 */
	class ActionStatementTypeEditor extends AbstractAction {
		private static final long serialVersionUID = -9078666078201409563L;
		
		public ActionStatementTypeEditor(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			if (Dna.sql.getActiveCoder().isPermissionEditStatementTypes()) {
				new StatementTypeEditor();
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"[GUI] Action executed: opened statement type editor.",
						"Opened a statement type editor window from the GUI.");
				Dna.logger.log(l);
			} else {
				LogEvent l = new LogEvent(Logger.WARNING,
						"[GUI] Action could not be executed: insufficient permissions to open statement type editor.",
						"Attempted to open a statement type editor from the GUI, but the coder did not have sufficient permissions for editing statement types. This message should never appear because the menu item for opening a statement type editor dialog window should be grayed out when the active coder has insufficient permissions. Please report the full error log to the developers through the issue tracker on GitHub.");
				Dna.logger.log(l);
			}
		}
	}

	/**
	 * An action to display an attribute manager dialog window.
	 */
	class ActionAttributeManager extends AbstractAction {
		private static final long serialVersionUID = -9078666078201409563L;
		
		public ActionAttributeManager(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			if (Dna.sql.getActiveCoder().isPermissionEditAttributes() == true) {
				new AttributeManager();
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"[GUI] Action executed: opened attribute manager.",
						"Opened an attribute manager window from the GUI.");
				Dna.logger.log(l);
			} else {
				LogEvent l = new LogEvent(Logger.WARNING,
						"[GUI] Action could not be executed: insufficient permissions to open attribute manager.",
						"Attempted to open an attribute manager from the GUI, but the coder did not have sufficient permissions for editing attributes. This message should never appear because the menu item for opening an attribute manager should be grayed out when the active coder has insufficient permissions. Please report the full error log to the developers through the issue tracker on GitHub.");
				Dna.logger.log(l);
			}
		}
	}

	/**
	 * An action to display a coder manager dialog window.
	 */
	class ActionCoderRelationsEditor extends AbstractAction {
		private static final long serialVersionUID = -6341308994517637774L;

		public ActionCoderRelationsEditor(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			if (Dna.sql.getActiveCoder().isPermissionEditCoderRelations() && Dna.sql.getActiveCoder().getId() != 1) {
				CoderRelationsEditor cre = new CoderRelationsEditor();
				if (cre.isUpdateViewDocuments()) {
					refreshDocumentTable();
				}
				if (cre.isUpdateViewStatements()) {
					refreshStatementTable();
				}
				LogEvent l = new LogEvent(Logger.MESSAGE,
						"[GUI] Action executed: opened coder relations editor.",
						"Opened a coder relations editor window from the GUI.");
				Dna.logger.log(l);
			} else {
				LogEvent l = new LogEvent(Logger.WARNING,
						"[GUI] Action could not be executed: insufficient permissions to open coder relations editor.",
						"Attempted to open a coder relations editor from the GUI, but the coder did not have sufficient permissions for editing coder relations. This message should never appear because the menu item for opening a coder relations editor should be grayed out when the active coder has insufficient permissions. Please report the full error log to the developers through the issue tracker on GitHub.");
				Dna.logger.log(l);
			}
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