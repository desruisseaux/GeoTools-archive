/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.image.io;

// J2SE dependencies
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests {@link Palette} and {@link PaletteFactory}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PaletteTest extends TestCase {
    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(PaletteTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public PaletteTest(final String name) {
        super(name);
    }

    /**
     * Tests the argument check performed by constructor.
     */
    public void testConstructor() {
        final PaletteFactory factory = PaletteFactory.getDefault(null);
        assertEquals(100, new Palette(factory, "grayscale",    0, 100, 100).upper);
        assertEquals( 50, new Palette(factory, "grayscale", -100,  50, 100).upper);
        try {
            new Palette(factory, "grayscale", 0, 100, -100);
            fail("Should not accept negative size.");
        } catch (IllegalArgumentException e) {
            // This is the expected exception.
        }
        try {
            new Palette(factory, "grayscale", 100, 50, 256);
            fail("Should not accept invalid range.");
        } catch (IllegalArgumentException e) {
            // This is the expected exception.
        }
        assertEquals(40000, new Palette(factory, "grayscale", 1,  40000, 0xFFFF).upper);
        try {
            new Palette(factory, "grayscale", -1,  40000, 0xFFFF);
            fail("Should not accept value out of range.");
        } catch (IllegalArgumentException e) {
            // This is the expected exception.
        }
        try {
            new Palette(factory, "grayscale", 1,  70000, 0xFFFF);
            fail("Should not accept value out of range.");
        } catch (IllegalArgumentException e) {
            // This is the expected exception.
        }
        try {
            new Palette(factory, "grayscale", -40000,  0, 0xFFFF);
            fail("Should not accept value out of range.");
        } catch (IllegalArgumentException e) {
            // This is the expected exception.
        }
    }

    /**
     * Tests {@link PaletteFactory#getAvailableNames}.
     */
    public void testAvailableNames() {
        final List names = Arrays.asList(PaletteFactory.getDefault(null).getAvailableNames());
        assertFalse(names.isEmpty());
        assertTrue (names.contains("rainbow"));
        assertTrue (names.contains("grayscale"));
        assertTrue (names.contains("bell"));
        assertFalse(names.contains("Donald Duck"));
    }

    /**
     * Tests the cache.
     */
    public void testCache() {
        final PaletteFactory factory = PaletteFactory.getDefault(null);
        final Palette first  = factory.getPalettePadValueFirst("rainbow", 100);
        final Palette second = factory.getPalettePadValueFirst("bell",    100);
        final Palette third  = factory.getPalettePadValueFirst("rainbow", 100);
        assertEquals (first, third);
        assertSame   (first, third);
        assertNotSame(first, second);
    }

    /**
     * Tests the color model.
     */
    public void testColorModel() throws IOException {
        final PaletteFactory  factory = PaletteFactory.getDefault(null);
        final Palette         palette = factory.getPalettePadValueFirst("rainbow", 100);
        final IndexColorModel icm     = (IndexColorModel) palette.getColorModel();
        assertEquals(100, icm.getMapSize());
        assertEquals(0, icm.getTransparentPixel());
    }
}
