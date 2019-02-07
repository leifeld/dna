package dna.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import dna.dataStructures.Document;
import dna.renderer.DocumentTableModel;

@SuppressWarnings("serial")
public class DocumentPanel extends JScrollPane {
	public DocumentTableModel documentContainer;
	public DocumentTable documentTable;
	TableRowSorter<DocumentTableModel> sorter;
	JScrollPane jsp;
	public JMenuItem menuItemDelete, menuItemResetTime;
	
	public DocumentPanel() {
		if(Dna.dna != null) {
			documentContainer = new DocumentTableModel(Dna.data.getDocuments());
		} else {
			documentContainer = new DocumentTableModel();
		}
		documentTable = new DocumentTable();
		documentTable.setModel(documentContainer);
		this.setViewportView(documentTable);
		this.setPreferredSize(new Dimension(700, 100));
		
		setRowSorterEnabled(true);

		TableColumn column[] = new TableColumn[11];
	    for (int i = 0; i < column.length; i++) {
	        column[i] = documentTable.getColumnModel().getColumn(i);
	    }
	    Boolean[] columnsVisible = new Boolean[] {false, true, true, true, false, false, false, false, false, false, false};
		while (documentTable.getColumnModel().getColumnCount() > 0) {
			documentTable.getColumnModel().removeColumn(documentTable.getColumnModel().getColumn(0));
	    }
	    for (int i = 0; i < columnsVisible.length; i++) {
	    	if (columnsVisible[i] == true) {
	    		documentTable.getColumnModel().addColumn(column[i]);
	    	}
	    }
		documentTable.getColumnModel().getColumn(0).setPreferredWidth(600);
		documentTable.getColumnModel().getColumn(1).setPreferredWidth(30);
		documentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
	    
		JPopupMenu popupMenu = new JPopupMenu();
		menuItemDelete = new JMenuItem("Delete selected document(s)");
		popupMenu.add(menuItemDelete);
		menuItemResetTime = new JMenuItem("Set document time to 00:00:00");
		popupMenu.add(menuItemResetTime);
		JSeparator sep = new JSeparator();
		popupMenu.add(sep);
		JCheckBoxMenuItem menuItemId = new JCheckBoxMenuItem("ID", false);
		popupMenu.add(menuItemId);
		JCheckBoxMenuItem menuItemTitle = new JCheckBoxMenuItem("Title", true);
		popupMenu.add(menuItemTitle);
		JCheckBoxMenuItem menuItemNumber = new JCheckBoxMenuItem("#", true);
		popupMenu.add(menuItemNumber);
		JCheckBoxMenuItem menuItemDate = new JCheckBoxMenuItem("Date", true);
		popupMenu.add(menuItemDate);
		JCheckBoxMenuItem menuItemTime = new JCheckBoxMenuItem("Time", false);
		popupMenu.add(menuItemTime);
		JCheckBoxMenuItem menuItemCoder = new JCheckBoxMenuItem("Coder", false);
		popupMenu.add(menuItemCoder);
		JCheckBoxMenuItem menuItemAuthor = new JCheckBoxMenuItem("Author", false);
		popupMenu.add(menuItemAuthor);
		JCheckBoxMenuItem menuItemSource = new JCheckBoxMenuItem("Source", false);
		popupMenu.add(menuItemSource);
		JCheckBoxMenuItem menuItemSection = new JCheckBoxMenuItem("Section", false);
		popupMenu.add(menuItemSection);
		JCheckBoxMenuItem menuItemType = new JCheckBoxMenuItem("Type", false);
		popupMenu.add(menuItemType);
		JCheckBoxMenuItem menuItemNotes = new JCheckBoxMenuItem("Notes", false);
		popupMenu.add(menuItemNotes);
		documentTable.setComponentPopupMenu(popupMenu);
		
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == menuItemDelete) {
					int[] selectedRows = documentTable.getSelectedRows();
					String message = "Are you sure you want to delete " + selectedRows.length + " document(s) including all statements?";
					int dialog = JOptionPane.showConfirmDialog(Dna.gui, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						Dna.dna.removeDocuments(selectedRows);
					}
				} else if (e.getSource() == menuItemResetTime) {
					int[] selectedRows = documentTable.getSelectedRows();
					String message = "";
					if (selectedRows.length == 1) {
						message = "Are you sure you want to reset the time stamp of the selected document?";
					} else {
						message = "Are you sure you want to reset the time stamp of these " + selectedRows.length + " documents?";
					}
					int dialog = JOptionPane.showConfirmDialog(Dna.gui, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						Dna.dna.resetTimeOfDocuments(selectedRows);
						if (selectedRows.length == 1) {
							Dna.gui.leftPanel.editDocPanel.createEditDocumentPanel(Dna.data.getDocuments().get(selectedRows[0]));
						}
					}
				} else {
					if (e.getSource() == menuItemId) {
						if (menuItemId.isSelected()) {
							columnsVisible[0] = true;
						} else {
							columnsVisible[0] = false;
						}
					} else if (e.getSource() == menuItemTitle) {
						if (menuItemTitle.isSelected()) {
							columnsVisible[1] = true;
						} else {
							columnsVisible[1] = false;
						}
					} else if (e.getSource() == menuItemNumber) {
						if (menuItemNumber.isSelected()) {
							columnsVisible[2] = true;
						} else {
							columnsVisible[2] = false;
						}
					} else if (e.getSource() == menuItemDate) {
						if (menuItemDate.isSelected()) {
							columnsVisible[3] = true;
						} else {
							columnsVisible[3] = false;
						}
					} else if (e.getSource() == menuItemTime) {
						if (menuItemTime.isSelected()) {
							columnsVisible[4] = true;
						} else {
							columnsVisible[4] = false;
						}
					} else if (e.getSource() == menuItemCoder) {
						if (menuItemCoder.isSelected()) {
							columnsVisible[5] = true;
						} else {
							columnsVisible[5] = false;
						}
					} else if (e.getSource() == menuItemAuthor) {
						if (menuItemAuthor.isSelected()) {
							columnsVisible[6] = true;
						} else {
							columnsVisible[6] = false;
						}
					} else if (e.getSource() == menuItemSource) {
						if (menuItemSource.isSelected()) {
							columnsVisible[7] = true;
						} else {
							columnsVisible[7] = false;
						}
					} else if (e.getSource() == menuItemSection) {
						if (menuItemSection.isSelected()) {
							columnsVisible[8] = true;
						} else {
							columnsVisible[8] = false;
						}
					} else if (e.getSource() == menuItemType) {
						if (menuItemType.isSelected()) {
							columnsVisible[9] = true;
						} else {
							columnsVisible[9] = false;
						}
					} else if (e.getSource() == menuItemNotes) {
						if (menuItemNotes.isSelected()) {
							columnsVisible[10] = true;
						} else {
							columnsVisible[10] = false;
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
			}
		};
		
		menuItemDelete.addActionListener(al);
		menuItemResetTime.addActionListener(al);
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
	
	public void setRowSorterEnabled(boolean enabled) {
		if (enabled == true) {
			sorter = new TableRowSorter<DocumentTableModel>(documentContainer);
			documentTable.setRowSorter(sorter);
		} else {
			documentTable.setRowSorter(null);
		}
	}
	
	public void documentFilter() {
		RowFilter<DocumentTableModel, Integer> documentFilter = new RowFilter<DocumentTableModel, Integer>() {
			public boolean include(Entry<? extends DocumentTableModel, ? extends Integer> entry) {
				DocumentTableModel dtm = entry.getModel();
				Document d = dtm.get(entry.getIdentifier());
				int documentId = d.getId();
				boolean[] b = Dna.data.getActiveDocumentPermissions(documentId);
				if (b[0] == true) {
					return true;
				}
				return false;
			}
		};
		sorter.setRowFilter(documentFilter);
	}
	
	public class DocumentTable extends JTable {
		
		public DocumentTable() {
			setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			getTableHeader().setReorderingAllowed(false);
			putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

			getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting()) {
						return;
					}
					updateDocumentView();
				}
			});
		}
		
		public void updateDocumentView() {
			int rowCount = this.getSelectedRowCount();
			if (rowCount == 0 || rowCount > 1) {
				Dna.gui.previousDocID = -1;
				Dna.gui.textPanel.setDocumentText("");
				Dna.gui.leftPanel.editDocPanel.createEditDocumentPanel();
				Dna.gui.leftPanel.editDocPanel.updateUI();
			} else if (rowCount == 1) {
				int selectedRow = getSelectedRow();
				int selectedModelIndex = this.convertRowIndexToModel(selectedRow);
				int id = documentContainer.get(selectedModelIndex).getId();
				Dna.gui.previousDocID = id;
				Document document = documentContainer.getDocumentByID(id);
				
				String text = document.getText();
				Dna.gui.textPanel.setDocumentId(id);
				Dna.gui.textPanel.setDocumentText(text);
				Dna.gui.textPanel.setEnabled(true);
				
				boolean[] b = Dna.data.getActiveDocumentPermissions(id);
				if (b[0] == true && b[1] == true) {
					Dna.gui.leftPanel.editDocPanel.createEditDocumentPanel(Dna.data.getDocuments().get(selectedModelIndex));
				} else {
					Dna.gui.leftPanel.editDocPanel.createEditDocumentPanel();
				}
				Dna.gui.leftPanel.editDocPanel.updateUI();
			} else {
				System.err.println("Negative number of rows in the document table!");
			}
			if (Dna.gui.rightPanel.statementPanel.statementFilter.showCurrent.isSelected()) {
				Dna.gui.rightPanel.statementPanel.statementFilter.currentDocumentFilter();
			}
			
			if (Dna.dna.sql != null) {
				Dna.gui.textPanel.paintStatements();
			}
			Dna.gui.textPanel.setCaretPosition(0);
			
			int ac = Dna.data.getActiveCoder();
			if (Dna.gui.leftPanel.editDocPanel.saveDetailsButton != null) {
				if (Dna.dna.sql == null || Dna.data.getCoderById(ac).getPermissions().get("editDocuments") == false) {
					Dna.gui.leftPanel.editDocPanel.saveDetailsButton.setEnabled(false);
					Dna.gui.leftPanel.editDocPanel.cancelButton.setEnabled(false);
				} else {
					Dna.gui.leftPanel.editDocPanel.saveDetailsButton.setEnabled(true);
					Dna.gui.leftPanel.editDocPanel.cancelButton.setEnabled(true);
				}
			}
			
			if (Dna.dna.sql == null || Dna.data.getCoderById(ac).getPermissions().get("deleteDocuments") == false) {
				Dna.gui.documentPanel.menuItemDelete.setEnabled(false);
			} else {
				Dna.gui.documentPanel.menuItemDelete.setEnabled(true);
			}
			
			if (Dna.dna.sql == null || Dna.data.getCoderById(ac).getPermissions().get("addDocuments") == false) {
				Dna.gui.menuBar.newDocumentButton.setEnabled(false);
			} else {
				Dna.gui.menuBar.newDocumentButton.setEnabled(true);
			}
		}
	}
}
