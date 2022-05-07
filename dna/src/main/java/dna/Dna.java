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
	public static final String date = "2022-05-07";
	public static final String version = "3.0.6";
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
	 * @throws Exception 
	 */
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
		dna = new Dna();
	}

	/**
	 * Default exception handler. This catches all uncaught exceptions and logs
	 * them in the logger, instead of printing them on the terminal.
	 */
	private static final class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
    		LogEvent le = new LogEvent(Logger.ERROR,
            		"Uncaught exception.",
            		"An uncaught exception occurred. This is most likely a bug. Please open a new issue at https://github.com/leifeld/dna/issues/ and paste the log event stack trace and the exception stack trace there as part of your error description, along with details of what happened and under what circumstances. Thank you.",
            		e);
            logger.log(le);
        }
    }
}
