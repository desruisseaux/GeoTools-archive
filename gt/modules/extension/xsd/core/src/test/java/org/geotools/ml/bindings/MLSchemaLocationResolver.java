package org.geotools.ml.bindings;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;

public class MLSchemaLocationResolver implements XSDSchemaLocationResolver {

    public String resolveSchemaLocation(XSDSchema xsdSchema, String namespaceURI,
        String schemaLocationURI) {
        if (schemaLocationURI == null) {
            return null;
        }

        //if no namespace given, assume default for the current schema
        if (((namespaceURI == null) || "".equals(namespaceURI)) && (xsdSchema != null)) {
            namespaceURI = xsdSchema.getTargetNamespace();
        }

        if (ML.NAMESPACE.equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("mails.xsd")) {
                return getClass().getResource("mails.xsd").toString();
            }
        }
     
        return null;
    }
}
