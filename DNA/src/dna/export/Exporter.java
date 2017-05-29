package dna.export;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import dna.Dna;
import dna.SqlConnection;
import dna.dataStructures.AttributeVector;
import dna.dataStructures.Data;
import dna.dataStructures.Document;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementType;
import dna.renderer.StatementTypeComboBoxModel;
import dna.renderer.StatementTypeComboBoxRenderer;

/**
 * @author Philip Leifeld
 * 
 * Contains everything needed to export networks, including GUI and network export algorithms.
 */
@SuppressWarnings("serial")
public class Exporter extends JDialog {
	
	// objects for GUI
	JCheckBox helpBox;
	JButton exportButton;
	JComboBox<String> networkModesBox, fileFormatBox, var1Box, var2Box, qualifierBox, aggregationBox, normalizationBox, 
			isolatesBox, duplicatesBox, timeWindowBox;
	JComboBox<StatementType> statementTypeBox;
	JSpinner startSpinner, stopSpinner, timeWindowSpinner;
	JList<String> excludeVariableList, excludeValueList;
	HashMap<String, ArrayList<String>> excludeValues;
	ArrayList<String> excludeAuthor, excludeSource, excludeSection, excludeType;
	JTextArea excludePreviewArea;
	Color fg;
	
	// objects for R calls
	String dbfile;
	SqlConnection sql;
	Data data;
	ArrayList<Statement> filteredStatements;
	Matrix matrix;
	ArrayList<Matrix> timeWindowMatrices;
	AttributeVector[] attributes;
	Object[] eventListColumnsR;
	String[] columnNames, columnTypes;

	/**
	 * Constructor for external R calls. Load and prepare data for export.
	 */
	public Exporter(String dbtype, String dbfile, String login, String password, boolean verbose) {
		this.dbfile = dbfile;
		this.sql = new SqlConnection(dbtype, this.dbfile, login, password);
		this.data = sql.getAllData();
		this.filteredStatements = new ArrayList<Statement>();
		for (int i = 0; i < data.getStatements().size(); i++) {
			filteredStatements.add(data.getStatements().get(i));
		}
		if (verbose == true) {
			String statementString = " statements and ";
			if (this.data.getStatements().size() == 1) {
				statementString = " statement and ";
			}
			String documentString = " documents.";
			if (this.data.getDocuments().size() == 1) {
				documentString = " document.";
			}
			System.out.println("Data loaded: " + data.getStatements().size() + statementString + data.getDocuments().size() + documentString);
		}
	}
	
	/**
	 * A function for printing details about the dataset. Used by rDNA.
	 */
	public void rShow() {
		String statementString = " statements in ";
		if (this.data.getStatements().size() == 1) {
			statementString = " statement in ";
		}
		String documentString = " documents";
		if (this.data.getDocuments().size() == 1) {
			documentString = " document";
		}
		System.out.println("DNA database: " + this.dbfile);
		System.out.println(data.getStatements().size() + statementString + data.getDocuments().size() + documentString);
		System.out.print("Statement types: ");
		for (int i = 0; i < data.getStatementTypes().size(); i++) {
			System.out.print(data.getStatementTypes().get(i).getLabel());
			if (i < data.getStatementTypes().size() - 1) {
				System.out.print(", ");
			}
		}
		System.out.print("\n");
	}
	
	/**
	 * Constructor for GUI. Opens an Exporter window, which displays the GUI for exporting network data.
	 */
	public Exporter() {
		this.setTitle("Export data");
		this.setModal(true);
		ImageIcon networkIcon = new ImageIcon(getClass().getResource("/icons/chart_organisation.png"));
		this.setIconImage(networkIcon.getImage());
		this.setLayout(new BorderLayout());
		
		JPanel settingsPanel = new JPanel();
		GridBagLayout g = new GridBagLayout();
		settingsPanel.setLayout(g);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		
		// first row of options
		JLabel networkModesLabel = new JLabel("Type of network");
		String networkModesToolTip = "<html><p width=\"500\">Select the type of network to export. A <strong>one-mode "
				+ "network</strong> has the same nodes in the rows and columns of the resulting matrix (e.g., "
				+ "organizations x organizations). A <strong>two-mode network</strong> has different sets of nodes in the "
				+ "rows and columns of the resulting matrix (e.g., organizations x concepts). An <strong>event list</strong> "
				+ "is a time-stamped list of edges, with each row of the resulting table containing all variables of a "
				+ "statement including the time.</p></html>";
		networkModesLabel.setToolTipText(networkModesToolTip);
		settingsPanel.add(networkModesLabel, gbc);
		
		gbc.gridx = 1;
		JLabel statementTypeLabel = new JLabel("Statement type");
		String statementTypeToolTip = 
				"<html><p width=\"500\">A network is constructed based on the variables from a specific statement type. "
				+ "Select here which statement type to use.</p></html>";
		statementTypeLabel.setToolTipText(statementTypeToolTip);
		settingsPanel.add(statementTypeLabel, gbc);
		
		gbc.gridx = 2;
		JLabel fileFormatLabel = new JLabel("File format");
		String fileFormatToolTip = "<html><p width=\"500\">Select the file format in which the resulting network is saved. "
				+ "<strong>CSV</strong> files can be read by most programs, including spreadsheet software like Microsoft "
				+ "Excel. <strong>DL</strong> files are plain text files which can be imported by the network analysis "
				+ "software Ucinet. <strong>GRAPHML</strong> files can be opened using the network visualization software "
				+ "visone. Note that the flavor of GRAPHML used here is specific to visone.</p></html>";
		fileFormatLabel.setToolTipText(fileFormatToolTip);
		settingsPanel.add(fileFormatLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		String[] networkModesItems = new String[] {"Two-mode network", "One-mode network", "Event list"};
		networkModesBox = new JComboBox<>(networkModesItems);
		networkModesBox.setToolTipText(networkModesToolTip);
		settingsPanel.add(networkModesBox, gbc);
		networkModesBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selected = (String) networkModesBox.getSelectedItem();
				if (selected.equals("Two-mode network")) {
					
					String fileFormatBackup = (String) fileFormatBox.getSelectedItem();
					fileFormatBox.removeAllItems();
					fileFormatBox.addItem(".csv");
					fileFormatBox.addItem(".dl");
					fileFormatBox.addItem(".graphml");
					fileFormatBox.setSelectedItem(fileFormatBackup);
					
					String aggregationBackup = (String) aggregationBox.getSelectedItem();
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("combine");
					aggregationBox.addItem("subtract");
					if (aggregationBackup.equals("ignore")) {
						aggregationBox.setSelectedItem("ignore");
					} else if (aggregationBackup.equals("combine")) {
						aggregationBox.setSelectedItem("combine");
					} else if (aggregationBackup.equals("subtract")) {
						aggregationBox.setSelectedItem("subtract");
					}
					
					normalizationBox.removeAllItems();
					normalizationBox.addItem("no");
					normalizationBox.addItem("activity");
					normalizationBox.addItem("prominence");
					
					isolatesBox.removeAllItems();
					isolatesBox.addItem("only current nodes");
					isolatesBox.addItem("include isolates");
					
					/*
					temporalBox.removeAllItems();
					temporalBox.addItem("across date range");
					*/
				} else if (selected.equals("One-mode network")) {
					
					String fileFormatBackup = (String) fileFormatBox.getSelectedItem();
					fileFormatBox.removeAllItems();
					fileFormatBox.addItem(".csv");
					fileFormatBox.addItem(".dl");
					fileFormatBox.addItem(".graphml");
					fileFormatBox.setSelectedItem(fileFormatBackup);

					String aggregationBackup = (String) aggregationBox.getSelectedItem();
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("congruence");
					aggregationBox.addItem("conflict");
					aggregationBox.addItem("subtract");
					if (aggregationBackup.equals("ignore")) {
						aggregationBox.setSelectedItem("ignore");
					} else if (aggregationBackup.equals("congruence")) {
						aggregationBox.setSelectedItem("congruence");
					} else if (aggregationBackup.equals("conflict")) {
						aggregationBox.setSelectedItem("conflict");
					} else if (aggregationBackup.equals("subtract")) {
						aggregationBox.setSelectedItem("subtract");
					}
					
					normalizationBox.removeAllItems();
					normalizationBox.addItem("no");
					normalizationBox.addItem("average activity");
					normalizationBox.addItem("Jaccard");
					normalizationBox.addItem("cosine");
					
					isolatesBox.removeAllItems();
					isolatesBox.addItem("only current nodes");
					isolatesBox.addItem("include isolates");
				} else if (selected.equals("Event list")) {
					
					fileFormatBox.removeAllItems();
					fileFormatBox.addItem(".csv");
					
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					
					normalizationBox.removeAllItems();
					normalizationBox.addItem("no");
					
					isolatesBox.removeAllItems();
					isolatesBox.addItem("only current nodes");
				}
			}
			
		});

		gbc.gridx = 1;
		StatementTypeComboBoxRenderer cbrenderer = new StatementTypeComboBoxRenderer();
		StatementTypeComboBoxModel model = new StatementTypeComboBoxModel();
		statementTypeBox = new JComboBox<>(model);
		statementTypeBox.setRenderer(cbrenderer);
		statementTypeBox.setToolTipText(statementTypeToolTip);
		String[] var1Items = null, var2Items = null;
		for (int i = 0; i < Dna.data.getStatementTypes().size(); i++) {
			String[] vars = getVariablesList(Dna.data.getStatementTypes().get(i), false, true, false, false);
			if (vars.length > 1) {
				statementTypeBox.setSelectedItem(Dna.data.getStatementTypes().get(i));
				var1Items = vars;
				var2Items = vars;
				break;
			}
		}
		if (var1Items == null) {
			System.err.println("No statement type with more than one short text variable found!");
		}
		settingsPanel.add(statementTypeBox, gbc);
		final int HEIGHT = (int) statementTypeBox.getPreferredSize().getHeight();
		final int WIDTH = 200;
		Dimension d = new Dimension(WIDTH, HEIGHT);
		networkModesBox.setPreferredSize(d);
		statementTypeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StatementType selected = (StatementType) statementTypeBox.getSelectedItem();
				String[] varItems = getVariablesList(selected, false, true, false, false);
				String[] qualifierItems = getVariablesList(selected, false, false, true, true);
				var1Box.removeAllItems();
				var2Box.removeAllItems();
				qualifierBox.removeAllItems();
				if (varItems.length > 0) {
					for (int i = 0; i < varItems.length; i++) {
						var1Box.addItem(varItems[i]);
						var2Box.addItem(varItems[i]);
					}
				}
				if (qualifierItems.length > 0) {
					for (int i = 0; i < qualifierItems.length; i++) {
						qualifierBox.addItem(qualifierItems[i]);
					}
				}
				populateExcludeVariableList();
				excludeVariableList.setSelectedIndex(0);
				excludePreviewArea.setText("");
			}
		});
		
		gbc.gridx = 2;
		String[] fileFormatItems = new String[] {".csv", ".dl", ".graphml"};
		fileFormatBox = new JComboBox<>(fileFormatItems);
		fileFormatBox.setToolTipText(fileFormatToolTip);
		settingsPanel.add(fileFormatBox, gbc);
		fileFormatBox.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		statementTypeBox.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		// second row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 2;
		JLabel var1Label = new JLabel("Variable 1");

		String var1ToolTip = "<html><p width=\"500\">In a one-mode network, the first variable denotes the node class used "
				+ "both for the rows and columns of the matrix. For example, select the variable for organizations in order "
				+ "to export an organization x organization network. In a two-mode network, the first variable denotes the "
				+ "node class for the rows. For example, select the variable for organizations here if you want to export "
				+ "an organization x concept two-mode network.</p></html>";
		var1Label.setToolTipText(var1ToolTip);
		settingsPanel.add(var1Label, gbc);
		
		gbc.gridx = 1;
		JLabel var2Label = new JLabel("Variable 2");
		String var2ToolTip = "<html><p width=\"500\">In a one-mode network, the second variable denotes the variable through "
				+ "which the edges are aggregated. For example, if you export a one-mode network of organizations and the "
				+ "edge weight that connects any two organizations should denote the two organizations' number of joint "
				+ "concepts, then the second variable should denote the concepts. In a two-mode network, the second variable "
				+ "denotes the node class used for the columns of the resulting network matrix. For example, one would select "
				+ "the variable for concepts here in order to export an organization x concept network.</p></html>";
		var2Label.setToolTipText(var2ToolTip);
		settingsPanel.add(var2Label, gbc);
		
		gbc.gridx = 2;
		JLabel qualifierLabel = new JLabel("Qualifier");
		String qualifierToolTip = "<html><p width=\"500\">The qualifier is a binary or integer variable which indicates "
				+ "different qualities or levels of association between variable 1 and variable 2. If a binary qualifier "
				+ "variable is used, this amounts to positive versus negative relations or agreement versus disagreement. "
				+ "For example, in an organization x concept two-mode network, a qualifier variable could indicate whether "
				+ "an organization supports or rejects a concept. The qualifier would indicate support or rejection in this "
				+ "case. The qualifier is also preserved in a one-mode network. For example, in an organization x organization "
				+ "network, any two organizations can be connected if they either co-support or co-reject the same concept. "
				+ "In other words, a one-mode projection of the support two-mode network and a one-mode projection of the "
				+ "rejection two-mode network are stacked and added in order to obtain a congruence network. If an integer "
				+ "(non-binary) qualifier variable is used, this indicates different intensities in a two-mode network, for "
				+ "example the extent of support (in the positive range) or rejection (in the negative range) of a concept by "
				+ "an organization. In a one-mode network, differences in these levels of intensity are used as edge weights; "
				+ "the description under Qualifier aggregation contains more details on this.</p></html>";
		qualifierLabel.setToolTipText(qualifierToolTip);
		settingsPanel.add(qualifierLabel, gbc);
		
		gbc.gridx = 3;
		JLabel aggregationLabel = new JLabel("Qualifier aggregation");
		String aggregationToolTip = "<html><p width=\"500\">The choices differ between one-mode and two-mode networks. <strong>"
				+ "ignore</strong> is available in both cases and means that the agreement qualifier variable is ignored, i.e., "
				+ "the network is constructed as if all values on the qualifier variable were the same. In the two-mode network "
				+ "case, <strong>subtract</strong> means that negative absolute values on the qualifier variable are subtracted from "
				+ "positive values (if an integer qualifier is selected) or that 0 values are subtracted from 1 values (if a "
				+ "binary qualifier variable is selected). For example, if an organization mentions a concept two times in a "
				+ "positive way and three times in a negative way, there will be an edge weight of -1 between the organization "
				+ "and the concept. In the one-mode case, <strong>subtract</strong> means that a congruence network and a "
				+ "conflict network are created separately and then the (potentially normalized) conflict network ties are "
				+ "subtracted from the (potentially normalized) congruence network ties. For example, if two organizations A "
				+ "and B disagree over 3 concepts and agree with regard to 5 concepts, they have an edge weight of 2. <strong>"
				+ "combine</strong> is only available for two-mode networks. In contrast to the subtract option, it means that "
				+ "the values of the qualifier are treated as qualitative categories. In this case, DNA creates all possible "
				+ "combinations of edges (e.g., in the binary case, these are support, rejection, and both/mixed/ambiguous). "
				+ "Integer values are then used as edge weights, as in a multiplex network. E.g., 1 represents support, 2 "
				+ "represents rejection, and 3 represents both. With an integer variable, this may become more complex. In "
				+ "one-mode networks, <strong>congruence</strong> means that similarity or matches on the qualifier variable "
				+ "are counted in order to construct an edge. With a binary variable, matches are used. For example, if "
				+ "organizations A and B both co-support or both co-reject concept C, they are connected, and the number of "
				+ "co-supported and co-rejected concepts is used as the edge weight. With an integer qualifier variable, "
				+ "the inverse of the absolute distance plus one (i.e., the proximity) is used instead of a match. In one-mode "
				+ "networks, <strong>conflict</strong> constructs the network by recording disagreements between actors, rather "
				+ "than agreements, as in the congruence case. With a binary qualifier, this means that organizations A and B "
				+ "are connected if one of them supports a concept and the other one rejects the concept. With an integer "
				+ "qualifier, the absolute distance is used instead.</p></html>";
		aggregationLabel.setToolTipText(aggregationToolTip);
		settingsPanel.add(aggregationLabel, gbc);
		
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 3;
		var1Box = new JComboBox<>(var1Items);
		var1Box.setToolTipText(var1ToolTip);
		settingsPanel.add(var1Box, gbc);
		int HEIGHT2 = (int) var1Box.getPreferredSize().getHeight();
		var1Box.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		var2Box = new JComboBox<>(var2Items);
		var2Box.setToolTipText(var2ToolTip);
		var2Box.setSelectedIndex(1);
		settingsPanel.add(var2Box, gbc);
		var2Box.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		fg = var2Box.getForeground();
		ActionListener varActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (var1Box.getItemCount() < 1 || var2Box.getItemCount() < 1) {
					var1Box.setBorder(null);
					var2Box.setBorder(null);
				} else if (var1Box.getSelectedItem().equals(var2Box.getSelectedItem())) {
					var1Box.setBorder(BorderFactory.createLineBorder(Color.RED));
					var2Box.setBorder(BorderFactory.createLineBorder(Color.RED));
				} else {
					var1Box.setBorder(null);
					var2Box.setBorder(null);
				}
			}
		};
		var1Box.addActionListener(varActionListener);
		var2Box.addActionListener(varActionListener);
		
		gbc.gridx = 2;
		String[] qualifierItems = getVariablesList((StatementType) statementTypeBox.getSelectedItem(), false, false, true, true);
		qualifierBox = new JComboBox<>(qualifierItems);
		qualifierBox.setToolTipText(qualifierToolTip);
		settingsPanel.add(qualifierBox, gbc);
		qualifierBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		qualifierBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (qualifierBox.getItemCount() == 0) {
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
				} else if (networkModesBox.getSelectedItem().equals("Two-mode network")) {
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("combine");
					aggregationBox.addItem("subtract");
				} else if (networkModesBox.getSelectedItem().equals("One-mode network")) {
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("congruence");
					aggregationBox.addItem("conflict");
					aggregationBox.addItem("subtract");
				} else if (networkModesBox.getSelectedItem().equals("Event list")) {
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
				}
			}
		});

		gbc.gridx = 3;
		String[] aggregationItems = new String[] {"ignore", "combine", "subtract"};
		aggregationBox = new JComboBox<>(aggregationItems);
		aggregationBox.setToolTipText(aggregationToolTip);
		settingsPanel.add(aggregationBox, gbc);
		aggregationBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		aggregationBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (aggregationBox.getItemCount() > 0) {
					if (aggregationBox.getSelectedItem().equals("combine")) {
						normalizationBox.removeAllItems();
						normalizationBox.addItem("no");
					} else {
						if (networkModesBox.getSelectedItem().equals("Two-mode network")) {
							normalizationBox.removeAllItems();
							normalizationBox.addItem("no");
							normalizationBox.addItem("activity");
							normalizationBox.addItem("prominence");
						} else if (networkModesBox.getSelectedItem().equals("One-mode network")) {
							normalizationBox.removeAllItems();
							normalizationBox.addItem("no");
							normalizationBox.addItem("average activity");
							normalizationBox.addItem("Jaccard");
							normalizationBox.addItem("cosine");
						} else if (networkModesBox.getSelectedItem().equals("Event list")) {
							normalizationBox.removeAllItems();
							normalizationBox.addItem("no");
						}
					}
				}
			}
		});
		
		
		// third row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 4;
		JLabel normalizationLabel = new JLabel("Normalization");
		String normalizationToolTip = "<html><p width=\"500\">Normalization of edge weights serves to cancel out the effect "
				+ "that some nodes are more active than others. For example, in an organization x organization one-mode "
				+ "network aggregated through a concept variable, some organizations with an institutional mandate end "
				+ "up in very central locations of the network because they have agreement with many other actors due to "
				+ "their role rather than their actual opinion, i.e., because their office requires them to reveal their "
				+ "preferences on many concepts. To cancel out this activity effect, one can divide the edge weight "
				+ "between two organizations by a function of the two incident organizations' activity or frequency of "
				+ "making statements. This normalization procedure leaves us with a network of edge weights that reflect "
				+ "similarity in opinions without taking into account centrality or activity in any way. There are several "
				+ "ways to normalize a one-mode network. <strong>No normalization</strong> switches off normalization and "
				+ "is the default value. <strong>Average activity</strong> divides edge weights between first-variable "
				+ "nodes by the average number of different second-variable nodes they are adjacent with in a two-mode "
				+ "network. For example, if organization A makes statements about 20 different concepts and B makes "
				+ "statements about 60 difference concepts, the edge weight between A and B in the congruence network "
				+ "is divided by 40. To achieve a better scaling, all edge weights in the resulting normalized one-mode "
				+ "network matrix are scaled between 0 and 1. <strong>Jaccard similarity</strong> is a similarity measure "
				+ "with known normalizing properties as well. Rather than average activity, it divides the co-occurrence "
				+ "frequency by the activity count of both separate actors plus their joint activity. <strong>Cosine "
				+ "similarity </strong>is another similarity measure with normalizing properties. It divides edge weights "
				+ "by the product of the nodes' activity. Details on these normalization measures can be found in <emph>"
				+ "Leifeld, Philip (2017): Discourse Network Analysis: Policy Debates as Dynamic Networks. In: Oxford "
				+ "Handbook of Political Networks, edited by JN Victor, AH Montgomery, and MN Lubell, chapter 25, Oxford "
				+ "University Press.</emph> With two-mode networks, <strong>activity</strong> normalization and <strong>"
				+ "prominence</strong> normalization are available. They divide the edge weights through the activity of "
				+ "the node from the first or the second mode, respectively.</p></html>";
		normalizationLabel.setToolTipText(normalizationToolTip);
		settingsPanel.add(normalizationLabel, gbc);
		
		gbc.gridx = 1;
		JLabel isolatesLabel = new JLabel("Isolates");
		String isolatesToolTip = "<html><p width=\"500\">Often, one would like to export several time slices or networks "
				+ "for different qualifier values separately etc. and merge the resulting networks later on manually. "
				+ "In these situations, it is easier to merge multiple networks if they have the same matrix dimensions. "
				+ "However, this is usually not the case because the network export functions by default only include "
				+ "nodes that show at least minimal activity in the network that is exported. To achieve compatibility of "
				+ "the matrix dimensions anyway, it is possible to include all nodes of the selected variable(s) in the "
				+ "whole database, irrespective of time, qualifiers, and excluded values (but without any edge weights "
				+ "larger than 0, i.e., as isolates). This is meant by including isolates.</p></html>";
		isolatesLabel.setToolTipText(isolatesToolTip);
		settingsPanel.add(isolatesLabel, gbc);

		gbc.gridx = 2;
		JLabel duplicatesLabel = new JLabel("Duplicates");
		String duplicatesToolTip = "<html><p width=\"500\">By default, all statements are included in a network export. "
				+ "However, in many data sources, repeated identical statements within the same document are an "
				+ "artifact of the source material. For example, in a newspaper article, the number of times an actor "
				+ "is quoted with a statement may be a function of the journalist's agenda or the reporting style of "
				+ "the news media outlet, rather than the actor's deliberate attempt to speak multiple times. In cases "
				+ "like this it makes sense to count each statement only once per document. Alternatively, it may "
				+ "make sense to count statements only once per week, month, or year or in the whole time period if this "
				+ "artifact happens across documents (e.g., if a newspaper reprints interviews or reports on the same "
				+ "press release multiple times in different documents).</p></html>";
		duplicatesLabel.setToolTipText(duplicatesToolTip);
		settingsPanel.add(duplicatesLabel, gbc);

		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 5;
		String[] normalizationItems = new String[] {"no", "activity", "prominence"};
		normalizationBox = new JComboBox<>(normalizationItems);
		normalizationBox.setToolTipText(normalizationToolTip);
		settingsPanel.add(normalizationBox, gbc);
		normalizationBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		String[] isolatesItems = new String[] {"only current nodes", "include isolates"};
		isolatesBox = new JComboBox<>(isolatesItems);
		isolatesBox.setToolTipText(isolatesToolTip);
		settingsPanel.add(isolatesBox, gbc);
		isolatesBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));

		gbc.gridx = 2;
		String[] duplicatesItems = new String[] {"include all duplicates", "ignore per document", "ignore per calendar week", 
				"ignore per calendar month", "ignore per calendar year", "ignore across date range"};
		duplicatesBox = new JComboBox<>(duplicatesItems);
		duplicatesBox.setToolTipText(duplicatesToolTip);
		settingsPanel.add(duplicatesBox, gbc);
		duplicatesBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		// fourth row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 6;
		JLabel startLabel = new JLabel("Include from");
		String dateToolTip = "<html><p width=\"500\">All statements before the start date and time and after the stop date "
				+ "and time will be ignored during network export. This can be helpful for exporting multiple time slices "
				+ "and analyzing them separately. By default, the first and last date/time stamp in any statement found in "
				+ "the whole database are used, i.e., the default setting includes all statements.</p></html>";
		startLabel.setToolTipText(dateToolTip);
		settingsPanel.add(startLabel, gbc);
		
		gbc.gridx = 1;
		JLabel stopLabel = new JLabel("Include until");
		stopLabel.setToolTipText(dateToolTip);
		settingsPanel.add(stopLabel, gbc);

		gbc.gridx = 2;
		JLabel timeWindowLabel = new JLabel("Moving time window");
		String timeWindowToolTip = "<html><p width=\"500\">Create multiple overlapping time slices that are moved "
				+ "forward along the time axis. For example, if a time window size of 100 days is used and the time "
				+ "axis starts on a certain start date and ends on a certain end date, then the first time slice will "
				+ "comprise all statements between the start date and 100 days later. E.g., a one-mode network or a "
				+ "two-mode network for this time period is created. Then the time window is shifted by one time unit, "
				+ "and a new network is created for the second to the 101st day, and so forth until the end of the time "
				+ "period is reached. All time slices are saved as separate networks. If CSV files or DL files are "
				+ "exported, the networks will be saved to new files each time, with an iterator number added to the "
				+ "file name. If graphml files are used, the networks are saved inside a network collection. Instead "
				+ "of days, the user can select other time units by which the time window is shifted. However, the "
				+ "time slice is always moved forward by one time unit. To get mutually exclusive (i.e., "
				+ "non-overlapping) time slices, the user should select them manually from the output. For example, if "
				+ "100 days are used, one could select the first time slice, the 101st slice, the 201st time slice and "
				+ "so on. Instead of time units, it is also possible to use event time. This will create time slices "
				+ "of exactly 100 statement events, for example. However, it is possible that multiple events have "
				+ "identical time steps. In this case, the resulting network time slice is more inclusive and also "
				+ "contains those statements that happened at the same time.</p></html>";
		timeWindowLabel.setToolTipText(timeWindowToolTip);
		settingsPanel.add(timeWindowLabel, gbc);
		
		gbc.gridx = 3;
		JLabel timeWindowNumberLabel = new JLabel("Time window size");
		timeWindowNumberLabel.setToolTipText(timeWindowToolTip);
		settingsPanel.add(timeWindowNumberLabel, gbc);
		
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 7;
		SpinnerDateModel startModel = new SpinnerDateModel();
		startSpinner = new JSpinner();
		startModel.setCalendarField(Calendar.DAY_OF_YEAR);
		startSpinner.setModel(startModel);
		ArrayList<Date> dates = new ArrayList<Date>();
		for (int i = 0; i < Dna.data.getStatements().size(); i++) {
			dates.add(Dna.data.getStatements().get(i).getDate());
		}
		Collections.sort(dates);
		startModel.setValue(dates.get(0));
		startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd - HH:mm:ss"));
		startSpinner.setToolTipText(dateToolTip);
		settingsPanel.add(startSpinner, gbc);
		startSpinner.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		SpinnerDateModel stopModel = new SpinnerDateModel();
		stopSpinner = new JSpinner();
		stopModel.setCalendarField(Calendar.DAY_OF_YEAR);
		stopSpinner.setModel(stopModel);
		stopModel.setValue(dates.get(dates.size() - 1));
		stopSpinner.setEditor(new JSpinner.DateEditor(stopSpinner, "yyyy-MM-dd - HH:mm:ss"));
		stopSpinner.setToolTipText(dateToolTip);
		settingsPanel.add(stopSpinner, gbc);
		stopSpinner.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 2;
		String[] timeWindowItems = new String[] {"no time window", "using events", "using seconds", "using minutes", 
				"using hours", "using days", "using weeks", "using months", "using years"};
		timeWindowBox = new JComboBox<>(timeWindowItems);
		timeWindowBox.setToolTipText(timeWindowToolTip);
		settingsPanel.add(timeWindowBox, gbc);
		timeWindowBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 3;
		JSpinner timeWindowSpinner = new JSpinner(new SpinnerNumberModel(100, 0, 100000, 1));
		timeWindowSpinner.setToolTipText(timeWindowToolTip);
		settingsPanel.add(timeWindowSpinner, gbc);
		timeWindowSpinner.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		// fifth row of options: exclude values from variables
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 8;
		JLabel excludeVariableLabel = new JLabel("Exclude from variable");
		String excludeToolTip = "<html><p width=\"500\">By default, all nodes from all statements are included in the "
				+ "network, given the type of network and the statement type and variables selected. Often, however, "
				+ "one wishes to exclude certain nodes or statements from the analysis. For example, in an "
				+ "organization x organization congruence network via concepts, if one already knows that a certain "
				+ "concept is very general and does not add to the structure of the discourse network, one can "
				+ "exclude this concept. Or if the analysis should be restricted to a subgroup of actors (e.g., only "
				+ "legislators), one can select multiple actors that do not belong to this subgroup, and they will be "
				+ "ignored during network export. The way this process works is that the list of statements from which "
				+ "the network is generated is first filtered, and only statements that do not contain any of the "
				+ "values to be excluded will be retained for the export. To do this, one first needs to select a "
				+ "variable on the left and then select the values to be excluded. To select or unselect multiple "
				+ "values, one can hold the ctrl key while selecting or unselecting additional values. The preview "
				+ "on the right lists all variable-value combinations that are excluded.</p></html>";
		excludeVariableLabel.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeVariableLabel, gbc);
		
		gbc.gridx = 1;
		JLabel excludeValueLabel = new JLabel("Exclude values");
		excludeValueLabel.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeValueLabel, gbc);
		
		gbc.gridx = 2;
		JLabel excludePreviewLabel = new JLabel("Preview of excluded values");
		excludePreviewLabel.setToolTipText(excludeToolTip);
		settingsPanel.add(excludePreviewLabel, gbc);
		
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 9;
		excludeVariableList = new JList<String>();
		excludeVariableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		excludeVariableList.setLayoutOrientation(JList.VERTICAL);
		excludeVariableList.setVisibleRowCount(10);
		JScrollPane excludeVariableScroller = new JScrollPane(excludeVariableList);
		excludeVariableScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		excludeVariableList.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeVariableScroller, gbc);
		excludeVariableScroller.setPreferredSize(new Dimension(WIDTH, (int) excludeVariableScroller.getPreferredSize().getHeight()));
		excludeAuthor = new ArrayList<String>();
		excludeSource = new ArrayList<String>();
		excludeSection = new ArrayList<String>();
		excludeType = new ArrayList<String>();
		excludeValues = new HashMap<String, ArrayList<String>>();
		populateExcludeVariableList();
		excludeVariableList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selectedValue = excludeVariableList.getSelectedValue();
				if (e.getValueIsAdjusting() == false) {
					if (selectedValue != null) {
						String[] entriesArray;
						int[] indices;
						ArrayList<String> entriesList = new ArrayList<String>();
						if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 1) {
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
							excludeValueList.setListData(entriesArray);
							indices = new int[excludeType.size()];
							for (int i = 0; i < entriesArray.length; i++) {
								for (int j = 0; j < excludeType.size(); j++) {
									if (entriesArray[i].equals(excludeType.get(j))) {
										indices[j] = i;
									}
								}
							}
						} else if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 2) {
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
							excludeValueList.setListData(entriesArray);
							indices = new int[excludeSection.size()];
							for (int i = 0; i < entriesArray.length; i++) {
								for (int j = 0; j < excludeSection.size(); j++) {
									if (entriesArray[i].equals(excludeSection.get(j))) {
										indices[j] = i;
									}
								}
							}
						} else if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 3) {
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
							excludeValueList.setListData(entriesArray);
							indices = new int[excludeSource.size()];
							for (int i = 0; i < entriesArray.length; i++) {
								for (int j = 0; j < excludeSource.size(); j++) {
									if (entriesArray[i].equals(excludeSource.get(j))) {
										indices[j] = i;
									}
								}
							}
						} else if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 4) {
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
							excludeValueList.setListData(entriesArray);
							indices = new int[excludeAuthor.size()];
							for (int i = 0; i < entriesArray.length; i++) {
								for (int j = 0; j < excludeAuthor.size(); j++) {
									if (entriesArray[i].equals(excludeAuthor.get(j))) {
										indices[j] = i;
									}
								}
							}
						} else {
							entriesArray = Dna.data.getStringEntries(((StatementType) statementTypeBox.getSelectedItem()).getId(), selectedValue);
							Arrays.sort(entriesArray);
							excludeValueList.setListData(entriesArray);
							indices = new int[excludeValues.get(selectedValue).size()];
							for (int i = 0; i < entriesArray.length; i++) {
								for (int j = 0; j < excludeValues.get(selectedValue).size(); j++) {
									if (entriesArray[i].equals(excludeValues.get(selectedValue).get(j))) {
										indices[j] = i;
									}
								}
							}
						}
						excludeValueList.setSelectedIndices(indices);
					}
				}
			}
		});
		
		gbc.gridx = 1;
		excludeValueList = new JList<String>();
		excludeValueList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		excludeValueList.setLayoutOrientation(JList.VERTICAL);
		excludeValueList.setVisibleRowCount(10);
		JScrollPane excludeValueScroller = new JScrollPane(excludeValueList);
		excludeValueScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		excludeValueList.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeValueScroller, gbc);
		excludeValueScroller.setPreferredSize(new Dimension(WIDTH, (int) excludeValueScroller.getPreferredSize().getHeight()));
		excludeValueList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selectedVariable = excludeVariableList.getSelectedValue();
				List<String> selectedValues = excludeValueList.getSelectedValuesList();
				if (e.getValueIsAdjusting() == true) {
					if (selectedValues != null) {
						ArrayList<String> sel = new ArrayList<String>(selectedValues);
						if (selectedVariable.equals("type")) {
							excludeType = (ArrayList<String>) sel;
						} else if (selectedVariable.equals("source")) {
							excludeSource = (ArrayList<String>) sel;
						} else if (selectedVariable.equals("section")) {
							excludeSection = (ArrayList<String>) sel;
						} else if (selectedVariable.equals("author")) {
							excludeAuthor = (ArrayList<String>) sel;
						} else {
							excludeValues.put(selectedVariable, (ArrayList<String>) sel);
						}
					}
					String excludePreviewText = "";
					Iterator<String> it = excludeValues.keySet().iterator();
					while (it.hasNext()) {
						String excludeVariable = it.next();
						ArrayList<String> excludeValueArrayList = excludeValues.get(excludeVariable);
						if (excludeValueArrayList.size() > 0) {
							for (int i = 0; i < excludeValueArrayList.size(); i++) {
								excludePreviewText = excludePreviewText + excludeVariable + ": " + excludeValueArrayList.get(i) + "\n";
							}
						}
					}
					if (excludeAuthor.size() > 0) {
						for (int i = 0; i < excludeAuthor.size(); i++) {
							excludePreviewText = excludePreviewText + "author: " + excludeAuthor.get(i) + "\n";
						} 
					}
					if (excludeSource.size() > 0) {
						for (int i = 0; i < excludeSource.size(); i++) {
							excludePreviewText = excludePreviewText + "source: " + excludeSource.get(i) + "\n";
						} 
					}
					if (excludeSection.size() > 0) {
						for (int i = 0; i < excludeSection.size(); i++) {
							excludePreviewText = excludePreviewText + "section: " + excludeSection.get(i) + "\n";
						} 
					}
					if (excludeType.size() > 0) {
						for (int i = 0; i < excludeType.size(); i++) {
							excludePreviewText = excludePreviewText + "type: " + excludeType.get(i) + "\n";
						} 
					}
					excludePreviewArea.setText(excludePreviewText);
				}
			}
		});
		
		gbc.gridx = 2;
		gbc.gridwidth = 2;
		excludePreviewArea = new JTextArea();
		excludePreviewArea.setText("");
		excludePreviewArea.setEditable(false);
		excludePreviewArea.setBackground(settingsPanel.getBackground());
		JScrollPane excludePreviewScroller = new JScrollPane(excludePreviewArea);
		excludePreviewArea.setToolTipText(excludeToolTip);
		settingsPanel.add(excludePreviewScroller, gbc);
		excludePreviewScroller.setPreferredSize(excludeValueScroller.getPreferredSize());
		
		// sixth row: buttons
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 10;
		helpBox = new JCheckBox("Display tooltips with instructions");
		helpBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleHelp();
			}
			
		});
		settingsPanel.add(helpBox, gbc);
		
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.EAST;
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton revertButton = new JButton("Revert", new ImageIcon(getClass().getResource("/icons/arrow_rotate_anticlockwise.png")));
		String revertToolTip = "<html><p>Reset all settings to their default values.</p></html>";
		revertButton.setToolTipText(revertToolTip);
		buttonPanel.add(revertButton);
		revertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				networkModesBox.setSelectedIndex(0);
				int statementTypeIndex = -1;
				for (int i = 0; i < Dna.data.getStatementTypes().size(); i++) {
					String[] vars = getVariablesList(Dna.data.getStatementTypes().get(i), false, true, false, false);
					if (vars.length > 1) {
						statementTypeIndex = i;
						break;
					}
				}
				if (statementTypeIndex == -1) {
					statementTypeBox.setSelectedIndex(0);
					System.err.println("No statement type with more than one short text variable found!");
				} else {
					statementTypeBox.setSelectedIndex(statementTypeIndex);
				}
				statementTypeBox.updateUI();
				if (var1Box.getItemCount() > 1) {
					var1Box.setSelectedIndex(0);
					var2Box.setSelectedIndex(1);
				}
				if (qualifierBox.getItemCount() > 0) {
					qualifierBox.setSelectedIndex(0);
				}
				aggregationBox.setSelectedIndex(0);
				normalizationBox.setSelectedIndex(0);
				isolatesBox.setSelectedIndex(0);
				duplicatesBox.setSelectedIndex(0);
				ArrayList<Date> dates = new ArrayList<Date>();
				for (int i = 0; i < Dna.data.getStatements().size(); i++) {
					dates.add(Dna.data.getStatements().get(i).getDate());
				}
				Collections.sort(dates);
				startModel.setValue(dates.get(0));
				stopModel.setValue(dates.get(dates.size() - 1));
				timeWindowBox.setSelectedIndex(0);
				timeWindowSpinner.setValue(100);
				excludeVariableList.setSelectedIndex(0);
				excludePreviewArea.setText("");
				helpBox.setSelected(false);
			}
		});
		JButton cancelButton = new JButton("Cancel", new ImageIcon(getClass().getResource("/icons/cancel.png")));
		String cancelToolTip = "<html><p>Reset and close this window.</p></html>";
		cancelButton.setToolTipText(cancelToolTip);
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		exportButton = new JButton("Export...", new ImageIcon(getClass().getResource("/icons/accept.png")));
		String exportToolTip = "<html><p>Select a file name and save the network using the current settings.</p></html>";
		exportButton.setToolTipText(exportToolTip);
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith((String) fileFormatBox.getSelectedItem()) || f.isDirectory();
					}
					public String getDescription() {
						return "Network File (*" + (String) fileFormatBox.getSelectedItem() + ")";
					}
				});
				
				int returnVal = fc.showSaveDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String fileName = file.getPath();
					if (!fileName.endsWith((String) fileFormatBox.getSelectedItem())) {
						fileName = fileName + (String) fileFormatBox.getSelectedItem();
					}
					Thread exportThread = new Thread( new GuiExportThread(fileName), "Export network" );
					exportThread.start();
				}
			}
		});
		buttonPanel.add(exportButton);
		toggleHelp();
		settingsPanel.add(buttonPanel, gbc);
		
		this.add(settingsPanel, BorderLayout.NORTH);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Indicates whether the first variable is a document-level variable (i.e., "author", "source", "section", or "type").
	 * 
	 * @return boolean indicating if variable 1 as selected in the Exporter GUI is a document-level variable
	 */
	public boolean var1Document() {
		int lastIndex = var1Box.getItemCount() - 1;
		int selected = var1Box.getSelectedIndex();
		if (selected > lastIndex - 4) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates whether the second variable is a document-level variable (i.e., "author", "source", "section", or "type").
	 * 
	 * @return boolean indicating if variable 2 as selected in the Exporter GUI is a document-level variable
	 */
	public boolean var2Document() {
		int lastIndex = var2Box.getItemCount() - 1;
		int selected = var2Box.getSelectedIndex();
		if (selected > lastIndex - 4) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Sets a new {@link DefaultListModel} in the excludeVariableList and adds variables conditional on the statement type selected
	 */
	public void populateExcludeVariableList() {
		excludeValues.clear();
		StatementType selected = (StatementType) statementTypeBox.getSelectedItem();
		String[] excludeVariableItems = getVariablesList(selected, true, true, true, true);
		DefaultListModel<String> excludeVariableModel = new DefaultListModel<String>();
		for (int i = 0; i < excludeVariableItems.length - 4; i++) {
			excludeVariableModel.addElement(excludeVariableItems[i]);
			excludeValues.put(excludeVariableItems[i], new ArrayList<String>());
		}
		excludeVariableModel.addElement("author");
		excludeVariableModel.addElement("source");
		excludeVariableModel.addElement("section");
		excludeVariableModel.addElement("type");
		excludeVariableList.setModel(excludeVariableModel);
	}
	
	/**
	 * Show or hide tool tips with instructions depending on whether helpBox is checked
	 */
	public void toggleHelp() {
		javax.swing.ToolTipManager.sharedInstance().setInitialDelay(10);
		if (helpBox.isSelected()) {
			javax.swing.ToolTipManager.sharedInstance().setEnabled(true);
			javax.swing.ToolTipManager.sharedInstance().setDismissDelay(999999);
		} else {
			javax.swing.ToolTipManager.sharedInstance().setEnabled(false);
			javax.swing.ToolTipManager.sharedInstance().setDismissDelay(0);
		}
	}
	
	/**
	 * Returns a String array with the variables of the {@link StatementType} selected to fill a {@link JComboBox}.
	 * 
	 * @param longtext	boolean indicating whether long text variables should be included.
	 * @param shorttext	boolean indicating whether short text variables should be included.
	 * @param integer	boolean indicating whether integer variables should be included.
	 * @param bool		boolean indicating whether boolean variables should be included.
	 * @return			{@link String[]} with variables of the statementType selected.
	 */
	String[] getVariablesList(StatementType statementType, boolean longtext, boolean shorttext, boolean integer, boolean bool) {
		LinkedHashMap<String, String> variables = statementType.getVariables();
		Iterator<String> it = variables.keySet().iterator();
		ArrayList<String> items = new ArrayList<String>();
		while (it.hasNext()) {
			String var = it.next();
			if ((longtext == true && variables.get(var).equals("long text")) || 
					(shorttext == true && variables.get(var).equals("short text")) ||
					(integer == true && variables.get(var).equals("integer")) ||
					(bool == true && variables.get(var).equals("boolean"))) {
				items.add(var);
			}
		}
		if (shorttext == true) {
			items.add("author");
			items.add("source");
			items.add("section");
			items.add("type");
		}
		String[] vec = new String[items.size()];
		if (vec.length > 0) {
			for (int i = 0; i < items.size(); i++) {
				vec[i] = items.get(i);
			}
		}
		return vec;
	}
	
	/**
	 * @author Philip Leifeld
	 *
	 * GUI export thread. This is where the computations are executed and the data are written to a file. 
	 */
	class GuiExportThread implements Runnable {
		
		String filename;
		ArrayList<Statement> statements;
		ArrayList<Document> documents;
		String[] names1, names2;
		ProgressMonitor progressMonitor;
		
		public GuiExportThread(String filename) {
			this.filename = filename;
		}
		
		public void run() {
			progressMonitor = new ProgressMonitor(Exporter.this, "Exporting network data.", "(1/4) Filtering statements...", 0, 4);
			progressMonitor.setMillisToDecideToPopup(1);
			// delay the process by a second to make sure the progress monitor shows up
			// see here: https://coderanch.com/t/631127/java/progress-monitor-showing
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			progressMonitor.setProgress(0);
			
			// step 1: filter statements by date, statement type, empty variable entries, qualifier, and excluded values
			progressMonitor.setNote("(1/4) Filtering statements...");
			Date startDate = (Date) startSpinner.getValue();
			Date stopDate = (Date) stopSpinner.getValue();
			StatementType statementType = (StatementType) statementTypeBox.getSelectedItem();
			String var1Name = (String) var1Box.getSelectedItem();
			String var2Name = (String) var2Box.getSelectedItem();
			String qualifierName = (String) qualifierBox.getSelectedItem();
			boolean ignoreQualifier = aggregationBox.getSelectedItem().equals("ignore");
			String duplicateSetting = (String) duplicatesBox.getSelectedItem();
			statements = Dna.data.getStatements();
			documents = Dna.data.getDocuments();
			boolean filterEmptyFields = true;
			if (networkModesBox.getSelectedItem().equals("Event list")) {
				filterEmptyFields = false;
			}
			statements = filter(statements, documents, startDate, stopDate, statementType, var1Name, var2Name, 
					var1Document(), var2Document(), qualifierName, ignoreQualifier, duplicateSetting, 
					excludeAuthor, excludeSource, excludeSection, excludeType, excludeValues, filterEmptyFields);
			System.out.println("Export was launched: " + statements.size() + " out of " + Dna.data.getStatements().size() 
					+ " statements retained after filtering.");
			progressMonitor.setProgress(1);
			
			// step 2: compile the node labels (and thereby dimensions) for the first and second mode
			progressMonitor.setNote("(2/4) Compiling node labels...");
			if (!networkModesBox.getSelectedItem().equals("Event list")) {  // labels are only needed for one-mode or two-mode networks
				boolean includeIsolates = false;
				if (isolatesBox.getSelectedItem().equals("include isolates")) {
					includeIsolates = true;
				}
				int statementTypeId = statementType.getId();
				ArrayList<Statement> originalStatements = Dna.data.getStatements();
				names1 = extractLabels(statements, originalStatements, documents, var1Name, var1Document(), statementTypeId, includeIsolates);
				names2 = extractLabels(statements, originalStatements, documents, var2Name, var2Document(), statementTypeId, includeIsolates);
				System.out.println("Node labels have been extracted.");
			}
			progressMonitor.setProgress(2);
			
			// step 3: create network data structure
			progressMonitor.setNote("(3/4) Computing network...");
			Matrix matrix = null;
			String qualifier = (String) qualifierBox.getSelectedItem();
			String qualifierAggregation = (String) aggregationBox.getSelectedItem();
			String normalization = (String) normalizationBox.getSelectedItem();
			if (networkModesBox.getSelectedItem().equals("Event list")) {
				// no network preparation needed
			} else if (networkModesBox.getSelectedItem().equals("Two-mode network")) {
				if (timeWindowBox.getSelectedItem().equals("no time window")) {
					matrix = computeTwoModeMatrix(statements, documents, statementType, var1Name, var2Name, var1Document(), 
							var2Document(), names1, names2, qualifier, qualifierAggregation, normalization);
				} else {
					timeWindowMatrices = computeTimeWindowMatrices(statements, documents, statementType, var1Name, var2Name, var1Document(), 
							var2Document(), names1, names2, qualifier, qualifierAggregation, normalization, true, startDate, stopDate, 
							(String) timeWindowBox.getSelectedItem(), (int) timeWindowSpinner.getValue());
				}
			} else if (networkModesBox.getSelectedItem().equals("One-mode network")) {
				if (timeWindowBox.getSelectedItem().equals("no time window")) {
					matrix = computeOneModeMatrix(statements, documents, statementType, var1Name, var2Name, var1Document(), 
							var2Document(), names1, names2, qualifier, qualifierAggregation, normalization);
				} else {
					timeWindowMatrices = computeTimeWindowMatrices(statements, documents, statementType, var1Name, var2Name, var1Document(), 
							var2Document(), names1, names2, qualifier, qualifierAggregation, normalization, false, startDate, stopDate, 
							(String) timeWindowBox.getSelectedItem(), (int) timeWindowSpinner.getValue());
				}
			}
			System.out.println("Network has been created.");
			progressMonitor.setProgress(3);
			
			// step 4: write to file
			progressMonitor.setNote("(4/4) Writing to file...");
			String fileFormat = (String) fileFormatBox.getSelectedItem();
			if (networkModesBox.getSelectedItem().equals("Event list")) {
				eventCSV(statements, documents, statementType, filename);
			} else {
				boolean twoMode = false;
				if (networkModesBox.getSelectedItem().equals("Two-mode network")) {
					twoMode = true;
				}
				if (fileFormat.equals(".csv")) {
					exportCSV(matrix, filename);
				} else if (fileFormat.equals(".dl")) {
					exportDL(matrix, filename, twoMode);
				} else if (fileFormat.equals(".graphml")) {
					String[] values1 = retrieveValues(statements, documents, var1Name, var1Document());
					String[] values2 = retrieveValues(statements, documents, var2Name, var2Document());
					int[] frequencies1 = countFrequencies(values1, names1);
					int[] frequencies2 = countFrequencies(values2, names2);
					ArrayList<AttributeVector> attributes = Dna.data.getAttributes();
					boolean qualifierBinary = false;
					if (statementType.getVariables().get(qualifierName).equals("boolean")) {
						qualifierBinary = true;
					}
					exportGraphml(matrix, twoMode, statementType, filename, var1Name, var2Name, frequencies1, frequencies2, 
							attributes, qualifierAggregation, qualifierBinary);
				}
			}
			progressMonitor.setProgress(4);
			JOptionPane.showMessageDialog(Dna.dna.gui, "Data were exported to \"" + filename + "\".");
		}
	}
	
	/**
	 * Create a series of one-mode or two-mode networks using a moving time window.
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param documents             An {@link ArrayList} of {@link Document}s which contain the statements.
	 * @param statementType         The {@link StatementType} corresponding to the statements.
	 * @param var1                  {@link String} denoting the first variable (containing the row values).
	 * @param var2                  {@link String} denoting the second variable (containing the columns values).
	 * @param var1Document          {@link boolean} indicating whether the first variable is a document-level variable.
	 * @param var2Document          {@link boolean} indicating whether the second variable is a document-level variable.
	 * @param names1                {@link String} array containing the row labels.
	 * @param names2                {@link String} array containing the column labels.
	 * @param qualifier             {@link String} denoting the name of the qualifier variable.
	 * @param qualifierAggregation  {@link String} indicating how different levels of the qualifier variable are aggregated. Valid values are "ignore", "subtract", and "combine".
	 * @param normalization         {@link String} indicating what type of normalization will be used. Valid values are "no", "average activity", "Jaccard", and "cosine".
	 * @param twoMode               Create two-mode networks? If false, one-mode networks are created.
	 * @param start                 Start date of the time range over which the time window moves.
	 * @param stop                  End date of the time range over which the time window moves.
	 * @param unitType              {@link String} indicating the kind of temporal unit used for the moving window. Valid values are "using seconds", "using minutes", "using hours", "using days", "using weeks", "using months", "using years", and "using events".
	 * @param timeUnits             How large is the time window? E.g., 100 days, where "days" are defined in the unit type argument.
	 * @return                      {@link Matrix} object containing a one-mode network matrix.
	 */
	private ArrayList<Matrix> computeTimeWindowMatrices(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, 
			String var1, String var2, boolean var1Document, boolean var2Document, String[] names1, String[] names2, String qualifier, 
			String qualifierAggregation, String normalization, boolean twoMode, Date start, Date stop, String unitType, int timeUnits) {
		
		timeWindowMatrices = new ArrayList<Matrix>();
		
		int statementIterator = timeUnits - 1;
		
		ArrayList<Date> timeLabels = new ArrayList<Date>();
		GregorianCalendar currentStop = new GregorianCalendar();
		currentStop.setTime(start);
		GregorianCalendar currentStart = (GregorianCalendar) currentStop.clone();
		if (unitType.equals("using seconds")) {
			currentStop.add(Calendar.SECOND, timeUnits);
		} else if (unitType.equals("using minutes")) {
			currentStop.add(Calendar.MINUTE, timeUnits);
		} else if (unitType.equals("using hours")) {
			currentStop.add(Calendar.HOUR_OF_DAY, timeUnits);
		} else if (unitType.equals("using days")) {
			currentStop.add(Calendar.DAY_OF_MONTH, timeUnits);
		} else if (unitType.equals("using weeks")) {
			currentStop.add(Calendar.WEEK_OF_YEAR, timeUnits);
		} else if (unitType.equals("using months")) {
			currentStop.add(Calendar.MONTH, timeUnits);
		} else if (unitType.equals("using years")) {
			currentStop.add(Calendar.YEAR, timeUnits);
		} else if (unitType.equals("using events")) {
			currentStop.setTime(statements.get(statementIterator).getDate());
		}
		while (!currentStop.after(stop)) {
			ArrayList<Statement> currentStatements = new ArrayList<Statement>();
			for (int i = 0; i < statements.size(); i++) {
				GregorianCalendar currentTime = new GregorianCalendar();
				currentTime.setTime(statements.get(i).getDate());
				if (!currentTime.before(currentStart) && !currentTime.after(currentStop)) {
					currentStatements.add(statements.get(i));
				}
			}
			
			if (twoMode == true) {
				timeWindowMatrices.add(computeTwoModeMatrix(currentStatements, documents, statementType, var1, var2, var1Document, 
						var2Document, names1, names2, qualifier, qualifierAggregation, normalization));
			} else {
				timeWindowMatrices.add(computeOneModeMatrix(currentStatements, documents, statementType, var1, var2, var1Document, 
						var2Document, names1, names2, qualifier, qualifierAggregation, normalization));
			}
			timeLabels.add(currentStop.getTime());
			if (unitType.equals("using seconds")) {
				currentStart.add(Calendar.SECOND, 1);
				currentStop.add(Calendar.SECOND, 1);
			} else if (unitType.equals("using minutes")) {
				currentStart.add(Calendar.MINUTE, 1);
				currentStop.add(Calendar.MINUTE, 1);
			} else if (unitType.equals("using hours")) {
				currentStart.add(Calendar.HOUR_OF_DAY, 1);
				currentStop.add(Calendar.HOUR_OF_DAY, 1);
			} else if (unitType.equals("using days")) {
				currentStart.add(Calendar.DAY_OF_MONTH, 1);
				currentStop.add(Calendar.DAY_OF_MONTH, 1);
			} else if (unitType.equals("using weeks")) {
				currentStart.add(Calendar.WEEK_OF_YEAR, 1);
				currentStop.add(Calendar.WEEK_OF_YEAR, 1);
			} else if (unitType.equals("using months")) {
				currentStart.add(Calendar.MONTH, 1);
				currentStop.add(Calendar.MONTH, 1);
			} else if (unitType.equals("using years")) {
				currentStart.add(Calendar.YEAR, 1);
				currentStop.add(Calendar.YEAR, 1);
			} else if (unitType.equals("using events")) {
				if (statementIterator + 1 < statements.size()) {
					statementIterator = statementIterator + 1;
					currentStop.setTime(statements.get(statementIterator).getDate());
				} else {
					currentStop.add(Calendar.SECOND, 1);  // invoke stop of while loop
				}
				currentStart.setTime(statements.get(statementIterator - timeUnits).getDate());
			}
		}
		return timeWindowMatrices;
	}
	
	/**
	 * Extract the labels for all nodes for a variable from the statements, conditional on isolates settings
	 * 
	 * @param statements          {@link ArrayList} of filtered {@link Statement}s
	 * @param originalStatements  {@link ArrayList} of unfiltered {@link Statement}s (i.e., the original list of statements)
	 * @param documents           {@link ArrayList} of documents in the database
	 * @param variable            {@link String} indicating the variable for which labels should be extracted
	 * @param statementTypeId     {@link int} specifying the statement type ID to which the variable belongs
	 * @param includeIsolates     {@link boolean} indicating whether all nodes should be included or just those after applying the statement filter
	 * @return                    {@link String} array containing all sorted node names
	 */
	private String[] extractLabels(
			ArrayList<Statement> statements, 
			ArrayList<Statement> originalStatements, 
			ArrayList<Document> documents, 
			String variable, 
			boolean variableDocument, 
			int statementTypeId, 
			boolean includeIsolates) {
		
		// decide whether to use the original statements or the filtered statements
		ArrayList<Statement> finalStatements;
		if (includeIsolates == true) {
			finalStatements = originalStatements;
		} else {
			finalStatements = statements;
		}

		// HashMap for fast lookup of document indices by ID
		HashMap<Integer, Integer> docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}
		
		// go through statements and extract names
		ArrayList<String> names = new ArrayList<String>();
		String n = null;
		for (int i = 0; i < finalStatements.size(); i++) {
			if (variableDocument == true) {
				if (variable.equals("author")) {
					n = documents.get(docMap.get(finalStatements.get(i).getDocumentId())).getAuthor();
				} else if (variable.equals("source")) {
					n = documents.get(docMap.get(finalStatements.get(i).getDocumentId())).getSource();
				} else if (variable.equals("section")) {
					n = documents.get(docMap.get(finalStatements.get(i).getDocumentId())).getSection();
				} else if (variable.equals("type")) {
					n = documents.get(docMap.get(finalStatements.get(i).getDocumentId())).getType();
				}
				if (!names.contains(n)) {
					names.add(n);
				}
			} else if (finalStatements.get(i).getStatementTypeId() == statementTypeId) {
				n = (String) finalStatements.get(i).getValues().get(variable);
				if (!names.contains(n)) {
					names.add(n);
				}
			}
		}
		
		// sort and convert to array, then return
		Collections.sort(names);
		if (names.get(0).equals("")) { // remove empty field
			names.remove(0);
		}
		String[] nameArray = new String[names.size()];
		for (int i = 0; i < names.size(); i++) {
			nameArray[i] = names.get(i);
		}
		return nameArray;
	}
	
	/**
	 * Return a filtered list of {@link Statement}s based on the settings in the GUI.
	 * 
	 * @return	ArrayList of filtered {@link Statement}s
	 */
	/**
	 * Return a filtered list of {@link Statement}s based on the settings in the GUI.
	 * 
	 * @param statements          {@link ArrayList} of {@link Statement}s to be filtered.
	 * @param startDate           {@link Date} object indicating the start date
	 * @param stopDate            {@link Date} object indicating the end date
	 * @param statementType       {@link StatementType} to which the export is restricted
	 * @param var1                {@link String} indicating the first variable used for network construction, e.g., "organization"
	 * @param var2                {@link String} indicating the second variable used for network construction, e.g., "concept"
	 * @param var1Document        {@link boolean} indicating if the var1 variable is a document-level variable (as opposed to statement-level)
	 * @param var2Document        {@link boolean} indicating if the var2 variable is a document-level variable (as opposed to statement-level)
	 * @param qualifierName       {@link String} indicating the qualifier variable, e.g., "agreement"
	 * @param ignoreQualifier     {@link boolean} indicating whether the qualifier variable should be ignored
	 * @param duplicateSetting    {@link String} indicating how to handle duplicates; valid settings include "include all duplicates", "ignore per document", "ignore per calendar week", "ignore per calendar month", "ignore per calendar year", or "ignore across date range"
	 * @param excludeAuthor       {@link ArrayList} with {@link String}s containing document authors to exclude
	 * @param excludeSource       {@link ArrayList} with {@link String}s containing document sources to exclude
	 * @param excludeSection      {@link ArrayList} with {@link String}s containing document sections to exclude
	 * @param excludeType         {@link ArrayList} with {@link String}s containing document types to exclude
	 * @param excludeValues       {@link HashMap} with {@link String}s as keys (indicating the variable for which entries should be excluded from export) and {@link HashMap}s of {@link String}s (containing variable entries to exclude from network export)
	 * @param filterEmptyFields   {@link boolean} indicating whether empty fields (i.e., "") should be excluded
	 * @return                    {@link ArrayList} of filtered {@link Statement}s
	 */
	private ArrayList<Statement> filter(
			ArrayList<Statement> statements, 
			ArrayList<Document> documents, 
			Date startDate, 
			Date stopDate, 
			StatementType statementType, 
			String var1, 
			String var2, 
			boolean var1Document, 
			boolean var2Document, 
			String qualifierName, 
			boolean ignoreQualifier, 
			String duplicateSetting, 
			ArrayList<String> excludeAuthor, 
			ArrayList<String> excludeSource, 
			ArrayList<String> excludeSection, 
			ArrayList<String> excludeType, 
			HashMap<String, ArrayList<String>> excludeValues, 
			boolean filterEmptyFields) {
		
		// reporting
		Iterator<String> excludeIterator = excludeValues.keySet().iterator();
		while (excludeIterator.hasNext()) {
			String key = excludeIterator.next();
			ArrayList<String> values = excludeValues.get(key);
			for (int i = 0; i < values.size(); i++) {
				System.out.println("[Excluded] " + key + ": " + values.get(i));
			}
		}
		for (int i = 0; i < excludeAuthor.size(); i++) {
			System.out.println("[Excluded] author: " + excludeAuthor.get(i));
		}
		for (int i = 0; i < excludeSource.size(); i++) {
			System.out.println("[Excluded] source: " + excludeSource.get(i));
		}
		for (int i = 0; i < excludeSection.size(); i++) {
			System.out.println("[Excluded] section: " + excludeSection.get(i));
		}
		for (int i = 0; i < excludeType.size(); i++) {
			System.out.println("[Excluded] type: " + excludeType.get(i));
		}
		
		// HashMap for fast lookup of document indices by ID
		HashMap<Integer, Integer> docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}
		
		// Create arrays with variable values
		String[] values1 = retrieveValues(statements, documents, var1, var1Document);
		String[] values2 = retrieveValues(statements, documents, var2, var2Document);
		
		// process and exclude statements
		Statement s;
		ArrayList<Statement> al = new ArrayList<Statement>();
	    String previousVar1 = null;
	    String previousVar2 = null;
	    Calendar cal, calPrevious;
	    int year, month, week, yearPrevious, monthPrevious, weekPrevious;
		for (int i = 0; i < statements.size(); i++) {
			boolean select = true;
			s = statements.get(i);
			
			// step 1: get all statement IDs corresponding to date range and statement type
			if (s.getDate().before(startDate)) {
				select = false;
			} else if (s.getDate().after(stopDate)) {
				select = false;
			} else if (s.getStatementTypeId() != statementType.getId()) {
				select = false;
			}
			
			// step 2: check against excluded values
			if (excludeAuthor.contains(documents.get(docMap.get(s.getDocumentId())).getAuthor())) {
				select = false;
			} else if (excludeSource.contains(documents.get(docMap.get(s.getDocumentId())).getSource())) {
				select = false;
			} else if (excludeSection.contains(documents.get(docMap.get(s.getDocumentId())).getSection())) {
				select = false;
			} else if (excludeType.contains(documents.get(docMap.get(s.getDocumentId())).getType())) {
				select = false;
			}
			if (select == true) {
				Iterator<String> keyIterator = excludeValues.keySet().iterator();
				while (keyIterator.hasNext()) {
					String key = keyIterator.next();
					String string = "";
					if (statementType.getVariables().get(key) == null) {
						throw new NullPointerException("'" + key + "' is not a statement-level variable and cannot be excluded.");
					} else if (statementType.getVariables().get(key).equals("boolean") || statementType.getVariables().get(key).equals("integer")) {
						string = String.valueOf(s.getValues().get(key));
					} else {
						string = (String) s.getValues().get(key);
					}
					if (excludeValues.get(key).contains(string)) {
						select = false;
					}
				}
			}

			// step 3: check against empty fields
			if (select == true) {
				if (values1[i].equals("") || values2[i].equals("")) {
					if (filterEmptyFields == true) {
						select = false;
					}
				}
			}
			
			// step 4: check for duplicates
			cal = Calendar.getInstance();
		    cal.setTime(s.getDate());
		    year = cal.get(Calendar.YEAR);
		    month = cal.get(Calendar.MONTH);
		    week = cal.get(Calendar.WEEK_OF_YEAR);
			if (!duplicateSetting.equals("include all duplicates")) {
				for (int j = al.size() - 1; j >= 0; j--) {
				    if (var1Document == false) {
				    	previousVar1 = (String) al.get(j).getValues().get(var1);
				    } else if (var1.equals("author")) {
				    	previousVar1 = documents.get(docMap.get(al.get(j).getDocumentId())).getAuthor();
				    } else if (var1.equals("source")) {
				    	previousVar1 = documents.get(docMap.get(al.get(j).getDocumentId())).getSource();
				    } else if (var1.equals("section")) {
				    	previousVar1 = documents.get(docMap.get(al.get(j).getDocumentId())).getSection();
				    } else if (var1.equals("type")) {
				    	previousVar1 = documents.get(docMap.get(al.get(j).getDocumentId())).getType();
				    }
				    if (var2Document == false) {
				    	previousVar2 = (String) al.get(j).getValues().get(var2);
				    } else if (var2.equals("author")) {
				    	previousVar2 = documents.get(docMap.get(al.get(j).getDocumentId())).getAuthor();
				    } else if (var2.equals("source")) {
				    	previousVar2 = documents.get(docMap.get(al.get(j).getDocumentId())).getSource();
				    } else if (var2.equals("section")) {
				    	previousVar2 = documents.get(docMap.get(al.get(j).getDocumentId())).getSection();
				    } else if (var2.equals("type")) {
				    	previousVar2 = documents.get(docMap.get(al.get(j).getDocumentId())).getType();
				    }
					calPrevious = Calendar.getInstance();
				    calPrevious.setTime(al.get(j).getDate());
				    yearPrevious = calPrevious.get(Calendar.YEAR);
				    monthPrevious = calPrevious.get(Calendar.MONTH);
				    weekPrevious = calPrevious.get(Calendar.WEEK_OF_YEAR);
					if ( s.getStatementTypeId() == al.get(j).getStatementTypeId()
							&& (al.get(j).getDocumentId() == s.getDocumentId() && duplicateSetting.equals("ignore per document") 
								|| duplicateSetting.equals("ignore across date range")
								|| (duplicateSetting.equals("ignore per calendar year") && year == yearPrevious)
								|| (duplicateSetting.equals("ignore per calendar month") && month == monthPrevious)
								|| (duplicateSetting.equals("ignore per calendar week") && week == weekPrevious) )
							&& values1[i].equals(previousVar1)
							&& values2[i].equals(previousVar2)
							&& (s.getValues().get(qualifierName).equals(al.get(j).getValues().get(qualifierName)) || ignoreQualifier == true) ) {
						select = false;
						break;
					}
				}
			}
			
			// step 5: add only if the statement passed all checks
			if (select == true) {
				al.add(s);
			}
		}
		return(al);
	}

	/**
	 * Create a one-mode network {@link Matrix}.
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param documents             An {@link ArrayList} of {@link Document}s which contain the statements.
	 * @param statementType         The {@link StatementType} corresponding to the statements.
	 * @param var1                  {@link String} denoting the first variable (containing the row values).
	 * @param var2                  {@link String} denoting the second variable (containing the columns values).
	 * @param var1Document          {@link boolean} indicating whether the first variable is a document-level variable.
	 * @param var2Document          {@link boolean} indicating whether the second variable is a document-level variable.
	 * @param names1                {@link String} array containing the row labels.
	 * @param names2                {@link String} array containing the column labels.
	 * @param qualifier             {@link String} denoting the name of the qualifier variable.
	 * @param qualifierAggregation  {@link String} indicating how different levels of the qualifier variable are aggregated. Valid values are "ignore", "subtract", and "combine".
	 * @param normalization         {@link String} indicating what type of normalization will be used. Valid values are "no", "average activity", "Jaccard", and "cosine".
	 * @return                      {@link Matrix} object containing a one-mode network matrix.
	 */
	private Matrix computeOneModeMatrix(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, 
			String var1, String var2, boolean var1Document, boolean var2Document, String[] names1, String[] names2, String qualifier, 
			String qualifierAggregation, String normalization) {
		
		if (statements.size() == 0) {
			double[][] m = new double[names1.length][names1.length];
			Matrix mt = new Matrix(m, names1, names1, true);
			return mt;
		}
		
		int statementTypeId = statementType.getId();
		boolean booleanQualifier = true;  // is the qualifier boolean, rather than integer?
		if (statementType.getVariables().get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (booleanQualifier == true) {
			qualifierValues = new int[] {0, 1};
		} else {
			qualifierValues = Dna.data.getIntEntries(statementTypeId, qualifier);
		}
		
		double[][][] array = createArray(statements, documents, statementType, var1, var2, var1Document, var2Document, 
				names1, names2, qualifier, qualifierAggregation);
		
		double[][] mat1 = new double[names1.length][names1.length];  // square matrix for "congruence" (or "ignore") results
		double[][] mat2 = new double[names1.length][names1.length];  // square matrix for "conflict" results
		double[][] m = new double[names1.length][names1.length];  // square matrix for final results
		double range = Math.abs(qualifierValues[qualifierValues.length - 1] - qualifierValues[0]);
		double i1count = 0.0;
		double i2count = 0.0;
		for (int i1 = 0; i1 < names1.length; i1++) {
			for (int i2 = 0; i2 < names1.length; i2++) {
				if (i1 != i2) {
					for (int j = 0; j < names2.length; j++) {
						// "ignore": sum up i1 and i2 independently over levels of k, then multiply.
						// In the binary case, this amounts to counting how often each concept is used and then multiplying frequencies.
						if (qualifierAggregation.equals("ignore")) {
							i1count = 0.0;
							i2count = 0.0;
							for (int k = 0; k < qualifierValues.length; k++) {
								i1count = i1count + array[i1][j][k];
								i2count = i2count + array[i2][j][k];
							}
							mat1[i1][i2] = mat1[i1][i2] + i1count * i2count;
						}
						// "congruence": sum up proximity of i1 and i2 per level of k, weighted by joint usage.
						// In the binary case, this reduces to the sum of weighted matches per level of k
						if (qualifierAggregation.equals("congruence") || qualifierAggregation.equals("subtract")) {
							for (int k1 = 0; k1 < qualifierValues.length; k1++) {
								for (int k2 = 0; k2 < qualifierValues.length; k2++) {
									mat1[i1][i2] = mat1[i1][i2] + (array[i1][j][k1] * array[i2][j][k2] * (1.0 - ((Math.abs(qualifierValues[k1] - qualifierValues[k2]) / range))));
								}
							}
						}
						// "conflict": same as congruence, but distance instead of proximity
						if (qualifierAggregation.equals("conflict") || qualifierAggregation.equals("subtract")) {
							for (int k1 = 0; k1 < qualifierValues.length; k1++) {
								for (int k2 = 0; k2 < qualifierValues.length; k2++) {
									mat2[i1][i2] = mat2[i1][i2] + (array[i1][j][k1] * array[i2][j][k2] * ((Math.abs(qualifierValues[k1] - qualifierValues[k2]) / range)));
								}
							}
						}
					}
					
					// normalization
					double norm = 1.0;
					if (normalization.equals("no")) {
						norm = 1.0;
					} else if (normalization.equals("average activity")) {
						i1count = 0.0;
						i2count = 0.0;
						for (int j = 0; j < names2.length; j++) {
							for (int k = 0; k < qualifierValues.length; k++) {
								i1count = i1count + array[i1][j][k];
								i2count = i2count + array[i2][j][k];
							}
						}
						norm = (i1count + i2count) / 2;
					} else if (normalization.equals("Jaccard")) {
						double m10 = 0.0;
						double m01 = 0.0;
						double m11 = 0.0;
						for (int j = 0; j < names2.length; j++) {
							for (int k = 0; k < qualifierValues.length; k++) {
								if (array[i2][j][k] == 0) {
									m10 = m10 + array[i1][j][k];
								}
								if (array[i1][j][k] == 0) {
									m01 = m01 + array[i2][j][k];
								}
								if (array[i1][j][k] > 0 && array[i2][j][k] > 0) {
									m11 = m11 + (array[i1][j][k] * array[i2][j][k]);
								}
							}
						}
						norm = m01 + m10 + m11;
					} else if (normalization.equals("cosine")) {
						i1count = 0.0;
						i2count = 0.0;
						for (int j = 0; j < names2.length; j++) {
							for (int k = 0; k < qualifierValues.length; k++) {
								i1count = i1count + array[i1][j][k];
								i2count = i2count + array[i2][j][k];
							}
						}
						norm = Math.sqrt(i1count * i1count) * Math.sqrt(i2count * i2count);
					}
					mat1[i1][i2] = mat1[i1][i2] / norm;
					mat2[i1][i2] = mat2[i1][i2] / norm;
					
					// "subtract": congruence minus conflict; use the appropriate matrix or matrices
					if (qualifierAggregation.equals("ignore")) {
						m[i1][i2] = mat1[i1][i2];
					} else if (qualifierAggregation.equals("congruence")) {
						m[i1][i2] = mat1[i1][i2];
					} else if (qualifierAggregation.equals("conflict")) {
						m[i1][i2] = mat2[i1][i2];
					} else if (qualifierAggregation.equals("subtract")) {
						m[i1][i2] = mat1[i1][i2] - mat2[i1][i2];
					}
				}
			}
		}
		
		
		boolean integerBoolean;
		if (normalization.equals("no") && booleanQualifier == true) {
			integerBoolean = true;
		} else {
			integerBoolean = false;
		}
		
		Matrix matrix = new Matrix(m, names1, names1, integerBoolean);
		return matrix;
	}
	
	/**
	 * Create a two-mode network {@link Matrix}.
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param documents             An {@link ArrayList} of {@link Document}s which contain the statements.
	 * @param statementType         The {@link StatementType} corresponding to the statements.
	 * @param var1                  {@link String} denoting the first variable (containing the row values).
	 * @param var2                  {@link String} denoting the second variable (containing the columns values).
	 * @param var1Document          {@link boolean} indicating whether the first variable is a document-level variable.
	 * @param var2Document          {@link boolean} indicating whether the second variable is a document-level variable.
	 * @param names1                {@link String} array containing the row labels.
	 * @param names2                {@link String} array containing the column labels.
	 * @param qualifier             {@link String} denoting the name of the qualifier variable.
	 * @param qualifierAggregation  {@link String} indicating how different levels of the qualifier variable are aggregated. Valid values are "ignore", "subtract", and "combine".
	 * @param normalization         {@link String} indicating what type of normalization will be used. Valid values are "no", "activity", and "prominence".
	 * @return                      {@link Matrix} object containing a two-mode network matrix.
	 */
	private Matrix computeTwoModeMatrix(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, 
			String var1, String var2, boolean var1Document, boolean var2Document, String[] names1, String[] names2, String qualifier, 
			String qualifierAggregation, String normalization) {
		if (statements.size() == 0) {
			double[][] m = new double[names1.length][names2.length];
			Matrix mt = new Matrix(m, names1, names2, true);
			return mt;
		}
		int statementTypeId = statementType.getId();
		boolean booleanQualifier = true;  // is the qualifier boolean, rather than integer?
		// TODO: it may be possible that there is no qualifier; adjust for this case (also in the one-mode case?)
		if (statementType.getVariables().get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (booleanQualifier == true) {
			qualifierValues = new int[] {0, 1};
		} else {
			qualifierValues = Dna.data.getIntEntries(statementTypeId, qualifier);
		}
		
		double[][][] array = createArray(statements, documents, statementType, var1, var2, var1Document, var2Document, 
				names1, names2, qualifier, qualifierAggregation);
		
		// combine levels of the qualifier variable conditional on qualifier aggregation option
		double[][] mat = new double[names1.length][names2.length];  // initialized with zeros
		HashMap<Integer, ArrayList<Integer>> combinations = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < names1.length; i++) {
			for (int j = 0; j < names2.length; j++) {
				if (qualifierAggregation.equals("combine")) {  // combine
					double[] vec = array[i][j];  // may be weighted, so create a second, binary vector vec2
					int[] vec2 = new int[vec.length];
					ArrayList<Integer> qualVal = new ArrayList<Integer>();  // a list of qualifier values used at mat[i][j]
					for (int k = 0; k < vec.length; k++) {
						if (vec[k] > 0) {
							vec2[k] = 1;
							qualVal.add(qualifierValues[k]);
						}
					}
					mat[i][j] = lexRank(vec2);  // compute lexical rank, i.e., map the combination of values to a single integer
					combinations.put(lexRank(vec2), qualVal);  // the bijection needs to be stored for later reporting
				} else {
					for (int k = 0; k < qualifierValues.length; k++) {
						if (qualifierAggregation.equals("ignore")) {  // ignore
							mat[i][j] = mat[i][j] + array[i][j][k];  // duplicates were already filtered out in the statement filter, so just add
						} else if (qualifierAggregation.equals("subtract")) {  // subtract
							if (booleanQualifier == false && qualifierValues[k] < 0) {  // subtract weighted absolute value
								mat[i][j] = mat[i][j] - (Math.abs(qualifierValues[k]) * array[i][j][k]);
							} else if (booleanQualifier == false && qualifierValues[k] >= 0) {  // add weighted absolute value
								mat[i][j] = mat[i][j] + (Math.abs(qualifierValues[k]) * array[i][j][k]);
							} else if (booleanQualifier == true && qualifierValues[k] == 0) {  // subtract 1 at most
								mat[i][j] = mat[i][j] - array[i][j][k];
							} else if (booleanQualifier == true && qualifierValues[k] > 0) {  // add 1 at most
								mat[i][j] = mat[i][j] + array[i][j][k];
							}
						}
					}
				}
			}
		}
		
		// report combinations if necessary
		if (combinations.size() > 0) {
			Iterator<Integer> keyIterator = combinations.keySet().iterator();
			while (keyIterator.hasNext()){
				Integer key = (Integer) keyIterator.next();
				ArrayList<Integer> values = combinations.get(key);
				System.out.print("An edge weight of " + key + " corresponds to the following combination of integers in the DNA coding: ");
				for (int i = 0; i < values.size(); i++) {
					System.out.print(values.get(i) + " ");
				}
				System.out.print("\n");
			}
		}
		
		// normalization
		boolean integerBoolean = false;
		if (normalization.equals("no")) {
			integerBoolean = true;
		} else if (normalization.equals("activity")) {
			integerBoolean = false;
			double currentDenominator;
			for (int i = 0; i < names1.length; i++) {
				currentDenominator = 0.0;
				if (qualifierAggregation.equals("ignore")) {  // iterate through columns of matrix and sum weighted values
					for (int j = 0; j < names2.length; j++) {
						currentDenominator = currentDenominator + mat[i][j];
					}
				} else if (qualifierAggregation.equals("combine")) {  // iterate through columns of matrix and count how many are larger than one
					System.err.println("Warning: Normalization and qualifier setting 'combine' yield results that cannot be interpreted.");
					for (int j = 0; j < names2.length; j++) {
						if (mat[i][j] > 0.0) {
							currentDenominator = currentDenominator + 1.0;
						}
					}
				} else if (qualifierAggregation.equals("subtract")) {  // iterate through array and sum for different levels
					for (int j = 0; j < names2.length; j++) {
						for (int k = 0; k < qualifierValues.length; k++) {
							currentDenominator = currentDenominator + array[i][j][k];
						}
					}
				}
				for (int j = 0; j < names2.length; j++) {  // divide all values by current denominator
					mat[i][j] = mat[i][j] / currentDenominator;
				}
			}
		} else if (normalization.equals("prominence")) {
			integerBoolean = false;
			double currentDenominator;
			for (int i = 0; i < names2.length; i++) {
				currentDenominator = 0.0;
				if (qualifierAggregation.equals("ignore")) {  // iterate through rows of matrix and sum weighted values
					for (int j = 0; j < names1.length; j++) {
						currentDenominator = currentDenominator + mat[j][i];
					}
				} else if (qualifierAggregation.equals("combine")) {  // iterate through rows of matrix and count how many are larger than one
					System.err.println("Warning: Normalization and qualifier setting 'combine' yield results that cannot be interpreted.");
					for (int j = 0; j < names1.length; j++) {
						if (mat[i][j] > 0.0) {
							currentDenominator = currentDenominator + 1.0;
						}
					}
				} else if (qualifierAggregation.equals("subtract")) {  // iterate through array and sum for different levels
					for (int j = 0; j < names1.length; j++) {
						for (int k = 0; k < qualifierValues.length; k++) {
							currentDenominator = currentDenominator + array[j][i][k];
						}
					}
				}
				for (int j = 0; j < names1.length; j++) {  // divide all values by current denominator
					mat[j][i] = mat[j][i] / currentDenominator;
				}
			}
		}
		
		// create Matrix object and return
		Matrix matrix = new Matrix(mat, names1, names2, integerBoolean); // assemble the Matrix object with labels
		return matrix;
	}
	
	/**
	 * Retrieve the values across statements/documents given the name of the variable. 
	 * E.g., provide a list of statements, a list of documents, a variable name, and 
	 * information on whether the variable is defined at the document level (e.g., 
	 * author or section) or at the statement level (e.g., organization), and return 
	 * a one-dimensional array of values (e.g., the organization names or authors for 
	 * all statements provided.
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param documents             An {@link ArrayList} of {@link Document}s which contain the statements.
	 * @param variable              {@link String} denoting the first variable (containing the row values).
	 * @param documentLevel         {@link boolean} indicating whether the first variable is a document-level variable.
	 * @return                      String array of values.
	 */
	private String[] retrieveValues(ArrayList<Statement> statements, ArrayList<Document> documents, String variable, boolean documentLevel) {
		
		// HashMap for fast lookup of document indices by ID
		HashMap<Integer, Integer> docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}
		
		Statement s;
		String docAuthor, docSource, docSection, docType;
		String[] values = new String[statements.size()];
		for (int i = 0; i < statements.size(); i++) {
			s = statements.get(i);
			docAuthor = documents.get(docMap.get(s.getDocumentId())).getAuthor();
			docSource = documents.get(docMap.get(s.getDocumentId())).getSource();
			docSection = documents.get(docMap.get(s.getDocumentId())).getSection();
			docType = documents.get(docMap.get(s.getDocumentId())).getType();
			if (documentLevel == true) {
				if (variable.equals("author")) {
					values[i] = docAuthor;
				} else if (variable.equals("source")) {
					values[i] = docSource;
				} else if (variable.equals("section")) {
					values[i] = docSection;
				} else if (variable.equals("type")) {
					values[i] = docType;
				}
			} else {
				values[i] = (String) s.getValues().get(variable);
			}
		}
		
		return values;
	}
	
	/**
	 * Create a three-dimensional array (variable 1 x variable 2 x qualifier).
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param documents             An {@link ArrayList} of {@link Document}s which contain the statements.
	 * @param statementType         The {@link StatementType} corresponding to the statements.
	 * @param var1                  {@link String} denoting the first variable (containing the row values).
	 * @param var2                  {@link String} denoting the second variable (containing the columns values).
	 * @param var1Document          {@link boolean} indicating whether the first variable is a document-level variable.
	 * @param var2Document          {@link boolean} indicating whether the second variable is a document-level variable.
	 * @param names1                {@link String} array containing the row labels.
	 * @param names2                {@link String} array containing the column labels.
	 * @param qualifier             {@link String} denoting the name of the qualifier variable.
	 * @param qualifierAggregation  {@link String} indicating how different levels of the qualifier variable are aggregated. Valid values are "ignore", "subtract", and "combine".
	 * @return                      3D double array
	 */
	private double[][][] createArray(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, 
			String var1, String var2, boolean var1Document, boolean var2Document, String[] names1, String[] names2, String qualifier, 
			String qualifierAggregation) {
		
		int statementTypeId = statementType.getId();
		boolean booleanQualifier = true;  // is the qualifier boolean, rather than integer?
		if (statementType.getVariables().get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (booleanQualifier == true) {
			qualifierValues = new int[] {0, 1};
		} else {
			qualifierValues = Dna.data.getIntEntries(statementTypeId, qualifier);
		}

		// Create arrays with variable values
		String[] values1 = retrieveValues(statements, documents, var1, var1Document);
		String[] values2 = retrieveValues(statements, documents, var2, var2Document);
		
		// create and populate array
		double[][][] array = new double[names1.length][names2.length][qualifierValues.length]; // 3D array: rows x cols x qualifier value
		for (int i = 0; i < statements.size(); i++) {
			String n1 = values1[i];  // retrieve first value from statement
			String n2 = values2[i];  // retrieve second value from statement
			int q = (int) statements.get(i).getValues().get(qualifier);  // retrieve qualifier value from statement
			
			// find out which matrix row corresponds to the first value
			int row = -1;
			for (int j = 0; j < names1.length; j++) {
				if (names1[j].equals(n1)) {
					row = j;
					break;
				}
			}
			
			// find out which matrix column corresponds to the second value
			int col = -1;
			for (int j = 0; j < names2.length; j++) {
				if (names2[j].equals(n2)) {
					col = j;
					break;
				}
			}
			
			// find out which qualifier level corresponds to the qualifier value
			int qual = -1;  // qualifier level in the array
			for (int j = 0; j < qualifierValues.length; j++) {
				if (qualifierValues[j] == q) {
					qual = j;
					break;
				}
			}
			
			// add match to matrix (note that duplicates were dealt with at the statement filter stage)
			array[row][col][qual] = array[row][col][qual] + 1.0;
		}
		
		return array;
	}
	
	/**
	 * Lexical ranking of a binary vector.
	 * 
	 * Examples:
	 * 
	 * [0, 0] -> 0
	 * [0, 1] -> 1
	 * [1, 0] -> 2
	 * [1, 1] -> 3
	 * [0, 0, 1, 0, 1, 0] -> 10
	 * 
	 * This bijection is used to map combinations of qualifier values into edge weights in the resulting network matrix.
	 * 
	 * Source: https://cw.fel.cvut.cz/wiki/_media/courses/b4m33pal/pal06.pdf
	 * 
	 * @param binaryVector  A binary int array of arbitrary length, indicating which qualifier values are used in the dataset
	 * @return              An integer
	 */
	private int lexRank(int[] binaryVector) {
		int n = binaryVector.length;
		int r = 0;
		for (int i = 0; i < n; i++) {
			if (binaryVector[i] > 0) {
				r = r + (int) Math.pow(2, n - i - 1);
			}
		}
		return r;
	}
	
	/**
	 * This function is supposed to convert a rank back into a binary vector, but it does not seem to work properly.
	 * 
	 * Source: https://cw.fel.cvut.cz/wiki/_media/courses/b4m33pal/pal06.pdf
	 * 
	 * @param rank  The integer rank
	 * @param n     Length of the binary vector
	 * @return      Binary vector of length n
	 */
	/*
	private int[] lexUnrank(int rank, int n) {
		int[] binaryVector = new int[n];
		for (int i = n; i > 0; i--) {
			if (rank % 2 == 1) {
				binaryVector[i - 1] = 1;
				rank = (int) Math.floor(rank / 2);
			}
		}
		return binaryVector;
	}
	*/
	
	/**
	 * This function accepts a list of statements that should be included in the relational event export, 
	 * and it exports the variables of all statements to a CSV file, along with the statement ID and a 
	 * date/time stamp. There is one statement per row, and the number of columns is the number of variables 
	 * present in the statement type.
	 * 
	 * @param statements	 An array list of {@link Statement}s (of the same statement type) that should be exported.
	 * @param documents      An array list of {@link Document}s in which the statements are embedded.
	 * @param statementType  The statement type corresponding to the statements.
	 * @param fileName		 String with the file name of the CSV file to which the event list will be exported.
	 */
	private void eventCSV(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType, String fileName) {
		String key, value;
		int statementId;
		Date d;
		SimpleDateFormat dateFormat;
		int statementTypeId = statementType.getId();
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getStatementTypeId() != statementTypeId) {
				throw new IllegalArgumentException("More than one statement type was selected. Cannot export to a spreadsheet!");
			}
		}

		// HashMap for fast lookup of document indices by ID
		HashMap<Integer, Integer> docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}
		
		HashMap<String, String> variables = statementType.getVariables();
		Iterator<String> keyIterator;
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
			keyIterator = variables.keySet().iterator();
			out.write("\"statement ID\";\"time\";\"document ID\";\"document title\";\"author\";\"source\";\"section\";\"type\";\"text\"");
			while (keyIterator.hasNext()){
				out.write(";\"" + keyIterator.next() + "\"");
			}
			for (int i = 0; i < statements.size(); i++) {
				out.newLine();
				statementId = statements.get(i).getId();
				String stringId = new Integer(statementId).toString();
				out.write(stringId);
				d = statements.get(i).getDate();
				dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				out.write(";" + dateFormat.format(d));
				Document doc = documents.get(docMap.get(statements.get(i).getDocumentId()));
				out.write(";" + doc.getId());
				out.write(";\"" + doc.getTitle().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getAuthor().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getSource().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getSection().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getType().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getText().substring(statements.get(i).getStart(), 
						statements.get(i).getStop()).replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				keyIterator = variables.keySet().iterator();
				while (keyIterator.hasNext()){
					key = keyIterator.next();
					value = variables.get(key);
					if (value.equals("short text") || value.equals("long text")) {
						out.write(";\"" + ((String) statements.get(i).getValues().get(key)).replaceAll(";", ",").replaceAll("\"", "'") + "\"");
					} else if (value.equals("boolean") || value.equals("integer")) {
						out.write(";" + statements.get(i).getValues().get(key));
					}
				}
			}
			out.close();
			System.out.println("Event list has been exported to \"" + fileName + "\".");
		} catch (IOException e) {
			System.err.println("Error while saving CSV file: " + e);
		}
	}

	/**
	 * Export {@link Matrix} to a CSV matrix file.
	 * 
	 * @param matrix   The input {@link Matrix} object.
	 * @param outfile  The path and file name of the target CSV file.
	 */
	private void exportCSV (Matrix matrix, String outfile) {
		String[] rn = matrix.getRownames();
		String[] cn = matrix.getColnames();
		int nr = rn.length;
		int nc = cn.length;
		double[][] mat = matrix.getMatrix();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("\"\"");
			for (int i = 0; i < nc; i++) {
				out.write(";\"" + cn[i].replaceAll("\"", "'") + "\"");
			}
			for (int i = 0; i < nr; i++) {
				out.newLine();
				out.write("\"" + rn[i].replaceAll("\"", "'") + "\"");
				for (int j = 0; j < nc; j++) {
					if (matrix.getInteger() == true) {
						out.write(";" + (int) mat[i][j]);
					} else {
						out.write(";" + String.format(new Locale("en"), "%.6f", mat[i][j]));  // six decimal places
					}
				}
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while saving CSV matrix file.");
		}
	}
	
	/**
	 * Export network to a DL fullmatrix file for the software UCINET.
	 * 
	 * @param matrix   The input {@link Matrix} object.
	 * @param outfile  The path and file name of the target .dl file.
	 * @param twoMode  A {@link boolean} indicating if the input matrix is a two-mode network matrix (rather than one-mode). 
	 */
	public void exportDL (Matrix matrix, String outfile, boolean twoMode) {
		String[] rn = matrix.getRownames();
		String[] cn = matrix.getColnames();
		int nr = rn.length;
		int nc = cn.length;
		double[][] mat = matrix.getMatrix();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("dl ");
			if (twoMode == false) {
				out.write("n = " + nr);
			} else if (twoMode == true) {
				out.write("nr = " + nr + ", nc = " + nc);
			}
			out.write(", format = fullmatrix");
			out.newLine();
			if (twoMode == true) {
				out.write("row labels:");
			} else {
				out.write("labels:");
			}
			for (int i = 0; i < nr; i++) {
				out.newLine();
				out.write("\"" + rn[i].replaceAll("\"", "'").replaceAll("'", "") + "\"");
			}
			if (twoMode == true) {
				out.newLine();
				out.write("col labels:");
				for (int i = 0; i < nc; i++) {
					out.newLine();
					out.write("\"" + cn[i].replaceAll("\"", "'").replaceAll("'", "") + "\"");
				}
			}
			out.newLine();
			out.write("data:");
			for (int i = 0; i < nr; i++) {
				out.newLine();
				for (int j = 0; j < nc; j++) {
					if (matrix.getInteger() == true) {
						out.write(" " + (int) mat[i][j]);
					} else {
						out.write(" " + String.format(new Locale("en"), "%.6f", mat[i][j]));
					}
				}
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while saving DL fullmatrix file.");
		}
	}
	
	/**
	 * Count how often a value is used across the range of filtered statements.
	 * 
	 * @param variableValues  String array of all values of a certain variable in a set of statements.
	 * @param uniqueNames     String array of unique values of the same variable across all statements.
	 * @return                int array of value frequencies for each unique value in same order as uniqueNames.
	 */
	private int[] countFrequencies(String[] variableValues, String[] uniqueNames) {
		int[] frequencies = new int[uniqueNames.length];
		for (int i = 0; i < uniqueNames.length; i++) {
			for (int j = 0; j < variableValues.length; j++) {
				if (uniqueNames[i].equals(variableValues[j])) {
					frequencies[i] = frequencies[i] + 1;
				}
			}
		}
		return frequencies;
	}
	
	/**
	 * Export filter for graphML files.
	 * 
	 * @param matrix                 Input {@link Matrix}.
	 * @param twoMode                Indicates whether the network is a two-mode network.
	 * @param statementType          The statement type on which the network is based.
	 * @param outfile                Name of the output file.
	 * @param var1                   Name of the first variable (the rows of the matrix).
	 * @param var2                   Name of the second variable (the columns of the matrix).
	 * @param frequencies1           The number of statements in which the row node is involved (after filtering).
	 * @param frequencies2           The number of statements in which the column node is involved (after filtering).
	 * @param attributes             An ArrayList of {@link AttributeVector}s containing all attribute vectors in the database.
	 * @param qualifierAggregation   A String denoting the qualifier aggregation. Valid values are "ignore", "combine", "subtract", "congruence", and "conflict".
	 * @param qualifierBinary        Indicates whether the qualifier is a binary variable.
	 */
	private void exportGraphml(Matrix matrix, boolean twoMode, StatementType statementType, String outfile, 
			String var1, String var2, int[] frequencies1, int[] frequencies2, ArrayList<AttributeVector> attributes, 
			String qualifierAggregation, boolean qualifierBinary) {
		
		// extract attributes
		String[] rn = matrix.getRownames();
		String[] cn = matrix.getColnames();
		String[] names;
		String[] variables;
		int[] frequencies;
		if (twoMode == true) {
			names = new String[rn.length + cn.length];
			variables = new String[names.length];
			frequencies = new int[names.length];
		} else {
			names = new String[rn.length];
			variables = new String[rn.length];
			frequencies = new int[rn.length];
		}
		for (int i = 0; i < rn.length; i++) {
			names[i] = rn[i];
			variables[i] = var1;
			frequencies[i] = frequencies1[i];
		}
		if (twoMode == true) {
			for (int i = 0; i < cn.length; i++) {
				names[i + rn.length] = cn[i];
				variables[i + rn.length] = var2;
				frequencies[i + rn.length] = frequencies2[i];
			}
		}
		int[] id = new int[names.length];
		String[] color = new String[names.length];
		String[] type = new String[names.length];
		String[] alias = new String[names.length];
		String[] notes = new String[names.length];
		for (int i = 0; i < attributes.size(); i++) {
			if (attributes.get(i).getStatementTypeId() == statementType.getId() && attributes.get(i).getVariable().equals(var1)) {
				for (int j = 0; j < rn.length; j++) {
					if (rn[j].equals(attributes.get(i).getValue())) {
						id[j] = attributes.get(i).getId();
						color[j] = String.format("#%02X%02X%02X", attributes.get(i).getColor().getRed(), 
								attributes.get(i).getColor().getGreen(), attributes.get(i).getColor().getBlue());
						type[j] = attributes.get(i).getType();
						alias[j] = attributes.get(i).getAlias();
						notes[j] = attributes.get(i).getNotes();
					}
				}
			} else if (attributes.get(i).getStatementTypeId() == statementType.getId() && attributes.get(i).getVariable().equals(var2) && twoMode == true) {
				for (int j = 0; j < cn.length; j++) {
					if (cn[j].equals(attributes.get(i).getValue())) {
						id[j + rn.length] = attributes.get(i).getId();
						color[j + rn.length] = String.format("#%02X%02X%02X", attributes.get(i).getColor().getRed(), 
								attributes.get(i).getColor().getGreen(), attributes.get(i).getColor().getBlue());
						type[j + rn.length] = attributes.get(i).getType();
						alias[j + rn.length] = attributes.get(i).getAlias();
						notes[j + rn.length] = attributes.get(i).getNotes();
					}
				}
			}
		}
		
		// set up graph structure
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
		org.jdom.Document document = new org.jdom.Document(graphml);
		
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

		Element keyName = new Element("key", xmlns);
		keyName.setAttribute(new Attribute("id", "name"));
		keyName.setAttribute(new Attribute("for", "node"));
		keyName.setAttribute(new Attribute("attr.name", "name"));
		keyName.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyName);
		
		Element keyType = new Element("key", xmlns);
		keyType.setAttribute(new Attribute("id", "type"));
		keyType.setAttribute(new Attribute("for", "node"));
		keyType.setAttribute(new Attribute("attr.name", "type"));
		keyType.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyType);
		
		Element keyAlias = new Element("key", xmlns);
		keyAlias.setAttribute(new Attribute("id", "alias"));
		keyAlias.setAttribute(new Attribute("for", "node"));
		keyAlias.setAttribute(new Attribute("attr.name", "alias"));
		keyAlias.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyAlias);
		
		Element keyNote = new Element("key", xmlns);
		keyNote.setAttribute(new Attribute("id", "note"));
		keyNote.setAttribute(new Attribute("for", "node"));
		keyNote.setAttribute(new Attribute("attr.name", "note"));
		keyNote.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyNote);
		
		Element keyVariable = new Element("key", xmlns);
		keyVariable.setAttribute(new Attribute("id", "variable"));
		keyVariable.setAttribute(new Attribute("for", "node"));
		keyVariable.setAttribute(new Attribute("attr.name", "variable"));
		keyVariable.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyVariable);

		Element keyFrequency = new Element("key", xmlns);
		keyFrequency.setAttribute(new Attribute("id", "frequency"));
		keyFrequency.setAttribute(new Attribute("for", "node"));
		keyFrequency.setAttribute(new Attribute("attr.name", "frequency"));
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
		
		graphElement.setAttribute(new Attribute("id", "DNA"));
		int numEdges = rn.length * cn.length;
		if (twoMode == false) {
			numEdges = (numEdges / 2) - rn.length;
		}
		int numNodes = rn.length;
		if (twoMode == true) {
			numNodes = numNodes + cn.length;
		}
		graphElement.setAttribute(new Attribute("parse.edges", String.valueOf(numEdges)));
		graphElement.setAttribute(new Attribute("parse.nodes", String.valueOf(numNodes)));
		graphElement.setAttribute(new Attribute("parse.order", "free"));
		Element properties = new Element("data", xmlns);
		properties.setAttribute(new Attribute("key", "prop"));
		Element labelAttribute = new Element("labelAttribute", visone);
		labelAttribute.setAttribute("edgeLabel", "weight");
		labelAttribute.setAttribute("nodeLabel", "name");
		properties.addContent(labelAttribute);
		graphElement.addContent(properties);
		
		// add nodes
		Comment nodes = new Comment(" nodes ");
		graphElement.addContent(nodes);
		
		for (int i = 0; i < names.length; i++) {
			Element node = new Element("node", xmlns);
			node.setAttribute(new Attribute("id", "n" + id[i]));
			
			Element idElement = new Element("data", xmlns);
			idElement.setAttribute(new Attribute("key", "id"));
			idElement.setText(String.valueOf(id[i]));
			node.addContent(idElement);
			
			Element nameElement = new Element("data", xmlns);
			nameElement.setAttribute(new Attribute("key", "name"));
			nameElement.setText(names[i]);
			node.addContent(nameElement);
			
			Element typeElement = new Element("data", xmlns);
			typeElement.setAttribute(new Attribute("key", "type"));
			typeElement.setText(type[i]);
			node.addContent(typeElement);
			
			Element aliasElement = new Element("data", xmlns);
			aliasElement.setAttribute(new Attribute("key", "alias"));
			aliasElement.setText(alias[i]);
			node.addContent(aliasElement);
			
			Element notesElement = new Element("data", xmlns);
			notesElement.setAttribute(new Attribute("key", "notes"));
			notesElement.setText(notes[i]);
			node.addContent(notesElement);
			
			Element variableElement = new Element("data", xmlns);
			variableElement.setAttribute(new Attribute("key", "variable"));
			variableElement.setText(variables[i]);
			node.addContent(variableElement);
			
			Element frequency = new Element("data", xmlns);
			frequency.setAttribute(new Attribute("key", "frequency"));
			frequency.setText(String.valueOf(frequencies[i]));
			node.addContent(frequency);
			
			Element vis = new Element("data", xmlns);
			vis.setAttribute(new Attribute("key", "d0"));
			Element visoneShapeNode = new Element("shapeNode", visone);
			Element yShapeNode = new Element("ShapeNode", yNs);
			Element geometry = new Element("Geometry", yNs);
			geometry.setAttribute(new Attribute("height", "20.0"));
			geometry.setAttribute(new Attribute("width", "20.0"));
			geometry.setAttribute(new Attribute("x", String.valueOf(Math.random() * 800)));
			geometry.setAttribute(new Attribute("y", String.valueOf(Math.random() * 600)));
			yShapeNode.addContent(geometry);
			Element fill = new Element("Fill", yNs);
			fill.setAttribute(new Attribute("color", color[i]));

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
			nodeLabel.setText(names[i]);
			yShapeNode.addContent(nodeLabel);
			
			Element shape = new Element("Shape", yNs);
			if (i < rn.length) {
				shape.setAttribute(new Attribute("type", "ellipse"));
			} else {
				shape.setAttribute(new Attribute("type", "roundrectangle"));
			}
			yShapeNode.addContent(shape);
			visoneShapeNode.addContent(yShapeNode);
			vis.addContent(visoneShapeNode);
			node.addContent(vis);
			
			graphElement.addContent(node);
		}
		
		// add edges
		double[][] m = matrix.getMatrix();
		Comment edges = new Comment(" edges ");
		graphElement.addContent(edges);
		for (int i = 0; i < rn.length; i++) {
			for (int j = 0; j < cn.length; j++) {
				if (m[i][j] != 0.0 && (twoMode == true || (twoMode == false && i < j))) {  // only lower triangle is used for one-mode networks
					Element edge = new Element("edge", xmlns);
					
					int currentId = id[i];
					edge.setAttribute(new Attribute("source", "n" + String.valueOf(currentId)));
					if (twoMode == true) {
						currentId = id[j + rn.length];
					} else {
						currentId = id[j];
					}
					edge.setAttribute(new Attribute("target", "n" + String.valueOf(currentId)));
					
					Element weight = new Element("data", xmlns);
					weight.setAttribute(new Attribute("key", "weight"));
					weight.setText(String.valueOf(m[i][j]));
					edge.addContent(weight);

					Element visEdge = new Element("data", xmlns);
					visEdge.setAttribute("key", "e0");
					Element visPolyLineEdge = new Element("polyLineEdge", visone);
					Element yPolyLineEdge = new Element("PolyLineEdge", yNs);

					Element yLineStyle = new Element("LineStyle", yNs);
					if (qualifierAggregation.equals("combine") && qualifierBinary == true) {
						if (m[i][j] == 1.0) {
							yLineStyle.setAttribute("color", "#00ff00");
						} else if (m[i][j] == 2.0) {
							yLineStyle.setAttribute("color", "#ff0000");
						} else if (m[i][j] == 3.0) {
							yLineStyle.setAttribute("color", "#0000ff");
						}
					} else if (qualifierAggregation.equals("subtract")) {
						if (m[i][j] < 0) {
							yLineStyle.setAttribute("color", "#ff0000");
						} else if (m[i][j] > 0) {
							yLineStyle.setAttribute("color", "#00ff00");
						}
					} else if (qualifierAggregation.equals("conflict")) {
						yLineStyle.setAttribute("color", "#ff0000");
					} else if (qualifierAggregation.equals("congruence")) {
						yLineStyle.setAttribute("color", "#00ff00");
					} else {
						yLineStyle.setAttribute("color", "#000000");
					}
					yLineStyle.setAttribute(new Attribute("type", "line"));
					yLineStyle.setAttribute(new Attribute("width", "2.0"));
					yPolyLineEdge.addContent(yLineStyle);
					visPolyLineEdge.addContent(yPolyLineEdge);
					visEdge.addContent(visPolyLineEdge);
					edge.addContent(visEdge);
					
					graphElement.addContent(edge);
				}
			}
		}
		
		graphml.addContent(graphElement);
		
		// write to file
		File dnaFile = new File(outfile);
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
		}
	}
	
	/**
	 * Compute one-mode or two-mode network matrix based on R arguments.
	 * 
	 * @param networkType            The network type as provided by rDNA (can be 'eventlist', 'twomode', or 'onemode')
	 * @param statementType          Statement type as a String
	 * @param variable1              First variable for export, provided as a String
	 * @param variable1Document      boolean indicating if the first variable is at the document level
	 * @param variable2              Second variable for export, provided as a String
	 * @param variable2Document      boolean indicating if the second variable is at the document level
	 * @param qualifier              Qualifier variable as a String
	 * @param qualifierAggregation   Aggregation rule for the qualifier variable (can be 'ignore', 'combine', 'subtract', 'congruence', or 'conflict')
	 * @param normalization          Normalization setting as a String, as provided by rDNA (can be 'no', 'activity', 'prominence', 'average', 'Jaccard', or 'cosine')
	 * @param includeIsolates        boolean indicating whether nodes not currently present should still be inserted into the network matrix
	 * @param duplicates             An input String from rDNA that can be 'include', 'document', 'week', 'month', 'year', or 'acrossrange'
	 * @param startDate              Start date for the export, provided as a String with format "dd.MM.yyyy"
	 * @param stopDate               Stop date for the export, provided as a String with format "dd.MM.yyyy"
	 * @param startTime              Start time for the export, provided as a String with format "HH:mm:ss"
	 * @param stopTime               Stop time for the export, provided as a String with format "HH:mm:ss"
	 * @param excludeVariables       A String array with n elements, indicating the variable of the n'th value
	 * @param excludeValues          A String array with n elements, indicating the value pertaining to the n'th variable String
	 * @param excludeAuthors         A String array of values to exclude in the 'author' variable at the document level
	 * @param excludeSources         A String array of values to exclude in the 'source' variable at the document level
	 * @param excludeSections        A String array of values to exclude in the 'section' variable at the document level
	 * @param excludeTypes           A String array of values to exclude in the 'type' variable at the document level
	 * @param invertValues           boolean indicating whether the statement-level exclude values should be included (= true) rather than excluded
	 * @param invertAuthors          boolean indicating whether the document-level author values should be included (= true) rather than excluded
	 * @param invertSources          boolean indicating whether the document-level source values should be included (= true) rather than excluded
	 * @param invertSections         boolean indicating whether the document-level section values should be included (= true) rather than excluded
	 * @param invertTypes            boolean indicating whether the document-level type values should be included (= true) rather than excluded
	 * @param verbose                Report progress to the console?
	 * @return                       A Matrix object containing the resulting one-mode or two-mode network
	 */
	public void rNetwork(String networkType, String statementType, String variable1, boolean variable1Document, String variable2, 
			boolean variable2Document, String qualifier, String qualifierAggregation, String normalization, boolean includeIsolates, 
			String duplicates, String startDate, String stopDate, String startTime, String stopTime, String[] excludeVariables, 
			String[] excludeValues, String[] excludeAuthors, String[] excludeSources, String[] excludeSections, 
			String[] excludeTypes, boolean invertValues, boolean invertAuthors, boolean invertSources, boolean invertSections, 
			boolean invertTypes, boolean verbose) {
		
		// step 1: preprocess arguments
		int max = 4;
		if (networkType.equals("eventlist")) {
			max = 3;
		}
		if (verbose == true) {
			System.out.print("(1/" + max + "): Processing network options... ");
		}
		networkType = formatNetworkType(networkType);
		StatementType st = processStatementType(networkType, statementType, variable1, variable2, qualifier, qualifierAggregation);
		boolean ignoreQualifier = qualifier.equals("ignore");
		int statementTypeId = st.getId();
		normalization = formatNormalization(networkType, normalization);
		duplicates = formatDuplicates(duplicates);
		Date start = formatDate(startDate, startTime);
		Date stop = formatDate(stopDate, stopTime);
		
		HashMap<String, ArrayList<String>> map = processExcludeVariables(excludeVariables, excludeValues, invertValues, data.getStatements(), 
				data.getStatements(), data.getDocuments(), statementTypeId, includeIsolates);
		ArrayList<String> authorExclude = processExcludeDocument("author", excludeAuthors, invertAuthors, data.getStatements(), data.getStatements(), 
				data.getDocuments(), statementTypeId, includeIsolates);
		ArrayList<String> sourceExclude = processExcludeDocument("source", excludeSources, invertSources, data.getStatements(), data.getStatements(),  
				data.getDocuments(), statementTypeId, includeIsolates);
		ArrayList<String> sectionExclude = processExcludeDocument("section", excludeSections, invertSections, data.getStatements(), data.getStatements(), 
				data.getDocuments(), statementTypeId, includeIsolates);
		ArrayList<String> typeExclude = processExcludeDocument("type", excludeTypes, invertTypes, data.getStatements(), data.getStatements(), 
				data.getDocuments(), statementTypeId, includeIsolates);
		if (verbose == true) {
			System.out.print("Done.\n");
		}
		
		// step 2: filter
		boolean filterEmptyFields = true;
		if (networkType.equals("Event list")) {
			filterEmptyFields = false;
		}
		if (verbose == true) {
			System.out.print("(2/" + max + "): Filtering statements...\n");
		}
		this.filteredStatements = filter(data.getStatements(), data.getDocuments(), start, stop, st, variable1, variable2, 
				variable1Document, variable2Document, qualifier, ignoreQualifier, duplicates, authorExclude, sourceExclude, sectionExclude, 
				typeExclude, map, filterEmptyFields);
		if (verbose == true) {
			System.out.print(this.filteredStatements.size() + " out of " + data.getStatements().size() + " statements retained.\n");
		}
		
		// step 3: compile node labels
		String[] names1 = null;
		String[] names2 = null;
		if (!networkType.equals("Event list")) {
			if (verbose == true) {
				System.out.print("(3/" + max + "): Compiling node labels... ");
			}
			names1 = extractLabels(this.filteredStatements, data.getStatements(), data.getDocuments(), variable1, variable1Document, 
					statementTypeId, includeIsolates);
			names2 = extractLabels(this.filteredStatements, data.getStatements(), data.getDocuments(), variable2, variable2Document, 
					statementTypeId, includeIsolates);
			if (verbose == true) {
				System.out.print(names1.length + " entries for the first and " + names2.length + " entries for the second variable.\n");
			}
		}
		
		// step 4: create matrix
		if (verbose == true) {
			int step = 4;
			if (networkType.equals("Event list")) {
				step = 3;
			}
			System.out.print("(" + step + "/" + max + "): Computing network matrix... ");
		}
		Matrix m = null;
		if (networkType.equals("Two-mode network")) {
			m = computeTwoModeMatrix(filteredStatements, data.getDocuments(), st, variable1, variable2, variable1Document, 
					variable2Document, names1, names2, qualifier, qualifierAggregation, normalization);
			this.matrix = m;
		} else if (networkType.equals("One-mode network")) {
			m = computeOneModeMatrix(filteredStatements, data.getDocuments(), st, variable1, variable2, variable1Document, 
					variable2Document, names1, names2, qualifier, qualifierAggregation, normalization);
			this.matrix = m;
		} else if (networkType.equals("Event list")) {
			this.matrix = null;
			this.eventListColumnsR = eventListR(filteredStatements, data.getDocuments(), st);
		}
		if (verbose == true) {
			System.out.print("Done.\n");
		}
	}

	/**
	 * This function accepts a list of statements that should be included in the relational event export, 
	 * and it returns the variables of all statements, along with the statement ID and a date/time stamp. 
	 * There is one statement per row, and the number of columns is the number of variables present in 
	 * the statement type plus 8 columns that represent statement ID and document-level variables.
	 * 
	 * @param statements	 An array list of {@link Statement}s (of the same statement type) that should be exported.
	 * @param documents      An array list of {@link Document}s in which the statements are embedded.
	 * @param statementType  The statement type corresponding to the statements.
	 */
	private Object[] eventListR(ArrayList<Statement> statements, ArrayList<Document> documents, StatementType statementType) {
		String key, value;
		Document doc;
		int statementTypeId = statementType.getId();
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getStatementTypeId() != statementTypeId) {
				throw new IllegalArgumentException("More than one statement type was selected. Cannot export to a spreadsheet!");
			}
		}

		// HashMap for fast lookup of document indices by ID
		HashMap<Integer, Integer> docMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}
		
		// Get variable names and types of current statement type
		HashMap<String, String> variables = statementType.getVariables();
		Iterator<String> keyIterator;
		ArrayList<String> variableNames = new ArrayList<String>();
		ArrayList<String> variableTypes = new ArrayList<String>();
		keyIterator = variables.keySet().iterator();
		while (keyIterator.hasNext()){
			key = keyIterator.next();
			value = variables.get(key);
			variableNames.add(key);
			variableTypes.add(value);
		}
		columnNames = new String[variableNames.size()];
		columnTypes = new String[variableTypes.size()];
		for (int i = 0; i < variableNames.size(); i++) {
			columnNames[i] = variableNames.get(i);
			columnTypes[i] = variableTypes.get(i);
		}
		
		// create array of columns and populate document-level and statement-level columns; leave out variables for now
		Object[] columns = new Object[variableNames.size() + 8];
		int[] ids = new int[statements.size()];
		long[] time = new long[statements.size()];
		int[] docId = new int[statements.size()];
		String[] docTitle = new String[statements.size()];
		String[] author = new String[statements.size()];
		String[] source = new String[statements.size()];
		String[] section = new String[statements.size()];
		String[] type = new String[statements.size()];
		for (int i = 0; i < statements.size(); i++) {
			ids[i] = statements.get(i).getId();
			time[i] = statements.get(i).getDate().getTime() / 1000;  // convert milliseconds to seconds (since 1/1/1970)
			docId[i] = statements.get(i).getDocumentId();
			doc = documents.get(docMap.get(docId[i]));
			docTitle[i] = doc.getTitle();
			author[i] = doc.getAuthor();
			source[i] = doc.getSource();
			section[i] = doc.getSection();
			type[i] = doc.getType();
		}
		columns[0] = ids;
		columns[1] = time;
		columns[2] = docId;
		columns[3] = docTitle;
		columns[4] = author;
		columns[5] = source;
		columns[6] = section;
		columns[7] = type;
		
		// Now add the variables to the columns array
		for (int i = 0; i < variableNames.size(); i++) {
			if (columnTypes[i].equals("short text") || columnTypes[i].equals("long text")) {
				columns[i + 8] = new String[statements.size()];
			} else {
				columns[i + 8] = new int[statements.size()];
			}
		}
		for (int i = 0; i < statements.size(); i++) {
			for (int j = 0; j < variableNames.size(); j++) {
				if (columnTypes[j].equals("short text") || columnTypes[j].equals("long text")) {
					String[] temp = ((String[]) columns[j + 8]);
					temp[i] = (String) statements.get(i).getValues().get(columnNames[j]);
					columns[j + 8] = temp;
				} else {
					int[] temp = ((int[]) columns[j + 8]);
					temp[i] = (int) statements.get(i).getValues().get(columnNames[j]);
					columns[j + 8] = temp;
				}
			}
		}
		
		return columns;
	}

	/**
	 * Return variable names in this.eventListColumnsR
	 * 
	 * @return   array of Strings with variable names
	 */
	public String[] getEventListColumnsRNames() {
		return columnNames;
	}

	/**
	 * Return variable types in this.eventListColumnsR
	 * 
	 * @return   array of Strings with variable types
	 */
	public String[] getEventListColumnsRTypes() {
		return columnTypes;
	}
	
	/**
	 * Return Object[] from this.eventListColumnsR
	 * 
	 * @return   array of array of different data types, which represent the columns
	 */
	public Object[] getEventListColumnsR() {
		return eventListColumnsR;
	}
	
	/**
	 * Return double[][] from this.matrix.
	 * 
	 * @return   network matrix
	 */
	public double[][] getMatrix() {
		return matrix.getMatrix();
	}
	
	/**
	 * Return row names from this.matrix.
	 * 
	 * @return   String array of node names for the row variable.
	 */
	public String[] getRowNames() {
		return matrix.getRownames();
	}
	
	/**
	 * Return column names from this.matrix.
	 * 
	 * @return   String array of node names for the column variable.
	 */
	public String[] getColumnNames() {
		return matrix.getColnames();
	}
	
	/**
	 * Save an array of AttributeVector objects to the Exporter class.
	 * 
	 * @param variable              The variable for which the attributes should be retrieved.
	 * @param statementTypeString   The statement type (given as a string) to which the variable belongs.
	 * @param values                String array of value names for which the attributes should be saved.
	 */
	public void rAttributes(String variable, String statementTypeString, String[] values) {
		
		// get statement type ID and all attribute vectors
		int statementTypeId = this.data.getStatementType(statementTypeString).getId();
		AttributeVector[] av = this.data.getAttributes(variable, statementTypeId);
		
		// extract full set of labels in alphabetical order if no names vector is provided
		if (values == null || values.length == 0) {
			values = extractLabels(this.filteredStatements, data.getStatements(), data.getDocuments(), variable, false, statementTypeId, true);
		}
		
		// extract only those attribute vectors that match the names vector
		AttributeVector[] at = new AttributeVector[values.length];
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < av.length; j++) {
				if (values[i].equals(av[j].getValue())) {
					at[i] = av[j];
				}
			}
		}
		
		this.attributes = at;
	}
	
	/**
	 * Get attribute IDs
	 * 
	 * @return integer array of attribute IDs
	 */
	public int[] getAttributeIds() {
		int[] ids = new int[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			ids[i] = this.attributes[i].getId();
		}
		return ids;
	}
	
	/**
	 * Get attribute values
	 * 
	 * @return String array of attribute values
	 */
	public String[] getAttributeValues() {
		String[] values = new String[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			values[i] = this.attributes[i].getValue();
		}
		return values;
	}
	
	/**
	 * Get attribute colors
	 * 
	 * @return String array of attribute colors
	 */
	public String[] getAttributeColors() {
		String[] colors = new String[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			colors[i] = String.format("#%02X%02X%02X", this.attributes[i].getColor().getRed(), 
					this.attributes[i].getColor().getGreen(), this.attributes[i].getColor().getBlue());
		}
		return colors;
	}
	
	/**
	 * Get attribute types
	 * 
	 * @return String array of attribute types
	 */
	public String[] getAttributeTypes() {
		String[] types = new String[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			types[i] = this.attributes[i].getType();
		}
		return types;
	}
	
	/**
	 * Get attribute aliases
	 * 
	 * @return String array of attribute aliases
	 */
	public String[] getAttributeAlias() {
		String[] alias = new String[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			alias[i] = this.attributes[i].getAlias();
		}
		return alias;
	}
	
	/**
	 * Get attribute notes
	 * 
	 * @return   String array of attribute notes
	 */
	public String[] getAttributeNotes() {
		String[] notes = new String[attributes.length];
		for (int i = 0; i < this.attributes.length; i++) {
			notes[i] = this.attributes[i].getNotes();
		}
		return notes;
	}
	
	/**
	 * Get boolean array indicating if each value is in the dataset
	 * 
	 * @return boolean array indicating if each attribute value is currently in the dataset
	 */
	public boolean[] getAttributeInDataset(String statementTypeString) {
		boolean[] inDataset = new boolean[attributes.length];
		int statementTypeId = this.data.getStatementType(statementTypeString).getId();
		boolean current = false;
		for (int i = 0; i < this.attributes.length; i++) {
			String variable = this.attributes[i].getVariable();
			String value = this.attributes[i].getValue();
			current = false;
			for (int j = 0; j < this.data.getStatements().size(); j++) {
				if (this.data.getStatements().get(j).getStatementTypeId() == statementTypeId 
						&& this.data.getStatements().get(j).getValues().get(variable).equals(value) ) {
					current = true;
					break;
				}
			}
			inDataset[i] = current;
		}
		return inDataset;
	}

	/**
	 * Get boolean array indicating if each value is in the exported network
	 * 
	 * @return boolean array indicating if each attribute value is currently in the network
	 */
	public boolean[] getAttributeInNetwork(String statementTypeString) {
		boolean[] inNetwork = new boolean[attributes.length];
		int statementTypeId = this.data.getStatementType(statementTypeString).getId();
		boolean current = false;
		for (int i = 0; i < this.attributes.length; i++) {
			String variable = this.attributes[i].getVariable();
			String value = this.attributes[i].getValue();
			current = false;
			for (int j = 0; j < this.filteredStatements.size(); j++) {
				if (this.filteredStatements.get(j).getStatementTypeId() == statementTypeId 
						&& this.filteredStatements.get(j).getValues().get(variable).equals(value) ) {
					current = true;
					break;
				}
			}
			inNetwork[i] = current;
		}
		return inNetwork;
	}
	
	/**
	 * Get attribute value frequencies
	 * 
	 * @return   integer array of frequencies (i.e., how often was a node active in the list of filtered statements?)
	 */
	public int[] getAttributeFrequencies(String variable) {
		String[] values = retrieveValues(this.filteredStatements, data.getDocuments(), variable, false);
		int[] frequencies = countFrequencies(values, this.getAttributeValues());
		return frequencies;
	}
	
	/**
	 * Format the R argument 'networkType' (can be 'eventlist', 'twomode', or 'onemode') to 'Event list', 'Two-mode network', or 'One-mode network'.
	 * 
	 * @param networkType   R argument
	 * @return              formatted string
	 */
	private String formatNetworkType(String networkType) {
		if (networkType.equals("eventlist")) {
			networkType = "Event list";
		} else if (networkType.equals("twomode")) {
			networkType = "Two-mode network";
		} else if (networkType.equals("onemode")) {
			networkType = "One-mode network";
		} else {
			System.err.println("Network type was not recognized. Use 'twomode', 'onemode', or 'eventlist'.");
		}
		return networkType;
	}
	
	/**
	 * Check if variables and statement type (provided as a String) are valid and return statement type.
	 * 
	 * @param networkType            Java-DNA-formatted network type String 
	 * @param statementType          Statement type given as a String
	 * @param variable1              First variable as a String
	 * @param variable2              Second variable as a String
	 * @param qualifier              Qualifier variable as a String
	 * @param qualifierAggregation   Qualifier aggregation rule as a String ('ignore', 'combine', 'subtract', 'congruence', or 'conflict')
	 * @return                       StatementType to be used
	 */
	private StatementType processStatementType(String networkType, String statementType, String variable1, String variable2, String qualifier, String qualifierAggregation) {
		StatementType st = data.getStatementType(statementType);
		if (st == null) {
			System.err.println("Statement type '" + statementType + " does not exist!");
		}
		
		if (!st.getVariables().containsKey(variable1)) {
			System.err.println("Variable 1 ('" + variable1 + "') does not exist in this statement type.");
		}
		if (!st.getVariables().get(variable1).equals("short text")) {
			System.err.println("Variable 1 ('" + variable1 + "') is not a short text variable.");
		}
		
		if (!st.getVariables().containsKey(variable2)) {
			System.err.println("Variable 2 ('" + variable2 + "') does not exist in this statement type.");
		}
		if (!st.getVariables().get(variable2).equals("short text")) {
			System.err.println("Variable 2 ('" + variable2 + "') is not a short text variable.");
		}
		
		if (!st.getVariables().containsKey(qualifier)) {
			System.err.println("The qualifier variable ('" + qualifier + "') does not exist in this statement type.");
		}
		if (!st.getVariables().get(qualifier).equals("boolean") && !st.getVariables().get(qualifier).equals("integer")) {
			System.err.println("The qualifier variable ('" + qualifier + "') is not a boolean or integer variable.");
		}
		
		if (!qualifierAggregation.equals("ignore") && !qualifierAggregation.equals("subtract") && !qualifierAggregation.equals("combine")
				&& !qualifierAggregation.equals("congruence") && !qualifierAggregation.equals("conflict")) {
			System.err.println("'qualifierAggregation' must be 'ignore', 'combine', 'subtract', 'congruence', or 'conflict'.");
		}
		if (qualifierAggregation.equals("combine") && !networkType.equals("Two-mode network")) {
			System.err.println("qualifierAggregation = 'combine' is only possible with two-mode networks.");
		}
		if (qualifierAggregation.equals("congruence") && !networkType.equals("One-mode network")) {
			System.err.println("qualifierAggregation = 'congruence' is only possible with one-mode networks.");
		}
		if (qualifierAggregation.equals("conflict") && !networkType.equals("One-mode network")) {
			System.err.println("qualifierAggregation = 'conflict' is only possible with one-mode networks.");
		}
		
		return st;
	}
	
	/**
	 * Format the normalization R argument.
	 * 
	 * @param networkType     Java-DNA-formatted network type String
	 * @param normalization   R argument String with the normalization type (can be 'no', 'activity', 'prominence', 'average', 'Jaccard', or 'cosine')
	 * @return                Formatted normalization String for DNA export (can be 'no', 'activity', 'prominence', 'average activity', 'Jaccard', or 'cosine')
	 */
	private String formatNormalization(String networkType, String normalization) {
		if (normalization.equals("jaccard")) {
			normalization = "Jaccard";
		}
		if (normalization.equals("Cosine")) {
			normalization = "cosine";
		}
		if (!normalization.equals("no") && !normalization.equals("activity") && !normalization.equals("prominence") 
				&& !normalization.equals("average") && !normalization.equals("Jaccard") && !normalization.equals("cosine")) {
			System.err.println("'normalization' must be 'no', 'activity', 'prominence', 'average', 'Jaccard', or 'cosine'.");
		}
		if (normalization.equals("activity") && !networkType.equals("Two-mode network")) {
			System.err.println("'normalization = 'activity' is only possible with two-mode networks.");
		}
		if (normalization.equals("prominence") && !networkType.equals("Two-mode network")) {
			System.err.println("'normalization = 'prominence' is only possible with two-mode networks.");
		}
		if (normalization.equals("average") && !networkType.equals("One-mode network")) {
			System.err.println("'normalization = 'average' is only possible with one-mode networks.");
		}
		if (normalization.equals("Jaccard") && !networkType.equals("One-mode network")) {
			System.err.println("'normalization = 'Jaccard' is only possible with one-mode networks.");
		}
		if (normalization.equals("cosine") && !networkType.equals("One-mode network")) {
			System.err.println("'normalization = 'cosine' is only possible with one-mode networks.");
		}
		if (normalization.equals("average")) {
			normalization = "average activity";
		}
		return normalization;
	}
	
	/**
	 * Format the duplicates R argument.
	 * 
	 * @param duplicates   An input String that can be 'include', 'document', 'week', 'month', 'year', or 'acrossrange'.
	 * @return             An output String that can be 'include all duplicates', 'ignore per document', 'ignore per calendar week', 'ignore per calendar month', 'ignore per calendar year', or 'ignore across date range'
	 */
	private String formatDuplicates(String duplicates) {
		if (!duplicates.equals("include") && !duplicates.equals("document") && !duplicates.equals("week") && !duplicates.equals("month") 
				&& !duplicates.equals("year") && !duplicates.equals("acrossrange")) {
			System.err.println("'duplicates' must be 'include', 'document', 'week', 'month', 'year', or 'acrossrange'.");
		}
		if (duplicates.equals("include")) {
			duplicates = "include all duplicates";
		} else if (duplicates.equals("document")) {
			duplicates = "ignore per document";
		} else if (duplicates.equals("week")) {
			duplicates = "ignore per calendar week";
		} else if (duplicates.equals("month")) {
			duplicates = "ignore per calendar month";
		} else if (duplicates.equals("year")) {
			duplicates = "ignore per calendar year";
		} else if (duplicates.equals("acrossrange")) {
			duplicates = "ignore across date range";
		}
		
		return duplicates;
	}
	
	/**
	 * Convert a date String of format "dd.MM.yyyy" and a time String of format "HH:mm:ss" to a Date object.
	 * 
	 * @param dateString    date String of format "dd.MM.yyyy"
	 * @param timeString    time String of format "HH:mm:ss"
	 * @return              Date object containing both the date and the time
	 */
	private Date formatDate(String dateString, String timeString) {
		String s = dateString + " " + timeString;
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		Date d = null;
		try {
			d = df.parse(s);
		} catch (ParseException e) {
			System.err.println("Date or time is invalid!");
		}
		if (!s.equals(df.format(d))) {
			d = null;
			System.err.println("Date or time is invalid!");
		}
		return d;
	}
	
	/**
	 * Convert a String array of variables and another one of values to be excluded from export into an array list, after considering the invert argument.
	 * 
	 * @param excludeVariables     A String array with n elements, indicating the variable of the n'th value
	 * @param excludeValues        A String array with n elements, indicating the value pertaining to the n'th variable String
	 * @param invertValues         boolean indicating whether the values should be included (= true) rather than excluded
	 * @param statements           ArrayList<String> of filtered statements
	 * @param originalStatements   Original ArrayList<String> of statements before applying the filter
	 * @param documents            ArrayList<Document> containing all documents in which the statements are embedded
	 * @param statementTypeId      int ID of the statement type to which the variables belong
	 * @param isolates             Should isolates be included in the network export?
	 * @return
	 */
	private HashMap<String, ArrayList<String>> processExcludeVariables(String[] excludeVariables, String[] excludeValues, 
			boolean invertValues, ArrayList<Statement> statements, ArrayList<Statement> originalStatements, 
			ArrayList<Document> documents, int statementTypeId, boolean isolates) {
		
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		if (excludeVariables.length > 0) {
			for (int i = 0; i < excludeVariables.length; i++) {
				ArrayList<String> values = map.get(excludeVariables[i]);
				if (values == null) {
					values = new ArrayList<String>();
				}
				if (!values.contains(excludeValues[i])) {
					values.add(excludeValues[i]);
				}
				Collections.sort(values);
				map.put(excludeVariables[i], values);
			}
		}
		if (invertValues == true) {
			Iterator<String> mapIterator = map.keySet().iterator();
			while (mapIterator.hasNext()) {
				String key = mapIterator.next();
				ArrayList<String> values = map.get(key);
				String[] labels = extractLabels(statements, originalStatements, documents, key, false, statementTypeId, isolates);
				ArrayList<String> newValues = new ArrayList<String>();
				for (int i = 0; i < labels.length; i++) {
					if (!values.contains(labels[i])) {
						newValues.add(labels[i]);
					}
				}
				map.put(key, newValues);
			}
		}
		return map;
	}
	
	/**
	 * Process exclude arguments for a document-level variable for R export.
	 * 
	 * @param documentVariable     String denoting the document-level variable for which the excludeValues should be excluded
	 * @param excludeValues        Values to be excluded in the docuementVariable, provided as an array of Strings
	 * @param invert               boolean indicating whether the values should be included (= true) rather than excluded
	 * @param statements           ArrayList<String> of filtered statements
	 * @param originalStatements   Original ArrayList<String> of statements before applying the filter
	 * @param documents            ArrayList<Document> containing all documents in which the statements are embedded
	 * @param statementTypeId      int ID of the statement type to which the variables belong
	 * @param isolates             Should isolates be included in the network export?
	 * @return                     ArrayList<String> of values to be excluded
	 */
	private ArrayList<String> processExcludeDocument(String documentVariable, String[] excludeValues, boolean invert, ArrayList<Statement> statements, 
			ArrayList<Statement> originalStatements, ArrayList<Document> documents, int statementTypeId, boolean isolates) {
		
		ArrayList<String> excludeValuesList = new ArrayList<String>();
		if (excludeValues.length > 0) {
			for (int i = 0; i < excludeValues.length; i++) {
				excludeValuesList.add(excludeValues[i]);
			}
		}
		ArrayList<String> exclude = new ArrayList<String>();
		if (invert == false) {
			exclude.addAll(excludeValuesList);
		} else {
			String[] labels = extractLabels(statements, originalStatements, data.getDocuments(), documentVariable, true, statementTypeId, isolates);
			for (int i = 0; i < labels.length; i++) {
				if (!excludeValuesList.contains(labels[i])) {
					exclude.add(labels[i]);
				}
			}
		}
		
		return exclude;
	}
	
}