/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.geometry.jts;

// J2SE dependencies
import java.awt.geom.AffineTransform;

// JTS dependencies
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.matrix.GeneralMatrix;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link JTS} implementation.
 *
 * @since 2.2
 * @version $Id$
 * @author Jess Eichar
 * @author Martin Desruisseaux
 */
public class JTSTest extends TestCase {
    /**
     * The tolerance factor.
     */
    private static final double EPS = 0.000001;

    /**
     * A CRS for testing purpose.
     */
    static final String UTM_ZONE_10N =
                "PROJCS[\"NAD_1983_UTM_Zone_10N\",\n"                    +
                "  GEOGCS[\"GCS_North_American_1983\",\n"                +
                "    DATUM[\"D_North_American_1983\",\n"                 +
                "      TOWGS84[0,0,0,0,0,0,0],\n"                        +
                "      SPHEROID[\"GRS_1980\",6378137,298.257222101]],\n" +
                "    PRIMEM[\"Greenwich\",0],\n"                         +
                "    UNIT[\"Degree\",0.017453292519943295]],\n"          +
                "  PROJECTION[\"Transverse_Mercator\"],\n"               +
                "    PARAMETER[\"False_Easting\",500000],\n"             +
                "    PARAMETER[\"False_Northing\",0],\n"                 +
                "    PARAMETER[\"Central_Meridian\",-123],\n"            +
                "    PARAMETER[\"Scale_Factor\",0.9996],\n"              +
                "    PARAMETER[\"Latitude_Of_Origin\",0],\n"             +
                "  UNIT[\"Meter\",1]]";

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
        return new TestSuite(JTSTest.class);
    }

    /**
     * Tests the transformation of a single coordinate.
     */
    public void testTransformCoordinate() throws FactoryException, TransformException {
        Coordinate   coord = new Coordinate(10, 10);
        AffineTransform at = AffineTransform.getScaleInstance(0.5, 1);
        MathTransform2D  t = (MathTransform2D) FactoryFinder.getMathTransformFactory(null)
                                            .createAffineTransform(new GeneralMatrix(at));
        coord = JTS.transform(coord, coord, t);
        assertEquals(new Coordinate(5, 10), coord);
        coord = JTS.transform(coord, coord, t.inverse());
        assertEquals(new Coordinate(10, 10), coord);
        
        CoordinateReferenceSystem crs = FactoryFinder.getCRSFactory(null).createFromWKT(UTM_ZONE_10N);
        t = (MathTransform2D) FactoryFinder.getCoordinateOperationFactory(null).createOperation(
                                            DefaultGeographicCRS.WGS84, crs).getMathTransform();
        coord = new Coordinate(-123, 55);
        coord = JTS.transform(coord, coord, t);
        coord = JTS.transform(coord, coord, t.inverse());
        assertEquals(-123, coord.x, EPS);
        assertEquals(  55, coord.y, EPS);
    }

    /*
     * Tests the transformation of an envelope.
     */
    public void testTransformEnvelopeMathTransform() throws FactoryException, TransformException {
        Envelope envelope  = new Envelope(0, 10, 0, 10);
        AffineTransform at = AffineTransform.getScaleInstance(0.5, 1);
        MathTransform2D t  = (MathTransform2D) FactoryFinder.getMathTransformFactory(null)
                                            .createAffineTransform(new GeneralMatrix(at));
        envelope = JTS.transform(envelope, t);
        assertEquals(new Envelope(0, 5, 0, 10), envelope);
        envelope = JTS.transform(envelope, t.inverse());
        assertEquals(new Envelope(0, 10, 0, 10), envelope);
        
        envelope = JTS.transform(envelope, null, t, 10);
        assertEquals(new Envelope(0, 5, 0, 10), envelope);
        envelope = JTS.transform(envelope, null, t.inverse(), 10);
        assertEquals(new Envelope(0, 10, 0, 10), envelope);
        
        CoordinateReferenceSystem crs = FactoryFinder.getCRSFactory(null).createFromWKT(UTM_ZONE_10N);
        t = (MathTransform2D) FactoryFinder.getCoordinateOperationFactory(null).createOperation(
                                            DefaultGeographicCRS.WGS84, crs).getMathTransform();
        envelope = new Envelope(-123, -133, 55, 60);
        envelope = JTS.transform(envelope, t);
        envelope = JTS.transform(envelope, t.inverse());
        /*
         * Use a large tolerance factory for comparaisons because an accurate transformed envelope
         * is bigger than the envelope that we get if we transformed only the 4 corners, and the
         * inverse envelope way expand yet again the envelope for exactly the same reason.
         */
        assertEquals(-133, envelope.getMinX(), 1.5);
        assertEquals(-123, envelope.getMaxX(), EPS);
        assertEquals(  55, envelope.getMinY(), 0.5);
        assertEquals(  60, envelope.getMaxY(), 0.5);
    }
}
