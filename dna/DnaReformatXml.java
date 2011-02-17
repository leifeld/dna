package dna;

import java.io.*;

public class DnaReformatXml {
   
    String xmlContents;
    String ls = System.getProperty("line.separator");

    public DnaReformatXml( String filename ) {
    	xmlContents = this.loadFile( filename );
    	xmlContents = xmlContents.replaceAll("\u0026\u006c\u0074\u003b", "\u003c");
    	xmlContents = xmlContents.replaceAll("\u0026\u0067\u0074\u003b", "\u003e");
    	xmlContents = xmlContents.replaceAll("&#xD;", "");
    	saveFile( xmlContents, filename );
    }
   
    public String loadFile( String filename ) {
        StringBuffer contents = new StringBuffer("");
        try {
            BufferedReader input = new BufferedReader(new FileReader(filename));
            String line = input.readLine();
            while ( line != null ) {
            	contents.append( line );
            	contents.append( ls );
            	line = input.readLine();
            }
            input.close();
        }
        catch (IOException e) {
            System.out.println("Error while reading the file " + filename);
        }
        return contents.toString();
    }
    
    public void saveFile( String contents, String filename ) {
    	try {
            BufferedWriter output = new BufferedWriter(new FileWriter(filename));
            output.write(contents);
            output.close();
        }
        catch (IOException e) {
            System.out.println("Error while saving the manipulated text to " + filename);
        }
    }
}