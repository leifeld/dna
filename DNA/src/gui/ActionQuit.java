package gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;

class ActionQuit extends AbstractAction {
	private static final long serialVersionUID = 3334696382161923841L;

	public ActionQuit(String text, ImageIcon icon, String desc, Integer mnemonic) {
		super(text, icon);
		putValue(SHORT_DESCRIPTION, desc);
		putValue(MNEMONIC_KEY, mnemonic);
	}
	
	public void actionPerformed(ActionEvent e) {
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"[GUI] Action executed: quit DNA.",
				"Quit DNA from the GUI.");
		Dna.logger.log(l);
		System.exit(0);
	}
}