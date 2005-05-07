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
package org.geotools.referencing.operation;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.metadata.quality.PositionalAccuracy;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.referencing.operation.PassThroughOperation;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.Transformation;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.FactoryFinder;


/**
 * Test transformation factory.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TransformationTest extends TestTransform {
    /**
     * Constructs a test case with the given name.
     */
    public TransformationTest(final String name) {
        super(name);
    }
    
    /**
     * Uses reflection to dynamically create a test suite containing all 
     * the <code>testXXX()</code> methods - from the JUnit FAQ.
     */
    public static Test suite() {
        return new TestSuite(TransformationTest.class);
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
    protected void setUp() throws Exception {
        super.setUp();
        assertNotNull(org.geotools.metadata.quality.PositionalAccuracy.DATUM_SHIFT_APPLIED);
        assertNotNull(org.geotools.metadata.quality.PositionalAccuracy.DATUM_SHIFT_OMITTED);
    }

    /**
     * Quick self test, in part to give this test suite a test
     * and also to test the internal method.
     */
    public void testAssertPointsEqual(){
        String name = "self test";
        double a[]     = {10,   10  };
        double b[]     = {10.1, 10.1};
        double delta[] = { 0.2,  0.2};
        assertPointsEqual(name, a, b, delta);
    }

    /**
     * Make sure that <code>createOperation(sourceCRS, targetCRS)</code>
     * returns an identity transform when <code>sourceCRS</code> and <code>targetCRS</code>
     * are identical, and tests the generic CRS.
     *
     * @todo uses static imports when we will be allowed to compile with J2SE 1.5.
     */
    public void testGenericTransform() throws FactoryException {
        assertTrue(opFactory.createOperation(org.geotools.referencing.crs.GeographicCRS.WGS84,
                   org.geotools.referencing.crs.GeographicCRS.WGS84).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(org.geotools.referencing.crs.EngineeringCRS.CARTESIAN_2D,
                   org.geotools.referencing.crs.EngineeringCRS.CARTESIAN_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(org.geotools.referencing.crs.EngineeringCRS.CARTESIAN_3D,
                   org.geotools.referencing.crs.EngineeringCRS.CARTESIAN_3D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(org.geotools.referencing.crs.EngineeringCRS.GENERIC_2D,
                   org.geotools.referencing.crs.EngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(org.geotools.referencing.crs.EngineeringCRS.GENERIC_2D,
                   org.geotools.referencing.crs.EngineeringCRS.CARTESIAN_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(org.geotools.referencing.crs.EngineeringCRS.CARTESIAN_2D,
                   org.geotools.referencing.crs.EngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(org.geotools.referencing.crs.GeographicCRS.WGS84,
                   org.geotools.referencing.crs.EngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(org.geotools.referencing.crs.EngineeringCRS.GENERIC_2D,
                   org.geotools.referencing.crs.GeographicCRS.WGS84).getMathTransform().isIdentity());
        try {
            opFactory.createOperation(org.geotools.referencing.crs.EngineeringCRS.CARTESIAN_2D,
                                      org.geotools.referencing.crs.GeographicCRS.WGS84);
            fail();
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
        try {
            opFactory.createOperation(org.geotools.referencing.crs.GeographicCRS.WGS84,
                                      org.geotools.referencing.crs.EngineeringCRS.CARTESIAN_2D);
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
        assertSame(sourceCRS, operation.getSourceCRS());
        assertSame(targetCRS, operation.getTargetCRS());

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
        assertSame(sourceCRS, operation.getSourceCRS());
        assertSame(targetCRS, operation.getTargetCRS());
        assertTrue(contains(operation.getPositionalAccuracy(),
                   org.geotools.metadata.quality.PositionalAccuracy.DATUM_SHIFT_APPLIED));
        assertFalse(contains(operation.getPositionalAccuracy(),
                   org.geotools.metadata.quality.PositionalAccuracy.DATUM_SHIFT_OMITTED));

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
        lenientFactory = FactoryFinder.getCoordinateOperationFactory(hints);
        assertSame(opFactory, lenientFactory);
        hints.put(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        lenientFactory = FactoryFinder.getCoordinateOperationFactory(hints);
        assertNotSame(opFactory, lenientFactory);
        final CoordinateOperation lenient = lenientFactory.createOperation(amputedCRS, targetCRS);
        assertSame(amputedCRS, lenient.getSourceCRS());
        assertSame( targetCRS, lenient.getTargetCRS());
        assertFalse(contains(lenient.getPositionalAccuracy(),
                   org.geotools.metadata.quality.PositionalAccuracy.DATUM_SHIFT_APPLIED));
        assertTrue(contains(lenient.getPositionalAccuracy(),
                   org.geotools.metadata.quality.PositionalAccuracy.DATUM_SHIFT_OMITTED));

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
     * Returns {@code true} if the specified array contains the specified element.
     */
    private static boolean contains(final PositionalAccuracy[] array,
                                    final PositionalAccuracy searchFor)
    {
        assertNotNull(searchFor);
        for (int i=0; i<array.length; i++) {
            if (array[i].equals(searchFor)) {
                return true;
            }
        }
        return false;
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
            assertSame(sourceCRS, op.getSourceCRS());
            assertSame(targetCRS, op.getTargetCRS());
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            // Note: Expected values below were computed with Geotools (not an external library).
            //       However, it was tested with both Molodenski and Geocentric transformations.
            assertTransformEquals2_2(mt, 0.0,                   0.0,
                                         0.001654978796746043,  0.0012755944235822696);
            assertTransformEquals2_2(mt, 5.0,                   8.0,
                                         5.001262960018587,     8.001271733843957);
        }
        if (true) {
            sourceCRS = crsFactory.createFromWKT(Z);
            targetCRS = crsFactory.createFromWKT(Z);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            assertSame(sourceCRS, op.getSourceCRS());
            assertSame(targetCRS, op.getTargetCRS());
            assertTrue(op instanceof Conversion);
            assertTrue(mt.isIdentity());
            assertInterfaced(mt);
        }
        if (true) {
            sourceCRS = crsFactory.createFromWKT(H);
            targetCRS = crsFactory.createFromWKT(H);
            op = opFactory.createOperation(sourceCRS, targetCRS);
            mt = op.getMathTransform();
            assertSame(sourceCRS, op.getSourceCRS());
            assertSame(targetCRS, op.getTargetCRS());
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
            assertNotSame(sourceCRS, op.getSourceCRS());
            assertSame(targetCRS, op.getTargetCRS());
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
            assertSame(sourceCRS, op.getSourceCRS());
            assertSame(targetCRS, op.getTargetCRS());
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
            assertSame   (sourceCRS, op.getSourceCRS());
            assertNotSame(targetCRS, op.getTargetCRS());
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
            assertSame   (sourceCRS, op.getSourceCRS());
            assertNotSame(targetCRS, op.getTargetCRS());
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
            assertSame(sourceCRS, op.getSourceCRS());
            assertSame(targetCRS, op.getTargetCRS());
            assertFalse(mt.isIdentity());
            assertInterfaced(mt);
            assertTransformEquals3_1(mt,  0,  0, 0,   0);
            assertTransformEquals3_1(mt,  5,  8, 20, 20);
            assertTransformEquals3_1(mt, -5, -8, 20, 20);
        }
    }
}
