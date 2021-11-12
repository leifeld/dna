package model;

import java.awt.Color;

public class CoderRelation {
	int targetCoderId;
	String targetCoderName;
	Color targetCoderColor;
	boolean viewDocuments, viewStatements, editDocuments, editStatements;

	public CoderRelation(int targetCoderId, String targetCoderName, Color targetCoderColor, boolean viewDocuments, boolean viewStatements, boolean editDocuments, boolean editStatements) {
		this.targetCoderId = targetCoderId;
		this.targetCoderName = targetCoderName;
		this.targetCoderColor = targetCoderColor;
		this.viewDocuments = viewDocuments;
		this.viewStatements = viewStatements;
		this.editDocuments = editDocuments;
		this.editStatements = editStatements;
	}

	public CoderRelation(int targetCoderId, boolean viewDocuments, boolean viewStatements, boolean editDocuments, boolean editStatements) {
		this.targetCoderId = targetCoderId;
		this.viewDocuments = viewDocuments;
		this.viewStatements = viewStatements;
		this.editDocuments = editDocuments;
		this.editStatements = editStatements;
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
}