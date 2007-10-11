package org.geotools.data.h2;

import org.geotools.data.jdbc.JDBCFeatureStoreTest;
import org.geotools.data.jdbc.JDBCTestSetup;

/**
 * FeatureStore test for H2.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class H2FeatureStoreTest extends JDBCFeatureStoreTest {

    protected JDBCTestSetup createTestSetup() {
        return new H2TestSetup();
    }

}
