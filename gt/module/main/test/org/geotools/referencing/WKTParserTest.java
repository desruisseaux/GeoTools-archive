/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.referencing;

// J2SE dependencies
import java.io.*;
import java.util.*;
import java.text.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.resources.TestData;
import org.geotools.referencing.wkt.Parser;
import org.geotools.referencing.wkt.AbstractParser;
import org.geotools.referencing.wkt.MathTransformParser;


/**
 * Test the WKT {@link Parser} implementation.
 *
 * @version $Id$
 * @author Yann Cézard
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class WKTParserTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        org.geotools.util.MonolineFormatter.initGeotools();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(WKTParserTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public WKTParserTest(final String name) {
        super(name);
    }

    /**
     * Parse parsing of math transforms.
     */
    public void testMathTransform() throws IOException, ParseException {
        testParsing(new MathTransformParser(), "wkt/MathTransform.txt");
    }

    /**
     * Parse all elements from the specified file. Parsing creates a set of
     * geographic objects. No special processing are done with them; we just
     * check if the parsing work without error and produces distincts objects.
     */
    private void testParsing(final AbstractParser parser, final String filename)
            throws IOException, ParseException
    {
        final BufferedReader reader = TestData.getReader(this, filename);
        final Collection       pool = new HashSet();
        String line;
        while ((line=reader.readLine()) != null) {
            line = line.trim();
            if (line.length()==0 || line.startsWith("#")) {
                continue;
            }
            /*
             * Parse a line. If the parse fails, then dump the WKT
             * and rethrow the exception.
             */
            final Object parsed;
            try {
                parsed = parser.parseObject(line);
            } catch (ParseException exception) {
                System.err.println("-----------------------------");
                System.err.println("Parse failed. Dump WKT below.");
                System.err.println("-----------------------------");
                System.err.println(line);
                Throwable cause = exception.getCause();
                throw exception;
            }
            assertNotNull("Parsing returns null.",                 parsed);
            assertEquals("Inconsistent equals method",             parsed, parsed);
            assertSame("Parsing twice returns different objects.", parsed, parser.parseObject(line));
            assertTrue("An identical object already exists.",      pool.add(parsed));
            assertTrue("Inconsistent hashCode or equals method.",  pool.contains(parsed));
            /*
             * Format the object and parse it again.
             * Ensure that the result is consistent.
             */
            final Object again = parser.parseObject(parser.format(parsed));
            assertEquals("Second parsing produced different objects", parsed, again);
            assertTrue("Inconsistent hashCode or equals method.",  pool.contains(again));
        }
        reader.close();
    }
}
