package dna;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;

public class RegexListModelBackup implements ListModel {
	String cf = "/home/philip/dna-db3";
	private ResultSet sortedData;
	private Vector<ListDataListener> listeners = new Vector<ListDataListener>();

	/*
	public RegexListModel () {
		try {
			Class.forName("org.h2.Driver");
			Connection db = DriverManager.getConnection("jdbc:h2:" + cf, 
					"sa", "");
			Statement s = db.createStatement();
			sortedData = s.executeQuery("SELECT * FROM REGEXES ORDER BY REGEX");
			s.close();
			db.close();
		} catch (SQLException e) {
			System.out.println("Warning: SQL exception in RegexListModel!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	*/

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add( l );
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove( l );
	}
	
	public void addRegex(String text, int red, int green, int blue) {
		try {
			Class.forName("org.h2.Driver");
			Connection db = DriverManager.getConnection("jdbc:h2:" + cf, 
					"sa", "");
			Statement s = db.createStatement();
			s.execute("INSERT INTO REGEXES (REGEX, RED, GREEN, BLUE) VALUES ('" + text + "', " + red + ", " + green + ", " + blue + ")");
			s.close();
			db.close();
		} catch (SQLException e) {
			System.out.println("Warning: SQL exception in addRegex!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		updateTable();
	}
	
	public void removeRegex(String text) {
		try {
			Class.forName("org.h2.Driver");
			Connection db = DriverManager.getConnection("jdbc:h2:" + cf, 
					"sa", "");
			Statement s = db.createStatement();
			s.execute("DELETE FROM REGEXES WHERE REGEX = \"" + text + "\"");
			s.close();
			db.close();
		} catch (SQLException e) {
			System.out.println("Warning: SQL exception in removeRegex (String)!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		updateTable();
	}

	public void removeRegex(int index) {
		String regex = null;
		try {
			Class.forName("org.h2.Driver");
			Connection db = DriverManager.getConnection("jdbc:h2:" + cf, 
					"sa", "");
			Statement s = db.createStatement();
			sortedData = s.executeQuery("SELECT * FROM REGEXES ORDER BY REGEX");
			sortedData.absolute(index);
			regex = sortedData.getString("REGEX");
			s.close();
			db.close();
		} catch (SQLException e) {
			System.out.println("Warning: SQL exception in removeRegex (int)!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		removeRegex(regex);
	}
	
	public void updateTable() {
		int numRecords = -1;
		try {
			Class.forName("org.h2.Driver");
			Connection db = DriverManager.getConnection("jdbc:h2:" + cf, 
					"sa", "");
			Statement s = db.createStatement();
			sortedData = s.executeQuery("SELECT * FROM REGEXES ORDER BY REGEX");
			boolean lastValid = sortedData.last();
			if (lastValid == false) {
				numRecords = 0;
			} else {
				numRecords = sortedData.getRow();
			}
			s.close();
			db.close();
		} catch (SQLException e) {
			System.out.println("Warning: SQL exception in updateTable!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, numRecords);
		for( int i = 0, n = listeners.size(); i < n; i++ ){
			((ListDataListener)listeners.get( i )).contentsChanged( e );
		}
	}
	
	public void removeAll() {
		try {
			Class.forName("org.h2.Driver");
			Connection db = DriverManager.getConnection("jdbc:h2:" + cf, 
					"sa", "");
			Statement s = db.createStatement();
			s.execute("TRUNCATE TABLE REGEXES");
			s.close();
			db.close();
		} catch (SQLException e) {
			System.out.println("Warning: SQL exception in removeAll!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		updateTable();
	}
	
	@Override
	public Object getElementAt(int index) {
		String regex = null;
		try {
			Class.forName("org.h2.Driver");
			Connection db = DriverManager.getConnection("jdbc:h2:" + cf, 
					"sa", "");
			Statement s = db.createStatement();
			sortedData = s.executeQuery("SELECT * FROM REGEXES ORDER BY REGEX");
			sortedData.absolute(index);
			regex = sortedData.getString("REGEX");
			sortedData.close();
			s.close();
			db.close();
		} catch (SQLException e) {
			System.out.println("Warning: SQL exception in getSize!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return regex;
	}

	@Override
	public int getSize() {
		System.out.println("Neues Spiel, neues Glück.");
		int numRecords = -1;
		try {
			Class.forName("org.h2.Driver");
			Connection db = DriverManager.getConnection("jdbc:h2:" + cf, 
					"sa", "");
			Statement s = db.createStatement();
			sortedData = s.executeQuery("SELECT * FROM REGEXES ORDER BY REGEX");
			boolean lastValid = sortedData.last();
			if (lastValid == false) {
				System.out.println("Nix da.");
				numRecords = 0;
			} else {
				System.out.println(numRecords + " Zeilen da.");
				numRecords = sortedData.getRow();
			}
			
			//ResultSet rs = s.executeQuery("SELECT count(*) FROM REGEXES");
			//rs.next();
			//numRecords = rs.getInt(1);
			
			//int count = 0;
			//  while(rs.next()) {
			//    count++; // Zeilen-Zähler erhöhen
			    // Hier Daten verarbeiten
			//  }
			//numRecords = count;
			//rs.close();
			s.close();
			db.close();
		} catch (SQLException e) {
			System.out.println("Warning: SQL exception in getSize!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return numRecords;
	}
}