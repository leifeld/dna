package dna;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import dna.dataStructures.Document;
import dna.dataStructures.StatementType;
import dna.panels.StatementPanel.CustomFilterPanel;
import dna.renderer.DocumentTableModel;

@SuppressWarnings("serial")
public class DocumentPanel extends JPanel {
	public DocumentTableModel documentContainer;
	public DocumentTable documentTable;
	TableRowSorter<DocumentTableModel> sorter;
	JScrollPane jsp;

	public DocumentPanel() {
		this.setLayout(new BorderLayout());
		if(Dna.dna != null) {
			documentContainer = new DocumentTableModel(Dna.data.getDocuments());
		} else {
			documentContainer = new DocumentTableModel();
		}
		documentTable = new DocumentTable();
		documentTable.setModel(documentContainer);
		jsp = new JScrollPane();
		jsp.setViewportView(documentTable);
		jsp.setPreferredSize(new Dimension(700, 100));
		documentTable.getColumnModel().getColumn(0).setPreferredWidth(600);
		documentTable.getColumnModel().getColumn(1).setPreferredWidth(30);
		documentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		
		setRowSorterEnabled(true);
		this.add(jsp, BorderLayout.NORTH);
		
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuItemDelete = new JMenuItem("Delete selected document(s)", new ImageIcon(getClass().getResource("/icons/trash.png")));
		popupMenu.add(menuItemDelete);
		documentTable.setComponentPopupMenu(popupMenu);
		
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == menuItemDelete) {
					int[] selectedRows = documentTable.getSelectedRows();
					String message = "Are you sure you want to delete " + selectedRows.length + " document(s) including all statements?";
					int dialog = JOptionPane.showConfirmDialog(Dna.dna.gui, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						Dna.dna.removeDocuments(selectedRows);
					}
				}
			}
		};
		
		menuItemDelete.addActionListener(al);
	}
	
	public void setRowSorterEnabled(boolean enabled) {
		if (enabled == true) {
			sorter = new TableRowSorter<DocumentTableModel>(documentContainer);
			/*
			sorter = new TableRowSorter<DocumentTableModel>(documentContainer) {
				public void toggleSortOrder(int i) {
					//leave blank; overwritten method makes the table unsortable
				}
			};
			*/
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
			//setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
				Dna.dna.gui.previousDocID = -1;
				Dna.dna.gui.textPanel.setDocumentText("");
				Dna.dna.gui.menuBar.removeDocumentButton.setEnabled(false);
				Dna.dna.gui.leftPanel.editDocPanel.createEditDocumentPanel();
				Dna.dna.gui.leftPanel.editDocPanel.updateUI();
			} else if (rowCount == 1) {
				int selectedRow = getSelectedRow();
				int selectedModelIndex = this.convertRowIndexToModel(selectedRow);
				int id = documentContainer.get(selectedModelIndex).getId();
				//int id = documentContainer.get(selectedRow).getId();
				Dna.dna.gui.previousDocID = id;
				Document document = documentContainer.getDocumentByID(id);
				
				String text = document.getText();
				Dna.dna.gui.textPanel.setDocumentId(id);
				Dna.dna.gui.textPanel.setDocumentText(text);
				Dna.dna.gui.textPanel.setEnabled(true);
				Dna.dna.gui.menuBar.removeDocumentButton.setEnabled(true);
				
				boolean[] b = Dna.data.getActiveDocumentPermissions(id);
				if (b[0] == true && b[1] == true) {
					//Dna.dna.gui.leftPanel.editDocPanel.createEditDocumentPanel(Dna.data.getDocuments().get(selectedRow));
					Dna.dna.gui.leftPanel.editDocPanel.createEditDocumentPanel(Dna.data.getDocuments().get(selectedModelIndex));
				} else {
					Dna.dna.gui.leftPanel.editDocPanel.createEditDocumentPanel();
				}
				Dna.dna.gui.leftPanel.editDocPanel.updateUI();
			} else {
				System.err.println("Negative number of rows in the document table!");
			}
			if (Dna.dna.gui.rightPanel.statementPanel.statementFilter.showCurrent.isSelected()) {
				Dna.dna.gui.rightPanel.statementPanel.statementFilter.currentDocumentFilter();
			} else if (Dna.dna.gui.rightPanel.statementPanel.statementFilter.showFilter.isSelected()) {
				Dna.dna.gui.rightPanel.statementPanel.statementFilter.updateFilter();
			}
			
			if (Dna.dna.sql != null) {
				Dna.dna.gui.textPanel.paintStatements();
			}
			Dna.dna.gui.textPanel.setCaretPosition(0);
			
			int ac = Dna.data.getActiveCoder();
			if (Dna.dna.gui.leftPanel.editDocPanel.saveDetailsButton != null) {
				if (Dna.dna.sql == null || Dna.data.getCoderById(ac).getPermissions().get("editDocuments") == false) {
					Dna.dna.gui.leftPanel.editDocPanel.saveDetailsButton.setEnabled(false);
					Dna.dna.gui.leftPanel.editDocPanel.cancelButton.setEnabled(false);
				} else {
					Dna.dna.gui.leftPanel.editDocPanel.saveDetailsButton.setEnabled(true);
					Dna.dna.gui.leftPanel.editDocPanel.cancelButton.setEnabled(true);
				}
			}
			
			if (Dna.dna.sql == null || Dna.data.getCoderById(ac).getPermissions().get("deleteDocuments") == false) {
				Dna.dna.gui.menuBar.removeDocumentButton.setEnabled(false);
			} else {
				Dna.dna.gui.menuBar.removeDocumentButton.setEnabled(true);
			}
			
			if (Dna.dna.sql == null || Dna.data.getCoderById(ac).getPermissions().get("addDocuments") == false) {
				Dna.dna.gui.menuBar.newDocumentButton.setEnabled(false);
			} else {
				Dna.dna.gui.menuBar.newDocumentButton.setEnabled(true);
			}
		}
	}
}
