
package org.geotools.xml.schema;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class DefaultAttributeValue implements AttributeValue {

    private String value;
    private Attribute attribute;
    private DefaultAttributeValue(){}
    
    public DefaultAttributeValue(Attribute attribute, String value){
        this.attribute = attribute;
        this.value = value;
    }
    /**
     * @see org.geotools.xml.schema.AttributeValue#getValue()
     */
    public String getValue() {
        return value;
    }

    /**
     * @see org.geotools.xml.schema.AttributeValue#getAttribute()
     */
    public Attribute getAttribute() {
        return attribute;
    }

}
