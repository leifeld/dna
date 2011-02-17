package dna;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

@SuppressWarnings( "unchecked" )
public class DnaParser {
	
	String infile;
	DnaStatementCollection sc;
    GregorianCalendar date;
    
    public DnaParser( String infile ) throws Exception {
        this.infile = infile;
        sc = parse( infile );
    }
    
    public DnaStatementCollection getDnaStatementCollection() {
    	return sc;
    }

    public DnaStatementCollection parse( String infile ) throws Exception {
    	DnaStatementCollection statementCollection = new DnaStatementCollection();
    	SAXBuilder builder = new SAXBuilder( );
        Document doc = builder.build(new InputStreamReader(new FileInputStream(infile), "UTF8"));
        Element discourse = doc.getRootElement();
        ArrayList<Element> artikelliste = new ArrayList<Element>(discourse.getChildren("article"));
        ArrayList<Element> statementliste = new ArrayList<Element>();
        for (int i = 0; i < artikelliste.size(); i = i + 1) {
            Element artikel = (Element) artikelliste.get(i);
            statementliste.addAll(artikel.getChildren("statement"));
        }
        for (int k = 0; k < statementliste.size(); k = k + 1) {
            Element statement = (Element) statementliste.get(k);
            
            Boolean agree = null;
            if (statement.getAttributeValue("agreement").equals("yes")) {
            	agree = true;
            } else if (statement.getAttributeValue("agreement").equals("no")) {
            	agree = false;
            }
            
            String content = statement.getText();
            String person = statement.getAttributeValue("person");
            String organization = statement.getAttributeValue("organization");
            String category = statement.getAttributeValue("category");
            
            Element art = (Element) statementliste.get(k).getParent();
            
            int day = Integer.parseInt(art.getAttributeValue("day"));
            int month = Integer.parseInt(art.getAttributeValue("month")) - 1;
            int year = Integer.parseInt(art.getAttributeValue("year"));
            
            date = new GregorianCalendar();
			date.set(year, month, day);
            
			statementCollection.add(new DnaStatement(content, person, organization, category, agree, date));
        }
        
        if ( statementliste.size() == 1 ) {
            System.out.println( statementliste.size() + " statement found in the file." );
        } else {
            System.out.println( statementliste.size() + " statements found in the file.");
        }
        return statementCollection;
    }
	
}