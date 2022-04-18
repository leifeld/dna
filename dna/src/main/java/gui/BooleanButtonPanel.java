package gui;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * A panel with a yes and a no radio button to represent boolean variables
 * in statement popup windows.
 */
class BooleanButtonPanel extends JPanel {
	private static final long serialVersionUID = 2614141772546080638L;
	private JRadioButton yes, no;
	
	BooleanButtonPanel() {
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		fl.setVgap(0);
		this.setLayout(fl);
		yes = new JRadioButton("yes");
		no = new JRadioButton("no");
		ButtonGroup group = new ButtonGroup();
		group.add(yes);
		group.add(no);
		this.add(yes);
		this.add(no);
		yes.setSelected(true);
	}
	
	/**
	 * Select the "yes" or "no" button
	 * 
	 * @param b  {@code true} if "yes" should be selected; {@code false} if
	 *   "no" should be selected.
	 */
	void setYes(boolean b) {
		if (b == true) {
			this.yes.setSelected(true);
		} else if (b == false) {
			this.no.setSelected(true);
		}
	}
	
	/**
	 * Is the "yes" button selected?
	 * 
	 * @return boolean yes selected?
	 */
	boolean isYes() {
		if (yes.isSelected()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Enable or disable the buttons.
	 * 
	 * @param enabled  Enable the buttons if {@code true} and disabled them
	 *   otherwise.
	 */
	public void setEnabled(boolean enabled) {
		if (enabled == true) {
			yes.setEnabled(true);
			no.setEnabled(true);
		} else {
			yes.setEnabled(false);
			no.setEnabled(false);
		}
	}
}