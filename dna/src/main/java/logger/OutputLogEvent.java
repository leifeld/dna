package logger;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

/**
 * A simplified version of {@link logger.LogEvent} objects for output to XML or
 * JSON data files. 
 */
class OutputLogEvent implements Serializable {
	private static final long serialVersionUID = -2275301137294798275L;
	String date;
	int priority, coder;
	String summary, details, stackLog, stackException;
	
	/**
	 * Take a {@link logger.LogEvent} object and extract its details, then
	 * create an {@link logger.OutputLogEvent} object using these details.
	 * 
	 * @param l  A {@link logger.LogEvent} object.
	 */
	OutputLogEvent(LogEvent l) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS");
		this.date = l.getTime().format(formatter);
		this.priority = l.getPriority();
		this.coder = l.getCoder();
		this.summary = l.getSummary();
		this.details = l.getDetails();
		this.stackLog = l.getLogStackTraceString();
		this.stackException = l.getExceptionStackTraceString();
	}
}