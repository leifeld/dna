package dna;

import java.awt.BorderLayout;
import java.awt.Container;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class TimeSeriesWindow extends JFrame {
	
	Container c;
	IncludePanel includePanel;
	StatementContainer sCont;
	DatePanel datePanel;
	TimeStepPanel timeStepPanel;
	ActorTypePanel actorTypePanel;
	TypePanel typePanel;
	AggregationPanel aggregationPanel;
	ButtonPanel buttonPanel;
	
	public TimeSeriesWindow(StatementContainer sCont) {
		this.sCont = sCont;
		this.setTitle("DNA Statistics");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon statisticsIcon = new ImageIcon(getClass().getResource("/icons/chart_curve.png"));
		this.setIconImage(statisticsIcon.getImage());
		
		appearance();
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public void appearance() {
		
		c = getContentPane();
		
		JPanel panel = new JPanel(new GridBagLayout()); //center of the options panel
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(2,2,2,2);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		includePanel = new IncludePanel();
		panel.add(includePanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		datePanel = new DatePanel();
		panel.add(datePanel, gbc);
		
		gbc.gridx = 1;
		timeStepPanel = new TimeStepPanel();
		panel.add(timeStepPanel, gbc);
		
		gbc.gridx = 2;
		typePanel = new TypePanel();
		panel.add(typePanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		actorTypePanel = new ActorTypePanel();
		panel.add(actorTypePanel, gbc);
		
		gbc.gridx = 1;
		gbc.gridwidth = 2;
		aggregationPanel = new AggregationPanel();
		panel.add(aggregationPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		buttonPanel = new ButtonPanel();
		panel.add(buttonPanel, gbc);
		
		c.add(panel);
	}
	
	public void resetAll() {
		buttonPanel.reset();
		includePanel.reset();
		datePanel.reset();
		timeStepPanel.reset();
		typePanel.reset();
		actorTypePanel.reset();
		aggregationPanel.reset();
		pack();
	}
	
	public class FileExporter extends Thread {
		
		String description, extension, filename;
		StatementContainer sc;
		
		public void run() {
			filename = getFileName();
			
			if (filename.equals("null")) {
				//canceled export
			} else {
				buttonPanel.export.setEnabled(false);
				buttonPanel.reset.setEnabled(false);
				buttonPanel.progress.setVisible(true);
				
				sc = (StatementContainer)sCont.clone();
				
				boolean persons = false;
				if (actorTypePanel.persons.isSelected()) {
					persons = true;
				}
				
				String timeUnit;
				if (timeStepPanel.month.isSelected()) {
					timeUnit = "month";
				} else if (timeStepPanel.year.isSelected()) {
					timeUnit = "year";
				} else {
					timeUnit = "total";
				}
				
				String ignoreDuplicates;
				if (typePanel.articleDuplicates.isSelected()) {
					ignoreDuplicates = "article";
				} else if (typePanel.monthDuplicates.isSelected()) {
					ignoreDuplicates = "month";
				} else {
					ignoreDuplicates = "off";
				}
				
				boolean onePerRow;
				if (aggregationPanel.rows.isSelected() == true) {
					onePerRow = true;
				} else {
					onePerRow = false;
				}
				
				Date start = (Date)datePanel.startSpinner.getValue();
				Date stop = (Date)datePanel.stopSpinner.getValue();
				
				String[] includedPersons = includePanel.getIncludedPersons();
				String[] includedOrganizations = includePanel.getIncludedOrganizations();
				String[] includedCategories = includePanel.getIncludedCategories();
				
				new TimeSeriesExporter(sc, filename, persons, timeUnit, ignoreDuplicates, onePerRow, start, stop, includedPersons, includedOrganizations, includedCategories);
				//String[] test = new String[2];
				//test[0] = "all";
				//new TimeSeriesExporter("/home/philip/Desktop/test.dna", false, "month", "article", true, "first", "last", test, test, test);
			}
			
			buttonPanel.export.setEnabled(true);
			buttonPanel.reset.setEnabled(true);
			buttonPanel.progress.setVisible(false);
		}

		private String getFileName() {
			extension = ".csv";
			description = "Comma-separated values (*.csv)";
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

			int returnVal = fc.showSaveDialog(TimeSeriesWindow.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				String filename = new String(file.getPath());
				if ( !file.getPath().endsWith(extension) ) {
					filename = filename + extension;
				}
				return filename;
			} else {
				System.out.println("Statistics export cancelled.");
				return "null";
			}
		}
		
	}
	
	public class ButtonPanel extends JPanel {
		
		JProgressBar progress;
		JButton reset, export;
		
		public ButtonPanel() {
			setLayout(new BorderLayout());
			
			ActionListener buttonListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource().equals(reset)) {
						resetAll();
					} else if (e.getSource().equals(export)) {
						FileExporter exp = new FileExporter();
						exp.start();
					}
				}
			};
			
			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			
			progress = new JProgressBar(0, 100);
			progress.setIndeterminate(true);
			progress.setString("Calculating...");
			progress.setStringPainted(true);
			progress.setToolTipText("<html>The calculation is in progress.<br/>" +
					"It may take a couple of minutes<br/>" +
					"if you have a slow machine and<br/>" +
					"many statements.</html>");
			progress.setVisible(false);
			
			Icon resetIcon = new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png"));
			reset = new JButton("Reset", resetIcon);
			reset.addActionListener(buttonListener);
			reset.setToolTipText("Revert all options to their default settings.");
			Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
			export = new JButton("Export...", okIcon);
			export.setToolTipText("Choose a file name and export with the selected options.");
			export.addActionListener(buttonListener);
			export.setEnabled(true);
			buttons.add(progress);
			buttons.add(reset);
			buttons.add(export);
			
			add(buttons, BorderLayout.EAST);
		}
		
		public void reset() {
			export.setEnabled(true);
			reset.setEnabled(true);
			progress.setVisible(false);
		}
	}
	
	public class AggregationPanel extends JPanel {
		
		ButtonGroup aggregationGroup;
		JRadioButton rows, aggregate;
		
		public AggregationPanel() {
			setLayout(new GridLayout(2,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Aggregation" ) );
			
			aggregationGroup = new ButtonGroup();
			aggregate = new JRadioButton("aggregate all selected actors in one row", false);
			aggregate.setToolTipText("<html>This will yield the frequencies for all actors together.</html>");
			aggregationGroup.add(aggregate);
			add(aggregate);
			
			rows = new JRadioButton("export one actor per row", true);
			rows.setToolTipText("<html>This will yield the frequencies for each<br/>" +
					"actor separately (i.e. one actor per row).</html>");
			aggregationGroup.add(rows);
			add(rows);
		}
		
		public void reset() {
			rows.setSelected(true);
		}
	}
	
	public class TypePanel extends JPanel {
		
		ButtonGroup typeGroup;
		JRadioButton total, monthDuplicates, articleDuplicates;
		
		public TypePanel() {
			setLayout(new GridLayout(3,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Duplicate filter" ) );
			
			typeGroup = new ButtonGroup();
			total = new JRadioButton("total number of statements", false);
			total.setToolTipText("<html>Do not apply any duplicate filter.</html>");
			typeGroup.add(total);
			add(total);
			
			monthDuplicates = new JRadioButton("ignore duplicates per month", false);
			monthDuplicates.setToolTipText("<html>Do not count a statement if there is another statement with the same<br/>" +
					"actor, category and agreement pattern in the same month.</html>");
			typeGroup.add(monthDuplicates);
			add(monthDuplicates);
			
			articleDuplicates = new JRadioButton("ignore duplicates per article", true);
			articleDuplicates.setToolTipText("<html>Do not count a statement if there is another statement with the same<br/>" +
			"actor, category and agreement pattern in the same article.</html>");
			typeGroup.add(articleDuplicates);
			add(articleDuplicates);
		}
		
		public void reset() {
			articleDuplicates.setSelected(true);
		}
	}
	
	public class ActorTypePanel extends JPanel {
		
		ButtonGroup actorTypeGroup;
		JRadioButton persons, organizations;
		
		public ActorTypePanel() {
			setLayout(new GridLayout(2,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Type of actor" ) );
			
			actorTypeGroup = new ButtonGroup();
			persons = new JRadioButton("persons", false);
			persons.setToolTipText("<html>Use the list of selected persons above to generate the statistics.</html>");
			actorTypeGroup.add(persons);
			add(persons);
			
			organizations = new JRadioButton("organizations", true);
			organizations.setToolTipText("<html>Use the list of selected organizations above to generate the statistics.</html>");
			actorTypeGroup.add(organizations);
			add(organizations);
		}
		
		public void reset() {
			organizations.setSelected(true);
		}
	}
	
	public class TimeStepPanel extends JPanel {
		
		ButtonGroup timeStepGroup;
		JRadioButton total, month, year;
		
		public TimeStepPanel() {
			setLayout(new GridLayout(3,1));
			setBorder( new TitledBorder( new EtchedBorder(), "Time steps" ) );
			
			timeStepGroup = new ButtonGroup();
			month = new JRadioButton("per month", true);
			month.setToolTipText("<html>Count the number of statements per month.</html>");
			timeStepGroup.add(month);
			add(month);
			
			year = new JRadioButton("per year", false);
			year.setToolTipText("<html>Count the number of statements per year.</html>");
			timeStepGroup.add(year);
			add(year);
			
			total = new JRadioButton("total", false);
			total.setToolTipText("<html>Count all statements (i.e. no time series).</html>");
			timeStepGroup.add(total);
			add(total);
		}
		
		public void reset() {
			month.setSelected(true);
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
			
			startLabel = new JLabel("start:");
			try {
				fDate = sCont.getFirstDate();
			} catch (NullPointerException npe) {
				fDate = new GregorianCalendar(1900, 1, 1);
			}
			startDate = fDate.getTime();
			startSpinner = new JSpinner();
			startSpinner.setToolTipText("All statements made before this date are not counted.");
			startLabel.setToolTipText("All statements made before this date are not counted.");
			startModel = new SpinnerDateModel();
			startModel.setCalendarField( Calendar.DAY_OF_YEAR );
			startModel.setValue(startDate);
			startSpinner.setModel( startModel );
			startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "dd.MM.yyyy"));
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
			stopSpinner.setToolTipText("All statements made after this date are not counted.");
			stopLabel.setToolTipText("All statements made after this date are not counted.");
			stopModel = new SpinnerDateModel();
			stopModel.setCalendarField( Calendar.DAY_OF_YEAR );
			stopModel.setValue(stopDate);
			stopSpinner.setModel( stopModel );
			stopSpinner.setEditor(new JSpinner.DateEditor(stopSpinner, "dd.MM.yyyy"));
			stopSpinner.addChangeListener(dateListener);
			add(stopLabel);
			add(stopSpinner);
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
	
	public class IncludePanel extends JPanel {
		
		JList person, organization, category;
		DefaultListModel personModel, organizationModel, categoryModel;
		JScrollPane personScroller, organizationScroller, categoryScroller;
		
		public IncludePanel() {
			setBorder( new TitledBorder( new EtchedBorder(), "Inclusion of actors and categories" ) );
			
			JPanel personPanel = new JPanel(new BorderLayout());
			personModel = new DefaultListModel();
			person = new JList(personModel);
			person.setToolTipText("<html>Keep the Ctrl key pressed while selecting or<br/>" +
					"unselecting actors that should be considered.</html>");
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
			organization.setToolTipText("<html>Keep the Ctrl key pressed while selecting or<br/>" +
			"unselecting actors that should be considered.</html>");
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
			category.setToolTipText("<html>Keep the Ctrl key pressed while selecting or<br/>" +
			"unselecting categories that should be considered.</html>");
			category.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			category.setLayoutOrientation(JList.VERTICAL);
			category.setVisibleRowCount(5);
			category.setFixedCellWidth(30);
			categoryScroller = new JScrollPane(category);
			JLabel categoryLabel = new JLabel("categories");
			categoryPanel.add(categoryLabel, BorderLayout.NORTH);
			categoryPanel.add(categoryScroller, BorderLayout.CENTER);
			
			setLayout(new GridLayout(2,1));
			JPanel horizontalPanel = new JPanel(new GridLayout(1,2));
			horizontalPanel.add(personPanel);
			horizontalPanel.add(organizationPanel);
			add(horizontalPanel);
			add(categoryPanel);
			reset();
		}
		
		public String[] getIncludedPersons() {
			String[] persons = new String[personModel.size()];
			for (int i = 0; i < personModel.size(); i++) {
				persons[i] = (String) personModel.getElementAt(i);
			}
			
			return persons;
		}

		public String[] getIncludedOrganizations() {
			String[] organizations = new String[organizationModel.size()];
			for (int i = 0; i < organizationModel.size(); i++) {
				organizations[i] = (String) organizationModel.getElementAt(i);
			}
			
			return organizations;
		}

		public String[] getIncludedCategories() {
			String[] categories = new String[categoryModel.size()];
			for (int i = 0; i < categoryModel.size(); i++) {
				categories[i] = (String) categoryModel.getElementAt(i);
			}
			
			return categories;
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
				person.setSelectionInterval(0, personModel.size()-1);
				for (int i = 0; i < organizations.size(); i++) {
					organizationModel.addElement(organizations.get(i));
				}
				organization.setSelectionInterval(0, organizationModel.size()-1);
				for (int i = 0; i < categories.size(); i++) {
					categoryModel.addElement(categories.get(i));
				}
				category.setSelectionInterval(0, categoryModel.size()-1);
			} catch (NullPointerException npe) {
				//no file was open
			}
			
		}
	}
	
}