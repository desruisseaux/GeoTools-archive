package org.geotools.xlink;

import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;

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

	protected final void registerBindings(MutablePicoContainer container) {
	}
    
}
