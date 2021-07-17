package guiCoder;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.AES256TextEncryptor;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import dna.Dna;
import sql.Sql;

@SuppressWarnings("serial")
public class GuiCoder extends JFrame {
	Container c;
	DocumentTableModel documentTableModel;
	
	public void updateGUI() {
		documentTableModel.reloadTableFromSQL();

		// database update listener
		/*
		if (Dna.sql.getConnectionProfile().getType().equals("sqlite")) {
			((SQLiteConnection) Dna.sql.sqliteConnection).addCommitListener(new SQLiteCommitListener() {

				@Override
				public void onCommit() {
					documentTableModel.reloadTableFromSQL();
				}

				@Override
				public void onRollback() {
					// nothing to do
				}
				
			});
		}
		*/
	}
	
	public GuiCoder() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			System.err.println("Unsupport look and feel. Using default theme.");
	    } catch (ClassNotFoundException e) {
			System.err.println("Class not found. Using default theme.");
	    } catch (InstantiationException e) {
			System.err.println("Instantiation exception. Using default theme.");
	    } catch (IllegalAccessException e) {
			System.err.println("Illegal access exception. Using default theme.");
	    }

		c = getContentPane();
		this.setTitle("Discourse Network Analyzer");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
		this.setIconImage(dna32Icon.getImage());
		
		// close SQL connection before exit
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				documentTableModel = null;
				dispose();
			}
		});

		JPanel framePanel = new JPanel(new BorderLayout());
		
		documentTableModel = new DocumentTableModel();
		DocumentPanel documentPanel = new DocumentPanel(documentTableModel);
		framePanel.add(documentPanel, BorderLayout.CENTER);
		
		//c.add(documentTableScroller);
		
		// menu bar
		JMenuBar menu = new JMenuBar();
		framePanel.add(menu, BorderLayout.NORTH);
		JMenu databaseMenu = new JMenu("Database");
		menu.add(databaseMenu);
		JMenu documentMenu = new JMenu("Document");
		menu.add(documentMenu);
		JMenu exportMenu = new JMenu("Export");
		menu.add(exportMenu);
		JMenu settingsMenu = new JMenu("Settings");
		menu.add(settingsMenu);

		// database menu: open a database
		Icon openDatabaseIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-database-16.png"));
		JMenuItem openDatabaseItem = new JMenuItem("Open DNA database", openDatabaseIcon);
		databaseMenu.add(openDatabaseItem);
		openDatabaseItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				NewDatabaseDialog n = new NewDatabaseDialog(true);
				ConnectionProfile cp = n.getConnectionProfile();
				if (cp != null) {
					//setSql(new Sql(cp));
					Dna.sql = new Sql(cp);
					updateGUI();
				} // user must have clicked cancel; do nothing
			}
		});

		// database menu: create a new database
		Icon newDatabaseIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-plus-16.png"));
		JMenuItem newDatabaseItem = new JMenuItem("Create new DNA database", newDatabaseIcon);
		databaseMenu.add(newDatabaseItem);
		newDatabaseItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NewDatabaseDialog n = new NewDatabaseDialog(false);
				ConnectionProfile cp = n.getConnectionProfile();
				if (cp != null) {
					Dna.sql = new Sql(cp);
					updateGUI();
				}
			}
		});
		
		// database menu: open a connection profile
		Icon openProfileIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-link-16.png"));
		JMenuItem openProfileItem = new JMenuItem("Open connection profile", openProfileIcon);
		databaseMenu.add(openProfileItem);
		openProfileItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String filename = null;
				boolean validFileInput = false;
				while (!validFileInput) {
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".dnc") || f.isDirectory();
						}
						public String getDescription() {
							return "DNA connection profile (*.dnc)";
						}
					});
					int returnVal = fc.showOpenDialog(c);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						if (file.exists()) {
							filename = new String(file.getPath());
							if (!filename.endsWith(".dnc")) {
								filename = filename + ".dnc";
							}
							validFileInput = true; // file choice accepted
						} else {
							JOptionPane.showMessageDialog(c, "The file name you entered does not exist. Please choose a new file.");
						}
					} else { // cancel button; mark as valid file input, but reset file name
						filename = null;
						validFileInput = true;
					}
				}
				
				if (filename != null) { // if file has been chosen successfully, go on with authentication
					boolean validPasswordInput = false;
					while (!validPasswordInput) {
						// ask user for password (for the user in the connection profile) to decrypt the profile
						CoderPasswordCheckDialog d = new CoderPasswordCheckDialog();
						String key = d.getPassword();
						
						// decrypt connection profile, create SQL connection, and set as default SQL connection
						if (key == null) {
							validPasswordInput = true; // user must have pressed cancel; quit the while-loop
						} else {
							if (!key.equals("")) {
								ConnectionProfile cp = null;
								try {
									cp = readConnectionProfile(filename, key);
								} catch (EncryptionOperationNotPossibleException e2) {
									cp = null;
								}
								if (cp != null) {
									Sql sqlTemp = new Sql(cp);
									boolean authenticated = sqlTemp.authenticate(key);
									if (authenticated == true) {
										validPasswordInput = true; // authenticated; quit the while-loop
										Dna.sql = sqlTemp;
										updateGUI();
									} else {
										cp = null;
									}
								}
								if (cp == null) {
									JOptionPane.showMessageDialog(null,
						        			"Database credentials could not be decrypted.\n"
						        					+ "Did you enter the right password?",
										    "Check failed",
										    JOptionPane.ERROR_MESSAGE);
								}
							} else {
								JOptionPane.showMessageDialog(c,
					        			"Password check failed. Zero-length passwords are not permitted.",
									    "Check failed",
									    JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		});

		// database menu: save connection profile
		Icon saveProfileIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-download-16.png"));
		JMenuItem saveProfileItem = new JMenuItem("Save connection profile", saveProfileIcon);
		databaseMenu.add(saveProfileItem);
		saveProfileItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String filename = null;
				File file = null;
				boolean validFileInput = false;
				while (!validFileInput) {
					JFileChooser fc;
					if (file == null) {
						fc = new JFileChooser();
					} else {
						fc = new JFileChooser(file);
					}
					fc.setDialogTitle("Save connection profile...");
					fc.setApproveButtonText("Save");
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".dnc") || f.isDirectory();
						}
						public String getDescription() {
							return "DNA connection profile (*.dnc)";
						}
					});
					int returnVal = fc.showOpenDialog(c);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fc.getSelectedFile();
						filename = new String(file.getPath());
						if (!filename.endsWith(".dnc")) {
							filename = filename + ".dnc";
						}
						file = new File(filename);
						if (!file.exists()) {
							validFileInput = true; // file approved
						} else {
							file = null;
							JOptionPane.showMessageDialog(null, "The file name you entered already exists. Please choose a new file.");
						}
					} else {
						validFileInput = true; // user must have clicked cancel in file chooser
					}
				}
				// after getting a valid file, authenticate coder and write to file
				if (file != null) {
					boolean validPasswordInput = false;
					while (!validPasswordInput) {
						CoderPasswordCheckDialog d = new CoderPasswordCheckDialog(Dna.sql, false);
						String key = d.getPassword();
						if (key == null) { // user must have pressed cancel
							validPasswordInput = true;
						} else {
							boolean authenticated = Dna.sql.authenticate(key);
							if (authenticated == true) {
								// write the connection profile to disk, with an encrypted version of the password
								writeConnectionProfile(filename, Dna.sql.getConnectionProfile(), key);
								validPasswordInput = true; // quit the while-loop after successful export
								JOptionPane.showMessageDialog(null,
										"The profile was saved as:\n" + new File(filename).getAbsolutePath(),
										"Success",
									    JOptionPane.PLAIN_MESSAGE);
							} else {
					        	JOptionPane.showMessageDialog(null,
					        			"Coder password could not be verified. Try again.",
									    "Check failed",
									    JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		});

		/*
		JToolBar tb = new JToolBar("DNA toolbar");
		
		// DNA toolbar button: refresh GUI from database
		Icon refreshIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-refresh-16.png"));
		JButton refreshButton = new JButton("Refresh", refreshIcon);
		refreshButton.setToolTipText( "Refresh GUI" );
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateGUI();
			}
		});
		tb.add(refreshButton);
		
        tb.setRollover(true);
		//framePanel.add(tb, BorderLayout.NORTH);
		*/
		
		c.add(framePanel);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Read in a saved connection profile from a JSON file, decrypt the
	 * credentials, and return the connection profile.
	 * 
	 * @param file  The file name including path of the JSON connection profile
	 * @param key   The key/password of the coder to decrypt the credentials
	 * @return      Decrypted connection profile
	 */
	private ConnectionProfile readConnectionProfile(String file, String key) throws EncryptionOperationNotPossibleException {

		// read connection profile JSON file in, in String format but with encrypted credentials
		ConnectionProfile cp = null;
		Gson gson = new Gson();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			cp = gson.fromJson(br, ConnectionProfile.class);
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			System.err.println("Connection profile could not be read from file.");
			e.printStackTrace();
		}
		
		// decrypt the URL, user name, and SQL connection password inside the profile
		AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
		textEncryptor.setPassword(key);
		cp.setUrl(textEncryptor.decrypt(cp.getUrl()));
		cp.setUser(textEncryptor.decrypt(cp.getUser()));
		cp.setPassword(textEncryptor.decrypt(cp.getPassword()));
		
		return cp;
	}

	/**
	 * Take a decrypted connection profile, encrypt the credentials, and write
	 * it to a JSON file on disk.
	 * 
	 * @param file  The file name including full path as a String
	 * @param cp    The connection profile to be encrypted and saved
	 * @param key   The key/password of the coder to encrypt the credentials
	 */
	private void writeConnectionProfile(String file, ConnectionProfile cp, String key) {
		
		// encrypt URL, user, and password using Jasypt
		AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
		textEncryptor.setPassword(key);
		cp.setUrl(textEncryptor.encrypt(cp.getUrl()));
		cp.setUser(textEncryptor.encrypt(cp.getUser()));
		cp.setPassword(textEncryptor.encrypt(cp.getPassword()));
		
		// serialize Connection object to JSON file and save to disk
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			Gson gson = new Gson();
			gson.toJson(cp, writer);
		} catch (IOException e) {
			System.err.println("Could not write connection profile to disk.");
			e.printStackTrace();
		}
	}
	
	/*
	public class CoderTableCellEditor extends AbstractCellEditor implements TableCellEditor {

		CoderComboBoxRenderer renderer;
		CoderComboBoxModel model;
		JComboBox<Coder> coderBox;
		
		@Override
		public Object getCellEditorValue() {
			return model.getSelectedItem();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
			renderer = new CoderComboBoxRenderer();
			model = new CoderComboBoxModel();
			coderBox = new JComboBox<Coder>(model);
			coderBox.setRenderer(renderer);
			coderBox.setEnabled(true);
			return coderBox;
		}
		
	}
	*/

}