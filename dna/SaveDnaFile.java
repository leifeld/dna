package dna;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class SaveDnaFile implements Runnable {
	
	String filename;
	ProgressMonitor progressMonitor;
	
	public SaveDnaFile(String infile) {
		this.filename = infile;
	}
		
	public void run() {
		SimpleDateFormat dfYear = new SimpleDateFormat( "yyyy" );
		SimpleDateFormat dfMonth = new SimpleDateFormat( "MM" );
		SimpleDateFormat dfDay = new SimpleDateFormat( "dd" );
		
		Element root = new Element("discourse");
		Document document = new Document(root);
		
		Element version = new Element("version");
		version.setText("1.16");
		root.addContent(version);
		
		Element regex = new Element("regex");
		for (int i = 0; i < dna.Dna.mainProgram.regexTerms.size(); i++) {
			Element term = new Element("term");
			Element pattern = new Element("pattern");
			Element red = new Element("red");
			Element green = new Element("green");
			Element blue = new Element("blue");
			pattern.setText(dna.Dna.mainProgram.regexTerms.get(i).getPattern());
			red.setText(new Integer(dna.Dna.mainProgram.regexTerms.get(i).getColor().getRed()).toString());
			green.setText(new Integer(dna.Dna.mainProgram.regexTerms.get(i).getColor().getGreen()).toString());
			blue.setText(new Integer(dna.Dna.mainProgram.regexTerms.get(i).getColor().getBlue()).toString());
			term.addContent(pattern);
			term.addContent(red);
			term.addContent(green);
			term.addContent(blue);
			regex.addContent(term);
		}
		root.addContent(regex);
		
		Element articles = new Element("articles");
		
		progressMonitor = new ProgressMonitor(dna.Dna.mainProgram, "Saving DNA file...", "", 0, dna.Dna.mainProgram.articleTable.getRowCount() - 1);
		progressMonitor.setMillisToDecideToPopup(1);
		
		for(int i = 0; i < dna.Dna.mainProgram.articleTable.getRowCount(); i++) {
			
			if (progressMonitor.isCanceled()) {
				break;
			}
			
			Element article = new Element("article");
			
			Element text = new Element("text");
			String htmlContents = dna.Dna.mainProgram.ac.get(i).getText();
			dna.Dna.mainProgram.strippedContents = dna.Dna.mainProgram.stripHtmlTags(htmlContents, false);
			text.setText(dna.Dna.mainProgram.strippedContents);
			article.addContent(text);
			
			Element date = new Element("date");
			Date dateStamp = dna.Dna.mainProgram.ac.get(i).getDate();
			String dateString = dfDay.format( dateStamp ) + "." + dfMonth.format( dateStamp ) + "." + dfYear.format( dateStamp );
			date.setText(dateString);
			article.addContent(date);
			
			Element title = new Element("title");
			String tit = dna.Dna.mainProgram.ac.get(i).getTitle();
			title.setText(tit);
			article.addContent(title);
			
			Element statementElements = new Element("statements");
			for (int j = 0; j < dna.Dna.mainProgram.sc.statements.size(); j++) {
				if (dna.Dna.mainProgram.sc.statements.get(j).articleTitle.equals(tit)) {
					Element statement = new Element("statement");
					
					Element id = new Element("id");
					id.setText(new Integer(dna.Dna.mainProgram.sc.get(j).getId()).toString());
					statement.addContent(id);
					
					Element start = new Element("start");
					start.setText(new Integer(dna.Dna.mainProgram.sc.statements.get(j).start).toString());
					statement.addContent(start);
					
					Element end = new Element("end");
					end.setText(new Integer(dna.Dna.mainProgram.sc.statements.get(j).stop).toString());
					statement.addContent(end);
					
					Element person = new Element("person");
					person.setText(dna.Dna.mainProgram.sc.statements.get(j).person);
					statement.addContent(person);
					
					Element organization = new Element("organization");
					organization.setText(dna.Dna.mainProgram.sc.statements.get(j).organization);
					statement.addContent(organization);
					
					Element category = new Element("category");
					category.setText(dna.Dna.mainProgram.sc.statements.get(j).category);
					statement.addContent(category);
					
					Element agreement = new Element("agreement");
					agreement.setText(dna.Dna.mainProgram.sc.statements.get(j).agreement);
					statement.addContent(agreement);
					
					statementElements.addContent(statement);
				}
			}
			article.addContent(statementElements);
			articles.addContent(article);
			progressMonitor.setProgress(i);
		}
		root.addContent(articles);
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
			JOptionPane.showMessageDialog(dna.Dna.mainProgram, "Error while saving the file!");
		}
		System.out.println("The file \"" + filename + "\" has been saved.");
	}
}
