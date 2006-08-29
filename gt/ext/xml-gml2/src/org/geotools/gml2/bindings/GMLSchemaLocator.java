package org.geotools.gml2.bindings;

import java.io.IOException;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;

import org.geotools.xlink.bindings.XLINKSchemaLocationResolver;
import org.geotools.xml.Schemas;

public class GMLSchemaLocator implements XSDSchemaLocator {

	public XSDSchema locateSchema( 
		XSDSchema schema, String namespaceURI,  String rawSchemaLocationURI, String resolvedSchemaLocationURI
	) {
	
		if ( GML.NAMESPACE.equals( namespaceURI ) ) {
			String location = getClass().getResource( "feature.xsd" ).toString();
			
			XSDSchemaLocationResolver[] locators = new XSDSchemaLocationResolver[] {
				new XLINKSchemaLocationResolver(), new GMLSchemaLocationResolver()
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
