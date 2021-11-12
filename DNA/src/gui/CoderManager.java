package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
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

public class CoderManager extends JDialog {
	private static final long serialVersionUID = -2714067204780133808L;
	private ListModel listModel;
	private JList<Coder> coderList;
	
	private JLabel nameLabel, colorLabel, pw1Label, pw2Label;
	private JTextField nameField;
	private ColorButton colorButton;
	private JPasswordField pw1Field, pw2Field;
	
	private JCheckBox boxAddDocuments, boxEditDocuments, boxDeleteDocuments, boxImportDocuments;
	private JCheckBox boxAddStatements, boxEditStatements, boxDeleteStatements;
	private JCheckBox boxEditAttributes, boxEditRegex, boxEditStatementTypes, boxEditCoders;
	private JCheckBox boxViewOthersDocuments, boxEditOthersDocuments, boxViewOthersStatements, boxEditOthersStatements;
	
	private JLabel viewOthersDocumentsLabel, editOthersDocumentsLabel, viewOthersStatementsLabel, editOthersStatementsLabel;
	private JList<CoderRelation> viewOthersDocumentsList, editOthersDocumentsList, viewOthersStatementsList, editOthersStatementsList;

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
		listScroller.setPreferredSize(new Dimension(200, 600));
		coderList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false && !coderList.isSelectionEmpty()) { // trigger only if selection has been completed and a coder is selected
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
					
					// coder relations
					listModel.clear();
					viewOthersDocumentsList.setEnabled(true);
					viewOthersDocumentsLabel.setEnabled(true);
					editOthersDocumentsList.setEnabled(true);
					editOthersDocumentsLabel.setEnabled(true);
					viewOthersStatementsList.setEnabled(true);
					viewOthersStatementsLabel.setEnabled(true);
					editOthersStatementsList.setEnabled(true);
					editOthersStatementsLabel.setEnabled(true);
					for (int i = 0; i < coderArrayList.size(); i++) {
						if (coderArrayList.get(i).getId() != c.getId()) {
							CoderRelation cr = c.getCoderRelations().get(coderArrayList.get(i).getId());
							cr.setTargetCoderName(coderArrayList.get(i).getName());
							cr.setTargetCoderColor(coderArrayList.get(i).getColor());
							listModel.add(cr);
						}
					}
					viewOthersDocumentsList.setSelectedIndices(listModel.getSelectedViewOthersDocuments());
					editOthersDocumentsList.setSelectedIndices(listModel.getSelectedEditOthersDocuments());
					viewOthersStatementsList.setSelectedIndices(listModel.getSelectedViewOthersStatements());
					editOthersStatementsList.setSelectedIndices(listModel.getSelectedEditOthersStatements());
				} else if (e.getValueIsAdjusting() == false && coderList.isSelectionEmpty()) { // reset button was pressed
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
					listModel.clear();
					viewOthersDocumentsList.clearSelection();
					viewOthersDocumentsList.removeAll();
					viewOthersDocumentsList.setEnabled(false);
					viewOthersDocumentsLabel.setEnabled(false);
					editOthersDocumentsList.clearSelection();
					editOthersDocumentsList.removeAll();
					editOthersDocumentsList.setEnabled(false);
					editOthersDocumentsLabel.setEnabled(false);
					viewOthersStatementsList.clearSelection();
					viewOthersStatementsList.removeAll();
					viewOthersStatementsList.setEnabled(false);
					viewOthersStatementsLabel.setEnabled(false);
					editOthersStatementsList.clearSelection();
					editOthersStatementsList.removeAll();
					editOthersStatementsList.setEnabled(false);
					editOthersStatementsLabel.setEnabled(false);
					
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
		
		// coder relations panel
		JPanel coderRelationsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints g2 = new GridBagConstraints();
		g2.gridx = 0;
		g2.gridy = 0;
		g2.anchor = GridBagConstraints.WEST;
		g2.fill = GridBagConstraints.BOTH;
		g2.insets = new Insets(5, 5, 0, 5);
		
		listModel = new ListModel();
		viewOthersDocumentsList = new JList<CoderRelation>(listModel);
		editOthersDocumentsList = new JList<CoderRelation>(listModel);
		viewOthersStatementsList = new JList<CoderRelation>(listModel);
		editOthersStatementsList = new JList<CoderRelation>(listModel);
		
		viewOthersDocumentsList.setPreferredSize(new Dimension(200, 300));
		editOthersDocumentsList.setPreferredSize(new Dimension(200, 300));
		viewOthersStatementsList.setPreferredSize(new Dimension(200, 300));
		editOthersStatementsList.setPreferredSize(new Dimension(200, 300));
		
		CoderRelationRenderer listRenderer = new CoderRelationRenderer();
		viewOthersDocumentsList.setCellRenderer(listRenderer);
		editOthersDocumentsList.setCellRenderer(listRenderer);
		viewOthersStatementsList.setCellRenderer(listRenderer);
		editOthersStatementsList.setCellRenderer(listRenderer);
		
		viewOthersDocumentsLabel = new JLabel("Can view documents by:");
		editOthersDocumentsLabel = new JLabel("Can edit documents by:");
		viewOthersStatementsLabel = new JLabel("Can view statements by:");
		editOthersStatementsLabel = new JLabel("Can edit statements by:");
		
		coderRelationsPanel.add(viewOthersDocumentsLabel, g2);
		g2.gridx = 1;
		coderRelationsPanel.add(editOthersDocumentsLabel, g2);
		g2.insets = new Insets(5, 5, 5, 5);
		g2.gridx = 0;
		g2.gridy = 1;
		coderRelationsPanel.add(viewOthersDocumentsList, g2);
		g2.gridx = 1;
		coderRelationsPanel.add(editOthersDocumentsList, g2);
		g2.insets = new Insets(5, 5, 0, 5);
		g2.gridx = 0;
		g2.gridy = 2;
		coderRelationsPanel.add(viewOthersStatementsLabel, g2);
		g2.gridx = 1;
		coderRelationsPanel.add(editOthersStatementsLabel, g2);
		g2.insets = new Insets(5, 5, 5, 5);
		g2.gridx = 0;
		g2.gridy = 3;
		coderRelationsPanel.add(viewOthersStatementsList, g2);
		g2.gridx = 1;
		coderRelationsPanel.add(editOthersStatementsList, g2);

		viewOthersDocumentsList.setEnabled(false);
		viewOthersDocumentsLabel.setEnabled(false);
		editOthersDocumentsList.setEnabled(false);
		editOthersDocumentsLabel.setEnabled(false);
		viewOthersStatementsList.setEnabled(false);
		viewOthersStatementsLabel.setEnabled(false);
		editOthersStatementsList.setEnabled(false);
		editOthersStatementsLabel.setEnabled(false);
		
		CompoundBorder borderRelations;
		borderRelations = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Coder relations"));
		coderRelationsPanel.setBorder(borderRelations);
		
		this.add(coderRelationsPanel, BorderLayout.EAST);

		// button panel
		JPanel buttonPanel = new JPanel();
		
		ImageIcon resetIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-backspace.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton resetButton = new JButton("Revert changes", resetIcon);
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				coderList.clearSelection();
			}
		});
		buttonPanel.add(resetButton);

		ImageIcon cancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton cancelButton = new JButton("Cancel", cancelIcon);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CoderManager.this.dispose();
			}
		});
		buttonPanel.add(cancelButton);

		ImageIcon applyIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-device-floppy.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton applyButton = new JButton("Apply/save", applyIcon);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				/*
				Coder c = coderList.getSelectedValue();
				c.setName(nameField.getText());
				c.setColor(colorButton.getColor());
				String plainPassword = new String(pw1Field.getPassword());
				StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
				String encryptedPassword = passwordEncryptor.encryptPassword(plainPassword);
				*/
				
				// TODO: extract all the updated information and call a new function updateCoder in the Sql class
			}
		});
		buttonPanel.add(applyButton);
		
		this.add(buttonPanel, BorderLayout.SOUTH);
		
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
	
	class CoderRelationRenderer implements ListCellRenderer<Object> {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			CoderRelation cr = (CoderRelation) value;
			
			Color coderColor = Color.LIGHT_GRAY;
			if (isSelected) {
				coderColor = cr.getTargetCoderColor();
			}
			CoderBadgePanel cbp = new CoderBadgePanel(new Coder(cr.getTargetCoderId(), cr.getTargetCoderName(), coderColor));
			if (!isSelected) {
				cbp.setCoderNameColor(Color.LIGHT_GRAY);
			}
			
			// list background
			Color selectedColor = javax.swing.UIManager.getColor("List.dropCellBackground");
			Color defaultColor = javax.swing.UIManager.getColor("List.background");
			if (isSelected == true) {
				cbp.setBackground(selectedColor);
				
			} else {
				cbp.setBackground(defaultColor);
			}
			cbp.setOpaque(true);
			cbp.setBorder(new EmptyBorder(5, 5, 5, 5));
			
			return cbp;
		}
	}
	
	private class ListModel extends AbstractListModel<CoderRelation> {
		private static final long serialVersionUID = -777621550003651581L;
		
		int sourceCoderId;
		ArrayList<CoderRelation> relations;

		public ListModel() {
			this.sourceCoderId = -1;
			this.relations = new ArrayList<CoderRelation>();
		}
		
		@Override
		public CoderRelation getElementAt(int index) {
			return relations.get(index);
		}

		@Override
		public int getSize() {
			return relations.size();
		}
		
		void add(CoderRelation cr) {
			relations.add(cr);
		}
		
		void setSourceCoderId(int sourceCoderId) {
			this.sourceCoderId = sourceCoderId;
		}
		
		void clear() {
			sourceCoderId = -1;
			relations.clear();
		}

		int[] getSelectedViewOthersDocuments() {
			ArrayList<Integer> indicesArrayList = new ArrayList<Integer>();
			for (int i = 0; i < relations.size(); i++) {
				if (relations.get(i).isViewDocuments()) {
					indicesArrayList.add(i);
				}
			}
			int[] indices = new int[indicesArrayList.size()];
			for (int i = 0; i < indicesArrayList.size(); i++) {
				indices[i] = indicesArrayList.get(i);
			}
			return indices;
		}
		
		int[] getSelectedEditOthersDocuments() {
			ArrayList<Integer> indicesArrayList = new ArrayList<Integer>();
			for (int i = 0; i < relations.size(); i++) {
				if (relations.get(i).isEditDocuments()) {
					indicesArrayList.add(i);
				}
			}
			int[] indices = new int[indicesArrayList.size()];
			for (int i = 0; i < indicesArrayList.size(); i++) {
				indices[i] = indicesArrayList.get(i);
			}
			return indices;
		}

		int[] getSelectedViewOthersStatements() {
			ArrayList<Integer> indicesArrayList = new ArrayList<Integer>();
			for (int i = 0; i < relations.size(); i++) {
				if (relations.get(i).isViewStatements()) {
					indicesArrayList.add(i);
				}
			}
			int[] indices = new int[indicesArrayList.size()];
			for (int i = 0; i < indicesArrayList.size(); i++) {
				indices[i] = indicesArrayList.get(i);
			}
			return indices;
		}
		
		int[] getSelectedEditOthersStatements() {
			ArrayList<Integer> indicesArrayList = new ArrayList<Integer>();
			for (int i = 0; i < relations.size(); i++) {
				if (relations.get(i).isEditStatements()) {
					indicesArrayList.add(i);
				}
			}
			int[] indices = new int[indicesArrayList.size()];
			for (int i = 0; i < indicesArrayList.size(); i++) {
				indices[i] = indicesArrayList.get(i);
			}
			return indices;
		}
	}
}