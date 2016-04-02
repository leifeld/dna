package dna.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTextField;

import dna.dataStructures.*;
import dna.Dna;
import dna.renderer.StatementTableModel;

@SuppressWarnings("serial")
public class StatementPanel extends JPanel {
	public StatementTableModel ssc;
	public JTable statementTable;
	JScrollPane statementTableScrollPane;
	public StatementFilter statementFilter;
	TableRowSorter<StatementTableModel> sorter;
	JXTextField patternField1, patternField2;
	JComboBox<String> typeComboBox1, typeComboBox2, variableComboBox1, 
	variableComboBox2;

	public StatementPanel() {
		this.setLayout(new BorderLayout());
		ssc = new StatementTableModel();
		statementTable = new JTable( ssc );
		statementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statementTableScrollPane = new JScrollPane(statementTable);
		statementTableScrollPane.setPreferredSize(new Dimension(200, 240));
		statementTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 38 );
		statementTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 162 );

		statementTable.getTableHeader().setReorderingAllowed( false );
		statementTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		setRowSorterEnabled(true);

		StatementCellRenderer statementCellRenderer = new StatementCellRenderer();
		statementTable.getColumnModel().getColumn(0).setCellRenderer(
				statementCellRenderer);

		statementFilter = new StatementFilter();
		this.add(statementTableScrollPane, BorderLayout.CENTER);
		this.add(statementFilter, BorderLayout.SOUTH);

		statementTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = -1;
				row = statementTable.rowAtPoint(e.getPoint());
				row = statementTable.convertRowIndexToModel(row);

				if (row > -1) {
					int statementId = ssc.get(row).getId();
					if (statementId != -1) {
						boolean[] b = Dna.data.getActiveStatementPermissions(statementId);
						int docId = Dna.data.getStatement(statementId).getDocumentId();
						int docRow = Dna.dna.gui.documentPanel.documentContainer.getRowIndexById(docId);
						Dna.dna.gui.documentPanel.documentTable.getSelectionModel().
						setSelectionInterval(docRow, docRow);
						Dna.dna.gui.documentPanel.documentTable.scrollRectToVisible(new Rectangle(
								Dna.dna.gui.documentPanel.documentTable.getCellRect(docRow, 0, true)));
						Dna.dna.gui.textPanel.selectStatement(statementId, docId, b[1]);
					}
				}
			}
		});
	}
	
	public void updateStatementTypes() {
		typeComboBox1.removeAllItems();
		ArrayList<StatementType> types = new ArrayList<StatementType>();
		if (Dna.data.getSettings().get("filename") != null && !Dna.data.getSettings().get("filename").equals("")) {
			types = Dna.data.getStatementTypes();
			for (int i = 0; i < types.size(); i++) {				
				String type = types.get(i).getLabel().toString();
				if(type!=null )
				{
					typeComboBox1.addItem(type.trim());
				}

			}
			try{
				typeComboBox1.setSelectedIndex(0);
			}
			catch (IllegalArgumentException ex){
				typeComboBox1.setSelectedIndex(-1);
			}
		}
	}

	public void updateVariables() {
		variableComboBox1.removeAllItems();
		variableComboBox2.removeAllItems();
		String type = (String) typeComboBox1.getSelectedItem();
		if (type != null && !type.equals("")) {
			HashMap<String, String> variables = Dna.data.getStatementType(type).getVariables();
			Iterator<String> keyIterator = variables.keySet().iterator();
			while (keyIterator.hasNext()){
				String key = keyIterator.next();
				variableComboBox1.addItem(key);
				variableComboBox2.addItem(key);
			}
			try{
				variableComboBox1.setSelectedIndex(0);
			}
			catch (IllegalArgumentException ex){
			}
			try{
				variableComboBox2.setSelectedIndex(0);
			}
			catch (IllegalArgumentException ex){
			}
		}
	}
	
	public void setRowSorterEnabled(boolean enabled) {
		if (enabled == true) {
			sorter = new TableRowSorter<StatementTableModel>(ssc) {
				public void toggleSortOrder(int i) {
					//leave blank; overwritten method makes the table unsortable
				}
			};
			statementTable.setRowSorter(sorter);
		} else {
			statementFilter.showAll.setSelected(true);
			statementTable.setRowSorter(null);
		}
	}
	
	public class StatementFilter extends JPanel {
		public JRadioButton showAll, showCurrent, showFilter;
		
		public StatementFilter() {
			this.setLayout(new BorderLayout());
			JPanel showPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			ButtonGroup showGroup = new ButtonGroup();
			showAll = new JRadioButton("all");
			showAll.setSelected(true);
			showCurrent = new JRadioButton("current");
			showFilter = new JRadioButton("filter:");
			showFilter.setEnabled(false);
			showGroup.add(showAll);
			showGroup.add(showCurrent);
			showGroup.add(showFilter);
			showPanel.add(showAll);
			showPanel.add(showCurrent);
			showPanel.add(showFilter);

			typeComboBox1 = new JComboBox<String>();
			typeComboBox1.setPreferredSize(new Dimension(208, 20));
			typeComboBox1.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					updateVariables();
					filter();
				}
			});

			variableComboBox1 = new JComboBox<String>();
			variableComboBox1.setPreferredSize(new Dimension(100, 20));

			variableComboBox1.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					filter();
				}
			});
			patternField1 = new JXTextField("regex");
			patternField1.setPreferredSize(new Dimension(104, 20));

			variableComboBox2 = new JComboBox<String>();
			variableComboBox2.setPreferredSize(new Dimension(100, 20));
			variableComboBox2.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					filter();
				}
			});
			patternField2 = new JXTextField("regex");
			patternField2.setPreferredSize(new Dimension(104, 20));
			
			toggleEnabled(false);

			JPanel filterPanel0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel0.add(typeComboBox1);
			JPanel filterPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel1.add(variableComboBox1);
			filterPanel1.add(patternField1);
			JPanel filterPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel2.add(variableComboBox2);
			filterPanel2.add(patternField2);

			JPanel filterPanel = new JPanel();
			filterPanel.setLayout(new BoxLayout(filterPanel,BoxLayout.Y_AXIS));
			filterPanel.add(filterPanel0, Component.CENTER_ALIGNMENT);
			filterPanel.add(filterPanel1, Component.CENTER_ALIGNMENT);
			filterPanel.add(filterPanel2, Component.CENTER_ALIGNMENT);
			
			this.add(showPanel, BorderLayout.NORTH);
			this.add(filterPanel, BorderLayout.CENTER);
			
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == showAll) {				
						allFilter();
						toggleEnabled(false);

					} else if (e.getSource() == showCurrent) {
						toggleEnabled(false);
						allFilter();
						currentDocumentFilter();
					} else if (e.getSource() == showFilter) {
						toggleEnabled(true);
						filter();

					}
					//if (!(e.getSource() == showAll)) {
					//	Dna.mainProgram.contradictionReporter.clearTree();
					//}
				}
			};
			
			showAll.addActionListener(al);
			showCurrent.addActionListener(al);
			showFilter.addActionListener(al);

			DocumentListener dl = new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					applyFilter();
				}
				public void insertUpdate(DocumentEvent e) {
					applyFilter();
				}
				public void removeUpdate(DocumentEvent e) {
					applyFilter();
				}
				public void applyFilter() {
					filter();
				}
			};

			patternField1.getDocument().addDocumentListener(dl);
			patternField2.getDocument().addDocumentListener(dl);
		}

		public void toggleEnabled(boolean enabled) {
			patternField1.setText("");
			patternField2.setText("");			
			typeComboBox1.setEnabled(enabled);
			variableComboBox1.setEnabled(enabled);
			patternField1.setEnabled(enabled);
			variableComboBox2.setEnabled(enabled);
			patternField2.setEnabled(enabled);
			if (enabled == true) {
				updateStatementTypes();
			}
			this.updateUI();
		}
		
		// used in the coder relation table model to update the statement table when coder relations are changed 
		public void updateFilter() {
			if (showAll.isSelected()) {
				allFilter();
				toggleEnabled(false);
			}
			if (showCurrent.isSelected()) {
				toggleEnabled(false);
				allFilter();
				currentDocumentFilter();
			}
			if (showFilter.isSelected()) {
				toggleEnabled(true);
				filter();
			}
		}
		
		public void allFilter() {
			RowFilter<StatementTableModel, Integer> allFilter = new RowFilter<StatementTableModel, Integer>() {
				public boolean include(Entry<? extends StatementTableModel, ? extends Integer> entry) {
					StatementTableModel stcont = entry.getModel();
					Statement st = stcont.get(entry.getIdentifier());
					boolean[] b = Dna.data.getActiveStatementPermissions(st.getId());
					if (b[0] == true && Dna.data.getActiveDocumentPermissions(st.getDocumentId())[0] == true) {
						return true;
					}
					return false;
				}
			};
			if (showAll.isSelected()) {
				sorter.setRowFilter(allFilter);
			}
		}
		
		public void currentDocumentFilter() {
			int row = Dna.dna.gui.documentPanel.documentTable.getSelectedRow();
			int docId = -1;
			if (row > -1) {
				docId = Dna.dna.gui.documentPanel.documentContainer.get(row).getId();
			}
			final int documentId = docId;
			
			RowFilter<StatementTableModel, Integer> documentFilter = new RowFilter<StatementTableModel, Integer>() {
				public boolean include(Entry<? extends StatementTableModel, ? extends Integer> entry) {
					StatementTableModel stcont = entry.getModel();
					Statement st = stcont.get(entry.getIdentifier());
					boolean[] b = Dna.data.getActiveStatementPermissions(st.getId());
					if (st.getDocumentId() == documentId && b[0] == true && Dna.data.getActiveDocumentPermissions(st.getDocumentId())[0] == true) {
						return true;
					}
					return false;
				}
			};
			if (showCurrent.isSelected()) {
				sorter.setRowFilter(documentFilter);
			}
		}
		
		private void filter() {
			String fn = Dna.data.getSettings().get("filename");
			if (fn != null && !fn.equals("")) {
				String p1 = patternField1.getText();
				String p2 = patternField2.getText();

				String t1 = (String) typeComboBox1.getSelectedItem();
				String v1 = (String) variableComboBox1.getSelectedItem();
				if (p1 == null) {
					p1 = "";
				}
				if (t1 == null) {
					t1 = "";
				}
				if (v1 == null) {
					v1 = "";
				}

				String v2 = (String) variableComboBox2.getSelectedItem();
				if (p2 == null) {
					p2 = "";
				}
				if (v2 == null) {
					v2 = "";
				}

				if (!t1.equals("") && ! v1.equals("") && !v2.equals("")) {
					ArrayList<Integer> ids1 = new ArrayList<Integer>();
					Pattern p = Pattern.compile(p1);
					for (int i = 0; i < Dna.data.getStatements().size(); i++) {
						String s = (String) Dna.data.getStatements().get(i).getValues().get(v1);
						Matcher m = p.matcher(s);
						boolean b = m.find();
						if (b == true) {
							ids1.add(Dna.data.getStatements().get(i).getId());
						}
					}
					ArrayList<Integer> ids2 = new ArrayList<Integer>();
					p = Pattern.compile(p2);
					for (int i = 0; i < Dna.data.getStatements().size(); i++) {
						String s = (String) Dna.data.getStatements().get(i).getValues().get(v2);
						Matcher m = p.matcher(s);
						boolean b = m.find();
						if (b == true) {
							ids2.add(Dna.data.getStatements().get(i).getId());
						}
					}
					
					final String p1final = p1;
					final String p2final = p2;

					RowFilter<StatementTableModel, Integer> idFilter = new RowFilter<StatementTableModel, Integer>() {
						public boolean include(Entry<? extends StatementTableModel, ? extends Integer> entry) {
							StatementTableModel stcont = entry.getModel();
							Statement st = stcont.get(entry.getIdentifier());
							boolean[] b = Dna.data.getActiveStatementPermissions(st.getId());
							boolean contentMatch;
							if (ids1.contains(st.getId()) && (ids2.contains(st.getId()) || p2final.equals(""))) {
								contentMatch = true;
							} else if (ids2.contains(st.getId()) && (ids1.contains(st.getId()) || p1final.equals(""))) {
								contentMatch = true;
							} else {
								contentMatch = false;
							}

							if (contentMatch == true && b[0] == true && Dna.data.getActiveDocumentPermissions(st.getDocumentId())[0] == true) {
								return true;
							}
							return false;
						}
					};
					if (showFilter.isSelected()) {
						sorter.setRowFilter(idFilter);
					}
				}
			}
		}
	}
	
	public class StatementCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			int modelRow = table.convertRowIndexToModel(row);
			c.setBackground(Dna.data.getStatementColor(((StatementTableModel)table.getModel()).get(modelRow).getId()));
			return c;
		}
	}
	
}
