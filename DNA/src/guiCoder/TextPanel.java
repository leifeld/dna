package guiCoder;

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

import dna.Coder;
import dna.Dna;
import dna.Dna.CoderListener;
import dna.Statement;
import dna.StatementType;
import logger.LogEvent;
import logger.Logger;

public class TextPanel extends JPanel implements CoderListener {
	private static final long serialVersionUID = -8094978928012991210L;
	private JTextPane textWindow;
	private JScrollPane textScrollPane;
	private DefaultStyledDocument doc;
	StyleContext sc;
	JPopupMenu popmen;
	int documentId;
	ArrayList<Statement> statements;
	Coder coder;
	
	public TextPanel(int popupTextFieldWidth) {
		this.setLayout(new BorderLayout());
		Dna.addCoderListener(this);
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
	 * Set the vertical position to scroll to in the scroll pane of the text
	 * pane, for example for restoring the viewport location after reloading
	 * documents from the database.
	 * 
	 * @param y  Vertical point position of the viewport of the scroll pane.
	 * 
	 * @see {@link #getViewportPosition()}
	 * @see {@link guiCoder.DocumentPanel#getSelectedDocumentId()}
	 * @see {@link guiCoder.DocumentPanel#setUserLocation(int documentId, int y)}
	 * @see {@link guiCoder.DocumentPanel#getViewportPosition()}
	 * @see {@link guiCoder.DocumentPanel#setViewportPosition(int y)}
	 */
	void setViewportPosition(int y) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textScrollPane.getViewport().setViewPosition(new Point(0, y));
			}
		});
	}
	
	/**
	 * Return the vertical position that is currently being displayed in the
	 * scroll pane, for example for restoring it after reloading documents from
	 * the database.
	 * 
	 * @return  Vertical point position of the viewport of the scroll pane.
	 * 
	 * @see {@link #setViewportPosition(int y)}
	 * @see {@link guiCoder.DocumentPanel#getSelectedDocumentId()}
	 * @see {@link guiCoder.DocumentPanel#setUserLocation(int documentId, int y)}
	 * @see {@link guiCoder.DocumentPanel#getViewportPosition()}
	 * @see {@link guiCoder.DocumentPanel#setViewportPosition(int y)}
	 */
	int getViewportPosition() {
		return (int) textScrollPane.getViewport().getViewPosition().getY();
	}
	
	void paintStatements() {
		if (documentId > -1) {
			//remove all initial foreground color styles
			int initialStart = 0;
			int initialEnd = getDocumentText().length();
			Style blackStyle = sc.addStyle("ConstantWidth", null);
			StyleConstants.setForeground(blackStyle, Color.black);
			StyleConstants.setBackground(blackStyle, Color.white);
			doc.setCharacterAttributes(initialStart, initialEnd - initialStart, blackStyle, false);

			// color statements
			statements = Dna.sql.getStatementsByDocument(documentId);
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
	
	void setContents(int documentId, String text) {
		this.textWindow.setText(text);
		this.documentId = documentId;
		paintStatements();
	}
	
	/**
	 * Add a new statement.
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
							&& (coder.getPermissionViewOthersStatements() == 1 || statements.get(i).getCoder() == coder.getId())
							// TODO here: check also the CODERRELATIONS table
							) {
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
	
	private String getDocumentText() {
		return(textWindow.getText());
	}

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
	
	void setCaretPosition(int position) {
		textWindow.setCaretPosition(position);
	}
	
	public Point getLocationOnScreen() {
		return(textWindow.getLocationOnScreen());
	}
	
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
					Statement statement = new Statement(-1, coder.getId(), selectionStart, selectionEnd, statementType.getId(), statementType.getVariables());
					Dna.sql.addStatement(statement, documentId);
					Dna.guiCoder.documentTableModel.increaseFrequency(documentId);
					paintStatements();
					textWindow.setCaretPosition(selectionEnd);
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
	public void selectStatement(final int statementId, int documentId, boolean editable) {
		this.setContents(documentId, Dna.sql.getDocumentText(documentId));
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
}