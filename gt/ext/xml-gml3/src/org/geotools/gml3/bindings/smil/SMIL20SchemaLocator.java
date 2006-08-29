package org.geotools.gml3.bindings.smil;

import java.io.IOException;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;


import org.geotools.xml.Schemas;

public class SMIL20SchemaLocator implements XSDSchemaLocator {

	public XSDSchema locateSchema( 
		XSDSchema schema, String namespaceURI,  String rawSchemaLocationURI, String resolvedSchemaLocationURI
	) {
	
		if ( SMIL20.NAMESPACE.equals( namespaceURI ) ) {
			String location = getClass().getResource( "smil20.xsd" ).toString();
			
			XSDSchemaLocationResolver[] locators = new XSDSchemaLocationResolver[] {
				new SMIL20SchemaLocationResolver()
			};
			try {
				return Schemas.parse( location, null, locators );
			} 
			catch (IOException e) {
				//TODO:  log this
			}
		}
		
		if ( SMIL20LANG.NAMESPACE.equals( namespaceURI ) ) {
			String location = getClass().getResource( "smil20-language.xsd" ).toString();
			
			XSDSchemaLocationResolver[] locators = new XSDSchemaLocationResolver[] {
				new SMIL20SchemaLocationResolver()
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
