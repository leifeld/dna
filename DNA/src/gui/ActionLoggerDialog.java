package gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import logger.LoggerDialog;

class ActionLoggerDialog extends AbstractAction {
	private static final long serialVersionUID = -629086240908166990L;

	public ActionLoggerDialog(String text, ImageIcon icon, String desc, Integer mnemonic) {
		super(text, icon);
		putValue(SHORT_DESCRIPTION, desc);
		putValue(MNEMONIC_KEY, mnemonic);
	}
	
	public void actionPerformed(ActionEvent e) {
		new LoggerDialog();
	}
}