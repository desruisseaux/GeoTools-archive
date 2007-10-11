package org.geotools.data.h2;

import org.geotools.data.jdbc.JDBCFeatureCollectionTest;
import org.geotools.data.jdbc.JDBCTestSetup;

/**
 * FeatureCollection test for H2.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class H2FeatureCollectionTest extends JDBCFeatureCollectionTest {

    protected JDBCTestSetup createTestSetup() {
        return new H2TestSetup();
    }

}
