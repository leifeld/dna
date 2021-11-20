package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.event.ListDataListener;

import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import model.Coder;
import sql.Sql;

/**
 * Class for creating a coder password dialog and returning the password.
 */
public class CoderPasswordCheckDialog {
	Coder coder = null;
	String password = null;

	/**
	 * Constructor with a specified coder.
	 */
	/*
	public CoderPasswordCheckDialog(Coder coder) {
		this.coder = coder;
		JPanel panel = new JPanel(new BorderLayout());

		JPanel questionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel pwLabel = new JLabel("Please enter the password for coder: ");
		questionPanel.add(pwLabel);
		CoderBadgePanel cbp = new CoderBadgePanel(coder);
		questionPanel.add(cbp);
		panel.add(questionPanel, BorderLayout.NORTH);
		
		JPasswordField pw = new JPasswordField(20);
		panel.add(pw);
		
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"[GUI] User was asked to enter coder password for connection profile.",
				"A password check dialog was displayed to authenticate Coder " + coder.getId() + ".");
		Dna.logger.log(l);
		
		@SuppressWarnings("serial")
		JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				pw.requestFocusInWindow();
			}
		};
		JDialog dialog = pane.createDialog(null, "Coder verification");
		dialog.setVisible(true);
		if ((int) pane.getValue() == 0) { // 0 = OK button pressed
			this.password = new String(pw.getPassword());
		}
	}
	*/
	
	/**
	 * Constructor with unknown coder (because the connection profile was
	 * encrypted and the coder details could not be read yet before getting the
	 * password from here).
	 */
	public CoderPasswordCheckDialog() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel pwLabel = new JLabel("Please enter the password for the coder in the connection profile.");
		panel.add(pwLabel, BorderLayout.NORTH);
		
		JPasswordField pw = new JPasswordField(20);
		panel.add(pw);
		
		LogEvent l = new LogEvent(Logger.MESSAGE,
				"[GUI] User was asked to enter coder password for connection profile.",
				"A password check dialog was displayed to authenticate the coder associated with a connection profile.");
		Dna.logger.log(l);
		
		@SuppressWarnings("serial")
		JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				pw.requestFocusInWindow();
			}
		};
		JDialog dialog = pane.createDialog(null, "Coder verification");
		dialog.setVisible(true);
		if ((int) pane.getValue() == 0) { // 0 = OK button pressed
			password = new String(pw.getPassword());
		}
	}
	
	/**
	 * Constructor with multiple coders to select from or assuming the coder
	 * saved in the connection profile in the SQL connection.
	 * 
	 * @param sql  Sql connection from which the coder(s) can be
	 *   retrieved.
	 * @param chooseCoder  boolean value indicating whether a combo box should
	 *   be created to select the coder (true) or whether the coder from the SQL
	 *   connection profile should be used (false).
	 * @param selectId     A coder ID to select. The ID is ignored if it is not
	 *   found among the coders, otherwise selected in the combo box.
	 */
	public CoderPasswordCheckDialog(Sql sql, boolean chooseCoder, int selectId) {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel questionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel pwLabel = new JLabel("Please enter the password for coder: ");
		questionPanel.add(pwLabel);
		JComboBox<Coder> comboBox = new JComboBox<Coder>();

		if (chooseCoder == true) {
			ArrayList<Coder> coders = sql.getCoders();
			int selectIndex = -1;
			if (selectId > 0) {
				for (int i = 0; i < coders.size(); i++) {
					if (coders.get(i).getId() == selectId) {
						selectIndex = i;
					}
				}
			}
			CoderComboBoxModel comboBoxModel = new CoderComboBoxModel(coders);
			comboBox.setModel(comboBoxModel);
			comboBox.setRenderer(new CoderComboBoxRenderer());
			if (coders.size() > 0) {
				if (selectIndex > -1) {
					comboBox.setSelectedIndex(selectIndex);
				} else {
					comboBox.setSelectedIndex(0);
				}
			}
			questionPanel.add(comboBox);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] User was asked to select coder and enter coder password.",
					"A password check dialog was displayed to select and authenticate a coder.");
			Dna.logger.log(l);
			
		} else {
			coder = sql.getActiveCoder();
			CoderBadgePanel cdp = new CoderBadgePanel(coder);
			questionPanel.add(cdp);
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"[GUI] User was asked to enter coder password.",
					"A password check dialog was displayed to authenticate a coder in a database.");
			Dna.logger.log(l);
		}
		panel.add(questionPanel, BorderLayout.NORTH);
		
		JPasswordField pw = new JPasswordField(20);
		panel.add(pw);
		
		@SuppressWarnings("serial")
		JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				pw.requestFocusInWindow();
			}
		};
		JDialog dialog = pane.createDialog(null, "Coder verification");
		dialog.setVisible(true);
		if ((int) pane.getValue() == 0) { // 0 = OK button pressed
			password = new String(pw.getPassword());
			coder = (Coder) comboBox.getSelectedItem();
		}
	}
	
	public Coder getCoder() {
		return this.coder;
	}
	
	public String getPassword() {
		return this.password;
	}

	private class CoderComboBoxRenderer implements ListCellRenderer<Object> {
		
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value == null) {
				return new JLabel("select coder...");
			} else {
				Coder coder = (Coder) value;
				CoderBadgePanel cbp = new CoderBadgePanel(coder);
				if (isSelected) {
					UIDefaults defaults = javax.swing.UIManager.getDefaults();
					Color bg = defaults.getColor("List.selectionBackground");
					cbp.setBackground(bg);
				}
				return cbp;
			}
		}
	}

	/**
	 * A model for holding coders in a combo box.
	 */
	public class CoderComboBoxModel extends AbstractListModel<Coder> implements ComboBoxModel<Coder> {
		private static final long serialVersionUID = 8412600030500406168L;
		private Object selectedItem;
		Vector<ListDataListener> listeners = new Vector<ListDataListener>();
		ArrayList<Coder> coders;
		
		public CoderComboBoxModel(ArrayList<Coder> coders) {
			this.coders = coders;
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
	}
}