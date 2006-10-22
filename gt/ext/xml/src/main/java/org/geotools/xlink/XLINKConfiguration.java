package org.geotools.xlink;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.geotools.xlink.bindings.XLINK;
import org.geotools.xlink.bindings.XLINKBindingConfiguration;
import org.geotools.xlink.bindings.XLINKSchemaLocationResolver;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;

/**
 * Parser configuration for the xlink schema.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class XLINKConfiguration extends Configuration {

	/**
	 * @return {@link XLINK#NAMESPACE}, http://www.w3.org/1999/xlink
	 */
	public String getNamespaceURI() {
		return XLINK.NAMESPACE;
	}

	/**
	 * @return The xlinks.xsd of the xlink schema.
	 */
	public URL getSchemaFileURL() throws MalformedURLException {
		return new URL( 
			getSchemaLocationResolver().resolveSchemaLocation( null, getNamespaceURI(), "xlinks.xsd" )
		);
	}

	/**
	 * @return A new instance of {@link XLINKBindingConfiguration}
	 */
	public BindingConfiguration getBindingConfiguration() {
		return new XLINKBindingConfiguration();
	}

	/**
	 * @return A new instance of {@link XLINKSchemaLocationResolver}
	 */
	public XSDSchemaLocationResolver getSchemaLocationResolver() {
		return new XLINKSchemaLocationResolver();
	}

}
