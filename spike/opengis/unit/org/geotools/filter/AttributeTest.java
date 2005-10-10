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
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * Tests for Attribute Expressions
 *
 * @author James Macgill
 */
public class AttributeTest extends TestCase {
    SimpleFeatureType schema = null;

    TypeFactory typeFactory = new TypeFactoryImpl();
    
    public AttributeTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(BetweenTest.class);

        return suite;
    }

    public Feature[] sampleFeatures() throws Exception {
        AttributeType a1 = typeFactory.createType("value", Integer.class);
        GeometryType a2 = (GeometryType)typeFactory.createType("geometry", Geometry.class);
        AttributeType a3 = typeFactory.createType("name", String.class);
        schema = typeFactory.createFeatureType(new QName("test"), (List<AttributeType>)Arrays.asList(new AttributeType[] {
                    a1, a2, a3}), a2);

        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        Feature[] f = new Feature[3];
        AttributeFactory featureFactory = new AttributeFactoryImpl();
        f[0] = featureFactory.create(schema, null, new Object[] {
                    new Integer(12), gf.createGeometryCollection(null),
                    "first"
                });
        f[1] = featureFactory.create(schema, null, new Object[] {
                    new Integer(3), gf.createGeometryCollection(null),
                    "second"
                });
        f[2] = featureFactory.create(schema, null, new Object[] {
                    new Integer(15), gf.createGeometryCollection(null),
                    "third"
                });

        return f;
    }

    public void testTypeMissmatch() throws Exception {
        Feature[] f = sampleFeatures();

        //the following are intentionaly backwards
        AttributeExpressionImpl e1 = new AttributeExpressionImpl(schema, "value");
        AttributeExpressionImpl e2 = new AttributeExpressionImpl(schema, "name");
        boolean pass = false;
        Object value = null;
        value = e1.getValue(f[0]);

        if (value instanceof Integer) {
            pass = true;
        }

        assertTrue("String expresion returned an Integer", pass);
        pass = false;

        value = e2.getValue(f[0]);

        if (value instanceof String) {
            pass = true;
        }

        assertTrue("Integer expresion returned a String", pass);
    }

    public void testSetupAndExtraction() throws Exception {
        //this should move out to a more configurable system run from scripts
        //but we can start with a set of hard coded tests
        Feature[] f = sampleFeatures();

        AttributeExpressionImpl e1 = new AttributeExpressionImpl(schema, "value");
        AttributeExpressionImpl e2 = new AttributeExpressionImpl(schema, "name");

        assertEquals(12d, ((Integer) e1.getValue(f[0])).doubleValue(), 0);
        assertEquals(3d, ((Integer) e1.getValue(f[1])).doubleValue(), 0);
        assertEquals(15d, ((Integer) e1.getValue(f[2])).doubleValue(), 0);
        assertEquals("first", (String) e2.getValue(f[0]));
        assertEquals("second", (String) e2.getValue(f[1]));
    }
}
