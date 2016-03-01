package dna;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

@SuppressWarnings("serial")
public class NewDatabaseDialog extends JFrame {

	/**
	 * Create the frame.
	 */
	public NewDatabaseDialog() {
		this.setTitle("Create new database...");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon icon = new ImageIcon(getClass().getResource("/icons/database.png"));
		this.setIconImage(icon.getImage());
		this.setLayout(new BorderLayout());
		
		JPanel menu = new JPanel();
		menu.setLayout(new BorderLayout());

		// Options panel
		JPanel optionsPanel = new OptionsPanel();
		
		// Database panel
		JPanel databasePanel = new JPanel();
		JLabel test1 = new JLabel("test1");
		databasePanel.add(test1);
		
		// Coder panel
		JPanel coderPanel = new JPanel();
		JLabel test2 = new JLabel("test2");
		coderPanel.add(test2);

		// Statement type panel
		JPanel statementTypePanel = new JPanel();
		JLabel test3 = new JLabel("test3");
		statementTypePanel.add(test3);
		
		// Card layout on the right
		JPanel panel = new JPanel();
		CardLayout cl = new CardLayout();
		panel.setLayout(cl);
		panel.add(optionsPanel, "optionsPanel");
		panel.add(databasePanel, "databasePanel");
		panel.add(coderPanel, "coderPanel");
		panel.add(statementTypePanel, "statementTypePanel");

		// Tree on the left
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Options");
		JTree tree = new JTree(top);
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode("Database");
		DefaultMutableTreeNode coderNode = new DefaultMutableTreeNode("Coder");
		DefaultMutableTreeNode statementTypeNode = new DefaultMutableTreeNode("Statement types");
		top.add(databaseNode);
		top.add(coderNode);
		top.add(statementTypeNode);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				String node = (String) tree.getLastSelectedPathComponent().toString();
				if (node.equals("Database")) {
					cl.show(panel, "databasePanel");
				} else if (node.equals("Coder")) {
					cl.show(panel, "coderPanel");
				} else if (node.equals("Statement types")) {
					cl.show(panel, "statementTypePanel");
				} else {
					cl.show(panel, "optionsPanel");
				}
			}
		});
		menu.add(tree);
		
		// Split pane
		JSplitPane sp = new JSplitPane();
		sp.setLeftComponent(menu);
		sp.setRightComponent(panel);
		this.add(sp);
		
		// set location and pack window
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.pack();
	}

	class OptionsPanel extends JPanel {
		OptionsPanel() {
			JTextArea notes = new JTextArea(10, 30);
			notes.setEditable(false);
			notes.setWrapStyleWord(true);
			notes.setLineWrap(true);
			notes.setText("You are about to create a new DNA database. \n\nPlease select a database, specify coders, and create statement types for coding. \n\nSome of these options cannot be reversed later.");
			notes.setOpaque(false);
			this.add(notes);
		}
	}
	
	class DatabasePanel extends JPanel {
		
	}
	
}
