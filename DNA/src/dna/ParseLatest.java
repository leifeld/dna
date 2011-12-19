package dna;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

public class ParseLatest {
	
	String filename;
	DnaContainer dc;
	
	public DnaContainer getDc() {
		return dc;
	}
	
	public void setDc(DnaContainer dc) {
		this.dc = dc;
	}
	
	public ParseLatest(String filename, boolean verbose) {
		
		this.filename = filename;
		dc = new DnaContainer();
		
		try {
			SAXBuilder builder = new SAXBuilder( false );
			Document docXml = builder.build( new File( filename ) );
			Element discourse = docXml.getRootElement();
			
			Element version = (Element) discourse.getChildren().get(0);
			String v = version.getText();
			if (v.equals("1.21")) {
				
				if (verbose == true) {
					System.out.print("Parsing metadata...");
				}
				
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
					dc.regexTerms.add(rt);
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
					//dc.pt.addElement(rt);
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
					dc.pc.addActor(p);
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
					//dc.ot.addElement(rt);
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
					dc.oc.addActor(o);
				}
				
				if (verbose == true) {
					System.out.println(" done.");
					System.out.println("Parsing articles... ");
				}
				
				Element articles = discourse.getChild("articles");
				
				StatementContainer duplicateStatements = new StatementContainer();
				
				for (int i = 0; i < articles.getChildren().size(); i++) {
					
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
					for (int j = 0; j < dc.ac.getRowCount(); j++) {
						if (dc.ac.getValueAt(j, 0).equals(title)) { //avoid duplicate article titles
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
				        dc.ac.addArticle(new Article(title, date, articleText));
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
				    			if (verbose == true) {
				    				System.out.println("End position of statement " + id + " was corrected.");
				    			}
				    		}
				    		if (startInt > selection.length()) {
				    			startInt = selection.length();
				    			if (verbose == true) {
				    				System.out.println("Start position of statement " + id + " was corrected.");
				    			}
				    		}
				    		
				    		selection = selection.substring(startInt-1, endInt-1);
				    	} catch (StringIndexOutOfBoundsException e) {
				    		System.err.println("DnaStatement text of statement " + id + " could not be identified.");
				    	}
				    	
						//put statements into the statement list
						try {
							dc.sc.addStatement(new DnaStatement(id, startInt, endInt, date, selection, title, person, organization, category, agreement), false);
						} catch (DuplicateStatementIdException e) {
							try { //save up duplicate statements for later insertion
								System.err.println("Duplicate statement with ID " + id + " detected. A new ID will be assigned.");
								duplicateStatements.addStatement(new DnaStatement(duplicateStatements.getFirstUnusedId(), startInt, endInt, date, selection, title, person, organization, category, agreement), false);
							} catch (DuplicateStatementIdException e1) {
								e1.printStackTrace();
							}
						}
				    }
				    if (i % 100 == 0 && i > 0) {
				    	if (verbose == true) {
				    		System.out.println("  " + i + " articles completed...");
				    	}
				    }
				}
				//insert duplicate statements
				for (int i = 0; i < duplicateStatements.size(); i++) {
					try {
						dc.sc.addStatement(new DnaStatement(
								dc.sc.getFirstUnusedId(), 
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
				dc.sc.sort();
				if (verbose == true) {
					System.out.println("done. " + dc.ac.getRowCount() + " articles and " + dc.sc.size() + " statements parsed.");
				}
			} else {
				System.err.println("Outdated DNA file version. Please convert the \nfile using the standalone version of DNA first.");
			}
		} catch (OutOfMemoryError ome) {
			dna.Dna.mainProgram.clearSpace();
			System.err.println("Out of memory. File has been closed. Please start Java with\nthe -Xmx1024M option, where '1024' is the space you want\nto allocate to DNA. The manual provides further details.");
		} catch (IOException e) {
			System.err.println("Error while reading the file \"" + filename + "\": " + e.getMessage());
			dna.Dna.mainProgram.dc.currentFileName = "";
		} catch (org.jdom.JDOMException e) {
			System.err.println("Error while opening the file \"" + filename + "\": " + e.getMessage());
			dna.Dna.mainProgram.dc.currentFileName = "";
		}
	}
}