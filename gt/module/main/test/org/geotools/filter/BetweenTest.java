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

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

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

        AttributeType a1 = AttributeTypeFactory.newAttributeType("value", Integer.class);
        AttributeType a2 = AttributeTypeFactory.newAttributeType("geometry",
                Geometry.class);
        FeatureType schema = FeatureTypeFactory.newFeatureType(new AttributeType[] {
                    a1, a2
                }, "testSchema");

        a.addLeftValue(new LiteralExpressionImpl(new Double(5)));
        a.addRightValue(new LiteralExpressionImpl(new Double(15)));
        a.addMiddleValue(new AttributeExpressionImpl(schema, "value"));

        LOGGER.fine("a1 official name is " + a1.getName());

        //FlatFeatureFactory fFac = new FlatFeatureFactory(schema);
        LOGGER.fine("geometry is " + schema.getAttributeType("geometry"));
        LOGGER.fine("value is " + schema.getAttributeType("value"));
        LOGGER.fine("schema has value in it ? "
            + schema.hasAttributeType("value"));

        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        Feature f1 = schema.create(new Object[] {
                    new Integer(12), gf.createGeometryCollection(null)
                });
        Feature f2 = schema.create(new Object[] {
                    new Integer(3), gf.createGeometryCollection(null)
                });
        Feature f3 = schema.create(new Object[] {
                    new Integer(15), gf.createGeometryCollection(null)
                });
        Feature f4 = schema.create(new Object[] {
                    new Integer(5), gf.createGeometryCollection(null)
                });
        Feature f5 = schema.create(new Object[] {
                    new Integer(30), gf.createGeometryCollection(null)
                });

        assertEquals(true, a.contains(f1)); // in between
        assertEquals(false, a.contains(f2)); // too small
        assertEquals(true, a.contains(f3)); // max value
        assertEquals(true, a.contains(f4)); // min value
        assertEquals(false, a.contains(f5)); // too large
    }
}
