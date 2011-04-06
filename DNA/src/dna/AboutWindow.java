package dna;

import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class AboutWindow extends JFrame {
    
    /**
	 * This class shows an about window with instructions.
	 * 
	 * @author Philip Leifeld
     * @version 1.27e1 - 2011-04-06
	 */
	JPanel aboutInhalt;
    JEditorPane aboutText;
    JScrollPane aboutScrollLeiste;
    
    public AboutWindow() {
        this.setTitle("About DNA");
        ImageIcon dna32Icon = new ImageIcon(getClass().getResource(
            "/icons/dna32.png"));
        this.setIconImage(dna32Icon.getImage());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setPreferredSize(new Dimension(400, 400));
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
        aboutText.setText("<p><b>Current version</b><br>1.27e1 (April 6, 2011)</p>"
            + "<p><b>Copyright</b><br> Philip Leifeld (Max Planck Institute for "
            + "Research on Collective Goods, Bonn, Germany)</p>"
            + "<p><b>DNA homepage</b><br> <a href=\"http://www.philipleifeld.de\">"
            + "http://www.philipleifeld.de</a><br/>Documentation, publications "
            + "and a mailing list can be found on this homepage.</p>"
            + "<p><b>Contact</b><br><a href=\"mailto:Leifeld@coll.mpg.de\">Leifeld@coll.mpg.de</a></p>"
            + "<p><b>Icons</b><br> taken from <a href=\"http://www.famfamfam.com/lab/icons/silk/\">"
            + "http://www.famfamfam.com/lab/icons/silk/</a>.</p>"
            );
        aboutText.setEditable(false);
        aboutScrollLeiste = new JScrollPane(aboutText);
        aboutScrollLeiste.setPreferredSize(new Dimension(580, 440));
        aboutInhalt.add(dnaIconPanel, BorderLayout.NORTH);
        aboutInhalt.add(aboutScrollLeiste, BorderLayout.CENTER);
        this.add(aboutInhalt);
    }
    
}