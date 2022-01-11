package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import gui.MainWindow.ActionAddDocument;
import gui.MainWindow.ActionEditDocuments;
import gui.MainWindow.ActionRemoveDocuments;
import model.Coder;
import model.TableDocument;

class DocumentTablePanel extends JPanel {
	private static final long serialVersionUID = 4543056929753553570L;
	private JTable documentTable;
	private DocumentTableModel documentTableModel;
	private String documentFilterPattern = "";

	DocumentTablePanel(DocumentTableModel documentTableModel,
			ActionAddDocument actionAddDocument,
			ActionRemoveDocuments actionRemoveDocuments,
			ActionEditDocuments actionEditDocuments) {
		this.setLayout(new BorderLayout());
		this.documentTableModel = documentTableModel;
		documentTable = new JTable(documentTableModel);
		documentTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		TableRowSorter<DocumentTableModel> sorter = new TableRowSorter<DocumentTableModel>(documentTableModel);
		documentTable.setRowSorter(sorter);

		// set column visibility
		TableColumn column[] = new TableColumn[11];
	    for (int i = 0; i < column.length; i++) {
	        column[i] = documentTable.getColumnModel().getColumn(i);
	    }
	    Boolean[] columnsVisible = new Boolean[] {true, true, true, true, true, true, true, true, true, true, true};
	    
		documentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		documentTable.getColumnModel().getColumn(1).setPreferredWidth(600);
		documentTable.getColumnModel().getColumn(2).setPreferredWidth(30);
		documentTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		documentTable.getColumnModel().getColumn(4).setPreferredWidth(80);
		documentTable.getColumnModel().getColumn(5).setPreferredWidth(110);
		
		// document table cell renderer
        documentTable.setDefaultRenderer(Coder.class, new CoderTableCellRenderer());

		JScrollPane documentTableScroller = new JScrollPane(documentTable);
		documentTableScroller.setViewportView(documentTable);
		documentTableScroller.setPreferredSize(new Dimension(1000, 200));
		this.add(documentTableScroller, BorderLayout.CENTER);

		// row filter
		RowFilter<DocumentTableModel, Integer> documentFilter = new RowFilter<DocumentTableModel, Integer>() {
			public boolean include(Entry<? extends DocumentTableModel, ? extends Integer> entry) {
				if (Dna.sql.getActiveCoder() == null || Dna.sql.getConnectionProfile() == null) {
					return false;
				}
				TableDocument d = documentTableModel.getRow(entry.getIdentifier());
				if (d.getCoder().getId() != Dna.sql.getActiveCoder().getId()) {
					if (Dna.sql.getActiveCoder().isPermissionViewOthersDocuments() == false) {
						return false;
					} else if (Dna.sql.getActiveCoder().isPermissionViewOthersDocuments(d.getCoder().getId()) == false) {
						return false;
					}
				}
				try {
					Pattern pattern = Pattern.compile(documentFilterPattern);
					Matcher matcherTitle = pattern.matcher(d.getTitle());
					Matcher matcherAuthor = pattern.matcher(d.getAuthor());
					Matcher matcherSource = pattern.matcher(d.getSource());
					Matcher matcherSection = pattern.matcher(d.getSection());
					Matcher matcherType = pattern.matcher(d.getType());
					Matcher matcherNotes = pattern.matcher(d.getTitle());
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy HH:mm");
					Matcher matcherDateTime = pattern.matcher(d.getDateTime().format(formatter));
					if (documentFilterPattern.equals("")) {
						return true;
					} else if (matcherTitle.find()) {
						return true;
					} else if (matcherAuthor.find()) {
						return true;
					} else if (matcherSource.find()) {
						return true;
					} else if (matcherSection.find()) {
						return true;
					} else if (matcherType.find()) {
						return true;
					} else if (matcherNotes.find()) {
						return true;
					} else if (matcherDateTime.find()) {
						return true;
					} else {
						return false;
					}
				} catch(PatternSyntaxException pse) {
					return true;
				}
				
			}
		};
		sorter.setRowFilter(documentFilter);

	    // right-click menu for document table
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuItemAddDocument = new JMenuItem(actionAddDocument);
		popupMenu.add(menuItemAddDocument);
		JMenuItem menuItemDelete = new JMenuItem(actionRemoveDocuments);
		popupMenu.add(menuItemDelete);
		JMenuItem menuItemEdit = new JMenuItem(actionEditDocuments);
		popupMenu.add(menuItemEdit);
		JSeparator sep = new JSeparator();
		popupMenu.add(sep);
		ImageIcon checkedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-checkbox.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		ImageIcon uncheckedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-square.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JMenuItem menuItemId = new JMenuItem("ID", checkedIcon);
		popupMenu.add(menuItemId);
		JMenuItem menuItemTitle = new JMenuItem("Title", checkedIcon);
		popupMenu.add(menuItemTitle);
		JMenuItem menuItemNumber = new JMenuItem("#", checkedIcon);
		popupMenu.add(menuItemNumber);
		JMenuItem menuItemDate = new JMenuItem("Date", checkedIcon);
		popupMenu.add(menuItemDate);
		JMenuItem menuItemTime = new JMenuItem("Time", checkedIcon);
		popupMenu.add(menuItemTime);
		JMenuItem menuItemCoder = new JMenuItem("Coder", checkedIcon);
		popupMenu.add(menuItemCoder);
		JMenuItem menuItemAuthor = new JMenuItem("Author", checkedIcon);
		popupMenu.add(menuItemAuthor);
		JMenuItem menuItemSource = new JMenuItem("Source", checkedIcon);
		popupMenu.add(menuItemSource);
		JMenuItem menuItemSection = new JMenuItem("Section", checkedIcon);
		popupMenu.add(menuItemSection);
		JMenuItem menuItemType = new JMenuItem("Type", checkedIcon);
		popupMenu.add(menuItemType);
		JMenuItem menuItemNotes = new JMenuItem("Notes", checkedIcon);
		popupMenu.add(menuItemNotes);
		documentTable.setComponentPopupMenu(popupMenu);
		documentTable.getTableHeader().setComponentPopupMenu(popupMenu);
		documentTableScroller.setComponentPopupMenu(popupMenu);
		
		// ActionListener with actions for right-click document menu
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == menuItemId) {
					if (columnsVisible[0] == false) {
						columnsVisible[0] = true;
						menuItemId.setIcon(checkedIcon);
					} else {
						columnsVisible[0] = false;
						menuItemId.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemTitle) {
					if (columnsVisible[1] == false) {
						columnsVisible[1] = true;
						menuItemTitle.setIcon(checkedIcon);
					} else {
						columnsVisible[1] = false;
						menuItemTitle.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemNumber) {
					if (columnsVisible[2] == false) {
						columnsVisible[2] = true;
						menuItemNumber.setIcon(checkedIcon);
					} else {
						columnsVisible[2] = false;
						menuItemNumber.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemDate) {
					if (columnsVisible[3] == false) {
						columnsVisible[3] = true;
						menuItemDate.setIcon(checkedIcon);
					} else {
						columnsVisible[3] = false;
						menuItemDate.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemTime) {
					if (columnsVisible[4] == false) {
						columnsVisible[4] = true;
						menuItemTime.setIcon(checkedIcon);
					} else {
						columnsVisible[4] = false;
						menuItemTime.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemCoder) {
					if (columnsVisible[5] == false) {
						columnsVisible[5] = true;
						menuItemCoder.setIcon(checkedIcon);
					} else {
						columnsVisible[5] = false;
						menuItemCoder.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemAuthor) {
					if (columnsVisible[6] == false) {
						columnsVisible[6] = true;
						menuItemAuthor.setIcon(checkedIcon);
					} else {
						columnsVisible[6] = false;
						menuItemAuthor.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemSource) {
					if (columnsVisible[7] == false) {
						columnsVisible[7] = true;
						menuItemSource.setIcon(checkedIcon);
					} else {
						columnsVisible[7] = false;
						menuItemSource.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemSection) {
					if (columnsVisible[8] == false) {
						columnsVisible[8] = true;
						menuItemSection.setIcon(checkedIcon);
					} else {
						columnsVisible[8] = false;
						menuItemSection.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemType) {
					if (columnsVisible[9] == false) {
						columnsVisible[9] = true;
						menuItemType.setIcon(checkedIcon);
					} else {
						columnsVisible[9] = false;
						menuItemType.setIcon(uncheckedIcon);
					}
				} else if (e.getSource() == menuItemNotes) {
					if (columnsVisible[10] == false) {
						columnsVisible[10] = true;
						menuItemNotes.setIcon(checkedIcon);
					} else {
						columnsVisible[10] = false;
						menuItemNotes.setIcon(uncheckedIcon);
					}
				}

				while (documentTable.getColumnModel().getColumnCount() > 0) {
					documentTable.getColumnModel().removeColumn(documentTable.getColumnModel().getColumn(0));
				}

				for (int i = 0; i < columnsVisible.length; i++) {
					if (columnsVisible[i] == true) {
						documentTable.getColumnModel().addColumn(column[i]);
					}
				}
			}
		};

		menuItemId.addActionListener(al);
		menuItemTitle.addActionListener(al);
		menuItemNumber.addActionListener(al);
		menuItemDate.addActionListener(al);
		menuItemTime.addActionListener(al);
		menuItemCoder.addActionListener(al);
		menuItemAuthor.addActionListener(al);
		menuItemSource.addActionListener(al);
		menuItemSection.addActionListener(al);
		menuItemType.addActionListener(al);
		menuItemNotes.addActionListener(al);
	}
	
	/**
	 * Get a reference to the document table.
	 * 
	 * @return The document table.
	 */
	JTable getDocumentTable() {
		return documentTable;
	}
	
	/**
	 * Return the indices of the rows that are currently selected in the
	 * document table.
	 * 
	 * @return A one-dimensional integer array of row indices in the table.
	 */
	int[] getSelectedRows() {
		return documentTable.getSelectedRows();
	}

	/**
	 * Retrieve the ID of the document that is currently selected in the table.
	 * 
	 * @return  The document ID. Can be {@code -1} if nothing is selected.
	 */
	int getSelectedDocumentId() {
		int viewRow = this.documentTable.getSelectedRow();
		if (viewRow > -1) {
			int modelRow = this.convertRowIndexToModel(viewRow);
			int id = this.documentTableModel.getIdByModelRow(modelRow);
			return id;
		} else {
			return -1;
		}
	}

	/**
	 * Select a document in the table based on its ID.
	 * 
	 * @param documentId  ID of the document to be selected in the table.
	 */
	void setSelectedDocumentId(int documentId) {
		if (documentId > -1) {
			int modelRow = this.documentTableModel.getModelRowById(documentId);
			if (modelRow > -1) {
				int index = documentTable.convertRowIndexToView(modelRow);
				if (index > -1) {
					this.documentTable.setRowSelectionInterval(index, index);
				}
			}
		}
	}

	/**
	 * Convert a row index in the document table to a document table model index.
	 * 
	 * @param rowIndex The row index in the table to convert.
	 * @return The row index in the table model corresponding to the table row index.
	 */
	int convertRowIndexToModel(int rowIndex) {
		return documentTable.convertRowIndexToModel(rowIndex);
	}
	
	/**
	 * Set the document filter pattern.
	 * 
	 * @param documentFilterPattern The document filter pattern.
	 */
	void setDocumentFilterPattern(String documentFilterPattern) {
		this.documentFilterPattern = documentFilterPattern;
	}
}