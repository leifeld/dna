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
import java.awt.event.*;
import java.awt.image.BaseMultiResolutionImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        this.roles = Dna.sql.getRoles();
        this.roles.stream().forEach(role -> roleMap.put(role.getId(), role));
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
                PopupMulti.this.rvp.getModifiedTableStatement().setCoderId(selectedCoder.getId());
                PopupMulti.this.rvp.getModifiedTableStatement().setCoderName(selectedCoder.getName());
                PopupMulti.this.rvp.getModifiedTableStatement().setCoderColor(selectedCoder.getColor());
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
        return !this.rvp.getModifiedTableStatement().equals(this.tableStatement);
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
        TableStatement getModifiedTableStatement() {
            return this.ts;
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
                Variable defaultVariable = PopupMulti.this.variables.stream().filter(v -> v.getVariableId() == defaultVariableId).findFirst().get();
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
                    box.setEditable(PopupMulti.this.isEditable());
                    box.setEnabled(PopupMulti.this.isEditable());
                    box.getModel().setSelectedItem(roleValue.getValue());
                    box.setPreferredSize(new Dimension(PopupMulti.this.textFieldWidth, 22));

                    // variable selection box
                    JComboBox<Variable> variableBox = new JComboBox<Variable>();
                    formatEntry(box, variableBox, roleVariableIds, defaultVariableId, roleValue, defaultVariable, finalI1);

                    variableBox.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent itemEvent) {
                            Variable v = (Variable) ((JComboBox) itemEvent.getSource()).getSelectedItem();
                            if (v != null && itemEvent.getStateChange() == ItemEvent.SELECTED) {
                                Object selectedObject = box.getModel().getSelectedItem();
                                Entity entity = null;
                                if (selectedObject.getClass().getName().endsWith("Entity")) {
                                    entity = (Entity) selectedObject;
                                    int vid = v.getVariableId();
                                    entity.setVariableId(vid);
                                    box.setSelectedItem(entity);
                                } else {
                                    int roleVariableLinkId = PopupMulti.this.roleVariableLinks
                                            .stream()
                                            .filter(rvl -> rvl.getVariableId() == v.getVariableId() &&
                                                    rvl.getRoleId() == RoleValuePanel.this.ts.getRoleValues().get(finalI1).getRoleId())
                                            .findFirst()
                                            .get()
                                            .getId();
                                    RoleValuePanel.this.ts.getRoleValues().get(finalI1).setRoleVariableLinkId(roleVariableLinkId);
                                    RoleValuePanel.this.ts.getRoleValues().get(finalI1).setVariableId(v.getVariableId());
                                    RoleValuePanel.this.ts.getRoleValues().get(finalI1).setVariableName(v.getVariableName());
                                    System.out.println("Was null: " + selectedObject);
                                }
                            }
                        }
                    });

                    // paint the selected value in the attribute color
                    String s = ((JTextField) box.getEditor().getEditorComponent()).getText();
                    Color fg = javax.swing.UIManager.getColor("TextField.foreground"); // default unselected foreground color of JTextField
                    for (int j = 0; j < box.getModel().getSize(); j++) {
                        if (s.equals(box.getModel().getElementAt(j).getValue())) {
                            fg = box.getModel().getElementAt(j).getColor();
                            break;
                        }
                    }
                    ((JTextField) box.getEditor().getEditorComponent()).setSelectedTextColor(fg);
                    box.getEditor().getEditorComponent().setForeground(fg);

                    // add a document listener to the combo box to paint the selected value in the attribute color, despite being highlighted
                    ((JTextField) box.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void changedUpdate(DocumentEvent arg0) {
                            formatEntry(box, variableBox, roleVariableIds, defaultVariableId, roleValue, defaultVariable, finalI1);
                        }
                        @Override
                        public void insertUpdate(DocumentEvent arg0) {
                            formatEntry(box, variableBox, roleVariableIds, defaultVariableId, roleValue, defaultVariable, finalI1);
                        }
                        @Override
                        public void removeUpdate(DocumentEvent arg0) {
                            formatEntry(box, variableBox, roleVariableIds, defaultVariableId, roleValue, defaultVariable, finalI1);
                        }
                    });

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
                long num = this.ts.getRoleValues().stream().filter(r -> r.getRoleId() == this.ts.getRoleValues().get(finalI).getRoleId()).count();

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


        private void formatEntry(JComboBox<Entity> box, JComboBox<Variable> variableBox, java.util.List<Integer> roleVariableIds, int defaultVariableId, RoleValue roleValue, Variable defaultVariable, int i) {
            Color fg = javax.swing.UIManager.getColor("TextField.foreground"); // default unselected foreground color of JTextField
            String currentText = ((JTextField) box.getEditor().getEditorComponent()).getText();
            if (currentText.length() > 0 && currentText.matches("^\\s+$")) { // replace a (multiple) whitespace string by an empty string
                currentText = "";
            }
            if (currentText.length() > 190) {
                currentText = currentText.substring(0, 189);
            }
            final String currentTextFinal = currentText;

            // which existing entities in the combo box match the current editor text?
            Entity[] entityCandidates = PopupMulti.this.entities
                    .stream()
                    .filter(e -> roleVariableIds.contains(e.getVariableId()) && e.getValue().equals(currentTextFinal))
                    .toArray(Entity[]::new);

            Entity chosenEntity = null;
            try {
                chosenEntity = Stream
                        .of(entityCandidates)
                        .filter(e -> e.getVariableId() == defaultVariableId)
                        .findFirst()
                        .get();
                // set contents of variable box
                final Entity chosenEntityFinal = chosenEntity;
                variableBox.removeAllItems();
                variableBox.addItem(PopupMulti.this.variables.stream().filter(v -> v.getVariableId() == chosenEntityFinal.getVariableId()).findFirst().get());
                variableBox.setSelectedIndex(0);
                variableBox.setEnabled(variableBox.getItemCount() > 1);
            } catch (NoSuchElementException e) {
                if (entityCandidates.length > 0) {
                    chosenEntity = entityCandidates[0];
                    // set contents of variable box
                    final Entity chosenEntityFinal = chosenEntity;
                    variableBox.removeAllItems();
                    variableBox.addItem(PopupMulti.this.variables.stream().filter(v -> v.getVariableId() == chosenEntityFinal.getVariableId()).findFirst().get());
                    variableBox.setSelectedIndex(0);
                    variableBox.setEnabled(variableBox.getItemCount() > 1);
                } else {
                    // if the editor text does not match any existing entity, create a new one to be selected and then inserted into the role value of the table statement
                    chosenEntity = new Entity(-1, defaultVariableId, currentTextFinal, Color.BLACK);
                    // set contents of variable box
                    variableBox.removeAllItems();
                    PopupMulti.this.variables
                            .stream()
                            .filter(v -> PopupMulti.this.roleVariableLinks
                                    .stream()
                                    .filter(rvl -> rvl.getVariableId() == v.getVariableId() && rvl.getRoleId() == roleValue.getRoleId())
                                    .count() > 0)
                            .forEachOrdered(variableBox::addItem);
                    variableBox.setSelectedItem(defaultVariable);
                    variableBox.setEnabled(variableBox.getItemCount() > 1);


                    /*
                    TODO:
                     - The variable box must determine which entities are loaded into the entity box.
                     - When a statement is opened and a role entry has an entity already, then set the variable box to the variable corresponding to the entity and put the values only for this variable into the entity box.
                     - When a statement is opened and a role entry is empty, then multiple variables may be eligible. But in this case, the default variable was already selected upon statement creation, so the case is like above.
                     - When the user selects a different existing entity, nothing needs changing because the correct variable is already selected.
                     - Changes when populating the two boxes initially:
                        i) populate each field with only the current role AND variable entities
                        ii) select the entity from the database initially when statement is opened
                        iii) populate variable box with all variables that are compatible with the role
                        iv) select the variable that belongs to the selected entity
                        v) disabled the variable box if there is only one eligible variable for a role, otherwise always enable
                        vi) set variable box invisible if coder setting requires this; otherwise always visible
                     - Changes in entity box listener:
                        i) when entity is selected, update it in the role-value entry of the table statement and nothing else
                        ii) if a new value is entered, create a new entity and put the current variable into the new entity, then update in table statement role value
                     - Changes in variable box listener:
                        i) when variable is selected and the current entity does not exist in the outer class environment of the popup, change the variable in the current entity and table statement to the newly selected variable
                        ii) when variable is selected and the current value in the entity box is a String (does this case ever happen?), convert to entity and set variable to newly selected variable, also change in table statement
                        iii) when variable is selected and the entity already exists in the list, reset entity field to the empty value/entity for the newly selected variable
                     - Changes in the revert button:
                        i) also revert the variable box (before resetting the entity box to the original value)
                     */




                }
            }

            int variableId = chosenEntity.getVariableId();
            RoleValuePanel.this.ts.getRoleValues().get(i).setVariableId(variableId);
            Variable currentVariable = PopupMulti.this.variables.stream().filter(v -> v.getVariableId() == variableId).findFirst().get();
            RoleValuePanel.this.ts.getRoleValues().get(i).setVariableName(currentVariable.getVariableName());
            RoleValuePanel.this.ts.getRoleValues().get(i).setRoleVariableLinkId(PopupMulti.this.roleVariableLinks.stream().filter(l -> l.getVariableId() == variableId).findFirst().get().getId());
            RoleValuePanel.this.ts.getRoleValues().get(i).setValue(new Entity(chosenEntity));
            toggleButtons();
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