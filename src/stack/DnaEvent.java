package stack;

/**
 * An DnaEvent can be any collection of data that is supposed to go into the
 * database or be updated in the database.
 */
abstract class DnaEvent {
	String eventType;
	int id;

	/**
	 * Constructor for abstract DnaEvent class.
	 */
	public DnaEvent(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * Return the type of event.
	 */
	String getEventType() {
		return this.eventType;
	}

	/**
	 * Set the type of event.
	 * 
	 * @param  DnaEvent type as a string.
	 */
	void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * An abstract method for inserting the event data into the database or
	 * updating the database with the event data.
	 */
	abstract void doSql();

	/**
	 * An abstract method for removing the event data from the database.
	 */
	abstract void undoSql();
}