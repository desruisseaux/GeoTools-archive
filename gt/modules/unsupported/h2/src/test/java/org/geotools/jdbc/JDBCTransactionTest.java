package org.geotools.jdbc;

import java.io.IOException;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.resources.JDBC;
import org.opengis.feature.simple.SimpleFeature;

public abstract class JDBCTransactionTest extends JDBCTestSupport {

    public void testCommit() throws IOException {
        JDBCFeatureStore fs = (JDBCFeatureStore) dataStore.getFeatureSource("ft1");
        
        Transaction tx = new DefaultTransaction();
        //tx.putState( fs, new JDBCTransactionState( fs ) );
        
        FeatureWriter writer = dataStore.getFeatureWriterAppend("ft1", tx );
        SimpleFeature feature = writer.next();
        feature.setAttribute( "intProperty", new Integer( 100 ) );
        writer.write();
        writer.close();
        tx.commit();
        tx.close();
        
        FeatureCollection fc = dataStore.getFeatureSource("ft1").getFeatures();
        assertEquals( 4, fc.size() );
    }
    
    public void testNoCommit() throws IOException {
        
        JDBCFeatureStore fs = (JDBCFeatureStore) dataStore.getFeatureSource("ft1");
        
        Transaction tx = new DefaultTransaction();
        //tx.putState( fs, new JDBCTransactionState( fs ) );
        
        FeatureWriter writer = dataStore.getFeatureWriterAppend("ft1", tx );
        SimpleFeature feature = writer.next();
        feature.setAttribute( "intProperty", new Integer( 100 ) );
        writer.write();
        writer.close();
        tx.rollback();
        tx.close();
        
        FeatureCollection fc = dataStore.getFeatureSource("ft1").getFeatures();
        assertEquals( 3, fc.size() );
    }
    
    
    public void testMultipleTransactions() throws IOException {
        JDBCFeatureStore fs = (JDBCFeatureStore) dataStore.getFeatureSource("ft1");
        
        Transaction tx1 = new DefaultTransaction();
        //tx1.putState( fs, new JDBCTransactionState( fs ) );
        
        Transaction tx2 = new DefaultTransaction();
        //tx2.putState( fs, new JDBCTransactionState( fs ) );
        
        FeatureWriter w1 = dataStore.getFeatureWriterAppend("ft1", tx1 );
        FeatureWriter w2 = dataStore.getFeatureWriterAppend("ft1", tx2 );
        
        SimpleFeature f1 = w1.next();
        SimpleFeature f2 = w2.next();
        
        f1.setAttribute( "intProperty", new Integer( 100 ) );
        f2.setAttribute( "intProperty", new Integer( 101 ) );
        
        w1.write();
        w2.write();
        
        w1.close();
        w2.close();
        
        tx1.commit();
        tx2.commit();
        
        FeatureCollection fc = dataStore.getFeatureSource("ft1").getFeatures();
        assertEquals( 5, fc.size() );
        
    }
}
