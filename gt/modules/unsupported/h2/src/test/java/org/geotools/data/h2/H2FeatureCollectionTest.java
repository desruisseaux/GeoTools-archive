package org.geotools.data.h2;

import org.geotools.jdbc.JDBCFeatureCollectionTest;
import org.geotools.jdbc.JDBCTestSetup;

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
