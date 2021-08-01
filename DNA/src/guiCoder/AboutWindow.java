package guiCoder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	JPanel aboutContents;
	JEditorPane aboutText;
	JScrollPane aboutScrollPane;

	public AboutWindow(String version, String date) {
		this.setTitle("About DNA");
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
		this.setIconImage(dna32Icon.getImage());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		aboutContents = new JPanel( new BorderLayout() );

		Icon dnaTextIcon = new ImageIcon(getClass().getResource("/icons/dna32text.png"));
		JLabel dnaIcon = new JLabel(dnaTextIcon);
		JPanel dnaIconPanel = new JPanel( new FlowLayout() );
		dnaIconPanel.add(dnaIcon);
		aboutContents.add(dnaIconPanel, BorderLayout.NORTH);

		aboutText = new JEditorPane();
		aboutText.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
		aboutText.setEditable(false);
		aboutText.setText("<p><b>Current version</b><br>" + version + " (" + date + ")</p>"
				+ "<p><b>DNA project homepage</b><br>"
				+ "Documentation, source code, publications, a forum, and a bug tracker can be found "
				+ "here: <a href=\"http://github.com/leifeld/dna/\">http://github.com/leifeld/dna/</a>.</p>"
				+ "<p><b>Icons</b><br> taken from <a href=\"https://tabler-icons.io/\">"
				+ "https://tabler-icons.io/</a> (MIT license).</p>"
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
		aboutScrollPane = new JScrollPane(aboutText);
		aboutScrollPane.setPreferredSize(new Dimension(500, 270));
		aboutContents.add(aboutScrollPane, BorderLayout.CENTER);

		// close button at the bottom of the window
		ImageIcon closeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		JButton closeButton = new JButton("Close", closeIcon);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(closeButton);
		aboutContents.add(buttonPanel, BorderLayout.SOUTH);
		
		this.add(aboutContents);

		// set location and pack window
		this.setModal(true);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setResizable(false);
	}
}