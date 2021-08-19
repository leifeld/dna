package dna;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * A throwable event that captures details like the priority/severity of the
 * event, a summary and details of the event, the stack trace, and the coder ID.
 * It generates the stack trace automatically. Upon instantiation, a log event
 * tries to set the current coder ID. If unsuccessful, it sets -1.
 */
public class LogEvent extends Throwable {
	private static final long serialVersionUID = 776936228209151721L;
	LocalDateTime time;
	String summary, details, stackTraceString;
	int priority; // 1 = message; 2 = warning; 3 = error
	int coder;
	
	/**
	 * Create a new log event.
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
		if (Dna.sql == null) {
			this.coder = -1;
		} else {
			this.coder = Dna.sql.getConnectionProfile().getCoderId();
		}
		this.time = LocalDateTime.now();
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		printStackTrace(pw);
		this.stackTraceString = sw.toString();
		pw.close();
	}
	
	public String getStackTraceString() {
		return stackTraceString;
	}

	public void setStackTraceString(String stackTraceString) {
		this.stackTraceString = stackTraceString;
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