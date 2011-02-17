package dna;

import java.awt.Component;
import java.util.Calendar;
import java.util.EventObject;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

class DnaTableDateEditor extends JSpinner implements TableCellEditor{
	private SpinnerDateModel model;
	
	private List<CellEditorListener> listeners = new ArrayList<CellEditorListener>();
	
	//default constructor
	public DnaTableDateEditor() {
		model = new SpinnerDateModel();
		model.setCalendarField( Calendar.DAY_OF_YEAR );
		setModel( model );
		setEditor(new JSpinner.DateEditor(this, "dd.MM.yyyy"));
	}
	
	//perhaps someone wants to be notified
	public void addCellEditorListener(CellEditorListener l) {
		listeners.add( l );
	}
	
	//remove a CellEditorListener
	public void removeCellEditorListener(CellEditorListener l) {
		listeners.remove( l );
	}
	
	//return current value of the editor
	public Object getCellEditorValue() {
		return model.getDate();
	}
	
	//returns a component which is displayed in the JTable and which the user can interact with
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		model.setValue( value );
		return this;
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
}