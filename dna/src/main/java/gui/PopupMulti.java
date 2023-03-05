package gui;

import dna.Dna;
import model.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class PopupMulti extends JDialog {
    private static final long serialVersionUID = 6731530941239032349L;
    private final Container c;
    private final Point los;
    private final Color color;
    private final boolean windowDecoration, editable;
    private final int textFieldWidth;
    TableStatement tableStatement;
    private final Coder coder;
    private JComboBox<Coder> coderComboBox;
    private JPanel gridBagPanel;
    private JButton duplicate, remove;
    private JButton cancelButton, saveButton;
    // private ArrayList<RoleValue> roleValues;
    ArrayList<RoleVariableLink> roleVariableLinks;
    ArrayList<Entity> entities;

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
        // this.roleValues = this.tableStatement.getRoleValues();
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

        // should the changes in the statements be saved? check permissions...
        editable = coder.isPermissionEditStatements() && ((tableStatement.getCoderId() == coder.getId() || (coder.isPermissionEditOthersStatements())) && coder.isPermissionEditOthersStatements(tableStatement.getCoderId()));

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

        ImageIcon duplicateIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-copy.png"))).getImage().getScaledInstance(h, h, Image.SCALE_SMOOTH));
        duplicate = new JButton(duplicateIcon);
        duplicate.setToolTipText("create a copy of this statement at the same location");
        duplicate.setPreferredSize(new Dimension(h, h));
        duplicate.setEnabled(coder.isPermissionAddStatements());

        ImageIcon removeIcon = new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/tabler-icon-trash.png"))).getImage().getScaledInstance(h, h, Image.SCALE_SMOOTH));
        remove = new JButton(removeIcon);
        remove.setToolTipText("completely remove the whole statement (but keep the text)");
        remove.setPreferredSize(new Dimension(h, h));
        remove.setEnabled(coder.isPermissionDeleteStatements());
        if (this.tableStatement.getCoderId() != coder.getId() && !Dna.sql.getActiveCoder().isPermissionEditOthersStatements()) {
            remove.setEnabled(false);
        }
        if (this.tableStatement.getCoderId() != coder.getId() && !coder.isPermissionEditOthersStatements(tableStatement.getCoderId())) {
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

        idAndPositionPanel.add(duplicate);
        idAndPositionPanel.add(remove);

        titleDecorationPanel.add(idAndPositionPanel, BorderLayout.EAST);
        titleDecorationPanel.add(typeLabel, BorderLayout.CENTER);
        titleDecorationPanel.add(sep, BorderLayout.SOUTH);
        titleDecorationPanel.add(colorPanel, BorderLayout.WEST);
        contentsPanel.add(titleDecorationPanel, BorderLayout.NORTH);

        // role-value table starts here

        entities = Dna.sql.getEntities(this.tableStatement.getStatementTypeId());
        roleVariableLinks = Dna.sql.getRoleVariableLinks(this.tableStatement.getStatementTypeId());

        RoleValueTableModel model = new RoleValueTableModel();
        JTable roleValueTable = new JTable(model);
        RoleValueCellRenderer roleValueCellRenderer = new RoleValueCellRenderer();
        roleValueTable.setDefaultRenderer(RoleValue.class, roleValueCellRenderer);
        roleValueTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        roleValueTable.setShowGrid(false);
        roleValueTable.getTableHeader().setReorderingAllowed(false);
        roleValueTable.setRowSelectionAllowed(false);
        roleValueTable.setColumnSelectionAllowed(false);
        roleValueTable.setTableHeader(null);
        JScrollPane scrollPane = new JScrollPane(roleValueTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(roleValueTable);

        // TODO: write a table cell editor to show and edit the combo box for the entities; show spinner and radio buttons for integer and binary and text field for long text; add delete buttons to third column; improve visual layout of table

        contentsPanel.add(scrollPane, BorderLayout.CENTER);

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

            this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            JLabel roleLabel = new JLabel(roleValue.getRoleName());
            roleLabel.setPreferredSize(new Dimension(200, 16));
            this.add(roleLabel);
            this.add(box);
        }
    }

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
}
