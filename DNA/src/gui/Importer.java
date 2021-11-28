package gui;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

class Importer extends JDialog {
	private static final long serialVersionUID = -5295303422543731461L;

	public Importer() {
		ImageIcon importerIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-database-import.png"));
		this.setIconImage(importerIcon.getImage());
		this.setModal(true);
		this.setTitle("Import from another DNA database");
		this.setLayout(new BorderLayout());

		this.add(new JLabel("to do"));
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}