package dna;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.AES256TextEncryptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import gui.MainWindow;
import logger.LogEvent;
import logger.Logger;
import sql.ConnectionProfile;
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
	public static final String date = "2022-06-12";
	public static final String version = "3.0.7";
	public MainWindow mainWindow;
	public HeadlessDna headlessDna;
	
	/**
	 * Create a new instance of DNA including the GUI.
	 */
	public Dna(String[] args) {
		logger = new Logger();

		sql = new Sql();

		LogEvent l = new LogEvent(Logger.MESSAGE,
				"DNA started. Version " + version + " (" + date + ").",
				"DNA started. Version " + version + " (" + date + ").");
		Dna.logger.log(l);

		if (args != null && args.length > 0 && args[0].equals("headless")) {
			mainWindow = new MainWindow();
			headlessDna = new HeadlessDna();
		} else {
			mainWindow = new MainWindow();
			mainWindow.setVisible(true);
		}
		
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
		dna = new Dna(args);
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

	/**
	 * Read in a saved connection profile from a JSON file, decrypt the
	 * credentials, and return the connection profile.
	 * 
	 * @param file  The file name including path of the JSON connection profile
	 * @param key   The key/password of the coder to decrypt the credentials
	 * @return      Decrypted connection profile
	 */
	public static ConnectionProfile readConnectionProfile(String file, String key) throws EncryptionOperationNotPossibleException {
		// read connection profile JSON file in, in String format but with encrypted credentials
		ConnectionProfile cp = null;
		Gson gson = new Gson();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			cp = gson.fromJson(br, ConnectionProfile.class);
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"Failed to read connection profile.",
					"Tried to read a connection profile from a JSON file and failed. File: " + file + ".",
					e);
			Dna.logger.log(l);
		}
		
		// decrypt the URL, user name, and SQL connection password inside the profile
		AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
		textEncryptor.setPassword(key);
		cp.setUrl(textEncryptor.decrypt(cp.getUrl()));
		cp.setUser(textEncryptor.decrypt(cp.getUser()));
		cp.setPassword(textEncryptor.decrypt(cp.getPassword()));
		
		return cp;
	}

	/**
	 * Take a decrypted connection profile, encrypt the credentials, and write
	 * it to a JSON file on disk.
	 * 
	 * @param file  The file name including full path as a String
	 * @param cp    The connection profile to be encrypted and saved
	 * @param key   The key/password of the coder to encrypt the credentials
	 */
	public static void writeConnectionProfile(String file, ConnectionProfile cp, String key) {
		// encrypt URL, user, and password using Jasypt
		AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
		textEncryptor.setPassword(key);
		cp.setUrl(textEncryptor.encrypt(cp.getUrl()));
		cp.setUser(textEncryptor.encrypt(cp.getUser()));
		cp.setPassword(textEncryptor.encrypt(cp.getPassword()));
		
		// serialize Connection object to JSON file and save to disk
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			Gson prettyGson = new GsonBuilder()
		            .setPrettyPrinting()
		            .serializeNulls()
		            .disableHtmlEscaping()
		            .create();
			String g = prettyGson.toJson(cp);
			writer.write(g);
		} catch (IOException e) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"Failed to write connection profile.",
					"Tried to write a connection profile to a JSON file and failed. File: " + file + ".",
					e);
			Dna.logger.log(l);
		}
	}
}