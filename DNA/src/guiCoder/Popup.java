package guiCoder;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import dna.Statement;
import dna.Value;

public class Popup extends JDialog {
	
	private static final long serialVersionUID = 1L;
	Container c;
	double X, Y;
	Point los;
	static int statementTypeId;
	Color color;
	static int statementId;
	JPanel gridBagPanel;
	Connection conn;
	int textFieldWidth;
	
	public Popup(double X, double Y, Statement statement, Point location, boolean editable) {
		this.X = X;
		this.Y = Y;
		Popup.statementId = statement.getId();
		this.los = location;
		this.textFieldWidth = 300; // TODO: take popup width from settings
		this.color = statement.getStatementTypeColor(); // TODO: replace by coder color depending on settings
		statementTypeId = statement.getStatementTypeId();
		
		//this.setModal(true);
		this.setUndecorated(true);
		this.setTitle("Statement details");
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		//this is only needed if close buttons are implemented
		//this.addWindowListener(new WindowAdapter() {
		//	public void windowClosing(WindowEvent e) {
		//		saveContents();
		//	}
		//});
		
		this.addWindowFocusListener(new WindowAdapter() {
			public void windowLostFocus(WindowEvent e) {
				// TODO: save contents
				// saveContents(gridBagPanel, statementId);
                dispose();
			}
		});

		ImageIcon statementIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-message-2.png"));
		this.setIconImage(statementIcon.getImage());
		
		c = getContentPane();
		
		JPanel contentsPanel = new JPanel(new BorderLayout());
		contentsPanel.setBorder(new LineBorder(Color.black));
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
		JTextField idField = 
				new JTextField(Integer.toString(statementId));
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
				// TODO: save contents
				// saveContents(gridBagPanel, statementId);
				// TODO: create copy of statement in database, then reload statements in text panel
				// TODO: update # frequency in document table
				// TODO: select the new statement
				// Dna.gui.textPanel.selectStatement(newId, newStatement.getDocumentId(), true);
			}
		});
		
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
					// TODO: delete statement in database, then reload text panel, then paint statements again, then update # frequency
					dispose();
				}
			}
		});
		if (editable == true) {
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
		
		ArrayList<Value> variables = statement.getValues();
		for (int i = 0; i < variables.size(); i++) {
			String key = variables.get(i).getKey();
			String dataType = variables.get(i).getDataType();
			JLabel label = new JLabel(key, JLabel.TRAILING);
			if (dataType.equals("short text")) {
				String val = (String) variables.get(i).getValue();
				// TODO: take the value out of the attribute vector and color it, instead of taking it out of the statement directly: write JDBCWorker
				String[] entryArray = new String[] {"a", "b", "c"};
				JComboBox<String> box = new JComboBox<String>(entryArray);
				//box.setRenderer(new AttributeComboBoxRenderer());
				box.setEditable(true);
				if (editable == true) {
					box.setEnabled(true);
				} else {
					box.setEnabled(false);
				}
    			box.setPreferredSize(new Dimension(this.textFieldWidth, 20));
    			//box.setSelectedItem((AttributeVector)entry);
    			AutoCompleteDecorator.decorate(box);
    			
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
				JCheckBox box = new JCheckBox();
    			box.setPreferredSize(new Dimension(20, 20));
				if (editable == true) {
					box.setEnabled(true);
				} else {
					box.setEnabled(false);
				}
    			if (val == true) {
    				box.setSelected(true);
    			} else {
    				box.setSelected(false);
    			}
    			
				gbc.anchor = GridBagConstraints.EAST;
	    		gridBagPanel.add(label, gbc);
				gbc.insets = new Insets(0,0,0,0);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.gridx++;
				gridBagPanel.add(box, gbc);
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
	 * In a statement popup window, read the contents from all combo boxes and save them into the database and GUI data structure.
	 * 
	 * @param gridBagPanel  The panel that contains the combo boxes
	 * @param statementID   The ID of the statement that is being edited
	 */
	/*
	@SuppressWarnings("unchecked")
	public static void saveContents(JPanel gridBagPanel, int statementID) {
		Component[] com = gridBagPanel.getComponents();
		
		for (int i = 0; i < com.length; i++) {
			Object content = null;      // the value of a variable, e.g., "EPA"
			String contentType = null;  // the variable name, e.g., "organization"
			if (com[i].getClass().getName().equals("javax.swing.JComboBox")) {  // short text
				contentType = ((JLabel)com[i-1]).getText();
				
				JComboBox<?> box = (JComboBox<?>) com[i];  // save the combo box
				String value = box.getEditor().getItem().toString();  // save its value as a string (no matter if it's a string or an attribute vector)
				int attributeId = Dna.data.getAttributeId((String) value, contentType, statementTypeId);  // look up if it's an attribute vector
				boolean newAttribute = false;
				if (attributeId == -1) {  // if not, create a new attribute vector and interpret as string below
					newAttribute = true;
					int id = Dna.data.generateNewId("attributes");
					int statementTypeId = Dna.data.getStatement(statementID).getStatementTypeId();
					AttributeVector attributeVector = new AttributeVector(id, (String) value, new Color(0, 0, 0), "", "", "", "", 
							statementTypeId, contentType);
					Dna.dna.addAttributeVector(attributeVector);
				}
				
				if (newAttribute == false) {
					try {
						content = ((AttributeVector) ((JComboBox<AttributeVector>) com[i]).getEditor().getItem()).getValue();
					} catch (java.lang.ClassCastException e) {  // attribute exists, but combo box only contains text for some weird reason
						content = value;
					}
				} else {
					content = value;
				}
				if (content == null) {
					content = "";
				}
				Dna.dna.updateVariable(statementId, statementTypeId, content, contentType);
			} else if (com[i].getClass().getName().equals("javax.swing.JCheckBox")) {  // boolean
				contentType = ((JLabel)com[i-1]).getText();
				content = ((JCheckBox)com[i]).isSelected();
				int intBool;
				if ((Boolean) content == false) {
					intBool = 0;
				} else {
					intBool = 1;
				}
				Dna.dna.updateVariable(statementId, statementTypeId, intBool, contentType);
			} else if (com[i].getClass().getName().equals("javax.swing.JScrollPane")) {  // long text
				contentType = ((JLabel)com[i-1]).getText();
				JScrollPane jsp = ((JScrollPane)com[i]);
				JTextArea jta = (JTextArea) jsp.getViewport().getView();
				content = jta.getText();
				if (content == null) {
					content = "";
				}
				Dna.dna.updateVariable(statementId, statementTypeId, content, contentType);
			} else if (com[i].getClass().getName().equals("javax.swing.JPanel")) {  // integer
				contentType = ((JLabel)com[i-1]).getText();
				JPanel jp = (JPanel) com[i];
				JSpinner jsp = (JSpinner) jp.getComponent(0);
				content = jsp.getValue();
				Dna.dna.updateVariable(statementId, statementTypeId, content, contentType);
			}
		}
	}
	*/
}
