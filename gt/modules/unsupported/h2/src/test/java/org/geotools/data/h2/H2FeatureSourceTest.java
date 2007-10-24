package org.geotools.data.h2;

import org.geotools.jdbc.JDBCFeatureSourceTest;
import org.geotools.jdbc.JDBCTestSetup;

/**
 * FeatureSource test for H2.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class H2FeatureSourceTest extends JDBCFeatureSourceTest {

    protected JDBCTestSetup createTestSetup() {
        return new H2TestSetup();
    }

}
