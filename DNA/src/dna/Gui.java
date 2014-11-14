package dna;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

public class Gui extends JFrame {

	/**
	 * DNA GUI
	 */
	private static final long serialVersionUID = 6798727706826962027L;
	Container c;
	StatusBar statusBar;
	DocumentPanel documentPanel;
	TextPanel textPanel;
	SidebarPanel sidebarPanel;
	MenuBar menuBar;

	public Gui() {
		c = getContentPane();
		this.setTitle("Discourse Network Analyzer");
		//this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource(
				"/icons/dna32.png"));
		this.setIconImage(dna32Icon.getImage());

		//addWindowListener(new WindowAdapter() {
		//	public void windowClosing(WindowEvent e) {
		//		dispose();
		//	}
		//});

		documentPanel = new DocumentPanel();
		textPanel = new TextPanel();

		JPanel codingPanel = new JPanel(new BorderLayout());
		JSplitPane codingSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
				documentPanel, textPanel);
		menuBar = new MenuBar();
		codingPanel.add(menuBar, BorderLayout.NORTH);
		statusBar = new StatusBar();
		codingPanel.add(statusBar, BorderLayout.SOUTH);

		sidebarPanel = new SidebarPanel();

		JPanel statementSplitPane = new JPanel(new BorderLayout());
		statementSplitPane.add(codingSplitPane, BorderLayout.CENTER);
		codingPanel.add(statementSplitPane, BorderLayout.CENTER);
		codingPanel.add(sidebarPanel, BorderLayout.EAST);

		c.add(codingPanel);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);

	}

	class StatusBar extends JPanel {

		private static final long serialVersionUID = 1L;
		JLabel currentFileLabel, loading;

		public StatusBar() {
			this.setLayout( new BorderLayout() );
			currentFileLabel = new JLabel("Current file: none");
			this.add(currentFileLabel, BorderLayout.WEST);
			loading = new JLabel("loading...", JLabel.TRAILING);
			loading.setVisible(false);
			this.add(loading, BorderLayout.EAST);
		}

		public void resetLabel() {
			String fn = Dna.dna.db.getFileName();
			if (fn == null) {
				currentFileLabel.setText("Current file: none");
			} else {
				currentFileLabel.setText("Current file: " + fn);
			}
		}
	}


	public class DocumentPanel extends JScrollPane {

		private static final long serialVersionUID = 1L;
		DocumentContainer documentContainer;
		DocumentTable documentTable;

		public DocumentPanel() {
			documentContainer = new DocumentContainer();
			documentTable = new DocumentTable();
			documentTable.setModel(documentContainer);
			this.setViewportView(documentTable);
			setPreferredSize(new Dimension(700, 100));
			documentTable.getColumnModel().getColumn(0).setPreferredWidth(680);
			documentTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		}

		public class DocumentTable extends JTable {

			private static final long serialVersionUID = 1L;

			public DocumentTable() {
				setModel(new DocumentContainer());
				setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				getTableHeader().setReorderingAllowed( false );
				putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

				getSelectionModel().addListSelectionListener(new 
						ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							return;
						}
						int selectedRow = getSelectedRow();
						if (selectedRow == -1) {
							textPanel.setDocumentText("");
							Dna.dna.gui.menuBar.changeDocumentButton.
							setEnabled(false);
							Dna.dna.gui.menuBar.removeDocumentButton.
							setEnabled(false);
						} else {
							int id = documentPanel.documentContainer.
									get(selectedRow).getId();
							Document document = Dna.dna.db.getDocument(id);
							String text = document.getText();
							textPanel.setDocumentId(id);
							textPanel.setDocumentText(text);
							textPanel.setEnabled(true);
							Dna.dna.gui.menuBar.changeDocumentButton.
							setEnabled(true);
							Dna.dna.gui.menuBar.removeDocumentButton.
							setEnabled(true);
						}
						if (Dna.dna.gui.sidebarPanel.statementFilter.
								showCurrent.isSelected()) {
							Dna.dna.gui.sidebarPanel.statementFilter.
							documentFilter();
						}

						if (Dna.dna.db.getFileName() != null) {
							textPanel.paintStatements();
						}
						textPanel.setCaretPosition( 0 );
					}
				});
			}
		}
	}

	class MenuBar extends JMenuBar {

		private static final long serialVersionUID = 1L;

		JMenu fileMenu, documentMenu, exportMenu, settingsMenu;
		JMenuItem closeFile, newDocumentButton, importHTMLButton,  
		typeEditorButton,changeDocumentButton, removeDocumentButton, 
		importOldButton, networkButton, aboutButton, 
		recodeVariableButton;

		public MenuBar() {
			fileMenu = new JMenu("File");
			this.add(fileMenu);
			documentMenu = new JMenu("Document");
			this.add(documentMenu);
			exportMenu = new JMenu("Export");
			this.add(exportMenu);
			settingsMenu = new JMenu("Settings");
			this.add(settingsMenu);

			//File menu: new DNA database file...
			Icon databaseIcon = new ImageIcon(getClass().getResource(
					"/icons/database.png"));
			JMenuItem newDatabase = new JMenuItem("New database file...", 
					databaseIcon);
			newDatabase.setToolTipText( "create a new database file..." );
			fileMenu.add(newDatabase);
			newDatabase.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".dna") 
									|| f.isDirectory();
						}
						public String getDescription() {
							return "Discourse Network Analyzer database " +
									"(*.dna)";
						}
					});

					int returnVal = fc.showSaveDialog(dna.Gui.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						String filename = new String(file.getPath());
						if (!filename.endsWith(".dna")) {
							filename = filename + ".dna";
						}
						Dna.dna.newFile(filename);
					}
				}
			});

			//File menu: open DNA file
			Icon openIcon = new ImageIcon(getClass().getResource(
					"/icons/folder.png"));
			JMenuItem openMenuItem = new JMenuItem("Open database file...", 
					openIcon);
			openMenuItem.setToolTipText( "open an existing database file..." );
			openMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					//File filter
					JFileChooser fc = new JFileChooser(); //TODO: THIS SHOULD REMEMBER THE LAST DIRECTORY USED (CAN BE PUT IN BRACKETS AS A STRING)
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".dna")
									|| f.isDirectory();
						}
						public String getDescription() {
							return "Discourse Network Analyzer database " +
									"(*.dna)";
						}
					});

					int returnVal = fc.showOpenDialog(dna.Gui.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						String dbfile;
						if (!file.getPath().endsWith(".dna")) {
							dbfile = file.getPath() + ".dna";
						} else {
							dbfile = file.getPath();
						}
						Dna.dna.openFile(dbfile);
					}
				}
			});
			fileMenu.add(openMenuItem);

			//File menu: open remote database
			Icon dbIcon = new ImageIcon(getClass().getResource(
					"/icons/database_link.png"));
			JMenuItem dbMenuItem = new JMenuItem("Open remote database...", 
					dbIcon);
			dbMenuItem.setToolTipText("establish a connection to a mySQL " +
					"or MS SQL Server database...");
			dbMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new SQLConnectionDialog();
				}
			});
			fileMenu.add(dbMenuItem);

			//File menu: close current database file
			Icon closeIcon = new ImageIcon( getClass().getResource(
					"/icons/cancel.png") );
			closeFile = new JMenuItem("Close database or file", closeIcon);
			closeFile.setToolTipText( "close current database file or " +
					"remote connection" );
			fileMenu.add(closeFile);
			closeFile.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Dna.dna.closeFile();
				}
			});
			closeFile.setEnabled(false);

			//File menu: exit
			Icon exitIcon = new ImageIcon( getClass().getResource(
					"/icons/door_out.png") );
			JMenuItem exit = new JMenuItem("Exit", exitIcon);
			exit.setToolTipText( "quit DNA" );
			fileMenu.add(exit);
			exit.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			//Document menu: add new document
			Icon newDocumentIcon = new ImageIcon(getClass().getResource(
					"/icons/table_add.png"));
			newDocumentButton = new JMenuItem("Add new document...", 
					newDocumentIcon);
			newDocumentButton.setToolTipText( "add new document..." );
			documentMenu.add(newDocumentButton);
			newDocumentButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new NewDocumentWindow();
				}
			});
			newDocumentButton.setEnabled(false);

			//LB.Add: Document menu: import documents from html file
			Icon importHTMLIcon = new ImageIcon(getClass().getResource(
					"/icons/table_add.png"));
			importHTMLButton = new JMenuItem("Import from  HTML-file(s)/URL(s)...", 
					importHTMLIcon);
			importHTMLButton.setToolTipText( "Import document(s) from html-file(s) or webpage(s)" );
			documentMenu.add(importHTMLButton);
			importHTMLButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new ImportHTMLWebpageTab();
				}
			});
			importHTMLButton.setEnabled(false);

			//Document menu: change document properties
			Icon changeDocumentIcon = new ImageIcon(getClass().getResource(
					"/icons/table_edit.png"));
			changeDocumentButton = new JMenuItem("Edit document metadata...", 
					changeDocumentIcon);
			changeDocumentButton.setToolTipText( "edit the title, date etc. " +
					"of the currently selected document..." );
			documentMenu.add(changeDocumentButton);
			changeDocumentButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int row = Dna.dna.gui.documentPanel.documentTable.
							getSelectedRow();
					int documentId = Dna.dna.gui.documentPanel.
							documentContainer.getIdByRowIndex(row);
					new DocumentProperties(documentId);
				}
			});
			changeDocumentButton.setEnabled(false);

			//Document menu: recode variables
			Icon recodeVariableIcon = new ImageIcon(getClass().getResource(
					"/icons/pencil.png"));
			//TODO: pencil-icon? or database_edit.png?
			recodeVariableButton = new JMenuItem("Recode variables...", 
					recodeVariableIcon);
			recodeVariableButton.setToolTipText("recode variables");
			documentMenu.add(recodeVariableButton);
			recodeVariableButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					new Recode();
				}
			});

			//Document menu: delete selected document
			Icon removeDocumentIcon = new ImageIcon(getClass().getResource(
					"/icons/table_delete.png"));
			removeDocumentButton = new JMenuItem("Delete selected document", 
					removeDocumentIcon);
			removeDocumentButton.setToolTipText( "delete selected document" );
			documentMenu.add(removeDocumentButton);
			removeDocumentButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int dialog = JOptionPane.showConfirmDialog(Dna.dna.gui, 
							"Are you sure you want to delete the selected " +
									"document?", "Confirmation required", 
									JOptionPane.YES_NO_OPTION);
					int row = Dna.dna.gui.documentPanel.documentTable.
							getSelectedRow();
					if (row != -1 && dialog == 0) {
						int documentId = Dna.dna.gui.documentPanel.
								documentContainer.getIdByRowIndex(row);
						Dna.dna.removeDocument(documentId);
					}
				}
			});
			removeDocumentButton.setEnabled(false);

			//Document menu: import old DNA dataset
			Icon importOldIcon = new ImageIcon(getClass().getResource(
					"/icons/table_add.png"));
			importOldButton = new JMenuItem(
					"Import from old DNA 1.xx file...", importOldIcon);
			importOldButton.setToolTipText(
					"import from old DNA < 2.0 XML dataset...");
			documentMenu.add(importOldButton);
			importOldButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					//File filter
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".dna")
									|| f.isDirectory();
						}
						public String getDescription() {
							return "DNA 1.xx XML file " +
									"(*.dna)";
						}
					});

					int returnVal = fc.showOpenDialog(dna.Gui.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						String dbfile;
						if (!file.getPath().endsWith(".dna")) {
							dbfile = file.getPath() + ".dna";
						} else {
							dbfile = file.getPath();
						}
						new ImportOldDNA1XML(dbfile);
					}
				}
			});
			importOldButton.setEnabled(false);

			//Export menu: network export
			Icon networkIcon = new ImageIcon(getClass().getResource(
					"/icons/chart_organisation.png"));
			networkButton = new JMenuItem("Export network...", networkIcon);
			networkButton.setToolTipText( "export a network file..." );
			exportMenu.add(networkButton);
			networkButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new NetworkExporter();
				}
			});
			networkButton.setEnabled(false);

			//Settings menu: edit statement types
			Icon typeEditorIcon = new ImageIcon(getClass().getResource(
					"/icons/application_form.png"));
			typeEditorButton = new JMenuItem("Edit statement types...", 
					typeEditorIcon);
			typeEditorButton.setToolTipText( "edit statement types..." );
			settingsMenu.add(typeEditorButton);
			typeEditorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new StatementTypeEditor();
				}
			});
			typeEditorButton.setEnabled(false);

			//Settings menu: about DNA
			Icon aboutIcon = new ImageIcon(getClass().getResource(
					"/icons/dna16.png"));
			aboutButton = new JMenuItem("About DNA...", aboutIcon);
			aboutButton.setToolTipText( "show information about the " +
					"Discourse Network Analyzer..." );
			settingsMenu.add(aboutButton);
			aboutButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new AboutWindow(Dna.dna.version, Dna.dna.date);
				}
			});
			typeEditorButton.setEnabled(false);

		}
	}

	public class SQLConnectionDialog extends JFrame {

		private static final long serialVersionUID = 1L;
		JTextField connectionField, loginField, pwField, dbField;
		JButton connectButton, cancelButton;
		JRadioButton mysqlButton, mssqlButton;
		JLabel dbLabel, connectionLabel;

		public SQLConnectionDialog () {
			this.setTitle("Enter remote SQL connection details.");
			this.setDefaultCloseOperation(JFrame.
					DISPOSE_ON_CLOSE);
			Icon mysqlIcon = new ImageIcon(getClass().getResource(
					"/icons/database_link.png"));
			this.setIconImage(((ImageIcon) mysqlIcon).getImage());
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			JPanel panel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(3, 3, 3, 3);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = 0;

			connectionLabel = new JLabel("mysql://", JLabel.TRAILING);
			connectionField = new JTextField("");
			connectionField.setColumns(30);

			dbLabel = new JLabel("Database:", JLabel.TRAILING);
			dbField = new JTextField();

			JLabel typeLabel = new JLabel("DB type:", JLabel.TRAILING);
			panel.add(typeLabel, gbc);
			gbc.gridx++;
			mysqlButton = new JRadioButton("mySQL");
			mysqlButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dbField.setText("");
					dbField.setEnabled(false);
					dbLabel.setEnabled(false);
					connectionLabel.setText("mysql://");
				}
			});
			mysqlButton.setSelected(true);
			dbField.setEnabled(false);
			dbLabel.setEnabled(false);
			panel.add(mysqlButton, gbc);
			gbc.gridx++;
			gbc.gridwidth = 2;
			mssqlButton = new JRadioButton("MS SQL Server");
			mssqlButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dbField.setEnabled(true);
					dbLabel.setEnabled(true);
					connectionLabel.setText("sqlserver://");
				}
			});
			panel.add(mssqlButton, gbc);
			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(mysqlButton);
			buttonGroup.add(mssqlButton);
			gbc.gridx = 0;
			gbc.gridy++;
			gbc.gridwidth = 1;

			panel.add(connectionLabel, gbc);
			gbc.gridx++;
			gbc.gridwidth = 3;
			panel.add(connectionField, gbc);
			DocumentListener dl = new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					checkButton();
				}
				public void insertUpdate(DocumentEvent e) {
					checkButton();
				}
				public void removeUpdate(DocumentEvent e) {
					checkButton();
				}
				public void checkButton() {
					connectButton.setEnabled(true);
					if (connectionField.getText().equals("") || loginField.
							getText().equals("") || pwField.getText().
							equals("") || connectionField.getText().equals(
									"mysql://") || (!mysqlButton.isSelected() && 
											!mssqlButton.isSelected())) {
						connectButton.setEnabled(false);
					}
				}
			};
			connectionField.getDocument().addDocumentListener(dl);
			gbc.gridx--;
			gbc.gridy++;
			gbc.gridwidth = 1;

			panel.add(dbLabel, gbc);
			gbc.gridx++;

			panel.add(dbField, gbc);
			gbc.gridwidth = 1;
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel loginLabel = new JLabel("User name:", JLabel.TRAILING);
			panel.add(loginLabel, gbc);
			gbc.gridx++;
			loginField = new JTextField(10);
			loginField.setText("");
			loginField.getDocument().addDocumentListener(dl);
			panel.add(loginField, gbc);
			gbc.gridx++;
			JLabel pwLabel = new JLabel("Password:", JLabel.TRAILING);
			panel.add(pwLabel, gbc);
			gbc.gridx++;
			pwField = new JTextField(10);
			pwField.setText("");
			pwField.getDocument().addDocumentListener(dl);
			panel.add(pwField, gbc);
			gbc.gridx = 0;
			gbc.gridy++;
			gbc.gridwidth = 2;
			connectButton = new JButton("Connect");
			connectButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String url = connectionField.getText();
					if (mysqlButton.isSelected()) {
						if (dbField.getText() != null && !dbField.equals("")) {
							if (url.endsWith("/")) {
								url = url + dbField.getText();
							} else {
								url = url + "/" + dbField.getText();
							}
						}
						Dna.dna.openMySQL(connectionField.getText(), 
								loginField.getText(), pwField.getText());
					} else if (mssqlButton.isSelected()) {
						if (dbField.getText() == null || dbField.equals("")) {
							System.err.println("A database name must be " +
									"provided for MSSQL databases.");
						}
						Dna.dna.openMSSQL(connectionField.getText(), 
								dbField.getText(), loginField.getText(), 
								pwField.getText());
					}
					dispose();
				}
			});
			connectButton.setEnabled(false);
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			panel.add(connectButton, gbc);
			gbc.gridx++;
			gbc.gridx++;
			panel.add(cancelButton, gbc);
			this.add(panel);
			this.pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}
	}
}
