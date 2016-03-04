package dna;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXTextField;

@SuppressWarnings("serial")
public class NewDatabaseDialog extends JDialog {

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
		JPanel databasePanel = new DatabasePanel();
		
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
		sp.setEnabled(false);
		
		// set location and pack window
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setResizable(false);
		this.pack();
	}

	class OptionsPanel extends JPanel {
		OptionsPanel() {
			JTextArea notes = new JTextArea(10, 46);
			notes.setEditable(false);
			notes.setWrapStyleWord(true);
			notes.setLineWrap(true);
			notes.setText("You are about to create a new DNA database. \n\nPlease select a database, specify coders, and create statement types for coding. \n\nSome of these options cannot be reversed later.");
			notes.setOpaque(false);
			this.add(notes);
		}
	}
	
	class DatabasePanel extends JPanel {
		DatabasePanel() {
			this.setLayout(new BorderLayout());
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
			buttonPanel.add(Box.createRigidArea(new Dimension(5,5)));
			JLabel dbLabel = new JLabel("Type of database:");
			buttonPanel.add(dbLabel);
			buttonPanel.add(Box.createRigidArea(new Dimension(5,5)));
			JRadioButton sqlite = new JRadioButton("Local .dna file (SQLite database)");
			JRadioButton mysql = new JRadioButton("Remote database on a server (mySQL)");
			ButtonGroup bg = new ButtonGroup();
			bg.add(sqlite);
			bg.add(mysql);
			buttonPanel.add(sqlite);
			buttonPanel.add(mysql);
			sqlite.setSelected(true);
			buttonPanel.add(Box.createRigidArea(new Dimension(5,5)));
			this.add(buttonPanel, BorderLayout.NORTH);
			
			JPanel sqlitePanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(0, 10, 0, 10);
			gbc.gridx = 0;
			gbc.gridy = 0;
			JTextField fileField = new JTextField("(No file selected)");
			fileField.setColumns(46);
			fileField.setBackground(sqlitePanel.getBackground());
			fileField.setEditable(false);
			sqlitePanel.add(fileField, gbc);
			gbc.gridx = 0;
			gbc.gridy = 1;
			JButton browseButton = new JButton("Browse...", new ImageIcon(getClass().getResource("/icons/folder.png")));
			browseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".dna") 
									|| f.isDirectory();
						}
						public String getDescription() {
							return "Discourse Network Analyzer database " +
									"(*.dna)";
						}
					});

					int returnVal = fc.showSaveDialog(dna.NewDatabaseDialog.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						String filename = new String(file.getPath());
						if (!filename.endsWith(".dna")) {
							filename = filename + ".dna";
						}
						fileField.setText(filename);
					}
				}
			});
			fileField.setPreferredSize(new Dimension(300, (int) browseButton.getPreferredSize().getHeight()));
			JButton clearButton = new JButton("Clear", new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png")));
			JPanel browseClearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
			browseClearPanel.add(browseButton);
			browseClearPanel.add(Box.createRigidArea(new Dimension(5,5)));
			browseClearPanel.add(clearButton);
			sqlitePanel.add(browseClearPanel, gbc);
			
			JPanel mysqlPanel = new JPanel(new GridBagLayout());
			JTextField addressField = new JTextField();
			addressField.setColumns(30);
			JTextField databaseField = new JTextField();
			databaseField.setColumns(15);
			JTextField userField = new JTextField();
			userField.setColumns(15);
			JPasswordField passwordField = new JPasswordField();
			passwordField.setColumns(15);
			GridBagConstraints g = new GridBagConstraints();
			g.gridx = 0;
			g.gridy = 0;
			g.gridwidth = 1;
			g.anchor = GridBagConstraints.EAST;
			g.fill = GridBagConstraints.HORIZONTAL;
			g.insets = new Insets(15, 10, 3, 10);
			JLabel addressLabel = new JLabel("mysql://", JLabel.TRAILING);
			mysqlPanel.add(addressLabel, g);
			g.gridx = 1;
			g.gridwidth = 3;
			mysqlPanel.add(addressField, g);
			g.insets = new Insets(0, 10, 3, 0);
			g.gridwidth = 1;
			g.gridy = 1;
			g.gridx = 0;
			JLabel databaseLabel = new JLabel("Database:", JLabel.TRAILING);
			mysqlPanel.add(databaseLabel, g);
			g.gridx = 1;
			mysqlPanel.add(databaseField, g);
			g.gridy = 2;
			g.gridx = 0;
			JLabel userLabel = new JLabel("User:", JLabel.TRAILING);
			mysqlPanel.add(userLabel, g);
			g.gridx = 1;
			mysqlPanel.add(userField, g);
			g.gridx = 2;
			JLabel passwordLabel = new JLabel("Password:", JLabel.TRAILING);
			mysqlPanel.add(passwordLabel, g);
			g.gridx = 3;
			g.insets = new Insets(0, 10, 3, 10);
			mysqlPanel.add(passwordField, g);
			g.gridx = 0;
			g.gridy = 3;
			g.gridwidth = 4;
			g.insets = new Insets(15, 10, 3, 10);
			JButton checkButton = new JButton("Check", new ImageIcon(getClass().getResource("/icons/database_connect.png")));
			JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			checkPanel.add(checkButton);
			checkPanel.add(Box.createRigidArea(new Dimension(5,5)));
			JLabel checkLabel = new JLabel("");
			checkPanel.add(checkLabel);
			mysqlPanel.add(checkPanel, g);
			
			CardLayout databaseCardLayout = new CardLayout();
			JPanel cardPanel = new JPanel(databaseCardLayout);
			cardPanel.add(sqlitePanel, "sqlitePanel");
			cardPanel.add(mysqlPanel, "mysqlPanel");
			this.add(cardPanel, BorderLayout.CENTER);
			sqlite.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					databaseCardLayout.show(cardPanel, "sqlitePanel");
				}
			});
			mysql.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					databaseCardLayout.show(cardPanel, "mysqlPanel");
				}
			});
		}
	}
	
}
