package org.geotools.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;

/**
 * Helper class which ensures that the xsd schema parser uses pre-build schema 
 * objects.
 * <p>
 * This class works from a {@link org.geotools.xml.XSD} which contains a reference
 * to the schema.
 * </p>
 * <p>
 * Example usage:
 *  
 * <code>
 * 	<pre>
 * 	XSD xsd = ...;
 * 	String namespaceURI = xsd.getNamesapceURI();
 * 
 * 	SchemaLocator locator = new SchemaLocator( xsd );
 * 	XSDSchema schema = locator.locateSchema( null, namespaceURI, null, null); 
 * 	</pre>
 * </code>
 * 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public final class SchemaLocator implements XSDSchemaLocator {
    /**
     * logging instance
     */
    protected static Logger LOGGER = Logger.getLogger("org.geotools.xml");
    /**
     * The xsd instance.
     */
    protected XSD xsd;
    
    /**
     * Creates a new instance of the schema locator.
     * 
     * @param xsd The XSD instance that references the schema to be "located".
     */
    public SchemaLocator( XSD xsd ) {
        this.xsd = xsd;
    }
       
    /**
    * Creates the schema, returning <code>null</code> if the schema could not be created.
    * </p>
    *  <code>namespaceURI</code> should not be <code>null</code>. All other parameters are ignored. 
    * 
    * @see XSDSchemaLocator#locateSchema(org.eclipse.xsd.XSDSchema, java.lang.String, java.lang.String, java.lang.String)
    */
   public XSDSchema locateSchema( 
       XSDSchema schema, String namespaceURI, String rawSchemaLocationURI, String resolvedSchemaLocationURI
   ) {
       
       if ( xsd.getNamespaceURI().equals( namespaceURI ) ) {
           try {
               return xsd.getSchema();
           } 
           catch (IOException e) {
               LOGGER.log( Level.WARNING, "Error occured getting schema", e );
           }
       }
       
       return null;
   }

   
   public String toString() {
       return xsd.toString();
   }
}
