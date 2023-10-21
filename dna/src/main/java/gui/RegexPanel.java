package gui;

import dna.Dna;
import model.Regex;
import org.jdesktop.swingx.JXTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class RegexPanel extends JPanel {
    private JButton addButton;
    private JButton removeButton;
    private ColorButton colorButton;
    private JXTextField textField;
    private RegexListModel regexListModel;
    private JList<Regex> regexList;

    public RegexPanel() {
        createLayout();
    }

    private void createLayout() {
        this.setLayout(new BorderLayout());

        // list
        regexListModel = new RegexListModel();
        regexList = new JList<>(regexListModel);
        regexList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        regexList.setLayoutOrientation(JList.VERTICAL);
        //regexList.setVisibleRowCount(20);
        regexList.setCellRenderer(new RegexListRenderer());
        regexList.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        regexList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                removeButton.setEnabled(regexList.getModel().getSize() != 0 && !regexList.isSelectionEmpty() && Dna.sql.getActiveCoder().isPermissionEditRegex());
            }
        });
        JScrollPane listScroller = new JScrollPane(regexList);
        listScroller.setBorder(new EmptyBorder(5, 7, 5, 5));
        this.add(listScroller, BorderLayout.CENTER);

        // entry panel: color button
        JPanel entryPanel = new JPanel(new BorderLayout(5, 0));
        entryPanel.setBorder(new EmptyBorder(0, 7, 5, 5));
        colorButton = new ColorButton();
        colorButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        colorButton.setColor(new model.Color(255, 0, 0));
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.Color currentColor = colorButton.getColor();
                Color newColor = JColorChooser.showDialog(RegexPanel.this, "Choose color...", currentColor.toAWTColor());
                if (newColor != null) {
                    colorButton.setColor(new model.Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
                }
            }
        });
        this.colorButton.setEnabled(false);
        entryPanel.add(colorButton, BorderLayout.WEST);

        // entry panel: text field
        textField = new JXTextField(); // "enter new regex entry here"
        textField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
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
                        if (regexList.getModel().getElementAt(i).getLabel().equals(s)) {
                            duplicate = true;
                        }
                    }
                }
                boolean connection = Dna.sql != null && Dna.sql.getActiveCoder() != null;
                if (duplicate) {
                    addButton.setEnabled(false);
                    if (!s.equals("") && connection) {
                        removeButton.setEnabled(true);
                    } else {
                        removeButton.setEnabled(false);
                    }
                } else {
                    addButton.setEnabled(connection);
                    removeButton.setEnabled(false);
                }
            }
        });
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                regexList.clearSelection();
            }
            @Override
            public void focusLost(FocusEvent e) {
                // nothing to do
            }
        });
        this.textField.setEnabled(false);
        entryPanel.add(textField, BorderLayout.CENTER);
        colorButton.setPreferredSize(new Dimension(colorButton.getPreferredSize().width, textField.getPreferredSize().height));

        // button panel: add button
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2));
        buttonPanel.setBorder(new EmptyBorder(0, 7, 5, 5)); // border left and right
        ImageIcon addIcon = new SvgIcon("/icons/tabler_plus.svg", 16).getImageIcon();
        addButton = new JButton("Add", addIcon);
        addButton.setEnabled(false);
        buttonPanel.add(addButton);

        // button panel: remove button
        ImageIcon removeIcon = new SvgIcon("/icons/tabler_minus.svg", 16).getImageIcon();
        removeButton = new JButton("Remove", removeIcon);
        removeButton.setEnabled(false);
        buttonPanel.add(removeButton);

        // lower panel for text field and buttons
        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.add(entryPanel, BorderLayout.NORTH);
        lowerPanel.add(buttonPanel, BorderLayout.SOUTH);
        this.add(lowerPanel, BorderLayout.SOUTH);
    }

    JButton getAddButton() {
        return this.addButton;
    }

    JButton getRemoveButton() {
        return this.removeButton;
    }

    void removeRegexes() {
        int[] selected = regexList.getSelectedIndices();
        ArrayList<String> labels = IntStream.of(selected).mapToObj(i -> regexListModel.getElementAt(i).getLabel()).collect(Collectors.toCollection(ArrayList::new));
        boolean deleted = Dna.sql.deleteRegexes(labels);
        if (deleted) {
            regexListModel.removeRegexes(labels);
            if (regexList.getVisibleRowCount() > 0) {
                regexList.setSelectedIndex(0);
            }
            regexList.updateUI();
            regexList.validate();
            regexList.invalidate();
            regexList.repaint();
        }
    }

    void addRegex() {
        model.Color cl = colorButton.getColor();
        int red = cl.getRed();
        int green = cl.getGreen();
        int blue = cl.getBlue();
        String text = textField.getText();
        boolean added = Dna.sql.addRegex(text, red, green, blue);
        if (added) {
            Regex regex = new Regex(text, cl);
            regexListModel.addElement(regex);
            textField.setText("");
            colorButton.setForeground(Color.RED);
        }
    }

    public void refresh() {
        this.textField.setText("");
        this.addButton.setEnabled(false);
        this.removeButton.setEnabled(false);
        if (Dna.sql != null && Dna.sql.getActiveCoder() != null) {
            regexListModel.setRegexItems(Dna.sql.getRegexes()); // load from database
            if (Dna.sql.getActiveCoder().isPermissionEditRegex()) {
                this.textField.setEnabled(true);
                this.colorButton.setEnabled(true);
            } else {
                this.textField.setEnabled(false);
                this.colorButton.setEnabled(false);
            }
        } else {
            regexListModel.clear();
            this.textField.setEnabled(false);
            this.colorButton.setEnabled(false);
        }
    }

    /**
     * A list renderer for displaying {@link Regex} objects as list items.
     */
    private static class RegexListRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = -2591524720728319393L;

        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText(((Regex)value).getLabel());
            label.setForeground(((Regex)value).getColor().toAWTColor());
            label.setOpaque(true);
            return label;
        }
    }

    /**
     * A list model for regular expressions.
     */
    private static class RegexListModel extends AbstractListModel<Regex> {
        private static final long serialVersionUID = 1232262685157283074L;
        ArrayList<Regex> regex;

        /**
         * Create a new regex list model.
         */
        public RegexListModel() {
            regex = new ArrayList<Regex>();
        }

        /**
         * Replace the current array list of regexes by a new array list.
         *
         * @param regexItems An array list of {@link Regex} objects.
         */
        public void setRegexItems(ArrayList<Regex> regexItems) {
            int index = Math.max(regex.size(), regexItems.size());
            index = Math.max(index, 1);
            regex = regexItems;
            Collections.sort(regex);
            this.fireContentsChanged(this, 0, index - 1);
        }

        /**
         * Add a {@link Regex} object to the list.
         *
         * @param newRegex A {@link Regex} object.
         */
        public void addElement(Regex newRegex) {
            int newIndex = -1;
            if (regex.size() > 0) {
                for (int i = 0; i < regex.size(); i++) {
                    if (newRegex.compareTo(regex.get(i)) < 0) {
                        newIndex = i;
                        break;
                    }
                }
            } else {
                newIndex = 0;
            }
            if (newIndex == -1) { // there were other regexes, but the new regex comes later than all of them
                newIndex = regex.size();
            }
            regex.add(newIndex, newRegex);
            fireIntervalAdded(this, newIndex, newIndex);
        }

        /**
         * Delete multiple regular expressions identified by their labels and notify the listeners.
         *
         * @param labels The labels of the regexes to remove.
         */
        public void removeRegexes(ArrayList<String> labels) {
            int firstIndex = labels.size();
            int lastIndex = -1;
            for (String label : labels) {
                for (int i = 0; i < this.regex.size(); i++) {
                    if (this.regex.get(i).getLabel().equals(label)) {
                        this.regex.remove(i);
                        firstIndex = Math.min(firstIndex, i);
                        lastIndex = Math.max(lastIndex, i);
                        break;
                    }
                }
            }
            if (firstIndex <= lastIndex) {
                fireIntervalRemoved(this, firstIndex, lastIndex);
            }
        }

        /**
         * Remove all regexes from the model.
         */
        void clear() {
            regex.clear();
            this.fireContentsChanged(this, 0, 0);
        }

        @Override
        public Regex getElementAt(int index) {
            return regex.get(index);
        }

        @Override
        public int getSize() {
            return regex.size();
        }
    }
}
