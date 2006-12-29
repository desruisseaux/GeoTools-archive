/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 * 
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLockException;
import org.geotools.data.FeatureLockFactory;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.InProcessLockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;
import org.geotools.data.postgis.fidmapper.OIDFidMapper;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SimpleFeature;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.function.FilterFunction_geometryType;
import org.geotools.filter.function.math.FilterFunction_ceil;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * This class tests the PostgisDataStoreAPI, against the same tests as MemoryDataStore.
 * <p>
 * The test fixture is available in the shared DataTestCase, really the common elements should move
 * to a shared DataStoreAPITestCase.
 * </p>
 * <p>
 * This class does require your own DataStore, it will create a table populated with the Features
 * from the test fixture, and run a test, and then remove the table.
 * </p>
 * <p>
 * Because of the nature of this testing process you cannot run these tests in conjunction with
 * another user, so they cannot be implemented against the public server.
 * </p>
 * <p>
 * A simple properties file has been constructed, <code>fixture.properties</code>, which you may
 * direct to your own potgis database installation.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/postgis/src/test/java/org/geotools/data/postgis/PostgisDataStoreAPIOnlineTest.java $
 */
public class PostgisDataStoreAPIOnlineTest extends AbstractPostgisDataTestCase {

    private static final int LOCK_DURATION = 3600 * 1000; // one hour

    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.postgis");

    String victim = null; // "testGetFeatureWriterRemoveAll";

    /**
     * Constructor for MemoryDataStoreTest.
     * 
     * @param test
     * @throws AssertionError
     *             DOCUMENT ME!
     */
    public PostgisDataStoreAPIOnlineTest(String test) {
        super(test);

        if ((victim != null) && !test.equals(victim)) {
            throw new AssertionError("test supressed " + test);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        // uncomment these lines and put breakpoints in QueryData and DefaultTransaction finalizers
        // to spot unclosed transactions, readers and writers
        // Runtime.getRuntime().gc(); Runtime.getRuntime().gc();
        // Runtime.getRuntime().runFinalization();
    }

    /**
     * This is a quick hack to have our fixture reflect the FIDs in the database.
     * <p>
     * When the dataStore learns how to preserve our FeatureIds this won't be required.
     * </p>
     * 
     * @throws Exception
     */
    protected void updateRoadFeaturesFixture() throws Exception {
        Connection conn = pool.getConnection();
        FeatureReader reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE),
                Transaction.AUTO_COMMIT);

        Envelope bounds = new Envelope();

        try {
            SimpleFeature f;

            while (reader.hasNext()) {
                f = (SimpleFeature) reader.next();

                int index = ((Integer) f.getAttribute("id")).intValue() - 1;
                roadFeatures[index] = f;
                bounds.expandToInclude(f.getBounds());
            }
        } finally {
            reader.close();
            conn.close();
        }

        if (!roadBounds.equals(bounds)) {
            System.out.println("warning! Database changed bounds()");
            System.out.println("was:" + roadBounds);
            System.out.println("now:" + bounds);
            roadBounds = bounds;
        }

        Envelope bounds12 = new Envelope();
        bounds12.expandToInclude(roadFeatures[0].getBounds());
        bounds12.expandToInclude(roadFeatures[1].getBounds());

        if (!rd12Bounds.equals(bounds12)) {
            System.out.println("warning! Database changed bounds of rd1 & rd2");
            System.out.println("was:" + rd12Bounds);
            System.out.println("now:" + bounds12);
            rd12Bounds = bounds12;
        }

        FeatureType schema = roadFeatures[0].getFeatureType();
        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        CompareFilter tFilter = factory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression rd1Literal = factory.createLiteralExpression("r1");
        tFilter.addLeftValue(rd1Literal);

        Expression rdNameAtt = factory.createAttributeExpression(schema, "name");
        tFilter.addRightValue(rdNameAtt);
        rd1Filter = tFilter;

        tFilter = factory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);

        Expression rd2Literal = factory.createLiteralExpression("r2");
        tFilter.addLeftValue(rd2Literal);
        tFilter.addRightValue(rdNameAtt);
        rd2Filter = tFilter;

        rd12Filter = ff.or(rd2Filter, rd1Filter);
    }

    /**
     * This is a quick hack to have our fixture reflect the FIDs in the database.
     * <p>
     * When the dataStore learns how to preserve our FeatureIds this won't be required.
     * </p>
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void updateRiverFeaturesFixture() throws Exception {
        Connection conn = pool.getConnection();
        FeatureReader reader = data.getFeatureReader(new DefaultQuery("river", Filter.INCLUDE),
                Transaction.AUTO_COMMIT);

        Envelope bounds = new Envelope();

        try {
            Feature f;

            while (reader.hasNext()) {
                f = reader.next();

                int index = ((Integer) f.getAttribute("id")).intValue() - 1;
                riverFeatures[index] = f;
                bounds.expandToInclude(f.getBounds());
            }
        } finally {
            reader.close();
            conn.close();
        }

        if (!riverBounds.equals(bounds)) {
            System.out.println("warning! Database changed bounds of river");
            System.out.println("was:" + riverBounds);
            System.out.println("now:" + bounds);
            riverBounds = bounds;
        }

        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        CompareFilter tFilter = factory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression rvLiteral = factory.createLiteralExpression("rv1");
        tFilter.addLeftValue(rvLiteral);

        FeatureType schema = riverFeatures[0].getFeatureType();
        Expression rvNameAtt = factory.createAttributeExpression(schema, "river");
        tFilter.addRightValue(rvNameAtt);
        rv1Filter = tFilter;
    }

    public void testGetFeatureTypes() {
        try {
            String[] names = data.getTypeNames();
            assertTrue(contains(names, "road"));
            assertTrue(contains(names, "river"));
        } catch (IOException e) {
            e.printStackTrace();
            fail("An IOException has been thrown!");
        }
    }

    boolean contains(Object[] array, Object expected) {
        if ((array == null) || (array.length == 0)) {
            return false;
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(expected)) {
                return true;
            }
        }

        return false;
    }

    void assertContains(Object[] array, Object expected) {
        assertFalse(array == null);
        assertFalse(array.length == 0);
        assertNotNull(expected);

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(expected)) {
                return;
            }
        }

        fail("Contains " + expected);
    }

    /**
     * Like contain but based on match rather than equals
     * 
     * @param array
     *            DOCUMENT ME!
     * @param expected
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    boolean containsLax(Feature[] array, Feature expected) {
        if ((array == null) || (array.length == 0)) {
            return false;
        }

        FeatureType type = expected.getFeatureType();

        for (int i = 0; i < array.length; i++) {
            if (match(array[i], expected)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compare based on attributes not getID allows comparison of Diff contents
     * 
     * @param expected
     *            DOCUMENT ME!
     * @param actual
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    boolean match(Feature expected, Feature actual) {
        FeatureType type = expected.getFeatureType();

        for (int i = 0; i < type.getAttributeCount(); i++) {
            Object av = actual.getAttribute(i);
            Object ev = expected.getAttribute(i);

            if ((av == null) && (ev != null)) {
                return false;
            } else if ((ev == null) && (av != null)) {
                return false;
            } else if (av instanceof Geometry && ev instanceof Geometry) {
                Geometry ag = (Geometry) av;
                Geometry eg = (Geometry) ev;

                if (!ag.equals(eg)) {
                    return false;
                }
            } else if (!av.equals(ev)) {
                return false;
            }
        }

        return true;
    }

    public void testGetSchemaRoad() throws IOException {
        FeatureType expected = roadType;
        FeatureType actual = data.getSchema("road");
        assertEquals("namespace", expected.getNamespace(), actual.getNamespace());
        assertEquals("typeName", expected.getTypeName(), actual.getTypeName());

        // assertEquals( "compare", 0, DataUtilities.compare( expected, actual ));
        assertEquals("attributeCount", expected.getAttributeCount(), actual.getAttributeCount());

        for (int i = 0; i < expected.getAttributeCount(); i++) {
            AttributeType expectedAttribute = expected.getAttributeType(i);
            AttributeType actualAttribute = actual.getAttributeType(i);
            assertEquals("attribute " + expectedAttribute.getName(), expectedAttribute,
                    actualAttribute);
        }

        assertEquals(expected, actual);
    }

    public void testGetSchemaRiver() throws IOException {
        FeatureType expected = riverType;
        FeatureType actual = data.getSchema("river");
        assertEquals("namespace", expected.getNamespace(), actual.getNamespace());
        assertEquals("typeName", expected.getTypeName(), actual.getTypeName());

        // assertEquals( "compare", 0, DataUtilities.compare( expected, actual ));
        assertEquals("attributeCount", expected.getAttributeCount(), actual.getAttributeCount());

        for (int i = 0; i < expected.getAttributeCount(); i++) {
            AttributeType expectedAttribute = expected.getAttributeType(i);
            AttributeType actualAttribute = actual.getAttributeType(i);
            assertEquals("attribute " + expectedAttribute.getName(), expectedAttribute,
                    actualAttribute);
        }

        assertEquals(expected, actual);
    }

    public void testCreateSchema() throws Exception {
        String featureTypeName = "stuff";

        // delete the table, if it exists
        try {
            Connection conn = pool.getConnection();
            conn.setAutoCommit(true);
            Statement st = conn.createStatement();
            String sql = "DROP TABLE " + featureTypeName + ";";
            st.execute(sql);
            conn.close();
        } catch (Exception e) {
            // table didn't exist
        }

        // create a featureType and write it to PostGIS
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326"); // requires gt2-epsg-wkt
        AttributeType at1 = AttributeTypeFactory.newAttributeType("id", Integer.class);
        AttributeType at2 = AttributeTypeFactory.newAttributeType("name", String.class, false, 256);
        AttributeType at3 = AttributeTypeFactory.newAttributeType("the_geom", Point.class, false,
                Filter.INCLUDE, null, crs);
        AttributeType[] atts = new AttributeType[] { at1, at2, at3 };

        FeatureType newFT = FeatureTypeBuilder.newFeatureType(atts, featureTypeName);
        data.createSchema(newFT);
        FeatureType newSchema = data.getSchema(featureTypeName);
        assertNotNull(newSchema);
        assertEquals(3, newSchema.getAttributeCount());
    }

    static public void assertEquals(String message, String expected, String actual) {
        if (expected == actual) {
            return;
        }

        assertNotNull(message, expected);
        assertNotNull(message, actual);

        if (!expected.equals(actual)) {
            fail(message + " expected:<" + expected + "> but was <" + actual + ">");
        }
    }

    void assertCovers(String msg, FeatureCollection c1, FeatureCollection c2) {
        if (c1 == c2) {
            return;
        }

        assertNotNull(msg, c1);
        assertNotNull(msg, c2);
        assertEquals(msg + " size", c1.size(), c2.size());

        Feature f;

        for (FeatureIterator i = c1.features(); i.hasNext();) {
            f = i.next();
            assertTrue(msg + " " + f.getID(), c2.contains(f));
        }
    }

    public FeatureReader reader(String typeName) throws IOException {
        return data.getFeatureReader(new DefaultQuery(typeName, Filter.INCLUDE),
                Transaction.AUTO_COMMIT);
    }

    public FeatureWriter writer(String typeName) throws IOException {
        return data.getFeatureWriter(typeName, Transaction.AUTO_COMMIT);
    }

    public void testGetFeatureReader() throws IOException, IllegalAttributeException {
        assertCovered(roadFeatures, reader("road"));
        assertEquals(3, count(reader("road")));
    }

    public void testGetFeatureReaderFilterPrePost() throws IOException, IllegalFilterException {
        Transaction t = new DefaultTransaction();
        FeatureReader reader;

        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        FilterFunction_geometryType geomTypeExpr = new FilterFunction_geometryType();
        geomTypeExpr.setArgs(new Expression[] { factory.createAttributeExpression("geom") });

        CompareFilter filter = factory.createCompareFilter(FilterType.COMPARE_EQUALS);
        filter.addLeftValue(geomTypeExpr);
        filter.addRightValue(factory.createLiteralExpression("Polygon"));

        reader = data.getFeatureReader(new DefaultQuery("road", filter), t);
        // if the above statement didn't throw an exception, we're content
        assertNotNull(reader);
        reader.close();
        t.close();
    }

    public void testGetFeatureReaderFilterPrePost2() throws IOException, IllegalFilterException {
        // GEOT-1069, make sure the post filter is run even if the geom property is not requested
        Transaction t = new DefaultTransaction();
        FeatureReader reader;

        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        FilterFunction_geometryType geomTypeExpr = new FilterFunction_geometryType();
        geomTypeExpr.setArgs(new Expression[] { factory.createAttributeExpression("geom") });

        CompareFilter filter = factory.createCompareFilter(FilterType.COMPARE_EQUALS);
        filter.addLeftValue(geomTypeExpr);
        filter.addRightValue(factory.createLiteralExpression("Polygon"));

        reader = data.getFeatureReader(new DefaultQuery("road", filter), t);
        // if the above statement didn't throw an exception, we're content
        assertNotNull(reader);
        reader.close();
        t.close();
    }

    public void testGetFeatureReaderRetypeBug() throws Exception {
        // this is here to avoid http://jira.codehaus.org/browse/GEOT-1069
        // to come up again

        Transaction t = new DefaultTransaction();
        FeatureType type = data.getSchema("river");
        FeatureReader reader;

        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        CompareFilter cf = ff.createCompareFilter(Filter.COMPARE_EQUALS);
        cf.addLeftValue(ff.createAttributeExpression("flow"));
        cf.addRightValue(ff.createLiteralExpression(4.5));

        DefaultQuery q = new DefaultQuery("river");
        q.setPropertyNames(new String[] { "geom" });
        q.setFilter(cf);

        // with GEOT-1069 an exception is thrown here
        reader = data.getFeatureReader(q, t);
        assertTrue(reader.hasNext());
        assertEquals(1, reader.getFeatureType().getAttributeCount());
        reader.next();
        assertFalse(reader.hasNext());
        reader.close();
        t.close();
    }

    public void testGetFeatureReaderRetypeBug2() throws Exception {
        // this is here to avoid http://jira.codehaus.org/browse/GEOT-1069
        // to come up again

        Transaction t = new DefaultTransaction();
        FeatureType type = data.getSchema("river");
        FeatureReader reader;

        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        CompareFilter cf = ff.createCompareFilter(Filter.COMPARE_EQUALS);
        FunctionExpression ceil = new FilterFunction_ceil();
        ceil.setArgs(new Expression[] { ff.createAttributeExpression("flow") });
        cf.addLeftValue(ceil);
        cf.addRightValue(ff.createLiteralExpression(5));

        DefaultQuery q = new DefaultQuery("river");
        q.setPropertyNames(new String[] { "geom" });
        q.setFilter(cf);

        // with GEOT-1069 an exception is thrown here
        reader = data.getFeatureReader(q, t);
        assertTrue(reader.hasNext());
        assertEquals(1, reader.getFeatureType().getAttributeCount());
        reader.next();
        assertFalse(reader.hasNext());
        reader.close();
        t.close();
    }

    public void testGetFeatureReaderMutability() throws IOException, IllegalAttributeException {
        FeatureReader reader = reader("road");
        Feature feature;

        while (reader.hasNext()) {
            feature = (Feature) reader.next();
            feature.setAttribute("name", null);
        }

        reader.close();

        reader = reader("road");

        while (reader.hasNext()) {
            feature = (Feature) reader.next();
            assertNotNull(feature.getAttribute("name"));
        }

        reader.close();

        try {
            reader.next();
            fail("next should fail with an IOException");
        } catch (IOException expected) {
        }
    }

    public void testGetFeatureReaderConcurrency() throws NoSuchElementException, IOException,
            IllegalAttributeException {
        FeatureReader reader1 = reader("road");
        FeatureReader reader2 = reader("road");
        FeatureReader reader3 = reader("river");

        Feature feature1;
        Feature feature2;
        Feature feature3;

        while (reader1.hasNext() || reader2.hasNext() || reader3.hasNext()) {
            assertTrue(contains(roadFeatures, reader1.next()));
            assertTrue(contains(roadFeatures, reader2.next()));

            if (reader3.hasNext()) {
                assertTrue(contains(riverFeatures, reader3.next()));
            }
        }

        try {
            reader1.next();
            fail("next should fail with an NoSuchElementException");
        } catch (NoSuchElementException expectedNoElement) {
            // this is new to me, I had expected an IOException
        }

        try {
            reader2.next();
            fail("next should fail with an NoSuchElementException");
        } catch (NoSuchElementException expectedNoElement) {
        }

        try {
            reader3.next();
            fail("next should fail with an NoSuchElementException");
        } catch (NoSuchElementException expectedNoElement) {
        }

        reader1.close();
        reader2.close();
        reader3.close();

        try {
            reader1.next();
            fail("next should fail with an IOException");
        } catch (IOException expectedClosed) {
        }

        try {
            reader2.next();
            fail("next should fail with an IOException");
        } catch (IOException expectedClosed) {
        }

        try {
            reader3.next();
            fail("next should fail with an IOException");
        } catch (IOException expectedClosed) {
        }
    }

    public void testGetFeatureReaderFilterAutoCommit() throws NoSuchElementException, IOException,
            IllegalAttributeException {
        FeatureType type = data.getSchema("road");
        FeatureReader reader;

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE),
                Transaction.AUTO_COMMIT);
        assertFalse(reader instanceof FilteringFeatureReader);
        assertEquals(type, reader.getFeatureType());
        assertEquals(roadFeatures.length, count(reader));

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.EXCLUDE),
                Transaction.AUTO_COMMIT);
        assertFalse(reader.hasNext());

        assertEquals(type, reader.getFeatureType());
        assertEquals(0, count(reader));

        reader = data
                .getFeatureReader(new DefaultQuery("road", rd1Filter), Transaction.AUTO_COMMIT);

        // assertTrue(reader instanceof FilteringFeatureReader);
        assertEquals(type, reader.getFeatureType());
        assertEquals(1, count(reader));
    }

    public void testGetFeatureReaderFilterTransaction() throws NoSuchElementException, IOException,
            IllegalAttributeException {
        Transaction t = new DefaultTransaction();
        FeatureType type = data.getSchema("road");
        FeatureReader reader;

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.EXCLUDE), t);
        assertFalse(reader.hasNext());
        assertEquals(type, reader.getFeatureType());
        assertEquals(0, count(reader));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t);
        assertEquals(type, reader.getFeatureType());
        assertEquals(roadFeatures.length, count(reader));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", rd1Filter), t);
        assertEquals(type, reader.getFeatureType());
        assertEquals(1, count(reader));
        reader.close();

        FeatureWriter writer = data.getFeatureWriter("road", Filter.INCLUDE, t);
        Feature feature;

        while (writer.hasNext()) {
            feature = writer.next();

            if (feature.getID().equals(roadFeatures[0].getID())) {
                writer.remove();
            }
        }
        writer.close();

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.EXCLUDE), t);
        assertEquals(0, count(reader));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t);
        assertEquals(roadFeatures.length - 1, count(reader));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", rd1Filter), t);
        assertEquals(0, count(reader));
        reader.close();

        t.rollback();
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.EXCLUDE), t);
        assertEquals(0, count(reader));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t);
        assertEquals(roadFeatures.length, count(reader));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", rd1Filter), t);
        assertEquals(1, count(reader));
        reader.close();
        t.close();
    }

    /**
     * Ensure readers contents equal those in the feature array
     * 
     * @param features
     *            DOCUMENT ME!
     * @param reader
     *            DOCUMENT ME!
     * @throws NoSuchElementException
     *             DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     * @throws IllegalAttributeException
     *             DOCUMENT ME!
     */
    void assertCovered(Feature[] features, FeatureReader reader) throws NoSuchElementException,
            IOException, IllegalAttributeException {
        int count = 0;

        try {
            while (reader.hasNext()) {
                assertContains(features, reader.next());
                count++;
            }
        } finally {
            reader.close();
        }

        assertEquals(features.length, count);
    }

    /**
     * Ensure readers contents match those in the feature array
     * <p>
     * Implemented using match on attribute types, not feature id
     * </p>
     * 
     * @param array
     *            DOCUMENT ME!
     * @param reader
     *            DOCUMENT ME!
     * @throws Exception
     *             DOCUMENT ME!
     */
    void assertMatched(Feature[] array, FeatureReader reader) throws Exception {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();
                assertMatch(array, feature);
                count++;
            }
        } finally {
            reader.close();
        }

        assertEquals("array not matched by reader", array.length, count);
    }

    void assertMatch(Feature[] array, Feature feature) {
        assertTrue(array != null);
        assertTrue(array.length != 0);

        FeatureType schema = feature.getFeatureType();

        for (int i = 0; i < array.length; i++) {
            if (match(array[i], feature)) {
                return;
            }
        }

        System.out.println("not found:" + feature);

        for (int i = 0; i < array.length; i++) {
            System.out.println(i + ":" + array[i]);
        }

        fail("array has no match for " + feature);
    }

    /**
     * Ensure that FeatureReader reader contains exactly the contents of array.
     * 
     * @param reader
     *            DOCUMENT ME!
     * @param array
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws NoSuchElementException
     *             DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     * @throws IllegalAttributeException
     *             DOCUMENT ME!
     */
    boolean covers(FeatureReader reader, Feature[] array) throws NoSuchElementException,
            IOException, IllegalAttributeException {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();

                if (!contains(array, feature)) {
                    return false;
                }

                count++;
            }
        } finally {
            reader.close();
        }

        return count == array.length;
    }

    boolean covers(FeatureIterator reader, Feature[] array) throws NoSuchElementException,
            IOException, IllegalAttributeException {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();

                if (!contains(array, feature)) {
                    return false;
                }

                count++;
            }
        } finally {
            reader.close();
        }

        return count == array.length;
    }

    boolean coversLax(FeatureReader reader, Feature[] array) throws NoSuchElementException,
            IOException, IllegalAttributeException {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();

                if (!containsLax(array, feature)) {
                    return false;
                }

                count++;
            }
        } finally {
            reader.close();
        }

        return count == array.length;
    }

    void dump(String message, FeatureReader reader) throws NoSuchElementException, IOException,
            IllegalAttributeException {
        Feature feature;
        int count = 0;

        try {
            while (reader.hasNext()) {
                feature = reader.next();

                String msg = message + ": feture " + count + "=" + feature;

                // LOGGER.info( msg );
                System.out.println(msg);
                count++;
            }
        } finally {
            reader.close();
        }
    }

    void dump(String message, Object[] array) {
        for (int i = 0; i < array.length; i++) {
            String msg = message + ": " + i + "=" + array[i];

            // LOGGER.info( msg );
            System.out.println(msg);
        }
    }

    /*
     * Test for FeatureWriter getFeatureWriter(String, Filter, Transaction)
     */
    public void xtestGetFeatureWriter() throws Exception {
        FeatureWriter writer = data.getFeatureWriter("road", Filter.INCLUDE,
                Transaction.AUTO_COMMIT);
        assertEquals(roadFeatures.length, count(writer));
    }

    public void testGetFeatureWriterClose() throws Exception {
        FeatureWriter writer = data.getFeatureWriter("road", Filter.INCLUDE,
                Transaction.AUTO_COMMIT);

        writer.close();

        try {
            assertFalse(writer.hasNext());
            fail("Should not be able to use a closed writer");
        } catch (IOException expected) {
        }

        try {
            assertNull(writer.next());
            fail("Should not be able to use a closed writer");
        } catch (IOException expected) {
        }

        try {
            writer.close();
        } catch (IOException expected) {
        }
    }

    public void testGetFeatureWriterRemove() throws IOException, IllegalAttributeException {
        FeatureWriter writer = writer("road");
        Feature feature;

        while (writer.hasNext()) {
            feature = writer.next();

            if (feature.getID().equals(roadFeatures[0].getID())) {
                writer.remove();
            }
        }
        writer.close();

        assertEquals(roadFeatures.length - 1, count("road"));
    }

    public void testGetFeatureWriterRemoveAll() throws IOException, IllegalAttributeException {
        FeatureWriter writer = writer("road");
        Feature feature;

        try {
            while (writer.hasNext()) {
                feature = writer.next();
                writer.remove();
            }
        } finally {
            writer.close();
        }

        assertEquals(0, count("road"));
    }

    public int count(String typeName) throws IOException {
        // return count(reader(typeName));
        // makes use of optimization if any
        return data.getFeatureSource(typeName).getFeatures().size();
    }

    public void testGetFeaturesWriterAdd() throws IOException, IllegalAttributeException {
        FeatureWriter writer = data.getFeatureWriter("road", Transaction.AUTO_COMMIT);
        SimpleFeature feature;

        LOGGER.info("about to call has next on writer " + writer);

        while (writer.hasNext()) {
            feature = (SimpleFeature) writer.next();
        }

        assertFalse(writer.hasNext());

        feature = (SimpleFeature) writer.next();
        feature.setAttributes(newRoad.getAttributes(null));
        writer.write();

        assertFalse(writer.hasNext());
        writer.close();
        assertEquals(roadFeatures.length + 1, count("road"));
    }

    /**
     * Seach for feature based on AttributeType.
     * <p>
     * If attributeName is null, we will search by feature.getID()
     * </p>
     * <p>
     * The provided reader will be closed by this opperations.
     * </p>
     * 
     * @param reader
     *            reader to search through
     * @param attributeName
     *            attributeName, or null for featureID
     * @param value
     *            value to match
     * @return Feature
     * @throws NoSuchElementException
     *             if a match could not be found
     * @throws IOException
     *             We could not use reader
     * @throws IllegalAttributeException
     *             if attributeName did not match schema
     */
    public Feature findFeature(FeatureReader reader, String attributeName, Object value)
            throws NoSuchElementException, IOException, IllegalAttributeException {
        Feature f;

        try {
            while (reader.hasNext()) {
                f = reader.next();

                if (attributeName == null) {
                    if (value.equals(f.getID())) {
                        return f;
                    }
                } else {
                    if (value.equals(f.getAttribute(attributeName))) {
                        return f;
                    }
                }
            }
        } finally {
            reader.close();
        }

        if (attributeName == null) {
            throw new NoSuchElementException("No match for FID=" + value);
        } else {
            throw new NoSuchElementException("No match for " + attributeName + "=" + value);
        }
    }

    public Feature feature(String typeName, String fid) throws NoSuchElementException, IOException,
            IllegalAttributeException {
        FeatureReader reader = reader(typeName);
        Feature f;

        try {
            while (reader.hasNext()) {
                f = reader.next();

                if (fid.equals(f.getID())) {
                    return f;
                }
            }
        } finally {
            reader.close();
        }

        return null;
    }

    public void testGetFeaturesWriterModify() throws IOException, IllegalAttributeException {
        FeatureWriter writer = writer("road");
        Feature feature;

        while (writer.hasNext()) {
            feature = writer.next();

            if (feature.getID().equals(roadFeatures[0].getID())) {
                feature.setAttribute("name", "changed");
                writer.write();
            }
        }
        writer.close();

        feature = (Feature) feature("road", roadFeatures[0].getID());
        assertNotNull(feature);
        assertEquals("changed", feature.getAttribute("name"));
    }

    public void testGetFeatureWriterTypeNameTransaction() throws NoSuchElementException,
            IOException, IllegalAttributeException {
        FeatureWriter writer;

        writer = data.getFeatureWriter("road", Transaction.AUTO_COMMIT);
        assertEquals(roadFeatures.length, count(writer));

        // writer.close(); called by count.
    }

    public void testGetFeatureWriterAppendTypeNameTransaction() throws Exception {
        FeatureWriter writer;

        writer = data.getFeatureWriterAppend("road", Transaction.AUTO_COMMIT);
        assertEquals(0, count(writer));

        // writer.close(); called by count
    }

    /*
     * Test for FeatureWriter getFeatureWriter(String, boolean, Transaction) @task REVISIT:
     * JDBCDataStore currently does not return these proper instanceof's. If we want to guarantee
     * that people can't append to a request with a FeatureWriter then we could add the
     * functionality to JDBCDataStore by having getFeatureWriter(.. Filter ...) check to see if the
     * FeatureWriter returned is instanceof FilteringFeatureWriter, and if not then just wrap it in
     * a FilteringFeatureWriter(writer, Filter.INCLUDE). I think it'd be a bit of unnecessary
     * overhead, but if we want it it's easy to do. It will guarantee that calls with Filter won't
     * ever append. Doing with Filter.INCLUDE, however, would require a bit of reworking, as the
     * Filter getFeatureWriter is currently where we do the bulk of the work.
     */
    public void testGetFeatureWriterFilter() throws NoSuchElementException, IOException,
            IllegalAttributeException {
        FeatureWriter writer;
        writer = data.getFeatureWriter("road", Filter.EXCLUDE, Transaction.AUTO_COMMIT);

        // see task above
        // assertTrue(writer instanceof EmptyFeatureWriter);
        assertEquals(0, count(writer));

        writer = data.getFeatureWriter("road", Filter.INCLUDE, Transaction.AUTO_COMMIT);

        // assertFalse(writer instanceof FilteringFeatureWriter);
        assertEquals(roadFeatures.length, count(writer));

        writer = data.getFeatureWriter("road", rd1Filter, Transaction.AUTO_COMMIT);

        // assertTrue(writer instanceof FilteringFeatureWriter);
        assertEquals(1, count(writer));
    }

    /**
     * Test two transactions one removing feature, and one adding a feature.
     * 
     * @throws IllegalAttributeException
     * @throws Exception
     *             DOCUMENT ME!
     */
    public void testGetFeatureWriterTransaction() throws Exception {
        Transaction t1 = new DefaultTransaction();
        Transaction t2 = new DefaultTransaction();
        FeatureWriter writer1 = data.getFeatureWriter("road", rd1Filter, t1);
        FeatureWriter writer2 = data.getFeatureWriterAppend("road", t2);

        FeatureType road = data.getSchema("road");
        FeatureReader reader;
        SimpleFeature feature;
        SimpleFeature[] ORIGINAL = roadFeatures;
        Feature[] REMOVE = new Feature[ORIGINAL.length - 1];
        Feature[] ADD = new Feature[ORIGINAL.length + 1];
        Feature[] FINAL = new Feature[ORIGINAL.length];
        int i;
        int index;
        index = 0;

        for (i = 0; i < ORIGINAL.length; i++) {
            feature = ORIGINAL[i];

            if (!feature.getID().equals(roadFeatures[0].getID())) {
                REMOVE[index++] = feature;
            }
        }

        for (i = 0; i < ORIGINAL.length; i++) {
            ADD[i] = ORIGINAL[i];
        }

        ADD[i] = newRoad; // will need to update with Fid from database

        for (i = 0; i < REMOVE.length; i++) {
            FINAL[i] = REMOVE[i];
        }

        FINAL[i] = newRoad; // will need to update with Fid from database

        // start off with ORIGINAL
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE),
                Transaction.AUTO_COMMIT);
        assertTrue("Sanity check failed: before modification reader didn't match original content",
                covers(reader, ORIGINAL));
        reader.close();

        // writer 1 removes road.rd1 on t1
        // -------------------------------
        // - tests transaction independence from DataStore
        while (writer1.hasNext()) {
            feature = (SimpleFeature) writer1.next();
            assertEquals(roadFeatures[0].getID(), feature.getID());
            writer1.remove();
        }

        // still have ORIGINAL and t1 has REMOVE
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE),
                Transaction.AUTO_COMMIT);
        assertTrue("Feature deletion managed to leak out of transaction?", covers(reader, ORIGINAL));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t1);
        assertTrue(covers(reader, REMOVE));
        reader.close();

        // close writer1
        // --------------
        // ensure that modification is left up to transaction commmit
        writer1.close();

        // We still have ORIGIONAL and t1 has REMOVE
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE),
                Transaction.AUTO_COMMIT);
        assertTrue(covers(reader, ORIGINAL));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t1);
        assertTrue(covers(reader, REMOVE));
        reader.close();

        // writer 2 adds road.rd4 on t2
        // ----------------------------
        // - tests transaction independence from each other
        feature = (SimpleFeature) writer2.next();
        feature.setAttributes(newRoad.getAttributes(null));
        writer2.write();

        // HACK: ?!? update ADD and FINAL with new FID from database
        //
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t2);
        newRoad = findFeature(reader, "id", new Integer(4));
        System.out.println("newRoad:" + newRoad);
        ADD[ADD.length - 1] = newRoad;
        FINAL[FINAL.length - 1] = newRoad;
        reader.close();

        // We still have ORIGINAL and t2 has ADD
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE),
                Transaction.AUTO_COMMIT);
        assertTrue(covers(reader, ORIGINAL));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t2);
        assertMatched(ADD, reader); // broken due to FID problem
        reader.close();

        writer2.close();

        // Still have ORIGIONAL and t2 has ADD
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE),
                Transaction.AUTO_COMMIT);
        assertTrue(covers(reader, ORIGINAL));
        reader.close();
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t2);
        assertTrue(coversLax(reader, ADD));
        reader.close();

        // commit t1
        // ---------
        // -ensure that delayed writing of transactions takes place
        //
        t1.commit();

        // We now have REMOVE, as does t1 (which has not additional diffs)
        // t2 will have FINAL
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE),
                Transaction.AUTO_COMMIT);
        assertTrue(covers(reader, REMOVE));
        reader.close();
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t1);
        assertTrue(covers(reader, REMOVE));
        reader.close();
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t2);
        assertTrue(coversLax(reader, FINAL));
        reader.close();

        // commit t2
        // ---------
        // -ensure that everyone is FINAL at the end of the day
        t2.commit();

        // We now have Number( remove one and add one)
        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE),
                Transaction.AUTO_COMMIT);
        assertTrue(coversLax(reader, FINAL));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t1);
        assertTrue(coversLax(reader, FINAL));
        reader.close();

        reader = data.getFeatureReader(new DefaultQuery("road", Filter.INCLUDE), t2);
        assertTrue(coversLax(reader, FINAL));
        reader.close();

        t1.close();
        t2.close();
    }

    /**
     * Tests that if 2 transactions attempt to modify the same feature without committing, that the
     * second transaction does not lock up waiting to obtain the lock.
     * 
     * @author chorner
     * @throws IOException
     * @throws IllegalAttributeException
     * @throws SQLException
     */
    public void testGetFeatureWriterConcurrency() throws Exception {
        // if we don't have postgres >= 8.1, don't bother testing (it WILL block)
        Connection conn = null;
        try {
            conn = pool.getConnection();
            int major = conn.getMetaData().getDatabaseMajorVersion();
            int minor = conn.getMetaData().getDatabaseMinorVersion();
            if (!((major > 8) || ((major == 8) && minor >= 1))) {
                return; // concurrency support is weak
            }
        } finally {
            if (conn != null)
                conn.close();
        }
        Transaction t1 = new DefaultTransaction();
        FeatureWriter writer1 = data.getFeatureWriter("road", rd1Filter, t1);
        Feature f1 = writer1.next();
        f1.setAttribute("name", new String("r1_"));
        writer1.write();

        Transaction t2 = new DefaultTransaction();
        FeatureWriter writer2 = data.getFeatureWriter("road", rd1Filter, t2);
        Feature f2 = writer2.next();
        f2.setAttribute("name", new String("r1__"));
        try {
            writer2.write(); // this will either lock up or toss chunks
            fail("Feature lock should have failed");
        } catch (FeatureLockException e) {
            // success (test-wise... our write failed quite well too)
            assertEquals("road.rd1", e.getFeatureID());
        }

        t1.rollback(); // don't save
        writer1.close();
        t1.close();

        t2.rollback();
        writer2.close();
        t2.close();
    }

    // Feature Source Testing
    public void testGetFeatureSourceRoad() throws IOException {
        FeatureSource road = data.getFeatureSource("road");

        assertEquals(roadType, road.getSchema());
        assertSame(data, road.getDataStore());

        int count = road.getCount(Query.ALL);
        assertTrue((count == 3) || (count == -1));

        Envelope bounds = road.getBounds(Query.ALL);
        assertTrue((bounds == null) || bounds.equals(roadBounds));

        FeatureCollection all = road.getFeatures();
        assertEquals(3, all.size());
        assertEquals(roadBounds, all.getBounds());

        FeatureCollection expected = DataUtilities.collection(roadFeatures);

        assertCovers("all", expected, all);
        assertEquals(roadBounds, all.getBounds());

        FeatureCollection some = road.getFeatures(rd12Filter);
        assertEquals(2, some.size());

        Envelope e = new Envelope();
        e.expandToInclude(roadFeatures[0].getBounds());
        e.expandToInclude(roadFeatures[1].getBounds());
        assertEquals(e, some.getBounds());
        assertEquals(some.getSchema(), road.getSchema());

        DefaultQuery query = new DefaultQuery("road", rd12Filter, new String[] { "name" });

        FeatureCollection half = road.getFeatures(query);
        assertEquals(2, half.size());
        assertEquals(1, half.getSchema().getAttributeCount());

        FeatureIterator reader = half.features();
        FeatureType type = half.getSchema();
        reader.close();

        FeatureType actual = half.getSchema();

        assertEquals(type.getTypeName(), actual.getTypeName());
        assertEquals(type.getNamespace(), actual.getNamespace());
        assertEquals(type.getAttributeCount(), actual.getAttributeCount());

        for (int i = 0; i < type.getAttributeCount(); i++) {
            assertEquals(type.getAttributeType(i), actual.getAttributeType(i));
        }

        assertNull(type.getDefaultGeometry()); // geometry is null, therefore no bounds
        assertEquals(type.getDefaultGeometry(), actual.getDefaultGeometry());
        assertEquals(type, actual);

        Envelope b = half.getBounds();
        Envelope expectedBounds = isEnvelopeComputingEnabled() ? roadBounds : new Envelope();
        assertEquals(expectedBounds, b); // empty envelope is expected
    }

    /**
     * Return true if the datastore is capable of computing the road bounds given a query
     * 
     * @return
     */
    protected boolean isEnvelopeComputingEnabled() {
        return false;
    }

    public void testGetFeatureSourceRiver() throws NoSuchElementException, IOException,
            IllegalAttributeException {
        FeatureSource river = data.getFeatureSource("river");

        assertEquals(riverType, river.getSchema());
        assertSame(data, river.getDataStore());

        FeatureCollection all = river.getFeatures();
        assertEquals(2, all.size());
        assertEquals(riverBounds, all.getBounds());
        assertTrue("rivers", covers(all.features(), riverFeatures));

        FeatureCollection expected = DataUtilities.collection(riverFeatures);
        assertCovers("all", expected, all);
        assertEquals(riverBounds, all.getBounds());
    }

    //
    // Feature Store Testing
    //
    public void testGetFeatureStoreModifyFeatures1() throws IOException {
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");

        // FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        // rd1Filter = factory.createFidFilter( roadFeatures[0].getID() );
        Object changed = new Integer(5);
        AttributeType name = roadType.getAttributeType("id");
        road.modifyFeatures(name, changed, rd1Filter);

        FeatureCollection results = road.getFeatures(rd1Filter);
        FeatureIterator features = results.features();
        assertEquals(changed, features.next().getAttribute("id"));
        results.close(features);
    }

    public void testGetFeatureStoreModifyFeatures2() throws IOException {
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");

        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        rd1Filter = factory.createFidFilter(roadFeatures[0].getID());

        AttributeType name = roadType.getAttributeType("name");
        road.modifyFeatures(new AttributeType[] { name, }, new Object[] { "changed", }, rd1Filter);

        FeatureCollection results = road.getFeatures(rd1Filter);
        FeatureIterator features = results.features();
        assertEquals("changed", features.next().getAttribute("name"));
        results.close(features);
    }

    /**
     * Test with a filter that won't be matched after the modification is done, was throwing an NPE
     * before the fix
     * 
     * @throws IOException
     */
    public void testGetFeatureStoreModifyFeatures3() throws IOException {
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");

        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        CompareFilter filter = factory.createCompareFilter(Filter.COMPARE_EQUALS);
        filter.addLeftValue(ff.createAttributeExpression("name"));
        filter.addRightValue(ff.createLiteralExpression("r1"));

        AttributeType name = roadType.getAttributeType("name");
        road.modifyFeatures(new AttributeType[] { name, }, new Object[] { "changed", }, filter);
    }

    public void testGetFeatureStoreRemoveFeatures() throws IOException {
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");

        road.removeFeatures(rd1Filter);
        assertEquals(0, road.getFeatures(rd1Filter).size());
        assertEquals(roadFeatures.length - 1, road.getFeatures().size());
    }

    public void testGetFeatureStoreRemoveAllFeatures() throws IOException {
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");

        road.removeFeatures(Filter.INCLUDE);
        assertEquals(0, road.getFeatures().size());
    }

    public void testGetFeatureStoreAddFeatures() throws IOException {
        FeatureReader reader = DataUtilities.reader(new Feature[] { newRoad, });
        FeatureStore road = (FeatureStore) data.getFeatureSource("road");

        road.addFeatures(DataUtilities.collection(reader));
        assertEquals(roadFeatures.length + 1, count("road"));
    }

    public void testGetFeatureStoreSetFeatures() throws NoSuchElementException, IOException,
            IllegalAttributeException {
        FeatureReader reader = DataUtilities.reader(new Feature[] { newRoad, });

        FeatureStore road = (FeatureStore) data.getFeatureSource("road");

        assertEquals(3, count("road"));

        road.setFeatures(reader);

        assertEquals(1, count("road"));
    }

    // public void testGetFeatureStoreTransactionSupport()
    // throws Exception {
    // Transaction t1 = new DefaultTransaction();
    // Transaction t2 = new DefaultTransaction();
    //
    // FeatureStore road = (FeatureStore) data.getFeatureSource("road");
    // FeatureStore road1 = (FeatureStore) data.getFeatureSource("road");
    // FeatureStore road2 = (FeatureStore) data.getFeatureSource("road");
    //
    // road1.setTransaction(t1);
    // road2.setTransaction(t2);
    //
    // Feature feature;
    // Feature[] ORIGIONAL = roadFeatures;
    // Feature[] REMOVE = new Feature[ORIGIONAL.length - 1];
    // Feature[] ADD = new Feature[ORIGIONAL.length + 1];
    // Feature[] FINAL = new Feature[ORIGIONAL.length];
    // int i;
    // int index;
    // index = 0;
    //
    // for (i = 0; i < ORIGIONAL.length; i++) {
    // feature = ORIGIONAL[i];
    // LOGGER.info("id is " + feature.getID());
    //
    // if (!feature.getID().equals("road.rd1")) {
    // REMOVE[index++] = feature;
    // }
    // }
    //
    // for (i = 0; i < ORIGIONAL.length; i++) {
    // ADD[i] = ORIGIONAL[i];
    // }
    //
    // ADD[i] = newRoad;
    //
    // for (i = 0; i < REMOVE.length; i++) {
    // FINAL[i] = REMOVE[i];
    // }
    //
    // FINAL[i] = newRoad;
    //
    // // start of with ORIGINAL
    // assertTrue(covers(road.getFeatures().reader(), ORIGIONAL));
    //
    // // road1 removes road.rd1 on t1
    // // -------------------------------
    // // - tests transaction independence from DataStore
    // road1.removeFeatures(rd1Filter);
    //
    // // still have ORIGIONAL and t1 has REMOVE
    // assertTrue(covers(road.getFeatures().reader(), ORIGIONAL));
    // assertTrue(covers(road1.getFeatures().reader(), REMOVE));
    //
    // // road2 adds road.rd4 on t2
    // // ----------------------------
    // // - tests transaction independence from each other
    // FeatureReader reader = DataUtilities.reader(new Feature[] { newRoad, });
    // road2.addFeatures(reader);
    //
    // // We still have ORIGIONAL, t1 has REMOVE, and t2 has ADD
    // assertTrue(covers(road.getFeatures().reader(), ORIGIONAL));
    // assertTrue(covers(road1.getFeatures().reader(), REMOVE));
    // assertTrue(coversLax(road2.getFeatures().reader(), ADD));
    //
    // // commit t1
    // // ---------
    // // -ensure that delayed writing of transactions takes place
    // //
    // t1.commit();
    //
    // // We now have REMOVE, as does t1 (which has not additional diffs)
    // // t2 will have FINAL
    // assertTrue(covers(road.getFeatures().reader(), REMOVE));
    // assertTrue(covers(road1.getFeatures().reader(), REMOVE));
    // assertTrue(coversLax(road2.getFeatures().reader(), FINAL));
    //
    // // commit t2
    // // ---------
    // // -ensure that everyone is FINAL at the end of the day
    // t2.commit();
    //
    // // We now have Number( remove one and add one)
    // assertTrue(coversLax(road.getFeatures().reader(), FINAL));
    // assertTrue(coversLax(road1.getFeatures().reader(), FINAL));
    // assertTrue(coversLax(road2.getFeatures().reader(), FINAL));
    // }

    boolean isLocked(String typeName, String fid) {
        InProcessLockingManager lockingManager = (InProcessLockingManager) data.getLockingManager();

        return lockingManager.isLocked(typeName, fid);
    }

    //
    // FeatureLocking Testing
    //

    /*
     * Test for void lockFeatures()
     */
    public void testLockFeatures() throws IOException {
        FeatureLock lock = FeatureLockFactory.generate("test", LOCK_DURATION);
        FeatureLocking road = (FeatureLocking) data.getFeatureSource("road");
        road.setFeatureLock(lock);

        assertFalse(isLocked("road", "road.rd1"));
        road.lockFeatures();
        assertTrue(isLocked("road", "road.rd1"));
    }

    public void testUnLockFeatures() throws IOException {
        FeatureLock lock = FeatureLockFactory.generate("test", LOCK_DURATION);
        FeatureLocking road = (FeatureLocking) data.getFeatureSource("road");
        road.setFeatureLock(lock);
        road.lockFeatures();

        try {
            road.unLockFeatures();
            fail("unlock should fail due on AUTO_COMMIT");
        } catch (IOException expected) {
        }

        Transaction t = new DefaultTransaction();
        road.setTransaction(t);

        try {
            road.unLockFeatures();
            fail("unlock should fail due lack of authorization");
        } catch (IOException expected) {
        }

        t.addAuthorization(lock.getAuthorization());
        road.unLockFeatures();
        t.close();
    }

    public void testLockFeatureInteraction() throws IOException {
        FeatureLock lockA = FeatureLockFactory.generate("LockA", LOCK_DURATION);
        FeatureLock lockB = FeatureLockFactory.generate("LockB", LOCK_DURATION);
        Transaction t1 = new DefaultTransaction();
        Transaction t2 = new DefaultTransaction();
        FeatureLocking road1 = (FeatureLocking) data.getFeatureSource("road");
        FeatureLocking road2 = (FeatureLocking) data.getFeatureSource("road");
        road1.setTransaction(t1);
        road2.setTransaction(t2);
        road1.setFeatureLock(lockA);
        road2.setFeatureLock(lockB);

        assertFalse(isLocked("road", "road.rd1"));
        assertFalse(isLocked("road", "road.rd2"));
        assertFalse(isLocked("road", "road.rd3"));

        road1.lockFeatures(rd1Filter);
        assertTrue(isLocked("road", "road.rd1"));
        assertFalse(isLocked("road", "road.rd2"));
        assertFalse(isLocked("road", "road.rd3"));

        road2.lockFeatures(rd2Filter);
        assertTrue(isLocked("road", "road.rd1"));
        assertTrue(isLocked("road", "road.rd2"));
        assertFalse(isLocked("road", "road.rd3"));

        try {
            road1.unLockFeatures(rd1Filter);
            fail("need authorization");
        } catch (IOException expected) {
        }

        t1.addAuthorization(lockA.getAuthorization());

        try {
            road1.unLockFeatures(rd2Filter);
            fail("need correct authorization");
        } catch (IOException expected) {
        }

        road1.unLockFeatures(rd1Filter);
        assertFalse(isLocked("road", "road.rd1"));
        assertTrue(isLocked("road", "road.rd2"));
        assertFalse(isLocked("road", "road.rd3"));

        t2.addAuthorization(lockB.getAuthorization());
        road2.unLockFeatures(rd2Filter);
        assertFalse(isLocked("road", "road.rd1"));
        assertFalse(isLocked("road", "road.rd2"));
        assertFalse(isLocked("road", "road.rd3"));

        t1.close();
        t2.close();
    }

    public void testGetFeatureLockingExpire() throws Exception {
        FeatureLock lock = FeatureLockFactory.generate("Timed", 1000);

        FeatureLocking road = (FeatureLocking) data.getFeatureSource("road");
        road.setFeatureLock(lock);
        assertFalse(isLocked("road", "road.rd1"));

        road.lockFeatures(rd1Filter);
        assertTrue(isLocked("road", "road.rd1"));
        Thread.sleep(1100);
        assertFalse(isLocked("road", "road.rd1"));
    }

    public void testOidFidMapper() throws IOException, IllegalAttributeException {
        // get the schema and make sure the FID mapper is an OID one
        FIDMapper mapper = ((PostgisDataStore) data).getFIDMapper("lake");
        FIDMapper base = null;
        if (mapper instanceof TypedFIDMapper) {
            base = ((TypedFIDMapper) mapper).getWrappedMapper();
        } else
            base = mapper;

        assertTrue(base instanceof OIDFidMapper);

        // read features from the database, just check we don't crash and that id's are not null
        FeatureReader reader = ((PostgisDataStore) data).getFeatureReader(data.getSchema("lake"),
                Filter.INCLUDE, Transaction.AUTO_COMMIT);

        while (reader.hasNext()) {
            Feature f = reader.next();
            assertNotNull(f.getID());
        }
        reader.close();

        FeatureWriter writer = data.getFeatureWriterAppend("lake", Transaction.AUTO_COMMIT);
        SimpleFeature f = (SimpleFeature) writer.next();
        Object[] attributes = new Object[f.getNumberOfAttributes()];
        f.setAttributes(lakeFeatures[0].getAttributes(attributes));
        writer.write();
        writer.close();

        String id = f.getID();
        assertNotNull(id);
        assertTrue(!id.trim().equals(""));
        Long.parseLong(id.substring(5)); // make sure it's a number
    }

}
