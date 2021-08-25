package logger;

import java.io.Serializable;

@SuppressWarnings("unused")
class OutputLog implements Serializable {
	private static final long serialVersionUID = -8540969879291371358L;
	private String version, date;
	private OutputLogEvent[] events;
	
	OutputLog(String version, String date, OutputLogEvent[] events) {
		this.version = version;
		this.date = date;
		this.events = events;
	}
}