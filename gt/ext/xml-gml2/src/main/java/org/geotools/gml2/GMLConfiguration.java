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
package org.geotools.gml2;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.picocontainer.MutablePicoContainer;
import java.net.MalformedURLException;
import java.net.URL;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import org.geotools.gml2.bindings.GML;
import org.geotools.gml2.bindings.GMLBindingConfiguration;
import org.geotools.gml2.bindings.GMLSchemaLocationResolver;
import org.geotools.xlink.XLINKConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;


/**
 * Configuration used by gml2 parsers.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GMLConfiguration extends Configuration {
    /**
     * Creates the new gml configuration, with a depenendency
     * on {@link XLINKConfiguration}
     */
    public GMLConfiguration() {
        super();

        //add xlink cdependency
        addDependency(new XLINKConfiguration());
    }

    /**
     * @return A new instanceof {@link GMLBindingConfiguration}
     */
    public BindingConfiguration getBindingConfiguration() {
        return new GMLBindingConfiguration();
    }

    /**
     * @return {@link GML#NAMESPACE}
     */
    public String getNamespaceURI() {
        return GML.NAMESPACE;
    }

    /**
     * @return new instance of {@link GMLSchemaLocationResolver}.
     */
    public XSDSchemaLocationResolver getSchemaLocationResolver() {
        return new GMLSchemaLocationResolver();
    }

    /**
     * @return URL to the gml2 feauture.xsd file.
     */
    public URL getSchemaFileURL() throws MalformedURLException {
        return new URL(getSchemaLocationResolver()
                           .resolveSchemaLocation(null, getNamespaceURI(), "feature.xsd"));
    }

    /**
     * Configures the gml2 context.
     * <p>
     * The following classes are registered:
     * <ul>
     * <li>{@link CoordinateArraySequenceFactory} under {@link CoordinateSequenceFactory}
     * <li>{@link GeometryFactory}
     * <li>{@link FeatureTypeCache}
     * </ul>
     * </p>
     */
    public void configureContext(MutablePicoContainer container) {
        super.configureContext(container);

        container.registerComponentInstance(new FeatureTypeCache());
        container.registerComponentInstance(CoordinateSequenceFactory.class,
            CoordinateArraySequenceFactory.instance());
        container.registerComponentImplementation(GeometryFactory.class);
    }
}
