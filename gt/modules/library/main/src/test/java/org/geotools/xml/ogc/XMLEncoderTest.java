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
package org.geotools.xml.ogc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.filter.BetweenFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterDOMParser;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.NullFilter;
import org.geotools.resources.TestData;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.DocumentWriter;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.XMLHandlerHints;
import org.geotools.xml.filter.FilterSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This code uses the filter parser to generate test cases, and runs it through the encoder.
 * 
 * TODO create the filters manually, and check the output.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 * @author David Zwiers
 * @source $URL$
 */
public class XMLEncoderTest extends FilterTestSupport {


    /** Constructor with test name. */
    String dataFolder = "";

    public XMLEncoderTest(String testName) {
        super(testName);

        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
        LOGGER.finer("running XMLEncoderTests");

        dataFolder = System.getProperty("dataFolder");

        if( dataFolder == null){
        	try {
				TestData.file( this, null );
			} catch (IOException e) {
				LOGGER.finer("data folder is unavailable" + dataFolder);
			}
        }
        if (dataFolder == null) {
            //then we are being run by maven        	
            dataFolder = System.getProperty("basedir");
            dataFolder = "file:////" + "tests/unit/testData"; //url.toString();
            LOGGER.finer("data folder is " + dataFolder);
        }
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
        TestSuite suite = new TestSuite(XMLEncoderTest.class);

        return suite;
    }

    public void test1() throws Exception {
        Filter test = parseDocument("test1.xml");
        assertNotNull( test );
        StringWriter output = new StringWriter();        
        DocumentWriter.writeFragment( test, FilterSchema.getInstance(), output, null);
        System.out.println( output );
        InputStream stream = new StringBufferInputStream( output.toString() );
                
        Object o = DocumentFactory.getInstance( stream, new HashMap(), Level.FINEST );
        assertNotNull( o );
        assertEquals( test, o );
        //LOGGER.fine("parsed filter is: " + test);        
    }

    public void test3a() throws Exception {
        Filter test = parseDocument("test3a.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test3b() throws Exception {
        Filter test = parseDocument("test3b.xml");
        StringWriter output = new StringWriter();        
        DocumentWriter.writeFragment( test, FilterSchema.getInstance(), output, null);

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test4() throws Exception {
        Filter test = parseDocument("test4.xml");
        StringWriter output = new StringWriter();        
        DocumentWriter.writeFragment( test, FilterSchema.getInstance(), output, null);

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test5() throws Exception {
        Filter test = parseDocument("test5.xml");
        StringWriter output = new StringWriter();        
        DocumentWriter.writeFragment( test, FilterSchema.getInstance(), output, null);

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test8() throws Exception {
        Filter test = parseDocument("test8.xml");

        StringWriter output = new StringWriter();
        DocumentWriter.writeFragment(test,
            FilterSchema.getInstance(), output, null);
        
        //System.out.println(output);
        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test9() throws Exception {
        Filter test = parseDocument("test9.xml");
        StringWriter output = new StringWriter();        
        DocumentWriter.writeFragment( test, FilterSchema.getInstance(), output, null);

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test12() throws Exception {
        Filter test = parseDocument("test12.xml");
        StringWriter output = new StringWriter();        
        DocumentWriter.writeFragment( test, FilterSchema.getInstance(), output, null);

        // LOGGER.fine("parsed filter is: " + test);
    }

    public void test13() throws Exception {
        Filter test = parseDocument("test13.xml");
        StringWriter output = new StringWriter();        
        DocumentWriter.writeFragment( test, FilterSchema.getInstance(), output, null);

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test14() throws Exception {
        Filter test = parseDocument("test14.xml");
        StringWriter output = new StringWriter();        
        DocumentWriter.writeFragment( test, FilterSchema.getInstance(), output, null);

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test28() throws Exception {
        Filter test = parseDocument("test28.xml");
        StringWriter output = new StringWriter();        
        DocumentWriter.writeFragment( test, FilterSchema.getInstance(), output, null);

        System.out.println(output);
//        LOGGER.fine("parsedfilter is: " + test);
    }

    public Filter parseDocument(String uri) throws Exception {
        Filter filter = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(TestData.getResource(this, uri).toExternalForm());

        //        LOGGER.fine("exporting " + uri);
        // first grab a filter node
        NodeList nodes = dom.getElementsByTagName("Filter");

        for (int j = 0; j < nodes.getLength(); j++) {
            Element filterNode = (Element) nodes.item(j);
            NodeList list = filterNode.getChildNodes();
            Node child = null;

            for (int i = 0; i < list.getLength(); i++) {
                child = list.item(i);

                //_log.getLoggerRepository().setThreshold(Level.INFO);
                if ((child == null)
                        || (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }

                filter = FilterDOMParser.parseFilter(child);

                StringWriter output = new StringWriter();
                DocumentWriter.writeFragment(filter,
                    FilterSchema.getInstance(), output, null);
                
//                System.out.println(output);
            }
        }
        
        return filter;
    }
    
    // TODO test or ( null, and( fidFilter, null ) ) filter
    public void testStrictHintComplexFilter() throws Exception {
        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        NullFilter null1=factory.createNullFilter();
        null1.nullCheckValue(factory.createAttributeExpression("name"));
        NullFilter null2=factory.createNullFilter();
        null2.nullCheckValue(factory.createAttributeExpression("geom"));
        FidFilter fidFilter = factory.createFidFilter("FID.1");
        Filter inner=null1.and(fidFilter);
        Filter filter=null2.or(inner);
        
        StringWriter output = new StringWriter();
        XMLHandlerHints hints = new XMLHandlerHints();
        hints.put(XMLHandlerHints.FILTER_COMPLIANCE_STRICTNESS,
                XMLHandlerHints.VALUE_FILTER_COMPLIANCE_MEDIUM);
        DocumentWriter.writeFragment(filter, FilterSchema.getInstance(),
                output, hints);
        String string = output.toString().replaceAll("\\s", "");
        assertEquals(
                "<Filterxmlns=\"http://www.opengis.net/ogc\"xmlns:gml=\"http://www.opengis.net/gml\">" +
                "<PropertyIsNull><PropertyName>geom</PropertyName></PropertyIsNull>" +
                "<FeatureIdfid=\"FID.1\"/>" +
                "</Filter>",
                string);
        
        // Note:  Round trip doesn't work in this case because request may returns more features than "filter" will accept
    }

    public void testStrictHintOR() throws Exception {
        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
		Filter filter=factory.createFidFilter("FID.1").or(factory.createFidFilter("FID.2"));
		
        StringWriter output = new StringWriter();
        XMLHandlerHints hints=new XMLHandlerHints();
        hints.put(XMLHandlerHints.FILTER_COMPLIANCE_STRICTNESS, XMLHandlerHints.VALUE_FILTER_COMPLIANCE_MEDIUM);
        DocumentWriter.writeFragment(filter,
            FilterSchema.getInstance(), output, hints);
		String string=output.toString().replaceAll("\\s", "");
        assertEquals("<Filterxmlns=\"http://www.opengis.net/ogc\"xmlns:gml=\"http://www.opengis.net/gml\"><FeatureIdfid=\"FID.1\"/><FeatureIdfid=\"FID.2\"/></Filter>",
        		string);
        
        Filter roundTrip=(Filter) DocumentFactory.getInstance(new ByteArrayInputStream(output.toString().getBytes()), null, Level.OFF);
        assertEquals(filter, roundTrip);
    }
    
    
}
