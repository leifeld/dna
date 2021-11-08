package dna;

import gui.MainWindow;
import logger.LogEvent;
import logger.Logger;
import sql.Sql;

public class Dna {
	public static Dna dna;
	public static Logger logger;
	public static Sql sql;
	public static final String date = "2021-11-08";
	public static final String version = "3.0.0";
	MainWindow mainWindow;
	
	public Dna() {
		logger = new Logger();

		sql = new Sql();

		LogEvent l = new LogEvent(Logger.MESSAGE,
				"DNA started. Version " + version + " (" + date + ").",
				"DNA started. Version " + version + " (" + date + ").");
		Dna.logger.log(l);
		
		mainWindow = new MainWindow();
	}
	
	public static void main(String[] args) {
		dna = new Dna();
	}

	public MainWindow getMainWindow() {
		return mainWindow;
	}
}