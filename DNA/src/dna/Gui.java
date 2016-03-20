package dna;

import dna.dataStructures.*;
import dna.renderer.DocumentTableModel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXCollapsiblePane;

@SuppressWarnings("serial")
public class Gui extends JFrame {
	Container c;
	StatusBar statusBar;
	public DocumentPanel documentPanel;
	public TextPanel textPanel;
	public RightPanel rightPanel;
	public LeftPanel leftPanel;
	MenuBar menuBar;
	
	int previousDocID = -1;

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
			String fn = Dna.data.getSettings().get("filename");
			if (fn == null) {
				currentFileLabel.setText("Current file: none");
			} else {
				currentFileLabel.setText("Current file: " + fn);
			}
		}
	}


	public class DocumentPanel extends JScrollPane {
		public DocumentTableModel documentContainer;
		public DocumentTable documentTable;
		TableRowSorter<DocumentTableModel> sorter;

		public DocumentPanel() {
			if(Dna.dna != null) {
				documentContainer = new DocumentTableModel(Dna.data.getDocuments()); //SK
			} else {
				documentContainer = new DocumentTableModel();
			}
			documentTable = new DocumentTable();
			documentTable.setModel(documentContainer);
			this.setViewportView(documentTable);
			setPreferredSize(new Dimension(700, 100));
			documentTable.getColumnModel().getColumn(0).setPreferredWidth(680);
			documentTable.getColumnModel().getColumn(1).setPreferredWidth(100);
			
			setRowSorterEnabled(true);
		}

		public void setRowSorterEnabled(boolean enabled) {
			if (enabled == true) {
				sorter = new TableRowSorter<DocumentTableModel>(documentContainer) {
					public void toggleSortOrder(int i) {
						//leave blank; overwritten method makes the table unsortable
					}
				};
				documentTable.setRowSorter(sorter);
			} else {
				documentTable.setRowSorter(null);
			}
		}
		
		public void documentFilter() {
			RowFilter<DocumentTableModel, Integer> documentFilter = new RowFilter<DocumentTableModel, Integer>() {
				public boolean include(Entry<? extends DocumentTableModel, ? extends Integer> entry) {
					DocumentTableModel dtm = entry.getModel();
					Document d = dtm.get(entry.getIdentifier());
					int documentId = d.getId();
					boolean[] b = Dna.data.getActiveDocumentPermissions(documentId);
					if (b[0] == true) {
						return true;
					}
					return false;
				}
			};
			sorter.setRowFilter(documentFilter);
		}
		
		public class DocumentTable extends JTable {
			
			public DocumentTable() {
				setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				getTableHeader().setReorderingAllowed(false);
				putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

				getSelectionModel().addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							return;
						}
						updateDocumentView();
					}
				});
			}
			
			public void updateDocumentView() {
				int selectedRow = getSelectedRow();
				if (selectedRow == -1) {
					previousDocID = -1;
					textPanel.setDocumentText("");
					Dna.dna.gui.menuBar.removeDocumentButton.setEnabled(false);
					Dna.dna.gui.leftPanel.editDocPanel.createEditDocumentPanel();
					Dna.dna.gui.leftPanel.editDocPanel.updateUI();
				} else {
					int id = documentPanel.documentContainer.get(selectedRow).getId();
					previousDocID = id;
					Document document = documentContainer.getDocumentByID(id);
					
					String text = document.getText();
					textPanel.setDocumentId(id);
					textPanel.setDocumentText(text);
					textPanel.setEnabled(true);
					Dna.dna.gui.menuBar.removeDocumentButton.setEnabled(true);
					
					//SK
					Dna.dna.gui.leftPanel.editDocPanel.createEditDocumentPanel(documentPanel.documentContainer.get(selectedRow));
					
					boolean[] b = Dna.data.getActiveDocumentPermissions(id);
					if (b[0] == true && b[1] == true) {
						Dna.dna.gui.leftPanel.editDocPanel.createEditDocumentPanel(Dna.data.getDocuments().get(selectedRow));
					} else {
						Dna.dna.gui.leftPanel.editDocPanel.createEditDocumentPanel();
					}
					Dna.dna.gui.leftPanel.editDocPanel.updateUI();
				}
				if (Dna.dna.gui.rightPanel.statementFilter.showCurrent.isSelected()) {
					Dna.dna.gui.rightPanel.statementFilter.documentFilter();
				}
				
				if (Dna.data.getSettings().get("filename") != null) {
					textPanel.paintStatements();
				}
				textPanel.setCaretPosition(0);
			}
		}
	}

	class MenuBar extends JMenuBar {
		JMenu fileMenu, documentMenu, exportMenu, settingsMenu;
		JMenuItem closeDatabase, newDatabase, openDatabase, importHTMLButton, typeEditorButton, newDocumentButton, 
			removeDocumentButton, importDnaButton, importOldButton, networkButton, aboutButton, colorStatementTypeButton, 
			colorCoderButton, recodeVariableButton;
		
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
			recodeVariableButton.setEnabled(false);
			*/
			
			//Document menu: delete selected document
			Icon removeDocumentIcon = new ImageIcon(getClass().getResource("/icons/table_delete.png"));
			removeDocumentButton = new JMenuItem("Delete selected document", removeDocumentIcon);
			removeDocumentButton.setToolTipText( "delete selected document" );
			documentMenu.add(removeDocumentButton);
			removeDocumentButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int dialog = JOptionPane.showConfirmDialog(Dna.dna.gui, 
							"Are you sure you want to delete the selected document (including all statements)?", 
									"Confirmation required", JOptionPane.YES_NO_OPTION);
					int row = Dna.dna.gui.documentPanel.documentTable.getSelectedRow();
					if (row != -1 && dialog == 0) {
						int documentId = Dna.data.getDocuments().get(row).getId();
						Dna.dna.removeDocument(documentId);
					}
				}
			});
			removeDocumentButton.setEnabled(true);

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
			importOldButton.setToolTipText("import from old DNA < 2.0 XML dataset...");
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
						new ImportOldDNA(dbfile);
					}
				}
			});
			importOldButton.setEnabled(false);

			//Export menu: network export
			Icon networkIcon = new ImageIcon(getClass().getResource("/icons/chart_organisation.png"));
			networkButton = new JMenuItem("Export network...", networkIcon);
			networkButton.setToolTipText( "export a network file..." );
			exportMenu.add(networkButton);
			networkButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new NetworkExporter();
				}
			});
			networkButton.setEnabled(false);
			
			//Settings menu: toggle search bar
			/*
			Icon bottomBarIcon = new ImageIcon(getClass().getResource("/icons/application_form_magnify.png"));
			toggleBottomButton = new JMenuItem("Toggle Search Window (show/hide)", bottomBarIcon); 
			settingsMenu.add(toggleBottomButton);
			toggleBottomButton.setEnabled(false);
			*/
			
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
					Dna.dna.gui.rightPanel.statementTable.updateUI();
					Dna.dna.gui.textPanel.paintStatements();
				}
			});
			colorCoderButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Dna.data.getSettings().put("statementColor", "coder");
					Dna.dna.sql.upsertSetting("statementColor", "coder");
					colorStatementTypeButton.setIcon(null);
					colorCoderButton.setIcon(tickIcon);
					Dna.dna.gui.rightPanel.statementTable.updateUI();
					Dna.dna.gui.textPanel.paintStatements();
				}
			});
			settingsMenu.addSeparator();
			colorStatementTypeButton.setEnabled(false);
			colorCoderButton.setEnabled(false);
			
			//Settings menu: about DNA
			Icon aboutIcon = new ImageIcon(getClass().getResource("/icons/dna16.png"));
			aboutButton = new JMenuItem("About DNA...", aboutIcon);
			aboutButton.setToolTipText( "show information about the Discourse Network Analyzer..." );
			settingsMenu.add(aboutButton);
			aboutButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new AboutWindow(Dna.data.getSettings().get("version"), Dna.data.getSettings().get("date"));
				}
			});
		}
	}
}