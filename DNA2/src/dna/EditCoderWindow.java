package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dna.dataStructures.Coder;

@SuppressWarnings("serial")
public class EditCoderWindow extends JDialog {
	Coder coder;
	JTextField nameField;
	JButton addColorButton;
	JCheckBox permAddDocuments, permEditDocuments, permDeleteDocuments, permImportDocuments;
	JCheckBox permViewOtherDocuments, permEditOtherDocuments;
	JCheckBox permAddStatements, permViewOtherStatements, permEditOtherStatements;
	JCheckBox permEditCoders, permEditStatementTypes, permEditRegex;
	
	public EditCoderWindow(Coder coder) {
		this.coder = coder;
		this.setTitle("Coder details");
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		ImageIcon icon = new ImageIcon(getClass().getResource("/icons/user_edit.png"));
		this.setIconImage(icon.getImage());
		this.setLayout(new BorderLayout());
		
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JButton colorButtonTemp = (new JButton() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(this.getForeground());
				g.fillRect(2, 2, 14, 14);
			}
		});
		addColorButton = colorButtonTemp;
		addColorButton.setForeground(coder.getColor());
		addColorButton.setPreferredSize(new Dimension(18, 18));
		addColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color actualColor = ((JButton)e.getSource()).getForeground();
				Color newColor = JColorChooser.showDialog(EditCoderWindow.this, "choose color...", actualColor);
				if (newColor != null) {
					((JButton) e.getSource()).setForeground(newColor);
				}
			}
		});
		namePanel.add(new JLabel("Color: "));
		namePanel.add(addColorButton);
		namePanel.add(Box.createRigidArea(new Dimension(5,5)));
		JLabel nameLabel = new JLabel("Name: ");
		namePanel.add(nameLabel);
		namePanel.add(Box.createRigidArea(new Dimension(5,5)));
		nameField = new JTextField(coder.getName());
		nameField.setColumns(40);
		namePanel.add(nameField);
		this.add(namePanel, BorderLayout.NORTH);
		
		JPanel permPanel = new JPanel(new GridLayout(4, 3));
		permAddDocuments = new JCheckBox("add documents");
		permEditDocuments = new JCheckBox("edit documents");
		permDeleteDocuments = new JCheckBox("delete documents");
		permImportDocuments = new JCheckBox("import documents");
		permViewOtherDocuments = new JCheckBox("view others' documents");
		permEditOtherDocuments = new JCheckBox("edit others' documents");
		permAddStatements = new JCheckBox("add statements");
		permViewOtherStatements = new JCheckBox("view others' statements");
		permEditOtherStatements = new JCheckBox("edit others' statements");
		permEditCoders = new JCheckBox("add coders");
		permEditStatementTypes = new JCheckBox("edit statement types");
		permEditRegex = new JCheckBox("edit regex settings");
		permPanel.add(permAddDocuments);
		permPanel.add(permEditDocuments);
		permPanel.add(permDeleteDocuments);
		permPanel.add(permImportDocuments);
		permPanel.add(permViewOtherDocuments);
		permPanel.add(permEditOtherDocuments);
		permPanel.add(permAddStatements);
		permPanel.add(permViewOtherStatements);
		permPanel.add(permEditOtherStatements);
		permPanel.add(permEditCoders);
		permPanel.add(permEditStatementTypes);
		permPanel.add(permEditRegex);
		permAddDocuments.setSelected(coder.getPermissions().get("addDocuments"));
		permEditDocuments.setSelected(coder.getPermissions().get("editDocuments"));
		permDeleteDocuments.setSelected(coder.getPermissions().get("deleteDocuments"));
		permImportDocuments.setSelected(coder.getPermissions().get("importDocuments"));
		permViewOtherDocuments.setSelected(coder.getPermissions().get("viewOthersDocuments"));
		permEditOtherDocuments.setSelected(coder.getPermissions().get("editOthersDocuments"));
		permAddStatements.setSelected(coder.getPermissions().get("addStatements"));
		permViewOtherStatements.setSelected(coder.getPermissions().get("viewOthersStatements"));
		permEditOtherStatements.setSelected(coder.getPermissions().get("editOthersStatements"));
		permEditCoders.setSelected(coder.getPermissions().get("editCoders"));
		permEditStatementTypes.setSelected(coder.getPermissions().get("editStatementTypes"));
		permEditRegex.setSelected(coder.getPermissions().get("editRegex"));
		this.add(permPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton okButton = new JButton("OK", new ImageIcon(getClass().getResource("/icons/accept.png")));
		if (nameField.getText().equals("")) {
			okButton.setEnabled(false);
		}
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				coder.setName(nameField.getText());
				coder.setColor(addColorButton.getForeground());
				coder.getPermissions().put("addDocuments", permAddDocuments.isSelected());
				coder.getPermissions().put("editDocuments", permEditDocuments.isSelected());
				coder.getPermissions().put("deleteDocuments", permDeleteDocuments.isSelected());
				coder.getPermissions().put("importDocuments", permImportDocuments.isSelected());
				coder.getPermissions().put("viewOthersDocuments", permViewOtherDocuments.isSelected());
				coder.getPermissions().put("editOthersDocuments", permEditOtherDocuments.isSelected());
				coder.getPermissions().put("addStatements", permAddStatements.isSelected());
				coder.getPermissions().put("viewOthersStatements", permViewOtherStatements.isSelected());
				coder.getPermissions().put("editOthersStatements", permEditOtherStatements.isSelected());
				coder.getPermissions().put("editCoders", permEditCoders.isSelected());
				coder.getPermissions().put("editStatementTypes", permEditStatementTypes.isSelected());
				coder.getPermissions().put("editRegex", permEditRegex.isSelected());
				setVisible(false);
			}
		});
		JButton cancelButton = new JButton("Cancel", new ImageIcon(getClass().getResource("/icons/cancel.png")));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelAction();
			}
		});
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				check();
			}
			public void removeUpdate(DocumentEvent e) {
				check();
			}
			public void changedUpdate(DocumentEvent e) {
				check();
			}
			public void check() {
				if (nameField.getText().equals("")) {
					okButton.setEnabled(false);
				} else {
					okButton.setEnabled(true);
				}
			}
		});
		
		this.add(buttonPanel, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cancelAction();
			}
		});
		
		this.pack();
		this.setModal(true);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setResizable(false);
		namePanel.requestFocus();
	}
	
	public void cancelAction() {
		dispose();
	}
	
	public void setCoder(Coder coder) {
		this.setCoder(coder);
	}
	
	public Coder getCoder() {
		return(this.coder);
	}
}