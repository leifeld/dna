package gui;

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

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import sql.ConnectionProfile;

/**
 * This class represents a dialog window for creating a new DNA database. The
 * user can choose between different kinds of databases, set the credentials,
 * and set an Admin coder password.
 */
@SuppressWarnings("serial")
public class NewDatabaseDialog extends JDialog {
	boolean openExistingDatabase;
	ConnectionDetailsPanel connectionDetailsPanel;
	JButton saveButton;
	JRadioButton typeSqliteButton, typeMysqlButton, typePostgresqlButton;
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
		this.openExistingDatabase = openExistingDatabase;

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

		// connection details panel (CENTER)
		connectionDetailsPanel = new ConnectionDetailsPanel();
		this.add(connectionDetailsPanel, BorderLayout.CENTER);
		
		// panel with radio buttons for selecting database type (NORTH)
		JPanel typePanel = new JPanel(new GridBagLayout());
		typeSqliteButton = new JRadioButton("SQLite (file-based)");
		typeMysqlButton = new JRadioButton("MySQL (remote database)");
		typePostgresqlButton = new JRadioButton("PostgreSQL (remote database)");
		ButtonGroup bg = new ButtonGroup();
		bg.add(typeSqliteButton);
		bg.add(typeMysqlButton);
		bg.add(typePostgresqlButton);
		typeSqliteButton.setSelected(true);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1;
		gbc.insets = new Insets(5, 5, 0, 0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		typePanel.add(typeSqliteButton, gbc);
		gbc.gridy = 1;
		typePanel.add(typeMysqlButton, gbc);
		gbc.gridy = 2;
		typePanel.add(typePostgresqlButton, gbc);
		
		CompoundBorder borderType;
		borderType = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Database type"));
		typePanel.setBorder(borderType);
		
		String ttType =
				"<html><p width=\"500\">DNA offers a choice of three database types for storing data. "
				+ "Look at the tooltips for the three formats for more information.</p></html>";
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
		typePanel.setToolTipText(ttType);

		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == typeSqliteButton) {
					connectionDetailsPanel.setConnectionType("sqlite");
				} else if (e.getSource() == typeMysqlButton) {
					connectionDetailsPanel.setConnectionType("mysql");
				} else if (e.getSource() == typePostgresqlButton) {
					connectionDetailsPanel.setConnectionType("postgresql");
				}
				checkButton();
			}
		};
		typeSqliteButton.addActionListener(al);
		typeMysqlButton.addActionListener(al);
		typePostgresqlButton.addActionListener(al);
		
		this.add(typePanel, BorderLayout.NORTH);
		
		JPanel southPanel = new JPanel(new BorderLayout());
		
		// lower panel for setting the Admin coder password
		if (this.openExistingDatabase == false) {
			JPanel coderPanel = new JPanel(new GridBagLayout());
			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 0;
			gbc.insets = new Insets(5, 5, 0, 0);
			gbc.gridx = 0;
			gbc.gridy = 0;
			JLabel pw1 = new JLabel("Admin password", JLabel.RIGHT);
			coderPanel.add(pw1, gbc);
			gbc.weightx = 1;
			gbc.gridx = 1;
			pw1Field = new JPasswordField();
			pw1.setLabelFor(pw1Field);
			coderPanel.add(pw1Field, gbc);

			gbc.weightx = 0;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = new Insets(0, 5, 5, 0);
			JLabel pw2 = new JLabel("Confirm password", JLabel.RIGHT);
			coderPanel.add(pw2, gbc);
			gbc.weightx = 1;
			gbc.gridx = 1;
			pw2Field = new JPasswordField();
			pw2.setLabelFor(pw2Field);
			coderPanel.add(pw2Field, gbc);
			
			DocumentListener pwListener = new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkButton();
				}
			};
			pw1Field.getDocument().addDocumentListener(pwListener);
			pw2Field.getDocument().addDocumentListener(pwListener);
			
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridwidth = 2;
			gbc.insets = new Insets(10, 5, 5, 0);
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
			southPanel.add(coderPanel, BorderLayout.CENTER);
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
					typeSqliteButton.setSelected(true);
					connectionDetailsPanel.setConnectionType("sqlite");
					connectionDetailsPanel.clear();
					if (openExistingDatabase == false) {
						pw1Field.setText("");
						pw2Field.setText("");
					}
				} else if (e.getSource() == cancelButton) {
					LogEvent l = new LogEvent(Logger.MESSAGE,
							"[GUI] Canceled opening a database or creating a new database.",
							"Closed the New Database dialog window.");
					Dna.logger.log(l);
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
					String url = connectionDetailsPanel.getUrl()
							.replaceAll("^jdbc:mysql://", "")
							.replaceAll("^mysql://", "")
							.replaceAll("^jdbc:postgresql://", "")
							.replaceAll("^postgresql://", "");
					ConnectionProfile tempConnectionProfile = new ConnectionProfile(
							type,
							url,
							connectionDetailsPanel.getDatabaseName(),
							connectionDetailsPanel.getPort(),
							connectionDetailsPanel.getLogin(),
							connectionDetailsPanel.getPassword());
					sql.Sql testConnection = new sql.Sql(tempConnectionProfile, true); // connection test, so true
					
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
			    						LogEvent l = new LogEvent(Logger.WARNING,
			    								"Authentication failed. Check your password.",
			    								"Tried to open database, but a wrong password was entered for Coder " + coder.getId() + ".");
			    						Dna.logger.log(l);
					    				JOptionPane.showMessageDialog(null,
					    						"Authentication failed. Check your password.",
					    					    "Check failed",
					    					    JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
						LogEvent l = new LogEvent(Logger.MESSAGE,
								"[GUI] Opened database using a dialog window.",
								"Opened a database from the GUI using a dialog window.");
						Dna.logger.log(l);
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
			    						LogEvent l = new LogEvent(Logger.ERROR,
			    								"[SQL] Connection test failed with a simple database query.",
			    								"Connection test failed with a simple database query while trying to create data structure in new DNA database.");
			    						Dna.logger.log(l);
					    				JOptionPane.showMessageDialog(null,
					    					    "Connection test failed. Please check your database.",
					    					    "Check failed",
					    					    JOptionPane.ERROR_MESSAGE);
			    					}
			    				}
			    			} catch (SQLException e1) {
	    						connectionValid = false;
	    						LogEvent l = new LogEvent(Logger.ERROR,
	    								"[SQL] Could not establish connection to remote database.",
	    								"Could not establish connection to remote database. Please check URL, login, and password.",
	    								e1);
	    						Dna.logger.log(l);
			    				JOptionPane.showMessageDialog(null,
			    					    "Could not establish connection to remote database.\nPlease check URL, login, and password.",
			    					    "Check failed",
			    					    JOptionPane.ERROR_MESSAGE);
			    			}
			    			if (connectionValid == true) {
								connectionValid = testConnection.createTables(encryptedPassword);
								if (connectionValid == false) {
		    						LogEvent l = new LogEvent(Logger.ERROR,
		    								"[SQL] Failed to create tables in the database.",
		    								"Failed to create tables in the database.");
		    						Dna.logger.log(l);
				    				JOptionPane.showMessageDialog(null,
				    					    "Failed to create tables in the database.",
				    					    "Operation failed",
				    					    JOptionPane.ERROR_MESSAGE);
				    			}
			    			}
			    		} else if (type.equals("sqlite")) {
			    			connectionValid = testConnection.createTables(encryptedPassword);
			    			if (connectionValid == false) {
	    						LogEvent l = new LogEvent(Logger.ERROR,
	    								"[SQL] Failed to create tables in the database.",
	    								"Failed to create tables in the database.");
	    						Dna.logger.log(l);
			    				JOptionPane.showMessageDialog(null,
			    					    "Could not create tables in the database.",
			    					    "Operation failed",
			    					    JOptionPane.ERROR_MESSAGE);
			    			}
			    		}
						
						// save the connection profile for retrieval from parent class via getConnectionProfile() method
						if (connectionValid == true) {
							cp = tempConnectionProfile;
    						LogEvent l = new LogEvent(Logger.MESSAGE,
    								"[GUI] Data structures were set up in SQLite database.",
    								"Data structures were set up in: " + new File(connectionDetailsPanel.getUrl()).getAbsolutePath());
    						Dna.logger.log(l);
							JOptionPane.showMessageDialog(null,
								    "Data structures were set up in:\n" + new File(connectionDetailsPanel.getUrl()).getAbsolutePath(),
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
		southPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.add(southPanel, BorderLayout.SOUTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Connection details panel for the database credentials and URL
	 */
	private class ConnectionDetailsPanel extends JPanel {
		
		/**
		 * A String containing the connection type. This can be one of the
		 * following: {\code "sqlite"}; {\code "mysql"}; {\code "postgresql"}.
		 */
		private String connectionType;
		
		private JTextField urlField, databaseNameField, portField, loginField;
		private JLabel urlLabel, databaseNameLabel, portLabel, loginLabel, passwordLabel;
		private JPasswordField passwordField;
		private JButton folderButton;
		private String ttUrl;
		
		/**
		 * Create a new panel with fields for the connection details.
		 */
		ConnectionDetailsPanel() {
			this.connectionType = "sqlite";
			setLayout(new GridBagLayout());
			GridBagConstraints g = new GridBagConstraints();

			// URL label
			g.fill = GridBagConstraints.HORIZONTAL;
			g.anchor = GridBagConstraints.WEST;
			g.insets = new Insets(5, 5, 0, 0);
			g.gridx = 0;
			g.gridy = 0;
			urlLabel = new JLabel("File name", JLabel.RIGHT);
			urlLabel.setLabelFor(urlField);
			this.add(urlLabel, g);
			
			// URL field
			g.gridwidth = 3;
			g.gridx = 1;
			urlField = new JTextField(10);
			urlField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkButton();
				}
			});
			this.add(urlField, g);
			
			// folder browse button
			g.gridwidth = 1;
			g.gridx = 4;
			ImageIcon folderIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-folder.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
			folderButton = new JButton("Browse...", folderIcon);
			folderButton.setToolTipText(ttUrl);
			this.add(folderButton, g);
			folderButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String filename = null;
					JFileChooser fc = new JFileChooser();
					fc.setApproveButtonText("OK");
					if (openExistingDatabase == true) {
						fc.setDialogTitle("Select database...");
					} else {
						fc.setDialogTitle("New database...");
					}
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
							urlField.setText(filename);
						} else {
							urlField.setText("");
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
			
			// database name field
			g.gridx = 0;
			g.gridy = 1;
			databaseNameLabel = new JLabel("Database name", JLabel.RIGHT);
			databaseNameField = new JTextField(10);
			databaseNameLabel.setLabelFor(databaseNameField);
			String ttDatabaseName = "<html><p width=\"500\">Enter the name of the remote database "
					+ "at the address you specified.</p></html>";
			databaseNameField.setToolTipText(ttDatabaseName);
			databaseNameLabel.setToolTipText(ttDatabaseName);
			this.add(databaseNameLabel, g);
			g.gridx = 1;
			this.add(databaseNameField, g);

			// port field
			g.gridx = 2;
			portLabel = new JLabel("Port", JLabel.RIGHT);
			portField = new JTextField(10);
			portField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkButton();
				}
			});
			
			portLabel.setLabelFor(portField);
			String ttPort = "<html><p width=\"500\">The port at which a connection should be established "
					+ "on the server. The default port for MySQL is 3306. The default port for PostgreSQL is "
					+ "5432. But your server settings may be different.</p></html>";
			portField.setToolTipText(ttPort);
			portLabel.setToolTipText(ttPort);
			this.add(portLabel, g);
			g.gridx = 3;
			this.add(portField, g);

			// user name field
			g.gridx = 0;
			g.gridy = 2;
			loginLabel = new JLabel("Database login", JLabel.RIGHT);
			loginField = new JTextField(10);
			loginField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkButton();
				}
			});
			loginLabel.setLabelFor(loginField);
			String ttLogin =
					"<html><p width=\"500\">Enter the user name (= login) for the remote "
							+ "database. This must be a login with user permissions for creating "
							+ "tables because this is done in the next step. Once the tables have "
							+ "been created, you can close the connection and log in again with "
							+ "login details with read and write access but without table creation "
							+ "user rights, for peace of mind.</p></html>";
			loginField.setToolTipText(ttLogin);
			loginLabel.setToolTipText(ttLogin);
			this.add(loginLabel, g);
			g.gridx = 1;
			this.add(loginField, g);

			// password field
			g.gridx = 2;
			passwordLabel = new JLabel("Database password", JLabel.RIGHT);
			passwordField = new JPasswordField();
			passwordLabel.setLabelFor(passwordField);
			String ttPassword =
					"<html><p width=\"500\">Enter the password corresponding to the database "
							+ "login provided above. Note that this is the database password, not the "
							+ "Admin coder password, which is defined below. The database password is "
							+ "used to establish the remote connection to the database as such and has "
							+ "nothing to do with DNA and coder management per se.</p></html>";
			passwordField.setToolTipText(ttPassword);
			passwordLabel.setToolTipText(ttPassword);
			this.add(passwordLabel, g);
			g.gridx = 3;
			this.add(passwordField, g);
			
			CompoundBorder border;
			border = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Database settings"));
			this.setBorder(border);
			
			this.setConnectionType("sqlite");
		}
		
		/**
		 * Set the connection type and adjust all fields.
		 * 
		 * @param connectionType The connection type. Valid values are
		 *   {@code "sqlite"}, {@code "mysql"}, and {@code "postgresql"}.
		 */
		void setConnectionType(String connectionType) {
			String tempUrl = getUrl();
			if (this.connectionType == null) {
				// do nothing here
			} else if (connectionType.equals("sqlite")) {
				this.connectionType = connectionType;
				this.urlField.setText(tempUrl);
				this.folderButton.setEnabled(true);
				this.databaseNameField.setText("");
				this.loginField.setText("");
				this.passwordField.setText("");
				this.portField.setText("");
				this.databaseNameLabel.setEnabled(false);
				this.databaseNameField.setEnabled(false);
				this.portLabel.setEnabled(false);
				this.portField.setEnabled(false);
				this.loginLabel.setEnabled(false);
				this.loginField.setEnabled(false);
				this.passwordLabel.setEnabled(false);
				this.passwordField.setEnabled(false);
				this.urlLabel.setText("File name");
			} else {
				this.folderButton.setEnabled(false);
				this.databaseNameLabel.setEnabled(true);
				this.databaseNameField.setEnabled(true);
				this.portLabel.setEnabled(true);
				this.portField.setEnabled(true);
				this.loginLabel.setEnabled(true);
				this.loginField.setEnabled(true);
				this.passwordLabel.setEnabled(true);
				this.passwordField.setEnabled(true);
				String tempDatabaseName = getDatabaseName();
				String tempLogin = getLogin();
				String tempPassword = getPassword();
				this.connectionType = connectionType;
				if (this.connectionType.equals("mysql")) {
					this.portField.setText("3306");
				} else if (this.connectionType.equals("postgresql")) {
					this.portField.setText("5432");
				}
				this.databaseNameField.setText(tempDatabaseName);
				this.loginField.setText(tempLogin);
				this.passwordField.setText(tempPassword);
				this.urlLabel.setText("Server");
			}

			// set URL tooltip
			if (openExistingDatabase == true && connectionType.equals("sqlite")) {
				ttUrl = "<html><p width=\"500\">Enter the file name of the SQLite DNA database, "
						+ "ending with \".dna\". You can browse to select the path and file name.</p></html>";
			} else if (openExistingDatabase == false && connectionType.equals("sqlite")) {
				ttUrl = "<html><p width=\"500\">Enter the file name of a new SQLite DNA database, "
						+ "ending with \".dna\". You can browse to select the path and enter a file name. "
						+ "Do not select an existing file.</p></html>";
			} else if (openExistingDatabase == true && !connectionType.equals("sqlite")) {
				ttUrl = "<html><p width=\"500\">Enter the remote address (IP address or server "
						+ "name) of the database you wish to connect to. The database must have the DNA "
						+ "data structures and tables in order to create a connection.</p></html>";
			} else if (openExistingDatabase == false && !connectionType.equals("sqlite")) {
				ttUrl = "<html><p width=\"500\">Enter the remote address (IP address or server "
						+ "name) of an existing but empty remote database. The database must have no "
						+ "tables, and your user account must have the user right to create new tables "
						+ "in the database. DNA will create the table structures in this vanilla "
						+ "database for you, but it is your responsibility to ensure that the database "
						+ "does not contain any tables.</p></html>";
			}
			this.urlLabel.setToolTipText(ttUrl);
			this.urlField.setToolTipText(ttUrl);
		}

		/**
		 * Get the URL or file name for establishing a database connection.
		 * 
		 * @return The URL or file name as a String.
		 */
		String getUrl() {
			return urlField.getText();
		}
		
		/**
		 * Get the port number on the database server for establishing the
		 * database connection.
		 * 
		 * @return The port number on the server.
		 */
		int getPort() {
			if (this.connectionType.equals("sqlite")) {
				return -1;
			} else {
				int result = -1;
				try {
					result = Integer.parseInt(this.portField.getText());
				} catch (NumberFormatException e) {
					LogEvent l = new LogEvent(Logger.ERROR,
							"Port number could not be parsed.",
							"Port number could not be parsed. Try entering an integer number.");
					Dna.logger.log(l);
				}
				return result;
			}
		}
		
		/**
		 * Get the name of the remote database on the server.
		 * 
		 * @return The database name as a String.
		 */
		String getDatabaseName() {
			if (this.connectionType.equals("sqlite")) {
				return "";
			} else {
				return databaseNameField.getText();
			}
		}
		
		/**
		 * Get the login user name for the database from the login field.
		 * 
		 * @return The database login user name as a String. 
		 */
		String getLogin() {
			if (this.connectionType.equals("sqlite")) {
				return "";
			} else {
				return loginField.getText();
			}
		}
		
		/**
		 * Get the password from the password field.
		 * 
		 * @return The password as a String.
		 */
		String getPassword() {
			if (this.connectionType.equals("sqlite")) {
				return "";
			} else {
				return new String(passwordField.getPassword());
			}
		}
		
		/**
		 * Is the input provided by the user valid?
		 * 
		 * @return Boolean indicating if the user input is valid.
		 */
		boolean isValidInput() {
			boolean valid = true;
			if (connectionType.equals("sqlite")) {
				File f = new File(getUrl());
				if ((!openExistingDatabase && f.exists()) || (openExistingDatabase && !f.exists())) { // only a file that does not exist yet is valid
					valid = false;
				}
				try { // check if the path of the file is valid or has weird characters (not working on Linux)
	                Paths.get(getUrl());
	            } catch (InvalidPathException ex) {
	                valid = false;
	            }
				try { // another path check that does not work on Linux
					f.getCanonicalPath();
				} catch (IOException e) {
					valid = false;
				}
				if (!getUrl().endsWith(".dna")) {
					valid = false;
				}
			} else {
				if (getUrl().endsWith(".dna")) {
					valid = false;
				}
			}
			if (getUrl().equals("") || getUrl().startsWith(" ") || getUrl().endsWith(" ")) {
				valid = false;
			}
			if (valid == true) {
				this.urlField.setForeground(Color.BLACK);
			} else {
				this.urlField.setForeground(Color.RED);
			}
			if (this.portField.getText().matches(".*\\D.*")) {
				this.portField.setForeground(Color.RED);
				valid = false;
			} else {
				this.portField.setForeground(Color.BLACK);
			}
			if (getLogin().matches("\\s")) {
				valid = false;
			}
			if (valid) {
				saveButton.setEnabled(true);
			} else {
				saveButton.setEnabled(false);
			}
			return valid;
		}
		
		/**
		 * Reset all fields.
		 */
		void clear() {
			this.setConnectionType("sqlite");
			this.urlField.setText("");
		}
	}

	/**
	 * This function is called by action listeners and document change listeners
	 * to determine if the button for creating the tables and data structures
	 * should be enabled and what fields should be colored in red as a hint on
	 * what part of the input is invalid. Various validity checks are performed.
	 */
	public void checkButton() {
		boolean valid = true;
		if (!openExistingDatabase) {
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
		if (!connectionDetailsPanel.isValidInput()) {
			valid = false;
		}
		if (valid) {
			saveButton.setEnabled(true);
		} else {
			saveButton.setEnabled(false);
		}
	}
}