package dna;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.toedter.calendar.JDateChooser;

/**
 * This is the export component of the Discourse Analyzer (DNA). The
 * main component, dnaCoder, will save its current file and pass over
 * the file name of the currently open dna file to this component.
 * The purpose is the generation of network files from the assigned
 * statement XML tags. Several file formats and options as well as
 * a time window algorithm are available in order to generate simple
 * or bipartite graphs.
 * 
 * @author Philip Leifeld
 * @version 1.0 - 16 April 2009
 */
public class DnaExport extends JFrame {
	
	String infile;
	Container c;
	DnaParseXml exportInput;

	//declarations for the network type panel
	JLabel by, via;
	String[] classes;
	JComboBox c1Combo, c2Combo, viaCombo;
	JPanel typePanel;

	//declarations for the format panel
	JPanel formatPanel;
	JRadioButton csv, el, nl, mat, gml;

	//declarations for the agreement panel
	JPanel agreePanel;
	JRadioButton yes, no, comb, conflict;
	
	//declarations for the normalization panel
	JPanel normalizePanel;
	JRadioButton normYes, normNo;
	
	//declarations for the algorithm panel
	JPanel algoPanel;
	JRadioButton xSec, tWind, atten;
	
	//declarations for the date panel
	JPanel datePanel;
	JDateChooser startCal, stopCal;
	int duration;
	GregorianCalendar fDate, lDate;

	//declarations for the time parameter panel
	JPanel chainPanel;
	JLabel chainLabel, shiftLabel, days2, days3;
	JSpinner chain, shift;
	SpinnerNumberModel shiftModel, chainModel;

	//declarations for the button panel
	JPanel buttonPanel, overallPanel;
	JButton export, reset;
	JProgressBar progress;


	/**
	 * Constructor of the export component. Sets the layout and opens
	 * the dna file.
	 * 
	 * @param input file
	 */
	public DnaExport( String infile ) {
		this.infile = infile;
		c = getContentPane();
		this.setTitle("DNA Export");
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
		this.setIconImage(dna32Icon.getImage());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		exportInput = new DnaParseXml( this.infile ); //open the dna file
		buttons();
		c.add(overallPanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * Make the classes/type panel active or inactive.
	 * 
	 * @param boolean true (=active) or false (=inactive)
	 */
	private void setTypePanelActive(boolean active) {
		via.setEnabled(active);
		by.setEnabled(active);
		c1Combo.setEnabled(active);
		c2Combo.setEnabled(active);
		viaCombo.setEnabled(active);
	}

	/**
	 * Make the time parameter panel active or inactive.
	 * 
	 * @param boolean true (=active) or false (=inactive)
	 */
	private void setChainPanelActive(boolean active) {
		chainPanel.setEnabled(active);
		chain.setEnabled(active);
		chainLabel.setEnabled(active);
		days2.setEnabled(active);
		shift.setEnabled(active);
		shiftLabel.setEnabled(active);
		days3.setEnabled(active);
		typePanel.setEnabled(active);
	}

	/**
	 * Make the algorithm panel active or inactive.
	 * 
	 * @param boolean true (=active) or false (=inactive)
	 */
	private void setAlgoPanelActive(boolean active) {
		algoPanel.setEnabled(active);
		xSec.setEnabled(active);
		tWind.setEnabled(active);
		atten.setEnabled(active);
	}

	/**
	 * Create a panel where the network format (CSV, DL, graphML) can be selected.
	 */
	private void networkFormat() {
		formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		formatPanel.setBorder( new TitledBorder( new EtchedBorder(), "1 Export format" ) );

		ActionListener formatButtonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(csv)) {
					setChainPanelActive(false);
					setTypePanelActive(false);
					setAlgoPanelActive(false);
				} else {
					if (c1Combo.getSelectedItem().equals(c2Combo.getSelectedItem())) {
						if (tWind.isSelected()) {
							setChainPanelActive(true);
						} else {
							setChainPanelActive(false);
						}
						
						setAlgoPanelActive(true);
					} else {
						setChainPanelActive(false);
						setAlgoPanelActive(false);
					}
					setTypePanelActive(true);
				}
			}
		};

		ButtonGroup formatGroup = new ButtonGroup();
		csv = new JRadioButton("CSV", false);
		csv.addActionListener(formatButtonListener);
		formatGroup.add(csv);
		formatPanel.add(csv);
		el = new JRadioButton("DL edgelist", true);
		el.addActionListener(formatButtonListener);
		formatGroup.add(el);
		formatPanel.add(el);
		gml = new JRadioButton("graphML", false);
		gml.addActionListener(formatButtonListener);
		formatGroup.add(gml);
		formatPanel.add(gml);
		nl = new JRadioButton("DL nodelist", false);
		nl.addActionListener(formatButtonListener);
		formatGroup.add(nl);
		formatPanel.add(nl);
		nl.setVisible(false);
		mat = new JRadioButton("DL matrix", false);
		mat.addActionListener(formatButtonListener);
		formatGroup.add(mat);
		formatPanel.add(mat);
		mat.setVisible(false);
	}

	/**
	 * Adjust network type combo boxes depending on selection.
	 */
	private void checkComboBoxes() {
		if (c1Combo.getSelectedItem().equals(c2Combo.getSelectedItem())) {
			via.setText("via");
			viaCombo.removeAllItems();
			if (!c1Combo.getSelectedItem().equals("Persons")) { viaCombo.addItem("Persons"); }
			if (!c1Combo.getSelectedItem().equals("Organizations")) { viaCombo.addItem("Organizations"); }
			if (!c1Combo.getSelectedItem().equals("Categories")) { viaCombo.addItem("Categories"); }
			viaCombo.setSelectedIndex(1);
			viaCombo.setVisible(true);
			setAlgoPanelActive(true);
			if (tWind.isSelected()) {
				setChainPanelActive(true);
			}
		} else {
			via.setText(" (bipartite network)");
			viaCombo.removeAllItems();
			viaCombo.setVisible(false);
			setAlgoPanelActive(false);
			setChainPanelActive(false);
		}
	}

	/**
	 * Create combo boxes for network type.
	 */
	private void networkType() {
		by = new JLabel("by");
		via = new JLabel("via");
		classes = new String[] {"Persons", "Organizations", "Categories"};
		c1Combo = new JComboBox(classes);
		c2Combo = new JComboBox(classes);
		viaCombo = new JComboBox(classes);
		c1Combo.setSelectedIndex( 1 );
		c2Combo.setSelectedIndex( 1 );
		viaCombo.setSelectedIndex( 2 );
		viaCombo.removeItem( "Organizations" );

		c2Combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkComboBoxes();
			}
		});

		c1Combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkComboBoxes();
			}            
		});

		typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		typePanel.setBorder( new TitledBorder( new EtchedBorder(), "2 Network type" ) );
		typePanel.add(c1Combo);
		typePanel.add(by);
		typePanel.add(c2Combo);
		typePanel.add(via);
		typePanel.add(viaCombo);
	}

	/**
	 * Decide which algorithm to employ.
	 */
	private void setAlgorithm() {
		algoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		algoPanel.setBorder( new TitledBorder( new EtchedBorder(), "3 Algorithm" ) );

		ActionListener algoListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(xSec)) {
					setChainPanelActive(false);
					conflict.setEnabled(true);
				} else if (e.getSource().equals(tWind)) {
					setChainPanelActive(true);
					conflict.setEnabled(false);
				} else if (e.getSource().equals(atten)) {
					setChainPanelActive(false);
					conflict.setEnabled(false);
				}
			}
		};

		ButtonGroup algoGroup = new ButtonGroup();
		xSec = new JRadioButton("Number of co-occurrences", true);
		xSec.addActionListener(algoListener);
		algoGroup.add(xSec);
		algoPanel.add(xSec);
		tWind = new JRadioButton("Time window", false);
		tWind.addActionListener(algoListener);
		algoGroup.add(tWind);
		algoPanel.add(tWind);
		atten = new JRadioButton("Attenuation", false);
		atten.addActionListener(algoListener);
		algoGroup.add(atten);
		algoPanel.add(atten);
	}
	
	/**
	 * JRadioButtons for the agreement options.
	 */
	private void setAgreement() {
		agreePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		agreePanel.setBorder( new TitledBorder( new EtchedBorder(), "4 Agreement" ) );

		ButtonGroup agreeGroup = new ButtonGroup();
		yes = new JRadioButton("yes", false);
		agreeGroup.add(yes);
		agreePanel.add(yes);
		no = new JRadioButton("no", false);
		agreeGroup.add(no);
		agreePanel.add(no);
		comb = new JRadioButton("combined", true);
		agreeGroup.add(comb);
		agreePanel.add(comb);
		conflict = new JRadioButton("conflict", false);
		agreeGroup.add(conflict);
		agreePanel.add(conflict);
	}
	
	private void setNormalization() {
		normalizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		normalizePanel.setBorder( new TitledBorder( new EtchedBorder(), "5 Normalization" ) );
		ButtonGroup normalizeGroup = new ButtonGroup();
		normYes = new JRadioButton("yes", false);
		normalizeGroup.add(normYes);
		normalizePanel.add(normYes);
		normNo = new JRadioButton("no", true);
		normalizeGroup.add(normNo);
		normalizePanel.add(normNo);
	}

	/**
	 * Set the start and end date of the time period to consider for export.
	 */
	private void setDates() {
		datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		datePanel.setBorder( new TitledBorder( new EtchedBorder(), "6 Time period" ) );

		String datePattern = new String("dd.MM.yyyy");
		String maskPattern = new String("##/##/####");
		final char placeholder = '_';
		startCal = new JDateChooser( datePattern, maskPattern, placeholder );
		fDate = exportInput.firstDate;
		startCal.setDate(fDate.getTime());
		startCal.setPreferredSize(new Dimension(100, 32));
		stopCal = new JDateChooser( datePattern, maskPattern, placeholder );
		lDate = exportInput.lastDate;
		stopCal.setDate(lDate.getTime());
		stopCal.setPreferredSize(new Dimension(100, 32));
		JLabel startLabel = new JLabel("start: ");
		JLabel stopLabel = new JLabel("stop: ");

		JPanel start = new JPanel();
		start.add(startLabel);
		start.add(startCal);

		JPanel stop = new JPanel();
		stop.add(stopLabel);
		stop.add(stopCal);

		datePanel.add(start);
		datePanel.add(stop);

		startCal.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				calcDuration();
			}
		});

		stopCal.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				calcDuration();
			}
		});
	}

	/**
	 * Calculate the duration between the start and the end date.
	 * 
	 * @return duration
	 */
	private int getDuration(){
		Date date1 = startCal.getDate();
		Date date2 = stopCal.getDate();
		GregorianCalendar d1cal = new GregorianCalendar();
		d1cal.setTime(date1);
		GregorianCalendar d2cal = new GregorianCalendar();
		d2cal.setTime(date2);
		double dur = (d2cal.getTimeInMillis() - d1cal.getTimeInMillis()) / (24*60*60*1000) + 1;
		dur = Math.abs(dur);
		return (int)dur;
	}

	/**
	 * Set the upper bound for the chain and shift spinner.
	 */
	private void calcDuration() {
		chain.setModel(new SpinnerNumberModel(20, 1, getDuration(), 1));
		shift.setModel(new SpinnerNumberModel(1, 1, getDuration(), 1));
	}

	/**
	 * Set chaining and shift parameters for the time window.
	 */
	private void timeParameters() {
		days2 = new JLabel(" days");
		chainModel = new SpinnerNumberModel(20, 1, getDuration(), 1);
		chain = new JSpinner(chainModel);
		chainLabel = new JLabel("moving time window of");

		chainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		chainPanel.setBorder( new TitledBorder( new EtchedBorder(), "7 Chaining parameter" ) );
		chainPanel.add(chainLabel);
		chainPanel.add(chain);

		shiftModel = new SpinnerNumberModel(1, 1, getDuration(), 1);
		shift = new JSpinner(shiftModel);
		shiftLabel = new JLabel(" which is shifted by");
		days3 = new JLabel(" days");
		chainPanel.add(days2);
		chainPanel.add(shiftLabel);
		chainPanel.add(shift);
		chainPanel.add(days3);

		chain.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent l) {
				shiftModel.setMaximum(getDuration());
			}
		});
		
		setChainPanelActive(false);
	}

	/**
	 * Create the overall layout panel. Add status bar and progress bar to the bottom.
	 */
	private void buttons() {
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		reset = new JButton("Reset values");
		export = new JButton("Export...");
		buttonPanel.add(reset);
		buttonPanel.add(export);

		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				c1Combo.setSelectedItem("Organizations");
				c2Combo.setSelectedItem("Organizations");
				checkComboBoxes();
				el.setSelected(true);
				comb.setSelected(true);
				xSec.setSelected(true);
				setChainPanelActive(false);
				startCal.setDate(fDate.getTime());
				stopCal.setDate(lDate.getTime());
				calcDuration();
				shiftModel.setMaximum(getDuration());
				shiftModel.setValue(getDuration());
				normNo.setSelected(true);
			}
		});

		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileExporter exp = new fileExporter();
				exp.start();
			}
		});

		progress = new JProgressBar(0, 100);
		progress.setIndeterminate(true);
		progress.setString("Calculating...");
		progress.setStringPainted(true);
		progress.setVisible(false);

		JPanel exportPanel = new JPanel(new GridLayout(7,1));
		networkFormat();
		exportPanel.add(formatPanel);
		networkType();
		exportPanel.add(typePanel);
		setAlgorithm();
		exportPanel.add(algoPanel);
		setAgreement();
		exportPanel.add(agreePanel);
		setNormalization();
		exportPanel.add(normalizePanel);
		setDates();
		exportPanel.add(datePanel);
		timeParameters();
		exportPanel.add(chainPanel);

		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.add(buttonPanel, BorderLayout.EAST);
		statusPanel.add(progress, BorderLayout.WEST);

		overallPanel = new JPanel(new BorderLayout());
		overallPanel.add(exportPanel, BorderLayout.CENTER);
		overallPanel.add(statusPanel, BorderLayout.SOUTH);
	}

	/**
	 * Thread for the actual aggregation of the simple or bipartite
	 * network from the data. Also includes the file export filters.
	 */
	class fileExporter extends Thread {

		//declarations for the export thread
		String outfile;
		String sync = new String("sync"); //some object necessary for thread synchronization
		String extension, description, ag;
		GregorianCalendar startDate, stopDate, startOfPeriod, endOfPeriod;
		ArrayList<String> s_pers = new ArrayList<String>();
		ArrayList<String> s_org = new ArrayList<String>();
		ArrayList<String> s_agree = new ArrayList<String>();
		ArrayList<String> s_cat = new ArrayList<String>();
		ArrayList<GregorianCalendar> s_date = new ArrayList<GregorianCalendar>();
		ArrayList<String> class1, class2, class3;
		ArrayList<Boolean> withinPeriod;
		DnaGraph simple, bip;
		int runningCount;
		HashMap<DnaEdge,String> edgeDate = new HashMap<DnaEdge,String>();
		
		//declarations for normalization procedures
		int sourceCounter;
		int targetCounter;
		String sourceLabel;
		String targetLabel;
		ArrayList<String> tabuSource = new ArrayList<String>();
		ArrayList<String> tabuTarget = new ArrayList<String>();

		/**
		 * Control the export thread.
		 */
		public void run() {
			export.setEnabled(false);
			reset.setEnabled(false);
			String outfile = getFileName();
			progress.setVisible(true);
			if (!outfile.equals("null")) {
				timeAgreeFilter();
				if (csv.isSelected()) {
					csvFilter(outfile);
				} else if (!c1Combo.getSelectedItem().equals(c2Combo.getSelectedItem())) {
					Formatierung(s_pers);
					Formatierung(s_org);
					Formatierung(s_cat);
					generateBipartiteGraph();
					if (el.isSelected()) {
						edgeList(outfile);
					} else if (gml.isSelected()) {
						exportGraphMl(outfile);
					}
				} else {
					Formatierung(s_pers);
					Formatierung(s_org);
					Formatierung(s_cat);
					if (tWind.isSelected()) {
						timeWindow();
						if (el.isSelected()) {
							edgeList(outfile);
						} else if (gml.isSelected()) {
							exportGraphMl(outfile);
						}
					} else if (xSec.isSelected()) {
						xSec();
						if (el.isSelected()) {
							edgeList(outfile);
						} else if (gml.isSelected()) {
							exportGraphMl(outfile);
						}
					} else if (atten.isSelected()) {
						atten();
						if (el.isSelected()) {
							edgeList(outfile);
						} else if (gml.isSelected()) {
							exportGraphMl(outfile);
						}
					}
				}
				synchronized (sync) {
					sync.notifyAll();
				}
			}
			export.setEnabled(true);
			reset.setEnabled(true);
			progress.setVisible(false);
		}

		/**
		 * File chooser.
		 * 
		 * @return string of the file name
		 */
		private String getFileName() {
			if (csv.isSelected()) {
				extension = ".csv";
				description = "Comma-separated values (*.csv)";
			} else if (el.isSelected()) {
				extension = ".dl";
				description = "UCINET edgelist DL file (*.dl)";
			} else if (gml.isSelected()) {
				extension = ".graphML";
				description = "GraphML file (*.graphML)";
			} else if (nl.isSelected()) {
				extension = ".dl";
				description = "UCINET nodelist DL file (*.dl)";
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

			int returnVal = fc.showSaveDialog(DnaExport.this);
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

		/**
		 * Remove strange symbols from persons, organizations and categories.
		 * 
		 * @param ArrayList of strings (before string manipulation)
		 * @return ArrayList of strings (after string manipulation)
		 */
		@SuppressWarnings( "unchecked" )
		private ArrayList Formatierung(ArrayList variable) {
			for (int i = 0; i < variable.size(); i++) {
				String zeichenkette = new String((String)variable.get(i).toString());
				zeichenkette = zeichenkette.replaceAll("ß", "ss");
				zeichenkette = zeichenkette.replaceAll("[^a-zA-ZäüöÄÜÖ\\s]", "");
				zeichenkette = zeichenkette.replaceAll("ß", "ss");
				zeichenkette = zeichenkette.replaceAll("ä", "ae");
				zeichenkette = zeichenkette.replaceAll("Ä", "Ae");
				zeichenkette = zeichenkette.replaceAll("ö", "oe");
				zeichenkette = zeichenkette.replaceAll("Ö", "Oe");
				zeichenkette = zeichenkette.replaceAll("ü", "ue");
				zeichenkette = zeichenkette.replaceAll("Ü", "Ue");
				zeichenkette = zeichenkette.replaceAll("^ ", "");
				zeichenkette = zeichenkette.replaceAll(" $", "");
				zeichenkette = zeichenkette.replaceAll("\\s+", "_");
				variable.set(i, zeichenkette);
			}
			return variable;
		}

		/**
		 * Retain only statements within the date range and matching the agreement options.
		 */
		private void timeAgreeFilter() {
			startDate = new GregorianCalendar();
			stopDate = new GregorianCalendar();
			startDate.setTime(startCal.getDate());
			stopDate.setTime(stopCal.getDate());
			GregorianCalendar stopDatePlus = (GregorianCalendar)stopDate.clone();
			stopDatePlus.add(Calendar.DATE, 1);
			System.out.println("new dates: " + startDate.get(Calendar.YEAR)
					+ "-" + (startDate.get(Calendar.MONTH)+1) + "-" + startDate.get(Calendar.DATE)
					+ " and " + stopDate.get(Calendar.YEAR) + "-" + (stopDate.get(Calendar.MONTH)+1)
					+ "-" + stopDate.get(Calendar.DATE));

			if ( yes.isSelected() ) {
				ag = "yes";
			} else if ( no.isSelected() ) {
				ag = "no";
			} else {
				ag = "combined";
			}

			s_org.clear();
			s_pers.clear();
			s_cat.clear();
			s_agree.clear();
			s_date.clear();
			GregorianCalendar currentDate = new GregorianCalendar();
			for ( int i = 0; i < exportInput.st_agree.size(); i++ ) {
				currentDate.set(Integer.parseInt(exportInput.st_year.get(i)),
						(Integer.parseInt(exportInput.st_month.get(i))-1),
						Integer.parseInt(exportInput.st_day.get(i)));
				if ( exportInput.st_agree.get(i).equals(ag) && !startDate.after(currentDate) && !currentDate.after(stopDatePlus) ) {
					s_agree.add(exportInput.st_agree.get(i));
					s_pers.add(exportInput.st_pers.get(i));
					s_org.add(exportInput.st_org.get(i));
					s_cat.add(exportInput.st_cat.get(i));
					s_date.add(new GregorianCalendar(currentDate.get(Calendar.YEAR),
							currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)));
				}
				else if ( ag.equals("combined") && (!startDate.after(currentDate) && !currentDate.after(stopDatePlus)) ) {
					s_agree.add(exportInput.st_agree.get(i));
					s_pers.add(exportInput.st_pers.get(i));
					s_org.add(exportInput.st_org.get(i));
					s_cat.add(exportInput.st_cat.get(i));
					s_date.add(new GregorianCalendar(currentDate.get(Calendar.YEAR),
							currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)));
				}
			}
			if ( s_agree.size() == 1 ) {
				System.out.println( s_agree.size() + 
						" statement retained after applying date and agreement filter." );
			} else {
				System.out.println( s_agree.size() + 
						" statements retained after applying date and agreement filter." );
			}
		}

		/**
		 * CSV export filter.
		 * 
		 * @param name of the output file
		 */
		private void csvFilter ( String outfile ) {
			try {
				BufferedWriter br = new BufferedWriter(new FileWriter(outfile));
				br.write("date;person;organization;category;agreement");
				for (int i = 0; i < s_pers.size(); i++) {
					br.newLine();
					br.write(s_date.get(i).get(Calendar.YEAR) + "-"
							+ (s_date.get(i).get(Calendar.MONTH)+1) + "-"
							+ s_date.get(i).get(Calendar.DATE) + ";"
							+ s_pers.get(i) + ";" + s_org.get(i) + ";"
							+ s_cat.get(i) + ";" + s_agree.get(i));
				}
				br.close();
				System.out.println("File has been exported to \"" + outfile + "\".");
			}
			catch (IOException e) {
				System.out.println("Error while saving CSV file.");
			}
		}

		/**
		 * Setup array lists for the three possible vertex classes.
		 */
		private void setupClasses() {
			class1 = new ArrayList<String>();
			class2 = new ArrayList<String>();
			class3 = new ArrayList<String>();
			if (c1Combo.getSelectedItem().equals("Persons")) {
				class1 = s_pers;
			} else if (c1Combo.getSelectedItem().equals("Organizations")) {
				class1 = s_org;
			} else {
				class1 = s_cat;
			}
			if (c2Combo.getSelectedItem().equals("Persons")) {
				class2 = s_pers;
			} else if (c2Combo.getSelectedItem().equals("Organizations")) {
				class2 = s_org;
			} else {
				class2 = s_cat;
			}
			if (c1Combo.getSelectedItem().equals(c2Combo.getSelectedItem())) {
				if (viaCombo.getSelectedItem().equals("Persons")) {
					class3 = s_pers;
				} else if (viaCombo.getSelectedItem().equals("Organizations")) {
					class3 = s_org;
				} else {
					class3 = s_cat;
				}
			}
		}

		/**
		 * Generate bipartite graph from lists of statements.
		 */
		private void generateBipartiteGraph() {

			//create bipartite graph with all vertices present in the time period
			setupClasses();
			bip = new DnaGraph();
			bip.removeAllEdges();
			bip.removeAllVertices();
			String id1 = "";
			String id2 = "";
			for (int i = 0; i < class1.size(); i++) {
				id1 = class1.get(i) + "1";
				if (bip.containsVertex(id1) == false) {
					bip.addVertex(new DnaVertex(id1, class1.get(i), false));
				}
			}
			for (int i = 0; i < class2.size(); i++) {
				id2 = class2.get(i) + "2";
				if (bip.containsVertex(id2) == false) {
					bip.addVertex(new DnaVertex(id2, class2.get(i), true));
				}
			}

			//add bipartite edges
			String edgeId;
			id1 = "";
			id2 = "";
			for (int i = 0; i < class1.size(); i++) {
				edgeId = class1.get(i) + class2.get(i);
				if (!bip.containsEdge(edgeId) && !class1.get(i).equals("") && !class2.get(i).equals("")) {
					id1 = class1.get(i) + "1";
					id2 = class2.get(i) + "2";
					bip.addEdge(new DnaEdge(edgeId, 1, bip.getVertex(id1), bip.getVertex(id2)));
				} //weights are not necessary because the graph is bipartite, i.e. there are no co-occurrences
			}

			//report
			System.out.println("Bipartite graph has " + bip.numberOfClass0()
					+ " class 1 vertices and " + bip.numberOfClass1() 
					+ " class 2 vertices with " + bip.numberOfEdges() + " edges.");
		}

		/**
		 * Set up simple graph and create vertices from class1.
		 */
		private void createSimpleVertices() {
			setupClasses();
			simple = new DnaGraph();
			simple.removeAllEdges();
			simple.removeAllVertices();
			for (int i = 0; i < class1.size(); i++) {
				if (!simple.containsVertex(class1.get(i))) {
					simple.addVertex(new DnaVertex(class1.get(i), class1.get(i)));
				}
			}
		}

		/**
		 * Generate simple graph from lists of statements. Run a time window 
		 * through the selected period and aggregate simple graph.
		 */
		private void timeWindow() {
			createSimpleVertices();			
			runningCount = 0;
			startOfPeriod = (GregorianCalendar)startDate.clone();
			startOfPeriod.add(Calendar.DATE, -((Integer)chain.getValue()));
			endOfPeriod = (GregorianCalendar)startDate.clone();
			while (!startOfPeriod.after(stopDate)) {
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
							runningCount++;
							if (simple.containsEdge(class1.get(i), class1.get(j))) {
								simple.getEdge(class1.get(i), class1.get(j)).setWeight(
										simple.getEdge(class1.get(i), class1.get(j)).getWeight() + 1);
							} else {
								String runningId = new Integer(runningCount).toString();
								simple.addEdge(new DnaEdge(runningId, 1, 
										simple.getVertex(class1.get(i)), simple.getVertex(class1.get(j))));
							}
						}
					}
				}
				startOfPeriod.add(Calendar.DATE, (Integer)shift.getValue());
				endOfPeriod.add(Calendar.DATE, (Integer)shift.getValue());
			}
			
			//time window normalization procedure starts here
			if (normYes.isSelected()) {
				for (int i = 0; i < simple.e.size(); i++) {
					sourceCounter = 0;
					targetCounter = 0;
					tabuSource.clear();
					tabuTarget.clear();
					sourceLabel = simple.e.get(i).getSource().getLabel();
					targetLabel = simple.e.get(i).getTarget().getLabel();
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
					double numerator = simple.e.get(i).getWeight();
					double denom1 = (sourceCounter + targetCounter) / 2;
					double denom2 = (getDuration() + (Integer)chain.getValue()) / (Integer)shift.getValue();
					simple.e.get(i).setWeight(numerator * 100 / (denom1 * denom2)); //normalization
				}
			}
			
		}
		
		/**
		 * Export filter for DL edgelist 1 or 2 files.
		 * 
		 * @param name of the output file
		 */
		private void edgeList(String outfile) {
			if (c1Combo.getSelectedItem().equals(c2Combo.getSelectedItem())) {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
					out.write("dl nr = " + simple.numberOfVertices() + ", nc = "
							+ simple.numberOfVertices() + ", format=edgelist1, ");
					out.newLine();
					out.write("labels embedded");
					out.newLine();
					out.newLine();
					out.write("data:");
					for (int i = 0; i < simple.v.size(); i++) {
						if (simple.vertexIsIsolate(simple.v.get(i)) == true ) {
							out.newLine();
							out.write(simple.v.get(i).getLabel());
						}
					}
					for (int i = 0; i < simple.e.size(); i++) {
						out.newLine();
						out.write(simple.e.get(i).getSource().getLabel() + " "
								+ simple.e.get(i).getTarget().getLabel() + " "
								+ String.format(new Locale("en"), "%.6f", simple.e.get(i).getWeight())); //output 6 digits after the comma
					}
					out.close();
					System.out.println("File has been exported to \"" + outfile + "\".");
				} catch (IOException e) {
					System.out.println("Error while saving edgelist1 DL file.");
				}
			} else {
				int nc1 = bip.numberOfClass0();
				int nc2 = bip.numberOfClass1();
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
					out.write("dl nr = " + nc1 + ", nc = " + nc2 + ", format=edgelist2, ");
					out.newLine();
					out.write("labels embedded");
					out.newLine();
					out.newLine();
					out.write("data:");
					for (int i = 0; i < bip.e.size(); i++) {
						out.newLine();
						out.write(bip.e.get(i).getSource().getLabel() + " "
								+ bip.e.get(i).getTarget().getLabel());
					}
					out.close();
					System.out.println("File has been exported to \"" + outfile + "\".");
				} catch (IOException e) {
					System.out.println("Error while saving edgelist2 DL file.");
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
				BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
				out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				out.newLine();
				out.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
				out.newLine();
				out.newLine();
				out.write("<!-- data schema -->");
				out.newLine();
				out.write("<key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>");
				out.newLine();
				if (!c1Combo.getSelectedItem().equals(c2Combo.getSelectedItem())) {
					out.write("<key id=\"class\" for=\"node\" attr.name=\"class\" attr.type=\"boolean\"/>");
				} else {
					out.write("<key id=\"weight\" for=\"edge\" attr.name=\"weight\" attr.type=\"int\"/>");
				}
				out.newLine();
				out.newLine();
				out.write("<graph edgedefault=\"directed\">");
				out.newLine();
				out.newLine();
				out.write("<!-- nodes -->");

				if (!c1Combo.getSelectedItem().equals(c2Combo.getSelectedItem())) {
					for (int i = 0; i < bip.v.size(); i++) {
						out.newLine();
						out.write("<node id=\"" + bip.v.get(i).getId() + "\">");
						out.newLine();
						out.write(" <data key=\"label\">" + bip.v.get(i).getLabel() + "</data>");
						out.newLine();
						if (bip.v.get(i).getClass1() == true) {
							out.write(" <data key=\"class\">0</data>");
						} else {
							out.write(" <data key=\"class\">1</data>");
						}
						out.newLine();
						out.write(" </node>");
					}
				} else {
					for (int i = 0; i < simple.v.size(); i++) {
						out.newLine();
						out.write("<node id=\"" + simple.v.get(i).getId() + "\">");
						out.newLine();
						out.write(" <data key=\"label\">" + simple.v.get(i).getLabel() + "</data>");
						out.newLine();
						out.write(" </node>");
					}
				}

				out.newLine();
				out.newLine();
				out.write("<!-- edges -->");

				if (!c1Combo.getSelectedItem().equals(c2Combo.getSelectedItem())) {
					for (int i = 0; i < bip.e.size(); i++) {
						out.newLine();
						out.write("<edge source=\"" + bip.e.get(i).getSource().getId()
								+ "\" target=\"" + bip.e.get(i).getTarget().getId() + "\"></edge>");
					}
				} else {
					for (int i = 0; i < simple.e.size(); i++) {
						if (simple.e.get(i).getWeight() > 0) {
							out.newLine();
							out.write("<edge source=\"" + simple.e.get(i).getSource().getId()
									+ "\" target=\"" + simple.e.get(i).getTarget().getId() + "\">");
							out.newLine();
							out.write(" <data key=\"weight\">" + String.format(new Locale("en"), "%.6f", simple.e.get(i).getWeight()) + "</data>");
							out.newLine();
							out.write(" </edge>");
						}
					}
				}

				out.newLine();
				out.newLine();
				out.write("</graph>");
				out.newLine();
				out.write("</graphml>");
				out.close();
			} catch (IOException e) {
				System.out.println("Error while saving GraphML file.");
			}
		}

		/**
		 * Generate a simple graph where edge weights are the number of different co-occurrences.
		 */
		private void xSec() {
			createSimpleVertices();
			ArrayList<String> tabuList = new ArrayList<String>();
			
			if (conflict.isSelected()) {
				for (int i = 0; i < class1.size(); i++) {
					for (int j = 0; j < class1.size(); j++) {
						if (class3.get(i).equals(class3.get(j))
								&& !class1.get(i).equals(class1.get(j))
								&& !class1.get(i).equals("")
								&& !class1.get(j).equals("")
								&& !class3.get(i).equals("")
								&& !class3.get(j).equals("")
								&& ! s_agree.get(i).equals(s_agree.get(j))) {
							if (simple.containsEdge(class1.get(i) + class1.get(j)) && !tabuList.contains(class1.get(i) + class3.get(i) + class1.get(j))) {
								simple.getEdge(class1.get(i) + class1.get(j)).addToWeight(1);
							} else if (!simple.containsEdge(class1.get(i) + class1.get(j))) {
								simple.addEdge(new DnaEdge(class1.get(i) + class1.get(j), 1, simple.getVertex(class1.get(i)), simple.getVertex(class1.get(j))));
							}
							tabuList.add(class1.get(i) + class3.get(i) + class1.get(j));
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
							if (simple.containsEdge(class1.get(i) + class1.get(j)) && !tabuList.contains(class1.get(i) + class3.get(i) + class1.get(j))) {
								simple.getEdge(class1.get(i) + class1.get(j)).addToWeight(1);
							} else if (!simple.containsEdge(class1.get(i) + class1.get(j))) {
								simple.addEdge(new DnaEdge(class1.get(i) + class1.get(j), 1, simple.getVertex(class1.get(i)), simple.getVertex(class1.get(j))));
							}
							tabuList.add(class1.get(i) + class3.get(i) + class1.get(j));
						}
					}
				}
			}
			
			//xsec normalization procedure starts here
			if (normYes.isSelected()) {
				for (int i = 0; i < simple.e.size(); i++) {
					sourceCounter = 0;
					targetCounter = 0;
					tabuSource.clear();
					tabuTarget.clear();
					sourceLabel = simple.e.get(i).getSource().getLabel();
					targetLabel = simple.e.get(i).getTarget().getLabel();
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
					simple.e.get(i).setWeight(simple.e.get(i).getWeight() / ((sourceCounter + targetCounter) / 2)); //normalization
				}
			}
		}
		
		/**
		 * Generate a simple graph using the attenuation algorithm.
		 */
		private void atten() {
			createSimpleVertices();
			ArrayList<String> concepts = new ArrayList<String>();
			for (int i = 0; i < class3.size(); i++) {
				if (!concepts.contains(class3.get(i))) {
					concepts.add(class3.get(i));
				}
			}
			
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
											if (normYes.isSelected()) {
												referrals[i][j][l] = referrals[i][j][l] + ((1/days) / actorList.get(i).conceptList.get(k).dateList.size());
											} else {
												referrals[i][j][l] = referrals[i][j][l] + 1/days; //without normalization
											}
										}
										m++;
									} else if (actorList.get(i).conceptList.get(k).dateList.get(m).after(actorList.get(j).conceptList.get(k).dateList.get(n)) && ! actorList.get(i).conceptList.get(k).dateList.get(m).after(actorList.get(j).conceptList.get(k).dateList.get(n + 1))) {
										time = actorList.get(i).conceptList.get(k).dateList.get(m).getTime().getTime() - actorList.get(j).conceptList.get(k).dateList.get(n).getTime().getTime();
										days = (int)Math.round( (double)time / (24. * 60.*60.*1000.) );
										if (normYes.isSelected()) {
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
			
			if (yes.isSelected()) {
				for (int i = 0; i < referrals.length; i++) {
					for (int j = 0; j < referrals[0].length; j++) {
						if (referrals[i][j][0] > 0) {
							String id = actorList.get(i).getId() + actorList.get(j).getId();
							simple.addEdge(new DnaEdge(id, referrals[i][j][0], simple.getVertex(actorList.get(i).getId()), simple.getVertex(actorList.get(j).getId())));
						}
					}
				}
			} else if (no.isSelected()) {
				for (int i = 0; i < referrals.length; i++) {
					for (int j = 0; j < referrals[0].length; j++) {
						if (referrals[i][j][1] > 0) {
							String id = actorList.get(i).getId() + actorList.get(j).getId();
							simple.addEdge(new DnaEdge(id, referrals[i][j][1], simple.getVertex(actorList.get(i).getId()), simple.getVertex(actorList.get(j).getId())));
						}
					}
				}
			} else if (comb.isSelected()) {
				for (int i = 0; i < referrals.length; i++) {
					for (int j = 0; j < referrals[0].length; j++) {
						if (referrals[i][j][0] + referrals[i][j][1] > 0) {
							String id = actorList.get(i).getId() + actorList.get(j).getId();
							simple.addEdge(new DnaEdge(id, (referrals[i][j][0] + referrals[i][j][1]), simple.getVertex(actorList.get(i).getId()), simple.getVertex(actorList.get(j).getId())));
						}
					}
				}
			}
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
	}
}