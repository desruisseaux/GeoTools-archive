package org.geotools.data.postgis;

import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;

/**
 * Hits a PostGIS database with a feature reader.
 * 
 * @author Cory Horner, Refractions Research 
 */
public class PostgisFeatureReaderOnlineTest extends AbstractPostgisOnlineTestCase {

    protected void createTables(Statement st) throws SQLException {
        createTable1(st);
        createTable3(st);
        //advance the sequence to larger values
        st.execute("SELECT setval('"+table1+"_fid_seq', 2000000000);");
        st.execute("SELECT setval('"+table3+"_fid_seq', 6000000000);");
    }
    
    /**
     * Make sure that both large integer and long values are acceptable and valid.
     * @throws Exception
     */
    public void testReadFid() throws Exception {
        assertEquals(table1+".2000000001",attemptRead(table1)); //int is signed :(
        assertEquals(table3+".6000000001",attemptRead(table3));
    }

    public boolean addFeature(String table) throws Exception {
        FeatureWriter writer = ds.getFeatureWriter(table, Transaction.AUTO_COMMIT);
        Feature feature;

        while (writer.hasNext()) {
            feature = (Feature) writer.next();
        }

        feature = (Feature) writer.next();
        feature.setAttribute(0, "test");
        writer.write();
        String id = feature.getID();
        return id != null;
    }
    
    public String attemptRead(String table) throws Exception {
        addFeature(table);
        Query query = new DefaultQuery(table);
        FeatureReader fr = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
        assertTrue(fr.hasNext());
        Feature feature = fr.next();
        String id = feature.getID();
        fr.close();
        return id;
    }
}
