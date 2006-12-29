/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Geomatys
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
 */
package org.geotools.referencing.factory;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.NoSuchIdentifierException;


/**
 * Tests the {@link URN_Parser} class.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class URN_ParserTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(URN_ParserTest.class);
    }

    /**
     * Creates a suite of the given name.
     */
    public URN_ParserTest(final String name) {
        super(name);
    }

    /**
     * The urn bases to test.
     */
    private static final String[] URN_BASES = {
        "urn:ogc:def:"
    };

    /**
     * Tests the main types.
     */
    public void testMainTypes() {
        assertEquals("crs",                 URN_Type.MAIN[0].name);
        assertEquals("datum",               URN_Type.MAIN[1].name);
        assertEquals("cs",                  URN_Type.MAIN[2].name);
        assertEquals("coordinateOperation", URN_Type.MAIN[3].name);
    }

    /**
     * Parses a valid URN.
     */
    public void testParse() throws NoSuchIdentifierException {
        final URN_Parser parser = new URN_Parser(URN_BASES, "urn:ogc:def:CRS:EPSG:6.11.2:4326");
        assertEquals("crs",       parser.type);
        assertEquals("EPSG",      parser.authority);
        assertEquals("6.11.2",    parser.version);
        assertEquals("4326",      parser.code);
        assertEquals("EPSG:4326", parser.getAuthorityCode());
    }

    /**
     * Parses a valid URN without version.
     */
    public void testParseWithoutVersion() throws NoSuchIdentifierException {
        final URN_Parser parser = new URN_Parser(URN_BASES, "urn:ogc:def:CRS:EPSG:4326");
        assertEquals("crs",       parser.type);
        assertEquals("EPSG",      parser.authority);
        assertNull  (             parser.version);
        assertEquals("4326",      parser.code);
        assertEquals("EPSG:4326", parser.getAuthorityCode());
    }

    /**
     * Parses an invalid URN.
     */
    public void testInvalidParse() {
        final String urn = "urn:x-ogc:def:CRS:EPSG:6.8:4326";
        try {
            new URN_Parser(URN_BASES, urn);
            fail();
        } catch (NoSuchIdentifierException e) {
            // This is the expected exception.
            assertEquals(urn, e.getIdentifierCode());
        }
    }

    /**
     * Parses a URN with an unknow type.
     */
    public void testInvalidType() {
        final String urn = "urn:ogc:def:dummy:EPSG:6.8:4326";
        try {
            new URN_Parser(URN_BASES, urn);
            fail();
        } catch (NoSuchIdentifierException e) {
            // This is the expected exception.
            assertEquals("dummy", e.getIdentifierCode());
        }
    }
}
