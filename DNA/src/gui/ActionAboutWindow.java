package gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;

class ActionAboutWindow extends AbstractAction {
	private static final long serialVersionUID = -9078666078201409563L;
	
	public ActionAboutWindow(String text, ImageIcon icon, String desc, Integer mnemonic) {
		super(text, icon);
		putValue(SHORT_DESCRIPTION, desc);
		putValue(MNEMONIC_KEY, mnemonic);
	}
	
	public void actionPerformed(ActionEvent e) {
		new AboutWindow(Dna.version, Dna.date);
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"[GUI] Action executed: opened About DNA window.",
				"Opened an About DNA window from the GUI.");
		Dna.logger.log(l);
	}
}