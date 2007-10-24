package org.geotools.data.mysql;

import org.geotools.data.jdbc.JDBCDataStoreAPITestSetup;
import org.geotools.data.jdbc.JDBCDataStoreAPITestSupport;

public class MySQLDataStoreAPITest extends JDBCDataStoreAPITestSupport {

    protected JDBCDataStoreAPITestSetup createTestSetup() {
        return new MySQLDataStoreAPITestSetup();
    }

}
