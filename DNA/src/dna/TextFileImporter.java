package dna;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SpinnerDateModel;

import dna.dataStructures.Coder;
import dna.dataStructures.Document;

@SuppressWarnings("serial")
public class TextFileImporter extends JDialog {
	File folder;
	File[] files;
	JList<String> fileList;
	DefaultListModel<String> listModel;
	SpinnerDateModel model;
	public int coderId;
	public JTextField titlePatternField, authorPatternField, sourcePatternField, sectionPatternField, typePatternField, notesPatternField, 
			datePatternField, dateFormatField;
	public JTextField titlePreviewField, authorPreviewField, sourcePreviewField, sectionPreviewField, typePreviewField, notesPreviewField, 
			datePreviewField;
	private JTextField dateFormatPreview;
	private JCheckBox titleCheckBox, authorCheckBox, sectionCheckBox, sourceCheckBox, typeCheckBox, dateCheckBox, notesCheckBox;
	
	int good, bad = 0;
	String fn, sCurrentLine, document, title, dateString, author, source, section, notes, type;
	FileInputStream is = null;
	InputStreamReader isr = null;
	BufferedReader br = null;
	java.util.Date date;
	Document article;
	
	public TextFileImporter() {
		this.setTitle("Import text files...");
        ImageIcon tableAddIcon = new ImageIcon(getClass().getResource("/icons/table_add.png"));
        this.setIconImage(tableAddIcon.getImage());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        ImageIcon folderIcon = new ImageIcon(getClass().getResource("/icons/folder.png"));
		JButton browseButton = new JButton("Select folder", folderIcon);
        browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(TextFileImporter.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					folder = fc.getSelectedFile();
				}
				if (folder != null) {
					files = folder.listFiles();
					String[] fileNames = new String[files.length];
					for (int i = 0; i < files.length; i++) {
						fileNames[i] = files[i].getName();
						listModel.addElement(fileNames[i]);
					}
					fileList.updateUI();
				}
			}
		});

        ImageIcon updateIcon = new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png"));
        JButton updateButton = new JButton("Refresh", updateIcon);
        updateButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String inputText = fileList.getSelectedValue();
				if (inputText == null) {
					titlePreviewField.setText("");
					datePreviewField.setText("");
					authorPreviewField.setText("");
					sourcePreviewField.setText("");
					sectionPreviewField.setText("");
					typePreviewField.setText("");
					notesPreviewField.setText("");
					datePreviewField.setText("");
				} else {
					if (titleCheckBox.isSelected()) {
						titlePreviewField.setText(patternToString(inputText, titlePatternField.getText()));
					} else {
						titlePreviewField.setText(titlePatternField.getText());
					}
					if (dateCheckBox.isSelected()) {
						datePreviewField.setText(patternToString(inputText, datePatternField.getText()));
						dateFormatPreview.setText(stringToDate(datePreviewField.getText(), dateFormatField.getText()).toString());
					} else {
						datePreviewField.setText(datePatternField.getText());
						dateFormatPreview.setText(stringToDate(datePreviewField.getText(), dateFormatField.getText()).toString());
					}
					if (authorCheckBox.isSelected()) {
						authorPreviewField.setText(patternToString(inputText, authorPatternField.getText()));
					} else {
						authorPreviewField.setText(authorPatternField.getText());
					}
					if (sectionCheckBox.isSelected()) {
						sectionPreviewField.setText(patternToString(inputText, sectionPatternField.getText()));
					} else {
						sectionPreviewField.setText(sectionPatternField.getText());
					}
					if (sourceCheckBox.isSelected()) {
						sourcePreviewField.setText(patternToString(inputText, sourcePatternField.getText()));
					} else {
						sourcePreviewField.setText(sourcePatternField.getText());
					}
					if (typeCheckBox.isSelected()) {
						typePreviewField.setText(patternToString(inputText, typePatternField.getText()));
					} else {
						typePreviewField.setText(typePatternField.getText());
					}
					if (notesCheckBox.isSelected()) {
						notesPreviewField.setText(patternToString(inputText, notesPatternField.getText()));
					} else {
						notesPreviewField.setText(notesPatternField.getText());
					}
				}
			}
		});
        
        ImageIcon goIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
        JButton goButton = new JButton("Import files", goIcon);
        goButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (files != null) {
					good = 0;
					bad = 0;
					//TextImporter ti = new TextImporter();
					Thread ti = new Thread( new TextImporter(), "Import text files" );
					ti.start();
				}
			}
		});
        
        listModel = new DefaultListModel<String>();
        fileList = new JList<String>(listModel);
        JScrollPane fileListScroller = new JScrollPane(fileList);
        fileListScroller.setPreferredSize(new Dimension(800, 200));
        
        JPanel filePanel = new JPanel(new BorderLayout());
        
        filePanel.add(fileListScroller, BorderLayout.CENTER);
        this.add(filePanel, BorderLayout.NORTH);
        

		JPanel patternPanel = new JPanel(new GridBagLayout());
		TitledBorder tb2 = new TitledBorder(new EtchedBorder(), "Metadata" );
		tb2.setTitleJustification(TitledBorder.CENTER);
		patternPanel.setBorder(tb2);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		JLabel patternHeaderLabel = new JLabel("Pattern");
		patternPanel.add(patternHeaderLabel, gbc);
		
		gbc.gridx = 2;
		JLabel checkLabel = new JLabel("Regex  ");
		patternPanel.add(checkLabel, gbc);
		
		gbc.gridx = 3;
		JLabel previewHeaderLabel = new JLabel("Preview");
		patternPanel.add(previewHeaderLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		JLabel titleLabel = new JLabel("Title: ");
		patternPanel.add(titleLabel, gbc);

		gbc.gridy = 2;
		JLabel authorLabel = new JLabel("Author: ");
		patternPanel.add(authorLabel, gbc);

		gbc.gridy = 3;
		JLabel sourceLabel = new JLabel("Source: ");
		patternPanel.add(sourceLabel, gbc);

		gbc.gridy = 4;
		JLabel sectionLabel = new JLabel("Section: ");
		patternPanel.add(sectionLabel, gbc);

		gbc.gridy = 5;
		JLabel typeLabel = new JLabel("Type: ");
		patternPanel.add(typeLabel, gbc);

		gbc.gridy = 6;
		JLabel notesLabel = new JLabel("Notes: ");
		patternPanel.add(notesLabel, gbc);

		gbc.gridy = 7;
		JLabel coderLabel = new JLabel("Coder: ");
		patternPanel.add(coderLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		titlePatternField = new JTextField(".+(?=\\.txt)");
		titlePatternField.setColumns(35);
		patternPanel.add(titlePatternField, gbc);
		
		gbc.gridy = 2;
		authorPatternField = new JTextField("(?<=.+? - ).+?(?= -)");
		authorPatternField.setColumns(35);
		patternPanel.add(authorPatternField, gbc);

		gbc.gridy = 3;
		sourcePatternField = new JTextField("(?<=.+? - )[a-zA-Z ]+(?= - [A-Z0-9\\( \\)]+\\.txt)");
		sourcePatternField.setColumns(35);
		patternPanel.add(sourcePatternField, gbc);

		gbc.gridy = 4;
		sectionPatternField = new JTextField("");
		sectionPatternField.setColumns(35);
		patternPanel.add(sectionPatternField, gbc);

		gbc.gridy = 5;
		typePatternField = new JTextField("[A-Z-]+(?=([0-9\\( \\)])*\\.txt)");
		typePatternField.setColumns(35);
		patternPanel.add(typePatternField, gbc);

		gbc.gridy = 6;
		notesPatternField = new JTextField("");
		notesPatternField.setColumns(35);
		patternPanel.add(notesPatternField, gbc);

		gbc.gridy = 7;
		gbc.gridwidth = 2;
		JPanel coderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		coderId = Dna.data.getActiveCoder();
		Coder coder = Dna.data.getCoderById(coderId);
		String name = coder.getName();
		JLabel coderName = new JLabel(name);
		JButton colorButton = new JButton();
		colorButton.setPreferredSize(new Dimension(16, 16));
		colorButton.setEnabled(false);
		colorButton.setBackground(coder.getColor());
		coderPanel.add(colorButton);
		coderPanel.add(coderName);
		patternPanel.add(coderPanel, gbc);
		
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridy = 1;
		titleCheckBox = new JCheckBox();
		titleCheckBox.setSelected(true);
		patternPanel.add(titleCheckBox, gbc);

		gbc.gridy = 2;
		authorCheckBox = new JCheckBox();
		authorCheckBox.setSelected(true);
		patternPanel.add(authorCheckBox, gbc);

		gbc.gridy = 3;
		sourceCheckBox = new JCheckBox();
		sourceCheckBox.setSelected(true);
		patternPanel.add(sourceCheckBox, gbc);

		gbc.gridy = 4;
		sectionCheckBox = new JCheckBox();
		sectionCheckBox.setSelected(true);
		patternPanel.add(sectionCheckBox, gbc);

		gbc.gridy = 5;
		typeCheckBox = new JCheckBox();
		typeCheckBox.setSelected(true);
		patternPanel.add(typeCheckBox, gbc);

		gbc.gridy = 6;
		notesCheckBox = new JCheckBox();
		notesCheckBox.setSelected(true);
		patternPanel.add(notesCheckBox, gbc);

		gbc.gridy = 8;
		dateCheckBox = new JCheckBox();
		dateCheckBox.setSelected(true);
		patternPanel.add(dateCheckBox, gbc);

		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 3;
		gbc.gridy = 1;
		titlePreviewField = new JTextField("");
		titlePreviewField.setColumns(35);
		titlePreviewField.setEditable(false);
		patternPanel.add(titlePreviewField, gbc);

		gbc.gridy = 2;
		authorPreviewField = new JTextField("");
		authorPreviewField.setColumns(35);
		authorPreviewField.setEditable(false);
		patternPanel.add(authorPreviewField, gbc);

		gbc.gridy = 3;
		sourcePreviewField = new JTextField("");
		sourcePreviewField.setColumns(35);
		sourcePreviewField.setEditable(false);
		patternPanel.add(sourcePreviewField, gbc);

		gbc.gridy = 4;
		sectionPreviewField = new JTextField("");
		sectionPreviewField.setColumns(35);
		sectionPreviewField.setEditable(false);
		patternPanel.add(sectionPreviewField, gbc);

		gbc.gridy = 5;
		typePreviewField = new JTextField("");
		typePreviewField.setColumns(35);
		typePreviewField.setEditable(false);
		patternPanel.add(typePreviewField, gbc);

		gbc.gridy = 6;
		notesPreviewField = new JTextField("");
		notesPreviewField.setColumns(35);
		notesPreviewField.setEditable(false);
		patternPanel.add(notesPreviewField, gbc);

		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridy = 8;
        gbc.gridx = 0;
        JLabel dateLabel = new JLabel("Date: ");
        patternPanel.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        datePatternField = new JTextField("[0-9]{2}\\.[0-9]{2}\\.[0-9]{4}");
		datePatternField.setColumns(35);
        patternPanel.add(datePatternField, gbc);
        
        gbc.gridx = 3;
        datePreviewField = new JTextField("");
		datePreviewField.setColumns(35);
		datePreviewField.setEditable(false);
        patternPanel.add(datePreviewField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 9;
        JLabel dateFormatLabel = new JLabel("Format: ");
        patternPanel.add(dateFormatLabel, gbc);
        
        gbc.gridx = 1;
        dateFormatField = new JTextField("dd.MM.yyyy");
		dateFormatField.setColumns(35);
        patternPanel.add(dateFormatField, gbc);
		
        gbc.gridx = 3;
        dateFormatPreview = new JTextField("");
		dateFormatPreview.setColumns(35);
		dateFormatPreview.setEditable(false);
        patternPanel.add(dateFormatPreview, gbc);
        
		this.add(patternPanel, BorderLayout.CENTER);
        
        JPanel lowerButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lowerButtons.add(browseButton);
        lowerButtons.add(updateButton);
        lowerButtons.add(goButton);
        this.add(lowerButtons, BorderLayout.SOUTH);
        
        
        this.setModal(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public String patternToString(String text, String pattern) {
		Pattern p;
		try {
			p = Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			return("");
		}
		Matcher m = p.matcher(text);
		if (m.find()) {
			try {
				String string = m.group(0);
			    return string;
			} catch (IndexOutOfBoundsException e) {
				return("");
			}
		} else {
			return "";
		}
	}
	
	public java.util.Date stringToDate(String text, String dateFormat) {
		DateFormat format = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
		try {
			java.util.Date date = format.parse(text);
			return date;
		} catch (ParseException e) {
			return new GregorianCalendar().getTime();
		}
	}
	
	public class TextImporter implements Runnable {
		ProgressMonitor progressMonitor;
		
		public TextImporter() {
			
		}
		
		@Override
		public void run() {
			progressMonitor = new ProgressMonitor(Dna.gui, "Importing text files...", "", 0, files.length - 1);
			progressMonitor.setMillisToDecideToPopup(1);
			
			for (int i = 0; i < files.length; i++) {
				progressMonitor.setProgress(i);
				if (progressMonitor.isCanceled()) {
					break;
				}
				fn = files[i].getName();
				try {
					is = new FileInputStream(files[i]);
					isr = new InputStreamReader(is, "UTF-8");
					br = new BufferedReader(isr);
					document = "";
					while ((sCurrentLine = br.readLine()) != null) {
						document = document + sCurrentLine + "\n";
					}
					
					dateString = "";
					if (dateCheckBox.isSelected()) {
						dateString = patternToString(fn, datePatternField.getText());
					} else {
						dateString = datePatternField.getText();
					}
					date = stringToDate(dateString, dateFormatField.getText());
					
					if (titleCheckBox.isSelected()) {
						title = patternToString(fn, titlePatternField.getText());
					} else {
						title = titlePatternField.getText();
					}
					
					if (authorCheckBox.isSelected()) {
						author = patternToString(fn, authorPatternField.getText());
					} else {
						author = authorPatternField.getText();
					}

					if (sourceCheckBox.isSelected()) {
						source = patternToString(fn, sourcePatternField.getText());
					} else {
						source = sourcePatternField.getText();
					}

					if (sectionCheckBox.isSelected()) {
						section = patternToString(fn, sectionPatternField.getText());
					} else {
						section = sectionPatternField.getText();
					}

					if (typeCheckBox.isSelected()) {
						type = patternToString(fn, typePatternField.getText());
					} else {
						type = typePatternField.getText();
					}

					if (notesCheckBox.isSelected()) {
						notes = patternToString(fn, notesPatternField.getText());
					} else {
						notes = notesPatternField.getText();
					}
					
					article = new Document(
							Dna.data.generateNewId("documents"), 
							title, 
							document, 
							coderId, 
							author, 
							source, 
							section, 
							notes, 
							type, 
							date
					);
					Dna.dna.addDocument(article);
					good++;
				} catch (Exception ex) {
					bad++;
				}
				
			}
			Dna.gui.refreshGui();
			JOptionPane.showMessageDialog(TextFileImporter.this, good + " documents were successfully imported, " + bad + " omitted.");
			dispose();
		}
	}
}
