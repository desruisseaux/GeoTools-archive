
package org.geotools.xml.schema;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class DefaultAttribute implements Attribute {

    private String defualT,fixed,id,name,namespace;
    private int use;
    private SimpleType type;
    private boolean form;
    
    private DefaultAttribute(){}
    
    public DefaultAttribute(String id, String name, String namespace, SimpleType type, int use, String defaulT, String fixed, boolean form){
        this.id = id;
        this.name = name;
        this.namespace = namespace;
        this.type = type;
        this.use = use;
        this.defualT = defaulT;
        this.fixed = fixed;
        this.form = form;
    }
    
    /**
     * @see org.geotools.xml.schema.Attribute#getDefault()
     */
    public String getDefault() {
        return defualT;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getFixed()
     */
    public String getFixed() {
        return fixed;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#isForm()
     */
    public boolean isForm() {
        return form;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getId()
     */
    public String getId() {
        return id;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getUse()
     */
    public int getUse() {
        return use;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getSimpleType()
     */
    public SimpleType getSimpleType() {
        return type;
    }

    /**
     * @see org.geotools.xml.schema.Attribute#getNamespace()
     */
    public String getNamespace() {
        return namespace;
    }

}
