package org.geotools.xs;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;
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
		//special case, this is the bootstrap
		return null;
	}
	
	public BindingConfiguration getBindingConfiguration() {
		return new XSBindingConfiguration();
	}

	public XSDSchemaLocationResolver getSchemaLocationResolver() {
		return null;
	}
	
	public XSDSchemaLocator getSchemaLocator() {
		return null;
	}
}
