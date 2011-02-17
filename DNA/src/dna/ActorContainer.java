package dna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class ActorContainer implements TableModel {

	private Vector<TableModelListener> listeners = new Vector<TableModelListener>();
	ArrayList<Actor> actorList = new ArrayList<Actor>();
	
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}
	
	public void addActor(Actor actor) {
		actorList.add(actor);
		Collections.sort(actorList);
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public Actor getActor(String actor) {
		for (int i = actorList.size() - 1; i >= 0 ; i--) {
			if (actorList.get(i).getName().equals(actor)) {
				return actorList.get(i);
			}
		}
		return null;
	}
	
	public void removeActor(String actor) {
		for (int i = actorList.size() - 1; i >= 0 ; i--) {
			if (actorList.get(i).getName().equals(actor)) {
				actorList.remove(i);
				break;
			}
		}
		//notify all listeners
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return String.class; //name of the actor as featured in the coding
			case 1: return RegexTerm.class; //the type of actor (as a RegexTerm)
			case 2: return String.class; //alias or description
			case 3: return String.class; //note field
			default: return null;
		}
	}
	
	public int getColumnCount() {
		return 4;
	}
	
	public String getColumnName(int column) {
		switch( column ){
			case 0: return "Actor";
			case 1: return "Type";
			case 2: return "Alias/description";
			case 3: return "Note";
			default: return null;
		}
	}
	
	public int getRowCount() {
		int count = actorList.size();
		return count;
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		Actor item = actorList.get(rowIndex);
		switch( columnIndex ){
			case 0:	return item.getName();
			case 1: return item.getType();
			case 2: return item.getAlias();
			case 3: return item.getNote();
			default: return null;
		}
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Actor item;
		try{
			item = actorList.get(rowIndex);
		} catch (IndexOutOfBoundsException a) {
			item = actorList.get(rowIndex - 1);
		}
		switch( columnIndex ){
			case 0: 
				item.setName( (String) aValue );
				break;
			case 1: 
				if (aValue != null) {
					item.setType(((RegexTerm)aValue).getPattern());
				}
				break;
			case 2:
				item.setAlias((String) aValue);
				break;
			case 3:
				item.setNote((String) aValue);
				break;
		}
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
}