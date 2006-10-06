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
 * Creates a schema from scratch for a particular namespace.
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
 * 	SchemaLocator locator = new SchemaLocator( myConfig );
 * 	XSDSchema schema = locator.locateSchema( null, namespaceURI, null, null); 
 * 	</pre>
 * </code>
 * 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public final class SchemaLocator implements XSDSchemaLocator {

	static Logger logger = Logger.getLogger( "org.geotools.xml" );
	
	/**
	 * the configuration
	 */
	Configuration configuration;
	
	/**
	 * cached schema
	 */
	XSDSchema schema;
	
	/**
	 * Creates a new schema location.
	 * 
	 * @param configuration the configuration defining information about the schema.
	 */
	public SchemaLocator( Configuration configuration ) {
		this.configuration = configuration;
	}
	
	/**
	 * Creates the schema, returning <code>null</code> if the schema could not be created.
	 * </p>
	 *	<code>namespaceURI</code> should not be <code>null</code>. All other paramters are ignored. 
	 * </p>
	 * <p>
	 * This method caches the returned schema, and is thread safe.
	 * </p>
	 * 
	 * @see XSDSchemaLocator#locateSchema(org.eclipse.xsd.XSDSchema, java.lang.String, java.lang.String, java.lang.String)
	 */
	public XSDSchema locateSchema( 
		XSDSchema schema, String namespaceURI, String rawSchemaLocationURI, String resolvedSchemaLocationURI
	) {
		
		if ( configuration.getNamespaceURI().equals( namespaceURI ) ) {
			if ( this.schema != null ) 
				return this.schema;

			synchronized ( this ) {
				if ( this.schema != null ) 
					return this.schema;;
				
				//add dependent location resolvers
				List resolvers = new ArrayList();
				for ( Iterator d = configuration.allDependencies().iterator(); d.hasNext(); ) {
					Configuration dependency = (Configuration) d.next();
					XSDSchemaLocationResolver resolver = dependency.getSchemaLocationResolver();
					
					if ( resolver != null) {
						resolvers.add( resolver );
					}
				}
				
				try {
					String location = configuration.getSchemaFileURL().toString();
					this.schema =  Schemas.parse( location, null, resolvers );
	            } 
				catch (IOException e) {
					String msg = "Failed to create schema: " + namespaceURI;
	                logger.log( Level.WARNING, msg, e );
	            }
			}
		}
		
		return this.schema;
	}
	    	
}
