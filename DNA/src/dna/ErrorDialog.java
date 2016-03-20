package dna;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

@SuppressWarnings("serial")
public class ErrorDialog extends JDialog {
    
    /**
	 * This class shows a window with an error message.
	 * 
	 * @author Philip Leifeld
	 */
	JPanel aboutInhalt;
    JEditorPane aboutText;
    JScrollPane aboutScrollLeiste;
    
    public ErrorDialog(String message) {
    	this.setModal(true);
        this.setTitle("Oops...");
        ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
        this.setIconImage(dna32Icon.getImage());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        String date = Dna.dna.date;
        String version = Dna.dna.version;

        JEditorPane instructions;
		instructions = new JEditorPane();
        instructions.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
		instructions.setText(
				"There was an unexpected error. If you would like to contribute to the development of DNA, "
				+ "please copy the error message below (using Ctrl-C) and submit it as a bug report to the DNA issue tracker at "
				+ "<a href=\"http://github.com/leifeld/dna/issues/\">http://github.com/leifeld/dna/issues/</a> "
				+ "or by e-mail to Philip Leifeld (see <a href=\"http://www.philipleifeld.com/contact/\">"
				+ "http://www.philipleifeld.com/contact/</a> "
				+ "for the current e-mail address). If you report this problem, please include (1) the version number ("
				+ version + "), (2) the date of the release (" + date + "), (3) a description of the problem, and (4) the steps "
				+ "necessary to reproduce the problem. Any details might help to improve DNA. Thank you for your support.");
        instructions.setBorder(null);
        instructions.setEditable(false);
        instructions.setBackground(panel.getBackground());
        instructions.setPreferredSize(new Dimension(800, 105));
        instructions.addHyperlinkListener(new HyperlinkListener() {
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
        panel.add(instructions, gbc);
        
        gbc.gridy = 1;
        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setPreferredSize(new Dimension(800, 495));
        JScrollPane messageScroller = new JScrollPane(messageArea);
        messageScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(messageScroller, gbc);
        
        gbc.gridy = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton okButton = new JButton("OK", new ImageIcon(getClass().getResource("/icons/accept.png")));
        okButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
        buttonPanel.add(okButton);
        panel.add(buttonPanel, gbc);
        
        this.add(panel);
        
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}