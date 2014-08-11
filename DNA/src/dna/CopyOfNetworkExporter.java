package dna;

//import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

public class CopyOfNetworkExporter extends JDialog {
	
	private static final long serialVersionUID = 1L;

	public CopyOfNetworkExporter() {
		this.setTitle("Export data");
		this.setModal(true);
		//ImageIcon networkIcon = new ImageIcon(getClass().getResource(
		//		"/icons/chart_organisation.png"));
		//this.setIconImage(networkIcon.getImage());
		//this.setSize(200, 200);
		
		JPanel statementTypePanel = new JPanel(new BorderLayout());
		JLabel statementTypeQuestion = new JLabel("1. For which statement " +
				"type would you like to create a network?");
		ArrayList<StatementType> typeList = Dna.dna.db.getStatementTypes();
		String[] types = new String[typeList.size()];
		for (int i = 0; i < typeList.size(); i++) {
			types[i] = typeList.get(i).getLabel();
		}
		JComboBox<String> typeBox = new JComboBox<String>(types);
		statementTypePanel.add(statementTypeQuestion, BorderLayout.NORTH);
		statementTypePanel.add(typeBox, BorderLayout.CENTER);
		
		JPanel cards = new JPanel(new CardLayout());
		cards.add(statementTypePanel, "statementType");
		//cards.add(variablePanel, "variables");
		
		this.add(cards);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

}