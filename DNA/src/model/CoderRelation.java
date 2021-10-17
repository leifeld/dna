package model;

public class CoderRelation {
	boolean viewDocuments, viewStatements, editDocuments, editStatements;

	public CoderRelation(boolean viewDocuments, boolean viewStatements, boolean editDocuments, boolean editStatements) {
		this.viewDocuments = viewDocuments;
		this.viewStatements = viewStatements;
		this.editDocuments = editDocuments;
		this.editStatements = editStatements;
	}

	/**
	 * @return the viewDocuments
	 */
	public boolean isViewDocuments() {
		return viewDocuments;
	}

	/**
	 * @param viewDocuments the viewDocuments to set
	 */
	public void setViewDocuments(boolean viewDocuments) {
		this.viewDocuments = viewDocuments;
	}

	/**
	 * @return the viewStatements
	 */
	public boolean isViewStatements() {
		return viewStatements;
	}

	/**
	 * @param viewStatements the viewStatements to set
	 */
	public void setViewStatements(boolean viewStatements) {
		this.viewStatements = viewStatements;
	}

	/**
	 * @return the editDocuments
	 */
	public boolean isEditDocuments() {
		return editDocuments;
	}

	/**
	 * @param editDocuments the editDocuments to set
	 */
	public void setEditDocuments(boolean editDocuments) {
		this.editDocuments = editDocuments;
	}

	/**
	 * @return the editStatements
	 */
	public boolean isEditStatements() {
		return editStatements;
	}

	/**
	 * @param editStatements the editStatements to set
	 */
	public void setEditStatements(boolean editStatements) {
		this.editStatements = editStatements;
	}
}