package dna;

import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NetworkExporter extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	JPanel cards;
	CardLayout cl;
	String statementType = null;
	ArrayList<StatementType> typeList;
	JComboBox<String> typeBox;
	JButton back, next, cancel, export;
	JRadioButton oneModeButton, twoModeButton;
	
	public NetworkExporter() {
		this.setTitle("Export data");
		this.setModal(true);
		ImageIcon networkIcon = new ImageIcon(getClass().getResource(
				"/icons/chart_organisation.png"));
		this.setIconImage(networkIcon.getImage());
		this.setLayout(new BorderLayout());
		cl = new CardLayout();
		cards = new JPanel(cl);
		
		// card 1: select statement type
		JPanel scopePanel = new JPanel(new GridBagLayout());
		GridBagConstraints scopegbc = new GridBagConstraints();
		scopegbc.gridx = 0;
		scopegbc.gridy = 0;
		scopegbc.fill = GridBagConstraints.NONE;
		scopegbc.anchor = GridBagConstraints.WEST;
		scopegbc.gridwidth = 2;
		scopegbc.insets = new Insets(0, 0, 10, 0);
		JLabel scopeQuestion = new JLabel("For which statement " +
				"type would you like to create a network?");
		scopePanel.add(scopeQuestion, scopegbc);
		scopegbc.gridy = 1;
		typeList = Dna.dna.db.getStatementTypes();
		String[] types = new String[typeList.size()];
		for (int i = 0; i < typeList.size(); i++) {
			types[i] = typeList.get(i).getLabel();
		}
		typeBox = new JComboBox<String>(types);
		scopePanel.add(typeBox, scopegbc);
		scopegbc.gridy = 2;
		JLabel modeQuestion = new JLabel("Which type of network would " +
				"you like to export?");
		scopePanel.add(modeQuestion, scopegbc);
		scopegbc.gridy = 3;
		scopegbc.gridwidth = 1;
		oneModeButton = new JRadioButton("one-mode network");
		oneModeButton.setSelected(true);
		twoModeButton = new JRadioButton("two-mode network");
		ButtonGroup bg = new ButtonGroup();
		bg.add(oneModeButton);
		bg.add(twoModeButton);
		scopePanel.add(oneModeButton, scopegbc);
		scopegbc.gridx = 1;
		scopePanel.add(twoModeButton, scopegbc);
		ActionListener modeAL = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals("oneModeButton")) {
					
				} else if (e.getSource().equals("twoModeButton")) {
					
				}
			}
			
		};
		oneModeButton.addActionListener(modeAL);
		twoModeButton.addActionListener(modeAL);
		TitledBorder scopeBorder;
		scopeBorder = BorderFactory.createTitledBorder("1 / 7");
		scopePanel.setBorder(scopeBorder);
		cards.add(scopePanel, "statementType");
		
		// card 2: variables
		JPanel variablesPanel = new JPanel(new GridBagLayout());
		GridBagConstraints vargbc = new GridBagConstraints();
		vargbc.gridx = 0;
		vargbc.gridy = 0;
		vargbc.fill = GridBagConstraints.NONE;
		vargbc.gridwidth = 2;
		vargbc.insets = new Insets(0, 0, 10, 0);
		JLabel variablesQuestion = new JLabel("Please select the variable(s) " +
				"representing your nodes.");
		variablesPanel.add(variablesQuestion, vargbc);
		vargbc.gridwidth = 1;
		vargbc.gridy = 1;
		vargbc.insets = new Insets(0, 0, 0, 5);
		
		JList<String> var1List = new JList<String>();
		var1List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		var1List.setLayoutOrientation(JList.VERTICAL);
		var1List.setVisibleRowCount(3);
		var1List.setFixedCellWidth(180);
		JScrollPane var1Scroller = new JScrollPane(var1List);
		JPanel var1Panel = new JPanel(new GridBagLayout());
		GridBagConstraints var1gbc = new GridBagConstraints();
		var1gbc.gridx = 0;
		var1gbc.gridy = 0;
		var1gbc.fill = GridBagConstraints.NONE;
		JLabel var1Label = new JLabel("first mode (rows)");
		var1Panel.add(var1Label, var1gbc);
		var1gbc.gridy = 1;
		var1Panel.add(var1Scroller, var1gbc);
		variablesPanel.add(var1Panel, vargbc);
		vargbc.gridx = 1;
		
		JList<String> var2List = new JList<String>();
		var2List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		var2List.setLayoutOrientation(JList.VERTICAL);
		var2List.setVisibleRowCount(3);
		var2List.setFixedCellWidth(180);
		JScrollPane var2Scroller = new JScrollPane(var2List);
		JPanel var2Panel = new JPanel(new GridBagLayout());
		GridBagConstraints var2gbc = new GridBagConstraints();
		var2gbc.gridx = 0;
		var2gbc.gridy = 0;
		var2gbc.fill = GridBagConstraints.NONE;
		JLabel var2Label = new JLabel("second mode (columns)");
		var2Panel.add(var2Label, var2gbc);
		var2gbc.gridy = 1;
		var2Panel.add(var2Scroller, var2gbc);
		variablesPanel.add(var2Panel, vargbc);
		
		TitledBorder variablesBorder;
		variablesBorder = BorderFactory.createTitledBorder("2 / 7");
		variablesPanel.setBorder(variablesBorder);
		cards.add(variablesPanel, "variables");
		
		// card 3: agreement
		JPanel agreePanel = new JPanel(new GridBagLayout());
		GridBagConstraints agreegbc = new GridBagConstraints();
		agreegbc.gridx = 0;
		agreegbc.gridy = 0;
		agreegbc.fill = GridBagConstraints.NONE;
		agreegbc.gridwidth = 3;
		agreegbc.insets = new Insets(0, 0, 10, 0);
		agreegbc.anchor = GridBagConstraints.WEST;
		JLabel agreeQuestion = new JLabel("Define the agreement qualifier.");
		agreePanel.add(agreeQuestion, agreegbc);
		agreegbc.gridwidth = 1;
		agreegbc.gridy = 1;
		agreegbc.insets = new Insets(0, 0, 0, 10);
		agreegbc.fill = GridBagConstraints.VERTICAL;
		JLabel agreeVarLabel = new JLabel("variable");
		agreePanel.add(agreeVarLabel, agreegbc);
		agreegbc.gridy = 2;
		agreegbc.gridheight = 4;
		JList<String> agreeVarList = new JList<String>();
		agreeVarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		agreeVarList.setLayoutOrientation(JList.VERTICAL);
		agreeVarList.setVisibleRowCount(3);
		agreeVarList.setFixedCellWidth(120);
		JScrollPane agreeVarScroller = new JScrollPane(agreeVarList);
		agreePanel.add(agreeVarScroller, agreegbc);
		agreegbc.gridx = 1;
		agreegbc.gridy = 1;
		agreegbc.gridheight = 1;
		JLabel agreeValLabel = new JLabel("restrict to values");
		agreePanel.add(agreeValLabel, agreegbc);
		agreegbc.gridy = 2;
		agreegbc.gridheight = 4;
		JList<String> agreeValList = new JList<String>();
		agreeValList.setSelectionMode(ListSelectionModel.
				MULTIPLE_INTERVAL_SELECTION);
		agreeValList.setLayoutOrientation(JList.VERTICAL);
		agreeValList.setVisibleRowCount(3);
		agreeValList.setFixedCellWidth(120);
		JScrollPane agreeValScroller = new JScrollPane(agreeValList);
		agreePanel.add(agreeValScroller, agreegbc);
		agreegbc.gridx = 2;
		agreegbc.gridy = 1;
		agreegbc.gridheight = 1;
		JLabel agreeButtonLabel = new JLabel("agreement pattern");
		agreePanel.add(agreeButtonLabel, agreegbc);
		agreegbc.gridy = 2;
		JRadioButton congruenceButton = new JRadioButton("congruence");
		congruenceButton.setSelected(true);
		JRadioButton conflictButton = new JRadioButton("conflict");
		JRadioButton subtractButton = new JRadioButton("subtract");
		JRadioButton separateButton = new JRadioButton("separate");
		ButtonGroup agreeButtonGroup = new ButtonGroup();
		agreeButtonGroup.add(congruenceButton);
		agreeButtonGroup.add(conflictButton);
		agreeButtonGroup.add(subtractButton);
		agreeButtonGroup.add(separateButton);
		agreePanel.add(congruenceButton, agreegbc);
		agreegbc.gridy = 3;
		agreePanel.add(conflictButton, agreegbc);
		agreegbc.gridy = 4;
		agreePanel.add(subtractButton, agreegbc);
		agreegbc.gridy = 5;
		agreePanel.add(separateButton, agreegbc);
		TitledBorder agreementBorder;
		agreementBorder = BorderFactory.createTitledBorder("3 / 7");
		agreePanel.setBorder(agreementBorder);
		cards.add(agreePanel, "agreement");
		
		// card 4: exclude nodes
		JPanel excludePanel = new JPanel(new GridBagLayout());
		GridBagConstraints excludegbc = new GridBagConstraints();
		excludegbc.gridx = 0;
		excludegbc.gridy = 0;
		excludegbc.fill = GridBagConstraints.NONE;
		excludegbc.anchor = GridBagConstraints.WEST;
		excludegbc.gridwidth = 2;
		excludegbc.insets = new Insets(0, 0, 10, 0);
		JLabel excludeQuestion = new JLabel("Please select nodes you want to " +
				"exclude from the analysis.");
		excludePanel.add(excludeQuestion, excludegbc);
		excludegbc.gridwidth = 1;
		excludegbc.gridy = 1;
		excludegbc.insets = new Insets(0, 0, 0, 5);
		
		JList<String> exclude1List = new JList<String>();
		exclude1List.setSelectionMode(ListSelectionModel.
				MULTIPLE_INTERVAL_SELECTION);
		exclude1List.setLayoutOrientation(JList.VERTICAL);
		exclude1List.setVisibleRowCount(3);
		exclude1List.setFixedCellWidth(180);
		JScrollPane exclude1Scroller = new JScrollPane(exclude1List);
		JPanel exclude1Panel = new JPanel(new GridBagLayout());
		GridBagConstraints exclude1gbc = new GridBagConstraints();
		exclude1gbc.gridx = 0;
		exclude1gbc.gridy = 0;
		exclude1gbc.fill = GridBagConstraints.NONE;
		JLabel exclude1Label = new JLabel("first mode");
		exclude1Panel.add(exclude1Label, exclude1gbc);
		exclude1gbc.gridy = 1;
		exclude1Panel.add(exclude1Scroller, exclude1gbc);
		excludePanel.add(exclude1Panel, excludegbc);
		excludegbc.gridx = 1;
		
		JList<String> exclude2List = new JList<String>();
		exclude2List.setSelectionMode(ListSelectionModel.
				MULTIPLE_INTERVAL_SELECTION);
		exclude2List.setLayoutOrientation(JList.VERTICAL);
		exclude2List.setVisibleRowCount(3);
		exclude2List.setFixedCellWidth(180);
		JScrollPane exclude2Scroller = new JScrollPane(exclude2List);
		JPanel exclude2Panel = new JPanel(new GridBagLayout());
		GridBagConstraints exclude2gbc = new GridBagConstraints();
		exclude2gbc.gridx = 0;
		exclude2gbc.gridy = 0;
		exclude2gbc.fill = GridBagConstraints.NONE;
		JLabel exclude2Label = new JLabel("second mode");
		exclude2Panel.add(exclude2Label, exclude2gbc);
		exclude2gbc.gridy = 1;
		exclude2Panel.add(exclude2Scroller, exclude2gbc);
		excludePanel.add(exclude2Panel, excludegbc);

		excludegbc.gridy = 2;
		excludegbc.gridx = 0;
		excludegbc.gridwidth = 2;
		excludegbc.insets = new Insets(10, 0, 0, 10);
		JLabel excludeHint = new JLabel("Use the ctrl key to select multiple " +
				"entries.");
		excludePanel.add(excludeHint, excludegbc);
		TitledBorder excludeBorder;
		excludeBorder = BorderFactory.createTitledBorder("4 / 7");
		excludePanel.setBorder(excludeBorder);
		cards.add(excludePanel, "exclude");
		
		// card 5: date range and aggregation
		JPanel datePanel = new JPanel(new GridBagLayout());
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
		startModel.setValue(Dna.dna.db.getFirstDate());
		startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, 
				"yyyy-MM-dd  HH:mm:ss"));
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
		stopModel.setValue(Dna.dna.db.getLastDate());
		stopSpinner.setEditor(new JSpinner.DateEditor(stopSpinner, 
				"yyyy-MM-dd  HH:mm:ss"));
		datePanel.add(stopSpinner, dategbc);
		dategbc.insets = new Insets(0, 0, 0, 30);
		dategbc.gridy = 1;
		dategbc.gridx = 1;
		dategbc.insets = new Insets(0, 0, 0, 0);
		JRadioButton allAggButton = new JRadioButton("whole date range");
		allAggButton.setSelected(true);
		JRadioButton docAggButton = new JRadioButton("per document");
		JRadioButton yearAggButton = new JRadioButton("per calendar year");
		JRadioButton windowAggButton = new JRadioButton("per time window");
		ButtonGroup aggregateButtonGroup = new ButtonGroup();
		aggregateButtonGroup.add(allAggButton);
		aggregateButtonGroup.add(docAggButton);
		aggregateButtonGroup.add(yearAggButton);
		aggregateButtonGroup.add(windowAggButton);
		datePanel.add(allAggButton, dategbc);
		dategbc.gridy = 2;
		datePanel.add(docAggButton, dategbc);
		dategbc.gridy = 3;
		datePanel.add(yearAggButton, dategbc);
		dategbc.gridy = 4;
		datePanel.add(windowAggButton, dategbc);
		dategbc.gridx = 2;
		SpinnerNumberModel dayModel = new SpinnerNumberModel(30, 1, 999, 1);
		JSpinner windowDays = new JSpinner(dayModel);
		windowDays.setEnabled(false);
		datePanel.add(windowDays, dategbc);
		TitledBorder dateBorder;
		dateBorder = BorderFactory.createTitledBorder("5 / 7");
		datePanel.setBorder(dateBorder);
		cards.add(datePanel, "date");

		// card 6: other options: duplicates; normalization
		// TODO
		
		
		
		
		
		// card 7: output format and file
		JPanel outputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints outputgbc = new GridBagConstraints();
		outputgbc.gridx = 0;
		outputgbc.gridy = 0;
		outputgbc.fill = GridBagConstraints.NONE;
		outputgbc.anchor = GridBagConstraints.WEST;
		outputgbc.insets = new Insets(0, 0, 10, 0);
		outputgbc.gridwidth = 2;
		JLabel outputQuestion = new JLabel("Please select the output format " +
				"and file name.");
		outputPanel.add(outputQuestion, outputgbc);
		outputgbc.gridy = 1;
		outputgbc.insets = new Insets(0, 0, 0, 0);
		JRadioButton csvFormatButton = new JRadioButton(
				".csv (comma-separated values)");
		JRadioButton dlFormatButton = new JRadioButton(
				".dl (Ucinet DL fullmatrix)");
		JRadioButton graphmlFormatButton = new JRadioButton(
				".graphml (visone)");
		ButtonGroup outputButtonGroup = new ButtonGroup();
		outputButtonGroup.add(csvFormatButton);
		outputButtonGroup.add(dlFormatButton);
		outputButtonGroup.add(graphmlFormatButton);
		graphmlFormatButton.setSelected(true);
		outputPanel.add(graphmlFormatButton, outputgbc);
		outputgbc.gridy = 2;
		outputPanel.add(dlFormatButton, outputgbc);
		outputgbc.gridy = 3;
		outputPanel.add(csvFormatButton, outputgbc);
		outputgbc.gridy = 4;
		outputgbc.gridwidth = 1;
		outputgbc.insets = new Insets(10, 0, 0, 10);
		ImageIcon fileIcon = new ImageIcon(getClass().getResource(
				"/icons/folder.png"));
		JButton fileButton = new JButton("...", fileIcon);
		fileButton.setPreferredSize(new Dimension(44, 16));
		outputPanel.add(fileButton, outputgbc);
		outputgbc.gridx = 1;
		JLabel fileLabel = new JLabel("(no output file selected)");
		outputPanel.add(fileLabel, outputgbc);
		TitledBorder outputBorder;
		outputBorder = BorderFactory.createTitledBorder("7 / 7");
		outputPanel.setBorder(outputBorder);
		cards.add(outputPanel, "output");
		
		
		// buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		ImageIcon backIcon = new ImageIcon(getClass().getResource(
				"/icons/resultset_previous.png"));
		ImageIcon nextIcon = new ImageIcon(getClass().getResource(
				"/icons/resultset_next.png"));
		ImageIcon cancelIcon = new ImageIcon(getClass().getResource(
				"/icons/cancel.png"));
		ImageIcon exportIcon = new ImageIcon(getClass().getResource(
				"/icons/accept.png"));
		back = new JButton("back", backIcon);
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
				//String current = getCurrentCard().getName();
				//if (current.equals("variables")) {
				//	back.setEnabled(false);
				//} else if (current.equals("custom")) {
				//	next.setEnabled(true);
				//	export.setEnabled(false);
				//}
				cl.previous(cards);
			}
		});
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//String current = getCurrentCard().getName();
				//System.out.println(current);
				//System.out.println(getCurrentCard());
				//if (current.equals("statementType")) {
				//	back.setEnabled(true);
				//} else if (current.equals("custom")) {
				//	next.setEnabled(false);
				//	export.setEnabled(true);
				//}
				cl.next(cards);
			}
		});
		
		this.add(cards, BorderLayout.NORTH);
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public JPanel getCurrentCard() {
		JPanel card = null;
		for (Component comp : cards.getComponents() ) {
			if (comp.isVisible() == true) {
				card = (JPanel)comp;
			}
		}
		return card;
	}

}