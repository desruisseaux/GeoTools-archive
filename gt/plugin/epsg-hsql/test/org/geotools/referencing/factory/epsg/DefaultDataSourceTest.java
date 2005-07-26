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

// J2SE dependencies and extensions
import java.util.Set;
import java.util.Locale;
import java.util.Iterator;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import javax.units.Unit;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.Transformation;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.factory.epsg.DefaultFactory;
import org.geotools.util.MonolineFormatter;
import org.geotools.resources.CRSUtilities;
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
     * Small value for parameter value comparaisons.
     */
    private static final double EPS = 1E-6;

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
            return;
        }
        if (factory.getDataSource() instanceof HSQLDataSource) {
            factory = null;
            noConnection = true;
            Logger.getLogger("org.geotools.referencing").info(
                "No data source other than HSQL found.\n" +
                "Skip this suite, since HSQL data source will be tested by an other suite.");
            return;
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
     * Tests the {@code getAuthorityCodes()} method.
     */
    public void testAuthorityCodes() throws FactoryException {
        if (factory == null) return;

        final Set crs = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
        assertFalse(crs.isEmpty());
        assertTrue (crs.size() > 0);
        assertEquals("Check size() consistency", crs.size(), crs.size());

        final Set geographicCRS = factory.getAuthorityCodes(GeographicCRS.class);
        assertFalse(geographicCRS.isEmpty());
        assertTrue (geographicCRS.size() > 0);
        assertTrue (geographicCRS.size() < crs.size());
        assertFalse(geographicCRS.containsAll(crs));
        assertTrue (crs.containsAll(geographicCRS));

        final Set projectedCRS = factory.getAuthorityCodes(ProjectedCRS.class);
        assertFalse(projectedCRS.isEmpty());
        assertTrue (projectedCRS.size() > 0);
        assertTrue (projectedCRS.size() < crs.size());
        assertFalse(projectedCRS.containsAll(crs));
        assertTrue (crs.containsAll(projectedCRS));
//        assertTrue(Collections.disjoint(geographicCRS, projectedCRS));
        // TODO: uncomment when we will be allowed to compile for J2SE 1.5.

        final Set datum = factory.getAuthorityCodes(Datum.class);
        assertFalse(datum.isEmpty());
        assertTrue (datum.size() > 0);
//        assertTrue(Collections.disjoint(datum, crs));
        // TODO: uncomment when we will be allowed to compile for J2SE 1.5.

        final Set geodeticDatum = factory.getAuthorityCodes(GeodeticDatum.class);
        assertFalse(geodeticDatum.isEmpty());
        assertTrue (geodeticDatum.size() > 0);
        assertFalse(geodeticDatum.containsAll(datum));
        assertTrue (datum.containsAll(geodeticDatum));

        // Ensures that the factory keept the set in its cache.
        assertSame(crs,           factory.getAuthorityCodes(CoordinateReferenceSystem.class));
        assertSame(geographicCRS, factory.getAuthorityCodes(            GeographicCRS.class));
        assertSame(projectedCRS,  factory.getAuthorityCodes(             ProjectedCRS.class));
        assertSame(datum,         factory.getAuthorityCodes(                    Datum.class));
        assertSame(geodeticDatum, factory.getAuthorityCodes(            GeodeticDatum.class));
        assertSame(geodeticDatum, factory.getAuthorityCodes(     DefaultGeodeticDatum.class));

        // Try a dummy type.
        assertTrue("Dummy type", factory.getAuthorityCodes(String.class).isEmpty());

        // Tests projections, which are handle in a special way.
        final Set operations      = factory.getAuthorityCodes(Operation     .class);
        final Set conversions     = factory.getAuthorityCodes(Conversion    .class);
        final Set projections     = factory.getAuthorityCodes(Projection    .class);
        final Set transformations = factory.getAuthorityCodes(Transformation.class);

        assertTrue (conversions    .size() < operations .size());
        assertTrue (projections    .size() < operations .size());
        assertTrue (transformations.size() < operations .size());
        assertTrue (projections    .size() < conversions.size());

        assertFalse(projections.containsAll(conversions));
        assertTrue (conversions.containsAll(projections));
        assertTrue (operations .containsAll(conversions));
        assertTrue (operations .containsAll(transformations));

        // TODO: uncomment when we will be allowed to compile for J2SE 1.5.
//        assertTrue (Collections.disjoint(conversions, transformations));
//        assertFalse(Collections.disjoint(conversions, projections));

        assertFalse(operations     .isEmpty());
        assertFalse(conversions    .isEmpty());
        assertFalse(projections    .isEmpty());
        assertFalse(transformations.isEmpty());

        assertTrue (conversions.contains("101"));
        assertFalse(projections.contains("101"));
        assertTrue (projections.contains("16001"));

        final Set units = factory.getAuthorityCodes(Unit.class);
        assertFalse(units.isEmpty());
        assertTrue (units.size() > 0);
    }

    /**
     * Tests the {@link AuthorityFactory#getDescriptionText} method.
     */
    public void testDescriptionText() throws FactoryException {
        assertEquals("World Geodetic System 1984", factory.getDescriptionText( "6326").toString(Locale.ENGLISH));
        assertEquals("Mean Sea Level",             factory.getDescriptionText( "5100").toString(Locale.ENGLISH));
        assertEquals("NTF (Paris) / Nord France",  factory.getDescriptionText("27591").toString(Locale.ENGLISH));
        assertEquals("Ellipsoidal height",         factory.getDescriptionText(   "84").toString(Locale.ENGLISH));
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
        /*
         * Tests unit
         */
        assertSame   (factory.createUnit("9002"), factory.createUnit("foot"));
        assertNotSame(factory.createUnit("9001"), factory.createUnit("foot"));
        /*
         * Tests CRS
         */
        final CoordinateReferenceSystem primary, byName;
        primary = factory.createCoordinateReferenceSystem("27581");
        assertEquals("27581", getIdentifier(primary));
        assertTrue(primary instanceof ProjectedCRS);
        assertEquals(2, primary.getCoordinateSystem().getDimension());
        /*
         * Gets the CRS by name. It should be the same.
         */
        byName = factory.createCoordinateReferenceSystem("NTF (Paris) / France I");
        assertEquals(primary, byName);
        /*
         * Gets the CRS using 'createObject'. It will requires ony more
         * SQL statement internally in order to determines the object type.
         */
        factory.dispose(); // Clear the cache. This is not a real disposal.
        assertEquals(primary, factory.createObject("27581"));
        assertEquals(byName,  factory.createObject("NTF (Paris) / France I"));
        /*
         * Tests descriptions.
         */
        assertEquals("NTF (Paris) / France I", factory.getDescriptionText("27581").toString());
        /*
         * Tests fetching an object with name containing semi-colon.
         */
        final IdentifiedObject cs = factory.createCoordinateSystem(
                "Ellipsoidal 2D CS. Axes: latitude, longitude. Orientations: north, east.  UoM: DMS");
        assertEquals("6411", getIdentifier(cs));
    }

    /**
     * Tests creations of CRS objects.
     */
    public void testCreation() throws FactoryException {
        if (factory == null) return;
        final CoordinateOperationFactory opf = FactoryFinder.getCoordinateOperationFactory(null);
        CoordinateReferenceSystem sourceCRS, targetCRS;
        CoordinateOperation operation;
        ParameterValueGroup parameters;
        
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
        parameters = ((ProjectedCRS) sourceCRS).getConversionFromBase().getParameterValues();
        assertEquals(   -93, parameters.parameter("central_meridian"  ).doubleValue(), EPS);
        assertEquals(     0, parameters.parameter("latitude_of_origin").doubleValue(), EPS);
        assertEquals(0.9996, parameters.parameter("scale_factor"      ).doubleValue(), EPS);
        assertEquals(500000, parameters.parameter("false_easting"     ).doubleValue(), EPS);
        assertEquals(     0, parameters.parameter("false_northing"    ).doubleValue(), EPS);
        
        sourceCRS = factory.createCoordinateReferenceSystem(" EPSG : 2442 ");
        assertEquals("2442", getIdentifier(sourceCRS));
        assertTrue(sourceCRS instanceof ProjectedCRS);
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());
        parameters = ((ProjectedCRS) sourceCRS).getConversionFromBase().getParameterValues();
        assertEquals(   135, parameters.parameter("central_meridian"  ).doubleValue(), EPS);
        assertEquals(     0, parameters.parameter("latitude_of_origin").doubleValue(), EPS);
        assertEquals(     1, parameters.parameter("scale_factor"      ).doubleValue(), EPS);
        assertEquals(500000, parameters.parameter("false_easting"     ).doubleValue(), EPS);
        assertEquals(     0, parameters.parameter("false_northing"    ).doubleValue(), EPS);
        
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

        // GeographicCRS without datum
        sourceCRS = factory.createCoordinateReferenceSystem("63266405");
        assertTrue(sourceCRS instanceof GeographicCRS);
        assertEquals("WGS 84 (deg)", sourceCRS.getName().getCode());
        assertEquals(2, sourceCRS.getCoordinateSystem().getDimension());

        // Operations
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
         * Tests closing the factory after the timeout.
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

    /**
     * Tests the creation of {@link Conversion} objects.
     */
    public void testConversions() throws FactoryException {
        if (factory == null) return;
        /*
         * UTM zone 10N
         */
        final CoordinateOperation operation = factory.createCoordinateOperation("16010");
        assertEquals("16010", getIdentifier(operation));
        assertTrue(operation instanceof Conversion);
        assertNull(operation.getSourceCRS());
        assertNull(operation.getTargetCRS());
        assertNull(operation.getMathTransform());
        /*
         * WGS 72 / UTM zone 10N
         */
        final ProjectedCRS crs = factory.createProjectedCRS("32210");
        final CoordinateOperation projection = crs.getConversionFromBase();
        assertEquals("32210", getIdentifier(crs));
        assertEquals("16010", getIdentifier(projection));
        /*
         * TODO: Current EPSG factory implementation creates Conversion object, not Projection,
         *       because the OperationMethod declared is not one of the build-in ones: it doesn't
         *       have a 'getOperationType()' method. We need to find a fix at some later stage.
         */
//      assertTrue(projection instanceof Projection);
        assertNotNull(projection.getSourceCRS());
        assertNotNull(projection.getTargetCRS());
        assertNotNull(projection.getMathTransform());
        assertNotSame(projection, operation);
        assertSame(((Conversion) operation).getMethod(), ((Conversion) projection).getMethod());
        /*
         * WGS 72BE / UTM zone 10N
         */
        assertFalse(CRSUtilities.equalsIgnoreMetadata(crs, factory.createProjectedCRS("32410")));
        /*
         * Creates a projected CRS from base and projected CRS codes.
         */
        final Set all = factory.createFromCoordinateReferenceSystemCodes("4322", "32210");
        assertEquals(1, all.size());
        assertTrue(all.contains(projection));
    }

    /**
     * Tests the creation of {@link Transformation} objects.
     */
    public void testTransformations() throws FactoryException {
        if (factory == null) return;
        /*
         * Longitude rotation
         */
        assertTrue(factory.createCoordinateOperation("1764") instanceof Transformation);
        /*
         * ED50 (4230)  -->  WGS 84 (4326)  using
         * Geocentric translations (9603).
         * Accuracy = 999
         */
        final CoordinateOperation      operation1 = factory.createCoordinateOperation("1087");
        final CoordinateReferenceSystem sourceCRS = operation1.getSourceCRS();
        final CoordinateReferenceSystem targetCRS = operation1.getTargetCRS();
        final MathTransform             transform = operation1.getMathTransform();
        assertEquals("1087", getIdentifier(operation1));
        assertEquals("4230", getIdentifier(sourceCRS));
        assertEquals("4326", getIdentifier(targetCRS));
        assertTrue   (operation1 instanceof Transformation);
        assertNotSame(sourceCRS, targetCRS);
        assertFalse  (operation1.getMathTransform().isIdentity());
        /*
         * ED50 (4230)  -->  WGS 84 (4326)  using
         * Position Vector 7-param. transformation (9606).
         * Accuracy = 1.5
         */
        final CoordinateOperation operation2 = factory.createCoordinateOperation("1631");
        assertEquals("1631", getIdentifier(operation2));
        assertTrue (operation2 instanceof Transformation);
        assertSame (sourceCRS, operation2.getSourceCRS());
        assertSame (targetCRS, operation2.getTargetCRS());
        assertFalse(operation2.getMathTransform().isIdentity());
        assertFalse(transform.equals(operation2.getMathTransform()));
        /*
         * ED50 (4230)  -->  WGS 84 (4326)  using
         * Coordinate Frame rotation (9607).
         * Accuracy = 1.0
         */
        final CoordinateOperation operation3 = factory.createCoordinateOperation("1989");
        assertEquals("1989", getIdentifier(operation3));
        assertTrue (operation3 instanceof Transformation);
        assertSame (sourceCRS, operation3.getSourceCRS());
        assertSame (targetCRS, operation3.getTargetCRS());
        assertFalse(operation3.getMathTransform().isIdentity());
        assertFalse(transform.equals(operation3.getMathTransform()));
        if (false) {
            System.out.println(operation3);
            System.out.println(operation3.getSourceCRS());
            System.out.println(operation3.getTargetCRS());
            System.out.println(operation3.getMathTransform());
        }
        /*
         * Creates from CRS codes. There is 40 such operations in EPSG version 6.7.
         * The preferred one (according the "supersession" table) is EPSG:1612.
         *
         * Note: the above assertion fails on PostgreSQL because its "ORDER BY" clause put null
         * values last, while Access and HSQL put them first. The PostgreSQL behavior is better
         * for what we want (operations with unknow accuracy last). Unfortunatly, I don't know
         * yet how to instructs Access to put null values last using standard SQL ("IIF" is not
         * standard, and Access doesn't seem to understand "CASE ... THEN" clauses).
         */
        final Set all = factory.createFromCoordinateReferenceSystemCodes("4230", "4326");
        assertTrue(all.size() >= 3);
        assertTrue(all.contains(operation1));
        assertTrue(all.contains(operation2));
        assertTrue(all.contains(operation3));
        final CoordinateOperation first = (CoordinateOperation) all.iterator().next();
        assertEquals("1612", getIdentifier(first)); // see comment above.
        for (final Iterator it=all.iterator(); it.hasNext();) {
            final CoordinateOperation check = (CoordinateOperation) it.next();
            assertSame(sourceCRS, check.getSourceCRS());
            assertSame(targetCRS, check.getTargetCRS());
        }
    }
}
