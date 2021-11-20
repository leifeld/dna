package gui;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import dna.Dna;
import model.Coder;
import model.CoderRelation;

/**
 * A coder relations editor for the currently active coder.
 */
class CoderRelationsEditor extends JDialog {
	private static final long serialVersionUID = 2210706535836329720L;
	private JButton reloadButton, okButton;
	private Coder activeCoderCopy;
	private CoderRelationsPanel coderRelationsPanel;
	
	/**
	 * Create and show a dialog to edit the currently active coder's
	 * coder relations.
	 */
	CoderRelationsEditor() {
		ImageIcon coderRelationsIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-user-check.png"));
		this.setIconImage(coderRelationsIcon.getImage());
		this.setModal(true);
		this.setTitle("Coder relations editor");
		this.setLayout(new BorderLayout());

		coderRelationsPanel = new CoderRelationsPanel();
		JTable coderRelationTable = coderRelationsPanel.getTable();
		
		populateTable();
		
		// monitor mouse clicks in the coder relation table to edit boolean permissions
		coderRelationTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = coderRelationTable.rowAtPoint(evt.getPoint());
				int col = coderRelationTable.columnAtPoint(evt.getPoint());
				if (activeCoderCopy.getId() > 1 && row >= 0 && col > 0 && col < 5) {
					boolean newValue = !(boolean) coderRelationTable.getValueAt(row, col);
					int coderId = ((Coder) coderRelationTable.getValueAt(row, 0)).getId();
					if (col == 1) {
						activeCoderCopy.getCoderRelations().get(coderId).setViewDocuments(newValue);
					} else if (col == 2) {
						activeCoderCopy.getCoderRelations().get(coderId).setEditDocuments(newValue);
					} else if (col == 3) {
						activeCoderCopy.getCoderRelations().get(coderId).setViewStatements(newValue);
					} else if (col == 4) {
						activeCoderCopy.getCoderRelations().get(coderId).setEditStatements(newValue);
					}
					coderRelationTable.setValueAt(newValue, row, col);
					if (activeCoderCopy.equals(Dna.sql.getActiveCoder())) {
						okButton.setEnabled(false);
						reloadButton.setEnabled(false);
					} else {
						okButton.setEnabled(true);
						reloadButton.setEnabled(true);
					}
				}
			}
		});
		this.add(coderRelationsPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		ImageIcon cancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton cancelButton = new JButton("Cancel", cancelIcon);
		cancelButton.setToolTipText("Close the coder manager without saving any changes.");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CoderRelationsEditor.this.dispose();
			}
		});
		buttonPanel.add(cancelButton);

		ImageIcon reloadIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-rotate-clockwise.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		reloadButton = new JButton("Reset", reloadIcon);
		reloadButton.setToolTipText("Reset all the changes made for the active coder and reload the coder relations.");
		reloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				populateTable();
			}
		});
		reloadButton.setEnabled(false);
		buttonPanel.add(reloadButton);

		ImageIcon okIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-user-check.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		okButton = new JButton("Save and close", okIcon);
		okButton.setToolTipText("Save the changes for the active coder to the database and make them effective.");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!activeCoderCopy.equals(Dna.sql.getActiveCoder())) {
					int dialog = JOptionPane.showConfirmDialog(CoderRelationsEditor.this, "Save changes for Coder " + activeCoderCopy.getId() + " to the database?", "Confirmation", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						boolean success = Dna.sql.updateCoder(activeCoderCopy, null);
						if (success) {
							JOptionPane.showMessageDialog(CoderRelationsEditor.this, "Changes for Coder " + activeCoderCopy.getId() + " were successfully saved.");
						} else {
							JOptionPane.showMessageDialog(CoderRelationsEditor.this, "Changes for Coder " + activeCoderCopy.getId() + " could not be saved. Check the message log for details.");
						}
						CoderRelationsEditor.this.dispose();
					}
				}
			}
		});
		okButton.setEnabled(false);
		buttonPanel.add(okButton);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Fill the table with coder relations from the active coder.
	 */
	void populateTable() {
		activeCoderCopy = new Coder(Dna.sql.getActiveCoder());
		coderRelationsPanel.getModel().clear();
		for (HashMap.Entry<Integer, CoderRelation> entry : activeCoderCopy.getCoderRelations().entrySet()) {
			coderRelationsPanel.getModel().addRow(entry.getValue());
		}
		if (reloadButton != null) {
			reloadButton.setEnabled(false);
		}
		if (okButton != null) {
			okButton.setEnabled(false);
		}
	}
}