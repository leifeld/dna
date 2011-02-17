package dna;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ProgressMonitor;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class ContradictionReporter extends JFrame {
	
	JTree tree;
	DefaultMutableTreeNode top;
	Container c;
	boolean persons;
	
	public ContradictionReporter(boolean persons) {
		
		this.persons = persons;
		c = getContentPane();
		this.setTitle("Within-actor contradictions");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon reportIcon;
		if (persons == true) {
			reportIcon = new ImageIcon(getClass().getResource("/icons/user.png"));
			top = new DefaultMutableTreeNode("Persons");
		} else {
			reportIcon = new ImageIcon(getClass().getResource("/icons/group.png"));
			top = new DefaultMutableTreeNode("Organizations");
		}
		this.setIconImage(reportIcon.getImage());
		
		JPanel reportPanel = new JPanel(new BorderLayout());
		
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		JScrollPane treeView = new JScrollPane(tree);
		treeView.setPreferredSize(new Dimension(350, 400));
		
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
						int viewId = Dna.mainProgram.statementTable.convertRowIndexToView(Dna.mainProgram.sc.getIndexById(nodeInt));
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
		
		reportPanel.add(treeView, BorderLayout.CENTER);
		
		c.add(reportPanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
		try {
			Thread generateThread = new Thread( new ReportGenerator(), "Create self-contradiction report" );
			generateThread.start();
		} catch (Exception ex) {
			System.err.println("There was a problem while generating the report: " + ex.getStackTrace());
			JOptionPane.showMessageDialog(ContradictionReporter.this, "There was a problem while generating the report: " + ex.getStackTrace());
		}
	}
	
	class ReportGenerator implements Runnable {
		
		ProgressMonitor progressMonitor;
		
		public void run() {
			
			ArrayList<String> actors;
			if (persons == true) {
				actors = Dna.mainProgram.sc.getPersonList();
			} else {
				actors = Dna.mainProgram.sc.getOrganizationList();
			}
			
			progressMonitor = new ProgressMonitor(ContradictionReporter.this, "Looking for within-actor contradictions...", "", 0, actors.size() - 1 );
			//progressMonitor.setMillisToDecideToPopup(1);
			
			ArrayList<Integer> tabuId = new ArrayList<Integer>();
			
			for (int i = 0; i < actors.size(); i++) {
				DefaultMutableTreeNode actor = new DefaultMutableTreeNode(actors.get(i));
				progressMonitor.setProgress(i);
				if (progressMonitor.isCanceled()) {
					break;
				}
				ArrayList<Integer> indices = new ArrayList<Integer>();
				for (int j = 0; j < Dna.mainProgram.sc.size(); j++) {
					if (persons == true) {
						if (actors.get(i).equals(Dna.mainProgram.sc.get(j).getPerson())) {
							indices.add(j);
						}
					} else {
						if (actors.get(i).equals(Dna.mainProgram.sc.get(j).getOrganization())) {
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
								Dna.mainProgram.sc.get(indices.get(j)).getCategory().equals(Dna.mainProgram.sc.get(indices.get(k)).getCategory()) && 
								! Dna.mainProgram.sc.get(indices.get(j)).getAgreement().equals(Dna.mainProgram.sc.get(indices.get(k)).getAgreement())
						) {
							DefaultMutableTreeNode category = new DefaultMutableTreeNode(Dna.mainProgram.sc.get(indices.get(j)).getCategory());
							ArrayList<Integer> matches = new ArrayList<Integer>();
							for (int l = 0; l < indices.size(); l++) {
								if (Dna.mainProgram.sc.get(indices.get(l)).getCategory().equals(Dna.mainProgram.sc.get(indices.get(j)).getCategory())) {
									matches.add(indices.get(l));
									DefaultMutableTreeNode id = new DefaultMutableTreeNode(Dna.mainProgram.sc.get(indices.get(l)).getAgreement() + " (" + Dna.mainProgram.sc.get(indices.get(l)).getId() + ")");
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