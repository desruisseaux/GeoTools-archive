package org.geotools.filter.v1_1;


import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;

/**
 * 
 * @generated
 */
public class OGCSchemaLocationResolver implements XSDSchemaLocationResolver {

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
			
		if ("http://www.opengis.net/ogc".equals(namespaceURI)) {
			if (schemaLocationURI.endsWith("filter.xsd")) {
				return getClass().getResource("filter.xsd").toString();
			}
		}
		if ("http://www.opengis.net/ogc".equals(namespaceURI)) {
			if (schemaLocationURI.endsWith("expr.xsd")) {
				return getClass().getResource("expr.xsd").toString();
			}
		}
		if ("http://www.opengis.net/ogc".equals(namespaceURI)) {
			if (schemaLocationURI.endsWith("sort.xsd")) {
				return getClass().getResource("sort.xsd").toString();
			}
		}
		if ("http://www.opengis.net/ogc".equals(namespaceURI)) {
			if (schemaLocationURI.endsWith("filterCapabilities.xsd")) {
				return getClass().getResource("filterCapabilities.xsd").toString();
			}
		}
		
		return null;
	}

}