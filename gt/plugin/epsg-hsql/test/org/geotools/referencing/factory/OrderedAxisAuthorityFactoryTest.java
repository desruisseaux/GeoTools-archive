/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Set;
import java.util.Locale;
import java.util.Iterator;
import java.util.logging.Level;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.resources.Arguments;
import org.geotools.util.MonolineFormatter;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.factory.epsg.LongitudeFirstFactory;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.factory.FactoryRegistryException;


/**
 * Tests the usage of {@link OrderedAxisAuthorityFactory} with the help of the
 * EPSG database. Any EPSG plugin should fit. However, this test live in the
 * {@code plugin/epsg-hsql} module since the HSQL plugin is the only one which
 * is garantee to work on any machine running Maven.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 */
public class OrderedAxisAuthorityFactoryTest extends TestCase {
    /**
     * {@code true} if metadata (especially identifiers) should be erased, or {@code false} if
     * they should be kepts. The {@code true} value matches the pre GEOT-854 state, while the
     * {@code false} value mathes the post GEOT-854 state.
     *
     * @see http://jira.codehaus.org/browse/GEOT-854
     */
    private static final boolean METADATA_ERASED = false;

    /**
     * For safety.
     */
    private static final String EPSG = "EPSG";

    /**
     * {@code true} for tracing operations on the standard output.
     */
    private static boolean verbose;

    /**
     * Run the suite from the command line. If {@code "-log"} flag is specified on the
     * command-line, then the logger will be set to {@link Level#CONFIG}. This is usefull
     * for tracking down which data source is actually used.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        final boolean log = arguments.getFlag("-log");
        verbose = arguments.getFlag("-verbose");
        arguments.getRemainingArguments(0);
        MonolineFormatter.initGeotools(log ? Level.CONFIG : null);
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(OrderedAxisAuthorityFactoryTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public OrderedAxisAuthorityFactoryTest(final String name) {
        super(name);
    }

    /**
     * Returns the ordered axis factory for the specified set of hints.
     */
    private static OrderedAxisAuthorityFactory getFactory(final Hints hints) {
        CRSAuthorityFactory factory;
        factory = FactoryFinder.getCRSAuthorityFactory(EPSG, hints);
        assertTrue(factory instanceof LongitudeFirstFactory);
        factory = (CRSAuthorityFactory) ((LongitudeFirstFactory) factory).getImplementationHints()
                   .get(Hints.CRS_AUTHORITY_FACTORY);
        assertTrue(factory instanceof OrderedAxisAuthorityFactory);
        return (OrderedAxisAuthorityFactory) factory;
    }

    /**
     * Tests the registration of the various flavor of {@link OrderedAxisAuthorityFactoryTest}
     * for the EPSG authority factory.
     */
    public void testRegistration() {
        final Hints  hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        OrderedAxisAuthorityFactory factory;
        factory = getFactory(hints);
        assertTrue(factory.forceStandardDirections);
        assertTrue(factory.forceStandardUnits);

        hints.put(Hints.FORCE_STANDARD_AXIS_DIRECTIONS, Boolean.TRUE);
        assertSame(factory, getFactory(hints));
        assertTrue(factory.forceStandardDirections);
        assertTrue(factory.forceStandardUnits);

        hints.put(Hints.FORCE_STANDARD_AXIS_UNITS, Boolean.TRUE);
        assertSame(factory, getFactory(hints));
        assertTrue(factory.forceStandardDirections);
        assertTrue(factory.forceStandardUnits);

        hints.put(Hints.FORCE_STANDARD_AXIS_UNITS, Boolean.FALSE);
        factory = getFactory(hints);
        assertTrue (factory.forceStandardDirections);
        assertFalse(factory.forceStandardUnits);

        hints.put(Hints.FORCE_STANDARD_AXIS_DIRECTIONS, Boolean.FALSE);
        factory = getFactory(hints);
        assertFalse(factory.forceStandardDirections);
        assertFalse(factory.forceStandardUnits);

        hints.put(Hints.FORCE_STANDARD_AXIS_UNITS, Boolean.TRUE);
        factory = getFactory(hints);
        assertFalse(factory.forceStandardDirections);
        assertTrue (factory.forceStandardUnits);
    }

    /**
     * Tests the axis reordering.
     */
    public void testAxisReordering() throws FactoryException {
        /*
         * Tests the OrderedAxisAuthorityFactory creating using FactoryFinder. The following
         * conditions are not tested directly, but are required in order to get the test to
         * succeed:
         *
         *    - EPSG factories must be provided for both "official" and "modified" axis order.
         *    - The "official" axis order must have precedence over the modified one.
         *    - The hints are correctly understood by FactoryFinder.
         */
        final AbstractAuthorityFactory factory0, factory1;
        final Hints hints = new Hints(Hints.CRS_AUTHORITY_FACTORY, AbstractAuthorityFactory.class);
        factory0 = (AbstractAuthorityFactory) FactoryFinder.getCRSAuthorityFactory(EPSG, hints);
        assertFalse(factory0 instanceof OrderedAxisAuthorityFactory);
        hints.put(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        hints.put(Hints.FORCE_STANDARD_AXIS_DIRECTIONS,   Boolean.TRUE);
        hints.put(Hints.FORCE_STANDARD_AXIS_UNITS,        Boolean.TRUE);
        factory1 = (AbstractAuthorityFactory) FactoryFinder.getCRSAuthorityFactory(EPSG, hints);
        /*
         * The local variables to be used for all remaining tests
         * (usefull to setup in the debugger).
         */
        String code;
        CoordinateReferenceSystem crs0, crs1;
        CoordinateOperationFactory opFactory = FactoryFinder.getCoordinateOperationFactory(null);
        MathTransform mt;
        Matrix matrix;
        /*
         * Tests a WGS84 geographic CRS (2D) with (NORTH, EAST) axis directions.
         * The factory should reorder the axis with no more operation than an axis swap.
         */
        code = "4326";
        crs0 = factory0.createCoordinateReferenceSystem(code);
        crs1 = factory1.createCoordinateReferenceSystem(code);
        final CoordinateReferenceSystem cacheTest = crs1;
        assertNotSame(crs0, crs1);
        assertNotSame(crs0.getCoordinateSystem(), crs1.getCoordinateSystem());
        assertSame(((SingleCRS) crs0).getDatum(), ((SingleCRS) crs1).getDatum());
        assertFalse(crs0.getIdentifiers().isEmpty());
        if (METADATA_ERASED) {
            assertTrue(crs1.getIdentifiers().isEmpty());
        } else {
            assertEquals(crs0.getIdentifiers(), crs1.getIdentifiers());
        }
        mt = opFactory.createOperation(crs0, crs1).getMathTransform();
        assertFalse(mt.isIdentity());
        assertTrue(mt instanceof LinearTransform);
        matrix = ((LinearTransform) mt).getMatrix();
        assertEquals(new GeneralMatrix(new double[][] {
            {0, 1, 0},
            {1, 0, 0},
            {0, 0, 1}}), new GeneralMatrix(matrix));
        /*
         * Tests a WGS84 geographic CRS (3D) with (NORTH, EAST, UP) axis directions.
         * Because this CRS uses sexagesimal units, conversions are not supported and
         * will not be tested.
         */
        code = "4329";
        crs0 = factory0.createCoordinateReferenceSystem(code);
        crs1 = factory1.createCoordinateReferenceSystem(code);
        assertNotSame(crs0, crs1);
        assertNotSame(crs0.getCoordinateSystem(), crs1.getCoordinateSystem());
        assertSame(((SingleCRS) crs0).getDatum(), ((SingleCRS) crs1).getDatum());
        assertFalse(crs0.getIdentifiers().isEmpty());
        if (METADATA_ERASED) {
            assertTrue(crs1.getIdentifiers().isEmpty());
        } else {
            assertEquals(crs0.getIdentifiers(), crs1.getIdentifiers());
        }
        /*
         * Tests a WGS84 geographic CRS (3D) with (NORTH, EAST, UP) axis directions.
         * The factory should reorder the axis with no more operation than an axis swap.
         */
        code = "63266413";
        crs0 = factory0.createCoordinateReferenceSystem(code);
        crs1 = factory1.createCoordinateReferenceSystem(code);
        assertNotSame(crs0, crs1);
        assertNotSame(crs0.getCoordinateSystem(), crs1.getCoordinateSystem());
        assertSame(((SingleCRS) crs0).getDatum(), ((SingleCRS) crs1).getDatum());
        assertFalse(crs0.getIdentifiers().isEmpty());
        if (METADATA_ERASED) {
            assertTrue(crs1.getIdentifiers().isEmpty());
        } else {
            assertEquals(crs0.getIdentifiers(), crs1.getIdentifiers());
        }
        mt = opFactory.createOperation(crs0, crs1).getMathTransform();
        assertFalse(mt.isIdentity());
        assertTrue(mt instanceof LinearTransform);
        matrix = ((LinearTransform) mt).getMatrix();
        assertEquals(new GeneralMatrix(new double[][] {
            {0, 1, 0, 0},
            {1, 0, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}}), new GeneralMatrix(matrix));
        /*
         * Tests a projected CRS with (EAST, NORTH) axis orientation. No axis reordering is needed,
         * which means that their coordinate systems are identical and the math transform should be
         * the identity one. Note that while no axis swap is needed, the base GeographicCRS are not
         * the same since an axis reordering has been done there.
         */
        code = "2027";
        crs0 = factory0.createCoordinateReferenceSystem(code);
        crs1 = factory1.createCoordinateReferenceSystem(code);
        assertNotSame(crs0, crs1);
        assertSame(crs0.getCoordinateSystem(), crs1.getCoordinateSystem());
        assertSame(((SingleCRS) crs0).getDatum(), ((SingleCRS) crs1).getDatum());
        assertNotSame(((ProjectedCRS) crs0).getBaseCRS(), ((ProjectedCRS) crs1).getBaseCRS());
        assertFalse(crs0.getIdentifiers().isEmpty());
        if (METADATA_ERASED) {
            assertTrue(crs1.getIdentifiers().isEmpty());
        } else {
            assertEquals(crs0.getIdentifiers(), crs1.getIdentifiers());
        }
        mt = opFactory.createOperation(crs0, crs1).getMathTransform();
        assertTrue(mt.isIdentity());
        /*
         * Tests a projected CRS with (WEST, SOUTH) axis orientation.
         * The factory should arrange the axis with no more operation than a direction change.
         * While the end result is a matrix like the GeographicCRS case, the path that lead to
         * this result is much more complex.
         */
        code = "22275";
        crs0 = factory0.createCoordinateReferenceSystem(code);
        crs1 = factory1.createCoordinateReferenceSystem(code);
        assertNotSame(crs0, crs1);
        assertNotSame(crs0.getCoordinateSystem(), crs1.getCoordinateSystem());
        assertSame(((SingleCRS) crs0).getDatum(), ((SingleCRS) crs1).getDatum());
        assertFalse(crs0.getIdentifiers().isEmpty());
        if (METADATA_ERASED) {
            assertTrue(crs1.getIdentifiers().isEmpty());
        } else {
            assertEquals(crs0.getIdentifiers(), crs1.getIdentifiers());
        }
        mt = opFactory.createOperation(crs0, crs1).getMathTransform();
        assertFalse(mt.isIdentity());
        assertTrue(mt instanceof LinearTransform);
        matrix = ((LinearTransform) mt).getMatrix();
        assertEquals(new GeneralMatrix(new double[][] {
            {-1,  0,  0},
            { 0, -1,  0},
            { 0,  0,  1}}), new GeneralMatrix(matrix));
        /*
         * Tests the cache.
         */
        assertSame(cacheTest, factory1.createCoordinateReferenceSystem("4326"));
    }

    // -------------------------------------------------------------------------
    // The following tests are copied from the legacy plugin/epsg-wkt test suite
    // -------------------------------------------------------------------------

    /**
     * Makes sure that the authority factory has the proper name.
     */
    public void testAuthority() {
        CRSAuthorityFactory factory;
        Citation authority;

        // Tests the official factory.
        factory   = FactoryFinder.getCRSAuthorityFactory(EPSG, null);
        authority = factory.getAuthority();
        assertNotNull(authority);
        assertEquals("European Petroleum Survey Group", authority.getTitle().toString(Locale.US));
        assertTrue(authority.getIdentifiers().contains(EPSG));

        // Tests the modified factory.
        factory   = new OrderedAxisAuthorityFactory(EPSG, null, null);
        authority = factory.getAuthority();
        assertNotNull(authority);
        assertTrue(authority.getIdentifiers().contains(EPSG));
    }

    /**
     * Tests the vendor name.
     */
    public void testVendor() {
        CRSAuthorityFactory factory;
        Citation vendor;

        factory = new OrderedAxisAuthorityFactory(EPSG, null, null);
        vendor  = factory.getVendor();
        assertNotNull(vendor);
        assertEquals("Geotools", vendor.getTitle().toString(Locale.US));
        assertFalse(vendor.getIdentifiers().contains(EPSG));
    }

    /**
     * Tests the amount of codes available.
     */
    public void testCodes() throws FactoryException {
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory(EPSG, null, null);
        final Set codes = factory.getAuthorityCodes( CoordinateReferenceSystem.class );
        assertNotNull(codes);
        assertTrue(codes.size() >= 3000);
    }

    /**
     * A random CRS for fun.
     */
    public void test26910() throws FactoryException {
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory(EPSG, null, null);
        final CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:26910");
        assertNotNull(crs);
        assertSame(crs, factory.createObject("EPSG:26910"));
    }

    /**
     * UDIG requires this to work.
     */
    public void test4326() throws FactoryException {
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory(EPSG, null, null);
        final CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:4326");
        assertNotNull(crs);
        assertSame(crs, factory.createObject("EPSG:4326"));
    }

    /**
     * UDIG requires this to work.
     */
    public void test4269() throws FactoryException {
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory(EPSG, null, null);
        final CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:4269");
        assertNotNull(crs);
        assertSame(crs, factory.createObject("EPSG:4269"));
    }

    /**
     * UDIG requires this to work.
     */
    public void test42102() throws FactoryException {
        if (true) {
            // TODO: not yet implemented: this CRS doesn't exists in the EPSG database.
            return;
        }
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory(EPSG, null, null);
        final CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:42102");
        assertNotNull(crs);
        assertNotNull(crs.getIdentifiers());
        assertFalse(crs.getIdentifiers().isEmpty());
        NamedIdentifier expected = new NamedIdentifier(Citations.EPSG, "42102");
        assertTrue(crs.getIdentifiers().contains(expected));
    }

    /**
     * Tests the number of CRS that can be created. This test will be executed only if this test
     * suite is run with the {@code -verbose} option provided on the command line.
     */
    public void testSuccess() throws FactoryException {
        if (!verbose) {
            return;
        }
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory(EPSG, null, null);
        Set codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
        int total = codes.size();
        int count = 0;
        for (Iterator i=codes.iterator(); i.hasNext();) {
            CoordinateReferenceSystem crs;
            String code = (String) i.next();
            try {
                crs = factory.createCoordinateReferenceSystem(code);
                assertNotNull(crs);
                count++;
            } catch (FactoryException e) {
                System.err.println("WARNING (CRS: "+code+" ):" + e.getMessage());
            }            
        }
        System.out.println("Success: " + count + "/" + total + " (" + (count*100)/total + "%)");
    }

    /**
     * A random CRS for fun.
     */
    public void test26910Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26910");
        assertNotNull(crs);                
    }

    /**
     * A random CRS for fun.
     */
    public void test26986Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26986");
        assertNotNull(crs);                
    }

    /**
     * WFS requires this to work.
     */
    public void test4326Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:4326");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test26742Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26742");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test4269Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:4269");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test42304Lower() throws FactoryException {
        if (true) {
            // TODO: not yet implemented: this CRS doesn't exists in the EPSG database.
            return;
        }
        CoordinateReferenceSystem crs = CRS.decode("epsg:42304");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test42102Lower() throws FactoryException {
        if (true) {
            // TODO: not yet implemented: this CRS doesn't exists in the EPSG database.
            return;
        }
        CoordinateReferenceSystem crs = CRS.decode("epsg:42102");
        assertNotNull(crs);
        assertNotNull(crs.getIdentifiers());
        assertFalse(crs.getIdentifiers().isEmpty());
        NamedIdentifier expected = new NamedIdentifier(Citations.EPSG, "42102");
        assertTrue(crs.getIdentifiers().contains(expected));
    }
}
