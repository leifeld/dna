package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.jdesktop.swingx.JXCollapsiblePane;

@SuppressWarnings("serial")
class TextPanel extends JPanel {
	
	private JTextPane textWindow;
	JScrollPane textScrollPane;
	private DefaultStyledDocument doc;
	StyleContext sc;
	JPopupMenu popmen;
	JMenuItem menu1;
	int documentId;
		
	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	public void setDocumentText(String text) {
		textWindow.setText(text);
	}

	public String getDocumentText() {
		return(textWindow.getText());
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
	
	public TextPanel() {
		documentId = -1;
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

	    // Create and add the constant width style
	    final Style cwStyle = sc.addStyle("ConstantWidth", null);
	    StyleConstants.setBackground(cwStyle, Color.green);
		
		textWindow.setEditable(false);

		textScrollPane = new JScrollPane(textWindow);
		textScrollPane.setPreferredSize(new Dimension(700, 500));
		textScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//textWindow.setEnabled(false);
		
		JXCollapsiblePane collapsiblePane = new JXCollapsiblePane(); 
		collapsiblePane.setName("Central Text Panel");
		this.add(textScrollPane, BorderLayout.CENTER);
		this.add(collapsiblePane, BorderLayout.SOUTH);
		
		//MouseListener for text window; one method for Windows and one for Unix
		textWindow.addMouseListener( new MouseAdapter() {
			public void mouseReleased( MouseEvent me ) {
				try {
					mouseListenPopup(me);
				} catch (ArrayIndexOutOfBoundsException ex) {
					//no documents available
				}
			}
			public void mousePressed( MouseEvent me ) {
				try {
					mouseListenPopup(me);
				} catch (ArrayIndexOutOfBoundsException ex) {
					//no documents available
				}
			}
			
			public void mouseClicked( MouseEvent me ) {
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
			doc.setCharacterAttributes(initialStart, 
				 initialEnd - initialStart, blackStyle, false);

			//color statements
			ArrayList<SidebarStatement> statements = 
					Dna.dna.db.getStatementsPerDocumentId(documentId);
			for (int i = 0; i < statements.size(); i++) {
				int start = statements.get(i).getStart();
				int stop = statements.get(i).getStop();
				Color color = statements.get(i).getColor();
				Style bgStyle = sc.addStyle("ConstantWidth", null);
				StyleConstants.setBackground(bgStyle, color);
				doc.setCharacterAttributes(start, stop - start, 
						bgStyle, false);
			}
			
			//color regular expressions
			ArrayList<Regex> regex = Dna.dna.db.getRegex();
			for (int i = 0; i < regex.size(); i++) {
				String label = regex.get(i).getLabel();
				Color color = regex.get(i).getColor();
				Pattern p = Pattern.compile(label, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(getDocumentText());
				while(m.find()) {
					//LB.Change: removed + 1 in start and end
					int start = m.start();
					int end = m.end();
					Style fgStyle = sc.addStyle("ConstantWidth", null);
					StyleConstants.setForeground(fgStyle, color);
					doc.setCharacterAttributes(start, end-start, 
							fgStyle, false);
				}
			}
		}
	}
	
	public void mouseListenPopup(MouseEvent me) throws 
			ArrayIndexOutOfBoundsException {
		if ( me.isPopupTrigger() ) {
			if (!(textWindow.getSelectedText() == null)) {
				popupMenu(me.getComponent(), me.getX(), me.getY());
			}
		}
	}
	
	public void popupMenu(Component comp, int x, int y) {
		popmen = new JPopupMenu();
		ArrayList<StatementType> st = Dna.dna.db.getStatementTypes();
		for (int i = 0; i < st.size(); i++) {
			String type = st.get(i).getLabel();
			Color col = st.get(i).getColor();
                    menu1 = new JMenuItem( "Format as " + type);
                    menu1.setOpaque(true);
                    menu1.setBackground(col);
			popmen.add( menu1 );
			
			menu1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					Object item = e.getSource();
					String itemText = ((JMenuItem) item).getText();
					String type = itemText.substring(10);
					
					
					int selectionStart = textWindow.getSelectionStart();
					int selectionEnd = textWindow.getSelectionEnd();
                                        String selectedText = textWindow.getText().substring(selectionStart, selectionEnd);
                                        CreateStatementFrame createPanel = new CreateStatementFrame(type, col,documentId, selectionStart, selectionEnd,selectedText);
                                        createPanel.setVisible(true);
                                        
                                        textWindow.setSelectionStart(selectionStart);
                                        textWindow.setSelectionEnd(selectionEnd);
                                                
//					Dna.dna.addStatement(type, documentId, selectionStart, selectionEnd);
					paintStatements();
//					textWindow.setCaretPosition(selectionEnd);
				}
			});
		}
		
		popmen.show( comp, x, y );
	}
	
	public void mouseListenSelect(MouseEvent me) throws 
			ArrayIndexOutOfBoundsException {
		if ( me.isPopupTrigger() ) {
			if (!(textWindow.getSelectedText() == null)) {
				popupMenu(me.getComponent(), me.getX(), me.getY());
			}
		} else {
			if (Dna.dna.db.getFileName() != null) {
				int pos = textWindow.getCaretPosition(); //click caret position
				SidebarStatement s = 
						Dna.dna.db.getStatementAtLocation(documentId, pos);
				Point p = me.getPoint();
				if (s != null) {
					int statementId = s.getStatementId();
					int startIndex = s.getStart();
					int stopIndex = s.getStop();
					Point location = textWindow.getLocationOnScreen();
					textWindow.setSelectionStart(startIndex);
					textWindow.setSelectionEnd(stopIndex);
					new Popup(p, statementId, location);
				}
			}
		}
	}
	
	/**
	 * Set text in the editor pane, select statement, and open popup window
	 * 
	 * @param statementId
	 * @param documentId
	 */
	public void selectStatement(final int statementId, int documentId) {
		textWindow.setText(Dna.dna.db.getDocument(documentId).getText());
		paintStatements();
		
		int start = Dna.dna.db.getStatement(statementId).getStart();
		int stop = Dna.dna.db.getStatement(statementId).getStop();
		textWindow.grabFocus();
		textWindow.select(start, stop);
		
		// the selection is too slow, so wait for it to finish...
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int start = Dna.dna.db.getStatement(statementId).getStart();
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
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				
				Rectangle mtv = null;
				try {
					mtv = textWindow.modelToView(start);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				Point p = mtv.getLocation();
				Point loc = textWindow.getLocationOnScreen();
				new Popup(p, statementId, loc);
			}
		});
	}
}