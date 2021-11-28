package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import sql.Sql.SqlListener;

/**
 * Represents a coder selection panel for selecting the active coder.
 */
class CoderSelectionPanel extends JPanel implements SqlListener {
	private static final long serialVersionUID = 7852541276993938860L;
	private CoderBadgePanel coderBadgePanel;
	private JButton changeCoderButton;
	private GridBagConstraints gbc;

	/**
	 * Create a new coder selection panel.
	 */
	public CoderSelectionPanel() {
		setLayout(new GridBagLayout());
		ImageIcon changeCoderIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-users.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		changeCoderButton = new JButton("Change coder", changeCoderIcon);
		changeCoderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean authenticated = false;
				int coderIdToSelect = -1;
				if (Dna.sql.getActiveCoder() != null) {
					coderIdToSelect = Dna.sql.getActiveCoder().getId();
				}
				while (!authenticated) {
					CoderPasswordCheckDialog dialog = new CoderPasswordCheckDialog(Dna.sql, true, coderIdToSelect);
					Coder coder = dialog.getCoder();
					String clearPassword = dialog.getPassword();
					if (coder != null && clearPassword != null) {
						coderIdToSelect = coder.getId();
						if (Dna.sql.authenticate(coder.getId(), clearPassword)) {
							authenticated = true;
							Dna.sql.changeActiveCoder(coder.getId());
						} else {
							LogEvent l = new LogEvent(Logger.WARNING,
    								"Authentication failed. Check your password.",
    								"Tried to select coder, but a wrong password was entered for Coder " + coder.getId() + ".");
    						Dna.logger.log(l);
		    				JOptionPane.showMessageDialog(null,
		    						"Authentication failed. Check your password.",
		    					    "Check failed",
		    					    JOptionPane.ERROR_MESSAGE);
						}
					} else {
						authenticated = true; // user must have pressed cancel
					}
				}
			}
		});
		changeCoderButton.setEnabled(false);
		JToolBar tb = new JToolBar();
		tb.add(changeCoderButton);
		tb.setFloatable(false);
		tb.setRollover(true);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		this.add(tb, gbc);
		
		coderBadgePanel = new CoderBadgePanel();
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		this.add(coderBadgePanel, gbc);
	}
	
	@Override
	public void adjustToChangedConnection() {
		// nothing to do
	}

	@Override
	public void adjustToChangedCoder() {
		this.gbc.gridx = 1;
		this.gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		this.gbc.weightx = 0.0;
		this.remove(coderBadgePanel);
		Coder coder = Dna.sql.getActiveCoder();
		if (coder != null) {
			this.coderBadgePanel = new CoderBadgePanel(coder, 18, 30);
		} else {
			this.coderBadgePanel = new CoderBadgePanel();
		}
		this.add(this.coderBadgePanel);
		this.revalidate();
		this.repaint();
		changeCoderButton.setEnabled(true);
		if (Dna.sql.getConnectionProfile() == null || Dna.sql.getActiveCoder() == null) {
			changeCoderButton.setEnabled(false);
		} else {
			changeCoderButton.setEnabled(true);
		}
	}
}