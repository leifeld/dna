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
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import dna.Dna;
import dna.Statement;
import dna.StatementType;

public class TextPanel extends JPanel {
	private static final long serialVersionUID = -8094978928012991210L;
	JTextPane textWindow;
	JScrollPane textScrollPane;
	private DefaultStyledDocument doc;
	StyleContext sc;
	JPopupMenu popmen;
	int documentId;
	ArrayList<Statement> statements;
	
	public TextPanel() {
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

	public void paintStatements() {
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
				StyleConstants.setBackground(bgStyle, statements.get(i).getStatementTypeColor());
				doc.setCharacterAttributes(start, statements.get(i).getStop() - start, bgStyle, false);
			}
		}
	}
	
	public void setContents(int documentId, String text) {
		this.textWindow.setText(text);
		this.documentId = documentId;
		paintStatements();
	}
	
	public void mouseListenPopup(MouseEvent me) throws ArrayIndexOutOfBoundsException {
		if (me.isPopupTrigger()) {
			if (!(textWindow.getSelectedText() == null)) { //  && Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions().get("addStatements") == true
				popupMenu(me.getComponent(), me.getX(), me.getY());
			}
		}
	}

	public void mouseListenSelect(MouseEvent me) throws ArrayIndexOutOfBoundsException {
		if (me.isPopupTrigger()) {
			if (!(textWindow.getSelectedText() == null)) {
				popupMenu(me.getComponent(), me.getX(), me.getY());
			}
		} else {
			int pos = textWindow.getCaretPosition(); //click caret position
			Point p = me.getPoint();
			
			if (statements.size() > 0) {
				for (int i = 0; i < statements.size(); i++) {
					if (statements.get(i).getStart() < pos && statements.get(i).getStop() > pos) {
						//boolean[] b = Dna.data.getActiveStatementPermissions(Dna.data.getStatements().get(i).getId());
						//if (b[0] == true) {  // statement is visible to the active coder
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
							new Popup(p.getX(), p.getY(), statements.get(i), location, true);
							break;
						//}
					}
				}
			}
		}
	}
	
	public void setDocumentText(String text) {
		textWindow.setText(text);
	}

	public String getDocumentText() {
		return(textWindow.getText());
	}

	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	public int getDocumentId() {
		return this.documentId;
	}
	
	public void highlightText(int startCaret, int stopCaret) {
		textWindow.grabFocus();
		textWindow.select(startCaret, stopCaret);
	}
	
	public int getCaretPosition() {
		return(textWindow.getCaretPosition());
	}

	public void setCaretPosition(int position) {
		textWindow.setCaretPosition(position);
	}
	
	public int getSelectionStart() {
		return(textWindow.getSelectionStart());
	}

	public int getSelectionEnd() {
		return(textWindow.getSelectionEnd());
	}
	
	public Point getLocationOnScreen() {
		return(textWindow.getLocationOnScreen());
	}
	
	public void popupMenu(Component comp, int x, int y) {
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
					Statement statement = new Statement(-1, Dna.sql.getConnectionProfile().getCoderId(), selectionStart, selectionEnd, statementType.getId(), statementType.getVariables());
					Dna.sql.addStatement(statement, documentId);
					Dna.guiCoder.documentTableModel.updateFrequency(documentId);
					paintStatements();
					textWindow.setCaretPosition(selectionEnd);
					//Dna.gui.textPanel.selectStatement(statementId, documentId, true);
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
	/*
	public void selectStatement(final int statementId, int documentId, boolean editable) {
		textWindow.setText(Dna.data.getDocument(documentId).getText());
		paintStatements();
		
		int start = Dna.data.getStatement(statementId).getStart();
		int stop = Dna.data.getStatement(statementId).getStop();
		textWindow.grabFocus();
		textWindow.select(start, stop);
		
		// the selection is too slow, so wait for it to finish...
		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("deprecation") // modelToView becomes modelToView2D in Java 9, but we still want Java 8 compliance 
			public void run() {
				int start = Dna.data.getStatement(statementId).getStart();
				Rectangle2D mtv = null;
				try {
					double y = textWindow.modelToView(start).getY();
					int l = textWindow.getText().length();
					double last = textWindow.modelToView(l).getY();
					double frac = y / last;
					double max = textScrollPane.getVerticalScrollBar().
							getMaximum();
					double h = textScrollPane.getHeight();
					int value = (int) Math.ceil(frac * max - (h / 2));
					textScrollPane.getVerticalScrollBar().setValue(value);
					mtv = textWindow.modelToView(start);
					Point loc = textWindow.getLocationOnScreen();
					new Popup(mtv.getX(), mtv.getY(), statementId, loc, editable);
				} catch (BadLocationException e) {
					System.err.println("Statement " + statementId + ": Popup window cannot be opened because the location is outside the document text.");
				}
			}
		});
	}
	*/
	
	/**
	 * @author Shraddha Highlight text in the editor pane, select statement
	 * @param statementId
	 */
	/*
	public void highlightSelectedStatement(final int statementId) {
		textWindow.setText(Dna.data.getDocument(documentId).getText());
		paintStatements();
		
		int start = Dna.data.getStatement(statementId).getStart();
		int stop = Dna.data.getStatement(statementId).getStart();
		textWindow.grabFocus();
		textWindow.select(start, stop);
	}
	*/
}
