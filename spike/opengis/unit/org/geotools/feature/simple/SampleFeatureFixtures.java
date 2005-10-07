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
package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.SchemaException;
import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.type.ChoiceAttributeType;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;


/**
 * This is a support class which creates test features for use in testing.
 *
 * @author jamesm
 */
public class SampleFeatureFixtures {
    static TypeFactory typeFactory = new TypeFactoryImpl();
    /**
     * Feature on which to preform tests
     */

    // private Feature testFeature = null;

    /**
     * Creates a new instance of SampleFeatureFixtures
     */
    public SampleFeatureFixtures() {
    }

    public static SimpleFeature createFeature() {
        try {
            SimpleFeatureType testType = createTestType();
            Object[] attributes = createAttributes();
            AttributeFactory factory = new AttributeFactoryImpl();
            return factory.create(testType, attributes);
        } catch (Exception e) {
            Error ae = new AssertionError(
                    "Sample Feature for tests has been misscoded");
            ae.initCause(e);
            throw ae;
        }
    }

    public static Feature createAddressFeature() {
        try {
            return createFeature();

            //FeatureType addressType = createAddressType();
            //Object[] attributes = createComplexAttributes();
            //return addressType.create(attributes);
        } catch (Exception e) {
            Error ae = new AssertionError(
                    "Sample Feature for tests has been misscoded");
            ae.initCause(e);
            throw ae;
        }
    }

    /**
     * creates and returns an array of sample attributes.
     *
     * @return
     */
    public static Object[] createAttributes() {
        Object[] attributes = new Object[10];
        GeometryFactory gf = new GeometryFactory();
        attributes[0] = gf.createPoint(new Coordinate(1, 2));
        attributes[1] = new Boolean(true);
        attributes[2] = new Character('t');
        attributes[3] = new Byte("10");
        attributes[4] = new Short("101");
        attributes[5] = new Integer(1002);
        attributes[6] = new Long(10003);
        attributes[7] = new Float(10000.4);
        attributes[8] = new Double(100000.5);
        attributes[9] = "test string data";

        return attributes;
    }

    //If we go to factories/protected constructors this won't work, will need
    //to move to a types directory, or use the factory
    /*
    public static AttributeType getChoiceAttrType1() {
        return createChoiceAttrType("choiceTest1", createType1Choices());
    }
    */

    public static AttributeType[] createType1Choices() {
        AttributeType[] choices = new AttributeType[3];
        choices[0] = typeFactory.createType("testByte",
                Byte.class);
        choices[1] = typeFactory.createType("testDouble",
                Double.class);
        choices[2] = typeFactory.createType("testString",
                String.class);

        return choices;
    }

    /*
    public static AttributeType getChoiceAttrType2() {
        AttributeType[] choices = new AttributeType[2];
        choices[0] = typeFactory.createType("testString",
                String.class, false);
        choices[1] = typeFactory.createType("testInt",
                Integer.class, false);

        return createChoiceAttrType("choiceTest2", choices);
    }
    */

    /*
    public static AttributeType createChoiceAttrType(String name,
        AttributeType[] choices) {
        return new ChoiceAttributeType(name, choices);
    }

    public static AttributeType createGeomChoiceAttrType(String name,
        GeometryAttributeType[] choices) {
        return new ChoiceAttributeType.Geometric(name, choices);
    }

    public static AttributeType getChoiceGeomType() {
        GeometryAttributeType[] choices = new GeometryAttributeType[2];
        choices[0] = (GeometryAttributeType) AttributeTypeFactory
            .newAttributeType("testLine", LineString.class);
        choices[1] = (GeometryAttributeType) AttributeTypeFactory
            .newAttributeType("testMultiLine", MultiLineString.class);

        return createGeomChoiceAttrType("choiceGeom", choices);
    }

    public static FeatureType createChoiceFeatureType() {
        FeatureTypeFactory typeFactory = FeatureTypeFactory.newInstance("test");
        typeFactory.addType(getChoiceAttrType1());
        typeFactory.addType(getChoiceAttrType2());
        typeFactory.addType(getChoiceGeomType());
        typeFactory.setDefaultGeometry((GeometryAttributeType) typeFactory.get(
                2));

        try {
            return typeFactory.getFeatureType();
        } catch (SchemaException se) {
            throw new RuntimeException("Programmer error making choice ftype",
                se);
        }
    }
    */

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws SchemaException
     */
    public static SimpleFeatureType createTestType() throws SchemaException {
        QName name = new QName("test");
        List<AttributeType>atts = new ArrayList<AttributeType>();
        atts.add(typeFactory.createType("testGeometry", Point.class));
        atts.add(typeFactory.createType("testBoolean", Boolean.class));
        atts.add(typeFactory.createType("testCharacter", Character.class));
        atts.add(typeFactory.createType("testByte", Byte.class));
        atts.add(typeFactory.createType("testShort", Short.class));
        atts.add(typeFactory.createType("testInteger", Integer.class));
        atts.add(typeFactory.createType("testLong", Long.class));
        atts.add(typeFactory.createType("testFloat", Float.class));
        atts.add(typeFactory.createType("testDouble", Double.class));
        atts.add(typeFactory.createType("testString", String.class));

        return typeFactory.createFeatureType(name, atts, (GeometryType)atts.get(0));
    }
}
