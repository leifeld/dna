package dna;

import java.util.HashMap;

public class Dna {
	static Dna dna;
	DataAccess db;
	Gui gui;
	
	public Dna () {
		//String dbfile = "/home/philip/test.dna";
		//db = new DataAccess(dbfile);
		//db.createTables();
		
		//HashMap<String, String> dnaStatementMap = new HashMap<String, String>();
		//dnaStatementMap.put("person", "short text");
		//dnaStatementMap.put("organization", "short text");
		//dnaStatementMap.put("category", "short text");
		//dnaStatementMap.put("agreement", "boolean");
		//db.insertStatementType("DNAStatement", 0, 255, 255, dnaStatementMap);
		db = new DataAccess();
		gui = new Gui();
	}
	
	public static void main (String[] args) {
		dna = new Dna();
	}
}
