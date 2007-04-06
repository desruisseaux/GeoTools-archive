/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.projection;

// JUnit dependencies
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.geometry.DirectPosition2D;
import org.geotools.parameter.ParameterWriter;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.resources.Arguments;


/**
 * Tests projection equations as well as the integration with {@link MathTransformFactory}.
 * Projections are tested through creation and testing of {@link MathTransform} objects;
 * no {@link org.opengis.referencing.operation.Projection} are created here.
 * <p>
 * The spherical tests here tests real spheres (tests in {@code "Simple"} test scripts are
 * not exactly spherical).
 *
 * @source $URL$
 * @version $Id$
 * @author Rueben Schulz
 */
public final class DirectCreationTest extends TestCase {
    /**
     * Set to {@code true} for printing some informations to standard output while
     * performing tests. Consider this field as constants after the application launch.
     */
    private static boolean VERBOSE = false;
    
    /** Tolerance for test when units are degrees. */
    private final static double[] TOL_DEG = {1E-6, 1E-6};
    
    /** Tolerance for test when units are metres. */
    private final static double[] TOL_M = {1E-2, 1E-2};
    
    /** factory to use to create projection transforms*/
    private MathTransformFactory mtFactory;

    /**
     * Constructs a test with the given name.
     */
    public DirectCreationTest(final String name) {
        super(name);
    }
    
    /**
     * Uses reflection to dynamically create a test suite containing all 
     * the {@code testXXX()} methods - from the JUnit FAQ.
     */
    public static Test suite() {
        return new TestSuite(DirectCreationTest.class);
    }
    
    /**
     * Runs the tests with the textual test runner.
     */
    public static void main(final String args[]) {
        final Arguments arguments = new Arguments(args);
        VERBOSE = arguments.getFlag("-verbose");
        arguments.getRemainingArguments(0);
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Set up common objects used by all tests.
     */
    protected void setUp() {
        mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
    }
    
    /**
     * Release common objects used by all tests.
     */
    protected void tearDown() {
        mtFactory = null;
    }
    
    /**
     * Check if two coordinate points are equals, in the limits of the specified
     * tolerance vector.
     *
     * @param expected  The expected coordinate point.
     * @param actual    The actual coordinate point.
     * @param tolerance The tolerance vector. If this vector length is smaller than the number
     *                  of dimension of <code>actual</code>, then the last tolerance value will
     *                  be reused for all extra dimensions.
     * @throws AssertionFailedError If the actual point is not equals to the expected point.
     */
    private static void assertEquals(final DirectPosition expected,
                                     final DirectPosition actual,
                                     final double[]       tolerance)
            throws AssertionFailedError
    {
        final int dimension = actual.getDimension();
        final int lastToleranceIndex = tolerance.length-1;
        assertEquals("The coordinate point doesn't have the expected dimension",
                     expected.getDimension(), dimension);
        for (int i=0; i<dimension; i++) {
            assertEquals("Mismatch for ordinate "+i+" (zero-based):",
                         expected.getOrdinate(i), actual.getOrdinate(i),
                         tolerance[Math.min(i, lastToleranceIndex)]);
        }
    }
    
    /**
     * Helper method to test transform from a source to a target point.
     * Coordinate points are (x,y) or (long, lat)
     */
    private static void doTransform(DirectPosition source,
                                    DirectPosition target,
                                    MathTransform transform)
            throws TransformException 
    {
        DirectPosition calculated;
        calculated = transform.transform(source, null);
        assertEquals(target, calculated, TOL_M);
        
        //the inverse
        target = source;
        source = calculated;
        calculated = transform.inverse().transform(source, null);
        assertEquals(target, calculated, TOL_DEG);
    }
    
    /**
     * Print parameters for the specified projection.
     * Used to see the if parameters for a transform are correct.
     */
    private void printParameters(final String proj) throws NoSuchIdentifierException {
        final ParameterValueGroup values = mtFactory.getDefaultParameters(proj);
        ParameterWriter.print((ParameterDescriptorGroup) values.getDescriptor());
    }

    /**
     * Some tests for the Equidistant Cylindrical projection.
     */
    public void testEquidistantCylindrical() throws FactoryException, TransformException {

        ///////////////////////////////////////
        // Equidistant_Cylindrical tests     //
        ///////////////////////////////////////
        if (VERBOSE) {
            printParameters("Equidistant_Cylindrical");
        }
        MathTransform transform;
        ParameterValueGroup params;

        // approx bristol UK
        params = mtFactory.getDefaultParameters("Equidistant_Cylindrical");
        params.parameter("semi_major")         .setValue(6378137);
        params.parameter("semi_minor")         .setValue(6378137);
        params.parameter("central_meridian")   .setValue(  0.000);
        params.parameter("standard_parallel_1").setValue(  0.000);
        params.parameter("false_easting")      .setValue(  0.0  );
        params.parameter("false_northing")     .setValue(  0.0  );
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }
        doTransform(new DirectPosition2D(-2.5, 51.37),
                    new DirectPosition2D(-278298.73, 5718482.24), transform);
    }

    /**
     * Some tests for the Mercator Projection.
     */
    public void testMercator() throws FactoryException, TransformException {
        
        ///////////////////////////////////////
        // Mercator_1SP tests                //
        ///////////////////////////////////////
        if (VERBOSE) {
            printParameters("Mercator_1SP");
        }
        MathTransform transform;
        ParameterValueGroup params;

        //epsg example (p. 26)
        params = mtFactory.getDefaultParameters("Mercator_1SP");
        params.parameter("semi_major")      .setValue(6377397.155);
        params.parameter("semi_minor")      .setValue(6356078.963);
        params.parameter("central_meridian").setValue(    110.000);
        params.parameter("scale_factor")    .setValue(      0.997);
        params.parameter("false_easting")   .setValue(3900000.0  );
        params.parameter("false_northing")  .setValue( 900000.0  );
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }
        doTransform(new DirectPosition2D(120.0, -3.0),
                    new DirectPosition2D(5009726.58, 569150.82), transform);
        
        //spherical test (Snyder p. 266)
        params.parameter("semi_major")      .setValue(   1.0);
        params.parameter("semi_minor")      .setValue(   1.0);
        params.parameter("central_meridian").setValue(-180.0);
        params.parameter("scale_factor")    .setValue(   1.0);
        params.parameter("false_easting")   .setValue(   0.0);
        params.parameter("false_northing")  .setValue(   0.0);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }
        doTransform(new DirectPosition2D(-75.0, 35.0),
                    new DirectPosition2D(1.8325957, 0.6528366), transform);
        
        //spherical test 2 (original units for target were feet)
        params.parameter("semi_major")      .setValue(6370997.0);
        params.parameter("semi_minor")      .setValue(6370997.0);
        params.parameter("central_meridian").setValue(      0.0);
        params.parameter("scale_factor")    .setValue(      1.0);
        params.parameter("false_easting")   .setValue(      0.0);
        params.parameter("false_northing")  .setValue(      0.0);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new DirectPosition2D(-123.1, 49.2166666666),
                    new DirectPosition2D(-13688089.02443480, 6304639.84599441), transform);
        
        
        ///////////////////////////////////////
        // Mercator_2SP tests                //
        ///////////////////////////////////////
        if (VERBOSE) {
            printParameters("Mercator_2SP");
        }
        //epsg p25. Note FE and FN are wrong in guide 7. Correct in epsg database v6.3.
        params = mtFactory.getDefaultParameters("Mercator_2SP");
        params.parameter("semi_major")         .setValue(6378245.000);
        params.parameter("semi_minor")         .setValue(6356863.019);
        params.parameter("central_meridian")   .setValue(     51.0);
        params.parameter("standard_parallel_1").setValue(     42.0);
        params.parameter("false_easting")      .setValue(      0.0);
        params.parameter("false_northing")     .setValue(      0.0);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new DirectPosition2D(53.0, 53.0),
                    new DirectPosition2D(165704.29, 5171848.07), transform);
        
        //a spherical case (me)
        params = mtFactory.getDefaultParameters("Mercator_2SP");
        params.parameter("semi_major")         .setValue( 6370997.0);
        params.parameter("semi_minor")         .setValue( 6370997.0);
        params.parameter("central_meridian")   .setValue(     180.0);
        params.parameter("standard_parallel_1").setValue(      60.0);
        params.parameter("false_easting")      .setValue( -500000.0);
        params.parameter("false_northing")     .setValue(-1000000.0);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new DirectPosition2D(-123.1, 49.2166666666),
                    new DirectPosition2D(2663494.1734, 2152319.9230), transform);
        
    }

    /**
     * Some tests for the Oblique Mercator Projection.
     */
    public void testObliqueMercator() throws FactoryException, TransformException {
        if (VERBOSE) {
            printParameters("Oblique Mercator");
        }
        MathTransform transform;
        ParameterValueGroup params;

        params = mtFactory.getDefaultParameters("Oblique Mercator");
        setObliqueMercatorParameter(params);
        transform = mtFactory.createParameterizedTransform(params);
        assertEquals(transform.getClass(), ObliqueMercator.class);
        assertEquals(transform, new ObliqueMercator(params));
        ParameterDescriptorGroup descriptor = ((MapProjection) transform).getParameterDescriptors();
        assertTrue (AbstractIdentifiedObject.nameMatches(descriptor, "Oblique Mercator"));
        assertFalse(AbstractIdentifiedObject.nameMatches(descriptor, "Hotine Oblique Mercator"));
        final MathTransform standard = transform;

        params = mtFactory.getDefaultParameters("Hotine Oblique Mercator");
        setObliqueMercatorParameter(params);
        transform = mtFactory.createParameterizedTransform(params);
        assertEquals(transform.getClass(), HotineObliqueMercator.class);
        assertEquals(transform, new HotineObliqueMercator(params));
        descriptor = ((MapProjection) transform).getParameterDescriptors();
        assertFalse(AbstractIdentifiedObject.nameMatches(descriptor, "Oblique Mercator"));
        assertTrue (AbstractIdentifiedObject.nameMatches(descriptor, "Hotine Oblique Mercator"));
        assertFalse(transform.equals(standard));
    }

    /**
     * For {@link #testObliqueMercator} internal use only.
     */
    private static void setObliqueMercatorParameter(ParameterValueGroup params) {
        params.parameter("semi_major")          .setValue(6377397.155);
        params.parameter("semi_minor")          .setValue(6356078.963);
        params.parameter("longitude_of_center") .setValue(7.439583333333333);
        params.parameter("latitude_of_center")  .setValue(46.952405555555565);
        params.parameter("azimuth")             .setValue(90.0);
        params.parameter("scale_factor")        .setValue(1.0);
        params.parameter("false_easting")       .setValue(600000.0);
        params.parameter("false_northing")      .setValue(200000.0);
        params.parameter("rectified_grid_angle").setValue(90.0);
    }
    
    /**
     * Some tests for the Lambert Conic Conformal Projection.
     */
    public void testLambert() throws FactoryException, TransformException {

        ///////////////////////////////////////
        // Lambert_Conformal_Conic_1SP tests //
        ///////////////////////////////////////
        if (VERBOSE) {
            printParameters("Lambert_Conformal_Conic_1SP");
        }
        MathTransform transform;
        ParameterValueGroup params;

        //EPGS p. 18
        params = mtFactory.getDefaultParameters("Lambert_Conformal_Conic_1SP");
        params.parameter("semi_major")        .setValue(6378206.4);
        params.parameter("semi_minor")        .setValue(6356583.8);
        params.parameter("central_meridian")  .setValue(    -77.0);
        params.parameter("latitude_of_origin").setValue(     18.0);
        params.parameter("scale_factor")      .setValue(      1.0);
        params.parameter("false_easting")     .setValue( 250000.0);
        params.parameter("false_northing")    .setValue( 150000.0);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new DirectPosition2D(-76.943683333, 17.932166666),
                    new DirectPosition2D(255966.58, 142493.51), transform);
        
        //Spherical (me)
        params.parameter("semi_major")        .setValue(6370997.0);
        params.parameter("semi_minor")        .setValue(6370997.0);
        params.parameter("central_meridian")  .setValue(    111.0);
        params.parameter("latitude_of_origin").setValue(    -55.0);
        params.parameter("scale_factor")      .setValue(      1.0);
        params.parameter("false_easting")     .setValue( 500000.0);
        params.parameter("false_northing")    .setValue(1000000.0);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform.toString());
        }    
        doTransform(new DirectPosition2D(151.283333333, -33.916666666),
                    new DirectPosition2D(4232963.1816, 2287639.9866), transform);
        
        
        ///////////////////////////////////////
        // Lambert_Conformal_Conic_2SP tests //
        ///////////////////////////////////////
        if (VERBOSE) {
            printParameters("Lambert_Conformal_Conic_2SP");
        }        
        //EPSG p. 17
        params = mtFactory.getDefaultParameters("Lambert_Conformal_Conic_2SP");
        params.parameter("semi_major")         .setValue(6378206.4);
        params.parameter("semi_minor")         .setValue(6356583.8);
        params.parameter("central_meridian")   .setValue(    -99.0);
        params.parameter("latitude_of_origin") .setValue(     27.833333333);
        params.parameter("standard_parallel_1").setValue(     28.383333333);
        params.parameter("standard_parallel_2").setValue(     30.283333333);
        params.parameter("false_easting")      .setValue( 609601.218);        //metres
        params.parameter("false_northing")     .setValue(      0.0);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new DirectPosition2D(-96.0, 28.5),
                    new DirectPosition2D(903277.7965, 77650.94219), transform);
        
        //Spherical (me)
        params.parameter("semi_major")         .setValue(6370997.0);
        params.parameter("semi_minor")         .setValue(6370997.0);
        params.parameter("central_meridian")   .setValue(   -120.0);
        params.parameter("latitude_of_origin") .setValue(      0.0);
        params.parameter("standard_parallel_1").setValue(      2.0);
        params.parameter("standard_parallel_2").setValue(     60.0);
        params.parameter("false_easting")      .setValue(      0.0);
        params.parameter("false_northing")     .setValue(      0.0);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new DirectPosition2D(139.733333333, 35.6833333333),
                    new DirectPosition2D(-6789805.6471, 7107623.6859), transform);
        
        //1SP where SP != lat of origin (me)
        params.parameter("semi_major")         .setValue(6378137.0);
        params.parameter("semi_minor")         .setValue(6356752.31424518);
        params.parameter("central_meridian")   .setValue(      0.0);
        params.parameter("latitude_of_origin") .setValue(    -50.0);
        params.parameter("standard_parallel_1").setValue(    -40.0);
        params.parameter("standard_parallel_2").setValue(    -40.0);
        params.parameter("false_easting")      .setValue( 100000.0);
        params.parameter("false_northing")     .setValue(      0.0);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new DirectPosition2D(18.45, -33.9166666666),
                    new DirectPosition2D(1803288.3324, 1616657.7846), transform);
        
        
        ///////////////////////////////////////////////
        // Lambert_Conformal_Conic_2SP_Belgium test  //
        ///////////////////////////////////////////////
        if (VERBOSE) {
            printParameters("Lambert_Conformal_Conic_2SP_Belgium");
        }    
        //epsg p. 19
        params = mtFactory.getDefaultParameters("Lambert_Conformal_Conic_2SP_Belgium");
        params.parameter("semi_major")         .setValue(6378388.0);
        params.parameter("semi_minor")         .setValue(6356911.946);
        params.parameter("central_meridian")   .setValue(      4.356939722);
        params.parameter("latitude_of_origin") .setValue(     90.0);
        params.parameter("standard_parallel_1").setValue(     49.833333333);
        params.parameter("standard_parallel_2").setValue(     51.166666666);
        params.parameter("false_easting")      .setValue( 150000.01);
        params.parameter("false_northing")     .setValue(5400088.44);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }    
        doTransform(new DirectPosition2D(5.807370277, 50.6795725),
                    new DirectPosition2D(251763.20, 153034.13), transform);
    }
    
    /**
     * Some tests for Krovak Projection.
     */
    public void testKrovak() throws FactoryException, TransformException {

        ///////////////////////////////////////
        //          Krovak tests             //
        ///////////////////////////////////////
        if (VERBOSE) {
            printParameters("Krovak");
        }        
        MathTransform transform;
        ParameterValueGroup params;
        
        params = mtFactory.getDefaultParameters("Krovak");
        params.parameter("semi_major")                .setValue(6377397.155);
        params.parameter("semi_minor")                .setValue(6356078.963);
        params.parameter("latitude_of_center")        .setValue(49.5);
        params.parameter("longitude_of_center")       .setValue(42.5 - 17.66666666666667);
        params.parameter("azimuth")                   .setValue(30.28813972222222);
        params.parameter("pseudo_standard_parallel_1").setValue(78.5);
        params.parameter("scale_factor")              .setValue(0.9999);
        transform = mtFactory.createParameterizedTransform(params);
        if (VERBOSE) {
            System.out.println(transform);
        }
        doTransform(new DirectPosition2D(14.370530947 , 50.071153856 ),
                    new DirectPosition2D(-746742.6075,  -1044389.4516), transform);
    }
}
