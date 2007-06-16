package org.geotools.data.jdbc.ds;

import java.sql.Connection;

import org.apache.commons.dbcp.DelegatingConnection;

/**
 * Unwraps DBCP managed connections
 * 
 * @author Andrea Aime - TOPP
 * 
 */
public class DBCPUnWrapper implements UnWrapper {

    public boolean canUnwrap(Connection conn) {
        return conn instanceof DelegatingConnection;
    }

    public Connection unwrap(Connection conn) {
        if (!canUnwrap(conn))
            throw new IllegalArgumentException("This unwrapper can only handle instances of "
                    + DelegatingConnection.class);
        Connection unwrapped = ((DelegatingConnection) conn).getInnermostDelegate();
        if (unwrapped == null)
            throw new RuntimeException("Could not unwrap connection. Is the DBCP pool configured "
                    + "to allow access to underlying connections?");
        return unwrapped;
    }

}
