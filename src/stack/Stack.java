/**
 * The stack package implements an undo/redo stack and the classes representing
 * the elements to be put on the stack.
 */
package stack;

import java.util.ArrayList;

/**
 * This class represents an undo/redo stack of events, such as AddDocumentEvent
 * and other implementations of events. It can receive events and add them to
 * the stack, remove them from the stack, and trigger SQL execution to make the
 * corresponding changes in the database.
 *
 */
public class Stack {
	ArrayList<DnaEvent> stack;
	int actionIndex;

	/**
	 * Constructor.
	 */
	public Stack() {
		this.stack = new ArrayList<DnaEvent>();
		this.actionIndex = 0; // the position where a new event can be written or overwritten
	}
	
	/**
	 * Return the latest element on the stack (not taking into account undone
	 * events).
	 * 
	 * @return Last done DNA event.
	 */
	public DnaEvent getLatestEvent() {
		if (this.actionIndex > 0) {
			return this.stack.get(this.actionIndex - 1);
		}
		return null;
	}
	
	/**
	 * Add a DNA event to the undo/redo stack. If there are other events in the
	 * way/ahead of the index, they will be removed first. Before adding the
	 * event to the stack, it is asked to push its data to the SQL database.
	 * 
	 * @param c   DNA event to be added to the stack.
	 * @return    Index of the stack after adding the event.
	 */
	public int add(DnaEvent e) {
		if (this.actionIndex > this.stack.size()) {
			System.err.println("Stack index larger than stack size.");
			this.actionIndex = this.stack.size();
		} else if (this.actionIndex < this.stack.size()) {
			for (int i = this.stack.size() - 1; i > this.actionIndex; i--) {
				this.stack.remove(i);
			}
		}
		e.doSql();
		this.stack.add(e);
		this.actionIndex++;
		return this.actionIndex;
	}
	
	/**
	 * Undo function. This moves the index of the stack back down. It retains
	 * the undone DNA events rather than deleting them to enable the redo
	 * functionality if needed. DNA events are only deleted if new events are
	 * added. Undoing events also undoes them in the SQL database.
	 * 
	 * @return New index of the stack after updating.
	 */
	public int undo() {
		if (this.actionIndex > 0) {
			this.stack.get(this.actionIndex - 1).undoSql();
			this.actionIndex--;
			// TODO: refresh table model
		}
		return this.actionIndex;
	}
	
	/**
	 * Redo function. Move the index of the stack back up and ask the redone
	 * event to push its data to the SQL database again.
	 * 
	 * @return New index of the stack after updating.
	 */
	public int redo() {
		if (this.actionIndex < this.stack.size()) {
			this.stack.get(this.actionIndex).doSql();
			this.actionIndex++;
			// TODO: refresh table model
		}
		return this.actionIndex;
	}
}