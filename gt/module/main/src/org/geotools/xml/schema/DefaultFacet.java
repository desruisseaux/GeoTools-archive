
package org.geotools.xml.schema;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class DefaultFacet implements Facet {

    private int type;
    private String value;
    
    private DefaultFacet(){}
    public DefaultFacet(int type, String value){
        this.type = type;
        this.value = value;
    }
    
    /**
     * @see org.geotools.xml.schema.Facet#getFacetType()
     */
    public int getFacetType() {
        return type;
    }

    /**
     * @see org.geotools.xml.schema.Facet#getValue()
     */
    public String getValue() {
        return value;
    }

}
