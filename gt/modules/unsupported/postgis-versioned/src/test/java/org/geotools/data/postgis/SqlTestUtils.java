package org.geotools.data.postgis;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCUtils;

/**
 * A few methods shared by multiple classes
 * 
 * @author aaime
 * 
 */
public class SqlTestUtils {

    protected static void dropTable(ConnectionPool pool, String tableName, boolean geomColumns)
            throws SQLException {
        Connection conn = null;
        Statement st = null;
        try {
            conn = pool.getConnection();
            st = conn.createStatement();
            if (geomColumns)
                st.execute("DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = '" + tableName + "'");
            st.execute("DROP TABLE " + tableName);
        } catch (SQLException e) {
            // no problem, the drop may fail
            System.out.println(e.getMessage());
        } finally {
            JDBCUtils.close(st);
            JDBCUtils.close(conn, null, null);
        }
    }

    protected static void execute(ConnectionPool pool, String sql) throws SQLException {
        Connection conn = null;
        Statement st = null;
        try {
            conn = pool.getConnection();
            st = conn.createStatement();
            st.execute(sql);
            conn.commit();
        } finally {
            JDBCUtils.close(st);
            JDBCUtils.close(conn, null, null);
        }
    }

}
