package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import model.Regex;
import model.Statement;

/**
 * Text panel, which displays the text of the selected document and paints the
 * statements in the given document in different colors. It keeps the current
 * document ID on record to paint only the statements in the current document.
 */
class TextPanel extends JPanel {
	private static final long serialVersionUID = -8094978928012991210L;
	private JTextPane textWindow;
	private JScrollPane textScrollPane;
	private DefaultStyledDocument doc;
	private StyleContext sc;
	private int documentId;
	
	/**
	 * Create a new text panel.
	 * @throws Exception 
	 */
	TextPanel() {
		this.setLayout(new BorderLayout());
		sc = new StyleContext();
	    doc = new DefaultStyledDocument(sc);
		textWindow = new JTextPane(doc);

	    // Create and add the main document style
	    Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
	    final Style mainStyle = sc.addStyle("MainStyle", defaultStyle);
	    StyleConstants.setLeftIndent(mainStyle, 16);
	    StyleConstants.setRightIndent(mainStyle, 16);
	    StyleConstants.setFirstLineIndent(mainStyle, 16);
	    StyleConstants.setFontFamily(mainStyle, "Serif");
	    StyleConstants.setFontSize(mainStyle, 12);
	    
	    Font font = new Font("Monospaced", Font.PLAIN, 14);
        textWindow.setFont(font);
	    
		textWindow.setEditable(false);

		textScrollPane = new JScrollPane(textWindow);
		textScrollPane.setPreferredSize(new Dimension(500, 450));
		textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(textScrollPane);
	}
	
	/**
	 * Return the text pane component.
	 * 
	 * @return The text pane in which the document text is displayed.
	 */
	JTextPane getTextWindow() {
		return textWindow;
	}

	/**
	 * Return the text scroll pane.
	 * 
	 * @return The text scroll pane that holds the text pane.
	 */
	JScrollPane getTextScrollPane() {
		return textScrollPane;
	}
	
	/**
	 * Get the current vertical scroll location. This can be used to restore the
	 * scroll location after reloading the document data from the database.
	 * 
	 * @return  An integer giving the vertical scroll position.
	 */
	int getVerticalScrollLocation() {
		return (int) textScrollPane.getViewport().getViewPosition().getY(); // get the scroll position to restore it later
	}

	/**
	 * Set the vertical scroll location. This can be used to restore the scroll
	 * location after reloading the document data from the database.
	 * 
	 * @param verticalScrollLocation  The vertical scroll location.
	 */
	void setVerticalScrollLocation(int verticalScrollLocation) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textScrollPane.getViewport().setViewPosition(new Point(0, verticalScrollLocation)); // scroll to previously saved position
			}
		});
	}

	/**
	 * Set the contents of the text panel, including the document ID and text,
	 * and paint the statements in the text, then scroll to the top of the text.
	 * 
	 * @param documentId  ID of the document to display.
	 * @param text        Text of the document to display.
	 */
	void setContents(int documentId, String text) {
		this.textWindow.setText(text);
		this.documentId = documentId;
		paintStatements();
		textWindow.setCaretPosition(0);
	}

	/**
	 * Highlight statements and regex in the text by adding color.
	 */
	void paintStatements() {
		if (documentId > -1) {
			//remove all initial foreground color styles
			int initialStart = 0;
			int initialEnd = textWindow.getText().length();
			Style blackStyle = sc.addStyle("ConstantWidth", null);
			StyleConstants.setForeground(blackStyle, Color.black);
			StyleConstants.setBackground(blackStyle, Color.white);
			doc.setCharacterAttributes(initialStart, initialEnd - initialStart, blackStyle, false);

			// color statements
			ArrayList<Statement> statements = Dna.sql.getShallowStatements(documentId);
			int i, start;
			for (i = 0; i < statements.size(); i++) {
				start = statements.get(i).getStart();
				Style bgStyle = sc.addStyle("ConstantWidth", null);
				if (Dna.sql.getActiveCoder() != null &&
						(statements.get(i).getCoderId() == Dna.sql.getActiveCoder().getId() || Dna.sql.getActiveCoder().isPermissionViewOthersStatements()) &&
						(statements.get(i).getCoderId() == Dna.sql.getActiveCoder().getId() || Dna.sql.getActiveCoder().getCoderRelations().get(statements.get(i).getCoderId()).isViewStatements())) {
					if (Dna.sql.getActiveCoder().isColorByCoder() == true) {
						StyleConstants.setBackground(bgStyle, statements.get(i).getCoderColor());
					} else {
						StyleConstants.setBackground(bgStyle, statements.get(i).getStatementTypeColor());
					}
				}
				doc.setCharacterAttributes(start, statements.get(i).getStop() - start, bgStyle, false);
			}
			
			// color regex
			ArrayList<Regex> regex = Dna.sql.getRegexes();
			for (i = 0; i < regex.size(); i++) {
				String label = regex.get(i).getLabel();
				Color color = regex.get(i).getColor();
				Pattern p = Pattern.compile(label, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(textWindow.getText());
				while(m.find()) {
					start = m.start();
					Style fgStyle = sc.addStyle("ConstantWidth", null);
					StyleConstants.setForeground(fgStyle, color);
					doc.setCharacterAttributes(start, m.end() - start, fgStyle, false);
				}
			}
		}
	}
	
	/**
	 * Repaint statements and update font size after a new coder has been
	 * selected.
	 */
	void adjustToChangedCoder() {
		if (Dna.sql.getConnectionProfile() == null) {
		    Font font = new Font("Monospaced", Font.PLAIN, 14);
	        textWindow.setFont(font);
		} else {
			try {
				Font font = new Font("Monospaced", Font.PLAIN, Dna.sql.getActiveCoder().getFontSize());
		        textWindow.setFont(font);
		        paintStatements();
			} catch (NullPointerException e) {
				LogEvent l = new LogEvent(Logger.ERROR,
						"Statements could not be painted in text.",
						"The statements could not be painted in the current document. This could be because you attempted to open a database that was created with DNA 2.0. Please create a new DNA 3 database and import documents from the old file if that is the case. Make sure you close the current database as soon as possible to avoid damage.",
						e);
				Dna.logger.log(l);
			}
		}
	}
}