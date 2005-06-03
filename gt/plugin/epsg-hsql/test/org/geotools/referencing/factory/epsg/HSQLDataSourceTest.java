/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
package org.geotools.referencing.factory.epsg;

// J2SE dependencies
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

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
 * Tests transformations from CRS and/or operations created from the EPSG factory,
 * using HSQL plugin.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Vadim Semenov
 */
public class HSQLDataSourceTest extends TestCase {
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
        return new TestSuite(HSQLDataSourceTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public HSQLDataSourceTest(final String name) {
        super(name);
    }

    /**
     * Sets up the authority factory.
     */
    protected void setUp() throws SQLException {
        // Do not rely on FactoryFinder: we rely want to test this implementation,
        // not an arbitrary implementation. The WKT-based factory for instance doesn't
        // have suffisient capabilities for this test.
        factory = new DefaultFactory();
        factory.setDataSource(new HSQLDataSource());
    }

    /**
     * Releases any resources holds by the EPSG factory.
     * Note: the shutdown is performed in a separated thread because the thread
     * name is used by HSQL as a flag meaning to stop the database process.
     */
    protected void tearDown() throws InterruptedException {
        final DefaultFactory f = factory;
        final Thread shutdown = new Thread(FactoryUsingSQL.SHUTDOWN_THREAD) {
            public void run() {
                try {
                    f.dispose();
                } catch (FactoryException exception) {
                    exception.printStackTrace();
                }
            }
        };
        shutdown.start();
        factory = null;
        shutdown.join();
    }

    /**
     * Tests creations.
     */
    public void testCreation() throws FactoryException {
        final CoordinateOperationFactory opf = FactoryFinder.getCoordinateOperationFactory(null);
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

    /**
     * Tests the serialization of many {@link CoordinateOperation} objects.
     */
    public void testSerialization() throws FactoryException, IOException, ClassNotFoundException {
        CoordinateReferenceSystem crs1 = factory.createCoordinateReferenceSystem("4326");
        CoordinateReferenceSystem crs2 = factory.createCoordinateReferenceSystem("4322");
        CoordinateOperationFactory opf = FactoryFinder.getCoordinateOperationFactory(null);
        CoordinateOperation cop = opf.createOperation(crs1, crs2);
        serialize(cop);

        crs1 = crs2 = null;
        final String crs1_name  = "4326";
        final int crs2_ranges[] = {4326,  4326,
                                   4322,  4322,
                                   4269,  4269,
                                   4267,  4267,
                                   4230,  4230,
                                  32601, 32660,
                                  32701, 32760,
                                   2759,  2930};

        for (int irange=0; irange<crs2_ranges.length; irange+=2) {
            int range_start = crs2_ranges[irange  ];
            int range_end   = crs2_ranges[irange+1];
            for (int isystem2=range_start; isystem2<=range_end; isystem2++) {
                if (crs1 == null) {
                    crs1 = factory.createCoordinateReferenceSystem(crs1_name);
                }
                String crs2_name = Integer.toString(isystem2);
                crs2 = factory.createCoordinateReferenceSystem(crs2_name);
                cop = opf.createOperation(crs1, crs2);
                serialize(cop);
            }
        }
    }

    /**
     * Tests the serialization of the specified object.
     */
    private static void serialize(final Object object) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(object);
        out.close();
        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        final Object read = in.readObject();
        in.close();
        assertEquals(object,            read);
        assertEquals(object.hashCode(), read.hashCode());
    }
}
