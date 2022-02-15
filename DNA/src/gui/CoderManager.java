package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jasypt.util.password.StrongPasswordEncryptor;

import dna.Dna;
import model.Coder;
import model.CoderRelation;

/**
 * A dialog for adding and deleting coders, editing coders' permissions, and
 * editing whose documents and statements they can view and edit.
 */
class CoderManager extends JDialog {
	private static final long serialVersionUID = -2714067204780133808L;
	private ArrayList<Coder> coderArrayList;
	private JList<Coder> coderList;
	
	private JLabel nameLabel, colorLabel, pw1Label, pw2Label;
	private JTextField nameField;
	private ColorButton colorButton;
	private JPasswordField pw1Field, pw2Field;
	
	private JCheckBox boxAddDocuments, boxEditDocuments, boxDeleteDocuments, boxImportDocuments;
	private JCheckBox boxAddStatements, boxEditStatements, boxDeleteStatements;
	private JCheckBox boxEditAttributes, boxEditRegex, boxEditStatementTypes, boxEditCoders, boxEditCoderRelations;
	private JCheckBox boxViewOthersDocuments, boxEditOthersDocuments, boxViewOthersStatements, boxEditOthersStatements;
	
	CoderRelationsPanel coderRelationsPanel;
	Coder selectedCoderCopy;
	
	private JButton reloadButton, addButton, deleteButton, applyButton;

	/**
	 * Constructor: create a new coder manager dialog.
	 */
	CoderManager(Frame parent) {
		super(parent, "Coder manager", true);
		this.setModal(true);
		this.setTitle("Coder manager");
		ImageIcon coderIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-users.png"));
		this.setIconImage(coderIcon.getImage());
		this.setLayout(new BorderLayout());
		
		// get coders from database
		coderArrayList = Dna.sql.getCoders();
		Coder[] coders = new Coder[coderArrayList.size()];
		coders = coderArrayList.toArray(coders);

		// coder list
		coderList = new JList<Coder>(coders);
		coderList.setCellRenderer(new CoderRenderer());
		coderList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane listScroller = new JScrollPane(coderList);
		listScroller.setPreferredSize(new Dimension(200, 600));
		coderList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					loadCoder();
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

		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				if (selectedCoderCopy != null) {
					selectedCoderCopy.setName(nameField.getText());
				}
				checkButtons();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (selectedCoderCopy != null) {
					selectedCoderCopy.setName(nameField.getText());
				}
				checkButtons();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				if (selectedCoderCopy != null) {
					selectedCoderCopy.setName(nameField.getText());
				}
				checkButtons();
			}
		};
		nameField.getDocument().addDocumentListener(documentListener);
		
		colorButton = new ColorButton();
		colorButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		colorButton.setEnabled(false);
		colorLabel = new JLabel("Color", JLabel.TRAILING);
		colorLabel.setLabelFor(colorButton);
		colorLabel.setEnabled(false);
		colorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(CoderManager.this, "Choose color...", colorButton.getColor());
				if (newColor != null) {
					colorButton.setColor(newColor);
					selectedCoderCopy.setColor(newColor);
					checkButtons();
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
		pw1Field.getDocument().addDocumentListener(documentListener);
		pw2Field.getDocument().addDocumentListener(documentListener);

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
		boxEditCoderRelations = new JCheckBox("Permission to edit coder relations");
		boxViewOthersDocuments = new JCheckBox("Permission to view documents of other coders");
		boxEditOthersDocuments = new JCheckBox("Permission to edit documents of other coders");
		boxViewOthersStatements = new JCheckBox("Permission to view statements of other coders");
		boxEditOthersStatements = new JCheckBox("Permission to edit statements of other coders");
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(boxAddDocuments)) {
					if (boxAddDocuments.isSelected()) {
						selectedCoderCopy.setPermissionAddDocuments(true);
					} else {
						selectedCoderCopy.setPermissionAddDocuments(false);
					}
				} else if (e.getSource().equals(boxEditDocuments)) {
					if (boxEditDocuments.isSelected()) {
						selectedCoderCopy.setPermissionEditDocuments(true);
					} else {
						selectedCoderCopy.setPermissionEditDocuments(false);
					}
				} else if (e.getSource().equals(boxDeleteDocuments)) {
					if (boxDeleteDocuments.isSelected()) {
						selectedCoderCopy.setPermissionDeleteDocuments(true);
					} else {
						selectedCoderCopy.setPermissionDeleteDocuments(false);
					}
				} else if (e.getSource().equals(boxImportDocuments)) {
					if (boxImportDocuments.isSelected()) {
						selectedCoderCopy.setPermissionImportDocuments(true);
					} else {
						selectedCoderCopy.setPermissionImportDocuments(false);
					}
				} else if (e.getSource().equals(boxAddStatements)) {
					if (boxAddStatements.isSelected()) {
						selectedCoderCopy.setPermissionAddStatements(true);
					} else {
						selectedCoderCopy.setPermissionAddStatements(false);
					}
				} else if (e.getSource().equals(boxEditStatements)) {
					if (boxEditStatements.isSelected()) {
						selectedCoderCopy.setPermissionEditStatements(true);
					} else {
						selectedCoderCopy.setPermissionEditStatements(false);
					}
				} else if (e.getSource().equals(boxDeleteStatements)) {
					if (boxDeleteStatements.isSelected()) {
						selectedCoderCopy.setPermissionDeleteStatements(true);
					} else {
						selectedCoderCopy.setPermissionDeleteStatements(false);
					}
				} else if (e.getSource().equals(boxEditAttributes)) {
					if (boxEditAttributes.isSelected()) {
						selectedCoderCopy.setPermissionEditAttributes(true);
					} else {
						selectedCoderCopy.setPermissionEditAttributes(false);
					}
				} else if (e.getSource().equals(boxEditRegex)) {
					if (boxEditRegex.isSelected()) {
						selectedCoderCopy.setPermissionEditRegex(true);
					} else {
						selectedCoderCopy.setPermissionEditRegex(false);
					}
				} else if (e.getSource().equals(boxEditStatementTypes)) {
					if (boxEditStatementTypes.isSelected()) {
						selectedCoderCopy.setPermissionEditStatementTypes(true);
					} else {
						selectedCoderCopy.setPermissionEditStatementTypes(false);
					}
				} else if (e.getSource().equals(boxEditCoders)) {
					if (boxEditCoders.isSelected()) {
						selectedCoderCopy.setPermissionEditCoders(true);
					} else {
						selectedCoderCopy.setPermissionEditCoders(false);
					}
				} else if (e.getSource().equals(boxEditCoderRelations)) {
					if (boxEditCoderRelations.isSelected()) {
						selectedCoderCopy.setPermissionEditCoderRelations(true);
					} else {
						selectedCoderCopy.setPermissionEditCoderRelations(false);
					}
				} else if (e.getSource().equals(boxViewOthersDocuments)) {
					if (boxViewOthersDocuments.isSelected()) {
						selectedCoderCopy.setPermissionViewOthersDocuments(true);
					} else {
						selectedCoderCopy.setPermissionViewOthersDocuments(false);
					}
				} else if (e.getSource().equals(boxEditOthersDocuments)) {
					if (boxEditOthersDocuments.isSelected()) {
						selectedCoderCopy.setPermissionEditOthersDocuments(true);
					} else {
						selectedCoderCopy.setPermissionEditOthersDocuments(false);
					}
				} else if (e.getSource().equals(boxViewOthersStatements)) {
					if (boxViewOthersStatements.isSelected()) {
						selectedCoderCopy.setPermissionViewOthersStatements(true);
					} else {
						selectedCoderCopy.setPermissionViewOthersStatements(false);
					}
				} else if (e.getSource().equals(boxEditOthersStatements)) {
					if (boxEditOthersStatements.isSelected()) {
						selectedCoderCopy.setPermissionEditOthersStatements(true);
					} else {
						selectedCoderCopy.setPermissionEditOthersStatements(false);
					}
				}
				checkButtons();
			}
		};
		boxAddDocuments.addActionListener(al);
		boxEditDocuments.addActionListener(al);
		boxDeleteDocuments.addActionListener(al);
		boxImportDocuments.addActionListener(al);
		boxAddStatements.addActionListener(al);
		boxEditStatements.addActionListener(al);
		boxDeleteStatements.addActionListener(al);
		boxEditAttributes.addActionListener(al);
		boxEditRegex.addActionListener(al);
		boxEditStatementTypes.addActionListener(al);
		boxEditCoders.addActionListener(al);
		boxEditCoderRelations.addActionListener(al);
		boxViewOthersDocuments.addActionListener(al);
		boxEditOthersDocuments.addActionListener(al);
		boxViewOthersStatements.addActionListener(al);
		boxEditOthersStatements.addActionListener(al);
		
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
		boxEditCoderRelations.setEnabled(false);
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
		permissionPanel.add(boxEditCoderRelations, g);
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
		
		// coder relations panel
		coderRelationsPanel = new CoderRelationsPanel();
		JTable coderRelationTable = coderRelationsPanel.getTable();
		coderRelationTable.addMouseListener(new java.awt.event.MouseAdapter() { // monitor mouse clicks in the coder relation table to edit boolean permissions
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = coderRelationTable.rowAtPoint(evt.getPoint());
				int col = coderRelationTable.columnAtPoint(evt.getPoint());
				if (selectedCoderCopy.getId() > 1 && row >= 0 && col > 0 && col < 5) { // do not save any changes for Admin coder (ID = 1)
					boolean newValue = !(boolean) coderRelationTable.getValueAt(row, col);
					int coderId = ((Coder) coderRelationTable.getValueAt(row, 0)).getId();
					if (col == 1) {
						selectedCoderCopy.getCoderRelations().get(coderId).setViewDocuments(newValue);
					} else if (col == 2) {
						selectedCoderCopy.getCoderRelations().get(coderId).setEditDocuments(newValue);
					} else if (col == 3) {
						selectedCoderCopy.getCoderRelations().get(coderId).setViewStatements(newValue);
					} else if (col == 4) {
						selectedCoderCopy.getCoderRelations().get(coderId).setEditStatements(newValue);
					}
					coderRelationTable.setValueAt(newValue, row, col);
					checkButtons();
				}
			}
		});
		this.add(coderRelationsPanel, BorderLayout.EAST);

		// button panel
		JPanel buttonPanel = new JPanel();

		ImageIcon deleteIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-user-minus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		deleteButton = new JButton("Delete coder", deleteIcon);
		deleteButton.setToolTipText("Delete the currently selected coder.");
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!coderList.isSelectionEmpty()) {
					int coderId = coderList.getSelectedValue().getId();
					int[] counts = Dna.sql.countCoderItems(coderId);
					int dialog = JOptionPane.showConfirmDialog(CoderManager.this,
							"Delete Coder " + coderId + " from the database?\nThe coder is associated with " + counts[0] + " documents and " + counts[1] + " statements,\nwhich will also be deleted permanently.",
							"Confirmation",
							JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						boolean success = Dna.sql.deleteCoder(coderId);
						if (success) {
							coderList.clearSelection();
							repopulateCoderListFromDatabase(-1); // -1 means select no coder after loading coders
							JOptionPane.showMessageDialog(CoderManager.this, "Coder " + coderId + " was successfully deleted.");
						} else {
							JOptionPane.showMessageDialog(CoderManager.this, "Coder " + coderId + " could not be deleted. Check the message log for details.");
						}
					}
				}
			}
		});
		deleteButton.setEnabled(false);
		buttonPanel.add(deleteButton);

		ImageIcon addIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-user-plus.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		addButton = new JButton("Add new coder...", addIcon);
		addButton.setToolTipText("Create and add a new coder...");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(CoderManager.this);
				AddCoderDialog addCoderDialog = new AddCoderDialog(frame);
				String coderName = addCoderDialog.getCoderName();
				String coderPasswordHash = addCoderDialog.getCoderPasswordHash();
				Color coderColor = addCoderDialog.getCoderColor();
				addCoderDialog.dispose();
				if (coderName != null && coderColor != null && coderPasswordHash != null) {
					int coderId = Dna.sql.addCoder(coderName, coderColor, coderPasswordHash);
					repopulateCoderListFromDatabase(coderId); // loadCoder() is executed when list selection changes...
				}
			}
		});
		buttonPanel.add(addButton);

		ImageIcon reloadIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-rotate-clockwise.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		reloadButton = new JButton("Reset", reloadIcon);
		reloadButton.setToolTipText("Reset all the changes made for the current coder and reload the coder details.");
		reloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadCoder();
			}
		});
		reloadButton.setEnabled(false);
		buttonPanel.add(reloadButton);

		ImageIcon applyIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-user-check.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		applyButton = new JButton("Apply / Save", applyIcon);
		applyButton.setToolTipText("Save the changes for the current coder to the database and make them effective.");
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// password
				String newPasswordHash = null;
				String plainPassword = new String(pw1Field.getPassword());
				String repeatPassword = new String(pw2Field.getPassword());
				if (!plainPassword.matches("^\\s*$") && plainPassword.equals(repeatPassword)) {
					StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
					newPasswordHash = passwordEncryptor.encryptPassword(plainPassword);
				}

				// update coder in the database
				if (newPasswordHash != null || !selectedCoderCopy.equals(coderList.getSelectedValue())) {
					int dialog = JOptionPane.showConfirmDialog(CoderManager.this, "Save changes for Coder " + selectedCoderCopy.getId() + " to the database?", "Confirmation", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						boolean success = Dna.sql.updateCoder(selectedCoderCopy, newPasswordHash);
						repopulateCoderListFromDatabase(coderList.getSelectedValue().getId());
						if (success) {
							JOptionPane.showMessageDialog(CoderManager.this, "Changes for Coder " + selectedCoderCopy.getId() + " were successfully saved.");
						} else {
							JOptionPane.showMessageDialog(CoderManager.this, "Changes for Coder " + selectedCoderCopy.getId() + " could not be saved. Check the message log for details.");
						}
					}
					checkButtons();
				}
			}
		});
		applyButton.setEnabled(false);
		buttonPanel.add(applyButton);

		ImageIcon cancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JButton cancelButton = new JButton("Close", cancelIcon);
		cancelButton.setToolTipText("Close the coder manager without saving any changes.");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CoderManager.this.dispose();
			}
		});
		buttonPanel.add(cancelButton);

		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Load all coders from the database and put them into the GUI. A specified
	 * coder can be selected afterwards. If this is not desired, a value of
	 * {@code -1} can be provided.
	 * 
	 * @param selectCoderId  ID of the coder to be selected. Can be {@code -1}.
	 */
	private void repopulateCoderListFromDatabase(int selectCoderId) {
		DefaultListModel<Coder> model = new DefaultListModel<Coder>();
		coderArrayList = Dna.sql.getCoders();
		int coderIndex = -1;
		for (int i = 0; i < coderArrayList.size(); i++) {
			model.addElement(coderArrayList.get(i));
			if (coderArrayList.get(i).getId() == selectCoderId) {
				coderIndex = i;
			}
		}
		coderList.setModel(model);
		if (coderIndex > -1) {
			coderList.setSelectedIndex(coderIndex);
		}
	}
	
	/**
	 * Check which coder is selected and populate the details, permissions, and
	 * coder relation lists and enable or disable GUI controls.
	 */
	private void loadCoder() {
		if (!coderList.isSelectionEmpty()) { // trigger only if selection has been completed and a coder is selected
			selectedCoderCopy = new Coder(coderList.getSelectedValue());
			if (selectedCoderCopy.getId() == 1) { // do not make boxes editable if it is the Admin coder (ID = 1)
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
				boxEditCoderRelations.setEnabled(false);
				boxViewOthersDocuments.setEnabled(false);
				boxEditOthersDocuments.setEnabled(false);
				boxViewOthersStatements.setEnabled(false);
				boxEditOthersStatements.setEnabled(false);
			} else {
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
				boxEditCoderRelations.setEnabled(true);
				boxViewOthersDocuments.setEnabled(true);
				boxEditOthersDocuments.setEnabled(true);
				boxViewOthersStatements.setEnabled(true);
				boxEditOthersStatements.setEnabled(true);
			}
			if (selectedCoderCopy.isPermissionAddDocuments()) {
				boxAddDocuments.setSelected(true);
			} else {
				boxAddDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditDocuments()) {
				boxEditDocuments.setSelected(true);
			} else {
				boxEditDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionDeleteDocuments()) {
				boxDeleteDocuments.setSelected(true);
			} else {
				boxDeleteDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionImportDocuments()) {
				boxImportDocuments.setSelected(true);
			} else {
				boxImportDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionAddStatements()) {
				boxAddStatements.setSelected(true);
			} else {
				boxAddStatements.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditStatements()) {
				boxEditStatements.setSelected(true);
			} else {
				boxEditStatements.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionDeleteStatements()) {
				boxDeleteStatements.setSelected(true);
			} else {
				boxDeleteStatements.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditAttributes()) {
				boxEditAttributes.setSelected(true);
			} else {
				boxEditAttributes.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditRegex()) {
				boxEditRegex.setSelected(true);
			} else {
				boxEditRegex.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditStatementTypes()) {
				boxEditStatementTypes.setSelected(true);
			} else {
				boxEditStatementTypes.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditCoders()) {
				boxEditCoders.setSelected(true);
			} else {
				boxEditCoders.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditCoderRelations()) {
				boxEditCoderRelations.setSelected(true);
			} else {
				boxEditCoderRelations.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionViewOthersDocuments()) {
				boxViewOthersDocuments.setSelected(true);
			} else {
				boxViewOthersDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditOthersDocuments()) {
				boxEditOthersDocuments.setSelected(true);
			} else {
				boxEditOthersDocuments.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionViewOthersStatements()) {
				boxViewOthersStatements.setSelected(true);
			} else {
				boxViewOthersStatements.setSelected(false);
			}
			if (selectedCoderCopy.isPermissionEditOthersStatements()) {
				boxEditOthersStatements.setSelected(true);
			} else {
				boxEditOthersStatements.setSelected(false);
			}
			
			nameLabel.setEnabled(true);
			colorLabel.setEnabled(true);
			pw1Label.setEnabled(true);
			pw2Label.setEnabled(true);
			
			nameField.setText(selectedCoderCopy.getName());
			colorButton.setColor(selectedCoderCopy.getColor());

			if (selectedCoderCopy.getId() == 1) { // do not permit editing if it is the Admin coder (ID = 1)
				nameField.setEnabled(false);
				colorButton.setEnabled(false);
				pw1Field.setEnabled(false);
				pw2Field.setEnabled(false);
			} else {
				nameField.setEnabled(true);
				colorButton.setEnabled(true);
				pw1Field.setEnabled(true);
				pw2Field.setEnabled(true);
			}
			
			// coder relations
			coderRelationsPanel.getTable().setEnabled(true);
			coderRelationsPanel.getModel().clear();
			for (HashMap.Entry<Integer, CoderRelation> entry : selectedCoderCopy.getCoderRelations().entrySet()) {
				coderRelationsPanel.getModel().addRow(entry.getValue());
			}
			
			if (selectedCoderCopy.getId() == 1) { // do not permitting selecting or unselecting coders if the Admin coder is selected (ID = 1)
				coderRelationsPanel.getTable().setEnabled(false);
			} else {
				coderRelationsPanel.getTable().setEnabled(true);
			}
		} else if (coderList.isSelectionEmpty()) { // reset button was pressed
			selectedCoderCopy = null;
			
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
			boxEditCoderRelations.setEnabled(false);
			boxViewOthersDocuments.setEnabled(false);
			boxEditOthersDocuments.setEnabled(false);
			boxViewOthersStatements.setEnabled(false);
			boxEditOthersStatements.setEnabled(false);
			
			boxAddDocuments.setSelected(false);
			boxEditDocuments.setSelected(false);
			boxDeleteDocuments.setSelected(false);
			boxImportDocuments.setSelected(false);
			boxAddStatements.setSelected(false);
			boxEditStatements.setSelected(false);
			boxDeleteStatements.setSelected(false);
			boxEditAttributes.setSelected(false);
			boxEditRegex.setSelected(false);
			boxEditStatementTypes.setSelected(false);
			boxEditCoders.setSelected(false);
			boxEditCoderRelations.setSelected(false);
			boxViewOthersDocuments.setSelected(false);
			boxEditOthersDocuments.setSelected(false);
			boxViewOthersStatements.setSelected(false);
			boxEditOthersStatements.setSelected(false);
			
			nameLabel.setEnabled(false);
			nameField.setEnabled(false);
			colorLabel.setEnabled(false);
			colorButton.setEnabled(false);
			pw1Label.setEnabled(false);
			pw1Field.setEnabled(false);
			pw2Label.setEnabled(false);
			pw2Field.setEnabled(false);
			
			nameField.setText("");
			colorButton.setColor(Color.BLACK);
			
			// coder relations
			coderRelationsPanel.getModel().clear();
			coderRelationsPanel.getTable().setEnabled(false);
		}
	}

	/**
	 * Check all the details and permissions for changes and adjust buttons.
	 */
	private void checkButtons() {
		boolean valid = true;
		if (coderList.isSelectionEmpty()) {
			valid = false;
			reloadButton.setEnabled(false);
			deleteButton.setEnabled(false);
		} else {
			reloadButton.setEnabled(true);
			if (coderList.getSelectedValue().getId() != 1) {
				deleteButton.setEnabled(true);
			}
		}
		if (coderList.getSelectedValue() != null && coderList.getSelectedValue().getId() == 1) {
			valid = false;
			deleteButton.setEnabled(false);
		}
		if (nameField.getText().matches("^\\s*$") || nameField.getText().length() > 190) {
			valid = false;
		}
		
		if (selectedCoderCopy != null && selectedCoderCopy.getId() != 1 && selectedCoderCopy.equals(coderList.getSelectedValue())) {
			valid = false;
			reloadButton.setEnabled(false);
		} else if (selectedCoderCopy != null && selectedCoderCopy.getId() == 1) {
			reloadButton.setEnabled(false);
		} else if (selectedCoderCopy != null) {
			reloadButton.setEnabled(true);
		} else {
			reloadButton.setEnabled(false);
		}
		
		String plainPassword = new String(pw1Field.getPassword());
		String repeatPassword = new String(pw2Field.getPassword());
		if (plainPassword.matches("^\\s+$") || !plainPassword.equals(repeatPassword)) {
			valid = false;
			pw1Field.setForeground(Color.RED);
			pw2Field.setForeground(Color.RED);
		} else {
			pw1Field.setForeground(Color.BLACK);
			pw2Field.setForeground(Color.BLACK);
			if (!plainPassword.matches("^\\s*$")) { // activate apply button if there is a valid new password, irrespective of other coder details
				valid = true;
			}
		}

		if (valid) {
			applyButton.setEnabled(true);
		} else {
			applyButton.setEnabled(false);
		}
	}
	
	/**
	 * A dialog window for adding a new coder. It contains a simple form for the
	 * name, color, and password of the new coder as well as two buttons.
	 */
	private class AddCoderDialog extends JDialog {
		private static final long serialVersionUID = 2704900961177249691L;
		private JButton addOkButton, addCancelButton;
		private JTextField addNameField;
		private JPasswordField addPw1Field, addPw2Field;
		private String name;
		private Color color;
		private String passwordHash;
		
		/**
		 * Create a new instance of an add coder dialog.
		 */
		AddCoderDialog(Frame parent) {
			super(parent, "Add coder", true);
			this.setModal(true);
			this.setTitle("Add new coder");
			this.setLayout(new BorderLayout());
			
			JLabel addNameLabel = new JLabel("Name");
			addNameField = new JTextField(20);
			addNameLabel.setLabelFor(addNameField);
			addNameLabel.setToolTipText("Enter the name of a new coder here.");
			addNameField.setToolTipText("Enter the name of a new coder here.");
			addNameField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
			});

			ColorButton addColorButton = new ColorButton();
			addColorButton.setColor(new Color(69, 212, 255));
			addColorButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			JLabel addColorLabel = new JLabel("Color", JLabel.TRAILING);
			addColorLabel.setLabelFor(addColorButton);
			addColorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color newColor = JColorChooser.showDialog(AddCoderDialog.this, "Choose color...", colorButton.getColor());
					if (newColor != null) {
						addColorButton.setColor(newColor);
					}
				}
			});
			
			addPw1Field = new JPasswordField(20);
			JLabel addPw1Label = new JLabel("Password", JLabel.TRAILING);
			addPw1Label.setLabelFor(addPw1Field);
			addPw2Field = new JPasswordField(20);
			JLabel addPw2Label = new JLabel("Repeat password", JLabel.TRAILING);
			addPw2Label.setLabelFor(addPw2Field);
			DocumentListener addPwListener = new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					checkAddButtons();
				}
			};
			addPw1Field.getDocument().addDocumentListener(addPwListener);
			addPw2Field.getDocument().addDocumentListener(addPwListener);

			JPanel formPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.insets = new Insets(5, 5, 0, 5);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 0;
			gbc.gridy = 0;
			formPanel.add(addNameLabel, gbc);
			gbc.gridx = 1;
			formPanel.add(addNameField, gbc);
			gbc.gridx = 0;
			gbc.gridy = 1;
			formPanel.add(addColorLabel, gbc);
			gbc.gridx = 1;
			formPanel.add(addColorButton, gbc);
			gbc.gridx = 0;
			gbc.gridy = 2;
			formPanel.add(addPw1Label, gbc);
			gbc.gridx = 1;
			formPanel.add(addPw1Field, gbc);
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.gridx = 0;
			gbc.gridy = 3;
			formPanel.add(addPw2Label, gbc);
			gbc.gridx = 1;
			formPanel.add(addPw2Field, gbc);
			this.add(formPanel, BorderLayout.CENTER);
			
			JPanel addButtonPanel = new JPanel();
			ImageIcon addCancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
			addCancelButton = new JButton("Cancel", addCancelIcon);
			addCancelButton.setToolTipText("Close without adding a new coder.");
			addCancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AddCoderDialog.this.dispose();
				}
			});
			addButtonPanel.add(addCancelButton);

			ImageIcon addOkIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-check.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
			addOkButton = new JButton("OK", addOkIcon);
			addOkButton.setToolTipText("Add this new coder to the database.");
			addOkButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AddCoderDialog.this.name = addNameField.getText();
					AddCoderDialog.this.color = addColorButton.getColor();
					String addPlainPassword = new String(addPw1Field.getPassword());
					StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
					AddCoderDialog.this.passwordHash = passwordEncryptor.encryptPassword(addPlainPassword);
					AddCoderDialog.this.dispose();
				}
			});
			addOkButton.setEnabled(false);
			addButtonPanel.add(addOkButton);

			this.add(addButtonPanel, BorderLayout.SOUTH);
			
			this.pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}
		
		/**
		 * Check if all the input is valid and enable or disable the OK button.
		 */
		private void checkAddButtons() {
			boolean valid = true;
			if (addNameField.getText().matches("^\\s*$")) {
				valid = false;
			}
			String p1 = new String(addPw1Field.getPassword());
			String p2 = new String(addPw2Field.getPassword());
			if (p1.matches("^\\s*$") || !p1.equals(p2)) {
				addPw1Field.setForeground(Color.RED);
				addPw2Field.setForeground(Color.RED);
				valid = false;
			} else {
				addPw1Field.setForeground(Color.BLACK);
				addPw2Field.setForeground(Color.BLACK);
			}
			if (valid) {
				addOkButton.setEnabled(true);
			} else {
				addOkButton.setEnabled(false);
			}
		}
		
		/**
		 * Retrieve the new coder name. Accessible from outside the dialog.
		 * 
		 * @return Name of the new coder.
		 */
		String getCoderName() {
			return this.name;
		}
		
		/**
		 * Retrieve the encrypted password hash String. Accessible from outside
		 * the dialog.
		 * 
		 * @return The hash String corresponding to the entered password.
		 */
		String getCoderPasswordHash() {
			return this.passwordHash;
		}
		
		/**
		 * Retrieve the selected color. Accessible from outside the dialog.
		 * 
		 * @return The selected color.
		 */
		Color getCoderColor() {
			return this.color;
		}
	}
}