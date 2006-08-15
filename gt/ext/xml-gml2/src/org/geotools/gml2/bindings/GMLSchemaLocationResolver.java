package org.geotools.gml2.bindings;


import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;

/**
 * 
 * @generated
 */
public class GMLSchemaLocationResolver implements XSDSchemaLocationResolver {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 *
	 *	@generated modifiable
	 */
	public String resolveSchemaLocation(XSDSchema xsdSchema, String namespaceURI,  String schemaLocationURI) {
		if (schemaLocationURI == null)
			return null;
		
		//if no namespace given, assume default for the current schema
		if ((namespaceURI == null || "".equals(namespaceURI)) && xsdSchema != null) {
			namespaceURI = xsdSchema.getTargetNamespace();
		}
			 
		
		if ("http://www.opengis.net/gml".equals(namespaceURI) && schemaLocationURI != null) {
			if (schemaLocationURI.endsWith("feature.xsd")) {
				return getClass().getResource("feature.xsd").toString();
			}
		}
		if ("http://www.opengis.net/gml".equals(namespaceURI) && schemaLocationURI != null) {
			if (schemaLocationURI.endsWith("geometry.xsd")) {
				return getClass().getResource("geometry.xsd").toString();
			}
		}
		if ("http://www.w3.org/1999/xlink".equals(namespaceURI) && schemaLocationURI != null) {
			if (schemaLocationURI.endsWith("xlinks.xsd")) {
				return getClass().getResource("xlinks.xsd").toString();
				
			}
		}
		
		return null;
	}

}