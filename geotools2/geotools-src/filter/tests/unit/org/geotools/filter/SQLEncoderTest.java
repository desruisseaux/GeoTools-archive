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

import junit.framework.*;
import org.w3c.dom.*;
import java.util.logging.Logger;
import javax.xml.parsers.*;


/**
 * Unit test for expressions.  This is a complimentary test suite with the
 * filter test suite.
 *
 * @author Chris Holmes, TOPP
 */
public class SQLEncoderTest extends FilterTestSupport {



    /** Test suite for this test case */
    TestSuite suite = null;

    /** Constructor with test name. */
    String dataFolder = "";
    boolean setup = false;

    public SQLEncoderTest(String testName) {
        super(testName);
        LOGGER.info("running SQLEncoderTests");
        ;
        dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder = "file:////" + dataFolder + "/tests/unit/testData"; 
            LOGGER.fine("data folder is " + dataFolder);
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
        //_log.getLoggerRepository().setThreshold(Level.DEBUG);
        TestSuite suite = new TestSuite(SQLEncoderTest.class);
        suite.addTestSuite(CapabilitiesTest.class);

        return suite;
    }

    public void test1() throws Exception {
        Filter test = parseDocument(dataFolder + "/test1.xml");
        LOGGER.fine("parsed filter is: " + test);
    }

    public void test2() throws Exception {
        Filter test = parseDocument(dataFolder + "/test2.xml");
        LOGGER.fine("parsed filter is: " + test);
    }

    public void test3a() throws Exception {
        try {
            Filter test = parseDocument(dataFolder + "/test3a.xml");
            LOGGER.fine("parsed filter is: " + test);
        } catch (SQLEncoderException e) {
            //contains geom, should not be supported
            String expectMessage = "Filter type not supported";
            assertTrue(expectMessage.equals(e.getMessage()));
        }
    }

    public void test8() throws Exception {
        Filter test = parseDocument(dataFolder + "/test8.xml");
        LOGGER.fine("parsed filter is: " + test);
    }

    public void test9() throws Exception {
        Filter test = parseDocument(dataFolder + "/test9.xml");
        LOGGER.fine("parsed filter is: " + test);
    }

    /*public void test11() //like filters, uncomment when they are supported
       throws Exception {
       Filter test = parseDocument(dataFolder+"/test11.xml");
       } */
    public void test13() throws Exception {
        Filter test = parseDocument(dataFolder + "/test13.xml");
        LOGGER.fine("parsed filter is: " + test);
    }

    /* public void test14() contains geom filter, not supported
       throws Exception {
       Filter test = parseDocument(dataFolder+"/test14.xml");
       } */
    public Filter parseDocument(String uri) throws Exception {
        Filter filter = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(uri);
        LOGGER.info("exporting " + uri);

        // first grab a filter node
        NodeList nodes = dom.getElementsByTagName("Filter");

        for (int j = 0; j < nodes.getLength(); j++) {
            Element filterNode = (Element) nodes.item(j);
            NodeList list = filterNode.getChildNodes();
            Node child = null;

            for (int i = 0; i < list.getLength(); i++) {
                child = list.item(i);

                //_log.getLoggerRepository().setThreshold(Level.INFO);
                if ((child == null) ||
                        (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }

                filter = FilterDOMParser.parseFilter(child);

                //_log.getLoggerRepository().setThreshold(Level.DEBUG);
                LOGGER.finer("filter: " + filter.getClass().toString());

                //StringWriter output = new StringWriter();
                SQLEncoder encoder = new SQLEncoder();
                String out = encoder.encode((AbstractFilter) filter);

                LOGGER.finer("Resulting SQL filter is \n" + out);
            }
        }

        return filter;
    }
}
