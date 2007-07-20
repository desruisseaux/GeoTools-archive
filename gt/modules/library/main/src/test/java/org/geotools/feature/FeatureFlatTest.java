/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.feature;

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opengis.util.Cloneable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class FeatureFlatTest extends TestCase {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.defaultcore");

    /** Feature on which to preform tests */
    private Feature testFeature = null;

    TestSuite suite = null;

    public FeatureFlatTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FeatureFlatTest.class);
        return suite;
    }

    public void setUp() {
        testFeature = SampleFeatureFixtures.createFeature();
    }

    public void testRetrieve() {
        GeometryFactory gf = new GeometryFactory();
        assertTrue(
            "geometry retrieval and match",
            ((Point) testFeature.getAttribute("testGeometry")).equals(
                gf.createPoint(new Coordinate(1, 2))));
        assertTrue(
            "boolean retrieval and match",
            ((Boolean) testFeature.getAttribute("testBoolean")).equals(new Boolean(true)));
        assertTrue(
            "character retrieval and match",
            ((Character) testFeature.getAttribute("testCharacter")).equals(new Character('t')));
        assertTrue("byte retrieval and match", ((Byte) testFeature.getAttribute("testByte")).equals(new Byte("10")));
        assertTrue(
            "short retrieval and match",
            ((Short) testFeature.getAttribute("testShort")).equals(new Short("101")));
        assertTrue(
            "integer retrieval and match",
            ((Integer) testFeature.getAttribute("testInteger")).equals(new Integer(1002)));
        assertTrue("long retrieval and match", ((Long) testFeature.getAttribute("testLong")).equals(new Long(10003)));
        assertTrue(
            "float retrieval and match",
            ((Float) testFeature.getAttribute("testFloat")).equals(new Float(10000.4)));
        assertTrue(
            "double retrieval and match",
            ((Double) testFeature.getAttribute("testDouble")).equals(new Double(100000.5)));
        assertTrue(
            "string retrieval and match",
            ((String) testFeature.getAttribute("testString")).equals("test string data"));

    }

    public void testBogusCreation() throws Exception {
        FeatureTypeFactory factory = FeatureTypeFactory.newInstance("test1");
        factory.addType(newAtt("billy", String.class, false));
        factory.addType(newAtt("jimmy", String.class, false));
        FeatureType test = factory.getFeatureType();
        try {
            test.create(null);
            fail("no error");
        } catch (IllegalAttributeException iae) {
        }

        try {
            test.create(new Object[32]);
            fail("no error");
        } catch (IllegalAttributeException iae) {
        }

    }

    public void testBounds() throws Exception {
        GeometryFactory gf = new GeometryFactory();
        Geometry[] g = new Geometry[4];
        g[0] = gf.createPoint(new Coordinate(0, 0));
        g[1] = gf.createPoint(new Coordinate(0, 10));
        g[2] = gf.createPoint(new Coordinate(10, 0));
        g[3] = gf.createPoint(new Coordinate(10, 10));

        GeometryCollection gc = gf.createGeometryCollection(g);
        FeatureTypeFactory factory = FeatureTypeFactory.newInstance("bounds");
        factory.addType(newAtt("p1", Point.class));
        factory.addType(newAtt("p2", Point.class));
        factory.addType(newAtt("p3", Point.class));
        factory.addType(newAtt("p4", Point.class));
        FeatureType t = factory.createFeatureType();
        Feature f = t.create(g);
        assertEquals(gc.getEnvelopeInternal(), f.getBounds());

        g[1].getCoordinate().y = 20;
        g[2].getCoordinate().x = 20;
        f.setAttribute(1, g[1]);
        f.setAttribute(2, g[2]);
        gc = gf.createGeometryCollection(g);
        assertEquals(gc.getEnvelopeInternal(), f.getBounds());
    }

    public void testClone() {
        DefaultFeature f = (DefaultFeature) SampleFeatureFixtures.createFeature();
        Feature c = (Feature) f.clone();
        for (int i = 0, ii = c.getNumberOfAttributes(); i < ii; i++) {
            assertEquals(c.getAttribute(i), f.getAttribute(i));
        }
    }

    public void testClone2() throws Exception {
        FeatureType type = SampleFeatureFixtures.createTestType();
        Object[] attributes = SampleFeatureFixtures.createAttributes();
        DefaultFeature feature = (DefaultFeature) type.create(attributes, "fid");
        Feature clone = (Feature) ((Cloneable)feature).clone();
        assertTrue("Clone was not equal", feature.equals(clone));
    }

    public void testToStringWontThrow() throws IllegalAttributeException {
        SimpleFeature f = (SimpleFeature)SampleFeatureFixtures.createFeature();
        f.setAttributes(new Object[f.getNumberOfAttributes()]);
        String s = f.toString();
    }

    static AttributeType newAtt(String name, Class c) {
        return AttributeTypeFactory.newAttributeType(name, c, true);
    }

    static AttributeType newAtt(String name, Class c, boolean nillable) {
        return AttributeTypeFactory.newAttributeType(name, c, nillable);
    }

    public void testModify() throws IllegalAttributeException {
        String newData = "new test string data";
        testFeature.setAttribute("testString", newData);
        assertEquals("match modified (string) attribute", testFeature.getAttribute("testString"), newData);

        GeometryFactory gf = new GeometryFactory();
        Point newGeom = gf.createPoint(new Coordinate(3, 4));
        testFeature.setAttribute("testGeometry", newGeom);
        assertEquals("match modified (geometry) attribute", testFeature.getAttribute("testGeometry"), newGeom);

        testFeature.setPrimaryGeometry(newGeom);
        assertEquals("match modified (geometry) attribute", testFeature.getAttribute("testGeometry"), newGeom);

    }

//    public void testFindAttribute() {
//        DefaultFeature f = (DefaultFeature) SampleFeatureFixtures.createFeature();
//        FeatureType t = f.getFeatureType();
//        for (int i = 0, ii = t.getAttributeCount(); i < ii; i++) {
//            AttributeType a = t.getAttributeType(i);
//            assertEquals(i, f.findAttributeByName(a.getName()));
//        }
//        assertEquals(-1, f.findAttributeByName("bilbo baggins"));
//        assertEquals(null, f.getAttribute("jimmy hoffa"));
//    }

    public void testAttributeAccess() throws Exception {
        // this ones kinda silly
    	SimpleFeature f = (SimpleFeature)SampleFeatureFixtures.createFeature();
        Object[] atts = null;
        atts = f.getAttributes(atts);
        for (int i = 0, ii = atts.length; i < ii; i++) {
            assertEquals(atts[i], f.getAttribute(i));
        }
        Object[] attsAgain = f.getAttributes(null);
        assertTrue(atts != attsAgain);
        f.setAttributes(atts);
        attsAgain = f.getAttributes(attsAgain);
        assertTrue(atts != attsAgain);
        for (int i = 0, ii = atts.length; i < ii; i++) {
            assertEquals(atts[i], f.getAttribute(i));
            assertEquals(attsAgain[i], f.getAttribute(i));
        }
        try {
            f.setAttribute(1244, "x");
            fail("not out of bounds");
        } catch (ArrayIndexOutOfBoundsException aioobe) {

        }
        catch (IndexOutOfBoundsException ioobe) {

        }
        try {
            f.setAttribute("1244", "x");
            fail("allowed bogus attribute setting");
        } catch (IllegalAttributeException iae) {

        }
        try {
            f.setAttribute("testGeometry", "x");
            fail("allowed bogus attribute setting");
        } catch (IllegalAttributeException iae) {

        } catch (RuntimeException rt) {
        }
    }

    // IanS - this is no longer good, cause we deal with parsing
//    public void testEnforceType() {
//        
//        Date d = new Date();
//        
//        Feature f = SampleFeatureFixtures.createFeature();
//        for (int i = 0, ii = f.getNumberOfAttributes(); i < ii; i++) {
//            try {
//                f.setAttribute(i, d);
//            } catch (IllegalAttributeException iae) {
//                continue;
//            }
//            fail("No error thrown during illegal set");
//        }
//
//    }

    public void testEquals() throws Exception {
        Feature f1 = SampleFeatureFixtures.createFeature();
        Feature f2 = SampleFeatureFixtures.createFeature();
        assertTrue(f1.equals(f1));
        assertTrue(f2.equals(f2));
        assertTrue(!f1.equals(f2));
        assertTrue(!f1.equals(null));
        FeatureType another =
            FeatureTypeFactory.newFeatureType(new AttributeType[] { newAtt("name", String.class)}, "different");
        assertTrue(!f1.equals(another.create(new Object[1])));
    }

    /*
     * This is actually a test for FeatureTypeFlat, but there is no test for that
     * written right now, so I'm just putting it here, as I just changed the
     * getDefaultGeometry method, and it should have a unit test.  It tests 
     * to make sure getDefaultGeometry returns null if there is no geometry,
     * as we now allow 
     */
    public void testDefaultGeometry() throws Exception {
        FeatureType testType = testFeature.getFeatureType();
        AttributeType geometry = testType.getAttributeType("testGeometry");
        assertTrue(geometry == testType.getPrimaryGeometry());
        assertTrue(testFeature.getPrimaryGeometry().getEnvelopeInternal().equals(testFeature.getBounds()));

        FeatureType another =
            FeatureTypeFactory.newFeatureType(new AttributeType[] { newAtt("name", String.class)}, "different");
        DefaultFeature f1 = (DefaultFeature) another.create(new Object[1]);
        assertEquals(null, f1.getPrimaryGeometry());
        try {
            f1.setPrimaryGeometry(null);
            fail("allowed bogus default geometry set ");
        } catch (IllegalAttributeException iae) {

        }
    }

}
