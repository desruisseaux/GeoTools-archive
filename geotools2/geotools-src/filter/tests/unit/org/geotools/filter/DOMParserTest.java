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
package org.geotools.filter;

import com.vividsolutions.jts.geom.*;
import junit.framework.*;
import org.geotools.data.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import javax.xml.parsers.*;


/**
 * Unit test for expressions.  This is a complimentary test suite with the
 * filter test suite.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */
public class DOMParserTest extends TestCase {
    /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.defaultcore");

    /** Schema on which to preform tests */
    private static FeatureType testSchema = null;

    /** Schema on which to preform tests */
    private static Feature testFeature = null;

    /** Feature on which to preform tests */
    private Filter filter = null;

    /** Test suite for this test case */
    TestSuite suite = null;

    /** Constructor with test name. */
    String dataFolder = "";
    boolean setup = false;

    public DOMParserTest(String testName) {
        super(testName);
        LOGGER.info("running DOMParserTests");
        System.out.println("running DOMParserTests");
        dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder = "file:////" + dataFolder + "/tests/unit/testData"; //url.toString();
            LOGGER.fine("data folder is " + dataFolder);
        }
    }

    /**
     * Main for test runner.
     *
     * @param args the passed in arguments (not used).
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
        //_log.getLoggerRepository().setThreshold(Level.INFO);
        TestSuite suite = new TestSuite(DOMParserTest.class);

        return suite;
    }

    /**
     * Sets up a schema and a test feature.
     *
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() throws SchemaException, IllegalFeatureException {
        if (setup) {
            return;
        }

        setup = true;

        // Create the schema attributes
        LOGGER.finer("creating flat feature...");

        AttributeType geometryAttribute = new AttributeTypeDefault("testGeometry",
                LineString.class);
        LOGGER.finer("created geometry attribute");

        AttributeType booleanAttribute = new AttributeTypeDefault("testBoolean",
                Boolean.class);
        LOGGER.finer("created boolean attribute");

        AttributeType charAttribute = new AttributeTypeDefault("testCharacter",
                Character.class);
        AttributeType byteAttribute = new AttributeTypeDefault("testByte",
                Byte.class);
        AttributeType shortAttribute = new AttributeTypeDefault("testShort",
                Short.class);
        AttributeType intAttribute = new AttributeTypeDefault("testInteger",
                Integer.class);
        AttributeType longAttribute = new AttributeTypeDefault("testLong",
                Long.class);
        AttributeType floatAttribute = new AttributeTypeDefault("testFloat",
                Float.class);
        AttributeType doubleAttribute = new AttributeTypeDefault("testDouble",
                Double.class);
        AttributeType doubleAttribute2 = new AttributeTypeDefault("testZeroDouble",
                Double.class);
        AttributeType stringAttribute = new AttributeTypeDefault("testString",
                String.class);

        // Builds the schema
        testSchema = new FeatureTypeFlat(geometryAttribute);
        LOGGER.finer("created feature type and added geometry");
        testSchema = testSchema.setAttributeType(booleanAttribute);
        LOGGER.finer("added boolean to feature type");
        testSchema = testSchema.setAttributeType(charAttribute);
        LOGGER.finer("added character to feature type");
        testSchema = testSchema.setAttributeType(byteAttribute);
        LOGGER.finer("added byte to feature type");
        testSchema = testSchema.setAttributeType(shortAttribute);
        LOGGER.finer("added short to feature type");
        testSchema = testSchema.setAttributeType(intAttribute);
        LOGGER.finer("added int to feature type");
        testSchema = testSchema.setAttributeType(longAttribute);
        LOGGER.finer("added long to feature type");
        testSchema = testSchema.setAttributeType(floatAttribute);
        LOGGER.finer("added float to feature type");
        testSchema = testSchema.setAttributeType(doubleAttribute);
        LOGGER.finer("added double to feature type");
        testSchema = testSchema.setAttributeType(doubleAttribute2);
        LOGGER.finer("added double to feature type");
        testSchema = testSchema.setAttributeType(stringAttribute);
        LOGGER.finer("added string to feature type");

        GeometryFactory geomFac = new GeometryFactory();

        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(1, 2);
        coords[1] = new Coordinate(3, 4);
        coords[2] = new Coordinate(5, 6);

        // Builds the test feature
        Object[] attributes = new Object[11];
        attributes[0] = geomFac.createLineString(coords);
        attributes[1] = new Boolean(true);
        attributes[2] = new Character('t');
        attributes[3] = new Byte("10");
        attributes[4] = new Short("101");
        attributes[5] = new Integer(1002);
        attributes[6] = new Long(10003);
        attributes[7] = new Float(10000.4);
        attributes[8] = new Double(100000.5);
        attributes[9] = new Double(0.0);
        attributes[10] = "test string data";

        // Creates the feature itself
        FeatureFactory factory = new FeatureFactory(testSchema);
        testFeature = factory.create(attributes);
        LOGGER.finer("...flat feature created");
    }

    public void test1() throws Exception {
        Filter test = parseDocument(dataFolder + "/test1.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test2() throws Exception {
        Filter test = parseDocument(dataFolder + "/test2.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test3a() throws Exception {
        Filter test = parseDocument(dataFolder + "/test3a.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test3b() throws Exception {
        Filter test = parseDocument(dataFolder + "/test3b.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test4() throws Exception {
        Filter test = parseDocument(dataFolder + "/test4.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test8() throws Exception {
        Filter test = parseDocument(dataFolder + "/test8.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test9() throws Exception {
        Filter test = parseDocument(dataFolder + "/test9.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test11() throws Exception {
        Filter test = parseDocument(dataFolder + "/test11.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test12() throws Exception {
        Filter test = parseDocument(dataFolder + "/test12.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test13() throws Exception {
        Filter test = parseDocument(dataFolder + "/test13.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test14() throws Exception {
        Filter test = parseDocument(dataFolder + "/test14.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test15() throws Exception {
        Filter test = parseDocument(dataFolder + "/test15.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test16() throws Exception {
        Filter test = parseDocument(dataFolder + "/test16.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public void test27() throws Exception {
        Filter test = parseDocument(dataFolder + "/test27.xml");
        LOGGER.fine("parsed filter is " + test);
    }

    public Filter parseDocument(String uri) throws Exception {
        Filter filter = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(uri);
        LOGGER.info("parsing " + uri);

        // first grab a filter node
        NodeList nodes = dom.getElementsByTagName("Filter");

        for (int j = 0; j < nodes.getLength(); j++) {
            Element filterNode = (Element) nodes.item(j);
            NodeList list = filterNode.getChildNodes();
            Node child = null;

            for (int i = 0; i < list.getLength(); i++) {
                child = list.item(i);

                if ((child == null) ||
                        (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }

                filter = FilterDOMParser.parseFilter(child);
                assertNotNull("Null filter returned", filter);
                LOGGER.finer("filter: " + filter.getClass().toString());
                LOGGER.info("parsed: " + filter.toString());
                LOGGER.finer("result " + filter.contains(testFeature));
            }
        }

        return filter;
    }
}
