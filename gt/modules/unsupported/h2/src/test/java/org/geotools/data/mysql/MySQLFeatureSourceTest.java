package org.geotools.data.mysql;

import org.geotools.jdbc.JDBCFeatureSourceTest;
import org.geotools.jdbc.JDBCTestSetup;

public class MySQLFeatureSourceTest extends JDBCFeatureSourceTest {

    protected JDBCTestSetup createTestSetup() {
        return new MySQLTestSetup();
    }

}
