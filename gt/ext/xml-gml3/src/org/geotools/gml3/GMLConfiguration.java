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
package org.geotools.gml3;

import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.geotools.gml3.bindings.GML;
import org.geotools.gml3.bindings.GMLBindingConfiguration;
import org.geotools.gml3.bindings.GMLSchemaLocationResolver;
import org.geotools.gml3.smil.SMIL20Configuration;
import org.geotools.gml3.smil.SMIL20LANGConfiguration;
import org.geotools.xlink.XLINKConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Parser configuration for the gml3 schema.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GMLConfiguration extends Configuration {
    public GMLConfiguration() {
        super();

        //add xlink cdependency
        addDependency(new XLINKConfiguration());

        //add smil depenedncy
        addDependency(new SMIL20Configuration());
        addDependency(new SMIL20LANGConfiguration());
    }

    /**
     * @return {@link GML#NAMESPACE}
     */
    public String getNamespaceURI() {
        return GML.NAMESPACE;
    }

    /**
     * @return A new instance of {@link org.geotools.gml3.bindings.GMLBindingConfiguration}
     */
    public BindingConfiguration getBindingConfiguration() {
        return new GMLBindingConfiguration();
    }

    /**
     * @return A new instance of {@link org.geotools.gml3.bindings.GMLSchemaLocationResolver}
     */
    public XSDSchemaLocationResolver getSchemaLocationResolver() {
        return new GMLSchemaLocationResolver();
    }

    /**
     * @return Url to the gml.xsd file of the gml3 schema.
     */
    public URL getSchemaFileURL() throws MalformedURLException {
        return new URL(getSchemaLocationResolver()
                           .resolveSchemaLocation(null, getNamespaceURI(),
                "gml.xsd"));
    }

    /**
     * Configures the gml3 context.
     * <p>
     * The following factories are registered:
     * <ul>
     * <li>{@link CoordinateArraySequenceFactory} under {@link CoordinateSequenceFactory}
     * <li>{@link GeometryFactory}
     * </ul>
     * </p>
     */
    public void configureContext(MutablePicoContainer container) {
        super.configureContext(container);

        //factories
        container.registerComponentInstance(CoordinateSequenceFactory.class,
            CoordinateArraySequenceFactory.instance());
        container.registerComponentImplementation(GeometryFactory.class);
    }
}
