package model;

import java.awt.Color;
import java.util.HashMap;

public class Coder {
	int id,
	popupWidth,
	refresh,
	fontSize;
	String name;
	Color color;
	boolean colorByCoder,
	popupDecoration,
	popupAutoComplete,
	permissionAddDocuments,
	permissionEditDocuments,
	permissionDeleteDocuments,
	permissionImportDocuments,
	permissionAddStatements,
	permissionEditStatements,
	permissionDeleteStatements,
	permissionEditAttributes,
	permissionEditRegex,
	permissionEditStatementTypes,
	permissionEditCoders,
	permissionViewOthersDocuments,
	permissionEditOthersDocuments,
	permissionViewOthersStatements,
	permissionEditOthersStatements;
	HashMap<Integer, CoderRelation> coderRelations;
	
	public Coder(int id,
			String name,
			Color color,  // color defined as java.awt.Color
			int refresh,
			int fontSize,
			int popupWidth,
			boolean colorByCoder,
			boolean popupDecoration,
			boolean popupAutoComplete,
			boolean permissionAddDocuments,
			boolean permissionEditDocuments,
			boolean permissionDeleteDocuments,
			boolean permissionImportDocuments,
			boolean permissionAddStatements,
			boolean permissionEditStatements,
			boolean permissionDeleteStatements,
			boolean permissionEditAttributes,
			boolean permissionEditRegex,
			boolean permissionEditStatementTypes,
			boolean permissionEditCoders,
			boolean permissionViewOthersDocuments,
			boolean permissionEditOthersDocuments,
			boolean permissionViewOthersStatements,
			boolean permissionEditOthersStatements,
			HashMap<Integer, CoderRelation> coderRelations) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.refresh = refresh;
		this.fontSize = fontSize;
		this.popupWidth = popupWidth;
		this.colorByCoder = colorByCoder;
		this.popupDecoration = popupDecoration;
		this.popupAutoComplete = popupAutoComplete;
		this.permissionAddDocuments = permissionAddDocuments;
		this.permissionEditDocuments = permissionEditDocuments;
		this.permissionDeleteDocuments = permissionDeleteDocuments;
		this.permissionImportDocuments = permissionImportDocuments;
		this.permissionAddStatements = permissionAddStatements;
		this.permissionEditStatements = permissionEditStatements;
		this.permissionDeleteStatements = permissionDeleteStatements;
		this.permissionEditAttributes = permissionEditAttributes;
		this.permissionEditRegex = permissionEditRegex;
		this.permissionEditStatementTypes = permissionEditStatementTypes;
		this.permissionEditCoders = permissionEditCoders;
		this.permissionViewOthersDocuments = permissionViewOthersDocuments;
		this.permissionEditOthersDocuments = permissionEditOthersDocuments;
		this.permissionViewOthersStatements = permissionViewOthersStatements;
		this.permissionEditOthersStatements = permissionEditOthersStatements;
		this.coderRelations = coderRelations;
	}

	public Coder(int id,
			String name,
			int red,  // color defined as RGB values
			int green,
			int blue,
			int refresh,
			int fontSize,
			int popupWidth,
			boolean colorByCoder,
			boolean popupDecoration,
			boolean popupAutoComplete,
			boolean permissionAddDocuments,
			boolean permissionEditDocuments,
			boolean permissionDeleteDocuments,
			boolean permissionImportDocuments,
			boolean permissionAddStatements,
			boolean permissionEditStatements,
			boolean permissionDeleteStatements,
			boolean permissionEditAttributes,
			boolean permissionEditRegex,
			boolean permissionEditStatementTypes,
			boolean permissionEditCoders,
			boolean permissionViewOthersDocuments,
			boolean permissionEditOthersDocuments,
			boolean permissionViewOthersStatements,
			boolean permissionEditOthersStatements,
			HashMap<Integer, CoderRelation> coderRelations) {
		this.id = id;
		this.name = name;
		this.color = new Color(red, green, blue);
		this.refresh = refresh;
		this.fontSize = fontSize;
		this.popupWidth = popupWidth;
		this.colorByCoder = colorByCoder;
		this.popupDecoration = popupDecoration;
		this.popupAutoComplete = popupAutoComplete;
		this.permissionAddDocuments = permissionAddDocuments;
		this.permissionEditDocuments = permissionEditDocuments;
		this.permissionDeleteDocuments = permissionDeleteDocuments;
		this.permissionImportDocuments = permissionImportDocuments;
		this.permissionAddStatements = permissionAddStatements;
		this.permissionEditStatements = permissionEditStatements;
		this.permissionDeleteStatements = permissionDeleteStatements;
		this.permissionEditAttributes = permissionEditAttributes;
		this.permissionEditRegex = permissionEditRegex;
		this.permissionEditStatementTypes = permissionEditStatementTypes;
		this.permissionEditCoders = permissionEditCoders;
		this.permissionViewOthersDocuments = permissionViewOthersDocuments;
		this.permissionEditOthersDocuments = permissionEditOthersDocuments;
		this.permissionViewOthersStatements = permissionViewOthersStatements;
		this.permissionEditOthersStatements = permissionEditOthersStatements;
		this.coderRelations = coderRelations;
	}

	/**
	 * Create a minimal-information dummy Coder for table display.
	 * 
	 * @param id     Coder ID.
	 * @param name   Name of the coder.
	 * @param color  Color of the coder.
	 */
	public Coder(int id,
			String name,
			Color color) { // color defined as java.awt.Color
		this.id = id;
		this.name = name;
		this.color = color;
		this.refresh = 0;
		this.fontSize = 14;
		this.popupWidth = 300;
		this.colorByCoder = false;
		this.popupDecoration = false;
		this.popupAutoComplete = false;
		this.permissionAddDocuments = false;
		this.permissionEditDocuments = false;
		this.permissionDeleteDocuments = false;
		this.permissionImportDocuments = false;
		this.permissionAddStatements = false;
		this.permissionEditStatements = false;
		this.permissionDeleteStatements = false;
		this.permissionEditAttributes = false;
		this.permissionEditRegex = false;
		this.permissionEditStatementTypes = false;
		this.permissionEditCoders = false;
		this.permissionViewOthersDocuments = false;
		this.permissionEditOthersDocuments = false;
		this.permissionViewOthersStatements = false;
		this.permissionEditOthersStatements = false;
		this.coderRelations = new HashMap<Integer, CoderRelation>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getRefresh() {
		return refresh;
	}

	public void setRefresh(int refresh) {
		this.refresh = refresh;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public int getPopupWidth() {
		return popupWidth;
	}

	public void setPopupWidth(int popupWidth) {
		this.popupWidth = popupWidth;
	}

	/**
	 * @return the colorByCoder
	 */
	public boolean isColorByCoder() {
		return colorByCoder;
	}

	/**
	 * @param colorByCoder the colorByCoder to set
	 */
	public void setColorByCoder(boolean colorByCoder) {
		this.colorByCoder = colorByCoder;
	}

	/**
	 * @return the popupDecoration
	 */
	public boolean isPopupDecoration() {
		return popupDecoration;
	}

	/**
	 * @param popupDecoration the popupDecoration to set
	 */
	public void setPopupDecoration(boolean popupDecoration) {
		this.popupDecoration = popupDecoration;
	}

	/**
	 * @return the popupAutoComplete
	 */
	public boolean isPopupAutoComplete() {
		return popupAutoComplete;
	}

	/**
	 * @param popupAutoComplete the popupAutoComplete to set
	 */
	public void setPopupAutoComplete(boolean popupAutoComplete) {
		this.popupAutoComplete = popupAutoComplete;
	}

	/**
	 * @return the permissionAddDocuments
	 */
	public boolean isPermissionAddDocuments() {
		return permissionAddDocuments;
	}

	/**
	 * @param permissionAddDocuments the permissionAddDocuments to set
	 */
	public void setPermissionAddDocuments(boolean permissionAddDocuments) {
		this.permissionAddDocuments = permissionAddDocuments;
	}

	/**
	 * @return the permissionEditDocuments
	 */
	public boolean isPermissionEditDocuments() {
		return permissionEditDocuments;
	}

	/**
	 * @param permissionEditDocuments the permissionEditDocuments to set
	 */
	public void setPermissionEditDocuments(boolean permissionEditDocuments) {
		this.permissionEditDocuments = permissionEditDocuments;
	}

	/**
	 * @return the permissionDeleteDocuments
	 */
	public boolean isPermissionDeleteDocuments() {
		return permissionDeleteDocuments;
	}

	/**
	 * @param permissionDeleteDocuments the permissionDeleteDocuments to set
	 */
	public void setPermissionDeleteDocuments(boolean permissionDeleteDocuments) {
		this.permissionDeleteDocuments = permissionDeleteDocuments;
	}

	/**
	 * @return the permissionImportDocuments
	 */
	public boolean isPermissionImportDocuments() {
		return permissionImportDocuments;
	}

	/**
	 * @param permissionImportDocuments the permissionImportDocuments to set
	 */
	public void setPermissionImportDocuments(boolean permissionImportDocuments) {
		this.permissionImportDocuments = permissionImportDocuments;
	}

	/**
	 * @return the permissionAddStatements
	 */
	public boolean isPermissionAddStatements() {
		return permissionAddStatements;
	}

	/**
	 * @param permissionAddStatements the permissionAddStatements to set
	 */
	public void setPermissionAddStatements(boolean permissionAddStatements) {
		this.permissionAddStatements = permissionAddStatements;
	}

	/**
	 * @return the permissionEditStatements
	 */
	public boolean isPermissionEditStatements() {
		return permissionEditStatements;
	}

	/**
	 * @param permissionEditStatements the permissionEditStatements to set
	 */
	public void setPermissionEditStatements(boolean permissionEditStatements) {
		this.permissionEditStatements = permissionEditStatements;
	}

	/**
	 * @return the permissionDeleteStatements
	 */
	public boolean isPermissionDeleteStatements() {
		return permissionDeleteStatements;
	}

	/**
	 * @param permissionDeleteStatements the permissionDeleteStatements to set
	 */
	public void setPermissionDeleteStatements(boolean permissionDeleteStatements) {
		this.permissionDeleteStatements = permissionDeleteStatements;
	}

	/**
	 * @return the permissionEditAttributes
	 */
	public boolean isPermissionEditAttributes() {
		return permissionEditAttributes;
	}

	/**
	 * @param permissionEditAttributes the permissionEditAttributes to set
	 */
	public void setPermissionEditAttributes(boolean permissionEditAttributes) {
		this.permissionEditAttributes = permissionEditAttributes;
	}

	/**
	 * @return the permissionEditRegex
	 */
	public boolean isPermissionEditRegex() {
		return permissionEditRegex;
	}

	/**
	 * @param permissionEditRegex the permissionEditRegex to set
	 */
	public void setPermissionEditRegex(boolean permissionEditRegex) {
		this.permissionEditRegex = permissionEditRegex;
	}

	/**
	 * @return the permissionEditStatementTypes
	 */
	public boolean isPermissionEditStatementTypes() {
		return permissionEditStatementTypes;
	}

	/**
	 * @param permissionEditStatementTypes the permissionEditStatementTypes to set
	 */
	public void setPermissionEditStatementTypes(boolean permissionEditStatementTypes) {
		this.permissionEditStatementTypes = permissionEditStatementTypes;
	}

	/**
	 * @return the permissionEditCoders
	 */
	public boolean isPermissionEditCoders() {
		return permissionEditCoders;
	}

	/**
	 * @param permissionEditCoders the permissionEditCoders to set
	 */
	public void setPermissionEditCoders(boolean permissionEditCoders) {
		this.permissionEditCoders = permissionEditCoders;
	}

	/**
	 * @return the permissionViewOthersDocuments
	 */
	public boolean isPermissionViewOthersDocuments() {
		return permissionViewOthersDocuments;
	}

	/**
	 * @param permissionViewOthersDocuments the permissionViewOthersDocuments to set
	 */
	public void setPermissionViewOthersDocuments(boolean permissionViewOthersDocuments) {
		this.permissionViewOthersDocuments = permissionViewOthersDocuments;
	}

	/**
	 * @return the permissionEditOthersDocuments
	 */
	public boolean isPermissionEditOthersDocuments() {
		return permissionEditOthersDocuments;
	}

	/**
	 * @param permissionEditOthersDocuments the permissionEditOthersDocuments to set
	 */
	public void setPermissionEditOthersDocuments(boolean permissionEditOthersDocuments) {
		this.permissionEditOthersDocuments = permissionEditOthersDocuments;
	}

	/**
	 * @return the permissionViewOthersStatements
	 */
	public boolean isPermissionViewOthersStatements() {
		return permissionViewOthersStatements;
	}

	/**
	 * @param permissionViewOthersStatements the permissionViewOthersStatements to set
	 */
	public void setPermissionViewOthersStatements(boolean permissionViewOthersStatements) {
		this.permissionViewOthersStatements = permissionViewOthersStatements;
	}

	/**
	 * @return the permissionEditOthersStatements
	 */
	public boolean isPermissionEditOthersStatements() {
		return permissionEditOthersStatements;
	}

	/**
	 * @param permissionEditOthersStatements the permissionEditOthersStatements to set
	 */
	public void setPermissionEditOthersStatements(boolean permissionEditOthersStatements) {
		this.permissionEditOthersStatements = permissionEditOthersStatements;
	}

	/**
	 * @return the coderRelations
	 */
	public HashMap<Integer, CoderRelation> getCoderRelations() {
		return coderRelations;
	}

	/**
	 * @param coderRelations the coderRelations to set
	 */
	public void setCoderRelations(HashMap<Integer, CoderRelation> coderRelations) {
		this.coderRelations = coderRelations;
	}
}