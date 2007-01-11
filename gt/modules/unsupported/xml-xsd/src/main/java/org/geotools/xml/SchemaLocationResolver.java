package org.geotools.xml;

import java.io.File;
import java.net.URL;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;

/**
 * Resolves a physical schema location from a namespace uri.
 * <p>
 * This class works from a {@link org.geotools.xml.Configuration} which defines information about
 * the schema. 
 * </p>
 * <p>
 * Example usage:
 *  
 * <code>
 * 	<pre>
 * 	Configuration myConfig = ...
 * 	String namespaceURI = myConfig.getNamesapceURI();
 * 
 * 	SchemaLocationResolver resolver = new SchemaLocationResolver( myConfig );
 * 	String schemaLocation = locator.resolveSchemaLocation( null, namespaceURI, "mySchema.xsd" ); 
 * 	</pre>
 * </code>
 * 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class SchemaLocationResolver implements XSDSchemaLocationResolver {

	/**
	 * The Configuration
	 */
	Configuration configuration;
	
	/**
	 * Creates the new schema location resolver.
	 * 
	 * @param configuration The schema configuration
	 */
	public SchemaLocationResolver( Configuration configuration ) {
		this.configuration = configuration;
	}
	
	/**
	 * Resolves <param>location<param> to a physical location.
	 * <p>
	 * Resolution is performed by stripping the filename off of <param>location</param>
	 * and looking up a resource located in the same package as the class of the configuration.
	 * </p>
	 */
	public String resolveSchemaLocation( XSDSchema schema, String uri, String location ) {
		 
        if ( location == null ) {
            return null;
        }

        //if no namespace given, assume default for the current schema
        if ((( uri == null) || "".equals( uri)) && ( schema != null ) ) {
            uri = schema.getTargetNamespace();
        }
        

        //namespace match?
        if ( configuration.getNamespaceURI().equals( uri )) {
        	//strip off the filename and do a resource lookup
           String fileName = new File( location ).getName();
           URL xsd = configuration.getClass().getResource( fileName );
           if ( xsd != null ) {
        	   return xsd.toString();
           }
        }
        
        return null;
	}

}
