package dna.renderer;

import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import dna.Dna;
import dna.dataStructures.StatementType;

@SuppressWarnings("serial")
public class StatementTypeComboBoxModel extends AbstractListModel<StatementType> implements ComboBoxModel<StatementType> {
	private Object selectedItem;
	Vector<ListDataListener> listeners = new Vector<ListDataListener>();
	
	@Override
	public int getSize() {
		return Dna.data.getStatementTypes().size();
	}
	
	@Override
	public StatementType getElementAt(int index) {
		return Dna.data.getStatementTypes().get(index);
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
		//System.out.println(((StatementType)selectedItem).getName());
		fireContentsChanged(this, 0, getSize() - 1);
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
