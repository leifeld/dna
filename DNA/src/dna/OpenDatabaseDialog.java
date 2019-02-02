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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class OpenDatabaseDialog extends JDialog {
	JTextField checkField;
	
	/**
	 * Constructor without GUI for loading data from an SQLite database.
	 * 
	 * @param dbfile     File name with path.
	 */
	public OpenDatabaseDialog(String dbfile) {
		Dna.dna.sql = new SqlConnection("sqlite", dbfile, "", "");
		loadDataAndDispose();
	}
	
	/**
	 * Constructor without GUI for loading data from a mySQL database.
	 * 
	 * @param dbfile     mySQL URL (without "jdbc:mysql://").
	 * @param login      User name for the database access.
	 * @param password   Password for accessing the database.
	 */
	public OpenDatabaseDialog(String dbfile, String login, String password) {
		Dna.dna.sql = new SqlConnection("mysql", dbfile, login, password);
		loadDataAndDispose();
	}
	
	/**
	 * Constructor with GUI
	 */
	public OpenDatabaseDialog() {
		
		this.setTitle("Open existing database...");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon icon = new ImageIcon(getClass().getResource("/icons/database.png"));
		this.setIconImage(icon.getImage());
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
						return f.getName().toLowerCase().endsWith(".dna") || f.isDirectory();
					}
					public String getDescription() {
						return "Discourse Network Analyzer database (*.dna)";
					}
				});

				int returnVal = fc.showOpenDialog(dna.OpenDatabaseDialog.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (file.exists()) {
						String filename = new String(file.getPath());
						if (!filename.endsWith(".dna")) {
							filename = filename + ".dna";
						}
						fileField.setText(filename);
					} else {
						JOptionPane.showMessageDialog(Dna.gui, "The file name you entered does not exist. Please choose a new file.");
						browseButton.doClick();
					}
				}
			}
		});
		fileField.setPreferredSize(new Dimension(300, (int) browseButton.getPreferredSize().getHeight()));
		
		JButton sqliteClearButton = new JButton("Clear", new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png")));
		sqliteClearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileField.setText("(No file selected)");
			}
		});
		JButton applyButton = new JButton("Open", new ImageIcon(getClass().getResource("/icons/accept.png")));
		applyButton.setEnabled(false);
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileField.getText().endsWith(".dna")) {
					Dna.dna.sql = new SqlConnection("sqlite", fileField.getText(), "", "");
					loadDataAndDispose();
				}
			}
		});
		fileField.getDocument().addDocumentListener(new DocumentListener() {
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
				if (fileField.getText().endsWith(".dna")) {
					applyButton.setEnabled(true);
				} else {
					applyButton.setEnabled(false);
				}
			}
		});
		JPanel browseClearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
		browseClearPanel.add(browseButton);
		browseClearPanel.add(Box.createRigidArea(new Dimension(5,5)));
		browseClearPanel.add(sqliteClearButton);
		browseClearPanel.add(Box.createRigidArea(new Dimension(5,5)));
		browseClearPanel.add(applyButton);
		sqlitePanel.add(browseClearPanel, gbc);
		
		JPanel mysqlPanel = new JPanel(new GridBagLayout());
		JTextField addressField = new JTextField();
		addressField.setColumns(30);
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
		g.gridy = 2;
		g.gridwidth = 4;
		g.insets = new Insets(15, 10, 3, 10);
		JButton applySqlButton = new JButton("Open", new ImageIcon(getClass().getResource("/icons/accept.png")));
		applySqlButton.setEnabled(false);
		applySqlButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Dna.dna.sql = new SqlConnection("mysql", addressField.getText(), userField.getText(), 
						String.copyValueOf(passwordField.getPassword()));
				loadDataAndDispose();
			}
		});
		JButton mysqlClearButton = new JButton("Clear", new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png")));
		JButton checkButton = new JButton("Check", new ImageIcon(getClass().getResource("/icons/database_connect.png")));
		JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
		checkPanel.add(checkButton);
		checkPanel.add(Box.createRigidArea(new Dimension(5,5)));
		checkPanel.add(mysqlClearButton);
		checkPanel.add(Box.createRigidArea(new Dimension(5,5)));
		checkPanel.add(applySqlButton);
		mysqlPanel.add(checkPanel, g);
		checkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (addressField.getText().startsWith("mysql://")) {
					addressField.setText(addressField.getText().substring(8, addressField.getText().length()));
				}
				SqlConnection testConnection = new SqlConnection("mysql", addressField.getText(), 
						userField.getText(), String.copyValueOf(passwordField.getPassword()));
				checkField.setText(testConnection.testExistingMySQLConnection());
				testConnection.closeConnection();
				if (checkField.getText().equals("OK. The database contains all necessary tables.")) {
					applySqlButton.setEnabled(true);
				} else {
					applySqlButton.setEnabled(false);
				}
			}
		});
		mysqlClearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkField.setText("");
				applySqlButton.setEnabled(false);
				addressField.setText("");
				userField.setText("");
				passwordField.setText("");
			}
		});
		
		g.gridy = 2;
		mysqlPanel.add(Box.createRigidArea(new Dimension(5,5)));
		g.gridy = 3;
		checkField = new JTextField("");
		checkField.setBackground(mysqlPanel.getBackground());
		checkField.setEditable(false);
		mysqlPanel.add(checkField, g);
		
		
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
		
		// set location and pack window
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setResizable(false);
	}
	
	public void loadDataAndDispose() {
		Dna.data = Dna.dna.sql.getAllData();
		Dna.gui.rightPanel.rm.regexListModel.updateList();
		Dna.gui.rightPanel.rm.setFieldsEnabled(true);
		Dna.gui.leftPanel.docStats.refreshButton.setEnabled(true);
		Dna.gui.statusBar.resetLabel();
		Dna.gui.menuBar.closeDatabase.setEnabled(true);
		Dna.gui.menuBar.openDatabase.setEnabled(false);
		Dna.gui.menuBar.newDatabase.setEnabled(false);
		Dna.gui.menuBar.newDocumentButton.setEnabled(true);
		Dna.gui.menuBar.networkButton.setEnabled(true);
		Dna.gui.menuBar.colorStatementTypeButton.setEnabled(true);
		Dna.gui.menuBar.colorCoderButton.setEnabled(true);
		if (Dna.data.getSettings().get("statementColor").equals("statementType")) {
			Dna.gui.menuBar.colorStatementTypeButton.setSelected(true);
			Dna.gui.menuBar.colorCoderButton.setSelected(false);
		} else {
			Dna.gui.menuBar.colorCoderButton.setSelected(true);
			Dna.gui.menuBar.colorStatementTypeButton.setSelected(false);
		}
		int ac = Dna.data.getActiveCoder();
		Dna.gui.leftPanel.setComboEnabled(true);
		
		if (Dna.gui.documentPanel.documentTable.getRowCount() > 0) {
			Dna.gui.documentPanel.documentTable.setRowSelectionInterval(0, 0);
		}
		Dna.gui.documentPanel.documentFilter();
		
		Dna.dna.sql.upsertSetting("version", Dna.dna.version);
		Dna.dna.sql.upsertSetting("date", Dna.dna.date);
		if (!Dna.data.getSettings().containsKey("popupWidth")) {
			Dna.data.getSettings().put("popupWidth", "220");
			Dna.dna.sql.upsertSetting("popupWidth", "220");
		}
		Dna.gui.popupWidthModel.setValue(Integer.parseInt(Dna.data.getSettings().get("popupWidth")));
		
		//Dna.dna.gui.leftPanel.coderPanel.coderBox.setSelectedItem(Dna.data.getCoderById(ac));
		Dna.gui.rightPanel.statementPanel.typeComboBox.setSelectedIndex(0);
		Dna.gui.rightPanel.statementPanel.statementFilter.showAll.doClick();
		Dna.gui.leftPanel.coderPanel.model.setSelectedItem(Dna.data.getCoderById(ac));
		
		Dna.gui.refreshGui();
		
		dispose();
	}
}