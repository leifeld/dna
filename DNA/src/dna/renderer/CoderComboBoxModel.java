package dna.renderer;

import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import dna.Dna;
import dna.dataStructures.Coder;

public class CoderComboBoxModel implements ComboBoxModel<Object> {
	private Object selectedItem;
	Vector<ListDataListener> listeners = new Vector<ListDataListener>();
	
	@Override
	public int getSize() {
		return Dna.data.getCoders().size();
	}
	
	@Override
	public Object getElementAt(int index) {
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
		Dna.data.setActiveCoder(((Coder) anItem).getId());
		selectedItem = anItem;
	}

	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}
	
	public void removeSelectedItem() {
		int id = ((Coder) selectedItem).getId();
		boolean success = false;
		for (int i = Dna.data.getCoders().size() - 1; i > -1; i--) {
			if (Dna.data.getCoders().get(i).getId() == id) {
				Dna.data.getCoders().remove(i);
				success = true;
			}
			break;
		}
		if (success == false) {
			System.err.println("Coder was not found and could not be removed!");
		}
	}
}
