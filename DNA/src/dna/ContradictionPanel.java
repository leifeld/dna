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

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ProgressMonitor;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;



public class ContradictionPanel extends JPanel {

	//TODO: what is that?
	private static final long serialVersionUID = 1L;
	
	
	JPanel selfContPanel;
	JTree tree;
	DefaultMutableTreeNode top;
	JScrollPane treeView;	
	JComboBox typeComboBox1b;
	JComboBox variableComboBox1b;
	JComboBox variableComboBox2b;
	JComboBox booleanComboBox1;

	public ContradictionPanel() {
		

		selfContPanel = new JPanel(new BorderLayout());
		this.setLayout(new BorderLayout());
		
		top = new DefaultMutableTreeNode("Variable 1");
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		treeView = new JScrollPane(tree);

		treeView.setPreferredSize(new Dimension(120, 250));

		// add TreeSectionListener
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				try {
					String node = (String) tree.getLastSelectedPathComponent().toString();

					Pattern p = Pattern.compile("(yes \\()|(no \\()");
					Matcher m = p.matcher(node);
					boolean b = m.find();
					if (b == true) {
						node = node.replaceAll("\\)", "");
						node = node.replaceAll("yes \\(", "");
						node = node.replaceAll("no \\(", "");
						int nodeInt = new Integer(node).intValue();

						//TODO: prüfen ob das sinn macht noch so
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


		//
		this.add(treeView, BorderLayout.CENTER);

		// add Filters:
		// add Buttons for Statement-Choice
		typeComboBox1b = new JComboBox();
		typeComboBox1b.setPreferredSize(new Dimension(208, 20));
		typeComboBox1b.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateSelfContFilterVars();
				// je nach item soll was passieren:
				//	updateVariables();
				//	filter();
			}

		});

		typeComboBox1b.setEnabled(true);


		variableComboBox1b = new JComboBox();
		variableComboBox1b.setPreferredSize(new Dimension(102, 20));
		variableComboBox1b.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				//TODO: give it a task
				//	filter();
				//			jComboBox1ItemStateChanged();
			}
		});

		variableComboBox2b = new JComboBox();
		variableComboBox2b.setPreferredSize(new Dimension(102, 20));
		variableComboBox2b.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				//	filter();
				//			jComboBox1ItemStateChanged();
			}
		});


		booleanComboBox1 = new JComboBox();
		booleanComboBox1.setPreferredSize(new Dimension(208, 20));

		booleanComboBox1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				//	filter();
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

		this.add(filterPanel, BorderLayout.SOUTH);


		//LB:TODO: create a "go"-Button => and give him an action
		//Add an ActionListener to the two ComboBox-variables
		/*			ActionListener buttonListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {					
					if (e.getSource() == variableComboBox1b) {
						Thread generateThread = new Thread( new ReportGenerator(true), "Create self-contradiction report" );
						generateThread.start();
					} else if (e.getSource() == variableComboBox2b) {
						Thread generateThread = new Thread( new ReportGenerator(false), "Create self-contradiction report" );
						generateThread.start();
					}
				}
			};

			variableComboBox1b.addActionListener(buttonListener);
			variableComboBox2b.addActionListener(buttonListener);
		 */
		

	}



	/*
	 * LB.Add:
	 */
	public void updateSelfContFilterVars() {
		//TODO: wenn 1 item in variableComboBox1b/2b ausgewählt ist => darf dieses im 2. nicht erscheinen! => 
		// search ComboBox => dort steht, wie man ein item aussschliessen kann
		variableComboBox1b.removeAllItems();
		variableComboBox2b.removeAllItems();
		booleanComboBox1.removeAllItems();
		String type = (String) typeComboBox1b.getSelectedItem();
		if (type != null && !type.equals("")) {
			HashMap<String, String> variables = Dna.dna.db.getVariables(type);
			Iterator<String> keyIterator = variables.keySet().iterator();
		    while (keyIterator.hasNext()){
				String key = keyIterator.next();
				variableComboBox1b.addItem(key);
				variableComboBox2b.addItem(key);				
				//TODO: make sure you can only select one ! 
				//String selectBox1b = (String) variableComboBox1b.getSelectedItem();
				//variableComboBox2b.removeItem(selectBox1b);
			}
		    variableComboBox1b.setSelectedIndex(0);
		    variableComboBox2b.setSelectedIndex(1);
			//evtl. 2.Box auf (1) stellen??
		}
		// Get boolean variable activated:	
		if (type != null && !type.equals("")) {
			//LB.Add in DataAccess => new method to get Variables by variableType
			HashMap<String, String> variables = Dna.dna.db.getVariablesByType(type, "boolean");
			Iterator<String> keyIterator = variables.keySet().iterator();
		    while (keyIterator.hasNext()){
				String key = keyIterator.next();
				booleanComboBox1.addItem(key);
			}
		    booleanComboBox1.setSelectedIndex(0);
		}
	}

	

	
	
	
	/* NEW CLASS
	 * LB Add: ReportGenerator-class
	 * alternative: simple class
	 */
/*	class ReportGenerator implements Runnable {
		
		//what is that?
		ProgressMonitor progressMonitor;
		//???
		boolean persons;
		
		//TODO: what does that do? => does it activate run()?
		public ReportGenerator(boolean persons) {
			this.persons = persons;
		}
		
		public void run() {
			
			
			//TODO: needed?
			variableComboBox1b.setEnabled(false);
			variableComboBox2b.setEnabled(false);

			String statType = (String) typeComboBox1b.getSelectedItem();
			String var1 = (String) variableComboBox1b.getSelectedItem();
			String var2 = (String) variableComboBox2b.getSelectedItem();
			String varBoolean = (String) booleanComboBox1.getSelectedItem();
			
			// get actors
			String[] actors; //Problem if not ArrayList?
			if (persons == true) {
				//OLD: actors = Dna.mainProgram.dc.sc.getPersonList();				
				// actors = entries from variable 1
				actors = Dna.dna.db.getVariableStringEntries(var1, statType);
			}
			
			progressMonitor = new ProgressMonitor(SidebarPanel.this, "Looking for within-actor contradictions...", "", 0, actors.length - 1 );
			progressMonitor.setMillisToDecideToPopup(1);
			
			//??
			ArrayList<Integer> tabuId = new ArrayList<Integer>();
			
			//??
			for (int i = 0; i < actors.length; i++) {
				DefaultMutableTreeNode actor = new DefaultMutableTreeNode(actors[i]);
				progressMonitor.setProgress(i);
				if (progressMonitor.isCanceled()) {
					break;
				}			
				// indices: wie viele Personen mit mind. 2 statements??
				ArrayList<Integer> indices = new ArrayList<Integer>();
				//TODO: get path to size() right => statement size = ja! => is that right?
				// ssc = sidebar statement container
				for (int j = 0; j < ssc.size(); j++) {
						//TODO: get path right => not Person => but get whatever is in var1
						if (actors[i].equals(Dna.dna.db.getVariableStringEntries(var1, statType)[j])) {
							indices.add(j);
						}
					} 
			
			
				// => here comes getCategory() => but we want Variable2
				for (int j = 0; j < indices.size(); j++) {
					for (int k = 0; k < indices.size(); k++) {
						if (
								indices.get(j) != indices.get(k) && 
								! tabuId.contains(indices.get(j)) && 
								! tabuId.contains(indices.get(k)) && 
								Dna.dna.db.getVariableStringEntries(var2, statType)[j].equals(Dna.dna.db.getVariableStringEntries(var1, statType)[k]) && 
								! Dna.mainProgram.dc.sc.get(indices.get(j)).getAgreement().equals(Dna.mainProgram.dc.sc.get(indices.get(k)).getAgreement())
						) {
							DefaultMutableTreeNode category = new DefaultMutableTreeNode(Dna.dna.db.getVariableStringEntries(var1, statType)[j]);
							ArrayList<Integer> matches = new ArrayList<Integer>();
							//display agreement and in brackets the ID of the statement
							for (int l = 0; l < indices.size(); l++) {
								if (Dna.mainProgram.dc.sc.get(indices.get(l)).getCategory().equals(Dna.mainProgram.dc.sc.get(indices.get(j)).getCategory())) {
									matches.add(indices.get(l));
									DefaultMutableTreeNode id = new DefaultMutableTreeNode(Dna.mainProgram.dc.sc.get(indices.get(l)).getAgreement() + " (" + Dna.mainProgram.dc.sc.get(indices.get(l)).getId() + ")");
									category.add(id);
								}
							}
							tabuId.addAll(matches);
							if (category.getChildCount() > 0) {
								actor.add(category);
							}
						}
					}
				}
				if (actor.getChildCount() > 0) {
					top.add(actor);
				}
			}
			if (top.getChildCount() == 0) {
				DefaultMutableTreeNode message = new DefaultMutableTreeNode("No contradictions found!");
				top.add(message);
			}
			tree.expandRow(0);
		}
	}
*/	
		
}
