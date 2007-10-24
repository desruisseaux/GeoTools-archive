package org.geotools.data.mysql;

import org.geotools.jdbc.JDBCFeatureCollectionTest;
import org.geotools.jdbc.JDBCTestSetup;

/**
 * FeatureCollection test for MySQL.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class MySQLFeatureCollectionTest extends JDBCFeatureCollectionTest {

    protected JDBCTestSetup createTestSetup() {
        return new MySQLTestSetup();
    }

}
