package dna;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import dna.dataStructures.StatementType;

public class DocStats extends JPanel {
	
	/**
	 * Document statistics side panel.
	 */
	private static final long serialVersionUID = 1L;
	JTextArea tf;
	JButton refreshButton, clearButton;
	JScrollPane scroll;
	
	public DocStats() {
		this.setLayout(new BorderLayout());
		
		tf = new JTextArea(7, 12);
		scroll = new JScrollPane (tf, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		DefaultCaret caret = (DefaultCaret) tf.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		tf.setEditable(false);
		ImageIcon clearIcon = new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png"));
		clearButton = new JButton("clear", clearIcon);
		clearButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		ImageIcon refreshIcon = new ImageIcon(getClass().getResource("/icons/chart_bar.png"));
		refreshButton = new JButton("refresh", refreshIcon);
		refreshButton.setEnabled(false);
		refreshButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Dna.dna.db != null) {
					computeStats();
				}
			}
		});
		
		this.add(scroll, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(clearButton);
		buttonPanel.add(refreshButton);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public void computeStats() {
		int numDocuments = Dna.dna.data.getDocuments().size();
		int numStatements = Dna.dna.gui.sidebarPanel.ssc.getRowCount();
		int statementLinks = Dna.dna.gui.sidebarPanel.linkedTableModel.getRowCount();
		
		String statText = "Documents: " + numDocuments + "\n"
				+ "Statements: " + numStatements + "\n"
				+ "Statement Links: " + statementLinks + "\n";
		
		for (StatementType st: Dna.dna.db.getStatementTypes()) {
			statText = statText + "\n\"" + st.getLabel() + "\" Variables:\n";
			for (String var : st.getVariables().keySet()) {
				statText = statText + "     " + var + ": " + Dna.dna.db.getVariableEntriesCount(var, st.getLabel()) + "\n";
			}
		}

		tf.setEditable(true);
		tf.setText(statText);
		tf.setEditable(false);
	}
	
	public void clear() {
		tf.setEditable(true);
		tf.setText("");
		tf.setEditable(false);
		tf.revalidate();
	}
	
	public void updateStatistics() {
		clear();
		refreshButton.setEnabled(true);
		computeStats();
	}
	
}
