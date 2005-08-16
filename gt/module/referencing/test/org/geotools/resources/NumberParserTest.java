/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.resources;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link NumberParser} class.
 *
 * @version $Id$
 * @author Ian Schneider
 */
public class NumberParserTest extends TestCase {
    /**
     * The parser to test.
     */
    private NumberParser numberParser;

    /**
     * Constructs a test case with the given name.
     */
    public NumberParserTest(String name) {
        super(name);
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(NumberParserTest.class);
    }

    /**
     * Constructs the parser to test.
     */
    protected void setUp() throws Exception {
        numberParser = new NumberParser();
    }

    /**
     * Verify that the Geotools and the J2SE parser both succed or both
     * fails in parsing the given string as a floating point number.
     */
    private void testDouble(String s) {
        boolean javafail = false;
        boolean gtfail = false;
        double javadouble = 0;
        double gtdouble = 0;
        try {
            // lets trim just to make sure
            javadouble = Double.parseDouble(s.trim());
        } catch(NumberFormatException nfe) {
            javafail = true;
        }

        try {
            gtdouble = numberParser.parseDouble(s);
        } catch(NumberFormatException nfe) {
            gtfail = true;
        }

        if (javafail) {
            assertTrue("gtfailure '" + s + "'", gtfail);
        }

        if (gtfail) {
            assertTrue("javafailure '" + s + "'", javafail);
        }

        if (!javafail && !gtfail) {
            assertEquals(javadouble, gtdouble, 0);
        }
    }

    /**
     * Verify that the Geotools and the J2SE parser both succed
     * or both fails in parsing the given string as an integer.
     */
    private void testInteger(String s) {
        boolean javafail = false;
        boolean gtfail = false;
        int javaint = 0;
        int gtint = 0;
        try {
            // Integer parse will not trim values...
            javaint = Integer.parseInt(s.trim());
        } catch(NumberFormatException nfe) {
            javafail = true;
        }

        try {
            gtint = numberParser.parseInt(s);
        } catch(NumberFormatException nfe) {
            gtfail = true;
        }

        if (javafail) {
            assertTrue("gtfailure '" + s + "' = " + gtint, gtfail);
        }

        if (gtfail) {
            assertTrue("javafailure '" + s + "' = " + javaint, javafail);
        }

        if (!javafail && !gtfail) {
            assertEquals(javaint, gtint, 0);
        }
    }
    
    private void testLong(String s) {
        boolean javafail = false;
        boolean gtfail = false;
        long javaint = 0;
        long gtint = 0;
        try {
            // Integer parse will not trim values...
            javaint = Long.parseLong(s.trim());
        } catch(NumberFormatException nfe) {
            javafail = true;
        }

        try {
            gtint = numberParser.parseLong(s);
        } catch(NumberFormatException nfe) {
            nfe.printStackTrace();
            gtfail = true;
        }

        if (javafail) {
            assertTrue("gtfailure '" + s + "' = " + gtint, gtfail);
        }

        if (gtfail) {
            assertTrue("javafailure '" + s + "' = " + javaint, javafail);
        }

        if (!javafail && !gtfail) {
            assertEquals(javaint, gtint, 0);
        }
    }

    /**
     * Test parsing of valid floating point numbers.
     */
    public void testValidDoubles() {
        testDouble("4.275");
        testDouble("0.123e5");
        testDouble(".123e5");
        testDouble("23e5");
        testDouble("3e5");
        testDouble("         123.456   ");
        testDouble("         123e123   ");
        testDouble("         123.456   ");
        testDouble("10e");
        testDouble("10.");
        testDouble("10.e");
        testDouble(".");
        testDouble(" -2000 ");

        // check garbage bytes robustness
        char[] zeros = new char[] { 0, 49, 49, 0 };
        assertEquals((double) 11, numberParser.parseDouble(new String(zeros)), 0);
    }

    /**
     * Test parsing of valid integers.
     */
    public void testValidIntegers() {
        testInteger("400");
        testInteger(" 400");
        testInteger(" 400 ");
        testInteger("400 ");
        testInteger(" -2000 ");
        testInteger(new String(new char[] {'0','1'}));
    }
    
    public void testValidLongs() {
        testLong("400");
        testLong(" 400");
        testLong(" 400 ");
        testLong("400 ");
        testLong(" -2000 ");
        testLong(" 12345678901 ");
        testLong("1111111111111111111");
        testInteger(new String(new char[] {'0','1'}));
    }


    /**
     * Test parsing of invalid strings.
     */
    public void testInvalidDoubles() {
        testDouble("");
        testDouble("x");
        testDouble("e");
        testDouble("\b");
    }

    /**
     * Run the test from the command line.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
