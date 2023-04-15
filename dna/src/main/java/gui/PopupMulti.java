package gui;

import dna.Dna;
import model.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
     * A hash map referencing all roles in the database by their ID (e.g., for finding the position of a role)
     */
    private HashMap<Integer, Role> roleMap;
    private Coder coder;
    private JComboBox<Coder> coderComboBox;
    private JButton addRole, duplicate, remove;
    private JButton cancelButton, saveButton;
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
        this.tableStatement = tableStatement;
        this.los = location;
        this.textFieldWidth = coder.getPopupWidth();
        this.color = tableStatement.getStatementTypeColor();
        this.coder = new Coder(tableStatement.getCoderId(), tableStatement.getCoderName(), tableStatement.getCoderColor());
        if (coder.isPopupDecoration()) {
            this.windowDecoration = true;
        } else {
            this.windowDecoration = false;
            this.setUndecorated(true);
        }

        // get roles, put in hash map for easy access, and sort the values in the statement by their role position defined in the statement type
        roleMap = new HashMap();
        Dna.sql.getRoles().stream().forEach(role -> roleMap.put(role.getId(), role));
        this.tableStatement.setRoleValues(this.tableStatement.getRoleValues()
                .stream()
                .sorted(Comparator.comparing(v -> roleMap.get(v.getRoleId()).getPosition()))
                .collect(Collectors.toCollection(ArrayList::new)));

        // get additional data from database for creating combo boxes and adding roles
        entities = Dna.sql.getEntities(this.tableStatement.getStatementTypeId());
        roleVariableLinks = Dna.sql.getRoleVariableLinks(this.tableStatement.getStatementTypeId());
        variables = Dna.sql.getVariables();

        // should the changes in the statements be saved? check permissions...
        editable = coder.isPermissionEditStatements() && (tableStatement.getCoderId() == coder.getId() || (coder.isPermissionEditOthersStatements() && coder.isPermissionEditOthersStatements(tableStatement.getCoderId())));

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
        idAndPositionPanel.setBorder( BorderFactory.createEmptyBorder(0, 0, 0, -1) ); // right-align with remove buttons in role value panel

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

        ImageIcon addRoleIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-row-insert-bottom.png"))).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
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

            public void mousePressed(MouseEvent e) {
                showMenu(e);
            }
        });

        ImageIcon duplicateIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-copy.png"))).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        duplicate = new JButton(duplicateIcon);
        duplicate.setToolTipText("create a copy of this statement at the same location");
        duplicate.setMargin(new Insets(0, 0, 0, 0));
        duplicate.setContentAreaFilled(false);
        duplicate.setEnabled(coder.isPermissionAddStatements());

        // TODO: add save and cancel buttons, then get rid of buttons at bottom and window decoration;
        //  don't show save and cancel buttons if window decoration deactivated;
        //  add action listeners to these buttons from the outside (main window) because updating remaining GUI from popup is bad practice;
        //  update the statement upon save; write SQL code for this (try existing function);
        //  add some visual element to indicate whether the statement has been updated or not at any point
        //  take care of permissions and check nothing is done that isn't permitted

        ImageIcon removeIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-trash.png"))).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        remove = new JButton(removeIcon);
        remove.setToolTipText("completely remove the whole statement (but keep the text)");
        remove.setMargin(new Insets(0, 0, 0, 0));
        remove.setContentAreaFilled(false);
        remove.setEnabled(coder.isPermissionDeleteStatements());
        if (this.tableStatement.getCoderId() != coder.getId() && !Dna.sql.getActiveCoder().isPermissionEditOthersStatements()) {
            addRole.setEnabled(false);
            remove.setEnabled(false);
        }
        if (this.tableStatement.getCoderId() != coder.getId() && !coder.isPermissionEditOthersStatements(tableStatement.getCoderId())) {
            addRole.setEnabled(false);
            remove.setEnabled(false);
        }

        idAndPositionPanel.add(idLabel);
        idAndPositionPanel.add(idField);
        idAndPositionPanel.add(sPosLabel);
        idAndPositionPanel.add(startPos);
        idAndPositionPanel.add(ePosLabel);
        idAndPositionPanel.add(endPos);

        if (eligibleCoders == null || eligibleCoders.size() == 1) {
            CoderBadgePanel cbp = new CoderBadgePanel(this.coder);
            idAndPositionPanel.add(cbp);
        } else {
            int selectedIndex = -1;
            for (int i = 0; i < eligibleCoders.size(); i++) {
                if (eligibleCoders.get(i).getId() == this.coder.getId()) {
                    selectedIndex = i;
                }
            }
            coderComboBox = new JComboBox<>();
            CoderComboBoxModel comboBoxModel = new CoderComboBoxModel(eligibleCoders);
            coderComboBox.setModel(comboBoxModel);
            coderComboBox.setRenderer(new CoderComboBoxRenderer(9, 0, 22));
            coderComboBox.setSelectedIndex(selectedIndex);
            coderComboBox.setPreferredSize(new Dimension(coderComboBox.getPreferredSize().width, h)); // need to hard-code height because of MacOS
            idAndPositionPanel.add(coderComboBox);
        }

        idAndPositionPanel.add(addRole);
        idAndPositionPanel.add(duplicate);
        idAndPositionPanel.add(remove);

        titleDecorationPanel.add(idAndPositionPanel, BorderLayout.EAST);
        titleDecorationPanel.add(typeLabel, BorderLayout.CENTER);
        titleDecorationPanel.add(sep, BorderLayout.SOUTH);
        titleDecorationPanel.add(colorPanel, BorderLayout.WEST);
        contentsPanel.add(titleDecorationPanel, BorderLayout.NORTH);

        rvp = new RoleValuePanel(this.tableStatement);
        rvp.rebuildLayout();
        contentsPanel.add(rvp, BorderLayout.CENTER);

        // add buttons if window decoration is true
        if (this.windowDecoration) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            ImageIcon cancelIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-x.png"))).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
            cancelButton = new JButton("Cancel", cancelIcon);
            cancelButton.setToolTipText("close this window without making any changes");
            buttonPanel.add(cancelButton);
            ImageIcon saveIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-check.png"))).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH));
            saveButton = new JButton("Save", saveIcon);
            saveButton.setToolTipText("save each variable into the database and close this window");
            buttonPanel.add(saveButton);
            if (!editable) {
                saveButton.setEnabled(false);
            }
            contentsPanel.add(buttonPanel, BorderLayout.SOUTH);
        }

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
     * Get a reference to the cancel button.
     *
     * @return The save button.
     */
    JButton getCancelButton() {
        return this.cancelButton;
    }

    /**
     * Get a reference to the save button.
     *
     * @return The save button.
     */
    JButton getSaveButton() {
        return this.saveButton;
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
     * Get an updated copy of the table statement.
     *
     * @return The table statement.
     */
    TableStatement getTableStatementCopy() {
        TableStatement s = new TableStatement(this.tableStatement);
        s.setCoderColor(this.coder.getColor());
        s.setCoderName(this.coder.getName());
        s.setCoderId(this.coder.getId());
        return s;
    }

    /**
     * In a statement popup window, read the contents from all combo boxes and
     * save them into the database.
     *
     * @param simulate  If true, do not actually write the changes.
     * @return          True if at least one of the values has changed.
     */
    boolean saveContents(boolean simulate) {
        boolean changed = false;
        // TODO: save contents of popup
        return changed;
    }

    private class RoleValueTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return PopupMulti.this.tableStatement.getRoleValues().size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int row, int col) {
            return PopupMulti.this.tableStatement.getRoleValues().get(row);
        }

        public boolean isCellEditable(int row, int col) {
            return col == 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return RoleValue.class;
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

            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.gridx = 0;
            gbc.gridy = -1;

            for (int i = 0; i < this.ts.getRoleValues().size(); i++) {
                gbc.gridy++;
                gbc.gridx = 0;
                final RoleValue roleValue = this.ts.getRoleValues().get(i);
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

                    gbc.anchor = GridBagConstraints.EAST;
                    this.add(new JLabel(roleValue.getRoleName(), JLabel.TRAILING), gbc);
                    gbc.gridx++;
                    gbc.anchor = GridBagConstraints.WEST;
                    this.add(box, gbc);
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

                    gbc.anchor = GridBagConstraints.NORTHEAST;
                    this.add(new JLabel(roleValue.getRoleName(), JLabel.TRAILING), gbc);
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.gridx++;
                    this.add(boxScroller, gbc);
                } else if (roleValue.getDataType().equals("boolean")) {
                    int entry = (Integer) roleValue.getValue();
                    boolean val = entry != 0;
                    BooleanButtonPanel buttons = new BooleanButtonPanel();
                    buttons.setYes(val);
                    buttons.setEnabled(PopupMulti.this.editable);

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
                gbc.gridx++;
                ImageIcon removeIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-trash-x-filled.png"))).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
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

                // column 4: add a duplicate
                gbc.gridx++;
                ImageIcon addIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-arrows-split-2.png"))).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
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

        void save() {
            PopupMulti.this.tableStatement = new TableStatement(this.ts);
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