package dna;

import java.awt.event.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class SearchWindow extends JPanel {
	
	JTable resultTable;
	SearchTableModel tableModel;
	JScrollPane resultScroller;
	JTextField textField;
	JButton searchButton, searchRevert;
	KeyAdapter enter;
	
	public SearchWindow() {
		
		ImageIcon searchIcon = new ImageIcon(getClass().getResource("/icons/find.png"));
		
		this.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		textField = new JTextField(30);
		searchButton = new JButton(searchIcon);
		enter = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					startThread();
				}
			}
		};
		textField.addKeyListener (enter);
		Icon searchRevertIcon = new ImageIcon(getClass().getResource("/icons/arrow_rotate_clockwise.png"));
		searchRevert = new JButton(searchRevertIcon);
		searchRevert.setToolTipText("reset search results and text field");
		searchButton.setToolTipText("<html>do a full-text search and find all occurrences of <br> the regular expression given in the text field</html>");
		searchRevert.setPreferredSize(new Dimension(18, 18));
		searchButton.setPreferredSize(new Dimension(18, 18));
		searchRevert.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableModel.clear();
				textField.setText("");
			}
		});
		searchButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startThread();
			}
		});
		textField.setToolTipText("<html>More information: <a href=\"http://www.regular-expressions.info\">http://www.regular-expressions.info</a></html>");
		buttonPanel.add(searchRevert);
		buttonPanel.add(searchButton);
		buttonPanel.add(textField);
		
		tableModel = new SearchTableModel();
		resultTable = new JTable( tableModel );
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultScroller = new JScrollPane(resultTable);
		resultScroller.setPreferredSize(new Dimension(500, 165));
		resultTable.getColumnModel().getColumn( 0 ).setPreferredWidth( 80 );
		resultTable.getColumnModel().getColumn( 1 ).setPreferredWidth( 200 );
		resultTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 40 );
		resultTable.getColumnModel().getColumn( 3 ).setPreferredWidth( 40 );
		resultTable.getColumnModel().getColumn( 4 ).setPreferredWidth( 300 );
		resultTable.getTableHeader().setReorderingAllowed( false );
		
		resultTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;
				int selectedRow = resultTable.getSelectedRow();
				if (selectedRow == -1) {
					
				} else {
					int acRow = tableModel.get(selectedRow).getAcRow();
					Dna.mainProgram.articleTable.changeSelection(acRow, 0, false, false);
					Dna.mainProgram.textWindow.grabFocus();
					Dna.mainProgram.textWindow.setSelectionStart(tableModel.get(selectedRow).getStartCoordinate());
					Dna.mainProgram.textWindow.setSelectionEnd(tableModel.get(selectedRow).getEndCoordinate());
				}
			}
		});
		
		this.add(buttonPanel, BorderLayout.NORTH);
		this.add(resultScroller, BorderLayout.CENTER);
		
		textField.grabFocus();
	}
	
	public void startThread() {
		try {
			Thread generateThread = new Thread( new ReportGenerator(), "Generate search results" );
			generateThread.start();
		} catch (Exception ex) {
			System.err.println("There was a problem during full-text search: " + ex.getStackTrace());
			JOptionPane.showMessageDialog(SearchWindow.this, "There was a problem during full-text search: " + ex.getStackTrace());
		}
	}
	
	class ReportGenerator implements Runnable {
		
		ProgressMonitor progressMonitor;
		
		public void run() {
			
			textField.removeKeyListener(enter);
			searchButton.setEnabled(false);
			searchRevert.setEnabled(false);
			
			progressMonitor = new ProgressMonitor(SearchWindow.this, "Searching...", "", 0, Dna.mainProgram.dc.ac.getRowCount() - 1 );
			progressMonitor.setMillisToDecideToPopup(1);
			
			tableModel.clear();
			
			String searchTerm = textField.getText();
			
			for (int i = 0; i < Dna.mainProgram.dc.ac.getRowCount(); i++) {
				progressMonitor.setProgress(i);
				if (progressMonitor.isCanceled()) {
					break;
				}
				
				String text = Dna.mainProgram.dc.ac.get(i).getText();
				text = Dna.mainProgram.stripHtmlTags(text, false);
				Pattern p = Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(text);
				while (m.find()) {
					Date d = Dna.mainProgram.dc.ac.get(i).getDate();
					GregorianCalendar date = new GregorianCalendar();
					date.setTime(d);
					int start;
					if (m.start() > 15) {
						start = m.start() - 15;
					} else {
						start = 1;
					}
					int end;
					if (m.end() < text.length() - 20) {
						end = m.end() + 20;
					} else {
						end = text.length();
					}
					String subtext = "..." + text.substring(start, end) + "...";
					SearchResult sr = new SearchResult(i, Dna.mainProgram.dc.ac.get(i).getTitle(), date, m.start() + 1, m.end() + 1, subtext);
					tableModel.addSearchResult(sr);
				}
				
			}

			searchButton.setEnabled(true);
			searchRevert.setEnabled(true);
			textField.addKeyListener(enter);
		}
	}
	
	class SearchResult {
		
		int startCoordinate;
		int endCoordinate;
		int acRow;
		String articleTitle;
		GregorianCalendar date;
		String displayText;
		
		public SearchResult(int acRow, String articleTitle, GregorianCalendar date, int startCoordinate, int endCoordinate, String displayText) {
			this.startCoordinate = startCoordinate;
			this.endCoordinate = endCoordinate;
			this.articleTitle = articleTitle;
			this.date = date;
			this.displayText = displayText;
			this.acRow = acRow;
		}

		public int getAcRow() {
			return acRow;
		}

		public void setAcRow(int acRow) {
			this.acRow = acRow;
		}

		public int getStartCoordinate() {
			return startCoordinate;
		}

		public void setStartCoordinate(int startCoordinate) {
			this.startCoordinate = startCoordinate;
		}

		public int getEndCoordinate() {
			return endCoordinate;
		}

		public void setEndCoordinate(int endCoordinate) {
			this.endCoordinate = endCoordinate;
		}

		public String getArticleTitle() {
			return articleTitle;
		}

		public void setArticleTitle(String articleTitle) {
			this.articleTitle = articleTitle;
		}

		public GregorianCalendar getDate() {
			return date;
		}

		public void setDate(GregorianCalendar date) {
			this.date = date;
		}

		public String getDisplayText() {
			return displayText;
		}

		public void setDisplayText(String displayText) {
			this.displayText = displayText;
		}
	}
	
	class SearchTableModel implements TableModel {
		
		private ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
		private Vector<TableModelListener> listeners = new Vector<TableModelListener>();
		
		public void addSearchResult(SearchResult sr) {
			searchResults.add(sr);
			int index = searchResults.size() - 1;
			//notify all listeners
			TableModelEvent e = new TableModelEvent( this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
			for( int i = 0, n = listeners.size(); i < n; i++ ){
				((TableModelListener)listeners.get( i )).tableChanged( e );
			}
		}
		
		public SearchResult get(int index) {
			return searchResults.get(index);
		}
		
		public void clear() {
			searchResults.clear();
			TableModelEvent e = new TableModelEvent(this);
			for( int i = 0, n = listeners.size(); i < n; i++ ){
				((TableModelListener)listeners.get( i )).tableChanged( e );
			}
		}
		
		@Override
		public void addTableModelListener(TableModelListener l) {
			listeners.add(l);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch( columnIndex ){
				case 0: return GregorianCalendar.class; //article date
				case 1: return String.class; //article title
				case 2: return Integer.class; //start coordinate
				case 3: return Integer.class; //end coordinate
				case 4: return String.class; //search result: text to be displayed
				default: return null;
			}
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public String getColumnName(int column) {
			switch( column ){
				case 0: return "Date";
				case 1: return "Article title";
				case 2: return "Start";
				case 3: return "End";
				case 4: return "Match";
				default: return null;
			}
		}

		@Override
		public int getRowCount() {
			return searchResults.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			SearchResult item = searchResults.get(rowIndex);
			switch( columnIndex ){
				case 0:
					SimpleDateFormat df = new SimpleDateFormat( "dd.MM.yyyy" );
					Date myDate = item.getDate().getTime();
					return df.format(myDate);
					//return item.getDate().get(GregorianCalendar.DATE) + "." + item.getDate().get(GregorianCalendar.MONTH + 1) + "." + item.getDate().get(GregorianCalendar.YEAR);
				case 1: return item.getArticleTitle();
				case 2: return item.getStartCoordinate();
				case 3: return item.getEndCoordinate();
				case 4: return item.getDisplayText();
				default: return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			listeners.remove(l);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			SearchResult item = searchResults.get(rowIndex);
			switch( columnIndex ){
				case 0: 
					item.setDate( (GregorianCalendar)aValue );
					break;
				case 1: 
					item.setArticleTitle((String)aValue);
					break;
				case 2:
					item.setStartCoordinate((Integer) aValue);
					break;
				case 3:
					item.setEndCoordinate((Integer) aValue);
					break;
				case 4:
					item.setDisplayText((String) aValue);
					break;
			}
			TableModelEvent e = new TableModelEvent(this);
			for( int i = 0, n = listeners.size(); i < n; i++ ){
				((TableModelListener)listeners.get( i )).tableChanged( e );
			}
		}
	}
}