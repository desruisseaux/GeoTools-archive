package org.geotools.data.postgis;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.fidmapper.PostgisFIDMapperFactory;
import org.geotools.feature.Feature;

public class PostgisFeatureWriterOnlineTest extends AbstractPostgisOnlineTestCase {

    public void testWrite() throws Exception {
    	if ( ((PostgisFIDMapperFactory) ds.getFIDMapperFactory() ).isReturningTypedFIDMapper()) {
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
    	else {
    		assertEquals("1",attemptWrite(table1));
            assertEquals("2",attemptWrite(table1));
            assertEquals("1001",attemptWrite(table2));
            assertEquals("1002",attemptWrite(table2));
            assertEquals("1",attemptWrite(table3));
            assertEquals("2",attemptWrite(table3));
            assertEquals("1000001",attemptWrite(table4));
            assertEquals("1000002",attemptWrite(table4));
            assertEquals("1",attemptWrite(table5));
            assertEquals("2",attemptWrite(table5));
            assertEquals("1001",attemptWrite(table6));
            assertEquals("1002",attemptWrite(table6));
    	
    	}
        
    }
    
    public String attemptWrite(String table) throws Exception {
        Transaction transaction = new DefaultTransaction("attemptWriteFW");
        FeatureWriter writer = ds.getFeatureWriter(table, transaction);
        Feature feature;

        while (writer.hasNext()) {
            feature = (Feature) writer.next();
        }
        
        feature = (Feature) writer.next();
        feature.setAttribute(0, "test");
        writer.write();
        String id = feature.getID();
        transaction.commit();
        transaction.close();
        return id;
    }
}
