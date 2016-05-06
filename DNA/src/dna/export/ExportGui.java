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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import dna.Dna;
import dna.dataStructures.StatementType;
import dna.renderer.StatementTypeComboBoxModel;
import dna.renderer.StatementTypeComboBoxRenderer;

@SuppressWarnings("serial")
public class ExportGui extends JDialog {
	JButton cancel, back, next, export;
	JPanel cards;
	CardLayout cl;
	String networkType;
	ExportSetting exportSetting;
	JTree tree;
	DefaultMutableTreeNode top, networkDataTypeNode, variablesNode, agreementNode, excludeNode, dateRangeNode, customOptionsNode, fileFormatNode;
	JList<String> var1List, var2List, var3List;
	JLabel var1Label, var2Label, var3Label, agreementLabel, agreementLabel2, variablesQuestion, agreeButtonLabel;
	JRadioButton ignoreButton, congruenceButton, conflictButton, subtractButton;
	JList<String> excludeVarList, excludeValList;
	JRadioButton allAggButton, docAggButton, yearAggButton, windowAggButton;
	JSpinner windowDays;
	JLabel daysLabel;
	JLabel normalizationLabel, isolatesLabel, duplicatesLabel;
	JRadioButton cooccurrenceButton, averageButton, jaccardButton, cosineButton, currentNodesButton, allNodesButton, countDuplicatesButton, 
			ignoreDuplicatesButton;
	JRadioButton csvFormatButton, dlFormatButton, graphmlFormatButton;
	
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
		loadCard5();
		loadCard6();
		loadCard7();
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		ImageIcon backIcon = new ImageIcon(getClass().getResource("/icons/resultset_previous.png"));
		ImageIcon nextIcon = new ImageIcon(getClass().getResource("/icons/resultset_next.png"));
		ImageIcon cancelIcon = new ImageIcon(getClass().getResource("/icons/cancel.png"));
		ImageIcon exportIcon = new ImageIcon(getClass().getResource("/icons/accept.png"));
		JButton back = new JButton("back", backIcon);
		back.setEnabled(false);
		next = new JButton("next", nextIcon);
		cancel = new JButton("cancel", cancelIcon);
		export = new JButton("export", exportIcon);
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
		
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = tree.getMaxSelectionRow();
				if (row > 1) {
					tree.setSelectionRow(row - 1);
				}
			}
		});
		
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = tree.getMaxSelectionRow();
				if (row < 7) {
					tree.setSelectionRow(row + 1);
				}
			}
		});

		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NetworkCreator(exportSetting);
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
		dateRangeNode = new DefaultMutableTreeNode("Date range");
		customOptionsNode = new DefaultMutableTreeNode("Custom options");
		fileFormatNode = new DefaultMutableTreeNode("File format");
		top.add(networkDataTypeNode);
		top.add(variablesNode);
		top.add(agreementNode);
		top.add(excludeNode);
		top.add(dateRangeNode);
		top.add(customOptionsNode);
		top.add(fileFormatNode);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		tree.setToggleClickCount(0);
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				String node = (String) tree.getLastSelectedPathComponent().toString();
				if (node.equals("Network data type")) {
					cl.show(cards, "networkDataType");
					next.setEnabled(true);
					back.setEnabled(false);
				} else if (node.equals("Variables")) {
					cl.show(cards, "variables");
					next.setEnabled(true);
					back.setEnabled(true);
				} else if (node.equals("Agreement")) {
					cl.show(cards, "agreement");
					next.setEnabled(true);
					back.setEnabled(true);
				} else if (node.equals("Exclude")) {
					cl.show(cards, "exclude");
					next.setEnabled(true);
					back.setEnabled(true);
				} else if (node.equals("Date range")) {
					cl.show(cards, "dateRange");
					next.setEnabled(true);
					back.setEnabled(true);
				} else if (node.equals("Custom options")) {
					cl.show(cards, "customOptions");
					next.setEnabled(true);
					back.setEnabled(true);
				} else if (node.equals("File format")) {
					cl.show(cards, "fileFormat");
					next.setEnabled(false);
					back.setEnabled(true);
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

		// find dates to populate initial settings
		ArrayList<Date> dates = new ArrayList<Date>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			dates.add(Dna.data.getDocuments().get(i).getDate());
		}
		Collections.sort(dates);
		Date startDate, stopDate;
		if (dates.size() > 0) {
			startDate = dates.get(0);
			stopDate = dates.get(dates.size() - 1);
		} else {
			startDate = null;
			stopDate = null;
		}
		
		// create and return settings
		ExportSetting es = new ExportSetting("oneMode", statementType, startDate, stopDate, var1, var2);
		es.setNetworkType("oneMode");
		return es;
	}

	private void loadCard1() {
		JPanel scopePanel = new JPanel(new GridBagLayout());
		scopePanel.setName("networkDataType");
		GridBagConstraints scopegbc = new GridBagConstraints();
		
		scopegbc.gridy = 0;
		scopegbc.gridx = 0;
		scopegbc.fill = GridBagConstraints.NONE;
		scopegbc.anchor = GridBagConstraints.WEST;
		JLabel modeQuestion = new JLabel("Which type of network would you like to export?");
		scopegbc.insets = new Insets(10, 0, 10, 0);  // vertical space before the question
		scopePanel.add(modeQuestion, scopegbc);
		scopegbc.insets = new Insets(0, 0, 10, 0);
		scopegbc.gridy = 1;
		JRadioButton oneModeButton = new JRadioButton("one-mode network");
		oneModeButton.setSelected(true);
		JRadioButton twoModeButton = new JRadioButton("two-mode network");
		JRadioButton eventListButton = new JRadioButton("event list");
		ButtonGroup bg = new ButtonGroup();
		bg.add(oneModeButton);
		bg.add(twoModeButton);
		bg.add(eventListButton);
		scopePanel.add(oneModeButton, scopegbc);
		scopegbc.gridy = 2;
		scopePanel.add(twoModeButton, scopegbc);
		scopegbc.gridy = 3;
		scopePanel.add(eventListButton, scopegbc);
		
		scopegbc.gridy = 4;
		scopegbc.insets = new Insets(10, 0, 10, 0);
		JLabel scopeQuestion = new JLabel("For which statement type would you like to create a network?");
		scopePanel.add(scopeQuestion, scopegbc);
		scopegbc.gridy = 5;
		StatementTypeComboBoxRenderer renderer = new StatementTypeComboBoxRenderer();
		StatementTypeComboBoxModel model = new StatementTypeComboBoxModel();
		JComboBox typeBox = new JComboBox(model);
		typeBox.setRenderer(renderer);
		typeBox.setSelectedItem(exportSetting.getStatementType());
		scopegbc.insets = new Insets(0, 0, 10, 0);
		scopePanel.add(typeBox, scopegbc);
		ActionListener statementAL = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StatementType statementType = (StatementType) typeBox.getSelectedItem();
				exportSetting.setStatementType(statementType);
				String var1 = "";
				String var2 = "";
				DefaultListModel<String> vars = getVariablesList(statementType, false, true, false, false);
				if (vars.size() > 1) {
					var1 = vars.get(0);
					var2 = vars.get(1);
				} else if (vars.size() > 0) {
					var1 = vars.get(0);
					var2 = vars.get(0);
				}
				exportSetting.setStatementType(statementType);
				exportSetting.setVar1(var1);
				exportSetting.setVar2(var2);
				exportSetting.setQualifier(null);
				var1List.setModel(getVariablesList(statementType, false, true, false, false));
				var2List.setModel(getVariablesList(statementType, false, true, false, false));
				var3List.setModel(getVariablesList(statementType, false, true, true, true));
				ignoreButton.setSelected(true);

				HashMap<String, ArrayList<String>> excludeValues = new HashMap<String, ArrayList<String>>();
				Iterator<String> it = statementType.getVariables().keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					excludeValues.put(key, new ArrayList<String>());
				}
				exportSetting.setExcludeValues(excludeValues);
				excludeVarList.setModel(getVariablesList(statementType, false, true, true, true));
				excludeValList.setModel(new DefaultListModel<String>());
			}
		};
		typeBox.addActionListener(statementAL);
		
		ActionListener modeAL = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JRadioButton button = (JRadioButton) e.getSource();
				if (button.getText().equalsIgnoreCase("one-mode network")) {
					exportSetting.setNetworkType("oneMode");
					if (!var3List.isSelectionEmpty()) {
						congruenceButton.setEnabled(true);
						conflictButton.setEnabled(true);
						subtractButton.setEnabled(true);
					}
					var1Label.setText("Node type (row x col)" );
					var2Label.setText("via variable");
					var1Label.setEnabled(true);
					var2Label.setEnabled(true);
					var1List.setEnabled(true);
					var2List.setEnabled(true);
					variablesQuestion.setEnabled(true);
					agreeButtonLabel.setEnabled(true);
					var3Label.setEnabled(true);
					var3List.setEnabled(true);
					agreementLabel.setEnabled(true);
					agreementLabel2.setEnabled(true);
					ignoreButton.setEnabled(true);
					docAggButton.setEnabled(true);
					yearAggButton.setEnabled(true);
					windowAggButton.setEnabled(true);
					daysLabel.setEnabled(true);
					if (windowAggButton.isSelected()) {
						windowDays.setEnabled(true);
					}
					normalizationLabel.setEnabled(true);
					cooccurrenceButton.setEnabled(true);
					averageButton.setEnabled(true);
					jaccardButton.setEnabled(true);
					cosineButton.setEnabled(true);
					isolatesLabel.setEnabled(true);
					currentNodesButton.setEnabled(true);
					allNodesButton.setEnabled(true);
					duplicatesLabel.setEnabled(true);
					ignoreDuplicatesButton.setEnabled(true);
					countDuplicatesButton.setEnabled(true);
					graphmlFormatButton.setEnabled(true);
					dlFormatButton.setEnabled(true);
				} else if (button.getText().equals("two-mode network")) {
					exportSetting.setNetworkType("twoMode");
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
					var3Label.setEnabled(false);
					var3List.setEnabled(false);
					agreementLabel.setEnabled(false);
					agreementLabel2.setEnabled(false);
					ignoreButton.setEnabled(false);
					agreeButtonLabel.setEnabled(false);
					allAggButton.setSelected(true);
					docAggButton.setEnabled(false);
					yearAggButton.setEnabled(false);
					windowAggButton.setEnabled(false);
					daysLabel.setEnabled(false);
					windowDays.setEnabled(false);
					normalizationLabel.setEnabled(false);
					cooccurrenceButton.setEnabled(false);
					averageButton.setEnabled(false);
					jaccardButton.setEnabled(false);
					cosineButton.setEnabled(false);
					isolatesLabel.setEnabled(true);
					currentNodesButton.setEnabled(true);
					allNodesButton.setEnabled(true);
					duplicatesLabel.setEnabled(true);
					ignoreDuplicatesButton.setEnabled(true);
					countDuplicatesButton.setEnabled(true);
					graphmlFormatButton.setEnabled(true);
					dlFormatButton.setEnabled(true);
				} else if (button.getText().equals("event list")) {
					exportSetting.setNetworkType("eventList");
					ignoreButton.setSelected(true);
					congruenceButton.setEnabled(false);
					conflictButton.setEnabled(false);
					subtractButton.setEnabled(false);
					var1Label.setEnabled(false);
					var2Label.setEnabled(false);
					var1List.setEnabled(false);
					var2List.setEnabled(false);
					variablesQuestion.setEnabled(false);
					var3Label.setEnabled(false);
					var3List.setEnabled(false);
					agreementLabel.setEnabled(false);
					agreementLabel2.setEnabled(false);
					ignoreButton.setEnabled(false);
					agreeButtonLabel.setEnabled(false);
					allAggButton.setSelected(true);
					docAggButton.setEnabled(false);
					yearAggButton.setEnabled(false);
					windowAggButton.setEnabled(false);
					daysLabel.setEnabled(false);
					windowDays.setEnabled(false);
					normalizationLabel.setEnabled(false);
					cooccurrenceButton.setEnabled(false);
					averageButton.setEnabled(false);
					jaccardButton.setEnabled(false);
					cosineButton.setEnabled(false);
					isolatesLabel.setEnabled(false);
					currentNodesButton.setEnabled(false);
					allNodesButton.setEnabled(false);
					duplicatesLabel.setEnabled(false);
					ignoreDuplicatesButton.setEnabled(false);
					countDuplicatesButton.setEnabled(false);
					csvFormatButton.setSelected(true);
					graphmlFormatButton.setEnabled(false);
					dlFormatButton.setEnabled(false);
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
		var1List.setModel(getVariablesList(exportSetting.getStatementType(), false, true, false, false));
		var1List.setSelectedValue(exportSetting.getVar1(), true);
		JScrollPane var1Scroller = new JScrollPane(var1List);
		var1Scroller.setPreferredSize(new Dimension(220, 150));
		var1Scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel var1Panel = new JPanel(new GridBagLayout());
		GridBagConstraints var1gbc = new GridBagConstraints();
		var1gbc.gridx = 0;
		var1gbc.gridy = 0;
		var1gbc.fill = GridBagConstraints.BOTH;
		var1gbc.weightx = 1;
		var1gbc.weighty = 1;
		var1Label = new JLabel("Node type (row x col)");
		var1Panel.add(var1Label, var1gbc);
		var1gbc.gridy = 1;
		var1Panel.add(var1Scroller, var1gbc);
		variablesPanel.add(var1Panel, vargbc);
		vargbc.gridx = 1;
		
		var2List = new JList<String>();
		var2List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		var2List.setLayoutOrientation(JList.VERTICAL);
		var2List.setModel(getVariablesList(exportSetting.getStatementType(), false, true, false, false));
		var2List.setSelectedValue(exportSetting.getVar2(), true);

		JScrollPane var2Scroller = new JScrollPane(var2List);
		var2Scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		var2Scroller.setPreferredSize(new Dimension(220, 150));
		JPanel var2Panel = new JPanel(new GridBagLayout());
		GridBagConstraints var2gbc = new GridBagConstraints();
		var2gbc.gridx = 0;
		var2gbc.gridy = 0;
		var2gbc.fill = GridBagConstraints.NONE;
		var2Label = new JLabel("via variable");
		var2Panel.add(var2Label, var2gbc);
		var2gbc.gridy = 1;
		var2Panel.add(var2Scroller, var2gbc);
		variablesPanel.add(var2Panel, vargbc);
		
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
		agreeButtonLabel = new JLabel("Aggregation pattern:");
		panel.add(agreeButtonLabel, gbc);
		gbc.insets = new Insets(0, 0, 10, 0);
		
		var3List = new JList<String>();
		var3List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		var3List.setSelectedIndex(0);
		var3List.setLayoutOrientation(JList.VERTICAL);
		var3List.setModel(getVariablesList(exportSetting.getStatementType(), false, true, true, true));
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		JScrollPane var3Scroller = new JScrollPane(var3List);
		var3Scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		var3Scroller.setPreferredSize(new Dimension(220, 130));
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
		patterngbc.gridy = 2;
		patternPanel.add(conflictButton, patterngbc);
		patterngbc.gridy = 3;
		patternPanel.add(subtractButton, patterngbc);
		gbc.gridx = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(0, 20, 10, 0);
		panel.add(patternPanel, gbc);
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		agreementLabel = new JLabel("The agreement qualifier can be used to connect nodes");
		panel.add(agreementLabel, gbc);
		gbc.gridy = 3;
		agreementLabel2 = new JLabel("only if they agree or disagree on a third variable.");
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
					if (exportSetting.getNetworkType().equals("oneMode")) {
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
		excludegbc.gridwidth = 2;
		excludegbc.anchor = GridBagConstraints.WEST;
		JLabel agreeQuestion = new JLabel("Select values you want to exclude. This will ignore all");
		JLabel agreeQuestion2 = new JLabel("statements matching your selection during export.");
		excludePanel.add(agreeQuestion, excludegbc);
		excludegbc.gridy = 1;
		excludegbc.insets = new Insets(0, 0, 20, 0);
		excludePanel.add(agreeQuestion2, excludegbc);
		excludegbc.gridwidth = 1;
		excludegbc.gridy = 2;
		excludegbc.insets = new Insets(0, 0, 0, 5);
		JLabel agreeVarLabel = new JLabel("variable");
		excludePanel.add(agreeVarLabel, excludegbc);
		excludegbc.gridy = 3;
		excludeVarList = new JList<String>();
		excludeVarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		excludeVarList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane excludeVarScroller = new JScrollPane(excludeVarList);
		excludeVarScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		excludeVarScroller.setPreferredSize(new Dimension(220, 130));
		excludePanel.add(excludeVarScroller, excludegbc);
		excludegbc.gridx = 1;
		excludegbc.gridy = 2;
		JLabel excludeValLabel = new JLabel("exclude values");
		excludePanel.add(excludeValLabel, excludegbc);
		excludegbc.gridy = 3;
		excludeValList = new JList<String>();
		excludeValList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		excludeValList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane excludeValScroller = new JScrollPane(excludeValList);
		excludeValScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		excludeValScroller.setPreferredSize(new Dimension(220, 130));
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
						String[] entriesArray;
						int[] indices;
						if (excludeVarList.getSelectedIndex() == excludeVarList.getModel().getSize() - 1) {
							ArrayList<String> entriesList = new ArrayList<String>();
							for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
								if (!entriesList.contains(Dna.data.getDocuments().get(i).getType())) {
									entriesList.add(Dna.data.getDocuments().get(i).getType());
								}
							}
							entriesArray = new String[entriesList.size()];
							for (int i = 0; i < entriesList.size(); i++) {
								entriesArray[i] = entriesList.get(i);
							}
							Arrays.sort(entriesArray);
							excludeValList.setListData(entriesArray);
							indices = new int[exportSetting.getTypeExclude().size()];
							for (int i = 0; i < entriesArray.length; i++) {
								for (int j = 0; j < exportSetting.getTypeExclude().size(); j++) {
									if (entriesArray[i].equals(exportSetting.getTypeExclude().get(j))) {
										indices[j] = i;
									}
								}
							}
						} else if (excludeVarList.getSelectedIndex() == excludeVarList.getModel().getSize() - 2) {
							ArrayList<String> entriesList = new ArrayList<String>();
							for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
								if (!entriesList.contains(Dna.data.getDocuments().get(i).getSection())) {
									entriesList.add(Dna.data.getDocuments().get(i).getSection());
								}
							}
							entriesArray = new String[entriesList.size()];
							for (int i = 0; i < entriesList.size(); i++) {
								entriesArray[i] = entriesList.get(i);
							}
							Arrays.sort(entriesArray);
							excludeValList.setListData(entriesArray);
							indices = new int[exportSetting.getSectionExclude().size()];
							for (int i = 0; i < entriesArray.length; i++) {
								for (int j = 0; j < exportSetting.getSectionExclude().size(); j++) {
									if (entriesArray[i].equals(exportSetting.getSectionExclude().get(j))) {
										indices[j] = i;
									}
								}
							}
						} else if (excludeVarList.getSelectedIndex() == excludeVarList.getModel().getSize() - 3) {
							ArrayList<String> entriesList = new ArrayList<String>();
							for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
								if (!entriesList.contains(Dna.data.getDocuments().get(i).getSource())) {
									entriesList.add(Dna.data.getDocuments().get(i).getSource());
								}
							}
							entriesArray = new String[entriesList.size()];
							for (int i = 0; i < entriesList.size(); i++) {
								entriesArray[i] = entriesList.get(i);
							}
							Arrays.sort(entriesArray);
							excludeValList.setListData(entriesArray);
							indices = new int[exportSetting.getSourceExclude().size()];
							for (int i = 0; i < entriesArray.length; i++) {
								for (int j = 0; j < exportSetting.getSourceExclude().size(); j++) {
									if (entriesArray[i].equals(exportSetting.getSourceExclude().get(j))) {
										indices[j] = i;
									}
								}
							}
						} else if (excludeVarList.getSelectedIndex() == excludeVarList.getModel().getSize() - 4) {
							ArrayList<String> entriesList = new ArrayList<String>();
							for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
								if (!entriesList.contains(Dna.data.getDocuments().get(i).getAuthor())) {
									entriesList.add(Dna.data.getDocuments().get(i).getAuthor());
								}
							}
							entriesArray = new String[entriesList.size()];
							for (int i = 0; i < entriesList.size(); i++) {
								entriesArray[i] = entriesList.get(i);
							}
							Arrays.sort(entriesArray);
							excludeValList.setListData(entriesArray);
							indices = new int[exportSetting.getAuthorExclude().size()];
							for (int i = 0; i < entriesArray.length; i++) {
								for (int j = 0; j < exportSetting.getAuthorExclude().size(); j++) {
									if (entriesArray[i].equals(exportSetting.getAuthorExclude().get(j))) {
										indices[j] = i;
									}
								}
							}
						} else {
							entriesArray = Dna.data.getStringEntries(exportSetting.getStatementType().getId(), selectedValue);
							Arrays.sort(entriesArray);
							excludeValList.setListData(entriesArray);
							indices = new int[exportSetting.getExcludeValues().get(selectedValue).size()];
							for (int i = 0; i < entriesArray.length; i++) {
								for (int j = 0; j < exportSetting.getExcludeValues().get(selectedValue).size(); j++) {
									if (entriesArray[i].equals(exportSetting.getExcludeValues().get(selectedValue).get(j))) {
										indices[j] = i;
									}
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
						if (excludeVarList.getSelectedIndex() == excludeVarList.getModel().getSize() - 4) {
							exportSetting.setAuthorExclude(sel);
						} else if (excludeVarList.getSelectedIndex() == excludeVarList.getModel().getSize() - 3) {
							exportSetting.setSourceExclude(sel);
						} else if (excludeVarList.getSelectedIndex() == excludeVarList.getModel().getSize() - 2) {
							exportSetting.setSectionExclude(sel);
						} else if (excludeVarList.getSelectedIndex() == excludeVarList.getModel().getSize() - 1) {
							exportSetting.setTypeExclude(sel);
						} else {
							exportSetting.getExcludeValues().put(selectedVariable, (ArrayList<String>) sel);
						}
					}
				}
			}
		});		
	}

	private void loadCard5() {
		// card 5: date range and aggregation
		JPanel datePanel = new JPanel(new GridBagLayout());
		datePanel.setName("card5");
		GridBagConstraints dategbc = new GridBagConstraints();
		dategbc.gridx = 0;
		dategbc.gridy = 0;
		dategbc.fill = GridBagConstraints.NONE;
		dategbc.anchor = GridBagConstraints.WEST;
		dategbc.insets = new Insets(0, 0, 10, 0);
		JLabel dateQuestion = new JLabel("Choose date range:");
		datePanel.add(dateQuestion, dategbc);
		dategbc.gridx = 1;
		JLabel aggregationQuestion = new JLabel("Aggregation rule:");
		datePanel.add(aggregationQuestion, dategbc);
		dategbc.gridx = 0;
		dategbc.gridy = 1;
		dategbc.insets = new Insets(0, 0, 0, 30);
		JLabel startLabel = new JLabel("include from:");
		datePanel.add(startLabel, dategbc);
		dategbc.gridy = 2;
		SpinnerDateModel startModel = new SpinnerDateModel();
		JSpinner startSpinner = new JSpinner();
		startModel.setCalendarField(Calendar.DAY_OF_YEAR);
		startSpinner.setModel(startModel);
		ArrayList<Date> dates = new ArrayList<Date>();
		for (int i = 0; i < Dna.data.getStatements().size(); i++) {
			dates.add(Dna.data.getStatements().get(i).getDate());
		}
		Collections.sort(dates);
		startModel.setValue(dates.get(0));
		
		startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd  HH:mm:ss"));
		datePanel.add(startSpinner, dategbc);
		dategbc.gridy = 3;
		dategbc.gridx = 0;
		JLabel stopLabel = new JLabel("include until:");
		datePanel.add(stopLabel, dategbc);
		dategbc.gridy = 4;
		SpinnerDateModel stopModel = new SpinnerDateModel();
		JSpinner stopSpinner = new JSpinner();
		stopModel.setCalendarField(Calendar.DAY_OF_YEAR);
		stopSpinner.setModel(stopModel);
		stopModel.setValue(dates.get(dates.size() - 1));
		stopSpinner.setEditor(new JSpinner.DateEditor(stopSpinner, "yyyy-MM-dd  HH:mm:ss"));
		datePanel.add(stopSpinner, dategbc);
		
		startSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				exportSetting.setStartDate((Date) startSpinner.getValue());
			}
		});

		stopSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				exportSetting.setStopDate((Date) stopSpinner.getValue());
			}
		});
		
		dategbc.insets = new Insets(0, 0, 0, 30);
		dategbc.gridy = 1;
		dategbc.gridx = 1;
		dategbc.insets = new Insets(0, 0, 0, 0);
		allAggButton = new JRadioButton("across date range");
		allAggButton.setSelected(true);
		docAggButton = new JRadioButton("per document");
		yearAggButton = new JRadioButton("per calendar year");
		windowAggButton = new JRadioButton("per time window:");
		ButtonGroup aggregateButtonGroup = new ButtonGroup();
		aggregateButtonGroup.add(allAggButton);
		aggregateButtonGroup.add(docAggButton);
		aggregateButtonGroup.add(yearAggButton);
		aggregateButtonGroup.add(windowAggButton);
		
		ActionListener aggregation = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JRadioButton button = (JRadioButton) e.getSource();
				exportSetting.setAggregationRule(button.getText());
				if (((JRadioButton) e.getSource()).getText().equals("per time window:")) {
					windowDays.setEnabled(true);
				} else {
					windowDays.setEnabled(false);
				}
			}
		};
		allAggButton.addActionListener(aggregation);
		docAggButton.addActionListener(aggregation);
		yearAggButton.addActionListener(aggregation);
		windowAggButton.addActionListener(aggregation);		
		
		datePanel.add(allAggButton, dategbc);
		dategbc.gridy = 2;
		datePanel.add(docAggButton, dategbc);
		dategbc.gridy = 3;
		datePanel.add(yearAggButton, dategbc);
		dategbc.gridy = 4;
		datePanel.add(windowAggButton, dategbc);
		dategbc.gridx = 2;
		SpinnerNumberModel dayModel = new SpinnerNumberModel(exportSetting.getWindowSize(), 1, 999, 1);
		windowDays = new JSpinner(dayModel);
		windowDays.setEnabled(false);
		datePanel.add(windowDays, dategbc);
		dategbc.gridx = 3;
		daysLabel = new JLabel(" days");
		datePanel.add(daysLabel, dategbc);
		TitledBorder dateBorder;
		dateBorder = BorderFactory.createTitledBorder("5 / 7");
		datePanel.setBorder(dateBorder);
		cards.add(datePanel, "dateRange");				
	}

	private void loadCard6() {
		// card 6: other options: duplicates; normalization
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setName("card6");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 10, 0, 0);
		normalizationLabel = new JLabel("Normalization:");
		panel.add(normalizationLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		
		JPanel normalizationPanel = new JPanel(new GridBagLayout());
		GridBagConstraints normgbc = new GridBagConstraints();
		normgbc.gridx = 0;
		normgbc.gridy = 0;
		normgbc.anchor = GridBagConstraints.WEST;
		normgbc.insets = new Insets(0, 0, 0, 0);
		cooccurrenceButton = new JRadioButton("Co-ocurrence");
		averageButton = new JRadioButton("Divide by average activity");
		jaccardButton = new JRadioButton("Jaccard similarity");
		cosineButton = new JRadioButton("Cosine similarity");
		ButtonGroup normButtonGroup = new ButtonGroup();
		normButtonGroup.add(cooccurrenceButton);
		normButtonGroup.add(averageButton);
		normButtonGroup.add(jaccardButton);
		normButtonGroup.add(cosineButton);
		normalizationPanel.add(cooccurrenceButton, normgbc);
		cooccurrenceButton.setSelected(true);
		normgbc.gridy = 1;
		normalizationPanel.add(averageButton, normgbc);
		normgbc.gridx = 1;
		normgbc.gridy = 0;
		normgbc.insets = new Insets(0, 20, 0, 0);
		normalizationPanel.add(jaccardButton, normgbc);
		normgbc.gridy = 1;
		normalizationPanel.add(cosineButton, normgbc);
		gbc.gridheight = 1;
		gbc.insets = new Insets(0, 20, 10, 0);
		panel.add(normalizationPanel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		
		isolatesLabel = new JLabel("Isolates:");
		gbc.insets = new Insets(0, 10, 0, 0);
		panel.add(isolatesLabel, gbc);
		
		gbc.gridx = 0;		
		gbc.gridy = 3;
		
		JPanel isolatePanel = new JPanel(new GridBagLayout());
		GridBagConstraints isolatesgbc = new GridBagConstraints();
		isolatesgbc.gridx = 0;
		isolatesgbc.gridy = 0;
		isolatesgbc.anchor = GridBagConstraints.WEST;
		isolatesgbc.insets = new Insets(0, 0, 1, 0);
		currentNodesButton = new JRadioButton("Only include nodes of current time steps");
		currentNodesButton.setSelected(true);
		allNodesButton = new JRadioButton("Include all nodes across time steps");
		ButtonGroup isolButtonGroup = new ButtonGroup();
		isolButtonGroup.add(currentNodesButton);
		isolButtonGroup.add(allNodesButton);
		isolatePanel.add(currentNodesButton, isolatesgbc);
		isolatesgbc.gridy = 1;
		isolatePanel.add(allNodesButton, isolatesgbc);
		gbc.gridheight = 1;
		gbc.insets = new Insets(0, 20, 10, 0);
		panel.add(isolatePanel, gbc);		
		
		gbc.insets = new Insets(0, 10, 0, 0);
		duplicatesLabel = new JLabel("Duplicates:");
		gbc.gridx = 0;
		gbc.gridy = 4;
		panel.add(duplicatesLabel, gbc);
		
		JPanel duplicatePanel = new JPanel(new GridBagLayout());
		GridBagConstraints duplicatesgbc = new GridBagConstraints();
		duplicatesgbc.gridx = 0;
		duplicatesgbc.gridy = 0;
		duplicatesgbc.anchor = GridBagConstraints.WEST;
		duplicatesgbc.insets = new Insets(0, 0, 1, 0);
		countDuplicatesButton = new JRadioButton("Count duplicate statements");
		ignoreDuplicatesButton = new JRadioButton("Ignore duplicate statements");
		ignoreDuplicatesButton.setSelected(true);
		ButtonGroup duplicatesButtonGroup = new ButtonGroup();
		duplicatesButtonGroup.add(ignoreDuplicatesButton);
		duplicatesButtonGroup.add(countDuplicatesButton);
		duplicatePanel.add(ignoreDuplicatesButton, duplicatesgbc);
		duplicatesgbc.gridx = 1;
		duplicatePanel.add(countDuplicatesButton, duplicatesgbc);
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.insets = new Insets(0, 20, 10, 0);
		panel.add(duplicatePanel, gbc);
		
		
		TitledBorder variablesBorder;
		variablesBorder = BorderFactory.createTitledBorder("6 / 7");
		panel.setBorder(variablesBorder);
		cards.add(panel, "customOptions");

		cooccurrenceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setNormalization("cooccurrence");
			}
		});
		averageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setNormalization("average");
			}
		});
		jaccardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setNormalization("jaccard");
			}
		});
		cosineButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setNormalization("cosine");
			}
		});
		
		currentNodesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setIncludeIsolates(false);
			}
		});
		allNodesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setIncludeIsolates(true);
			}
		});
		
		countDuplicatesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setCountDuplicates(true);
			}
		});
		ignoreDuplicatesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportSetting.setCountDuplicates(false);
			}
		});
	}

	private void loadCard7() {
		// card 7: output format and file
		JPanel outputPanel = new JPanel(new GridBagLayout());
		outputPanel.setName("card7");
		GridBagConstraints outputgbc = new GridBagConstraints();
		outputgbc.gridx = 0;
		outputgbc.gridy = 0;
		outputgbc.fill = GridBagConstraints.NONE;
		outputgbc.anchor = GridBagConstraints.WEST;
		outputgbc.insets = new Insets(0, 0, 10, 0);
		outputgbc.gridwidth = 2;
		JLabel outputQuestion = new JLabel("Please select the output format and file name for export.");
		outputPanel.add(outputQuestion, outputgbc);
		outputgbc.gridy = 1;
		outputgbc.insets = new Insets(0, 0, 0, 0);
		csvFormatButton = new JRadioButton(".csv (comma-separated values)");
		dlFormatButton = new JRadioButton(".dl (Ucinet DL fullmatrix)");
		graphmlFormatButton = new JRadioButton(".graphml (visone)");
		ButtonGroup outputButtonGroup = new ButtonGroup();
		outputButtonGroup.add(csvFormatButton);
		outputButtonGroup.add(dlFormatButton);
		outputButtonGroup.add(graphmlFormatButton);
		outputPanel.add(graphmlFormatButton, outputgbc);
		outputgbc.gridy = 2;
		outputPanel.add(dlFormatButton, outputgbc);
		outputgbc.gridy = 3;
		outputPanel.add(csvFormatButton, outputgbc);
		
		ActionListener modeFormat = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JRadioButton button = (JRadioButton) e.getSource();
				if (button.getText().equalsIgnoreCase(".csv (comma-separated values)")) {
					exportSetting.setExportFormat(".csv");
				} else if (button.getText().equals(".dl (Ucinet DL fullmatrix)")) {
					exportSetting.setExportFormat(".dl");
				} else if (button.getText().equals(".graphml (visone)")) {
					exportSetting.setExportFormat(".graphml");
				}
			}
			
		};
		csvFormatButton.addActionListener(modeFormat);
		dlFormatButton.addActionListener(modeFormat);
		graphmlFormatButton.addActionListener(modeFormat);
		csvFormatButton.setSelected(true);
		
		outputgbc.gridy = 4;
		outputgbc.gridwidth = 1;
		outputgbc.insets = new Insets(10, 0, 0, 10);
		ImageIcon fileIcon = new ImageIcon(getClass().getResource("/icons/folder.png"));
		JButton fileButton = new JButton("Browse...", fileIcon);
		outputPanel.add(fileButton, outputgbc);
		outputgbc.gridx = 1;
		JLabel fileLabel = new JLabel("(no output file selected)");
		outputPanel.add(fileLabel, outputgbc);
		TitledBorder outputBorder;
		outputBorder = BorderFactory.createTitledBorder("7 / 7");
		outputPanel.setBorder(outputBorder);
		cards.add(outputPanel, "fileFormat");
		
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(exportSetting.getExportFormat()) || f.isDirectory();
					}
					public String getDescription() {
						return "Network File (*" + exportSetting.getExportFormat() + ")";
					}
				});
				
				int returnVal = fc.showSaveDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String fileName = file.getPath();
					if (!fileName.endsWith(exportSetting.getExportFormat())) {
						fileName = fileName + exportSetting.getExportFormat();
					}
					fileLabel.setText(fileName);
					export.setEnabled(true);
					exportSetting.setFileName(fileName);
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
		if (shorttext == true) {
			listData.addElement("author");
			listData.addElement("source");
			listData.addElement("section");
			listData.addElement("type");
		}
		return listData;
	}
}
