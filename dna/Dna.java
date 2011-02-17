package dna;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.filechooser.*;

//import org.jdom.Attribute;
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
 * improvements in this version:
 * - new custom option for the co-occurrence and the affiliation algorithm: duplicates are not ignored anymore if checked
 * - include isolates option now also for the affiliation algorithm
 * - bugfix: edge weights now work properly in the graphML format 
 * 
 * @author Philip Leifeld
 * @version 1.14 - 30 November 2009
 */
public class Dna extends JFrame {

	Container c;
	
	JTextField titleField;
	
	ArrayList<String> catListe = new ArrayList<String>();
	ArrayList<String> persListe = new ArrayList<String>();
	ArrayList<String> orgListe = new ArrayList<String>();
	String[] agListe = new String[] {"yes", "no"};

	JPopupMenu popmen = new JPopupMenu();
	ImageIcon addStatementIcon = new ImageIcon(getClass().getResource("/icons/comment_edit.png"));
	JMenuItem menu1 = new JMenuItem( "Format as statement", addStatementIcon);

	JLabel currentFileLabel;
	String currentFileName;
	JLabel loading;
	
	JEditorPane textWindow = new JEditorPane();
	JScrollPane textScrollPane;
	HTMLEditorKit kit;
	HTMLDocument doc;
	
	JTable articleTable;
	ArticleContainer ac;
	JScrollPane tableScrollPane;
	TableRowSorter<StatementContainer> sorter;

	JPanel statusBar;
	JPanel toolbarPanel;
	JToolBar toolbar;
	
	StatementContainer sc;
	JTable statementTable;
	JScrollPane statementTableScrollPane;
	JPanel statementPanel;
	JTextField textField, personField, organizationField, categoryField, idField;
	JRadioButton textButton, allButton, articleButton, personButton, organizationButton, categoryButton, idButton;
	JCheckBox editMode;
	
	String strippedContents;

	/**
	 * This constructor opens up the main window and executes a
	 * number of layout components.
	 */
	public Dna() {
		c = getContentPane();
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
		statementTable();
		
		//layout of the main window
		JPanel codingPanel = new JPanel(new BorderLayout());
		JSplitPane codingSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, textScrollPane);
		codingPanel.add(toolbarPanel, BorderLayout.WEST);
		JSplitPane statementSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, codingSplitPane, statementPanel);
		codingPanel.add(statementSplitPane, BorderLayout.CENTER);
		codingPanel.add(statusBar, BorderLayout.SOUTH);
		c.add(codingPanel);
		
		//pack the window, set its location, and make it visible
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public class Recode extends JFrame {
		
		JRadioButton renameRadio, removeRadio, persRadio, orgRadio, catRadio;
		JComboBox persCombo, orgCombo, catCombo;
		JTextField textField;
		Container c;
		
		public Recode() {
			c = getContentPane();
			this.setTitle("Recode statements");
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			ImageIcon recodeIcon = new ImageIcon(getClass().getResource("/icons/database_go.png"));
			this.setIconImage(recodeIcon.getImage());
			
			updateLists();
			
			ButtonGroup bg1 = new ButtonGroup();
			renameRadio = new JRadioButton("rename into", true);
			removeRadio = new JRadioButton("remove", false);
			bg1.add(renameRadio);
			bg1.add(removeRadio);
			
			ButtonGroup bg2 = new ButtonGroup();
			persRadio = new JRadioButton("person: ", true);
			orgRadio = new JRadioButton("organization: ", false);
			catRadio = new JRadioButton("category: ", false);
			bg2.add(persRadio);
			bg2.add(orgRadio);
			bg2.add(catRadio);
			
			textField = new JTextField(20);
			
			persCombo = new JComboBox(persListe.toArray());
			orgCombo = new JComboBox(orgListe.toArray());
			catCombo = new JComboBox(catListe.toArray());
			
			Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
			JButton okButton = new JButton("OK", okIcon);
			Icon cancelIcon = new ImageIcon(getClass().getResource("/icons/cancel.png"));
			JButton cancelButton = new JButton("cancel", cancelIcon);
			
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int counter = 0;
					
					String item = "";
					String type = ""; 
					String operation = "";
					String into = "";
					if (persRadio.isSelected()) {
						type = "person";
						item = (String)persCombo.getSelectedItem();
					} else if (orgRadio.isSelected()) {
						type = "organization";
						item = (String)orgCombo.getSelectedItem();
					} else if (catRadio.isSelected()) {
						type = "category";
						item = (String)catCombo.getSelectedItem();
					}
					if (removeRadio.isSelected()) {
						operation = "remove";
					} else if (renameRadio.isSelected()) {
						operation = "rename";
						into = " into \"" + textField.getText() + "\"";
					}
					
					int dialog = JOptionPane.showConfirmDialog(Recode.this, "Are you sure you want to " + operation + " all statements matching\n" +
							"the " + type + " \"" + item + "\"" + into + "?", "Confirmation required", JOptionPane.OK_CANCEL_OPTION);
					
					if (dialog == 0) {
						for (int i = sc.size() - 1; i >= 0; i--) {
							if ( persRadio.isSelected() && removeRadio.isSelected() && persCombo.getSelectedItem().equals(sc.get(i).getPerson())
									|| orgRadio.isSelected() && removeRadio.isSelected() && orgCombo.getSelectedItem().equals(sc.get(i).getOrganization())
									|| catRadio.isSelected() && removeRadio.isSelected() && catCombo.getSelectedItem().equals(sc.get(i).getCategory()) ) {
								sc.removeStatement(sc.get(i).getId());
								counter++;
							} else if (persRadio.isSelected() && renameRadio.isSelected() && persCombo.getSelectedItem().equals(sc.get(i).getPerson())) {
								sc.get(i).setPerson(textField.getText());
								counter++;
							} else if (orgRadio.isSelected() && renameRadio.isSelected() && orgCombo.getSelectedItem().equals(sc.get(i).getOrganization())) {
								sc.get(i).setOrganization(textField.getText());
								counter++;
							} else if (catRadio.isSelected() && renameRadio.isSelected() && catCombo.getSelectedItem().equals(sc.get(i).getCategory())) {
								sc.get(i).setCategory(textField.getText());
								counter++;
							}
						}
						dispose();
					}
					if (counter > 0) {
						JOptionPane.showMessageDialog(Recode.this, counter + " statements were " + operation + "d.");
					}
				}
			});
			
			JPanel innerSelectionPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(3,0,3,3);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridy = 0;
			gbc.gridx = 0;
			innerSelectionPanel.add(persRadio, gbc);
			gbc.gridx = 1;
			gbc.gridwidth = 2;
			innerSelectionPanel.add(persCombo, gbc);
			gbc.gridwidth = 1;
			gbc.gridy = 1;
			gbc.gridx = 0;
			innerSelectionPanel.add(orgRadio, gbc);
			gbc.gridx = 1;
			gbc.gridwidth = 2;
			innerSelectionPanel.add(orgCombo, gbc);
			gbc.gridwidth = 1;
			gbc.gridy = 2;
			gbc.gridx = 0;
			innerSelectionPanel.add(catRadio, gbc);
			gbc.gridx = 1;
			gbc.gridwidth = 2;
			innerSelectionPanel.add(catCombo, gbc);
			gbc.gridwidth = 1;
			
			JPanel outerSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			outerSelectionPanel.setBorder( new TitledBorder( new EtchedBorder(), "selection" ) );
			outerSelectionPanel.add(innerSelectionPanel);
			
			JPanel operationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			operationPanel.setBorder( new TitledBorder( new EtchedBorder(), "operation" ) );
			operationPanel.add(removeRadio, gbc);
			operationPanel.add(renameRadio, gbc);
			operationPanel.add(textField, gbc);
			
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(cancelButton);
			buttonPanel.add(okButton);
			
			JPanel layout = new JPanel(new BorderLayout());
			layout.add(outerSelectionPanel, BorderLayout.NORTH);
			layout.add(operationPanel, BorderLayout.CENTER);
			layout.add(buttonPanel, BorderLayout.SOUTH);
			c.add(layout);
			
			this.pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}
	}
	
	public class RenameArticle extends JFrame {
		
		SpinnerDateModel dateModel;
		JSpinner dateSpinner;
		JButton okButton;
		Container c;
		Date currentDate;
		String currentTitle;
		int currentRow;
		
		public RenameArticle() {
			c = getContentPane();
			this.setTitle("Change article details");
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			ImageIcon tableEditIcon = new ImageIcon(getClass().getResource("/icons/table_edit.png"));
			this.setIconImage(tableEditIcon.getImage());
			
			currentRow = articleTable.getSelectedRow();
			
			if (currentRow > -1) {
				currentTitle = ac.get(currentRow).getTitle();
				currentDate = ac.get(currentRow).getDate();
				
				JLabel instructions = new JLabel("Please enter a new title and/or date for the current article.");
				
				JLabel titleLabel = new JLabel("Title:", JLabel.TRAILING);
				titleField = new JTextField(40);
				DocumentFilter dfilter = new TextFilter();
				((AbstractDocument)titleField.getDocument()).setDocumentFilter(dfilter);
				titleField.setText(currentTitle);
				
				JLabel dateLabel = new JLabel("Date:", JLabel.TRAILING);
				dateModel = new SpinnerDateModel();
				dateSpinner = new JSpinner();
				dateModel.setCalendarField( Calendar.DAY_OF_YEAR );
				dateSpinner.setModel( dateModel );
				dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd.MM.yyyy"));
				dateSpinner.setValue(currentDate);
				
				Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
				okButton = new JButton("OK", okIcon);
				okButton.setToolTipText( "rename the currently selected article and change its date" );
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						articleTable.setValueAt(titleField.getText(), currentRow, 0);
						articleTable.setValueAt((Date)dateSpinner.getValue(), currentRow, 1);
						articleTable.changeSelection(currentRow, 0, false, false);
						for (int i = 0; i < sc.size(); i++) {
							if (sc.get(i).getArticleTitle().equals(currentTitle)) {
								sc.get(i).setDate((Date)dateSpinner.getValue());
								sc.get(i).setArticleTitle(titleField.getText());
							}
						}
						dispose();
					}
				});
				
				titleField.getDocument().addDocumentListener(new DocumentListener() {
					@Override
					public void changedUpdate(DocumentEvent e) {
						checkButton();
					}
					@Override
					public void insertUpdate(DocumentEvent e) {
						checkButton();
					}
					@Override
					public void removeUpdate(DocumentEvent e) {
						checkButton();
					}
					public void checkButton() {
						String title = titleField.getText();
						boolean duplicate = false;
						if (titleField.getText().equals("")) {
							duplicate = true;
						} else {
							for (int i = 0; i < ac.getRowCount(); i++) {
								if (ac.getValueAt(i, 0).equals(title) && currentRow != i) {
									duplicate = true;
								}
							}
						}
						if (duplicate == true) {
							okButton.setEnabled(false);
						} else {
							okButton.setEnabled(true);
						}
					}
				});
				
				JPanel panel = new JPanel(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(3,3,3,3);
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.gridwidth = 3;
				panel.add(instructions, gbc);
				
				gbc.gridwidth = 1;
				gbc.gridx = 0;
				gbc.gridy = 1;
				panel.add(titleLabel, gbc);
				
				gbc.gridx = 1;
				gbc.gridwidth = 2;
				panel.add(titleField, gbc);
				
				gbc.gridwidth = 1;
				gbc.gridx = 0;
				gbc.gridy = 2;
				panel.add(dateLabel, gbc);
				
				gbc.gridx = 1;
				panel.add(dateSpinner, gbc);
				
				gbc.gridx = 2;
				panel.add(okButton, gbc);
				
				c.add(panel);
				
				this.pack();
				this.setLocationRelativeTo(null);
				this.setVisible(true);
			}
			
		}
	}
	
	private void newArticle() {
		SpinnerDateModel dateModel;
		final JSpinner dateSpinner;
		JScrollPane textScroller;
		final JTextArea textArea;
		final JFrame frame;
		final JButton okButton;
		
		frame = new JFrame("Add new article...");
		Container cont = frame.getContentPane();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon tableAddIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
		frame.setIconImage(tableAddIcon.getImage());
		
		JLabel titleLabel = new JLabel("Title:");
		titleField = new JTextField(40);
		DocumentFilter dfilter = new TextFilter();
		((AbstractDocument)titleField.getDocument()).setDocumentFilter(dfilter);
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		titlePanel.add(titleLabel);
		titlePanel.add(titleField);
		
		JLabel dateLabel = new JLabel("Date:");
		dateModel = new SpinnerDateModel();
		dateSpinner = new JSpinner();
		dateModel.setCalendarField( Calendar.DAY_OF_YEAR );
		dateSpinner.setModel( dateModel );
		dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd.MM.yyyy"));
		JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		datePanel.add(dateLabel);
		datePanel.add(dateSpinner);

		textArea = new JTextArea("(paste the contents of the article here by highlighting this text and replacing it using Ctrl-V)");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		((AbstractDocument)textArea.getDocument()).setDocumentFilter(dfilter);
		textScroller = new JScrollPane(textArea);
		textScroller.setPreferredSize(new Dimension(600, 300));
		textScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		okButton = new JButton("OK", okIcon);
		okButton.setToolTipText( "insert a new article based on the information you entered in this window" );
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textArea.getText();
				text = text.replaceAll("\n", "<br/>");
				Article article = new Article(titleField.getText(), (Date)dateSpinner.getValue(), text);
				ac.addArticle( article );
				
				int index = -1;
				for (int i = 0; i < ac.getRowCount(); i++) {
					if (titleField.getText().equals(ac.get(i).getTitle())) {
						index = i;
					}
				}
				
				articleTable.changeSelection(index, 0, false, false);
				frame.dispose();
			}
		});
		okButton.setEnabled(false);
		
		titleField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkButton();
			}
			public void checkButton() {
				String title = titleField.getText();
				boolean duplicate = false;
				if (titleField.getText().equals("")) {
					duplicate = true;
				} else {
					for (int i = 0; i < ac.getRowCount(); i++) {
						if (ac.getValueAt(i, 0).equals(title)) {
							duplicate = true;
						}
					}
				}
				if (duplicate == true) {
					okButton.setEnabled(false);
				} else {
					okButton.setEnabled(true);
				}
			}
		});
		
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(titlePanel, BorderLayout.WEST);
		northPanel.add(datePanel, BorderLayout.CENTER);
		northPanel.add(okButton, BorderLayout.EAST);
		
		JPanel newArticlePanel = new JPanel(new BorderLayout());
		newArticlePanel.add(northPanel, BorderLayout.NORTH);
		newArticlePanel.add(textScroller, BorderLayout.CENTER);
		
		cont.add(newArticlePanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void idFilter(String regx) {
		final String regex = regx;
		try {
			RowFilter<StatementContainer, Integer> idFilter = new RowFilter<StatementContainer, Integer>() {
				public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
					StatementContainer stcont = entry.getModel();
					Statement st = stcont.get(entry.getIdentifier());
					Pattern p = Pattern.compile(regex);
					Matcher m = p.matcher(new Integer(st.getId()).toString());
					boolean b = m.find();
					if (b == true) {
						return true;
					}
					return false;
				}
			};
			sorter.setRowFilter(idFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	private void personFilter(String regx) {
		final String regex = regx;
		try {
			RowFilter<StatementContainer, Integer> personFilter = new RowFilter<StatementContainer, Integer>() {
				public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
					StatementContainer stcont = entry.getModel();
					Statement st = stcont.get(entry.getIdentifier());
					Pattern p = Pattern.compile(regex);
					Matcher m = p.matcher(st.getPerson());
					boolean b = m.find();
					if (b == true) {
						return true;
					}
					return false;
				}
			};
			sorter.setRowFilter(personFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	private void organizationFilter(String regx) {
		final String regex = regx;
		try {
			RowFilter<StatementContainer, Integer> organizationFilter = new RowFilter<StatementContainer, Integer>() {
    			public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
    				StatementContainer stcont = entry.getModel();
    				Statement st = stcont.get(entry.getIdentifier());
    				Pattern p = Pattern.compile(regex);
    				Matcher m = p.matcher(st.getOrganization());
    				boolean b = m.find();
    				if (b == true) {
    					return true;
    				}
    				return false;
    			}
    		};
    		sorter.setRowFilter(organizationFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	private void categoryFilter(String regx) {
		final String regex = regx;
		try {
			RowFilter<StatementContainer, Integer> categoryFilter = new RowFilter<StatementContainer, Integer>() {
    			public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
    				StatementContainer stcont = entry.getModel();
    				Statement st = stcont.get(entry.getIdentifier());
    				Pattern p = Pattern.compile(regex);
    				Matcher m = p.matcher(st.getCategory());
    				boolean b = m.find();
    				if (b == true) {
    					return true;
    				}
    				return false;
    			}
    		};
    		sorter.setRowFilter(categoryFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	private void textFilter(String regex) {
		try {
			RowFilter<StatementContainer, Object> rf = null;
    		rf = RowFilter.regexFilter(regex, 1);
    		sorter.setRowFilter(rf);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	private void articleFilter() {
		int row = articleTable.getSelectedRow();
		String articleTitle = "";
		if (row > -1) {
			articleTitle = ac.get(row).getTitle();
		}
		final String title = articleTitle;
		try {
			RowFilter<StatementContainer, Integer> articleFilter = new RowFilter<StatementContainer, Integer>() {
    			public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
    				StatementContainer stcont = entry.getModel();
    				Statement st = stcont.get(entry.getIdentifier());
    				if (st.getArticleTitle().equals(title)) {
        				return true;
        			}
    				return false;
    			}
    		};
    		sorter.setRowFilter(articleFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	private void allFilter() {
		try {
			RowFilter<StatementContainer, Object> rf = null;
    		rf = RowFilter.regexFilter("");
    		sorter.setRowFilter(rf);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	private void statementTable() {
		sc = new StatementContainer();
		statementTable = new JTable( sc );
		statementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statementTableScrollPane = new JScrollPane(statementTable);
		statementTableScrollPane.setPreferredSize(new Dimension(200, 200));
		
		statementTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 50 );
		statementTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 180 );
		statementTable.getTableHeader().setReorderingAllowed( false );
		statementTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		sorter = new TableRowSorter<StatementContainer>(sc) {
			public void toggleSortOrder(int i) {
				//leave blank; the overwritten method makes the table unsortable
			}
		};
        statementTable.setRowSorter(sorter);
		
		ButtonGroup statementGroup = new ButtonGroup();
		textButton = new JRadioButton("text:", false);
		personButton = new JRadioButton("person:", false);
		categoryButton = new JRadioButton("category:", false);
		organizationButton = new JRadioButton("organization:", false);
		articleButton = new JRadioButton("show only current article", false);
		allButton = new JRadioButton("show all statements", true);
		idButton = new JRadioButton("statement ID:", false);
		statementGroup.add(textButton);
		statementGroup.add(personButton);
		statementGroup.add(categoryButton);
		statementGroup.add(organizationButton);
		statementGroup.add(articleButton);
		statementGroup.add(allButton);
		statementGroup.add(idButton);
		allButton.setToolTipText("do not use a filter on the statement table");
		articleButton.setToolTipText("filter the statement table by corresponding article title");
		textButton.setToolTipText("use a regular expression as a statement content filter");
		personButton.setToolTipText("use a regular expression to filter by person");
		organizationButton.setToolTipText("use a regular expression to filter by organization");
		categoryButton.setToolTipText("use a regular expression to filter by category");
		idButton.setToolTipText("use a regular expression to filter by ID");
		
		DocumentListener dl = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            	selectFilter();
            }
            public void insertUpdate(DocumentEvent e) {
            	selectFilter();
            }
            public void removeUpdate(DocumentEvent e) {
            	selectFilter();
            }
            private void selectFilter() {
            	if (textField.isFocusOwner()) {
            		textButton.setSelected(true);
            		personField.setText("");
            		organizationField.setText("");
            		categoryField.setText("");
            		idField.setText("");
            		textFilter(textField.getText());
            	} else if (personField.isFocusOwner()) {
            		personButton.setSelected(true);
            		textField.setText("");
            		organizationField.setText("");
            		categoryField.setText("");
            		idField.setText("");
            		personFilter(personField.getText());
            	} else if (organizationField.isFocusOwner()) {
            		organizationButton.setSelected(true);
            		textField.setText("");
            		personField.setText("");
            		categoryField.setText("");
            		idField.setText("");
            		organizationFilter(organizationField.getText());
            	} else if (categoryField.isFocusOwner()) {
            		categoryButton.setSelected(true);
            		textField.setText("");
            		organizationField.setText("");
            		personField.setText("");
            		idField.setText("");
            		categoryFilter(categoryField.getText());
            	} else if (idField.isFocusOwner()) {
            		idButton.setSelected(true);
            		textField.setText("");
            		organizationField.setText("");
            		personField.setText("");
            		categoryField.setText("");
            		idFilter(idField.getText());
            	}
            }
        };
        
        ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == allButton) {
					allFilter();
				} else if (e.getSource() == articleButton) {
					articleFilter();
				}
			}
		};
		
		articleButton.addActionListener(al);
		allButton.addActionListener(al);
        
		textField = new JTextField(10);
        textField.getDocument().addDocumentListener(dl);
        personField = new JTextField(10);
        personField.getDocument().addDocumentListener(dl);
        organizationField = new JTextField(10);
        organizationField.getDocument().addDocumentListener(dl);
        categoryField = new JTextField(10);
        categoryField.getDocument().addDocumentListener(dl);
        idField = new JTextField(10);
        idField.getDocument().addDocumentListener(dl);
        
        editMode = new JCheckBox("edit statements upon click", true);
        
		JPanel sfp = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 2;
		sfp.add(allButton, gbc);
		
		gbc.gridy = 1;
		sfp.add(articleButton, gbc);
		
		gbc.gridwidth = 1;
		gbc.gridy = 2;
		sfp.add(textButton, gbc);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		sfp.add(textField, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		sfp.add(personButton, gbc);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		sfp.add(personField, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		sfp.add(organizationButton, gbc);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		sfp.add(organizationField, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.WEST;
		sfp.add(categoryButton, gbc);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		sfp.add(categoryField, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		sfp.add(idButton, gbc);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		sfp.add(idField, gbc);
		
		gbc.gridy = 7;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		sfp.add(editMode, gbc);
		
		statementPanel = new JPanel(new BorderLayout());
		statementPanel.add(sfp, BorderLayout.SOUTH);
		statementPanel.add(statementTableScrollPane, BorderLayout.CENTER);
		
		statementTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = -1;
				row = statementTable.rowAtPoint(e.getPoint());
				row = statementTable.convertRowIndexToModel(row);
				
				if (row > -1) {
					int id = sc.get(row).getId();
					if (id != -1) {
						String title = sc.getById(id).getArticleTitle();
						int index = -1;
						for (int i = 0; i < ac.getRowCount(); i++) {
							if (ac.get(i).getTitle().equals(title)) {
								index = i;
							}
						}
						int startCaret = sc.getById(id).getStart();
		    			int stopCaret = sc.getById(id).getStop();
		    			articleTable.changeSelection(index, 0, false, false);
		    			textWindow.grabFocus();
		    			textWindow.setSelectionStart(startCaret);
		    			textWindow.setSelectionEnd(stopCaret);
		    			
		    			//show popup
		    			if (editMode.isSelected()) {
		    				new ToolTip(id);
		    			}
					}
				}
			}
		});
	}
	
	/**
	 * Status bar.
	 */
	private void status() {
		statusBar = new JPanel( new BorderLayout() );
		currentFileLabel = new JLabel("Current file: none");
		statusBar.add(currentFileLabel, BorderLayout.WEST);
		currentFileName = new String("");
		
		loading = new JLabel("loading...", JLabel.TRAILING);
		loading.setVisible(false);
		statusBar.add(loading, BorderLayout.EAST);
	}

	/**
	 * This method asks whether the current dna file should be
	 * saved and then exits the application.
	 */
	private void exitSave() {
		if (articleTable.getRowCount() == 0) {
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
				System.out.println("Canceled.");
			}
		}
	}
	
	public String stripHtmlTags(String text, boolean htmlLineSep) {
		String htmlContents = text.replaceAll("<br>", "\\[br\\]"); //JEditorPane converts <br/> internally into <br>
		htmlContents = htmlContents.replaceAll("<br/>", "\\[br\\]");
		htmlContents = htmlContents.replaceAll("<br />", "\\[br\\]");
		htmlContents = htmlContents.replaceAll("\\<.*?>","");
		strippedContents = "";
		
		HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback () {
		    public void handleText(char[] data, int pos) {
		        strippedContents = String.valueOf(data);
		    }
		};
		Reader reader = new StringReader(htmlContents);
		try {
			new ParserDelegator().parse(reader, callback, false);
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
		
		if (htmlLineSep == true) {
			strippedContents = strippedContents.replaceAll("\\[br\\]", "<br/>");
		} else {
			strippedContents = strippedContents.replaceAll("\\[br\\]", "\n");
		}
		
		return strippedContents;
	}

	/**
	 * Save a dna file.
	 */
	private void saveDnaFile(String filename) {
		SimpleDateFormat dfYear = new SimpleDateFormat( "yyyy" );
		SimpleDateFormat dfMonth = new SimpleDateFormat( "MM" );
		SimpleDateFormat dfDay = new SimpleDateFormat( "dd" );
		
		Element root = new Element("discourse");
		Document document = new Document(root);
		
		Element version = new Element("version");
		version.setText("1.09");
		root.addContent(version);
		
		for(int i = 0; i < articleTable.getRowCount(); i++) {
			Element article = new Element("article");
			
			Element text = new Element("text");
			String htmlContents = ac.get(i).getText();
			strippedContents = stripHtmlTags(htmlContents, false);
			text.setText(strippedContents);
			article.addContent(text);
			
			Element date = new Element("date");
			Date dateStamp = ac.get(i).getDate();
			String dateString = dfDay.format( dateStamp ) + "." + dfMonth.format( dateStamp ) + "." + dfYear.format( dateStamp );
			date.setText(dateString);
			article.addContent(date);
			
			Element title = new Element("title");
			String tit = ac.get(i).getTitle();
			title.setText(tit);
			article.addContent(title);
			
			Element statementElements = new Element("statements");
			for (int j = 0; j < sc.statements.size(); j++) {
				if (sc.statements.get(j).articleTitle.equals(tit)) {
					Element statement = new Element("statement");
					
					Element id = new Element("id");
					id.setText(new Integer(sc.get(j).getId()).toString());
					statement.addContent(id);
					
					Element start = new Element("start");
					start.setText(new Integer(sc.statements.get(j).start).toString());
					statement.addContent(start);
					
					Element end = new Element("end");
					end.setText(new Integer(sc.statements.get(j).stop).toString());
					statement.addContent(end);
					
					Element person = new Element("person");
					person.setText(sc.statements.get(j).person);
					statement.addContent(person);
					
					Element organization = new Element("organization");
					organization.setText(sc.statements.get(j).organization);
					statement.addContent(organization);
					
					Element category = new Element("category");
					category.setText(sc.statements.get(j).category);
					statement.addContent(category);
					
					Element agreement = new Element("agreement");
					agreement.setText(sc.statements.get(j).agreement);
					statement.addContent(agreement);
					
					statementElements.addContent(statement);
				}
			}
			article.addContent(statementElements);
			root.addContent(article);
		}
		File dnaFile = new File (filename);
		try {
			FileOutputStream outStream = new FileOutputStream(dnaFile);
			XMLOutputter outToFile = new XMLOutputter();
			Format format = Format.getPrettyFormat();
			format.setEncoding("utf-8");
			outToFile.setFormat(format);
			outToFile.output(document, outStream);
			outStream.flush();
			outStream.close();
		} catch (IOException e) {
			System.out.println("Cannot save \"" + dnaFile + "\":" + e.getMessage());
			JOptionPane.showMessageDialog(Dna.this, "Error while saving the file!");
		}
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
			System.out.println("Saving canceled.");
		}
	}

	/**
	 * Close the dna file that is currently open.
	 */
	private void closeDnaFile() {
		if (articleTable.getRowCount() == 0) {
			clearSpace();
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
				System.out.println("Canceled closing file.");
			}
		}
	}

	/**
	 * Helper method for closeDnaFile(). Clears the article table, statement table and status bar.
	 */
	private void clearSpace() {
		ac.clear();
		sc.clear();
		currentFileName = "";
		currentFileLabel.setText("Current file: none");
		textWindow.setEnabled(false);
		System.out.println("File was closed.");
	}
	
	/**
	 * Table for title and date of article entries. Interacts with the text window.
	 */
	private void articleTable() {
		
		//create table, assign model and create scroll pane
		ac = new ArticleContainer();
		articleTable = new JTable( ac );
		articleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableScrollPane = new JScrollPane(articleTable);
		tableScrollPane.setPreferredSize(new Dimension(600, 100));

		articleTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 420 );
		articleTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 90 );
		articleTable.getTableHeader().setReorderingAllowed( false );
		
		articleTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		//ActionListener for interaction with the text window
		articleTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;
				int selectedRow = articleTable.getSelectedRow();
				if (selectedRow == -1) {
					textWindow.setText("");
				} else {
					textWindow.setText(ac.get(selectedRow).getText());
					textWindow.setEnabled(true);
				}
				if (articleButton.isSelected()) {
					articleFilter();
				}
				paintStatements();
				textWindow.setCaretPosition( 0 );
			}
		});
	}

	/**
	 * Text area with scroll bar. Interacts with the table.
	 */
	public void textArea() {
		
		textWindow.setContentType("text/html");
		textWindow.setEditable(false);
		kit = new HTMLEditorKit();
		doc = (HTMLDocument)(kit.createDefaultDocument());
		textWindow.setEditorKit(kit);
		textWindow.setDocument(doc);

		//create scroll pane
		textScrollPane = new JScrollPane(textWindow);
		textScrollPane.setPreferredSize(new Dimension(600, 400));
		textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
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
				int selectedRow = articleTable.getSelectedRow();
				
				if (selectedRow == -1) {
					System.out.println("Please select a row before inserting text!");
				} else {
					ac.get(selectedRow).setText(textWindow.getText());
				}
			}
		});
	}
	
	public void paintStatements() {
		int row = articleTable.getSelectedRow();
		if (row > -1) {
			String title = ac.get(row).getTitle();
			String text = ac.get(row).getText();
			text = stripHtmlTags(text, true);
			textWindow.setText(text);
			for (int i = 0; i < sc.size(); i++) {
				if (sc.get(i).getArticleTitle().equals(title)) {
					int start = sc.get(i).getStart();
					int end = sc.get(i).getStop();
					textWindow.setEditable(true);
			    	textWindow.setSelectionStart(start);
			    	textWindow.setSelectionEnd(end);
			    	String selection = textWindow.getSelectedText();
			    	
			    	textWindow.replaceSelection("");
					try{
						String replacement = "<span style=\"background-color:#EEEE66\">" + selection + "</span>";
						kit.insertHTML(doc, start, replacement, 0, 0, HTML.Tag.SPAN);
					} catch (IOException ioe) {
						System.out.println("IO Exception: " + ioe);
					} catch (BadLocationException ble) {
						System.out.println("Bad Location Exception: " + ble + " " + ble.offsetRequested());
					}
					textWindow.setEditable(false);
				}
			}
		}
	}
	
	/**
	 * Open a dna file.
	 */
	private void openDnaFile( String filename ) {
		clearSpace();
		
		try {
			SAXBuilder builder = new SAXBuilder( false );
			Document docXml = builder.build( new File( filename ) );
			Element discourse = docXml.getRootElement();
			
			Element version = (Element) discourse.getChildren().get(0);
			String v = version.getText();
			if (v.equals("1.09")) {
				System.out.println("DNA file with version 1.09 of the file format has been detected.");
				for (int i = 1; i < discourse.getChildren().size(); i++) {
					
					//create article with title, date and text
					Element article = (Element)discourse.getChildren().get(i);
					Element text = (Element) article.getChild("text");
					String articleText = new String("");
					for (Iterator<?> j = text.getContent().iterator(); j.hasNext( ); ) {
						Object o = j.next( );
						if (o instanceof Text) {
							articleText = articleText + ((Text)o).getText();
						} else if (o instanceof CDATA) {
							articleText = articleText + ((CDATA)o).getText();
						} else if (o instanceof Comment) {
							articleText = articleText + ((Comment)o).getText();
						} else if (o instanceof ProcessingInstruction) {
							articleText = articleText + (ProcessingInstruction)o;
						} else if (o instanceof EntityRef) {
							articleText = articleText + (EntityRef)o;
						} else if (o instanceof Element) {
							articleText = articleText + "<" + ((Element)o).getName() + "/>";
						}
					}
					
					articleText = articleText.replaceAll("\n", "<br />");
					
					String dateString = article.getChild("date").getText();
					String title = article.getChild("title").getText();
					Date date = new Date();
					try {
				    	SimpleDateFormat sdfToDate = new SimpleDateFormat("dd.MM.yyyy");
				    	date = sdfToDate.parse(dateString);
				        ac.addArticle(new Article(title, date, articleText));
				    } catch (ParseException pe) {
				    	pe.printStackTrace();
				    }
					
				    //create statements
				    Element statementsXml = (Element) article.getChild("statements");
				    for (int j = 0; j < statementsXml.getChildren().size(); j++) {
				    	Element statement = (Element) statementsXml.getChildren().get(j);
				    	int id = Integer.parseInt(statement.getChild("id").getText());
				    	String start = statement.getChild("start").getText();
				    	String end = statement.getChild("end").getText();
				    	String person = statement.getChild("person").getText();
				    	String organization = statement.getChild("organization").getText();
				    	String category = statement.getChild("category").getText();
				    	String agreement = statement.getChild("agreement").getText();
				    	
				    	int startInt = Integer.valueOf( start ).intValue();
				    	int endInt = Integer.valueOf( end ).intValue();
				    	
				    	String selection = articleText.replaceAll("<br />", "\n");
				    	
				    	try {
				    		if (endInt > selection.length() + 1) {
				    			endInt = selection.length() + 1;
				    			System.out.println("End position of statement " + id + " was corrected.");
				    		}
				    		if (startInt > selection.length()) {
				    			startInt = selection.length();
				    			System.out.println("Start position of statement " + id + " was corrected.");
				    		}
				    		
				    		selection = selection.substring(startInt-1, endInt-1);
				    	} catch (StringIndexOutOfBoundsException e) {
				    		System.out.println("Statement text of statement " + id + " could not be identified.");
				    	}
				    	
						//put statements into the statement list
						sc.addStatement(new Statement(id, startInt, endInt, date, selection, title, person, organization, category, agreement));
				    }
				}
				System.out.println("The file has been opened.");
			} else {
				System.out.println("An outdated version of the file format was found.");
				int convertDialog = JOptionPane.showConfirmDialog(Dna.this,
						"An outdated version of the file format was found." + "\n" +
						"DNA can try to import it. Please be sure you have" + "\n" +
						"a backup copy because the old file may be over-" + "\n" +
						"written. Please check your data for correctness" + "\n" +
						"after importing. If you encounter any errors, " + "\n" +
						"please contact the author. Would you like to " + "\n" +
						"import the file now?", "Confirmation required", JOptionPane.YES_NO_OPTION);
				if (convertDialog == 0) {
					System.out.println("Trying to import the file.");
					HashMap<String,Integer> titleOccurrences = new HashMap<String,Integer>();
					for (int i = 0; i < discourse.getChildren().size(); i++) {
						
						Element article = (Element)discourse.getChildren().get(i);
						
						String title = article.getAttributeValue("title");
						if (title.equals("")) {
							title = "(no title)";
						}
						if (titleOccurrences.containsKey(title)) {
							int value = titleOccurrences.get(title);
							value++;
							titleOccurrences.put(title, value);
							String valueString = new Integer(value).toString();
							title = title.concat(" (" + valueString + ")");
						} else {
							titleOccurrences.put(title, 1);
						}
						String day = article.getAttributeValue("day");
						String month = article.getAttributeValue("month");
						String year = article.getAttributeValue("year");
						
						GregorianCalendar articleCal = new GregorianCalendar();
						articleCal.set( Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day) );
						Date articleDate = articleCal.getTime();
						
						String articleText = new String("");
						String plainText = "";
						boolean lastWasStatement = false;
						for (Iterator<?> j = article.getContent().iterator(); j.hasNext( ); ) {
							Object o = j.next( );
							if (o instanceof Text) {
								plainText = "";
								plainText = ((Text)o).getText();
								articleText = articleText + plainText;
								articleText = articleText.replaceAll("(?<=\n)[ \\t\\x0B\\f]+(?=\n)", "");
								articleText = articleText.replaceAll("(?<!\n)\n(?!\n)", " ");
								articleText = articleText.replaceAll("[ \\t\\x0B\\f]+", " ");
								articleText = articleText.replaceAll("(?<=\n) ", "");
								articleText = articleText.replaceAll(" (?=\n)", "");
								lastWasStatement = false;
							} else if (o instanceof CDATA) {
								articleText = articleText + ((CDATA)o).getText();
							} else if (o instanceof Comment) {
								articleText = articleText + ((Comment)o).getText();
							} else if (o instanceof ProcessingInstruction) {
								articleText = articleText + (ProcessingInstruction)o;
							} else if (o instanceof EntityRef) {
								articleText = articleText + (EntityRef)o;
							} else if (o instanceof Element) {
								if (((Element)o).getName().equals("statement")) {
									String person = ((Element)o).getAttributeValue("person");
									String organization = ((Element)o).getAttributeValue("organization");
									String category = ((Element)o).getAttributeValue("category");
									String agreement = ((Element)o).getAttributeValue("agreement");
									String text = ((Element)o).getText();
									text = text.replaceAll("(?<=\n)[ \\t\\x0B\\f]+(?=\n)", "");
									text = text.replaceAll("\n", " ");
									text = text.replaceAll("[ \\t\\x0B\\f]+", " ");
									text = text.replaceAll("(?<=\n) ", "");
									int offset = 0;
									if (lastWasStatement == false && articleText.endsWith(" ") && text.startsWith(" ")) {
										text = text.substring(1);
									} else if (!articleText.endsWith(" ") && text.startsWith(" ")) {
										articleText = articleText.concat(" ");
										text = text.substring(1);
										offset++;
									} else if (lastWasStatement == true && articleText.endsWith(" ")) {
										offset++;
									}
									int start, end;
									start = articleText.length() + 1 - offset;
									end = text.length() + start;
									int id = sc.getFirstUnusedId();
									articleText = articleText + text;
									sc.addStatement(new Statement(id, start, end, articleDate, text, title, person, organization, category, agreement));
									lastWasStatement = true;
								}
							}
						}
						
						articleText = articleText.replaceAll("\n", "<br/>");
						
						JEditorPane jepOpen = new JEditorPane();
						jepOpen.setContentType("text/html");
						jepOpen.setText(articleText);
						articleText = jepOpen.getText();
						
						Article art = new Article(title, articleDate, articleText);
						ac.addArticle(art);
					}
					for (int i = 0; i < ac.getRowCount(); i++) {
						articleTable.changeSelection(i, 0, false, false);
						textWindow.grabFocus();
					}
					System.out.println("File was imported. Please use the save-as button if you don't want to overwrite the old file.");
				}
			}
		} catch (IOException e) {
			System.out.println("Error while reading the file \"" + filename + "\".");
			JOptionPane.showMessageDialog(Dna.this, "Error while reading the file!");
			currentFileName = "";
			currentFileLabel.setText("Current file: none");
		} catch (org.jdom.JDOMException e) {
			System.out.println("Error while opening the file \"" + filename + "\": " + e.getMessage());
			JOptionPane.showMessageDialog(Dna.this, "Error while opening the file!");
			currentFileName = "";
			currentFileLabel.setText("Current file: none");
		}
	}
	
	/**
	 * Toolbar with buttons.
	 */
	private void buttons() {
		toolbarPanel = new JPanel(new BorderLayout());
		toolbar = new JToolBar("Toolbar", JToolBar.VERTICAL);
		toolbar.setFloatable(false);
		toolbarPanel.add(toolbar, BorderLayout.NORTH);

		//Button: Open .dna file
		Icon openIcon = new ImageIcon(getClass().getResource("/icons/folder.png"));
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
					if (ac.getRowCount() > 0) {
						currentFileName = file.getPath();
						currentFileLabel.setText( "Current file: " + currentFileName );
					}
				}
			}
		});
		
		//Button: import statements
		Icon importIcon = new ImageIcon(getClass().getResource("/icons/page_white_get.png"));
		JButton importStatements = new JButton(importIcon);
		importStatements.setToolTipText( "import articles from DNA file..." );
		toolbar.add(importStatements);
		importStatements.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
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
					new ArticleImport(file.getPath());
				}
			}
		});

		//Button: save as...
		Icon saveAsIcon = new ImageIcon(getClass().getResource("/icons/disk_multiple.png"));
		JButton saveAs = new JButton(saveAsIcon);
		saveAs.setToolTipText( "save as DNA file..." );
		toolbar.add(saveAs);
		saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsDialog();
			}
		});

		//Save button
		Icon saveIcon = new ImageIcon(getClass().getResource("/icons/disk.png"));
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
		Icon closeIcon = new ImageIcon( getClass().getResource("/icons/cancel.png") );
		JButton closeFile = new JButton(closeIcon);
		closeFile.setToolTipText( "close current file" );
		toolbar.add(closeFile);
		closeFile.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDnaFile();
			}
		});
		
		//Button: add new article
		Icon newArticleIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
		JButton newArticleButton = new JButton(newArticleIcon);
		newArticleButton.setToolTipText( "add new article..." );
		toolbar.add(newArticleButton);
		newArticleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newArticle();
			}
		});
		
		//Button: delete selected row
		Icon deleteRowIcon = new ImageIcon(getClass().getResource("/icons/table_delete.png"));
		JButton cmdDelete = new JButton(deleteRowIcon);
		cmdDelete.setToolTipText( "delete selected row" );
		toolbar.add(cmdDelete);
		cmdDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ac.getRowCount() > 0) {
					int dialog = JOptionPane.showConfirmDialog(Dna.this, "Are you sure you want to delete the selected article?", "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (articleTable.getSelectedRow() != -1 && dialog == 0) {
						int zeile = articleTable.convertRowIndexToModel(articleTable.getSelectedRow());
						for (int i = sc.getRowCount() - 1; i > -1; i--) {
							if (sc.get(i).getArticleTitle().equals(ac.get(articleTable.getSelectedRow()).getTitle())) {
								int id = sc.get(i).getId();
								sc.removeStatement(id);
							}
						}
						ac.remove(zeile);
						articleTable.changeSelection(articleTable.getRowCount()-1, 0, false, false);
						if (articleTable.getRowCount() < 1) {
							textWindow.setEnabled(false);
						}
					}
				}
			}
		});
		
		//Button: change name or date of current article
		Icon renameIcon = new ImageIcon(getClass().getResource("/icons/table_edit.png"));
		JButton renameButton = new JButton(renameIcon);
		renameButton.setToolTipText( "rename current article or change its date..." );
		toolbar.add(renameButton);
		renameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new RenameArticle();
			}
		});
		
		//Button: recode window
		Icon recodeIcon = new ImageIcon(getClass().getResource("/icons/database_go.png"));
		JButton recodeButton = new JButton(recodeIcon);
		recodeButton.setToolTipText( "rename or remove actors or categories..." );
		toolbar.add(recodeButton);
		recodeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sc.size() > 0) {
					new Recode();
				}
			}
		});
		
		//Button: statistics component
		Icon statisticsIcon = new ImageIcon(getClass().getResource("/icons/chart_curve.png"));
		JButton statisticsButton = new JButton(statisticsIcon);
		statisticsButton.setToolTipText( "export time series statistics of statement frequencies..." );
		toolbar.add(statisticsButton);
		statisticsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Statistics(sc);
			}
		});
		
		//Button: export
		Icon expIcon = new ImageIcon(getClass().getResource("/icons/chart_organisation.png"));
		JButton expButton = new JButton(expIcon);
		expButton.setToolTipText( "export to network..." );
		toolbar.add(expButton);
		expButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StatementContainer sCont = new StatementContainer();
				sCont = (StatementContainer)sc.clone();
				new DnaExportWindow(sCont);
			}
		});

		//Button: about DNA
		Icon aboutIcon = new ImageIcon(getClass().getResource("/icons/information.png"));
		JButton aboutButton = new JButton(aboutIcon);
		aboutButton.setToolTipText( "about DNA" );
		toolbar.add(aboutButton);
		aboutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new DnaAbout();
			}
		});
	}
	
	public void updateLists() {
		persListe.clear();
		orgListe.clear();
		catListe.clear();
		
		for (int i = 0; i < sc.getRowCount(); i++) {
			if(!persListe.contains(sc.get(i).getPerson())) {
				persListe.add(sc.get(i).getPerson());
			}
			if(!orgListe.contains(sc.get(i).getOrganization())) {
				orgListe.add(sc.get(i).getOrganization());
			}
			if(!catListe.contains(sc.get(i).getCategory())) {
				catListe.add(sc.get(i).getCategory());
			}
		}
		Collections.sort(persListe);
		Collections.sort(orgListe);
		Collections.sort(catListe);
	}
	
	public int getIdByClickPosition(int clickPosition) {
		int id = -1;
		for (int i = 0; i < sc.getRowCount(); i++) {
			if (sc.get(i).start < clickPosition && sc.get(i).stop > clickPosition && sc.get(i).articleTitle.equals(ac.get(articleTable.getSelectedRow()).getTitle())) {
				id =  sc.get(i).getId();
			}
		}
		return id;
	}
	
	public void mouseListenPopup(MouseEvent me) {
		if ( me.isPopupTrigger() ) {
			if (textWindow.getSelectedText() == null || textWindow.getSelectionStart() < 2 || textWindow.getSelectedText().contains("  ")) {
				menu1.setEnabled(false);
			} else {
				menu1.setEnabled(true);
			}
			popmen.show( me.getComponent(), me.getX(), me.getY() );
		}
	}
	
	public void mouseListenSelect(MouseEvent me) {
		if ( me.isPopupTrigger() ) {
			if (textWindow.getSelectedText() == null || textWindow.getSelectionStart() < 2 || textWindow.getSelectedText().contains("  ")) {
				menu1.setEnabled(false);
			} else {
				menu1.setEnabled(true);
			}
			popmen.show( me.getComponent(), me.getX(), me.getY() );
		} else {
			int id = getIdByClickPosition(textWindow.getCaretPosition());
			if (id != -1) {
				int viewId = statementTable.convertRowIndexToView(sc.getIndexById(id));
				if (viewId == -1) {
					statementTable.clearSelection();
				} else {
					statementTable.changeSelection(viewId, 0, false, false);
				}
				int startIndex = -1;
				int stopIndex = -1;
				startIndex = sc.getById(id).getStart();
				stopIndex = sc.getById(id).getStop();
				textWindow.setSelectionStart(startIndex);
				textWindow.setSelectionEnd(stopIndex);
				if (editMode.isSelected()) {
					Point p = me.getPoint();
					new ToolTip(p, id);
				}
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

		//MouseListener for the text window; one method for Windows and one for Unix
		textWindow.addMouseListener( new MouseAdapter() {
			public void mouseReleased( MouseEvent me ) {
				mouseListenPopup(me);
			}
			public void mousePressed( MouseEvent me ) {
				mouseListenPopup(me);
			}
			
			public void mouseClicked( MouseEvent me ) {
				mouseListenSelect(me);
			}
		});
		
		//Item: Insert statement
		menu1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectionStart = textWindow.getSelectionStart();
				int selectionEnd = textWindow.getSelectionEnd();
				String selection = new String(textWindow.getSelectedText());
				
				String firstChar = selection.substring(0, 1);
				String lastChar = selection.substring((selection.length() - 1), selection.length());
				if (firstChar.equals(" ")) {
					textWindow.setSelectionStart(selectionStart+1);
					selectionStart = textWindow.getSelectionStart();
				}
				if (lastChar.equals(" ")) {
					textWindow.setSelectionEnd(selectionEnd-1);
					selectionEnd = textWindow.getSelectionEnd();
				}
				selection = new String(textWindow.getSelectedText());
				
				updateLists();
				
				Date date = ac.get(articleTable.getSelectedRow()).getDate();
				
				String articleTitle = ac.get(articleTable.getSelectedRow()).getTitle();
				int statementId = sc.getFirstUnusedId();
				sc.addStatement(new Statement(statementId, selectionStart, selectionEnd, date, selection, articleTitle, "", "", "", "yes"));
				System.out.println("Added a statement with ID " + statementId + " starting at offset " + selectionStart + " and ending at " + selectionEnd);
				paintStatements();
			}
		});
	}
	
	public void removeStatement(int id) {
		int currentArticle = articleTable.getSelectedRow();
		if (currentArticle > -1) {
			if (id != -1) {
				sc.removeStatement(id);
				articleTable.changeSelection(currentArticle, 0, false, false);
				paintStatements();
				System.out.println("Statement with ID " + id + " has been removed.");
			}
		}
	}
	
	public class ToolTip extends JFrame {
		
		Container c;
		Point point;
		int statementId;
		JPanel gridBagPanel;
		GridBagConstraints gbc;
		JComboBox agreement;
		JComboBoxCustomize person, organization, category;
		JTextField idField, startPos, endPos;
		
		public void showToolTip() {
			this.setTitle("Statement details");
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			ImageIcon addIcon = new ImageIcon(getClass().getResource("/icons/comment_edit.png"));
			this.setIconImage(addIcon.getImage());
			
			c = getContentPane();
			
			updateLists();
			
			gridBagPanel = new JPanel(new GridBagLayout());
			gbc = new GridBagConstraints();
			
			JLabel sPosLabel = new JLabel("start position");
			startPos = new JTextField(new Integer(sc.getById(statementId).getStart()).toString());
			startPos.setEditable(false);
			
			JLabel ePosLabel = new JLabel("end position");
			endPos = new JTextField(new Integer(sc.getById(statementId).getStop()).toString());
			endPos.setEditable(false);
			
			JLabel agreeLabel = new JLabel("agreement", JLabel.TRAILING);
			agreement = new JComboBox(agListe);
			agreement.setEditable(false);
			agreement.setSelectedItem(sc.getById(statementId).getAgreement());
			
			JLabel idLabel = new JLabel("ID");
			idField = new JTextField(new Integer(sc.getById(statementId).getId()).toString());
			idField.setEditable(false);
			
			JLabel persLabel = new JLabel("person", JLabel.TRAILING);
			person = new JComboBoxCustomize(persListe.toArray(), true);
			person.setEditable(true);
			person.setSelectedItem(sc.getById(statementId).getPerson());
			
			JLabel orgLabel = new JLabel("organization", JLabel.TRAILING);
			organization = new JComboBoxCustomize(orgListe.toArray(), true);
			organization.setEditable(true);
			organization.setSelectedItem(sc.getById(statementId).getOrganization());
			
			JLabel catLabel = new JLabel("category", JLabel.TRAILING);
			category = new JComboBoxCustomize(catListe.toArray(), true);
			category.setEditable(true);
			category.setSelectedItem(sc.getById(statementId).getCategory());
			
			ImageIcon removeIcon = new ImageIcon(getClass().getResource("/icons/cross.png"));
			JButton remove = new JButton("remove", removeIcon);
			remove.setToolTipText("completely remove the whole statement but keep the text");
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int question = JOptionPane.showConfirmDialog(Dna.this, "Are you sure you want to remove this statement?", 
							"Remove?", JOptionPane.YES_NO_OPTION);
					if (question == 0) {
						removeStatement(statementId);
						dispose();
					}
				}
			});
			
			ImageIcon closeIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
			JButton close = new JButton(closeIcon);
			close.setToolTipText("commit changes and get rid of this popup window");
			close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveAndClose();
				}
			});
			
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets(3,3,3,3);
			gbc.fill = GridBagConstraints.BOTH;
			gridBagPanel.add(persLabel, gbc);
			
			gbc.gridx = 1;
			gridBagPanel.add(person, gbc);
			
			gbc.gridx = 2;
			gridBagPanel.add(idField, gbc);
			
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 3;
			gridBagPanel.add(idLabel, gbc);
			
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gridBagPanel.add(orgLabel, gbc);
			
			gbc.gridx = 1;
			gridBagPanel.add(organization, gbc);
			
			gbc.gridx = 2;
			gridBagPanel.add(startPos, gbc);
			
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 3;
			gridBagPanel.add(sPosLabel, gbc);
			
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridx = 0;
			gbc.gridy = 2;
			gridBagPanel.add(catLabel, gbc);
			
			gbc.gridx = 1;
			gridBagPanel.add(category, gbc);
			
			gbc.gridx = 2;
			gridBagPanel.add(endPos, gbc);
			
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 3;
			gridBagPanel.add(ePosLabel, gbc);
			
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridx = 0;
			gbc.gridy = 3;
			gridBagPanel.add(agreeLabel, gbc);
			
			gbc.gridx = 1;
			gridBagPanel.add(agreement, gbc);
			
			gbc.gridx = 2;
			gridBagPanel.add(close, gbc);
			
			gbc.gridx = 3;
			gridBagPanel.add(remove, gbc);
			
			c.add(gridBagPanel);
			
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					try {
						saveAndClose();
					} catch (NullPointerException npe) {
						System.out.println("Statement details could not be saved.");
					}
				}
			});
			
			addWindowFocusListener(new WindowAdapter() {
				public void windowLostFocus(WindowEvent e) {
					try {
						saveAndClose();
					} catch (NullPointerException npe) {
						System.out.println("Statement details could not be saved.");
					}
			    }
			});
			
			this.setUndecorated(true);
			this.pack();
		}
		
		public ToolTip(Point point, final int statementId) {
			this.point = point;
			this.statementId = statementId;
			showToolTip();
			Point los = textWindow.getLocationOnScreen();
			double xDouble = los.getX() + point.getX();
			double yDouble = los.getY() + point.getY();
			int x = (int) xDouble + 6;
			int y = (int) yDouble + 13;
			this.setLocation(x, y);
			this.setVisible(true);
		}
		
		public ToolTip(final int statementId) {
			this.statementId = statementId;
			showToolTip();
			this.setLocationRelativeTo(Dna.this);
			Point dnaPos = Dna.this.getLocationOnScreen();
			double dnaY = dnaPos.getY();
			double h = Dna.this.getHeight();
			double thisHeight = this.getHeight();
			double newY = dnaY + h - thisHeight - 30;
			Point p = this.getLocation();
			double x = p.getX();
			this.setLocation(new Double(x).intValue(), new Double(newY).intValue());
			this.setVisible(true);
		}
		
		public void saveAndClose() {
			String p = (String)person.getEditor().getItem();
			sc.getById(statementId).setPerson(p);
			String o = (String)organization.getEditor().getItem();
			sc.getById(statementId).setOrganization(o);
			String c = (String)category.getEditor().getItem();
			sc.getById(statementId).setCategory(c);
			String a = agreement.getSelectedItem().toString();
			sc.getById(statementId).setAgreement(a);
			dispose();
		}
	}
	
	public class ArticleImport extends JFrame {
		
		Container c;
		JTable articleImportTable;
		ArticleImportTableModel aitm;
		
		public ArticleImport(final String file) {
			c = getContentPane();
			this.setTitle("Import statements...");
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			ImageIcon importIcon = new ImageIcon(getClass().getResource("/icons/page_white_get.png"));
			this.setIconImage(importIcon.getImage());
			
			aitm = new ArticleImportTableModel();
			articleImportTable = new JTable(aitm);
			articleImportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane tableScrollPane = new JScrollPane(articleImportTable);
			tableScrollPane.setPreferredSize(new Dimension(500, 300));
			
			articleImportTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 20 );
			articleImportTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 400 );
			articleImportTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 80 );
			articleImportTable.getTableHeader().setReorderingAllowed( false );
			
			JButton importButton = new JButton("Import selected articles", importIcon);
			importButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int question = JOptionPane.showConfirmDialog(Dna.this, "Are you sure you want to insert the selected \n articles into the currently open DNA file?", 
							"Confirmation", JOptionPane.YES_NO_OPTION);
					if (question == 0) {
						insertArticles(file);
						dispose();
					}
				}
			});
			ImageIcon selectIcon = new ImageIcon(getClass().getResource("/icons/asterisk_yellow.png"));
			JButton selectAll = new JButton("Select/unselect all articles", selectIcon);
			selectAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if ((Boolean)aitm.getValueAt(0, 0) == false) {
						for (int i = 0; i < aitm.getRowCount(); i++) {
							aitm.setValueAt(true, i, 0);
						}
					} else {
						for (int i = 0; i < aitm.getRowCount(); i++) {
							aitm.setValueAt(false, i, 0);
						}
					}
					
				}
			});
			JPanel buttonPanel = new JPanel(new GridLayout(1,2));
			buttonPanel.add(selectAll);
			buttonPanel.add(importButton);
			
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(tableScrollPane, BorderLayout.CENTER);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			
			parseArticles(file);
			
			c.add(panel);
			
			this.pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}
		
		public void insertArticles(String filename) {
			for (int k = 0; k < aitm.getRowCount(); k++) {
				if ((Boolean)aitm.getValueAt(k, 0) == true) {
					try {
						SAXBuilder builder = new SAXBuilder( false );
						Document docXml = builder.build( new File( filename ) );
						Element discourse = docXml.getRootElement();
						
						Element article = (Element)discourse.getChildren().get(k+1);
						Element text = (Element) article.getChild("text");
						String articleText = new String("");
						for (Iterator<?> j = text.getContent().iterator(); j.hasNext( ); ) {
							Object o = j.next( );
							if (o instanceof Text) {
								articleText = articleText + ((Text)o).getText();
							} else if (o instanceof CDATA) {
								articleText = articleText + ((CDATA)o).getText();
							} else if (o instanceof Comment) {
								articleText = articleText + ((Comment)o).getText();
							} else if (o instanceof ProcessingInstruction) {
								articleText = articleText + (ProcessingInstruction)o;
							} else if (o instanceof EntityRef) {
								articleText = articleText + (EntityRef)o;
							} else if (o instanceof Element) {
								articleText = articleText + "<" + ((Element)o).getName() + "/>";
							}
						}
						
						articleText = articleText.replaceAll("\n", "<br />");
						
						String dateString = article.getChild("date").getText();
						String title = article.getChild("title").getText();
						
						boolean duplicate = false;
						for (int l = 0; l < Dna.this.ac.getRowCount(); l++) {
							if (Dna.this.ac.get(l).getTitle().equals(title)) {
								duplicate = true;
							}
						}
						if (duplicate == true) {
							int count = 2;
							Pattern p = Pattern.compile(title + " \\(" + "[0-9]+" + "\\)");
							for (int l = 0; l < Dna.this.ac.getRowCount(); l++) {
								Matcher m = p.matcher(Dna.this.ac.get(l).getTitle());
								boolean b = m.find();
								if (b == true) {
									count++;
								}
							}
							title = title.concat(" (" + count + ")");
						}
						
						Date date = new Date();
						try {
					    	SimpleDateFormat sdfToDate = new SimpleDateFormat("dd.MM.yyyy");
					    	date = sdfToDate.parse(dateString);
					        Dna.this.ac.addArticle(new Article(title, date, articleText));
					    } catch (ParseException pe) {
					    	pe.printStackTrace();
					    }
						
					    //create statements
					    Element statementsXml = (Element) article.getChild("statements");
					    for (int j = 0; j < statementsXml.getChildren().size(); j++) {
					    	Element statement = (Element) statementsXml.getChildren().get(j);
					    	int id = sc.getFirstUnusedId();
					    	String start = statement.getChild("start").getText();
					    	String end = statement.getChild("end").getText();
					    	String person = statement.getChild("person").getText();
					    	String organization = statement.getChild("organization").getText();
					    	String category = statement.getChild("category").getText();
					    	String agreement = statement.getChild("agreement").getText();
					    	
					    	int startInt = Integer.valueOf( start ).intValue();
					    	int endInt = Integer.valueOf( end ).intValue();
					    	
					    	String selection = articleText.replaceAll("<br />", "\n");
					    	
					    	try {
					    		if (endInt > selection.length() + 1) {
					    			endInt = selection.length() + 1;
					    			System.out.println("End position of statement " + id + " was corrected.");
					    		}
					    		if (startInt > selection.length()) {
					    			startInt = selection.length();
					    			System.out.println("Start position of statement " + id + " was corrected.");
					    		}
					    		
					    		selection = selection.substring(startInt-1, endInt-1);
					    	} catch (StringIndexOutOfBoundsException e) {
					    		System.out.println("Statement text of statement " + id + " could not be identified.");
					    	}
					    	
							//put statements into the statement list
							sc.addStatement(new Statement(id, startInt, endInt, date, selection, title, person, organization, category, agreement));
					    }
					} catch (IOException e) {
						System.out.println("Error while reading the file \"" + filename + "\".");
						JOptionPane.showMessageDialog(Dna.this, "Error while reading the file!");
						currentFileName = "";
						currentFileLabel.setText("Current file: none");
					} catch (org.jdom.JDOMException e) {
						System.out.println("Error while opening the file \"" + filename + "\": " + e.getMessage());
						JOptionPane.showMessageDialog(Dna.this, "Error while opening the file!");
						currentFileName = "";
						currentFileLabel.setText("Current file: none");
					}
				}
			}
		}
		
		public void parseArticles(String filename) {
			try {
				SAXBuilder builder = new SAXBuilder( false );
				Document docXml = builder.build( new File( filename ) );
				Element rootElement = docXml.getRootElement();
				
				Element version = (Element) rootElement.getChildren().get(0);
				String v = version.getText();
				if (v.equals("1.09")) {
					for (int i = 1; i < rootElement.getChildren().size(); i++) {
						Element article = (Element)rootElement.getChildren().get(i);
						String dateString = article.getChild("date").getText();
						String title = article.getChild("title").getText();
						aitm.addArticle(title, dateString, false);
					}
				} else {
					System.out.println("Articles can only be imported from valid DNA files!");
					JOptionPane.showMessageDialog(Dna.this,	"Articles can only be imported from valid DNA files!", "Confirmation required", JOptionPane.OK_OPTION);
				}
			} catch (IOException e) {
				System.out.println("Error while reading the file \"" + filename + "\".");
				JOptionPane.showMessageDialog(Dna.this, "Error while reading the file!");
				currentFileName = "";
				currentFileLabel.setText("Current file: none");
			} catch (org.jdom.JDOMException e) {
				System.out.println("Error while opening the file \"" + filename + "\": " + e.getMessage());
				JOptionPane.showMessageDialog(Dna.this, "Error while opening the file!");
				currentFileName = "";
				currentFileLabel.setText("Current file: none");
			}
		}
		
		class ArticleImportTableModel implements TableModel {
			private Vector<TableModelListener> listeners = new Vector<TableModelListener>();
			private Vector<String> titles = new Vector<String>();
			private Vector<String> dates = new Vector<String>();
			private Vector<Boolean> selections = new Vector<Boolean>();
			
			public void addArticle( String title, String date, boolean selection ){
				
				int index = titles.size();
				titles.add( title );
				dates.add( date );
				selections.add( selection );
				
				TableModelEvent e = new TableModelEvent( this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
				
				for( int i = 0, n = listeners.size(); i<n; i++ ) {
					((TableModelListener)listeners.get( i )).tableChanged( e );
				}
			}
			
			public int getColumnCount() {
				return 3;
			}
			
			public int getRowCount() {
				return titles.size();
			}
			
			public String getColumnName(int column) {
				switch( column ){
					case 0: return "";
					case 1: return "title";
					case 2: return "date";
					default: return null;
				}
			}
			
			public Object getValueAt(int rowIndex, int columnIndex) {
				switch( columnIndex ){
					case 0: return selections.get(rowIndex) ? Boolean.TRUE : Boolean.FALSE; 
					case 1: return titles.get(rowIndex);
					case 2: return dates.get(rowIndex);
					default: return null;
				}
			}
			
			@SuppressWarnings( "unchecked" )
			public Class getColumnClass(int columnIndex) {
				switch( columnIndex ){
					case 0: return Boolean.class;
					case 1: return String.class;
					case 2: return String.class; 
					default: return null;
				}	
			}
			
			public void addTableModelListener(TableModelListener l) {
				listeners.add( l );
			}
			public void removeTableModelListener(TableModelListener l) {
				listeners.remove( l );
			}
			
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				switch( columnIndex ){
					case 0: return true;
					case 1: return false;
					case 2: return false; 
					default: return false;
				}
			}
			
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				if (columnIndex == 0) {
					selections.set(rowIndex, (Boolean)aValue);
				}
				TableModelEvent e = new TableModelEvent(this);
				for( int i = 0, n = listeners.size(); i < n; i++ ){
					((TableModelListener)listeners.get( i )).tableChanged( e );
				}
			}
		}
	}

	/**
	 * Main method. Instantiates the application.
	 */
	public static void main (String[] args) {
		new Dna();
	}
}