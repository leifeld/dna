package dna;

import dna.dataStructures.*;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import javax.swing.ImageIcon;

public class Dna {
	public static Data data = new Data();
	public static Dna dna;
	public Gui gui;
	public SqlConnection sql;
	public String version, date;
	PrintStream console;
	
	public Dna() {
		date = "2016-04-11";
		version = "2.0 beta 7";
		System.out.println("DNA version: " + version + " (" + date + ")");
		System.out.println("Java version: " + System.getProperty("java.version"));
		System.out.println("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
		console = System.err;
		
		gui = new Gui();
	}
	
	public static void main (String[] args) {
		dna = new Dna();
	}

	public void addDocument(Document document) {
		gui.documentPanel.setRowSorterEnabled(false);
		gui.documentPanel.documentContainer.addDocument(document);
		gui.documentPanel.setRowSorterEnabled(true);
		sql.upsertDocument(document);
	}
	
	public void removeDocument(int documentId) {
		Dna.data.removeDocument(documentId);
		sql.removeDocument(documentId);
	}
	
	public void removeStatement(int statementId) {
		gui.rightPanel.statementPanel.ssc.removeStatement(statementId);
		sql.removeStatement(statementId);
	}
	
	public void addStatement(Statement statement) {
		gui.rightPanel.statementPanel.setRowSorterEnabled(false);
		gui.rightPanel.statementPanel.ssc.addStatement(statement);
		gui.rightPanel.statementPanel.setRowSorterEnabled(true);
		int statementTypeId = statement.getStatementTypeId();
		LinkedHashMap<String, String> map = data.getStatementTypeById(statementTypeId).getVariables();
		sql.addStatement(statement, map);
	}
	
	public void updateVariable(int statementId, int statementTypeId, Object content, String variable) {
		StatementType st = data.getStatementTypeById(statementTypeId);
		String dataType = st.getVariables().get(variable);
		try {
			sql.upsertVariableContent(content, statementId, variable, statementTypeId, dataType);
			Dna.data.getStatement(statementId).getValues().put(variable, content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addCoder(Coder coder) {
		data.addCoder(coder);
		sql.addCoder(coder);
	}
	
	public void replaceCoder(Coder coder) {
		data.replaceCoder(coder);
		sql.upsertCoder(coder);
		
	}
	
	public void setActiveCoder(int activeCoder) {
		data.setActiveCoder(activeCoder);
		sql.upsertSetting("activeCoder", (new Integer(activeCoder)).toString());
	}
	
	public void addRegex(Regex regex) {
		data.addRegex(regex);
		sql.upsertRegex(regex);
	}
	
	public void removeRegex(String label) {
		data.removeRegex(label);
		sql.removeRegex(label);
	}
	
	public void closeDatabase() {
		data = new Data();
		sql.closeConnection();
		sql = null;
		Dna.dna.gui.leftPanel.coderPanel.clear();
		Dna.dna.gui.statusBar.resetLabel();
		Dna.dna.gui.documentPanel.documentContainer.clear();
		Dna.dna.gui.rightPanel.statementPanel.ssc.clear();
		Dna.dna.gui.textPanel.setDocumentText("");
		Dna.dna.gui.menuBar.openDatabase.setEnabled(true);
		Dna.dna.gui.menuBar.newDatabase.setEnabled(true);
		Dna.dna.gui.menuBar.colorCoderButton.setIcon(new ImageIcon(getClass().getResource("/icons/tick.png")));
		Dna.dna.gui.menuBar.colorCoderButton.setEnabled(false);
		Dna.dna.gui.menuBar.colorStatementTypeButton.setEnabled(false);
		//Dna.dna.gui.menuBar.typeEditorButton.setEnabled(false);
		Dna.dna.gui.menuBar.newDocumentButton.setEnabled(false);
		//Dna.dna.gui.menuBar.importHTMLButton.setEnabled(false);
		//Dna.dna.gui.menuBar.recodeVariableButton.setEnabled(false);
		Dna.dna.gui.menuBar.networkButton.setEnabled(false);
		Dna.dna.gui.menuBar.closeDatabase.setEnabled(false);
		//Dna.dna.gui.menuBar.importDnaButton.setEnabled(false);
		Dna.dna.gui.menuBar.networkButton.setEnabled(false);
		//Dna.dna.gui.rightPanel.statementPanel.updateStatementTypes();  //TODO: reimplement
		Dna.dna.gui.rightPanel.rm.addButton.setEnabled(false);
		Dna.dna.gui.rightPanel.rm.clear();
		//Dna.dna.gui.rightPanel.linkedTableModel.setRowCount(0);
		Dna.dna.gui.rightPanel.rm.regexListModel.updateList();
		Dna.dna.gui.rightPanel.rm.setFieldsEnabled(false);
		Dna.dna.gui.leftPanel.docStats.clear();
		Dna.dna.gui.leftPanel.docStats.refreshButton.setEnabled(false);
		Dna.dna.gui.menuBar.importOldButton.setEnabled(false);
		Dna.dna.gui.rightPanel.statementPanel.model.clear();
		Dna.dna.gui.rightPanel.statementPanel.typeComboBox.setEnabled(false);
		Dna.dna.gui.rightPanel.statementPanel.statementFilter.showAll.doClick();
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
	*/
	
	/*
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