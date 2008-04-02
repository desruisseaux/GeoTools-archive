/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.measure;

import java.text.Format;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.opengis.geometry.MismatchedDimensionException;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.crs.AbstractCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.crs.DefaultVerticalCRS;
import org.geotools.referencing.cs.DefaultTimeCS;
import org.geotools.referencing.datum.DefaultTemporalDatum;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests formatting done by the {@link CoordinateFormat} class.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class FormatTest {
    /**
     * Test {@link AngleFormat}.
     */
    @Test
    public void testAngleFormat() throws ParseException {
        AngleFormat f = new AngleFormat("DD.ddd\u00B0", Locale.CANADA);
        assertFormat( "20.000\u00B0",  new Angle   ( 20.000), f);
        assertFormat( "20.749\u00B0",  new Angle   ( 20.749), f);
        assertFormat("-12.247\u00B0",  new Angle   (-12.247), f);
        assertFormat( "13.214\u00B0N", new Latitude( 13.214), f);
        assertFormat( "12.782\u00B0S", new Latitude(-12.782), f);

        f = new AngleFormat("DD.ddd\u00B0", Locale.FRANCE);
        assertFormat("19,457\u00B0E", new Longitude( 19.457), f);
        assertFormat("78,124\u00B0S", new Latitude (-78.124), f);

        f = new AngleFormat("DDddd", Locale.CANADA);
        assertFormat("19457E", new Longitude( 19.457), f);
        assertFormat("78124S", new Latitude (-78.124), f);

        f = new AngleFormat("DD\u00B0MM.m", Locale.CANADA);
        assertFormat( "12\u00B030.0", new Angle( 12.50), f);
        assertFormat("-10\u00B015.0", new Angle(-10.25), f);
    }

    /**
     * Formats an object and parse the result. The format
     * output is compared with the expected output.
     */
    private static void assertFormat(final String expected,
                                     final Object value,
                                     final Format format) throws ParseException
    {
        final String label = value.toString();
        final String text  = format.format(value);
        assertEquals("Formatting of \""+label+'"', expected, text);
        assertEquals("Parsing of \""   +label+'"', value, format.parseObject(text));
    }

    /**
     * Tests formatting of a 4-dimensional coordinates.
     */
    @Test
    public void testCoordinateFormat() {
        final Date epoch = new Date(1041375600000L); // January 1st, 2003
        final DefaultTemporalDatum datum = new DefaultTemporalDatum("Time", epoch);
        final AbstractCRS crs = new DefaultCompoundCRS("WGS84 3D + time",
                    DefaultGeographicCRS.WGS84, DefaultVerticalCRS.ELLIPSOIDAL_HEIGHT,
                    new DefaultTemporalCRS("Time", datum, DefaultTimeCS.DAYS));
        final CoordinateFormat format = new CoordinateFormat(Locale.FRANCE);
        format.setCoordinateReferenceSystem(crs);
        format.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));
        final GeneralDirectPosition position = new GeneralDirectPosition(new double[] {
            23.78, -12.74, 127.9, 3.2
        });
        assertEquals("23°46,8'E 12°44,4'S 127,9\u00A0m 4 janv. 2003", format.format(position));
        /*
         * Try a point with wrong dimension.
         */
        final GeneralDirectPosition wrong = new GeneralDirectPosition(new double[] {
            23.78, -12.74, 127.9, 3.2, 8.5
        });
        try {
            assertNotNull(format.format(wrong));
            fail("Excepted a mismatched dimension exception.");
        } catch (MismatchedDimensionException e) {
            // This is the expected dimension.
        }
        /*
         * Try a null CRS. Should formats everything as numbers.
         */
        format.setCoordinateReferenceSystem(null);
        assertEquals("23,78 -12,74 127,9 3,2",     format.format(position));
        assertEquals("23,78 -12,74 127,9 3,2 8,5", format.format(wrong));
        /*
         * Try again the original CRS, but different separator.
         */
        format.setCoordinateReferenceSystem(crs);
        format.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));
        format.setSeparator("; ");
        assertEquals("23°46,8'E; 12°44,4'S; 127,9\u00A0m; 4 janv. 2003", format.format(position));
     }
}
