package gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import sql.ConnectionProfile;
import sql.Sql;

class ActionCreateDatabase extends AbstractAction {
	private static final long serialVersionUID = -9019267411134217476L;

	public ActionCreateDatabase(String text, ImageIcon icon, String desc, Integer mnemonic) {
		super(text, icon);
		putValue(SHORT_DESCRIPTION, desc);
		putValue(MNEMONIC_KEY, mnemonic);
	}
	
	public void actionPerformed(ActionEvent e) {
		NewDatabaseDialog n = new NewDatabaseDialog(false);
		ConnectionProfile cp = n.getConnectionProfile();
		if (cp != null) {
			Dna.setSql(new Sql(cp));
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Action executed: created new database.",
					"Created a new database from the GUI.");
			Dna.logger.log(l);
		}
	}
}