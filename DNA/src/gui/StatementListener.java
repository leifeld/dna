package gui;

/**
 * Listener interface for statement panel listeners. It makes all classes that
 * implement this interface also implement the methods included here. The
 * statement panel class can then notify the listeners by executing their
 * methods.
 */
public interface StatementListener {
	void statementRefreshStart();
	void statementRefreshEnd();
}