package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

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

import dna.dataStructures.AttributeVector;
import dna.dataStructures.Statement;
import dna.renderer.AttributeComboBoxRenderer;

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
	
	public Popup(double X, double Y, int statementId, Point location, boolean editable) {
		this.X = X;
		this.Y = Y;
		Popup.statementId = statementId;
		this.los = location;
		this.textFieldWidth = Integer.parseInt(Dna.data.getSettings().get("popupWidth"));
		
		Statement statement = Dna.data.getStatement(statementId);
		this.color = Dna.data.getStatementColor(statementId);
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
				saveContents(gridBagPanel, statementId);
                dispose();
			}
		});
		
		ImageIcon addIcon = new ImageIcon(getClass().getResource("/icons/comment_edit.png"));
		this.setIconImage(addIcon.getImage());
		
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

		String type = Dna.data.getStatementTypeById(statementTypeId).getLabel();
		JLabel typeLabel = new JLabel(" " + type);
		
		JSeparator sep = new JSeparator();
		
		JPanel colorPanel = new JPanel();
		colorPanel.setBackground(color);
		colorPanel.setPreferredSize(new Dimension(4,4));
		
		ImageIcon duplicateIcon = new ImageIcon(getClass().getResource("/icons/add.png"));
		JButton duplicate = new JButton(duplicateIcon);
		duplicate.setToolTipText("create a copy of this statement at the same location");
		duplicate.setPreferredSize(new Dimension(16, 16));
		duplicate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveContents(gridBagPanel, statementId);
				int newId = Dna.data.generateNewId("statements");
				int documentId = Dna.data.getStatement(statementId).getDocumentId();
				int start = Dna.data.getStatement(statementId).getStart();
				int stop = Dna.data.getStatement(statementId).getStop();
				Date date = Dna.data.getStatement(statementId).getDate();
				int statementTypeId = Dna.data.getStatement(statementId).getStatementTypeId();
				int coder = Dna.data.getActiveCoder();
				LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
				Iterator<String> keyIterator = Dna.data.getStatement(statementId).getValues().keySet().iterator();
				while (keyIterator.hasNext()){
					String key = keyIterator.next();
					map.put(key, Dna.data.getStatement(statementId).getValues().get(key));
				}
				Statement newStatement = new Statement(newId, documentId, start, stop, date, statementTypeId, coder, map);
				Dna.dna.addStatement(newStatement);
				Dna.gui.documentPanel.documentTable.updateUI(); // for the "#" column
				Dna.gui.textPanel.selectStatement(newId, newStatement.getDocumentId(), true);
			}
		});
		
		ImageIcon removeIcon = new ImageIcon(getClass().getResource("/icons/trash.png"));
		JButton remove = new JButton(removeIcon);
		remove.setToolTipText("completely remove the whole statement (but keep the text)");
		remove.setPreferredSize(new Dimension(16, 16));
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Dna.gui, 
						"Are you sure you want to remove this statement?", 
						"Remove?", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					Dna.dna.removeStatement(statementId);
					Dna.gui.textPanel.paintStatements();
					Dna.gui.documentPanel.documentTable.updateUI(); // for the "#" column
					
                    // update links table after removal of statements
                    //Dna.dna.gui.rightPanel.updateViewLinksTable();
                    
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
		
		gbc.insets = new Insets(3,3,3,3);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		
		HashMap<String, String> variables = Dna.data.getStatementType(type).getVariables();
		
		Iterator<String> keyIterator = variables.keySet().iterator();
		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			String value = variables.get(key);
			JLabel label = new JLabel(key, JLabel.TRAILING);
			if (value.equals("short text")) {
				String val = (String) Dna.data.getStatement(statementId).getValues().get(key);
				int attributeIndex = Dna.data.getAttributeIndex(val, key, statementTypeId);
				AttributeVector entry = Dna.data.getAttributes().get(attributeIndex);
				AttributeVector[] entriesArray = Dna.data.getAttributes(key, statementTypeId);
				JComboBox<AttributeVector> box = new JComboBox<AttributeVector>(entriesArray);
				box.setRenderer(new AttributeComboBoxRenderer());
				box.setEditable(true);
				if (editable == true) {
					box.setEnabled(true);
				} else {
					box.setEnabled(false);
				}
    			box.setPreferredSize(new Dimension(this.textFieldWidth, 20));
    			box.setSelectedItem((AttributeVector)entry);
    			AutoCompleteDecorator.decorate(box);
    			
				gbc.anchor = GridBagConstraints.EAST;
	    		gridBagPanel.add(label, gbc);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.gridx++;
				gridBagPanel.add(box, gbc);
				gbc.gridx--;
				gbc.gridy++;
			} else if (value.equals("long text")) {
				String entry = (String) Dna.data.getStatement(statementId).getValues().get(key);
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
			} else if (value.equals("boolean")) {
				int entry = (Integer) Dna.data.getStatement(statementId).getValues().get(key);
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
			} else if (value.equals("integer")) {
				int entry = (Integer) Dna.data.getStatement(statementId).getValues().get(key);
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
				gbc.insets = new Insets(0,0,0,0);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.gridx++;
				gridBagPanel.add(jp, gbc);
				gbc.insets = new Insets(3,3,3,3);
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
}
