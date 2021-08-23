package sql;

public class ConnectionProfile {
	String type, url, user, password;
	int coderId;

	public ConnectionProfile(String type, String url, String user, String password) {
		this.type = type;
		this.url = url;
		this.user = user;
		this.password = password;
		this.coderId = 1; // default value; not part of the constructor
	}
	
	public ConnectionProfile(int coderId, String type, String url, String user, String password) {
		this.type = type;
		this.url = url;
		this.user = user;
		this.password = password;
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
}