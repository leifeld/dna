package gui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXTextField;

import dna.Dna;
import dna.Dna.CoderListener;
import dna.Dna.SqlListener;
import gui.MainWindow.ActionAddDocument;
import gui.MainWindow.ActionEditDocuments;
import gui.MainWindow.ActionRefresh;
import gui.MainWindow.ActionRemoveDocuments;
import logger.LogEvent;
import logger.Logger;
import model.Coder;

class Toolbar extends JToolBar implements SqlListener, CoderListener {
	private static final long serialVersionUID = 5561195349172139438L;
	private List<ToolbarListener> listeners = new ArrayList<ToolbarListener>();
	private JTextField documentFilterField;
	private JButton documentFilterResetButton;
	private JLabel popupWidthLabel, fontSizeLabel;
	private JSpinner popupWidthSpinner, fontSizeSpinner;
	private SpinnerNumberModel popupWidthModel, fontSizeModel;
	private JToggleButton popupDecorationButton, popupAutoCompleteButton, colorByCoderButton;

	public Toolbar(DocumentTableModel documentTableModel,
			ActionAddDocument actionAddDocument,
			ActionRemoveDocuments actionRemoveDocuments,
			ActionEditDocuments actionEditDocuments,
			ActionRefresh actionRefresh) {
		this.setFloatable(false);
		this.setRollover(true);

		ImageIcon documentFilterResetIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-backspace.png")).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
		documentFilterResetButton = new JButton(documentFilterResetIcon);
		documentFilterResetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				documentFilterField.setText("");
			}
		});
		documentFilterResetButton.setEnabled(false);
		documentFilterResetButton.setToolTipText("Filter the documents using a regular expression.");
		documentFilterField = new JXTextField("Document regex filter");
		documentFilterField.setToolTipText("Filter the documents using a regular expression.");
        documentFilterField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				processFilterDocumentChanges();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				processFilterDocumentChanges();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				processFilterDocumentChanges();
			}
			
			private void processFilterDocumentChanges() {
				fireUpdatedDocumentFilterPattern(documentFilterField.getText());
				if (documentFilterField.getText().equals("")) {
					documentFilterResetButton.setEnabled(false);
				} else {
					documentFilterResetButton.setEnabled(true);
				}
				documentTableModel.fireTableDataChanged();
			}
		});
		documentFilterField.setEnabled(false);
		this.add(documentFilterField);
		this.add(documentFilterResetButton);

		JButton addDocumentButton = new JButton(actionAddDocument);
		addDocumentButton.setText("Add");
		this.add(addDocumentButton);

		JButton removeDocumentsButton = new JButton(actionRemoveDocuments);
		removeDocumentsButton.setText("Remove");
		this.add(removeDocumentsButton);

		JButton editDocumentsButton = new JButton(actionEditDocuments);
		editDocumentsButton.setText("Edit");
		this.add(editDocumentsButton);

		JButton documentTableRefreshButton = new JButton(actionRefresh);
		documentTableRefreshButton.setText("Refresh");
		this.add(documentTableRefreshButton);

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
				if (Dna.sql != null) {
					Dna.sql.setCoderFontSize(Dna.sql.getConnectionProfile().getCoderId(), (int) fontSizeSpinner.getValue());
					Dna.fireCoderChange();
				}
			}
		});
		fontSizeLabel.setEnabled(false);
		fontSizeSpinner.setEnabled(false);
		this.add(fontSizeLabel);
		this.add(fontSizeSpinner);

		// color statements by coder toggle button
		ImageIcon colorByCoderIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-palette.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		colorByCoderButton = new JToggleButton(colorByCoderIcon);
		colorByCoderButton.setToolTipText("If the button is selected, statements in the text are highlighted using the color of the coder who created them; otherwise using the statement type color.");
		colorByCoderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Dna.sql != null) {
					Dna.sql.setColorByCoder(Dna.sql.getConnectionProfile().getCoderId(), colorByCoderButton.isSelected());
					Dna.fireCoderChange();
				}
			}
		});
		colorByCoderButton.setEnabled(false);
		this.addSeparator(new Dimension(3, 3));
		this.add(colorByCoderButton);

		// popup window decoration toggle button
		ImageIcon popupDecorationIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-border-outer.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		popupDecorationButton = new JToggleButton(popupDecorationIcon);
		popupDecorationButton.setToolTipText("If the button is selected, statement popup windows will have buttons and a frame. If not, statements will auto-save.");
		popupDecorationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Dna.sql != null) {
					Dna.sql.setCoderPopupDecoration(Dna.sql.getConnectionProfile().getCoderId(), popupDecorationButton.isSelected());
					Dna.fireCoderChange();
				}
			}
		});
		popupDecorationButton.setEnabled(false);
		this.addSeparator(new Dimension(3, 3));
		this.add(popupDecorationButton);

		// popup auto-completion toggle button
		ImageIcon popupAutoCompleteIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-forms.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		popupAutoCompleteButton = new JToggleButton(popupAutoCompleteIcon);
		popupAutoCompleteButton.setToolTipText("If the button is selected, text fields in statement popup windows will have auto-complete activated for entries.");
		popupAutoCompleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Dna.sql != null) {
					Dna.sql.setCoderPopupAutoComplete(Dna.sql.getConnectionProfile().getCoderId(), popupAutoCompleteButton.isSelected());
					Dna.fireCoderChange();
				}
			}
		});
		popupAutoCompleteButton.setEnabled(false);
		this.addSeparator(new Dimension(3, 3));
		this.add(popupAutoCompleteButton);

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
				if (Dna.sql != null) {
					Dna.sql.setCoderPopupWidth(Dna.sql.getConnectionProfile().getCoderId(), (int) popupWidthSpinner.getValue());
					Dna.fireCoderChange();
				}
			}
		});
		popupWidthLabel.setEnabled(false);
		popupWidthSpinner.setEnabled(false);
		this.add(popupWidthLabel);
		this.add(popupWidthSpinner);
		
	}
	
	private void fireUpdatedDocumentFilterPattern(String pattern) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).updatedDocumentFilterPattern(pattern);
		}
	}

	void addToolbarListener(ToolbarListener listener) {
		listeners.add(listener);
	}

	@Override
	public void adjustToChangedCoder() {
		if (Dna.sql == null) {
			popupWidthModel.setValue(300);
			fontSizeModel.setValue(14);
			popupDecorationButton.setSelected(false);
			popupAutoCompleteButton.setSelected(false);
			colorByCoderButton.setSelected(false);
		} else {
			Coder coder = Dna.sql.getCoder(Dna.sql.getConnectionProfile().getCoderId());
			popupWidthModel.setValue(coder.getPopupWidth());
			fontSizeModel.setValue(coder.getFontSize());
			if (coder.getPopupDecoration() == 1) {
				popupDecorationButton.setSelected(true);
			} else {
				popupDecorationButton.setSelected(false);
			}
			if (coder.getPopupAutoComplete() == 1) {
				popupAutoCompleteButton.setSelected(true);
			} else {
				popupAutoCompleteButton.setSelected(false);
			}
			if (coder.getColorByCoder() == 1) {
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
	public void adjustToDatabaseState() {
		if (Dna.sql == null) {
			documentFilterField.setText("");
			documentFilterField.setEnabled(false);
			documentFilterResetButton.setEnabled(false);
			popupWidthLabel.setEnabled(false);
			popupWidthSpinner.setEnabled(false);
			fontSizeLabel.setEnabled(false);
			fontSizeSpinner.setEnabled(false);
			popupDecorationButton.setEnabled(false);
			popupAutoCompleteButton.setEnabled(false);
			colorByCoderButton.setEnabled(false);
		} else {
			documentFilterField.setEnabled(true);
			if (documentFilterField.getText().equals("")) {
				documentFilterResetButton.setEnabled(false);
			} else {
				documentFilterResetButton.setEnabled(true);
			}
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