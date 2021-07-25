package dna;

import dna.export.ExporterGUI;
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
	public SpinnerModel popupWidthModel;
	
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
		
		// left collapsible panel
		leftPanel = new LeftPanel();
		JXCollapsiblePane leftCollapsiblePane = new JXCollapsiblePane();
		leftCollapsiblePane.setName("Coder settings");
		leftCollapsiblePane.setCollapsed(false);
		leftCollapsiblePane.setDirection(JXCollapsiblePane.Direction.LEFT);
		codingPanel.add(leftCollapsiblePane, BorderLayout.WEST);
		leftCollapsiblePane.add(leftPanel);
		
		// toggle buttons in upper right corner of DNA GUI
		JPanel toggleButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		popupWidthModel = new SpinnerNumberModel(220, 220, 9990, 10);
		JSpinner popupWidthSpinner = new JSpinner(popupWidthModel);
		popupWidthSpinner.setPreferredSize(new Dimension(60, (int) popupWidthSpinner.getPreferredSize().getHeight()));
		popupWidthSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (Dna.dna.sql != null) {
					String s = String.valueOf((int) popupWidthSpinner.getValue());
					Dna.data.getSettings().put("popupWidth", s);
					Dna.dna.sql.upsertSetting("popupWidth", s);
				}
			}
		});
		toggleButtons.add(new JLabel(new ImageIcon(getClass().getResource("/icons/shape_align_left.png"))));
		toggleButtons.add(popupWidthSpinner);
		toggleButtons.add(Box.createRigidArea(new Dimension(5,5)));
		
		SpinnerModel fontSizeModel = new SpinnerNumberModel(14, 8, 60, 1);
		JSpinner fontSpinner = new JSpinner(fontSizeModel);
		fontSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				Font font = new Font("Monospaced", Font.PLAIN, (int) fontSpinner.getValue());
				Dna.gui.textPanel.textWindow.setFont(font);
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
			Dna.gui.menuBar.importTextButton.setEnabled(true);
			Dna.gui.menuBar.importOldButton.setEnabled(true);
			Dna.gui.menuBar.importDnaButton.setEnabled(true);
			Dna.gui.menuBar.recodeMetaData.setEnabled(true);
		} else {
			Dna.gui.menuBar.importTextButton.setEnabled(false);
			Dna.gui.menuBar.importOldButton.setEnabled(false);
			Dna.gui.menuBar.importDnaButton.setEnabled(false);
			Dna.gui.menuBar.recodeMetaData.setEnabled(false);
		}
		
		if (Dna.gui.leftPanel.editDocPanel.saveDetailsButton != null) {
			if (perm.get("editDocuments") == false) {
				Dna.gui.leftPanel.editDocPanel.saveDetailsButton.setEnabled(false);
				Dna.gui.leftPanel.editDocPanel.cancelButton.setEnabled(false);
			} else {
				Dna.gui.leftPanel.editDocPanel.saveDetailsButton.setEnabled(true);
				Dna.gui.leftPanel.editDocPanel.cancelButton.setEnabled(true);
			}
		}
		
		if (perm.get("deleteDocuments") == false) {
			Dna.gui.documentPanel.menuItemDelete.setEnabled(false);
		} else {
			Dna.gui.documentPanel.menuItemDelete.setEnabled(true);
		}

		if (perm.get("addDocuments") == false) {
			Dna.gui.menuBar.newDocumentButton.setEnabled(false);
		} else {
			Dna.gui.menuBar.newDocumentButton.setEnabled(true);
		}
		
		if (perm.get("editRegex") == false) {
			Dna.gui.rightPanel.rm.setFieldsEnabled(false);
		} else {
			Dna.gui.rightPanel.rm.setFieldsEnabled(true);
		}
		
		if (perm.get("editCoders") == false) {
			Dna.gui.leftPanel.coderPanel.addButton.setEnabled(false);
		} else {
			Dna.gui.leftPanel.coderPanel.addButton.setEnabled(true);
		}

		Dna.gui.leftPanel.coderPanel.coderBox.updateUI();
		Dna.gui.leftPanel.coderPanel.setRowSorterEnabled(true);
		Dna.gui.textPanel.paintStatements();
		Dna.gui.documentPanel.documentFilter();
		Dna.gui.documentPanel.documentTable.updateDocumentView();
		Dna.gui.textPanel.bottomCardPanel.attributePanel.attributeTableModel.sort();
		Dna.gui.textPanel.bottomCardPanel.attributePanel.typeComboBox.updateUI();
		Dna.gui.textPanel.bottomCardPanel.attributePanel.typeComboBox.setEnabled(true);
		Dna.gui.textPanel.bottomCardPanel.attributePanel.entryBox.setEnabled(true);
		Dna.gui.textPanel.bottomCardPanel.attributePanel.addMissingButton.setEnabled(true);
		Dna.gui.textPanel.bottomCardPanel.attributePanel.typeComboBox.setEnabled(true);
		Dna.gui.textPanel.bottomCardPanel.attributePanel.entryBox.setEnabled(true);
		Dna.gui.textPanel.bottomCardPanel.attributePanel.cleanUpButton.setEnabled(true);
		Dna.gui.textPanel.bottomCardPanel.recodePanel.typeComboBox.updateUI();
		Dna.gui.textPanel.bottomCardPanel.recodePanel.applyButton.setEnabled(true);
		Dna.gui.textPanel.bottomCardPanel.recodePanel.resetButton.setEnabled(true);
		Dna.gui.textPanel.bottomCardPanel.recodePanel.typeComboBox.setEnabled(true);
		Dna.gui.textPanel.bottomCardPanel.recodePanel.entryBox.setEnabled(true);
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
			importDnaButton = new JMenuItem("Import from DNA 2.0 file...", importDnaIcon);
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
			importOldButton = new JMenuItem("Import from DNA 1.31 file...", importOldIcon);
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
							return "DNA 1.31 XML file (*.dna)";
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
							JOptionPane.showMessageDialog(Dna.gui, 
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
					new Exporter();
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
						new ExporterGUI();
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
					Dna.gui.rightPanel.statementPanel.statementTable.updateUI();
					Dna.gui.textPanel.paintStatements();
				}
			});
			colorCoderButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Dna.data.getSettings().put("statementColor", "coder");
					Dna.dna.sql.upsertSetting("statementColor", "coder");
					colorStatementTypeButton.setIcon(null);
					colorCoderButton.setIcon(tickIcon);
					Dna.gui.rightPanel.statementPanel.statementTable.updateUI();
					Dna.gui.textPanel.paintStatements();
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