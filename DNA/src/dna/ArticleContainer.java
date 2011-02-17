package dna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class ArticleContainer implements TableModel {
	private ArrayList<Article> articles = new ArrayList<Article> ();
	private Vector<TableModelListener> listeners = new Vector<TableModelListener>();
	
	public boolean containsTitle(String title) {
		boolean contains = false;
		for (int i = 0; i < articles.size(); i++) {
			if (articles.get(i).getTitle().equals(title)) {
				contains = true;
			}
		}
		return contains;
	}
	
	public void addArticle( Article article ){
		String title = article.getTitle();
		articles.add( article );
		sort();
		int index = getRowIndexByTitle(title);
		
		//notify all listeners
		TableModelEvent e = new TableModelEvent( this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public Article get(int index) {
		return articles.get(index);
	}
	
	//return number of columns
	public int getColumnCount() {
		return 2;
	}
	
	//return number of articles in the table
	public int getRowCount() {
		return articles.size();
	}
	
	public int getRowIndexByTitle(String title) throws NullPointerException {
		for (int i = 0; i < articles.size(); i++) {
			if (articles.get(i).getTitle().equals(title)) {
				return i;
			}
		}
		throw new NullPointerException();
	}
	
	public void remove(int index) {
		articles.remove(index);
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public void clear() {
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
			case 1: return "Date";
			default: return null;
		}
	}
	
	//get the value of a cell (rowIndex, columnIndex)
	public Object getValueAt(int rowIndex, int columnIndex) {
		Article article = articles.get(rowIndex);
		
		switch( columnIndex ){
			case 0: return article.getTitle();
			case 1: return article.getDate();
			default: return null;
		}
	}

	//which type of object (i.e. class) shall be shown in the columns?
	@SuppressWarnings( "unchecked" )
	public Class getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return String.class;
			case 1: return Date.class;
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
		return false;
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Article article = articles.get(rowIndex);
		
		switch( columnIndex ){
			case 0: 
				article.setTitle( (String)aValue );
				break;
			case 1: 
				article.setDate((Date)aValue);
				break;
		}
		
		TableModelEvent e = new TableModelEvent(this);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((TableModelListener)listeners.get( i )).tableChanged( e );
		}
	}
	
	public void sort() {
		Collections.sort(articles);
	}
}