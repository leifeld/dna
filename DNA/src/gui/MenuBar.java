package gui;

import java.awt.Image;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import gui.MainWindow.ActionAddDocument;
import gui.MainWindow.ActionBatchImportDocuments;
import gui.MainWindow.ActionCloseDatabase;
import gui.MainWindow.ActionEditDocuments;
import gui.MainWindow.ActionRefresh;
import gui.MainWindow.ActionRemoveDocuments;
import gui.MainWindow.ActionRemoveStatements;
import gui.MainWindow.ActionSaveProfile;

public class MenuBar extends JMenuBar {
	private static final long serialVersionUID = 6631392079690346097L;
	private ActionOpenDatabase actionOpenDatabase;
	private ActionOpenProfile actionOpenProfile;
	private ActionCreateDatabase actionCreateDatabase;
	private ActionQuit actionQuit;
	private ActionAboutWindow actionAboutWindow;
	private ActionLoggerDialog actionLoggerDialog;

	public MenuBar(ActionSaveProfile actionSaveProfile,
			ActionCloseDatabase actionCloseDatabase,
			ActionAddDocument actionAddDocument,
			ActionRemoveDocuments actionRemoveDocuments,
			ActionEditDocuments actionEditDocuments,
			ActionRefresh actionRefresh,
			ActionBatchImportDocuments actionBatchImportDocuments,
			ActionRemoveStatements actionRemoveStatements) {
		JMenu databaseMenu = new JMenu("Database");
		this.add(databaseMenu);
		JMenu documentMenu = new JMenu("Document");
		this.add(documentMenu);
		JMenu statementsMenu = new JMenu("Statements");
		this.add(statementsMenu);
		JMenu exportMenu = new JMenu("Export");
		this.add(exportMenu);
		JMenu settingsMenu = new JMenu("Settings");
		this.add(settingsMenu);

		// database menu: open a database
		ImageIcon openDatabaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionOpenDatabase = new ActionOpenDatabase("Open DNA database", openDatabaseIcon, "Open a dialog window to establish a connection to a remote or file-based database", KeyEvent.VK_O);
		JMenuItem openDatabaseItem = new JMenuItem(actionOpenDatabase);
		databaseMenu.add(openDatabaseItem);

		// database menu: close database
		JMenuItem closeDatabaseItem = new JMenuItem(actionCloseDatabase);
		databaseMenu.add(closeDatabaseItem);

		// database menu: create a new database
		ImageIcon createDatabaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-plus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionCreateDatabase = new ActionCreateDatabase("Create new DNA database", createDatabaseIcon, "Open a dialog window to create a new remote or file-based database", KeyEvent.VK_C);
		JMenuItem createDatabaseItem = new JMenuItem(actionCreateDatabase);
		databaseMenu.add(createDatabaseItem);

		// database menu: open a connection profile
		ImageIcon openProfileIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-link.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionOpenProfile = new ActionOpenProfile("Open connection profile", openProfileIcon, "Open a connection profile, which acts as a bookmark to a database", KeyEvent.VK_P);
		JMenuItem openProfileItem = new JMenuItem(actionOpenProfile);
		databaseMenu.add(openProfileItem);

		// database menu: save a connection profile
		JMenuItem saveProfileItem = new JMenuItem(actionSaveProfile);
		databaseMenu.add(saveProfileItem);

		// database menu: quit DNA
		ImageIcon quitIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-logout.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionQuit = new ActionQuit("Exit / quit", quitIcon, "Close the Discourse Network Analyzer", KeyEvent.VK_Q);
		JMenuItem quitItem = new JMenuItem(actionQuit);
		databaseMenu.add(quitItem);

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

		// statements menu: remove statements
		JMenuItem removeStatementsItem = new JMenuItem(actionRemoveStatements);
		documentMenu.add(removeStatementsItem);

		// settings menu: display about DNA window
		ImageIcon aboutIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/dna32.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionAboutWindow = new ActionAboutWindow("About DNA", aboutIcon, "Display information about DNA", KeyEvent.VK_B);
		JMenuItem aboutWindowItem = new JMenuItem(actionAboutWindow);
		settingsMenu.add(aboutWindowItem);
		
		// settings menu: display logger dialog window
		ImageIcon loggerIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-message-report.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionLoggerDialog = new ActionLoggerDialog("Message log", loggerIcon, "Display a log of messages, warnings, and errors in a dialog window", KeyEvent.VK_L);
		JMenuItem loggerDialogItem = new JMenuItem(actionLoggerDialog);
		settingsMenu.add(loggerDialogItem);
	}

	ActionOpenDatabase getActionOpenDatabase() {
		return actionOpenDatabase;
	}

	ActionOpenProfile getActionOpenProfile() {
		return actionOpenProfile;
	}

	ActionCreateDatabase getActionCreateDatabase() {
		return actionCreateDatabase;
	}

	ActionQuit getActionQuit() {
		return actionQuit;
	}

	ActionAboutWindow getActionAboutWindow() {
		return actionAboutWindow;
	}

	ActionLoggerDialog getActionLoggerDialog() {
		return actionLoggerDialog;
	}
}