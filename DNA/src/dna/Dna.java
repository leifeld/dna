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
	public static final String date = "2022-02-02";
	public static final String version = "3.0.1";
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
		
		// TODO: remove this placeholder and add database update log events throughout the code
		LogEvent l2 = new LogEvent(Logger.UPDATE,
				"Testing log events for database updates.",
				"Please ignore this message. It is just a placeholder to test if database updates can be logged. It will be removed in the future when DNA is able to show database updates in the log.");
		Dna.logger.log(l2);
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