
package org.geotools.xml.ogc;

import java.net.URI;

import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeGroup;
import org.geotools.xml.schema.ComplexType;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Group;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.SimpleType;

/**
 * <p> 
 * DOCUMENT ME!
 * TODO Fill me in !!!
 * </p>
 * @author dzwiers
 *
 */
public class FilterSchema implements Schema {
    
    public static final String NAMESPACE = "http://www.opengis.net/ogc";
    
    private static final FilterSchema instance = new FilterSchema();

    public static FilterSchema getInstance(){return instance;}

    /**
     * @see org.geotools.xml.schema.Schema#getAttributeGroups()
     */
    public AttributeGroup[] getAttributeGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getAttributes()
     */
    public Attribute[] getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getBlockDefault()
     */
    public int getBlockDefault() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getComplexTypes()
     */
    public ComplexType[] getComplexTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getElements()
     */
    public Element[] getElements() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getFinalDefault()
     */
    public int getFinalDefault() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getGroups()
     */
    public Group[] getGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getId()
     */
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getImports()
     */
    public Schema[] getImports() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getURI()
     */
    public URI getURI() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getPrefix()
     */
    public String getPrefix() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getSimpleTypes()
     */
    public SimpleType[] getSimpleTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getTargetNamespace()
     */
    public String getTargetNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#getVersion()
     */
    public String getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.xml.schema.Schema#includesURI(java.net.URI)
     */
    public boolean includesURI(URI uri) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.geotools.xml.schema.Schema#isAttributeFormDefault()
     */
    public boolean isAttributeFormDefault() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.geotools.xml.schema.Schema#isElementFormDefault()
     */
    public boolean isElementFormDefault() {
        // TODO Auto-generated method stub
        return false;
    }

//} // TODO uncomment this line
