package org.geotools.data.jdbc.ds;

import java.sql.Connection;

/**
 * Generic {@link Connection} unwrapper. A user can test if the unwrapper is able to unwrap a
 * certain connection, on positive answer he can call {@link #unwrap(Connection)} to get the native
 * connection
 * 
 * @author Andrea Aime - TOPP
 * 
 */
public interface UnWrapper {
    /**
     * Returns true if this unwrapper knows how to unwrap the specified connection
     * 
     * @param conn
     * @return
     */
    boolean canUnwrap(Connection conn);

    /**
     * Returns the unwrapped connection, of throws {@link IllegalArgumentException} if the passed
     * {@link Connection} is not supported ({@link #canUnwrap(Connection)} returns false}.
     * 
     * @param conn
     * @return
     */
    Connection unwrap(Connection conn);
}
