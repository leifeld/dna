package guiCoder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.UIDefaults;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTextField;

import dna.Coder;
import dna.Dna;
import dna.TableDocument;
import dna.Dna.CoderListener;
import dna.Dna.SqlListener;
import logger.LogEvent;
import logger.Logger;

/**
 * Panel with a toolbar, a document table, and a text pane for displaying
 * documents and their metadata. Typically used at the center of the main window
 * GUI of the Discourse Network Analyzer.
 */
@SuppressWarnings("serial")
class DocumentPanel extends JPanel implements SqlListener, CoderListener {
	private DocumentTableModel documentTableModel;
	TextPanel textPanel;
	private JTable documentTable;
	private JTextField documentFilterField;
	private JButton documentFilterResetButton;
	private SpinnerNumberModel popupWidthModel, fontSizeModel;
	private JToggleButton popupDecorationButton;

	/**
	 * Create an instance of the document panel class, using a table model and
	 * some actions from the surrounding GUI, which are handed over via the
	 * constructor.
	 * 
	 * @param documentTableModel The document table model used in the GUI.
	 * @param addDocumentAction An {@link Action} for adding a document to the
	 *   database.
	 * @param editDocumentsAction An {@link Action} for editing the metadata of
	 *   one or more documents in the database.
	 * @param removeDocumentsAction An {@link Action} for deleting one or more
	 *   documents from the database.
	 * @param BatchImportDocumentsAction An {@link Action} for creating a
	 *   document batch importer window.
	 */
	public DocumentPanel(DocumentTableModel documentTableModel, Action addDocumentAction, Action editDocumentsAction, Action removeDocumentsAction, Action BatchImportDocumentsAction) {
		Dna.addCoderListener(this);
		Dna.addSqlListener(this);
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
		documentTableScroller.setPreferredSize(new Dimension(1200, 200));
		this.add(documentTableScroller, BorderLayout.CENTER);

		// row filter
		RowFilter<DocumentTableModel, Integer> documentFilter = new RowFilter<DocumentTableModel, Integer>() {
			public boolean include(Entry<? extends DocumentTableModel, ? extends Integer> entry) {
				TableDocument d = documentTableModel.getRow(entry.getIdentifier());
				try {
					Pattern pattern = Pattern.compile(documentFilterField.getText());
					Matcher matcherTitle = pattern.matcher(d.getTitle());
					Matcher matcherAuthor = pattern.matcher(d.getAuthor());
					Matcher matcherSource = pattern.matcher(d.getSource());
					Matcher matcherSection = pattern.matcher(d.getSection());
					Matcher matcherType = pattern.matcher(d.getType());
					Matcher matcherNotes = pattern.matcher(d.getTitle());
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy HH:mm");
					Matcher matcherDateTime = pattern.matcher(d.getDateTime().format(formatter));
					if (documentFilterField.getText().equals("")) {
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
		
		// toolbar of the document panel
		JPanel toolbarPanel = new JPanel(new BorderLayout());
		JToolBar tb1 = new JToolBar("Document toolbar");
		
		JButton addDocumentButton = new JButton(addDocumentAction);
		addDocumentButton.setText("Add");
		tb1.add(addDocumentButton);

		JButton removeDocumentsButton = new JButton(removeDocumentsAction);
		removeDocumentsButton.setText("Remove");
		tb1.add(removeDocumentsButton);

		JButton editDocumentsButton = new JButton(editDocumentsAction);
		editDocumentsButton.setText("Edit");
		tb1.add(editDocumentsButton);

		ImageIcon documentFilterResetIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-backspace.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		documentFilterResetButton = new JButton(documentFilterResetIcon);
		documentFilterResetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				documentFilterField.setText("");
			}
		});
		documentFilterResetButton.setEnabled(false);
		documentFilterResetButton.setToolTipText("Filter the documents using a regular expression.");
		documentFilterField = new JXTextField("Document regex filter");
		documentFilterField.setPreferredSize(new Dimension(200, 16));
		documentFilterField.setToolTipText("Filter the documents using a regular expression.");
        documentFilterField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				processFilterDocumentChanges();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				processFilterDocumentChanges();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				processFilterDocumentChanges();
			}
			
			private void processFilterDocumentChanges() {
				documentTableModel.fireTableDataChanged();
				if (documentFilterField.getText().equals("")) {
					documentFilterResetButton.setEnabled(false);
				} else {
					documentFilterResetButton.setEnabled(true);
				}
			}
		});
		documentFilterField.setEnabled(false);
		tb1.addSeparator(new Dimension(8, 8));
		tb1.add(documentFilterField);
		tb1.add(documentFilterResetButton);
		
        tb1.setRollover(true);
        toolbarPanel.add(tb1, BorderLayout.WEST);
        
        JToolBar tb2 = new JToolBar();

        ImageIcon fontSizeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-typography.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		JLabel fontSizeLabel = new JLabel(fontSizeIcon);
		fontSizeLabel.setToolTipText("Set the font size of the text area.");
        fontSizeModel = new SpinnerNumberModel(14, 1, 99, 1);
		JSpinner fontSizeSpinner = new JSpinner(fontSizeModel);
		((DefaultEditor) fontSizeSpinner.getEditor()).getTextField().setColumns(2);
		fontSizeSpinner.setToolTipText("Set the font size of the text area.");
		fontSizeLabel.setLabelFor(fontSizeSpinner);
		fontSizeSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (Dna.sql != null) {
					Dna.sql.setCoderFontSize(Dna.sql.getConnectionProfile().getCoderId(), (int) fontSizeSpinner.getValue());
					Dna.fireCoderChange();
				}
			}
		});
		tb2.add(fontSizeLabel);
		tb2.add(fontSizeSpinner);
		tb2.addSeparator(new Dimension(8, 8));
		
        ImageIcon popupWidthIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-chart-arrows.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		JLabel popupWidthLabel = new JLabel(popupWidthIcon);
		popupWidthLabel.setToolTipText("Set the width of the text fields for the variables in a statement popup window (in px).");
        popupWidthModel = new SpinnerNumberModel(300, 160, 9990, 10);
		JSpinner popupWidthSpinner = new JSpinner(popupWidthModel);
		((DefaultEditor) popupWidthSpinner.getEditor()).getTextField().setColumns(4);
		popupWidthSpinner.setToolTipText("Set the width of the text fields for the variables in a statement popup window (in px).");
		popupWidthLabel.setLabelFor(popupWidthSpinner);
		popupWidthSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (Dna.sql != null) {
					Dna.sql.setCoderPopupWidth(Dna.sql.getConnectionProfile().getCoderId(), (int) popupWidthSpinner.getValue());
					Dna.fireCoderChange();
				}
			}
		});
		tb2.add(popupWidthLabel);
		tb2.add(popupWidthSpinner);
		
		ImageIcon popupDecorationIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-border-outer.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		popupDecorationButton = new JToggleButton(popupDecorationIcon);
		popupDecorationButton.setToolTipText("If the button is pressed, statement popup windows will have buttons and a frame. If not, statements will auto-save.");
		popupDecorationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Dna.sql != null) {
					Dna.sql.setCoderPopupDecoration(Dna.sql.getConnectionProfile().getCoderId(), popupDecorationButton.isSelected());
					Dna.fireCoderChange();
				}
			}
		});
		popupDecorationButton.setEnabled(false);
		tb2.addSeparator(new Dimension(8, 8));
		tb2.add(popupDecorationButton);
		
        toolbarPanel.add(tb2, BorderLayout.EAST);
		this.add(toolbarPanel, BorderLayout.NORTH);

	    // right-click menu for document table
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuItemAddDocument = new JMenuItem(addDocumentAction);
		popupMenu.add(menuItemAddDocument);
		JMenuItem menuItemDelete = new JMenuItem(removeDocumentsAction);
		popupMenu.add(menuItemDelete);
		JMenuItem menuItemEdit = new JMenuItem(editDocumentsAction);
		popupMenu.add(menuItemEdit);
		JSeparator sep = new JSeparator();
		popupMenu.add(sep);
		ImageIcon checkedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-checkbox.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon uncheckedIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-square.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
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
					textPanel.setContents(-1, "");
				} else if (rowCount == 1) {
					int selectedRow = documentTable.getSelectedRow();
					int selectedModelIndex = documentTable.convertRowIndexToModel(selectedRow);
					int id = (int) documentTableModel.getValueAt(selectedModelIndex, 0);
					textPanel.setContents(id, documentTableModel.getDocumentText(id));
					//Dna.gui.textPanel.setEnabled(true);
				} else {
					LogEvent l = new LogEvent(Logger.WARNING,
							"[GUI] Negative number of rows in the document table!",
							"When a document is selected in the document table in the DNA coding window, the text of the document is displayed in the text panel. When checking which row in the table was selected, it was found that the table contained negative numbers of documents. This is obviously an error. Please report it by submitting a bug report along with the saved log.");
					Dna.logger.log(l);
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
		textPanel = new TextPanel((int) popupWidthSpinner.getValue());
		this.add(textPanel, BorderLayout.SOUTH);
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
	 * Convert a row index in the document table to a document table model index.
	 * 
	 * @param rowIndex The row index in the table to convert.
	 * @return The row index in the table model corresponding to the table row index.
	 */
	int convertRowIndexToModel(int rowIndex) {
		return documentTable.convertRowIndexToModel(rowIndex);
	}
	
	/*
	private void selectDocument(int documentId) {
		int index = documentTable.convertRowIndexToView(this.documentTableModel.getModelRowById(documentId));
		this.documentTable.setRowSelectionInterval(index, index);
	}
	*/
	
	/**
	 * A renderer for {@link dna.Coder} objects in {@link JTable} tables.
	 */
	private class CoderTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value == null) {
				return new JLabel("");
			} else {
				Coder coder = (Coder) value;
				CoderBadgePanel cbp = new CoderBadgePanel(coder, 14, 22);
				if (isSelected) {
					UIDefaults defaults = javax.swing.UIManager.getDefaults();
					Color bg = defaults.getColor("List.selectionBackground");
					cbp.setBackground(bg);
				}
				return cbp;
			}
		}
	}

	@Override
	public void adjustToChangedCoder() {
		if (Dna.sql == null) {
			popupWidthModel.setValue(300);
			fontSizeModel.setValue(14);
			popupDecorationButton.setSelected(false);
		} else {
			Coder coder = Dna.sql.getCoder(Dna.sql.getConnectionProfile().getCoderId());
			popupWidthModel.setValue(coder.getPopupWidth());
			fontSizeModel.setValue(coder.getFontSize());
			if (coder.getPopupDecoration() == 1) {
				popupDecorationButton.setSelected(true);
			} else {
				popupDecorationButton.setSelected(false);
			}
		}
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"[GUI] Document panel adjusted to updated coder settings (or closed database).",
				"[GUI] Document panel adjusted to updated coder settings (or closed database).");
		Dna.logger.log(l);
	}

	@Override
	public void adjustToDatabaseState() {
		if (Dna.sql == null) {
			documentFilterField.setText("");
			documentFilterField.setEnabled(false);
			documentFilterResetButton.setEnabled(false);
			popupDecorationButton.setEnabled(false);
		} else {
			documentFilterField.setEnabled(true);
			if (documentFilterField.getText().equals("")) {
				documentFilterResetButton.setEnabled(false);
			} else {
				documentFilterResetButton.setEnabled(true);
			}
			popupDecorationButton.setEnabled(true);
		}
	}
}