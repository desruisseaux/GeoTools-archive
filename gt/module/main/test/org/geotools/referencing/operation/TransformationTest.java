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
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.PassThroughOperation;
import org.opengis.referencing.operation.OperationNotFoundException;

// Geotools dependencies
import org.geotools.referencing.crs.EngineeringCRS;
import org.geotools.referencing.crs.GeographicCRS;


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
     */
    public void testGenericTransform() throws FactoryException {
        assertTrue(opFactory.createOperation(GeographicCRS.WGS84,
                   GeographicCRS.WGS84).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.CARTESIAN_2D,
                   EngineeringCRS.CARTESIAN_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.CARTESIAN_3D,
                   EngineeringCRS.CARTESIAN_3D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.GENERIC_2D,
                   EngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.GENERIC_2D,
                   EngineeringCRS.CARTESIAN_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.CARTESIAN_2D,
                   EngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(GeographicCRS.WGS84,
                   EngineeringCRS.GENERIC_2D).getMathTransform().isIdentity());
        assertTrue(opFactory.createOperation(EngineeringCRS.GENERIC_2D,
                   GeographicCRS.WGS84).getMathTransform().isIdentity());
        try {
            opFactory.createOperation(EngineeringCRS.CARTESIAN_2D,
                                      GeographicCRS.WGS84);
            fail();
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
        try {
            opFactory.createOperation(GeographicCRS.WGS84,
                                      EngineeringCRS.CARTESIAN_2D);
            fail();
        } catch (OperationNotFoundException exception) {
            // This is the expected exception.
        }
    }

    /**
     * Tests a transformation with unit conversion.
     */
    public void testUnitConversion() throws Exception {
        // NOTE: TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0] is used here as
        //       a hack for avoiding datum shift. Shifts will be tested later.
        final CoordinateReferenceSystem targetCRS = crsFactory.createFromWKT(
                "PROJCS[\"TransverseMercator\",\n"                     +
                "  GEOGCS[\"Sphere\",\n"                               +
                "    DATUM[\"Sphere\",\n"                              +
                "      SPHEROID[\"Sphere\", 6370997.0, 0.0],\n"        +
                "      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]],\n" +
                "    PRIMEM[\"Greenwich\", 0.0],\n"                    +
                "    UNIT[\"degree\", 0.017453292519943295],\n"        +
                "    AXIS[\"Longitude\", EAST],\n"                     +
                "    AXIS[\"Latitude\", NORTH]],\n"                    +
                "  PROJECTION[\"Transverse_Mercator\",\n"              +
                "    AUTHORITY[\"OGC\",\"Transverse_Mercator\"]],\n"   +
                "  PARAMETER[\"central_meridian\", 170.0],\n"          +
                "  PARAMETER[\"latitude_of_origin\", 50.0],\n"         +
                "  PARAMETER[\"scale_factor\", 0.95],\n"               +
                "  PARAMETER[\"false_easting\", 0.0],\n"               +
                "  PARAMETER[\"false_northing\", 0.0],\n"              +
                "  UNIT[\"feet\", 0.304800609601219],\n"               +
                "  AXIS[\"x\", EAST],\n"                               +
                "  AXIS[\"y\", NORTH]]\n");

        final CoordinateReferenceSystem sourceCRS = crsFactory.createFromWKT(
                "GEOGCS[\"Sphere\",\n"                               +
                "  DATUM[\"Sphere\",\n"                              +
                "    SPHEROID[\"Sphere\", 6370997.0, 0.0],\n"        +
                "    TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]],\n" +
                "  PRIMEM[\"Greenwich\", 0.0],\n"                    +
                "  UNIT[\"degree\", 0.017453292519943295],\n"        +
                "  AXIS[\"Longitude\", EAST],\n"                     +
                "  AXIS[\"Latitude\", NORTH]]\n");

        final CoordinateOperation operation = opFactory.createOperation(sourceCRS, targetCRS);
        assertEquals(sourceCRS, operation.getSourceCRS());
        assertEquals(targetCRS, operation.getTargetCRS());

        if (true) return;
        final ParameterValueGroup param = null;
        assertEquals("semi_major",     6370997.0, param.parameter("semi_major"        ).doubleValue(), 1E-5);
        assertEquals("semi_minor",     6370997.0, param.parameter("semi_minor"        ).doubleValue(), 1E-5);
        assertEquals("latitude_of_origin",  50.0, param.parameter("latitude_of_origin").doubleValue(), 1E-8);
        assertEquals("central_meridian",   170.0, param.parameter("central_meridian"  ).doubleValue(), 1E-8);
        assertEquals("scale_factor",        0.95, param.parameter("scale_factor"      ).doubleValue(), 1E-8);
        assertEquals("false_easting",        0.0, param.parameter("false_easting"     ).doubleValue(), 1E-8);
        assertEquals("false_northing",       0.0, param.parameter("false_northing"    ).doubleValue(), 1E-8);
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

        final String Z =
                "VERT_CS[\"mean sea level height\",\n"                                    +
                "  VERT_DATUM[\"Mean Sea Level\", 2005, AUTHORITY[\"EPSG\",\"5100\"]],\n" +
                "  UNIT[\"metre\", 1, AUTHORITY[\"EPSG\",\"9001\"]],\n"                   +
                "  AXIS[\"Z\",UP], AUTHORITY[\"EPSG\",\"5714\"]]\n";

        final String WGS84_Z = "COMPD_CS[\"Wgs84 with sea-level Z\","+WGS84+","+Z+"]";
        final String NAD27_Z = "COMPD_CS[\"NAD27 with sea-level Z\","+NAD27+","+Z+"]";
        final String Z_NAD27 = "COMPD_CS[\"sea-level Z with NAD27\","+Z+","+NAD27+"]";

        CoordinateReferenceSystem sourceCRS, targetCRS;
        CoordinateOperation op;
        MathTransform mt;

        sourceCRS = crsFactory.createFromWKT(NAD27);
        targetCRS = crsFactory.createFromWKT(WGS84);
        op = opFactory.createOperation(sourceCRS, targetCRS);
        mt = op.getMathTransform();
        assertFalse(op instanceof PassThroughOperation);
        assertFalse(mt.isIdentity());

        sourceCRS = crsFactory.createFromWKT(Z);
        targetCRS = crsFactory.createFromWKT(Z);
        op = opFactory.createOperation(sourceCRS, targetCRS);
        mt = op.getMathTransform();
        assertFalse(op instanceof PassThroughOperation);
        assertTrue (mt.isIdentity());

        if (true) return;
        sourceCRS = crsFactory.createFromWKT(NAD27_Z);
        targetCRS = crsFactory.createFromWKT(WGS84_Z);
        op = opFactory.createOperation(sourceCRS, targetCRS);
        mt = op.getMathTransform();
        assertTrue(op instanceof PassThroughOperation);
        assertFalse(mt.isIdentity());
        
System.out.println(mt.toWKT());
    }
}
