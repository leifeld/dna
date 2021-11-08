package logger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTextArea;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;

import dna.Dna;
import gui.CoderBadgePanel;
import model.Coder;

/**
 * A dialog window with a table, showing log messages, warnings, and errors that
 * have accumulated in DNA during usage.
 */
public class LoggerDialog extends JDialog {
	private static final long serialVersionUID = -8365310356679647056L;
	private JTable table;
	private JTextArea summaryTextArea, detailsTextArea, logTextArea, exceptionTextArea;
	private HashMap<Integer, Coder> coders;

	/**
	 * Create a new instance of the logger dialog window.
	 */
	public LoggerDialog() {
		this.setModal(true);
		this.setTitle("Log entries");
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		// store coders to avoid re-accessing SQL in the table cell renderer
		ArrayList<Coder> coderList;
		if (Dna.sql.getActiveCoder() == null || Dna.sql.getConnectionProfile() == null) {
			coderList = new ArrayList<Coder>();
		} else {
			coderList = Dna.sql.getCoders();
		}
		coders = new HashMap<Integer, Coder>();
		for (int i = 0; i < coderList.size(); i++) {
			coders.put(coderList.get(i).getId(), coderList.get(i));
		}

		// upper panel with filters and buttons
		JPanel filterPanel = new JPanel(new BorderLayout());
		
		// regex filter panel, upper left corner
		JTextField filterField = new JTextField(25);
		filterField.setToolTipText("Display only those log events whose data match a regular expression.");
		JLabel filterLabel = new JLabel("Filter: ");
		filterLabel.setToolTipText("Display only those log events whose data match a regular expression.");
		filterLabel.setLabelFor(filterField);
		filterField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				Dna.logger.fireTableDataChanged();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				Dna.logger.fireTableDataChanged();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				Dna.logger.fireTableDataChanged();
			}
		});
		JPanel regexPanel = new JPanel();
		regexPanel.add(filterLabel);
		regexPanel.add(filterField);
		filterPanel.add(regexPanel, BorderLayout.WEST);
		
		// check box panel, upper middle part
		JCheckBox errorCheckBox = new JCheckBox("Errors", true);
		errorCheckBox.setToolTipText("Display log events with priority 3 (errors)?");
		errorCheckBox.setBackground(new Color(255, 130, 130));
		JCheckBox warningCheckBox = new JCheckBox("Warnings", true);
		warningCheckBox.setToolTipText("Display log events with priority 2 (warnings)?");
		warningCheckBox.setBackground(new Color(255, 255, 130));
		JCheckBox messageCheckBox = new JCheckBox("Messages", true);
		messageCheckBox.setToolTipText("Display log events with priority 1 (messages)?");
		messageCheckBox.setBackground(Color.BLACK);
		messageCheckBox.setForeground(Color.WHITE);
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Dna.logger.fireTableDataChanged();
			}
		};
		errorCheckBox.addActionListener(al);
		warningCheckBox.addActionListener(al);
		messageCheckBox.addActionListener(al);
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.add(errorCheckBox);
		checkBoxPanel.add(warningCheckBox);
		checkBoxPanel.add(messageCheckBox);
		filterPanel.add(checkBoxPanel, BorderLayout.CENTER);

		// clear filter button, upper right corner
		JPanel buttonPanel = new JPanel();
		ImageIcon resetIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-rotate-clockwise.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton resetButton = new JButton("Clear filter", resetIcon);
		resetButton.setToolTipText("Reset the filter settings and display all log events again.");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				filterField.setText("");
				errorCheckBox.setSelected(true);
				warningCheckBox.setSelected(true);
				messageCheckBox.setSelected(true);
				Dna.logger.fireTableDataChanged();
			}
		});
		buttonPanel.add(resetButton);
		
		// clear list button, upper right corner
		ImageIcon clearIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-backspace.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton clearButton = new JButton("Clear list", clearIcon);
		clearButton.setToolTipText("Delete all log events. You cannot get them back after clearing the table.");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Dna.logger.clear();
			}
		});
		buttonPanel.add(clearButton);
		
		// save to XML and JSON buttons, upper right corner
		ImageIcon xmlIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-code.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton xmlButton = new JButton("Save to XML", xmlIcon);
		xmlButton.setToolTipText("Save the filtered log events (i.e., those currently displayed in the table) to an XML file.");
		ImageIcon jsIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-brand-javascript.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton jsonButton = new JButton("Save to JSON", jsIcon);
		jsonButton.setToolTipText("Save the filtered log events (i.e., those currently displayed in the table) to a JSON file.");
		ActionListener saveButtonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String format;
				if (e.getSource().equals(jsonButton)) {
					format = "json";
				} else if (e.getSource().equals(xmlButton)) {
					format = "xml";
				} else {
					format = "";
					LogEvent l = new LogEvent(Logger.WARNING,
							"Could not determine file format for logging.",
							"Tried to save the message log to a file, but the file type is unknown.");
					Dna.logger.log(l);
				}
				String filename = null;
				File file = null;
				boolean validFileInput = false;
				while (!validFileInput) {
					JFileChooser fc;
					if (file == null) {
						fc = new JFileChooser();
					} else {
						fc = new JFileChooser(file);
					}
					fc.setDialogTitle("Save log as " + format.toUpperCase() + " file...");
					fc.setApproveButtonText("Save");
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith("." + format) || f.isDirectory();
						}
						public String getDescription() {
							return format.toUpperCase() + " log file (*." + format + ")";
						}
					});
					int returnVal = fc.showOpenDialog(null);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						file = fc.getSelectedFile();
						filename = new String(file.getPath());
						if (!filename.endsWith("." + format)) {
							filename = filename + "." + format;
						}
						file = new File(filename);
						if (!file.exists()) {
							validFileInput = true; // file approved
						} else {
							int dialog = JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Confirmation required", JOptionPane.YES_NO_OPTION);
							if (dialog == 0) {
								validFileInput = true;
								LogEvent l = new LogEvent(Logger.MESSAGE,
										"Overwriting existing " + format + " file.",
										"Overwriting existing file: " + file.getAbsolutePath() + ".");
								Dna.logger.log(l);
							} else {
								validFileInput = false;
								file = null;
							}
						}
					} else {
						validFileInput = true; // user must have clicked cancel in file chooser
						LogEvent l = new LogEvent(Logger.MESSAGE,
								"Saving the event log to a file was canceled.",
								"Saving the event log to a file was canceled by the user.");
						Dna.logger.log(l);
					}
				}
				if (file != null) {
					OutputLogEvent[] events = new OutputLogEvent[table.getRowCount()];
					for (int row = 0; row < table.getRowCount(); row++) {
						events[row] = new OutputLogEvent(Dna.logger.getRow(table.convertRowIndexToModel(row)));
					}
					OutputLog log = new OutputLog(Dna.version, Dna.date, events);
					String s = "";
					if (format.equals("xml")) {
						XStream xstream = new XStream();
						xstream.alias("log", OutputLog.class); // give more pleasant names to the XML tags
						xstream.alias("event", OutputLogEvent.class);
						s = xstream.toXML(log);
					} else if (format.equals("json")) {
						Gson prettyGson = new GsonBuilder()
					            .setPrettyPrinting()
					            .serializeNulls()
					            .disableHtmlEscaping()
					            .create();
						s = prettyGson.toJson(log);
					}
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
						writer.write(s);
						LogEvent l = new LogEvent(Logger.MESSAGE,
								"Log in " + format.toUpperCase() + " format was saved to disk.",
								"Log in " + format.toUpperCase() + " format was saved to file: " + file.getAbsolutePath() + ".");
						Dna.logger.log(l);
						JOptionPane.showMessageDialog(null,
								"The log was saved as:\n" + new File(filename).getAbsolutePath(),
								"Success",
							    JOptionPane.PLAIN_MESSAGE);
					} catch (IOException exception) {
						LogEvent l = new LogEvent(Logger.ERROR,
								"Log could not be saved to " + format.toUpperCase() + " file.",
								"Attempted to save the log events from the Logger to a " + format.toUpperCase() + " file. The file saving operation did not work, possibly because the file could not be written to disk or because the log could not be converted to " + format.toUpperCase() + " format.",
								exception);
						Dna.logger.log(l);
					}
				}
			}
		};
		xmlButton.addActionListener(saveButtonListener);
		jsonButton.addActionListener(saveButtonListener);
		buttonPanel.add(xmlButton);
		buttonPanel.add(jsonButton);
		
		// close button, upper right corner
		ImageIcon closeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		JButton closeButton = new JButton("Close", closeIcon);
		closeButton.setToolTipText("Close this window.");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		buttonPanel.add(closeButton);
		filterPanel.add(buttonPanel, BorderLayout.EAST);
		mainPanel.add(filterPanel, BorderLayout.NORTH);

		// log table
		table = new JTable(Dna.logger);
		TableRowSorter<Logger> sorter = new TableRowSorter<Logger>(Dna.logger);
		table.setRowSorter(sorter);
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(500);
		table.getColumnModel().getColumn(2).setPreferredWidth(150);
		table.getColumnModel().getColumn(3).setPreferredWidth(150);
		table.getColumnModel().getColumn(4).setPreferredWidth(150);
		table.getColumnModel().getColumn(5).setPreferredWidth(100);
		table.setDefaultRenderer(String.class, new LogTableCellRenderer());
		table.setDefaultRenderer(LocalDateTime.class, new LogTableCellRenderer());
		table.setDefaultRenderer(Integer.class, new LogTableCellRenderer());
		JScrollPane scroller = new JScrollPane(table);
		scroller.setViewportView(table);
		scroller.setPreferredSize(new Dimension(1200, 400));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mainPanel.add(scroller, BorderLayout.CENTER);
		
		RowFilter<Logger, Integer> logFilter = new RowFilter<Logger, Integer>() {
			public boolean include(Entry<? extends Logger, ? extends Integer> entry) {
				LogEvent l = Dna.logger.getRow(entry.getIdentifier());
				if (l.getPriority() == 3 && !errorCheckBox.isSelected()) {
					return false;
				}
				if (l.getPriority() == 2 && !warningCheckBox.isSelected()) {
					return false;
				}
				if (l.getPriority() == 1 && !messageCheckBox.isSelected()) {
					return false;
				}
				try {
					Pattern pattern = Pattern.compile(filterField.getText());
					Matcher matcherSummary = pattern.matcher(l.getSummary());
					Matcher matcherDetails = pattern.matcher(l.getDetails());
					Matcher matcherLogStack = pattern.matcher(l.getLogStackTraceString());
					Matcher matcherExceptionStack = pattern.matcher(l.getExceptionStackTraceString());
					if (filterField.getText().equals("")) {
						return true;
					} else if (matcherSummary.find()) {
						return true;
					} else if (matcherDetails.find()) {
						return true;
					} else if (matcherLogStack.find()) {
						return true;
					} else if (matcherExceptionStack.find()) {
						return true;
					} else {
						return false;
					}
				} catch(PatternSyntaxException pse) {
					return true;
				}
				
			}
		};
		sorter.setRowFilter(logFilter);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				int rowCount = table.getSelectedRowCount();
				if (rowCount > 0) {
					summaryTextArea.setEnabled(true);
					detailsTextArea.setEnabled(true);
					logTextArea.setEnabled(true);
					exceptionTextArea.setEnabled(true);
					LogEvent logEvent = Dna.logger.getRow(table.convertRowIndexToModel(table.getSelectedRow()));
					summaryTextArea.setText(logEvent.getSummary());
					detailsTextArea.setText(logEvent.getDetails());
					logTextArea.setText(logEvent.getLogStackTraceString());
					exceptionTextArea.setText(logEvent.getExceptionStackTraceString());
					detailsTextArea.setCaretPosition(0);
					logTextArea.setCaretPosition(0);
					exceptionTextArea.setCaretPosition(0);
				} else {
					summaryTextArea.setText("");
					detailsTextArea.setText("");
					logTextArea.setText("");
					exceptionTextArea.setText("");
					summaryTextArea.setEnabled(false);
					detailsTextArea.setEnabled(false);
					logTextArea.setEnabled(false);
					exceptionTextArea.setEnabled(false);
				}
			}
		});
		
		// details panel, bottom of the window
		JPanel detailsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.fill = GridBagConstraints.HORIZONTAL;
		g.insets = new Insets(3, 5, 3, 5);
		g.anchor = GridBagConstraints.NORTH;
		g.gridx = 0;
		g.gridy = 0;
		JLabel summaryLabel = new JLabel("Summary:", JLabel.TRAILING);
		summaryLabel.setToolTipText("The summary shows a short summary of the log event.");
		summaryTextArea = new JXTextArea();
		summaryTextArea.setToolTipText("The summary shows a short summary of the log event.");
		summaryTextArea.setLineWrap(true);
		summaryTextArea.setWrapStyleWord(true);
		summaryTextArea.setRows(1);
		summaryTextArea.setColumns(95);
		summaryTextArea.setEnabled(false);
		summaryTextArea.setEditable(false);
		JScrollPane summaryScroller = new JScrollPane(summaryTextArea);
		summaryScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		summaryLabel.setLabelFor(summaryTextArea);
		detailsPanel.add(summaryLabel, g);
		g.gridx = 1;
		detailsPanel.add(summaryScroller, g);
		
		g.gridx = 0;
		g.gridy = 1;
		JLabel detailsLabel = new JLabel("Details:", JLabel.TRAILING);
		detailsLabel.setToolTipText("The details field shows details of the log event.");
		detailsTextArea = new JXTextArea();
		detailsTextArea.setToolTipText("The details field shows details of the log event.");
		detailsTextArea.setLineWrap(true);
		detailsTextArea.setWrapStyleWord(true);
		detailsTextArea.setRows(4);
		detailsTextArea.setEnabled(false);
		detailsTextArea.setEditable(false);
		JScrollPane detailsScroller = new JScrollPane(detailsTextArea);
		detailsScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		detailsLabel.setLabelFor(detailsTextArea);
		detailsPanel.add(detailsLabel, g);
		g.gridx = 1;
		detailsPanel.add(detailsScroller, g);

		g.gridx = 0;
		g.gridy = 2;
		JLabel logLabel = new JLabel("Log stack trace:", JLabel.TRAILING);
		logLabel.setToolTipText("Show where in the class hierarchy the log event was triggered in the code.");
		logTextArea = new JXTextArea();
		logTextArea.setToolTipText("Show where in the class hierarchy the log event was triggered in the code.");
		logTextArea.setLineWrap(true);
		logTextArea.setWrapStyleWord(true);
		logTextArea.setRows(6);
		logTextArea.setEnabled(false);
		logTextArea.setEditable(false);
		JScrollPane logScroller = new JScrollPane(logTextArea);
		logScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		logLabel.setLabelFor(logTextArea);
		detailsPanel.add(logLabel, g);
		g.gridx = 1;
		detailsPanel.add(logScroller, g);

		g.gridx = 0;
		g.gridy = 3;
		JLabel exceptionLabel = new JLabel("Exception stack trace:", JLabel.TRAILING);
		exceptionLabel.setToolTipText("Show details of the error that was logged while executing the code.");
		exceptionTextArea = new JXTextArea();
		exceptionTextArea.setToolTipText("Show details of the error that was logged while executing the code.");
		exceptionTextArea.setLineWrap(true);
		exceptionTextArea.setWrapStyleWord(true);
		exceptionTextArea.setRows(6);
		exceptionTextArea.setEnabled(false);
		exceptionTextArea.setEditable(false);
		JScrollPane exceptionScroller = new JScrollPane(exceptionTextArea);
		exceptionScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		exceptionLabel.setLabelFor(exceptionTextArea);
		detailsPanel.add(exceptionLabel, g);
		g.gridx = 1;
		detailsPanel.add(exceptionScroller, g);
		mainPanel.add(detailsPanel, BorderLayout.SOUTH);

		this.add(mainPanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * Renderer for the table cells of the log table. Displays the contents of
	 * the table in appropriate ways.
	 */
	private class LogTableCellRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = 5607731678747286839L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
        	DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        	Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        	Logger model = (Logger) table.getModel();
	        LogEvent logEntry = model.getRow(table.convertRowIndexToModel(row));
	        
			Color priorityColor = javax.swing.UIManager.getColor("Table.background");
	        Color selectedColor = javax.swing.UIManager.getColor("Table.dropCellBackground");
	        if (logEntry.getPriority() == 1) {
	        	// priorityColor = new Color(130, 255, 130);
	        } else if (logEntry.getPriority() == 2) {
	        	priorityColor = new Color(255, 255, 130);
	        } else if (logEntry.getPriority() == 3) {
	        	priorityColor = new Color(255, 130, 130);
	        }
	        
        	if (column == 0) {
    			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss.SSS");
        		String s = ((LocalDateTime) value).format(formatter);
        		c = renderer.getTableCellRendererComponent(table, s, isSelected, hasFocus, row, column);
        	} else if (column == 5) {
        		if (Dna.sql.getConnectionProfile() == null || (int) value == -1 || coders.isEmpty() || !coders.containsKey((int) value)) {
        			value = null;
        		} else {
        			c = new CoderBadgePanel(coders.get((int) value), 14, 22);
        		}
        	}
        	if (isSelected == true) {
	        	c.setBackground(selectedColor);
	        } else {
	        	c.setBackground(priorityColor);
	        }
        	return c;
		}
	}
}