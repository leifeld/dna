package dna;

import dna.dataStructures.*;
import dna.panels.BottomCardPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
public class TextPanel extends JPanel {
	
	public JTextPane textWindow;
	JScrollPane textScrollPane;
	private DefaultStyledDocument doc;
	StyleContext sc;
	JPopupMenu popmen;
	JMenuItem menu1;
	int documentId;
	
	public BottomCardPanel bottomCardPanel;
	public JXCollapsiblePane collapsiblePane;
	
	ArrayList<StatementType> statementTypes ;
	
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
	    
	    //Font font = new Font("Serif", Font.ITALIC, 20);
	    Font font = new Font("Monospaced", Font.PLAIN, 14);
        textWindow.setFont(font);
	    
	    // Create and add the constant width style
	    final Style cwStyle = sc.addStyle("ConstantWidth", null);
	    StyleConstants.setBackground(cwStyle, Color.green);
		
		textWindow.setEditable(false);

		textScrollPane = new JScrollPane(textWindow);
		textScrollPane.setPreferredSize(new Dimension(500, 500));
		textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		collapsiblePane = new JXCollapsiblePane(); 
		collapsiblePane.setName("Central Text Panel");
		this.add(textScrollPane, BorderLayout.CENTER);
		this.add(collapsiblePane, BorderLayout.SOUTH);
		collapsiblePane.setCollapsed(true);
		bottomCardPanel = new BottomCardPanel();
		collapsiblePane.add(bottomCardPanel, "Search and recode");
		
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
			doc.setCharacterAttributes(initialStart, initialEnd - initialStart, blackStyle, false);

			//color statements
			ArrayList<Statement> statements = new ArrayList<Statement>();
			ArrayList<Integer> codersVisible = new ArrayList<Integer>();
			int ac = Dna.data.getActiveCoder();
			codersVisible.add(ac);
			for (int i = 0; i < Dna.data.getCoderRelations().size(); i++) {
				if (Dna.data.getCoderRelations().get(i).getCoder() == ac && Dna.data.getCoderRelations().get(i).isViewStatements() == true) {
					codersVisible.add(Dna.data.getCoderRelations().get(i).getOtherCoder());  // which coders are visible?
				}
			}
			for (int i = 0; i < Dna.data.getStatements().size(); i++) {
				if (Dna.data.getStatements().get(i).getDocumentId() == documentId 
						&& codersVisible.contains(Dna.data.getStatements().get(i).getCoder())) {
					statements.add(Dna.data.getStatements().get(i));  // add to list of statements to be painted
				}
			}
			for (int i = 0; i < statements.size(); i++) {
				int start = statements.get(i).getStart();
				int stop = statements.get(i).getStop();
				Color color = Dna.data.getStatementColor(statements.get(i).getId());
				Style bgStyle = sc.addStyle("ConstantWidth", null);
				StyleConstants.setBackground(bgStyle, color);
				doc.setCharacterAttributes(start, stop - start, bgStyle, false);
			}
			
			//color regular expressions
			//ArrayList<Regex> regex = Dna.dna.db.getRegex();
			for (int i = 0; i < Dna.data.getRegexes().size(); i++) {
				String label = Dna.data.getRegexes().get(i).getLabel();
				Color color = Dna.data.getRegexes().get(i).getColor();
				Pattern p = Pattern.compile(label, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(getDocumentText());
				while(m.find()) {
					//LB.Change: removed + 1 in start and end
					int start = m.start();
					int end = m.end();
					Style fgStyle = sc.addStyle("ConstantWidth", null);
					StyleConstants.setForeground(fgStyle, color);
					doc.setCharacterAttributes(start, end-start, fgStyle, false);
				}
			}
		}
	}
	
	public void mouseListenPopup(MouseEvent me) throws ArrayIndexOutOfBoundsException {
		if ( me.isPopupTrigger() ) {
			if (!(textWindow.getSelectedText() == null) && Dna.data.getCoderById(Dna.data.getActiveCoder()).getPermissions().get("addStatements") == true) {
				popupMenu(me.getComponent(), me.getX(), me.getY());
			}
		}
	}
	
	public void popupMenu(Component comp, int x, int y) {
		popmen = new JPopupMenu();
		statementTypes = Dna.data.getStatementTypes();
		int docRow = Dna.dna.gui.documentPanel.documentTable.getSelectedRow();
		int documentIndex = Dna.dna.gui.documentPanel.documentTable.convertRowIndexToModel(docRow);
		//int documentIndex = Dna.dna.gui.documentPanel.documentTable.getSelectedRow();
		int documentId = Dna.data.getDocuments().get(documentIndex).getId();
		Date documentDate = Dna.data.getDocuments().get(documentIndex).getDate();
		
		for (int i = 0; i < statementTypes.size(); i++) {
			String type = statementTypes.get(i).getLabel();
			StatementType statementType = statementTypes.get(i);
			Color col = statementTypes.get(i).getColor();
			menu1 = new JMenuItem("Format as " + type);
			menu1.setOpaque(true);
			menu1.setBackground(col);
			popmen.add( menu1 );
			
			menu1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selectionStart = textWindow.getSelectionStart();
					int selectionEnd = textWindow.getSelectionEnd();
					int statementId = Dna.data.generateNewId("statements");
					int coderId = Dna.data.getActiveCoder();
					LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
					Iterator<String> keyIterator = statementType.getVariables().keySet().iterator();
			        while (keyIterator.hasNext()){
			    		String key = keyIterator.next();
			    		String value = statementType.getVariables().get(key);
			    		if (value.equals("boolean")) {
			    			map.put(key, 1);
			    		} else if (value.equals("integer")) {
			    			map.put(key, 0);
			    		} else {
			    			map.put(key, "");
			    		}
			    	}
					Statement statement = new Statement(statementId, documentId, selectionStart, selectionEnd, 
							documentDate, statementType.getId(), coderId, map);
					Dna.dna.addStatement(statement);
					Dna.dna.gui.documentPanel.documentTable.updateUI(); // for the "#" column
					
					paintStatements();
					textWindow.setCaretPosition(selectionEnd);
					Dna.dna.gui.textPanel.selectStatement(statementId, documentId, true);
				}
			});
		}
		popmen.show( comp, x, y );
	}
	
	public void mouseListenSelect(MouseEvent me) throws ArrayIndexOutOfBoundsException {
		if (me.isPopupTrigger()) {
			if (!(textWindow.getSelectedText() == null)) {
				popupMenu(me.getComponent(), me.getX(), me.getY());
			}
		} else {
			int pos = textWindow.getCaretPosition(); //click caret position
			Point p = me.getPoint();
			
			for (int i = 0; i < Dna.data.getStatements().size(); i++) {
				if (Dna.data.getStatements().get(i).getDocumentId() == documentId 
						&& Dna.data.getStatements().get(i).getStart() < pos 
						&& Dna.data.getStatements().get(i).getStop() > pos) {
					boolean[] b = Dna.data.getActiveStatementPermissions(Dna.data.getStatements().get(i).getId());
					if (b[0] == true) {  // statement is visible to the active coder
						int statementId = Dna.data.getStatements().get(i).getId();
						int startIndex = Dna.data.getStatements().get(i).getStart();
						int stopIndex = Dna.data.getStatements().get(i).getStop();
						Point location = textWindow.getLocationOnScreen();
						textWindow.setSelectionStart(startIndex);
						textWindow.setSelectionEnd(stopIndex);
						int row = Dna.dna.gui.rightPanel.statementPanel.ssc.getIndexByStatementId(statementId);
						if (row > -1) {
							Dna.dna.gui.rightPanel.statementPanel.statementTable.setRowSelectionInterval(row, row);
							Dna.dna.gui.rightPanel.statementPanel.statementTable.scrollRectToVisible(new Rectangle(  // scroll to selected row
									Dna.dna.gui.rightPanel.statementPanel.statementTable.getCellRect(i, 0, true)));
						}
						int docModelIndex = Dna.dna.gui.documentPanel.documentContainer.getModelIndexById(Dna.data.getStatements().get(i).getDocumentId());
						int docRow = Dna.dna.gui.documentPanel.documentTable.convertRowIndexToView(docModelIndex);
						//int docRow = Dna.dna.gui.documentPanel.documentContainer.getRowIndexById(Dna.data.getStatements().get(i).getDocumentId());
						Dna.dna.gui.documentPanel.documentTable.scrollRectToVisible(new Rectangle(Dna.dna.gui.documentPanel.documentTable.getCellRect(docRow, 0, true)));
						if (b[1] == true) {  // statement is editable by the active coder
							new Popup(p, statementId, location, true);
						} else {
							new Popup(p, statementId, location, false);
						}
						break;
					}
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
	public void selectStatement(final int statementId, int documentId, boolean editable) {
		textWindow.setText(Dna.data.getDocument(documentId).getText());
		paintStatements();
		
		int start = Dna.data.getStatement(statementId).getStart();
		int stop = Dna.data.getStatement(statementId).getStop();
		textWindow.grabFocus();
		textWindow.select(start, stop);
		
		// the selection is too slow, so wait for it to finish...
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int start = Dna.data.getStatement(statementId).getStart();
				Rectangle mtv = null;
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
					Point p = mtv.getLocation();
					Point loc = textWindow.getLocationOnScreen();
					new Popup(p, statementId, loc, editable);
				} catch (BadLocationException e) {
					System.err.println("Statement " + statementId + ": Popup window cannot be opened because the location is outside the document text.");
				}
			}
		});
	}
	
	/**
	 * @author Shraddha Highlight text in the editor pane, select statement
	 * @param statementId
	 */
	public void highlightSelectedStatement(final int statementId) {
		textWindow.setText(Dna.data.getDocument(documentId).getText());
		paintStatements();
		
		int start = Dna.data.getStatement(statementId).getStart();
		int stop = Dna.data.getStatement(statementId).getStart();
		textWindow.grabFocus();
		textWindow.select(start, stop);
	}
}