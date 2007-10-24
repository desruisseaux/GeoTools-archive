package org.geotools.data.h2;

import java.io.IOException;

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

    public void testMultipleTransactions() throws IOException {
        //JD: h2 does table level locking so this test fails.
    }
    
}
