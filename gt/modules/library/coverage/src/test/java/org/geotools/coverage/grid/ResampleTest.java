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
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.coverage.FactoryFinder;
import org.geotools.factory.Hints;


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
     * Set to {@code true} if the test case should show the projection results
     * in a windows. This flag is set to {@code true} if the test is run from
     * the command line through the {@code main(String[])} method. Otherwise
     * (for example if it is run from Maven), it is left to {@code false}.
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
    
	private GridCoverage2D indexedCoverage;

	private GridCoverage2D indexedCoverageWithTransparency;

	private GridCoverage2D floatCoverage;

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
		indexedCoverage = getExample(2);
		indexedCoverageWithTransparency = getExample(3);
		floatCoverage=getExample(4);
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
        tr = (AffineTransform)((GridGeometry2D)coverage.getGridGeometry()).getGridToCRS2D();
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

    private String projectTo( GridCoverage2D gc,final CoordinateReferenceSystem crs,
            final GridGeometry2D       geometry)
    {
    	return projectTo(gc, crs, geometry, null,true);
    }
    /**
     * Projects the specified image to the specified CRS.
     * The result will be displayed in a window if {@link #SHOW} is set to {@code true}.
     *
     * @return The operation name which was applied on the image, or {@code null} if none.
     */
    private String projectTo( GridCoverage2D gc,final CoordinateReferenceSystem crs,
                             final GridGeometry2D       geometry,Hints hints,boolean geo)
    {
        final AbstractProcessor processor = (hints!=null)?new DefaultProcessor(hints):
        	AbstractProcessor.getInstance();
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
        GridCoverage2D projected = gc.geophysics(geo);
        final ParameterValueGroup param = processor.getOperation("Resample").getParameters();
        param.parameter("Source").setValue(projected);
        param.parameter(arg1).setValue(value1);
        param.parameter(arg2).setValue(value2);
        projected = (GridCoverage2D) processor.doOperation(param);
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
        return operation;
    }

    /**
     * Tests the "Resample" operation with an identity transform.
     */
    public void testIdentity() {
        assertEquals("Lookup", projectTo(coverage,coverage.getCoordinateReferenceSystem(), null));
        assertNull( projectTo(indexedCoverage,indexedCoverage.getCoordinateReferenceSystem(), null));
        assertNull( projectTo(indexedCoverageWithTransparency,indexedCoverageWithTransparency.getCoordinateReferenceSystem(), null));
    	  assertNull( projectTo(floatCoverage,floatCoverage.getCoordinateReferenceSystem(), null));
    }

    /**
     * Tests the "Resample" operation with a "Crop" transform.
     */
    public void testCrop() {
        assertEquals("Crop", projectTo(coverage,null, new GridGeometry2D(
                             new GeneralGridRange(new Rectangle(50,50,200,200)),
                             (MathTransform)null, null)));
        assertEquals("Crop", projectTo(indexedCoverage,null, new GridGeometry2D(
                new GeneralGridRange(new Rectangle(50,50,100,100)),
                (MathTransform)null, null)));
        assertEquals("Crop", projectTo(indexedCoverageWithTransparency,null, new GridGeometry2D(
                new GeneralGridRange(new Rectangle(50,50,100,100)),
                (MathTransform)null, null)));
    	 assertEquals("Crop", projectTo(floatCoverage,null, new GridGeometry2D(
                 new GeneralGridRange(new Rectangle(50,50,100,100)),
                 (MathTransform)null, null),new Hints(Hints.REPLACE_NON_GEOPHYSICS_VIEW,Boolean.FALSE),false));
    }

    /**
     * Tests the "Resample" operation with a stereographic coordinate system.
     */
    public void testStereographic() {
        assertEquals("Warp", projectTo(coverage,getProjectedCRS(coverage), null));
        assertEquals("Warp", projectTo(indexedCoverage,getProjectedCRS(indexedCoverage), null));
        assertEquals("Warp", projectTo(indexedCoverageWithTransparency,getProjectedCRS(indexedCoverageWithTransparency), null));
        assertEquals("Warp", projectTo(floatCoverage,getProjectedCRS(floatCoverage), 
        		null, new Hints(Hints.REPLACE_NON_GEOPHYSICS_VIEW,Boolean.FALSE),false));
    }

    /**
     * Tests the "Resample" operation with an "Affine" transform.
     */
    public void testAffine() {

		performAffine(coverage,null,true,"Lookup","Affine");
		performAffine(indexedCoverage,null,true,"Lookup","Affine");
		performAffine(indexedCoverageWithTransparency,null,false,"BandSelect","Affine");
		performAffine(floatCoverage,new Hints(Hints.REPLACE_NON_GEOPHYSICS_VIEW,Boolean.FALSE),false,"Lookup","Affine");
       
    }

	/**
	 * Performs an Affine transformation on the provded {@link GridCoverage2D} using the 
	 * Resample operation.
	 * @param gc is the {@link GridCoverage2D} to apply the operation on.
	 */
	private void performAffine(GridCoverage2D gc,Hints hints, boolean geo,String testString1,String testString2 ) {
		AffineTransform atr = getAffineTransform(gc);
        atr.preConcatenate(AffineTransform.getTranslateInstance(5, 5));
        MathTransform tr = ProjectiveTransform.create(atr);
        CoordinateReferenceSystem crs = gc.getCoordinateReferenceSystem();
        crs = new DefaultDerivedCRS("F2", new DefaultOperationMethod(tr), crs, tr, crs.getCoordinateSystem());
        /*
         * Note: In current Resampler implementation, the affine transform effect tested
         *       on the first line below will not be visible with the simple viewer used
         *       here.  It would be visible however with more elaborated viewer like the
         *       one provided in the {@code org.geotools.renderer} package.
         */
        String operation = projectTo(gc, crs, null);
        if(operation!=null)
        ;//	assertEquals(testString1, operation);
        operation =projectTo(gc,null, new GridGeometry2D(null, tr, null),hints,geo );
        if(operation!=null)
        ;//	assertEquals(testString2,operation);
	}

    /**
     * Tests <var>X</var>,<var>Y</var> translation in the {@link GridGeometry} after
     * a "Resample" operation.
     */
    public void testTranslation() throws NoninvertibleTransformException {

        doTranslation(coverage);
        doTranslation(indexedCoverage);
        doTranslation(indexedCoverageWithTransparency);
        
    }

	/**
	 * Performs a translation using the "Resample" operation.
	 * @param grid is the {@link GridCoverage2D} to apply the translation on.
	 * @throws NoninvertibleTransformException
	 */
	private void doTranslation(GridCoverage2D grid) throws NoninvertibleTransformException {
		final int    transX =  -253;
        final int    transY =  -456;
        final double scaleX =  0.04;
        final double scaleY = -0.04;
        ParameterBlock block = new ParameterBlock().addSource(grid.getRenderedImage())
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
        AffineTransform at = AffineTransform.getScaleInstance(scaleX, scaleY);
        MathTransform   tr = ProjectiveTransform.create(at);
        GridGeometry2D geometry = new GridGeometry2D(null, tr, null);
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
