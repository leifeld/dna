package gui;

import dna.Dna;
import model.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class PopupMulti extends JDialog {
    private static final long serialVersionUID = 6731530941239032349L;
    private Container c;
    private Point los;
    private Color color;
    private boolean windowDecoration, editable;
    private int textFieldWidth;
    TableStatement tableStatement;
    /**
     * A list of coders that are eligible to edit the statement. Supplied by the constructor.
     */
    private ArrayList<Coder> eligibleCoders;
    /**
     * A hash map referencing all roles in the database by their ID (e.g., for finding the position of a role)
     */
    private HashMap<Integer, Role> roleMap;
    private Coder coder;
    private JComboBox<Coder> coderComboBox;
    private JButton addRole, duplicate, remove;
    private JButton saveButton;
    private ArrayList<RoleVariableLink> roleVariableLinks;
    private ArrayList<Entity> entities;
    private ArrayList<Variable> variables;
    private RoleValuePanel rvp;

    /**
     * Popup dialog window to display the contents of a statements. The user can
     * edit the values of each variable.
     *
     * @param X Horizontal coordinate for the window.
     * @param Y Vertical coordinate for the window.
     * @param tableStatement The {@link model.TableStatement} to be edited.
     * @param location Location of the DNA text panel on screen.
     * @param coder The current coder who is viewing the statement.
     * @param eligibleCoders A list of coders who are allowed to own the statement.
     */
    PopupMulti(double X, double Y, TableStatement tableStatement, Point location, Coder coder, ArrayList<Coder> eligibleCoders) {

        // TODO:
        //  transfer the contents from int spinners and boolean radio buttons to modified statement in role value panel using listeners, then test saving and updating statements
        //  debug revert and save buttons;
        //  add amber color to save and revert buttons if any changes have been made; write listeners for statement content changes
        //  debug #Sql.updateTableStatements() function;
        //  take care of permissions and check nothing is done that isn't permitted
        //  clean up the code and write javadoc annotations

        this.tableStatement = tableStatement;
        this.los = location;
        this.textFieldWidth = coder.getPopupWidth();
        this.color = tableStatement.getStatementTypeColor();
        this.eligibleCoders = eligibleCoders;
        this.coder = coder;
        if (coder.isPopupDecoration()) {
            this.windowDecoration = true;
        } else {
            this.windowDecoration = false;
            this.setUndecorated(true);
        }

        // get roles, put in hash map for easy access, and sort the values in the statement by their role position defined in the statement type
        this.roleMap = new HashMap();
        Dna.sql.getRoles().stream().forEach(role -> roleMap.put(role.getId(), role));
        this.tableStatement.setRoleValues(this.tableStatement.getRoleValues()
                .stream()
                .sorted(Comparator.comparing(v -> roleMap.get(v.getRoleId()).getPosition()))
                .collect(Collectors.toCollection(ArrayList::new)));

        // get additional data from database for creating combo boxes and adding roles
        this.entities = Dna.sql.getEntities(this.tableStatement.getStatementTypeId());
        this.roleVariableLinks = Dna.sql.getRoleVariableLinks(this.tableStatement.getStatementTypeId());
        this.variables = Dna.sql.getVariables();

        // should the changes in the statements be saved? check permissions...
        this.editable = coder.isPermissionEditStatements() && (tableStatement.getCoderId() == coder.getId() || (coder.isPermissionEditOthersStatements() && coder.isPermissionEditOthersStatements(tableStatement.getCoderId())));

        this.setTitle("Statement details");
        this.setAlwaysOnTop(true);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        ImageIcon statementIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-message-2.png")));
        this.setIconImage(statementIcon.getImage());

        c = getContentPane();

        JPanel contentsPanel = new JPanel(new BorderLayout());
        contentsPanel.setBorder(new LineBorder(Color.BLACK));
        JPanel titleDecorationPanel = new JPanel(new BorderLayout());
        JPanel idAndPositionPanel = new JPanel();

        JLabel sPosLabel = new JLabel("start");
        JTextField startPos = new JTextField(Integer.toString(tableStatement.getStart()));
        int h = 20; // getting the text field height does not work properly on MacOS, so need to hard-code
        startPos.setPreferredSize(new Dimension(startPos.getPreferredSize().width, h));
        startPos.setEditable(false);

        JLabel ePosLabel = new JLabel("end");
        JTextField endPos = new JTextField(Integer.toString(tableStatement.getStop()));
        endPos.setPreferredSize(new Dimension(endPos.getPreferredSize().width, h));
        endPos.setEditable(false);

        JLabel idLabel = new JLabel(" ID");
        JTextField idField = new JTextField(Integer.toString(tableStatement.getId()));
        idField.setPreferredSize(new Dimension(idField.getPreferredSize().width, h));
        idField.setEditable(false);

        String type = tableStatement.getStatementTypeLabel();
        JLabel typeLabel = new JLabel(" " + type);

        JSeparator sep = new JSeparator();

        JPanel colorPanel = new JPanel();
        colorPanel.setBackground(color);
        colorPanel.setPreferredSize(new Dimension(4, 4));

        ImageIcon addRoleIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-row-insert-bottom.png"))).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
        addRole = new JButton(addRoleIcon);
        addRole.setToolTipText("add an additional role to this statement...");
        addRole.setMargin(new Insets(0, 0, 0, 0));
        addRole.setContentAreaFilled(false);
        addRole.setEnabled(coder.isPermissionAddStatements());
        addRole.addMouseListener(new MouseAdapter() {
            public void showMenu(MouseEvent e) {
                JPopupMenu addMenu = new JPopupMenu();
                PopupMulti.this.roleMap.values()
                        .stream()
                        .filter(r -> r.getStatementTypeId() == PopupMulti.this.tableStatement.getStatementTypeId())
                        .forEach(r -> {
                            if (r.getNumMax() > PopupMulti.this.rvp.getModifiedTableStatement().getRoleValues()
                                    .stream()
                                    .filter(m -> m.getRoleId() == r.getId())
                                    .count()) {
                                JMenuItem menuItem = new JMenuItem("Add " + r.getRoleName(), addRoleIcon);
                                menuItem.addActionListener(actionEvent -> {
                                    // create a blank entity; figure out the existing blank entity for one of the variables corresponding to the role
                                    int variableId = PopupMulti.this.roleVariableLinks
                                            .stream()
                                            .filter(rv -> rv.getRoleId() == r.getId())
                                            .mapToInt(rv -> rv.getVariableId())
                                            .findFirst()
                                            .getAsInt();
                                    Entity blankEntity = PopupMulti.this.entities
                                            .stream()
                                            .filter(entity -> entity.getValue().equals("") && entity.getVariableId() == variableId)
                                            .findFirst()
                                            .get();
                                    Variable variable = PopupMulti.this.variables.stream().filter(v -> v.getVariableId() == variableId).findFirst().get();
                                    int roleVariableLinkId = roleVariableLinks
                                            .stream()
                                            .filter(rvl -> rvl.getVariableId() == variableId && rvl.getRoleId() == r.getId())
                                            .mapToInt(rvl -> rvl.getId())
                                            .findFirst()
                                            .getAsInt();
                                    PopupMulti.this.rvp.addRoleValue(new RoleValue(variableId, variable.getVariableName(), variable.getDataType(), blankEntity, roleVariableLinkId, r.getId(), r.getRoleName(), r.getStatementTypeId()));
                                });
                                addMenu.add(menuItem);
                            }
                        });
                addMenu.show(e.getComponent(), e.getX(), e.getY());
            }

            public void mouseClicked(MouseEvent e) {
                showMenu(e);
            }
        });
        addRole.setEnabled(this.editable);

        ImageIcon duplicateIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-copy.png"))).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
        duplicate = new JButton(duplicateIcon);
        duplicate.setToolTipText("create a copy of this statement at the same location");
        duplicate.setMargin(new Insets(0, 0, 0, 0));
        duplicate.setContentAreaFilled(false);
        duplicate.setEnabled(this.coder.isPermissionAddStatements());

        ImageIcon removeIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-trash.png"))).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
        remove = new JButton(removeIcon);
        remove.setToolTipText("completely remove the whole statement (but keep the text)");
        remove.setMargin(new Insets(0, 0, 0, 0));
        remove.setContentAreaFilled(false);
        remove.setEnabled((this.tableStatement.getCoderId() == this.coder.getId() && this.coder.isPermissionDeleteStatements()) ||
                (this.tableStatement.getCoderId() != this.coder.getId() && this.editable));

        ImageIcon revertIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-rotate-clockwise.png"))).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
        JButton revertButton = new JButton(revertIcon);
        revertButton.setToolTipText("revert any changes to the state when the statement was opened");
        revertButton.setMargin(new Insets(0, 0, 0, 0));
        revertButton.setContentAreaFilled(false);
        revertButton.addActionListener(actionEvent -> {
            if (PopupMulti.this.isStatementModified()) {
                PopupMulti.this.rvp.ts = new TableStatement(PopupMulti.this.tableStatement);
                PopupMulti.this.rvp.rebuildLayout();
                if (PopupMulti.this.eligibleCoders != null && PopupMulti.this.eligibleCoders.size() > 1) {
                    int coderIndex = -1;
                    for (int i = 0; i < PopupMulti.this.eligibleCoders.size(); i++) {
                        if (PopupMulti.this.eligibleCoders.get(i).getId() == PopupMulti.this.rvp.ts.getCoderId()) {
                            coderIndex = i;
                        }
                    }
                    PopupMulti.this.coderComboBox.setSelectedIndex(coderIndex);
                    PopupMulti.this.coderComboBox.repaint(); // TODO: not sure if necessary
                }
            }
        });

        ImageIcon saveIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/google_round_save_black_48dp.png"))).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
        // ImageIcon saveIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-device-floppy.png"))).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
        saveButton = new JButton(saveIcon);
        saveButton.setToolTipText("save the contents of the statement into the database");
        saveButton.setMargin(new Insets(0, 0, 0, 0));
        saveButton.setContentAreaFilled(false);
        saveButton.addActionListener(actionEvent -> {
            PopupMulti.this.saveContents();
        });
        saveButton.setEnabled(this.editable);
        if (!this.hasWindowDecoration()) {
            saveButton.setVisible(false);
        }

        idAndPositionPanel.add(idLabel);
        idAndPositionPanel.add(idField);
        idAndPositionPanel.add(sPosLabel);
        idAndPositionPanel.add(startPos);
        idAndPositionPanel.add(ePosLabel);
        idAndPositionPanel.add(endPos);

        Coder statementCoder = new Coder(this.tableStatement.getCoderId(), this.tableStatement.getCoderName(), this.tableStatement.getCoderColor());
        if (eligibleCoders == null || eligibleCoders.size() == 1) {
            CoderBadgePanel cbp = new CoderBadgePanel(statementCoder);
            idAndPositionPanel.add(cbp);
        } else {
            int selectedIndex = -1;
            for (int i = 0; i < eligibleCoders.size(); i++) {
                if (eligibleCoders.get(i).getId() == statementCoder.getId()) {
                    selectedIndex = i;
                }
            }
            coderComboBox = new JComboBox<>();
            CoderComboBoxModel comboBoxModel = new CoderComboBoxModel(eligibleCoders);
            coderComboBox.setModel(comboBoxModel);
            coderComboBox.setRenderer(new CoderComboBoxRenderer(9, 0, 22));
            coderComboBox.setSelectedIndex(selectedIndex);
            coderComboBox.setPreferredSize(new Dimension(coderComboBox.getPreferredSize().width, h)); // need to hard-code height because of MacOS
            coderComboBox.addItemListener(itemEvent -> {
                Coder selectedCoder = (Coder) coderComboBox.getSelectedItem();
                PopupMulti.this.rvp.getModifiedTableStatement().setCoderId(selectedCoder.getId());
                PopupMulti.this.rvp.getModifiedTableStatement().setCoderName(selectedCoder.getName());
                PopupMulti.this.rvp.getModifiedTableStatement().setCoderColor(selectedCoder.getColor());
            });
            idAndPositionPanel.add(coderComboBox);
        }

        idAndPositionPanel.add(addRole);
        idAndPositionPanel.add(duplicate);
        idAndPositionPanel.add(remove);
        idAndPositionPanel.add(revertButton);
        idAndPositionPanel.add(saveButton);

        titleDecorationPanel.add(idAndPositionPanel, BorderLayout.EAST);
        titleDecorationPanel.add(typeLabel, BorderLayout.CENTER);
        titleDecorationPanel.add(sep, BorderLayout.SOUTH);
        titleDecorationPanel.add(colorPanel, BorderLayout.WEST);
        contentsPanel.add(titleDecorationPanel, BorderLayout.NORTH);

        rvp = new RoleValuePanel(this.tableStatement);
        rvp.rebuildLayout();
        contentsPanel.add(rvp, BorderLayout.CENTER);

        c.add(contentsPanel);

        this.pack();
        double xDouble = los.getX() + X;
        double yDouble = los.getY() + Y;
        int x = (int) xDouble + 6;
        int y = (int) yDouble + 13;
        this.setLocation(x, y);
    }

    /**
     * Does the popup window have window decoration?
     *
     * @return True if the popup has window decoration and false otherwise.
     */
    boolean hasWindowDecoration() {
        return this.windowDecoration;
    }

    /**
     * Is the popup window editable?
     *
     * @return True if the values can be changed and false otherwise.
     */
    boolean isEditable() {
        return this.editable;
    }

    /**
     * Get a reference to the duplicate button.
     *
     * @return The duplicate button.
     */
    JButton getDuplicateButton() {
        return duplicate;
    }

    /**
     * Get a reference to the remove button.
     *
     * @return The remove button.
     */
    JButton getRemoveButton() {
        return remove;
    }

    /**
     * Check if the coder ID has been changed.
     *
     * @return Indicator of statement coder ID change.
     */
    boolean isCoderChanged() {
        if (this.coder.getId() != this.tableStatement.getCoderId()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the table statement.
     *
     * @return The table statement.
     */
    TableStatement getTableStatement() {
        return this.tableStatement;
    }

    boolean isStatementModified() {
        return this.rvp.getModifiedTableStatement().equals(this.tableStatement);
    }

    /**
     * Save the modified statement from the role value panel into the database and into the statement.
     *
     * @return True if successful or if saving was not necessary because the statement was unchanged.
     */
    boolean saveContents() {
        if (this.isStatementModified()) {
            TableStatement statementCopy = new TableStatement(this.rvp.getModifiedTableStatement());
            ArrayList<TableStatement> tableStatementList = new ArrayList();
            tableStatementList.add(statementCopy);
            boolean changed = Dna.sql.updateTableStatements(tableStatementList);
            if (changed) {
                this.tableStatement = statementCopy;
            }
            saveButton.setEnabled(!changed);
            return changed;
        } else {
            saveButton.setEnabled(false);
            return true;
        }
    }

    /**
     * A renderer for JComboBox items that represent {@link model.Entity
     * Entity} objects. The value is shown as text. The color is shown as the
     * foreground color. If the attribute is not present in the database, it
     * gets a red background color. The renderer is used to display combo boxes
     * for short text variables in popup windows. The renderer only displays the
     * list items, not the contents of the text editor at the top of the list.
     */
    class EntityComboBoxRenderer implements ListCellRenderer<Object> {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Entity a = (Entity) value;
            JLabel label = new JLabel(a.getValue());
            label.setForeground(a.getColor());
            // list background
            Color selectedColor = javax.swing.UIManager.getColor("List.selectionBackground");
            Color notInDatabaseColor = new Color(255, 102, 102);
            // selected entry that is not in database: average of the previous two colors
            Color selectedAndNotInDatabaseColor = new Color((selectedColor.getRed() + notInDatabaseColor.getRed()) / 2, (selectedColor.getGreen() + notInDatabaseColor.getGreen()) / 2, (selectedColor.getBlue() + notInDatabaseColor.getBlue()) / 2);
            Color defaultColor = javax.swing.UIManager.getColor("List.background");
            if (isSelected && a.isInDatabase()) {
                label.setBackground(selectedColor);
            } else if (isSelected && !a.isInDatabase()) {
                label.setBackground(selectedAndNotInDatabaseColor);
            } else if (!isSelected && !a.isInDatabase()) {
                label.setBackground(notInDatabaseColor);
            } else if (!isSelected && a.isInDatabase()) {
                label.setBackground(defaultColor);
            }
            label.setOpaque(true);
            return label;
        }
    }

    class RoleValuePanel extends JPanel {
        TableStatement ts;

        public RoleValuePanel(TableStatement ts) {
            this.ts = new TableStatement(ts);
        }

        TableStatement getModifiedTableStatement() {
            return this.ts;
        }

        private void rebuildLayout() {
            this.removeAll();
            this.revalidate();
            GridBagLayout gbl = new GridBagLayout();
            this.setLayout(gbl);
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridx = 0;
            gbc.gridy = -1;

            for (int i = 0; i < this.ts.getRoleValues().size(); i++) {
                gbc.insets = new Insets(3, 3, 3, 3);
                gbc.gridy++;
                gbc.gridx = 0;
                final RoleValue roleValue = this.ts.getRoleValues().get(i);
                int finalI1 = i;
                if (roleValue.getDataType().equals("short text")) {
                    // find variable IDs from which current role is sourced, to populate the combo box
                    java.util.List<Integer> roleVariableIds = PopupMulti.this.roleVariableLinks
                            .stream()
                            .filter(r -> r.getRoleId() == roleValue.getRoleId())
                            .map(r -> r.getVariableId())
                            .collect(Collectors.toList());
                    // retain the subset of entities corresponding to the selected variable IDs, to put them in the combo box
                    Entity[] entitySubset = PopupMulti.this.entities
                            .stream()
                            .filter(e -> roleVariableIds.contains(e.getVariableId()))
                            .toArray(Entity[]::new);
                    JComboBox<Entity> box = new JComboBox<>(entitySubset);
                    box.setRenderer(new EntityComboBoxRenderer());
                    box.setEditable(true);
                    box.getModel().setSelectedItem(roleValue.getValue());
                    box.setPreferredSize(new Dimension(PopupMulti.this.textFieldWidth, 20));

                    // paint the selected value in the attribute color
                    String s = ((JTextField) box.getEditor().getEditorComponent()).getText();
                    Color fg = javax.swing.UIManager.getColor("TextField.foreground"); // default unselected foreground color of JTextField
                    for (int j = 0; j < box.getModel().getSize(); j++) {
                        if (s.equals(box.getModel().getElementAt(j).getValue())) {
                            fg = box.getModel().getElementAt(j).getColor();
                        }
                    }
                    ((JTextField) box.getEditor().getEditorComponent()).setSelectedTextColor(fg);
                    ((JTextField) box.getEditor().getEditorComponent()).setForeground(fg);

                    // add a document listener to the combobox to paint the selected value in the attribute color, despite being highlighted
                    ((JTextField) box.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void changedUpdate(DocumentEvent arg0) {
                            formatEntry();
                        }
                        @Override
                        public void insertUpdate(DocumentEvent arg0) {
                            formatEntry();
                        }
                        @Override
                        public void removeUpdate(DocumentEvent arg0) {
                            formatEntry();
                        }
                        private void formatEntry() {
                            Color fg = javax.swing.UIManager.getColor("TextField.foreground"); // default unselected foreground color of JTextField
                            for (int i = 0; i < box.getModel().getSize(); i++) {
                                if (((JTextField) box.getEditor().getEditorComponent()).getText().equals(box.getModel().getElementAt(i).getValue())) {
                                    fg = box.getModel().getElementAt(i).getColor();
                                }
                            }
                            ((JTextField) box.getEditor().getEditorComponent()).setSelectedTextColor(fg);
                            ((JTextField) box.getEditor().getEditorComponent()).setForeground(fg);
                        }
                    });

                    // listener to save changes in modified statement
                    box.addItemListener(itemEvent -> {
                        if (!((String) box.getSelectedItem().toString()).equals(box.getEditor().getItem().toString())) {
                            box.setSelectedItem(box.getEditor().getItem()); // make sure combo box edits are saved even if the editor has not lost its focus yet
                        }
                        Object object = box.getSelectedItem();
                        Entity entity;
                        if (object.getClass().getName().endsWith("String")) { // if not an existing entity, the editor returns a String
                            String s1 = (String) object;
                            if (s1.length() > 0 && s1.matches("^\\s+$")) { // replace a (multiple) whitespace string by an empty string
                                s1 = "";
                            }
                            s1 = s1.substring(0, Math.min(190, s1.length()));
                            entity = new Entity(s1); // the new entity has an ID of -1; the SQL class needs to take care of this when writing into the database
                            RoleValuePanel.this.ts.getRoleValues().get(finalI1).setVariableId(PopupMulti.this.roleMap.get(roleValue.getRoleId()).getDefaultVariableId());
                        } else {
                            entity = (Entity) box.getSelectedItem();
                            RoleValuePanel.this.ts.getRoleValues().get(finalI1).setVariableId(entity.getVariableId());
                        }
                        RoleValuePanel.this.ts.getRoleValues().get(finalI1).setVariableName(PopupMulti.this.variables.stream().filter(v -> v.getVariableId() == entity.getVariableId()).findFirst().get().getVariableName());
                        RoleValuePanel.this.ts.getRoleValues().get(finalI1).setRoleVariableLinkId(PopupMulti.this.roleVariableLinks.stream().filter(l -> l.getVariableId() == entity.getVariableId()).findFirst().get().getId());
                        RoleValuePanel.this.ts.getRoleValues().get(finalI1).setValue(entity);
                    });

                    gbc.anchor = GridBagConstraints.EAST;
                    this.add(new JLabel(roleValue.getRoleName(), JLabel.TRAILING), gbc);
                    gbc.gridx++;
                    gbc.weightx = 1.0;
                    gbc.anchor = GridBagConstraints.WEST;
                    this.add(box, gbc);
                    gbc.weightx = 0;
                } else if (roleValue.getDataType().equals("long text")) {
                    String entry = (String) roleValue.getValue();
                    JTextArea box = new JTextArea();
                    box.setEditable(true);
                    box.setEnabled(PopupMulti.this.editable);
                    box.setWrapStyleWord(true);
                    box.setLineWrap(true);
                    box.setText(entry);
                    JScrollPane boxScroller = new JScrollPane(box);
                    boxScroller.setPreferredSize(new Dimension(PopupMulti.this.textFieldWidth, 100));
                    boxScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                    // save contents
                    box.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent documentEvent) {
                            save();
                        }

                        @Override
                        public void removeUpdate(DocumentEvent documentEvent) {
                            save();
                        }

                        @Override
                        public void changedUpdate(DocumentEvent documentEvent) {
                            save();
                        }

                        private void save() {
                            String content = box.getText();
                            if (content == null) {
                                content = "";
                            }
                            RoleValuePanel.this.ts.getRoleValues().get(finalI1).setValue(content);
                        }
                    });

                    gbc.anchor = GridBagConstraints.NORTHEAST;
                    this.add(new JLabel(roleValue.getRoleName(), JLabel.TRAILING), gbc);
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.gridx++;
                    gbc.weightx = 1.0;
                    this.add(boxScroller, gbc);
                    gbc.weightx = 0;
                } else if (roleValue.getDataType().equals("boolean")) {
                    int entry = (Integer) roleValue.getValue();
                    boolean val = entry != 0;
                    BooleanButtonPanel buttons = new BooleanButtonPanel();
                    buttons.setYes(val);
                    buttons.setEnabled(PopupMulti.this.editable);

                    // save contents
                    // TODO: add a listener and update contents of modified statement in role value panel

                    gbc.anchor = GridBagConstraints.EAST;
                    gbc.insets = new Insets(3,3,3,2);
                    this.add(new JLabel(roleValue.getRoleName(), JLabel.TRAILING), gbc);
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.insets = new Insets(0,0,0,0);
                    gbc.gridx++;
                    this.add(buttons, gbc);
                    gbc.insets = new Insets(3,3,3,3);
                } else if (roleValue.getDataType().equals("integer")) {
                    int entry = (Integer) roleValue.getValue();
                    JSpinner jsp = new JSpinner();
                    jsp.setValue(entry);
                    jsp.setPreferredSize(new Dimension(70, 20));
                    jsp.setEnabled(true);
                    JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    jp.add(jsp);
                    jsp.setEnabled(PopupMulti.this.editable);

                    // save contents
                    // TODO: add a listener and update contents of modified statement in role value panel

                    gbc.anchor = GridBagConstraints.EAST;
                    this.add(new JLabel(roleValue.getRoleName(), JLabel.TRAILING), gbc);
                    gbc.insets = new Insets(0, 0, 0, 0);
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.gridx++;
                    this.add(jp, gbc);
                    gbc.insets = new Insets(3, 3, 3, 3);
                }

                int finalI = i;
                long num = this.ts.getRoleValues().stream().filter(r -> r.getRoleId() == this.ts.getRoleValues().get(finalI).getRoleId()).count();

                // column 3: remove the roleValue row
                gbc.insets = new Insets(3, 2, 3, 5);
                gbc.gridx++;
                ImageIcon addIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-arrows-split-2.png"))).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
                JButton addButton = new JButton(addIcon);
                addButton.setMargin(new Insets(0, 0, 0, 0));
                addButton.setContentAreaFilled(false);
                addButton.addActionListener(actionEvent -> {
                    RoleValue duplicate = new RoleValue(this.ts.getRoleValues().get(finalI));
                    // replace value by the entity corresponding to an empty string
                    duplicate.setValue(PopupMulti.this.entities
                            .stream()
                            .filter(e -> (e.getVariableId() == duplicate.getVariableId() && e.getValue().equals("")))
                            .findFirst()
                            .get());
                    this.addRoleValue(duplicate);
                });
                int max = PopupMulti.this.roleMap.get(this.ts.getRoleValues().get(i).getRoleId()).getNumMax();
                addButton.setEnabled(max > num);
                addButton.setVisible(max > num);
                addButton.setToolTipText("add another " + this.ts.getRoleValues().get(i).getRoleName() + " to the statement");
                this.add(addButton, gbc);

                // column 4: add a duplicate
                gbc.insets = new Insets(3, 0, 3, 5);
                gbc.gridx++;
                ImageIcon removeIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-trash-x-filled.png"))).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
                JButton removeButton = new JButton(removeIcon);
                removeButton.setMargin(new Insets(0, 0, 0, 0));
                removeButton.setContentAreaFilled(false);
                removeButton.addActionListener(actionEvent -> {
                    removeRoleValue(finalI);
                });
                int min = PopupMulti.this.roleMap.get(this.ts.getRoleValues().get(i).getRoleId()).getNumMin();
                removeButton.setEnabled(min < num);
                removeButton.setVisible(min < num);
                removeButton.setToolTipText("remove this entity from the statement");
                this.add(removeButton, gbc);
            }

            this.repaint();
            PopupMulti.this.pack();
        }

        void addRoleValue(RoleValue added) {
            this.ts.getRoleValues().add(added);
            this.ts.setRoleValues(this.ts.getRoleValues()
                    .stream()
                    .sorted(Comparator.comparing(v -> roleMap.get(v.getRoleId()).getPosition()))
                    .collect(Collectors.toCollection(ArrayList::new)));
            this.rebuildLayout();
        }

        void removeRoleValue(int index) {
            this.ts.getRoleValues().remove(index);
            this.rebuildLayout();
        }
    }

    class EntityComboBoxPanel extends JPanel {
        JComboBox<Entity> box;

        public EntityComboBoxPanel(RoleValue roleValue) {
            super();
            // find variable IDs from which current role is sourced, to populate the combo box
            java.util.List<Integer> roleVariableIds = PopupMulti.this.roleVariableLinks
                    .stream()
                    .filter(r -> r.getRoleId() == roleValue.getRoleId())
                    .map(r -> r.getVariableId())
                    .collect(Collectors.toList());
            // retain the subset of entities corresponding to the selected variable IDs, to put them in the combo box
            Entity[] entitySubset = PopupMulti.this.entities
                    .stream()
                    .filter(e -> roleVariableIds.contains(e.getVariableId()))
                    .toArray(Entity[]::new);
            box = new JComboBox<>(entitySubset);
            box.setRenderer(new EntityComboBoxRenderer());
            box.setEditable(true);
            box.getModel().setSelectedItem(roleValue.getValue());
            box.setPreferredSize(new Dimension(textFieldWidth, 16));

            this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            //JLabel roleLabel = new JLabel(roleValue.getRoleName());
            //roleLabel.setPreferredSize(new Dimension(200, 16));
            //this.add(roleLabel);
            this.add(box);
        }
    }

    /*
    public class RoleValueCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof RoleValue) {
                RoleValue roleValue = (RoleValue) value;
                if (column == 1) {
                    EntityComboBoxPanel p = new EntityComboBoxPanel(roleValue);
                    return p;
                } else if (column == 0) {
                    ((JLabel) cellComponent).setText(roleValue.getRoleName());
                    ((JLabel) cellComponent).setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    return new JLabel("Text");
                }
            }
            return cellComponent;
        }
    }
    */
}