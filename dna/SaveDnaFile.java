package dna;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class SaveDnaFile {
	
	String filename;
	
	public SaveDnaFile(String infile) {
		this.filename = infile;
		
		System.out.println("Please wait while saving.");
		
		SimpleDateFormat dfYear = new SimpleDateFormat( "yyyy" );
		SimpleDateFormat dfMonth = new SimpleDateFormat( "MM" );
		SimpleDateFormat dfDay = new SimpleDateFormat( "dd" );
		
		Element root = new Element("discourse");
		Document document = new Document(root);
		
		Element version = new Element("version");
		version.setText("1.21");
		root.addContent(version);
		
		Element metadata = new Element("metadata");
		
		//regex items
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
		metadata.addContent(regex);

		//persons and their attributes
		Element persons = new Element("persons");
		for (int i = 0; i < Dna.mainProgram.pm.getActors().size(); i++) {
			Element pers = new Element("person");
			Element namePers = new Element("name");
			Element typePers = new Element("type");
			Element aliasPers = new Element("alias");
			Element notePers = new Element("note");
			namePers.setText(Dna.mainProgram.pm.getActors().get(i).getName());
			typePers.setText(Dna.mainProgram.pm.getActors().get(i).getType());
			aliasPers.setText(Dna.mainProgram.pm.getActors().get(i).getAlias());
			notePers.setText(Dna.mainProgram.pm.getActors().get(i).getNote());
			pers.addContent(namePers);
			pers.addContent(typePers);
			pers.addContent(aliasPers);
			pers.addContent(notePers);
			persons.addContent(pers);
		}
		metadata.addContent(persons);
		
		//person types
		Element personTypes = new Element("personTypes");
		for (int i = 0; i < Dna.mainProgram.pm.getTypes().size(); i++) {
			Element type = new Element("type");
			Element label = new Element("label");
			Element red = new Element("red");
			Element green = new Element("green");
			Element blue = new Element("blue");
			label.setText(((RegexTerm)Dna.mainProgram.pm.getTypes().get(i)).getPattern());
			red.setText(String.valueOf(((RegexTerm)Dna.mainProgram.pm.getTypes().get(i)).getColor().getRed()));
			green.setText(String.valueOf(((RegexTerm)Dna.mainProgram.pm.getTypes().get(i)).getColor().getGreen()));
			blue.setText(String.valueOf(((RegexTerm)Dna.mainProgram.pm.getTypes().get(i)).getColor().getBlue()));
			type.addContent(label);
			type.addContent(red);
			type.addContent(green);
			type.addContent(blue);
			personTypes.addContent(type);
		}
		metadata.addContent(personTypes);
		
		//organizations and their attributes
		Element organizations = new Element("organizations");
		for (int i = 0; i < Dna.mainProgram.om.getActors().size(); i++) {
			Element org = new Element("organization");
			Element name = new Element("name");
			Element type = new Element("type");
			Element alias = new Element("alias");
			Element note = new Element("note");
			name.setText(Dna.mainProgram.om.getActors().get(i).getName());
			type.setText(Dna.mainProgram.om.getActors().get(i).getType());
			alias.setText(Dna.mainProgram.om.getActors().get(i).getAlias());
			note.setText(Dna.mainProgram.om.getActors().get(i).getNote());
			org.addContent(name);
			org.addContent(type);
			org.addContent(alias);
			org.addContent(note);
			organizations.addContent(org);
		}
		metadata.addContent(organizations);
		
		//organization types
		Element organizationTypes = new Element("organizationTypes");
		for (int i = 0; i < Dna.mainProgram.om.getTypes().size(); i++) {
			Element type = new Element("type");
			Element label = new Element("label");
			Element red = new Element("red");
			Element green = new Element("green");
			Element blue = new Element("blue");
			label.setText(((RegexTerm)Dna.mainProgram.om.getTypes().get(i)).getPattern());
			red.setText(String.valueOf(((RegexTerm)Dna.mainProgram.om.getTypes().get(i)).getColor().getRed()));
			green.setText(String.valueOf(((RegexTerm)Dna.mainProgram.om.getTypes().get(i)).getColor().getGreen()));
			blue.setText(String.valueOf(((RegexTerm)Dna.mainProgram.om.getTypes().get(i)).getColor().getBlue()));
			type.addContent(label);
			type.addContent(red);
			type.addContent(green);
			type.addContent(blue);
			organizationTypes.addContent(type);
		}
		metadata.addContent(organizationTypes);
		
		root.addContent(metadata);
		
		Element articles = new Element("articles");
		for(int i = 0; i < dna.Dna.mainProgram.articleTable.getRowCount(); i++) {
			
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
			System.err.println("Cannot save \"" + dnaFile + "\":" + e.getMessage());
			JOptionPane.showMessageDialog(dna.Dna.mainProgram, "Error while saving the file!\n" + e.getStackTrace());
		}
		System.out.println("The file \"" + filename + "\" has been saved.");
	}
}
