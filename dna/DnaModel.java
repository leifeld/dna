package dna;

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class DnaModel implements TableModel{
	private ArrayList<DnaArticle> articles = new ArrayList<DnaArticle> ();
	private Vector<TableModelListener> listeners = new Vector<TableModelListener>();
	
	public void addArticle( DnaArticle article ){
		int index = articles.size();
		articles.add( article );
		
		//notify all listeners
		TableModelEvent e = new TableModelEvent( this, index, index, //create event 'new row at index'
				TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
		for( int i = 0, n = listeners.size(); i < n; i++ ){ //send the event
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	//return number of columns
	public int getColumnCount() {
		return 3;
	}
	
	//return number of articles in the table
	public int getRowCount() {
		return articles.size();
	}
	
	public void removeArticle(int index) {
		articles.remove(index);
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public void removeAllArticles() {
		articles.clear();
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	//return the name of a column
	public String getColumnName(int column) {
		switch( column ){
			case 0: return "Title";
			case 1: return "Date (dd.mm.yyyy)";
			case 2: return "Preview";
			default: return null;
		}
	}
	
	//get the value of a cell (rowIndex, columnIndex)
	public Object getValueAt(int rowIndex, int columnIndex) {
		DnaArticle article = (DnaArticle)articles.get( rowIndex );
		
		switch( columnIndex ){
			case 0: return article.getTitle();
			case 1: return article.getDate();
			case 2: return article.getText();
			default: return null;
		}
	}

	//which type of object (i.e. class) shall be shown in the columns?
	@SuppressWarnings( "unchecked" )
	public Class getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return String.class;
			case 1: return Date.class;
			case 2: return String.class;
			default: return null;
		}	
	}
	
	public void addTableModelListener(TableModelListener l) {
		listeners.add( l );
	}
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove( l );
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 2) {
			return false; //text preview should not be visible
		} else {
			return true;
		}
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		DnaArticle article = (DnaArticle)articles.get( rowIndex );
		
		switch( columnIndex ){
			case 0: 
				article.setTitle( (String)aValue );
				break;
			case 1: 
				article.setDate((Date)aValue);
				break;
			case 2: 
				article.setText((String)aValue);
				break;
		}
	}
	
}