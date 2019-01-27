package dna;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

@SuppressWarnings("serial")
public class AboutWindow extends JDialog {
    
    /**
	 * This class shows an about window with instructions.
	 * 
	 * @author Philip Leifeld
	 */
	JPanel aboutInhalt;
    JEditorPane aboutText;
    JScrollPane aboutScrollLeiste;
    
    public AboutWindow(String version, String date) {
        this.setTitle("About DNA");
        ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
        this.setIconImage(dna32Icon.getImage());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setPreferredSize(new Dimension(420, 360));
        
        Icon dnaTextIcon = new ImageIcon(getClass().getResource("/icons/dna32text.png"));
        JLabel dnaIcon = new JLabel(dnaTextIcon);
        JPanel dnaIconPanel = new JPanel( new FlowLayout() );
        dnaIconPanel.add(dnaIcon);
        
        aboutInhalt = new JPanel( new BorderLayout() );
        aboutText = new JEditorPane();
        aboutText.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        aboutText.setEditable(false);
        
        aboutText.setText("<p><b>Current version</b><br>" + version + " (" + date + ")</p>"
            + "<p><b>DNA project homepage</b><br>"
            + "Documentation, source code, publications, a forum, and a bug tracker can be found "
            + "here: <a href=\"http://github.com/leifeld/dna/\">http://github.com/leifeld/dna/</a></p>"
            + "<p><b>Icons</b><br> taken from <a href=\"http://www.famfamfam.com/lab/icons/silk/\">"
            + "http://www.famfamfam.com/lab/icons/silk/</a>.</p>"
            + "<p><b>JRI</b><br>To display output in R, this software project uses JRI by Simon Urbanek (under LGPL license), "
            + "which can be downloaded at <a href=\"https://github.com/s-u/rJava\">https://github.com/s-u/rJava</a>.</p>"
            );
        
        aboutText.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                	if(Desktop.isDesktopSupported()) {
                	    try {
							Desktop.getDesktop().browse(e.getURL().toURI());
						} catch (IOException e1) {
							System.out.println("URL cannot be opened.");
						} catch (URISyntaxException e1) {
							System.out.println("URL cannot be opened.");
						}
                	}
                }
            }
        });
        
        aboutScrollLeiste = new JScrollPane(aboutText);
        aboutScrollLeiste.setPreferredSize(new Dimension(580, 340));
        aboutInhalt.add(dnaIconPanel, BorderLayout.NORTH);
        aboutInhalt.add(aboutScrollLeiste, BorderLayout.CENTER);
        this.add(aboutInhalt);
        
		// set location and pack window
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setResizable(false);
    }
}