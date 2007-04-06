package org.geotools.filter;

import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.test.TestData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import junit.framework.Assert;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class DOMParserTestSuite extends TestSuite {
    
    /** Standard logging instance */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.filter");
    protected static AttributeTypeFactory attFactory = AttributeTypeFactory.defaultInstance();

    /** Schema on which to preform tests */
    protected static FeatureType testSchema = null;

    /** Schema on which to preform tests */
    protected static Feature testFeature = null;
    
    protected boolean setup = false;
    
    public DOMParserTestSuite(){
        super("DOM Parser Test Suite");
    }
    
    static void prepareFeatures() throws SchemaException, IllegalAttributeException {
    //_log.getLoggerRepository().setThreshold(Level.INFO);
    // Create the schema attributes
    LOGGER.finer("creating flat feature...");

    AttributeType geometryAttribute = attFactory.newAttributeType("testGeometry",
            LineString.class);
    LOGGER.finer("created geometry attribute");

    AttributeType booleanAttribute = attFactory.newAttributeType("testBoolean",
            Boolean.class);
    LOGGER.finer("created boolean attribute");

    AttributeType charAttribute = attFactory.newAttributeType("testCharacter",
            Character.class);
    AttributeType byteAttribute = attFactory.newAttributeType("testByte",
            Byte.class);
    AttributeType shortAttribute = attFactory.newAttributeType("testShort",
            Short.class);
    AttributeType intAttribute = attFactory.newAttributeType("testInteger",
            Integer.class);
    AttributeType longAttribute = attFactory.newAttributeType("testLong",
            Long.class);
    AttributeType floatAttribute = attFactory.newAttributeType("testFloat",
            Float.class);
    AttributeType doubleAttribute = attFactory.newAttributeType("testDouble",
            Double.class);
    AttributeType stringAttribute = attFactory.newAttributeType("testString",
            String.class);

    AttributeType[] types = {
        geometryAttribute, booleanAttribute, charAttribute, byteAttribute,
        shortAttribute, intAttribute, longAttribute, floatAttribute,
        doubleAttribute, stringAttribute
    };

    // Builds the schema
    testSchema = FeatureTypeFactory.newFeatureType(types,"testSchema");

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
     * The individual tests are defined as xml files!
     * @return Test
     */
    public static Test suite() {
        DOMParserTestSuite suite = new DOMParserTestSuite();
        
        try {
            suite.prepareFeatures();
            suite.addTest( suite.new DomTestXml("test9.xml"));
            // .. etc..

        } catch (SchemaException e) {            
        } catch (IllegalAttributeException e) {            
        }          
        System.out.println( suite.countTestCases() );
        return suite;
    }
    
    /** Quick test of a single xml document */
    class DomTestXml extends Assert implements Test {
        String document;        
        public DomTestXml( String document ){            
            this.document = document;
        }        
        public String toString() {
            return document;
        }
        public int countTestCases() {
            return 1;
        }

        public void run( TestResult result ) {
            System.out.println( getName() );
            result.startTest( this );            
            Protectable p= new Protectable() {
                public void protect() throws Throwable {
                    DomTestXml.this.runBare();
                }
            };
            result.runProtected( this, p);
            result.endTest( this );
        }
        
        public void runBare() throws Throwable {
            Throwable exception= null;
            try {
                runTest();
            } catch (Throwable running) {
                exception= running;
            }
            if (exception != null) throw exception;
        }
        
        public void runTest() throws Throwable {
            Filter filter = parseDocument( document );
            assertNotNull( filter );
            LOGGER.fine("Parsed filter is " + filter );
        }
        public Filter parseDocument(String uri) throws Exception {
            Filter filter = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
           
            Document dom = db.parse(TestData.getResource(this,uri).toExternalForm());
            LOGGER.fine("parsing " + uri);

            // first grab a filter node
            NodeList nodes = dom.getElementsByTagName("Filter");

            for (int j = 0; j < nodes.getLength(); j++) {
                Element filterNode = (Element) nodes.item(j);
                NodeList list = filterNode.getChildNodes();
                Node child = null;

                for (int i = 0; i < list.getLength(); i++) {
                    child = list.item(i);

                    if ((child == null)
                            || (child.getNodeType() != Node.ELEMENT_NODE)) {
                        continue;
                    }

                    filter = FilterDOMParser.parseFilter(child);
                    assertNotNull("Null filter returned", filter);
                    LOGGER.finer("filter: " + filter.getClass().toString());
                    LOGGER.fine("parsed: " + filter.toString());
                    LOGGER.finer("result " + filter.contains(testFeature));
                }
            }

            return filter;
        }
    }
}
