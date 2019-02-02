package dna;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import dna.dataStructures.AttributeVector;
import dna.dataStructures.Coder;
import dna.dataStructures.Data;
import dna.dataStructures.StatementType;

@SuppressWarnings("serial")
public class NewDatabaseDialog extends JDialog {
	JTree tree;
	DefaultMutableTreeNode top, coderNode, databaseNode, statementTypeNode, summaryNode;
	JPanel optionsPanel, databasePanel, coderPanel, statementTypePanel, summaryPanel;
	String dbType = "";
	String dbFile = "";
	String dbUser = "";
	String dbPassword = "";
	
	JButton goButton;
	public JLabel coderLabel, statementTypeLabel, dbLabel;
	public JPanel infoPanel;
	
	/**
	 * Create the frame.
	 */
	public NewDatabaseDialog() {
		this.setTitle("Create new database...");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon icon = new ImageIcon(getClass().getResource("/icons/database.png"));
		this.setIconImage(icon.getImage());
		this.setLayout(new BorderLayout());
		
		// remove data if canceled
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Dna.data = new Data();
				dispose();
			}
		});
		
		JPanel menu = new JPanel();
		menu.setLayout(new BorderLayout());

		// Options panel
		optionsPanel = new OptionsPanel();
		
		// Database panel
		databasePanel = new DatabasePanel();
		
		// Coder panel
		coderPanel = new CoderPanel();

		// Statement type panel
		statementTypePanel = new StatementTypePanel();

		// Summary panel
		summaryPanel = new SummaryPanel();
		
		// Card layout on the right
		JPanel panel = new JPanel();
		CardLayout cl = new CardLayout();
		panel.setLayout(cl);
		panel.add(optionsPanel, "optionsPanel");
		panel.add(databasePanel, "databasePanel");
		panel.add(coderPanel, "coderPanel");
		panel.add(statementTypePanel, "statementTypePanel");
		panel.add(summaryPanel, "summaryPanel");

		// Tree on the left
		top = new DefaultMutableTreeNode("Options");
		tree = new JTree(top);
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		databaseNode = new DefaultMutableTreeNode("Database");
		coderNode = new DefaultMutableTreeNode("Coder");
		statementTypeNode = new DefaultMutableTreeNode("Statement types");
		summaryNode = new DefaultMutableTreeNode("Summary");
		top.add(databaseNode);
		top.add(coderNode);
		top.add(statementTypeNode);
		top.add(summaryNode);
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
				} else if (node.equals("Options")){
					cl.show(panel, "optionsPanel");
				} else if (node.equals("Summary")){
					cl.show(panel, "summaryPanel");
					if (dbType == "" || dbFile == "") {
						dbLabel.setText("(No database selected)");
					} else {
						dbLabel.setText("Database: " + dbFile);
					}
					coderLabel.setText("Number of coders: " + Dna.data.getCoders().size());
					statementTypeLabel.setText("Statement types: " + Dna.data.getStatementTypes().size());
					if (dbType == "" || dbFile == "" || Dna.data.getCoders().size() == 0 || Dna.data.getStatementTypes().size() == 0) {
						goButton.setEnabled(false);
					} else {
						goButton.setEnabled(true);
					}
				} else {
					System.out.println(node);
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
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setResizable(false);
	}

	class OptionsPanel extends JPanel {
		public JTextArea notes;
		OptionsPanel() {
			this.setLayout(new BorderLayout());
			notes = new JTextArea();
			notes.setEditable(false);
			notes.setWrapStyleWord(true);
			notes.setLineWrap(true);
			notes.setText("You are about to create a new DNA database. \n\nPlease select a database, specify coders, and create statement types for coding. \n\nSome of these options cannot be reversed later.");
			notes.setOpaque(false);
			this.add(notes, BorderLayout.CENTER);
		}
	}
	
	class DatabasePanel extends JPanel {
		JButton applySqlButton;
		JTextField checkField;
		
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
							return f.getName().toLowerCase().endsWith(".dna") || f.isDirectory();
						}
						public String getDescription() {
							return "Discourse Network Analyzer database (*.dna)";
						}
					});

					int returnVal = fc.showSaveDialog(dna.NewDatabaseDialog.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						if (file.exists()) {
							JOptionPane.showMessageDialog(Dna.gui, "The file already exists. Please choose a new file.");
							browseButton.doClick();
						} else {
							String filename = new String(file.getPath());
							if (!filename.endsWith(".dna")) {
								filename = filename + ".dna";
							}
							fileField.setText(filename);
						}
					}
				}
			});
			fileField.setPreferredSize(new Dimension(300, (int) browseButton.getPreferredSize().getHeight()));
			
			JButton sqliteClearButton = new JButton("Clear", new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png")));
			sqliteClearButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fileField.setText("(No file selected)");
					NewDatabaseDialog.this.dbType = "";
					NewDatabaseDialog.this.dbFile = "";
					NewDatabaseDialog.this.dbUser = "";
					NewDatabaseDialog.this.dbPassword = "";
				}
			});
			JButton applyButton = new JButton("Apply", new ImageIcon(getClass().getResource("/icons/accept.png")));
			applyButton.setEnabled(false);
			applyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (fileField.getText().endsWith(".dna")) {
						NewDatabaseDialog.this.dbType = "sqlite";
						NewDatabaseDialog.this.dbFile = fileField.getText();
						NewDatabaseDialog.this.tree.setSelectionRow(2);
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
			applySqlButton = new JButton("Apply", new ImageIcon(getClass().getResource("/icons/accept.png")));
			applySqlButton.setEnabled(false);
			applySqlButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					NewDatabaseDialog.this.dbType = "mysql";
					NewDatabaseDialog.this.dbFile = addressField.getText();
					NewDatabaseDialog.this.dbUser = userField.getText();
					NewDatabaseDialog.this.dbPassword = String.copyValueOf(passwordField.getPassword());
					NewDatabaseDialog.this.tree.setSelectionRow(2);
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
					checkField.setText(testConnection.testNewMySQLConnection());
					testConnection.closeConnection();
					if (checkField.getText().equals("OK. Tables will be created.") || checkField.getText().equals("Warning: Database contains data that may be overwritten!")) {
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
					NewDatabaseDialog.this.dbType = "";
					NewDatabaseDialog.this.dbFile = "";
					NewDatabaseDialog.this.dbUser = "";
					NewDatabaseDialog.this.dbPassword = "";
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
		}
	}
	
	// Coder Panel
	class CoderPanel extends JPanel {
		JButton addButton, removeButton, editButton;
		CoderListModel model;
		CoderListRenderer renderer;
		
		CoderPanel() {
			this.setLayout(new BorderLayout());
			this.add(new JLabel("Manage coders and permissions"), BorderLayout.NORTH);
			
			JList<Coder> coderList = new JList<Coder>();
			model = new CoderListModel();
			coderList.setModel(model);
			coderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			renderer = new CoderListRenderer();
			coderList.setCellRenderer(renderer);
			JScrollPane sp = new JScrollPane(coderList);
			this.add(sp, BorderLayout.CENTER);
			
			addButton = new JButton("Add", new ImageIcon(getClass().getResource("/icons/user_add.png")));
			removeButton = new JButton("Remove", new ImageIcon(getClass().getResource("/icons/user_delete.png")));
			editButton = new JButton("Edit", new ImageIcon(getClass().getResource("/icons/user_edit.png")));
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			buttonPanel.add(addButton);
			buttonPanel.add(removeButton);
			buttonPanel.add(editButton);
			removeButton.setEnabled(false);
			editButton.setEnabled(false);
			this.add(buttonPanel, BorderLayout.SOUTH);
			
			coderList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (coderList.getModel().getSize() == 0 || coderList.isSelectionEmpty()) {
						removeButton.setEnabled(false);
						editButton.setEnabled(false);
					} else {
						removeButton.setEnabled(true);
						editButton.setEnabled(true);
					}
				}
			});
			
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					EditCoderWindow ecw = new EditCoderWindow(new Coder(Dna.data.generateNewId("coders")));
					Coder coder = ecw.getCoder();
					ecw.dispose();
					if (!coder.getName().equals("")) {
						Dna.data.addCoder(coder);
					}
					coderList.updateUI();
				}
			});
			
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					model.removeElement(coderList.getSelectedIndex());
					coderList.updateUI();
				}
			});

			editButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Coder coder = coderList.getSelectedValue();
					int index = coderList.getSelectedIndex();
					EditCoderWindow ecw = new EditCoderWindow(coder);
					Coder coderUpdated = ecw.getCoder();
					ecw.dispose();
					model.replaceElement(index, coderUpdated);
					coderList.updateUI();
				}
			});

			Coder admin = new Coder(Dna.data.generateNewId("coders"));
			admin.setName("Admin");
			admin.setColor(Color.YELLOW);
			Dna.data.addCoder(admin);
			coderList.updateUI();
		}
		
		// List model for linking the coder array list to the JList
		public class CoderListModel extends AbstractListModel<Coder> {
			
			public void removeElement(int index) {
				Dna.data.getCoders().remove(index);
		        fireContentsChanged(this, 0, Dna.data.getCoders().size() - 1);
			}
			
			public void replaceElement(int index, Object o) {
				Dna.data.getCoders().set(index, (Coder) o);
		        fireContentsChanged(this, index, index);
			}
			
		    public void addElement(Object o) {
		        Dna.data.getCoders().add((Coder) o);
		        Collections.sort(Dna.data.getCoders());
		        fireContentsChanged(this, 0, Dna.data.getCoders().size() - 1);
		    }

		    @Override
		    public Coder getElementAt(int index) {
		    	return Dna.data.getCoders().get(index);
		    }
		    
		    @Override
		    public int getSize() {
		    	return Dna.data.getCoders().size();
		    }
		}

		/**
		 * Renderer for coders in a JComboBox
		 */
		class CoderListRenderer extends JLabel implements ListCellRenderer<Object> {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Coder coder = (Coder) value;
				JPanel panel = new JPanel(new BorderLayout());
				
				JButton colorRectangle = (new JButton() {
					public void paintComponent(Graphics g) {
						super.paintComponent(g);
						g.setColor(coder.getColor());
						g.fillRect(2, 2, 14, 14);
					}
				});
				colorRectangle.setPreferredSize(new Dimension(18, 18));
				
				JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				namePanel.add(colorRectangle);
				namePanel.add(Box.createRigidArea(new Dimension(5,5)));
				JLabel coderLabel = new JLabel(coder.getName());
				namePanel.add(coderLabel);

				int numPerm = 0;
				if (coder.getPermissions().get("addDocuments")) {
					numPerm++;
				}
				if (coder.getPermissions().get("editDocuments")) {
					numPerm++;
				}
				if (coder.getPermissions().get("deleteDocuments")) {
					numPerm++;
				}
				if (coder.getPermissions().get("importDocuments")) {
					numPerm++;
				}
				if (coder.getPermissions().get("viewOthersDocuments")) {
					numPerm++;
				}
				if (coder.getPermissions().get("editOthersDocuments")) {
					numPerm++;
				}
				if (coder.getPermissions().get("addStatements")) {
					numPerm++;
				}
				if (coder.getPermissions().get("viewOthersStatements")) {
					numPerm++;
				}
				if (coder.getPermissions().get("editOthersStatements")) {
					numPerm++;
				}
				if (coder.getPermissions().get("editCoders")) {
					numPerm++;
				}
				if (coder.getPermissions().get("editStatementTypes")) {
					numPerm++;
				}
				if (coder.getPermissions().get("editRegex")) {
					numPerm++;
				}
				JLabel permissionsLabel = new JLabel("(" + numPerm + " out of 12 permissions set)");
				namePanel.add(Box.createRigidArea(new Dimension(5,5)));
				namePanel.add(permissionsLabel);
				
				if (isSelected) {
					UIDefaults defaults = javax.swing.UIManager.getDefaults();
					Color bg = defaults.getColor("List.selectionBackground");
					panel.setBackground(bg);
					namePanel.setBackground(bg);
				}
				
				panel.add(namePanel, BorderLayout.NORTH);
				return panel;
			}
		}
	}
	
	// StatementType panel
	class StatementTypePanel extends JPanel {
		JButton addButton, removeButton, editButton;
		StatementTypeListModel model;
		StatementTypeListRenderer renderer;
		
		StatementTypePanel() {
			this.setLayout(new BorderLayout());
			this.add(new JLabel("Manage statement types"), BorderLayout.NORTH);
			
			JList<StatementType> statementTypeList = new JList<StatementType>();
			statementTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			model = new StatementTypeListModel();
			statementTypeList.setModel(model);
			renderer = new StatementTypeListRenderer();
			statementTypeList.setCellRenderer(renderer);
			JScrollPane sp = new JScrollPane(statementTypeList);
			this.add(sp, BorderLayout.CENTER);
			
			addButton = new JButton("Add", new ImageIcon(getClass().getResource("/icons/user_add.png")));
			removeButton = new JButton("Remove", new ImageIcon(getClass().getResource("/icons/user_delete.png")));
			editButton = new JButton("Edit", new ImageIcon(getClass().getResource("/icons/user_edit.png")));
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			buttonPanel.add(addButton);
			buttonPanel.add(removeButton);
			buttonPanel.add(editButton);
			removeButton.setEnabled(false);
			editButton.setEnabled(false);
			this.add(buttonPanel, BorderLayout.SOUTH);
			
			statementTypeList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (statementTypeList.getModel().getSize() == 0 || statementTypeList.isSelectionEmpty()) {
						removeButton.setEnabled(false);
						editButton.setEnabled(false);
					} else {
						removeButton.setEnabled(true);
						editButton.setEnabled(true);
					}
				}
			});
			
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					LinkedHashMap<String, String> lhm = new LinkedHashMap<String, String>();
					EditStatementTypeWindow ecw = new EditStatementTypeWindow(
							new StatementType(Dna.data.generateNewId("statementTypes"), "", Color.YELLOW, lhm));
					StatementType statementType = ecw.getStatementType();
					ecw.dispose();
					if (!statementType.getLabel().equals("")) {
						model.addElement(statementType);
					}
					statementTypeList.updateUI();
				}
			});
			
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					model.removeElement(statementTypeList.getSelectedIndex());
					statementTypeList.updateUI();
				}
			});

			editButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					StatementType statementType = statementTypeList.getSelectedValue();
					int index = statementTypeList.getSelectedIndex();
					EditStatementTypeWindow estw = new EditStatementTypeWindow(statementType);
					StatementType statementTypeUpdated = estw.getStatementType();
					estw.dispose();
					model.replaceElement(index, statementTypeUpdated);
					statementTypeList.updateUI();
				}
			});
			
			LinkedHashMap<String, String> lhm = new LinkedHashMap<String, String>();
			lhm.put("person", "short text");
			lhm.put("organization", "short text");
			lhm.put("concept", "short text");
			lhm.put("agreement", "boolean");
			StatementType dnaStatement = new StatementType(
					Dna.data.generateNewId("statementTypes"), "DNA Statement", Color.YELLOW, lhm);
			model.addElement(dnaStatement);
			
			LinkedHashMap<String, String> annoMap = new LinkedHashMap<String, String>();
			annoMap.put("note", "long text");
			StatementType annotation = new StatementType(
					Dna.data.generateNewId("statementTypes"), "Annotation", Color.LIGHT_GRAY, annoMap);
			model.addElement(annotation);
			
			statementTypeList.updateUI();
		}
		
		// List model for linking the statement type array list to the JList
		public class StatementTypeListModel extends AbstractListModel<StatementType> {
			
			public void removeElement(int index) {
				Dna.data.getStatementTypes().remove(index);
		        fireContentsChanged(this, 0, Dna.data.getStatementTypes().size() - 1);
			}
			
			public void replaceElement(int index, Object o) {
				Dna.data.getStatementTypes().set(index, (StatementType) o);
		        fireContentsChanged(this, index, index);
			}
			
		    public void addElement(Object o) {
		        Dna.data.getStatementTypes().add((StatementType) o);
		        //Collections.sort(Dna.data.getStatementTypes());
		        fireContentsChanged(this, 0, Dna.data.getStatementTypes().size() - 1);
		    }

		    @Override
		    public StatementType getElementAt(int index) {
		    	return Dna.data.getStatementTypes().get(index);
		    }
		    
		    @Override
		    public int getSize() {
		    	return Dna.data.getStatementTypes().size();
		    }
		}

		/**
		 * Renderer for statement types in a JComboBox
		 */
		class StatementTypeListRenderer extends JLabel implements ListCellRenderer<Object> {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				StatementType statementType = (StatementType) value;
				JPanel panel = new JPanel(new BorderLayout());
				
				JButton colorRectangle = (new JButton() {
					public void paintComponent(Graphics g) {
						super.paintComponent(g);
						g.setColor(statementType.getColor());
						g.fillRect(2, 2, 14, 14);
					}
				});
				colorRectangle.setPreferredSize(new Dimension(18, 18));
				
				JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				namePanel.add(colorRectangle);
				namePanel.add(Box.createRigidArea(new Dimension(5,5)));
				JLabel statementTypeLabel = new JLabel(statementType.getLabel());
				namePanel.add(statementTypeLabel);
				
				int numVar = statementType.getVariables().size();
				JLabel varNumLabel = new JLabel("(" + numVar + " variables)");
				namePanel.add(Box.createRigidArea(new Dimension(5,5)));
				namePanel.add(varNumLabel);
				
				if (isSelected) {
					UIDefaults defaults = javax.swing.UIManager.getDefaults();
					Color bg = defaults.getColor("List.selectionBackground");
					//Color fg = defaults.getColor("List.selectionForeground");
					panel.setBackground(bg);
					namePanel.setBackground(bg);
				}
				
				panel.add(namePanel, BorderLayout.NORTH);
				return panel;
			}
		}
		
		// GUI component for editing statement type details
		class EditStatementTypeWindow extends JDialog{
			StatementType statementType, copy;
			JTextField nameField;
			JColorChooser colorChooser;
			JTable varTable;
			JRadioButton stext, ltext, integ, bool;
			JTextField varTextField;
			JButton addVariable, trashVariable, addColorButton;
			
			public EditStatementTypeWindow(StatementType statementType) {
				this.statementType = statementType;
				this.copy = statementType;
				
				this.setTitle("Statement type details");
				this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				ImageIcon icon = new ImageIcon(getClass().getResource("/icons/application_form_edit.png"));
				this.setIconImage(icon.getImage());
				this.setLayout(new BorderLayout());
				
				JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				JLabel nameLabel = new JLabel("Name: ");
				namePanel.add(nameLabel);
				namePanel.add(Box.createRigidArea(new Dimension(5,5)));
				nameField = new JTextField(statementType.getLabel());
				nameField.setColumns(20);
				namePanel.add(nameField);
				
				JButton colorButtonTemp = (new JButton() {
					public void paintComponent(Graphics g) {
						super.paintComponent(g);
						g.setColor(this.getForeground());
						g.fillRect(2, 2, 14, 14);
					}
				});
				addColorButton = colorButtonTemp;
				addColorButton.setForeground(statementType.getColor());
				addColorButton.setPreferredSize(new Dimension(18, 18));
				addColorButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Color actualColor = ((JButton)e.getSource()).getForeground();
						Color newColor = JColorChooser.showDialog(EditStatementTypeWindow.this, "choose color...", actualColor);
						if (newColor != null) {
							((JButton) e.getSource()).setForeground(newColor);
						}
					}
				});
				namePanel.add(Box.createRigidArea(new Dimension(5,5)));
				namePanel.add(addColorButton);
				
				this.add(namePanel, BorderLayout.NORTH);
				
				// variable table
				JPanel rightPanel = new JPanel(new BorderLayout());
				String[] columnNames = {"variable name", "data type"};
				DefaultTableModel varTableModel = new DefaultTableModel(columnNames, 0);
				varTable = new JTable(varTableModel);
				varTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				JScrollPane varScrollPane = new JScrollPane(varTable);
				varScrollPane.setPreferredSize(new Dimension(100, 184));
				varTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 50 );
				varTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 50 );
				Class<?> colClass = varTable.getColumnClass(0);
			    varTable.setDefaultEditor(colClass, null);
				colClass = varTable.getColumnClass(1);
			    varTable.setDefaultEditor(colClass, null);
				JPanel middleRightPanel = new JPanel(new BorderLayout());
				middleRightPanel.add(varScrollPane, BorderLayout.NORTH);
				
				JPanel radioButtonPanel = new JPanel(new GridLayout(2, 2));
				ButtonGroup buttonGroup = new ButtonGroup();
				stext = new JRadioButton("short text");
				ltext = new JRadioButton("long text");
				integ = new JRadioButton("integer");
				bool = new JRadioButton("boolean");
				stext.setEnabled(false);
				ltext.setEnabled(false);
				integ.setEnabled(false);
				bool.setEnabled(false);
				stext.setSelected(true);
				buttonGroup.add(stext);
				buttonGroup.add(ltext);
				buttonGroup.add(integ);
				buttonGroup.add(bool);
				radioButtonPanel.add(stext);
				radioButtonPanel.add(ltext);
				radioButtonPanel.add(integ);
				radioButtonPanel.add(bool);
				
				// variable buttons
				JPanel varButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				
				varTextField = new JTextField(24);
				varTextField.getDocument().addDocumentListener(new DocumentListener() {
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
					public void checkButton() {
						String var = varTextField.getText();
						boolean validAdd = false;
						boolean validApply = false;
						if (var.equals("") || var.matches(".*\\s+.*")) {
							validAdd = false;
							validApply = false;
						} else {
							validAdd = true;
						}
						LinkedHashMap<String, String> map = statementType.getVariables();
						Object[] types = map.keySet().toArray();
						for (int i = 0; i < types.length; i++) {
							if (types[i].equals(var)) {
								validAdd = false;
								validApply = true;
							}
						}
						if (!stext.isSelected() && !ltext.isSelected() && !integ.isSelected() && !bool.isSelected()) {
							validAdd = false;
							validApply = false;
						}
						if (validAdd == true) {
							addVariable.setEnabled(true);
						} else {
							addVariable.setEnabled(false);
						}
						if (validApply == true) {
							trashVariable.setEnabled(true);
						} else {
							trashVariable.setEnabled(false);
						}
					}
				});
				
				//varTextField.setEnabled(false);
				ImageIcon addIcon = new ImageIcon(getClass().getResource("/icons/add.png"));
				ImageIcon removeIcon = new ImageIcon(getClass().getResource("/icons/trash.png"));
				addVariable = new JButton(addIcon);
				addVariable.setPreferredSize(new Dimension(18, 18));
				addVariable.setToolTipText("add this new variable to the list");
				addVariable.setEnabled(false);
		        addVariable.addActionListener(new ActionListener() {
		        	public void actionPerformed(ActionEvent e) {
		        		// collect required data
		        		String dataType = null;
		        		if (stext.isSelected()) {
		        			dataType = "short text";
		        		} else if (ltext.isSelected()) {
		        			dataType = "long text";
		        		} else if (integ.isSelected()) {
		        			dataType = "integer";
		        		} else if (bool.isSelected()) {
		        			dataType = "boolean";
		        		}
		        		String newVar = varTextField.getText();

		        		// update table in the current window
			    		int varRows = varTable.getModel().getRowCount();
			    		String[] newRow = {newVar, dataType};
			    		((DefaultTableModel)varTable.getModel()).insertRow(varRows, newRow);
		        		
		        		// update statement type
			    		statementType.getVariables().put(newVar, dataType);
		        	}
		        });
		        
				trashVariable = new JButton(removeIcon);
				trashVariable.setPreferredSize(new Dimension(18, 18));
				trashVariable.setToolTipText("remove this variable from the list");
				trashVariable.setEnabled(false);
		        trashVariable.addActionListener(new ActionListener() {
		        	public void actionPerformed(ActionEvent e) {
		        		// collect data
		        		String varName = varTextField.getText();
		        		
		        		// ask for confirmation
		        		int dialog = JOptionPane.showConfirmDialog(
								EditStatementTypeWindow.this, 
								"Really remove variable \"" + varName + 
								"\" along with all data?", 
								"Confirmation required", JOptionPane.OK_CANCEL_OPTION);
		        		
		        		if (dialog == 0) {
		            		// update statement type
		    	    		statementType.getVariables().remove(varName);
		            		
		            		// update table in the current window
		            		for (int i = varTable.getModel().getRowCount() - 1; i >= 0; i--) {
		            			if (varTable.getModel().getValueAt(i, 0).equals(varName)) {
		            				((DefaultTableModel)varTable.getModel()).removeRow(i);
		            			}
		            		}
		        		}
		        	}
		        });
		        
				varButtonPanel.add(varTextField);
				varButtonPanel.add(addVariable);
				varButtonPanel.add(trashVariable);
				
				//populate table with variables
				Iterator<String> keyIterator = statementType.getVariables().keySet().iterator();
		        while (keyIterator.hasNext()){
		    		String key = keyIterator.next();
		    		String value = statementType.getVariables().get(key);
		    		int varRows = varTable.getModel().getRowCount();
		    		String[] newRow = {key, value};
		    		((DefaultTableModel)varTable.getModel()).insertRow(varRows, newRow);
		    	}
				
				// assemble right panel
				rightPanel.add(middleRightPanel, BorderLayout.NORTH);
				rightPanel.add(radioButtonPanel, BorderLayout.CENTER);
				rightPanel.add(varButtonPanel, BorderLayout.SOUTH);
				this.add(rightPanel);
				
				varTable.getSelectionModel().addListSelectionListener(new VarTableSelectionHandler());
				
				// button panel
				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				JButton okButton = new JButton("OK", new ImageIcon(getClass().getResource("/icons/accept.png")));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						statementType.setLabel(nameField.getText());
						statementType.setColor(addColorButton.getForeground());
						setVisible(false);
					}
				});
				JButton cancelButton = new JButton("Cancel", new ImageIcon(getClass().getResource("/icons/cancel.png")));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelAction();
					}
				});
				buttonPanel.add(okButton);
				buttonPanel.add(cancelButton);
				
				nameField.getDocument().addDocumentListener(new DocumentListener() {
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
						if (nameField.getText().equals("")) {
							okButton.setEnabled(false);
							stext.setEnabled(false);
							ltext.setEnabled(false);
							integ.setEnabled(false);
							bool.setEnabled(false);
							varTextField.setEnabled(false);
							addVariable.setEnabled(false);
							trashVariable.setEnabled(false);
						} else {
							okButton.setEnabled(true);
							stext.setEnabled(true);
							ltext.setEnabled(true);
							integ.setEnabled(true);
							bool.setEnabled(true);
							varTextField.setEnabled(true);
						}
					}
				});
				
				JPanel lowerPanel = new JPanel(new BorderLayout());
				lowerPanel.add(buttonPanel, BorderLayout.SOUTH);
				this.add(lowerPanel, BorderLayout.SOUTH);

				addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						cancelAction();
					}
				});

				this.pack();
				this.setModal(true);
				this.setLocationRelativeTo(null);
				this.setVisible(true);
				this.setResizable(false);
			}
			
			public void cancelAction() {
				statementType = copy;
				dispose();
			}
			
			public void setStatementType(StatementType statementType) {
				this.setStatementType(statementType);
			}
			
			public StatementType getStatementType() {
				return(this.statementType);
			}

			class VarTableSelectionHandler implements ListSelectionListener {
			    public void valueChanged(ListSelectionEvent e) {
		    		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		    		if (!lsm.getValueIsAdjusting()) {
		    			int varRow = lsm.getMinSelectionIndex();
		    			if (varRow > -1) {
		    				String varName = (String) varTable.getValueAt(varRow, 0);
		    	    		varTextField.setText(varName);
		    	    		String dataType = (String) varTable.getValueAt(varRow, 1);
		    	        	if (dataType.equals("short text")) {
		    	        		stext.setSelected(true);
		    	        	} else if (dataType.equals("long text")) {
		    	        		ltext.setSelected(true);
		    	        	} else if (dataType.equals("integer")) {
		    	        		integ.setSelected(true);
		    	        	} else if (dataType.equals("boolean")) {
		    	        		bool.setSelected(true);
		    	        	}
		    	        	
		    	    		stext.setEnabled(true);
		    	    		ltext.setEnabled(true);
		    	    		integ.setEnabled(true);
		    	    		bool.setEnabled(true);
		    		        
		    		        varTextField.setEnabled(true);
		    				trashVariable.setEnabled(true);
		    			} else {
		    				stext.setSelected(true);
		    				varTextField.setText("");
		    			}
			    	}
			    }
			}
		}
	}

	public class SummaryPanel extends JPanel {
		
		public SummaryPanel() {
			this.setLayout(new BorderLayout());
			JLabel summaryLabel = new JLabel("Summary:");
			infoPanel = new JPanel();
			infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
			dbLabel = new JLabel("(No database selected)");
			coderLabel = new JLabel("(No coders selected)");
			statementTypeLabel = new JLabel("Statement types: " + Dna.data.getStatementTypes().size());
			infoPanel.add(summaryLabel);
			infoPanel.add(Box.createRigidArea(new Dimension(5, 5)));
			infoPanel.add(dbLabel);
			infoPanel.add(Box.createRigidArea(new Dimension(5, 5)));
			infoPanel.add(coderLabel);
			infoPanel.add(Box.createRigidArea(new Dimension(5, 5)));
			infoPanel.add(statementTypeLabel);
			this.add(infoPanel, BorderLayout.CENTER);
			
			goButton = new JButton("Create database", new ImageIcon(getClass().getResource("/icons/accept.png")));
			JButton cancelButton = new JButton("Cancel", new ImageIcon(getClass().getResource("/icons/cancel.png")));
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			buttonPanel.add(goButton);
			buttonPanel.add(cancelButton);
			goButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Dna.dna.sql = new SqlConnection(NewDatabaseDialog.this.dbType, NewDatabaseDialog.this.dbFile, NewDatabaseDialog.this.dbUser, NewDatabaseDialog.this.dbPassword);
					Dna.dna.sql.createDataStructure();
					Dna.data.getSettings().put("version", Dna.dna.version);
					Dna.dna.sql.upsertSetting("version", Dna.dna.version);
					Dna.data.getSettings().put("date", Dna.dna.date);
					Dna.dna.sql.upsertSetting("date", Dna.dna.date);
					Dna.data.getSettings().put("popupWidth", "220");  // default width of text fields in popup windows
					Dna.dna.sql.upsertSetting("popupWidth", "220");
					Dna.gui.popupWidthModel.setValue(220);

					Dna.data.setActiveCoder(Dna.data.getCoders().get(0).getId());
					Dna.dna.sql.upsertSetting("activeCoder", Integer.toString(Dna.data.getCoders().get(0).getId()));
					
					for (int i = 0; i < Dna.data.getCoders().size(); i++) {
						Dna.dna.sql.addCoder(Dna.data.getCoders().get(i));
					}
					for (int i = 0; i < Dna.data.getStatementTypes().size(); i++) {
						Dna.dna.sql.upsertStatementType(Dna.data.getStatementTypes().get(i));
					}
					
					Dna.data.getSettings().put("statementColor", "coder");
					Dna.dna.sql.upsertSetting("statementColor", "coder");
					
					Dna.data.setActiveCoder(Dna.data.getCoders().get(0).getId());
					
					Dna.gui.statusBar.resetLabel();
					Dna.gui.menuBar.openDatabase.setEnabled(false);
					Dna.gui.menuBar.newDatabase.setEnabled(false);
					Dna.gui.menuBar.closeDatabase.setEnabled(true);
					Dna.gui.menuBar.newDocumentButton.setEnabled(true);
					Dna.gui.menuBar.networkButton.setEnabled(true);
					Dna.gui.menuBar.colorStatementTypeButton.setEnabled(true);
					Dna.gui.menuBar.colorCoderButton.setEnabled(true);
					Dna.gui.rightPanel.rm.setFieldsEnabled(true);
					Dna.gui.leftPanel.docStats.refreshButton.setEnabled(true);
					Dna.gui.rightPanel.statementPanel.typeComboBox.setSelectedIndex(0);
					Dna.gui.rightPanel.statementPanel.statementFilter.showAll.doClick();
					
					Dna.gui.leftPanel.setComboEnabled(true);
					int ac = Dna.data.getActiveCoder();
					Dna.gui.leftPanel.coderPanel.model.setSelectedItem(Dna.data.getCoderById(ac));
					
					// add an initial empty attribute vector for each "short text" variable
					for (int i = 0; i < Dna.data.getStatementTypes().size(); i++) {
						StatementType statementType = Dna.data.getStatementTypes().get(i);
						Iterator<String> keyIterator = statementType.getVariables().keySet().iterator();
				        while (keyIterator.hasNext()){
				    		String key = keyIterator.next();
				    		String value = statementType.getVariables().get(key);
				    		if (value.equals("short text")) {
				    			AttributeVector av = new AttributeVector(Dna.data.generateNewId("attributes"), "", Color.BLACK, "", "", "", "", statementType.getId(), key);
				    			Dna.dna.addAttributeVector(av);
				    		}
				    	}
					}
					
					Dna.gui.refreshGui();
					
					dispose();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			this.add(buttonPanel, BorderLayout.SOUTH);
		}
		
		public void setDbLabel(String text) {
			dbLabel.setText(text);
		}
	}
}
