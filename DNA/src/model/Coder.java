package model;

import java.awt.Color;
import java.util.HashMap;

/**
 * Represents a coder, including all permissions and coder-specific settings.
 */
public class Coder {
	private int id, popupWidth, refresh, fontSize;
	private String name;
	private Color color;
	private boolean colorByCoder, popupDecoration, popupAutoComplete;
	private boolean permissionAddDocuments,	permissionEditDocuments, permissionDeleteDocuments,	permissionImportDocuments;
	private boolean permissionAddStatements, permissionEditStatements, permissionDeleteStatements;
	private boolean permissionEditAttributes, permissionEditRegex, permissionEditStatementTypes, permissionEditCoders, permissionEditCoderRelations;
	private boolean permissionViewOthersDocuments, permissionEditOthersDocuments, permissionViewOthersStatements, permissionEditOthersStatements;
	private HashMap<Integer, CoderRelation> coderRelations;

	/**
	 * Create a coder with full information.
	 * 
	 * @param id                             The ID of the coder.
	 * @param name                           The name of the coder.
	 * @param color                          The color of the coder.
	 * @param refresh                        Refresh rate in seconds.
	 * @param fontSize                       Text font size in px (default 14).
	 * @param popupWidth                     Short text field width for popups.
	 * @param colorByCoder                   Color statements in text by coder?
	 * @param popupDecoration                Show popup window decoration?
	 * @param popupAutoComplete              Auto-complete short text fields?
	 * @param permissionAddDocuments         Permission to add documents?
	 * @param permissionEditDocuments        Permission to edit documents?
	 * @param permissionDeleteDocuments      Permission to delete documents?
	 * @param permissionImportDocuments      Permission to import documents?
	 * @param permissionAddStatements        Permission to add statements?
	 * @param permissionEditStatements       Permission to edit statements?
	 * @param permissionDeleteStatements     Permission to delete statements?
	 * @param permissionEditAttributes       Permission to edit attributes?
	 * @param permissionEditRegex            Permission to edit regexes?
	 * @param permissionEditStatementTypes   Permission to edit statemnt types.
	 * @param permissionEditCoders           Permission to edit coders.
	 * @param permissionEditCoderRelations   Permission to edit coder relations.
	 * @param permissionViewOthersDocuments  Permission to view others' docs.
	 * @param permissionEditOthersDocuments  Permission to edit others' docs.
	 * @param permissionViewOthersStatements Permission to view others' stmnts.
	 * @param permissionEditOthersStatements Permission to edit others' stmnts.
	 * @param coderRelations                 HashMap with coder relations.
	 */
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
			boolean permissionEditCoderRelations,
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
		this.permissionEditCoderRelations = permissionEditCoderRelations;
		this.permissionViewOthersDocuments = permissionViewOthersDocuments;
		this.permissionEditOthersDocuments = permissionEditOthersDocuments;
		this.permissionViewOthersStatements = permissionViewOthersStatements;
		this.permissionEditOthersStatements = permissionEditOthersStatements;
		this.coderRelations = coderRelations;
	}

	/**
	 * Create a coder with full information (with RGB color).
	 * 
	 * @param id                             The ID of the coder.
	 * @param name                           The name of the coder.
	 * @param red                            The red RGB color value (0-255).
	 * @param green                          The green RGB color value (0-255).
	 * @param blue                           The blue RGB color value (0-255).
	 * @param refresh                        Refresh rate in seconds.
	 * @param fontSize                       Text font size in px (default 14).
	 * @param popupWidth                     Short text field width for popups.
	 * @param colorByCoder                   Color statements in text by coder?
	 * @param popupDecoration                Show popup window decoration?
	 * @param popupAutoComplete              Auto-complete short text fields?
	 * @param permissionAddDocuments         Permission to add documents?
	 * @param permissionEditDocuments        Permission to edit documents?
	 * @param permissionDeleteDocuments      Permission to delete documents?
	 * @param permissionImportDocuments      Permission to import documents?
	 * @param permissionAddStatements        Permission to add statements?
	 * @param permissionEditStatements       Permission to edit statements?
	 * @param permissionDeleteStatements     Permission to delete statements?
	 * @param permissionEditAttributes       Permission to edit attributes?
	 * @param permissionEditRegex            Permission to edit regexes?
	 * @param permissionEditStatementTypes   Permission to edit statemnt types.
	 * @param permissionEditCoders           Permission to edit coders.
	 * @param permissionEditCoderRelations   Permission to edit coder relations.
	 * @param permissionViewOthersDocuments  Permission to view others' docs.
	 * @param permissionEditOthersDocuments  Permission to edit others' docs.
	 * @param permissionViewOthersStatements Permission to view others' stmnts.
	 * @param permissionEditOthersStatements Permission to edit others' stmnts.
	 * @param coderRelations                 HashMap with coder relations.
	 */
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
			boolean permissionEditCoderRelations,
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
		this.permissionEditCoderRelations = permissionEditCoderRelations;
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
		this.permissionEditCoderRelations = false;
		this.permissionViewOthersDocuments = false;
		this.permissionEditOthersDocuments = false;
		this.permissionViewOthersStatements = false;
		this.permissionEditOthersStatements = false;
		this.coderRelations = new HashMap<Integer, CoderRelation>();
	}
	
	/**
	 * Copy constructor. Creates a deep copy of an existing coder.
	 * 
	 * @param coder Existing coder to be cloned.
	 */
	public Coder(Coder coder) {
		this.id = coder.getId();
		this.name = coder.getName();
		this.color = coder.getColor();
		this.refresh = coder.getRefresh();
		this.fontSize = coder.getFontSize();
		this.popupWidth = coder.getPopupWidth();
		this.colorByCoder = coder.isColorByCoder();
		this.popupDecoration = coder.isPopupDecoration();
		this.popupAutoComplete = coder.isPopupAutoComplete();
		this.permissionAddDocuments = coder.isPermissionAddDocuments();
		this.permissionEditDocuments = coder.isPermissionEditDocuments();
		this.permissionDeleteDocuments = coder.isPermissionDeleteDocuments();
		this.permissionImportDocuments = coder.isPermissionImportDocuments();
		this.permissionAddStatements = coder.isPermissionAddStatements();
		this.permissionEditStatements = coder.isPermissionEditStatements();
		this.permissionDeleteStatements = coder.isPermissionDeleteStatements();
		this.permissionEditAttributes = coder.isPermissionEditAttributes();
		this.permissionEditRegex = coder.isPermissionEditRegex();
		this.permissionEditStatementTypes = coder.isPermissionEditStatementTypes();
		this.permissionEditCoders = coder.isPermissionEditCoders();
		this.permissionEditCoderRelations = coder.isPermissionEditCoderRelations();
		this.permissionViewOthersDocuments = coder.isPermissionViewOthersDocuments();
		this.permissionEditOthersDocuments = coder.isPermissionEditOthersDocuments();
		this.permissionViewOthersStatements = coder.isPermissionViewOthersStatements();
		this.permissionEditOthersStatements = coder.isPermissionEditOthersStatements();
		this.coderRelations = new HashMap<Integer, CoderRelation>();
		for (HashMap.Entry<Integer, CoderRelation> entry : coder.getCoderRelations().entrySet()) {
			this.coderRelations.put(entry.getKey(), new CoderRelation(entry.getValue()));
		}
	}
	
	/**
	 * Check if switching to this coder from another coder means that the
	 * permissions to view other coders' documents have changed. This is useful
	 * for determining if the document table should be updated after switching
	 * to a new coder.
	 * 
	 * @param c  Another coder.
	 * @return   An indicator of whether the view needs to be updated.
	 */
	public boolean differentViewDocumentPermissions(Coder c) {
		if (this.isPermissionViewOthersDocuments() != c.isPermissionViewOthersDocuments() || (this.id != c.getId()) && !this.isPermissionViewOthersDocuments()) {
			return true;
		}
		for (HashMap.Entry<Integer, CoderRelation> entry : this.getCoderRelations().entrySet()) {
			if (c.getCoderRelations().containsKey(entry.getKey()) && entry.getValue().isViewDocuments() != c.getCoderRelations().get(entry.getKey()).isViewDocuments()) {
				return true;
			} else if (!c.getCoderRelations().containsKey(entry.getKey()) && entry.getKey() != c.getId()) {
				return true;
			} else if (entry.getKey() == c.getId() && !entry.getValue().isViewDocuments()) {
				return true;
			}
		}
		for (HashMap.Entry<Integer, CoderRelation> entry : c.getCoderRelations().entrySet()) {
			if (this.getCoderRelations().containsKey(entry.getKey()) && entry.getValue().isViewDocuments() != this.getCoderRelations().get(entry.getKey()).isViewDocuments()) {
				return true;
			} else if (!this.getCoderRelations().containsKey(entry.getKey()) && entry.getKey() != this.getId()) {
				return true;
			} else if (entry.getKey() == this.getId() && !entry.getValue().isViewDocuments()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if switching to this coder from another coder means that the
	 * permissions to view other coders' statements have changed. This is useful
	 * for determining if the statement table should be updated after switching
	 * to a new coder.
	 * 
	 * @param c  Another coder.
	 * @return   An indicator of whether the view needs to be updated.
	 */
	public boolean differentViewStatementPermissions(Coder c) {
		if (this.isPermissionViewOthersStatements() != c.isPermissionViewOthersStatements() || (this.id != c.getId()) && !this.isPermissionViewOthersStatements()) {
			return true;
		}
		for (HashMap.Entry<Integer, CoderRelation> entry : this.getCoderRelations().entrySet()) {
			if (c.getCoderRelations().containsKey(entry.getKey()) && entry.getValue().isViewStatements() != c.getCoderRelations().get(entry.getKey()).isViewStatements()) {
				return true;
			} else if (!c.getCoderRelations().containsKey(entry.getKey()) && entry.getKey() != c.getId()) {
				return true;
			} else if (entry.getKey() == c.getId() && !entry.getValue().isViewStatements()) {
				return true;
			}
		}
		for (HashMap.Entry<Integer, CoderRelation> entry : c.getCoderRelations().entrySet()) {
			if (this.getCoderRelations().containsKey(entry.getKey()) && entry.getValue().isViewStatements() != this.getCoderRelations().get(entry.getKey()).isViewStatements()) {
				return true;
			} else if (!this.getCoderRelations().containsKey(entry.getKey()) && entry.getKey() != this.getId()) {
				return true;
			} else if (entry.getKey() == this.getId() && !entry.getValue().isViewStatements()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if switching to this coder from another coder means that the
	 * coder settings for displaying document text and painting statements in
	 * document text have changed. This is useful for determining if the
	 * statements should be repainted in the current document and if the
	 * document text should be redrawn after switching to a new coder.
	 * 
	 * @param c  Another coder.
	 * @return   An indicator of whether the view needs to be updated.
	 */
	public boolean differentPaintSettings(Coder c) {
		if (this.isColorByCoder() != c.isColorByCoder()) {
			return true;
		}
		if (this.getFontSize() != c.getFontSize()) {
			return true;
		}
		return false;
	}

	/**
	 * Get the ID of the coder.
	 * 
	 * @return The coder's ID.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the name of the coder.
	 * 
	 * @return The coder's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the color of the coder.
	 * 
	 * @return The coder's color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Get the refresh rate with which the coder's document and statement table
	 * are refreshed from the database (if enabled).
	 * 
	 * @return The refresh rate in seconds (0 by default).
	 */
	public int getRefresh() {
		return refresh;
	}

	/**
	 * Get the font size with which document text is displayed in the text panel
	 * from the perspective of the coder.
	 * 
	 * @return The font size in px.
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * When the coder opens a statement popup window, what is the display width
	 * of short text fields in px?
	 * 
	 * @return The popup text field width.
	 */
	public int getPopupWidth() {
		return popupWidth;
	}

	/**
	 * @param popupWidth the popupWidth to set
	 */
	public void setPopupWidth(int popupWidth) {
		this.popupWidth = popupWidth;
	}

	/**
	 * From the perspective of the coder, are the statements in the text painted
	 * in the color of the respective coder the statements belong to? If not,
	 * they are coded in the color corresponding to the statement type (the
	 * default behavior).
	 * 
	 * @return The colorByCoder boolean indicator.
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
	 * When the coder opens a statement popup window, does the window have a
	 * window decoration (i.e., a frame with an {@code X} and {@code cancel} and
	 * {@code save} buttons?
	 * 
	 * @return The popupDecoration boolean indicator.
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
	 * When the coder opens a statement popup window, are entries in short text
	 * fields auto-completed when text is entered by the user?
	 * 
	 * @return The popupAutoComplete boolean indicator.
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
	 * Is the coder allowed to add new documents?
	 * 
	 * @return The permissionAddDocuments boolean indicator.
	 */
	public boolean isPermissionAddDocuments() {
		return permissionAddDocuments;
	}

	/**
	 * Is the coder allowed to edit documents?
	 * 
	 * @return The permissionEditDocuments boolean indicator.
	 */
	public boolean isPermissionEditDocuments() {
		return permissionEditDocuments;
	}

	/**
	 * Is the coder allowed to delete existing documents?
	 * 
	 * @return The permissionDeleteDocuments boolean indicator.
	 */
	public boolean isPermissionDeleteDocuments() {
		return permissionDeleteDocuments;
	}

	/**
	 * Is the coder allowed to import documents from other files?
	 * 
	 * @return The permissionImportDocuments boolean indicator.
	 */
	public boolean isPermissionImportDocuments() {
		return permissionImportDocuments;
	}

	/**
	 * Is the coder allowed to add new statements?
	 * 
	 * @return The permissionAddStatements boolean indicator.
	 */
	public boolean isPermissionAddStatements() {
		return permissionAddStatements;
	}

	/**
	 * Is the coder allowed to edit statements?
	 * 
	 * @return The permissionEditStatements boolean indicator.
	 */
	public boolean isPermissionEditStatements() {
		return permissionEditStatements;
	}

	/**
	 * Is the coder allowed to delete statements?
	 * 
	 * @return The permissionDeleteStatements boolean indicator.
	 */
	public boolean isPermissionDeleteStatements() {
		return permissionDeleteStatements;
	}

	/**
	 * Is the coder allowed to edit attributes?
	 * 
	 * @return The permissionEditAttributes boolean indicator.
	 */
	public boolean isPermissionEditAttributes() {
		return permissionEditAttributes;
	}

	/**
	 * Is the coder allowed to edit regular expressions?
	 * 
	 * @return The permissionEditRegex boolean indicator.
	 */
	public boolean isPermissionEditRegex() {
		return permissionEditRegex;
	}

	/**
	 * Is the coder allowed to edit statement types?
	 * 
	 * @return The permissionEditStatementTypes boolean indicator.
	 */
	public boolean isPermissionEditStatementTypes() {
		return permissionEditStatementTypes;
	}

	/**
	 * Is the coder allowed to edit the list and user rights of coders?
	 * 
	 * @return The permissionEditCoders boolean indicator.
	 */
	public boolean isPermissionEditCoders() {
		return permissionEditCoders;
	}

	/**
	 * Is the coder allowed to edit its coder relations?
	 * 
	 * @return The permissionEditCoderRelations boolean indicator.
	 */
	public boolean isPermissionEditCoderRelations() {
		return permissionEditCoderRelations;
	}

	/**
	 * Is the coder allowed to view other coders' documents (in general)?
	 * 
	 * @return The permissionViewOthersDocuments boolean indicator.
	 */
	public boolean isPermissionViewOthersDocuments() {
		return permissionViewOthersDocuments;
	}

	/**
	 * Is the coder allowed to edit other coders' documents (in general)?
	 * 
	 * @return The permissionEditOthersDocuments boolean indicator.
	 */
	public boolean isPermissionEditOthersDocuments() {
		return permissionEditOthersDocuments;
	}

	/**
	 * Is the coder allowed to view other coders' statements (in general)?
	 * 
	 * @return The permissionViewOthersStatements boolean indicator.
	 */
	public boolean isPermissionViewOthersStatements() {
		return permissionViewOthersStatements;
	}

	/**
	 * Is the coder allowed to edit other coders' statements (in general)?
	 * 
	 * @return The permissionEditOthersStatements boolean indicator.
	 */
	public boolean isPermissionEditOthersStatements() {
		return permissionEditOthersStatements;
	}

	/**
	 * Is the coder allowed to view the statements by a specific other coder?
	 * 
	 * @param otherCoderId  ID of the other coder.
	 * @return              Boolean indicating if the coder may view statements.
	 */
	public boolean isPermissionViewOthersStatements(int otherCoderId) {
		if (coderRelations.containsKey(otherCoderId)) {
			if (coderRelations.get(otherCoderId).isViewStatements() == true) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Is the coder allowed to view the documents by a specific other coder?
	 * 
	 * @param otherCoderId  ID of the other coder.
	 * @return              Boolean indicating if the coder may view documents.
	 */
	public boolean isPermissionViewOthersDocuments(int otherCoderId) {
		if (coderRelations.containsKey(otherCoderId)) {
			if (coderRelations.get(otherCoderId).isViewDocuments() == true) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Is the coder allowed to edit the statements by a specific other coder?
	 * 
	 * @param otherCoderId  ID of the other coder.
	 * @return              Boolean indicating if the coder may edit statements.
	 */
	public boolean isPermissionEditOthersStatements(int otherCoderId) {
		if (coderRelations.containsKey(otherCoderId)) {
			if (coderRelations.get(otherCoderId).isEditStatements() == true) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Is the coder allowed to edit the documents by a specific other coder?
	 * 
	 * @param otherCoderId  ID of the other coder.
	 * @return              Boolean indicating if the coder may edit documents.
	 */
	public boolean isPermissionEditOthersDocuments(int otherCoderId) {
		if (coderRelations.containsKey(otherCoderId)) {
			if (coderRelations.get(otherCoderId).isEditDocuments() == true) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @param permissionAddDocuments the permissionAddDocuments to set
	 */
	public void setPermissionAddDocuments(boolean permissionAddDocuments) {
		this.permissionAddDocuments = permissionAddDocuments;
	}

	/**
	 * @param permissionEditDocuments the permissionEditDocuments to set
	 */
	public void setPermissionEditDocuments(boolean permissionEditDocuments) {
		this.permissionEditDocuments = permissionEditDocuments;
	}

	/**
	 * @param permissionDeleteDocuments the permissionDeleteDocuments to set
	 */
	public void setPermissionDeleteDocuments(boolean permissionDeleteDocuments) {
		this.permissionDeleteDocuments = permissionDeleteDocuments;
	}

	/**
	 * @param permissionImportDocuments the permissionImportDocuments to set
	 */
	public void setPermissionImportDocuments(boolean permissionImportDocuments) {
		this.permissionImportDocuments = permissionImportDocuments;
	}

	/**
	 * @param permissionAddStatements the permissionAddStatements to set
	 */
	public void setPermissionAddStatements(boolean permissionAddStatements) {
		this.permissionAddStatements = permissionAddStatements;
	}

	/**
	 * @param permissionEditStatements the permissionEditStatements to set
	 */
	public void setPermissionEditStatements(boolean permissionEditStatements) {
		this.permissionEditStatements = permissionEditStatements;
	}

	/**
	 * @param permissionDeleteStatements the permissionDeleteStatements to set
	 */
	public void setPermissionDeleteStatements(boolean permissionDeleteStatements) {
		this.permissionDeleteStatements = permissionDeleteStatements;
	}

	/**
	 * @param permissionEditAttributes the permissionEditAttributes to set
	 */
	public void setPermissionEditAttributes(boolean permissionEditAttributes) {
		this.permissionEditAttributes = permissionEditAttributes;
	}

	/**
	 * @param permissionEditRegex the permissionEditRegex to set
	 */
	public void setPermissionEditRegex(boolean permissionEditRegex) {
		this.permissionEditRegex = permissionEditRegex;
	}

	/**
	 * @param permissionEditStatementTypes the permissionEditStatementTypes to set
	 */
	public void setPermissionEditStatementTypes(boolean permissionEditStatementTypes) {
		this.permissionEditStatementTypes = permissionEditStatementTypes;
	}

	/**
	 * @param permissionEditCoders the permissionEditCoders to set
	 */
	public void setPermissionEditCoders(boolean permissionEditCoders) {
		this.permissionEditCoders = permissionEditCoders;
	}

	/**
	 * @param permissionEditCoderRelations the permissionEditCoderRelations to
	 *   set
	 */
	public void setPermissionEditCoderRelations(boolean permissionEditCoderRelations) {
		this.permissionEditCoderRelations = permissionEditCoderRelations;
	}

	/**
	 * @param permissionViewOthersDocuments the permissionViewOthersDocuments to set
	 */
	public void setPermissionViewOthersDocuments(boolean permissionViewOthersDocuments) {
		this.permissionViewOthersDocuments = permissionViewOthersDocuments;
	}

	/**
	 * @param permissionEditOthersDocuments the permissionEditOthersDocuments to set
	 */
	public void setPermissionEditOthersDocuments(boolean permissionEditOthersDocuments) {
		this.permissionEditOthersDocuments = permissionEditOthersDocuments;
	}

	/**
	 * @param permissionViewOthersStatements the permissionViewOthersStatements to set
	 */
	public void setPermissionViewOthersStatements(boolean permissionViewOthersStatements) {
		this.permissionViewOthersStatements = permissionViewOthersStatements;
	}

	/**
	 * @param permissionEditOthersStatements the permissionEditOthersStatements to set
	 */
	public void setPermissionEditOthersStatements(boolean permissionEditOthersStatements) {
		this.permissionEditOthersStatements = permissionEditOthersStatements;
	}

	/**
	 * Get the coder relations as a hash map.
	 * 
	 * @return Hash map of the four permissions with respect to other coders.
	 */
	public HashMap<Integer, CoderRelation> getCoderRelations() {
		return coderRelations;
	}
	
	/**
	 * Is the coder identical to another coder?
	 * 
	 * @param coder  The other coder.
	 * @return       Boolean indicator.
	 */
	public boolean equals(Coder coder) {
		boolean sourceIdenticalInTarget = true;
		boolean targetIdenticalInSource = true;
		for (HashMap.Entry<Integer, CoderRelation> entry : coder.getCoderRelations().entrySet()) {
			if (!this.coderRelations.containsKey(entry.getKey())) {
				targetIdenticalInSource = false;
				break;
			} else {
				CoderRelation thisCR = this.coderRelations.get(entry.getKey());
				CoderRelation otherCR = entry.getValue();
				if (!thisCR.equals(otherCR)) {
					targetIdenticalInSource = false;
					break;
				}
			}
		}
		if (targetIdenticalInSource) {
			for (HashMap.Entry<Integer, CoderRelation> entry : this.coderRelations.entrySet()) {
				if (!coder.getCoderRelations().containsKey(entry.getKey())) {
					sourceIdenticalInTarget = false;
					break;
				} else if (!coder.getCoderRelations().get(entry.getKey()).equals(entry.getValue())) {
					sourceIdenticalInTarget = false;
					break;
				}
			}
		}
		
		if (this.id == coder.getId() &&
				this.name.equals(coder.getName()) &&
				this.color.equals(coder.getColor()) &&
				this.refresh == coder.getRefresh() &&
				this.fontSize == coder.getFontSize() &&
				this.popupWidth == coder.getPopupWidth() &&
				this.colorByCoder == coder.isColorByCoder() &&
				this.popupDecoration == coder.isPopupDecoration() &&
				this.popupAutoComplete == coder.isPopupAutoComplete() &&
				this.permissionAddDocuments == coder.isPermissionAddDocuments() &&
				this.permissionEditDocuments == coder.isPermissionEditDocuments() &&
				this.permissionDeleteDocuments == coder.isPermissionDeleteDocuments() &&
				this.permissionImportDocuments == coder.isPermissionImportDocuments() &&
				this.permissionAddStatements == coder.isPermissionAddStatements() &&
				this.permissionEditStatements == coder.isPermissionEditStatements() &&
				this.permissionDeleteStatements == coder.isPermissionDeleteStatements() &&
				this.permissionEditAttributes == coder.isPermissionEditAttributes() &&
				this.permissionEditRegex == coder.isPermissionEditRegex() &&
				this.permissionEditStatementTypes == coder.isPermissionEditStatementTypes() &&
				this.permissionEditCoders == coder.isPermissionEditCoders() &&
				this.permissionEditCoderRelations == coder.isPermissionEditCoderRelations() &&
				this.permissionViewOthersDocuments == coder.isPermissionViewOthersDocuments() &&
				this.permissionEditOthersDocuments == coder.isPermissionEditOthersDocuments() &&
				this.permissionViewOthersStatements == coder.isPermissionViewOthersStatements() &&
				this.permissionEditOthersStatements == coder.isPermissionEditOthersStatements() &&
				sourceIdenticalInTarget &&
				targetIdenticalInSource) {
			return true;
		} else {
			return false;
		}
	}
}