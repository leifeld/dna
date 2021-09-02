package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import gui.DocumentTablePanel.DocumentPanelListener;
import gui.Popup;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import model.Statement;
import model.StatementType;

/**
 * Text panel, which displays the text of the selected document and stores a
 * list of the statements contained in the document. It paints the statements in
 * the text and can initiate a context menu for inserting statements or popup
 * dialog window to display the contents of a statement.
 */
class TextPanel extends JPanel implements CoderListener, DocumentPanelListener {
	private static final long serialVersionUID = -8094978928012991210L;
	private ArrayList<TextPanelListener> listeners = new ArrayList<TextPanelListener>();
	private JTextPane textWindow;
	private JScrollPane textScrollPane;
	private DefaultStyledDocument doc;
	private StyleContext sc;
	private JPopupMenu popmen;
	private int documentId;
	private ArrayList<Statement> statements;
	private Coder coder;
	private int verticalScrollLocation;
	private DocumentTableModel documentTableModel;
	
	/**
	 * Create a new text panel.
	 * 
	 * @param documentTableModel A reference to the table model in which the
	 *   documents are stored.
	 */
	TextPanel(DocumentTableModel documentTableModel) {
		this.documentTableModel = documentTableModel;
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

		//MouseListener for text window; one method for Windows and one for Unix
		textWindow.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				try {
					mouseListenPopup(me);
				} catch (ArrayIndexOutOfBoundsException ex) {
					//no documents available
				}
			}
			public void mousePressed(MouseEvent me) {
				try {
					mouseListenPopup(me);
				} catch (ArrayIndexOutOfBoundsException ex) {
					//no documents available
				}
			}

			public void mouseClicked(MouseEvent me) {
				try {
					mouseListenSelect(me);
				} catch (ArrayIndexOutOfBoundsException ex) {
					//no documents available
				}
			}
		});
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
			statements = Dna.sql.getStatements(documentId);
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
	private void setContents(int documentId, String text) {
		this.textWindow.setText(text);
		this.documentId = documentId;
		paintStatements();
		textWindow.setCaretPosition(0);
		fireDocumentIdChange();
	}
	
	/**
	 * Add a new statement and display a popup dialog window with the new
	 *   statement.
	 * 
	 * @param me  A mouse event.
	 * @throws ArrayIndexOutOfBoundsException
	 */
	private void mouseListenPopup(MouseEvent me) throws ArrayIndexOutOfBoundsException {
		if (me.isPopupTrigger()) {
			if (!(textWindow.getSelectedText() == null) && coder != null && coder.getPermissionAddStatements() == 1) {
				popupMenu(me.getComponent(), me.getX(), me.getY());
			}
		}
	}

	/**
	 * Check the current position in the text for statements and display a
	 * statement popup window if there is a statement.
	 * 
	 * @param me  The mouse event that triggers the popup window, including the
	 *   location.
	 * @throws ArrayIndexOutOfBoundsException
	 */
	private void mouseListenSelect(MouseEvent me) throws ArrayIndexOutOfBoundsException {
		if (me.isPopupTrigger()) {
			if (!(textWindow.getSelectedText() == null)) {
				popupMenu(me.getComponent(), me.getX(), me.getY());
			}
		} else {
			int pos = textWindow.getCaretPosition(); //click caret position
			Point p = me.getPoint();
			
			if (statements != null && statements.size() > 0) {
				for (int i = 0; i < statements.size(); i++) {
					if (statements.get(i).getStart() < pos
							&& statements.get(i).getStop() > pos
							&& coder != null
							&& (coder.getPermissionViewOthersStatements() == 1 || statements.get(i).getCoderId() == coder.getId())
							// TODO here: check also the CODERRELATIONS table
							) {
						fireStatementSelected(statements.get(i).getId());
						Point location = textWindow.getLocationOnScreen();
						textWindow.setSelectionStart(statements.get(i).getStart());
						textWindow.setSelectionEnd(statements.get(i).getStop());
						/*
							int row = Dna.gui.rightPanel.statementPanel.ssc.getIndexByStatementId(statementId);
							if (row > -1) {
								Dna.gui.rightPanel.statementPanel.statementTable.setRowSelectionInterval(row, row);
								Dna.gui.rightPanel.statementPanel.statementTable.scrollRectToVisible(new Rectangle(  // scroll to selected row
										Dna.gui.rightPanel.statementPanel.statementTable.getCellRect(i, 0, true)));
							}

							int docModelIndex = Dna.gui.documentPanel.documentContainer.getModelIndexById(Dna.data.getStatements().get(i).getDocumentId());
							int docRow = Dna.gui.documentPanel.documentTable.convertRowIndexToView(docModelIndex);
							//int docRow = Dna.dna.gui.documentPanel.documentContainer.getRowIndexById(Dna.data.getStatements().get(i).getDocumentId());
							Dna.gui.documentPanel.documentTable.scrollRectToVisible(new Rectangle(Dna.gui.documentPanel.documentTable.getCellRect(docRow, 0, true)));
							if (b[1] == true) {  // statement is editable by the active coder
								new Popup(p.getX(), p.getY(), statementId, location, true);
							} else {
								new Popup(p.getX(), p.getY(), statementId, location, false);
							}
						 */
						new Popup(p.getX(), p.getY(), statements.get(i), documentId, location, coder);
						break;
						//}
					}
				}
			}
		}
	}
	
	/*
	private String getDocumentText() {
		return(textWindow.getText());
	}
*/
	/*
	private void setDocumentText(String text) {
		textWindow.setText(text);
	}

	private void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	private int getDocumentId() {
		return this.documentId;
	}
	
	private void highlightText(int startCaret, int stopCaret) {
		textWindow.grabFocus();
		textWindow.select(startCaret, stopCaret);
	}
	
	private int getCaretPosition() {
		return(textWindow.getCaretPosition());
	}
	
	private int getSelectionStart() {
		return(textWindow.getSelectionStart());
	}

	private int getSelectionEnd() {
		return(textWindow.getSelectionEnd());
	}
	*/
	
	/**
	 * Set the caret position of the viewport (e.g., scroll to the beginning).
	 * 
	 * @param position  Caret position.
	 */
	/*
	private void setCaretPosition(int position) {
		textWindow.setCaretPosition(position);
	}
	*/
	
	/*
	public Point getLocationOnScreen() {
		return(textWindow.getLocationOnScreen());
	}
	*/
	
	/**
	 * Show a text popup menu upon right mouse click to insert statements.
	 * 
	 * @param comp  The AWT component on which to draw the dialog window.
	 * @param x     The horizontal coordinate where the dialog should be shown.
	 * @param y     The vertical coordinate where the dialog should be shown.
	 */
	private void popupMenu(Component comp, int x, int y) {
		popmen = new JPopupMenu();
		ArrayList<StatementType> statementTypes = Dna.sql.getStatementTypes();
		for (int i = 0; i < statementTypes.size(); i++) {
			StatementType statementType = statementTypes.get(i);
			JMenuItem menuItem = new JMenuItem("Format as " + statementType.getLabel());
			menuItem.setOpaque(true);
			menuItem.setBackground(statementType.getColor());
			popmen.add(menuItem);
			
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int selectionStart = textWindow.getSelectionStart();
					int selectionEnd = textWindow.getSelectionEnd();
					Statement statement = new Statement(selectionStart,
							selectionEnd,
							statementType.getId(),
							coder.getId(),
							statementType.getVariables());
					Dna.sql.addStatement(statement, documentId);
					documentTableModel.increaseFrequency(documentId);
					paintStatements();
					textWindow.setCaretPosition(selectionEnd);
					fireStatementAdded(statement.getId());
				}
			});
		}
		popmen.show(comp, x, y);
	}
	
	/**
	 * Set text in the editor pane, select statement, and open popup window
	 * 
	 * @param statementId
	 * @param documentId
	 */
	void selectStatement(final int statementId, int documentId, boolean editable) {
		Statement s = Dna.sql.getStatement(statementId);
		
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
					new Popup(mtv.getX(), mtv.getY(), s, documentId, loc, coder);
				} catch (BadLocationException e) {
					LogEvent l = new LogEvent(Logger.WARNING,
							"[GUI] Statement " + statementId + ": Popup window bad location exception.",
							"Statement " + statementId + ": Popup window cannot be opened because the location is outside the document text.");
					Dna.logger.log(l);
				}
			}
		});
	}

	/**
	 * Text panel listener interface to notify other GUI elements that a new
	 * text document has been set, permitting them to react accordingly.
	 */
	public interface TextPanelListener {
		void adjustToSelectedDocument(int documentId);
		void statementAdded(int statementId);
		void statementDeleted(int statementId);
		void statementSelected(int statementId);
	}
	
	/**
	 * Add a text panel listener.
	 * 
	 * @param listener  An object whose class implements {@link
	 *   TextPanelListener}.
	 */
	public void addTextPanelListener(TextPanelListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Notify the listeners that the document ID of the text panel has changed.
	 */
	public void fireDocumentIdChange() {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).adjustToSelectedDocument(this.documentId);
		}
	}
	
	/**
	 * Notify the listeners that a statement has been added.
	 * 
	 * @param statementId  The ID of the statement that has been added.
	 */
	public void fireStatementAdded(int statementId) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).statementAdded(statementId);
		}
	}

	/**
	 * Notify the listeners that a statement has been deleted.
	 * 
	 * @param statementId  The ID of the statement that has been deleted.
	 */
	public void fireStatementDeleted(int statementId) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).statementDeleted(statementId);
		}
	}

	/**
	 * Notify the listeners that a statement has been selected from within the
	 * text panel.
	 * 
	 * @param statementId  The ID of the statement that has been selected.
	 */
	public void fireStatementSelected(int statementId) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).statementSelected(statementId);
		}
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

	@Override
	public void documentTableSingleSelection(int documentId, String documentText) {
		setContents(documentId, documentText);
	}

	@Override
	public void documentTableMultipleSelection(int[] documentId) {
		setContents(-1, "");
	}

	@Override
	public void documentTableNoSelection() {
		setContents(-1, "");
	}

	@Override
	public void documentRefreshStarted() {
		verticalScrollLocation = (int) textScrollPane.getViewport().getViewPosition().getY(); // get the scroll position to restore it later
	}

	@Override
	public void documentRefreshEnded() {
		// nothing to do because the viewport position is already refreshed after each chunk is completed
	}

	@Override
	public void documentRefreshChunkComplete() { // document ID has already been selected in the document table refresh worker
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textScrollPane.getViewport().setViewPosition(new Point(0, verticalScrollLocation)); // scroll to previously saved position
			}
		});
	}
}