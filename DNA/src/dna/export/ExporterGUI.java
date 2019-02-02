package dna.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
public class ExporterGUI extends JDialog {
	
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
	java.awt.Color fg;

	// objects for R calls
	String dbfile;
	SqlConnection sql;
	Data data;
	ArrayList<Statement> filteredStatements;
	ArrayList<Matrix> timeWindowMatrices;
	ArrayList<Date> timeLabels;
	ExportHelper exportHelper;
	
	/**
	 * Constructor for GUI. Opens an Exporter window, which displays the GUI for exporting network data.
	 */
	public ExporterGUI() {
		this.setTitle("Export data");
		this.setModal(true);
		ImageIcon networkIcon = new ImageIcon(getClass().getResource("/icons/chart_organisation.png"));
		this.setIconImage(networkIcon.getImage());
		this.setLayout(new java.awt.BorderLayout());
		
		this.exportHelper = new ExportHelper();
		
		JPanel settingsPanel = new JPanel();
		java.awt.GridBagLayout g = new java.awt.GridBagLayout();
		settingsPanel.setLayout(g);
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
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
		networkModesBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
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
		java.awt.Dimension d = new java.awt.Dimension(WIDTH, HEIGHT);
		networkModesBox.setPreferredSize(d);
		statementTypeBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
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
		fileFormatBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT));
		statementTypeBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT));
		
		// second row of options
		gbc.insets = new java.awt.Insets(10, 3, 3, 3);
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
		
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 3;
		var1Box = new JComboBox<>(var1Items);
		var1Box.setToolTipText(var1ToolTip);
		settingsPanel.add(var1Box, gbc);
		int HEIGHT2 = (int) var1Box.getPreferredSize().getHeight();
		var1Box.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		var2Box = new JComboBox<>(var2Items);
		var2Box.setToolTipText(var2ToolTip);
		var2Box.setSelectedIndex(1);
		settingsPanel.add(var2Box, gbc);
		var2Box.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		fg = var2Box.getForeground();
		java.awt.event.ActionListener varActionListener = new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (var1Box.getItemCount() < 1 || var2Box.getItemCount() < 1) {
					var1Box.setBorder(null);
					var2Box.setBorder(null);
				} else if (var1Box.getSelectedItem().equals(var2Box.getSelectedItem())) {
					var1Box.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED));
					var2Box.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED));
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
		qualifierBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		qualifierBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
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
		aggregationBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		aggregationBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
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
		gbc.insets = new java.awt.Insets(10, 3, 3, 3);
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

		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 5;
		String[] normalizationItems = new String[] {"no", "activity", "prominence"};
		normalizationBox = new JComboBox<>(normalizationItems);
		normalizationBox.setToolTipText(normalizationToolTip);
		settingsPanel.add(normalizationBox, gbc);
		normalizationBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		String[] isolatesItems = new String[] {"only current nodes", "include isolates"};
		isolatesBox = new JComboBox<>(isolatesItems);
		isolatesBox.setToolTipText(isolatesToolTip);
		settingsPanel.add(isolatesBox, gbc);
		isolatesBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));

		gbc.gridx = 2;
		String[] duplicatesItems = new String[] {"include all duplicates", "ignore per document", "ignore per calendar week", 
				"ignore per calendar month", "ignore per calendar year", "ignore across date range"};
		duplicatesBox = new JComboBox<>(duplicatesItems);
		duplicatesBox.setToolTipText(duplicatesToolTip);
		settingsPanel.add(duplicatesBox, gbc);
		duplicatesBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		// fourth row of options
		gbc.insets = new java.awt.Insets(10, 3, 3, 3);
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
				+ "period is reached. All time slices are saved as separate networks. The networks will be saved to new "
				+ "files each time, with an iterator number added to the file name. Instead "
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
		
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
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
		startSpinner.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		SpinnerDateModel stopModel = new SpinnerDateModel();
		stopSpinner = new JSpinner();
		stopModel.setCalendarField(Calendar.DAY_OF_YEAR);
		stopSpinner.setModel(stopModel);
		stopModel.setValue(dates.get(dates.size() - 1));
		stopSpinner.setEditor(new JSpinner.DateEditor(stopSpinner, "yyyy-MM-dd - HH:mm:ss"));
		stopSpinner.setToolTipText(dateToolTip);
		settingsPanel.add(stopSpinner, gbc);
		stopSpinner.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 2;
		String[] timeWindowItems = new String[] {"no time window", "using events", "using seconds", "using minutes", 
				"using hours", "using days", "using weeks", "using months", "using years"};
		timeWindowBox = new JComboBox<>(timeWindowItems);
		timeWindowBox.setToolTipText(timeWindowToolTip);
		settingsPanel.add(timeWindowBox, gbc);
		timeWindowBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 3;
		timeWindowSpinner = new JSpinner(new SpinnerNumberModel(100, 0, 100000, 1));
		timeWindowSpinner.setToolTipText(timeWindowToolTip);
		settingsPanel.add(timeWindowSpinner, gbc);
		timeWindowSpinner.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		// fifth row of options: exclude values from variables
		gbc.insets = new java.awt.Insets(10, 3, 3, 3);
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
		
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
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
		excludeVariableScroller.setPreferredSize(new java.awt.Dimension(WIDTH, (int) excludeVariableScroller.getPreferredSize().getHeight()));
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
		excludeValueScroller.setPreferredSize(new java.awt.Dimension(WIDTH, (int) excludeValueScroller.getPreferredSize().getHeight()));
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
		helpBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				toggleHelp();
			}
			
		});
		settingsPanel.add(helpBox, gbc);
		
		gbc.gridx = 2;
		gbc.anchor = java.awt.GridBagConstraints.EAST;
		JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
		JButton revertButton = new JButton("Revert", new ImageIcon(getClass().getResource("/icons/arrow_rotate_anticlockwise.png")));
		String revertToolTip = "<html><p>Reset all settings to their default values.</p></html>";
		revertButton.setToolTipText(revertToolTip);
		buttonPanel.add(revertButton);
		revertButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
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
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dispose();
			}
		});
		exportButton = new JButton("Export...", new ImageIcon(getClass().getResource("/icons/accept.png")));
		String exportToolTip = "<html><p>Select a file name and save the network using the current settings.</p></html>";
		exportButton.setToolTipText(exportToolTip);
		exportButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
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
		
		this.add(settingsPanel, java.awt.BorderLayout.NORTH);
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
			progressMonitor = new ProgressMonitor(ExporterGUI.this, "Exporting network data.", "(1/4) Filtering statements...", 0, 4);
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
			statements = exportHelper.filter(statements, documents, startDate, stopDate, statementType, var1Name, var2Name, 
					var1Document(), var2Document(), qualifierName, ignoreQualifier, duplicateSetting, 
					excludeAuthor, excludeSource, excludeSection, excludeType, excludeValues, filterEmptyFields, true);
			System.out.println("Export was launched: " + statements.size() + " out of " + Dna.data.getStatements().size() 
					+ " statements retained after filtering.");
			progressMonitor.setProgress(1);
			
			// step 2: compile the node labels (and thereby dimensions) for the first and second mode
			progressMonitor.setNote("(2/4) Compiling node labels...");
			boolean includeIsolates = false;
			if (!networkModesBox.getSelectedItem().equals("Event list")) {  // labels are only needed for one-mode or two-mode networks
				if (isolatesBox.getSelectedItem().equals("include isolates")) {
					includeIsolates = true;
				}
				int statementTypeId = statementType.getId();
				ArrayList<Statement> originalStatements = Dna.data.getStatements();
				names1 = exportHelper.extractLabels(statements, originalStatements, documents, var1Name, var1Document(), statementTypeId, includeIsolates);
				names2 = exportHelper.extractLabels(statements, originalStatements, documents, var2Name, var2Document(), statementTypeId, includeIsolates);
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
					boolean verbose;
					verbose = true;
					matrix = exportHelper.computeTwoModeMatrix(statements, documents, statementType, var1Name, var2Name, var1Document(), 
							var2Document(), names1, names2, qualifier, qualifierAggregation, normalization, verbose);
				} else {
					String timeWindowUnit = (String) timeWindowBox.getSelectedItem();
					int timeWindowDuration = (int) timeWindowSpinner.getModel().getValue();
					timeWindowMatrices = exportHelper.computeTimeWindowMatrices(statements, documents, statementType, var1Name, var2Name, var1Document(), 
							var2Document(), names1, names2, qualifier, qualifierAggregation, normalization, true, startDate, stopDate, 
							timeWindowUnit, timeWindowDuration, includeIsolates);
				}
			} else if (networkModesBox.getSelectedItem().equals("One-mode network")) {
				if (timeWindowBox.getSelectedItem().equals("no time window")) {
					matrix = exportHelper.computeOneModeMatrix(statements, documents, statementType, var1Name, var2Name, var1Document(), 
							var2Document(), names1, names2, qualifier, qualifierAggregation, normalization);
				} else {
					String timeWindowUnit = (String) timeWindowBox.getSelectedItem();
					int timeWindowDuration = (int) timeWindowSpinner.getModel().getValue();
					timeWindowMatrices = exportHelper.computeTimeWindowMatrices(statements, documents, statementType, var1Name, var2Name, var1Document(), 
							var2Document(), names1, names2, qualifier, qualifierAggregation, normalization, false, startDate, stopDate, 
							timeWindowUnit, timeWindowDuration, includeIsolates);
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
					if (timeWindowBox.getSelectedItem().equals("no time window")) {
						exportCSV(matrix, filename);
					} else {
						String filename1 = filename.substring(0, filename.length() - 4);
						String filename3 = filename.substring(filename.length() - 4, filename.length());
						for (int i = 0; i < timeWindowMatrices.size(); i++) {
							String filename2 = "-" + String.format("%0" + String.valueOf(timeWindowMatrices.size()).length() + "d", i + 1);
							exportCSV(timeWindowMatrices.get(i), filename1 + filename2 + filename3);
						}
					}
				} else if (fileFormat.equals(".dl")) {
					if (timeWindowBox.getSelectedItem().equals("no time window")) {
						exportDL(matrix, filename, twoMode);
					} else {
						String filename1 = filename.substring(0, filename.length() - 3);
						String filename3 = filename.substring(filename.length() - 3, filename.length());
						for (int i = 0; i < timeWindowMatrices.size(); i++) {
							String filename2 = "-" + String.format("%0" + String.valueOf(timeWindowMatrices.size()).length() + "d", i + 1);
							exportDL(timeWindowMatrices.get(i), filename1 + filename2 + filename3, twoMode);
						}
					}
				} else if (fileFormat.equals(".graphml")) {
					String[] values1 = exportHelper.retrieveValues(statements, documents, var1Name, var1Document());
					String[] values2 = exportHelper.retrieveValues(statements, documents, var2Name, var2Document());
					int[] frequencies1 = exportHelper.countFrequencies(values1, names1);
					int[] frequencies2 = exportHelper.countFrequencies(values2, names2);
					ArrayList<AttributeVector> attributes = Dna.data.getAttributes();
					boolean qualifierBinary = false;
					if (statementType.getVariables().get(qualifierName).equals("boolean")) {
						qualifierBinary = true;
					}
					if (timeWindowBox.getSelectedItem().equals("no time window")) {
						exportGraphml(matrix, twoMode, statementType, filename, var1Name, var2Name, frequencies1, frequencies2, 
								attributes, qualifierAggregation, qualifierBinary);
					} else {
						String filename1 = filename.substring(0, filename.length() - 8);
						String filename3 = filename.substring(filename.length() - 8, filename.length());
						for (int i = 0; i < timeWindowMatrices.size(); i++) {
							String filename2 = "-" + String.format("%0" + String.valueOf(timeWindowMatrices.size()).length() + "d", i + 1);
							exportGraphml(timeWindowMatrices.get(i), twoMode, statementType, filename1 + filename2 + filename3, 
									var1Name, var2Name, frequencies1, frequencies2, attributes, qualifierAggregation, qualifierBinary);
						}
					}
				}
			}
			progressMonitor.setProgress(4);
			JOptionPane.showMessageDialog(Dna.gui, "Data were exported to \"" + filename + "\".");
		}
	}
	
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
				String stringId = Integer.valueOf(statementId).toString();
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
	private void exportCSV (Matrix m, String outfile) {
		String[] rn = m.getRownames();
		String[] cn = m.getColnames();
		int nr = rn.length;
		int nc = cn.length;
		double[][] mat = m.getMatrix();
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
					if (m.getInteger() == true) {
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
	public void exportDL (Matrix m, String outfile, boolean twoMode) {
		String[] rn = m.getRownames();
		String[] cn = m.getColnames();
		int nr = rn.length;
		int nc = cn.length;
		double[][] mat = m.getMatrix();
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
					if (m.getInteger() == true) {
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
	
	/*
	private void exportGEFX(Matrix mt, boolean twoMode, StatementType statementType, String outfile, String var1, String var2, 
			int[] frequencies1, int[] frequencies2, ArrayList<AttributeVector> attributes, String qualifierAggregation, 
			boolean qualifierBinary) {

		// extract attributes
		String[] rn = mt.getRownames();
		String[] cn = mt.getColnames();
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
		java.awt.Color[] color = new java.awt.Color[names.length];
		String[] type = new String[names.length];
		String[] alias = new String[names.length];
		String[] notes = new String[names.length];
		for (int i = 0; i < attributes.size(); i++) {
			if (attributes.get(i).getStatementTypeId() == statementType.getId() && attributes.get(i).getVariable().equals(var1)) {
				for (int j = 0; j < rn.length; j++) {
					if (rn[j].equals(attributes.get(i).getValue())) {
						id[j] = attributes.get(i).getId();
						color[j] = attributes.get(i).getColor();
						type[j] = attributes.get(i).getType();
						alias[j] = attributes.get(i).getAlias();
						notes[j] = attributes.get(i).getNotes();
					}
				}
			} else if (attributes.get(i).getStatementTypeId() == statementType.getId() && attributes.get(i).getVariable().equals(var2) && twoMode == true) {
				for (int j = 0; j < cn.length; j++) {
					if (cn[j].equals(attributes.get(i).getValue())) {
						id[j + rn.length] = attributes.get(i).getId();
						color[j + rn.length] = attributes.get(i).getColor();
						type[j + rn.length] = attributes.get(i).getType();
						alias[j + rn.length] = attributes.get(i).getAlias();
						notes[j + rn.length] = attributes.get(i).getNotes();
					}
				}
			}
		}
		
		
		
		DocType dt = new DocType("xml");
		org.jdom.Document document = new org.jdom.Document();
		document.setDocType(dt);
		
		// gexf element with schema information
		Element gexfElement = new Element("gexf");
		gexfElement.setAttribute(new Attribute("xmlns", "http://www.gexf.net/1.2draft"));
		gexfElement.setAttribute(new Attribute("xmlns:viz", "http://www.gexf.net/1.2draft/viz"));
		gexfElement.setAttribute(new Attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"));
		gexfElement.setAttribute(new Attribute("xsi:schemaLocation", "http://www.gexf.net/1.2draft/gexf.xsd"));
		gexfElement.setAttribute(new Attribute("version", "1.2"));
		document.addContent(gexfElement);
		
		// meta data
		Element meta = new Element("meta");
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
		Date today = new Date();
		meta.setAttribute(new Attribute("lastmodifieddate", df.format(today)));
		Element creator = new Element("creator");
		creator.setText("Discourse Network Analyzer (DNA)");
		meta.addContent(creator);
		Element description = new Element("description");
		if (twoMode == true) {
			description.setText("Two-mode network");
		} else {
			description.setText("One-mode network");
		}
		meta.addContent(description);
		gexfElement.addContent(meta);
		
		// graph element with node and edge attribute definitions
		Element graphElement = new Element("graph");
		graphElement.setAttribute(new Attribute("mode", "static"));
		graphElement.setAttribute(new Attribute("defaultedgetype", "undirected"));
		
		Element attributesElement = new Element("attributes");
		attributesElement.setAttribute(new Attribute("class", "node"));
		Element at0 = new Element("attribute");
		at0.setAttribute(new Attribute("id", "0"));
		at0.setAttribute(new Attribute("title", "type"));
		at0.setAttribute(new Attribute("type", "string"));
		attributesElement.addContent(at0);
		Element at1 = new Element("attribute");
		at1.setAttribute(new Attribute("id", "1"));
		at1.setAttribute(new Attribute("title", "alias"));
		at1.setAttribute(new Attribute("type", "string"));
		attributesElement.addContent(at1);
		Element at2 = new Element("attribute");
		at2.setAttribute(new Attribute("id", "2"));
		at2.setAttribute(new Attribute("title", "notes"));
		at2.setAttribute(new Attribute("type", "string"));
		attributesElement.addContent(at2);
		
		//<attributes class="node">
	    //  <attribute id="0" title="url" type="string"/>
	    //  <attribute id="1" title="indegree" type="float"/>
	    //  <attribute id="2" title="frog" type="boolean">
	    //    <default>true</default>
	    //  </attribute>
	    //</attributes>
		
		// add nodes
		Element nodes = new Element("nodes");
		for (int i = 0; i < names.length; i++) {
			Element node = new Element("node");
			node.setAttribute(new Attribute("id", "" + id[i]));
			node.setAttribute(new Attribute("label", "" + names[i]));
			
			Element attvaluesElement = new Element("attvalues");
			
			Element attvalueElement0 = new Element("attvalue");
			attvalueElement0.setAttribute(new Attribute("for", "0"));
			attvalueElement0.setAttribute(new Attribute("value", type[i]));
			attvaluesElement.addContent(attvalueElement0);

			Element attvalueElement1 = new Element("attvalue");
			attvalueElement1.setAttribute(new Attribute("for", "1"));
			attvalueElement1.setAttribute(new Attribute("value", alias[i]));
			attvaluesElement.addContent(attvalueElement1);

			Element attvalueElement2 = new Element("attvalue");
			attvalueElement2.setAttribute(new Attribute("for", "2"));
			attvalueElement2.setAttribute(new Attribute("value", notes[i]));
			attvaluesElement.addContent(attvalueElement2);

			Element attvalueElement3 = new Element("attvalue");
			attvalueElement3.setAttribute(new Attribute("for", "3"));
			attvalueElement3.setAttribute(new Attribute("value", variables[i]));
			attvaluesElement.addContent(attvalueElement3);

			Element attvalueElement4 = new Element("attvalue");
			attvalueElement4.setAttribute(new Attribute("for", "4"));
			attvalueElement4.setAttribute(new Attribute("value", String.valueOf(frequencies[i])));
			attvaluesElement.addContent(attvalueElement4);
			
			node.addContent(attvaluesElement);
			
			Element vizColor = new Element("viz:color");
			vizColor.setAttribute(new Attribute("r", "" + color[i].getRed()));
			vizColor.setAttribute(new Attribute("g", "" + color[i].getGreen()));
			vizColor.setAttribute(new Attribute("b", "" + color[i].getBlue()));
			vizColor.setAttribute(new Attribute("a", "" + color[i].getAlpha()));
			node.addContent(vizColor);
			
			Element vizShape = new Element("viz:shape");
			if (i < rn.length) {
				vizShape.setAttribute(new Attribute("value", "disc"));
			} else {
				vizShape.setAttribute(new Attribute("value", "square"));
			}
			node.addContent(vizShape);
			
			nodes.addContent(node);
		}
		graphElement.addContent(nodes);
		
		// add edges
		Element edges = new Element("edges");
		
		graphElement.addContent(edges);
		
		gexfElement.addContent(graphElement);
		
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
	*/
	
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
	private void exportGraphml(Matrix mt, boolean twoMode, StatementType statementType, String outfile, 
			String var1, String var2, int[] frequencies1, int[] frequencies2, ArrayList<AttributeVector> attributes, 
			String qualifierAggregation, boolean qualifierBinary) {
		
		// extract attributes
		String[] rn = mt.getRownames();
		String[] cn = mt.getColnames();
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
		double[][] m = mt.getMatrix();
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
	
}