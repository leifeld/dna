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
import java.math.BigInteger;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import dna.Dna;
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
	JCheckBox helpBox;
	JButton exportButton;
	JComboBox<String> networkModesBox, fileFormatBox, var1Box, var2Box, qualifierBox, aggregationBox, normalizationBox, 
			isolatesBox, duplicatesBox, temporalBox;
	JComboBox<StatementType> statementTypeBox;
	JSpinner startSpinner, stopSpinner;
	JList<String> excludeVariableList, excludeValueList;
	HashMap<String, ArrayList<String>> excludeValues;
	ArrayList<String> excludeAuthor, excludeSource, excludeSection, excludeType;
	JTextArea excludePreviewArea;
	Color fg;
	
	/**
	 * Opens an Exporter window, which displays the GUI for exporting network data.
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
					fileFormatBox.removeAllItems();
					fileFormatBox.addItem(".csv");
					fileFormatBox.addItem(".dl");
					fileFormatBox.addItem(".graphml");
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("combine");
					aggregationBox.addItem("subtract");
					normalizationBox.removeAllItems();
					normalizationBox.addItem("no");
					normalizationBox.addItem("activity");
					normalizationBox.addItem("prominence");
					temporalBox.removeAllItems();
					temporalBox.addItem("across date range");
				} else if (selected.equals("One-mode network")) {
					fileFormatBox.removeAllItems();
					fileFormatBox.addItem(".csv");
					fileFormatBox.addItem(".dl");
					fileFormatBox.addItem(".graphml");
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("congruence");
					aggregationBox.addItem("conflict");
					aggregationBox.addItem("subtract");
					normalizationBox.removeAllItems();
					normalizationBox.addItem("no");
					normalizationBox.addItem("average activity");
					normalizationBox.addItem("Jaccard");
					normalizationBox.addItem("cosine");
					temporalBox.removeAllItems();
					temporalBox.addItem("across date range");
					temporalBox.addItem("nested in document");
				} else if (selected.equals("Event list")) {
					fileFormatBox.removeAllItems();
					fileFormatBox.addItem(".csv");
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					normalizationBox.removeAllItems();
					normalizationBox.addItem("no");
					temporalBox.removeAllItems();
					temporalBox.addItem("across date range");
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
				if (varItems.length > 0 && qualifierItems.length > 0) {
					exportButton.setEnabled(true);
				} else {
					exportButton.setEnabled(false);
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
		JLabel temporalLabel = new JLabel("Temporal aggregation");
		String temporalToolTip = "<html><p width=\"500\">By default, one-mode networks are creating by aggregating over "
				+ "the qualifier variable across the whole date range. In some settings, however, it makes sense to "
				+ "create edges only within documents (but still taking into account all documents at all time points). "
				+ "For example, if the context of the second variable differs across documents, such as norms in legal "
				+ "cases, it makes sense to create edges between nodes within the first case, then only within the "
				+ "second case, then the third etc., and finally merge the resulting networks into a joint network. "
				+ "This is what is done when edges are nested within documents.</p></html>";
		temporalLabel.setToolTipText(temporalToolTip);
		settingsPanel.add(temporalLabel, gbc);

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
		String[] temporalItems = new String[] {"across date range", "nested in document"};
		temporalBox = new JComboBox<>(temporalItems);
		temporalBox.setToolTipText(temporalToolTip);
		settingsPanel.add(temporalBox, gbc);
		temporalBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
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
							excludePreviewText = excludePreviewText + "type: " + excludeType.get(i);
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
				temporalBox.setSelectedIndex(0);
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
					Thread exportThread = new Thread( new ExportThread(fileName), "Export network" );
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
	 * Export thread. This is where the computations are executed and the data are written to a file. 
	 */
	class ExportThread implements Runnable {
		
		String filename;
		ArrayList<Statement> statements;
		String[] names1, names2;
		ProgressMonitor progressMonitor;
		
		public ExportThread(String filename) {
			this.filename = filename;
		}
		
		public void run() {
			progressMonitor = new ProgressMonitor(Exporter.this, "Exporting network data.", "(1/4) Filtering statements...", 0, 3);
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
			statements = filter(startDate, stopDate, statementType, var1Name, var2Name, qualifierName, ignoreQualifier, 
					duplicateSetting, excludeAuthor, excludeSource, excludeSection, excludeType, excludeValues);
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
				names1 = extractLabels(statements, var1Name, statementTypeId, includeIsolates);
				names2 = extractLabels(statements, var2Name, statementTypeId, includeIsolates);
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
				matrix = computeTwoModeMatrix(statements, var1Name, var2Name, names1, names2, qualifier, qualifierAggregation, normalization);
			} else if (networkModesBox.getSelectedItem().equals("One-mode network")) {
				String temporalAggregation = (String) temporalBox.getSelectedItem();
				// TODO: generate one-mode network
			}
			System.out.println("Network has been created.");
			progressMonitor.setProgress(3);
			
			// step 4: write to file
			progressMonitor.setNote("(4/4) Writing to file...");
			String fileFormat = (String) fileFormatBox.getSelectedItem();
			if (networkModesBox.getSelectedItem().equals("Event list")) {
				eventCSV(statements, filename);
			} else {
				if (fileFormat.equals(".csv")) {
					exportCSV(matrix, filename);
				} else if (fileFormat.equals(".dl")) {
					
				} else if (fileFormat.equals(".graphml")) {
					
				}
				// TODO: connect network to appropriate output format and export
			}
			JOptionPane.showMessageDialog(Dna.dna.gui, "Data were exported to \"" + filename + "\".");
		}
	}
	
	/**
	 * Extract the labels for all nodes for a variable from the statements, conditional on isolates settings
	 * 
	 * @param statements        {@link ArrayList} of filtered {@link Statement}s
	 * @param variable          {@link String} indicating the variable for which labels should be extracted
	 * @param statementTypeId   {@link int} specifying the statement type ID to which the variable belongs
	 * @param includeIsolates   {@link boolean} indicating whether all nodes should be included or just those after applying the statement filter
	 * @return                  {@link String} array containing all sorted node names
	 */
	String[] extractLabels(ArrayList<Statement> statements, String variable, int statementTypeId, boolean includeIsolates) {
		ArrayList<String> names = new ArrayList<String>();
		if (includeIsolates == true) {  // take them from the main database 
			for (int i = 0; i < Dna.data.getStatements().size(); i++) {
				if (Dna.data.getStatements().get(i).getStatementTypeId() == statementTypeId) {
					String n = (String) Dna.data.getStatements().get(i).getValues().get(variable);
					if (!names.contains(n)) {
						names.add(n);
					}
				}
			}
		} else {  // no isolates: take them from the filtered results
			for (int i = 0; i < statements.size(); i++) {
				String n = (String) statements.get(i).getValues().get(variable);
				if (!names.contains(n)) {
					names.add(n);
				}
			}
		}
		Collections.sort(names);
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
	 * @param startDate           {@link Date} object indicating the start date
	 * @param stopDate            {@link Date} object indicating the end date
	 * @param statementType       {@link StatementType} to which the export is restricted
	 * @param var1                {@link String} indicating the first variable used for network construction, e.g., "organization"
	 * @param var2                {@link String} indicating the second variable used for network construction, e.g., "concept"
	 * @param qualifierName       {@link String} indicating the qualifier variable, e.g., "agreement"
	 * @param ignoreQualifier     {@link boolean} indicating whether the qualifier variable should be ignored
	 * @param duplicateSetting    {@link String} indicating how to handle duplicates; valid settings include "include all duplicates", "ignore per document", "ignore per calendar week", "ignore per calendar month", "ignore per calendar year", or "ignore across date range"
	 * @param excludeAuthor       {@link ArrayList} with {@link String}s containing document authors to exclude
	 * @param excludeSource       {@link ArrayList} with {@link String}s containing document sources to exclude
	 * @param excludeSection      {@link ArrayList} with {@link String}s containing document sections to exclude
	 * @param excludeType         {@link ArrayList} with {@link String}s containing document types to exclude
	 * @param excludeValues       {@link HashMap} with {@link String}s as keys (indicating the variable for which entries should be excluded from export) and {@link HashMap}s of {@link String}s (containing variable entries to exclude from network export)
	 * @return                    {@link ArrayList} of filtered {@link Statement}s
	 */
	ArrayList<Statement> filter(Date startDate, Date stopDate, StatementType statementType, String var1, String var2, String qualifierName, 
			boolean ignoreQualifier, String duplicateSetting, ArrayList<String> excludeAuthor, 
			ArrayList<String> excludeSource, ArrayList<String> excludeSection, ArrayList<String> excludeType, 
			HashMap<String, ArrayList<String>> excludeValues) {
		
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
		
		// process and exclude statements
		ArrayList<Statement> al = new ArrayList<Statement>();
		for (int i = 0; i < Dna.dna.gui.rightPanel.statementPanel.ssc.size(); i++) {
			boolean select = true;
			Statement s = Dna.dna.gui.rightPanel.statementPanel.ssc.get(i);
			
			// step 1: get all statement IDs corresponding to date range and statement type
			if (s.getDate().before(startDate)) {
				select = false;
			} else if (s.getDate().after(stopDate)) {
				select = false;
			} else if (s.getStatementTypeId() != statementType.getId()) {
				select = false;
			}
			
			// step 2: check against excluded values
			if (excludeAuthor.contains(Dna.data.getDocument(s.getDocumentId()).getAuthor())) {
				select = false;
			} else if (excludeSource.contains(Dna.data.getDocument(s.getDocumentId()).getSource())) {
				select = false;
			} else if (excludeSection.contains(Dna.data.getDocument(s.getDocumentId()).getSection())) {
				select = false;
			} else if (excludeType.contains(Dna.data.getDocument(s.getDocumentId()).getType())) {
				select = false;
			}
			if (select == true) {
				Iterator<String> keyIterator = excludeValues.keySet().iterator();
				while (keyIterator.hasNext()) {
					String key = keyIterator.next();
					String string = "";
					if (statementType.getVariables().get(key).equals("boolean") || statementType.getVariables().get(key).equals("integer")) {
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
				if (s.getValues().get(var1).equals("")) {
					select = false;
				}
				if (s.getValues().get(var2).equals("")) {
					select = false;
				}
			}
			
			// step 4: check for duplicates
			Calendar cal = Calendar.getInstance();
		    cal.setTime(s.getDate());
		    int year = cal.get(Calendar.YEAR);
		    int month = cal.get(Calendar.MONTH);
		    int week = cal.get(Calendar.WEEK_OF_YEAR);
			if (!duplicateSetting.equals("include all duplicates")) {
				for (int j = al.size() - 1; j >= 0; j--) {
					Calendar calPrevious = Calendar.getInstance();
				    calPrevious.setTime(al.get(j).getDate());
				    int yearPrevious = calPrevious.get(Calendar.YEAR);
				    int monthPrevious = calPrevious.get(Calendar.MONTH);
				    int weekPrevious = calPrevious.get(Calendar.WEEK_OF_YEAR);
					if ( s.getStatementTypeId() == al.get(j).getStatementTypeId()
							&& (al.get(j).getDocumentId() == s.getDocumentId() && duplicateSetting.equals("ignore per document") 
								|| duplicateSetting.equals("ignore across date range")
								|| (duplicateSetting.equals("ignore per calendar year") && year == yearPrevious)
								|| (duplicateSetting.equals("ignore per calendar month") && month == monthPrevious)
								|| (duplicateSetting.equals("ignore per calendar week") && week == weekPrevious) )
							&& s.getValues().get(var1).equals(al.get(j).getValues().get(var1))
							&& s.getValues().get(var2).equals(al.get(j).getValues().get(var2))
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
	 * Create a two-mode network {@link Matrix}.
	 * 
	 * @param statements            A (potentially filtered) {@link ArrayList} of {@link Statement}s.
	 * @param var1                  {@link String} denoting the first variable (containing the row values).
	 * @param var2                  {@link String} denoting the second variable (containing the columns values).
	 * @param names1                {@link String} array containing the row labels.
	 * @param names2                {@link String} array containing the column labels.
	 * @param qualifier             {@link String} denoting the qualifier variable.
	 * @param qualifierAggregation  {@link String} indicating how different levels of the qualifier variable are aggregated. Valid values are "ignore", "subtract", and "combine".
	 * @param normalization         {@link String} indicating what type of normalization will be used. Valid values are "no", "activity", and "prominence".
	 * @return                      {@link Matrix} object containing a two-mode network matrix.
	 */
	public Matrix computeTwoModeMatrix(ArrayList<Statement> statements, String var1, String var2, String[] names1, 
			String[] names2, String qualifier, String qualifierAggregation, String normalization) {
		
		// TODO: fix "combine" aggregation with integer qualifier variable; test this also with normalization
		
		int statementTypeId = statements.get(0).getStatementTypeId();
		boolean booleanQualifier = true;  // is the qualifier boolean, rather than integer?
		if (Dna.data.getStatementTypeById(statementTypeId).getVariables().get(qualifier).equals("integer")) {
			booleanQualifier = false;
		}
		int[] qualifierValues;  // unique qualifier values (i.e., all of them found at least once in the dataset)
		if (booleanQualifier == true) {
			qualifierValues = new int[] {0, 1};
		} else {
			qualifierValues = Dna.data.getIntEntries(statementTypeId, qualifier);
		}
		
		// create and populate array
		double[][][] array = new double[names1.length][names2.length][qualifierValues.length]; // 3D array: rows x cols x qualifier value
		for (int i = 0; i < statements.size(); i++) {
			String n1 = (String) statements.get(i).getValues().get(var1);  // retrieve first value from statement
			String n2 = (String) statements.get(i).getValues().get(var2);  // retrieve second value from statement
			int q = (int) statements.get(i).getValues().get(qualifier);    // retrieve qualifier value from statement
			
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
		
		// combine levels of the qualifier variable conditional on qualifier aggregation option
		double[][] mat = new double[names1.length][names2.length];  // initialized with zeros
		/*
		if (booleanQualifier == false && qualifierAggregation.equals("combine")) {
			qualifierAggregation = "ignore";
			System.err.print("Qualifier aggregation 'combine' was only implemented for binary qualifiers. Choosing option 'ignore' instead.");
		}
		*/
		ArrayList<Integer> chosenValuesList;
		int[] chosenValuesArray;
		for (int i = 0; i < names1.length; i++) {
			for (int j = 0; j < names2.length; j++) {
				if (qualifierAggregation.equals("combine")) {  // combine
					if (booleanQualifier == true) {
						if (array[i][j][0] == 0.0 && array[i][j][1] == 0.0) {
							mat[i][j] = 0.0;
						} else if (array[i][j][0] == 0.0 && array[i][j][1] > 0.0) {
							mat[i][j] = 1.0;
						} else if (array[i][j][0] > 0.0 && array[i][j][1] == 0) {
							mat[i][j] = 2.0;
						} else if (array[i][j][0] > 0.0 && array[i][j][1] > 0.0) {
							mat[i][j] = 3.0;
						}
					} else {
						chosenValuesList = new ArrayList<Integer>();
						for (int k = 0; k < qualifierValues.length; k++) {
							if (array[i][j][k] > 0 && !chosenValuesList.contains(qualifierValues[k])) {
								chosenValuesList.add(qualifierValues[k]);
							}
						}
						Collections.sort(chosenValuesList);
						chosenValuesArray = new int[chosenValuesList.size()];
						for (int k = 0; k < chosenValuesList.size(); k++) {
							chosenValuesArray[k] = chosenValuesList.get(k);
						}
						mat[i][j] = rankQualifierCombination(chosenValuesArray, qualifierValues);
					}
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

	private int rankQualifierCombination(int[] chosenValues, int[] allValues) {
		// create binary vector with matches
		int[] matches = new int[allValues.length];
		int numMatches = 0;
		for (int i = 0; i < chosenValues.length; i++) {
			for (int j = 0; j < allValues.length; j++) {
				if (chosenValues[i] == allValues[j]) {
					matches[j] = 1;
					numMatches++;
				}
			}
		}
		
		// check how many combinations were there with fewer matches than the empirical number of matches
		int numPreviousCombinations = 0;
		int l = numMatches - 1;
		while (l > 0) {
			numPreviousCombinations = numPreviousCombinations + nChooseK(allValues.length, l).intValue();
			l--;
		}
		// create an artificial sequence with the same number of matches
		int[] temp = new int[matches.length];
		for (int i = 0; i < numMatches; i++) {
			temp[i] = 1;
		}
		int currentMatches = 0;  // used to check whether the current test configuration matches the empirical configuration
		for (int i = 0; i < temp.length; i++) {
			if (temp[i] == matches[i]) {
				currentMatches++;
			}
		}
		if (currentMatches == numMatches) {
			numPreviousCombinations++;
			return numPreviousCombinations;
		}
		// if it's not that first combination, permute that combination until we hit the right one
		int count = 1;  // counts how many permutations are necessary to reach the final configuration
		String binaryNumberString;
		int binaryNumberInt;
		char[] binaryNumberCharArray;
		while (currentMatches != numMatches) {  // as long as there is no complete match, continue iterating
			count++;
			binaryNumberString = Arrays.toString(temp);  // convert current configuration to string
			binaryNumberInt = Integer.parseInt(binaryNumberString, 2);  // then parse into binary number (e.g., 1000110)
			binaryNumberInt++;  // increase in the binary system by one
			binaryNumberString = Integer.toBinaryString(binaryNumberInt);  // convert binary number back to string
			binaryNumberCharArray = binaryNumberString.toCharArray();  // convert binary string to char array
			for (int i = 0; i < binaryNumberCharArray.length; i++) {  // convert char array to integer array temp
				if (Character.toString(binaryNumberCharArray[i]).equals("0")) {
					temp[i] = 0;
				} else {
					temp[i] = 1;
				}
			}
			currentMatches = 0;  // check if the current configuration matches the empirical configuration
			for (int i = 0; i < temp.length; i++) {
				if (temp[i] == matches[i]) {
					currentMatches++;
				}
			}
		}
		numPreviousCombinations = numPreviousCombinations + count;
		return numPreviousCombinations;
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
	public void eventCSV(ArrayList<Statement> statements, String fileName) {
		String key, value;
		int statementId;
		Date d;
		SimpleDateFormat dateFormat;
		int statementTypeId = statements.get(0).getStatementTypeId();
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).getStatementTypeId() != statementTypeId) {
				throw new IllegalArgumentException("More than one statement type was selected. Cannot export to a spreadsheet!");
			}
		}
		HashMap<String, String> variables = Dna.data.getStatementTypeById(statementTypeId).getVariables();
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
				dna.dataStructures.Document doc = Dna.data.getDocument(Dna.data.getStatement(statementId).getDocumentId());
				out.write(";" + doc.getId());
				out.write(";\"" + doc.getTitle().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getAuthor().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getSource().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getSection().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getType().replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				out.write(";\"" + doc.getText().substring(Dna.data.getStatement(statementId).getStart(), 
						Dna.data.getStatement(statementId).getStop()).replaceAll(";", ",").replaceAll("\"", "'") + "\"");
				keyIterator = variables.keySet().iterator();
				while (keyIterator.hasNext()){
					key = keyIterator.next();
					value = variables.get(key);
					if (value.equals("short text") || value.equals("long text")) {
						out.write(";\"" + ((String) Dna.data.getStatement(statementId).getValues().get(key)).replaceAll(";", ",").replaceAll("\"", "'") + "\"");
					} else if (value.equals("boolean") || value.equals("integer")) {
						out.write(";" + Dna.data.getStatement(statementId).getValues().get(key));
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
	public void exportCSV (Matrix matrix, String outfile) {
		String[] rn = matrix.getRownames();
		String[] cn = matrix.getColnames();
		int nr = rn.length;
		int nc = cn.length;
		double[][] mat = matrix.getMatrix();
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
	 * Computes n choose k.
	 * 
	 * @param N    How large is the population?
	 * @param K    How many items are selected?
	 * @return     A {@link BigInteger} indicating the result of n choose k.
	 */
	static BigInteger nChooseK(int N, int K) {
	    BigInteger ret = BigInteger.ONE;
	    for (int k = 0; k < K; k++) {
	        ret = ret.multiply(BigInteger.valueOf(N - k)).divide(BigInteger.valueOf(k + 1));
	    }
	    return ret;
	}
}
