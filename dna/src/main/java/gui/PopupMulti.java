package gui;

import dna.Dna;
import model.*;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.awt.image.BaseMultiResolutionImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * {@link JDialog} extension that displays the content of a statement in the GUI, allowing the user to add (or remove)
 * multiple instances of roles.
 */
public class PopupMulti extends JDialog {
    private static final long serialVersionUID = 6731530941239131349L;
    private Container c;
    /**
     * The location of the popup window on screen. Supplied by the parent class through the constructor.
     */
    private Point los;
    /**
     * The color of the statement type associated with the statement displayed in the popup window.
     */
    private Color color;
    /**
     * Is the window decoration shown (i.e., the bar with minimization, maximization, and close buttons)?
     */
    private boolean windowDecoration;
    /**
     * Can the current user edit the contents of the statement, or are the contents disabled?
     */
    private boolean editable;
    /**
     * The width of the text fields.
     */
    private int textFieldWidth;
    /**
     * The statement to be edited.
     */
    private TableStatement tableStatement;
    /**
     * A list of coders that are eligible to edit the statement. Supplied by the constructor.
     */
    private ArrayList<Coder> eligibleCoders;
    /**
     * A hash map referencing all roles in the database by their ID (e.g., for finding the position of a role).
     */
    private HashMap<Integer, Role> roleMap;
    /**
     * An array list holding all roles in the database.
     */
    private ArrayList<Role> roles;
    /**
     * The coder for whom the statement should be opened, usually the active coder. Supplied by the parent class.
     */
    private Coder coder;
    /**
     * A combo box for eligible coders. Can be used to alter the coder associated with the statement.
     */
    private JComboBox<Coder> coderComboBox;
    /**
     * A button for adding new role instances to the statement.
     */
    private JButton addRole;
    /**
     * A button for creating a duplicate of the statement.
     */
    private JButton duplicate;
    /**
     * A button to delete the current statement from the database.
     */
    private JButton remove;
    /**
     * A button to revert the statement to its original version.
     */
    private JButton revertButton;
    /**
     * A button to save all changes made since the statement was loaded.
     */
    private JButton saveButton;
    /**
     * All role-variable links in the database that are relevant to the statement type of the current statement.
     */
    private ArrayList<RoleVariableLink> roleVariableLinks;
    /**
     * All entities in the database that are relevant to the statement.
     */
    private ArrayList<Entity> entities;
    /**
     * All variables in the database that are relevant to the statement type of the current statement.
     */
    private ArrayList<Variable> variables;
    /**
     * The role value panel, which displays the contents of the statement, i.e., the roles and variable values/entities.
     */
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
    PopupMulti(Frame parent, double X, double Y, TableStatement tableStatement, Point location, Coder coder, ArrayList<Coder> eligibleCoders) {
        super(parent, "Statement details", false);
        this.tableStatement = tableStatement;
        this.los = location;
        this.textFieldWidth = coder.getPopupWidth();
        this.color = tableStatement.getStatementTypeColor().toAWTColor();
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
        this.roles = Dna.sql.getRoles();
        this.roles.stream().forEach(role -> roleMap.put(role.getId(), role));
        // this.tableStatement.setRoleValues(this.tableStatement.getRoleValues()
        //         .stream()
        //         .sorted(Comparator.comparing(v -> roleMap.get(v.getRoleId()).getPosition()))
        //         .collect(Collectors.toCollection(ArrayList::new)));

        // get additional data from database for creating combo boxes and adding roles
        this.entities = Dna.sql.getEntities(this.tableStatement.getStatementTypeId());
        this.roleVariableLinks = Dna.sql.getRoleVariableLinks(this.tableStatement.getStatementTypeId());
        this.variables = Dna.sql.getVariables();

        // should the changes in the statements be saved? check permissions...
        this.editable = coder.isPermissionEditStatements() && (tableStatement.getCoderId() == coder.getId() || (coder.isPermissionEditOthersStatements() && coder.isPermissionEditOthersStatements(tableStatement.getCoderId())));

        this.setTitle("Statement details");
        this.setAlwaysOnTop(true);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        BaseMultiResolutionImage statementImage = new SvgIcon("/icons/dna.svg", 16).getImage();
        this.setIconImage(statementImage);

        c = getContentPane();

        JPanel contentsPanel = new JPanel(new BorderLayout());
        contentsPanel.setBorder(new LineBorder(Color.BLACK));
        JPanel titleDecorationPanel = new JPanel(new BorderLayout());
        JPanel idAndPositionPanel = new JPanel();

        JLabel sPosLabel = new JLabel("start");
        JTextField startPos = new JTextField(Integer.toString(tableStatement.getStart()));
        int h = 22; // getting the text field height does not work properly on MacOS, so need to hard-code
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

        // create a button for adding new role instances
        ImageIcon addRoleIcon = new SvgIcon("/icons/tabler_text_plus.svg", 16).getImageIcon();
        addRole = new JButton(addRoleIcon);
        addRole.setToolTipText("add an additional role to this statement...");
        addRole.setMargin(new Insets(0, 0, 0, 0));
        addRole.setContentAreaFilled(false);
        addRole.setEnabled(PopupMulti.this.isEditable());
        if (PopupMulti.this.isEditable()) {
            addRole.addMouseListener(new MouseAdapter() {
                public void showMenu(MouseEvent e) {
                    JPopupMenu addMenu = new JPopupMenu();
                    PopupMulti.this.roleMap.values()
                            .stream()
                            .filter(r -> r.getStatementTypeId() == PopupMulti.this.tableStatement.getStatementTypeId())
                            .forEach(r -> {
                                if (r.getNumMax() > PopupMulti.this.rvp.getTableStatement().getRoleValues()
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
                                        Variable variable = PopupMulti.this.variables.stream().filter(v -> v.getVariableId() == variableId).findFirst().get();
                                        int roleVariableLinkId = roleVariableLinks
                                                .stream()
                                                .filter(rvl -> rvl.getVariableId() == variableId && rvl.getRoleId() == r.getId())
                                                .mapToInt(rvl -> rvl.getId())
                                                .findFirst()
                                                .getAsInt();
                                        Object blankValue = null;
                                        if (variable.getDataType().equals("short text")) {
                                            blankValue = PopupMulti.this.entities
                                                    .stream()
                                                    .filter(entity -> entity.getValue().equals("") && entity.getVariableId() == variableId)
                                                    .findFirst()
                                                    .get();

                                        } else if (variable.getDataType().equals("long text")) {
                                            blankValue = "";
                                        } else if (variable.getDataType().equals("boolean")) {
                                            blankValue = 1;
                                        } else if (variable.getDataType().equals("integer")) {
                                            blankValue = 0;
                                        }
                                        PopupMulti.this.rvp.addRoleValue(new RoleValue(variableId, variable.getVariableName(), variable.getDataType(), blankValue, roleVariableLinkId, r.getId(), r.getRoleName(), r.getStatementTypeId()));
                                        toggleButtons();
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
        }

        // create a button for creating a statement duplicate (action listener is added in the parent class)
        ImageIcon duplicateIcon = new SvgIcon("/icons/tabler_copy.svg", 16).getImageIcon();
        duplicate = new JButton(duplicateIcon);
        duplicate.setToolTipText("create a copy of this statement at the same location");
        duplicate.setMargin(new Insets(0, 0, 0, 0));
        duplicate.setContentAreaFilled(false);
        duplicate.setEnabled(this.coder.isPermissionAddStatements());

        // create a button for deleting the statement from the database (action listener is added in the parent class)
        ImageIcon removeIcon = new SvgIcon("/icons/tabler_trash.svg", 16).getImageIcon();
        remove = new JButton(removeIcon);
        remove.setToolTipText("completely remove the whole statement (but keep the text)");
        remove.setMargin(new Insets(0, 0, 0, 0));
        remove.setContentAreaFilled(false);
        remove.setEnabled((this.tableStatement.getCoderId() == this.coder.getId() && this.coder.isPermissionDeleteStatements()) ||
                (this.tableStatement.getCoderId() != this.coder.getId() && this.editable));

        // create a button for reverting all statement changes to the original state when the statement was loaded
        ImageIcon revertIcon = new SvgIcon("/icons/google_device_reset.svg", 16).getImageIcon();
        revertButton = new JButton(revertIcon);
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
                        if (PopupMulti.this.eligibleCoders.get(i).getId() == PopupMulti.this.tableStatement.getCoderId()) {
                            coderIndex = i;
                        }
                    }
                    PopupMulti.this.coderComboBox.setSelectedIndex(coderIndex);
                    PopupMulti.this.coderComboBox.repaint();
                }
                toggleButtons();
            }
        });
        revertButton.setEnabled(false);

        // create a button for saving all changes to the database
        ImageIcon saveIcon = new SvgIcon("/icons/tabler_device_floppy.svg", 16).getImageIcon();
        saveButton = new JButton(saveIcon);
        saveButton.setToolTipText("save the contents of the statement into the database");
        saveButton.setMargin(new Insets(0, 0, 0, 0));
        saveButton.setContentAreaFilled(false);
        saveButton.addActionListener(actionEvent -> {
            PopupMulti.this.saveContents();
        });
        saveButton.setEnabled(false);
        if (!this.hasWindowDecoration()) {
            saveButton.setVisible(false);
        }

        idAndPositionPanel.add(idLabel);
        idAndPositionPanel.add(idField);
        idAndPositionPanel.add(sPosLabel);
        idAndPositionPanel.add(startPos);
        idAndPositionPanel.add(ePosLabel);
        idAndPositionPanel.add(endPos);

        // create coder combo box with statement coder selected in eligible coders in the combo box model; display only a coder badge if no other coder is eligible
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
                PopupMulti.this.rvp.getTableStatement().setCoderId(selectedCoder.getId());
                PopupMulti.this.rvp.getTableStatement().setCoderName(selectedCoder.getName());
                PopupMulti.this.rvp.getTableStatement().setCoderColor(selectedCoder.getColor());
                toggleButtons();
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

        // role-value panel for displaying and editing the actual statement contents; supply a deep copy of the statement to be able to track changes
        rvp = new RoleValuePanel(new TableStatement(this.tableStatement));
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

    /**
     * Have any of the contents (e.g., the coder or any of the values) been changed since the statement was opened or
     * last saved?
     *
     * @return Was the statement modified?
     */
    boolean isStatementModified() {
        return !this.rvp.getTableStatement().equals(this.tableStatement);
    }

    /**
     * Save the modified statement from the role value panel into the database and into the statement.
     *
     * @return True if successful or if saving was not necessary because the statement was unchanged.
     */
    boolean saveContents() {
        if (this.isStatementModified()) {
            TableStatement statementCopy = new TableStatement(this.rvp.getTableStatement());
            ArrayList<TableStatement> tableStatementList = new ArrayList();
            tableStatementList.add(statementCopy);
            boolean changed = Dna.sql.updateTableStatements(tableStatementList);
            if (changed) {
                this.tableStatement = Dna.sql.getTableStatement(statementCopy.getId());
                PopupMulti.this.rvp.setTableStatement(new TableStatement(this.tableStatement));
            }
            toggleButtons();
            return changed;
        } else {
            toggleButtons();
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
            label.setForeground(a.getColor().toAWTColor());
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

    /**
     * Enabled/disable and color the revert and save button depending on whether the statement has been changed.
     */
    private void toggleButtons() {
        if (PopupMulti.this.isStatementModified()) {
            revertButton.setIcon(new SvgIcon("/icons/google_device_reset.svg", 16, Color.ORANGE).getImageIcon());
            revertButton.setEnabled(true);
            saveButton.setIcon(new SvgIcon("/icons/tabler_device_floppy.svg", 16, Color.ORANGE).getImageIcon());
            saveButton.setEnabled(true);
        } else {
            revertButton.setIcon(new SvgIcon("/icons/google_device_reset.svg", 16).getImageIcon());
            revertButton.setEnabled(false);
            saveButton.setIcon(new SvgIcon("/icons/tabler_device_floppy.svg", 16).getImageIcon());
            saveButton.setEnabled(false);
        }
    }

    /**
     * A panel with role labels, corresponding variable values (in combo boxes, spinners, or radio buttons), and buttons
     * for removing and adding roles.
     */
    class RoleValuePanel extends JPanel {
        /**
         * A copy of the table statement in the popup window. Any changes to the contents, including values and coder,
         * are saved in this local copy and only transferred to the outer popup statement once the save button is
         * pressed.
         */
        TableStatement ts;

        /**
         * Constructor. Accepts a copy (i.e., not reference) of the outer statement from the popup window.
         *
         * @param ts A copy of the popup statement.
         */
        public RoleValuePanel(TableStatement ts) {
            this.ts = ts;
        }

        /**
         * Get the statement from this panel, possibly including any changes.
         *
         * @return The table statement.
         */
        TableStatement getTableStatement() {
            return this.ts;
        }

        /**
         * Set a new table statement, then rebuild the layout.
         *
         * @param tableStatement A new table statement to display.
         */
        public void setTableStatement(TableStatement tableStatement) {
            this.ts = tableStatement;
            this.rebuildLayout();
        }

        /**
         * Create rows for the roles, boxes, and buttons in the layout and repaint.
         */
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
                int defaultVariableId = PopupMulti.this.roleMap.get(roleValue.getRoleId()).getDefaultVariableId();
                int finalI1 = i;
                if (roleValue.getDataType().equals("short text")) {
                    // find variable IDs from which current role is sourced, to populate the combo box
                    java.util.List<Integer> roleVariableIds = PopupMulti.this.roleVariableLinks
                            .stream()
                            .filter(r -> r.getRoleId() == roleValue.getRoleId())
                            .map(r -> r.getVariableId())
                            .collect(Collectors.toList());
                    // retain the subset of entities corresponding to the selected variable ID; put them in the combo box
                    Entity[] entitySubset = PopupMulti.this.entities
                            .stream()
                            .filter(e -> e.getVariableId() == roleValue.getVariableId())
                            .toArray(Entity[]::new);
                    JComboBox<Entity> box = new JComboBox<>(entitySubset);
                    box.setRenderer(new EntityComboBoxRenderer());
                    box.setEditable(PopupMulti.this.isEditable());
                    box.setEnabled(PopupMulti.this.isEditable());
                    box.getModel().setSelectedItem(roleValue.getValue());
                    box.setPreferredSize(new Dimension(PopupMulti.this.textFieldWidth, 22));

                    // variable selection box: find those variables that belong to the current role and put them in a variable box
                    Variable[] eligibleVariables = PopupMulti.this.roleVariableLinks
                            .stream()
                            .filter(rvl -> rvl.getRoleId() == roleValue.getRoleId())
                            .map(rvl -> PopupMulti.this.variables
                                    .stream()
                                    .filter(v -> v.getVariableId() == rvl.getVariableId())
                                    .sorted()
                                    .findFirst()
                                    .get())
                            .toArray(Variable[]::new);
                    JComboBox<Variable> variableBox = new JComboBox<Variable>(eligibleVariables);
                    variableBox.setSelectedItem(PopupMulti.this.variables
                            .stream()
                            .filter(v -> v.getVariableId() == roleValue.getVariableId())
                            .findFirst()
                            .get());
                    // item listener for the variable box: if a different variable is selected, set the empty entity corresponding to this variable in the entity box -- unless there is an entity with the same text as previously, then select this one
                    variableBox.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent itemEvent) {
                            Entity entity = (Entity) box.getSelectedItem();
                            Variable v = (Variable) ((JComboBox) itemEvent.getSource()).getSelectedItem();
                            if (entity.getVariableId() != v.getVariableId()) {
                                Entity[] matches = PopupMulti.this.entities
                                        .stream()
                                        .filter(e -> e.getVariableId() == v.getVariableId() && e.getValue().equals(entity.getValue()))
                                        .toArray(Entity[]::new);
                                Entity newEntity;
                                // if there is an equivalent entity with the same text as before, use this match as the new entity
                                if (matches.length > 0) {
                                    newEntity = matches[0];
                                } else { // otherwise find the empty entity belonging to the newly set variable and use this
                                    newEntity = PopupMulti.this.entities
                                            .stream()
                                            .filter(e -> e.getValue().equals("") && e.getVariableId() == v.getVariableId())
                                            .findFirst()
                                            .get();
                                }
                                // reset the entity box, both the items that can be selected and the selected item
                                box.removeAllItems();
                                PopupMulti.this.entities
                                        .stream()
                                        .filter(e -> e.getVariableId() == v.getVariableId())
                                        .forEachOrdered(e -> box.addItem(e));
                                box.setSelectedItem(newEntity);
                                // save the newly set entity also in the current role value and reset the revert and save buttons
                                RoleValuePanel.this.ts.getRoleValues().get(finalI1).setVariableId(v.getVariableId());
                                RoleValuePanel.this.ts.getRoleValues().get(finalI1).setVariableName(v.getVariableName());
                                RoleValuePanel.this.ts.getRoleValues().get(finalI1).setRoleVariableLinkId(PopupMulti.this.roleVariableLinks
                                        .stream()
                                        .filter(l -> l.getRoleId() == RoleValuePanel.this.ts.getRoleValues().get(finalI1).getRoleId() && l.getVariableId() == v.getVariableId())
                                        .findFirst()
                                        .get()
                                        .getId());
                                RoleValuePanel.this.ts.getRoleValues().get(finalI1).setValue(newEntity);
                                toggleButtons();
                            }
                        }
                    });
                    // only show and enable the variable box if multiple variables are associated with the current role
                    variableBox.setEnabled(eligibleVariables.length > 1);
                    variableBox.setVisible(eligibleVariables.length > 1);

                    // paint the selected value in the attribute color
                    String s = ((JTextField) box.getEditor().getEditorComponent()).getText();
                    Color fg = javax.swing.UIManager.getColor("TextField.foreground"); // default unselected foreground color of JTextField
                    for (int j = 0; j < box.getModel().getSize(); j++) {
                        if (s.equals(box.getModel().getElementAt(j).getValue())) {
                            fg = box.getModel().getElementAt(j).getColor().toAWTColor();
                            break;
                        }
                    }
                    ((JTextField) box.getEditor().getEditorComponent()).setSelectedTextColor(fg);
                    box.getEditor().getEditorComponent().setForeground(fg);

                    // add a document listener to the combo box to paint the selected value in the attribute color, despite being highlighted
                    ((JTextField) box.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            updateEntry();
                        }
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            updateEntry();
                        }
                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            updateEntry();
                        }

                        /**
                         * Retrieve the selected entity from the combo box (or turn the entered String into an Entity)
                         * and set it as the entity in the current role value of the table statement.
                         */
                        private void updateEntry() {
                            Entity entity = null;
                            if (box.getSelectedItem() == null) {
                                // do nothing if there is no item in the entity box; this can happen if the item is replaced by the variable box item listener
                            } else {
                                if (box.getSelectedItem().getClass().getName().endsWith("Entity")) {
                                    entity = (Entity) box.getSelectedItem(); // select the current entity from the box if the box has an entity selected, rather than a string
                                } else { // if it's a string, convert it into an entity and use it, but only the first 190 characters
                                    String currentText = (String) box.getSelectedItem();
                                    if (currentText.length() > 0 && currentText.matches("^\\s+$")) { // replace a (multiple) whitespace string by an empty string
                                        currentText = "";
                                    }
                                    if (currentText.length() > 191) {
                                        currentText = currentText.substring(0, 190);
                                        box.setSelectedItem(currentText); // ensure the text in the box is never longer than 190 characters to comply with old MySQL requirements
                                    }
                                    entity = new Entity(-1, roleValue.getVariableId(), currentText, new model.Color(0, 0, 0));
                                }
                                // save the selected entity in the modified table statement and update revert and save buttons
                                RoleValuePanel.this.ts.getRoleValues().get(finalI1).setValue(entity);
                                toggleButtons();
                            }
                        }
                    });

                    // use auto-complete decoration (depending on coder setting)
                    if (coder.isPopupAutoComplete()) {
                        AutoCompleteDecorator.decorate(box); // auto-complete short text values; part of SwingX
                    }

                    gbc.anchor = GridBagConstraints.EAST;
                    JLabel label = new JLabel(roleValue.getRoleName(), JLabel.TRAILING);
                    label.setEnabled(PopupMulti.this.isEditable());
                    this.add(label, gbc);
                    gbc.gridx++;
                    gbc.weightx = 1.0;
                    gbc.anchor = GridBagConstraints.WEST;
                    this.add(box, gbc);

                    // variable box
                    gbc.gridx++;
                    this.add(variableBox, gbc);
                    gbc.weightx = 0;
                } else if (roleValue.getDataType().equals("long text")) {
                    String entry = (String) roleValue.getValue();
                    JTextArea box = new JTextArea();
                    box.setEditable(PopupMulti.this.isEditable());
                    box.setEnabled(PopupMulti.this.isEditable());
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
                            toggleButtons();
                        }
                    });

                    gbc.anchor = GridBagConstraints.NORTHEAST;
                    JLabel label = new JLabel(roleValue.getRoleName(), JLabel.TRAILING);
                    label.setEnabled(PopupMulti.this.isEditable());
                    this.add(label, gbc);
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
                    buttons.setEnabled(PopupMulti.this.isEditable());

                    ChangeListener cl = new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent changeEvent) {
                            if (buttons.isYes()) {
                                RoleValuePanel.this.ts.getRoleValues().get(finalI1).setValue(1);
                            } else {
                                RoleValuePanel.this.ts.getRoleValues().get(finalI1).setValue(0);
                            }
                            toggleButtons();
                        }
                    };
                    buttons.getYesButton().addChangeListener(cl);
                    buttons.getNoButton().addChangeListener(cl);

                    gbc.anchor = GridBagConstraints.EAST;
                    gbc.insets = new Insets(3,3,3,2);
                    JLabel label = new JLabel(roleValue.getRoleName(), JLabel.TRAILING);
                    label.setEnabled(PopupMulti.this.isEditable());
                    this.add(label, gbc);
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
                    jsp.setEnabled(PopupMulti.this.isEditable());

                    jsp.addChangeListener(cl -> {
                        RoleValuePanel.this.ts.getRoleValues().get(finalI1).setValue(jsp.getValue());
                        toggleButtons();
                    });

                    gbc.anchor = GridBagConstraints.EAST;
                    JLabel label = new JLabel(roleValue.getRoleName(), JLabel.TRAILING);
                    label.setEnabled(PopupMulti.this.isEditable());
                    this.add(label, gbc);
                    gbc.insets = new Insets(0, 0, 0, 0);
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.gridx++;
                    this.add(jp, gbc);
                    gbc.insets = new Insets(3, 3, 3, 3);
                }

                int finalI = i;
                long num = this.ts.getRoleValues()
                        .stream()
                        .filter(r -> r.getRoleId() == this.ts.getRoleValues().get(finalI).getRoleId())
                        .count();

                // column 4: add a new row
                gbc.insets = new Insets(3, 2, 3, 5);
                gbc.gridx = 3;
                ImageIcon addIcon = new SvgIcon("/icons/tabler_plus.svg", 16).getImageIcon();
                JButton addButton = new JButton(addIcon);
                addButton.setMargin(new Insets(0, 0, 0, 0));
                addButton.setContentAreaFilled(false);
                addButton.addActionListener(actionEvent -> {
                    RoleValue duplicate = new RoleValue(this.ts.getRoleValues().get(finalI));
                    // replace value in duplicate by default value
                    if (this.ts.getRoleValues().get(finalI).getDataType().equals("short text")) {
                        // replace value by the entity corresponding to an empty string
                        duplicate.setValue(PopupMulti.this.entities
                                .stream()
                                .filter(e -> (e.getVariableId() == defaultVariableId && e.getValue().equals("")))
                                .findFirst()
                                .get());
                    } else if (this.ts.getRoleValues().get(finalI).getDataType().equals("long text")) {
                        duplicate.setValue("");
                    } else if (this.ts.getRoleValues().get(finalI).getDataType().equals("boolean")) {
                        duplicate.setValue(1);
                    } else if (this.ts.getRoleValues().get(finalI).getDataType().equals("integer")) {
                        duplicate.setValue(0);
                    }
                    this.addRoleValue(duplicate);
                    toggleButtons();
                });
                int max = PopupMulti.this.roleMap.get(this.ts.getRoleValues().get(i).getRoleId()).getNumMax();
                addButton.setEnabled(max > num && PopupMulti.this.isEditable());
                addButton.setVisible(max > num);
                addButton.setToolTipText("add another " + this.ts.getRoleValues().get(i).getRoleName() + " to the statement");
                this.add(addButton, gbc);

                // column 5: remove the roleValue row
                gbc.insets = new Insets(3, 0, 3, 5);
                gbc.gridx = 4;
                ImageIcon removeIcon = new SvgIcon("/icons/tabler_minus.svg", 16).getImageIcon();
                JButton removeButton = new JButton(removeIcon);
                removeButton.setMargin(new Insets(0, 0, 0, 0));
                removeButton.setContentAreaFilled(false);
                removeButton.addActionListener(actionEvent -> {
                    removeRoleValue(finalI);
                    toggleButtons();
                });
                int min = PopupMulti.this.roleMap.get(this.ts.getRoleValues().get(i).getRoleId()).getNumMin();
                removeButton.setEnabled(min < num && PopupMulti.this.isEditable());
                removeButton.setVisible(min < num);
                removeButton.setToolTipText("remove this entity from the statement");
                this.add(removeButton, gbc);
            }

            this.repaint();
            PopupMulti.this.pack();
        }

        /**
         * Add a role/value to the statement and layout.
         *
         * @param added An added role-value object.
         */
        void addRoleValue(RoleValue added) {
            this.ts.getRoleValues().add(added);
            this.ts.setRoleValues(this.ts.getRoleValues()
                    .stream()
                    .sorted(Comparator.comparing(v -> roleMap.get(v.getRoleId()).getPosition()))
                    .collect(Collectors.toCollection(ArrayList::new)));
            this.rebuildLayout();
        }

        /**
         * Remove a role/value from the statement and layout.
         *
         * @param index The index among the {@link RoleValue} objects in the statement.
         */
        void removeRoleValue(int index) {
            this.ts.getRoleValues().remove(index);
            this.rebuildLayout();
        }
    }
}