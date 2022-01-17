package logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * The Logger class contains an error and warning log. It extends
 * {@link AbstractTableModel} to display the log more easily in a {@link JTable}.
 */
public class Logger extends AbstractTableModel {
	private static final long serialVersionUID = -4311166671521091151L;
	public static final int UPDATE = 0;
	public static final int MESSAGE = 1;
	public static final int WARNING = 2;
	public static final int ERROR = 3;
	ArrayList<LogEvent> rows;
	private List<LogListener> listeners = new ArrayList<LogListener>();
	
	/**
	 * Create a new logger.
	 */
	public Logger() {
		this.rows = new ArrayList<LogEvent>();
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rows.size() == 0 || rowIndex > rows.size() - 1) {
			return null;
		}
		switch(columnIndex) {
		case 0: return rows.get(rowIndex).getTime();
		case 1: return rows.get(rowIndex).getSummary();
		case 2: return rows.get(rowIndex).getDetails();
		case 3: return rows.get(rowIndex).getLogStackTraceString();
		case 4: return rows.get(rowIndex).getExceptionStackTraceString();
		case 5: return rows.get(rowIndex).getCoder();
		default: return null;
		}
	}

	/**
	 * Which type of object (i.e., class) shall be shown in the columns?
	 * 
	 * @param column The column index of the table for which the class type should be returned, starting at 0.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return LocalDateTime.class; // Date and time
			case 1: return String.class;        // Summary
			case 2: return String.class;        // Details
			case 3: return String.class;        // Exception stack trace
			case 4: return String.class;        // Log stack trace
			case 5: return Integer.class;       // Coder ID
			default: return null;
		}
	}

	/**
	 * Return the name of a column.
	 * 
	 * @param column The column index of the table for which the name should be returned, starting at 0.
	 */
	public String getColumnName(int column) {
		switch(column) {
			case 0: return "Date and time";
			case 1: return "Summary";
			case 2: return "Details";
			case 3: return "Log stack trace";
			case 4: return "Exception stack trace";
			case 5: return "Coder";
			default: return null;
		}
	}
	
	/**
	 * Is the table cell editable? Returns false to make sure that the whole
	 * table cannot be edited.
	 * 
	 * @param rowIndex The row of the table for which we want to know if the cell is editable.
	 * @param columnIndex The column of the table for which we want to know if the cell is editable.
	 * @return Is the cell editable? Yields false.
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	/**
	 * Return a row of the table based on the internal array list in the Logger
	 * object.
	 * 
	 * @param row Index of the {@link LogEvent} object in the array list.
	 * @return The {@link LogEvent} object.
	 */
	public LogEvent getRow(int row) {
		return rows.get(row);
	}

	/**
	 * Add a {@link LogEvent} object to the array list and notify listeners.
	 * 
	 * @param logEntry The {@link LogEvent} object to be added.
	 */
	public void log(LogEvent l) {
		this.rows.add(l);
		fireTableRowsInserted(this.rows.size() - 1, this.rows.size() - 1);
		for (LogListener listener : listeners) {
			listener.processLogEvents();
		}
	}
	
	/**
	 * Add a log listener. This can be an object that implements the
	 * {@link LogListener} interface.
	 * 
	 * @param listener An object implementing the {@link LogListener} interface.
	 */
	public void addListener(LogListener listener) {
        listeners.add(listener);
    }
	
	/**
	 * Clear all log events and notify listeners.
	 */
	public void clear() {
		this.rows.clear();
		fireTableDataChanged();
		for (LogListener l : listeners) {
			l.processLogEvents();
		}
	}

	/**
	 * An interface for listeners for log events. For example, the main window
	 * should react to changes in the Logger by adjusting the information in the
	 * status bar. It thus needs to be registered as a listener to the Logger.
	 */
	public interface LogListener {
		void processLogEvents();
	}
}