package dna;

import guiCoder.GuiCoder;
import sql.Sql;

public class Dna {
	public static Dna dna;
	public static GuiCoder guiCoder;
	public static Logger logger;
	public static Sql sql;
	public final String date;
	public final String version;
	
	public Dna() {
		date = "2021-08-19";
		version = "3.0.0";

		logger = new Logger();
		
		guiCoder = new GuiCoder();
		logger.addListener(guiCoder);
	}
	
	public static void main(String[] args) {
		dna = new Dna();
	}
}