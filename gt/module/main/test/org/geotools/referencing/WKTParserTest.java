/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le D�veloppement
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
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.referencing.wkt.AbstractParser;
import org.geotools.referencing.wkt.MathTransformParser;
import org.geotools.referencing.wkt.Parser;
import org.geotools.resources.TestData;


/**
 * Test the WKT {@link Parser} implementation.
 *
 * @version $Id$
 * @author Yann C�zard
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
     * Parse parsing of coordinate reference systems.
     */
    public void testCoordinateReferenceSystem() throws IOException, ParseException {
        testParsing(new Parser(), "wkt/CoordinateReferenceSystem.txt");
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
                System.err.println();
                System.err.println("-----------------------------");
                System.err.println("Parse failed. Dump WKT below.");
                System.err.println("-----------------------------");
                System.err.println(line);
                System.err.println();
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
            String formatted = parser.format(parsed);
            if (line.indexOf("semi_major")<0) formatted=remove(formatted, "semi_major");
            if (line.indexOf("semi_minor")<0) formatted=remove(formatted, "semi_minor");
            final Object again;
            try {
                again = parser.parseObject(formatted);
            } catch (ParseException exception) {
                System.err.println();
                System.err.println("------------------------------------");
                System.err.println("Second parse failed. Dump WKT below.");
                System.err.println("------------------------------------");
                System.err.println(line);
                System.err.println();
                System.err.println("------ Reformatted WKT below -------");
                System.err.println();
                System.err.println(formatted);
                System.err.println();
                throw exception;
            }
            assertEquals("Second parsing produced different objects", parsed, again);
            assertTrue("Inconsistent hashCode or equals method.",  pool.contains(again));
        }
        reader.close();
    }

    /**
     * Remove the "PARAMETER" statement of the given name. This is needed because
     * "semi_major" and "semi_minor" are not explicit parameters in the original
     * WKT strings. The WKT formatter add them, but may format them in a different
     * unit. Parsing them again may result in rounding errors.
     */
    private static String remove(final String wkt, final String param) {
        int offset = -1;
        while ((offset=wkt.indexOf("PARAMETER[", offset+1)) >= 0) {
            final int present = wkt.indexOf(param);
            int end = wkt.indexOf(']', offset);
            if (present>=offset && present<end) {
                final int length = wkt.length();
                while (++end<length && Character.isWhitespace(wkt.charAt(end)));
                if (end<length && wkt.charAt(end)==',') {
                    end++;
                }
                return wkt.substring(0, offset) + wkt.substring(end);
            }
        }
        return wkt;
    }
}
