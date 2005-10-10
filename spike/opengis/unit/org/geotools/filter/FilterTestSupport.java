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

import java.util.Arrays;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;


/**
 * Common filter testing code factored up here.
 *
 * @author Chris Holmes
 */
public abstract class FilterTestSupport extends TestCase {
    /** Standard logging instance */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.filter");
    private TypeFactory typeFactory = new TypeFactoryImpl();
    private AttributeFactory attf = new AttributeFactoryImpl();

    
    /** Schema on which to preform tests */
    protected static SimpleFeatureType testSchema = null;

    /** Schema on which to preform tests */
    protected static Feature testFeature = null;
    protected boolean setup = false;

    /**
     * Creates a new instance of TestCaseSupport
     *
     * @param name what to call this...
     */
    public FilterTestSupport(String name) {
        super(name);
    }

    protected void setUp(){
        if (setup) {
            return;
        } else {
            prepareFeatures();
        }

        setup = true;
    }

    protected void prepareFeatures(){
        //_log.getLoggerRepository().setThreshold(Level.INFO);
        // Create the schema attributes
        LOGGER.finer("creating flat feature...");

        AttributeType geometryAttribute = typeFactory.createType("testGeometry",
                LineString.class);
        LOGGER.finer("created geometry attribute");

        AttributeType booleanAttribute = typeFactory.createType("testBoolean",
                Boolean.class);
        LOGGER.finer("created boolean attribute");

        AttributeType charAttribute = typeFactory.createType("testCharacter",
                Character.class);
        AttributeType byteAttribute = typeFactory.createType("testByte",
                Byte.class);
        AttributeType shortAttribute = typeFactory.createType("testShort",
                Short.class);
        AttributeType intAttribute = typeFactory.createType("testInteger",
                Integer.class);
        AttributeType longAttribute = typeFactory.createType("testLong",
                Long.class);
        AttributeType floatAttribute = typeFactory.createType("testFloat",
                Float.class);
        AttributeType doubleAttribute = typeFactory.createType("testDouble",
                Double.class);
        AttributeType stringAttribute = typeFactory.createType("testString",
                String.class);

        AttributeType[] types = {
            geometryAttribute, booleanAttribute, charAttribute, byteAttribute,
            shortAttribute, intAttribute, longAttribute, floatAttribute,
            doubleAttribute, stringAttribute
        };

        // Builds the schema
        testSchema = typeFactory.createFeatureType("testSchema", Arrays.asList(types), null);

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
        testFeature = attf.create(testSchema, null, attributes);
        LOGGER.finer("...flat feature created");

        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
    }
}
