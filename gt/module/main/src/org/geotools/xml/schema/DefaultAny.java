
package org.geotools.xml.schema;

import java.net.URI;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class DefaultAny implements Any {
    private String id = null;
    private int min = 1;
    private int max = 1;
    private URI ns = null;
    
    private DefaultAny(){}
    public DefaultAny(URI namespace){
        ns = namespace;
    }
    public DefaultAny(URI namespace,int min, int max){
        ns = namespace;
        this.min = min;this.max=max;
    }

    /**
     * @see org.geotools.xml.schema.Any#getId()
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
     * @see org.geotools.xml.schema.Any#getNamespace()
     */
    public URI getNamespace() {
        return ns;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#getGrouping()
     */
    public int getGrouping() {
        return ElementGrouping.ANY;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#findChildElement(java.lang.String)
     */
    public Element findChildElement(String name) {
        return null;
    }

}
