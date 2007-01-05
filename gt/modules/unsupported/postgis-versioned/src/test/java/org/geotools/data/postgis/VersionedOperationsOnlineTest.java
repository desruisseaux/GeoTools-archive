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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Envelope;

public class VersionedOperationsOnlineTest extends AbstractVersionedPostgisDataTestCase {
    FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    public VersionedOperationsOnlineTest(String name) {
        super(name);
    }

    public void testChangesetFeatureType() throws IOException {
        VersionedPostgisDataStore ds = getDataStore();
        ds.getSchema(VersionedPostgisDataStore.CHANGESETS);
    }

    public void testVersionEnableDisableFeatureType() throws IOException {
        VersionedPostgisDataStore ds = getDataStore();
        FeatureType ft = ds.getSchema("road");
        assertFalse(ds.isVersioned("road"));

        // version
        ds.setVersioned("road", true, "gimbo", "Initial import of roads");
        assertTrue(ds.isVersioned("road"));
        assertEquals(ft, ds.getSchema("road"));
        if (ds.getFIDMapper("road").returnFIDColumnsAsAttributes())
            assertNotNull(ds.wrapped.getSchema("road").getAttributeType("revision"));
        assertNotNull(ds.wrapped.getSchema("road").getAttributeType("expired"));

        // un-version
        ds.setVersioned("road", false, "gimbo", "Versioning no more needed");
        assertFalse(ds.isVersioned("road"));
        assertEquals(ft, ds.getSchema("road"));
        assertNull(ds.wrapped.getSchema("road").getAttributeType("revision"));
        assertNull(ds.wrapped.getSchema("road").getAttributeType("expired"));
    }

    public void testVersionEnableChangeSets() throws IOException {
        VersionedPostgisDataStore ds = getDataStore();
        ds.getSchema(VersionedPostgisDataStore.CHANGESETS);
        assertFalse(ds.isVersioned("road"));

        // try version, should fail
        try {
            ds.setVersioned(VersionedPostgisDataStore.CHANGESETS, true, "gimbo",
                    "Initial import of roads");
            fail("It should not be possible to version enable changesets");
        } catch (IOException e) {
        }
    }

    public void testGetFeatureReader() throws IOException, NoSuchElementException, Exception {
        VersionedPostgisDataStore ds = getDataStore();

        FeatureType originalFt = ds.getSchema("road");
        ds.setVersioned("road", true, "gimbo", "version enabling stuff");
        DefaultQuery q = new DefaultQuery("road");
        FeatureReader fr = ds.wrapped.getFeatureReader(q, Transaction.AUTO_COMMIT);
        while (fr.hasNext()) {
            Feature f = fr.next();
            assertEquals(new Long(1), (Long) f.getAttribute("revision"));
            assertEquals(new Long(Long.MAX_VALUE), (Long) f.getAttribute("expired"));
        }
        fr.close();

        // now insert by hand three revisions of first road and check we can
        // extract the good ones
        SqlTestUtils.execute(pool, "INSERT INTO CHANGESETS VALUES(2, 'gimbo', default, '', null)");
        SqlTestUtils.execute(pool, "INSERT INTO CHANGESETS VALUES(3, 'gimbo', default, '', null)");
        SqlTestUtils.execute(pool, "INSERT INTO ROAD SELECT FID, ID, GEOM, 'r1 rev 2', 2, 3 "
                + "FROM ROAD WHERE ID = 1 AND EXPIRED = " + Long.MAX_VALUE);
        SqlTestUtils.execute(pool, "INSERT INTO ROAD SELECT FID, ID, GEOM, 'r1 rev 3', 3, "
                + Long.MAX_VALUE + " " + "FROM ROAD WHERE ID = 1 AND EXPIRED = " + Long.MAX_VALUE);
        SqlTestUtils.execute(pool,
                "UPDATE ROAD SET EXPIRED = 2 WHERE ID = 1 AND REVISION = 1 AND EXPIRED = "
                        + Long.MAX_VALUE);

        // non versioned data store should return two more features now
        assertEquals(roadFeatures.length + 2, ds.wrapped.getFeatureSource("road").getCount(
                Query.ALL));

        // no revision info, use last
        Filter idFilter = ff.equals(ff.property("id"), ff.literal(1l));
        q = new DefaultQuery("road", idFilter);
        fr = ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
        // make sure the type is the same as if we were working against a non
        // versioned datastore
        assertEquals(originalFt, fr.getFeatureType());
        assertTrue(fr.hasNext());
        Feature f = fr.next();
        assertEquals("road.rd1", f.getID());
        assertEquals("r1 rev 3", f.getAttribute("name"));
        assertFalse(fr.hasNext());

        // now extract revision 1
        q = new DefaultQuery("road", idFilter);
        q.setVersion("1");
        fr = ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
        assertTrue(fr.hasNext());
        f = fr.next();
        assertEquals("road.rd1", f.getID());
        assertEquals("r1", f.getAttribute("name"));
        assertFalse(fr.hasNext());

        // and now extract revision 2
        q = new DefaultQuery("road", idFilter);
        q.setVersion("2");
        fr = ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
        assertTrue(fr.hasNext());
        f = fr.next();
        assertEquals("road.rd1", f.getID());
        assertEquals("r1 rev 2", f.getAttribute("name"));
        assertFalse(fr.hasNext());

        // now try the same with a fid filter
        Filter fidFilter = ff.id(Collections.singleton(ff.featureId("road.rd1")));
        q = new DefaultQuery("road", fidFilter);

        // fid and last revision
        fr = ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
        assertTrue(fr.hasNext());
        f = fr.next();
        assertEquals("road.rd1", f.getID());
        assertEquals("r1 rev 3", f.getAttribute("name"));
        assertFalse(fr.hasNext());

        // fid and specific revision
        q = new DefaultQuery("road", fidFilter);
        q.setVersion("2");
        fr = ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
        assertTrue(fr.hasNext());
        f = fr.next();
        assertEquals("road.rd1", f.getID());
        assertEquals("r1 rev 2", f.getAttribute("name"));
        assertFalse(fr.hasNext());
    }

    public void testGetFeatureWriter() throws IOException, NoSuchElementException, Exception {
        VersionedPostgisDataStore ds = getDataStore();
        Envelope originalBounds = ds.wrapped.getFeatureSource("road").getBounds();

        // version enable road
        FeatureType originalFt = ds.getSchema("road");
        ds.setVersioned("road", true, "gimbo", "version enabling stuff");

        // build a filter to extract just road 1
        Filter filter = ff.id(Collections.singleton(ff.featureId("road.rd1")));

        // now write one revision
        Transaction t = createTransaction("gimbo", "first change");
        FeatureWriter fw = ds.getFeatureWriter("road", filter, t);
        assertTrue(fw.hasNext());
        Feature f = fw.next();
        f.setAttribute("name", "r1 rev 2");
        fw.write();
        fw.close();
        t.commit();
        assertEquals(new Long(2), t.getProperty(VersionedPostgisDataStore.REVISION));
        assertEquals("2", t.getProperty(VersionedPostgisDataStore.VERSION));

        // write another
        t = createTransaction("gimbo", "second change");
        fw = ds.getFeatureWriter("road", filter, t);
        assertTrue(fw.hasNext());
        f = fw.next();
        f.setAttribute("name", "r1 rev 3");
        fw.write();
        fw.close();
        t.commit();

        // check we have the rigth changesets in the database
        DefaultQuery q = new DefaultQuery(VersionedPostgisDataStore.CHANGESETS);
        t = new DefaultTransaction();
        FeatureReader fr = ds.getFeatureReader(q, t);
        // ... ah, would very much like to sort on revision...
        // ... first revision, import
        assertTrue(fr.hasNext());
        f = fr.next();
        assertEquals("1", f.getID());
        // TODO : get revision back among the attributes
        // assertEquals(new Long(1), f.getAttribute("revision"));
        assertEquals(originalBounds, f.getDefaultGeometry().getEnvelopeInternal());
        // ... first change
        assertTrue(fr.hasNext());
        f = fr.next();
        // assertEquals(new Long(2), f.getAttribute("revision"));
        assertEquals("first change", f.getAttribute("message"));
        assertEquals(roadFeatures[0].getDefaultGeometry().getEnvelope(), f.getDefaultGeometry()
                .getEnvelope());
        // ... second change
        assertTrue(fr.hasNext());
        f = fr.next();
        // assertEquals(new Long(2), f.getAttribute("revision"));
        assertEquals("second change", f.getAttribute("message"));
        assertEquals(roadFeatures[0].getDefaultGeometry().getEnvelope(), f.getDefaultGeometry()
                .getEnvelope());
        // finish
        assertFalse(fr.hasNext());

        // no revision info, use last
        Filter idFilter = ff.equals(ff.property("id"), ff.literal(1l));
        q = new DefaultQuery("road", idFilter);
        fr = ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
        // make sure the type is the same as if we were working against a non
        // versioned datastore
        assertEquals(originalFt, fr.getFeatureType());
        assertTrue(fr.hasNext());
        f = fr.next();
        assertEquals("r1 rev 3", f.getAttribute("name"));
        assertFalse(fr.hasNext());

        // now extract revision 1
        q = new DefaultQuery("road", idFilter);
        q.setVersion("1");
        fr = ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
        assertTrue(fr.hasNext());
        f = fr.next();
        assertEquals("r1", f.getAttribute("name"));
        assertFalse(fr.hasNext());

        // and now extract revision 2
        q = new DefaultQuery("road", idFilter);
        q.setVersion("2");
        fr = ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
        assertTrue(fr.hasNext());
        f = fr.next();
        assertEquals("r1 rev 2", f.getAttribute("name"));
        assertFalse(fr.hasNext());
    }

    public void testAppendFeatures() throws IOException, IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();

        // version enable road and lakes
        ds.setVersioned("road", true, "gimbo", "version enabling roads");
        ds.setVersioned("river", true, "gimbo", "version enabling lakes");

        // create a transaction and append some features to both feature types
        Transaction t = createTransaction("mambo", "Today I feel like adding fetures, yeah");
        FeatureWriter fw = ds.getFeatureWriterAppend("road", t);
        // ... new road
        Feature f = fw.next();
        f.setAttribute(0, new Integer(4));
        f.setAttribute(1, line(new int[] { 3, 3, 4, 4, 5, 10 }));
        f.setAttribute(2, "r4");
        fw.write();
        String rd4id = f.getID();
        fw.close();
        // ... new lake
        fw = ds.getFeatureWriterAppend("river", t);
        f = fw.next();
        f.setAttribute(0, new Integer(4));
        f.setAttribute(1, lines(new int[][] { { 0, 0, 1, 10 } }));
        f.setAttribute(2, "rv4");
        f.setAttribute(3, new Double(6.5));
        fw.write();
        String rv4id = f.getID();
        fw.close();
        // ... end
        t.commit();

        // check features are there
        // ... roads
        DefaultQuery q = new DefaultQuery("road", ff.id(Collections.singleton(ff.featureId(rd4id))));
        FeatureReader fr = ds.getFeatureReader(q, t);
        assertTrue(fr.hasNext());
        fr.close();
        // ... rivers
        q = new DefaultQuery("river", ff.id(Collections.singleton(ff.featureId(rv4id))));
        fr = ds.getFeatureReader(q, t);
        assertTrue(fr.hasNext());
        fr.close();

        // ok, now check we registered the modification to both tables in the
        // same revision
        assertEquals(3, ds.getLastRevision());
        List types = Arrays.asList(ds.getModifiedFeatureTypes("2", "3"));
        assertEquals(2, types.size());
        assertTrue(types.contains("river"));
        assertTrue(types.contains("road"));
    }

    public void testDeleteFeatures() throws IOException, NoSuchElementException,
            IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();

        // version enable road
        ds.setVersioned("road", true, "gimbo", "version enabling stuff");

        // build a filter to extract just road 1
        Filter filter = ff.id(Collections.singleton(ff.featureId("road.rd1")));

        // now delete one feature
        Transaction t = createTransaction("gimbo", "first change");
        FeatureWriter fw = ds.getFeatureWriter("road", filter, t);
        assertTrue(fw.hasNext());
        fw.next();
        fw.remove();
        fw.close();
        t.commit();

        // and now see if it's still there
        FeatureReader fr = ds.getFeatureReader(new DefaultQuery("road"), Transaction.AUTO_COMMIT);
        while (fr.hasNext())
            assertFalse(fr.next().getID().equals("road.rd1"));
        fr.close();
    }

    public void testSerialIdWriting() throws IOException, IllegalArgumentException,
            IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();
        ds.setVersioned("rail", true, "mambo", "Version enabling rails");

        Transaction t = createTransaction("serial", "Feature modification");
        FeatureWriter fw = ds.getFeatureWriter("rail", Filter.INCLUDE, t);
        assertTrue(fw.hasNext());
        Feature f = fw.next();
        f.setDefaultGeometry(line(new int[] { 0, 0, -10, -10 }));
        fw.write();
        fw.close();
        t.commit();

        fw = ds.getFeatureWriterAppend("rail", t);
        f = fw.next();
        f.setDefaultGeometry(line(new int[] { -10, -10, -20, -10 }));
        fw.write();
        assertEquals("rail.2", f.getID());
        fw.close();
        t.commit();

        fw = ds.getFeatureWriter("rail", ff.id(Collections.singleton(ff.featureId("rail.1"))), t);
        assertTrue(fw.hasNext());
        f = fw.next();
        fw.remove();
        fw.close();
        t.commit();
        t.close();
    }

    public void testModifiedIds() throws IOException, IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();
        String newId = buildRiverHistory();

        // check modified feature types are the proper ones
        // full history
        String[] modifiedTypes = ds.getModifiedFeatureTypes("1", null);
        assertEquals(1, modifiedTypes.length);
        assertEquals("river", modifiedTypes[0]);

        // get features modified in first revisions, without filters
        Transaction ac = Transaction.AUTO_COMMIT;
        // ... all history
        ModifiedFeatureIds mfids = ds.getModifiedFeatureFIDs("river", "1", "5", Filter.INCLUDE, ac);
        assertEquals(1, mfids.getCreated().size());
        assertEquals(1, mfids.getDeleted().size());
        assertEquals(1, mfids.getModified().size());
        assertTrue(mfids.getCreated().contains(newId));
        assertTrue(mfids.getDeleted().contains("river.rv2"));
        assertTrue(mfids.getModified().contains("river.rv1"));
        // ... just first modification
        mfids = ds.getModifiedFeatureFIDs("river", "1", "2", Filter.INCLUDE, ac);
        assertEquals(0, mfids.getCreated().size());
        assertEquals(0, mfids.getDeleted().size());
        assertEquals(2, mfids.getModified().size());
        assertTrue(mfids.getModified().contains("river.rv1"));
        assertTrue(mfids.getModified().contains("river.rv2"));
        // ... just second one
        mfids = ds.getModifiedFeatureFIDs("river", "2", "3", Filter.INCLUDE, ac);
        assertEquals(0, mfids.getCreated().size());
        assertEquals(0, mfids.getDeleted().size());
        assertEquals(1, mfids.getModified().size());
        assertTrue(mfids.getModified().contains("river.rv2"));
        // ... just creation and deletion
        mfids = ds.getModifiedFeatureFIDs("river", "3", "5", Filter.INCLUDE, ac);
        assertEquals(1, mfids.getCreated().size());
        assertEquals(1, mfids.getDeleted().size());
        assertEquals(0, mfids.getModified().size());
        assertTrue(mfids.getCreated().contains(newId));
        assertTrue(mfids.getDeleted().contains("river.rv2"));
        // ... a non existent one
        mfids = ds.getModifiedFeatureFIDs("river", "10", "11", Filter.INCLUDE, ac);
        assertEquals(0, mfids.getCreated().size());
        assertEquals(0, mfids.getDeleted().size());
        assertEquals(0, mfids.getModified().size());

        // now check with some filters too
        // ... a fid one
        Filter fidFilter = ff.id(Collections.singleton(ff.featureId("river.rv1")));
        mfids = ds.getModifiedFeatureFIDs("river", "1", "3", fidFilter, ac);
        assertEquals(0, mfids.getCreated().size());
        assertEquals(0, mfids.getDeleted().size());
        assertEquals(1, mfids.getModified().size());
        assertTrue(mfids.getModified().contains("river.rv1"));
        // ... a bbox one
        Filter bboxFilter = ff.bbox("geom", 100, 100, 300, 300, null);
        mfids = ds.getModifiedFeatureFIDs("river", "1", "3", bboxFilter, ac);
        assertEquals(0, mfids.getCreated().size());
        assertEquals(0, mfids.getDeleted().size());
        assertEquals(1, mfids.getModified().size());
        assertTrue(mfids.getModified().contains("river.rv2"));
        // ... a non encodable one, matching
        Filter roundedFlowFilter = ff.equals(ff.function("ceil", ff.property("flow")), ff
                .literal(10));
        mfids = ds.getModifiedFeatureFIDs("river", "1", "3", roundedFlowFilter, ac);
        assertEquals(0, mfids.getCreated().size());
        assertEquals(0, mfids.getDeleted().size());
        assertEquals(1, mfids.getModified().size());
        assertTrue(mfids.getModified().contains("river.rv1"));
        // ... same filter, but feature not modified in those revisions
        mfids = ds.getModifiedFeatureFIDs("river", "2", "3", roundedFlowFilter, ac);
        assertEquals(0, mfids.getCreated().size());
        assertEquals(0, mfids.getDeleted().size());
        assertEquals(0, mfids.getModified().size());
        // ... a non encodable one, not matching
        Filter roundedFlowFilter2 = ff.equals(ff.function("ceil", ff.property("flow")), ff
                .literal(11));
        mfids = ds.getModifiedFeatureFIDs("river", "1", "3", roundedFlowFilter2, ac);
        assertEquals(0, mfids.getCreated().size());
        assertEquals(0, mfids.getDeleted().size());
        assertEquals(0, mfids.getModified().size());
    }
    
    public void testModifiedIdsUnversioned() throws IOException, IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();

        // check we get no modifications out of an unversioned feature type
        ModifiedFeatureIds mfids = ds.getModifiedFeatureFIDs("river", "1", "5", Filter.INCLUDE, Transaction.AUTO_COMMIT);
        assertTrue(mfids.getCreated().isEmpty());
        assertTrue(mfids.getDeleted().isEmpty());
        assertTrue(mfids.getModified().isEmpty());
    }

    public void testRollbackDeleted() throws IOException, IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();
        buildRiverHistory();

        Filter rv2Filter = ff.id(Collections.singleton(ff.featureId("river.rv2")));
        FeatureReader fr = ds.getFeatureReader(new DefaultQuery("river", rv2Filter),
                Transaction.AUTO_COMMIT);
        assertFalse(fr.hasNext());
        fr.close();

        // try to rollback to revision 4, that is, rollback last deletion
        Transaction t = createTransaction("Mambo", "Gimbo, what did you do? "
                + "Now I have to rollback your changes!");
        VersionedPostgisFeatureStore fs = (VersionedPostgisFeatureStore) ds
                .getFeatureSource("river");
        fs.setTransaction(t);
        fs.rollback("4", Filter.INCLUDE);
        t.commit();

        // now check rv2 is again there
        fr = ds.getFeatureReader(new DefaultQuery("river", rv2Filter), Transaction.AUTO_COMMIT);
        assertTrue(fr.hasNext());
        fr.close();
        assertEquals(3, fs.getFeatures(Filter.INCLUDE).size());
    }

    public void testRollbackCreatedDeleted() throws IOException, IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();
        String newId = buildRiverHistory();

        // try to rollback to revision 3, that is, rollback last deletion and creation
        Transaction t = createTransaction("Mambo", "Gimbo, what did you do? "
                + "Now I have to rollback your changes!");
        VersionedPostgisFeatureStore fs = (VersionedPostgisFeatureStore) ds
                .getFeatureSource("river");
        fs.setTransaction(t);
        fs.rollback("3", Filter.INCLUDE);
        t.commit();

        // now check rv2 is again there
        Filter rv2Filter = ff.id(Collections.singleton(ff.featureId("river.rv2")));
        Filter newFilter = ff.id(Collections.singleton(ff.featureId(newId)));
        FeatureReader fr = ds.getFeatureReader(new DefaultQuery("river", rv2Filter),
                Transaction.AUTO_COMMIT);
        assertTrue(fr.hasNext());
        fr.close();
        fr = ds.getFeatureReader(new DefaultQuery("river", newFilter), Transaction.AUTO_COMMIT);
        assertFalse(fr.hasNext());
        fr.close();
        assertEquals(2, fs.getFeatures(Filter.INCLUDE).size());
    }

    public void testRollbackAll() throws IOException, IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();
        buildRiverHistory();

        // try to rollback to revision 3, that is, rollback last deletion and creation
        Transaction t = createTransaction("Mambo", "Gimbo, what did you do? "
                + "Now I have to rollback your changes!");
        VersionedPostgisFeatureStore fs = (VersionedPostgisFeatureStore) ds
                .getFeatureSource("river");
        fs.setTransaction(t);
        fs.rollback("1", Filter.INCLUDE);
        t.commit();

        // now check river features are just like at the beginning
        FeatureCollection fc = fs.getFeatures();
        assertEquals(riverFeatures.length, fc.size());
        for (int i = 0; i < riverFeatures.length; i++) {
            assertTrue(fc.contains(riverFeatures[i]));
        }
    }
    
    public void testFeatureStoreUnversioned() throws IOException, IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();

        // try to get a feature store for an unversioned type, it should be a plain feature store
        // not the versioned one
        FeatureStore fs = (FeatureStore) ds.getFeatureSource("river");
        assertFalse(fs instanceof VersionedPostgisFeatureStore);
    }

    public void testLog() throws IOException, IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();
        String newId = buildRiverHistory();
        VersionedPostgisFeatureStore fs = (VersionedPostgisFeatureStore) ds
                .getFeatureSource("river");

        // get log only for newly created features
        Filter newIdFilter = ff.id(Collections.singleton(ff.featureId(newId)));
        FeatureCollection fc = fs.getLog("1", "5", newIdFilter);
        assertEquals(1, fc.size());
        FeatureIterator it = fc.features();
        Feature f = it.next();
        assertEquals("4", f.getID());
        assertEquals("gimbo", f.getAttribute("author"));
        assertEquals("third change", f.getAttribute("message"));
        it.close();

        // get log for rv2 (most modified)
        Filter rv2IdFilter = ff.id(Collections.singleton(ff.featureId("river.rv2")));
        fc = fs.getLog("1", "5", rv2IdFilter);
        assertEquals(3, fc.size());
        it = fc.features();
        f = it.next();
        assertEquals(new Long(5), f.getAttribute("revision"));
        assertEquals("gimbo", f.getAttribute("author"));
        assertEquals("fourth change", f.getAttribute("message"));
        f = it.next();
        assertEquals(new Long(3), f.getAttribute("revision"));
        assertEquals("gimbo", f.getAttribute("author"));
        assertEquals("second change", f.getAttribute("message"));
        f = it.next();
        assertEquals(new Long(2), f.getAttribute("revision"));
        assertEquals("gimbo", f.getAttribute("author"));
        assertEquals("first change", f.getAttribute("message"));
        it.close();

        // get log for rv1
        Filter rv1IdFilter = ff.id(Collections.singleton(ff.featureId("river.rv1")));
        fc = fs.getLog("1", "5", rv1IdFilter);
        assertEquals(1, fc.size());
        it = fc.features();
        f = it.next();
        assertEquals("2", f.getID());
        assertEquals("gimbo", f.getAttribute("author"));
        assertEquals("first change", f.getAttribute("message"));
        it.close();
    }

    public void testDiff() throws IOException, IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();
        String newId = buildRiverHistory();
        VersionedPostgisFeatureStore fs = (VersionedPostgisFeatureStore) ds
                .getFeatureSource("river");

        // forward, deletion changeset
        FeatureDiffReader fdr = fs.getDifferences("4", "5", Filter.INCLUDE);
        assertTrue(fdr.hasNext());
        FeatureDiff diff = fdr.next();
        assertEquals("river.rv2", diff.getID());
        assertEquals(FeatureDiff.DELETED, diff.getState());
        assertFalse(fdr.hasNext());
        fdr.close();

        // same changeset, but backwards
        fdr = fs.getDifferences("5", "4", Filter.INCLUDE);
        assertTrue(fdr.hasNext());
        diff = fdr.next();
        assertEquals("river.rv2", diff.getID());
        assertEquals(FeatureDiff.CREATED, diff.getState());
        assertEquals("rv2 v3", diff.getChanges().get("river"));
        assertEquals(new Double(3.0), diff.getChanges().get("flow"));
        // ... can't compare directly, they have different geometry factories (afaik)
        assertTrue(DataUtilities.attributesEqual(lines(new int[][] { { 200, 200, 120, 120 } }),
                diff.getChanges().get("geom")));
        assertFalse(fdr.hasNext());
        fdr.close();

        // forward diff, two modifications on changeset 1-2
        fdr = fs.getDifferences("1", "2", Filter.INCLUDE);
        Set ids = new HashSet(Arrays.asList(new String[] { "river.rv1", "river.rv2" }));
        assertTrue(fdr.hasNext());
        while (fdr.hasNext()) {
            diff = fdr.next();
            assertTrue("Unexpected id: " + diff.getID(), ids.remove(diff.getID()));
            assertEquals("1", fdr.getFromVersion());
            assertEquals("2", fdr.getToVersion());
            assertEquals(FeatureDiff.MODIFIED, diff.state);
            if (diff.getID().equals("river.rv1")) {
                assertEquals(2, diff.getChanges().size());
                assertEquals("rv1 v2", diff.getChanges().get("river"));
                assertEquals(new Double(9.6), diff.getChanges().get("flow"));
            } else {
                assertEquals(2, diff.getChanges().size());
                assertEquals("rv2 v2", diff.getChanges().get("river"));
                assertTrue(DataUtilities.attributesEqual(
                        lines(new int[][] { { 100, 100, 120, 120 } }), diff.getChanges()
                                .get("geom")));
            }
        }
        fdr.close();
        
        // forward diff on creation
        fdr = fs.getDifferences("3", "4", Filter.INCLUDE);
        assertTrue(fdr.hasNext());
        diff = fdr.next();
        assertEquals(FeatureDiff.CREATED, diff.getState());
        assertEquals(fs.getSchema().getAttributeCount(), diff.getChanges().size());
        assertFalse(fdr.hasNext());
        fdr.close();
    }

    /**
     * Version enables rivers
     * 
     * @param ds
     * @return
     * @throws IOException
     * @throws IllegalAttributeException
     */
    protected String buildRiverHistory() throws IOException, IllegalAttributeException {
        VersionedPostgisDataStore ds = getDataStore();
        ds.setVersioned("river", true, "mambo", "version enabling stuff");

        // revision 2), modify two elements
        Transaction t = createTransaction("gimbo", "first change");
        FeatureWriter fw = ds.getFeatureWriter("river", Filter.INCLUDE, t);
        while (fw.hasNext()) {
            Feature f = fw.next();
            if (f.getID().equals("river.rv1")) {
                f.setAttribute("river", "rv1 v2");
                f.setAttribute("flow", new Double(9.6));
            } else {
                f.setAttribute("river", "rv2 v2");
                f.setAttribute("geom", lines(new int[][] { { 100, 100, 120, 120 } }));
            }
            fw.write();
        }
        fw.close();
        t.commit();

        // revision 3) modify just one
        t = createTransaction("gimbo", "second change");
        fw = ds.getFeatureWriter("river", Filter.INCLUDE, t);
        while (fw.hasNext()) {
            Feature f = fw.next();
            if (f.getID().equals("river.rv2")) {
                f.setAttribute("river", "rv2 v3");
                f.setAttribute("geom", lines(new int[][] { { 200, 200, 120, 120 } }));
            }
            fw.write();
        }
        fw.close();
        t.commit();

        // revision 4) create a new feature
        t = createTransaction("gimbo", "third change");
        fw = ds.getFeatureWriterAppend("river", t);
        Feature f = fw.next();
        f.setAttribute("id", new Integer(3));
        f.setAttribute("geom", lines(new int[][] { { 300, 300, 301, 301 } }));
        f.setAttribute("river", "rv2 v3");
        f.setAttribute("flow", new Double(12.2));
        fw.write();
        String newId = f.getID();
        fw.close();
        t.commit();

        // revision 5), delete river rv2
        t = createTransaction("gimbo", "fourth change");
        fw = ds.getFeatureWriter("river", Filter.INCLUDE, t);
        while (fw.hasNext()) {
            f = fw.next();
            if (f.getID().equals("river.rv2")) {
                fw.remove();
            } else {
                fw.write();
            }
        }
        fw.close();
        t.commit();
        return newId;
    }

    private Transaction createTransaction(String author, String message) throws IOException {
        Transaction t = new DefaultTransaction();
        t.putProperty(VersionedPostgisDataStore.AUTHOR, author);
        t.putProperty(VersionedPostgisDataStore.MESSAGE, message);
        return t;
    }

}
