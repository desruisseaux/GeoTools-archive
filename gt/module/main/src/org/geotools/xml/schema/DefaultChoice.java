
package org.geotools.xml.schema;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class DefaultChoice implements Choice {

    private String id;
    private int min,max;
    private ElementGrouping[] children;
    
    private DefaultChoice(){}
    public DefaultChoice(String id, int min, int max, ElementGrouping[] children){
        this.id = id;
        this.min = min; this.max = max;
        this.children = children;
    }
    
    /**
     * @see org.geotools.xml.schema.Choice#getId()
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
     * @see org.geotools.xml.schema.Choice#getChildren()
     */
    public ElementGrouping[] getChildren() {
        return children;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#getGrouping()
     */
    public int getGrouping() {
        return CHOICE;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#findChildElement(java.lang.String)
     */
    public Element findChildElement(String name) {
        if(children == null)
            return null;
        for(int i=0;i<children.length;i++){
            Element e = children[i].findChildElement(name);
            if(e!=null)
                return e;
        }
        return null;
    }

}
