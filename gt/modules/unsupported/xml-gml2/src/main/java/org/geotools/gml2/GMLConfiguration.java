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

import org.picocontainer.MutablePicoContainer;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.gml2.bindings.GML;
import org.geotools.gml2.bindings.GMLBindingConfiguration;
import org.geotools.xlink.XLINKConfiguration;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;


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

        //add the parse unknown attributes property, this is mostly for 
        // the "fid" attribute
        getProperties().add(Parser.Properties.PARSE_UNKNOWN_ELEMENTS);
        getProperties().add(Parser.Properties.PARSE_UNKNOWN_ATTRIBUTES);
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
     * @return URL to the gml2 feauture.xsd file.
     */
    public String getSchemaFileURL() {
        return getSchemaLocationResolver()
                   .resolveSchemaLocation(null, getNamespaceURI(), "feature.xsd");
    }

    /**
     * Configures the gml2 context.
     * <p>
     * The following classes are registered:
     * <ul>
     * <li>{@link CoordinateArraySequenceFactory} under {@link CoordinateSequenceFactory}
     * <li>{@link GeometryFactory}
     * <li>{@link FeatureTypeCache}
     * <li>{@link DefaultFeatureCollections}
     * </ul>
     * </p>
     */
    public void configureContext(MutablePicoContainer container) {
        super.configureContext(container);

        container.registerComponentInstance(new FeatureTypeCache());

        container.registerComponentInstance(CoordinateSequenceFactory.class,
            CoordinateArraySequenceFactory.instance());
        container.registerComponentImplementation(GeometryFactory.class);
        container.registerComponentImplementation(DefaultFeatureCollections.class);
    }
}
