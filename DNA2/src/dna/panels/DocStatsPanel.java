package dna.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import dna.Dna;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementType;

@SuppressWarnings("serial")
public class DocStatsPanel extends JPanel {
	JTextArea tf;
	public JButton refreshButton, clearButton;
	JScrollPane scroll;
	
	public DocStatsPanel() {
		this.setLayout(new BorderLayout());
		
		tf = new JTextArea(7, 12);
		scroll = new JScrollPane (tf, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		DefaultCaret caret = (DefaultCaret) tf.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		tf.setEditable(false);
		ImageIcon clearIcon = new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png"));
		clearButton = new JButton("clear", clearIcon);
		clearButton.setEnabled(false);
		clearButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear();
				clearButton.setEnabled(false);
			}
		});
		ImageIcon refreshIcon = new ImageIcon(getClass().getResource("/icons/chart_bar.png"));
		refreshButton = new JButton("refresh", refreshIcon);
		refreshButton.setEnabled(false);
		refreshButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				computeStats();
				clearButton.setEnabled(true);
			}
		});
		
		this.add(scroll, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(clearButton);
		buttonPanel.add(refreshButton);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public void computeStats() {
		clear();
		int numDocuments = Dna.data.getDocuments().size();
		int numStatements = Dna.gui.rightPanel.statementPanel.ssc.getRowCount();
		//int statementLinks = Dna.dna.gui.rightPanel.linkedTableModel.getRowCount();
		
		String statText = "Documents: " + numDocuments + "\n"
				+ "Statements: " + numStatements + "\n";
				//+ "Statement Links: " + statementLinks + "\n";
		
		for (StatementType st : Dna.data.getStatementTypes()) {
			statText = statText + "\n\"" + st.getLabel() + "\" Variables:\n";
			String[] vars = st.getVariables().keySet().toArray(new String[st.getVariables().keySet().size()]);
			ArrayList<Statement> s = Dna.data.getStatementsByStatementTypeId(st.getId());
			for (int j = 0; j < vars.length; j++) {
				ArrayList<Object> varEntries = new ArrayList<Object>();
				for (int i = 0; i < s.size(); i++) {
					if (!varEntries.contains(s.get(i).getValues().get(vars[j]))) {
						varEntries.add(s.get(i).getValues().get(vars[j]));
					}
				}
				int count = varEntries.size();
				statText = statText + "     " + vars[j] + ": " + count + "\n";
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

	/*
	public void updateStatistics() {
		clear();
		refreshButton.setEnabled(true);
		computeStats();
	}
	*/
}
