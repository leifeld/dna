package logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import dna.Dna;

/**
 * A throwable event that captures details like the priority/severity of the
 * event, a summary and details of the event, the stack trace, and the coder ID.
 * It generates the stack trace automatically. Upon instantiation, a log event
 * tries to set the current coder ID. If unsuccessful, it sets -1.
 */
public class LogEvent extends Throwable {
	private static final long serialVersionUID = 776936228209151721L;
	LocalDateTime time;
	String summary, details, exceptionStackTraceString, logStackTraceString;
	int priority; // 1 = message; 2 = warning; 3 = error
	int coder;
	
	/**
	 * Create a new log event with empty exception stack trace string.
	 * 
	 * @param priority Priority of the event, which can be 1
	 *   ({@link Logger.MESSAGE}, 2 ({@link Logger.WARNING}), or 3
	 *   ({@link Logger.ERROR}).
	 * @param summary The title or short version of the event.
	 * @param details A more detailed description of the event.
	 */
	public LogEvent(int priority, String summary, String details) {
		this.priority = priority;
		this.summary = summary;
		this.details = details;
		this.exceptionStackTraceString = "";
		if (Dna.sql.getConnectionProfile() == null) {
			this.coder = -1;
		} else {
			this.coder = Dna.sql.getConnectionProfile().getCoderId();
		}
		this.time = LocalDateTime.now();
		this.logStackTraceString = this.stackTraceToString(this);
	}

	/**
	 * Create a new log event with an exception whose stack trace is saved.
	 * 
	 * @param priority Priority of the event, which can be 1
	 *   ({@link Logger.MESSAGE}, 2 ({@link Logger.WARNING}), or 3
	 *   ({@link Logger.ERROR}).
	 * @param summary    The title or short version of the event.
	 * @param details    A more detailed description of the event.
	 * @param exception  The Throwable object from which the exception should be
	 *   parsed.
	 */
	public LogEvent(int priority, String summary, String details, Throwable exception) {
		this.priority = priority;
		this.summary = summary;
		this.details = details;
		this.exceptionStackTraceString = this.stackTraceToString(exception);
		if (Dna.sql == null) {
			this.coder = -1;
		} else {
			this.coder = Dna.sql.getConnectionProfile().getCoderId();
		}
		this.time = LocalDateTime.now();
		this.logStackTraceString = this.stackTraceToString(this);
	}
	
	/**
	 * Take a throwable object (e.g., an Exception of any kind) and convert its
	 * stack trace into a printable String object and return it.
	 * 
	 * @param t  A throwable object, for example an exception.
	 * @return   A String representing the stack trace of the throwable.
	 */
	private String stackTraceToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String s = sw.toString();
		pw.close();
		return s;
	}

	public String getExceptionStackTraceString() {
		return exceptionStackTraceString;
	}

	public void setExceptionStackTraceString(String exceptionStackTraceString) {
		this.exceptionStackTraceString = exceptionStackTraceString;
	}

	public String getLogStackTraceString() {
		return logStackTraceString;
	}

	public void setLogStackTraceString(String logStackTraceString) {
		this.logStackTraceString = logStackTraceString;
	}

	public LocalDateTime getTime() {
		return time;
	}
	
	public void setTime(LocalDateTime time) {
		this.time = time;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getDetails() {
		return details;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public int getCoder() {
		return coder;
	}
	
	/**
	 * Set the coder ID of the coder who triggered the log entry.
	 * 
	 * @param coder An integer coder ID, which represents the coder's primary
	 *   key in the database.
	 */
	public void setCoder(int coder) {
		this.coder = coder;
	}
}