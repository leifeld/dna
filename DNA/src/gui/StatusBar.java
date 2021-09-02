package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dna.Dna;
import dna.Dna.SqlListener;
import gui.DocumentTablePanel.DocumentPanelListener;
import gui.StatementPanel.StatementListener;
import logger.Logger.LogListener;
import logger.LoggerDialog;
import sql.Sql;

/**
 * A status bar panel showing the database on the left and messages on the right. 
 */
class StatusBar extends JPanel implements LogListener, SqlListener, DocumentPanelListener, StatementListener {
	private static final long serialVersionUID = -1987834394140569531L;
	JLabel urlLabel, documentRefreshLabel, documentRefreshIconLabel, statementRefreshLabel, statementRefreshIconLabel;
	int numWarnings, numErrors;
	JButton messageIconButton, warningButton, errorButton;
	
	/**
	 * Create a new status bar.
	 */
	public StatusBar() {
		this.setLayout(new BorderLayout());
		JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ImageIcon databaseIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-database.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ActionOpenDatabase actionOpenDatabase = new ActionOpenDatabase(null, databaseIcon, "Open a dialog window to establish a connection to a remote or file-based database", KeyEvent.VK_O);
		JButton databaseButton = new JButton(actionOpenDatabase);
		databaseButton.setContentAreaFilled(false);
		databaseButton.setBorderPainted(false);
		databaseButton.setBorder(null);
		databaseButton.setMargin(new Insets(0, 0, 0, 0));
		leftPanel.add(databaseButton);
		urlLabel = new JLabel("");
		leftPanel.add(urlLabel);
		this.add(leftPanel, BorderLayout.WEST);
		
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		ImageIcon refreshIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-refresh.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		documentRefreshIconLabel = new JLabel(refreshIcon);
		documentRefreshIconLabel.setVisible(false);
		rightPanel.add(documentRefreshIconLabel);
		documentRefreshLabel = new JLabel("Documents");
		rightPanel.add(documentRefreshLabel);
		documentRefreshLabel.setVisible(false);
		statementRefreshIconLabel = new JLabel(refreshIcon);
		statementRefreshIconLabel.setVisible(false);
		rightPanel.add(statementRefreshIconLabel);
		statementRefreshLabel = new JLabel("Statements");
		rightPanel.add(statementRefreshLabel);
		statementRefreshLabel.setVisible(false);
		
		ImageIcon messageIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-message-report.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		messageIconButton = new JButton(messageIcon);
		messageIconButton.setContentAreaFilled(false);
		messageIconButton.setBorderPainted(false);
		messageIconButton.setBorder(null);
		messageIconButton.setMargin(new Insets(0, 0, 0, 0));
		numWarnings = 0;
		numErrors = 0;
		
		errorButton = new JButton(numErrors + "");
		errorButton.setContentAreaFilled(false);
		errorButton.setBorderPainted(false);
		errorButton.setForeground(new Color(153, 0, 0));
		errorButton.setBorder(null);
		errorButton.setMargin(new Insets(0, 0, 0, 0));
		errorButton.setVisible(false);
		
		warningButton = new JButton(numWarnings + "");
		warningButton.setContentAreaFilled(false);
		warningButton.setBorderPainted(false);
		warningButton.setForeground(new Color(220, 153, 0));
		warningButton.setBorder(null);
		warningButton.setMargin(new Insets(0, 0, 0, 0));
		warningButton.setVisible(false);

		ActionListener messageButtonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new LoggerDialog();
			}
		};
		messageIconButton.addActionListener(messageButtonListener);
		errorButton.addActionListener(messageButtonListener);
		warningButton.addActionListener(messageButtonListener);
		
		rightPanel.add(messageIconButton);
		rightPanel.add(errorButton);
		rightPanel.add(warningButton);
		this.add(rightPanel, BorderLayout.EAST);
	}
	
	/**
	 * Read the database URL from the {@link Sql} object and update it in
	 * the status bar. Show an empty string if no database is open.
	 */
	public void updateUrl() {
		if (Dna.sql == null) {
			this.urlLabel.setText("");
			this.urlLabel.setVisible(false);
		} else {
			this.urlLabel.setText(Dna.sql.getConnectionProfile().getUrl());
			this.urlLabel.setVisible(true);
		}
	}
	
	/**
	 * Refresh the count of warnings and errors. The respective
	 * count is only shown if it is greater than zero.
	 *  
	 * @param warnings Number of new warnings in the logger.
	 * @param errors Number of new errors in the logger.
	 */
	public void updateLog(int warnings, int errors) {
		this.numWarnings = warnings;
		warningButton.setText(this.numWarnings + " warnings");
		if (warnings == 0) {
			warningButton.setVisible(false);
		} else {
			warningButton.setVisible(true);
		}
		this.numErrors = errors;
		errorButton.setText(this.numErrors + " errors");
		if (errors == 0) {
			errorButton.setVisible(false);
		} else {
			errorButton.setVisible(true);
		}
	}

	@Override
	public void statementRefreshStart() {
		this.statementRefreshIconLabel.setVisible(true);
		this.statementRefreshLabel.setVisible(true);
	}

	@Override
	public void statementRefreshEnd() {
		this.statementRefreshIconLabel.setVisible(false);
		this.statementRefreshLabel.setVisible(false);
	}

	/**
	 * Listen to changes in the Logger in DNA and respond by updating the event
	 * counts in the status bar.
	 */
	@Override
	public void processLogEvents() {
		int numWarnings = 0;
		int numErrors = 0;
		for (int i = 0; i < Dna.logger.getRowCount(); i++) {
			if (Dna.logger.getRow(i).getPriority() == 2) {
				numWarnings++;
			} else if (Dna.logger.getRow(i).getPriority() == 3) {
				numErrors++;
			}
		}
		updateLog(numWarnings, numErrors);
	}

	@Override
	public void documentTableSingleSelection(int documentId, String documentText) {
		// nothing to do
	}

	@Override
	public void documentTableMultipleSelection(int[] documentId) {
		// nothing to do
	}

	@Override
	public void documentTableNoSelection() {
		// nothing to do
	}

	@Override
	public void documentRefreshStarted() {
		this.documentRefreshIconLabel.setVisible(true);
		this.documentRefreshLabel.setVisible(true);
	}

	@Override
	public void documentRefreshChunkComplete() {
		// nothing to do
	}

	@Override
	public void documentRefreshEnded() {
		this.documentRefreshIconLabel.setVisible(false);
		this.documentRefreshLabel.setVisible(false);
	}

	@Override
	public void adjustToDatabaseState() {
		updateUrl();
	}

	@Override
	public void statementSelected(int statementId) {
		// nothing to do
	}
}