package stack;

import java.util.ArrayList;

/**
 * Container for multiple DnaEvents. Each container is one undo or redo action.
 * A container can contain one DnaEvent or multiple DnaEvents in the order in
 * which they are added to the database, from general to specific (for example,
 * document first, then statement contained in document).
 */
public class DnaEventContainer {
	ArrayList<DnaEvent> dnaEvents;

	/**
	 * Constructor.
	 */
	public DnaEventContainer() {
		this.dnaEvents = new ArrayList<DnaEvent>();
	}
	
	void addEvent(DnaEvent e) {
		this.dnaEvents.add(e);
	}
	
	int size() {
		return this.dnaEvents.size();
	}
}