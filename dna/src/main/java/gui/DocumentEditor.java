package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import model.Document;
import model.Statement;

public class DocumentEditor extends JDialog {
	private static final long serialVersionUID = 8937997814159804095L;
	private JScrollPane textScroller;
	private JButton okButton;
	private JPanel newArticlePanel;
	private JXTextField titleField;
	private JXTextArea textArea, notesArea;
	private JXComboBox authorBox, sourceBox, sectionBox, typeBox;
	private DateTimePicker dateTimePicker;
	private int[] documentIds;
	private ArrayList<Document> documents;
	private boolean changesApplied = false;
	private JComboBox<Coder> coderComboBox = null;
	private CoderBadgePanel cbp = null;
	
	public DocumentEditor(int[] documentIds) {
		this.documentIds = documentIds;
		documents = Dna.sql.getDocuments(documentIds);
		createGui(documentIds.length);
	}

	public DocumentEditor() {
		createGui(0);
	}

	private void createGui(int numDocuments) {
		String toolTipText = "<html><p width=\"500\">Overwrite the document contents and meta-data "
				+ "by editing the fields shown in this editor dialog.</p>"
				+ "<p width=\"500\">Note that the document text field is only editable if there are "
				+ "no statements contained in the document.</p>"
				+ "<p width=\"500\">In the title, author, source, section, type, notes, and text "
				+ "fields, the following wildcards can be used (across the different fields) to "
				+ "represent the contents of the respective field that are currently saved "
				+ "in the database:</p>"
				+ "<dl>"
				+ "<dt><b>%title</b></dt><dd>Represents the current contents of the title field.</dd>"
				+ "<dt><b>%author</b></dt><dd>Represents the current contents of the author field.</dd>"
				+ "<dt><b>%source</b></dt><dd>Represents the current contents of the source field.</dd>"
				+ "<dt><b>%section</b></dt><dd>Represents the current contents of the section field.</dd>"
				+ "<dt><b>%type</b></dt><dd>Represents the current contents of the type field.</dd>"
				+ "<dt><b>%notes</b></dt><dd>Represents the current contents of the notes field.</dd>"
				+ "<dt><b>%text</b></dt><dd>Represents the current contents of the text field.</dd>"
				+ "<dt><b>%day</b></dt><dd>Represents the current day (1-31) of the document.</dd>"
				+ "<dt><b>%month</b></dt><dd>Represents the current month (1-12) of the document.</dd>"
				+ "<dt><b>%year</b></dt><dd>Represents the current year (e.g., 2007) of the document.</dd>"
				+ "<dt><b>%hour</b></dt><dd>Represents the current hour (0-23) of the document.</dd>"
				+ "<dt><b>%minute</b></dt><dd>Represents the current minute (0-59) of the document.</dd>"
				+ "</dl>"
				+ "<p width=\"500\">For example, you can combine title and date like this: "
				+ "%title (%day.%month.%year).</p>"
				+ "<p width=\"500\">Note that the date or time fields may show as empty when multiple "
				+ "documents are edited simultaneously. This just means the original date or time will be "
				+ "kept when saving unless edited. It is possible to edit only the date or only the time "
				+ "if necessary while keeping the other field empty.</p></html>";
		
		this.setModal(true);
		if (numDocuments == 0) {
			this.setTitle("Add new document...");
		} else {
			String s = " document...";
			if (numDocuments > 1) {
				s = " documents...";
			}
			this.setTitle("Edit " + numDocuments + s);
		}
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon tableAddIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-file-plus.png"));
		this.setIconImage(tableAddIcon.getImage());
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		textArea = new JXTextArea("paste the contents of the document here using Ctrl-V...");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		textScroller = new JScrollPane(textArea);
		textScroller.setPreferredSize(new Dimension(700, 360));
		textScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		if (numDocuments > 0) {
			textScroller.setToolTipText(toolTipText);
		}
		
		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// first column: labels
		gbc.insets = new Insets(3, 0, 3, 7);
		gbc.gridy = 0;
		gbc.gridx = 0;
		JLabel titleLabel = new JLabel("title", JLabel.RIGHT);
		if (numDocuments > 0) {
			titleLabel.setToolTipText(toolTipText);
		}
		fieldsPanel.add(titleLabel, gbc);

		gbc.gridy = 1;
		gbc.gridx = 0;
		JLabel dateLabel = new JLabel("date", JLabel.RIGHT);
		if (numDocuments > 0) {
			dateLabel.setToolTipText(toolTipText);
		}
		fieldsPanel.add(dateLabel, gbc);

		gbc.gridy = 2;
		gbc.gridx = 0;
		JLabel coderLabel = new JLabel("coder", JLabel.RIGHT);
		fieldsPanel.add(coderLabel, gbc);

		gbc.gridy = 3;
		gbc.gridx = 0;
		JLabel authorLabel = new JLabel("author", JLabel.RIGHT);
		if (numDocuments > 0) {
			authorLabel.setToolTipText(toolTipText);
		}
		fieldsPanel.add(authorLabel, gbc);

		gbc.gridy = 4;
		gbc.gridx = 0;
		JLabel sourceLabel = new JLabel("source", JLabel.RIGHT);
		if (numDocuments > 0) {
			sourceLabel.setToolTipText(toolTipText);
		}
		fieldsPanel.add(sourceLabel, gbc);

		gbc.gridy = 5;
		gbc.gridx = 0;
		JLabel sectionLabel = new JLabel("section", JLabel.RIGHT);
		if (numDocuments > 0) {
			sectionLabel.setToolTipText(toolTipText);
		}
		fieldsPanel.add(sectionLabel, gbc);

		gbc.gridy = 6;
		gbc.gridx = 0;
		JLabel typeLabel = new JLabel("type", JLabel.RIGHT);
		if (numDocuments > 0) {
			typeLabel.setToolTipText(toolTipText);
		}
		fieldsPanel.add(typeLabel, gbc);

		gbc.gridy = 7;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		JLabel notesLabel = new JLabel("notes", JLabel.RIGHT);
		if (numDocuments > 0) {
			notesLabel.setToolTipText(toolTipText);
		}
		fieldsPanel.add(notesLabel, gbc);

		gbc.gridy = 8;
		gbc.gridx = 0;
		JLabel textLabel = new JLabel("text", JLabel.RIGHT);
		if (numDocuments > 0) {
			textLabel.setToolTipText(toolTipText);
		}
		fieldsPanel.add(textLabel, gbc);

		// second column: fields
		gbc.insets = new Insets(3, 0, 3, 0);
		gbc.gridy = 0;
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		titleField = new JXTextField("paste the title of the document here using Ctrl-V...");
		titleField.setColumns(60);
		if (numDocuments > 0) {
			titleField.setToolTipText(toolTipText);
		}
		fieldsPanel.add(titleField, gbc);

		if (numDocuments < 2) {
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
					boolean problem = false;
					if (titleField.getText().equals("")) {
						problem = true;
					}
					if (problem == true) {
						okButton.setEnabled(false);
					} else {
						okButton.setEnabled(true);
					}
				}
			});
		}
		
		gbc.gridy = 1;
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("dd MM yyyy");
        dateSettings.setFormatForDatesBeforeCommonEra("dd MM uuuu");
        TimePickerSettings timeSettings = new TimePickerSettings();
        timeSettings.setFormatForDisplayTime("HH:mm:ss");
        timeSettings.setFormatForMenuTimes("HH:mm:ss");
        dateTimePicker = new DateTimePicker(dateSettings, timeSettings);
        dateTimePicker.getDatePicker().setDateToToday();
        dateTimePicker.getTimePicker().setTime(LocalTime.MIDNIGHT);
		ImageIcon dateIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-calendar-event.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        JButton dateButton = dateTimePicker.getDatePicker().getComponentToggleCalendarButton();
        dateButton.setText("");
        dateButton.setIcon(dateIcon);
		ImageIcon timeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-clock.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        JButton timeButton = dateTimePicker.getTimePicker().getComponentToggleTimeMenuButton();
        timeButton.setText("");
        timeButton.setIcon(timeIcon);
		if (numDocuments > 0) {
			dateTimePicker.setToolTipText(toolTipText);
			dateButton.setToolTipText(toolTipText);
			timeButton.setToolTipText(toolTipText);
		}
        fieldsPanel.add(dateTimePicker, gbc);
		
		gbc.gridy = 2;
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		if (Dna.sql.getActiveCoder().isPermissionEditOthersDocuments()) {
			// create list of eligible coders
			List<Coder> eligibleCoders = Dna.sql.getCoders()
					.stream()
					.filter(e -> e.getId() == Dna.sql.getActiveCoder().getId() || (e.getId() != Dna.sql.getActiveCoder().getId() && Dna.sql.getActiveCoder().isPermissionEditOthersDocuments(e.getId())))
					.sorted()
					.collect(Collectors.toList());
			if (eligibleCoders.size() == 1) { // only one eligible coder (must be the active coder); create coder badge panel instead of combo box
				this.cbp = new CoderBadgePanel(eligibleCoders.get(0));
				fieldsPanel.add(this.cbp, gbc);
			} else {
				// check if there is only a single coder across the documents and add a fake coder for multiple coders coder varies across documents
				boolean oneCoder = (numDocuments == 0 || documents
						.stream()
						.mapToInt(Document::getCoder)
						.distinct()
						.limit(2)
						.count() <= 1);
				if (!oneCoder) {
					eligibleCoders.add(0, new Coder(-1, "(keep multiple coders)", new model.Color(0, 0, 0)));
				}
				
				// create and populate combo box with coders
				ArrayList<Coder> coderArrayList = new ArrayList<Coder>(eligibleCoders);
				this.coderComboBox = new JComboBox<Coder>();
				CoderComboBoxModel comboBoxModel = new CoderComboBoxModel(coderArrayList);
				this.coderComboBox.setModel(comboBoxModel);
				this.coderComboBox.setRenderer(new CoderComboBoxRenderer(18, 0, 97));
				
				// select the right coder and add combo box to panel
				if (numDocuments == 0) {
					this.coderComboBox.setSelectedIndex(IntStream.range(0, eligibleCoders.size()).filter(i -> Dna.sql.getActiveCoder().getId() == eligibleCoders.get(i).getId()).findFirst().getAsInt());
				} else if (oneCoder) {
					this.coderComboBox.setSelectedIndex(IntStream.range(0, eligibleCoders.size()).filter(i -> documents.get(0).getCoder() == eligibleCoders.get(i).getId()).findFirst().getAsInt());
				} else {
					this.coderComboBox.setSelectedIndex(0);
				}
				fieldsPanel.add(this.coderComboBox, gbc);
			}
		} else { // no permission to add other coders' documents; create coder badge panel
			this.cbp = new CoderBadgePanel(Dna.sql.getActiveCoder());
			fieldsPanel.add(this.cbp, gbc);
		}
		
		gbc.gridy = 3;
		gbc.gridx = 1;
		authorBox = new JXComboBox();
		authorBox.setEditable(true);
		AutoCompleteDecorator.decorate(authorBox);
		if (numDocuments > 0) {
			authorBox.setToolTipText(toolTipText);
		}
		authorBox.setRenderer(new ComboBoxRenderer());
		fieldsPanel.add(authorBox, gbc);
		
		gbc.gridy = 4;
		gbc.gridx = 1;
		sourceBox = new JXComboBox();
		sourceBox.setEditable(true);
		sourceBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(sourceBox);
		if (numDocuments > 0) {
			sourceBox.setToolTipText(toolTipText);
		}
		sourceBox.setRenderer(new ComboBoxRenderer());
		fieldsPanel.add(sourceBox, gbc);

		gbc.gridy = 5;
		gbc.gridx = 1;
		sectionBox = new JXComboBox();
		sectionBox.setEditable(true);
		sectionBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(sectionBox);
		if (numDocuments > 0) {
			sectionBox.setToolTipText(toolTipText);
		}
		sectionBox.setRenderer(new ComboBoxRenderer());
		fieldsPanel.add(sectionBox, gbc);
		
		gbc.gridy = 6;
		gbc.gridx = 1;
		typeBox = new JXComboBox();
		typeBox.setEditable(true);
		typeBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(typeBox);
		if (numDocuments > 0) {
			typeBox.setToolTipText(toolTipText);
		}
		typeBox.setRenderer(new ComboBoxRenderer());
		JDBCWorker worker = new JDBCWorker(numDocuments == 0);
        worker.execute();
		fieldsPanel.add(typeBox, gbc);

		gbc.gridy = 7;
		gbc.gridx = 1;
		notesArea = new JXTextArea("notes...");
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesArea.setRows(4);
		JScrollPane notesScroller = new JScrollPane(notesArea);
		notesScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		if (numDocuments > 0) {
			notesScroller.setToolTipText(toolTipText);
		}
		fieldsPanel.add(notesScroller, gbc);
		
		gbc.gridy = 8;
		gbc.gridx = 1;
		fieldsPanel.add(textScroller, gbc);
		
		newArticlePanel = new JPanel(new BorderLayout());
		newArticlePanel.add(fieldsPanel, BorderLayout.CENTER);

		FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
		JPanel buttons = new JPanel(fl);
		ImageIcon cancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		JButton cancelButton = new JButton("Cancel", cancelIcon);
		cancelButton.setToolTipText("close this window without making any changes");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttons.add(cancelButton);
		
		ImageIcon okIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-check.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
		String okString = "Add";
		if (numDocuments > 0) {
			okString = "Update";
		}
		okButton = new JButton(okString, okIcon);
		if (numDocuments == 0) {
			okButton.setToolTipText("insert a new article based on the information you entered in this window");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int coder = 1;
					if (DocumentEditor.this.cbp != null) {
						coder = cbp.getCoder().getId();
					} else {
						coder = ((Coder) coderComboBox.getSelectedItem()).getId();
					}
					String text = textArea.getText();
					String title = titleField.getText().substring(0, Math.min(190, titleField.getText().length()));
					LocalDateTime dateTime = dateTimePicker.getDateTimeStrict();
					String author = ((String) authorBox.getModel().getSelectedItem()).substring(0, Math.min(190, ((String) authorBox.getModel().getSelectedItem()).length()));
					String source = ((String) sourceBox.getModel().getSelectedItem()).substring(0, Math.min(190, ((String) sourceBox.getModel().getSelectedItem()).length()));
					String section = ((String) sectionBox.getModel().getSelectedItem()).substring(0, Math.min(190, ((String) sectionBox.getModel().getSelectedItem()).length()));
					String notes = notesArea.getText();
					String type = ((String) typeBox.getModel().getSelectedItem()).substring(0, Math.min(190, ((String) typeBox.getModel().getSelectedItem()).length()));
					
					ArrayList<Document> al = new ArrayList<Document>();
					Document d = new Document(-1, coder, title, text, author, source, section, type, notes, dateTime, new ArrayList<Statement>());
					al.add(d);
					documents = al;
					documentIds = Dna.sql.addDocuments(documents);
					changesApplied = true;
					LogEvent l = new LogEvent(Logger.MESSAGE,
							"[GUI] A new document was added to the database.",
							"A new document was manually added to the database by clicking on the Add button in a New Document dialog window.");
					Dna.logger.log(l);
					dispose();
				}
			});
			okButton.setEnabled(false);
		} else {
			okButton.setToolTipText("save updated document data into the database");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int coder = 1;
					if (DocumentEditor.this.cbp != null) {
						coder = cbp.getCoder().getId();
					} else {
						coder = ((Coder) coderComboBox.getSelectedItem()).getId(); // can be -1 for keeping existing coders; needs to be taken care of in the Sql.updateDocuments method
					}
					String message = "Are you sure you want to recode " + documentIds.length + " documents and save the changes to the database?";
					int dialog = JOptionPane.showConfirmDialog(DocumentEditor.this, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						LocalDate ld = dateTimePicker.getDatePicker().getDate();
						LocalTime lt = dateTimePicker.getTimePicker().getTime();
						Dna.sql.updateDocuments(documentIds, coder, titleField.getText(), textArea.getText(), (String) authorBox.getSelectedItem(), (String) sourceBox.getSelectedItem(), (String) sectionBox.getSelectedItem(), (String) typeBox.getSelectedItem(), notesArea.getText(), ld, lt);
						changesApplied = true;
						LogEvent l = new LogEvent(Logger.MESSAGE,
								"[GUI] " + documentIds.length + " documents were updated in the database.",
								"Using a Document Editor dialog window, the meta-data of " + documentIds.length + " documents were updated in the database.");
						Dna.logger.log(l);
					}
					dispose();
				}
			});
		}
		buttons.add(okButton);
		newArticlePanel.add(buttons, BorderLayout.SOUTH);
		
		// add contents for editing if this is not a new document
		if (numDocuments > 0) {
			String contentTitle = documents.get(0).getTitle();
			titleField.setText(contentTitle);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getTitle().equals(contentTitle)) {
						titleField.setText("%title");
						titleField.setPrompt("(Overwrite all titles at once by replacing this text.)");
						break;
					}
				}
			}
			String contentText = documents.get(0).getText();
			if (Dna.sql.documentsContainStatements(documentIds) == true) {
				textArea.setEditable(false);
			}
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getText().equals(contentText)) {
						textArea.setText("%text");
						textArea.setPrompt("(Overwrite all texts at once by replacing this text.)");
						break;
					}
				}
			}
			if (textArea.getText().equals("")) {
				textArea.setText(contentText);
			}
			String contentNotes = documents.get(0).getNotes();
			notesArea.setText(contentNotes);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getNotes().equals(contentNotes)) {
						notesArea.setText("%notes");
						notesArea.setPrompt("(Overwrite all notes at once by replacing this text.)");
						break;
					}
				}
			}
			String contentAuthor = documents.get(0).getAuthor();
			authorBox.setSelectedItem(contentAuthor);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getAuthor().equals(contentAuthor)) {
						authorBox.setSelectedItem("%author");
						break;
					}
				}
			}
			String contentSource = documents.get(0).getSource();
			sourceBox.setSelectedItem(contentSource);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getSource().equals(contentSource)) {
						sourceBox.setSelectedItem("%source");
						break;
					}
				}
			}
			String contentSection = documents.get(0).getSection();
			sectionBox.setSelectedItem(contentSection);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getSection().equals(contentSection)) {
						sectionBox.setSelectedItem("%section");
						break;
					}
				}
			}
			String contentType = documents.get(0).getType();
			typeBox.setSelectedItem(contentType);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getType().equals(contentType)) {
						typeBox.setSelectedItem("%type");
						break;
					}
				}
			}
			LocalDateTime contentDateTime = documents.get(0).getDateTime();
			dateTimePicker.setDateTimeStrict(contentDateTime);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getDateTime().equals(contentDateTime)) {
						dateTimePicker.clear();
						break;
					}
				}
			}
		}
		
		this.add(newArticlePanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Check if any changes have been written to the database.
	 * 
	 * @return  Have the document(s) been added or updated in the database?
	 */
	boolean isChangesApplied() {
		return changesApplied;
	}
	
	/**
	 * Get the document IDs. If a new document is generated, this is an int
	 * array with one element, the document ID that is generated by adding the
	 * document to the database. If existing documents are edited, the int array
	 * contains their document IDs, but they are already known to the main
	 * window class anyway, so this is more useful to retrieve when creating a
	 * new document to update the GUI accordingly.
	 * 
	 * @return An array of document IDs.
	 */
	int[] getDocumentIds() {
		return documentIds;
	}
	
	/**
	 * Swing worker to populate the author, source, section, and type combo
	 * boxes without blocking the event thread and GUI.
	 * 
	 * @see <a href="https://stackoverflow.com/questions/43161033/cant-add-tablerowsorter-to-jtable-produced-by-swingworker" target="_top">https://stackoverflow.com/questions/43161033/</a>
	 * @see <a href="https://stackoverflow.com/questions/68884145/how-do-i-use-a-jdbc-swing-worker-with-connection-pooling-ideally-while-separati" target="_top">https://stackoverflow.com/questions/68884145/</a>
	 */
	private class JDBCWorker extends SwingWorker<List<String[]>, String[]> {
		boolean newDocument;
		
		/**
		 * Initialize JDBC worker.
		 */
		public JDBCWorker(boolean newDocument) {
			this.newDocument = newDocument;
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Initializing thread to populate Document Editor: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").",
					"Initializing a new thread to populate the author, source, section, and type combo boxes in a Document Editor dialog window: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").");
			Dna.logger.log(l);
		}
		
        @Override
        protected List<String[]> doInBackground() {
        	String sql = "SELECT DISTINCT * " + 
    				"FROM (" + 
    				"SELECT 'Author' AS Field, Author as Value FROM DOCUMENTS " + 
    				"UNION ALL " + 
    				"SELECT 'Source' AS Field, Source as Value FROM DOCUMENTS " + 
    				"UNION ALL " + 
    				"SELECT 'Section' AS Field, Section as Value FROM DOCUMENTS " + 
    				"UNION ALL " + 
    				"SELECT 'Type' AS Field, Type as Value FROM DOCUMENTS) AS RESULT " + 
    				"WHERE Field IS NOT NULL ORDER BY Field, Value;";
        	try (Connection conn = Dna.sql.getDataSource().getConnection();
        			PreparedStatement s = conn.prepareStatement(sql);
        			ResultSet rs = s.executeQuery();) {
				while (rs.next()) {
					String[] pair = new String[] {rs.getString("Field"), rs.getString("Value")};
					publish(pair);
				}
			} catch (SQLException e) {
				LogEvent le = new LogEvent(Logger.WARNING,
						"[GUI] Could not retrieve document fields from database.",
						"The document editor swing worker tried to retrieve all unique values for the author, source, section, and type fields of all documents from the database to display them in combo boxes, but some or all values could not be retrieved because there was a problem while processing the result set. The combo box choices may be incomplete.",
						e);
				Dna.logger.log(le);
			}
			return null;
        }
    	
        @SuppressWarnings("unchecked")
		protected void process(List<String[]> chunks) {
            for (String[] p: chunks) {
            	if (p[0].equals("Author")) {
                    authorBox.addItem(p[1]);
            	} else if (p[0].equals("Source")) {
            		sourceBox.addItem(p[1]);
            	} else if (p[0].equals("Section")) {
            		sectionBox.addItem(p[1]);
            	} else if (p[0].equals("Type")) {
            		typeBox.addItem(p[1]);
            	}
            }
        }

        @Override
        protected void done() {
        	int w = authorBox.getWidth();
    		int h = authorBox.getHeight();
            authorBox.setPreferredSize(new Dimension(w, h));
            sourceBox.setPreferredSize(new Dimension(w, h));
            sectionBox.setPreferredSize(new Dimension(w, h));
            typeBox.setPreferredSize(new Dimension(w, h));
            if (newDocument) {
            	authorBox.setSelectedItem("");
        		sourceBox.setSelectedItem("");
        		sectionBox.setSelectedItem("");
        		typeBox.setSelectedItem("");
            }
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] Closing thread to populate Document Editor: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").",
					"All combo boxes in the Document Editor window have been filled. Closing thread to populate Document Editor: " + Thread.currentThread().getName() + " (" + Thread.currentThread().getId() + ").");
			Dna.logger.log(l);
        }
    }
	
	/**
	 * Combo box renderer for the author, source, section, and type combo boxes.
	 * The renderer cuts of entries after 100 words to ensure the fields do not
	 * get too wide.
	 */
	private class ComboBoxRenderer implements ListCellRenderer<String> {
		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
			int l = value.length();
			if (l > 100) {
				value = value.substring(0, Math.min(97, value.length())) + "...";
			}
			JLabel label = new JLabel(value);
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(label, BorderLayout.CENTER);
			if (isSelected) {
				panel.setBackground(UIManager.getColor("List.selectionBackground"));
			}
			return panel;
		}
	}
}