package org.geotools.data.h2;

import org.geotools.jdbc.JDBCTestSetup;
import org.geotools.jdbc.JDBCTransactionTest;

/**
 * Transaction test for H2.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class H2JDBCTransactionTest extends JDBCTransactionTest {

    protected JDBCTestSetup createTestSetup() {
        return new H2TestSetup();
    }

}
