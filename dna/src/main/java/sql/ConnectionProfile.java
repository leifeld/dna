package sql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import logger.LogEvent;
import logger.Logger;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.AES256TextEncryptor;

import java.io.*;

public class ConnectionProfile {
	/**
	 * The connection type. Valid values are {@code "sqlite"}, {@code "mysql"},
	 * and {@code "postgresql"}.
	 */
	String type;
	
	/**
	 * The file name (if it is an SQLite connection) or URL/server address/IP
	 * address of the server where the database is located.
	 */
	String url;
	
	/**
	 * The login/user name of the database (if it is a MySQL or PostgreSQL
	 * database).
	 */
	String user;
	
	/**
	 * The password that corresponds to the login/user name.
	 */
	String password;
	
	/**
	 * The name of the database (if it is a remote database).
	 */
	String databaseName;
	
	/**
	 * The connection port number (if it is a remote database).
	 */
	int port;
	
	/**
	 * The coder ID of the active coder.
	 */
	int coderId;

	public ConnectionProfile(String type, String url, String databaseName, int port, String user, String password) {
		this.type = type;
		this.url = url;
		this.user = user;
		this.password = password;
		this.databaseName = databaseName;
		this.port = port;
		this.coderId = 1; // default value; not part of the constructor
	}
	
	public ConnectionProfile(int coderId, String type, String url, String databaseName, int port, String user, String password) {
		this.type = type;
		this.url = url;
		this.user = user;
		this.password = password;
		this.databaseName = databaseName;
		this.port = port;
	}

	public ConnectionProfile(String file, String key) {
		ConnectionProfile p = readConnectionProfile(file, key);
		this.type = p.getType();
		this.url = p.getUrl();
		this.user = p.getUser();
		this.password = p.getPassword();
		this.databaseName = p.getDatabaseName();
		this.port = p.getPort();
		this.coderId = p.getCoderId();
	}

	/**
	 * Copy constructor. Creates a deep copy of a connection profile.
	 * 
	 * @param cp  An existing connection profile to be duplicated.
	 */
	public ConnectionProfile(ConnectionProfile cp) {
		this.type = cp.getType();
		this.url = cp.getUrl();
		this.user = cp.getUser();
		this.password = cp.getPassword();
		this.databaseName = cp.getDatabaseName();
		this.port = cp.getPort();
		this.coderId = cp.getCoderId();
	}
	
	public int getCoderId() {
		return coderId;
	}

	public void setCoder(int coderId) {
		this.coderId = coderId;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Return the port.
	 * 
	 * @return The server connection port for connecting to the database.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Return the database name.
	 * 
	 * @return The database name.
	 */
	public String getDatabaseName() {
		return this.databaseName;
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
			dna.Dna.logger.log(l);
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
			dna.Dna.logger.log(l);
		}
	}
}