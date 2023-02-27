package logger;

import java.io.Serializable;

/**
 * Represents an output log, with DNA version and date and a list of log events.
 * For use with XML and JSON export.
 */
@SuppressWarnings("unused")
class OutputLog implements Serializable {
	private static final long serialVersionUID = -8540969879291371358L;
	private String version, date, os;
	private OutputLogEvent[] events;
	
	/**
	 * Create a new OutputLog object.
	 * 
	 * @param version  DNA version as a String.
	 * @param date     Date the DNA version was released, as a String.
	 * @param os       Operating system.
	 * @param events   An array of {@link logger.OutputLogEvent OutputLogEvent}
	 *   objects.
	 */
	OutputLog(String version, String date, String os, OutputLogEvent[] events) {
		this.version = version;
		this.date = date;
		this.os = os;
		this.events = events;
	}
}