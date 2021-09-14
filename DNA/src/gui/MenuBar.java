package gui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import gui.MainWindow.ActionAboutWindow;
import gui.MainWindow.ActionAddDocument;
import gui.MainWindow.ActionBatchImportDocuments;
import gui.MainWindow.ActionCloseDatabase;
import gui.MainWindow.ActionCreateDatabase;
import gui.MainWindow.ActionEditDocuments;
import gui.MainWindow.ActionLoggerDialog;
import gui.MainWindow.ActionOpenProfile;
import gui.MainWindow.ActionQuit;
import gui.MainWindow.ActionRefresh;
import gui.MainWindow.ActionRemoveDocuments;
import gui.MainWindow.ActionRemoveStatements;
import gui.MainWindow.ActionSaveProfile;

/**
 * Menu bar with actions as menu items.
 */
public class MenuBar extends JMenuBar {
	private static final long serialVersionUID = 6631392079690346097L;

	/**
	 * Create a new menu bar.
	 * 
	 * @param actionOpenDatabase          Action for opening a new database.
	 * @param actionCloseDatabase         Action for closing the database.
	 * @param actionCreateDatabase        Action for creating a new database.
	 * @param actionOpenProfile           Action for opening connection profile.
	 * @param actionSaveProfile           Action for saving connection profile.
	 * @param actionQuit                  Action for quitting DNA.
	 * @param actionAddDocument           Action for adding a document.
	 * @param actionRemoveDocuments       Action for removing documents.
	 * @param actionEditDocuments         Action for editing documents.
	 * @param actionBatchImportDocuments  Action for batch-importing documents.
	 * @param actionRefresh               Action for reloading data.
	 * @param actionRemoveStatements      Action for removing statements.
	 * @param actionLoggerDialog          Action for opening logger dialog.
	 * @param actionAboutWindow           Action for opening DNA About window.
	 */
	public MenuBar(ActionOpenDatabase actionOpenDatabase,
			ActionCloseDatabase actionCloseDatabase,
			ActionCreateDatabase actionCreateDatabase,
			ActionOpenProfile actionOpenProfile,
			ActionSaveProfile actionSaveProfile,
			ActionQuit actionQuit,
			ActionAddDocument actionAddDocument,
			ActionRemoveDocuments actionRemoveDocuments,
			ActionEditDocuments actionEditDocuments,
			ActionBatchImportDocuments actionBatchImportDocuments,
			ActionRefresh actionRefresh,
			ActionRemoveStatements actionRemoveStatements,
			ActionLoggerDialog actionLoggerDialog,
			ActionAboutWindow actionAboutWindow) {

		// database menu
		JMenu databaseMenu = new JMenu("Database");
		this.add(databaseMenu);
		
		// database menu: open a database
		JMenuItem openDatabaseItem = new JMenuItem(actionOpenDatabase);
		databaseMenu.add(openDatabaseItem);

		// database menu: close database
		JMenuItem closeDatabaseItem = new JMenuItem(actionCloseDatabase);
		databaseMenu.add(closeDatabaseItem);

		// database menu: create a new database
		JMenuItem createDatabaseItem = new JMenuItem(actionCreateDatabase);
		databaseMenu.add(createDatabaseItem);

		// database menu: open a connection profile
		JMenuItem openProfileItem = new JMenuItem(actionOpenProfile);
		databaseMenu.add(openProfileItem);

		// database menu: save a connection profile
		JMenuItem saveProfileItem = new JMenuItem(actionSaveProfile);
		databaseMenu.add(saveProfileItem);

		// database menu: quit DNA
		JMenuItem quitItem = new JMenuItem(actionQuit);
		databaseMenu.add(quitItem);

		// document menu
		JMenu documentMenu = new JMenu("Document");
		this.add(documentMenu);
		
		// document menu: add new document
		JMenuItem addDocumentItem = new JMenuItem(actionAddDocument);
		documentMenu.add(addDocumentItem);
		
		// document menu: remove documents
		JMenuItem removeDocumentsItem = new JMenuItem(actionRemoveDocuments);
		documentMenu.add(removeDocumentsItem);
		
		// document menu: edit documents
		JMenuItem editDocumentsItem = new JMenuItem(actionEditDocuments);
		documentMenu.add(editDocumentsItem);

		// document menu: batch import documents
		JMenuItem batchImportDocumentsItem = new JMenuItem(actionBatchImportDocuments);
		documentMenu.add(batchImportDocumentsItem);
		
		// document menu: refresh document table
		JMenuItem refreshItem = new JMenuItem(actionRefresh);
		documentMenu.add(refreshItem);

		// statements menu
		JMenu statementsMenu = new JMenu("Statements");
		this.add(statementsMenu);
		
		// statements menu: remove statements
		JMenuItem removeStatementsItem = new JMenuItem(actionRemoveStatements);
		statementsMenu.add(removeStatementsItem);

		// export menu
		JMenu exportMenu = new JMenu("Export");
		this.add(exportMenu);
		
		// settings menu
		JMenu settingsMenu = new JMenu("Settings");
		this.add(settingsMenu);

		// settings menu: display about DNA window
		JMenuItem aboutWindowItem = new JMenuItem(actionAboutWindow);
		settingsMenu.add(aboutWindowItem);
		
		// settings menu: display logger dialog window
		JMenuItem loggerDialogItem = new JMenuItem(actionLoggerDialog);
		settingsMenu.add(loggerDialogItem);
	}
}