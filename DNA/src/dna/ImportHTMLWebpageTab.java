package dna;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

// TODO: Add buffering-symbol when importing documents
/* TODO: Error message if one of the xml-elements don't exist/are not found 
 * in document (add try-catch for each of the elements where Strings are 
 * extracted with?) */

public class ImportHTMLWebpageTab extends JFrame {
	private static final long serialVersionUID = 1L;

	JTabbedPane tabbedPane = new JTabbedPane();
	ImportHTML ihtml;
	ImportWebpage iweb;

	public ImportHTMLWebpageTab() {

		tabbedPane = new JTabbedPane();
		ihtml = new ImportHTML();

		//ok-Button: Action Listener for OK-Button (needs to be in this Class
		//bc otherwise window won't dispose()
		ihtml.okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//save the text-entries from the import-window in the variables				
				String fileNameImport = "\"" + ihtml.fileName.getText() + "\"";			
				String filePathImport = ihtml.pathNameField.getText() ;
				String textElement = ihtml.elementTextField.getText() ;
				String titleElement = ihtml.elementTitleField.getText() ;

				Date dateManually = (Date) ihtml.dateSpinner.getValue();
				String dateElement = (String) ihtml.elementDateField.getText() ;

				String sourceAttElement = ihtml.elementSectionField.getText() ;
				int coder = (int) ihtml.coderBox.getModel().getSelectedItem() ;
				String source = (String)  ihtml.sourceBox.getModel().getSelectedItem() ;
				String type = (String)  ihtml.typeBox.getModel().getSelectedItem();
				String notes = ihtml.notesArea.getText() ;

				boolean dateManuallySelected = ihtml.dateRadioButton.isSelected();

				// if folder-option is chosen = parse every document in it: 
				if (ihtml.folderRadioButton.isSelected() == true) {
					List<String> filesInFolder = new ArrayList<String>();
					List<String> pathForFiles = new ArrayList<String>();
					File[] files = new File(filePathImport).listFiles();
					for (File file : files) {
						if (file.isFile()) {
							if (file.getName().endsWith(".html") == true){
								filesInFolder.add(file.getName());
								pathForFiles.add(filePathImport + "/" + file.getName());
							}
						}
					}
					for (int i = 0; i < pathForFiles.size(); i++) {
						try {
							Dna.dna.importDocumentsFromHTMLFile(filesInFolder.get(i), 
									pathForFiles.get(i), textElement, titleElement, 
									dateElement, sourceAttElement, coder, source, 
									type, notes, dateManually, dateManuallySelected);
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
						dispose();
					}
				}else{
					//TODO: LB: learn about IOExceptions
					try {
						Dna.dna.importDocumentsFromHTMLFile(fileNameImport, 
								filePathImport, textElement, titleElement, 
								dateElement, sourceAttElement, coder, source, 
								type, notes, dateManually, dateManuallySelected);
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
					//LB.Note: the actionListener needs to be here, 
					//bc otherwise "dispose()" won't work
					dispose();
				}	
			}
		});
		tabbedPane.add("import document(s) from HTML file(s)", ihtml);

		// import webpages from an URS or multiple URLs saved in a link list
		iweb = new ImportWebpage();
		iweb.okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//save the text-entries from the import-window in the variables				
				String urlPathNameImport = iweb.pathNameField.getText() ;
				String textElement = iweb.elementTextField.getText() ;
				String titleElement = iweb.elementTitleField.getText() ;

				Date dateManually = (Date) iweb.dateSpinner.getValue();
				String dateElement = iweb.elementDateField.getText() ;
				boolean dateManuallySelected = iweb.dateRadioButton.isSelected();

				String sectionElement = iweb.elementSectionField.getText() ;
				int coder = (int) iweb.coderBox.getModel().getSelectedItem() ;
				String source = (String)  iweb.sourceBox.getModel().getSelectedItem() ;
				String type = (String)  iweb.typeBox.getModel().getSelectedItem();
				String notes = iweb.notesArea.getText() ;

				// if folder-option is chosen = parse every document in it: 
				if (iweb.linkListRadioButton.isSelected() == true) {
					File f = new File(iweb.pathNameField.getText());
					List<String> webpages = new ArrayList<String>();
					try{
						ArrayList<String> lines = getArrayListFromString(f);
						for(int x = 0; x < lines.size(); x++){
							if (lines.get(x).toString().startsWith("www.") || 
									lines.get(x).toString().startsWith("http:")){
								webpages.add(lines.get(x).toString());
							}
						}
					}
					catch(Exception en){
						en.printStackTrace();
					}

					for (int i = 0; i < webpages.size(); i++) {
						try {
							Dna.dna.importDocumentsFromWebpage(webpages.get(i).toString(),
									textElement, titleElement, 
									dateElement, sectionElement, coder, source, 
									type, notes, dateManually, dateManuallySelected);
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
					}
				}else{
					try {
						Dna.dna.importDocumentsFromWebpage(urlPathNameImport, 
								textElement, titleElement,dateElement, 
								sectionElement, coder, source, type, notes, 
								dateManually, dateManuallySelected);
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}	
				//LB.Note: do not dispose() and keep entries so that one can add 
				// several webpages at once
				//dispose();
				//LB.Note: clear URL-path => so that it is ready for new webpage.
				iweb.pathNameField.setText("");
			}
		});
		tabbedPane.add("import document(s) from webpage(s)", iweb);

		this.add(tabbedPane);
		this.setVisible(true);
		this.pack();
		//LB.Note: setLocationRelativeTo() needs to be after pack()
		this.setLocationRelativeTo(null);	
	}

	public static ArrayList<String> getArrayListFromString(File f) 
			throws FileNotFoundException {
		Scanner s;
		ArrayList<String> list = new ArrayList<String>();
		s = new Scanner(f);
		while (s.hasNext()) {
			list.add(s.next());
		}
		s.close();
		return list;
	}
}


