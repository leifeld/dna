package dna;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

/**
 * This is the export component of the Discourse Analyzer (DNA). You 
 * can use it to convert a .dna file into a network file. Bipartite 
 * graphs, co-occurrence networks as well as some advanced algorithms 
 * are offered. Network data can be exported as DL files (Ucinet), 
 * graphML (visone) or CSV files (spreadsheet).
 * 
 * @author Philip Leifeld
 * @version 1.27e1 - 2011-04-06
 */
@SuppressWarnings("serial")
public class ExportWindow extends JFrame {
	
	Container c;
	StatementContainer sCont;
	
	CardNetworkTypePanel cardNetworkTypePanel;
	TwoModeTypePanel twoModeTypePanel;
	OneModeTypePanel oneModeTypePanel;
	FormatPanel formatPanel;
	AlgorithmPanel algorithmPanel;
	DatePanel datePanel;
	AgreementPanel agreementPanel;
	ButtonPanel buttonPanel;
	TimeWindowPanel timeWindowPanel;
	CommetrixPanel commetrixPanel;
	SoniaPanel soniaPanel;
	AttenuationPanel attenuationPanel;
	CoOccurrencePanel coOccurrencePanel;
	NormalizationPanel normalizationPanel;
	//EmptyPanel emptyPanel;
	CardPanel cardPanel;
	ExcludePanel excludePanel;
	
	/**
	 * Constructor.
	 * 
	 * @param arguments
	 */
	public ExportWindow() {
		sCont = new StatementContainer();
		sCont = dna.Dna.mainProgram.dc.sc;
		exportWindow();
		resetAll();
	}
	
	public void exportWindow() {
		this.setTitle("DNA Network Export");
		ImageIcon exportIcon = new ImageIcon(getClass().getResource("/icons/chart_organisation.png"));
		this.setIconImage(exportIcon.getImage());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		appearance();
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void resetAll() {
		algorithmPanel.reset();
		datePanel.reset();
		agreementPanel.reset();
		oneModeTypePanel.reset();
		twoModeTypePanel.reset();
		excludePanel.reset();
		normalizationPanel.reset();
		commetrixPanel.reset();
		soniaPanel.reset();
		attenuationPanel.reset();
		timeWindowPanel.reset();
		coOccurrencePanel.reset();
		formatPanel.reset();
		pack();
	}
	
	public void appearance() {
		
		c = getContentPane();
		
		JPanel appearancePanel = new JPanel(new BorderLayout()); //overall layout
		JPanel basicOptionsPanel = new JPanel(new GridBagLayout()); //center of the options panel
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(2,2,2,2);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		cardNetworkTypePanel = new CardNetworkTypePanel();
		CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
	    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
		basicOptionsPanel.add(cardNetworkTypePanel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		algorithmPanel = new AlgorithmPanel();
		basicOptionsPanel.add(algorithmPanel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		formatPanel = new FormatPanel();
		basicOptionsPanel.add(formatPanel, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		JPanel rightPanel = new JPanel(new BorderLayout());
		datePanel = new DatePanel();
		agreementPanel = new AgreementPanel();
		rightPanel.add(datePanel, BorderLayout.NORTH);
		rightPanel.add(agreementPanel, BorderLayout.SOUTH);
		basicOptionsPanel.add(rightPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		cardPanel = new CardPanel();
		CardLayout cl = (CardLayout)(cardPanel.getLayout());
	    cl.show(cardPanel, "emptyPanel");
		basicOptionsPanel.add(cardPanel, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		normalizationPanel = new NormalizationPanel();
		basicOptionsPanel.add(normalizationPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.BOTH;
		excludePanel = new ExcludePanel();
		basicOptionsPanel.add(excludePanel, gbc);
		
		gbc.gridy = 4;
		buttonPanel = new ButtonPanel();
		basicOptionsPanel.add(buttonPanel, gbc);
		
		appearancePanel.add(basicOptionsPanel, BorderLayout.NORTH);
		
		c.add(appearancePanel);
	}
	
	/**
	 * Panel with exclude lists.
	 */
	public class ExcludePanel extends JPanel {
		
		JList person, organization, category;
		DefaultListModel personModel, organizationModel, categoryModel;
		JScrollPane personScroller, organizationScroller, categoryScroller;
		
		public ExcludePanel() {
			
			setBorder( new TitledBorder( new EtchedBorder(), "Exclude actors or categories (press ctrl)" ) );
			
			JPanel personPanel = new JPanel(new BorderLayout());
			personModel = new DefaultListModel();
			person = new JList(personModel);
			person.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			person.setLayoutOrientation(JList.VERTICAL);
			person.setVisibleRowCount(5);
			person.setFixedCellWidth(30);
			personScroller = new JScrollPane(person);
			JLabel personLabel = new JLabel("persons");
			personPanel.add(personLabel, BorderLayout.NORTH);
			personPanel.add(personScroller, BorderLayout.CENTER);
			
			JPanel organizationPanel = new JPanel(new BorderLayout());
			organizationModel = new DefaultListModel();
			organization = new JList(organizationModel);
			organization.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			organization.setLayoutOrientation(JList.VERTICAL);
			organization.setVisibleRowCount(5);
			organization.setFixedCellWidth(30);
			organizationScroller = new JScrollPane(organization);
			JLabel organizationLabel = new JLabel("organizations");
			organizationPanel.add(organizationLabel, BorderLayout.NORTH);
			organizationPanel.add(organizationScroller, BorderLayout.CENTER);
			
			JPanel categoryPanel = new JPanel(new BorderLayout());
			categoryModel = new DefaultListModel();
			category = new JList(categoryModel);
			category.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			category.setLayoutOrientation(JList.VERTICAL);
			category.setVisibleRowCount(5);
			category.setFixedCellWidth(30);
			categoryScroller = new JScrollPane(category);
			JLabel categoryLabel = new JLabel("categories");
			categoryPanel.add(categoryLabel, BorderLayout.NORTH);
			categoryPanel.add(categoryScroller, BorderLayout.CENTER);
			
			String toolTipText = "<html>From these three lists, you can select actors and/or <br>" +
					"categories that you do not want to export. DNA will act as <br>" +
					"if any statement containing the selected items were not <br>" +
					"encoded in the project file. Select multiple entries for <br>" +
					"exclusion by pressing Ctrl-V while clicking on an entry.</html>";
			setToolTipText(toolTipText);
			person.setToolTipText(toolTipText);
			personLabel.setToolTipText(toolTipText);
			organization.setToolTipText(toolTipText);
			organizationLabel.setToolTipText(toolTipText);
			category.setToolTipText(toolTipText);
			categoryLabel.setToolTipText(toolTipText);
			
			setLayout(new GridLayout(2,1));
			JPanel horizontalPanel = new JPanel(new GridLayout(1,2));
			horizontalPanel.add(personPanel);
			horizontalPanel.add(organizationPanel);
			add(horizontalPanel);
			add(categoryPanel);
		}
		
		public void reset() {
			personModel.clear();
			organizationModel.clear();
			categoryModel.clear();
			
			try {
				ArrayList<String> persons = sCont.getPersonList();
				ArrayList<String> organizations = sCont.getOrganizationList();
				ArrayList<String> categories = sCont.getCategoryList();
				
				for (int i = 0; i < persons.size(); i++) {
					personModel.addElement(persons.get(i));
				}
				for (int i = 0; i < organizations.size(); i++) {
					organizationModel.addElement(organizations.get(i));
				}
				for (int i = 0; i < categories.size(); i++) {
					categoryModel.addElement(categories.get(i));
				}
			} catch (NullPointerException npe) {
				//no file was open
			}
			
		}
	}
	
	/**
	 * Panel with card layout for custom options.
	 */
	public class CardPanel extends JPanel {
		
		public CardPanel() {
			
			setLayout(new CardLayout());
			
			timeWindowPanel = new TimeWindowPanel();
			commetrixPanel = new CommetrixPanel();
			soniaPanel = new SoniaPanel();
			attenuationPanel = new AttenuationPanel();
			//emptyPanel = new EmptyPanel();
			coOccurrencePanel = new CoOccurrencePanel();
			
			add(timeWindowPanel, "timeWindowPanel");
			add(commetrixPanel, "commetrixPanel");
			add(soniaPanel, "soniaPanel");
			add(attenuationPanel, "attenuationPanel");
			//add(emptyPanel, "emptyPanel");
			add(coOccurrencePanel, "coOccurrencePanel");
		}
		
	}
	
	/**
	 * Custom option panel without any options.
	 */
	/*
	public class EmptyPanel extends JPanel {
		
		public EmptyPanel() {
			
			setLayout(new FlowLayout(FlowLayout.LEFT));
			setBorder( new TitledBorder( new EtchedBorder(), "Custom options" ) );
			
			JLabel label = new JLabel("(no options available)");
			
			String toolTipText = "<html>This panel displays specific options for each algorithm. The <br>" +
					"algorithm you have selected does not have any such parameters.</html>";
			setToolTipText(toolTipText);
			
			add(label);
		}
	}
	*/
	
	/**
	 * Custom option panel for co-occurrence and attenuation options.
	 */
	public class CoOccurrencePanel extends JPanel {
		
		JCheckBox includeIsolates;
		JCheckBox ignoreDuplicates;
		
		public CoOccurrencePanel() {
			
			setLayout(new GridLayout(2,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Custom options" ) );
			
			includeIsolates = new JCheckBox("include isolates", false);
			ignoreDuplicates = new JCheckBox("ignore duplicate statements", true);
			
			includeIsolates.setToolTipText("<html>If you export a time slice (i.e. you set a certain <br>" +
					"start or end date), there may be inactive vertices <br>" +
					"in the data set which do not connect to the other <br>" +
					"vertices in the time period you are exporting. Inac-<br>" +
					"tive vertices can also occur if you use the exclude <br>" +
					"lists. This option determines whether the isolates<br>" +
					"should be included in the export file.</html>");
			
			ignoreDuplicates.setToolTipText("<html>Disable this option to count repeated occurrences of the same<br>" +
					"statement (albeit with a different text) within the time range.<br>" +
					"Warning: If this option is disabled, you can no longer distinguish<br>" +
					"between many different statements occurring only once each, and<br>" +
					"few statements occurring rather frequently in the same article.</html>");
			
			add(ignoreDuplicates);
			add(includeIsolates);
		}
		
		public void reset() {
			includeIsolates.setSelected(false);
			ignoreDuplicates.setSelected(true);
		}
	}
	
	/**
	 * Custom option panel for co-occurrence and attenuation options.
	 */
	public class NormalizationPanel extends JPanel {
		
		JCheckBox normalization;
		
		public NormalizationPanel() {
			
			setLayout(new FlowLayout(FlowLayout.LEFT));
			setBorder( new TitledBorder( new EtchedBorder(), "Normalization" ) );
			
			normalization = new JCheckBox("normalize", false);
			
			String toolTipText = "<html>Normalization renders the edge weight independent from the <br" +
					"propensity of an actor to make statements. The trick is to use the <br>" +
					"empirically observed number of statements of each actor as a proxy <br>" +
					"of this propensity. Normalization may be sensible e.g. if you do <br>" +
					"not want the measured similarity of actors to be affected by their <br>" +
					"institutional roles. In the case of the co-occurrence algorithm, <br>" +
					"normalization will divide the edge weight by the average number of <br>" +
					"different categories of the two actors involved in an edge. In the <br>" +
					"case of the time window algorithm, normalization will then, on top <br>" +
					"of that, divide the result by the number of time windows (i.e. the <br>" +
					"'shift' parameter. In the case of the attenuation algorithm, normali- <br>" +
					"zation will divide each incremental portion of the edge weight of <br>" +
					"the directed graph by the number of different dates of the statement <br>" +
					"currently being processed before adding up these portions. A more <br>" +
					"detailed description of the algorithms and the normalization proce- <br>" +
					"dures will be provided in the documentation.</html>";
			setToolTipText(toolTipText);
			
			normalization.setToolTipText(toolTipText);
			add(normalization);
		}
		
		public void reset() {
			normalization.setSelected(false);
		}
	}
	
	/**
	 * Custom option panel for the Commetrix algorithm.
	 */
	public class CommetrixPanel extends JPanel {
		
		JSpinner chain;
		SpinnerNumberModel chainModel;
		JTextField networkName;
		
		public CommetrixPanel() {
			
			setLayout(new GridLayout(2,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Custom options: Commetrix" ) );
			
			JPanel upperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			JLabel days1 = new JLabel(" days");
			chainModel = new SpinnerNumberModel(getChDur(), 1, datePanel.getDuration(), 1);
			chain = new JSpinner(chainModel);
			JLabel chainLabel = new JLabel("backward window of ");
			
			String chainToolTipText = "<html>Whenever an actor makes a statement, previous occurrences of this <br>" +
					"statement of other actors are used to establish edges. These edges are <br>" +
					"tagged with the date of the current statement. The backward window para- <br>" +
					"meter restricts the time period of previous statements, i.e. how many <br>" +
					"days should we go back in time in order to establish edges?</html>";
			chain.setToolTipText(chainToolTipText);
			chainLabel.setToolTipText(chainToolTipText);
			days1.setToolTipText(chainToolTipText);
			
			upperPanel.add(chainLabel);
			upperPanel.add(chain);
			upperPanel.add(days1);
			
			JPanel lowerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			JLabel name = new JLabel("network name:");
			networkName = new JTextField("DNA_CMX", 15);
			
			String nameToolTipText = "<html>This is the name of the network inside the Commetrix file. <br>" +
					"If you don't know a suitable name, you can use the default value.</html>";
			name.setToolTipText(nameToolTipText);
			networkName.setToolTipText(nameToolTipText);
			
			lowerPanel.add(name);
			lowerPanel.add(networkName);
			
			add(upperPanel);
			add(lowerPanel);
		}
		
		private double getChDur() {
			double chDur;
			if (datePanel.getDuration() < 20) {
				chDur = datePanel.getDuration();
			} else {
				chDur = 20;
			}
			return chDur;
		}
		
		public void reset() {
			chainModel = new SpinnerNumberModel(getChDur(), 1, datePanel.getDuration(), 1);
			chain.setModel(chainModel);
		}
	}
	
	/**
	 * Custom option panel for the SoNIA algorithm.
	 */
	public class SoniaPanel extends JPanel {
		
		JSpinner backwardWindow, forwardWindow;
		SpinnerNumberModel backwardModel, forwardModel;
		
		public SoniaPanel() {
			
			setLayout(new GridLayout(2,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Custom options: SoNIA" ) );
			
			JPanel upperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			JLabel days1 = new JLabel(" days");
			JLabel days2 = new JLabel(" days");
			backwardModel = new SpinnerNumberModel(getChDur(), 1, datePanel.getDuration(), 1);
			forwardModel = new SpinnerNumberModel(1, 1, datePanel.getDuration(), 1);
			backwardWindow = new JSpinner(backwardModel);
			forwardWindow = new JSpinner(forwardModel);
			JLabel backwardLabel = new JLabel("backward window of ");
			JLabel forwardLabel = new JLabel("forward window of ");
			
			String backwardToolTipText = "<html>Whenever an actor makes a statement, previous occurrences of this <br>" +
					"statement of other actors are used to establish edges. These edges are <br>" +
					"tagged with the date of the current statement. The backward window para- <br>" +
					"meter restricts the time period of previous statements, i.e. how many <br>" +
					"days should we go back in time in order to establish edges?</html>";
			
			String forwardToolTipText = "<html>This parameter determines how long an edge is valid before it is<br>" +
					"disposed, i.e. how long should an edge be displayed after being established?</html>";
			
			backwardWindow.setToolTipText(backwardToolTipText);
			backwardLabel.setToolTipText(backwardToolTipText);
			forwardWindow.setToolTipText(forwardToolTipText);
			forwardLabel.setToolTipText(forwardToolTipText);
			days1.setToolTipText(backwardToolTipText);
			days2.setToolTipText(forwardToolTipText);
			
			upperPanel.add(backwardLabel);
			upperPanel.add(backwardWindow);
			upperPanel.add(days1);
			
			JPanel lowerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			lowerPanel.add(forwardLabel);
			lowerPanel.add(forwardWindow);
			lowerPanel.add(days2);
			
			add(upperPanel);
			add(lowerPanel);
		}
		
		private double getChDur() {
			double chDur;
			if (datePanel.getDuration() < 100) {
				chDur = datePanel.getDuration();
			} else {
				chDur = 100;
			}
			return chDur;
		}
		
		public void reset() {
			backwardModel = new SpinnerNumberModel(getChDur(), 1, datePanel.getDuration(), 1);
			backwardWindow.setModel(backwardModel);
			forwardModel = new SpinnerNumberModel(1, 1, datePanel.getDuration(), 1);
			forwardWindow.setModel(forwardModel);
		}
	}

	/**
	 * Custom option panel for the attenuation algorithm.
	 */
	public class AttenuationPanel extends JPanel {
		
		JSpinner lambdaSpinner;
		SpinnerNumberModel lambdaModel;
		
		public AttenuationPanel() {
			
			setLayout(new FlowLayout(FlowLayout.LEFT));
			setBorder( new TitledBorder( new EtchedBorder(), "Custom options: Attenuation" ) );
			
			JLabel days1 = new JLabel(" days");
			lambdaModel = new SpinnerNumberModel(0.10, 0.00, 9.99, 0.01);
			lambdaSpinner = new JSpinner(lambdaModel);
			JLabel lambdaLabel = new JLabel("Lambda decay constant ");
			
			String lambdaToolTipText = "<html>The lambda parameter controls the exponential  <br>" +
					"decay of the duration between statements when <br>" +
					"edges are established. Use smaller values to <br>" + 
					"incorporate longer time periods or larger values <br>" + 
					"for shorter interaction periods.</html>";
			lambdaSpinner.setToolTipText(lambdaToolTipText);
			lambdaLabel.setToolTipText(lambdaToolTipText);
			days1.setToolTipText(lambdaToolTipText);
			
			this.add(lambdaLabel);
			this.add(lambdaSpinner);
		}
		
		public void reset() {
			lambdaModel = new SpinnerNumberModel(0.10, 0.00, 9.99, 0.01);
			lambdaSpinner.setModel(lambdaModel);
		}
	}
	
	/**
	 * Custom option panel for the time window algorithm.
	 */
	public class TimeWindowPanel extends JPanel {
		
		JSpinner chain, shift, alphaSpinner;
		SpinnerNumberModel chainModel, shiftModel, alphaModel;
		
		public TimeWindowPanel() {
			
			setLayout(new BorderLayout());
			setBorder( new TitledBorder( new EtchedBorder(), "Custom options: Time window" ) );
			
			JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			ChangeListener cl = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (e.getSource().equals(chain)) {
						Double chValue = (Double)chain.getValue();
						shiftModel.setMaximum(chValue);
						if (chValue < (Double)shift.getValue()) {
							shiftModel.setValue(chValue);
						}
						
					}
				}
			};
			
			JLabel days1 = new JLabel(" window size    ");
			chainModel = new SpinnerNumberModel(getChDur(), 1, datePanel.getDuration(), 1);
			chain = new JSpinner(chainModel);
			chain.addChangeListener(cl);
			
			String chainToolTipText = "<html>When using the time window algorithm, there are two parameters. <br>" +
					"This parameter is called the chaining parameter. It specifies the <br>" +
					"size of a window that moves through the whole discourse. At each <br>" +
					"time step, only those edges will be established or considered that <br>" +
					"are within the time window. A useful parameter value is 20 days <br>" +
					"because this often reflects the time until the debate changes.</html>";
			
			chain.setToolTipText(chainToolTipText);
			days1.setToolTipText(chainToolTipText);

			shiftModel = new SpinnerNumberModel(1, 1, datePanel.getDuration(), 1);
			shift = new JSpinner(shiftModel);
			JLabel days2 = new JLabel(" step size");

			String shiftToolTipText = "<html>The second parameter of the time window algorithm is the shift <br>" +
			"parameter. It specifies by how many days the time window should <br>" +
			"move after every round. If you set a value of 1 day, the results <br>" +
			"will be precise, but calculations are slow. If you prefer non- <br>" +
			"overlapping time windows (i.e. discrete time units), you should <br>" +
			"set the shift parameter to the same value as the chaining para- <br>" +
			"meter, which is the maximum possible value.</html>";
			
			shift.setToolTipText(shiftToolTipText);
			days2.setToolTipText(shiftToolTipText);
			
			top.add(chain);
			top.add(days1);
			top.add(shift);
			top.add(days2);
			add(top, BorderLayout.NORTH);
			
			JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			JLabel alphaLabel = new JLabel("alpha constant (for normalization)");
			alphaModel = new SpinnerNumberModel(100.0, 1.0, 9999, 1.0);
			alphaSpinner = new JSpinner(alphaModel);
			
			String alphaToolTipText = "<html>Use this parameter to adjust the normalization of edge weights. <br>" +
			"The value is only used if normalization is checked. The alpha value <br>" +
			"will multiply the normalized edge weights to make them larger. A <br>" +
			"value of 100 should scale edge weights to about 1.0 in many real- <br>" +
			"world discourse networks. </html>";
			
			alphaLabel.setToolTipText(alphaToolTipText);
			alphaSpinner.setToolTipText(alphaToolTipText);
			
			bottom.add(alphaSpinner);
			bottom.add(alphaLabel);
			
			add(bottom, BorderLayout.SOUTH);
			shiftModel.setMaximum(getChDur());
			
		}
		
		private double getChDur() {
			double chDur;
			if (datePanel.getDuration() < 20) {
				chDur = datePanel.getDuration();
			} else {
				chDur = 20;
			}
			return chDur;
		}
		
		public void reset() {
			chainModel = new SpinnerNumberModel(getChDur(), 1, datePanel.getDuration(), 1);
			chain.setModel(chainModel);
			shiftModel = new SpinnerNumberModel(1, 1, getChDur(), 1);
			shift.setModel(shiftModel);
			alphaModel = new SpinnerNumberModel(100.0, 1.0, 9999, 1.0);
			alphaSpinner.setModel(alphaModel);
		}
	}
	
	/**
	 * Create a panel where the network format (CSV, DL, graphML) can be selected.
	 */
	public class FormatPanel extends JPanel {
		
		ButtonGroup formatGroup;
		JRadioButton csv, csvmatrix, mat, gml, comsql, son;
		
		public FormatPanel() {
			setLayout(new GridLayout(6,1)); //change from 6 to 7 to re-activate DL edgelist support
			setBorder( new TitledBorder( new EtchedBorder(), "Export format" ) );
			
			ActionListener formatButtonListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == son) {
						CardLayout cl = (CardLayout)(cardPanel.getLayout());
					    cl.show(cardPanel, "soniaPanel");
					} else if (e.getSource() == comsql) {
						CardLayout cl = (CardLayout)(cardPanel.getLayout());
					    cl.show(cardPanel, "commetrixPanel");
					} else if (e.getSource() == csv && algorithmPanel.affil.isSelected()) {
						CardLayout cl = (CardLayout)(cardPanel.getLayout());
					    cl.show(cardPanel, "emptyPanel");
					} else if (algorithmPanel.affil.isSelected()) {
						CardLayout cl = (CardLayout)(cardPanel.getLayout());
					    cl.show(cardPanel, "coOccurrencePanel");
					}
				}
			};
			
			formatGroup = new ButtonGroup();
			csvmatrix = new JRadioButton("CSV matrix", false);
			csvmatrix.addActionListener(formatButtonListener);
			csvmatrix.setToolTipText("<html>The network will be exported as a socio-matrix <br>" +
					"in a text file with comma-separated values.</html");
			formatGroup.add(csvmatrix);
			add(csvmatrix);
			mat = new JRadioButton("DL fullmatrix", true);
			mat.addActionListener(formatButtonListener);
			mat.setToolTipText("<html>The DL format can be imported by UCINET and visone. <br>" +
					"DL fullmatrix corresponds to the DL file specification <br>" +
					"where networks are stored as socio-matrices.</html>");
			formatGroup.add(mat);
			add(mat);
			
			gml = new JRadioButton("graphML", false);
			gml.addActionListener(formatButtonListener);
			gml.setToolTipText("<html>The graphML format is the native format of visone and other <br>" +
					"graph-drawing software packages. The export filter will not <br>" +
					"export any spatial positions of the vertices, so they will <br>" +
					"at first appear all at the same position. Please use layout <br>" +
					"algorithms to place your nodes in an appropriate way.</html>");
			formatGroup.add(gml);
			add(gml);
			comsql = new JRadioButton("Commetrix SQL", false);
			comsql.addActionListener(formatButtonListener);
			comsql.setToolTipText("<html>Commetrix is a software for the dynamic visualization of <br>" +
					"networks with continuous-time measures. DNA will export a format that <br>" +
					"can be read by the developers of Commetrix or by a tool called CMX <br>" +
					"Producer. They will produce a CMX file which can be read by the CMX <br>" +
					"Analyzer. The advantage of Commetrix is that you can see how the <br>" +
					"discourse evolves over time. You can directly evaluate when important <br>" +
					"changes happen in the discourse network and to what extent.</html>");
			formatGroup.add(comsql);
			add(comsql);
			son = new JRadioButton("SoNIA", false);
			son.addActionListener(formatButtonListener);
			son.setToolTipText("<html>SoNIA is a free software for the dynamic visualization of networks.</html>");
			formatGroup.add(son);
			add(son);
			csv = new JRadioButton("CSV list", false);
			csv.addActionListener(formatButtonListener);
			csv.setToolTipText("<html>" +
					"Comma-separated text file for use <br>" +
					"in spreadsheet/office or statistics <br>" +
					"software packages. This is <i>not</i> a <br>" +
					"network format! It only lists statements <br>" +
					"or persons with their affiliations.</html>");
			formatGroup.add(csv);
			add(csv);
		}
		
		public void reset() {
			mat.setSelected(true);
		}
	}
	
	public class AlgorithmPanel extends JPanel {
		
		public JRadioButton el, affil, xSec, tWind, atten, sonia;
		
		public AlgorithmPanel() {
			setLayout(new GridLayout(6,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Algorithm" ) );

			ActionListener algoListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource().equals(el)) {
						elAdjust();
					} else if (e.getSource().equals(affil)) {
						affilAdjust();
					} else if (e.getSource().equals(tWind)) {
						tWindAdjust();
					} else if (e.getSource().equals(atten)) {
						attenAdjust();
					} else {
						xSecAdjust();
					}
				}
			};
			
			ButtonGroup algoGroup = new ButtonGroup();
			affil = new JRadioButton("Affiliation network", false);
			affil.addActionListener(algoListener);
			affil.setToolTipText("The affiliation algorithm will produce a bipartite graph with binary edges.");
			algoGroup.add(affil);
			add(affil);
			xSec = new JRadioButton("Number of co-occurrences", true);
			xSec.addActionListener(algoListener);
			xSec.setToolTipText("<html>The co-occurrence algorithm will produce a one-mode network with the <br>" +
					"number of co-occurrences between two vertices as the edge weight. This <br>" +
					"can be interpreted as a measure of similarity between the vertices.</html>");
			algoGroup.add(xSec);
			add(xSec);
			tWind = new JRadioButton("Time window", false);
			tWind.addActionListener(algoListener);
			tWind.setToolTipText("<html>The time window algorithm will move a window through the discourse, i.e. from <br>" +
					"the beginning to the end of the discourse, and will establish links at each <br>" +
					"time step only if the statements of the source vertex and the target vertex <br>" +
					"are made within the time period of the window. This approach can account for <br>" +
					"the context of a political discourse since the meaning of statements can <br>" +
					"change over time. The result is a weighted network because values are added <br>" +
					"to the existing edge weights at each time step if a new edge is detected.</html>");
			algoGroup.add(tWind);
			add(tWind);
			atten = new JRadioButton("Attenuation", false);
			atten.addActionListener(algoListener);
			atten.setToolTipText("<html>The attenuation algorithm employs a decay function for edge weights. <br>" +
					"An additional assumption could be that political actors refer to each <br>" +
					"other by making a statement that somebody else made before. The proba- <br>" +
					"bility that the actor is really referring to the other actor can be <br>" +
					"conceptualized as being inversely proportional to the time that has <br>" +
					"passed since the earlier statement. The algorithm will compare the dates <br>" +
					"of statements and create a network of referrals between actors, where <br>" +
					"the edge weight is determined by the inverse duration between each <br>" +
					"pair of statements. Inverse weights are aggregated to a nuanced directed <br>" +
					"network of referrals between actors.</html>");
			algoGroup.add(atten);
			add(atten);
			el = new JRadioButton("Dynamic visualization", false);
			el.addActionListener(algoListener);
			el.setToolTipText("<html>This produces a list of edges, where each edge is affiliated with <br>" +
					"the date of the vertex with the statement that occurred later <br>" +
					"in the discourse, i.e. if actor A makes a statement on the 12th <br>" +
					"of June and Actor B makes a statement on the 24th of June, a <br>" +
					"binary edge between A and B will be recorded, and the edge will <br>" +
					"have the 24th of July as the date of its creation. This algorithm <br>" +
					"is useful for dynamic, continuous-time visualization of network <br>" +
					"evolution as featured in Commetrix or SoNIA. If you use SoNIA, <br>" +
					"you additionally have to select a duration after which an edge <br>" +
					"expires. If you use Commetrix, this can be done inside Commetrix.</html>");
			algoGroup.add(el);
			add(el);
		}
		
		public void elAdjust() {
			formatPanel.csvmatrix.setEnabled(false);
			formatPanel.mat.setEnabled(false);
			formatPanel.gml.setEnabled(false);
			formatPanel.comsql.setEnabled(true);
			formatPanel.csv.setEnabled(false);
			formatPanel.comsql.setSelected(true);
			formatPanel.son.setEnabled(true);
			agreementPanel.conflict.setEnabled(true);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
		    cl.show(cardPanel, "commetrixPanel");
		    CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
		    cardNetworkTypePanel.setEnabled(true);
		    coOccurrencePanel.reset();
		    normalizationPanel.normalization.setEnabled(false);
		    normalizationPanel.normalization.setSelected(false);
		    oneModeTypePanel.reset();
		}
		
		public void affilAdjust() {
			formatPanel.csvmatrix.setEnabled(true);
			formatPanel.mat.setEnabled(true);
			formatPanel.gml.setEnabled(true);
			formatPanel.comsql.setEnabled(false);
			formatPanel.csv.setEnabled(true);
			formatPanel.mat.setSelected(true);
			formatPanel.son.setEnabled(true);
			if (agreementPanel.conflict.isSelected()) {
				agreementPanel.yes.setSelected(true);
			}
			agreementPanel.conflict.setEnabled(false);
			CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "twoModeTypePanel");
			cardNetworkTypePanel.setEnabled(true);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
		    cl.show(cardPanel, "coOccurrencePanel");
		    coOccurrencePanel.reset();
		    normalizationPanel.normalization.setEnabled(false);
		    normalizationPanel.normalization.setSelected(false);
		    oneModeTypePanel.reset();
		}
		
		public void tWindAdjust() {
			formatPanel.csvmatrix.setEnabled(true);
			formatPanel.mat.setEnabled(true);
			formatPanel.gml.setEnabled(true);
			formatPanel.comsql.setEnabled(false);
			formatPanel.csv.setEnabled(false);
			formatPanel.mat.setSelected(true);
			formatPanel.son.setEnabled(false);
			CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
			cardNetworkTypePanel.setEnabled(true);
			if (agreementPanel.conflict.isSelected()) {
				agreementPanel.comb.setSelected(true);
			}
			agreementPanel.conflict.setEnabled(false);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
		    cl.show(cardPanel, "timeWindowPanel");
		    coOccurrencePanel.reset();
		    normalizationPanel.normalization.setEnabled(true);
		    oneModeTypePanel.reset();
		}
		
		public void attenAdjust() {
			formatPanel.csvmatrix.setEnabled(true);
			formatPanel.mat.setEnabled(true);
			formatPanel.gml.setEnabled(true);
			formatPanel.comsql.setEnabled(false);
			formatPanel.csv.setEnabled(false);
			formatPanel.mat.setSelected(true);
			formatPanel.son.setEnabled(false);
			CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
			cardNetworkTypePanel.setEnabled(true);
			if (agreementPanel.conflict.isSelected()) {
				agreementPanel.comb.setSelected(true);
			}
			agreementPanel.conflict.setEnabled(false);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
			cl.show(cardPanel, "attenuationPanel");
			coOccurrencePanel.reset();
			normalizationPanel.normalization.setEnabled(true);
			oneModeTypePanel.oneModeCombo.removeItemAt(2);
			oneModeTypePanel.oneModeCombo.setSelectedIndex(1);
			oneModeTypePanel.changeViaCombo();
		}
		
		public void xSecAdjust() {
			formatPanel.csvmatrix.setEnabled(true);
			formatPanel.mat.setEnabled(true);
			formatPanel.gml.setEnabled(true);
			formatPanel.comsql.setEnabled(false);
			formatPanel.csv.setEnabled(false);
			formatPanel.mat.setSelected(true);
			formatPanel.son.setEnabled(false);
			CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
			cardNetworkTypePanel.setEnabled(true);
			agreementPanel.conflict.setEnabled(true);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
			cl.show(cardPanel, "coOccurrencePanel");
			coOccurrencePanel.reset();
			normalizationPanel.normalization.setEnabled(true);
			oneModeTypePanel.reset();
		}
		
		public void reset() {
			xSec.setSelected(true);
			xSecAdjust();
		}
	}
	
	public class AgreementPanel extends JPanel {
		
		JRadioButton yes, no, comb, conflict;
		
		public AgreementPanel() {
			setLayout(new GridLayout(4,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Agreement" ) );

			ActionListener agreementListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//put action here
				}
			};

			ButtonGroup agreeGroup = new ButtonGroup();
			yes = new JRadioButton("yes", false);
			yes.addActionListener(agreementListener);
			yes.setToolTipText("Only establish an edge if both statements are positive.");
			agreeGroup.add(yes);
			add(yes);
			no = new JRadioButton("no", false);
			no.addActionListener(agreementListener);
			no.setToolTipText("Only establish an edge if both statements are negative.");
			agreeGroup.add(no);
			add(no);
			comb = new JRadioButton("combined", true);
			comb.addActionListener(agreementListener);
			comb.setToolTipText("<html>Only establish an edge if both statements <br>" +
					"are positive or both statements are negative.</html>");
			agreeGroup.add(comb);
			add(comb);
			conflict = new JRadioButton("conflict", false);
			conflict.addActionListener(agreementListener);
			conflict.setToolTipText("<html>Only establish an edge if one statement is <br>" +
					"positive and the other one is negative. This <br>" +
					"yields a network of direct contradictions.</html>");
			agreeGroup.add(conflict);
			add(conflict);
		}
		
		public void reset() {
			comb.setSelected(true);
		}
	}
	
	/**
	 * Panel with card layout for network types.
	 */
	public class CardNetworkTypePanel extends JPanel {
		
		public CardNetworkTypePanel() {
			
			setLayout(new CardLayout());
			
			twoModeTypePanel = new TwoModeTypePanel();
			oneModeTypePanel = new OneModeTypePanel();
			
			add(twoModeTypePanel, "twoModeTypePanel");
			add(oneModeTypePanel, "oneModeTypePanel");
		}
		
		public void setEnabled(boolean active) {
			twoModeTypePanel.po.setEnabled(active);
			twoModeTypePanel.pc.setEnabled(active);
			twoModeTypePanel.oc.setEnabled(active);
			oneModeTypePanel.oneModeCombo.setEnabled(active);
			oneModeTypePanel.viaLabel.setEnabled(active);
			oneModeTypePanel.viaCombo.setEnabled(active);
		}
	}
	
	public class TwoModeTypePanel extends JPanel {
		
		JRadioButton po, pc, oc;
		
		public TwoModeTypePanel() {
			
			setLayout(new FlowLayout(FlowLayout.LEFT));
			setBorder( new TitledBorder( new EtchedBorder(), "Network type (bipartite)" ) );
			
			ButtonGroup bipartiteGroup = new ButtonGroup();
			oc = new JRadioButton("org x cat", true);
			oc.setToolTipText("Create a bipartite network of organizations x categories.");
			bipartiteGroup.add(oc);
			add(oc);
			pc = new JRadioButton("pers x cat", false);
			pc.setToolTipText("Create a bipartite network of persons x categories.");
			bipartiteGroup.add(pc);
			add(pc);
			po = new JRadioButton("pers x org", false);
			po.setToolTipText("Create a bipartite network of persons x organizations.");
			bipartiteGroup.add(po);
			add(po);
		}
		
		public void reset() {
			oc.setSelected(true);
		}
	}
	
	public class OneModeTypePanel extends JPanel {
		
		//JRadioButton oo, pp, cc;
		JComboBox oneModeCombo, viaCombo;
		JLabel viaLabel;
		
		public OneModeTypePanel() {
			
			setLayout(new FlowLayout(FlowLayout.LEFT));
			setBorder( new TitledBorder( new EtchedBorder(), "Network type (one-mode)" ) );
			
			ActionListener networkTypeListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource().equals(oneModeCombo)) {
						changeViaCombo();
					}
				}
			};
			
			String[] classes = new String[] {"persons x persons", "organizations x organizations", "categories x categories"};
			oneModeCombo = new JComboBox(classes);
			oneModeCombo.setSelectedIndex(1);
			oneModeCombo.addActionListener(networkTypeListener);
			oneModeCombo.setToolTipText("Select the type of adjacency network to be created.");
			
			viaLabel = new JLabel("via");
			
			viaCombo = new JComboBox();
			String viaToolTipText = "Select what the vertices should have in common.";
			viaLabel.setToolTipText(viaToolTipText);
			viaCombo.setToolTipText(viaToolTipText);
			changeViaCombo();
			
			add(oneModeCombo);
			add(viaLabel);
			add(viaCombo);
		}
		
		public void changeViaCombo() {
			viaCombo.removeAllItems();
			if (oneModeCombo.getSelectedIndex() == 0) {
				viaCombo.addItem("org");
				viaCombo.addItem("cat");
				viaCombo.setSelectedItem("cat");
			} else if (oneModeCombo.getSelectedIndex() == 1) {
				viaCombo.addItem("pers");
				viaCombo.addItem("cat");
				viaCombo.setSelectedItem("cat");
			} else {
				viaCombo.addItem("pers");
				viaCombo.addItem("org");
				viaCombo.setSelectedItem("org");
			}
			
			try {
				if (algorithmPanel.atten.isSelected()) {
					viaCombo.removeAllItems();
					viaCombo.addItem("cat");
				}
			} catch (NullPointerException npe) {}
		}
		
		public void reset() {
			if (oneModeTypePanel.oneModeCombo.getItemCount() < 3) {
				oneModeTypePanel.oneModeCombo.addItem("categories x categories");
			}
			oneModeCombo.setSelectedIndex(1);
			changeViaCombo();
		}
	}
	
	public class DatePanel extends JPanel {
		
		String datePattern, maskPattern;
		JLabel startLabel, stopLabel;
		GregorianCalendar fDate, lDate;
		SpinnerDateModel startModel, stopModel;
		JSpinner startSpinner, stopSpinner;
		ChangeListener dateListener;
		Date startDate, stopDate;
		
		public DatePanel() {
			setLayout(new GridLayout(4,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Time period" ) );
			
			dateListener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					Double chValue = (Double)timeWindowPanel.chain.getValue();
					if (getDuration() < chValue) {
						commetrixPanel.chainModel.setValue(getDuration());
						commetrixPanel.chainModel.setMaximum(getDuration());
						
						timeWindowPanel.chainModel.setValue(getDuration());
						timeWindowPanel.chainModel.setMaximum(getDuration());
						timeWindowPanel.shift.setValue(getDuration());
						timeWindowPanel.shift.setModel(timeWindowPanel.shiftModel);
					} else {
						commetrixPanel.chainModel.setMaximum(getDuration());
						timeWindowPanel.chainModel.setMaximum(getDuration());
					}
					commetrixPanel.chain.setModel(timeWindowPanel.chainModel);
					timeWindowPanel.chain.setModel(timeWindowPanel.chainModel);
				}
			};
			
			startLabel = new JLabel("start:");
			try {
				fDate = sCont.getFirstDate();
			} catch (NullPointerException npe) {
				fDate = new GregorianCalendar(1900, 1, 1);
			}
			startDate = fDate.getTime();
			startSpinner = new JSpinner();
			startSpinner.setToolTipText("The start date of the time period that you want to consider for export.");
			startLabel.setToolTipText("The start date of the time period that you want to consider for export.");
			startModel = new SpinnerDateModel();
			startModel.setCalendarField( Calendar.DAY_OF_YEAR );
			startModel.setValue(startDate);
			startSpinner.setModel( startModel );
			startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "dd.MM.yyyy"));
			startSpinner.addChangeListener(dateListener);
			add(startLabel);
			add(startSpinner);
			
			stopLabel = new JLabel("stop:");
			try {
				lDate = sCont.getLastDate();
			} catch (NullPointerException npe) {
				lDate = new GregorianCalendar(2099, 1, 1);
			}
			stopDate = lDate.getTime();
			stopSpinner = new JSpinner();
			stopSpinner.setToolTipText("The end date of the time period that you want to consider for export.");
			stopLabel.setToolTipText("The end date of the time period that you want to consider for export.");
			stopModel = new SpinnerDateModel();
			stopModel.setCalendarField( Calendar.DAY_OF_YEAR );
			stopModel.setValue(stopDate);
			stopSpinner.setModel( stopModel );
			stopSpinner.setEditor(new JSpinner.DateEditor(stopSpinner, "dd.MM.yyyy"));
			stopSpinner.addChangeListener(dateListener);
			add(stopLabel);
			add(stopSpinner);
		}
		
		private double getDuration() {
			Date date1 = (Date)startSpinner.getValue();
			Date date2 = (Date)stopSpinner.getValue();
			GregorianCalendar d1cal = new GregorianCalendar();
			d1cal.setTime(date1);
			GregorianCalendar d2cal = new GregorianCalendar();
			d2cal.setTime(date2);
			double dur = (d2cal.getTimeInMillis() - d1cal.getTimeInMillis()) / (24*60*60*1000) + 1;
			dur = Math.abs(dur);
			return dur;
		}
		
		public void reset() {
			try {
				fDate = sCont.getFirstDate();
			} catch (NullPointerException npe) {
				fDate = new GregorianCalendar(1900, 1, 1);
			}
			startDate = fDate.getTime();
			startModel.setValue(startDate);
			
			try {
				lDate = sCont.getLastDate();
			} catch (NullPointerException npe) {
				lDate = new GregorianCalendar(2099, 1, 1);
			}
			stopDate = lDate.getTime();
			stopModel.setValue(stopDate);
		}
	}
	
	public class ButtonPanel extends JPanel {
		
		JButton reset, export;
		JCheckBox help;
		String extension, description, outfile;
		
		public ButtonPanel() {
			setLayout(new BorderLayout());
			
			ActionListener buttonListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource().equals(reset)) {
						resetAll();
					} else if (e.getSource().equals(help)) {
						toggleHelp();
					} else if (e.getSource().equals(export)) {
						outfile = getFileName();
						if (outfile.equals("null")) {
							//canceled export
						} else {
							Thread exp = new Thread( new FileExporter(outfile) );
							exp.start();
						}
					}
				}
			};
			
			help = new JCheckBox("display help");
			help.addActionListener(buttonListener);
			help.setToolTipText("<html>If you check this button, context-sensitive help is <br>" +
					"enabled. Tooltips will show up if the mouse is moved <br>" +
					"over an option.</html>");
			
			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			
			Icon resetIcon = new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png"));
			reset = new JButton("Reset", resetIcon);
			reset.addActionListener(buttonListener);
			reset.setToolTipText("Revert all options to their default settings.");
			Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
			export = new JButton("Export...", okIcon);
			export.setToolTipText("Choose a file name and export with the selected options.");
			export.addActionListener(buttonListener);
			export.setEnabled(true);
			buttons.add(reset);
			buttons.add(export);
			
			add(help, BorderLayout.WEST);
			add(buttons, BorderLayout.EAST);
			
			toggleHelp();
		}
		
		public void toggleHelp() {
			javax.swing.ToolTipManager.sharedInstance().setInitialDelay(10);
			if (help.isSelected() == true) {
				javax.swing.ToolTipManager.sharedInstance().setEnabled(true);
				javax.swing.ToolTipManager.sharedInstance().setDismissDelay(30000);
			} else {
				javax.swing.ToolTipManager.sharedInstance().setEnabled(false);
				javax.swing.ToolTipManager.sharedInstance().setDismissDelay(0);
			}
		}
		
		/**
		 * File chooser.
		 * 
		 * @return string of the file name
		 */
		public String getFileName() {
			if (formatPanel.csv.isSelected() || formatPanel.csvmatrix.isSelected()) {
				extension = ".csv";
				description = "Comma-separated values (*.csv)";
			} else if (formatPanel.mat.isSelected()) {
				extension = ".dl";
				description = "UCINET DL file (*.dl)";
			} else if (formatPanel.gml.isSelected()) {
				extension = ".graphml";
				description = "GraphML file (*.graphml)";
			} else if (formatPanel.comsql.isSelected()) {
				extension = ".sql";
				description = "Commetrix SQL file (*.sql)";
			} else if (formatPanel.son.isSelected()) {
				extension = ".son";
				description = "SoNIA file (*.son)";
			}
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					return f.getName().toLowerCase().endsWith(extension) 
					|| f.isDirectory();
				}
				public String getDescription() {
					return description;
				}
			});

			int returnVal = fc.showSaveDialog(ExportWindow.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				String filename = new String(file.getPath());
				if ( !file.getPath().endsWith(extension) ) {
					filename = filename + extension;
				}
				return filename;
			} else {
				System.out.println("Export cancelled.");
				return "null";
			}
		}
	}
	
	private class FileExporter implements Runnable {
		
		String outfile;
		JFrame progressFrame;
		
		public FileExporter(String outfile) {
			this.outfile = outfile;
		}
		
		public void run() {
			buttonPanel.export.setEnabled(false);
			buttonPanel.reset.setEnabled(false);
			showProgress();
			
			Object[] excludePers = excludePanel.person.getSelectedValues();
			Object[] excludeOrg = excludePanel.organization.getSelectedValues();
			Object[] excludeCat = excludePanel.category.getSelectedValues();
			String[] excludeP = new String[excludePers.length];
			for (int i = 0; i < excludePers.length; i++) {
				excludeP[i] = (String) excludePers[i];
			}
			String[] excludeO = new String[excludeOrg.length];
			for (int i = 0; i < excludeOrg.length; i++) {
				excludeO[i] = (String) excludeOrg[i];
			}
			String[] excludeC = new String[excludeCat.length];
			for (int i = 0; i < excludeCat.length; i++) {
				excludeC[i] = (String) excludeCat[i];
			}
			
			GregorianCalendar start = new GregorianCalendar();
			start.setTime((Date)datePanel.startSpinner.getValue());
			GregorianCalendar stop = new GregorianCalendar();
			stop.setTime((Date)datePanel.stopSpinner.getValue());
			
			String agreement = "";
			if (agreementPanel.yes.isSelected()) {
				agreement = "yes";
			} else if (agreementPanel.no.isSelected()) {
				agreement = "no";
			} else if (agreementPanel.comb.isSelected()) {
				agreement = "combined";
			} else if (agreementPanel.conflict.isSelected()) {
				agreement = "conflict";
			}
			
			String algorithm = "";
			if (algorithmPanel.affil.isSelected()) {
				algorithm = "affiliation";
			} else if (algorithmPanel.xSec.isSelected()) {
				algorithm = "cooccurrence";
			} else if (algorithmPanel.tWind.isSelected()) {
				algorithm = "timewindow";
			} else if (algorithmPanel.atten.isSelected()) {
				algorithm = "attenuation";
			} else if (algorithmPanel.sonia.isSelected()) {
				algorithm = "dynamic";
			} else if (algorithmPanel.el.isSelected()) {
				algorithm = "edgelist";
			}
			
			String format = "";
			if (formatPanel.csvmatrix.isSelected()) {
				format = "csvmatrix";
			} else if (formatPanel.csv.isSelected()) {
				format = "csvlist";
			} else if (formatPanel.mat.isSelected()) {
				format = "dl";
			} else if (formatPanel.son.isSelected()) {
				format = "sonia";
			} else if (formatPanel.comsql.isSelected()) {
				format = "commtrix";
			} else if (formatPanel.gml.isSelected()) {
				format = "graphml";
			}
			
			String twoModeType = "";
			if (twoModeTypePanel.oc.isSelected()) {
				twoModeType = "oc";
			} else if (twoModeTypePanel.pc.isSelected()) {
				twoModeType = "pc";
			} else if (twoModeTypePanel.po.isSelected()) {
				twoModeType = "po";
			}
			
			String oneModeType = "";
			if ( ((String)oneModeTypePanel.oneModeCombo.getSelectedItem()).equals("organizations x organizations") ) {
				oneModeType = "organizations";
			} else if ( ((String)oneModeTypePanel.oneModeCombo.getSelectedItem()).equals("persons x persons") ) {
				oneModeType = "persons";
			} else if ( ((String)oneModeTypePanel.oneModeCombo.getSelectedItem()).equals("categories x categories") ) {
				oneModeType = "categories";
			}
			
			String via = "";
			if ( ((String)oneModeTypePanel.viaCombo.getSelectedItem()).equals("org") ) {
				via = "organizations";
			} else if ( ((String)oneModeTypePanel.viaCombo.getSelectedItem()).equals("cat") ) {
				via = "categories";
			} else if ( ((String)oneModeTypePanel.viaCombo.getSelectedItem()).equals("pers") ) {
				via = "persons";
			}
			
			boolean includeIsolates;
			if (coOccurrencePanel.includeIsolates.isSelected()) {
				includeIsolates = true;
			} else {
				includeIsolates = false;
			}
			
			boolean ignoreDuplicates;
			if (coOccurrencePanel.ignoreDuplicates.isSelected()) {
				ignoreDuplicates = true;
			} else {
				ignoreDuplicates = false;
			}
			
			boolean normalization;
			if (normalizationPanel.normalization.isSelected()) {
				normalization = true;
			} else {
				normalization = false;
			}
			
			Double windowSizeDouble = (Double)timeWindowPanel.chain.getValue();
			Double stepSizeDouble = (Double)timeWindowPanel.shift.getValue();
			
			Double alpha = (Double)timeWindowPanel.alphaSpinner.getValue();
			Double lambda = (Double)attenuationPanel.lambdaSpinner.getValue();
			
			Double forwardDays = (Double) soniaPanel.forwardWindow.getValue();
			Double backwardDays = (Double) soniaPanel.backwardModel.getValue();
			
			String networkName = commetrixPanel.networkName.getText();
			Double commetrixBackwardWindow = (Double)commetrixPanel.chain.getValue();
			
			new Export(
					sCont, 
					outfile, 
					excludeP, 
					excludeO, 
					excludeC, 
					start, 
					stop, 
					agreement, 
					algorithm, 
					format, 
					twoModeType, 
					oneModeType, 
					via, 
					includeIsolates, 
					ignoreDuplicates, 
					normalization,
					windowSizeDouble, //time window algorithm
					stepSizeDouble, //time window algorithm
					alpha,
					lambda,
					forwardDays, //SONIA
					backwardDays, //SONIA
					networkName, //Commetrix
					commetrixBackwardWindow //Commetrix
					);
			
			progressFrame.dispose();
			buttonPanel.export.setEnabled(true);
			buttonPanel.reset.setEnabled(true);
		}

		public void showProgress() {
			progressFrame = new JFrame();
			Container progressContent = progressFrame.getContentPane();
			progressFrame.setTitle("Calculating...");
			ImageIcon progressIcon = new ImageIcon(getClass().getResource("/icons/dna16.png"));
			progressFrame.setIconImage(progressIcon.getImage());
			progressFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			JProgressBar progress = new JProgressBar();
			progress.setPreferredSize(new Dimension(200, 30));
			progress.setString("Calculating");
			progress.setStringPainted(true);
			progress.setIndeterminate(true);
			progressContent.add(progress);
			progressFrame.pack();
			progressFrame.setLocationRelativeTo(null);
			progressFrame.setVisible(true);
			progressFrame.setAlwaysOnTop(true);
		}
	}
}