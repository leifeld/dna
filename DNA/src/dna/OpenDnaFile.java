package dna;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

public class OpenDnaFile implements Runnable {
	
	String filename;
	ProgressMonitor progressMonitor;
	JEditorPane invisibleTextArea;
	
	public OpenDnaFile(String infile) {
		this.filename = infile;
	}
		
	public void run() {
		
		dna.Dna.mainProgram.clearSpace();
		
		Dna.mainProgram.setRowSorterEnabled(true);
		
    	invisibleTextArea = new JEditorPane();
    	invisibleTextArea.setContentType("text/html");
    	invisibleTextArea.setEditable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		HTMLDocument doc = (HTMLDocument)(kit.createDefaultDocument());
		invisibleTextArea.setEditorKit(kit);
		invisibleTextArea.setDocument(doc);
		
		try {
			SAXBuilder builder = new SAXBuilder( false );
			Document docXml = builder.build( new File( filename ) );
			Element discourse = docXml.getRootElement();
			
			Element version = (Element) discourse.getChildren().get(0);
			String v = version.getText();
			if (v.equals("1.16") || v.equals("1.21")) {
				
				if (v.equals("1.16")) {
					
					//Load the regex terms and colors
					Element regex = discourse.getChild("regex");
					for (int i = 0; i < regex.getChildren().size(); i++) {
						Element term = (Element)regex.getChildren().get(i);
						String pattern = term.getChildText("pattern");
						int red = Integer.valueOf(term.getChildText("red")).intValue();
						int green = Integer.valueOf(term.getChildText("green")).intValue();
						int blue = Integer.valueOf(term.getChildText("blue")).intValue();
						Color color = new Color(red, green, blue);
						RegexTerm rt = new RegexTerm(pattern, color);
						dna.Dna.mainProgram.dc.regexTerms.add(rt);

						Dna.mainProgram.regexManager.listModel.removeAllElements();
						for (int j = 0; j < Dna.mainProgram.dc.regexTerms.size(); j++) {
							Dna.mainProgram.regexManager.listModel.addElement(Dna.mainProgram.dc.regexTerms.get(j));
						}
					}
				} else if (v.equals("1.21")) {
					Element metadata = discourse.getChild("metadata");
					
					//Load the regex terms and colors
					Element regex = metadata.getChild("regex");
					for (int i = 0; i < regex.getChildren().size(); i++) {
						Element term = (Element)regex.getChildren().get(i);
						String pattern = term.getChildText("pattern");
						int red = Integer.valueOf(term.getChildText("red")).intValue();
						int green = Integer.valueOf(term.getChildText("green")).intValue();
						int blue = Integer.valueOf(term.getChildText("blue")).intValue();
						Color color = new Color(red, green, blue);
						RegexTerm rt = new RegexTerm(pattern, color);
						dna.Dna.mainProgram.dc.regexTerms.add(rt);
						
						Dna.mainProgram.regexManager.listModel.removeAllElements();
						for (int j = 0; j < Dna.mainProgram.dc.regexTerms.size(); j++) {
							Dna.mainProgram.regexManager.listModel.addElement(Dna.mainProgram.dc.regexTerms.get(j));
						}
					}
					
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
						Dna.mainProgram.pm.addType(rt);
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
						Dna.mainProgram.pm.add(p);
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
						Dna.mainProgram.om.addType(rt);
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
						Dna.mainProgram.om.add(o);
					}
				}
				
				Element articles = discourse.getChild("articles");
				
				progressMonitor = new ProgressMonitor(dna.Dna.mainProgram, "Opening DNA file...", "", 0, articles.getChildren().size() - 1);
				progressMonitor.setMillisToDecideToPopup(1);
				
				StatementContainer duplicateStatements = new StatementContainer();
				
				for (int i = 0; i < articles.getChildren().size(); i++) {
					
					if (progressMonitor.isCanceled()) {
						break;
					}
					
					//create article with title, date and text
					Element article = (Element)articles.getChildren().get(i);
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
					for (int j = 0; j < Dna.mainProgram.dc.ac.getRowCount(); j++) {
						if (Dna.mainProgram.dc.ac.getValueAt(j, 0).equals(title)) { //avoid duplicate article titles
							Pattern p = Pattern.compile(" \\([0-9]+\\)$");
							Matcher m = p.matcher(title);
							boolean b = m.find();
							if (b == true) {
								int openBracket = title.lastIndexOf("(");
								int closeBracket = title.lastIndexOf(")");
								String number = title.substring(openBracket + 1, closeBracket);
								int numberInt = Integer.parseInt(number);
								numberInt++;
								title = title.substring(0, openBracket + 1) + numberInt + ")";
							} else {
								title = title + " (2)";
							}
							System.err.println("A duplicate article name was found. It will be renamed to: " + title);
							continue;
						}
					}
					
					Date date = new Date();
					try {
				    	SimpleDateFormat sdfToDate = new SimpleDateFormat("dd.MM.yyyy");
				    	date = sdfToDate.parse(dateString);
				        dna.Dna.mainProgram.dc.ac.addArticle(new Article(title, date, articleText));
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
				    	
				    	int artRow = Dna.mainProgram.dc.ac.getRowIndexByTitle(title);
				    	String artText = Dna.mainProgram.dc.ac.get(artRow).getText();
				    	invisibleTextArea.setText(artText);
				    	invisibleTextArea.setSelectionStart(startInt);
				    	invisibleTextArea.setSelectionEnd(endInt);
				    	String selection = invisibleTextArea.getSelectedText();
				    	
						//put statements into the statement list
						try {
							dna.Dna.mainProgram.dc.sc.addStatement(new Statement(id, startInt, endInt, date, selection, title, person, organization, category, agreement), false);
						} catch (DuplicateStatementIdException e) {
							try { //save up duplicate statements for later insertion
								System.err.println("Duplicate statement with ID " + id + " detected. A new ID will be assigned.");
								dna.Dna.mainProgram.dc.sc.sort();
								duplicateStatements.addStatement(new Statement(duplicateStatements.getFirstUnusedId(), startInt, endInt, date, selection, title, person, organization, category, agreement), false);
							} catch (DuplicateStatementIdException e1) {
								e1.printStackTrace();
							}
						}
				    }
				    progressMonitor.setProgress(i);
				}
				//insert duplicate statements
				for (int i = 0; i < duplicateStatements.size(); i++) {
					try {
						Dna.mainProgram.dc.sc.addStatement(new Statement(
								Dna.mainProgram.dc.sc.getFirstUnusedId(), 
								duplicateStatements.get(i).getStart(), 
								duplicateStatements.get(i).getStop(), 
								duplicateStatements.get(i).getDate(), 
								duplicateStatements.get(i).getText(), 
								duplicateStatements.get(i).getArticleTitle(), 
								duplicateStatements.get(i).getPerson(), 
								duplicateStatements.get(i).getOrganization(), 
								duplicateStatements.get(i).getCategory(), 
								duplicateStatements.get(i).getAgreement()
						), false);
					} catch (DuplicateStatementIdException e) {
						e.printStackTrace();
					}
				}
			} else if (v.equals("1.09")) {
				progressMonitor = new ProgressMonitor(dna.Dna.mainProgram, "Opening DNA file...", "", 0, discourse.getChildren().size() - 1);
				progressMonitor.setMillisToDecideToPopup(1);
				for (int i = 1; i < discourse.getChildren().size(); i++) {
					
					if (progressMonitor.isCanceled()) {
						break;
					}
					
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
				        dna.Dna.mainProgram.dc.ac.addArticle(new Article(title, date, articleText));
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
						try {
							dna.Dna.mainProgram.dc.sc.addStatement(new Statement(id, startInt, endInt, date, selection, title, person, organization, category, agreement), true);
						} catch (DuplicateStatementIdException e) {
							System.err.println("A statement with ID " + id + " already exists. It will not be added.");
						}
				    }
				    progressMonitor.setProgress(i);
				}
				dna.Dna.mainProgram.dc.sc.sort();
			} else {
				System.out.println("An outdated version of the file format was found.");
				int convertDialog = JOptionPane.showConfirmDialog(dna.Dna.mainProgram,
						"An outdated version of the file format was found." + "\n" +
						"DNA can try to import it. Please be sure you have" + "\n" +
						"a backup copy because the old file may be over-" + "\n" +
						"written. Please check your data for correctness" + "\n" +
						"after importing. If you encounter any errors, " + "\n" +
						"please contact the author. Would you like to " + "\n" +
						"import the file now?", "Confirmation required", JOptionPane.YES_NO_OPTION);
				if (convertDialog == 0) {
					System.out.println("Trying to import the file.");
					progressMonitor = new ProgressMonitor(dna.Dna.mainProgram, "Opening DNA file...", "", 0, discourse.getChildren().size() - 1);
					progressMonitor.setMillisToDecideToPopup(1);
					HashMap<String,Integer> titleOccurrences = new HashMap<String,Integer>();
					for (int i = 0; i < discourse.getChildren().size(); i++) {
						
						if (progressMonitor.isCanceled()) {
							break;
						}
						
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
									int id = dna.Dna.mainProgram.dc.sc.getFirstUnusedId();
									articleText = articleText + text;
									try {
										dna.Dna.mainProgram.dc.sc.addStatement(new Statement(id, start, end, articleDate, text, title, person, organization, category, agreement), true);
									} catch (DuplicateStatementIdException e) {
										System.err.println("A statement with ID " + id + " already exists. It will not be added.");
									}
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
						dna.Dna.mainProgram.dc.ac.addArticle(art);
						progressMonitor.setProgress(i);
					}
					dna.Dna.mainProgram.dc.sc.sort();
					System.out.println("Note: Please use the save-as button if you don't want to overwrite the old file.");
				}
			}
		} catch (OutOfMemoryError ome) {
			dna.Dna.mainProgram.clearSpace();
			System.out.println("Out of memory. File has been closed. Please start Java with\nthe -Xmx1024M option, where '1024' is the space you want\nto allocate to DNA. The manual provides further details.");
			JOptionPane.showMessageDialog(dna.Dna.mainProgram, "Out of memory. File has been closed. Please start Java with\nthe -Xmx1024M option, where '1024' is the space you want\nto allocate to DNA. The manual provides further details.");
		} catch (IOException e) {
			System.out.println("Error while reading the file \"" + filename + "\": " + e.getMessage());
			JOptionPane.showMessageDialog(dna.Dna.mainProgram, "Error while reading the file!\n " + e.getMessage());
			dna.Dna.mainProgram.dc.currentFileName = "";
			dna.Dna.mainProgram.currentFileLabel.setText("Current file: none");
		} catch (org.jdom.JDOMException e) {
			System.out.println("Error while opening the file \"" + filename + "\": " + e.getMessage());
			JOptionPane.showMessageDialog(dna.Dna.mainProgram, "Error while opening the file!\n " + e.getMessage());
			dna.Dna.mainProgram.dc.currentFileName = "";
			dna.Dna.mainProgram.currentFileLabel.setText("Current file: none");
		}
		
		if (progressMonitor.isCanceled()) {
			System.out.println("Loading the .dna file has been canceled.\nWarning: The file has been loaded only partially.\nYou should *not* accidentally overwrite your file now!");
			JOptionPane.showMessageDialog(dna.Dna.mainProgram, "Loading the .dna file has been canceled.\nWarning: The file has been loaded only partially.\nYou should *not* accidentally overwrite your file now!");
		} else {
			if (dna.Dna.mainProgram.dc.ac.getRowCount() > 0) {
				dna.Dna.mainProgram.dc.currentFileName = filename;
				dna.Dna.mainProgram.currentFileLabel.setText( "Current file: " + dna.Dna.mainProgram.dc.currentFileName );
			}
		}
		Dna.mainProgram.setRowSorterEnabled(true);
		Dna.mainProgram.updateLists();
		Dna.mainProgram.om.correctAppearance(Dna.mainProgram.dc.sc.getOrganizationList());
		Dna.mainProgram.pm.correctAppearance(Dna.mainProgram.dc.sc.getPersonList());
	}
}