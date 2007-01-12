package org.geotools.data.postgis.collection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.postgis.PostgisDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.test.OnlineTestCase;
import org.opengis.filter.Filter;

public class TableFeatureCollectionTest extends OnlineTestCase {

    protected PostgisDataStore dataStore;

    protected final String DRIVER_CLASS = "org.postgresql.Driver";
    protected final String DRIVER_PATH = "jdbc:postgresql";

    /** Connection that can be used to test interaction */
    private Connection connection;

    protected void connect() throws Exception {
        //connection to postgis
    }

    protected void disconnect() throws Exception {
 
    }

    protected String getFixtureId() {
        return null;
    }

    public void testTable() throws Exception {
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource featureSource = dataStore.getFeatureSource( typeName );        
        FeatureCollection all = featureSource.getFeatures();
        
        assertTrue( all instanceof TableFeatureCollection );
        
        assertEquals( featureSource.getBounds(), all.getBounds() );        
        assertSame( all, all.subCollection( Filter.INCLUDE ) );
    }
}
