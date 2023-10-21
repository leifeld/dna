package gui;

import model.Entity;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class EntityPanel extends JPanel {

    public EntityPanel() {
        createLayout();
    }

    private void createLayout() {
        this.setLayout(new BorderLayout());

        setBorder(new EmptyBorder(5, 7, 5, 5));

        HashMap<String, String> hm = new HashMap<>();
        hm.put("alias", "");
        hm.put("type", "");
        hm.put("notes", "");
        Entity r = new Entity(0, 0, "variables", new model.Color(0, 0, 0), -1, false, hm);
        EntityTreeTableModel m = new EntityTreeTableModel(r);
        JXTreeTable tt2 = new JXTreeTable(m);
        EntityTreeCellRenderer entityRenderer = new EntityTreeCellRenderer();
        tt2.setDefaultRenderer(Color.class, entityRenderer);
        tt2.setDefaultRenderer(String.class, entityRenderer);
        CustomTreeCellRenderer treeRenderer = new CustomTreeCellRenderer();
        tt2.setTreeCellRenderer(treeRenderer);
        tt2.setRowSelectionAllowed(true);
        tt2.setColumnSelectionAllowed(false);
        tt2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m.addEntity(new Entity(-1, 1, "person", new model.Color(0, 0, 0), 0, false, hm));
        m.addEntity(new Entity(1, 1, "person 1", new model.Color(0, 255, 0), -1, false, hm));
        m.addEntity(new Entity(2, 1, "person 2", new model.Color(255, 200, 0), -1, false, hm));
        m.addEntity(new Entity(3, 1, "person 3", new model.Color(128, 128, 128), 2, false, hm));
        m.addEntity(new Entity(-2, 2, "organization", new model.Color(0, 0, 0), 0, false, hm));
        m.addEntity(new Entity(4, 2, "organization 1", new model.Color(0, 255, 0), -2, false, hm));
        m.addEntity(new Entity(5, 2, "organization 2", new model.Color(255, 200, 0), -2, false, hm));
        m.addEntity(new Entity(6, 2, "organization 3", new model.Color(128, 128, 128), 4, false, hm));
        JScrollPane scrollPane = new JScrollPane(tt2);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

        private final ImageIcon variableIcon = new SvgIcon("/icons/tabler_player_stop_filled.svg", 16).getImageIcon();
        private final ImageIcon blackIcon = new SvgIcon("/icons/tabler_player_record_filled.svg", 16).getImageIcon();
        private final ImageIcon redIcon = new SvgIcon("/icons/tabler_player_record_filled_red.svg", 16).getImageIcon();
        private final Color notInDatabaseColor = new Color(255, 102, 102);

        public CustomTreeCellRenderer() {
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            Entity entity = (Entity) value;
            setText(entity.getValue());
            if (entity.getId() < 0) {
                setForeground(Color.BLACK);
                setClosedIcon(variableIcon);
                setLeafIcon(variableIcon);
                setOpenIcon(variableIcon);
            } else if (entity.isInDatabase()) {
                setForeground(Color.BLACK);
                setClosedIcon(blackIcon);
                setLeafIcon(blackIcon);
                setOpenIcon(blackIcon);
            } else {
                setForeground(notInDatabaseColor);
                setClosedIcon(redIcon);
                setLeafIcon(redIcon);
                setOpenIcon(redIcon);
            }
            return this;
        }
    }

    public class EntityTreeCellRenderer extends JLabel implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Color defaultColor = javax.swing.UIManager.getColor("Table.background");
            Color selectedColor = javax.swing.UIManager.getColor("Table.selectionBackground");
            Color notInDatabaseColor = new Color(255, 102, 102);

            setOpaque(true);

            Entity e = (Entity) table.getValueAt(row, 0);

            if (value instanceof Entity) {
                setText(((Entity) value).getValue());
                if (!((Entity) value).isInDatabase()) {
                    setForeground(notInDatabaseColor);
                }
            }

            if (value instanceof Color) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JButton colorButton = (new JButton() {
                    private static final long serialVersionUID = 1648028274961429514L;
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(e.getColor().toAWTColor());
                        g.fillRect(0, 0, 20, 8);
                    }
                });
                colorButton.setPreferredSize(new Dimension(20, 8));
                colorButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                colorButton.setEnabled(false);
                panel.add(colorButton);
                panel.setOpaque(true);
                if (isSelected) {
                    panel.setBackground(selectedColor);
                } else {
                    panel.setBackground(defaultColor);
                }
                return panel;
            }

            if (value instanceof String) {
                if (!e.isInDatabase()) {
                    setForeground(notInDatabaseColor);
                }

                if (isSelected) {
                    setBackground(selectedColor);
                } else {
                    setBackground(defaultColor);
                }
            }

            return this;
        }
    }

    public static class EntityTreeTableModel extends AbstractTreeTableModel {
        private final String[] columns;
        HashMap<Integer, ArrayList<Integer>> entityChildrenMap; // entity ID to children entities' IDs
        HashMap<Integer, Entity> entities;

        public EntityTreeTableModel(Entity root) {
            super(root);
            this.columns = root.getAttributeValues().keySet().stream().sorted().toArray(String[]::new);
            entities = new HashMap<>();
            root.setId(0);
            entities.put(root.getId(), root);
            entityChildrenMap = new HashMap<>();
            entityChildrenMap.put(root.getId(), new ArrayList<>());
            modelSupport.fireNewRoot();
        }

        private Object[] getPathToRoot(Entity node) {
            ArrayList<Object> items = new ArrayList<>();
            while (node != null) {
                items.add(node);
                node = entities.get(node.getChildOf());
            }
            Collections.reverse(items);
            return items.toArray();
        }

        void addEntity(Entity entity) {
            entities.put(entity.getId(), entity);entityChildrenMap.put(entity.getId(), new ArrayList<>());
            if (entityChildrenMap.containsKey(entity.getChildOf())) {
                entityChildrenMap.get(entity.getChildOf()).add(entity.getId());
            } else {
                entityChildrenMap.put(entity.getChildOf(), new ArrayList<>());
                entityChildrenMap.get(entity.getChildOf()).add(entity.getId());
                Collections.sort(entityChildrenMap.get(entity.getChildOf()));
            }
            modelSupport.fireNewRoot();
        }

        void removeEntities(int[] entityIds) {
            for (int entityId : entityIds) {
                Entity entity = entities.get(entityId);
                if (entity != null) {
                    int parent = entity.getChildOf();
                    entityChildrenMap.get(parent).remove(Integer.valueOf(entityId));
                    entities.remove(entityId);
                }
            }
        }

        @Override
        public Object getRoot() {
            if (entities.isEmpty()) {
                return null;
            } else {
                return entities.get(0);
            }
        }

        @Override
        public boolean isCellEditable(Object node, int column) {
            return false;
        }

        @Override
        public int getColumnCount() {
            return columns.length + 2;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "entity";
            } else if (column == 1) {
                return "color";
            } else {
                return columns[column - 2];
            }
        }

        @Override
        public Object getValueAt(Object node, int column) {
            Entity entity = (Entity) node;
            if (column == 0) {
                return entity;  // return the Entity object for the tree column
            } else if (column == 1) {
                return entity.getColor();
            } else {
                String columnName = columns[column - 2];
                return entity.getAttributeValues().get(columnName);
            }
        }

        @Override
        public Object getChild(Object parent, int index) {
            int childId = this.entityChildrenMap.get(((Entity) parent).getId()).get(index);
            return entities.get(childId);
        }

        @Override
        public int getChildCount(Object parent) {
            return this.entityChildrenMap.get(((Entity) parent).getId()).size();
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            int parentId = ((Entity) parent).getId();
            int childId = ((Entity) child).getId();
            for (int i = 0; i < entityChildrenMap.get(parentId).size(); i++) {
                if (entityChildrenMap.get(parentId).get(i).equals(childId)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public boolean isLeaf(Object node) {
            return !entityChildrenMap.containsKey(((Entity) node).getId()) || entityChildrenMap.get(((Entity) node).getId()).size() == 0;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return Entity.class;
            } else if (column == 1) {
                return Color.class;
            } else {
                return String.class;
            }
        }
    }
}
