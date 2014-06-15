package dna;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
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
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
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
			documentTable.getColumnModel().getColumn(0).setPreferredWidth(700);
			documentTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		}
		
		public class DocumentTable extends JTable {
			
			private static final long serialVersionUID = 1L;

			public DocumentTable() {
				setModel(new DocumentContainer());
				setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				getTableHeader().setReorderingAllowed( false );
				putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
				
				getSelectionModel().addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							return;
						}
						int selectedRow = getSelectedRow();
						if (selectedRow == -1) {
							textPanel.setDocumentText("");
						} else {
							int id = documentPanel.documentContainer.
									get(selectedRow).getId();
							Document document = Dna.dna.db.getDocument(id);
							String text = document.getText();
							textPanel.setDocumentId(id);
					    	textPanel.setDocumentText(text);
					    	textPanel.setEnabled(true);
						}
						//if (statementFilter.showCurrent.isSelected()) {
						//	statementFilter.articleFilter();
						//}
						
						if (Dna.dna.db.getFileName() != null) {
							textPanel.paintStatements();
						}
						textPanel.setCaretPosition( 0 );
					}
				});
			}
			
			/*
			public void rebuildTable() {
				documentTable.clearSelection();
				documentContainer.clear();
				ArrayList<Document> documents = new SqlQuery(dbfile).getArticles();
				for (int i = 0; i < articles.size(); i++) {
					articleContainer.addArticle(articles.get(i));
				}
			}
			*/
		}
	}

	class MenuBar extends JMenuBar {
		
		private static final long serialVersionUID = 1L;
		
		JMenu fileMenu,articleMenu,settingsMenu;
		JMenuItem newArticleButton,importOldButton,typeEditorButton;
		
		public MenuBar() {
			fileMenu = new JMenu("File");
			this.add(fileMenu);
			articleMenu = new JMenu("Article");
			this.add(articleMenu);
			settingsMenu = new JMenu("Settings");
			this.add(settingsMenu);
			
			//File menu: new DNA database file...
			Icon databaseIcon = new ImageIcon(getClass().getResource(
					"/icons/database.png"));
			JMenuItem newDatabase = new JMenuItem("New database...", 
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
					//reloadDb();
				}
			});
			
			//File menu: open DNA file
			Icon openIcon = new ImageIcon(getClass().getResource(
					"/icons/folder.png"));
			JMenuItem openMenuItem = new JMenuItem("Open database...", 
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
					//reloadDb();
				}
			});
			fileMenu.add(openMenuItem);

			//File menu: close current database file
			Icon closeIcon = new ImageIcon( getClass().getResource(
					"/icons/cancel.png") );
			JMenuItem closeFile = new JMenuItem("Close database", closeIcon);
			closeFile.setToolTipText( "close current database file" );
			fileMenu.add(closeFile);
			closeFile.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Dna.dna.closeFile();
				}
			});

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
			
			//Article menu: add new article
			Icon newArticleIcon = new ImageIcon(getClass().getResource(
					"/icons/table_add.png"));
			newArticleButton = new JMenuItem("Add new article...", 
					newArticleIcon);
			newArticleButton.setToolTipText( "add new article..." );
			articleMenu.add(newArticleButton);
			newArticleButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new NewDocumentWindow();
				}
			});

			/*
			//Article menu: import old DNA dataset
			Icon importOldIcon = new ImageIcon(getClass().getResource(
					"/icons/table_add.png"));
			importOldButton = new JMenuItem("Import from old DNA file...", 
					importOldIcon);
			importOldButton.setToolTipText( "import from old DNA < 2.0 dataset..." );
			articleMenu.add(importOldButton);
			importOldButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new ImportOldDna();
				}
			});
			*/
			
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

		}
	}
	
}
