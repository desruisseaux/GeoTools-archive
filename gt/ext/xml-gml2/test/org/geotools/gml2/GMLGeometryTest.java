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

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import junit.framework.TestCase;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.geotools.gml2.bindings.GMLBindingConfiguration;
import org.geotools.gml2.bindings.GMLSchemaLocationResolver;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.geotools.xml.impl.ParserHandler;
import org.geotools.xs.bindings.XSBindingConfiguration;
import org.picocontainer.MutablePicoContainer;
import java.io.File;
import java.util.LinkedList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class GMLGeometryTest extends TestCase {
    Parser parser;

    protected void setUp() throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setNamespaceAware(true);

        Configuration configuration = new Configuration() {
                public void configureBindings(MutablePicoContainer container) {
                    container.registerComponentImplementation(LinkedList.class);
                    new XSBindingConfiguration().configure(container);
                    new GMLBindingConfiguration().configure(container);
                }

                public void configureContext(MutablePicoContainer container) {
                    container.registerComponentImplementation(GMLSchemaLocationResolver.class);
                    container.registerComponentInstance(CoordinateArraySequenceFactory
                        .instance());
                    container.registerComponentImplementation(GeometryFactory.class);
                }
            };

        parser = new Parser(configuration,
                getClass().getResourceAsStream("geometry.xml"));
    }

    public void test() throws Exception {
        GeometryCollection gc = (GeometryCollection) parser.parse();

        assertEquals(gc.getNumGeometries(), 3);

        Object o = gc.getGeometryN(0);
        assertNotNull(o);
        assertTrue(o instanceof Point);

        o = gc.getGeometryN(1);
        assertNotNull(o);
        assertTrue(o instanceof LineString);

        o = gc.getGeometryN(2);
        assertNotNull(o);
        assertTrue(o instanceof Polygon);
    }
}
