package dna;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
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
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTextField;

public class Recode extends JDialog {
	private static final long serialVersionUID = 1L;

	CardLayout cl;
	JPanel cards, chooseBooleanPanel, buttonCard2Next, buttonCard3Next, 
	buttonCard4Next, buttonCard5Next, buttonCard6Next, buttonCard2Go, 
	buttonCard3Go, buttonCard4Go, buttonCard5Go, buttonCard6Go, 
	card2Panel, card3Panel, card4Panel, card5Panel, card6Panel, card7Panel,
	metaListPanel;
	JDialog openTableImportFromFile;
	GridBagConstraints gbc;
	int booleanCounter = 0;
	JComboBox<String> typeBox, variableBox, newVariableType, 
	variableBoxOverwrite, newVariableType1, newVariableType2, newVariableType3,
	newVariableType4, newVariableType5, variableBoxOverwrite1, 
	variableBoxOverwrite2, variableBoxOverwrite3, variableBoxOverwrite4,
	variableBoxOverwrite5, variableBox1, variableBox2, variableBox3, 
	variableBox4, variableBox5;
	JList<String> variableEntryList;
	JRadioButton createNewVariable, overwriteVariable, booleanAffectedButton, 
	createNewVariable1, createNewVariable2, createNewVariable3, 
	createNewVariable4, createNewVariable5, overwriteVariable1, 
	overwriteVariable2, overwriteVariable3, overwriteVariable4,
	overwriteVariable5;
	JXTextField newVariableTextField, newVariableEntry, newVariableTextField1,
	newVariableTextField2, newVariableTextField3, newVariableTextField4,
	newVariableTextField5;
	JButton addVariableButton, removeVariableButton, nextButton, 
	cancelButtonRecode, previousButtonRecode, goButtonRecode, nextButtonRecode, 
	cancelButtonBoolean1, previousButtonBoolean1, goButtonBoolean1, 
	nextButtonBoolean1, cancelButtonBoolean2, previousButtonBoolean2, 
	goButtonBoolean2, nextButtonBoolean2, cancelButtonBoolean3, 
	previousButtonBoolean3, goButtonBoolean3, nextButtonBoolean3, 
	cancelButtonBoolean4, previousButtonBoolean4, goButtonBoolean4, 
	nextButtonBoolean4, cancelButtonBoolean5, previousButtonBoolean5, 
	goButtonBoolean5, nextButtonBoolean5, okButtonListEntry;
	String statementType, oldVariableName, oldVarDataType, oldVarColumnName,
	newVariableName, newVarColumnName,
	newVariableDataType, borderName, boolean1OldName, boolean2OldName,
	boolean3OldName, boolean4OldName, boolean5OldName, boolean1NewName,
	boolean2NewName, boolean3NewName, boolean4NewName, boolean5NewName,
	boolean1DataType, boolean2DataType, boolean3DataType, boolean4DataType, 
	boolean5DataType, boolean1NewColumnName, boolean1OldColumnName, 
	boolean2OldColumnName, boolean2NewColumnName, boolean3OldColumnName,
	boolean3NewColumnName, boolean4OldColumnName, boolean4NewColumnName, 
	boolean5OldColumnName, boolean5NewColumnName, borderNameBoolean1, 
	borderNameBoolean2, borderNameBoolean3, borderNameBoolean4, 
	borderNameBoolean5, boolean1NewDataType, boolean2NewDataType, 
	boolean3NewDataType, boolean4NewDataType, boolean5NewDataType;	
	ImageIcon cancelIcon, nextIcon, addIcon;
	DefaultTableModel tableModel, tableModelImportFromFile, tableModelBoolean1, 
	tableModelBoolean2, tableModelBoolean3, tableModelBoolean4, 
	tableModelBoolean5; 
	JXTable tableRecode, tableBoolean1, tableBoolean2, tableBoolean3, 
	tableBoolean4, tableBoolean5; 
	DefaultListModel<String> listModel;
	File file;
	ArrayList<String> linesImported;
	public static String[] columnNames = new String[3];
	public static String[] columnNamesBooleanPanel1 = new String[5];
	public static String[] columnNamesBooleanPanel2 = new String[5];
	public static String[] columnNamesBooleanPanel3 = new String[5];
	public static String[] columnNamesBooleanPanel4 = new String[5];
	public static String[] columnNamesBooleanPanel5 = new String[5];

	public Recode() {

		this.setTitle("Recode variables");
		this.setModal(true);
		ImageIcon networkIcon = new ImageIcon(getClass().getResource(
				"/icons/pencil.png"));
		this.setIconImage(networkIcon.getImage());
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		cl = new CardLayout();
		cards = new JPanel(cl);

		/*---------------------------------------------------------------------
		 * CARD 1
		 *--------------------------------------------------------------------*/

		/*
		 *  CARD 1.1: SELECT VARIABLE
		 */
		JPanel chooseVariablePanel = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(1, 0, 1, 5);

		// card 1.1: chose variable - label
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		JLabel chooseVar = new JLabel("choose variable: ", JLabel.RIGHT);
		chooseVariablePanel.add(chooseVar, gbc);

		// card 1.1: choose variable type - ComboBox
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
				variableBoxOverwrite.removeAllItems();
				String type = (String) typeBox.getSelectedItem();
				if (type != null && !type.equals("")) {
					HashMap<String, String> variables = Dna.dna.db.
							getVariablesByTypes(type, "short text", "long text");
					Iterator<String> keyIterator = variables.keySet().iterator();
					while (keyIterator.hasNext()){
						String key = keyIterator.next();
						variableBox.addItem(key);
						variableBoxOverwrite.addItem(key);
					}
					variableBox.setSelectedIndex(0);
					variableBoxOverwrite.setSelectedIndex(0);
				}
				// if you change type: set boolean counter to false and remove 
				// all boolean variables
				booleanAffectedButton.setSelected(false);
				booleanCounter = 0;
				int maxComponentCount = (int) chooseBooleanPanel.
						getComponentCount();
				for (int i = 3; i < maxComponentCount; i++){
					int countCompartments = (int) chooseBooleanPanel.
							getComponentCount()-1;
					chooseBooleanPanel.remove(countCompartments);
				}
				addVariableButton.setEnabled(false);
				removeVariableButton.setEnabled(false);
				pack();
			}
		});

		// card 1.1: choose variable - ComboBox
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

		// card 1.1: new variable - label
		gbc.gridy++;
		gbc.gridx = 0;
		JLabel newVariableLabel = new JLabel("new variable: ", JLabel.RIGHT);
		chooseVariablePanel.add(newVariableLabel, gbc);

		// card 1.1: create new variable - radioButton
		gbc.gridx = 1;
		createNewVariable = new JRadioButton("create new variable");
		chooseVariablePanel.add(createNewVariable, gbc);

		createNewVariable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				if (createNewVariable.isSelected()){
					overwriteVariable.setEnabled(false);
					newVariableTextField.setEnabled(true);
					newVariableType.setEnabled(true);
				}else{
					overwriteVariable.setEnabled(true);
					newVariableTextField.setEnabled(false);
					newVariableType.setEnabled(false);
				}
				checkIfNextButtonCanBeActivated();
			}			
		});	

		// card 1.1: overwrite existing variable - radioButton
		gbc.gridx = 2;
		overwriteVariable = new JRadioButton("overwrite existing variable");
		chooseVariablePanel.add(overwriteVariable, gbc);

		overwriteVariable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				if (overwriteVariable.isSelected()){
					createNewVariable.setEnabled(false);
					variableBoxOverwrite.setEnabled(true);
				}else{
					createNewVariable.setEnabled(true);
					variableBoxOverwrite.setEnabled(false);
				}
				checkIfNextButtonCanBeActivated();
			}			
		});

		// card 1.1: set variable name/type - label
		gbc.gridx = 0; 
		gbc.gridy++; 
		JLabel setVariableLabel = new JLabel("set name/type of variable: ", 
				JLabel.RIGHT);
		chooseVariablePanel.add(setVariableLabel, gbc);

		// card 1.1: variable name - text field
		gbc.gridx = 1; 
		newVariableTextField = new JXTextField("name of new variable");
		newVariableTextField.setColumns(12);
		chooseVariablePanel.add(newVariableTextField, gbc);
		newVariableTextField.setEnabled(false);

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

		// card 1.1: type for new variable  - comboBox
		gbc.gridx = 2; 
		newVariableType = new JComboBox<String>();
		newVariableType.addItem("short text");
		newVariableType.addItem("long text");
		newVariableType.addItem("integer");
		newVariableType.addItem("boolean");
		chooseVariablePanel.add(newVariableType, gbc);
		newVariableType.setEnabled(false);

		// card 1.1: choose variable to be overwritten: 
		// card 1.1: chose variable 2 - label
		gbc.gridy++;
		gbc.gridx = 0;
		JLabel chooseVarOverwrite = new JLabel("variable to be overwritten: ", 
				JLabel.RIGHT);
		chooseVariablePanel.add(chooseVarOverwrite, gbc);

		// card 1.1: choose variable 2 - ComboBox
		gbc.gridx = 1; 
		String typeOverwrite = typeBox.getSelectedItem().toString();
		variableList = Dna.dna.db.getVariablesByTypes(typeOverwrite, 
				"short text", "long text");
		Object[] variableKeysOverwrite = variableList.keySet().toArray();
		String[] variablesOverwrite = new String[variableKeysOverwrite.length];
		for (int i = 0; i < variableKeysOverwrite.length; i++) {
			variablesOverwrite[i] = variableKeysOverwrite[i].toString();
		}
		variableBoxOverwrite = new JComboBox<String>(variablesOverwrite);
		chooseVariablePanel.add(variableBoxOverwrite, gbc);
		variableBoxOverwrite.setEnabled(false);

		// card 1.1: write border around chooseVariablePanel
		TitledBorder chooseVariablePanelBorder = BorderFactory.
				createTitledBorder("Choose variable that needs to be recoded");
		chooseVariablePanel.setBorder(chooseVariablePanelBorder);	

		/*
		 * CARD 1.2: SET PANEL
		 */
		JPanel card1Panel = new JPanel(new BorderLayout());
		card1Panel.add(chooseVariablePanel, BorderLayout.NORTH);

		/*
		 * CARD 1.3: BOOLEAN VARIABLE AFFECTED?
		 */
		chooseBooleanPanel = new JPanel(new GridBagLayout());

		// card 1.3: is a boolean affected? - label 
		gbc.gridx = 0; 
		gbc.gridwidth = 4;

		booleanAffectedButton = new JRadioButton("Are boolean"
				+ " or integer (i.e. numerical) variables affected by"
				+ " forthcoming changes?");
		chooseBooleanPanel.add(booleanAffectedButton, gbc);

		booleanAffectedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				if (booleanAffectedButton.isSelected() == true){
					// task 1: iterate boolean Counter (to 1)
					booleanCounter++;	
					// task 3: 
					try{
						addChooseVariableToPanel(gbc, chooseBooleanPanel, 
								booleanCounter);
					}
					//TODO: why does warning message appear twice?
					catch(ArrayIndexOutOfBoundsException iae){
						// give warning
						String message = "\n This statement type does not have "
								+ "any numerical variables."; 
						JOptionPane.showMessageDialog(new JFrame(), message, 
								"Warning", JOptionPane.ERROR_MESSAGE);
						booleanAffectedButton.setSelected(false);
					}				
					// task 4: enable Buttons
					addVariableButton.setEnabled(true);
					removeVariableButton.setEnabled(true);
					// task 5: give warning if no boolean in this statement type
					selectBooleanOrGiveWarning(variableBox1);
					// task 6: call pack() on panel
					pack();
				}
				if (booleanAffectedButton.isSelected() == false){
					booleanCounter = 0;
					int maxComponentCount = (int) chooseBooleanPanel.
							getComponentCount();
					for (int i = 3; i < maxComponentCount; i++){
						int countCompartments = chooseBooleanPanel.
								getComponentCount()-1;
						chooseBooleanPanel.remove(countCompartments);
					}
					addVariableButton.setEnabled(false);
					removeVariableButton.setEnabled(false);
					pack();
				}
				checkIfNextButtonCanBeActivated();
			}
		});

		// card 1.3: add boolean panel - button
		gbc.gridx = 0; 
		gbc.gridy++;
		gbc.gridwidth = 1;
		ImageIcon addVariableIcon = new ImageIcon(getClass().getResource(
				"/icons/add.png"));
		addVariableButton = new JButton("add boolean", addVariableIcon);
		addVariableButton.setEnabled(false);
		addVariableButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				booleanCounter++;	
				addChooseVariableToPanel(gbc, chooseBooleanPanel,
						booleanCounter);
				if (booleanCounter == 5){
					addVariableButton.setEnabled(false);
				}
				pack();
				checkIfNextButtonCanBeActivated();
			}
		});
		pack();
		chooseBooleanPanel.add(addVariableButton, gbc);

		// card 1.3: remove boolean panel - button
		gbc.gridx = 0; 
		gbc.gridy++;
		ImageIcon removeVariableIcon = new ImageIcon(getClass().getResource(
				"/icons/delete.png"));
		removeVariableButton = new JButton("remove boolean", removeVariableIcon);
		removeVariableButton.setEnabled(false);
		removeVariableButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {			
				//remove last 11 components from the JPanel
				if (booleanCounter < 6){
					addVariableButton.setEnabled(true);
				}
				for (int i = 1; i < 12; i++){
					int countCompartments = chooseBooleanPanel.
							getComponentCount()-1;
					chooseBooleanPanel.remove(countCompartments);
					pack();
				}
				pack();
				booleanCounter = booleanCounter -1; 
				if (booleanCounter == 0){
					removeVariableButton.setEnabled(false);
					booleanAffectedButton.setSelected(false);
					addVariableButton.setEnabled(false);
				}
				pack();
				checkIfNextButtonCanBeActivated();
			}
		});
		pack();
		chooseBooleanPanel.add(removeVariableButton, gbc);

		// card 1.3: Button Panel Boolean
		JPanel buttonCard1Panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		// card 1.3: cancel - button
		cancelIcon = new ImageIcon(getClass().getResource(
				"/icons/cancel.png"));
		JButton cancelButtonBoolean = new JButton("cancel", cancelIcon);
		buttonCard1Panel.add(cancelButtonBoolean);

		cancelButtonBoolean.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		// card 1.3: next - button
		nextIcon = new ImageIcon(getClass().getResource(
				"/icons/resultset_next.png"));
		nextButton = new JButton("next", nextIcon);
		buttonCard1Panel.add(nextButton);
		nextButton.setEnabled(false);

		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// task 1: update Names
				updateVariableNames();
				// task 2: update
				buttonCard2Next = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				buttonCard2Go = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				nextOrGo(0, buttonCard2Go, buttonCard2Next, 
						cancelButtonRecode, previousButtonRecode, 
						goButtonRecode, nextButtonRecode, card2Panel);
				// task 3: update ColumnHeader
				updateTableHeaderColumnNames(tableRecode, columnNames);
				// task 4: create data set
				createDataSet();
				// task 5: switch to next card
				cl.next(cards);
				// task 6: update meta-list for variable
				updateVariableListModel();
				// task 7: set border name for meta list:
				TitledBorder metaListPanelBorder = BorderFactory.
						createTitledBorder(borderName);
				metaListPanel.setBorder(metaListPanelBorder);
			}
		});

		// card 1.3: add vertical Scroller (first add panel so that it's not centered)
		JPanel newPanel = new JPanel(new BorderLayout());
		newPanel.add(chooseBooleanPanel, BorderLayout.NORTH); 
		newPanel.setPreferredSize(new Dimension(800, 800));

		// card 1.3: add border to chooseBooleanPanel
		TitledBorder buttonPanelBooleanBorder = BorderFactory.createTitledBorder(
				"Choose additional variable(s)");
		newPanel.setBorder(buttonPanelBooleanBorder);	

		JScrollPane scrollPane = new JScrollPane (newPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.
				VERTICAL_SCROLLBAR_AS_NEEDED); //VERTICAL_SCROLLBAR_ALWAYS

		card1Panel.add(scrollPane, BorderLayout.CENTER);
		card1Panel.add(buttonCard1Panel, BorderLayout.SOUTH);
		card1Panel.setPreferredSize(new Dimension(860, 600));
		cards.add(card1Panel, "Boolean affected by recode?");

		/*---------------------------------------------------------------------
		 *  CARD 2: do the actual recoding
		 *--------------------------------------------------------------------*/
		card2Panel = new JPanel(new BorderLayout());	

		/*
		 *  CARD 2.1: CREATE THE TABLE
		 */
		tableModel = new DefaultTableModel(columnNames, 0); // 0 =nr of rows
		tableRecode = new JXTable(tableModel);					

		// card 2.1: get values in table => done w/createDataSet() under card2-nextButton	

		// card 2.1: decide which columns are editable
		tableRecode.getColumnExt(0).setEditable(false);
		tableRecode.getColumnExt(1).setEditable(false);
		tableRecode.getColumnExt(2).setEditable(true);

		// card 2.1: drag&drop from column 0 to 2
		tableRecode.setDragEnabled(true);
		tableRecode.setDropMode(DropMode.USE_SELECTION);
		//TS() creates my own TransferHandler
		tableRecode.setTransferHandler(new TS());

		// card 2.1: add vertical/horizontal Scroller
		JScrollPane scrollPaneRecode = new JScrollPane (tableRecode);
		scrollPaneRecode.setPreferredSize(new Dimension(600, 300));
		scrollPaneRecode.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // .VERTICAL_SCROLLBAR_ALWAYS);
		//TODO: activate vertical scroller if variable entry = longer than table
		scrollPaneRecode.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// card 2.1: add table + tableHeader to panel
		JPanel tableRecodeAndHeaderPanel = new JPanel(new BorderLayout());
		tableRecodeAndHeaderPanel.add(tableRecode.getTableHeader(), 
				BorderLayout.NORTH);	
		tableRecodeAndHeaderPanel.add(scrollPaneRecode, BorderLayout.WEST);

		card2Panel.add(tableRecodeAndHeaderPanel, BorderLayout.WEST);

		/*
		 * CARD 2.2. META-LIST PANEL
		 */
		// card 2.2: List window
		listModel = new DefaultListModel<String>();
		variableEntryList = new JList<String>(listModel);
		variableEntryList.setSelectionMode(ListSelectionModel.
				SINGLE_INTERVAL_SELECTION);
		variableEntryList.setLayoutOrientation(JList.VERTICAL);
		variableEntryList.setVisibleRowCount(20);
		variableEntryList.setCellRenderer(new DefaultListCellRenderer(){
			private static final long serialVersionUID = 1L;
			public Component getListCellRendererComponent(JList<?> list, 
					Object value, int index, boolean isSelected, 
					boolean cellHasFocus) {
				String valueString = value.toString();
				valueString = valueString.substring(valueString.
						lastIndexOf('.')+1);
				return super.getListCellRendererComponent(list, valueString, 
						index, isSelected, cellHasFocus);
			}
		});

		variableEntryList.setDragEnabled(true);
		variableEntryList.setDropMode(DropMode.USE_SELECTION);

		JScrollPane listScroller = new JScrollPane(variableEntryList);
		listScroller.setPreferredSize(new Dimension(240, 300));

		// card 2.2: set options
		JPanel optionsMetaListPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbclist = new GridBagConstraints();
		gbclist.fill = GridBagConstraints.HORIZONTAL;
		gbclist.insets = new Insets(1, 0, 1, 5);
		optionsMetaListPanel.setPreferredSize(new Dimension (240, 230));


		// card 2.2: new list entry - text field
		gbclist.gridy = 0;
		gbclist.gridx = 0; 
		gbclist.gridwidth = 2; 
		newVariableEntry = new JXTextField("new entry for meta-list");
		//newVariableEntry.setMinimumSize(new Dimension(160, newVariableEntry.
		//		getHeight()));
		//newVariableEntry.setColumns(25);
		optionsMetaListPanel.add(newVariableEntry, gbclist);

		newVariableEntry.getDocument().addDocumentListener(new 
				DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkButton();
			}
			public void checkButton() {
				String var = newVariableEntry.getText().toString();
				boolean validAdd = false;				
				String[] variableListEntries = Dna.dna.db.
						getEntriesFromVariableList(statementType, 
								newVariableName);		
				for (int i = 0; i < variableListEntries.length; i++) {
					if (variableListEntries[i].equals(var)) {
						validAdd = true;
					}
				}
				if (validAdd == true) {
					okButtonListEntry.setEnabled(false);
				} else {
					okButtonListEntry.setEnabled(true);
				}
			}
		});

		// card 2.2: add list entry - Button
		gbclist.gridx = 2;
		gbclist.gridwidth = 1;
		addIcon = new ImageIcon(getClass().getResource(
				"/icons/add.png"));
		okButtonListEntry = new JButton("add", addIcon);
		//okButtonListEntry.setEnabled(true);
		optionsMetaListPanel.add(okButtonListEntry, gbclist);

		okButtonListEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String getListEntryText = (String) newVariableEntry.getText().
						toString();
				Dna.dna.db.addEntryToVariableList(getListEntryText,
						statementType, newVariableName);
				listModel.addElement(getListEntryText);
				newVariableEntry.setText("");
				okButtonListEntry.setEnabled(false);
			}
		});

		// card 2.2: delete list entry - Button
		gbclist.gridx = 0; 
		gbclist.gridy++;
		gbclist.gridwidth = 1;
		ImageIcon deleteIcon = new ImageIcon(getClass().getResource(
				"/icons/delete.png"));
		JButton deleteButtonListEntry = new JButton("delete", deleteIcon);
		optionsMetaListPanel.add(deleteButtonListEntry, gbclist);

		deleteButtonListEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				int index = variableEntryList.getSelectedIndex();
				String entries[] = Dna.dna.db.getEntriesFromVariableList(
						statementType, newVariableName);
				String label = entries[index];	
				if (index >= 0) {
					Dna.dna.db.removeEntryFromVariableList(label,
							statementType, newVariableName);
					listModel.removeElementAt(index);
				}
			}
		});

		// card 2.2: delete all entries - Button
		gbclist.gridx = 1;
		gbclist.gridwidth = 2;
		JButton deleteAllButtonListEntry = new JButton("delete all", deleteIcon);
		optionsMetaListPanel.add(deleteAllButtonListEntry, gbclist);

		deleteAllButtonListEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String entries[] = Dna.dna.db.getEntriesFromVariableList(
						statementType, newVariableName);
				for(int i=0; i < entries.length; i++){
					Dna.dna.db.removeEntryFromVariableList(entries[i],
							statementType, newVariableName);
					listModel.removeElement(entries[i]);
				}
			}
		});

		// card 2.2: import from oldVar - Button
		gbclist.gridy++;
		gbclist.gridx = 0; 
		gbclist.gridwidth = 3;
		ImageIcon getFromLeftIcon = new ImageIcon(getClass().getResource(
				"/icons/application_side_expand.png"));
		JButton importFromOldVar = new JButton("import entries from old "
				+ "variable", getFromLeftIcon);
		optionsMetaListPanel.add(importFromOldVar, gbclist);

		importFromOldVar.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
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

		// card 2.2: import from newVar - Button
		gbclist.gridy++;
		gbclist.gridx = 0; 
		gbclist.gridwidth = 3;
		JButton importFromNewVar = new JButton("import entries from new "
				+ "variable", getFromLeftIcon);
		optionsMetaListPanel.add(importFromNewVar, gbclist);

		importFromNewVar.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
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

		// card 2.2: load external list - Button
		gbclist.gridx = 0;
		gbclist.gridy++;
		gbclist.gridwidth = 1;
		ImageIcon importIcon = new ImageIcon(getClass().getResource(
				"/icons/table_add.png"));
		JButton importList = new JButton("import list", importIcon);
		optionsMetaListPanel.add(importList, gbclist);

		importList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
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
					//TODO: expand this to .rtf-Documents/etc.
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
						Object[] dataImportFromFile = {select, linesImported.get(i)};
						tableModelImportFromFile.addRow(dataImportFromFile);
					}

					// button-panel
					JPanel buttonImportEntriesFromFilePanel = new JPanel(new 
							FlowLayout(FlowLayout.RIGHT));

					// select all - button
					JButton selectEntriesImportFromFile = new JButton("select all");
					buttonImportEntriesFromFilePanel.add(
							selectEntriesImportFromFile);

					selectEntriesImportFromFile.addActionListener(
							new ActionListener(){
								public void actionPerformed(ActionEvent e){
									for (int i = 0; i < tableModelImportFromFile.
											getRowCount(); i++){
										tableModelImportFromFile.setValueAt(true, 
												i, 0);
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
										tableModelImportFromFile.setValueAt(false, 
												i, 0);
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
					buttonImportEntriesFromFilePanel.add(addEntriesImportFromFile);

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
										Dna.dna.db.addEntryToVariableList(newEntry,
												statementType, 
												newVariableName);
										listModel.addElement(newEntry);
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

		// card 2.2: export list - Button
		gbclist.gridx = 1; 
		gbclist.gridwidth = 2; 
		ImageIcon exportIcon = new ImageIcon(getClass().getResource(
				"/icons/table_go.png"));
		JButton exportList = new JButton("export list", exportIcon);
		optionsMetaListPanel.add(exportList, gbclist);

		exportList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String[] variableListEntries = Dna.dna.db.
						getEntriesFromVariableList(statementType, 
								newVariableName);
				String writeEntries = "List of meta-categories for variable '" + 
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
					//TODO: expand this to .rtf-Documents/etc.
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
					try {
						File newTextFile = new File(fileToSave.getAbsolutePath());
						FileWriter fw = new FileWriter(newTextFile);
						fw.write(writeEntries);
						fw.close();

					} catch (IOException iox) {
						iox.printStackTrace();
					}
				}


			}
		});

		// card 2.2: add the panels:
		pack();
		metaListPanel = new JPanel(new BorderLayout());
		metaListPanel.add(listScroller, BorderLayout.NORTH);
		metaListPanel.add(optionsMetaListPanel, BorderLayout.SOUTH);

		// card 2.2: add title to list-panel (done in: updateVariableNames())
		// card 2.2: add metaListPanel to card 2
		card2Panel.add(metaListPanel, BorderLayout.EAST);

		/*
		 * CARD 2.3. BUTTONS-PANEL
		 */		
		// card 2.3: cancel - button
		cancelButtonRecode = new JButton("cancel", cancelIcon);

		cancelButtonRecode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		// card 2.3: previous - button
		ImageIcon previousIcon = new ImageIcon(getClass().getResource(
				"/icons/resultset_previous.png"));
		previousButtonRecode = new JButton("previous", previousIcon);

		previousButtonRecode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "\n Would you like to go to the previous"
						+ " window?\nAll changes you made in this window"
						+ " will be lost.";
				Object[] options = {"previous window",
				"cancel"};
				int result = JOptionPane.showOptionDialog(new JFrame(), message, 
						"Warning", 
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,     //do not use a custom Icon
						options,  //the titles of buttons
						options[0]); //default button title
				switch (result) {
				case 0:
					// task 1: remove the panel: buttonCard2Next and buttonCard2Go
					if (booleanCounter == 0){
						card2Panel.remove(buttonCard2Go);
					}else{
						card2Panel.remove(buttonCard2Next);	
					}
					// task 2: show previous panel
					cl.previous(cards);
				case 1:
					break;
				}
			}
		});

		// card 2.3: go-recode - button
		ImageIcon goIcon = new ImageIcon(getClass().getResource(
				"/icons/accept.png"));
		goButtonRecode = new JButton("go", goIcon);

		goButtonRecode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// create new variable
				if (createNewVariable.isSelected() == true){
					Dna.dna.db.addVariable(newVariableName, 
							newVariableDataType, 
							statementType);
				}
				// get values from table
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
				dispose();
			}
		});

		// card 2.3: next-button (if boolean is selected)
		nextButtonRecode = new JButton("next", nextIcon);

		nextButtonRecode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				// task 1: create data set
				createDataSetBoolean(tableModelBoolean1, boolean1OldName);
				// task 2: update buttons-panel
				buttonCard3Go = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				buttonCard3Next = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				nextOrGo(1, buttonCard3Go, buttonCard3Next, 
						cancelButtonBoolean1, previousButtonBoolean1, 
						goButtonBoolean1, nextButtonBoolean1, card3Panel);
				// task 3: update table header
				updateTableHeaderColumnNames(tableBoolean1, 
						columnNamesBooleanPanel1);
				// task 4: update border around Boolean1-Panel
				TitledBorder card3PanelBorder = BorderFactory.
						createTitledBorder(borderNameBoolean1);
				card3Panel.setBorder(card3PanelBorder);
				// task 5: show next window
				cl.next(cards);			
			}
		});

		cards.add(card2Panel, "Recode variables");

		/*---------------------------------------------------------------------
		 * CARD 3 - RECODE BOOLEAN PANEL #1
		 *--------------------------------------------------------------------*/
		card3Panel = new JPanel(new BorderLayout());

		/*
		 * CARD 3.1: Create table
		 */	
		// card 3.1: table
		tableModelBoolean1 = new DefaultTableModel(columnNamesBooleanPanel1, 0);
		tableBoolean1 = new JXTable(tableModelBoolean1);	

		// card 3.1: get values in table => done w/createDataSetBoolean() under card2.3-nextButton

		// card 3.1: decide which columns are editable
		tableBoolean1.getColumnExt(0).setEditable(false);
		tableBoolean1.getColumnExt(1).setEditable(false);
		tableBoolean1.getColumnExt(2).setEditable(false);
		tableBoolean1.getColumnExt(3).setEditable(true);
		tableBoolean1.getColumnExt(4).setEditable(false);

		// card 3.1: set column width of col 1, 2 and 3
		tableBoolean1.getColumnModel().getColumn(1).setPreferredWidth(90);
		tableBoolean1.getColumnModel().getColumn(2).setPreferredWidth(90);
		tableBoolean1.getColumnModel().getColumn(3).setPreferredWidth(90);

		// card 3.1: add vertical/horizontal Scroller
		JScrollPane scrollPaneTableBoolean1 = new JScrollPane (tableBoolean1);
		scrollPaneTableBoolean1.setPreferredSize(new Dimension(800, 520));
		scrollPaneTableBoolean1.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneTableBoolean1.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// card 3.1: add table + tableHeader to panel
		JPanel tableBoolean1AndHeaderPanel = new JPanel(new BorderLayout());
		tableBoolean1AndHeaderPanel.add(tableBoolean1.getTableHeader(), 
				BorderLayout.NORTH);	
		tableBoolean1AndHeaderPanel.add(scrollPaneTableBoolean1, 
				BorderLayout.WEST);

		card3Panel.add(tableBoolean1AndHeaderPanel, BorderLayout.NORTH);

		/*
		 * CARD 3.2: BUTTONS PANEL FOR BOOLEAN TABLE 1
		 */
		// card 3.2: cancel button
		cancelButtonBoolean1 = new JButton("cancel", cancelIcon);

		cancelButtonBoolean1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		// card 3.2: previous - button
		previousButtonBoolean1 = new JButton("previous", previousIcon);

		previousButtonBoolean1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "\n Would you like to go to the previous"
						+ " window?\nAll changes you made in this window"
						+ " will be lost.";
				Object[] options = {"previous window",
				"cancel"};
				int result = JOptionPane.showOptionDialog(new JFrame(), message, 
						"Warning", 
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,     //do not use a custom Icon
						options,  //the titles of buttons
						options[0]); //default button title
				switch (result) {
				case 0:	
					// task 1: remove the panel: buttonCard3Next and buttonCard3Go
					if (booleanCounter == 1){
						card3Panel.remove(buttonCard3Go);
					}else{
						card3Panel.remove(buttonCard3Next);	
					}
					// task 2: show previous panel
					cl.previous(cards);
				case 1:
					break;
				}
			}
		});

		// card 3.2: go - button
		goButtonBoolean1 = new JButton("go", goIcon);

		goButtonBoolean1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean breakNow = false;
				// task 1: create new variable
				if (createNewVariable.isSelected() == true){
					Dna.dna.db.addVariable(newVariableName, 
							newVariableDataType, 
							statementType);
				}
				if (createNewVariable1.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField1.getText(), 
							boolean1NewDataType, 
							statementType);
				}
				// task 2: recode boolean/int variables
				breakNow = recodeBooleanVariables(tableBoolean1, 
						tableModelBoolean1, boolean1OldName, 
						boolean1NewName, breakNow);
				System.out.println("breakNow = " + breakNow);
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
									oldVarDataType);
						}
					}
					// task 4: close window
					dispose();
				}
			}
		});

		// card 3.2: next-button (if boolean #2 is selected)
		nextButtonBoolean1 = new JButton("next", nextIcon);

		nextButtonBoolean1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// task 1: check if all entries in table are integers
				boolean breakNow = false;
				for (int i = 0; i < tableModelBoolean1.getRowCount(); i++){
					try{
						Integer.parseInt(tableModelBoolean1.getValueAt(i, 3).
								toString());
					}catch(java.lang.NumberFormatException nfe){
						String message = "\n The entries in column 4 are not all "
								+ "integers.\nPlease check your entries before "
								+ "moving on to the next variable."; 
						JOptionPane.showMessageDialog(new JFrame(), message, 
								"Warning", JOptionPane.ERROR_MESSAGE);
						breakNow = true;
					}
				}
				// task 2: create data set
				if (breakNow == false){
					createDataSetBoolean(tableModelBoolean2, boolean2OldName);
					// task 3: update buttons-panel
					buttonCard4Go = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					buttonCard4Next = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					nextOrGo(2, buttonCard4Go, buttonCard4Next, 
							cancelButtonBoolean2, previousButtonBoolean2, 
							goButtonBoolean2, nextButtonBoolean2, card4Panel);
					// task 4: update table header
					updateTableHeaderColumnNames(tableBoolean2, 
							columnNamesBooleanPanel2);
					// task 5: update border around Boolean1-Panel
					TitledBorder card4PanelBorder = BorderFactory.
							createTitledBorder(borderNameBoolean2);
					card4Panel.setBorder(card4PanelBorder);
					// task 6: show next window
					cl.next(cards);
				}
			}
		});

		cards.add(card3Panel, "Recode additional variables");

		/*---------------------------------------------------------------------
		 * CARD 4 - RECODE BOOLEAN PANEL #2
		 *--------------------------------------------------------------------*/
		card4Panel = new JPanel(new BorderLayout());

		/*
		 * CARD 4.1: Create table
		 */	
		// card 4.1: table
		tableModelBoolean2 = new DefaultTableModel(columnNamesBooleanPanel2, 0); 
		tableBoolean2 = new JXTable(tableModelBoolean2);	

		// card 4.1: get values in table => done w/createDataSetBoolean() under card3.2-nextButton

		// card 4.1: decide which columns are editable
		tableBoolean2.getColumnExt(0).setEditable(false);
		tableBoolean2.getColumnExt(1).setEditable(false);
		tableBoolean2.getColumnExt(2).setEditable(false);
		tableBoolean2.getColumnExt(3).setEditable(true);
		tableBoolean2.getColumnExt(4).setEditable(false);

		// card 4.1: set column width of col 1, 2 and 3
		tableBoolean2.getColumnModel().getColumn(1).setPreferredWidth(90);
		tableBoolean2.getColumnModel().getColumn(2).setPreferredWidth(90);
		tableBoolean2.getColumnModel().getColumn(3).setPreferredWidth(90);

		// card 4.1: add vertical/horizontal Scroller
		JScrollPane scrollPaneTableBoolean2 = new JScrollPane (tableBoolean2);
		scrollPaneTableBoolean2.setPreferredSize(new Dimension(800, 520));
		scrollPaneTableBoolean2.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneTableBoolean2.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// card 4.1: add table + tableHeader to panel
		JPanel tableBoolean2AndHeaderPanel = new JPanel(new BorderLayout());
		tableBoolean2AndHeaderPanel.add(tableBoolean2.getTableHeader(), 
				BorderLayout.NORTH);	
		tableBoolean2AndHeaderPanel.add(scrollPaneTableBoolean2, 
				BorderLayout.WEST);

		card4Panel.add(tableBoolean2AndHeaderPanel, BorderLayout.NORTH);

		/*
		 * CARD 4.2: BUTTONS PANEL FOR BOOLEAN TABLE 2
		 */
		// card 4.2: cancel button
		cancelButtonBoolean2 = new JButton("cancel", cancelIcon);

		cancelButtonBoolean2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		// card 4.2: previous - button
		previousButtonBoolean2 = new JButton("previous", previousIcon);

		previousButtonBoolean2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "\n Would you like to go to the previous"
						+ " window?\nAll changes you made in this window"
						+ " will be lost.";
				Object[] options = {"previous window",
				"cancel"};
				int result = JOptionPane.showOptionDialog(new JFrame(), message, 
						"Warning", 
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,     //do not use a custom Icon
						options,  //the titles of buttons
						options[0]); //default button title
				switch (result) {
				case 0:
					// task 1: remove the panel: buttonCard4Next and buttonCard4Go
					if (booleanCounter == 2){
						card4Panel.remove(buttonCard4Go);
					}else{
						card4Panel.remove(buttonCard4Next);	
					}
					// task 2: show previous panel
					cl.previous(cards);
				case 1:
					break;
				}
			}
		});

		// card 4.2: go - button
		goButtonBoolean2 = new JButton("go", goIcon);

		goButtonBoolean2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean breakNow = false;
				// task 1: create new variable
				if (createNewVariable.isSelected() == true){
					Dna.dna.db.addVariable(newVariableName, 
							newVariableDataType, 
							statementType);
				}
				if (createNewVariable1.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField1.getText(), 
							boolean1NewDataType, 
							statementType); 
				}
				if (createNewVariable2.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField2.getText(), 
							boolean2NewDataType, 
							statementType); 
				}
				// task 2: recode boolean/int variables
				breakNow = recodeBooleanVariables(tableBoolean2, 
						tableModelBoolean2, boolean2OldName, 
						boolean2NewName, breakNow);
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
									oldVarDataType);
						}
					}
					// task 4: close window
					dispose();
				}
			}
		});

		// card 4.2: next-button (if boolean #2 is selected)
		nextButtonBoolean2 = new JButton("next", nextIcon);

		nextButtonBoolean2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// task 1: check if all entries in table are integers
				boolean breakNow = false;
				for (int i = 0; i < tableModelBoolean2.getRowCount(); i++){
					try{
						Integer.parseInt(tableModelBoolean2.getValueAt(i, 3).
								toString());
					}catch(java.lang.NumberFormatException nfe){
						String message = "\n The entries in column 4 are not all "
								+ "integers.\nPlease check your entries before "
								+ "moving on to the next variable."; 
						JOptionPane.showMessageDialog(new JFrame(), message, "Warning",
								JOptionPane.ERROR_MESSAGE);
						breakNow = true;
					}
				}
				// task 2: create data set
				if (breakNow == false){
					createDataSetBoolean(tableModelBoolean3, boolean3OldName);
					// task 3: update buttons-panel
					buttonCard5Go = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					buttonCard5Next = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					nextOrGo(3, buttonCard5Go, buttonCard5Next, 
							cancelButtonBoolean3, previousButtonBoolean3, 
							goButtonBoolean3, nextButtonBoolean3, card5Panel);
					// task 4: update table header
					updateTableHeaderColumnNames(tableBoolean3, 
							columnNamesBooleanPanel3);
					// task 5: update border around Boolean1-Panel
					TitledBorder card5PanelBorder = BorderFactory.
							createTitledBorder(borderNameBoolean3);
					card5Panel.setBorder(card5PanelBorder);
					// task 6: show next window
					cl.next(cards);
				}
			}
		});

		cards.add(card4Panel, "Recode additional variables");

		/*---------------------------------------------------------------------
		 * CARD 5 - RECODE BOOLEAN PANEL #3
		 *--------------------------------------------------------------------*/
		card5Panel = new JPanel(new BorderLayout());

		/*
		 * CARD 5.1: Create table
		 */	
		// card 5.1: table
		tableModelBoolean3 = new DefaultTableModel(columnNamesBooleanPanel3, 0);
		tableBoolean3 = new JXTable(tableModelBoolean3);	

		// card 5.1: get values in table => done w/createDataSetBoolean() under card3.2-nextButton

		// card 5.1: decide which columns are editable
		tableBoolean3.getColumnExt(0).setEditable(false);
		tableBoolean3.getColumnExt(1).setEditable(false);
		tableBoolean3.getColumnExt(2).setEditable(false);
		tableBoolean3.getColumnExt(3).setEditable(true);
		tableBoolean3.getColumnExt(4).setEditable(false);

		// card 5.1: set column width of col 1, 2 and 3
		tableBoolean3.getColumnModel().getColumn(1).setPreferredWidth(90);
		tableBoolean3.getColumnModel().getColumn(2).setPreferredWidth(90);
		tableBoolean3.getColumnModel().getColumn(3).setPreferredWidth(90);

		// card 5.1: add vertical/horizontal Scroller
		JScrollPane scrollPaneTableBoolean3 = new JScrollPane (tableBoolean3);
		scrollPaneTableBoolean3.setPreferredSize(new Dimension(800, 520));
		scrollPaneTableBoolean3.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneTableBoolean3.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// card 5.1: add table + tableHeader to panel
		JPanel tableBoolean3AndHeaderPanel = new JPanel(new BorderLayout());
		tableBoolean3AndHeaderPanel.add(tableBoolean3.getTableHeader(), 
				BorderLayout.NORTH);	
		tableBoolean3AndHeaderPanel.add(scrollPaneTableBoolean3, 
				BorderLayout.WEST);

		card5Panel.add(tableBoolean3AndHeaderPanel, BorderLayout.NORTH);

		/*
		 * CARD 5.2: BUTTONS PANEL FOR BOOLEAN TABLE 2
		 */
		// card 5.2: cancel button
		cancelButtonBoolean3 = new JButton("cancel", cancelIcon);

		cancelButtonBoolean3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		// card 5.2: previous - button
		previousButtonBoolean3 = new JButton("previous", previousIcon);

		previousButtonBoolean3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "\n Would you like to go to the previous"
						+ " window?\nAll changes you made in this window"
						+ " will be lost.";
				Object[] options = {"previous window",
				"cancel"};
				int result = JOptionPane.showOptionDialog(new JFrame(), message, 
						"Warning", 
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,     //do not use a custom Icon
						options,  //the titles of buttons
						options[0]); //default button title
				switch (result) {
				case 0:
					// task 1: remove the panel: buttonCard5Next and buttonCard5Go
					if (booleanCounter == 3){
						card5Panel.remove(buttonCard5Go);
					}else{
						card5Panel.remove(buttonCard5Next);	
					}
					// task 2: show previous panel
					cl.previous(cards);
				case 1:
					break;
				}
			}
		});

		// card 5.2: go - button
		goButtonBoolean3 = new JButton("go", goIcon);

		goButtonBoolean3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean breakNow = false;
				// task 1: create new variable
				if (createNewVariable.isSelected() == true){
					Dna.dna.db.addVariable(newVariableName, 
							newVariableDataType, 
							statementType);
				}
				if (createNewVariable1.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField1.getText(), 
							boolean1NewDataType, 
							statementType); 
				}
				if (createNewVariable2.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField2.getText(), 
							boolean2NewDataType, 
							statementType); 
				}
				if (createNewVariable3.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField3.getText(), 
							boolean3NewDataType, 
							statementType); 
				}
				// task 2: recode boolean/int variables
				breakNow = recodeBooleanVariables(tableBoolean3, 
						tableModelBoolean3, boolean3OldName, 
						boolean3NewName, breakNow);
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
									oldVarDataType);
						}
					}
					// task 4: close window
					dispose();
				}
			}
		});

		// card 5.2: next-button (if boolean #2 is selected)
		nextButtonBoolean3 = new JButton("next", nextIcon);

		nextButtonBoolean3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// task 1: check if all entries in table are integers
				boolean breakNow = false;
				for (int i = 0; i < tableModelBoolean3.getRowCount(); i++){
					try{
						Integer.parseInt(tableModelBoolean3.getValueAt(i, 3).
								toString());
					}catch(java.lang.NumberFormatException nfe){
						String message = "\n The entries in column 4 are not all "
								+ "integers.\nPlease check your entries before "
								+ "moving on to the next variable."; 
						JOptionPane.showMessageDialog(new JFrame(), message,
								"Warning", JOptionPane.ERROR_MESSAGE);
						breakNow = true;
					}
				}
				// task 2: create data set
				if (breakNow == false){
					createDataSetBoolean(tableModelBoolean4, boolean4OldName);
					// task 3: update buttons-panel
					buttonCard6Go = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					buttonCard6Next = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					nextOrGo(4, buttonCard6Go, buttonCard6Next, 
							cancelButtonBoolean4, previousButtonBoolean4, 
							goButtonBoolean4, nextButtonBoolean4, card6Panel);
					// task 4: update table header
					updateTableHeaderColumnNames(tableBoolean4, 
							columnNamesBooleanPanel4);
					// task 5: update border around Boolean1-Panel
					TitledBorder card6PanelBorder = BorderFactory.
							createTitledBorder(borderNameBoolean4);
					card6Panel.setBorder(card6PanelBorder);
					// task 6: show next window
					cl.next(cards);
				}
			}
		});

		cards.add(card5Panel, "Recode additional variables");

		/*---------------------------------------------------------------------
		 * CARD 6 - RECODE BOOLEAN PANEL #4
		 *--------------------------------------------------------------------*/
		card6Panel = new JPanel(new BorderLayout());

		/*
		 * CARD 6.1: Create table
		 */	
		// card 6.1: table
		tableModelBoolean4 = new DefaultTableModel(columnNamesBooleanPanel4, 0);
		tableBoolean4 = new JXTable(tableModelBoolean4);	

		// card 6.1: get values in table => done w/createDataSetBoolean() under card3.2-nextButton

		// card 6.1: decide which columns are editable
		tableBoolean4.getColumnExt(0).setEditable(false);
		tableBoolean4.getColumnExt(1).setEditable(false);
		tableBoolean4.getColumnExt(2).setEditable(false);
		tableBoolean4.getColumnExt(3).setEditable(true);
		tableBoolean4.getColumnExt(4).setEditable(false);

		// card 6.1: set column width of col 1, 2 and 3
		tableBoolean4.getColumnModel().getColumn(1).setPreferredWidth(90);
		tableBoolean4.getColumnModel().getColumn(2).setPreferredWidth(90);
		tableBoolean4.getColumnModel().getColumn(3).setPreferredWidth(90);

		// card 6.1: add vertical/horizontal Scroller
		JScrollPane scrollPaneTableBoolean4 = new JScrollPane (tableBoolean4);
		scrollPaneTableBoolean4.setPreferredSize(new Dimension(800, 520));
		scrollPaneTableBoolean4.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneTableBoolean4.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// card 6.1: add table + tableHeader to panel
		JPanel tableBoolean4AndHeaderPanel = new JPanel(new BorderLayout());
		tableBoolean4AndHeaderPanel.add(tableBoolean4.getTableHeader(), 
				BorderLayout.NORTH);	
		tableBoolean4AndHeaderPanel.add(scrollPaneTableBoolean4, 
				BorderLayout.WEST);

		card6Panel.add(tableBoolean4AndHeaderPanel, BorderLayout.NORTH);

		/*
		 * CARD 6.2: BUTTONS PANEL FOR BOOLEAN TABLE 2
		 */
		// card 6.2: cancel button
		cancelButtonBoolean4 = new JButton("cancel", cancelIcon);

		cancelButtonBoolean4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		// card 6.2: previous - button
		previousButtonBoolean4 = new JButton("previous", previousIcon);

		previousButtonBoolean4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "\n Would you like to go to the previous"
						+ " window?\nAll changes you made in this window"
						+ " will be lost.";
				Object[] options = {"previous window",
				"cancel"};
				int result = JOptionPane.showOptionDialog(new JFrame(), message, 
						"Warning", 
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,     //do not use a custom Icon
						options,  //the titles of buttons
						options[0]); //default button title
				switch (result) {
				case 0:
					// task 1: remove the panel: buttonCard6Next and buttonCard6Go
					if (booleanCounter == 4){
						card6Panel.remove(buttonCard6Go);
					}else{
						card6Panel.remove(buttonCard6Next);	
					}
					// task 2: show previous panel
					cl.previous(cards);
				case 1:
					break;
				}
			}
		});

		// card 6.2: go - button
		goButtonBoolean4 = new JButton("go", goIcon);

		goButtonBoolean4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean breakNow = false;
				// task 1: create new variable
				if (createNewVariable.isSelected() == true){
					Dna.dna.db.addVariable(newVariableName, 
							newVariableDataType, 
							statementType);
				}
				if (createNewVariable1.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField1.getText(), 
							boolean1NewDataType, 
							statementType); 
				}
				if (createNewVariable2.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField2.getText(), 
							boolean2NewDataType, 
							statementType); 
				}
				if (createNewVariable3.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField3.getText(), 
							boolean3NewDataType, 
							statementType); 
				}
				if (createNewVariable4.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField4.getText(), 
							boolean4NewDataType, 
							statementType); 
				}
				// task 2: recode boolean/int variables
				breakNow = recodeBooleanVariables(tableBoolean4, 
						tableModelBoolean4, boolean4OldName, 
						boolean4NewName, breakNow);
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
									oldVarDataType);
						}
					}
					// task 4: close window
					dispose();
				}
			}
		});

		// card 6.2: next-button (if boolean #2 is selected)
		nextButtonBoolean4 = new JButton("next", nextIcon);

		nextButtonBoolean4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// task 1: check if all entries in table are integers
				boolean breakNow = false;
				for (int i = 0; i < tableModelBoolean4.getRowCount(); i++){
					try{
						Integer.parseInt(tableModelBoolean4.getValueAt(i, 3).
								toString());
					}catch(java.lang.NumberFormatException nfe){
						String message = "\n The entries in column 4 are not all "
								+ "integers.\nPlease check your entries before "
								+ "moving on to the next variable."; 
						JOptionPane.showMessageDialog(new JFrame(), message,
								"Warning", JOptionPane.ERROR_MESSAGE);
						breakNow = true;
					}
				}
				// task 1: create data set
				if (breakNow == false){
					createDataSetBoolean(tableModelBoolean5, boolean5OldName);
					// task 2: update table header
					updateTableHeaderColumnNames(tableBoolean5, 
							columnNamesBooleanPanel5);
					// task 3: update border around Boolean1-Panel
					TitledBorder card7PanelBorder = BorderFactory.
							createTitledBorder(borderNameBoolean5);
					card7Panel.setBorder(card7PanelBorder);
					// task 4: show next window
					cl.next(cards);
				}
			}
		});

		cards.add(card6Panel, "Recode additional variables");

		/*---------------------------------------------------------------------
		 * CARD 7 - RECODE BOOLEAN PANEL #5
		 *--------------------------------------------------------------------*/
		// task 2: update buttons-panel
		card7Panel = new JPanel(new BorderLayout());

		/*
		 * CARD 7.1: Create table
		 */	
		// card 7.1: table
		tableModelBoolean5 = new DefaultTableModel(columnNamesBooleanPanel5, 0);
		tableBoolean5 = new JXTable(tableModelBoolean5);	

		// card 7.1: get values in table => done w/createDataSetBoolean() under card3.2-nextButton

		// card 7.1: decide which columns are editable
		tableBoolean5.getColumnExt(0).setEditable(false);
		tableBoolean5.getColumnExt(1).setEditable(false);
		tableBoolean5.getColumnExt(2).setEditable(false);
		tableBoolean5.getColumnExt(3).setEditable(true);
		tableBoolean5.getColumnExt(4).setEditable(false);

		// card 7.1: set column width of col 1, 2 and 3
		tableBoolean5.getColumnModel().getColumn(1).setPreferredWidth(90);
		tableBoolean5.getColumnModel().getColumn(2).setPreferredWidth(90);
		tableBoolean5.getColumnModel().getColumn(3).setPreferredWidth(90);

		// card 7.1: add vertical/horizontal Scroller
		JScrollPane scrollPaneTableBoolean5 = new JScrollPane (tableBoolean5);
		scrollPaneTableBoolean5.setPreferredSize(new Dimension(800, 520));
		scrollPaneTableBoolean5.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneTableBoolean5.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// card 7.1: add table + tableHeader to panel
		JPanel tableBoolean5AndHeaderPanel = new JPanel(new BorderLayout());
		tableBoolean5AndHeaderPanel.add(tableBoolean5.getTableHeader(), 
				BorderLayout.NORTH);	
		tableBoolean5AndHeaderPanel.add(scrollPaneTableBoolean5, 
				BorderLayout.WEST);

		card7Panel.add(tableBoolean5AndHeaderPanel, BorderLayout.NORTH);

		/*
		 * CARD 7.2: BUTTONS PANEL FOR BOOLEAN TABLE 2
		 */
		JPanel buttonCard7Go = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		// card 7.2: cancel button
		cancelButtonBoolean5 = new JButton("cancel", cancelIcon);
		buttonCard7Go.add(cancelButtonBoolean5);

		cancelButtonBoolean5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		// card 7.2: previous - button
		previousButtonBoolean5 = new JButton("previous", previousIcon);
		buttonCard7Go.add(previousButtonBoolean5);

		previousButtonBoolean5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "\n Would you like to go to the previous"
						+ " window?\nAll changes you made in this window"
						+ " will be lost.";
				Object[] options = {"previous window",
				"cancel"};
				int result = JOptionPane.showOptionDialog(new JFrame(), message, 
						"Warning", 
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,     //do not use a custom Icon
						options,  //the titles of buttons
						options[0]); //default button title
				switch (result) {
				case 0:
					// task 1: show previous panel
					cl.previous(cards);
				case 1: 
					break;
				}
			}
		});

		// card 7.2: go - button
		goButtonBoolean5 = new JButton("go", goIcon);
		buttonCard7Go.add(goButtonBoolean5);

		goButtonBoolean5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean breakNow = false;
				// task 1: create new variable
				if (createNewVariable.isSelected() == true){
					Dna.dna.db.addVariable(newVariableName, 
							newVariableDataType, 
							statementType);
				}
				if (createNewVariable1.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField1.getText(), 
							boolean1NewDataType, 
							statementType); 
				}
				if (createNewVariable2.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField2.getText(), 
							boolean2NewDataType, 
							statementType); 
				}
				if (createNewVariable3.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField3.getText(), 
							boolean3NewDataType, 
							statementType); 
				}
				if (createNewVariable4.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField4.getText(), 
							boolean4NewDataType, 
							statementType); 
				}
				if (createNewVariable5.isSelected() == true){
					Dna.dna.db.addVariable(newVariableTextField5.getText(), 
							boolean5NewDataType, 
							statementType); 
				}
				// task 2: recode boolean/int variables
				breakNow = recodeBooleanVariables(tableBoolean5, 
						tableModelBoolean5, boolean5OldName, 
						boolean5NewName, breakNow);
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
									oldVarDataType);
						}
					}
					// task 4: close window
					dispose();
				}
			}
		});

		card7Panel.add(buttonCard7Go, BorderLayout.SOUTH);
		cards.add(card7Panel, "Recode additional variables");

		/*
		 * FINISH
		 */
		// Finish JDialog
		this.add(cards, BorderLayout.NORTH);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	//-------------------------------------------------------------------------	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------	

	/**
	 * Add panel to cardPanel - either NEXT-button or GO-button.
	 * 
	 * @param n					Integer, counts how many boolean variables are chosen
	 * @param panelGo			Panel with CANCEL, PREVIOUS and GO-buttons
	 * @param panelNext			Panel with CANCEL, PREVIOUS and NEXT-buttons
	 * @param cancelButton		Button that calls dispose()
	 * @param previousButton	Button that leads to previous card
	 * @param goButton			Button that does the actual recoding
	 * @param nextButton		Button that leads to next card
	 */
	public void nextOrGo(int n, JPanel panelGo, JPanel panelNext, 
			JButton cancelButton, JButton previousButton, JButton goButton, 
			JButton nextButton, JPanel cardPanel){
		if (booleanCounter == n){
			panelGo.add(cancelButton);
			panelGo.add(previousButton);
			panelGo.add(goButton);
			cardPanel.add(panelGo, BorderLayout.SOUTH);
			//pack();
		}else{
			panelNext.add(cancelButton);
			panelNext.add(previousButton);
			panelNext.add(nextButton);
			cardPanel.add(panelNext, BorderLayout.SOUTH);
			//pack();
		}

	}

	/**
	 * Add boolean variables to lower-half of first card-panel.
	 * 
	 * @param gbc			Same gbc as used in booleanPanel
	 * @param panel			Name of panel where variables are added to
	 * @param counter		Counts how many boolean variables are chosen
	 */
	public void addChooseVariableToPanel(GridBagConstraints gbc, 
			final JPanel panel, final int counter){

		if (counter == 1){
			// 1 - Variable # - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			gbc.gridwidth = 1;
			JLabel numberLabel1 = new JLabel("Variable 1", JLabel.RIGHT);
			panel.add(numberLabel1, gbc);

			// 1 - chose variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			JLabel chooseVar1 = new JLabel("choose variable: ", JLabel.RIGHT);
			panel.add(chooseVar1, gbc);

			// 1 - choose variable - ComboBox
			gbc.gridx = 1; 
			String type1 = typeBox.getSelectedItem().toString();
			LinkedHashMap<String, String> variableList1 = Dna.dna.db.
					getVariablesByTypes(type1, "boolean", "integer");
			Object[] variableKeys1 = variableList1.keySet().toArray();
			String[] variables1 = new String[variableKeys1.length];
			for (int i = 0; i < variableKeys1.length; i++) {
				variables1[i] = variableKeys1[i].toString();
			}
			variableBox1 = new JComboBox<String>(variables1);
			panel.add(variableBox1, gbc);

			variableBox1.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
				}
			});

			// 1 - new variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel newVariableLabel1 = new JLabel("new variable: ",
					JLabel.RIGHT);
			panel.add(newVariableLabel1, gbc);

			// 1 - create new variable - radioButton
			gbc.gridx = 1;
			createNewVariable1 = new JRadioButton("create new variable");
			panel.add(createNewVariable1, gbc);

			createNewVariable1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (createNewVariable1.isSelected()){
						overwriteVariable1.setEnabled(false);
						newVariableTextField1.setEnabled(true);
						newVariableType1.setEnabled(true);
					}else{
						overwriteVariable1.setEnabled(true);
						newVariableTextField1.setEnabled(false);
						newVariableType1.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});	

			// 1 - overwrite existing variable - radioButton
			gbc.gridx = 2;
			overwriteVariable1 = new JRadioButton("overwrite existing variable");
			panel.add(overwriteVariable1, gbc);

			overwriteVariable1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (overwriteVariable1.isSelected()){
						createNewVariable1.setEnabled(false);
						variableBoxOverwrite1.setEnabled(true);
					}else{
						createNewVariable1.setEnabled(true);
						variableBoxOverwrite1.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});

			// 1 - set variable name/type - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			JLabel setVariableLabel1 = new JLabel("set name of variable: ", 
					JLabel.RIGHT);
			panel.add(setVariableLabel1, gbc);

			// 1 - variable name - text field
			gbc.gridx = 1; 
			newVariableTextField1 = new JXTextField("name of new variable");
			newVariableTextField1.setColumns(12);
			panel.add(newVariableTextField1, gbc);
			newVariableTextField1.setEnabled(false);
			newVariableTextField1.getDocument().addDocumentListener(new 
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

			// 1 - type for new variable  - comboBox
			gbc.gridx = 2; 
			newVariableType1 = new JComboBox<String>();
			newVariableType1.addItem("integer");
			newVariableType1.addItem("boolean");
			panel.add(newVariableType1, gbc);
			newVariableType1.setEnabled(false);

			// 1 - set type/variable of overwrite-variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel chooseVarOverwrite1 = new JLabel("variable to be "
					+ "overwritten: ", JLabel.RIGHT);
			panel.add(chooseVarOverwrite1, gbc);

			// 1 - choose variable of overwrite-variable - ComboBox
			gbc.gridx = 1; 
			String typeOverwrite1 = typeBox.getSelectedItem().toString();
			HashMap<String, String> variableList = Dna.dna.db.
					getVariablesByTypes(typeOverwrite1, "boolean", "integer");
			Object[] variableKeysOverwrite = variableList.keySet().toArray();
			String[] variablesOverwrite = new String[variableKeysOverwrite.length];
			for (int i = 0; i < variableKeysOverwrite.length; i++) {
				variablesOverwrite[i] = variableKeysOverwrite[i].toString();
			}
			variableBoxOverwrite1 = new JComboBox<String>(variablesOverwrite);
			panel.add(variableBoxOverwrite1, gbc);
			variableBoxOverwrite1.setEnabled(false);

			variableBoxOverwrite1.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
					if (nextButton.isEnabled() == true){
						// only update Variables if they are valid
						updateVariableNames();
					}
				}
			});	
		}
		if (counter == 2){
			// 2 - Variable # - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			gbc.gridwidth = 1;
			JLabel numberLabel2 = new JLabel("Variable 2", JLabel.RIGHT);
			panel.add(numberLabel2, gbc);

			// 2 - chose variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			JLabel chooseVar2 = new JLabel("choose variable: ", JLabel.RIGHT);
			panel.add(chooseVar2, gbc);

			// 2 - choose variable - ComboBox
			gbc.gridx = 1; 
			String type2 = typeBox.getSelectedItem().toString();
			HashMap<String, String> variableList2 = Dna.dna.db.
					getVariablesByTypes(type2, "boolean", "integer");
			Object[] variableKeys2 = variableList2.keySet().toArray();
			String[] variables2 = new String[variableKeys2.length];
			for (int i = 0; i < variableKeys2.length; i++) {
				variables2[i] = variableKeys2[i].toString();
			}
			variableBox2 = new JComboBox<String>(variables2);
			panel.add(variableBox2, gbc);

			variableBox2.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
				}
			});

			// 2 - new variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel newVariableLabel2 = new JLabel("new variable: ", 
					JLabel.RIGHT);
			panel.add(newVariableLabel2, gbc);

			// 2 - create new variable - radioButton
			gbc.gridx = 1;
			createNewVariable2 = new JRadioButton("create new variable");
			panel.add(createNewVariable2, gbc);

			createNewVariable2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (createNewVariable2.isSelected()){
						overwriteVariable2.setEnabled(false);
						newVariableTextField2.setEnabled(true);
						newVariableType2.setEnabled(true);
					}else{
						overwriteVariable2.setEnabled(true);
						newVariableTextField2.setEnabled(false);
						newVariableType2.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});	

			// 2 - overwrite existing variable - radioButton
			gbc.gridx = 2;
			overwriteVariable2 = new JRadioButton("overwrite existing variable");
			panel.add(overwriteVariable2, gbc);

			overwriteVariable2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (overwriteVariable2.isSelected()){
						createNewVariable2.setEnabled(false);
						variableBoxOverwrite2.setEnabled(true);
					}else{
						createNewVariable2.setEnabled(true);
						variableBoxOverwrite2.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});

			// 2 - set variable name/type - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			JLabel setVariableLabel2 = new JLabel("set name of variable: ", 
					JLabel.RIGHT);
			panel.add(setVariableLabel2, gbc);

			// 2 - variable name - text field
			gbc.gridx = 1; 
			newVariableTextField2 = new JXTextField("name of new variable");
			newVariableTextField2.setColumns(12);
			panel.add(newVariableTextField2, gbc);
			newVariableTextField2.setEnabled(false);
			newVariableTextField2.getDocument().addDocumentListener(new 
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

			// 2 - type for new variable  - comboBox
			gbc.gridx = 2; 
			newVariableType2 = new JComboBox<String>();
			newVariableType2.addItem("integer");
			newVariableType2.addItem("boolean");
			panel.add(newVariableType2, gbc);
			newVariableType2.setEnabled(false);

			// 2 - set type/variable of overwrite-variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel chooseVarOverwrite2 = new JLabel("variable to be "
					+ "overwritten: ", JLabel.RIGHT);
			panel.add(chooseVarOverwrite2, gbc);

			// 2 - choose variable of overwrite-variable - ComboBox
			gbc.gridx = 1; 
			String typeOverwrite2 = typeBox.getSelectedItem().toString();
			HashMap<String, String> variableList = Dna.dna.db.
					getVariablesByTypes(typeOverwrite2, "boolean", "integer");
			Object[] variableKeysOverwrite = variableList.keySet().toArray();
			String[] variablesOverwrite = new String[variableKeysOverwrite.length];
			for (int i = 0; i < variableKeysOverwrite.length; i++) {
				variablesOverwrite[i] = variableKeysOverwrite[i].toString();
			}
			variableBoxOverwrite2 = new JComboBox<String>(variablesOverwrite);
			panel.add(variableBoxOverwrite2, gbc);
			variableBoxOverwrite2.setEnabled(false);

			variableBoxOverwrite2.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
					if (nextButton.isEnabled() == true){
						updateVariableNames();
					}
				}
			});	
		}
		if (counter == 3){
			// 3 - Variable # - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			gbc.gridwidth = 1;
			JLabel numberLabel3 = new JLabel("Variable 3", JLabel.RIGHT);
			panel.add(numberLabel3, gbc);

			// 3 - chose variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			JLabel chooseVar3 = new JLabel("choose variable: ", JLabel.RIGHT);
			panel.add(chooseVar3, gbc);

			// 3 - choose variable - ComboBox
			gbc.gridx = 1; 
			String type3 = typeBox.getSelectedItem().toString();
			HashMap<String, String> variableList3 = Dna.dna.db.
					getVariablesByTypes(type3, "boolean", "integer");
			Object[] variableKeys3 = variableList3.keySet().toArray();
			String[] variables3 = new String[variableKeys3.length];
			for (int i = 0; i < variableKeys3.length; i++) {
				variables3[i] = variableKeys3[i].toString();
			}
			variableBox3 = new JComboBox<String>(variables3);
			panel.add(variableBox3, gbc);

			variableBox3.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
				}
			});

			// 3 - new variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel newVariableLabel3 = new JLabel("new variable: ", 
					JLabel.RIGHT);
			panel.add(newVariableLabel3, gbc);

			// 3 - create new variable - radioButton
			gbc.gridx = 1;
			createNewVariable3 = new JRadioButton("create new variable");
			panel.add(createNewVariable3, gbc);

			createNewVariable3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (createNewVariable3.isSelected()){
						overwriteVariable3.setEnabled(false);
						newVariableTextField3.setEnabled(true);
						newVariableType3.setEnabled(true);
					}else{
						overwriteVariable3.setEnabled(true);
						newVariableTextField3.setEnabled(false);
						newVariableType3.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});	

			// 3 - overwrite existing variable - radioButton
			gbc.gridx = 2;
			overwriteVariable3 = new JRadioButton("overwrite existing variable");
			panel.add(overwriteVariable3, gbc);

			overwriteVariable3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (overwriteVariable3.isSelected()){
						createNewVariable3.setEnabled(false);
						variableBoxOverwrite3.setEnabled(true);
					}else{
						createNewVariable3.setEnabled(true);
						variableBoxOverwrite3.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});

			// 3 - set variable name/type - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			JLabel setVariableLabel3 = new JLabel("set name of variable: ",
					JLabel.RIGHT);
			panel.add(setVariableLabel3, gbc);

			// 3 - variable name - text field
			gbc.gridx = 1; 
			newVariableTextField3 = new JXTextField("name of new variable");
			newVariableTextField3.setColumns(12);
			panel.add(newVariableTextField3, gbc);
			newVariableTextField3.setEnabled(false);
			newVariableTextField3.getDocument().addDocumentListener(new 
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

			// 3 - type for new variable  - comboBox
			gbc.gridx = 2; 
			newVariableType3 = new JComboBox<String>();
			newVariableType3.addItem("integer");
			newVariableType3.addItem("boolean");
			panel.add(newVariableType3, gbc);
			newVariableType3.setEnabled(false);

			// 3 - set type/variable of overwrite-variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel chooseVarOverwrite3 = new JLabel("variable to be "
					+ "overwritten: ", JLabel.RIGHT);
			panel.add(chooseVarOverwrite3, gbc);

			// 3 - choose variable of overwrite-variable - ComboBox
			gbc.gridx = 1; 
			String typeOverwrite3 = typeBox.getSelectedItem().toString();
			HashMap<String, String> variableList = Dna.dna.db.
					getVariablesByTypes(typeOverwrite3, "boolean", "integer");
			Object[] variableKeysOverwrite = variableList.keySet().toArray();
			String[] variablesOverwrite = new String[variableKeysOverwrite.length];
			for (int i = 0; i < variableKeysOverwrite.length; i++) {
				variablesOverwrite[i] = variableKeysOverwrite[i].toString();
			}
			variableBoxOverwrite3 = new JComboBox<String>(variablesOverwrite);
			panel.add(variableBoxOverwrite3, gbc);
			variableBoxOverwrite3.setEnabled(false);

			variableBoxOverwrite3.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
					if (nextButton.isEnabled() == true){
						updateVariableNames();
					}
				}
			});	
		}
		if (counter == 4){
			// 4 - Variable # - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			gbc.gridwidth = 1;
			JLabel numberLabel4 = new JLabel("Variable 4", JLabel.RIGHT);
			panel.add(numberLabel4, gbc);

			// 4 - chose variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			JLabel chooseVar4 = new JLabel("choose variable: ", JLabel.RIGHT);
			panel.add(chooseVar4, gbc);

			// 4 - choose variable - ComboBox
			gbc.gridx = 1; 
			String type4 = typeBox.getSelectedItem().toString();
			HashMap<String, String> variableList4 = Dna.dna.db.
					getVariablesByTypes(type4, "boolean", "integer");
			Object[] variableKeys4 = variableList4.keySet().toArray();
			String[] variables4 = new String[variableKeys4.length];
			for (int i = 0; i < variableKeys4.length; i++) {
				variables4[i] = variableKeys4[i].toString();
			}
			variableBox4 = new JComboBox<String>(variables4);
			panel.add(variableBox4, gbc);

			variableBox4.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
				}
			});

			// 4 - new variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel newVariableLabel4 = new JLabel("new variable: ",
					JLabel.RIGHT);
			panel.add(newVariableLabel4, gbc);

			// 4 - create new variable - radioButton
			gbc.gridx = 1;
			createNewVariable4 = new JRadioButton("create new variable");
			panel.add(createNewVariable4, gbc);

			createNewVariable4.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (createNewVariable4.isSelected()){
						overwriteVariable4.setEnabled(false);
						newVariableTextField4.setEnabled(true);
						newVariableType4.setEnabled(true);
					}else{
						overwriteVariable4.setEnabled(true);
						newVariableTextField4.setEnabled(false);
						newVariableType4.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});	

			// 4 - overwrite existing variable - radioButton
			gbc.gridx = 2;
			overwriteVariable4 = new JRadioButton("overwrite existing variable");
			panel.add(overwriteVariable4, gbc);

			overwriteVariable4.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (overwriteVariable4.isSelected()){
						createNewVariable4.setEnabled(false);
						variableBoxOverwrite4.setEnabled(true);
					}else{
						createNewVariable4.setEnabled(true);
						variableBoxOverwrite4.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});

			// 4 - set variable name/type - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			JLabel setVariableLabel4 = new JLabel("set name of variable: ", 
					JLabel.RIGHT);
			panel.add(setVariableLabel4, gbc);

			// 4 - variable name - text field
			gbc.gridx = 1; 
			newVariableTextField4 = new JXTextField("name of new variable");
			newVariableTextField4.setColumns(12);
			panel.add(newVariableTextField4, gbc);
			newVariableTextField4.setEnabled(false);
			newVariableTextField4.getDocument().addDocumentListener(new 
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

			// 4 - type for new variable  - comboBox
			gbc.gridx = 2; 
			newVariableType4 = new JComboBox<String>();
			newVariableType4.addItem("integer");
			newVariableType4.addItem("boolean");
			panel.add(newVariableType4, gbc);
			newVariableType4.setEnabled(false);

			// 4 - set type/variable of overwrite-variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel chooseVarOverwrite4 = new JLabel("variable to be "
					+ "overwritten: ", JLabel.RIGHT);
			panel.add(chooseVarOverwrite4, gbc);

			// 4 - choose variable of overwrite-variable - ComboBox
			gbc.gridx = 1; 
			String typeOverwrite4 = typeBox.getSelectedItem().toString();
			HashMap<String, String> variableList = Dna.dna.db.
					getVariablesByTypes(typeOverwrite4, "boolean", "integer");
			Object[] variableKeysOverwrite = variableList.keySet().toArray();
			String[] variablesOverwrite = new String[variableKeysOverwrite.length];
			for (int i = 0; i < variableKeysOverwrite.length; i++) {
				variablesOverwrite[i] = variableKeysOverwrite[i].toString();
			}
			variableBoxOverwrite4 = new JComboBox<String>(variablesOverwrite);
			panel.add(variableBoxOverwrite4, gbc);
			variableBoxOverwrite4.setEnabled(false);

			variableBoxOverwrite4.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
					if (nextButton.isEnabled() == true){
						updateVariableNames();
					}
				}
			});	
		}
		if (counter == 5){
			// 5 - Variable # - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			gbc.gridwidth = 1;
			JLabel numberLabel5 = new JLabel("Variable 5", JLabel.RIGHT);
			panel.add(numberLabel5, gbc);

			// 5 - chose variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			JLabel chooseVar5 = new JLabel("choose variable: ", JLabel.RIGHT);
			panel.add(chooseVar5, gbc);

			// 5 - choose variable - ComboBox
			gbc.gridx = 1; 
			String type5 = typeBox.getSelectedItem().toString();
			HashMap<String, String> variableList5 = Dna.dna.db.
					getVariablesByTypes(type5, "boolean", "integer");
			Object[] variableKeys5 = variableList5.keySet().toArray();
			String[] variables5 = new String[variableKeys5.length];
			for (int i = 0; i < variableKeys5.length; i++) {
				variables5[i] = variableKeys5[i].toString();
			}
			variableBox5 = new JComboBox<String>(variables5);
			panel.add(variableBox5, gbc);

			variableBox5.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
				}
			});

			// 5 - new variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel newVariableLabel5 = new JLabel("new variable: ", 
					JLabel.RIGHT);
			panel.add(newVariableLabel5, gbc);

			// 5 - create new variable - radioButton
			gbc.gridx = 1;
			createNewVariable5 = new JRadioButton("create new variable");
			panel.add(createNewVariable5, gbc);

			createNewVariable5.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (createNewVariable5.isSelected()){
						overwriteVariable5.setEnabled(false);
						newVariableTextField5.setEnabled(true);
						newVariableType5.setEnabled(true);
					}else{
						overwriteVariable5.setEnabled(true);
						newVariableTextField5.setEnabled(false);
						newVariableType5.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});	

			// 5 - overwrite existing variable - radioButton
			gbc.gridx = 2;
			overwriteVariable5 = new JRadioButton("overwrite existing variable");
			panel.add(overwriteVariable5, gbc);

			overwriteVariable5.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {	
					if (overwriteVariable5.isSelected()){
						createNewVariable5.setEnabled(false);
						variableBoxOverwrite5.setEnabled(true);
					}else{
						createNewVariable5.setEnabled(true);
						variableBoxOverwrite5.setEnabled(false);
					}
					checkIfNextButtonCanBeActivated();
				}			
			});

			// 5 - set variable name/type - label
			gbc.gridx = 0; 
			gbc.gridy++; 
			JLabel setVariableLabel5 = new JLabel("set name of variable: ", 
					JLabel.RIGHT);
			panel.add(setVariableLabel5, gbc);

			// 5 - variable name - text field
			gbc.gridx = 1; 
			newVariableTextField5 = new JXTextField("name of new variable");
			newVariableTextField5.setColumns(12);
			panel.add(newVariableTextField5, gbc);
			newVariableTextField5.setEnabled(false);
			newVariableTextField5.getDocument().addDocumentListener(new 
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

			// 5 - type for new variable  - comboBox
			gbc.gridx = 2; 
			newVariableType5 = new JComboBox<String>();
			newVariableType5.addItem("integer");
			newVariableType5.addItem("boolean");
			panel.add(newVariableType5, gbc);
			newVariableType5.setEnabled(false);

			// 5 - set type/variable of overwrite-variable - label
			gbc.gridy++;
			gbc.gridx = 0;
			JLabel chooseVarOverwrite5 = new JLabel("variable to be "
					+ "overwritten: ", JLabel.RIGHT);
			panel.add(chooseVarOverwrite5, gbc);

			// 5 - choose variable of overwrite-variable - ComboBox
			gbc.gridx = 1; 
			String typeOverwrite5 = typeBox.getSelectedItem().toString();
			HashMap<String, String> variableList = Dna.dna.db.
					getVariablesByTypes(typeOverwrite5, "boolean", "integer");
			Object[] variableKeysOverwrite = variableList.keySet().toArray();
			String[] variablesOverwrite = new String[variableKeysOverwrite.length];
			for (int i = 0; i < variableKeysOverwrite.length; i++) {
				variablesOverwrite[i] = variableKeysOverwrite[i].toString();
			}
			variableBoxOverwrite5 = new JComboBox<String>(variablesOverwrite);
			panel.add(variableBoxOverwrite5, gbc);
			variableBoxOverwrite5.setEnabled(false);

			variableBoxOverwrite5.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					checkIfNextButtonCanBeActivated();
					if (nextButton.isEnabled() == true){
						updateVariableNames();
					}
				}
			});	
		}	
	}

	/**
	 *  Update variable names after some action was taken.
	 */
	public void updateVariableNames(){			
		// old variable
		statementType = (String) typeBox.getSelectedItem().toString();		
		oldVariableName = (String) variableBox.getSelectedItem().toString();
		oldVarDataType = (String) Dna.dna.db.getDataType(oldVariableName, 
				statementType);
		oldVarColumnName = (String) "<html>old variable:<br>" + oldVariableName +
				"</html";
		// if overwrite/newVar is selected:
		if (overwriteVariable.isSelected() == true){
			newVariableName = (String) variableBoxOverwrite.
					getSelectedItem().toString();
			newVarColumnName = (String) "<html>new variable:<br>" + 
					newVariableName +"</html";
			newVariableDataType = Dna.dna.db.getDataType(newVariableName, 
					statementType);
			borderName = (String) "<html>Meta list of entries for:<br>" + 
					newVariableName +"</html>";
		}
		if (createNewVariable.isSelected() == true){
			newVariableName = (String) newVariableTextField.getText().
					toString();
			newVarColumnName = (String) "<html>new variable:<br>" + 
					newVariableName +"</html";
			newVariableDataType = newVariableType.getSelectedItem().toString(); 
			borderName = (String) "<html>Meta list of entries for:<br>" + 
					newVariableName +"</html>";
		}	
		// columnNames
		columnNames = new String[]{oldVarColumnName, 
				"<html>number of<br>occurences</html>", newVarColumnName};
		// columnNamesBooleanPanel1
		if (booleanCounter >= 1){
			boolean1OldName = variableBox1.getSelectedItem().toString();
			boolean1DataType = Dna.dna.db.getDataType(boolean1OldName, 
					statementType);
			boolean1OldColumnName = (String) "<html>old variable:<br>" + 
					boolean1OldName +"</html";
			if (overwriteVariable1.isSelected() == true){
				boolean1NewName = variableBoxOverwrite1.getSelectedItem().
						toString();
				boolean1NewDataType = boolean1DataType;
				boolean1NewColumnName = (String) "<html>new variable:<br>" + 
						boolean1NewName + "</html";
				borderNameBoolean1 = (String) "<html>Recode additional "
						+ "variable: " + boolean1NewName + "</html>";
			}
			if (createNewVariable1.isSelected() == true){
				boolean1NewName = newVariableTextField1.getText().toString();
				boolean1NewDataType = newVariableType1.getSelectedItem().
						toString();
				boolean1NewColumnName = (String) "<html>new variable:<br>" + 
						boolean1NewName + "</html";
				borderNameBoolean1 = (String) "<html>Recode additional "
						+ "variable: " + boolean1NewName + "</html>";
			}
			// oldVar, nr occur, boolean_old, boolean_new, newVar
			columnNamesBooleanPanel1 = new String[]{oldVarColumnName, 
					"<html>number of<br>occurences</html>", 
					boolean1OldColumnName, 
					boolean1NewColumnName, newVarColumnName};
		}
		// columnNamesBooleanPanel2
		if (booleanCounter >= 2){
			boolean2OldName = variableBox2.getSelectedItem().toString();
			boolean2DataType = Dna.dna.db.getDataType(boolean2OldName, 
					statementType);
			boolean2OldColumnName = (String) "<html>old variable:<br>" + 
					boolean2OldName +"</html";
			if (overwriteVariable2.isSelected() == true){
				boolean2NewName = variableBoxOverwrite2.getSelectedItem().
						toString();
				boolean2NewDataType = boolean2DataType;
				boolean2NewColumnName = (String) "<html>new variable:<br>" + 
						boolean2NewName + "</html";
				borderNameBoolean2 = (String) "<html>Recode additional "
						+ "variable: " + boolean2NewName + "</html>";
			}
			if (createNewVariable2.isSelected() == true){
				boolean2NewName = newVariableTextField2.getText().toString();
				boolean2NewDataType = newVariableType2.getSelectedItem().
						toString();
				boolean2NewColumnName = (String) "<html>new variable:<br>" + 
						boolean2NewName + "</html";
				borderNameBoolean2 = (String) "<html>Recode additional "
						+ "variable: " + boolean2NewName + "</html>";
			}
			// oldVar, nr occur, boolean_old, boolean_new, newVar
			columnNamesBooleanPanel2 = new String[]{oldVarColumnName, 
					"<html>number of<br>occurences</html>", 
					boolean2OldColumnName, 
					boolean2NewColumnName, newVarColumnName};
		}
		// columnNamesBooleanPanel3
		if (booleanCounter >= 3){
			boolean3OldName = variableBox3.getSelectedItem().toString();
			boolean3DataType = Dna.dna.db.getDataType(boolean3OldName, 
					statementType);
			boolean3OldColumnName = (String) "<html>old variable:<br>" + 
					boolean3OldName +"</html";
			if (overwriteVariable3.isSelected() == true){
				boolean3NewName = variableBoxOverwrite3.getSelectedItem().
						toString();
				boolean3NewDataType = boolean3DataType;
				boolean3NewColumnName = (String) "<html>new variable:<br>" + 
						boolean3NewName + "</html";
				borderNameBoolean3 = (String) "<html>Recode additional "
						+ "variable: " + boolean3NewName + "</html>";
			}
			if (createNewVariable3.isSelected() == true){
				boolean3NewName = newVariableTextField3.getText().toString();
				boolean3NewDataType = newVariableType3.getSelectedItem().
						toString();
				boolean3NewColumnName = (String) "<html>new variable:<br>" + 
						boolean3NewName + "</html";
				borderNameBoolean3 = (String) "<html>Recode additional "
						+ "variable: " + boolean3NewName + "</html>";
			}
			// oldVar, nr occur, boolean_old, boolean_new, newVar
			columnNamesBooleanPanel3 = new String[]{oldVarColumnName, 
					"<html>number of<br>occurences</html>", 
					boolean3OldColumnName, 
					boolean3NewColumnName, newVarColumnName};
		}
		// columnNamesBooleanPanel4
		if (booleanCounter >= 4){
			boolean4OldName = variableBox4.getSelectedItem().toString();
			boolean4DataType = Dna.dna.db.getDataType(boolean4OldName, 
					statementType);
			boolean4OldColumnName = (String) "<html>old variable:<br>" + 
					boolean4OldName +"</html";
			if (overwriteVariable4.isSelected() == true){
				boolean4NewName = variableBoxOverwrite4.getSelectedItem().
						toString();
				boolean4NewDataType = boolean4DataType;
				boolean4NewColumnName = (String) "<html>new variable:<br>" + 
						boolean4NewName + "</html";
				borderNameBoolean4 = (String) "<html>Recode additional "
						+ "variable: " + boolean4NewName + "</html>";
			}
			if (createNewVariable4.isSelected() == true){
				boolean4NewName = newVariableTextField4.getText().toString();
				boolean4NewDataType = newVariableType4.getSelectedItem().
						toString();
				boolean4NewColumnName = (String) "<html>new variable:<br>" + 
						boolean4NewName + "</html";
				borderNameBoolean4 = (String) "<html>Recode additional "
						+ "variable: " + boolean4NewName + "</html>";
			}
			// oldVar, nr occur, boolean_old, boolean_new, newVar
			columnNamesBooleanPanel4 = new String[]{oldVarColumnName, 
					"<html>number of<br>occurences</html>", 
					boolean4OldColumnName, 
					boolean4NewColumnName, newVarColumnName};
		}
		// columnNamesBooleanPanel5
		if (booleanCounter >= 5){
			boolean5OldName = variableBox5.getSelectedItem().toString();
			boolean5DataType = Dna.dna.db.getDataType(boolean5OldName, 
					statementType);
			boolean5OldColumnName = (String) "<html>old variable:<br>" + 
					boolean5OldName +"</html";
			if (overwriteVariable5.isSelected() == true){
				boolean5NewName = variableBoxOverwrite5.getSelectedItem().
						toString();
				boolean5NewDataType = boolean5DataType;
				boolean5NewColumnName = (String) "<html>new variable:<br>" + 
						boolean5NewName + "</html";
				borderNameBoolean5 = (String) "<html>Recode additional "
						+ "variable: " + boolean5NewName + "</html>";
			}
			if (createNewVariable5.isSelected() == true){
				boolean5NewName = newVariableTextField5.getText().toString();
				boolean5NewDataType = newVariableType5.getSelectedItem().
						toString();
				boolean5NewColumnName = (String) "<html>new variable:<br>" + 
						boolean5NewName + "</html";
				borderNameBoolean5 = (String) "<html>Recode additional "
						+ "variable: " + boolean5NewName + "</html>";
			}
			// oldVar, nr occur, boolean_old, boolean_new, newVar
			columnNamesBooleanPanel5 = new String[]{oldVarColumnName, 
					"<html>number of<br>occurences</html>", 
					boolean5OldColumnName, 
					boolean5NewColumnName, newVarColumnName};
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
		oldVarEntriesString =  Dna.dna.db.getVariableStringEntries(oldVariableName, 
				statementType);
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
	public void updateVariableListModel() {
		listModel.clear();
		String entries[] = Dna.dna.db.getEntriesFromVariableList(
				statementType, newVariableName);
		for (int i = 0; i < entries.length; i++) {
			String label = entries[i];
			listModel.addElement(label);
		}
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
			columnNamesImport = new String[]{"add entry", newVarColumnName};
		}else{
			columnNamesImport = new String[]{"add entry", oldVarColumnName};
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
							listModel.addElement(newEntry);
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
	 * Check if boolean variable is selected correctly.
	 * 
	 * @return		Boolean; true if selected correctly
	 */
	public boolean booleanSelectedRight(){
		Boolean correct = false;
		// step 1: is boolean-option chosen?
		if (booleanAffectedButton.isSelected() == false){
			correct = true;
		}else{
			// step 2: 1 boolean variable selected 
			if (booleanCounter == 1){
				// see if everything selected/entered right
				if (variableBox1.getSelectedIndex() != -1 &&
						variableSelectedRight(createNewVariable1, 
								overwriteVariable1, newVariableTextField1, 
								variableBoxOverwrite1) == true &&
								newBooleanVariableNameWrittenRight(
										createNewVariable1, 
										newVariableTextField1, 
										statementType) == true){
					correct = true;
				}else{
					correct = false;
				}
				if (createNewVariable1.isSelected() == false && 
						overwriteVariable1.isSelected() == false){
					correct = false;
				}
			}
			// step 3: 2 boolean variables selected
			if (booleanCounter == 2){
				// see if everything selected/entered right
				if (variableBox2.getSelectedIndex() != -1 &&
						variableSelectedRight(createNewVariable2, 
								overwriteVariable2, newVariableTextField2, 
								variableBoxOverwrite2) == true &&
								newBooleanVariableNameWrittenRight(
										createNewVariable2, 
										newVariableTextField2, 
										statementType) == true){
					correct = true;
				}else{
					correct = false;
				}
				if (createNewVariable2.isSelected() == false && 
						overwriteVariable2.isSelected() == false){
					correct = false;
				}
			}
			// step 4: 3 boolean variables selected
			if (booleanCounter == 3){
				// see if everything selected/entered right
				if (variableBox3.getSelectedIndex() != -1 &&
						variableSelectedRight(createNewVariable3, 
								overwriteVariable3, newVariableTextField3, 
								variableBoxOverwrite3) == true &&
								newBooleanVariableNameWrittenRight(
										createNewVariable3, 
										newVariableTextField3, 
										statementType) == true){
					correct = true;
				}else{
					correct = false;
				}
				if (createNewVariable3.isSelected() == false && 
						overwriteVariable3.isSelected() == false){
					correct = false;
				}
			}
			// step 5: 4 boolean variables selected
			if (booleanCounter == 4){
				// see if everything selected/entered right
				if (variableBox4.getSelectedIndex() != -1 &&
						variableSelectedRight(createNewVariable4, 
								overwriteVariable4, newVariableTextField4, 
								variableBoxOverwrite4) == true &&
								newBooleanVariableNameWrittenRight(
										createNewVariable4, 
										newVariableTextField4, 
										statementType) == true){
					correct = true;
				}else{
					correct = false;
				}
				if (createNewVariable4.isSelected() == false && 
						overwriteVariable4.isSelected() == false){
					correct = false;
				}
			}
			// step 6: 5 boolean variables selected
			if (booleanCounter == 5){
				// see if everything selected/entered right
				if (variableBox5.getSelectedIndex() != -1 &&
						variableSelectedRight(createNewVariable5, 
								overwriteVariable5, newVariableTextField5, 
								variableBoxOverwrite5) == true &&
								newBooleanVariableNameWrittenRight(
										createNewVariable5, 
										newVariableTextField5, 
										statementType) == true){
					correct = true;
				}else{
					correct = false;
				}
				if (createNewVariable5.isSelected() == false && 
						overwriteVariable5.isSelected() == false){
					correct = false;
				}
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
				overwriteVariable, newVariableTextField, variableBoxOverwrite);
		boolean b = (boolean) newVariableNameWrittenRight();
		boolean c = (boolean) booleanSelectedRight();
		if (a == true && b == true && c == true){
			nextButton.setEnabled(true);
		}else{
			nextButton.setEnabled(false);
		}
	}

	/**
	 * Create warning if a statement type does not have a boolean/int variable.
	 * 
	 * @param varBox	ComboBox variable with various variables in it
	 */
	public void selectBooleanOrGiveWarning(JComboBox<String> varBox){
		try{
			varBox.removeAllItems();
			String type = (String) typeBox.getSelectedItem();
			if (type != null && !type.equals("")) {
				HashMap<String, String> variables = Dna.dna.db.
						getVariablesByTypes(type, "boolean", "integer");
				Iterator<String> keyIterator = variables.keySet().iterator();
				while (keyIterator.hasNext()){
					String key = keyIterator.next();
					varBox.addItem(key);	
				}
				varBox.setSelectedIndex(0);
			}
		}
		catch(IllegalArgumentException iae){
			// give warning
			String message = "\n This statement type does not have any "
					+ "boolean variables."; 
			JOptionPane.showMessageDialog(new JFrame(), message, "Warning",
					JOptionPane.ERROR_MESSAGE);
			//TODO: set booleanSelected-button to false and remove all?
		}
	}

	/**
	 * Read in file one line at a time.
	 * 
	 * @param f							File
	 * @return							list with read-in lines (one by one)
	 * @throws FileNotFoundException	
	 */
	//TODO: Why does this not work for PC generated txt-files?
	public static ArrayList<String> getArrayListFromString(File f) 
			throws FileNotFoundException {
		Scanner s;
		ArrayList<String> list = new ArrayList<String>();
		s = new Scanner(f);
		while (s.hasNext()) {
			list.add(s.next());
		}
		s.close();
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
				String message = "\n The entries in column 4 are not all "
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
		System.out.println("breakNOw in loop = "+ breakNow);
		return breakNow;
	}
}