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
package org.geotools.filter.v1_1;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.picocontainer.MutablePicoContainer;
import org.opengis.filter.FilterFactory;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;


/**
 * Parser configuration for the filter 1.1 schema.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class OGCConfiguration extends Configuration {
    /**
     * Adds a dependency on {@link GMLConfiguration}
     */
    public OGCConfiguration() {
        super(OGC.getInstance());

        addDependency(new GMLConfiguration());
    }

    /**
     * @return A new instance of {@link OGCBindingConfiguration}.
     */
    public BindingConfiguration getBindingConfiguration() {
        return new OGCBindingConfiguration();
    }

    /**
     * Configures the filter context.
     * <p>
     * The following factories are registered:
     * <ul>
     * <li>{@link FilterFactoryImpl} under {@link FilterFactory}
     * </ul>
     * </p>
     */
    public void configureContext(MutablePicoContainer container) {
        super.configureContext(container);

        container.registerComponentImplementation(FilterFactory.class, FilterFactoryImpl.class);
    }
}
