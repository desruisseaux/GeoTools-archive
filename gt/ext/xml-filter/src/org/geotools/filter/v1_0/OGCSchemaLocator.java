package org.geotools.filter.v1_0;

import java.io.IOException;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;

import org.geotools.gml2.bindings.GMLSchemaLocationResolver;
import org.geotools.xlink.bindings.XLINKSchemaLocationResolver;
import org.geotools.xml.Schemas;

public class OGCSchemaLocator implements XSDSchemaLocator {

	public XSDSchema locateSchema( 
			XSDSchema schema, String namespaceURI,  String rawSchemaLocationURI, String resolvedSchemaLocationURI
		) {
		
			if ( OGC.NAMESPACE.equals( namespaceURI ) ) {
				String location = getClass().getResource( "filter.xsd" ).toString();
				
				XSDSchemaLocationResolver[] locators = new XSDSchemaLocationResolver[] {
					new XLINKSchemaLocationResolver(), new GMLSchemaLocationResolver(),
					new OGCSchemaLocationResolver()
				};
				try {
					return Schemas.parse( location, null, locators );
				} 
				catch (IOException e) {
					//TODO:  log this
				}
			}
			
			return null;
		}

}
