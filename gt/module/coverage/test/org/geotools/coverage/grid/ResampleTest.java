/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.coverage.grid;

// J2SE dependencies
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.crs.DefaultDerivedCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.operation.DefaultOperationMethod;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.coverage.FactoryFinder;


/**
 * Visual test of the "Resample" operation. A remote sensing image is projected from a fitted
 * coordinate system to a geographic one.
 *
 * @source $URL$
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public final class ResampleTest extends GridCoverageTest {
    /**
     * Set to <code>true</code> if the test case should show the projection results
     * in a windows. This flag is set to <code>true</code> if the test is run from
     * the command line through the <code>main(String[])</code> method. Otherwise
     * (for example if it is run from Maven), it is left to <code>false</code>.
     */
    private static boolean SHOW = false;

    /**
     * Small number for comparaisons.
     */
    private static final double EPS = 1E-6;

    /**
     * The source grid coverage.
     */
    private GridCoverage2D coverage;
    
    /**
     * Constructs a test case with the given name.
     */
    public ResampleTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        coverage = getExample(0);
    }

    /**
     * Applies an operation on the specified coverage.
     * This is invoked before to run the tests defined in the super-class.
     */
    protected GridCoverage2D transform(final GridCoverage2D coverage) {
        return (GridCoverage2D) Operations.DEFAULT.resample(coverage, getProjectedCRS(coverage));
    }

    /**
     * Compares two affine transforms.
     */
    public static void assertEquals(final AffineTransform expected, final AffineTransform actual) {
        assertEquals("scaleX",     expected.getScaleX(),     actual.getScaleX(),     EPS);
        assertEquals("scaleY",     expected.getScaleY(),     actual.getScaleY(),     EPS);
        assertEquals("shearX",     expected.getShearX(),     actual.getShearX(),     EPS);
        assertEquals("shearY",     expected.getShearY(),     actual.getShearY(),     EPS);
        assertEquals("translateX", expected.getTranslateX(), actual.getTranslateX(), EPS);
        assertEquals("translateY", expected.getTranslateY(), actual.getTranslateY(), EPS);
    }

    /**
     * Returns the "Sample to geophysics" transform as an affine transform.
     */
    private static AffineTransform getAffineTransform(final GridCoverage2D coverage) {
        AffineTransform tr;
        tr = (AffineTransform)((GridGeometry2D)coverage.getGridGeometry()).getGridToCoordinateSystem2D();
        tr = new AffineTransform(tr); // Change the type to the default Java2D implementation.
        return tr;
    }

    /**
     * Returns a projected CRS for test purpose.
     */
    private static CoordinateReferenceSystem getProjectedCRS(final GridCoverage2D coverage) {
        try {
            final GeographicCRS  base = (GeographicCRS) coverage.getCoordinateReferenceSystem();
            final Ellipsoid ellipsoid = ((GeodeticDatum) base.getDatum()).getEllipsoid();
            final DefaultMathTransformFactory factory = new DefaultMathTransformFactory();
            final ParameterValueGroup parameters = factory.getDefaultParameters("Oblique_Stereographic");
            parameters.parameter("semi_major").setValue(ellipsoid.getSemiMajorAxis());
            parameters.parameter("semi_minor").setValue(ellipsoid.getSemiMinorAxis());
            parameters.parameter("central_meridian").setValue(5);
            parameters.parameter("latitude_of_origin").setValue(-5);
            final MathTransform mt;
            try {
                mt = factory.createParameterizedTransform(parameters);
            } catch (FactoryException exception) {
                fail(exception.getLocalizedMessage());
                return null;
            }
            return new DefaultProjectedCRS("Stereographic", new DefaultOperationMethod(mt),
                                           base, mt, DefaultCartesianCS.PROJECTED);
        } catch (NoSuchIdentifierException exception) {
            fail(exception.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Projects the specified image to the specified CRS.
     * The result will be displayed in a window if {@link #SHOW} is set to {@code true}.
     *
     * @return The operation name which was applied on the image, or <code>null</code> if none.
     */
	private void projectTo(final CoordinateReferenceSystem crs,
                             final GridGeometry2D       geometry)
    {
        final AbstractProcessor processor = AbstractProcessor.getInstance();
        final String arg1; final Object value1;
        final String arg2; final Object value2;
        if (crs != null) {
            arg1="CoordinateReferenceSystem"; value1=crs;
            if (geometry != null) {
                arg2="GridGeometry"; value2=geometry;
            } else {
                arg2="InterpolationType"; value2="bilinear";
            }
        } else {
            arg1="GridGeometry";      value1=geometry;
            arg2="InterpolationType"; value2="bilinear";
        }
        GridCoverage2D projected = coverage.geophysics(true);
        final ParameterValueGroup param = processor.getOperation("Resample").getParameters();
        param.parameter("Source").setValue(projected);
        param.parameter(arg1).setValue(value1);
        param.parameter(arg2).setValue(value2);
        projected = (GridCoverage2D) processor.doOperation(param);
        assertNotNull(projected.getRenderedImage().getData());
        final RenderedImage image = projected.getRenderedImage();
        projected = projected.geophysics(false);
        String operation = null;
        if (image instanceof RenderedOp) {
            operation = ((RenderedOp) image).getOperationName();
            AbstractProcessor.LOGGER.fine("Applied \""+operation+"\" JAI operation.");
        }
        if (SHOW) {
            Viewer.show(projected, operation);
        } else {
            // Force computation
            assertNotNull(projected.getRenderedImage().getData());
        }
    }

    /**
     * Tests the "Resample" operation with an identity transform.
     */
    public void testIdentity() {
		projectTo(coverage.getCoordinateReferenceSystem(), null);
    }

    /**
     * Tests the "Resample" operation with a "Crop" transform.
     */
    public void testCrop() {
		projectTo(null, new GridGeometry2D(new GeneralGridRange(new Rectangle(
				50, 50, 200, 200)), (MathTransform) null, null));
    }

    /**
     * Tests the "Resample" operation with a stereographic coordinate system.
     */
    public void testStereographic() {
		projectTo(getProjectedCRS(coverage), null);
    }

    /**
     * Tests the "Resample" operation with an "Affine" transform.
     */
    public void testAffine() {
        AffineTransform atr = getAffineTransform(coverage);
        atr.preConcatenate(AffineTransform.getTranslateInstance(5, 5));
        MathTransform tr = ProjectiveTransform.create(atr);
        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem();
        crs = new DefaultDerivedCRS("F2", new DefaultOperationMethod(tr), crs, tr, crs.getCoordinateSystem());
        /*
         * Note: In current Resampler implementation, the affine transform effect tested
         *       on the first line below will not be visible with the simple viewer used
         *       here.  It would be visible however with more elaborated viewer like the
         *       one provided in the <code>org.geotools.renderer</code> package.
         */
		projectTo(crs, null);
		projectTo(null, new GridGeometry2D(null, tr, null));
    }
    
    /**
     * Tests <var>X</var>,<var>Y</var> translation in the {@link GridGeometry} after
     * a "Resample" operation.
     */
    public void testTranslation() throws NoninvertibleTransformException {
        GridCoverage2D grid = coverage;
		final int transX = -0;
		final int transY = -0;
        final double scaleX =  0.04;
        final double scaleY = -0.04;
        final ParameterBlock block = new ParameterBlock().addSource(grid.getRenderedImage())
                                                         .add((float)transX).add((float)transY);
        RenderedImage img = JAI.create("Translate", block);
        assertEquals("Incorrect X translation", transX, img.getMinX());
        assertEquals("Incorrect Y translation", transY, img.getMinY());
        /*
         * Create a grid coverage from the translated image but with the same envelope.
         * Consequently, the 'gridToCoordinateSystem' should be translated by the same
         * amount, with the opposite sign.
         */
        AffineTransform expected = getAffineTransform(grid);
        grid = (GridCoverage2D) FactoryFinder.getGridCoverageFactory(null).create("Translated",
                                  img, grid.getEnvelope(), grid.getSampleDimensions(),
                                  new GridCoverage2D[]{grid}, grid.getProperties());
        expected.translate(-transX, -transY);
        assertEquals(expected, getAffineTransform(grid));
        /*
         * Apply the "Resample" operation with a specific 'gridToCoordinateSystem' transform.
         * The envelope is left unchanged. The "Resample" operation should compute automatically
         * new image bounds.
         */
        final AffineTransform at = AffineTransform.getScaleInstance(scaleX, scaleY);
        final MathTransform   tr = ProjectiveTransform.create(at);
        final GridGeometry2D geometry = new GridGeometry2D(null, tr, null);
        grid = (GridCoverage2D) Operations.DEFAULT.resample(grid,
                grid.getCoordinateReferenceSystem(), geometry, null);
        assertEquals(at, getAffineTransform(grid));
        img = grid.getRenderedImage();
        expected.preConcatenate(at.createInverse());
        Point point = new Point(transX, transY);
        expected.transform(point, point); // Round toward neareast integer
        assertEquals("Incorrect X translation", point.x, img.getMinX());
        assertEquals("Incorrect Y translation", point.y, img.getMinY());
    }

    /**
     * Inherited test disabled for this suite.
     *
     * @todo Investigate why this test fails.
     */
    public void testSerialization() {
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ResampleTest.class);
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        SHOW = true;
        org.geotools.util.MonolineFormatter.initGeotools(AbstractProcessor.OPERATION);
        junit.textui.TestRunner.run(suite());
    }
}
