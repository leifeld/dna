package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import dna.Dna;
import model.Attribute;
import model.Coder;
import model.Statement;
import model.Value;

/**
 * Show a small popup window to display and/or edit the variables of a statement.
 */
public class Popup extends JDialog {
	private static final long serialVersionUID = -4955213646188753456L;
	Container c;
	double X, Y;
	Point los;
	static int statementTypeId;
	Color color;
	boolean windowDecoration, editable;
	static int statementId;
	JPanel gridBagPanel;
	Connection conn;
	int textFieldWidth;
	ArrayList<Value> variables;
	
	/**
	 * Popup dialog window to display the contents of a statements. The user can
	 * edit the values of each variable.
	 * 
	 * @param X         Horizontal coordinate for the window.
	 * @param Y         Vertical coordinate for the window.
	 * @param statement The {@link Statement} to be edited.
	 * @param location  Location of the DNA text panel on screen.
	 * @param coder     The current coder who is viewing the statement.
	 */
	public Popup(double X, double Y, Statement statement, int documentId, Point location, Coder coder) {
		this.X = X;
		this.Y = Y;
		Popup.statementId = statement.getId();
		this.los = location;
		this.textFieldWidth = coder.getPopupWidth();
		this.color = statement.getStatementTypeColor();
		if (coder.getPopupDecoration() == 1) {
			this.windowDecoration = true;
			this.setModal(true);
		} else {
			this.windowDecoration = false;
			this.setUndecorated(true);
		}
		statementTypeId = statement.getStatementTypeId();
		
		// should the changes in the statements be saved? check permissions...
		editable = true;
		if (statement.getCoder() != coder.getId() && coder.getPermissionEditOthersStatements() == 0) {
			editable = false;
		}
		if (coder.getPermissionEditStatements() == 0) {
			editable = false;
		}
		
		this.setTitle("Statement details");
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		if (windowDecoration == true) {
			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					if (editable == true) {
						String message = "Save any changes in Statement " + statement.getId() + "?";
						int dialog = JOptionPane.showConfirmDialog(Popup.this, message, "Confirmation", JOptionPane.YES_NO_OPTION);
						if (dialog == 0) {
							saveContents(gridBagPanel, statementId, variables);
						}
					}
					dispose();
				}
			});
		} else {
			this.addWindowFocusListener(new WindowAdapter() {
				public void windowLostFocus(WindowEvent e) {
					saveContents(gridBagPanel, statementId, variables);
	                dispose();
				}
			});
		}

		ImageIcon statementIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-message-2.png"));
		this.setIconImage(statementIcon.getImage());
		
		c = getContentPane();
		
		JPanel contentsPanel = new JPanel(new BorderLayout());
		contentsPanel.setBorder(new LineBorder(Color.BLACK));
		JPanel titleDecorationPanel = new JPanel(new BorderLayout());
		JPanel idAndPositionPanel = new JPanel();
		
		gridBagPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JLabel sPosLabel = new JLabel(" start:");
		JTextField startPos = new JTextField(Integer.toString(statement.getStart()));
		startPos.setEditable(false);
		
		JLabel ePosLabel = new JLabel(" end:");
		JTextField endPos = new JTextField(Integer.toString(statement.getStop()));
		endPos.setEditable(false);

		JLabel idLabel = new JLabel(" ID:");
		JTextField idField = new JTextField(Integer.toString(statementId));
		idField.setEditable(false);

		String type = statement.getStatementTypeLabel();
		JLabel typeLabel = new JLabel(" " + type);
		
		JSeparator sep = new JSeparator();
		
		JPanel colorPanel = new JPanel();
		colorPanel.setBackground(color);
		colorPanel.setPreferredSize(new Dimension(4, 4));
		
		ImageIcon duplicateIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-copy.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton duplicate = new JButton(duplicateIcon);
		duplicate.setToolTipText("create a copy of this statement at the same location");
		duplicate.setPreferredSize(new Dimension(16, 16));
		duplicate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (coder.getPermissionAddStatements() == 1) {
					if (editable == true && windowDecoration == true) {
						String message = "Save any changes in Statement " + statement.getId() + " before creating copy?";
						int dialog = JOptionPane.showConfirmDialog(Popup.this, message, "Confirmation", JOptionPane.YES_NO_OPTION);
						if (dialog == 0) {
							saveContents(gridBagPanel, statementId, variables);
						}
					}
					int newStatementId = Dna.sql.cloneStatement(statementId, coder.getId());
					Dna.dna.getMainWindow().getTextPanel().selectStatement(newStatementId, documentId, true);
					dispose();
				}
			}
		});
		if (coder.getPermissionAddStatements() == 0) {
			duplicate.setEnabled(false);
		}
		
		ImageIcon removeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-trash.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton remove = new JButton(removeIcon);
		remove.setToolTipText("completely remove the whole statement (but keep the text)");
		remove.setPreferredSize(new Dimension(16, 16));
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(null, 
						"Are you sure you want to remove this statement?", 
						"Remove?", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					Dna.sql.deleteStatement(statementId);
					Dna.dna.getMainWindow().getTextPanel().paintStatements();
					Dna.dna.getMainWindow().getDocumentTableModel().decreaseFrequency(documentId);
					dispose();
				}
			}
		});
		if (coder.getPermissionDeleteStatements() == 1 && (statement.getCoder() == coder.getId() || coder.getPermissionEditOthersStatements() == 1)) {
			remove.setEnabled(true);
		} else {
			remove.setEnabled(false);
		}
		
		idAndPositionPanel.add(idLabel);
		idAndPositionPanel.add(idField);
		idAndPositionPanel.add(sPosLabel);
		idAndPositionPanel.add(startPos);
		idAndPositionPanel.add(ePosLabel);
		idAndPositionPanel.add(endPos);
		idAndPositionPanel.add(new JLabel("  "));
		idAndPositionPanel.add(duplicate);
		idAndPositionPanel.add(remove);
		
		titleDecorationPanel.add(idAndPositionPanel, BorderLayout.EAST);
		titleDecorationPanel.add(typeLabel, BorderLayout.CENTER);
		titleDecorationPanel.add(sep, BorderLayout.SOUTH);
		titleDecorationPanel.add(colorPanel, BorderLayout.WEST);
		contentsPanel.add(titleDecorationPanel, BorderLayout.NORTH);
		
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		
		variables = statement.getValues();
		for (int i = 0; i < variables.size(); i++) {
			String key = variables.get(i).getKey();
			String dataType = variables.get(i).getDataType();
			JLabel label = new JLabel(key, JLabel.TRAILING);
			if (dataType.equals("short text")) {
				Attribute[] attributeArray = Dna.sql.getAttributes(variables.get(i).getVariableId());
				JComboBox<Attribute> box = new JComboBox<Attribute>(attributeArray);
				box.setRenderer(new AttributeComboBoxRenderer());
				box.setEditable(true);
				
				// paint the selected value in the attribute color
				String s = ((JTextField) box.getEditor().getEditorComponent()).getText();
				Color fg = javax.swing.UIManager.getColor("TextField.foreground"); // default unselected foreground color of JTextField
				for (int j = 0; j < box.getModel().getSize(); j++) {
					if (s.equals(box.getModel().getElementAt(j).getValue())) {
						fg = box.getModel().getElementAt(j).getColor();
					}
				}
				((JTextField) box.getEditor().getEditorComponent()).setSelectedTextColor(fg);
				((JTextField) box.getEditor().getEditorComponent()).setForeground(fg);
				
				// add a document listener to the combobox to paint the selected value in the attribute color, despite being highlighted
				((JTextField) box.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {
					@Override
					public void changedUpdate(DocumentEvent arg0) {
						formatEntry();
					}
					@Override
					public void insertUpdate(DocumentEvent arg0) {
						formatEntry();
					}
					@Override
					public void removeUpdate(DocumentEvent arg0) {
						formatEntry();
					}
					private void formatEntry() {
						Color fg = javax.swing.UIManager.getColor("TextField.foreground"); // default unselected foreground color of JTextField
						for (int i = 0; i < box.getModel().getSize(); i++) {
							if (((JTextField) box.getEditor().getEditorComponent()).getText().equals(box.getModel().getElementAt(i).getValue())) {
								fg = box.getModel().getElementAt(i).getColor();
							}
						}
						((JTextField) box.getEditor().getEditorComponent()).setSelectedTextColor(fg);
						((JTextField) box.getEditor().getEditorComponent()).setForeground(fg);
					}
				});
				
				if (coder.getPopupAutoComplete() == 1) {
					AutoCompleteDecorator.decorate(box); // auto-complete short text values; part of SwingX
				}
				if (editable == true) {
					box.setEnabled(true);
				} else {
					box.setEnabled(false);
				}
    			box.setPreferredSize(new Dimension(this.textFieldWidth, 20));
    			box.setSelectedItem((Attribute) variables.get(i).getValue());
    			
				gbc.anchor = GridBagConstraints.EAST;
	    		gridBagPanel.add(label, gbc);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.gridx++;
				gridBagPanel.add(box, gbc);
				gbc.gridx--;
				gbc.gridy++;
			} else if (dataType.equals("long text")) {
				String entry = (String) variables.get(i).getValue();
    			JTextArea box = new JTextArea();
    			box.setEditable(true);
				if (editable == true) {
					box.setEnabled(true);
				} else {
					box.setEnabled(false);
				}
    			box.setWrapStyleWord(true);
    			box.setLineWrap(true);
    			box.setText(entry);
    			JScrollPane boxScroller = new JScrollPane(box);
    			boxScroller.setPreferredSize(new Dimension(this.textFieldWidth, 100));
    			boxScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    			
				gbc.anchor = GridBagConstraints.NORTHEAST;
	    		gridBagPanel.add(label, gbc);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.gridx++;
				gridBagPanel.add(boxScroller, gbc);
				gbc.gridx--;
				gbc.gridy++;
			} else if (dataType.equals("boolean")) {
				int entry = (Integer) variables.get(i).getValue();
				boolean val;
				if (entry == 0) {
					val = false;
				} else {
					val = true;
				}
				BooleanButtonPanel buttons = new BooleanButtonPanel();
				if (val == true) {
					buttons.setYes(true);
				} else {
					buttons.setYes(false);
				}
				if (editable == true) {
					buttons.setEnabled(true);
				} else {
					buttons.setEnabled(false);
				}
    			
				gbc.anchor = GridBagConstraints.EAST;
	    		gridBagPanel.add(label, gbc);
				gbc.insets = new Insets(0,0,0,0);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.gridx++;
				gridBagPanel.add(buttons, gbc);
				gbc.insets = new Insets(3,3,3,3);
				gbc.gridx--;
				gbc.gridy++;
			} else if (dataType.equals("integer")) {
				int entry = (Integer) variables.get(i).getValue();
				JSpinner jsp = new JSpinner();
				jsp.setValue(entry);
    			jsp.setPreferredSize(new Dimension(70, 20));
    			jsp.setEnabled(true);
    			JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
    			jp.add(jsp);
				if (editable == true) {
					jsp.setEnabled(true);
				} else {
					jsp.setEnabled(false);
				}
    			
				gbc.anchor = GridBagConstraints.EAST;
	    		gridBagPanel.add(label, gbc);
				gbc.insets = new Insets(0, 0, 0, 0);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.gridx++;
				gridBagPanel.add(jp, gbc);
				gbc.insets = new Insets(3, 3, 3, 3);
				gbc.gridx--;
				gbc.gridy++;
			}
		}
		
		contentsPanel.add(gridBagPanel, BorderLayout.CENTER);
		
		if (windowDecoration == true) {
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			ImageIcon cancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
			JButton cancelButton = new JButton("Cancel", cancelIcon);
			cancelButton.setToolTipText("close this window without making any changes");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
			buttonPanel.add(cancelButton);
			ImageIcon saveIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-check.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
			JButton saveButton = new JButton("Save", saveIcon);
			saveButton.setToolTipText("save each variable into the database and close this window");
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					saveContents(gridBagPanel, statementId, variables);
					dispose();
				}
			});
			buttonPanel.add(saveButton);
			if (editable == false) {
				saveButton.setEnabled(false);
			}
			contentsPanel.add(buttonPanel, BorderLayout.SOUTH);
		}
		
		c.add(contentsPanel);
		
		this.pack();
		double xDouble = los.getX() + X;
		double yDouble = los.getY() + Y;
		int x = (int) xDouble + 6;
		int y = (int) yDouble + 13;
		this.setLocation(x, y);
		this.setVisible(true);
	}

	/**
	 * In a statement popup window, read the contents from all combo boxes and save them into the database.
	 * 
	 * @param gridBagPanel  The panel that contains the combo boxes
	 * @param statementID   The ID of the statement that is being edited
	 */
	public static void saveContents(JPanel gridBagPanel, int statementID, ArrayList<Value> variables) {
		Component[] com = gridBagPanel.getComponents();
		int i, j;
		for (i = 0; i < com.length; i++) {
			Object content = null; // the value of a variable, e.g., "EPA"
			String variableName; // the name of the variable, e.g., "organization"
			if (com[i].getClass().getName().equals("javax.swing.JComboBox")) { // short text
				variableName = ((JLabel) com[i - 1]).getText();
				@SuppressWarnings("unchecked")
				JComboBox<Attribute> box = (JComboBox<Attribute>) com[i]; // save the combo box
				Object object = box.getSelectedItem();
				Attribute attribute;
				if (object.getClass().getName().endsWith("String")) { // if not an existing attribute, the editor returns a String
					attribute = new Attribute((String) object); // the new attribute has an ID of -1; the SQL class needs to take care of this when writing into the database
				} else {
					attribute = (Attribute) box.getSelectedItem();
				}
				for (j = 0; j < variables.size(); j++) { // update the variable corresponding to the variable name identified
					if (variables.get(j).getKey().equals(variableName)) {
						variables.get(j).setValue(attribute);
					}
				}
			} else if (com[i].getClass().getName().equals("javax.swing.JScrollPane")) { // long text
				variableName = ((JLabel) com[i - 1]).getText();
				JScrollPane jsp = ((JScrollPane) com[i]);
				JTextArea jta = (JTextArea) jsp.getViewport().getView();
				content = jta.getText();
				if (content == null) {
					content = "";
				}
				for (j = 0; j < variables.size(); j++) {
					if (variables.get(j).getKey().equals(variableName)) {
						variables.get(j).setValue(content);
					}
				}
			} else if (com[i].getClass().getName().equals("javax.swing.JPanel")) { // integer
				variableName = ((JLabel) com[i - 1]).getText();
				JPanel jp = (JPanel) com[i];
				JSpinner jsp = (JSpinner) jp.getComponent(0);
				content = jsp.getValue();
				for (j = 0; j < variables.size(); j++) {
					if (variables.get(j).getKey().equals(variableName)) {
						variables.get(j).setValue(content);
					}
				}
			} else if (com[i].getClass().getName().endsWith("BooleanButtonPanel")) { // boolean
				variableName = ((JLabel) com[i - 1]).getText();
				content = ((BooleanButtonPanel) com[i]).isYes();
				int intBool;
				if ((Boolean) content == false) {
					intBool = 0;
				} else {
					intBool = 1;
				}
				for (j = 0; j < variables.size(); j++) {
					if (variables.get(j).getKey().equals(variableName)) {
						variables.get(j).setValue(intBool);
					}
				}
			}
		}
		Dna.sql.updateStatement(statementID, variables); // write changes into the database
	}
	
	/**
	 * A panel with a yes and a no radio button to represent boolean variables
	 * in statement popup windows.
	 */
	private class BooleanButtonPanel extends JPanel {
		private static final long serialVersionUID = 2614141772546080638L;
		JRadioButton yes, no;
		
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
		 * @param b  true if yes should be selected; false if no should be selected
		 */
		public void setYes(boolean b) {
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
		public boolean isYes() {
			if (yes.isSelected()) {
				return true;
			} else {
				return false;
			}
		}
		
		/**
		 * Enable or disable the buttons
		 * 
		 * @param enabled Enable the buttons if true and disabled them otherwise
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
	
	/**
	 * A renderer for JComboBox items that represent {@link Attribute} objects.
	 * The value is shown as text. The color is shown as the foreground color.
	 * If the attribute is not present in the database, it gets a red background
	 * color. The renderer is used to display combo boxes for short text
	 * variables in popup windows. The renderer only displays the list items,
	 * not the contents of the text editor at the top of the list.
	 */
	public class AttributeComboBoxRenderer implements ListCellRenderer<Object> {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Attribute a = (Attribute) value;
			JLabel label = new JLabel(a.getValue());
			label.setForeground(a.getColor());
			
			// list background
			Color selectedColor = javax.swing.UIManager.getColor("List.dropCellBackground");
			Color notInDatabaseColor = new Color(255, 102, 102);
			// selected entry that is not in database: average of the previous two colors
			Color selectedAndNotInDatabaseColor = new Color((selectedColor.getRed() + notInDatabaseColor.getRed()) / 2, (selectedColor.getGreen() + notInDatabaseColor.getGreen()) / 2, (selectedColor.getBlue() + notInDatabaseColor.getBlue()) / 2);
			Color defaultColor = javax.swing.UIManager.getColor("List.background");
			if (isSelected == true && a.isInDatabase() == true) {
				label.setBackground(selectedColor);
			} else if (isSelected == true && a.isInDatabase() == false) {
				label.setBackground(selectedAndNotInDatabaseColor);
			} else if (isSelected == false && a.isInDatabase() == false) {
				label.setBackground(notInDatabaseColor);
			} else if (isSelected == false && a.isInDatabase() == true) {
				label.setBackground(defaultColor);
			}
			label.setOpaque(true);
			
			return label;
		}
	}
}