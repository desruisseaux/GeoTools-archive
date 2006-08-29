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

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import org.geotools.gml3.bindings.GMLBindingConfiguration;
import org.geotools.gml3.bindings.GMLSchemaLocationResolver;
import org.geotools.gml3.bindings.smil.SMIL20BindingConfiguration;
import org.geotools.gml3.bindings.smil.SMIL20SchemaLocationResolver;
import org.geotools.xlink.bindings.XLINKBindingConfiguration;
import org.geotools.xlink.bindings.XLINKSchemaLocationResolver;
import org.geotools.xml.Configuration;
import org.geotools.xml.test.XMLTestSupport;
import org.geotools.xs.bindings.XSBindingConfiguration;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Element;


public abstract class GML3TestSupport extends XMLTestSupport {
    protected void registerNamespaces(Element root) {
        super.registerNamespaces(root);
        root.setAttribute("xmlns", "http://www.opengis.net/gml");
    }

    protected void registerSchemaLocation(Element root) {
        root.setAttribute("xsi:schemaLocation",
            "http://www.opengis.net/gml gml.xsd ");
    }

    protected Configuration createConfiguration() {
        return new Configuration() {
                public void configureBindings(MutablePicoContainer container) {
                    new XSBindingConfiguration().configure(container);
                    new XLINKBindingConfiguration().configure(container);
                    new SMIL20BindingConfiguration().configure(container);
                    new GMLBindingConfiguration().configure(container);
                }

                public void configureContext(MutablePicoContainer container) {
                    container.registerComponentImplementation(XLINKSchemaLocationResolver.class);
                    container.registerComponentImplementation(SMIL20SchemaLocationResolver.class);
                    container.registerComponentImplementation(GMLSchemaLocationResolver.class);
                    container.registerComponentInstance(CoordinateArraySequenceFactory
                        .instance());
                    container.registerComponentImplementation(GeometryFactory.class);
                }
            };
    }
}
