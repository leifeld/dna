package dna;

import dna.dataStructures.*;

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
	static Data data = new Data();
	static Dna dna;
	Gui gui;
	SqlConnection sql;
	
	//ArrayList<Document> documents = new ArrayList<Document>(); //SK All changes w.r.t. documents list
	public Dna () {
		data.getSettings().put("version", "2.0 beta 3");
		data.getSettings().put("date", "2016-03-09");
		
		gui = new Gui();
	}
	
	public static void main (String[] args) {
		dna = new Dna();
	}

	public void addDocument(Document document) {
		gui.documentPanel.documentContainer.addDocument(document);
		sql.upsertDocument(document);
	}

	public void removeStatement(int statementId) {
		gui.rightPanel.ssc.removeStatement(statementId);
		sql.removeStatement(statementId);
	}
	
	public void addStatement(Statement statement) {
		gui.rightPanel.ssc.addStatement(statement);
		sql.addStatement(statement, data.getStatementTypeById(statement.getStatementTypeId()).getVariables());
	}
	
	public void updateVariable(int statementId, int statementTypeId, Object content, String variable) {
		StatementType st = data.getStatementTypeById(statementTypeId);
		String dataType = st.getVariables().get(variable);
		Dna.data.getStatement(statementId).getValues().put(variable, content);
		sql.upsertVariableContent(content, statementId, variable, statementTypeId, dataType);
	}
	
	public void addCoder(Coder coder) {
		data.addCoder(coder);
		sql.addCoder(coder);
	}
	
	/*
	public String getVersion() {
		for (int i = 0; i < data.getSettings().size(); i++) {
			Setting s = data.getSettings().get(i);
			if (s.getProperty().equals("version")) {
				return(s.getValue());
			}
		}
		return(null);
	}

	public String getDate() {
		for (int i = 0; i < data.getSettings().size(); i++) {
			Setting s = data.getSettings().get(i);
			if (s.getProperty().equals("date")) {
				return(s.getValue());
			}
		}
		return(null);
	}
	*/
	
	/*
	public int addStatement(String type, int doc, int start, int stop) {
		int statementId = dna.db.addStatement(type, doc, start, stop);
		Color color = dna.db.getStatementTypeColor(type);
		Date date = dna.db.getDocument(doc).getDate();
		int coder = dna.db.getStatement(statementId).getCoder();
		Statement s = new Statement(statementId, doc, start, 
				stop, date, color, type, coder);
		dna.gui.sidebarPanel.ssc.addStatement(s, true);
		return statementId;
	}

	public void removeStatement(int statementId) {
		dna.db.removeStatement(statementId);
		int row = dna.gui.sidebarPanel.ssc.getIndexByStatementId(statementId);
		dna.gui.sidebarPanel.ssc.remove(row);
		dna.gui.sidebarPanel.statementTable.updateUI();
	}
	
	public int addDocument(String title, String text, Date date, int coder, 
			String source, String section, String notes, String type) {
		//TODO: do the replaceAll() here or in NewDOcumentWindow!
		title = title.replaceAll("'", "''");
		text = text.replaceAll("'", "''");
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
	*/

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
	/*
	public void importDocumentsFromHTMLFile(String fileName, String pathName, 
			String textElement, String titleElement, String dateElement, 
			String sectionElement, int coder, String source, String type, 
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
	*/
	
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
	/*
	public void importDocumentsFromWebpage(String urlName, 
			String textElement, String titleElement, String dateElement, 
			String sectionElement, int coder, String source, String type, 
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
		ArrayList<Statement> al = dna.db.
				getStatementsPerDocumentId(documentId);
		for (int i = 0; i < al.size(); i++) {
			removeStatement(al.get(i).getId());
		}
		int row = dna.gui.documentPanel.documentContainer.
				getRowIndexById(documentId);
		dna.gui.documentPanel.documentContainer.remove(row);
		dna.db.removeDocument(documentId);
	}

	public void openFile(String dbfile) {
		Dna.dna.db.openSQLite(dbfile);
		Dna.dna.gui.statusBar.resetLabel();
		Dna.dna.data.setDocuments(Dna.dna.db.getDocuments());
		
		//System.out.println( "Total documents >> " + documents.size());
		for (int i = 0; i < Dna.dna.data.getDocuments().size(); i++) {
						
			//Dna.dna.gui.documentPanel.documentContainer.addDocument(Dna.dna.data.getDocuments().get(i));
			int documentId = Dna.dna.data.getDocuments().get(i).getId();
			ArrayList<Statement> statements = Dna.dna.db.
					getStatementsPerDocumentId(documentId);
			for (int j = 0; j < statements.size(); j++) {
				Statement s = statements.get(j);
				Dna.dna.gui.sidebarPanel.ssc.addStatement(s, true);
			}
		}
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.importHTMLButton.setEnabled(true);
		Dna.dna.gui.menuBar.recodeVariableButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importDnaButton.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateRegexManagerPanel();
		Dna.dna.gui.sidebarPanel.updateViewLinksTable();
		Dna.dna.gui.sidebarPanel.docStats.updateStatistics();
		Dna.dna.gui.menuBar.updateTeggleAction();
	}

	public void openMySQL(String url, String userName, String password) {
		Dna.dna.db.openMySQL(url, userName, password);
		Dna.dna.gui.statusBar.resetLabel();
		Dna.dna.data.setDocuments(Dna.dna.db.getDocuments());
		for (int i = 0; i < Dna.dna.data.getDocuments().size(); i++) {
			//Dna.dna.gui.documentPanel.documentContainer.addDocument(Dna.dna.data.getDocuments().get(i));
			int documentId = Dna.dna.data.getDocuments().get(i).getId();
			ArrayList<Statement> statements = Dna.dna.db.getStatementsPerDocumentId(documentId);
			for (int j = 0; j < statements.size(); j++) {
				Statement s = statements.get(j);
				Dna.dna.gui.sidebarPanel.ssc.addStatement(s, true);
			}
		}
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.importHTMLButton.setEnabled(true);
		Dna.dna.gui.menuBar.recodeVariableButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importDnaButton.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateRegexManagerPanel();
		Dna.dna.gui.sidebarPanel.updateViewLinksTable();
		Dna.dna.gui.sidebarPanel.docStats.updateStatistics();
		Dna.dna.gui.menuBar.updateTeggleAction();
	}
	*/
	
	/*
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
		Dna.dna.gui.menuBar.importDnaButton.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateRegexManagerPanel();
		Dna.dna.gui.sidebarPanel.updateViewLinksTable();
		Dna.dna.gui.sidebarPanel.docStats.updateStatistics();
		Dna.dna.gui.menuBar.updateTeggleAction();
	}
	*/
	
	/*
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
		Dna.dna.gui.menuBar.importDnaButton.setEnabled(false);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(false);
		Dna.dna.gui.menuBar.networkButton.setEnabled(false);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(false);
		Dna.dna.gui.sidebarPanel.rm.clear();
		Dna.dna.gui.sidebarPanel.linkedTableModel.setRowCount(0);
		Dna.dna.gui.sidebarPanel.docStats.clear();
		Dna.dna.gui.textPanel.collapsiblePane.setCollapsed(true);
		Dna.dna.gui.menuBar.toggleBottomButton.setEnabled(false);
	}

	public void newFile(String filename) {
		Dna.dna.db.openSQLite(filename);
		Dna.dna.gui.statusBar.resetLabel();
		Dna.dna.gui.menuBar.typeEditorButton.setEnabled(true);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.dna.gui.menuBar.importHTMLButton.setEnabled(true);
		Dna.dna.gui.menuBar.recodeVariableButton.setEnabled(true);
		Dna.dna.gui.menuBar.closeFile.setEnabled(true);
		Dna.dna.gui.menuBar.importDnaButton.setEnabled(true);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(true);
		Dna.dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateStatementTypes();
		Dna.dna.gui.sidebarPanel.rm.addButton.setEnabled(true);
		Dna.dna.gui.sidebarPanel.updateViewLinksTable();
		Dna.dna.gui.sidebarPanel.docStats.updateStatistics();
		Dna.dna.gui.menuBar.updateTeggleAction();
	}
	*/
}