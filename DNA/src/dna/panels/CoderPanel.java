package dna.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.RowFilter.Entry;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import dna.LeftPanel;
import dna.dataStructures.Coder;
import dna.dataStructures.CoderRelation;
import dna.renderer.CoderComboBoxModel;
import dna.renderer.CoderComboBoxRenderer;
import dna.renderer.CoderRelationCellRenderer;
import dna.renderer.CoderRelationTableModel;

public class CoderPanel extends JPanel {
	public JComboBox<Coder> coderBox;
	CoderComboBoxModel model;
	CoderComboBoxRenderer renderer;
	JButton editButton, deleteButton, addButton;
	CoderRelationTableModel coderTableModel;
	public JTable coderRelationTable;
	TableRowSorter<CoderRelationTableModel> sorter;
	RowFilter<CoderRelationTableModel, Integer> filter;
	
	public CoderPanel() {
		this.setLayout(new BorderLayout());

		// coder combo box
		renderer = new CoderComboBoxRenderer();
		model = new CoderComboBoxModel();
		coderBox = new JComboBox(model);
		coderBox.setRenderer(renderer);
		coderBox.setPreferredSize(new Dimension(150, 30));
		coderBox.setEnabled(false);
		this.add(coderBox, BorderLayout.NORTH);
		
		// buttons
		JPanel buttonPanel = new JPanel();
		GridLayout gl = new GridLayout(1, 3);
		gl.setHgap(5);
		buttonPanel.setLayout(gl);
		editButton = new JButton(new ImageIcon(getClass().getResource("/icons/pencil.png")));
		editButton.setEnabled(false);
		buttonPanel.add(editButton);
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Coder coder = (Coder) model.getElementAt(coderBox.getSelectedIndex());
				new EditCoderWindow(coder);
			}
		});
		addButton = new JButton(new ImageIcon(getClass().getResource("/icons/add.png")));
		buttonPanel.add(addButton);
		addButton.setEnabled(false);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO: add coder dialog
			}
		});
		deleteButton = new JButton(new ImageIcon(getClass().getResource("/icons/delete.png")));
		buttonPanel.add(deleteButton);
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Dna.data.getCoders().size() == 1) {
					JOptionPane.showMessageDialog(Dna.dna.gui, "The selected coder cannot be deleted because there are no other coders left.");
				} else {
					int countDoc = 0;
					for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
						if (Dna.data.getDocuments().get(i).getCoder() == Dna.data.getActiveCoder()) {
							countDoc++;
						}
					}
					int countSt = 0;
					for (int i = 0; i < Dna.data.getStatements().size(); i++) {
						if (Dna.data.getStatements().get(i).getCoder() == Dna.data.getActiveCoder()) {
							countSt++;
						}
					}
					String message = "Are you sure you want to delete the selected coder?";
					if (countSt > 0 || countDoc > 0) {
						message = "The selected coder owns " + countDoc + " documents and " + countSt 
								+ " statements. All of them will be \n deleted, including other coders' statements "
								+ "in these documents. Are you \n sure you want to do this? The changes cannot be reverted.";
					}
					
					int dialog = JOptionPane.showConfirmDialog(Dna.dna.gui, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						int id = Dna.data.getActiveCoder();
						Dna.dna.sql.removeCoder(id);
						Dna.data.removeCoder(id);
						coderBox.setSelectedIndex(0);
						coderBox.updateUI();
					}
				}
			}
		});
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		this.add(buttonPanel, BorderLayout.CENTER);
		
		// define coder relation table and its appearance
		coderTableModel = new CoderRelationTableModel();
		coderRelationTable = new JTable(coderTableModel);
		CoderRelationCellRenderer cellRenderer = new CoderRelationCellRenderer();
		coderRelationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane coderRelationScrollPane = new JScrollPane(coderRelationTable);
		coderRelationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
				int coderId = ((Coder) model.getSelectedItem()).getId();
				if (cr.getOtherCoder() == coderId || cr.getCoder() != coderId) {
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
		
		this.add(coderRelationScrollPane, BorderLayout.SOUTH);
		
		coderBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				coderRelationTable.updateUI();
				sorter.setRowFilter(filter);
			}
		});
	}

	public void setComboEnabled(boolean enabled) {
		coderBox.setEnabled(enabled);
		editButton.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
	}
	

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
				coderBox.updateUI();
				coderRelationTable.updateUI();
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
