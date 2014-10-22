package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/*
 * Class: ImportWebpage 
 * Import one/multiple links and parse them into one/multiple documents
 */
//TODO: Add Warning 'no internet connection'
@SuppressWarnings("serial")
class ImportWebpage extends JPanel {

	String dbfile;
	JPanel fieldsPanel, newHTMLPanel;
	JLabel noteLabel, pathLabel, elementTitle, elementSection, elementTextBody, 
	elementDate, coderLabel, sourceLabel, typeLabel;
	JXLabel explanationLabel;
	JButton okButton, previewButton, importButton;
	JRadioButton linkRadioButton, linkListRadioButton, dateRadioButton;
	JTextField pathNameField, elementTitleField, elementSectionField, 
	elementTextField, elementDateField;
	JTextField   fileName  = new JTextField(15);
	SpinnerDateModel dateModel;
	JSpinner dateSpinner;
	JXComboBox coderBox, sourceBox, typeBox;
	JXTextArea notesArea; 
	DateExtractor de;

	public  ImportWebpage(){
		//ImageIcon tableAddIcon = new ImageIcon(getClass().getResource(
		//		"/icons/table_add.png"));
		//this.setIconImage(tableAddIcon.getImage());
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		// create a JPanel
		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(1, 0, 1, 5);

		// add NOTE with explanation on how to import html-documents
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		JLabel noteLabel = new JLabel("Note: ", JLabel.RIGHT);
		fieldsPanel.add(noteLabel, gbc);

		gbc.gridx = 1;
		gbc.gridwidth = 3;
		JXLabel explanationLabel = new JXLabel("You can import a document from "
				+ "a webpage (use the 'import webpage'-option)\nor import a text"
				+ "file (.txt) with a list of links (use the 'import link "
				+ "list'-option).\nHint: Use SelectorGadget from "
				+ "http://selectorgadget.com to identify the\nappropriate "
				+ "elements from webpages.");
		explanationLabel.setLineWrap(true);
		Font font = new Font(explanationLabel.getFont().getName(), 
				Font.ITALIC, explanationLabel.getFont().getSize());
		explanationLabel.setFont(font);
		fieldsPanel.add(explanationLabel, gbc);

		//OkButton
		gbc.gridx = 4;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(1, 0, 1, 0);
		Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		okButton = new JButton("add", okIcon);
		okButton.setToolTipText( "import new document(s) based on the " +
				"information you entered in this window" );
		okButton.setEnabled(false);
		fieldsPanel.add(okButton, gbc);

		//Webpage/Link list-buttons 
		gbc.gridx = 1;
		gbc.gridy++;
		gbc.gridwidth = 1;
		linkRadioButton = new JRadioButton("import webpage");
		fieldsPanel.add(linkRadioButton, gbc);

		gbc.gridx = 2;
		gbc.gridwidth = 2;
		linkListRadioButton = new JRadioButton("import link list");
		fieldsPanel.add(linkListRadioButton, gbc);

		linkRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				if (linkRadioButton.isSelected()){
					linkListRadioButton.setEnabled(false);
					importButton.setEnabled(false);
				}else{
					linkListRadioButton.setEnabled(true);
					importButton.setEnabled(false);
				}
			}			
		});	

		linkListRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				if (linkListRadioButton.isSelected()){
					linkRadioButton.setEnabled(false);
					importButton.setEnabled(true);
				}else{
					linkRadioButton.setEnabled(true);
					importButton.setEnabled(false);
				}
			}			
		});	

		//PreviewButton
		gbc.gridx = 4;
		gbc.gridwidth = 1; 
		gbc.insets = new Insets(1, 0, 1, 0);
		Icon previewIcon = new ImageIcon(getClass().
				getResource("/icons/application_form_magnify.png"));
		previewButton = new JButton("preview", previewIcon);
		previewButton.setToolTipText( "preview one document based on the " +
				"information you entered in this window" );
		previewButton.setEnabled(false); 
		fieldsPanel.add(previewButton, gbc);

		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				try {
					showPreviewWindow();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ParseException e1) {
					e1.printStackTrace();
				}	
			}			
		});	

		//"choose-file"-button
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.gridy++;
		JLabel importLabel = new JLabel("get document", JLabel.RIGHT);
		fieldsPanel.add(importLabel, gbc);	
		gbc.gridx = 1;
		gbc.insets = new Insets(1, 0, 1, 0);
		importButton = new JButton("Choose file");
		importButton.setToolTipText( "import document(s) based on the " +
				"information you entered in this window" );

		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {			
				// fileRadioButton activated = create a file chooser: 			
				if (linkListRadioButton.isSelected() == true){				
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".txt")
									|| f.isDirectory();
						}
						public String getDescription() {
							return "Text file " +
									"(*.txt)";
						}
					});
					//show the dialogue-window
					if (e.getSource() == importButton) {
						int returnVal = fc.showOpenDialog(ImportWebpage.this);

						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							//This is where a real application would open the file.	 
							fileName.setText(file.getName());
							pathNameField.setText(file.getPath());
						}
					}
				}
			}
		});	
		importButton.setEnabled(false);
		fieldsPanel.add(importButton, gbc);		

		//Path
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel pathLabel = new JLabel("file path/URL", JLabel.RIGHT);
		fieldsPanel.add(pathLabel, gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		pathNameField = new JTextField(15);
		pathNameField.setColumns(30);
		fieldsPanel.add(pathNameField, gbc);

		//Title 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		JLabel elementTitle = new JLabel("title element", JLabel.RIGHT);
		fieldsPanel.add(elementTitle, gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		elementTitleField = new JXTextField("title code here");
		fieldsPanel.add(elementTitleField, gbc);

		//Section 
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		JLabel elementSection = new JLabel("section element", JLabel.RIGHT);
		fieldsPanel.add(elementSection, gbc);
		gbc.gridx = 1;
		elementSectionField = new JXTextField("section code here");
		fieldsPanel.add(elementSectionField, gbc);

		//Text-body 
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel elementTextBody = new JLabel("Text body element", JLabel.RIGHT);
		fieldsPanel.add(elementTextBody, gbc);
		gbc.gridx = 1;
		elementTextField = new JXTextField("article/text body code here");
		fieldsPanel.add(elementTextField, gbc);

		//Date (including set date manually-button and field)
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel elementDate = new JLabel("date element", JLabel.RIGHT);
		fieldsPanel.add(elementDate, gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		elementDateField = new JXTextField("date code here");
		fieldsPanel.add(elementDateField, gbc);

		gbc.gridx = 2;
		dateRadioButton = new JRadioButton("set date manually");
		fieldsPanel.add(dateRadioButton, gbc);

		gbc.gridx = 3;
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
		dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, 
				"yyyy-MM-dd  HH:mm:ss"));
		fieldsPanel.add(dateSpinner, gbc);

		//Coder
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel coderLabel = new JLabel("coder", JLabel.RIGHT);
		fieldsPanel.add(coderLabel, gbc);
		gbc.gridx = 1;
		String[] coderEntries = Dna.dna.db.getDocumentCoders();
		coderBox = new JXComboBox(coderEntries);
		coderBox.setEditable(true);
		coderBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(coderBox);
		fieldsPanel.add(coderBox, gbc);

		//Source	
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel sourceLabel = new JLabel("source", JLabel.RIGHT);
		fieldsPanel.add(sourceLabel, gbc);
		gbc.gridx = 1;
		String[] sourceEntries = Dna.dna.db.getDocumentSources();
		sourceBox = new JXComboBox(sourceEntries);
		sourceBox.setEditable(true);
		sourceBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(sourceBox);
		fieldsPanel.add(sourceBox, gbc);

		// Type
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel typeLabel = new JLabel("type", JLabel.RIGHT);
		fieldsPanel.add(typeLabel, gbc);
		gbc.gridx = 1;
		String[] typeEntries = Dna.dna.db.getDocumentTypes();
		typeBox = new JXComboBox(typeEntries);
		typeBox.setEditable(true);
		typeBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(typeBox);
		fieldsPanel.add(typeBox, gbc);

		//Notes
		gbc.gridy = 4;
		gbc.gridx = 2;
		gbc.gridwidth = 2;
		gbc.gridheight = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(1, 0, 2, 0);
		notesArea = new JXTextArea("notes...");
		notesArea.setBorder(pathNameField.getBorder()); 
		fieldsPanel.add(notesArea, gbc);

		//Add document/action-listeners to obligatory fields (Path, Title, 
		//Section, Text, Date)
		elementDateField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
			public void removeUpdate(DocumentEvent e) {
				changed();
			}
			public void insertUpdate(DocumentEvent e) {
				changed();
			}
		});
		elementTitleField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
			public void removeUpdate(DocumentEvent e) {
				changed();
			}
			public void insertUpdate(DocumentEvent e) {
				changed();
			}
		});
		elementSectionField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
			public void removeUpdate(DocumentEvent e) {
				changed();
			}
			public void insertUpdate(DocumentEvent e) {
				changed();
			}
		});
		pathNameField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
			public void removeUpdate(DocumentEvent e) {
				changed();
			}
			public void insertUpdate(DocumentEvent e) {
				changed();
			}
		});
		elementTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
			public void removeUpdate(DocumentEvent e) {
				changed();
			}
			public void insertUpdate(DocumentEvent e) {
				changed();
			}
		});
		dateRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changed();
			}
		});

		newHTMLPanel = new JPanel(new BorderLayout());
		newHTMLPanel.add(fieldsPanel, BorderLayout.NORTH);

		this.add(newHTMLPanel);
		this.setVisible(true);
	}

	/*
	 *  Activate Buttons if all fields are activated 
	 */
	public void changed() {
		boolean dateBooleanBothEmpty = elementDateField.getText().equals("") && 
				dateRadioButton.isSelected() == false;
		boolean dateBooleanBothFull = elementDateField.getText().length() > 0 && 
				dateRadioButton.isSelected() == true;
		if (pathNameField.getText().equals("") || 
				elementTitleField.getText().equals("") ||
				elementSectionField.getText().equals("") ||
				elementTextField.getText().equals("") ||
				dateBooleanBothEmpty == true ||
				dateBooleanBothFull == true)
		{
			previewButton.setEnabled(false);
			okButton.setEnabled(false);}
		else {
			previewButton.setEnabled(true);
			okButton.setEnabled(true);
		}
	}

	/*
	 * Show preview window
	 */
	public void showPreviewWindow() throws IOException, ParseException{

		JPanel previewPanel; 
		JScrollPane textScrollerPreview;
		JXLabel notesAreaPreview, textAreaPreview;
		JButton okButtonPreview; 

		final JDialog previewJDialog = new JDialog();
		previewJDialog.setTitle("Preview of one to be imported document");
		previewJDialog.setLayout(new FlowLayout(FlowLayout.LEFT));
		previewJDialog.setModal(true);

		org.jsoup.nodes.Document  file = null;
		String urlName = null;
		String urlPathNameImport = pathNameField.getText() ;

		// if folder-option is chosen = parse every document in it: 
		if (linkListRadioButton.isSelected() == true) {
			File f = new File(pathNameField.getText());
			List<String> webpages = new ArrayList<String>();
			try{
				ArrayList<String> lines = getArrayListFromString(f);
				for(int x = 0; x < lines.size(); x++){
					if (lines.get(x).toString().startsWith("www.") || 
							lines.get(x).toString().startsWith("http:")){
						webpages.add(lines.get(x).toString());
					}
				}
			}
			catch(Exception en){
				en.printStackTrace();
			}
			file = (Document) Jsoup.connect(webpages.get(1)).get();
			urlName = (String) webpages.get(1).toString();
		}else{
			file = Jsoup.connect(urlPathNameImport).get();
			urlName = (String) urlPathNameImport.toString();
		}

		// get input variables
		String title = file.select(elementTitleField.getText()).text();
		String section = file.select(elementSectionField.getText()).text();	
		String source = (String) sourceBox.getSelectedItem();
		String coder = (String) coderBox.getSelectedItem();
		String type = (String) typeBox.getSelectedItem();
		String notes = (String) notesArea.getText();
		Elements textTemp = file.select(elementTextField.getText());
		textTemp.select("p").prepend("CODE_LINEBREAK_1234");
		textTemp.select("br").append("CODE_LINEBREAK_1234");
		String textTempString = textTemp.text();
		textTempString = textTempString.replaceAll("CODE_LINEBREAK_1234", "\n\n");
		String textTitle = String.format("%s \n%s, %s \nURL: %s\n\n--------------"
				+ "-------------------------------------------------\n\n", 
				title, source, section, urlName);
		String text = textTitle +"" + "" + textTempString;

		Date dateHTML = null; 
		if (dateRadioButton.isSelected() == false){
			String dateElementPreview = elementDateField.getText();
			DateExtractor de = new DateExtractor();
			String datefull = file.select(dateElementPreview).text();
			System.out.println("undlos");
			dateHTML = (Date) de.extractDate(datefull);
		}
		else{
			dateHTML = (Date) dateSpinner.getValue();
		}

		// fieldsPanelPreview: put all the variables on the panel
		JPanel fieldsPanelPreview = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// title
		gbc.gridx++;
		gbc.gridy++;
		JLabel titleLabelPreview = new JLabel("Title: ", JLabel.RIGHT);
		fieldsPanelPreview.add(titleLabelPreview, gbc);

		gbc.gridx++;
		JXLabel titlePreviewFilled = new JXLabel(title, JLabel.LEFT);
		titlePreviewFilled.setBackground(Color.white);
		titlePreviewFilled.setOpaque(true);
		titlePreviewFilled.setSize(new Dimension(480, 18));
		fieldsPanelPreview.add(titlePreviewFilled, gbc);

		// ok button
		gbc.gridx = 3;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(1, 0, 1, 0);
		Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		okButtonPreview = new JButton("ok", okIcon);
		okButtonPreview.setToolTipText( "return to import-Window" );
		okButtonPreview.setEnabled(true);
		fieldsPanelPreview.add(okButtonPreview, gbc);

		okButtonPreview.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				previewJDialog.dispose();
			}			
		});	

		// date
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel dateLabelPreview = new JLabel("Date: ", JLabel.RIGHT);
		fieldsPanelPreview.add(dateLabelPreview, gbc);

		gbc.gridx++;
		try{
			JLabel datePreview = new JLabel(dateHTML.toString());
			datePreview.setBackground(Color.white);
			datePreview.setOpaque(true);
			datePreview.setPreferredSize(new Dimension(480, 18));
			fieldsPanelPreview.add(datePreview, gbc);
		}
		//TODO: Put warning into usual DNA-warnings format
		catch(NullPointerException e) {
			String message = "\n Date not extractable.\nUse 'set date"
					+ " manually'-option."; 
			JOptionPane.showMessageDialog(new JFrame(), message, "Warning",
					JOptionPane.ERROR_MESSAGE);
		}

		// coder
		gbc.gridy++;
		gbc.gridx--;
		JLabel coderLabelPreview = new JLabel("Coder: ", JLabel.RIGHT);
		fieldsPanelPreview.add(coderLabelPreview, gbc);

		gbc.gridx++;
		JLabel coderPreview = new JLabel(coder.toString(), JLabel.LEFT);
		coderPreview.setBackground(Color.white);
		coderPreview.setOpaque(true);
		coderPreview.setPreferredSize(new Dimension(480, 18));
		fieldsPanelPreview.add(coderPreview, gbc);

		// source
		gbc.gridy++;
		gbc.gridx--;
		JLabel sourceLabelPreview = new JLabel("Source: ", JLabel.RIGHT);
		fieldsPanelPreview.add(sourceLabelPreview, gbc);

		gbc.gridx++;
		JLabel sourcePreview = new JLabel(source, JLabel.LEFT);
		sourcePreview.setBackground(Color.white);
		sourcePreview.setOpaque(true);
		sourcePreview.setPreferredSize(new Dimension(480, 18));
		fieldsPanelPreview.add(sourcePreview, gbc);

		// section
		gbc.gridy++;
		gbc.gridx--;
		JLabel sectionLabelPreview = new JLabel("Section: ", JLabel.RIGHT);
		fieldsPanelPreview.add(sectionLabelPreview, gbc);

		gbc.gridx++;
		JLabel sectionPreview = new JLabel(section, JLabel.LEFT);
		sectionPreview.setBackground(Color.white);
		sectionPreview.setOpaque(true);
		sectionPreview.setPreferredSize(new Dimension(480, 18));
		fieldsPanelPreview.add(sectionPreview, gbc);

		// type
		gbc.gridy++;
		gbc.gridx--;
		JLabel typeLabelPreview = new JLabel("Type: ", JLabel.RIGHT);
		fieldsPanelPreview.add(typeLabelPreview, gbc);

		gbc.gridx++;
		JLabel typePreview = new JLabel(type, JLabel.LEFT);
		typePreview.setBackground(Color.white);
		typePreview.setOpaque(true);
		typePreview.setPreferredSize(new Dimension(480, 18));
		fieldsPanelPreview.add(typePreview, gbc);

		// notes
		gbc.gridy++;
		gbc.gridx--;
		JLabel notesLabelPreview = new JLabel("Notes: ", JLabel.RIGHT);
		fieldsPanelPreview.add(notesLabelPreview, gbc);

		gbc.gridx++;
		gbc.gridheight = 5;
		notesAreaPreview = new JXLabel(notes);
		notesAreaPreview.setLineWrap(true);
		notesAreaPreview.setSize(500, 20);
		notesAreaPreview.setBackground(Color.white);
		notesAreaPreview.setOpaque(true);
		notesAreaPreview.setBorder(notesLabelPreview.getBorder());
		fieldsPanelPreview.add(notesAreaPreview, gbc);

		// text
		textAreaPreview = new JXLabel(text);
		textAreaPreview.setLineWrap(true);
		textAreaPreview.setSize(600, 400);
		textScrollerPreview = new JScrollPane(textAreaPreview);
		textScrollerPreview.setPreferredSize(new Dimension(600, 400));
		textScrollerPreview.setVerticalScrollBarPolicy(JScrollPane.
				VERTICAL_SCROLLBAR_ALWAYS);

		previewPanel = new JPanel(new BorderLayout());
		previewPanel.add(fieldsPanelPreview, BorderLayout.NORTH);
		previewPanel.add(textScrollerPreview, BorderLayout.CENTER);

		previewJDialog.add(previewPanel);
		previewJDialog.pack();
		previewJDialog.setLocationRelativeTo(null);
		previewJDialog.setVisible(true);
		//previewJDialog.pack();
	}

	/*
	 * Method: read in file one line at a time
	 */
	public static ArrayList<String> getArrayListFromString(File f) 
			throws FileNotFoundException {
		Scanner s;
		ArrayList<String> list = new ArrayList<String>();
		s = new Scanner(f);
		while (s.hasNext()) {
			list.add(s.next());
		}
		s.close();
		return list;
	}
}

