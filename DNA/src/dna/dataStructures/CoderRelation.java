package dna.dataStructures;

public class CoderRelation {
	int id, coder, otherCoder, permission;
	String type;

	public CoderRelation(int id, int coder, int otherCoder, int permission, String type) {
		this.id = id;
		this.coder = coder;
		this.otherCoder = otherCoder;
		this.permission = permission;
		this.type = type;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the coder
	 */
	public int getCoder() {
		return coder;
	}

	/**
	 * @param coder the coder to set
	 */
	public void setCoder(int coder) {
		this.coder = coder;
	}

	/**
	 * @return the otherCoder
	 */
	public int getOtherCoder() {
		return otherCoder;
	}

	/**
	 * @param otherCoder the otherCoder to set
	 */
	public void setOtherCoder(int otherCoder) {
		this.otherCoder = otherCoder;
	}

	/**
	 * @return the permission
	 */
	public int getPermission() {
		return permission;
	}

	/**
	 * @param permission the permission to set
	 */
	public void setPermission(int permission) {
		this.permission = permission;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}
