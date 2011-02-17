//this class was found at http://www.tutorials.de/forum/swing-java2d-3d-swt-jface/309848-jcombobox-mit-autovervollstaendigung.html
package dna;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;

import java.awt.event.*;
import org.apache.commons.lang.*;
 
/**
 * @author jal1976
 */
public class JComboBoxCustomize extends JComboBox implements KeyListener
{
    private JTextField jtf_searchField;
    boolean onlyBeginning; //PL
    
    //the following line was replaced by Philip Leifeld
    //public JComboBoxCustomize()
    public JComboBoxCustomize(Object[] items, boolean onlyBeginning)
    {
    	super(items); //PL
    	this.onlyBeginning = onlyBeginning;
    	this.setEditable(true);
        this.jtf_searchField = (JTextField)this.getEditor().getEditorComponent();
        DocumentFilter dfilter = new TextFilter(); //PL
		((AbstractDocument)jtf_searchField.getDocument()).setDocumentFilter(dfilter); //to prohibit control sequences when pressing Escape (PL)
        this.jtf_searchField.addKeyListener(this);
    }
 
    public void keyReleased(final KeyEvent ke)
    {
       
    }
 
    public void keyPressed(final KeyEvent ke)
    {
        if(!this.isPopupVisible())
        {
            this.setPopupVisible(true);
        }
    }
 
    public void keyTyped(final KeyEvent ke)
    {
        char source = ke.getKeyChar();
        String search = null;
        if(source == KeyEvent.VK_ENTER)
        {
            ComboBoxModel cbm = this.getModel();
            if(!this.jtf_searchField.getText().equalsIgnoreCase(""))
            {
                search = this.jtf_searchField.getText();
                int index = this.findString(search);
                if(index != -1)
                {
                    this.jtf_searchField.setText(cbm.getElementAt(index)
                                                                .toString());
                }
            }
        }   
        if(source == KeyEvent.VK_BACK_SPACE || source == KeyEvent.VK_DELETE)
        {
            if(this.jtf_searchField.getText().length() > 0 ||
                            !this.jtf_searchField.getText().equalsIgnoreCase(""))
            {
                search = this.jtf_searchField.getText();
                this.findAndSel(search, ke);
            }
            else
            {
                this.resetInput();
                ke.consume();
                return;
            }
        }
        else
        {
            if(source != KeyEvent.VK_ENTER)
            {
                search = this.jtf_searchField.getText();
                search += source;
                this.findAndSel(search, ke);
            }
        }
    }  
 
    private void findAndSel(String search, KeyEvent ke)
    {    
        int index = this.findString(search);
        this.setSelectedIndex(index);
        this.jtf_searchField.setText(search);
        this.jtf_searchField.setSelectionEnd(search.length());
        this.jtf_searchField.setSelectionStart(search.charAt(0));      
        ke.consume();
    }
 
    private int findString(String search)
    {
        ComboBoxModel cbm = this.getModel();
        for(int i=0; i<cbm.getSize(); i++)
        {
            String source = cbm.getElementAt(i).toString();
            if (onlyBeginning == true) { //PL
            	if(StringUtils.startsWithIgnoreCase(source, search))
                {
                    return i;
                }
            } else { //PL
            	if(StringUtils.containsIgnoreCase(source, search))
                {
                    return i;
                }
            } //PL
        }
        return -1;
    }
 
    private void resetInput()
    {
        this.jtf_searchField.setText("");
        this.jtf_searchField.setCaretPosition(0);
        this.setSelectedIndex(-1);        
    }
}