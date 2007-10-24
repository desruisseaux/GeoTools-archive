package org.geotools.data.mysql;

import org.geotools.jdbc.JDBCTestSetup;
import org.geotools.jdbc.JDBCTransactionTest;

/**
 * Transation test for MySQL.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class MySQLTransactionTest extends JDBCTransactionTest {

    protected JDBCTestSetup createTestSetup() {
        return new MySQLTestSetup();
    }

}
