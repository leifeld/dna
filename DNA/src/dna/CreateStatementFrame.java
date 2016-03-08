/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import dna.dataStructures.Document;
import dna.dataStructures.Statement;
import dna.dataStructures.StatementType;

/**
 *
 * @author Shraddha
 */
public class CreateStatementFrame extends JFrame {

    double WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    double HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    private static  CreateStatementFrame createStatementFrame;
    Dimension minSize = new Dimension((int) WIDTH / 2, (int) HEIGHT / 3);

    JPanel gridBagPanel;
    StatementType statementType;
    
    public CreateStatementFrame(StatementType type, Color typeColor, int documentId, 
            int selectionStart, int selectionEnd,  String text) {
        super();
        createStatementFrame = this;
        this.setTitle(" Create new " + type);
        this.setAlwaysOnTop(true);
        this.setResizable(false);
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        statementType = type;
        JPanel titleDecorationPanel = new JPanel(new BorderLayout());
        
        JLabel typeLabel = new JLabel(" Create new " + type);
//        textLabel.setBackground(typeColor);
        
        JSeparator sep = new JSeparator();
        JPanel colorPanel = new JPanel();
        colorPanel.setBackground(typeColor);
        colorPanel.setMaximumSize(new Dimension(8,4));
        colorPanel.setPreferredSize(new Dimension(8,4));
                
                
                titleDecorationPanel.add(typeLabel, BorderLayout.CENTER);
		titleDecorationPanel.add(sep, BorderLayout.SOUTH);
		titleDecorationPanel.add(colorPanel, BorderLayout.WEST);
                
         gridBagPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
//        gbc.anchor = GridBagConstraints.EAST;
//                gridBagPanel.add(colorbox, gbc);
//                gbc.insets = new Insets(0, 0, 0, 0);
//                gbc.anchor = GridBagConstraints.WEST;
//                gbc.gridx++;
//                gridBagPanel.add(textLabel, gbc);
//                gbc.insets = new Insets(3, 3, 3, 3);
//                gbc.gridx--;
//                gbc.gridy++;

        //LinkedHashMap<String, String> variables = Dna.dna.db.getVariables(type);
        LinkedHashMap<String, String> variables = type.getVariables();

        Iterator<String> keyIterator = variables.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            String value = variables.get(key);
            JLabel label = new JLabel(key, JLabel.TRAILING);
            
            if (value.equals("short text")) {
//				String entry = Dna.dna.db.getVariableStringEntry(statementId, 	key);
                //String[] entries = Dna.dna.db.getVariableStringEntries(key, type);
            	ArrayList<String> entries = new ArrayList<String>();
        		ArrayList<Statement> statements = Dna.data.getStatementsByStatementTypeId(type.getId());
        		for (int i = 0; i < statements.size(); i++) {
        			String a = (String) Dna.data.getStatements().get(i).getValues().get(key);
        			if (!entries.contains(a)) {
        				entries.add(a);
        			}
        		}
        		String[] entriesArray = new String[entries.size()];
        		entries.toArray(entriesArray);
                JComboBox<String> box = new JComboBox<String>(entriesArray);
                box.setEditable(true);
                box.setPreferredSize(new Dimension(220, 20));
//    			box.setSelectedItem((String)entry);
                AutoCompleteDecorator.decorate(box);
                label.setLabelFor(box);
                
                gbc.anchor = GridBagConstraints.EAST;
                gridBagPanel.add(label, gbc);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.gridx++;
                gridBagPanel.add(box, gbc);
                gbc.gridx--;
                gbc.gridy++;
            } else if (value.equals("long text")) {

                JTextArea box = new JTextArea();
                box.setEditable(true);
                box.setWrapStyleWord(true);
                box.setLineWrap(true);
                
                label.setLabelFor(box);
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

                JCheckBox box = new JCheckBox();
                box.setPreferredSize(new Dimension(20, 20));
                box.setEnabled(true);

                label.setLabelFor(box);
                gbc.anchor = GridBagConstraints.EAST;
                gridBagPanel.add(label, gbc);
                gbc.insets = new Insets(0, 0, 0, 0);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.gridx++;
                gridBagPanel.add(box, gbc);
                gbc.insets = new Insets(3, 3, 3, 3);
                gbc.gridx--;
                gbc.gridy++;
            } else if (value.equals("integer")) {
//				int entry = Dna.dna.db.getVariableIntEntry(statementId, key);
                JSpinner jsp = new JSpinner();
//				jsp.setValue(entry);
                jsp.setPreferredSize(new Dimension(70, 20));
                jsp.setEnabled(true);
                JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
                jp.add(jsp);

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

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 50, 10));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                    createStatementFrame.dispose();
            }
        });
               
        JButton saveBtn = new JButton("Save");
        saveBtn.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                
                LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
                LinkedHashMap<String, String> vars = type.getVariables();
                Iterator<String> keyIterator = vars.keySet().iterator();
				while (keyIterator.hasNext()){	
					String key = keyIterator.next();
					if (vars.get(key).equals("long text") || vars.get(key).equals("short text")) {
						values.put(key, "");
					} else if (vars.get(key).equals("boolean") || vars.get(key).equals("integer")) {
						values.put(key, 0);
					}
				}
				int id = Dna.data.generateNewStatementId();
				Document document = Dna.data.getDocument(documentId);
				Date date = document.getDate();
				Color color = type.getColor();
				int coder = Integer.parseInt(Dna.data.getSettings().get("currentCoder"));
				Statement statement = new Statement(id, documentId, selectionStart, selectionEnd, date, color, type.getId(), coder, values);
				Dna.data.getStatements().add(statement);
                
                //Dna.dna.addStatement(type, documentId, selectionStart, selectionEnd);
                //int statementId = Dna.dna.db.getStatementId(type, documentId, selectionStart, selectionEnd);
				//System.out.println("statement added id: " + statementId );
                
                Component[] com = gridBagPanel.getComponents();
                //HashMap<String, String> vars = Dna.dna.db.getVariables(type);
                for (int i = 1; i < com.length; i++) {
                	Object content = null;
                	String contentType = null;
                	//String dataType = null;
                	if (com[i].getClass().getName().equals("javax.swing.JComboBox")) {
                		contentType = ((JLabel)com[i-1]).getText();
                		//dataType = vars.get(contentType);
                		content = ((JComboBox<String>) com[i]).getEditor().getItem();
                		if (content == null) {
                			content = "";
                		}
                		Dna.data.getStatement(id).getValues().put(contentType, content);
                		//Dna.dna.db.changeStatement(statementId, contentType, (String) content, dataType);
                	} else if (com[i].getClass().getName().equals("javax.swing.JCheckBox")) {
                		contentType = ((JLabel)com[i-1]).getText();
                		//dataType = vars.get(contentType);
                		content = ((JCheckBox)com[i]).isSelected();
                		int intBool;
                		if ((Boolean) content == false) {
                			intBool = 0;
                		} else {
                			intBool = 1;
                		}
                		//Dna.dna.db.changeStatement(statementId, contentType, intBool, dataType);
                		Dna.data.getStatement(id).getValues().put(contentType, intBool);
                	} else if (com[i].getClass().getName().equals("javax.swing.JScrollPane")) {
                		contentType = ((JLabel)com[i-1]).getText();
                		//dataType = vars.get(contentType);
                		JScrollPane jsp = ((JScrollPane)com[i]);
                		JTextArea jta = (JTextArea) jsp.getViewport().getView();
                		content = jta.getText();
                		if (content == null) {
                			content = "";
                		}
                		//Dna.dna.db.changeStatement(statementId, contentType, (String) content, dataType);
                		Dna.data.getStatement(id).getValues().put(contentType, content);
                	} else if (com[i].getClass().getName().equals("javax.swing.JPanel")) {
                		contentType = ((JLabel)com[i-1]).getText();
                		//dataType = vars.get(contentType);
                		JPanel jp = (JPanel) com[i];
                		JSpinner jsp = (JSpinner) jp.getComponent(0);
                		content = jsp.getValue();
                		//Dna.dna.db.changeStatement(statementId, contentType, (Integer) content, dataType);
                		Dna.data.getStatement(id).getValues().put(contentType, (Integer) content);
                	}
                }
                 
//                Popup.saveContents(gridBagPanel, statementId, statementType); //causing error, makes slow
                dispose();
                Dna.dna.gui.textPanel.paintStatements();
                Dna.dna.gui.textPanel.updateUI();
            }
        });

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        
        JPanel mainpPanel = new JPanel(new BorderLayout());
        titleDecorationPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        gridBagPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        buttonPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        mainpPanel.setBorder(new LineBorder(Color.black));
        mainpPanel.add(titleDecorationPanel, BorderLayout.NORTH);
        mainpPanel.add(gridBagPanel, BorderLayout.CENTER);
        mainpPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.setContentPane(mainpPanel);
        this.setLocation((int)(WIDTH/4 ), (int)(HEIGHT/4));
        pack();
    }
}