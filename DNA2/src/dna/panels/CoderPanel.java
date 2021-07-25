package dna.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import dna.Dna;
import dna.EditCoderWindow;
import dna.dataStructures.Coder;
import dna.dataStructures.CoderRelation;
import dna.renderer.CoderComboBoxModel;
import dna.renderer.CoderComboBoxRenderer;
import dna.renderer.CoderRelationCellRenderer;
import dna.renderer.CoderRelationTableModel;

@SuppressWarnings("serial")
public class CoderPanel extends JPanel {
	public JComboBox<Coder> coderBox;
	public CoderComboBoxModel model;
	CoderComboBoxRenderer renderer;
	public JButton editButton;
	public JButton deleteButton;
	public JButton addButton;
	public CoderRelationTableModel coderTableModel;
	public JTable coderRelationTable;
	public TableRowSorter<CoderRelationTableModel> sorter;
	public RowFilter<CoderRelationTableModel, Integer> filter;
	
	public CoderPanel() {
		this.setLayout(new BorderLayout());

		// coder combo box
		renderer = new CoderComboBoxRenderer();
		model = new CoderComboBoxModel();
		coderBox = new JComboBox<Coder>(model);
		coderBox.setRenderer(renderer);
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
				Coder coder = (Coder) coderBox.getSelectedItem();
				EditCoderWindow ecw = new EditCoderWindow(coder);
				Coder coderUpdated = ecw.getCoder();
				ecw.dispose();
				Dna.dna.replaceCoder(coderUpdated);
				Dna.gui.refreshGui();
			}
		});
		addButton = new JButton(new ImageIcon(getClass().getResource("/icons/add.png")));
		buttonPanel.add(addButton);
		addButton.setEnabled(false);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditCoderWindow ecw = new EditCoderWindow(new Coder(Dna.data.generateNewId("coders")));
				Coder coder = ecw.getCoder();
				ecw.dispose();
				if (!coder.getName().equals("")) {
					Dna.dna.addCoder(coder);
				}
				coderBox.updateUI();
				coderRelationTable.updateUI();
				sorter.setRowFilter(filter);
			}
		});
		deleteButton = new JButton(new ImageIcon(getClass().getResource("/icons/delete.png")));
		buttonPanel.add(deleteButton);
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Dna.data.getCoders().size() == 1) {
					JOptionPane.showMessageDialog(Dna.gui, "The selected coder cannot be deleted because there are no other coders left.");
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
					
					int dialog = JOptionPane.showConfirmDialog(Dna.gui, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						int id = Dna.data.getActiveCoder();
						Dna.dna.sql.removeCoder(id);
						Dna.data.removeCoder(id);
						coderBox.updateUI();
						coderBox.setSelectedIndex(0);
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
		coderRelationScrollPane.setPreferredSize(new Dimension(200, 120));
		coderRelationTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 200 );
		coderRelationTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 25 );
		coderRelationTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 25 );
		coderRelationTable.getColumnModel().getColumn( 3 ).setPreferredWidth( 25 );
		coderRelationTable.getColumnModel().getColumn( 4 ).setPreferredWidth( 25 );
		
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
		
		setRowSorterEnabled(true);
		
		// apply cell renderer to display CoderRelation entries properly
		coderRelationTable.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
		coderRelationTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
		coderRelationTable.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);
		coderRelationTable.getColumnModel().getColumn(3).setCellRenderer(cellRenderer);
		coderRelationTable.getColumnModel().getColumn(4).setCellRenderer(cellRenderer);
		
		this.add(coderRelationScrollPane, BorderLayout.SOUTH);
		
		coderBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Dna.data.setActiveCoder(((Coder) coderBox.getSelectedItem()).getId());
				Dna.dna.sql.upsertSetting("activeCoder", Integer.toString(((Coder) coderBox.getSelectedItem()).getId()));
				coderRelationTable.updateUI();
				Dna.gui.refreshGui();
			}
		});
	}
	
	public void setComboEnabled(boolean enabled) {
		coderBox.setEnabled(enabled);
		editButton.setEnabled(enabled);
		addButton.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
	}
	
	public void clear() {
		coderTableModel.clear();
		model.clear();
		setComboEnabled(false);
	}

	public void setRowSorterEnabled(boolean enabled) {
		if (enabled == true) {
			sorter = new TableRowSorter<CoderRelationTableModel>(coderTableModel) {
				public void toggleSortOrder(int i) {
					//leave blank; overwritten method makes the table unsortable
				}
			};
			sorter.setRowFilter(filter);
			coderRelationTable.setRowSorter(sorter);
		} else {
			coderRelationTable.setRowSorter(null);
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
