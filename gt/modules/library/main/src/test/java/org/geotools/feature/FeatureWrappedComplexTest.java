/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.opengis.util.Cloneable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;


/**
 * Provides testing for a special type of Feature a Simple Feature wrapped in
 * Complex clothing.  Specifically DefaultFeature.WrappedComplex.  It should
 * return Lists for all of its get Attributes, since that's what a complex
 * feature does.
 *
 * @author Chris Holmes, TOPP
 * @source $URL$
 */
public class FeatureWrappedComplexTest extends TestCase {
    /** The logger for the default core module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.feature");

    /** Feature on which to preform tests */
    private Feature testFeature = null;
    TestSuite suite = null;

    public FeatureWrappedComplexTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        org.geotools.util.MonolineFormatter.initGeotools();
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FeatureWrappedComplexTest.class);

        return suite;
    }

    public void setUp() {
        SimpleFeature feature = (SimpleFeature) SampleFeatureFixtures
            .createFeature();
        //testFeature = feature.toComplex(); //this isn't in the api...yet.
        try {
	testFeature = new DefaultFeature.ComplexWrapper(feature);
	} catch (IllegalAttributeException iae) {
	    throw new RuntimeException("programming error with wrapper");
	}
    }

    public void testAttributeAccess() throws Exception {
        // this ones kinda silly
        Feature f = testFeature;

        try {
            f.setAttribute(1244, Collections.singletonList("x"));
            fail("not out of bounds");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
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
        }
    }

    public void testWrappedAccessIndex() {
        int attrIndex = 5;
        Feature simple = SampleFeatureFixtures.createFeature();
        Feature wrapped = testFeature;
        Object intAtt = wrapped.getAttribute(attrIndex);
        LOGGER.info("simple is: " + simple + ", and wrapped is: " + wrapped);
        LOGGER.info("intAtt is: " + intAtt);
        assertTrue(intAtt instanceof List);

        List intAttList = (List) intAtt;
        assertTrue(intAttList.get(0).equals(simple.getAttribute(attrIndex)));
    }

    public void testWrappedAccessName() {
        String attName = "testCharacter";
        Feature simple = SampleFeatureFixtures.createFeature();
        Feature wrapped = testFeature;
        Object intAtt = wrapped.getAttribute(attName);
        assertTrue(intAtt instanceof List);

        List intAttList = (List) intAtt;
        assertTrue(intAttList.get(0).equals(simple.getAttribute(attName)));
    }

    public void testWrappedAccessAll() {
        Feature simple = SampleFeatureFixtures.createFeature();
        Feature wrapped = testFeature;
        Object[] simpleAtts = simple.getAttributes(null);
        Object[] complexAtts = wrapped.getAttributes(null);
        List curAttList = null;

        //we're starting at 1 because of the annoying equals with geometries, 
        //and I don't want to figure out where the util method is...
        for (int i = 1; i < complexAtts.length; i++) {
            assertTrue(complexAtts[i] instanceof List);
            curAttList = (List) complexAtts[i];
            assertEquals(1, curAttList.size());
            assertEquals(curAttList.get(0), (simpleAtts[i]));
        }
    }

    public void testModify() throws IllegalAttributeException {
        String newData = "new string";

        try {
            testFeature.setAttribute("testString", newData);
            fail("All objects should be wrapped in Lists for Complex Features");
        } catch (Exception iae) {
            assertTrue(iae instanceof IllegalAttributeException);
        }

        List singleNewData = Collections.singletonList(newData);
        testFeature.setAttribute("testString", singleNewData);
        assertEquals(testFeature.getAttribute("testString"), singleNewData);

        try {
            testFeature.setAttribute("testGeometry", singleNewData);
            fail("a wrapped string should not be able to set a geometry");
        } catch (Exception e) {
            //e.printStackTrace();
            assertTrue(e instanceof IllegalAttributeException);
        }
    }

    public void testEquals() throws Exception {
        //todo
    }
}
