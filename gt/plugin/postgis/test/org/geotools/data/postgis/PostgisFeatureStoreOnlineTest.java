package org.geotools.data.postgis;

import java.util.Set;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;

public class PostgisFeatureStoreOnlineTest extends AbstractPostgisOnlineTestCase {

    public void testWrite() throws Exception {
        assertEquals(table1+".1",attemptWrite(table1));
        assertEquals(table1+".2",attemptWrite(table1));
        assertEquals(table2+".1001",attemptWrite(table2));
        assertEquals(table2+".1002",attemptWrite(table2));
        assertEquals(table3+".1",attemptWrite(table3));
        assertEquals(table3+".2",attemptWrite(table3));
        assertEquals(table4+".1000001",attemptWrite(table4));
        assertEquals(table4+".1000002",attemptWrite(table4));
        assertEquals(table5+".1",attemptWrite(table5));
        assertEquals(table5+".2",attemptWrite(table5));
        assertEquals(table6+".1001",attemptWrite(table6));
        assertEquals(table6+".1002",attemptWrite(table6));
    }
    
    public String attemptWrite(String table) throws Exception {
        Transaction transaction = new DefaultTransaction("attemptWriteFS");
        FeatureStore fs = (FeatureStore) ds.getFeatureSource(table);
        fs.setTransaction(transaction);
        FeatureType ft = fs.getSchema();
        FeatureCollection fc = FeatureCollections.newCollection();
        Feature feature = ft.create(new Object[] {"test"});
        fc.add(feature);
        Set set = fs.addFeatures(fc);
        String id = (String) set.toArray()[0];
        transaction.commit();
        
        return id;
    }
}
