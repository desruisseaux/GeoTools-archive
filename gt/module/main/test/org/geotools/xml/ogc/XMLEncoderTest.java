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
package org.geotools.xml.ogc;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.filter.BetweenFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterDOMParser;
import org.geotools.filter.FilterFactory;
import org.geotools.resources.TestData;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.DocumentWriter;
import org.geotools.xml.SchemaFactory;
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

        //LOGGER.fine("parsedfilter is: " + test);
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
}
