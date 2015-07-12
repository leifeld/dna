package dna;

import javax.swing.ImageIcon;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class NetworkExporter extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	JPanel cards;
	CardLayout cl;
	String statementType = null, selectedVariable = "" ;
	ArrayList<StatementType> typeList;
	JComboBox<String> typeBox;
	JButton back, next, cancel, export;
	JRadioButton oneModeButton, twoModeButton, eventListButton;
	JRadioButton csvFormatButton, dlFormatButton, graphmlFormatButton;
	JRadioButton congruenceButton, conflictButton, subtractButton, separateButton;
	JRadioButton allAggButton, docAggButton, windowAggButton, yearAggButton;
	JList<String> var1List, var2List, var3List,agreeVarList, agreeValList;
	JLabel fileLabel, var1Label, var2Label,var3Label;
	JSpinner startSpinner, stopSpinner;
	String fileName;
	int [] exclude1Indices, exclude2Indices;
	Color row = new Color(220,20,60), column= new Color(65,105,225), agreement= new Color(0,201,87);
	int agreeVarIndex =0;
	int var1modeIndex = 0, var2modeIndex = 0,var3modeIndex = 0;
	HashMap<String, int[]> agreeValIndices = new HashMap<String, int[]>();
	
	
	NetworkExporterObject nt;
	
	//Ele
	boolean selected = false;
	
	public NetworkExporter() {
		this.setTitle("Export data");
		this.setModal(true);
		ImageIcon networkIcon = new ImageIcon(getClass().getResource(
				"/icons/chart_organisation.png"));
		this.setIconImage(networkIcon.getImage());
		this.setLayout(new BorderLayout());
		cl = new CardLayout();
		cards = new JPanel(cl);
		
		loadCard1();
		
		if (!nt.getNetworkType().equals("eventList"))
			loadCard2();
				
		loadCard3();
				
		loadCard4();

		//TODO card 5: other options: duplicates; normalization
				
		loadCard6();
				
		// buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
				String current = getCurrentCard().getName();
			    if ((current.equals("card3"))&&(nt.getNetworkType().equals("eventList")))
					cl.previous(cards);
				cl.previous(cards);
			}
		});
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String current = getCurrentCard().getName();
				if (current.equals("card5"))
				{
					nt.setStartDate((Date) startSpinner.getValue());
					nt.setEndDate((Date) stopSpinner.getValue());
				}
				updateCards();
				//System.out.println(nt.toString());
			  if ((current.equals("card1"))&&(nt.getNetworkType().equals("eventList")))
					cl.next(cards);
				cl.next(cards);
			}
		});
		
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<SidebarStatement> statements = filter(nt);
				Network network;
				
				if (nt.getNetworkType().equals("oneMode"))
				{
					network = oneModeNetwork(statements,nt.getVar1mode(),nt.getVar2mode(), nt.getAgreeVar(), nt.getAgreeValList(),nt.getAgreementPattern());
				
				// TODO write file with the network
					JOptionPane.showMessageDialog(null, "One-mode network exported successfully!", "Information",
                            JOptionPane.INFORMATION_MESSAGE);
				}
				else if (nt.getNetworkType().equals("eventList"))
				{
					releventCSV(statements, fileName);
					JOptionPane.showMessageDialog(null, "Event list network exported successfully!", "Information",
                            JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					network = affiliation(statements, nt.getVar1mode(), nt.getVar2mode(), nt.getAgreeVar(), nt.getAgreeValList());
					
					// TODO write file with the network
					
					releventCSV(statements, fileName);
					JOptionPane.showMessageDialog(null, "Two-mode network exported successfully!", "Information",
                            JOptionPane.INFORMATION_MESSAGE);
				}
				
				
				dispose();
			}

			
		});
		
		this.add(cards, BorderLayout.NORTH);
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void updateCards()
	{
		
		if (nt.getNetworkType().equals("oneMode"))
		{
			var1Label.setText("one-mode node type" );
			var2Label.setText("via variable");
		}
		else
		{
			var1Label.setText("first model (rows)");
			var2Label.setText("second model (columns)");
		}
			
		var1List.setModel(nt.getVariablesList());
		var1List.setSelectedIndex(var1modeIndex);
		var2List.setModel(nt.getVariablesList());	
		var2List.setSelectedIndex(var2modeIndex);
		var3List.setSelectedIndex(var3modeIndex);
		agreeVarList.setModel(nt.getVariablesList());
		
		if (selectedVariable.equalsIgnoreCase(nt.getAgreeVar()))
			agreeValList.setModel(nt.getValuesList());
		else if (selectedVariable.equalsIgnoreCase(nt.getVar1mode()))
			agreeValList.setModel(nt.getValuesVar1());
		else if (selectedVariable.equalsIgnoreCase(nt.getVar2mode()))
			agreeValList.setModel(nt.getValuesVar2());
		else 
			agreeValList.setModel(nt.getVarVal(selectedVariable));
		
		if (agreeValIndices!=null)
		{
			if(agreeValIndices.containsKey(selectedVariable))
				agreeValList.setSelectedIndices(agreeValIndices.get(selectedVariable));
		}
		
		boolean enableAgreement = nt.getEnable();
		
		congruenceButton.setEnabled(enableAgreement);
		conflictButton.setEnabled(enableAgreement);
		subtractButton.setEnabled(enableAgreement);
		separateButton.setEnabled(enableAgreement);
	
		// Just .csv export format valid
		if (nt.getNetworkType().equals("eventList"))
		{
			dlFormatButton.setEnabled(false);
			graphmlFormatButton.setEnabled(false);
		}
		else
		{
			dlFormatButton.setEnabled(true);
			graphmlFormatButton.setEnabled(true);
		}
		
		allAggButton.setEnabled(enableAgreement);
		/* Disable for now
		docAggButton.setEnabled(enableAgreement);
		windowAggButton.setEnabled(enableAgreement);
		yearAggButton.setEnabled(enableAgreement);
		*/	
	}
	
	private void loadCard1() {
		JPanel scopePanel = new JPanel(new GridBagLayout());
		scopePanel.setName("card1");
		GridBagConstraints scopegbc = new GridBagConstraints();
		scopegbc.gridx = 0;
		scopegbc.gridy = 0;
		scopegbc.fill = GridBagConstraints.NONE;
		scopegbc.anchor = GridBagConstraints.WEST;
		scopegbc.gridwidth = 3;
		scopegbc.insets = new Insets(0, 0, 10, 0);
		JLabel scopeQuestion = new JLabel("For which statement " +
				"type would you like to create a network?");
		scopePanel.add(scopeQuestion, scopegbc);
		scopegbc.gridy = 1;
		//Get info from db
		typeList = Dna.dna.db.getStatementTypes();
		String[] types = new String[typeList.size()];
		for (int i = 0; i < typeList.size(); i++) {
			types[i] = typeList.get(i).getLabel();
		}
		typeBox = new JComboBox<String>(types);
		scopePanel.add(typeBox, scopegbc);
		nt = new NetworkExporterObject(typeList.get(0));
		ActionListener statementAL = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nt = new NetworkExporterObject();
				int stSelIndex = typeBox.getSelectedIndex();
				nt.setSt(typeList.get(stSelIndex));
			}
			
		};
		typeBox.addActionListener(statementAL);
				
		scopegbc.gridy = 2;
		JLabel modeQuestion = new JLabel("Which type of network would " +
				"you like to export?");
		scopePanel.add(modeQuestion, scopegbc);
		scopegbc.gridy = 3;
		scopegbc.gridwidth = 1;
		oneModeButton = new JRadioButton("one-mode network");
		oneModeButton.setSelected(true);
		twoModeButton = new JRadioButton("two-mode network");
		eventListButton = new JRadioButton("event list");
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
					nt.setNetworkType("oneMode");
				} else if (button.getText().equals("two-mode network")) {
					nt.setNetworkType("twoMode");					
				} else if (button.getText().equals("event list")) {
					nt.setNetworkType("eventList");
				}
			}
			
		};
		oneModeButton.addActionListener(modeAL);
		twoModeButton.addActionListener(modeAL);
		eventListButton.addActionListener(modeAL);
		TitledBorder scopeBorder;
		scopeBorder = BorderFactory.createTitledBorder("1 / 6");
		scopePanel.setBorder(scopeBorder);
		cards.add(scopePanel, "statementType");
	}

	private void loadCard2() {
		JPanel variablesPanel = new JPanel(new GridBagLayout());
		variablesPanel.setName("card2");
		GridBagConstraints vargbc = new GridBagConstraints();
		vargbc.gridx = 0;
		vargbc.gridy = 0;
		vargbc.fill = GridBagConstraints.NONE;
		vargbc.gridwidth = 3;

		vargbc.insets = new Insets(0, 0, 10, 0);
		JLabel variablesQuestion = new JLabel("Please select the variable(s) " +
				"representing your nodes.");
		variablesPanel.add(variablesQuestion, vargbc);
		vargbc.gridwidth = 1;
		vargbc.gridy = 1;
		vargbc.insets = new Insets(0, 0, 0, 5);
		
		var1List = new JList<String>();
		var1List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		var1List.setSelectionForeground(row);
		var1List.setLayoutOrientation(JList.VERTICAL);
		var1List.setVisibleRowCount(3);
		var1List.setFixedCellWidth(180);
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
		var2List.setSelectionForeground(column);
		var2List.setLayoutOrientation(JList.VERTICAL);
		var2List.setVisibleRowCount(3);
		var2List.setFixedCellWidth(180);

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
		vargbc.gridx = 2;
		
		var3List = new JList<String>();
		var3List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		var3List.setSelectionForeground(agreement);
		var3List.setSelectedIndex(0);
		var3List.setLayoutOrientation(JList.VERTICAL);
		var3List.setVisibleRowCount(3);
		var3List.setFixedCellWidth(180);
		
		JScrollPane var3Scroller = new JScrollPane(var3List);
		JPanel var3Panel = new JPanel(new GridBagLayout());
		GridBagConstraints var3gbc = new GridBagConstraints();
		var3gbc.gridx = 0;
		var3gbc.gridy = 0;
		var3gbc.fill = GridBagConstraints.NONE;
		var3Label = new JLabel("agreement qualifier");
		var3Panel.add(var3Label, var3gbc);
		var3gbc.gridy = 1;
		var3Panel.add(var3Scroller, var3gbc);
		variablesPanel.add(var3Panel, vargbc);

		var1List.setModel(nt.getVariablesList());
		var2List.setModel(nt.getVariablesList());	
		var3List.setModel(nt.getBoolVariablesList());
		
		TitledBorder variablesBorder;
		variablesBorder = BorderFactory.createTitledBorder("2 / 6");
		variablesPanel.setBorder(variablesBorder);
		cards.add(variablesPanel, "variables");
		
		var1List.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList<String> lsl = (JList<String>) e.getSource();
				if (lsl.getSelectedValue()!=null)
				{
					nt.setVar1mode(lsl.getSelectedValue().toString());
					var1modeIndex = lsl.getSelectedIndex();
				}				
			}
					
		});
		
		var2List.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList<String> lsl = (JList<String>) e.getSource();
				if (lsl.getSelectedValue()!=null){
					var2modeIndex = lsl.getSelectedIndex();
					nt.setVar2mode(lsl.getSelectedValue().toString());
				}
				
			}
		});
		
		var3List.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList<String> lsl = (JList<String>) e.getSource();
				if (lsl.getSelectedValue()!=null){
					var3modeIndex = lsl.getSelectedIndex();
					nt.setAgreeVar(lsl.getSelectedValue().toString());
				}
			}
		});
	}
	
	private void loadCard3() {
		// card 3: agreement
		JPanel agreePanel = new JPanel(new GridBagLayout());
		agreePanel.setName("card3");
		GridBagConstraints agreegbc = new GridBagConstraints();
		agreegbc.gridx = 0;
		agreegbc.gridy = 0;
		agreegbc.fill = GridBagConstraints.NONE;
		agreegbc.gridwidth = 3;
		agreegbc.insets = new Insets(0, 0, 10, 0);
		agreegbc.anchor = GridBagConstraints.WEST;
		JLabel agreeQuestion = new JLabel("Define the values that you want to exclude");
		agreePanel.add(agreeQuestion, agreegbc);
		agreegbc.gridwidth = 1;
		agreegbc.gridy = 1;
		agreegbc.insets = new Insets(0, 0, 0, 10);
		agreegbc.fill = GridBagConstraints.VERTICAL;
		JLabel agreeVarLabel = new JLabel("variable");
		agreePanel.add(agreeVarLabel, agreegbc);
		agreegbc.gridy = 2;
		agreegbc.gridheight = 4;
		agreeVarList = new JList<String>();
		agreeVarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		agreeVarList.setLayoutOrientation(JList.VERTICAL);
		agreeVarList.setVisibleRowCount(3);
		agreeVarList.setFixedCellWidth(120);
		JScrollPane agreeVarScroller = new JScrollPane(agreeVarList);
		agreePanel.add(agreeVarScroller, agreegbc);
		agreegbc.gridx = 1;
		agreegbc.gridy = 1;
		agreegbc.gridheight = 1;
		JLabel agreeValLabel = new JLabel("exclude values");
		agreePanel.add(agreeValLabel, agreegbc);
		agreegbc.gridy = 2;
		agreegbc.gridheight = 4;
		agreeValList = new JList<String>();
		agreeValList.setSelectionMode(ListSelectionModel.
				MULTIPLE_INTERVAL_SELECTION);
		agreeValList.setLayoutOrientation(JList.VERTICAL);
		agreeValList.setVisibleRowCount(3);
		agreeValList.setFixedCellWidth(120);
		agreeValList.setSize(120, 100);
		JScrollPane agreeValScroller = new JScrollPane(agreeValList);
		agreePanel.add(agreeValScroller, agreegbc);
		agreegbc.gridx = 2;
		agreegbc.gridy = 1;
		agreegbc.gridheight = 1;
		JLabel agreeButtonLabel = new JLabel("agreement pattern");
		agreePanel.add(agreeButtonLabel, agreegbc);
		agreegbc.gridy = 2;
		congruenceButton = new JRadioButton("congruence");
		congruenceButton.setSelected(true);
		conflictButton = new JRadioButton("conflict");
		subtractButton = new JRadioButton("subtract");
		separateButton = new JRadioButton("separate");
		ButtonGroup agreeButtonGroup = new ButtonGroup();
		agreeButtonGroup.add(congruenceButton);
		agreeButtonGroup.add(conflictButton);
		agreeButtonGroup.add(subtractButton);
		agreeButtonGroup.add(separateButton);
		agreeButtonGroup.clearSelection();
		agreePanel.add(congruenceButton, agreegbc);
		agreegbc.gridy = 3;
		agreePanel.add(conflictButton, agreegbc);
		agreegbc.gridy = 4;
		agreePanel.add(subtractButton, agreegbc);
		agreegbc.gridy = 5;
		agreePanel.add(separateButton, agreegbc);
		TitledBorder agreementBorder;
		agreementBorder = BorderFactory.createTitledBorder("3 / 6");
		agreePanel.setBorder(agreementBorder);
		cards.add(agreePanel, "agreement");
		
		agreeVarList.setModel(nt.getVariablesList());
		
		if (nt.getNetworkType().equalsIgnoreCase("eventList"))
		{
			congruenceButton.setEnabled(false);
			conflictButton.setEnabled(false);
			subtractButton.setEnabled(false);
			separateButton.setEnabled(false);
		}
		
		congruenceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nt.setAgreementPattern("congruence");
			}
		});
		conflictButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nt.setAgreementPattern("conflict");
			}
		});
		subtractButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nt.setAgreementPattern("subtract");
			}
		});
		separateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nt.setAgreementPattern("separate");
			}
		});
		
		agreeVarList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList<String> lsl = (JList<String>) e.getSource();
				if (lsl.getSelectedValue()!=null)
				{					
					selectedVariable = lsl.getSelectedValue().toString();
					agreeVarIndex = lsl.getSelectedIndex();
					
					String type = Dna.dna.db.getDataType(selectedVariable, nt.getSt().getLabel());
					if (type.equals("boolean")||type.equals("integer"))
					{						
						int [] intValues = Dna.dna.db.getAllVariableIntEntries(selectedVariable,nt.getSt().getLabel());
						String [] values = new String [intValues.length];
						for (int i=0; i<intValues.length; i++)
							values[i] = Integer.toString(intValues[i]);
						nt.setValues(values);
					}
					 else
					{
						String [] values = Dna.dna.db.getAllVariableStringEntries(selectedVariable,nt.getSt().getLabel());
						nt.setValues(values);
					}
					
					agreeValList.setModel(nt.getValuesList());
					if (agreeValIndices.containsKey(selectedVariable))
					{
						int[] indices = agreeValIndices.get(selectedVariable);
						agreeValList.setSelectedIndices(indices);
					}
				}				
			}
		});
		
		agreeValList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList<String> lsl = (JList<String>) e.getSource();
				if (lsl.getSelectedValue()!=null){
					ArrayList<String> values = new ArrayList<String>();
					values = (ArrayList<String>) lsl.getSelectedValuesList();
					
					if (selectedVariable.equalsIgnoreCase(nt.getAgreeVar()))
						nt.setAgreeValList(values);
					else if (selectedVariable.equalsIgnoreCase(nt.getVar1mode()))
						nt.setExclude1List(values);
					else if (selectedVariable.equalsIgnoreCase(nt.getVar2mode()))
						nt.setExclude2List(values);
					else
						nt.setVarVal(values, selectedVariable);
					
					agreeValIndices.put(selectedVariable, lsl.getSelectedIndices());
				}
			}
		});		
	}

	//TODO define interface to change colour in a jList
	/*
	private void setListColour(JList jList)
	{
		 Object elements[][] = {
			        {row, nt.getAgreeVar() }};	        
	}*/
	
	private void loadCard4() {
		// card 4: date range and aggregation
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
		startSpinner = new JSpinner();
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
		stopSpinner = new JSpinner();
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
		allAggButton = new JRadioButton("whole date range");
		allAggButton.setSelected(true);
		docAggButton = new JRadioButton("per document");
		yearAggButton = new JRadioButton("per calendar year");
		windowAggButton = new JRadioButton("per time window");
		docAggButton.setEnabled(false);
		yearAggButton.setEnabled(false);
		windowAggButton.setEnabled(false);
		ButtonGroup aggregateButtonGroup = new ButtonGroup();
		aggregateButtonGroup.add(allAggButton);
		aggregateButtonGroup.add(docAggButton);
		aggregateButtonGroup.add(yearAggButton);
		aggregateButtonGroup.add(windowAggButton);
		
		ActionListener aggregation = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JRadioButton button = (JRadioButton) e.getSource();
				nt.setAgregationRule(button.getText());
				nt.setStartDate((Date) startSpinner.getValue());
				nt.setEndDate((Date) stopSpinner.getValue());
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
		SpinnerNumberModel dayModel = new SpinnerNumberModel(30, 1, 999, 1);
		JSpinner windowDays = new JSpinner(dayModel);
		windowDays.setEnabled(false);
		datePanel.add(windowDays, dategbc);
		TitledBorder dateBorder;
		dateBorder = BorderFactory.createTitledBorder("4 / 6");
		datePanel.setBorder(dateBorder);
		
		
		cards.add(datePanel, "date");				
	}

	private void loadCard6() {
		// card 6: output format and file
		JPanel outputPanel = new JPanel(new GridBagLayout());
		outputPanel.setName("card7");
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
		csvFormatButton = new JRadioButton(
				".csv (comma-separated values)");
		dlFormatButton = new JRadioButton(
				".dl (Ucinet DL fullmatrix)");
		graphmlFormatButton = new JRadioButton(
				".graphml (visone)");
		ButtonGroup outputButtonGroup = new ButtonGroup();
		outputButtonGroup.add(csvFormatButton);
		outputButtonGroup.add(dlFormatButton);
		outputButtonGroup.add(graphmlFormatButton);
		outputPanel.add(graphmlFormatButton, outputgbc);
		outputgbc.gridy = 2;
		outputPanel.add(dlFormatButton, outputgbc);
		outputgbc.gridy = 3;
		outputPanel.add(csvFormatButton, outputgbc);
		csvFormatButton.setSelected(true);
		
		ActionListener modeFormat = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JRadioButton button = (JRadioButton) e.getSource();
				if (button.getText().equalsIgnoreCase(".csv (comma-separated values)")) {
					nt.setExportFormat(".csv");
				} else if (button.getText().equals(".dl (Ucinet DL fullmatrix)")) {
					nt.setExportFormat(".dl");
				} else if (button.getText().equals(".graphml (visone)")) {
					nt.setExportFormat(".graphml");
				}
			}
			
		};
		csvFormatButton.addActionListener(modeFormat);
		dlFormatButton.addActionListener(modeFormat);
		graphmlFormatButton.addActionListener(modeFormat);
				
		outputgbc.gridy = 4;
		outputgbc.gridwidth = 1;
		outputgbc.insets = new Insets(10, 0, 0, 10);
		ImageIcon fileIcon = new ImageIcon(getClass().getResource(
				"/icons/folder.png"));
		JButton fileButton = new JButton("...", fileIcon);
		fileButton.setPreferredSize(new Dimension(44, 16));
		outputPanel.add(fileButton, outputgbc);
		outputgbc.gridx = 1;
		fileLabel = new JLabel("(no output file selected)");
		outputPanel.add(fileLabel, outputgbc);
		TitledBorder outputBorder;
		outputBorder = BorderFactory.createTitledBorder("6 / 6");
		outputPanel.setBorder(outputBorder);
		cards.add(outputPanel, "output");
		
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(nt.getExportFormat()) 
								|| f.isDirectory();
					}
					public String getDescription() {
						return "Network File " +
								"(*.csv)";
					}
				});

				int returnVal = fc.showSaveDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					fileName = new String(file.getPath());
					if (!fileName.endsWith(nt.getExportFormat())) {
						fileName = fileName + nt.getExportFormat();
					}
					//Dna.dna.newFile(fileName);
					fileLabel.setText(fileName);
				}
				
				export.setEnabled(true);
			}
		});
	}


	
	private ArrayList<SidebarStatement> filter(NetworkExporterObject nt)
	{
		ArrayList<SidebarStatement> filtStatements = new ArrayList<SidebarStatement>();
		ArrayList<String> entries1 = new ArrayList<String>(); // all variable 1 entries
		ArrayList<String> entries2 = new ArrayList<String>(); // all variable 2 entries
		ArrayList<SidebarStatement> statements = Dna.dna.gui.sidebarPanel.ssc.getAll();
		
		entries1 = nt.getExclude1List();
		entries2 = nt.getExclude2List();
		
		for (int i = 0; i < statements.size(); i++) { // filter statements by type
			SidebarStatement st = statements.get(i);
			if (st.type.equals(nt.getSt().label))
				filtStatements.add(st);
		}
		
		if (!nt.getAgregationRule().equals("whole date range"))
		{		
			for (int i = 0; i < filtStatements.size(); i++) { // filter by date
				SidebarStatement statement = filtStatements.get(i);
				
				if (!(statement.date.after(nt.getStartDate())&&statement.date.before(nt.getEndDate())))
				{
					System.out.println(statement.date);
					filtStatements.remove(statement);
				}
			}
		}
		
		if(nt.getNetworkType().equals("eventList"))
		{
			for (int i = 0; i < filtStatements.size(); i++) 
			{ 
				int statementId = filtStatements.get(i).getStatementId();
				boolean removed = false;
				HashMap<String, ArrayList<String>> otherVar = nt.getFilterVariables();
				Iterator<String> keySetIterator = otherVar.keySet().iterator(); 
				
				while(keySetIterator.hasNext()&&(!removed))
				{ 
					String variable = keySetIterator.next(); 
					String others = Dna.dna.db.getVariableStringEntry(statementId, variable);
					ArrayList<String> values = otherVar.get(variable);
					
					for (int j=0; j<values.size(); j++)
					{
						if  (others.equalsIgnoreCase(values.get(j)))
						{
							filtStatements.remove(i);
							i = i-1;
							removed = true;
							break;
						}
					}
				}
			}
		}
		else
		{	
			for (int i = 0; i < filtStatements.size(); i++) { 
				int statementId = filtStatements.get(i).getStatementId();
				boolean removed = false;
	
				String name1 = Dna.dna.db.getVariableStringEntry(statementId, nt.getVar1mode());
				
				for (int j=0; j<nt.getExclude1List().size(); j++)
				{
					if  (name1.equalsIgnoreCase(entries1.get(j)))
					{
						filtStatements.remove(i);
						i = i-1;
						removed = true;
						break;
					}
				}
				
				if (!removed)
				{
					String name2 = Dna.dna.db.getVariableStringEntry(statementId, nt.getVar2mode());
					
					for (int j=0; j<nt.getExclude2List().size(); j++)
					{
						if  (name2.equalsIgnoreCase(entries2.get(j)))
						{
							
						}
					}
					
					if (!removed)
					{
						String name3 = Dna.dna.db.getVariableStringEntry(statementId, nt.getAgreeVar());
						
						for (int j=0; j<nt.getAgreeValList().length; j++)
						{
							if  (name3.equalsIgnoreCase(""+nt.getAgreeValList()[j]))
							{
								filtStatements.remove(i);
								i = i-1;
								removed = true;
								break;
							}
						}
						
						if (!removed)
						{
							HashMap<String, ArrayList<String>> otherVar = nt.getFilterVariables();
							Iterator<String> keySetIterator = otherVar.keySet().iterator(); 
							
							while(keySetIterator.hasNext()){ 
								String variable = keySetIterator.next(); 
								String others = Dna.dna.db.getVariableStringEntry(statementId, variable);
								ArrayList<String> values = otherVar.get(variable);
								
								for (int j=0; j<values.size(); j++)
								{
									if  (others.equalsIgnoreCase(values.get(j)))
									{
										filtStatements.remove(i);
										i = i-1;
										removed = true;
										break;
									}
								}
							}
						}
					}
				}
			}
			
		}
		
		return filtStatements;
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

	/**
	 * @author philip
	 *
	 * A class for Matrix objects. As two-dimensional arrays do not store the row and column labels, 
	 * this class stores both the two-dimensional array and its labels. Matrix objects are created 
	 * by the different network algorithms. Some of the file export functions take Matrix objects as 
	 * input data.
	 *
	 */
	class Matrix {
		double[][] matrix;
		String[] rownames, colnames;
		
		public Matrix(double[][] matrix, String[] rownames, String[] colnames) {
			this.matrix = matrix;
			this.rownames = rownames;
			this.colnames = colnames;
		}

		/**
		 * @return the matrix
		 */
		public double[][] getMatrix() {
			return matrix;
		}

		/**
		 * @param matrix the matrix to set
		 */
		public void setMatrix(double[][] matrix) {
			this.matrix = matrix;
		}

		/**
		 * @return the rownames
		 */
		public String[] getRownames() {
			return rownames;
		}

		/**
		 * @param rownames the rownames to set
		 */
		public void setRownames(String[] rownames) {
			this.rownames = rownames;
		}

		/**
		 * @return the colnames
		 */
		public String[] getColnames() {
			return colnames;
		}

		/**
		 * @param colnames the colnames to set
		 */
		public void setColnames(String[] colnames) {
			this.colnames = colnames;
		}
	}

	/**
	 * @author philip
	 * 
	 * A class for Edge objects. An edge consists of a source node, a target node, and an edge weight. 
	 * Some of the export functions take Edgelist objects as input data. This class represents the edges in 
	 * such an edge list.
	 * 
	 */
	class Edge {
		String source;
		String target;
		double weight;
		
		public Edge(String source, String target, double weight) {
			this.source = source;
			this.target = target;
			this.weight = weight;
		}

		/**
		 * @return the source
		 */
		public String getSource() {
			return source;
		}

		/**
		 * @param source the source to set
		 */
		public void setSource(String source) {
			this.source = source;
		}

		/**
		 * @return the target
		 */
		public String getTarget() {
			return target;
		}

		/**
		 * @param target the target to set
		 */
		public void setTarget(String target) {
			this.target = target;
		}

		/**
		 * @return the weight
		 */
		public double getWeight() {
			return weight;
		}

		/**
		 * @param weight the weight to set
		 */
		public void setWeight(double weight) {
			this.weight = weight;
		}
	}
		
	/**
	 * @author philip
	 * 
	 * A class for Edgelist objects. An edge list is a list of Edge objects and is an alternative to 
	 * Matrix objects for storing network data. If an edge is added that is already part of the 
	 * edge list, its weights is increased instead of adding a duplicate edge.
	 */
	class Edgelist {
		ArrayList<Edge> edgelist;

		public Edgelist(ArrayList<Edge> edgelist) {
			this.edgelist = edgelist;
		}
		
		public Edgelist() {
			this.edgelist = new ArrayList<Edge>();
		}
		
		public void addEdge(Edge edge) {
			int id = -1;
			for (int i = 0; i < edgelist.size(); i++) {
				if (edgelist.get(i).getSource().equals(edge.getSource()) && edgelist.get(i).getTarget().equals(edge.getTarget())) {
					id = i;
				}
			}
			if (id == -1) {
				edgelist.add(edge);
			} else {
				edgelist.get(id).setWeight(edge.getWeight());
			}
		}
		
		/**
		 * @return unique String array of all source node names in the edge list
		 */
		public String[] getSources() {
			ArrayList<String> sources = new ArrayList<String>();
			String currentSource;
			for (int i = 0; i < edgelist.size(); i++) {
				currentSource = edgelist.get(i).getSource();
				if (!sources.contains(currentSource)) {
					sources.add(currentSource);
				}
			}
			String[] s = new String[sources.size()]; // cast row names from array list to array
			s = sources.toArray(s);
			return s;
		}

		/**
		 * @return unique String array of all target node names in the edge list
		 */
		public String[] getTargets() {
			ArrayList<String> targets = new ArrayList<String>();
			String currentTarget;
			for (int i = 0; i < edgelist.size(); i++) {
				currentTarget = edgelist.get(i).getTarget();
				if (!targets.contains(currentTarget)) {
					targets.add(currentTarget);
				}
			}
			String[] t = new String[targets.size()]; // cast row names from array list to array
			t = targets.toArray(t);
			return t;
		}

		/**
		 * @return the edgelist
		 */
		public ArrayList<Edge> getEdgelist() {
			return edgelist;
		}

		/**
		 * @param edgelist the edgelist to set
		 */
		public void setEdgelist(ArrayList<Edge> edgelist) {
			this.edgelist = edgelist;
		}

	}
	
	/**
	 * @author philip
	 * 
	 * A class for Network objects. A Network object is merely a container for Matrix objects and/or 
	 * Edgelist objects. This container class is necessary because the export functions should be able 
	 * to return either matrices or edgelists; but since only one data type can be returned by functions, 
	 * this is going to be a Network object that contains either the matrix or the edge list or both.
	 * 
	 */
	class Network {
		Matrix matrix;
		Edgelist edgelist;
		int modes;
		
		// constructor when only the matrix has been computed: also convert to edge list
		public Network(Matrix matrix, int modes) {
			this.matrix = matrix;
			this.modes = modes;
			double[][] m = matrix.getMatrix();
			String[] r = matrix.getRownames();
			String[] c = matrix.getColnames();
			ArrayList<Edge> el = new ArrayList<Edge>();
			for (int i = 0; i < m.length; i++) {
				for (int j = 0; j < m[0].length; j++) {
					el.add(new Edge(r[i], c[j], m[i][j]));
				}
			}
			this.edgelist = new Edgelist(el);
		}
		
		// constructor when only the edge list has been computed: also convert to matrix
		public Network(Edgelist edgelist, int modes) {
			this.edgelist = edgelist;
			this.modes = modes;
			String[] sources = edgelist.getSources();
			String[] targets = edgelist.getTargets();
			double[][] mat = new double[sources.length][targets.length];
			int row = -1;
			int col = -1;
			ArrayList<Edge> el = edgelist.getEdgelist();
			for (int i = 0; i < el.size(); i++) {
				for (int j = 0; j < sources.length; j++) {
					if (el.get(i).getSource().equals(sources[j])) {
						row = j;
					}
				}
				for (int j = 0; j < targets.length; j++) {
					if (el.get(i).getTarget().equals(targets[j])) {
						col = j;
					}
				}
				mat[row][col] = el.get(i).getWeight();
			}
			this.matrix = new Matrix(mat, sources, targets);
		}
		
		// constructor when both matrix and edge list are present
		public Network(Matrix matrix, Edgelist edgelist, int modes) {
			this.matrix = matrix;
			this.edgelist = edgelist;
			this.modes = modes;
		}

		/**
		 * @return the matrix
		 */
		public Matrix getMatrix() {
			return matrix;
		}

		/**
		 * @param matrix the matrix to set
		 */
		public void setMatrix(Matrix matrix) {
			this.matrix = matrix;
		}

		/**
		 * @return the edgelist
		 */
		public Edgelist getEdgelist() {
			return edgelist;
		}

		/**
		 * @param edgelist the edgelist to set
		 */
		public void setEdgelist(Edgelist edgelist) {
			this.edgelist = edgelist;
		}

		/**
		 * @return the number of node classes
		 */
		public int getModes() {
			return modes;
		}

		/**
		 * @param modes the number of node classes to set
		 */
		public void setModes(int modes) {
			this.modes = modes;
		}
	}
	
	/**
	 * This function computes the matrix product of two two-dimensional arrays.
	 * 
	 * @param mat1	Two-dimensional array with the first input matrix.
	 * @param mat2	Two-dimensional array with the second input matrix.
	 * @return		Two-dimensional array with the output matrix.
	 */
	public static double[][] multiply(double[][] mat1, double[][] mat2) {
        int aRows = mat1.length;
        int aColumns = mat1[0].length;
        int bRows = mat2.length;
        int bColumns = mat2[0].length;
        
        if (aColumns != bRows) {
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }
        
        double[][] mat3 = new double[aRows][bColumns];
        
        for (int i = 0; i < aRows; i++) { // aRow
            for (int j = 0; j < bColumns; j++) { // bColumn
                for (int k = 0; k < aColumns; k++) { // aColumn
                    mat3[i][j] += mat1[i][k] * mat2[k][j];
                }
            }
        }
        
        return mat3;
    }
	
	/**
	 * This function transposes a two-dimensional array.
	 * 
	 * @param mat	Two-dimensional array that should be transposed.
	 * @return		Transposed two-dimensional array.
	 */
	public static double[][] transpose(double[][] mat) {
	    int m = mat.length;
	    int n = mat[0].length;

	    double[][] t = new double[n][m];

	    for(int i = 0; i < n; i++) {
	    	for(int j = 0; j < m; j++) {
	            t[i][j] = mat[j][i];
	        }
	    }
	    return t;
	}

	/**
	 * This function adds two two-dimensional arrays.
	 * 
	 * @param mat1	Two-dimensional array with the first input matrix.
	 * @param mat2	Two-dimensional array with the second input matrix.
	 * @return		Two-dimensional array with the output matrix.
	 */
	public static double[][] add(double[][] mat1, double[][] mat2) {
        int aRows = mat1.length;
        int aColumns = mat1[0].length;
        int bRows = mat2.length;
        int bColumns = mat2[0].length;

        if (aRows != bRows) {
            throw new IllegalArgumentException("Matrix dimensions do not match: " + aRows + " vs. " + bRows + " rows.");
        }
        if (aColumns != bColumns) {
            throw new IllegalArgumentException("Matrix dimensions do not match: " + aColumns + " vs. " + bColumns + " columns.");
        }
        
        double[][] mat3 = new double[aRows][aColumns];
        
        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < aColumns; j++) {
                mat3[i][j] = mat1[i][j] + mat2[i][j];
            }
        }
        
        return mat3;
    }
	
	/**
	 * This function accepts a list of statements that should be included in the network export, 
	 * retrieves their actual contents from the database, and creates and returns a two-mode 
	 * network matrix (= an affiliation matrix) or network based on the statement data. 
	 * 
	 * @param statements	An array list of SidebarStatement objects that should be included in the export.
	 * @param variable1		The name of the first variable (for the row labels).
	 * @param variable2		The name of the second variable (for the column labels).
	 * @param qualifier		The name of the agreement variable that determines whether an edge should be established.
	 * @param selection		The levels of the variable that should be taken into account during export.
	 * @return				A Network object with an affiliation network.
	 */
	public Network affiliation(ArrayList<SidebarStatement> statements, String variable1, String variable2, String qualifier, int[] selection) {
		ArrayList<String> names1 = new ArrayList<String>(); // unique row labels
		ArrayList<String> names2 = new ArrayList<String>(); // unique column labels
		ArrayList<String> entries1 = new ArrayList<String>(); // all variable 1 entries
		ArrayList<String> entries2 = new ArrayList<String>(); // all variable 2 entries
		ArrayList<Integer> qual = new ArrayList<Integer>(); // all qualifier entries
		for (int i = 0; i < statements.size(); i++) { // retrieve the data for variables 1 and 2 from database
			int statementId = statements.get(i).getStatementId();
			String name1 = Dna.dna.db.getVariableStringEntry(statementId, variable1);
			entries1.add(name1);
			if (!names1.contains(name1)) {
				names1.add(name1);
			}
			String name2 = Dna.dna.db.getVariableStringEntry(statementId, variable2);
			entries2.add(name2);
			if (!names2.contains(name2)) {
				names2.add(name2);
			}
			if (qualifier != null) {
				qual.add(Dna.dna.db.getVariableIntEntry(statementId, qualifier));
			}
		}
		double[][] mat = new double[names1.size()][names2.size()]; // the resulting affiliation matrix; 0 by default
		Edgelist edgelist = new Edgelist();
		for (int i = 0; i < entries1.size(); i++) {
			boolean selected = false; // figure out if the current agreement level should be included
			if (qualifier == null) { // if null, do not regard the qualifier variable and process all statements
				selected = true;
			} else {
				for (int j = 0; j < selection.length; j++) {
					if (qual.get(i) == selection[j]) {
						selected = true;
					}
				}
			}
			if (selected == true) { // if the agreement level is acceptable, add 1 to the matrix
				int row = -1;
				for (int j = 0; j < names1.size(); j++) {
					if (entries1.get(i).equals(names1.get(j))) {
						row = j;
					}
				}
				int col = -1;
				for (int j = 0; j < names2.size(); j++) {
					if (entries2.get(i).equals(names2.get(j))) {
						row = j;
					}
				}
				mat[row][col] = mat[row][col] + 1; // populate matrix
				edgelist.addEdge(new Edge(names1.get(i), names2.get(i), 1)); //populate edgelist
			}
		}
		String[] rownames = new String[names1.size()]; // cast row names from array list to array
		rownames = names1.toArray(rownames);
		String[] colnames = new String[names2.size()]; // cast column names from array list to array
		colnames = names2.toArray(colnames);
		Matrix matrix = new Matrix(mat, rownames, colnames); // assemble the Matrix object with labels
		Network network = new Network(matrix, edgelist, 2);  // wrap matrix and edgelist in a network object
		return(network);
		// TODO: should agreement = 0 result in negative edge weights?
	}
	
	/**
	 * This function accepts a list of statements that should be included in the network export, 
	 * retrieves their actual contents from the database, and creates and returns a one-mode 
	 * network matrix (co-occurrence/congruence or conflict matrix) based on the statement data.
	 * 
	 * @param statements	An array list of SidebarStatement objects that should be included in the export.
	 * @param variable1		The name of the variable for which the new matrix should be created (e.g., actors).
	 * @param variable2		The name of the variable via which the new matrix should be aggregated (e.g., concepts).
	 * @param qualifier		The name of the agreement variable via which an edge should be established.
	 * @param selection		The levels of the variable that should be taken into account during export.
	 * @param type			A string with with type of one-mode matrix to be created. Can have values "congruence" or "conflict").
	 * @return				A network object with a one-mode network.
	 */
	public Network oneModeNetwork(ArrayList<SidebarStatement> statements, String variable1, String variable2, String qualifier, int[] selection, String type) {
		ArrayList<String> names1 = new ArrayList<String>(); // unique row labels
		ArrayList<String> names2 = new ArrayList<String>(); // unique column labels
		for (int i = 0; i < statements.size(); i++) { // retrieve the row and column names from database
			int statementId = statements.get(i).getStatementId();
			String name1 = Dna.dna.db.getVariableStringEntry(statementId, variable1);
			if (!names1.contains(name1)) {
				names1.add(name1);
			}
			String name2 = Dna.dna.db.getVariableStringEntry(statementId, variable2);
			if (!names2.contains(name2)) {
				names2.add(name2);
			}
		}
		double[][] cooc = new double[names1.size()][names1.size()];
		for (int i = 0; i < selection.length; i++) { // compute one-mode projections for each agreement level, then add up
			int[] currentselection = new int[] { selection[i] };
			double[][] mat = affiliation(statements, variable1, variable2, qualifier, currentselection).getMatrix().getMatrix();
			mat = multiply(mat, transpose(mat));
			cooc = add(cooc, mat);
		}
		String[] labels = new String[names1.size()]; // cast row names from array list to array
		labels = names1.toArray(labels);
		Matrix matrix = new Matrix(cooc, labels, labels);
		Network network = new Network(matrix, 1);
		return network;
		// TODO: take into account the type variable inside this function to create conflict networks
		// TODO: allow for concept congruence networks; i.e., multiply numbers of nodes by qualifier selection size
		// TODO: add normalization options (divide, subtract, Jaccard, cosine similarity)
		// TODO: consider not only matching agreement levels, but using distance measure, e.g., intensities 2 and 5 have a distance of 3.
	}

	/**
	 * This function accepts a list of statements that should be included in the relational event export, 
	 * and it exports the variables of all statements to a CSV file, along with the statement ID and a 
	 * date/time stamp. There is one statement per row, and the number of columns is the number of variables 
	 * present in the statement type.
	 * 
	 * @param statements	An array list of SidebarStatement objects (of the same statement type) that should be exported.
	 * @param fileName		String with the file name of the CSV file to which the event list will be exported.
	 */
	public void releventCSV(ArrayList<SidebarStatement> statements, String fileName) {
		String key, value;
		int statementId;
		Date d;
		SimpleDateFormat dateFormat;
		String statementType = statements.get(0).getType();
		for (int i = 0; i < statements.size(); i++) {
			if (!statements.get(i).getType().equals(statementType)) {
				throw new IllegalArgumentException("More than one statement type was selected. Cannot export to a spreadsheet!");
			}
		}
		HashMap<String, String> variables = Dna.dna.db.getVariables(statementType);
		Iterator<String> keyIterator;
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF8"));
			keyIterator = variables.keySet().iterator();
			out.write("\"ID\";\"time\"");
			while (keyIterator.hasNext()){
				out.write(";\"" + keyIterator.next() + "\"");
			}
			for (int i = 0; i < statements.size(); i++) {
				out.newLine();
				statementId = statements.get(i).getStatementId();
				out.write(statementId);
				d = statements.get(i).getDate();
				dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				out.write(";" + dateFormat.format(d));
				keyIterator = variables.keySet().iterator();
				while (keyIterator.hasNext()){
					key = keyIterator.next();
					value = variables.get(key);
					if (value.equals("short text") || value.equals("long text")) {
						out.write(";" + Dna.dna.db.getVariableStringEntry(statementId, key).replaceAll(";", ","));
					} else if (value.equals("boolean") || value.equals("integer")) {
						out.write(";" + Dna.dna.db.getVariableIntEntry(statementId, key));
					}
				}
			}
			out.close();
			System.out.println("File has been exported to \"" + fileName + "\".");
		} catch (IOException e) {
			System.err.println("Error while saving CSV file: " + e);
		}
	}
	
	/**
	 * Export network to a CSV matrix file.
	 * 
	 * @param network  The input Network object.
	 * @param outfile  The path and file name of the target CSV file.
	 */
	public void exportCSV (Network network, String outfile) {
		int nr = network.getMatrix().rownames.length;
		int nc = network.getMatrix().colnames.length;
		String[] rn = network.getMatrix().rownames;
		String[] cn = network.getMatrix().colnames;
		double[][] mat = network.getMatrix().getMatrix();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("\"\"");
			for (int i = 0; i < nc; i++) {
				out.write(";\"" + cn[i] + "\"");
			}
			for (int i = 0; i < nr; i++) {
				out.newLine();
				out.write("\"" + rn[i] + "\"");
				for (int j = 0; j < nc; j++) {
					out.write(";" + String.format(new Locale("en"), "%.6f", mat[i][j]));
				}
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while saving CSV matrix file.");
		}
	}

	/**
	 * Export network to a DL fullmatrix file for the software Ucinet.
	 * 
	 * @param network  The input Network object.
	 * @param outfile  The path and file name of the target .dl file.
	 */
	public void exportDL (Network network, String outfile) {
		int nr = network.getMatrix().rownames.length;
		int nc = network.getMatrix().colnames.length;
		String[] rn = network.getMatrix().rownames;
		String[] cn = network.getMatrix().colnames;
		double[][] mat = network.getMatrix().getMatrix();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("DL");
			out.newLine();
			if (network.getModes() == 1) {
				out.write("N=" + nr);
			} else if (network.getModes() == 2) {
				out.write("NR=" + nr + ", NC=" + nc);
			}
			out.newLine();
			out.write("FORMAT = FULLMATRIX DIAGONAL PRESENT");
			out.newLine();
			out.write("ROW LABELS:");
			for (int i = 0; i < nr; i++) {
				out.newLine();
				out.write("\"" + rn[i] + "\"");
			}
			out.newLine();
			out.write("ROW LABELS EMBEDDED");
			out.newLine();
			out.write("COLUMN LABELS:");
			for (int i = 0; i < nc; i++) {
				out.newLine();
				out.write("\"" + cn[i] + "\"");
			}
			out.newLine();
			out.write("COLUMN LABELS EMBEDDED");
			out.newLine();
			out.write("DATA:");
			for (int i = 0; i < nr; i++) {
				out.newLine();
				for (int j = 0; j < nc; j++) {
					out.write(" " + String.format(new Locale("en"), "%.6f", mat[i][j]));
				}
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while saving DL fullmatrix file.");
		}
	}

	/**
	 * Export filter for graphML files.
	 * 
	 * @param network  The input Network object.
	 * @param outfile  The path and file name of the target .graphML file.
	 */
	private void exportGraphML(Network network, String outfile) {
		ArrayList<Edge> edges = network.getEdgelist().getEdgelist();
		int modes = network.getModes();
		String[] rn = network.getEdgelist().getSources();
		String[] cn = network.getEdgelist().getTargets();
		int nr = rn.length;
		int nc = cn.length;
		int numVertices = nr;
		if (modes == 1) {
			numVertices = nr;
		} else if (modes == 2) {
			numVertices = nr + nc;
		}
		int numEdges = edges.size();
		String[] labels = null;
		if (modes == 1) {
			labels = rn;
		} else if (modes == 2) {
			labels = Stream.concat(Arrays.stream(rn), Arrays.stream(cn)).toArray(String[]::new);
		}
		
		Namespace xmlns = Namespace.getNamespace("http://graphml.graphdrawing.org/xmlns");
		Element graphml = new Element("graphml", xmlns);
		Namespace visone = Namespace.getNamespace("visone", "http://visone.info/xmlns");
		graphml.addNamespaceDeclaration(visone);
		Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		graphml.addNamespaceDeclaration(xsi);
		Namespace yNs = Namespace.getNamespace("y", "http://www.yworks.com/xml/graphml");
		graphml.addNamespaceDeclaration(yNs);
		Attribute attSchema = new Attribute("schemaLocation", "http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd ", xsi);
		graphml.setAttribute(attSchema);
		Document document = new Document(graphml);
		
		Comment dataSchema = new Comment(" data schema ");
		graphml.addContent(dataSchema);
		
		Element keyVisoneNode = new Element("key", xmlns);
		keyVisoneNode.setAttribute(new Attribute("for", "node"));
		keyVisoneNode.setAttribute(new Attribute("id", "d0"));
		keyVisoneNode.setAttribute(new Attribute("yfiles.type", "nodegraphics"));
		graphml.addContent(keyVisoneNode);

		Element keyVisoneEdge = new Element("key", xmlns);
		keyVisoneEdge.setAttribute(new Attribute("for", "edge"));
		keyVisoneEdge.setAttribute(new Attribute("id", "e0"));
		keyVisoneEdge.setAttribute(new Attribute("yfiles.type", "edgegraphics"));
		graphml.addContent(keyVisoneEdge);

		Element keyVisoneGraph = new Element("key", xmlns);
		keyVisoneGraph.setAttribute(new Attribute("for", "graph"));
		keyVisoneGraph.setAttribute(new Attribute("id", "prop"));
		keyVisoneGraph.setAttribute(new Attribute("visone.type", "properties"));
		graphml.addContent(keyVisoneGraph);
		
		Element keyId = new Element("key", xmlns);
		keyId.setAttribute(new Attribute("id", "id"));
		keyId.setAttribute(new Attribute("for", "node"));
		keyId.setAttribute(new Attribute("attr.name", "id"));
		keyId.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyId);
		
		Element keyClass = new Element("key", xmlns);
		keyClass.setAttribute(new Attribute("id", "class"));
		keyClass.setAttribute(new Attribute("for", "node"));
		keyClass.setAttribute(new Attribute("attr.name", "class"));
		keyClass.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyClass);
		
		Element keyFrequency = new Element("key", xmlns);
		keyFrequency.setAttribute(new Attribute("id", "statementFrequency"));
		keyFrequency.setAttribute(new Attribute("for", "node"));
		keyFrequency.setAttribute(new Attribute("attr.name", "statementFrequency"));
		keyFrequency.setAttribute(new Attribute("attr.type", "int"));
		graphml.addContent(keyFrequency);

		Element keyWeight = new Element("key", xmlns);
		keyWeight.setAttribute(new Attribute("id", "weight"));
		keyWeight.setAttribute(new Attribute("for", "edge"));
		keyWeight.setAttribute(new Attribute("attr.name", "weight"));
		keyWeight.setAttribute(new Attribute("attr.type", "double"));
		graphml.addContent(keyWeight);
		
		Element graphElement = new Element("graph", xmlns);
		graphElement.setAttribute(new Attribute("edgedefault", "undirected"));
		
		graphElement.setAttribute(new Attribute("id", "G"));
		graphElement.setAttribute(new Attribute("parse.edges", String.valueOf(numEdges)));
		graphElement.setAttribute(new Attribute("parse.nodes", String.valueOf(numVertices)));
		graphElement.setAttribute(new Attribute("parse.order", "free"));
		Element properties = new Element("data", xmlns);
		properties.setAttribute(new Attribute("key", "prop"));
		Element labelAttribute = new Element("labelAttribute", visone);
		labelAttribute.setAttribute("edgeLabel", "weight");
		labelAttribute.setAttribute("nodeLabel", "id");
		properties.addContent(labelAttribute);
		graphElement.addContent(properties);
		
		Comment nodes = new Comment(" nodes ");
		graphElement.addContent(nodes);
		
		for (int i = 0; i < labels.length; i++) {
			Element node = new Element("node", xmlns);
			node.setAttribute(new Attribute("id", labels[i]));
			
			Element id = new Element("data", xmlns);
			id.setAttribute(new Attribute("key", "id"));
			id.setText(labels[i]);
			node.addContent(id);
			
			String hex;
			String type;
			String shapeString;
			if (i > nr) {
				hex = "#00FF00";  // green
				type = "mode2";
				shapeString = "ellipse";
			} else {
				hex = "#3399FF";  // light blue
				type = "mode1";
				shapeString = "roundrectangle";
			}
			
			Element vClass = new Element("data", xmlns);
			vClass.setAttribute(new Attribute("key", "class"));
			vClass.setText(type);
			node.addContent(vClass);
			
			Element vis = new Element("data", xmlns);
			vis.setAttribute(new Attribute("key", "d0"));
			Element visoneShapeNode = new Element("shapeNode", visone);
			Element yShapeNode = new Element("ShapeNode", yNs);
			Element geometry = new Element("Geometry", yNs);
			geometry.setAttribute(new Attribute("height", "20.0"));
			geometry.setAttribute(new Attribute("width", "20.0"));
			geometry.setAttribute(new Attribute("x", String.valueOf(Math.random()*800)));
			geometry.setAttribute(new Attribute("y", String.valueOf(Math.random()*600)));
			yShapeNode.addContent(geometry);
			Element fill = new Element("Fill", yNs);
			fill.setAttribute(new Attribute("color", hex));
			fill.setAttribute(new Attribute("transparent", "false"));
			yShapeNode.addContent(fill);
			Element borderStyle = new Element("BorderStyle", yNs);
			borderStyle.setAttribute(new Attribute("color", "#000000"));
			borderStyle.setAttribute(new Attribute("type", "line"));
			borderStyle.setAttribute(new Attribute("width", "1.0"));
			yShapeNode.addContent(borderStyle);
			
			Element nodeLabel = new Element("NodeLabel", yNs);
			nodeLabel.setAttribute(new Attribute("alignment", "center"));
			nodeLabel.setAttribute(new Attribute("autoSizePolicy", "content"));
			nodeLabel.setAttribute(new Attribute("backgroundColor", "#FFFFFF"));
			nodeLabel.setAttribute(new Attribute("fontFamily", "Dialog"));
			nodeLabel.setAttribute(new Attribute("fontSize", "12"));
			nodeLabel.setAttribute(new Attribute("fontStyle", "plain"));
			nodeLabel.setAttribute(new Attribute("hasLineColor", "false"));
			nodeLabel.setAttribute(new Attribute("height", "19.0"));
			nodeLabel.setAttribute(new Attribute("modelName", "eight_pos"));
			nodeLabel.setAttribute(new Attribute("modelPosition", "n"));
			nodeLabel.setAttribute(new Attribute("textColor", "#000000"));
			nodeLabel.setAttribute(new Attribute("visible", "true"));
			nodeLabel.setText(labels[i]);
			yShapeNode.addContent(nodeLabel);
			
			Element shape = new Element("Shape", yNs);
			shape.setAttribute(new Attribute("type", shapeString));
			yShapeNode.addContent(shape);
			visoneShapeNode.addContent(yShapeNode);
			vis.addContent(visoneShapeNode);
			node.addContent(vis);
			
			graphElement.addContent(node);
		}
		
		Comment edgesComment = new Comment(" edges ");
		graphElement.addContent(edgesComment);
		
		for (int i = 0; i < edges.size(); i++) {
			Element edge = new Element("edge", xmlns);
			edge.setAttribute(new Attribute("source", edges.get(i).getSource()));
			edge.setAttribute(new Attribute("target", edges.get(i).getTarget()));
			Element weight = new Element("data", xmlns);
			weight.setAttribute(new Attribute("key", "weight"));
			weight.setText(String.valueOf(edges.get(i).getWeight()));
			edge.addContent(weight);
			
			Element visEdge = new Element("data", xmlns);
			visEdge.setAttribute("key", "e0");
			Element visPolyLineEdge = new Element("polyLineEdge", visone);
			Element yPolyLineEdge = new Element("PolyLineEdge", yNs);
			Element yLineStyle = new Element("LineStyle", yNs);
			String col;
			if (edges.get(i).getWeight() > 0) {
				col = "#00ff00";
			} else if (edges.get(i).getWeight() < 0) {
				col = "#ff0000";
			} else {
				col = "#000000";
			}
			yLineStyle.setAttribute("color", col);
			yLineStyle.setAttribute(new Attribute("type", "line"));
			yLineStyle.setAttribute(new Attribute("width", "2.0"));
			yPolyLineEdge.addContent(yLineStyle);
			visPolyLineEdge.addContent(yPolyLineEdge);
			visEdge.addContent(visPolyLineEdge);
			edge.addContent(visEdge);
			
			graphElement.addContent(edge);
		}
		
		graphml.addContent(graphElement);
		
		File dnaFile = new File (outfile);
		try {
			FileOutputStream outStream = new FileOutputStream(dnaFile);
			XMLOutputter outToFile = new XMLOutputter();
			Format format = Format.getPrettyFormat();
			format.setEncoding("utf-8");
			outToFile.setFormat(format);
			outToFile.output(document, outStream);
			outStream.flush();
			outStream.close();
		} catch (IOException e) {
			System.err.println("Cannot save \"" + dnaFile + "\":" + e.getMessage());
			JOptionPane.showMessageDialog(Dna.dna.gui, "Error while saving the file!\n" + e.getStackTrace());
		}
	}
}
