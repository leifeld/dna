package dna.export;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import dna.Dna;
import dna.dataStructures.StatementType;
import dna.renderer.StatementTypeComboBoxModel;
import dna.renderer.StatementTypeComboBoxRenderer;

@SuppressWarnings("serial")
public class Exporter extends JDialog {
	
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
		settingsPanel.add(networkModesLabel, gbc);
		
		gbc.gridx = 1;
		JLabel statementTypeLabel = new JLabel("Statement type");
		settingsPanel.add(statementTypeLabel, gbc);
		
		gbc.gridx = 2;
		JLabel fileFormatLabel = new JLabel("File format");
		settingsPanel.add(fileFormatLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		String[] networkModesItems = new String[] {"Two-mode network", "One-mode network", "Event list"};
		JComboBox<String> networkModesBox = new JComboBox<>(networkModesItems);
		settingsPanel.add(networkModesBox, gbc);

		gbc.gridx = 1;
		StatementTypeComboBoxRenderer cbrenderer = new StatementTypeComboBoxRenderer();
		StatementTypeComboBoxModel model = new StatementTypeComboBoxModel();
		JComboBox<StatementType> statementTypeBox = new JComboBox<>(model);
		statementTypeBox.setRenderer(cbrenderer);
		
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
		int HEIGHT = (int) statementTypeBox.getPreferredSize().getHeight();
		int WIDTH = 200;
		Dimension d = new Dimension(WIDTH, HEIGHT);
		networkModesBox.setPreferredSize(d);
		
		gbc.gridx = 2;
		String[] fileFormatItems = new String[] {".csv", ".dl", ".graphml"};
		JComboBox<String> fileFormatBox = new JComboBox<>(fileFormatItems);
		settingsPanel.add(fileFormatBox, gbc);
		fileFormatBox.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		statementTypeBox.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		// second row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 2;
		JLabel var1Label = new JLabel("Variable 1");
		settingsPanel.add(var1Label, gbc);
		
		gbc.gridx = 1;
		JLabel var2Label = new JLabel("Variable 2");
		settingsPanel.add(var2Label, gbc);

		gbc.gridx = 2;
		JLabel qualifierLabel = new JLabel("Qualifier");
		settingsPanel.add(qualifierLabel, gbc);

		gbc.gridx = 3;
		JLabel aggregationLabel = new JLabel("Qualifier aggregation");
		settingsPanel.add(aggregationLabel, gbc);

		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 3;
		JComboBox<String> var1Box = new JComboBox<>(var1Items);
		settingsPanel.add(var1Box, gbc);
		int HEIGHT2 = (int) var1Box.getPreferredSize().getHeight();
		var1Box.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		JComboBox<String> var2Box = new JComboBox<>(var2Items);
		var2Box.setSelectedIndex(1);
		settingsPanel.add(var2Box, gbc);
		var2Box.setPreferredSize(new Dimension(WIDTH, HEIGHT2));

		gbc.gridx = 2;
		//String[] qualifierItems = getVariablesList((StatementType) statementTypeBox.getSelectedItem(), false, false, true, true);
		String[] qualifierItems = new String[0];
		JComboBox<String> qualifierBox = new JComboBox<>(qualifierItems);
		settingsPanel.add(qualifierBox, gbc);
		qualifierBox.setEnabled(false);
		qualifierBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));

		gbc.gridx = 3;
		String[] aggregationItems = new String[] {"ignore", "combine", "subtract"};
		JComboBox<String> aggregationBox = new JComboBox<>(aggregationItems);
		settingsPanel.add(aggregationBox, gbc);
		aggregationBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		// third row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 4;
		JLabel normalizationLabel = new JLabel("Normalization");
		settingsPanel.add(normalizationLabel, gbc);
		
		gbc.gridx = 1;
		JLabel isolatesLabel = new JLabel("Isolates");
		settingsPanel.add(isolatesLabel, gbc);

		gbc.gridx = 2;
		JLabel duplicatesLabel = new JLabel("Duplicates");
		settingsPanel.add(duplicatesLabel, gbc);

		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 5;
		String[] normalizationItems = new String[0];
		JComboBox<String> normalizationBox = new JComboBox<>(normalizationItems);
		settingsPanel.add(normalizationBox, gbc);
		normalizationBox.setEnabled(false);
		normalizationBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		String[] isolatesItems = new String[] {"include isolates", "only current nodes"};
		JComboBox<String> isolatesBox = new JComboBox<>(isolatesItems);
		settingsPanel.add(isolatesBox, gbc);
		isolatesBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));

		gbc.gridx = 2;
		String[] duplicatesItems = new String[] {"include all duplicates", "ignore per document"};
		JComboBox<String> duplicatesBox = new JComboBox<>(duplicatesItems);
		settingsPanel.add(duplicatesBox, gbc);
		duplicatesBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		// fourth row of options
		gbc.insets = new Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 6;
		JLabel startLabel = new JLabel("Include from");
		settingsPanel.add(startLabel, gbc);
		
		gbc.gridx = 1;
		JLabel stopLabel = new JLabel("Include until");
		settingsPanel.add(stopLabel, gbc);

		gbc.gridx = 2;
		JLabel temporalLabel = new JLabel("Temporal aggregation");
		settingsPanel.add(temporalLabel, gbc);

		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 7;
		SpinnerDateModel startModel = new SpinnerDateModel();
		JSpinner startSpinner = new JSpinner();
		startModel.setCalendarField(Calendar.DAY_OF_YEAR);
		startSpinner.setModel(startModel);
		ArrayList<Date> dates = new ArrayList<Date>();
		for (int i = 0; i < Dna.data.getStatements().size(); i++) {
			dates.add(Dna.data.getStatements().get(i).getDate());
		}
		Collections.sort(dates);
		startModel.setValue(dates.get(0));
		startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd - HH:mm:ss"));
		settingsPanel.add(startSpinner, gbc);
		startSpinner.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		SpinnerDateModel stopModel = new SpinnerDateModel();
		JSpinner stopSpinner = new JSpinner();
		stopModel.setCalendarField(Calendar.DAY_OF_YEAR);
		stopSpinner.setModel(stopModel);
		stopModel.setValue(dates.get(dates.size() - 1));
		stopSpinner.setEditor(new JSpinner.DateEditor(stopSpinner, "yyyy-MM-dd - HH:mm:ss"));
		settingsPanel.add(stopSpinner, gbc);
		stopSpinner.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 2;
		String[] temporalItems = new String[] {"across date range", "nested by document"};
		JComboBox<String> temporalBox = new JComboBox<>(temporalItems);
		settingsPanel.add(temporalBox, gbc);
		temporalBox.setPreferredSize(new Dimension(WIDTH, HEIGHT2));
		
		this.add(settingsPanel, BorderLayout.NORTH);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * This function returns a String array with the variables of the {@link StatementType} selected to fill a {@link JComboBox}.
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
}
