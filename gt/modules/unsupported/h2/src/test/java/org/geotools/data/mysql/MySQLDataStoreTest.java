package org.geotools.data.mysql;

import org.geotools.jdbc.JDBCDataStoreTest;
import org.geotools.jdbc.JDBCTestSetup;

/**
 * Data store test for mysql.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class MySQLDataStoreTest extends JDBCDataStoreTest {

    protected JDBCTestSetup createTestSetup() {
        return new MySQLTestSetup();
    }
    
}
