package org.geotools.xml;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;

/**
 * Schema location resolver which resolves a schema location from a namespace uri
 * and a physical schema location.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class SchemaLocationResolver implements XSDSchemaLocationResolver {

	String uri, location;
	
	/**
	 * Creates the new schema location resolver.
	 * 
	 * @param uri The namesapce of the schema.
	 * @param location The location of the schema.
	 */
	public SchemaLocationResolver( String uri, String location ) {
		this.uri = uri;
		this.location = location;
	}
	
	public String resolveSchemaLocation( XSDSchema schema, String uri, String location ) {
		if ( this.uri.equals( uri ) ) {
			return this.location;
		}
		
		return null;
	}

}
