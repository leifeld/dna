package dna.panels;

import java.awt.CardLayout;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class BottomCardPanel extends JPanel {
	public SearchWindow searchWindow;
	public RecodePanel recodePanel;
	public AttributePanel attributePanel;

	public BottomCardPanel() {
		this.setLayout(new CardLayout());
		recodePanel = new RecodePanel();
		this.add(recodePanel, "recodePanel");
		searchWindow = new SearchWindow();
		this.add(searchWindow, "searchPanel");
		attributePanel = new AttributePanel();
		this.add(attributePanel, "attributePanel");
	}
}