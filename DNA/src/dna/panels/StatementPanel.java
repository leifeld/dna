package dna.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import dna.dataStructures.*;
import dna.Dna;
import dna.renderer.StatementTableModel;
import dna.renderer.StatementTypeComboBoxModel;
import dna.renderer.StatementTypeComboBoxRenderer;

@SuppressWarnings("serial")
public class StatementPanel extends JPanel {
	public StatementTableModel ssc;
	public JTable statementTable;
	JScrollPane statementTableScrollPane;
	public StatementFilter statementFilter;
	TableRowSorter<StatementTableModel> sorter;
	public JComboBox<StatementType> typeComboBox;
	StatementTypeComboBoxRenderer renderer;
	public StatementTypeComboBoxModel model;

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
		statementTable.getColumnModel().getColumn(0).setCellRenderer(statementCellRenderer);
		
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
						int docModelIndex = Dna.gui.documentPanel.documentContainer.getModelIndexById(docId);
						int docRow = Dna.gui.documentPanel.documentTable.convertRowIndexToView(docModelIndex);
						Dna.gui.documentPanel.documentTable.getSelectionModel().setSelectionInterval(docRow, docRow);
						Dna.gui.documentPanel.documentTable.scrollRectToVisible(new Rectangle(Dna.gui.documentPanel.documentTable.getCellRect(docRow, 0, true)));
						Dna.gui.textPanel.selectStatement(statementId, docId, b[1]);
					}
				}
			}
		});
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
	
	public class CustomFilterPanel extends JPanel {
		
		public HashMap<String, Pattern> map = new HashMap<String, Pattern>();
		
		public CustomFilterPanel() {
			// empty panel because no statement type is selected from typeComboBox
		}
		
		public CustomFilterPanel(StatementType statementType) {  // create custom search fields for statement type from typeComboBox
			this.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			
			this.add(Box.createRigidArea(new Dimension(5,5)), gbc);
			gbc.gridy = 1;
			
			JLabel idLabel = new JLabel("ID: ");
    		gbc.weightx = 0.1;
    		gbc.anchor = GridBagConstraints.EAST;
    		this.add(idLabel, gbc);
    		gbc.anchor = GridBagConstraints.WEST;
    		gbc.weightx = 0.9;
    		gbc.gridx = 1;
    		JTextField idField = new JTextField("");
    		idField.setColumns(1);
    		map.put("ID", Pattern.compile(""));
    		idField.getDocument().addDocumentListener(new DocumentListener() {
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
					Pattern pattern = Pattern.compile(idField.getText());
					map.put("ID", pattern);
					CustomFilterPanel.this.addRowFilter(statementType);
				}
			});
    		this.add(idField, gbc);
    		gbc.gridx = 0;
    		gbc.gridy++;
			
			Iterator<String> keyIterator = statementType.getVariables().keySet().iterator();
	        while (keyIterator.hasNext()) {  // create filter fields for all variables
	    		String key = keyIterator.next();
	    		JLabel label = new JLabel(key + ": ");
	    		gbc.weightx = 0.1;
	    		gbc.anchor = GridBagConstraints.EAST;
	    		this.add(label, gbc);
	    		gbc.anchor = GridBagConstraints.WEST;
	    		gbc.weightx = 0.9;
	    		gbc.gridx = 1;
	    		JTextField field = new JTextField("");
	    		field.setColumns(1);
	    		map.put(key, Pattern.compile(""));
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
						try {
							Pattern pattern = Pattern.compile(field.getText());
							map.put(key, pattern);
							CustomFilterPanel.this.addRowFilter(statementType);
						} catch (java.util.regex.PatternSyntaxException pse) {
							// if the pattern is not valid, don't apply the filter...
						}
					}
				};
	    		field.getDocument().addDocumentListener(dl);
	    		this.add(field, gbc);
	    		gbc.gridx = 0;
	    		gbc.gridy++;
	    	}
	        
	        CustomFilterPanel.this.addRowFilter(statementType);
	        
	        statementFilter.remove(statementFilter.getComponentCount() - 1);
	        statementFilter.add(this, BorderLayout.SOUTH);
	        statementFilter.updateUI();
		}
		
		public void addRowFilter(StatementType statementType) {
			try {
				RowFilter<StatementTableModel, Integer> filter = new RowFilter<StatementTableModel, Integer>() {
					public boolean include(Entry<? extends StatementTableModel, ? extends Integer> entry) {
						Statement st = ssc.get(entry.getIdentifier());
						if (st.getStatementTypeId() != statementType.getId()) {
							return false;
						}
						Matcher idMatcher = map.get("ID").matcher(String.valueOf((int) st.getId()));
						if (idMatcher.find() == false) {
							return false;
						}
						boolean match = true;
						Iterator<String> keyIterator = statementType.getVariables().keySet().iterator();
				        while (keyIterator.hasNext()) {
				        	String key = keyIterator.next();
				    		String type = statementType.getVariables().get(key);
				    		String value = "";
				    		if (type.equals("short text") || type.equals("long text")) {
					        	value = (String) st.getValues().get(key);
				    		} else if (type.equals("integer") || type.equals("boolean")) {
				    			value = String.valueOf((int) st.getValues().get(key));
				    		}
				        	if (value != null) {
								Matcher matcher = map.get(key).matcher(value);
								if (matcher.find() == false) {
									match = false;
								}
				        	}
				        }
						return match;
					}
				};
				sorter.setRowFilter(filter);
			} catch (java.util.regex.PatternSyntaxException pse) {
				return;
			}
		}
	}
	
	public class StatementFilter extends JPanel {
		public JRadioButton showAll, showCurrent, showFilter;
		CustomFilterPanel customFilterPanel;
		
		public StatementFilter() {
			this.setLayout(new BorderLayout());
			JPanel showPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			ButtonGroup showGroup = new ButtonGroup();
			showAll = new JRadioButton("all");
			showAll.setSelected(true);
			showCurrent = new JRadioButton("current");
			showFilter = new JRadioButton("filter");
			showGroup.add(showAll);
			showGroup.add(showCurrent);
			showGroup.add(showFilter);
			showPanel.add(showAll);
			showPanel.add(showCurrent);
			showPanel.add(showFilter);
			
			renderer = new StatementTypeComboBoxRenderer();
			model = new StatementTypeComboBoxModel();
			typeComboBox = new JComboBox<StatementType>(model);
			typeComboBox.setRenderer(renderer);
			typeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					customFilterPanel = new CustomFilterPanel((StatementType) model.getSelectedItem());
				}
			});
			typeComboBox.updateUI();
			typeComboBox.setEnabled(false);
			typeComboBox.setVisible(false);
			
			customFilterPanel = new CustomFilterPanel();
			
			this.add(showPanel, BorderLayout.NORTH);
			this.add(typeComboBox, BorderLayout.CENTER);
			this.add(customFilterPanel, BorderLayout.SOUTH);
			
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == showAll) {				
						allFilter();
				        statementFilter.remove(statementFilter.getComponentCount() - 1);
				        statementFilter.add(new CustomFilterPanel(), BorderLayout.SOUTH);
				        statementFilter.updateUI();
						typeComboBox.setEnabled(false);
						typeComboBox.setVisible(false);
					} else if (e.getSource() == showCurrent) {
						allFilter();
						currentDocumentFilter();
				        statementFilter.remove(statementFilter.getComponentCount() - 1);
				        statementFilter.add(new CustomFilterPanel(), BorderLayout.SOUTH);
				        statementFilter.updateUI();
						typeComboBox.setEnabled(false);
						typeComboBox.setVisible(false);
					} else if (e.getSource() == showFilter) {
						typeComboBox.setVisible(true);
						if (Dna.dna.sql != null) {
							typeComboBox.setEnabled(true);
							typeComboBox.updateUI();
							new CustomFilterPanel((StatementType) typeComboBox.getSelectedItem());
						}

					}
					//if (!(e.getSource() == showAll)) {
					//	Dna.mainProgram.contradictionReporter.clearTree();
					//}
				}
			};
			
			showAll.addActionListener(al);
			showCurrent.addActionListener(al);
			showFilter.addActionListener(al);
		}
		
		// used in the coder relation table model to update the statement table when coder relations are changed 
		public void updateFilter() {
			if (showAll.isSelected()) {
				allFilter();
			}
			if (showCurrent.isSelected()) {
				allFilter();
				currentDocumentFilter();
			}
			if (showFilter.isSelected()) {
				showFilter.doClick();
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
			int row = Dna.gui.documentPanel.documentTable.getSelectedRow();
			if (row > -1) {
				int modelIndex = Dna.gui.documentPanel.documentTable.convertRowIndexToModel(row);
				int docId = -1;
				if (modelIndex > -1) {
					docId = Dna.gui.documentPanel.documentContainer.get(modelIndex).getId();
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
