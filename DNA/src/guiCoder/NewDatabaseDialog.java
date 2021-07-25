package guiCoder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.jasypt.util.password.StrongPasswordEncryptor;

/**
 * This class represents a dialog window for creating a new DNA database. The
 * user can choose between different kinds of databases, set the credentials,
 * and set an Admin coder password.
 */
@SuppressWarnings("serial")
public class NewDatabaseDialog extends JDialog {
	JButton saveButton;
	JRadioButton typeSqliteButton, typeMysqlButton, typePostgresqlButton;
	JTextField dbUrlField;
	JPasswordField pw1Field, pw2Field;
	ConnectionProfile cp = null;
	
	/**
	 * Get the connection profile created after pressing the save button.
	 * 
	 * @return Connection profile for the new database.
	 */
	public ConnectionProfile getConnectionProfile() {
		return cp;
	}
	
	/**
	 * Constructor that shows the dialog window.
	 */
	public NewDatabaseDialog(boolean openExistingDatabase) {

		this.setModal(true);
		if (openExistingDatabase == true) {
			this.setTitle("Connect to DNA database");
		} else {
			this.setTitle("Create new DNA database");
		}
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon tableDatabaseIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-database.png"));
		this.setIconImage(tableDatabaseIcon.getImage());
		this.setLayout(new BorderLayout());
		
		// upper panel for the database credentials and URL
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();

		g.fill = GridBagConstraints.HORIZONTAL;
		g.anchor = GridBagConstraints.WEST;
		g.gridwidth = 2;
		g.gridx = 1;
		g.gridy = 0;
		typeSqliteButton = new JRadioButton("SQLite (file-based)");
		typeMysqlButton = new JRadioButton("MySQL (remote database)");
		typePostgresqlButton = new JRadioButton("PostgreSQL (remote database)");
		ButtonGroup bg = new ButtonGroup();
		bg.add(typeSqliteButton);
		bg.add(typeMysqlButton);
		bg.add(typePostgresqlButton);
		panel.add(typeSqliteButton, g);
		g.gridy = 1;
		panel.add(typeMysqlButton, g);
		g.gridy = 2;
		panel.add(typePostgresqlButton, g);
		typeSqliteButton.setSelected(true);
		String ttSqlite = 
				"<html><p width=\"500\"><b>SQLite</b> is a file-based format. You can conveniently save "
				+ "your work in a file on your local computer and do not need to worry "
				+ "about setting up a remote database. SQLite is very responsive "
				+ "and fast and updates the graphical user interface immediately when a "
				+ "change happens in the database. Use SQLite if there is only "
				+ "one user. <it>Do not</it> save the database in a synchronized "
				+ "folder, such as Dropbox, because this may lead to data loss. "
				+ "Be sure to create backups frequently. When you select this option,"
				+ "a new file will be created; you do <it>not</it> need to have an empty database"
				+ "yet.</p></html>";
		String ttMysql =
				"<html><p width=\"500\"><b>MySQL</b> is a remote database that resides on a webserver, "
				+ "but you can also host the MySQL database on your local computer "
				+ "or network if you install MySQL locally. MySQL is designed for teams and collaborative "
				+ "coding with multiple users. DNA uses connection pooling, and "
				+ "coders can log in from different locations and collaborate, even simultaneously. "
				+ "A slight disadvantage is that changes made by other users are "
				+ "not instantly visible. The refresh interval determines how "
				+ "often the documents and statements are updated from the "
				+ "database (e.g., every 20 seconds), and it can be changed in the DNA user interface. "
				+ "IT departments and web hosting companies can "
				+ "provide empty MySQL databases to you. You can enter the URL, login, and password for "
				+ "the empty database here, and DNA can then create the data structures for you. "
				+ "Note that the login you provide must have user rights for table creation "
				+ "for this purpose.</p></html>";
		String ttPostgresql =
				"<html><p width=\"500\"><b>PostgreSQL</b> is another remote database, similar to MySQL. "
				+ "See the description of MySQL for details. Some providers offer "
				+ "MySQL while others offer PostgreSQL. Other popular choices are "
				+ "MS SQL Server, Oracle databases, and MariaDB, but these are "
				+ "presently not supported. The advantages and disadvantages of "
				+ "MySQL and PostgreSQL are largely identical, so just choose the "
				+ "database you can get access to. Like with MySQL, you provide the URL of "
				+ "and empty PostgreSQL database below, and your user rights must permit creating "
				+ "new tables, which DNA will do for you there to set up the DNA data structures. "
				+ "You can subsequently just connect to the database with more restrictive user "
				+ "rights (e.g., querying and writing into tables, but not creating tables anymore).</p></html>";
		typeSqliteButton.setToolTipText(ttSqlite);
		typeMysqlButton.setToolTipText(ttMysql);
		typePostgresqlButton.setToolTipText(ttPostgresql);

		g.gridwidth = 1;
		g.insets = new Insets(0, 5, 5, 5);
		g.gridx = 1;
		g.gridy = 3;
		dbUrlField = new JTextField(20);
		panel.add(dbUrlField, g);
		dbUrlField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkButton(openExistingDatabase);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkButton(openExistingDatabase);
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkButton(openExistingDatabase);
			}
		});
		String ttUrl =
				"<html><p width=\"500\">If SQLite, enter the file name of the new "
				+ "SQLite DNA database, ending with \".dna\". You can browse to select "
				+ "the path and file name. Do not select an existing file. If MySQL "
				+ "or PostgreSQL, enter the remote address (IP or server name with "
				+ "database name), without the mysql:// or postgresql:// prefix. "
				+ "This is an address for an existing but empty database. DNA will "
				+ "create the required table structure once you go ahead.</p></html>";
		dbUrlField.setToolTipText(ttUrl);
		
		
		g.gridx = 2;
		ImageIcon folderIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-folder.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton folderButton = new JButton("Browse...", folderIcon);
		folderButton.setToolTipText(ttUrl);
		panel.add(folderButton, g);
		folderButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String filename = null;
				JFileChooser fc = new JFileChooser();
				fc.setApproveButtonText("OK");
				fc.setDialogTitle("New database...");
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".dna") || f.isDirectory();
					}
					public String getDescription() {
						return "DNA SQLite database (*.dna)";
					}
				});
				int returnVal = fc.showOpenDialog(null);
				
				// extract chosen file name and check its validity
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if ((!openExistingDatabase && !file.exists()) || (openExistingDatabase && file.exists())) {
						filename = new String(file.getPath());
						if (!filename.endsWith(".dna")) {
							filename = filename + ".dna";
						}
						dbUrlField.setText(filename);
					} else {
						dbUrlField.setText("");
						if (openExistingDatabase) {
							JOptionPane.showMessageDialog(null,
								    "The file does not exist. Please choose a new file.",
								    "Error",
								    JOptionPane.ERROR_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(null,
								    "The file already exists and will not be overwritten.\nPlease choose a new file.",
								    "Error",
								    JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			
		});

		g.gridwidth = 2;
		g.gridx = 1;
		g.gridy = 4;
		JTextField dbUserField = new JTextField();
		dbUserField.setEnabled(false);
		panel.add(dbUserField, g);
		String ttUser =
				"<html><p width=\"500\">Enter the user name (= login) for the remote "
				+ "database. This must be a login with user permissions for creating "
				+ "tables because this is done in the next step. Once the tables have "
				+ "been created, you can close the connection and log in again with "
				+ "login details with read and write access but without table creation "
				+ "user rights, for peace of mind. A user name cannot be set for SQLite "
				+ "databases because these reside on a local hard drive in a file.</p></html>";
		dbUserField.setToolTipText(ttUser);

		g.gridx = 1;
		g.gridy = 5;
		JPasswordField dbPasswordField = new JPasswordField();
		dbPasswordField.setEnabled(false);
		panel.add(dbPasswordField, g);
		String ttPassword =
				"<html><p width=\"500\">Enter the password corresponding to the database "
				+ "login provided above. Note that this is the database password, not the "
				+ "Admin coder password, which is defined below. The database password is "
				+ "used to establish the remote connection to the database as such and has "
				+ "nothing to do with DNA and coder management per se. A database password "
				+ "is not required (nor possible) for SQLite databases.</p></html>";
		dbPasswordField.setToolTipText(ttPassword);

		g.gridwidth = 1;
		g.insets = new Insets(0, 5, 0, 0);
		g.gridx = 0;
		g.gridy = 0;
		JLabel dbTypeLabel = new JLabel("Database type", JLabel.RIGHT);
		dbTypeLabel.setLabelFor(typeSqliteButton);
		String ttType =
				"<html><p width=\"500\">DNA offers a choice of three database types for storing data. "
				+ "Look at the tooltips for the three formats for more information.</p></html>";
		dbTypeLabel.setToolTipText(ttType);
		panel.add(dbTypeLabel, g);

		g.insets = new Insets(0, 5, 5, 0);
		g.gridx = 0;
		g.gridy = 3;
		JLabel dbUrlLabel = new JLabel("File name", JLabel.RIGHT);
		dbUrlLabel.setLabelFor(dbUrlField);
		dbUrlLabel.setToolTipText(ttUrl);
		panel.add(dbUrlLabel, g);
		
		g.gridx = 0;
		g.gridy = 4;
		JLabel dbUserLabel = new JLabel("Database login", JLabel.RIGHT);
		dbUserLabel.setLabelFor(dbUserField);
		dbUserLabel.setToolTipText(ttUser);
		dbUserLabel.setEnabled(false);
		panel.add(dbUserLabel, g);

		g.gridx = 0;
		g.gridy = 5;
		JLabel dbPasswordLabel = new JLabel("Database password", JLabel.RIGHT);
		dbPasswordLabel.setLabelFor(dbPasswordField);
		dbPasswordLabel.setToolTipText(ttPassword);
		dbPasswordLabel.setEnabled(false);
		panel.add(dbPasswordLabel, g);

		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == typeSqliteButton) {
					dbUrlLabel.setText("File name");
					dbUserLabel.setEnabled(false);
					dbUserField.setEnabled(false);
					dbPasswordLabel.setEnabled(false);
					dbPasswordField.setEnabled(false);
					folderButton.setEnabled(true);
				} else if (e.getSource() == typeMysqlButton) {
					dbUrlLabel.setText("URL:   mysql://");
					dbUserLabel.setEnabled(true);
					dbUserField.setEnabled(true);
					dbPasswordLabel.setEnabled(true);
					dbPasswordField.setEnabled(true);
					folderButton.setEnabled(false);
				} else if (e.getSource() == typePostgresqlButton) {
					dbUrlLabel.setText("URL:   postgresql://");
					dbUserLabel.setEnabled(true);
					dbUserField.setEnabled(true);
					dbPasswordLabel.setEnabled(true);
					dbPasswordField.setEnabled(true);
					folderButton.setEnabled(false);
				} else {
					System.err.println("Database type not recognized.");
				}
				checkButton(openExistingDatabase);
			}
		};
		typeSqliteButton.addActionListener(al);
		typeMysqlButton.addActionListener(al);
		typePostgresqlButton.addActionListener(al);
		
		CompoundBorder border;
		border = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Database settings"));
		panel.setBorder(border);
		this.add(panel, BorderLayout.NORTH);
		
		// lower panel for setting the Admin coder password
		if (openExistingDatabase == false) {
			JPanel coderPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(5, 5, 5, 0);
			gbc.gridx = 0;
			gbc.gridy = 0;
			JLabel pw1 = new JLabel("Admin password", JLabel.RIGHT);
			coderPanel.add(pw1, gbc);
			gbc.gridx = 1;
			pw1Field = new JPasswordField();
			pw1.setLabelFor(pw1Field);
			coderPanel.add(pw1Field, gbc);
			
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = new Insets(0, 5, 5, 0);
			JLabel pw2 = new JLabel("Confirm password", JLabel.RIGHT);
			coderPanel.add(pw2, gbc);
			gbc.gridx = 1;
			pw2Field = new JPasswordField();
			pw2.setLabelFor(pw2Field);
			coderPanel.add(pw2Field, gbc);
			
			DocumentListener pwListener = new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkButton(openExistingDatabase);
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkButton(openExistingDatabase);
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkButton(openExistingDatabase);
				}
			};
			pw1Field.getDocument().addDocumentListener(pwListener);
			pw2Field.getDocument().addDocumentListener(pwListener);
			
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridwidth = 2;
			gbc.insets = new Insets(10, 0, 0, 0);
			JLabel coderInstructions = new JLabel("Keep the Admin password in a safe place. If you lose it, you lose database access.");
			coderPanel.add(coderInstructions, gbc);
			
			CompoundBorder borderCoder;
			borderCoder = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Set Admin coder password"));
			coderPanel.setBorder(borderCoder);
			String ttCoder =
					"<html><p width=\"500\">You must set (and confirm) a password for "
					+ "the Admin coder. You can log in as Admin and then create additional "
					+ "users with fewer user rights, who do the actual coding. If you "
					+ "code alone, you can do the coding as Admin, but it is recommended "
					+ "to code as a user to prevent accidental damage. Each coder has their "
					+ "own password. If the Admin password is lost, it cannot be recovered. "
					+ "It is therefore very important to store it safely. If a non-Admin "
					+ "password is lost, you can log in as Admin and reset that coder's "
					+ "password. You cannot reset the Admin password without knowing the "
					+ "Admin password. You can also provide empty (zero-length) passwords, "
					+ "in which case you will not be asked to confirm your password when "
					+ "you connect to the DNA database. But without password, you will not "
					+ "be able to save a connection profile because your password would "
					+ "be needed to encrypt the database credentials on your hard disk. So "
					+ "it is probably better to use a password unless you are using SQLite "
					+ "and don't need a connection profile anyway.</p></html>";
			coderPanel.setToolTipText(ttCoder);
			pw1Field.setToolTipText(ttCoder);
			pw2Field.setToolTipText(ttCoder);
			this.add(coderPanel, BorderLayout.CENTER);
		}
		
		// button panel at the bottom of the dialog
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		ImageIcon clearIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-backspace.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton clearButton = new JButton("Clear form", clearIcon);
		buttonPanel.add(clearButton);
		ImageIcon cancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton cancelButton = new JButton("Cancel", cancelIcon);
		buttonPanel.add(cancelButton);
		ImageIcon saveIcon;
		if (openExistingDatabase == true) {
			saveIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
			saveButton = new JButton("Connect", saveIcon);
		} else {
			saveIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-device-floppy.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
			saveButton = new JButton("Create data structures...", saveIcon);
		}
		saveButton.setEnabled(false);
		buttonPanel.add(saveButton);
		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == clearButton) {
					dbUrlLabel.setText("File name");
					typeSqliteButton.setSelected(true);
					dbUserField.setText("");
					dbPasswordField.setText("");
					dbUrlField.setText("");
					dbUserLabel.setEnabled(false);
					dbUserField.setEnabled(false);
					dbPasswordLabel.setEnabled(false);
					dbPasswordField.setEnabled(false);
					folderButton.setEnabled(true);
					if (openExistingDatabase == false) {
						pw1Field.setText("");
						pw2Field.setText("");
					}
				} else if (e.getSource() == cancelButton) {
					dispose();
				} else if (e.getSource() == saveButton) {
					String type = null;
					if (typeSqliteButton.isSelected()) {
						type = "sqlite";
					} else if (typeMysqlButton.isSelected()) {
						type = "mysql";
					} else if (typePostgresqlButton.isSelected()) {
						type = "postgresql";
					}
					
					// create connection profile with the details provided
					ConnectionProfile tempConnectionProfile = new ConnectionProfile(type, dbUrlField.getText(), dbUserField.getText(), new String(dbPasswordField.getPassword()));
					sql.Sql testConnection = new sql.Sql(tempConnectionProfile);
					
					if (openExistingDatabase == true) { // existing database: select and authenticate user, then open connection as main database in DNA
						boolean validInput = false;
						while (!validInput) {
							CoderPasswordCheckDialog cpcd = new CoderPasswordCheckDialog(testConnection, true);
							Coder coder = cpcd.getCoder();
							if (coder == null) { // user must have pressed cancel
								validInput = true;
							} else {
								tempConnectionProfile.setCoder(coder.getId());
								testConnection.getConnectionProfile().setCoder(coder.getId());
								String password = cpcd.getPassword();
								if (coder != null && password != null) {
									boolean authenticated = testConnection.authenticate(password);
									if (authenticated == true) {
										validInput = true; // password check passed; quit while-loop
										cp = tempConnectionProfile;
										dispose();
									} else {
			    						System.err.println("Authentication failed. Check your password.");
					    				JOptionPane.showMessageDialog(null,
					    						"Authentication failed. Check your password.",
					    					    "Check failed",
					    					    JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
					} else { // new database: digest password, test database, and create data structures
						// generate hash from password
						String plainPassword = new String(pw1Field.getPassword()); // this must be the coder password, not the database password!
						StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
						String encryptedPassword = passwordEncryptor.encryptPassword(plainPassword);
						
						// test connection and create tables
						boolean connectionValid = true;
						if (type.equals("mysql") || type.equals("postgresql")) {
			    			try (Connection conn = testConnection.getDataSource().getConnection();
			    					PreparedStatement ps = conn.prepareStatement("SELECT 1;")) {
			    				ResultSet rs = ps.executeQuery();
			    				while (rs.next()) {
			    					if (rs.getInt(1) != 1) {
			    						connectionValid = false;
			    						System.err.println("Connection test failed with a simple database query.");
					    				JOptionPane.showMessageDialog(null,
					    					    "Connection test failed. Please check your database.",
					    					    "Check failed",
					    					    JOptionPane.ERROR_MESSAGE);
			    					}
			    				}
			    			} catch (SQLException e1) {
	    						connectionValid = false;
			    				System.err.println("Could not establish connection to remote database. Please check URL, login, and password.");
			    				JOptionPane.showMessageDialog(null,
			    					    "Could not establish connection to remote database.\nPlease check URL, login, and password.",
			    					    "Check failed",
			    					    JOptionPane.ERROR_MESSAGE);
			    				e1.printStackTrace();
			    			}
			    			if (connectionValid == true) {
								connectionValid = testConnection.createTables(encryptedPassword);
								if (connectionValid == false) {
									System.err.println("Could not create tables in the database.");
				    				JOptionPane.showMessageDialog(null,
				    					    "Could not create tables in the database.",
				    					    "Operation failed",
				    					    JOptionPane.ERROR_MESSAGE);
				    			}
			    			}
			    		} else if (type.equals("sqlite")) {
			    			connectionValid = testConnection.createTables(encryptedPassword);
			    			if (connectionValid == false) {
								System.err.println("Could not create tables in the database.");
			    				JOptionPane.showMessageDialog(null,
			    					    "Could not create tables in the database.",
			    					    "Operation failed",
			    					    JOptionPane.ERROR_MESSAGE);
			    			}
			    		}
						
						// save the connection profile for retrieval from parent class via getConnectionProfile() method
						if (connectionValid == true) {
							cp = tempConnectionProfile;
							JOptionPane.showMessageDialog(null,
								    "Data structures were set up in:\n" + new File(dbUrlField.getText()).getAbsolutePath(),
								    "Success",
								    JOptionPane.PLAIN_MESSAGE);
							dispose();
						}
					}
				}
			}
		};
		clearButton.addActionListener(buttonListener);
		cancelButton.addActionListener(buttonListener);
		saveButton.addActionListener(buttonListener);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * This function is called by action listeners and document change listeners
	 * to determine if the button for creating the tables and data structures
	 * should be enabled and what fields should be colored in red as a hint on
	 * what part of the input is invalid. Various validity checks are performed.
	 */
	public void checkButton(boolean openExistingDatabase) {
		boolean valid = true;
		if (typeSqliteButton.isSelected()) {
			File f = new File(dbUrlField.getText());
			if ((!openExistingDatabase && f.exists()) || (openExistingDatabase && !f.exists())) { // only a file that does not exist yet is valid
				valid = false;
			}
			try { // check if the path of the file is valid or has weird characters (not working on Linux)
                Paths.get(dbUrlField.getText());
            } catch (InvalidPathException ex) {
                valid = false;
            }
			try { // another path check that does not work on Linux
				f.getCanonicalPath();
			} catch (IOException e) {
				valid = false;
			}
			if (!dbUrlField.getText().endsWith(".dna")) {
				valid = false;
			}
		} else {
			if (dbUrlField.getText().startsWith("mysql://")) {
				dbUrlField.setText(dbUrlField.getText().substring(8, dbUrlField.getText().length()));
			}
			if (dbUrlField.getText().startsWith("postgresql://")) {
				dbUrlField.setText(dbUrlField.getText().substring(13, dbUrlField.getText().length()));
			}
			if (dbUrlField.getText().endsWith(".dna")) {
				valid = false;
			}
		}
		if (dbUrlField.getText().equals("")) {
			valid = false;
		}
		if (valid == true) {
			dbUrlField.setForeground(Color.BLACK);
		} else {
			dbUrlField.setForeground(Color.RED);
		}
		if (openExistingDatabase == false) {
			String p1 = new String(pw1Field.getPassword());
			String p2 = new String(pw2Field.getPassword());
			if (p1.equals("") || p2.equals("") || !p1.equals(p2)) {
				valid = false;
				pw1Field.setForeground(Color.RED);
				pw2Field.setForeground(Color.RED);
			} else {
				pw1Field.setForeground(Color.BLACK);
				pw2Field.setForeground(Color.BLACK);
			}
		}
		if (valid == true) {
			saveButton.setEnabled(true);
		} else {
			saveButton.setEnabled(false);
		}
	}
}