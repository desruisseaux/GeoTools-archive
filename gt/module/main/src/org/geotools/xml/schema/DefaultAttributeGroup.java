
package org.geotools.xml.schema;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class DefaultAttributeGroup implements AttributeGroup {
    private String anyAttributeNamespace;
    private Attribute[] attributes;
    private String id;
    private String name;
    private String namespace;
    
    private DefaultAttributeGroup(){}
    public DefaultAttributeGroup(String id, String name, String namespace, Attribute[] attributes, String anyAttributeNamespace){
        this.id = id;
        this.name = name;
        this.namespace = namespace;
        this.attributes = attributes;
        this.anyAttributeNamespace = anyAttributeNamespace;
    }

    /**
     * 
     * @see org.geotools.xml.xsi.AttributeGroup#getAnyAttributeNameSpace()
     */
    public String getAnyAttributeNameSpace() {
        return anyAttributeNamespace;
    }

    /**
     * 
     * @see org.geotools.xml.xsi.AttributeGroup#getAttributes()
     */
    public Attribute[] getAttributes() {
        return attributes;
    }

    /**
     * 
     * @see org.geotools.xml.xsi.AttributeGroup#getId()
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @see org.geotools.xml.xsi.AttributeGroup#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @see org.geotools.xml.xsi.AttributeGroup#getNamespace()
     */
    public String getNamespace() {
        return namespace;
    }
}
