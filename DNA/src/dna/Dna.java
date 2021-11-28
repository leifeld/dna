package dna;

import gui.MainWindow;
import logger.LogEvent;
import logger.Logger;
import sql.Sql;

/**
 * Main class of the Discourse Network Analyzer (DNA).
 * 
 * @author Philip Leifeld {@literal <philip.leifeld@essex.ac.uk>}
 */
public class Dna {
	public static Dna dna;
	public static Logger logger;
	public static Sql sql;
	public static final String date = "2021-11-28";
	public static final String version = "3.0.0";
	MainWindow mainWindow;
	
	/**
	 * Create a new instance of DNA including the GUI.
	 */
	public Dna() {
		logger = new Logger();

		sql = new Sql();

		LogEvent l = new LogEvent(Logger.MESSAGE,
				"DNA started. Version " + version + " (" + date + ").",
				"DNA started. Version " + version + " (" + date + ").");
		Dna.logger.log(l);
		
		mainWindow = new MainWindow();
	}
	
	/**
	 * Start DNA.
	 * 
	 * @param args Any arguments from the terminal/command line/shell.
	 */
	public static void main(String[] args) {
		dna = new Dna();
	}
}