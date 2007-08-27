/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.util;

import javax.units.SI;
import javax.units.Unit;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link MeasurementRange}.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MeasurementRangeTest extends TestCase {
    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(MeasurementRangeTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public MeasurementRangeTest(final String name) {
        super(name);
    }

    /**
     * Tests unit conversions.
     */
    public void testConversion() {
        final MeasurementRange range = new MeasurementRange(1000f, 2000f, SI.METER);
        assertSame(range, range.convertTo(SI.METER));
        final Unit KILOMETER = SI.KILO(SI.METER);
        assertEquals(new MeasurementRange(1f, 2f, KILOMETER), range.convertTo(KILOMETER));
    }

    /**
     * Tests the bounds values of a range of integers.
     */
    public void testIntegerBounds() {
        final NumberRange range = new NumberRange(10, 20);
        assertEquals(10, range.getMinimum(     ), 0);
        assertEquals(10, range.getMinimum(true ), 0);
        assertEquals( 9, range.getMinimum(false), 0);
        assertEquals(20, range.getMaximum(     ), 0);
        assertEquals(20, range.getMaximum(true ), 0);
        assertEquals(21, range.getMaximum(false), 0);
    }

    /**
     * Tests union and intersection without units and type change.
     */
    public void testIntegerIntersect() {
        NumberRange r1 = new NumberRange(10, 20);
        NumberRange r2 = new NumberRange(15, 30);
        assertTrue (r1.equals(r1));
        assertTrue (r2.equals(r2));
        assertFalse(r1.equals(r2));
        assertEquals(Integer.class, r1.getElementClass());
        assertEquals(Integer.class, r2.getElementClass());
        assertEquals(new NumberRange(10, 30), r1.union(r2));
        assertEquals(new NumberRange(15, 20), r1.intersect(r2));
    }

    /**
     * Tests union and intersection with type change.
     */
    public void testDoubleIntersect() {
        NumberRange r1 = new NumberRange(10.0, 20.0);
        NumberRange r2 = new NumberRange(15.0, 30.0);
        assertEquals(Double.class, r1.getElementClass());
        assertEquals(Double.class, r2.getElementClass());
        assertEquals(new NumberRange(10.0, 30.0), r1.union(r2));
        assertEquals(new NumberRange(15.0, 20.0), r1.intersect(r2));
    }

    /**
     * Tests union and intersection with type change.
     */
    public void testIntegerDoubleIntersect() {
        NumberRange r1 = new NumberRange(10, 20);
        NumberRange r2 = new NumberRange(15.0, 30.0);
        assertEquals(Integer.class, r1.getElementClass());
        assertEquals(Double .class, r2.getElementClass());
        assertEquals(new NumberRange(10.0, 30.0), r1.union(r2));
        assertEquals(new NumberRange(15, 20), r1.intersect(r2));

        r2 = new NumberRange(15.5, 30.0);
        assertEquals(new NumberRange(15.5f, 20.0f), r1.intersect(r2));
    }

    /**
     * Tests union and intersection with type change.
     */
    public void testDoubleIntegerIntersect() {
        NumberRange r1 = new NumberRange(10.0, 20.0);
        NumberRange r2 = new NumberRange(15, 30);
        assertEquals(Double .class, r1.getElementClass());
        assertEquals(Integer.class, r2.getElementClass());
        assertEquals(new NumberRange(10.0, 30.0), r1.union(r2));
        assertEquals(new NumberRange(15, 20), r1.intersect(r2));

        r1 = new NumberRange(10.0, 20.5);
        assertEquals(new NumberRange(15.0f, 20.5f), r1.intersect(r2));
    }

    /**
     * Tests union and intersection involving a unit conversion.
     */
    public void testIntersectWithConversion() {
        final Unit KILOMETER = SI.KILO(SI.METER);
        NumberRange r1 = new MeasurementRange(1000f, 2000f, SI.METER);
        NumberRange r2 = new MeasurementRange(1.5f, 3f, KILOMETER);
        assertEquals(Float.class, r1.getElementClass());
        assertEquals(Float.class, r2.getElementClass());
        assertEquals(new MeasurementRange(1000f, 3000f, SI.METER ), r1.union    (r2));
        assertEquals(new MeasurementRange(1f,    3f,    KILOMETER), r2.union    (r1));
        assertEquals(new MeasurementRange(1500f, 2000f, SI.METER ), r1.intersect(r2));
        assertEquals(new MeasurementRange(1.5f,  2f,    KILOMETER), r2.intersect(r1));
    }

    /**
     * Tests {@link MeasurementRange#toString()} method.
     */
    public void testToString() {
        final MeasurementRange range = new MeasurementRange(10, 20, SI.KILO(SI.METER));
        assertEquals("[10.0, 20.0] km", range.toString());
    }
}
