package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.RowFilter.Entry;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import dna.NewDatabaseDialog.CoderPanel.EditCoderWindow;
import dna.dataStructures.Coder;
import dna.dataStructures.CoderRelation;
import dna.dataStructures.Statement;

public class LeftPanel extends JPanel {
	CoderPanel coderPanel;
	public JComboBox<Coder> coderBox;
	
	public LeftPanel() {
		JXTaskPaneContainer tpc = new JXTaskPaneContainer();
		//this.setColumnHeaderView(tpc);
		tpc.setBackground(this.getBackground());
		this.add(tpc);
		
		
		// coder visibility panel
		JXTaskPane coderVisibilityTaskPane = new JXTaskPane();
		ImageIcon groupIcon = new ImageIcon(getClass().getResource("/icons/group.png"));
		coderVisibilityTaskPane.setName("Coder");
		coderVisibilityTaskPane.setTitle("Coder");
		coderVisibilityTaskPane.setIcon(groupIcon);
		((Container)tpc).add(coderVisibilityTaskPane);
		
		coderPanel = new CoderPanel();
		coderPanel.setPreferredSize(new Dimension(200, (int) coderPanel.getPreferredSize().getHeight()));
		coderVisibilityTaskPane.add(coderPanel);
		
		
	}
	
	@SuppressWarnings("serial")
	public class CoderPanel extends JPanel {
		CoderComboBoxModel model;
		CoderComboRenderer renderer;
		JButton editButton;
		CoderRelationTableModel coderTableModel;
		JTable coderRelationTable;
		TableRowSorter<CoderRelationTableModel> sorter;
		RowFilter<CoderRelationTableModel, Integer> filter;
		
		public CoderPanel() {
			this.setLayout(new BorderLayout());
			
			// coder edit button
			JPanel coderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			editButton = new JButton(new ImageIcon(getClass().getResource("/icons/pencil.png")));
			editButton.setPreferredSize(new Dimension(30, 30));
			editButton.setEnabled(false);
			coderPanel.add(editButton);
			
			// coder combo box
			coderPanel.add(Box.createRigidArea(new Dimension(5,5)));
			renderer = new CoderComboRenderer();
			model = new CoderComboBoxModel();
			coderBox = new JComboBox(model);
			coderBox.setRenderer(renderer);
			coderBox.setPreferredSize(new Dimension(150, 30));
			coderBox.setEnabled(false);
			coderPanel.add(coderBox);
			this.add(coderPanel, BorderLayout.NORTH);
			
			// define coder relation table and its appearance
			coderTableModel = new CoderRelationTableModel();
			coderRelationTable = new JTable(coderTableModel);
			CoderRelationCellRenderer cellRenderer = new CoderRelationCellRenderer();
			coderRelationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane coderRelationScrollPane = new JScrollPane(coderRelationTable);
			coderRelationScrollPane.setPreferredSize(new Dimension(200, 240));
			coderRelationTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 200 );
			coderRelationTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 25 );
			coderRelationTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 25 );
			coderRelationTable.getColumnModel().getColumn( 3 ).setPreferredWidth( 25 );
			coderRelationTable.getColumnModel().getColumn( 4 ).setPreferredWidth( 25 );
			coderRelationTable.setRowHeight(30);
			
			// table header icons
			TableCellRenderer headerRenderer = new JComponentTableCellRenderer();
			Border headerBorder = UIManager.getBorder("TableHeader.cellBorder");
			JLabel viewStatementLabel = new JLabel(new ImageIcon(getClass().getResource("/icons/comment.png")), JLabel.CENTER);
			JLabel editStatementLabel = new JLabel(new ImageIcon(getClass().getResource("/icons/comment_edit.png")), JLabel.CENTER);
			JLabel viewDocumentLabel = new JLabel(new ImageIcon(getClass().getResource("/icons/page_white.png")), JLabel.CENTER);
			JLabel editDocumentLabel = new JLabel(new ImageIcon(getClass().getResource("/icons/page_white_edit.png")), JLabel.CENTER);
			viewStatementLabel.setToolTipText("Can the current coder view statements of this coder?");
			editStatementLabel.setToolTipText("Can the current coder edit statements of this coder?");
			viewDocumentLabel.setToolTipText("Can the current coder view documents of this coder?");
			editDocumentLabel.setToolTipText("Can the current coder edit documents of this coder?");
			viewStatementLabel.setBorder(headerBorder);
			editStatementLabel.setBorder(headerBorder);
			viewDocumentLabel.setBorder(headerBorder);
			editDocumentLabel.setBorder(headerBorder);
			TableColumn column1 = coderRelationTable.getColumnModel().getColumn(1);
			TableColumn column2 = coderRelationTable.getColumnModel().getColumn(2);
			TableColumn column3 = coderRelationTable.getColumnModel().getColumn(3);
			TableColumn column4 = coderRelationTable.getColumnModel().getColumn(4);
			column1.setHeaderRenderer(headerRenderer);
			column2.setHeaderRenderer(headerRenderer);
			column3.setHeaderRenderer(headerRenderer);
			column4.setHeaderRenderer(headerRenderer);
			column1.setHeaderValue(viewStatementLabel);
			column2.setHeaderValue(editStatementLabel);
			column3.setHeaderValue(viewDocumentLabel);
			column4.setHeaderValue(editDocumentLabel);
			coderRelationTable.getTableHeader().setReorderingAllowed( false );
			
			// make table unsortable and filter out loops
			filter = new RowFilter<CoderRelationTableModel, Integer>() {
				public boolean include(Entry<? extends CoderRelationTableModel, ? extends Integer> entry) {
					CoderRelationTableModel crtm = entry.getModel();
					CoderRelation cr = crtm.get(entry.getIdentifier());
					if (cr.getOtherCoder() == ((Coder) model.getSelectedItem()).getId()) {
						return false;
					}
					return true;
				}
			};
			sorter = new TableRowSorter<CoderRelationTableModel>(coderTableModel) {
				public void toggleSortOrder(int i) {
					//leave blank; overwritten method makes the table unsortable
				}
			};
			coderRelationTable.setRowSorter(sorter);
			
			// apply cell renderer to display CoderRelation entries properly
			coderRelationTable.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
			coderRelationTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
			coderRelationTable.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);
			coderRelationTable.getColumnModel().getColumn(3).setCellRenderer(cellRenderer);
			coderRelationTable.getColumnModel().getColumn(4).setCellRenderer(cellRenderer);
			
			this.add(coderRelationScrollPane, BorderLayout.CENTER);
			
			coderBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					coderRelationTable.updateUI();
					sorter.setRowFilter(filter);
				}
			});

			editButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Coder coder = (Coder) model.getElementAt(coderBox.getSelectedIndex());
					new EditCoderWindow(coder);
				}
			});
		}

		public void setComboEnabled(boolean enabled) {
			coderBox.setEnabled(enabled);
			editButton.setEnabled(enabled);
		}
	}

	public void setComboEnabled(boolean enabled) {
		coderPanel.setComboEnabled(enabled);
	}


	/**
	 * @author Philip Leifeld
	 *
	 * Class for rendering table headers
	 */
	class JComponentTableCellRenderer implements TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			return (JComponent) value;
		}
	}

	/**
	 * @author Philip Leifeld
	 *
	 * Editor dialog window for changing coder permissions, name, or color.
	 */
	class EditCoderWindow extends JDialog{
		private static final long serialVersionUID = -3405773030010173292L;
		Coder coder;
		JTextField nameField;
		JButton addColorButton;
		JCheckBox permAddDocuments, permEditDocuments, permDeleteDocuments, permImportDocuments;
		JCheckBox permViewOtherDocuments, permEditOtherDocuments;
		JCheckBox permAddStatements, permViewOtherStatements, permEditOtherStatements;
		JCheckBox permEditCoders, permEditStatementTypes, permEditRegex;
		
		public EditCoderWindow(Coder coder) {
			this.coder = coder;
			
			this.setTitle("Coder details");
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			ImageIcon icon = new ImageIcon(getClass().getResource("/icons/user_edit.png"));
			this.setIconImage(icon.getImage());
			this.setLayout(new BorderLayout());
			
			JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			addColorButton = new JButton();
			addColorButton.setBackground(coder.getColor());
			addColorButton.setPreferredSize(new Dimension(18, 18));
			addColorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color actualColor = ((JButton)e.getSource()).getBackground();
					Color newColor = JColorChooser.showDialog(EditCoderWindow.this, "choose color...", actualColor);
					if (newColor != null) {
						((JButton) e.getSource()).setBackground(newColor);
					}
				}
			});
			namePanel.add(new JLabel("Color: "));
			namePanel.add(addColorButton);
			namePanel.add(Box.createRigidArea(new Dimension(5,5)));
			JLabel nameLabel = new JLabel("Name: ");
			namePanel.add(nameLabel);
			namePanel.add(Box.createRigidArea(new Dimension(5,5)));
			nameField = new JTextField(coder.getName());
			nameField.setColumns(40);
			namePanel.add(nameField);
			this.add(namePanel, BorderLayout.NORTH);
			
			JPanel permPanel = new JPanel(new GridLayout(4, 3));
			permAddDocuments = new JCheckBox("add documents");
			permEditDocuments = new JCheckBox("edit documents");
			permDeleteDocuments = new JCheckBox("delete documents");
			permImportDocuments = new JCheckBox("import documents");
			permViewOtherDocuments = new JCheckBox("view others' documents");
			permEditOtherDocuments = new JCheckBox("edit others' documents");
			permAddStatements = new JCheckBox("add statements");
			permViewOtherStatements = new JCheckBox("view others' statements");
			permEditOtherStatements = new JCheckBox("edit others' statements");
			permEditCoders = new JCheckBox("edit coder settings");
			permEditStatementTypes = new JCheckBox("edit statement types");
			permEditRegex = new JCheckBox("edit regex settings");
			permPanel.add(permAddDocuments);
			permPanel.add(permEditDocuments);
			permPanel.add(permDeleteDocuments);
			permPanel.add(permImportDocuments);
			permPanel.add(permViewOtherDocuments);
			permPanel.add(permEditOtherDocuments);
			permPanel.add(permAddStatements);
			permPanel.add(permViewOtherStatements);
			permPanel.add(permEditOtherStatements);
			permPanel.add(permEditCoders);
			permPanel.add(permEditStatementTypes);
			permPanel.add(permEditRegex);
			permAddDocuments.setSelected(coder.getPermissions().get("addDocuments"));
			permEditDocuments.setSelected(coder.getPermissions().get("editDocuments"));
			permDeleteDocuments.setSelected(coder.getPermissions().get("deleteDocuments"));
			permImportDocuments.setSelected(coder.getPermissions().get("importDocuments"));
			permViewOtherDocuments.setSelected(coder.getPermissions().get("viewOthersDocuments"));
			permEditOtherDocuments.setSelected(coder.getPermissions().get("editOthersDocuments"));
			permAddStatements.setSelected(coder.getPermissions().get("addStatements"));
			permViewOtherStatements.setSelected(coder.getPermissions().get("viewOthersStatements"));
			permEditOtherStatements.setSelected(coder.getPermissions().get("editOthersStatements"));
			permEditCoders.setSelected(coder.getPermissions().get("editCoders"));
			permEditStatementTypes.setSelected(coder.getPermissions().get("editStatementTypes"));
			permEditRegex.setSelected(coder.getPermissions().get("editRegex"));
			this.add(permPanel, BorderLayout.CENTER);
			
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JButton okButton = new JButton("OK", new ImageIcon(getClass().getResource("/icons/accept.png")));
			if (nameField.getText().equals("")) {
				okButton.setEnabled(false);
			}
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					coder.setName(nameField.getText());
					coder.setColor(addColorButton.getBackground());
					coder.getPermissions().put("addDocuments", permAddDocuments.isSelected());
					coder.getPermissions().put("editDocuments", permEditDocuments.isSelected());
					coder.getPermissions().put("deleteDocuments", permDeleteDocuments.isSelected());
					coder.getPermissions().put("importDocuments", permImportDocuments.isSelected());
					coder.getPermissions().put("viewOthersDocuments", permViewOtherDocuments.isSelected());
					coder.getPermissions().put("editOthersDocuments", permEditOtherDocuments.isSelected());
					coder.getPermissions().put("addStatements", permAddStatements.isSelected());
					coder.getPermissions().put("viewOthersStatements", permViewOtherStatements.isSelected());
					coder.getPermissions().put("editOthersStatements", permEditOtherStatements.isSelected());
					coder.getPermissions().put("editCoders", permEditCoders.isSelected());
					coder.getPermissions().put("editStatementTypes", permEditStatementTypes.isSelected());
					coder.getPermissions().put("editRegex", permEditRegex.isSelected());
					Dna.data.replaceCoder(coder);
					for (int i = 0; i < Dna.data.getCoderRelations().size(); i++) {
						if (Dna.data.getCoderRelations().get(i).getCoder() == coder.getId()) {
							Dna.data.getCoderRelations().get(i).setViewStatements(permViewOtherStatements.isSelected());
							Dna.data.getCoderRelations().get(i).setEditStatements(permEditOtherStatements.isSelected());
							Dna.data.getCoderRelations().get(i).setViewDocuments(permViewOtherDocuments.isSelected());
							Dna.data.getCoderRelations().get(i).setEditDocuments(permEditOtherDocuments.isSelected());
						}
					}
					Dna.dna.sql.upsertCoder(coder);
					LeftPanel.this.coderBox.updateUI();
					LeftPanel.this.coderPanel.coderRelationTable.updateUI();
					dispose();
				}
			});
			JButton cancelButton = new JButton("Cancel", new ImageIcon(getClass().getResource("/icons/cancel.png")));
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelAction();
				}
			});
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
			
			nameField.getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent e) {
					check();
				}
				public void removeUpdate(DocumentEvent e) {
					check();
				}
				public void changedUpdate(DocumentEvent e) {
					check();
				}
				public void check() {
					if (nameField.getText().equals("")) {
						okButton.setEnabled(false);
					} else {
						okButton.setEnabled(true);
					}
				}
			});
			
			this.add(buttonPanel, BorderLayout.SOUTH);

			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					cancelAction();
				}
			});

			this.pack();
			this.setModal(true);
			this.setLocationRelativeTo(null);
			this.setVisible(true);
			this.setResizable(false);
			namePanel.requestFocus();
		}
		
		public void cancelAction() {
			dispose();
		}
		
		public void setCoder(Coder coder) {
			this.setCoder(coder);
		}
		
		public Coder getCoder() {
			return(this.coder);
		}
	}

	public class CoderComboRenderer implements ListCellRenderer<Object> {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (value == null) {
				return new JLabel("");
			} else {
				Coder coder = (Coder) value;
				JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				if (isSelected) {
					UIDefaults defaults = javax.swing.UIManager.getDefaults();
					Color bg = defaults.getColor("List.selectionBackground");
					panel.setBackground(bg);
				}
				JButton colorButton = new JButton();
				colorButton.setPreferredSize(new Dimension(12, 16));
				colorButton.setBackground(coder.getColor());
				panel.add(colorButton);
				
				String name = coder.getName();
				int nameLength = name.length();
				if (nameLength > 13) {
					nameLength = 10;
					name = name.substring(0,  nameLength);
					name = name + "...";
				}
				panel.add(new JLabel(name));
				return panel;
			}
		}
		
	}
	
	public class CoderComboBoxModel implements ComboBoxModel<Object> {
		private Object selectedItem;
		Vector<ListDataListener> listeners = new Vector<ListDataListener>();
		
		@Override
		public int getSize() {
			return Dna.data.getCoders().size();
		}
		
		@Override
		public Object getElementAt(int index) {
			return Dna.data.getCoders().get(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
			
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

		@Override
		public void setSelectedItem(Object anItem) {
			Dna.data.setActiveCoder(((Coder) anItem).getId());
			selectedItem = anItem;
		}

		@Override
		public Object getSelectedItem() {
			return selectedItem;
		}
	}

	public class CoderRelationCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			//Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			//if (isSelected) {
			//	UIDefaults defaults = javax.swing.UIManager.getDefaults();
			//	Color bg = defaults.getColor("List.selectionBackground");
			//	panel.setBackground(bg);
			//}
			int modelRow = table.convertRowIndexToModel(row);
			CoderRelation cr = ((CoderRelationTableModel)table.getModel()).get(modelRow);
			int otherCoderId = cr.getOtherCoder();
			Coder otherCoder = Dna.data.getCoderById(otherCoderId);
			Color otherColor = otherCoder.getColor();
			if (column == 0) {
				String name = otherCoder.getName();
				JLabel otherName = new JLabel(name);
				panel.add(otherName);
				panel.setBackground(otherColor);
			} else if (column == 1) {
				boolean viewStatements = cr.isViewStatements();
				JCheckBox box = new JCheckBox();
				box.setSelected(viewStatements);
				JPanel panelBox = new JPanel(new FlowLayout(FlowLayout.CENTER));
				if (((Coder) coderBox.getSelectedItem()).getPermissions().get("viewOthersStatements") == false) {
					box.setEnabled(false);
				}
				panelBox.add(box);
				return panelBox;
			} else if (column == 2) {
				boolean editStatements = cr.isEditStatements();
				JCheckBox box = new JCheckBox();
				box.setSelected(editStatements);
				JPanel panelBox = new JPanel(new FlowLayout(FlowLayout.CENTER));
				if (((Coder) coderBox.getSelectedItem()).getPermissions().get("editOthersStatements") == false) {
					box.setEnabled(false);
				}
				panelBox.add(box);
				return panelBox;
			} else if (column == 3) {
				boolean viewDocuments = cr.isViewDocuments();
				JCheckBox box = new JCheckBox();
				box.setSelected(viewDocuments);
				JPanel panelBox = new JPanel(new FlowLayout(FlowLayout.CENTER));
				if (((Coder) coderBox.getSelectedItem()).getPermissions().get("viewOthersDocuments") == false) {
					box.setEnabled(false);
				}
				panelBox.add(box);
				return panelBox;
			} else if (column == 4) {
				boolean editDocuments = cr.isEditDocuments();
				JCheckBox box = new JCheckBox();
				box.setSelected(editDocuments);
				JPanel panelBox = new JPanel(new FlowLayout(FlowLayout.CENTER));
				if (((Coder) coderBox.getSelectedItem()).getPermissions().get("editOthersDocuments") == false) {
					box.setEnabled(false);
				}
				panelBox.add(box);
				return panelBox;
			}
			return panel;
		}
	}
	
	public class CoderRelationTableModel implements TableModel {

		Vector<TableModelListener> listeners = new Vector<TableModelListener>();
		
		public CoderRelation get(int modelRow) {
			return Dna.data.getCoderRelations().get(modelRow);
		}
		
		@Override
		public int getRowCount() {
			return Dna.data.getCoderRelations().size();
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public String getColumnName(int column) {
			switch( column ){
			case 0: return "Name";
			case 1: return "view statements";
			case 2: return "edit statements";
			case 3: return "view documents";
			case 4: return "edit documents";
			default: return null;
			}
		}

		public Class<?> getColumnClass(int columnIndex) {
			switch( columnIndex ){
			case 0: return String.class;
			case 1: return Boolean.class;
			case 2: return Boolean.class;
			case 3: return Boolean.class;
			case 4: return Boolean.class;
			default: return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return false;
			} else if (columnIndex == 1 && ((Coder) coderBox.getSelectedItem()).getPermissions().get("viewOthersStatements") == false) {
				return false;
			} else if (columnIndex == 2 && ((Coder) coderBox.getSelectedItem()).getPermissions().get("editOthersStatements") == false) {
				return false;
			} else if (columnIndex == 3 && ((Coder) coderBox.getSelectedItem()).getPermissions().get("viewOthersDocuments") == false) {
				return false;
			} else if (columnIndex == 4 && ((Coder) coderBox.getSelectedItem()).getPermissions().get("editOthersDocuments") == false) {
				return false;
			} else {
				return true;
			}
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			CoderRelation cr = Dna.data.getCoderRelations().get(rowIndex);
			if (columnIndex == 0) {
				return Dna.data.getCoderById(cr.getOtherCoder()).getName();
			} else if (columnIndex == 1) {
				return Dna.data.getCoderRelations().get(rowIndex).isViewStatements();
			} else if (columnIndex == 2) {
				return Dna.data.getCoderRelations().get(rowIndex).isEditStatements();
			} else if (columnIndex == 3) {
				return Dna.data.getCoderRelations().get(rowIndex).isViewDocuments();
			} else if (columnIndex == 4) {
				return Dna.data.getCoderRelations().get(rowIndex).isEditDocuments();
			} else {
				return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			CoderRelation cr = Dna.data.getCoderRelations().get(rowIndex);
			if (columnIndex == 0) {
				Dna.data.getCoderById(cr.getOtherCoder()).setName((String) aValue);
			} else if (columnIndex == 1) {
				Dna.data.getCoderRelations().get(rowIndex).setViewStatements((boolean) aValue);
				Dna.dna.sql.updateCoderRelationViewStatements(cr.getId(), (boolean) aValue);
			} else if (columnIndex == 2) {
				Dna.data.getCoderRelations().get(rowIndex).setEditStatements((boolean) aValue);
				Dna.dna.sql.updateCoderRelationEditStatements(cr.getId(), (boolean) aValue);
			} else if (columnIndex == 3) {
				Dna.data.getCoderRelations().get(rowIndex).setViewDocuments((boolean) aValue);
				Dna.dna.sql.updateCoderRelationViewDocuments(cr.getId(), (boolean) aValue);
			} else if (columnIndex == 4) {
				Dna.data.getCoderRelations().get(rowIndex).setEditDocuments((boolean) aValue);
				Dna.dna.sql.updateCoderRelationEditDocuments(cr.getId(), (boolean) aValue);
			}
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			listeners.add( l );
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			listeners.remove( l );
		}
	}
}
