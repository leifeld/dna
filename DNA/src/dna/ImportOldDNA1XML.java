package dna;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

@SuppressWarnings("serial")
public class ImportOldDNA1XML extends JFrame {
	
	Container c;
	JTable articleImportTable;
	ArticleImportTableModel aitm;
	ImageIcon filterIcon;
	
	public ImportOldDNA1XML(final String file) {
		c = getContentPane();
		this.setTitle("Import statements...");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon importIcon = new ImageIcon(getClass().getResource(
				"/icons/page_white_get.png"));
		this.setIconImage(importIcon.getImage());
		
		aitm = new ArticleImportTableModel();
		articleImportTable = new JTable(aitm);
		articleImportTable.setSelectionMode(ListSelectionModel.
				SINGLE_SELECTION);
		JScrollPane tableScrollPane = new JScrollPane(articleImportTable);
		tableScrollPane.setPreferredSize(new Dimension(500, 300));
		
		articleImportTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		articleImportTable.getColumnModel().getColumn(1).setPreferredWidth(400);
		articleImportTable.getColumnModel().getColumn(2).setPreferredWidth(80);
		articleImportTable.getTableHeader().setReorderingAllowed( false );
		
		JButton importButton = new JButton("Import selected", importIcon);
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Dna.dna.gui, 
						"Are you sure you want to insert the selected \n" +
						"articles into the currently open DNA database?", 
						"Confirmation", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					try {
						Thread importThread = new Thread( new ArticleInserter(
								file), "Import documents" );
						importThread.start();
					} catch (OutOfMemoryError ome) {
						System.err.println("Out of memory. File has been " +
								"closed. Please start Java with\nthe " +
								"-Xmx1024M option, where '1024' is the space " +
								"you want\nto allocate to DNA. The manual " +
								"provides further details.");
						JOptionPane.showMessageDialog(Dna.dna.gui, 
								"Out of memory. File has been closed. Please " +
								"start Java with\nthe -Xmx1024M option, " +
								"where '1024' is the space you want\nto " +
								"allocate to DNA. The manual provides " +
								"further details.");
					}
					dispose();
				}
			}
		});
		ImageIcon selectIcon = new ImageIcon(getClass().getResource(
				"/icons/asterisk_yellow.png"));
		JButton selectAll = new JButton("(Un)select all", selectIcon);
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
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,3));
		
		filterIcon = new ImageIcon(getClass().getResource(
				"/icons/application_form.png"));
		JButton filterButton = new JButton("Keyword filter...", filterIcon);
		filterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String s = (String)JOptionPane.showInputDialog(
						ImportOldDNA1XML.this, 
						"Please enter a regular expression to filter the " +
								"articles:", 
						"Keyword filter", 
						JOptionPane.PLAIN_MESSAGE, 
						filterIcon,
	                    null,
	                    "");
				
				if ((s != null) && (s.length() > 0)) {
					for (int i = 0; i < aitm.getRowCount(); i++) {
						Pattern p = Pattern.compile(s);
	    				Matcher m = p.matcher(aitm.getValueAt(i, 1).toString());
	    				boolean b = m.find();
	    				if (b == true) {
	    					aitm.setValueAt(true, i, 0);
	    				} else {
	    					aitm.setValueAt(false, i, 0);
	    				}
					}
				}
			}
		});
		
		buttonPanel.add(filterButton);
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
	
	class ArticleInserter implements Runnable {
		
		String filename;
		ProgressMonitor progressMonitor;
		
		public ArticleInserter(String filename) {
			this.filename = filename;
		}
		
		public void run() {
			progressMonitor = new ProgressMonitor(Dna.dna.gui, 
					"Importing documents...", "", 0, aitm.getRowCount() - 1 );
			progressMonitor.setMillisToDecideToPopup(1);
			
			for (int k = 0; k < aitm.getRowCount(); k++) {
				progressMonitor.setProgress(k);
				if (progressMonitor.isCanceled()) {
					break;
				}
				if ((Boolean)aitm.getValueAt(k, 0) == true) {
					try {
						SAXBuilder builder = new SAXBuilder( false );
						Document docXml = builder.build( new File( filename ) );
						Element discourse = docXml.getRootElement();
						Element art = (Element)discourse.getChild("articles");
						Element article = (Element)art.getChildren().get(k);
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
						
						String dateString = article.getChild("date").getText();
						String title = article.getChild("title").getText();
						
						// handle duplicates
						if (Dna.dna.gui.documentPanel.documentContainer.containsTitle(title)) {
							int count = 2;
							Pattern p = Pattern.compile(title + " \\(" + "[0-9]+" + "\\)");
							for (int l = 0; l < Dna.dna.gui.documentPanel.documentContainer.getRowCount(); l++) {
								Matcher m = p.matcher(Dna.dna.gui.documentPanel.documentContainer.get(l).getTitle());
								boolean b = m.find();
								if (b == true) {
									count++;
								}
							}
							title = title.concat(" (" + count + ")");
						}
						
						Date date = new Date();
						
				    	title = title.replaceAll("'", "''");
				    	articleText = articleText.replaceAll("'", "''");
						
						int documentId = -1;
						try {
					    	SimpleDateFormat sdfToDate = new SimpleDateFormat(
					    			"dd.MM.yyyy");
					    	date = sdfToDate.parse(dateString);
					    	documentId = Dna.dna.addDocument(title, 
					    			articleText, date, "", "", "", 
					    			"Imported from DNA 1.xx.", "");
					    } catch (ParseException pe) {
					    	pe.printStackTrace();
					    }
						
					    //create statements
					    Element statementsXml = (Element) article.getChild("statements");
					    for (int j = 0; j < statementsXml.getChildren().size(); j++) {
					    	Element statement = (Element) statementsXml.getChildren().get(j);
					    	String start = statement.getChild("start").getText();
					    	String end = statement.getChild("end").getText();
					    	String person = statement.getChild("person").getText();
					    	String organization = statement.getChild("organization").getText();
					    	String category = statement.getChild("category").getText();
					    	String agreement = statement.getChild("agreement").getText();
					    	int agreeInt;
					    	if (agreement.equals("yes")) {
					    		agreeInt = 1;
					    	} else {
					    		agreeInt = 0;
					    	}
					    	
					    	int startInt = Integer.valueOf( start ).intValue() - 1;
					    	int endInt = Integer.valueOf( end ).intValue() - 1;
					    	
					    	person = person.replaceAll("'", "''");
					    	organization = organization.replaceAll("'", "''");
					    	category = category.replaceAll("'", "''");
					    	
					    	int statementId = Dna.dna.addStatement("DNAStatement", documentId, startInt, endInt);
					    	
					    	Dna.dna.db.changeStatement(statementId, "person", person, "short text");
					    	Dna.dna.db.changeStatement(statementId, "organization", organization, "short text");
					    	Dna.dna.db.changeStatement(statementId, "category", category, "short text");
					    	Dna.dna.db.changeStatement(statementId, "agreement", agreeInt, "boolean");
					    }
					} catch (IOException e) {
						System.err.println("Error while reading the file \"" + filename + "\".");
						JOptionPane.showMessageDialog(Dna.dna.gui, "Error while reading the file!");
					} catch (org.jdom.JDOMException e) {
						System.err.println("Error while opening the file \"" + filename + "\": " + e.getMessage());
						JOptionPane.showMessageDialog(Dna.dna.gui, "Error while opening the file!");
					}
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
			if (v.equals("1.16") || v.equals("1.21")) {
				Element articles = rootElement.getChild("articles");
				for (int i = 0; i < articles.getChildren().size(); i++) {
					Element article = (Element)articles.getChildren().get(i);
					String dateString = article.getChild("date").getText();
					String title = article.getChild("title").getText();
					aitm.addArticle(title, dateString, false);
				}
			} else if (v.equals("1.09")) {
				System.err.println("Your file was saved in an older version of DNA. Please open the file in DNA 1.31, save it to a new file, and try to import it again.");
				JOptionPane.showMessageDialog(Dna.dna.gui,	"Your file was saved in an earlier version of DNA.\nPlease open the file, save it to a new .dna file,\nand try to import it again.", "Confirmation required", JOptionPane.OK_OPTION);
			} else {
				System.err.println("Articles can only be imported from valid DNA 1.xx files!");
				JOptionPane.showMessageDialog(Dna.dna.gui,	"Articles can only be imported from valid DNA files!", "Confirmation required", JOptionPane.OK_OPTION);
			}
		} catch (IOException e) {
			System.err.println("Error while reading the file \"" + filename + "\".");
			JOptionPane.showMessageDialog(Dna.dna.gui, "Error while reading the file!");
		} catch (org.jdom.JDOMException e) {
			System.err.println("Error while opening the file \"" + filename + "\": " + e.getMessage());
			JOptionPane.showMessageDialog(Dna.dna.gui, "Error while opening the file!");
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
		
		public Class<?> getColumnClass(int columnIndex) {
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