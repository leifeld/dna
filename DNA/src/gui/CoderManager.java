package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dna.Dna;
import model.Coder;

public class CoderManager extends JDialog {
	private static final long serialVersionUID = -2714067204780133808L;
	private JList<Coder> coderList;
	
	private JLabel nameLabel, colorLabel, pw1Label, pw2Label;
	private JTextField nameField;
	private ColorButton colorButton;
	private JPasswordField pw1Field, pw2Field;
	
	private JCheckBox boxAddDocuments, boxEditDocuments, boxDeleteDocuments, boxImportDocuments;
	private JCheckBox boxAddStatements, boxEditStatements, boxDeleteStatements;
	private JCheckBox boxEditAttributes, boxEditRegex, boxEditStatementTypes, boxEditCoders;
	private JCheckBox boxViewOthersDocuments, boxEditOthersDocuments, boxViewOthersStatements, boxEditOthersStatements;

	public CoderManager() {
		this.setModal(true);
		this.setTitle("Coder manager");
		this.setLayout(new BorderLayout());
		
		// get coders from database
		ArrayList<Coder> coderArrayList = Dna.sql.getCoders();
		Coder[] coders = new Coder[coderArrayList.size()];
		coders = coderArrayList.toArray(coders);
		
		// coder list
		coderList = new JList<Coder>(coders);
		coderList.setCellRenderer(new CoderRenderer());
		coderList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane listScroller = new JScrollPane(coderList);
		listScroller.setPreferredSize(new Dimension(200, 800));
		coderList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) { // trigger only if selection has been completed
					Coder c = coderList.getSelectedValue();
					boxAddDocuments.setEnabled(true);
					boxEditDocuments.setEnabled(true);
					boxDeleteDocuments.setEnabled(true);
					boxImportDocuments.setEnabled(true);
					boxAddStatements.setEnabled(true);
					boxEditStatements.setEnabled(true);
					boxDeleteStatements.setEnabled(true);
					boxEditAttributes.setEnabled(true);
					boxEditRegex.setEnabled(true);
					boxEditStatementTypes.setEnabled(true);
					boxEditCoders.setEnabled(true);
					boxViewOthersDocuments.setEnabled(true);
					boxEditOthersDocuments.setEnabled(true);
					boxViewOthersStatements.setEnabled(true);
					boxEditOthersStatements.setEnabled(true);
					if (c.isPermissionAddDocuments()) {
						boxAddDocuments.setSelected(true);
					} else {
						boxAddDocuments.setSelected(false);
					}
					if (c.isPermissionEditDocuments()) {
						boxEditDocuments.setSelected(true);
					} else {
						boxEditDocuments.setSelected(false);
					}
					if (c.isPermissionDeleteDocuments()) {
						boxDeleteDocuments.setSelected(true);
					} else {
						boxDeleteDocuments.setSelected(false);
					}
					if (c.isPermissionImportDocuments()) {
						boxImportDocuments.setSelected(true);
					} else {
						boxImportDocuments.setSelected(false);
					}
					if (c.isPermissionAddStatements()) {
						boxAddStatements.setSelected(true);
					} else {
						boxAddStatements.setSelected(false);
					}
					if (c.isPermissionEditStatements()) {
						boxEditStatements.setSelected(true);
					} else {
						boxEditStatements.setSelected(false);
					}
					if (c.isPermissionDeleteStatements()) {
						boxDeleteStatements.setSelected(true);
					} else {
						boxDeleteStatements.setSelected(false);
					}
					if (c.isPermissionEditAttributes()) {
						boxEditAttributes.setSelected(true);
					} else {
						boxEditAttributes.setSelected(false);
					}
					if (c.isPermissionEditRegex()) {
						boxEditRegex.setSelected(true);
					} else {
						boxEditRegex.setSelected(false);
					}
					if (c.isPermissionEditStatementTypes()) {
						boxEditStatementTypes.setSelected(true);
					} else {
						boxEditStatementTypes.setSelected(false);
					}
					if (c.isPermissionEditCoders()) {
						boxEditCoders.setSelected(true);
					} else {
						boxEditCoders.setSelected(false);
					}
					if (c.isPermissionViewOthersDocuments()) {
						boxViewOthersDocuments.setSelected(true);
					} else {
						boxViewOthersDocuments.setSelected(false);
					}
					if (c.isPermissionEditOthersDocuments()) {
						boxEditOthersDocuments.setSelected(true);
					} else {
						boxEditOthersDocuments.setSelected(false);
					}
					if (c.isPermissionViewOthersStatements()) {
						boxViewOthersStatements.setSelected(true);
					} else {
						boxViewOthersStatements.setSelected(false);
					}
					if (c.isPermissionEditOthersStatements()) {
						boxEditOthersStatements.setSelected(true);
					} else {
						boxEditOthersStatements.setSelected(false);
					}
					
					nameLabel.setEnabled(true);
					nameField.setEnabled(true);
					colorLabel.setEnabled(true);
					colorButton.setEnabled(true);
					pw1Label.setEnabled(true);
					pw1Field.setEnabled(true);
					pw2Label.setEnabled(true);
					pw2Field.setEnabled(true);
					
					nameField.setText(c.getName());
					colorButton.setColor(c.getColor());
				}
			}
		});
		this.add(listScroller, BorderLayout.WEST);
		
		// details panel
		nameField = new JTextField(20);
		nameField.setEnabled(false);
		nameLabel = new JLabel("Name", JLabel.TRAILING);
		nameLabel.setLabelFor(nameField);
		nameLabel.setEnabled(false);
		
		colorButton = new ColorButton();
		colorButton.setEnabled(false);
		colorLabel = new JLabel("Color", JLabel.TRAILING);
		colorLabel.setLabelFor(colorButton);
		colorLabel.setEnabled(false);
		colorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(CoderManager.this, "Choose color...", colorButton.getColor());
				if (newColor != null) {
					colorButton.setColor(newColor);
				}
			}
		});
		
		pw1Field = new JPasswordField(20);
		pw1Field.setEnabled(false);
		pw1Label = new JLabel("New password", JLabel.TRAILING);
		pw1Label.setLabelFor(pw1Field);
		pw1Label.setEnabled(false);
		pw2Field = new JPasswordField(20);
		pw2Field.setEnabled(false);
		pw2Label = new JLabel("Repeat new password", JLabel.TRAILING);
		pw2Label.setLabelFor(pw2Field);
		pw2Label.setEnabled(false);
		DocumentListener pwListener = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				checkPassword();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				checkPassword();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				checkPassword();
			}
			private void checkPassword() {
				String p1 = new String(pw1Field.getPassword());
				String p2 = new String(pw2Field.getPassword());
				if (p1.equals("") || p2.equals("") || !p1.equals(p2)) {
					pw1Field.setForeground(Color.RED);
					pw2Field.setForeground(Color.RED);
					// TODO: disable the button
				} else {
					pw1Field.setForeground(Color.BLACK);
					pw2Field.setForeground(Color.BLACK);
					// TODO: enable the button if other conditions are also met
				}
			}
		};
		pw1Field.getDocument().addDocumentListener(pwListener);
		pw2Field.getDocument().addDocumentListener(pwListener);

		JPanel detailsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.insets = new Insets(5, 5, 0, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		detailsPanel.add(nameLabel, gbc);
		gbc.gridx = 1;
		detailsPanel.add(nameField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		detailsPanel.add(colorLabel, gbc);
		gbc.gridx = 1;
		detailsPanel.add(colorButton, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		detailsPanel.add(pw1Label, gbc);
		gbc.gridx = 1;
		detailsPanel.add(pw1Field, gbc);
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 3;
		detailsPanel.add(pw2Label, gbc);
		gbc.gridx = 1;
		detailsPanel.add(pw2Field, gbc);

		CompoundBorder borderDetails;
		borderDetails = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Coder details"));
		detailsPanel.setBorder(borderDetails);

		// permission panel
		JPanel permissionPanel = new JPanel(new GridBagLayout());
		
		boxAddDocuments = new JCheckBox("Permission to add documents");
		boxEditDocuments = new JCheckBox("Permission to edit documents");
		boxDeleteDocuments = new JCheckBox("Permission to delete documents");
		boxImportDocuments = new JCheckBox("Permission to import documents");
		boxAddStatements = new JCheckBox("Permission to add statements");
		boxEditStatements = new JCheckBox("Permission to edit statements");
		boxDeleteStatements = new JCheckBox("Permission to delete statements");
		boxEditAttributes = new JCheckBox("Permission to edit entities/attributes");
		boxEditRegex = new JCheckBox("Permission to edit regex search terms");
		boxEditStatementTypes = new JCheckBox("Permission to edit statement types");
		boxEditCoders = new JCheckBox("Permission to edit coders");
		boxViewOthersDocuments = new JCheckBox("Permission to view documents of other coders");
		boxEditOthersDocuments = new JCheckBox("Permission to edit documents of other coders");
		boxViewOthersStatements = new JCheckBox("Permission to view statements of other coders");
		boxEditOthersStatements = new JCheckBox("Permission to edit statements of other coders");
		
		boxAddDocuments.setEnabled(false);
		boxEditDocuments.setEnabled(false);
		boxDeleteDocuments.setEnabled(false);
		boxImportDocuments.setEnabled(false);
		boxAddStatements.setEnabled(false);
		boxEditStatements.setEnabled(false);
		boxDeleteStatements.setEnabled(false);
		boxEditAttributes.setEnabled(false);
		boxEditRegex.setEnabled(false);
		boxEditStatementTypes.setEnabled(false);
		boxEditCoders.setEnabled(false);
		boxViewOthersDocuments.setEnabled(false);
		boxEditOthersDocuments.setEnabled(false);
		boxViewOthersStatements.setEnabled(false);
		boxEditOthersStatements.setEnabled(false);
		
		GridBagConstraints g = new GridBagConstraints();
		g.weightx = 1.0;
		g.anchor = GridBagConstraints.WEST;
		g.gridx = 0;
		g.gridy = 0;
		permissionPanel.add(boxAddDocuments, g);
		g.gridy++;
		permissionPanel.add(boxEditDocuments, g);
		g.gridy++;
		permissionPanel.add(boxDeleteDocuments, g);
		g.gridy++;
		permissionPanel.add(boxImportDocuments, g);
		g.gridy++;
		permissionPanel.add(boxAddStatements, g);
		g.gridy++;
		permissionPanel.add(boxEditStatements, g);
		g.gridy++;
		permissionPanel.add(boxDeleteStatements, g);
		g.gridy++;
		permissionPanel.add(boxEditAttributes, g);
		g.gridy++;
		permissionPanel.add(boxEditRegex, g);
		g.gridy++;
		permissionPanel.add(boxEditStatementTypes, g);
		g.gridy++;
		permissionPanel.add(boxEditCoders, g);
		g.gridy++;
		permissionPanel.add(boxViewOthersDocuments, g);
		g.gridy++;
		permissionPanel.add(boxEditOthersDocuments, g);
		g.gridy++;
		permissionPanel.add(boxViewOthersStatements, g);
		g.gridy++;
		permissionPanel.add(boxEditOthersStatements, g);

		CompoundBorder borderPermissions;
		borderPermissions = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Permissions"));
		permissionPanel.setBorder(borderPermissions);

		// content panel: details and permissions
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(detailsPanel, BorderLayout.NORTH);
		contentPanel.add(permissionPanel, BorderLayout.CENTER);
		
		this.add(contentPanel, BorderLayout.CENTER);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * A custom Button for displaying and choosing a color
	 */
	private class ColorButton extends JButton {
		private static final long serialVersionUID = -8121834065246525986L;
		private Color color;
		
		public ColorButton() {
			this.color = Color.BLACK;
			this.setPreferredSize(new Dimension(18, 18));
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(color);
			g.fillRect(0, 0, 18, 18);
		}
		
		void setColor(Color color) {
			this.color = color;
			this.repaint();
		}
		
		Color getColor() {
			return this.color;
		}
	}
}