package dna;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import dna.dataStructures.Statement;

public class ContradictionPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	JPanel selfContPanel;
	JTree tree;
	DefaultMutableTreeNode top;
	JScrollPane treeView;	
	JComboBox<String> filterComboBoxType, filterComboBoxVar1, 
	filterComboBoxVar2, filterComboBoxBoolean;
	JButton goButton,  clearButton;

	public ContradictionPanel() {
		selfContPanel = new JPanel(new BorderLayout());
		this.setLayout(new BorderLayout());
		top = new DefaultMutableTreeNode("Variable 1");
		tree = new JTree(top);

		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		treeView = new JScrollPane(tree);
		treeView.setPreferredSize(new Dimension(120, 250));

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				try {
					String node = (String) tree.getLastSelectedPathComponent().
							toString();
					Pattern p = Pattern.compile("(0 \\()|(1 \\()");
					Matcher m = p.matcher(node);
					boolean b = m.find();
					if (b == true) {
						node = node.replaceAll("\\)", "");
						node = node.replaceAll("1 \\(", "");
						node = node.replaceAll("0 \\(", "");
						int nodeInt = Integer.parseInt(node);

						Dna.gui.rightPanel.statementPanel.statementFilter.showAll.
						setSelected(true);
						//Dna.dna.gui.rightPanel.statementPanel.statementFilter.toggleEnabled(false);
						Dna.gui.rightPanel.statementPanel.statementFilter.allFilter();

						int viewId = Dna.gui.rightPanel.statementPanel.statementTable.convertRowIndexToView(
								Dna.gui.rightPanel.statementPanel.ssc.getIndexByStatementId(nodeInt));
						if (viewId == -1) {
							Dna.gui.rightPanel.statementPanel.statementTable.clearSelection();
						} else {
							Dna.gui.rightPanel.statementPanel.statementTable.changeSelection(viewId, 0, false, false);
							int docId = Dna.data.getStatement(nodeInt).getDocumentId();
							int docModelIndex = Dna.gui.documentPanel.documentContainer.getModelIndexById(docId);
							int docRow = Dna.gui.documentPanel.documentTable.convertRowIndexToView(docModelIndex);
							//int docRow = Dna.dna.gui.documentPanel.documentContainer.getRowIndexById(docId);
							Dna.gui.documentPanel.documentTable.getSelectionModel().
							setSelectionInterval(docRow, docRow);
							Dna.gui.textPanel.selectStatement(nodeInt, docId, true);
						}
					}
				} catch (NullPointerException npe) { }
			}
		});
		this.add(treeView, BorderLayout.NORTH);

		// add ComboBox-Filters:
		filterComboBoxType = new JComboBox<String>();
		filterComboBoxType.setPreferredSize(new Dimension(208, 20));
		filterComboBoxType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateSelfContFilterVars();
			}
		});
		filterComboBoxType.setEnabled(true);

		JLabel filterComboBoxVar1Label = new JLabel("Variable 1");
		filterComboBoxVar1 = new JComboBox<String>();
		filterComboBoxVar1.setPreferredSize(new Dimension(102, 20));
		filterComboBoxVar1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				top = new DefaultMutableTreeNode(
						filterComboBoxVar1.getSelectedItem());
				tree.setModel(new DefaultTreeModel(top));
				updateStatementVariables(e);
			}
		});

		JLabel filterComboBoxVar2Label = new JLabel("Variable 2");
		filterComboBoxVar2 = new JComboBox<String>();
		filterComboBoxVar2.setPreferredSize(new Dimension(102, 20));

		JLabel filterComboBoxBooleanLabel = new JLabel("Boolean   ");
		filterComboBoxBoolean = new JComboBox<String>();
		filterComboBoxBoolean.setPreferredSize(new Dimension(102, 20));

		JPanel varPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varPanel1.add(filterComboBoxVar1Label);
		varPanel1.add(filterComboBoxVar1);
		JPanel varPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varPanel2.add(filterComboBoxVar2Label);
		varPanel2.add(filterComboBoxVar2);
		JPanel varPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varPanel3.add(filterComboBoxBooleanLabel);
		varPanel3.add(filterComboBoxBoolean);
		JPanel varPanel = new JPanel(new BorderLayout());
		varPanel.add(varPanel1, BorderLayout.NORTH);
		varPanel.add(varPanel2, BorderLayout.CENTER);
		varPanel.add(varPanel3, BorderLayout.SOUTH);

		JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		typePanel.add(filterComboBoxType);

		JPanel filterPanel = new JPanel(new BorderLayout());
		filterPanel.add(typePanel, BorderLayout.NORTH);
		filterPanel.add(varPanel, BorderLayout.SOUTH);
		this.add(filterPanel, BorderLayout.CENTER);

		// add two buttons
		Icon tickIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		Icon clearIcon = new ImageIcon(getClass().getResource(
				"/icons/arrow_rotate_clockwise.png"));
		goButton = new JButton("go", tickIcon);
		clearButton = new JButton("clear", clearIcon);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(goButton);
		buttonPanel.add(clearButton);
		goButton.setEnabled(false);
		clearButton.setEnabled(false);

		// add task for clear-button
		clearButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearTree();
			}
		});

		// add task for go-button
		goButton.addActionListener(new  ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findContradictions();
				goButton.setEnabled(false);
			}
		});
		this.add(buttonPanel, BorderLayout.SOUTH);
	}

	/*
	 * method: update Self Contradiction Variables
	 */
	public void updateSelfContFilterVars() {
		filterComboBoxVar1.removeAllItems();
		filterComboBoxVar2.removeAllItems();
		String type = (String) filterComboBoxType.getSelectedItem();
		if (type != null && !type.equals("")) {
			//HashMap<String, String> variables = Dna.dna.db.getVariables(type);
			HashMap<String, String> variables = Dna.data.getStatementType(type).getVariables();
			Iterator<String> keyIterator = variables.keySet().iterator();
			while (keyIterator.hasNext()){
				String key = keyIterator.next();
				filterComboBoxVar1.addItem(key);
				//not necessary, filterComboBoxVar2 is filled in updateStatementVariables
				//filterComboBoxVar2.addItem(key);
			}
			try{
				filterComboBoxVar1.setSelectedIndex(0);
			}
			catch (IllegalArgumentException ex){
				goButton.setEnabled(false);
				clearButton.setEnabled(false);
			}
			/*
			try{
				filterComboBoxVar2.setSelectedIndex(0);
			}
			catch (IllegalArgumentException ex){
				goButton.setEnabled(false);
				clearButton.setEnabled(false);
			}
			*/
			goButton.setEnabled(true);
			clearButton.setEnabled(true);
		}
		// Get boolean variable activated:	
		freezeOkButton();
	}

	/*
	 * method: if there are no Boolean variables in a statement type =>
	 * the ok-button is not activated
	 */
	public void freezeOkButton() {
		filterComboBoxBoolean.removeAllItems();
		String type = (String) filterComboBoxType.getSelectedItem();
		if (type != null && !type.equals("")) {
			ArrayList<String> variables = Dna.data.getStatementType(type).getVariablesByType("boolean");
			if (variables.size() == 0) {
				goButton.setEnabled(false);
				clearButton.setEnabled(false);
			}
			for (int i = 0; i < variables.size(); i++) {
				filterComboBoxBoolean.addItem(variables.get(i));
				filterComboBoxBoolean.setSelectedIndex(0);
			}
		}
		else {
			goButton.setEnabled(false);
			clearButton.setEnabled(false);
		}
	}

	/*
	 * method updateStatementVariables() makes sure an item cannot be selected 
	 * in filterComboBoxVar1 and filterComboBoxVar2
	 */ 
	public void updateStatementVariables(ItemEvent e) {	
		if (e.getSource() == filterComboBoxVar1) {
			filterComboBoxVar2.removeAllItems();
			String type = (String) filterComboBoxType.getSelectedItem();
			if (type != null && !type.equals("")) {
				HashMap<String, String> variables = Dna.data.getStatementType(type).getVariables();
				String variable1 = (String) filterComboBoxVar1.getSelectedItem();
				// remove item from HashMap: http://stackoverflow.com/questions/6531132/java-hashmap-removing-key-value
				variables.remove(variable1);
				Iterator<String> keyIterator = variables.keySet().iterator();
				while (keyIterator.hasNext()){	
					String key = keyIterator.next();
					filterComboBoxVar2.addItem(key);
				}
			}
		}
	}

	/*
	 * method: clear self-contradiction tree to restart search for self-contr.
	 */
	public void clearTree() {
		top = new DefaultMutableTreeNode(filterComboBoxVar1.getSelectedItem());
		tree.setModel(new DefaultTreeModel(top));
		filterComboBoxVar1.setSelectedIndex(0);
		filterComboBoxVar2.setSelectedIndex(0);
		goButton.setEnabled(true);
		freezeOkButton();
		clearButton.setEnabled(true);
	}

	/*
	 * method: find Contradictions (previously Thread ContradictionReporter)
	 */
	public void findContradictions() {
		String statType = (String) filterComboBoxType.getSelectedItem();
		int statTypeId = -1;
		for (int i = 0; i < Dna.data.getStatementTypes().size(); i++) {
			if (Dna.data.getStatementTypes().get(i).getLabel().equals(statType)) {
				statTypeId = Dna.data.getStatementTypes().get(i).getId();
			}
		}
		String var1 = (String) filterComboBoxVar1.getSelectedItem();
		String var2 = (String) filterComboBoxVar2.getSelectedItem();
		String varBoolean = (String) filterComboBoxBoolean.getSelectedItem();

		// get list of statement IDs 
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < Dna.data.getStatements().size(); i++) {
			ids.add(Dna.data.getStatements().get(i).getId());
		}

		// get List of actors:
		ArrayList<String> actors = new ArrayList<String>();
		ArrayList<Statement> statements = Dna.data.getStatementsByStatementTypeId(statTypeId);
		//String[] actors = new String[statements.size()];
		for (int i = 0; i < statements.size(); i++) {
			String a = (String) Dna.data.getStatements().get(i).getValues().get(var1);
			if (!actors.contains(a)) {
				actors.add(a);
			}
		}
		//String[] actors; //Problem if not ArrayList?
		//actors = Dna.dna.db.getVariableStringEntries(var1, statType);
		ArrayList<Integer> tabuId = new ArrayList<Integer>();

		// actors-loop
		for (int i = 0; i < actors.size(); i++) {
			DefaultMutableTreeNode actor = new DefaultMutableTreeNode(actors.get(i));
			ArrayList<Integer> indices = new ArrayList<Integer>();

			// for j = statement IDs
			for (int j : ids){
				//if (actors.get(i).equals(Dna.dna.db.getVariableStringEntryWithType(j, var1, statType))) {
				if (actors.get(i).equals(Dna.data.getStatement(j).getValues().get(var1))) {
					indices.add(j);
				}
			}
			for (int j : indices) {
				for (int k :indices) {
					if (
							j != k && 
							! tabuId.contains(j) && 
							! tabuId.contains(k) && 
							//Dna.dna.db.getVariableStringEntryWithType(j, var2, statType).equals(Dna.dna.db.getVariableStringEntryWithType(k, var2, statType)) &&

							Dna.data.getStatement(j).getValues().get(var2).equals(Dna.data.getStatement(k).getValues().get(var2)) && 
							//! Dna.dna.db.getVariableStringEntryWithType(j, varBoolean, statType).equals(Dna.dna.db.getVariableStringEntryWithType(k, varBoolean, statType))
							Dna.data.getStatement(j).getValues().get(varBoolean).equals(Dna.data.getStatement(k).getValues().get(varBoolean))
							) {
						//DefaultMutableTreeNode category = new DefaultMutableTreeNode(
						//		Dna.dna.db.getVariableStringEntryWithType(
						//				j, var2, statType));
						DefaultMutableTreeNode category = new DefaultMutableTreeNode(Dna.data.getStatement(j).getValues().get(var2));
						ArrayList<Integer> matches = new ArrayList<Integer>();
						for (int l: indices) {
							//if (Dna.dna.db.getVariableStringEntryWithType(
							//		l, var2, statType)
							//		.equals(Dna.dna.db.getVariableStringEntryWithType(
							//				j, var2, statType))){
							if (Dna.data.getStatement(l).getValues().get(var2).equals(Dna.data.getStatement(j).getValues().get(var2))) {
								matches.add(l);
								DefaultMutableTreeNode id = new DefaultMutableTreeNode(
										//Dna.dna.db.getVariableStringEntryWithType(
										//		l, varBoolean, statType) +
										Dna.data.getStatement(l).getValues().get(varBoolean) + 
												" (" + l + ")");
								category.add(id);
							}
						}
						tabuId.addAll(matches);
						if (category.getChildCount() > 0) {
							actor.add(category);
						}
					}
				}//closes k-loop
			}//closes j-loop
			if (actor.getChildCount() > 0) {
				top.add(actor);
			}
		}//closes i-loop
		if (top.getChildCount() == 0) {
			DefaultMutableTreeNode message = new DefaultMutableTreeNode(
					"No contradictions found!");
			top.add(message);
		}
		// display first node in tree: 
		tree.expandRow(0);
		/*//display the entire tree:
		 * for (int d = 0; d < tree.getRowCount(); d++) {
		 * tree.expandRow(d);
		 * }
		 */
	}
}