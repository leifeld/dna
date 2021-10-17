package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTextField;

import dna.Dna;
import gui.MainWindow.ActionAddDocument;
import gui.MainWindow.ActionEditDocuments;
import gui.MainWindow.ActionRefresh;
import gui.MainWindow.ActionRemoveDocuments;
import gui.MainWindow.ActionRemoveStatements;
import logger.LogEvent;
import logger.Logger;
import sql.Sql.SqlListener;

class ToolbarPanel extends JPanel implements SqlListener {
	private static final long serialVersionUID = 5561195349172139438L;
	private JTextField documentFilterField;
	private JLabel popupWidthLabel, fontSizeLabel;
	private JSpinner popupWidthSpinner, fontSizeSpinner;
	private SpinnerNumberModel popupWidthModel, fontSizeModel;
	private JToggleButton popupDecorationButton, popupAutoCompleteButton, colorByCoderButton;

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

        // font size spinner
        ImageIcon fontSizeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-typography.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		fontSizeLabel = new JLabel(fontSizeIcon);
		fontSizeLabel.setToolTipText("Set the font size of the text area.");
        fontSizeModel = new SpinnerNumberModel(14, 1, 99, 1);
		fontSizeSpinner = new JSpinner(fontSizeModel);
		((DefaultEditor) fontSizeSpinner.getEditor()).getTextField().setColumns(2);
		fontSizeSpinner.setToolTipText("Set the font size of the text area.");
		fontSizeLabel.setLabelFor(fontSizeSpinner);
		fontSizeSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (Dna.sql.getConnectionProfile() != null) {
					Dna.sql.setCoderFontSize(Dna.sql.getConnectionProfile().getCoderId(), (int) fontSizeSpinner.getValue());
				}
			}
		});
		fontSizeLabel.setEnabled(false);
		fontSizeSpinner.setEnabled(false);
		toolBar.add(fontSizeLabel);
		toolBar.add(fontSizeSpinner);

		// color statements by coder toggle button
		ImageIcon colorByCoderIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-palette.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		colorByCoderButton = new JToggleButton(colorByCoderIcon);
		colorByCoderButton.setToolTipText("If the button is selected, statements in the text are highlighted using the color of the coder who created them; otherwise using the statement type color.");
		colorByCoderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Dna.sql.getConnectionProfile() != null) {
					Dna.sql.setColorByCoder(Dna.sql.getConnectionProfile().getCoderId(), colorByCoderButton.isSelected());
				}
			}
		});
		colorByCoderButton.setEnabled(false);
		toolBar.addSeparator(new Dimension(3, 3));
		toolBar.add(colorByCoderButton);

		// popup window decoration toggle button
		ImageIcon popupDecorationIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-border-outer.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		popupDecorationButton = new JToggleButton(popupDecorationIcon);
		popupDecorationButton.setToolTipText("If the button is selected, statement popup windows will have buttons and a frame. If not, statements will auto-save.");
		popupDecorationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Dna.sql.getConnectionProfile() != null) {
					Dna.sql.setCoderPopupDecoration(Dna.sql.getConnectionProfile().getCoderId(), popupDecorationButton.isSelected());
				}
			}
		});
		popupDecorationButton.setEnabled(false);
		toolBar.addSeparator(new Dimension(3, 3));
		toolBar.add(popupDecorationButton);

		// popup auto-completion toggle button
		ImageIcon popupAutoCompleteIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-forms.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		popupAutoCompleteButton = new JToggleButton(popupAutoCompleteIcon);
		popupAutoCompleteButton.setToolTipText("If the button is selected, text fields in statement popup windows will have auto-complete activated for entries.");
		popupAutoCompleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Dna.sql.getConnectionProfile() != null) {
					Dna.sql.setCoderPopupAutoComplete(Dna.sql.getConnectionProfile().getCoderId(), popupAutoCompleteButton.isSelected());
				}
			}
		});
		popupAutoCompleteButton.setEnabled(false);
		toolBar.addSeparator(new Dimension(3, 3));
		toolBar.add(popupAutoCompleteButton);

		// popup width spinner
        ImageIcon popupWidthIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-chart-arrows.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		popupWidthLabel = new JLabel(popupWidthIcon);
		popupWidthLabel.setToolTipText("Set the width of the text fields for the variables in a statement popup window (in px).");
        popupWidthModel = new SpinnerNumberModel(300, 160, 9990, 10);
		popupWidthSpinner = new JSpinner(popupWidthModel);
		((DefaultEditor) popupWidthSpinner.getEditor()).getTextField().setColumns(4);
		popupWidthSpinner.setToolTipText("Set the width of the text fields for the variables in a statement popup window (in px).");
		popupWidthLabel.setLabelFor(popupWidthSpinner);
		popupWidthSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if (Dna.sql.getConnectionProfile() != null) {
					Dna.sql.setCoderPopupWidth(Dna.sql.getConnectionProfile().getCoderId(), (int) popupWidthSpinner.getValue());
				}
			}
		});
		popupWidthLabel.setEnabled(false);
		popupWidthSpinner.setEnabled(false);
		toolBar.add(popupWidthLabel);
		toolBar.add(popupWidthSpinner);
		
		JButton removeStatementsButton = new JButton(actionRemoveStatements);
		removeStatementsButton.setText("Remove statement(s)");
		toolBar.add(removeStatementsButton);

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
		if (Dna.sql.getConnectionProfile() == null) {
			popupWidthModel.setValue(300);
			fontSizeModel.setValue(14);
			popupDecorationButton.setSelected(false);
			popupAutoCompleteButton.setSelected(false);
			colorByCoderButton.setSelected(false);
		} else {
			popupWidthModel.setValue(Dna.sql.getActiveCoder().getPopupWidth());
			fontSizeModel.setValue(Dna.sql.getActiveCoder().getFontSize());
			if (Dna.sql.getActiveCoder().isPopupDecoration() == true) {
				popupDecorationButton.setSelected(true);
			} else {
				popupDecorationButton.setSelected(false);
			}
			if (Dna.sql.getActiveCoder().isPopupAutoComplete() == true) {
				popupAutoCompleteButton.setSelected(true);
			} else {
				popupAutoCompleteButton.setSelected(false);
			}
			if (Dna.sql.getActiveCoder().isColorByCoder() == true) {
				colorByCoderButton.setSelected(true);
			} else {
				colorByCoderButton.setSelected(false);
			}
		}
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"[GUI] Document panel adjusted to updated coder settings (or closed database).",
				"[GUI] Document panel adjusted to updated coder settings (or closed database).");
		Dna.logger.log(l);
	}

	@Override
	public void adjustToChangedConnection() {
		if (Dna.sql.getConnectionProfile() == null) {
			documentFilterField.setText("");
			documentFilterField.setEnabled(false);
			popupWidthLabel.setEnabled(false);
			popupWidthSpinner.setEnabled(false);
			fontSizeLabel.setEnabled(false);
			fontSizeSpinner.setEnabled(false);
			popupDecorationButton.setEnabled(false);
			popupAutoCompleteButton.setEnabled(false);
			colorByCoderButton.setEnabled(false);
		} else {
			documentFilterField.setEnabled(true);
			popupWidthLabel.setEnabled(true);
			popupWidthSpinner.setEnabled(true);
			fontSizeLabel.setEnabled(true);
			fontSizeSpinner.setEnabled(true);
			popupDecorationButton.setEnabled(true);
			popupAutoCompleteButton.setEnabled(true);
			colorByCoderButton.setEnabled(true);
		}
	}
}