package dna;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTextField;

import dna.dataStructures.StatementType;

public class Recode extends JDialog {
	private static final long serialVersionUID = 1L;

	JPanel window2ButtonNext,  window2ButtonGo, metaListPanel, tableBooleanPanel;
	JDialog openTableImportFromFile, window1, window2, window3;
	GridBagConstraints gbc;
	/* LB.Note: Recode variable is called "variable". 
	 * Additional numerical variable is called "boolean" */
	JComboBox<String> typeBox, variableBox, newVariableDataTypeBox, 
	variableOverwriteBox, newBooleanDataTypeBox, booleanOverwriteBox, booleanBox;
	JList<String> variableEntryList;
	JRadioButton createNewVariable, overwriteVariable, 
	createNewBoolean, overwriteBoolean;
	JXTextField newVariableTextField, newBooleanTextField;
	JButton addVariableButton, removeVariableButton, nextButton, 
	cancelButtonWindow2, previousButtonWindow2, goButtonWindow2, 
	nextButtonWindow2, cancelButtonWindow3, previousButtonWindow3, 
	goButtonWindow3;
	String statementType, oldVariableName, oldVariableDataType, 
	oldVariableColumnName, newVariableName, newVariableColumnName,
	newVariableDataType, oldBooleanName, newBooleanName, booleanDataType, 
	newBooleanColumnName, oldBooleanColumnName, booleanPanelBorderName, 
	newBooleanDataType;
	ImageIcon cancelIcon, nextIcon, addIcon, previousIcon, goIcon;
	DefaultTableModel tableModel, tableMetaListModel, tableModelImportFromFile, 
	tableModelBoolean;
	JXTable tableRecode, tableMetaList, tableBoolean; 
	File file;
	ArrayList<String> linesImported;
	public static String[] columnNames = new String[3];
	public static String columnNameMetaList;
	public static String[] columnNamesBooleanPanel = new String[5];

	//-------------------------------------------------------------------------	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------	

	public Recode() {

		this.setTitle("Recode variables");
		this.setModal(true);
		ImageIcon networkIcon = new ImageIcon(getClass().getResource(
				"/icons/pencil.png"));
		this.setIconImage(networkIcon.getImage());
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		// set tool tip time
		ToolTipManager.sharedInstance().setInitialDelay(0);
		//ToolTipManager.sharedInstance().setDismissDelay(500);

		new SetVariablesWindow();
		//this.add(window1, BorderLayout.NORTH);
		window1.pack();
		window1.setLocationRelativeTo(null);
		window1.setVisible(true);

	}

	/*---------------------------------------------------------------------
	 * WINDOW 1: SET VARIABLES
	 *--------------------------------------------------------------------*/
	class SetVariablesWindow  {
		public SetVariablesWindow(){
			/*
			 *  WINDOW 1.1: SELECT VARIABLE
			 */
			JPanel chooseVariablePanel = new JPanel(new GridBagLayout());
			chooseVariablePanel = new JPanel(new GridBagLayout());
			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(1, 0, 1, 5);

			// window 1.1: chose variable - label
			gbc.gridy = 0;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			JLabel chooseVar = new JLabel("choose variable: ", JLabel.RIGHT);
			chooseVariablePanel.add(chooseVar, gbc);

			// window 1.1: choose variable type - ComboBox
			gbc.gridy = 0;
			gbc.gridx = 1;
			ArrayList<StatementType> typeList = Dna.dna.db.getStatementTypes();
			String[] types = new String[typeList.size()];
			for (int i = 0; i < typeList.size(); i++) {
				types[i] = typeList.get(i).getLabel();
			}
			typeBox = new JComboBox<String>(types);
			statementType = typeBox.getSelectedItem().toString();
			chooseVariablePanel.add(typeBox, gbc);

			typeBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {	
					variableBox.removeAllItems();
					variableOverwriteBox.removeAllItems();
					String type = (String) typeBox.getSelectedItem();
					if (type != null && !type.equals("")) {
						// task 1: set variable selection for recode variable
						HashMap<String, String> variables = Dna.dna.db.
								getVariablesByTypes(type, "short text", 
										"long text");
						Iterator<String> keyIterator = variables.keySet().
								iterator();
						while (keyIterator.hasNext()){
							String key = keyIterator.next();
							variableBox.addItem(key);
							variableOverwriteBox.addItem(key);
						}
						variableBox.setSelectedIndex(0);
						variableOverwriteBox.setSelectedIndex(0);
						// task2: set variable selection for additional variable
						booleanBox.removeAllItems();
						HashMap<String, String> variablesBoolean = Dna.dna.db.
								getVariablesByTypes(type, "boolean", "integer");
						Iterator<String> keyIteratorBoolean = variablesBoolean.
								keySet().iterator();
						while (keyIteratorBoolean.hasNext()){
							String key = keyIteratorBoolean.next();
							booleanBox.addItem(key);
						}
						booleanBox.addItem("");
						booleanBox.setSelectedItem("");
					}
				}
			});	 

			// window 1.1: choose variable - ComboBox
			gbc.gridy = 0;
			gbc.gridx = 2; 
			String type = typeBox.getSelectedItem().toString();
			LinkedHashMap<String, String> variableList = Dna.dna.db.
					getVariablesByTypes(type, "short text", "long text");
			Object[] variableKeys = variableList.keySet().toArray();
			String[] variables = new String[variableKeys.length];
			for (int i = 0; i < variableKeys.length; i++) {
				variables[i] = variableKeys[i].toString();
			}
			variableBox = new JComboBox<String>(variables);
			chooseVariablePanel.add(variableBox, gbc);

			// window 1.1: new variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel newVariableLabel = new JLabel("new variable: ", JLabel.RIGHT);
			chooseVariablePanel.add(newVariableLabel, gbc);

			// window 1.1: create button group for overwrite vs. new
			ButtonGroup overNewButtonGroup = new ButtonGroup();

			// window 1.1: create new variable - radioButton
			gbc.gridx = 1;
			createNewVariable = new JRadioButton("create new variable");
			overNewButtonGroup.add(createNewVariable);	
			createNewVariable.setSelected(true);
			chooseVariablePanel.add(createNewVariable, gbc);

			createNewVariable.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (createNewVariable.isSelected()){
						newVariableTextField.setEnabled(true);
						newVariableDataTypeBox.setEnabled(true);
						variableOverwriteBox.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});	

			// window 1.1: overwrite existing variable - radioButton
			gbc.gridx = 2;
			overwriteVariable = new JRadioButton("overwrite existing variable");
			overNewButtonGroup.add(overwriteVariable);
			chooseVariablePanel.add(overwriteVariable, gbc);

			overwriteVariable.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (overwriteVariable.isSelected()){
						newVariableTextField.setEnabled(false);
						newVariableDataTypeBox.setEnabled(false);
						variableOverwriteBox.setEnabled(true);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});

			// window 1.1: set variable name/type - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			JLabel setVariableLabel = new JLabel("set name/type of variable: ", 
					JLabel.RIGHT);
			chooseVariablePanel.add(setVariableLabel, gbc);

			// window 1.1: variable name - text field
			gbc.gridx = 1; 
			newVariableTextField = new JXTextField("name of new variable");
			newVariableTextField.setColumns(12);
			chooseVariablePanel.add(newVariableTextField, gbc);
			newVariableTextField.setEnabled(true);

			newVariableTextField.getDocument().addDocumentListener(new 
					DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkIfNextButtonCanBeActivated();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkIfNextButtonCanBeActivated();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkIfNextButtonCanBeActivated();
				}

			});

			// window 1.1: type for new variable  - comboBox
			gbc.gridx = 2; 
			newVariableDataTypeBox = new JComboBox<String>();
			newVariableDataTypeBox.addItem("short text");
			newVariableDataTypeBox.addItem("long text");
			// LB.Note: not yet implemented
			//newVariableDataTypeBox.addItem("integer");
			//newVariableDataTypeBox.addItem("boolean");
			chooseVariablePanel.add(newVariableDataTypeBox, gbc);
			newVariableDataTypeBox.setEnabled(true);

			// window 1.1: choose variable to be overwritten: 
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel chooseVarOverwrite = new JLabel("variable to be overwritten: ", 
					JLabel.RIGHT);
			chooseVariablePanel.add(chooseVarOverwrite, gbc);

			// window 1.1: choose variable 2 - ComboBox
			gbc.gridx = 1; 
			String typeOverwrite = typeBox.getSelectedItem().toString();
			variableList = Dna.dna.db.getVariablesByTypes(typeOverwrite, 
					"short text", "long text");
			Object[] variableKeysOverwrite = variableList.keySet().toArray();
			String[] variablesOverwrite = new String[variableKeysOverwrite.
			                                         length];
			for (int i = 0; i < variableKeysOverwrite.length; i++) {
				variablesOverwrite[i] = variableKeysOverwrite[i].toString();
			}
			variableOverwriteBox = new JComboBox<String>(variablesOverwrite);
			chooseVariablePanel.add(variableOverwriteBox, gbc);
			variableOverwriteBox.setEnabled(false);

			// window 1.1: write border around chooseVariablePanel
			TitledBorder chooseVariablePanelBorder = BorderFactory.
					createTitledBorder("Choose variable to be recoded:");
			chooseVariablePanel.setBorder(chooseVariablePanelBorder);	

			/*
			 * Window 1.2: BOOLEAN VARIABLE AFFECTED?
			 */
			JPanel chooseBooleanPanel = new JPanel(new GridBagLayout());
			// window 1.1: write border around chooseBooleanPanel
			TitledBorder chooseBooleanPanelBorder = BorderFactory.
					createTitledBorder("Choose additional variable "
							+ "affected by recoding the above specified "
							+ "variable:");
			chooseBooleanPanel.setBorder(chooseBooleanPanelBorder);	

			// window 1.2: chose variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			JLabel chooseVar1 = new JLabel("choose variable:", JLabel.RIGHT);
			chooseBooleanPanel.add(chooseVar1, gbc);

			// window 1.2: choose variable - ComboBox
			gbc.gridx = 1; 
			String type1 = typeBox.getSelectedItem().toString();
			LinkedHashMap<String, String> variableList1 = Dna.dna.db.
					getVariablesByTypes(type1, "boolean", "integer");
			Object[] variableKeys1 = variableList1.keySet().toArray();
			String[] variables1 = new String[variableKeys1.length];
			for (int i = 0; i < variableKeys1.length; i++) {
				variables1[i] = variableKeys1[i].toString();
			}
			booleanBox = new JComboBox<String>(variables1);
			booleanBox.addItem("");
			booleanBox.setSelectedItem("");
			chooseBooleanPanel.add(booleanBox, gbc);

			booleanBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					if (booleanBox.getSelectedItem() == ""){
						createNewBoolean.setSelected(false);
						createNewBoolean.setEnabled(false);
						overwriteBoolean.setSelected(false);
						overwriteBoolean.setEnabled(false);
						newBooleanTextField.setText(null);
						newBooleanTextField.setEnabled(false);
						newBooleanDataTypeBox.setEnabled(false);
						booleanOverwriteBox.setSelectedItem("");
						booleanOverwriteBox.setEnabled(false);
					}else{
						createNewBoolean.setEnabled(true);
						overwriteBoolean.setEnabled(true);
					}
					checkIfNextButtonCanBeActivated();
				}
			});

			// window 1.2: new variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel newVariableLabel1 = new JLabel("new variable: ",
					JLabel.RIGHT);
			chooseBooleanPanel.add(newVariableLabel1, gbc);

			// window 1.2: create button group for overwrite vs. new
			ButtonGroup overNewBooButtonGroup = new ButtonGroup();

			// window 1.2: create new variable - radioButton
			gbc.gridx = 1;
			createNewBoolean = new JRadioButton("create new variable");
			chooseBooleanPanel.add(createNewBoolean, gbc);
			createNewBoolean.setSelected(false);
			createNewBoolean.setEnabled(false);
			overNewBooButtonGroup.add(createNewBoolean);	

			createNewBoolean.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (createNewBoolean.isSelected()){
						newBooleanTextField.setEnabled(true);
						newBooleanDataTypeBox.setEnabled(true);
						booleanOverwriteBox.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});	

			// window 1.2: overwrite existing variable - radioButton
			gbc.gridx = 2;
			overwriteBoolean = new JRadioButton("overwrite existing variable");
			overNewBooButtonGroup.add(overwriteBoolean);	
			overwriteBoolean.setEnabled(false);
			chooseBooleanPanel.add(overwriteBoolean, gbc);

			overwriteBoolean.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (overwriteBoolean.isSelected()){
						newBooleanTextField.setEnabled(false);
						newBooleanDataTypeBox.setEnabled(false);
						booleanOverwriteBox.setEnabled(true);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});

			// window 1.2: set variable name/type - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			JLabel setVariableLabel1 = new JLabel("set name of variable: ", 
					JLabel.RIGHT);
			chooseBooleanPanel.add(setVariableLabel1, gbc);

			// window 1.2: variable name - text field
			gbc.gridx = 1; 
			newBooleanTextField = new JXTextField("name of new variable");
			newBooleanTextField.setColumns(12);
			chooseBooleanPanel.add(newBooleanTextField, gbc);
			newBooleanTextField.setEnabled(true);
			newBooleanTextField.getDocument().addDocumentListener(new 
					DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkIfNextButtonCanBeActivated();
					updateVariableNames();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkIfNextButtonCanBeActivated();
					updateVariableNames();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkIfNextButtonCanBeActivated();
					updateVariableNames();
				}

			});

			// window 1.2: type for new variable  - comboBox
			gbc.gridx = 2; 
			newBooleanDataTypeBox = new JComboBox<String>();
			newBooleanDataTypeBox.addItem("integer");
			newBooleanDataTypeBox.addItem("boolean");
			chooseBooleanPanel.add(newBooleanDataTypeBox, gbc);
			newBooleanDataTypeBox.setEnabled(false);

			// window 1.2: set type/variable of overwrite-variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel chooseVarOverwrite1 = new JLabel("variable to be "
					+ "overwritten: ", JLabel.RIGHT);
			chooseBooleanPanel.add(chooseVarOverwrite1, gbc);

			// window 1.2: choose variable of overwrite-variable - ComboBox
			gbc.gridx = 1; 
			String typeOverwrite1 = typeBox.getSelectedItem().toString();
			HashMap<String, String> variableListBoolean = Dna.dna.db.
					getVariablesByTypes(typeOverwrite1, "boolean", "integer");
			Object[] variableKeysOverwriteBoolean = variableListBoolean.
					keySet().toArray();
			String[] variablesOverwriteBoolean = new 
					String[variableKeysOverwriteBoolean.length];
			for (int i = 0; i < variableKeysOverwriteBoolean.length; i++) {
				variablesOverwriteBoolean[i] = variableKeysOverwriteBoolean[i].
						toString();
			}
			booleanOverwriteBox = new JComboBox<String>(
					variablesOverwriteBoolean);
			chooseBooleanPanel.add(booleanOverwriteBox, gbc);
			booleanOverwriteBox.setEnabled(false);

			booleanOverwriteBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
					if (nextButton.isEnabled() == true){
						// only update Variables if they are valid
						updateVariableNames();
					}
				}
			});	

			/*
			 *  WINDOW 1.3: BUTTON PANEL
			 */
			// window 1.3: Button Panel Boolean
			JPanel buttonCard1Panel = new JPanel(new FlowLayout(
					FlowLayout.RIGHT));

			// window 1.3: cancel - button
			cancelIcon = new ImageIcon(getClass().getResource(
					"/icons/cancel.png"));
			JButton cancelButtonBoolean = new JButton("cancel", cancelIcon);
			buttonCard1Panel.add(cancelButtonBoolean);

			cancelButtonBoolean.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					window1.dispose();
				}
			});

			// window 1.3: next - button
			nextIcon = new ImageIcon(getClass().getResource(
					"/icons/resultset_next.png"));
			nextButton = new JButton("next", nextIcon);
			buttonCard1Panel.add(nextButton);
			nextButton.setEnabled(false);

			nextButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// task 1: update Names
					updateVariableNames();
					// task 2: close window1
					window1.dispose();
					// task 2: create new Panel
					new RecodeWindow();
					// task 3: add previous/next-buttons to panel
					window2ButtonGo = new JPanel(new FlowLayout(
							FlowLayout.RIGHT));
					window2ButtonNext = new JPanel(new FlowLayout(
							FlowLayout.RIGHT));
					if (booleanBox.getSelectedItem() == ""){
						window2ButtonGo.add(cancelButtonWindow2);
						window2ButtonGo.add(previousButtonWindow2);
						window2ButtonGo.add(goButtonWindow2);
						window2.add(window2ButtonGo, BorderLayout.SOUTH);
						//pack();
					}else{
						window2ButtonNext.add(cancelButtonWindow2);
						window2ButtonNext.add(previousButtonWindow2);
						window2ButtonNext.add(nextButtonWindow2);
						window2.add(window2ButtonNext, BorderLayout.SOUTH);
						//pack();
					}					
					// task 4: update ColumnHeader
					updateTableHeaderColumnNames(tableRecode, columnNames);
					// task 5: create data set
					createDataSet();
					// task 6: update meta-list for variable
					updateMetaListTable();
					// task 7: show the recode panel window
					window2.pack();
					window2.setLocationRelativeTo(null);
					window2.setVisible(true);	
				}
			});

			/*
			 * WINDOW 1.4: SET THE PANEL
			 */
			// window 1.4: 
			//window1 = new JPanel(new BorderLayout());
			window1 = new JDialog();
			window1.setTitle("Recode variables: choose affected variable(s)");
			window1.setModal(true);
			window1.setLayout(new BorderLayout());
			window1.add(chooseVariablePanel, BorderLayout.NORTH);
			window1.add(chooseBooleanPanel, BorderLayout.CENTER);
			window1.add(buttonCard1Panel, BorderLayout.SOUTH);
		}	
	}

	/*---------------------------------------------------------------------
	 *  WINDOW 2: DO THE ACTUAL RECODING
	 *--------------------------------------------------------------------*/

	class RecodeWindow extends JDialog {
		private static final long serialVersionUID = 1L;
		public RecodeWindow(){

			window2 = new JDialog();
			window2.setTitle("Recode variables");
			window2.setModal(true);
			window2.setLayout(new BorderLayout());

			/*
			 *  CARD 2.1: CREATE THE TABLE
			 */
			tableModel = new DefaultTableModel(columnNames, 0); 
			tableRecode = new JXTable(tableModel);					

			// window 2.1: get values in table => done w/createDataSet() under card2-nextButton	

			// window 2.1: decide which columns are editable
			tableRecode.getColumnExt(0).setEditable(false);
			tableRecode.getColumnExt(1).setEditable(false);
			tableRecode.getColumnExt(2).setEditable(true);

			// window 2.1: drag&drop from column 0 to 2
			tableRecode.setDragEnabled(true);
			tableRecode.setDropMode(DropMode.USE_SELECTION);
			//TS() creates my own TransferHandler
			tableRecode.setTransferHandler(new TS());

			// window 2.1: add vertical/horizontal Scroller
			JScrollPane scrollPaneRecode = new JScrollPane(tableRecode);
			scrollPaneRecode.setPreferredSize(new Dimension(700, 450));
			scrollPaneRecode.setVerticalScrollBarPolicy(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 

			// window 2.1: add table + tableHeader to panel
			JPanel tableRecodeAndHeaderPanel = new JPanel(new BorderLayout());
			tableRecodeAndHeaderPanel.add(tableRecode.getTableHeader(), 
					BorderLayout.NORTH);	
			tableRecodeAndHeaderPanel.add(scrollPaneRecode, BorderLayout.WEST);
			TitledBorder recodePanelBorder = BorderFactory.createTitledBorder(
					"recode from old variable to new variable");
			tableRecodeAndHeaderPanel.setBorder(recodePanelBorder);
			window2.add(tableRecodeAndHeaderPanel, BorderLayout.WEST);

			/*
			 * CARD 2.2. META-LIST PANEL
			 */
			// window 2.2: table
			tableMetaListModel = new DefaultTableModel(1, 1); 
			tableMetaList = new JXTable(tableMetaListModel);
			tableMetaList.getColumn(0).setMinWidth(260);
			//tableMetaList.setPreferredSize(new Dimension(400, 600));

			// window 2.2: get values in table => done in updateMetaListTable()

			// window 2.2: drag&drop (same as in recode-table)
			tableMetaList.setDragEnabled(true);
			tableMetaList.setDropMode(DropMode.USE_SELECTION);
			tableMetaList.setTransferHandler(new TS());

			// window 2.2: set selection mode
			tableMetaList.setRowSelectionAllowed(true);
			tableMetaList.setSelectionMode(ListSelectionModel.
					MULTIPLE_INTERVAL_SELECTION);

			// window 2.2: add vertical/horizontal Scroller
			JScrollPane scrollPaneMetaList = new JScrollPane (tableMetaList);
			scrollPaneMetaList.setVerticalScrollBarPolicy(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
			scrollPaneMetaList.setHorizontalScrollBarPolicy(
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			//scrollPaneMetaList.setPreferredSize(new Dimension(260, 500));

			// window 2.2: add table + tableHeader to panel
			JPanel tableMetaListPanel = new JPanel(new BorderLayout());
			TitledBorder metaListPanelBorder = BorderFactory.createTitledBorder(
					"set meta list entries for new variable");
			tableMetaListPanel.setBorder(metaListPanelBorder);
			//tableMetaListPanel.add(tableMetaList.getTableHeader(), BorderLayout.NORTH);	
			tableMetaListPanel.add(scrollPaneMetaList, BorderLayout.CENTER);

			/*
			 * CARD 2.3: META-LIST BUTTONS
			 */
			// window 2.3: save meta list - button
			ImageIcon saveIcon = new ImageIcon(getClass().getResource(
					"/icons/table_save.png"));
			JButton saveMetaList = new JButton(saveIcon);
			saveMetaList.setToolTipText("save changes made to the meta list");

			saveMetaList.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					saveMetaListEntries();
					String message = "Entries for the meta list have been saved."; 
					JOptionPane.showMessageDialog(new JFrame(), message, 
							"Information", JOptionPane.INFORMATION_MESSAGE);
				}
			});	

			// window 2.3: add meta list table row - button
			addIcon = new ImageIcon(getClass().getResource(
					"/icons/add.png"));
			JButton addRow = new JButton(addIcon);
			addRow.setToolTipText("add new entry to meta list");

			addRow.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tableMetaListModel.addRow(new Object[]{""});
				}
			});

			// window 2.3: delete meta list entry - button
			ImageIcon deleteIcon = new ImageIcon(getClass().getResource(
					"/icons/delete.png"));
			JButton deleteRow = new JButton(deleteIcon);
			deleteRow.setToolTipText("remove selected entry/ies");

			deleteRow.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// task 1: get ints of all selected items in table
					int[] allSelectedEntries = tableMetaList.getSelectedRows();
					// task 2: get all entries in meta list
					ArrayList<String> entries = new ArrayList<String>(Arrays.
							asList(Dna.dna.db.getEntriesFromVariableList(
									statementType, newVariableName)));
					// task 3: remove entries from meta list (Attention: sorting!)
					for (int i : allSelectedEntries){
						//convertRowIndexModel() fixes problem with sorting
						int row = (int) tableMetaList.convertRowIndexToModel(i);
						// task 1: get the label
						String label = (String) tableMetaListModel.getValueAt(
								row, 0);
						// task 2: delete entry from Meta list in database (if it is in there)
						if (label != "" && label != null && entries.contains(
								label) == true){Dna.dna.db.
							removeEntryFromVariableList(label, 
									statementType, newVariableName);
						}
					}
					// task 4: remove all rows from table ( + make sure sorting is not a problem)
					int res = 0;
					for(int i = 0; i < allSelectedEntries.length; i++) {
						res += (i>0)?(allSelectedEntries[i]-
								allSelectedEntries[i-1]-1):0;
						int index = tableMetaList.convertRowIndexToModel(
								allSelectedEntries[0]+res);
						tableMetaListModel.removeRow(index);
					}
				}
			});

			// window 2.3: import from old var - button
			ImageIcon importOldVarIcon = new ImageIcon(getClass().getResource(
					"/icons/arrow_turn_left_down.png"));
			JButton importFromOldVar = new JButton(importOldVarIcon);
			importFromOldVar.setToolTipText("import entries from old variable "
					+ "to meta list");

			importFromOldVar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// add table to JDialog
					JDialog openTableImport = new JDialog();
					openTableImport.setModal(true);
					// true = import from old var; false = import from new var
					Boolean importOldVariable = true;
					openTableForImport(openTableImport, importOldVariable);
					openTableImport.pack();
					openTableImport.setLocationRelativeTo(null);
					openTableImport.setVisible(true);	
				}
			});

			// window 2.3: import from new var - button
			ImageIcon importNewVarIcon = new ImageIcon(getClass().getResource(
					"/icons/arrow_turn_right_down.png"));
			JButton importFromNewVar = new JButton(importNewVarIcon);
			importFromNewVar.setToolTipText("import entries from new variable "
					+ "to meta list");

			importFromNewVar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// add table to JDialog
					JDialog openTableImport = new JDialog();
					openTableImport.setModal(true);
					// true = import from old var; false = import from new var
					//boolean importOldVariable => oben definieren??
					Boolean importOldVariable = false;
					openTableForImport(openTableImport, importOldVariable);
					openTableImport.pack();
					openTableImport.setLocationRelativeTo(null);
					openTableImport.setVisible(true);	
				}
			});

			// window 2.3: import external list - button
			ImageIcon importIcon = new ImageIcon(getClass().getResource(
					"/icons/table_add.png"));
			JButton loadList = new JButton(importIcon);
			loadList.setToolTipText("load .txt-file row by row and import "
					+ "selected entries into meta list");

			loadList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Panel für Import
					openTableImportFromFile = new JDialog();
					openTableImportFromFile.setModal(true);	
					JPanel importTableExternalFile = new JPanel(new BorderLayout());
					// Datei öffnen
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".txt")
									|| f.isDirectory();
						}
						public String getDescription() {
							return "Text file " +
									"(*.txt)";
						}
					});
					// show dialog window
					int returnVal = fc.showOpenDialog(Recode.this);
					if (returnVal == JFileChooser.CANCEL_OPTION) {
						openTableImportFromFile.dispose();
					}
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fc.getSelectedFile();	
						// Datei einlesen
						try{
							linesImported = getArrayListFromString(file);
						}
						catch(Exception en){
							en.printStackTrace();
						}

						// Tabelle bilden
						String[] columnNamesImportFromFile = new String[]
								{"add entry", file.getName()};
						tableModelImportFromFile = new 
								DefaultTableModel(columnNamesImportFromFile, 0);
						JXTable tableImportFromFile = new JXTable(
								tableModelImportFromFile);
						tableImportFromFile.getColumnModel().getColumn(0).
						setCellRenderer(tableImportFromFile.getDefaultRenderer(
								Boolean.class));
						tableImportFromFile.getColumn(0).setMaxWidth(62); 
						// make columns editable
						tableImportFromFile.getColumnModel().getColumn(0).
						setCellEditor(tableImportFromFile.
								getDefaultEditor(Boolean.class));
						tableImportFromFile.getColumnExt(1).setEditable(true);
						// add vertical/horizontal scroller for table
						JScrollPane scrollPaneImportPanel = new JScrollPane(
								tableImportFromFile);
						scrollPaneImportPanel.setVerticalScrollBarPolicy(
								JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
						scrollPaneImportPanel.setHorizontalScrollBarPolicy(
								JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);		

						importTableExternalFile.add(scrollPaneImportPanel, 
								BorderLayout.NORTH);	

						// Datensatz bilden
						boolean select = false;
						for (int i = 0; i < linesImported.size(); i++) {	
							Object[] dataImportFromFile = {select, 
									linesImported.get(i)};
							if (linesImported.get(i).startsWith("List of "
									+ "meta-entries for variable") || 
									linesImported.get(i).equals("")){
								//do not add to table
							}else{
								tableModelImportFromFile.addRow(dataImportFromFile);
							}
						}

						// button-panel
						JPanel buttonImportEntriesFromFilePanel = new JPanel(new 
								FlowLayout(FlowLayout.RIGHT));

						// select all - button
						JButton selectEntriesImportFromFile = new JButton(
								"select all");
						buttonImportEntriesFromFilePanel.add(
								selectEntriesImportFromFile);

						selectEntriesImportFromFile.addActionListener(
								new ActionListener(){
									public void actionPerformed(ActionEvent e){
										for (int i = 0; i < tableModelImportFromFile.
												getRowCount(); i++){
											tableModelImportFromFile.setValueAt(
													true, i, 0);
										}		
									}
								});

						// deselect all - button
						JButton unselectEntriesImportFromFile = new JButton(
								"deselect all");
						buttonImportEntriesFromFilePanel.add(
								unselectEntriesImportFromFile);

						unselectEntriesImportFromFile.addActionListener(
								new ActionListener(){
									public void actionPerformed(ActionEvent e){
										for (int i = 0; i < tableModelImportFromFile.
												getRowCount(); i++){
											tableModelImportFromFile.setValueAt(
													false, i, 0);
										}		
									}
								});

						// cancel - button
						JButton cancelImport = new JButton("cancel", cancelIcon);
						buttonImportEntriesFromFilePanel.add(cancelImport);

						cancelImport.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e){
								openTableImportFromFile.dispose();
							}
						});

						// add entries
						JButton addEntriesImportFromFile = new JButton(
								"add selected entries", addIcon);
						buttonImportEntriesFromFilePanel.add(
								addEntriesImportFromFile);

						addEntriesImportFromFile.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e){
								for (int i = 0; i < tableModelImportFromFile.
										getRowCount(); i++){
									if (tableModelImportFromFile.getValueAt(i, 0).
											equals(true)){//if the entry was chosen=true
										String newEntry = (String) 
												tableModelImportFromFile.
												getValueAt(i, 1).toString();
										String[] variableListEntries = Dna.dna.db.
												getEntriesFromVariableList(
														statementType, 
														newVariableName);		
										boolean validAdd = false;
										if (variableListEntries.length == 0){
											validAdd = false;
										}else{
											for (int j = 0; j < variableListEntries.
													length; j++) {
												if (variableListEntries[j].equals(
														newEntry)){
													validAdd = true;
												}
											}
										}
										if (validAdd == false) {
											Dna.dna.db.addEntryToVariableList(
													newEntry, statementType, 
													newVariableName);
											Object[] data = {newEntry};
											tableMetaListModel.addRow(data);
										} 
									}
								}
								openTableImportFromFile.dispose();
							}
						});

						// add to JDialog-Window
						openTableImportFromFile.add(importTableExternalFile, 
								BorderLayout.NORTH);
						openTableImportFromFile.add(buttonImportEntriesFromFilePanel, 
								BorderLayout.SOUTH);
						openTableImportFromFile.pack();
						openTableImportFromFile.setLocationRelativeTo(null);
						openTableImportFromFile.setVisible(true);
					}
				}
			});

			// window 2.3: export meta list - button
			ImageIcon exportIcon = new ImageIcon(getClass().getResource(
					"/icons/table_go.png"));
			JButton exportList = new JButton(exportIcon);
			exportList.setToolTipText("export meta list to .txt-file");

			exportList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String[] variableListEntries = Dna.dna.db.
							getEntriesFromVariableList(statementType, 
									newVariableName);
					String writeEntries = "List of meta-entries for variable '" + 
							newVariableName + "'\n\n";
					for (int i = 0; i < variableListEntries.length; i++){
						writeEntries = writeEntries + variableListEntries[i] + "\n";
					}
					// save a file
					// parent component of the dialog
					JDialog saveFilePanel = new JDialog();		 
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".txt")
									|| f.isDirectory();
						}
						public String getDescription() {
							return "Text file " +
									"(*.txt)";
						}
					});

					// open file-chooser dialog and specify path/file name
					fileChooser.setDialogTitle("Specify a file to save");   
					int userSelection = fileChooser.showSaveDialog(saveFilePanel);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						File fileToSave = fileChooser.getSelectedFile();
						String file_name = fileToSave.toString();
						if (!file_name.endsWith(".txt"))
							file_name += ".txt";
						try {
							FileWriter fw = new FileWriter(file_name);
							fw.write(writeEntries);
							fw.close();

						} catch (IOException iox) {
							iox.printStackTrace();
						}
					}
				}
			});

			// window 2.3: add buttons to panel 
			Box iconPanel = new Box(BoxLayout.Y_AXIS);
			iconPanel.add(saveMetaList);
			iconPanel.add(addRow);
			iconPanel.add(deleteRow);
			iconPanel.add(importFromOldVar);
			iconPanel.add(importFromNewVar);
			iconPanel.add(loadList);
			iconPanel.add(exportList);
			iconPanel.setVisible(true);		
			tableMetaListPanel.add(iconPanel, BorderLayout.EAST);

			// window 2.3: add meta-list-panel to card2 
			window2.add(tableMetaListPanel, BorderLayout.CENTER);

			/*
			 * CARD 2.4. BUTTONS-PANEL
			 */		
			// window 2.4: cancel - button
			cancelButtonWindow2 = new JButton("cancel", cancelIcon);

			cancelButtonWindow2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// task 1: save meta list entries
					saveMetaListEntries();
					// task 2: close or not?
					String message = "Would you like to close the recode window?\n"
							+ "All changes will be lost.";
					Object[] options ={"Yes, close window", "No, keep recoding"};
					int result = JOptionPane.showOptionDialog(new JFrame(), 
							message, "Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,  	
							options[0]); 
					switch (result) {
					case 0:	
						window2.dispose();
					case 1:
						break;
					}
				}
			});

			// window 2.4: previous - button
			previousIcon = new ImageIcon(getClass().getResource(
					"/icons/resultset_previous.png"));
			previousButtonWindow2 = new JButton("previous", previousIcon);

			previousButtonWindow2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// task 1: save meta list
					saveMetaListEntries();
					// task 2: want to go to previous window?
					String message = "Would you like to go to the previous"
							+ " window?\nAll changes you made in this window"
							+ " will be lost.";
					Object[] options = {"previous window",
					"cancel"};
					int result = JOptionPane.showOptionDialog(new JFrame(), 
							message, "Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,     		//do not use a custom Icon
							options,  		//the titles of buttons
							options[0]); 	//default button title
					switch (result) {
					case 0: // Goal: Want to return to SetVariables()-Window
						// task 1: remove the panel: buttonCard2Next and buttonCard2Go
						if (booleanBox.getSelectedItem() == ""){
							window2.remove(window2ButtonGo);
						}else{
							window2.remove(window2ButtonNext);	
						}
						//
						// task 2: save variable names => no need => just re-activate previous window
						/*
						String statTypPrev = statementType;
						String oldVarNamePrev = oldVariableName;
						boolean createNewVarPrev = createNewVariable.isSelected();
						String newVarNamePrev = newVariableName;
						String newVarDataTypePrev = newVariableDataType;
						String oldBooNamePrev = boolean1OldName;
						boolean createNewBooPrev = createNewVariable1.isSelected();
						String newBooNamePrev = boolean1NewName;
						String newBooDataTypePrev = boolean1NewDataType;
						// task 2: show first window
						new SetVariablesWindow();				
						// task 3: set variables in window 1 as they were
						typeBox.setSelectedItem(statTypPrev);
						variableBox.setSelectedItem(oldVarNamePrev);
						if (createNewVarPrev == true){
							createNewVariable.setSelected(true);
							newVariableTextField.setText(newVarNamePrev);
							newVariableTextField.setEnabled(true);
							newVariableType.setSelectedItem(newVarDataTypePrev);
							newVariableType.setEnabled(true);
							variableBoxOverwrite.setEnabled(false);
						}else{
							overwriteVariable.setSelected(true);
							variableBoxOverwrite.setSelectedItem(newVarNamePrev);
							variableBoxOverwrite.setEnabled(true);
							newVariableTextField.setEnabled(false);
							newVariableType.setEnabled(false);
						}
						if (oldBooNamePrev != ""){
							variableBox1.setSelectedItem(oldBooNamePrev);
							if (createNewBooPrev == true){
								createNewVariable1.setSelected(true);
								newVariableTextField1.setText(newBooNamePrev);
								newVariableTextField1.setEnabled(true);
								newVariableType1.setSelectedItem(newBooDataTypePrev);
								newVariableType1.setEnabled(true);
								variableBoxOverwrite1.setEnabled(false);
							}
							if (createNewBooPrev == false){
								overwriteVariable1.setSelected(true);
								variableBoxOverwrite1.setSelectedItem(newBooNamePrev);
								variableBoxOverwrite1.setEnabled(true);
								newVariableTextField1.setEnabled(false);
								newVariableType1.setEnabled(false);
							}
						}else{
							createNewVariable1.setSelected(false);
							overwriteVariable1.setSelected(false);
							createNewVariable1.setEnabled(false);
							overwriteVariable1.setEnabled(false);
							variableBoxOverwrite1.setEnabled(false);
							newVariableTextField1.setEnabled(false);
							newVariableType1.setEnabled(false);
						}	
						 */				
						//task 4: close window 2
						window2.dispose();
						// task 5: make window 1 visible
						window1.pack();
						window1.setLocationRelativeTo(null);
						window1.setVisible(true);
					case 1:
						break;
					}
				}
			});

			// window 2.4: go-recode - button
			goIcon = new ImageIcon(getClass().getResource(
					"/icons/accept.png"));
			goButtonWindow2 = new JButton("go", goIcon);

			goButtonWindow2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// task 1: save meta list
					saveMetaListEntries();
					// task 2: want to recode?
					String message = "Would you like to recode the variable(s) as "
							+ "specified?";
					Object[] options = {"recode", "cancel"};
					int result = JOptionPane.showOptionDialog(new JFrame(), 
							message, "Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,    	 //do not use a custom Icon
							options, 	 //the titles of buttons
							options[0]); //default button title
					switch (result) {
					case 0:
						// create new variable
						if (createNewVariable.isSelected() == true){
							Dna.dna.db.addVariable(newVariableName, 
									newVariableDataType, 
									statementType);
						}
						// get values from table
						/*
						 * LB.Note: the sorting of the row does not matter as 
						 * all three columns are sorted together + you go 
						 * through each row one by one.
						 */
						for (int i = 0; i < tableRecode.getRowCount(); i++) {
							String oldVarEntry = "";
							oldVarEntry = (String) tableRecode.getModel().
									getValueAt(i, 0); 
							String newVarEntry = "";
							newVarEntry = (String) tableRecode.getModel().
									getValueAt(i, 2);
							// get statement-ID-list where oldVarEntry is included
							ArrayList<Integer> statIDList = Dna.dna.db.
									getVariableEntryMatch(statementType, 
											oldVariableName, oldVarEntry);
							for (Integer ints: statIDList){
								Dna.dna.db.changeStatement(ints, newVariableName, 
										(String) newVarEntry, newVariableDataType);
							}
						}
						window2.dispose();
					case 1:
						break;
					}
				}
			});

			// window 2.4: next-button (if boolean is selected)
			nextButtonWindow2 = new JButton("next", nextIcon);

			nextButtonWindow2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {				
					// task 1: save meta list
					saveMetaListEntries();
					// task 2: show next window
					new BooleanWindow();
					// task 3: create data set
					createDataSetBoolean(tableModelBoolean, oldBooleanName);
					// task 4: update table header
					updateTableHeaderColumnNames(tableBoolean, 
							columnNamesBooleanPanel);
					// task 5: update border around Boolean1-Panel
					TitledBorder window3PanelBorder = BorderFactory.
							createTitledBorder(booleanPanelBorderName);
					tableBooleanPanel.setBorder(window3PanelBorder);
					// task 6: close this window
					window2.dispose();	
					// task 7: show new window
					window3.pack();
					window3.setLocationRelativeTo(null);
					window3.setVisible(true);

				}
			});
		}
	}

	/*---------------------------------------------------------------------
	 * CARD 3 - RECODE BOOLEAN PANEL #1
	 *--------------------------------------------------------------------*/

	class BooleanWindow {
		public BooleanWindow(){

			window3 = new JDialog();
			window3.setModal(true);
			window3.setTitle("Recode variables: recode additional variable");
			window3.setLayout(new BorderLayout());

			/*
			 * CARD 3.1: Create table
			 */	
			// window 3.1: table
			tableModelBoolean = new DefaultTableModel(columnNamesBooleanPanel, 0);
			tableBoolean = new JXTable(tableModelBoolean);	

			// window 3.1: get values in table => done w/createDataSetBoolean() under card2.3-nextButton

			// window 3.1: decide which columns are editable
			tableBoolean.getColumnExt(0).setEditable(false);
			tableBoolean.getColumnExt(1).setEditable(false);
			tableBoolean.getColumnExt(2).setEditable(false);
			tableBoolean.getColumnExt(3).setEditable(true);
			tableBoolean.getColumnExt(4).setEditable(false);

			// window 3.1: set column width of col 1, 2 and 3
			tableBoolean.getColumnModel().getColumn(1).setPreferredWidth(90);
			tableBoolean.getColumnModel().getColumn(2).setPreferredWidth(90);
			tableBoolean.getColumnModel().getColumn(3).setPreferredWidth(90);

			// window 3.1: add vertical/horizontal Scroller
			JScrollPane scrollPaneTableBoolean1 = new JScrollPane (tableBoolean);
			scrollPaneTableBoolean1.setPreferredSize(new Dimension(800, 520));
			scrollPaneTableBoolean1.setVerticalScrollBarPolicy(
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPaneTableBoolean1.setHorizontalScrollBarPolicy(
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

			// window 3.1: add table + tableHeader to panel
			tableBooleanPanel = new JPanel(new BorderLayout());
			tableBooleanPanel.add(tableBoolean.getTableHeader(), 
					BorderLayout.NORTH);	
			tableBooleanPanel.add(scrollPaneTableBoolean1, BorderLayout.WEST);

			window3.add(tableBooleanPanel, BorderLayout.NORTH);

			/*
			 * CARD 3.2: BUTTONS PANEL FOR BOOLEAN TABLE 1
			 */
			// window 3.2: cancel button
			JPanel buttonBooleanPanel =new JPanel(new FlowLayout(
					FlowLayout.RIGHT));

			cancelButtonWindow3 = new JButton("cancel", cancelIcon);
			buttonBooleanPanel.add(cancelButtonWindow3);

			cancelButtonWindow3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String message = "Would you like to close the recode window"
							+ "?\nChanges made will be lost.";
					Object[] options ={"Yes, close window", "No, keep recoding"};
					int result = JOptionPane.showOptionDialog(new JFrame(), 
							message, "Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,  	
							options[0]); 
					switch (result) {
					case 0:	
						window3.dispose();
					case 1:
						break;
					}
				}
			});

			// window 3.2: previous - button
			previousButtonWindow3 = new JButton("previous", previousIcon);
			buttonBooleanPanel.add(previousButtonWindow3);

			previousButtonWindow3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String message = "Would you like to go to the previous"
							+ " window?\nAll changes you made in this window"
							+ " will be lost.";
					Object[] options = {"previous window",
					"cancel"};
					int result = JOptionPane.showOptionDialog(new JFrame(),
							message, "Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,     //do not use a custom Icon
							options,  //the titles of buttons
							options[0]); //default button title
					switch (result) {
					case 0:	
						// task 1: close this panel
						window3.dispose();
						// task 2: show previous panel
						window2.pack();
						window2.setLocationRelativeTo(null);
						window2.setVisible(true);
					case 1:
						break;
					}
				}
			});

			// window 3.2: go - button
			goButtonWindow3 = new JButton("go", goIcon);
			buttonBooleanPanel.add(goButtonWindow3);

			goButtonWindow3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String message = "Would you like to recode the variable(s) "
							+ "as specified?";
					Object[] options = {"recode", "cancel"};
					int result = JOptionPane.showOptionDialog(new JFrame(), 
							message, "Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, 
							options[0]);
					switch (result) {
					case 0:
						boolean breakNow = false;
						// task 1: create new variable
						if (createNewVariable.isSelected() == true){
							Dna.dna.db.addVariable(newVariableName, 
									newVariableDataType, 
									statementType);
						}
						if (createNewBoolean.isSelected() == true){
							Dna.dna.db.addVariable(newBooleanTextField.getText(), 
									newBooleanDataType, 
									statementType);
						}
						// task 2: recode boolean/int variables
						breakNow = recodeBooleanVariables(tableBoolean, 
								tableModelBoolean, oldBooleanName, 
								newBooleanName, breakNow);
						// do task 3 and 4 only if column 4 entries are integers:
						// task 3: recode chosen variable
						if (breakNow == false){
							for (int i = 0; i < tableRecode.getRowCount(); i++) {
								String oldVarEntry = "";
								oldVarEntry = (String) tableRecode.getModel().
										getValueAt(i, 0); //
								String newVarEntry = "";
								newVarEntry = (String) tableRecode.getModel().
										getValueAt(i, 2);
								// get statement-ID-list where oldVarEntry is included
								ArrayList<Integer> statIDList = Dna.dna.db.
										getVariableEntryMatch(statementType, 
												oldVariableName, oldVarEntry);
								for (Integer ints: statIDList){
									Dna.dna.db.changeStatement(ints, 
											newVariableName, (String) newVarEntry,
											oldVariableDataType);
								}
							}
							// task 4: close window3
							window3.dispose();
						}
					case 1:
						break;
					}
				}
			});

			// "Recode additional variables"
			window3.add(tableBooleanPanel, BorderLayout.NORTH);
			window3.add(buttonBooleanPanel, BorderLayout.SOUTH);
		}
	}

	//-------------------------------------------------------------------------	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------	

	/**
	 * remove one or multiply-selected row(s) from table
	 * 
	 * @param table			Table from which selected items are removed
	 */
	public void removeSelectedFromTable(JTable table) {

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int indexes[] = table.getSelectedRows(); 
		int res = 0;
		for(int i = 0; i < indexes.length; i++) {
			res += (i>0)?(indexes[i]-indexes[i-1]-1):0;
			int index = table.convertRowIndexToModel(indexes[0]+res);
			model.removeRow(index);
		}
	}

	/**
	 *  Update variable names after some action was taken.
	 */
	public void updateVariableNames(){			
		// old variable
		statementType = (String) typeBox.getSelectedItem().toString();		
		oldVariableName = (String) variableBox.getSelectedItem().toString();
		oldVariableDataType = (String) Dna.dna.db.getDataType(oldVariableName, 
				statementType);
		oldVariableColumnName = (String) "<html>old variable:<br>" + 
				oldVariableName + "</html";
		// if overwrite/newVar is selected:
		if (overwriteVariable.isSelected() == true){
			newVariableName = (String) variableOverwriteBox.
					getSelectedItem().toString();
			newVariableColumnName = (String) "<html>new variable:<br>" + 
					newVariableName +"</html";
			newVariableDataType = Dna.dna.db.getDataType(newVariableName, 
					statementType);
		}
		if (createNewVariable.isSelected() == true){
			newVariableName = (String) newVariableTextField.getText().
					toString();
			newVariableColumnName = (String) "<html>new variable:<br>" + 
					newVariableName +"</html";
			newVariableDataType = newVariableDataTypeBox.getSelectedItem().
					toString(); 
		}	
		// columnNames
		columnNames = new String[]{oldVariableColumnName, 
				"<html>number of<br>occurences</html>", newVariableColumnName};
		// columnNamesBooleanPanel1
		oldBooleanName = booleanBox.getSelectedItem().toString();
		if (booleanBox.getSelectedItem() != ""){
			booleanDataType = Dna.dna.db.getDataType(oldBooleanName, 
					statementType);
			oldBooleanColumnName = (String) "<html>old variable:<br>" + 
					oldBooleanName +"</html";
			if (overwriteBoolean.isSelected() == true){
				newBooleanName = booleanOverwriteBox.getSelectedItem().
						toString();
				newBooleanDataType = booleanDataType;
				newBooleanColumnName = (String) "<html>new variable:<br>" + 
						newBooleanName + "</html";
				booleanPanelBorderName = (String) "<html>Recode additional "
						+ "variable: " + newBooleanName + "</html>";
			}
			if (createNewBoolean.isSelected() == true){
				newBooleanName = newBooleanTextField.getText().toString();
				newBooleanDataType = newBooleanDataTypeBox.getSelectedItem().
						toString();
				newBooleanColumnName = (String) "<html>new variable:<br>" + 
						newBooleanName + "</html";
				booleanPanelBorderName = (String) "<html>Recode additional "
						+ "variable: " + newBooleanName + "</html>";
			}
			// oldVar, nr occur, boolean_old, boolean_new, newVar
			columnNamesBooleanPanel = new String[]{oldVariableColumnName, 
					"<html>number of<br>occurences</html>", 
					oldBooleanColumnName, 
					newBooleanColumnName, newVariableColumnName};
		}
	}

	/**
	 * Force JTable's header columns names update from its own model.
	 * 
	 * @param table				Name of table
	 * @param columnNames		String[] with column names
	 */
	public static void updateTableHeaderColumnNames(JXTable table, String[] 
			columnNames) {
		for (int i = 0; i < columnNames.length; i++){
			table.getColumnModel().getColumn(i).setHeaderValue(columnNames[i]);
		}
		table.getTableHeader().resizeAndRepaint();
	}

	/**
	 * Create initial data set for recode-table.
	 * 
	 */
	public void createDataSet(){
		// get values in table: 
		String[] oldVarEntriesString = new String[Dna.dna.db.getVariableStringEntries(
				oldVariableName, statementType).length];
		oldVarEntriesString =  Dna.dna.db.getVariableStringEntries(
				oldVariableName, statementType);
		// get list with ALL entries (including duplicates) for a certain variable
		List<String> oldVarAllEntriesStringList = Arrays.asList(Dna.dna.db.
				getAllVariableStringEntries(oldVariableName, statementType));
		//remove all rows from data[]
		if (tableModel.getRowCount() > 0) {
			for (int i = tableModel.getRowCount() - 1; i > -1; i--) {
				tableModel.removeRow(i);
			}
		}
		//prepare data:
		if (overwriteVariable.isSelected() == true){
			String newVar = null;
			for (int i = 0; i < oldVarEntriesString.length; i++){
				String oldVar = oldVarEntriesString[i];
				int nrOccurence = Collections.frequency(
						oldVarAllEntriesStringList, oldVar);
				// get values to put in "newVar" (newVar = overwriteVar)
				ArrayList<Integer> statIDList = Dna.dna.db.
						getVariableEntryMatch(statementType, oldVariableName, 
								oldVar);
				String firstEntryNewVar = (String) Dna.dna.db.
						getVariableStringEntryWithType(statIDList.get(0), 
								newVariableName, statementType);
				if (statIDList.size() == 1){
					newVar = firstEntryNewVar;
				}else{
					for (Integer ints: statIDList){
						String entryNewVar = (String) Dna.dna.db.
								getVariableStringEntryWithType(ints,
										newVariableName, 
										statementType);
						if (firstEntryNewVar.equals(entryNewVar)){
							newVar = firstEntryNewVar;
						}else{
							newVar = "";
							break;	
						}
					}
				}
				// create data set
				Object[] data = {oldVar, nrOccurence, newVar};
				tableModel.addRow(data);
			}	
		}else{
			for (int i = 0; i < oldVarEntriesString.length; i++){
				String oldVar = oldVarEntriesString[i];
				//get frequency of string oldvar occuring in oldVarALLEntriesStringList
				int nrOccurence = Collections.frequency(
						oldVarAllEntriesStringList, oldVar);
				String newVar = "";
				Object[] data = {oldVar, nrOccurence, newVar};
				tableModel.addRow(data);
			}
		}
	}

	/**
	 * Update the Variable-Meta-List (listModel).
	 */
	public void updateMetaListTable() {
		// task 1: update table data
		if (tableMetaListModel.getRowCount() > 0) {
			for (int i = tableMetaListModel.getRowCount() - 1; i > -1; i--) {
				tableMetaListModel.removeRow(i);
			}
		}
		String entries[] = Dna.dna.db.getEntriesFromVariableList(
				statementType, newVariableName);
		for (int i = 0; i < entries.length; i++){
			Object[] data = {entries[i]};
			tableMetaListModel.addRow(data);
		}
		// task 2: update table header
		tableMetaList.getColumnModel().getColumn(0).setHeaderValue(
				newVariableName);
		tableMetaList.getTableHeader().resizeAndRepaint();
	}

	/**
	 * Open JDialog with Table. Column 1: boolean checkbox whether entry should
	 * be imported or not. Column 2: String entry to be imported to meta-list.
	 * 
	 * @param openTableImport		JDialog-panel
	 * @param importOldVariable		boolean; true if import comes from old variable entries
	 */
	public void openTableForImport(final JDialog openTableImport, 
			final Boolean importOldVariable) {
		JPanel importTable = new JPanel(new BorderLayout());

		String[] columnNamesImport = null;
		// create JTable
		if (importOldVariable == false){
			columnNamesImport = new String[]{"add entry", newVariableColumnName};
		}else{
			columnNamesImport = new String[]{"add entry", oldVariableColumnName};
		}
		final DefaultTableModel tableModelImport = new DefaultTableModel(
				columnNamesImport, 0);
		JXTable tableImport = new JXTable(tableModelImport);
		tableImport.getColumnModel().getColumn(0).setCellRenderer(tableImport.
				getDefaultRenderer(Boolean.class));
		tableImport.getColumn(0).setMaxWidth(62); 

		// make columns editable
		tableImport.getColumnModel().getColumn(0).setCellEditor(tableImport.
				getDefaultEditor(Boolean.class));
		tableImport.getColumnExt(1).setEditable(true);

		// add vertical/horizontal scroller for table
		JScrollPane scrollPaneImportPanel = new JScrollPane (tableImport);
		//scrollPaneImportPanel.setPreferredSize(new Dimension(400, 100));
		scrollPaneImportPanel.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneImportPanel.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);		

		importTable.add(scrollPaneImportPanel, BorderLayout.NORTH);	

		// create variables and data
		boolean select = false;
		for (int i = 0; i < tableRecode.getRowCount(); i++) {
			String oldVarEntryImport = "";
			oldVarEntryImport = (String) tableRecode.getModel().
					getValueAt(i, 0).toString(); 
			String newVarEntryImport = "";
			newVarEntryImport = (String) tableRecode.getModel().
					getValueAt(i, 2).toString();
			Object[] dataImportOldVar = {select, oldVarEntryImport};
			Object[] dataImportNewVar = {select, newVarEntryImport};
			if (importOldVariable == false){
				tableModelImport.addRow(dataImportNewVar);
			}else{
				tableModelImport.addRow(dataImportOldVar);
			}
		}

		// button-panel 
		JPanel buttonImportEntriesPanel = new JPanel(new FlowLayout(
				FlowLayout.RIGHT));

		// select all - button
		JButton selectEntriesImport = new JButton("select all");
		buttonImportEntriesPanel.add(selectEntriesImport);

		selectEntriesImport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (int i = 0; i < tableModelImport.getRowCount(); i++){
					tableModelImport.setValueAt(true, i, 0);
				}		
			}
		});

		// deselect all - button
		JButton unselectEntriesImport = new JButton("deselect all");
		buttonImportEntriesPanel.add(unselectEntriesImport);

		unselectEntriesImport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (int i = 0; i < tableModelImport.getRowCount(); i++){
					tableModelImport.setValueAt(false, i, 0);
				}		
			}
		});

		// cancel - button
		JButton cancelImport = new JButton("cancel", cancelIcon);
		buttonImportEntriesPanel.add(cancelImport);

		cancelImport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				openTableImport.dispose();
			}
		});

		// add entry - button
		JButton addEntriesImport = new JButton("add selected entries", addIcon);
		buttonImportEntriesPanel.add(addEntriesImport);

		addEntriesImport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (int i = 0; i < tableModelImport.getRowCount(); i++){
					if (tableModelImport.getValueAt(i, 0).equals(true)){
						String newEntry = (String) tableModelImport.
								getValueAt(i, 1).toString();
						String[] variableListEntries = Dna.dna.db.
								getEntriesFromVariableList(statementType, 
										newVariableName);
						boolean validAdd = false;
						if (variableListEntries.length == 0){
							validAdd = false;
						}else{
							for (int j = 0; j < variableListEntries.length; j++){
								if (variableListEntries[j].equals(newEntry)){
									validAdd = true;
								}
							}
						}
						if (validAdd == false) {
							Dna.dna.db.addEntryToVariableList(newEntry,
									statementType, 
									newVariableName);
							Object[] data = {newEntry};
							tableMetaListModel.addRow(data);
						} 
					}
				}
				openTableImport.dispose();
			}
		});

		// add table to panel
		openTableImport.add(importTable, BorderLayout.NORTH);
		openTableImport.add(buttonImportEntriesPanel, BorderLayout.SOUTH);
	}

	/**
	 * Check if chosen variable is correctly specified.
	 * 
	 * @param newVarSelected		Create new variable-RadioButton
	 * @param overwriteVarSelected	Overwrite old variable-RadioButton
	 * @param newVarSetText			Name of new variable
	 * @return						Boolean; true if selected correctly
	 */
	public boolean variableSelectedRight(JRadioButton newVarSelected,
			JRadioButton overwriteVarSelected, JXTextField newVarSetText, 
			JComboBox<String> variableBoxOverwrite){
		Boolean correct = false; 
		// step 1: 
		if (newVarSelected.isSelected() == true && newVarSetText.getText().
				length() != 0){
			correct = true;
		}
		// step 2:
		if (overwriteVarSelected.isSelected() == true && 
				variableBoxOverwrite.getSelectedIndex() != -1){ 
			correct = true;
		}
		return correct;
	}

	/**
	 * Check if new Variable name is written correctly (no spaces etc.).
	 * 
	 * @return		Boolean; true if written correctly	
	 */
	public boolean newVariableNameWrittenRight(){
		Boolean correct = false;
		if (createNewVariable.isSelected() == false){
			correct = true;
		}else{
			String var = newVariableTextField.getText().toString();
			boolean validAdd = false;
			// do not allow spaces in variable names
			if (var.equals("") || var.matches(".*\\s+.*")) {
				validAdd = false;
			} else {
				validAdd = true;
			}
			// do not allow duplicate variable names			
			boolean validApply = false;
			LinkedHashMap<String, String> map = Dna.dna.db.getVariables(
					statementType);
			Object[] types = map.keySet().toArray();
			if (Arrays.asList(types).contains(var)){
				validApply = false;
			}else{
				validApply = true;
			}
			if (validApply == true && validAdd == true){
				correct = true;	
			}else{
				correct = false;
			}
		}		
		return correct; 
	}

	/**
	 * Check if new boolean Variable name is written correctly (no spaces etc.).
	 * 
	 * @param newVarSelected			Create new Variable-RadioButton
	 * @param newVarSetText				Name of new variable
	 * @param statementType				Statement type of variable	
	 * @return							Boolean; true if selected correctly
	 */
	public boolean newBooleanVariableNameWrittenRight(JRadioButton 
			newVarSelected, JXTextField newVarSetText, String statementType){
		Boolean correct = false;
		if (newVarSelected.isSelected() == false){
			correct = true;
		}else{
			String var = newVarSetText.getText();
			boolean validAdd = false;
			// do not allow spaces in variable names
			if (var.equals("") || var.matches(".*\\s+.*")) {
				validAdd = false;
			} else {
				validAdd = true;
			}
			// do not allow duplicate variable names			
			boolean validApply = false;
			boolean validApply2 = false;
			LinkedHashMap<String, String> map = Dna.dna.db.getVariables(
					statementType);
			Object[] types = map.keySet().toArray();			
			if (Arrays.asList(types).contains(var)){
				validApply = false;
			}else{
				validApply = true;
			}
			if (createNewVariable.isSelected() == true && 
					newVariableTextField.getText().equals(var)){
				validApply2 = false;
			}else{
				validApply2 = true;
			}
			if (validApply == true && validAdd == true && validApply2 == true){
				correct = true;	
			}else{
				correct = false;
			}
		}		
		return correct; 
	}

	/**
	 * Check if boolean variable is selected correctly.
	 * 
	 * @return		Boolean; true if selected correctly
	 */
	public boolean booleanSelectedRight(){
		Boolean correct = false;
		// Is boolean-variable set?
		if (booleanBox.getSelectedItem() == ""){
			correct = true;
		}else{
			// see if everything selected/entered right
			if (variableSelectedRight(createNewBoolean, 
					overwriteBoolean, newBooleanTextField, 
					booleanOverwriteBox) == true &&
					newBooleanVariableNameWrittenRight(
							createNewBoolean, 
							newBooleanTextField, 
							statementType) == true){
				correct = true;
			}else{
				correct = false;
			}
			if (createNewBoolean.isSelected() == false && 
					overwriteBoolean.isSelected() == false){
				correct = false;
			}
		}
		return correct;
	}


	/**
	 *  Check if NextButton can be activated: three things need to be true for 
	 *  button to be activated:
	 *  	1) variable needs to be selected right
	 * 	 	2) new variable name needs to be written right
	 *  	3) boolean variables need to be selected right.
	 */
	public void checkIfNextButtonCanBeActivated(){
		// can next-Button be activated?
		boolean a = (boolean) variableSelectedRight(createNewVariable, 
				overwriteVariable, newVariableTextField, variableOverwriteBox);
		boolean b = (boolean) newVariableNameWrittenRight();
		boolean c = (boolean) booleanSelectedRight();
		if (a == true && b == true && c == true){
			nextButton.setEnabled(true);
		}else{
			nextButton.setEnabled(false);
		}
	}

	/**
	 * Read in file one line at a time.
	 * 
	 * @param f							File
	 * @return							list with read-in lines (one by one)
	 * @throws FileNotFoundException	
	 */
	public static ArrayList<String> getArrayListFromString(File f) 
			throws FileNotFoundException {
		ArrayList<String> list = new ArrayList<String>();
		try {
			BufferedReader input = new BufferedReader(new FileReader(f));
			String row = input.readLine();
			while (row != null){
				list.add(row);
				row = input.readLine();
			}
			input.close();
		} catch (IOException e) {
			String message = "Error while reading the file."; 
			JOptionPane.showMessageDialog(new JFrame(), message, "Warning",
					JOptionPane.ERROR_MESSAGE);
		}	
		return list;
	}

	/**
	 * Create data set to put in boolean-table.
	 * 
	 * @param tableModelBoolean		Name of table model used
	 * @param booleanOldName		Name of boolean variable to be recoded
	 */
	public void createDataSetBoolean(DefaultTableModel tableModelBoolean, 
			String booleanOldName){
		// task 1: remove all rows from data[]
		if (tableModelBoolean.getRowCount() > 0) {
			for (int i = tableModelBoolean.getRowCount() - 1; i > -1; i--) {
				tableModelBoolean.removeRow(i);
			}
		}
		// task 3: get boolean entries
		int[] booleanEntriesInt = (int[]) Dna.dna.db.getVariableIntEntries(
				booleanOldName, statementType);

		// task 3: create the data set
		for (int i = 0; i < tableModel.getRowCount(); i++){
			// old and new-var entry
			String oldVarEntry = "";
			oldVarEntry = (String) tableRecode.getModel().getValueAt(i, 0); 
			String newVarEntry = "";
			newVarEntry = (String) tableRecode.getModel().getValueAt(i, 2);	
			// boolean entries: 
			for (int j = 0; j < booleanEntriesInt.length; j++){
				// how often does entry (with boolean == j) occur?
				List<String> oldVarEntriesListWithValue = Arrays.asList(Dna.dna.
						db.getAllVariableStringEntriesWithGivenValue(oldVariableName, 
								statementType, booleanOldName, 
								booleanEntriesInt[j]));
				int nrOccurence = Collections.frequency(
						oldVarEntriesListWithValue, oldVarEntry);	
				Object[] data = {oldVarEntry, nrOccurence, booleanEntriesInt[j],
						booleanEntriesInt[j] , newVarEntry};
				tableModelBoolean.addRow(data);
			}
		}
	}

	/**
	 * Do the actual recoding of the boolean variable according to what is 
	 * written in booleanTable#.
	 * 
	 * @param tableBooleanN			Name of table
	 * @param tableModelBooleanN	Name of table model
	 * @param booleanNOldName		Name of boolean variable to be recoded
	 * @param booleanNNewName		Name of new boolean variable
	 * @param breakNow				Parameter; 1 if problem in column #4
	 * @return						Integer, 0 if all entries are numerical, 1 if otherwise
	 */
	public boolean recodeBooleanVariables(JXTable tableBooleanN, 
			DefaultTableModel tableModelBooleanN, String booleanNOldName, 
			String booleanNNewName, boolean breakNow){
		for (int i = 0; i < tableBooleanN.getRowCount(); i++) {
			String oldVarEntry = "";
			oldVarEntry = (String) tableModelBooleanN.getValueAt(i, 0);
			int booleanOldEntry = -1;
			booleanOldEntry = Integer.parseInt(tableModelBooleanN.
					getValueAt(i, 2).toString());
			int booleanNewEntry = -1;
			try{
				booleanNewEntry = Integer.parseInt(tableModelBooleanN.
						getValueAt(i, 3).toString());
			}catch(java.lang.NumberFormatException nfe){
				breakNow = true;
				String message = "The entries in column 4 are not all "
						+ "integers. Please check your entries."; 
				JOptionPane.showMessageDialog(new JFrame(), message, "Warning",
						JOptionPane.ERROR_MESSAGE);
			}
			ArrayList<Integer> listIDs;
			listIDs = Dna.dna.db.getStatementIdsWithVarAndValue(
					statementType, oldVariableName, oldVarEntry, 
					booleanNOldName, booleanOldEntry);					
			for (Integer ints: listIDs){
				Dna.dna.db.changeStatement(ints, booleanNNewName, 
						(int) booleanNewEntry, statementType);
			}
		}
		return breakNow;
	}

	public void saveMetaListEntries(){
		ArrayList<String> entries = new ArrayList<String>(Arrays.asList(
				Dna.dna.db.getEntriesFromVariableList(statementType, 
						newVariableName)));
		for (int i = 0; i < tableMetaListModel.getRowCount(); i++){
			String entry = (String) tableMetaListModel.getValueAt(i, 0);		
			if (entry!= "" && entry != null && entries.contains(entry) == false){
				Dna.dna.db.addEntryToVariableList(entry,
						statementType, newVariableName);
			}
		}
	}
}