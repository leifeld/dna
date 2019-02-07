package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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

import dna.dataStructures.AttributeVector;
import dna.dataStructures.Coder;
import dna.dataStructures.Data;
import dna.dataStructures.Document;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementType;
import dna.renderer.CoderComboBoxModel;
import dna.renderer.CoderComboBoxRenderer;
import dna.renderer.CoderTableCellRenderer;


@SuppressWarnings("serial")
public class ImportDnaDocuments extends JDialog {
	
	Container c;
	CoderTableModel coderTableModel;
	JTable articleImportTable;
	ArticleImportTableModel aitm;
	ImageIcon filterIcon;
	public SqlConnection foreignSql;
	public Data foreignData;
	
	public ImportDnaDocuments(final String file) {
		this.setModal(true);
		c = getContentPane();
		this.setTitle("Import statements...");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon importIcon = new ImageIcon(getClass().getResource("/icons/page_white_get.png"));
		this.setIconImage(importIcon.getImage());

		CoderComboBoxModel coderComboBoxModel = new CoderComboBoxModel();
		JComboBox<Coder> comboBox = new JComboBox<Coder>(coderComboBoxModel);
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
		articleImportTable.getColumnModel().getColumn(1).setPreferredWidth(350);
		articleImportTable.getColumnModel().getColumn(2).setPreferredWidth(80);
		articleImportTable.getColumnModel().getColumn(3).setPreferredWidth(130);
		articleImportTable.getTableHeader().setReorderingAllowed( false );
		
		JButton importButton = new JButton("Import selected", importIcon);
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Dna.gui, 
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
						JOptionPane.showMessageDialog(Dna.gui, 
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
		ProgressMonitor progressMonitor;
		
		public ArticleInserter(String filename) {
			this.filename = filename;
		}
		
		public void run() {
			
			HashMap<Integer, Integer> coderMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < coderTableModel.coderForeign.size(); i++) {
				coderMap.put(coderTableModel.coderForeign.get(i).getId(), coderTableModel.coderDomestic.get(i).getId());
			}
			
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
			progressMonitor = new ProgressMonitor(Dna.gui, "Importing documents and statements.", "(1/5) Reading documents...", 0, 5);
			progressMonitor.setMillisToDecideToPopup(1);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			progressMonitor.setProgress(0);
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			ArrayList<Document> newDocs = new ArrayList<Document>();
			for (int k = 0; k < aitm.getRowCount(); k++) {
				if ((Boolean) aitm.getValueAt(k, 0) == true) {
					Document document = aitm.documents.get(k);
					int documentId = document.getId();
					int newDocumentId = Dna.data.generateNewId("documents");
					if (map.containsKey(documentId)) {
						System.err.println("Import: document " + documentId + " already exists.");
					}
					map.put(documentId, newDocumentId);
					document.setId(newDocumentId);
					document.setCoder(coderMap.get(document.getCoder()));
					Dna.gui.documentPanel.setRowSorterEnabled(false);
					Dna.gui.documentPanel.documentContainer.addDocument(document);
					Dna.gui.documentPanel.setRowSorterEnabled(true);
					newDocs.add(document);
				}
			}
			progressMonitor.setProgress(1);
			progressMonitor.setNote("(2/5) Saving documents to database...");
			Dna.dna.sql.insertDocuments(newDocs);
			
			// import statements for this document
			progressMonitor.setProgress(2);
			progressMonitor.setNote("(3/5) Reading statements...");
			Dna.gui.rightPanel.statementPanel.setRowSorterEnabled(false);
			ArrayList<Statement> newStatements = new ArrayList<Statement>();
			for (int i = 0; i < foreignData.getStatements().size(); i++) {
				Statement statement = foreignData.getStatements().get(i);
				int documentId = statement.getDocumentId();
				if (map.containsKey(documentId)) {
					int newDocumentId = map.get(documentId);
					statement.setDocumentId(newDocumentId);
					statement.setCoder(coderMap.get(statement.getCoder()));
					int newStatementId = Dna.data.generateNewId("statements");
					statement.setId(newStatementId);
					Dna.data.addStatement(statement);
					newStatements.add(statement);
				}
			}
			Dna.gui.rightPanel.statementPanel.setRowSorterEnabled(true);
			progressMonitor.setProgress(3);
			progressMonitor.setNote("(4/5) Saving statements to database...");
			Dna.dna.sql.addStatements(newStatements);
			progressMonitor.setProgress(4);
			
			// import attributes
			progressMonitor.setNote("(5/5) Importing attributes...");
			ArrayList<StatementType> foreignStatementTypes = foreignData.getStatementTypes();
			for (int i = 0; i < foreignStatementTypes.size(); i++) {  // iterate through all foreign statement types also present in current database
				if (Dna.data.getStatementTypes().contains(foreignStatementTypes.get(i))) {
					int statementTypeId = Dna.data.getStatementType(foreignStatementTypes.get(i).getLabel()).getId();
					ArrayList<Statement> statements = Dna.data.getStatementsByStatementTypeId(statementTypeId);
					LinkedHashMap<String, String> variables = foreignStatementTypes.get(i).getVariables();
					Iterator<String> keyIterator = variables.keySet().iterator();
			        while (keyIterator.hasNext()){  // iterate through all foreign variables nested in the current statement type
			    		String key = keyIterator.next();
			    		String var = variables.get(key);
			    		if (var.equals("short text")) {  // consider all nested attributes
			    			AttributeVector[] av = foreignData.getAttributes(key, foreignStatementTypes.get(i).getId());
			    			ArrayList<AttributeVector> newAttributes = new ArrayList<AttributeVector>();
			    			for (int j = 0; j < av.length; j++) {
			    				int avIndex = Dna.data.getAttributeIndex(av[j].getValue(), key, statementTypeId);
			    				if (avIndex > -1) {  // attribute present in both databases; update empty fields
			    					if (Dna.data.getAttributes().get(avIndex).getAlias().equals("") && !av[j].getAlias().equals("")) {
			    						Dna.dna.updateAttributeAlias(avIndex, av[j].getAlias());
			    					}
			    					if (Dna.data.getAttributes().get(avIndex).getNotes().equals("") && !av[j].getNotes().equals("")) {
			    						Dna.dna.updateAttributeNotes(avIndex, av[j].getNotes());
			    					}
			    					if (Dna.data.getAttributes().get(avIndex).getType().equals("") && !av[j].getType().equals("")) {
			    						Dna.dna.updateAttributeType(avIndex, av[j].getType());
			    					}
			    					if (Dna.data.getAttributes().get(avIndex).getColor().equals(Color.BLACK) && !av[j].getColor().equals(Color.BLACK)) {
			    						Dna.dna.updateAttributeColor(avIndex, av[j].getColor());
			    					}
			    				} else {  // attribute not present; check if at least one statement contains it and mark for import later
			    					for (int k = 0; k < statements.size(); k++) {
			    						if (statements.get(k).getValues().get(key).equals(av[j].getValue())) {
			    							int newId = Dna.data.generateNewId("attributes");
			    							AttributeVector a = av[j];
			    							a.setId(newId);
			    							Dna.data.getAttributes().add(a);
			    							newAttributes.add(a);
			    							break;
			    						}
			    					}
			    				}
			    			}
			    			Dna.dna.sql.insertAttributeVectors(newAttributes);  // import marked attributes
			    			Dna.gui.textPanel.bottomCardPanel.attributePanel.attributeTableModel.sort();
			    		}
			        }
				}
			}
			progressMonitor.setProgress(5);
			Dna.gui.documentPanel.documentTable.setRowSelectionInterval(0, 0);  // if no document is selected, the statement filter may throw an error
		}
	}
	
	public void parseArticles(String filename) {
		foreignSql = new SqlConnection("sqlite", filename, "", "");
		foreignData = foreignSql.getAllData();
		for (int i = 0; i < foreignData.getDocuments().size(); i++) {
			aitm.addArticle(foreignData.getDocuments().get(i), false);
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
		ArrayList<Document> documents = new ArrayList<Document>();
		ArrayList<Boolean> selections = new ArrayList<Boolean>();
		
		public void addArticle( Document document, boolean selection ){
			documents.add(document);
			selections.add( selection );
			int index = documents.size();
			TableModelEvent e = new TableModelEvent( this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
			
			for( int i = 0, n = listeners.size(); i < n; i++ ) {
				((TableModelListener)listeners.get( i )).tableChanged( e );
			}
		}
		
		public Document getDocument(int row) {
			return documents.get(row);
		}
		
		public int getColumnCount() {
			return 4;
		}
		
		public int getRowCount() {
			return documents.size();
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
			if (columnIndex == 0) {
				return selections.get(rowIndex);
			} else if (columnIndex == 1) {
				return documents.get(rowIndex).getTitle();
			} else if (columnIndex == 2) {
				int coderId = documents.get(rowIndex).getCoder();
				for (int i = 0; i < coderTableModel.getRowCount(); i++) {
					if (coderTableModel.coderForeign.get(i).getId() == coderId) {
						return coderTableModel.coderForeign.get(i).getName();
					}
				}
				return "";
			} else if (columnIndex == 3) {
				Date d = documents.get(rowIndex).getDate();
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
				String dateString = dateFormat.format(d);
				return dateString;
			} else {
				return null;
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