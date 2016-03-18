package dna;

import java.awt.Container;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import dna.panels.CoderPanel;
import dna.panels.EditDocumentPanel;

@SuppressWarnings("serial")
public class LeftPanel extends JScrollPane {
	CoderPanel coderPanel;
    EditDocumentPanel editDocPanel;

	public LeftPanel() {
		this.setPreferredSize(new Dimension(260, 440));
		JXTaskPaneContainer tpc = new JXTaskPaneContainer();
		tpc.setBackground(this.getBackground());
		this.setColumnHeaderView(tpc);
		
		// coder panel
		JXTaskPane coderVisibilityTaskPane = new JXTaskPane();
		ImageIcon groupIcon = new ImageIcon(getClass().getResource("/icons/group.png"));
		coderVisibilityTaskPane.setName("Coder");
		coderVisibilityTaskPane.setTitle("Coder");
		coderVisibilityTaskPane.setIcon(groupIcon);
		coderVisibilityTaskPane.setCollapsed(false);
		((Container)tpc).add(coderVisibilityTaskPane);

		coderPanel = new CoderPanel();
		coderVisibilityTaskPane.add(coderPanel);
		
        // document properties panel
        JXTaskPane docDetailsTaskPane = new JXTaskPane();
        editDocPanel = new EditDocumentPanel();
        ImageIcon docDetailsIcon = new ImageIcon(getClass().getResource("/icons/table_edit.png"));
        docDetailsTaskPane.setName("Document meta-data");
        docDetailsTaskPane.setTitle("Document meta-data");
        
        docDetailsTaskPane.setIcon(docDetailsIcon);
        docDetailsTaskPane.setCollapsed(false);
        docDetailsTaskPane.add(editDocPanel);
        ((Container)tpc).add(docDetailsTaskPane);
        
	}

	public void setComboEnabled(boolean enabled) {
		coderPanel.setComboEnabled(enabled);
	}

}
