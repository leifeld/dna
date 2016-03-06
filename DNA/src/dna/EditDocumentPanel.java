package dna;

import dna.dataStructures.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ListDataListener;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister.Pack;


public class EditDocumentPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Dimension fieldSize = new Dimension(120, 25);
	Dimension lableSize = new Dimension(70, 15);

	JXTextField titleField;
	JXTextArea notesArea;
	SpinnerDateModel dateModel;
	JSpinner dateSpinner;
	JXComboBox coderBox, sourceBox, sectionBox, typeBox;
	JButton saveButton, cancelButton;
	JScrollPane notesScroll;
	JLabel notesLabel;
	JLabel sectionLabel;
	 JLabel titleLabel;
	 JLabel dateLabel;
	 JLabel coderLabel;

	 JLabel sourceLabel;
	 JLabel typeLabel;
	 GregorianCalendar gc;
	
	 Document document;
	EditDocumentPanel()
	{
		createEditDocumentPanel();		
	}
	
	public void createEditDocumentPanel()
	{
		this.removeAll();
		JLabel selectDoc = new JLabel("Select a document from list");
		 selectDoc.setEnabled(false);
		 this.add(selectDoc);
	}
	
	public void createEditDocumentPanel(Document doc)
	{	
		this.document = doc;
		this.removeAll();
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		JPanel titlePanel =new JPanel(new FlowLayout(FlowLayout.LEFT));
		titleLabel = new JLabel("Title");
		titlePanel.add(titleLabel);
		this.add(titlePanel);
		
		
		titleField = new JXTextField();
		titleField.setText(document.getTitle());
		titleField.setCaretPosition(0);
		titleField.setPreferredSize(fieldSize);
		titleField.setColumns(50);
	
		titlePanel.add(titleField);
		this.add(titleField);
		
		
		JPanel datePanel =new JPanel(new FlowLayout(FlowLayout.LEFT));
		dateLabel = new JLabel("Date", JLabel.RIGHT);
		dateLabel.setPreferredSize(lableSize);
		datePanel.add(dateLabel);
		
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
		dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, 	"yyyy-MM-dd  HH:mm:ss"));
		dateSpinner.setPreferredSize(fieldSize);
		datePanel.add(dateSpinner);
		
		this.add(datePanel);
	
		JPanel coderPanel =new JPanel(new FlowLayout(FlowLayout.LEFT));
		coderLabel = new JLabel("Coder", JLabel.RIGHT);
		coderLabel.setPreferredSize(lableSize);
		coderPanel.add(coderLabel);		

		//coderEntries = Dna.dna.db.getDocumentCoders();
		//coderBox = new JXComboBox(coderEntries);
		coderBox = new JXComboBox(Dna.data.getCoders().toArray());
		CoderComboBoxRenderer coderRenderer = new CoderComboBoxRenderer();
		coderBox.setRenderer(coderRenderer);
		coderBox.setPreferredSize(fieldSize);
		coderBox.setEditable(true);
		coderBox.setSelectedItem(document.getCoder());
		AutoCompleteDecorator.decorate(coderBox);
		coderPanel.add(coderBox);
		
		this.add(coderPanel);
		
		
		JPanel sourcePanel =new JPanel(new FlowLayout(FlowLayout.LEFT));
		sourceLabel = new JLabel("Source", JLabel.RIGHT);
		sourceLabel.setPreferredSize(lableSize);
		sourcePanel.add(sourceLabel);
		
		//sourceEntries = Dna.dna.db.getDocumentSources();
		ArrayList<String> sourceEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!sourceEntries.contains(Dna.data.getDocuments().get(i).getSource())) {
				sourceEntries.add(Dna.data.getDocuments().get(i).getSource());
			}
		}
		Collections.sort(sourceEntries);
		sourceBox = new JXComboBox(sourceEntries.toArray());
		sourceBox.setPreferredSize(fieldSize);
		sourceBox.setEditable(true);
		sourceBox.setSelectedItem(document.getSource());
		AutoCompleteDecorator.decorate(sourceBox);
		sourcePanel.add(sourceBox);
		
		this.add(sourcePanel);
		
		
		JPanel sectionPanel =new JPanel(new FlowLayout(FlowLayout.LEFT));
		 sectionLabel = new JLabel("Section", JLabel.RIGHT);
		sectionLabel.setPreferredSize(lableSize);
		sectionPanel.add(sectionLabel);
		

		//sectionEntries = Dna.dna.db.getDocumentSections();
		ArrayList<String> sectionEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!sectionEntries.contains(Dna.data.getDocuments().get(i).getSection())) {
				sectionEntries.add(Dna.data.getDocuments().get(i).getSection());
			}
		}
		Collections.sort(sectionEntries);
		sectionBox = new JXComboBox(sectionEntries.toArray());
		sectionBox.setPreferredSize(fieldSize);
		sectionBox.setEditable(true);
		sectionBox.setSelectedItem(document.getSection());
		AutoCompleteDecorator.decorate(sectionBox);
		sectionPanel.add(sectionBox);
		
		this.add(sectionPanel);
		
		
		JPanel typePanel =new JPanel(new FlowLayout(FlowLayout.LEFT));
		typeLabel = new JLabel("Type", JLabel.RIGHT);
		typeLabel.setPreferredSize(lableSize);
		typePanel.add(typeLabel);
		
		//typeEntries = Dna.dna.db.getDocumentTypes();
		ArrayList<String> typeEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!typeEntries.contains(Dna.data.getDocuments().get(i).getType())) {
				typeEntries.add(Dna.data.getDocuments().get(i).getType());
			}
		}
		Collections.sort(typeEntries);
		typeBox = new JXComboBox(typeEntries.toArray());
		typeBox.setPreferredSize(fieldSize);
		typeBox.setEditable(true);
		typeBox.setSelectedItem(document.getType());
		AutoCompleteDecorator.decorate(typeBox);
		typePanel.add(typeBox);
		
		this.add(typePanel);
		
		
		JPanel notesPanel =new JPanel(new FlowLayout(FlowLayout.LEFT));
		notesLabel = new JLabel("Notes");		
		
		notesPanel.add(notesLabel);
		this.add(notesPanel);
		
		
		notesArea = new JXTextArea();
		notesArea.setText(document.getNotes());
		notesArea.setCaretPosition(0);
		notesArea.setBorder(titleField.getBorder());
		notesArea.setRows(3);
		
		notesScroll = new JScrollPane(notesArea);
		this.add(notesScroll);
		
		
		// buttons
		Icon tickIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		Icon clearIcon = new ImageIcon(getClass().getResource(
				"/icons/cross.png"));
		saveButton = new JButton("Save", tickIcon);
		saveButton.setToolTipText("Save the changes");
		cancelButton = new JButton("Cancel", clearIcon);
		cancelButton.setToolTipText("Cancel changes and refresh without saving");
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);

		// add task for cancelButton
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createEditDocumentPanel(doc);
				revalidate();
				
			}
		});

		// add task for save-button
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				saveDetails();
			}
		});
		
		
		
		this.add(buttonPanel);	
		this.setEnabled(true);
		this.revalidate();
		
	}

	//String[] coderEntries;
	String[] sourceEntries ;
	String[] sectionEntries;
	String[] typeEntries;
	
	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		super.setEnabled(enabled);
		
		if(document!= null)
		{
			titleField.setEnabled(enabled);
			 notesLabel.setEnabled(enabled);
			 
			  sectionLabel.setEnabled(enabled);
			  titleLabel.setEnabled(enabled);
			  dateLabel.setEnabled(enabled);
			  coderLabel.setEnabled(enabled);
			  sourceLabel.setEnabled(enabled);
			  typeLabel.setEnabled(enabled);
			 
			 notesArea.setEnabled(enabled);
			 dateSpinner.setEnabled(enabled);
			 coderBox.setEnabled(enabled);
			 sourceBox.setEnabled(enabled); 
			 sectionBox.setEnabled(enabled);
			 typeBox.setEnabled(enabled);
			 saveButton.setEnabled(enabled);
			 cancelButton.setEnabled(enabled);
		}
		 	
		
	}
	
	
	void saveDetails()
	{
		String title = titleField.getText();
		title = title.replaceAll("'", "''");
		Date date = (Date)dateSpinner.getValue();
		int coder = (int) coderBox.getModel().getSelectedItem();
		String source = (String) sourceBox.getModel().getSelectedItem();
		source = source.replaceAll("'", "''");
		String section = (String) sectionBox.getModel().getSelectedItem();
		section = section.replaceAll("'", "''");
		String notes = notesArea.getText();
		notes = notes.replaceAll("'", "''");
		String type = (String) typeBox.getModel().getSelectedItem();
		type = type.replaceAll("'", "''");

		//Dna.dna.db.changeDocument(document.getId(), title, date, coder, source, section, notes, type);
		int documentId = document.getId();
		Dna.data.getDocument(documentId).setTitle(title);
		Dna.data.getDocument(documentId).setDate(date);
		Dna.data.getDocument(documentId).setCoder(coder);
		Dna.data.getDocument(documentId).setSource(source);
		Dna.data.getDocument(documentId).setSection(section);
		Dna.data.getDocument(documentId).setNotes(notes);
		
		Dna.dna.gui.documentPanel.documentContainer.changeDocument(
				document.getId(), title, date, coder, source, section, notes, 
				type);
		Dna.dna.gui.documentPanel.documentContainer.sort();
		int newRow = Dna.dna.gui.documentPanel.documentContainer.
				getRowIndexById(document.getId());
		Dna.dna.gui.documentPanel.documentTable.updateUI();
		Dna.dna.gui.documentPanel.documentTable.getSelectionModel().
				setSelectionInterval(newRow, newRow);
		
		createEditDocumentPanel(document);
		revalidate();

	}
}