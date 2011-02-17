package dna;

import java.awt.Component;
import java.util.EventObject;
import java.util.List;
import java.util.ArrayList;

import javax.swing.text.AbstractDocument;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class DnaTableTitleEditor extends JTextField implements TableCellEditor {
	
	private List<CellEditorListener> listeners = new ArrayList<CellEditorListener>();
	private JTextField editor = new JTextField();
	
	public DnaTableTitleEditor() {
		editor = new JTextField();
		
	}
	
//	perhaps someone wants to be notified
	public void addCellEditorListener(CellEditorListener l) {
		listeners.add( l );
	}
	
	//remove a CellEditorListener
	public void removeCellEditorListener(CellEditorListener l) {
		listeners.remove( l );
	}
	
	//return current value of the editor
	public Object getCellEditorValue() {
		return ((JTextField)editor).getText();
	}
	
	//returns a component which is displayed in the JTable and which the user can interact with
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		DocumentFilter dfilter = new TextFilter();
		
		((AbstractDocument) ((JTextField) editor).getDocument()).setDocumentFilter(dfilter);

		((JTextField) editor).setText((String) value);
		
		return editor;
	}

	//Is the cell editable? The EventObject can be a MouseEvent, KeyEvent or anything else.
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}
	
	//returns whether the editor component must be selected to be used
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}
	
	//cancel editing the cell
	public void cancelCellEditing() {
		fireEditingCanceled();
	}
	
	//stop editing the cell if possible (this is always possible because the JSpinner always shows a value)
	public boolean stopCellEditing() {
		fireEditingStopped();
		return true;
	}
	
	//notify all listeners that editing has been canceled
	protected void fireEditingCanceled(){
		ChangeEvent e = new ChangeEvent( this );
		for( int i = 0, n = listeners.size(); i<n; i++ )
			((CellEditorListener)listeners.get( i )).editingCanceled( e );
	}
	
	//notify all listeners that editing has been finished
	protected void fireEditingStopped(){
		ChangeEvent e = new ChangeEvent( this );
		for( int i = 0, n = listeners.size(); i<n; i++ )
			((CellEditorListener)listeners.get( i )).editingStopped( e );
	}
	
	class TextFilter extends DocumentFilter {  
		
		public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {  
			fb.insertString(offset, text.replaceAll("<|>|\"", ""), attr);
		}
		// no need to override remove(): inherited version allows all removals  
		public void replace(DocumentFilter.FilterBypass fb, int offset, int length,	String text, AttributeSet attr) throws BadLocationException {  
			fb.replace(offset, length, text.replaceAll("<|>|\"", ""), attr);
		}
	}
	
}
