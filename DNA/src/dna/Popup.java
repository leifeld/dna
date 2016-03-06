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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

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

import dna.dataStructures.Statement;

public class Popup extends JDialog {
	
	private static final long serialVersionUID = 1L;
	Container c;
	Point point, los;
	String type;
	Color color;
	static int statementId;
	JPanel gridBagPanel;
	Connection conn;
	//Statement st6;
	
	public Popup(Point point, int statementId, Point location) {
		this.point = point;
		Popup.statementId = statementId;
		this.los = location;
		
		//final dna.dataStructures.Statement s = Dna.dna.db.getStatement(statementId);
		Statement statement = Dna.data.getStatement(statementId);
		this.color = statement.getColor();
		//this.type = Dna.dna.db.getStatementType(statementId);
		this.type = statement.getType();
		
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
				saveContents(gridBagPanel, statementId, type);
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
		JTextField startPos = new JTextField(new Integer(statement.getStart()).toString());
		startPos.setEditable(false);
		
		JLabel ePosLabel = new JLabel(" end:");
		JTextField endPos = new JTextField(new Integer(statement.getStop()).toString());
		endPos.setEditable(false);

		JLabel idLabel = new JLabel(" ID:");
		JTextField idField = 
				new JTextField(new Integer(statementId).toString());
		idField.setEditable(false);
		
		JLabel typeLabel = new JLabel(" " + type);
		
		JSeparator sep = new JSeparator();
		
		JPanel colorPanel = new JPanel();
		colorPanel.setBackground(color);
		colorPanel.setPreferredSize(new Dimension(4,4));
		
		ImageIcon duplicateIcon = new ImageIcon(getClass().getResource(
				"/icons/add.png"));
		JButton duplicate = new JButton(duplicateIcon);
		duplicate.setToolTipText("create a copy of this statement at the " +
				"same location");
		duplicate.setPreferredSize(new Dimension(16, 16));
		duplicate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Statement newStatement = Dna.data.getStatement(statementId);
				int newId = Dna.data.generateNewStatementId();
				newStatement.setId(newId);
				newStatement.setCoder(Integer.parseInt(Dna.data.getSettings().get("coder")));
				Dna.data.getStatements().add(newStatement);
				//int newStatementId = Dna.dna.db.duplicateStatement(statementId, s.getDocument(), s.getStart(), s.getStop());
				//Color color = Dna.dna.db.getStatementTypeColor(type);
				//Date date = Dna.dna.db.getDocument(s.getDocument()).getDate();
				//int coder = Dna.dna.db.getStatement(statementId).getCoder();
				//Statement st = new Statement(newStatementId, s.getDocument(), s.getStart(), s.getStop(), date, color, type, coder);
				//Dna.dna.gui.sidebarPanel.ssc.addStatement(st, true);
				Dna.dna.gui.textPanel.selectStatement(newId, newStatement.getDocument());
			}
		});
		
		ImageIcon removeIcon = new ImageIcon(getClass().getResource("/icons/trash.png"));
		JButton remove = new JButton(removeIcon);
		remove.setToolTipText(
				"completely remove the whole statement (but keep the text)");
		remove.setPreferredSize(new Dimension(16, 16));
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int question = JOptionPane.showConfirmDialog(Dna.dna.gui, 
						"Are you sure you want to remove this statement?", 
						"Remove?", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					Dna.data.removeStatement(statementId);
					//Dna.dna.removeStatement(statementId);
					Dna.dna.gui.textPanel.paintStatements();
					

                    // update links table after removal of statements
                    Dna.dna.gui.sidebarPanel.updateViewLinksTable();
                    
					dispose();
				}
			}
		});
		
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
		
		//HashMap<String, String> variables = Dna.dna.db.getVariables(type);
		HashMap<String, String> variables = Dna.data.getStatementType(type).getVariables();
		
		Iterator<String> keyIterator = variables.keySet().iterator();
		while (keyIterator.hasNext()){
			String key = keyIterator.next();
			String value = variables.get(key);
			JLabel label = new JLabel(key, JLabel.TRAILING);
			if (value.equals("short text")) {
				//String entry = Dna.dna.db.getVariableStringEntry(statementId, key);
				String entry = (String) Dna.data.getStatement(statementId).getValues().get(key);
				//String[] entries = Dna.dna.db.getVariableStringEntries(key, type);
				ArrayList<Statement> subset = Dna.data.getStatementsByType(type);
				ArrayList<String> entries = new ArrayList<String>();
				for (int i = 0; i < subset.size(); i++) {
					String mykey = (String) subset.get(i).getValues().get(key);
					if (!entries.contains(mykey)) {
						entries.add(mykey);
					}
				}
				JComboBox<String> box = new JComboBox<String>((String[]) entries.toArray());
				box.setEditable(true);
    			box.setPreferredSize(new Dimension(220, 20));
    			box.setSelectedItem((String)entry);
    			AutoCompleteDecorator.decorate(box);
    			
				gbc.anchor = GridBagConstraints.EAST;
	    		gridBagPanel.add(label, gbc);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.gridx++;
				gridBagPanel.add(box, gbc);
				gbc.gridx--;
				gbc.gridy++;
			} else if (value.equals("long text")) {
				//String entry = Dna.dna.db.getVariableStringEntry(statementId, key);
				String entry = (String) Dna.data.getStatement(statementId).getValues().get(key);
    			JTextArea box = new JTextArea();
    			box.setEditable(true);
    			box.setWrapStyleWord(true);
    			box.setLineWrap(true);
    			box.setText(entry);
    			JScrollPane boxScroller = new JScrollPane(box);
    			boxScroller.setPreferredSize(new Dimension(220, 100));
    			boxScroller.setVerticalScrollBarPolicy(
    					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    			
				gbc.anchor = GridBagConstraints.NORTHEAST;
	    		gridBagPanel.add(label, gbc);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.gridx++;
				gridBagPanel.add(boxScroller, gbc);
				gbc.gridx--;
				gbc.gridy++;
			} else if (value.equals("boolean")) {
				//int entry = Dna.dna.db.getVariableIntEntry(statementId, key);
				int entry = (Integer) Dna.data.getStatement(statementId).getValues().get(key);
				boolean val;
				if (entry == 0) {
					val = false;
				} else {
					val = true;
				}
				JCheckBox box = new JCheckBox();
    			box.setPreferredSize(new Dimension(20, 20));
    			box.setEnabled(true);
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
				//int entry = Dna.dna.db.getVariableIntEntry(statementId, key);
				int entry = (Integer) Dna.data.getStatement(statementId).getValues().get(key);
				JSpinner jsp = new JSpinner();
				jsp.setValue(entry);
    			jsp.setPreferredSize(new Dimension(70, 20));
    			jsp.setEnabled(true);
    			JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
    			jp.add(jsp);
    			
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
		
		double xDouble = los.getX() + point.getX();
		double yDouble = los.getY() + point.getY();
		int x = (int) xDouble + 6;
		int y = (int) yDouble + 13;
		this.setLocation(x, y);
		this.setVisible(true);
	}
	
	@SuppressWarnings("unchecked")
	public static void saveContents(JPanel gridBagPanel, int statementID, String type) {
		Component[] com = gridBagPanel.getComponents();
		//HashMap<String, String> vars = Dna.dna.db.getVariables(type);
		HashMap<String, String> vars = Dna.data.getStatementType(type).getVariables();
		
		for (int i = 0; i < com.length; i++) {
			Object content = null;
			String contentType = null;
			String dataType = null;
			if (com[i].getClass().getName().equals("javax.swing.JComboBox")) {
				contentType = ((JLabel)com[i-1]).getText();
				dataType = vars.get(contentType);
				content = ((JComboBox<String>) com[i]).getEditor().getItem();
				if (content == null) {
					content = "";
				}
				//Dna.dna.db.changeStatement(statementId, contentType, (String) content, dataType);
				Dna.data.getStatement(statementId).getValues().put(contentType, content);
			} else if (com[i].getClass().getName().equals(
					"javax.swing.JCheckBox")) {
				contentType = ((JLabel)com[i-1]).getText();
				dataType = vars.get(contentType);
				content = ((JCheckBox)com[i]).isSelected();
				int intBool;
				if ((Boolean) content == false) {
					intBool = 0;
				} else {
					intBool = 1;
				}
				//Dna.dna.db.changeStatement(statementId, contentType, intBool, dataType);
				Dna.data.getStatement(statementId).getValues().put(contentType, content);
			} else if (com[i].getClass().getName().equals(
					"javax.swing.JScrollPane")) {
				contentType = ((JLabel)com[i-1]).getText();
				dataType = vars.get(contentType);
				JScrollPane jsp = ((JScrollPane)com[i]);
				JTextArea jta = (JTextArea) jsp.getViewport().getView();
				content = jta.getText();
				if (content == null) {
					content = "";
				}
				//Dna.dna.db.changeStatement(statementId, contentType, (String) content, dataType);
				Dna.data.getStatement(statementId).getValues().put(contentType, content);
			} else if (com[i].getClass().getName().equals(
					"javax.swing.JPanel")) {
				contentType = ((JLabel)com[i-1]).getText();
				dataType = vars.get(contentType);
				JPanel jp = (JPanel) com[i];
				JSpinner jsp = (JSpinner) jp.getComponent(0);
				content = jsp.getValue();
				//Dna.dna.db.changeStatement(statementId, contentType, (Integer) content, dataType);
				Dna.data.getStatement(statementId).getValues().put(contentType, content);
			}
		}
		
	}
}
