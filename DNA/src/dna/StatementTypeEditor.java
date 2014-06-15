package dna;

import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class StatementTypeEditor extends JFrame {
	
	private static final long serialVersionUID = -7821187025150495806L;

	public StatementTypeEditor() {
		this.setTitle("Add new article...");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon tableAddIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
		this.setIconImage(tableAddIcon.getImage());
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel test = new JLabel("test");
		// TODO: create editor panels for statement types here 
		// (including a JTable with a custom TableModel and CellRenderer) 
		
		this.add(test);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

}
