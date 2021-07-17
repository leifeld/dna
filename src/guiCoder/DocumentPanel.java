package guiCoder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;

@SuppressWarnings("serial")
class DocumentPanel extends JPanel {
	private DocumentTableModel documentTableModel;
	private TextPanel textPanel;
	private JTable documentTable;

	public DocumentPanel(DocumentTableModel documentTableModel) {
		this.documentTableModel = documentTableModel;
		this.setLayout(new BorderLayout());
		
		// toolbar of the document panel
		JToolBar tb = new JToolBar("Document toolbar");
		
		Icon addDocumentIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-file-plus-16.png")); // https://tabler-icons.io/i/file-plus
		JButton addDocumentButton = new JButton("Add", addDocumentIcon);
		addDocumentButton.setToolTipText( "Add document" );
		addDocumentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocumentEditor de = new DocumentEditor();
				if (de.getDocuments() != null) {
					Dna.sql.addDocuments(de.getDocuments());
					documentTableModel.reloadTableFromSQL();
				}
			}
		});
		tb.add(addDocumentButton);

		Icon removeDocumentIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-file-minus-16.png")); // https://tabler-icons.io/i/file-minus
		JButton removeDocumentButton = new JButton("Delete", removeDocumentIcon);
		removeDocumentButton.setToolTipText( "Remove document" );
		removeDocumentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = documentTable.getSelectedRows();
				String message = "Are you sure you want to delete " + selectedRows.length + " document(s) including all statements?";
				int dialog = JOptionPane.showConfirmDialog(null, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
				if (dialog == 0) {
					for (int i = 0; i < selectedRows.length; i++) {
						selectedRows[i] = documentTable.convertRowIndexToModel(selectedRows[i]);
					}
					documentTableModel.removeDocuments(selectedRows);
				}
				Dna.guiCoder.updateGUI();
			}
		});
		tb.add(removeDocumentButton);

		Icon editDocumentIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-edit-16.png"));
		JButton editDocumentButton = new JButton("Edit", editDocumentIcon);
		editDocumentButton.setToolTipText( "Edit document" );
		editDocumentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = documentTable.getSelectedRows();
				for (int i = 0; i < selectedRows.length; i++) {
					selectedRows[i] = documentTableModel.getIdByModelRow(documentTable.convertRowIndexToModel(selectedRows[i]));
				}
				DocumentEditor de = new DocumentEditor(selectedRows); // TODO: implement document editing infrastructure
			}
		});
		tb.add(editDocumentButton);

        tb.setRollover(true);
		this.add(tb, BorderLayout.NORTH);

		// create document table and model
		documentTable = new JTable(this.documentTableModel);
		documentTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		TableRowSorter<DocumentTableModel> sorter = new TableRowSorter<DocumentTableModel>(documentTableModel);
		documentTable.setRowSorter(sorter);

		// set column visibility
		TableColumn column[] = new TableColumn[11];
	    for (int i = 0; i < column.length; i++) {
	        column[i] = documentTable.getColumnModel().getColumn(i);
	    }
	    Boolean[] columnsVisible = new Boolean[] {true, true, true, true, true, true, true, true, true, true, true};
		while (documentTable.getColumnModel().getColumnCount() > 0) {
			documentTable.getColumnModel().removeColumn(documentTable.getColumnModel().getColumn(0));
	    }
	    for (int i = 0; i < columnsVisible.length; i++) {
	    	if (columnsVisible[i] == true) {
	    		documentTable.getColumnModel().addColumn(column[i]);
	    	}
	    }
		documentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		documentTable.getColumnModel().getColumn(1).setPreferredWidth(600);
		documentTable.getColumnModel().getColumn(2).setPreferredWidth(30);
		documentTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		documentTable.getColumnModel().getColumn(4).setPreferredWidth(80);
		documentTable.getColumnModel().getColumn(5).setPreferredWidth(110);
		
		// document table cell renderers
        documentTable.setDefaultRenderer(Coder.class, new CoderTableCellRenderer());

		JScrollPane documentTableScroller = new JScrollPane(documentTable);
		documentTableScroller.setViewportView(documentTable);
		documentTableScroller.setPreferredSize(new Dimension(1000, 200));
		this.add(documentTableScroller, BorderLayout.CENTER);
		
	    // right-click menu for document table
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuItemAddDocument = new JMenuItem("Add new document");
		popupMenu.add(menuItemAddDocument);
		JMenuItem menuItemDelete = new JMenuItem("Delete selected document(s)");
		popupMenu.add(menuItemDelete);
		JMenuItem menuItemResetTime = new JMenuItem("Set document time to 00:00:00");
		popupMenu.add(menuItemResetTime);
		JSeparator sep = new JSeparator();
		popupMenu.add(sep);
		JCheckBoxMenuItem menuItemId = new JCheckBoxMenuItem("ID", true);
		popupMenu.add(menuItemId);
		JCheckBoxMenuItem menuItemTitle = new JCheckBoxMenuItem("Title", true);
		popupMenu.add(menuItemTitle);
		JCheckBoxMenuItem menuItemNumber = new JCheckBoxMenuItem("#", true);
		popupMenu.add(menuItemNumber);
		JCheckBoxMenuItem menuItemDate = new JCheckBoxMenuItem("Date", true);
		popupMenu.add(menuItemDate);
		JCheckBoxMenuItem menuItemTime = new JCheckBoxMenuItem("Time", true);
		popupMenu.add(menuItemTime);
		JCheckBoxMenuItem menuItemCoder = new JCheckBoxMenuItem("Coder", true);
		popupMenu.add(menuItemCoder);
		JCheckBoxMenuItem menuItemAuthor = new JCheckBoxMenuItem("Author", true);
		popupMenu.add(menuItemAuthor);
		JCheckBoxMenuItem menuItemSource = new JCheckBoxMenuItem("Source", true);
		popupMenu.add(menuItemSource);
		JCheckBoxMenuItem menuItemSection = new JCheckBoxMenuItem("Section", true);
		popupMenu.add(menuItemSection);
		JCheckBoxMenuItem menuItemType = new JCheckBoxMenuItem("Type", true);
		popupMenu.add(menuItemType);
		JCheckBoxMenuItem menuItemNotes = new JCheckBoxMenuItem("Notes", true);
		popupMenu.add(menuItemNotes);
		documentTable.setComponentPopupMenu(popupMenu);
		documentTable.getTableHeader().setComponentPopupMenu(popupMenu);
		documentTableScroller.setComponentPopupMenu(popupMenu);
		
		// disable document right-click menu items if no document was selected
		documentTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (documentTable.getSelectedRowCount() > 0) {
					menuItemDelete.setEnabled(true);
				} else {
					menuItemDelete.setEnabled(false);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				mouseClicked(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				mouseClicked(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mouseClicked(e);
			}
		});
		
		// ActionListener with actions for right-click document menu
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == menuItemAddDocument) {
					DocumentEditor d = new DocumentEditor();
					if (d.getDocuments() != null) {
						Dna.sql.addDocuments(d.getDocuments());
						documentTableModel.reloadTableFromSQL();
					}
				} else if (e.getSource() == menuItemDelete) {
					int[] selectedRows = documentTable.getSelectedRows();
					String message = "Are you sure you want to delete " + selectedRows.length + " document(s) including all statements?";
					int dialog = JOptionPane.showConfirmDialog(null, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						for (int i = 0; i < selectedRows.length; i++) {
							selectedRows[i] = documentTable.convertRowIndexToModel(selectedRows[i]);
						}
						documentTableModel.removeDocuments(selectedRows);
					}
					Dna.guiCoder.updateGUI();
				} else if (e.getSource() == menuItemResetTime) {
					int[] selectedRows = documentTable.getSelectedRows();
					String message = "";
					if (selectedRows.length == 1) {
						message = "Are you sure you want to reset the time stamp of the selected document?";
					} else {
						message = "Are you sure you want to reset the time stamp of these " + selectedRows.length + " documents?";
					}
					int dialog = JOptionPane.showConfirmDialog(null, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						//TODO: create ChangeMultipleDocumentPropertiesEvent and hand over to event stack
						//Dna.dna.resetTimeOfDocuments(selectedRows);
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
		
		menuItemAddDocument.addActionListener(al);
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
		

		documentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				
				int rowCount = documentTable.getSelectedRowCount();
				if (rowCount == 0 || rowCount > 1) {
					textPanel.setDocumentText("");
				} else if (rowCount == 1) {
					int selectedRow = documentTable.getSelectedRow();
					int selectedModelIndex = documentTable.convertRowIndexToModel(selectedRow);
					int id = (int) documentTableModel.getValueAt(selectedModelIndex, 0);
					String text = documentTableModel.getDocumentText(id);
					textPanel.setDocumentText(text);
					//Dna.gui.textPanel.setEnabled(true);
				} else {
					System.err.println("Negative number of rows in the document table!");
				}
				// if (Dna.gui.rightPanel.statementPanel.statementFilter.showCurrent.isSelected()) {
				// 	Dna.gui.rightPanel.statementPanel.statementFilter.currentDocumentFilter();
				// }
				
				// if (Dna.dna.sql != null) {
				// 	Dna.gui.textPanel.paintStatements();
				// }
				textPanel.setCaretPosition(0);
				
				/*
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
				*/
			}
		});
		
		// text panel
		textPanel = new TextPanel();
		this.add(textPanel, BorderLayout.SOUTH);
	}
	
	/*
	private void selectDocument(int documentId) {
		int index = documentTable.convertRowIndexToView(this.documentTableModel.getModelRowById(documentId));
		this.documentTable.setRowSelectionInterval(index, index);
	}
	*/
	
	private class CoderTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value == null) {
				return new JLabel("");
			} else {
				Coder coder = (Coder) value;
				JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
				if (isSelected) {
					UIDefaults defaults = javax.swing.UIManager.getDefaults();
					Color bg = defaults.getColor("List.selectionBackground");
					panel.setBackground(bg);
				}
				JButton colorButton = (new JButton() {
					public void paintComponent(Graphics g) {
						super.paintComponent(g);
						g.setColor(coder.getColor());
						g.fillRect(2, 2, 14, 14);
					}
				});
				colorButton.setPreferredSize(new Dimension(14, 14));
				colorButton.setEnabled(false);
				panel.add(colorButton);

				String name = coder.getName();

				int nameLength = name.length();
				if (nameLength > 22) {
					nameLength = 22 - 3;
					name = name.substring(0,  nameLength);
					name = name + "...";
				}

				panel.add(new JLabel(name));
				return panel;
			}
		}
	}
}