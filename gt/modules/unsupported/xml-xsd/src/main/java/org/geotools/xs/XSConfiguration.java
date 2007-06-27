package org.geotools.xs;

import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xs.bindings.XS;
import org.geotools.xs.bindings.XSBindingConfiguration;

/**
 * Parser configuration for xml schema schema.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class XSConfiguration extends Configuration {

	public String getNamespaceURI() {
		return XS.NAMESPACE;
	}

	public String getSchemaFileURL() {
		return getClass().getResource("XMLSchema.xsd").toString();
	}
	
	public BindingConfiguration getBindingConfiguration() {
		return new XSBindingConfiguration();
	}
}
