package model;

/**
 * Represents a statement type.
 */
public class StatementType {
	/**
	 * The statement type ID.
	 */
	private final int id;
	
	/**
	 * The label or name of the statement type.
	 */
	private String label;
	
	/**
	 * The color associated with the statement type.
	 */
	private model.Color color;

	/**
	 * Create a statement type.
	 * 
	 * @param id The statement type ID.
	 * @param label The label or name of the statement type.
	 * @param color The color of the statement type.
	 */
	public StatementType(int id, String label, Color color) {
		this.id = id;
		this.label = label;
		this.color = color;
	}

	/**
	 * Copy constructor for statement type objects to create a deep clone.
	 * 
	 * @param st A statement type.
	 */
	public StatementType(StatementType st) {
		this.id = st.getId();
		this.label = st.getLabel();
		this.color = st.getColor();
	}
	
	/**
	 * Get the statement type ID.
	 * 
	 * @return The statement type ID.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the statement type label/name.
	 * 
	 * @return The statement type label/name.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get the statement type color.
	 * 
	 * @return The color of the statement type.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Is this statement type equal to another statement type?
	 * 
	 * @param st  The other statement type for comparison.
	 * @return    Boolean indicator: Is the other statement type identical?
	 */
	public boolean equals(StatementType st) {
		return this.getId() == st.getId() && this.getLabel().equals(st.getLabel()) && this.getColor().equals(st.getColor()) && this.getClass().equals(st.getClass());
	}
}