package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.jasypt.util.text.AES256TextEncryptor;

import com.google.gson.Gson;

import gui.DocumentTablePanel;
import dna.Dna;
import dna.Dna.SqlListener;
import logger.LogEvent;
import logger.Logger;
import sql.ConnectionProfile;

public class MainWindow extends JFrame implements SqlListener {
	private static final long serialVersionUID = 2740437090361841747L;
	Container c;
	DocumentTablePanel documentTablePanel;
	DocumentTableModel documentTableModel;
	StatementPanel statementPanel;
	StatementTableModel statementTableModel;
	TextPanel textPanel;
	StatusBar statusBar;
	ActionSaveProfile actionSaveProfile;
	ActionCloseDatabase actionCloseDatabase;
	ActionAddDocument actionAddDocument;
	ActionRemoveDocuments actionRemoveDocuments;
	ActionEditDocuments actionEditDocuments;
	ActionRefresh actionRefresh;
	ActionBatchImportDocuments actionBatchImportDocuments;
	ActionRemoveStatements actionRemoveStatements;

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
		ImageIcon saveProfileIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-download.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionSaveProfile = new ActionSaveProfile("Save connection profile", saveProfileIcon, "Save a connection profile, which acts as a bookmark to a database", KeyEvent.VK_S);
		actionSaveProfile.setEnabled(false);
		
		ImageIcon closeDatabaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		actionCloseDatabase = new ActionCloseDatabase("Close database", closeDatabaseIcon, "Close the connection to the current database and reset graphical user interface", KeyEvent.VK_X);
		actionCloseDatabase.setEnabled(false);
		
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
		MenuBar menuBar = new MenuBar(actionSaveProfile,
				actionCloseDatabase,
				actionAddDocument,
				actionRemoveDocuments,
				actionEditDocuments,
				actionRefresh,
				actionBatchImportDocuments);
		statusBar = new StatusBar();
		statementPanel = new StatementPanel(statementTableModel, actionRemoveStatements);
		textPanel = new TextPanel(documentTableModel);
		
		// add listeners
		Dna.addSqlListener(this);
		Dna.logger.addListener(statusBar);
		statementPanel.addStatementListener(statusBar);
		Dna.addCoderListener(textPanel);
		Dna.addSqlListener(documentTablePanel);
		Dna.addCoderListener(documentTablePanel);
		Dna.addSqlListener(statementPanel);
		Dna.addCoderListener(statementPanel);
		Dna.addSqlListener(toolbar);
		Dna.addCoderListener(toolbar);
		documentTablePanel.addDocumentPanelListener(textPanel);
		toolbar.addToolbarListener(documentTablePanel);
		textPanel.addTextPanelListener(statementPanel);
		
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

		c.add(mainPanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	TextPanel getTextPanel() {
		return textPanel;
	}

	DocumentTableModel getDocumentTableModel() {
		return documentTableModel;
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
	}

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
	
	class ActionAddDocument extends AbstractAction {
		private static final long serialVersionUID = -3332492885668412485L;

		public ActionAddDocument(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			new DocumentEditor();
			documentTablePanel.refresh();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: added a new document.",
					"Added a new document from the GUI.");
			Dna.logger.log(l);
		}
	}
	
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
			documentTablePanel.refresh();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: removed document(s).",
					"Deleted one or more documents in the database from the GUI.");
			Dna.logger.log(l);
		}
	}
	
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
			documentTablePanel.refresh();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: edited meta-data for document(s).",
					"Edited the meta-data for one or more documents in the database.");
			Dna.logger.log(l);
		}
	}
	
	class ActionBatchImportDocuments extends AbstractAction {
		private static final long serialVersionUID = -1460878736275897716L;
		
		public ActionBatchImportDocuments(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			new DocumentBatchImporter();
			documentTablePanel.refresh();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: used document batch importer.",
					"Batch-imported documents to the database.");
			Dna.logger.log(l);
		}
	}
	
	class ActionRefresh extends AbstractAction {
		private static final long serialVersionUID = -5684628034931158710L;

		public ActionRefresh(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			documentTablePanel.refresh();
			statementPanel.refresh();
		}
	}

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
			statementPanel.refresh();
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: removed statement(s).",
					"Deleted one or more statements in the database from the GUI.");
			Dna.logger.log(l);
		}
	}
	
}