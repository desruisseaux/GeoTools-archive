/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.coverage.grid;

// J2SE dependencies
import java.awt.geom.AffineTransform;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.matrix.MatrixFactory;


/**
 * Test the {@link GridGeometry} implementation.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridGeometryTest extends TestCase {
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
        return new TestSuite(GridGeometryTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public GridGeometryTest(final String name) {
        super(name);
    }

    /**
     * Tests the construction with an identity transform.
     */
    public void testIdentity() throws FactoryException {
        final MathTransformFactory factory = FactoryFinder.getMathTransformFactory(null);
        final int[] lower = new int[] {0,     0, 2};
        final int[] upper = new int[] {100, 200, 4};
        final MathTransform identity = factory.createAffineTransform(MatrixFactory.create(4));
        GridGeometry2D gg;
        try {
            gg = new GridGeometry2D(new GeneralGridRange(lower,upper), identity, null);
            fail();
        } catch (IllegalArgumentException e) {
            // This is the expected dimension.
        }
        upper[2] = 3;
        gg = new GridGeometry2D(new GeneralGridRange(lower,upper), identity, null);
        assertTrue(identity.isIdentity());
        assertTrue(gg.getGridToCoordinateSystem().isIdentity());
        assertTrue(gg.getGridToCoordinateSystem2D().isIdentity());
        assertEquals(3, gg.getGridToCoordinateSystem().getSourceDimensions());
        assertEquals(2, gg.getGridToCoordinateSystem2D().getSourceDimensions());
        assertTrue(gg.getGridToCoordinateSystem2D() instanceof AffineTransform);
    }

    /**
     * Tests the construction from an envelope.
     */
    public void testEnvelope() {
        final int[]    lower   = new int[]    {   0,   0,  4};
        final int[]    upper   = new int[]    {  90,  45,  5};
        final double[] minimum = new double[] {-180, -90,  9};
        final double[] maximum = new double[] {+180, +90, 10};
        final GridGeometry2D gg;
        gg = new GridGeometry2D(new GeneralGridRange(lower,upper),
                                new GeneralEnvelope(minimum, maximum), null, false);
        final AffineTransform tr = (AffineTransform) gg.getGridToCoordinateSystem2D();
        assertEquals(AffineTransform.TYPE_UNIFORM_SCALE |
                     AffineTransform.TYPE_TRANSLATION, tr.getType());

        assertEquals(4, tr.getScaleX(), 0);
        assertEquals(4, tr.getScaleY(), 0);
        assertEquals(-178, tr.getTranslateX(), 0);
        assertEquals( -88, tr.getTranslateY(), 0);
    }
}
