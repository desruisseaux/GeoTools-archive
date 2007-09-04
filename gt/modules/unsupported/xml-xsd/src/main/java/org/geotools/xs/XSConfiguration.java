package org.geotools.xs;

import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.XSD;
import org.geotools.xs.bindings.XSBindingConfiguration;

/**
 * Parser configuration for xml schema schema.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class XSConfiguration extends Configuration {

	public XSConfiguration() {
        super(XS.getInstance());
        
    }

    public BindingConfiguration getBindingConfiguration() {
		return new XSBindingConfiguration();
	}
}
