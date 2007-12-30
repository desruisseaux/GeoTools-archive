package org.geotools.ows.v1_1;

import org.geotools.xlink.XLINKConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.XMLConfiguration;
import org.picocontainer.MutablePicoContainer;

/**
 * Parser configuration for the http://www.opengis.net/ows/1.1 schema.
 * 
 * @generated
 */
public class OWSConfiguration extends Configuration {

    /**
     * Creates a new configuration.
     * 
     * @generated
     */
    public OWSConfiguration() {
        super(OWS.getInstance());

        addDependency(new XMLConfiguration());
        addDependency(new XLINKConfiguration());
    }

    /**
     * Registers the bindings for the configuration.
     * 
     * @generated
     */
    protected final void registerBindings(MutablePicoContainer container) {

    }
}