package dna.export;

import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;

@SuppressWarnings("serial")
public class Gui extends JDialog {
	
	public Gui() {
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * This function returns a {@link DefaultListModel} with the variables of the statementType selected to fill a JList.
	 * 
	 * @param longtext	boolean indicating whether long text variables should be included.
	 * @param shorttext	boolean indicating whether short text variables should be included.
	 * @param integer	boolean indicating whether integer variables should be included.
	 * @param bool		boolean indicating whether boolean variables should be included.
	 * @return			{@link DefaultListModel<String>} with variables of the the statementType selected.
	 */
	/*
	DefaultListModel<String> getVariablesList(boolean longtext, boolean shorttext, boolean integer, boolean bool) {
		LinkedHashMap<String, String> variables = statementType.getVariables();
		Iterator<String> it = variables.keySet().iterator();
		DefaultListModel<String> listData = new DefaultListModel<String>();
		while (it.hasNext()) {
			String var = it.next();
			if ((longtext == true && variables.get(var).equals("long text")) || 
					(shorttext == true && variables.get(var).equals("short text")) ||
					(integer == true && variables.get(var).equals("integer")) ||
					(bool == true && variables.get(var).equals("boolean"))) {
				listData.addElement(var);
			}
		}
		return listData;
	}
	*/
}
