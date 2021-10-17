package gui;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import org.jdesktop.swingx.JXTextField;

import dna.Dna;
import gui.MainWindow.ActionAddDocument;
import gui.MainWindow.ActionEditDocuments;
import gui.MainWindow.ActionRefresh;
import gui.MainWindow.ActionRemoveDocuments;
import gui.MainWindow.ActionRemoveStatements;
import sql.Sql.SqlListener;

class ToolbarPanel extends JPanel implements SqlListener {
	private static final long serialVersionUID = 5561195349172139438L;
	private JTextField documentFilterField;

	public ToolbarPanel(DocumentTableModel documentTableModel,
			ActionAddDocument actionAddDocument,
			ActionRemoveDocuments actionRemoveDocuments,
			ActionEditDocuments actionEditDocuments,
			ActionRefresh actionRefresh,
			ActionRemoveStatements actionRemoveStatements) {
		
		this.setLayout(new BorderLayout());
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);

		documentFilterField = new JXTextField("Document regex filter");
		documentFilterField.setToolTipText("Filter the documents using a regular expression.");
		documentFilterField.setEnabled(false);
		toolBar.add(documentFilterField);

		JButton addDocumentButton = new JButton(actionAddDocument);
		addDocumentButton.setText("Add document");
		toolBar.add(addDocumentButton);

		JButton removeDocumentsButton = new JButton(actionRemoveDocuments);
		removeDocumentsButton.setText("Remove document(s)");
		toolBar.add(removeDocumentsButton);

		JButton editDocumentsButton = new JButton(actionEditDocuments);
		editDocumentsButton.setText("Edit document(s)");
		toolBar.add(editDocumentsButton);

		JButton documentTableRefreshButton = new JButton(actionRefresh);
		documentTableRefreshButton.setText("Refresh");
		toolBar.add(documentTableRefreshButton);

		this.add(toolBar, BorderLayout.NORTH);
	}
	
	/**
	 * Get a reference to the document filter field.
	 * 
	 * @return The document filter text field.
	 */
	JTextField getDocumentFilterField() {
		return documentFilterField;
	}

	@Override
	public void adjustToChangedCoder() {
		// nothing to do
	}

	@Override
	public void adjustToChangedConnection() {
		if (Dna.sql.getConnectionProfile() == null) {
			documentFilterField.setText("");
			documentFilterField.setEnabled(false);
		} else {
			documentFilterField.setEnabled(true);
		}
	}
}