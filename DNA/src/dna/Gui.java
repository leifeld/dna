package dna;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Gui extends JFrame {
	
	/**
	 * DNA GUI
	 */
	private static final long serialVersionUID = 6798727706826962027L;
	Container c;
	StatusBar statusBar;
	DocumentPanel documentPanel;
	TextPanel textPanel;

	public Gui() {
		c = getContentPane();
		this.setTitle("Discourse Network Analyzer");
		//this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageIcon dna32Icon = new ImageIcon(getClass().getResource("/icons/dna32.png"));
		this.setIconImage(dna32Icon.getImage());

		//addWindowListener(new WindowAdapter() {
		//	public void windowClosing(WindowEvent e) {
		//		dispose();
		//	}
		//});
		
		documentPanel = new DocumentPanel();
		textPanel = new TextPanel();

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}

	class StatusBar extends JPanel {
		
		private static final long serialVersionUID = 1L;
		JLabel currentFileLabel, loading;
		
		public StatusBar() {
			this.setLayout( new BorderLayout() );
			currentFileLabel = new JLabel("Current file: none");
			this.add(currentFileLabel, BorderLayout.WEST);
			loading = new JLabel("loading...", JLabel.TRAILING);
			loading.setVisible(false);
			this.add(loading, BorderLayout.EAST);
		}
		
		public void resetLabel() {
			String fn = Dna.dna.db.getFileName();
			if (fn.equals("")) {
				currentFileLabel.setText("Current file: none");
			} else {
				currentFileLabel.setText("Current file: " + fn);
			}
		}
	}
	
	
	class DocumentPanel extends JScrollPane {
		
		private static final long serialVersionUID = 1L;
		DocumentTable documentTable;
		DocumentContainer documentContainer;
		
		public DocumentPanel() {
			documentContainer = new DocumentContainer();
			documentTable = new DocumentTable();
			documentTable.setModel(documentContainer);
			this.setColumnHeaderView(documentTable);
			setPreferredSize(new Dimension(700, 100));
		}
		
		class DocumentTable extends JTable {
			
			private static final long serialVersionUID = 1L;

			public DocumentTable() {
				setModel(new DocumentContainer());
				setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				getColumnModel().getColumn( 0 ).setPreferredWidth( 620 );
				getColumnModel().getColumn( 1 ).setPreferredWidth( 80 );
				getTableHeader().setReorderingAllowed( false );
				putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
				
				getSelectionModel().addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							return;
						}
						int selectedRow = getSelectedRow();
						if (selectedRow == -1) {
							//textPanel.textWindow.setText("");
							textPanel.setDocumentText("");
						} else {
							int id = documentPanel.documentContainer.
									get(selectedRow).getId();
							Document document = Dna.dna.db.getDocument(id);
							String text = document.getText();
							textPanel.setDocumentId(id);
					    	textPanel.setDocumentText(text);
							//textPanel.textWindow.setEnabled(true);
					    	textPanel.setEnabled(true);
						}
						//if (statementFilter.showCurrent.isSelected()) {
						//	statementFilter.articleFilter();
						//}
						
						if (Dna.dna.db.getFileName() != null) {
							textPanel.paintStatements();
						}
						textPanel.setCaretPosition( 0 );
					}
				});
			}
			
			/*
			public void rebuildTable() {
				documentTable.clearSelection();
				documentContainer.clear();
				ArrayList<Document> documents = new SqlQuery(dbfile).getArticles();
				for (int i = 0; i < articles.size(); i++) {
					articleContainer.addArticle(articles.get(i));
				}
			}
			*/
		}
	}
	
}
