package dna.export;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import dna.Dna;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementType;
import dna.renderer.StatementTypeComboBoxModel;
import dna.renderer.StatementTypeComboBoxRenderer;

@SuppressWarnings("serial")
public class ExportGui extends JDialog {
	JPanel cards;
	CardLayout cl;
	String networkType;
	ExportSetting exportSetting;
	JTree tree;
	DefaultMutableTreeNode top, networkDataTypeNode, variablesNode, agreementNode, excludeNode, summaryNode;
	JList<String> var1List, var2List, var3List;
	JLabel var1Label, var2Label, var3Label, variablesQuestion;
	JRadioButton ignoreButton, congruenceButton, conflictButton, subtractButton;
	
	public ExportGui() {
		this.setTitle("Export data");
		this.setModal(true);
		ImageIcon networkIcon = new ImageIcon(getClass().getResource("/icons/chart_organisation.png"));
		this.setIconImage(networkIcon.getImage());
		this.setLayout(new BorderLayout());
		cl = new CardLayout();
		cards = new JPanel(cl);
		
		exportSetting = populateInitialSettings();
		loadCard1();
		loadCard2();
		loadCard3();
		loadCard4();
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		ImageIcon backIcon = new ImageIcon(getClass().getResource("/icons/resultset_previous.png"));
		ImageIcon nextIcon = new ImageIcon(getClass().getResource("/icons/resultset_next.png"));
		ImageIcon cancelIcon = new ImageIcon(getClass().getResource("/icons/cancel.png"));
		ImageIcon exportIcon = new ImageIcon(getClass().getResource("/icons/accept.png"));
		JButton back = new JButton("back", backIcon);
		back.setEnabled(false);
		JButton next = new JButton("next", nextIcon);
		JButton cancel = new JButton("cancel", cancelIcon);
		JButton export = new JButton("export", exportIcon);
		export.setEnabled(false);
		buttonPanel.add(cancel);
		buttonPanel.add(back);
		buttonPanel.add(next);
		buttonPanel.add(export);

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		

		JPanel menu = new JPanel();
		menu.setLayout(new BorderLayout());
		
		// Tree on the left
		top = new DefaultMutableTreeNode("Options");
		tree = new JTree(top);
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		networkDataTypeNode = new DefaultMutableTreeNode("Network data type");
		variablesNode = new DefaultMutableTreeNode("Variables");
		agreementNode = new DefaultMutableTreeNode("Agreement");
		excludeNode = new DefaultMutableTreeNode("Exclude");
		summaryNode = new DefaultMutableTreeNode("Summary");
		top.add(networkDataTypeNode);
		top.add(variablesNode);
		top.add(agreementNode);
		top.add(excludeNode);
		top.add(summaryNode);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		tree.setToggleClickCount(0);
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				String node = (String) tree.getLastSelectedPathComponent().toString();
				if (node.equals("Network data type")) {
					cl.show(cards, "networkDataType");
				} else if (node.equals("Variables")) {
					cl.show(cards, "variables");
				} else if (node.equals("Agreement")) {
					cl.show(cards, "agreement");
				} else if (node.equals("Exclude")) {
					cl.show(cards, "exclude");
				} else if (node.equals("Options")) {
					tree.setSelectionRow(1);
				} else {
					System.out.println(node);
				}
			}
		});
		menu.add(tree);
		
		// Split pane
		JSplitPane sp = new JSplitPane();
		sp.setLeftComponent(menu);
		sp.setRightComponent(cards);
		this.add(sp);
		sp.setEnabled(false);
		tree.setSelectionRow(1);
		
		this.add(sp, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private ExportSetting populateInitialSettings() {
		// find dates to populate initial settings
		ArrayList<Date> dates = new ArrayList<Date>();
		for (int i = 0; i < Dna.data.getStatements().size(); i++) {
			dates.add(Dna.data.getStatements().get(i).getDate());
		}
		Collections.sort(dates);
		Date startDate = dates.get(0);
		Date stopDate = dates.get(dates.size() - 1);
		
		// find statement type and variables to populate initial settings
		String var1 = "";
		String var2 = "";
		StatementType statementType = null;
		for (int i = 0; i < Dna.data.getStatementTypes().size(); i++) {
			statementType = Dna.data.getStatementTypes().get(i);
			DefaultListModel<String> vars = getVariablesList(statementType, false, true, false, false);
			if (vars.size() > 1) {
				var1 = vars.get(0);
				var2 = vars.get(1);
				break;
			}
		}
		if (var1.equals("") && var2.equals("")) {
			for (int i = 0; i < Dna.data.getStatementTypes().size(); i++) {
				statementType = Dna.data.getStatementTypes().get(i);
				DefaultListModel<String> vars = getVariablesList(statementType, false, true, false, false);
				if (vars.size() > 0) {
					var1 = vars.get(0);
					var2 = vars.get(0);
					break;
				}
			}
		}
		
		// create and return settings
		ExportSetting es = new ExportSetting(statementType, startDate, stopDate, var1, var2);
		return es;
	}

	private void loadCard1() {
		JPanel scopePanel = new JPanel(new GridBagLayout());
		scopePanel.setName("networkDataType");
		GridBagConstraints scopegbc = new GridBagConstraints();
		scopegbc.gridx = 0;
		scopegbc.gridy = 0;
		scopegbc.fill = GridBagConstraints.NONE;
		scopegbc.anchor = GridBagConstraints.WEST;
		scopegbc.gridwidth = 3;
		scopegbc.insets = new Insets(10, 0, 10, 0);
		JLabel scopeQuestion = new JLabel("For which statement type would you like to create a network?");
		scopePanel.add(scopeQuestion, scopegbc);
		scopegbc.gridy = 1;
		StatementTypeComboBoxRenderer renderer = new StatementTypeComboBoxRenderer();
		StatementTypeComboBoxModel model = new StatementTypeComboBoxModel();
		JComboBox typeBox = new JComboBox(model);
		typeBox.setRenderer(renderer);
		typeBox.setSelectedItem(exportSetting.getStatementType());
		scopegbc.insets = new Insets(0, 0, 10, 0);
		scopePanel.add(typeBox, scopegbc);
		ActionListener statementAL = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setStatementType((StatementType) typeBox.getSelectedItem());
			}
		};
		typeBox.addActionListener(statementAL);
		
		scopegbc.gridy = 2;
		JLabel modeQuestion = new JLabel("Which type of network would you like to export?");
		scopegbc.insets = new Insets(10, 0, 10, 0);  // vertical space before the question
		scopePanel.add(modeQuestion, scopegbc);
		scopegbc.insets = new Insets(0, 0, 10, 0);
		scopegbc.gridy = 3;
		scopegbc.gridwidth = 1;
		JRadioButton oneModeButton = new JRadioButton("one-mode network");
		oneModeButton.setSelected(true);
		networkType = "oneMode";  // select one-mode network by default
		JRadioButton twoModeButton = new JRadioButton("two-mode network");
		JRadioButton eventListButton = new JRadioButton("event list");
		ButtonGroup bg = new ButtonGroup();
		bg.add(oneModeButton);
		bg.add(twoModeButton);
		bg.add(eventListButton);
		scopePanel.add(oneModeButton, scopegbc);
		scopegbc.gridx = 1;
		scopePanel.add(twoModeButton, scopegbc);
		scopegbc.gridx = 2;
		scopePanel.add(eventListButton, scopegbc);
		ActionListener modeAL = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JRadioButton button = (JRadioButton) e.getSource();
				if (button.getText().equalsIgnoreCase("one-mode network")) {
					exportSetting.setNetworkType("oneMode");
					typeBox.setEnabled(true);
					congruenceButton.setEnabled(true);
					conflictButton.setEnabled(true);
					subtractButton.setEnabled(true);
					var1Label.setText("Node type (row x col)" );
					var2Label.setText("via variable");
					var1Label.setEnabled(true);
					var2Label.setEnabled(true);
					var1List.setEnabled(true);
					var2List.setEnabled(true);
					variablesQuestion.setEnabled(true);
				} else if (button.getText().equals("two-mode network")) {
					exportSetting.setNetworkType("twoMode");
					typeBox.setEnabled(true);
					ignoreButton.setSelected(true);
					congruenceButton.setEnabled(false);
					conflictButton.setEnabled(false);
					subtractButton.setEnabled(false);
					var1Label.setText("first mode (rows)" );
					var2Label.setText("second mode (columns)");
					var1Label.setEnabled(true);
					var2Label.setEnabled(true);
					var1List.setEnabled(true);
					var2List.setEnabled(true);
					variablesQuestion.setEnabled(true);
				} else if (button.getText().equals("event list")) {
					exportSetting.setNetworkType("eventList");
					typeBox.setEnabled(false);
					ignoreButton.setSelected(true);
					congruenceButton.setEnabled(false);
					conflictButton.setEnabled(false);
					subtractButton.setEnabled(false);
					var1Label.setEnabled(false);
					var2Label.setEnabled(false);
					var1List.setEnabled(false);
					var2List.setEnabled(false);
					variablesQuestion.setEnabled(false);
				}
			}
		};
		oneModeButton.addActionListener(modeAL);
		twoModeButton.addActionListener(modeAL);
		eventListButton.addActionListener(modeAL);
		TitledBorder scopeBorder;
		scopeBorder = BorderFactory.createTitledBorder("1 / 7");
		scopePanel.setBorder(scopeBorder);
		cards.add(scopePanel, "networkDataType");
	}
	
	private void loadCard2() {
		JPanel variablesPanel = new JPanel(new GridBagLayout());
		variablesPanel.setName("card2");
		GridBagConstraints vargbc = new GridBagConstraints();
		vargbc.gridx = 0;
		vargbc.gridy = 0;
		vargbc.anchor = GridBagConstraints.WEST;
		vargbc.gridwidth = 3;

		vargbc.insets = new Insets(0, 0, 20, 0);
		variablesQuestion = new JLabel("Please select the variables for network creation.");
		variablesPanel.add(variablesQuestion, vargbc);
		vargbc.gridwidth = 1;
		vargbc.gridy = 1;
		vargbc.insets = new Insets(0, 0, 0, 5);
		
		var1List = new JList<String>();
		var1List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		var1List.setLayoutOrientation(JList.VERTICAL);
		var1List.setVisibleRowCount(5);
		var1List.setPreferredSize(new Dimension(220, 20));
		JScrollPane var1Scroller = new JScrollPane(var1List);
		JPanel var1Panel = new JPanel(new GridBagLayout());
		GridBagConstraints var1gbc = new GridBagConstraints();
		var1gbc.gridx = 0;
		var1gbc.gridy = 0;
		var1gbc.fill = GridBagConstraints.NONE;
		var1Label = new JLabel("first mode (rows)");
		var1Panel.add(var1Label, var1gbc);
		var1gbc.gridy = 1;
		var1Panel.add(var1Scroller, var1gbc);
		variablesPanel.add(var1Panel, vargbc);
		vargbc.gridx = 1;
		
		var2List = new JList<String>();
		var2List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		var2List.setLayoutOrientation(JList.VERTICAL);
		
		var2List.setVisibleRowCount(5);
		var2List.setPreferredSize(new Dimension(220, 20));

		JScrollPane var2Scroller = new JScrollPane(var2List);
		JPanel var2Panel = new JPanel(new GridBagLayout());
		GridBagConstraints var2gbc = new GridBagConstraints();
		var2gbc.gridx = 0;
		var2gbc.gridy = 0;
		var2gbc.fill = GridBagConstraints.NONE;
		var2Label = new JLabel("second mode (columns)");
		var2Panel.add(var2Label, var2gbc);
		var2gbc.gridy = 1;
		var2Panel.add(var2Scroller, var2gbc);
		variablesPanel.add(var2Panel, vargbc);

		var1List.setModel(getVariablesList(exportSetting.getStatementType(), false, true, false, false));
		var2List.setModel(getVariablesList(exportSetting.getStatementType(), false, true, false, false));
		var1List.setSelectedValue(exportSetting.getVar1(), true);
		var2List.setSelectedValue(exportSetting.getVar2(), true);
		TitledBorder variablesBorder;
		variablesBorder = BorderFactory.createTitledBorder("2 / 7");
		variablesPanel.setBorder(variablesBorder);
		cards.add(variablesPanel, "variables");
		
		var1List.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selectedValue = var1List.getSelectedValue();
				if (selectedValue != null) {
					exportSetting.setVar1(selectedValue);
				}
			}
		});

		var2List.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selectedValue = var2List.getSelectedValue();
				if (selectedValue != null) {
					exportSetting.setVar2(selectedValue);
				}
			}
		});
	}

	private void loadCard3() {
		// card 3: agreement qualifier
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setName("card4");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;

		gbc.insets = new Insets(0, 0, 10, 0);
		var3Label = new JLabel("Agreement qualifier:");
		panel.add(var3Label, gbc);
		gbc.gridx = 1;
		gbc.insets = new Insets(0, 20, 10, 0);
		JLabel agreeButtonLabel = new JLabel("Aggregation pattern:");
		panel.add(agreeButtonLabel, gbc);
		gbc.insets = new Insets(0, 0, 10, 0);
		
		var3List = new JList<String>();
		var3List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		var3List.setSelectedIndex(0);
		var3List.setLayoutOrientation(JList.VERTICAL);
		var3List.setVisibleRowCount(4);
		var3List.setPreferredSize(new Dimension(220, 20));
		var3List.setModel(getVariablesList(exportSetting.getStatementType(), false, true, true, true));
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		JScrollPane var3Scroller = new JScrollPane(var3List);
		panel.add(var3Scroller, gbc);
		panel.add(var3Scroller, gbc);
		
		JPanel patternPanel = new JPanel(new GridBagLayout());
		GridBagConstraints patterngbc = new GridBagConstraints();
		patterngbc.gridx = 0;
		patterngbc.gridy = 0;
		patterngbc.anchor = GridBagConstraints.WEST;
		patterngbc.insets = new Insets(0, 0, 1, 0);
		ignoreButton = new JRadioButton("ignore");
		ignoreButton.setSelected(true);
		congruenceButton = new JRadioButton("congruence");
		congruenceButton.setEnabled(false);
		conflictButton = new JRadioButton("conflict");
		conflictButton.setEnabled(false);
		subtractButton = new JRadioButton("subtract");
		subtractButton.setEnabled(false);
		ButtonGroup agreeButtonGroup = new ButtonGroup();
		agreeButtonGroup.add(ignoreButton);
		agreeButtonGroup.add(congruenceButton);
		agreeButtonGroup.add(conflictButton);
		agreeButtonGroup.add(subtractButton);
		
		patternPanel.add(ignoreButton, patterngbc);
		patterngbc.gridy = 1;
		patternPanel.add(congruenceButton, patterngbc);
		patterngbc.gridx = 1;
		patterngbc.gridy = 0;
		patternPanel.add(conflictButton, patterngbc);
		patterngbc.gridy = 1;
		patternPanel.add(subtractButton, patterngbc);
		gbc.gridx = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(0, 20, 10, 0);
		panel.add(patternPanel, gbc);
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		var3Label = new JLabel("The agreement qualifier can be used to connect nodes");
		panel.add(var3Label, gbc);
		gbc.gridy = 3;
		JLabel agreementLabel2 = new JLabel("only if they agree or disagree on a third variable.");
		panel.add(agreementLabel2, gbc);
		
		TitledBorder variablesBorder;
		variablesBorder = BorderFactory.createTitledBorder("3 / 7");
		panel.setBorder(variablesBorder);
		cards.add(panel, "agreement");
		
		var3List.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selectedValue = var3List.getSelectedValue();
				if (selectedValue != null) {
					exportSetting.setQualifier(selectedValue);
					ignoreButton.setEnabled(true);
					if (networkType.equals("oneMode")) {
						congruenceButton.setEnabled(true);
						conflictButton.setEnabled(true);
						subtractButton.setEnabled(true);
					} else {
						congruenceButton.setEnabled(false);
						conflictButton.setEnabled(false);
						subtractButton.setEnabled(false);
					}
				} else {
					exportSetting.setQualifier(null);
					congruenceButton.setEnabled(false);
					conflictButton.setEnabled(false);
					subtractButton.setEnabled(false);
				}
			}
		});

		ignoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setAgreementPattern("ignore");
			}
		});
		congruenceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setAgreementPattern("congruence");
			}
		});
		conflictButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setAgreementPattern("conflict");
			}
		});
		subtractButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setAgreementPattern("subtract");
			}
		});
	}

	private void loadCard4() {
		// card 4: exclude values
		JPanel excludePanel = new JPanel(new GridBagLayout());
		excludePanel.setName("card3");
		GridBagConstraints excludegbc = new GridBagConstraints();
		excludegbc.gridx = 0;
		excludegbc.gridy = 0;
		excludegbc.fill = GridBagConstraints.NONE;
		excludegbc.gridwidth = 3;
		excludegbc.insets = new Insets(0, 30, 0, 0);
		excludegbc.anchor = GridBagConstraints.WEST;
		JLabel agreeQuestion = new JLabel("Select values you want to exclude. This will ignore all");
		JLabel agreeQuestion2 = new JLabel("statements matching your selection during export.");
		excludePanel.add(agreeQuestion, excludegbc);
		excludegbc.gridy = 1;
		excludegbc.insets = new Insets(0, 30, 10, 0);
		excludePanel.add(agreeQuestion2, excludegbc);
		excludegbc.gridwidth = 1;
		excludegbc.gridy = 2;
		excludegbc.insets = new Insets(0, 30, 0, 10);
		excludegbc.fill = GridBagConstraints.VERTICAL;
		JLabel agreeVarLabel = new JLabel("variable");
		excludePanel.add(agreeVarLabel, excludegbc);
		excludegbc.gridy = 3;
		excludegbc.gridheight = 4;
		JList<String> excludeVarList = new JList<String>();
		excludeVarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		excludeVarList.setLayoutOrientation(JList.VERTICAL);
		excludeVarList.setVisibleRowCount(4);
		excludeVarList.setFixedCellWidth(180);
		JScrollPane excludeVarScroller = new JScrollPane(excludeVarList);
		excludePanel.add(excludeVarScroller, excludegbc);
		excludegbc.gridx = 1;
		excludegbc.gridy = 2;
		excludegbc.gridheight = 1;
		JLabel excludeValLabel = new JLabel("exclude values");
		excludePanel.add(excludeValLabel, excludegbc);
		excludegbc.gridy = 3;
		excludegbc.gridheight = 4;
		JList<String> excludeValList = new JList<String>();
		excludeValList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		excludeValList.setLayoutOrientation(JList.VERTICAL);
		excludeValList.setVisibleRowCount(4);
		excludeValList.setFixedCellWidth(180);
		excludeValList.setSize(180, 100);
		JScrollPane excludeValScroller = new JScrollPane(excludeValList);
		excludePanel.add(excludeValScroller, excludegbc);
		
		TitledBorder excludeBorder;
		excludeBorder = BorderFactory.createTitledBorder("4 / 7");
		excludePanel.setBorder(excludeBorder);
		cards.add(excludePanel, "exclude");
		
		excludeVarList.setModel(getVariablesList(exportSetting.getStatementType(), true, true, true, true));
		excludeVarList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selectedValue = excludeVarList.getSelectedValue();
				if (e.getValueIsAdjusting() == false) {
					if (selectedValue != null) {
						String[] entriesArray = Dna.data.getStringEntries(exportSetting.getStatementType().getId(), selectedValue);
						excludeValList.setListData(entriesArray);
						int[] indices = new int[exportSetting.getExcludeValues().get(selectedValue).size()];
						System.out.println(indices.length);
						for (int i = 0; i < entriesArray.length; i++) {
							for (int j = 0; j < exportSetting.getExcludeValues().get(selectedValue).size(); j++) {
								if (entriesArray[i].equals(exportSetting.getExcludeValues().get(selectedValue).get(j))) {
									indices[j] = i;
								}
							}
						}
						excludeValList.setSelectedIndices(indices);
					}
				}
			}
		});
		
		excludeValList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selectedVariable = excludeVarList.getSelectedValue();
				List<String> selectedValues = excludeValList.getSelectedValuesList();
				if (e.getValueIsAdjusting() == true) {
					if (selectedValues != null){
						ArrayList<String> sel = new ArrayList<String>(selectedValues);
						exportSetting.getExcludeValues().put(selectedVariable, (ArrayList<String>) sel);
					}
				}
			}
		});		
	}
	
	/**
	 * This function returns a {@link DefaultListModel} with the variables of the statementType selected to fill a JList.
	 * 
	 * @param longtext	boolean indicating whether long text variables should be included.
	 * @param shorttext	boolean indicating whether short text variables should be included.
	 * @param integer	boolean indicating whether integer variables should be included.
	 * @param bool		boolean indicating whether boolean variables should be included.
	 * @return			{@link DefaultListModel<String>} with variables of the the statementType selected.
	 */
	DefaultListModel<String> getVariablesList(StatementType statementType, boolean longtext, boolean shorttext, boolean integer, boolean bool) {
		LinkedHashMap<String, String> variables = statementType.getVariables();
		Iterator<String> it = variables.keySet().iterator();
		DefaultListModel<String> listData = new DefaultListModel<String>();
		while (it.hasNext()) {
			String var = it.next();
			if ((longtext == true && variables.get(var).equals("long text")) || 
					(shorttext == true && variables.get(var).equals("short text")) ||
					(integer == true && variables.get(var).equals("integer")) ||
					(bool == true && variables.get(var).equals("boolean"))) {
				listData.addElement(var);
			}
		}
		return listData;
	}
}
