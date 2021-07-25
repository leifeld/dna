package dna.panels;

import dna.Dna;
import dna.dataStructures.*;
import dna.renderer.CoderComboBoxModel;
import dna.renderer.CoderComboBoxRenderer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

@SuppressWarnings("serial")
public class EditDocumentPanel extends JPanel {
	JTextField titleField;
	JTextArea notesArea;
	SpinnerDateModel dateModel;
	JSpinner dateSpinner;
	JComboBox<Coder> coderBox;
	JComboBox<String> authorBox;
	JComboBox<String> sourceBox;
	JComboBox<String> sectionBox;
	JComboBox<String> typeBox;
	public JButton saveDetailsButton, cancelButton;
	JScrollPane notesScroll;
	JLabel notesLabel;
	JLabel authorLabel;
	JLabel sectionLabel;
	JLabel titleLabel;
	JLabel dateLabel;
	JLabel coderLabel;
	JLabel sourceLabel;
	JLabel typeLabel;
	GregorianCalendar gc;

	Document document;
	
	public EditDocumentPanel() {
		createEditDocumentPanel();		
	}
	
	public void createEditDocumentPanel() {
		this.removeAll();
		JLabel selectDoc = new JLabel("(No document or permission)");
		selectDoc.setEnabled(false);
		this.add(selectDoc);
	}
	
	public void createEditDocumentPanel(Document doc) {	
		this.document = doc;
		this.removeAll();
		this.setLayout(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		
		g.gridwidth = 2;
		g.insets = new Insets(0, 0, 0, 1);
		g.gridx = 0;
		g.gridy = 0;
		g.anchor = GridBagConstraints.WEST;
		titleLabel = new JLabel("Title");
		this.add(titleLabel, g);
		
		g.gridy = 1;
		g.weightx = 1.;
		g.fill = GridBagConstraints.HORIZONTAL;
		titleField = new JTextField();
		titleField.setText(document.getTitle());
		titleField.setCaretPosition(0);
		this.add(titleField, g);
		
		g.gridy = 2;
		this.add(Box.createRigidArea(new Dimension(5, 5)), g);
		
		g.gridy = 3;
		dateLabel = new JLabel("Date");
		this.add(dateLabel, g);
		
		g.gridy = 4;
		dateModel = new SpinnerDateModel();
		dateSpinner = new JSpinner();
		dateModel.setCalendarField( Calendar.DAY_OF_YEAR );
		dateSpinner.setModel( dateModel );
		gc = new GregorianCalendar();
		gc.set(Calendar.HOUR, -12);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		dateModel.setValue(document.getDate());
		dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd  HH:mm:ss"));
		this.add(dateSpinner, g);
		
		g.gridy = 5;
		this.add(Box.createRigidArea(new Dimension(5, 5)), g);
		
		g.gridy = 6;
		coderLabel = new JLabel("Coder");
		this.add(coderLabel, g);
		
		g.gridy = 7;
		CoderComboBoxRenderer renderer = new CoderComboBoxRenderer();
		CoderComboBoxModel model = new CoderComboBoxModel();
		coderBox = new JComboBox<Coder>(model);
		coderBox.setRenderer(renderer);
		coderBox.setSelectedItem(Dna.data.getCoderById(doc.getCoder()));
		this.add(coderBox, g);
		
		g.gridy = 8;
		this.add(Box.createRigidArea(new Dimension(5, 5)), g);

		g.gridy = 9;
		authorLabel = new JLabel("Author");
		this.add(authorLabel, g);
		
		g.gridy = 10;
		ArrayList<String> authorEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!authorEntries.contains(Dna.data.getDocuments().get(i).getAuthor())) {
				authorEntries.add(Dna.data.getDocuments().get(i).getAuthor());
			}
		}
		Collections.sort(authorEntries);
		String[] authorEntriesArray = Arrays.copyOf(authorEntries.toArray(), authorEntries.toArray().length, String[].class);
		authorBox = new JComboBox<String>(authorEntriesArray);
		authorBox.setEditable(true);
		authorBox.setSelectedItem(document.getAuthor());
		AutoCompleteDecorator.decorate(authorBox);
		this.add(authorBox, g);
		
		g.gridy = 11;
		this.add(Box.createRigidArea(new Dimension(5, 5)), g);
		
		g.gridy = 12;
		sourceLabel = new JLabel("Source");
		this.add(sourceLabel, g);
		
		g.gridy = 13;
		ArrayList<String> sourceEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!sourceEntries.contains(Dna.data.getDocuments().get(i).getSource())) {
				sourceEntries.add(Dna.data.getDocuments().get(i).getSource());
			}
		}
		Collections.sort(sourceEntries);
		String[] sourceEntriesArray = Arrays.copyOf(sourceEntries.toArray(), sourceEntries.toArray().length, String[].class);
		sourceBox = new JComboBox<String>(sourceEntriesArray);
		sourceBox.setEditable(true);
		sourceBox.setSelectedItem(document.getSource());
		AutoCompleteDecorator.decorate(sourceBox);
		this.add(sourceBox, g);
		
		g.gridy = 14;
		this.add(Box.createRigidArea(new Dimension(5, 5)), g);
		
		g.gridy = 15;
		sectionLabel = new JLabel("Section");
		this.add(sectionLabel, g);

		g.gridy = 16;
		ArrayList<String> sectionEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!sectionEntries.contains(Dna.data.getDocuments().get(i).getSection())) {
				sectionEntries.add(Dna.data.getDocuments().get(i).getSection());
			}
		}
		Collections.sort(sectionEntries);
		String[] sectionEntriesArray = Arrays.copyOf(sectionEntries.toArray(), sectionEntries.toArray().length, String[].class);
		sectionBox = new JComboBox<String>(sectionEntriesArray);
		sectionBox.setEditable(true);
		sectionBox.setSelectedItem(document.getSection());
		AutoCompleteDecorator.decorate(sectionBox);
		this.add(sectionBox, g);
		
		g.gridy = 17;
		this.add(Box.createRigidArea(new Dimension(5, 5)), g);
		
		g.gridy = 18;
		typeLabel = new JLabel("Type");
		this.add(typeLabel, g);
		
		g.gridy = 19;
		ArrayList<String> typeEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!typeEntries.contains(Dna.data.getDocuments().get(i).getType())) {
				typeEntries.add(Dna.data.getDocuments().get(i).getType());
			}
		}
		Collections.sort(typeEntries);
		String[] typeEntriesArray = Arrays.copyOf(typeEntries.toArray(), typeEntries.toArray().length, String[].class);
		typeBox = new JComboBox<String>(typeEntriesArray);
		typeBox.setEditable(true);
		typeBox.setSelectedItem(document.getType());
		AutoCompleteDecorator.decorate(typeBox);
		this.add(typeBox, g);
		
		g.gridy = 20;
		this.add(Box.createRigidArea(new Dimension(5, 5)), g);
		
		g.gridy = 21;
		notesLabel = new JLabel("Notes");
		this.add(notesLabel, g);
		
		g.gridy = 22;
		g.weighty = 1.0;
		g.fill = GridBagConstraints.BOTH;
		notesArea = new JTextArea(document.getNotes());
		notesArea.setCaretPosition(0);
		notesArea.setBorder(titleField.getBorder());
		notesArea.setRows(4);
		
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesScroll = new JScrollPane(notesArea);
		this.add(notesScroll, g);
		
		g.gridy = 23;
		g.fill = GridBagConstraints.HORIZONTAL;
		g.weighty = 0;
		this.add(Box.createRigidArea(new Dimension(5, 5)), g);
		
		// buttons
		g.gridwidth = 1;
		g.gridy = 24;
		Icon tickIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		Icon clearIcon = new ImageIcon(getClass().getResource("/icons/cross.png"));
		saveDetailsButton = new JButton("Save", tickIcon);
		saveDetailsButton.setToolTipText("save the changes");
		cancelButton = new JButton("Cancel", clearIcon);
		cancelButton.setToolTipText("cancel changes and refresh without saving");
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createEditDocumentPanel(doc);
				boolean[] b = Dna.data.getActiveDocumentPermissions(document.getId());
				setEnabled(b[1]);
				revalidate();
			}
		});
		saveDetailsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDetails();
			}
		});
		int ac = Dna.data.getActiveCoder();
		if (Dna.data.getCoderById(ac).getPermissions().get("editDocuments") == false) {
			saveDetailsButton.setEnabled(false);
			cancelButton.setEnabled(false);
		} else {
			saveDetailsButton.setEnabled(true);
			cancelButton.setEnabled(true);
		}
		this.add(saveDetailsButton, g);
		
		g.gridx = 1;
		this.add(cancelButton, g);
		
		this.setEnabled(true);
		this.revalidate();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (document != null) {
			titleLabel.setEnabled(enabled);
			titleField.setEnabled(enabled);
			dateLabel.setEnabled(enabled);
			dateSpinner.setEnabled(enabled);
			coderLabel.setEnabled(enabled);
			coderBox.setEnabled(enabled);
			sourceLabel.setEnabled(enabled);
			sourceBox.setEnabled(enabled);
			sectionLabel.setEnabled(enabled);
			sectionBox.setEnabled(enabled);
			typeLabel.setEnabled(enabled);
			typeBox.setEnabled(enabled);
			notesLabel.setEnabled(enabled);
			notesArea.setEnabled(enabled);
			saveDetailsButton.setEnabled(enabled);
			cancelButton.setEnabled(enabled);
		}
	}
	
	void saveDetails() {
		String title = titleField.getText();
		title = title.replaceAll("'", "''");
		Date date = (Date) dateSpinner.getValue();
		int coder = Dna.data.getCoders().get(coderBox.getSelectedIndex()).getId();
		String author = (String) authorBox.getModel().getSelectedItem();
		author = author.replaceAll("'", "''");
		String source = (String) sourceBox.getModel().getSelectedItem();
		source = source.replaceAll("'", "''");
		String section = (String) sectionBox.getModel().getSelectedItem();
		section = section.replaceAll("'", "''");
		String notes = notesArea.getText();
		notes = notes.replaceAll("'", "''");
		String type = (String) typeBox.getModel().getSelectedItem();
		type = type.replaceAll("'", "''");
		
		int documentId = document.getId();
		Dna.data.getDocument(documentId).setTitle(title);
		Dna.data.getDocument(documentId).setDate(date);
		Dna.data.getDocument(documentId).setCoder(coder);
		Dna.data.getDocument(documentId).setAuthor(author);
		Dna.data.getDocument(documentId).setSource(source);
		Dna.data.getDocument(documentId).setSection(section);
		Dna.data.getDocument(documentId).setType(type);
		Dna.data.getDocument(documentId).setNotes(notes);
		
		Dna.dna.sql.upsertDocument(Dna.data.getDocument(documentId));
		
		Dna.gui.documentPanel.documentContainer.sort();
		int newModelIndex = Dna.gui.documentPanel.documentContainer.getModelIndexById(document.getId());
		int newRow = Dna.gui.documentPanel.documentTable.convertRowIndexToView(newModelIndex);
		Dna.gui.documentPanel.documentTable.updateUI();
		Dna.gui.documentPanel.documentTable.getSelectionModel().setSelectionInterval(newRow, newRow);

		createEditDocumentPanel(document);
		boolean[] b = Dna.data.getActiveDocumentPermissions(document.getId());
		setEnabled(b[1]);
		revalidate();
	}
}