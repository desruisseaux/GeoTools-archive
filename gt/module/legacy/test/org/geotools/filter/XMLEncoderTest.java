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

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.resources.TestData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Unit test for expressions.  This is a complimentary test suite with the
 * filter test suite.
 *
 * @author James MacGill, CCG
 * @author Rob Hranac, TOPP
 */
public class XMLEncoderTest extends LegacyFilterTestSupport {
    /** Feature on which to preform tests */
    private Filter filter = null;

    /** Test suite for this test case */
    TestSuite suite = null;

    /** Constructor with test name. */
    String dataFolder = "";
    boolean setup = false;

    public XMLEncoderTest(String testName) {
        super(testName);

        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
        LOGGER.finer("running XMLEncoderTests");
        ;
        dataFolder = System.getProperty("dataFolder");

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

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test2() throws Exception {
        Filter test = parseDocument("test2.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test3a() throws Exception {
        Filter test = parseDocument("test3a.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test3b() throws Exception {
        Filter test = parseDocument("test3b.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test4() throws Exception {
        Filter test = parseDocument("test4.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test5() throws Exception {
	Filter test = parseDocument("test5.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

      public void test7() throws Exception {
	Filter test = parseDocument("test7.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test8() throws Exception {
        Filter test = parseDocument("test8.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test9() throws Exception {
        Filter test = parseDocument("test9.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test11() throws Exception {
        Filter test = parseDocument("test11.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test12() throws Exception {
        Filter test = parseDocument("test12.xml");

        // LOGGER.fine("parsed filter is: " + test);
    }

    public void test13() throws Exception {
        Filter test = parseDocument("test13.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }

    public void test14() throws Exception {
        Filter test = parseDocument("test14.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }
    
     public void test27() throws Exception {
        Filter test = parseDocument("test27.xml");

        //LOGGER.fine("parsedfilter is: " + test);
    }
    

    public void test28() throws Exception {
        Filter test = parseDocument("test28.xml");

        //LOGGER.fine("parsedfilter is: " + test);
    }
    
  /*  public void test29() throws Exception {
        Filter test = parseDocument("test29.xml");

        //LOGGER.fine("parsed filter is: " + test);
    }*/

    public Filter parseDocument(String uri) throws Exception {
        Filter filter = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(TestData.getResource(this, uri).toExternalForm());
        LOGGER.fine("exporting " + uri);

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

                //_log.getLoggerRepository().setThreshold(Level.DEBUG);
                LOGGER.fine("filter: " + filter);

                StringWriter output = new StringWriter();
                XMLEncoder encode = new XMLEncoder(output, filter);
                LOGGER.fine("Resulting filter XML is \n"
                    + output.getBuffer().toString());
            }
        }

        return filter;
    }
}
