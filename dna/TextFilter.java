package dna;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * A document filter that does not allow to insert quotation marks or 
 * angle brackets because this would confuse the XML parser etc.
 */
public class TextFilter extends DocumentFilter {  
	
	public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
		fb.insertString(offset, regex(text), attr);
	}
	// no need to override remove(): inherited version allows all removals
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length,	String text, AttributeSet attr) throws BadLocationException {  
		fb.replace(offset, length, regex(text), attr);
	}
	
	public String regex(String text) {
		text = text.replaceAll("<|>|\"|\n$|\u0000|\u0001|\u0002|\u0003|\u0004|\u0005|\u0006|\u0007|\u0008|\u0009|\u000B|\u000C|\u000E|\u000F|\u0010|\u0011|\u0012|\u0013|\u0014|\u0015|\u0016|\u0017|\u0018|\u0019|\u001A|\u001B|\u001C|\u001D|\u001E|\u001F", "");
		return text;
	}
}