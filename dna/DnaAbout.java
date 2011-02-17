package dna;

import java.awt.*;
import javax.swing.*;

public class DnaAbout extends JFrame {
    
    /**
	 * This class shows an about window with instructions.
	 * 
	 * @author Philip Leifeld
     * @version 1.13 - 30 October 2009
	 */
	JPanel aboutInhalt;
    JEditorPane aboutText;
    JScrollPane aboutScrollLeiste;
    
    public DnaAbout() {
        this.setTitle("About DNA");
        ImageIcon dna32Icon = new ImageIcon(getClass().getResource(
            "/icons/dna32.png"));
        this.setIconImage(dna32Icon.getImage());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setPreferredSize(new Dimension(580, 440));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
        Icon dnaTextIcon = new ImageIcon(getClass().getResource("/icons/dna32text.png"));
        JLabel dnaIcon = new JLabel(dnaTextIcon);
        JPanel dnaIconPanel = new JPanel( new FlowLayout() );
        dnaIconPanel.add(dnaIcon);
        
        aboutInhalt = new JPanel( new BorderLayout() );
        aboutText = new JEditorPane();
        aboutText.setContentType("text/html");
        aboutText.setText("<p>Current version: 1.13 (30 October 2009)</p>"
            + "<p><b>Copyright</b> by Philip Leifeld (Max Planck Institute for "
            + "Research on Collective Goods, Bonn, Germany)</p>"
            + "<p><b>DNA homepage:</b> <a href=\"http://www.philipleifeld.de\">"
            + "http://www.philipleifeld.de</a><br/>Documentation, publications "
            + "and a newsletter can be found on this homepage.</p>"
            + "<p>Icons taken from <a href=\"http://www.famfamfam.com/lab/icons/silk/\">"
            + "http://www.famfamfam.com/lab/icons/silk/</a>.</p>"
            + "<p><b>Quick start:</b></p>"
            + "<p><u>Coding window</u>: Use this window to create new entries "
            + "for newspaper articles. Fill in the date and title of an article "
            + "and insert the article text into the text area by pressing Ctrl-V. "
            + "Highlight some text and assign tags by using the right mouse button.</p>"
            + "<p><u>Export window</u>: Open the export window using the toolbar icon. "
            + "Set the two classes to consider (e.g. organizations x categories "
            + "for a bipartite/affiliation network or organizations x organizations "
            + "for a simple graph based on co-occurrences). Then click on export.</p>"
            );
        aboutText.setEditable(false);
        aboutScrollLeiste = new JScrollPane(aboutText);
        aboutScrollLeiste.setPreferredSize(new Dimension(580, 440));
        aboutInhalt.add(dnaIconPanel, BorderLayout.NORTH);
        aboutInhalt.add(aboutScrollLeiste, BorderLayout.CENTER);
        this.add(aboutInhalt);
    }
    
}