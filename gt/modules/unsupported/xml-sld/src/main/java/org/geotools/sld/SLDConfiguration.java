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
package org.geotools.sld;

import org.geotools.filter.v1_0.OGCConfiguration;
import org.geotools.sld.bindings.SLD;
import org.geotools.sld.bindings.SLDBindingConfiguration;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryImpl;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;


/**
 * Parser configuration for the Styled Layer Descriptor  schema.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class SLDConfiguration extends Configuration {
    /**
     * Adds a dependency on {@link OGCConfiguration}
     */
    public SLDConfiguration() {
        super(SLD.getInstance());

        addDependency(new OGCConfiguration());
    }

    /**
     * @return A new instance of {@link SLDBindingConfiguration}.
     */
    public BindingConfiguration getBindingConfiguration() {
        return new SLDBindingConfiguration();
    }

    /**
     * Configures the sld context.
     * <p>
     * The following factories are registered:
     * <ul>
     * <li>{@link StyleFactoryImpl.class} under {@link StyleFactory.class}
     * </ul>
     * </p>
     */
    protected void configureContext(MutablePicoContainer container) {
        super.configureContext(container);

        container.registerComponentImplementation(StyleFactory.class, StyleFactoryImpl.class);
    }
}
