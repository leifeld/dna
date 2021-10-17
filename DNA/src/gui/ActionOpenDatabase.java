package gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import sql.ConnectionProfile;

/**
 * An action to display a dialog to open a database.
 */
class ActionOpenDatabase extends AbstractAction {
	private static final long serialVersionUID = 5076889002458881750L;
	
	public ActionOpenDatabase(String text, ImageIcon icon, String desc, Integer mnemonic) {
		super(text, icon);
		putValue(SHORT_DESCRIPTION, desc);
		putValue(MNEMONIC_KEY, mnemonic);
	}
	
	public void actionPerformed(ActionEvent e) {
		NewDatabaseDialog n = new NewDatabaseDialog(true);
		ConnectionProfile cp = n.getConnectionProfile();
		Dna.sql.setConnectionProfile(cp);
		if (cp == null) {
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: could not open database.",
					"Started opening a database connection from the GUI, but the connection was not established.");
			Dna.logger.log(l);
		} else {
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: opened database.",
					"Opened a database connection from the GUI.");
			Dna.logger.log(l);
		}
	}
}