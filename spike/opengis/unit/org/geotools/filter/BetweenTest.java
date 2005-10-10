/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 * BetweenTest.java
 * JUnit based test
 *
 * Created on 20 June 2002, 18:53
 */
package org.geotools.filter;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * tests for between filters.
 *
 * @author James Macgill
 */
public class BetweenTest extends TestCase {
    /** Standard logging instance */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.filter");
    
    private TypeFactory typeFactory = new TypeFactoryImpl();
    private AttributeFactory attf = new AttributeFactoryImpl();

    public BetweenTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(BetweenTest.class);

        return suite;
    }

    public void testContains() throws Exception {
        //this should move out to a more configurable system run from scripts
        //but we can start with a set of hard coded tests
        BetweenFilterImpl a = new BetweenFilterImpl();

        AttributeType a1 = typeFactory.createType("value", Integer.class);
        AttributeType a2 = typeFactory.createType("geometry",
                Geometry.class);
        SimpleFeatureType schema = typeFactory.createFeatureType(new QName("testSchema"),(List<AttributeType>) Arrays.asList(new AttributeType[] {
                    a1, a2
                }),(GeometryType) a2);

        a.addLeftValue(new LiteralExpressionImpl(new Double(5)));
        a.addRightValue(new LiteralExpressionImpl(new Double(15)));
        a.addMiddleValue(new AttributeExpressionImpl(schema, "value"));

        LOGGER.fine("a1 official name is " + a1.getName());

        //FlatFeatureFactory fFac = new FlatFeatureFactory(schema);
        LOGGER.fine("geometry is " + schema.get("geometry"));
        LOGGER.fine("value is " + schema.get("value"));
        LOGGER.fine("schema has value in it ? "
            + (schema.get("value") != null));

        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        Feature f1 = attf.create(schema, null, new Object[] {
                    new Integer(12), gf.createGeometryCollection(null)
                });
        Feature f2 = attf.create(schema, null, new Object[] {
                    new Integer(3), gf.createGeometryCollection(null)
                });
        Feature f3 = attf.create(schema, null, new Object[] {
                    new Integer(15), gf.createGeometryCollection(null)
                });
        Feature f4 = attf.create(schema, null, new Object[] {
                    new Integer(5), gf.createGeometryCollection(null)
                });
        Feature f5 = attf.create(schema, null, new Object[] {
                    new Integer(30), gf.createGeometryCollection(null)
                });

        assertEquals(true, a.contains(f1)); // in between
        assertEquals(false, a.contains(f2)); // too small
        assertEquals(true, a.contains(f3)); // max value
        assertEquals(true, a.contains(f4)); // min value
        assertEquals(false, a.contains(f5)); // too large
    }
}
