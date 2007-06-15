package org.geotools.data.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This is an interface, it has the same method signature as the origional ConnectionPool.
 * It can be used in the constructor of JDBCDataStore (instead of ConnectionPool), allowing
 * us to create either ConnectionPool (the origional) or DataSourceAccess (the replacement
 * that wraps up a normal java.sql.DataSource.
 * 
 * @author Jody Garnett
 */
public interface ConnectionManager {
    /**
     * Borrow a connection, please close after use so we can recycle it.
     * @return
     * @throws SQLException 
     */
    Connection getConnection() throws SQLException;
    /**
     * Clean up everything we can; all outstanding Connections would be rendered
     * unfunctional.
     */
    void close();
    /**
     * Check if this ConnectionAccess is already closed.
     * @return
     */
    boolean isClosed();
}
