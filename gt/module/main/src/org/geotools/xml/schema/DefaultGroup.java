
package org.geotools.xml.schema;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class DefaultGroup implements Group {

    private ElementGrouping child;
    private String id,name,namespace;
    private int min,max;
    
    private DefaultGroup(){}
    public DefaultGroup(String id, String name, String namespace, ElementGrouping child, int min, int max){
        this.id = id;this.name = name; this.namespace = namespace;
        this.child = child;this.min = min;this.max = max;
    }
    
    /**
     * @see org.geotools.xml.schema.Group#getChild()
     */
    public ElementGrouping getChild() {
        return child;
    }

    /**
     * @see org.geotools.xml.schema.Group#getId()
     */
    public String getId() {
        return id;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#getMaxOccurs()
     */
    public int getMaxOccurs() {
        return max;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#getMinOccurs()
     */
    public int getMinOccurs() {
        return min;
    }

    /**
     * @see org.geotools.xml.schema.Group#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.geotools.xml.schema.Group#getNamespace()
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#getGrouping()
     */
    public int getGrouping() {
        return GROUP;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#findChildElement(java.lang.String)
     */
    public Element findChildElement(String name) {
        return child==null?null:child.findChildElement(name);
    }
}
