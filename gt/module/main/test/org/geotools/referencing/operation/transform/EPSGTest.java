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
package org.geotools.referencing.operation.transform;

// J2SE dependencies
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;

// Geotools dependencies
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.epsg.DefaultFactory;


/**
 * Tests transformations from CRS and/or operations created from the EPSG factory.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EPSGTest extends TestCase {
    /**
     * The EPSG factory.
     */
    private DefaultFactory factory;
    
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.MonolineFormatter.initGeotools();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(EPSGTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public EPSGTest(final String name) {
        super(name);
    }

    /**
     * Sets up the authority factory, or lets it to null if the initialisation failed.
     * In the last case, a warning will be logged but no test will be performed. We will
     * not throws an exception for peoples who don't have an EPSG database on their machine.
     */
    protected void setUp() {
        boolean isReady = false;
        try {
            // Do not rely on FactoryFinder: we rely want to test this implementation,
            // not an arbitrary implementation. The WKT-based factory for instance doesn't
            // have suffisient capabilities for this test.
            factory = new DefaultFactory();
            isReady = factory.isReady();
        } catch (RuntimeException error) {
            factory = null;
            final LogRecord record = new LogRecord(Level.WARNING,
                    "An error occured while setting up the date source for the EPSG database.\n" +
                    "Maybe there is no JDBC-ODBC bridge for the current platform.\n" +
                    "No test will pe performed for this class.");
            record.setSourceClassName(EPSGTest.class.getName());
            record.setSourceMethodName("setUp");
            record.setThrown(error);
            Logger.getLogger("org.geotools.referencing").log(record);
            return;
        }
        if (!isReady) {
            factory = null;
            Logger.getLogger("org.geotools.referencing").warning(
                "Failed to connect to the EPSG authority factory.\n" +
                "This is a normal failure when no EPSG database is available on the current machine.\n" +
                "No test will pe performed for this class.");
        }
    }

    /**
     * Release any resources holds by the EPSG factory.
     */
    protected void tearDown() throws FactoryException {
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
    }

    /**
     * Tests creations.
     */
    public void testCreation() throws FactoryException {
        if (factory == null) return;
        final CoordinateOperationFactory opf = FactoryFinder.getCoordinateOperationFactory();
        CoordinateReferenceSystem sourceCRS, targetCRS;
        CoordinateOperation operation;
        
        sourceCRS = factory.createCoordinateReferenceSystem("4274");
        assertEquals("4274", sourceCRS.getIdentifiers()[0].getCode());
        assertTrue(sourceCRS instanceof GeographicCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:4140");
        assertEquals("4140", sourceCRS.getIdentifiers()[0].getCode());
        assertTrue(sourceCRS instanceof GeographicCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("2027");
        assertEquals("2027", sourceCRS.getIdentifiers()[0].getCode());
        assertTrue(sourceCRS instanceof ProjectedCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem(" EPSG : 2442 ");
        assertEquals("2442", sourceCRS.getIdentifiers()[0].getCode());
        assertTrue(sourceCRS instanceof ProjectedCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:4915");
        assertEquals("4915", sourceCRS.getIdentifiers()[0].getCode());
        assertTrue(sourceCRS instanceof GeocentricCRS);
        assertEquals(3, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:4993");
        assertEquals("4993", sourceCRS.getIdentifiers()[0].getCode());
        assertTrue(sourceCRS instanceof GeographicCRS);
        assertEquals(3, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:5735");
        assertEquals("5735", sourceCRS.getIdentifiers()[0].getCode());
        assertTrue(sourceCRS instanceof VerticalCRS);
        assertEquals(1, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:5801");
        assertEquals("5801", sourceCRS.getIdentifiers()[0].getCode());
        assertTrue(sourceCRS instanceof EngineeringCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:7400");
        assertEquals("7400", sourceCRS.getIdentifiers()[0].getCode());
        assertTrue(sourceCRS instanceof CompoundCRS);
        assertEquals(3, sourceCRS.getCoordinateSystem().getDimension());

        sourceCRS = factory.createCoordinateReferenceSystem("4273");
        targetCRS = factory.createCoordinateReferenceSystem("4979");
        operation = opf.createOperation(sourceCRS, targetCRS);
        assertNotSame(sourceCRS, targetCRS);
        assertFalse(operation.getMathTransform().isIdentity());

        assertSame(sourceCRS, factory.createCoordinateReferenceSystem("EPSG:4273"));
        assertSame(targetCRS, factory.createCoordinateReferenceSystem("EPSG:4979"));

        assertSame(sourceCRS, factory.createCoordinateReferenceSystem(" EPSG : 4273 "));
        assertSame(targetCRS, factory.createCoordinateReferenceSystem(" EPSG : 4979 "));

        /*
         * Test closing the factory after the timeout.
         */
        factory.setTimeout(200);
        try {
            assertTrue(factory.isConnected());
            Thread.currentThread().sleep(500);
            assertFalse(factory.isConnected());
        } catch (InterruptedException e) {
            fail(e.getLocalizedMessage());
        }
        assertFalse(factory.isConnected());
        // Should be in the cache.
        assertEquals("4273", factory.createCoordinateReferenceSystem("4273").getIdentifiers()[0].getCode());
        assertFalse(factory.isConnected());
        // Was not in the cache
        assertEquals("4275", factory.createCoordinateReferenceSystem("4275").getIdentifiers()[0].getCode());
        assertTrue(factory.isConnected());
    }
}
