package org.geotools.data.mysql;

import junit.framework.TestResult;

import org.geotools.data.jdbc.JDBCDataStoreTest;
import org.geotools.data.jdbc.JDBCTestSetup;

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
