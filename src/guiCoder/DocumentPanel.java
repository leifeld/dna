package guiCoder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
	public JMenuItem addDocumentItem, removeDocumentsItem, editDocumentsItem;
	public AddDocumentAction addDocumentAction;
	public RemoveDocumentsAction removeDocumentsAction;
	public EditDocumentsAction editDocumentsAction;

	public DocumentPanel(DocumentTableModel documentTableModel) {
		this.documentTableModel = documentTableModel;
		this.setLayout(new BorderLayout());
		
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

		// items for documents menu toolbar
		ImageIcon addDocumentIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-file-plus-16.png"));
		addDocumentAction = new AddDocumentAction("Add document", addDocumentIcon, "Open a dialog window to enter details of a new document", KeyEvent.VK_A);
		addDocumentItem = new JMenuItem(addDocumentAction);
		addDocumentAction.setEnabled(false);
		
		ImageIcon removeDocumentsIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-file-minus-16.png"));
		RemoveDocumentsAction removeDocumentsAction = new RemoveDocumentsAction("Remove document(s)", removeDocumentsIcon, "Remove the document(s) currently selected in the document table", KeyEvent.VK_R);
		removeDocumentsItem = new JMenuItem(removeDocumentsAction);
		removeDocumentsAction.setEnabled(false);
		
		ImageIcon editDocumentsIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-edit-16.png"));
		EditDocumentsAction editDocumentsAction = new EditDocumentsAction("Edit document(s)", editDocumentsIcon, "Edit the document(s) currently selected in the document table", KeyEvent.VK_E);
		editDocumentsItem = new JMenuItem(editDocumentsAction);
		editDocumentsAction.setEnabled(false);
		
		// toolbar of the document panel
		JToolBar tb = new JToolBar("Document toolbar");
		
		JButton addDocumentButton = new JButton(addDocumentAction);
		addDocumentButton.setText("Add");
		tb.add(addDocumentButton);

		JButton removeDocumentsButton = new JButton(removeDocumentsAction);
		removeDocumentsButton.setText("Remove");
		tb.add(removeDocumentsButton);

		JButton editDocumentsButton = new JButton(editDocumentsAction);
		editDocumentsButton.setText("Edit");
		tb.add(editDocumentsButton);
		
        tb.setRollover(true);
		this.add(tb, BorderLayout.NORTH);

	    // right-click menu for document table
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuItemAddDocument = new JMenuItem(addDocumentAction);
		popupMenu.add(menuItemAddDocument);
		JMenuItem menuItemDelete = new JMenuItem(removeDocumentsAction);
		popupMenu.add(menuItemDelete);
		JMenuItem menuItemResetTime = new JMenuItem("Set document time to 00:00:00");
		popupMenu.add(menuItemResetTime);
		JSeparator sep = new JSeparator();
		popupMenu.add(sep);
		ImageIcon checkedIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-checkbox-16.png"));
		ImageIcon uncheckedIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-square-16.png"));
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
		
		// disable document right-click menu items if no document was selected
		documentTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
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
				if (e.getSource() == menuItemResetTime) {
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
			}
			

		};
		
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
				if (rowCount > 0) {
					removeDocumentsAction.setEnabled(true);
					editDocumentsAction.setEnabled(true);
				} else {
					removeDocumentsAction.setEnabled(false);
					editDocumentsAction.setEnabled(false);
				}
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

	// add new document action
	class AddDocumentAction extends AbstractAction {
		public AddDocumentAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		public void actionPerformed(ActionEvent e) {
			DocumentEditor de = new DocumentEditor();
			if (de.getDocuments() != null) {
				Dna.sql.addDocuments(de.getDocuments());
				documentTableModel.reloadTableFromSQL();
			}
		}
	}

	// remove documents action
	class RemoveDocumentsAction extends AbstractAction {
		public RemoveDocumentsAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
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
	}

	// edit documents action
	class EditDocumentsAction extends AbstractAction {
		public EditDocumentsAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		public void actionPerformed(ActionEvent e) {
			int[] selectedRows = documentTable.getSelectedRows();
			for (int i = 0; i < selectedRows.length; i++) {
				selectedRows[i] = documentTableModel.getIdByModelRow(documentTable.convertRowIndexToModel(selectedRows[i]));
			}
			DocumentEditor de = new DocumentEditor(selectedRows); // TODO: implement document editing infrastructure
		}
	}

	public void enableActions(boolean enabled) {
		addDocumentAction.setEnabled(enabled);
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