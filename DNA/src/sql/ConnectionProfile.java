package sql;

public class ConnectionProfile {
	String type, url, user, password;
	int refresh, coderId;

	public ConnectionProfile(String type, String url, String user, String password) {
		this.type = type;
		this.url = url;
		this.user = user;
		this.password = password;

		// set default values that are not part of the constructor
		this.coderId = 1;
		this.refresh = 20;
		if (type.equals("sqlite")) {
			refresh = 0;
		}
	}
	
	public ConnectionProfile(int coderId, String type, String url, String user, String password, int refresh) {
		this.type = type;
		this.url = url;
		this.user = user;
		this.password = password;
		this.refresh = refresh;
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
	
	public int getRefresh() {
		return refresh;
	}
	
	public void setRefresh(int refresh) {
		this.refresh = refresh;
	}
}