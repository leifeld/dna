package dna;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class DocumentProperties extends JPanel {

	public DocumentProperties() {
		this.setLayout(new BorderLayout());
		JLabel test = new JLabel("test");
		add(test, BorderLayout.NORTH);
	}
	
}