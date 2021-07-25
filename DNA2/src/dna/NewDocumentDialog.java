package dna;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import dna.dataStructures.Coder;
import dna.dataStructures.Document;

@SuppressWarnings("serial")
class NewDocumentDialog extends JDialog {
	
	String dbfile;
	SpinnerDateModel dateModel;
	JSpinner dateSpinner;
	JScrollPane textScroller;
	JButton okButton;
	JPanel newArticlePanel;
	JXTextField titleField;
	JXTextArea textArea, notesArea;
	JXComboBox authorBox, sourceBox, sectionBox, typeBox;
	
	public NewDocumentDialog() {
		this.setModal(true);
		this.setTitle("Add new document...");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon tableAddIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
		this.setIconImage(tableAddIcon.getImage());
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		textArea = new JXTextArea("paste the contents of the document here using Ctrl-V...");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		textScroller = new JScrollPane(textArea);
		textScroller.setPreferredSize(new Dimension(600, 400));
		textScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(1, 0, 1, 5);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		JLabel titleLabel = new JLabel("title", JLabel.RIGHT);
		fieldsPanel.add(titleLabel, gbc);
		
		gbc.gridx++;
		gbc.gridwidth = 2;
		titleField = new JXTextField("title...");
		titleField.setColumns(50);
		fieldsPanel.add(titleField, gbc);

		titleField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkButton();
			}
			public void checkButton() {
				String title = titleField.getText();
				boolean duplicate = false;
				if (titleField.getText().equals("")) {
					duplicate = true;
				} else {
					for (int i = 0; i < Dna.gui.documentPanel.documentContainer.
							getRowCount(); i++) { //TODO: better: compare with db directly
						if (Dna.gui.documentPanel.documentContainer.getValueAt(i, 0).equals(title)) {
							duplicate = true;
						}
					}
				}
				if (duplicate == true) {
					okButton.setEnabled(false);
				} else {
					okButton.setEnabled(true);
				}
			}
		});
		
		gbc.gridx = 3;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(1, 0, 1, 0);
		Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		okButton = new JButton("add", okIcon);
		okButton.setToolTipText( "insert a new article based on the information you entered in this window" );
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textArea.getText();
				String title = titleField.getText();
				Date date = (Date)dateSpinner.getValue();
				int coder = Dna.data.getActiveCoder();
				String author = (String) authorBox.getModel().getSelectedItem();
				String source = (String) sourceBox.getModel().getSelectedItem();
				String section = (String) sectionBox.getModel().getSelectedItem();
				String notes = notesArea.getText();
				String type = (String) typeBox.getModel().getSelectedItem();
				int docId = Dna.data.generateNewId("documents");
				Document d = new Document(docId, title, text, coder, author, source, section, notes, type, date);
				Dna.dna.addDocument(d);
				
				int index = -1;
				for (int i = 0; i < Dna.gui.documentPanel.documentTable.getRowCount(); i++) {
					if (docId == Dna.gui.documentPanel.documentContainer.get(i).getId()) {
						index = i;
					}
				}
				Dna.gui.documentPanel.documentTable.changeSelection(index, 0, false, false);
				dispose();
			}
		});
		okButton.setEnabled(false);
		fieldsPanel.add(okButton, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.insets = new Insets(1, 0, 1, 5);
		JLabel dateLabel = new JLabel("date", JLabel.RIGHT);
		fieldsPanel.add(dateLabel, gbc);
		
		gbc.gridx++;
		dateModel = new SpinnerDateModel();
		dateSpinner = new JSpinner();
		dateModel.setCalendarField( Calendar.DAY_OF_YEAR );
		dateSpinner.setModel( dateModel );
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.HOUR, -12);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		dateModel.setValue(gc.getTime());
		dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd  HH:mm:ss"));
		fieldsPanel.add(dateSpinner, gbc);
		
		gbc.gridy++;
		gbc.gridx--;
		JLabel coderLabel = new JLabel("coder", JLabel.RIGHT);
		fieldsPanel.add(coderLabel, gbc);
		
		gbc.gridx++;
		JPanel coderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Coder coder = Dna.data.getCoderById(Dna.data.getActiveCoder());
		String name = coder.getName();
		if (name.length() > 20) {
			name = name.substring(0, 16) + "...";
		}
		JLabel coderName = new JLabel(name);
		JButton colorButton = (new JButton() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(coder.getColor());
				g.fillRect(2, 2, 14, 14);
			}
		});
		colorButton.setPreferredSize(new Dimension(18, 18));
		colorButton.setEnabled(false);
		coderPanel.add(colorButton);
		coderPanel.add(coderName);
		fieldsPanel.add(coderPanel, gbc);
		
		gbc.gridy++;
		gbc.gridx--;
		JLabel authorLabel = new JLabel("author", JLabel.RIGHT);
		fieldsPanel.add(authorLabel, gbc);
		
		gbc.gridx++;
		ArrayList<String> authorEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!authorEntries.contains(Dna.data.getDocuments().get(i).getAuthor())) {
				authorEntries.add(Dna.data.getDocuments().get(i).getAuthor());
			}
		}
		Collections.sort(authorEntries);
		authorBox = new JXComboBox(authorEntries.toArray());
		authorBox.setEditable(true);
		authorBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(authorBox);
		fieldsPanel.add(authorBox, gbc);
		
		gbc.gridy++;
		gbc.gridx--;
		JLabel sourceLabel = new JLabel("source", JLabel.RIGHT);
		fieldsPanel.add(sourceLabel, gbc);
		
		gbc.gridx++;
		ArrayList<String> sourceEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!sourceEntries.contains(Dna.data.getDocuments().get(i).getSource())) {
				sourceEntries.add(Dna.data.getDocuments().get(i).getSource());
			}
		}
		Collections.sort(sourceEntries);
		sourceBox = new JXComboBox(sourceEntries.toArray());
		sourceBox.setEditable(true);
		sourceBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(sourceBox);
		fieldsPanel.add(sourceBox, gbc);

		gbc.gridy++;
		gbc.gridx--;
		JLabel sectionLabel = new JLabel("section", JLabel.RIGHT);
		fieldsPanel.add(sectionLabel, gbc);
		
		gbc.gridx++;
		ArrayList<String> sectionEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!sectionEntries.contains(Dna.data.getDocuments().get(i).getSection())) {
				sectionEntries.add(Dna.data.getDocuments().get(i).getSection());
			}
		}
		Collections.sort(sectionEntries);
		sectionBox = new JXComboBox(sectionEntries.toArray());
		sectionBox.setEditable(true);
		sectionBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(sectionBox);
		fieldsPanel.add(sectionBox, gbc);
		
		gbc.gridy++;
		gbc.gridx--;
		gbc.insets = new Insets(1, 0, 3, 5);
		JLabel typeLabel = new JLabel("type", JLabel.RIGHT);
		fieldsPanel.add(typeLabel, gbc);
		
		gbc.gridx++;
		ArrayList<String> typeEntries = new ArrayList<String>();
		for (int i = 0; i < Dna.data.getDocuments().size(); i++) {
			if (!typeEntries.contains(Dna.data.getDocuments().get(i).getType())) {
				typeEntries.add(Dna.data.getDocuments().get(i).getType());
			}
		}
		Collections.sort(typeEntries);
		typeBox = new JXComboBox(typeEntries.toArray());
		typeBox.setEditable(true);
		typeBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(typeBox);
		fieldsPanel.add(typeBox, gbc);
		
		gbc.gridy = 1;
		gbc.gridx = 2;
		gbc.gridheight = 6;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(1, 0, 2, 0);
		notesArea = new JXTextArea("notes...");
		notesArea.setBorder(titleField.getBorder());
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		JScrollPane notesScroller = new JScrollPane(notesArea);
		notesScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		fieldsPanel.add(notesScroller, gbc);
		
		newArticlePanel = new JPanel(new BorderLayout());
		newArticlePanel.add(fieldsPanel, BorderLayout.NORTH);
		newArticlePanel.add(textScroller, BorderLayout.CENTER);
		
		this.add(newArticlePanel);
		this.pack();
		dateSpinner.grabFocus();
		okButton.setPreferredSize(new Dimension(81, titleField.getHeight()));
		dateSpinner.setPreferredSize(new Dimension(170, titleField.getHeight()));
		typeBox.setPreferredSize(new Dimension(170, titleField.getHeight()));
		sourceBox.setPreferredSize(new Dimension(170, titleField.getHeight()));
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.pack();
	}
}
