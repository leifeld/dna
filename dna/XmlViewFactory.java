package dna;

import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * @author kees
 * @date 13-jan-2006
 *
 */
public class XmlViewFactory extends Object implements ViewFactory {

    /**
     * @see javax.swing.text.ViewFactory#create(javax.swing.text.Element)
     */
    public View create(Element element) {

        return new XmlView(element);
    }

}