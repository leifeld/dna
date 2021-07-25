package dna.dataStructures;

public class CoderRelation {
	int id, coder, otherCoder;
	boolean viewStatements, editStatements, viewDocuments, editDocuments;

	public CoderRelation(int id, int coder, int otherCoder, boolean viewStatements, boolean editStatements, boolean viewDocuments, boolean editDocuments) {
		this.id = id;
		this.coder = coder;
		this.otherCoder = otherCoder;
		this.viewStatements = viewStatements;
		this.editStatements = editStatements;
		this.viewDocuments = viewDocuments;
		this.editDocuments = editDocuments;
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
}
