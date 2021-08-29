package gui;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.AES256TextEncryptor;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import sql.ConnectionProfile;
import sql.Sql;

class ActionOpenProfile extends AbstractAction {
	private static final long serialVersionUID = -1985734783855268915L;
	
	public ActionOpenProfile(String text, ImageIcon icon, String desc, Integer mnemonic) {
		super(text, icon);
		putValue(SHORT_DESCRIPTION, desc);
		putValue(MNEMONIC_KEY, mnemonic);
	}
	
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
			int returnVal = fc.showOpenDialog(null);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					filename = new String(file.getPath());
					if (!filename.endsWith(".dnc")) {
						filename = filename + ".dnc";
					}
					validFileInput = true; // file choice accepted
				} else {
					JOptionPane.showMessageDialog(null, "The file name you entered does not exist. Please choose a new file.");
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
								Dna.setSql(sqlTemp);
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
						JOptionPane.showMessageDialog(null,
			        			"Password check failed. Zero-length passwords are not permitted.",
							    "Check failed",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"[GUI] Action executed: opened connection profile.",
				"Opened a connection profile from the GUI.");
		Dna.logger.log(l);
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
			LogEvent l = new LogEvent(Logger.ERROR,
					"[GUI] Failed to read connection profile.",
					"Tried to read a connection profile from a JSON file and failed.",
					e);
			Dna.logger.log(l);
		}
		
		// decrypt the URL, user name, and SQL connection password inside the profile
		AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
		textEncryptor.setPassword(key);
		cp.setUrl(textEncryptor.decrypt(cp.getUrl()));
		cp.setUser(textEncryptor.decrypt(cp.getUser()));
		cp.setPassword(textEncryptor.decrypt(cp.getPassword()));
		
		return cp;
	}
}