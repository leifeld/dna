package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import model.Document;
import model.TableDocument;
import sql.ConnectionProfile;
import sql.Sql;
import sql.Sql.SQLCloseable;
import sql.Sql.SqlResults;

class Importer extends JDialog {
	private static final long serialVersionUID = -5295303422543731461L;
	private JButton dbButton, filterButton, selectAll, cancelButton, importButton;
	private DocumentTableModel dtm;
	private CoderTableModel coderTableModel;
	private ArrayList<Coder> domesticCoders;
	private JTable documentTable;
	private Sql sql;
	private JCheckBox importStatementsBox, statementTypeBox, skipFullBox, skipEmptyBox, coderDocumentBox, coderStatementBox, skipDuplicatesBox, fixDatesBox, mergeAttributesBox, overwriteAttributesBox, importEntitiesBox, importRegexBox;

	public Importer() {
		ImageIcon importerIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-database-import.png"));
		this.setIconImage(importerIcon.getImage());
		this.setModal(true);
		this.setTitle("Import from another DNA database");
		this.setLayout(new BorderLayout());
		JPanel panel = new JPanel(new BorderLayout());

		// coder table panel
		domesticCoders = Dna.sql.getCoders();
		Coder[] coderArray = new Coder[domesticCoders.size()];
		coderArray = domesticCoders.toArray(coderArray);
		JComboBox<Coder> comboBox = new JComboBox<Coder>(coderArray);
		CoderRenderer boxRenderer = new CoderRenderer();
		comboBox.setRenderer(boxRenderer);
		coderTableModel = new CoderTableModel();
		JTable coderTable = new JTable( coderTableModel );
        TableColumn column = coderTable.getColumnModel().getColumn(1);
        coderTable.setRowHeight(28);
        column.setCellEditor(new DefaultCellEditor(comboBox));
        CoderTableCellRenderer coderTableCellRenderer = new CoderTableCellRenderer();
        //coderTableCellRenderer.setHorizontalAlignment(JLabel.CENTER);
        coderTableCellRenderer.setBorder(5);
        coderTable.setDefaultRenderer(Coder.class, coderTableCellRenderer);
		coderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane coderScroller = new JScrollPane(coderTable);
		coderScroller.setPreferredSize(new Dimension(300, 260));
		coderTable.getTableHeader().setReorderingAllowed(false);

		JPanel coderPanel = new JPanel(new GridBagLayout());
		GridBagConstraints coderConstraints = new GridBagConstraints();
		coderConstraints.insets = new Insets(5, 5, 5, 5);
		coderConstraints.fill = GridBagConstraints.BOTH;
		coderConstraints.weightx = 1.0;
		coderConstraints.gridx = 0;
		coderConstraints.gridy = 0;
		coderPanel.add(coderScroller, coderConstraints);
		CompoundBorder borderCoders;
		borderCoders = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Map coders from import to open database"));
		coderPanel.setBorder(borderCoders);
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(coderPanel, BorderLayout.CENTER);
		
		// checkbox panel with additional options
		JPanel checkBoxPanel = new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.weightx = 1.0;
		g.anchor = GridBagConstraints.WEST;
		g.gridx = 0;
		g.gridy = 0;
		importStatementsBox = new JCheckBox("Import statements");
		importStatementsBox.setSelected(true);
		checkBoxPanel.add(importStatementsBox, g);

		g.gridy = 1;
		statementTypeBox = new JCheckBox("Include unknown statement types in import");
		statementTypeBox.setSelected(true);
		checkBoxPanel.add(statementTypeBox, g);

		g.gridy = 2;
		skipFullBox = new JCheckBox("Skip documents with statements");
		skipFullBox.setSelected(false);
		checkBoxPanel.add(skipFullBox, g);

		g.gridy = 3;
		skipEmptyBox = new JCheckBox("Skip documents without statements");
		skipEmptyBox.setSelected(false);
		checkBoxPanel.add(skipEmptyBox, g);

		g.gridy = 4;
		coderDocumentBox = new JCheckBox("Skip documents not mapped to active coder");
		coderDocumentBox.setSelected(false);
		checkBoxPanel.add(coderDocumentBox, g);

		g.gridy = 5;
		coderStatementBox = new JCheckBox("Skip statements not mapped to active coder");
		coderStatementBox.setSelected(false);
		checkBoxPanel.add(coderStatementBox, g);

		g.gridy = 6;
		skipDuplicatesBox = new JCheckBox("Skip documents with identical title and text");
		skipDuplicatesBox.setSelected(false);
		checkBoxPanel.add(skipDuplicatesBox, g);

		g.gridy = 7;
		fixDatesBox = new JCheckBox("Round date/time to nearest date at 00:00");
		fixDatesBox.setSelected(false);
		checkBoxPanel.add(fixDatesBox, g);

		g.gridy = 8;
		mergeAttributesBox = new JCheckBox("Merge attributes upon import");
		mergeAttributesBox.setSelected(true);
		checkBoxPanel.add(mergeAttributesBox, g);

		g.gridy = 9;
		overwriteAttributesBox = new JCheckBox("Overwrite attribute data on conflict");
		overwriteAttributesBox.setSelected(false);
		checkBoxPanel.add(overwriteAttributesBox, g);

		g.gridy = 10;
		importEntitiesBox = new JCheckBox("Import unused entities");
		importEntitiesBox.setSelected(true);
		checkBoxPanel.add(importEntitiesBox, g);

		g.gridy = 11;
		importRegexBox = new JCheckBox("Import regex keywords");
		importRegexBox.setSelected(true);
		checkBoxPanel.add(importRegexBox, g);

		CompoundBorder borderCheckBoxes;
		borderCheckBoxes = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Import options"));
		checkBoxPanel.setBorder(borderCheckBoxes);
		
		northPanel.add(checkBoxPanel, BorderLayout.EAST);
		panel.add(northPanel, BorderLayout.NORTH);
		
		// document table panel
		dtm = new DocumentTableModel();
		documentTable = new JTable(dtm);
		documentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        documentTable.setDefaultRenderer(Coder.class, new CoderTableCellRenderer());
		
		documentTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		documentTable.getColumnModel().getColumn(1).setPreferredWidth(350);
		documentTable.getColumnModel().getColumn(2).setPreferredWidth(40);
		documentTable.getColumnModel().getColumn(3).setPreferredWidth(80);
		documentTable.getColumnModel().getColumn(4).setPreferredWidth(130);
		documentTable.getTableHeader().setReorderingAllowed(false);

		JScrollPane tableScrollPane = new JScrollPane(documentTable);
		tableScrollPane.setPreferredSize(new Dimension(1000, 300));

		JPanel documentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints documentConstraints = new GridBagConstraints();
		documentConstraints.insets = new Insets(5, 5, 5, 5);
		documentConstraints.fill = GridBagConstraints.BOTH;
		documentConstraints.weightx = 1.0;
		documentConstraints.gridx = 0;
		documentConstraints.gridy = 0;
		documentPanel.add(tableScrollPane, coderConstraints);
		CompoundBorder borderDocuments;
		borderDocuments = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Select documents to import"));
		documentPanel.setBorder(borderDocuments);
		panel.add(documentPanel, BorderLayout.CENTER);
		
		// button panel
		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));

		ImageIcon dbIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database-import.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		dbButton = new JButton("Select database...", dbIcon);
		dbButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewDatabaseDialog n = new NewDatabaseDialog(true);
				ConnectionProfile cp = n.getConnectionProfile();
				if (cp == null) {
					LogEvent l = new LogEvent(Logger.MESSAGE,
							"[GUI] Action executed: could not connect to database for document import.",
							"Started opening a database connection from the GUI for document import, but the connection was not established.");
					dna.Dna.logger.log(l);
				} else {
					Sql s = new Sql(cp, false);
					ArrayList<Coder> foreignCoders = s.getCoders();
					coderTableModel.clear();
					for (int i = 0; i < foreignCoders.size(); i++) {
						Coder c = null;
						for (int j = 0; j < domesticCoders.size(); j++) {
							if (foreignCoders.get(i).getName().equals(domesticCoders.get(j).getName())) {
								c = domesticCoders.get(j);
								break;
							}
						}
						if (c == null) {
							c = Dna.sql.getActiveCoder();
						}
						coderTableModel.addCoderPair(foreignCoders.get(i), c);
					}
					(new DocumentTableRefreshWorker()).execute();
				}
			}
		});
		buttonPanel.add(dbButton);

		ImageIcon filterIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-filter.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		filterButton = new JButton("Keyword filter...", filterIcon);
		filterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = (String)JOptionPane.showInputDialog(
						Importer.this, 
						"Please enter a regular expression to filter the documents:", 
						"Keyword filter", 
						JOptionPane.PLAIN_MESSAGE, 
						filterIcon,
	                    null,
	                    "");
				if ((s != null) && (s.length() > 0)) {
					for (int i = 0; i < dtm.getRowCount(); i++) {
						Pattern p = Pattern.compile(s);
	    				Matcher m = p.matcher(dtm.getValueAt(i, 1).toString());
	    				boolean b = m.find();
	    				if (b == true) {
	    					dtm.setValueAt(true, i, 0);
	    				} else {
	    					dtm.setValueAt(false, i, 0);
	    				}
					}
				}
			}
		});
		buttonPanel.add(filterButton);

		ImageIcon selectIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-checks.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		selectAll = new JButton("(Un)select all", selectIcon);
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((Boolean) dtm.getValueAt(0, 0) == false) {
					for (int i = 0; i < dtm.getRowCount(); i++) {
						dtm.setValueAt(true, i, 0);
					}
				} else {
					for (int i = 0; i < dtm.getRowCount(); i++) {
						dtm.setValueAt(false, i, 0);
					}
				}
				
			}
		});
		buttonPanel.add(selectAll);

		ImageIcon cancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		cancelButton = new JButton("Cancel / close", cancelIcon);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Importer.this.dispose();
			}
		});
		buttonPanel.add(cancelButton);

		ImageIcon okIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database-import.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		importButton = new JButton("Import selected", okIcon);
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Importer.this, 
						"Are you sure you want to insert the selected \n" +
						"documents into the currently open DNA database?", 
						"Confirmation", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					try {
						// Thread importThread = new Thread( new DocumentInserter(file), "Import documents" );
						// importThread.start();
						System.out.println("test");
						// TODO: start swing worker or thread here
					} catch (OutOfMemoryError ome) {
						System.err.println("Out of memory. File has been " +
								"closed. Please start Java with\nthe " +
								"-Xmx1024M option, where '1024' is the space " +
								"you want\nto allocate to DNA. The manual " +
								"provides further details.");
						JOptionPane.showMessageDialog(Importer.this, 
								"Out of memory. File has been closed. Please " +
								"start Java with\nthe -Xmx1024M option, " +
								"where '1024' is the space you want\nto " +
								"allocate to DNA. The manual provides " +
								"further details.");
					}
					dispose();
				}
			}
		});
		buttonPanel.add(importButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		this.add(panel);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	private class CoderTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 6251543822435460514L;
		private ArrayList<Coder> coderForeign = new ArrayList<Coder>();
		private ArrayList<Coder> coderDomestic = new ArrayList<Coder>();
		
		public Class<?> getColumnClass(int columnIndex) {
			return Coder.class;
		}
		
		public int getColumnCount() {
			return 2;
		}
		
		public String getColumnName(int column) {
			switch( column ){
				case 0: return "Coder in imported document";
				case 1: return "Mapped to coder in master document";
				default: return null;
			}
		}
		
		public int getRowCount() {
			int count = coderForeign.size();
			return count;
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch( columnIndex ){
				case 0:	return coderForeign.get(rowIndex);
				case 1: return coderDomestic.get(rowIndex);
				default: return null;
			}
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return false;
			} else {
				return true;
			}
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Coder coder = (Coder) aValue;
			if (columnIndex == 0) {
				coderForeign.set(rowIndex, coder);
			} else {
				coderDomestic.set(rowIndex, coder);
			}
			fireTableDataChanged();
		}
		
		void clear() {
			coderForeign.clear();
			coderDomestic.clear();
			fireTableDataChanged();
		}
		
		void addCoderPair(Coder foreign, Coder domestic) {
			coderForeign.add(foreign);
			coderDomestic.add(domestic);
			fireTableDataChanged();
		}
	}
	
	private class DocumentTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 7098098353712215778L;
		ArrayList<TableDocument> documents = new ArrayList<TableDocument>();
		ArrayList<Boolean> selections = new ArrayList<Boolean>();
		
		public int getColumnCount() {
			return 5;
		}
		
		public int getRowCount() {
			return documents.size();
		}
		
		public String getColumnName(int column) {
			switch (column) {
				case 0: return "Import?";
				case 1: return "Title";
				case 2: return "Statements";
				case 3: return "Coder";
				case 4: return "Date";
				default: return null;
			}
		}
		
		public TableDocument getTableDocument(int index) {
			return this.documents.get(index);
		}
		
		public boolean isSelected(int index) {
			return this.selections.get(index);
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex < 0 || rowIndex > this.documents.size() - 1 || this.documents.size() == 0 || columnIndex > getColumnCount()) {
				return null;
			} else if (columnIndex == 0) {
				return selections.get(rowIndex);
			} else if (columnIndex == 1) {
				return documents.get(rowIndex).getTitle();
			} else if (columnIndex == 2) {
				return documents.get(rowIndex).getFrequency();
			} else if (columnIndex == 3) {
				return documents.get(rowIndex).getCoder();
			} else if (columnIndex == 4) {
				LocalDateTime d = documents.get(rowIndex).getDateTime();
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MM yyyy, hh:mm:ss");
				String dateString = d.format(dateTimeFormatter);
				return dateString;
			} else {
				return null;
			}
		}
		
		public Class<?> getColumnClass(int columnIndex) {
			switch( columnIndex ){
				case 0: return Boolean.class;
				case 1: return String.class;
				case 2: return int.class;
				case 3: return Coder.class;
				case 4: return String.class;
				default: return null;
			}	
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch( columnIndex ){
				case 0: return true;
				case 1: return false;
				case 2: return false;
				case 3: return false;
				case 4: return false;
				default: return false;
			}
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				selections.set(rowIndex, (Boolean)aValue);
			}
			fireTableDataChanged();
		}

		/**
		 * Delete all {@link TableDocument} objects from the table model and notify
		 * the listeners.
		 */
		void clear() {
			documents.clear();
			selections.clear();
			fireTableDataChanged();
		}
		
		void addDocuments(List<TableDocument> chunks) {
			this.documents.addAll(chunks);
			for (int i = 0; i < chunks.size(); i++) {
				this.selections.add(true);
			}
			fireTableDataChanged();
		}
	}

	/**
	 * Swing worker class for loading documents from the database and adding
	 * them to the document table in a background thread.
	 */
	private class DocumentTableRefreshWorker extends SwingWorker<List<TableDocument>, TableDocument> {
		
		/**
		 * Create a new document table swing worker.
		 */
		private DocumentTableRefreshWorker() {
			dbButton.setEnabled(false);
			filterButton.setEnabled(false);
			selectAll.setEnabled(false);
			importButton.setEnabled(false);
			dtm.clear();
		}
		
		@Override
		protected List<TableDocument> doInBackground() {
			try (SqlResults s = Importer.this.sql.getTableDocumentResultSet(); // result set and connection are automatically closed when done because SqlResults implements AutoCloseable
					ResultSet rs = s.getResultSet();) {
				while (rs.next()) {
					TableDocument r = new TableDocument(
							rs.getInt("ID"),
							rs.getString("Title"),
							rs.getInt("Frequency"),
							new Coder(rs.getInt("CoderId"),
									rs.getString("CoderName"),
									new Color(rs.getInt("Red"), rs.getInt("Green"), rs.getInt("Blue"))),
							rs.getString("Author"),
							rs.getString("Source"),
							rs.getString("Section"),
							rs.getString("Type"),
							rs.getString("Notes"),
							LocalDateTime.ofEpochSecond(rs.getLong("Date"), 0, ZoneOffset.UTC));
					if (Importer.this.sql.getActiveCoder().isPermissionImportDocuments() && 
							(Importer.this.sql.getActiveCoder().getId() == r.getCoder().getId() || Importer.this.sql.getActiveCoder().getCoderRelations().get(r.getCoder().getId()).isViewDocuments() && Importer.this.sql.getActiveCoder().isPermissionViewOthersDocuments()) &&
							Dna.sql.getActiveCoder().isPermissionImportDocuments()) {
						publish(r); // send the new document row out of the background thread
					}
				}
			} catch (SQLException e) {
				LogEvent le = new LogEvent(Logger.WARNING,
						"[SQL] Failed to retrieve documents from database for import.",
						"The import document table model swing worker tried to retrieve all documents from the selected database to display them in the importer document table, but some or all documents could not be retrieved because there was a problem while processing the result set. The resulting document table may be incomplete.",
						e);
				dna.Dna.logger.log(le);
			}
			return null;
		}
	    
	    @Override
	    protected void process(List<TableDocument> chunks) {
	    	dtm.addDocuments(chunks);
	    }

	    @Override
	    protected void done() {
			dbButton.setEnabled(true);
			filterButton.setEnabled(true);
			selectAll.setEnabled(true);
			importButton.setEnabled(true);
	    }
	}
	
	private class ImportWorker extends SwingWorker<List<Document>, Document> {
		HashMap<Integer, Integer> coderMap;
		ArrayList<Integer> docIds;
		
		/**
		 * Create a new document import swing worker.
		 */
		private ImportWorker() {
			// disable buttons
			dbButton.setEnabled(false);
			filterButton.setEnabled(false);
			selectAll.setEnabled(false);
			importButton.setEnabled(false);
			
			// create coder hash map for easier look-up of corresponding coder ID
			coderMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < coderTableModel.getRowCount(); i++) {
				coderMap.put(((Coder) coderTableModel.getValueAt(i, 0)).getId(), ((Coder) coderTableModel.getValueAt(i, 1)).getId());
			}
			
			// some preprocessing: compile list of document IDs to import after skipping unselected/empty/full/unmapped documents
			docIds = new ArrayList<Integer>();
			for (int i = 0; i < dtm.getRowCount(); i++) {
				if (Importer.this.sql.getActiveCoder().isPermissionImportDocuments() && 
						Dna.sql.getActiveCoder().isPermissionImportDocuments() &&
						dtm.isSelected(i)) {
					TableDocument td = dtm.getTableDocument(i);
					if (!(td.getFrequency() == 0 && skipEmptyBox.isSelected()) &&
							!(td.getFrequency() > 0 && skipFullBox.isSelected()) &&
							!(coderDocumentBox.isSelected() && coderMap.get(td.getCoder().getId()) != Dna.sql.getActiveCoder().getId())) {
						docIds.add(td.getId());
					}
				}
			}
		}
		
		@Override
		protected List<Document> doInBackground() {
			String documentSelectSql = "SELECT * FROM DOCUMENTS WHERE ID IN (";
			for (int i = 0; i < docIds.size(); i++) {
				documentSelectSql = documentSelectSql + docIds.get(i);
				if (i < docIds.size() - 1) {
					documentSelectSql = documentSelectSql + ", ";
				}
			}
			documentSelectSql = documentSelectSql + ");";
			
			try (Connection connForeign = Importer.this.sql.getDataSource().getConnection();
					Connection connDomestic = Dna.sql.getDataSource().getConnection();
					PreparedStatement s1 = connDomestic.prepareStatement("INSERT INTO DOCUMENTS (Title, Text, Coder, Author, Source, Section, Notes, Type, Date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
					PreparedStatement s2 = connForeign.prepareStatement(documentSelectSql);
					PreparedStatement s3 = connDomestic.prepareStatement("SELECT COUNT(ID) FROM DOCUMENTS WHERE Title = ? AND Text = ?;");
					SQLCloseable finish = connDomestic::rollback;) {
				connDomestic.setAutoCommit(false);
				
				/*
				 * TODO:
				 * - match statement types and put in hash map; import unknown statement types
				 * - import regexes
				 * - for each document, check if statements should be imported (if any)
				 * - statement coder check
				 * - for each statement, check entities and create; merge attributes depending on settings
				 */
				
				// process documents
				if (docIds.size() > 0) {
					ResultSet r1 = s2.executeQuery(); // select documents
					while (r1.next()) {
						
						// check for duplicate title and text if necessary
						boolean proceed = true;
						if (skipDuplicatesBox.isSelected()) {
							s3.setString(1, r1.getString("Title"));
							s3.setString(2, r1.getString("Text"));
							ResultSet r2 = s3.executeQuery();
							while (r2.next()) {
								 if (r2.getInt(0) > 0) {
									 proceed = false;
								 }
							}
						}
						
						// insert document
						if (proceed) {
							// fix date if necessary
							LocalDateTime date = LocalDateTime.ofEpochSecond(r1.getLong("Date"), 0, ZoneOffset.UTC);
							if (fixDatesBox.isSelected()) {
								if (date.truncatedTo(ChronoUnit.DAYS).isBefore(date.plusHours(12).truncatedTo(ChronoUnit.DAYS))) {
									date = date.plusHours(12).truncatedTo(ChronoUnit.DAYS);
								} else {
									date = date.truncatedTo(ChronoUnit.DAYS);
								}
							}
							// extract remaining document details and insert document into domestic database
							s1.setString(1, r1.getString("Title"));
							s1.setString(2, r1.getString("Text"));
							s1.setInt(3, coderMap.get(r1.getInt("Coder"))); // replace by mapped coder
							s1.setString(4, r1.getString("Author"));
							s1.setString(5, r1.getString("Source"));
							s1.setString(6, r1.getString("Section"));
							s1.setString(7, r1.getString("Notes"));
							s1.setString(8, r1.getString("Type"));
							s1.setLong(10, date.toEpochSecond(ZoneOffset.UTC));
							s1.executeUpdate();
							
							// get generated document ID
							ResultSet keySetDocument = s1.getGeneratedKeys();
							int documentId = -1;
							while (keySetDocument.next()) {
								documentId = keySetDocument.getInt(1);
							}
							
							// import statements contained in the document
							if (importStatementsBox.isSelected()) {
								// TODO
							}
						}
					}
				}
				
				connDomestic.commit();
			} catch (SQLException e) {
				LogEvent le = new LogEvent(Logger.WARNING,
						"[SQL] Failed to retrieve documents from database for import.",
						"The import document table model swing worker tried to retrieve all documents from the selected database to display them in the importer document table, but some or all documents could not be retrieved because there was a problem while processing the result set. The resulting document table may be incomplete.",
						e);
				dna.Dna.logger.log(le);
			}
			return null;
		}
	    
	    @Override
	    protected void process(List<Document> chunks) {
	    	// TODO
	    }

	    @Override
	    protected void done() {
			dbButton.setEnabled(true);
			filterButton.setEnabled(true);
			selectAll.setEnabled(true);
			importButton.setEnabled(true);
	    }
	}
}