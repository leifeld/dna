package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import model.StatementType;
import model.TableDocument;
import model.Value;
import sql.ConnectionProfile;
import sql.Sql;
import sql.Sql.SQLCloseable;

/**
 * A class for importing documents and statements from another database,
 * including statement types, entities, attributes, attribute variables, and
 * regular expressions if required. The class uses a dialog window to select
 * documents for import and maps foreign to domestic coders and let the user
 * select a range of options for the import. A separate background thread does
 * the actual import work.
 */
class Importer extends JDialog {
	private static final long serialVersionUID = -5295303422543731461L;
	private JButton dbButton, filterButton, selectAll, cancelButton, importButton;
	private ImportDocumentTableModel idtm;
	private CoderTableModel coderTableModel;
	private ArrayList<Coder> domesticCoders;
	private JTable documentTable;
	private Sql sql;
	private int version;
	private JCheckBox importStatementsBox, statementTypeBox, skipFullBox, skipEmptyBox, coderDocumentBox, coderStatementBox, skipDuplicatesBox, fixDatesBox, mergeAttributesBox, overwriteAttributesBox, importEntitiesBox, importRegexBox;

	/**
	 * Constructor of the Importer class. Creates a new instance of the dialog
	 * window and creates the GUI with all action listeners.
	 */
	public Importer(Frame parent) {
		super(parent, "Import", true);
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
        column.setCellEditor(new DefaultCellEditor(comboBox));
        CoderTableCellRenderer coderTableCellRenderer = new CoderTableCellRenderer();
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
		importStatementsBox.setToolTipText("<html><p width=\"500\">If checked, statements will be imported. "
				+ "If unchecked, only the documents will be imported, without the statements contained in "
				+ "these documents.</p></html>");
		checkBoxPanel.add(importStatementsBox, g);

		g.gridy = 1;
		statementTypeBox = new JCheckBox("Include unknown statement types in import");
		statementTypeBox.setSelected(true);
		statementTypeBox.setToolTipText("<html><p width=\"500\">The source database may have statement "
				+ "types that are not present in the current/target database. If checked, these foreign "
				+ "statement types will be created as new statement types in the current/target database, "
				+ "and the statements corresponding to these statement types will be imported. The import "
				+ "process checks if the name and all variables are identical between the two databases to "
				+ "establish whether a statement type in the source database can be mapped onto a statement "
				+ "type in the current/target database. This also means that if you add a new variable to "
				+ "the default statement types (such as to the DNA Statement definition) in the source "
				+ "database, this will lead to the creation of a new statement type in the current/target "
				+ "database if that exact statement type definition is not found in the current/target "
				+ "database. If you want to map the statements to an existing statement type, you would "
				+ "need to delete superfluous variables and rename the statement type and variables such "
				+ "that it maps exactly onto an existing statement type. The color and ID of statement types "
				+ "are not taken into account for the comparison.</p></html>");
		checkBoxPanel.add(statementTypeBox, g);

		g.gridy = 2;
		skipFullBox = new JCheckBox("Skip documents with statements");
		skipFullBox.setSelected(false);
		skipFullBox.setToolTipText("<html><p width=\"500\">If checked, only documents will be imported that "
				+ "do not contain any statements. If unchecked, all selected documents will be imported "
				+ "(subject to the remaining import options).</p></html>");
		checkBoxPanel.add(skipFullBox, g);

		g.gridy = 3;
		skipEmptyBox = new JCheckBox("Skip documents without statements");
		skipEmptyBox.setSelected(false);
		skipEmptyBox.setToolTipText("<html><p width=\"500\">If checked, only documents will be imported that "
				+ "contain at least one statement. If unchecked, all selected documents will be imported "
				+ "(subject to the remaining import options).</p></html>");
		checkBoxPanel.add(skipEmptyBox, g);

		g.gridy = 4;
		coderDocumentBox = new JCheckBox("Skip documents not mapped to active coder");
		coderDocumentBox.setSelected(false);
		coderDocumentBox.setToolTipText("<html><p width=\"500\">If checked, only those documents among the "
				+ "selected documents are imported that are mapped onto the currently active coder in the "
				+ "table at the top of the dialog window. That is, documents mapped to another coder will "
				+ "be ignored. If unchecked, all selected documents will be imported (subject to the "
				+ "remaining import options). Note that document ownership will be recoded during import "
				+ "according to the coder mapping table at the top of the dialog window.</p></html>");
		checkBoxPanel.add(coderDocumentBox, g);

		g.gridy = 5;
		coderStatementBox = new JCheckBox("Skip statements not mapped to active coder");
		coderStatementBox.setSelected(false);
		coderStatementBox.setToolTipText("<html><p width=\"500\">If checked, only those statements among the "
				+ "selected statements are imported that are mapped onto the currently active coder in the "
				+ "table at the top of the dialog window. That is, statements mapped to another coder will "
				+ "be ignored. If unchecked, all statements will be imported (subject to the remaining import "
				+ "options). Note that statement ownership will be recoded during import according to the "
				+ "coder mapping table at the top of the dialog window.</p></html>");
		checkBoxPanel.add(coderStatementBox, g);

		g.gridy = 6;
		skipDuplicatesBox = new JCheckBox("Skip documents with identical title and text");
		skipDuplicatesBox.setSelected(false);
		skipDuplicatesBox.setToolTipText("<html><p width=\"500\">If checked, documents in the source database "
				+ "(including the statements contained in these documents) are omitted from the import "
				+ "process if a document with the same title and text exists in the current/target database. "
				+ "If unchecked, they will be inserted anyway as new documents.</p></html>");
		checkBoxPanel.add(skipDuplicatesBox, g);

		g.gridy = 7;
		fixDatesBox = new JCheckBox("Round date/time to nearest date at 00:00");
		fixDatesBox.setSelected(false);
		fixDatesBox.setToolTipText("<html><p width=\"500\">If checked, the time stamp of the documents will be "
				+ "erased and set to 00:00 at the nearest full day. For example, if a document has the date "
				+ "24 June 2011 and a time of 11:36, the date/time will be rounded to 24 June 2011 at 00:00. "
				+ "If a document has the date 24 June 2011 and a time of 13:15, the date/time will be rounded "
				+ "to 25 June 2011 at 00:00. In some older DNA versions, the date/time stamp was relative to "
				+ "the local time zone, meaning that reopening the database in a different time zone could "
				+ "change the date. Checking this box fixes these imprecisions by rounding the dates.</p></html>");
		checkBoxPanel.add(fixDatesBox, g);

		g.gridy = 8;
		mergeAttributesBox = new JCheckBox("Merge attributes upon import");
		mergeAttributesBox.setSelected(true);
		mergeAttributesBox.setToolTipText("<html><p width=\"500\">The source database may have different attribute "
				+ "variables from the current/target database. If checked, any attribute variables from the "
				+ "source database will also be copied into the target database if they do not exist yet.</p>"
				+ "<p width=\"500\">The source database may also contain attribute values where the current/target "
				+ "database contains empty values. For example, if the source database contains statements that "
				+ "have the same person as some statements in the current/target database and if the source "
				+ "database has a 'type' or 'alias' defined for person while the current/target database does not, "
				+ "then the attribute value will be transferred into the current/target database as well, without "
				+ "overwriting any existing attribute values (unless the 'Overwrite attribute data on conflict' "
				+ "option is selected) if this box is checked.</p>"
				+ "<p width=\"500\">If not checked, then attribute values in the source database will be ignored "
				+ "and not transferred into the current/target database.</p></html>");
		checkBoxPanel.add(mergeAttributesBox, g);

		g.gridy = 9;
		overwriteAttributesBox = new JCheckBox("Overwrite attribute data on conflict");
		overwriteAttributesBox.setSelected(false);
		overwriteAttributesBox.setToolTipText("<html><p width=\"500\">If checked (and if the previous option is "
				+ "checked as well), attributes will not only be merged into the current/target database when "
				+ "there is no attribute value present in the current/target database, but any existing "
				+ "attribute values in the current/target database will be overwritten with corresponding "
				+ "attribute values from the source database if both databases contain attribute values for "
				+ "the same entity and the same attribute variable. That is, the source attribute value takes "
				+ "priority over the existing attribute data. If there is no attribute value in the source but "
				+ "there is one present in the current/target database, the existing attribute value in the "
				+ "current/target database will not be changed. If unchecked, attribute data are not overwritten "
				+ "upon conflict and will be imported only if there is no existing attribute value in the "
				+ "current/target database for the respective entity and attribute variable.</p></html>");
		checkBoxPanel.add(overwriteAttributesBox, g);

		g.gridy = 10;
		importEntitiesBox = new JCheckBox("Import unused entities");
		importEntitiesBox.setSelected(false);
		importEntitiesBox.setToolTipText("<html><p width=\"500\">In DNA, it is possible to add entities to "
				+ "a variable that do not have any instances in any of the coded statements. For example, one "
				+ "could add a list of persons before starting the coding process. These unused entities are "
				+ "highlighted in red in the attribute manager. Used entities can also become unused if all "
				+ "statements that contain instances of them are deleted. If this box is checked, all unused "
				+ "entities are imported from the source database into the current/target database, "
				+ "irrespective of document or statement or statement type, for all variables. If unchecked, "
				+ "unused entities will not be imported.</p></html>");
		checkBoxPanel.add(importEntitiesBox, g);

		g.gridy = 11;
		importRegexBox = new JCheckBox("Import regex keywords");
		importRegexBox.setSelected(true);
		importRegexBox.setToolTipText("<html><p width=\"500\">If checked, regular expressions (to be "
				+ "highlighted in the text) are imported from the source database into the current/target "
				+ "database. Regex terms that already exist (even if in a different color) are ignored during "
				+ "import. If unchecked, no regex terms are imported.</p></html>");
		checkBoxPanel.add(importRegexBox, g);

		CompoundBorder borderCheckBoxes;
		borderCheckBoxes = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Import options"));
		checkBoxPanel.setBorder(borderCheckBoxes);
		
		northPanel.add(checkBoxPanel, BorderLayout.EAST);
		panel.add(northPanel, BorderLayout.NORTH);
		
		// document table panel
		idtm = new ImportDocumentTableModel();
		documentTable = new JTable(idtm);
		documentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        documentTable.setDefaultRenderer(Coder.class, new CoderTableCellRenderer());
		
		documentTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		documentTable.getColumnModel().getColumn(1).setPreferredWidth(350);
		documentTable.getColumnModel().getColumn(2).setPreferredWidth(40);
		documentTable.getColumnModel().getColumn(3).setPreferredWidth(80);
		documentTable.getColumnModel().getColumn(4).setPreferredWidth(130);
		documentTable.getTableHeader().setReorderingAllowed(false);
		
		// allow rows to be sorted by column by pressing on column header
		TableRowSorter<ImportDocumentTableModel> sorter = new TableRowSorter<ImportDocumentTableModel>(idtm);
		documentTable.setRowSorter(sorter);

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

		ImageIcon dbIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database-import.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		dbButton = new JButton("Select database...", dbIcon);
		dbButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(Importer.this);
				NewDatabaseDialog n = new NewDatabaseDialog(frame, true);
				ConnectionProfile cp = n.getConnectionProfile();
				if (cp == null) {
					LogEvent l = new LogEvent(Logger.MESSAGE,
							"[GUI] Action executed: could not connect to database for document import.",
							"Started opening a database connection from the GUI for document import, but the connection was not established.");
					dna.Dna.logger.log(l);
				} else {
					Sql s = new Sql(cp, false, true);
					
					String v = s.getVersion();
					if (v.startsWith("3")) {
						Importer.this.version = 3;
					} else if (v.startsWith("2")) {
						Importer.this.version = 2;
					}
					
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
					Importer.this.sql = s;
					(new DocumentTableRefreshWorker()).execute();
				}
			}
		});
		buttonPanel.add(dbButton);

		ImageIcon filterIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-filter.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
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
					for (int i = 0; i < idtm.getRowCount(); i++) {
						Pattern p = Pattern.compile(s);
	    				Matcher m = p.matcher(idtm.getValueAt(i, 1).toString());
	    				boolean b = m.find();
	    				if (b == true) {
	    					idtm.setValueAt(true, i, 0);
	    				} else {
	    					idtm.setValueAt(false, i, 0);
	    				}
					}
				}
			}
		});
		filterButton.setEnabled(false);
		buttonPanel.add(filterButton);

		ImageIcon selectIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-checks.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		selectAll = new JButton("(Un)select all", selectIcon);
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((Boolean) idtm.getValueAt(0, 0) == false) {
					for (int i = 0; i < idtm.getRowCount(); i++) {
						idtm.setValueAt(true, i, 0);
					}
				} else {
					for (int i = 0; i < idtm.getRowCount(); i++) {
						idtm.setValueAt(false, i, 0);
					}
				}
				
			}
		});
		selectAll.setEnabled(false);
		buttonPanel.add(selectAll);

		ImageIcon cancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		cancelButton = new JButton("Cancel / close", cancelIcon);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Importer.this.dispose();
			}
		});
		buttonPanel.add(cancelButton);

		ImageIcon okIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database-import.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		importButton = new JButton("Import selected", okIcon);
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Importer.this, 
						"Are you sure you want to insert the selected \n" +
						"documents into the currently open DNA database?", 
						"Confirmation", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					try {
						Thread importThread = new Thread( new ImportWorker(), "Import data" );
						importThread.start();
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
				}
			}
		});
		importButton.setEnabled(false);
		buttonPanel.add(importButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		this.add(panel);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Table model for matching coders from a foreign database with coders from
	 * the currently open ("domestic") database.
	 */
	private class CoderTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 6251543822435460514L;
		
		/**
		 * A list of coders in a foreign database.
		 */
		private ArrayList<Coder> coderForeign = new ArrayList<Coder>();
		
		/**
		 * A list of coders, possibly including duplicates, taken from the
		 * currently open ("domestic") database. Each item in the list indicates
		 * which coder in the domestic database a foreign coder from the foreign
		 * coder list is translated into.
		 */
		private ArrayList<Coder> coderDomestic = new ArrayList<Coder>();
		
		/**
		 * Get the class of a table column.
		 * 
		 * @param columnIndex  The column index.
		 * @return             The class of the column.
		 */
		public Class<?> getColumnClass(int columnIndex) {
			return Coder.class;
		}
		
		/**
		 * Get the number of columns.
		 * 
		 * @return  The column count.
		 */
		public int getColumnCount() {
			return 2;
		}
		
		/**
		 * Get the name of a column for the header.
		 * 
		 * @param column  The column index.
		 * @return        The name of the column.
		 */
		public String getColumnName(int column) {
			switch( column ){
				case 0: return "Coder in imported document";
				case 1: return "Mapped to coder in master document";
				default: return null;
			}
		}
		
		/**
		 * Get the number of rows in the table.
		 * 
		 * @return  The row count.
		 */
		public int getRowCount() {
			int count = coderForeign.size();
			return count;
		}
		
		/**
		 * Get the value represented by a cell.
		 * 
		 * @param rowIndex     The row index of the cell.
		 * @param columnIndex  The column index of the cell.
		 * @return             The object corresponding to the cell.
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch( columnIndex ){
				case 0:	return coderForeign.get(rowIndex);
				case 1: return coderDomestic.get(rowIndex);
				default: return null;
			}
		}
		
		/**
		 * Is the cell editable?
		 * 
		 * @param rowIndex     The row index of the cell.
		 * @param columnIndex  The column index of the cell.
		 * @return             Indicator of whether the cell can be edited.
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return false;
			} else {
				return true;
			}
		}
		
		/**
		 * Set the contents of a cell.
		 * 
		 * @param aValue       The new object to store in the cell.
		 * @param rowIndex     The row index of the cell.
		 * @param columnIndex  The column index of the cell.
		 */
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Coder coder = (Coder) aValue;
			if (columnIndex == 0) {
				coderForeign.set(rowIndex, coder);
			} else {
				coderDomestic.set(rowIndex, coder);
			}
			fireTableDataChanged();
		}
		
		/**
		 * Remove all items from the table by clearing the foreign and domestic
		 * lists of coders.
		 */
		void clear() {
			coderForeign.clear();
			coderDomestic.clear();
			fireTableDataChanged();
		}
		
		/**
		 * Add a foreign coder and its corresponding domestic coder to the list.
		 * 
		 * @param foreign   The foreign {@link model.Coder Coder}.
		 * @param domestic  The domestic {@link model.Coder Coder}.
		 */
		void addCoderPair(Coder foreign, Coder domestic) {
			coderForeign.add(foreign);
			coderDomestic.add(domestic);
			fireTableDataChanged();
		}
	}
	
	/**
	 * A table model for the documents in the GUI. It is light-weight because it
	 * does not save the text of the documents. For this purpose, it uses the
	 * {@link model.TableDocument TableDocument} class.
	 */
	private class ImportDocumentTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 7098098353712215778L;
		
		/**
		 * An array list with {@link model.TableDocument TableDocument} objects
		 * that represent the documents of the foreign database in a
		 * light-weight way (i.e., without text).
		 */
		private ArrayList<TableDocument> documents = new ArrayList<TableDocument>();
		
		/**
		 * An array list that stores for each document whether it has been
		 * selected for inclusion in the import.
		 */
		private ArrayList<Boolean> selections = new ArrayList<Boolean>();
		
		/**
		 * Get the number of columns in the table.
		 * 
		 * @return Number of columns.
		 */
		public int getColumnCount() {
			return 5;
		}
		
		/**
		 * Get the number of rows in the table.
		 * 
		 * @return Number of rows.
		 */
		public int getRowCount() {
			return documents.size();
		}
		
		/**
		 * Get the name of a column in the table.
		 * 
		 * @param column  The index of the column, starting with 0.
		 * @return        The name of the column.
		 */
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
		
		/**
		 * Get the table document corresponding to a row index in the table.
		 * 
		 * @param index  The row index.
		 * @return       A {@link model.TableDocument TableDocument} object.
		 */
		public TableDocument getTableDocument(int index) {
			return this.documents.get(index);
		}
		
		/**
		 * Is the document represented by the table row selected for import?
		 * 
		 * @param index  The row index of the document in the table.
		 * @return       A boolean value indicating if the document will be
		 *   imported.
		 */
		public boolean isSelected(int index) {
			return this.selections.get(index);
		}
		
		/**
		 * Get the value in a table cell.
		 * 
		 * @param rowIndex     The row index.
		 * @param columnIndex  The column index.
		 * @return             The object in the table cell.
		 */
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
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss");
				String dateString = d.format(dateTimeFormatter);
				return dateString;
			} else {
				return null;
			}
		}
		
		/**
		 * Get the class of a column in the table.
		 * 
		 * @param columnIndex  The index of the column.
		 * @return             The class of the column.
		 */
		public Class<?> getColumnClass(int columnIndex) {
			switch( columnIndex ){
				case 0: return Boolean.class;
				case 1: return String.class;
				case 2: return Integer.class;
				case 3: return Coder.class;
				case 4: return String.class;
				default: return null;
			}	
		}
		
		/**
		 * Is the cell editable?
		 * 
		 * @param rowIndex     The index of the row of the cell.
		 * @param columnIndex  The index of the column of the cell.
		 * @return             Indicates if the cell is editable.
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0: return true;
				case 1: return false;
				case 2: return false;
				case 3: return false;
				case 4: return false;
				default: return false;
			}
		}
		
		/**
		 * Set the value of a cell in the table.
		 * 
		 * @param aValue       The new value for the cell.
		 * @param rowIndex     The row index for the cell.
		 * @param columnIndex  The column index for the cell.
		 */
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				selections.set(rowIndex, (Boolean)aValue);
			}
			fireTableDataChanged();
		}

		/**
		 * Delete all {@link model.TableDocument TableDocument} objects
		 * including their selection indicators from the table model and notify
		 * the listeners.
		 */
		void clear() {
			documents.clear();
			selections.clear();
			fireTableDataChanged();
		}
		
		/**
		 * Add a chunk of new documents to the table model, including selection
		 * values, which are by default true.
		 * 
		 * @param chunks  A list of {@link model.TableDocument TableDocument}
		 *   objects for addition to the table.
		 */
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
			idtm.clear();
		}
		
		@Override
		protected List<TableDocument> doInBackground() {
			try (Connection conn = Importer.this.sql.getDataSource().getConnection();
					PreparedStatement s = conn.prepareStatement("SELECT D.ID, Title, (SELECT COUNT(ID) FROM STATEMENTS WHERE DocumentId = D.ID) AS Frequency, C.ID AS CoderId, Name AS CoderName, Red, Green, Blue, Date, Author, Source, Section, Type, Notes FROM CODERS C INNER JOIN DOCUMENTS D ON D.Coder = C.ID;");
					ResultSet rs = s.executeQuery();) {
				LocalDateTime dateTime;
				Date dateV2;
				while (rs.next()) {
					if (Importer.this.version == 2) {
						dateV2 = new Date(rs.getLong("Date"));
						dateTime = Instant.ofEpochMilli(dateV2.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
					} else {
						dateTime = LocalDateTime.ofEpochSecond(rs.getLong("Date"), 0, ZoneOffset.UTC);
					}
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
							dateTime);
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
	    	idtm.addDocuments(chunks);
	    }

	    @Override
	    protected void done() {
			dbButton.setEnabled(true);
			if (idtm.getRowCount() > 0) {
				filterButton.setEnabled(true);
				selectAll.setEnabled(true);
				importButton.setEnabled(true);
			} else {
				filterButton.setEnabled(false);
				selectAll.setEnabled(false);
				importButton.setEnabled(false);
			}
	    }
	}

	/**
	 * An import thread that attempts to read in the necessary data from the
	 * foreign database in the background and insert them into the current DNA
	 * database.
	 */
	private class ImportWorker implements Runnable {
		HashMap<Integer, Integer> coderMap;
		ArrayList<Integer> docIds;
		ProgressMonitor progressMonitor;

		/**
		 * Start a new import worker. This constructor creates a coder hash map
		 * for easier lookup later and filters out some document IDs depending
		 * on the settings in the GUI.
		 */
		public ImportWorker() {
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
			for (int i = 0; i < idtm.getRowCount(); i++) {
				if (Importer.this.sql.getActiveCoder().isPermissionImportDocuments() && 
						Dna.sql.getActiveCoder().isPermissionImportDocuments() &&
						idtm.isSelected(i)) {
					TableDocument td = idtm.getTableDocument(i);
					if (!(td.getFrequency() == 0 && skipEmptyBox.isSelected()) &&
							!(td.getFrequency() > 0 && skipFullBox.isSelected()) &&
							!(coderDocumentBox.isSelected() && coderMap.get(td.getCoder().getId()) != Dna.sql.getActiveCoder().getId())) {
						docIds.add(td.getId());
					}
				}
			}
		}

		/**
		 * Execute the background tasks and try to import the data into the
		 * current database.
		 */
		@SuppressWarnings("resource")
		public void run() {
			long time = System.nanoTime(); // take the time to compute later how long the updating took
			
			progressMonitor = new ProgressMonitor(Importer.this, "Preparing data for import", "(1/5) Processing regex keywords...", 0, 5);
			progressMonitor.setMillisToDecideToPopup(1);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			progressMonitor.setProgress(0);
			
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
					PreparedStatement d1 = connDomestic.prepareStatement("INSERT INTO DOCUMENTS (Title, Text, Coder, Author, Source, Section, Notes, Type, Date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
					PreparedStatement f1 = connForeign.prepareStatement(documentSelectSql);
					PreparedStatement d2 = connDomestic.prepareStatement("SELECT COUNT(ID) FROM DOCUMENTS WHERE Title = ? AND Text = ?;");
					PreparedStatement f2 = connForeign.prepareStatement("SELECT * FROM REGEXES;");
					PreparedStatement d3 = connDomestic.prepareStatement("SELECT Label FROM REGEXES;");
					PreparedStatement d4 = connDomestic.prepareStatement("INSERT INTO REGEXES (Label, Red, Green, Blue) VALUES (?, ?, ?, ?);");
					PreparedStatement f3 = connForeign.prepareStatement("SELECT * FROM STATEMENTTYPES");
					PreparedStatement f4 = connForeign.prepareStatement("SELECT * FROM VARIABLES WHERE StatementTypeId = ?;");
					PreparedStatement d5 = connDomestic.prepareStatement("SELECT * FROM STATEMENTTYPES");
					PreparedStatement d6 = connDomestic.prepareStatement("SELECT * FROM VARIABLES WHERE StatementTypeId = ?;");
					PreparedStatement d7 = connDomestic.prepareStatement("INSERT INTO STATEMENTTYPES (Label, Red, Green, Blue) VALUES (?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
					PreparedStatement d8 = connDomestic.prepareStatement("INSERT INTO VARIABLES (Variable, DataType, StatementTypeId) VALUES (?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
					PreparedStatement d11 = connDomestic.prepareStatement("INSERT INTO STATEMENTS (StatementTypeId, DocumentId, Start, Stop, Coder) VALUES (?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
					PreparedStatement f6 = connForeign.prepareStatement("SELECT * FROM STATEMENTS WHERE DocumentId = ?;");
					PreparedStatement f8 = connForeign.prepareStatement("SELECT * FROM DATABOOLEAN WHERE StatementId = ?;");
					PreparedStatement f9 = connForeign.prepareStatement("SELECT * FROM DATAINTEGER WHERE StatementId = ?;");
					PreparedStatement f10 = connForeign.prepareStatement("SELECT * FROM DATALONGTEXT WHERE StatementId = ?;");
					PreparedStatement f11 = connForeign.prepareStatement("SELECT * FROM DATASHORTTEXT WHERE StatementId = ?;");
					PreparedStatement d12 = connDomestic.prepareStatement("INSERT INTO DATABOOLEAN (StatementId, VariableId, Value) VALUES (?, ?, ?);");
					PreparedStatement d13 = connDomestic.prepareStatement("INSERT INTO DATAINTEGER (StatementId, VariableId, Value) VALUES (?, ?, ?);");
					PreparedStatement d14 = connDomestic.prepareStatement("INSERT INTO DATALONGTEXT (StatementId, VariableId, Value) VALUES (?, ?, ?);");
					PreparedStatement d15 = connDomestic.prepareStatement("INSERT INTO DATASHORTTEXT (StatementId, VariableId, Entity) VALUES (?, ?, ?);");
					PreparedStatement f15 = connForeign.prepareStatement("SELECT COUNT(ID) FROM STATEMENTS WHERE DocumentId = ?;");
					PreparedStatement d16 = connDomestic.prepareStatement("INSERT INTO ATTRIBUTEVARIABLES (VariableId, AttributeVariable) VALUES (?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
					PreparedStatement d17 = connDomestic.prepareStatement("INSERT INTO ATTRIBUTEVALUES (EntityId, AttributeVariableId, AttributeValue) VALUES (?, ?, ?);");
					PreparedStatement d18 = connDomestic.prepareStatement("SELECT * FROM ATTRIBUTEVARIABLES WHERE VariableId = ?;");
					PreparedStatement d19 = connDomestic.prepareStatement("SELECT ID FROM ATTRIBUTEVARIABLES WHERE VariableId = ? AND AttributeVariable = ?;");
					PreparedStatement d20 = connDomestic.prepareStatement("INSERT INTO ENTITIES (VariableId, Value, Red, Green, Blue) VALUES (?, ?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
					PreparedStatement d21 = connDomestic.prepareStatement("SELECT ID FROM ENTITIES WHERE VariableId = ? AND Value = ?;");
					PreparedStatement d22 = connDomestic.prepareStatement("UPDATE ATTRIBUTEVALUES SET AttributeValue = ? WHERE EntityId = ? AND AttributeVariableId = ?;");
					PreparedStatement d23 = connDomestic.prepareStatement("SELECT AttributeValue FROM ATTRIBUTEVALUES WHERE EntityId = ? AND AttributeVariableId = ?;");
					SQLCloseable finish = connDomestic::rollback;) {

				LogEvent le1 = new LogEvent(Logger.MESSAGE,
						"[SQL] Initializing thread to import data: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").",
						"A new thread has been started to import data from another database into the current database in the background: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").");
				Dna.logger.log(le1);
				connDomestic.setAutoCommit(false);
				ResultSet r1, r2, r3, r4;
				
				// create statement for getting short text values when using DNA 2.0
				PreparedStatement f5 = null,
						f7 = null,
						f12 = null,
						f13 = null,
						f14V2 = null,
						f16 = null,
						f16V2 = null,
						d9 = null,
						d10 = null;
				
				// put selected document IDs into a string to use in an SQL query
				String docIdString = "";
				for (int i = 0; i < docIds.size(); i++) {
					docIdString = docIdString + docIds.get(i);
					if (i < docIds.size() - 1) {
						docIdString = docIdString + ", ";
					}
				}
				
				if (Importer.this.version == 2) {
					// get attributes vom DNA 2.0 dataset and check if the attribute is used in the dataset
					f14V2 = connForeign.prepareStatement("SELECT A.*, " + 
							"CASE WHEN EXISTS (SELECT ID from DATASHORTTEXT D WHERE D.Value = A.Value AND D.VariableId = A.VariableId) " + 
							"THEN 1 " + 
							"ELSE 0 " + 
							"END AS InDatabase " + 
							"FROM ATTRIBUTES A WHERE A.VariableId = ?;");
					
					// for a given attribute ID, get a 1 if the attribute is used in one of the selected documents and 0 otherwise
					String s = "SELECT DISTINCT COUNT(DocumentId) > 0 "
							+ "FROM ATTRIBUTES A JOIN DATASHORTTEXT D ON D.VariableId = A.VariableId AND D.Value = A.Value "
							+ "JOIN STATEMENTS S ON D.StatementId = S.ID WHERE A.ID = ? AND DocumentId IN (" + docIdString + ");";
					f16V2 = connForeign.prepareStatement(s);
				} else {
					f5 = connForeign.prepareStatement("SELECT * FROM VARIABLELINKS;");
					f7 = connForeign.prepareStatement("SELECT * FROM ATTRIBUTEVARIABLES WHERE VariableId = ?;");
					f12 = connForeign.prepareStatement("SELECT * FROM ATTRIBUTEVALUES;");
					f13 = connForeign.prepareStatement("SELECT E.*, " + 
							"CASE WHEN EXISTS (SELECT ID from DATASHORTTEXT D WHERE D.Entity = E.ID AND D.VariableId = E.VariableId) " + 
							"THEN 1 " + 
							"ELSE 0 " + 
							"END AS InDatabase " + 
							"FROM ENTITIES E WHERE E.VariableId = ?;");
					String s = "SELECT DISTINCT COUNT(DocumentId) > 0 "
							+ "FROM ENTITIES E JOIN DATASHORTTEXT D ON D.VariableId = E.VariableId AND D.Entity = E.ID "
							+ "JOIN STATEMENTS S ON D.StatementId = S.ID WHERE E.ID = ? AND DocumentId IN (" + docIdString + ");";
					f16 = connForeign.prepareStatement(s);
					d9 = connDomestic.prepareStatement("SELECT COUNT(ID) FROM VARIABLELINKS WHERE SourceVariableId = ? AND TargetVariableId = ?;");
					d10 = connDomestic.prepareStatement("INSERT INTO VARIABLELINKS (SourceVariableId, TargetVariableId) VALUES (?, ?);");
				}
				
				// process regex keywords
				int regexCount = 0;
				if (importRegexBox.isSelected()) {
					ArrayList<String> existingRegexes = new ArrayList<String>();
					r1 = d3.executeQuery();
					while (r1.next()) {
						existingRegexes.add(r1.getString("Label").substring(0, Math.min(190, r1.getString("Label").length())));
					}
					r1.close();
					r1 = f2.executeQuery();
					while (r1.next()) {
						if (!existingRegexes.contains(r1.getString("Label").substring(0, Math.min(190, r1.getString("Label").length())))) {
							d4.setString(1, r1.getString("Label").substring(0, Math.min(190, r1.getString("Label").length())));
							d4.setInt(2, r1.getInt("Red"));
							d4.setInt(3, r1.getInt("Green"));
							d4.setInt(4, r1.getInt("Blue"));
							d4.executeUpdate();
							regexCount++;
						}
					}
					r1.close();
				}
				LogEvent le2 = new LogEvent(Logger.MESSAGE,
						"[SQL]  ├─ Added " + regexCount + " regex keywords to import transaction.",
						"Added " + regexCount + " regex keywords to the import transaction." +
						" The transaction has not been committed yet and will be rolled" +
						" back in the event of an error during further processing of the transaction.");
				Dna.logger.log(le2);
				
				// process statement types; first, create array list of foreign statement types
				progressMonitor.setProgress(1);
				progressMonitor.setNote("(2/5) Entities, attributes, statement types...");
				
				int statementTypeCount = 0;
				int entityCount = 0;
				ArrayList<StatementType> foreignStatementTypes = new ArrayList<StatementType>();
				r1 = f3.executeQuery();
				while (r1.next()) {
					f4.setInt(1, r1.getInt("ID"));
					r2 = f4.executeQuery();
					ArrayList<Value> variables = new ArrayList<Value>();
					while (r2.next()) {
						variables.add(new Value(r2.getInt("ID"), r2.getString("Variable"), r2.getString("DataType"), ""));
					}
					foreignStatementTypes.add(new StatementType(
							r1.getInt("ID"),
							r1.getString("Label"),
							new Color(r1.getInt("Red"),
									r1.getInt("Green"),
									r1.getInt("Blue")),
							variables));
				}
				r1.close();
				
				// create array list of domestic statement types
				ArrayList<StatementType> domesticStatementTypes = new ArrayList<StatementType>();
				r1 = d5.executeQuery();
				while (r1.next()) {
					d6.setInt(1, r1.getInt("ID"));
					r2 = d6.executeQuery();
					ArrayList<Value> variables = new ArrayList<Value>();
					while (r2.next()) {
						variables.add(new Value(r2.getInt("ID"), r2.getString("Variable"), r2.getString("DataType"), ""));
					}
					r2.close();
					domesticStatementTypes.add(new StatementType(
							r1.getInt("ID"),
							r1.getString("Label"),
							new Color(r1.getInt("Red"),
									r1.getInt("Green"),
									r1.getInt("Blue")),
							variables));
				}
				r1.close();
				
				// compare foreign and domestic types, save correspondence in a hash map, and add new statement types
				HashMap<Integer, Integer> statementTypeMap = new HashMap<Integer, Integer>();
				HashMap<Integer, StatementType> statementTypeIdToTypeMap = new HashMap<Integer, StatementType>(); // reference new domestic statement type by its ID
				HashMap<Integer, Integer> variableMap = new HashMap<Integer, Integer>();
				HashMap<Integer, Integer> attributeVariableMap = new HashMap<Integer, Integer>();
				HashMap<Integer, Integer> entityMap = new HashMap<Integer, Integer>();
				int attributeCount = 0;
				for (int i = 0; i < foreignStatementTypes.size(); i++) {
					for (int j = 0; j < domesticStatementTypes.size(); j++) {
						if (foreignStatementTypes.get(i).getLabel().equals(domesticStatementTypes.get(j).getLabel())) {
							boolean currentEntryNotAMatch = false;
							for (int k = 0; k < foreignStatementTypes.get(i).getVariables().size(); k++) {
								boolean variableExists = false;
								for (int l = 0; l < domesticStatementTypes.get(j).getVariables().size(); l++) {
									if (foreignStatementTypes.get(i).getVariables().get(k).getKey().equals(domesticStatementTypes.get(j).getVariables().get(l).getKey()) &&
											foreignStatementTypes.get(i).getVariables().get(k).getDataType().equals(domesticStatementTypes.get(j).getVariables().get(l).getDataType())) {
										if (!foreignStatementTypes.get(i).getVariables().get(k).getDataType().equals("short text")) {
											variableExists = true;
											break;
										} else {
											boolean differentAttributeVariables = false;
											d18.setInt(1, domesticStatementTypes.get(j).getVariables().get(l).getVariableId());
											r2 = d18.executeQuery();
											ArrayList<String> attributes = new ArrayList<String>();
											while (r2.next()) {
												attributes.add(r2.getString("AttributeVariable"));
											}
											r2.close();
											if (Importer.this.version == 3) {
												f7.setInt(1, foreignStatementTypes.get(i).getVariables().get(k).getVariableId());
												r2 = f7.executeQuery();
												while (r2.next()) {
													if (!attributes.contains(r2.getString("AttributeVariable"))) {
														differentAttributeVariables = true;
														break;
													}
												}
												r2.close();
											} else if (Importer.this.version == 2) { // In DNA 2.0, only three fixed attribute variables were present
												if (!attributes.contains("Type") || !attributes.contains("Alias") || !attributes.contains("Notes")) {
													differentAttributeVariables = true;
													break;
												}
											}
											
											if (!differentAttributeVariables) {
												variableExists = true;
												break;
											}
										}
									}
								}
								if (!variableExists) {
									currentEntryNotAMatch = true;
									break;
								}
							}
							if (!currentEntryNotAMatch) { // put statement type and variable IDs into hash maps to establish correspondence
								statementTypeMap.put(foreignStatementTypes.get(i).getId(), domesticStatementTypes.get(j).getId());
								statementTypeIdToTypeMap.put(domesticStatementTypes.get(j).getId(), domesticStatementTypes.get(j));
								for (int k = 0; k < foreignStatementTypes.get(i).getVariables().size(); k++) {
									for (int l = 0; l < domesticStatementTypes.get(j).getVariables().size(); l++) {
										if (foreignStatementTypes.get(i).getVariables().get(k).getKey().equals(domesticStatementTypes.get(j).getVariables().get(l).getKey()) &&
												foreignStatementTypes.get(i).getVariables().get(k).getDataType().equals(domesticStatementTypes.get(j).getVariables().get(l).getDataType())) {

											// put variable ID correspondence in map
											variableMap.put(foreignStatementTypes.get(i).getVariables().get(k).getVariableId(), domesticStatementTypes.get(j).getVariables().get(l).getVariableId());
											
											// put attribute variable ID correspondence in map and add attribute variables if necessary
											if (foreignStatementTypes.get(i).getVariables().get(k).getDataType().equals("short text")) {
												if (Importer.this.version == 3) {
													f7.setInt(1, foreignStatementTypes.get(i).getVariables().get(k).getVariableId());
													r2 = f7.executeQuery();
													while (r2.next()) {
														boolean attributeVariablePresent = false;
														d19.setInt(1, domesticStatementTypes.get(j).getVariables().get(l).getVariableId());
														d19.setString(2, r2.getString("AttributeVariable"));
														r3 = d19.executeQuery();
														while (r3.next()) {
															attributeVariablePresent = true;
															attributeVariableMap.put(r2.getInt("ID"), r3.getInt("ID"));
														}
														r3.close();
														if (!attributeVariablePresent && mergeAttributesBox.isSelected()) {
															d16.setInt(1, domesticStatementTypes.get(j).getVariables().get(l).getVariableId());
															d16.setString(2, r2.getString("AttributeVariable"));
															d16.executeUpdate();
															ResultSet keySetAttributeVariable = d16.getGeneratedKeys();
															int attributeVariableId = -1;
															while (keySetAttributeVariable.next()) {
																attributeVariableId = keySetAttributeVariable.getInt(1);
															}
															keySetAttributeVariable.close();
															attributeVariableMap.put(r2.getInt("ID"), attributeVariableId);
														}
													}
													r2.close();
												} else if (Importer.this.version == 2) { // in DNA 2.0, assume attribute ID 1 = Type, 2 = Alias, 3 = Notes
													String[] foreignAttributes = new String[] {"Type", "Alias", "Notes"};
													for (int f = 0; f < foreignAttributes.length; f++) {
														boolean attributeVariablePresent = false;
														d19.setInt(1, domesticStatementTypes.get(j).getVariables().get(l).getVariableId());
														d19.setString(2, foreignAttributes[f]);
														r3 = d19.executeQuery();
														while (r3.next()) {
															attributeVariablePresent = true;
															attributeVariableMap.put(f + 1, r3.getInt("ID")); // may not be valid with DNA 2.0 because there is no attribute variable ID
														}
														r3.close();
														if (!attributeVariablePresent && mergeAttributesBox.isSelected()) {
															d16.setInt(1, domesticStatementTypes.get(j).getVariables().get(l).getVariableId());
															d16.setString(2, foreignAttributes[f]);
															d16.executeUpdate();
															ResultSet keySetAttributeVariable = d16.getGeneratedKeys();
															int attributeVariableId = -1;
															while (keySetAttributeVariable.next()) {
																attributeVariableId = keySetAttributeVariable.getInt(1);
															}
															keySetAttributeVariable.close();
															attributeVariableMap.put(f + 1, attributeVariableId); // may not be valid with DNA 2.0 because there is no attribute variable ID
														}
													}
												}
											}

											// put entity IDs in correspondence map and add entities if necessary
											if (foreignStatementTypes.get(i).getVariables().get(k).getDataType().equals("short text")) {
												if (Importer.this.version == 3) {
													f13.setInt(1, foreignStatementTypes.get(i).getVariables().get(k).getVariableId());
													r2 = f13.executeQuery();
												} else {
													f14V2.setInt(1, foreignStatementTypes.get(i).getVariables().get(k).getVariableId());
													r2 = f14V2.executeQuery();
												}
												while (r2.next()) {
													d21.setInt(1, domesticStatementTypes.get(j).getVariables().get(l).getVariableId());
													d21.setString(2, r2.getString("Value"));
													r3 = d21.executeQuery();
													boolean entityPresent = false;
													while (r3.next()) {
														entityPresent = true;
														entityMap.put(r2.getInt("ID"), r3.getInt("ID")); // may not be valid with DNA 2.0 because there is no entity ID; works only with version 3
													}
													r3.close();
													
													// find out if the foreign entity was used in a selected document
													if (Importer.this.version == 2) {
														f16V2.setInt(1, r2.getInt("ID"));
														r3 = f16V2.executeQuery();
													} else if (Importer.this.version == 3) {
														f16.setInt(1, r2.getInt("ID"));
														r3 = f16.executeQuery();
													}
													boolean usedInSelectedDocuments = true;
													while (r3.next()) {
														if (r3.getInt(1) == 1) {
															usedInSelectedDocuments = true;
														} else {
															usedInSelectedDocuments = false;
														}
													}
													r3.close();
													
													// import if not present yet and the entity is either used in the foreign file or unused entities are imported, too
													if (!entityPresent && (importEntitiesBox.isSelected() || (r2.getInt("InDatabase") > 0 && usedInSelectedDocuments))) {
														d20.setInt(1, domesticStatementTypes.get(j).getVariables().get(l).getVariableId()); // variable ID
														d20.setString(2, r2.getString("Value").substring(0, Math.min(190, r2.getString("Value").length())));
														d20.setInt(3, r2.getInt("Red"));
														d20.setInt(4, r2.getInt("Green"));
														d20.setInt(5, r2.getInt("Blue"));
														d20.executeUpdate();
														ResultSet keySetEntity = d20.getGeneratedKeys();
														int entityId = -1;
														while (keySetEntity.next()) {
															entityId = keySetEntity.getInt(1);
														}
														keySetEntity.close();
														entityMap.put(r2.getInt("ID"), entityId); // may not be valid with DNA 2.0 because there is no entity ID; works only with version 3
														entityCount++;
													}
													
													// import attributes for DNA 2.0 here already because they are part of ResultSet r2 anyway
													if (Importer.this.version == 2) {
														String[] foreignAttributes = new String[] {"Type", "Alias", "Notes"};
														for (int f = 0; f < foreignAttributes.length; f++) {
															
															// get attribute value in foreign database
															String foreignAttributeValue = "";
															if (f == 0) {
																foreignAttributeValue = r2.getString("Type");
															} else if (f == 1) {
																foreignAttributeValue = r2.getString("Alias");
															} else if (f == 2) {
																foreignAttributeValue = r2.getString("Notes");
															}
															
															// get attribute variable ID in domestic database
															d19.setInt(1, domesticStatementTypes.get(j).getVariables().get(l).getVariableId());
															d19.setString(2, foreignAttributes[f]);
															r3 = d19.executeQuery();
															int attributeVariableId = -1;
															while (r3.next()) {
																attributeVariableId = r3.getInt("ID");
															}
															r3.close();
															
															// get entity ID in domestic database
															d21.setInt(1, domesticStatementTypes.get(j).getVariables().get(l).getVariableId());
															d21.setString(2, r2.getString("Value").substring(0, Math.min(190, r2.getString("Value").length())));
															r3 = d21.executeQuery();
															int entityId = -1;
															while (r3.next()) {
																entityId = r3.getInt("ID");
															}
															r3.close();
															
															// determine the current attribute value in the domestic database and overwrite or merge if desired
															if (entityId > 0) { // if entityId == -1, that's because it was an unused entity/attribute and unused entities are not being imported; can be ignored.
																d23.setInt(1, entityId);
																d23.setInt(2, attributeVariableId);
																r3 = d23.executeQuery();
																boolean attributeExists = false;
																while (r3.next()) {
																	attributeExists = true;
																	String domesticAttributeValue = r3.getString("AttributeValue");
																	if (!foreignAttributeValue.equals(domesticAttributeValue)) {
																		if (overwriteAttributesBox.isSelected() ||
																				(mergeAttributesBox.isSelected() && (domesticAttributeValue == null || domesticAttributeValue.equals("")))) {
																			d22.setString(1, foreignAttributeValue.substring(0, Math.min(190, foreignAttributeValue.length())));
																			d22.setInt(2, entityMap.get(r2.getInt("ID")));
																			d22.setInt(3, attributeVariableId);
																			d22.executeUpdate();
																			attributeCount++;
																		}
																	}
																}
																r3.close();
																if (!attributeExists) {
																	d17.setInt(1, entityMap.get(r2.getInt("ID")));
																	d17.setInt(2, attributeVariableId);
																	d17.setString(3, foreignAttributeValue.substring(0, Math.min(190, foreignAttributeValue.length())));
																	d17.executeUpdate();
																	attributeCount++;
																}
															}
														}
													}
												}
												r2.close();
											}
										}
									}
								}
							}
						}
					}
					// add new statement type (if unknown statement types are imported) and save correspondence in hash maps
					if (!statementTypeMap.containsKey(foreignStatementTypes.get(i).getId()) && statementTypeBox.isSelected()) {
						d7.setString(1, foreignStatementTypes.get(i).getLabel());
						d7.setInt(2, foreignStatementTypes.get(i).getColor().getRed());
						d7.setInt(3, foreignStatementTypes.get(i).getColor().getGreen());
						d7.setInt(4, foreignStatementTypes.get(i).getColor().getBlue());
						d7.executeUpdate();
						ResultSet keySetStatementType = d7.getGeneratedKeys();
						int statementTypeId = -1;
						while (keySetStatementType.next()) {
							statementTypeId = keySetStatementType.getInt(1);
						}
						
						// add variables
						for (int k = 0; k < foreignStatementTypes.get(i).getVariables().size(); k++) {
							d8.setString(1, foreignStatementTypes.get(i).getVariables().get(k).getKey());
							d8.setString(2, foreignStatementTypes.get(i).getVariables().get(k).getDataType());
							d8.setInt(3, statementTypeId);
							d8.executeUpdate();
							ResultSet keySetVariable = d8.getGeneratedKeys();
							int variableId = -1;
							while (keySetVariable.next()) {
								variableId = keySetVariable.getInt(1);
							}
							variableMap.put(foreignStatementTypes.get(i).getVariables().get(k).getVariableId(), variableId);
							
							// add attribute variables
							if (Importer.this.version == 3) {
								f7.setInt(1, foreignStatementTypes.get(i).getVariables().get(k).getVariableId());
								r2 = f7.executeQuery();
								while (r2.next()) {
									d16.setInt(1, variableId);
									d16.setString(2, r2.getString("AttributeVariable"));
									d16.executeUpdate();
									ResultSet keySetAttributeVariable = d16.getGeneratedKeys();
									int attributeVariableId = -1;
									while (keySetAttributeVariable.next()) {
										attributeVariableId = keySetAttributeVariable.getInt(1);
									}
									attributeVariableMap.put(r2.getInt("ID"), attributeVariableId);
								}
								r2.close();
							} else if (Importer.this.version == 2) { // for DNA 2.0, do not retrieve attribute variables because they are fixed and known
								String[] foreignAttributes = new String[] {"Type", "Alias", "Notes"};
								for (int f = 0; f < foreignAttributes.length; f++) {
									d16.setInt(1, variableId);
									d16.setString(2, foreignAttributes[f]);
									d16.executeUpdate();
									ResultSet keySetAttributeVariable = d16.getGeneratedKeys();
									int attributeVariableId = -1;
									while (keySetAttributeVariable.next()) {
										attributeVariableId = keySetAttributeVariable.getInt(1);
									}
									keySetAttributeVariable.close();
									attributeVariableMap.put(f + 1, attributeVariableId); // may not be valid with DNA 2.0 because there is no attribute variable ID
								}
							}
							
							// add all entities and put entity IDs in correspondence map
							if (foreignStatementTypes.get(i).getVariables().get(k).getDataType().equals("short text")) {
								if (Importer.this.version == 3) {
									f13.setInt(1, foreignStatementTypes.get(i).getVariables().get(k).getVariableId());
									r2 = f13.executeQuery();
								} else { // version 2
									f14V2.setInt(1, foreignStatementTypes.get(i).getVariables().get(k).getVariableId());
									r2 = f14V2.executeQuery();
								}
								while (r2.next()) {
									if (importEntitiesBox.isSelected() || r2.getInt("InDatabase") > 0) {
										d20.setInt(1, variableId);
										if (Importer.this.version == 3) {
											d20.setString(2, r2.getString("Value"));
										} else {
											d20.setString(2, r2.getString("Value").substring(0, Math.min(190, r2.getString("Value").length())));
										}
										d20.setInt(3, r2.getInt("Red"));
										d20.setInt(4, r2.getInt("Green"));
										d20.setInt(5, r2.getInt("Blue"));
										d20.executeUpdate();
										ResultSet keySetEntity = d20.getGeneratedKeys();
										int entityId = -1;
										while (keySetEntity.next()) {
											entityId = keySetEntity.getInt(1);
										}
										entityMap.put(r2.getInt("ID"), entityId); // may not be valid with DNA 2.0 because there is no entity ID
										entityCount++;
									}
									
									// import attributes for DNA 2.0 here already because they are part of ResultSet r2 anyway
									if (Importer.this.version == 2) {
										String[] foreignAttributes = new String[] {"Type", "Alias", "Notes"};
										for (int f = 0; f < foreignAttributes.length; f++) {
											
											// get attribute value in foreign database
											String foreignAttributeValue = "";
											if (f == 0) {
												foreignAttributeValue = r2.getString("Type");
											} else if (f == 1) {
												foreignAttributeValue = r2.getString("Alias");
											} else if (f == 2) {
												foreignAttributeValue = r2.getString("Notes");
											}
											
											// get attribute variable ID in domestic database
											d19.setInt(1, variableId);
											d19.setString(2, foreignAttributes[f]);
											r3 = d19.executeQuery();
											int attributeVariableId = -1;
											while (r3.next()) {
												attributeVariableId = r3.getInt("ID");
											}
											r3.close();
											
											// get entity ID in domestic database
											d21.setInt(1, variableId);
											d21.setString(2, r2.getString("Value").substring(0, Math.min(190, r2.getString("Value").length())));
											r3 = d21.executeQuery();
											int entityId = -1;
											while (r3.next()) {
												entityId = r3.getInt("ID");
											}
											r3.close();
											
											// determine the current attribute value in the domestic database and overwrite or merge if desired
											if (entityId > 0) { // if entityId == -1, that's because it was an unused entity/attribute and unused entities are not being imported; can be ignored.
												d17.setInt(1, entityId);
												d17.setInt(2, attributeVariableId);
												d17.setString(3, foreignAttributeValue.substring(0, Math.min(190, foreignAttributeValue.length())));
												d17.executeUpdate();
												attributeCount++;
											}
										}
									}
								}
								r2.close();
							}
						}
						statementTypeMap.put(foreignStatementTypes.get(i).getId(), statementTypeId);
						statementTypeIdToTypeMap.put(statementTypeId, foreignStatementTypes.get(i));
						statementTypeCount++;
					}
				}
				LogEvent le3 = new LogEvent(Logger.MESSAGE,
						"[SQL]  ├─ Added " + statementTypeCount + " statement types and " + entityCount + " entities.",
						"Added " + statementTypeCount + " statement types and " + entityCount +
						" entities and added attribute variables where necessary to the entity definitions." +
						" The transaction has not been committed yet and will be rolled" +
						" back in the event of an error during further processing of the transaction.");
				Dna.logger.log(le3);
				
				// process variable links
				progressMonitor.setProgress(2);
				progressMonitor.setNote("(3/5) Variable links...");
				
				if (statementTypeBox.isSelected() && Importer.this.version == 3) {
					r1 = f5.executeQuery();
					while (r1.next()) {
						int source = variableMap.get(r1.getInt("SourceVariableId"));
						int target = variableMap.get(r1.getInt("TargetVariableId"));
						d9.setInt(1, source);
						d9.setInt(2, target);
						r2 = d9.executeQuery();
						while (r2.next()) {
							if (r2.getInt(0) == 0) {
								d10.setInt(1, source);
								d10.setInt(2, target);
								d10.executeUpdate();
							}
						}
					}
					r1.close();
				}
				
				// process attribute values
				progressMonitor.setProgress(3);
				progressMonitor.setNote("(4/5) Attribute values...");

				if (Importer.this.version == 3) {
					r1 = f12.executeQuery(); // select all attribute values
					while (r1.next()) {
						int foreignEntityId = r1.getInt("EntityId");
						int foreignAttributeVariableId = r1.getInt("AttributeVariableId");
						String foreignAttributeValue = r1.getString("AttributeValue");
						if (entityMap.containsKey(foreignEntityId)) {
							d23.setInt(1, entityMap.get(foreignEntityId));
							d23.setInt(2, attributeVariableMap.get(foreignAttributeVariableId));
							r2 = d23.executeQuery();
							boolean attributeExists = false;
							while (r2.next()) {
								attributeExists = true;
								String domesticAttributeValue = r2.getString("AttributeValue");
								if (!foreignAttributeValue.equals(domesticAttributeValue)) {
									if (overwriteAttributesBox.isSelected() ||
											(mergeAttributesBox.isSelected() && (domesticAttributeValue == null || domesticAttributeValue.equals("")))) {
										d22.setString(1, foreignAttributeValue);
										d22.setInt(2, entityMap.get(foreignEntityId));
										d22.setInt(3, attributeVariableMap.get(foreignAttributeVariableId));
										d22.executeUpdate();
										attributeCount++;
									}
								}
							}
							r2.close();
							if (!attributeExists) {
								d17.setInt(1, entityMap.get(foreignEntityId));
								d17.setInt(2, attributeVariableMap.get(foreignAttributeVariableId));
								d17.setString(3, foreignAttributeValue);
								d17.executeUpdate();
								attributeCount++;
							}
						}
					}
					r1.close();
				}
				LogEvent le4 = new LogEvent(Logger.MESSAGE,
						"[SQL]  ├─ Added or updated " + attributeCount + " attribute values in import transaction.",
						"The import thread has added or updated " + attributeCount + " attributes " +
						" in the SQL transaction. The transaction has not been committed yet and will be rolled" +
						" back in the event of an error during further processing of the transaction.");
				Dna.logger.log(le4);
				
				// process documents and statements
				progressMonitor.setProgress(4);
				progressMonitor.setNote("(5/5) Documents and statements...");
				
				int documentCount = 0;
				int statementCount = 0;
				int dateFixCount = 0;
				int ignoredStatementCount = 0;
				if (docIds.size() > 0) {
					r1 = f1.executeQuery(); // select documents
					while (r1.next()) {
						
						// check for duplicate title and text if necessary
						boolean proceed = true;
						if (skipDuplicatesBox.isSelected()) {
							if (Importer.this.version == 3) {
								d2.setString(1, r1.getString("Title"));
							} else {
								d2.setString(1, r1.getString("Title").substring(0, Math.min(190, r1.getString("Title").length())));
							}
							d2.setString(2, r1.getString("Text"));
							r2 = d2.executeQuery();
							while (r2.next()) {
								 if (r2.getInt(1) > 0) {
									 proceed = false;
								 }
							}
							r2.close();
						}
						
						// check empty/full document options
						if (skipFullBox.isSelected() || skipEmptyBox.isSelected()) {
							f15.setInt(1, r1.getInt("ID"));
							r2 = f15.executeQuery();
							while (r2.next()) {
								if ((skipFullBox.isSelected() && r2.getInt(1) > 0) || (skipEmptyBox.isSelected() && r2.getInt(1) == 0)) {
									proceed = false;
								}
							}
							r2.close();
						}
						
						if (proceed) {
							// fix date if necessary
							LocalDateTime date;
							if (Importer.this.version == 3) {
								date = LocalDateTime.ofEpochSecond(r1.getLong("Date"), 0, ZoneOffset.UTC);
							} else { // DNA 2.0: use old Date class and convert
								Date dateV2 = new Date(r1.getLong("Date"));
								date = Instant.ofEpochMilli(dateV2.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
							}
							if (fixDatesBox.isSelected() && (date.getHour() != 0 || date.getMinute() != 0 || date.getSecond() != 0)) {
								if (date.truncatedTo(ChronoUnit.DAYS).isBefore(date.plusHours(12).truncatedTo(ChronoUnit.DAYS))) {
									date = date.plusHours(12).truncatedTo(ChronoUnit.DAYS);
								} else {
									date = date.truncatedTo(ChronoUnit.DAYS);
								}
								dateFixCount++;
							}
							// extract remaining document details and insert document into domestic database
							d1.setString(1, r1.getString("Title").substring(0, Math.min(190, r1.getString("Title").length())));
							d1.setString(2, r1.getString("Text"));
							d1.setInt(3, coderMap.get(r1.getInt("Coder"))); // replace by mapped coder
							d1.setString(4, r1.getString("Author").substring(0, Math.min(190, r1.getString("Author").length())));
							d1.setString(5, r1.getString("Source").substring(0, Math.min(190, r1.getString("Source").length())));
							d1.setString(6, r1.getString("Section").substring(0, Math.min(190, r1.getString("Section").length())));
							d1.setString(7, r1.getString("Notes"));
							d1.setString(8, r1.getString("Type").substring(0, Math.min(190, r1.getString("Type").length())));
							d1.setLong(9, date.toEpochSecond(ZoneOffset.UTC));
							d1.executeUpdate();
							
							// get generated document ID
							ResultSet keySetDocument = d1.getGeneratedKeys();
							int documentId = -1;
							while (keySetDocument.next()) {
								documentId = keySetDocument.getInt(1);
							}
							keySetDocument.close();
							
							// import statements contained in the document
							if (importStatementsBox.isSelected()) {
								f6.setInt(1, r1.getInt("ID"));
								r2 = f6.executeQuery();
								while (r2.next()) {
									// ignore unknown statement types and statements with the wrong coder if required by user options
									if (statementTypeMap.containsKey(r2.getInt("StatementTypeId")) ||
											(coderStatementBox.isSelected() && (coderMap.get(r2.getInt("Coder")) != Dna.sql.getActiveCoder().getId()))) {
										
										// add statement
										int newStatementTypeId = statementTypeMap.get(r2.getInt("StatementTypeId"));
										d11.setInt(1, newStatementTypeId);
										d11.setInt(2, documentId);
										d11.setInt(3, r2.getInt("Start"));
										d11.setInt(4, r2.getInt("Stop"));
										d11.setInt(5, coderMap.get(r2.getInt("Coder")));
										d11.executeUpdate();

										// get generated statement ID
										ResultSet keySetStatement = d11.getGeneratedKeys();
										int statementId = -1;
										while (keySetStatement.next()) {
											statementId = keySetStatement.getInt(1);
										}
										keySetStatement.close();
										
										// add boolean values
										f8.setInt(1, r2.getInt("ID"));
										r3 = f8.executeQuery();
										while (r3.next()) {
											d12.setInt(1, statementId);
											d12.setInt(2, variableMap.get(r3.getInt("VariableId")));
											d12.setInt(3, r3.getInt("Value"));
											d12.executeUpdate();
										}
										r3.close();
										
										// add integer values
										f9.setInt(1, r2.getInt("ID"));
										r3 = f9.executeQuery();
										while (r3.next()) {
											d13.setInt(1, statementId);
											d13.setInt(2, variableMap.get(r3.getInt("VariableId")));
											d13.setInt(3, r3.getInt("Value"));
											d13.executeUpdate();
										}
										r3.close();
										
										// add long text values
										f10.setInt(1, r2.getInt("ID"));
										r3 = f10.executeQuery();
										while (r3.next()) {
											d14.setInt(1, statementId);
											d14.setInt(2, variableMap.get(r3.getInt("VariableId")));
											d14.setString(3, r3.getString("Value"));
											d14.executeUpdate();
										}
										r3.close();
										
										// add short text entity references
										f11.setInt(1, r2.getInt("ID"));
										r3 = f11.executeQuery();
										while (r3.next()) {
											d15.setInt(1, statementId);
											d15.setInt(2, variableMap.get(r3.getInt("VariableId")));
											if (Importer.this.version == 3) {
												d15.setInt(3, entityMap.get(r3.getInt("Entity")));
											} else if (Importer.this.version == 2) { // if DNA 2.0, the entity map is not valid and we need to query the entity ID
												
												// get entity ID in domestic database
												d21.setInt(1, variableMap.get(r3.getInt("VariableId"))); // variable ID may have changed, so look up
												d21.setString(2, r3.getString("Value").substring(0, Math.min(190, r3.getString("Value").length())));
												r4 = d21.executeQuery();
												int entityId = -1;
												while (r4.next()) {
													entityId = r4.getInt("ID");
												}
												r4.close();
												d15.setInt(3, entityId);
											}
											d15.executeUpdate();
										}
										r3.close();
										statementCount++;
									} else {
										ignoredStatementCount++;
									}
								}
								r2.close();
							}
						}
						documentCount++;
					}
					r1.close();
					LogEvent le5 = new LogEvent(Logger.MESSAGE,
							"[SQL]  ├─ Added " + documentCount + " documents and " + statementCount + " statements to import transaction.",
							"The import thread has added " + documentCount + " documents and " + statementCount +
							" statements to the SQL transaction. It ignored " + ignoredStatementCount +
							" statements due to unknown statement types and rounded " + dateFixCount +
							" dates. The documents and statements have been added to the database, but the transaction has not" +
							" been committed yet and will be rolled back in the event of an error during further processing of the transaction.");
					Dna.logger.log(le5);
				}
				
				// close statements after use if not part of the try-with-resources header
				if (Importer.this.version == 3) {
					f5.close();
					f7.close();
					f12.close();
					f13.close();
					d9.close();
					d10.close();
				}
				
				connDomestic.commit();
				
				// log the results
				long elapsed = System.nanoTime(); // measure time again for calculating difference
				LogEvent le6 = new LogEvent(Logger.MESSAGE,
						"[SQL]  └─ Successfully imported all data and committed to database.",
						"Imported " + documentCount + " documents, " + statementCount + " statements, " + statementTypeCount +
						" statement types, " + entityCount + " entities, " + attributeCount + " attribute values, and " + regexCount +
						" regex keywords from another database and rounded " + dateFixCount +
						" date/time stamps and ignored " + ignoredStatementCount +
						" statements because they had an unknown statement type or wrong coder. It took " + (elapsed - time) / 1000000 +
						" milliseconds.");
				dna.Dna.logger.log(le6);
				dispose(); // close the importer when done
			} catch (Exception e) {
				LogEvent le7 = new LogEvent(Logger.ERROR,
						"[SQL] Failed to import data from other database.",
						"Attempted importing data from another database, but the import failed. The transaction has been rolled back, and no changes have been written to the currently open database. Check the exception message stack for details.",
						e);
				dna.Dna.logger.log(le7);
			} finally {
				progressMonitor.setProgress(5);
			}
			
			// enable buttons again after the import work is done
			dbButton.setEnabled(true);
			filterButton.setEnabled(true);
			selectAll.setEnabled(true);
			importButton.setEnabled(true);
		}
	}
}