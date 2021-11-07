package sql;

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
}