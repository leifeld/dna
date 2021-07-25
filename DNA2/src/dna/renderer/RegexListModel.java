package dna.renderer;

import java.util.Collections;

import javax.swing.AbstractListModel;

import dna.Dna;
import dna.dataStructures.Regex;

@SuppressWarnings("serial")
public class RegexListModel extends AbstractListModel<Regex> {
	
	public void removeElement(int index) {
		Dna.data.getRegexes().remove(index);
        fireContentsChanged(this, 0, Dna.data.getRegexes().size() - 1);
	}
	
	public void replaceElement(int index, Object o) {
		Dna.data.getRegexes().set(index, (Regex) o);
        fireContentsChanged(this, index, index);
	}
	
    public void addElement(Object o) {
        Dna.data.getRegexes().add((Regex) o);
        Collections.sort(Dna.data.getRegexes());
        fireContentsChanged(this, 0, Dna.data.getRegexes().size() - 1);
    }

    @Override
    public Regex getElementAt(int index) {
    	return Dna.data.getRegexes().get(index);
    }
    
    @Override
    public int getSize() {
    	return Dna.data.getRegexes().size();
    }
    
    public void updateList() {
    	fireContentsChanged(this, 0, Dna.data.getRegexes().size() - 1);
    }
}