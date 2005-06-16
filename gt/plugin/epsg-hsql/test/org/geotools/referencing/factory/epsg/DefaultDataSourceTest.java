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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
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
import org.opengis.metadata.Identifier;
import org.opengis.referencing.IdentifiedObject;
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
import org.geotools.factory.Hints;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.factory.epsg.DefaultFactory;
import org.geotools.util.MonolineFormatter;
import org.geotools.resources.Arguments;


/**
 * Tests transformations from CRS and/or operations created from the EPSG factory, using
 * the default plugin. If the MS-Access database is installed and the {@code epsg-access}
 * plugin is in the classpath, then the default plugin will be the factory backed by the
 * MS-Access database. Otherwise, the default will probably be the one backed by the HSQL
 * database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Vadim Semenov
 */
public class DefaultDataSourceTest extends TestCase {
    /**
     * The EPSG factory to test.
     */
    DefaultFactory factory;

    /**
     * {@code true} if {@link #setUp} has been invoked at least once and failed to make a
     * connection. This flag is used in order to log a warning only once and avoid any new
     * useless tentative to get a connection.
     */
    static boolean noConnection;

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
        return new TestSuite(DefaultDataSourceTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public DefaultDataSourceTest(final String name) {
        super(name);
    }

    /**
     * Sets up the authority factory, or lets it to null if the initialisation failed.
     * In the last case, a warning will be logged but no test will be performed. We will
     * not throws an exception for peoples who don't have an EPSG database on their machine.
     */
    protected void setUp() throws SQLException {
        if (noConnection) {
            // This method was already invoked before and failed.
            // Do not try again.
            return;
        }
        boolean isReady = false;
        try {
            factory = (DefaultFactory) FactoryFinder.getCRSAuthorityFactory("EPSG",
                        new Hints(Hints.CRS_AUTHORITY_FACTORY, DefaultFactory.class));
            isReady = factory.isReady();
        } catch (Throwable error) {
            factory = null;
            noConnection = true;
            final LogRecord record = new LogRecord(Level.WARNING,
                    "An error occured while setting up the date source for the EPSG database.\n" +
                    "Maybe there is no JDBC-ODBC bridge for the current platform.\n" +
                    "No test will be performed for this class.");
            record.setSourceClassName(DefaultDataSourceTest.class.getName());
            record.setSourceMethodName("setUp");
            record.setThrown(error);
            Logger.getLogger("org.geotools.referencing").log(record);
            return;
        }
        if (!isReady) {
            factory = null;
            noConnection = true;
            Logger.getLogger("org.geotools.referencing").warning(
                "Failed to connect to the EPSG authority factory.\n" +
                "This is a normal failure when no EPSG database is available on the current machine.\n" +
                "No test will be performed for this class.");
        }
        if (factory.getDataSource() instanceof HSQLDataSource) {
            factory = null;
            noConnection = true;
            Logger.getLogger("org.geotools.referencing").info(
                "No data source other than HSQL found.\n" +
                "Skip this suite, since HSQL data source will be tested by an other suite.");
        }
    }

    /**
     * Release any resources holds by the EPSG factory.
     * Note: the shutdown is performed in a separated thread because the thread
     * name is used by HSQL as a flag meaning to stop the database process.
     */
    protected void tearDown() throws InterruptedException {
        if (factory != null) {
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
    }

    /**
     * Returns the first identifier for the specified object.
     */
    private static String getIdentifier(final IdentifiedObject object) {
        return ((Identifier) object.getIdentifiers().iterator().next()).getCode();
    }

    /**
     * Tests the creation of CRS using name instead of primary keys.
     */
    public void testNameUsage() throws FactoryException {
        if (factory == null) return;
        final CoordinateReferenceSystem primary, byName;
        primary = factory.createCoordinateReferenceSystem("27581");
        assertEquals("27581", getIdentifier(primary));
        assertTrue(primary instanceof ProjectedCRS);
        assertEquals(2, primary.getCoordinateSystem().getDimension());

        byName = factory.createCoordinateReferenceSystem("NTF (Paris) / France I");
        assertEquals(primary, byName);
    }

    /**
     * Tests creations.
     */
    public void testCreation() throws FactoryException {
        if (factory == null) return;
        final CoordinateOperationFactory opf = FactoryFinder.getCoordinateOperationFactory(null);
        CoordinateReferenceSystem sourceCRS, targetCRS;
        CoordinateOperation operation;
        
        sourceCRS = factory.createCoordinateReferenceSystem("4274");
        assertEquals("4274", getIdentifier(sourceCRS));
        assertTrue(sourceCRS instanceof GeographicCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:4140");
        assertEquals("4140", getIdentifier(sourceCRS));
        assertTrue(sourceCRS instanceof GeographicCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("2027");
        assertEquals("2027", getIdentifier(sourceCRS));
        assertTrue(sourceCRS instanceof ProjectedCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem(" EPSG : 2442 ");
        assertEquals("2442", getIdentifier(sourceCRS));
        assertTrue(sourceCRS instanceof ProjectedCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:4915");
        assertEquals("4915", getIdentifier(sourceCRS));
        assertTrue(sourceCRS instanceof GeocentricCRS);
        assertEquals(3, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:4993");
        assertEquals("4993", getIdentifier(sourceCRS));
        assertTrue(sourceCRS instanceof GeographicCRS);
        assertEquals(3, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:5735");
        assertEquals("5735", getIdentifier(sourceCRS));
        assertTrue(sourceCRS instanceof VerticalCRS);
        assertEquals(1, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:5801");
        assertEquals("5801", getIdentifier(sourceCRS));
        assertTrue(sourceCRS instanceof EngineeringCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        
        sourceCRS = factory.createCoordinateReferenceSystem("EPSG:7400");
        assertEquals("7400", getIdentifier(sourceCRS));
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
        assertEquals("4273", getIdentifier(factory.createCoordinateReferenceSystem("4273")));
        assertFalse(factory.isConnected());
        // Was not in the cache
        assertEquals("4275", getIdentifier(factory.createCoordinateReferenceSystem("4275")));
        assertTrue(factory.isConnected());
    }

    /**
     * Tests the serialization of many {@link CoordinateOperation} objects.
     */
    public void testSerialization() throws FactoryException, IOException, ClassNotFoundException {
        if (factory == null) return;
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
