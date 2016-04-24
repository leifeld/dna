package dna;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import dna.dataStructures.Coder;
import dna.dataStructures.Data;
import dna.dataStructures.Document;
import dna.dataStructures.Statement;
import dna.renderer.CoderComboBoxModel;
import dna.renderer.CoderComboBoxRenderer;
import dna.renderer.CoderTableCellRenderer;


@SuppressWarnings("serial")
public class ImportDnaDocuments extends JFrame {
	
	Container c;
	CoderTableModel coderTableModel;
	JTable articleImportTable;
	ArticleImportTableModel aitm;
	ImageIcon filterIcon;
	public SqlConnection foreignSql;
	public Data foreignData;
	
	public ImportDnaDocuments(final String file) {
		c = getContentPane();
		this.setTitle("Import statements...");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon importIcon = new ImageIcon(getClass().getResource("/icons/page_white_get.png"));
		this.setIconImage(importIcon.getImage());

		CoderComboBoxModel coderComboBoxModel = new CoderComboBoxModel();
		JComboBox comboBox = new JComboBox(coderComboBoxModel);
		CoderComboBoxRenderer renderer = new CoderComboBoxRenderer();
		comboBox.setRenderer(renderer);
		coderTableModel = new CoderTableModel();
		JTable coderTable = new JTable( coderTableModel );
        TableColumn column = coderTable.getColumnModel().getColumn(1);
        coderTable.setRowHeight(30);
        column.setCellEditor(new DefaultCellEditor(comboBox));
        coderTable.setDefaultRenderer( Coder.class, new CoderTableCellRenderer() );
		coderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane coderScroller = new JScrollPane(coderTable);
		coderScroller.setPreferredSize(new Dimension(300, 200));
		coderTable.getTableHeader().setReorderingAllowed(false);
		
		aitm = new ArticleImportTableModel();
		articleImportTable = new JTable(aitm);
		articleImportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane tableScrollPane = new JScrollPane(articleImportTable);
		tableScrollPane.setPreferredSize(new Dimension(500, 300));
		
		articleImportTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		articleImportTable.getColumnModel().getColumn(1).setPreferredWidth(400);
		articleImportTable.getColumnModel().getColumn(2).setPreferredWidth(160);
		articleImportTable.getTableHeader().setReorderingAllowed( false );
		
		JButton importButton = new JButton("Import selected", importIcon);
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Dna.dna.gui, 
						"Are you sure you want to insert the selected \n" +
						"articles into the currently open DNA database?", 
						"Confirmation", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					try {
						Thread importThread = new Thread( new ArticleInserter(file), "Import documents" );
						importThread.start();
					} catch (OutOfMemoryError ome) {
						System.err.println("Out of memory. File has been " +
								"closed. Please start Java with\nthe " +
								"-Xmx1024M option, where '1024' is the space " +
								"you want\nto allocate to DNA. The manual " +
								"provides further details.");
						JOptionPane.showMessageDialog(Dna.dna.gui, 
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
		ImageIcon selectIcon = new ImageIcon(getClass().getResource("/icons/asterisk_yellow.png"));
		JButton selectAll = new JButton("(Un)select all", selectIcon);
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((Boolean)aitm.getValueAt(0, 0) == false) {
					for (int i = 0; i < aitm.getRowCount(); i++) {
						aitm.setValueAt(true, i, 0);
					}
				} else {
					for (int i = 0; i < aitm.getRowCount(); i++) {
						aitm.setValueAt(false, i, 0);
					}
				}
				
			}
		});
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,3));
		
		filterIcon = new ImageIcon(getClass().getResource("/icons/application_form.png"));
		JButton filterButton = new JButton("Keyword filter...", filterIcon);
		filterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String s = (String)JOptionPane.showInputDialog(
						ImportDnaDocuments.this, 
						"Please enter a regular expression to filter the documents:", 
						"Keyword filter", 
						JOptionPane.PLAIN_MESSAGE, 
						filterIcon,
	                    null,
	                    "");
				
				if ((s != null) && (s.length() > 0)) {
					for (int i = 0; i < aitm.getRowCount(); i++) {
						Pattern p = Pattern.compile(s);
	    				Matcher m = p.matcher(aitm.getValueAt(i, 1).toString());
	    				boolean b = m.find();
	    				if (b == true) {
	    					aitm.setValueAt(true, i, 0);
	    				} else {
	    					aitm.setValueAt(false, i, 0);
	    				}
					}
				}
			}
		});
		
		buttonPanel.add(filterButton);
		buttonPanel.add(selectAll);
		buttonPanel.add(importButton);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(coderScroller, BorderLayout.NORTH);
		panel.add(tableScrollPane, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		parseArticles(file);
		
		c.add(panel);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	class CoderTableModel implements TableModel {

		private Vector<TableModelListener> listeners = new Vector<TableModelListener>();
		ArrayList<Coder> coderForeign = new ArrayList<Coder>();
		ArrayList<Coder> coderDomestic = new ArrayList<Coder>();
		
		public void addTableModelListener(TableModelListener l) {
			listeners.add(l);
		}
		
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
		
		public void removeTableModelListener(TableModelListener l) {
			listeners.remove(l);
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Coder coder = (Coder) aValue;
			if (columnIndex == 0) {
				coderForeign.set(rowIndex, coder);
			} else {
				coderDomestic.set(rowIndex, coder);
			}
			TableModelEvent e = new TableModelEvent(this);
			for( int i = 0, n = listeners.size(); i < n; i++ ){
				((TableModelListener)listeners.get( i )).tableChanged( e );
			}
		}
	}
	
	class ArticleInserter implements Runnable {
		
		String filename;
		ProgressMonitor progressMonitor1;
		ProgressMonitor progressMonitor2;
		
		public ArticleInserter(String filename) {
			this.filename = filename;
		}
		
		public void run() {
			
			HashMap<Integer, Integer> coderMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < coderTableModel.coderForeign.size(); i++) {
				coderMap.put(coderTableModel.coderForeign.get(i).getId(), coderTableModel.coderDomestic.get(i).getId());
			}

			HashMap<Integer, Integer> documentMap = new HashMap<Integer, Integer>();
			HashMap<Integer, Boolean> documentExistsMap = new HashMap<Integer, Boolean>();
			
			// TODO: check if statement types exist and create if not; map foreign to domestic statementTypeId
			// for now, we assume that they are identical, but this may lead to errors!
			/*
			HashMap<Integer, Integer> statementTypeMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < foreignData.getStatementTypes().size(); i++) {
				int domesticStatementTypeId = -1;
				for (int j = 0; j < Dna.data.getStatementTypes().size(); j++) {
					
				}
				statementTypeMap.put(foreignData.getStatementTypes().get(i).getId(), );
			}
			*/
			
			// documents
			Dna.dna.gui.rightPanel.statementPanel.setRowSorterEnabled(false);
			progressMonitor1 = new ProgressMonitor(Dna.dna.gui, "Importing documents and statements...", "", 0, aitm.getRowCount() - 1 );
			progressMonitor1.setMillisToDecideToPopup(1);
			
			for (int k = 0; k < aitm.getRowCount(); k++) {
				progressMonitor1.setProgress(k);
				if (progressMonitor1.isCanceled()) {
					break;
				}
				if ((Boolean) aitm.getValueAt(k, 0) == true) {
					int documentId = aitm.getDocumentId(k);
					Document document = foreignData.getDocument(documentId);
					int newDocumentId = Dna.data.generateNewId("documents");
					document.setId(newDocumentId);
					document.setCoder(coderMap.get(document.getCoder()));
					Dna.dna.addDocument(document);
					
					// import statements for this document
					for (int i = 0; i < foreignData.getStatements().size(); i++) {
						if (foreignData.getStatements().get(i).getDocumentId() == documentId) {
							Statement statement = foreignData.getStatements().get(i);
							statement.setDocumentId(newDocumentId);
							statement.setCoder(coderMap.get(statement.getCoder()));
							int newStatementId = Dna.data.generateNewId("statements");
							statement.setId(newStatementId);
							//Dna.dna.gui.rightPanel.statementPanel.ssc.addStatement(statement);
							Dna.data.addStatement(statement);
							int statementTypeId = statement.getStatementTypeId();
							LinkedHashMap<String, String> map = Dna.data.getStatementTypeById(statementTypeId).getVariables();
							Dna.dna.sql.addStatement(statement, map);
						}
					}
				}
			}
			Dna.dna.gui.rightPanel.statementPanel.setRowSorterEnabled(true);
			
			/*
			for (int k = 0; k < aitm.getRowCount(); k++) {
				progressMonitor1.setProgress(k);
				if (progressMonitor1.isCanceled()) {
					break;
				}
				if ((Boolean)aitm.getValueAt(k, 0) == true) {
					int oldDocumentId = aitm.getDocumentId(k);
					int newDocumentId = Dna.data.generateNewId("documents");
					documentMap.put(oldDocumentId, newDocumentId);
					documentExistsMap.put(oldDocumentId, true);
					Document document = foreignData.getDocument(oldDocumentId);
					document.setId(newDocumentId);
					document.setCoder(coderMap.get(document.getCoder()));
					Dna.dna.addDocument(document);
				} else {
					documentExistsMap.put(aitm.getDocumentId(k), false);
				}
			}
			
			// statements
			progressMonitor2 = new ProgressMonitor(Dna.dna.gui, "Importing statements...", "", 0, foreignData.getStatements().size() - 1 );
			progressMonitor2.setMillisToDecideToPopup(1);
			Dna.dna.gui.rightPanel.statementPanel.setRowSorterEnabled(false);
			for (int i = 0; i < foreignData.getStatements().size(); i++) {
				progressMonitor2.setProgress(i);
				if (progressMonitor2.isCanceled()) {
					break;
				}
				int statementDocumentId = foreignData.getStatements().get(i).getDocumentId();
				boolean exists = documentExistsMap.get(statementDocumentId);
				if (exists == true) {
					int newDocumentId = documentMap.get(statementDocumentId);
					Statement statement = foreignData.getStatements().get(i);
					statement.setCoder(coderMap.get(statement.getCoder()));
					int newStatementId = Dna.data.generateNewId("statements");
					statement.setId(newStatementId);
					statement.setDocumentId(newDocumentId);
					Dna.dna.gui.rightPanel.statementPanel.ssc.addStatement(statement);
					int statementTypeId = statement.getStatementTypeId();
					LinkedHashMap<String, String> map = Dna.data.getStatementTypeById(statementTypeId).getVariables();
					Dna.dna.sql.addStatement(statement, map);
				}
			}
			Dna.dna.gui.rightPanel.statementPanel.setRowSorterEnabled(true);
			*/
		}
	}
	
	public void parseArticles(String filename) {
		foreignSql = new SqlConnection("sqlite", filename, "", "");
		foreignData = foreignSql.getAllData();
		for (int i = 0; i < foreignData.getDocuments().size(); i++) {
			int id = foreignData.getDocuments().get(i).getId();
			String title = foreignData.getDocuments().get(i).getTitle();
			Date date = foreignData.getDocuments().get(i).getDate();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
			String dateString = dateFormat.format(date);
			String coderString = foreignData.getCoderById(foreignData.getDocuments().get(i).getCoder()).getName();
			aitm.addArticle(id, title, coderString, dateString, false);
		}
		for (int i = 0; i < foreignData.getCoders().size(); i++) {
			coderTableModel.coderForeign.add(foreignData.getCoders().get(i));
			int domesticCoder = -1;
			for (int j = 0; j < Dna.data.getCoders().size(); j++) {
				if (Dna.data.getCoders().get(j).getName().equals(foreignData.getCoders().get(i).getName())) {
					domesticCoder = Dna.data.getCoders().get(j).getId();
				}
			}
			if (domesticCoder == -1) {
				for (int j = 0; j < Dna.data.getCoders().size(); j++) {
					if (Dna.data.getCoders().get(j).getId() == foreignData.getCoders().get(i).getId()) {
						domesticCoder = Dna.data.getCoders().get(j).getId();
					}
				}
			}
			if (domesticCoder == -1) {
				domesticCoder = Dna.data.getCoders().get(0).getId();
			}
			coderTableModel.coderDomestic.add(Dna.data.getCoderById(domesticCoder));
		}
	}
	
	class ArticleImportTableModel implements TableModel {
		private Vector<TableModelListener> listeners = new Vector<TableModelListener>();
		private Vector<Integer> ids = new Vector<Integer>();
		private Vector<String> titles = new Vector<String>();
		private Vector<String> coders = new Vector<String>();
		private Vector<String> dates = new Vector<String>();
		private Vector<Boolean> selections = new Vector<Boolean>();
		
		public void addArticle( int id, String title, String coderString, String date, boolean selection ){
			
			int index = titles.size();
			ids.add(id);
			titles.add( title );
			coders.add( coderString );
			dates.add( date );
			selections.add( selection );
			
			TableModelEvent e = new TableModelEvent( this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
			
			for( int i = 0, n = listeners.size(); i < n; i++ ) {
				((TableModelListener)listeners.get( i )).tableChanged( e );
			}
		}
		
		public int getDocumentId(int row) {
			return ids.get(row);
		}
		
		public int getColumnCount() {
			return 4;
		}
		
		public int getRowCount() {
			return titles.size();
		}
		
		public String getColumnName(int column) {
			switch( column ){
				case 0: return "";
				case 1: return "title";
				case 2: return "coder";
				case 3: return "date";
				default: return null;
			}
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch( columnIndex ){
				case 0: return selections.get(rowIndex) ? Boolean.TRUE : Boolean.FALSE; 
				case 1: return titles.get(rowIndex);
				case 2: return coders.get(rowIndex);
				case 3: return dates.get(rowIndex);
				default: return null;
			}
		}
		
		public Class<?> getColumnClass(int columnIndex) {
			switch( columnIndex ){
				case 0: return Boolean.class;
				case 1: return String.class;
				case 2: return String.class;
				case 3: return String.class;
				default: return null;
			}	
		}
		
		public void addTableModelListener(TableModelListener l) {
			listeners.add( l );
		}
		public void removeTableModelListener(TableModelListener l) {
			listeners.remove( l );
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch( columnIndex ){
				case 0: return true;
				case 1: return false;
				case 2: return false;
				case 3: return false;
				default: return false;
			}
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				selections.set(rowIndex, (Boolean)aValue);
			}
			TableModelEvent e = new TableModelEvent(this);
			for( int i = 0, n = listeners.size(); i < n; i++ ){
				((TableModelListener)listeners.get( i )).tableChanged( e );
			}
		}
	}
}