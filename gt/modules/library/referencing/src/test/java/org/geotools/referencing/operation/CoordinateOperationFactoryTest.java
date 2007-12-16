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
package org.geotools.referencing.operation;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.Transformation;

import org.geotools.factory.Hints;
import org.geotools.factory.GeoTools;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.metadata.iso.quality.PositionalAccuracyImpl;


/**
 * Test the default coordinate operation factory.
 * <p>
 * <strong>NOTE:</strong> Some tests are disabled in the particular case when the
 * {@link CoordinateOperationFactory} is actually an {@link AuthorityBackedFactory}
 * instance. This is because the later can replace source or target CRS by some CRS
 * found in the EPSG authority factory, causing {@code assertSame} to fails. It may
 * also returns a more accurate operation than the one expected from the WKT in the
 * code below, causing transformation checks to fail as well. This situation occurs
 * only if some EPSG authority factory like {@code plugin/epsg-hsql} is found in the
 * classpath while the test are running. It should not occurs during Maven build, so
 * all tests should be executed with Maven. It may occurs during an execution from
 * the IDE however, in which case the tests are disabled in order to allows normal
 * execution of other tests.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class CoordinateOperationFactoryTest extends TestTransform {
    /**
     * Constructs a test case with the given name.
     */
    public CoordinateOperationFactoryTest(final String name) {
        super(name);
    }

    /**
     * Uses reflection to dynamically create a test suite containing all
     * the <code>testXXX()</code> methods - from the JUnit FAQ.
     */
    public static Test suite() {
        return new TestSuite(CoordinateOperationFactoryTest.class);
    }

    /**
     * Runs the tests with the textual test runner.
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Ensures that positional accuracy dependencies are properly loaded. This is not needed for
     * normal execution, but JUnit behavior with class loaders is sometime surprising.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assertNotNull(PositionalAccuracyImpl.DATUM_SHIFT_APPLIED);
        assertNotNull(PositionalAccuracyImpl.DATUM_SHIFT_OMITTED);
    }

    /**
     * Make sure that <code>createOperation(sourceCRS, targetCRS)</code>
     * returns an identity transform when <code>sourceCRS</code> and <code>targetCRS</code>
     * are identical, and tests the generic CRS.
     *
     * @todo uses static imports when we will be allowed to compile with J2SE 1.5.
     */
    public void testGenericTransform() throws FactoryException {
        assertTrue(opFactory.createOperation(DefaultGeographicCRS.WGS84,
                   DefaultGeographicCRS.WGS84).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(DefaultEngineeringCRS.CARTESIAN_2D,
                   DefaultEngineeringCRS.CARTESIAN_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(DefaultEngineeringCRS.CARTESIAN_3D,
                   DefaultEngineeringCRS.CARTESIAN_3D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(DefaultEngineeringCRS.GENERIC_2D,
                   DefaultEngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(DefaultEngineeringCRS.GENERIC_2D,
                   DefaultEngineeringCRS.CARTESIAN_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(DefaultEngineeringCRS.CARTESIAN_2D,
                   DefaultEngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(DefaultGeographicCRS.WGS84,
                   DefaultEngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(DefaultEngineeringCRS.GENERIC_2D,
                   DefaultGeographicCRS.WGS84).getMathTransform().isIdentity());
        try {
            opFactory.createOperation(DefaultEngineeringCRS.CARTESIAN_2D,
                                      DefaultGeographicCRS.WGS84);
            fail();
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
        try {
            opFactory.createOperation(DefaultGeographicCRS.WGS84,
                                      DefaultEngineeringCRS.CARTESIAN_2D);
            fail();
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
    }

    /**
     * Tests a transformation with unit conversion.
     */
    public void testUnitConversion() throws Exception {
        // NOTE: TOWGS84[0,0,0,0,0,0,0] is used here as a hack for
        //       avoiding datum shift. Shifts will be tested later.
        final CoordinateReferenceSystem targetCRS = crsFactory.createFromWKT(
                "PROJCS[\"TransverseMercator\",\n"                   +
                "  GEOGCS[\"Sphere\",\n"                             +
                "    DATUM[\"Sphere\",\n"                            +
                "      SPHEROID[\"Sphere\", 6370997.0, 0.0],\n"      +
                "      TOWGS84[0,0,0,0,0,0,0]],\n"                   +
                "    PRIMEM[\"Greenwich\", 0.0],\n"                  +
                "    UNIT[\"degree\", 0.017453292519943295],\n"      +
                "    AXIS[\"Longitude\", EAST],\n"                   +
                "    AXIS[\"Latitude\", NORTH]],\n"                  +
                "  PROJECTION[\"Transverse_Mercator\",\n"            +
                "    AUTHORITY[\"OGC\",\"Transverse_Mercator\"]],\n" +
                "  PARAMETER[\"central_meridian\", 170.0],\n"        +
                "  PARAMETER[\"latitude_of_origin\", 50.0],\n"       +
                "  PARAMETER[\"scale_factor\", 0.95],\n"             +
                "  PARAMETER[\"false_easting\", 0.0],\n"             +
                "  PARAMETER[\"false_northing\", 0.0],\n"            +
                "  UNIT[\"feet\", 0.304800609601219],\n"             +
                "  AXIS[\"x\", EAST],\n"                             +
                "  AXIS[\"y\", NORTH]]\n");

        final CoordinateReferenceSystem sourceCRS = crsFactory.createFromWKT(
                "GEOGCS[\"Sphere\",\n"                        +
                "  DATUM[\"Sphere\",\n"                       +
                "    SPHEROID[\"Sphere\", 6370997.0, 0.0],\n" +
                "    TOWGS84[0,0,0,0,0,0,0]],\n"              +
                "  PRIMEM[\"Greenwich\", 0.0],\n"             +
                "  UNIT[\"degree\", 0.017453292519943295],\n" +
                "  AXIS[\"Longitude\", EAST],\n"              +
                "  AXIS[\"Latitude\", NORTH]]\n");

        final CoordinateOperation operation = opFactory.createOperation(sourceCRS, targetCRS);
        assertEquals(sourceCRS, operation.getSourceCRS());
        assertEquals(targetCRS, operation.getTargetCRS());
        assertTrue  (operation instanceof Projection);

        final ParameterValueGroup param = ((Operation) operation).getParameterValues();
        assertEquals("semi_major",     6370997.0, param.parameter("semi_major"        ).doubleValue(), 1E-5);
        assertEquals("semi_minor",     6370997.0, param.parameter("semi_minor"        ).doubleValue(), 1E-5);
        assertEquals("latitude_of_origin",  50.0, param.parameter("latitude_of_origin").doubleValue(), 1E-8);
        assertEquals("central_meridian",   170.0, param.parameter("central_meridian"  ).doubleValue(), 1E-8);
        assertEquals("scale_factor",        0.95, param.parameter("scale_factor"      ).doubleValue(), 1E-8);
        assertEquals("false_easting",        0.0, param.parameter("false_easting"     ).doubleValue(), 1E-8);
        assertEquals("false_northing",       0.0, param.parameter("false_northing"    ).doubleValue(), 1E-8);

        final MathTransform transform = operation.getMathTransform();
        assertInterfaced(transform);
        assertTransformEquals2_2(transform.inverse(), 0, 0, 170, 50);
        assertTransformEquals2_2(transform, 170, 50, 0, 0);
    }

    /**
     * Tests a transformation that requires a datum shift with TOWGS84[0,0,0].
     * In addition, this method tests datum aliases.
     */
    public void testEllipsoidShift() throws Exception {
        final CoordinateReferenceSystem sourceCRS = crsFactory.createFromWKT(
                "GEOGCS[\"NAD83\",\n"                                           +
                "  DATUM[\"North_American_Datum_1983\",\n"                      +
                "    SPHEROID[\"GRS 1980\", 6378137.0, 298.257222101,\n"        +
                "      AUTHORITY[\"EPSG\",\"7019\"]],\n"                        +
                "    TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\n"             +
                "    AUTHORITY[\"EPSG\",\"6269\"]],\n"                          +
                "  PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]],\n" +
                "  UNIT[\"degree\", 0.017453292519943295],\n"                   +
                "  AXIS[\"Lon\", EAST],\n"                                      +
                "  AXIS[\"Lat\", NORTH],\n"                                     +
                "  AUTHORITY[\"EPSG\",\"4269\"]]");

        final CoordinateReferenceSystem targetCRS = crsFactory.createFromWKT(
                "GEOGCS[\"GCS_WGS_1984\",\n"                               +
                "  DATUM[\"D_WGS_1984\",\n"                                +
                "    SPHEROID[\"WGS_1984\", 6378137.0, 298.257223563]],\n" +
                "  PRIMEM[\"Greenwich\", 0.0],\n"                          +
                "  UNIT[\"degree\", 0.017453292519943295],\n"              +
                "  AXIS[\"Lon\", EAST],\n"                                 +
                "  AXIS[\"Lat\", NORTH]]");

        final CoordinateOperation operation = opFactory.createOperation(sourceCRS, targetCRS);
        if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
            assertSame(sourceCRS, operation.getSourceCRS());
            assertSame(targetCRS, operation.getTargetCRS());
        }
        final MathTransform transform = operation.getMathTransform();
        assertInterfaced(transform);
        assertTransformEquals2_2(transform, -180, -88.21076182660325, -180, -88.21076182655470);
        assertTransformEquals2_2(transform, +180,  85.41283436546335, -180,  85.41283436531322);
//      assertTransformEquals2_2(transform, +180,  85.41283436546335, +180,  85.41283436548373);
        // Note 1: Expected values above were computed with Geotools (not an external library).
        // Note 2: The commented-out test it the one we get when using geocentric instead of
        //         Molodenski method.
    }

    /**
     * Tests a transformation that requires a datum shift.
     */
    public void testDatumShift() throws Exception {
        final CoordinateReferenceSystem sourceCRS = crsFactory.createFromWKT(
                "GEOGCS[\"NTF (Paris)\",\n"                                             +
                "  DATUM[\"Nouvelle_Triangulation_Francaise\",\n"                       +
                "    SPHEROID[\"Clarke 1880 (IGN)\", 6378249.2, 293.466021293627,\n"    +
                "      AUTHORITY[\"EPSG\",\"7011\"]],\n"                                +
                "    TOWGS84[-168,-60,320,0,0,0,0],\n"                                  +
                "    AUTHORITY[\"EPSG\",\"6275\"]],\n"                                  +
                "  PRIMEM[\"Paris\", 2.5969213, AUTHORITY[\"EPSG\",\"8903\"]],\n"       +
                "  UNIT[\"grad\", 0.015707963267949, AUTHORITY[\"EPSG\", \"9105\"]],\n" +
                "  AXIS[\"Lat\",NORTH],\n"+
                "  AXIS[\"Long\",EAST],\n" +
                "  AUTHORITY[\"EPSG\",\"4807\"]]");

        final CoordinateReferenceSystem targetCRS = crsFactory.createFromWKT(
                "GEOGCS[\"WGS84\",\n"                                   +
                "  DATUM[\"WGS84\",\n"                                  +
                "    SPHEROID[\"WGS84\", 6378137.0, 298.257223563]],\n" +
                "  PRIMEM[\"Greenwich\", 0.0],\n"                       +
                "  UNIT[\"degree\", 0.017453292519943295],\n"+""        +
                "  AXIS[\"Longitude\",EAST],"                           +
                "  AXIS[\"Latitude\",NORTH]]");

        final CoordinateOperation operation = opFactory.createOperation(sourceCRS, targetCRS);
        if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
            assertSame (sourceCRS, operation.getSourceCRS());
            assertSame (targetCRS, operation.getTargetCRS());
            assertTrue (operation.getCoordinateOperationAccuracy().contains(PositionalAccuracyImpl.DATUM_SHIFT_APPLIED));
            assertFalse(operation.getCoordinateOperationAccuracy().contains(PositionalAccuracyImpl.DATUM_SHIFT_OMITTED));
        }
        final MathTransform transform = operation.getMathTransform();
        assertInterfaced(transform);
        assertTransformEquals2_2(transform,  0,   0,  2.3367521703619816, 0.0028940088671177986);
        assertTransformEquals2_2(transform, 20, -10, -6.663517606186469, 18.00134508026729);
        // Note: Expected values above were computed with Geotools (not an external library).
        //       However, it was tested with both Molodenski and Geocentric transformations.

        /*
         * Remove the TOWGS84 element and test again. An exception should be throws,
         * since no Bursa-Wolf parameters were available.
         */
        final CoordinateReferenceSystem amputedCRS;
        if (true) {
            String wkt = sourceCRS.toWKT();
            final int start = wkt.indexOf("TOWGS84");  assertTrue(start >= 0);
            final int end   = wkt.indexOf(']', start); assertTrue(end   >= 0);
            final int comma = wkt.indexOf(',', end);   assertTrue(comma >= 0);
            wkt = wkt.substring(0, start) + wkt.substring(comma+1);
            amputedCRS = crsFactory.createFromWKT(wkt);
        } else {
            amputedCRS = sourceCRS;
        }
        try {
            assertNotNull(opFactory.createOperation(amputedCRS, targetCRS));
            fail("Operation without Bursa-Wolf parameters should not have been allowed.");
        } catch (OperationNotFoundException excption) {
            // This is the expected exception.
        }
        /*
         * Try again with hints, asking for a lenient factory.
         */
        CoordinateOperationFactory lenientFactory;
        Hints hints = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.FALSE);
        lenientFactory = ReferencingFactoryFinder.getCoordinateOperationFactory(hints);
        assertSame(opFactory, lenientFactory);
        hints.put(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        lenientFactory = ReferencingFactoryFinder.getCoordinateOperationFactory(hints);
        assertNotSame(opFactory, lenientFactory);
        final CoordinateOperation lenient = lenientFactory.createOperation(amputedCRS, targetCRS);
        assertSame(amputedCRS, lenient.getSourceCRS());
        assertSame( targetCRS, lenient.getTargetCRS());
        assertFalse(lenient.getCoordinateOperationAccuracy().contains(PositionalAccuracyImpl.DATUM_SHIFT_APPLIED));
        assertTrue (lenient.getCoordinateOperationAccuracy().contains(PositionalAccuracyImpl.DATUM_SHIFT_OMITTED));

        final MathTransform lenientTr = lenient.getMathTransform();
        assertInterfaced(lenientTr);
        assertTransformEquals2_2(lenientTr,  0,   0,  2.33722917, 0.0);
        assertTransformEquals2_2(lenientTr, 20, -10, -6.66277083, 17.99814879585781);
//      assertTransformEquals2_2(lenientTr, 20, -10, -6.66277083, 17.998143675921714);
        // Note 1: Expected values above were computed with Geotools (not an external library).
        // Note 2: The commented-out test is the one we get with "Abridged_Molodenski" method
        //         instead of "Molodenski".
    }

    /**
     * Tests a transformation that requires a datum shift with 7 parameters.
     */
    public void testDatumShift7Param() throws Exception {
        final CoordinateReferenceSystem sourceCRS = DefaultGeographicCRS.WGS84;
        final CoordinateReferenceSystem targetCRS = crsFactory.createFromWKT(
                "PROJCS[\"IGN53 Mare / UTM zone 58S\",\n"                                 +
                "  GEOGCS[\"IGN53 Mare\",\n"                                              +
                "    DATUM[\"IGN53 Mare\",\n"                                             +
                "      SPHEROID[\"International 1924\", 6378388.0, 297.0, AUTHORITY[\"EPSG\",\"7022\"]],\n"  +
                "      TOWGS84[-408.809, 366.856, -412.987, 1.8842, -0.5308, 2.1655, -24.978523651158998],\n"+
                "      AUTHORITY[\"EPSG\",\"6641\"]],\n"                                  +
                "    PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]],\n"         +
                "    UNIT[\"degree\", 0.017453292519943295],\n"                           +
                "    AXIS[\"Geodetic latitude\", NORTH, AUTHORITY[\"EPSG\",\"106\"]],\n"  +
                "    AXIS[\"Geodetic longitude\", EAST, AUTHORITY[\"EPSG\",\"107\"]],\n"  +
                "    AUTHORITY[\"EPSG\",\"4641\"]],\n"                                    +
                "  PROJECTION[\"Transverse Mercator\", AUTHORITY[\"EPSG\",\"9807\"]],\n"  +
                "  PARAMETER[\"central_meridian\", 165.0],\n"                             +
                "  PARAMETER[\"latitude_of_origin\", 0.0],\n"                             +
                "  PARAMETER[\"scale_factor\", 0.9996],\n"                                +
                "  PARAMETER[\"false_easting\", 500000.0],\n"                             +
                "  PARAMETER[\"false_northing\", 10000000.0],\n"                          +
                "  UNIT[\"m\", 1.0],\n"                                                   +
                "  AXIS[\"Easting\", EAST, AUTHORITY[\"EPSG\",\"1\"]],\n"                 +
                "  AXIS[\"Northing\", NORTH, AUTHORITY[\"EPSG\",\"2\"]],\n"               +
                "  AUTHORITY[\"EPSG\",\"2995\"]]");

        CoordinateOperation operation = opFactory.createOperation(sourceCRS, targetCRS);
        if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
            assertSame(sourceCRS, operation.getSourceCRS());
            assertSame(targetCRS, operation.getTargetCRS());
            assertTrue (operation.getCoordinateOperationAccuracy().contains(PositionalAccuracyImpl.DATUM_SHIFT_APPLIED));
            assertFalse(operation.getCoordinateOperationAccuracy().contains(PositionalAccuracyImpl.DATUM_SHIFT_OMITTED));
        }
        MathTransform transform = operation.getMathTransform();
        assertInterfaced(transform);
        assertTransformEquals2_2(transform, 168.1075, -21.597283333333, 822023.338884308, 7608648.67486555);
        // Note: Expected values above were computed with Geotools (not an external library).

        /*
         * Try again using lenient factory. The result should be identical, since we do have
         * Bursa-Wolf parameters. This test failed before GEOT-661 fix.
         */
        final Hints hints = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        final CoordinateOperationFactory lenientFactory =
                ReferencingFactoryFinder.getCoordinateOperationFactory(hints);
        assertNotSame(opFactory, lenientFactory);
        operation = lenientFactory.createOperation(sourceCRS, targetCRS);
        if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
            assertSame(sourceCRS, operation.getSourceCRS());
            assertSame(targetCRS, operation.getTargetCRS());
            assertTrue (operation.getCoordinateOperationAccuracy().contains(PositionalAccuracyImpl.DATUM_SHIFT_APPLIED));
            assertFalse(operation.getCoordinateOperationAccuracy().contains(PositionalAccuracyImpl.DATUM_SHIFT_OMITTED));
        }
        transform = operation.getMathTransform();
        assertInterfaced(transform);
        assertTransformEquals2_2(transform, 168.1075, -21.597283333333, 822023.338884308, 7608648.67486555);
        // Note: Expected values above were computed with Geotools (not an external library).
    }

    /**
     * Tests transformations involving compound CRS.
     */
    public void testCompoundCRS() throws Exception {
        final String WGS84 =
                "GEOGCS[\"WGS 84\",\n"                                                  +
                "  DATUM[\"WGS_1984\",\n"                                               +
                "    SPHEROID[\"WGS 84\", 6378137, 298.257223563,\n"                    +
                "      AUTHORITY[\"EPSG\",\"7030\"]],\n"                                +
                "    TOWGS84[0,0,0,0,0,0,0],\n"                                         +
                "    AUTHORITY[\"EPSG\",\"6326\"]],\n"                                  +
                "  PRIMEM[\"Greenwich\", 0, AUTHORITY[\"EPSG\",\"8901\"]],\n"           +
                "  UNIT[\"DMSH\",0.0174532925199433, AUTHORITY[\"EPSG\",\"9108\"]],\n"  +
                "  AXIS[\"Lat\",NORTH],\n"                                              +
                "  AXIS[\"Long\",EAST],\n"                                              +
                "  AUTHORITY[\"EPSG\",\"4326\"]]\n";

        final String NAD27 =
                "GEOGCS[\"NAD27\",\n"                                                   +
                "  DATUM[\"North_American_Datum_1927\",\n"                              +
                "    SPHEROID[\"Clarke 1866\", 6378206.4, 294.978698213901,\n"          +
                "      AUTHORITY[\"EPSG\",\"7008\"]],\n"                                +
                "    TOWGS84[-3,142,183,0,0,0,0],\n"                                    +
                "    AUTHORITY[\"EPSG\",\"6267\"]],\n"                                  +
                "  PRIMEM[\"Greenwich\", 0, AUTHORITY[\"EPSG\",\"8901\"]],\n"           +
                "  UNIT[\"DMSH\",0.0174532925199433, AUTHORITY[\"EPSG\",\"9108\"]],\n"  +
                "  AXIS[\"Lat\",NORTH],\n"                                              +
                "  AXIS[\"Long\",EAST],\n"                                              +
                "  AUTHORITY[\"EPSG\",\"4267\"]]\n";

        final String Z = "VERT_CS[\"ellipsoid Z in meters\",\n"+"" +
                         "  VERT_DATUM[\"Ellipsoid\",2002],\n"     +
                         "  UNIT[\"metre\", 1],\n"                 +
                         "  AXIS[\"Z\",UP]]";

        final String H =
                "VERT_CS[\"mean sea level height\",\n"                                    +
                "  VERT_DATUM[\"Mean Sea Level\", 2005, AUTHORITY[\"EPSG\",\"5100\"]],\n" +
                "  UNIT[\"metre\", 1, AUTHORITY[\"EPSG\",\"9001\"]],\n"                   +
                "  AXIS[\"Z\",UP], AUTHORITY[\"EPSG\",\"5714\"]]\n";

        final String WGS84_Z = "COMPD_CS[\"Wgs84 with height Z\","+WGS84+","+Z+"]";
        final String NAD27_Z = "COMPD_CS[\"NAD27 with height Z\","+NAD27+","+Z+"]";
        final String Z_NAD27 = "COMPD_CS[\"height Z with NAD27\","+Z+","+NAD27+"]";

        final String WGS84_H = "COMPD_CS[\"Wgs84 with sea-level Z\","+WGS84+","+H+"]";
        final String NAD27_H = "COMPD_CS[\"NAD27 with sea-level Z\","+NAD27+","+H+"]";

        CoordinateReferenceSystem sourceCRS, targetCRS;
        CoordinateOperation op;
        MathTransform mt;

        if (true) {
            sourceCRS = crsFactory.createFromWKT(NAD27);
            targetCRS = crsFactory.createFromWKT(WGS84);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            assertTrue(op instanceof Transformation);
            if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
                assertSame(sourceCRS, op.getSourceCRS());
                assertSame(targetCRS, op.getTargetCRS());
            }
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
                // Note: Expected values below were computed with Geotools (not an external library).
                //       However, it was tested with both Molodenski and Geocentric transformations.
                assertTransformEquals2_2(mt, 0.0,                   0.0,
                                             0.001654978796746043,  0.0012755944235822696);
                assertTransformEquals2_2(mt, 5.0,                   8.0,
                                             5.001262960018587,     8.001271733843957);
            }
        }
        if (true) {
            sourceCRS = crsFactory.createFromWKT(Z);
            targetCRS = crsFactory.createFromWKT(Z);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
                assertSame(sourceCRS, op.getSourceCRS());
                assertSame(targetCRS, op.getTargetCRS());
            }
            assertTrue(op instanceof Conversion);
            assertTrue(mt.isIdentity());
            assertInterfaced(mt);
        }
        if (true) {
            sourceCRS = crsFactory.createFromWKT(H);
            targetCRS = crsFactory.createFromWKT(H);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
                assertSame(sourceCRS, op.getSourceCRS());
                assertSame(targetCRS, op.getTargetCRS());
            }
            assertTrue(op instanceof Conversion);
            assertTrue(mt.isIdentity());
            assertInterfaced(mt);
        }
        if (true) try {
            sourceCRS = crsFactory.createFromWKT(Z);
            targetCRS = crsFactory.createFromWKT(H);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            fail();
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
        if (true) {
            sourceCRS = crsFactory.createFromWKT(NAD27_Z);
            targetCRS = crsFactory.createFromWKT(WGS84_Z);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            assertNotSame(sourceCRS, op.getSourceCRS());
            assertNotSame(targetCRS, op.getTargetCRS());
            assertTrue(op                instanceof Transformation);
            assertTrue(sourceCRS         instanceof CompoundCRS);
            assertTrue(op.getSourceCRS() instanceof GeographicCRS);   // 2D + 1D  --->  3D
            assertTrue(targetCRS         instanceof CompoundCRS);
            assertTrue(op.getTargetCRS() instanceof GeographicCRS);   // 2D + 1D  --->  3D
            assertFalse(sourceCRS.equals(targetCRS));
            assertFalse(op.getSourceCRS().equals(op.getTargetCRS()));
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            // Note: Expected values below were computed with Geotools (not an external library).
            //       However, it was tested with both Molodenski and Geocentric transformations.
            assertTransformEquals3_3(mt, 0,                    0,                      0,
                                         0.001654978796746043, 0.0012755944235822696, 66.4042236590758);
            assertTransformEquals3_3(mt, 5,                    8,                     20,
                                         5.0012629560319874,   8.001271729856333,    120.27929787151515);
            assertTransformEquals3_3(mt, 5,                    8,                    -20,
                                         5.001262964005206,    8.001271737831601,     80.2792978901416);
            assertTransformEquals3_3(mt,-5,                   -8,                    -20,
                                        -4.99799698932651,    -7.998735783965731,      9.007854541763663);
        }
        if (true) {
            sourceCRS = crsFactory.createFromWKT(Z_NAD27);
            targetCRS = crsFactory.createFromWKT(WGS84_Z);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            assertNotSame(sourceCRS, op.getSourceCRS());
            assertNotSame(targetCRS, op.getTargetCRS());
            assertTrue(op                instanceof Transformation);
            assertTrue(sourceCRS         instanceof CompoundCRS);
            assertTrue(op.getSourceCRS() instanceof GeographicCRS);   // 2D + 1D  --->  3D
            assertTrue(targetCRS         instanceof CompoundCRS);
            assertTrue(op.getTargetCRS() instanceof GeographicCRS);   // 2D + 1D  --->  3D
            assertFalse(sourceCRS.equals(targetCRS));
            assertFalse(op.getSourceCRS().equals(op.getTargetCRS()));
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            // Note: Expected values below were computed with Geotools (not an external library).
            //       However, it was tested with both Molodenski and Geocentric transformations.
            assertTransformEquals3_3(mt, 0,                    0,                      0,
                                         0.001654978796746043, 0.0012755944235822696, 66.4042236590758);
            assertTransformEquals3_3(mt, -20,                  5,                      8,
                                         5.001262964005206,    8.001271737831601,     80.2792978901416);
        }
        if (true) {
            sourceCRS = crsFactory.createFromWKT(NAD27_Z);
            targetCRS = crsFactory.createFromWKT(WGS84);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
                assertNotSame(sourceCRS, op.getSourceCRS());
                assertSame   (targetCRS, op.getTargetCRS());
            }
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            // Note: Expected values below were computed with Geotools (not an external library).
            //       However, it was tested with both Molodenski and Geocentric transformations.
            assertTransformEquals3_2(mt, 0,                    0,                      0,
                                         0.001654978796746043, 0.0012755944235822696);
            assertTransformEquals3_2(mt, 5,                    8,                     20,
                                         5.0012629560319874,   8.001271729856333);
            assertTransformEquals3_2(mt, 5,                    8,                    -20,
                                         5.001262964005206,    8.001271737831601);
        }
        if (true) {
            sourceCRS = crsFactory.createFromWKT(NAD27_Z);
            targetCRS = crsFactory.createFromWKT(Z);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
                assertSame(sourceCRS, op.getSourceCRS());
                assertSame(targetCRS, op.getTargetCRS());
            }
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            assertTransformEquals3_1(mt,  0,  0, 0,   0);
            assertTransformEquals3_1(mt,  5,  8, 20, 20);
            assertTransformEquals3_1(mt, -5, -8, 20, 20);
        }
        if (true) {
            sourceCRS = crsFactory.createFromWKT(NAD27);
            targetCRS = crsFactory.createFromWKT(WGS84_Z);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
                assertSame   (sourceCRS, op.getSourceCRS());
                assertNotSame(targetCRS, op.getTargetCRS());
            }
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            // Note: Expected values below were computed with Geotools (not an external library).
            //       However, it was tested with both Molodenski and Geocentric transformations.
            assertTransformEquals2_3(mt, 0,                    0,
                                         0.001654978796746043, 0.0012755944235822696, 66.4042236590758);
            assertTransformEquals2_3(mt, 5,                    8,
                                         5.001262960018587,    8.001271733843957,    100.27929787896574);
        }
        if (true) try {
            sourceCRS = crsFactory.createFromWKT(NAD27_H);
            targetCRS = crsFactory.createFromWKT(NAD27_Z);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            assertNotSame(sourceCRS, op.getSourceCRS());
            assertNotSame(targetCRS, op.getTargetCRS());
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            fail("Should fails unless GEOT-352 has been fixed");
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
        if (true) try {
            sourceCRS = crsFactory.createFromWKT(NAD27_H);
            targetCRS = crsFactory.createFromWKT(WGS84_H);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            assertNotSame(sourceCRS, op.getSourceCRS());
            assertNotSame(targetCRS, op.getTargetCRS());
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            fail("Should fails unless GEOT-352 has been fixed");
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
        if (true) try {
            sourceCRS = crsFactory.createFromWKT(NAD27);
            targetCRS = crsFactory.createFromWKT(WGS84_H);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
                assertSame   (sourceCRS, op.getSourceCRS());
                assertNotSame(targetCRS, op.getTargetCRS());
            }
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            fail("Should fails unless GEOT-352 has been fixed");
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
        if (true) {
            sourceCRS = crsFactory.createFromWKT(NAD27_H);
            targetCRS = crsFactory.createFromWKT(H);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            if (!(opFactory instanceof AuthorityBackedFactory)) { // See comment in class javadoc
                assertSame(sourceCRS, op.getSourceCRS());
                assertSame(targetCRS, op.getTargetCRS());
            }
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            assertTransformEquals3_1(mt,  0,  0, 0,   0);
            assertTransformEquals3_1(mt,  5,  8, 20, 20);
            assertTransformEquals3_1(mt, -5, -8, 20, 20);
        }
    }

    /**
     * Make sure that a factory can be find in the presence of some global hints.
     *
     * @see http://jira.codehaus.org/browse/GEOT-1618
     */
    public void testFactoryWithHints() {
        final Hints hints = new Hints();
        hints.put(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        hints.put(Hints.FORCE_STANDARD_AXIS_DIRECTIONS,   Boolean.TRUE);
        hints.put(Hints.FORCE_STANDARD_AXIS_UNITS,        Boolean.TRUE);

        final CoordinateOperationFactory factory =
                ReferencingFactoryFinder.getCoordinateOperationFactory(hints);
        assertSame(opFactory, factory);
    }
}
