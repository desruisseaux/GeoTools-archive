package org.geotools.data.mysql;

import org.geotools.jdbc.JDBCDataStoreAPITest;
import org.geotools.jdbc.JDBCDataStoreAPITestSetup;

public class MySQLDataStoreAPITest extends JDBCDataStoreAPITest {

    protected JDBCDataStoreAPITestSetup createTestSetup() {
        return new MySQLDataStoreAPITestSetup();
    }

}
