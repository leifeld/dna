package dna;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;

import dna.dataStructures.Document;

@SuppressWarnings("serial")
public class RecodeDialog extends JDialog {
	
	JComboBox<String> sourceBox, targetBox;
	JTextField usingSourceStringField, usingTargetStringField, matchingOnTargetRegexField, matchingOnSourceRegexField, 
			sourceTranslatedIntoField, targetTranslatedIntoField, asFollowsField;
	RecodeTableModel tableModel;
	
	public RecodeDialog() {
		this.setTitle("Recode document meta-data");
        ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/table_key.png"));
        this.setIconImage(dna32Icon.getImage());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setModal(true);
        this.setLayout(new BorderLayout());
        

		ItemListener il = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Thread recodeThread = new Thread( new Recoder(true), "Recode documents" );
				recodeThread.start();
			}
		};
        
		DocumentListener dl = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				Thread recodeThread = new Thread( new Recoder(true), "Recode documents" );
				recodeThread.start();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				Thread recodeThread = new Thread( new Recoder(true), "Recode documents" );
				recodeThread.start();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				Thread recodeThread = new Thread( new Recoder(true), "Recode documents" );
				recodeThread.start();
			}
		};
		
        JPanel userPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel targetFieldLabel = new JLabel("Target field:");
        userPanel.add(targetFieldLabel, gbc);
        targetFieldLabel.setToolTipText("The field in which to put the new information.");
        gbc.gridy++;
        JLabel sourceFieldLabel = new JLabel("Source field:");
        userPanel.add(sourceFieldLabel, gbc);
        sourceFieldLabel.setToolTipText("The field from which some existing information is taken and processed.");
        gbc.gridy++;
        JLabel matchingOnTargetRegexLabel = new JLabel("Matching on target regex:");
        userPanel.add(matchingOnTargetRegexLabel, gbc);
        matchingOnTargetRegexLabel.setToolTipText("Something is written into the target field only if this regular expression matches the target field.");
        gbc.gridy++;
        JLabel matchingOnSourceRegexLabel = new JLabel("Matching on source regex:");
        userPanel.add(matchingOnSourceRegexLabel, gbc);
        matchingOnSourceRegexLabel.setToolTipText("Something is written into the target field only if this regular expression matches the source field.");
        gbc.gridy++;
        JLabel usingTargetStringLabel = new JLabel("%target regular expression:");
        userPanel.add(usingTargetStringLabel, gbc);
        usingTargetStringLabel.setToolTipText("Match an expression on the target field and re-use it later as %target.");
        gbc.gridy++;
        JLabel targetTranslatedIntoLabel = new JLabel("%target replacement:");
        userPanel.add(targetTranslatedIntoLabel, gbc);
        targetTranslatedIntoLabel.setToolTipText("A replacement string that is used instead of %target.");
        gbc.gridy++;
        JLabel usingSourceStringLabel = new JLabel("%source regular rexpression:");
        userPanel.add(usingSourceStringLabel, gbc);
        usingSourceStringLabel.setToolTipText("Match an expression on the source field and re-use it later as %source.");
        gbc.gridy++;
        JLabel sourceTranslatedIntoLabel = new JLabel("%source replacement:");
        userPanel.add(sourceTranslatedIntoLabel, gbc);
        sourceTranslatedIntoLabel.setToolTipText("A replacement string that is used instead of %source.");
        gbc.gridy++;
        JLabel asFollowsLabel = new JLabel("New target field:");
        userPanel.add(asFollowsLabel, gbc);
        asFollowsLabel.setToolTipText("The new contents of the target field. You may use %source and %target.");
        gbc.gridy = 0;
        
        gbc.gridx = 1;
        String[] fields = new String[] {"Title", "Author", "Source", "Section", "Type", "Notes"};
        targetBox = new JComboBox<String>(fields);
        targetBox.addItemListener(il);
        targetBox.setToolTipText("The field in which to put the new information.");
        userPanel.add(targetBox, gbc);
        gbc.gridy++;
        sourceBox = new JComboBox<String>(fields);
        sourceBox.addItemListener(il);
        sourceBox.setToolTipText("The field from which some existing information is taken and processed.");
        userPanel.add(sourceBox, gbc);
        gbc.gridy++;
        matchingOnTargetRegexField = new JTextField("");
        matchingOnTargetRegexField.setColumns(35);
        matchingOnTargetRegexField.getDocument().addDocumentListener(dl);
        matchingOnTargetRegexField.setToolTipText("Something is written into the target field only if this regular expression matches the target field.");
        userPanel.add(matchingOnTargetRegexField, gbc);
        gbc.gridy++;
        matchingOnSourceRegexField = new JTextField("");
        matchingOnSourceRegexField.setColumns(35);
        matchingOnSourceRegexField.getDocument().addDocumentListener(dl);
        matchingOnSourceRegexField.setToolTipText("Something is written into the target field only if this regular expression matches the source field.");
        userPanel.add(matchingOnSourceRegexField, gbc);
        gbc.gridy++;
        usingTargetStringField = new JTextField(".+");
        usingTargetStringField.setColumns(35);
        usingTargetStringField.getDocument().addDocumentListener(dl);
        usingTargetStringField.setToolTipText("Match an expression on the target field and re-use it later as %target.");
        userPanel.add(usingTargetStringField, gbc);
        gbc.gridy++;
        targetTranslatedIntoField = new JTextField(".+");
        targetTranslatedIntoField.setColumns(35);
        targetTranslatedIntoField.getDocument().addDocumentListener(dl);
        targetTranslatedIntoField.setToolTipText("A replacement string that is used instead of %target.");
        userPanel.add(targetTranslatedIntoField, gbc);
        gbc.gridy++;
        usingSourceStringField = new JTextField(".+");
        usingSourceStringField.setColumns(35);
        usingSourceStringField.getDocument().addDocumentListener(dl);
        usingSourceStringField.setToolTipText("Match an expression on the source field and re-use it later as %source.");
        userPanel.add(usingSourceStringField, gbc);
        gbc.gridy++;
        sourceTranslatedIntoField = new JTextField(".+");
        sourceTranslatedIntoField.setColumns(35);
        sourceTranslatedIntoField.getDocument().addDocumentListener(dl);
        sourceTranslatedIntoField.setToolTipText("A replacement string that is used instead of %source.");
        userPanel.add(sourceTranslatedIntoField, gbc);
        gbc.gridy++;
        asFollowsField = new JTextField("%source");
        asFollowsField.setColumns(35);
        asFollowsField.getDocument().addDocumentListener(dl);
        asFollowsField.setToolTipText("The new contents of the target field. You may use %source and %target.");
        userPanel.add(asFollowsField, gbc);
        
		tableModel = new RecodeTableModel();
		JTable table = new JTable(tableModel);
		JScrollPane tableScrollPane = new JScrollPane(table);
		tableScrollPane.setPreferredSize(new Dimension(500, 400));
		
		table.getColumnModel().getColumn(0).setPreferredWidth(15);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(2).setPreferredWidth(150);
		table.getColumnModel().getColumn(3).setPreferredWidth(150);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		JButton revertButton = new JButton("Revert changes", new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png")));
		revertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				targetBox.getModel().setSelectedItem("Title");
				sourceBox.getModel().setSelectedItem("Title");
				matchingOnTargetRegexField.setText("");
				matchingOnSourceRegexField.setText("");
				usingTargetStringField.setText(".+");
				usingSourceStringField.setText(".+");
				targetTranslatedIntoField.setText(".+");
				sourceTranslatedIntoField.setText(".+");
				asFollowsField.setText("%source");
				Thread recodeThread = new Thread( new Recoder(true), "Recode documents" );
				recodeThread.start();
			}
		});
		buttonPanel.add(revertButton);
		
		JButton cancelButton = new JButton("Cancel", new ImageIcon(getClass().getResource("/icons/cancel.png")));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(cancelButton);
		
		JButton okButton = new JButton("Recode", new ImageIcon(getClass().getResource("/icons/table_go.png")));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread recodeThread = new Thread( new Recoder(false), "Recode documents" );
				recodeThread.start();
			}
		});
		buttonPanel.add(okButton);
		
        this.add(userPanel, BorderLayout.NORTH);
        this.add(tableScrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

		Thread recodeThread = new Thread( new Recoder(true), "Recode documents" );
		recodeThread.start();
        
        this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	class Recoder implements Runnable {
		
		boolean onlyTable;
		ProgressMonitor progressMonitor;
		
		public Recoder(boolean onlyTable) {
			this.onlyTable = onlyTable;
		}
		
		public void run() {
			progressMonitor = new ProgressMonitor(RecodeDialog.this, "Recoding documents...", "", 0, tableModel.getRowCount());
			progressMonitor.setMillisToDecideToPopup(1000);
			
			if (onlyTable == true) {
				tableModel.clear();
			}
			String source = (String) sourceBox.getSelectedItem();
			String target = (String) targetBox.getSelectedItem();
			
	        for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
				if (progressMonitor.isCanceled()) {
					break;
				}
	        	Document document = Dna.data.getDocuments().get(i);
	        	
	    		Pattern pTarget = Pattern.compile(matchingOnTargetRegexField.getText());
	    		String targetEntry = "";
	    		if (target.equals("Title")) {
	    			targetEntry = document.getTitle();
	    		} else if (target.equals("Author")) {
	    			targetEntry = document.getAuthor();
	    		} else if (target.equals("Source")) {
	    			targetEntry = document.getSource();
	    		} else if (target.equals("Section")) {
	    			targetEntry = document.getSection();
	    		} else if (target.equals("Type")) {
	    			targetEntry = document.getType();
	    		} else if (target.equals("Notes")) {
	    			targetEntry = document.getNotes();
	    		}
	    		Matcher mTarget = pTarget.matcher(targetEntry);
	    		boolean bTarget = mTarget.find();
	    		
	    		Pattern pSource = Pattern.compile(matchingOnSourceRegexField.getText());
	    		String sourceEntry = "";
	    		if (source.equals("Title")) {
	    			sourceEntry = document.getTitle();
	    		} else if (source.equals("Author")) {
	    			sourceEntry = document.getAuthor();
	    		} else if (source.equals("Source")) {
	    			sourceEntry = document.getSource();
	    		} else if (source.equals("Section")) {
	    			sourceEntry = document.getSection();
	    		} else if (source.equals("Type")) {
	    			sourceEntry = document.getType();
	    		} else if (source.equals("Notes")) {
	    			sourceEntry = document.getNotes();
	    		}
	    		Matcher mSource = pSource.matcher(sourceEntry);
	    		boolean bSource = mSource.find();
	    		
	    		String result = targetEntry;
	        	if (bTarget == true && bSource == true) {
	        		String targetResult = "";
	        		if (!targetTranslatedIntoField.getText().equals(".+")) {
	        			targetResult = targetTranslatedIntoField.getText();
	        		} else if (target.equals("Title")) {
	        			targetResult = patternToString(document.getTitle(), usingTargetStringField.getText());
	        		} else if (target.equals("Author")) {
	        			targetResult = patternToString(document.getAuthor(), usingTargetStringField.getText());
	        		} else if (target.equals("Source")) {
	        			targetResult = patternToString(document.getSource(), usingTargetStringField.getText());
	        		} else if (target.equals("Section")) {
	        			targetResult = patternToString(document.getSection(), usingTargetStringField.getText());
	        		} else if (target.equals("Type")) {
	        			targetResult = patternToString(document.getType(), usingTargetStringField.getText());
	        		} else if (target.equals("Notes")) {
	        			targetResult = patternToString(document.getNotes(), usingTargetStringField.getText());
	        		}

	        		String sourceResult = "";
	        		if (!sourceTranslatedIntoField.getText().equals(".+")) {
	        			sourceResult = sourceTranslatedIntoField.getText();
	        		} else if (source.equals("Title")) {
	        			sourceResult = patternToString(document.getTitle(), usingSourceStringField.getText());
	        		} else if (source.equals("Author")) {
	        			sourceResult = patternToString(document.getAuthor(), usingSourceStringField.getText());
	        		} else if (source.equals("Source")) {
	        			sourceResult = patternToString(document.getSource(), usingSourceStringField.getText());
	        		} else if (source.equals("Section")) {
	        			sourceResult = patternToString(document.getSection(), usingSourceStringField.getText());
	        		} else if (source.equals("Type")) {
	        			sourceResult = patternToString(document.getType(), usingSourceStringField.getText());
	        		} else if (source.equals("Notes")) {
	        			sourceResult = patternToString(document.getNotes(), usingSourceStringField.getText());
	        		}
	        		
	        		result = asFollowsField.getText();
	        		result = result.replaceAll("%source", sourceResult);
	        		result = result.replaceAll("%target", targetResult);
	        		
	        		if (onlyTable == false) {
	        			if (target.equals("Title")) {
	        				Dna.data.getDocument(document.getId()).setTitle(result);
	        			} else if (target.equals("Author")) {
	        				Dna.data.getDocument(document.getId()).setAuthor(result);
	        			} else if (target.equals("Source")) {
	        				Dna.data.getDocument(document.getId()).setSource(result);
	        			} else if (target.equals("Section")) {
	        				Dna.data.getDocument(document.getId()).setSection(result);
	        			} else if (target.equals("Type")) {
	        				Dna.data.getDocument(document.getId()).setType(result);
	        			} else if (target.equals("Notes")) {
	        				Dna.data.getDocument(document.getId()).setNotes(result);
	        			}
	        			Dna.dna.sql.upsertDocument(Dna.data.getDocument(document.getId()));
	        		}
	        	}
	        	if (onlyTable == true) {
	        		TableEntry entry = new TableEntry(document.getId(), sourceEntry, targetEntry, result);
	        		tableModel.addEntry(entry);
	        	}
				progressMonitor.setProgress(i);
	        }
	        if (onlyTable == false) {
    			Dna.gui.documentPanel.documentContainer.sort();
    			Dna.gui.documentPanel.documentTable.updateUI();
    			Dna.gui.documentPanel.documentTable.getSelectionModel().setSelectionInterval(0, 0);
    			Dna.gui.leftPanel.editDocPanel.createEditDocumentPanel(Dna.data.getDocuments().get(0));
				RecodeDialog.this.dispose();
	        }
		}
	}
	
	public String patternToString(String text, String pattern) {
		Pattern p;
		try {
			p = Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			return("");
		}
		Matcher m = p.matcher(text);
		if (m.find()) {
			try {
				String string = m.group(0);
			    return string;
			} catch (IndexOutOfBoundsException e) {
				return("");
			}
		} else {
			return "";
		}
	}
	
	class TableEntry {
		int id;
		String source;
		String oldTarget;
		
		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(int id) {
			this.id = id;
		}

		/**
		 * @return the source
		 */
		public String getSource() {
			return source;
		}

		/**
		 * @param source the source to set
		 */
		public void setSource(String source) {
			this.source = source;
		}

		/**
		 * @return the oldTarget
		 */
		public String getOldTarget() {
			return oldTarget;
		}

		/**
		 * @param oldTarget the oldTarget to set
		 */
		public void setOldTarget(String oldTarget) {
			this.oldTarget = oldTarget;
		}

		/**
		 * @return the newTarget
		 */
		public String getNewTarget() {
			return newTarget;
		}

		/**
		 * @param newTarget the newTarget to set
		 */
		public void setNewTarget(String newTarget) {
			this.newTarget = newTarget;
		}

		String newTarget;
		
		public TableEntry(int id, String source, String oldTarget, String newTarget) {
			this.id = id;
			this.source = source;
			this.oldTarget = oldTarget;
			this.newTarget = newTarget;
		}
	}
	
	class RecodeTableModel extends AbstractTableModel {
		private ArrayList<TableEntry> entries = new ArrayList<TableEntry>();
		
		public int getColumnCount() {
			return 4;
		}
		
		public int getRowCount() {
			return entries.size();
		}
		
		public String getColumnName(int column) {
			switch( column ){
				case 0: return "ID";
				case 1: return "Source field";
				case 2: return "Old target field";
				case 3: return "New target field";
				default: return null;
			}
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch( columnIndex ){
				case 0: return entries.get(rowIndex).getId(); 
				case 1: return entries.get(rowIndex).getSource();
				case 2: return entries.get(rowIndex).getOldTarget();
				case 3: return entries.get(rowIndex).getNewTarget();
				default: return null;
			}
		}
		
		public Class<?> getColumnClass(int columnIndex) {
			switch( columnIndex ){
				case 0: return Integer.class;
				case 1: return String.class;
				case 2: return String.class;
				case 3: return String.class;
				default: return null;
			}	
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				entries.get(rowIndex).setId((int) aValue);
			} else if (columnIndex == 1) {
				entries.get(rowIndex).setSource((String) aValue);
			} else if (columnIndex == 2) {
				entries.get(rowIndex).setOldTarget((String) aValue);
			} else if (columnIndex == 3) {
				entries.get(rowIndex).setNewTarget((String) aValue);
			}
			this.fireTableCellUpdated(rowIndex, columnIndex);
		}
		
		public void addEntry(TableEntry entry) {
			entries.add(entry);
			this.fireTableRowsInserted(entries.size(), entries.size());
		}
		
		public void clear() {
			int size = entries.size();
			entries.clear();
			this.fireTableRowsDeleted(0, size);
		}
	}
}
