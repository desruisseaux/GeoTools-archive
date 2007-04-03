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
package org.geotools.filter;

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.jdbc.fidmapper.BasicFIDMapper;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;


/**
 * Unit test for SQLEncoderPostgis.  This is a complimentary  test suite with
 * the filter test suite.
 *
 * @author Chris Holmes, TOPP
 * @source $URL$
 */
public class SQLEncoderMySQLTest extends TestCase {
    /** Standard logging instance */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.filter");
    protected static AttributeTypeFactory attFactory = AttributeTypeFactory
        .defaultInstance();

    /** Schema on which to preform tests */
    protected static FeatureType testSchema = null;

    /** Schema on which to preform tests */
    protected static Feature testFeature = null;

    /** Test suite for this test case */
    TestSuite suite = null;

    /** folder where test data is stored.. */
    String dataFolder = "";
    protected boolean setup = false;

    public SQLEncoderMySQLTest(String testName) {
        super(testName);
        LOGGER.finer("running SQLEncoderTests");
        ;
        dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder += "/tests/unit/testData";
        }
    }

    protected void setUp() throws SchemaException, IllegalAttributeException {
        if (setup) {
            return;
        } else {
            prepareFeatures();
        }

        setup = true;
    }

    protected void prepareFeatures()
        throws SchemaException, IllegalAttributeException {
        //_log.getLoggerRepository().setThreshold(Level.INFO);
        // Create the schema attributes
        LOGGER.finer("creating flat feature...");

        AttributeType geometryAttribute = AttributeTypeFactory.newAttributeType("testGeometry",
                LineString.class);
        LOGGER.finer("created geometry attribute");

        AttributeType booleanAttribute = AttributeTypeFactory.newAttributeType("testBoolean",
                Boolean.class);

        AttributeType charAttribute = AttributeTypeFactory.newAttributeType("testCharacter",
                Character.class);
        AttributeType byteAttribute = AttributeTypeFactory.newAttributeType("testByte",
                Byte.class);
        AttributeType shortAttribute = AttributeTypeFactory.newAttributeType("testShort",
                Short.class);
        AttributeType intAttribute = AttributeTypeFactory.newAttributeType("testInteger",
                Integer.class);
        AttributeType longAttribute = AttributeTypeFactory.newAttributeType("testLong",
                Long.class);
        AttributeType floatAttribute = AttributeTypeFactory.newAttributeType("testFloat",
                Float.class);
        AttributeType doubleAttribute = AttributeTypeFactory.newAttributeType("testDouble",
                Double.class);
        AttributeType stringAttribute = AttributeTypeFactory.newAttributeType("testString",
                String.class);

        AttributeType[] types = {
                geometryAttribute, booleanAttribute, charAttribute,
                byteAttribute, shortAttribute, intAttribute, longAttribute,
                floatAttribute, doubleAttribute, stringAttribute
            };

        // Builds the schema
        testSchema = FeatureTypeFactory.newFeatureType(types, "testSchema");

        GeometryFactory geomFac = new GeometryFactory();

        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

        // Builds the test feature
        Object[] attributes = new Object[10];
        attributes[0] = geomFac.createLineString(coords);
        attributes[1] = new Boolean(true);
        attributes[2] = new Character('t');
        attributes[3] = new Byte("10");
        attributes[4] = new Short("101");
        attributes[5] = new Integer(1002);
        attributes[6] = new Long(10003);
        attributes[7] = new Float(10000.4);
        attributes[8] = new Double(100000.5);
        attributes[9] = "test string data";

        // Creates the feature itself
        testFeature = testSchema.create(attributes);
        LOGGER.finer("...flat feature created");

        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
    }

    /**
     * Main for test runner.
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Required suite builder.
     *
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(SQLEncoderMySQLTest.class);

        return suite;
    }

    public void test1() throws Exception {
    	FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        GeometryFilter gf = factory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        LiteralExpressionImpl right = new BBoxExpressionImpl(new Envelope(0,
                    300, 0, 300));
        gf.addRightGeometry(right);

        AttributeExpressionImpl left = new AttributeExpressionImpl(testSchema,
                "testGeometry");
        gf.addLeftGeometry(left);

        SQLEncoderMySQL encoder = new SQLEncoderMySQL();
        encoder.setSRID(2356);

        String out = encoder.encode((AbstractFilter) gf);
        LOGGER.fine("Resulting SQL filter is \n" + out);

        //assertEquals("WHERE \"testGeometry\" && GeometryFromText('POLYGON"
        //    + " ((0 0, 0 300, 300 300, 300 0, 0 0))'" + ", 2356)", out);
    }

    public void test2() throws Exception {
    	FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        GeometryFilter gf = factory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        LiteralExpressionImpl left = new BBoxExpressionImpl(new Envelope(10,
                    300, 10, 300));
        gf.addLeftGeometry(left);

        SQLEncoderMySQL encoder = new SQLEncoderMySQL(2346);
        encoder.setDefaultGeometry("testGeometry");

        String out = encoder.encode((AbstractFilter) gf);
        LOGGER.fine("Resulting SQL filter is \n" + out);

        //assertEquals(out,
        //    "WHERE GeometryFromText("
        //    + "'POLYGON ((10 10, 10 300, 300 300, 300 10, 10 10))'"
        //    + ", 2346) && \"testGeometry\"");
    }

    public void testFid() throws Exception {
        FilterFactory filterFac = FilterFactoryFinder.createFilterFactory();

        FidFilter fidFilter = filterFac.createFidFilter("road.345");
        SQLEncoderMySQL encoder = new SQLEncoderMySQL();
        encoder.setFIDMapper(new TypedFIDMapper(
                new BasicFIDMapper("gid", 255, true), "road"));

        String out = encoder.encode((AbstractFilterImpl) fidFilter);
        LOGGER.fine("Resulting SQL filter is \n" + out);
        LOGGER.fine(out + "|" + "WHERE (gid = '345')");

        //assertEquals(out, "WHERE (gid = '345')");
    }

    public void test3() throws Exception {
        FilterFactory filterFac = FilterFactoryFinder.createFilterFactory();
        CompareFilter compFilter = filterFac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        compFilter.addLeftValue(filterFac.createAttributeExpression(
                testSchema, "testInteger"));
        compFilter.addRightValue(filterFac.createLiteralExpression(
                new Double(5)));

        SQLEncoderMySQL encoder = new SQLEncoderMySQL(2346);
        String out = encoder.encode((AbstractFilter) compFilter);
        LOGGER.fine("Resulting SQL filter is \n" + out);

        //assertEquals(out, "WHERE \"testInteger\" = 5.0");
    }

    public void testException() throws Exception {
    	FilterFactory filterFac = FilterFactoryFinder.createFilterFactory();
        GeometryFilter gf = filterFac.createGeometryFilter(AbstractFilter.GEOMETRY_BEYOND);
        LiteralExpressionImpl right = new BBoxExpressionImpl(new Envelope(10,
                    10, 300, 300));
        gf.addRightGeometry(right);

        AttributeExpressionImpl left = new AttributeExpressionImpl(testSchema,
                "testGeometry");
        gf.addLeftGeometry(left);

        try {
            SQLEncoderMySQL encoder = new SQLEncoderMySQL(2346);
            String out = encoder.encode((AbstractFilter) gf);
            LOGGER.fine("out is " + out);
        } catch (SQLEncoderException e) {
            LOGGER.fine(e.getMessage());

            //  assertEquals("Filter type not supported", e.getMessage());
        }
    }
}
