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
import java.util.logging.Level;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.resources.Arguments;
import org.geotools.util.MonolineFormatter;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.matrix.GeneralMatrix;


/**
 * Tests the usage of {@link OrderedAxisAuthorityFactory} with the help of the
 * EPSG database. Any EPSG plugin should fit. However, this test live in the
 * {@code plugin/epsg-hsql} module since the HSQL plugin is the only one which
 * is garantee to work on any machine running Maven.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class OrderedAxisAuthorityFactoryTest extends TestCase {
    /**
     * Run the suite from the command line. If {@code "-log"} flag is specified on the
     * command-line, then the logger will be set to {@link Level#CONFIG}. This is usefull
     * for tracking down which data source is actually used.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        final boolean log = arguments.getFlag("-log");
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
     * Tests the axis reordering.
     */
    public void testAxisReordering() throws FactoryException {
        final AbstractAuthorityFactory factory0, factory1;
        factory0 = (AbstractAuthorityFactory) FactoryFinder.getCRSAuthorityFactory("EPSG",
                        new Hints(Hints.CRS_AUTHORITY_FACTORY, AbstractAuthorityFactory.class));
        factory1 = new OrderedAxisAuthorityFactory(factory0);

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
        assertTrue (crs1.getIdentifiers().isEmpty());
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
        assertTrue (crs1.getIdentifiers().isEmpty());
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
        assertTrue (crs1.getIdentifiers().isEmpty());
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
        assertTrue (crs1.getIdentifiers().isEmpty());
        mt = opFactory.createOperation(crs0, crs1).getMathTransform();
        assertTrue(mt.isIdentity());
        /*
         * Tests a projected CRS with (WEST, SOUTH) axis orientation.
         * The factory should reorder the axis with no more operation than an axis swap.
         * While the end result is a matrix like the GeographicCRS case, the path that
         * lead to this result is much more complex.
         */
        code = "22275";
        crs0 = factory0.createCoordinateReferenceSystem(code);
        crs1 = factory1.createCoordinateReferenceSystem(code);
        assertNotSame(crs0, crs1);
        assertNotSame(crs0.getCoordinateSystem(), crs1.getCoordinateSystem());
        assertSame(((SingleCRS) crs0).getDatum(), ((SingleCRS) crs1).getDatum());
        assertFalse(crs0.getIdentifiers().isEmpty());
        assertTrue (crs1.getIdentifiers().isEmpty());
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
}
