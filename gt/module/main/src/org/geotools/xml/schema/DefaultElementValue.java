
package org.geotools.xml.schema;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class DefaultElementValue implements ElementValue {

    private Element element;
    private Object value;
    
    private DefaultElementValue(){}
    public DefaultElementValue(Element element, Object value){
        this.element = element;
        this.value = value;
    }
    /**
     * @see org.geotools.xml.schema.ElementValue#getElement()
     */
    public Element getElement() {
        return element;
    }

    /**
     * @see org.geotools.xml.schema.ElementValue#getValue()
     */
    public Object getValue() {
        return value;
    }

}
