package gui;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import dna.Dna;
import export.Exporter;
import logger.LogEvent;
import logger.Logger;
import me.tongfei.progressbar.ProgressBar;
import model.StatementType;
import model.TableDocument;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * GUI for network export. Allows the user to set up options for exporting
 * various kinds of networks.
 */
public class BackboneExporter extends JDialog {
	private static final long serialVersionUID = 3774134211831948308L;
	private LocalDateTime[] dateTimeRange = Dna.sql.getDateTimeRange();
	private DateTimePicker startPicker, stopPicker;
	private JCheckBox helpBox;
	private JButton backboneButton, exportXmlButton, exportJsonButton;
	private JComboBox<String> fileFormatBox, var1Box, var2Box, qualifierBox, aggregationBox, normalizationBox,	duplicatesBox;
	private StatementType[] statementTypes;
	private JComboBox<StatementType> statementTypeBox;
	private JList<String> excludeVariableList, excludeValueList;
	private HashMap<String, ArrayList<String>> excludeValues;
	private ArrayList<String> excludeAuthor, excludeSource, excludeSection, excludeType;
	private JTextArea excludePreviewArea;
	private Exporter exporter;

	/**
	 * Constructor for GUI. Opens an Exporter window, which displays the GUI for exporting network data.
	 */
	public BackboneExporter(Frame parent) {
		super(parent, "Backbone finder", true);
		this.setTitle("Backbone finder");
		this.setModal(true);
		ImageIcon backboneIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-arrows-split.png"));
		this.setIconImage(backboneIcon.getImage());
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
		JLabel statementTypeLabel = new JLabel("Statement type");
		String statementTypeToolTip = 
				"<html><p width=\"500\">A network is constructed based on the variables from a specific statement type. "
				+ "Select here which statement type to use.</p></html>";
		statementTypeLabel.setToolTipText(statementTypeToolTip);
		settingsPanel.add(statementTypeLabel, gbc);
		
		gbc.gridx = 1;
		JLabel penaltyLabel = new JLabel("Penalty parameter (p)");
		String penaltyTooltipText = "<html><p width=\"500\">The penalty (p) determines how large the " +
				"backbone set is, with larger penalties penalizing larger backbones more strongly, hence leading to " +
				"smaller backbones and larger redundant sets. Typical values could be 5.5, 7.5, or 12.</p></html>";
		penaltyLabel.setToolTipText(penaltyTooltipText);
		settingsPanel.add(penaltyLabel, gbc);

		gbc.gridx = 2;
		JLabel iterationsLabel = new JLabel("Iterations (T)");
		String iterationsTooltipText = "<html><p width=\"500\">The number of iterations (T) determines how long the " +
				"algorithm should run. We have had good results with T = 50,000 iterations for a network of about " +
				"200 actors and about 60 concepts, though fewer iterations might have been acceptable. The quality " +
				"of the solution increases with larger T and reaches the global optimum asymptotically. How many " +
				"iterations are required for a good solution depends on the number of entities on the second-mode " +
				"variable.</p></html>";
		iterationsLabel.setToolTipText(iterationsTooltipText);
		settingsPanel.add(iterationsLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		this.statementTypes = Dna.sql.getStatementTypes()
				.stream()
				.filter(s -> s.getVariables()
						.stream()
						.filter(v -> v.getDataType().equals("short text"))
						.count() > 1)
				.sorted((s1, s2) -> Integer.valueOf(s1.getId()).compareTo(Integer.valueOf(s2.getId())))
				.toArray(StatementType[]::new);
		if (statementTypes.length < 1) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[GUI] No suitable statement types for network export.",
					"The network export dialog window has tried to populate the statement type combo box with statement types, but there was not a single statement type containing at least two short text variables that could be used for network construction.");
			Dna.logger.log(l);
		}
		StatementTypeComboBoxRenderer cbrenderer = new StatementTypeComboBoxRenderer();
		statementTypeBox = new JComboBox<StatementType>(statementTypes);
		statementTypeBox.setRenderer(cbrenderer);
		statementTypeBox.setToolTipText(statementTypeToolTip);
		settingsPanel.add(statementTypeBox, gbc);
		final int HEIGHT = (int) statementTypeBox.getPreferredSize().getHeight();
		final int WIDTH = 200;
		Dimension d = new Dimension(WIDTH, HEIGHT);
		statementTypeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StatementType selected = (StatementType) statementTypeBox.getSelectedItem();
				String[] varItems = selected.getVariablesList(false, true, false, false);
				String[] qualifierItems = selected.getVariablesList(false, true, true, true);
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
					String[] nonTextItems = ((StatementType) statementTypeBox.getSelectedItem()).getVariablesList(false, false, true, true);
					if (nonTextItems.length > 0) {
						qualifierBox.setSelectedItem(nonTextItems[0]);
					} else {
						int newIndex = 0;
						for (int i = 0; i < qualifierItems.length; i++) {
							if (!qualifierItems[i].equals(((String) var1Box.getSelectedItem())) && !qualifierItems[i].equals(((String) var2Box.getSelectedItem()))) {
								newIndex = i;
								break;
							}
						}
						qualifierBox.setSelectedIndex(newIndex);
					}
				}
				populateExcludeVariableList();
				excludeVariableList.setSelectedIndex(0);
				excludePreviewArea.setText("");
			}
		});


		gbc.gridx = 1;
		SpinnerNumberModel penaltyModel = new SpinnerNumberModel(3.50, 0.00, 1000.00, 0.10);
		JSpinner penaltySpinner = new JSpinner(penaltyModel);
		penaltyLabel.setLabelFor(penaltySpinner);
		((JSpinner.DefaultEditor) penaltySpinner.getEditor()).getTextField().setColumns(4);
		penaltySpinner.setToolTipText(penaltyTooltipText);
		settingsPanel.add(penaltySpinner, gbc);

		gbc.gridx = 2;
		SpinnerNumberModel iterationsModel = new SpinnerNumberModel(10000, 0, 1000000, 1000);
		JSpinner iterationsSpinner = new JSpinner(iterationsModel);
		((JSpinner.DefaultEditor) iterationsSpinner.getEditor()).getTextField().setColumns(7);
		iterationsLabel.setLabelFor(iterationsSpinner);
		iterationsSpinner.setToolTipText(iterationsTooltipText);
		settingsPanel.add(iterationsSpinner, gbc);

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
		String[] varItems = ((StatementType) statementTypeBox.getSelectedItem()).getVariablesList(false, true, false, false);
		var1Box = new JComboBox<String>(varItems);
		var1Box.setToolTipText(var1ToolTip);
		var1Box.setSelectedIndex(0);
		settingsPanel.add(var1Box, gbc);
		int HEIGHT2 = (int) var1Box.getPreferredSize().getHeight();
		var1Box.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		var2Box = new JComboBox<String>(varItems);
		var2Box.setToolTipText(var2ToolTip);
		var2Box.setSelectedIndex(1);
		settingsPanel.add(var2Box, gbc);
		var2Box.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
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
		String[] qualifierItems = ((StatementType) statementTypeBox.getSelectedItem()).getVariablesList(false, true, true, true);
		qualifierBox = new JComboBox<>(qualifierItems);
		if (qualifierItems.length > 0) {
			String[] nonTextItems = ((StatementType) statementTypeBox.getSelectedItem()).getVariablesList(false, false, true, true);
			if (nonTextItems.length > 0) {
				qualifierBox.setSelectedItem(nonTextItems[0]);
			} else {
				int newIndex = 0;
				for (int i = 0; i < qualifierItems.length; i++) {
					if (!qualifierItems[i].equals(((String) var1Box.getSelectedItem())) && !qualifierItems[i].equals(((String) var2Box.getSelectedItem()))) {
						newIndex = i;
						break;
					}
				}
				qualifierBox.setSelectedIndex(newIndex);
			}
		}
		qualifierBox.setToolTipText(qualifierToolTip);
		settingsPanel.add(qualifierBox, gbc);
		qualifierBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		qualifierBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (qualifierBox.getItemCount() == 0) {
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
				} else {
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("congruence");
					aggregationBox.addItem("conflict");
					aggregationBox.addItem("subtract");
					aggregationBox.setSelectedItem("subtract");
				}
			}
		});

		gbc.gridx = 3;
		String[] aggregationItems = new String[] {"ignore", "congruence", "conflict", "subtract"};
		aggregationBox = new JComboBox<>(aggregationItems);
		aggregationBox.setSelectedItem("subtract");
		aggregationBox.setToolTipText(aggregationToolTip);
		settingsPanel.add(aggregationBox, gbc);
		aggregationBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));

		// third row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 4;
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

		gbc.gridx = 3;
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

		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 5;
		ImageIcon dateIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-calendar-event.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
		ImageIcon timeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-clock.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));

		DatePickerSettings startDateSettings = new DatePickerSettings();
		startDateSettings.setFormatForDatesCommonEra("dd MM yyyy");
		startDateSettings.setFormatForDatesBeforeCommonEra("dd MM uuuu");
		TimePickerSettings startTimeSettings = new TimePickerSettings();
		startTimeSettings.setFormatForDisplayTime("HH:mm:ss");
		startTimeSettings.setFormatForMenuTimes("HH:mm:ss");

		startPicker = new DateTimePicker(startDateSettings, startTimeSettings);
		startPicker.setDateTimeStrict(dateTimeRange[0]);
		JButton startDateButton = startPicker.getDatePicker().getComponentToggleCalendarButton();
		startDateButton.setText("");
		startDateButton.setIcon(dateIcon);
		startDateButton.setToolTipText(dateToolTip);
		JButton startTimeButton = startPicker.getTimePicker().getComponentToggleTimeMenuButton();
		startTimeButton.setText("");
		startTimeButton.setIcon(timeIcon);
		startTimeButton.setToolTipText(dateToolTip);
		startPicker.setToolTipText(dateToolTip);
		startPicker.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		settingsPanel.add(startPicker, gbc);

		DatePickerSettings stopDateSettings = new DatePickerSettings();
		stopDateSettings.setFormatForDatesCommonEra("dd MM yyyy");
		stopDateSettings.setFormatForDatesBeforeCommonEra("dd MM uuuu");
		TimePickerSettings stopTimeSettings = new TimePickerSettings();
		stopTimeSettings.setFormatForDisplayTime("HH:mm:ss");
		stopTimeSettings.setFormatForMenuTimes("HH:mm:ss");

		gbc.gridx = 1;
		stopPicker = new DateTimePicker(stopDateSettings, stopTimeSettings);
		stopPicker.setDateTimeStrict(dateTimeRange[1]);
		JButton stopDateButton = stopPicker.getDatePicker().getComponentToggleCalendarButton();
		stopDateButton.setText("");
		stopDateButton.setIcon(dateIcon);
		stopDateButton.setToolTipText(dateToolTip);
		JButton stopTimeButton = stopPicker.getTimePicker().getComponentToggleTimeMenuButton();
		stopTimeButton.setText("");
		stopTimeButton.setIcon(timeIcon);
		stopTimeButton.setToolTipText(dateToolTip);
		stopPicker.setToolTipText(dateToolTip);
		stopPicker.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		settingsPanel.add(stopPicker, gbc);

		gbc.gridx = 2;
		String[] duplicatesItems = new String[] {"include all duplicates", "ignore per document", "ignore per calendar week",
				"ignore per calendar month", "ignore per calendar year", "ignore across date range"};
		duplicatesBox = new JComboBox<>(duplicatesItems);
		duplicatesBox.setToolTipText(duplicatesToolTip);
		settingsPanel.add(duplicatesBox, gbc);
		duplicatesBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));

		gbc.gridx = 3;
		String[] normalizationItems = new String[] {"no", "average", "jaccard", "cosine"};
		normalizationBox = new JComboBox<>(normalizationItems);
		normalizationBox.setToolTipText(normalizationToolTip);
		settingsPanel.add(normalizationBox, gbc);
		normalizationBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));

		// fourth row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 6;
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
		gbc.gridy = 7;
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
		BackboneExporter.this.populateExcludeVariableList();
		excludeVariableList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selectedValue = excludeVariableList.getSelectedValue();
				if (!e.getValueIsAdjusting() && selectedValue != null) {
					String[] entriesArray;
					int[] indices;
					ArrayList<TableDocument> documents = Dna.sql.getTableDocuments(new int[0]);
					if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 1) { // document type variable
						entriesArray = documents
								.stream()
								.map(d -> d.getType())
								.distinct()
								.sorted((d1, d2) -> d1.compareTo(d2))
								.toArray(String[]::new);
						excludeValueList.setListData(entriesArray);
						indices = new int[excludeType.size()];
						for (int i = 0; i < entriesArray.length; i++) {
							for (int j = 0; j < excludeType.size(); j++) {
								if (entriesArray[i].equals(excludeType.get(j))) {
									indices[j] = i;
								}
							}
						}
					} else if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 2) { // document section variable
						entriesArray = documents
								.stream()
								.map(d -> d.getSection())
								.distinct()
								.sorted((d1, d2) -> d1.compareTo(d2))
								.toArray(String[]::new);
						excludeValueList.setListData(entriesArray);
						indices = new int[excludeSection.size()];
						for (int i = 0; i < entriesArray.length; i++) {
							for (int j = 0; j < excludeSection.size(); j++) {
								if (entriesArray[i].equals(excludeSection.get(j))) {
									indices[j] = i;
								}
							}
						}
					} else if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 3) { // document source variable
						entriesArray = documents
								.stream()
								.map(d -> d.getSource())
								.distinct()
								.sorted((d1, d2) -> d1.compareTo(d2))
								.toArray(String[]::new);
						excludeValueList.setListData(entriesArray);
						indices = new int[excludeSource.size()];
						for (int i = 0; i < entriesArray.length; i++) {
							for (int j = 0; j < excludeSource.size(); j++) {
								if (entriesArray[i].equals(excludeSource.get(j))) {
									indices[j] = i;
								}
							}
						}
					} else if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 4) { // document author variable
						entriesArray = documents
								.stream()
								.map(d -> d.getAuthor())
								.distinct()
								.sorted((d1, d2) -> d1.compareTo(d2))
								.toArray(String[]::new);
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
						entriesArray = Dna.sql.getUniqueValues(((StatementType) statementTypeBox.getSelectedItem()).getId(), selectedValue)
								.stream()
								.toArray(String[]::new);
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
				if (!e.getValueIsAdjusting()) {
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
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 8;
		helpBox = new JCheckBox("Display instructions");
		helpBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleHelp();
			}
			
		});
		settingsPanel.add(helpBox, gbc);

		gbc.gridwidth = 3;
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton revertButton = new JButton("Revert", new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-backspace.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
		
		String revertToolTip = "<html><p>Reset all settings to their default values.</p></html>";
		revertButton.setToolTipText(revertToolTip);
		buttonPanel.add(revertButton);
		revertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int statementTypeIndex = -1;
				for (int i = 0; i < BackboneExporter.this.statementTypes.length; i++) {
					String[] vars = BackboneExporter.this.statementTypes[i].getVariablesList(false, true, false, false);
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
					int varIndex = ((DefaultComboBoxModel) var1Box.getModel()).getIndexOf("organization");
					if (varIndex > -1) {
						var1Box.setSelectedIndex(varIndex);
					} else {
						var1Box.setSelectedIndex(0);
					}
					varIndex = ((DefaultComboBoxModel) var2Box.getModel()).getIndexOf("concept");
					if (varIndex > -1) {
						var2Box.setSelectedIndex(varIndex);
					} else {
						var2Box.setSelectedIndex(1);
					}
					if (var1Box.getSelectedIndex() == var2Box.getSelectedIndex()) {
						var1Box.setSelectedIndex(0);
						var2Box.setSelectedIndex(1);
					}
				}
				if (qualifierBox.getItemCount() > 0) {
					String[] nonTextItems = ((StatementType) statementTypeBox.getSelectedItem()).getVariablesList(false, false, true, true);
					if (nonTextItems.length > 0) {
						qualifierBox.setSelectedItem(nonTextItems[0]);
					} else {
						int newIndex = 0;
						for (int i = 0; i < qualifierItems.length; i++) {
							if (!qualifierItems[i].equals(((String) var1Box.getSelectedItem())) && !qualifierItems[i].equals(((String) var2Box.getSelectedItem()))) {
								newIndex = i;
								break;
							}
						}
						qualifierBox.setSelectedIndex(newIndex);
					}
				}
				if (aggregationBox.getItemCount() == 4) {
					aggregationBox.setSelectedIndex(3); // "subtract"
				} else {
					aggregationBox.setSelectedIndex(0);
				}
				normalizationBox.setSelectedIndex(1);
				duplicatesBox.setSelectedIndex(1);
		        startPicker.setDateTimeStrict(BackboneExporter.this.dateTimeRange[0]);
		        stopPicker.setDateTimeStrict(BackboneExporter.this.dateTimeRange[1]);
				excludeVariableList.setSelectedIndex(0);
				excludePreviewArea.setText("");
				helpBox.setSelected(false);
				backboneButton.setEnabled(true);
				exportXmlButton.setEnabled(false);
				exportJsonButton.setEnabled(false);
			}
		});

		JButton cancelButton = new JButton("Cancel", new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
		String cancelToolTip = "<html><p>Reset and close this window.</p></html>";
		cancelButton.setToolTipText(cancelToolTip);
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		backboneButton = new JButton("Find backbone...", new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-arrows-split.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
		String backboneToolTip = "<html><p width=\"500\">Find the backbone and redundant set of second-mode entities. " +
				"This functionality is only available with <b>one-mode networks, no isolates, and no time window</b>. " +
				"The <b>backbone set</b> is the subset of entities/values of Variable 2 (e.g., concepts) that can " +
				"reproduce the cluster structure of the one-mode network almost perfectly, i.e., with little loss. " +
				"The <b>redundant set</b> is the complementary subset of entities/values of Variable 2 that do not " +
				"contribute much additional value in structuring the one-mode network into clusters. A custom " +
				"<b>simulated annealing</b> algorithm is employed to find the backbone and redundant sets by " +
				"minimizing penalized Euclidean spectral distances between the backbone network and the full network. " +
				"The results can inform how to recode entities or which entities to include or exclude during network " +
				"export. By pressing this button, the calculation will start, but the results will not be saved to " +
				"a file yet.</p></html>";
		backboneButton.setToolTipText(backboneToolTip);
		buttonPanel.add(backboneButton);
		backboneButton.addActionListener(al -> {
			// read settings from GUI elements and translate into values for Exporter class
			double penalty = (double) penaltySpinner.getValue();
			int iterations = (int) iterationsSpinner.getValue();
			StatementType statementType = (StatementType) statementTypeBox.getSelectedItem();
			String variable1Name = (String) var1Box.getSelectedItem();
			boolean variable1Document = var1Box.getSelectedIndex() > var1Box.getItemCount() - 7;
			String variable2Name = (String) var2Box.getSelectedItem();
			boolean variable2Document = var2Box.getSelectedIndex() > var2Box.getItemCount() - 7;
			String qualifier = (String) qualifierBox.getSelectedItem();
			boolean qualifierDocument = qualifierBox.getSelectedIndex() > qualifierBox.getItemCount() - 7;
			String qualifierAggregation = (String) aggregationBox.getSelectedItem();
			String normalization = (String) normalizationBox.getSelectedItem();

			String duplicates = (String) duplicatesBox.getSelectedItem();
			if (duplicates.equals("include all duplicates")) {
				duplicates = "include";
			} else if (duplicates.equals("ignore per document")) {
				duplicates = "document";
			} else if (duplicates.equals("ignore per calendar week")) {
				duplicates = "week";
			} else if (duplicates.equals("ignore per calendar month")) {
				duplicates = "month";
			} else if (duplicates.equals("ignore per calendar year")) {
				duplicates = "year";
			} else if (duplicates.equals("ignore across date range")) {
				duplicates = "acrossrange";
			}
			LocalDateTime startDateTime = startPicker.getDateTimeStrict();
			LocalDateTime stopDateTime = stopPicker.getDateTimeStrict();

			// start backbone thread
			Thread backboneThread = new Thread(new GuiBackboneThread(penalty, iterations, statementType, variable1Name,
					variable1Document, variable2Name, variable2Document, qualifier, qualifierDocument,
					qualifierAggregation, normalization, duplicates, startDateTime, stopDateTime,
					BackboneExporter.this.excludeValues, BackboneExporter.this.excludeAuthor,
					BackboneExporter.this.excludeSource, BackboneExporter.this.excludeSection,
					BackboneExporter.this.excludeType, false, false, false,
					false, false), "Find backbone");
			backboneThread.start();
		});

		exportXmlButton = new JButton("Export XML file...", new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-code.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
		String exportXmlToolTip = "<html><p>Select a file name and save the computed results to an XML file.</p></html>";
		exportXmlButton.setToolTipText(exportXmlToolTip);
		exportXmlButton.setEnabled(false);
		exportXmlButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.removeChoosableFileFilter(fc.getFileFilter());
				fc.addChoosableFileFilter(new FileNameExtensionFilter("XMl files (*.xml)", "xml"));
				int returnVal = fc.showSaveDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String fileName = file.getAbsolutePath();
					if (!fileName.toLowerCase().endsWith(".xml")) {
						fileName = fileName + ".xml";
					}
					BackboneExporter.this.exporter.writeBackboneToFile(fileName);
					JOptionPane.showMessageDialog(BackboneExporter.this, "Data were exported to \"" + fileName + "\".");
				}
			}
		});
		buttonPanel.add(exportXmlButton);

		exportJsonButton = new JButton("Export JSON file...", new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-code.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
		String exportJsonToolTip = "<html><p>Select a file name and save the computed results to a JSON file.</p></html>";
		exportJsonButton.setToolTipText(exportJsonToolTip);
		exportJsonButton.setEnabled(false);
		exportJsonButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.removeChoosableFileFilter(fc.getFileFilter());
				fc.addChoosableFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
				int returnVal = fc.showSaveDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String fileName = file.getAbsolutePath();
					if (!fileName.toLowerCase().endsWith(".json")) {
						fileName = fileName + ".json";
					}
					BackboneExporter.this.exporter.writeBackboneToFile(fileName);
					JOptionPane.showMessageDialog(BackboneExporter.this, "Data were exported to \"" + fileName + "\".");
				}
			}
		});
		buttonPanel.add(exportJsonButton);

		toggleHelp();
		settingsPanel.add(buttonPanel, gbc);

		// set defaults
		if (var1Box.getItemCount() > 1) {
			int varIndex = ((DefaultComboBoxModel) var1Box.getModel()).getIndexOf("organization");
			if (varIndex > -1) {
				var1Box.setSelectedIndex(varIndex);
			} else {
				var1Box.setSelectedIndex(0);
			}
			varIndex = ((DefaultComboBoxModel) var2Box.getModel()).getIndexOf("concept");
			if (varIndex > -1) {
				var2Box.setSelectedIndex(varIndex);
			} else {
				var2Box.setSelectedIndex(1);
			}
			if (var1Box.getSelectedIndex() == var2Box.getSelectedIndex()) {
				var1Box.setSelectedIndex(0);
				var2Box.setSelectedIndex(1);
			}
		}
		if (qualifierBox.getItemCount() > 0) {
			String[] nonTextItems = ((StatementType) statementTypeBox.getSelectedItem()).getVariablesList(false, false, true, true);
			if (nonTextItems.length > 0) {
				qualifierBox.setSelectedItem(nonTextItems[0]);
			} else {
				int newIndex = 0;
				for (int i = 0; i < qualifierItems.length; i++) {
					if (!qualifierItems[i].equals(((String) var1Box.getSelectedItem())) && !qualifierItems[i].equals(((String) var2Box.getSelectedItem()))) {
						newIndex = i;
						break;
					}
				}
				qualifierBox.setSelectedIndex(newIndex);
			}
		}
		if (aggregationBox.getItemCount() == 4) {
			aggregationBox.setSelectedIndex(3); // "subtract"
		} else {
			aggregationBox.setSelectedIndex(0);
		}
		normalizationBox.setSelectedIndex(1);
		duplicatesBox.setSelectedIndex(1);
		
		this.add(settingsPanel, BorderLayout.CENTER);
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
		String[] excludeVariableItems = selected.getVariablesList(true, true, true, true);
		DefaultListModel<String> excludeVariableModel = new DefaultListModel<String>();
		for (int i = 0; i < excludeVariableItems.length - 6; i++) {
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
		ToolTipManager.sharedInstance().setInitialDelay(10);
		if (helpBox.isSelected()) {
			ToolTipManager.sharedInstance().setEnabled(true);
			ToolTipManager.sharedInstance().setDismissDelay(999999);
		} else {
			ToolTipManager.sharedInstance().setEnabled(false);
			ToolTipManager.sharedInstance().setDismissDelay(0);
		}
	}

	/**
	 * GUI backbone thread. This is where the computations are executed and the
	 * data are written to a file.
	 */
	private class GuiBackboneThread implements Runnable {

		private double p;
		private int T;
		private String variable1, variable2, qualifier, qualifierAggregation, normalization, duplicates;
		private StatementType statementType;
		private boolean variable1Document, variable2Document, qualifierDocument, invertValues, invertAuthors, invertSources, invertSections, invertTypes;
		private LocalDateTime startDateTime, stopDateTime;
		private HashMap<String, ArrayList<String>> excludeValues;
		private ArrayList<String> excludeAuthors, excludeSources, excludeSections, excludeTypes;
		private ProgressMonitor progressMonitor;

		public GuiBackboneThread(
				double p,
				int T,
				StatementType statementType,
				String variable1,
				boolean variable1Document,
				String variable2,
				boolean variable2Document,
				String qualifier,
				boolean qualifierDocument,
				String qualifierAggregation,
				String normalization,
				String duplicates,
				LocalDateTime startDateTime,
				LocalDateTime stopDateTime,
				HashMap<String, ArrayList<String>> excludeValues,
				ArrayList<String> excludeAuthors,
				ArrayList<String> excludeSources,
				ArrayList<String> excludeSections,
				ArrayList<String> excludeTypes,
				boolean invertValues,
				boolean invertAuthors,
				boolean invertSources,
				boolean invertSections,
				boolean invertTypes) {
			this.p = p;
			this.T = T;
			this.statementType = statementType;
			this.variable1 = variable1;
			this.variable1Document = variable1Document;
			this.variable2 = variable2;
			this.variable2Document = variable2Document;
			this.qualifier = qualifier;
			this.qualifierDocument = qualifierDocument;
			this.qualifierAggregation = qualifierAggregation;
			this.normalization = normalization;
			this.duplicates = duplicates;
			this.startDateTime = startDateTime;
			this.stopDateTime = stopDateTime;
			this.excludeValues = excludeValues;
			this.excludeAuthors = excludeAuthors;
			this.excludeSources = excludeSources;
			this.excludeSections = excludeSections;
			this.excludeTypes = excludeTypes;
			this.invertValues = invertValues;
			this.invertAuthors = invertAuthors;
			this.invertSources = invertSources;
			this.invertSections = invertSections;
			this.invertTypes = invertTypes;
		}

		public void run() {
			backboneButton.setEnabled(false);
			exportJsonButton.setEnabled(false);
			exportXmlButton.setEnabled(false);
			boolean proceed = true;

			// step 1: create Exporter object and filter data
			progressMonitor = new ProgressMonitor(BackboneExporter.this, "Finding backbone:", "Filtering statements...", 0, T);
			progressMonitor.setMillisToDecideToPopup(1);
			// delay the process by a second to make sure the progress monitor shows up
			// see here: https://coderanch.com/t/631127/java/progress-monitor-showing
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			progressMonitor.setProgress(0);
			Exporter exporter = new Exporter(
					"onemode",
					this.statementType,
					this.variable1,
					this.variable1Document,
					this.variable2,
					this.variable2Document,
					this.qualifier,
					this.qualifierDocument,
					this.qualifierAggregation,
					this.normalization,
					true,
					this.duplicates,
					this.startDateTime,
					this.stopDateTime,
					"no",
					1,
					this.excludeValues,
					this.excludeAuthors,
					this.excludeSources,
					this.excludeSections,
					this.excludeTypes,
					this.invertValues,
					this.invertAuthors,
					this.invertSources,
					this.invertSections,
					this.invertTypes,
					null,
					null);
			if (progressMonitor.isCanceled()) {
				proceed = false;
				progressMonitor.setProgress(2);
				progressMonitor.close();
			}

			// step 2: initialize algorithm
			if (proceed) {
				exporter.loadData();
				exporter.filterStatements();
				if (exporter.getFilteredStatements().size() == 0) {
					proceed = false;
					LogEvent le = new LogEvent(Logger.ERROR,
							"No statements left after filtering.",
							"Attempted to filtered the statements by date and other criteria before finding backbone. But no statements were left after applying the filters. Perhaps the time period was mis-specified?");
					Dna.logger.log(le);
				}
			}
			if (proceed) {
				progressMonitor.setNote("Computing initial networks...");
				exporter.backbone(p, T);
			}
			if (!proceed || progressMonitor.isCanceled()) {
				proceed = false;
			}

			// step 3: simulated annealing
			if (proceed) {
				progressMonitor.setNote("Simulated annealing...");
				try (ProgressBar pb = new ProgressBar("Simulated annealing...", T)) {
					while (exporter.getCurrentT() <= T && !progressMonitor.isCanceled()) { // run up to upper bound of iterations T, provided by the user
						if (progressMonitor.isCanceled()) {
							exporter.setCurrentT(T);
							pb.stepTo(T);
							break;
						} else {
							pb.stepTo(exporter.getCurrentT());
							progressMonitor.setProgress(exporter.getCurrentT());
							exporter.iterateBackbone();
						}
					}
					if (!progressMonitor.isCanceled()) {
						exporter.saveBackboneResult();
						BackboneExporter.this.exporter = exporter;
						progressMonitor.setProgress(T);
					}
				} finally {
					if (progressMonitor.isCanceled()) {
						System.err.println("Canceled.");
					}
				}
			}
			if (progressMonitor.isCanceled()) {
				JOptionPane.showMessageDialog(BackboneExporter.this, "Backbone finder was canceled.");
			} else {
				if (proceed) {
					JOptionPane.showMessageDialog(BackboneExporter.this, "Backbone and redundant set have been computed.");
					exportXmlButton.setEnabled(true);
					exportJsonButton.setEnabled(true);
				} else {
					JOptionPane.showMessageDialog(BackboneExporter.this, "The backbone could not be computed. Perhaps there were no statements in the selected time period?");
				}
			}
			progressMonitor.close();
			backboneButton.setEnabled(true);
		}
	}
}