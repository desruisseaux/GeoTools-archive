package org.geotools.data.h2;

import org.geotools.data.jdbc.JDBCDataStoreAPITestSetup;
import org.geotools.data.jdbc.JDBCDataStoreAPITestSupport;

public class H2DataStoreAPITest extends JDBCDataStoreAPITestSupport {

    protected JDBCDataStoreAPITestSetup createTestSetup() {
        return new H2DataStoreAPITestSetup();
    }

}
