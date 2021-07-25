package guiCoder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class TextPanel extends JPanel {
	private static final long serialVersionUID = -8094978928012991210L;
	JTextPane textWindow;
	JScrollPane textScrollPane;
	private DefaultStyledDocument doc;
	StyleContext sc;
	//JPopupMenu popmen;
	//JMenuItem menu1;
	//int documentId;
	
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
		this.add(textScrollPane);
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
	
}
