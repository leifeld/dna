package dna;

import java.util.ArrayList;
import java.util.List;

import gui.MainWindow;
import logger.LogEvent;
import logger.Logger;
import sql.Sql;

public class Dna {
	public static Dna dna;
	public static Logger logger;
	public static Sql sql;
	public static final String date = "2021-08-25";
	public static final String version = "3.0.0";
	MainWindow mainWindow;
	
	/**
	 * Keep a list of classes that depend on resets of the {@link #sql} slot,
	 * including setting it to null, for example the document panel. 
	 */
	private static List<SqlListener> sqlListeners = new ArrayList<SqlListener>();
	/**
	 * Keep a list of classes that depend on which coder is selected and what
	 * characteristics the coder has, for examples text panel.
	 */
	public static List<CoderListener> coderListeners = new ArrayList<CoderListener>();
	
	public Dna() {
		logger = new Logger();

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

	/**
	 * An interface for listeners for the presence or absence of an SQL
	 * database. For example, the GUI should react to changes here by switching
	 * certain buttons or actions on or off. The GUI components thus need to be
	 * registered as listeners.
	 */
	public interface SqlListener {
		void adjustToDatabaseState();
	}

	/**
	 * Add an Sql listener. This can be an object that implements the
	 * {@link SqlListener} interface.
	 * 
	 * @param listener An object implementing the {@link SqlListener} interface.
	 */
	public static void addSqlListener(SqlListener listener) {
        sqlListeners.add(listener);
    }
	
	/**
	 * Set a new SQL database. Can also be set null if there is not active
	 * database.
	 * 
	 * @param sql  The {@link sql.Sql Sql} object to store.
	 */
	public static void setSql(Sql sql) {
		Dna.sql = sql;
		Dna.logger.clear();
		for (SqlListener listener : sqlListeners) {
			listener.adjustToDatabaseState();
		}
		fireCoderChange();
	}

	/**
	 * An interface for listeners for changes in the active coder, including
	 * selection of a different coder and changes in any coder details, such as
	 * permissions. For example, the GUI should react to permission changes
	 * or change of coder by enabling or disabling statement popups windows,
	 * repainting statements in the text etc. The GUI components thus need to be
	 * registered as listeners.
	 */
	public interface CoderListener {
		void adjustToChangedCoder();
	}

	/**
	 * Add a coder listener. This can be an object that implements the
	 * {@link CoderListener} interface.
	 * 
	 * @param listener An object implementing the {@link CoderListener}
	 *   interface.
	 */
	public static void addCoderListener(CoderListener listener) {
        coderListeners.add(listener);
    }
	
	/**
	 * Notify listeners that the active coder has changed.
	 */
	public static void fireCoderChange() {
		for (CoderListener listener : coderListeners) {
			listener.adjustToChangedCoder();
		}
	}
	
	/**
	 * Set a new active coder.
	 * 
	 * @param coderId
	 */
	public void changeActiveCoder(int coderId) {
		Dna.sql.getConnectionProfile().setCoder(coderId);
		Dna.sql.setActiveCoder();
		fireCoderChange();
	}
}