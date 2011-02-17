package dna;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

@SuppressWarnings( "unchecked" )
public class DnaParseXml {
	
    String infile;
    
    int i;
    ArrayList<String> st_pers = new ArrayList<String>();
    ArrayList<String> st_cat = new ArrayList<String>();
    ArrayList<String> st_org = new ArrayList<String>();
    ArrayList<String> st_agree = new ArrayList<String>();
    ArrayList<String> st_day = new ArrayList<String>();
    ArrayList<String> st_month = new ArrayList<String>();
    ArrayList<String> st_year = new ArrayList<String>();
    GregorianCalendar firstDate;
    GregorianCalendar lastDate;
    long durationDays;
    
    public DnaParseXml( String infile ) {
        this.infile = infile;
        parser( infile );
        findDates();
    }

    public void parser( String infile ) {
        try {
            SAXBuilder builder = new SAXBuilder( );
            Document doc = builder.build( new File( infile ) );
            Element discourse = doc.getRootElement();
            int i;
            int k;
            ArrayList<Element> artikelliste = new ArrayList<Element>(discourse.getChildren("article"));
            ArrayList<Element> statementliste = new ArrayList<Element>();
            for (i = 0; i < artikelliste.size(); i = i + 1) {
                Element artikel = (Element) artikelliste.get(i);
                statementliste.addAll(artikel.getChildren("statement"));
            }
            for (k = 0; k < statementliste.size(); k = k + 1) {
                Element statement = (Element) statementliste.get(k);
                st_pers.add(statement.getAttributeValue("person"));
                st_org.add(statement.getAttributeValue("organization"));
                st_cat.add(statement.getAttributeValue("category"));
                st_agree.add(statement.getAttributeValue("agreement"));
                Element art = (Element) statementliste.get(k).getParent();
                st_day.add(art.getAttributeValue("day"));
                st_month.add(art.getAttributeValue("month"));
                st_year.add(art.getAttributeValue("year"));
            }
            
            if ( statementliste.size() == 1 ) {
                System.out.println( statementliste.size() + " statement found in the file." );
            } else {
                System.out.println( statementliste.size() + " statements found in the file.");
            }
            
        } catch (IOException e) {
            System.out.println("Error while reading the file \"" + infile + "\".");
        } catch (org.jdom.JDOMException e) {
            System.out.println("Error while opening XML file \"" + infile + "\": " + e.getMessage());
        }
    }
    
    /**
     * Find the first and the last date and the duration in the DNA file.
     */
    public void findDates() {
        ArrayList<GregorianCalendar> date = new ArrayList<GregorianCalendar>();
        for (i=0; i<st_day.size(); i=i+1) {
            date.add(i, new GregorianCalendar(Integer.parseInt(st_year.get(i)),
                (Integer.parseInt(st_month.get(i)))-1, Integer.parseInt(st_day.get(i))));
        }
        Collections.sort(date);
        if (date.size() > 0) {
            firstDate = date.get(0);
            lastDate = date.get(date.size()-1);
        } else {
            firstDate = new GregorianCalendar(1900, 1, 1);
            lastDate = new GregorianCalendar(2099, 1, 1);
        }
    } 
}