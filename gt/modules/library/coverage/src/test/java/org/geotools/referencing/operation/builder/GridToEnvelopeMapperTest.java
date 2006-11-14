/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
 *    (C) 2006, Geomatys
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
package org.geotools.referencing.operation.builder;

// J2SE dependencies
import java.util.Arrays;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.geometry.GeneralEnvelope;


/**
 * Tests {@link GridToEnvelopeMapper}.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridToEnvelopeMapperTest extends TestCase {
    /**
     * Tolerance factor for the comparaison of floating point numbers.
     */
    private static final double EPS = 1E-10;

    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(GridToEnvelopeMapperTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public GridToEnvelopeMapperTest(final String name) {
        super(name);
    }

    /**
     * Various tests.
     */
    public void testMapper() {
        ///////////////////////////////////////////////////////////////
        ///  Tests the initial state.
        ///
        final GridToEnvelopeMapper mapper = new GridToEnvelopeMapper();
        assertTrue (mapper.isAutomatic(GridToEnvelopeMapper.SWAP_XY));
        assertTrue (mapper.isAutomatic(GridToEnvelopeMapper.REVERSE_AXIS));
        assertFalse(mapper.getSwapXY());
        assertNull (mapper.getReverseAxis());
        try {
            mapper.getGridRange();
            fail();
        } catch (IllegalStateException e) {
            // This is the expected exception.
        }
        try {
            mapper.getEnvelope();
            fail();
        } catch (IllegalStateException e) {
            // This is the expected exception.
        }
        try {
            mapper.createTransform();
            fail();
        } catch (IllegalStateException e) {
            // This is the expected exception.
        }


        ///////////////////////////////////////////////////////////////
        ///  Tests the setting of grid range and envelope.
        ///
        Point2D.Double   point = new Point2D.Double();
        GeneralGridRange gridRange;
        GeneralEnvelope  envelope;
        gridRange = new GeneralGridRange(new int[] {10, 20}, new int[] {110, 220});
        envelope  = new GeneralEnvelope(new double[] {1, 4, 6}, new double[] {11, 44, 66});
        mapper.setGridRange(gridRange);
        assertSame(gridRange, mapper.getGridRange());
        try {
            mapper.getEnvelope();
            fail();
        } catch (IllegalStateException e) {
            // This is the expected exception.
        }
        try {
            mapper.setEnvelope(envelope);
            fail();
        } catch (MismatchedDimensionException e) {
            // This is the expected exception.
        }
        try {
            new GridToEnvelopeMapper(gridRange, envelope);
            fail();
        } catch (MismatchedDimensionException e) {
            // This is the expected exception.
        }
        envelope = envelope.getSubEnvelope(0, 2);
        mapper.setEnvelope(envelope);
        assertSame(envelope, mapper.getEnvelope());


        ///////////////////////////////////////////////////////////////
        ///  Tests the creation when no CRS is available.
        ///
        assertFalse(mapper.getSwapXY());
        assertNull (mapper.getReverseAxis());
        final AffineTransform tr1 = mapper.createAffineTransform();
        assertEquals(AffineTransform.TYPE_GENERAL_SCALE |
                     AffineTransform.TYPE_TRANSLATION, tr1.getType());
        assertEquals(0.1,  tr1.getScaleX(),     EPS);
        assertEquals(0.2,  tr1.getScaleY(),     EPS);
        assertEquals(0.05, tr1.getTranslateX(), EPS);
        assertEquals(0.10, tr1.getTranslateY(), EPS);
        assertSame("Transform should be cached", tr1, mapper.createAffineTransform());

        // Tests a coordinate transformation.
        point.x = 10 - 0.5;
        point.y = 20 - 0.5;
        assertSame(point, tr1.transform(point, point));
        assertEquals(1, point.x, EPS);
        assertEquals(4, point.y, EPS);


        ///////////////////////////////////////////////////////////////
        ///  Tests the creation when a CRS is available.
        ///
        envelope = (GeneralEnvelope) envelope.clone();
        envelope.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
        mapper.setEnvelope(envelope);
        assertFalse(mapper.getSwapXY());
        assertTrue (Arrays.equals(new boolean[] {false, true}, mapper.getReverseAxis()));
        final AffineTransform tr2 = mapper.createAffineTransform();
        assertNotSame("Should be a new transform", tr1, tr2);
        assertEquals(AffineTransform.TYPE_GENERAL_SCALE |
                     AffineTransform.TYPE_TRANSLATION   |
                     AffineTransform.TYPE_FLIP, tr2.getType());
        assertEquals( 0.1, tr2.getScaleX(), EPS);
        assertEquals(-0.2, tr2.getScaleY(), EPS);
        assertSame("Transform should be cached", tr2, mapper.createAffineTransform());

        // Tests a coordinate transformation.
        point.x = 10 - 0.5;
        point.y = 20 - 0.5;
        assertSame(point, tr2.transform(point, point));
        assertEquals( 1, point.x, EPS);
        assertEquals(44, point.y, EPS);


        ///////////////////////////////////////////////////////////////
        ///  Tests the creation with a (latitude, longitude) CRS.
        ///
        envelope = (GeneralEnvelope) envelope.clone();
        envelope.setCoordinateReferenceSystem(new DefaultGeographicCRS("WGS84",
                DefaultGeodeticDatum.WGS84, new DefaultEllipsoidalCS("WGS84",
                DefaultCoordinateSystemAxis.LATITUDE,
                DefaultCoordinateSystemAxis.LONGITUDE)));
        mapper.setEnvelope(envelope);
        assertTrue (mapper.getSwapXY());
        assertTrue (Arrays.equals(new boolean[] {true, false}, mapper.getReverseAxis()));
        final AffineTransform tr3 = mapper.createAffineTransform();
        assertNotSame("Should be a new transform", tr2, tr3);
        assertEquals(AffineTransform.TYPE_QUADRANT_ROTATION |
                     AffineTransform.TYPE_GENERAL_SCALE     |
                     AffineTransform.TYPE_TRANSLATION, tr3.getType());
        assertEquals( 0.0,  tr3.getScaleX(), EPS);
        assertEquals( 0.0,  tr3.getScaleY(), EPS);
        assertEquals(-0.05, tr3.getShearX(), EPS);
        assertEquals( 0.4,  tr3.getShearY(), EPS);
        assertSame("Transform should be cached", tr3, mapper.createAffineTransform());

        // Tests a coordinate transformation.
        point.x = 10 - 0.5;
        point.y = 20 - 0.5;
        assertSame(point, tr3.transform(point, point));
        assertEquals( 4, point.y, EPS);
        assertEquals(11, point.x, EPS);


        ///////////////////////////////////////////////////////////////
        ///  Tests explicit axis reversal and swapping
        ///
        assertTrue (mapper.isAutomatic(GridToEnvelopeMapper.SWAP_XY));
        assertTrue (mapper.isAutomatic(GridToEnvelopeMapper.REVERSE_AXIS));
        assertTrue (mapper.getSwapXY());
        mapper.setSwapXY(false);
        assertFalse(mapper.isAutomatic(GridToEnvelopeMapper.SWAP_XY));
        assertTrue (mapper.isAutomatic(GridToEnvelopeMapper.REVERSE_AXIS));
        assertFalse(mapper.getSwapXY());
        assertNotSame(tr3, mapper.createAffineTransform());
        mapper.setReverseAxis(null);
        assertFalse(mapper.isAutomatic(GridToEnvelopeMapper.SWAP_XY));
        assertFalse(mapper.isAutomatic(GridToEnvelopeMapper.REVERSE_AXIS));
        assertEquals(tr1, mapper.createAffineTransform());
    }
}
