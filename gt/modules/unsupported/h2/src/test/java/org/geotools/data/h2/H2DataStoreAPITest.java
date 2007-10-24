package org.geotools.data.h2;

import org.geotools.jdbc.JDBCDataStoreAPITestSetup;
import org.geotools.jdbc.JDBCDataStoreAPITest;

public class H2DataStoreAPITest extends JDBCDataStoreAPITest {

    protected JDBCDataStoreAPITestSetup createTestSetup() {
        return new H2DataStoreAPITestSetup();
    }

}
