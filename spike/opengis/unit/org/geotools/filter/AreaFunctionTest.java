/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * Unit test for expressions.  This is a complimentary test suite with the
 * filter test suite.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */                                
public class AreaFunctionTest extends TestCase {
    
    TypeFactory typeFactory = new TypeFactoryImpl();

      /** Standard logging instance */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.filter");
    /** Feature on which to preform tests */
    private static Feature testFeature = null;

    /** Schema on which to preform tests */
    private static SimpleFeatureType testSchema = null;
    boolean setup = false;
    /** Test suite for this test case */
    TestSuite suite = null;


    /** 
     * Constructor with test name.
     */
    public AreaFunctionTest(String testName) {
        super(testName);
        //BasicConfigurator.configure();
    
        
    }        
    
    /** 
     * Main for test runner.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** 
     * Required suite builder.
     * @return A test suite for this unit test.
     */
    public static Test suite() {
       
        TestSuite suite = new TestSuite(AreaFunctionTest.class);
        return suite;
    }
    
    /** 
     * Sets up a schema and a test feature.
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalFeatureException If problem setting up the feature.
     */
    protected void setUp() throws Exception{
        if (setup) {
            return;
        }
        prepareFeatures();

        setup = true;
    }

    //HACK - this is cut and pasted from filter module tests.  Should be 
    //in a test support module.
    protected void prepareFeatures()
        throws Exception {
        //_log.getLoggerRepository().setThreshold(Level.INFO);
        // Create the schema attributes
        LOGGER.finer("creating flat feature...");

        AttributeType geometryAttribute = typeFactory.createType("testGeometry",
                Polygon.class);
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
        
        List<AttributeType>typesList = Arrays.asList(types);

        // Builds the schema
        testSchema = typeFactory.createFeatureType(new QName("testSchema"), typesList, (GeometryType)geometryAttribute);

        // Creates coordinates for the linestring
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate(0, 0);
        coords[1] = new Coordinate(10, 0);
        coords[2] = new Coordinate(10, 10);
        coords[3] = new Coordinate(0, 10);
        coords[4] = new Coordinate(0, 0);

        // Builds the test feature
        Object[] attributes = new Object[10];
        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        LinearRing ring = gf.createLinearRing(coords);
        attributes[0] = gf.createPolygon(ring,null);
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
        AttributeFactory attFac = new AttributeFactoryImpl();
        testFeature = attFac.create(testSchema, null, attributes);
        LOGGER.finer("...flat feature created");

        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
    }

    static FilterFactory filterFactory = FilterFactory.createFilterFactory();
     /** 
     * Tests the min function expression.
     */
    public void testAreaFunction()
        throws IllegalFilterException {
            
        Expression a = filterFactory.createAttributeExpression(testSchema, "testGeometry");         
        
        AreaFunction area = new AreaFunction();
        area.setArgs(new Expression[]{a});         
        assertEquals(100d,((Double)area.getValue(testFeature)).doubleValue(),0);
    }
    
   
    
}
