package dna;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.bounce.text.xml.XMLEditorKit;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;


/**
 * This is the main component of the Discourse Network Analyzer.
 * It instantiates the DNA coder.
 * 
 * @author Philip Leifeld
 * @version 1.0 - 16 April 2009
 */
public class Dna extends JFrame {

	//variables for the parser method
	ArrayList<String> catListe = new ArrayList<String>();
	ArrayList<String> persListe = new ArrayList<String>();
	ArrayList<String> orgListe = new ArrayList<String>();
	String[] agliste = new String[] {"yes", "no"};
	File dnaTempFolder;

	JPopupMenu popmen = new JPopupMenu(); //context menu 
	JMenuItem menu1 = new JMenuItem( "Format as statement"); //menu item 1
	JMenuItem menu2 = new JMenuItem( "Remove statement tag"); //menu item 2

	JLabel currentFileLabel;
	String currentFileName;

	JEditorPane textWindow = new JEditorPane();
	JScrollPane textScrollPane;

	JTable table;
	DefaultTableModel tabModel;
	JScrollPane tableScrollPane;

	JPanel statusBar;
	JToolBar toolbar;

	//Data model for interaction between table and text window
	HashMap<String, String> map = new HashMap<String, String>();

	/**
	 * This constructor opens up the main window and executes a
	 * number of layout components.
	 */
	public Dna() {
		Container c = getContentPane();
		this.setTitle("Discourse Network Analyzer");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
		this.setIconImage(dna32Icon.getImage());

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitSave();
			}
		});

		buttons();
		articleTable();
		textArea();
		status();
		contextMenu();

		//layout of the main window
		JPanel codingPanel = new JPanel(new BorderLayout());
		JSplitPane codingSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
				tableScrollPane, textScrollPane);
		codingPanel.add(toolbar, BorderLayout.NORTH);
		codingPanel.add(codingSplitPane, BorderLayout.CENTER);
		codingPanel.add(statusBar, BorderLayout.SOUTH);
		c.add(codingPanel);

		//pack the window, set its location, and make it visible
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * Status bar.
	 */
	private void status() {
		statusBar = new JPanel( new BorderLayout() );
		currentFileLabel = new JLabel("Current file: none");
		statusBar.add(currentFileLabel, BorderLayout.WEST);
		currentFileName = new String("");
	}

	/**
	 * This method asks whether the current dna file should be
	 * saved and then exits the application.
	 */
	private void exitSave() {
		if (table.getRowCount() == 0) {
			System.exit(0);
		} else {
			int question = JOptionPane.showConfirmDialog(Dna.
					this, "Would you like to save your work?", 
					"Save?", JOptionPane.YES_NO_CANCEL_OPTION);
			if ( question == JOptionPane.YES_OPTION ) {
				if ( currentFileName.equals("") ) {
					saveAsDialog();
				} else {
					saveDnaFile(currentFileName);
				}
				System.exit(0);
			} else if ( question == JOptionPane.NO_OPTION ) {
				System.exit(0);
			} else {
				System.out.println("Cancelled.");
			}
		}
	}

	/**
	 * Open a dna file.
	 */
	private void openDnaFile( String filename ) {
		try {
			tabModel.setRowCount(0);
			SAXBuilder builder = new SAXBuilder( false );
			Document doc = builder.build( new File( filename ) );
			Element discourse = doc.getRootElement();
			List artikelliste = discourse.getChildren("article");
			for (int i = 0; i < artikelliste.size(); i = i + 1) {
				Element artikel = (Element) artikelliste.get(i);
				List artikelinhalt = artikel.getContent();

				String artikeltext = new String("");
				for (Iterator j = artikelinhalt.iterator(); j.hasNext( ); ) {
					Object o = j.next( );
					if (o instanceof Text) {
						artikeltext = artikeltext + ((Text)o).getText();
					} else if (o instanceof CDATA) {
						artikeltext = artikeltext + ((CDATA)o).getText();
					} else if (o instanceof Comment) {
						artikeltext = artikeltext + ((Comment)o).getText();
					} else if (o instanceof ProcessingInstruction) {
						artikeltext = artikeltext + (ProcessingInstruction)o;
					} else if (o instanceof EntityRef) {
						artikeltext = artikeltext + (EntityRef)o;
					} else if (o instanceof Element) {
						artikeltext = artikeltext + "<" + ((Element)o).getName()
						+ " person=\"" + ((Element)o).getAttributeValue("person") + "\" organization=\""
						+ ((Element)o).getAttributeValue("organization") + "\" category=\""
						+ ((Element)o).getAttributeValue("category") + "\" agreement=\""
						+ ((Element)o).getAttributeValue("agreement") + "\">"
						+ ((Element)o).getText() + "</" + ((Element)o).getName() + ">";
					}
				}
				String artikeltitel = artikel.getAttributeValue("title");
				String day = artikel.getAttributeValue("day");
				String month = artikel.getAttributeValue("month");
				String year = artikel.getAttributeValue("year");               
				map.put(artikeltitel, artikeltext);
				Object[] tmp = {artikeltitel,day,month,year};
				tabModel.addRow(tmp);
			}
		} catch (IOException e) {
			System.out.println("Error while reading the file \"" + filename + "\".");
			JOptionPane.showMessageDialog(Dna.this, "Error while reading the file!");
		} catch (org.jdom.JDOMException e) {
			System.out.println("Error while opening the file \"" + filename + "\": " + e.getMessage());
			JOptionPane.showMessageDialog(Dna.this, "Error while opening the file!");
		}
	}

	/**
	 * Save a dna file.
	 */
	private void saveDnaFile(String filename) {
		int i;
		Element root = new Element("discourse");
		Document document = new Document(root);
		for(i=0; i<table.getRowCount(); i=i+1) {
			Element article = new Element("article");
			Attribute title = new Attribute("title", (String) table.getValueAt(i,0).toString());
			Attribute day = new Attribute("day", (String) table.getValueAt(i,1).toString());
			Attribute month = new Attribute("month", (String) table.getValueAt(i,2).toString());
			Attribute year = new Attribute("year", (String) table.getValueAt(i,3).toString());
			article.setAttribute(title);
			article.setAttribute(day);
			article.setAttribute(month);
			article.setAttribute(year);
			article.setText(map.get((String) table.getValueAt(i,0).toString()));
			root.addContent(article);
		}
		File dnaFile = new File (filename);
		try {
			FileOutputStream outStream = new FileOutputStream(dnaFile);
			XMLOutputter outToFile = new XMLOutputter();
			Format format = Format.getPrettyFormat();
			outToFile.setFormat(format);
			outToFile.output(document, outStream);
			outStream.flush();
			outStream.close();
		} catch (IOException e) {
			System.out.println("Cannot save \"" + dnaFile + "\":" + e.getMessage());
			JOptionPane.showMessageDialog(Dna.this, "Error while saving the file!");
		}
		new DnaReformatXml( filename );
		System.out.println("The file \"" + filename + "\" has been saved.");
	}

	/**
	 * Save-as file dialog.
	 */
	private void saveAsDialog() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".dna") 
				|| f.isDirectory();
			}
			public String getDescription() {
				return "Discourse Network files (*.dna)";
			}
		});

		int returnVal = fc.showSaveDialog(Dna.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String filename = new String(file.getPath());
			if ( !file.getPath().endsWith(".dna") ) {
				filename = filename + ".dna";
			}
			if ( file.getPath().endsWith(".dna") ) {
				currentFileName = file.getPath();
			} else {
				currentFileName = file.getPath() + ".dna";
			}
			saveDnaFile(currentFileName);
			currentFileLabel.setText( "Current file: " + currentFileName );
		} else {
			System.out.println("Saving cancelled.");
		}
	}

	/**
	 * Close the dna file that is currently open.
	 */
	private void closeDnaFile() {
		if (table.getRowCount() == 0) {
			map.clear();
			currentFileName = "";
			currentFileLabel.setText("Current file: none");
		} else {
			int question = JOptionPane.showConfirmDialog(Dna.this, 
					"Would you like to save your work?", "Save?", JOptionPane.
					YES_NO_CANCEL_OPTION);
			if ( question == JOptionPane.YES_OPTION ) {
				if ( currentFileName.equals("") ) {
					saveAsDialog();
				} else {
					saveDnaFile( currentFileName );
				}
				clearSpace();
			} else if ( question == JOptionPane.NO_OPTION ) {
				clearSpace();
			} else {
				System.out.println("Cancelled closing file.");
			}
		}
	}

	/**
	 * Helper method for closeDnaFile(). Clears the table and map.
	 */
	private void clearSpace() {
		while (table.getRowCount() > 0) {
			((DefaultTableModel) table.getModel()).removeRow(0);
		}
		map.clear();
		currentFileName = "";
		currentFileLabel.setText("Current file: none");
		System.out.println("File was closed.");
	}

	/**
	 * Table for title and date of article entries. Interacts with the text window.
	 */
	private void articleTable() {
		//create table model
		String[] columnNames = {"title","day", "month", "year"};
		Object[][] data = { };
		tabModel = new DefaultTableModel(data, columnNames);

		//create table and scroll pane
		table = new JTable(tabModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableScrollPane = new JScrollPane(table);
		tableScrollPane.setPreferredSize(new Dimension(200, 200));

		table.getColumnModel().getColumn( 0 ).setPreferredWidth( 650 );
		table.getColumnModel().getColumn( 1 ).setPreferredWidth( 50 );
		table.getColumnModel().getColumn( 2 ).setPreferredWidth( 50 );
		table.getColumnModel().getColumn( 3 ).setPreferredWidth( 50 );
		table.setRowHeight( table.getRowHeight() + 5 );
		table.getTableHeader().setReorderingAllowed( false );

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>();
		table.setRowSorter( sorter );
		sorter.setModel( tabModel );
		sorter.setMaxSortKeys( 3 );

		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		//ActionListener for interaction with the text window
		table.getSelectionModel().addListSelectionListener(new 
				ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;
				int SelectedRow = table.getSelectedRow();
				if (table.getSelectedRow() == -1) {
					textWindow.setText("");
				} else {
					textWindow.setText(map.get((String) table.
							getValueAt(SelectedRow,0).toString()));
					textWindow.setEnabled(true);
				}
			}
		});
	}

	/**
	 * Text area with scroll bar and XML editor kit. Interacts with the table.
	 */
	private void textArea() {
		//XML syntax highlighting with JSyntaxPane
//		DefaultSyntaxKit.initKit();
//		textWindow.setContentType("text/xml");

		//XML syntax highlighting using the Bounce package
		XMLEditorKit kit = new XMLEditorKit(true);
		kit.setWrapStyleWord(true);

		textWindow.setEditorKit( kit);
		textWindow.setFont(new Font("Courier", Font.PLAIN, 13));

		//create scroll pane
		textScrollPane = new JScrollPane(textWindow);
		textScrollPane.setPreferredSize(new Dimension(800, 400));
//		textScrollPane.setRowHeaderView(new LineNumberMargin(textfenster)); //line numbers
		textWindow.setEnabled(false);

		//ActionListener for interaction with the table
		textWindow.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				textToString();
			}
			public void removeUpdate(DocumentEvent e) {
				textToString();
			}
			public void changedUpdate(DocumentEvent e) {
				textToString();
			}
			public void textToString() {
				int SelectedRow = table.getSelectedRow();
				if (SelectedRow == -1) {
					System.out.println("Please select a row before inserting text!");
					JOptionPane.showMessageDialog(Dna.this, 
							"Please select a row before inserting text!");
				} else {
					if ( (String) table.getValueAt(SelectedRow,0).toString()
							== "" && ! textWindow.getText().equals("") ) {
						System.out.println("Please enter a title before inserting text!");
						JOptionPane.showMessageDialog(Dna.this,
						"Please enter a title before inserting text!");
					} else {
						map.put((String) table.getValueAt(SelectedRow,0).
								toString(), textWindow.getText());
					}
				}
			}
		});
	}

	/**
	 * Toolbar with buttons.
	 */
	private void buttons() {
		toolbar = new JToolBar("Toolbar"); //create toolbar

		//Button: Open .dna file
		Icon openIcon = new ImageIcon(getClass().getResource(
		"/icons/Open24.gif"));
		JButton xmlOpen = new JButton(openIcon);
		xmlOpen.setToolTipText( "open DNA file..." );
		toolbar.add(xmlOpen);
		xmlOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				closeDnaFile();

				//File filter
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".dna") 
						|| f.isDirectory();
					}
					public String getDescription() {
						return "Discourse Network files (*.dna)";
					}
				});

				int returnVal = fc.showOpenDialog(Dna.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					openDnaFile(file.getPath());
//					System.out.println("The file \"" + file.getPath() 
//					+ "\" has been opened.");
					currentFileName = file.getPath();
					currentFileLabel.setText( "Current file: " + currentFileName );
				} else {
//					System.out.println("Loading cancelled.");
				}
			}
		});

		//Button: save as...
		Icon saveAsIcon = new ImageIcon(getClass().getResource(
		"/icons/SaveAs24.gif"));
		JButton saveAs = new JButton(saveAsIcon);
		saveAs.setToolTipText( "save as DNA file..." );
		toolbar.add(saveAs);
		saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsDialog();
			}
		});

		//Save button
		Icon saveIcon = new ImageIcon(getClass().getResource(
		"/icons/Save24.gif"));
		JButton save = new JButton(saveIcon);
		save.setToolTipText( "save current file" );
		toolbar.add(save);
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ( currentFileName.equals("") ) {
					saveAsDialog();                
				} else {
					saveDnaFile(currentFileName);
				}
			}
		});

		//Button: close current file
		Icon closeIcon = new ImageIcon( getClass().getResource(
		"/icons/Delete24.gif") );
		JButton closeFile = new JButton(closeIcon);
		closeFile.setToolTipText( "close current file" );
		toolbar.add(closeFile);
		closeFile.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDnaFile();
			}
		});

		//Button: create new row
		Icon newRowIcon = new ImageIcon(getClass().getResource(
		"/icons/RowInsertAfter24.gif"));
		JButton cmdAdd = new JButton(newRowIcon);
		cmdAdd.setToolTipText( "create new row" );
		toolbar.add(cmdAdd);
		cmdAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] tmp = {"","","",""};
				tabModel.addRow(tmp);
			}
		});

		//Button: delete selected row
		Icon deleteRowIcon = new ImageIcon(getClass().getResource(
		"/icons/RowDelete24.gif"));
		JButton cmdDelete = new JButton(deleteRowIcon);
		cmdDelete.setToolTipText( "delete selected row" );
		toolbar.add(cmdDelete);
		cmdDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRow() != -1) {
					int zeile;
					zeile = table.convertRowIndexToModel(table.getSelectedRow());
					tabModel.removeRow(zeile);
					textWindow.setEnabled(false);
				}
			}
		});

		//Button: export
		Icon expIcon = new ImageIcon(getClass().getResource(
				"/icons/Export24.gif"));
		JButton expButton = new JButton(expIcon);
		expButton.setToolTipText( "export to network..." );
		toolbar.add(expButton);
		expButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentFileName.equals("")) {
					System.out.println("No file to export! Please save your data first.");
					JOptionPane.showMessageDialog(Dna.this,
					"No file to export! Please save your data first.");
				} else {
					saveDnaFile(currentFileName);
					new DnaExport( currentFileName );
				}
			}
		});

		//button: about DNA
		Icon aboutIcon = new ImageIcon(getClass().getResource(
		"/icons/About24.gif"));
		JButton aboutButton = new JButton(aboutIcon);
		aboutButton.setToolTipText( "about DNA" );
		toolbar.add(aboutButton);
		aboutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new DnaAbout();
			}
		});
	}

	/**
	 * Parse the contents of a dna file into some ArrayLists that are
	 * used for the insert statement context menu dialog box.
	 */
	private void parseDnaFile( String infile ) {
		persListe.clear();
		orgListe.clear();
		catListe.clear();

		DnaParseXml parseFile = new DnaParseXml( infile );

		persListe.add("");
		for (int i = 0; i < parseFile.st_pers.size(); i = i + 1) {
			if (persListe.contains(parseFile.st_pers.get(i))) {
				//exists already
			} else {
				persListe.add(parseFile.st_pers.get(i));
			}
		}
		Collections.sort( persListe );
		catListe.add("");
		for (int i = 0; i < parseFile.st_cat.size(); i = i + 1) {
			if (catListe.contains(parseFile.st_cat.get(i))) {
				//exists already
			} else {
				catListe.add(parseFile.st_cat.get(i));
			}
		}
		Collections.sort( catListe );
		orgListe.add("");
		for (int i = 0; i < parseFile.st_org.size(); i = i + 1) {
			if (orgListe.contains(parseFile.st_org.get(i))) {
				//exists already
			} else {
				orgListe.add(parseFile.st_org.get(i));
			}
		}
		Collections.sort( orgListe );
	}

	/**
	 * Mouse listener that opens the context menu.
	 */
	private void mouseListen( MouseEvent me ) {
		if ( me.isPopupTrigger() ) {
			if (textWindow.getSelectedText() == null ) {
				menu1.setEnabled(false);
				popmen.show( me.getComponent(), me.getX(), me.getY() );
			} else {
				menu1.setEnabled(true);
				popmen.show( me.getComponent(), me.getX(), me.getY() );
			}
			String text = new String(textWindow.getText());
			int s_cur = textWindow.getCaretPosition();
			int s_beg = text.lastIndexOf("<s", s_cur);
			int s_end = text.indexOf("t>", s_cur) + 2;
			int s_next = text.indexOf("<s", s_cur);
			int s_prev;
			if ( text.lastIndexOf("t>", s_cur) == -1 ) { s_prev = -1; }
			else { s_prev = text.lastIndexOf("t>", s_cur) + 2; }
			if ( textWindow.getSelectedText() != null ) {
				menu2.setEnabled(false);
			} else if ( s_end == -1 ) { 
				menu2.setEnabled(false);
			} else if ( s_beg == -1 ) {
				menu2.setEnabled(false);
			} else if ( s_beg < s_prev ) {
				menu2.setEnabled(false);
			} else if ( s_end > s_next && s_next != -1 ) {
				menu2.setEnabled(false);
			} else {
				menu2.setEnabled(true);
			}
		}
	}

	/**
	 * Context menu with two items. The first opens a dialog window
	 * where organization, person, category and agreement can be
	 * selected from lists. The lists are generated on the fly from
	 * the open dna document. For this purpose, the file is saved
	 * to a temporary directory first and then parsed again.
	 * Alternatively, new items can be inserted. The second context
	 * menu item allows the detection of statement tags in the text
	 * and removes the statement that is located around the current
	 * mouse cursor position.
	 */
	private void contextMenu() {
		//create context menu items
		popmen.add( menu1 );
		menu1.setEnabled(false);
		popmen.add( menu2 );

		//MouseListener for the text window; one method for Windows and one for Unix
		textWindow.addMouseListener( new MouseAdapter() {
			public void mouseReleased( MouseEvent me ) {
				mouseListen(me);
			}
			public void mousePressed( MouseEvent me ) {
				mouseListen(me);
			}
		});

		//Item: Insert statement
		menu1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String currentFolder = System.getProperty("user.dir");
				File dnaTempFolder = new File(currentFolder + "/.dna");
				dnaTempFolder.mkdirs();

				saveDnaFile(dnaTempFolder + "/temp.dna");
				parseDnaFile(dnaTempFolder + "/temp.dna");

				String selection = new String(textWindow.getSelectedText());
				JComboBox person = new JComboBox(persListe.toArray());
				person.setEditable(true);
				JComboBox organization = new JComboBox(orgListe.toArray());
				organization.setEditable(true);
				JComboBox category = new JComboBox(catListe.toArray());
				category.setEditable(true);
				JComboBox agreement = new JComboBox(agliste);
				agreement.setEditable(false);
				Object[] message = {"Person", person, "Organization", 
						organization, "Category", category, "Agreement", agreement};
				JOptionPane messagepane = new JOptionPane(message, 
						JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
				messagepane.createDialog(null, "Statement details").setVisible(true);
				int result = 1;
				try {
					result = ((Integer)messagepane.getValue()).intValue();
				} catch (Exception uninitializedValue) {
					System.out.println("Insert statement dialog was closed.");
				}
				if ( result > 1 ) {
					System.out.println("Cancelled inserting statement.");
				} else {
					String popperson = new String("");
					String popcategory = new String("");
					String poporganization = new String("");
					String popagreement = new String("");
					if ( result == JOptionPane.OK_OPTION ) {
						if (person.getSelectedIndex() > -1) {
							popperson = (String)person.getSelectedItem();
						} else {
							popperson = person.getEditor().getItem().toString();
						}
						if (organization.getSelectedIndex() > -1) {
							poporganization = (String)organization.getSelectedItem();
						} else {
							poporganization = organization.getEditor().getItem().toString();
						}
						if (category.getSelectedIndex() > -1) {
							popcategory = (String)category.getSelectedItem();
						} else {
							popcategory = category.getEditor().getItem().toString();
						}
						popagreement = (String)agreement.getSelectedItem();
						textWindow.replaceSelection("<statement person=\"" + popperson 
								+ "\" organization=\"" + poporganization + "\" category=\""
								+ popcategory + "\" agreement=\"" + popagreement + "\">" 
								+ selection + "</statement>");
					} else {
						System.out.println("Cancelled inserting statement.");
					}
				}
			}
		});


		//Item: Statement-Formatierung entfernen
		menu2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = new String(textWindow.getText());
				int s_cur = textWindow.getCaretPosition();
				int s_beg = text.lastIndexOf("<s", s_cur);
				int s_end = text.indexOf("t>", s_cur) + 2;
				int t_beg = text.indexOf("\">", s_beg) + 2;
				int t_end = text.lastIndexOf("</", s_end);
				int t_length = t_end - t_beg;
				try {
					String textContents = new String(textWindow.getText(t_beg, t_length));
					textWindow.select(s_beg, s_end);
					textWindow.replaceSelection(textContents);
				} catch (javax.swing.text.BadLocationException ble) {
					System.out.println("Bad Location Exception.");
				}
			}
		});
	}

	/**
	 * Main method. Instantiates the application.
	 */
	public static void main (String[] args) {
		new Dna();
	}
}