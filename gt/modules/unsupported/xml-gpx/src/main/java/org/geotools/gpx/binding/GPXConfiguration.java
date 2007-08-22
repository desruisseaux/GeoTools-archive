/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gpx.binding;

import org.picocontainer.MutablePicoContainer;
import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;


/**
 * Parser configuration for the http://www.topografix.com/GPX/1/1 schema.
 *
 * @generated
 */
public class GPXConfiguration extends Configuration {
    /**
     * Creates a new configuration.
     *
     * @generated
     */
    public GPXConfiguration() {
        super();

        //TODO: add dependencies here
    }

    @Override
    protected void configureContext(MutablePicoContainer container) {
        container.registerComponentImplementation(ObjectFactory.class);
    }

    /**
     * @return the schema namespace uri: http://www.topografix.com/GPX/1/1.
     * @generated
     */
    public String getNamespaceURI() {
        return GPX.NAMESPACE;
    }

    /**
     * @return the uri to the the gpx.xsd .
     * @generated
     */
    public String getSchemaFileURL() {
        return getSchemaLocationResolver().resolveSchemaLocation(null, getNamespaceURI(), "gpx.xsd");
    }

    /**
     * @return new instanceof {@link GPXBindingConfiguration}.
     */
    public BindingConfiguration getBindingConfiguration() {
        return new GPXBindingConfiguration();
    }
}
