package dna;

import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

@SuppressWarnings("serial")
public class RegexListRendererBackup extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value,	int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		label.setText((String)value);
		try {
			Class.forName("org.h2.Driver");
			Connection db = DriverManager.getConnection("jdbc:h2:" + dna.Dna.mainProgram.cf, 
					"sa", "");
			Statement s = db.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM REGEXES WHERE REGEX = '" + (String)value + "'");
			rs.next();
			int red = rs.getInt("RED");
			int green = rs.getInt("GREEN");
			int blue = rs.getInt("BLUE");
			Color cl = new Color(red, green, blue);
			label.setForeground(cl);
			s.close();
			db.close();
		} catch (SQLException e2) {
			e2.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		return label;
	}
}