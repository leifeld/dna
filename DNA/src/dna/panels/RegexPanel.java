package dna.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTextField;

import dna.Dna;
import dna.dataStructures.Regex;
import dna.renderer.RegexListModel;
import dna.renderer.RegexListRenderer;

public class RegexPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public JButton colorButton;
	public JButton addButton;
	public JButton remove;
	JXTextField textField;
	public RegexListModel regexListModel;
	public JList<Regex> regexList;

	public RegexPanel() {			
		this.setLayout(new BorderLayout());
		regexListModel = new RegexListModel();
		regexList = new JList<Regex>(regexListModel);
		regexList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		regexList.setLayoutOrientation(JList.VERTICAL);
		regexList.setVisibleRowCount(10);
		regexList.setCellRenderer(new RegexListRenderer());
		JScrollPane listScroller = new JScrollPane(regexList);
		listScroller.setPreferredSize(new Dimension(50, 100));
		this.add(listScroller, BorderLayout.NORTH);	
		
		regexList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int ac = Dna.data.getActiveCoder();
				if (regexList.getModel().getSize() == 0 || regexList.isSelectionEmpty() 
						|| Dna.data.getCoderById(ac).getPermissions().get("editRegex") == false) {
					remove.setEnabled(false);
				} else {
					remove.setEnabled(true);
				}
			}
		});
		
		FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
		JPanel newFields = new JPanel(layout);
		
		textField = new JXTextField("(enter a regular expression)");
		textField.setPreferredSize(new Dimension(190, 18));
		layout.setHgap(0);
		newFields.add(textField);
		textField.setEnabled(false);
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
				String s = textField.getText();
				boolean duplicate = false;
				if (textField.getText().equals("")) {
					duplicate = true;
				} else {
					for (int i = 0; i < regexList.getModel().getSize(); i++) {
						if (((Regex) regexList.getModel().getElementAt(i)).getLabel().equals(s)) {
							duplicate = true;
						}
					}
				}
				if (duplicate == true) {
					addButton.setEnabled(false);
					if (!s.equals("")) {
						remove.setEnabled(true);
					}
				} else {
					addButton.setEnabled(true);
					remove.setEnabled(false);
				}
			}
		});
		

		@SuppressWarnings("serial")
		JButton colorButtonTemp = (new JButton() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(this.getForeground());
				g.fillRect(2, 2, 14, 14);
			}
		});
		colorButton = colorButtonTemp;
		colorButton.setForeground(Color.RED);
		colorButton.setPreferredSize(new Dimension(18, 18));
		colorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color actualColor = ((JButton)e.getSource()).getForeground();
				Color newColor = JColorChooser.showDialog(RegexPanel.this, "choose color...", actualColor);
				if (newColor != null) {
					((JButton) e.getSource()).setForeground(newColor);
				}
			}
		});
		newFields.add(Box.createRigidArea(new Dimension(5, 5)));
		newFields.add(colorButton);
		colorButton.setEnabled(false);

		JPanel buttons = new JPanel();
		GridLayout gl = new GridLayout(1, 2);
		gl.setHgap(3);
		buttons.setLayout(gl);

		Icon addIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		addButton = new JButton("add", addIcon);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color cl = colorButton.getForeground();
				int red = cl.getRed();
				int green = cl.getGreen();
				int blue = cl.getBlue();
				String text = textField.getText();
				Dna.dna.addRegex(new Regex(text, new Color(red, green, blue)));
				textField.setText("");
				Dna.gui.textPanel.paintStatements();
			}
		});
		addButton.setEnabled(false);
		buttons.add(addButton);

		Icon removeIcon = new ImageIcon(getClass().getResource("/icons/cross.png"));
		remove = new JButton("remove", removeIcon);
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String label = regexList.getSelectedValue().getLabel();
				Dna.dna.removeRegex(label);
				regexList.clearSelection();
				regexList.setSelectedIndex(0);
				Dna.gui.textPanel.paintStatements();
			}
		});
		buttons.add(remove);
		remove.setEnabled(false);
		
		this.add(newFields, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);	
	}

	public class ColorChooserDemo extends JPanel {
		private static final long serialVersionUID = 1L;
		JColorChooser chooser;
		public ColorChooserDemo() {
			super(new BorderLayout());
			chooser = new JColorChooser(Color.RED);
			add(chooser, BorderLayout.PAGE_END);
		}
	}
	
	public void setFieldsEnabled(boolean enabled) {
		textField.setEnabled(enabled);
		colorButton.setEnabled(enabled);
		remove.setEnabled(enabled);
		addButton.setEnabled(enabled);
	}
	
	public void clear() {
		textField.setText("");
		//listModel.removeAllElements();
	}
}