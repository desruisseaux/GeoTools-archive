package org.geotools.ows.v1_1;


import java.util.Set;
import javax.xml.namespace.QName;

import org.geotools.xlink.XLINK;
import org.geotools.xml.XML;
import org.geotools.xml.XSD;

/**
 * This interface contains the qualified names of all the types,elements, and 
 * attributes in the http://www.opengis.net/ows/1.1 schema.
 *
 * @generated
 */
public final class OWS extends XSD {

    /** singleton instance */
    private static final OWS instance = new OWS();
    
    /**
     * Returns the singleton instance.
     */
    public static final OWS getInstance() {
       return instance;
    }
    
    /**
     * private constructor
     */
    private OWS() {
    }
    
    protected void addDependencies(Set dependencies) {
        dependencies.add( XML.getInstance() );
        dependencies.add( XLINK.getInstance() );
    }
    
    /**
     * Returns 'http://www.opengis.net/ows/1.1'.
     */
    public String getNamespaceURI() {
       return NAMESPACE;
    }
    
    /**
     * Returns the location of 'owsAll.xsd.'.
     */
    public String getSchemaLocation() {
       return getClass().getResource("owsAll.xsd").toString();
    }
    
    /** @generated */
    public static final String NAMESPACE = "http://www.opengis.net/ows/1.1";
    
    /* Type Definitions */

    /* Elements */

    /* Attributes */

}
    