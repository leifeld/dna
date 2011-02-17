package dna;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ProgressMonitor;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

@SuppressWarnings("serial")
public class ContradictionReporter extends JPanel {
	
	JTree tree;
	JScrollPane treeView;
	DefaultMutableTreeNode top;
	JButton persButton, orgButton, clearButton;
	
	public ContradictionReporter() {
		
		this.setLayout(new BorderLayout());
		
		top = new DefaultMutableTreeNode("Actors");
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		treeView = new JScrollPane(tree);
		
		treeView.setPreferredSize(new Dimension(120, 250));
		
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
						
						Dna.mainProgram.statementFilter.allMenuItem.setSelected(true);
						Dna.mainProgram.statementFilter.textField.setText("");
						Dna.mainProgram.statementFilter.textField.setEnabled(false);
						Dna.mainProgram.statementFilter.allFilter();
						
						int viewId = Dna.mainProgram.statementTable.convertRowIndexToView(Dna.mainProgram.dc.sc.getIndexById(nodeInt));
						if (viewId == -1) {
							Dna.mainProgram.statementTable.clearSelection();
						} else {
							Dna.mainProgram.statementTable.changeSelection(viewId, 0, false, false);
							Dna.mainProgram.highlightStatementInText(nodeInt);
						}
					}
				} catch (NullPointerException npe) { }
			}
		});
		
		this.add(treeView, BorderLayout.CENTER);
		
		Icon contrPersIcon = new ImageIcon(getClass().getResource("/icons/user.png"));
		Icon contrOrgIcon = new ImageIcon(getClass().getResource("/icons/group.png"));
		Icon clearIcon = new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png"));
		persButton = new JButton("Pers", contrPersIcon);
		orgButton = new JButton("Org", contrOrgIcon);
		clearButton = new JButton(clearIcon);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(persButton);
		buttonPanel.add(orgButton);
		buttonPanel.add(clearButton);
		
		clearButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearTree();
			}
		});
		
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		ActionListener buttonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (e.getSource() == persButton) {
					Thread generateThread = new Thread( new ReportGenerator(true), "Create self-contradiction report" );
					generateThread.start();
				} else if (e.getSource() == orgButton) {
					Thread generateThread = new Thread( new ReportGenerator(false), "Create self-contradiction report" );
					generateThread.start();
				}
			}
		};
		
		persButton.addActionListener(buttonListener);
		orgButton.addActionListener(buttonListener);
	}
	
	public void clearTree() {
		top = new DefaultMutableTreeNode("Actors");
		tree.setModel(new DefaultTreeModel(top));
		persButton.setEnabled(true);
		orgButton.setEnabled(true);
	}
	
	class ReportGenerator implements Runnable {
		
		ProgressMonitor progressMonitor;
		boolean persons;
		
		public ReportGenerator(boolean persons) {
			this.persons = persons;
		}
		
		public void run() {
			
			persButton.setEnabled(false);
			orgButton.setEnabled(false);
			
			ArrayList<String> actors;
			if (persons == true) {
				actors = Dna.mainProgram.dc.sc.getPersonList();
			} else {
				actors = Dna.mainProgram.dc.sc.getOrganizationList();
			}
			
			progressMonitor = new ProgressMonitor(ContradictionReporter.this, "Looking for within-actor contradictions...", "", 0, actors.size() - 1 );
			progressMonitor.setMillisToDecideToPopup(1);
			
			ArrayList<Integer> tabuId = new ArrayList<Integer>();
			
			for (int i = 0; i < actors.size(); i++) {
				DefaultMutableTreeNode actor = new DefaultMutableTreeNode(actors.get(i));
				progressMonitor.setProgress(i);
				if (progressMonitor.isCanceled()) {
					break;
				}
				ArrayList<Integer> indices = new ArrayList<Integer>();
				for (int j = 0; j < Dna.mainProgram.dc.sc.size(); j++) {
					if (persons == true) {
						if (actors.get(i).equals(Dna.mainProgram.dc.sc.get(j).getPerson())) {
							indices.add(j);
						}
					} else {
						if (actors.get(i).equals(Dna.mainProgram.dc.sc.get(j).getOrganization())) {
							indices.add(j);
						}
					}
				}
				for (int j = 0; j < indices.size(); j++) {
					for (int k = 0; k < indices.size(); k++) {
						if (
								indices.get(j) != indices.get(k) && 
								! tabuId.contains(indices.get(j)) && 
								! tabuId.contains(indices.get(k)) && 
								Dna.mainProgram.dc.sc.get(indices.get(j)).getCategory().equals(Dna.mainProgram.dc.sc.get(indices.get(k)).getCategory()) && 
								! Dna.mainProgram.dc.sc.get(indices.get(j)).getAgreement().equals(Dna.mainProgram.dc.sc.get(indices.get(k)).getAgreement())
						) {
							DefaultMutableTreeNode category = new DefaultMutableTreeNode(Dna.mainProgram.dc.sc.get(indices.get(j)).getCategory());
							ArrayList<Integer> matches = new ArrayList<Integer>();
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
}