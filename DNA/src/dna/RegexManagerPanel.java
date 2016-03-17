package dna;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
/*
 * TODO: Kommentar Philip: Wenn beim Regex-Highlighter ein Ausdruck eingegeben 
 * wird, der schon in der Liste existiert, oder wenn das Feld leer ist, sollte 
 * während dieser Zeit der Knopf zum Hinzufügen deaktiviert werden. Das kann man
 * mit einem DocumentListener machen. Du findest ein Beispiel in der Datei 
 * NewDocumentDialog.java ab Zeile 81.
 * => Momentan gelöst: man kann auf ok klicken, aber es werden nur Wörter hinzu-
 * gefügt, die sich von den bereits aufgelisteten unterscheiden.
 */

import dna.dataStructures.Regex;

public class RegexManagerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	JButton colorButton, addButton;
	JTextField textField;
	DefaultListModel<Regex> listModel;
	JList<Regex> regexList;

	public RegexManagerPanel() {			
		this.setLayout(new BorderLayout());
		//addWindowListener(new WindowAdapter() {
		//	public void windowClosing(WindowEvent e) {
		//  	Dna.dna.gui.textPanel.paintStatements();
		//	}	
		//});

		listModel = new DefaultListModel<Regex>();
		regexList = new JList<Regex>(listModel);
		regexList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		regexList.setLayoutOrientation(JList.VERTICAL);
		regexList.setVisibleRowCount(10);
		regexList.setCellRenderer(new RegexListRenderer());
		JScrollPane listScroller = new JScrollPane(regexList);
		listScroller.setPreferredSize(new Dimension(50, 100));

		this.add(listScroller, BorderLayout.NORTH);	

		JPanel newFields = new JPanel(new FlowLayout(FlowLayout.LEFT));

		textField = new JTextField(13);
		newFields.add(textField);

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
				Color newColor = JColorChooser.showDialog(RegexManagerPanel.
						this, "choose color...", actualColor);
				if (newColor != null) {
					((JButton) e.getSource()).setForeground(newColor);
				}

			}
		});
		newFields.add(colorButton);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));

		Icon addIcon = new ImageIcon(getClass().getResource("/icons/tick.png"));
		addButton = new JButton("add", addIcon);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color cl = colorButton.getForeground();
				int red = cl.getRed();
				int green = cl.getGreen();
				int blue = cl.getBlue();

				String text = textField.getText();

				if (text.length() > 0) {
					//LB.Comment: Cannot add an existing regex-term
					ArrayList<String> labels = new ArrayList<String>();
					//ArrayList<Regex> regex = Dna.dna.db.getRegex();
					for (int i = 0; i < Dna.data.getRegexes().size(); i++) {
						String label = Dna.data.getRegexes().get(i).getLabel();
						labels.add(label);
					}
					if (labels.contains(text)){
						//textField.setText("");
						//LB.Comment: better if word remains in text-window
					}else{
						//Dna.dna.db.addRegex(text, red, green, blue);
						Dna.data.getRegexes().add(new Regex(text, new Color(red, green, blue)));
						Regex r = new Regex(text, cl);
						listModel.addElement(r);
						textField.setText("");
					}
				}
				Dna.dna.gui.textPanel.paintStatements();
			}
		});
		addButton.setEnabled(false);
		buttons.add(addButton);

		Icon removeIcon = new ImageIcon(getClass().getResource("/icons/cross.png"));
		JButton remove = new JButton("remove", removeIcon);
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = regexList.getSelectedIndex();
				//String label = Dna.dna.db.getRegex().get(index).getLabel();
				//String label = Dna.data.getRegexes().get(index).getLabel();
				if (index >= 0) {
					//Dna.dna.db.removeRegex(label);
					Dna.data.getRegexes().remove(index);
					//listModel.remove(index);
					listModel.removeElementAt(index);
				}
				Dna.dna.gui.textPanel.paintStatements();
			}
		});
		buttons.add(remove);

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

	public void clear() {
		textField.setText("");
		listModel.removeAllElements();
	}
}