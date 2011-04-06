package dna;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class StatementFilter extends JPanel {
	
	JRadioButton showAll;
	JRadioButton showCurrent;
	JRadioButton showFilter;
	JTextField personField;
	JTextField organizationField;
	JTextField categoryField;
	JTextField agreementField;
	JTextField idField;
	JTextField textField;
	JLabel personLabel;
	JLabel organizationLabel;
	JLabel categoryLabel;
	JLabel agreementLabel;
	JLabel idLabel;
	JLabel textLabel;
	
	public StatementFilter() {
		
		this.setLayout(new BorderLayout());
		
		JPanel showPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ButtonGroup showGroup = new ButtonGroup();
		showAll = new JRadioButton("show all");
		showAll.setSelected(true);
		showCurrent = new JRadioButton("current");
		showFilter = new JRadioButton("filter:");
		showGroup.add(showAll);
		showGroup.add(showCurrent);
		showGroup.add(showFilter);
		showPanel.add(showAll);
		showPanel.add(showCurrent);
		showPanel.add(showFilter);
		
		JPanel personPanel = new JPanel(new BorderLayout());
		personLabel = new JLabel("person");
		personField = new JTextField(13);
		personPanel.add(personLabel, BorderLayout.WEST);
		personPanel.add(personField, BorderLayout.EAST);
		
		JPanel organizationPanel = new JPanel(new BorderLayout());
		organizationLabel = new JLabel("organization");
		organizationField = new JTextField(13);
		organizationPanel.add(organizationLabel, BorderLayout.WEST);
		organizationPanel.add(organizationField, BorderLayout.EAST);
		
		JPanel categoryPanel = new JPanel(new BorderLayout());
		categoryLabel = new JLabel("category");
		categoryField = new JTextField(13);
		categoryPanel.add(categoryLabel, BorderLayout.WEST);
		categoryPanel.add(categoryField, BorderLayout.EAST);
		
		JPanel agreementPanel = new JPanel(new BorderLayout());
		agreementLabel = new JLabel("agreement");
		agreementField = new JTextField(13);
		agreementPanel.add(agreementLabel, BorderLayout.WEST);
		agreementPanel.add(agreementField, BorderLayout.EAST);
		
		JPanel idPanel = new JPanel(new BorderLayout());
		idLabel = new JLabel("statement ID");
		idField = new JTextField(13);
		idPanel.add(idLabel, BorderLayout.WEST);
		idPanel.add(idField, BorderLayout.EAST);
		
		JPanel textPanel = new JPanel(new BorderLayout());
		textLabel = new JLabel("text");
		textField = new JTextField(13);
		textPanel.add(textLabel, BorderLayout.WEST);
		textPanel.add(textField, BorderLayout.EAST);
		
		toggleEnabled(false);
		
		JPanel fieldsPanel = new JPanel(new GridLayout(6,1));
		fieldsPanel.add(personPanel);
		fieldsPanel.add(organizationPanel);
		fieldsPanel.add(categoryPanel);
		fieldsPanel.add(agreementPanel);
		fieldsPanel.add(idPanel);
		fieldsPanel.add(textPanel);
		
		this.add(showPanel, BorderLayout.NORTH);
		this.add(fieldsPanel, BorderLayout.CENTER);
		
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == showAll) {
					toggleEnabled(false);
					allFilter();
				} else if (e.getSource() == showCurrent) {
					toggleEnabled(false);
					allFilter();
					articleFilter();
				} else if (e.getSource() == showFilter) {
					toggleEnabled(true);
					filter();
            	}
				if (!(e.getSource() == showAll)) {
					Dna.mainProgram.contradictionReporter.clearTree();
				}
			}
		};
		
		showAll.addActionListener(al);
		showCurrent.addActionListener(al);
		showFilter.addActionListener(al);
		
		DocumentListener dl = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            	applyFilter();
            }
            public void insertUpdate(DocumentEvent e) {
            	applyFilter();
            }
            public void removeUpdate(DocumentEvent e) {
            	applyFilter();
            }
            public void applyFilter() {
            	filter();
            }
        };
        
        personField.getDocument().addDocumentListener(dl);
        organizationField.getDocument().addDocumentListener(dl);
        categoryField.getDocument().addDocumentListener(dl);
        agreementField.getDocument().addDocumentListener(dl);
        idField.getDocument().addDocumentListener(dl);
        textField.getDocument().addDocumentListener(dl);
        
	}
	
	public void toggleEnabled(boolean enabled) {
		personField.setEnabled(enabled);
		organizationField.setEnabled(enabled);
		categoryField.setEnabled(enabled);
		agreementField.setEnabled(enabled);
		idField.setEnabled(enabled);
		textField.setEnabled(enabled);
		personLabel.setEnabled(enabled);
		organizationLabel.setEnabled(enabled);
		categoryLabel.setEnabled(enabled);
		agreementLabel.setEnabled(enabled);
		idLabel.setEnabled(enabled);
		textLabel.setEnabled(enabled);
	}

	public void allFilter() {
		try {
			RowFilter<StatementContainer, Object> rf = null;
    		rf = RowFilter.regexFilter("");
    		Dna.mainProgram.sorter.setRowFilter(rf);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	public void articleFilter() {
		int row = Dna.mainProgram.articleTable.getSelectedRow();
		String articleTitle = "";
		if (row > -1) {
			articleTitle = Dna.mainProgram.dc.ac.get(row).getTitle();
		}
		final String title = articleTitle;
		try {
			RowFilter<StatementContainer, Integer> articleFilter = new RowFilter<StatementContainer, Integer>() {
    			public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
    				StatementContainer stcont = entry.getModel();
    				Statement st = stcont.get(entry.getIdentifier());
    				if (st.getArticleTitle().equals(title)) {
        				return true;
        			}
    				return false;
    			}
    		};
    		Dna.mainProgram.sorter.setRowFilter(articleFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}

	private void filter() {
		final String p = personField.getText();
		final String o = organizationField.getText();
		final String c = categoryField.getText();
		final String a = agreementField.getText();
		final String i = idField.getText();
		final String t = textField.getText();
		try {
			RowFilter<StatementContainer, Integer> idFilter = new RowFilter<StatementContainer, Integer>() {
				public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
					StatementContainer stcont = entry.getModel();
					Statement st = stcont.get(entry.getIdentifier());
					
					Pattern pPattern = Pattern.compile(p);
					Matcher pMatcher = pPattern.matcher(st.getPerson());
					boolean pBoolean = pMatcher.find();
					
					Pattern oPattern = Pattern.compile(o);
					Matcher oMatcher = oPattern.matcher(st.getOrganization());
					boolean oBoolean = oMatcher.find();
					
					Pattern cPattern = Pattern.compile(c);
					Matcher cMatcher = cPattern.matcher(st.getCategory());
					boolean cBoolean = cMatcher.find();
					
					Pattern aPattern = Pattern.compile(a);
					Matcher aMatcher = aPattern.matcher(st.getAgreement());
					boolean aBoolean = aMatcher.find();
					
					Pattern iPattern = Pattern.compile(i);
					Matcher iMatcher = iPattern.matcher(new Integer(st.getId()).toString());
					boolean iBoolean = iMatcher.find();
					
					Pattern tPattern = Pattern.compile(t);
					Matcher tMatcher = tPattern.matcher(st.getText());
					boolean tBoolean = tMatcher.find();
					
					if (pBoolean == true && oBoolean == true && cBoolean == true && aBoolean == true && iBoolean == true && tBoolean == true) {
						return true;
					}
					return false;
				}
			};
			Dna.mainProgram.sorter.setRowFilter(idFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
}

/*
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class StatementFilter extends JPanel {
	
	JTextField textField;
	JPopupMenu popmen;
	JRadioButtonMenuItem textMenuItem, personMenuItem, organizationMenuItem, categoryMenuItem, agreementMenuItem, idMenuItem, allMenuItem, articleMenuItem;
	JButton filterButton;
	
	public StatementFilter() {
		
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		textField = new JTextField(18);
		textField.setEnabled(false);
		
		popmen = new JPopupMenu();
		allMenuItem = new JRadioButtonMenuItem("show all statements", true);
		articleMenuItem = new JRadioButtonMenuItem("show statements in current article");
		textMenuItem = new JRadioButtonMenuItem("filter by statement text");
		personMenuItem = new JRadioButtonMenuItem("filter by person");
		categoryMenuItem = new JRadioButtonMenuItem("filter by category");
		organizationMenuItem = new JRadioButtonMenuItem("filter by organization");
		idMenuItem = new JRadioButtonMenuItem("filter by statement ID");
		agreementMenuItem = new JRadioButtonMenuItem("filter by agreement");
		
		allMenuItem.setToolTipText("do not use a filter on the statement table");
		articleMenuItem.setToolTipText("filter the statement table by corresponding article title");
		textMenuItem.setToolTipText("use a regular expression as a statement content filter");
		personMenuItem.setToolTipText("use a regular expression to filter by person");
		organizationMenuItem.setToolTipText("use a regular expression to filter by organization");
		categoryMenuItem.setToolTipText("use a regular expression to filter by category");
		idMenuItem.setToolTipText("use a regular expression to filter by ID");
		agreementMenuItem.setToolTipText("use a regular expression to filter by agreement");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(textMenuItem);
		buttonGroup.add(personMenuItem);
		buttonGroup.add(organizationMenuItem);
		buttonGroup.add(idMenuItem);
		buttonGroup.add(categoryMenuItem);
		buttonGroup.add(agreementMenuItem);
		buttonGroup.add(allMenuItem);
		buttonGroup.add(articleMenuItem);
		
		popmen.add(allMenuItem);
		popmen.add(articleMenuItem);
		popmen.add(personMenuItem);
		popmen.add(organizationMenuItem);
		popmen.add(categoryMenuItem);
		popmen.add(agreementMenuItem);
		popmen.add(textMenuItem);
		popmen.add(idMenuItem);
		
		
		allMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textField.setText("");
			}
		});
		
		Icon filterIcon = new ImageIcon(getClass().getResource("/icons/bullet_wrench.png"));
		filterButton = new JButton(filterIcon);
		filterButton.setPreferredSize(new Dimension(18, 18));
		filterButton.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent me ) {
				mouseListenPopup(me);
			}
			public void mouseListenPopup(MouseEvent me) {
				popmen.show( me.getComponent(), me.getX(), me.getY() );
			}
		});

		DocumentListener dl = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            	selectFilter();
            }
            public void insertUpdate(DocumentEvent e) {
            	selectFilter();
            }
            public void removeUpdate(DocumentEvent e) {
            	selectFilter();
            }
            private void selectFilter() {
            	if (textMenuItem.isSelected()) {
            		textFilter(textField.getText());
            	} else if (personMenuItem.isSelected()) {
            		personFilter(textField.getText());
            	} else if (organizationMenuItem.isSelected()) {
            		organizationFilter(textField.getText());
            	} else if (categoryMenuItem.isSelected()) {
            		categoryFilter(textField.getText());
            	} else if (idMenuItem.isSelected()) {
            		idFilter(textField.getText());
            	} else if (agreementMenuItem.isSelected()) {
            		agreementFilter(textField.getText());
            	}
            }
        };
        
        textField.getDocument().addDocumentListener(dl);
        
        ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == allMenuItem) {
					textField.setText("");
					textField.setEnabled(false);
					allFilter();
				} else if (e.getSource() == articleMenuItem) {
					textField.setText("");
					textField.setEnabled(false);
					articleFilter();
				} else if (e.getSource() == textMenuItem) {
					textField.setEnabled(true);
            		textFilter(textField.getText());
            	} else if (e.getSource() == personMenuItem) {
            		textField.setEnabled(true);
            		personFilter(textField.getText());
            	} else if (e.getSource() == organizationMenuItem) {
            		textField.setEnabled(true);
            		organizationFilter(textField.getText());
            	} else if (e.getSource() == categoryMenuItem) {
            		textField.setEnabled(true);
            		categoryFilter(textField.getText());
            	} else if (e.getSource() == idMenuItem) {
            		textField.setEnabled(true);
            		idFilter(textField.getText());
            	} else if (e.getSource() == agreementMenuItem) {
            		textField.setEnabled(true);
            		agreementFilter(textField.getText());
            	}
				if (!(e.getSource() == allMenuItem)) {
					Dna.mainProgram.contradictionReporter.clearTree();
				}
			}
		};
		
		articleMenuItem.addActionListener(al);
		allMenuItem.addActionListener(al);
		textMenuItem.addActionListener(al);
		personMenuItem.addActionListener(al);
		organizationMenuItem.addActionListener(al);
		categoryMenuItem.addActionListener(al);
		idMenuItem.addActionListener(al);
		agreementMenuItem.addActionListener(al);
		
		this.add(filterButton);
		this.add(textField);
	}

	private void idFilter(String regx) {
		final String regex = regx;
		try {
			RowFilter<StatementContainer, Integer> idFilter = new RowFilter<StatementContainer, Integer>() {
				public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
					StatementContainer stcont = entry.getModel();
					Statement st = stcont.get(entry.getIdentifier());
					Pattern p = Pattern.compile(regex);
					Matcher m = p.matcher(new Integer(st.getId()).toString());
					boolean b = m.find();
					if (b == true) {
						return true;
					}
					return false;
				}
			};
			Dna.mainProgram.sorter.setRowFilter(idFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}

	private void agreementFilter(String regx) {
		final String regex = regx;
		try {
			RowFilter<StatementContainer, Integer> agreementFilter = new RowFilter<StatementContainer, Integer>() {
				public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
					StatementContainer stcont = entry.getModel();
					Statement st = stcont.get(entry.getIdentifier());
					Pattern p = Pattern.compile(regex);
					Matcher m = p.matcher(st.getAgreement());
					boolean b = m.find();
					if (b == true) {
						return true;
					}
					return false;
				}
			};
			Dna.mainProgram.sorter.setRowFilter(agreementFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	private void personFilter(String regx) {
		final String regex = regx;
		try {
			RowFilter<StatementContainer, Integer> personFilter = new RowFilter<StatementContainer, Integer>() {
				public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
					StatementContainer stcont = entry.getModel();
					Statement st = stcont.get(entry.getIdentifier());
					Pattern p = Pattern.compile(regex);
					Matcher m = p.matcher(st.getPerson());
					boolean b = m.find();
					if (b == true) {
						return true;
					}
					return false;
				}
			};
			Dna.mainProgram.sorter.setRowFilter(personFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	private void organizationFilter(String regx) {
		final String regex = regx;
		try {
			RowFilter<StatementContainer, Integer> organizationFilter = new RowFilter<StatementContainer, Integer>() {
    			public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
    				StatementContainer stcont = entry.getModel();
    				Statement st = stcont.get(entry.getIdentifier());
    				Pattern p = Pattern.compile(regex);
    				Matcher m = p.matcher(st.getOrganization());
    				boolean b = m.find();
    				if (b == true) {
    					return true;
    				}
    				return false;
    			}
    		};
    		Dna.mainProgram.sorter.setRowFilter(organizationFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	private void categoryFilter(String regx) {
		final String regex = regx;
		try {
			RowFilter<StatementContainer, Integer> categoryFilter = new RowFilter<StatementContainer, Integer>() {
    			public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
    				StatementContainer stcont = entry.getModel();
    				Statement st = stcont.get(entry.getIdentifier());
    				Pattern p = Pattern.compile(regex);
    				Matcher m = p.matcher(st.getCategory());
    				boolean b = m.find();
    				if (b == true) {
    					return true;
    				}
    				return false;
    			}
    		};
    		Dna.mainProgram.sorter.setRowFilter(categoryFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	public void textFilter(String regex) {
		try {
			RowFilter<StatementContainer, Object> rf = null;
    		rf = RowFilter.regexFilter(regex, 1);
    		Dna.mainProgram.sorter.setRowFilter(rf);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	public void articleFilter() {
		int row = Dna.mainProgram.articleTable.getSelectedRow();
		String articleTitle = "";
		if (row > -1) {
			articleTitle = Dna.mainProgram.dc.ac.get(row).getTitle();
		}
		final String title = articleTitle;
		try {
			RowFilter<StatementContainer, Integer> articleFilter = new RowFilter<StatementContainer, Integer>() {
    			public boolean include(Entry<? extends StatementContainer, ? extends Integer> entry) {
    				StatementContainer stcont = entry.getModel();
    				Statement st = stcont.get(entry.getIdentifier());
    				if (st.getArticleTitle().equals(title)) {
        				return true;
        			}
    				return false;
    			}
    		};
    		Dna.mainProgram.sorter.setRowFilter(articleFilter);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
	
	public void allFilter() {
		try {
			RowFilter<StatementContainer, Object> rf = null;
    		rf = RowFilter.regexFilter("");
    		Dna.mainProgram.sorter.setRowFilter(rf);
		} catch (java.util.regex.PatternSyntaxException pse) {
			return;
		}
	}
}
*/