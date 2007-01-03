package org.geotools.data.postgis;

import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.feature.IllegalAttributeException;

public class TransparentUnversionedOnlineTest extends PostgisDataStoreAPIOnlineTest {

    public TransparentUnversionedOnlineTest(String test) {
        super(test);
    }

    protected void setupDbTables() throws Exception {
        super.setupDbTables();
        // make sure versioned metadata is not in the way
        SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.TABLESCHANGED, false);
        SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.VERSIONEDTABLES, false);
        SqlTestUtils.dropTable(pool, VersionedPostgisDataStore.CHANGESETS, true);
    }

    public String getFixtureFile() {
        return "versioned.properties";
    }

    protected DataStore newDataStore() throws IOException {
        VersionedPostgisDataStore ds = new VersionedPostgisDataStore(pool, f.schema, getName(),
                PostgisDataStore.OPTIMIZE_SQL);
        ds.setWKBEnabled(true);
        return ds;
    }
    
    /**
     * Return true if the datastore is capable of computing the road bounds given a query
     * @return
     */
    protected boolean isEnvelopeComputingEnabled() {
        return true;
    }
    
    public void testOidFidMapper() throws IOException, IllegalAttributeException {
        // we have to override this one since versioned does not support oid mapper
    }
    
//    public FeatureReader reader(String typeName) throws IOException {
//        FeatureType type = data.getSchema(typeName);
//
//        return data.getFeatureReader(new DefaultQuery(typeName, Filter.NONE), transaction);
//    }
//
//    public FeatureWriter writer(String typeName) throws IOException {
//        return data.getFeatureWriter(typeName, Transaction.AUTO_COMMIT);
//    }
//    
//    private Transaction createTransaction(String author, String message) throws IOException {
//        Transaction t = new DefaultTransaction();
//        t.putProperty(VersionedPostgisDataStore.AUTHOR, author);
//        t.putProperty(VersionedPostgisDataStore.MESSAGE, message);
//        return t;
//    }
}
