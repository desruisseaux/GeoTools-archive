package org.geotools.data.postgis;

import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.feature.IllegalAttributeException;

public class TransparentVersionedOnlineTest extends
        PostgisDataStoreAPIOnlineTest {

    public TransparentVersionedOnlineTest(String test) {
        super(test);
    }

    protected void setupDbTables() throws Exception {
        super.setupDbTables();
        // make sure versioned metadata is not in the way
        SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.TABLESCHANGED,
                false);
        SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.VERSIONEDTABLES,
                false);
        SqlTestUtils
                .dropTable(pool, VersionedPostgisDataStore.CHANGESETS, true);
    }

    public String getFixtureFile() {
        return "versioned.properties";
    }

    protected DataStore newDataStore() throws IOException {
        VersionedPostgisDataStore ds = new VersionedPostgisDataStore(pool,
                f.schema, getName(), PostgisDataStore.OPTIMIZE_SQL);
        ds.setWKBEnabled(true);
        ds.setVersioned("road", true, "gimbo", "hallabaloola");
        ds.setVersioned("river", true, "gimbo", "hallabaloola");
        return ds;
    }

    /**
     * Return true if the datastore is capable of computing the road bounds
     * given a query
     * 
     * @return
     */
    protected boolean isEnvelopeComputingEnabled() {
        return true;
    }

    public void testOidFidMapper() throws IOException,
            IllegalAttributeException {
        // we have to override this one since versioned does not support oid
        // mapper
    }

    public void testLockFeatures() throws IOException {
        // disable this test, we don't support locking at the moment
    }

    public void testUnLockFeatures() throws IOException {
        // disable this test, we don't support locking at the moment
    }

    public void testLockFeatureInteraction() throws IOException {
        // disable this test, we don't support locking at the moment
    }

    public void testGetFeatureLockingExpire() throws Exception {
        // disable this test, we don't support locking at the moment
    }
}
