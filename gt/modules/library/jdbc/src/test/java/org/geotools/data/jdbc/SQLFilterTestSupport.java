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
package org.geotools.data.jdbc;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;


/**
 * Common filter testing code factored up here.
 *
 * @author Chris Holmes
 * @source $URL: http://gtsvn.refractions.net/geotools/trunk/gt/modules/library/jdbc/src/test/java/org/geotools/filter/SQLFilterTestSupport.java $
 */
public abstract class SQLFilterTestSupport extends TestCase {
    /** Standard logging instance */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.filter");
    protected static AttributeTypeFactory attFactory = AttributeTypeFactory.defaultInstance();

    /** Schema on which to preform tests */
    protected static FeatureType testSchema = null;

    /** Schema on which to preform tests */
    protected static Feature testFeature = null;
    protected boolean setup = false;

    /**
     * Creates a new instance of TestCaseSupport
     *
     * @param name what to call this...
     */
    public SQLFilterTestSupport(String name) {
        super(name);
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
        LOGGER.finer("created boolean attribute");

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
            geometryAttribute, booleanAttribute, charAttribute, byteAttribute,
            shortAttribute, intAttribute, longAttribute, floatAttribute,
            doubleAttribute, stringAttribute
        };

        // Builds the schema
        testSchema = FeatureTypeBuilder.newFeatureType(types,"testSchema");

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
}
