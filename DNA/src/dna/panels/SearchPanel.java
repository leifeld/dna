package dna.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import dna.Dna;


public class SearchPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	JTextField searchField;
	JButton searchNext, searchPrevious, searchRevert;
	ArrayList<Tupel> matches;
	
	public SearchPanel() {
		this.setLayout(new BorderLayout());
		searchField = new JTextField();
		String searchToolTip = 
				"<html>You can enter a regular expression in the search " +
				"<br/>field and click on the arrow buttons to go to the " +
				"<br/>previous or next occurrence in the text. This " +
				"<br/> works only in the current article. There is " +
				"another <br/> full-text search function for the whole " +
				"file in the menu.</html>";
		searchField.setToolTipText(searchToolTip);
		Icon searchPreviousIcon = new ImageIcon(getClass().getResource(
				"/icons/resultset_previous.png"));
		Icon searchNextIcon = new ImageIcon(getClass().getResource(
				"/icons/resultset_next.png"));
		Icon searchRevertIcon = new ImageIcon(getClass().getResource(
				"/icons/arrow_rotate_clockwise.png"));
		searchNext = new JButton(searchNextIcon);
		searchNext.setToolTipText(searchToolTip);
		searchPrevious = new JButton(searchPreviousIcon);
		searchPrevious.setToolTipText(searchToolTip);
		searchRevert = new JButton(searchRevertIcon);
		searchRevert.setToolTipText(searchToolTip);
		searchPrevious.setPreferredSize(new Dimension(18, 18));
		searchNext.setPreferredSize(new Dimension(18, 18));
		searchRevert.setPreferredSize(new Dimension(18, 18));
		JPanel searchButtonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gb = new GridBagConstraints();
		gb.insets = new Insets(0,0,0,0);
		gb.gridx = 0;
		gb.gridy = 0;
		searchButtonPanel.add(searchPrevious, gb);
		gb.gridx = 1;
		searchButtonPanel.add(searchNext, gb);
		gb.gridx = 2;
		searchButtonPanel.add(searchRevert, gb);
		this.add(searchField, BorderLayout.CENTER);
		this.add(searchButtonPanel, BorderLayout.EAST);
		
		searchRevert.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchField.setText("");
			}
		});
		
		matches = new ArrayList<Tupel>();
		
		ActionListener searchListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (e.getSource() == searchPrevious) {
						continueSearch(false);
					} else if (e.getSource() == searchNext) {
						continueSearch(true);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		
		searchPrevious.addActionListener(searchListener);
		searchNext.addActionListener(searchListener);
		
		KeyAdapter enter = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					try {
						continueSearch(true);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		};
		searchField.addKeyListener (enter);
	}

	public void continueSearch(boolean forward) {
		if (Dna.gui.documentPanel.documentContainer.getRowCount() == 0 || 
				Dna.gui.documentPanel.documentTable.getSelectedRow() == -1) {
			//there is no article where text could be found...
		} else {
			int matchStart = 0;
			int matchEnd = 0;
			
			//create list of matches in current article
			matches.clear();
			String term = searchField.getText();
			if (Dna.gui.documentPanel.documentTable.getSelectedRow() == -1) {
				Dna.gui.documentPanel.documentTable.changeSelection(0, 0, false, 
						false);
			}
			
			//int id = Dna.dna.gui.documentPanel.documentContainer.get(
			//		Dna.dna.gui.documentPanel.documentTable.getSelectedRow()).getId();
			
			String searchText = Dna.gui.textPanel.getDocumentText();
	    	//String searchText = new SqlQuery(dbfile).getArticleTextById(id);
	    	
			//searchText = stripHtmlTags(searchText, false);
			Pattern p = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(searchText);
			while (m.find()) {
				matches.add(new Tupel(m.start(), m.end()));
			}
			
			//determine cursor position from which to start searching
			int searchPosition;
			int selectionStart = Dna.gui.textPanel.getSelectionStart();
			int selectionEnd = Dna.gui.textPanel.getSelectionEnd();
			int selectionLength = selectionEnd - selectionStart;
			if (selectionLength > 0) {
				searchPosition = selectionStart;
			} else {
				if (Dna.gui.textPanel.getCaretPosition() > 0) {
					searchPosition = Dna.gui.textPanel.getCaretPosition();
				} else {
					searchPosition = 1;
				}
			}
			
			//search next or previous occurrence in the list
			if (forward == false) {
				for (int i = matches.size() - 1; i >= 0; i--) {
					if (matches.get(i).getEndValue() < searchPosition) {
						matchStart = matches.get(i).getStartValue();
						matchEnd = matches.get(i).getEndValue();
						break;
					}
					if (matches.size() > 0) {
						matchStart = matches.get(0).getStartValue();
						matchEnd = matches.get(0).getEndValue();
					}
				}
			} else if (forward == true) {
				for (int i = 0; i < matches.size(); i++) {
					if (matches.get(i).getStartValue() > searchPosition) {
						matchStart = matches.get(i).getStartValue();
						matchEnd = matches.get(i).getEndValue();
						break;
					}
					if (matches.size() > 0) {
						matchStart = matches.get(matches.size() - 1).
								getStartValue();
						matchEnd = matches.get(matches.size() - 1).
								getEndValue();
					}
				}
			}
			
			//select the match in the text window
			Dna.gui.textPanel.highlightText(matchStart, matchEnd);
		}
	}
	
	class Tupel {
		int startValue;
		int endValue;
		
		public Tupel(int startValue, int endValue) {
			this.startValue = startValue;
			this.endValue = endValue;
		}
		
		public int getStartValue() {
			return startValue;
		}
		
		public int getEndValue() {
			return endValue;
		}
	}
}
