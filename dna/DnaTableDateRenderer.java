package dna;

import javax.swing.table.DefaultTableCellRenderer;
import java.util.*;
import java.awt.Component;
import javax.swing.JTable;
import java.text.SimpleDateFormat;
import javax.swing.*;

class DnaTableDateRenderer extends DefaultTableCellRenderer{
	SimpleDateFormat df = new SimpleDateFormat( "dd.MM.yyyy" );
	
	public DnaTableDateRenderer(){
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		String text = df.format((Date)value);
		
		setHorizontalAlignment(SwingConstants.CENTER);
		
		return super.getTableCellRendererComponent(table, text, isSelected,
				hasFocus, row, column);
	}
}