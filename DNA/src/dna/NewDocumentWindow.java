package dna;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
class NewDocumentWindow extends JFrame {
	
	String dbfile;
	SpinnerDateModel dateModel;
	JSpinner dateSpinner;
	JScrollPane textScroller;
	JTextArea textArea;
	JButton okButton;
	JPanel newArticlePanel;
	JTextField titleField;
	
	public NewDocumentWindow() {
		
		this.setTitle("Add new article...");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon tableAddIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
		this.setIconImage(tableAddIcon.getImage());
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel titleLabel = new JLabel("Title:");
		titleField = new JTextField(40);
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		titlePanel.add(titleLabel);
		titlePanel.add(titleField);
		
		JLabel dateLabel = new JLabel("Date:");
		dateModel = new SpinnerDateModel();
		dateSpinner = new JSpinner();
		dateModel.setCalendarField( Calendar.DAY_OF_YEAR );
		dateSpinner.setModel( dateModel );
		dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd.MM.yyyy"));
		JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		datePanel.add(dateLabel);
		datePanel.add(dateSpinner);

		textArea = new JTextArea("(paste the contents of the article here by highlighting this text and replacing it using Ctrl-V)");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		textScroller = new JScrollPane(textArea);
		textScroller.setPreferredSize(new Dimension(600, 400));
		
		textScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		okButton = new JButton("OK", okIcon);
		okButton.setToolTipText( "insert a new article based on the information you entered in this window" );
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textArea.getText();
				//TODO: check if the following line is necessary; use RTF document type instead?
				//text = text.replaceAll("\n", "<br/>");
				
				String title = titleField.getText();
				Date date = (Date)dateSpinner.getValue();
				String coder = ""; // TODO
				String source = ""; //TODO
				String notes = ""; //TODO
				String type = ""; //TODO
				Dna.dna.addDocument(title, text, date, coder, source, notes, 
						type);
				//TODO: change selection to new row
				
				int index = -1;
				for (int i = 0; i < Dna.dna.gui.documentPanel.documentTable.getRowCount(); i++) {
					if (titleField.getText().equals(Dna.dna.gui.documentPanel.
							documentContainer.get(i).getTitle())) {
						index = i;
					} //TODO: IT WOULD BE NICE IF THE ID FIELD COULD BE USED TO LOCATE THE ARTICLE (INSTEAD OF THE TEXT)
				}
				Dna.dna.gui.documentPanel.documentTable.changeSelection(index, 0, false, false);
				dispose();
			}
		});
		okButton.setEnabled(false);
		
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
					for (int i = 0; i < Dna.dna.gui.documentPanel.documentContainer.
							getRowCount(); i++) { //TODO: better: compare with db directly
						if (Dna.dna.gui.documentPanel.documentContainer.getValueAt(i, 0).
								equals(title)) {
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
		
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(titlePanel, BorderLayout.WEST);
		northPanel.add(datePanel, BorderLayout.CENTER);
		northPanel.add(okButton, BorderLayout.EAST);
		
		newArticlePanel = new JPanel(new BorderLayout());
		newArticlePanel.add(northPanel, BorderLayout.NORTH);
		newArticlePanel.add(textScroller, BorderLayout.CENTER);
		
		this.add(newArticlePanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
