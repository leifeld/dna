package dna;

import javax.swing.DefaultListModel;

@SuppressWarnings("serial")
public class RegexListModel extends DefaultListModel {
	public boolean containsIdenticalRegexTerm(RegexTerm rt) {
		for (int i = 0; i < size(); i++) {
			if ( ((RegexTerm)getElementAt(i)).equals(rt) ) {
				return true;
			}
		}
		return false;
	}
}