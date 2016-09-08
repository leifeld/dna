package dna;

import dna.export.ExportGui;
import dna.panels.DocumentPanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXCollapsiblePane;

@SuppressWarnings("serial")
public class Gui extends JFrame {
	Container c;
	StatusBar statusBar;
	public DocumentPanel documentPanel;
	public TextPanel textPanel;
	public RightPanel rightPanel;
	public LeftPanel leftPanel;
	public MenuBar menuBar;
	
	public int previousDocID = -1;
	
	public Gui() {
		c = getContentPane();
		this.setTitle("Discourse Network Analyzer");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
		this.setIconImage(dna32Icon.getImage());
		
		// close SQL connection before exit
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (Dna.dna.sql != null) {
					Dna.dna.sql.closeConnection();
				}
				dispose();
			}
		});
		
		// center of the DNA window: documents and text panel
		documentPanel = new DocumentPanel();
		textPanel = new TextPanel();
		JPanel codingPanel = new JPanel(new BorderLayout());
		JSplitPane codingSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, documentPanel, textPanel);
		
		// bottom of the DNA window: status bar
		statusBar = new StatusBar();
		codingPanel.add(statusBar, BorderLayout.SOUTH);
		
		// top of the DNA window: menu bar (left) and toggle buttons (right)
		JPanel menuAndButtonsPanel = new JPanel(new BorderLayout());
		menuBar = new MenuBar();
		menuAndButtonsPanel.add(menuBar, BorderLayout.WEST);
		
		// right collapsible panel
		rightPanel = new RightPanel();
		JXCollapsiblePane rightCollapsiblePane = new JXCollapsiblePane();
		rightCollapsiblePane.setName("Statements");
		rightCollapsiblePane.setCollapsed(false);
		rightCollapsiblePane.setDirection(JXCollapsiblePane.Direction.RIGHT);
		codingPanel.add(rightCollapsiblePane, BorderLayout.EAST);
		rightCollapsiblePane.add(rightPanel);
		
		JPanel statementSplitPane = new JPanel(new BorderLayout());
		statementSplitPane.add(codingSplitPane, BorderLayout.CENTER);
		codingPanel.add(statementSplitPane, BorderLayout.CENTER);
		
		leftPanel = new LeftPanel();
		JXCollapsiblePane leftCollapsiblePane = new JXCollapsiblePane();
		leftCollapsiblePane.setName("Coder settings");
		leftCollapsiblePane.setCollapsed(false);
		leftCollapsiblePane.setDirection(JXCollapsiblePane.Direction.LEFT);
		codingPanel.add(leftCollapsiblePane, BorderLayout.WEST);
		leftCollapsiblePane.add(leftPanel);
		
		JPanel toggleButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		SpinnerModel spinnerModel = new SpinnerNumberModel(14, 8, 60, 1);
		JSpinner fontSpinner = new JSpinner(spinnerModel);
		fontSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				Font font = new Font("Monospaced", Font.PLAIN, (int) fontSpinner.getValue());
				Dna.dna.gui.textPanel.textWindow.setFont(font);
			}
		});
		toggleButtons.add(new JLabel(new ImageIcon(getClass().getResource("/icons/font.png"))));
		toggleButtons.add(fontSpinner);
		toggleButtons.add(Box.createRigidArea(new Dimension(5,5)));
		
		JToggleButton leftToggleButton = new JToggleButton(new ImageIcon(getClass().getResource("/icons/application_side_list.png")));
		leftToggleButton.setPreferredSize(new Dimension(36, 18));
		leftToggleButton.setSelected(true);
		leftToggleButton.setName("leftToggle");
		leftToggleButton.addActionListener(leftCollapsiblePane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION));
		
		toggleButtons.add(leftToggleButton);
		JToggleButton bottomToggleButton = new JToggleButton(new ImageIcon(getClass().getResource("/icons/application_split.png")));
		bottomToggleButton.setPreferredSize(new Dimension(36, 18));
		bottomToggleButton.setSelected(false);
		bottomToggleButton.addActionListener(textPanel.collapsiblePane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION));
		
		toggleButtons.add(bottomToggleButton);
		JToggleButton rightToggleButton = new JToggleButton(new ImageIcon(getClass().getResource("/icons/application_side_list_right.png")));
		rightToggleButton.setPreferredSize(new Dimension(36, 18));
		rightToggleButton.setSelected(true);
		rightToggleButton.addActionListener(rightCollapsiblePane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION));
		
		toggleButtons.add(rightToggleButton);
		menuAndButtonsPanel.add(toggleButtons, BorderLayout.EAST);
		
		codingPanel.add(menuAndButtonsPanel, BorderLayout.NORTH);
		c.add(codingPanel);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void refreshGui() {
		HashMap<String, Boolean> perm = Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions();
		if (perm.get("importDocuments") == true) {
			Dna.dna.gui.menuBar.importTextButton.setEnabled(true);
			Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
			Dna.dna.gui.menuBar.importDnaButton.setEnabled(true);
			Dna.dna.gui.menuBar.recodeMetaData.setEnabled(true);
		} else {
			Dna.dna.gui.menuBar.importTextButton.setEnabled(false);
			Dna.dna.gui.menuBar.importOldButton.setEnabled(false);
			Dna.dna.gui.menuBar.importDnaButton.setEnabled(false);
			Dna.dna.gui.menuBar.recodeMetaData.setEnabled(false);
		}
		
		if (Dna.dna.gui.leftPanel.editDocPanel.saveDetailsButton != null) {
			if (perm.get("editDocuments") == false) {
				Dna.dna.gui.leftPanel.editDocPanel.saveDetailsButton.setEnabled(false);
				Dna.dna.gui.leftPanel.editDocPanel.cancelButton.setEnabled(false);
			} else {
				Dna.dna.gui.leftPanel.editDocPanel.saveDetailsButton.setEnabled(true);
				Dna.dna.gui.leftPanel.editDocPanel.cancelButton.setEnabled(true);
			}
		}
		
		if (perm.get("deleteDocuments") == false) {
			Dna.dna.gui.documentPanel.menuItemDelete.setEnabled(false);
		} else {
			Dna.dna.gui.documentPanel.menuItemDelete.setEnabled(true);
		}

		if (perm.get("addDocuments") == false) {
			Dna.dna.gui.menuBar.newDocumentButton.setEnabled(false);
		} else {
			Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		}
		
		if (perm.get("editRegex") == false) {
			Dna.dna.gui.rightPanel.rm.setFieldsEnabled(false);
		} else {
			Dna.dna.gui.rightPanel.rm.setFieldsEnabled(true);
		}
		
		if (perm.get("editCoders") == false) {
			Dna.dna.gui.leftPanel.coderPanel.addButton.setEnabled(false);
		} else {
			Dna.dna.gui.leftPanel.coderPanel.addButton.setEnabled(true);
		}

		Dna.dna.gui.leftPanel.coderPanel.coderBox.updateUI();
		Dna.dna.gui.leftPanel.coderPanel.setRowSorterEnabled(true);
		Dna.dna.gui.textPanel.paintStatements();
		Dna.dna.gui.documentPanel.documentFilter();
		Dna.dna.gui.documentPanel.documentTable.updateDocumentView();
		Dna.dna.gui.textPanel.bottomCardPanel.recodePanel.typeComboBox.updateUI();
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
			if (Dna.dna.sql == null) {
				currentFileLabel.setText("Current file: none");
			} else {
				currentFileLabel.setText("Current file: " + Dna.dna.sql.dbfile);
			}
		}
	}

	public class MenuBar extends JMenuBar {
		JMenu fileMenu, documentMenu, exportMenu, settingsMenu;
		JMenuItem closeDatabase, newDatabase, openDatabase, importHTMLButton, typeEditorButton;
		public JMenuItem newDocumentButton;
		JMenuItem importDnaButton;
		public JMenuItem importTextButton;
		public JMenuItem importOldButton;
		JMenuItem recodeMetaData;
		JMenuItem networkButton;
		JMenuItem aboutButton;
		JMenuItem colorStatementTypeButton;
		JMenuItem colorCoderButton;
		JMenuItem redirectButton;
		
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
			Icon databaseIcon = new ImageIcon(getClass().getResource("/icons/database.png"));
			newDatabase = new JMenuItem("New DNA database...", databaseIcon);
			newDatabase.setToolTipText("create a new DNA database...");
			fileMenu.add(newDatabase);
			newDatabase.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new NewDatabaseDialog();
				}
			});
			
			//File menu: open database
			Icon dbIcon = new ImageIcon(getClass().getResource("/icons/folder.png"));
			openDatabase = new JMenuItem("Open DNA database...", dbIcon);
			openDatabase.setToolTipText("open a local or remote database...");
			openDatabase.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new OpenDatabaseDialog();
				}
			});
			fileMenu.add(openDatabase);
			
			//File menu: close current database
			Icon closeIcon = new ImageIcon( getClass().getResource("/icons/disconnect.png") );
			closeDatabase = new JMenuItem("Close database or file", closeIcon);
			closeDatabase.setToolTipText( "close current database file or remote connection" );
			fileMenu.add(closeDatabase);
			closeDatabase.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Dna.dna.closeDatabase();
				}
			});
			closeDatabase.setEnabled(false);

			//File menu: exit
			Icon exitIcon = new ImageIcon( getClass().getResource("/icons/door_out.png") );
			JMenuItem exit = new JMenuItem("Exit", exitIcon);
			exit.setToolTipText( "quit DNA" );
			fileMenu.add(exit);
			exit.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (Dna.dna.sql != null) {
						Dna.dna.sql.closeConnection();
					}
					dispose();
				}
			});

			//Document menu: add new document
			Icon newDocumentIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
			newDocumentButton = new JMenuItem("Add new document...", newDocumentIcon);
			newDocumentButton.setToolTipText( "add new document..." );
			documentMenu.add(newDocumentButton);
			newDocumentButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new NewDocumentDialog();
				}
			});
			newDocumentButton.setEnabled(false);

			//LB.Add: Document menu: import documents from html file
			/*
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
			*/
			
			//Document menu: recode variables
			/*
			Icon recodeVariableIcon = new ImageIcon(getClass().getResource("/icons/pencil.png"));
			//TODO: pencil-icon? or database_edit.png?
			recodeVariableButton = new JMenuItem("Recode variables...", recodeVariableIcon);
			recodeVariableButton.setToolTipText("recode variables");
			documentMenu.add(recodeVariableButton);
			recodeVariableButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					new Recode();
				}
			});
			recodeVariableButton.setEnabled(false);
			*/
			
			//Document menu: import text files
			Icon textFileIcon = new ImageIcon(getClass().getResource("/icons/folder.png"));
			importTextButton = new JMenuItem("Import text files...", textFileIcon);
			importTextButton.setToolTipText( "import text files from folder..." );
			documentMenu.add(importTextButton);
			importTextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new TextFileImporter();
				}
			});
			importTextButton.setEnabled(false);
			
			//Document menu: import documents from another DNA database
			Icon importDnaIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
			importDnaButton = new JMenuItem("Import from DNA 2.xx file...", importDnaIcon);
			importDnaButton.setToolTipText("import from DNA 2.0 file...");
			documentMenu.add(importDnaButton);
			importDnaButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					//File filter
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".dna") || f.isDirectory();
						}
						public String getDescription() {
							return "DNA 2.0 file (*.dna)";
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
						new ImportDnaDocuments(dbfile);
					}
				}
			});
			importDnaButton.setEnabled(false);

			//Document menu: import old DNA dataset
			Icon importOldIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
			importOldButton = new JMenuItem("Import from old DNA 1.xx file...", importOldIcon);
			importOldButton.setToolTipText("import from old DNA 1.31 XML dataset...");
			documentMenu.add(importOldButton);
			importOldButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					//File filter
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".dna") || f.isDirectory();
						}
						public String getDescription() {
							return "DNA 1.xx XML file (*.dna)";
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
						try {
							new ImportOldDNA(dbfile);
						} catch (NullPointerException npe) {
							JOptionPane.showMessageDialog(Dna.dna.gui, 
									"The default statement type \"DNA Statement\" is not available or has been modified!");
						}
					}
				}
			});
			importOldButton.setEnabled(false);

			//Document menu: Batch-recode meta-data
			Icon recodeIcon = new ImageIcon(getClass().getResource("/icons/table_key.png"));
			recodeMetaData = new JMenuItem("Batch-recode meta-data...", recodeIcon);
			recodeMetaData.setToolTipText("change or auto-complete author, section etc. for multiple documents at once...");
			recodeMetaData.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new RecodeDialog();
				}
			});
			documentMenu.add(recodeMetaData);
			recodeMetaData.setEnabled(false);
			
			/*
			//Export menu: network export
			networkButton = new JMenuItem("Export network...", networkIcon);
			networkButton.setToolTipText( "export a network file..." );
			exportMenu.add(networkButton);
			networkButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new NetworkExporter();
				}
			});
			networkButton.setEnabled(true);
			*/
			
			//Export menu: network export
			Icon networkIcon = new ImageIcon(getClass().getResource("/icons/chart_organisation.png"));
			networkButton = new JMenuItem("Export network...", networkIcon);
			networkButton.setToolTipText( "export a network file..." );
			exportMenu.add(networkButton);
			networkButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (Dna.data.getDocuments().size() > 0 && Dna.data.getStatements().size() > 0) {
						new ExportGui();
					} else {
						System.err.println("Warning: Network export not possible because no statements present.");
					}
					
				}
			});
			networkButton.setEnabled(false);
			
			//Settings menu: statement color by statement type or coder?
			Icon tickIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
			colorStatementTypeButton = new JMenuItem("Color statements by type");
			colorCoderButton = new JMenuItem("Color statements by coder", tickIcon);
			colorStatementTypeButton.setToolTipText("use the color of the respective statement type to paint statements");
			colorCoderButton.setToolTipText("use the color of the respective coder to paint statements");
			settingsMenu.add(colorStatementTypeButton);
			settingsMenu.add(colorCoderButton);
			colorStatementTypeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Dna.data.getSettings().put("statementColor", "statementType");
					Dna.dna.sql.upsertSetting("statementColor", "statementType");
					colorStatementTypeButton.setIcon(tickIcon);
					colorCoderButton.setIcon(null);
					Dna.dna.gui.rightPanel.statementPanel.statementTable.updateUI();
					Dna.dna.gui.textPanel.paintStatements();
				}
			});
			colorCoderButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Dna.data.getSettings().put("statementColor", "coder");
					Dna.dna.sql.upsertSetting("statementColor", "coder");
					colorStatementTypeButton.setIcon(null);
					colorCoderButton.setIcon(tickIcon);
					Dna.dna.gui.rightPanel.statementPanel.statementTable.updateUI();
					Dna.dna.gui.textPanel.paintStatements();
				}
			});
			settingsMenu.addSeparator();
			colorStatementTypeButton.setEnabled(false);
			colorCoderButton.setEnabled(false);
			
			//Settings menu: redirect output to file
			Icon redirectIcon = new ImageIcon(getClass().getResource("/icons/report_add.png"));
			redirectButton = new JMenuItem("Redirect error messages...", redirectIcon);
			redirectButton.setToolTipText( "redirect all exceptions to a text file..." );
			settingsMenu.add(redirectButton);
			redirectButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (redirectButton.getText().equals("Redirect error messages...")) {
						//File filter
						JFileChooser fc = new JFileChooser();
						fc.setFileFilter(new FileFilter() {
							public boolean accept(File f) {
								return f.getName().toLowerCase().endsWith(".txt") || f.isDirectory();
							}
							public String getDescription() {
								return "Text file (*.txt)";
							}
						});

						int returnVal = fc.showSaveDialog(dna.Gui.this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							String fileName;
							File file = fc.getSelectedFile();
							if (!file.getPath().endsWith(".txt")) {
								fileName = file.getPath() + ".txt";
							} else {
								fileName = file.getPath();
							}
							
							// redirect exceptions to file
							FileOutputStream fos;
							try {
								fos = new FileOutputStream(fileName);
								PrintStream ps = new PrintStream(fos);
								System.setErr(ps);
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							}
							redirectButton.setText(fileName);
							redirectButton.setIcon(new ImageIcon(getClass().getResource("/icons/report_delete.png")));
							System.err.println("DNA version: " + Dna.dna.version + " (" + Dna.dna.date + ")");
							System.err.println("Java version: " + System.getProperty("java.version"));
							System.err.println("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
						}
					} else {
						System.setErr(Dna.dna.console);
						redirectButton.setText("Redirect error messages...");
						redirectButton.setIcon(new ImageIcon(getClass().getResource("/icons/report_add.png")));
					}
				}
			});
			settingsMenu.addSeparator();
			
			//Settings menu: about DNA
			Icon aboutIcon = new ImageIcon(getClass().getResource("/icons/dna16.png"));
			aboutButton = new JMenuItem("About DNA...", aboutIcon);
			aboutButton.setToolTipText( "show information about the Discourse Network Analyzer..." );
			settingsMenu.add(aboutButton);
			aboutButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new AboutWindow(Dna.dna.version, Dna.dna.date);
				}
			});
		}
	}
}