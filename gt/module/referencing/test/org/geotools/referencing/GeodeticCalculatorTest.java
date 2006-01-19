/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Management Committee (PMC)
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
 *    Foundation, Inc.. 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.referencing;

// J2SE dependencies and extensions
import java.awt.Shape;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import javax.units.SI;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;


/**
 * Tests the geodetic calculator.
 *
 * @source $URL$
 * @version $Id$
 */
public class GeodeticCalculatorTest extends TestCase {
    /**
     * Run the test from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(GeodeticCalculatorTest.class);
    }

    /**
     * Construct a new test case.
     */
    public GeodeticCalculatorTest(final String name) {
        super(name);
    }

    /**
     * Tests some trivial azimuth directions.
     */
    public void testAzimuth() {
        final double EPS = 2E-1;
        final GeodeticCalculator calculator = new GeodeticCalculator();
        assertTrue(calculator.getCoordinateReferenceSystem() instanceof GeographicCRS);
        calculator.setAnchorPoint(12, 20);
        calculator.setDestinationPoint(13, 20);  assertEquals("East",   90, calculator.getAzimuth(), EPS);
        calculator.setDestinationPoint(12, 21);  assertEquals("North",   0, calculator.getAzimuth(), EPS);
        calculator.setDestinationPoint(11, 20);  assertEquals("West",  -90, calculator.getAzimuth(), EPS);
        calculator.setDestinationPoint(12, 19);  assertEquals("South", 180, calculator.getAzimuth(), EPS);
    }

    /**
     * Test path on the 45th parallel. Data for this test come from the
     * <A HREF="http://www.univ-lemans.fr/~hainry/articles/loxonavi.html">Orthodromie et
     * loxodromie</A> page.
     */
    public void testParallel45() {
        // Column 1: Longitude difference in degrees.
        // Column 2: Orthodromic distance in kilometers
        // Column 3: Loxodromic  distance in kilometers
        final double[] DATA = {
              0.00,     0,      0,
             11.25,   883,    884,
             22.50,  1762,   1768,
             33.75,  2632,   2652,
             45.00,  3489,   3536,
             56.25,  4327,   4419,
             67.50,  5140,   5303,
             78.75,  5923,   6187,
             90.00,  6667,   7071,
            101.25,  7363,   7955,
            112.50,  8002,   8839,
            123.75,  8573,   9723,
            135.00,  9064,  10607,
            146.25,  9463,  11490,
            157.50,  9758,  12374,
            168.75,  9939,  13258,
            180.00, 10000,  14142
        };
        final double             R          = 20000/Math.PI;
        final DefaultEllipsoid   ellipsoid  = DefaultEllipsoid.createEllipsoid("Test",R,R,SI.KILO(SI.METER));
        final GeodeticCalculator calculator = new GeodeticCalculator(ellipsoid);
        calculator.setAnchorPoint(0, 45);
        for (int i=0; i<DATA.length; i+=3) {
            calculator.setDestinationPoint(DATA[i], 45);
            final double orthodromic = calculator.getOrthodromicDistance();
//          final double loxodromic  = calculator. getLoxodromicDistance();
            assertEquals("Orthodromic distance", DATA[i+1], orthodromic, 0.75);
//          assertEquals( "Loxodromic distance", DATA[i+2], loxodromic,  0.75);
            /*
             * Test the orthodromic path. We compare its length with the expected length.
             */
            int    count=0;
            double length=0, lastX=Double.NaN, lastY=Double.NaN;
            final Shape        path     = calculator.getGeodeticCurve(1000);
            final PathIterator iterator = path.getPathIterator(null, 0.1);
            final double[]     buffer   = new double[6];
            while (!iterator.isDone()) {
                switch (iterator.currentSegment(buffer)) {
                    case PathIterator.SEG_LINETO: {
                        count++;
                        length += ellipsoid.orthodromicDistance(lastX, lastY, buffer[0], buffer[1]);
                        // Fall through
                    }
                    case PathIterator.SEG_MOVETO: {
                        lastX = buffer[0];
                        lastY = buffer[1];
                        break;
                    }
                    default: {
                        throw new IllegalPathStateException();
                    }
                }
                iterator.next();
            }
            assertEquals("Segment count", 1000, count); // Implementation check; will no longer be
                                                        // valid when the path will contains curves.
            assertEquals("Orthodromic path length", orthodromic, length, 1E-4);
        }
    }

    /**
     * Tests geodetic calculator involving a coordinate operation.
     * Our test uses a simple geographic CRS with only the axis order interchanged.
     */
    public void testUsingTransform() throws FactoryException, TransformException {
        final GeographicCRS crs = new DefaultGeographicCRS("Test", DefaultGeodeticDatum.WGS84,
                new DefaultEllipsoidalCS("Test", DefaultCoordinateSystemAxis.LATITUDE,
                                                 DefaultCoordinateSystemAxis.LONGITUDE));
        final GeodeticCalculator calculator = new GeodeticCalculator(crs);
        assertSame(crs, calculator.getCoordinateReferenceSystem());

        final double x = 45;
        final double y = 30;
        calculator.setAnchorPosition(new DirectPosition2D(x,y));
        Point2D point = calculator.getAnchorPoint();
        assertEquals(y, point.getX(), 1E-5);
        assertEquals(x, point.getY(), 1E-5);

        calculator.setDirection(10, 100);
        DirectPosition position = calculator.getDestinationPosition();
        point = calculator.getDestinationPoint();
        assertEquals(point.getX(), position.getOrdinate(1), 1E-5);
        assertEquals(point.getY(), position.getOrdinate(0), 1E-5);
    }
}
