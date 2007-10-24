package org.geotools.data.h2;

import org.geotools.jdbc.JDBCDataStoreTest;
import org.geotools.jdbc.JDBCTestSetup;

/**
 * Datastore test for H2.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class H2DataStoreTest extends JDBCDataStoreTest {

    protected JDBCTestSetup createTestSetup() {
        return new H2TestSetup();
    }

}
