package dna;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

/**
 * This is the export component of the Discourse Analyzer (DNA). You 
 * can use it to convert a .dna file into a network file. Bipartite 
 * graphs, co-occurrence networks as well as some advanced algorithms 
 * are offered. Network data can be exported as DL files (Ucinet), 
 * graphML (visone) or CSV files (spreadsheet).
 * 
 * @author Philip Leifeld
 * @version 1.08 - 2 August 2009
 */
public class DnaExportWindow extends JFrame {
	
	Container c;
	DnaStatementCollection statementCollection;
	
	CardNetworkTypePanel cardNetworkTypePanel;
	TwoModeTypePanel twoModeTypePanel;
	OneModeTypePanel oneModeTypePanel;
	FormatPanel formatPanel;
	AlgorithmPanel algorithmPanel;
	DatePanel datePanel;
	AgreementPanel agreementPanel;
	ButtonPanel buttonPanel;
	FilePanel filePanel;
	TimeWindowPanel timeWindowPanel;
	CommetrixPanel commetrixPanel;
	NormalizationPanel normalizationPanel;
	EmptyPanel emptyPanel;
	CardPanel cardPanel;
	ExcludePanel excludePanel;
	
	/**
	 * This constructor is called from the main method and does not open any dna file by default.
	 */
	public DnaExportWindow( ) {
		exportWindow();
	}
	
	/**
	 * This constructor is called from another method and opens a file immediately.
	 * 
	 * @param infile
	 */
	public DnaExportWindow(String infile) {
		exportWindow();
		openFile(infile);
		resetAll();
	}
	
	public void exportWindow() {
		this.setTitle("DNA Export");
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
		this.setIconImage(dna32Icon.getImage());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		appearance();
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void openFile(String fileName) {
		try {
			DnaParser p = new DnaParser(fileName);
			statementCollection = p.getDnaStatementCollection();
			System.out.println("File was parsed: " + fileName);
			filePanel.currentFileName = fileName;
			
			//if the file name gets too long, shorten it for the label
			if (filePanel.currentFileName.length() > 58) {
				int startPos = filePanel.currentFileName.length() - 58;
				int endPos = filePanel.currentFileName.length();
				filePanel.currentFileLabel.setText("..." + filePanel.currentFileName.substring(startPos, endPos));
			} else {
				filePanel.currentFileLabel.setText( filePanel.currentFileName );
			}
			
			buttonPanel.export.setEnabled(true);
		} catch (IOException e) {
			buttonPanel.export.setEnabled(false);
			filePanel.currentFileLabel.setText( "Current file: none" );
			statementCollection.clearAll();
	        System.out.println("Error while reading the file \"" + fileName + "\".");
	        JOptionPane.showMessageDialog(DnaExportWindow.this, "Error while reading the file \n\"" + fileName + "\".");
	    } catch (org.jdom.JDOMException e) {
	    	buttonPanel.export.setEnabled(false);
	    	filePanel.currentFileLabel.setText( "Current file: none" );
	    	statementCollection.clearAll();
	        System.out.println("Validation error while opening DNA file \"" + fileName + "\": " + e.getMessage());
	        JOptionPane.showMessageDialog(DnaExportWindow.this, "Validation error while opening DNA file \n\"" + fileName + "\": \n" + e.getMessage());
	    } catch (Exception ex) {
	    	buttonPanel.export.setEnabled(false);
	    	filePanel.currentFileLabel.setText( "Current file: none" );
	    	statementCollection.clearAll();
	    	System.out.println("An unknown error occurred when opening the file " + fileName + ".");
	    	JOptionPane.showMessageDialog(DnaExportWindow.this, "An unknown error occurred when opening the file \n" + fileName + ".");
	    }
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
		timeWindowPanel.reset();
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
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		filePanel = new FilePanel();
		basicOptionsPanel.add(filePanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		cardNetworkTypePanel = new CardNetworkTypePanel();
		CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
	    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
		basicOptionsPanel.add(cardNetworkTypePanel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 1;
		algorithmPanel = new AlgorithmPanel();
		basicOptionsPanel.add(algorithmPanel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		formatPanel = new FormatPanel();
		basicOptionsPanel.add(formatPanel, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridheight = 2;
		JPanel rightPanel = new JPanel(new BorderLayout());
		datePanel = new DatePanel();
		agreementPanel = new AgreementPanel();
		rightPanel.add(datePanel, BorderLayout.NORTH);
		rightPanel.add(agreementPanel, BorderLayout.SOUTH);
		basicOptionsPanel.add(rightPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		cardPanel = new CardPanel();
		CardLayout cl = (CardLayout)(cardPanel.getLayout());
	    cl.show(cardPanel, "emptyPanel");
		basicOptionsPanel.add(cardPanel, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		normalizationPanel = new NormalizationPanel();
		basicOptionsPanel.add(normalizationPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 3;
		excludePanel = new ExcludePanel();
		basicOptionsPanel.add(excludePanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 5;
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
			
			JPanel personPanel = new JPanel(new BorderLayout());
			personModel = new DefaultListModel();
			person = new JList(personModel);
			person.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			person.setLayoutOrientation(JList.VERTICAL);
			person.setVisibleRowCount(5);
			person.setFixedCellWidth(30);
			personScroller = new JScrollPane(person);
			JLabel personLabel = new JLabel("exclude selected persons");
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
			JLabel organizationLabel = new JLabel("exclude selected organizations");
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
			JLabel categoryLabel = new JLabel("exclude selected categories");
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
				ArrayList<String> persons = statementCollection.getPersonList();
				ArrayList<String> organizations = statementCollection.getOrganizationList();
				ArrayList<String> categories = statementCollection.getCategoryList();
				
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
			emptyPanel = new EmptyPanel();
			
			add(timeWindowPanel, "timeWindowPanel");
			add(commetrixPanel, "commetrixPanel");
			add(emptyPanel, "emptyPanel");
		}
		
	}
	
	/**
	 * Custom option panel without any options.
	 */
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
	 * Custom option panel for the time window algorithm.
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
	 * Custom option panel for the time window algorithm.
	 */
	public class TimeWindowPanel extends JPanel {
		
		JSpinner chain, shift;
		SpinnerNumberModel chainModel, shiftModel;
		
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
			
			JLabel days1 = new JLabel(" days,");
			chainModel = new SpinnerNumberModel(getChDur(), 1, datePanel.getDuration(), 1);
			chain = new JSpinner(chainModel);
			chain.addChangeListener(cl);
			JLabel chainLabel = new JLabel("moving time window of");
			
			String chainToolTipText = "<html>When using the time window algorithm, there are two parameters. <br>" +
					"This parameter is called the chaining parameter. It specifies the <br>" +
					"size of a window that moves through the whole discourse. At each <br>" +
					"time step, only those edges will be established or considered that <br>" +
					"are within the time window. A useful parameter value is 20 days <br>" +
					"because this often reflects the time until the debate changes.</html>";
			
			chain.setToolTipText(chainToolTipText);
			chainLabel.setToolTipText(chainToolTipText);
			days1.setToolTipText(chainToolTipText);
			
			top.add(chainLabel);
			top.add(chain);
			top.add(days1);
			add(top, BorderLayout.NORTH);
			
			JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			shiftModel = new SpinnerNumberModel(1, 1, datePanel.getDuration(), 1);
			shift = new JSpinner(shiftModel);
			JLabel shiftLabel = new JLabel("which is shifted by");
			JLabel days2 = new JLabel(" days");

			String shiftToolTipText = "<html>The second parameter of the time window algorithm is the shift <br>" +
			"parameter. It specifies by how many days the time window should <br>" +
			"move after every round. If you set a value of 1 day, the results <br>" +
			"will be precise, but calculations are slow. If you prefer non- <br>" +
			"overlapping time windows (i.e. discrete time units), you should <br>" +
			"set the shift parameter to the same value as the chaining para- <br>" +
			"meter, which is the maximum possible value.</html>";
			
			shift.setToolTipText(shiftToolTipText);
			shiftLabel.setToolTipText(shiftToolTipText);
			days2.setToolTipText(shiftToolTipText);
			
			bottom.add(shiftLabel);
			bottom.add(shift);
			bottom.add(days2);
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
		}
	}
	
	/**
	 * Panel which shows the name of the current file and a file chooser button.
	 */
	public class FilePanel extends JPanel {
		
		JButton openButton;
		JLabel currentFileLabel;
		String currentFileName;
		JToolBar toolbar;
		
		public FilePanel() {
			
			setLayout(new FlowLayout(FlowLayout.LEFT));
			
			Icon openIcon = new ImageIcon(getClass().getResource(
			"/icons/Open16.gif"));
			JButton openButton = new JButton(openIcon);
			openButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			openButton.setContentAreaFilled(false);
			openButton.setRolloverEnabled(true);
			openButton.setSize(new Dimension(16,16));
			openButton.setToolTipText( "open DNA file..." );
			
			add(openButton);
			
			currentFileLabel = new JLabel("Current file: none");
			add(currentFileLabel);
			
			currentFileName = new String("");
			
			String openToolTipText = "<html>Click on this button to close the current DNA <br>" +
					"file and open another file for export instead.</html>";
			
			String labelToolTipText = "<html>This is the DNA project file from which <br>" +
					"network data will be retrieved and exported.</html>";
			
			openButton.setToolTipText(openToolTipText);
			currentFileLabel.setToolTipText(labelToolTipText);
			
			openButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					//File filter
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".dna") 
							|| f.isDirectory();
						}
						public String getDescription() {
							return "Discourse Network files (*.dna)";
						}
					});

					int returnVal = fc.showOpenDialog(DnaExportWindow.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						//openDnaFile(file.getPath());
						//put the method here which will parse the DNA file!
						currentFileName = file.getPath();
						
					}
					
					openFile(currentFileName);
					resetAll();
				}
			});
		}
	}
	
	/**
	 * Create a panel where the network format (CSV, DL, graphML) can be selected.
	 */
	public class FormatPanel extends JPanel {
		
		ButtonGroup formatGroup;
		JRadioButton csv, csvmatrix, mat, el, gml, comsql;
		
		public FormatPanel() {
			setLayout(new GridLayout(6,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Export format" ) );
			
			ActionListener formatButtonListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//put action here
				}
			};
			
			formatGroup = new ButtonGroup();
			csv = new JRadioButton("CSV list", false);
			csv.addActionListener(formatButtonListener);
			csv.setToolTipText("<html>A list of statements (including date, person, organization <br>" +
					"and agreement) in a comma-separated text file for use <br>" +
					"in spreadsheet/office or statistics software packages. <br>" +
					"This is <i>not</i> a network format!</html>");
			formatGroup.add(csv);
			add(csv);
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
			el = new JRadioButton("DL edgelist", false);
			el.addActionListener(formatButtonListener);
			el.setToolTipText("<html>The DL edgelist format can be imported by UCINET but <br>" +
					"not by visone. This DL specification stores the <br>" +
					"network as a list of edges rather than a matrix.</html>");
			formatGroup.add(el);
			add(el);
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
			comsql.setVisible(true); //hier ggf. wieder ausschalten
		}
		
		public void reset() {
			mat.setSelected(true);
		}
	}
	
	public class AlgorithmPanel extends JPanel {
		
		JRadioButton sl, el, affil, xSec, tWind, atten;
		
		public AlgorithmPanel() {
			setLayout(new GridLayout(6,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Algorithm" ) );

			ActionListener algoListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource().equals(sl)) {
						slAdjust();
					} else if (e.getSource().equals(el)) {
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
			el = new JRadioButton("Edgelist with time stamp", false);
			el.addActionListener(algoListener);
			el.setToolTipText("<html>This produces a list of edges, where each edge is affiliated with <br>" +
					"the date of the vertex with the statement that occurred later <br>" +
					"in the discourse, i.e. if actor A makes a statement on the 12th <br>" +
					"of June and Actor B makes a statement on the 24th of June, a <br>" +
					"binary edge between A and B will be recorded, and the edge will <br>" +
					"have the 24th of July as the date of its creation. This algorithm <br>" +
					"is useful for dynamic, continuous-time visualization of network <br>" +
					"evolution as featured in Commetrix.</html>");
			algoGroup.add(el);
			add(el);
			sl = new JRadioButton("Statement list", false);
			sl.addActionListener(algoListener);
			sl.setToolTipText("This algorithm will produce a plain list of statements, i.e. no network data!");
			algoGroup.add(sl);
			add(sl);
		}
		
		public void slAdjust() {
			formatPanel.csvmatrix.setEnabled(false);
			formatPanel.mat.setEnabled(false);
			formatPanel.gml.setEnabled(false);
			formatPanel.comsql.setEnabled(false);
			formatPanel.el.setEnabled(false);
			formatPanel.csv.setEnabled(true);
			formatPanel.csv.setSelected(true);
			agreementPanel.conflict.setEnabled(false);
			if (agreementPanel.conflict.isSelected()) {
				agreementPanel.comb.setSelected(true);
			}
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
		    cl.show(cardPanel, "emptyPanel");
		    CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
		    cardNetworkTypePanel.setEnabled(false);
		    normalizationPanel.normalization.setEnabled(false);
		    normalizationPanel.normalization.setSelected(false);
		    oneModeTypePanel.reset();
		}
		
		public void elAdjust() {
			formatPanel.csvmatrix.setEnabled(false);
			formatPanel.mat.setEnabled(false);
			formatPanel.gml.setEnabled(false);
			formatPanel.comsql.setEnabled(true);
			formatPanel.el.setEnabled(false);
			formatPanel.csv.setEnabled(false);
			formatPanel.comsql.setSelected(true);
			agreementPanel.conflict.setEnabled(true);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
		    cl.show(cardPanel, "commetrixPanel");
		    CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
		    cardNetworkTypePanel.setEnabled(true);
		    normalizationPanel.normalization.setEnabled(false);
		    normalizationPanel.normalization.setSelected(false);
		    oneModeTypePanel.reset();
		}
		
		public void affilAdjust() {
			formatPanel.csvmatrix.setEnabled(true);
			formatPanel.mat.setEnabled(true);
			formatPanel.gml.setEnabled(true);
			formatPanel.comsql.setEnabled(false);
			formatPanel.el.setEnabled(true);
			formatPanel.csv.setEnabled(false);
			formatPanel.mat.setSelected(true);
			if (agreementPanel.conflict.isSelected()) {
				agreementPanel.yes.setSelected(true);
			}
			agreementPanel.conflict.setEnabled(false);
			CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "twoModeTypePanel");
			cardNetworkTypePanel.setEnabled(true);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
		    cl.show(cardPanel, "emptyPanel");
		    normalizationPanel.normalization.setEnabled(false);
		    normalizationPanel.normalization.setSelected(false);
		    oneModeTypePanel.reset();
		}
		
		public void tWindAdjust() {
			formatPanel.csvmatrix.setEnabled(true);
			formatPanel.mat.setEnabled(true);
			formatPanel.gml.setEnabled(true);
			formatPanel.comsql.setEnabled(false);
			formatPanel.el.setEnabled(true);
			formatPanel.csv.setEnabled(false);
			formatPanel.mat.setSelected(true);
			CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
			cardNetworkTypePanel.setEnabled(true);
			if (agreementPanel.conflict.isSelected()) {
				agreementPanel.comb.setSelected(true);
			}
			agreementPanel.conflict.setEnabled(false);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
		    cl.show(cardPanel, "timeWindowPanel");
		    normalizationPanel.normalization.setEnabled(true);
		    oneModeTypePanel.reset();
		}
		
		public void attenAdjust() {
			formatPanel.csvmatrix.setEnabled(true);
			formatPanel.mat.setEnabled(true);
			formatPanel.gml.setEnabled(true);
			formatPanel.comsql.setEnabled(false);
			formatPanel.el.setEnabled(true);
			formatPanel.csv.setEnabled(false);
			formatPanel.mat.setSelected(true);
			CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
			cardNetworkTypePanel.setEnabled(true);
			if (agreementPanel.conflict.isSelected()) {
				agreementPanel.comb.setSelected(true);
			}
			agreementPanel.conflict.setEnabled(false);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
			cl.show(cardPanel, "emptyPanel");
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
			formatPanel.el.setEnabled(true);
			formatPanel.csv.setEnabled(false);
			formatPanel.mat.setSelected(true);
			CardLayout cntl = (CardLayout)(cardNetworkTypePanel.getLayout());
		    cntl.show(cardNetworkTypePanel, "oneModeTypePanel");
			cardNetworkTypePanel.setEnabled(true);
			agreementPanel.conflict.setEnabled(true);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
			cl.show(cardPanel, "emptyPanel");
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
			
			ActionListener networkTypeListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//put action here
				}
			};
			
			ButtonGroup bipartiteGroup = new ButtonGroup();
			oc = new JRadioButton("org x cat", true);
			oc.addActionListener(networkTypeListener);
			oc.setToolTipText("Create a bipartite network of organizations x categories.");
			bipartiteGroup.add(oc);
			add(oc);
			pc = new JRadioButton("pers x cat", false);
			pc.addActionListener(networkTypeListener);
			pc.setToolTipText("Create a bipartite network of persons x categories.");
			bipartiteGroup.add(pc);
			add(pc);
			po = new JRadioButton("pers x org", false);
			po.addActionListener(networkTypeListener);
			po.setToolTipText("Create a bipartite network of persons x organizations.");
			bipartiteGroup.add(po);
			add(po);
		}
		
		public void reset() {
			oc.setSelected(true);
		}
	}
	
	public class OneModeTypePanel extends JPanel {
		
		JRadioButton oo, pp, cc;
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
					//if (e.getSource().equals(startSpinner)) { }
				}
			};
			
			startLabel = new JLabel("start:");
			try {
				fDate = statementCollection.getFirstDate();
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
				lDate = statementCollection.getLastDate();
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
				fDate = statementCollection.getFirstDate();
			} catch (NullPointerException npe) {
				fDate = new GregorianCalendar(1900, 1, 1);
			}
			startDate = fDate.getTime();
			startModel.setValue(startDate);
			
			try {
				lDate = statementCollection.getLastDate();
			} catch (NullPointerException npe) {
				lDate = new GregorianCalendar(2099, 1, 1);
			}
			stopDate = lDate.getTime();
			stopModel.setValue(stopDate);
		}
	}
	
	public class ButtonPanel extends JPanel {
		
		JProgressBar progress;
		JButton reset, export;
		JCheckBox help;
		
		public ButtonPanel() {
			setLayout(new BorderLayout());
			
			ActionListener buttonListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource().equals(reset)) {
						resetAll();
					} else if (e.getSource().equals(help)) {
						toggleHelp();
					} else if (e.getSource().equals(export)) {
						fileExporter exp = new fileExporter();
						exp.start();
					}
				}
			};
			
			help = new JCheckBox("display help");
			help.addActionListener(buttonListener);
			help.setToolTipText("<html>If you check this button, context-sensitive help is <br>" +
					"enabled. Tooltips will show up if the mouse is moved <br>" +
					"over an option.</html>");
			
			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			
			progress = new JProgressBar(0, 100);
			progress.setToolTipText("If this progress bar is active, the export algorithm is still working. Please do not interrupt.");
			progress.setIndeterminate(true);
			progress.setString("Calculating...");
			progress.setStringPainted(true);
			progress.setVisible(false);
			
			reset = new JButton("Reset");
			reset.addActionListener(buttonListener);
			reset.setToolTipText("Revert all options to their default settings.");
			export = new JButton("Export...");
			export.setToolTipText("Choose a file name and export with the selected options.");
			export.addActionListener(buttonListener);
			export.setEnabled(false);
			buttons.add(progress);
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
	}
	
	class fileExporter extends Thread {
		
		String extension, description, outfile;
		DnaStatementCollection sc;
		ArrayList<String> class1, class2, class3, s_agree, s_text;
		ArrayList<GregorianCalendar> s_date;
		DnaGraph graph;
		GregorianCalendar start, stop;
		
		//declarations for normalization procedures
		int sourceCounter;
		int targetCounter;
		String sourceLabel;
		String targetLabel;
		ArrayList<String> tabuSource = new ArrayList<String>();
		ArrayList<String> tabuTarget = new ArrayList<String>();
		
		public void run() {
			
			//run file chooser and create export data
			outfile = getFileName();
			if (outfile.equals("null")) {
				//cancelled export
			} else {
				buttonPanel.export.setEnabled(false);
				buttonPanel.reset.setEnabled(false);
				buttonPanel.progress.setVisible(true);
				
				sc = statementCollection.clone();
				
				//apply date filter
				start = new GregorianCalendar();
				start.setTime((Date)datePanel.startSpinner.getValue());
				stop = new GregorianCalendar();
				stop.setTime((Date)datePanel.stopSpinner.getValue());
				sc.timeFilter(start, stop);
				
				//apply agreement filter
				if (agreementPanel.yes.isSelected()) {
					sc.agreementFilter("yes");
				} else if (agreementPanel.no.isSelected()) {
					sc.agreementFilter("no");
				}
				
				//apply exclude list
				sc.excludeFilter(excludePanel.person.getSelectedValues(), excludePanel.organization.getSelectedValues(), excludePanel.category.getSelectedValues());
				
				//reformat statement collection
				if (formatPanel.el.isSelected() || formatPanel.mat.isSelected()) {
					sc.reformatDl();
				} else {
					sc.reformat();
				}
				
				createGraph();
				exportControl();
			}
			
			buttonPanel.export.setEnabled(true);
			buttonPanel.reset.setEnabled(true);
			buttonPanel.progress.setVisible(false);
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
			} else if (formatPanel.el.isSelected() || formatPanel.mat.isSelected()) {
				extension = ".dl";
				description = "UCINET DL file (*.dl)";
			} else if (formatPanel.gml.isSelected()) {
				extension = ".graphML";
				description = "GraphML file (*.graphML)";
			} else if (formatPanel.comsql.isSelected()) {
				extension = ".sql";
				description = "Commetrix SQL file (*.sql)";
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

			int returnVal = fc.showSaveDialog(DnaExportWindow.this);
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
		
		public void createGraph() {
			if (algorithmPanel.sl.isSelected()) {
				csvListFilter(outfile);
			} else if (algorithmPanel.affil.isSelected()) {
				generateGraphAffiliation();
			} else if (algorithmPanel.el.isSelected()) {
				exportFilterCommetrix(outfile);
			} else if (algorithmPanel.xSec.isSelected()) {
				generateGraphCoOccurrence();
			} else if (algorithmPanel.tWind.isSelected()) {
				generateGraphTimeWindow();
			} else if (algorithmPanel.atten.isSelected()) {
				generateGraphAttenuation();
			}
		}
		
		public void exportControl() {
			if (formatPanel.csvmatrix.isSelected()) {
				exportCsvMatrix(outfile);
			} else if (formatPanel.mat.isSelected()) {
				exportDlFullMatrix(outfile);
			} else if (formatPanel.gml.isSelected()) {
				exportGraphMl(outfile);
			} else if (formatPanel.el.isSelected()) {
				exportDlEdgeList(outfile);
			}
		}
		
		/**
		 * CSV statement list export filter.
		 * 
		 * @param name of the output file
		 */
		public void csvListFilter ( String outfile ) {
			try {
				BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
				br.write("date;person;organization;category;agreement;text");
				for (int i = 0; i < sc.size(); i++) {
					String agree = "";
					if (sc.get(i).getAgreement() == true) {
						agree = "yes";
					} else if (sc.get(i).getAgreement() == false) {
						agree = "no";
					}
					br.newLine();
					br.write(sc.get(i).getDate().get(Calendar.YEAR) + "-" 
							+ (sc.get(i).getDate().get(Calendar.MONTH)+1) + "-" 
							+ sc.get(i).getDate().get(Calendar.DATE) + ";"
							+ sc.get(i).getPerson() + ";" 
							+ sc.get(i).getOrganization() + ";"
							+ sc.get(i).getCategory() + ";"
							+ agree + ";"
							+ sc.get(i).getContent());
				}
				br.close();
				System.out.println("File has been exported to \"" + outfile + "\".");
			} catch (IOException e) {
				System.out.println("Error while saving CSV statement list.");
			}
		}
		
		/**
		 * Generate bipartite graph from lists of statements.
		 */
		private void generateGraphAffiliation() {
			
			//create graph
			graph = new DnaGraph();
			graph.removeAllEdges();
			graph.removeAllVertices();
			
			//create vertices
			int count = 0;
			class1 = new ArrayList<String>();
			class2 = new ArrayList<String>();
			if (twoModeTypePanel.oc.isSelected()) {
				class1 = sc.getStringList("o");
				for (int i = 0; i < sc.getOrganizationList().size(); i++) {
					if (!sc.getOrganizationList().get(i).equals("")) {
						graph.addVertex(new DnaGraphVertex(count, sc.getOrganizationList().get(i), "o"));
						count++;
					}
				}
				class2 = sc.getStringList("c");
				for (int i = 0; i < sc.getCategoryList().size(); i++) {
					if (!sc.getCategoryList().get(i).equals("")) {
						graph.addVertex(new DnaGraphVertex(count, sc.getCategoryList().get(i), "c"));
						count++;
					}
				}
			} else if (twoModeTypePanel.po.isSelected()) {
				class1 = sc.getStringList("p");
				for (int i = 0; i < sc.getPersonList().size(); i++) {
					if (!sc.getPersonList().get(i).equals("")) {
						graph.addVertex(new DnaGraphVertex(count, sc.getPersonList().get(i), "p"));
						count++;
					}
				}
				class2 = sc.getStringList("o");
				for (int i = 0; i < sc.getOrganizationList().size(); i++) {
					if (!sc.getOrganizationList().get(i).equals("")) {
						graph.addVertex(new DnaGraphVertex(count, sc.getOrganizationList().get(i), "o"));
						count++;
					}
				}
			} else if (twoModeTypePanel.pc.isSelected()) {
				class1 = sc.getStringList("p");
				for (int i = 0; i < sc.getPersonList().size(); i++) {
					if (!sc.getPersonList().get(i).equals("")) {
						graph.addVertex(new DnaGraphVertex(count, sc.getPersonList().get(i), "p"));
						count++;
					}
				}
				class2 = sc.getStringList("c");
				for (int i = 0; i < sc.getCategoryList().size(); i++) {
					if (!sc.getCategoryList().get(i).equals("")) {
						graph.addVertex(new DnaGraphVertex(count, sc.getCategoryList().get(i), "c"));
						count++;
					}
				}
			}

			//add bipartite edges
			count = 0;
			for (int i = 0; i < class1.size(); i++) {
				if (!class1.get(i).equals("") && !class2.get(i).equals("")) {
					int sourceId = graph.getVertex(class1.get(i)).getId();
					int targetId = graph.getVertex(class2.get(i)).getId();
					if (!graph.containsEdge(sourceId, targetId)) {
						graph.addEdge(new DnaGraphEdge(count, 1, graph.getVertex(sourceId), graph.getVertex(targetId)));
						count++;
					}
				}
			}
			
			if (twoModeTypePanel.oc.isSelected()) {
				System.out.println("A bipartite graph with " + graph.countVertexType("o") + " organization vertices, " + graph.countVertexType("c") + " category vertices and " + graph.countEdges() + " edges with a mean edge weight of " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + " was created.");
			} else if (twoModeTypePanel.pc.isSelected()) {
				System.out.println("A bipartite graph with " + graph.countVertexType("p") + " person vertices, " + graph.countVertexType("c") + " category vertices and " + graph.countEdges() + " edges with a mean edge weight of " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + " was created.");
			} else if (twoModeTypePanel.po.isSelected()) {
				System.out.println("A bipartite graph with " + graph.countVertexType("p") + " person vertices, " + graph.countVertexType("o") + " organization vertices and " + graph.countEdges() + " edges with a mean edge weight of " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + " was created.");
			}
			
		}
		
		public class tabuItem {
			
			int sourceId, targetId;
			String via;
			
			public tabuItem(int sourceId, int targetId, String via) {
				this.sourceId = sourceId;
				this.targetId = targetId;
				this.via = via;
			}

			public int getSourceId() {
				return sourceId;
			}

			public void setSourceId(int sourceId) {
				this.sourceId = sourceId;
			}

			public int getTargetId() {
				return targetId;
			}

			public void setTargetId(int targetId) {
				this.targetId = targetId;
			}

			public String getVia() {
				return via;
			}

			public void setVia(String via) {
				this.via = via;
			}
		}
		
		public class tabuList {
			
			ArrayList<tabuItem> l;
			
			public tabuList() {
				l = new ArrayList<tabuItem>();
			}
			
			public boolean contains(int sourceId, int targetId, String via) {
				boolean condition = false;
				for (int i = 0; i < l.size(); i++) {
					if (l.get(i).getSourceId() == sourceId && l.get(i).getTargetId() == targetId && l.get(i).getVia().equals(via)) {
						condition = true;
					}
				}
				return condition;
			}
			
			public void add(tabuItem t) {
				if (!l.contains(t)) {
					l.add(t);
				}
			}
		}
		
		public void prepareSimpleGraph() {
			
			//create graph
			graph = new DnaGraph();
			graph.removeAllEdges();
			graph.removeAllVertices();
			
			//create vertices and get lists from statementCollection
			class1 = new ArrayList<String>();
			int count = 1;
			if (oneModeTypePanel.oneModeCombo.getSelectedItem().equals("persons x persons")) {
				class1 = sc.getStringList("p");
				for (int i = 0; i < sc.getPersonList().size(); i++) {
					if (!sc.getPersonList().get(i).equals("")) {
						graph.addVertex(new DnaGraphVertex(count, sc.getPersonList().get(i), "p"));
						count++;
					}
				}
			} else if (oneModeTypePanel.oneModeCombo.getSelectedItem().equals("organizations x organizations")) {
				class1 = sc.getStringList("o");
				for (int i = 0; i < sc.getOrganizationList().size(); i++) {
					if (!sc.getOrganizationList().get(i).equals("")) {
						graph.addVertex(new DnaGraphVertex(count, sc.getOrganizationList().get(i), "o"));
						count++;
					}
				}
			} else if (oneModeTypePanel.oneModeCombo.getSelectedItem().equals("categories x categories")) {
				class1 = sc.getStringList("c");
				for (int i = 0; i < sc.getCategoryList().size(); i++) {
					if (!sc.getCategoryList().get(i).equals("")) {
						graph.addVertex(new DnaGraphVertex(count, sc.getCategoryList().get(i), "c"));
						count++;
					}
				}
			}
			if (oneModeTypePanel.viaCombo.getSelectedItem().equals("org")) {
				class3 = sc.getStringList("o");
			} else if (oneModeTypePanel.viaCombo.getSelectedItem().equals("pers")) {
				class3 = sc.getStringList("p");
			} else {
				class3 = sc.getStringList("c");
			}
			
			s_agree = sc.getStringList("a");
			s_text = sc.getStringList("t");
			s_date = sc.getDateList();
		}
		
		/**
		 * Generate a simple graph where edge weights are the number of different co-occurrences.
		 */
		public void generateGraphCoOccurrence() {
			
			prepareSimpleGraph();
			
			tabuList tl = new tabuList();
			int count = 0;
			if (agreementPanel.conflict.isSelected()) {
				for (int i = 0; i < class1.size(); i++) {
					for (int j = 0; j < class1.size(); j++) {
						if (class3.get(i).equals(class3.get(j))
								&& !class1.get(i).equals(class1.get(j))
								&& !class1.get(i).equals("")
								&& !class1.get(j).equals("")
								&& !class3.get(i).equals("")
								&& !class3.get(j).equals("")
								&& ! s_agree.get(i).equals(s_agree.get(j))) {
							int sourceId = graph.getVertex(class1.get(i)).getId();
							int targetId = graph.getVertex(class1.get(j)).getId();
							if (graph.containsEdge(sourceId, targetId) && !tl.contains(sourceId, targetId, class3.get(i))) {
								graph.getEdge(sourceId, targetId).addToWeight(1);
							} else if (!graph.containsEdge(sourceId, targetId)) {
								graph.addEdge(new DnaGraphEdge(count, 1, graph.getVertex(sourceId), graph.getVertex(targetId)));
							}
							tl.add(new tabuItem(sourceId, targetId, class3.get(i)));
							count++;
						}
					}
				}
			} else {
				for (int i = 0; i < class1.size(); i++) {
					for (int j = 0; j < class1.size(); j++) {
						if (class3.get(i).equals(class3.get(j))
								&& !class1.get(i).equals(class1.get(j))
								&& !class1.get(i).equals("")
								&& !class1.get(j).equals("")
								&& !class3.get(i).equals("")
								&& !class3.get(j).equals("")
								&& s_agree.get(i).equals(s_agree.get(j))) {
							int sourceId = graph.getVertex(class1.get(i)).getId();
							int targetId = graph.getVertex(class1.get(j)).getId();
							if (graph.containsEdge(sourceId, targetId) && !tl.contains(sourceId, targetId, class3.get(i))) {
								graph.getEdge(sourceId, targetId).addToWeight(1);
							} else if (!graph.containsEdge(sourceId, targetId)) {
								graph.addEdge(new DnaGraphEdge(count, 1, graph.getVertex(sourceId), graph.getVertex(targetId)));
							}
							tl.add(new tabuItem(sourceId, targetId, class3.get(i)));
							count++;
						}
					}
				}
			}
			System.out.println("An adjacency graph with " + graph.countVertices() + " vertices and " + graph.countEdges() + " edges with a mean edge weight of " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + " was created.");
			
			//normalization procedure starts here
			if (normalizationPanel.normalization.isSelected()) {
				for (int i = 0; i < graph.e.size(); i++) {
					sourceCounter = 0;
					targetCounter = 0;
					tabuSource.clear();
					tabuTarget.clear();
					sourceLabel = graph.e.get(i).getSource().getLabel();
					targetLabel = graph.e.get(i).getTarget().getLabel();
					for (int j = 0; j < class1.size(); j++) {
						if (class1.get(j).equals(sourceLabel) && !tabuSource.contains(class3.get(j))) {
							sourceCounter++;
							tabuSource.add(class3.get(j));
						}
						if (class1.get(j).equals(targetLabel) && !tabuTarget.contains(class3.get(j))) {
							targetCounter++;
							tabuTarget.add(class3.get(j));
						}
					}
					graph.e.get(i).setWeight(graph.e.get(i).getWeight() / ((sourceCounter + targetCounter) / 2)); //normalization
				}
				System.out.println("Mean edge weight after normalization: " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + ", median: " + String.format(new Locale("en"), "%.2f", graph.getMedianWeight()) + ", minimum: " + String.format(new Locale("en"), "%.2f", graph.getMinimumWeight()) + ", maximum: " + String.format(new Locale("en"), "%.2f", graph.getMaximumWeight()) + ".");
			}
		}
		
		/**
		 * Generate simple graph from lists of statements. Run a time window 
		 * through the selected period and aggregate simple graph.
		 */
		public void generateGraphTimeWindow() {
			prepareSimpleGraph();
			
			Double chainD = (Double)timeWindowPanel.chain.getValue();
			int chain = new Double(chainD).intValue();
			Double shiftD = (Double)timeWindowPanel.shift.getValue();
			int shift = new Double(shiftD).intValue();
			
			int runningCount = 0;
			GregorianCalendar startOfPeriod = (GregorianCalendar)start.clone();
			startOfPeriod.add(Calendar.DATE, -chain);
			GregorianCalendar endOfPeriod = (GregorianCalendar)start.clone();
			while (!startOfPeriod.after(stop)) {
				for (int i = 0; i < class1.size(); i++) {
					for (int j = 0; j < class1.size(); j++) {
						if (class3.get(i).equals(class3.get(j))
								&& !class1.get(i).equals(class1.get(j))
								&& s_agree.get(i).equals(s_agree.get(j))
								&& !startOfPeriod.after(s_date.get(i))
								&& !s_date.get(i).after(endOfPeriod)
								&& !startOfPeriod.after(s_date.get(j))
								&& !s_date.get(j).after(endOfPeriod)
								&& !class1.get(i).equals("")
								&& !class1.get(j).equals("")
								&& !class3.get(i).equals("")
								&& !class3.get(j).equals("")) {
							int sourceId = graph.getVertex(class1.get(i)).getId();
							int targetId = graph.getVertex(class1.get(j)).getId();
							if (graph.containsEdge(sourceId, targetId)) {
								graph.getEdge(sourceId, targetId).addToWeight(1);
							} else {
								graph.addEdge(new DnaGraphEdge(runningCount, 1, graph.getVertex(sourceId), graph.getVertex(targetId)));
							}
							runningCount++;
						}
					}
				}
				startOfPeriod.add(Calendar.DATE, shift);
				endOfPeriod.add(Calendar.DATE, shift);
			}
			System.out.println("An adjacency graph with " + graph.countVertices() + " vertices and " + graph.countEdges() + " edges with a mean edge weight of " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + " was created.");
			
			//time window normalization procedure starts here
			if (normalizationPanel.normalization.isSelected()) {
				for (int i = 0; i < graph.e.size(); i++) {
					sourceCounter = 0;
					targetCounter = 0;
					tabuSource.clear();
					tabuTarget.clear();
					sourceLabel = graph.e.get(i).getSource().getLabel();
					targetLabel = graph.e.get(i).getTarget().getLabel();
					for (int j = 0; j < class1.size(); j++) {
						if (class1.get(j).equals(sourceLabel) && !tabuSource.contains(class3.get(j))) {
							sourceCounter++;
							tabuSource.add(class3.get(j));
						}
						if (class1.get(j).equals(targetLabel) && !tabuTarget.contains(class3.get(j))) {
							targetCounter++;
							tabuTarget.add(class3.get(j));
						}
					}
					double numerator = graph.e.get(i).getWeight();
					double denom1 = (sourceCounter + targetCounter) / 2;
					double denom2 = (datePanel.getDuration() + chain) / shift;
					graph.e.get(i).setWeight(numerator * 100 / (denom1 * denom2)); //normalization
				}
				System.out.println("Mean edge weight after normalization: " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + ", median: " + String.format(new Locale("en"), "%.2f", graph.getMedianWeight()) + ", minimum: " + String.format(new Locale("en"), "%.2f", graph.getMinimumWeight()) + ", maximum: " + String.format(new Locale("en"), "%.2f", graph.getMaximumWeight()) + ".");
			}
		}
		
		/**
		 * Generate a simple graph using the attenuation algorithm.
		 */
		public void generateGraphAttenuation() {
			prepareSimpleGraph();
			
			ArrayList<String> concepts = sc.getCategoryList();
			
			System.out.println("Total number of concepts: " + concepts.size());
			
			ActorList actorList = new ActorList();
			for (int i = 0; i < class1.size(); i++) {
				if (!actorList.containsActor(class1.get(i)) && !class1.get(i).equals("")) {
					actorList.add(new Actor(class1.get(i)));
				}
			}
			
			System.out.println("Total number of actors: " + actorList.size());
			
			for (int i = 0; i < actorList.size(); i++) {
				for (int j = 0; j < concepts.size(); j++) {
					actorList.get(i).addConcept(new Concept(concepts.get(j)));
				}
			}
			
			double[][][] referrals = new double[actorList.size()][actorList.size()][2];
			for (int i = 0; i < actorList.size(); i++) {
				for (int j = 0; j < actorList.size(); j++) {
					for (int k = 0; k < 1; k++) {
						referrals[i][j][k] = 0;
					}
				}
			}
			int m;
			int n;
			long time;
			double days;
			String agree;
			
			for (int l = 0; l < 2; l++) {
				
				if (l == 0) {
					agree = "yes";
				} else {
					agree = "no";
				}
				
				for (int i = 0; i < actorList.size(); i++) {
					for (int j = 0; j < actorList.get(i).conceptList.size(); j++) {
						actorList.get(i).conceptList.get(j).dateList.clear();
					}
				}
				
				for (int i = 0; i < actorList.size(); i++) {
					for (int j = 0; j < actorList.get(i).conceptList.size(); j++) {
						for (int k = 0; k < class3.size(); k++) {
							if (actorList.get(i).getId().equals(class1.get(k)) && actorList.get(i).conceptList.get(j).getName().equals(class3.get(k)) && s_agree.get(k).equals(agree)) {
								actorList.get(i).conceptList.get(j).addDate(s_date.get(k));
							}
						}
					}	
				}
				
				for (int i = 0; i < actorList.size(); i++) {
					for (int j = 0; j < actorList.get(0).conceptList.size(); j++) {
						Collections.sort(actorList.get(i).conceptList.get(j).dateList);
					}
				}
				
				//the actual algorithm follows
				for (int i = 0; i < actorList.size(); i++) {
					for (int j = 0; j < actorList.size(); j++) {
						for (int k = 0; k < concepts.size(); k++) {
							if (i != j && actorList.get(i).conceptList.get(k).dateList.size() > 0 && actorList.get(j).conceptList.get(k).dateList.size() > 0) {
								m = 0;
								n = 0;
								while (m < actorList.get(i).conceptList.get(k).dateList.size()) {
									if (n + 1 >= actorList.get(j).conceptList.get(k).dateList.size()) {
										if (actorList.get(i).conceptList.get(k).dateList.get(m).after(actorList.get(j).conceptList.get(k).dateList.get(n))) {
											time = actorList.get(i).conceptList.get(k).dateList.get(m).getTime().getTime() - actorList.get(j).conceptList.get(k).dateList.get(n).getTime().getTime();
											days = (double)Math.round( (double)time / (24. * 60.*60.*1000.) );
											if (days == 0) {
												days = 1;
											}
											if (normalizationPanel.normalization.isSelected()) {
												referrals[i][j][l] = referrals[i][j][l] + ((1/days) / actorList.get(i).conceptList.get(k).dateList.size());
											} else {
												referrals[i][j][l] = referrals[i][j][l] + 1/days; //without normalization
											}
										}
										m++;
									} else if (actorList.get(i).conceptList.get(k).dateList.get(m).after(actorList.get(j).conceptList.get(k).dateList.get(n)) && ! actorList.get(i).conceptList.get(k).dateList.get(m).after(actorList.get(j).conceptList.get(k).dateList.get(n + 1))) {
										time = actorList.get(i).conceptList.get(k).dateList.get(m).getTime().getTime() - actorList.get(j).conceptList.get(k).dateList.get(n).getTime().getTime();
										days = (int)Math.round( (double)time / (24. * 60.*60.*1000.) );
										if (days == 0) {
											days = 1;
										}
										if (normalizationPanel.normalization.isSelected()) {
											referrals[i][j][l] = referrals[i][j][l] + ((1/days) / actorList.get(i).conceptList.get(k).dateList.size());
										} else {
											referrals[i][j][l] = referrals[i][j][l] + 1/days; //without normalization
										}
										m++;
									} else if (actorList.get(i).conceptList.get(k).dateList.get(m).after(actorList.get(j).conceptList.get(k).dateList.get(n + 1))) {
										n++;
									} else {
										m++;
									}
								}
							}
						}
					}
					
				}
			}
			
			int count = 0;
			if (agreementPanel.yes.isSelected()) {
				for (int i = 0; i < referrals.length; i++) {
					for (int j = 0; j < referrals[0].length; j++) {
						if (referrals[i][j][0] > 0) {
							graph.addEdge(new DnaGraphEdge(count, referrals[i][j][0], graph.getVertex(actorList.get(i).getId()), graph.getVertex(actorList.get(j).getId())));
							count++;
						}
					}
				}
			} else if (agreementPanel.no.isSelected()) {
				for (int i = 0; i < referrals.length; i++) {
					for (int j = 0; j < referrals[0].length; j++) {
						if (referrals[i][j][1] > 0) {
							graph.addEdge(new DnaGraphEdge(count, referrals[i][j][1], graph.getVertex(actorList.get(i).getId()), graph.getVertex(actorList.get(j).getId())));
							count++;
						}
					}
				}
			} else if (agreementPanel.comb.isSelected()) {
				for (int i = 0; i < referrals.length; i++) {
					for (int j = 0; j < referrals[0].length; j++) {
						if (referrals[i][j][0] + referrals[i][j][1] > 0) {
							graph.addEdge(new DnaGraphEdge(count, (referrals[i][j][0] + referrals[i][j][1]), graph.getVertex(actorList.get(i).getId()), graph.getVertex(actorList.get(j).getId())));
							count++;
						}
					}
				}
			}
			System.out.println("An adjacency graph with " + graph.countVertices() + " vertices and " + graph.countEdges() + " edges with a mean edge weight of " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + " was created.");
		}
		
		/**
		 * This class represents a list of actors. It is needed for the attenuation algorithm.
		 */
		class ActorList extends ArrayList<Actor> {
			
			public boolean containsActor(String id) {
				boolean flag = false;
				for (int i = 0; i < this.size(); i++) {
					if (this.get(i).getId().equals(id)) {
						flag = true;
					}
				}
				return flag;
			}
			
			public Actor getActor(String id) {
				int count = -1;
				for (int i = 0; i < this.size(); i++) {
					if (this.get(i).getId().equals(id)) {
						count = i;
					}
				}
				return this.get(count);
			}
		}
		
		/**
		 * This class represents an actor. It is needed for the attenuation algorithm.
		 */
		class Actor {
			String id;
			ArrayList<Concept> conceptList;
			
			public Actor(String id) {
				this.id = id;
				conceptList = new ArrayList<Concept>();
			}
			
			public void addConcept(Concept concept) {
				conceptList.add(concept);
			}
			
			public boolean containsConcept(String id) {
				boolean flag = false;
				for (int i = 0; i < conceptList.size(); i++) {
					if (conceptList.get(i).getName().equals(id)) {
						flag = true;
					}
				}
				return flag;
			}
			
			public String getId() {
				return id;
			}
			
			public Concept getConcept(String id) {
				int count = -1;
				for (int i = 0; i < conceptList.size(); i++) {
					if (conceptList.get(i).getName().equals(id)) {
						count = i;
					}
				}
				return conceptList.get(count);
			}
		}
		
		/**
		 * This class represents a concept. It is needed for the attenuation algorithm.
		 */
		class Concept {
			ArrayList<GregorianCalendar> dateList;
			String name;
			
			public Concept(String name) {
				dateList = new ArrayList<GregorianCalendar>();
				this.name = name;
			}
			
			public void addDate(GregorianCalendar date) {
				dateList.add(date);
			}
			
			public String getName() {
				return name;
			}
		}
		
		/**
		 * Commetrix SQL export filter.
		 * 
		 * @param name of the output file
		 */
		public void exportFilterCommetrix (String outfile) {
			String networkSubName = commetrixPanel.networkName.getText();
			if (networkSubName.equals("")) {
				networkSubName = "DNA_CMX";
			}
			String nodeName = "Name";
			String nodeNumber = "Number";
			String nodeNameDescription = nodeName;
			String nodeNumberDescription = nodeNumber;
			if (oneModeTypePanel.oneModeCombo.getSelectedItem().equals("persons x persons")) {
				nodeNameDescription = "Name of the person.";
				nodeNumberDescription = "Person number.";
			} else if (oneModeTypePanel.oneModeCombo.getSelectedItem().equals("organizations x organizations")) {
				nodeNameDescription = "Name of the organization.";
				nodeNumberDescription = "Organization number.";
			} else if (oneModeTypePanel.oneModeCombo.getSelectedItem().equals("categories x categories")) {
				nodeNameDescription = "Name of the category.";
				nodeNumberDescription = "Category number.";
			}
			
			prepareSimpleGraph();
			int counter = 0;
			Double daysD = (Double)commetrixPanel.chain.getValue();
			int days = new Double(daysD).intValue();
			double duration;
			for (int i = 0; i < class1.size(); i++) {
				for (int j = 0; j < class1.size(); j++) {
					if (class3.get(i).equals(class3.get(j))
							&& !class1.get(i).equals(class1.get(j))
							&& !class1.get(i).equals("")
							&& !class1.get(j).equals("")
							&& !class3.get(i).equals("")
							&& !class3.get(j).equals("")) {
						if ((agreementPanel.conflict.isSelected() && !s_agree.get(i).equals(s_agree.get(j))) || (!agreementPanel.conflict.isSelected() && s_agree.get(i).equals(s_agree.get(j)))) {
							duration = (s_date.get(i).getTimeInMillis() - s_date.get(j).getTimeInMillis()) / (24*60*60*1000) + 1;
							if (0 <= duration && duration <= days) {
								counter++;
								graph.addEdge(new DnaGraphEdge(counter, 1, graph.getVertex(class1.get(i)), graph.getVertex(class1.get(j)), s_date.get(i), class3.get(i), s_text.get(i)));
							}
						}
					}
				}
			}
			System.out.println("A dynamic graph with " + graph.v.size() + " vertices and " + graph.e.size() + " binary edges was generated.");
			
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
				out.write("-- Commetrix SQL file.");
				out.newLine();
				out.write("-- Produced by Discourse Network Analyzer (DNA).");
				out.newLine();
				out.write("-- http://www.philipleifeld.de");
				out.newLine();
				out.newLine();
				out.newLine();
				out.newLine();
				out.write("-- Data for table `network`");
				out.newLine();
				out.newLine();
				out.write("INSERT INTO `network` (`networkid`,`ElementTypeID`,`Name`,`Detail1`,`Detail2`,`Detail3`,`Detail4`,`Detail5`) VALUES ");
				out.newLine();
				out.write(" (1,0,'" + networkSubName + "','" + networkSubName + "',NULL,NULL,NULL,NULL);");
				out.newLine();
				out.newLine();
				out.newLine();
				out.write("-- Data for table `node`");
				out.newLine();
				out.newLine();
				out.write("INSERT INTO `node` (`nodeID`,`networkid`,`AliasID`,`Detail1`,`Detail2`,`Detail3`,`Detail4`,`Detail5`) VALUES ");
				
				for (int i = 0; i < graph.v.size(); i++) {
					out.newLine();
					out.write(" (" + graph.v.get(i).getId() + ",1,0,'" + graph.v.get(i).getLabel() + "','" + graph.v.get(i).getId() + "','null','null','null')");
					if (i == graph.v.size() - 1) {
						out.write(";");
					} else {
						out.write(",");
					}
				}
				
				out.newLine();
				out.newLine();
				out.newLine();
				out.write("-- Data for table `linkevent`");
				out.newLine();
				out.newLine();
				out.write("INSERT INTO `linkevent` (`linkeventID`,`networkid`,`LinkeventDate`,`Subject`,`Content`,`Detail1`,`Detail2`,`Detail3`,`Detail4`,`Detail5`) VALUES"); 
				
				SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
				for (int i = 0; i < graph.e.size(); i++) {
					out.newLine();
					out.write(" (" + graph.e.get(i).getId() + ",1,'" + df.format(graph.e.get(i).getDate().getTime()) + "','" + graph.e.get(i).getCategory() + "','" + graph.e.get(i).getDetail() + "','null','null','null','null','null')");
					if (i == graph.e.size() - 1) {
						out.write(";");
					} else {
						out.write(",");
					}
				}
				
				out.newLine();
				out.newLine();
				out.newLine();
				out.write("-- Data for table `linkeventsender`");
				out.newLine();
				out.newLine();
				out.write("INSERT INTO `linkeventsender` (`linkeventID`,`senderNodeID`,`networkid`) VALUES ");
				
				for (int i = 0; i < graph.e.size(); i++) {
					out.newLine();
					out.write(" (" + graph.e.get(i).getId() + "," + graph.e.get(i).getSource().getId() + ",1)"); 
					if (i == graph.e.size() - 1) {
						out.write(";");
					} else {
						out.write(",");
					}
				}
				
				out.newLine();
				out.newLine();
				out.newLine();
				out.write("-- Data for table `linkeventrecipient`");
				out.newLine();
				out.newLine();
				out.write("INSERT INTO `linkeventrecipient` (`linkeventID`,`recipientNodeID`,`networkID`) VALUES ");
				
				for (int i = 0; i < graph.e.size(); i++) {
					out.newLine();
					out.write(" (" + graph.e.get(i).getId() + "," + graph.e.get(i).getTarget().getId() + ",1)"); 
					if (i == graph.e.size() - 1) {
						out.write(";");
					} else {
						out.write(",");
					}
				}
				
				out.newLine();
				out.newLine();
				
				out.newLine();
				out.write("-- Metadata for table `network`");
				out.newLine();
				out.newLine();
				out.write("INSERT INTO `networkdetailconfig` (`detail`,`label`,`description`,`isReadOnly`,`networkid`,`isSize`,`isColor`,`isNumeric`) VALUES");
				out.newLine();
				out.write(" (1,'" + networkSubName + "','" + networkSubName + "',1,1,0,0,0);");
				out.newLine();
				out.newLine();
				out.write("-- Metadata for table `node`");
				out.newLine();
				out.newLine();
				out.write("INSERT INTO `nodedetailconfig` (`detail`,`label`,`description`,`isReadOnly`,`isColor`,`isSize`,`isNumeric`,`networkid`) VALUES");
				out.newLine();
				out.write(" (1,'" + nodeName + "','" + nodeNameDescription +"',1,1,0,0,1),");
				out.newLine();
				out.write(" (2,'" + nodeNumber + "','" + nodeNumberDescription + "',1,1,0,1,1);");
				out.newLine();
				out.newLine();
				out.write("-- Metadata for table `linkevent`");
				out.newLine();
				out.newLine();
				out.write("INSERT INTO `linkeventdetailconfig` (`detail`,`label`,`description`,`isReadOnly`,`networkid`,`isSize`,`isColor`,`isNumeric`) VALUES");
				out.newLine();
				out.write(" (2,'numeric test','numeric test values',0,1,1,1,1),");
				out.newLine();
				out.write(" (3,'alphanum test','alphanumeric test values',0,1,0,1,0);");
				out.newLine();
				out.newLine();
				
				out.close();
				System.out.println("File has been exported to \"" + outfile + "\".");
			} catch (IOException e) {
				System.out.println("Error while saving Commetrix SQL file.");
			}
		}
		
		/**
		 * CSV matrix export filter.
		 * 
		 * @param name of the output file
		 */
		public void exportCsvMatrix (String outfile) {
			if (!algorithmPanel.affil.isSelected()) {
				int nc1 = graph.countVertices();
				double[][] csvmat = new double[nc1][nc1];
				
				ArrayList<String> c1list = new ArrayList<String>();
				for (int i = 0; i < class1.size(); i++) {
					if (!c1list.contains(class1.get(i)) && !class1.get(i).equals("")) {
						c1list.add(class1.get(i));
					}
				}
				
				for (int e = 0; e < graph.e.size(); e++) {
					for (int i = 0; i < nc1; i++) {
						for (int j = 0; j < nc1; j++) {
							if (c1list.get(i).equals(graph.e.get(e).source.getLabel()) && c1list.get(j).equals(graph.e.get(e).target.getLabel())) {
								csvmat[i][j] = graph.e.get(e).getWeight();
							}
						}
					}
				}
				
				try {
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
					out.write("Label");
					for (int i = 0; i < nc1; i++) {
						out.write(";" + c1list.get(i));
					}
					for (int i = 0; i < nc1; i++) {
						out.newLine();
						out.write(c1list.get(i));
						for (int j = 0; j < nc1; j++) {
							out.write(";" + String.format(new Locale("en"), "%.6f", csvmat[i][j]));
						}
					}
					out.close();
					System.out.println("File has been exported to \"" + outfile + "\".");
				} catch (IOException e) {
					System.out.println("Error while saving CSV matrix file.");
				}
			} else {
				int nc1 = 0;
				int nc2 = 0;
				ArrayList<String> c1list;
				ArrayList<String> c2list;
				if (twoModeTypePanel.oc.isSelected()) {
					nc1 = graph.countVertexType("o");
					nc2 = graph.countVertexType("c");
					c1list = sc.getOrganizationList();
					c2list = sc.getCategoryList();
				} else if (twoModeTypePanel.po.isSelected()) {
					nc1 = graph.countVertexType("p");
					nc2 = graph.countVertexType("o");
					c1list = sc.getPersonList();
					c2list = sc.getOrganizationList();
				} else {
					nc1 = graph.countVertexType("p");
					nc2 = graph.countVertexType("c");
					c1list = sc.getPersonList();
					c2list = sc.getCategoryList();
				}
				
				double[][] csvmat = new double[nc1][nc2];
				
				for (int k = 0; k < graph.countEdges(); k++) {
					for (int i = 0; i < nc1; i++) {
						for (int j = 0; j < nc2; j++) {
							DnaGraphVertex sv = graph.e.get(k).getSource();
							String sl = sv.label;
							DnaGraphVertex tv = graph.e.get(k).getTarget();
							String tl = tv.getLabel();
							if (c1list.get(i).equals(sl) && c2list.get(j).equals(tl)) {
								csvmat[i][j] = graph.e.get(k).getWeight();
							}
						}
					}
				}
				
				try {
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
					out.write("Label");
					for (int i = 0; i < nc2; i++) {
						out.write(";" + c2list.get(i));
					}
					for (int i = 0; i < nc1; i++) {
						out.newLine();
						out.write(c1list.get(i));
						for (int j = 0; j < nc2; j++) {
							out.write(";" + new Integer(new Double(csvmat[i][j]).intValue()));
						}
					}
					out.close();
					System.out.println("File has been exported to \"" + outfile + "\".");
				} catch (IOException e) {
					System.out.println("Error while saving CSV matrix file.");
				}
			}
		}
		
		/**
		 * Export filter for DL edgelist 1 or 2 files.
		 * 
		 * @param name of the output file
		 */
		private void exportDlEdgeList(String outfile) {
			if (!algorithmPanel.affil.isSelected()) {
				try {
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
					out.write("dl nr = " + graph.countVertices() + ", nc = "
							+ graph.countVertices() + ", format=edgelist1, ");
					out.newLine();
					out.write("labels embedded");
					out.newLine();
					out.newLine();
					out.write("data:");
					for (int i = 0; i < graph.v.size(); i++) {
						if (graph.vertexIsIsolate(graph.v.get(i)) == true ) {
							out.newLine();
							out.write(graph.v.get(i).getLabel());
						}
					}
					for (int i = 0; i < graph.e.size(); i++) {
						out.newLine();
						out.write(graph.e.get(i).getSource().getLabel() + " "
								+ graph.e.get(i).getTarget().getLabel() + " "
								+ String.format(new Locale("en"), "%.6f", graph.e.get(i).getWeight())); //output 6 digits after the comma
					}
					out.close();
					System.out.println("File has been exported to \"" + outfile + "\".");
				} catch (IOException e) {
					System.out.println("Error while saving edgelist1 DL file.");
				}
			} else {
				int nc1 = 0;
				int nc2 = 0;
				if (twoModeTypePanel.oc.isSelected()) {
					nc1 = graph.countVertexType("o");
					nc2 = graph.countVertexType("c");
				} else if (twoModeTypePanel.po.isSelected()) {
					nc1 = graph.countVertexType("p");
					nc2 = graph.countVertexType("o");
				} else {
					nc1 = graph.countVertexType("p");
					nc2 = graph.countVertexType("c");
				}
				
				try {
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
					out.write("dl nr = " + nc1 + ", nc = " + nc2 + ", format=edgelist2, ");
					out.newLine();
					out.write("labels embedded");
					out.newLine();
					out.newLine();
					out.write("data:");
					for (int i = 0; i < graph.e.size(); i++) {
						out.newLine();
						out.write(graph.e.get(i).getSource().getLabel() + " "
								+ graph.e.get(i).getTarget().getLabel());
					}
					out.close();
					System.out.println("File has been exported to \"" + outfile + "\".");
				} catch (IOException e) {
					System.out.println("Error while saving edgelist2 DL file.");
				}
			}
		}
		
		/**
		 * DL fullmatrix export filter.
		 * 
		 * @param name of the output file
		 */
		private void exportDlFullMatrix (String outfile) {
			if (!algorithmPanel.affil.isSelected()) {
				int nc1 = graph.countVertices();
				double[][] csvmat = new double[nc1][nc1];
				
				ArrayList<String> c1list = new ArrayList<String>();
				for (int i = 0; i < class1.size(); i++) {
					if (!c1list.contains(class1.get(i)) && !class1.get(i).equals("")) {
						c1list.add(class1.get(i));
					}
				}
				
				for (int e = 0; e < graph.e.size(); e++) {
					for (int i = 0; i < nc1; i++) {
						for (int j = 0; j < nc1; j++) {
							if (c1list.get(i).equals(graph.e.get(e).source.getLabel()) && c1list.get(j).equals(graph.e.get(e).target.getLabel())) {
								csvmat[i][j] = graph.e.get(e).getWeight();
							}
						}
					}
				}
				
				try {
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
					out.write("DL");
					out.newLine();
					out.write("N=" + nc1);
					out.newLine();
					out.write("FORMAT = FULLMATRIX DIAGONAL PRESENT");
					out.newLine();
					out.write("ROW LABELS:");
					for (int i = 0; i < nc1; i++) {
						out.newLine();
						out.write("\"" + c1list.get(i) + "\"");
					}
					out.newLine();
					out.write("ROW LABELS EMBEDDED");
					out.newLine();
					out.write("COLUMN LABELS:");
					for (int i = 0; i < nc1; i++) {
						out.newLine();
						out.write("\"" + c1list.get(i) + "\"");
					}
					out.newLine();
					out.write("COLUMN LABELS EMBEDDED");
					out.newLine();
					out.write("DATA:");
					out.newLine();
					out.write("      ");
					for (int i = 0; i < nc1; i++) {
						out.write(" \"" + c1list.get(i) + "\"");
					}
					for (int i = 0; i < nc1; i++) {
						out.newLine();
						out.write("\"" + c1list.get(i) + "\"");
						for (int j = 0; j < nc1; j++) {
							out.write(" " + String.format(new Locale("en"), "%.6f", csvmat[i][j]));
						}
					}
					out.close();
					System.out.println("File has been exported to \"" + outfile + "\".");
				} catch (IOException e) {
					System.out.println("Error while saving CSV matrix file.");
				}
			} else {
				int nc1 = 0;
				int nc2 = 0;
				ArrayList<String> c1list;
				ArrayList<String> c2list;
				if (twoModeTypePanel.oc.isSelected()) {
					nc1 = graph.countVertexType("o");
					nc2 = graph.countVertexType("c");
					c1list = sc.getOrganizationList();
					c2list = sc.getCategoryList();
				} else if (twoModeTypePanel.po.isSelected()) {
					nc1 = graph.countVertexType("p");
					nc2 = graph.countVertexType("o");
					c1list = sc.getPersonList();
					c2list = sc.getOrganizationList();
				} else {
					nc1 = graph.countVertexType("p");
					nc2 = graph.countVertexType("c");
					c1list = sc.getPersonList();
					c2list = sc.getCategoryList();
				}
				
				double[][] csvmat = new double[nc1][nc2];
				
				for (int k = 0; k < graph.countEdges(); k++) {
					for (int i = 0; i < nc1; i++) {
						for (int j = 0; j < nc2; j++) {
							DnaGraphVertex sv = graph.e.get(k).getSource();
							String sl = sv.label;
							DnaGraphVertex tv = graph.e.get(k).getTarget();
							String tl = tv.getLabel();
							if (c1list.get(i).equals(sl) && c2list.get(j).equals(tl)) {
								csvmat[i][j] = graph.e.get(k).getWeight();
							}
						}
					}
				}
				
				try {
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
					out.write("DL");
					out.newLine();
					out.write("NR=" + nc1 + ", NC=" + nc2);
					out.newLine();
					out.write("FORMAT = FULLMATRIX DIAGONAL PRESENT");
					out.newLine();
					out.write("ROW LABELS:");
					for (int i = 0; i < nc1; i++) {
						out.newLine();
						out.write("\"" + c1list.get(i) + "\"");
					}
					out.newLine();
					out.write("ROW LABELS EMBEDDED");
					out.newLine();
					out.write("COLUMN LABELS:");
					for (int i = 0; i < nc2; i++) {
						out.newLine();
						out.write("\"" + c2list.get(i) + "\"");
					}
					out.newLine();
					out.write("COLUMN LABELS EMBEDDED");
					out.newLine();
					out.write("DATA:");
					out.newLine();
					out.write("      ");
					for (int i = 0; i < nc2; i++) {
						out.write(" \"" + c2list.get(i) + "\"");
					}
					out.newLine();
					for (int i = 0; i < nc1; i++) {
						out.newLine();
						out.write("\"" + c1list.get(i) + "\"");
						for (int j = 0; j < nc2; j++) {
							//out.write(String.format(new Locale("en"), "%.6f", csvmat[i][j]));
							out.write(" " + new Integer(new Double(csvmat[i][j]).intValue()));
						}
					}
					out.close();
					System.out.println("File has been exported to \"" + outfile + "\".");
				} catch (IOException e) {
					System.out.println("Error while saving CSV matrix file.");
				}
			}
		}
		
		/**
		 * Export filter for graphML files.
		 * 
		 * @param name of the output file
		 */
		private void exportGraphMl(String outfile) {
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
				out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				out.newLine();
				out.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
				out.newLine();
				out.newLine();
				out.write("<!-- data schema -->");
				out.newLine();
				out.write("<key id=\"id\" for=\"node\" attr.name=\"id\" attr.type=\"string\"/>");
				out.newLine();
				out.write("<key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>");
				out.newLine();
				if (algorithmPanel.affil.isSelected()) {
					out.write("<key id=\"class\" for=\"node\" attr.name=\"class\" attr.type=\"string\"/>");
				} else {
					out.write("<key id=\"weight\" for=\"edge\" attr.name=\"weight\" attr.type=\"int\"/>");
				}
				out.newLine();
				out.newLine();
				out.write("<graph edgedefault=\"directed\">");
				out.newLine();
				out.newLine();
				out.write("<!-- nodes -->");

				if (algorithmPanel.affil.isSelected()) {
					for (int i = 0; i < graph.v.size(); i++) {
						out.newLine();
						out.write("<node id=\"" + graph.v.get(i).getId() + "\">");
						out.newLine();
						out.write(" <data key=\"id\">" + graph.v.get(i).getLabel() + "</data>");
						out.newLine();
						out.write(" <data key=\"label\">" + graph.v.get(i).getLabel() + "</data>");
						out.newLine();
						out.write(" <data key=\"class\">" + graph.v.get(i).getType() + "</data>");
						out.newLine();
						out.write(" </node>");
					}
				} else {
					for (int i = 0; i < graph.v.size(); i++) {
						out.newLine();
						out.write("<node id=\"" + graph.v.get(i).getId() + "\">");
						out.newLine();
						out.write(" <data key=\"id\">" + graph.v.get(i).getLabel() + "</data>");
						out.newLine();
						out.write(" <data key=\"label\">" + graph.v.get(i).getLabel() + "</data>");
						out.newLine();
						out.write(" </node>");
					}
				}

				out.newLine();
				out.newLine();
				out.write("<!-- edges -->");

				if (algorithmPanel.affil.isSelected()) {
					for (int i = 0; i < graph.e.size(); i++) {
						out.newLine();
						out.write("<edge source=\"" + graph.e.get(i).getSource().getId()
								+ "\" target=\"" + graph.e.get(i).getTarget().getId() + "\"></edge>");
					}
				} else {
					for (int i = 0; i < graph.e.size(); i++) {
						//if (graph.e.get(i).getWeight() > 0) {
							out.newLine();
							out.write("<edge source=\"" + graph.e.get(i).getSource().getId()
									+ "\" target=\"" + graph.e.get(i).getTarget().getId() + "\">");
							out.newLine();
							out.write(" <data key=\"weight\">" + String.format(new Locale("en"), "%.6f", graph.e.get(i).getWeight()) + "</data>");
							out.newLine();
							out.write(" </edge>");
						//}
					}
				}

				out.newLine();
				out.newLine();
				out.write("</graph>");
				out.newLine();
				out.write("</graphml>");
				out.close();
				System.out.println("File has been exported to \"" + outfile + "\".");
			} catch (IOException e) {
				System.out.println("Error while saving GraphML file.");
			}
		}
	}
	
	/**
	 * Main method. Instantiates the application.
	 */
	public static void main (String[] args) {
		new DnaExportWindow();
	}
}