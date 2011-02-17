package dna;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 * This is the main component of the Discourse Network Analyzer.
 * It instantiates the DNA coder.
 * 
 * done:
 * - Bugfix: selections in the include-lists in the time series statistics component had no effect
 * - 
 * 
 * to do:
 * - automatically remove and re-insert instructions in NewArticle window upon mouse click
 * - forbid quotation marks etc. in person/org/cat fields
 * - option to remove a type from the table in the actor attribute manager
 * - Show empty export options panel where appropriate
 * - Embed the bottom bar in a better way; with buttons inside the panes
 * - use colors and other attributes for Sonia and Commetrix export and possibly Ucinet/DL export
 * - network export options: select actors by their type; show colors of actors; binary choice whether name or alias is exported
 * - Gephi interoperability via GFX file format; dynamic data?
 * - concept hierarchy
 * - network export: two new options: exclude actors/concepts with less than n statements (or less than n different statements)
 * - check DNA files for strange symbols, double spaces etc.; this could be a problem because people use the XML import format
 * - auto-backup function
 * 
 * @author Philip Leifeld
 * @version 1.25 - 2010-12-06
 */
@SuppressWarnings("serial")
public class Dna extends JFrame {
	
	static Dna mainProgram;
	
	DnaContainer dc;

	Container c;
	
	JTextField titleField;
	
	ArrayList<String> catListe = new ArrayList<String>();
	ArrayList<String> persListe = new ArrayList<String>();
	String[] agListe = new String[] {"yes", "no"};
	ActorManager pm;
	ActorManager om;
	
	JPopupMenu popmen = new JPopupMenu();
	ImageIcon addStatementIcon = new ImageIcon(getClass().getResource("/icons/comment_edit.png"));
	JMenuItem menu1 = new JMenuItem( "Format as statement", addStatementIcon);

	JLabel currentFileLabel;
	JLabel loading;
	
	JPanel centralTextPanel;
	JXCollapsiblePane collapsiblePane;
	CardLayout containerStack;
	SearchWindow searchWindow;
	JMenuItem searchPanelButton, persManagerButton, orgManagerButton, toggleBottomButton, orgExportButton, persExportButton;
	JEditorPane textWindow = new JEditorPane();
	JScrollPane textScrollPane;
	HTMLEditorKit kit;
	HTMLDocument doc;
	
	ExportWindow export;
	
	JTable articleTable;
	JScrollPane tableScrollPane;
	TableRowSorter<ArticleContainer> articleSorter;
	TableRowSorter<StatementContainer> sorter;

	JPanel statusBar;
	JMenuBar menuBar;
	JMenu fileMenu, articleMenu, exportMenu, extrasMenu;
	String extension, description;
	
	JScrollPane taskPaneScroller;
	StatementFilter statementFilter;
	RegexManager regexManager;
	ContradictionReporter contradictionReporter;
	DocStats docStats;
	JTextField searchField;
	SearchPanel sp;
	JButton searchPrevious, searchNext, searchRevert;
	ArrayList<Tupel> matches;
	JTable statementTable;
	JScrollPane statementTableScrollPane;
	JPanel statementPanel;
	JCheckBoxMenuItem editMode, toolTipColors;
	
	String strippedContents;

	/**
	 * This constructor opens up the main window and executes a
	 * number of layout components.
	 */
	public Dna() {
		
		dc = new DnaContainer();
		
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
		
		//buttons();
		menu();
		articleTable();
		textArea();
		status();
		contextMenu();
		statementTable();
		
		//layout of the main window
		JPanel codingPanel = new JPanel(new BorderLayout());
		JSplitPane codingSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, centralTextPanel);
		codingPanel.add(menuBar, BorderLayout.NORTH);
		JPanel statementSplitPane = new JPanel(new BorderLayout());
		statementSplitPane.add(codingSplitPane, BorderLayout.CENTER);
		statementSplitPane.add(taskPaneScroller, BorderLayout.EAST);
		codingPanel.add(statementSplitPane, BorderLayout.CENTER);
		codingPanel.add(statusBar, BorderLayout.SOUTH);
		c.add(codingPanel);
		
		//pack the window, set its location, and make it visible
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public class RegexManager extends JPanel {
		
		JButton colorButton;
		JTextField textField;
		DefaultListModel listModel;
		JList regexList;
		
		public RegexManager() {
			this.setLayout(new BorderLayout());
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					paintStatements();
				}
			});
			
			listModel = new DefaultListModel();
			regexList = new JList(listModel);
			regexList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			regexList.setLayoutOrientation(JList.VERTICAL);
			regexList.setVisibleRowCount(10);
			regexList.setCellRenderer(new RegexListRenderer());
			JScrollPane listScroller = new JScrollPane(regexList);
			listScroller.setPreferredSize(new Dimension(50, 100));
			
			for (int i = 0; i < dc.regexTerms.size(); i++) {
				listModel.addElement(dc.regexTerms.get(i));
			}
			
			this.add(listScroller, BorderLayout.NORTH);
			
			JPanel newFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			textField = new JTextField(15);
			newFields.add(textField);
			
			colorButton = (new JButton() {
	            protected void paintComponent(Graphics g) {
	                super.paintComponent(g);
	                g.setColor(this.getForeground());
	                g.fillRect(2, 2, 14, 14);
	            }
	        });
			colorButton.setForeground(Color.RED);
			colorButton.setPreferredSize(new Dimension(18, 18));
			colorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color actualColor = Color.RED;
					Color newColor = JColorChooser.showDialog(c, "new color...", actualColor);
					if (newColor != null) {
						((JButton) e.getSource()).setForeground(newColor);
					}
				}
			});
			newFields.add(colorButton);

			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			Icon addIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
			JButton add = new JButton("add", addIcon);
			add.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color cl = colorButton.getForeground();
					String text = textField.getText();
					if (text.length() > 0) {
						dc.regexTerms.add(new RegexTerm(text, cl));
						listModel.addElement(dc.regexTerms.get(dc.regexTerms.size()-1));
					}
					Dna.mainProgram.paintStatements();
				}
			});
			buttons.add(add);
			
			Icon removeIcon = new ImageIcon(getClass().getResource("/icons/cross.png"));
			JButton remove = new JButton("remove", removeIcon);
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index = regexList.getSelectedIndex();
					if (index >= 0) {
						dc.regexTerms.remove(index);
						listModel.remove(index);
					}
					Dna.mainProgram.paintStatements();
				}
			});
			buttons.add(remove);
			
			this.add(newFields, BorderLayout.CENTER);
			this.add(buttons, BorderLayout.SOUTH);
		}

		public class ColorChooserDemo extends JPanel {
			JColorChooser chooser;
			public ColorChooserDemo() {
		        super(new BorderLayout());
		        chooser = new JColorChooser(Color.RED);
		        add(chooser, BorderLayout.PAGE_END);
		    }
		}
		
		public void clear() {
			dc.regexTerms.clear();
			textField.setText("");
			listModel.removeAllElements();
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
				currentTitle = dc.ac.get(currentRow).getTitle();
				currentDate = dc.ac.get(currentRow).getDate();
				
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
						for (int i = 0; i < dc.sc.size(); i++) {
							if (dc.sc.get(i).getArticleTitle().equals(currentTitle)) {
								dc.sc.get(i).setDate((Date)dateSpinner.getValue());
								dc.sc.get(i).setArticleTitle(titleField.getText());
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
							for (int i = 0; i < dc.ac.getRowCount(); i++) {
								if (dc.ac.getValueAt(i, 0).equals(title) && currentRow != i) {
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
	
	public class NewArticle extends JFrame {
		
		SpinnerDateModel dateModel;
		JSpinner dateSpinner;
		JScrollPane textScroller;
		JTextArea textArea;
		JButton okButton;
		JPanel newArticlePanel;
		
		public NewArticle() {
			
			this.setTitle("Add new article...");
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			ImageIcon tableAddIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
			this.setIconImage(tableAddIcon.getImage());
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			
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
			textScroller.setPreferredSize(new Dimension(600, 400));
			
			textScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			
			Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
			okButton = new JButton("OK", okIcon);
			okButton.setToolTipText( "insert a new article based on the information you entered in this window" );
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String text = textArea.getText();
					text = text.replaceAll("\n", "<br/>");
					Article article = new Article(titleField.getText(), (Date)dateSpinner.getValue(), text);
					dc.ac.addArticle( article );
					
					int index = -1;
					for (int i = 0; i < dc.ac.getRowCount(); i++) {
						if (titleField.getText().equals(dc.ac.get(i).getTitle())) {
							index = i;
						}
					}
					
					articleTable.changeSelection(index, 0, false, false);
					dispose();
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
						for (int i = 0; i < dc.ac.getRowCount(); i++) {
							if (dc.ac.getValueAt(i, 0).equals(title)) {
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
			
			newArticlePanel = new JPanel(new BorderLayout());
			newArticlePanel.add(northPanel, BorderLayout.NORTH);
			newArticlePanel.add(textScroller, BorderLayout.CENTER);
			
			this.add(newArticlePanel);
			this.pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}
	}
	
	private class Tupel {
		int startValue;
		int endValue;
		
		public Tupel(int startValue, int endValue) {
			this.startValue = startValue;
			this.endValue = endValue;
		}
		
		public int getStartValue() {
			return startValue;
		}
		
		public int getEndValue() {
			return endValue;
		}
	}
	
	class SearchPanel extends JPanel {
		
		public SearchPanel() {
			this.setLayout(new BorderLayout());
			searchField = new JTextField();
			String searchToolTip = "<html>You can enter a regular expression in the search <br/>field and click on the arrow buttons to go to the <br/>previous or next occurrence in the text. This <br/> works only in the current article. There is another <br/> full-text search function for the whole file in the menu.</html>";
			searchField.setToolTipText(searchToolTip);
			Icon searchPreviousIcon = new ImageIcon(getClass().getResource("/icons/resultset_previous.png"));
			Icon searchNextIcon = new ImageIcon(getClass().getResource("/icons/resultset_next.png"));
			Icon searchRevertIcon = new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png"));
			searchNext = new JButton(searchNextIcon);
			searchNext.setToolTipText(searchToolTip);
			searchPrevious = new JButton(searchPreviousIcon);
			searchPrevious.setToolTipText(searchToolTip);
			searchRevert = new JButton(searchRevertIcon);
			searchRevert.setToolTipText(searchToolTip);
			searchPrevious.setPreferredSize(new Dimension(18, 18));
			searchNext.setPreferredSize(new Dimension(18, 18));
			searchRevert.setPreferredSize(new Dimension(18, 18));
			JPanel searchButtonPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gb = new GridBagConstraints();
			gb.insets = new Insets(0,0,0,0);
			gb.gridx = 0;
			gb.gridy = 0;
			searchButtonPanel.add(searchPrevious, gb);
			gb.gridx = 1;
			searchButtonPanel.add(searchNext, gb);
			gb.gridx = 2;
			searchButtonPanel.add(searchRevert, gb);
			this.add(searchField, BorderLayout.CENTER);
			this.add(searchButtonPanel, BorderLayout.EAST);
			
			searchRevert.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					searchField.setText("");
				}
			});
			
			matches = new ArrayList<Tupel>();
			
			
			ActionListener searchListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == searchPrevious) {
						continueSearch(false);
					} else if (e.getSource() == searchNext) {
						continueSearch(true);
					}
					
				}
			};
			
			searchPrevious.addActionListener(searchListener);
			searchNext.addActionListener(searchListener);
			
			KeyAdapter enter = new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						continueSearch(true);
					}
				}
			};
			searchField.addKeyListener (enter);
		}

		public void continueSearch(boolean forward) {
			if (dc.ac.getRowCount() == 0) {
				//there is no article where text could be found...
			} else {
				
				int matchStart = 0;
				int matchEnd = 0;
				
				//create list of matches in current article
				matches.clear();
				String term = searchField.getText();
				if (articleTable.getSelectedRow() == -1) {
					articleTable.changeSelection(0, 0, false, false);
				}
				String searchText = dc.ac.get(articleTable.getSelectedRow()).getText();
				searchText = stripHtmlTags(searchText, false);
				Pattern p = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(searchText);
				while (m.find()) {
					matches.add(new Tupel(m.start(), m.end()));
				}
				
				//determine cursor position from which to start searching
				int searchPosition;
				int selectionStart = textWindow.getSelectionStart();
				int selectionEnd = textWindow.getSelectionEnd();
				int selectionLength = selectionEnd - selectionStart;
				if (selectionLength > 0) {
					searchPosition = selectionStart;
				} else {
					if (textWindow.getCaretPosition() > 0) {
						searchPosition = textWindow.getCaretPosition();
					} else {
						searchPosition = 1;
					}
				}
				
				//search next or previous occurrence in the list
				if (forward == false) {
					for (int i = matches.size() - 1; i >= 0; i--) {
						if (matches.get(i).getEndValue() < searchPosition) {
							matchStart = matches.get(i).getStartValue();
							matchEnd = matches.get(i).getEndValue();
							break;
						}
						if (matches.size() > 0) {
							matchStart = matches.get(0).getStartValue();
							matchEnd = matches.get(0).getEndValue();
						}
					}
				} else if (forward == true) {
					for (int i = 0; i < matches.size(); i++) {
						if (matches.get(i).getStartValue() > searchPosition) {
							matchStart = matches.get(i).getStartValue();
							matchEnd = matches.get(i).getEndValue();
							break;
						}
						if (matches.size() > 0) {
							matchStart = matches.get(matches.size() - 1).getStartValue();
							matchEnd = matches.get(matches.size() - 1).getEndValue();
						}
					}
				}
				
				//select the match in the text window
				textWindow.grabFocus();
				textWindow.select(matchStart + 1, matchEnd + 1);
			}
		}
	}
	
	class DocStats extends JPanel {
		
		JTextArea tf;
		
		public DocStats() {
			this.setLayout(new BorderLayout());
			
			tf = new JTextArea(5, 12);
			tf.setEditable(false);
			ImageIcon clearIcon = new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png"));
			JButton clearButton = new JButton("clear", clearIcon);
			clearButton.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clear();
				}
			});
			ImageIcon refreshIcon = new ImageIcon(getClass().getResource("/icons/chart_bar.png"));
			JButton refreshButton = new JButton("refresh", refreshIcon);
			refreshButton.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					computeStats();
				}
			});
			
			
			this.add(tf, BorderLayout.CENTER);
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(clearButton);
			buttonPanel.add(refreshButton);
			this.add(buttonPanel, BorderLayout.SOUTH);
		}
		
		public void computeStats() {
			int numArticles = dc.ac.getRowCount();
			int numStatements = dc.sc.getRowCount();
			updateLists();
			int numOrg = om.getActors().size();
			int numPers = pm.getActors().size();
			int numCat = catListe.size();
			tf.setEditable(true);
			tf.setText("Articles: " + numArticles + "\n"
					+ "Statements: " + numStatements + "\n"
					+ "Organizations: " + numOrg + "\n"
					+ "Persons: " + numPers + "\n"
					+ "Categories: " + numCat
			);
			tf.setEditable(false);
		}
		
		public void clear() {
			tf.setEditable(true);
			tf.setText("");
			tf.setEditable(false);
		}
	}
	
	private void statementTable() {
		
		dc.sc = new StatementContainer();
		statementTable = new JTable( dc.sc );
		statementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statementTableScrollPane = new JScrollPane(statementTable);
		statementTableScrollPane.setPreferredSize(new Dimension(200, 360));
		statementTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 30 );
		statementTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 170 );
		
		statementTable.getTableHeader().setReorderingAllowed( false );
		statementTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		setRowSorterEnabled(true);
		
		statementFilter = new StatementFilter();
		
		statementPanel = new JPanel(new BorderLayout());
		statementPanel.add(statementTableScrollPane, BorderLayout.CENTER);
		statementPanel.add(statementFilter, BorderLayout.SOUTH);
		
		statementTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = -1;
				row = statementTable.rowAtPoint(e.getPoint());
				row = statementTable.convertRowIndexToModel(row);
				
				if (row > -1) {
					int id = dc.sc.get(row).getId();
					if (id != -1) {
						highlightStatementInText(id);
						
		    			//show popup
		    			if (editMode.isSelected()) {
		    				new ToolTip(id);
		    			}
					}
				}
			}
		});
		
		JXTaskPaneContainer tpc = new JXTaskPaneContainer();
		taskPaneScroller = new JScrollPane(tpc);
		taskPaneScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		sp = new SearchPanel();
		JXTaskPane searchTaskPane = new JXTaskPane();
		ImageIcon findIcon = new ImageIcon(getClass().getResource("/icons/find.png"));
		searchTaskPane.setName("Search within article");
		searchTaskPane.setTitle("Search within article");
		searchTaskPane.setIcon(findIcon);
		((Container)tpc).add(searchTaskPane);
        searchTaskPane.add(sp);
		
		JXTaskPane statementTaskPane = new JXTaskPane();
		ImageIcon statementIcon = new ImageIcon(getClass().getResource("/icons/comments.png"));
		statementTaskPane.setName("Statements");
		statementTaskPane.setTitle("Statements");
		statementTaskPane.setIcon(statementIcon);
        ((Container)tpc).add(statementTaskPane);
        statementTaskPane.add(statementPanel);
		
        contradictionReporter = new ContradictionReporter();
        JXTaskPane contradictionTaskPane = new JXTaskPane();
        ImageIcon contradictionIcon = new ImageIcon(getClass().getResource("/icons/group.png"));
        contradictionTaskPane.setName("Find self-contradictions");
        contradictionTaskPane.setTitle("Find self-contradictions");
        contradictionTaskPane.setIcon(contradictionIcon);
        contradictionTaskPane.setCollapsed(true);
        contradictionTaskPane.add(contradictionReporter);
        ((Container)tpc).add(contradictionTaskPane);

        JXTaskPane highlighterTaskPane = new JXTaskPane();
        regexManager = new RegexManager();
        ImageIcon tableEditIcon = new ImageIcon(getClass().getResource("/icons/color_swatch.png"));
        highlighterTaskPane.setName("Regex highlighter");
        highlighterTaskPane.setTitle("Regex highlighter");
        highlighterTaskPane.setIcon(tableEditIcon);
        highlighterTaskPane.setCollapsed(true);
        highlighterTaskPane.add(regexManager);
        ((Container)tpc).add(highlighterTaskPane);
        
        JXTaskPane docStatisticsTaskPane = new JXTaskPane();
        docStats = new DocStats();
        ImageIcon docStatisticsIcon = new ImageIcon(getClass().getResource("/icons/chart_bar.png"));
        docStatisticsTaskPane.setName("Document summary statistics");
        docStatisticsTaskPane.setTitle("Document summary statistics");
        docStatisticsTaskPane.setIcon(docStatisticsIcon);
        docStatisticsTaskPane.setCollapsed(true);
        docStatisticsTaskPane.add(docStats);
        ((Container)tpc).add(docStatisticsTaskPane);
	}
	
	public void highlightStatementInText(int id) {
		String title = dc.sc.getById(id).getArticleTitle();
		int index = -1;
		for (int i = 0; i < dc.ac.getRowCount(); i++) {
			if (dc.ac.get(i).getTitle().equals(title)) {
				index = i;
			}
		}
		int startCaret = dc.sc.getById(id).getStart();
		int stopCaret = dc.sc.getById(id).getStop();
		articleTable.changeSelection(index, 0, false, false);
		textWindow.grabFocus();
		textWindow.setSelectionStart(startCaret);
		textWindow.setSelectionEnd(stopCaret);
	}
	
	public void setRowSorterEnabled(boolean enabled) {
		if (enabled == true) {
			sorter = new TableRowSorter<StatementContainer>(dc.sc) {
				public void toggleSortOrder(int i) {
					//leave blank; the overwritten method makes the table unsortable
				}
			};
	        statementTable.setRowSorter(sorter);
		} else {
			statementFilter.allMenuItem.setSelected(true);
    		statementTable.setRowSorter(null);
		}
	}
	
	/**
	 * Status bar.
	 */
	private void status() {
		statusBar = new JPanel( new BorderLayout() );
		currentFileLabel = new JLabel("Current file: none");
		statusBar.add(currentFileLabel, BorderLayout.WEST);
		
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
			dispose();
		} else {
			int question = JOptionPane.showConfirmDialog(Dna.
					this, "Would you like to save your work?", 
					"Save?", JOptionPane.YES_NO_CANCEL_OPTION);
			if ( question == JOptionPane.YES_OPTION ) {
				if ( dc.currentFileName.equals("") ) {
					saveAsDialog();
				} else {
					new SaveDnaFile( dc.currentFileName );
				}
				dispose();
			} else if ( question == JOptionPane.NO_OPTION ) {
				dispose();
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
			System.err.println(ioe);
		}
		
		if (htmlLineSep == true) {
			strippedContents = strippedContents.replaceAll("\\[br\\]", "<br/>");
		} else {
			strippedContents = strippedContents.replaceAll("\\[br\\]", "\n");
		}
		
		return strippedContents;
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
				dc.currentFileName = file.getPath();
			} else {
				dc.currentFileName = file.getPath() + ".dna";
			}
			new SaveDnaFile( dc.currentFileName );
			currentFileLabel.setText( "Current file: " + dc.currentFileName );
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
			dc.currentFileName = "";
			currentFileLabel.setText("Current file: none");
		} else {
			int question = JOptionPane.showConfirmDialog(Dna.this, 
					"Would you like to save your work?", "Save?", JOptionPane.
					YES_NO_CANCEL_OPTION);
			if ( question == JOptionPane.YES_OPTION ) {
				if ( dc.currentFileName.equals("") ) {
					saveAsDialog();
				} else {
					new SaveDnaFile( dc.currentFileName );
				}
				clearSpace();
				System.out.println("File was closed.");
			} else if ( question == JOptionPane.NO_OPTION ) {
				clearSpace();
				System.out.println("File was closed.");
			} else {
				System.out.println("Canceled closing file.");
			}
		}
	}

	/**
	 * Helper method for closeDnaFile(). Clears the article table, statement table and status bar.
	 */
	public void clearSpace() {
		dc.clear();
		om.clear();
		pm.clear();
		currentFileLabel.setText("Current file: none");
		textWindow.setEnabled(false);
		regexManager.clear();
		contradictionReporter.clearTree();
		searchWindow.tableModel.clear();
		docStats.clear();
	}
	
	/**
	 * Table for title and date of article entries. Interacts with the text window.
	 */
	private void articleTable() {
		
		//create table, assign model and create scroll pane
		articleTable = new JTable( dc.ac );
		articleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableScrollPane = new JScrollPane(articleTable);
		tableScrollPane.setPreferredSize(new Dimension(700, 100));
		articleTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 620 );
		articleTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 80 );
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
					textWindow.setText(dc.ac.get(selectedRow).getText());
					textWindow.setEnabled(true);
				}
				if (statementFilter.articleMenuItem.isSelected()) {
					statementFilter.articleFilter();
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
		textScrollPane.setPreferredSize(new Dimension(700, 500));
		textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textWindow.setEnabled(false);
		
		centralTextPanel = new JPanel(new BorderLayout());
		collapsiblePane = new JXCollapsiblePane(); 
		collapsiblePane.setName("Central Text Panel");
		centralTextPanel.add(textScrollPane, BorderLayout.CENTER);
		centralTextPanel.add(collapsiblePane, BorderLayout.SOUTH);
		
		containerStack = new CardLayout();
        collapsiblePane.setLayout(containerStack);
		
		searchWindow = new SearchWindow();
		collapsiblePane.add(searchWindow, "Full-text search");
		om = new ActorManager(dc.ot);
		collapsiblePane.add(om, "Organizations");
		collapsiblePane.setCollapsed(true);
		pm = new ActorManager(dc.pt);
		collapsiblePane.add(pm, "Persons");
		collapsiblePane.setCollapsed(true);
		
		searchPanelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				containerStack.show(collapsiblePane.getContentPane(), "Full-text search");
				collapsiblePane.setCollapsed(false);
				searchPanelButton.setEnabled(false);
				orgManagerButton.setEnabled(true);
				persManagerButton.setEnabled(true);
			}
		});
		
		orgManagerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				containerStack.show(collapsiblePane.getContentPane(), "Organizations");
				collapsiblePane.setCollapsed(false);
				orgManagerButton.setEnabled(false);
				searchPanelButton.setEnabled(true);
				persManagerButton.setEnabled(true);
			}
		});

		persManagerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				containerStack.show(collapsiblePane.getContentPane(), "Persons");
				collapsiblePane.setCollapsed(false);
				orgManagerButton.setEnabled(true);
				searchPanelButton.setEnabled(true);
				persManagerButton.setEnabled(false);
			}
		});
		
		toggleBottomButton.addActionListener(collapsiblePane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION));
	}
	
	public void paintStatements() {
		int row = articleTable.getSelectedRow();
		if (row > -1) {
			String title = dc.ac.get(row).getTitle();
			String textToStrip = dc.ac.get(row).getText();
			String textOriginal = dc.ac.get(row).getText();
			textToStrip = stripHtmlTags(textToStrip, false);
			textWindow.setText(textOriginal);
			
			//remove all initial foreground color styles
			int initialStart = 0;
			int initialEnd = textWindow.getText().length();
			MutableAttributeSet initialStyle = new SimpleAttributeSet();
			StyleConstants.setForeground(initialStyle, Color.BLACK);
			doc.setCharacterAttributes(initialStart, initialEnd-initialStart, initialStyle, false);
			
			//add yellow background color to statements
			for (int i = 0; i < dc.sc.size(); i++) {
				if (dc.sc.get(i).getArticleTitle().equals(title)) {
					int start = dc.sc.get(i).getStart();
					int end = dc.sc.get(i).getStop();
					MutableAttributeSet style = new SimpleAttributeSet();
					StyleConstants.setBackground(style, Color.YELLOW);
					doc.setCharacterAttributes(start, end-start, style, false);
				}
			}
			
			//color user's regular expressions
			for (int i = 0; i < dc.regexTerms.size(); i++) {
				String pattern = dc.regexTerms.get(i).getPattern();
				Color color = dc.regexTerms.get(i).getColor();
				Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(textToStrip);
				while(m.find()) {
					int start = m.start() + 1;
					int end = m.end() + 1;
					MutableAttributeSet style = new SimpleAttributeSet();
					StyleConstants.setForeground(style, color);
					doc.setCharacterAttributes(start, end-start, style, false);
				}
			}
		}
	}
	
	/**
	 * Menu bar.
	 */
	private void menu() {
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		articleMenu = new JMenu("Article");
		menuBar.add(articleMenu);
		exportMenu = new JMenu("Export");
		menuBar.add(exportMenu);
		extrasMenu = new JMenu("Extras");
		menuBar.add(extrasMenu);
		
		//File menu: open DNA file
		Icon openIcon = new ImageIcon(getClass().getResource("/icons/folder.png"));
		JMenuItem openMenuItem = new JMenuItem("Open DNA file...", openIcon);
		openMenuItem.addActionListener(new ActionListener() {
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
					try {
						Thread open = new Thread( new OpenDnaFile( file.getPath() ), "Open DNA file" );
						open.start();
					} catch (OutOfMemoryError ome) {
						clearSpace();
						System.err.println("Out of memory. File has been closed. Please start Java with\nthe -Xmx1024M option, where '1024' is the space you want\nto allocate to DNA. The manual provides further details.");
						JOptionPane.showMessageDialog(Dna.this, "Out of memory. File has been closed. Please start Java with\nthe -Xmx1024M option, where '1024' is the space you want\nto allocate to DNA. The manual provides further details.");
					}
				}
			}
		});
		fileMenu.add(openMenuItem);
		
		
		//Article menu: import articles
		Icon importIcon = new ImageIcon(getClass().getResource("/icons/page_white_get.png"));
		JMenuItem importArticles = new JMenuItem("Import articles...", importIcon);
		importArticles.setToolTipText( "import articles from DNA file..." );
		articleMenu.add(importArticles);
		importArticles.addActionListener(new ActionListener() {
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

		//File menu: save as...
		Icon saveAsIcon = new ImageIcon(getClass().getResource("/icons/disk_multiple.png"));
		JMenuItem saveAs = new JMenuItem("Save as...", saveAsIcon);
		saveAs.setToolTipText( "save as DNA file..." );
		fileMenu.add(saveAs);
		saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsDialog();
			}
		});

		//File menu: save
		Icon saveIcon = new ImageIcon(getClass().getResource("/icons/disk.png"));
		JMenuItem save = new JMenuItem("Save", saveIcon);
		save.setToolTipText( "save current file" );
		fileMenu.add(save);
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ( dc.currentFileName.equals("") ) {
					saveAsDialog();                
				} else {
					new SaveDnaFile( dc.currentFileName );
				}
			}
		});
		
		//File menu: close current file
		Icon closeIcon = new ImageIcon( getClass().getResource("/icons/cancel.png") );
		JMenuItem closeFile = new JMenuItem("Close DNA file", closeIcon);
		closeFile.setToolTipText( "close current file" );
		fileMenu.add(closeFile);
		closeFile.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDnaFile();
			}
		});

		//File menu: exit
		Icon exitIcon = new ImageIcon( getClass().getResource("/icons/door_out.png") );
		JMenuItem exit = new JMenuItem("Exit", exitIcon);
		exit.setToolTipText( "quit DNA" );
		fileMenu.add(exit);
		exit.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDnaFile();
				dispose();
			}
		});
		
		//Article menu: add new article
		Icon newArticleIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
		JMenuItem newArticleButton = new JMenuItem("Add new article...", newArticleIcon);
		newArticleButton.setToolTipText( "add new article..." );
		articleMenu.add(newArticleButton);
		newArticleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NewArticle();
			}
		});
		
		//Article menu: delete selected article
		Icon deleteRowIcon = new ImageIcon(getClass().getResource("/icons/table_delete.png"));
		JMenuItem cmdDelete = new JMenuItem("Delete selected article", deleteRowIcon);
		cmdDelete.setToolTipText( "delete selected row" );
		articleMenu.add(cmdDelete);
		cmdDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dc.ac.getRowCount() > 0) {
					int dialog = JOptionPane.showConfirmDialog(Dna.this, "Are you sure you want to delete the selected article?", "Confirmation required", JOptionPane.YES_NO_OPTION);
					int row = articleTable.getSelectedRow();
					if (row != -1 && dialog == 0) {
						Dna.mainProgram.searchWindow.tableModel.clear();
						for (int i = dc.sc.getRowCount() - 1; i > -1; i--) {
							if (dc.sc.get(i).getArticleTitle().equals(dc.ac.get(articleTable.getSelectedRow()).getTitle())) {
								int id = dc.sc.get(i).getId();
								dc.sc.removeStatement(id);
								contradictionReporter.clearTree();
								searchWindow.tableModel.clear();
							}
						}
						dc.ac.remove(row);
						if (row > 0) {
							articleTable.changeSelection(row-1, 0, false, false);
						} else {
							articleTable.changeSelection(0, 0, false, false);
						}
						if (articleTable.getRowCount() < 1) {
							textWindow.setEnabled(false);
						}
					}
				}
			}
		});
		
		//Article menu: change name or date of current article
		Icon renameIcon = new ImageIcon(getClass().getResource("/icons/table_edit.png"));
		JMenuItem renameButton = new JMenuItem("Rename selected article...", renameIcon);
		renameButton.setToolTipText( "rename current article or change its date..." );
		articleMenu.add(renameButton);
		renameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new RenameArticle();
			}
		});

		//Extras menu: edit mode
		editMode = new JCheckBoxMenuItem("Edit statements upon click", true);
		extrasMenu.add(editMode);
		
		//Extras menu: use color rendering for tool tips
		toolTipColors = new JCheckBoxMenuItem("Use colors for actor selection", true);
		toolTipColors.setToolTipText("use the colors specified in the actor properties for statement combo boxes");
		extrasMenu.add(toolTipColors);
		
		//Extras menu: recode window
		Icon recodeIcon = new ImageIcon(getClass().getResource("/icons/database_go.png"));
		JMenuItem recodeButton = new JMenuItem("Recode statements...", recodeIcon);
		recodeButton.setToolTipText( "rename or remove actors or categories..." );
		extrasMenu.add(recodeButton);
		recodeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dc.sc.size() > 0) {
					new Recode();
				}
			}
		});
		
		//Export menu: statistics component
		Icon statisticsIcon = new ImageIcon(getClass().getResource("/icons/chart_curve.png"));
		JMenuItem statisticsButton = new JMenuItem("Time series statistics...", statisticsIcon);
		statisticsButton.setToolTipText( "export time series statistics of statement frequencies as CSV file..." );
		exportMenu.add(statisticsButton);
		statisticsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new TimeSeriesWindow(dc.sc);
			}
		});
		
		//Export menu: statistics component
		Icon statementsIcon = new ImageIcon(getClass().getResource("/icons/comments.png"));
		JMenuItem statementsButton = new JMenuItem("List of statements", statementsIcon);
		statisticsButton.setToolTipText( "export list of statements including their details as CSV file..." );
		exportMenu.add(statementsButton);
		statementsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				extension = ".csv";
				description = "Comma-separated values (*.csv)";
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(extension) || f.isDirectory();
					}
					public String getDescription() {
						return description;
					}
				});
				int returnVal = fc.showSaveDialog(Dna.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String filename = new String(file.getPath());
					if ( !file.getPath().endsWith(extension) ) {
						filename = filename + extension;
					}
					try {
						System.out.println("Exporting list of statements... ");
						BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
						br.write("id;article title;start position;end position;date;person;organization;category;agreement;text");
						for (int i = 0; i < dc.sc.size(); i++) {
							String agree = "";
							if (dc.sc.get(i).getAgreement().equals("yes")) {
								agree = "yes";
							} else if (dc.sc.get(i).getAgreement().equals("no")) {
								agree = "no";
							}
							br.newLine();
							GregorianCalendar g = new GregorianCalendar();
							g.setTime(dc.sc.get(i).getDate());
							br.write(dc.sc.get(i).getId() + ";"
									+ dc.sc.get(i).getArticleTitle().replaceAll(";", ":").replaceAll("\n", "") + ";"
									+ dc.sc.get(i).getStart() + ";"
									+ dc.sc.get(i).getStop() + ";"
									+ g.get(Calendar.YEAR) + "-" 
									+ (g.get(Calendar.MONTH)+1) + "-" 
									+ g.get(Calendar.DATE) + ";"
									+ dc.sc.get(i).getPerson().replaceAll(";", ":").replaceAll("\n", "") + ";" 
									+ dc.sc.get(i).getOrganization().replaceAll(";", ":").replaceAll("\n", "") + ";"
									+ dc.sc.get(i).getCategory().replaceAll(";", ":").replaceAll("\n", "") + ";"
									+ agree + ";"
									+ dc.sc.get(i).getText().replaceAll(";", ":").replaceAll("\n", ""));
						}
						br.close();
						System.out.println("File has been exported to \"" + filename + "\".");
					} catch (IOException e1) {
						System.err.println("Error while saving CSV statement list.");
					}
				} else {
					System.out.println("Export cancelled.");
				}
			}
		});
		
		//Export menu: organization attributes and person attributes
		Icon organizationIcon = new ImageIcon(getClass().getResource("/icons/group.png"));
		Icon personIcon = new ImageIcon(getClass().getResource("/icons/user.png"));
		orgExportButton = new JMenuItem("Attributes of organizations", organizationIcon);
		persExportButton = new JMenuItem("Attributes of persons", personIcon);
		orgExportButton.setToolTipText("export the organizations and their properties to a CSV file...");
		persExportButton.setToolTipText("export the persons and their properties to a CSV file...");
		exportMenu.add(persExportButton);
		exportMenu.add(orgExportButton);
		
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				extension = ".csv";
				description = "Comma-separated values (*.csv)";
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(extension) || f.isDirectory();
					}
					public String getDescription() {
						return description;
					}
				});
				int returnVal = fc.showSaveDialog(Dna.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String filename = new String(file.getPath());
					if ( !file.getPath().endsWith(extension) ) {
						filename = filename + extension;
					}
					if (e.getSource() == orgExportButton) {
						om.exportToCsv(filename);
					} else if (e.getSource() == persExportButton) {
						pm.exportToCsv(filename);
					}
				} else {
					System.out.println("Export cancelled.");
				}
			}
		};
		
		orgExportButton.addActionListener(al);
		persExportButton.addActionListener(al);
		
		//Export menu: network export
		Icon expIcon = new ImageIcon(getClass().getResource("/icons/chart_organisation.png"));
		JMenuItem expButton = new JMenuItem("Network export...", expIcon);
		expButton.setToolTipText( "export to network..." );
		exportMenu.add(expButton);
		expButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export = new ExportWindow();
			}
		});
		
		/*
		 * Put in comments because the corresponding function is also disabled.
		 * 
		//Extras menu: import actor attributes from another DNA file
		Icon actorImportIcon = new ImageIcon(getClass().getResource("/icons/page_white_get.png"));
		JMenuItem actorImportButton = new JMenuItem("Import actor attributes", actorImportIcon);
		actorImportButton.setToolTipText("import persons and organizations along with their \nattribute data from another .dna file...");
		extrasMenu.add(actorImportButton);
		
		ActionListener ail = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				extension = ".dna";
				description = "Discourse Network Analyzer files (*.dna)";
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(extension) || f.isDirectory();
					}
					public String getDescription() {
						return description;
					}
				});
				int returnVal = fc.showSaveDialog(Dna.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String filename = new String(file.getPath());
					if ( !file.getPath().endsWith(extension) ) {
						filename = filename + extension;
					}
					importActorAttributes(filename); //do it!
				} else {
					System.out.println("Export cancelled.");
				}
			}
		};
		actorImportButton.addActionListener(ail);
		*/
		
		extrasMenu.addSeparator();
		
		//Extras menu: toggle bottom bar
		Icon bottomBarIcon = new ImageIcon(getClass().getResource("/icons/application_form.png"));
		toggleBottomButton = new JMenuItem("Toggle bottom bar (show/hide)", bottomBarIcon); 
		extrasMenu.add(toggleBottomButton);
		
		//Extras menu: show full-text search in bottom bar
		Icon searchPanelIcon = new ImageIcon(getClass().getResource("/icons/find.png"));
		searchPanelButton = new JMenuItem("Show full-text search in bottom bar", searchPanelIcon);
		searchPanelButton.setToolTipText( "a full-text search function across all articles..." );
		extrasMenu.add(searchPanelButton);
		searchPanelButton.setEnabled(false);
		
		//Extras menu: show person attributes in bottom bar
		persManagerButton = new JMenuItem("Show persons in bottom bar", personIcon);
		persManagerButton.setToolTipText( "view or set attributes of persons" );
		extrasMenu.add(persManagerButton);
		
		//Extras menu: show organization attributes in bottom bar
		orgManagerButton = new JMenuItem("Show organizations in bottom bar", organizationIcon);
		orgManagerButton.setToolTipText( "view or set attributes of organizations" );
		extrasMenu.add(orgManagerButton);
		
		extrasMenu.addSeparator();
		
		//Extras menu: about DNA
		Icon aboutIcon = new ImageIcon(getClass().getResource("/icons/information.png"));
		JMenuItem aboutButton = new JMenuItem("About DNA...", aboutIcon);
		aboutButton.setToolTipText( "about DNA" );
		extrasMenu.add(aboutButton);
		aboutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new AboutWindow();
			}
		});
	}
	
	public void updateLists() {
		catListe.clear();
		
		for (int i = 0; i < dc.sc.getRowCount(); i++) {
			if(!pm.contains(dc.sc.get(i).getPerson()) && !dc.sc.get(i).getPerson().equals("")) {
				pm.add(new Actor(dc.sc.get(i).getPerson(), true));
			}
			if(!om.contains(dc.sc.get(i).getOrganization()) && !dc.sc.get(i).getOrganization().equals("")) {
				om.add(new Actor(dc.sc.get(i).getOrganization(), true));
			}
			if(!catListe.contains(dc.sc.get(i).getCategory())) {
				catListe.add(dc.sc.get(i).getCategory());
			}
		}
		Collections.sort(catListe);
	}
	
	public int getIdByClickPosition(int clickPosition) {
		int id = -1;
		for (int i = 0; i < dc.sc.getRowCount(); i++) { 
			int row = articleTable.getSelectedRow();
			if (dc.sc.get(i).start < clickPosition && dc.sc.get(i).stop > clickPosition && dc.sc.get(i).articleTitle.equals(dc.ac.get(row).getTitle())) {
				id = dc.sc.get(i).getId();
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
				int viewId = statementTable.convertRowIndexToView(dc.sc.getIndexById(id));
				if (viewId == -1) {
					statementTable.clearSelection();
				} else {
					statementTable.changeSelection(viewId, 0, false, false);
				}
				int startIndex = -1;
				int stopIndex = -1;
				startIndex = dc.sc.getById(id).getStart();
				stopIndex = dc.sc.getById(id).getStop();
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
	 * Context menu from which a statement can be inserted into the text.
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
				try {
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
					
					Date date = dc.ac.get(articleTable.getSelectedRow()).getDate();
					
					String articleTitle = dc.ac.get(articleTable.getSelectedRow()).getTitle();
					int statementId = dc.sc.getFirstUnusedId();
					try {
						dc.sc.addStatement(new Statement(statementId, selectionStart, selectionEnd, date, selection, articleTitle, "", "", "", "yes"));
					} catch (DuplicateStatementIdException e1) {
						e1.printStackTrace();
					}
					System.out.println("Added a statement with ID " + statementId + " starting at offset " + selectionStart + " and ending at " + selectionEnd);
					paintStatements();
					textWindow.setCaretPosition(selectionStart);
				} catch (OutOfMemoryError ome) {
					System.err.println("Out of memory. Statement has not been inserted. Please restart Java\nwith the -Xmx1024M option, where '1024' is the space you want\nto allocate to DNA. The manual provides further details.");
					JOptionPane.showMessageDialog(Dna.this, "Out of memory. Statement has not been inserted. Please restart Java\nwith the -Xmx1024M option, where '1024' is the space you want\nto allocate to DNA. The manual provides further details.");
				}
			}
		});
	}
	
	public void removeStatement(int id) {
		int currentArticle = articleTable.getSelectedRow();
		if (currentArticle > -1) {
			if (id != -1) {
				int caretPosition = dc.sc.getById(id).getStart();
				dc.sc.removeStatement(id);
				contradictionReporter.clearTree();
				searchWindow.tableModel.clear();
				articleTable.changeSelection(currentArticle, 0, false, false);
				paintStatements();
				textWindow.setCaretPosition(caretPosition);
				System.out.println("Statement with ID " + id + " has been removed.");
			}
		}
	}

	private class ActorComboBoxRenderer extends JLabel implements ListCellRenderer {
		
		ActorManager am;
		
		public ActorComboBoxRenderer (ActorManager am) {
			this.am = am;
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel lab = new JLabel((String) value);
			lab.setOpaque(true);
			
			//define colors
			Color sel = new Color( 184, 207, 229 );
			Color red = new Color(250, 175, 175);
			Color redDark = new Color(200, 100, 100);
			Color fg;
			fg = this.am.getColor((String)value);
			if (fg.equals(Color.white)) {
				fg = Color.black;
			}
			
			//color the comboBox entries
			if (this.am.getActor((String)value).appearsInDataSet()) {
				if (isSelected) {
					lab.setBackground(sel);
				} else {
					lab.setBackground(Color.white);
				}
				lab.setForeground(fg);
			} else {
				if (isSelected) {
					lab.setBackground(sel);
					lab.setForeground(redDark);
				} else {
					lab.setBackground(red);
					lab.setForeground(fg);
				}
			}
			
			return lab;
		}
	}
		
	public class ToolTip extends JFrame {
		
		Container c;
		Point point;
		int statementId;
		JPanel gridBagPanel;
		GridBagConstraints gbc;
		JComboBox person, organization, category, agreement;
		JTextField idField, startPos, endPos;
		String orgAtOpen, persAtOpen;
		
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
			startPos = new JTextField(new Integer(dc.sc.getById(statementId).getStart()).toString());
			startPos.setEditable(false);
			
			JLabel ePosLabel = new JLabel("end position");
			endPos = new JTextField(new Integer(dc.sc.getById(statementId).getStop()).toString());
			endPos.setEditable(false);
			
			JLabel agreeLabel = new JLabel("agreement", JLabel.TRAILING);
			agreement = new JComboBox(agListe);
			agreement.setEditable(false);
			agreement.setSelectedItem(dc.sc.getById(statementId).getAgreement());
			
			JLabel idLabel = new JLabel("ID");
			idField = new JTextField(new Integer(dc.sc.getById(statementId).getId()).toString());
			idField.setEditable(false);
			
			persAtOpen = dc.sc.getById(statementId).getPerson();
			JLabel persLabel = new JLabel("person", JLabel.TRAILING);
			person = new JComboBox(pm.getActorNames().toArray());
			person.setEditable(true);
			ActorComboBoxRenderer persRenderer = new ActorComboBoxRenderer(pm);
			if (toolTipColors.isSelected()) {
				person.setRenderer(persRenderer);
			}
			person.setSelectedItem(dc.sc.getById(statementId).getPerson());
			AutoCompleteDecorator.decorate(person);
			
			orgAtOpen = dc.sc.getById(statementId).getOrganization();
			JLabel orgLabel = new JLabel("organization", JLabel.TRAILING);
			organization = new JComboBox(om.getActorNames().toArray());
			organization.setEditable(true);
			ActorComboBoxRenderer orgRenderer = new ActorComboBoxRenderer(om);
			if (toolTipColors.isSelected()) {
				organization.setRenderer(orgRenderer);
			}
			organization.setSelectedItem(dc.sc.getById(statementId).getOrganization());
			AutoCompleteDecorator.decorate(organization);
			
			JLabel catLabel = new JLabel("category", JLabel.TRAILING);
			category = new JComboBox(catListe.toArray());
			category.setEditable(true);
			category.setSelectedItem(dc.sc.getById(statementId).getCategory());
			AutoCompleteDecorator.decorate(category);
			
			ImageIcon removeIcon = new ImageIcon(getClass().getResource("/icons/cross.png"));
			JButton remove = new JButton("remove", removeIcon);
			remove.setToolTipText("completely remove the whole statement but keep the text");
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int question = JOptionPane.showConfirmDialog(Dna.this, "Are you sure you want to remove this statement?", 
							"Remove?", JOptionPane.YES_NO_OPTION);
					if (question == 0) {
						
						boolean orgStillExists = false;
						boolean persStillExists = false;
						for (int i = 0; i < dc.sc.size(); i++) {
							if (dc.sc.get(i).getOrganization().equals(orgAtOpen) && i != dc.sc.getIndexById(statementId)) {
								orgStillExists = true;
							}
							if (dc.sc.get(i).getPerson().equals(persAtOpen) && i != dc.sc.getIndexById(statementId)) {
								persStillExists = true;
							}
						}
						
						try {
							@SuppressWarnings("unused")
							String aliasOrg = om.getActor(orgAtOpen).getAlias();
							@SuppressWarnings("unused")
							String typeOrg = om.getActor(orgAtOpen).getType();
							@SuppressWarnings("unused")
							String noteOrg = om.getActor(orgAtOpen).getNote();
							if (orgStillExists == false) {
								om.getActor(orgAtOpen).setAppearsInDataSet(false);
								om.repaint();
							}
						} catch (NullPointerException np) {
							if (orgStillExists == false) {
								om.remove(orgAtOpen);
							}
						}
						
						try {
							@SuppressWarnings("unused")
							String aliasPers = pm.getActor(persAtOpen).getAlias();
							@SuppressWarnings("unused")
							String typePers = pm.getActor(persAtOpen).getType();
							@SuppressWarnings("unused")
							String notePers = pm.getActor(persAtOpen).getNote();
							if (persStillExists == false) {
								pm.getActor(persAtOpen).setAppearsInDataSet(false);
								pm.repaint();
							}
						} catch (NullPointerException np) {
							if (persStillExists == false) {
								pm.remove(persAtOpen);
							}
						}
						
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
					dispose();
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
			
			addWindowFocusListener(new WindowAdapter() {
				public void windowLostFocus(WindowEvent e) {
					try {
						String p = (String)person.getEditor().getItem();
						dc.sc.getById(statementId).setPerson(p);
						String o = (String)organization.getEditor().getItem();
						dc.sc.getById(statementId).setOrganization(o);
						
						if (!o.equals(orgAtOpen) && !orgAtOpen.equals("")) {
							boolean orgStillExists = false;
							for (int i = 0; i < dc.sc.size(); i++) {
								if (dc.sc.get(i).getOrganization().equals(orgAtOpen)) {
									orgStillExists = true;
									break;
								}
							}
							String aliasOrg = om.getActor(orgAtOpen).getAlias();
							String typeOrg = om.getActor(orgAtOpen).getType();
							String noteOrg = om.getActor(orgAtOpen).getNote();
							if (orgStillExists == false && typeOrg == null && aliasOrg == null && noteOrg == null) {
								om.remove(orgAtOpen);
							} else if (orgStillExists == false) {
								om.getActor(orgAtOpen).setAppearsInDataSet(false);
								om.repaint();
							}
						}
						if (!om.contains(o)) {
							om.add(new Actor(o, true));
						} else {
							om.getActor(o).setAppearsInDataSet(true);
							om.repaint();
						}
						
						if (!p.equals(persAtOpen) && !persAtOpen.equals("")) {
							boolean persStillExists = false;
							for (int i = 0; i < dc.sc.size(); i++) {
								if (dc.sc.get(i).getPerson().equals(persAtOpen)) {
									persStillExists = true;
									break;
								}
							}
							String aliasPers = pm.getActor(persAtOpen).getAlias();
							String typePers = pm.getActor(persAtOpen).getType();
							String notePers = pm.getActor(persAtOpen).getNote();
							if (persStillExists == false && typePers == null && aliasPers == null && notePers == null) {
								pm.remove(persAtOpen);
							} else if (persStillExists == false) {
								pm.getActor(persAtOpen).setAppearsInDataSet(false);
								pm.repaint();
							}
						}
						if (!pm.contains(p)) {
							pm.add(new Actor(p, true));
						} else {
							pm.getActor(p).setAppearsInDataSet(true);
							pm.repaint();
						}
						
						String c = (String)category.getEditor().getItem();
						dc.sc.getById(statementId).setCategory(c);
						String a = agreement.getSelectedItem().toString();
						dc.sc.getById(statementId).setAgreement(a);
						dispose();
					} catch (NullPointerException npe) {
						System.err.println("Statement details could not be saved.");
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
	}
	
	/*
	 * This method can import attributes of actors from other .dna files. However, 
	 * it is somewhat buggy and is therefore put in comments.
	 * 
	//import actors and their attributes from external DNA file
	public void importActorAttributes(String infile) {
		
		int question = JOptionPane.showConfirmDialog(dna.Dna.mainProgram, "Overwrite existing data in current file? \nSelect no to overwrite empty fields only.", 
				"Confirmation", JOptionPane.YES_NO_CANCEL_OPTION); // 0 = yes
		if (question != 2) {
			try {
				SAXBuilder builder = new SAXBuilder( false );
				Document docXml = builder.build( new File( infile ) );
				Element discourse = docXml.getRootElement();
				
				Element version = (Element) discourse.getChildren().get(0);
				String v = version.getText();
				if (v.equals("1.21")) {
					Element metadata = discourse.getChild("metadata");
					
					//load person types
					Element personTypes = metadata.getChild("personTypes");
					for (int i = 0; i < personTypes.getChildren().size(); i++) {
						Element type = (Element)personTypes.getChildren().get(i);
						String label = type.getChildText("label");
						int red = Integer.valueOf(type.getChildText("red")).intValue();
						int green = Integer.valueOf(type.getChildText("green")).intValue();
						int blue = Integer.valueOf(type.getChildText("blue")).intValue();
						Color color = new Color(red, green, blue);
						RegexTerm rt = new RegexTerm(label, color);
						if (! pm.containsType(label)) {
							pm.addType(rt);
						} else if (question == 0) {
							pm.setTypeColor(label, color);
						}
					}
					
					//load persons and their attributes
					Element persons = metadata.getChild("persons");
					for (int i = 0; i < persons.getChildren().size(); i++) {
						Element person = (Element)persons.getChildren().get(i);
						String name = person.getChildText("name");
						String type = person.getChildText("type");
						String alias = person.getChildText("alias");
						String note = person.getChildText("note");
						Actor p = new Actor(name, type, alias, note, true);
						if (! pm.contains(name)) {
							pm.add(p);
						} else if (question == 1) {
							if (pm.getActor(name).getType() == null || pm.getActor(name).getType().equals("")) {
								pm.getActor(name).setAlias(type);
							}
							if (pm.getActor(name).getAlias() == null || pm.getActor(name).getAlias().equals("")) {
								pm.getActor(name).setAlias(alias);
							}
							if (pm.getActor(name).getNote() == null || pm.getActor(name).getNote().equals("")) {
								pm.getActor(name).setNote(note);
							}
						} else if (question == 0) {
							pm.getActor(name).setAlias(type);
							pm.getActor(name).setAlias(alias);
							pm.getActor(name).setNote(note);
						}
					}
					
					//load organization types
					Element organizationTypes = metadata.getChild("organizationTypes");
					for (int i = 0; i < organizationTypes.getChildren().size(); i++) {
						Element type = (Element)organizationTypes.getChildren().get(i);
						String label = type.getChildText("label");
						int red = Integer.valueOf(type.getChildText("red")).intValue();
						int green = Integer.valueOf(type.getChildText("green")).intValue();
						int blue = Integer.valueOf(type.getChildText("blue")).intValue();
						Color color = new Color(red, green, blue);
						RegexTerm rt = new RegexTerm(label, color);
						if (! om.containsType(label)) {
							om.addType(rt);
						} else if (question == 0) {
							om.setTypeColor(label, color);
						}
					}
					
					//load organizations and their attributes
					Element organizations = metadata.getChild("organizations");
					for (int i = 0; i < organizations.getChildren().size(); i++) {
						Element organization = (Element)organizations.getChildren().get(i);
						String name = organization.getChildText("name");
						String type = organization.getChildText("type");
						String alias = organization.getChildText("alias");
						String note = organization.getChildText("note");
						Actor o = new Actor(name, type, alias, note, true);
						if (! om.contains(name)) {
							om.add(o);
						} else if (question == 1) {
							if (om.getActor(name).getType() == null || om.getActor(name).getType().equals("")) {
								om.getActor(name).setAlias(type);
							}
							if (om.getActor(name).getAlias() == null || om.getActor(name).getAlias().equals("")) {
								om.getActor(name).setAlias(alias);
							}
							if (om.getActor(name).getNote() == null || om.getActor(name).getNote().equals("")) {
								om.getActor(name).setNote(note);
							}
						} else if (question == 0) {
							om.getActor(name).setAlias(type);
							om.getActor(name).setAlias(alias);
							om.getActor(name).setNote(note);
						}
					}
				}
			} catch (IOException e) {
				System.out.println("Error while reading the file \"" + infile + "\": " + e.getMessage());
				JOptionPane.showMessageDialog(dna.Dna.mainProgram, "Error while reading the file!\n " + e.getMessage());
			} catch (org.jdom.JDOMException e) {
				System.out.println("Error while opening the file \"" + infile + "\": " + e.getMessage());
				JOptionPane.showMessageDialog(dna.Dna.mainProgram, "Error while opening the file!\n " + e.getMessage());
			}
		}
		om.correctAppearance(sc.getOrganizationList());
		pm.correctAppearance(sc.getPersonList());
		System.out.println("Actor attributes have been imported.");
	}
	*/
	
	/**
	 * Main method. Instantiates the application.
	 */
	public static void main (String[] args) {
		mainProgram = new Dna();
	}
}