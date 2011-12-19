package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * @author Philip Leifeld
 * 
 * This class represents an actor manager where the attributes of 
 * persons or organizations can be altered.
 * 
 * @date 2010-11-17
 */
/*
@SuppressWarnings("serial")
public class ActorManagerBackup extends JPanel {
	
	REListModel at;
	
	JTable actorTable;
	ActorContainer tableModel;
	JScrollPane resultScroller;
	JTextField newActor;
	JButton addButton, removeSelectedButton, removeAllButton;
	JComboBox comboBox;
	CustomTableSelectionListener ctsl;
	
	JButton colorButton, add, remove, change;
	JTextField textField;
	JList typeList;
	ListSelectionListener lsl;
	
	public ActorManagerBackup(final REListModel at) {
		
		this.at = at;
		this.setLayout(new BorderLayout());
		
		JPanel tablePanel = new JPanel(new BorderLayout());
		
		tableModel = new ActorContainer();
		actorTable = new JTable( tableModel );
		ctsl = new CustomTableSelectionListener();
		actorTable.getSelectionModel().addListSelectionListener(ctsl);
		TableModelListener tml = new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				typeList.removeListSelectionListener(lsl);
				typeList.clearSelection();
				remove.setEnabled(false);
				change.setEnabled(false);
				typeList.addListSelectionListener(lsl);
			}
		};
		tableModel.addTableModelListener(tml);
		
		comboBox = new JComboBox();
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		comboBox.setRenderer(renderer);
		
        TableColumn column = actorTable.getColumnModel().getColumn(1);
        column.setCellEditor(new DefaultCellEditor(comboBox));
        
        actorTable.setDefaultRenderer( RegexTerm.class, new RegexTableCellRenderer() );
		actorTable.setDefaultRenderer(Object.class, new ColorRenderer());
		
		actorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultScroller = new JScrollPane(actorTable);
		resultScroller.setPreferredSize(new Dimension(500, 165));
		actorTable.getTableHeader().setReorderingAllowed( false );
		
		tablePanel.add(resultScroller, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		newActor = new JTextField(13);
		buttonPanel.add(newActor);
		Icon addIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		addButton = new JButton("add", addIcon);
		addButton.setEnabled(false);
		buttonPanel.add(addButton);
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actorTable.getSelectionModel().removeListSelectionListener(ctsl);
				String o = newActor.getText();
				newActor.setText("");
				tableModel.addActor(new Actor(o, false));
				actorTable.getSelectionModel().addListSelectionListener(ctsl);
			}
		});
		
		//check whether the "add" field for organizations contains valid input and enable/disable the add button
		newActor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkButton();
			}
			private void checkButton() {
				String o = newActor.getText();
				boolean duplicate = false;
				if (newActor.getText().equals("")) {
					duplicate = true;
				} else {
					for (int i = 0; i < tableModel.actorList.size(); i++) {
						if (tableModel.actorList.get(i).getName().equals(o)) {
							duplicate = true;
						}
					}
				}
				if (duplicate == true) {
					addButton.setEnabled(false);
				} else {
					addButton.setEnabled(true);
				}
			}
		});
		
		Icon removeIcon = new ImageIcon(getClass().getResource("/icons/cross.png"));
		removeSelectedButton = new JButton("remove selected", removeIcon);
		removeSelectedButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actorTable.getSelectionModel().removeListSelectionListener(ctsl);
				int question = JOptionPane.showConfirmDialog(dna.Dna.mainProgram, "Are you sure you want to remove the \nselected actor from the attribute manager? \n(This will not affect any statements.)", 
						"Confirmation", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					int index = actorTable.getSelectedRow();
					if (index > -1 && ! tableModel.actorList.get(index).appearsInDataSet()) {
						tableModel.removeActor(tableModel.actorList.get(index).getName());
					}
					actorTable.validate();
					repaint();
				}
				actorTable.getSelectionModel().addListSelectionListener(ctsl);
				removeSelectedButton.setEnabled(false);
				actorTable.revalidate();
			}
		});
		removeSelectedButton.setEnabled(false);
		buttonPanel.add(removeSelectedButton);
		
		removeAllButton = new JButton("clean up...", removeIcon);
		removeAllButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actorTable.getSelectionModel().removeListSelectionListener(ctsl);
				int question = JOptionPane.showConfirmDialog(dna.Dna.mainProgram, "Are you sure you want to remove all \nactors from file which are not part of any \nstatement (i.e., the red rows in the table)?", 
						"Confirmation", JOptionPane.YES_NO_OPTION);
				if (question == 0) {
					for (int i = tableModel.actorList.size() - 1; i >= 0 ; i--) {
						if (!tableModel.actorList.get(i).appearsInDataSet()) {
							tableModel.actorList.remove(i);
						}
					}
					repaint();
				}
				actorTable.getSelectionModel().addListSelectionListener(ctsl);
				removeSelectedButton.setEnabled(false);
				actorTable.revalidate();
			}
		});
		buttonPanel.add(removeAllButton);
		
		int buttonHeight = (int) newActor.getPreferredSize().getHeight();
		int addWidth = (int) addButton.getPreferredSize().getWidth();
		int selectedWidth = (int) removeSelectedButton.getPreferredSize().getWidth();
		int allWidth = (int) removeAllButton.getPreferredSize().getWidth();
		addButton.setPreferredSize(new Dimension(addWidth, buttonHeight));
		removeSelectedButton.setPreferredSize(new Dimension(selectedWidth, buttonHeight));
		removeAllButton.setPreferredSize(new Dimension(allWidth, buttonHeight));
		
		tablePanel.add(buttonPanel, BorderLayout.SOUTH);
		this.add(tablePanel, BorderLayout.CENTER);
		
		
		JPanel tm = new JPanel();
		tm.setLayout(new BorderLayout());
		
		//DefaultListModel listModel = new RegexListModel();
		//typeList = new JList(listModel);
		typeList = new JList(at);
		typeList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		typeList.setLayoutOrientation(JList.VERTICAL);
		typeList.setVisibleRowCount(10);
		typeList.setCellRenderer(new RegexListRenderer());
		JScrollPane listScroller = new JScrollPane(typeList);
		listScroller.setPreferredSize(new Dimension(30, 126));
		
		//check whether the RegexTerm type list on the right changes its selection and adjust the buttons
		lsl = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				RegexTerm rt = (RegexTerm) typeList.getSelectedValue();
				String label = rt.getPattern();
				boolean exists = false;
				for (int i = 0; i < tableModel.actorList.size(); i++) {
					String tableString = tableModel.actorList.get(i).getType();
					if (tableString != null && tableString.equals(label)) {
						exists = true;
						break;
					}
				}
				if (exists == false) {
					remove.setEnabled(true);
				} else {
					remove.setEnabled(false);
				}
				if (typeList.getSelectedIndex() > -1) {
					change.setEnabled(true);
				}
			}
		};
		
		typeList.addListSelectionListener(lsl);
		
		tm.add(listScroller, BorderLayout.NORTH);
		
		JPanel newFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		textField = new JTextField(10);
		//check the text field for whether the add button should be enabled
		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkButton();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkButton();
			}
			public void checkButton() {
				boolean duplicate = false;
				if (textField.getText().equals("")) {
					duplicate = true;
				} else {
					for (int i = 0; i < at.size(); i++) {
						if (((RegexTerm) at.getElementAt(i)).getPattern().equals(textField.getText())) {
							duplicate = true;
						}
					}
				}
				if (duplicate == true) {
					add.setEnabled(false);
				} else {
					add.setEnabled(true);
				}
			}
		});
		newFields.add(textField);
		
		colorButton = (new JButton() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(this.getForeground());
                g.fillRect(2, 2, 14, 14);
            }
        });
		colorButton.setForeground(Color.RED);
		colorButton.setPreferredSize(new Dimension(18, 18));
		colorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color actualColor = Color.RED;
				Color newColor = JColorChooser.showDialog(Dna.mainProgram, "new color...", actualColor);
				if (newColor != null) {
					((JButton) e.getSource()).setForeground(newColor);
				}
			}
		});
		newFields.add(colorButton);
		
		JPanel buttons = new JPanel(new GridLayout(3,1));
		
		add = new JButton("add type", addIcon);
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				typeList.removeListSelectionListener(lsl);
				Color cl = colorButton.getForeground();
				String text = textField.getText();
				RegexTerm rt = new RegexTerm(text,cl);
				if (text.length() > 0 && ! ((REListModel) at).containsIdenticalRegexTerm(rt)) {
					at.addElement(rt);
				}
				typeList.addListSelectionListener(lsl);
				comboBox.addItem(rt);
				textField.setText("");
			}
		});
		add.setEnabled(false);
		buttons.add(add);
		int tmButtonWidth = (int) add.getPreferredSize().getWidth();
		int tmButtonHeight = (int) newActor.getPreferredSize().getHeight();
		add.setPreferredSize(new Dimension(tmButtonWidth, tmButtonHeight));
		
		Icon changeIcon = new ImageIcon(getClass().getResource("/icons/color_swatch.png"));
		change = new JButton("change...", changeIcon);
		change.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				typeList.removeListSelectionListener(lsl);
				
				int index = typeList.getSelectedIndex();
				if (index >= 0) {
					new ChangeType();
				}
				
				typeList.addListSelectionListener(lsl);
			}
		});
		change.setEnabled(false);
		buttons.add(change);
		change.setPreferredSize(new Dimension(tmButtonWidth, tmButtonHeight));
		
		remove = new JButton("remove type", removeIcon);
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				typeList.removeListSelectionListener(lsl);
				try{
					RegexTerm rt = (RegexTerm)typeList.getSelectedValue();
					at.removeElement(rt);
					comboBox.removeItem(rt);
				} catch (NullPointerException npe) {
					System.out.println("Object could not be fully removed.");
				}
				if (at.size() <= 0) {
					remove.setEnabled(false);
				}
				typeList.addListSelectionListener(lsl);
			}
		});
		remove.setEnabled(false);
		buttons.add(remove);
		remove.setPreferredSize(new Dimension(tmButtonWidth, tmButtonHeight));
		
		tm.add(newFields, BorderLayout.CENTER);
		tm.add(buttons, BorderLayout.SOUTH);
		
		this.add(tm, BorderLayout.EAST);
	}
	
	//clear everything; needed when a file is closed
	public void clear() {
		tableModel.actorList.clear();
		at.clear();
		((DefaultComboBoxModel) comboBox.getModel()).removeAllElements();
		textField.setText("");
		change.setEnabled(false);
		add.setEnabled(false);
		remove.setEnabled(false);
		repaint();
	}
	
	//add a new person or organization to the table
	public void add(Actor actor) {
		if (actor.getName() != null && ! actor.getName().equals("")) {
			tableModel.addActor(actor);
		}
	}
	
	//remove an actor from the table by its string name
	public void remove(String actor) {
		tableModel.removeActor(actor);
	}
	
	//get an actor from the table by its string name
	public Actor getActor(String actor) {
		return tableModel.getActor(actor);
	}
	
	//get the color of an actor by the string name of the actor
	public Color getColor(String actorName) {
		String type = "";
		for (int i = 0; i < tableModel.actorList.size(); i++) {
			if (tableModel.actorList.get(i).getName().equals(actorName)) {
				type = tableModel.actorList.get(i).getType();
				break;
			}
		}
		for (int i = 0; i < at.size(); i++) {
			RegexTerm rt = (RegexTerm) at.get(i);
			String pat = rt.getPattern();
			if (pat.equals(type)) {
				return rt.getColor();
			}
		}
		return Color.white; //return white if there is no other color
	}
	
	//does the table contain an actor whose string name is known?
	public boolean contains(String actorName) {
		for (int i = 0; i < tableModel.actorList.size(); i++) {
			if (tableModel.actorList.get(i).getName().equals(actorName)) {
				return true;
			}
		}
		return false;
	}
	
	//does the table contain a type whose string pattern is known?
	public boolean containsType(String typePattern) {
		for (int i = 0; i < getTypes().size(); i++) {
			if (getTypes().get(i).getPattern().equals(typePattern)) {
				return true;
			}
		}
		return false;
	}
	
	//access the list of actors from outside the class
	public ArrayList<Actor> getActors() {
		return tableModel.actorList;
	}
	
	//get a list of actor types as RegexTerm items
	public ArrayList<RegexTerm> getTypes() {
		ArrayList<RegexTerm> list = new ArrayList<RegexTerm>();
		for (int i = 0; i < at.size(); i++) {
			list.add((RegexTerm) at.get(i));
		}
		return list;
	}
	
	//add an actor type to the list
	public void addType(RegexTerm rt) {
		at.addElement((RegexTerm)rt);
		comboBox.addItem(rt);
		typeList.validate();
	}
	
	//get a string list of the actor names in the table
	public ArrayList<String> getActorNames() {
		ArrayList<String> o = new ArrayList<String>();
		for (int i = 0; i < tableModel.actorList.size(); i++) {
			o.add(tableModel.actorList.get(i).getName());
		}
		return o;
	}
	
	//correct the appearsInDataSet variable in all actors in the table by comparing the list with a given list of actors
	public void correctAppearance(ArrayList<String> actorNameList) {
		for (int i = 0; i < tableModel.actorList.size(); i++) {
			boolean b = false;
			for (int j = 0; j < actorNameList.size(); j++) {
				if (tableModel.actorList.get(i).getName().equals(actorNameList.get(j))) {
					b = true;
					break;
				}
			}
			if (b == true) {
				tableModel.actorList.get(i).setAppearsInDataSet(true);
			} else {
				tableModel.actorList.get(i).setAppearsInDataSet(false);
			}
		}
	}
	
	//export the actor table to a CSV file
	public void exportToCsv(String outfile) {
		try {
			System.out.println("Exporting actors and their attributes... ");
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			br.write("\"actor\";\"type\";\"alias/description\";\"note\";\"appears in dataset\"");
			for (int i = 0; i < tableModel.actorList.size(); i++) {
				String appears;
				if (tableModel.actorList.get(i).appearsInDataSet()) {
					appears = "yes";
				} else {
					appears = "no";
				}
				br.newLine();
				String replacedType = "";
				if (tableModel.actorList.get(i).getType() == null) {
					replacedType = "";
				} else {
					replacedType = tableModel.actorList.get(i).getType().replaceAll(";", ",");
				}
				br.write("\"" + tableModel.actorList.get(i).getName().replaceAll(";", ",") + "\"" + ";"
						+ "\"" + replacedType + "\"" + ";"
						+ "\"" + tableModel.actorList.get(i).getAlias().replaceAll(";", ",") + "\"" + ";"
						+ "\"" + tableModel.actorList.get(i).getNote().replaceAll(";", ",") + "\"" + ";"
						+ "\"" + appears + "\""
						);
			}
			br.close();
			System.out.println("File has been exported to \"" + outfile + "\".");
		} catch (IOException e) {
			System.err.println("Error while saving CSV file.");
		}
	}
	
	private class ColorRenderer extends DefaultTableCellRenderer {
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			Color colorSelected = new Color( 184, 207, 229 );
		    Color red = new Color(250, 175, 175);
		    Color redDark = new Color(200, 100, 100);
		    
		    if( isSelected && !ActorManager.this.getActors().get(row).appearsInDataSet()) {
				cell.setBackground( colorSelected );
					cell.setForeground( redDark );
			} else if (!isSelected && !ActorManager.this.getActors().get(row).appearsInDataSet()) {
				cell.setBackground( red );
					cell.setForeground( Color.black );
			} else if (isSelected && ActorManager.this.getActors().get(row).appearsInDataSet()) {
				cell.setBackground( colorSelected );
					cell.setForeground( Color.black );
			} else if (!isSelected && ActorManager.this.getActors().get(row).appearsInDataSet()) {
				cell.setBackground( Color.white );
					cell.setForeground( Color.black );
			}
		    
			return cell;
		}
	}
	
	private class RegexTableCellRenderer extends DefaultTableCellRenderer {
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        
	        if (value == null) {
	        	value = "";
	        }
	        
	        String pat = "";
	        Color col = Color.black;
	        for (int i = 0; i < at.size(); i++) {
	        	if (((RegexTerm) at.get(i)).getPattern().equals((String)value)) {
	        		col = ((RegexTerm) at.get(i)).getColor();
	        		pat = ((RegexTerm) at.get(i)).getPattern();
	        	}
	        }
	        
		    setText( pat );
		    
		    Color colorSelected = new Color( 184, 207, 229 );
		    Color red = new Color(250, 175, 175);
		    if(!isSelected && !ActorManager.this.getActors().get(row).appearsInDataSet()) {
				setBackground( red );
				setForeground( col );
			} else if(isSelected && !ActorManager.this.getActors().get(row).appearsInDataSet()) {
				setBackground( colorSelected );
				setForeground( col );
			} else if(isSelected && ActorManager.this.getActors().get(row).appearsInDataSet()) {
				setBackground( colorSelected );
				setForeground( col );
			} else if(!isSelected && ActorManager.this.getActors().get(row).appearsInDataSet()) {
				setBackground( Color.white );
				setForeground( col );
			}
		    
		    return this;
	    }
	}
	
	private class CustomTableSelectionListener implements ListSelectionListener {
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			int index = actorTable.getSelectedRow();
			if (index >= 0 && tableModel.actorList.get(index).appearsInDataSet()) {
				removeSelectedButton.setEnabled(false);
			} else {
				removeSelectedButton.setEnabled(true);
			}
		}
	}
	
	private class ComboBoxRenderer extends JLabel implements ListCellRenderer {
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			setOpaque(true);
			Color col;
			String pat;
			if (value == null) {
				col = Color.black;
				pat = "";
			} else {
				RegexTerm rt = (RegexTerm)value;
				col = rt.getColor();
				pat = rt.getPattern();
			}
			setForeground(col);
			setText(pat);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
			} else {
				setBackground(Color.white);
			}
			return this;
		}
	}
	
	public class ChangeType extends JFrame {
		
		Container c;
		RegexTerm rt;
		int index;
		JTextField titleField;
		JButton okButton, colorPicker;
		String pat;
		
		public ChangeType() {
			
			c = getContentPane();
			this.setTitle("Change type details");
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			ImageIcon changeIcon = new ImageIcon(getClass().getResource("/icons/color_swatch.png"));
			this.setIconImage(changeIcon.getImage());
			
			rt = (RegexTerm) typeList.getSelectedValue();
			index = typeList.getSelectedIndex();
			pat = rt.getPattern();
			
			titleField = new JTextField(10);
			titleField.setText(pat);
			titleField.selectAll();
			
			colorPicker = (new JButton() {
	            protected void paintComponent(Graphics g) {
	                super.paintComponent(g);
	                g.setColor(this.getForeground());
	                g.fillRect(2, 2, 14, 14);
	            }
	        });
			colorPicker.setForeground(rt.getColor());
			colorPicker.setPreferredSize(new Dimension(18, 18));
			colorPicker.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color newColor = JColorChooser.showDialog(Dna.mainProgram, "new color...", rt.getColor());
					if (newColor != null) {
						((JButton) e.getSource()).setForeground(newColor);
					}
				}
			});
			
			Icon okIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
			okButton = new JButton("OK", okIcon);
			okButton.setToolTipText( "rename the currently selected item and change its color" );
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					((RegexTerm) at.get(index)).setColor(colorPicker.getForeground());
					((RegexTerm) at.get(index)).setPattern(titleField.getText());
					
					((RegexTerm)comboBox.getModel().getElementAt(index)).setColor(colorPicker.getForeground());
					((RegexTerm)comboBox.getModel().getElementAt(index)).setPattern(titleField.getText());
					ActorManager.this.validate();
					ActorManager.this.repaint();
					comboBox.validate();
					comboBox.repaint();
					
					for (int i = 0; i < tableModel.getRowCount(); i++) {
						String tab = (String) tableModel.getValueAt(i, 1);
						if (tab.equals(pat)) {
							RegexTerm nrt = new RegexTerm(titleField.getText(), colorPicker.getForeground());
							tableModel.setValueAt(nrt, i, 1);
						}
					}
					dispose();
				}
			});
			
			//check whether the name already exists etc.
			titleField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkButton();
				}
				public void checkButton() {
					String title = titleField.getText();
					boolean duplicate = false;
					if (titleField.getText().equals("")) {
						duplicate = true;
					} else {
						for (int i = 0; i < getTypes().size(); i++) {
							if (getTypes().get(i).getPattern().equals(title)) {
								duplicate = true;
							}
						}
					}
					if (duplicate == true) {
						okButton.setEnabled(false);
					} else {
						okButton.setEnabled(true);
					}
				}
			});
			
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(titleField);
			panel.add(colorPicker);
			panel.add(okButton);
			c.add(panel);
			
			this.pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}
	}
	
	public class REListModel extends DefaultListModel {
		public boolean containsIdenticalRegexTerm(RegexTerm rt) {
			for (int i = 0; i < size(); i++) {
				if ( ((RegexTerm)getElementAt(i)).equals(rt) ) {
					return true;
				}
			}
			return false;
		}
	}

	public class ActorTypeListRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,	int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setText((String)value);
			try {
				Class.forName("org.h2.Driver");
				Connection db = DriverManager.getConnection("jdbc:h2:" + dna.Dna.mainProgram.cf, 
						"sa", "");
				Statement s = db.createStatement();
				ResultSet rs = s.executeQuery("SELECT * FROM REGEXES WHERE REGEX = '" + (String)value + "'");
				rs.next();
				int red = rs.getInt("RED");
				int green = rs.getInt("GREEN");
				int blue = rs.getInt("BLUE");
				Color cl = new Color(red, green, blue);
				label.setForeground(cl);
				s.close();
				db.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			return label;
		}
	}
}
*/