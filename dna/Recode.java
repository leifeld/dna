package dna;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class Recode extends JFrame {
	
	JRadioButton removeRadio, changeRadio;
	JCheckBox persCB, orgCB, catCB;
	JComboBox persCombo, orgCombo, catCombo;
	JTextField persTextField, orgTextField, catTextField;
	JLabel persDesc, orgDesc, catDesc, description;
	Container c;
	JPanel modificationPanel, outerSelectionPanel;
	
	public Recode() {
		c = getContentPane();
		this.setTitle("Recode statements");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon recodeIcon = new ImageIcon(getClass().getResource("/icons/database_go.png"));
		this.setIconImage(recodeIcon.getImage());
		
		dna.Dna.mainProgram.updateLists();
		
		ButtonGroup bg1 = new ButtonGroup();
		changeRadio = new JRadioButton("rename or modify entries:", true);
		removeRadio = new JRadioButton("remove all matching statements completely", false);
		bg1.add(removeRadio);
		bg1.add(changeRadio);
		
		persCB = new JCheckBox("person: ", false);
		orgCB = new JCheckBox("organization: ", false);
		catCB = new JCheckBox("category: ", false);
		
		persTextField = new JTextField("%p", 20);
		orgTextField = new JTextField("%o", 20);
		catTextField = new JTextField("%c", 20);
		persDesc = new JLabel("person", JLabel.TRAILING);
		orgDesc = new JLabel("organization", JLabel.TRAILING);
		catDesc = new JLabel("category", JLabel.TRAILING);
		description = new JLabel("%p = person; %o = organization; %c = category");
		
		persCombo = new JComboBox(dna.Dna.mainProgram.persListe.toArray());
		orgCombo = new JComboBox(dna.Dna.mainProgram.orgListe.toArray());
		catCombo = new JComboBox(dna.Dna.mainProgram.catListe.toArray());
		
		Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		JButton okButton = new JButton("OK", okIcon);
		Icon cancelIcon = new ImageIcon(getClass().getResource("/icons/cancel.png"));
		JButton cancelButton = new JButton("cancel", cancelIcon);
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int counter = 0;
				String operation = "";
				if (removeRadio.isSelected()) {
					operation = "remove";
				} else {
					operation = "change";
				}
				
				int dialog = JOptionPane.showConfirmDialog(Recode.this, "Are you sure you want to " + operation + " all statements matching\n" +
						"your above selection?", "Confirmation required", JOptionPane.OK_CANCEL_OPTION);
				
				if (dialog == 0) {
					for (int i = dna.Dna.mainProgram.sc.size() - 1; i >= 0; i--) {
						if ((persCB.isSelected() && persCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getPerson()) && orgCB.isSelected() == false && catCB.isSelected() == false) // 1 0 0
								|| (persCB.isSelected() == false && orgCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getOrganization()) && orgCB.isSelected() == true && catCB.isSelected() == false) // 0 1 0
								|| (persCB.isSelected() == false && catCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getCategory()) && orgCB.isSelected() == false && catCB.isSelected() == true) // 0 0 1
								|| (persCB.isSelected() == true && orgCB.isSelected() == true && catCB.isSelected() == true && persCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getPerson()) && orgCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getOrganization()) && catCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getCategory())) // 1 1 1
								|| (persCB.isSelected() == true && orgCB.isSelected() == true && catCB.isSelected() == false && persCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getPerson()) && orgCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getOrganization())) // 1 1 0
								|| (persCB.isSelected() == true && orgCB.isSelected() == false && catCB.isSelected() == true && persCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getPerson()) && catCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getCategory())) // 1 0 1
								|| (persCB.isSelected() == false && orgCB.isSelected() == true && catCB.isSelected() == true && orgCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getOrganization()) && catCombo.getSelectedItem().equals(dna.Dna.mainProgram.sc.get(i).getCategory())) // 0 1 1
								) {
							if (removeRadio.isSelected()) {
								dna.Dna.mainProgram.sc.removeStatement(dna.Dna.mainProgram.sc.get(i).getId());
								counter++;
							} else if (changeRadio.isSelected()) {
								String persOld = dna.Dna.mainProgram.sc.get(i).getPerson();
								String orgOld = dna.Dna.mainProgram.sc.get(i).getOrganization();
								String catOld = dna.Dna.mainProgram.sc.get(i).getCategory();
								String persNew = persTextField.getText();
								persNew = persNew.replaceAll("%p", persOld);
								persNew = persNew.replaceAll("%o", orgOld);
								persNew = persNew.replaceAll("%c", catOld);
								dna.Dna.mainProgram.sc.get(i).setPerson(persNew);
								String orgNew = orgTextField.getText();
								orgNew = orgNew.replaceAll("%p", persOld);
								orgNew = orgNew.replaceAll("%o", orgOld);
								orgNew = orgNew.replaceAll("%c", catOld);
								dna.Dna.mainProgram.sc.get(i).setOrganization(orgNew);
								String catNew = catTextField.getText();
								catNew = catNew.replaceAll("%p", persOld);
								catNew = catNew.replaceAll("%o", orgOld);
								catNew = catNew.replaceAll("%c", catOld);
								dna.Dna.mainProgram.sc.get(i).setCategory(catNew);
								counter++;
							}
						}
					}
					dispose();
				}
				JOptionPane.showMessageDialog(Recode.this, counter + " statements were " + operation + "d.");
			}
		});
		
		JPanel innerSelectionPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,0,3,3);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 0;
		gbc.gridx = 0;
		innerSelectionPanel.add(persCB, gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		innerSelectionPanel.add(persCombo, gbc);
		gbc.gridwidth = 1;
		gbc.gridy = 1;
		gbc.gridx = 0;
		innerSelectionPanel.add(orgCB, gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		innerSelectionPanel.add(orgCombo, gbc);
		gbc.gridwidth = 1;
		gbc.gridy = 2;
		gbc.gridx = 0;
		innerSelectionPanel.add(catCB, gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		innerSelectionPanel.add(catCombo, gbc);
		gbc.gridwidth = 1;
		
		outerSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		outerSelectionPanel.setBorder( new TitledBorder( new EtchedBorder(), "selection (intersection, not union)" ) );
		outerSelectionPanel.add(innerSelectionPanel);
		
		modificationPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbcon = new GridBagConstraints();
		gbcon.insets = new Insets(3,0,3,3);
		gbcon.anchor = GridBagConstraints.WEST;
		gbcon.fill = GridBagConstraints.HORIZONTAL;
		gbcon.gridx = 0;
		gbcon.gridy = 0;
		gbcon.gridwidth = 2;
		modificationPanel.add(removeRadio, gbcon);
		gbcon.gridy = 1;
		modificationPanel.add(changeRadio, gbcon);
		gbcon.insets = new Insets(3,20,3,3);
		gbcon.gridy = 2;
		gbcon.gridwidth = 1;
		modificationPanel.add(persDesc, gbcon);
		gbcon.gridx = 1;
		modificationPanel.add(persTextField, gbcon);
		gbcon.gridx = 0;
		gbcon.gridy = 3;
		modificationPanel.add(orgDesc, gbcon);
		gbcon.gridx = 1;
		modificationPanel.add(orgTextField, gbcon);
		gbcon.gridx = 0;
		gbcon.gridy = 4;
		modificationPanel.add(catDesc, gbcon);
		gbcon.gridx = 1;
		modificationPanel.add(catTextField, gbcon);
		gbcon.gridx = 0;
		gbcon.gridy = 5;
		gbcon.gridwidth = 2;
		modificationPanel.add(description, gbcon);
		
		String modificationToolTip = "example: \"%p (%o)\" yields \"person (organization)\"";
		modificationPanel.setToolTipText(modificationToolTip);
		description.setToolTipText(modificationToolTip);
		persDesc.setToolTipText(modificationToolTip);
		orgDesc.setToolTipText(modificationToolTip);
		catDesc.setToolTipText(modificationToolTip);
		persTextField.setToolTipText(modificationToolTip);
		orgTextField.setToolTipText(modificationToolTip);
		catTextField.setToolTipText(modificationToolTip);
		changeRadio.setToolTipText(modificationToolTip);
		
		JPanel changePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		changePanel.setBorder( new TitledBorder( new EtchedBorder(), "operation" ) );
		changePanel.add(modificationPanel);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		
		JPanel layout = new JPanel(new BorderLayout());
		layout.add(outerSelectionPanel, BorderLayout.NORTH);
		layout.add(changePanel, BorderLayout.CENTER);
		layout.add(buttonPanel, BorderLayout.SOUTH);
		c.add(layout);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}