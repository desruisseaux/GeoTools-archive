package org.geotools.data.mysql;

import org.geotools.jdbc.JDBCFeatureStoreTest;
import org.geotools.jdbc.JDBCTestSetup;

/**
 * FeatureStore test for MySQL.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class MySQLFeatureStoreTest extends JDBCFeatureStoreTest {

    protected JDBCTestSetup createTestSetup() {
        return new MySQLTestSetup();
    }

}
