package dna.renderer;

import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import dna.Dna;
import dna.dataStructures.Coder;

@SuppressWarnings("serial")
public class CoderComboBoxModel extends AbstractListModel<Coder> implements ComboBoxModel<Coder> {
	private Object selectedItem;
	Vector<ListDataListener> listeners = new Vector<ListDataListener>();
	
	@Override
	public int getSize() {
		return Dna.data.getCoders().size();
	}
	
	@Override
	public Coder getElementAt(int index) {
		return Dna.data.getCoders().get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
		
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}
	
	@Override
	public void setSelectedItem(Object anItem) {
		selectedItem = anItem;
		//System.out.println(((Coder)selectedItem).getName());
		//fireContentsChanged(this, 0, getSize() - 1);
	}
	
	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}
	
	public void clear() {
		selectedItem = null;
		fireContentsChanged(this, 0, 0);
	}
}
