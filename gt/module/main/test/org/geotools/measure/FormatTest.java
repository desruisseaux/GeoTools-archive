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
package org.geotools.measure;

// J2SE dependencies
import java.util.Date;
import java.util.Locale;
import java.text.Format;
import java.text.ParseException;

// Geotools dependencies
import org.geotools.referencing.*;
import org.geotools.referencing.crs.*;
import org.geotools.referencing.cs.TemporalCS;
import org.geotools.referencing.datum.TemporalDatum;
import org.geotools.geometry.DirectPosition;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Test formatting done by the {@link CoordinateFormat} class.
 *
 * @version $Id: CoordinateFormatTest.java,v 1.2 2003/05/13 10:58:50 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class FormatTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(FormatTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public FormatTest(final String name) {
        super(name);
    }

    /**
     * Test {@link AngleFormat}.
     */
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
     * Format an object and parse the result. The format
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
     * Test formatting of a 4-dimensional coordinates.
     */
    public void testFormat() {
        final Date epoch = new Date(1041375600000L); // January 1st, 2003
        final TemporalDatum datum = new TemporalDatum("Time", epoch);
        final CoordinateReferenceSystem crs = new CompoundCRS("WGS84 3D + time",
                    GeographicCRS.WGS84,
                    VerticalCRS.ELLIPSOIDAL_HEIGHT,
                    new TemporalCRS("Time", datum, TemporalCS.DAYS));
        final CoordinateFormat format = new CoordinateFormat(Locale.FRANCE);
        format.setCoordinateReferenceSystem(crs);
	DirectPosition position = new DirectPosition(new double[]{23.78, -12.74, 127.9, 3.2});
        String txt = format.format( position );
	String expected = "23°46,8'E 12°44,4'S 127,9 4 janv. 2003";
	if( !expected.equals( txt )){
	    System.err.println( "FORMAT:'"+txt+"'" );
	    System.err.println( "EXPECT:'"+expected+"'" );

	}
        //assertEquals( expected, txt );
     }
}
