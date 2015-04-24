package dna;

import javax.swing.ImageIcon;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

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

/**
 * @author philip
 *
 */
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
		
		// constructor when only the matrix has been computed: also convert to edge list
		public Network(Matrix matrix) {
			this.matrix = matrix;
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
		public Network(Edgelist edgelist) {
			this.edgelist = edgelist;
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
		public Network(Matrix matrix, Edgelist edgelist) {
			this.matrix = matrix;
			this.edgelist = edgelist;
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
		Network network = new Network(matrix, edgelist);  // wrap matrix and edgelist in a network object
		return(network);
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
		Network network = new Network(matrix);
		return network;
		// TODO: take into account the type variable inside this function to create conflict networks
		// TODO: allow for concept congruence networks; i.e., multiply numbers of nodes by qualifier selection size
		// TODO: add normalization options (divide, subtract, Jaccard, cosine similarity)
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
}