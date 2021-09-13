package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import dna.Dna;
import dna.Dna.CoderListener;
import gui.Popup;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import model.Statement;

/**
 * Text panel, which displays the text of the selected document and stores a
 * list of the statements contained in the document. It paints the statements in
 * the text and can initiate a context menu for inserting statements or popup
 * dialog window to display the contents of a statement.
 */
class TextPanel extends JPanel implements CoderListener {
	private static final long serialVersionUID = -8094978928012991210L;
	private JTextPane textWindow;
	private JScrollPane textScrollPane;
	private DefaultStyledDocument doc;
	private StyleContext sc;
	private int documentId;
	private Coder coder;
	private StatementPanel statementPanel;
	
	/**
	 * Create a new text panel.
	 * 
	 * @param documentTableModel A reference to the table model in which the
	 *   documents are stored.
	 */
	TextPanel(StatementPanel statementPanel) {
		this.statementPanel = statementPanel;
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
	    StyleConstants.setFontFamily(mainStyle, "serif");
	    StyleConstants.setFontSize(mainStyle, 12);
	    
	    Font font = new Font("Monospaced", Font.PLAIN, 14);
        textWindow.setFont(font);
	    
		textWindow.setEditable(false);

		textScrollPane = new JScrollPane(textWindow);
		textScrollPane.setPreferredSize(new Dimension(500, 500));
		textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(textScrollPane);
	}
	
	/**
	 * Highlight statements in the text by adding background color.
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
			ArrayList<Statement> statements = Dna.sql.getStatements(documentId);
			int i, start;
			for (i = 0; i < statements.size(); i++) {
				start = statements.get(i).getStart();
				Style bgStyle = sc.addStyle("ConstantWidth", null);
				if (coder != null) {
					if (coder.getColorByCoder() == 1) {
						StyleConstants.setBackground(bgStyle, coder.getColor());
					} else {
						StyleConstants.setBackground(bgStyle, statements.get(i).getStatementTypeColor());
					}
				}
				doc.setCharacterAttributes(start, statements.get(i).getStop() - start, bgStyle, false);
			}
		}
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

	JTextPane getTextWindow() {
		return textWindow;
	}
	
	/**
	 * Set text in the editor pane, select statement, and open popup window
	 * 
	 * @param statementId
	 * @param documentId
	 */
	void selectStatement(Statement s, int documentId, boolean editable) {
		//Statement s = Dna.sql.getStatement(statementId);
		
		int start = s.getStart();
		int stop = s.getStop();
		textWindow.grabFocus();
		textWindow.select(start, stop);
		
		// the selection is too slow, so wait for it to finish...
		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("deprecation") // modelToView becomes modelToView2D in Java 9, but we still want Java 8 compliance 
			public void run() {
				Rectangle2D mtv = null;
				try {
					double y = textWindow.modelToView(start).getY();
					int l = textWindow.getText().length();
					double last = textWindow.modelToView(l).getY();
					double frac = y / last;
					double max = textScrollPane.getVerticalScrollBar().getMaximum();
					double h = textScrollPane.getHeight();
					int value = (int) Math.ceil(frac * max - (h / 2));
					textScrollPane.getVerticalScrollBar().setValue(value);
					mtv = textWindow.modelToView(start);
					Point loc = textWindow.getLocationOnScreen();
					new Popup(mtv.getX(), mtv.getY(), s, documentId, loc, coder, statementPanel);
				} catch (BadLocationException e) {
					LogEvent l = new LogEvent(Logger.WARNING,
							"[GUI] Statement " + s.getId() + ": Popup window bad location exception.",
							"Statement " + s.getId() + ": Popup window cannot be opened because the location is outside the document text.");
					Dna.logger.log(l);
				}
			}
		});
	}

	@Override
	public void adjustToChangedCoder() {
		if (Dna.sql == null) {
			this.coder = null;
		    Font font = new Font("Monospaced", Font.PLAIN, 14);
	        textWindow.setFont(font);
		} else {
			this.coder = Dna.sql.getCoder(Dna.sql.getConnectionProfile().getCoderId());
		    Font font = new Font("Monospaced", Font.PLAIN, this.coder.getFontSize());
	        textWindow.setFont(font);
	        paintStatements();
		}
	}

	int getVerticalScrollLocation() {
		return (int) textScrollPane.getViewport().getViewPosition().getY(); // get the scroll position to restore it later
	}

	public void setVerticalScrollLocation(int verticalScrollLocation) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textScrollPane.getViewport().setViewPosition(new Point(0, verticalScrollLocation)); // scroll to previously saved position
			}
		});
	}
}