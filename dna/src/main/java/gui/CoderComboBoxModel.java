package gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import model.Coder;

/**
 * A model for holding coders in a combo box.
 */
class CoderComboBoxModel extends AbstractListModel<Coder> implements ComboBoxModel<Coder> {
	private static final long serialVersionUID = 8412600030500406168L;
	private Object selectedItem;
	Vector<ListDataListener> listeners = new Vector<ListDataListener>();
	ArrayList<Coder> coders;
	HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
	
	public CoderComboBoxModel(ArrayList<Coder> coders) {
		this.coders = coders;
		for (int i = 0; i < coders.size(); i++) {
			indexMap.put(coders.get(i).getId(), i);
		}
	}
	
	@Override
	public int getSize() {
		return coders.size();
	}
	
	@Override
	public Coder getElementAt(int index) {
		return coders.get(index);
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
	}
	
	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}
	
	public void clear() {
		selectedItem = null;
		fireContentsChanged(this, 0, 0);
	}
	
	/**
	 * Get the model index for a specific coder ID.
	 * 
	 * @param coderId  The coder ID.
	 * @return The index.
	 */
	public int getIndexByCoderId(int coderId) {
		return(indexMap.get(coderId));
	}
}