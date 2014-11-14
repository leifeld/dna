package dna;
// Source: http://crypt-sdk.blogspot.ch/2012/08/drag-and-drop-cells-in-jtable-java-swing.html

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.JTable;

class TS extends TransferHandler {
	private static final long serialVersionUID = 1L;

	public TS() {
	}

	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent source) {
		return new StringSelection((String) ((JTable) source).
				getModel().getValueAt(((JTable) source).getSelectedRow(), 
						((JTable) source).getSelectedColumn()));
	}
	/*
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        ((JTable) source).getModel().setValueAt("", ((JTable) source).
        		getSelectedRow(), ((JTable) source).getSelectedColumn());
    }
	 */
	@Override
	public boolean canImport(TransferSupport support) {
		return true;
	}

	/*
	 * LB.Note: The primary support for handling a drop is the same as for a 
	 * paste operation,  the importData method on the TransferHandler
	 * http://docs.oracle.com/javase/7/docs/technotes/guides/swing/1.4/dnd.html
	 * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
	 */
	@Override
	public boolean importData(TransferSupport support) {
		JTable jt = (JTable) support.getComponent();
		try {
			jt.setValueAt(support.getTransferable().getTransferData(DataFlavor.
					stringFlavor), jt.getSelectedRow(), jt.getSelectedColumn());
		} catch (UnsupportedFlavorException ex) {
			Logger.getLogger(TS.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(TS.class.getName()).log(Level.SEVERE, null, ex);
		}
		return super.importData(support);
	}
}




