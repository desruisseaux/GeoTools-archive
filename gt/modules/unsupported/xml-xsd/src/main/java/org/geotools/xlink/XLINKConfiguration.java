package org.geotools.xlink;

import org.geotools.xlink.bindings.XLINKBindingConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;

/**
 * Parser configuration for the xlink schema.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class XLINKConfiguration extends Configuration {

	public XLINKConfiguration() {
        super(XLINK.getInstance());
    }

    /**
	 * @return A new instance of {@link XLINKBindingConfiguration}
	 */
	public BindingConfiguration getBindingConfiguration() {
		return new XLINKBindingConfiguration();
	}
}
