package dna.dataStructures;

public class Coder {
	int id, red, green, blue;
	String name, password;
	boolean editOtherDocuments, viewOtherDocuments, editOtherStatements, viewOtherStatements, editStatementTypes, editRegex, addDocuments, addStatements, editCoders;
	
	public Coder(int id, String name, int red, int green, int blue, String password, boolean editOtherDocuments, boolean viewOtherDocuments, boolean editOtherStatements, boolean viewOtherStatements, boolean editStatementTypes, boolean editRegex, boolean addDocuments, boolean addStatements, boolean editCoders) {
		this.id = id;
		this.name = name;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.password = password;
		this.editOtherDocuments = editOtherDocuments;
		this.editOtherStatements = editOtherStatements;
		this.viewOtherStatements = viewOtherStatements;
		this.editStatementTypes = editStatementTypes;
		this.editRegex = editRegex;
		this.addDocuments = addDocuments;
		this.addStatements = addStatements;
		this.editCoders = editCoders;
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
	 * @return the red
	 */
	public int getRed() {
		return red;
	}

	/**
	 * @param red the red to set
	 */
	public void setRed(int red) {
		this.red = red;
	}

	/**
	 * @return the green
	 */
	public int getGreen() {
		return green;
	}

	/**
	 * @param green the green to set
	 */
	public void setGreen(int green) {
		this.green = green;
	}

	/**
	 * @return the blue
	 */
	public int getBlue() {
		return blue;
	}

	/**
	 * @param blue the blue to set
	 */
	public void setBlue(int blue) {
		this.blue = blue;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the editOtherDocuments
	 */
	public boolean isEditOtherDocuments() {
		return editOtherDocuments;
	}

	/**
	 * @param editOtherDocuments the editOtherDocuments to set
	 */
	public void setEditOtherDocuments(boolean editOtherDocuments) {
		this.editOtherDocuments = editOtherDocuments;
	}

	/**
	 * @return the viewOtherDocuments
	 */
	public boolean isViewOtherDocuments() {
		return viewOtherDocuments;
	}

	/**
	 * @param viewOtherDocuments the viewOtherDocuments to set
	 */
	public void setViewOtherDocuments(boolean viewOtherDocuments) {
		this.viewOtherDocuments = viewOtherDocuments;
	}

	/**
	 * @return the editOtherStatements
	 */
	public boolean isEditOtherStatements() {
		return editOtherStatements;
	}

	/**
	 * @param editOtherStatements the editOtherStatements to set
	 */
	public void setEditOtherStatements(boolean editOtherStatements) {
		this.editOtherStatements = editOtherStatements;
	}

	/**
	 * @return the viewOtherStatements
	 */
	public boolean isViewOtherStatements() {
		return viewOtherStatements;
	}

	/**
	 * @param viewOtherStatements the viewOtherStatements to set
	 */
	public void setViewOtherStatements(boolean viewOtherStatements) {
		this.viewOtherStatements = viewOtherStatements;
	}

	/**
	 * @return the editStatementTypes
	 */
	public boolean isEditStatementTypes() {
		return editStatementTypes;
	}

	/**
	 * @param editStatementTypes the editStatementTypes to set
	 */
	public void setEditStatementTypes(boolean editStatementTypes) {
		this.editStatementTypes = editStatementTypes;
	}

	/**
	 * @return the editRegex
	 */
	public boolean isEditRegex() {
		return editRegex;
	}

	/**
	 * @param editRegex the editRegex to set
	 */
	public void setEditRegex(boolean editRegex) {
		this.editRegex = editRegex;
	}

	/**
	 * @return the addDocuments
	 */
	public boolean isAddDocuments() {
		return addDocuments;
	}

	/**
	 * @param addDocuments the addDocuments to set
	 */
	public void setAddDocuments(boolean addDocuments) {
		this.addDocuments = addDocuments;
	}

	/**
	 * @return the addStatements
	 */
	public boolean isAddStatements() {
		return addStatements;
	}

	/**
	 * @param addStatements the addStatements to set
	 */
	public void setAddStatements(boolean addStatements) {
		this.addStatements = addStatements;
	}

	/**
	 * @return the editCoders
	 */
	public boolean isEditCoders() {
		return editCoders;
	}

	/**
	 * @param editCoders the editCoders to set
	 */
	public void setEditCoders(boolean editCoders) {
		this.editCoders = editCoders;
	}
	
}