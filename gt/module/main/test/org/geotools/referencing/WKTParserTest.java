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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.referencing.crs.ProjectedCRS;
import org.geotools.referencing.wkt.AbstractParser;
import org.geotools.referencing.wkt.MathTransformParser;
import org.geotools.referencing.wkt.Parser;
import org.geotools.resources.TestData;


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
     * Test a hard coded version of a WKT. This is more convenient for debugging.
     */
    public void testHardCoded() throws ParseException {
        final Parser parser = new Parser();
        String       wkt1, wkt2;
        ProjectedCRS crs1, crs2;
        /*
         * First, rather simple Mercator projection.
         * Uses standard units and axis order.
         */
        wkt1 = "PROJCS[\"Mercator test\",\n"                             +
               "  GEOGCS[\"WGS84\",\n"                                   +
               "    DATUM[\"WGS84\",\n"                                  +
               "      SPHEROID[\"WGS84\", 6378137.0, 298.257223563]],\n" +
               "    PRIMEM[\"Greenwich\", 0.0],\n"                       +
               "    UNIT[\"degree\", 0.017453292519943295],\n"           +
               "    AXIS[\"Longitude\", EAST],\n"                        +
               "    AXIS[\"Latitude\", NORTH]],\n"                       +
               "  PROJECTION[\"Mercator_1SP\"],\n"                       +
               "  PARAMETER[\"central_meridian\", -20.0],\n"             +
               "  PARAMETER[\"scale_factor\", 1.0],\n"                   +
               "  PARAMETER[\"false_easting\", 500000.0],\n"             +
               "  PARAMETER[\"false_northing\", 0.0],\n"                 +
               "  UNIT[\"m\", 1.0],\n"                                   +
               "  AXIS[\"x\", EAST],\n"                                  +
               "  AXIS[\"y\", NORTH]]\n";
        crs1 = (ProjectedCRS) parser.parseObject(wkt1);
        wkt2 = parser.format(crs1);
        crs2 = (ProjectedCRS)parser.parseObject(wkt2);
        assertEquals(crs1, crs2);
        assertEquals("Mercator_1SP", crs1.getConversionFromBase().getMethod().getName().getCode());
        assertTrue(crs1.getConversionFromBase().getMathTransform().toWKT().startsWith("PARAM_MT[\"Mercator_1SP\""));
        /*
         * Same Mercator projection as above, but
         * switch longitude and latitude axis.
         */
        wkt1 = "PROJCS[\"Mercator test\",\n"                             +
               "  GEOGCS[\"WGS84\",\n"                                   +
               "    DATUM[\"WGS84\",\n"                                  +
               "      SPHEROID[\"WGS84\", 6378137.0, 298.257223563]],\n" +
               "    PRIMEM[\"Greenwich\", 0.0],\n"                       +
               "    UNIT[\"degree\", 0.017453292519943295],\n"           +
               "    AXIS[\"Latitude\", NORTH],\n"                        +
               "    AXIS[\"Longitude\", EAST]],\n"                       +
               "  PROJECTION[\"Mercator_1SP\"],\n"                       +
               "  PARAMETER[\"central_meridian\", -20.0],\n"             +
               "  PARAMETER[\"scale_factor\", 1.0],\n"                   +
               "  PARAMETER[\"false_easting\", 500000.0],\n"             +
               "  PARAMETER[\"false_northing\", 0.0],\n"                 +
               "  UNIT[\"m\", 1.0],\n"                                   +
               "  AXIS[\"x\", EAST],\n"                                  +
               "  AXIS[\"y\", NORTH]]\n";
        crs1 = (ProjectedCRS) parser.parseObject(wkt1);
        wkt2 = parser.format(crs1);
        crs2 = (ProjectedCRS)parser.parseObject(wkt2);
        assertEquals(crs1, crs2);
        assertEquals("Mercator_1SP", crs1.getConversionFromBase().getMethod().getName().getCode());
        assertTrue(crs1.getConversionFromBase().getMathTransform().toWKT().startsWith("CONCAT_MT[PARAM_MT["));
        /*
         * Try an other projection (Transverse Mercator).
         */
        wkt1 = "PROJCS[\"OSGB 1936 / British National Grid\",\n"                                          +
               "  GEOGCS[\"OSGB 1936\",\n"                                                                +
               "    DATUM[\"OSGB_1936\",\n"                                                               +
               "      SPHEROID[\"Airy 1830\", 6377563.396, 299.3249646, AUTHORITY[\"EPSG\",\"7001\"]],\n" +
               "      TOWGS84[375.0, -111.0, 431.0, 0.0, 0.0, 0.0, 0.0],\n"                               +
               "      AUTHORITY[\"EPSG\",\"6277\"]],\n"                                                   +
               "    PRIMEM[\"Greenwich\",0.0, AUTHORITY[\"EPSG\",\"8901\"]],\n"                           +
               "    UNIT[\"DMSH\",0.0174532925199433, AUTHORITY[\"EPSG\",\"9108\"]],\n"                   +
               "    AXIS[\"Lat\",NORTH],AXIS[\"Long\",EAST], AUTHORITY[\"EPSG\",\"4277\"]],\n"            +
               "  PROJECTION[\"Transverse_Mercator\"],\n"                                                 +
               "  PARAMETER[\"latitude_of_origin\", 49.0],\n"                                             +
               "  PARAMETER[\"central_meridian\", -2.0],\n"                                               +
               "  PARAMETER[\"scale_factor\", 0.999601272],\n"                                            +
               "  PARAMETER[\"false_easting\", 400000.0],\n"                                              +
               "  PARAMETER[\"false_northing\", -100000.0],\n"                                            +
               "  UNIT[\"metre\", 1.0, AUTHORITY[\"EPSG\",\"9001\"]],\n"                                  +
               "  AXIS[\"E\",EAST],\n"                                                                    +
               "  AXIS[\"N\",NORTH],\n"                                                                   +
               "  AUTHORITY[\"EPSG\",\"27700\"]]\n";
        crs1 = (ProjectedCRS) parser.parseObject(wkt1);
        wkt2 = parser.format(crs1);
        crs2 = (ProjectedCRS)parser.parseObject(wkt2);
        assertEquals(crs1, crs2);
        assertEquals("Transverse_Mercator", crs1.getConversionFromBase().getMethod().getName().getCode());
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
        if (reader == null) {
            throw new FileNotFoundException(filename);
        }
        final Collection pool = new HashSet();
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
}
