package org.geotools.data.mysql;

import org.geotools.data.jdbc.JDBCFeatureSourceTest;
import org.geotools.data.jdbc.JDBCTestSetup;

public class MySQLFeatureSourceTest extends JDBCFeatureSourceTest {

    protected JDBCTestSetup createTestSetup() {
        return new MySQLTestSetup();
    }

}
