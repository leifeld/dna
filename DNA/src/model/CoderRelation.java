package model;

import java.awt.Color;

/**
 * Represents relations with a target coder. Any source coder can hold multiple
 * permissions for all other (target) coders in a hash map of coder relations.
 * For each target coder, four permissions are saved: view/edit
 * documents/statements.
 */
public class CoderRelation {
	int targetCoderId;
	String targetCoderName;
	Color targetCoderColor;
	boolean viewDocuments, viewStatements, editDocuments, editStatements;

	/**
	 * Creates a new coder relation object. This constructor contains the name
	 * and color of the target coder because it is sometimes important to
	 * display the name and color, for example in the coder manager.
	 * 
	 * @param targetCoderId    The ID of the coder with whom relations are held.
	 * @param targetCoderName  The name of the target coder.
	 * @param targetCoderColor The color of the target coder.
	 * @param viewDocuments    Permission to view the target coder's documents?
	 * @param editDocuments    Permission to edit the target coder's documents?
	 * @param viewStatements   Permission to view the target coder's statements?
	 * @param editStatements   Permission to edit the target coder's statements?
	 */
	public CoderRelation(int targetCoderId, String targetCoderName, Color targetCoderColor, boolean viewDocuments, boolean viewStatements, boolean editDocuments, boolean editStatements) {
		this.targetCoderId = targetCoderId;
		this.targetCoderName = targetCoderName;
		this.targetCoderColor = targetCoderColor;
		this.viewDocuments = viewDocuments;
		this.viewStatements = viewStatements;
		this.editDocuments = editDocuments;
		this.editStatements = editStatements;
	}

	/**
	 * Creates a new coder relation object. This constructor omits the name and
	 * color of the target coder because it is unimportant or unavailable in
	 * some contexts, such as when saving it to the database.
	 * 
	 * @param targetCoderId   The ID of the coder with whom relations are held.
	 * @param viewDocuments   Permission to view the target coder's documents?
	 * @param editDocuments   Permission to edit the target coder's documents?
	 * @param viewStatements  Permission to view the target coder's statements?
	 * @param editStatements  Permission to edit the target coder's statements?
	 */
	public CoderRelation(int targetCoderId, boolean viewDocuments, boolean editDocuments, boolean viewStatements, boolean editStatements) {
		this.targetCoderId = targetCoderId;
		this.viewDocuments = viewDocuments;
		this.viewStatements = viewStatements;
		this.editDocuments = editDocuments;
		this.editStatements = editStatements;
	}
	
	/**
	 * Copy constructor. Creates a deep clone of an existing coder relation.
	 * 
	 * @param cr The existing coder relation object to clone.
	 */
	public CoderRelation(CoderRelation cr) {
		this.targetCoderId = cr.getTargetCoderId();
		this.targetCoderName = cr.getTargetCoderName();
		this.targetCoderColor = cr.getTargetCoderColor();
		this.viewDocuments = cr.isViewDocuments();
		this.editDocuments = cr.isEditDocuments();
		this.viewStatements = cr.isViewStatements();
		this.editStatements = cr.isEditStatements();
	}

	/**
	 * @return the targetCoderId
	 */
	public int getTargetCoderId() {
		return targetCoderId;
	}

	/**
	 * @param targetCoderId the targetCoderId to set
	 */
	public void setTargetCoderId(int targetCoderId) {
		this.targetCoderId = targetCoderId;
	}

	/**
	 * @return the targetCoderName
	 */
	public String getTargetCoderName() {
		return targetCoderName;
	}

	/**
	 * @param targetCoderName the targetCoderName to set
	 */
	public void setTargetCoderName(String targetCoderName) {
		this.targetCoderName = targetCoderName;
	}

	/**
	 * @return the targetCoderColor
	 */
	public Color getTargetCoderColor() {
		return targetCoderColor;
	}

	/**
	 * @param targetCoderColor the targetCoderColor to set
	 */
	public void setTargetCoderColor(Color targetCoderColor) {
		this.targetCoderColor = targetCoderColor;
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

	/**
	 * Does the coder relation object equal another coder relation object?
	 * 
	 * @param cr Another coder relation object.
	 * @return   A boolean indicator.
	 */
	boolean equals(CoderRelation cr) {
		if (this.targetCoderId != cr.getTargetCoderId() ||
				!this.targetCoderName.equals(cr.getTargetCoderName()) ||
				!this.targetCoderColor.equals(cr.getTargetCoderColor()) ||
				this.viewDocuments != cr.isViewDocuments() ||
				this.editDocuments != cr.isEditDocuments() ||
				this.viewStatements != cr.isViewStatements() ||
				this.editStatements != cr.isEditStatements()) {
			return false;
		} else {
			return true;
		}
	}
}