package guiCoder;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTable;

import dna.Dna;
import dna.Dna.CoderListener;
import dna.Dna.SqlListener;

public class StatementPanel extends JPanel implements SqlListener, CoderListener {
	private static final long serialVersionUID = 1044070479152247253L;
	JTable statementTable;
	StatementTableModel statementTableModel;

	public StatementPanel(StatementTableModel statementTableModel) {
		Dna.addCoderListener(this);
		Dna.addSqlListener(this);
		this.setLayout(new BorderLayout());
		this.statementTableModel = statementTableModel;
		statementTable = new JTable(statementTableModel);
		this.add(statementTable, BorderLayout.NORTH);
	}
	
	public int getSelectedStatementId() {
		try {
			return (int) statementTable.getValueAt(statementTable.getSelectedRow(), 0);
		} catch (NullPointerException npe) {
			return -1;
		}
	}
	
	public void setSelectedStatementId(int statementId) {
		int modelRowIndex = statementTableModel.getModelRowById(statementId);
		if (modelRowIndex > -1) {
			int tableRow = statementTable.convertRowIndexToView(modelRowIndex);
			this.statementTable.setRowSelectionInterval(tableRow, tableRow);
		}
	}

	@Override
	public void adjustToChangedCoder() {
		// TODO Auto-generated method stub
	}

	@Override
	public void adjustToDatabaseState() {
		// TODO Auto-generated method stub
	}
}