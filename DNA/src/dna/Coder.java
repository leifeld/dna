package dna;

import java.awt.Color;

public class Coder {
	int id,
	popupWidth,
	colorByCoder,
	refresh,
	fontSize,
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
	String name;
	Color color;

	public Coder(int id,
			String name,
			Color color,  // color defined as java.awt.Color
			int refresh,
			int fontSize,
			int popupWidth,
			int colorByCoder,
			int popupDecoration,
			int popupAutoComplete,
			int permissionAddDocuments,
			int permissionEditDocuments,
			int permissionDeleteDocuments,
			int permissionImportDocuments,
			int permissionAddStatements,
			int permissionEditStatements,
			int permissionDeleteStatements,
			int permissionEditAttributes,
			int permissionEditRegex,
			int permissionEditStatementTypes,
			int permissionEditCoders,
			int permissionViewOthersDocuments,
			int permissionEditOthersDocuments,
			int permissionViewOthersStatements,
			int permissionEditOthersStatements) {
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
	}

	public Coder(int id,
			String name,
			int red,  // color defined as RGB values
			int green,
			int blue,
			int refresh,
			int fontSize,
			int popupWidth,
			int colorByCoder,
			int popupDecoration,
			int popupAutoComplete,
			int permissionAddDocuments,
			int permissionEditDocuments,
			int permissionDeleteDocuments,
			int permissionImportDocuments,
			int permissionAddStatements,
			int permissionEditStatements,
			int permissionDeleteStatements,
			int permissionEditAttributes,
			int permissionEditRegex,
			int permissionEditStatementTypes,
			int permissionEditCoders,
			int permissionViewOthersDocuments,
			int permissionEditOthersDocuments,
			int permissionViewOthersStatements,
			int permissionEditOthersStatements) {
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

	public int getColorByCoder() {
		return colorByCoder;
	}

	public void setColorByCoder(int colorByCoder) {
		this.colorByCoder = colorByCoder;
	}

	public int getPopupDecoration() {
		return popupDecoration;
	}

	public void setPopupDecoration(int popupDecoration) {
		this.popupDecoration = popupDecoration;
	}

	public int getPopupAutoComplete() {
		return popupAutoComplete;
	}

	public void setPopupAutoComplete(int popupAutoComplete) {
		this.popupAutoComplete = popupAutoComplete;
	}

	public int getPermissionAddDocuments() {
		return permissionAddDocuments;
	}

	public void setPermissionAddDocuments(int permissionAddDocuments) {
		this.permissionAddDocuments = permissionAddDocuments;
	}

	public int getPermissionEditDocuments() {
		return permissionEditDocuments;
	}

	public void setPermissionEditDocuments(int permissionEditDocuments) {
		this.permissionEditDocuments = permissionEditDocuments;
	}

	public int getPermissionDeleteDocuments() {
		return permissionDeleteDocuments;
	}

	public void setPermissionDeleteDocuments(int permissionDeleteDocuments) {
		this.permissionDeleteDocuments = permissionDeleteDocuments;
	}

	public int getPermissionImportDocuments() {
		return permissionImportDocuments;
	}

	public void setPermissionImportDocuments(int permissionImportDocuments) {
		this.permissionImportDocuments = permissionImportDocuments;
	}

	public int getPermissionAddStatements() {
		return permissionAddStatements;
	}

	public void setPermissionAddStatements(int permissionAddStatements) {
		this.permissionAddStatements = permissionAddStatements;
	}

	public int getPermissionEditStatements() {
		return permissionEditStatements;
	}

	public void setPermissionEditStatements(int permissionEditStatements) {
		this.permissionEditStatements = permissionEditStatements;
	}

	public int getPermissionDeleteStatements() {
		return permissionDeleteStatements;
	}

	public void setPermissionDeleteStatements(int permissionDeleteStatements) {
		this.permissionDeleteStatements = permissionDeleteStatements;
	}

	public int getPermissionEditAttributes() {
		return permissionEditAttributes;
	}

	public void setPermissionEditAttributes(int permissionEditAttributes) {
		this.permissionEditAttributes = permissionEditAttributes;
	}

	public int getPermissionEditRegex() {
		return permissionEditRegex;
	}

	public void setPermissionEditRegex(int permissionEditRegex) {
		this.permissionEditRegex = permissionEditRegex;
	}

	public int getPermissionEditStatementTypes() {
		return permissionEditStatementTypes;
	}

	public void setPermissionEditStatementTypes(int permissionEditStatementTypes) {
		this.permissionEditStatementTypes = permissionEditStatementTypes;
	}

	public int getPermissionEditCoders() {
		return permissionEditCoders;
	}

	public void setPermissionEditCoders(int permissionEditCoders) {
		this.permissionEditCoders = permissionEditCoders;
	}

	public int getPermissionViewOthersDocuments() {
		return permissionViewOthersDocuments;
	}

	public void setPermissionViewOthersDocuments(int permissionViewOthersDocuments) {
		this.permissionViewOthersDocuments = permissionViewOthersDocuments;
	}

	public int getPermissionEditOthersDocuments() {
		return permissionEditOthersDocuments;
	}

	public void setPermissionEditOthersDocuments(int permissionEditOthersDocuments) {
		this.permissionEditOthersDocuments = permissionEditOthersDocuments;
	}

	public int getPermissionViewOthersStatements() {
		return permissionViewOthersStatements;
	}

	public void setPermissionViewOthersStatements(int permissionViewOthersStatements) {
		this.permissionViewOthersStatements = permissionViewOthersStatements;
	}

	public int getPermissionEditOthersStatements() {
		return permissionEditOthersStatements;
	}

	public void setPermissionEditOthersStatements(int permissionEditOthersStatements) {
		this.permissionEditOthersStatements = permissionEditOthersStatements;
	}
}