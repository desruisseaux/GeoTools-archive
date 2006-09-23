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

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import junit.framework.TestCase;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.gml3.bindings.GMLBindingConfiguration;
import org.geotools.gml3.bindings.GMLSchemaLocationResolver;
import org.geotools.gml3.bindings.smil.SMIL20BindingConfiguration;
import org.geotools.gml3.bindings.smil.SMIL20SchemaLocationResolver;
import org.geotools.xlink.bindings.XLINKSchemaLocationResolver;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.geotools.xs.bindings.XSBindingConfiguration;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.picocontainer.MutablePicoContainer;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class OGCFilterTest extends TestCase {
    Parser parser;

    protected void setUp() throws Exception {
        super.setUp();

        Configuration configuration = new Configuration() {
                public void configureBindings(MutablePicoContainer container) {
                    // XS schema bindings
                    new XSBindingConfiguration().configure(container);

                    // SMIL 2.0 bindings (for GML 3.1)
                    new SMIL20BindingConfiguration().configure(container);

                    // GML 3.1.1 bindings
                    new GMLBindingConfiguration().configure(container);

                    // Filter 1.1 bindings
                    new OGCBindingConfiguration().configure(container);
                }

                public void configureContext(MutablePicoContainer container) {
                    //every time a FilterFactory is needed, use this one
                    final FilterFactory filterFactory = FilterFactoryFinder
                        .createFilterFactory();
                    container.registerComponentInstance(filterFactory);

                    container.registerComponentInstance(CoordinateArraySequenceFactory
                        .instance());
                    container.registerComponentImplementation(GeometryFactory.class);

                    // Schema location resolver for XLink
                    container.registerComponentImplementation(XLINKSchemaLocationResolver.class);

                    // Schema location resolver for SMIL 2.0
                    container.registerComponentImplementation(SMIL20SchemaLocationResolver.class);

                    // Schema location resolver for GML 3.1.1
                    container.registerComponentImplementation(GMLSchemaLocationResolver.class);

                    // Schema location resolver for Filter 1.1
                    container.registerComponentImplementation(OGCSchemaLocationResolver.class);
                }
            };

        parser = new Parser(configuration);
    }

    public void testRun() throws Exception {
        InputStream in = getClass().getResourceAsStream("test1.xml");

        if (in == null) {
            throw new FileNotFoundException(getClass().getResource("test1.xml")
                                                .toExternalForm());
        }

        Object thing = parser.parse(in);
        assertNotNull(thing);
        assertTrue(thing instanceof PropertyIsEqualTo);

        PropertyIsEqualTo equal = (PropertyIsEqualTo) thing;
        assertTrue(equal.getExpression1() instanceof PropertyName);
        assertTrue(equal.getExpression2() instanceof Literal);

        PropertyName name = (PropertyName) equal.getExpression1();
        assertEquals("testString", name.getPropertyName());

        Literal literal = (Literal) equal.getExpression2();
        assertEquals("2", literal.toString());
    }
}
