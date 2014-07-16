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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;


public class ContradictionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	JPanel selfContPanel;
	JTree tree;
	DefaultMutableTreeNode top;
	JScrollPane treeView;	
	JComboBox typeComboBox1b, variableComboBox1b, variableComboBox2b, booleanComboBox1;
	JButton goButton,  clearButton;
	
	public ContradictionPanel() {
		
		selfContPanel = new JPanel(new BorderLayout());
		this.setLayout(new BorderLayout());
		top = new DefaultMutableTreeNode("Variable 1");
		tree = new JTree(top);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		treeView = new JScrollPane(tree);

		treeView.setPreferredSize(new Dimension(120, 250));

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				try {
					String node = (String) tree.getLastSelectedPathComponent().toString();

					Pattern p = Pattern.compile("(0 \\()|(1 \\()");
					Matcher m = p.matcher(node);
					boolean b = m.find();
					if (b == true) {
						node = node.replaceAll("\\)", "");
						node = node.replaceAll("1 \\(", "");
						node = node.replaceAll("0 \\(", "");
						int nodeInt = new Integer(node).intValue();

						Dna.dna.gui.sidebarPanel.statementFilter.showAll.setSelected(true);
						Dna.dna.gui.sidebarPanel.statementFilter.toggleEnabled(false);
						Dna.dna.gui.sidebarPanel.statementFilter.allFilter();

						int viewId = Dna.dna.gui.sidebarPanel.statementTable.convertRowIndexToView(Dna.dna.gui.sidebarPanel.ssc.getIndexByStatementId(nodeInt));
						if (viewId == -1) {
							Dna.dna.gui.sidebarPanel.statementTable.clearSelection();
						} else {
							Dna.dna.gui.sidebarPanel.statementTable.changeSelection(viewId, 0, false, false);
							Dna.dna.gui.sidebarPanel.highlightStatementInText(nodeInt);
						}
					}
				} catch (NullPointerException npe) { }
			}
		});
	
		this.add(treeView, BorderLayout.NORTH);

		
		// add ComboBox-Filters:
		typeComboBox1b = new JComboBox();
		typeComboBox1b.setPreferredSize(new Dimension(208, 20));
		typeComboBox1b.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateSelfContFilterVars();
			}
		});

		typeComboBox1b.setEnabled(true);

		variableComboBox1b = new JComboBox();
		variableComboBox1b.setPreferredSize(new Dimension(102, 20));
		variableComboBox1b.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateStatementVariables(e);
			}
		});

		variableComboBox2b = new JComboBox();
		variableComboBox2b.setPreferredSize(new Dimension(102, 20));
		variableComboBox2b.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			}
		});


		booleanComboBox1 = new JComboBox();
		booleanComboBox1.setPreferredSize(new Dimension(208, 20));
		booleanComboBox1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			}
		});

		//toggleEnabled(false);
		JPanel filterPanel0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel0.add(typeComboBox1b);
		JPanel filterPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel1.add(variableComboBox1b);
		filterPanel1.add(variableComboBox2b);
		JPanel filterPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterPanel2.add(booleanComboBox1);
		JPanel filterPanel = new JPanel(new BorderLayout());
		filterPanel.add(filterPanel0, BorderLayout.NORTH);
		filterPanel.add(filterPanel1, BorderLayout.CENTER);
		filterPanel.add(filterPanel2, BorderLayout.SOUTH);

		this.add(filterPanel, BorderLayout.CENTER);

		
		// add two buttons
		Icon tickIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		Icon clearIcon = new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png"));
		goButton = new JButton("go", tickIcon);
		clearButton = new JButton("clear", clearIcon);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(goButton);
		buttonPanel.add(clearButton);
		
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
			}
		});

		this.add(buttonPanel, BorderLayout.SOUTH);
	}


	// method: update Self Contradiction Variables
	public void updateSelfContFilterVars() {
		variableComboBox1b.removeAllItems();
		variableComboBox2b.removeAllItems();
		booleanComboBox1.removeAllItems();
		String type = (String) typeComboBox1b.getSelectedItem();
		if (type != null && !type.equals("")) {
			HashMap<String, String> variables = Dna.dna.db.getVariables(type);
			Iterator<String> keyIterator = variables.keySet().iterator();
		    while (keyIterator.hasNext()){
				String key = keyIterator.next();
				variableComboBox2b.addItem(key);				
				variableComboBox1b.addItem(key);
				//TODO: make sure you can only select one ! 
			}
		    variableComboBox1b.setSelectedIndex(-1);
		    variableComboBox2b.setSelectedIndex(-1);
		}
		// Get boolean variable activated:	
		if (type != null && !type.equals("")) {
			HashMap<String, String> variables = Dna.dna.db.getVariablesByType(type, "boolean");
			//LB.Add in DataAccess => new method to get Variables by variableType
			Iterator<String> keyIterator = variables.keySet().iterator();
		    while (keyIterator.hasNext()){
				String key = keyIterator.next();
				booleanComboBox1.addItem(key);
			}
		    booleanComboBox1.setSelectedIndex(0);
		}
	}


	// method updateStatementVariables() makes sure an item cannot be selected in Combobox1b and Combobox2b
	public void updateStatementVariables(ItemEvent e) {		
		if (e.getSource() == variableComboBox1b) {
			String type = (String) typeComboBox1b.getSelectedItem();
			if (type != null && !type.equals("")) {
				HashMap<String, String> variables = Dna.dna.db.getVariables(type);
				String variable1 = (String) variableComboBox1b.getSelectedItem();
				// remove item from HashMap: http://stackoverflow.com/questions/6531132/java-hashmap-removing-key-value
				variables.remove(variable1);
				Iterator<String> keyIterator = variables.keySet().iterator();
				variableComboBox2b.removeAllItems();
				while (keyIterator.hasNext()){
					String key = keyIterator.next();
					variableComboBox2b.addItem(key);
				}
			}
		}
	}
	

	public void clearTree() {
		top = new DefaultMutableTreeNode(variableComboBox1b.getSelectedItem());
		tree.setModel(new DefaultTreeModel(top));
		goButton.setEnabled(true);
		clearButton.setEnabled(true);
	}


	// method: find Contradictions (previously Thread ContradictionReporter)
	//TODO get Int variable entries as well: getVariableIntEntry() // getVariableIntEntries()
	//get data type of variable: getDataType()
	public void findContradictions() {

		String statType = (String) typeComboBox1b.getSelectedItem();
		String var1 = (String) variableComboBox1b.getSelectedItem();
		String var2 = (String) variableComboBox2b.getSelectedItem();
		String varBoolean = (String) booleanComboBox1.getSelectedItem();

		// get list of statement IDs 
		ArrayList<Integer> ids;
		ids = Dna.dna.db.getStatementIdsAll();
		//LB.Add in DataAccess: getStatementIdsAll()

		// get List of actors:
		String[] actors; //Problem if not ArrayList?
		actors = Dna.dna.db.getVariableStringEntries(var1, statType);

		ArrayList<Integer> tabuId = new ArrayList<Integer>();

		// actors-loop
		for (int i = 0; i < actors.length; i++) {
			DefaultMutableTreeNode actor = new DefaultMutableTreeNode(actors[i]);
			ArrayList<Integer> indices = new ArrayList<Integer>();

			// for j = statement IDs!
			for (int j : ids){
				if (actors[i].equals(Dna.dna.db.getVariableStringEntry2(j, var1, statType))) {
					indices.add(j);
				}
			}

			for (int j : indices) {
				for (int k :indices) {
					if (
							j != k && 
							! tabuId.contains(j) && 
							! tabuId.contains(k) && 
							Dna.dna.db.getVariableStringEntry2(j, var2, statType).equals(Dna.dna.db.getVariableStringEntry2(k, var2, statType)) && 
							! Dna.dna.db.getVariableStringEntry2(j, varBoolean, statType).equals(Dna.dna.db.getVariableStringEntry2(k, varBoolean, statType))
							) {
						DefaultMutableTreeNode category = new DefaultMutableTreeNode(Dna.dna.db.getVariableStringEntry2(j, var2, statType));
						ArrayList<Integer> matches = new ArrayList<Integer>();
						for (int l: indices) {
							if (Dna.dna.db.getVariableStringEntry2(l, var2, statType).equals(Dna.dna.db.getVariableStringEntry2(j, var2, statType))){
								matches.add(l);
								DefaultMutableTreeNode id = new DefaultMutableTreeNode(Dna.dna.db.getVariableStringEntry2(l, varBoolean, statType) + 	
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
			DefaultMutableTreeNode message = new DefaultMutableTreeNode("No contradictions found!");
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





