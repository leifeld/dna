package dna;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Dna {
	String version = "2.0 beta 2";
	String date = "August 16, 2015"; 
	static Dna dna;
	DataAccess db;
	Gui gui;
	
	ArrayList<Document> documents = new ArrayList<Document>(); //SK All changes w.r.t. documents list
	public Dna () {
		documents = new ArrayList<Document>(); //SK
		db = new DataAccess();
		gui = new Gui();
	}

	public static void main (String[] args) {
		dna = new Dna();
	}

	public int addStatement(String type, int doc, int start, int stop) {
		int statementId = dna.db.addStatement(type, doc, start, stop);
		Color color = dna.db.getStatementTypeColor(type);
		Date date = dna.db.getDocument(doc).getDate();
		SidebarStatement s = new SidebarStatement(statementId, doc, start, 
				stop, date, color, type);
		dna.gui.sidebarPanel.ssc.addSidebarStatement(s, true);
		return statementId;
	}

	public void removeStatement(int statementId) {
		dna.db.removeStatement(statementId);
		int row = dna.gui.sidebarPanel.ssc.getIndexByStatementId(statementId);
		dna.gui.sidebarPanel.ssc.remove(row);
		dna.gui.sidebarPanel.statementTable.updateUI();
	}

	public int addDocument(String title, String text, Date date, String coder, 
			String source, String section, String notes, String type) {
		//TODO: do the replaceAll() here or in NewDOcumentWindow!
		title = title.replaceAll("'", "''");
		text = text.replaceAll("'", "''");
		coder = coder.replaceAll("'", "''");
		source = source.replaceAll("'", "''");
		section = section.replaceAll("'", "''");
		notes = notes.replaceAll("'", "''");
		type = type.replaceAll("'", "''");
		int id = dna.db.addDocument(title, text, date, coder, source, section, 
				notes, type);
		Document d = Dna.dna.db.getDocument(id);
		dna.gui.documentPanel.documentContainer.addDocument(d);
		return id;
	}

	/**
	 * LB.Add:
	 * Import multiple documents from one HTML file, parse them and add them 
	 * to the data set.
	 * 
	 * @param fileName			Name of imported file
	 * @param pathName			Path of imported file
	 * @param textElement		HTML code where text is saved in file
	 * @param titleElement		HTML code where title is saved in file
	 * @param dateElement		HTML code where date is saved in file
	 * @param sectionElement	HTML code where section is saved in file
	 * @param coder				Name of coder
	 * @param source			Name of source
	 * @param type				Name of type of document
	 * @param notes				Notes
	 * @throws IOException
	 * @throws ParseException
	 */
	public void importDocumentsFromHTMLFile(String fileName, String pathName, 
			String textElement, String titleElement, String dateElement, 
			String sectionElement, String coder, String source, String type, 
			String notes, Date dateManually, boolean dateManuallySelected)
					throws IOException, ParseException {

		File input = new File(pathName);
		org.jsoup.nodes.Document file = Jsoup.parse(input, "UTF-8");

		for(int i=0; i< file.select(titleElement).size() ; i++){					
			String title = file.select(titleElement).get(i).text();
			String section = file.select(sectionElement).get(i).text();		

			// Idea: take all the "p" and "br" in the text body, replace them w/
			// distinct string (CODE_LINEBREAK_1234), convert HTML to text and
			// then replace said string with line breaks.
			Element textTemp = file.select(textElement).get(i);
			textTemp.select("p").prepend("CODE_LINEBREAK_1234");
			textTemp.select("br").append("CODE_LINEBREAK_1234");
			String textTempString = textTemp.text();
			textTempString = textTempString.replaceAll("CODE_LINEBREAK_1234", 
					"\n\n");
			String textTitle = String.format("%s \n%s, %s \n\n--------------"
					+ "-------------------------------------------------\n\n", 
					title, source, section);
			String text = textTitle +"" + "" + textTempString;
			/*
			// other version of how to make sure the paragraphs within the text body are separated by \n
			// Problem here is that if body does not have a "p" => it will not be recorded.
			//String text = file.select(textElement).get(i).text();
			StringBuilder stringBuilder = new StringBuilder();
			for( Element element : file.select(textElement).get(i).select("p") ){
				stringBuilder.append("\n");
				stringBuilder.append(element.text());
				stringBuilder.append("\n");
				// eg. you can use a StringBuilder and append lines here ...
			}
			String text = stringBuilder.toString();
			 */

			if (dateManuallySelected == false){
				DateExtractor de = new DateExtractor();
				String datefull = file.select(dateElement).get(i).text();
				try{
					Date dateHTML = (Date) de.extractDate(datefull);
					addDocument(title, text, dateHTML, coder, source, section, 
							notes, type);
				}
				//TODO: Put warning into usual DNA-warnings format
				catch(NullPointerException e) {
					String message = "\n Date not extractable.\nUse 'set date"
							+ " manually'-option."; 
					JOptionPane.showMessageDialog(new JFrame(), message, "Warning",
							JOptionPane.ERROR_MESSAGE);
				}
			}else{
				addDocument(title, text, dateManually, coder, source, section, 
						notes, type);
			}	
		} 	
	}

	/**
	 * LB.Add:
	 * Import one document from a webpage, parse it and add it to the data set. 
	 * 
	 * @param urlName			URL of webpage
	 * @param textElement		HTML/Xpath code where text is saved in file
	 * @param titleElement		HTML/Xpath code where title is saved in file
	 * @param dateElement		HTML/Xpath code where date is saved in file
	 * @param sectionElement	HTML/Xpath code where section is saved in file
	 * @param coder				Name of coder
	 * @param source			Name of source
	 * @param type				Type of document
	 * @param notes				Notes
	 * @throws IOException		
	 * @throws ParseException
	 */
	public void importDocumentsFromWebpage(String urlName, 
			String textElement, String titleElement, String dateElement, 
			String sectionElement, String coder, String source, String type, 
			String notes, Date dateManually, boolean dateManuallySelected) 
					throws IOException, ParseException {

		org.jsoup.nodes.Document file = Jsoup.connect(urlName).get();

		String title = file.select(titleElement).text();
		String section = file.select(sectionElement).text();
		Elements textTemp = file.select(textElement);
		textTemp.select("p").prepend("CODE_LINEBREAK_1234");
		textTemp.select("br").append("CODE_LINEBREAK_1234");
		String textTempString = textTemp.text();
		textTempString = textTempString.replaceAll("CODE_LINEBREAK_1234", "\n\n");

		String textTitle = String.format("%s \n%s, %s \nURL: %s\n\n--------------"
				+ "-------------------------------------------------\n\n", 
				title, source, section, urlName);
		String text = textTitle +"" + "" + textTempString;

		if (dateManuallySelected == false){
			DateExtractor de = new DateExtractor();
			String datefull = file.select(dateElement).text();
			try{
				Date dateHTML = (Date) de.extractDate(datefull);
				addDocument(title, text, dateHTML, coder, source, section, notes, 
						type);
			}
			//TODO: Put warning into usual DNA-warnings format
			catch(NullPointerException e) {
				String message = "\n Date not extractable.\nUse 'set date"
						+ " manually'-option."; 
				JOptionPane.showMessageDialog(new JFrame(), message, "Warning",
						JOptionPane.ERROR_MESSAGE);
			}
		}else{
			addDocument(title, text, dateManually, coder, source, section, 
					notes, type);
		}        	
	}

	public void removeDocument(int documentId) {
		ArrayList<SidebarStatement> al = dna.db.
				getStatementsPerDocumentId(documentId);
		for (int i = 0; i < al.size(); i++) {
			removeStatement(al.get(i).getStatementId());
		}
		int row = dna.gui.documentPanel.documentContainer.
				getRowIndexById(documentId);
		dna.gui.documentPanel.documentContainer.remove(row);
		dna.db.removeDocument(documentId);
	}

	public void openFile(String dbfile) {
		Dna.dna.db.openSQLite(dbfile);
		Dna.dna.gui.statusBar.resetLabel();
		documents = Dna.dna.db.getDocuments();
		
		//System.out.println( "Total documents >> " + documents.size());
		for (int i = 0; i < documents.size(); i++) {
						
			Dna.dna.gui.documentPanel.documentContainer.addDocument(documents.get(i));
			int documentId = documents.get(i).getId();
			ArrayList<SidebarStatement> statements = Dna.dna.db.
					getStatementsPerDocumentId(documentId);
			for (int j = 0; j < statements.size(); j++) {
				//
				SidebarStatement s = statements.get(j);
				Dna.dna.gui.sidebarPanel.ssc.addSidebarStatement(s, true);
			}
		}
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.importHTMLButton.setEnabled(true);
		Dna.dna.gui.menuBar.recodeVariableButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateRegexManagerPanel();
	}

	public void openMySQL(String url, String userName, String password) {
		Dna.dna.db.openMySQL(url, userName, password);
		Dna.dna.gui.statusBar.resetLabel();
		documents = Dna.dna.db.getDocuments();
		for (int i = 0; i < documents.size(); i++) {
			Dna.dna.gui.documentPanel.documentContainer.addDocument(documents.get(i));
			int documentId = documents.get(i).getId();
			ArrayList<SidebarStatement> statements = Dna.dna.db.
					getStatementsPerDocumentId(documentId);
			for (int j = 0; j < statements.size(); j++) {
				SidebarStatement s = statements.get(j);
				Dna.dna.gui.sidebarPanel.ssc.addSidebarStatement(s, true);
			}
		}
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.importHTMLButton.setEnabled(true);
		Dna.dna.gui.menuBar.recodeVariableButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateRegexManagerPanel();
	}

	public void openMSSQL(String url, String dbname, String userName, 
			String password) {
		Dna.dna.db.openMSSQL(url, dbname, userName, password);
		Dna.dna.gui.statusBar.resetLabel();
		documents = Dna.dna.db.getDocuments();
		for (int i = 0; i < documents.size(); i++) {
			Dna.dna.gui.documentPanel.documentContainer.addDocument(documents.get(i));
			int documentId = documents.get(i).getId();
			ArrayList<SidebarStatement> statements = Dna.dna.db.
					getStatementsPerDocumentId(documentId);
			for (int j = 0; j < statements.size(); j++) {
				SidebarStatement s = statements.get(j);
				Dna.dna.gui.sidebarPanel.ssc.addSidebarStatement(s, true);
			}
		}
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.importHTMLButton.setEnabled(true);
		Dna.dna.gui.menuBar.recodeVariableButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateRegexManagerPanel();
	}

	public void closeFile() {
		Dna.dna.db.closeFile();
		Dna.dna.gui.statusBar.resetLabel();
		Dna.dna.gui.documentPanel.documentContainer.clear();
		Dna.dna.gui.sidebarPanel.ssc.clear();
		Dna.dna.gui.textPanel.setDocumentText("");
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(false);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(false);
		Dna.dna.gui.menuBar.importHTMLButton.setEnabled(false);
		Dna.dna.gui.menuBar.recodeVariableButton.setEnabled(false);
		Dna.dna.gui.menuBar.closeFile.setEnabled(false);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(false);
		Dna.dna.gui.menuBar.networkButton.setEnabled(false);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(false);
		Dna.dna.gui.sidebarPanel.rm.clear();
	}

	public void newFile(String filename) {
		Dna.dna.db.openSQLite(filename);
		Dna.dna.gui.statusBar.resetLabel();
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.importHTMLButton.setEnabled(true);
		Dna.dna.gui.menuBar.recodeVariableButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
	}
}