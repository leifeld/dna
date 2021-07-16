package guiCoder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import dna.Dna;
import dna.Document;
import dna.Statement;

@SuppressWarnings("serial")
public class DocumentEditor extends JDialog {
	
	String dbfile;
	SpinnerDateModel dateModel;
	JSpinner dateSpinner;
	JScrollPane textScroller;
	JButton okButton;
	JPanel newArticlePanel;
	JXTextField titleField;
	JXTextArea textArea, notesArea;
	JXComboBox authorBox, sourceBox, sectionBox, typeBox;
	ArrayList<Document> documents = null;
	
	public ArrayList<Document> getDocuments() {
		return documents;
	}
	
	public DocumentEditor(int[] documentIds) {
		createGui();
		System.out.println(documentIds.length);
	}
	
	public DocumentEditor() {
		createGui();
	}

	private void createGui() {
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
		textScroller.setPreferredSize(new Dimension(700, 500));
		textScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// first column: labels
		gbc.insets = new Insets(3, 0, 3, 7);
		gbc.gridy = 0;
		gbc.gridx = 0;
		JLabel titleLabel = new JLabel("title", JLabel.RIGHT);
		fieldsPanel.add(titleLabel, gbc);

		gbc.gridy = 1;
		gbc.gridx = 0;
		JLabel dateLabel = new JLabel("date", JLabel.RIGHT);
		fieldsPanel.add(dateLabel, gbc);

		gbc.gridy = 2;
		gbc.gridx = 0;
		JLabel coderLabel = new JLabel("coder", JLabel.RIGHT);
		fieldsPanel.add(coderLabel, gbc);

		gbc.gridy = 3;
		gbc.gridx = 0;
		JLabel authorLabel = new JLabel("author", JLabel.RIGHT);
		fieldsPanel.add(authorLabel, gbc);

		gbc.gridy = 4;
		gbc.gridx = 0;
		JLabel sourceLabel = new JLabel("source", JLabel.RIGHT);
		fieldsPanel.add(sourceLabel, gbc);

		gbc.gridy = 5;
		gbc.gridx = 0;
		JLabel sectionLabel = new JLabel("section", JLabel.RIGHT);
		fieldsPanel.add(sectionLabel, gbc);

		gbc.gridy = 6;
		gbc.gridx = 0;
		JLabel typeLabel = new JLabel("type", JLabel.RIGHT);
		fieldsPanel.add(typeLabel, gbc);

		gbc.gridy = 7;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		JLabel notesLabel = new JLabel("notes", JLabel.RIGHT);
		fieldsPanel.add(notesLabel, gbc);

		gbc.gridy = 8;
		gbc.gridx = 0;
		JLabel textLabel = new JLabel("text", JLabel.RIGHT);
		fieldsPanel.add(textLabel, gbc);

		// second column: fields
		gbc.insets = new Insets(3, 0, 3, 0);
		gbc.gridy = 0;
		gbc.gridx = 1;
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
		
		gbc.gridy = 1;
		gbc.gridx = 1;
		dateModel = new SpinnerDateModel();
		dateSpinner = new JSpinner();
		dateModel.setCalendarField(Calendar.DAY_OF_YEAR);
		dateSpinner.setModel( dateModel );
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.HOUR, -12);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		dateModel.setValue(gc.getTime());
		dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd  HH:mm:ss"));
		fieldsPanel.add(dateSpinner, gbc);
		
		gbc.gridy = 2;
		gbc.gridx = 1;
		JPanel coderPanel = new CoderBadgePanel();
		fieldsPanel.add(coderPanel, gbc);
		
		gbc.gridy = 3;
		gbc.gridx = 1;
		authorBox = new JXComboBox();
		authorBox.setEditable(true);
		JDBCWorker workerAuthor = new JDBCWorker("Author");
        workerAuthor.execute();
		authorBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(authorBox);
		fieldsPanel.add(authorBox, gbc);
		
		gbc.gridy = 4;
		gbc.gridx = 1;
		sourceBox = new JXComboBox();
		JDBCWorker workerSource = new JDBCWorker("Source");
        workerSource.execute();
		sourceBox.setEditable(true);
		sourceBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(sourceBox);
		fieldsPanel.add(sourceBox, gbc);

		gbc.gridy = 5;
		gbc.gridx = 1;
		sectionBox = new JXComboBox();
		JDBCWorker workerSection = new JDBCWorker("Section");
        workerSection.execute();
		sectionBox.setEditable(true);
		sectionBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(sectionBox);
		fieldsPanel.add(sectionBox, gbc);
		
		gbc.gridy = 6;
		gbc.gridx = 1;
		typeBox = new JXComboBox();
		JDBCWorker workerType = new JDBCWorker("Type");
        workerType.execute();
		typeBox.setEditable(true);
		typeBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(typeBox);
		fieldsPanel.add(typeBox, gbc);
		
		gbc.gridy = 7;
		gbc.gridx = 1;
		notesArea = new JXTextArea("notes...");
		notesArea.setBorder(titleField.getBorder());
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesArea.setRows(4);
		JScrollPane notesScroller = new JScrollPane(notesArea);
		notesScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		fieldsPanel.add(notesScroller, gbc);
		
		gbc.gridy = 8;
		gbc.gridx = 1;
		fieldsPanel.add(textScroller, gbc);
		
		newArticlePanel = new JPanel(new BorderLayout());
		newArticlePanel.add(fieldsPanel, BorderLayout.NORTH);

		Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		okButton = new JButton("add", okIcon);
		okButton.setToolTipText( "insert a new article based on the information you entered in this window" );
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textArea.getText();
				String title = titleField.getText();
				Date date = (Date)dateSpinner.getValue();
				String author = (String) authorBox.getModel().getSelectedItem();
				String source = (String) sourceBox.getModel().getSelectedItem();
				String section = (String) sectionBox.getModel().getSelectedItem();
				String notes = notesArea.getText();
				String type = (String) typeBox.getModel().getSelectedItem();
				
				ArrayList<Document> al = new ArrayList<Document>();
				Document d = new Document(-1, Dna.sql.getConnectionProfile().getCoderId(), title, text, author, source, section, type, notes, date, new ArrayList<Statement>());
				al.add(d);
				documents = al;
				dispose();
			}
		});
		okButton.setEnabled(false);
		FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
		JPanel buttons = new JPanel(fl);
		buttons.add(okButton);
		newArticlePanel.add(buttons, BorderLayout.SOUTH);
		
		this.add(newArticlePanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	// Swing worker to populate the author, source, section, and type combo boxes without blocking the event thread and GUI
	// https://stackoverflow.com/questions/43161033/cant-add-tablerowsorter-to-jtable-produced-by-swingworker
	private class JDBCWorker extends SwingWorker<List<String>, String> {
		String field;
		
		public JDBCWorker(String field) {
			this.field = field;
		}
		
        @Override
        protected List<String> doInBackground() {
        	try (Connection conn = Dna.sql.getDataSource().getConnection();
        			PreparedStatement s = conn.prepareStatement("SELECT DISTINCT " + field + " FROM DOCUMENTS WHERE " + field + " IS NOT NULL ORDER BY " + field + ";")) {
        		ResultSet result = s.executeQuery();
    			while (result.next()) {
    				publish(result.getString(field));
    			}
			} catch (SQLException e) {
				System.err.println("Could not establish connection to database to retrieve document field entries.");
				e.printStackTrace();
			}
			return null;
        }
    	
        @SuppressWarnings("unchecked")
		@Override
        protected void process(List<String> chunks) {
    		int w = authorBox.getWidth();
    		int h = authorBox.getHeight();
            for (String row : chunks) {
            	if (field.equals("Author")) {
                    authorBox.addItem(row);
            	} else if (field.equals("Source")) {
            		sourceBox.addItem(row);
            	} else if (field.equals("Section")) {
            		sectionBox.addItem(row);
            	} else if (field.equals("Type")) {
            		typeBox.addItem(row);
            	}
            }
            authorBox.setPreferredSize(new Dimension(w, h));
            sourceBox.setPreferredSize(new Dimension(w, h));
            sectionBox.setPreferredSize(new Dimension(w, h));
            typeBox.setPreferredSize(new Dimension(w, h));
        }

        @Override
        protected void done() {
        	// nothing to do
        }
    }
}