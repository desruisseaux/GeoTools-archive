package org.geotools.xlink.bindings;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;

public class XLINKSchemaLocationResolver implements XSDSchemaLocationResolver {

	public String resolveSchemaLocation(
		XSDSchema xsdSchema,  String namespaceURI,  String schemaLocationURI
	) {
		
		if (schemaLocationURI == null)
			return null;
		
		//if no namespace given, assume default for the current schema
		if ((namespaceURI == null || "".equals(namespaceURI)) && xsdSchema != null) {
			namespaceURI = xsdSchema.getTargetNamespace();
		}
			 
		if ("http://www.w3.org/1999/xlink".equals(namespaceURI) && schemaLocationURI != null) {
			if (schemaLocationURI.endsWith("xlinks.xsd")) {
				return getClass().getResource("xlinks.xsd").toString();
				
			}
		}
		
		return null;
	}

}
